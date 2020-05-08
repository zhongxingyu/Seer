 package com.aegamesi.mc.po8;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 public class Po8Util {
 	public static String processColours(String str) {
 		return str.replaceAll("(&([a-f0-9klmnor]))", "\u00A7$2");
 	}
 
 	public static String stripColours(String str) {
 		return str.replaceAll("(&([a-f0-9klmnor]))", "");
 	}
 
 	public static void message(CommandSender sender, String message) {
 		sender.sendMessage(processColours(message));
 	}
 
 	public static double round2(double num) {
 		double result = num * 100;
 		result = Math.round(result);
 		result = result / 100;
 		return result;
 	}
 
 	public static double getBasePrice(String id) {
 		Po8Item item = Po8.itemMap.get(id);
 		double stock = Po8.stockMap.get(id);
 		return round2(Math.max(Math.min(item.maxPrice * Math.pow(.5, (stock / (double) (item.stackSize * 27))), item.maxPrice), item.minPrice));
 	}
 
 	public static String stockKey(MaterialData m) {
 		return m.getItemTypeId() + (m.getData() != 0 ? "t" + m.getData() : "");
 	}
 
 	public static MaterialData dataKey(String key) {
 		String[] arr = key.split("t");
 		if (arr.length == 1)
 			return new MaterialData(Integer.parseInt(arr[0]), (byte) 0);
 		return new MaterialData(Integer.parseInt(arr[0]), Byte.parseByte(arr[1]));
 	}
 
 	public static void log(String from, String to, double amount, String type, String notes) {
 		// from, to, amount, type, notes
 		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
 		String line = String.format("\"%s\",\"%s\",\"%s\",\"%f\",\"%s\",\"%s\"", from, to, time, amount, type, notes);
 		try {
 			Po8.writer.write(line);
 			Po8.writer.newLine();
 			Po8.writer.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String pack(ItemStack[] stacks) {
 		String pack = "";
 		for (ItemStack stack : stacks) {
 			if (stack == null)
 				continue;
 			if (pack.length() > 0)
 				pack += "|";
 			pack += stack.getTypeId();
 			pack += "," + stack.getAmount();
 			pack += "," + stack.getData().getData();
 		}
 		return pack;
 	}
 
 	public static ItemStack[] unpack(String pack) {
 		String[] pack2 = pack.split("|");
 		ItemStack[] stacks = new ItemStack[pack2.length];
 		for (int i = 0; i < pack2.length; i++) {
 			String[] pack3 = pack2[i].split(",");
 			stacks[i] = new ItemStack(Integer.parseInt(pack3[0]), Integer.parseInt(pack3[1]), (short) 0);
 			stacks[i].setData(new MaterialData(Integer.parseInt(pack3[0]), Byte.parseByte(pack3[2])));
 		}
 		return stacks;
 	}
 
 	public static ItemStack[] splitStack(String key, int amt) {
 		MaterialData dataKey = dataKey(key);
 		ItemStack stack = new ItemStack(dataKey.getItemType(), amt, (short) 0);
		stack.setData(new MaterialData(dataKey.getItemType(), dataKey.getData()));
 		double num = stack.getAmount();
 		int size = (int) Math.ceil(num / ((double) stack.getMaxStackSize()));
 		ItemStack[] stacks = new ItemStack[size];
 		for (int i = 0; i < size; i++) {
			ItemStack tempStack = new ItemStack(stack);
 			tempStack.setAmount((int) Math.min(stack.getMaxStackSize(), num));
 			stacks[i] = tempStack;
 			num -= tempStack.getAmount();
 		}
 		return stacks;
 	}
 
 	public static String combine(String args[], int start, int num) {
 		if (num == 0)
 			num = args.length - start;
 		String result = "";
 		for (int i = start; i < start + num; i++)
 			result += args[i] + ((i == start + num - 1) ? "" : " ");
 		return result;
 	}
 
 	public static String getBlock(String blockName) {
 		String check = blockName.toLowerCase().trim().replace(':', 't');
 		if (Po8.itemMap.containsKey(check))
 			return check;
 		for (Map.Entry<String, Po8Item> entry : Po8.itemMap.entrySet()) {
 			String key = entry.getKey();
 			Po8Item value = entry.getValue();
 			if (value.name.toLowerCase().equals(check))
 				return key;
 		}
 		return null;
 	}
 }
