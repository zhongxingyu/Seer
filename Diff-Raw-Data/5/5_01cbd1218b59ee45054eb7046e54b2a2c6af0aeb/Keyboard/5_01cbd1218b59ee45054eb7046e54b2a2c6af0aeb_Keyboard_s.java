 
 import java.io.*;
 import java.util.*;
 import java.awt.event.*;
 
 public class Keyboard implements KeyListener
 {
 	private static final HashMap<Integer, Boolean> map = new HashMap<>();
 	private static final HashMap<Integer, Boolean> once = new HashMap<>();
 	// Constructor
 	public Keyboard()
 	{
 	}
 	public static boolean isPressed(int kIn)
 	{
 		if(map.get(kIn) == null)
 		{
 			return false;
 		}
 		return map.get(kIn);
 	}
 	public static boolean isOnce(int kIn)
 	{
 		if(once.get(kIn) == null)
 		{
 			return false;
 		}
 		return once.get(kIn);
 	}
 	public static void useOnce(int kIn)
 	{
 		once.put(kIn, Boolean.FALSE);
 	}
 	public static boolean onceReady(int kIn)
 	{
 		return once.get(kIn) == null;
 	}
 	public static void release(int kIn)
 	{
 		map.put(kIn, Boolean.FALSE); // Use static variable from Boolean class so it won't make a new Boolean object inside the map.
 	}
 	@Override
 	public void keyPressed(KeyEvent eIn)
 	{
 		int k = eIn.getKeyCode();
 		map.put(k, Boolean.TRUE);
 
 		if(onceReady(k))
 		{
 			once.put(k, Boolean.TRUE);
 		}
 
 		//String s = KeyEvent.getKeyText(k); // Not important.
 		//System.out.println(k + "=" + s); // Not important.
 	}
 	@Override
 	public void keyTyped(KeyEvent eIn)
 	{
 	}
 	@Override
 	public void keyReleased(KeyEvent eIn)
 	{
 		int k = eIn.getKeyCode();
 		map.put(k, Boolean.FALSE);
 		once.put(k, null);
 	}
}
