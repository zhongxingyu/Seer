 /* Copyright (c) <2010>, <Radiological Society of North America>
  * All rights reserved.
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  * Neither the name of the <RSNA> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGE.
  */
 package org.rsna.isn.prepcontent.dcm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executor;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.log4j.Logger;
 import org.dcm4che2.data.BasicDicomObject;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.data.UID;
 import org.dcm4che2.data.VR;
 import org.dcm4che2.net.Association;
 import org.dcm4che2.net.CommandUtils;
 import org.dcm4che2.net.DimseRSP;
 import org.dcm4che2.net.DimseRSPHandler;
 import org.dcm4che2.net.ExtRetrieveTransferCapability;
 import org.dcm4che2.net.NetworkApplicationEntity;
 import org.dcm4che2.net.NetworkConnection;
 import org.dcm4che2.net.NewThreadExecutor;
 import org.dcm4che2.net.TransferCapability;
 import org.rsna.isn.dao.ConfigurationDao;
 import org.rsna.isn.dao.JobDao;
 import org.rsna.isn.domain.Device;
 import org.rsna.isn.domain.Exam;
 import org.rsna.isn.domain.Job;
 
 /**
  * A collection of DICOM utility functions.  Mostly used by the worker
  * thread.
  *
  * @author Wyatt Tellis
  * @version 1.2.0
  *
  */
 public class DcmUtil
 {
 	private static final Logger logger = Logger.getLogger(DcmUtil.class);
 
 	private static final String moveUids[] =
 	{
		UID.StudyRootQueryRetrieveInformationModelMOVE
		//UID.PatientRootQueryRetrieveInformationModelMOVE,
 	//UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired
 	};
 
 	private static final String findUids[] =
 	{
 		UID.PatientRootQueryRetrieveInformationModelFIND
 	};
 
 	private static final String transferSyntaxes[] =
 	{
 		UID.ImplicitVRLittleEndian,
 		UID.ExplicitVRLittleEndian
 	};
 
 	private static TransferCapability capabilities[] =
 			new TransferCapability[moveUids.length + findUids.length];
 
 	static
 	{
 		for (int i = 0; i < moveUids.length; i++)
 		{
 			String cuid = moveUids[i];
 
 			ExtRetrieveTransferCapability tc = new ExtRetrieveTransferCapability(
 					cuid, transferSyntaxes, TransferCapability.SCU);
 			tc.setExtInfoBoolean(ExtRetrieveTransferCapability.RELATIONAL_RETRIEVAL, true);
 
 			capabilities[i] = tc;
 		}
 
 		for (int i = moveUids.length, j = 0;
 				i < capabilities.length && j < findUids.length;
 				i++, j++)
 		{
 			String cuid = findUids[j];
 
 			ExtRetrieveTransferCapability tc = new ExtRetrieveTransferCapability(
 					cuid, transferSyntaxes, TransferCapability.SCU);
 			tc.setExtInfoBoolean(ExtRetrieveTransferCapability.RELATIONAL_RETRIEVAL, true);
 
 			capabilities[i] = tc;
 		}
 	}
 
 	private DcmUtil()
 	{
 	}
 
 	/**
 	 * Attempts a DICOM C-MOVE against the specified remote device.
 	 * @param device The remote device.
 	 * @param job The job to process
 	 * @return True if objects were successfully retrieved, false if not
 	 * @throws Exception If there was an error attempting the C-MOVE.
 	 */
 	public static boolean doCMove(Device device, Job job) throws Exception
 	{
 		List<String> studyUids = findStudyUids(device, job);
 
 		if (studyUids.isEmpty())
 			return false;
 
 		for (String studyUid : studyUids)
 		{
 			String error = moveStudy(device, studyUid);
 
 			if (error != null)
 			{
 				JobDao dao = new JobDao();
 				dao.updateStatus(job, Job.RSNA_DICOM_C_MOVE_FAILED, error);
 
 				return true;
 			}
 		}
 
 		JobDao dao = new JobDao();
 		dao.updateStatus(job, Job.RSNA_WAITING_FOR_TRANSFER_CONTENT);
 
 		return true;
 	}
 
 	private static List<String> findStudyUids(Device device, Job job) throws Exception
 	{
 		List<String> uids = new ArrayList();
 
 		Exam exam = job.getExam();
 		String mrn = exam.getMrn();
 		String accNum = exam.getAccNum();
 
 		Association assoc = connect(device, "query-" + accNum);
 
 		TransferCapability tc = null;
 		for (int i = 0; i < moveUids.length; i++)
 		{
 			tc = assoc.getTransferCapabilityAsSCU(findUids[i]);
 			if (tc != null)
 				break;
 		}
 
 		if (tc == null)
 		{
 
 			assoc.release(true);
 
 			logger.warn("C-FIND not supported by "
 					+ device.getAeTitle());
 		}
 
 		String cuid = tc.getSopClass();
 		String tsuid = tc.getTransferSyntax()[0];
 
 		DicomObject keys = new BasicDicomObject();
 		keys.putString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
 		keys.putString(Tag.PatientID, VR.LO, mrn);
 		keys.putString(Tag.AccessionNumber, VR.SH, accNum);
 		keys.putString(Tag.StudyInstanceUID, VR.SH, null);
 
 		DimseRSP rsp = assoc.cfind(cuid, 0, keys, tsuid, Integer.MAX_VALUE);
 		while (rsp.next())
 		{
 			DicomObject cmd = rsp.getCommand();
 			if (CommandUtils.isPending(cmd))
 			{
 				DicomObject dataset = rsp.getDataset();
 				String studyUid = dataset.getString(Tag.StudyInstanceUID);
 				uids.add(studyUid);
 			}
 		}
 
 		assoc.release(true);
 
 		return uids;
 	}
 
 	private static String moveStudy(Device device, String studyUid) throws Exception
 	{
 		Association assoc = connect(device, "transfer-" + studyUid);
 
 		TransferCapability tc = null;
 		for (int i = 0; i < moveUids.length; i++)
 		{
 			tc = assoc.getTransferCapabilityAsSCU(moveUids[i]);
 			if (tc != null)
 				break;
 		}
 
 		if (tc == null)
 		{
 
 			assoc.release(true);
 
 			return "C-MOVE not supported by " + device.getAeTitle();
 		}
 		else
 		{
 			String cuid = tc.getSopClass();
 			String tsuid = tc.getTransferSyntax()[0];
 
 			DicomObject keys = new BasicDicomObject();
 			keys.putString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
 			keys.putString(Tag.StudyInstanceUID, VR.SH, studyUid);
 
                         ConfigurationDao confDao = new ConfigurationDao();
                         String scpAeTitle = confDao.getConfiguration("scp-ae-title");
 
 			CMoveResponseHandler handler = new CMoveResponseHandler();
 			assoc.cmove(cuid, 0, keys, tsuid, scpAeTitle, handler);
 
 			assoc.waitForDimseRSP();
 
 			assoc.release(true);
 
 
 			if (handler.completed + handler.warning + handler.failed == 0)
 			{
 				return "No objects for study " + studyUid
 						+ " on " + device.getAeTitle();
 			}
 			else
 			{
 				int status;
 				String msg;
 				if (handler.warning > 0 || handler.failed > 0)
 				{
 					return "Unable to retrieve study. There were "
 							+ handler.warning + " warnings and "
 							+ handler.failed + " failures";
 				}
 				else
 				{
 					return null;
 				}
 			}
 		}
 	}
 
 	private static Association connect(Device device, String threadName) throws Exception
 	{
 		NetworkConnection remoteConn = new NetworkConnection();
 		remoteConn.setHostname(device.getHost());
 		remoteConn.setPort(device.getPort());
 
 		NetworkApplicationEntity remoteAe = new NetworkApplicationEntity();
 		remoteAe.setAETitle(device.getAeTitle());
 		remoteAe.setAssociationAcceptor(true);
 		remoteAe.setNetworkConnection(remoteConn);
 
 
 		NetworkConnection localConn = new NetworkConnection();
 
                 ConfigurationDao confDao = new ConfigurationDao();
                 String scuAeTitle = confDao.getConfiguration("scu-ae-title");
 
 		NetworkApplicationEntity localAe = new NetworkApplicationEntity();
 		localAe.setAETitle(scuAeTitle);
 		localAe.setAssociationInitiator(true);
 		localAe.setNetworkConnection(localConn);
                 localAe.setRetrieveRspTimeout((int) DateUtils.MILLIS_PER_DAY);
 		localAe.setTransferCapability(capabilities);
 
 
 
 		org.dcm4che2.net.Device dcmDevice = new org.dcm4che2.net.Device();
 		dcmDevice.setNetworkApplicationEntity(localAe);
 		dcmDevice.setNetworkConnection(localConn);
 
 
 		Executor executor = new NewThreadExecutor("transfer-" + threadName);
 
 		return localAe.connect(remoteAe, executor);
 	}
 
 	private static class CMoveResponseHandler extends DimseRSPHandler
 	{
 		private int completed = 0;
 
 		private int warning = 0;
 
 		private int failed = 0;
 
 		@Override
 		public void onDimseRSP(Association as, DicomObject cmd, DicomObject data)
 		{
 			if (!CommandUtils.isPending(cmd))
 			{
 				completed += cmd.getInt(Tag.NumberOfCompletedSuboperations);
 				warning += cmd.getInt(Tag.NumberOfWarningSuboperations);
 				failed += cmd.getInt(Tag.NumberOfFailedSuboperations);
 			}
 		}
 
 	}
 }
