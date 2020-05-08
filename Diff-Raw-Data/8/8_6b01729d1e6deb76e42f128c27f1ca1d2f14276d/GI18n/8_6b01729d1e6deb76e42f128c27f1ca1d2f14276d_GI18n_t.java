 /**
  * 
  */
 package com.mlp.util.i18n;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 /**
  *
  *@author Colin Doug
  *version：1.0
  *des：
  * g_i18n.conf
  * bundleFileName+language+country.properties
  * is needed at the same path.
  * default：GResourceBundle_zh_CN.properties
  */
 public class GI18n {
 
 	/**
 	 * 
 	 */
 	public GI18n() {
 		super();
 		loadConfFile();
 	}
 	public GI18n(String bundleFileName) {
 		super();
 		if(bundleFileName!=null){
 			this.bundleFileName= bundleFileName;
 		}
 //		if(language!=null){
 //		this.language = language;
 //		}
 //		if(country!=null){
 //			this.country = country;
 //		}
 		
 	}
 	
 	Properties config = null;
 	
 	private String language="zh";
 	private String country = "CN";
 //	private  String postfix= "_"+language+"_"+country+".properties";
 	private String bundleFileName="GResourceBundle";
 	
 //	private String composeFileName(String filePre){
 //		
 //		filePre = filePre+"_"+language+"_"+country+".properties";
 //		return filePre;
 //	}
 	
	private  final String conffile= System.getProperty("user.dir")+"/conf/g_i18n.conf";
 	public  Properties loadConfFile(){
 		Properties p =null;
 		File f= new File(conffile);
 		if(f.exists() && f.isFile()){
 			FileInputStream fin;
 			try {
 				fin = new FileInputStream(f);
 				p = new Properties();
 				p.load(fin);
 				return p;
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 //				p.setProperty("language","zh");
 //				p.setProperty("country","CN");
 				return null;
 			}
 			
 		}else {
			System.err.println("Not found "+conffile);
 			
 
		String dp =System.getProperty("user.dir")+"/conf";
 		String fp =conffile;
 		File d =new File(dp);
 		if(!d.exists()){
 			d.mkdir();
 		}
 		f = new File(fp);
 		if(!f.exists()){
 			try {
 				f.createNewFile();
 			} catch (IOException e) {
 				
 				e.printStackTrace();
 			}
 		}
 		f=null;
 			
 //			p.setProperty("language","zh");
 //			p.setProperty("country","CN");
 			return null;
 		}
 		
 	}
 	private Locale currentLocale=null;
 	private ResourceBundle resourceBundle=null; 
 	public  void init(){
 //		currentLocale = new Locale(getConf().getProperty("language"),getConf().getProperty("country"));
 		 
 	}
 	public Properties getConf(){
 		if(config==null){
 			config = loadConfFile();
 		}
 		return config;
 	}
 	public  Locale getCurrentLocale(){
 		if(currentLocale==null){
 //			System.out.println("language="+language+" country="+country);
 			if(getConf()!=null){//配置文件优先
 				currentLocale=new Locale(getConf().getProperty("language"),getConf().getProperty("country"));
 			}else{
 				currentLocale=new Locale(language,country);
 			}
 		}
 		return currentLocale;
 	}
 	public ResourceBundle createResourceBundle(String filename){
 //		System.out.println(filename);
 //		System.out.println(System.getProperty("user.dir"));
 		resourceBundle = ResourceBundle.getBundle(filename,getCurrentLocale());
 		return resourceBundle;
 	}
 	
 	public ResourceBundle getResourceBundle(){
 		if(resourceBundle==null){
 			
 			resourceBundle=createResourceBundle(bundleFileName);//bundleFileName composeFileName(bundleFileName)
 		}
 		return resourceBundle;
 	}
 	public  String getString(String msg){
 		
 		String s=null;
 		
 		try {
 			s =getResourceBundle().getString(msg);
 		} catch (Exception e) {
 			return msg;
 		}
 		if(s==null || s.length()<1){
 			return msg;
 		}
 		return s;
 	}
 
 	
 }
