 package mc.alk.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mc.alk.util.handlers.IHologramHandler;
 import mc.alk.util.objects.Hologram;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 
 public class HologramUtils
 {
 	private static class NullHologramHandler implements IHologramHandler
 	{
 		@Override
 		public boolean destroyHologram(Hologram hologram)
 		{
 			return false;
 		}
 
 		@Override
 		public List<Integer> showLine(Location location, String text)
 		{
 			return new ArrayList<Integer>();
 		}
 	}
 
 	private static IHologramHandler handler;
 
 	static
 	{
 		try
 		{
 			final String pkg = Bukkit.getServer().getClass().getPackage()
 					.getName();
 			String version = pkg.substring(pkg.lastIndexOf('.') + 1);
 			final Class<?> clazz;
 			if (version.equalsIgnoreCase("craftbukkit"))
 			{
 				clazz = Class
						.forName("mc.alk.joining.compat.v1_2_5.HologramHandler");
 			}
 			else
 			{
 				clazz = Class.forName("mc.alk.joining.compat." + version
						+ ".HologramHandler");
 			}
 			Class<?>[] args = {};
 			handler = (IHologramHandler) clazz.getConstructor(args)
 					.newInstance((Object[]) args);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			handler = new NullHologramHandler();
 		}
 	}
 
 	public static void sendHologram(Hologram hologram)
 	{
 		Location first = hologram
 				.getLocation()
 				.clone()
 				.add(0,
 						(hologram.getLines().size() / 2)
 								* hologram.getDistanceBetweenLines(), 0);
 		for (int i = 0; i < hologram.getLines().size(); i++)
 		{
 			hologram.getIds()
 					.addAll(handler.showLine(first.clone(), hologram.getLines()
 							.get(i)));
 			first.subtract(0, hologram.getDistanceBetweenLines(), 0);
 		}
 		hologram.setShowing(true);
 	}
 
 	public static void changeHologram(Hologram hologram)
 	{
 		destroyHologram(hologram);
 		sendHologram(hologram);
 	}
 
 	public static boolean destroyHologram(Hologram hologram)
 	{
 		if (!hologram.isShowing())
 		{
 			return false;
 		}
 		return handler.destroyHologram(hologram);
 	}
 }
