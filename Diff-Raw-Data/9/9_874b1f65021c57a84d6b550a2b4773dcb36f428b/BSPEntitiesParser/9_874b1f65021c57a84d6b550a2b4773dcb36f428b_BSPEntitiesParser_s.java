 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.loaders.models.bsp;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.openmali.vecmath2.Colorf;
 import org.openmali.vecmath2.Point3f;
 import org.openmali.vecmath2.Tuple3f;
 
 /**
  * Insert type comment here.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class BSPEntitiesParser
 {
     private static final String qs( String s )
     {
         if ( s == null )
             return( null );
         
         return( "\"" + s + "\"" );
     }
     
     public static abstract class BSPEntity
     {
         public final String className;
         public final String className2;
         
         protected String getFieldsString()
         {
             return( "    className = " + qs( className ) + "\n" +
                     "    className2 = " + qs( className2 ) + "\n"
                   );
         }
         
         @Override
         public String toString()
         {
             return( this.getClass().getSimpleName() + "\n{\n" + getFieldsString() + "}" );
         }
         
         protected static final String parseLineValue( String line )
         {
             int spacePos = line.indexOf( "\" \"" );
             
             if ( spacePos < 0 )
                 return( null );
             
             return( line.substring( spacePos + 3, line.length() - 1 ) );
         }
         
         protected abstract void parseLine( String line );
         
         protected void parse( BufferedReader br ) throws IOException
         {
             String line;
             while ( true )
             {
                 line = br.readLine();
                 
                 if ( line.indexOf( "}" ) >= 0 )
                     break;
                 
                 //System.out.println( line );
                 
                 if ( line.length() > 0 )
                 {
                     /*
                     if ( line.startsWith( "\"classname\"" ) )
                     {
                         this.className2 = parseLineValue( line );
                     }
                     else
                     */
                     {
                         parseLine( line );
                     }
                 }
             }
         }
         
         public BSPEntity( String className, String className2 )
         {
             this.className = className;
             this.className2 = className2;
         }
     }
     
     public static class BSPEntity_worldspawn extends BSPEntity
     {
         public String[] wadsFull;
         public String[] wads;
         public String chapterTitle;
         public String skyName = null;
         public String message = null;
         public int gameTitle;
         public int startDark;
         public int maxRange;
         public int light;
         public int sounds;
         
         private String getWadsFullString()
         {
             if ( wadsFull == null )
                 return( null );
             
             String s = "{ ";
             for ( int i = 0; i < wadsFull.length; i++ )
             {
                 if ( i > 0 )
                     s += ", ";
                 
                 s += qs( wadsFull[i] );
             }
             
             s += " }";
             
             return( s );
         }
         
         private String getWadsString()
         {
             if ( wads == null )
                 return( null );
             
             String s = "{ ";
             for ( int i = 0; i < wads.length; i++ )
             {
                 if ( i > 0 )
                     s += ", ";
                 
                 s += qs( wads[i] );
             }
             
             s += " }";
             
             return( s );
         }
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    wadsFull = " + getWadsFullString() + "\n" +
                     "    wads = " + getWadsString() + "\n" +
                     "    chapterTitle = " + qs( chapterTitle ) + "\n" +
                     "    skyName = " + qs( skyName ) + "\n" +
                     "    message = " + qs( message ) + "\n" +
                     "    gameTitle = " + gameTitle + "\n" +
                     "    startDark = " + startDark + "\n" +
                     "    maxRange = " + maxRange + "\n" +
                     "    light = " + light + "\n" +
                     "    sounds = " + sounds + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"wad\"" ) )
             {
                 this.wadsFull = parseLineValue( line ).split( ";" );
                 this.wads = new String[ wadsFull.length ];
                 for ( int i = 0; i < wadsFull.length; i++ )
                 {
                     wadsFull[i] = wadsFull[i].replace( '\\', '/' );
                     int lastSlashPos = wadsFull[i].lastIndexOf( '/' );
                     if ( lastSlashPos >= 0 )
                         wads[i] = wadsFull[i].substring( lastSlashPos + 1 );
                     else
                         wads[i] = wadsFull[i];
                 }
             }
             else if ( line.startsWith( "\"chaptertitle\"" ) )
             {
                 this.chapterTitle = parseLineValue( line );
             }
             else if ( line.startsWith( "\"skyname\"" ) )
             {
                 this.skyName = parseLineValue( line );
             }
             else if ( line.startsWith( "\"message\"" ) )
             {
                 this.message = parseLineValue( line );
             }
             else if ( line.startsWith( "\"gametitle\"" ) )
             {
                 this.gameTitle = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"startdark\"" ) )
             {
                 this.startDark = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"MaxRange\"" ) )
             {
                 this.maxRange = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"linght\"" ) )
             {
                 this.light = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"sounds\"" ) )
             {
                 this.sounds = Integer.parseInt( parseLineValue( line ) );
             }
         }
         
         public BSPEntity_worldspawn( String className2 )
         {
             super( "worldspawn", className2 );
         }
     }
     
     public static class BSPEntity_Location extends BSPEntity
     {
         /*
          * info_player_start
          * info_player_start2
          * info_player_deathmatch
          * weapon_*
          * ammo_*
          * item_*
          */
         
         public Point3f origin;
         public Tuple3f angles = new Tuple3f( 0f, 0f, 0f );
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    origin = " + origin + "\n" +
                     "    angles = " + angles + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"origin\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.origin = new Point3f( Float.parseFloat( v[0] ), Float.parseFloat( v[1] ), Float.parseFloat( v[2] ) );
             }
             else if ( line.startsWith( "\"angles\"" ) )
             {
                 // HL
                 
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.angles.set( Float.parseFloat( v[0] ), Float.parseFloat( v[1] ), Float.parseFloat( v[2] ) );
             }
             else if ( line.startsWith( "\"angle\"" ) )
             {
                 // Q3
                 
                 // The x-angle is the one, that will rotate around the up-vector.
                this.angles.set( Float.parseFloat( parseLineValue( line ) ), 0f, 0f );
             }
         }
         
         public BSPEntity_Location( String className2 )
         {
             super( "location", className2 );
         }
         
         public BSPEntity_Location( String className, String className2 )
         {
             super( className, className2 );
         }
     }
     
     public static class BSPEntity_info extends BSPEntity_Location
     {
         /*
          * info_player_start
          * info_player_start2
          * info_player_deathmatch
          */
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
         }
         
         public BSPEntity_info( String className2 )
         {
             super( "info", className2 );
         }
     }
     
     public static class BSPEntity_weapon extends BSPEntity_Location
     {
         /*
          * weapon_*
          */
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
         }
         
         public BSPEntity_weapon( String className2 )
         {
             super( "weapon", className2 );
         }
     }
     
     public static class BSPEntity_ammo extends BSPEntity_Location
     {
         /*
          * ammo_*
          */
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
         }
         
         public BSPEntity_ammo( String className2 )
         {
             super( "ammo", className2 );
         }
     }
     
     public static class BSPEntity_item extends BSPEntity_Location
     {
         /*
          * item_*
          */
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
         }
         
         public BSPEntity_item( String className2 )
         {
             super( "item", className2 );
         }
     }
     
     public static class BSPEntity_light extends BSPEntity
     {
         public Point3f origin;
         public int style = -1;
         public Colorf lightColor;
         public float _light;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    origin = " + origin + "\n" +
                     "    style = " + style + "\n" +
                     "    lightColor = " + lightColor + "\n" +
                     "    _light = " + _light + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"origin\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.origin = new Point3f( Float.parseFloat( v[0] ), Float.parseFloat( v[1] ), Float.parseFloat( v[2] ) );
             }
             else if ( line.startsWith( "\"style\"" ) )
             {
                 this.style = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"_color\"" ) )
             {
                 // Q3
                 
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.lightColor = new Colorf( Float.parseFloat( v[0] ), Float.parseFloat( v[1] ), Float.parseFloat( v[2] ) );
             }
             else if ( line.startsWith( "\"light\"" ) )
             {
                 // Q3
                 
                 this._light = Float.parseFloat( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"_light\"" ) )
             {
                 // HL
                 
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.lightColor = new Colorf( Float.parseFloat( v[0] ) / 255f, Float.parseFloat( v[1] ) / 255f, Float.parseFloat( v[2] ) / 255f );
                 if ( v.length > 3 )
                     this._light = Float.parseFloat( v[3] );
             }
         }
         
         public BSPEntity_light( String className2 )
         {
             super( "light", className2 );
         }
     }
     
     public static class BSPEntity_func_pushable extends BSPEntity
     {
         public String model;
         public int delay;
         public int explosion;
         public int material;
         public int health;
         public int renderMode;
         public int renderFX;
         public int buoyancy;
         public int friction;
         public int size;
         public Colorf renderColor;
         public int renderAMT;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    model = " + qs( model ) + "\n" +
                     "    delay = " + delay + "\n" +
                     "    explosion = " + explosion + "\n" +
                     "    material = " + material + "\n" +
                     "    health = " + health + "\n" +
                     "    renderMode = " + renderMode + "\n" +
                     "    renderFX = " + renderFX + "\n" +
                     "    buoyancy = " + buoyancy + "\n" +
                     "    friction = " + friction + "\n" +
                     "    size = " + size + "\n" +
                     "    renderColor = " + renderColor + "\n" +
                     "    renderAMT = " + renderAMT + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"model\"" ) )
             {
                 this.model = parseLineValue( line );
             }
             else if ( line.startsWith( "\"delay\"" ) )
             {
                 this.delay = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"explosion\"" ) )
             {
                 this.explosion = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"material\"" ) )
             {
                 this.material = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"health\"" ) )
             {
                 this.health = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"rendermode\"" ) )
             {
                 this.renderMode = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"renderfx\"" ) )
             {
                 this.renderFX = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"buoyancy\"" ) )
             {
                 this.buoyancy = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"friction\"" ) )
             {
                 this.friction = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"size\"" ) )
             {
                 this.size = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"rendercolor\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 if ( v.length == 3 )
                 {
                     this.renderColor = new Colorf( Float.parseFloat( v[0] ) / 255f, Float.parseFloat( v[1] ) / 255f, Float.parseFloat( v[2] ) / 255f );
                 }
                 else
                 {
                     float value = Float.parseFloat( v[0] ) / 255f;
                     this.renderColor = new Colorf( value, value, value );
                 }
             }
             else if ( line.startsWith( "\"renderamt\"" ) )
             {
                 this.renderAMT = Integer.parseInt( parseLineValue( line ) );
             }
         }
         
         public BSPEntity_func_pushable( String className2 )
         {
             super( "func_pushable", className2 );
         }
     }
     
     public static class BSPEntity_func_plat extends BSPEntity
     {
         public String model;
         public int _minLight;
         public int height;
         public float volume;
         public int stopSound;
         public int moveSound;
         public Colorf renderColor;
         public int renderAMT;
         public int renderMode;
         public int renderFX;
         public float angle;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    model = " + qs( model ) + "\n" +
                     "    _minLight = " + _minLight + "\n" +
                     "    height = " + height + "\n" +
                     "    volume = " + volume + "\n" +
                     "    stopSound = " + stopSound + "\n" +
                     "    moveSound = " + moveSound + "\n" +
                     "    renderColor = " + renderColor + "\n" +
                     "    renderAMT = " + renderAMT + "\n" +
                     "    renderMode = " + renderMode + "\n" +
                     "    renderFX = " + renderFX + "\n" +
                     "    angle = " + angle + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"model\"" ) )
             {
                 this.model = parseLineValue( line );
             }
             else if ( line.startsWith( "\"_minlight\"" ) )
             {
                 this._minLight = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"height\"" ) )
             {
                 this.height = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"volume\"" ) )
             {
                 this.volume = Float.parseFloat( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"stopsnd\"" ) )
             {
                 this.stopSound = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"movesnd\"" ) )
             {
                 this.moveSound = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"rendercolor\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.renderColor = new Colorf( Float.parseFloat( v[0] ) / 255f, Float.parseFloat( v[1] ) / 255f, Float.parseFloat( v[2] ) / 255f );
             }
             else if ( line.startsWith( "\"renderamt\"" ) )
             {
                 this.renderAMT = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"rendermode\"" ) )
             {
                 this.renderMode = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"renderfx\"" ) )
             {
                 this.renderFX = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"angle\"" ) )
             {
                 this.angle = Float.parseFloat( parseLineValue( line ) );
             }
         }
         
         public BSPEntity_func_plat( String className2 )
         {
             super( "func_plat", className2 );
         }
     }
     
     public static class BSPEntity_func_ladder extends BSPEntity
     {
         public String model;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    model = " + qs( model ) + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"model\"" ) )
             {
                 this.model = parseLineValue( line );
             }
         }
         
         public BSPEntity_func_ladder( String className2 )
         {
             super( "func_ladder", className2 );
         }
     }
     
     public static class BSPEntity_func_wall extends BSPEntity
     {
         public String model;
         public Colorf renderColor;
         public int renderAMT;
         public int renderMode;
         public int renderFX;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    model = " + qs( model ) + "\n" +
                     "    renderColor = " + renderColor + "\n" +
                     "    renderAMT = " + renderAMT + "\n" +
                     "    renderMode = " + renderMode + "\n" +
                     "    renderFX = " + renderFX + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"model\"" ) )
             {
                 this.model = parseLineValue( line );
             }
             else if ( line.startsWith( "\"rendercolor\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 if ( v.length == 3 )
                 {
                     this.renderColor = new Colorf( Float.parseFloat( v[0] ) / 255f, Float.parseFloat( v[1] ) / 255f, Float.parseFloat( v[2] ) / 255f );
                 }
                 else
                 {
                     float value = Float.parseFloat( v[0] ) / 255f;
                     this.renderColor = new Colorf( value, value, value );
                 }
             }
             else if ( line.startsWith( "\"renderamt\"" ) )
             {
                 this.renderAMT = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"rendermode\"" ) )
             {
                 this.renderMode = Integer.parseInt( parseLineValue( line ) );
             }
             else if ( line.startsWith( "\"renderfx\"" ) )
             {
                 this.renderFX = Integer.parseInt( parseLineValue( line ) );
             }
         }
         
         public BSPEntity_func_wall( String className2 )
         {
             super( "func_wall", className2 );
         }
     }
     
     public static class BSPEntity_misc_model extends BSPEntity_Location
     {
         public String model;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    model = " + qs( model ) + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
             
             if ( line.startsWith( "\"model\"" ) )
             {
                 this.model = parseLineValue( line );
             }
         }
         
         public BSPEntity_misc_model( String className2 )
         {
             super( "misc_model", className2 );
         }
     }
     
     public static class BSPEntity_decal_texture extends BSPEntity
     {
         /*
          * infodecal
          */
         
         public Point3f origin;
         public String texture;
         public float angle;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    origin = " + origin + "\n" +
                     "    texture = " + qs( texture ) + "\n" +
                     "    angle = " + angle + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"origin\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.origin = new Point3f( Float.parseFloat( v[0] ), Float.parseFloat( v[1] ), Float.parseFloat( v[2] ) );
             }
             else if ( line.startsWith( "\"texture\"" ) )
             {
                 this.texture = parseLineValue( line );
             }
             else if ( line.startsWith( "\"angle\"" ) )
             {
                 this.angle = Float.parseFloat( parseLineValue( line ) );
             }
         }
         
         public BSPEntity_decal_texture( String className2 )
         {
             super( "decal_texture", className2 );
         }
     }
     
     public abstract static class BSPEntity_target extends BSPEntity
     {
         public Point3f origin;
         public String targetName;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    origin = " + origin + "\n" +
                     "    targetName = " + qs( targetName ) + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             if ( line.startsWith( "\"origin\"" ) )
             {
                 String[] v = parseLineValue( line ).split( " " );
                 
                 this.origin = new Point3f( Float.parseFloat( v[0] ), Float.parseFloat( v[1] ), Float.parseFloat( v[2] ) );
             }
             else if ( line.startsWith( "\"targetname\"" ) )
             {
                 this.targetName = parseLineValue( line );
             }
         }
         
         public BSPEntity_target( String className, String className2 )
         {
             super( className, className2 );
         }
     }
     
     public static class BSPEntity_target_push extends BSPEntity_target
     {
         public String target;
         public int spawnFlags;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    target = " + qs( target ) + "\n" +
                     "    spawnFlags = " + spawnFlags + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
             
             if ( line.startsWith( "\"target\"" ) )
             {
                 this.target = parseLineValue( line );
             }
             else if ( line.startsWith( "\"spawnflags\"" ) )
             {
                 this.spawnFlags = Integer.parseInt( parseLineValue( line ) );
             }
         }
         
         public BSPEntity_target_push( String className2 )
         {
             super( "target_push", className2 );
         }
     }
     
     public static class BSPEntity_target_position extends BSPEntity_target
     {
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
         }
         
         public BSPEntity_target_position( String className2 )
         {
             super( "target_position", className2 );
         }
     }
     
     public abstract static class BSPEntity_trigger extends BSPEntity
     {
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLineValue( line );
         }
         
         public BSPEntity_trigger( String className, String className2 )
         {
             super( className, className2 );
         }
     }
     
     public static class BSPEntity_trigger_multiple extends BSPEntity_trigger
     {
         public String model;
         
         @Override
         protected String getFieldsString()
         {
             return( super.getFieldsString() +
                     "    model = " + qs( model ) + "\n"
                   );
         }
         
         @Override
         protected void parseLine( String line )
         {
             super.parseLine( line );
             
             if ( line.startsWith( "\"model\"" ) )
             {
                 this.model = parseLineValue( line );
             }
         }
         
         public BSPEntity_trigger_multiple( String className2 )
         {
             super( "trigger_multiple", className2 );
         }
     }
     
     public static class BSPEntity_unknwown extends BSPEntity
     {
         @Override
         protected void parseLine( String line )
         {
             //System.out.println( line );
         }
         
         public BSPEntity_unknwown( String className2 )
         {
             super( "unknown", className2 );
         }
     }
     
     public static BSPEntity[] parseEntites( byte[] entityBytes )
     {
         BufferedReader br = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( entityBytes ) ) );
         
         ArrayList<BSPEntity> entities = new ArrayList<BSPEntity>();
         
         try
         {
             while ( br.ready() )
             {
                 // Find beginning of the next block...
                 while ( br.ready() && ( (char)br.read() != '{' ) ) {};
                 
                 br.mark( 4096 );
                 
                 boolean blockValid = true;
                 
                 // Read the entire block and record the classname...
                 String line;
                 String classname = null;
                 do
                 {
                     line = br.readLine();
                     
                     if ( line == null )
                     {
                         blockValid = false;
                         break;
                     }
                     
                     if ( line.startsWith( "\"classname\"" ) )
                     {
                         classname = BSPEntity.parseLineValue( line );
                         
                         if ( classname == null )
                         {
                             blockValid = false;
                             break;
                         }
                     }
                 }
                 while ( !line.startsWith( "}" ) );
                 
                 if ( blockValid )
                 {
                     br.reset();
                     
                     BSPEntity entity = null;
                     
                     //System.out.println( classname );
                     
                     if ( classname.startsWith( "worldspawn" ) )
                     {
                         entity = new BSPEntity_worldspawn( classname );
                     }
                     else if ( classname.startsWith( "info_" ) )
                     {
                         entity = new BSPEntity_info( classname );
                     }
                     else if ( classname.startsWith( "weapon_" ) )
                     {
                         entity = new BSPEntity_weapon( classname );
                     }
                     else if ( classname.startsWith( "ammo_" ) )
                     {
                         entity = new BSPEntity_ammo( classname );
                     }
                     else if ( classname.startsWith( "item_" ) )
                     {
                         entity = new BSPEntity_item( classname );
                     }
                     else if ( classname.startsWith( "holdable_teleporter" ) )
                     {
                         entity = new BSPEntity_Location( classname );
                     }
                     else if ( classname.startsWith( "misc_model" ) )
                     {
                         entity = new BSPEntity_misc_model( classname );
                     }
                     else if ( classname.startsWith( "light" ) )
                     {
                         entity = new BSPEntity_light( classname );
                     }
                     else if ( classname.startsWith( "func_" ) )
                     {
                         if ( classname.startsWith( "func_pushable" ) )
                         {
                             entity = new BSPEntity_func_pushable( classname );
                         }
                         else if ( classname.startsWith( "func_plat" ) )
                         {
                             entity = new BSPEntity_func_plat( classname );
                         }
                         else if ( classname.startsWith( "func_ladder" ) )
                         {
                             entity = new BSPEntity_func_ladder( classname );
                         }
                         else if ( classname.startsWith( "func_wall" ) )
                         {
                             entity = new BSPEntity_func_wall( classname );
                         }
                     }
                     else if ( classname.startsWith( "infodecal" ) )
                     {
                         entity = new BSPEntity_decal_texture( classname );
                     }
                     else if ( classname.startsWith( "target_" ) )
                     {
                         if ( classname.startsWith( "target_push" ) )
                         {
                             entity = new BSPEntity_target_push( classname );
                         }
                         else if ( classname.startsWith( "target_position" ) )
                         {
                             entity = new BSPEntity_target_position( classname );
                         }
                     }
                     else if ( classname.startsWith( "trigger_" ) )
                     {
                         if ( classname.startsWith( "trigger_multiple" ) )
                         {
                             entity = new BSPEntity_trigger_multiple( classname );
                         }
                     }
                     else
                     {
                         new BSPEntity_unknwown( classname ).parse( br );
                     }
                     
                     if ( entity == null )
                     {
                         //System.out.println( "Unrecognized entity classname \"" + classname + "\"." );
                     }
                     else
                     {
                         entity.parse( br );
                         
                         entities.add( entity );
                     }
                 }
             }
         }
         catch ( IOException e )
         {
             e.printStackTrace();
         }
         
         return( entities.toArray( new BSPEntity[ entities.size() ] ) );
     }
 }
