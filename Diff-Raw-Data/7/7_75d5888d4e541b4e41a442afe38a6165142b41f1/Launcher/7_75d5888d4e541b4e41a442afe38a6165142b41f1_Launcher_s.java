 package org.lastbamboo.common.amazon.s3;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Accepts arguments to create s3 buckets, upload files, etc.
  */
 public class Launcher
     {
 
     /**
      * Called from the command line.
      * 
      * @param args The command line arguments.
      */
     public static void main (final String[] args)
         {
         if (args.length < 4)
             {
             System.out.println("Usage: run.sh $AWS_ACCESS_KEY_ID $AWS_ACCESS_KEY bucketName filePath");
             return;
             }
         final AmazonS3 s3 = new AmazonS3Impl(args[0], args[1]);
         try
             {
            s3.createBucket(args[2]);
             }
         catch (final IOException e)
             {
             System.out.println("Could not create bucket.  Already exists?");
             e.printStackTrace();
             }
         final String fileString = args[3];
         final File file = new File(fileString);
         if (!file.isFile())
             {
             System.out.println("File not found: "+fileString);
             return;
             }
         try
             {
            s3.putPublicFile(args[3], file);
             }
         catch (IOException e)
             {
             System.out.println("Could not upload file.  Error was: ");
             e.printStackTrace();
             }
         }
     }
