 package ch20.ex09;
 
 import java.io.File;
 import java.io.IOException;
 
 public class FileInfoReader {
 	public static void showFileProperty(File[] files) throws IOException {
 		if (files == null)
 			return;
 		
 		for (File file : files) {
 			System.out.println("getName() = " + file.getName());
 			System.out.println("getPath() = " + file.getPath());
 			System.out.println("getAbsolutePath() = " + file.getAbsolutePath());
 			System.out.println("getCanonicalPath() = " + file.getCanonicalPath());
 			System.out.println("getParent() = " + file.getParent());
 			System.out.println("lastModified() = " + file.lastModified());
 			System.out.println("length() = " + file.length());
 			System.out.print("list() = ");
 			String[] sameDirFiles = file.list();
 			if (sameDirFiles != null) {
 				for (String f : sameDirFiles) {
 					System.out.print(f + ", ");
 				}
 			} else {
 				System.out.print("0");
 			}
 			System.out.println("");
 		}
 	}
 	
 	public static void main(String[] args) throws IOException {
 		final String INPUT_FILE_PATH_1 = "JPL/ch20/ex09/input1.txt";
 		final String INPUT_FILE_PATH_2 = "JPL/ch20/ex09/input2.txt";
 		File file1 = new File(INPUT_FILE_PATH_1);
 		File file2 = new File(INPUT_FILE_PATH_2);
 		File[] files = {file1, file2};
 		showFileProperty(files);
 	}
 }
