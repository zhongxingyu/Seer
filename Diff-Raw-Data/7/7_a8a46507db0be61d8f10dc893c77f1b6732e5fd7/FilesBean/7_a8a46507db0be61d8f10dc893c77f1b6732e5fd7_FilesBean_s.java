 package gov.usgs.gdp.bean;
 
 import gov.usgs.gdp.helper.FileHelper;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 
 @XStreamAlias("file-set")
 public class FilesBean implements XmlBean {
 	@XStreamAlias("file-set-name")
 	@XStreamAsAttribute
 	private String name;
 
     @XStreamAlias("user-directory")
 	@XStreamAsAttribute
     private String userDirectory;
 	private static final long serialVersionUID = 1L;
 
 
 
 	@XStreamAlias("files")
 	@XStreamImplicit
 	private Collection<File> files;
 	
 	public String toXml() {
 		XStream xstream = new XStream();
 		xstream.processAnnotations(FilesBean.class);
 		return xstream.toXML(this);
 	}
 	
 	public ShapeFileSetBean getShapeFileSetBean() {
 		ShapeFileSetBean result = new ShapeFileSetBean();
 		for (File file : this.files) {			
 			if (file.getName().toLowerCase().contains(".shp")) {
 				result.setShapeFile(file);
 			} else if (file.getName().toLowerCase().contains(".prj")) {
 				result.setProjectionFile(file);
 			} else if (file.getName().toLowerCase().contains(".dbf")) {
 				result.setDbfFile(file);
 			}
 		}
 		return result;
 	}
 
 	public Collection<File> getFiles() {
 		if (this.files == null) this.files = new ArrayList<File>();
 		return this.files;
 	}
 
 	public void setFiles(Collection<File> localFiles) {
 		this.files = localFiles;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String localName) {
 		this.name = localName;
 	}
 	
 	public static List<FilesBean> getFilesBeanSetList(String exampleDir, String userDir) {
 		List<FilesBean> result = new ArrayList<FilesBean>();
 		result.addAll(FilesBean.getFilesBeanSetList(exampleDir, true));
                 List<FilesBean> userFiles = new ArrayList<FilesBean>();
 		if (!"".equals(userDir)) {
             //result.addAll(FilesBean.getFilesBeanSetList(userDir, false));
             userFiles = FilesBean.getFilesBeanSetList(userDir, false);
             for (FilesBean filesBean : userFiles) {
                 filesBean.setUserDirectory(userDir.substring(userDir.lastIndexOf('/') + 1));                        
             }
             result.addAll(userFiles);
         }
 		return result;
 	}
 
 	public static List<FilesBean> getFilesBeanSetList(String directory, boolean recursive) throws IllegalArgumentException {
 		return FilesBean.getFilesBeanSetList(FileHelper.getFileCollection(directory, recursive));
 	}
 	
 	/**
 	 * Gets a list of FileBean objects organized from a Collection of File objects
 	 * 
 	 * @param files
 	 * @return
 	 */
 	public static List<FilesBean> getFilesBeanSetList(Collection<File> files) {
 		if (files == null) return null;
 		List<FilesBean> result = new ArrayList<FilesBean>();
 		Map<String, FilesBean> filesBeanMap = new HashMap<String, FilesBean>();
 		
 		for (File file : files) {
 			String fileName = file.getName();
			String fileNameWithoutSuffix = fileName.substring(0, fileName.lastIndexOf('.'));
			
 			//Check if we already have a FilesBean by this name
 			FilesBean filesBean = filesBeanMap.get(fileNameWithoutSuffix);
 			if (filesBean == null) {
 				filesBean = new FilesBean();
 				filesBean.setName(fileNameWithoutSuffix);
 			}
 			filesBean.getFiles().add(file);
 			filesBeanMap.put(fileNameWithoutSuffix, filesBean);
 		}
 		
 		Iterator<String> filesBeanMapIterator = filesBeanMap.keySet().iterator();
 		
 		while (filesBeanMapIterator.hasNext()) {
 			String key = filesBeanMapIterator.next();
 			result.add(filesBeanMap.get(key));
 		}
 		return result;
 	}
 
     /**
      * @return the userDirectory
      */
     public String getUserDirectory() {
         return userDirectory;
     }
 
     /**
      * @param userDirectory the userDirectory to set
      */
     public void setUserDirectory(String userDirectory) {
         this.userDirectory = userDirectory;
     }
 }
