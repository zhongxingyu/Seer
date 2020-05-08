 package org.imirsel.nema.webapp.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.imirsel.nema.model.ExecutableBundle;
 import org.imirsel.nema.model.NemaZipFile;
 import org.springframework.web.multipart.MultipartFile;
 
 public class UploadedExecutableBundle extends ExecutableBundle {
 
    /**
     * Version of this class.
     */
    private static final long serialVersionUID = -8886083754671426825L;
    
    private File uploadedFile;
    private MultipartFile file;
    private String group;
    
    public MultipartFile getFile() {
       return file;
    }
    
    public void setFile(MultipartFile file) throws IOException {
       try {
          setFileName(file.getOriginalFilename());
          setBundleContent(file.getBytes());
          persistUploadedFile();
       } catch (IOException e) {
          throw new IOException("An error occured while retrieving the " +
                "bytes of the uploaded file.",e);
       }
    }
    
    public ExecutableType[] getTypeOptions(){
       return new ExecutableType[]{ExecutableType.JAVA,ExecutableType.MATLAB,ExecutableType.C,ExecutableType.SHELL};
    }
 
    public String getGroup() {
       return group;
    }
 
    public void setGroup(String group) {
       this.group = group;
    }
 
    @Override
    public void setExecutableName(String executableName) {
       if(getType()==ExecutableType.JAVA) {
          setMainClass(executableName);
       } else {
          setExecutableName(executableName);
       }
    }
    
    @Override
    public String getExecutableName() {
       if(getType()==ExecutableType.JAVA) {
         return super.getMainClass();
       } else {
         return super.getExecutableName();
       }
    }
    
    public boolean containsExecutable() throws IOException {
       ZipFile uploadedZipFile = new ZipFile(uploadedFile);
       NemaZipFile nemaZipFile = new NemaZipFile(uploadedZipFile);
       nemaZipFile.open();
       boolean containsExecutable = false;
       if(getType()==ExecutableType.JAVA) {
          System.out.println(getMainClass());
          containsExecutable = nemaZipFile.containsClass(getMainClass());
       } else {
          containsExecutable = nemaZipFile.containsFile(getExecutableName());
       }
       nemaZipFile.close();
       uploadedZipFile.close();
       return containsExecutable;
    }
    
    
    private void persistUploadedFile() throws IOException {
       String tempDir = System.getProperty("java.io.tmpdir");
       String fileSeparator = System.getProperty("file.separator");
       
       uploadedFile = new File(tempDir + fileSeparator + 
                                            getFileName() + 
                                            "_" + UUID.randomUUID());
       FileOutputStream fos = null;
       
       try {
          fos = new FileOutputStream(uploadedFile);
          fos.write(getBundleContent());
       } catch (IOException e) {
          throw new IOException("Uploaded file could not be saved to disk.",e);
       } finally {
          try {
             if(fos!=null) {
                fos.flush();
                fos.close();
             }
          } catch (IOException e) {
             e.printStackTrace();
          }
       }
    }
    
    public boolean passesIntegrityCheck() {
       boolean valid = false;
       try {
          // Exception will be thrown if file is not valid
          new ZipFile(uploadedFile);
          valid = true;
       } catch (ZipException e) {
          e.printStackTrace();
       } catch (IOException e) {
          e.printStackTrace();
       }
       return valid;
    }
 }
