 /**
  * 
  */
 package org.vivoweb.harvester.util;
 
 import java.io.File;
 import org.apache.commons.vfs.AllFileSelector;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.VFS;
 import org.slf4j.LoggerFactory;
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 /**
  * @author Christopher Haines (hainesc@ctrip.ufl.edu)
  */
 public class InitLog {
 	
 	/**
 	 * Setup the logger
 	 */
 	public static void initLogger() {
 		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
 		System.out.println("trying to get task from ENV");
 		String task = System.getenv("HARVESTER_TASK");
		if(task == null || task.trim().equals("")) {
 			System.out.println("ENV not set, using Property");
 			task = System.getProperty("harvester-task");
 		}
		if(task == null || task.trim().equals("")) {
 			System.out.println("Property not set, using default");
 			task = "harvester";
 		}
 		System.out.println("harvester-task: "+task);
 		context.putProperty("harvester-task", task);
 		JoranConfigurator jc = new JoranConfigurator();
 		jc.setContext(context);
 		context.reset();
 		try {
 			for(FileObject file : VFS.getManager().toFileObject(new File(".")).findFiles(new AllFileSelector())) {
 				if(file.getName().getBaseName().equals("logback.xml")) {
 					System.out.println("configuring: "+file.getName().getPath());
 					jc.doConfigure(file.getContent().getInputStream());
 					break;
 				}
 			}
 		} catch(FileSystemException e) {
 			throw new IllegalArgumentException(e);
 		} catch(JoranException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 }
