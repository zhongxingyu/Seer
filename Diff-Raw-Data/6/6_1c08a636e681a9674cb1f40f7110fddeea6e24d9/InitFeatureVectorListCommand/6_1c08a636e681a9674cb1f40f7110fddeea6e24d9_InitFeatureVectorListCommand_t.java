 package de.thatsich.bachelor.javafx.business.command;
 
 import java.io.IOException;
 import java.nio.file.DirectoryIteratorException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.concurrent.Task;
 
 import org.opencv.core.MatOfFloat;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
 
 import de.thatsich.bachelor.javafx.business.model.entity.FeatureVector;
 import de.thatsich.bachelor.service.CSVService;
 import de.thatsich.core.javafx.Command;
 
 public class InitFeatureVectorListCommand extends Command<List<FeatureVector>> {
 
 	private final ObjectProperty<Path> featureVectorFolderPath = new SimpleObjectProperty<Path>();
 	
 	@Inject
 	protected InitFeatureVectorListCommand(@Assisted Path featureVectorFolderPath) {
 		this.featureVectorFolderPath.set(featureVectorFolderPath);
 	}
 
 	@Override
 	protected Task<List<FeatureVector>> createTask() {
 		return new Task<List<FeatureVector>>() {
 			@Override protected List<FeatureVector> call() throws Exception {
 				final Path folderPath = featureVectorFolderPath.get(); 
 				final List<FeatureVector> featureVectorList = new ArrayList<FeatureVector>();
 				
 				if (Files.notExists(folderPath) || !Files.isDirectory(folderPath)) Files.createDirectory(folderPath);
 				
 				final String GLOB_PATTERN = "*.{csv}";
 				
 				try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, GLOB_PATTERN)) {
 					for (Path child : stream) {
 						final String fileName = child.getFileName().toString();
 						final String[] fileNameSplit = fileName.split("_");
 						
 						if (fileNameSplit.length != 4) throw new WrongNumberArgsException("Expected 4 encoded information but found " + fileNameSplit.length);
 						List<float[]> floatValues = CSVService.read(child);

 						final String className = fileNameSplit[0];
 						final String extractorName = fileNameSplit[1];
 						final int frameSize = Integer.parseInt(fileNameSplit[2]);
 						final String id = fileNameSplit[3];
 						
 						for (float[] floatArray : floatValues) {
 							final int length = floatArray.length;
 							
							final float[] copy = Arrays.copyOfRange(floatArray, 0, length - 1);
							final MatOfFloat featureVector = new MatOfFloat(copy);
 							final MatOfFloat featureLabel = new MatOfFloat(floatArray[length - 1]);
 							featureVectorList.add(new FeatureVector(className, extractorName, frameSize, id, featureVector, featureLabel));
 						}
 						log.info("Added " + child + " with Attribute " + Files.probeContentType(child));
 					}
 				} catch (IOException | DirectoryIteratorException e) {
 					e.printStackTrace();
 				}
 				log.info("All FeatureVectors added.");
 				
 				return featureVectorList;
 			}
 		};
 	}
 
 }
