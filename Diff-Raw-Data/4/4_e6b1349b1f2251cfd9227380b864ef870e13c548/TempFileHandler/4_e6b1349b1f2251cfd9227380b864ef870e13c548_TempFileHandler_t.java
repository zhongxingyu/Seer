 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmlExercises;
 
 import java.io.*;
 
 
 /**
  *
  * @author Zlej robočlověk
  */
 
 public class TempFileHandler{
     
     String tempPath;
     String type;
     File tempDir;
     
     
     /*
      * [in] String tempPathName -   path where the files are to be stored
      * [in] String fileType -       expects "xsd", "dtd" extension the files will use AND subdir
      *                              that will be used to store these files
      * TODO> parsing fileType (neccessary?)
      * 
      * Creates class that opens main directory with subdirectory for
      * specific file type. For each filetype there should be created
      * new instance of class. (To ease further overloading, if the DTD/XSD
      * differences will require it).
      * Throws obvious IOException, when the dir cannot be found or 
      * subdir found/created
      */
     public TempFileHandler(String tempPathName, String fileType) throws IOException{
         File rootTempPath = new File(tempPathName);
        type = fileType;
         
         if(!rootTempPath.isDirectory()){
             System.out.println("invalid dir: "+rootTempPath);
             throw new IOException("Directory does not exist");
         }
         
         tempPath = tempPathName + "/" + fileType;
         tempDir =  new File(rootTempPath, fileType);        
         if(!tempDir.isDirectory()){
             tempDir.mkdir();
         }
         if(!rootTempPath.isDirectory()){
             System.out.println("invalid root/dir");
             throw new IOException("Directory/"+fileType+" does not exist");
         }
     }
     /*
      * [in] String exercise - DTD plaintext to save
      * [in] File xmlFile    - XML file to save too
      * 
      * return - path to XML file
      * 
      * Method adds temporary file to the folder, while creating a
      * filename thats not taken. Returns path to created file
      * 
      * TODO> better exceptions. Maybe split methods for easier catching? New exception handler?
      */
     public String addDirectory(String exercise, String DTDname, File xmlFile) throws IOException, OverflowException{
         String result;        
         File foundDir;
         File DTDEx = new File(exercise);
         
         String rootDir = findDirName();
         
         foundDir = new File(rootDir);
         File xmlCopy = new File(foundDir, xmlFile.getName());
         if(foundDir.mkdir()){
 
             FileWriter fw = new FileWriter(rootDir+DTDname);
             BufferedWriter bw = new BufferedWriter(fw);
             bw.write(exercise);
             fw.close();
             
             if(xmlCopy.createNewFile()){
                 copy(xmlFile, xmlCopy);            
             }else{
                 throw new IOException("XML soubor jiz existuje.");
             }
         }else{
             throw new IOException("Adresar jiz existuje.");
         }
         return xmlCopy.getPath();
     }
 
     /*
      * [in] String exercise - plaintext to save
      *
      * return - path to file
      * 
      * Method adds temporary file to the folder, while creating a
      * filename thats not taken. Returns path to created file
      * 
      * TODO> better exceptions. Maybe split methods for easier catching? New exception handler?
      */
     public String addFile(String exercise) throws IOException, OverflowException{
         String result;        
         File foundDir;
         
         result = findFileName();
         
        foundDir = new File(result+type);
         if(foundDir.createNewFile()){
             FileWriter fw = new FileWriter(foundDir);
             BufferedWriter bw = new BufferedWriter(fw);
             bw.write(exercise);
             fw.close();            
         }else{
             throw new IOException("Soubor jiz existuje.");
         }
         return result;       
     }
     
     public void deleteFile(String delPath) throws IOException{
         File delFile = new File(delPath);
         if(!delFile.delete()) throw new IOException("Failed to delete file");
     }
     
     
     public void deleteDirectory(String delPath) throws IOException{
         File delDir = new File(delPath);
         File[] delist = delDir.listFiles();
         
         for(int i=0; i<delist.length; i++){
             if(!delist[i].delete()) throw new IOException("Failed to delete file in DIR");
         }
         
         if(!delDir.delete()) throw new IOException("Failed to delete DIR");
     }
 
     
     private String findFileName() throws IOException, OverflowException{
         
         String resPath ="NOPE!";
         String list[] = tempDir.list();        
         boolean found = false;
         
         if(list.length == 0){
             resPath = tempPath + "/0."+ type;
             found = true;
         }
         //check whether name is taken
         
         for(int i = 0; i<list.length; i++){
             boolean taken = false;
             for(int j = 0; j<list.length; j++){
                 if(Integer.parseInt(list[j].split("\\.")[0])==i) taken = true;
             }
             if(!taken){
                 found = true;
                 resPath = tempPath + "/"+i+"."+ type;               
                 break;
             }
         }
         if(!found) throw new OverflowException("Doslo k preteceni buferu docasnych souboru");
         if(resPath.equals("NOPE!")) throw new OverflowException("Tohle se vůbec nemělo stát");
         return resPath;
     }
     private String findDirName() throws IOException, OverflowException{
         
         String resPath ="NOPE!";
         String list[] = tempDir.list();        
         boolean found = false;
         
         if(list.length == 0){
             resPath = tempPath + "/0."+ type;
             found = true;
         }
         //check whether name is taken
         
         for(int i = 0; i<list.length; i++){
             boolean taken = false;
             for(int j = 0; j<list.length; j++){
                 if(Integer.parseInt(list[j])==i) taken = true;
             }
             if(!taken){
                 resPath = tempPath;
                 found = true;
 
                 break;
             }
         }
         if(!found) throw new OverflowException("Doslo k preteceni buferu docasnych Adresaru");
         if(resPath.equals("NOPE!")) throw new OverflowException("Tohle se vůbec nemělo stát");
         return resPath;
     }
     private void copy(File original, File copy) throws IOException{
         InputStream in = new FileInputStream(original);  
         OutputStream out = new FileOutputStream(copy);
 
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0){
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
 }
