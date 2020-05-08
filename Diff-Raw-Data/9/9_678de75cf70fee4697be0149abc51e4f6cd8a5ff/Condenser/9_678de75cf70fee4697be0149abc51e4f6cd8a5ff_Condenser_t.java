 package ru.rkfg.tests.diststorage;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Random;
 
 import org.hibernate.NonUniqueResultException;
 import org.hibernate.Session;
 
 import ru.ppsrk.gwt.client.ClientAuthenticationException;
 import ru.ppsrk.gwt.client.LogicException;
 import ru.ppsrk.gwt.server.HibernateCallback;
 import ru.ppsrk.gwt.server.HibernateUtil;
 import ru.ppsrk.gwt.server.ServerUtils;
 import ru.rkfg.tests.diststorage.domain.RainFileDB;
 
 public class Condenser {
 
     private RainFile rainFile;
     private Storage storage;
     private RaindropContentWriter writer;
     private Random random = new Random();
 
     public Condenser(final RainHash hash, Storage storage, final String filename) throws LogicException, ClientAuthenticationException {
         this.storage = storage;
         HibernateUtil.exec(new HibernateCallback<Void>() {
 
             @Override
             public Void run(Session session) throws LogicException, ClientAuthenticationException {
                 try {
                     RainFileDB rainFileDB = (RainFileDB) session.createQuery("from RainFileDB where selfHash = :sh")
                             .setBinary("sh", hash.getBytes()).uniqueResult();
                     rainFile = new RainFile(rainFileDB);
                     ServerUtils.createFileDirs(ServerUtils.expandHome(filename));
                     FileOutputStream stream = new FileOutputStream(filename);
                     if (rainFile.getFec() != null && rainFile.getFec()) {
                         writer = new FECContentWriter(stream, rainFile.getFileLength());
                     } else {
                         writer = new PlainContentWriter(stream, rainFile.getFileLength());
                     }
                 } catch (NonUniqueResultException e) {
                     throw new LogicException("Non-unique selfHash found: " + hash.getBase64());
                 } catch (FileNotFoundException e) {
                     throw new ExtractionError("File not found: " + filename);
                 } catch (IOException e) {
                     throw new ExtractionError("IOException while loading rainFile: " + e.getMessage());
                 }
                 return null;
             }
         });
     }
 
     public void extractFile() throws ClientAuthenticationException, LogicException {
         try {
             int dataIndex = 0;
             int skip = 0;
             for (RainHash rainHash : rainFile.getHashList()) {
                 if (skip == 0) {
                     RainDrop rainDrop = storage.retrieveRaindrop(rainHash);
                    if (random.nextInt(100) < 95) {
                         if (writer.writeBlock(rainDrop.getContent(), dataIndex++)) {
                             skip = FEC.n - dataIndex;
                             dataIndex = 0;
                             System.out.println("--- segment ---");
                         }
                     } else {
                         System.out.println("Data loss simulated at: " + dataIndex++);
                     }
                 } else {
                     skip--;
                 }
             }
         } catch (FileNotFoundException e) {
             throw new ExtractionError("Error extracting file, file not found: " + e.getMessage());
         } catch (IOException e) {
             throw new ExtractionError("IOException while extracting file: " + e.getMessage());
         } finally {
             try {
                 writer.closeStream();
             } catch (IOException e) {
                 throw new ExtractionError("Can't close the writer stream: " + e.getMessage());
             }
         }
     }
 }
