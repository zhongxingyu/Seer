 package es.darkhogg.bbcc2;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import es.darkhogg.gameboy.Palette;
 import es.darkhogg.gameboy.Sprite;
 import es.darkhogg.gameboy.SpriteInfo;
 import es.darkhogg.gameboy.Tile;
 
 /**
  * TODO Document this class
  * 
  * @author Daniel Escoz (Darkhogg)
  * @version 1.0
  */
 public final class BugsRom {
 
 	/**
 	 * Position of the first enemy group
 	 */
 	private static final int ENEMY_GROUPS_ADDR = 0x27D1;
 	
 	/**
 	 * Position of the first password
 	 */
 	private static final int PASSWORDS_ADDR = 0x2BF0;
 	
 	/**
 	 * Position of every level in game order, that is, level N will be on
 	 * the address stored on the component N-1 of this array.
 	 */
 	private static final int[] LEVELS_ADDRS = {
 		0x14016, 0x195F3, 0x1893A, 0x14CCF, 0x1D7B0, 0x186A7, 0x19E99, 0x14EE2,
 		0x1C3C7, 0x1C87A, 0x1D29D, 0x15548, 0x15215, 0x1A12C, 0x1C024, 0x16381,
 		0x15EEE, 0x1DB43, 0x1E869, 0x14169, 0x18BCD, 0x19060, 0x1463C, 0x1E1D6,
 		0x18014, 0x19806, 0x1585B, 0x1CC0D, 0x1ECFC
 	};
 
 	/**
 	 * Positions of the 7 main tilesets for the levels
 	 */
 	private static final int[] TILESETS_ADDRS = {
 		0x7FFE, 0x87FE, 0x8FFE, 0x97FE, 0x9FFE, 0xA7FE, 0xAFFE
 	};
 	
 	/**
 	 * Position of the item/bugs tileset
 	 */
 	private static final int ITEMS_TILESET_ADDR = 0xBFFE;
 	
 	/**
 	 * Positions of the 4 enemy tilesets
 	 */
 	private static final int[] ENEMY_TILESETS_ADDRS = {
 		0xDFFE, 0xE7FE, 0xEFFE, 0xF7FE
 	};
 	
 	/**
 	 * Default background palette
 	 */
 	public static final Palette LEVEL_PALETTE = new Palette(
 		Color.DARK_GRAY, Color.WHITE, Color.LIGHT_GRAY, Color.BLACK
 	);
 	
 	/**
 	 * Default sprite palette
 	 */
 	public static final Palette SPRITE_PALETTE = new Palette(
 		null, Color.BLACK, Color.LIGHT_GRAY, Color.WHITE
 	);
 	
 	/**
 	 * ROM contents
 	 */
 	private final ByteBuffer contents;
 
 	/**
 	 * A list of all the levels read from the ROM
 	 */
 	private final List<Level> levels;
 
 	/**
 	 * A list of all seven theme tilesets from the ROM
 	 */
 	private final List<List<Tile>> themeTilesets;
 	
 	/**
 	 * Tileset that contains all items and some other things
 	 */
 	private final List<Tile> itemTileset;
 	
 	/**
 	 * A list of all enemy tilesets
 	 */
 	private final List<List<Tile>> enemyTilesets;
 	
 	/**
 	 * A list of all seven theme spritesets from the ROM
 	 */
 	private final List<List<Sprite>> themeSpritesets;
 	
 	/**
 	 * Spriteset that contains all items
 	 */
 	private final List<Sprite> itemSpriteset;
 	
 	/**
 	 * List of the enemy spritesets
 	 */
 	private final List<List<Sprite>> enemySpritesets;
 	
 	/**
 	 * Sprite representing bugs bunny
 	 */
 	private final Sprite bugsSprite;
 	
 	/**
 	 * Creates a new BugsRom with the ROM contents given and parses it to get an
 	 * internal abstract representation of it.
 	 * 
 	 * @param contents The full ROM file
 	 */
 	private BugsRom ( ByteBuffer contents ) {
 		this.contents = contents;
 
 		try {
 			// Get the passwords
 			String[] passwords = new String[ 29 ];
 			contents.position( PASSWORDS_ADDR );
 			for ( int i = 0; i < 28; i++ ) {
 				StringBuilder sb = new StringBuilder();
 				for ( int j = 0; j < 4; j++ ) {
 					char chr = (char)( 'A' + ( contents.get() & 0xFF ) );
 					sb.append( chr );
 				}
 				passwords[ i ] = sb.toString();
 			}
 	
 			// Get the enemy groups
 			EnemyGroup[] groups = new EnemyGroup[ 29 ];
 			contents.position( ENEMY_GROUPS_ADDR );
 			for ( int i = 0; i < 29; i++ ) {
 				groups[ i ] = EnemyGroup.valueOf( contents.get() );
 			}
 	
 			// Get the levels (now, IN GAME ORDER - srsly!)
 			Level[] levels = new Level[ 29 ];
 			for ( int i = 0; i < 29; i++ ) {
 				contents.position( LEVELS_ADDRS[ i ] );
 				RomLevel rl = RomLevelFormat.getInstance().loadRomLevelFromBuffer(
					contents, i==28 );
 				levels[ i ] = new Level( rl, groups[ i ], passwords[ i ] );
 			}
 			
 			// Set the levels, at last
 			this.levels = Arrays.asList( levels );
 			
 			// Get the main theme tilesets
 			@SuppressWarnings( "unchecked" ) List<List<Tile>> themeTilesets =
 				Arrays.asList( (List<Tile>[]) new List[ 7 ] );
 			for ( int i = 0; i < 7; i++ ) {
 				contents.position( TILESETS_ADDRS[ i ] );
 				List<Tile> tiles = Arrays.asList( new Tile[ 128 ] );
 				for ( int j = 0; j < 128; j++ ) {
 					long low = contents.getLong();
 					long high = contents.getLong();
 					tiles.set( j, new Tile( low, high ) );
 				}
 				themeTilesets.set( i, tiles );
 			}
 			this.themeTilesets = themeTilesets;
 			
 			// Read the info
 			List<SpriteInfo> info = SpriteInfo.loadInfoList(
 				BugsRom.class.getResource( "/es/darkhogg/bbcc2/combo_info.txt" )
 			);
 			
 			// Create the combosets (a.k.a. themeSpritesets)
 			@SuppressWarnings( "unchecked" ) List<List<Sprite>> themeSpritesets =
 				Arrays.asList( (List<Sprite>[]) new List[ 7 ] );
 			for ( int i = 0; i < 7; i++ ) {
 				List<Sprite> sprites = Arrays.asList( new Sprite[ 256 ] );
 				
 				for ( int j = 0; j < sprites.size(); j++ ) {
 					List<Tile> tiles = themeTilesets.get( i );
 					Sprite spr = info.get( j ).generateSpriteFromTileSet( tiles );
 					sprites.set( j, spr );
 				}
 				
 				themeSpritesets.set( i, sprites );
 			}
 			
 			this.themeSpritesets = themeSpritesets;
 			
 			// Create the item tileset
 			contents.position( ITEMS_TILESET_ADDR );
 			List<Tile> itemTileset = Arrays.asList( new Tile[ 128 ] );
 			for ( int j = 0; j < 128; j++ ) {
 				long low = contents.getLong();
 				long high = contents.getLong();
 				itemTileset.set( j, new Tile( low, high ) );
 			}
 			this.itemTileset = itemTileset;
 			
 			// Read the info
 			List<SpriteInfo> itemInfo = SpriteInfo.loadInfoList(
 				BugsRom.class.getResource( "/es/darkhogg/bbcc2/item_info.txt" )
 			);
 			
 			// Create the item spriteset
 			List<Sprite> itemSpriteset = Arrays.asList( new Sprite[ itemInfo.size() ] );
 			for ( int j = 0; j < itemInfo.size(); j++ ) {
 				Sprite spr = itemInfo.get( j ).generateSpriteFromTileSet( itemTileset );
 				itemSpriteset.set( j, spr );
 			}
 			this.itemSpriteset = itemSpriteset;
 			
 			// Create the bugs bunny sprite (LOL)
 			bugsSprite = new Sprite( 2, 3 );
 			bugsSprite.setTileAt( 0, 0, itemTileset.get( 1 ) );
 			bugsSprite.setTileAt( 1, 0, itemTileset.get( 2 ) );
 			bugsSprite.setTileAt( 0, 1, itemTileset.get( 17 ) );
 			bugsSprite.setTileAt( 1, 1, itemTileset.get( 18 ) );
 			bugsSprite.setTileAt( 0, 2, itemTileset.get( 33 ) );
 			bugsSprite.setTileAt( 1, 2, itemTileset.get( 34 ) );
 			
 			// Create the enemy tilesets
 			@SuppressWarnings( "unchecked" ) List<List<Tile>> enemyTilesets =
 				Arrays.asList( (List<Tile>[]) new List[ 4 ] );
 			for ( int i = 0; i < 4; i++ ) {
 				contents.position( ENEMY_TILESETS_ADDRS[ i ] );
 				List<Tile> tiles = Arrays.asList( new Tile[ 128 ] );
 				for ( int j = 0; j < 128; j++ ) {
 					long low = contents.getLong();
 					long high = contents.getLong();
 					tiles.set( j, new Tile( low, high ) );
 				}
 				enemyTilesets.set( i, tiles );
 			}
 			this.enemyTilesets = enemyTilesets;
 			
 			// Load the enemy info
 			List<SpriteInfo> enemyInfo = SpriteInfo.loadInfoList(
 				BugsRom.class.getResource( "/es/darkhogg/bbcc2/enemy_info.txt" )
 			);
 			
 			// Create the enemy spritesets
 			@SuppressWarnings( "unchecked" ) List<List<Sprite>> enemySpritesets =
 				Arrays.asList( (List<Sprite>[]) new List[ 4 ] );
 			for ( int i = 0; i < 4; i++ ) {
 				List<Sprite> sprites = Arrays.asList( new Sprite[ enemyInfo.size() ] );
 				
 				for ( int j = 0; j < sprites.size(); j++ ) {
 					List<Tile> tiles = enemyTilesets.get( i );
 					Sprite spr = enemyInfo.get( j ).generateSpriteFromTileSet( tiles );
 					sprites.set( j, spr );
 				}
 				
 				enemySpritesets.set( i, sprites );
 			}
 			this.enemySpritesets = enemySpritesets;
 			
 		} catch ( IOException e ) {
 			System.err.println( "FATAL: Error while loading resources" );
 			e.printStackTrace();
 			System.exit( 1 );
 			
 			// The program should have ended before this
 			throw new AssertionError();
 		}
 	}
 
 	/**
 	 * Saves the whole ROM into a given file. This method writes into
 	 * the internal contents buffer to update the ROM contents and then copies
 	 * the buffer into the specified file.
 	 * 
 	 * @param romFile The file in which the ROM is going to be saved
 	 * @throws IOException If some I/O error occurs
 	 */
 	public void saveToFile ( File romFile )
 	throws IOException {
 		for ( int i = 0; i < levels.size(); i++ ) {
 			Level level = levels.get( i );
 			
 			// Save password
 			if ( level.getPassword() != null ) {
 				contents.position( PASSWORDS_ADDR + i*4 );
 				for ( int j = 0; j < 4; j++ ) {
 					contents.put( (byte)(level.getPassword().charAt( j ) - 'A' ) );
 				}
 			}
 			
 			// Save enemy group
 			contents.position( ENEMY_GROUPS_ADDR + i );
 			contents.put( level.getEnemyGroup().getValue() );
 			
 			// Save level data
 			contents.position( LEVELS_ADDRS[ i ] );
 			RomLevelFormat.getInstance().saveRomLevelToBuffer(
 				level.getRomLevel(), contents, i==28 );
 			
 		}
 		
 		// Write it all to a file
 		contents.position( 0 );
 		
 		FileChannel fc = null;
 		try {
 			fc = new FileOutputStream( romFile ).getChannel();
 			fc.write( contents );
 		} finally {
 			if ( fc != null ) {
 				fc.close();
 			}
 		}
 	}
 	
 	/**
 	 * Creates a BugsRom object from the contents of a file.
 	 * <p>
 	 * No checks are performed on the file to ensure it is the actual Bugs Bunny
 	 * Crazy Castle 2 ROM, but it will throw an IOException if something goes
 	 * wrong while reading it.
 	 * 
 	 * @param romFile File to load
 	 * @return A BugsRom object representing the contents of the file.
 	 * @throws IOException if something goes wrong while reading the file
 	 */
 	public static BugsRom loadFromFile ( File romFile )
 	throws IOException {
 		ByteBuffer contents = ByteBuffer.allocate( 128 * 1024 );
 
 		// Read the contents of the file
 		FileChannel fc = null;
 		try {
 			fc = new FileInputStream( romFile ).getChannel();
 			fc.read( contents );
 		} finally {
 			if ( fc != null ) {
 				fc.close();
 			}
 		}
 
 		// Return a new object
 		return new BugsRom( contents );
 	}
 	
 	/**
 	 * Returns the <i>num</i>-th level for this ROM. The level returned is the
 	 * representation of the (<i>num+1</i>)-th level in the game, as levels are
 	 * 0-indexed here but not in the game.
 	 * 
 	 * @param num The level number
 	 * @return The <i>num</i>-th level of this ROM
 	 */
 	public Level getLevel ( int num ) {
 		if ( num < 0 || num > 28 ) {
 			throw new IllegalArgumentException();
 		}
 		
 		return levels.get( num );
 	}
 	
 	/**
 	 * Sets the <i>num</i>-th level of this ROM to a new level. The actual
 	 * level this method modified is the (<i>num+1</i>)-th level in the game,
 	 * as levels are 0-indexed here but not in the game.
 	 * <p>
 	 * This method performs no checks in the <i>level</i> argument other than
 	 * <tt>null</tt>-check. This means that any constraint the ROM may have, 
 	 * like not being able to modify the total area of a level, need to be
 	 * checked by client code.
 	 * 
 	 * @param num The level number
 	 * @param level The new level
 	 * @return The old level in that position
 	 * @throws IllegalArgumentException If <i>num</i> is not in the range 0-28
 	 * @throws NullPointerException If <i>level</i> is <tt>null</t>
 	 */
 	public Level setLevel ( int num, Level level ) {
 		if ( num < 0 || num > 28 ) {
 			throw new IllegalArgumentException();
 		}
 		
 		if ( level == null ) {
 			throw new NullPointerException();
 		}
 		
 		return levels.set( num, level );
 	}
 	
 	/**
 	 * Returns the tileset used for the <i>num</i>-th theme of this ROM. The
 	 * returned list is unmodifiable. The <i>num</i> argument is compatible
 	 * with {@link Theme#getValue()}.
 	 * 
 	 * @param num Theme number which tileset is to be retrieved
 	 * @return The <i>num</i>-th tileset of the ROM
 	 */
 	public List<Tile> getThemeTileSet ( int num ) {
 		if ( num < 0 || num > 6 ) {
 			throw new IllegalArgumentException();
 		}
 		
 		return Collections.unmodifiableList( themeTilesets.get( num ) );
 	}
 	
 	/**
 	 * Changes the <i>num</i>-th theme tileset to the one given and returns the
 	 * old value as an unmodifiable list. The tileset passed will be
 	 * <i>copied</i>, so no reference to it is kept by this object.
 	 * <p>
 	 * Note that because this method needs to check if some element of the
 	 * given tileset is <tt>null</tt> and to copy the given tileset, it
 	 * performs in linear time. Because the size of a tileset is always 128, 
 	 * it could be considered constant time, but it is still not exactly fast.
 	 * <p>
 	 * <b>Note: This method is currently <i>not implemented</i>. It does,
 	 * however, return its correct value and throw the correct exceptions, but
 	 * it will not modify the tileset in any way.
 	 * 
 	 * @param num Theme number which tileset is to be modified
 	 * @param tileset The new tileset
 	 * @return The old tileset
 	 */
 	public List<Tile> setThemeTileSet( int num, List<Tile> tileset ) {
 		if ( num < 0 || num > 6 || tileset.size() != 128 ) {
 			throw new IllegalArgumentException();
 		}
 		
 		List<Tile> oldTileset = themeTilesets.get( num );
 		
 		Tile[] tiles = new Tile[ 128 ];
 		{
 			int i = 0;
 			for ( Iterator<Tile> it = tileset.iterator(); it.hasNext(); i++ ) {
 				Tile tile = it.next();
 				if ( tile == null ) {
 					throw new NullPointerException();
 				}
 				tiles[ i ] = tile;
 			}
 		}
 		
 		// TODO uncomment the next line to "implement" this method
 		// themeTilesets.set( num, Arrays.asList( tiles ) );
 		
 		return Collections.unmodifiableList( oldTileset );
 	}
 	
 	/**
 	 * Returns the comboset used for the <i>num</i>-th theme of this ROM. The
 	 * returned list is unmodifiable. The <i>num</i> argument is 
 	 * compatible with {@link Theme#getValue()}.
 	 * 
 	 * @param num Theme number which comboset is to be retrieved
 	 * @return The specified comboset
 	 */
 	public List<Sprite> getThemeSpriteSet ( int num ) {
 		if ( num < 0 || num >= 7 ) {
 			throw new IllegalArgumentException();
 		}
 		
 		return Collections.unmodifiableList( themeSpritesets.get( num ) );
 	}
 	
 	/**
 	 * Returns a list with the item tileset
 	 * 
 	 * @return The item tileset
 	 */
 	public List<Tile> getItemTileSet () {
 		return itemTileset;
 	}
 	
 	/**
 	 * Returns a list with all the item sprites
 	 * 
 	 * @return The item spriteset
 	 */
 	public List<Sprite> getItemSpriteSet () {
 		return itemSpriteset;
 	}
 	
 	public List<Tile> getEnemyTileSet ( int num ) {
 		if ( num < 0 || num >= 4 ) {
 			throw new IllegalArgumentException();
 		}
 		return enemyTilesets.get( num );
 	}
 
 	public List<Sprite> getEnemySpriteSet ( int num ) {
 		if ( num < 0 || num >= 4 ) {
 			throw new IllegalArgumentException();
 		}
 		return enemySpritesets.get( num );
 	}
 	
 	/**
 	 * Returns a single sprite that represents bugs bunny in the game.
 	 * 
 	 * @return A sprite of bugs bunny
 	 */
 	public Sprite getBugsSprite () {
 		return bugsSprite;
 	}
 	
 }
