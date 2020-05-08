 package de.thatsich.bachelor.errorgeneration.restricted.service;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 
 import de.thatsich.bachelor.errorgeneration.api.entities.ErrorEntry;
 import de.thatsich.core.opencv.Images;
 
 
 public class ErrorStorageService
 {
 	public void store( ErrorEntry entry )
 	{
 		final List<Mat> listMat = Arrays.asList( entry.getOriginalMat(), entry.getOriginalWithErrorMat(), entry.getErrorMat() );
 		Mat mergedMat = new Mat( entry.getOriginalMat().size(), CvType.CV_8UC3 );
 		Core.merge( listMat, mergedMat );
 
 		Images.store( mergedMat, entry.getStoragePath() );
 	}
 }
