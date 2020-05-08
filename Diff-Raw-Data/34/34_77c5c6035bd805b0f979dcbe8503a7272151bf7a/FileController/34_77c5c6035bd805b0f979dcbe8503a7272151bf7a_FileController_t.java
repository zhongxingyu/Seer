 package edu.chl.asciicam.file;
 
 import java.util.*;
 import java.io.*;

import android.content.Context;
 import android.os.Environment;
 
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 /**
  * This class if or saving pictures for the application Asciicam. It also handles
  * saving and reading options from file.
  * 
  * @author Braaf
  *
  */
 public class FileController {
 	
 	public static final String UNMOUNTED_SD = "SDCARD_NOT_MOUNTED";
 	
	private static final String SEQ_FILENAME = "ASCIISEQNR";
	
 	private static int SEQ_NUMBER = 0;
 	
	private Context context;
	
 	/**
	 * In order to get access to local storage on phone we need a context.
	 * This constructor does try to init the sequence number for naming pictures automagically.
	 * @param context 
 	 */
	public FileController(Context context){
 		if(SEQ_NUMBER == 0)
 			setSequence();
		this.context = context;
 	}
 	
 	/**
 	 * Use this method to save a picture on the external storage (SD card).
 	 * @param picArray A byte array of the pictures data.
 	 * @throws IOException If an error occurs while writing or if the SD card is not avaible.
 	 * Check message for FileController.UNMOUNTED_SD if its because of unavaible SD card.
 	 */
 	public void savePic(byte[] picArray) throws IOException{
 		if(checkSD()){
 			//Set up file with path to SD card and file name
 			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 			File file = new File(path, "ASCIIPIC_" + getSequenceNumber() + ".jpg");
 			//Make sure directory is created and doesnt exist
 			while(file.mkdirs() == false){
 				//If file.mkdirs(); == false, file already exists and we need to change sequencenumber until we get 
 				//a new file so we dont overwrite anything!
 				sequenceIncrement();
 				file = new File(path, "ASCIIPIC_" + getSequenceNumber() + ".jpg");
 			}
 
 			//Save picture
 			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
 			bos.write(picArray);
 
 			bos.close();
 
 			//Handle the sequence number
 			sequenceIncrement();
 			saveSequence();
 
 		}else{
 			throw new IOException(UNMOUNTED_SD);
 		}
 	}
 
 	//Check if SD-card is avaible.
 	private boolean checkSD(){
 		//Get current state of SD card from Environment
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			return true;
 		}
 		return false;
 	}
 
 	//Sequence for file storing
 	private int getSequenceNumber(){
 		return SEQ_NUMBER;
 	}
 	//Increment sequence
 	private static void sequenceIncrement(){
 		SEQ_NUMBER++;
 	}
 	//Save sequencenumber locally
 	private void saveSequence(){
 		try{
 			//Save the sequence to a local file here
 			// TODO save sequencenumber to file
			Integer seq_Int = Integer.valueOf(SEQ_NUMBER);
			FileOutputStream fos = context.openFileOutput(SEQ_FILENAME, Context.MODE_PRIVATE);
			fos.write(seq_Int.byteValue());
			fos.close();
			
 		}catch(Exception e){
 			//Failed to save sequence number, not so important to take care of.
 		}
 	}
 	
 	/**
 	 * Reloads sequence from saved file for naming saved pictures.
 	 */
	public void setSequence(){
 		try{
 			//Read sequencenumber from file here.
 			// TODO read sequencenumber from file instead of setting it to 1
			FileInputStream fis = context.openFileInput(SEQ_FILENAME);
			SEQ_NUMBER = fis.read();
			fis.close();
 		}catch(Exception e){
 			SEQ_NUMBER = 1;
 		}
 	}
 	
 
 	////////////////////////////////////////////////////////////////////////////////
 	////////////////////////////////////////////////////////////////////////////////
 	//////////////////////OPTIONS//SAVE//AND//GET///////////////////////////////////
 	////////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Use this method to save options made by the user.
 	 * @param options A hashmap consisting of the options to be saved.
 	 */
 	public void saveOptions(HashMap<String, String> options){
 		// TODO
 	}
 	
 	/**
 	 * Use this method to load options on application start.
 	 * @return A map of all options saved.
 	 */
 	public HashMap<String, String> getOptions(){
 		// TODO
 		return new HashMap<String, String>();
 	}
 	
 	
 }
