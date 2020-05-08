 import java.io.*;
 
 import ij.*;
 import ij.io.*;
 import ij.gui.*;
 import ij.plugin.*;
 import com.drew.metadata.*;
 import com.drew.imaging.*;
 import com.drew.metadata.exif.ExifSubIFDDirectory;
 
 
 
 public class List_Files implements PlugIn {
    public void run(String arg) {
 	  String[] syncMethodList = {"Assume already synced", "Enter offset", "Calculate offset from pair"};
 	  String visDirectory=null;
       String IR_Directory=null;
       File visFolder=null;
       File IR_Folder=null;
       String syncMethod = Prefs.get("pm.syncMethod", syncMethodList[2]);
       long offset = 0;
       double acceptableDifference = Prefs.get("pm.acceptableDifference", 1.0);
 
       // create a dialog with two numeric input fields
       
       GenericDialog gd = new GenericDialog("Image pair matching");
       gd.addChoice("Select offset calculation method:", syncMethodList, syncMethod);
       gd.addNumericField("Max difference in seconds between offset-corrected image pairs:",acceptableDifference, 2);
 		
       // show the dialog and quit, if the user clicks "cancel"
       gd.showDialog();
       if (gd.wasCanceled()) {
          return;
       }
       
       syncMethod = gd.getNextChoice();
       acceptableDifference = gd.getNextNumber();
       
       // Set preferences to IJ.Prefs file
       Prefs.set("pm.syncMethod", syncMethod);
       Prefs.set("pm.acceptableDifference", acceptableDifference);
    		
    	  // Save preferences to IJ.Prefs file
       Prefs.savePreferences();
       
       acceptableDifference = acceptableDifference * 1000.0;
      if (syncMethod == "Assume synced" | syncMethod == "Enter offset") {
         if (syncMethod == "Assume synced") offset = 0; 
          else {
         	 double offsetD = Prefs.get("pm.offsetD", 0.0);
         	 offsetD = IJ.getNumber("Enter the offset (Vis - NIR) in seconds", offsetD);
         	 if (offsetD == IJ.CANCELED) {
         		 return;
         	 }
         	 Prefs.set("pm.offsetD", offsetD);
         	 Prefs.savePreferences();
         	 offset = (long)offsetD;
          }
          DirectoryChooser dcVis = new DirectoryChooser("Select a directory with visible images");
          visDirectory = dcVis.getDirectory();
          if (visDirectory == null) {
         	 IJ.error("No visible image directory was selected");
         	 return;
          }
          DirectoryChooser dcIR = new DirectoryChooser("Select a directory with IR images");
           IR_Directory = dcIR.getDirectory();
          if (IR_Directory == null) {
         	 IJ.error("No near-IR image directory was selected");
         	 return;
          }
       } 
      else if (syncMethod == "Calculate from pair"){
          OpenDialog odVis = new OpenDialog("Open visible reference image", arg);
          visDirectory = odVis.getDirectory();
          String visFileName = odVis.getFileName();
          if (visFileName==null) {
         	 IJ.error("No file was selected");
         	 return;
          }
          File visRefFile = new File(visDirectory, visFileName);
          OpenDialog odIR = new OpenDialog("Open near IR reference image", arg);
          IR_Directory = odIR.getDirectory();
          String IR_FileName = odIR.getFileName();
          if (IR_FileName==null) {
         	 IJ.error("No file was selected");
         	 return;
          }
          File IR_RefFile = new File(IR_Directory, IR_FileName);
          long visTime = getExifTime(visRefFile);
          long IR_Time = getExifTime(IR_RefFile);
          offset = (long) (visTime - IR_Time);
       }
 
       visFolder = new File(visDirectory);
       IR_Folder = new File(IR_Directory);
       FilePairList photoPairs = new FilePairList(IR_Folder.listFiles(), visFolder.listFiles(), offset, acceptableDifference);
            
       SaveDialog sd = new SaveDialog("Output text file", "matchedImages",".txt");
       String dir = sd.getDirectory();
       String fileName = sd.getFileName();
       
       if (fileName==null) {
      	 IJ.error("No output file was selected");
     	 return;
      }
       photoPairs.writeFilePairs(dir, fileName);
    } 
    
 // Get the original image date from the EXIF tag
 	long getExifTime(File file){
 		long time = 0;
 		boolean usingExif = false;
 		try {
 			Metadata metadata = ImageMetadataReader.readMetadata(file);
 			ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
 			boolean hasTag = directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
 			if (hasTag) {
 				time = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL).getTime();
 				usingExif = true;
 }
 		} catch (ImageProcessingException e) {
             String msg = e.getMessage();
             if (msg==null) msg = ""+e;
             IJ.error("Error extracting EXIF metadata from file \n" + file.getAbsolutePath()); 
             usingExif = false;
         } 
 		catch (IOException e) {
 			e.printStackTrace();
 			usingExif = false;;
 		}
 		if (!usingExif) {
 			file.lastModified();
 			usingExif = false;
 		}
 		
 		return time;
 	}
 }
