 package com.alexwyler.wwc.dawg;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
import us.monoid.json.JSONArray;
 import us.monoid.json.JSONException;
 import us.monoid.json.JSONObject;
 
 import com.alexwyler.wwc.Tile;
 
 public class DawgNode {
 
 	public boolean terminal;
 	private Map<Character, DawgNode> edges = new HashMap<Character, DawgNode>();
 
 	static HashMap<File, DawgNode> instances = new HashMap<File, DawgNode>();
 
 	public static void main(String argsp[]) throws JSONException, IOException {
 		DawgNode root = getInstance(new File("WebContent/words.txt"));
 		JSONObject rootJSON = convertToJSON(root);
 		FileWriter out = new FileWriter(new File("WebContent/ChromeExtension/js/dawg.json"));
 		out.append("DawgUtil.dawg = \n");
 		out.append(rootJSON.toString());
 		out.append(";\n");
 		out.close();
 	}
 
 	public static JSONObject convertToJSON(DawgNode node) throws JSONException {
 		JSONObject json = new JSONObject();
 		for (Character c : node.edges.keySet()) {
 			json.put("" + c, convertToJSON(node.getChild(c)));
 			if (node.terminal) {
				json.put("te", 1);
 			}
 		}
 		return json;
 	}
 
 	public static DawgNode getInstance(File file) {
 		DawgNode instance = instances.get(file);
 		if (instance == null) {
 			try {
 				instance = makeDawg(file);
 				instances.put(file, instance);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		return instance;
 	}
 
 	private static DawgNode makeDawg(File dict) throws FileNotFoundException {
 		DawgNode root = new DawgNode();
 		Scanner in = new Scanner(dict);
 		while (in.hasNextLine()) {
 			DawgNode cur = root;
 			for (char c : in.nextLine().toCharArray()) {
 				DawgNode next = cur.edges.get(c);
 				if (next == null) {
 					next = new DawgNode();
 					cur.edges.put(Character.toLowerCase(c), next);
 				}
 				cur = next;
 			}
 			cur.terminal = true;
 		}
 		return root;
 	}
 
 	public DawgNode getChild(Tile t) {
 		if (t != null) {
 			return getChild(t.c);
 		}
 		return null;
 	}
 
 	public DawgNode getChild(char c) {
 		return edges.get(Character.toLowerCase(c));
 	}
 
 	public Collection<Character> getNextCharacters() {
 		return edges.keySet();
 	}
 
 }
