 package com.dbdoc;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.dbdoc.db.model.provider.TableProvider;
 import com.dbdoc.utils.FreemarkerUtils;
 
 /***
  * ˵ĵ-
  * @author moonights
  *
  * @date 2011-11-23
  */
 public class Main {
 	public static final Logger log = Logger.getLogger(Main.class); 
	public static final String TEMPLEATE_DEFAUTL="template/dbdoc_siample.xml";
	public static final String OUTER_default="c:\\temp-output\\dbdoc_siample.doc";
 	
 	public static void main(String args[]) throws IOException{
 		log.info("<<<<<<<<<<<<<<<<<<<ݿĵɿʼ>>>>>>>>>>>>>>>>>>>>>");
 		Map propMap=new HashMap();
 		try {
 			List tables = TableProvider.getInstance().getAllTables();
 			propMap.put("tableList", tables);
 			FreemarkerUtils.writeTemplateToFile(Main.TEMPLEATE_DEFAUTL, propMap, Main.OUTER_default);
 		} catch (SQLException e) {
 			// TODO Զ catch 
 			log.info("ĵгִ"+e.toString());
 		}
 		log.info("<<<<<<<<<<<<<<<<<<<ݿĵɽ>>>>>>>>>>>>>>>>>>>>>");
 		Runtime.getRuntime().exec("cmd.exe /c start c:\\temp-output");
 	}
 }
