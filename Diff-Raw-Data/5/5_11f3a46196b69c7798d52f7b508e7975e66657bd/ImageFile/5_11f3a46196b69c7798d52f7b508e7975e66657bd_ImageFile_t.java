 package ch.softhenge.supren.exif.entity;
 
 import java.io.File;
 
 public class ImageFile {
 
 	/**First Image number if the pattern that matches the image filename doesn't contain an image number */
 	public static final int FIRST_IMAGE_NUMBER = 1;
 	
 	/**The File itself**/
 	private final File imageFile;
 	/**image Number of the original Picture, or given number if no image number exists in filename according to the filename pattern**/
 	private final String imageNumber;
 	/**True if the imageNumber is the number taken from the filename*/
 	private final boolean isImageNumberFromFileName;
 	/**File Pattern Object that matched**/
 	private final FilePattern filePattern;
 	
 	/**The exif info of the image file**/
 	private ExifFileInfo exifFileInfo;
 	/**4-character camera model. Can be either 4 char or null**/
 	private String cameraModel4ch;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param imageFile
 	 * @param imageNumber
 	 * @param isImageNumberFromFileName
 	 * @param filePattern
 	 */
 	public ImageFile(File imageFile, String imageNumber, boolean isImageNumberFromFileName, FilePattern filePattern)  {
 		this.imageFile = imageFile;
 		this.imageNumber = imageNumber;
 		this.isImageNumberFromFileName = isImageNumberFromFileName;
 		this.filePattern = filePattern;
 		
 		assert imageFile != null;
		assert Integer.valueOf(imageNumber) >= 0;
		assert Integer.valueOf(imageNumber) <= 9999;
 		assert cameraModel4ch == null || cameraModel4ch.length() == 4;
 		assert filePattern != null: "filePattern must not be null";
 	}
 
 	public ExifFileInfo getExifFileInfo() {
 		return exifFileInfo;
 	}
 
 	public void setExifFileInfo(ExifFileInfo exifFileInfo) {
 		this.exifFileInfo = exifFileInfo;
 	}
 
 	public String getCameraModel4ch() {
 		return cameraModel4ch;
 	}
 
 	public void setCameraModel4ch(String cameraModel4ch) {
 		this.cameraModel4ch = cameraModel4ch;
 	}
 
 	public File getImageFile() {
 		return imageFile;
 	}
 	
 	public String getOriginalFileName() {
 		return imageFile.getName();
 	}
 	
 	public String getNewFileName() {
		//TODO: What to return?
 		return "hihi";
 	}
 	
 	/**Return the path of the file only excluding File separator at the end*/
 	public String getFilePath() {
 		return imageFile.getAbsolutePath().substring(0, imageFile.getAbsolutePath().lastIndexOf(File.separator));
 	}
 
 	public String getImageNumber() {
 		return imageNumber;
 	}
 
 	public boolean isImageNumberFromFileName() {
 		return isImageNumberFromFileName;
 	}
 
 	public FilePattern getFilePattern() {
 		return filePattern;
 	}
 	
 }
