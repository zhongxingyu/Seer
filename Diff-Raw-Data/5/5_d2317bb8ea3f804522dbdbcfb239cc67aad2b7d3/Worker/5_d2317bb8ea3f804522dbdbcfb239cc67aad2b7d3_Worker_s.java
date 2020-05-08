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
 package org.rsna.isn.transfercontent;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Map;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.openhealthtools.ihe.utils.IHEException;
 import org.rsna.isn.dao.JobDao;
 import org.rsna.isn.domain.DicomStudy;
 import org.rsna.isn.domain.Exam;
 import org.rsna.isn.domain.Job;
 import org.rsna.isn.transfercontent.dcm.KosGenerator;
 import org.rsna.isn.transfercontent.ihe.ClearinghouseException;
 import org.rsna.isn.transfercontent.ihe.Iti41;
 import org.rsna.isn.transfercontent.ihe.Iti8;
 import org.rsna.isn.util.Environment;
 
 /**
  * Worker thread that processes jobs.
  * 
  * @author Wyatt Tellis
  * @version 2.1.0
  */
 class Worker extends Thread
 {
 	private static final Logger logger = Logger.getLogger(Worker.class);
 
 	private static final File dcmDir = Environment.getDcmDir();
 
 	private static final File tmpDir = Environment.getTmpDir();
 
 	private final Job job;
 
 	private final Exam exam;
 
 	Worker(ThreadGroup group, Job job)
 	{
 		super(group, "worker-" + job.getJobId());
 
 		this.job = job;
 
 		this.exam = job.getExam();
 	}
 
 	@Override
 	public void run()
 	{
 		logger.info("Started worker thread for " + job);
 
 		try
 		{
 			JobDao dao = new JobDao();
 			try
 			{
 				//
 				// Generate KOS objects
 				//
 				Map<String, DicomStudy> studies = Collections.EMPTY_MAP;
 				try
 				{
 					dao.updateStatus(job, Job.RSNA_STARTED_KOS_GENERATION);
 
 					logger.info("Started KOS generation for " + job);
 
 					KosGenerator gen = new KosGenerator(job);
 					studies = gen.processFiles();
 
 					logger.info("Completed KOS generation for " + job);
 				}
 				catch (IOException ex)
 				{
 					logger.error("Unable to generate KOS for " + job + ". ", ex);
 
 					dao.updateStatus(job, Job.RSNA_FAILED_TO_GENERATE_KOS, ex);
 
 					return;
 				}
 
 
 
 
 				//
 				// Register patient
 				//
 				try
 				{
 					dao.updateStatus(job, Job.RSNA_STARTED_PATIENT_REGISTRATION);
 
 					logger.info("Started patient registration for " + job);
 
 					Iti8 iti8 = new Iti8(job);
 					iti8.registerPatient();
 
 					logger.info("Completed patient registration for " + job);
 				}
 				catch (ClearinghouseException ex)
 				{
 					String chMsg = ex.getMessage();
 					String errorMsg = "Unable to register patient for "
 							+ job + ". " + chMsg;
 
 					logger.error(errorMsg);
 
 					dao.updateStatus(job, Job.RSNA_FAILED_TO_REGISTER_PATIENT, chMsg);
 
 					return;
 				}
 				catch (IHEException ex)
 				{
 					logger.error("Unable to register patient for " + job + ". ", ex);
 
 					dao.updateStatus(job, Job.RSNA_FAILED_TO_REGISTER_PATIENT, ex);
 
 					return;
 				}
 
 				//
 				// Submit documents to registry
 				//
 				try
 				{
 					dao.updateStatus(job, Job.RSNA_STARTED_DOCUMENT_SUBMISSION);
 
 					logger.info("Started document submission for " + job);
 
 					File jobDir = new File(tmpDir, Integer.toString(job.getJobId()));
 					File studiesDir = new File(jobDir, "studies");
 					for (DicomStudy study : studies.values())
 					{
 						File studyDir = new File(studiesDir, study.getStudyUid());
 						File debugFile = new File(studyDir, "submission-set.xml");
 
 						Iti41 iti41 = new Iti41(study);
 						iti41.submitDocuments(debugFile);
 					}
 
 					logger.info("Completed document submission for " + job);
 				}
 				catch (ClearinghouseException ex)
 				{
 					String chMsg = ex.getMessage();
 					String errorMsg = "Unable to submit documents for "
 							+ job + ". " + chMsg;
 
 					logger.error(errorMsg);
 
 					dao.updateStatus(job, Job.RSNA_FAILED_TO_SUBMIT_DOCUMENTS, chMsg);
 
 					return;
 				}
 				catch (Exception ex)
 				{
 					logger.error("Unable to submit documents for " + job + ". ", ex);
 
 					dao.updateStatus(job, Job.RSNA_FAILED_TO_SUBMIT_DOCUMENTS, ex);
 
 					return;
 				}
 
 				File jobDir = new File(tmpDir, Integer.toString(job.getJobId()));
 				FileUtils.deleteDirectory(jobDir);
 
 				
 				File jobDcmDir = new File(dcmDir, Integer.toString(job.getJobId()));
 				FileUtils.deleteDirectory(jobDcmDir);
 
 				dao.updateStatus(job, Job.RSNA_COMPLETED_TRANSFER_TO_CLEARINGHOUSE);
 
 				logger.info("Successfully transferred content to clearinghouse for " + job);
 			}
			catch (Exception ex)
 			{
 				logger.error("Uncaught exception while processing job " + job, ex);
 
 				dao.updateStatus(job, Job.RSNA_FAILED_TO_TRANSFER_TO_CLEARINGHOUSE, ex);
 			}
 
 
 
 
 		}
		catch (Exception ex)
 		{
 			logger.error("Uncaught exception while updating job " + job, ex);
 		}
 		finally
 		{
 			logger.info("Stopped worker thread");
 		}
 	}
 
 }
