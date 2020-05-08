 package org.genericsystem.file;
 
 import org.genericsystem.annotation.Components;
 import org.genericsystem.annotation.Dependencies;
 import org.genericsystem.annotation.InstanceGenericClass;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.constraints.InstanceValueClassConstraint;
 import org.genericsystem.annotation.constraints.SingularConstraint;
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Context;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.Snapshot;
 import org.genericsystem.core.TreeImpl;
 import org.genericsystem.file.FileSystem.Directory;
 import org.genericsystem.file.FileSystem.FileType;
 import org.genericsystem.file.FileSystem.FileType.File;
 import org.genericsystem.file.FileSystem.FileType.FileContent;
 import org.genericsystem.generic.Attribute;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SystemGeneric
 @InstanceValueClassConstraint(String.class)
 @Components(FileSystem.class)
 @Dependencies(FileType.class)
 @InstanceGenericClass(Directory.class)
 public class FileSystem extends TreeImpl {
 	protected static Logger log = LoggerFactory.getLogger(FileSystem.class);
 
 	private static final String SEPARATOR = "/";
 
 	private static final byte[] EMPTY = "<html/>".getBytes();
 
	public static class Directory extends GenericImpl {
 		public <T extends File> Snapshot<T> getFiles(Context context) {
 			return getHolders(context, context.<Attribute> find(FileType.class));
 		}
 
 		public <T extends File> T getFile(Context context, final String name) {
 			return getHolderByValue(context, context.<Attribute> find(FileType.class), name);
 		}
 
 		public <T extends File> T addFile(Cache cache, String name) {
 			return addFile(cache, name, EMPTY);
 		}
 
 		public <T extends File> T addFile(Cache cache, String name, byte[] content) {
 			T result = addHolder(cache, cache.<Attribute> find(FileType.class), name);
 			result.setContent(cache, content);
 			return result;
 		}
 
 		public <T extends File> T touchFile(Cache cache, String name) {
 			return touchFile(cache, name, EMPTY);
 		}
 
 		public <T extends File> T touchFile(Cache cache, String name, byte[] content) {
 			T result = setHolder(cache, cache.<Attribute> find(FileType.class), name);
 			result.setContent(cache, content);
 			return result;
 		}
 
 		public <T extends Directory> Snapshot<T> getDirectories(final Context context) {
 			return getChildren(context);
 		}
 
 		public <T extends Directory> T getDirectory(Context context, final String name) {
 			return getChild(context, name);
 		}
 
 		public <T extends Directory> T addDirectory(Cache cache, String name) {
 			return addNode(cache, name);
 		}
 
 		public <T extends Directory> T touchDirectory(Cache cache, String name) {
 			return setNode(cache, name);
 		}
 
 		public String getShortPath() {
 			return this.<String> getValue();
 		}
 	}
 
 	@SystemGeneric
 	@Components(FileSystem.class)
 	@InstanceValueClassConstraint(String.class)
 	@InstanceGenericClass(File.class)
 	@Dependencies(FileContent.class)
 	public static class FileType extends GenericImpl {
 		@SystemGeneric
 		@SingularConstraint
 		@Components(FileType.class)
 		@InstanceValueClassConstraint(byte[].class)
 		public static class FileContent extends GenericImpl {
 
 		}
 
 		public static class File extends GenericImpl {
 			public byte[] getContent(Context context) {
 				return this.<byte[]> getValues(context, context.<Attribute> find(FileContent.class)).first();
 			}
 
 			public <T extends Generic> T setContent(Cache cache, byte[] content) {
 				return setValue(cache, cache.<Attribute> find(FileContent.class), content);
 			}
 
 			public String getShortPath() {
 				return this.<String> getValue();
 			}
 		}
 	}
 
 	public <T extends Directory> Snapshot<T> getRootDirectories(Context context) {
 		return getRoots(context);
 	}
 
 	public <T extends Directory> T getRootDirectory(Context context, final String name) {
 		return getRootByValue(context, name);
 	}
 
 	public <T extends Directory> T addRootDirectory(Cache cache, String name) {
 		if (getRootDirectory(cache, name) != null)
 			throw new IllegalStateException("Root directory : " + name + " already exists");
 		return touchRootDirectory(cache, name);
 	}
 
 	public <T extends Directory> T touchRootDirectory(Cache cache, String name) {
 		return newRoot(cache, name);
 	}
 
 	public byte[] getFileContent(Cache cache, String resource) {
 		if (resource.startsWith(SEPARATOR))
 			resource = resource.substring(1);
 		String[] files = resource.split(SEPARATOR);
 		Directory directory = getRootDirectory(cache, files[0]);
 		if (directory == null)
 			return null;
 		for (int i = 1; i < files.length - 1; i++) {
 			directory = directory.getDirectory(cache, files[i]);
 			if (directory == null)
 				return null;
 		}
 		File file = directory.getFile(cache, files[files.length - 1]);
 		if (file == null)
 			return null;
 		return file.getContent(cache);
 	}
 
 	public <T extends File> T touchFile(Cache cache, String resource) {
 		return touchFile(cache, resource, EMPTY);
 	}
 
 	public <T extends File> T touchFile(Cache cache, String resource, byte[] content) {
 		if (resource.startsWith(SEPARATOR))
 			resource = resource.substring(1);
 		String[] files = resource.split(SEPARATOR);
 		Directory directory = touchRootDirectory(cache, files[0]);
 		for (int i = 1; i < files.length - 1; i++)
 			directory = directory.touchDirectory(cache, files[i]);
 		return directory.touchFile(cache, files[files.length - 1], content);
 	}
 }
