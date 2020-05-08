 package com.klape.file;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import java.util.Collections;
 import java.util.ArrayList;
 import java.util.List;
 
 import java.math.BigInteger;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.jboss.logging.Logger;
 
 /*
  * A simple XML-backed datasource for file metadata.
  * Data is cached in a List and Map for quick reads,
  * and every modification requires the XML to be
  * rewritten to the disk.  
  *
  * Modifications should only be limited to when a file 
  * is uploaded or removed.
  *
  * If this app ever gets used in a relatively high-load
  * environment, then this datasource will need to be
  * changed out for something that can handle 
  * modifications much more efficiently.
  */
 public class FileDao
 {
   private Logger log = Logger.getLogger(this.getClass());
   private final File xmlFile;
   private Marshaller marshaller;
   private boolean initialized = false;
 
   private Files files;
   private int currentId = 0;
 
   public FileDao(String xmlFile)
   {
     log.info("Initialize FileDao");
     this.xmlFile = new File(xmlFile);
     try
     {
       init();
       initialized = true;
     }
     catch(IOException ioe)
     {
       log.fatal("Failed to initialize FileDao", ioe);
     }
     catch(JAXBException je)
     {
       log.fatal("Failed to initialize FileDao", je);
     }
   }
 
   public void init() throws IOException, JAXBException
   {
     JAXBContext jaxb = JAXBContext.newInstance(FileType.class, Files.class);
     marshaller = jaxb.createMarshaller();
 
     if(xmlFile.exists())
     {
       Unmarshaller unmarshaller = jaxb.createUnmarshaller();
       files = (Files)unmarshaller.unmarshal(xmlFile);
     }
     else
     {
       files = new Files();
     }
 
     for(FileType file : files.getFile())
     {
       int id = file.getId().intValue();
       if(currentId < id)
       {
         currentId = id;
       }
     }
     currentId++;
   }
 
   public FileType getById(int id)
   {
 
     for(FileType file : files.getFile())
     {
       if(file.getId().intValue() == id)
       {
         return file;
       }
     }
     return null;
   }
 
   public FileType getByName(String name)
   {
     initIfNotAlreadyDone();
 
     for(FileType file : files.getFile())
     {
       if(file.getName().equals(name))
       {
         return file;
       }
     }
     return null;
   }
 
   public List<FileType> getAll()
   {
     initIfNotAlreadyDone();
 
     return Collections.unmodifiableList(files.getFile());
   }
 
   public synchronized void add(FileType file)
   {
     initIfNotAlreadyDone();
 
     try
     {
       file.setId(BigInteger.valueOf((long)currentId++));
       files.getFile().add(file);
       persist();
     }
     catch(IOException ioe)
     {
       log.error("Failure to read/write XML", ioe);
       files.getFile().remove(files.getFile().size()-1);
     }
     catch(JAXBException jbe)
     {
       log.error("Failure to bind xml", jbe);
       files.getFile().remove(files.getFile().size()-1);
     }
   }
 
   public synchronized FileType remove(int id)
   {
     initIfNotAlreadyDone();
 
     for(int i=0; i<files.getFile().size(); i++)
     {
       FileType file = files.getFile().get(i);
       if(file.getId().intValue() == id)
       {
         try
         {
          FileType f = files.getFile().remove(i);
          File realFile = new File(f.getPath());
          realFile.delete();
           persist();
           return file;
         }
         catch(JAXBException je)
         {
           log.error("Failure to bind xml", je);
           files.getFile().add(i, file);
           return null;
         }
         catch(IOException ioe)
         {
           log.error("Failure to read/write XML", ioe);
           files.getFile().add(i, file);
           return null;
         }
       }
     }
 
     return null;
   }
 
   public FileType remove(FileType file)
   {
     initIfNotAlreadyDone();
 
     return remove(file.getId().intValue());
   }
 
   private void persist() throws IOException, JAXBException
   {
     FileOutputStream fos = null;
     try
     {
       fos = new FileOutputStream(xmlFile);
       marshaller.marshal(files, fos);
     }
     finally
     {
       fos.close();
     }
   }
 
   private void initIfNotAlreadyDone()
   {
     try
     {
       if(!initialized)
       {
         init();
       }
     }
     catch(Exception e)
     {
       throw new IllegalStateException("Dao not initialized", e);
     }
   }
 }
