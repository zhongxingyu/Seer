 package jp.ac.waseda.cs.washi;
 
 import java.io.IOException;
 import java.io.PrintStream;
 
 public class CoverageWriter {
 	private static PrintStream writer;
 	static {
 		try {
 			writer = new PrintStream(".occf_record");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static boolean WriteTestCase(int id, int type, int value) {
		writer.println(id + " " + type + " " + (value + 1));
 		return true;
 	}
 
 	public static boolean WriteStatement(int id, int type, int value) {
		writer.println(id + " " + type + " " + (value + 1));
 		return true;
 	}
 
 	public static boolean WritePredicate(int id, int type, boolean value) {
		writer.println(id + " " + type + " " + (value ? 2 : 1));
 		return value;
 	}
 }
