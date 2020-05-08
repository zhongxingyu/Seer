 /*-
  * TestFS - A testing file system for jFUSE.
  * Copyright (C) 2008-2009  Erik Larsson <erik82@kth.se>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.catacombae.jfuse.test;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.TreeMap;
 import org.catacombae.jfuse.FUSE;
 import org.catacombae.jfuse.types.fuse26.FUSEConnInfo;
 import org.catacombae.jfuse.types.fuse26.FUSEFileInfo;
 import org.catacombae.jfuse.MacFUSEFileSystemAdapter;
 import org.catacombae.jfuse.types.fuse26.FUSEFillDir;
 import org.catacombae.jfuse.types.macfuse20.Setattr_x;
 import org.catacombae.jfuse.util.FUSEUtil;
 import org.catacombae.jfuse.types.system.Stat;
 import org.catacombae.jfuse.types.system.StatVFS;
 import org.catacombae.jfuse.types.system.Timespec;
 import org.catacombae.jfuse.types.system.XattrUtil;
 import org.catacombae.jfuse.util.Log;
 
 /**
  * In-memory file system for testing jFUSE.<br>
  * This file system will be used for testing jFUSE. All or most of the
  * FUSEOperations will be implemented in this file system eventually.
  * (Current status is work-in-progress.)
  *
  * @author Erik Larsson
  */
 public class TestFS extends MacFUSEFileSystemAdapter {
 
     private static final String CLASS_NAME = "TestFS";
 
     /** No-op Iterable used in some internal operations. */
     private static final Iterable<String> nullStringIterable =
             new Iterable<String>() {
 
                 public Iterator<String> iterator() {
                     return new Iterator<String>() {
 
                         public boolean hasNext() {
                             return false;
                         }
 
                         public String next() {
                             throw new NoSuchElementException();
                         }
 
                         public void remove() {
                             throw new UnsupportedOperationException();
                         }
                     };
                 }
             };
 
     private final Object inodeIdSync = new Object();
     private int lastInodeId = 0;
 
     private final int blockSize = 65535;
     private final byte[] zeroBlock;
 
     private void setCreateTimes(Inode node, long createTime) {
         node.accessTime.setToMillis(createTime);
         node.modificationTime.setToMillis(createTime);
         node.statusChangeTime.setToMillis(createTime);
         node.createTime.setToMillis(createTime);
         node.backupTime.setToMillis(0);
     }
 
     private abstract class Inode {
         public final int id;
 
         public long uid;
         public long gid;
         public short mode;
         public int nlink;
 
         public final Timespec accessTime = new Timespec();
         public final Timespec modificationTime = new Timespec();
         public final Timespec statusChangeTime = new Timespec();
         public final Timespec createTime = new Timespec();
         public final Timespec backupTime = new Timespec();
 
         private int flags = 0;
 
         public TreeMap<String, DataStream> xattrMap = null;
 
         {
             synchronized(inodeIdSync) {
                 id = lastInodeId++;
             }
         }
 
         public DataStream getXattr(String name) {
             if(xattrMap == null)
                 return null;
             else
                 return xattrMap.get(name);
         }
 
         public DataStream createXattr(String name)
                 throws IllegalArgumentException {
             if(name == null)
                 throw new IllegalArgumentException("No null names allowed.");
 
             if(xattrMap == null)
                 xattrMap = new TreeMap<String, DataStream>();
 
             DataStream stream = xattrMap.get(name);
             if(stream == null) {
                 stream = new DataStream();
                 xattrMap.put(name, stream);
                 return stream;
             }
             else
                 return null;
         }
 
         public boolean removeXattr(String name) {
             if(xattrMap == null)
                 return false;
             else
                 return xattrMap.remove(name) != null;
         }
 
         public Iterable<String> listXattrs() {
             if(xattrMap == null)
                 return nullStringIterable;
             else
                 return xattrMap.keySet();
         }
 
         public boolean existsXattr(String nameString) {
             if(xattrMap == null)
                 return false;
             else
                 return xattrMap.get(nameString) != null;
         }
 
         /** Sets file mode, preserving the file type. */
         public void setRWXSModeBits(short mode) {
             this.mode = (short) ((this.mode & S_IFMT) | // Preserve file type.
                     (mode & S_IRWXU) | // Set user bits
                     (mode & S_IRWXG) | // Set group bits
                     (mode & S_IRWXO) | // Set other bits
                     (mode & S_ISUID) | // Set setuid bit
                     (mode & S_ISGID) | // Set setgid bit
                     (mode & S_ISVTX)); // Set sticky bit
         }
         public void setRWXModeBits(short mode) {
             this.mode = (short) ((this.mode & S_IFMT) | // Preserve file type.
                     (mode & S_IRWXU) | // Set user bits
                     (mode & S_IRWXG) | // Set group bits
                     (mode & S_IRWXO)); // Set other bits
         }
     }
 
     private class Directory extends Inode {
         public final TreeMap<String, Inode> children = new TreeMap<String, Inode>();
     }
 
     private class File extends Inode {
         public final DataStream data = new DataStream();
     }
 
     private class Symlink extends Inode {
         public String target;
     }
 
     private class DataStream {
         private final ArrayList<byte[]> blocks = new ArrayList<byte[]>();
         private long length = 0;
 
         public int read(ByteBuffer target, long position) {
             long bytesLeftInFile = this.length - position;
             if(bytesLeftInFile > 0) {
                 int len = (int) Math.min(bytesLeftInFile, target.remaining());
 
                 int totalBytesRead = 0;
                 while(totalBytesRead < len) {
                     long curOffset = position + totalBytesRead;
                     int currentBlock = (int) (curOffset / blockSize);
                     int offsetInBlock =
                             (int) (curOffset - (currentBlock * blockSize));
                     int bytesToRead = Math.min(len - totalBytesRead,
                             blockSize - offsetInBlock);
 
                     byte[] block = this.blocks.get(currentBlock);
                     if(block != null)
                         target.put(block, offsetInBlock, bytesToRead);
                     else
                         target.put(zeroBlock, offsetInBlock, bytesToRead);
 
                     totalBytesRead += bytesToRead;
                     ++currentBlock;
                 }
 
                 if(totalBytesRead != len)
                     throw new RuntimeException("totalBytesRead != len (" +
                             totalBytesRead + " != " + len);
 
                 return len;
             }
             else
                 return 0;
 
         }
 
         public void write(ByteBuffer source, long position) {
             final int len = source.remaining();
 
             Log.debug("len = " + len);
 
             int totalBytesWritten = 0;
             while(totalBytesWritten < len) {
 
                 long curOffset = position + totalBytesWritten;
                 int currentBlockIndex = (int) (curOffset / blockSize);
                 int offsetInBlock =
                         (int) (curOffset - (currentBlockIndex * blockSize));
                 int bytesToWrite = Math.min(len - totalBytesWritten,
                         blockSize - offsetInBlock);
                 Log.debug("write: copying " + bytesToWrite + " bytes " +
                         "to block " + currentBlockIndex + " starting at " +
                         offsetInBlock);
 
                 while(this.blocks.size() <= currentBlockIndex) {
                     this.blocks.add(null);
                     Log.debug("added empty block. this.blocks.size()=" +
                             this.blocks.size());
                 }
 
                 byte[] currentBlock = this.blocks.get(currentBlockIndex);
 
                 if(currentBlock == null) {
                     currentBlock = new byte[blockSize];
                     this.blocks.set(currentBlockIndex, currentBlock);
                 }
 
                 source.get(currentBlock, offsetInBlock,
                         bytesToWrite);
 
                 if(this.length < curOffset + bytesToWrite)
                     this.length = curOffset + bytesToWrite;
 
                 totalBytesWritten += bytesToWrite;
             }
 
             if(totalBytesWritten != len)
                 throw new RuntimeException("totalBytesWritten != len (" +
                         totalBytesWritten + " != " + len);
         }
 
         public void truncate(long newLength) {
             if(newLength == 0) {
                 this.blocks.clear();
                 this.blocks.add(new byte[blockSize]);
                 this.length = 0;
             }
             else {
                 this.length = newLength;
 
                 int numBlocks = (int) (this.length / blockSize +
                         (this.length % blockSize != 0 ? 1 : 0));
 
                 while(this.blocks.size() > numBlocks)
                     this.blocks.remove(this.blocks.size() - 1);
 
                 while(this.blocks.size() < numBlocks)
                     this.blocks.add(null);
 
                 if(numBlocks > 0) {
                     byte[] lastBlock = this.blocks.get(numBlocks - 1);
                     if(lastBlock != null) {
                         // Zero out the truncated part of the last block.
                         int activeBytesInBlock = (int)(this.length % blockSize);
                         System.arraycopy(zeroBlock, activeBytesInBlock,
                                 lastBlock, activeBytesInBlock,
                                 blockSize - activeBytesInBlock);
                     }
                 }
             }
         }
 
         public long getLength() {
             return length;
         }
     }
 
     private Hashtable<String, Inode> fileTable = new Hashtable<String, Inode>();
 
     public TestFS() {
         zeroBlock = new byte[blockSize];
         for(int i = 0; i < zeroBlock.length; ++i)
             zeroBlock[i] = 0;
     }
 
     private Inode resolveSymlink(Symlink l) {
         HashSet<String> visited = new HashSet<String>();
 
         while(!visited.contains(l.target)) {
             Inode curNode = lookupInode(l.target);
             if(curNode == null)
                 break;
             else if(curNode instanceof Symlink)
                 l = (Symlink) curNode;
             else
                 return curNode;
         }
 
         return null;
     }
 
     private int createFile(String path, FUSEFileInfo fi, Short mode) {
         String parentPath = FUSEUtil.dirname(path);
         String childName = FUSEUtil.basename(path);
         if(childName.length() == 0)
             return -ENOENT; // Invalid filename (empty)
 
         Inode parent = lookupInode(parentPath);
         if(parent instanceof Symlink)
             parent = resolveSymlink((Symlink) parent);
 
         if(parent == null)
             return -ENOENT; // A component of the path name that must exist does not exist.
         else if(!(parent instanceof Directory)) {
             return -ENOTDIR; // A component of the path prefix is not a directory.
         }
         else {
             long createTime = System.currentTimeMillis();
 
             Directory parentDir = (Directory) parent;
             File f = new File();
             f.uid = parent.uid;
             f.gid = parent.gid;
             f.mode = (short)S_IFREG;
             if(mode != null)
                 f.setRWXSModeBits(mode);
             else
                 // Inherit mode, but not the suid/sticky bits. TODO: What does POSIX say?
                 f.setRWXModeBits(parent.mode);
             f.data.truncate(0);
             f.nlink = 1;
             setCreateTimes(f, createTime);
 
             parentDir.children.put(childName, f);
             fileTable.put(path, f);
             return 0;
         }
     }
 
     private int createDirectory(String pathString, Short mode) {
         String parentPath = FUSEUtil.dirname(pathString);
         String childName = FUSEUtil.basename(pathString);
         if(childName.length() == 0)
             return -ENOENT; // Invalid filename (empty)
 
         Inode parent = lookupInode(parentPath);
         if(parent instanceof Symlink)
             parent = resolveSymlink((Symlink) parent);
 
         if(parent == null)
             return -ENOENT; // A component of the path name that must exist does not exist.
         else if(!(parent instanceof Directory)) {
             return -ENOTDIR; // A component of the path prefix is not a directory.
         }
         else {
             long createTime = System.currentTimeMillis();
 
             Directory parentDir = (Directory) parent;
 
             Directory node = new Directory();
             node.uid = parent.uid;
             node.gid = parent.gid;
             node.mode = (short)S_IFDIR;
             if(mode != null) {
                 Log.debug("mode supplied: 0x" + Integer.toHexString(mode));
                 node.setRWXSModeBits(mode);
             }
             else {
                 Log.debug("no mode supplied... setting mode to standard:");
                 node.setRWXModeBits(parent.mode);
             }
             Log.debug("mode set: 0x" + Integer.toHexString(node.mode));
             setCreateTimes(node, createTime);
             node.nlink = 1;
 
             parentDir.children.put(childName, node);
             fileTable.put(pathString, node);
             return 0;
         }
     }
 
     private int createHardlink(String source, String destination) {
         String parentPath = FUSEUtil.dirname(destination);
         String childName = FUSEUtil.basename(destination);
         if(childName.length() == 0)
             return -ENOENT; // Invalid filename (empty)
 
 
         Inode parent = lookupInode(parentPath);
         if(parent instanceof Symlink)
             parent = resolveSymlink((Symlink) parent);
 
         if(parent == null) {
             // A component of the path name that must exist does not exist.
             return -ENOENT;
         }
         else if(!(parent instanceof Directory)) {
             // A component of the path prefix is not a directory.
             return -ENOTDIR;
         }
         else {
             Directory parentDir = (Directory) parent;
 
             Inode sourceNode = lookupInode(source);
             if(sourceNode == null)
                 return -ENOENT;
             else if(sourceNode instanceof Directory)
                 return -EPERM;
             else {
                 parentDir.children.put(childName, sourceNode);
                 fileTable.put(destination, sourceNode);
                 ++sourceNode.nlink;
                 sourceNode.statusChangeTime.setToMillis(System.currentTimeMillis());
                 
                 return 0;
             }
         }
     }
 
     private int createSymlink(String source, String destination) {
         String parentPath = FUSEUtil.dirname(destination);
         String childName = FUSEUtil.basename(destination);
         if(childName.length() == 0)
             return -ENOENT; // Invalid filename (empty)
 
         Inode parent = lookupInode(parentPath);
         if(parent instanceof Symlink)
             parent = resolveSymlink((Symlink) parent);
 
         if(parent == null) {
             // A component of the path name that must exist does not exist.
             return -ENOENT;
         }
         else if(!(parent instanceof Directory)) {
             // A component of the path prefix is not a directory.
             return -ENOTDIR;
         }
         else {
             long createTime = System.currentTimeMillis();
 
             Directory parentDir = (Directory) parent;
 
             Symlink l = new Symlink();
             l.uid = parent.uid;
             l.gid = parent.gid;
             l.mode = (short)S_IFLNK;
             l.setRWXModeBits(parent.mode); // Inherit.
             setCreateTimes(l, createTime);
             l.nlink = 1;
             l.target = source;
 
             parentDir.children.put(childName, l);
             fileTable.put(destination, l);
             return 0;
         }
     }
 
     private int removeNode(String path, Class<?> clazz) {
         String parentPath = FUSEUtil.dirname(path);
         String childName = FUSEUtil.basename(path);
         if(childName.length() == 0)
             return -ENOENT; // Invalid filename (empty)
 
         Inode parent = lookupInode(parentPath);
         //if(parent instanceof Symlink)
         //    parent = resolveSymlink((Symlink) parent);
 
         if(parent == null)
             return -ENOENT;
         else if(!(parent instanceof Directory)) {
             // A component of the path prefix is not a directory.
             return -ENOTDIR;
         }
         else {
             Directory parentDir = (Directory) parent;
 
             Inode node = parentDir.children.get(childName);
             if(node != null && (clazz == null || clazz.isInstance(node))) {
                 Inode dirNode = parentDir.children.remove(childName);
                 if(dirNode != node) {
                     Log.error("Removed node was not equal to retrieved node. " +
                             "dirNode=" + dirNode + " node=" + node);
                     return -EIO;
                 }
 
                 Inode tableNode = fileTable.remove(path);
                 if(tableNode != node) {
                     Log.error("Node in fileTable was different from node in " +
                             "directory. tableNode=" + tableNode + "node=" +
                             node);
                     return -EIO;
                 }
 
                 --node.nlink;
                 node.statusChangeTime.setToMillis(System.currentTimeMillis());
                 
                 return 0;
             }
             else
                 return -ENOENT;
         }
     }
 
     private int renameNode(String source, String dest) {
         String parentPath = FUSEUtil.dirname(source);
         String childName = FUSEUtil.basename(source);
         String newParentPath = FUSEUtil.dirname(dest);
         String newChildName = FUSEUtil.basename(dest);
 
         if(childName.length() == 0 || newChildName.length() == 0)
             return -ENOENT; // Invalid filename (empty)
 
         Inode parent = lookupInode(parentPath);
         Inode newParent = lookupInode(newParentPath);
         //if(parent instanceof Symlink)
         //    parent = resolveSymlink((Symlink) parent);
 
         if(parent == null || newParent == null)
             return -ENOENT;
         else if(!(parent instanceof Directory) ||
                 !(newParent instanceof Directory)) {
             // A component of the path prefix is not a directory.
             return -ENOTDIR;
         }
         else {
             Directory parentDir = (Directory) parent;
             Directory newParentDir = (Directory) newParent;
 
             Inode node = parentDir.children.get(childName);
             if(node != null) {
                 Inode dirNode = parentDir.children.remove(childName);
                 if(dirNode != node) {
                     Log.error("Removed node was not equal to retrieved node. " +
                             "dirNode=" + dirNode + " node=" + node);
                     return -EIO;
                 }
 
                 newParentDir.children.put(newChildName, node);
 
                 Inode tableNode = fileTable.get(source);
                 if(tableNode != node) {
                     Log.error("Node in fileTable was different from node in " +
                             "directory. tableNode=" + tableNode + "node=" +
                             node);
                     return -EIO;
                 }
 
                 fileTable.put(dest, node);
                 fileTable.remove(source);
 
                 node.statusChangeTime.setToMillis(System.currentTimeMillis());
 
                 return 0;
             }
             else
                 return -ENOENT;
         }
     }
 
     private void truncateFile(File f, long size) {
         f.data.truncate(size);
 
     }
 
     private Inode lookupInode(String path) {
         return fileTable.get(path);
     }
 
     @Override
     public Object init(FUSEConnInfo conn) {
         final String METHOD_NAME = "init";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, conn);
 
         Log.trace("conn.proto_major = " + conn.proto_major);
         Log.trace("conn.proto_minor = " + conn.proto_minor);
         Log.trace("conn.async_read = " + conn.async_read);
         Log.trace("conn.max_readahead = " + conn.max_readahead);
         Log.trace("conn.max_write = " + conn.max_write);
 
         Log.debug("process pid=" + FUSEUtil.getProcessPid());
 
         long mountTime = System.currentTimeMillis();
         Directory rootNode = new Directory();
         rootNode.uid = FUSEUtil.getProcessUid();
         Log.debug("root directory uid set to: " + rootNode.uid +
                 " (0x" + Long.toHexString(rootNode.uid) + ")");
         rootNode.gid = FUSEUtil.getProcessGid();
         Log.debug("root directory gid set to: " + rootNode.gid +
                 " (0x" + Long.toHexString(rootNode.gid) + ")");
         rootNode.nlink = 2; // Always 2 for root dir?
         rootNode.mode = (short)(S_IFDIR | 0777);
         setCreateTimes(rootNode, mountTime);
         fileTable.put("/", rootNode);
 
         Log.debug("fileTable result for '/': " + lookupInode("/"));
 
         Object retval = "Laban1235";
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, retval, conn);
         return retval;
     }
 
     @Override
     public void destroy(Object o) {
         final String METHOD_NAME = "destroy";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, o);
 
         Log.trace("Java method destroy got object: " + o);
 
         Log.traceLeaveVoid(CLASS_NAME + "." + METHOD_NAME, o);
     }
 
     @Override
     public int getattr(ByteBuffer path, Stat stbuf) {
         final String METHOD_NAME = "getattr";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, stbuf);
 
         int res = 0;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode e = lookupInode(pathString);
             if(e != null) {
                 //Log.debug("stbuf before:");
                 //stbuf.printFields("  ", System.err);
 
                 stbuf.st_uid = e.uid;
                 stbuf.st_gid = e.gid;
                 stbuf.st_mode = e.mode;
                 stbuf.st_nlink = e.nlink;
                 stbuf.st_atimespec.setToTimespec(e.accessTime);
                 stbuf.st_mtimespec.setToTimespec(e.modificationTime);
                 stbuf.st_ctimespec.setToTimespec(e.statusChangeTime);
                 stbuf.st_flags = e.flags;
                 
                 if(e instanceof File)
                     stbuf.st_size = ((File) e).data.getLength();
                 else if(e instanceof Symlink)
                      // TODO: Don't waste memory and CPU cycles here.
                     stbuf.st_size = FUSEUtil.encodeUTF8(((Symlink)e).target).length;
                 else
                     stbuf.st_size = 0;
 
                 //Log.debug("stbuf after:");
                 //stbuf.printFields("  ", System.err);
             }
             else
                 res = -ENOENT;
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, stbuf);
         return res;
     }
 
     @Override
     public int chmod(ByteBuffer path, short newMode) {
         final String METHOD_NAME = "chmod";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, newMode);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode e = lookupInode(pathString);
             if(e != null) {
                 Log.debug("newMode: 0x" + Integer.toHexString(newMode));
                 Log.debug("permissions before chmod: 0x" +
                         Integer.toHexString(e.mode));
                 e.setRWXSModeBits(newMode);
                 Log.debug("permissions after chmod: 0x" +
                         Integer.toHexString(e.mode));
 
                 e.statusChangeTime.setToMillis(System.currentTimeMillis());
 
                 res = 0;
             }
             else
                 res = -ENOENT;
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, newMode);
         return res;
     }
 
     @Override
     public int chown(ByteBuffer path, long userId, long groupId) {
         final String METHOD_NAME = "chown";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, userId, groupId);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode e = lookupInode(pathString);
             if(e != null) {
                 if(userId != -1)
                     e.uid = userId;
                 if(groupId != -1)
                     e.gid = groupId;
 
                 e.statusChangeTime.setToMillis(System.currentTimeMillis());
 
                 res = 0;
             }
             else
                 res = -ENOENT;
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, userId,
                 groupId);
         return res;
     }
 
     @Override
     public int readdir(ByteBuffer path, FUSEFillDir filler, long offset,
             FUSEFileInfo fi) {
         final String METHOD_NAME = "readdir";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, filler, offset,
                 fi);
 
         int res = 0;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode e = lookupInode(pathString);
             if(e != null && e instanceof Directory) {
                 Directory dir = (Directory) e;
 
                 filler.fill(FUSEUtil.encodeUTF8("."), null, 0);
                 filler.fill(FUSEUtil.encodeUTF8(".."), null, 0);
                 for(String childName : dir.children.keySet())
                     filler.fill(FUSEUtil.encodeUTF8(childName), null, 0);
             }
             else
                 res = -ENOENT;
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, filler,
                 offset, fi);
         return res;
     }
 
     @Override
     public int readlink(ByteBuffer path, ByteBuffer buffer) {
         final String METHOD_NAME = "readlink";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, buffer);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode e = lookupInode(pathString);
             if(e != null) {
                 if(e instanceof Symlink) {
                     if(buffer.capacity() > 0) {
                         Symlink link = (Symlink) e;
                         byte[] encodedTarget = FUSEUtil.encodeUTF8(link.target);
                         int copySize = Math.min(buffer.capacity() - 1,
                                 encodedTarget.length);
                         buffer.put(encodedTarget, 0, copySize);
                         buffer.put((byte)0); // Null terminator
                     }
 
                     res = 0; // ?
                 }
                 else
                     res = -EINVAL;
             }
             else
                 res = -ENOENT;
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, buffer);
         return res;
     }
 
     @Override
     public int symlink(ByteBuffer source, ByteBuffer dest) {
         final String METHOD_NAME = "symlink";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, source, dest);
 
         int res;
         String sourceString = FUSEUtil.decodeUTF8(source);
         String destString = FUSEUtil.decodeUTF8(dest);
         Log.trace("  sourceString = \"" + sourceString + "\"");
         Log.trace("  destString = \"" + destString + "\"");
         if(sourceString == null || destString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else
             res = createSymlink(sourceString, destString);
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, source, dest);
         return res;
     }
 
     @Override
     public int link(ByteBuffer source,
             ByteBuffer dest) {
         final String METHOD_NAME = "link";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, source, dest);
 
         int res;
         String sourceString = FUSEUtil.decodeUTF8(source);
         String destString = FUSEUtil.decodeUTF8(dest);
         Log.trace("  sourceString = \"" + sourceString + "\"");
         Log.trace("  destString = \"" + destString + "\"");
         if(sourceString == null || destString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else
             res = createHardlink(sourceString, destString);
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, source, dest);
         return res;
     }
 
     @Override
     public int open(ByteBuffer path, FUSEFileInfo fi) {
         final String METHOD_NAME = "open";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, fi);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
 
             Inode e = lookupInode(pathString);
             if(e != null) {
                 if(e instanceof Directory)
                     res = -EISDIR;
                 else {
                     File f = (File) e;
 
                     if(fi.getFlagCreate() && fi.getFlagExcl())
                         res = -EEXIST;
                     else if(fi.getFlagNofollow() && e instanceof Symlink)
                         res = -ELOOP;
                     else if(fi.getFlagSymlink() && e instanceof Symlink)
                         res = -ENOENT; // Open a symlink? ahem... later?
                     else if(fi.getFlagSharedLock() || fi.getFlagExclusiveLock())
                         res = -EOPNOTSUPP;
                     else {
                         if(fi.getFlagTruncate())
                             f.data.truncate(0);
 
                         res = 0;
                     }
                 }
                 /* Do I really need to worry so much about this? Won't FUSE
                  * take care of it for me? */
             }
             else {
                 if(!fi.getFlagCreate())
                     res = -ENOENT;
                 else
                     res = createFile(pathString, fi, null);
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, fi);
         return res;
     }
     
     @Override
     public int create(ByteBuffer path, short mode, FUSEFileInfo fi) {
         final String METHOD_NAME = "create";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, mode, fi);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             res = createFile(pathString, fi, mode);
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, mode, fi);
         return res;
     }
 
     @Override
     public int unlink(ByteBuffer path) {
         final String METHOD_NAME = "unlink";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             res = removeNode(pathString, null);
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path);
         return res;
     }
 
     @Override
     public int truncate(ByteBuffer path,
 			 long newSize) {
         final String METHOD_NAME = "truncate";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, newSize);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else if(e instanceof Directory)
                 res = -EISDIR;
             else if(!(e instanceof File))
                 res = -EACCES; // ?
             else {
                 File f = (File) e;
 
                 truncateFile(f, newSize);
 
                 f.modificationTime.setToMillis(System.currentTimeMillis());
 
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, newSize);
         return res;
     }
 
     @Override
     public int utimens(ByteBuffer path,
             Timespec accessTime,
             Timespec modificationTime) {
         final String METHOD_NAME = "utimens";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, accessTime,
                 modificationTime);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             Inode node = lookupInode(pathString);
             if(node == null)
                 res = -ENOENT;
             else {
                 node.accessTime.sec = accessTime.sec;
                 node.accessTime.nsec = accessTime.nsec;
                 
                 node.modificationTime.sec = modificationTime.sec;
                 node.modificationTime.nsec = modificationTime.nsec;
 
                 node.statusChangeTime.setToMillis(System.currentTimeMillis());
                 
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, accessTime,
                 modificationTime);
         return res;
     }
 
     @Override
     public int mkdir(ByteBuffer path, short mode) {
         final String METHOD_NAME = "mkdir";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, mode);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             res = createDirectory(pathString, mode);
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, mode);
         return res;
     }
 
     @Override
     public int rmdir(ByteBuffer path) {
         final String METHOD_NAME = "rmdir";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path);
 
         int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             res = removeNode(pathString, Directory.class);
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path);
         return res;
     }
 
     @Override
     public int rename(ByteBuffer oldPath,
 		       ByteBuffer newPath) {
         final String METHOD_NAME = "rename";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, oldPath, newPath);
 
         int res;
         String oldPathString = FUSEUtil.decodeUTF8(oldPath);
         String newPathString = FUSEUtil.decodeUTF8(newPath);
         Log.trace("  oldPathString = \"" + oldPathString + "\"");
         Log.trace("  newPathString = \"" + newPathString + "\"");
         if(oldPathString == null || newPathString == null) // Invalid UTF-8 sequence.
             res = -ENOENT;
         else {
             res = renameNode(oldPathString, newPathString);
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, oldPath, newPath);
         return res;
     }
 
     @Override
     public int read(ByteBuffer path, ByteBuffer buf, long offset, FUSEFileInfo fi) {
         final String METHOD_NAME = "read";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, buf, offset, fi);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else if(offset < 0 || offset > Integer.MAX_VALUE) {
             Log.warning("'offset' out of range: " + offset);
             res = -EINVAL;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else if(!(e instanceof File))
                 res = -EACCES; // ?
             else {
                 File f = (File) e;
                 res = f.data.read(buf, offset);
 
                 // Update access time.
                 f.accessTime.setToMillis(System.currentTimeMillis());
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, buf, offset,
                 fi);
         return res;
     }
 
     @Override
     public int write(ByteBuffer path, ByteBuffer buf, final long offset,
             FUSEFileInfo fi) {
         final String METHOD_NAME = "write";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, buf, offset, fi);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else if(offset < 0 || offset > Integer.MAX_VALUE) {
             Log.warning("'offset' out of range: " + offset);
             res = -EINVAL;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else if(!(e instanceof File))
                 res = -EACCES; // ?
             else {
                 File f = (File) e;
                 final int len = buf.remaining();
 
                 f.data.write(buf, offset);
 
                 // Update modification time.
                 f.modificationTime.setToMillis(System.currentTimeMillis());
                 
                 res = len;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, buf, offset,
                 fi);
         return res;
     }
 
     @Override
     public int statfs(ByteBuffer path,
 		       StatVFS st) {
         final String METHOD_NAME = "statfs";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, st);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 Runtime rt = Runtime.getRuntime();
                 long freeMem = rt.freeMemory();
                 long maxMem = rt.maxMemory();
                 long totalMem = rt.totalMemory();
 
                 st.f_frsize = blockSize;
                 st.f_bsize = 1*1024*1024; // As much data as possible in each op
 
                 st.f_blocks = maxMem / blockSize;
                 st.f_bfree = (freeMem + (maxMem - totalMem)) / blockSize;
                 st.f_bavail = st.f_bfree;
 
                 st.f_flag = 0;
                 st.f_namemax = Integer.MAX_VALUE; // yes?
 
                 // We do not set the inode number limits... not sure about that yet.
                 st.f_files = Integer.MAX_VALUE;
                 st.f_ffree = Integer.MAX_VALUE - lastInodeId;
                 st.f_favail = st.f_ffree;
 
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, st);
         return res;
     }
     
     @Override
     public int setxattr(ByteBuffer path,
             ByteBuffer name,
             ByteBuffer value,
             int flags,
             long position) {
         final String METHOD_NAME = "setxattr";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, name, value, flags, position);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         String nameString = FUSEUtil.decodeUTF8(name);
         Log.trace("  pathString = \"" + pathString + "\"");
         Log.trace("  nameString = \"" + nameString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence (path) that could not be decoded.");
             res = -ENOENT;
         }
         else if(nameString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence (name) that could not be decoded.");
             res = -ENOENT;
         }
         else if(position < 0 || position > Integer.MAX_VALUE) {
             Log.warning("'position' argument out of int range: " + position);
             res = -EINVAL;
         }
         else if(position != 0 && !nameString.equals("com.apple.ResourceFork")) {
             Log.warning("'position' argument not zero (=" + position +
                     ") for non-resource fork argument \"" + nameString + "\"");
             res = -EINVAL;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 boolean existsXattr = e.existsXattr(nameString);
 
                 if(XattrUtil.isCreateFlagSet(flags) && existsXattr)
                     res = -EEXIST;
                 else if(XattrUtil.isReplaceFlagSet(flags) && !existsXattr)
                     res = -ENOATTR;
                 else {
 
                     DataStream stream;
                     if(existsXattr)
                         stream = e.getXattr(nameString);
                     else
                         stream = e.createXattr(nameString);
 
                     if(stream == null) {
                         if(existsXattr)
                             throw new RuntimeException("Unexpected: No \"" +
                                     nameString + "\" attribute found!");
                         else
                             throw new RuntimeException("Unexpected: Could " +
                                     "not create attribute \"" + nameString +
                                     "\"");
                     }
 
                     if(nameString.equals("com.apple.ResourceFork"))
                         stream.write(value, position);
                     else {
                         stream.truncate(0);
                         stream.write(value, 0);
                     }
                     res = 0;
                 }
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, name, value, flags, position);
         return res;
     }
 
     @Override
     public int getxattr(ByteBuffer path,
             ByteBuffer name,
             ByteBuffer value,
             long position) {
         final String METHOD_NAME = "getxattr";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, name, value, position);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         String nameString = FUSEUtil.decodeUTF8(name);
         Log.trace("  pathString = \"" + pathString + "\"");
         Log.trace("  nameString = \"" + nameString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence (path) that could not be decoded.");
             res = -ENOENT;
         }
         else if(nameString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence (name) that could not be decoded.");
             res = -ENOENT;
         }
         else if(position < 0 || position > Integer.MAX_VALUE) {
             Log.warning("'position' argument out of int range: " + position);
             res = -EINVAL;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 DataStream stream = e.getXattr(nameString);
                 if(stream == null)
                     res = -ENOATTR; // No such attribute.
                 else if(value == null) {
                     /* We are supposed to return the length of the extended
                      * attribute. position argument is in this case ignored,
                      * as this is how Mac OS X behaves. */
                     long len = stream.getLength();
                     if(len > 0xFFFFFFFFL)
                         res = (int) 0xFFFFFFFFL;
                     else
                         res = (int) len;
 
                 }
                 else {
                     int remainingData = (int) (stream.getLength() - position);
                     if(remainingData <= 0)
                         res = 0;
                     else {
                         if(value.remaining() < remainingData)
                             res = -ERANGE;
                         else
                             res = stream.read(value, position);
                     }
                 }
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, name, value, position);
         return res;
     }
 
     @Override
     public int listxattr(ByteBuffer path,
             ByteBuffer namebuf) {
         final String METHOD_NAME = "listxattr";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, namebuf);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 int len = 0;
                 for(String cur : e.listXattrs()) {
                     byte[] utf8Name = FUSEUtil.encodeUTF8(cur);
                     int curLen = utf8Name.length + 1;
                     
                     if(namebuf != null) {
                         if(namebuf.remaining() < curLen) {
                             len = -ERANGE;
                             break;
                         }
                         else {
                             namebuf.put(utf8Name);
                             namebuf.put((byte) 0); // Each name is null terminated.
                         }
                     }
 
                     len += curLen;
                 }
                 res = len;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, namebuf);
         return res;
 
     }
 
     @Override
     public int removexattr(ByteBuffer path,
             ByteBuffer name) {
         final String METHOD_NAME = "removexattr";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, name);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         String nameString = FUSEUtil.decodeUTF8(name);
         Log.trace("  pathString = \"" + pathString + "\"");
         Log.trace("  nameString = \"" + nameString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence (path) that could not be decoded.");
             res = -ENOENT;
         }
         else if(nameString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence (name) that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 if(e.removeXattr(nameString))
                     res = 0;
                 else
                     res = -ENOATTR;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, name);
         return res;
 
     }
 
     @Override
     public int getxtimes(ByteBuffer path, Timespec bkuptime, Timespec crtime) {
         final String METHOD_NAME = "getxtimes";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, bkuptime, crtime);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 bkuptime.setToTimespec(e.backupTime);
                 crtime.setToTimespec(e.createTime);
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, bkuptime,
                 crtime);
         return res;
     }
 
     @Override
     public int setbkuptime(ByteBuffer path, Timespec tv) {
         final String METHOD_NAME = "setbkuptime";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, tv);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 e.backupTime.setToTimespec(tv);
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, tv);
         return res;
     }
 
     @Override
     public int setcrtime(ByteBuffer path, Timespec tv) {
         final String METHOD_NAME = "setcrtime";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, tv);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 e.createTime.setToTimespec(tv);
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, tv);
         return res;
     }
 
     @Override
     public int setchgtime(ByteBuffer path, Timespec tv) {
         final String METHOD_NAME = "setchgtime";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, tv);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 e.statusChangeTime.setToTimespec(tv);
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, tv);
         return res;
     }
 
     @Override
     public int chflags(ByteBuffer path, int flags) {
         final String METHOD_NAME = "chflags";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, flags);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 Log.debug("Changing flags from 0x" +
                         Integer.toHexString(e.flags) + " to 0x" +
                         Integer.toHexString(flags) + "...");
                 e.flags = flags;
                 res = 0;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, flags);
         return res;
     }
 
     //@Override
     //public int setattr_x(ByteBuffer path, Setattr_x attr) {
     public int setattr_x_disabled(ByteBuffer path, Setattr_x attr) {
         final String METHOD_NAME = "setattr_x";
         Log.traceEnter(CLASS_NAME + "." + METHOD_NAME, path, attr);
 
         final int res;
         String pathString = FUSEUtil.decodeUTF8(path);
         Log.trace("  pathString = \"" + pathString + "\"");
         if(pathString == null) { // Invalid UTF-8 sequence.
             Log.warning("Recieved byte sequence that could not be decoded.");
             res = -ENOENT;
         }
         else {
             Inode e = lookupInode(pathString);
             if(e == null)
                 res = -ENOENT;
             else {
                 int status = 0;
                 do {
                     if(attr.wantsMode())
                         e.setRWXSModeBits(attr.mode);
 
                     if(attr.wantsUid())
                         e.uid = attr.uid;
 
                     if(attr.wantsGid())
                         e.gid = attr.gid;
 
                     if(attr.wantsSize()) {
                         if(e instanceof File)
                             truncateFile(((File)e), attr.size);
                         else {
                             if(e instanceof Directory)
                                 status = -EISDIR;
                             else
                                 status = -EACCES; // Access denied for non-file inode.
                             break;
                         }
                     }
 
                     if(attr.wantsAcctime())
                         e.accessTime.setToTimespec(attr.acctime);
 
                     if(attr.wantsModtime())
                         e.modificationTime.setToTimespec(attr.modtime);
 
                     if(attr.wantsCrtime())
                         e.createTime.setToTimespec(attr.crtime);
 
                     if(attr.wantsChgtime())
                         e.statusChangeTime.setToTimespec(attr.chgtime);
                     else
                         e.statusChangeTime.setToMillis(System.currentTimeMillis());
 
                     if(attr.wantsBkuptime())
                         e.backupTime.setToTimespec(attr.bkuptime);
 
                     if(attr.wantsFlags())
                         e.flags = attr.flags;
                 } while(false);
 
                 res = status;
             }
         }
 
         Log.traceLeave(CLASS_NAME + "." + METHOD_NAME, res, path, attr);
         return res;
     }
     
     public static void main(String[] args) {
         Log.info(CLASS_NAME + ".main(");
         for(String s : args)
            Log.info("\"" + s + "\" ");
        Log.info("\b)");
         FUSE.main(args, new TestFS());
     }
 }
