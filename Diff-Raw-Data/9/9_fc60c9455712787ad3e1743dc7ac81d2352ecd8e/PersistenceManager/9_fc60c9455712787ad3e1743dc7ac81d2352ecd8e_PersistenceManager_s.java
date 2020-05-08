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
 
 package br.org.indt.ndg.lwuit.control;
 
 import br.org.indt.ndg.lwuit.model.CategoryAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceAnswer;
 import br.org.indt.ndg.lwuit.model.DateAnswer;
 import br.org.indt.ndg.lwuit.model.DescriptiveAnswer;
 import br.org.indt.ndg.lwuit.model.ImageAnswer;
 import br.org.indt.ndg.lwuit.model.NDGAnswer;
 import br.org.indt.ndg.lwuit.model.NumericAnswer;
 import br.org.indt.ndg.lwuit.model.TimeAnswer;
 import br.org.indt.ndg.lwuit.ui.GeneralAlert;
 import br.org.indt.ndg.lwuit.ui.WaitingScreen;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import br.org.indt.ndg.mobile.FileSystem;
 import br.org.indt.ndg.mobile.NdgConsts;
 import br.org.indt.ndg.mobile.Resources;
 import br.org.indt.ndg.mobile.SortsKeys;
 import br.org.indt.ndg.mobile.Utils;
 import br.org.indt.ndg.mobile.logging.Logger;
 import br.org.indt.ndg.mobile.structures.ResultStructure;
 import com.nokia.xfolite.xforms.dom.XFormsDocument;
 import com.nokia.xfolite.xforms.submission.XFormsXMLSerializer;
 import com.nokia.xfolite.xml.dom.Document;
 import com.nokia.xfolite.xml.dom.Element;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Vector;
 import javax.microedition.io.ConnectionNotFoundException;
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 import javax.microedition.location.Location;
 
 /**
  *
  * @author kgomes
  */
 public class PersistenceManager {
     private static final int VERSION = 2;//1-without conditional categories;2-with conditional categories
 
     private static PersistenceManager instance = null;
 
     private SaveResultsObserver m_saveObserver = null;
     private boolean error = false;
     private String resultId;
     private ResultStructure mAnswers = null;
     private XFormsDocument xFormDoc = null;
     private boolean encryption = false;
     private static String ORX_NAMESPACE = "http://openrosa.org/xforms/metadata";
     private static String INSTANCE_NAME = "orx:instanceID";
     private static String META_NAME = "orx:meta";
     private static String TIME_START_NAME = "orx:timeStart";
     private static String TIME_END_NAME = "orx:timeEnd";
     private static String DEVICE_ID_NAME = "orx:deviceID";
 
     private PersistenceManager(){
     }
 
     public static PersistenceManager getInstance(){
         if(instance==null){
             instance = new PersistenceManager();
         }
         return instance;
     }
 
     public boolean getError() {
         return error;
     }
 
     public boolean isEditing() {
         return AppMIDlet.getInstance().getFileSystem().isLocalFile();
     }
 
     public void saveNdgResult( SaveResultsObserver saveObserver ) {
         m_saveObserver = saveObserver;
 
         mAnswers = SurveysControl.getInstance().getResult();
 
        if ( !addCoordinates() ) {
           return;
        }
 
         WaitingScreen.show(Resources.SAVING_RESULT);
         SaveResultRunnable srr = new SaveResultRunnable();
         Thread t = new Thread(srr);
         t.setPriority(Thread.MIN_PRIORITY);
         t.start();
     }
 
     public void saveOpenRosaResult(XFormsDocument document, SaveResultsObserver saveObserver){
         m_saveObserver = saveObserver;
         xFormDoc = document;
 
         WaitingScreen.show(Resources.SAVING_RESULT);
         SaveXFormResultRunnable srr = new SaveXFormResultRunnable();
         Thread t = new Thread(srr);
         t.setPriority(Thread.MIN_PRIORITY);
         t.start();
     }
 
     private void encode(String surveyFilepath, String surveyFilename) throws IOException {
         FileConnection inputFile = null;
         InputStream in = null;
         FileConnection outputFile = null;
         OutputStream out = null;
         try {
             inputFile = (FileConnection) Connector.open(surveyFilepath);
             in = inputFile.openInputStream();
         } catch (IOException ex) {
         }
         try {
             String encrypedFilename = "e_" + surveyFilename;
             String encryptedSurveyFilepath = surveyFilepath + encrypedFilename;
             outputFile = (FileConnection) Connector.open( encryptedSurveyFilepath );
             if (outputFile.exists()) {
                 outputFile.delete();
             }
             outputFile.create();
             out = outputFile.openOutputStream();
         } catch (IOException ex) {
         }
         AES encrypter = new AES();
         try {
             encrypter.encrypt(in, out);
         } catch (Exception e) {
             GeneralAlert.getInstance().addCommand(ExitCommand.getInstance());
             GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.WRONG_KEY, GeneralAlert.ERROR);
         }
         in.close();
         out.close();
         inputFile.delete();
         inputFile.close();
         outputFile.rename(surveyFilename);
         outputFile.close();
     }
 
     private void saveSurvey( int surveyType ) {
         if( AppMIDlet.getInstance().getSettings() != null
           && AppMIDlet.getInstance().getSettings().getStructure().isEncryptionConfigured() ) {
             encryption = AppMIDlet.getInstance().getSettings().getStructure().getEncryption();
         }
         String surveyId = null;
         switch (surveyType) {
             case Utils.NDG_FORMAT:
                 surveyId = String.valueOf(SurveysControl.getInstance().getSurveyIdNumber());
                 break;
             case Utils.OPEN_ROSA_FORMAT:
                 surveyId = ""; //TODO get OpenRosa surveyId
                 break;
             default:
                 throw new IllegalArgumentException("Unrecognized survey format");
         }
         boolean isLocalFile = AppMIDlet.getInstance().getFileSystem().isLocalFile();
         String surveyFilename = getSurveyFilename(surveyId, isLocalFile);
         String surveyFilepath = getSurveyFilePath(surveyFilename);
         resultId = extractResultId(surveyFilename);
 
         try {
             switch (surveyType) {
                 case Utils.NDG_FORMAT:
                     persistNdgResult( surveyFilepath, surveyFilename );
                     break;
                 case Utils.OPEN_ROSA_FORMAT:
                     persistOpenRosaResult( surveyFilepath, surveyFilename, isLocalFile );
                     break;
                 default:
                     throw new IllegalArgumentException("Unrecognized survey format");
             }
         } catch ( ConnectionNotFoundException e ) {
             error = true;
             Logger.getInstance().log(e.getMessage());
             GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
             GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.ECREATE_RESULT, GeneralAlert.ERROR );
         } catch ( IOException e ) {
             error = true;
             Logger.getInstance().log(e.getMessage());
             GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
             GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.EWRITE_RESULT, GeneralAlert.ERROR );
         } catch ( Exception e ) {
             Logger.getInstance().log(e.getMessage());
             e.printStackTrace();
         } finally {
             System.gc();
         }
     }
 
     private void persistNdgResult( String surveyFilepath, String surveyFilename ) throws IOException {
         // Delete old binaries (do not move this line unless you KNOW that you can, it has to be here. i mean it.)
         AppMIDlet.getInstance().getFileSystem().deleteDir( "b_" + surveyFilename );
         // Add result to logical structure
         String displayName = getResultDisplayName();
         FileSystem fs = AppMIDlet.getInstance().getFileSystem();
         fs.storeFilename(displayName, surveyFilename);
         AppMIDlet.getInstance().setFileSystem(fs);
         // Save result file without binaries
         boolean includeBinaries = false;
         writeNdgResult(surveyFilepath, includeBinaries);
         if( encryption ) {
             encode(surveyFilepath, surveyFilename);
         }
         if ( hasBinaryData() ) {
             // Save result file with binaries
             includeBinaries = true;
             // Create directory for binaries
             String surveyDirname = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
             String surveyFilenameWithBinaries = "b_" + surveyFilename;
             String surveyDirnameWithBinaries = AppMIDlet.getInstance().getRootDir() + surveyDirname + surveyFilenameWithBinaries;
             FileConnection directory = null;
             try {
                 directory = (FileConnection) Connector.open(surveyDirnameWithBinaries);
                 if ( !directory.exists() ) {
                     directory.mkdir();
                 }
             } finally {
                 if ( directory != null )
                     directory.close();
             }
             String surveyFilePathWithBinaries = surveyDirnameWithBinaries + "/" + surveyFilenameWithBinaries;
             writeNdgResult(surveyFilePathWithBinaries, includeBinaries);
             if( encryption ) {
                 encode( surveyFilePathWithBinaries, surveyFilenameWithBinaries );
             }
         }
     }
 
     private void persistOpenRosaResult(String surveyFilepath, String surveyFilename, boolean fileExists) {
         if ( xFormDoc == null ) {
             throw new IllegalArgumentException("Tried to save OpenRosa survey when document not available");
         }
         Document instanceDocument = xFormDoc.getModel().getDefaultInstance().getDocument();
         if ( !fileExists ) {
             addOpenRosaMetadata(instanceDocument);
         }
 
         XFormsXMLSerializer serilizer = new XFormsXMLSerializer();
         try {
             FileConnection fCon = (FileConnection)Connector.open(surveyFilepath);
             if(!fCon.exists()){
                 fCon.create();
             }
             OutputStream stream = fCon.openOutputStream();
             serilizer.serialize(stream, instanceDocument.getDocumentElement(), null);
             stream.close();
 
             if( encryption ) {
                 encode(surveyFilepath, surveyFilename);
             }
 
             fCon.close();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     private void writeNdgResult( String surveyFilepath, boolean appendBinaryData ) throws IOException {
         FileConnection connection = null;
         OutputStream out = null;
         try {
             connection = (FileConnection) Connector.open(surveyFilepath);
             if ( connection.exists() ) {
                 connection.delete();
             }
             connection.create();
             out = connection.openOutputStream();
 
             PrintStream output = new PrintStream(out);
             output.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
             output.print("<result ");
             output.print("r_id=\"" + resultId + "\" ");
             // *****************************   PROTOCOL DEFINITION *************************
             //
             //****************************************************************************************************
             //Type of Msg |      SurveyID			  |      Number of  Msgs  |    	ResultID  	   | UserID
             //      9           9999999999                          99             a9b9c9d9       999999999999999
             //****************************************************************************************************
             //end of header
             output.print("s_id=\"" + String.valueOf(SurveysControl.getInstance().getSurveyIdNumber()) + "\" ");
             output.print("u_id=\"" + AppMIDlet.getInstance().getIMEI() + "\" ");
 
             long timeTaken = new Date().getTime();
             output.print("time=\"" + String.valueOf(timeTaken) + "\" " );//convert to seconds
             output.print( "version=\"" + VERSION + "\">" );
             output.println();
 
             if( AppMIDlet.getInstance().getFileStores().getResultStructure() != null &&
                AppMIDlet.getInstance().getFileStores().getResultStructure().isLocationValid())
             {
                 String latitude = AppMIDlet.getInstance().getFileStores().getResultStructure().getLatitude();
                 String longitude = AppMIDlet.getInstance().getFileStores().getResultStructure().getLongitude();
 
                 output.println("<latitude>" + latitude + "</latitude>");
                 output.println("<longitude>" + longitude + "</longitude>");
             }
 
 
             output.println("<title>" + Utils.u2x(getResultDisplayName()) + "</title>");
 
             Vector/*<CategoryAnwser>*/ anwsers = mAnswers.getAllAnwsers();
             for( int i = 0; i< anwsers.size(); i++ ){
                 CategoryAnswer category = (CategoryAnswer)anwsers.elementAt(i);
                 output.print("<category " + "name=\"" + Utils.u2x(category.getName() ) + "\" ");
                 output.println("id=\"" + category.getId() + "\">");
                 for ( int subCat = 0; subCat < category.getSubcategoriesCount(); subCat++ ) {
                     /** Subcategory **/
                     output.println("<subcategory subCatId=\"" + (subCat + 1) + "\">" );
                     Enumeration questionIndex = category.getSubCategoryAnswers( subCat ).keys();
                     Vector keys = new Vector(category.getSubCategoryAnswers( subCat ).size());
                     while( questionIndex.hasMoreElements() ) {
                         keys.addElement(questionIndex.nextElement());
                     }
                     SortsKeys sorts = new SortsKeys();
                     sorts.qsort(keys);
                     questionIndex = keys.elements();
                     while( questionIndex.hasMoreElements() ) {
                         NDGAnswer answer = (NDGAnswer) category.getSubCategoryAnswers( subCat ).get( questionIndex.nextElement() );
                         String type = answer.getType();
                         output.print("<answer " + "type=\"" + type + "\" ");
                         output.print("id=\"" + answer.getId() + "\" ");
                         output.print("visited=\"" + answer.getVisited() + "\"");
                         if (answer instanceof TimeAnswer ) {
                             output.print(" convention=\"" + ((TimeAnswer)answer).getConvetionString() + "\"");
                             output.print(">");
                             output.println();
                             answer.save(output);
                         } else if ( answer instanceof ImageAnswer ) {
                             output.print(">");
                             output.println();
                             ((ImageAnswer)answer).save( output, appendBinaryData );
                         } else {
                             output.print(">");
                             output.println();
                             answer.save(output);
                         }
                         output.println("</answer>");
                     }
                     output.println("</subcategory>");
                 }
                 output.println("</category>");
             }
             output.println("</result>");
         } finally {
             if ( out != null )
                 out.close();
             if ( connection != null )
                 connection.close();
         }
     }
 
     /**
      * @param surveyId      Id of results survey
      * @param isLocalFile   Indicates whether file already exists or new name should be generated
      * @return  Filename without survey directory part
      */
     private String getSurveyFilename( String surveyId, boolean isLocalFile ) {
         String fname;
         if (isLocalFile) { //check whether to create new file or use existing surveyFilePathWithBinaries
             fname = AppMIDlet.getInstance().getFileSystem().getResultFilename();
         } else {
             String UID = generateUniqueID();
             fname = "r_" + surveyId + "_" + AppMIDlet.getInstance().getIMEI() + "_" + UID + ".xml";
         }
         return fname;
     }
 
     private String getSurveyFilePath( String fname ) {
         String surveyDir = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
         String filename;
 
         filename = AppMIDlet.getInstance().getRootDir() + surveyDir + fname;
         return filename;
     }
 
     private String extractResultId(String filename){
         String result = "";
         int i = filename.lastIndexOf('_');
         int z = filename.indexOf('.');
         if((i>0) && (z>0)){
             result = filename.substring(i+1,z);
         }
         return result;
     }
 
     private boolean hasBinaryData() {
         boolean result = false;
         Vector/*<CategoryAnwser>*/ anwsers = mAnswers.getAllAnwsers();
         try {
             for ( int i = 0; i< anwsers.size(); i++ ) {
                 CategoryAnswer category = (CategoryAnswer)anwsers.elementAt(i);
                 for ( int subCat = 0; subCat < category.getSubcategoriesCount(); subCat++ ) {
                     Enumeration questionIndex = category.getSubCategoryAnswers( subCat ).keys();
                     while( questionIndex.hasMoreElements() ) {
                         NDGAnswer answer = (NDGAnswer) category.getSubCategoryAnswers( subCat ).get( questionIndex.nextElement() );
                         if ( answer instanceof ImageAnswer ) {
                             throw new FoundBinaryDataException();
                         }
                     }
                 }
             }
         } catch ( FoundBinaryDataException ex ) {
             result = true;
         }
         return result;
     }
 
     private class FoundBinaryDataException extends Exception {
         public FoundBinaryDataException() {}
     }
 
     private String getResultDisplayName() {
         String result = "";
         //take first answer and use it to prepare displayable survey title
         boolean found = false;
         NDGAnswer answer = null;
         for( int i = 0; i< mAnswers.getAllAnwsers().size(); i++ ) {
             try {
                 CategoryAnswer categoryanswer =  mAnswers.getCategoryAnswers( String.valueOf(i+1) );
                 Hashtable table = categoryanswer.getSubCategoryAnswers(0);
                 answer = (NDGAnswer)table.get( String.valueOf( "1" ) );
                 found = true;
                 break;
             } catch (Exception ex ) {
                 //do nothing
             }
         }
         if( !found ) {
             return resultId;
         }
 
         if (( answer instanceof ChoiceAnswer) || (answer instanceof ImageAnswer)) {
             result = resultId;
         } else if ( (answer  instanceof DescriptiveAnswer) || (answer instanceof NumericAnswer ) ) {
             result = (String) answer.getValue();
             if ( result.trim().equals("") ) {
                 result = resultId;
             }
         } else if ( answer instanceof DateAnswer ) {
             Date date = new Date( ( (DateAnswer)answer).getDate() );
             result = date.toString();
         } else if ( answer instanceof TimeAnswer ) {
             Date time = new Date( ((TimeAnswer)answer).getTime() );
             result = time.toString();
         }
         return result;
     }
 
     private String generateUniqueID() {
         Random rnd = new Random();
         long uniqueID = (((System.currentTimeMillis() >>>16)<<16)+rnd.nextLong());
         return Integer.toHexString((int) uniqueID);
     }
 
     /***
      *  @return boolean - if false saving will be aborted
      */
     private boolean addCoordinates(){
         AppMIDlet.getInstance().getFileStores().createResultStructure();
         if (!AppMIDlet.getInstance().getSettings().getStructure().getGpsConfigured())
         {
             AppMIDlet.getInstance().getFileStores().getResultStructure().resetLocation();
             return true; //gps in settings is switched off
         }
 
         if(AppMIDlet.getInstance().getFileStores().getResultStructure().isLocationValid()){
             // we do not set new location if it was already in survey and survey is modified
             // it is kept by resultHandler
             return true;
         }
 
         Location loc = AppMIDlet.getInstance().getLocation();
         if (loc == null || loc.getQualifiedCoordinates() == null) {
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
             GeneralAlert.getInstance().show(Resources.WARNING, Resources.ADD_LOCATION_FAILURE, GeneralAlert.WARNING);
             return true;
         }
 
         if(!AppMIDlet.getInstance().locationObtained()){
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_YES_NO, true);
             int dialogRetVal = GeneralAlert.getInstance().show( Resources.WARNING, Resources.LOCATION_OUT_OF_DATE, GeneralAlert.WARNING );
             if(GeneralAlert.RESULT_NO == dialogRetVal){
                 return false;
             }
         }
 
         double latitude = loc.getQualifiedCoordinates().getLatitude();
         double longtitude = loc.getQualifiedCoordinates().getLongitude();
         AppMIDlet.getInstance().getFileStores().getResultStructure().setLatitude(Double.toString(latitude));
         AppMIDlet.getInstance().getFileStores().getResultStructure().setLongitude(Double.toString(longtitude));
 
         return true;
     }
 
     /**
      * Adds required OpenRosa meta tags.
      * https://bitbucket.org/javarosa/javarosa/wiki/OpenRosaMetaDataSchema
      */
     private void addOpenRosaMetadata(Document instanceDocument){
 
         Element docElem = instanceDocument.getDocumentElement();
         docElem.setAttribute("xmlns:orx", ORX_NAMESPACE);
 
         Element metaElem = instanceDocument.createElement(META_NAME);
         Element instanceElem = instanceDocument.createElement(INSTANCE_NAME);
         Element timeStartElem = instanceDocument.createElement(TIME_START_NAME);
         Element timeEndElem = instanceDocument.createElement(TIME_END_NAME);
         Element deviceIdElem = instanceDocument.createElement(DEVICE_ID_NAME);
 
         instanceElem.setText(generateUniqueID());
         deviceIdElem.setText(AppMIDlet.getInstance().getIMEI());
 
         metaElem.appendChild(instanceElem);
         metaElem.appendChild(deviceIdElem);
         metaElem.appendChild(timeStartElem);
         metaElem.appendChild(timeEndElem);
 
         docElem.insertBefore(metaElem, docElem.getChild(0));
     }
 
     private void resultsSaved() {
         if ( m_saveObserver != null) {
             m_saveObserver.onResultsSaved();
             m_saveObserver = null;
         }
     }
 
     class SaveResultRunnable implements Runnable {
         public void run() {
             PersistenceManager.getInstance().saveSurvey(Utils.NDG_FORMAT);
             AppMIDlet.getInstance().getFileStores().resetQuestions();
             AppMIDlet.getInstance().getFileSystem().loadResultFiles();
             AppMIDlet.getInstance().setResultList(new br.org.indt.ndg.mobile.ResultList());
             PersistenceManager.getInstance().resultsSaved();
         }
     }
 
     class SaveXFormResultRunnable implements Runnable {
         public void run() {
             PersistenceManager.getInstance().saveSurvey(Utils.OPEN_ROSA_FORMAT);
             AppMIDlet.getInstance().getFileSystem().loadResultFiles();
             AppMIDlet.getInstance().setResultList(new br.org.indt.ndg.mobile.ResultList());
             PersistenceManager.getInstance().resultsSaved();
         }
     }
 }
