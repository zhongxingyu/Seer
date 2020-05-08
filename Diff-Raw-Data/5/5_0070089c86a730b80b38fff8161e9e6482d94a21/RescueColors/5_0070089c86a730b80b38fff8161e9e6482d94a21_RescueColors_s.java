 package technobotts.rescue;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import lejos.nxt.LCD;
 import lejos.nxt.addon.ColorSensor;
 
 public class RescueColors
 {
 	public final RawColor	silver;
 	public final RawColor	white;
 	public final RawColor	green;
 	public final RawColor	black;
 
 	private final RawColor[]	  colors;
 
 	public RescueColors(RawColor silver, RawColor white, RawColor green, RawColor black)
 	{
 		this.silver = silver;
 		this.white = white;
 		this.green = green;
 		this.black = black;
 		colors = new RawColor[] {silver, white, green, black};
 	}
 	
 	public RescueColors(RawColor[] colors)
 	{
 		this(colors[0],colors[1],colors[2],colors[3]);
 	}
 
 	public void printToLCD()
 	{
 		LCD.clear();
 		LCD.drawString("S:" + silver, 0, 0);
 		LCD.drawString("W:" + white, 0, 1);
 		LCD.drawString("G:" + green, 0, 2);
 		LCD.drawString("B:" + black, 0, 3);
 		LCD.refresh();
 	}
 
 	public void writeObject(OutputStream out) throws IOException
 	{
 		for(RawColor color : colors)
 		{
 			color.writeObject(out);
 		}
 	}
 	public static RescueColors readObject(InputStream in) throws IOException
 	{
 		return new RescueColors(RawColor.readObject(in),
 		                      RawColor.readObject(in),
 		                      RawColor.readObject(in),
 		                      RawColor.readObject(in));
 	}
 
 	public RawColor getUnbiasedColor(RawColor input)
 	{
 		if(input == null) return null;
 		RawColor closest = null;
 		long bestDistance = 0;
 
 		for(RawColor color : colors)
 		{
 			long curDistance = input.distanceTo(color);
 			if(closest == null || curDistance < bestDistance)
 			{
 				closest = color;
 				bestDistance = curDistance;
 			}
 		}
 		return closest;
 	}
 
 	public RawColor getColor(RawColor input)
 	{
 		if(input == null) return null;
 		RawColor col = getUnbiasedColor(input);
 		if(col == green)
 		{
 			float GreenRatio = (float) green.getG() / green.getR();
 			float CurrentRatio = (float) input.getG() / input.getR();
 
			if(GreenRatio * 0.95f > CurrentRatio)
 				col = black;
 		}
 		return col;
 	}
 	
 	public RawColor getSensorColor(ColorSensor s)
 	{
 		return getColor(RawColor.fromHTSensor(s));
 	}
 }
