 package de.thatsich.bachelor.errorgeneration.intern.service;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 
 import com.google.inject.Singleton;
 
 import de.thatsich.bachelor.errorgeneration.api.entities.ErrorEntry;
 import de.thatsich.core.opencv.Images;
 
 
 @Singleton
 public class ErrorFactoryService
 {
 	public ErrorEntry getErrorEntryFromPath( Path filePath )
 	{
 		final String unparsedString = filePath.getFileName().toString();
		final String splitString[] = unparsedString.split( "_" );
		final String className = splitString[0];
		final String errorName = splitString[1];
 
 		final Mat encodedImage = Images.toMat( filePath );
 
 		// split channels to extract GL and Error Mat
 		final List<Mat> encodedImageChannelMats = new ArrayList<Mat>();
 		Core.split( encodedImage, encodedImageChannelMats );
 
 		final Mat originalMat = encodedImageChannelMats.get( 0 );
 		final Mat originalWithError = encodedImageChannelMats.get( 1 );
 		final Mat errorMat = encodedImageChannelMats.get( 2 );
 
 		return new ErrorEntry( filePath, originalMat, originalWithError, errorMat, className, errorName );
 	}
 
 	public ErrorEntry getErrorEntryFromMat( Path filePath, Mat originalMat, Mat originalWithErrorMat )
 	{
 		final String unparsedString = filePath.getFileName().toString();
		final String splitString[] = unparsedString.split( "_" );
		final String className = splitString[0];
		final String errorName = splitString[1];
 		
 		final Mat onlyErrorMat = new Mat();
 		Core.absdiff( originalMat, originalWithErrorMat, onlyErrorMat );
 
 		return new ErrorEntry( filePath, originalMat, originalWithErrorMat, onlyErrorMat, className, errorName );
 	}
 }
