 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chee.archive.net.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 
 import javax.xml.transform.Templates;
 
 import org.dcm4che.data.Attributes;
 import org.dcm4che.data.Tag;
 import org.dcm4che.data.VR;
 import org.dcm4che.io.DicomInputStream;
 import org.dcm4che.io.SAXTransformer;
 import org.dcm4che.net.Association;
 import org.dcm4che.net.Dimse;
 import org.dcm4che.net.Status;
 import org.dcm4che.net.TransferCapability;
 import org.dcm4che.net.pdu.PresentationContext;
 import org.dcm4che.net.service.BasicCStoreSCP;
 import org.dcm4che.net.service.DicomServiceException;
 import org.dcm4che.util.AttributesFormat;
 import org.dcm4che.util.SafeClose;
 import org.dcm4che.util.TagUtils;
 import org.dcm4chee.archive.ejb.store.InstanceStore;
 import org.dcm4chee.archive.net.ArchiveApplicationEntity;
 import org.dcm4chee.archive.persistence.FileRef;
 import org.dcm4chee.archive.persistence.FileSystem;
 
 /**
  * @author Gunter Zeilinger <gunterze@gmail.com>
  */
 public class CStoreSCPImpl extends BasicCStoreSCP {
 
     private boolean initFileSystem = true;
     private IanSCU ianSCU;
 
     public CStoreSCPImpl(String... sopClasses) {
         super(sopClasses);
     }
 
     public final IanSCU getIanSCU() {
         return ianSCU;
     }
 
     public final void setIanSCU(IanSCU ianSCU) {
         this.ianSCU = ianSCU;
     }
 
     @Override
     protected Object selectStorage(Association as, Attributes rq) throws DicomServiceException {
         try {
             ArchiveApplicationEntity ae = (ArchiveApplicationEntity) as.getApplicationEntity();
             String fsGroupID = ae.getFileSystemGroupID();
             if (fsGroupID == null)
                 throw new IllegalStateException(
                         "No File System Group ID configured for " + ae.getAETitle());
             InstanceStore store = initInstanceStore(as);
             if (initFileSystem) {
                 store.initFileSystem(fsGroupID);
                 initFileSystem = false;
             }
             return store.selectFileSystem(fsGroupID);
         } catch (Exception e) {
             LOG.warn(as + ": Failed to select filesystem:", e);
             throw new DicomServiceException(Status.OutOfResources, e);
         }
     }
 
     private static class LazyInitialization {
         static final SecureRandom random = new SecureRandom();
 
         static int nextInt() {
             int n = random.nextInt();
             return n < 0 ? -(n+1) : n;
         }
     }
 
     @Override
     protected File createFile(Association as, Attributes rq, Object storage)
             throws DicomServiceException {
         try {
             FileSystem fs = (FileSystem) storage;
             String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
             ArchiveApplicationEntity ae = (ArchiveApplicationEntity) as.getApplicationEntity();
             String subdir = ae.getReceivingDirectoryPath();
             File dir = subdir != null ? new File(fs.getDirectory(), subdir)
                                       : fs.getDirectory();
             dir.mkdirs();
             File file = new File(dir, iuid);
             while (!file.createNewFile())
                 file = new File(dir, Integer.toString(LazyInitialization.nextInt()));
             return file;
         } catch (Exception e) {
             LOG.warn(as + ": Failed to create file:", e);
             throw new DicomServiceException(Status.OutOfResources, e);
         }
     }
 
     @Override
     protected MessageDigest getMessageDigest(Association as) {
         ArchiveApplicationEntity ae = (ArchiveApplicationEntity) as.getApplicationEntity();
         String algorithm = ae.getDigestAlgorithm();
         try {
             return algorithm != null ? MessageDigest.getInstance(algorithm) : null;
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
    protected boolean process(Association as, PresentationContext pc, Attributes rq,
            Attributes rsp, Object storage, FileHolder fileHolder, MessageDigest digest)
             throws DicomServiceException {
         FileSystem fs = (FileSystem) storage;
        Attributes ds = readDataset(as, rq, fileHolder.getFile());
         if (ds.bigEndian())
             ds = new Attributes(ds, false);
         String sourceAET = as.getRemoteAET();
         String cuid = rq.getString(Tag.AffectedSOPClassUID);
         ArchiveApplicationEntity ae = (ArchiveApplicationEntity) as.getApplicationEntity();
         try {
             Attributes modified = new Attributes();
             Templates tpl = ae.getAttributeCoercionTemplates(cuid, Dimse.C_STORE_RQ,
                     TransferCapability.Role.SCP, sourceAET);
             if (tpl != null)
                 ds.update(SAXTransformer.transform(ds, tpl, false, false), modified);
             AttributesFormat filePathFormat = ae.getStorageFilePathFormat();
            if (filePathFormat != null)
                fileHolder.setFile(rename(as, rq, fs, fileHolder.getFile(),
                        format(filePathFormat, ds)));
            File dst = fileHolder.getFile();
             String filePath = dst.toURI().toString().substring(fs.getURI().length());
             InstanceStore store = (InstanceStore) as.getProperty(InstanceStore.JNDI_NAME);
            boolean add = store.addFileRef(sourceAET, ds, modified,
                     new FileRef(fs, filePath, pc.getTransferSyntax(), dst.length(),
                            digest(digest)), ae.getStoreParam());
            if (add && ae.hasIANDestinations()) {
                scheduleIAN(ae, store.createIANforPreviousMPPS());
                for (Attributes ian : store.createIANsforRejectionNote())
                    scheduleIAN(ae, ian);
             }
             if (!modified.isEmpty()) {
                 if (LOG.isInfoEnabled()) {
                     LOG.info("{}:Coercion of Data Elements:\n{}\nto:\n{}",
                             new Object[] { 
                                 as,
                                 modified,
                                 new Attributes(ds, ds.bigEndian(), modified.tags())
                             });
                 }
                 if (!ae.isSuppressWarningCoercionOfDataElements()) {
                     rsp.setInt(Tag.Status, VR.US, Status.CoercionOfDataElements);
                     rsp.setInt(Tag.OffendingElement, VR.AT, modified.tags());
                 }
             }
            return add;
         } catch (DicomServiceException e) {
             throw e;
         } catch (Exception e) {
             throw new DicomServiceException(Status.ProcessingFailure,
                     DicomServiceException.initialCauseOf(e));
         }
     }
 
     private String format(AttributesFormat format, Attributes ds) {
         synchronized (format) {
             return format.format(ds);
         }
     }
 
     private static File rename(Association as, Attributes rq, FileSystem fs, File file,
             String fpath) throws DicomServiceException {
         File dst = new File(fs.getDirectory(), fpath);
         File dir = dst.getParentFile();
         dir.mkdirs();
         while (dst.exists()) {
             dst = new File(dir, TagUtils.toHexString(LazyInitialization.nextInt()));
         }
         if (file.renameTo(dst))
             LOG.info(as + ": M-RENAME " + file + " to " + dst);
         else {
             LOG.warn(as + ": Failed to M-RENAME " + file + " to " + dst);
             throw new DicomServiceException(Status.OutOfResources, "Failed to rename file");
         }
         return dst;
     }
 
     private String digest(MessageDigest digest) {
         return digest != null ? TagUtils.toHexString(digest.digest()) : null;
     }
 
     private Attributes readDataset(Association as, Attributes rq, File file)
             throws DicomServiceException {
         DicomInputStream in = null;
         try {
             in = new DicomInputStream(file);
             in.setIncludeBulkData(false);
             return in.readDataset(-1, Tag.PixelData);
         } catch (IOException e) {
             LOG.warn(as + ": Failed to decode dataset:", e);
             throw new DicomServiceException(Status.CannotUnderstand);
         } finally {
             SafeClose.close(in);
         }
     }
 
     private InstanceStore initInstanceStore(Association as) {
         InstanceStore store =
                     (InstanceStore) as.getProperty(InstanceStore.JNDI_NAME);
         if (store == null) {
             store = (InstanceStore) JNDIUtils.lookup(InstanceStore.JNDI_NAME);
             as.setProperty(InstanceStore.JNDI_NAME, store);
         }
         return store;
     }
 
     private void closeInstanceStore(Association as) {
         InstanceStore store =
                 (InstanceStore) as.clearProperty(InstanceStore.JNDI_NAME);
         if (store != null) {
             ArchiveApplicationEntity ae = (ArchiveApplicationEntity) as.getApplicationEntity();
             if (ae.hasIANDestinations())
                 try {
                     scheduleIAN(ae, store.createIANforCurrentMPPS());
                 } catch (Exception e) {
                     LOG.warn(as + ": Failed to create IAN for MPPS:", e);
                 }
             store.close();
         }
     }
 
     private void scheduleIAN(ArchiveApplicationEntity ae, Attributes ian) {
         if (ian != null)
             for (String remoteAET : ae.getIANDestinations())
                 ianSCU.scheduleIAN(ae.getAETitle(), remoteAET, ian, 0, 0);
     }
 
     @Override
     public void onClose(Association as) {
         closeInstanceStore(as);
     }
 }
