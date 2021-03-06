 /*
   UpdateIdThread.java / Frost
   Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of
   the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
 package frost.threads;
 
 import java.io.*;
 import java.util.*;
 
 
 
 import frost.*;
 import frost.FcpTools.*;
 import frost.crypt.MetaData;
 import frost.gui.objects.FrostBoardObject;
 import frost.identities.Identity;
 import frost.messages.*;
 
 public class UpdateIdThread extends BoardUpdateThreadObject implements BoardUpdateThread
 {
     private static boolean DEBUG = true;
     private static int maxFailures = 4;
     //private static int keyCount = 0;
     //private static int minKeyCount = 50;
     //private static int maxKeysPerFile = 5000;
     
     private static final int MAX_TRIES = 2; //number of times each index will be tried -1
 
     private Vector indices;
     private File indicesFile;
     private int maxKeys;
     private String date;
     private String currentDate;
     private String oldDate;
     private int requestHtl;
     private int insertHtl;
     private String keypool;
     private FrostBoardObject board;
     private String publicKey;
     private String privateKey;
     private String requestKey;
     private String insertKey;
     private String boardState;
     private final static String fileSeparator = System.getProperty("file.separator");
     
     //these pertain to the currently received index
 //	Identity sharer = null;
 //	String _sharer = null;
 //	String pubkey = null;
 
     public int getThreadType() { return BoardUpdateThread.BOARD_FILE_DNLOAD; }
 
     /**
      * Generates a new index file containing keys to upload.
      * @return true if index file was created, else false.
      */
      
      
     private void loadIndex(String date) {
     	indicesFile = new File(frame1.keypool + board.getBoardFilename() + fileSeparator + "indices-"+date);
 	
 	//indices = new Vector();
 	
 	try {
 		if (indicesFile.exists()) {
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(indicesFile));
 			indices = (Vector)in.readObject();
 			in.close();
 		}else {
 			indices = new Vector(100);
 			for (int i = 0;i < 100;i++)
 				indices.add(new Integer(0));
 		}
 	}catch(IOException e) {
 		e.printStackTrace(Core.getOut());
 	}catch(ClassNotFoundException e) {
 		e.printStackTrace(Core.getOut());
 	}
     }
     private void commit() {
     	try{
 		indicesFile.delete();
 		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(indicesFile));
 		out.writeObject(indices);
 		out.flush();
 		out.close();
 	}catch(IOException e) {
 		e.printStackTrace(Core.getOut());
 	}
     }
     
     /**
      * resets all indices that were tried MAX_TRIES times to 0
      * for the next run of the thread
      */
     private void resetIndices() {
     	for (int i=0;i<indices.size();i++) {
 		Integer current = (Integer)indices.elementAt(i);
 		if (current.intValue() >= MAX_TRIES)
 			indices.setElementAt(new Integer(0),i);
 	}
     }
     private int findFreeUploadIndex() {
     	for (int i = 0;i<indices.size();i++){
 		Integer current = (Integer)indices.elementAt(i);
 		if (current.intValue() > -1)
 			return i;
 	}
 	return -1;
     }
     
     private int findFreeUploadIndex(int exclude) {
     	for (int i = 0;i<indices.size();i++){
 		if (i==exclude) continue;
 		Integer current = (Integer)indices.elementAt(i);
 		if (current.intValue() > -1)
 			return i;
 	}
 	return -1;
     }
     
     private int findFreeDownloadIndex() {
     	for (int i = 0;i<indices.size();i++){
 		Integer current = (Integer)indices.elementAt(i);
 		if (current.intValue() > -1 && current.intValue() < MAX_TRIES)
 			return i;
 	}
 	return -1;
     }
 
     private int findFreeDownloadIndex(int exclude) {
 	for (int i=0;i<indices.size();i++) {
 		if (i==exclude) continue;
 		Integer current = (Integer)indices.elementAt(i);
 		if (current.intValue() > -1 && current.intValue() < MAX_TRIES)
 			return i;
 	}
 	return -1;
     }
     
     private void setIndexFailed(int i) {
     	int current = ((Integer)indices.elementAt(i)).intValue();
 	
 	if (current == -1 || current > MAX_TRIES) {
 		System.err.println("\n\nWARNING - index sequence screwed in setFailed. report to a dev\n\n");
 		return;
 	}
 	
 	indices.setElementAt(new Integer(current++),i);
 	
 	commit();
 		
     }
     
     private void setIndexSuccessfull(int i) {
     	int current = ((Integer)indices.elementAt(i)).intValue();
 	if (current == -1 || current > MAX_TRIES) {
 		System.err.println("\n\nWARNING - index sequence screwed in setSuccesful. report to a dev\n\n");
 		return;
 	}
 	
 	indices.setElementAt(new Integer(-1),i);
 	
 	commit();
     }
     
     private FrostIndex makeIndexFile()
     {
         if( DEBUG ) Core.getOut().println("FILEDN: UpdateIdThread.makeIndexFile for " + board.toString());
 
         // Calculate the keys to be uploaded
         Map files = Index.getUploadKeys(board.getBoardFilename());
         
 		if (files == null)
 			return null;
        
      	return new FrostIndex(files);
     }
 
     private void uploadIndexFile(FrostIndex idx) throws Throwable
     {
     	//load the indices for the current date
 	//currentDate = DateFun.getDate();
     	loadIndex(currentDate);
         File indexFile = new File(keypool + board.getBoardFilename() + "_upload.zip");
         XMLTools.writeXmlFile(XMLTools.getXMLDocument(idx),indexFile.getPath());
         boolean success = false;
         int tries = 0;
         String[] result = {"Error", "Error"};
 
         if( indexFile.length() > 0 && indexFile.isFile() )
         {
             boolean signUpload = frame1.frostSettings.getBoolValue("signUploads");
             byte[] metadata = null;
             
             // first zip, then maybe sign the zipped file
             FileAccess.writeZipFile(FileAccess.readByteArray(indexFile), 
                                     "entry", indexFile); // WRITE TO SAME NAME??? 
                                     							//why not? ;)
             
             if( signUpload )
             {
                 byte[] zipped = FileAccess.readByteArray(indexFile);
                 MetaData md = new MetaData(zipped);
                 metadata = XMLTools.getRawXMLDocument(md);
             }
             
             // search empty slot
             int index = findFreeUploadIndex();
             while( !success && tries <= MAX_TRIES )
             {
                 // Does this index already exist?
 		           
                 result = FcpInsert.putFile(insertKey + index + ".idx.sha3.zip",  //this format is sha3 ;)
                                            indexFile,
                                            metadata,
                                            insertHtl,
                                            false); // doRedirect
                                          
                 if( result[0].equals("Success") )
                 {
                     success = true;
 		            setIndexSuccessfull(index);
                     if( DEBUG ) Core.getOut().println("FILEDN:***** Index file successfully uploaded *****");
                 }
                 else
                 {
                     if( result[0].equals("KeyCollision") )
                     {
                         index = findFreeUploadIndex(index);
                         tries=0; // reset tries
                         if( DEBUG ) Core.getOut().println("FILEDN:***** Index file collided, increasing index. *****");
                     }
                     else
                     {
                         String tv = result[0];
                         if( tv == null ) tv="";
                         if( DEBUG ) Core.getOut().println("FILEDN:***** Unknown upload error (#" + tries + ", '"+tv+"'), retrying. *****");
                     }
                 }
                 tries++;
             }
         }
     }
 
     // If we're getting too much files on a board, we lower
     // the maxAge of keys. That way older keys get removed
     // sooner. With the new index system it should be possible
     // to work with large numbers of keys because they are
     // no longer kept in memory, but on disk.
     private void adjustMaxAge(int count) {/*  //this is not used
     //if (DEBUG) Core.getOut().println("FILEDN: AdjustMaxAge: old value = " + frame1.frame1.frostSettings.getValue("maxAge"));
 
     int lowerLimit = 10 * maxKeys / 100;
     int upperLimit = 90 * maxKeys / 100;
     int maxAge = frame1.frame1.frame1.frostSettings.getIntValue("maxAge");
 
     if (count < lowerLimit && maxAge < 21)
         maxAge++;
     if (count > upperLimit && maxAge > 1)
         maxAge--;
 
     frame1.frame1.frame1.frostSettings.setValue("maxAge", maxAge);
     //if (DEBUG) Core.getOut().println("FILEDN: AdjustMaxAge: new value = " + maxAge);*/
     }
 
     public void run()
     {
         notifyThreadStarted(this);
 
         try
         {
             // Wait some random time to speed up the update of the TOF table
             // ... and to not to flood the node
             int waitTime = (int) (Math.random() * 5000);
             // wait a max. of 5 seconds between start of threads
             mixed.wait(waitTime);
 
             int index = findFreeDownloadIndex();
             int failures = 0;
 
             while (failures < maxFailures)
             {
                 if (index == -1)
                 { //something happened
                     notifyThreadFinished(this);
                     return;
                 }
                 File target =
                     File.createTempFile(
                         "frost-index-" + index,
                         board.getBoardFilename(),
                         new File(frame1.frostSettings.getValue("temp.dir")));
                 if (DEBUG)
                     Core.getOut().println(
                         "FILEDN: Requesting index "
                             + index
                             + " for board "
                             + board.getBoardName()
                             + " for date "
                             + date);
                 // Download the keyfile
                 
                 FcpResults fcpresults = FcpRequest.getFile(
                         requestKey + index + ".idx.sha3.zip",   //this format is sha3 ;)
                         null,
                         target,
                         requestHtl
                             + ((Integer)indices.elementAt(index)).intValue(),
                         //^^^ this way we bypass the failure table
                         true);
                         
                 if( fcpresults != null && target.length() > 0)
                 {
                     //mark it as successful
                     setIndexSuccessfull(index);
                     failures = 0;
 					
 					//check if we have received such file before
 					String digest = Core.getCrypto().digest(target);
 					if (Core.getMessageSet().contains(digest)) {
 						//we have.  erase and continue
 						target.delete();
 						index = findFreeDownloadIndex();
 						continue;
 					}
 					//else add it to the set of received files to prevent duplicates
 					Core.getMessageSet().add(digest);
 					
                     // Add it to the index
                     try
                     {
                         // first check if received ZIP file is correctly signed
                         byte[] zippedXml = FileAccess.readByteArray(target);
 						//we need to unzip here to check if identity IN FILE == identity IN METADATA
 						byte[] unzippedXml = FileAccess.readZipFileBinary(target);
                         if( unzippedXml == null )
                         {
                             Core.getOut().println("Could not extract received zip file, skipping.");
                             target.delete();
                             index = findFreeDownloadIndex();
                             continue;
                         }
 						
                         File unzippedTarget = new File (target.getPath() + "_unzipped");
                         FileAccess.writeByteArray(unzippedXml, unzippedTarget);
                         
 						//create the FrostIndex object
                         FrostIndex receivedIndex = null;
                         try {
                             receivedIndex = new FrostIndex(
                                 XMLTools.parseXmlFile(unzippedTarget,false)
                                 .getDocumentElement());
                         } catch(Exception ex)
                         {
                             Core.getOut().println("Could not parse the index file, skipping.");
                             target.delete();
                             unzippedTarget.delete();
                             index = findFreeDownloadIndex();
                             continue;
                         }
                         
                         Identity sharer = null;
                         Identity sharerInFile = receivedIndex.getSharer();
                         
                         // verify the file if it is signed
                         if( fcpresults.getRawMetadata() != null ) 
                         {
                             MetaData md; 
                             try {
                                 md = new MetaData(zippedXml, fcpresults.getRawMetadata());
                             }
                             catch(Throwable t)
                             {
                                 // reading of xml metadata failed, handle
                                 t.printStackTrace(Core.getOut()); //get traces ALWAYS, please!
                                 Core.getOut().println("Could not read the XML metadata, skipping file index.");
                                 target.delete();
                                 index = findFreeDownloadIndex();
                                 continue;
                             }
                             
                             
                             //metadata says we're signed.  Check if there is identity in the file
                             if (sharerInFile == null){
                             	Core.getOut().println("MetaData present, but file didn't contain an identity :(");
                                 unzippedTarget.delete();
 								target.delete();
 								index = findFreeDownloadIndex();
 								continue;
                             }
                             
                             String _owner = null;
                             String _pubkey =null;
                             if (md.getSharer() != null ){
                             	_owner = md.getSharer().getUniqueName();
                             	_pubkey = md.getSharer().getKey();
                             }
                             
                             //check if metadata is proper
                             if( _owner == null || _owner.length() == 0 ||
                                 _pubkey == null || _pubkey.length() == 0 )
                             {
                                 Core.getOut().println("XML metadata have missing fields, skipping file index.");
                                 unzippedTarget.delete();
                                 target.delete();
                                 index = findFreeDownloadIndex();
                                 continue;
                             }
                             
                             //check if fields match those in the index file
                             if ( !_owner.equals(sharerInFile.getUniqueName()) || 
                             		!_pubkey.equals(sharerInFile.getKey()) ) {
 
 								Core.getOut().println("the identity in MetaData didn't match the identity in File! :(");
 								Core.getOut().println("file owner : " +sharerInFile.getUniqueName());
 								Core.getOut().println("file key : " +sharerInFile.getKey());
 								Core.getOut().println("meta owner: "+ _owner);
 								Core.getOut().println("meta key : "+ _pubkey);
                                 unzippedTarget.delete();
 								target.delete();
 								index = findFreeDownloadIndex();
 								continue;                            			
                             }
                         
                             //verify! :)
                             boolean valid = Core.getCrypto().detachedVerify(
                                                 zippedXml,
                                                 _pubkey,
                                                 md.getSig());
                                                 
                             if( valid == false )
                             {
                                 Core.getOut().println("Invalid sign for index file from "+_owner);
                                 unzippedTarget.delete();
                                 target.delete();
                                 index = findFreeDownloadIndex();
                                 continue;
                             }
                             
                             
 							//check if we have the owner already on the lists
                             if( Core.getMyId().getUniqueName().trim().equals(_owner) )
                             {
                                 Core.getOut().println("Received index file from myself");
                                 sharer = Core.getMyId();
                             }
                             else
                             {
                                 Core.getOut().print("Received index file from " + _owner);
                                 if( Core.getFriends().containsKey(_owner) )
                                 {
                                     sharer = Core.getFriends().Get(_owner);
                                     Core.getOut().println(", a friend");
                                 }
                                 else if( Core.getNeutral().containsKey(_owner) )
                                 {
                                     sharer = Core.getNeutral().Get(_owner);
                                     Core.getOut().println(", a neutral");
                                 }
                                 //check if person is blocked
                                 else if( Core.getEnemies().containsKey(_owner) )
                                 {
                                     if( frame1.frostSettings.getBoolValue("hideBadFiles") )
                                     {
                                     	Core.getOut().println(""); //complete the .print from above
                                         Core.getOut().println("Skipped index file from BAD user "+_owner);
                                         target.delete();
                                         unzippedTarget.delete();
                                         index = findFreeDownloadIndex();
                                         continue;
                                     }
                                     //we may chose not to block files from bad people
                                     sharer = Core.getEnemies().Get(_owner);
                                     Core.getOut().println(", an enemy");
                                 }
                                 else
                                 {
                                     // a new sharer, put to neutral list
                                     Core.getOut().println(", a new contact");
                                     sharer = addNewSharer(_owner, _pubkey);
                                     if( sharer == null ) // digest did not match, block file
                                     {
                                     	Core.getOut().println("sharer was null... :(");
                                         unzippedTarget.delete();
                                         target.delete();
                                         index = findFreeDownloadIndex();
                                         continue;
                                     }
                                 }
                             }
                         } // end-of: if metadata != null
                         else if(frame1.frostSettings.getBoolValue("hideAnonFiles"))
                         {
                             unzippedTarget.delete();
                             target.delete();
                             index = findFreeDownloadIndex();
                             continue; //do not show index.
                         }
                         
                         //TODO: rework the Index.java methods to use FrostIndex object
                         //for now just rename the file and use old methods
                         //unzippedTarget.renameTo(target);
 
 						//if the user is not on the GOOD list..
                         if( sharer == null ||
                             Core.getFriends().containsKey(sharer.getUniqueName()) == false )
                         {
                            // add only files from that user        
                            Core.getOut().println("adding only files from "+sharer.getUniqueName());                    
                            Index.add( receivedIndex, board, sharer.getUniqueName() );
                         }
                         else
                         {
                             // if user is, add all files
 							Core.getOut().println("adding all files from "+sharer.getUniqueName());
                             Index.add(unzippedTarget, board, sharer);
                         }
                         target.delete();
                         unzippedTarget.delete();
                     }
                     catch (Throwable t)
                     {
                         Core.getOut().println(
                             "Error in UpdateIdThread: " + t.getMessage());
                         t.printStackTrace(Core.getOut());
                         // delete the file and try a re download???
                     }
 
                     index = findFreeDownloadIndex(index);
                     failures = 0;
                 }
                 else
                 {
                     // download failed. Sometimes there are some 0 byte
                     // files left, we better remove them now.
                     //Core.getOut().println("FILEDN:failed getting index "+index
                     target.delete();
                     setIndexFailed(index);
                     failures++;
                     index = findFreeDownloadIndex(index);
                 }
             }
             if (isInterrupted()) // check if thread should stop
             {
                 notifyThreadFinished(this);
                 return;
             }
 
             // Ok, we're done with downloading the keyfiles
             // Now calculate whitch keys we want to upload.
             // We only upload own keyfiles if:
             // 1. We've got more than minKeyCount keys to upload
             // 2. We don't upload any more files
             //index -= maxFailures;
             FrostIndex frostIndex = makeIndexFile();
             if (frostIndex!=null)
             {
           
                     if (DEBUG)
                         Core.getOut().println(
                             "FILEDN: Starting upload of index file to board '"
                                 + board.toString());
                     uploadIndexFile(frostIndex);
     
             }
             else
             {
                 if (DEBUG)
                     Core.getOut().println(
                         "FILEDN: No keys to upload, stopping UpdateIdThread for "
                             + board.toString());
            }
 
         }
         catch (Throwable t)
         {
             Core.getOut().println("Oo. EXCEPTION in UpdateIdThread:");
             t.printStackTrace(Core.getOut());
         }
 
         notifyThreadFinished(this);
         resetIndices();
         commit();
     }
     
     /**
      * This method checks if the digest of sharer matches the pubkey,
      * and adds the NEW identity to list of neutrals.   
      * @param _sharer
      * @param _pubkey
      * @return
      */
     private Identity addNewSharer(String _sharer, String _pubkey)
     {
         Identity sharer = null;
         
         //check if the digest matches
         String given_digest = _sharer.substring(_sharer.indexOf("@") + 1,
                                                 _sharer.length()).trim();
         String calculatedDigest = frame1.getCrypto().digest(_pubkey.trim()).trim();
         calculatedDigest = mixed.makeFilename( calculatedDigest ).trim();
         
         if( ! mixed.makeFilename(given_digest).equals( calculatedDigest ) )
         {
             Core.getOut().println("Warning: public key of sharer didn't match its digest:");
             Core.getOut().println("given digest :'" + given_digest+"'");
             Core.getOut().println("pubkey       :'" + _pubkey.trim()+"'");
             Core.getOut().println("calc. digest :'"+calculatedDigest+"'");
             return null;        
         }
         //create the identity of the sharer
         sharer = new Identity( _sharer.substring(0,_sharer.indexOf("@")),
                                null,
                                _pubkey);
         //add him to the neutral list
         Core.getNeutral().Add(sharer);
         return sharer;
     }
 
     /**Constructor*/
     public UpdateIdThread(FrostBoardObject board, String date)
     {
         super(board);
         this.board = board;
         this.date = date;
 	currentDate = DateFun.getDate();
         requestHtl = frame1.frostSettings.getIntValue("keyDownloadHtl");
         insertHtl = frame1.frostSettings.getIntValue("keyUploadHtl");
         keypool = frame1.frostSettings.getValue("keypool.dir");
         maxKeys = frame1.frostSettings.getIntValue("maxKeys");
 	
 	//first load the index with the date we wish to download
 	loadIndex(date);
 
         publicKey = board.getPublicKey();
         privateKey = board.getPrivateKey();
 
         if( board.isPublicBoard()==false && publicKey != null )
         {
             requestKey = new StringBuffer().append(publicKey).append("/").append(date).append("/").toString();
         }
         else
         {
             requestKey = new StringBuffer().append("KSK@frost/index/")
                          .append(board.getBoardFilename())
                          .append("/")
                          .append(date)
                          .append("/").toString();
         }
 
 	//make all inserts today
         if( board.isPublicBoard()==false && privateKey != null )
             insertKey = new StringBuffer().append(privateKey).append("/").append(currentDate).append("/").toString();
         else
             insertKey = new StringBuffer().append("KSK@frost/index/").append(board.getBoardFilename())
                         .append("/").append(currentDate).append("/").toString();
     }
 }
