 package xtremweb.gen.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 
 /**
  * This class generates the infrastructure needed to generate the classes to
  * integrate a new service in bitdew through the SDK.
  * 
  * @author jsaray
  */
 public class GenService {
     /**
      * Application logger
      */
     protected static Logger log = LoggerFactory.getLogger(GenService.class);
 
     /**
      * This constructor generates four files for a new service : the callback,
      * the .jdo file, the .idl file and the Dao file
      * 
      * @param args
      *            an array containing only the service name
      * @throws IOException
      *             if a problem arises while writing the files
      */
     public GenService(String[] args) throws IOException {
 	String service = args[1];
 	String objectstr = "";
 	for (int i = 2; i < args.length; i++) {
 	    objectstr += args[i] + " ";
 	}
 	String[] objects = {};
	if (objectstr != null)
 	    objects = objectstr.split(" ");
 	File dir = new File(System.getProperty("user.dir")
 		+ "/src/xtremweb/serv/" + service + "/");
 	if (!dir.exists())
 	    dir.mkdir();
 	else {
 	    log.fatal("You already define that service ");
 	    return;
 	}
 	writeCallback(dir, service);
 	writeJdo(dir, service, objects);
 	writeIdl(dir, service);
 
 	File daodir = new File(System.getProperty("user.dir")
 		+ "/src/xtremweb/dao/");
 	writeDao(daodir, service, objects);
     }
 
     /**
      * Writes the Callback file for a service
      * 
      * @param dir
      *            the directory where the file will be created
      * @param service
      *            the service name
      * @throws IOException
      *             if a problem writing the file happens
      */
     public void writeCallback(File dir, String service) throws IOException {
 	File nucleus = new File(dir.getAbsolutePath() + "/Callback" + service
 		+ ".java");
 	BufferedWriter bw;
 	bw = new BufferedWriter(new FileWriter(nucleus));
 	bw.write("package xtremweb.serv." + service + ";\n");
 	bw.write("import xtremweb.core.com.idl.CallbackTemplate;\n");
 	bw.write("import xtremweb.core.iface.Interface" + service + ";\n");
 	bw.write("\n");
 	bw.write("\n");
 	bw.write("public class Callback" + service
 		+ " extends CallbackTemplate implements Interface" + service
 		+ " {\n");
 	bw.write("}\n");
 	bw.flush();
 	bw.close();
 	log.info("class xtremweb.serv." + service + ".Callback" + service
 		+ " succesfully Generated");
 
     }
 
     /**
      * Write the idl file for a service
      * 
      * @param dir
      *            the directory where the idl will be added
      * @param service
      *            the service name
      * @throws IOException
      */
     public void writeIdl(File dir, String service) throws IOException {
 	File idl = new File(dir.getAbsolutePath() + "/" + service + ".idl");
 	BufferedWriter bw = new BufferedWriter(new FileWriter(idl));
 	bw.write("<Module name=\"" + service + "\">\n");
 	bw.write("</Module>");
 	bw.flush();
 	bw.close();
 	log.info("File " + service
 		+ ".idl succesfully generated in xtremweb.serv." + service);
     }
 
     /**
      * Write the jdo file for a service
      * 
      * @param dir
      *            the directory where the idl will be added
      * @param service
      *            the service name
      * @throws IOException
      */
     public void writeJdo(File dir, String service, String[] objects)
 	    throws IOException {
 	File jdo = new File(dir.getAbsolutePath() + "/package.jdo");
 	BufferedWriter bw = new BufferedWriter(new FileWriter(jdo));
 	bw.write("<?xml version=\"1.0\"?>\n");
 	bw.write("<jdo>\n");
 
 	bw.write("<package name=\"xtremweb.core.obj." + service + "\">\n");
 	for (int i = 0; i < objects.length; i++) {
 	    bw.write("<class name=\"" + objects[i]
 		    + "\" identity-type=\"application\" table=\""
 		    + objects[i].toUpperCase() + "\">\n");
 	    bw.write("    <!-- PLEASE DO NOT ERASE THIS FIELD AS IT IS NECESSARY FOR JPOX (JDO FRAMEWORK ) TO RECOGNIZE EACH OBJECT WITH AN ID-->");
 	    bw.write("<field name=\"uid\" primary-key=\"true\"  value-strategy=\"auid\">\n");
 	    bw.write("<column name=\"UID\"/>\n");
 	    bw.write("</field>");
 	    bw.write("</class>");
 	}
 	bw.write("</package>\n");
 	bw.write("</jdo>\n");
 	bw.flush();
 	bw.close();
 	log.info("File package.jdo succesfully generated in xtremweb.serv."
 		+ service);
     }
 
     /**
      * Write the dao file for a service
      * 
      * @param dir
      *            the directory where the idl will be added
      * @param service
      *            the service name
      * @throws IOException
      */
     public void writeDao(File dir, String service, String[] objects)
 	    throws IOException {
 
 	for (int i = 0; i < objects.length; i++) {
 	    File dao = new File(dir.getAbsolutePath() + "/Dao" + objects[i]
 		    + ".java");
 	    BufferedWriter bw = new BufferedWriter(new FileWriter(dao));
 	    bw.write("package xtremweb.dao." + objects[i].toLowerCase() + ";\n");
 	    bw.write("import xtremweb.core.com.idl.CallbackTemplate;\n");
 	    bw.write("import xtremweb.core.iface.Interface" + objects[i]
 		    + ";\n");
 	    bw.write("\n");
 	    bw.write("\n");
 	    bw.write("public class Dao" + objects[i] + " extends DaoJDOImpl {\n");
 	    bw.write("}");
 	    bw.flush();
 	    bw.close();
 	    log.info("File " + objects[i]+ "Dao successfully generated in xtremweb.dao." + objects[i].toLowerCase()+"."+objects[i]+"Dao.java");
 	}
     }
 
     /**
      * 
      */
     public static void usage() {
 	System.out
 		.println("Usage : xtremweb.gen.service.GenService <servicename>");
     }
 
 }
