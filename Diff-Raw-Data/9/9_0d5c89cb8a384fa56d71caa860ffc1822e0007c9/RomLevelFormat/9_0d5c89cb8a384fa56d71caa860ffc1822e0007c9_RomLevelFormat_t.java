 package es.darkhogg.bbcc2;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import es.darkhogg.util.IntVector;
 
 /**
  * Represents the format used by the original BBCC2 game internaly.
  * 
  * @author Daniel Escoz (Darkhogg)
  * @version 1.0.1
  */
 public final class RomLevelFormat extends LevelFormat {
 
 	/**
 	 * The singleton for this class
 	 */
 	private static final RomLevelFormat INSTANCE = new RomLevelFormat();
 	
 	/**
 	 * Private constructor to ensure singleton
 	 */
 	private RomLevelFormat () {}
 	
 	/**
 	 * Returns the singleton of this class
 	 * 
 	 * @return This class singleton
 	 */
 	public static RomLevelFormat getInstance () {
 		return INSTANCE;
 	}
 	
 	/**
 	 * Loads a RomLevel object from the given ByteBuffer in the original ROM
 	 * format.
 	 * 
 	 * @param buffer The buffer from which the RomLevel will be read
 	 * @param sp Whether this is the special level with only 15 DI's or not
 	 * @return The RomLevel that it represented in the buffer
 	 */
 	public RomLevel loadRomLevelFromBuffer ( ByteBuffer buffer, boolean sp ) {
 		// Load Theme & Size
 		Theme theme = Theme.valueOf( buffer.get() );
 		int width = ((int)buffer.get() ) & 0xFF;
 		int height = ((int)buffer.get() ) & 0xFF;
 		
 		int lvlDataPos = buffer.position();
 		buffer.position( lvlDataPos + width*height );
 		
 		// Load Weapon & Spawn
 		Weapon weapon = Weapon.valueOf( buffer.get() );
 		int spawnX = ((int)buffer.get() ) & 0xFF;
 		int spawnY = ((int)buffer.get() ) & 0xFF;
 		
 		RomLevel rl = new RomLevel(
 			theme, weapon,
 			new IntVector( width, height ),
 			new IntVector( spawnX, spawnY ),
 			sp
 		);
 		
 		// Load Items
 		for ( int i = 0; i < 16; i++ ) {
 			int itX = ((int)buffer.get())&0xFF;
 			int itY = ((int)buffer.get())&0xFF;
 			byte itVal = buffer.get();
 			ItemType itType = ItemType.valueOf( itVal );
 			if ( itType != null ) {
 				rl.getItems().add( new Item( itType, itX, itY ) );
 			}
 		}
 		
 		// Load Enemies
 		for ( int i = 0; i < 15; i++ ) {
 			int enX = ((int)buffer.get())&0xFF;
 			int enY = ((int)buffer.get())&0xFF;
 			byte enVal = buffer.get();
 			EnemyType enType = EnemyType.valueOf( enVal );
 			if ( enType != null ) {
 				rl.getEnemies().add( new Enemy( enType, enX, enY ) );
 			}
 		}
 		
 		// Load Door Items
 		for ( int i = 0; i < (sp?15:16); i++ ) {
 			int itX = ((int)buffer.get())&0xFF;
 			int itY = ((int)buffer.get())&0xFF;
 			byte itVal = buffer.get();
 			ItemType itType = ItemType.valueOf( itVal );
 			if ( itType != null ) {
 				rl.getDoorItems().add( new Item( itType, itX, itY ) );
 			}
 		}
 
 		// Load Data
 		byte[][] data = rl.getData();
 		buffer.position( lvlDataPos );
 		for ( int j = 0; j < height; j++ ) {
 			for ( int i = 0; i < width; i++ ) {
 				data[ i ][ j ] = buffer.get();
 			}
 		}
 		
 		return rl;
 	}
 	
 	/**
 	 * Loads a level to the given buffer using the original ROM format. The
 	 * returned level will use some default values for passwords and enemy
 	 * group.
 	 */
 	@Override
 	public Level loadLevelFromBuffer ( ByteBuffer buffer ) {
 		return new Level(
 			loadRomLevelFromBuffer( buffer, false ),
 			EnemyGroup.DAFFY,
 			null
 		);
 	}
 
 	/**
 	 * Saves a RomLevel to the given buffer using the original ROM format.
 	 * 
 	 * @param level The level to be saved
 	 * @param buffer The buffer in which the level will be saved
 	 * @param sp Whether this is the special level with only 15 DI's or not
 	 * @return The actual number of bytes written into the level
 	 */
 	public int saveRomLevelToBuffer ( RomLevel level, ByteBuffer buffer, boolean sp ) {
 		int pos = buffer.position();
 		int entPos = 6 + level.getSize().getX()*level.getSize().getY() + pos;
 		int n;
 		byte[] threeZeros = { (byte)0, (byte)0, (byte)0 };
 		
 		// Theme & Size
 		int bytes = 3;
 		buffer.put( level.getTheme().getValue() );
 		buffer.put( (byte)level.getSize().getX() );
 		buffer.put( (byte)level.getSize().getY() );
 		
 		// Data
 		bytes += level.getSize().getX() * level.getSize().getY();
 		byte[][] data = level.getData();
 		for ( int j = 0; j < level.getSize().getY(); j++ ) {
 			for ( int i = 0; i < level.getSize().getX(); i++ ) {
 				buffer.put( data[ i ][ j ] );
 			}
 		}
 		
 		// Weapon & Spawn
 		bytes += 3;
 		buffer.put( level.getWeapon().getValue() );
 		buffer.put( (byte)level.getSpawn().getX() );
 		buffer.put( (byte)level.getSpawn().getY() );
 		
 		// Items
 		buffer.position( entPos );
 		n = 0;
 		for ( Item item : level.getItems() ) {
 			bytes += 3;
 			buffer.put( (byte)item.getX() );
 			buffer.put( (byte)item.getY() );
 			buffer.put( item.getType().getValue() );
 			n++;
 		}
 		while ( n < level.getItems().maxSize() ) {
 			bytes += 3;
 			buffer.put( threeZeros );
 			n++;
 		}
 		
 		// Enemies
 		buffer.position( entPos + 16*3 );
 		n = 0;
 		for ( Enemy enemy : level.getEnemies() ) {
 			bytes += 3;
 			buffer.put( (byte)enemy.getX() );
 			buffer.put( (byte)enemy.getY() );
 			buffer.put( enemy.getType().getValue() );
 		}
 		while ( n < level.getEnemies().maxSize() ) {
 			bytes += 3;
 			buffer.put( threeZeros );
 			n++;
 		}
 		
 		// Door Items
 		buffer.position( entPos + 31*3 );
 		n = 0;
 		int i = 0;
 		for ( Item item : level.getDoorItems() ) {
 			if ( i < 15 || !sp ) {
 				bytes += 3;
 				buffer.put( (byte)item.getX() );
 				buffer.put( (byte)item.getY() );
 				buffer.put( item.getType().getValue() );
 			}
 			i++;
 			n++;
 		}
		while ( n < level.getDoorItems().maxSize() && ( n < 15 || !sp ) ) {
 			bytes += 3;
 			buffer.put( threeZeros );
 			n++;
 		}
 		
 		return bytes;
 	}
 	
 	/**
 	 * Saves a level to the given buffer using the original ROM format. The
 	 * password and enemy group of this level are completely ignored.
 	 */
 	@Override
 	public int saveLevelToBuffer ( Level level, ByteBuffer buffer ) {
 		return saveRomLevelToBuffer( level.getRomLevel(), buffer, false );
 	}
 	
 	/**
 	 * Saves a level to a file using the original ROM format. The password and
 	 * enemy group of this level are completely ignored.
 	 */
 	@Override
 	public int saveLevelToFile ( Level level, File file )
 	throws IOException {
 		return saveLevelToFile( level, file,
 			level.getRomLevel().getSize().getX()*level.getRomLevel().getSize().getY()
 			+ (16*3+1)*3);
 	}
 
 }
