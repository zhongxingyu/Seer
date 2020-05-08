 /**
  * 
  */
 package com.meaglin.sms.model;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.meaglin.sms.SmsServer;
 
 /**
  * 
  * @author Joseph Verburg
  * 
  */
 public class ServerCategory {
 
 	private SmsServer controller;
 
 	private int id, categoryid, serverid;
 	private String name, path;
	// Temporary fix for too many deep mounts
	private boolean dontScan;
 
 	private Category category;
 	private Server server;
 
 	private String[] ignores;
 
 	public ServerCategory(SmsServer smsServer, Server server) {
 		controller = smsServer;
 		this.server = server;
 		ignores = controller.getDirectoryIgnores();
 	}
 
 	public List<ServerFile> getServerFiles() {
 		List<ServerFile> files = new ArrayList<>();
		if(dontScan) {
			return files;
		}
 		ServerFile sf;
 		File[] dirfiles;
 		getServer().log("Parsing " + getDirectory().getParent() + "/" + getDirectory().getName());
 		dirs: for (File directory : getFiles()) {
 			if (!directory.isDirectory()) {
 				continue; // only dirs.
 			}
 
 			for (String ignore : ignores) {
 				if (directory.getName().startsWith(ignore)) {
 					continue dirs;
 				}
 			}
 
 			dirfiles = directory.listFiles();
 			if (dirfiles == null) { // System Volume Information >.>
 				continue;
 			}
 			for (File sub : dirfiles) {
 				sf = new ServerFile();
 				sf.setServercategory(this);
 				sf.setServer(getServer());
 				sf.setName(sub.getName());
 				sf.setDisplayname(sub.getName());
 				sf.setDirectory(directory.getName());
 				sf.setDisplaydirectory(directory.getName());
 				sf.setServerpath(sub.getPath());
 				sf.setFlag(ServerFile.CREATED);
 				sf.setPath("");
 				sf.setCreated();
 
 				// TODO: this is slow, find a better way.
 				// sf.setType(sub.isDirectory() ? "directory" : "file");
 				sf.setType("unknown");
 				// TODO: fill these.
 				sf.setExtension("");
 				files.add(sf);
 			}
 
 		}
 		return files;
 	}
 
 	private File[] getFiles() {
 		return getDirectory().listFiles();
 	}
 
 	public File getDirectory() {
 		return new File(getServer().getPath(), getPath());
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @return the categoryid
 	 */
 	public int getCategoryid() {
 		return categoryid;
 	}
 
 	/**
 	 * @return the serverid
 	 */
 	public int getServerid() {
 		return serverid;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the path
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	/**
 	 * @return the category
 	 */
 	public Category getCategory() {
 		return category;
 	}
 
 	/**
 	 * @return the server
 	 */
 	public Server getServer() {
 		return server;
 	}
 
 	/**
 	 * @return the controller
 	 */
 	public SmsServer getController() {
 		return controller;
 	}
 
 	/**
 	 * @param id
 	 *			the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	/**
 	 * @param categoryid
 	 *			the categoryid to set
 	 */
 	public void setCategoryid(int categoryid) {
 		this.categoryid = categoryid;
 	}
 
 	/**
 	 * @param serverid
 	 *			the serverid to set
 	 */
 	public void setServerid(int serverid) {
 		this.serverid = serverid;
 	}
 
 	/**
 	 * @param name
 	 *			the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @param path
 	 *			the path to set
 	 */
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	/**
 	 * @param category
 	 *			the category to set
 	 */
 	public void setCategory(Category category) {
 		if (this.category != null) {
 			this.category.forget(this);
 		}
 		this.category = category;
 		this.category.learn(this);
 	}
 
 	public ServerCategory bind(ResultSet res) throws SQLException {
 		id = res.getInt("id");
 		categoryid = res.getInt("categoryid");
 		serverid = res.getInt("serverid");
 		name = res.getString("name");
 		path = res.getString("path");
		dontScan = res.getBoolean("dontscan");
 		return this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ServerCategory other = (ServerCategory) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 }
