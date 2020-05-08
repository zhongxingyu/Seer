 package com.tos_prophet;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 public class ServiceCenter {
 	final String _filePath = "/data/data/com.madhead.tos.zh/shared_prefs/com.madhead.tos.zh.xml";
	final String _MyCardFp = "/data/data/com.madhead.tos.zh.ex/shared_prefs/com.madhead.tos.zh.ex.xml";
 	private final String xmlid = "MH_CACHE_RUNTIME_DATA_CURRENT_FLOOR_WAVES";
 	String _xmlContent;
 
 	public ServiceCenter() {
 		_xmlContent = "";
 	}
 
 	public ArrayList<String> getDisplayStringData() {
 		checkoutRoot();
 		XmlParser xmp = new XmlParser();
 		TosJsonParser tjp = new TosJsonParser();
 		String xmlres = xmp.parserXmlByID("/mnt/sdcard/tmp/TOS_tmp.xml", xmlid);
 		if (xmlres.equals("")) {
 			return null;
 		}
 		ArrayList<levleData> ld = tjp.getLevelData(xmlres);
 		ArrayList<String> ret = new ArrayList<String>();
 		for (int i = 0; i < ld.size(); i++) {
 			ret.add("level " + (i + 1));
 			ArrayList<enemiesData> el = ld.get(i).getEnemiesList();
 			for (enemiesData ed : el) {
 				if(!ed.getLootItem().equals("null")){
 					ret.add("Drop "+ed.getName());
 				}
 			}
 		}
 		return ret;
 
 	}
 
 	private String getXmlFile(String _path) {
 		String ret = "";
 
 		try {
 			// Open the file that is the first
 			// command line parameter
 			FileInputStream fstream = new FileInputStream(_path);
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				// Print the content on the console
 				ret = ret + strLine;
 				System.out.println(strLine);
 			}
 			// Close the input stream
 			in.close();
 
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 		return ret;
 	}
 
 	private void checkoutRoot() {
 		Process p;
 		try {
 			// Preform su to get root privledges
 			p = Runtime.getRuntime().exec("su");
 
 			// Attempt to write a file to a root-only
 			DataOutputStream os = new DataOutputStream(p.getOutputStream());
 			String cmd = "cp " + _filePath + " /mnt/sdcard/tmp/TOS_tmp.xml\n";
 			os.writeBytes(cmd);
			
			cmd = "cp " + _MyCardFp + " /mnt/sdcard/tmp/TOS_tmp.xml\n";
			os.writeBytes(cmd);
 
 			// Close the terminal
 			os.writeBytes("exit\n");
 			os.flush();
 			try {
 				p.waitFor();
 				if (p.exitValue() != 255) {
 					// TODO Code to run on success
 					// return true;
 				} else {
 					// TODO Code to run on unsuccessful
 					// return false;
 				}
 			} catch (InterruptedException e) {
 				// TODO Code to run in interrupted exception
 				// return false;
 			}
 		} catch (IOException e) {
 			// TODO Code to run in input/output exception
 			// return false;
 		}
 	}
 }
