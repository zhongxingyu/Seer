 package application;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 
 import tools.Shape;
 
 public class MySwerveFile implements SwerveFile {
 	
 	File file;
 	@Override
 	public Object open(JFileChooser filename) {
 		file = filename.getSelectedFile();
 		FileInputStream fileIn = null; 
 		ObjectInputStream in = null;
 		Object o = null;
 		
 		try {
 			fileIn = new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			 in = new ObjectInputStream(fileIn);
 			  o = in.readObject();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return o;
 	}
 
 	@Override
 	public void save(JFileChooser filename, List<Shape> shapes) {
 		file = filename.getSelectedFile();
		String path = file.getPath() + ".swerve";
 		if (!path.contains(".swerve"))
			file = new File(path);
 		FileOutputStream fileOut = null;
 		
 		try {
 			fileOut = new FileOutputStream(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(fileOut);
 			out.writeObject(shapes);
 			out.flush();
 			out.close();
 			//FileUtils.writeLines(file, shapes);
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void close() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
