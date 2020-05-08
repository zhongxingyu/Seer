 package com.orange.place.upload;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import me.prettyprint.cassandra.utils.TimeUUIDUtils;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.ProgressListener;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 
 import com.orange.place.api.PlaceAPIServer;
 import com.orange.place.constant.ErrorCode;
 import com.orange.place.constant.ServiceConstant;
 
 public abstract class AbstractUploadManager {
 
 	public static final Logger log = Logger.getLogger(PlaceAPIServer.class
 			.getName());
 
 	public int resultCode = ErrorCode.ERROR_SUCCESS;
 	private String fileType = null;
 	private String filePath = null;
 	private long fileSize = 0;
 	private String localFilePath = null;
 
 	public abstract int getResultCode();
 
 	protected String uploadFile(HttpServletRequest request) {
 		String filepath = null;
 		try {
 			request.setCharacterEncoding("UTF-8");
 			ServletFileUpload upload = new ServletFileUpload();
 			upload.setProgressListener(progressListener);
 			if (!ServletFileUpload.isMultipartContent(request)) {
 				log.info("<uploadFile> the request doesn't contain a multipart/form-data "
 								+ "or multipart/mixed stream, content type header is null .");
 				return null;
 			}
 			FileItemIterator iter = upload.getItemIterator(request);
 			if (iter == null) {
 				log.info("<uploadFile> the iterator is null.");
 				return null;
 			}
 			while (iter.hasNext()) {
 				FileItemStream item = iter.next();
 				String name = item.getFieldName();
 				InputStream stream = item.openStream();
 				if (item.isFormField()) {
 					log.info("<uploadFile> Form field " + name + " with value "
 							+ Streams.asString(stream) + " detected.");
 				} else {
 					log.info("<uploadFile> File field " + name
 							+ " with file name " + item.getName()
 							+ " detected.");
 					String filename = "";
 					if (item.getName() != null) {
 						filename = item.getName();
 					}
 					// Process the input stream
 					// TODO the code below could be better
 					ArrayList<Integer> byteArray = new ArrayList<Integer>();
 					int tempByte;
 					do {
 						tempByte = stream.read();
 						byteArray.add(tempByte);
 					} while (tempByte != -1);
 					stream.close();
 
 					int size = byteArray.size();
 					log.info("<uploadFile> total " + size + " bytes read");
 
 					byteArray.remove(size - 1);
 					byte[] bytes = new byte[size];
 					int i = 0;
 					for (Integer tByte : byteArray) {
 						bytes[i++] = tByte.byteValue();
 					}
 					// write to file
 					String commonName = getTimeFilePath()
 							+ "/"
 							+ TimeUUIDUtils.getUniqueTimeUUIDinMillis()
 									.toString() + "_" + filename;
					filepath = ServiceConstant.FILE_LOCAL_IMAGE_PATH
 							+ commonName;
 
 					log.info("<uploadFile> write to file=" + filepath);
 					FileOutputStream fw = new FileOutputStream(filepath);
 					fw.write(bytes);
 					fw.close();
					String httpPath = ServiceConstant.FILE_IMAGE_PATH
 							+ commonName;
 
 					setLocalFilePath(filepath);
 					setFilePath(httpPath);
 					setFileSize(size);
 					setFileType(item.getContentType());
 				}
 			}
 		} catch (Exception e) {
 			resultCode = ErrorCode.ERROR_UPLOAD_FILE;
 			log.info("<uploadFile> filepaht=" + filepath
 					+ ", but catch exception=" + e.toString());
 			return null;
 		}
 		return filepath;
 	}
 
 	public String getLocalFilePath() {
 		return localFilePath;
 	}
 
 	public void setLocalFilePath(String localFilePath) {
 		this.localFilePath = localFilePath;
 	}
 
 	private String getTimeFilePath() {
 		Date now = new Date();
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
 		String datePath = formatter.format(now);
		String pathString = ServiceConstant.FILE_LOCAL_IMAGE_PATH + datePath;
 		File path = new File(pathString);
 		if (!path.exists()) {
 			path.mkdirs();
 		}
 		return datePath;
 	}
 
 	// Create a progress listener
 	ProgressListener progressListener = new ProgressListener() {
 		public void update(long pBytesRead, long pContentLength, int pItems) {
 			System.out.println("We are currently reading item " + pItems);
 			if (pContentLength == -1) {
 				System.out.println("So far, " + pBytesRead
 						+ " bytes have been read.");
 			} else {
 				System.out.println("So far, " + pBytesRead + " of "
 						+ pContentLength + " bytes have been read.");
 			}
 		}
 	};
 
 	public String getFileType() {
 		return fileType;
 	}
 
 	public void setFileType(String fileType) {
 		this.fileType = fileType;
 	}
 
 	public String getFilePath() {
 		return filePath;
 	}
 
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 	public long getFileSize() {
 		return fileSize;
 	}
 
 	public void setFileSize(long fileSize) {
 		this.fileSize = fileSize;
 	}
 
 }
