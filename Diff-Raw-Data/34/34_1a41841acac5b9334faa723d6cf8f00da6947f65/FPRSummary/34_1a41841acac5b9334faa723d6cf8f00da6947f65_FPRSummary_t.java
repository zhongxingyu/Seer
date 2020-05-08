 package org.jvnet.hudson.plugins.fortify360;
 
 import hudson.XmlFile;
 
 import java.io.*;
 
 public class FPRSummary {
 	
 	public final static String FILE_BASENAME = "fortify360_plugin.xml";
 	
 	private String fprFullPath;
 	private Double nvs;
 	private Integer failedCount;
 	
 	/* Not used at the monent */
 	private Integer hot;
 	private Integer warning;
 	private Integer info;
 	
	public FPRSummary() { 
		nvs = 0.0;
		failedCount = 0;
	}
 	
 	public void load(File file) throws IOException {
 		XmlFile xml = new XmlFile(file);
 		nvs = (Double) xml.read();
 	}
 	
 	public void save(File file) throws IOException {
 		// save data under the builds directory, this is always in Hudson master node
 		XmlFile xml = new XmlFile(file);
 		xml.write(nvs);
 	}
 
 	public Double getNvs() {
 		return nvs;
 	}
 
 	public void setNvs(Double nvs) {
 		this.nvs = nvs;
 	}
 
 	public String getFprFullPath() {
 		return fprFullPath;
 	}
 
 	public void setFprFullPath(String fprFullPath) {
 		this.fprFullPath = fprFullPath;
 	}
 
 	public Integer getFailedCount() {
 		return failedCount;
 	}
 
 	public void setFailedCount(Integer failedCount) {
 		this.failedCount = failedCount;
 	}
 }
