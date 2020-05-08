 /**
  * This file was written by Miguel Gonzalez and is part of the
  * game "LittleWars". For more information mailto info@my-reality.de
  * or visit the game page: http://dev.my-reality.de/littlewars
  * 
  * Updates the game files from google code
  * 
  * @version 	0.4
  * @author 		Miguel Gonzalez		
  */
 package de.myreality.dev.littlewars.components;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.newdawn.slick.SlickException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import de.myreality.dev.littlewars.components.resources.ResourceManager;
 
 public class Updater implements Runnable {
 	
 	private String currentVersion;
 	private boolean done;
 	private boolean found;
 	private boolean preperationPhase;
 	private boolean reachable;
 	public static final String server = "raw.github.com/MyRealityCoding/LittleWars";
	public static final String serverPath = "https://raw.github.com/MyRealityCoding/LittleWars/master/";
 	public static final String tempDir = ".temp";
 	
 	// Files to update
 	private List<Pair<String, String> > downloads;
 	
 	// Download size in bytes
 	private long downloadSize;
 	
 	// Current download size
 	private long currentSize, tmpSize;
 	
 	public Updater() {
 		currentVersion = ResourceManager.getInstance().getText("TXT_GAME_VER");
 		done = false;
 		found = false;
 		reachable = true;
 		preperationPhase = false;
 		downloads = new ArrayList<Pair<String, String> >();
 		downloadSize = 0;
 		currentSize = 0;
 		tmpSize = 0;
 	}
 
 	@Override
 	public void run() {
 		
 		// Check if server is reachable
 		if (isServerReachable(server)) {
 			// Checking for updates
 			String onlineVersion = "";		
 				
 			try {
 				URL url = new URL("https://" + server + "/master/res/meta.xml");			
 				
 				onlineVersion = getOnlineVersion(url);
 			} catch (SlickException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 				
 			if (compare(currentVersion, onlineVersion) < 0) {
 				// Update!	
 				found = true;				
 				calculate();				
				update("https://" + server + "/master/", onlineVersion);
 			}
 		} else {
 			Debugger.getInstance().write("Server 'www." + server + "' is not reachable.");		
 			reachable = false;
 		}
 		
 		done = true;
 	}
 	
 	public boolean isDone() {
 		return done;
 	}
 	
 	private String getOnlineVersion(URL metaXMLFile) throws SlickException, IOException {
 		String onlineVersion = "";
 		InputStream is;
 		is = metaXMLFile.openStream();
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = null;
 		try {
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			throw new SlickException("Could not load resources", e);
 		}
 		Document doc = null;
         try {
 			doc = docBuilder.parse(is);
 		} catch (SAXException e) {
 			throw new SlickException("Could not load resources", e);
 		} catch (IOException e) {
 			throw new SlickException("Could not load resources", e);
 		}
  
 		// normalize text representation
         doc.getDocumentElement ().normalize ();
  
         NodeList listResources = doc.getElementsByTagName("resource");
  
         int totalResources = listResources.getLength();
         
         for(int resourceIdx = 0; resourceIdx < totalResources; resourceIdx++) {
  
         	Node resourceNode = listResources.item(resourceIdx);
  
         	if(resourceNode.getNodeType() == Node.ELEMENT_NODE){
         		Element resourceElement = (Element)resourceNode;
  
         		String type = resourceElement.getAttribute("type");
 
         		if(type.equals("text") && resourceElement.getAttribute("id").equals("TXT_GAME_VER")) {        			
         			onlineVersion = resourceElement.getTextContent();
         		}   		
         	}
         }
 
         is.close();	
 		
 		return onlineVersion;
 	}
 	
 	public boolean isServerReachable(String url) {		
 
 		try {
 			URL u = new URL ( "https://" + url); 
 			HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
 			huc.setDoOutput(true);
 			huc.setRequestMethod ("GET"); 
 			huc.connect(); 		 
 			int code = huc.getResponseCode();
 			if (code == 200) {
 				 return true;
 			}
 		} catch (IOException e) {			
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	private static int compare(String v1, String v2) {
         String s1 = normalisedVersion(v1);
         String s2 = normalisedVersion(v2);
         int cmp = s1.compareTo(s2);
         return cmp;
     }
 
     public static String normalisedVersion(String version) {
         return normalisedVersion(version, ".", 4);
     }
 
     public static String normalisedVersion(String version, String sep, int maxWidth) {
         String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
         StringBuilder sb = new StringBuilder();
         for (String s : split) {
             sb.append(String.format("%" + maxWidth + 's', s));
         }
         return sb.toString();
     }
     
     public boolean hasFoundUpdate() {
     	return found;
     }
 
 	public float getPercent() {
 		return (float) (currentSize + tmpSize) * 100 / (float) downloadSize;
 	}
 	
 	private void update(String strurl, String newVersion) {
 		try {
 			@SuppressWarnings("unused")
 			URL url = new URL(strurl);
 			Debugger.getInstance().write("Begin download for LittleWars v. " + newVersion + "..");
 			
 			for (Pair<String, String> file : downloads) {
 				downloadFile(file.getFirst(), file.getSecond());
 			}
 			
 			Debugger.getInstance().write("LittleWars v. " + newVersion + " has been downloaded.");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}		
 	}
 	
     public void openPatchnotes() {
         if(java.awt.Desktop.isDesktopSupported() ) {
               java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        
               if(desktop.isSupported(java.awt.Desktop.Action.BROWSE) ) {
             	  URI uri;
 				try {
 					uri = new URI("http://www.dev.my-reality.de/littlewars/patchnotes/");
 					desktop.browse(uri);
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
                   
               }
             }
     }
     
     private void calculate() {
     	preperationPhase = true;
     	// Config
     	addDownloadPath(serverPath + "config/");
     	// Resources
     	addDownloadPath(serverPath + "res/");
     	// Binary
     	addDownloadFile(serverPath + "trunk/littlewars.jar", "littlewars.jar");
     	preperationPhase = false;
     }
 
 	public boolean isReachable() {
 		return reachable;
 	}
 	
 	
 	public boolean isPreperationPhase() {
 		return preperationPhase;
 	}
 	
 
 	public void downloadFile(String sourceurl, String dest){
 		
 		// Make dirs
 		File file = new File(dest);
 		File dirs = new File(file.getParent() + "/");
         if (!dirs.exists()) {
         	dirs.mkdirs();
         }
 		
         URL url;        
         // TODO: Fix download problem
         Debugger.getInstance().write("Download file '" + sourceurl + "..");
         try {
             url = new URL(sourceurl);
             HttpURLConnection hConnection = (HttpURLConnection) url
                     .openConnection();
             HttpURLConnection.setFollowRedirects(true);
             if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
                 InputStream in = hConnection.getInputStream();
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
                 byte[] buffer = new byte[4096];
                 int numRead;
                 long numWritten = 0;
                 while ((numRead = in.read(buffer)) != -1) {
                     out.write(buffer, 0, numRead);
                     numWritten += numRead;                
                     tmpSize = numWritten;
                 }
                 tmpSize = 0;
                 currentSize += numWritten;              	
                 out.close();
                 in.close();
             }
         } catch(IOException e) {
             // Save the file temporary
         	File tmpDirs = new File(tempDir + "\\" + file.getParent() + "\\");
             if (!tmpDirs.exists()) {
             	tmpDirs.mkdirs();
             }            
             downloadFile(sourceurl, tempDir + "\\" + file.getParent() + "\\" + file.getName());
         }
     }
 	
 	
 	public void addDownloadFile(String file, String local) {	
 		try {
             URL url = new URL(file);
             HttpURLConnection hConnection = (HttpURLConnection) url
                     .openConnection();
             HttpURLConnection.setFollowRedirects(true);
             if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
                 int filesize = hConnection.getContentLength();
                 
                 // Get the local size
                 File f = new File(local);
                 long localsize = f.length();
                 if (filesize != localsize) {
                 	downloadSize += filesize;		
                 	downloads.add(new Pair<String, String>(file, local));	
                 }
                 hConnection.disconnect();               	
             }                
         }catch(IOException e){
             e.printStackTrace();
         }
 	}
 	
 	public List<UpdateFile> loadServerFiles(String folder) {	
 		
 		List<UpdateFile> serverFiles = new ArrayList<UpdateFile>();
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = null;
         InputStream is;
         try {
         	URL url = new URL(serverPath + "config/files.xml");
 			is = url.openStream();	
 			docBuilder = docBuilderFactory.newDocumentBuilder();			
 			Document doc = docBuilder.parse (is);			
 			// normalize text representation
 	        doc.getDocumentElement ().normalize(); 
 	        NodeList listResources = doc.getElementsByTagName("file");        
 	        int totalResources = listResources.getLength();
 	       
 	        // Load the content
 	        for(int resourceIdx = 0; resourceIdx < totalResources; resourceIdx++){
 	        	 
 	        	Node resourceNode = listResources.item(resourceIdx);
 	 
 	        	if(resourceNode.getNodeType() == Node.ELEMENT_NODE){        		
 	        		Element resourceElement = (Element)resourceNode; 
 	        		String source = resourceElement.getAttribute("src");
 	        		long size = Long.parseLong(resourceElement.getAttribute("size"));
 	        		long sum = Long.parseLong(resourceElement.getAttribute("sum"));
 	        		source = source.replace('\\', '/');
 	        		if (folder.equals(source.substring(0, folder.length()))) {	
 						serverFiles.add(new UpdateFile(source, size, sum));
 					}
 	        	}
 	        }			
         } catch (FileNotFoundException e) {
         	e.printStackTrace();
         } catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
 		return serverFiles;		
 	}
 
 	
 	public void addDownloadPath(String file) {
 		// Initialize the comparison
 		List<File> AllLocalFiles = new ArrayList<File>();
 		List<UpdateFile> localFiles = new ArrayList<UpdateFile>();	
 		List<UpdateFile> serverFiles = loadServerFiles(file.replace(serverPath, ""));
 		
 		if (!serverFiles.isEmpty()) {
 			// Get all local files
 			String localPath = file.replace(serverPath, "");
 			addFilesRecursively(new File(localPath), AllLocalFiles);
 			
 			for (File f : AllLocalFiles) {
 				if (f.isFile()) {
 					localFiles.add(new UpdateFile(f.getPath().replace("\\", "/"), f.length(), getFileCharSize(f.getPath())));
 				}
 			}
 		
 			// Compare the files and add them as download
 			for (UpdateFile serverFile : serverFiles) {			
 				boolean foundOnDisk = false;
 				for (UpdateFile localFile : localFiles) {					
 					if (localFile.getPath().equals(serverFile.getPath())) {							
 						if (!localFile.getSum().equals(serverFile.getSum()) ||
 							!localFile.getSize().equals(serverFile.getSize())) {
 							downloadSize += serverFile.getSize();	
 							System.out.println("Add file " + serverFile.getPath());
 							downloads.add(new Pair<String, String>(serverPath + serverFile.getPath(), serverFile.getPath()));	
 						}
 						foundOnDisk = true;
 						break;
 					} 			
 				}
 				if (!foundOnDisk) {
 					downloadSize += serverFile.getSize();	
 					System.out.println("Add file " + serverFile.getPath());
 					downloads.add(new Pair<String, String>(serverPath + serverFile.getPath(), serverFile.getPath()));
 				}
 			}
 		}		
 	}
 	
 	
 
 	
 	public long getCurrentSize() {
 		return currentSize + tmpSize;
 	}
 	
 	public long getDownloadSize() {
 		return downloadSize;
 	}
 	
 	
 	public static void addFilesRecursively(File file, List<File> all) {
 	    final File[] children = file.listFiles();
 	    if (children != null) {
 	        for (File child : children) {
 	        	child.setReadable(true);
 	        	child.setWritable(true);
 	        	child.setExecutable(true);	        	
 	            all.add(child);
 	            addFilesRecursively(child, all);
 	        }
 	    }
 	}
     
     
     
     public static long getFileCharSize(String path) {
 		FileInputStream stream = null;
 		try {
 			stream = new FileInputStream(new File(path));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		try {
 		    FileChannel fc = stream.getChannel();
 		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 		    /* Instead of using default, pass in a decoder. */
 		    String content = Charset.defaultCharset().decode(bb).toString();
 		    long checksum = 0;
 			for (int i = 0; i < content.length(); ++i) {
 				checksum += content.charAt(i);
 			}
 			return checksum;
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			  try {
 				stream.close();
 			  } catch (IOException e) {
 					e.printStackTrace();
 			  }
 		}		
 		return 0;
 	}
     
     
     /**
      * Update content from temporary folder in order to 
      * fix the update problem
      */
     public static void updateTemporary() {
     	// Scan files
     	List<File> allFiles = new ArrayList<File>();
     	List<File> tmpFiles = new ArrayList<File>();
     	addFilesRecursively(new File(tempDir), allFiles);
     	for (File f : allFiles) {
     		if (f.isFile()) {
     			tmpFiles.add(f);
     		}
     	}
     	// Copy files
     	for (File f : tmpFiles) {
     		String newPath = f.getPath().replace(tempDir + "\\", "");
     		copyfile(f.getPath(), newPath);
     	}
     	
     	// Delete temporary files
     	for (File f : allFiles) {
     		f.delete();    		
     	}
     }
     
     
     
     
     private static void copyfile(String srFile, String dtFile){
 		  try{
 			  File f1 = new File(srFile);
 			  File f2 = new File(dtFile);
 			  InputStream in = new FileInputStream(f1); 
 		
 			  //For Overwrite the file.
 			  OutputStream out = new FileOutputStream(f2);
 		
 			  byte[] buf = new byte[1024];
 			  int len;
 			  while ((len = in.read(buf)) > 0){
 				  out.write(buf, 0, len);
 			  }
 			  in.close();
 			  out.close();
 			  Debugger.getInstance().write("File copied.");
 		  }
 		  catch(FileNotFoundException ex) {
 			  Debugger.getInstance().write(ex.getMessage() + " in the specified directory.");
 		  }
 		  catch(IOException e) {
 			  System.out.println(e.getMessage());  
 		  }
 	    }    	  
 }
