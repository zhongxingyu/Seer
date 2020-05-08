 package hudson.plugins.sctmexecutor;
 
 import hudson.FilePath;
 import hudson.plugins.sctmexecutor.exceptions.SCTMException;
 import hudson.plugins.sctmexecutor.service.ISCTMService;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Text;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import com.borland.sctm.ws.execution.entities.ExecutionResult;
 import com.borland.sctm.ws.execution.entities.TestDefinitionResult;
 import com.borland.sctm.ws.performer.SPNamedEntity;
 
 public class SCTMResultWriter implements ITestResultWriter {
   private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor");
   
   private ISCTMService service;
   private FilePath rootDir;
   private boolean ignoreSetupCleanup;
 
   public SCTMResultWriter(FilePath rootDir, ISCTMService service, boolean ignoreSetupCleanup) {
     this.rootDir = rootDir;
     this.service = service;
     this.ignoreSetupCleanup = ignoreSetupCleanup;
   }
 
   @Override
   public void write(ExecutionResult result) throws SCTMException {
     try {
       FilePath execDefFolder = new FilePath(this.rootDir, result.getExecDefName());
         execDefFolder.mkdirs();
       
       if (!ignoreSetupCleanup) {
        writeTestDefResult(result.getSetupTestDef(), execDefFolder, result.getExecDefName());
        writeTestDefResult(result.getCleanupTestDef(), execDefFolder, result.getExecDefName());
       }
       
       for (TestDefinitionResult testResult : result.getTestDefResult()) {
         writeTestDefResult(testResult, execDefFolder, result.getExecDefName());
       }
     } catch (Exception e) {
       String msg = MessageFormat.format("Cannot create result folder for Execution definition ''{0}'' ({1}). ", result.getExecDefName(), result.getExecDefId());
       LOGGER.log(Level.FINE, msg, e);
       throw new SCTMException(msg);
     }
   }
 
   private void writeTestDefResult(TestDefinitionResult testDefResult, FilePath execDefResultFolder, String execDefName) throws SCTMException {
    if (testDefResult == null)
      return;
     String name = testDefResult.getName();
     int testRunId = testDefResult.getTestRunId();
     
     InputStream result = null;
     try {
       FilePath testDefResFolder = new FilePath(execDefResultFolder, name);
       testDefResFolder.mkdirs();
       SPNamedEntity[] resultFiles = this.service.getResultFiles(testRunId);
       for (SPNamedEntity resultFile : resultFiles) {
         String resultFileName = resultFile.getMsName();
         FilePath file = new FilePath(testDefResFolder, resultFileName);
         int miId = resultFile.getMiId();
         result = this.service.loadResultFile(miId);
         if (resultFileName.matches("output.xml"))
           completeAndWriteResultFile(result, file, execDefName, testDefResult.getName());
         else
           file.copyFrom(result);
       }
     } catch (Exception e) {
       String msg = MessageFormat.format("Cannot create result folder for Test definition ''{0}'' ({1}). ", testDefResult.getName(), testDefResult.getTestDefId());
       LOGGER.log(Level.FINE, msg, e);
       throw new SCTMException(msg);
     } finally {
       if (result != null) {
         try {
           result.close();
         } catch (IOException e) {
           throw new SCTMException(e.getMessage());
         }
       }
     }
   }
 
   private void completeAndWriteResultFile(InputStream result, FilePath file, String execDefName, String testDefName) throws JDOMException, IOException, InterruptedException {
     Document document = new SAXBuilder().build(result);
     Element rootElement = document.getRootElement();
     Element oldRootElement = (Element) rootElement.clone();
     rootElement.removeContent();
     rootElement.setAttribute("TestItem", execDefName);
     rootElement.removeAttribute("ExtId");
     completeElement(oldRootElement, rootElement);
     Element testDefElement = new Element("TestSuite");
     completeElement(oldRootElement, testDefElement);
     testDefElement.setAttribute("TestItem", testDefName);
     testDefElement.addContent(oldRootElement);
     rootElement.addContent(testDefElement);
     
     XMLOutputter xmlWriter = new XMLOutputter(Format.getPrettyFormat());
     OutputStream out = null;
     try {
       out = file.write();
       xmlWriter.output(document, out);
     } finally {
       if (out != null)
         out.close();
     }
   }
 
   private void completeElement(Element oldRootElement, Element testDefElement) {
     addElement(testDefElement, "RunCount", oldRootElement.getChildText("RunCount"));
     addElement(testDefElement, "Timer", oldRootElement.getChildText("Timer"));
     addElement(testDefElement, "WasSuccess", oldRootElement.getChildText("WasSuccess"));
   }
 
   private void addElement(Element testDefElement, String elementName, String value) {
     Element elem = new Element(elementName);
     elem.addContent(new Text(value));
     testDefElement.addContent(elem);
   }
 }
