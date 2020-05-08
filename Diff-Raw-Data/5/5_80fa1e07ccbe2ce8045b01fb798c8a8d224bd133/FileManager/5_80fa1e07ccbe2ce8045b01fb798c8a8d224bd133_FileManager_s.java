 package org.netvogue.server.aws.core;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.imageio.ImageIO;
 
 import org.apache.commons.codec.binary.Base64;
 import org.netvogue.server.aws.core.Scalr.Method;
 import org.netvogue.server.aws.core.Scalr.Mode;
 import org.netvogue.server.common.ResultStatus;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.DeleteObjectsRequest;
 import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
 import com.amazonaws.services.s3.model.DeleteObjectsResult;
 import com.amazonaws.services.s3.model.ListObjectsRequest;
 import com.amazonaws.services.s3.model.MultiObjectDeleteException;
 import com.amazonaws.services.s3.model.MultiObjectDeleteException.DeleteError;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.ProgressEvent;
 import com.amazonaws.services.s3.model.ProgressListener;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 import com.amazonaws.services.s3.transfer.TransferManager;
 import com.amazonaws.services.s3.transfer.Upload;
 
 public class FileManager extends TransferManager {
 	
 	private FileManager(AWSCredentials credentials) {
 		super(credentials);
 	}
 
 	private static String accesskey = "AKIAJPHEXCL7WIP7ITSQ";
 	private static FileManager transferManager;
 	private static SecretKeySpec signingKey = null;
 	private static Mac mac = null;
 	private static String secureKey = "yWbD67M+VidV+4G/6oMdfiSzg0ouVo2kD58+9yqV";
 	
 	public static FileManager getSharedInstance() throws Exception {
 		
 		if ( transferManager == null ) {
 			//				PropertiesCredentials credentials = new PropertiesCredentials(FileManager.class.getResourceAsStream("AwsCredentials.properties"));
 			AWSCredentials credentials = new BasicAWSCredentials(accesskey,secureKey);
 			transferManager = new FileManager(credentials);
 			setKey(secureKey);
 			
 		}
 		
 		return transferManager;
 	}
 	
 	// This method converts AWSSecretKey into crypto instance.
 	public static void setKey(String AWSSecretKey) throws Exception {
 		  mac = Mac.getInstance("HmacSHA1");
 		  byte[] keyBytes = AWSSecretKey.getBytes("UTF8");
 		  signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
 		  mac.init(signingKey);
 	}
 
 	// This method creates S3 signature for a given String.
 	private String sign(String data) throws Exception
 	{
 		// Signed String must be BASE64 encoded.
 		byte[] signBytes = mac.doFinal(data.getBytes("UTF8"));
 		String signBytesString = new String(signBytes);
 		
 		//byte[] signature = org.springframework.security.crypto.codec.Base64.encode(signBytes);
 		byte[] signature = Base64.encodeBase64(signBytes);
 		String sig = new String(signature);
 		return sig;
 	}
 	
 	public String getQueryString(String bucket,String key) {
         QueryStringAuthGenerator generator =
                 new QueryStringAuthGenerator(accesskey, secureKey, true, Utils.DEFAULT_HOST, CallingFormat.getPathCallingFormat());
 
         return generator.get(bucket, key, null);
 	}
 
 
 	public String signForGet(String link) {	
 		String signature = null;
 		
 		// S3 timestamp pattern.
 		String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
 		SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
 		df.setTimeZone(TimeZone.getTimeZone("GMT"));
 		  
 		String method = "GET";
 		String contentMD5 = "";
 		String contentType = "";
 		//String date = df.format(new Date()) + "GMT";
 		Date date = new Date();
 		Long time = date.getTime();
 		time += 20 * 60 * 1000;
 		time = time/1000;
 		String expires = time.toString();
 
 		// Generate signature
 		StringBuffer buf = new StringBuffer();
 		buf.append(method).append("\n");
 		buf.append(contentMD5).append("\n");
 		buf.append(contentType).append("\n");
 		buf.append(expires).append("\n");
 		buf.append(link);
 		try {
 			signature = sign(buf.toString());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		StringBuffer reqAuthenticationString = new StringBuffer();
 		//reqAuthenticationString.append(method).append(" ");
 		reqAuthenticationString.append(link).append("?");
 		reqAuthenticationString.append("AWSAccessKeyId=").append(accesskey);
 		reqAuthenticationString.append("&Signature=").append(signature);
 		reqAuthenticationString.append("&Expires=").append(expires);
 		return reqAuthenticationString.toString();
 	}
 	
 	public ObjectMetadata getMetaData(MultipartFile file) {
         ObjectMetadata metaData = new ObjectMetadata();
         metaData.addUserMetadata("fileName", file.getOriginalFilename());
         metaData.setContentType(file.getContentType());
         metaData.setContentLength(file.getSize());
         return metaData;
 	}
 	
 	public Upload upload(String bucketName,String fileName,byte[] buffer, ObjectMetadata metadata,
 								ImageType imageType, String username) {
 		
 		String imageKey = username + "/" + imageType.getKey() + "/" + fileName;
		Size[] sizes = imageType.getSizes();
 		
 		
 		try {
 			boolean firstImage = true;
 			for ( Size size : sizes ) {
 				Upload temp = upload(bucketName,imageKey+ "-" + size.toString(), buffer, metadata, size);
 				if(firstImage) {
 					temp.waitForCompletion();
 					//temp.waitForUploadResult();
 					firstImage = false;
 					break;
 				}
 //				temp.waitForUploadResult();
 				System.out.println("input" + buffer + " : "+ temp.getDescription());
 			}
 		} catch (AmazonServiceException e) {
 			System.out.println("Amazon service exception:" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		} catch (AmazonClientException e) {
 			System.out.println("Amazon client exception:" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			System.out.println("Illegal argument exception" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("There was an error in Upload manager while uploading different sizes" +  
 													" - " + e.toString());
 			e.printStackTrace();
		}
 		Upload upload = null;
 		try {
 			upload = upload(bucketName, imageKey, buffer, metadata);
 			upload.waitForCompletion();
 			System.out.println("input" + buffer.toString() + " : " + upload.getDescription());
 		} catch (Exception e) {
 			System.out.println("There was an error in Upload manager while uploading original image" +  
 					" - " + e.toString());
 		}
 		return upload;
 	}
 	
 	public Upload upload(String bucketName,String key,byte[] input,ObjectMetadata metaData,Size size) {
 
 		try {
 			BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(input));
 			BufferedImage ResizedImage 	= Scalr.resize(originalImage, Method.ULTRA_QUALITY, Mode.FIT_TO_WIDTH, size.getWidth(), size.getHeight());
 			//ResizedImage 	= Scalr.pad(ResizedImage, size.getWidth(), size.getHeight(), Color.WHITE);
 			String fileExtension = metaData.getUserMetadata().get("fileName");
 			fileExtension = fileExtension.substring(fileExtension.lastIndexOf(".")+1);
 			System.out.println("fileExtension: " + fileExtension);
 			ByteArrayOutputStream output = new ByteArrayOutputStream();
 			ImageIO.write(ResizedImage,fileExtension,output);
 			System.out.println("Size of buffer is " + output.size());
 			return upload(bucketName, key, output.toByteArray(), metaData);
 		} catch (AmazonServiceException e) {
 			System.out.println("Amazon service exception:" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		} catch (AmazonClientException e) {
 			System.out.println("Amazon client exception:" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			System.out.println("Illegal argument exception" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("There was an error in Upload manager main API" +  
 					" - " + e.toString());
 			e.printStackTrace();
 		}  
 		
 		return null;
 	}
 	
 	public Upload upload(String bucketName,String key,byte[] bytes) {
 		System.out.println("bucketName: " + bucketName);
 		Upload upload = upload(bucketName, key, bytes, new ObjectMetadata());
 		return upload;
 	}
 
 	public Upload upload(String bucketName,String key,byte[] bytes,ObjectMetadata metadata) {
 		ByteArrayInputStream input = new ByteArrayInputStream(bytes);
 		metadata.setContentLength(bytes.length);
 		Upload upload = upload(bucketName, key, input, metadata);
 		System.out.println("is Upload done:" + upload.isDone());
 		System.out.println("size of this transfer:" + bytes.length);
 		System.out.println("state of this transfer:" + upload.getState());
 		System.out.println("progress of this transfer:" + upload.getProgress());
 		ProgressListener listener = new ProgressListener() {
 			
 			@Override
 			public void progressChanged(ProgressEvent progressEvent) {
 				if(ProgressEvent.COMPLETED_EVENT_CODE == progressEvent.getEventCode()) {
 					System.out.println("Transfer is successfull" + progressEvent.getBytesTransfered());
 				} else if(ProgressEvent.FAILED_EVENT_CODE == progressEvent.getEventCode()) {
 					System.out.println("Transfer is failed" + progressEvent.getBytesTransfered());
 				}
 			}
 		};
 		upload.addProgressListener(listener);
 		return upload;
 	}
 
 	public Upload upload(String bucketName,String key,InputStream input) {
 		Upload upload = upload(bucketName, key, input, new ObjectMetadata());
 		return upload;
 	}
 	
 	public ResultStatus deletePhotosById(String bucketName, String photoId) {
 		AmazonS3 s3Client = null;
 		AWSCredentials credentials = null;
 		ObjectListing objectListing = null;
 		ListObjectsRequest listObjectsRequest = null;
 		List<KeyVersion> keys = null;
 		try {
 			System.out.println("photoId in transfer manager: " + photoId);
 			credentials = new BasicAWSCredentials(accesskey, secureKey);
 			s3Client = new AmazonS3Client(credentials);
 			keys = new ArrayList<KeyVersion>();
 			listObjectsRequest = new ListObjectsRequest().withBucketName(
 					bucketName).withPrefix(photoId);
 			String key = null;
 			do {
 				objectListing = s3Client.listObjects(listObjectsRequest);
 				for (S3ObjectSummary objectSummary : objectListing
 						.getObjectSummaries()) {
 					key = objectSummary.getKey();
 					KeyVersion keyVersion = new KeyVersion(key);
 					keys.add(keyVersion);
 					System.out.println(" - " + key + "  " + "(size = "
 							+ objectSummary.getSize() + ")");
 				}
 				listObjectsRequest.setMarker(objectListing.getNextMarker());
 			} while (objectListing.isTruncated());
 
 			// Multi-object delete by specifying only keys (no version ID).
 			DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(
 					bucketName).withQuiet(false);
 			multiObjectDeleteRequest.setKeys(keys);
 			DeleteObjectsResult delObjRes = s3Client
 					.deleteObjects(multiObjectDeleteRequest);
 			System.out.format("Successfully deleted all the %s items.\n",
 					delObjRes.getDeletedObjects().size());
 			// s3Client.deleteObject(new DeleteObjectRequest(bucketName,
 			// photoId));
 		} catch (MultiObjectDeleteException mode) {
 			for (DeleteError deleteError : mode.getErrors()) {
 				System.out.format("Object Key: %s\t%s\t%s\n",
 						deleteError.getKey(), deleteError.getCode(),
 						deleteError.getMessage());
 			}
 		} catch (AmazonServiceException ase) {
 			System.out.println("Caught an AmazonServiceException.");
 			System.out.println("Error Message:    " + ase.getMessage());
 			System.out.println("HTTP Status Code: " + ase.getStatusCode());
 			System.out.println("AWS Error Code:   " + ase.getErrorCode());
 			System.out.println("Error Type:       " + ase.getErrorType());
 			System.out.println("Request ID:       " + ase.getRequestId());
 			ase.printStackTrace();
 			return ResultStatus.FAILURE;
 		} catch (AmazonClientException ace) {
 			System.out.println("Caught an AmazonClientException.");
 			System.out.println("Error Message: " + ace.getMessage());
 			ace.printStackTrace();
 			return ResultStatus.FAILURE;
 		} catch (Exception ex) {
 			System.out.println("Caught an AmazonClientException.");
 			System.out.println("Error Message: " + ex.getMessage());
 			ex.printStackTrace();
 			return ResultStatus.FAILURE;
 		}
 		return ResultStatus.SUCCESS;
 	}
 	
 	public ResultStatus deletePhotosList(List<String> photoIdsList,
 			String bucketName, String prefixKey) {
 		AmazonS3 s3Client = null;
 		AWSCredentials credentials = null;
 		ObjectListing objectListing = null;
 		ListObjectsRequest listObjectsRequest = null;
 		List<String> keys = null;
 		List<KeyVersion> keyVersions = null;
 		try {
 			System.out.println("photoId in transfer manager: " + prefixKey);
 			credentials = new BasicAWSCredentials(accesskey, secureKey);
 			s3Client = new AmazonS3Client(credentials);
 			keys = new ArrayList<String>();
 			listObjectsRequest = new ListObjectsRequest().withBucketName(
 					bucketName).withPrefix(prefixKey);
 			String key = null;
 			do {
 				objectListing = s3Client.listObjects(listObjectsRequest);
 				for (S3ObjectSummary objectSummary : objectListing
 						.getObjectSummaries()) {
 					key = objectSummary.getKey();
 					// KeyVersion keyVersion = new KeyVersion(key);
 					keys.add(key);
 					System.out.println(" - " + key + "  " + "(size = "
 							+ objectSummary.getSize() + ")");
 				}
 				listObjectsRequest.setMarker(objectListing.getNextMarker());
 			} while (objectListing.isTruncated());
 
 			keyVersions = new ArrayList<KeyVersion>();
 			for (Iterator<String> iterator = keys.iterator(); iterator
 					.hasNext();) {
 				String string = iterator.next();
 				System.out.println("string: " + string);
 				for (int i = 0; i < photoIdsList.size(); i++) {
 					String str = photoIdsList.get(i);
 					System.out.println("str: " + str);
 					if (string.contains(str)) {
 						System.out.println("Matching string: " + string
 								+ " matching str: " + str);
 						KeyVersion keyVersion = new KeyVersion(string);
 						keyVersions.add(keyVersion);
 						break;
 					} else {
 						continue;
 					}
 				}
 			}
 			// Multi-object delete by specifying only keys (no version ID).
 			DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(
 					bucketName).withQuiet(false);
 			multiObjectDeleteRequest.setKeys(keyVersions);
 			DeleteObjectsResult delObjRes = s3Client
 					.deleteObjects(multiObjectDeleteRequest);
 			System.out.format("Successfully deleted all the %s items.\n",
 					delObjRes.getDeletedObjects().size());
 			// s3Client.deleteObject(new DeleteObjectRequest(bucketName,
 			// photoId));
 		} catch (MultiObjectDeleteException mode) {
 			for (DeleteError deleteError : mode.getErrors()) {
 				System.out.format("Object Key: %s\t%s\t%s\n",
 						deleteError.getKey(), deleteError.getCode(),
 						deleteError.getMessage());
 			}
 		} catch (AmazonServiceException ase) {
 			System.out.println("Caught an AmazonServiceException.");
 			System.out.println("Error Message:    " + ase.getMessage());
 			System.out.println("HTTP Status Code: " + ase.getStatusCode());
 			System.out.println("AWS Error Code:   " + ase.getErrorCode());
 			System.out.println("Error Type:       " + ase.getErrorType());
 			System.out.println("Request ID:       " + ase.getRequestId());
 			ase.printStackTrace();
 			return ResultStatus.FAILURE;
 		} catch (AmazonClientException ace) {
 			System.out.println("Caught an AmazonClientException.");
 			System.out.println("Error Message: " + ace.getMessage());
 			ace.printStackTrace();
 			return ResultStatus.FAILURE;
 		} catch (Exception ex) {
 			System.out.println("Caught an AmazonClientException.");
 			System.out.println("Error Message: " + ex.getMessage());
 			ex.printStackTrace();
 			return ResultStatus.FAILURE;
 		}
 		return ResultStatus.SUCCESS;
 	}
 
 }
