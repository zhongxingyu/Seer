 package queryfs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import java.nio.ByteBuffer;
 
 import java.nio.channels.FileChannel;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import fuse.FilesystemConstants;
 import fuse.FuseException;
 import fuse.FuseFtype;
 import fuse.FuseGetattrSetter;
 
 import queryfs.QueryGroup.Type;
 
 import static queryfs.Util.*;
 
 public class FileNode extends Node {
 	private FileChannel channel;
 	private int channelFlags = FilesystemConstants.O_RDONLY;
 	private File file;
 
 	public FileNode(QueryBackend backend, File file, Set<QueryGroup> groups) {
 		super(backend, groups);
 		assert file.exists() && !file.isDirectory();
 		this.file = file;
 		this.name = file.getName();
 	}
 
 	public int getFType() {
 		return FuseFtype.TYPE_FILE;
 	}
 
 	public Set<QueryGroup> getQueryGroups() {
 		return groups;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int rename(String from, String to, View target) throws FuseException {
		if (target != null && target != this) {
			// the common rename-swap-file-to-write behavior
 			if (target instanceof Node)
 				((Node)target).unlink();
 			else
 				return fuse.Errno.EPERM;
 		}
 		setName(new File(to).getName());
 		if (!new File(to).getParent().equals(new File(from).getParent())) {
			// move to different dir
 			Set<QueryGroup> add = new HashSet<QueryGroup>();
 			for (String tag : tagsOf(new File(to).getParent()))
 				add.add(QueryGroup.create(tag, Type.TAG));
 			Set<QueryGroup> remove = new HashSet<QueryGroup>();
 			for (String tag : tagsOf(new File(from).getParent()))
 				remove.add(QueryGroup.create(tag, Type.TAG));
 			changeQueryGroups(add, remove);
 		} else {
			// name change in same dir
 			for (QueryGroup q : groups)
 				backend.flag(q);
 			backend.flush();
 		}
 		return 0;
 	}
 
 	private void rmdirs(File f) {
 		if (f == null || !f.isDirectory() || f.list().length != 0)
 			return;
 		f.delete();
 		rmdirs(f.getParentFile());
 	}
 
 
 	public void stat(FuseGetattrSetter setter) {
 		int time = (int)(file.lastModified() / 1000L);
 		long size = file.length();
 		setter.set(
 			0, // inode
 			FuseFtype.TYPE_FILE | 0644,
 			1, // nlink
 			UID,
 			GID,
 			0, // rdev
 			size,
 			(int)((size + 511L) / 512L),
 			time, time, time // atime, mtime, ctime
 		);
 	}
 
 	public void setModified(long mtime) throws FuseException {
 		file.setLastModified(mtime);
 	}
 
 	public void changeQueryGroups(Set<QueryGroup> add, Set<QueryGroup> remove) throws FuseException {
 		changeQueryGroups(add, remove, false);
 	}
 
 	public void changeQueryGroups(Set<QueryGroup> add, Set<QueryGroup> remove, boolean allowMimetypeChange) throws FuseException {
 		if (remove != null)
 			for (QueryGroup r : remove) {
 				if (allowMimetypeChange || r.getType() != Type.MIME) {
 					groups.remove(r);
 					backend.flag(r);
 				}
 			}
 		if (add != null)
 			for (QueryGroup a : add) {
 				if (allowMimetypeChange || a.getType() != Type.MIME)
 					groups.add(a);
 			}
 		File loc = null;
 		try {
 			loc = getDestination(newPath(backend.root, groups), name);
 		} catch (IOException e) {
 			throw new FuseException(e.getMessage()).initErrno(FuseException.EIO);
 		}
 		file.renameTo(loc);
 		rmdirs(file.getParentFile());
 		file = loc;
 		for (QueryGroup g : groups)
 			backend.flag(g);
 		backend.flush();
 		if (remove != null)
 			backend.checkRoot(remove);
 		if (add != null)
 			backend.checkRootAdd(add);
 		if (untagged(groups))
 			backend.unref(this);
 	}
 
 	private boolean untagged(Set<QueryGroup> groups) {
 		for (QueryGroup group : groups)
 			if (group.getType() == Type.TAG)
 				return false;
 		return true;
 	}
 
 	public void setName(String name) throws FuseException {
 		String extI = extensionOf(this.name);
 		String extF = extensionOf(name);
 		if (!extI.equals(extF)) {
 			Set<QueryGroup> add = new HashSet<QueryGroup>();
 			Set<QueryGroup> remove = new HashSet<QueryGroup>();
 			remove.add(QueryGroup.create(extI, Type.MIME));
 			add.add(QueryGroup.create(extF, Type.MIME));
 			changeQueryGroups(add, remove, true);
 		}
 		this.name = name;
 		synchronized (this) {
 			String current = file.getName();
 			if (current.equals(name))
 				return;
 			File dest = null;
 			try {
 				dest = getDestination(file.getParent(), name);
 			} catch (IOException e) {
 				throw new FuseException(e.getMessage()).initErrno(FuseException.EIO);
 			}
 			file.renameTo(dest);
 			file = dest;
 		}
 	}
 
 	public void unlink() throws FuseException {
 		changeQueryGroups(null, new HashSet<QueryGroup>(groups));
 	}
 
 	public void open(int flags) throws FuseException {
 		synchronized (this) {
 			if (!file.exists())
 				throw new FuseException("No Such Entry").initErrno(FuseException.ENOENT);
 			if (channel == null || channelFlags < flags) {
 				try {
 					String mode = "r";
 					if (flags > FilesystemConstants.O_RDONLY)
 						mode = "rw";
 					channel = new RandomAccessFile(file, mode).getChannel();
 				} catch (FileNotFoundException e) {
 					throw new FuseException("No Such Entry", e).initErrno(FuseException.ENOENT);
 				}
 				channelFlags = flags;
 			}
 		}
 	}
 
 	public void close() throws FuseException {
 		synchronized (this) {
 			if (channel == null)
 				return;
 			try {
 				channel.close();
 			} catch (IOException eio) {
 				throw new FuseException("IO Exception", eio).initErrno(FuseException.EIO);
 			}
 			channel = null;
 			channelFlags = FilesystemConstants.O_RDONLY;
 		}
 	}
 
 	public void read(ByteBuffer buf, long offset) throws FuseException {
 		open(FilesystemConstants.O_RDONLY);
 		try {
 			channel.read(buf, offset);
 		} catch (IOException e) {
 			throw new FuseException("IO Exception", e).initErrno(FuseException.EIO);
 		}
 	}
 
 	public void write(ByteBuffer buf, long offset) throws FuseException {
 		open(FilesystemConstants.O_RDWR);
 		try {
 			channel.position(offset);
 			channel.write(buf);
 		} catch (IOException e) {
 			throw new FuseException("IO Exception", e).initErrno(FuseException.EIO);
 		}
 	}
 
 	public void truncate(long size) throws FuseException {
 		open(FilesystemConstants.O_RDWR);
 		try {
 			channel.truncate(size);
 		} catch (IOException e) {
 			throw new FuseException("IO Exception", e).initErrno(FuseException.EIO);
 		}
 	}
 }
