 package shamebot.Map;
 
 
 import java.awt.image.BufferedImage;
 
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 
 import net.minecraft.server.Item;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.WorldMap;
 
 public class MapImage {
 
 	private MapColumn[] data;
 	private byte x,y;
 	
 	public MapImage(byte width, byte height)
 	{
 		this((byte)0,(byte)0,width,height);
 	}
 	
 	public MapImage(int x, int y, int width, int height)
 	{
 		if(0 > x || x > 127)
 		{
 			throw new IllegalArgumentException("X is out of range: " + x + "must be: 0 <= x <= 128");
 		}
 		if(0 > y || y > 127)
 		{
 			throw new IllegalArgumentException("Y is out of range: " + y + "must be: 0 <= y <= 128");
 		}
 		if(0 >= width)
 		{
 			throw new IllegalArgumentException("Width is out of range: " + width + "must be greater than 0");
 		}
 		if(0 >= height)
 		{
 			throw new IllegalArgumentException("Height is out of range: " + height + "must be greater than 0");
 		}
 		
 		this.x = (byte) x;
 		this.y = (byte) y;
 		height =	(128 - y < height ? 128 - y : height);
 		width  = 	(128 - x < width  ? 128 - x : width );
 		data = new MapColumn[width];
 		for(short i = 0; i < width; i++)
 		{
 			data[i] = new MapColumn((byte)(x + i),(byte) y,(short)height);
 		}
 	}
 	
 	public MapImage(short mapItemDamage, World world)
 	{
 		data = new MapColumn[128];
 		WorldMap worldMap = getWorldMap(mapItemDamage,world);
 		for(short i = 0; i < 128; i++)
 		{
 			byte[] byteData = new byte[128];
 			for(short j = 0; j < 128; j++)
 			{
				byteData[j] = worldMap.f[i*128+j];
 			}
 			data[i] = new MapColumn(byteData,(byte)i,(byte)0);
 		}
 	}
 
 	public static WorldMap getWorldMap(short mapItemDamage, World world)
 	{
 		return Item.MAP.a(new ItemStack(Item.MAP,0,mapItemDamage), ((CraftWorld)world).getHandle());
 	}
 	public static void setXCenter(short mapItemDamage, World world, int x)
 	{
 		getWorldMap(mapItemDamage,world).b=x;
 	}
 	public static void setZCenter(short mapItemDamage, World world, int z)
 	{
 		getWorldMap(mapItemDamage,world).c=z;
 	}
 	
 	public short getX()
 	{
 		return (short)(x & 0xFF);
 	}
 	
 	public short getY()
 	{
 		return (short)(y & 0xFF);
 	}
 	
 	public short getWidth()
 	{
 		return (short)data.length;
 	}
 	
 	public short getHeight()
 	{
 		return data[0].getHeight();
 	}
 	
 	public MapPixel getPixel(byte x, byte y)
 	{
 		return data[x].getPixel(y);
 	}
 	
 	public void setPixel(MapPixel pixel)
 	{
 		data[pixel.getX()].setPixel(pixel);
 	}
 
 	public void fill(MapColor color)
 	{
 		drawRectangle((short)0, (short)0, getWidth(), getHeight(), color);
 	}
 	
 	public void drawRectangle(short x, short y, short width, short height, MapColor color)
 	{
 		int toX = x + width; 
 		int toY = y + height;
 		short imageHeight = getHeight();
 		short imageWidth = getWidth();
 		for(short i = x; i < toX && i < imageWidth;i++)
 		{
 			for(short j = y; j < toY && j < imageHeight;j++)
 			{
 				data[i].setPixel(color, j);
 			}
 		}
 	}
 	
 	public void drawImage(short x, short y, BufferedImage image)
 	{
 		drawImage(x, y,(short) image.getWidth(),(short) image.getHeight(),image);
 	}
 	
 	public void drawImage(short x, short y, short width, short height, BufferedImage image)
 	{
 		int toX = x + width; 
 		int toY = y + height;
 		for(short i = x; i < toX && i < getWidth() && i-x < image.getWidth();i++)
 		{
 			for(short j = y; j < toY && j < getHeight() && j-y < image.getHeight();j++)
 			{
 				int rgb = image.getRGB(i-x, j-y);
 				data[i].setPixel(new MapPixel(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, (byte)i, (byte)j), j);
 			}
 		}
 	}
 	
 	public void drawOnMap(short mapItemDamage, World world)
 	{
 		WorldMap worldMap = getWorldMap(mapItemDamage, world);
 		for(int i = 0;i<data.length;i++)
 		{
 			for(int j = 0;j<data[i].getHeight();j++)
 			{
				worldMap.f[(x+1)*128+y+j]=data[i].getPixel((short) j).getMinecraftColor();
 			}
 		}
 	}
 	
 	public void send(Player player, short mapItemDamage)
 	{
 		for(MapColumn column: data)
 		{
 			column.send(player, mapItemDamage);
 		}
 	}
 	
 	public BufferedImage getImage()
 	{
 		BufferedImage image = new BufferedImage(data.length,data[0].data.length,BufferedImage.TYPE_INT_ARGB);
 		for(short i = 0; i < data.length; i++)
 		{
 			MapColumn column = data[i];
 			for(short j = 0; j < column.getHeight(); j++)
 			{
 				MapPixel pixel = column.getPixel(j);
 				image.setRGB(pixel.getX(), pixel.getY(), 0xFF000000 | pixel.getR() << 16 | pixel.getG() << 8 | pixel.getB());
 			}
 		}
 		return image;
 	}
 }
