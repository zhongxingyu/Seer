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
 package es.eucm.eadventure.engine.core.data;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.engine.core.control.FlagSummary;
 import es.eucm.eadventure.engine.core.control.ItemSummary;
import es.eucm.eadventure.engine.core.control.VarSummary;
 import es.eucm.eadventure.engine.core.control.TimerManager;
import es.eucm.eadventure.common.data.chapter.elements.Item;
 
 /**
  * This class manages the saved games in eAdventure
  */
 public class SaveGame implements Serializable {
 
     /**
      * Required by serializable
      */
     private static final long serialVersionUID = 1L;
 
     // version number has not to be checked
     private static final int NUMBEROFCHECKMARKS = 10;
 
     private static final String TITLEMARK = "TITLE#";
 
     private static final String CHAPTERMARK = "CHAPTER#";
 
     private static final String TOTALTIMEMARK = "TOTALTIME#";
 
     private static final String SAVETIMEMARK = "SAVETIME#";
 
     private static final String IDSCENEMARK = "IDSCENE#";
 
     private static final String FLAGSMARK = "ACTIVEFLAGS#";
 
     private static final String VARMARK = "VAR#";
 
     private static final String ITEMSMARK = "ITEMS#";
 
     private static final String PLAYERPOSMARK = "PLAYERPOS#";
 
     private static final String TIMERSMARK = "TIMER#";
 
     private static final String VERSIONNUMBERMARK = "VERSIONNUMBER#";
 
     private SaveGameData saveGameData;
 
     private boolean[] check;
 
     /**
      * Creates a new SaveGame
      */
     public SaveGame( ) {
 
         saveGameData = new SaveGameData( );
         check = new boolean[ NUMBEROFCHECKMARKS ];
 
     }
 
     public boolean existSaveFile( String name ) {
 
         try {
             BufferedReader file = new BufferedReader( new FileReader( name ) );
             saveGameData = new SaveGameData( );
 
             String line;
             line = file.readLine( ).trim( );
             if( VERSIONNUMBERMARK.equals( line.split( "#" )[0] + "#" ) ) {
                 saveGameData.versionNumber = Integer.parseInt( line.split( "#" )[1].split( "&" )[0] );
                 saveGameData.projectName = line.split( "#" )[1].split( "&" )[1];
                 line = file.readLine( ).trim( ).split( "#" )[1].split( "&" )[0];
             }
             else
                 line = line.split( "#" )[1].split( "&" )[0];
 
             saveGameData.title = line;
             line = file.readLine( ).trim( );
             line = file.readLine( ).trim( ).split( "#" )[1].split( "&" )[0];
             ;
             saveGameData.saveTime = line;
 
             return true;
         }
         catch( FileNotFoundException e ) {
             return false;
         }
         catch( IOException e ) {
             return false;
         }
     }
 
     private void initializeCheck( ) {
 
         for( int i = 0; i < NUMBEROFCHECKMARKS; i++ ) {
             check[i] = false;
         }
     }
 
     /**
      * Loads a game from the given file.
      * 
      * @param filename
      *            the file to load
      * @return true if the load was succesful, false otherwise
      * @throws SaveGameException
      */
     public boolean loadTxt( String filename ) throws SaveGameException {
 
         boolean loaded = true;
         boolean end = false;
         try {
             BufferedReader file = new BufferedReader( new FileReader( filename ) );
             saveGameData = new SaveGameData( );
             String line;
             initializeCheck( );
             while( !end ) {
                 line = file.readLine( ).trim( );
 
                 if( !line.isEmpty( ) ) {

                     String[] spliter = line.split( "#" );
                     analyzeString( spliter );
                 }
                 else {
                     end = true;
                 }
             }
         }
         catch( FileNotFoundException e ) {
             loaded = false;
         }
         catch( IOException e ) {
             loaded = false;
         }
         catch( NullPointerException e ) {
             // the file is empty
             check( );
         }
         catch( NumberFormatException e ) {
             loaded = false;
         }
         return loaded;
     }
 
     private void check( ) throws SaveGameException {
 
         for( int i = 0; i < NUMBEROFCHECKMARKS; i++ ) {
             if( !check[i] )
                 throw new SaveGameException( "Error loading" );
 
         }
     }
 
     private void analyzeString( String[] str ) throws SaveGameException, NumberFormatException {
 
         String mark = str[0];
         mark += "#";
         // if there arent anything after the mark
         if( str.length == 2 ) {
             if( str[1].contains( "&" ) ) {
                 if( mark.equals( TITLEMARK ) ) {
 
                     saveGameData.title = str[1].split( "&" )[0];
                     check[0] = true;
 
                 }
                 else if( mark.equals( CHAPTERMARK ) ) {
 
                     saveGameData.chapter = Integer.parseInt( str[1].split( "&" )[0] );
                     check[1] = true;
 
                 }
                 else if( mark.equals( TOTALTIMEMARK ) ) {
 
                     saveGameData.totalTime = Long.parseLong( str[1].split( "&" )[0] );
                     check[2] = true;
 
                 }
                 else if( mark.equals( SAVETIMEMARK ) ) {
 
                     saveGameData.saveTime = str[1].split( "&" )[0];
                     check[3] = true;
 
                 }
                 else if( mark.equals( IDSCENEMARK ) ) {
 
                     saveGameData.idScene = str[1].split( "&" )[0];
                     check[4] = true;
 
                 }
                 else if( mark.equals( FLAGSMARK ) ) {
                     analiceFlags( str[1] );
                     check[5] = true;
                 }
                 else if( mark.equals( VARMARK ) ) {
                     analiceVars( str[1] );
                     check[6] = true;
                 }
                 else if( mark.equals( ITEMSMARK ) ) {
                     analyzeItems( str[1] );
                     check[7] = true;
                 }
                 else if( mark.equals( PLAYERPOSMARK ) ) {
 
                     saveGameData.playerX = Float.parseFloat( str[1].split( ";" )[0] );
                     saveGameData.playerY = Float.parseFloat( str[1].split( ";" )[1].split( "&" )[0] );
                     check[8] = true;
 
                 }
                 else if( mark.equals( TIMERSMARK ) ) {
                     String[] aux = str[1].split( "&" );
                     if( aux.length != 0 ) {
                         str[1] = aux[0];
                         saveGameData.loadTimers = str[1].split( ";" );
                     }
                     check[9] = true;
 
                 }
                 else if( mark.equals( VERSIONNUMBERMARK ) ) {
                     String[] aux = str[1].split( "&" );
                     saveGameData.versionNumber = Integer.parseInt( aux[0] );
                     saveGameData.projectName = aux[1];
 
                 }
             }
         }
 
     }
 
     private void analiceFlags( String line ) {
 
         String[] inactiveFlags = new String[ 1 ];
         inactiveFlags[0] = "";
         String[] allFlags = line.split( "&" );
         String[] activeFlags = allFlags[0].split( ";" );
         if( allFlags.length > 1 )
             inactiveFlags = allFlags[1].split( ";" );
         ArrayList<String> flags = new ArrayList<String>( );
         for( int i = 0; i < activeFlags.length; i++ ) {
             if( !activeFlags[i].trim( ).equals( "" ) )
                 flags.add( activeFlags[i] );
         }
         for( int i = 0; i < inactiveFlags.length; i++ ) {
             if( !inactiveFlags[i].trim( ).equals( "" ) )
                 flags.add( inactiveFlags[i] );
         }
         saveGameData.flags = new FlagSummary( flags, false );
         for( int i = 0; i < activeFlags.length; i++ ) {
             if( !activeFlags[i].trim( ).equals( "" ) )
                 saveGameData.flags.activateFlag( activeFlags[i] );
         }
     }
 
     private void analiceVars( String line ) throws SaveGameException {
 
         String[] allVars = line.split( "&" );
         if( allVars.length != 0 ) {
             String[] varNames = allVars[0].split( ";" );
             List<String> vars = new ArrayList<String>( );
             for( String var : varNames ) {
                 vars.add( var );
             }
             //Corrupted load file
             if( allVars.length > 1 ) {
                 String[] varValuesStrings = allVars[1].split( ";" );
                 int[] varValues = new int[ varValuesStrings.length ];
                 for( int i = 0; i < varValuesStrings.length; i++ ) {
                     varValues[i] = Integer.parseInt( varValuesStrings[i] );
                 }
                 saveGameData.vars = new VarSummary( vars, false );
                 if( varValuesStrings.length == varNames.length ) {
                     for( int i = 0; i < varNames.length; i++ ) {
 
                         saveGameData.vars.setVarValue( varNames[i], varValues[i] );
                     }
                 }
                 else {
                     throw new SaveGameException( "Corrupted load file" );
                 }
 
             }
             else {
                 throw new SaveGameException( "Corrupted load file" );
             }
         }
     }
 
     private void analyzeItems( String line ) {
 
         String[] placedItems = new String[ 1 ];
         String[] consumedItems = new String[ 1 ];
         String[] grabbedItems = new String[ 1 ];
         placedItems[0] = "";
         grabbedItems[0] = "";
         consumedItems[0] = "";
         String[] items = line.split( "&" );
         if( items.length >= 1 ) {
             placedItems = items[0].split( ";" );
         }
 
         if( items.length >= 2 ) {
             consumedItems = items[1].split( ";" );
         }
 
         if( items.length >= 3 ) {
             grabbedItems = items[2].split( ";" );
         }
 
         ArrayList<Item> finalItems = new ArrayList<Item>( );
         for( int i = 0; i < placedItems.length; i++ ) {
             if( !placedItems[i].trim( ).equals( "" ) )
                 finalItems.add( new Item( placedItems[i] ) );
         }
         for( int i = 0; i < consumedItems.length; i++ ) {
             if( !consumedItems[i].trim( ).equals( "" ) )
                 finalItems.add( new Item( consumedItems[i] ) );
         }
         for( int i = 0; i < grabbedItems.length; i++ ) {
             if( !grabbedItems[i].trim( ).equals( "" ) )
                 finalItems.add( new Item( grabbedItems[i] ) );
         }
         saveGameData.itemSummary = new ItemSummary( finalItems );
         for( int i = 0; i < consumedItems.length; i++ ) {
             if( !consumedItems[i].trim( ).equals( "" ) )
                 saveGameData.itemSummary.consumeItem( consumedItems[i] );
         }
         for( int i = 0; i < grabbedItems.length; i++ ) {
             if( !grabbedItems[i].trim( ).equals( "" ) )
                 saveGameData.itemSummary.grabItem( grabbedItems[i] );
         }
     }
 
     /*
        public boolean loadTxt( String filename ) {
            boolean loaded = true;
            try {
                BufferedReader  file = new BufferedReader ( new FileReader( filename ) );
                saveGameData = new SaveGameData( );
                String line;
                line = file.readLine().trim();
                saveGameData.title = line;
                line = file.readLine().trim();
                saveGameData.chapter = Integer.parseInt( line );
                line = file.readLine().trim();
                saveGameData.saveTime = line;
                line = file.readLine().trim();
                saveGameData.totalTime = Long.parseLong(line);
                line = file.readLine().trim();
                saveGameData.idScene = line;
                line = file.readLine().trim();
                saveGameData.playerX = Float.parseFloat( line.split(";")[0] );
                saveGameData.playerY = Float.parseFloat( line.split(";")[1] );
                line = file.readLine().trim();
                String[] placedItems = line.split(";");
                line = file.readLine().trim();
                String[] consumedItems = line.split(";");
                line = file.readLine().trim();
                String[] grabbedItems = line.split(";");
                ArrayList<Item> items = new ArrayList<Item>();
                for(int i=0; i<placedItems.length; i++){
                    if ( !placedItems[i].trim().equals("") )
                        items.add( new Item( placedItems[i] ) ); 
                }
                for(int i=0; i<consumedItems.length; i++){
                    if ( !consumedItems[i].trim().equals("") )
                        items.add( new Item( consumedItems[i] ) ); 
                }
                for(int i=0; i<grabbedItems.length; i++){
                    if ( !grabbedItems[i].trim().equals("") )
                        items.add( new Item( grabbedItems[i] ) ); 
                }
                saveGameData.itemSummary = new ItemSummary( items );
                for(int i=0; i<consumedItems.length; i++){
                    if ( !consumedItems[i].trim().equals("") )
                        saveGameData.itemSummary.consumeItem( consumedItems[i] ); 
                }
                for(int i=0; i<grabbedItems.length; i++){
                    if ( !grabbedItems[i].trim().equals("") )
                        saveGameData.itemSummary.grabItem( grabbedItems[i] ); 
                }
                line = file.readLine().trim();
                String[] activeFlags = line.split(";");
                line = file.readLine().trim();
                String[] inactiveFlags = line.split(";");
                ArrayList<String> flags = new ArrayList<String>();
                for(int i=0; i<activeFlags.length; i++){
                    if ( !activeFlags[i].trim().equals("") )
                        flags.add( activeFlags[i] );
                }
                for(int i=0; i<inactiveFlags.length; i++){
                    if ( !inactiveFlags[i].trim().equals("") )
                        flags.add( inactiveFlags[i] );
                }
                saveGameData.flags = new FlagSummary( flags );
                for(int i=0; i<activeFlags.length; i++){
                    if ( !activeFlags[i].trim().equals("") )
                        saveGameData.flags.activateFlag( activeFlags[i] ); 
                }
                
                line = file.readLine().trim();
                String[] varNames = line.split(";");
                List<String> vars = new ArrayList<String>();
                for (String var: varNames){
                	vars.add(var);
                }
                line = file.readLine().trim();
                String[] varValuesStrings = line.split(";");
                int[] varValues = new int[varValuesStrings.length];
                for (int i=0; i<varValuesStrings.length; i++){
                	varValues[i] = Integer.parseInt(varValuesStrings[i]);
                }
                saveGameData.vars = new VarSummary( vars );
                for (int i=0; i<varNames.length; i++){
                	saveGameData.vars.setVarValue(varNames[i], varValues[i]);
                }
                
                line = file.readLine().trim();
                saveGameData.loadTimers = line.split(";");    
            } catch( FileNotFoundException e ) {
                loaded=false;
            } catch( IOException e ) {
                loaded=false;
            }
            return loaded;
        }
        */
 
     /**
      * Saves a game to the given file.
      * 
      * @param filename
      *            the file to save to
      * @return true if the save was succesful, false otherwise
      */
     public boolean saveTxt( String filename ) {
 
         boolean saved = true;
         try {
 
             PrintStream file = new PrintStream( new FileOutputStream( filename ) );
 
             file.println( VERSIONNUMBERMARK + saveGameData.versionNumber + "&" + saveGameData.projectName + "&" );
             file.println( TITLEMARK + saveGameData.title + "&" );
             file.println( CHAPTERMARK + saveGameData.chapter + "&" );
             file.println( SAVETIMEMARK + saveGameData.saveTime + "&" );
             file.println( TOTALTIMEMARK + saveGameData.totalTime + "&" );
             file.println( IDSCENEMARK + saveGameData.idScene + "&" );
             file.println( PLAYERPOSMARK + saveGameData.playerX + ";" + saveGameData.playerY + "&" );
 
             file.print( ITEMSMARK );
             for( String item : saveGameData.itemSummary.getNormalItems( ) ) {
                 file.print( item + ";" );
             }
             // file.println();
 
             //file.print(CONSUMEDITEMMARK);
             file.print( "&" );
             for( String item : saveGameData.itemSummary.getConsumedItems( ) ) {
                 file.print( item + ";" );
             }
             //file.println();
 
             //file.print(GRABBEDITEMMARK);
             file.print( "&" );
             for( String item : saveGameData.itemSummary.getGrabbedItems( ) ) {
                 file.print( item + ";" );
             }
             file.println( );
 
             file.print( FLAGSMARK );
 
             for( String flag : saveGameData.flags.getActiveFlags( ) ) {
                 file.print( flag + ";" );
             }
             //file.println();
 
             //file.print(FLAGSMARK);
             file.print( "&" );
             for( String flag : saveGameData.flags.getInactiveFlags( ) ) {
                 file.print( flag + ";" );
             }
             file.println( );
 
             file.print( VARMARK );
             for( String var : saveGameData.vars.getVarNames( ) ) {
                 file.print( var + ";" );
             }
             //file.println();
 
             //file.print(VARVALUEMARK);
             file.print( "&" );
             for( String value : saveGameData.vars.getVarValues( ) ) {
                 file.print( value + ";" );
             }
             file.println( );
 
             file.print( TIMERSMARK );
             long currentTime = System.currentTimeMillis( );
             for( SaveTimer saveT : saveGameData.timers.getTimers( ) ) {
                 boolean isAssessment = saveT.isAssessmentRule( );
                 file.print( saveT.getState( ) + "-" );
                 if( isAssessment )
                     file.print( "assess" + "-" );
                 else
                     file.print( saveT.getTimeUpdate( ) + "-" );
                 // store the time in second that has been 
                 file.print( ( ( currentTime / 1000 ) - saveT.getLastUpdate( ) ) + "-" );
                 if( isAssessment ) {
                     file.print( "0;" );
                 }
                 else {
                     file.print( "1;" );
                 }
 
             }
             file.print( "&" );
             file.println( );
             file.close( );
         }
         catch( FileNotFoundException e ) {
             saved = false;
         }
 
         return saved;
     }
 
     /**
      * Returns the loaded flags
      * 
      * @return the loaded flags
      */
     public FlagSummary getFlags( ) {
 
         return saveGameData.flags;
     }
 
     /**
      * Returns the loaded vars
      * 
      * @return the loaded vars
      */
     public VarSummary getVars( ) {
 
         return saveGameData.vars;
     }
 
     /**
      * Changes the flags to save
      * 
      * @param flags
      *            the flags to save
      */
     public void setFlags( FlagSummary flags ) {
 
         saveGameData.flags = flags;
     }
 
     /**
      * Changes the vars to save
      * 
      * @param vars
      *            the vars to save
      */
     public void setVars( VarSummary vars ) {
 
         saveGameData.vars = vars;
     }
 
     /**
      * Returns the loaded current scene id
      * 
      * @return the loaded current scene id
      */
     public String getIdScene( ) {
 
         return saveGameData.idScene;
     }
 
     /**
      * Changes the current scene id to save
      * 
      * @param idScene
      *            the current scene id to save
      */
     public void setIdScene( String idScene ) {
 
         saveGameData.idScene = idScene;
     }
 
     /**
      * Returns the loaded item summary
      * 
      * @return the loaded item summary
      */
     public ItemSummary getItemSummary( ) {
 
         return saveGameData.itemSummary;
     }
 
     /**
      * Changes the item summary to save
      * 
      * @param itemSummary
      *            the item summary to save
      */
     public void setItemSummary( ItemSummary itemSummary ) {
 
         saveGameData.itemSummary = itemSummary;
     }
 
     /**
      * Returns the loaded player's horizontal position
      * 
      * @return the loaded player's horizontal position
      */
     public float getPlayerX( ) {
 
         return saveGameData.playerX;
     }
 
     /**
      * Changes the player's horizontal position to save
      * 
      * @param playerX
      *            the player's horizontal position to save
      */
     public void setPlayerX( float playerX ) {
 
         saveGameData.playerX = playerX;
     }
 
     /**
      * Returns the loaded player's vertical position
      * 
      * @return the loaded player's vertical position
      */
     public float getPlayerY( ) {
 
         return saveGameData.playerY;
     }
 
     /**
      * Changes the player's vertical position to save
      * 
      * @param playerY
      *            the player's vertical position to save
      */
     public void setPlayerY( float playerY ) {
 
         saveGameData.playerY = playerY;
     }
 
     /**
      * Returns the loaded game title
      * 
      * @return the loaded game title
      */
     public String getTitle( ) {
 
         return saveGameData.title;
     }
 
     /**
      * Changes the game title to save
      * 
      * @param title
      *            the game title to save
      */
     public void setTitle( String title ) {
 
         saveGameData.title = title;
     }
 
     public String getSaveTime( ) {
 
         return saveGameData.saveTime;
     }
 
     public void setSaveTime( String saveTime ) {
 
         saveGameData.saveTime = saveTime;
     }
 
     public long getTotalTime( ) {
 
         return saveGameData.totalTime;
     }
 
     public void setTotalTime( long totalTime ) {
 
         saveGameData.totalTime = totalTime;
     }
 
     public int getChapter( ) {
 
         return saveGameData.chapter;
     }
 
     public void setChapter( int chapter ) {
 
         saveGameData.chapter = chapter;
     }
 
     public void setTimers( TimerManager timers ) {
 
         saveGameData.timers = timers;
     }
 
     public String[] getLoadTimers( ) {
 
         return saveGameData.loadTimers;
     }
 
     public int getVersionNumber( ) {
 
         return saveGameData.versionNumber;
     }
 
     public void setVersionNumber( int versionNumber ) {
 
         saveGameData.versionNumber = versionNumber;
     }
 
     public void setProjectName( String projectName ) {
 
         saveGameData.projectName = projectName;
     }
 
     public String getProjectName( ) {
 
         return saveGameData.projectName;
     }
 
     private class SaveGameData {
 
         private int versionNumber;
 
         private String projectName;
 
         private String title;
 
         private int chapter;
 
         private long totalTime;
 
         private String saveTime;
 
         private String idScene;
 
         private FlagSummary flags;
 
         private VarSummary vars;
 
         private ItemSummary itemSummary;
 
         private float playerX, playerY;
 
         private TimerManager timers;
 
         private String[] loadTimers;
 
     }
 
 }
