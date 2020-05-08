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
		writer.println(id + " " + value);
 		return true;
 	}
 
 	public static boolean WriteStatement(int id, int type, int value) {
		writer.println(id + " " + value);
 		return true;
 	}
 
 	public static boolean WritePredicate(int id, int type, boolean value) {
		writer.println(id + " " + (value ? 1 : 0));
 		return value;
 	}
 }
