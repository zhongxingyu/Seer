 package ch.softhenge.supren.exif.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 
 import ch.softhenge.supren.exif.entity.ExifFileInfo;
 import ch.softhenge.supren.exif.entity.FilePattern;
 import ch.softhenge.supren.exif.entity.ImageFile;
 import ch.softhenge.supren.exif.file.ImageFileValidator;
 import ch.softhenge.supren.exif.file.OutFilenameGenerator;
 import ch.softhenge.supren.exif.property.UserPropertyReader;
 import ch.softhenge.supren.exif.property.UserPropertyReader.PropertyName;
 
 /**
  * Image Service is able to handle image filenames and compare them against patterns.
  * 
  * @author Werni
  *
  */
 public class ImageService {
 
 	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
 	private final static String SEPERATOR = "/";
 	
 	private final File baseDir;
 	private final UserPropertyReader userPropertyReader;
 	private final ExifService exifService;
 	private final ImageFileValidator imageFileValidator;
 	private final OutFilenameGenerator outFilenameGenerator;
 
 	/**The Map with the filename pattern as key **/
 	private Map<FilePattern, Collection<ImageFile>> mapOfImageFiles;
 	/**This is the mv command that could be used to rename files**/
 	private String mvCommand;
 	/**This is the mv undo command that could be used to undo renamed files**/
 	private String mvUndoCommand;
 	/**No mv command for those files possible.*/
 	private String mvError;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param resourceFileName
 	 * @param baseDirectory
 	 */
 	public ImageService(String resourceFileName, String baseDirectory) {
 		this.baseDir = new File(baseDirectory);
 		this.userPropertyReader = new UserPropertyReader(resourceFileName);
 		this.exifService = new ExifService();
 		this.imageFileValidator = new ImageFileValidator(userPropertyReader);
 		Map<Integer, String> outfilePatternGroupMap = userPropertyReader.getPropertyMapOfProperty(PropertyName.OutfilePatternGroup);
 		this.outFilenameGenerator = new OutFilenameGenerator(outfilePatternGroupMap);
 		resetImageFileList();
 	}
 
 
 	/**
 	 * Create necessary mv and undo commands of the files to be renamed
 	 */
 	public void createMvAndUndoCommands() {
 		createImageFilesMap();
 		StringBuilder sbmv = new StringBuilder();
 		StringBuilder sbundomv = new StringBuilder();
 		StringBuilder sberror = new StringBuilder();
 		for (Entry<FilePattern, Collection<ImageFile>> imageFilesEntry : this.mapOfImageFiles.entrySet()) {
 			for (ImageFile imageFile : imageFilesEntry.getValue()) {
 				if (imageFilesEntry.getKey().equals(FilePattern.UNKNOWN_FILE_PATTERN)) {
 					sberror.append("# ImageFile ").append(imageFile.getOriginalFileName()).append(" can't be renamed. Filepattern is unknown\n");
 				} else if (imageFile.getFilePattern().getPatternIdx() == 0) {
 					sberror.append("# ImageFile ").append(imageFile.getOriginalFileName()).append(" can't be renamed. No image number available\n");
 				} else {
 					enrichImageFileWithExifInfo(imageFile);
 					if (imageFile.isKnownCameraModel()) {
						sbmv.append("mv ").append('"').append(imageFile.getUnixFilePath()).append(SEPERATOR).append(imageFile.getOriginalFileName()).append(" ");
						sbmv.append(imageFile.getUnixFilePath()).append(SEPERATOR).append(imageFile.getNewFileName()).append('"').append("\n");
 						sbundomv.append("mv ").append('"').append(imageFile.getUnixFilePath()).append(SEPERATOR).append(imageFile.getNewFileName()).append(" ");
 						sbundomv.append(imageFile.getUnixFilePath()).append(SEPERATOR).append(imageFile.getOriginalFileName()).append('"').append("\n");
 					} else {
 						if (imageFile.getExifFileInfo() != null) {
 							sberror.append("# ImageFile ").append(imageFile.getOriginalFileName()).append(" can't be renamed. Unknown Camera type " + imageFile.getExifFileInfo().getCameraModel() + "\n");
 						} else {
 							sberror.append("# ImageFile ").append(imageFile.getOriginalFileName()).append(" can't be renamed. Unknown Camera type\n");
 						}
 					}
 				}
 			}
 		}
 		this.mvCommand = sbmv.toString();
 		this.mvUndoCommand = sbundomv.toString();
 		this.mvError = sberror.toString();
 	}
 	
 	/**
 	 * Create csv Files of image Files
 	 */
 	public String createCsvSeperatedStringOfImageFiles() {
 		createImageFilesMap();
 		StringBuilder sbCsv = new StringBuilder();
 		enrichImageFilesWithExifInfo(sbCsv);
 		return (sbCsv.toString());
 	}
 
 
 	/**
 	 * Enrich all imageFiles with Exif Infos
 	 * This might take a while, since every file is scanned. 
 	 * 
 	 * @param append
 	 */
 	public void enrichImageFilesWithExifInfo(Appendable append) {
 		for (Entry<FilePattern, Collection<ImageFile>> imageFilesEntry : this.mapOfImageFiles.entrySet()) {
 			for (ImageFile imageFile : imageFilesEntry.getValue()) {
 				enrichImageFileWithExifInfo(imageFile);
 				try {
 					append.append(imageFile.toString()).append("\n");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create a list of Image Files that are candidates to rename and save it
 	 * as a map and get it using getMapOfImageFiles.
 	 */
 	public void createImageFilesMap() {
 		if (getListOfImageFiles().isEmpty()) {
 			Collection<File> listAllImageFiles = listAllImageFilesInDir();
 			for (File file : listAllImageFiles) {
 				FilePattern filePattern = imageFileValidator.getFilePattern(file.getName());
 				ImageFile imageFile;
 				if (filePattern != null) {
 					String imageNumber = imageFileValidator.getInfilePatternImgNum(file.getName(), filePattern.getPatternIdx());
 					if (imageNumber == null) {
 						imageNumber = String.format("%04d", this.mapOfImageFiles.get(filePattern).size() + ImageFile.FIRST_IMAGE_NUMBER);
 						imageFile = new ImageFile(file, imageNumber, false, filePattern);
 					} else {
 						imageFile = new ImageFile(file, imageNumber, true, filePattern);
 					}
 				} else {
 					filePattern = FilePattern.UNKNOWN_FILE_PATTERN;
 					imageFile = new ImageFile(file, null, false, filePattern);
 				}
 				this.mapOfImageFiles.get(filePattern).add(imageFile);
 			}
 		}
 	}
 
 	
 	public Map<FilePattern, Collection<ImageFile>> getMapOfImageFiles() {
 		return mapOfImageFiles;
 	}
 	
 	/**
 	 * 
 	 * @return a List of all Image Files no mater whether they are known or not
 	 */
 	public Collection<ImageFile> getListOfImageFiles() {
 		Collection<ImageFile> resultFiles = new ArrayList<>();
 		for (Collection<ImageFile> imageFiles : this.mapOfImageFiles.values()) {
 			resultFiles.addAll(imageFiles);
 		}
 		return resultFiles;
 	}
 
 	/**
 	 * 
 	 * @return a List of all unknown Image Files
 	 */
 	public Collection<ImageFile> getListOfUnknownImageFiles() {
 		return this.mapOfImageFiles.get(FilePattern.UNKNOWN_FILE_PATTERN);
 	}
 	
 
 	public String getMvCommand() {
 		return mvCommand;
 	}
 
 
 	public String getMvUndoCommand() {
 		return mvUndoCommand;
 	}
 
 
 	public String getMvError() {
 		return mvError;
 	}
 
 
 	/**
 	 * Empties the list of Image Files
 	 */
 	public void resetImageFileList() {
 		this.mapOfImageFiles = new HashMap<FilePattern, Collection<ImageFile>>();
 		Collection<FilePattern> filePatterns = this.imageFileValidator.getFilePatterns();
 		for (FilePattern filePattern : filePatterns) {
 			this.mapOfImageFiles.put(filePattern, new ArrayList<ImageFile>());
 		}
 		this.mapOfImageFiles.put(FilePattern.UNKNOWN_FILE_PATTERN, new ArrayList<ImageFile>());
 		this.mvCommand = null;
 		this.mvUndoCommand = null;
 	}
 	
 	
 	/**
 	 * Return a Collection of all image Files in the base directory recursively
 	 * 
 	 * @return
 	 */
 	private Collection<File> listAllImageFilesInDir() {
 		long currTime = System.currentTimeMillis();
 		String [] extensions = getAllExtensions();
 		Collection<File> listFiles = FileUtils.listFiles(baseDir, extensions, true);
 		LOGGER.fine("Anz Files: " + listFiles.size() + " , took: " + (System.currentTimeMillis() - currTime) + " ms");
 		
 		return listFiles;
 	}
 	
 	/**
 	 * 
 	 * @return all extensions, also non case sensitive as an array
 	 */
 	private String[] getAllExtensions() {
         String fileExtensionList = userPropertyReader.getPropertyMapOfProperty(PropertyName.fileExtensionList).get(UserPropertyReader.INDEX_IF_EXACTLYONE); 
         String[] fileExtensionsLowC = fileExtensionList.toLowerCase().split(",");
         String[] fileExtensionsUpC = fileExtensionList.toUpperCase().split(",");
 		Set<String> fileExtSet = new HashSet<String>(Arrays.asList(fileExtensionsLowC));
 		fileExtSet.addAll(Arrays.asList(fileExtensionsUpC));
 		return fileExtSet.toArray(new String[fileExtSet.size()]);
 	}
 	
 	private void enrichImageFileWithExifInfo(ImageFile imageFile) {
 		if (imageFile.getExifFileInfo() == null) {
 			ExifFileInfo exifFileInfo = exifService.getExifInfoFromImageFile(imageFile.getImageFile());
 			if (exifFileInfo == null) return;
 			String cameraModel4ch = imageFileValidator.getCameraModel4chForCameraModel(exifFileInfo.getCameraModel());
 			imageFile.setExifFileInfo(exifFileInfo);
 			imageFile.setCameraModel4ch(cameraModel4ch);
 			if (!cameraModel4ch.equals(imageFileValidator.getUnknownCamera4ch())) {
 				imageFile.setKnownCameraModel(true);
 			}
 			if (!imageFile.getFilePattern().isUnknownPattern()) {
 				String outFileName = generateOutFileName(imageFile.getExifFileInfo().getPictureDate(), cameraModel4ch, imageFile.getImageNumber());
 				imageFile.setNewFileName(outFileName);
 			}
 		}
 	}
 	
 	private String generateOutFileName(Date pictureDate, String cameraModel4ch, String imageNumber) {
 		return outFilenameGenerator.createOutFileName(pictureDate, cameraModel4ch, imageNumber);
 	}
 	
 }
