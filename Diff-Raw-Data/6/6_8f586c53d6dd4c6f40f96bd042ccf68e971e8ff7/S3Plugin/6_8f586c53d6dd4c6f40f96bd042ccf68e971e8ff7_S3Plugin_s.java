 package plugin.s3;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3Client;
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 import play.exceptions.ConfigurationException;
 import plugin.s3.model.S3Blob;
 import plugin.s3.model.impl.S3RealBlob;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class S3Plugin extends PlayPlugin {
 
     private static final String MODE_MOCK = "mock";
     private static final String MODE_REAL = "real";
 
     @Override
     public void onApplicationStart() {
 
         if (StringUtils.equals(Play.configuration.getProperty("plugin.s3"), MODE_MOCK)) {
             S3Blob.mode = S3Blob.Mode.MOCK;
 
         } else if (StringUtils.equals(Play.configuration.getProperty("plugin.s3"), MODE_REAL)) {
 
             S3Blob.mode = S3Blob.Mode.REAL;
 
            S3RealBlob.s3Bucket = Play.configuration.getProperty("s3.plugin.real.bucket");
            String accessKey = Play.configuration.getProperty("s3.plugin.real.aws.access.key");
            String secretKey = Play.configuration.getProperty("s3.plugin.real.aws.secret.key");
 
             if (StringUtils.isBlank(S3RealBlob.s3Bucket) || StringUtils.isBlank(accessKey) ||
                     StringUtils.isBlank(secretKey)) {
                 throw new ConfigurationException("Real mode in s3 plugin requires the following keys : " +
                         "plugin.s3.real.bucket, plugin.s3.real.aws.access.key, plugin.s3.real.aws.secret.key");
             }
 
             AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
             S3RealBlob.s3Client = new AmazonS3Client(awsCredentials);
 
             if (!S3RealBlob.s3Client.doesBucketExist(S3RealBlob.s3Bucket)) {
                 S3RealBlob.s3Client.createBucket(S3RealBlob.s3Bucket);
             }
 
         } else {
             Logger.trace("S3 Plugin : No configuration specified, the plugin will be ignored.");
         }
     }
 }
