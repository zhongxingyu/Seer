 /*
 * $Id: ImageManagerImpl.java,v 1.27 2007-09-12 19:36:03 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.26  2007/08/01 18:11:46  pandyas
  * Fixed image upload issue - the code was not saving the constructs in the correct folder so the image was not viewable in the edit mode.
  *
  * Also changed a few properties in camod.properties to comple the fix
  *
  * Revision 1.25  2007/08/01 18:05:02  pandyas
  * VCDE changes
  *
  * Revision 1.24  2007/07/31 12:02:22  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.23  2007/06/25 16:57:11  pandyas
  * Fixed code so staining_method will not clear when other fields are edited
  *
  * Revision 1.22  2007/04/20 17:51:27  pandyas
  * Modified to add Staining Method tree to Image submission
  *
  * Revision 1.21  2006/08/17 18:26:17  pandyas
  * Defect# 410: Externalize properties files - Code Changes to send mail method
  *
  * Revision 1.20  2006/05/24 20:25:29  georgeda
  * Fixed staining methods
  *
  * Revision 1.19  2006/05/24 19:01:24  georgeda
  * Cleaned up
  *
  * Revision 1.18  2006/05/24 16:46:14  pandyas
  * Converted StainingMethod to lookup - modified code to pull dropdown list from DB
  *
  * Revision 1.17  2006/04/17 19:11:05  pandyas
  * caMod 2.1 OM changes
  *
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Image;
 import gov.nih.nci.camod.domain.StainingMethod;
 import gov.nih.nci.camod.service.ImageManager;
 import gov.nih.nci.camod.util.FtpUtil;
 import gov.nih.nci.camod.util.RandomGUID;
 import gov.nih.nci.camod.webapp.form.ImageData;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.struts.upload.FormFile;
 
 public class ImageManagerImpl extends BaseManager implements ImageManager {
 
 	public List getAll() throws Exception {
 		log.trace("In ImageManagerImpl.getAll");
 		return super.getAll(Image.class);
 	}
 
 	public Image get(String id) throws Exception {
 		log.trace("In ImageManagerImpl.get");
 		return (Image) super.get(id, Image.class);
 	}
 
 	public void save(Image Image) throws Exception {
 		log.debug("In ImageManagerImpl.save");
 		super.save(Image);
 	}
 
 	public void remove(String id, AnimalModel inAnimalModel) throws Exception {
 		log.trace("In ImageManagerImpl.remove");
 
 		inAnimalModel.getImageCollection().remove(get(id));
 		super.save(inAnimalModel);
 	}
 
 	public Image create(AnimalModel inAnimalModel, ImageData inImageData,
 			String inPath, String inStorageDirKey) throws Exception {
 
 		log.debug("Entering ImageManagerImpl.create");
 
 		Image inImage = new Image();
 		populateImage(inAnimalModel, inImageData, inImage, inPath,
 				inStorageDirKey);
 
 		log.debug("Exiting ImageManagerImpl.create");
 
 		return inImage;
 	}
 
 	public void update(AnimalModel inAnimalModel, ImageData inImageData,
 			Image inImage, String inPath, String inStorageDirKey)
 			throws Exception {
 
 		log.trace("Entering ImageManagerImpl.update");
 		log.debug("Updating ImageForm: " + inImage.getId());
 
 		// Populate w/ the new values and save
 		populateImage(inAnimalModel, inImageData, inImage, inPath,
 				inStorageDirKey);
 		save(inImage);
 
 		log.trace("Exiting ImageManagerImpl.update");
 	}
 
 	private void populateImage(AnimalModel inAnimalModel,
 			ImageData inImageData, Image inImage, String inPath,
 			String inStorageDirKey) throws Exception {
 
 		log.debug("Entering populateImage");
 		if (inImageData.getStainingMethodCode() != null
 				&& inImageData.getStainingMethodCode().length() >0) {
 
             // every submission - lookup the StainingMethod or create one new
 			StainingMethod stainingMethod = StainingMethodManagerSingleton
 					.instance().getOrCreate(inImageData.getStainingMethodCode(),
 							inImageData.getStainingMethodName());
             log.debug("populateImage inImageData.getStainingMethodCode(): " +inImageData.getStainingMethodCode());
             log.debug("populateImage inImageData.getStainingMethodName(): " +inImageData.getStainingMethodName());            
             inImage.setStainingMethod(stainingMethod);
 
 		} else {
 			// null staining method - covers editing
 			inImage.setStainingMethod(null);
 		}
 
 
 		if (inImage != null) {
 			inImage.setTitle(inImageData.getTitle());
 			inImage.setDescription(inImageData.getDescriptionOfConstruct());
 		}
 
 		// Upload Construct File location, Title of Construct, Description of
 		// Construct
 		// Check for exisiting Image for this Image
 		if (inImageData.getFileLocation() != null) {
 
 			log.debug("<ImageManagerImpl> Uploading a file");
 
 			// If this is a new Image, upload it to the server
 			FormFile f = inImageData.getFileLocation();
 
 			// Retrieve the file type
 			String fileType = null;
 			StringTokenizer strToken = new StringTokenizer(f.getFileName(), ".");
 
 			while (strToken.hasMoreTokens()) {
 				fileType = strToken.nextToken();
 				System.out.println("Token=" + fileType);
 			}
 
 			log.debug("<ImageManagerImpl> fileType is: " + fileType
 					+ " FileName is: " + f.getFileName() + " Type is: "
 					+ f.getContentType());
 
 			// Check the file type
 			if (fileType != null) {
 				// Supported file types. Sid is only supported for the normal
 				// image upload
 				if (fileType.toLowerCase().equals("jpg")
 						|| fileType.toLowerCase().equals("jpeg")
 						|| fileType.toLowerCase().equals("gif")
 						|| (fileType.toLowerCase().equals("sid") && !inStorageDirKey
 								.equals(Constants.CaImage.FTPGENCONSTORAGEDIRECTORY))
 						|| fileType.toLowerCase().equals("png")) {
 					InputStream in = null;
 					OutputStream out = null;
 
 					try {
 						// Get an input stream on the form file
 						in = f.getInputStream();
 
 						// Create an output stream to a file
 						// this file is stored on the jboss server
 						// TODO: Set a max size for this file
 						out = new BufferedOutputStream(new FileOutputStream(
 								inPath));
 
 						byte[] buffer = new byte[512];
 						while (in.read(buffer) != -1) {
 							out.write(buffer);
 						}
 					} finally {
 						if (out != null)
 							out.close();
 						if (in != null)
 							in.close();
 					}
 
 					String theFilename = inPath;
 					File uploadFile = new File(theFilename);
 
 					// TODO: Add ability to delete images from caIMAGE Ftp,
 					// requires more advanced FTPUtil
 
 					// Generate a random filename
 					RandomGUID theGUID = new RandomGUID();
 					String uniqueFileName = theGUID.toString() + "." + fileType;
 
 					// Get the e-mail resource
 					Properties camodProperties = new Properties();
 					String camodPropertiesFileName = null;
 
 					camodPropertiesFileName = System
 							.getProperty("gov.nih.nci.camod.camodProperties");
 
 					try {
 
 						FileInputStream ins = new FileInputStream(
 								camodPropertiesFileName);
 						camodProperties.load(ins);
 
 					} catch (FileNotFoundException e) {
 						log
 								.error(
 										"Caught exception finding file for properties: ",
 										e);
 						e.printStackTrace();
 					} catch (IOException e) {
 						log
 								.error(
 										"Caught exception finding file for properties: ",
 										e);
 						e.printStackTrace();
 					}
 
 					// Iterate through all the reciepts in the config file
 					String ftpServer = camodProperties
 							.getProperty("caimage.ftp.server");
 					String ftpUsername = camodProperties
 							.getProperty("caimage.ftp.username");
 					String ftpPassword = camodProperties
 							.getProperty("caimage.ftp.password");
 
                     // Determine which path to use to store the image AND              
 					// Determine which path to store or use to view in 
                     String ftpStorageDirectory = "";
 					String serverViewUrl = "";
 					if (inStorageDirKey
 							.equals(Constants.CaImage.FTPGENCONSTORAGEDIRECTORY)) {
 						serverViewUrl = camodProperties
 								.getProperty("caimage.genconview.uri");
                         ftpStorageDirectory = camodProperties
                         .getProperty("caimage.ftp.genconstoragedirectory");                         
 					} else {
 						serverViewUrl = camodProperties
 								.getProperty("caimage.modelview.uri");
                         ftpStorageDirectory = camodProperties
                         .getProperty("caimage.ftp.modelstoragedirectory");                        
 					}
 
 					// Upload the file to caIMAGE
 					FtpUtil ftpUtil = new FtpUtil();
 					ftpUtil.upload(ftpServer, ftpUsername, ftpPassword,
 							ftpStorageDirectory + uniqueFileName, uploadFile);
 
 					log.error("File upload successful.  File name: "
 							+ uniqueFileName);
 
 					inImage.setTitle(inImageData.getTitle());
 
 					inImage.setDescription(inImageData
 							.getDescriptionOfConstruct());
 
 					// Do something fancy for the sid images
 					if (fileType.equals("sid")) {
 						String theType = "";
 						if (inStorageDirKey
 								.equals(Constants.CaImage.FTPGENCONSTORAGEDIRECTORY)) {
 							theType = camodProperties
 									.getProperty("caimage.gencon");
 						} else {
 							theType = camodProperties
 									.getProperty("caimage.model");
 						}
 
 						String sidThumbView = camodProperties
 								.getProperty("caimage.sidthumbview.uri");
 						String theThumbUrl = sidThumbView + theType
 								+ uniqueFileName;
 						String theViewUrl = Constants.CaImage.LEGACYJSP
 								+ theType + uniqueFileName;
 						inImage.setUrl(theThumbUrl
 								+ Constants.CaImage.FILESEP + theViewUrl);
 					} else {
 						inImage.setUrl(serverViewUrl
 								+ uniqueFileName);
 					}
 				} else {
 					log.error("Unsupported file type: " + fileType);
 					throw new IllegalArgumentException("Unknown file type: "
 							+ fileType);
 				}
 			}
 		}
 
 		log.trace("Exiting populateImage");
 	}
 
 
 }
