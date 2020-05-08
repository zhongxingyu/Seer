 package me.yonatan.globals.c2.view;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import lombok.Getter;
 import lombok.Setter;
 import me.yonatan.globals.c2.action.DbManager;
 
 import org.apache.commons.lang3.StringUtils;
 
 @SuppressWarnings("serial")
 @Named
 @SessionScoped
 public class FileImportBean implements Serializable {
 
 	@Inject
 	private LogTableTabsBean tabs;
 
 	@Inject
 	private DbManager dbManager;
 
 	@Getter
 	@Setter
	private String localFileLocation;
 
 	@Inject
 	private Instance<LogTableBean> logTableBeanCreator;
 
 	public void openLocal() {
		System.out.println("Using file "+localFileLocation);
 		if (StringUtils.isBlank(localFileLocation)) {
 			System.out.println("Missing file!!");
 			return; // TODO
 		}
 
 		File file = new File(localFileLocation);
 		if (!file.isFile()) {
 			System.out.println("File not exists!!");
 			return; // TODO
 		}
 
 		System.out.println("Loading file " + logTableBeanCreator);
 		try {
 			String handler = dbManager.importLocalFile(file);
 			LogTableBean ltb = logTableBeanCreator.get();
 			if (ltb.getLogFile() != null) {
 				throw new IllegalStateException("???" + ltb);
 			}
 			ltb.setLogFile(dbManager.getFileInfo(handler));
 			tabs.addTab(ltb);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
