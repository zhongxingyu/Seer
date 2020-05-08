 package map;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import utils.MathUtils;
 import exceptions.SpriteException;
 
 /**
  * @author Razican (Iban Eguia)
  */
 public class Map implements Serializable {
 
 	private static final long					serialVersionUID	= 1630051961654828210L;
 	private BufferedImage						image;
 	private Square[][]							squares;
 	private int									width, height;
 	private HashMap<Entry<Byte, Byte>, Link>	links;
 
 	/**
 	 * @param width Map's width
 	 * @param height Map's height
 	 * @throws SpriteException if the sprite is not set
 	 */
 	public Map(int width, int height) throws SpriteException
 	{
 		this.squares = new Square[height][width];
 		this.width = width;
 		this.height = height;
 		links = new HashMap<>();
 		this.image = new BufferedImage(width * 32, height * 32,
 		BufferedImage.TYPE_INT_RGB);
 
 		Graphics2D g = this.image.createGraphics();
 		g.setPaint(Color.WHITE);
 		g.fillRect(0, 0, this.image.getWidth(), this.image.getHeight());
 	}
 
 	/**
 	 * Sets the square in specified coordinates to match specified square
 	 * 
 	 * @param x - X coordinate for the new square
 	 * @param y - Y coordinate for the new square
 	 * @param sq Square to set
 	 */
 	public void setSquare(int x, int y, Square sq)
 	{
 		squares[y][x] = sq;
 
 		this.image.createGraphics().drawImage(sq.getImage(), x * 32, y * 32,
 		(x + 1) * 32, (y + 1) * 32, 0, 0, 32, 32, null);
 	}
 
 	/**
 	 * @param x - X coordinate
 	 * @param y - Y coordinate
 	 * @return The square in the given coordinates
 	 */
 	public Square getSquare(int x, int y)
 	{
 		return squares[y][x];
 	}
 
 	/**
 	 * @param x - The X coordinate for the link
 	 * @param y - The Y coordinate for the link
 	 * @param l Link for the square
 	 */
 	public void addLink(byte x, byte y, Link l)
 	{
 		links.put(new SimpleEntry<Byte, Byte>(x, y), l);
 	}
 
 	/**
 	 * @param x - The X coordinate of the link
 	 * @param y - The Y coordinate of the link
 	 */
 	public void removeLink(byte x, byte y)
 	{
 		links.remove(new SimpleEntry<Byte, Byte>(x, y));
 	}
 
 	/**
 	 * @param x - The X coordinate of the link
 	 * @param y - The Y coordinate of the link
 	 * @return The link in the given coordinates
 	 */
 	public Link getLink(byte x, byte y)
 	{
 		return links.get(new SimpleEntry<Byte, Byte>(x, y));
 	}
 
 	/**
 	 * Get the entry set for the map's links
 	 * 
 	 * @return The entry set for the current links
 	 */
 	public Set<Entry<Entry<Byte, Byte>, Link>> getLinks()
 	{
 		return links.entrySet();
 	}
 
 	/**
 	 * @return The byte array ready to be exported
 	 */
 	public byte[] export()
 	{
 		if (isFinished())
 		{
 			byte[] array = compress();
 			array = addLinks(array);
 			return array;
 		}
 		return null;
 	}
 
 	/**
 	 * @return If the map is completely created
 	 */
 	public boolean isFinished()
 	{
 		boolean finished = true;
 		for (int i = 0; finished && i < squares.length; i++)
 		{
 			for (int h = 0; h < squares[1].length; h++)
 			{
 				if (squares[i][h] == null)
 				{
 					finished = false;
 					break;
 				}
 			}
 		}
 
 		return finished;
 	}
 
 	/**
 	 * @return Current image of the map
 	 */
 	public BufferedImage getImage()
 	{
 		return image;
 	}
 
 	/**
 	 * @return Height of the map in squares
 	 */
 	public int getHeight()
 	{
 		return squares.length;
 	}
 
 	/**
 	 * @return Width of the map in squares
 	 */
 	public int getWidth()
 	{
 		return squares[0].length;
 	}
 
 	private byte[] compress()
 	{
 		byte[][] arr2d = new byte[height][2 * width];
 		byte[][] arr2dc = new byte[height][2 * width];
 
 		// We load the bidimensional array, square by square
 		for (int i = 0; i < this.squares.length; i++)
 		{
 			for (int h = 0, j = 0; h < this.squares[i].length; h++)
 			{
 				arr2d[i][j++] = this.squares[i][h].bytes()[0];
 				arr2d[i][j++] = this.squares[i][h].bytes()[1];
 			}
 		}
 
 		short deleted = 0;
 
 		// We first compress in X coordinate
 		for (int i = 0; i < arr2d.length; i++)
 		{
 			for (int h = 0; h < arr2d[i].length; h += 2)
 			{
 				// We copy the first data
 				arr2dc[i][h] = arr2d[i][h];
 				arr2dc[i][h + 1] = arr2d[i][h + 1];
 				int r = 1;
 
 				// We count repetitions
 				while ((h + r * 2) < arr2d[i].length
 				&& arr2d[i][h] == arr2d[i][h + r * 2]
 				&& arr2d[i][h + 1] == arr2d[i][h + r * 2 + 1])
 				{
 					r++;
 				}
 
 				if (r > 2)
 				{
 					// We set the repetition data
 					arr2dc[i][h + 2] = (byte) (r - 1);
 					arr2dc[i][h + 3] = (byte) 0xFF;
 
 					// We delete the extra data
 					for (int j = 4; j < r * 2; j += 2)
 					{
 						arr2dc[i][h + j + 1] = arr2dc[i][h + j] = (byte) 0xFF;
 						deleted++;
 					}
 
 					h += r * 2 - 2;
 				}
 			}
 		}
 
 		// Now we compress in Y coordinate
 		for (int h = 0; h < arr2d[0].length; h += 2)
 		{
 			for (int i = 0; i < arr2d.length; i++)
 			{
 				int r = 1;
 
 				// We count repetitions
 				while (i + r < arr2d.length && arr2d[i][h] == arr2d[i + r][h]
 				&& arr2d[i][h + 1] == arr2d[i + r][h + 1]
				&& arr2dc[i][h] != (byte) 0xFF
				&& arr2dc[i][h + 1] != (byte) 0xFF)
 				{
 					r++;
 				}
 
 				if (r > 2)
 				{
 					// We set the repetition data
 					arr2dc[i + 1][h] = (byte) 0xFF;
 					arr2dc[i + 1][h + 1] = (byte) (r - 1);
 
 					// We delete extra data
 					for (int j = 2; j < r; j++)
 					{
 						arr2dc[i + j][h + 1] = arr2dc[i + j][h] = (byte) 0xFF;
 						deleted++;
 					}
 
 					i += r - 1;
 				}
 			}
 		}
 
 		// We create the compressed array
 		byte[] arr = new byte[2 + 2 * height * width - deleted * 2];
 		arr[0] = (byte) width;
 		arr[1] = (byte) height;
 		int índice = 2;
 
 		for (byte[] element: arr2dc)
 		{
 			for (int h = 0; h < element.length; h += 2)
 			{
 				// We only save if the square has not been deleted
 				if (element[h] != (byte) 0xFF || element[h + 1] != (byte) 0xFF)
 				{
					arr[índice++] = element[h];
 					arr[índice++] = element[h + 1];
 				}
 			}
 		}
 
 		return arr;
 	}
 
 	private byte[] addLinks(byte[] array)
 	{
 		byte[] result = Arrays.copyOf(array, array.length + links.size() * 6
 		+ 2);
 		int i = array.length;
 		result[i] = result[i + 1] = (byte) 0xFF;
 		i += 2;
 		for (Entry<Entry<Byte, Byte>, Link> ent: links.entrySet())
 		{
 			result[i++] = ent.getKey().getKey();
 			result[i++] = ent.getKey().getValue();
 			result[i++] = MathUtils.getByte(ent.getValue().getMap(), 1);
 			result[i++] = MathUtils.getByte(ent.getValue().getMap(), 0);
 			result[i++] = ent.getValue().getX();
 			result[i++] = ent.getValue().getY();
 		}
 		return result;
 	}
 
 	private Object writeReplace() throws ObjectStreamException
 	{
 		Map m = null;
 		try
 		{
 			m = new Map(width, height);
 		}
 		catch (SpriteException e)
 		{
 			e.printStackTrace();
 		}
 
 		m.image = null;
 		m.squares = squares;
 		m.links = links;
 
 		return m;
 	}
 
 	private Object readResolve() throws ObjectStreamException
 	{
 		Map m = null;
 		try
 		{
 			m = new Map(width, height);
 		}
 		catch (SpriteException e)
 		{
 			e.printStackTrace();
 		}
 
 		m.links = links;
 		m.squares = squares;
 
 		for (int i = 0; i < m.squares.length; i++)
 		{
 			for (int h = 0; h < m.squares[1].length; h++)
 			{
 				Square sq = m.squares[i][h];
 				if (sq != null)
 				{
 					m.image.createGraphics().drawImage(sq.getImage(), h * 32,
 					i * 32, (h + 1) * 32, (i + 1) * 32, 0, 0, 32, 32, null);
 				}
 			}
 		}
 
 		return m;
 	}
 }
