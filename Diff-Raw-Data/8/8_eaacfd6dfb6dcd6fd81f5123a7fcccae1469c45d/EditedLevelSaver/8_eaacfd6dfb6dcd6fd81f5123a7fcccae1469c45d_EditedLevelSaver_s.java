 package fr.umlv.escape.editor;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import fr.umlv.escape.game.Wave;
 
 import android.content.Context;
 
 public class EditedLevelSaver {
 	
 	public EditedLevelSaver() {
 	}
 	
 	public static void saveLevel(Context context, String fileName) throws IOException{
 		File file = new File(context.getFilesDir(), fileName);
 		FileWriter fw = new FileWriter(file);
 		ArrayList<Wave> waveList = EditedLevel.level.getWaveList();
 		ArrayList<Long> delayList = EditedLevel.level.getDelayWaveList();
 		
 		fw.write(EditedLevel.backgroundName+'\n');
 		for(int i =0; i<waveList.size();++i){
 			Wave wave = waveList.get(i);
 			Long delay = delayList.get(i);
 			int delay2 = delay.intValue();
 			
 			fw.write(wave.getName()+'\n');
 			fw.write(delay2);
 			fw.write('\n');
 		}
 		
 		fw.close();
 	}
 }
