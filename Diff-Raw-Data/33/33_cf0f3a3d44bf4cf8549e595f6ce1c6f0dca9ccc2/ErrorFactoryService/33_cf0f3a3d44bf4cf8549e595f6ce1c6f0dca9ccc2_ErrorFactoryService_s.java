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
		final String className = this.parseClassName( unparsedString );
		final String errorName = unparsedString.replace( className, "" );
 
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
		final String className = this.parseClassName( unparsedString );
		final String errorName = unparsedString.replace( className, "" );
 		
 		final Mat onlyErrorMat = new Mat();
 		Core.absdiff( originalMat, originalWithErrorMat, onlyErrorMat );
 
 		return new ErrorEntry( filePath, originalMat, originalWithErrorMat, onlyErrorMat, className, errorName );
 	}

	/**
	 * Extracts classname
	 * 
	 * @param unparsedString
	 *            String which contains the ClassName in it.
	 * 
	 * @return the name of the class
	 */
	private String parseClassName( String unparsedString )
	{
		// Defining Pattern and Matcher
		final Pattern errorClassPattern = Pattern.compile( "[a-zA-Z]+" );
		final Matcher m = errorClassPattern.matcher( unparsedString );

		// Searching first instance
		m.find();
		final String className = m.group();

		return className;
	}
 }
