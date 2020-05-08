 import java.io.File;
 import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 
 /**
  * This little app reads .java files from the current directory and attemps to move them to appropriate
  * subfolders according to their "package"
  *
  * @author Felipe Weber <wbr.felipe@gmail.com>
 **/
 public class OrganizeJavaSources {
 	
 	public static void main(String args[]) {
 		try {
 			File workingDir = new File(System.getProperty("user.dir"));
 			for (String currFileName : workingDir.list()) {
				File currFile = new File(workingDir.getCanonicalPath() + "/" + currFileName);
 				if (currFile.isDirectory() || !currFile.toString().endsWith(".java"))
 					continue;
 				RandomAccessFile raf = new RandomAccessFile(currFile, "r");
 				Path toDir = FileSystems.getDefault().getPath(workingDir.toString());
 				for (String line = new String(); line != null; line = raf.readLine() ) {
 					if (line.startsWith("package")) {
 						String packagePath = line.split(" ")[1];
 						String[] packageNames = packagePath.split("\\.");
 						for (String packageName : packageNames) {
 							if (packageName.endsWith(";")) {
 								packageName = packageName.substring(0, packageName.length() - 1);
 							}
 							toDir = FileSystems.getDefault().getPath(toDir.toAbsolutePath().toString(), packageName);
 							File packageDir = toDir.toFile();
 							if (!packageDir.exists()) {
 								packageDir.mkdir();
 							}
 						}
 						break;
 					}
 				}
 				raf.close();
				Path fromFile = FileSystems.getDefault().getPath(currFile.getAbsolutePath());
 				Path toFile = FileSystems.getDefault().getPath(toDir.toAbsolutePath().toString(), fromFile.getFileName().toString());
 				Files.move(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
