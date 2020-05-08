 package com.gorod.live;
 
 import java.util.ArrayList;
 
 public class Storage {
 	public static final String SEP = ",";
 
 	public static ArrayList<Integer> explode(String s) {
 		ArrayList<Integer> al = new ArrayList<Integer>();
 
 		if ((s.indexOf(SEP) != -1)) {
 			String[] a = s.split(",");
 
 			for (int i = 0; i < a.length; i++)
				try{
				al.add(Integer.parseInt(a[i]));
				}catch(NumberFormatException nFE) { 
					System.out.println("NFE:"+nFE.toString());
 				}
 		} else {
 			if (!s.equals(""))
 				al.add(Integer.parseInt(s));
 		}
 
 		return al;
 	}
 
 	public static String implode(ArrayList<Integer> al) {
 		StringBuffer sb = new StringBuffer();
 
 		for (int i = 0; i < al.size(); i++) {
 			sb.append(al.get(i));
 			if (!al.get(i).equals(al.get(al.size() - 1)))
 				sb.append(SEP);
 
 		}
		//System.out.println("after implode:" + sb.toString());
 		return sb.toString();
 	}
 
 	public static String list_add(String s, Integer v) {
 		ArrayList<Integer> al = explode(s);
 		if (!al.contains(v))
 			al.add(v);
 		return implode(al);
 	}
 
 	public static String list_remove(String s, Integer v) {
 		ArrayList<Integer> al = explode(s);
 		if (al.contains(v))
 			al.remove(v);
 		return implode(al);
 	}
 
 	public static boolean checkin(String s, Integer el) {
 		ArrayList<Integer> al = explode(s);
		if (al.contains(el))
 			return true;
		else
 			return false;
 	}
 
 }
