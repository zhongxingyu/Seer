 package edu.uc.cs.distsys.properties;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import edu.uc.cs.distsys.Logger;
 import edu.uc.cs.distsys.Node;
 import edu.uc.cs.distsys.init.Cookie;
 
 public class NodePropertiesManager {
 	private Properties properties;
 	private File file;
 	private Logger logger;
 	private Node node;
 	
 	public NodePropertiesManager(String name, Node myNode, Logger logger) {
 		this.node = myNode;
 		this.logger = logger;
 		if (name.equals("")) {
 			this.file = new File(this.node.getId() + ".properties");
 		} else {
 			this.file = new File(name + ".properties");
 		}
 		if (file.exists()) {
			logger.error("File '" + file.getName() + ".properties' already exists, I guess we can try to use it");
 		}
 		this.properties = new Properties();
 	}
 
 	public boolean load() {
 		boolean success = false;
 		try {
 			// use local file for storage
 			if (this.file.exists()) {
 				FileInputStream input = new FileInputStream(this.file);
 				this.properties.load(input);
 				// update our internal state to match the data in the file
 				this.updateInternalNode();
 				input.close();
 				display();
 				success = true;
 			} else {
 				logger.debug("File will be created when saved...");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return success;
 	}
 	
 	public boolean save() {
 		FileOutputStream output = null;
 		boolean success = false;
 		try {
 			File nodeStorageFile = new File(this.file.getAbsolutePath());
 			if (!nodeStorageFile.exists()) {
 				// create the file
 				nodeStorageFile.createNewFile();
 			}
 			output = new FileOutputStream(nodeStorageFile,false);
 			try {
 				this.properties.store(output, "Properties used by iDetect Node " + this.node.getId());
 				this.display();
 				success = true;
 			} catch (IOException e) {
 				e.printStackTrace();
 				this.logger.error("Unable to store file data, something is horribly wrong");
 			}
 		} catch (IOException e) {
 			// unable to create file
 			e.printStackTrace();
 			this.logger.error("Unable to create file, cannot persist data");
 		}
 		
 		return success;
 	}
 
 	private void display() {
 		String line = "Displaying properties from " + this.file.getAbsoluteFile() + ":";
 		for (String key : this.properties.stringPropertyNames()) {
 			String value = this.properties.getProperty(key);
 			line += ", " + key + " => " + value;
 		}
 		logger.debug(line);
 	}
 
 	public void setProperties(Node node) {
 		this.properties.setProperty("NodeID", Integer.toString(node.getId()));
 		this.properties.setProperty("GroupID", Integer.toString(node.getGroupId()));
 		this.properties.setProperty("LeaderID", Integer.toString(node.getLeaderId()));
 		this.properties.setProperty("GroupCookie", node.getGroupCookie().toString());
 	}
 	
 	public Properties getProperties() {
 		return properties;
 	}
 	
 	private void updateInternalNode() {
 		if (this.properties.getProperty("GroupID") != null) {
 			this.node.setGroupId(Integer.parseInt(this.properties.getProperty("GroupID")));
 		}
 		if (this.properties.getProperty("LeaderID") != null) {
 			this.node.setLeaderId(Integer.parseInt(this.properties.getProperty("LeaderID")));
 		}
 		if (this.properties.getProperty("GroupCookie") != null) {
 			this.node.setGroupCookie(new Cookie(Long.parseLong(this.properties.getProperty("GroupCookie"))));
 		}
 	}
 
 	public Node getNode() {
 		return this.node;
 	}
 }
