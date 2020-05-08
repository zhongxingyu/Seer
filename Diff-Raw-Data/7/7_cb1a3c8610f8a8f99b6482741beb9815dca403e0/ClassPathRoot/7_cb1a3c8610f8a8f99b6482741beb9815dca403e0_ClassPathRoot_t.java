 package com.bertvanbrakel.test.finder;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.io.FilenameUtils;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableSet;
 
 //TODO:may need to specialize into different root types. DIr/Zip/Url/other...?
 public class ClassPathRoot implements Root {
 	private static final Collection<String> DEFAULT_ARCHIVE_TYPES = ImmutableSet.of("jar", "war", "zip", "ear");
 	private final File path;
 	private final TYPE type;
 	private final boolean isArchive;
 	
 	public ClassPathRoot(File path){
 		this(path,TYPE.UNKNOWN);
 	}
 	
 	public ClassPathRoot(File path,TYPE type){
 		this.path = checkNotNull(path,"expect path");
 		this.type = checkNotNull(type,"expect root type");
 		this.isArchive = isArchive(path.getName());
 	}
 	
 	private static boolean isArchive(String fileName) {
 		String extension = FilenameUtils.getExtension(fileName);
 		return extension == null ? false : DEFAULT_ARCHIVE_TYPES.contains(extension);
 	}
 
 	@Override
 	public OutputStream getResourceOutputStream(String relPath) throws IOException {
 		if( isArchive()){
 			//read zip??
 			return null;
 		} else if( isDirectory()){
 			//TODO:check relPath is in the given directory, no escaping up!
 			File f = new File(path.getAbsolutePath(),relPath);
			if( !f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
 			if( !f.canWrite()){
				throw new FileNotFoundException(String.format("Don't have permission to write file '%s' in dir '%s' for root %s. Full path %s",relPath,path.getAbsolutePath(),this, f.getAbsolutePath()));
 			}
 			if( !f.exists()){
 				f.getParentFile().mkdir();
 			}
 			return new FileOutputStream(f);
 		} else {
 			throw new FileNotFoundException(String.format("Couldn't write resource path '%s' for root %s",relPath,this));
 		}
 	}
 	
 	@Override
 	public InputStream getResourceInputStream(String relPath) throws IOException {
 		if( isArchive()){
 			//read zip??
 			getAsZip();//
 			return null;
 		} else if( isDirectory()){
 			//TODO:check relPath is in the given directory, no escaping up!
 			File f = new File(path.getAbsolutePath(),relPath);
 			if( !f.exists()){
 				throw new FileNotFoundException(String.format("Couldn't find file '%s' in dir '%s' for root %s",relPath,path.getAbsolutePath(),this));
 			}
 			if(!f.canRead()){
 				throw new FileNotFoundException(String.format("Don't have permission to read file '%s' in dir '%s' for root %s",relPath,path.getAbsolutePath(),this));
 			}
 			return new FileInputStream(f);
 		} else {
 			throw new FileNotFoundException(String.format("Couldn't read resource path '%s' for root %s",relPath,this));
 		}
 	}
 	
 	private ZipFile getAsZip(){
 		try {
 			return new ZipFile(path);
 		} catch (ZipException e) {
 			throw new ClassFinderException("Error opening archive file:" + path.getAbsolutePath(), e);
 		} catch (IOException e) {
 			throw new ClassFinderException("Error opening archive file:" + path.getAbsolutePath(), e);
 		}
 	}
 	
 	@Override
 	public boolean isTypeKnown(){
 		return !TYPE.UNKNOWN.equals(type);
 	}
 
 	@Override
 	public String getPathName(){
 		return path.getAbsolutePath();
 	}
 	
 	public File getPath(){
 		return path;
 	}
 	
 	@Override
 	public TYPE getType(){
 		return type;
 	}
 	
 	@Override
 	public boolean isDirectory(){
 		return path.isDirectory();
 	}
 	
 	@Override
 	public boolean isArchive() {
 		return isArchive;
 	}
 
 	@Override
 	public String toString(){
 		return Objects
     		.toStringHelper(this)
     		.add("path", getPathName())
     		.add("type", type)
     		.add("isArchive", isArchive())
     		.toString();
 	}
 
 	@Override
     public int hashCode() {
 	    final int prime = 31;
 	    int result = 1;
 	    result = prime * result + (isArchive ? 1231 : 1237);
 	    result = prime * result + ((path == null) ? 0 : path.hashCode());
 	    result = prime * result + ((type == null) ? 0 : type.hashCode());
 	    return result;
     }
 
 	@Override
     public boolean equals(Object obj) {
 	    if (this == obj)
 		    return true;
 	    if (obj == null)
 		    return false;
 	    if (getClass() != obj.getClass())
 		    return false;
 	    ClassPathRoot other = (ClassPathRoot) obj;
 	    if (isArchive != other.isArchive)
 		    return false;
 	    if (path == null) {
 		    if (other.path != null)
 			    return false;
 	    } else if (!path.equals(other.path))
 		    return false;
 	    if (type != other.type)
 		    return false;
 	    return true;
     }
 
 }
