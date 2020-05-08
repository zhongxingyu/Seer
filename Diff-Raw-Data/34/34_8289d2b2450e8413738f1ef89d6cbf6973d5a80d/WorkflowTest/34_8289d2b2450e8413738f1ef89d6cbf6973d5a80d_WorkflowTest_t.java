 package safran.test;
 
import org.custommonkey.xmlunit.Diff;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
import org.xml.sax.SAXException;
 import safran.Client;
 import server.global.Constants;
 import utils.ParamParser;
 
 import java.io.FileNotFoundException;
import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Test the server extension WorkflowApi with some simple test cases.
  * Note: you must enable the workflow server thread by adding 
  * {@code <startWorkflowServer>true</startWorkflowServer>} to the
  * cinnamon_config.xml. Otherwise, at least 2 tests will fail.  
  *
  */
 public class WorkflowTest extends BaseTest{	
 
 	public final String workflowDataFolder = "testdata/workflow/";
 	
 	Long workflowFolderId;
 	Long taskDefinitionFolderId;
 	Long templateId;
 	Long workflowId;
 	Long startTaskDefId;
 	Long startTaskId;
 	Long testFolderId;
 	Long documentId;
 	Long automaticWorkflowTemplateId;
 	
 	public WorkflowTest(){
 		
 	}
 	
 	public WorkflowTest(Properties config, Client client){
 		this.config = config;
 		this.client = client;
 	}
 	
 	@BeforeClass
 	public void setUp(){
		super.setUp(false);
 		workflowSetup();
 	}
 	
 	protected void workflowSetup(){
 //		client.initializeComponent("initializeworkflows"); // with auto-initialize, this is no longer needed.
 		String folderPathResponse = client.getFolderByPath("root/system/workflows/templates");
 		log.debug("folderPath:"+folderPathResponse);
 		workflowFolderId = Long.parseLong(client.getFieldValue(folderPathResponse, "//folder[name='templates']/id"));
 //		log.debug("workflowTemplateFolder: "+workflowFolderId);
 		folderPathResponse = client.getFolderByPath("root/system/workflows/task_definitions");
 		taskDefinitionFolderId = Long.parseLong(client.getFieldValue(folderPathResponse, "//folder[name='task_definitions']/id"));
 //		log.debug("taskDefinitionFolderId: "+taskDefinitionFolderId);
 		
 		// create start-Taskdef and other taskdefs
 		startTaskDefId = createTaskDef("cinnamon.test.StartTask", "start_taskdef.xml");
 		createTaskDef("cinnamon.test.ReviewTask", "review_taskdef.xml");
 		createTaskDef("cinnamon.test.EditTask", "edit_taskdef.xml");
 		Long deadlineTaskDefId = createTaskDef("cinnamon.test.DeadlineTask", "deadline_taskdef.xml");
 		
 		// create workflow template
 		String meta = "<meta><metaset type='workflow_template'><active_workflow>true</active_workflow></metaset></meta>";
 		templateId = client.create(meta, 
 				"ReviewDemoWorkflow", workflowFolderId, 
 				Constants.OBJTYPE_WORKFLOW_TEMPLATE);		
 		client.createRelation(Constants.RELATION_TYPE_WORKFLOW_TO_START_TASK, templateId, startTaskDefId);
 		client.createRelation(Constants.RELATION_TYPE_WORKFLOW_TO_DEADLINE_TASK, templateId, deadlineTaskDefId);
 		// Folder for test docs:
		testFolderId = Client.parseLongNode(client.getFolderByPath("documents", true), "/folders/folder/id");
 		
 		// setup for automatic transition test:
 		Long automaticTransitionTask = createTaskDef("cinnamon.test.AutomaticStartTask", 
 				"automatic_taskdef.xml");
 		automaticWorkflowTemplateId = client.create(meta, "AutomaticWorkflow",workflowFolderId, 
 				Constants.OBJTYPE_WORKFLOW_TEMPLATE);
 		client.createRelation(Constants.RELATION_TYPE_WORKFLOW_TO_START_TASK, automaticWorkflowTemplateId, automaticTransitionTask);
 		
 		/*
 		 * start-task to review-task
 		 * start-task to cancel
 		 * review-task to end
 		 * review-task to edit
 		 * review-task to cancel
 		 * edit-task to review-task
 		 * edit-task to cancel
 		 *  
 		 */
 	}
 	
 	//	 -------------- User related tests ---------------------
 	@Test
 	public void getWorkflowTemplateTest(){
 		String result = client.getWorkflowTemplateList();
         log.debug("try to find template-id "+templateId+" in result: "+result);
 		try {
 			Document doc = DocumentHelper.parseText(result);
             String xpath = String.format("//objects/object[id='%s']/id",templateId.toString());
             log.debug("xpath: "+xpath);
 			String id = doc.selectSingleNode(xpath).getText();
 			assert id.equals(templateId.toString()) : "did not find templateId in workflowTemplateList.";
 		} catch (Exception e) {
             log.debug(result);
             log.debug("",e);
             assert false : e.getMessage();
  		}
 		System.out.println(result);
 	}
 	
 	@Test(dependsOnMethods ={"getWorkflowTemplateTest"})
 	public void createWorkflowTest() {
 		try {
 			workflowId = createWorkflow(templateId);
 			assert workflowId > 0 : "No valid workflow id found.";
 		} catch (Exception e) {
 			assert false : "Problem with server response:"+e.getMessage();
 		}
 		String xml = client.getWorkflowList(Constants.PROCSTATE_WORKFLOW_STARTED);
 		assert xml.contains(workflowId.toString());
 	}
 	
 	@Test(dependsOnMethods = {"createWorkflowTest"})
 	/**
 	 * Find the start task of the newly created workflow.
 	 */
 	public void findStartTaskTest(){
 		String task = client.findOpenTasks(null,null);
 		log.debug("result: "+task);
 		startTaskId = Client.parseLongNode(task, "/objects/object/id");
 		assert ! startTaskId.equals(startTaskDefId) : "New task has id of taskdefiniton.";
        String startTaskMeta = client.getMetaset(startTaskId, "transition", "OSD").replaceAll("id=\"\\d+\"", "");
        String startTaskDefMeta = client.getMetaset(startTaskDefId, "transition", "OSD").replaceAll("id=\"\\d+\"", "");
        diffXml(startTaskMeta, startTaskDefMeta, "findStartTaskTest failed");
 		
 		// currently we need only the task, explicit tests are needed:
 		// TODO: test findOpenTasks - for all workflows && given owner
 			// needs: 2 workflows with 2 owners
 		// TODO: test findOpenTasks - for all workflows
 			// needs: 2 workflows
 		// TODO: test findOpenTasks - for a given owner
 			// needs: 2 workflows with 1 owner
 		// TODO: test findOpenTasks - for a given workflow
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test(dependsOnMethods = {"findStartTaskTest"})
 	public void doTransitionTest(){
 		/*
 		 * load transition data from startTask.content
 		 * extract required params and set them.
 		 */
 		String meta = client.getMeta(startTaskId);
 		Document doc =  ParamParser.parseXmlToDocument(meta, null);
 		List<Node> requiredParams = doc.selectNodes("//required/param");
 		for(Node param : requiredParams){
 			String valueAssistance = param.selectSingleNode("valueAssistance").getText();
 			log.debug("va: "+valueAssistance);
 			Node value = param.selectSingleNode("value");
 			value.setText(interpretValueAssistance(valueAssistance));
 		}
 		
 		log.debug("setting new meta:");
 		client.lockAndSetMeta(startTaskId, doc.asXML());
 		
 		String transitions = client.getMeta(startTaskId);
 		String transitionName = "start_task_to_review_task"; 
 		// a normal client would offer the available transitions to the user. 
 		assert transitions.contains(transitionName) : "Did not find the expected transition:\n"+transitions;
 		
 		// TODO: transition without valid params.
 		String xml = client.doTransition(startTaskId, transitionName);
 		assert xml.contains("transition.successful") : "Failed to execute transition.\n"+xml;
 		
 		// there should now exist a review task
 		Long userId = Client.parseLongNode(client.getUserByName("admin"), "/users/user/id");		
 		xml = client.findOpenTasks(workflowId, userId);
 		assert xml.contains("<name>cinnamon.test.ReviewTask</name>") : "Found no open review task.\n"+xml;
 	
 		/*
 		 * After the Transition from start to review, we have a review task waiting.
 		 * This task needs a document (already set by transition) and an editor param.
 		 */
 		Long reviewTaskId = Client.parseLongNode(xml, "/objects/object/id");
 //		String reviewTaskMeta = loadTextFile("review_taskdef.xml");
 		String reviewTaskMeta = client.getMeta(reviewTaskId);
 		doc = ParamParser.parseXmlToDocument(reviewTaskMeta, null);
 		setNodeValue(doc, "//param[name='editor']/value", userId.toString());
 		
 		client.lockAndSetMeta(reviewTaskId, doc.asXML());
 		xml = client.doTransition(reviewTaskId, "review_to_edit");
 		assert xml.contains("transition.successful") : "Failed to execute transition.\n"+xml;
 		
 		/*
 		 * The review_to_edit transition should result in a edit-task.
 		 * The edit task needs the userId of a reviewer.
 		 */
 		xml = client.findOpenTasks(workflowId, userId);
 		assert xml.contains("cinnamon.test.EditTask") : "Did not find edit task name:\n"+xml;
 		Long editTaskId = Client.parseLongNode(xml, "/objects/object/id");
 		String editTaskMeta = client.getMeta(editTaskId);
 		doc = ParamParser.parseXmlToDocument(editTaskMeta, null);
 		setNodeValue(doc, "//param[name='reviewer']/value", userId.toString());
 		client.lockAndSetMeta(editTaskId, doc.asXML());
 		xml = client.doTransition(editTaskId, "edit_to_review");
 		assert xml.contains("transition.successful") : "Failed to execute transition.\n"+xml;
 		
 		/*
 		 * Now let's transition from the review task to the end task:
 		 */
 //		doc = ParamParser.parseXmlToDocument(xml, null);
 		xml = client.findOpenTasks(workflowId, userId);
 		String findOpenTask = client.findOpenTasks(null, userId);
 		assert xml.equals(findOpenTask);
 		assert xml.contains("cinnamon.test.ReviewTask") : "Did not find review task name:\n"+xml;
 		Long secondReviewTaskId = Client.parseLongNode(xml, "/objects/object/id"); 
 		xml = client.doTransition(secondReviewTaskId, "review_to_end");
 		assert xml.contains("transition.successful") : "Failed to execute transition.\n"+xml;
 		
 		/*
 		 * After the workflow is finished, there should be no open tasks left:
 		 */
 		xml = client.findOpenTasks(workflowId, userId);
 		assert ! xml.contains("<id>") : "Response to findOpenTasks contains id.\n"+xml;
 	
 		// check that the workflow is finished.
 		xml = client.getWorkflowList(Constants.PROCSTATE_WORKFLOW_FINISHED);
 		assert xml.contains(workflowId.toString());
 		
 		assert client.getSysMeta(documentId, null, "procstate").contains(Constants.PROCSTATE_REVIEW_OK) :
 			"reviewd document is not marked as REVIEWD_OK";
 	}
 	
 	@Test(dependsOnMethods = {"doTransitionTest"})
 	public void deadlineTaskTest(){
 		// start a new review workflow
 		log.debug("open tasks: "+client.findOpenTasks(null, null));
 		Long workflow = createWorkflow(templateId);
 		String task = client.findOpenTasks(workflow,null);
 		startTaskId = Client.parseLongNode(task, "/objects/object/id");
 		
 		// set startTask.meta to a deadline.
 		String meta = client.getMeta(startTaskId);
 		Document metaDoc = ParamParser.parseXmlToDocument(meta, null);
 		((Element) metaDoc.selectSingleNode("/meta/metaset[@type='task_definition']"))
                 .addElement("deadline").addText("2000-01-01T00:00:01");
 		client.lockAndSetMeta(startTaskId, metaDoc.asXML());
 		
 		/*
 		 * startTask now has a deadline which is overdue.
 		 * We wait for some seconds for the WorkflowServer to visit this object 
 		 */
 		waitForSeconds(12);
 		
 		/*
 		 * Expected results:
 		 * 1. deadline_query succeeds.
 		 * 2. no open tasks.
 		 * 3. workflow finished.
 		 */
 		
 		String tasks = client.findOpenTasks(workflow,null);
 		assert tasks.contains("<objects/>") : "Found open task were none should exist.\n"+tasks; 
 		String finished = client.searchObjects(loadTextFile("deadline_query.xml"));
 		assert finished.contains("<object>") : "search for finished tasks did not return a result.\n"+finished;
 		
 		String wfObject = client.getObject(workflow);  
 		assert wfObject.contains("<procstate>finished</procstate>"):
 			"workflow is not done.\n"+wfObject;
 	}
 
 	/**
 	 * Test if the WorkflowServer generates an automatic deadline task when a workflow reaches its
 	 * deadline.
 	 */
 	@Test(dependsOnMethods={"deadlineTaskTest", "doTransitionTest"})
 	public void deadlineWorkflowTest(){
 		// start a new review workflow
 		Long workflow = createWorkflow(templateId);
 //		String task = client.findOpenTasks(workflow,null);
 //		startTaskId = Client.parseLongNode(task, "/objects/object/id");
 		
 		// set startTask.meta to a deadline.
 		String meta = client.getMeta(workflow);
 		Document metaDoc = ParamParser.parseXmlToDocument(meta, null);
 		metaDoc.getRootElement().addElement("deadline").addText("2000-01-01T00:00:01");
 		client.lockAndSetMeta(workflow, metaDoc.asXML());
 		
 		/*
 		 * startTask now has a deadline which is overdue.
 		 * We wait for 6 seconds for the WorkflowServer to visit this object 
 		 */
 		waitForSeconds(6);
 		
 		/*
 		 * Expected results:
 		 * 1. deadline_query succeeds.
 		 * 2. there should be one open task - the unfinished start task.
 		 * 3. workflow not finished.
 		 */
 		
 		String tasks = client.findOpenTasks(workflow, null);
 		assert tasks.contains("<name>cinnamon.test.StartTask</name>") : "Did not found one open task as expected.\n"+tasks; 
 		String finished = client.searchObjects(loadTextFile("deadline_query.xml"));
 		assert finished.contains("<object>") : "search for deadline task did not return a result.\n"+finished;
 			
 		String wfObject = client.getObject(workflow);  
 		assert wfObject.contains("<procstate>"+Constants.PROCSTATE_WORKFLOW_STARTED+"</procstate>"):
 			"workflow status is not 'running'.\n"+wfObject;
 	}
 	
 	@Test
 	public void cancelWorkflowTest(){
 		// start a new review workflow
 		Long workflow = createWorkflow(templateId);
 		String task = client.findOpenTasks(workflow,null);
 		startTaskId = Client.parseLongNode(task, "/objects/object/id");
 		// call upon the cancel.review transition.
 		String xml = client.doTransition(startTaskId, "cancel.review");
 		assert xml.contains("transition.successful") : "Failed to execute transition.\n"+xml;
 		
 		/*
 		 * Expected:
 		 * 1. no open task left.
 		 * 2. workflow-status = done. 
 		 */
 		task = client.findOpenTasks(workflow,null);
 		assert ! task.contains("<id>") : 
 			"Found taskId but there should be no open tasks.\n"+task;
 		String wfObject = client.getObject(workflow);  
 		assert wfObject.contains("<procstate>finished</procstate>"):
 			"workflow is not done.\n"+wfObject;
 	}
 	
 	@Test
 	public void automaticTransitionTest(){
 		/*
 		 * Testplan:
 		 * - create WorkflowTemplate with dummy startTask, which has an automatic transition
 		 * - start workflow.
 		 * - set task to transition_ready (a start task is 
 		 * - create StartTaskDefinition which transitions to 
 		 */
 		
 		Long workflowId = createWorkflow(automaticWorkflowTemplateId);
 		
 		waitForSeconds(6);
 		
 		String tasks = client.findOpenTasks(workflowId,null);
 		assert tasks.contains("<objects/>") : "Found open task were none should exist.\n"+tasks; 
 		String finished = client.searchObjects(loadTextFile("automatic_query.xml"));
 		assert finished.contains("<object>") : "search for finished tasks did not return a result.\n"+finished;
 	}
 	
 	// ------------ End of Testing -------------------
 	
 	@AfterClass
 	public void disconnectTest() {
 		boolean result = client.disconnect();
 		assert result : "Disconnect form Server failed!";
 	}
 	
 	void setNodeValue(Document doc, String xpath, String value){
 		Node node = doc.selectSingleNode(xpath);
 		node.setText(value);
 	}
 	
 	/**
 	 * Load the content of the given file which has to be located in the folder testData/workflow.
 	 * @param filename
 	 * @return the content of the given file.
 	 */
 	String loadTextFile(String filename){
 		String text;
 		try{
 			text = utils.ContentReader.readFileAsString(workflowDataFolder + filename);
 		}
         catch(FileNotFoundException fnf){
             log.error("File not found: "+workflowDataFolder+filename);
             throw new RuntimeException(fnf);
         }
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return text;
 	}
 	
 	String interpretValueAssistance(String va){
 		if(va.equals("set_to_owner")){
 			return Client.parseLongNode(client.getUserByName(client.getUsername()), "//id").toString();
 		}
 		else if(va.equals("set_to_document")){
 			documentId = client.create("<meta/>", "test document for review workflow", testFolderId );
 			return documentId.toString();
 		}
 		else{ 
 			return "";
 		}
 	}
 	
 	Long createTaskDef(String taskname, String taskMetaFile){
 		String meta = loadTextFile(taskMetaFile);
 		return client.create(meta, taskname, taskDefinitionFolderId,
 				Constants.OBJTYPE_TASK_DEFINITION);
 	}
 	
 	public void waitForSeconds(Integer seconds){
 		try{
 			Thread.sleep(seconds * 1000L);
 			// The WorkflowServer-Thread sleeps for 5s.
 		}
 		catch (Exception e) {
 			// ignore.
 		}
 	}
 	
 	Long createWorkflow(Long templateId){
 		String result = client.createWorkflow(templateId);
 		Long localWorkflowId = 0L;
 		try {
 			Document doc = DocumentHelper.parseText(result);
 			Node workflow= doc.selectSingleNode("//workflowId");
 			localWorkflowId = Long.parseLong( workflow.getText() );
 			assert localWorkflowId > 0 : "No valid workflow id found.";
 		} catch (Exception e) {
 			assert false : "Problem with server response:"+e.getMessage();
 		}
 		return localWorkflowId;
 	}
 	
 	Long getTemplateId(){
 		return templateId;
 	}
    
    void diffXml(String a, String b, String msg){
        Diff myDiff = null;
        try {
            myDiff = new Diff(a,b);
        } catch (SAXException e) {
            e.printStackTrace();
            assert false : msg+"\n"+a;
        } catch (IOException e) {
            e.printStackTrace();
            assert false : msg;
        }
        assert myDiff.similar() : "XML-Diff failed: \n"+msg+"\n"+a+"\n"+myDiff.toString();
    }
 }
