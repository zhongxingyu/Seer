 package de.thatsich.bachelor.errorgeneration.restricted.controller;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.fxml.FXML;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 
 import org.opencv.core.Mat;
 import org.opencv.imgproc.Imgproc;
 
 import com.google.inject.Inject;
 
 import de.thatsich.bachelor.errorgeneration.api.core.IErrorEntries;
 import de.thatsich.bachelor.errorgeneration.api.entities.ErrorEntry;
 import de.thatsich.bachelor.errorgeneration.restricted.command.ErrorInitCommander;
 import de.thatsich.core.javafx.AFXMLPresenter;
 import de.thatsich.core.opencv.Images;
 
 
 public class ErrorDisplayPresenter extends AFXMLPresenter
 {
 
 	// Nodes
 	@FXML
 	private ImageView		nodeImageViewError;
 
 	// Injects
 	@Inject
 	private IErrorEntries	errorEntryList;
 
 	@Inject
 	ErrorInitCommander		initCommander;
 
 	@Override
 	protected void initComponents()
 	{
 		final ErrorEntry entry = this.errorEntryList.getSelectedErrorEntry();
 		if ( entry != null )
 		{
 			final Image image = this.errorEntryToImage( entry );
 			this.nodeImageViewError.imageProperty().setValue( image );
 			this.log.info( "Initialized nodeImageViewError." );
 		}
 	}
 
 	@Override
 	protected void bindComponents()
 	{
 		this.errorEntryList.getSelectedErrorEntryProperty().addListener( new ChangeListener<ErrorEntry>()
 		{
 			@Override
 			public void changed( ObservableValue<? extends ErrorEntry> observable, ErrorEntry oldValue, ErrorEntry newValue )
 			{
 				if ( newValue != null )
 				{
 					final Image image = errorEntryToImage( newValue );
 					nodeImageViewError.imageProperty().setValue( image );
 					log.info( "Selected new ErrorEntry." );
 				}
 				else
 				{
 					nodeImageViewError.imageProperty().set( null );
 					log.info( "Set Image to null." );
 				}
 			}
 		} );
 		this.log.info( "Bound nodeImageViewError to Model." );
 	}
 
 	private Image errorEntryToImage( ErrorEntry entry )
 	{
 		final Mat originalMat = entry.getOriginalMat().clone();
 		final Mat onlyErrorMat = entry.getErrorMat();
 
 		if ( !originalMat.size().equals( onlyErrorMat.size() ) ) throw new IllegalStateException( "Original Size: " + originalMat.size() + ", ErrorSize: " + onlyErrorMat.size() );
 
 		// convert originalMat into RGB
 		Imgproc.cvtColor( originalMat, originalMat, Imgproc.COLOR_GRAY2RGB );
 
 		// overwrite error pixel in red layer
 		for ( int row = 0; row < originalMat.rows(); row++ )
 		{
 			for ( int col = 0; col < originalMat.cols(); col++ )
 			{
 				final int value = ( int ) onlyErrorMat.get( row, col )[0];
 
 				// if is error pixel overwrite tuple in original mat
 				if ( value > 0 )
 				{
					final double[] buffer = { 0, 0, 255};
 					originalMat.put( row, col, buffer );
 				}
 			}
 		}
 
 		return Images.toImage( originalMat );
 	}
 }
