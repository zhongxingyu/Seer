 package util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.amazonaws.services.s3.model.ListObjectsRequest;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 
 public class S3Util {
 
 	private AmazonS3 s3;
 	private String bucketName = "Zork";
 
 	/**
 	 * Init S3
 	 */
 	public S3Util() {
 		try {
 			s3 = new AmazonS3Client(new PropertiesCredentials(
 					new FileInputStream("AwsCredentials.properties")));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Upload the input file to S3 bucket
 	 * 
 	 * @param file
 	 *            : the file you want to upload
 	 * @param name
 	 *            : File name
 	 * 
 	 * 
 	 * @return if the upload is success, return true
 	 */
 	public boolean S3MapUpload(File file, String name, String userName,
 			boolean ifPublic) {
 
 		System.out.println("Uploading a new object to S3 from a file\n");
 
 		if (ifPublic) {
 			PutObjectRequest por = new PutObjectRequest(bucketName,
 					"maps/public/" + name, file);
 
 			por.setCannedAcl(CannedAccessControlList.PublicRead);
 			s3.putObject(por);
 
 		}
 
 		PutObjectRequest por = new PutObjectRequest(bucketName, "maps/"
 				+ userName + "/" + name, file);
 
 		por.setCannedAcl(CannedAccessControlList.PublicRead);
 		s3.putObject(por);
 
 		return true;
 	}
 
 	/**
 	 * get the maps
 	 * 
 	 * @return List<String> list of name of maps
 	 * 
 	 */
 	public List<String> getMapsList(String userName) {
 		List<String> maps = new ArrayList<String>();
 		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
 				.withBucketName(bucketName).withPrefix("maps/public/")
 				.withDelimiter("/");
 		ObjectListing objects = s3.listObjects(listObjectsRequest);
 
 		for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
 			if (!objectSummary.getKey().equals("maps/public")) {
 				System.out.println("get map: " + objectSummary.getKey());
 				maps.add(objectSummary.getKey());
 			}
 		}
 
 		ListObjectsRequest listObjectsRequest2 = new ListObjectsRequest()
 				.withBucketName(bucketName)
 				.withPrefix("maps/" + userName + "/").withDelimiter("/");
		ObjectListing objects2 = s3.listObjects(listObjectsRequest2);
 
		for (S3ObjectSummary objectSummary : objects2.getObjectSummaries()) {
 			if (!objectSummary.getKey().equals("maps/" + userName)) {
 				System.out.println("get map: " + objectSummary.getKey());
 				maps.add(objectSummary.getKey());
 			}
 		}
 
 		return maps;
 	}
 
 	/**
 	 * get a single map
 	 * 
 	 * @param mapName
 	 */
 	public void getMap(String mapName) {
 		s3.getObject(new GetObjectRequest(bucketName, mapName), new File(
 				mapName));
 	}
 }
