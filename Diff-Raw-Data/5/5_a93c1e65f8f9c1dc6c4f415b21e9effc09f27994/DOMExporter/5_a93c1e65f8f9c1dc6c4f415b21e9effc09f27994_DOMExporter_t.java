 package com.runwaysdk.eclipse.plugin.schema.exporter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.maven.cli.MavenCli;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.RunwayDiagramEditorPlugin;
 import com.runwaysdk.eclipse.plugin.schema.SchemaUtil;
 import com.runwaysdk.eclipse.plugin.schema.runwayxml.XMLMdBusiness;
 import com.runwaysdk.eclipse.plugin.schema.runwayxml.XMLMetadata;
 
 public class DOMExporter
 {
   private Document dom;
 
   private Element  version;
 
   private Element  doIt;
 
   private Element  doItCreate;
 
   
   private Element  doItUpdate;
 
   private Element  doItDelete;
 
   private Element  undoIt;
 
   private Element  undoItCreate;
 
   private Element  undoItUpdate;
 
   private Element  undoItDelete;
   
   // This method is called when the user saves the document.
   public static void doExport()
   {
     XMLRecordFactory.validateRecords();
     
     List<XMLMdBusiness> records = XMLRecordFactory.getRecords();
     if (records.size() <= 0) { return; }
     
     DOMExporter exporter = new DOMExporter();
     String fileName = exporter.getExportPath();
     
     System.out.println("Writing file to '" + fileName + "'");
     
     // Generates an empty Runway XML file
     DOMExporter instance = new DOMExporter();
     instance.generateEmptySchema(fileName);
 
     for (int i = 0; i < records.size(); i++)
     {
       XMLMdBusiness record = records.get(i);
       
       Element el = record.writeDoItXML(instance.dom);
       Element ele = record.writeUndoItXML(instance.dom);
       
       if (record.getCrudFlag() == XMLMetadata.CREATE)
       {
         instance.doItCreate.appendChild(el);
         instance.undoItDelete.appendChild(ele);
       }
       else if (record.getCrudFlag() == XMLMetadata.UPDATE)
       {
         instance.doItUpdate.appendChild(el);
         instance.undoItDelete.appendChild(el);
       }
       else if (record.getCrudFlag() == XMLMetadata.DELETE)
       {
       }
       else {
         throw new RuntimeException("Unrecognized Crud Flag on XMLMdBusiness [" + record.getCrudFlag() + "]");
       }
     }
     
     XMLRecordFactory.clearRecordStore();
     
     try
     {
       Transformer tr = TransformerFactory.newInstance().newTransformer();
       tr.setOutputProperty(OutputKeys.INDENT, "yes");
       tr.setOutputProperty(OutputKeys.METHOD, "xml");
       tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
       tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
       tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 
       // send DOM to file
       tr.transform(new DOMSource(instance.dom), new StreamResult(new FileOutputStream(fileName)));
     }
     catch (TransformerException te)
     {
       throw new RuntimeException(te);
     }
     catch (IOException ioe)
     {
       throw new RuntimeException(ioe);
     }
   }
   
   private String getExportPath() {
     final String activeProjectName;
     String activeDiagramFilename;
     IProject activeProject;
     Shell shell;
     IWorkbenchPage page = RunwayDiagramEditorPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getActivePage();
     try {
       IEditorPart editorPart = page.getActiveEditor();
       shell = editorPart.getSite().getShell();
       IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput() ;
       IFile file = input.getFile();
       activeProject = file.getProject();
       
       activeProjectName = activeProject.getName();
       activeDiagramFilename = file.getName();
       activeDiagramFilename = activeDiagramFilename.substring(0, activeDiagramFilename.length() - file.getFileExtension().length() - 1);
     }
     catch (Exception e) {
       throw new RuntimeException("The file you wish to export must be open.", e);
     }
     
     URL platUrl = Platform.getInstanceLocation().getURL();
     final String workspace = new File(platUrl.getPath()).getAbsolutePath();
     
     final String saveDirectory = workspace + "/.metadata/.plugins/com.runwaysdk.eclipse.plugin/" + activeProjectName + "/" + activeDiagramFilename;
     
     // Use Runway to create a new schema file.
 //    ProfileManager.setProfileHome(workspace + "/" + activeProjectName + "/src/main/resources");
 //    String path;
 //    try
 //    {
 //      path = new CreateDomainModel(saveDirectory).create();
 //    }
 //    finally
 //    {
 //      CacheShutdown.shutdown();
 //    }
     
     /*
      * Call Runway's new schema tool using Maven.
      */
    if (new File(workspace + activeProject.getFullPath().toOSString() + "/target/classes/master.properties").exists() == false) {
      RuntimeException e = new RuntimeException("The project must be compiled first.");
      throw e;
    }
    
     File f = new File(saveDirectory);
     if (f.exists() == false) {
       f.mkdirs();
     }
     
     final List<File> beforeFiles = Arrays.asList(new File(saveDirectory).listFiles());
     
     /*
     final String[] mavenArgs = new String[] {
         "exec:java",
 //        "-X",
         "-Dexec.mainClass=com.runwaysdk.dataaccess.io.CreateDomainModel",
         "-Dexec.arguments=" + saveDirectory };
     */
     final String[] mavenArgs = new String[] {
         "exec:exec",
         "-Dexec.executable=java",
         "-Dexec.args=-classpath %classpath com.runwaysdk.dataaccess.io.CreateDomainModel " + saveDirectory };
 
     final PrintStream out = SchemaUtil.openRunwayConsole();
     int retVal = new MavenCli().doMain(mavenArgs, workspace + activeProject.getFullPath().toOSString(), out, out);
 
     if (retVal != 0) {
       throw new RuntimeException("An exception has occurred while creating a new runway schema. (Maven exited with status code " + retVal + ")");
     }
 //    SchemaUtil.runMavenCmd(mavenArgs, workspace + activeProject.getFullPath().toOSString(), "creating a new runway schema.");
     
     /**
      * The operating system unfortunately doesn't report the new file yet. Spawn a thread to check every second.
      */
     final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
     Future<String> future = worker.schedule(new SchemaDetectThread(saveDirectory, beforeFiles, shell), 1, TimeUnit.SECONDS);
     
     try
     {
       String path = future.get();
       worker.shutdown();
       return path;
     }
     catch (InterruptedException e)
     {
       throw new RuntimeException(e);
     }
     catch (ExecutionException e)
     {
       throw new RuntimeException(e);
     }
   }
   
   private class SchemaDetectThread implements Callable<String> {
     private int timeCounter = 0;
     private final String saveDirectory;
     private final List<File> beforeFiles;
     private final Shell shell;
     
     SchemaDetectThread(String saveDirectory, List<File> beforeFiles, Shell shell) {
       this.saveDirectory = saveDirectory;
       this.beforeFiles = beforeFiles;
       this.shell = shell;
     }
     
     public String call() throws Exception {
       if (timeCounter > 8) {
 //        System.out.println("Unable to find the new schema file (created by Runway). Attempting to save your schema file anyway.");
 //        
 //        Random rand = new Random();
 //        int n = rand.nextInt(500000) + 1;
 //        
 //        File file = new File(saveDirectory + "/schema" + Integer.toString(n) + ".xml");
 //        try
 //        {
 //          file.createNewFile();
 //        }
 //        catch (IOException e)
 //        {
 //          throw new RuntimeException(e);
 //        }
 //        
 //        return file.getAbsolutePath();
         
         throw new RuntimeException("Timeout waiting for creation of new schema file.");
       }
       
       LinkedList<File> afterFiles = new LinkedList<File>(Arrays.asList(new File(saveDirectory).listFiles()));
       afterFiles.removeAll(beforeFiles);
       
       if (afterFiles.size() > 0) {
         System.out.println("Schema file created after " + (timeCounter + 1) + " seconds.");
         return afterFiles.iterator().next().getAbsolutePath();
       }
       
       System.out.println("timeCounter = " + timeCounter);
       
       timeCounter++;
       
       final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
       Future<String> future = worker.schedule(this, 1, TimeUnit.SECONDS);
       
       try
       {
         String path = future.get();
         worker.shutdown();
         return path;
       }
       catch (InterruptedException e)
       {
         throw new RuntimeException(e);
       }
       catch (ExecutionException e)
       {
         throw new RuntimeException(e);
       }
     }
   }
   
   public void generateEmptySchema(String filename)
   {
     // instance of a DocumentBuilderFactory
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     try
     {
       // use factory to get an instance of document builder
       DocumentBuilder db = dbf.newDocumentBuilder();
       // create instance of DOM
       dom = db.newDocument();
 
       // create data elements and place them in the structure
       version = dom.createElement("version");
       version.setAttribute("xsi:noNamespaceSchemaLocation", "../../profiles/version_gis.xsd");
       version.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 
       doIt = dom.createElement("doIt");
       version.appendChild(doIt);
 
       doItCreate = dom.createElement("create");
       doIt.appendChild(doItCreate);
 
       doItUpdate = dom.createElement("update");
       doIt.appendChild(doItUpdate);
 
       doItDelete = dom.createElement("delete");
       doIt.appendChild(doItDelete);
 
       undoIt = dom.createElement("undoIt");
       version.appendChild(undoIt);
 
       undoItCreate = dom.createElement("create");
       undoIt.appendChild(undoItCreate);
 
       undoItUpdate = dom.createElement("update");
       undoIt.appendChild(undoItUpdate);
 
       undoItDelete = dom.createElement("delete");
       undoIt.appendChild(undoItDelete);
 
       dom.appendChild(version);
     }
     catch (ParserConfigurationException pce)
     {
       System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
     }
   }
 
 }
