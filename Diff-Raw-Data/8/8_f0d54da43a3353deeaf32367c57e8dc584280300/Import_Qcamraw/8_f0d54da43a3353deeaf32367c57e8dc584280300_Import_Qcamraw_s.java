 import ij.*;
 import ij.process.*;
 import ij.gui.*;
 import java.awt.*;
 import ij.plugin.*;
 import ij.plugin.frame.*;
 import ij.io.OpenDialog;
 import ij.io.FileInfo;
 import ij.io.FileOpener;
 import java.io.*;
 import java.util.*;
 
 public class Import_Qcamraw implements PlugIn {
 
 	public void run(String arg) {
 		OpenDialog od = new OpenDialog("Select a qcamraw file.","");
 		String directory = od.getDirectory();
 		String fileName = od.getFileName();
 		
 		try{
 			HashMap<String, String> headerHash = getHeader(directory, fileName);
 			loadImages(directory, fileName, headerHash);
 			
 		} catch(IOException x){
 			System.err.println("error!");
 		}
 		
 	}
 
 	private HashMap<String, String> getHeader(String directory, String fileName) throws IOException {
 		
 		HashMap<String, String> headerHash = new HashMap<String, String>();
 	
 		FileReader fr = new FileReader(directory + fileName);
 		BufferedReader br = new BufferedReader(fr);
 		
 		for (int k = 0; k < 8; k++){ // the first 8 lines are critical, according to their documentation.
 			String headerLine = br.readLine();
 			String[] keyValue = headerLine.split(": ");
 			headerHash.put(keyValue[0], keyValue[1]);
 		}
 
 		// it would be better if other key-value pairs are stored.  
 		br.close();
 			
 		return headerHash;
 	}
 
 	private void loadImages(String directory, String fileName, HashMap<String, String> headerHash) throws IOException {
 		
 		String headerSizeValue = (String) headerHash.get("Fixed-Header-Size");
 		Integer headerSize = Integer.parseInt(headerSizeValue.replaceAll(" \\[bytes\\]",""));		
 		File f = new File(directory + fileName);
 		long fileLength = f.length();			
 		int frameSize = Integer.parseInt(headerHash.get("Frame-Size").replaceAll(" \\[bytes\\]",""));
 		String[] roi = headerHash.get("ROI").split(", ");		
 		
 		FileInfo fi = new FileInfo();
 		fi.width = Integer.parseInt(roi[2]) - Integer.parseInt(roi[0]);
 		fi.height = Integer.parseInt(roi[3]) - Integer.parseInt(roi[1]);
		fi.offset = 1 + Integer.parseInt(headerSizeValue.replaceAll(" \\[bytes\\]",""));
 		fi.nImages = (int) (fileLength - headerSize) / frameSize;
 		if (headerHash.get("Image-Encoding").matches("raw16")){
			fi.fileType = FileInfo.GRAY16_UNSIGNED;
 		}
		
 		fi.fileName = fileName;
 		fi.directory = directory;
 		new FileOpener(fi).open();
 
 	}
 
 }
