 
 package org.paxle.core.io.temp.impl;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.paxle.core.io.temp.ITempDir;
 
 public class FSTempDir extends ATempDir implements ITempDir {
 	
 	private final File directory;
 	
 	public FSTempDir(File dir, String prefix) throws IOException {
 		super(prefix);
 		this.directory = dir;
 		if (!dir.exists())
 			if (!dir.mkdirs())
 				throw new IOException("Couldn't create directory for temporay files: " + dir);
 	}
 	
	public FSTempDir(File dir, boolean deleteOnExit) throws IOException {
 		this(dir, null);
 	}
 	
 	public File createTempFile(String prefix, String suffix) throws IOException {
 		final String name = generateNewName(prefix, suffix);
 		final File r = new File(this.directory, name);
 		if (!r.createNewFile())
 			throw new IOException("file '" + name + "' in directory '" + this.directory + "' already exists, this is an internal error and must be fixed");
 		return r;
 	}
 }
