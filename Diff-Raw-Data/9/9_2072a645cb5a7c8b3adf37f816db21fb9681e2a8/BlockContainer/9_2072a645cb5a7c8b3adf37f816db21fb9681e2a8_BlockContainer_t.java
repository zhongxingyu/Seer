package fearlesscode;

 /**
  * Blokkot és a hozzátartozó pozíciót tömörítő wrapper osztály.
  */
 public class BlockContainer
 {
 	/**
 	 * A blokkohoz tartozó pozíció.
 	 */
 	private Position position;
 
 	/**
 	 * Az adott blokk.
 	 */
 	private Block block;
 
 	/**
 	 * Létrehoz egy BlockContainert a megadott paraméterekkel.
 	 * @param p A blokkhoz tartozó pozíció.
 	 * @param b Az adott blokk.
 	 */
 	public BlockContainer(Position p, Block b)
 	{
 		position=p;
 		block=b;
 	}
 	
 	/**
 	 * Position getter.
 	 */
 	public Position getPosition()
 	{
 		return this.position;
 	}
 	
 	/**
 	 * Block getter.
 	 */
 	public Block getBlock()
 	{
 		return this.block;
 	}
 	
 	/**
 	 * Position setter.
 	 */
 	public void setPosition(Position position)
 	{
 		this.position = position;
 	}
 	
 	/**
 	 * Block setter.
 	 */
 	public void setBlock(Block block)
 	{
 		this.block = block;
 	}
 }
