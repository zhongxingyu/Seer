 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.control.controllers.general;
 
 import java.util.List;
 
 import es.eucm.eadventure.common.auxiliar.AssetsConstants;
 import es.eucm.eadventure.common.data.HasSound;
 import es.eucm.eadventure.common.data.chapter.Exit;
 import es.eucm.eadventure.common.data.chapter.ExitLook;
 import es.eucm.eadventure.common.data.chapter.NextScene;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.tools.general.InvalidExitCursorTool;
 import es.eucm.eadventure.editor.control.tools.general.assets.SelectExitCursorPathTool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeStringValueTool;
 
 public class ExitLookDataControl implements HasSound{
 
     private ExitLook exitLook;
 
     public ExitLookDataControl( NextScene nextScene ) {
 
         if( nextScene.getExitLook( ) == null )
             nextScene.setExitLook( new ExitLook( ) );
         this.exitLook = nextScene.getExitLook( );
     }
 
     public ExitLookDataControl( Exit exit ) {
 
         if( exit.getDefaultExitLook( ) == null )
             exit.setDefaultExitLook( new ExitLook( ) );
         this.exitLook = exit.getDefaultExitLook( );
     }
 
     /**
      * @return the isTextCustomized
      */
     public boolean isTextCustomized( ) {
 
         return exitLook.getExitText( ) != null;
     }
 
     public String getCustomizedText( ) {
 
         String text = null;
         if( exitLook != null && exitLook.getExitText( ) != null )
             text = exitLook.getExitText( );
         return text;
     }
     
     //v1.4
     public String getSoundPath( ) {
 
         String text = null;
         if( exitLook != null && exitLook.getSoundPath( ) != null )
             text = exitLook.getSoundPath( );
         return text;
     }
 
     /**
      * @return the isCursorCustomized
      */
     public boolean isCursorCustomized( ) {
 
         return exitLook.getCursorPath( ) != null || exitLook.getSoundPath( )!=null;
     }
 
     public String getCustomizedCursor( ) {
 
         String text = null;
         if( exitLook != null && exitLook.getCursorPath( ) != null )
             text = exitLook.getCursorPath( );
         return text;
     }
 
     public void setExitText( String text ) {
         try {
             Controller.getInstance( ).addTool( new ChangeStringValueTool ( exitLook, text, "getExitText", "setExitText") );
         }
         catch( Exception e ) {
             e.printStackTrace( );
         }
         
         //this.exitLook.setExitText( text );
     }
 
     public void editCursorPath( ) {
 
         try {
             Controller.getInstance( ).addTool( new SelectExitCursorPathTool( exitLook ) );
         }
         catch( CloneNotSupportedException e ) {
             e.printStackTrace( );
         }
     }
 
     public void invalidCursor( ) {
 
         Controller.getInstance( ).addTool( new InvalidExitCursorTool( exitLook ) );
     }
 
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         if (exitLook.getCursorPath( )!=null && !exitLook.getCursorPath( ).equals( "" ) && !assetPaths.contains( exitLook.getCursorPath( ) )){
             assetPaths.add( exitLook.getCursorPath( ) );
             assetTypes.add( AssetsConstants.CATEGORY_CURSOR );    
         }
         
         if (exitLook.getSoundPath( )!=null && !exitLook.getSoundPath( ).equals( "" ) && !assetPaths.contains( exitLook.getSoundPath( ) )){
             assetPaths.add( exitLook.getSoundPath( ) );
             assetTypes.add( AssetsConstants.CATEGORY_AUDIO );    
         }
         
     }
 
     public int countAssetReferences( String assetPath ) {
 
         if( exitLook.getCursorPath( ) != null && exitLook.getCursorPath( ).equals( assetPath ) )
             return 1;
         else if( exitLook.getSoundPath( ) != null && exitLook.getSoundPath( ).equals( assetPath ) )
             return 1;
         else
             return 0;
 
     }
 
     public void deleteAssetReferences( String assetPath ) {
 
         if( exitLook.getCursorPath( ) != null && exitLook.getCursorPath( ).equals( assetPath ) )
             exitLook.setCursorPath( "" );
         
         if( exitLook.getSoundPath( ) != null && exitLook.getSoundPath( ).equals( assetPath ) )
             exitLook.setSoundPath( "" );
 
     }
 
     public void setSoundPath( String soundPath ) {
         if (exitLook!=null){
             exitLook.setSoundPath( soundPath );
         }
     }
 
    //DO NOT REMOVE: USED WITH REFLECTION
    public void setCursorPath( String value ) {

        exitLook.setCursorPath( value );
    }


 }
