 package de.d2dev.heroquest.editor;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.system.JmeSystem;
 
 import de.d2dev.fourseasons.Application;
 import de.d2dev.fourseasons.resource.JmeAssetLocatorAdapter;
 import de.d2dev.fourseasons.resource.TFileResourceFinder;
 import de.d2dev.fourseasons.resource.util.TFileLocator;
 import de.d2dev.heroquest.engine.files.HqRessourceFile;
 import de.schlichtherle.truezip.file.TFile;
 
 /**
  * This class provides access to the resources used by our editor -
  * also used by our tests!
  * @author Sebastian Bordt
  *
  */
 public class EditorResources {
 	
 	public String publicDataStoragePath;
 	
 	public TFile assetsFolder;
 
 	public TFile textureFolder;
 	public TFile soundFolder;	
 	
 	public HqRessourceFile globalRessources = null;
 	
 	public Dropbox dropbox;
 	
 	public TFileResourceFinder resourceFinder = new TFileResourceFinder();
 	public AssetManager assestManager = JmeSystem.newAssetManager();
 	
 	public EditorResources() throws Exception {		
 		// get our public data storage location
     	this.publicDataStoragePath = Application.getPublicStoragePath("HeroQuest Editor");
     	
     	// assets folder 'assets'
     	this.assetsFolder = new TFile( this.publicDataStoragePath + "/assets" ); 
     	
    	if ( this.assetsFolder.exists() ) {
    		this.assestManager.registerLocator( this.assetsFolder.getAbsolutePath(), TFileLocator.class );
    	}
     	
     	// textures folder 'textures'
     	this.textureFolder = new TFile( this.publicDataStoragePath + "/textures" );
     	
     	if ( this.textureFolder.exists() ) {
     		this.resourceFinder.textureLocations.add( this.textureFolder );
     	}
     	
     	// sound folder 'sounds'
     	this.soundFolder = new TFile( this.publicDataStoragePath + "/sounds" );
     	
     	if ( this.soundFolder.exists() ) {
     		this.resourceFinder.audioLocations.add( this.soundFolder );
     	}
     	
     	// global resources are in globalResources.zip
     	if ( new TFile( this.publicDataStoragePath + "/" + "globalResources.zip" ).exists() ) {
         	this.globalRessources = new HqRessourceFile( this.publicDataStoragePath + "/" + "globalResources.zip" );
         	
         	this.globalRessources.addAsResourceLocation( resourceFinder );
     	}
     	
     	// Dropbox setup
     	this.dropbox = new Dropbox();
     	
     	this.dropbox.addAsResourceLocation( this.resourceFinder );
     	this.dropbox.addAsResourceLocation( this.assestManager );
     	
 		// make the asset manager use our resource finders resources
 		JmeAssetLocatorAdapter.locators.put( "EditorFinder", this.resourceFinder );
 		
 		this.assestManager.registerLocator( "EditorFinder", JmeAssetLocatorAdapter.class  );
 	}
 }
