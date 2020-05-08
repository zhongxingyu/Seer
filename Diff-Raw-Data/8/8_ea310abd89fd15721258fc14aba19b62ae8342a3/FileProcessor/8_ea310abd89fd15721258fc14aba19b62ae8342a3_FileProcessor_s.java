 package fi.mikuz.boarder.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.commons.io.IOUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.StreamException;
 
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboardHistory;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboardHolder;
 import fi.mikuz.boarder.gui.SoundboardMenu;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class FileProcessor {
 	public static final String TAG = "FileProcessor";
 	
 	public static GraphicalSoundboardHolder loadGraphicalSoundboardHolder(String boardName) throws IOException {
 		
 		GraphicalSoundboardHolder gsbHolder = new GraphicalSoundboardHolder();
 		File projectDir = new File(SoundboardMenu.mSbDir, boardName);
 		
 		try{
 			XStream xstream = XStreamUtil.graphicalBoardXStream();
 			gsbHolder = (GraphicalSoundboardHolder) xstream.fromXML(new File(projectDir + "/graphicalBoard"));
 			return gsbHolder;
 		} catch(StreamException e) {
 			Log.e(TAG, "Can't open the board " + boardName, e);
 			
 			GraphicalSoundboard gsb = new GraphicalSoundboard();
 			
 			DataInputStream in = new DataInputStream(new FileInputStream(projectDir + "/graphicalBoard"));
 	        BufferedReader br = new BufferedReader(new InputStreamReader(in), 8192);
 	        
 		    String line;
 		    line = br.readLine();
 		    
 		    gsb.setPlaySimultaneously(Boolean.parseBoolean(line.substring(0, line.indexOf("1"))));
 		    gsb.setBoardVolume(Float.valueOf(line.substring(line.indexOf("1") + 3, line.indexOf("2"))).floatValue());
 		    gsb.setUseBackgroundImage(Boolean.parseBoolean(line.substring(line.indexOf("2") + 3, line.indexOf("3"))));
 		    gsb.setBackgroundColor(Integer.valueOf(line.substring(line.indexOf("3") + 3, line.indexOf("4"))).intValue());
 		    File backgroundImagePath = new File(line.substring(line.indexOf("4") + 3, line.indexOf("5")));
 		    if (backgroundImagePath.toString().contains("local/")) {
 		    	backgroundImagePath = new File(SoundboardMenu.mSbDir + "/" + boardName, backgroundImagePath.toString().
 		    			substring(6, backgroundImagePath.toString().length()));
 		    } else if (backgroundImagePath.toString().equals("na")) {
 		    	backgroundImagePath = null;
 		    }
 		    gsb.setBackgroundImagePath(backgroundImagePath);
 		    gsb.setBackgroundX(Float.valueOf(line.substring(line.indexOf("5") + 3, line.indexOf("6"))).floatValue());
 		    gsb.setBackgroundY(Float.valueOf(line.substring(line.indexOf("6") + 3, line.indexOf("7"))).floatValue());
 		    gsb.setBackgroundWidth(Float.valueOf(line.substring(line.indexOf("7") + 3, line.indexOf("8"))).floatValue());
 		    gsb.setBackgroundHeight(Float.valueOf(line.substring(line.indexOf("8") + 3, line.indexOf("9"))).floatValue());
 		    gsb.setScreenOrientation(Integer.valueOf(line.substring(line.indexOf("9") + 3, line.indexOf("10"))).intValue());
 		    gsb.setAutoArrange(Boolean.parseBoolean(line.substring(line.indexOf("10") + 4, line.indexOf("11"))));
 		    gsb.setAutoArrangeColumns(Integer.valueOf(line.substring(line.indexOf("11") + 4, line.indexOf("12"))).intValue());
 		    gsb.setAutoArrangeRows(Integer.valueOf(line.substring(line.indexOf("12") + 4, line.length())).intValue());
 
 		    while ((line = br.readLine()) != null)   {
 		    	GraphicalSound sound = new GraphicalSound();
 
 		    	sound.setName(line.substring(0, line.indexOf("1")).replaceAll("lineBreak", "\n"));
 
 		    	File soundPath = new File(line.substring(line.indexOf("1") + 3, line.indexOf("2")));
 		    	if (soundPath.toString().contains("local/")) {
 		    		sound.setPath(new File(
 		    				SoundboardMenu.mSbDir + "/" + boardName, soundPath.toString().substring(6, soundPath.toString().length())));
 		    	} else {
 		    		sound.setPath(soundPath);
 		    	}
 
 		    	sound.setVolumeLeft(Float.valueOf(line.substring(line.indexOf("2") + 3, line.indexOf("3"))).floatValue());
 		    	sound.setVolumeRight(Float.valueOf(line.substring(line.indexOf("3") + 3, line.indexOf("4"))).floatValue());
 		    	sound.setNameFrameX(Float.valueOf(line.substring(line.indexOf("4") + 3, line.indexOf("5"))).floatValue());
 		    	sound.setNameFrameY(Float.valueOf(line.substring(line.indexOf("5") + 3, line.indexOf("6"))).floatValue());
 		    	sound.setHideImageOrText(Integer.valueOf(line.substring(line.indexOf("8") + 3, line.indexOf("9"))));
 
 		    	File imagePath = new File(line.substring(line.indexOf("9") + 3, line.indexOf("10")));
 		    	if (imagePath.toString().contains("local/")) {
 		    		sound.setImagePath(new File(
 		    				SoundboardMenu.mSbDir + "/" + boardName, imagePath.toString().substring(6, imagePath.toString().length())));
 		    	} else {
 		    		sound.setImagePath(imagePath);
 		    	}
 
 		    	sound.setImageX(Float.valueOf(line.substring(line.indexOf("10") + 4, line.indexOf("11"))).floatValue());
 		    	sound.setImageY(Float.valueOf(line.substring(line.indexOf("11") + 4, line.indexOf("12"))).floatValue());
 		    	sound.setImageWidth(Float.valueOf(line.substring(line.indexOf("12") + 4, line.indexOf("13"))).floatValue());
 		    	sound.setImageHeight(Float.valueOf(line.substring(line.indexOf("13") + 4, line.indexOf("14"))).floatValue());
 		    	sound.setHideImageOrText(Integer.valueOf(line.substring(line.indexOf("14") + 4, line.indexOf("15"))));
 		    	sound.setNameTextColorInt(Integer.valueOf(line.substring(line.indexOf("15") + 4, line.indexOf("16"))));
 		    	sound.setNameFrameInnerColorInt(Integer.valueOf(line.substring(line.indexOf("16") + 4, line.indexOf("17"))));
 		    	sound.setNameFrameBorderColorInt(Integer.valueOf(line.substring(line.indexOf("17") + 4, line.indexOf("18"))));
 		    	sound.setShowNameFrameInnerPaint(Boolean.parseBoolean(line.substring(line.indexOf("18") + 4, line.indexOf("19"))));
 		    	sound.setShowNameFrameBorderPaint(Boolean.parseBoolean(line.substring(line.indexOf("19") + 4, line.indexOf("20"))));
 		    	sound.setLinkNameAndImage(Boolean.parseBoolean(line.substring(line.indexOf("20") + 4, line.indexOf("21"))));
 		    	sound.setNameSize(Float.valueOf(line.substring(line.indexOf("21") + 4, line.indexOf("22"))));
 		    	sound.setAutoArrangeColumn(Integer.valueOf(line.substring(line.indexOf("22") + 4, line.indexOf("23"))));
 		    	sound.setAutoArrangeRow(Integer.valueOf(line.substring(line.indexOf("23") + 4, line.indexOf("24"))));
 
 		    	File activeImagePath = new File(line.substring(line.indexOf("24") + 4, line.indexOf("25")));
 		    	if (activeImagePath.toString().contains("local/")) {
 		    		sound.setActiveImagePath(new File(
 		    				SoundboardMenu.mSbDir + "/" + boardName, activeImagePath.toString().substring(6, activeImagePath.toString().length())));
 		    	} else {
 		    		sound.setActiveImagePath(activeImagePath);
 		    	}
 		    	sound.setSecondClickAction(Integer.valueOf(line.substring(line.indexOf("25") + 4, line.length())));
 		    	
 		    	if (sound.getImagePath().getAbsolutePath().equals("/")) sound.setImagePath(null);
 		    	if (sound.getActiveImagePath().getAbsolutePath().equals("/")) sound.setActiveImagePath(null);
 
 		    	gsb.addSound(sound);
 		    }
 		    
 		    in.close();
 		    gsbHolder.getBoardList().add(gsb);
 		    
 		    Log.i(TAG, "Imported Unlimited Soundboards board");
 			return gsbHolder;
 		}
 	}
 	
 	public static void saveGraphicalSoundboardHolder(String boardName, GraphicalSoundboardHolder boardHolder) throws IOException {
 		
 		File project = new File(SoundboardMenu.mSbDir, boardName);
 		File sbFile = new File(project, "graphicalBoard");
 		
 		project.mkdirs();
 		attemptBackup(sbFile);
 
 		if (project.exists() == false) {
 			project.mkdirs();
 		}
 		
 		BufferedWriter out = new BufferedWriter(new FileWriter(sbFile));
 		
 		XStream xstream = XStreamUtil.graphicalBoardXStream();
 		xstream.toXML(boardHolder, out);
 		out.close();
 	}
 	
 	public static void attemptBackup(File backupIn) {
 		if (backupIn.exists()) {
 			try {
 				File backupDir = new File(SoundboardMenu.mBackupDir, backupIn.getParentFile().getName());
 				for (int i = 8; i >= 0; i--) {
 					if (new File(backupDir, "graphicalBoard." + i).exists()) {
 						IOUtils.copy(new FileInputStream(new File(backupDir, "graphicalBoard." + i)), 
 								new FileOutputStream(new File(backupDir, "graphicalBoard." + (i+1))));
 					}
 				}
 				if (!backupDir.exists()) backupDir.mkdirs();
 				File backupOut = new File(backupDir, "graphicalBoard.0");
 				InputStream in = new FileInputStream(backupIn);
 				OutputStream out = new FileOutputStream(backupOut);
 			    IOUtils.copy(in, out);
 			} catch (FileNotFoundException e) {
 				Log.e(TAG, "Failed to backup", e);
 				BugSenseHandler.log(TAG, e);
 			} catch (IOException e) {
 				Log.e(TAG, "Failed to backup", e);
 				BugSenseHandler.log(TAG, e);
 			}
 		}
 	}
 	
 	public static GraphicalSoundboardHistory loadGraphicalBoardHistory(String boardName) {
 		GraphicalSoundboardHistory gsbh = null;
 		
 		try{
 			XStream xstream = XStreamUtil.graphicalBoardHistoryXStream();
 			gsbh = (GraphicalSoundboardHistory) xstream.fromXML(new File(SoundboardMenu.mHistoryDir, boardName));
 		} catch (Exception e) {
 			Log.e(TAG, "Failed to load history", e);
			BugSenseHandler.log(TAG, e);
 			gsbh = new GraphicalSoundboardHistory();
 		}
 		
 		return gsbh;
 	}
 	
 	public static void saveGraphicalBoardHistory(String boardName, GraphicalSoundboardHistory gsbh) {
 		if (!SoundboardMenu.mHistoryDir.exists()) SoundboardMenu.mHistoryDir.mkdirs();
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(new File(SoundboardMenu.mHistoryDir, boardName)));
 			XStream xstream = XStreamUtil.graphicalBoardHistoryXStream();
 			xstream.toXML(gsbh, out);
 			out.close();
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to save history", e);
 			BugSenseHandler.log(TAG, e);
 		}
 	}
 	
 	public static void convertGraphicalBoard(Activity activity, String boardName, GraphicalSoundboard gsb) throws IOException {
 		
 		File projectDir = new File(SoundboardMenu.mSbDir, boardName);
 		String doesntExist = " doesn't exist";
 		String baseDir = SoundboardMenu.mSbDir.getAbsolutePath();
 		
 		if (gsb.getBackgroundImagePath() != null) {
 			if (!gsb.getBackgroundImagePath().exists()) {
 				notify(activity, "Background image" + doesntExist);
 			} else if (gsb.getBackgroundImagePath().getAbsolutePath().contains(baseDir) == false) {
 				
 				File outFile = new File(SoundboardMenu.mSbDir, gsb.getBackgroundImagePath().getName());
 				InputStream in = new FileInputStream(gsb.getBackgroundImagePath());
 			    OutputStream out = new FileOutputStream(outFile);
 				IOUtils.copy(in, out);
 				gsb.setBackgroundImagePath(outFile);
 			}
 		}
 		
 		
 		for (GraphicalSound sound : gsb.getSoundList()) {
 			if (BoardLocal.isFunctionSound(sound)) {
 			} else if (!sound.getPath().exists()) {
 				notify(activity, "Sound:\n" + sound.getName() + "\n\nSound file" + doesntExist);
 			} else if (sound.getPath().getAbsolutePath().contains(baseDir) == false) {
 				File soundFile = new File(projectDir, sound.getPath().getName());
 				try {
 					InputStream in = new FileInputStream(sound.getPath());
 				    OutputStream out = new FileOutputStream(soundFile);
 				    IOUtils.copy(in, out);
 					sound.setPath(soundFile);
 				} catch(FileNotFoundException fnfe) {}
 			}
 			
 			if (sound.getImagePath() != null) {
 				if (!sound.getImagePath().exists()) {
 					notify(activity, "Sound:\n" + sound.getName() + "\n\nImage file " + doesntExist);
 				} else if (sound.getImagePath().getAbsolutePath().contains(baseDir) == false) {
 					File imageFile = new File(projectDir, sound.getImagePath().getName());
 					try {
 						InputStream in = new FileInputStream(sound.getImagePath().getAbsolutePath());
 					    OutputStream out = new FileOutputStream(imageFile);
 					    IOUtils.copy(in, out);
 						sound.setImagePath(imageFile);
 					} catch(FileNotFoundException fnfe) {}
 				}
 			}
 			
 			if (sound.getActiveImagePath() != null) {
 				if (!sound.getActiveImagePath().exists()) {
 					notify(activity, "Sound:\n" + sound.getName() + "\n\nActive image file" + doesntExist);
 				} else if (sound.getActiveImagePath().getAbsolutePath().contains(baseDir) == false) {
 					File activeImageFile = new File(projectDir, sound.getActiveImagePath().getName());
 					try {
 						InputStream in = new FileInputStream(sound.getActiveImagePath().getAbsolutePath());
 					    OutputStream out = new FileOutputStream(activeImageFile);
 					    IOUtils.copy(in, out);
 						sound.setActiveImagePath(activeImageFile);
 					} catch(FileNotFoundException fnfe) {}
 				}
 			}
 		}
 		Log.v(TAG, boardName + " converted");
 	}
 	
 	private static void notify(final Activity activity, final String text) {
 		activity.runOnUiThread(new Runnable() {
 		    public void run() {
 		    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
             	builder.setMessage(text);
             	builder.show();
 		    }
 		});
 	}
 	
 	public static void duplicateBoard(String boardName) {
 		File sourceLocation = new File(SoundboardMenu.mSbDir + "/" + boardName);
 		File targetLocation = new File(SoundboardMenu.mSbDir + "/" + "duplicate-" + boardName);
 		try {
 			copyDirectory(sourceLocation, targetLocation);
 		} catch (IOException e) {
 			Log.v(TAG, "unable to duplicate", e);
 		}
 	}
 	
 	public static void delete(File f) throws IOException {
 		  if (f.isDirectory()) {
 		    for (File c : f.listFiles())
 		      delete(c);
 		  }
 		  if (!f.delete())
 		    throw new FileNotFoundException("Failed to delete file: " + f);
 	}
 	
 	private static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
 
 		if (sourceLocation.isDirectory()) {
 			if (!targetLocation.exists()) {
 				targetLocation.mkdir();
 			}
 
 			String[] children = sourceLocation.list();
 			for (int i=0; i<children.length; i++) {
 				copyDirectory(new File(sourceLocation, children[i]),
 						new File(targetLocation, children[i]));
 			}
 		} else {
 			copyFile(sourceLocation, targetLocation);
 		}
 	}
 	
 	private static void copyFile(File sourceLocation, File targetLocation) throws IOException {
 		InputStream in = new FileInputStream(sourceLocation);
 		OutputStream out = new FileOutputStream(targetLocation);
 
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 	
 	public static String saveScreenshot(Bitmap bitmap, String boardName) {
 		String returnString;
 		
 		try {
 			SoundboardMenu.mShareDir.mkdirs();
 			File screenshot = new File(SoundboardMenu.mShareDir, boardName + ".png");
 			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(screenshot))) {
 				returnString = "Screenshot saved as sdcard/" + SoundboardMenu.mShareDir.getName() + "/" + screenshot.getName();
 	    	} else {
 	    		returnString = "Couldn't save screenshot";
 	    	}
 			return returnString;
 		} catch (FileNotFoundException e) {
 			BugSenseHandler.log(TAG, e);
 			Log.e(TAG, "Error saving screenshot", e);
 			returnString = "Error saving screenshot";
 		}
 		return returnString;
 	}
 	
 	private int mBoardDirLength;
 	
 	public void zipBoard(String boardName) {
 		
 		try {
 			SoundboardMenu.mShareDir.mkdirs();
 			
 			File inFolder=new File(SoundboardMenu.mSbDir, boardName);
 			File outFile=new File(SoundboardMenu.mShareDir, boardName + ".zip");
 			mBoardDirLength = inFolder.getAbsolutePath().length()-boardName.length();
 		    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
 		    System.out.println("Creating : " + outFile);
 		    zipAddDir(inFolder, out);
 		    out.close();
 		}
 		catch(IOException e) {
 			BugSenseHandler.log(TAG, e);
 			Log.e(TAG, "Error zipping", e);
 		}
 	}
 	
 	void zipAddDir(File dirObj, ZipOutputStream out) throws IOException {
 		File[] files = dirObj.listFiles();
 		byte[] tmpBuf = new byte[1024];
 
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isDirectory()) {
 				zipAddDir(files[i], out);
 				continue;
 			}
 			FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
 			System.out.println(" Adding: " + files[i].getAbsolutePath().substring(mBoardDirLength));
 			out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(mBoardDirLength)));
 			int len;
 			while ((len = in.read(tmpBuf)) > 0) {
 				out.write(tmpBuf, 0, len);
 			}
 			out.closeEntry();
 			in.close();
 		}
 	}
 }
