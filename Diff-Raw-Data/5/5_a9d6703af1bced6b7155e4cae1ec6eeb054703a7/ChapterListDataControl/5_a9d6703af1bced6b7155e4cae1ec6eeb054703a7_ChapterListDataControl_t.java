 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *          research group.
  *   
  *    Copyright 2005-2012 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
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
  * This file is part of <e-Adventure>, version 1.4.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       <e-Adventure> is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      <e-Adventure> is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.control.controllers.general;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.Chapter;
 import es.eucm.eadventure.editor.control.controllers.AdventureDataControl;
 import es.eucm.eadventure.editor.control.controllers.ChapterToolManager;
 import es.eucm.eadventure.editor.control.controllers.adaptation.AdaptationProfileDataControl;
 import es.eucm.eadventure.editor.control.controllers.assessment.AssessmentProfileDataControl;
 import es.eucm.eadventure.editor.control.tools.Tool;
 import es.eucm.eadventure.editor.data.support.IdentifierSummary;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 public class ChapterListDataControl {
 
     /**
      * Controller for the chapters of the adventure.
      */
     private List<ChapterDataControl> chapterDataControlList;
 
     /**
      * The list of toolManagers (for undo/redo)
      */
     private List<ChapterToolManager> chapterToolManagers;
 
     /**
      * Stores the current selected Chapter
      */
     private int selectedChapter;
 
     /**
      * Summary of identifiers.
      */
     private IdentifierSummary identifierSummary;
 
     /**
      * Summary of flags.
      */
     private VarFlagSummary varFlagSummary;
 
     /**
      * The list of chapters
      */
     private List<Chapter> chapters;
 
     public ChapterListDataControl( ) {
 
         varFlagSummary = new VarFlagSummary( );
         chapterDataControlList = new ArrayList<ChapterDataControl>( );
         chapterToolManagers = new ArrayList<ChapterToolManager>( );
         setSelectedChapterInternal( -1 );
         chapters = new ArrayList<Chapter>( );
     }
 
     public ChapterListDataControl( List<Chapter> chapters ) {
 
         this( );
         for( Chapter chapter : chapters ) {
             chapterDataControlList.add( new ChapterDataControl( chapter ) );
             chapterToolManagers.add( new ChapterToolManager( ) );
         }
         if( chapters.size( ) > 0 )
             setSelectedChapterInternal( 0 );
         this.chapters = chapters;
     }
     
     public List<ChapterDataControl> getChapters() {
         return chapterDataControlList;
     }
 
     public void setSelectedChapterInternal( int newSelectedChapter ) {
 
         this.selectedChapter = newSelectedChapter;
         if( selectedChapter == -1 ) {
             if( chapterDataControlList.size( ) > 0 ) {
                 selectedChapter = 0;
                 if( identifierSummary == null )
                     identifierSummary = new IdentifierSummary( getSelectedChapterData( ) );
                 else
                     identifierSummary.loadIdentifiers( getSelectedChapterData( ) );
 
                 if( varFlagSummary == null )
                     varFlagSummary = new VarFlagSummary( );
                 getSelectedChapterDataControl( ).updateVarFlagSummary( varFlagSummary );
             }
             else {
                 identifierSummary = null;
                 varFlagSummary = new VarFlagSummary( );
             }
         }
         else {
             identifierSummary = new IdentifierSummary( getSelectedChapterData( ) );
            //if( varFlagSummary == null )
            //delete all flags and vars before add those included in the chapter through updateVarFlagSummary methods
            varFlagSummary = new VarFlagSummary( );
             getSelectedChapterDataControl( ).updateVarFlagSummary( varFlagSummary );
         }
     }
 
     /**
      * Returns the data of the selected chapter.
      * 
      * @return Selected chapter data
      */
     public Chapter getSelectedChapterData( ) {
 
         return (Chapter) chapterDataControlList.get( selectedChapter ).getContent( );
     }
 
     /**
      * Adds a new data control with the new chapter. It also updates
      * automatically the selectedChapter, pointing to this new one
      * 
      * @param chapter
      */
     public void addChapterDataControl( Chapter newChapter ) {
 
         chapters.add( newChapter );
         chapterDataControlList.add( new ChapterDataControl( newChapter ) );
         chapterToolManagers.add( new ChapterToolManager( ) );
         setSelectedChapterInternal( chapterDataControlList.size( ) - 1 );
     }
 
     /**
      * Adds a new data control with the new chapter in the given position. It
      * also updates automatically the selectedChapter, pointing to this new one
      * 
      * @param chapter
      */
     public void addChapterDataControl( int index, Chapter newChapter ) {
 
         chapters.add( index, newChapter );
         chapterDataControlList.add( index, new ChapterDataControl( newChapter ) );
         chapterToolManagers.add( index, new ChapterToolManager( ) );
         setSelectedChapterInternal( index );
     }
 
     /**
      * Deletes the selected chapter data control. It also updates automatically
      * the selectedChapter if necessary
      * 
      * @param chapter
      */
     public ChapterDataControl removeChapterDataControl( ) {
 
         return removeChapterDataControl( selectedChapter );
     }
 
     /**
      * Deletes the chapter data control in the given position. It also updates
      * automatically the selectedChapter if necessary
      * 
      * @param chapter
      */
     public ChapterDataControl removeChapterDataControl( int index ) {
 
         chapters.remove( index );
         ChapterDataControl removed = chapterDataControlList.remove( index );
         chapterToolManagers.remove( index );
         setSelectedChapterInternal( selectedChapter - 1 );
         return removed;
     }
 
     /**
      * Returns the index of the chapter currently selected.
      * 
      * @return Index of the selected chapter
      */
     public int getSelectedChapter( ) {
 
         return selectedChapter;
     }
 
     /**
      * Returns the selected chapter data controller.
      * 
      * @return The selected chapter data controller
      */
     public ChapterDataControl getSelectedChapterDataControl( ) {
 
         if( chapterDataControlList.size( ) != 0 )
             return chapterDataControlList.get( selectedChapter );
         else
             return null;
     }
 
     public void addPlayerToAllScenesChapters( ) {
 
         for( ChapterDataControl dataControl : chapterDataControlList ) {
             dataControl.getScenesList( ).addPlayerToAllScenes( );
         }
     }
 
     public void addPlayerToAllScenesSelectedChapter( ) {
 
     }
 
     public void deletePlayerToAllScenesChapters( ) {
 
         for( ChapterDataControl dataControl : chapterDataControlList ) {
             dataControl.getScenesList( ).deletePlayerToAllScenes( );
         }
     }
 
     public void deletePlayerToAllScenesSelectedChapter( ) {
 
     }
 
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         boolean valid = true;
         for( ChapterDataControl dataControl : chapterDataControlList ) {
             valid &= dataControl.isValid( currentPath, incidences );
         }
 
         return valid;
     }
 
     public boolean isAnyChapterSelected( ) {
 
         return selectedChapter != -1;
     }
 
     /**
      * @return the identifierSummary
      */
     public IdentifierSummary getIdentifierSummary( ) {
 
         return identifierSummary;
     }
 
     /**
      * @return the varFlagSummary
      */
     public VarFlagSummary getVarFlagSummary( ) {
 
         return varFlagSummary;
     }
 
     public boolean replaceSelectedChapter( Chapter newChapter ) {
 
         int chapter = this.getSelectedChapter( );
         chapters.set( getSelectedChapter( ), newChapter );
         chapterDataControlList.set( chapter, new ChapterDataControl( newChapter ) );
         identifierSummary = new IdentifierSummary( newChapter );
         identifierSummary.loadIdentifiers( getSelectedChapterData( ) );
 
         return true;
     }
 
     public boolean hasScorm12Profiles( AdventureDataControl adventureData ) {
 
         boolean hasProfiles = true;
         for( ChapterDataControl dataControl : chapterDataControlList ) {
             if( dataControl.hasAdaptationProfile( ) ) {
                 AdaptationProfileDataControl adpProfile = dataControl.getSelectedAdaptationProfile( );
                 hasProfiles &= adpProfile.isScorm12( );
             }
             if( dataControl.hasAssessmentProfile( ) ) {
                 AssessmentProfileDataControl assessmentProfile = dataControl.getSelectedAssessmentProfile( );
                 hasProfiles &= assessmentProfile.isScorm12( );
             }
         }
         return hasProfiles;
     }
 
     public boolean hasScorm2004Profiles( AdventureDataControl adventureData ) {
 
         boolean hasProfiles = true;
         for( ChapterDataControl dataControl : chapterDataControlList ) {
             if( dataControl.hasAdaptationProfile( ) ) {
                 AdaptationProfileDataControl adpProfile = dataControl.getSelectedAdaptationProfile( );
                 hasProfiles &= adpProfile.isScorm2004( );
             }
             if( dataControl.hasAssessmentProfile( ) ) {
                 AssessmentProfileDataControl assessmentProfile = dataControl.getSelectedAssessmentProfile( );
                 hasProfiles &= assessmentProfile.isScorm2004( );
             }
         }
         return hasProfiles;
     }
 
     public void updateVarFlagSummary( ) {
 
         getSelectedChapterDataControl( ).updateVarFlagSummary( varFlagSummary );
     }
 
     /**
      * Moves the selected chapter to the previous position of the chapter's
      * list.
      */
     public boolean moveChapterUp( ) {
 
         return moveChapterUp( selectedChapter );
     }
 
     /**
      * Moves the selected chapter to the previous position of the chapter's
      * list.
      */
     public boolean moveChapterUp( int index ) {
 
         // If the chapter can be moved
         if( index > 0 ) {
             // Move the chapter and update the selected chapter
             chapters.add( index - 1, chapters.remove( index ) );
             // Move the chapter and update the selected chapter
             chapterDataControlList.add( index - 1, chapterDataControlList.remove( index ) );
             chapterToolManagers.add( index - 1, chapterToolManagers.remove( index ) );
 
             setSelectedChapterInternal( index - 1 );
             return true;
         }
         return false;
     }
 
     /**
      * Moves the selected chapter to the next position of the chapter's list.
      * 
      */
     public boolean moveChapterDown( ) {
 
         return moveChapterDown( selectedChapter );
     }
 
     /**
      * Moves the selected chapter to the next position of the chapter's list.
      * 
      */
     public boolean moveChapterDown( int index ) {
 
         // If the chapter can be moved
         if( index < chapterDataControlList.size( ) - 1 ) {
             // Move the chapter and update the selected chapter
             chapters.add( index + 1, chapters.remove( index ) );
             // Move the chapter and update the selected chapter
             chapterDataControlList.add( index + 1, chapterDataControlList.remove( index ) );
             setSelectedChapterInternal( index + 1 );
             return true;
         }
         return false;
     }
 
     /**
      * Counts all the references to a given asset in the entire script.
      * 
      * @param assetPath
      *            Path of the asset (relative to the ZIP), without suffix in
      *            case of an animation or set of slides
      * @return Number of references to the given asset
      */
     public int countAssetReferences( String assetPath ) {
 
         int count = 0;
 
         // Search in all the chapters
         for( ChapterDataControl chapterDataControl : chapterDataControlList ) {
             count += chapterDataControl.countAssetReferences( assetPath );
         }
         return count;
     }
 
     /**
      * Gets a list with all the assets referenced in the chapter along with the
      * types of those assets
      * 
      * @param assetPaths
      * @param assetTypes
      */
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         for( ChapterDataControl chapterDataControl : chapterDataControlList ) {
             chapterDataControl.getAssetReferences( assetPaths, assetTypes );
         }
     }
 
     /**
      * Deletes a given asset from the script, removing all occurrences.
      * 
      * @param assetPath
      *            Path of the asset (relative to the ZIP), without suffix in
      *            case of an animation or set of slides
      */
     public void deleteAssetReferences( String assetPath ) {
 
         // Delete the asset in all the chapters
         for( ChapterDataControl chapterDataControl : chapterDataControlList )
             chapterDataControl.deleteAssetReferences( assetPath );
     }
 
     /**
      * Deletes a given identifier from the script, removing all occurrences.
      * 
      * @param id
      *            Identifier to be deleted
      */
     public void deleteIdentifierReferences( String id ) {
 
         if( getSelectedChapterDataControl( ) != null )
             getSelectedChapterDataControl( ).deleteIdentifierReferences( id );
       //  else
         //    this.identifierSummary.deleteAssessmentRuleId( id );
     }
 
     public boolean addTool( Tool tool ) {
 
         boolean done = true;
         if( !tool.doesClone( ) ) {
             done = chapterToolManagers.get( getSelectedChapter( ) ).addTool( tool );
         }
         else {
             if( done = tool.doTool( ) ) {
                 chapterToolManagers.get( getSelectedChapter( ) ).clear( );
                 chapterToolManagers.get( getSelectedChapter( ) ).addTool( false, tool );
             }
             else
                 chapterToolManagers.get( getSelectedChapter( ) ).addTool( false, tool );
         }
 
         return done;
     }
 
     public void undoTool( ) {
 
         chapterToolManagers.get( getSelectedChapter( ) ).undoTool( );
     }
 
     public void redoTool( ) {
 
         chapterToolManagers.get( getSelectedChapter( ) ).redoTool( );
     }
 
     public void pushLocalToolManager( ) {
 
         chapterToolManagers.get( getSelectedChapter( ) ).pushLocalToolManager( );
     }
 
     public void popLocalToolManager( ) {
 
         chapterToolManagers.get( getSelectedChapter( ) ).popLocalToolManager( );
     }
 
     /**
      * Returns an array with the chapter titles.
      * 
      * @return Array with the chapter titles
      */
     public String[] getChapterTitles( ) {
 
         List<String> chapterNames = new ArrayList<String>( );
 
         // Add the chapter titles
         for( ChapterDataControl chapterDataControl : chapterDataControlList ) {
             Chapter chapter = (Chapter) chapterDataControl.getContent( );
             chapterNames.add( chapter.getTitle( ) );
         }
 
         return chapterNames.toArray( new String[] {} );
     }
 
     public int getChaptersCount( ) {
 
         return chapters.size( );
     }
 
     public boolean exitsChapter( String chapterTitle ) {
 
         // look the chapter titles
         for( ChapterDataControl chapterDataControl : chapterDataControlList ) {
             Chapter chapter = (Chapter) chapterDataControl.getContent( );
             if( chapter.getTitle( ).equals( chapterTitle ) )
                 return true;
         }
         return false;
     }
 
     /**
      * Private method that fills the flags and vars structures of the chapter
      * data before passing on the information to the game engine for running
      */
     public void updateVarsFlagsForRunning( ) {
 
         // Update everyChapter
         for( ChapterDataControl chapterDataControl : chapterDataControlList ) {
             VarFlagSummary tempSummary = new VarFlagSummary( );
             chapterDataControl.updateVarFlagSummary( tempSummary );
             tempSummary.clean( );
             Chapter chapter = (Chapter) chapterDataControl.getContent( );
             // Update flags
             for( String flag : tempSummary.getFlags( ) ) {
                 chapter.addFlag( flag );
             }
             // Update vars
             for( String var : tempSummary.getVars( ) ) {
                 chapter.addVar( var );
             }
         }
     }
 
     ////////DEBUGGING OPTIONS
     /**
      * @return the chapterToolManagers
      */
     public List<ChapterToolManager> getChapterToolManagers( ) {
 
         return chapterToolManagers;
     }
 
 }
