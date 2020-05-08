 package es.darkhogg.bbcc2;
 
 import java.util.Iterator;
 
 import es.darkhogg.util.IntVector;
 
 /**
  * Represents a level from BBCC2 as encountered in the original ROM
  * 
  * @author Daniel Escoz (Darkhogg)
  * @version 1.0
  */
 public final class RomLevel {
 	
 	private Theme theme;
 	private Weapon weapon;
 	
 	private IntVector size;
 	private IntVector spawn;
 	
 	private final EntityCollection<Item> items;
 	private final EntityCollection<Enemy> enemies;
 	private final EntityCollection<Item> doorItems;
 	
 	private byte[][] data;
 	
 	public RomLevel (
 		Theme theme, Weapon weapon, IntVector size,
 		IntVector spawn, boolean isLastLevel
 	) {
 		setTheme( theme );
 		setWeapon( weapon );
 		setSize( size );
 		setSpawn( spawn );
 		
 		this.items = new EntityCollection<Item>( 16 );
 		this.enemies = new EntityCollection<Enemy>( 15 );
 		this.doorItems = new EntityCollection<Item>( isLastLevel?15:16 );
 		
 		data = new byte[ size.getX() ][ size.getY() ];
 	}
 	
 	public RomLevel (
 		Theme theme, Weapon weapon, IntVector size, IntVector spawn
 	) {
 		this( theme, weapon, size, spawn, false );
 	}
 	
 	public RomLevel ( RomLevel romLevel ) {
 		theme = romLevel.theme;
 		weapon = romLevel.weapon;
 		size = romLevel.size;
 		spawn = romLevel.spawn;
 
 		items = new EntityCollection<Item>( romLevel.items );
 		enemies = new EntityCollection<Enemy>( romLevel.enemies );
 		doorItems = new EntityCollection<Item>( romLevel.doorItems );
 		
 		data = new byte[ size.getX() ][ size.getY() ];
 		for ( int i = 0; i < size.getX(); i++ ) {
 			for ( int j = 0; j < size.getY(); j++ ) {
 				data[ i ][ j ] = romLevel.data[ i ][ j ];
 			}
 		}
 	}
 
 	public Theme getTheme () {
 		return theme;
 	}
 	
 	public void setTheme ( Theme theme ) {
 		if ( theme == null ) {
 			throw new NullPointerException();
 		}
 		this.theme = theme;
 	}
 	
 	public Weapon getWeapon () {
 		return weapon;
 	}
 	
 	public void setWeapon ( Weapon weapon ) {
 		if ( weapon == null ) {
 			throw new NullPointerException();
 		}
 		this.weapon = weapon;
 	}
 	
 	public IntVector getSize () {
 		return size;
 	}
 	
 	public void setSize ( IntVector size ) {
 		if ( size == null ) {
 			throw new NullPointerException();
 		}
 		
 		if ( size.getX() < 0 || size.getX() > 255
 		  || size.getY() < 0 || size.getY() > 255
 		) {
 			throw new IllegalArgumentException();
 		}
 		
 		if ( this.size != null ) { // This call is NOT from the constructor
 			// Modify the data array
 			byte[][] newData = new byte[ size.getX() ][ size.getY() ];
 			
 			int minX = Math.min( size.getX(), this.size.getX() );
 			int minY = Math.min( size.getY(), this.size.getY() );
 			for ( int i = 0; i < minX; i++ ) {
 				for ( int j = 0; j < minY; j++ ) {
 					newData[ i ][ j ] = data[ i ][ j ];
 				}
 			}
 			data = newData;
 			
 			// Remove items outside the new size
 			for ( Iterator<Item> it = items.iterator(); it.hasNext(); ) {
 				Item item = it.next();
 				if ( item.getX() >= size.getX() || item.getY() >= size.getY() ) {
 					it.remove();
 				}
 			}
 			
 			// Same with enemies
 			for ( Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
 				Enemy enem = it.next();
 				if ( enem.getX() >= size.getX() || enem.getY() >= size.getY() ) {
 					it.remove();
 				}
 			}
 			
 			// Same with door items
 			for ( Iterator<Item> it = doorItems.iterator(); it.hasNext(); ) {
 				Item ditem = it.next();
 				if ( ditem.getX() >= size.getX() || ditem.getY() >= size.getY() ) {
 					it.remove();
 				}
 			}
 			
 			// Move the spawn point if it's now outside the level
 			spawn = new IntVector(
 				Math.min( spawn.getX(), size.getX()-1 ),
 				Math.min( spawn.getY(), size.getY()-1 )
 			);
 		}
 		
 		// Set the new size
 		this.size = size;
 	}
 	
 	public IntVector getSpawn () {
 		return spawn;
 	}
 	
 	public void setSpawn ( IntVector spawn ) {
 		if ( spawn == null ) {
 			throw new NullPointerException();
 		}
 		if ( spawn.getX() < 0 || spawn.getX() >= size.getX()
 		  || spawn.getY() < 0 || spawn.getY() >= size.getY()
 		) {
			throw new IllegalArgumentException();
 		}
 		this.spawn = spawn;
 	}
 
 	public EntityCollection<Item> getItems () {
 		return items;
 	}
 	
 	public EntityCollection<Enemy> getEnemies () {
 		return enemies;
 	}
 	
 	public EntityCollection<Item> getDoorItems () {
 		return doorItems;
 	}
 	
 	public byte[][] getData () {
 		return data;
 	}
 
 	public void fixWeaponItems () {
 		// TODO Implement this
 	}
 	
 	@Override
 	public String toString () {
 		StringBuilder sb = new StringBuilder( "es.darkhogg.bbcc2.RomLevel{" );
 
 		sb.append( "Theme:" );
 		sb.append( theme );
 		
 		sb.append( ";Size:" );
 		sb.append( size.getX() );
 		sb.append( ',' );
 		sb.append( size.getY() );
 		
 		sb.append( ";Weapon:" );
 		sb.append( weapon );
 
 		sb.append( ";Spawn:" );
 		sb.append( spawn.getX() );
 		sb.append( ',' );
 		sb.append( spawn.getY() );
 		
 		sb.append( "}" );
 		return sb.toString();
 	}
 }
