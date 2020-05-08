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
 package es.eucm.eadventure.editor.control.controllers.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.data.chapter.elements.ActiveArea;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 public class ActiveAreasListDataControl extends DataControl {
 
     /**
      * Scene controller that contains this element reference.
      */
     private SceneDataControl sceneDataControl;
 
     /**
      * List of activeAreas.
      */
     private List<ActiveArea> activeAreasList;
 
     /**
      * List of activeAreas controllers.
      */
     private List<ActiveAreaDataControl> activeAreasDataControlList;
 
     /**
      * Constructor.
      * 
      * @param sceneDataControl
      *            Link to the parent scene controller
      * @param activeAreasList
      *            List of activeAreas
      */
     public ActiveAreasListDataControl( SceneDataControl sceneDataControl, List<ActiveArea> activeAreasList ) {
 
         this.sceneDataControl = sceneDataControl;
         this.activeAreasList = activeAreasList;
 
         // Create subcontrollers
         activeAreasDataControlList = new ArrayList<ActiveAreaDataControl>( );
         for( ActiveArea activeArea : activeAreasList )
             activeAreasDataControlList.add( new ActiveAreaDataControl( sceneDataControl, activeArea ) );
     }
 
     /**
      * Returns the list of activeAreas controllers.
      * 
      * @return List of activeAreas controllers
      */
     public List<ActiveAreaDataControl> getActiveAreas( ) {
 
         return activeAreasDataControlList;
     }
 
     /**
      * Returns the last activeArea controller from the list.
      * 
      * @return Last activeArea controller
      */
     public ActiveAreaDataControl getLastActiveArea( ) {
 
         return activeAreasDataControlList.get( activeAreasDataControlList.size( ) - 1 );
     }
 
     /**
      * Returns the id of the scene that contains this activeAreas list.
      * 
      * @return Parent scene id
      */
     public String getParentSceneId( ) {
 
         return sceneDataControl.getId( );
     }
 
     @Override
     public Object getContent( ) {
 
         return activeAreasList;
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         return new int[] { Controller.ACTIVE_AREA };
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
         // It can always add new activeArea
         return type == Controller.ACTIVE_AREA;
     }
 
     @Override
     public boolean canBeDeleted( ) {
 
         return false;
     }
 
     @Override
     public boolean canBeMoved( ) {
 
         return false;
     }
 
     @Override
     public boolean canBeRenamed( ) {
 
         return false;
     }
 
     @Override
     public boolean addElement( int type, String id ) {
 
         boolean elementAdded = false;
 
         if( type == Controller.ACTIVE_AREA ) {
 
             // Show a dialog asking for the item id
             String itemId = id;
             if( id == null )
                 itemId = controller.showInputDialog( TC.get( "Operation.AddItemTitle" ), TC.get( "Operation.AddItemMessage" ), TC.get( "Operation.AddItemDefaultValue" ) );
 
             // If some value was typed and the identifier is valid
             if( itemId != null && controller.isElementIdValid( itemId ) ) {
                 ActiveArea newActiveArea = new ActiveArea( itemId, true, 220, 220, 100, 100 );
                 activeAreasList.add( newActiveArea );
                 ActiveAreaDataControl newActiveAreaDataControl = new ActiveAreaDataControl( sceneDataControl, newActiveArea );
                 activeAreasDataControlList.add( newActiveAreaDataControl );
                 controller.getIdentifierSummary( ).addActiveAreaId( itemId );
                 //controller.dataModified( );
                 elementAdded = true;
             }
         }
 
         return elementAdded;
     }
 
     @Override
     public String getDefaultId( int type ) {
 
         return "IdObject";
     }
 
     @Override
     public boolean duplicateElement( DataControl dataControl ) {
 
         if( !( dataControl instanceof ActiveAreaDataControl ) )
             return false;
 
         try {
             ActiveArea newElement = (ActiveArea) ( ( (ActiveArea) ( dataControl.getContent( ) ) ).clone( ) );
            String id = newElement.getId( );
             int i = 1;
             do {
                id = newElement.getId( ) + i;
                 i++;
             } while( !controller.isElementIdValid( id, false ) );
             newElement.setId( id );
             activeAreasList.add( newElement );
             activeAreasDataControlList.add( new ActiveAreaDataControl( sceneDataControl, newElement ) );
             controller.getIdentifierSummary( ).addActiveAreaId( id );
             return true;
         }
         catch( CloneNotSupportedException e ) {
             ReportDialog.GenerateErrorReport( e, true, "Could not clone activeArea" );
             return false;
         }
     }
 
     @Override
     public boolean deleteElement( DataControl dataControl, boolean askConfirmation ) {
 
         boolean elementDeleted = false;
         String id = ( (ActiveAreaDataControl) dataControl ).getId( );
         String references = String.valueOf( controller.countIdentifierReferences( id ) );
 
         if( !askConfirmation || controller.showStrictConfirmDialog( TC.get( "Operation.DeleteElementTitle" ), TC.get( "Operation.DeleteElementWarning", new String[] { id, references } ) ) ) {
             if( activeAreasList.remove( dataControl.getContent( ) ) ) {
                 activeAreasDataControlList.remove( dataControl );
                 controller.deleteIdentifierReferences( id );
                 controller.getIdentifierSummary( ).deleteActiveAreaId( ( (ActiveArea) dataControl.getContent( ) ).getId( ) );
                 elementDeleted = true;
             }
         }
 
         return elementDeleted;
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = activeAreasList.indexOf( dataControl.getContent( ) );
 
         if( elementIndex > 0 ) {
             activeAreasList.add( elementIndex - 1, activeAreasList.remove( elementIndex ) );
             activeAreasDataControlList.add( elementIndex - 1, activeAreasDataControlList.remove( elementIndex ) );
             //controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = activeAreasList.indexOf( dataControl.getContent( ) );
 
         if( elementIndex < activeAreasList.size( ) - 1 ) {
             activeAreasList.add( elementIndex + 1, activeAreasList.remove( elementIndex ) );
             activeAreasDataControlList.add( elementIndex + 1, activeAreasDataControlList.remove( elementIndex ) );
             //controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public String renameElement( String name ) {
 
         return null;
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         // Iterate through each activeArea
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             activeAreaDataControl.updateVarFlagSummary( varFlagSummary );
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         boolean valid = true;
 
         // Iterate through the activeAreas
         for( int i = 0; i < activeAreasDataControlList.size( ); i++ ) {
             String activeAreaPath = currentPath + " >> " + TC.getElement( Controller.ACTIVE_AREA ) + " #" + ( i + 1 );
             valid &= activeAreasDataControlList.get( i ).isValid( activeAreaPath, incidences );
         }
 
         return valid;
     }
 
     @Override
     public int countAssetReferences( String assetPath ) {
 
         int count = 0;
 
         // Iterate through each activeArea
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             count += activeAreaDataControl.countAssetReferences( assetPath );
 
         return count;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             activeAreaDataControl.getAssetReferences( assetPaths, assetTypes );
 
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
         // Iterate through each activeArea
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             activeAreaDataControl.deleteAssetReferences( assetPath );
     }
 
     @Override
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
 
         // Iterate through each activeArea
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             count += activeAreaDataControl.countIdentifierReferences( id );
 
         return count;
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         // Iterate through each activeArea
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             activeAreaDataControl.replaceIdentifierReferences( oldId, newId );
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         // Spread the call to every activeArea
         for( ActiveAreaDataControl activeAreaDataControl : activeAreasDataControlList )
             activeAreaDataControl.deleteIdentifierReferences( id );
 
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return false;
     }
 
     /**
      * Returns the data controllers of the item references of the scene that
      * contains this element reference.
      * 
      * @return List of item references (including the one being edited)
      */
     public List<ElementReferenceDataControl> getParentSceneItemReferences( ) {
 
         return sceneDataControl.getReferencesList( ).getItemReferences( );
     }
 
     /**
      * Returns the data controllers of the character references of the scene
      * that contains this element reference.
      * 
      * @return List of character references (including the one being edited)
      */
     public List<ElementReferenceDataControl> getParentSceneNPCReferences( ) {
 
         return sceneDataControl.getReferencesList( ).getNPCReferences( );
     }
 
     /**
      * Returns the data controllers of the atrezzo items references of the scene
      * that contains this element reference.
      * 
      * @return List of atrezzo references (including the one being edited)
      */
     public List<ElementReferenceDataControl> getParentSceneAtrezzoReferences( ) {
 
         return sceneDataControl.getReferencesList( ).getAtrezzoReferences( );
     }
 
     public List<ExitDataControl> getParentSceneExits( ) {
 
         return sceneDataControl.getExitsList( ).getExits( );
     }
 
     public List<BarrierDataControl> getParentSceneBarriers( ) {
 
         return sceneDataControl.getBarriersList( ).getBarriers( );
     }
 
     @Override
     public void recursiveSearch( ) {
 
         for( DataControl dc : this.activeAreasDataControlList )
             dc.recursiveSearch( );
     }
 
     public SceneDataControl getSceneDataControl( ) {
 
         return this.sceneDataControl;
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         return getPathFromChild( dataControl, activeAreasDataControlList );
     }
 
     public List<ActiveArea> getActiveAreasList( ) {
 
         return this.activeAreasList;
     }
 
 }
