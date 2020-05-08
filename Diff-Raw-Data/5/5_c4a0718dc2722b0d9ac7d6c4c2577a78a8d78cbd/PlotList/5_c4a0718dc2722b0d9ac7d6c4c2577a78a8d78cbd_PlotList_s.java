 package no.runsafe.creativetoolbox;
 
 import com.google.common.collect.Lists;
 import no.runsafe.framework.api.player.IPlayer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class PlotList
 {
 	public void set(IPlayer player, List<String> list)
 	{
 		lists.put(player.getName(), list);
 	}
 
 	public int current(IPlayer player)
 	{
 		return lists.get(player.getName()).indexOf(pointer.get(player.getName())) + 1;
 	}
 
 	public void wind(IPlayer player, String to)
 	{
 		pointer.put(player.getName(), to);
 	}
 
 	public int count(IPlayer player)
 	{
 		return lists.get(player.getName()).size();
 	}
 
 	public void remove(String plot)
 	{
 		for (Map.Entry<String, List<String>> list : lists.entrySet())
 		{
 			if (list.getValue().contains(plot))
 			{
 				ArrayList<String> plots = Lists.newArrayList(list.getValue());
 				int i = plots.indexOf(plot);
 				plots.remove(plot);
 				lists.put(list.getKey(), plots);
				pointer.put(list.getKey(), plots.get(i));
 			}
 		}
 	}
 
 	public String previous(IPlayer player)
 	{
 		if (lists.containsKey(player.getName()))
 		{
 			List<String> list = lists.get(player.getName());
 			if (list == null || list.isEmpty())
 				return null;
 			int i = list.indexOf(pointer.get(player.getName()));
 			pointer.put(player.getName(), list.get(i > 0 ? i - 1 : list.size() - 1));
 			return pointer.get(player.getName());
 		}
 		return null;
 	}
 
 	public String next(IPlayer player)
 	{
 		if (lists.containsKey(player.getName()))
 		{
 			List<String> list = lists.get(player.getName());
 			if (list == null || list.isEmpty())
 				return null;
 			int i = list.indexOf(pointer.get(player.getName()));
 			pointer.put(player.getName(), list.get(i + 1 >= list.size() ? 0 : i + 1));
 			return pointer.get(player.getName());
 		}
 		return null;
 	}
 
 	private final ConcurrentHashMap<String, String> pointer = new ConcurrentHashMap<String, String>();
 	private final ConcurrentHashMap<String, List<String>> lists = new ConcurrentHashMap<String, List<String>>();
 }
