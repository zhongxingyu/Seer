 /*
  * The MIT License
  * 
  * Copyright 2011 Winston.Prakash@Oracle.com
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.hudsonci.plugins.jna;
 
 import com.sun.jna.Native;
 import hudson.Extension;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.eclipse.hudson.jna.*;
 import org.jvnet.solaris.libzfs.ACLBuilder;
 import org.jvnet.solaris.libzfs.LibZFS;
 import org.jvnet.solaris.libzfs.ZFSFileSystem;
 import org.jvnet.solaris.mount.MountFlags;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import static org.hudsonci.plugins.jna.GNUCLibrary.LIBC;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  *  JNA based ZFS Support Extension for Hudson
  */
 public class JnaZfsSupport extends NativeZfsSupport {
 
     private static Logger logger = LoggerFactory.getLogger(JnaZfsSupport.class);
    private transient LibZFS zfs;
 
     @DataBoundConstructor
     public JnaZfsSupport() {
         try {
             zfs = new LibZFS();
         } catch (Throwable thr) {
             
             // Never mind. Native ZFS library could not be loaded
             // We don't support ZFS after all
         }
     }
 
     @Override
     public boolean hasSupportFor(NativeFunction nativeFunc) {
         if (zfs != null) {
             switch (nativeFunc) {
                 case ZFS:
                     return true;
             }
         }
         return false;
     }
 
     @Override
     public List<NativeZfsFileSystem> getZfsRoots() throws NativeAccessException {
         List<NativeZfsFileSystem> zfsRoots = new ArrayList<NativeZfsFileSystem>();
 
         List<ZFSFileSystem> roots = zfs.roots();
 
         for (ZFSFileSystem fs : roots) {
             zfsRoots.add(new ZfsFileSystemImpl(fs));
         }
         return zfsRoots;
     }
 
     @Override
     public NativeZfsFileSystem getZfsByMountPoint(File mountPoint) throws NativeAccessException {
         ZFSFileSystem fs = zfs.getFileSystemByMountPoint(mountPoint);
         return new ZfsFileSystemImpl(fs);
     }
 
     @Override
     public NativeZfsFileSystem createZfs(String mountName) throws NativeAccessException {
         ZFSFileSystem fs = zfs.create(mountName, ZFSFileSystem.class);
         return new ZfsFileSystemImpl(fs);
     }
 
     @Override
     public NativeZfsFileSystem openZfs(String target) throws NativeAccessException {
         ZFSFileSystem fs = zfs.open(target, ZFSFileSystem.class);
         return new ZfsFileSystemImpl(fs);
     }
 
     @Override
     public boolean zfsExists(String zfsName) throws NativeAccessException {
         return zfs.exists(zfsName);
     }
 
     @Override
     public String getLastError() {
         return LIBC.strerror(Native.getLastError());
     }
 
     @Extension
     public static class DescriptorImpl extends NativeZfsSupportDescriptor {
 
         @Override
         public String getDisplayName() {
             return "Java ZFS Support";
         }
     }
 
     private static class ZfsFileSystemImpl implements NativeZfsFileSystem {
 
         ZFSFileSystem zfsFileSystem;
 
         ZfsFileSystemImpl(ZFSFileSystem zfsFileSystem) {
             this.zfsFileSystem = zfsFileSystem;
         }
 
         public String getName() {
             return zfsFileSystem.getName();
         }
 
         public void setMountPoint(File dir) {
             zfsFileSystem.setMountPoint(dir);
         }
 
         public void mount() {
             zfsFileSystem.mount();
         }
 
         public void unmount() {
             zfsFileSystem.unmount(MountFlags.MS_FORCE);
             zfsFileSystem.unmount();
         }
 
         public void unmount(int flag) {
             zfsFileSystem.unmount(flag);
         }
 
         public void setProperty(String key, String value) {
             zfsFileSystem.setProperty(key, value);
         }
 
         public void destory() {
             zfsFileSystem.destory();
         }
 
         public void allow(String userName) {
             ACLBuilder acl = new ACLBuilder();
             acl.user(userName).withEverything();
             zfsFileSystem.allow(acl);
         }
 
         public void share() {
             zfsFileSystem.share();
         }
 
         public void destory(boolean recursive) {
             zfsFileSystem.destory(recursive);
         }
     }
 }
