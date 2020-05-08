 package com.cheesymountain.woe;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 
 public class Database extends Activity {
 	
	private static final String MY_EVERBIE = "everbie_data";
 	
 	public void save() throws IOException, FileNotFoundException{
 		System.out.println("save");
		FileOutputStream fos = openFileOutput(MY_EVERBIE, Context.MODE_PRIVATE);
		
 		Everbie everbie = Everbie.getEverbie();
		String data = everbie.GetName()+
				Integer.toString(everbie.getHealth())+
				Integer.toString(everbie.getFullness())+
				Integer.toString(everbie.getToxicity());
		fos.write(data.getBytes());
		fos.close();
 	}
 	
 	@SuppressWarnings("null")
 	public void load() throws IOException, FileNotFoundException{
 		System.out.println("load");
 		FileInputStream fis = openFileInput(MY_EVERBIE);
		byte[] newData = null;
		fis.read(newData);
 		if(newData == null || newData.length == 0){
 			System.out.println("No Data Found when loading");
 		}
 		String dataString = "";
 		for (int i=0;i<newData.length;i++){
			dataString = dataString+Byte.toString(newData[i]);
 		}
 		System.out.println(dataString);
 	}
 }
