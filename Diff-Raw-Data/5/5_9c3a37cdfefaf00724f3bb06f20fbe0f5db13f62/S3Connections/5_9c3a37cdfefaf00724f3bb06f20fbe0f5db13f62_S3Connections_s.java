 package arcade.database;
 import arcade.games.GameData;
 import arcade.games.UserGameData;
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
 import com.amazonaws.regions.Region;
 import com.amazonaws.regions.Regions;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 /*
  * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * You may not use this file except in compliance with the License.
  * A copy of the License is located at
  *
  *  http://aws.amazon.com/apache2.0
  *
  * or in the "license" file accompanying this file. This file is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
import java.util.Random;
 
 /**
  * This sample demonstrates how to make basic requests to Amazon S3 using
  * the AWS SDK for Java.
  * <p>
  * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
  * account, and be signed up to use Amazon S3. For more information on
  * Amazon S3, see http://aws.amazon.com/s3.
  * <p>
  * <b>Important:</b> Be sure to fill in your AWS access credentials in the
  *                   AwsCredentials.properties file before you try to run this
  *                   sample.
  * http://aws.amazon.com/security-credentials
  * @author Amazon SDK and modified by Natalia Carvalho
  */
 public class S3Connections {
     
     private static final String USER_DIR = System.getProperty("user.dir");
     private static final String SRC = "/src";
     private static final String FILENAME = "/filename";
     private static final String TEMPORARY_PNG = "/temporary";
     private static final String GAMEDATA = "gamedata";
     private static final String USERGAMEDATA = "usergamedata";
     private static final String AVATAR = "avatar";
     private static final String THUMBNAIL = "thumbnail";
     private static final String ADSCREEN = "adscreen";
     private static final String BUCKET_NAME = "mycs308database";  
     private static final String RELATIVE_PATH = "/arcade/amazondownloads";
 
 
 
     private AmazonS3 myS3Instance;
     
     /**
      * Constructor that connects to S3Instance
      */
     public S3Connections() {
         myS3Instance = new AmazonS3Client(
                         new ClasspathPropertiesFileCredentialsProvider("AwsCredentials.properties"));
         Region usWest2 = Region.getRegion(Regions.US_WEST_2);
         myS3Instance.setRegion(usWest2);
     }
     
     /**
      * Places avatar into a bucket
      * @param username is user
      * @param filepath is path of avatar
      */
     public void putAvatarIntoBucket(String username, String filepath) {
         putFileIntoBucket(AVATAR + username, filepath);
     }
     
     /**
      * Places game thumbnail into a bucket
      * @param gameName is gamename
      * @param filepath is path of thumbnail
      */
     public void putGameThumbnailIntoBucket(String gameName, String filepath) {
         putFileIntoBucket(THUMBNAIL + gameName, filepath);
     }
     
     /**
      * Places game adscreen into a bucket
      * @param gameName is gamename
      * @param filepath is path of adscreen
      */
     public void putAdScreenIntoBucket (String gameName, String filepath) {
         putFileIntoBucket(ADSCREEN + gameName, filepath); 
     }
     
     /**
      * Retrieves avatar
      * @param username is username
      */
     public String getAvatar(String username) {
         return downloadObjectToFile(AVATAR + username);
     }
     
     /**
      * Retrieves thumbnail
      * @param gameName is game name
      */
     public String getThumbnail(String gameName) {
         return downloadObjectToFile(THUMBNAIL + gameName);
     }
     
     /**
      * Retrieves ad screen
      * @param gameName is game name
      */
     public String getAdScreen(String gameName) {
         return downloadObjectToFile(ADSCREEN + gameName);
     }
     
     /**
      * Places usergamedata into bucket
      * @param username is user name
      * @param gameName is game name
      * @param usd is user game data to be placed
      */
     public void putUserGameDataIntoBucket(String username, String gameName, UserGameData usd) {
         putFileIntoBucket(USERGAMEDATA + username + gameName, 
                           createFileFromByteArray(serializeObject(usd)));
     }
     
     /**
      * Returns given user game data
      * @param username is user name
      * @param gameName is game name
      */
     public UserGameData getUserGameDataFromBucket(String username, String gameName) {
         String tempFilePath = downloadObjectToFile(USERGAMEDATA + username + gameName);
         byte[] data = read(createFileFromFilePath(tempFilePath));
         return (UserGameData) deserialize(data);
     }
     
     /**
      * Places gamedata into bucket
      * @param gameName is game name
      * @param gd is gameData to be placed
      */
     public void putGameDataIntoBucket(String gameName, GameData gd) {
         putFileIntoBucket(GAMEDATA + gameName, createFileFromByteArray(serializeObject(gd)));
     }
     
     /**
      * Returns given GameData
      * @param gameName is game name
      */
     public GameData getGameDataFromBucket(String gameName) {
         String tempFilePath = downloadObjectToFile(GAMEDATA + gameName);
         byte[] data = read(createFileFromFilePath(tempFilePath));
         return (GameData) deserialize(data);
     }
     
     
     /**
      * Returns given GameData
      * @param filepath is the filepath to create file from
      */
     public File createFileFromFilePath(String filepath) {
         return new File(filepath);
     }
     
     /**
      * Reads a file into a byte array
      * @param file is the file
      */
     public byte[] read(File file) {
         byte [] buffer = new byte[(int) file.length()];
         InputStream ios = null;
         try {
             ios = new FileInputStream(file);     
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         finally { 
             try {
                 if (ios != null) {
                     ios.close();
                 }
             } 
             catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return buffer;
     }
 
     /**
      * Creates file from byte array
      * @param bytes is array of bytes
      */
     public String createFileFromByteArray(byte[] bytes) {
         FileOutputStream out;
         try {
             out = new FileOutputStream(USER_DIR + SRC + RELATIVE_PATH + FILENAME);
             out.write(bytes);
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         return USER_DIR + SRC + RELATIVE_PATH + FILENAME;
     }
     
     /**
      * Serializes an object
      * @param obj is object to be serialized
      */
     public byte[] serializeObject(Object obj) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         ObjectOutputStream o;
         try {
             o = new ObjectOutputStream(b);
             o.writeObject(obj);
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         return b.toByteArray();
     }
     
     /**
      * Deserializes an object
      * @param bytes are byte to be read in
      */
     public Object deserialize(byte[] bytes) {
         ByteArrayInputStream b = new ByteArrayInputStream(bytes);
         ObjectInputStream o;
         try {
             o = new ObjectInputStream(b);
             return o.readObject();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     
     /**
      * Places an object into amazon bucket
      * @param key (name) of file
      * @param filepath of file
      */
     public void putFileIntoBucket(String key, String filepath) {
         File file = new File(filepath);
         try {
             myS3Instance.putObject(new PutObjectRequest(BUCKET_NAME, key, file));
         }
         catch (AmazonServiceException ase) {
             System.out.println("Caught an AmazonServiceException, which means your request made it "
                     + "to Amazon S3, but was rejected with an error response for some reason.");
             System.out.println("Error Message:    " + ase.getMessage());
             System.out.println("HTTP Status Code: " + ase.getStatusCode());
             System.out.println("AWS Error Code:   " + ase.getErrorCode());
             System.out.println("Error Type:       " + ase.getErrorType());
             System.out.println("Request ID:       " + ase.getRequestId());
         }
         catch (AmazonClientException ace) {
             System.out.println("Caught an AmazonClientException, which means the client encountered "
                     + "a serious internal problem while trying to communicate with S3, "
                     + "such as not being able to access the network.");
             System.out.println("Error Message: " + ace.getMessage());        
         }
     }
     
     /**
      * Downloads an object to AmazonDownloads folder
      * @param key (name) of file
      */
     public String downloadObjectToFile(String key) {
        Random random = new Random();
        String pathOfImage = SRC + RELATIVE_PATH + TEMPORARY_PNG + random.nextInt() + ".png";
         File tempFile = new File(USER_DIR + pathOfImage);
         @SuppressWarnings("unused")
         ObjectMetadata object = myS3Instance.getObject(
                                             new GetObjectRequest(BUCKET_NAME, key), tempFile);
         return pathOfImage;
     }
     
     /**
      * Deletes an object from bucket
      * @param key (name) of file
      */
     public void deleteObject(String key) {
         myS3Instance.deleteObject(BUCKET_NAME, key);
     }
 }
