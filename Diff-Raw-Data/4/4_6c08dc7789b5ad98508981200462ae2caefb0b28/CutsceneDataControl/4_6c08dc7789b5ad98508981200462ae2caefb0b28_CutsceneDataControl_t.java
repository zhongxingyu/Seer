 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.control.controllers.cutscene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.auxiliar.AssetsImageDimensions;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.data.chapter.scenes.Cutscene;
 import es.eucm.eadventure.common.data.chapter.scenes.GeneralScene;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.Loader;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.DataControlWithResources;
 import es.eucm.eadventure.editor.control.controllers.EditorImageLoader;
 import es.eucm.eadventure.editor.control.controllers.EffectsController;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.controllers.general.ResourcesDataControl;
 import es.eucm.eadventure.editor.control.tools.general.ChangeNSDestinyPositionTool;
 import es.eucm.eadventure.editor.control.tools.general.assets.AddResourcesBlockTool;
 import es.eucm.eadventure.editor.control.tools.general.commontext.ChangeDocumentationTool;
 import es.eucm.eadventure.editor.control.tools.general.commontext.ChangeNameTool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeIntegerValueTool;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 public class CutsceneDataControl extends DataControlWithResources {
 
     /**
      * Contained cutscene data.
      */
     private Cutscene cutscene;
 
     /**
      * Holds the type of the cutscene.
      */
     private int cutsceneType;
 
     /**
      * Constructor.
      * 
      * @param cutscene
      *            Contained cutscene data
      */
     public CutsceneDataControl( Cutscene cutscene ) {
 
         this.cutscene = cutscene;
         this.resourcesList = cutscene.getResources( );
 
         switch( cutscene.getType( ) ) {
             case GeneralScene.SLIDESCENE:
                 cutsceneType = Controller.CUTSCENE_SLIDES;
                 break;
             case GeneralScene.VIDEOSCENE:
                 cutsceneType = Controller.CUTSCENE_VIDEO;
                 break;
         }
 
         selectedResources = 0;
 
         // Add a new resource if the list is empty
         if( resourcesList.size( ) == 0 )
             resourcesList.add( new Resources( ) );
 
         // Create the subcontrollers
         resourcesDataControlList = new ArrayList<ResourcesDataControl>( );
         for( Resources resources : resourcesList )
             resourcesDataControlList.add( new ResourcesDataControl( resources, cutsceneType ) );
     }
 
     /**
      * Returns the type of the contained cutscene.
      * 
      * @return Type of the contained cutscene
      */
     public int getType( ) {
 
         return cutsceneType;
     }
 
     /**
      * Returns the id of the contained cutscene.
      * 
      * @return If of the contained cutscene
      */
     public String getId( ) {
 
         return cutscene.getId( );
     }
 
     /**
      * Returns the documentation of the scene.
      * 
      * @return Cutscene's documentation
      */
     public String getDocumentation( ) {
 
         return cutscene.getDocumentation( );
     }
 
     /**
      * Returns the name of the cutscene.
      * 
      * @return Cutscene's name
      */
     public String getName( ) {
 
         return cutscene.getName( );
     }
 
     /**
      * Sets the new documentation of the cutscene.
      * 
      * @param documentation
      *            Documentation of the cutscene
      */
     public void setDocumentation( String documentation ) {
 
         controller.addTool( new ChangeDocumentationTool( cutscene, documentation ) );
     }
 
     /**
      * Sets the new name of the cutscene.
      * 
      * @param name
      *            Name of the cutscene
      */
     public void setName( String name ) {
 
         controller.addTool( new ChangeNameTool( cutscene, name ) );
     }
 
     public String getTargetId( ) {
 
         return cutscene.getTargetId( );
     }
 
     public void setTargetId( String targetId ) {
 
         cutscene.setTargetId( targetId );
     }
 
     @Override
     public Object getContent( ) {
 
         return cutscene;
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         //return new int[] { Controller.RESOURCES, Controller.NEXT_SCENE, Controller.END_SCENE };
         return new int[] { Controller.NEXT_SCENE, Controller.END_SCENE };
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
         boolean canAddElement = false;
 
         return canAddElement;
     }
 
     @Override
     public boolean canBeDeleted( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeMoved( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeRenamed( ) {
 
         return true;
     }
 
     @Override
     public boolean addElement( int type, String selectedScene ) {
 
         boolean elementAdded = false;
 
         // If the element is a resources block
         if( type == Controller.RESOURCES ) {
             elementAdded = Controller.getInstance( ).addTool( new AddResourcesBlockTool( resourcesList, resourcesDataControlList, cutsceneType, this ) );
         }
 
         return elementAdded;
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         boolean elementMoved = false;
 
         // If the element to move is a resources block
         if( resourcesList.contains( dataControl.getContent( ) ) ) {
             int elementIndex = resourcesList.indexOf( dataControl.getContent( ) );
 
             if( elementIndex > 0 ) {
                 resourcesList.add( elementIndex - 1, resourcesList.remove( elementIndex ) );
                 resourcesDataControlList.add( elementIndex - 1, resourcesDataControlList.remove( elementIndex ) );
                 //controller.dataModified( );
                 elementMoved = true;
             }
         }
 
         return elementMoved;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         boolean elementMoved = false;
 
         // If the element to move is a resources block
         if( resourcesList.contains( dataControl.getContent( ) ) ) {
             int elementIndex = resourcesList.indexOf( dataControl.getContent( ) );
 
             if( elementIndex < resourcesList.size( ) - 1 ) {
                 resourcesList.add( elementIndex + 1, resourcesList.remove( elementIndex ) );
                 resourcesDataControlList.add( elementIndex + 1, resourcesDataControlList.remove( elementIndex ) );
                 //controller.dataModified( );
                 elementMoved = true;
             }
         }
 
         return elementMoved;
     }
 
     @Override
     public String renameElement( String name ) {
 
         boolean elementRenamed = false;
         String oldCutsceneId = cutscene.getId( );
         String references = String.valueOf( controller.countIdentifierReferences( oldCutsceneId ) );
 
         // Ask for confirmation
         if( name != null || controller.showStrictConfirmDialog( TC.get( "Operation.RenameCutsceneTitle" ), TC.get( "Operation.RenameElementWarning", new String[] { oldCutsceneId, references } ) ) ) {
 
             // Show a dialog asking for the new cutscnee id
             String newCutsceneId = name;
             if( name == null )
                 newCutsceneId = controller.showInputDialog( TC.get( "Operation.RenameCutsceneTitle" ), TC.get( "Operation.RenameCutsceneMessage" ), oldCutsceneId );
 
             // If some value was typed and the identifiers are different
             if( newCutsceneId != null && !newCutsceneId.equals( oldCutsceneId ) && controller.isElementIdValid( newCutsceneId ) ) {
                 cutscene.setId( newCutsceneId );
                 controller.replaceIdentifierReferences( oldCutsceneId, newCutsceneId );
                 controller.getIdentifierSummary( ).deleteCutsceneId( oldCutsceneId );
                 controller.getIdentifierSummary( ).addCutsceneId( newCutsceneId );
                 //controller.dataModified( );
                 elementRenamed = true;
             }
         }
 
         if( elementRenamed )
             return oldCutsceneId;
         else
             return null;
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         // Iterate through each next scene
         EffectsController.updateVarFlagSummary( varFlagSummary, cutscene.getEffects( ) );
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.updateVarFlagSummary( varFlagSummary );
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         boolean valid = true;
 
         // Iterate through the resources
         for( int i = 0; i < resourcesDataControlList.size( ); i++ ) {
             String resourcesPath = currentPath + " >> " + TC.getElement( Controller.RESOURCES ) + " #" + ( i + 1 );
             valid &= resourcesDataControlList.get( i ).isValid( resourcesPath, incidences );
         }
 
         valid &= EffectsController.isValid( currentPath + " >> " + TC.get( "Element.Effects" ), incidences, cutscene.getEffects( ) );
 
         return valid;
     }
 
     @Override
     public int countAssetReferences( String assetPath ) {
 
         int count = 0;
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             count += resourcesDataControl.countAssetReferences( assetPath );
 
         count += EffectsController.countAssetReferences( assetPath, cutscene.getEffects( ) );
 
         return count;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.getAssetReferences( assetPaths, assetTypes );
 
         EffectsController.getAssetReferences( assetPaths, assetTypes, cutscene.getEffects( ) );
 
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.deleteAssetReferences( assetPath );
 
         EffectsController.deleteAssetReferences( assetPath, cutscene.getEffects( ) );
     }
 
     @Override
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
 
         if( cutscene.getTargetId( ) != null ) {
             if( cutscene.getTargetId( ).equals( id ) )
                 count++;
         }
 
         count += EffectsController.countIdentifierReferences( id, cutscene.getEffects( ) );
 
         return count;
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
        if( cutscene.getNext( ) == Cutscene.NEWSCENE && cutscene.getTargetId( ).equals( oldId ) )
            cutscene.setTargetId( newId );
        
         EffectsController.replaceIdentifierReferences( oldId, newId, cutscene.getEffects( ) );
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         EffectsController.deleteIdentifierReferences( id, cutscene.getEffects( ) );
         if( cutscene.getNext( ) == Cutscene.NEWSCENE && cutscene.getTargetId( ).equals( id ) )
             cutscene.setNext( Cutscene.GOBACK );
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return false;
     }
 
     @Override
     public void recursiveSearch( ) {
 
         check( this.getId( ), "ID" );
         check( this.getDocumentation( ), TC.get( "Search.Documentation" ) );
         check( this.getName( ), TC.get( "Search.Name" ) );
         check( this.getTargetId( ), TC.get( "Search.NextScene" ) );
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         return getPathFromChild( dataControl, resourcesDataControlList );
     }
 
     /**
      * Returns the path to the selected preview image.
      * 
      * @return Path to the image, null if not present
      */
     public String getPreviewImage( ) {
 
         if( cutsceneType == Controller.CUTSCENE_SLIDES ) {
 
             String previewImagePath = resourcesDataControlList.get( selectedResources ).getAssetPath( "slides" );
 
             // Add the extension of the frame
             if( previewImagePath != null && !previewImagePath.toLowerCase( ).endsWith( ".eaa" ) )
                 previewImagePath += "_01.jpg";
             else if( previewImagePath != null ) {
                 return Loader.loadAnimation( AssetsController.getInputStreamCreator( ), previewImagePath, new EditorImageLoader()).getFrame( 0 ).getUri( );
             }
 
             return previewImagePath;
         }
         else {
             return "img/icons/video.png";
         }
     }
 
     public Integer getNext( ) {
 
         return cutscene.getNext( );
     }
 
     public void setNext( Integer next ) {
 
         Controller.getInstance( ).addTool( new ChangeIntegerValueTool( cutscene, next, "getNext", "setNext" ) );
         if( cutscene.getTargetId( ) == null || cutscene.getTargetId( ).equals( "" ) ) {
             cutscene.setTargetId( Controller.getInstance( ).getIdentifierSummary( ).getGeneralSceneIds( )[0] );
         }
         else {
             boolean exists = false;
             for( int i = 0; i < Controller.getInstance( ).getIdentifierSummary( ).getGeneralSceneIds( ).length; i++ ) {
                 if( Controller.getInstance( ).getIdentifierSummary( ).getGeneralSceneIds( )[i].equals( cutscene.getTargetId( ) ) )
                     exists = true;
             }
             if( !exists )
                 cutscene.setTargetId( Controller.getInstance( ).getIdentifierSummary( ).getGeneralSceneIds( )[0] );
         }
     }
 
     public String getNextSceneId( ) {
 
         return cutscene.getTargetId( );
     }
 
     public void setNextSceneId( String targetId ) {
 
         cutscene.setTargetId( targetId );
     }
 
     public boolean hasDestinyPosition( ) {
 
         return cutscene.hasPlayerPosition( );
     }
 
     public Integer getTransitionType( ) {
 
         return cutscene.getTransitionType( );
     }
 
     public Integer getTransitionTime( ) {
 
         return cutscene.getTransitionTime( );
     }
 
     public void setTransitionTime( int value ) {
 
         Controller.getInstance( ).addTool( new ChangeIntegerValueTool( cutscene, value, "getTransitionTime", "setTransitionTime" ) );
     }
 
     public void setTransitionType( int value ) {
 
         Controller.getInstance( ).addTool( new ChangeIntegerValueTool( cutscene, value, "getTransitionType", "setTransitionType" ) );
     }
 
     /**
      * Toggles the destiny position. If the next scene has a destiny position
      * deletes it, if it doesn't have one, set initial values for it.
      */
     public void toggleDestinyPosition( ) {
 
         if( cutscene.hasPlayerPosition( ) )
             controller.addTool( new ChangeNSDestinyPositionTool( cutscene, Integer.MIN_VALUE, Integer.MIN_VALUE ) );
         else
             controller.addTool( new ChangeNSDestinyPositionTool( cutscene, AssetsImageDimensions.BACKGROUND_MAX_WIDTH/2 , AssetsImageDimensions.BACKGROUND_MAX_HEIGHT/2 ) );
     }
 
     /**
      * Returns the X coordinate of the destiny position
      * 
      * @return X coordinate of the destiny position
      */
     public int getDestinyPositionX( ) {
 
         return cutscene.getPositionX( );
     }
 
     /**
      * Returns the Y coordinate of the destiny position
      * 
      * @return Y coordinate of the destiny position
      */
     public int getDestinyPositionY( ) {
 
         return cutscene.getPositionY( );
     }
 
     /**
      * Sets the new destiny position of the next scene.
      * 
      * @param positionX
      *            X coordinate of the destiny position
      * @param positionY
      *            Y coordinate of the destiny position
      */
     public void setDestinyPosition( int positionX, int positionY ) {
 
         controller.addTool( new ChangeNSDestinyPositionTool( cutscene, positionX, positionY ) );
     }
 
     public EffectsController getEffects( ) {
 
         return new EffectsController( cutscene.getEffects( ) );
     }
 
 }
