 /*  Copyright (c) 2008,2009 Konrad-Zuse-Zentrum fuer Informationstechnik Berlin.
 
  This file is part of XtreemFS. XtreemFS is part of XtreemOS, a Linux-based
  Grid Operating System, see <http://www.xtreemos.eu> for more details.
  The XtreemOS project has been developed with the financial support of the
  European Commission's IST program under contract #FP6-033576.
 
  XtreemFS is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 2 of the License, or (at your option)
  any later version.
 
  XtreemFS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with XtreemFS. If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * AUTHORS: Bjoern Kolbeck (ZIB), Jan Stender (ZIB)
  */
 
 package org.xtreemfs.mrc.operations;
 
 import java.net.InetSocketAddress;
 
 import org.xtreemfs.common.Capability;
 import org.xtreemfs.common.TimeSync;
 import org.xtreemfs.common.logging.Logging;
 import org.xtreemfs.interfaces.FileCredentials;
 import org.xtreemfs.interfaces.Replica;
 import org.xtreemfs.interfaces.ReplicaSet;
 import org.xtreemfs.interfaces.XLocSet;
 import org.xtreemfs.interfaces.MRCInterface.createResponse;
 import org.xtreemfs.interfaces.MRCInterface.openRequest;
 import org.xtreemfs.interfaces.MRCInterface.openResponse;
 import org.xtreemfs.mrc.ErrNo;
 import org.xtreemfs.mrc.ErrorRecord;
 import org.xtreemfs.mrc.MRCRequest;
 import org.xtreemfs.mrc.MRCRequestDispatcher;
 import org.xtreemfs.mrc.UserException;
 import org.xtreemfs.mrc.ErrorRecord.ErrorClass;
 import org.xtreemfs.mrc.ac.FileAccessManager;
 import org.xtreemfs.mrc.database.AtomicDBUpdate;
 import org.xtreemfs.mrc.database.StorageManager;
 import org.xtreemfs.mrc.metadata.FileMetadata;
 import org.xtreemfs.mrc.metadata.XLocList;
 import org.xtreemfs.mrc.utils.Converter;
 import org.xtreemfs.mrc.utils.MRCHelper;
 import org.xtreemfs.mrc.utils.Path;
 import org.xtreemfs.mrc.utils.PathResolver;
 import org.xtreemfs.mrc.volumes.VolumeManager;
 import org.xtreemfs.mrc.volumes.metadata.VolumeInfo;
 
 /**
  * 
  * @author stender
  */
 public class OpenOperation extends MRCOperation {
     
     public static final int OP_ID = 11;
     
     public OpenOperation(MRCRequestDispatcher master) {
         super(master);
     }
     
     @Override
     public void startRequest(MRCRequest rq) {
         
         try {
             
             final openRequest rqArgs = (openRequest) rq.getRequestArgs();
             
             final VolumeManager vMan = master.getVolumeManager();
             final FileAccessManager faMan = master.getFileAccessManager();
             
             Path p = new Path(rqArgs.getPath());
             
             VolumeInfo volume = vMan.getVolumeByName(p.getComp(0));
             StorageManager sMan = vMan.getStorageManager(volume.getId());
             PathResolver res = new PathResolver(sMan, p);
             
             // check whether the path prefix is searchable
             faMan.checkSearchPermission(sMan, res, rq.getDetails().userId, rq.getDetails().superUser, rq
                     .getDetails().groupIds);
             
             AtomicDBUpdate update = sMan.createAtomicDBUpdate(master, rq);
             FileMetadata file = null;
             
             // check whether the file/directory exists
             try {
                 
                 res.checkIfFileDoesNotExist();
                 
                 file = res.getFile();
                 
                 // if the file refers to a symbolic link, resolve the link
                 String target = sMan.getSoftlinkTarget(file.getId());
                 if (target != null) {
                     rqArgs.setPath(target);
                     p = new Path(target);
                     
                     // if the local MRC is not responsible, send a redirect
                     if (!vMan.hasVolume(p.getComp(0))) {
                         finishRequest(rq, new ErrorRecord(ErrorClass.USER_EXCEPTION, ErrNo.ENOENT,
                             "link target " + target + " does not exist"));
                         return;
                     }
                     
                     volume = vMan.getVolumeByName(p.getComp(0));
                     sMan = vMan.getStorageManager(volume.getId());
                     res = new PathResolver(sMan, p);
                     file = res.getFile();
                 }
                 
                 if (file.isDirectory())
                     throw new UserException(ErrNo.EISDIR, "open is restricted to files");
                 
                 // check whether the file is marked as 'read-only'; in this
                 // case, throw an exception if write access is requested
                 if (file.isReadOnly()
                     && ((rqArgs.getFlags() & (FileAccessManager.O_RDWR | FileAccessManager.O_WRONLY
                         | FileAccessManager.O_TRUNC | FileAccessManager.O_APPEND)) != 0))
                     throw new UserException(ErrNo.EPERM, "read-only files cannot be written");
                 
                 // check whether the permission is granted
                 faMan.checkPermission(rqArgs.getFlags(), sMan, file, res.getParentDirId(),
                     rq.getDetails().userId, rq.getDetails().superUser, rq.getDetails().groupIds);
                 
             } catch (UserException exc) {
                 
                 // if the file does not exist, check whether the O_CREAT flag
                 // has been provided
                 if (exc.getErrno() == ErrNo.ENOENT && (rqArgs.getFlags() & FileAccessManager.O_CREAT) != 0) {
 
                     //check for write permission in parent dir
                     // check whether the parent directory grants write access
                     faMan.checkPermission(FileAccessManager.O_WRONLY, sMan, res.getParentDir(), res
                             .getParentsParentId(), rq.getDetails().userId, rq.getDetails().superUser,
                         rq.getDetails().groupIds);
 
                     // prepare file creation in database
                     if ((rqArgs.getFlags() & FileAccessManager.O_EXCL) != 0)
                         res.checkIfFileExistsAlready();
                     
                     // get the next free file ID
                     long fileId = sMan.getNextFileId();
                     
                     // atime, ctime, mtime
                     int time = (int) (TimeSync.getGlobalTime() / 1000);
                     
                     // create the metadata object
                     file = sMan.createFile(fileId, res.getParentDirId(), res.getFileName(), time, time, time,
                         rq.getDetails().userId, rq.getDetails().groupIds.get(0), rqArgs.getMode(), 0, 0,
                         false, 0, 0, update);
                     
                     // set the file ID as the last one
                     sMan.setLastFileId(fileId, update);
                    
                    // set the response
                    rq.setResponse(new createResponse());
                 }
 
                 else
                     throw exc;
             }
             
             // get the current epoch, use (and increase) the truncate number if
             // the open mode is truncate
             int trEpoch = file.getEpoch();
             if ((rqArgs.getFlags() & FileAccessManager.O_TRUNC) != 0) {
                update = sMan.createAtomicDBUpdate(master, rq);
                 file.setIssuedEpoch(file.getIssuedEpoch() + 1);
                 trEpoch = file.getIssuedEpoch();
                 sMan.setMetadata(file, FileMetadata.RC_METADATA, update);
             }
             
             XLocList xLocList = file.getXLocList();
             XLocSet xLocSet = null;
             
             // if no replicas have been assigned yet, assign a new replica
             if (xLocList == null || xLocList.getReplicaCount() == 0) {
                 
                 // create a replica with the default striping policy together
                 // with a set of feasible OSDs from the OSD status manager
                 Replica replica = MRCHelper.createReplica(null, sMan, master.getOSDStatusManager(), volume,
                     res.getParentDirId(), rqArgs.getPath(), ((InetSocketAddress) rq.getRPCRequest()
                             .getClientIdentity()).getAddress());
                 
                 ReplicaSet replicas = new ReplicaSet();
                 replicas.add(replica);
                 
                 // TODO: include proper replica update policy in MRC
                 // database design
                 xLocSet = new XLocSet(replicas, 0, "", 0);
                 
                 file.setXLocList(Converter.xLocSetToXLocList(sMan, xLocSet));
                 sMan.setMetadata(file, FileMetadata.XLOC_METADATA, update);
             }
 
             else
                 xLocSet = Converter.xLocListToXLocSet(xLocList);
             
             Capability cap = new Capability(volume.getId() + ":" + file.getId(), rqArgs.getFlags(), TimeSync
                     .getGlobalTime()
                 / 1000 + Capability.DEFAULT_VALIDITY, ((InetSocketAddress) rq.getRPCRequest()
                     .getClientIdentity()).getAddress().getHostAddress(), trEpoch, master.getConfig()
                     .getCapabilitySecret());
             
             // update POSIX timestamps of file
             MRCHelper.updateFileTimes(res.getParentsParentId(), file, !master.getConfig().isNoAtime(), true,
                 true, sMan, update);
             
             // update POSIX timestamps of parent directory
             MRCHelper.updateFileTimes(res.getParentsParentId(), res.getParentDir(), false, true, true, sMan,
                 update);
             
             // update POSIX timestamps of parent directory
             if (!master.getConfig().isNoAtime())
                 MRCHelper.updateFileTimes(res.getParentsParentId(), res.getParentDir(), true, false, false,
                     sMan, update);
             
             // set the response
             rq.setResponse(new openResponse(new FileCredentials(xLocSet, cap.getXCap())));
             
             update.execute();
             
         } catch (UserException exc) {
             Logging.logMessage(Logging.LEVEL_TRACE, this, exc);
             finishRequest(rq, new ErrorRecord(ErrorClass.USER_EXCEPTION, exc.getErrno(), exc.getMessage(),
                 exc));
         } catch (Exception exc) {
             finishRequest(rq, new ErrorRecord(ErrorClass.INTERNAL_SERVER_ERROR, "an error has occurred", exc));
         }
     }
     
 }
