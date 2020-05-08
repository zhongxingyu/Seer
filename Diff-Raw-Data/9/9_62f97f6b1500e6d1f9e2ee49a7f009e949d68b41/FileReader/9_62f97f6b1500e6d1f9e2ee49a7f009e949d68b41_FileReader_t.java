package main.java.com.ee.excellentpdf;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 public class FileReader {
 
 	public static void main(String[] args) {
 		BufferedReader br = null;
 
 		try {
 
 			String sCurrentLine;
 
 			br = new BufferedReader(new java.io.FileReader("ExcelFile/file.txt"));
 
 			while ((sCurrentLine = br.readLine()) != null) {
 				System.out.println(sCurrentLine);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)
 					br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 }
