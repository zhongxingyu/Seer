 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems          - initial API and implementation
  * Anna Dushistova(Montavista) - [386484]Allow file transfer from target to host into existing directories
  *******************************************************************************/
 
 package org.eclipse.tcf.te.tcf.filesystem.core.services;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.services.IFileSystem;
 import org.eclipse.tcf.services.IFileSystem.FileAttrs;
 import org.eclipse.tcf.services.IFileSystem.FileSystemException;
 import org.eclipse.tcf.services.IFileSystem.IFileHandle;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
 import org.eclipse.tcf.te.runtime.utils.ProgressHelper;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFChannelException;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation;
 import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
 import org.eclipse.tcf.util.TCFFileInputStream;
 import org.eclipse.tcf.util.TCFFileOutputStream;
 
 /**
  * TCF file transfer service.
  */
 public class FileTransferService {
 
     /**
      * Transfer a file between host and target depending on the {@link IFileTransferItem} data.
      *
      * @param peer The peer, must not be <code>null</code>.
      * @param channel The channel or <code>null</code>.
      * @param item The file transfer item, must not be <code>null</code>.
      * @param monitor The progress monitor or <code>null</code>.
      * @param callback The callback or <code>null</code>.
      */
     public static void transfer(IPeer peer, IChannel channel, IFileTransferItem item, IProgressMonitor monitor, ICallback callback) {
 
         boolean ownChannel = false;
         IFileSystem fileSystem;
         try {
             if (channel == null) {
                 ownChannel = true;
                 channel = Operation.openChannel(peer);
             }
             fileSystem = Operation.getBlockingFileSystem(channel);
 
             Assert.isNotNull(fileSystem);
 
             // Check the direction of the transfer
             if (item.getDirection() == IFileTransferItem.TARGET_TO_HOST) {
                 transferToHost(peer, fileSystem, item, monitor, callback);
             }
             else {
                 transferToTarget(peer, fileSystem, item, monitor, callback);
             }
             if (ownChannel) {
                 closeChannel(peer, channel);
             }
         }
         catch (Exception e) {
             if (callback != null) {
                 callback.done(peer, StatusHelper.getStatus(e));
             }
         }
     }
 
     /**
      * Transfer a file between host and target depending on the {@link IFileTransferItem} data.
      *
      * @param peer The peer, must not be <code>null</code>.
      * @param item The file transfer item, must not be <code>null</code>.
      * @param monitor The progress monitor or <code>null</code>.
      * @param callback The callback or <code>null</code>.
      */
     protected static void transfer(IPeer peer, IFileTransferItem item, IProgressMonitor monitor, ICallback callback) {
 
         // Check if we can skip the transfer
         if (!item.isEnabled()) {
             if (callback != null) {
                 callback.done(peer, Status.OK_STATUS);
             }
             return;
         }
 
         try {
             IChannel channel = Operation.openChannel(peer);
             transfer(peer, channel, item, monitor, callback);
             closeChannel(peer, channel);
         }
         catch (Exception e) {
             if (callback != null) {
                 callback.done(peer, StatusHelper.getStatus(e));
             }
         }
     }
 
     protected static void transferToHost(IPeer peer, IFileSystem fileSystem, IFileTransferItem item, IProgressMonitor monitor, ICallback callback) {
 
         IStatus result = Status.OK_STATUS;
 
         IPath hostPath = item.getHostPath();
         IPath targetPath = item.getTargetPath();
 
         BufferedOutputStream outStream = null;
         TCFFileInputStream inStream = null;
 
         final IFileSystem.IFileHandle[] handle = new IFileSystem.IFileHandle[1];
         final FileSystemException[] error = new FileSystemException[1];
         final IFileSystem.FileAttrs[] attrs = new IFileSystem.FileAttrs[1];
 
         // Create necessary parent directory structure on host side
         boolean rc = hostPath.removeLastSegments(1).toFile().exists();
         if(!rc){
         	rc = hostPath.removeLastSegments(1).toFile().mkdirs();
         	if (!rc) {
         		IOException e = new IOException(NLS.bind(Messages.FileTransferService_error_mkdirFailed, hostPath.removeLastSegments(1).toOSString()));
         		result = StatusHelper.getStatus(e);
         		if (callback != null) callback.done(peer, result);
         		return;
         	}
         }
         // If the host file is a directory, append the remote file name
         if (hostPath.toFile().isDirectory()) {
             hostPath = item.getHostPath().append(targetPath.lastSegment());
         }
 
         // Remember the modification time of the remote file.
         // We need this value to set the modification time of the host file
         // _after_ the stream closed.
         long mtime = -1;
 
         try {
             // Open the remote file
             fileSystem.open(targetPath.toString(), IFileSystem.TCF_O_READ, null, new IFileSystem.DoneOpen() {
                 @Override
                 public void doneOpen(IToken token, FileSystemException e, IFileHandle h) {
                     error[0] = e;
                     handle[0] = h;
                 }
             });
             if (error[0] != null) {
                 throw error[0];
             }
             // Get the remote file attributes
             fileSystem.fstat(handle[0], new IFileSystem.DoneStat() {
                 @Override
                 public void doneStat(IToken token, FileSystemException e, FileAttrs a) {
                     error[0] = e;
                     attrs[0] = a;
                 }
             });
             if (error[0] != null) {
                 throw error[0];
             }
             // Remember the modification time
             mtime = attrs[0].mtime;
 
             // Open a output stream to the host file
             outStream = new BufferedOutputStream(new FileOutputStream(hostPath.toFile()));
             // And open the input stream to the target file handle
             inStream = new TCFFileInputStream(handle[0]);
 
             ProgressHelper.setSubTaskName(monitor, "Transfer '" + targetPath.toString() + "' to '" + hostPath.toOSString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
             long bytesTotal = attrs[0].size;
             copy(inStream, outStream, bytesTotal, monitor);
         }
         catch (OperationCanceledException e) {
             result = Status.CANCEL_STATUS;
         }
         catch (Exception e) {
             result = StatusHelper.getStatus(e);
         }
         finally {
             // Close all streams and cleanup
             if (outStream != null) {
                 try {
                     outStream.close();
                     outStream = null;
                 }
                 catch (IOException e) {
                 }
             }
             if (inStream != null) {
                 try {
                     inStream.close();
                     inStream = null;
                 }
                 catch (IOException e) {
                 }
             }
 
             if (result.isOK()) {
                 if (mtime >= 0) {
                     rc = hostPath.toFile().setLastModified(mtime);
                     if (!rc && Platform.inDebugMode()) {
                     	System.err.println("Failed to set mtime for " + hostPath.toOSString()); //$NON-NLS-1$
                     }
                 }
             }
             else if (result.getSeverity() == IStatus.ERROR || result.getSeverity() == IStatus.CANCEL) {
                 try {
                     rc = hostPath.toFile().delete();
                     if (!rc && Platform.inDebugMode()) {
                     	System.err.println("Failed to delete host file " + hostPath.toOSString()); //$NON-NLS-1$
                     }
                 }
                 catch (Throwable e) {
                 }
             }
         }
         if (callback != null) callback.done(peer, result);
     }
 
     protected static void transferToTarget(IPeer peer, IFileSystem fileSystem, IFileTransferItem item, IProgressMonitor monitor, ICallback callback) {
 
         IStatus result = Status.OK_STATUS;
 
         IPath targetPath = item.getTargetPath();
         IPath hostPath = item.getHostPath();
 
         BufferedInputStream inStream = null;
         TCFFileOutputStream outStream = null;
 
         final IFileSystem.IFileHandle[] handle = new IFileSystem.IFileHandle[1];
         final FileSystemException[] error = new FileSystemException[1];
         final FileAttrs[] attrs = new FileAttrs[1];
 
         // Check the target destination directory
         fileSystem.stat(targetPath.toString(), new IFileSystem.DoneStat() {
             @Override
             public void doneStat(IToken token, FileSystemException e, FileAttrs a) {
                 error[0] = e;
                 attrs[0] = a;
             }
         });
         // If we get the attributes back, the name at least exist in the target file system
        if (error[0] != null || (attrs[0] != null && attrs[0].isDirectory())) {
             targetPath = targetPath.append(item.getHostPath().lastSegment());
         }
 
         try {
             // Open the remote file
             fileSystem.open(targetPath.toString(), IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_TRUNC, null, new IFileSystem.DoneOpen() {
                 @Override
                 public void doneOpen(IToken token, FileSystemException e, IFileHandle h) {
                     error[0] = e;
                     handle[0] = h;
                 }
             });
             if (error[0] != null) {
                 throw error[0];
             }
 
             // Open a input stream from the host file
             inStream = new BufferedInputStream(new FileInputStream(hostPath.toFile()));
             // Open the output stream for the target file handle
             outStream = new TCFFileOutputStream(handle[0]);
 
             ProgressHelper.setSubTaskName(monitor, "Transfer '" + hostPath.toOSString() + "' to '" + targetPath.toString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
             copy(inStream, outStream, hostPath.toFile().length(), monitor);
 
             // Get the remote file attributes
             fileSystem.fstat(handle[0], new IFileSystem.DoneStat() {
                 @Override
                 public void doneStat(IToken token, FileSystemException e, FileAttrs a) {
                     error[0] = e;
                     attrs[0] = a;
                 }
             });
             // Update the remote file attributes
             IFileSystem.FileAttrs newAttrs = new FileAttrs(attrs[0].flags, attrs[0].size, attrs[0].uid, attrs[0].gid, attrs[0].permissions,
                 attrs[0].atime, hostPath.toFile().lastModified(), attrs[0].attributes);
             // Set the remote file attributes
             fileSystem.fsetstat(handle[0], newAttrs, new IFileSystem.DoneSetStat() {
                 @Override
                 public void doneSetStat(IToken token, FileSystemException e) {
                     error[0] = e;
                 }
             });
         }
         catch (OperationCanceledException e) {
             result = Status.CANCEL_STATUS;
         }
         catch (Exception e) {
             result = StatusHelper.getStatus(e);
         }
         finally {
             // Close all streams and cleanup
             if (outStream != null) {
                 try {
                     outStream.close();
                     outStream = null;
                 }
                 catch (IOException e) {
                 }
             }
             if (inStream != null) {
                 try {
                     inStream.close();
                     inStream = null;
                 }
                 catch (IOException e) {
                 }
             }
 
             if (result.getSeverity() == IStatus.ERROR || result.getSeverity() == IStatus.CANCEL) {
                 fileSystem.remove(targetPath.toString(), new IFileSystem.DoneRemove() {
 					@Override
 					public void doneRemove(IToken token, FileSystemException error) {
 					}
 				});
             }
         }
         if (callback != null) callback.done(peer, result);
     }
 
     private static void copy(InputStream in, OutputStream out, long bytesTotal, IProgressMonitor monitor) throws IOException {
         long bytesDone = 0;
         long speed;
         long startTimeStamp = System.currentTimeMillis();
         byte[] dataBuffer = new byte[12 * 1024];
 
         // Copy from the input stream to the output stream (always binary).
         while (true) {
             if (ProgressHelper.isCanceled(monitor)) {
                 throw new OperationCanceledException();
             }
             // Read the data from the remote file
             int bytesRead = in.read(dataBuffer);
             // If reached EOF, we are done and break the loop
             if (bytesRead < 0) {
                 break;
             }
             if (ProgressHelper.isCanceled(monitor)) {
                 throw new OperationCanceledException();
             }
             // Write back to the host file
             out.write(dataBuffer, 0, bytesRead);
 
             bytesDone += bytesRead;
             long timestamp = System.currentTimeMillis();
             speed = ((bytesDone) * 1000) / Math.max(timestamp - startTimeStamp, 1);
 
             ProgressHelper.worked(monitor, (int)((bytesRead/bytesTotal) * 1000));
             ProgressHelper.setSubTaskName(monitor, getProgressMessage(bytesDone, bytesTotal, speed));
         }
     }
 
     /**
      * Close the channel for file transfer.
      * @param peer
      * @param channel
      * @throws TCFChannelException
      */
     protected static void closeChannel(final IPeer peer, final IChannel channel) throws TCFChannelException {
         if (channel != null) {
         	Tcf.getChannelManager().closeChannel(channel);
         }
     }
 
     private static String getProgressMessage(long bytesDone, long bytesTotal, long bytesSpeed) {
         String done = "B"; //$NON-NLS-1$
         String total = "B"; //$NON-NLS-1$
         String speed = "B/s"; //$NON-NLS-1$
 
         if (bytesDone > 1024) {
             bytesDone /= 1024;
             done = "KB"; //$NON-NLS-1$
         }
         if (bytesDone > 1024) {
             bytesDone /= 1024;
             done = "MB"; //$NON-NLS-1$
         }
         if (bytesSpeed > 1024) {
             bytesSpeed /= 1024;
             speed = "GB/s"; //$NON-NLS-1$
         }
 
         if (bytesTotal > 1024) {
             bytesTotal /= 1024;
             total = "KB"; //$NON-NLS-1$
         }
         if (bytesTotal > 1024) {
             bytesTotal /= 1024;
             total = "MB"; //$NON-NLS-1$
         }
         if (bytesTotal > 1024) {
             bytesTotal /= 1024;
             total = "GB"; //$NON-NLS-1$
         }
 
         if (bytesSpeed > 1024) {
             bytesSpeed /= 1024;
             speed = "KB/s"; //$NON-NLS-1$
         }
         if (bytesSpeed > 1024) {
             bytesSpeed /= 1024;
             speed = "MB/s"; //$NON-NLS-1$
         }
         if (bytesDone > 1024) {
             bytesDone /= 1024;
             done = "GB"; //$NON-NLS-1$
         }
 
         return bytesDone + done + " of " + bytesTotal + total + " at " + bytesSpeed + speed; //$NON-NLS-1$ //$NON-NLS-2$
     }
 
 }
