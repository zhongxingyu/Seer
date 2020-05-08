 package com.gmail.zant95.LiveChat;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Utils {
 	public static String getMsg(String[] args, int start, CommandSender sender) {
 		StringBuffer buff = new StringBuffer();
 		for (int i = start; i < args.length; i++) {
 			buff.append(args[i] + " ");
 		}
 		String msg = buff.toString();
 		return msg;
 	}
 
	public static void closeChannels(Player player) {
		String playerName = player.getName();
		MemStorage.speaker.remove(playerName);
		MemStorage.local.remove(playerName);
		MemStorage.admin.remove(playerName);
		MemStorage.map.remove(playerName);
		return;
	}

 	public static boolean isConsole(CommandSender sender) {
 		if (sender instanceof Player) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public static boolean isIgnored(Player sender, Player target) {
 		if (MemStorage.ignore.containsKey(target.getName() + "." + sender.getName())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean containsIgnoreCase(String[] string, String element) {
 		List<String> list = Arrays.asList(string);
 		Iterator<String> it = list.iterator();
 		while(it.hasNext()) {
 			if(it.next().equalsIgnoreCase(element))
 			return true;
 		}
 		return false;
 	}
 
 	public static void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while((len=in.read(buf))>0){
 				out.write(buf,0,len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
