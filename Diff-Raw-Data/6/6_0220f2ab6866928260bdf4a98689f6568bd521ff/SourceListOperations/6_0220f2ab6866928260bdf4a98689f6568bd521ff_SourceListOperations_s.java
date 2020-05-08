 package com.calcprogrammer1.calctunes.SourceList;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 
 import org.jaudiotagger.audio.*;
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xmlpull.v1.XmlSerializer;
 
 import com.calcprogrammer1.calctunes.FileOperations;
 import com.calcprogrammer1.calctunes.Library.LibraryDatabaseHelper;
 import com.calcprogrammer1.calctunes.SourceTypes.LibrarySource;
 import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;
 import com.calcprogrammer1.calctunes.Subsonic.CalcTunesXMLParser;
 
 import android.content.Context;
 import android.util.Xml;
 
 public class SourceListOperations
 {   
     public static void writeLibraryFile(LibrarySource lib)
     {
         XmlSerializer serializer = Xml.newSerializer();
         StringWriter  writer     = new StringWriter();
         try
         {
             //Start document
             serializer.setOutput(writer);
             serializer.startDocument("UTF-8", true);
             serializer.startTag("", "CalcTunesLibrary");
             
             //Name
             serializer.startTag("", "name");
             serializer.text(lib.name);
             serializer.endTag("", "name");
             
             //Folders
             serializer.startTag("", "folders");
             
             for(int i = 0; i < lib.folders.size(); i++)
             {
                 serializer.startTag("", "folder");
                 serializer.text(lib.folders.get(i));
                 serializer.endTag("", "folder");
             }
             
             serializer.endTag("", "folders");
             
             //End document
             serializer.endTag("", "CalcTunesLibrary");
             serializer.endDocument();
             PrintWriter out = new PrintWriter(lib.filename);
             out.println(writer.toString());
             out.close();
         }
         catch(Exception e){}      
     }
     
     public static LibrarySource readLibraryFile(String libFilePath)
     {
         LibrarySource lib = new LibrarySource();
         
         String XMLData;
         Document DocData;
         NodeList NodeData;
         
         lib.filename = libFilePath;
         
         //Read XML file
         XMLData = CalcTunesXMLParser.getXmlFromFile(libFilePath);
         DocData = CalcTunesXMLParser.getDomElement(XMLData);
         NodeData = DocData.getElementsByTagName("CalcTunesLibrary");
         if(NodeData.getLength() == 1)
         {
             NodeData = DocData.getElementsByTagName("name");
             lib.name = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             NodeData = DocData.getElementsByTagName("folder");
             for(int i = 0; i < NodeData.getLength(); i++)
             {
                 lib.folders.add(NodeData.item(i).getChildNodes().item(0).getNodeValue());
             }
         }
 
         return lib;
     }
 
     public static void scanMediaIntoDatabase(Context c, String libFilePath)
     {
         LibrarySource lib = readLibraryFile(libFilePath);
         String libName = lib.name;
         ArrayList<File> libFiles = new ArrayList<File>();
         
         for(int i = 0; i < lib.folders.size(); i++)
         {
             File f = new File(lib.folders.get(i));
             FileOperations.addFilesRecursively(f, libFiles);
         }
         
         writeMediaDatabase(c, libFiles.toArray(new File[libFiles.size()]), libName);
     }
     
     public static ArrayList<LibrarySource> readLibraryList(String libPath)
     {
         try
         {
             File libDir = new File(libPath);
             File[] libFiles = FileOperations.selectFilesOnly(libDir.listFiles());
             ArrayList<LibrarySource> libData = new ArrayList<LibrarySource>();
             for(int i = 0; i < libFiles.length; i++)
             {
                 LibrarySource lib = readLibraryFile(libFiles[i].getAbsolutePath());
                 lib.status = LibrarySource.LIBRARY_OK;
                 File file = new File("/data/data/com.calcprogrammer1.calctunes/databases/" + lib.name + ".db");
                 if(!file.exists())
                 {
                     lib.status = LibrarySource.LIBRARY_UNAVAILABLE;
                 }
                 else
                 {
                     for(int j = 0; j < lib.folders.size(); j++)
                     {
                         File directory = new File(lib.folders.get(j));
                         if(!directory.exists() || (directory.isDirectory() && (directory.list().length == 0)))
                         {
                             lib.status = LibrarySource.LIBRARY_OFFLINE;
                         }
                     }
                 }
                 libData.add(lib);
             }
             return libData;
         }
         catch(Exception e)
         {
             return new ArrayList<LibrarySource>();
         }
     }
     
     public static void writeSubsonicFile(SubsonicSource sub)
     {
         XmlSerializer serializer = Xml.newSerializer();
         StringWriter  writer     = new StringWriter();
         try
         {
             //Start document
             serializer.setOutput(writer);
             serializer.startDocument("UTF-8", true);
             serializer.startTag("", "CalcTunesSubsonicServer");
             
             //Name
             serializer.startTag("", "name");
             serializer.text(sub.name);
             serializer.endTag("", "name");
             
             //Address
             serializer.startTag("", "address");
             serializer.text(sub.address);
             serializer.endTag("", "address");
             
             //Port
             serializer.startTag("", "port");
             serializer.text("" + sub.port);
             serializer.endTag("", "port");
             
             //Username
             serializer.startTag("", "username");
             serializer.text(sub.username);
             serializer.endTag("", "username");
             
             //Password
             serializer.startTag("", "password");
             serializer.text(sub.password);
             serializer.endTag("", "password");
             
             //Download Path
             serializer.startTag("", "downloadpath");
             serializer.text(sub.downloadPath);
             serializer.endTag("", "downloadpath");
             
             //Streaming Format
             serializer.startTag("", "streamingformat");
             serializer.text(sub.streamingFormat);
             serializer.endTag("", "streamingformat");
             
             //Streaming Bitrate
             serializer.startTag("", "streamingbitrate");
             serializer.text(sub.streamingBitrate);
             serializer.endTag("", "streamingbitrate");
             
             //End document
             serializer.endTag("", "CalcTunesSubsonicServer");
             serializer.endDocument();
             PrintWriter out = new PrintWriter(sub.filename);
             out.println(writer.toString());
             out.close();
         }
         catch(Exception e){}      
     }
 
     public static SubsonicSource readSubsonicFile(String subFilePath)
     {
         SubsonicSource sub = new SubsonicSource();
         
         String XMLData;
         Document DocData;
         NodeList NodeData;
         
         sub.filename = subFilePath;
         
         //Read XML file
         XMLData = CalcTunesXMLParser.getXmlFromFile(subFilePath);
         DocData = CalcTunesXMLParser.getDomElement(XMLData);
         NodeData = DocData.getElementsByTagName("CalcTunesSubsonicServer");
         if(NodeData.getLength() == 1)
         {
             //Get Name
             NodeData = DocData.getElementsByTagName("name");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.name = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
             
             //Get Address
             NodeData    = DocData.getElementsByTagName("address");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.address = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
             
             //Get Port
             NodeData = DocData.getElementsByTagName("port");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.port = Integer.parseInt(NodeData.item(0).getChildNodes().item(0).getNodeValue());
             }
             
             //Get Username
             NodeData     = DocData.getElementsByTagName("username");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.username = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
             
             //Get Password
             NodeData     = DocData.getElementsByTagName("password");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.password = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
             
             //Get Download Path
             NodeData         = DocData.getElementsByTagName("downloadpath");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.downloadPath = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
             
             //Get Streaming Format
             NodeData            = DocData.getElementsByTagName("streamingformat");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.streamingFormat = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
             
             //Get Streaming Bitrate
             NodeData             = DocData.getElementsByTagName("streamingbitrate");
             if(NodeData.item(0).getChildNodes().getLength() > 0)
             {
                 sub.streamingBitrate = NodeData.item(0).getChildNodes().item(0).getNodeValue();
             }
         }
         return sub;
     }
 
     public static ArrayList<SubsonicSource> readSubsonicList(String subPath)
     {
         try
         {
             File subDir = new File(subPath);
             File[] subFiles = FileOperations.selectFilesOnly(subDir.listFiles());
             ArrayList<SubsonicSource> subData = new ArrayList<SubsonicSource>();
             for(int i = 0; i < subFiles.length; i++)
             {
                 SubsonicSource sub = readSubsonicFile(subFiles[i].getAbsolutePath());
                 sub.status = SubsonicSource.SUBSONIC_OK;
                 subData.add(sub);
             }
             return subData;
         }
         catch(Exception e)
         {
             return new ArrayList<SubsonicSource>();
         }
     }
     
     public static AudioFile readAudioFileReadOnly(File inFile)
     {
         AudioFile f = null;
         try
         {
 
                 f = AudioFileIO.read(inFile);
         }
         catch(Exception e)
         {
                 try
                 {
                     f = new MP3File(inFile, MP3File.LOAD_IDV1TAG, true);
                 }catch (Exception ex){}
         }
         return f;
     }
     
     public static String formatTime(int duration)
     {
         String time = "";
         int mins = duration / 60;
         int secs = duration % 60;
         time = mins + ":" + String.format("%02d", secs);
         return time;
     }
     
     public static String getLibraryPath(Context c)
     {
         return c.getApplicationContext().getExternalFilesDir(null).getPath() + "/libraries";
     }
     
     public static String getAlbumArtPath(Context c)
     {
         return c.getApplicationContext().getExternalFilesDir(null).getPath() + "/albumart";
     }
     
     public static String getSubsonicPath(Context c)
     {
         return c.getApplicationContext().getExternalFilesDir(null).getPath() + "/subsonic";
     }
     
     public static String makeFilename(String s)
     {
         return s.replaceAll("[^a-zA-Z0-9]", "");
     }
     
     public static String getFilename(String name)
     {
         String fileName = "";
         
         fileName = makeFilename(name);
         
         return fileName + ".xml";
     }
     
     public static String getLibraryFullPath(Context c, String libName)
     {
         return getLibraryPath(c) + "/" + getFilename(libName);
     }
  
     public static void writeMediaDatabase(Context c, File[] files, String libName)
     {
        LibraryDatabaseHelper db = new LibraryDatabaseHelper(c, libName + ".db");
        db.startDatabase();
        for(int i = 0; i < files.length; i++)
        {
            db.addFileToDatabase(files[i]);
        }
     }
 }
