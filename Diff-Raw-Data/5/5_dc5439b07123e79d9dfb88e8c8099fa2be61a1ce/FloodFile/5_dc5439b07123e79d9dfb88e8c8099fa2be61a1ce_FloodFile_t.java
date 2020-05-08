 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.security.MessageDigest;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
 import org.apache.xerces.parsers.DOMParser;
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.w3c.dom.*;
 
 import sdk.Base64.Base64;
 
 /*
  * Created on Nov 12, 2004
  *
  */
 
 /**
  * @author burke
  *
  */
 public class FloodFile 
 {
   public class Chunk 
   {
     public String hash;
     public int    index;
     public int    size;
     public int    weight;
   }
 
   public class TargetFile 
   {
     public String  name;
     public long    size;
     public Chunk[] chunks;
   }
 
   public String[]       trackers;
   public TargetFile[]   files;
   public int            chunkSize;
   public String         filePath;
   public String         contentHash;
 
   // for doing the sha1's of our chunks
   private MessageDigest sha1Encoder = null;
   
   public FloodFile(String filePath)
   {
   	this.filePath = filePath;
   	this.chunkSize = 256 * 1024; // default to 256K
 
     try
 		{
 	  	sha1Encoder = MessageDigest.getInstance("SHA-1");
 	  } 
 	  catch (Exception e) 
 		{ 
 	  	System.out.println(e.toString());
 		}
 
   }
   
   public FloodFile(String filePath, int chunkSize)
   {
   	this.filePath = filePath;
   	this.chunkSize = chunkSize;
 
     try
 		{
 	  	sha1Encoder = MessageDigest.getInstance("SHA-1");
 	  } 
 	  catch (Exception e) 
 		{ 
 	  	System.out.println(e.toString());
 		}
 
   }
   
   public FloodFile(String filePath, int chunkSize, String[] trackers)
   {
   	this.filePath = filePath;
   	this.chunkSize = chunkSize;
   	this.trackers = trackers;
 
     try
 		{
 	  	sha1Encoder = MessageDigest.getInstance("SHA-1");
 	  } 
 	  catch (Exception e) 
 		{ 
 	  	System.out.println(e.toString());
 		}
   }
   
   public boolean Read()
   {
   	InputStream inputFileStream = null;
   	File inputFile = null;
   	try
 		{
   		inputFile = new File(filePath);
   		inputFileStream = new FileInputStream(inputFile);
 		}
   	catch(Exception e)
 		{
   		System.out.println("Error: " + e);
   		System.exit(0);
 		}
   	
   	try
 		{
   		DataInputStream dataStream = new DataInputStream(inputFileStream);
   		FromXML(dataStream);
   		inputFileStream.close();
 		}
   	catch(IOException ioEx)
 		{
   		System.out.println("Error: " + ioEx);
   		System.exit(0);
 		}
   	
   	return true;
   }
   
   public boolean Write()
   {
     OutputStream outputFile = null;
     try 
     {
       outputFile = new FileOutputStream(filePath);
     } 
     catch (Exception e) 
     {
       System.out.println("Error: " + e);
       System.exit(0);
     }
 
     try
 		{
 //    	System.out.println(ToXML());
     	outputFile.write(ToXML().getBytes());
       outputFile.close();
 		}
     catch(IOException ioEx)
 		{
     	System.out.println("Error: " + ioEx);
     	System.exit(0);
 		}
 
     return true;
   } 
   
   public void Dump()
   {
   	if(files == null)
   	{
   		System.out.println("No files defined...");
   		return;
   	}
   	if(trackers == null)
   	{
   		System.out.println("No trackers defined...");
   		return;
   	}
   	
   	System.out.println("# Files   : " + files.length);
   	System.out.println("# Trackers: " + trackers.length);
   	
   	System.out.println("Files:");
   	for(int fileIndex = 0; fileIndex < files.length; fileIndex++)
   	{
   		if(files[fileIndex] == null)
   		{
   			break;
   		}
   		TargetFile file = files[fileIndex];
   		System.out.println("  name: " + file.name);
   		System.out.println("    size: " + file.size + " chunks:" + file.chunks.length);
   	}
   	
   	System.out.println("Trackers:");
   	for(int trackerIndex = 0; trackerIndex < trackers.length; trackerIndex++)
   	{
   		if(trackers[trackerIndex] == null)
   		{
   			break;
   		}
   		
   		System.out.println("  address: " + trackers[trackerIndex]);
   	}
   }
   
   public String ToXML()
   {
     DocumentBuilderFactory docBuilderFactory = null;
     DocumentBuilder docBuilder = null;
     Document document = null;
     
     Element root;
     Element fileInfo;
     Element item;
     
     // set up the objects we need to encode the flood data to XML
     try 
     {
       docBuilderFactory = DocumentBuilderFactoryImpl.newInstance();
       docBuilder = docBuilderFactory.newDocumentBuilder();
       document = docBuilder.newDocument();
     } 
     catch(Exception e) 
     {
       System.out.println("Error: " + e);
       System.exit(0);
     }
     
     // create the root node
     root = document.createElement("BitFlood");
 		
     // create the fileinfo node and append it to the root
     fileInfo = document.createElement("FileInfo");
     root.appendChild(fileInfo);
 		
     // add any tracker nodes
     for(int trackerIndex = 0; trackerIndex < trackers.length; trackerIndex++) 
     {
       if(trackers[trackerIndex] == null)
       {
         break;
       }
 		  
       item = document.createElement("Tracker");
       item.appendChild(document.createTextNode(trackers[trackerIndex]));
       root.appendChild(item);
     }
     
     for(int fileIndex = 0; fileIndex < files.length; fileIndex++) 
     {
       if(files[fileIndex] == null)
       {
         break;
       }
   		
       Element fileNode = document.createElement("File");
       fileNode.setAttribute("name", files[fileIndex].name); // FIXME: have to cleanse the filename to spec (unix)
      fileNode.setAttribute("size", Long.toString(files[fileIndex].size));
       fileInfo.appendChild(fileNode);
 
       for(int chunkIndex = 0; chunkIndex < files[fileIndex].chunks.length; chunkIndex++)
       {
       	Element chunkNode = document.createElement("Chunk");
       	chunkNode.setAttribute("index", Long.toString(files[fileIndex].chunks[chunkIndex].index));
       	chunkNode.setAttribute("hash", files[fileIndex].chunks[chunkIndex].hash);
       	chunkNode.setAttribute("size", Long.toString(files[fileIndex].chunks[chunkIndex].size));
       	chunkNode.setAttribute("weight", Long.toString(files[fileIndex].chunks[chunkIndex].weight));
       	fileNode.appendChild(chunkNode);
       }
     }
       
     // add the root node to the document
     document.appendChild(root);
   	
     // write it all out
     StringWriter  strWriter       = null;
     XMLSerializer xmlSerializer   = null;
     OutputFormat  outFormat       = null;
 
     String result = null;
     try 
     {
       xmlSerializer = new XMLSerializer();
       strWriter = new StringWriter();
       outFormat = new OutputFormat();
 
       outFormat.setEncoding("UTF-8");
       outFormat.setVersion("1.0");
       outFormat.setIndenting(true);
       outFormat.setIndent(2);
       outFormat.setLineWidth(0);
       
       xmlSerializer.setOutputCharStream(strWriter);
       xmlSerializer.setOutputFormat(outFormat);
 
       xmlSerializer.serialize(document);
       
       result = strWriter.toString();
 
       strWriter.close();
     } 
     catch (IOException ioEx) 
     {
       System.out.println("Error: " + ioEx);
       System.exit(0);
     }
     
     return result;
   }
 
   public void FromXML( DataInputStream inputStream )
   {
     DOMParser parser = new DOMParser();
     try
 		{
     	parser.setFeature("http://xml.org/sax/features/validation", false); // don't validate
     }
     catch(Exception e)
 		{
     	System.out.println("Error: " + e);
 			System.exit(0);
 		}
     
     try {
     	parser.parse(new org.xml.sax.InputSource(inputStream));
     	Document floodDataDoc = parser.getDocument();
 
     	// get all our files and fill out our 'files' array
     	NodeList fileinfoList = floodDataDoc.getElementsByTagName("FileInfo");
     	if(fileinfoList.getLength() == 1) { // should only be one fileinfo tag
         Element fileinfo = (Element) fileinfoList.item(0);
     		NodeList fileList = fileinfo.getElementsByTagName("File");
         if ( fileList.getLength() > 0)
         {
         	files = new TargetFile[fileList.getLength()];
         	
 					for ( int fileIndex = 0; fileIndex < fileList.getLength(); fileIndex++ )
           {
             Element file = (Element) fileList.item(fileIndex);
             TargetFile targetFile = new TargetFile();
             
            targetFile.size = Long.parseLong(file.getAttribute("size"));
             targetFile.name = file.getAttribute("name");
             
             NodeList chunkList = file.getElementsByTagName("Chunk");
             if ( chunkList.getLength() > 0 )
             {
               targetFile.chunks = new Chunk[chunkList.getLength()]; 
 
             	for ( int chunkIndex = 0; chunkIndex < chunkList.getLength(); chunkIndex++ )
               {
                 Element chunk = (Element) chunkList.item(chunkIndex);
                 Chunk tempChunk = new Chunk();
 
                 tempChunk.index  = Integer.parseInt(chunk.getAttribute("index"));
                 tempChunk.weight = Integer.parseInt(chunk.getAttribute("weight"));
                 tempChunk.size   = Integer.parseInt(chunk.getAttribute("size"));
                 tempChunk.hash   = chunk.getAttribute("hash");
 
                 if(tempChunk.index >= targetFile.chunks.length)
                 {
                 	System.out.println("Number of chunks incorrect!");
                 	System.exit(0);
                 }
                 targetFile.chunks[tempChunk.index] = tempChunk;
               }
             }
             
             if(fileIndex >= files.length)
             {
             	System.out.println("Number of files incorrect!");
             	System.exit(0);            	
             }
             files[fileIndex] = targetFile;
           }
         }
 
         // get all our trackers
         NodeList trackersList = floodDataDoc.getElementsByTagName("Tracker");
         if ( trackersList.getLength() > 0)
         {
         	trackers = new String[trackersList.getLength()];
 					for ( int trackerIndex = 0; trackerIndex < trackersList.getLength(); trackerIndex++ )
           {
             Element tracker = (Element) trackersList.item(trackerIndex);
             Node child   = tracker.getFirstChild();
             if(trackerIndex >= trackers.length)
             {
             	System.out.println("Too many trackers!");
             	System.exit(0);
             }
             trackers[trackerIndex] = child.getNodeValue().toString();
           }     	
         }
         else
         {
         	System.out.println("No trackers in flood file?");
         }
     	}
     } catch (Exception e) {
     	System.out.println("Error: " + e);
     }
   }
 
   public boolean AddTracker( final String trackerAddress)
   {
   	// we find the next open spot in the trackers array
   	int trackerIndex = 0;
     for(; trackerIndex < trackers.length; trackerIndex++) 
     {
     	if(trackers[trackerIndex] == null)
     	{
     		break;
     	}
     }
 
     // grow our array of files if necessary
     if(trackerIndex >= trackers.length)
     {
       int newSize = 2 * trackers.length;
       String[] tempTrackers = new String[newSize];
       System.arraycopy(trackers, 0, tempTrackers, 0, trackers.length);
       trackers = tempTrackers;
     }
   	
     trackers[trackerIndex] = trackerAddress;
     return true;
   }
   
   public boolean Add( final String path )
   {
   	File file = new File(path);
   	boolean result = false;
   	
   	if(file.exists() && file.canRead())
   	{
   		if(file.isDirectory())
   		{
   			result = AddDirectory(file.getAbsolutePath());
   		}
   		else
   		{
   			result = AddFile(file.getAbsolutePath());
   		}
   	}
   	
   	return result;
   }
   
   // FIXME this should throw exceptions
   public boolean AddDirectory( final String dirPath)
   {
     File dirToAdd = new File(dirPath);
     
     if(!dirToAdd.isDirectory())
     {
     	return false;
     }
     
     String absDirPath = dirToAdd.getAbsolutePath() + '/'; // change this to be the abs path for cleanup later on
     absDirPath = absDirPath.replace('\\', '/');
     
     String[] childFiles = new String[32];
 
     RecursiveFilenameFind(absDirPath, childFiles);
 
     for(int childIndex = 0; childIndex < childFiles.length; childIndex++) 
     {
     	if(childFiles[childIndex] == null)
     	{
     		break;
       }
   			
     	// FIXME i'm do a lot of shit on this one line...
     	if(!AddFile(childFiles[childIndex],
     							childFiles[childIndex].replaceFirst(absDirPath, absDirPath.substring(absDirPath.lastIndexOf('/')))))
       {
     		return false;
       }
     }
     
     return true;
   }
   
   public boolean AddFile( final String localFilePath)
   {
   	String localPath = null;
   	String targetPath = null;
 
   	localPath = localFilePath.replace('\\', '/');
   	
   	int lastSlash;
   	if((lastSlash = localPath.lastIndexOf('/')) != -1)
   	{
   		targetPath = localPath.substring(lastSlash, localPath.length());
   	}
   	else
   	{
   		targetPath = localPath;
   	}
   	
   	return AddFile(localPath, targetPath);
   }
   
   public boolean AddFile( final String localPath, final String targetPath)
   {
   	File fileToAdd = new File(localPath);
   	if(!fileToAdd.exists() || !fileToAdd.canRead())
   	{
   		return false;
   	}
   	
   	// we find the next open spot in the files array
   	int filesIndex = 0;
     for(; filesIndex < files.length; filesIndex++) 
     {
     	if(files[filesIndex] == null)
     	{
     		break;
     	}
     }
 
     // grow our array of files if necessary
     if(filesIndex >= files.length)
     {
       int newSize = 2 * files.length;
       TargetFile[] tempFiles = new TargetFile[newSize];
       System.arraycopy(files, 0, tempFiles, 0, files.length);
       files = tempFiles;
     }
 
     files[filesIndex] = new TargetFile();
     files[filesIndex].name = targetPath;
     files[filesIndex].size = fileToAdd.length();
     Long numChunks = new Long((fileToAdd.length() + (chunkSize - 1)) / chunkSize); //rounding
     files[filesIndex].chunks = new Chunk[numChunks.intValue()]; 
 
     InputStream inputFileStream = null;
     try 
     {
       inputFileStream = new FileInputStream(fileToAdd);
     } 
     catch(Exception e) 
     {
       System.out.println("Error: " + e);
       System.exit(0);
     }
       
     byte[] chunkData = new byte[chunkSize];
 
     int chunkIndex = 0;
     int offset = 0;
     int bytesRead = 0;
     int weight = 0;
     while (offset < fileToAdd.length()) 
     {
       try 
       {
         bytesRead = inputFileStream.read(chunkData, 0, chunkSize);
         offset += bytesRead; // FIXME this shouldn't be necessary, we should just detect the end of the stream
       } 
       catch(IOException e) 
       {
         System.out.println("Error: " + e);
         System.exit(0);
       }
       	
       if( bytesRead <= 0)
       {
         break;
       }
       	
       byte[] digest = null;
 
       sha1Encoder.reset();
       sha1Encoder.update(chunkData, 0, bytesRead);
       digest = sha1Encoder.digest();
 
       // NOTE: base64 encoded sha1s are always 27 chars
       String chunkHash = Base64.encodeToString(digest, false).substring(0, 27);
 
       Chunk chunk = new Chunk();
       chunk.hash = chunkHash;
       chunk.index = chunkIndex;
       chunk.size = bytesRead;
       chunk.weight = weight;
     
       if(chunkIndex >= files[filesIndex].chunks.length)
       {
       	// error: we shouldn't have gone beyond the number of chunks in the file
       	// FIXME we should probably back out anything we've done so far in terms of adding this file
       	return false;
       }
       
       files[filesIndex].chunks[chunkIndex] = chunk;
       chunkIndex++;
       
     }
       
     try 
     {
       inputFileStream.close();
     }
     catch (IOException e) 
     {
       System.out.println("Error: " + e);
       System.exit(0);
     }
     
     return true;
   }
   	
   
   private boolean RecursiveFilenameFind(String root, String[] result) 
   {
     File currentDir = new File(root);
     if(currentDir.isDirectory()) 
     {
       File[] subFiles = currentDir.listFiles();
       for(int i = 0; i < subFiles.length; i++) 
       {
         if(subFiles[i] == null)
         {
           break;
         }
 				
         if(subFiles[i].isDirectory()) 
         {
           RecursiveFilenameFind(subFiles[i].getAbsolutePath(), result);
         } 
         else 
         {
           int resultIndex = 0;
           for(; resultIndex < result.length; resultIndex++) 
           {
             if(result[resultIndex] == null)
             {	
               break;
             }
           }
 					
           if(resultIndex >= result.length) 
           {
             String[] temp = new String[2 * result.length];
             System.arraycopy(result, 0, temp, 0, result.length);
             result = temp;
           }
           result[resultIndex] = subFiles[i].getAbsolutePath();
         }
       }
     }
 
     return true;
   }  
 }
