 package com.tp.action;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import com.tp.entity.*;
 import com.tp.utils.DateUtil;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.shiro.authz.annotation.RequiresPermissions;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.dao.HibernateUtils;
 import com.tp.service.CategoryManager;
 import com.tp.service.ClientFileManager;
 import com.tp.service.FileManager;
 import com.tp.utils.Constants;
 import com.tp.utils.FileUtils;
 
 @Namespace("/file")
 @Results({
 		@Result(name = "editinfo", location = "file-info.action", params = { "themeId", "${id}" }, type = "redirect"),
 		@Result(name = "reupload", location = "file-upload.action", type = "redirect"),
 		@Result(name = "reuploadClient", location = "funlocker-client.action", type = "redirect") })
 public class FileUploadAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 	private static final String RELOAD = "reupload";
 	private static final String EDITINFO = "editinfo";
 	private static final String RELOAD_CLIENT = "reuploadClient";
 
 	private File upload;
 	private String uploadFileName;
 
 	private String marketURL;
 
 	private String title;
 	private String version;
 	private String dtype;
 	private Long price;
 	private String shortDescription;
 	private String longDescription;
 	private String author;
 
 	private List<Long> checkedCategoryIds;
 	private List<Long> checkedStoreIds;
 
 	private FileManager fileManager;
 	private CategoryManager categoryManager;
 	private ClientFileManager clientFileManager;
 
 	private Long id;
 
 	@Override
 	public String execute() throws Exception {
 
 		return SUCCESS;
 	}
 
 	@RequiresPermissions("file:edit")
 	public String upload() throws IOException {
 		String extension = FileUtils.getExtension(uploadFileName);
 		if (!extension.equalsIgnoreCase(FileType.ZIP.getValue())) {
 			addActionMessage("请上传一个zip文件");
 			return RELOAD;
 		}
 
 		List<File> files = FileUtils.unZip(upload, Constants.LOCKER_STORAGE);
 		ThemeFile theme = getThemeFile();
 		FileInfo info = getFileInfo();
 		theme = fileManager.saveFiles(files, theme, info);
 		this.setId(theme.getId());
 		addActionMessage("上传成功");
 		return EDITINFO;
 	}
 
 	public String client() throws Exception {
 		return "client";
 	}
 
 	@RequiresPermissions("file:edit")
 	public String uploadClient() throws Exception {
 		String fileName = FileUtils.getFileName(uploadFileName);
 		int indexOfVersion = StringUtils.lastIndexOfIgnoreCase(fileName, "v");
 		String version = StringUtils.substring(fileName, indexOfVersion + 1);
 		File targetDir = new File(Constants.CLIENT_STORAGE);
 		File targetFile = new File(targetDir, uploadFileName);
 		org.apache.commons.io.FileUtils.copyFile(upload, targetFile);
 		ClientFile clientFile = clientFileManager.getClientByVersion(version);
 		if (clientFile == null) {
 			clientFile = new ClientFile();
 			clientFile.setCreateTime(DateUtil.convert(new Date()));
 		} else {
 			clientFile.setModifyTime(DateUtil.convert(new Date()));
 		}
 
 		clientFile.setName(fileName);
 		clientFile.setVersion(version);
 		clientFile.setSize(targetFile.length());
 		clientFile.setPath("client" + File.separator + uploadFileName);
 		clientFileManager.save(clientFile);
 		addActionMessage("上传成功");
 		return RELOAD_CLIENT;
 	}
 
 	private ThemeFile getThemeFile() {
 		ThemeFile theme = new ThemeFile();
 		theme.setName(FileUtils.getFileName(uploadFileName));
 		theme.setTitle(title);
 		theme.setVersion(version);
 		theme.setPrice(new BigDecimal(price));
 		theme.setDtype(dtype);
 		theme.setMarketURL(marketURL);
         theme.setIsnew(0L);
         theme.setIshot(0L);
 		theme.setCreateTime(DateUtil.convert(new Date()));
 		HibernateUtils.mergeByCheckedIds(theme.getCategories(), checkedCategoryIds, Category.class);
 		HibernateUtils.mergeByCheckedIds(theme.getStores(), checkedStoreIds, Store.class);
 		return theme;
 	}
 
 	private FileInfo getFileInfo() {
 		FileInfo info = new FileInfo();
 		info.setTitle(title);
 		info.setPrice(price);
 		info.setLanguage("ZH");
 		info.setLongDescription(longDescription);
 		info.setShortDescription(shortDescription);
 		info.setAuthor(author);
 		return info;
 	}
 
 	public File getUpload() {
 		return upload;
 	}
 
 	public void setUpload(File upload) {
 		this.upload = upload;
 	}
 
 	public String getUploadFileName() {
 		return uploadFileName;
 	}
 
 	public void setUploadFileName(String uploadFileName) {
 		this.uploadFileName = uploadFileName;
 	}
 
 	public List<Category> getAllCategoryList() {
 		return categoryManager.getCategories();
 	}
 
 	public List<Store> getAllStore() {
 		return categoryManager.getAllStore();
 	}
 
 	public void setLongDescription(String longDescription) {
 		this.longDescription = longDescription;
 	}
 
 	public void setShortDescription(String shortDescription) {
 		this.shortDescription = shortDescription;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getDtype() {
 		return dtype;
 	}
 
 	public void setDtype(String dtype) {
 		this.dtype = dtype;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public void setMarketURL(String marketURL) {
 		this.marketURL = marketURL;
 	}
 
 	public void setPrice(Long price) {
 		if (price == null)
 			price = 0L;
 		this.price = price;
 	}
 
 	public void setCheckedCategoryIds(List<Long> checkedCategoryIds) {
 		this.checkedCategoryIds = checkedCategoryIds;
 	}
 
 	public void setCheckedStoreIds(List<Long> checkedStoreIds) {
 		this.checkedStoreIds = checkedStoreIds;
 	}
 
     public List<FileTag> getTagList(){
         return categoryManager.getAllTags();
     }
 
 	@Autowired
 	public void setFileManager(FileManager fileManager) {
 		this.fileManager = fileManager;
 	}
 
 	@Autowired
 	public void setCategoryManager(CategoryManager categoryManager) {
 		this.categoryManager = categoryManager;
 	}
 
 	@Autowired
 	public void setClientFileManager(ClientFileManager clientFileManager) {
 		this.clientFileManager = clientFileManager;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 }
