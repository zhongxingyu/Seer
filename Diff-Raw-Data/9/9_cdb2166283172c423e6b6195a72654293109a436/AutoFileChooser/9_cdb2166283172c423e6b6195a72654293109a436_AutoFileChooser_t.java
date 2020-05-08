 package data;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import main.God;
 
 public class AutoFileChooser {
 	
 	private int pos = 0;
 	private ArrayList<File> images = new ArrayList<File>();
 	
 	public AutoFileChooser(String path) {
 		int pathLength = path.length() - 1;
 		for (int i = pathLength; i >= 0; i--) {
 			if (path.charAt(i) == File.separatorChar) {
 				path = path.substring(0,i+1); //+ 1 means we keep separator
 				break;
 			}
 		}
 		File folder = new File(path);
 		for (File f : folder.listFiles()) {
 			if (f.isFile()) {
 				String fileName = f.getName();
 				if (fileName.endsWith(".tiff") || 
 						fileName.endsWith(".tif") || 
 						fileName.endsWith(".gif") || 
 						fileName.endsWith(".jpeg") || 
 						fileName.endsWith(".jpg") || 
 						fileName.endsWith(".png")) {
 					images.add(f.getAbsoluteFile());
 					System.out.println(f.getAbsoluteFile());
 				}
 			}
 		}
 		if (images.size() == 0) {
 			System.out.println("No images found in directory");
 		}
 	}
 	
 	public void loadNextFile() {
 		File next = null;
 		if (images.size() > 0) {
 			//increment pos
 			pos++;
 			if (pos >= images.size()) {
 				pos = 0;
 			}
			next = images.get(pos);
			
 			if (next != null) {
 				//this is unnecessary check
 				loadImage(next);
 			}
 		}
 	}
 	
 	public void loadPreviousFile() {
 		if (images.size() > 0) {
 			//decrement pos
 			pos--;
 			if (pos < 0) {
 				pos = images.size()-1;
 			}
			File next = images.get(pos);
 			
 			if (next != null) {
 				//this is unnecessary check
 				loadImage(next);
 			}
 		}
 	}
 	
 	public void loadImage(File file) {
 		String text = file.getAbsolutePath();
 		if(God.dirtyFlag)
 		{
 			int response = JOptionPane.showConfirmDialog(null,"You have not saved your labels. \nWould you like to continue loading a new image?"
 					,"New Image",JOptionPane.YES_NO_OPTION);
 			if(response == JOptionPane.NO_OPTION)
 				return;
 		}
 		
 		System.out.println("Loading.. " + text);
 		try 
 		{
 			// TODO SAVE BEFORE NEW PROJECT
 			/*** Reset properties for new image ***/
 			ShapeData shapeData = new ShapeData();
 			God.shapeData = shapeData;
 			God.imagePanel.newImage(text);
 			God.fileName.setText(file.getName());
 			// Empty thumbnail list
 			God.shapeList.emptyList();
 			God.dirtyFlag = false;
 			God.vertexPanel.repaint();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 }
