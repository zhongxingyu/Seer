 package es.eucm.eadventure.editor.control.controllers.animation;
 
 import java.awt.Component;
 
 import es.eucm.eadventure.common.data.animation.Animation;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.gui.assetchooser.AssetChooser;
 
 public class AnimationDataControl {
 
 	private Animation animation;
 	
 	private String filename;
 	
 	
 	public String getFilename() {
 		return filename;
 	}
 
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 
 	public AnimationDataControl(Animation animation) {
 		this.animation = animation;
 	}
 
 	public Animation getAnimation() {
 		return animation;
 	}
 
 	public void setAnimation(Animation animation) {
 		this.animation = animation;
 	}
 	
 	
 	
 	public String editAssetPath(Component window){
 		String selectedAsset = null;
 
 		AssetChooser chooser = AssetsController.getAssetChooser( AssetsController.CATEGORY_IMAGE, AssetsController.FILTER_NONE );
 		int option = chooser.showAssetChooser( window );
 		//In case the asset was selected from the zip file
 		if( option == AssetChooser.ASSET_FROM_ZIP ) {
 			selectedAsset = chooser.getSelectedAsset( );
			selectedAsset = AssetsController.getCategoryFolder(AssetsController.CATEGORY_IMAGE) + "/" + selectedAsset;
 		}
 
 		//In case the asset was not in the zip file: first add it
 		else if( option == AssetChooser.ASSET_FROM_OUTSIDE ) {
 			boolean added = AssetsController.addSingleAsset( AssetsController.CATEGORY_ANIMATION_IMAGE, chooser.getSelectedFile( ).getAbsolutePath( ) );
 			
 			if( added ) {
 				selectedAsset = chooser.getSelectedFile( ).getName( );
				selectedAsset = AssetsController.getCategoryFolder(AssetsController.CATEGORY_ANIMATION_IMAGE) + "/" + selectedAsset;
 			}
 		}
 		
 		return selectedAsset;
 /*
 		// If a file was selected
 		if( selectedAsset != null ) {
 			// Take the index of the selected asset
 			String[] assetFilenames = AssetsController.getAssetFilenames( AssetsController.CATEGORY_ANIMATION );
 			String[] assetPaths = AssetsController.getAssetsList( AssetsController.CATEGORY_ANIMATION);
 			int assetIndex = -1;
 			for( int i = 0; i < assetFilenames.length; i++ )
 				if( assetFilenames[i].equals( selectedAsset ) )
 					assetIndex = i;
 
 			Controller.getInstance().dataModified( );
 			return assetPaths[assetIndex];
 		}else
 			return null;
 */
 	}
 
 	
 }
