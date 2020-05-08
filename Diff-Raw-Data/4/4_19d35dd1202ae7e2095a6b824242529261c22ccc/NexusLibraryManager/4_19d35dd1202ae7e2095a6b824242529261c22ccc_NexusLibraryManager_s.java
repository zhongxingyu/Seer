 package com.nexus.classloading;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.nexus.NexusServer;
 import com.nexus.NexusVersion;
 import com.nexus.build.ILibraryList;
 import com.nexus.logging.NexusLog;
 import com.nexus.utils.CertificateHelper;
 
 public class NexusLibraryManager{
 	
 	private final ArrayList<ILibraryList> RequiredLibraries = new ArrayList<ILibraryList>();
 	private final ArrayList<String> LoadedLibraries = new ArrayList<String>();
 	
 	private final File LibraryDir = CreateLibDir();
 	
 	private static NexusLibraryManager INSTANCE;
 	
 	private static String LibDirPath = "lib";
 	
 	private NexusClassLoader ClassLoader;
 	
 	public static void main(String[] args){
 		if(args.length < 2){
 			NexusLog.info("Usage: NexusLibraryManager <LibDirPath> <ILibraryList1> [ILibraryList2]...");
 		}
 		int i = 0;
 		for(String s : args){
 			if(i == 0){
 				LibDirPath = s;
 			}else{
 				AddRequiredLibrary(s);
 			}
 			i++;
 		}
 		instance().load();
 	}
 	
 	public static NexusLibraryManager instance(){
 		if(INSTANCE == null){
 			return new NexusLibraryManager();
 		}
 		return INSTANCE;
 	}
 	
 	public static void AddRequiredLibrary(String s){
 		try{
 			AddRequiredLibrary(Class.forName(s));
 		}catch(ClassNotFoundException e){
 		}
 	}
 	
 	public static void AddRequiredLibrary(Class<?> cl){
 		try{
 			Object inst = cl.newInstance();
 			AddRequiredLibrary((ILibraryList) inst);
 		}catch(Exception e){
 		}
 	}
 	
 	public static void AddRequiredLibrary(ILibraryList lib){
 		if(instance().RequiredLibraries.contains(lib)) return;
 		instance().RequiredLibraries.add(lib);
 	}
 	
 	public NexusLibraryManager(){
 		INSTANCE = this;
 		if(NexusServer.Instance != null){
 			this.ClassLoader = NexusServer.Instance.ClassLoader;
 		}
 	}
 	
 	public void load(){
 		if(NexusVersion.IsDevelopmentVersion) return;
 		List<Throwable> LoadingErrors = new ArrayList<Throwable>();
 		
 		try{
 			for(ILibraryList lib : this.RequiredLibraries){
 				for(int i = 0; i < lib.getLibraries().length; i++){
 					boolean ShouldDownload = false;
 					
 					String LibraryName = lib.getLibraries()[i];
 					String TargetFileName = LibraryName.lastIndexOf('/') >= 0 ? LibraryName.substring(LibraryName.lastIndexOf('/')) : LibraryName;
 					String Checksum = lib.getChecksums()[i];
 					File LibraryFile = new File(this.LibraryDir, TargetFileName);
 					if(!LibraryFile.exists()){
 						try{
 							this.DownloadFile(LibraryFile, lib.getDownloadURL(), LibraryName, Checksum);
 							ShouldDownload = true;
 						}catch(Throwable e){
 							LoadingErrors.add(e);
 							continue;
 						}
 					}
 					
 					if(LibraryFile.exists() && !LibraryFile.isFile()){
 						LoadingErrors.add(new RuntimeException(String.format("Found a file %s that is not a normal file - you should clear this out of the way", LibraryFile)));
 						continue;
 					}
 					
 					if(!ShouldDownload){
 						try{
 							FileInputStream is = new FileInputStream(LibraryFile);
 							FileChannel channel = is.getChannel();
 							MappedByteBuffer mappedFile = channel.map(MapMode.READ_ONLY, 0, LibraryFile.length());
 							String FileChecksum = this.GenerateChecksum(mappedFile);
 							is.close();
 							if(!Checksum.equals(FileChecksum)){
 								LoadingErrors.add(new RuntimeException(String.format("The file %s was found in your lib directory and has an invalid checksum %s (expecting %s) - it is unlikely to be the correct download, please move it out of the way and try again.", LibraryName, FileChecksum, Checksum)));
 								continue;
 							}
 							channel.close();
 							is.close();
 						}catch(Exception e){
 							NexusLog.log(Level.SEVERE, e, "The library file %s could not be validated", LibraryFile.getName());
 							
 							LoadingErrors.add(new RuntimeException(String.format("The library file %s could not be validated", LibraryFile.getName()), e));
 							continue;
 						}
 					}
 					
 					if(!ShouldDownload){
 						NexusLog.fine("Found library file %s present and correct in lib dir", LibraryName);
 					}else{
 						NexusLog.fine("Library file %s was downloaded and verified successfully", LibraryName);
 					}
 					
 					try{
 						if(ClassLoader != null){
 							ClassLoader.addURL(LibraryFile.toURI().toURL());
 							LoadedLibraries.add(LibraryName);
 						}
 					}catch(MalformedURLException e){
 						LoadingErrors.add(new RuntimeException(String.format("Should never happen - %s is broken - probably a somehow corrupted download. Delete it and try again.", LibraryFile.getName()), e));
 					}
 				}
 			}
 		}finally{
 			if(!LoadingErrors.isEmpty()){
 				NexusLog.severe("There were errors during initial Nexus setup. " + "Some files failed to download or were otherwise corrupted. " + "You will need to manually obtain the missing files from " + "the following download links and ensure your lib directory is clean. ");
 				for(ILibraryList set : this.RequiredLibraries){
 					for(String file : set.getLibraries()){
 						NexusLog.severe("*** Download " + set.getDownloadURL(), file);
 					}
 				}
 				NexusLog.severe("");
 				NexusLog.severe("The following is the errors that caused the setup to fail. " + "They may help you diagnose and resolve the issue");
 				for(Throwable t : LoadingErrors){
 					if(t.getMessage() != null){
 						NexusLog.severe(t.getMessage());
 					}
 				}
 				NexusLog.severe("");
 				NexusLog.severe("The following is diagnostic information for developers to review.");
 				for(Throwable t : LoadingErrors){
 					NexusLog.log(Level.SEVERE, t, "Error details");
 				}
 				throw new RuntimeException("A fatal error occured and Nexus cannot continue");
 			}
 			NexusLog.info("All the libraries are ready and loaded. Now booting nexus!");
 		}
 	}
 	
 	private void DownloadFile(File LibraryFile, String DownloadURL, String RealFilePath, String Checksum){
 		try{
 			URL Download = new URL(String.format(DownloadURL, RealFilePath));
 			NexusLog.info("Downloading file %s", Download.toString());
 			URLConnection connection = Download.openConnection();
 			connection.setConnectTimeout(5000);
 			connection.setReadTimeout(5000);
 			connection.setRequestProperty("User-Agent", "Nexus Library Downloader");
 			int sizeGuess = connection.getContentLength();
 			this.PerformDownload(connection.getInputStream(), sizeGuess, Checksum, LibraryFile);
 			NexusLog.info("Download complete");
 		}catch(Exception e){
 			if(e instanceof RuntimeException) throw (RuntimeException) e;
 			NexusLog.severe("There was a problem downloading the file %s automatically. Perhaps you " + "don\'t have internet access. You will need to download " + "the file manually or restart and let it try again", LibraryFile.getName());
 			LibraryFile.delete();
 			throw new RuntimeException("A download error occured", e);
 		}
 	}
 	
 	private final ByteBuffer DownloadBuffer = ByteBuffer.allocateDirect(1 << 23);
 	
 	private void PerformDownload(InputStream is, int sizeGuess, String validationHash, File target){
 		if(sizeGuess > DownloadBuffer.capacity()){
 			throw new RuntimeException(String.format("The file %s is too large to be downloaded by Nexus", target.getName()));
 		}
 		DownloadBuffer.clear();
 		
 		int BytesRead = 0;
 		int FullLength = 0;
 		
 		try{
 			byte[] smallBuffer = new byte[1024];
 			while ((BytesRead = is.read(smallBuffer)) >= 0){
 				DownloadBuffer.put(smallBuffer, 0, BytesRead);
 				FullLength += BytesRead;
 			}
 			is.close();
 			DownloadBuffer.limit(FullLength);
 			DownloadBuffer.position(0);
 		}catch(InterruptedIOException e){
 			Thread.interrupted();
 			return;
 		}catch(IOException e){
 			throw new RuntimeException(e);
 		}
 		
 		try{
 			String cksum = this.GenerateChecksum(DownloadBuffer);
 			if(cksum.equals(validationHash)){
 				DownloadBuffer.position(0);
 				FileOutputStream fos = new FileOutputStream(target);
 				fos.getChannel().write(DownloadBuffer);
 				fos.close();
 			}else{
 				throw new RuntimeException(String.format("The downloaded file %s has an invalid checksum %s (expecting %s). The download did not succeed correctly and the file has been deleted. Please try launching again.", target.getName(), cksum, validationHash));
 			}
 		}catch(Exception e){
 			if(e instanceof RuntimeException) throw (RuntimeException) e;
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private String GenerateChecksum(ByteBuffer buffer){
 		return CertificateHelper.getFingerprint(buffer);
 	}
 	
 	private File CreateLibDir(){
 		File libDir = new File(LibDirPath);
 		try{
 			libDir = libDir.getCanonicalFile();
 		}catch(IOException e){
 			throw new RuntimeException("Unable to canonicalize the lib dir", e);
 		}
 		if(!libDir.exists()){
 			libDir.mkdir();
 		}else if(libDir.exists() && !libDir.isDirectory()){
 			throw new RuntimeException("Found a lib file that's not a directory");
 		}
 		return libDir;
 	}
 }
