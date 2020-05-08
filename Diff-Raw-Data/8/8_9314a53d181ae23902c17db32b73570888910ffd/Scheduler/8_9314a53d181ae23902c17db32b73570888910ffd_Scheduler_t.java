 /*
 *  Nokia Data Gathering
 *
 *  Copyright (C) 2011 Nokia Corporation
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/
 */
 
 package jobs;
 
 import play.Play;
 
 import play.jobs.*;
 import play.Logger;
 
 import play.*;
 import play.mvc.*;
 import java.util.*;
 
 import org.apache.commons.mail.*;
 import play.libs.*;
 import org.apache.commons.mail.EmailException; 
 
 
 import javax.persistence.*;
 import play.db.jpa.*;
 
 import models.Survey;
 import models.NdgUser;
 import controllers.transformer.CSVTransformer;
 import controllers.transformer.ExcelTransformer;
 import controllers.Service;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Collection;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import models.Answer;
 import models.Category;
 import models.NdgResult;
 import models.Question;
 import models.Jobs;
 import models.constants.QuestionTypesConsts;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import java.util.Iterator;
 
 
 
 public class Scheduler extends Job {
 
     public Long id;
     public String surveyId;
     public String dateTo;
     public String dateFrom;
     public String email;
     private static final String CSV = ".csv";
     private static final String XLS = ".xls";
     private static final String ZIP = ".zip";
     private static final String SURVEY = "survey";
 
     
     
     public Scheduler(Long id, String surveyId, String dateTo, String dateFrom, String email) {
         this.id = id;
         this.surveyId = surveyId;
         this.dateTo = dateTo;
         this.dateFrom = dateFrom;
         this.email = email;
        boolean exists = (new File(Play.configuration.getProperty("attachments.path"))).exists();
        if (exists) {
         doJob();
        } else {
         new File(Play.configuration.getProperty("attachments.path")).mkdirs();
         doJob();
               }
     }
     
     public void doJob() {
 
         byte[] fileContent = null;
         String fileType = "";
 
         Boolean exportWithImages = surveyHasImages(surveyId);
         //System.out.println("The survey has images is " + exportWithImages);
         Jobs jobber = null;
         jobber = Jobs.find( "byId", id ).first();
         //System.out.println("The job id is " +  id);
 
 
 
         Collection<NdgResult> results = new ArrayList<NdgResult>();
         NdgResult result = null;
         List resultsIds0 = null;
 
         String query = "SELECT ndg_result.id FROM ndg_result, transaction_log WHERE transaction_log.transaction_date BETWEEN '" + dateFrom +  "' and '"  + dateTo +  "' AND transaction_log.survey_id =" + Long.decode(surveyId) + " AND ndg_result.ndg_result_id = transaction_log.id_result";
         
        
         resultsIds0 = JPA.em().createNativeQuery(query).getResultList();
 
 
         if(resultsIds0.size()!=0){
             for (int i = 0; i < resultsIds0.size(); i++) {
 
             String o = resultsIds0.get(i).toString();
  
             result = NdgResult.find( "byId", Long.parseLong(o)).first();
                if ( result != null ) {
                     results.add( result );
                                      }      
                                                          }
             ExcelTransformer transformer = new ExcelTransformer( result.survey, results, exportWithImages );
             fileContent = transformer.getBytes();
                                    
             
             if ( exportWithImages == true ) {
                 new File(result.survey.surveyId ).mkdir();
                 new File(result.survey.surveyId + File.separator + "photos" ).mkdir();
                 fileType = XLS;
                 zipSurvey( result.survey.surveyId, fileContent, fileType );
                 File zipFile = new File(SURVEY + result.survey.surveyId + ZIP );
                 File zipDir = new File(result.survey.surveyId );
                 try {
                     FileOutputStream fop1 = new FileOutputStream(zipFile);
                     fileContent = getBytesFromFile( zipFile );
                     fop1.write(fileContent);
                     fop1.flush();
                     fop1.close();
                     //System.out.println("Done with images");
                             } catch ( IOException e ) {
                                     e.printStackTrace();
                                                       }
                 zipFile.delete();
                 deleteDir( zipDir );
                                               }
 
             if ( exportWithImages == false ) {
                 try{
                    File file = new File(Play.configuration.getProperty("attachments.path") + "/" + SURVEY + result.survey.surveyId + XLS );
                    FileOutputStream fop = new FileOutputStream(file);
                    if (!file.exists()) {
                        file.createNewFile();
                                         }
                    byte[] contentInBytes =  transformer.getBytes();
                    fop.write(contentInBytes);
                    fop.flush();
                    fop.close();
                    //System.out.println("Done no images");
                      } catch ( IOException e ) {
                                     e.printStackTrace();
                                                }
                     
                                                } 
 
 
                 MultiPartEmail emailTo = new MultiPartEmail();
                 try{
                    emailTo.setFrom("scheduler@nokiadatagathering.net");
                    emailTo.addTo(email);
                    emailTo.setSubject("NDG Scheduled Results");
                    emailTo.setMsg("Attached are your scheduled results");
                    } catch (EmailException e) {
                       e.printStackTrace();
                    }
 
                 EmailAttachment attachment = new EmailAttachment();
                 try{
                    attachment.setDescription("Results");
                    attachment.setDisposition(EmailAttachment.ATTACHMENT);
                    if ( exportWithImages == true ) {                      
                       attachment.setPath(Play.getFile(Play.configuration.getProperty("attachments.path") + "/" + SURVEY + result.survey.surveyId + ZIP ).getPath());
                    }else{
                       attachment.setPath(Play.getFile(Play.configuration.getProperty("attachments.path") + "/" + SURVEY + result.survey.surveyId + XLS ).getPath());
                    }
                    emailTo.attach(attachment);
                    } catch (EmailException e) {
                       e.printStackTrace();
                    }
 
                 Mail.send(emailTo);
                 jobber.setComplete(true);
                 jobber.save(); 
 
                         }else{
                        System.out.println("The job failed");
                         }          
       
                            }
                    
 
     private static void zipSurvey( String surveyId, byte[] fileContent, String fileType ) {
         FileOutputStream arqExport;
         try {
             if ( fileType.equals( XLS ) ) {
                 arqExport = new FileOutputStream( surveyId + File.separator + SURVEY + surveyId + XLS );
                 arqExport.write( fileContent );
                 arqExport.close();
             } else if ( fileType.equals( CSV ) ) {
                 arqExport = new FileOutputStream( surveyId + File.separator + SURVEY + surveyId + CSV );
                 arqExport.write( fileContent );
                 arqExport.close();
             }
 
             ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( Play.configuration.getProperty("attachments.path") + "/" + SURVEY + surveyId + ZIP ) );
 
             zipDir( surveyId, zos );
 
             zos.close();
         } catch ( Exception e ) {
             e.printStackTrace();
         }
     }
 
     private static byte[] getBytesFromFile( File file ) throws IOException {
         InputStream is = new FileInputStream( file );
 
         long length = file.length();
 
         byte[] bytes = new byte[(int) length];
 
         int offset = 0;
         int numRead = 0;
 
         while ( offset < bytes.length && (numRead = is.read( bytes, offset, bytes.length - offset )) >= 0 ) {
             offset += numRead;
         }
 
         if ( offset < bytes.length ) {
             throw new IOException( "Could not completely read file " + file.getName() );
         }
 
         is.close();
 
         return bytes;
     }
 
     private static boolean deleteDir( File dir ) {
         if ( dir.isDirectory() ) {
             String[] children = dir.list();
             for ( int i = 0; i < children.length; i++ ) {
                 boolean success = deleteDir( new File( dir, children[i] ) );
 
                 if ( !success ) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 
     private static void zipDir( String dir2zip, ZipOutputStream zos ) {
         try {
 
             File zipDir = new File( dir2zip );
 
             String[] dirList = zipDir.list();
             byte[] readBuffer = new byte[2156];
             int bytesIn = 0;
 
             for ( int i = 0; i < dirList.length; i++ ) {
                 File f = new File( zipDir, dirList[i] );
                 if ( f.isDirectory() ) {
                     String filePath = f.getPath();
                     zipDir( filePath, zos );
                     continue;
                 }
 
                 FileInputStream fis = new FileInputStream( f );
 
                 ZipEntry anEntry = new ZipEntry( f.getPath() );
 
                 zos.putNextEntry( anEntry );
 
                 while ( (bytesIn = fis.read( readBuffer )) != -1 ) {
                     zos.write( readBuffer, 0, bytesIn );
                 }
 
                 fis.close();
             }
         } catch ( Exception e ) {
             e.printStackTrace();
         }
     }
 
     public static Boolean surveyHasImages( String surveyId ) {
         Survey survey = Survey.findById( Long.decode( surveyId ) );
 
         boolean hasImages = false;
 
         for (Category category : survey.categoryCollection){
             for ( Question question :category.questionCollection ) {
                 if ( question.questionType.typeName.equals( QuestionTypesConsts.IMAGE ) ) {
                          hasImages = true;          
                                                                                           }
 
                                                                     }
                                                             }
         return hasImages;
                                                            }
     
 }
