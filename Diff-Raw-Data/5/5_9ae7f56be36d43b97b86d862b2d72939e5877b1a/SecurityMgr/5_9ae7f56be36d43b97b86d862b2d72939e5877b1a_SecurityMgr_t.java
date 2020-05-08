 package com.cffreedom.utils.security;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cffreedom.exceptions.FileSystemException;
 import com.cffreedom.exceptions.InfrastructureException;
 import com.cffreedom.utils.Convert;
 import com.cffreedom.utils.SystemUtils;
 import com.cffreedom.utils.Utils;
 import com.cffreedom.utils.file.FileUtils;
 
 /**
  * Automated layer for accessing/security usernames and passwords that should 
  * guarantee that the user is not prompted for any information.
  * 
  * Original Class: com.cffreedom.utils.security.SecurityMgr
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-07-19 	markjacobsen.net 	Created
 * 2013-10-24 	MarkJacobsen.net 	Added getKeys() and fixed bug getting username
  */
 public class SecurityMgr
 {
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.security.SecurityMgr");
 	
 	public static final String PROP_FILE = "security.properties";
 	public static final String DEFAULT_FILE = SystemUtils.getDirConfig() + SystemUtils.getPathSeparator() + PROP_FILE;
 	public static final boolean CREATE_FILE = false;
 	
 	private Hashtable<String, Entry> entries = new Hashtable<String, Entry>();
 	private String file = null;
 	private SecurityCipher cipher = null;
 	
 	public SecurityMgr(String masterKey) throws FileSystemException, InfrastructureException
 	{
 		this(masterKey, SecurityMgr.DEFAULT_FILE);
 	}
 	
 	public SecurityMgr(String masterKey, String file) throws FileSystemException, InfrastructureException
 	{
 		this(masterKey, file, SecurityMgr.CREATE_FILE);
 	}
 	
 	public SecurityMgr(String masterKey, String file, boolean createPropFileIfNew) throws FileSystemException, InfrastructureException
 	{		
 		this.loadFile(masterKey, file, createPropFileIfNew);
 	}
 		
 	public void loadFile(String masterKey, String file) throws FileSystemException, InfrastructureException { this.loadFile(masterKey, file, SecurityMgr.CREATE_FILE); }
 	@SuppressWarnings("resource")
 	public void loadFile(String masterKey, String file, boolean createPropFileIfNew) throws FileSystemException, InfrastructureException
 	{
 		InputStream inputStream = null;
 		Properties props = new Properties();
 		
 		try
 		{
 			this.cipher = new SecurityCipher(masterKey);
 			this.file = file;
 		
 			if ((this.file != null) && (FileUtils.fileExists(this.file) == false) && (createPropFileIfNew == true))
 			{
 				logger.debug("Attempting to create file: {}", this.file);
 				this.save();
 			}
 			
 			if (FileUtils.fileExists(this.file) == true)
 			{
 				logger.info("Loading from passed in file: {}", this.file);
 				inputStream = new FileInputStream(this.file);
 			}
 			else
 			{
 				logger.info("Attempting to find file on classpath: {}", SecurityMgr.PROP_FILE);
 				inputStream = this.getClass().getClassLoader().getResourceAsStream(SecurityMgr.PROP_FILE);
 			}
 			
 			if (inputStream == null)
 			{
 				throw new InfrastructureException("Invalid password file or no default file \""+SecurityMgr.PROP_FILE+"\" found on the classpath");
 			}
 			else
 			{
 				logger.debug("Loading password file");
 				
 				props.load(inputStream);
 				inputStream.close();
 				
 				if (props.getProperty("keys") == null)
 				{
 					logger.warn("No \"keys\" property exists so nothing will be read");
 				}
 				else
 				{
 					String[] keys = props.getProperty("keys").split(",");
 					
 					for (String key : keys)
 					{
 						logger.trace(key);
						String user = props.getProperty(key + ".username");
 						String password = props.getProperty(key + ".password");
 						String note = props.getProperty(key + ".note");
 						
 						if (Utils.hasLength(user) == true) { user = this.cipher.decrypt(user); }
 						if (Utils.hasLength(password) == true) { password = this.cipher.decrypt(password); }
 						
 						Entry entry = new Entry(key, user, password, note);
 		
 						this.entries.put(key, entry);
 					}
 				}
 			}
 			
 			logger.debug("Loaded {} entries", this.entries.size());
 		}
 		catch (FileNotFoundException e)
 		{
 			throw new FileSystemException("FileNotFound", e);
 		}
 		catch (IOException e)
 		{
 			throw new FileSystemException("IOException", e);
 		}
 	}
 	
 	private boolean save()
 	{
 		if (this.file == null)
 		{
 			logger.warn("No file to save to");
 			return false;
 		}
 		else
 		{
 			ArrayList<String> lines = new ArrayList<String>();
 			logger.debug("Saving to file {}", this.getFile());
 			
 			lines.add("#--------------------------------------------------------------------------------------");
 			lines.add("# Usernames and Passwords do need to be encrypted using the SecurityCipher class.");
 			lines.add("# It is suggested that you use the PasswordManager app in cffreedom-cl-apps to maintain");
 			lines.add("# this file.");
 			lines.add("#--------------------------------------------------------------------------------------");
 			lines.add("");
 			
 			if (this.entries.size() <= 0)
 			{
 				logger.warn("No Entry objects cached so no actual values will be written");
 				lines.add("# No entries to save");
 			}
 			else
 			{
 				lines.add("keys=" + Convert.toDelimitedString(this.entries.keySet(), ","));
 				lines.add("");
 				
 				for (String key : this.entries.keySet())
 				{
 					logger.trace(key);
 					Entry entry = this.getEntry(key);
 					lines.add(key + ".username=" + this.getPropFileValue(entry.username, true));
 					lines.add(key + ".password=" + this.getPropFileValue(entry.password, true));
 					lines.add(key + ".note=" + this.getPropFileValue(entry.note, false));
 					lines.add("");
 				}
 			}
 			
 			return FileUtils.writeLinesToFile(this.getFile(), lines);
 		}
 	}
 	
 	private String getPropFileValue(String val, boolean encrypt)
 	{
 		if (val == null){
 			return "";
 		}else{
 			if (encrypt == true){
 				return this.cipher.encrypt(val);
 			}else{
 				return val;
 			}
 		}
 	}
 	
 	public String getFile() { return this.file; }
 	
 	public boolean keyExists(String key)
 	{
 		return this.entries.containsKey(key);
 	}
 	
 	public Entry getEntry(String key)
 	{
 		return this.entries.get(key);
 	}
 	
 	public Set<String> getKeys()
 	{
 		return this.entries.keySet();
 	}
 	
 	public String getUsername(String key)
 	{
 		Entry entry = this.getEntry(key);
 		
 		if (entry == null)
 		{
 			logger.warn("An Entry does not exist for key: {}", key);
 			return null;
 		}
 		else
 		{
 			return entry.username;
 		}
 	}
 	
 	public String getPassword(String key)
 	{
 		Entry entry = this.getEntry(key);
 		
 		if (entry == null)
 		{
 			logger.warn("An Entry does not exist for key: {}", key);
 			return null;
 		}
 		else
 		{
 			return entry.password;
 		}
 	}
 	
 	public String getNote(String key)
 	{
 		Entry entry = this.getEntry(key);
 		
 		if (entry == null)
 		{
 			logger.warn("An Entry does not exist for key: {}", key);
 			return null;
 		}
 		else
 		{
 			return entry.note;
 		}
 	}
 	
 	public boolean addEntry(String key, String username, String password, String note)
 	{
 		if (this.entries.containsKey(key) == false)
 		{
 			this.entries.put(key, new Entry(key, username, password, note));
 			this.save();
 			return true;
 		}
 		else
 		{
 			logger.error("An entry named {} already exists", key);
 			return false;
 		}
 	}
 	
 	public boolean updateEntry(String key, String username, String password, String note)
 	{
 		deleteEntry(key);
 		return addEntry(key, username, password, note);
 	}
 	
 	public boolean deleteEntry(String key)
 	{
 		if (this.entries.containsKey(key) == true)
 		{
 			this.entries.remove(key);
 			this.save();
 			return true;
 		}
 		else
 		{
 			logger.error("An entry named {} does not exist");
 			return false;
 		}
 	}
 	
 	public void printKeys()
 	{
 		Utils.output("Keys");
 		Utils.output("======================");
 		if ((this.entries != null) && (this.entries.size() > 0))
 		{
 			for(String key : this.entries.keySet())
 			{
 				Utils.output(key);
 			}
 		}
 	}
 	
 	public void printKey(String key)
 	{
 		Entry entry = getEntry(key);
 		Utils.output("");
 		Utils.output("Key = " + key);
 		Utils.output("Username = " + entry.username);
 		Utils.output("Note = " + entry.note);
 	}
 	
 	private class Entry
 	{
 		private String key;
 		private String username;
 		private String password;
 		private String note;
 		
 		protected Entry(String key, String username, String password, String note)
 		{
 			this.key = key;
 			this.username = username;
 			this.password = password;
 			this.note = note;
 		}
 	}
 }
