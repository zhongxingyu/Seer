 package framework;
 
 import j3m.J3MException;
 import j3m.J3MWrapper;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import util.JSONTreeSearcher;
 import util.Util;
 
 import com.google.gson.stream.JsonWriter;
 
 
 
 public class ImageProcessor extends FileProcessor{
 	
 
 	
 	public ImageProcessor(File sourceFile, File outputFolder) {
 		super(sourceFile, outputFolder);
 	}
 
 	public void extractMetadataAndKeywords() throws J3MException, Exception{
 		File outFile = new File(getOutputFolder(),
 		getSourceFileName() + "." +
 		FrameworkProperties.getInstance().getImageMetadataFileExt());
 		J3MWrapper j3m = new J3MWrapper();
 		try {
 			j3m.extractMetaData(getSourceFile(), outFile);
 			if (!outFile.exists()){
 				throw new Exception("Could not create image metadata file : " + outFile.getAbsolutePath());
 			}
 		} catch (Exception e) {
 			throw new J3MException("Could not extract image metadata file : " + outFile.getAbsolutePath(), e);
 		}
 		File keyWordFile = new File (getOutputFolder(),
		"key_words_" + getSourceFileName() + "." + 
 		FrameworkProperties.getInstance().getImageKeywordsFileExt());
 		
 		try {
 			parseKeyWords(outFile, keyWordFile);
 		} catch (Exception e) {
			throw new Exception("Could not create image keyword file : " + keyWordFile.getAbsolutePath(), e);
 		}
 		
 	}
 	
 	/**
 	 * Uses google's gson lib to parse keywords out of metadata into a separate json file, probably badly
 	 * this all seems very iffy and un-java-ry
 	 * @param sourceFile
 	 * @param outputFile
 	 * @throws IOException
 	 */
 	public void parseKeyWords(File sourceFile, File outputFile) throws IOException{
 		List<String> exclusions = FrameworkProperties.getInstance().getImageKeywordExclussions();
 		List<String> keywordList = new ArrayList<String>();
 		
 		for (String container : FrameworkProperties.getInstance().getImageKeywordContainers()) {
 			String[] path = container.split("\\.");
 			JSONTreeSearcher jsonSearcher = new JSONTreeSearcher(sourceFile, path);
 			List<String> values = jsonSearcher.performSearch();
 			
 			for (String value : values){
 				if (value != null) {
 					String[] keywords = value.toString().split(" ");
 					for (int i = 0; i < keywords.length; i++) {
 						if (!exclusions.contains(keywords[i])) {
 							keywordList.add(keywords[i]);
 						}
 					}
 				}
 			}
 		}
 		JsonWriter jsonWriter;
 		jsonWriter = new JsonWriter(new FileWriter(outputFile));
 		jsonWriter.beginObject();
 		jsonWriter.name("keywords");
 		jsonWriter.beginArray();
 		for (String keyword : keywordList) {
 			jsonWriter.value(keyword);
 		}
 		jsonWriter.endArray();
 		jsonWriter.endObject(); // }
 		jsonWriter.close();
 	}
 	
 	public void createThumbnail() throws Exception{
 		File outFile = new File(getOutputFolder(), "thumb_" + getSourceFileName() + "." + 	
 		FrameworkProperties.getInstance().getThumbFileExt());
 		try {
 			Util.resizeImage(getSourceFile(), outFile, 
 					FrameworkProperties.getInstance().getThumbWidth(), 
 					FrameworkProperties.getInstance().getThumbHeight());
 		} catch (Exception e) {
 			throw new Exception("Thumbnail file " + outFile.getName() + " could not be created", e);
 		}
 		
 	}
 	public void toLowResolution(boolean updateSource) throws Exception{
 		File outFile = new File(getOutputFolder(),
 		"low_" + getSourceFileName() + "." + 
 		Util.getFileExtenssion(getSourceFile().getName()));
 		try {
 			Util.resizeImage(getSourceFile(), outFile, 
 					FrameworkProperties.getInstance().getImageSmallWidth(), 
 					FrameworkProperties.getInstance().getImageSmallHeight());
 			if (updateSource) {
 				setSourceFile(outFile);
 			}
 		} catch (Exception e) {
 			throw new Exception("Low res image file " + outFile.getName() + " could not be created", e);
 		}
 	}
 	public void toMediumResolution(boolean updateSource)throws Exception{
 		File outFile = new File(getOutputFolder(),
 		"med_" + getSourceFileName() + "." + 
 		Util.getFileExtenssion(getSourceFile().getName()));
 		try {
 			Util.resizeImage(getSourceFile(), outFile, 
 					FrameworkProperties.getInstance().getImageMedWidth(), 
 					FrameworkProperties.getInstance().getImageMedHeight());
 			if (updateSource) {
 				setSourceFile(outFile);
 			}
 		} catch (Exception e) {
 			throw new Exception("Medium res image file  " + outFile.getName() + " could not be created", e);
 		}
 		
 	}
 	public void toHighResolution(boolean updateSource)throws Exception{
 		File outFile = new File(getOutputFolder(),
 		"high_" + getSourceFileName() + "." + 
 		Util.getFileExtenssion(getSourceFile().getName()));
 		try {
 			Util.resizeImage(getSourceFile(), outFile, 
 					FrameworkProperties.getInstance().getImageLargeWidth(), 
 					FrameworkProperties.getInstance().getImageLargeHeight());
 			if (updateSource) {
 				setSourceFile(outFile);
 			}
 		} catch (Exception e) {
 			throw new Exception("High res image file " + outFile.getName() + " could not be created", e);
 		}
 		
 	}
 	public void toOriginalResolution(boolean updateSource)throws Exception{
 		File outFile =  new File(getOutputFolder(), getSourceFile().getName());
 		try {
 			BufferedImage image = ImageIO.read(getSourceFile());
 			String fileType = Util.getFileExtenssion(outFile.getName());
 			ImageIO.write(image, fileType, outFile);
 			if (updateSource) {
 				setSourceFile(outFile);
 			}
 		} catch (Exception e) {
 			throw new Exception("Image file " + outFile.getName() + " could not be created", e);
 		}
 		
 	}
 
 }
