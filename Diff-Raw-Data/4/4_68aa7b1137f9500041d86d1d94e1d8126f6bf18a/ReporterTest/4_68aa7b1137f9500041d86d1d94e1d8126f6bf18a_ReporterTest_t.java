 package net.mindengine.oculus.experior.tests;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang3.ClassUtils;
 import org.junit.Test;
 
 import net.mindengine.oculus.experior.ExperiorConfig;
 import net.mindengine.oculus.experior.framework.report.DefaultReport;
 import net.mindengine.oculus.experior.reporter.Report;
 import net.mindengine.oculus.experior.reporter.ReportBranchConfiguration;
 import net.mindengine.oculus.experior.reporter.ReportConfiguration;
 import net.mindengine.oculus.experior.reporter.ReportReason;
 import net.mindengine.oculus.experior.reporter.nodes.BranchReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.ExceptionInfo;
 import net.mindengine.oculus.experior.reporter.nodes.ExceptionReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.ReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.TextReportNode;
 import net.mindengine.oculus.experior.reporter.render.XmlReportRender;
 
 public class ReporterTest {
 
     
     @Test
     public void readsPropertiesSuccessfuly() {
         ExperiorConfig config = ExperiorConfig.getInstance();
         ReportConfiguration rc = config.getReportConfiguration();
         
         Map<String, ReportBranchConfiguration> branches = rc.getBranches();
         Assert.assertNotNull(branches);
         
         Assert.assertTrue(branches.containsKey("test"));
         Assert.assertTrue(branches.containsKey("action"));
         Assert.assertTrue(branches.containsKey("component"));
         Assert.assertTrue(branches.containsKey("flow"));
         Assert.assertTrue(branches.containsKey("page"));
         Assert.assertTrue(branches.containsKey("apicall"));
         Assert.assertTrue(branches.containsKey("section"));
         Assert.assertTrue(branches.containsKey("validation"));
         
         ReportBranchConfiguration branch = branches.get("component");
         
         Assert.assertNotNull(branch.getParentBranches());
         Assert.assertEquals(4, branch.getParentBranches().size());
         Assert.assertTrue(branch.getParentBranches().contains("test"));
         Assert.assertTrue(branch.getParentBranches().contains("action"));
         Assert.assertTrue(branch.getParentBranches().contains("flow"));
         Assert.assertTrue(branch.getParentBranches().contains("component"));
     }
     
     @Test
     public void writesRendersAndDecodesReportXml() throws Exception {
         Report report = new DefaultReport(ExperiorConfig.getInstance().getReportConfiguration());
         report.info("test info").details("test details").hint("test hint");
         report.error(new NullPointerException("Some test exception")).icon("exception");
         report.warn("test warn");
         report.branch("action", "component branch");
         BranchReportNode branch = report.branch("component").title("component branch");
         report.error("test error in component branch");
         branch.close();
         report.info("test info in upper component branch");
         
         report.branch("action").hint("hint in branch").title("action branch").debug(true);
         String branchId = report.branch("page").title("page branch").getId();
         report.closeBranchById(branchId);
         report.info("info in main branch");
         
         XmlReportRender render = new XmlReportRender();
         String xmlString = render.render(report.getMainBranch());
         
         ReportNode node = render.decode(xmlString);
         validateDecodedReport(node, report.getMainBranch());
     }
     
     @Test
     public void rendersAndDecodesReportReasonsXml() throws Exception {
         XmlReportRender render = new XmlReportRender();
         List<ReportReason> reasons = new LinkedList<ReportReason>();
         reasons.add(new ReportReason().setLevel("info").setReason("some test reason"));
         reasons.add(new ReportReason().setLevel("error").setReason("some test error"));
         String xml = render.renderReasons(reasons);
         
         List<ReportReason> decodedReasons = render.decodeReasons(xml);
         
         Assert.assertEquals("Amount of decoded reasons is not the same", reasons.size(), decodedReasons.size());
         Iterator<ReportReason> it1 = reasons.iterator();
         Iterator<ReportReason> it2 = decodedReasons.iterator();
         while(it1.hasNext()) {
             assertReasonIsSame(it1.next(), it2.next());
         }
     }
     
     @Test
     public void messagesAreLoadedAndProcessed() throws Exception {
     	Report report = new DefaultReport(ExperiorConfig.getInstance().getReportConfiguration());
    	String message = report.message("CollectionVerificator.hasAny.pass").put("name","some test name").toString();
    	Assert.assertEquals("some test name contains at least on item from expected list", message);
     }
 
     private void assertReasonIsSame(ReportReason reason, ReportReason reason2) {
         Assert.assertEquals("Level is not same", reason.getLevel(), reason2.getLevel());
         Assert.assertEquals("Reason is not same", reason.getReason(), reason2.getReason());
     }
 
     private void validateDecodedReport(ReportNode node, ReportNode expectedNode) throws Exception {
          if ( node == expectedNode ) {
              throw new Exception("Objects are same");
          }
          if ( node == null ) {
              throw new NullPointerException("Provided node is null");
          }
          if ( expectedNode == null ) {
              throw new NullPointerException("Provided expected node is null");
          }
          if ( !node.getClass().equals(expectedNode.getClass())) {
              throw new Exception("Objects classes are different: " + node.getClass().getName() + ", " + expectedNode.getClass().getName());
          }
          
          Assert.assertEquals("Title is not the same", expectedNode.getTitle(), node.getTitle());
          Assert.assertEquals("Icon is not the same", expectedNode.getIcon(), node.getIcon());
          Assert.assertEquals("Date is not the same", expectedNode.getDate(), node.getDate());
          Assert.assertEquals("Level is not the same", expectedNode.getLevel(), node.getLevel());
          Assert.assertEquals("Debug is not the same", expectedNode.getDebug(), node.getDebug());
          Assert.assertEquals("Hint is not the same", expectedNode.getHint(), node.getHint());
          
          if ( node instanceof TextReportNode ) {
              Assert.assertEquals("Title is not the same", ((TextReportNode) expectedNode).getDetails(), ((TextReportNode) node).getDetails());
          }
          else if ( node instanceof ExceptionReportNode ) {
              assertExceptionInfoIsSame(((ExceptionReportNode) node).getException(), ((ExceptionReportNode) expectedNode).getException());
          }
          else if ( node instanceof BranchReportNode ) {
              BranchReportNode b1 = (BranchReportNode) node;
              BranchReportNode b2 = (BranchReportNode) expectedNode;
              
              if ( b1.getChildNodes() != null ) {
                  Assert.assertNotNull( b2.getChildNodes() );
                  Assert.assertEquals("Amount of child nodes in branch is not same", b1.getChildNodes().size(), b2.getChildNodes().size());
                  
                  Iterator<ReportNode> nodes1 = b1.getChildNodes().iterator();
                  Iterator<ReportNode> nodes2 = b2.getChildNodes().iterator();
                  while ( nodes1.hasNext() ) {
                      validateDecodedReport(nodes1.next(), nodes2.next());
                  }
              }
              else  {
                  Assert.assertNull( b2.getChildNodes() );
              }
          }
     }
 
     private void assertExceptionInfoIsSame(ExceptionInfo exception, ExceptionInfo exception2) {
         Assert.assertNotNull( exception );
         Assert.assertNotNull( exception2 );
         
         Assert.assertEquals("Classname is not the same", exception2.getClassName(), exception.getClassName());
         Assert.assertEquals("Messagename is not the same", exception2.getMessageName(), exception.getMessageName());
         
         assertStackTraceIsSame(exception.getStackTrace(), exception2.getStackTrace());
         
         if ( exception2.getCause() != null ) {
             Assert.assertNotNull( exception );
             assertExceptionInfoIsSame(exception.getCause(), exception2.getCause());
         }
         else {
             Assert.assertNull( exception.getCause() );
         }
     }
 
     private void assertStackTraceIsSame(StackTraceElement[] stackTrace, StackTraceElement[] stackTrace2) {
         if (stackTrace != null ) {
             Assert.assertNotNull(stackTrace2);
             Assert.assertEquals("Amount of elements in stacktrace is not same", stackTrace.length, stackTrace2.length);
             
             for ( int i=0; i<stackTrace.length; i++ ) {
                 Assert.assertEquals("Stacktrace classname is not same", stackTrace2[i].getClassName(), stackTrace[i].getClassName());
                 Assert.assertEquals("Stacktrace filename is not same", stackTrace2[i].getFileName(), stackTrace[i].getFileName());
                 Assert.assertEquals("Stacktrace methodname is not same", stackTrace2[i].getMethodName(), stackTrace[i].getMethodName());
                 Assert.assertEquals("Stacktrace linunumber is not same", stackTrace2[i].getLineNumber(), stackTrace[i].getLineNumber());
             }
             
         }
         else {
             Assert.assertNull(stackTrace2);
         }
     }
 }
