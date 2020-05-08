 package com.justcloud.osgifier.service.impl;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.osgi.framework.FrameworkUtil;
 import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
 
 import com.justcloud.osgifier.SpringContextHolder;
 import com.justcloud.osgifier.annotation.REST;
 import com.justcloud.osgifier.annotation.REST.RESTMethod;
 import com.justcloud.osgifier.annotation.RESTParam;
 import com.justcloud.osgifier.dto.SpringContext;
 import com.justcloud.osgifier.service.SpringService;
 import com.justcloud.osgifier.service.UtilsService;
 
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 public class SpringServiceImpl implements SpringService {
 
 	private JSONSerializer serializer;
 	private JSONDeserializer<SpringContext> deserializer;
 
 	public SpringServiceImpl() {
 		serializer = new JSONSerializer();
 		deserializer = new JSONDeserializer<SpringContext>();
 	}
 
 	@Override
 	@REST(url = "/spring/list", method = RESTMethod.GET)
 	public List<SpringContext> getSpringServiceDetails() {
 		List<SpringContext> result = new ArrayList<SpringContext>();
 		File parent = getSpringPath();
 		for (File context : parent.listFiles()) {
			result.add(readFile(context));
 		}
 		return result;
 	}
 
 	@Override
 	@REST(url = "/spring/find", method = RESTMethod.POST)
 	public SpringContext getFullContext(@RESTParam("name") String name) {
 		return readFile(new File(getSpringPath(), name));
 	}
 
 	@Override
 	@REST(url = "/spring/register", method = RESTMethod.POST)
 	public void registerContext(@RESTParam("context") SpringContext context) {
 		File installFile = new File(getSpringPath(), context.getName() + "-osgifier.xml");
 		File realFile = new File(getSpringPath(), context.getName());
 		writeFile(context, realFile);
 		writeXmlFile(context, installFile);
 		
 		String[] filenames = new String[] {
 			"file://" + installFile.getAbsolutePath()	
 		};
 		
 		try {
 			OsgiBundleXmlApplicationContext ctx = new OsgiBundleXmlApplicationContext(filenames);
 			ctx.setBundleContext(FrameworkUtil.getBundle(
 					SpringServiceImpl.class).getBundleContext());
 			SpringContextHolder.getInstance().registerContext(context.getName(), ctx);
 		} catch(RuntimeException ex) {
 			realFile.delete();
 			throw ex;
		} finally {
			installFile.delete();
 		}
 		
 	}
 
 	@Override
 	@REST(url = "/spring/destroy", method = RESTMethod.POST)
 	public void destroyContext(@RESTParam("name") String name) {
 
 		File f = new File(getSpringPath(), name);
 		f.delete();
 		SpringContextHolder.getInstance().unregisterContext(name);
 
 	}
 
 	private void writeFile(SpringContext context, File f) {
 		BufferedWriter writer = null;
 
 		try {
 			writer = new BufferedWriter(new FileWriter(f));
 			serializer.deepSerialize(context, writer);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 					Logger.getLogger(UserServiceImpl.class.getName()).severe(
 							e.getMessage());
 				}
 			}
 		}
 
 	}
 	
 	private void writeXmlFile(SpringContext context, File f) {
 		BufferedWriter writer = null;
 
 		try {
 			writer = new BufferedWriter(new FileWriter(f));
 			writer.write(context.getContent());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 					Logger.getLogger(UserServiceImpl.class.getName()).severe(
 							e.getMessage());
 				}
 			}
 		}
 
 	}
 
 	private SpringContext readFile(File f) {
 		BufferedReader reader = null;
 		SpringContext result = null;
 
 		try {
 			reader = new BufferedReader(new FileReader(f));
 			result = deserializer.deserialize(reader);
 		} catch (FileNotFoundException e) {
 			Logger.getLogger(UserServiceImpl.class.getName()).severe(
 					e.getMessage());
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					Logger.getLogger(UserServiceImpl.class.getName()).severe(
 							e.getMessage());
 				}
 			}
 		}
 		return result;
 	}
 
 	private File getSpringPath() {
 		File parent = new File(UtilsService.getPath("/spring"));
 		if (!parent.exists()) {
 			parent.mkdirs();
 		}
 		return parent;
 	}
 
 	@Override
 	public void stop() {
 		try {
 			SpringContextHolder.getInstance().stop();
 		} catch (Exception ex) {
 			Logger.getLogger(SpringServiceImpl.class.getName()).log(
 					Level.SEVERE, "Cannot stop environment", ex);
 		}
 	}
 
 	@Override
 	public void start() {
 		for (SpringContext context : getSpringServiceDetails()) {
 			try {
 				registerContext(context);
 			} catch (Exception ex) {
 				Logger.getLogger(SpringServiceImpl.class.getName()).log(
 						Level.SEVERE, "Cannot load context " + context, ex);
 			}
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "Spring";
 	}
 
 }
