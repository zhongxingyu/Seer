 package com.igate.dam.dynamic.workflow.serviceimpl;
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.ProcessInstance;
 import org.drools.runtime.rule.FactHandle;
 import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
 
 import com.igate.dam.dynamic.workflow.common.exception.DAMException;
 import com.igate.dam.dynamic.workflow.model.DynamicWorkFlow;
 import com.igate.dam.dynamic.workflow.service.DynamicWorkFlowService;
 import com.igate.dam.dynamic.workflow.util.ConstantUtil;
 
 public class DynamicWorkFlowServiceImpl implements DynamicWorkFlowService {
 
 	private StatefulKnowledgeSession statefulknowsession;
 	
 	
 	public StatefulKnowledgeSession getStatefulknowsession() {
 		return statefulknowsession;
 	}
 
 
 	public void setStatefulknowsession(StatefulKnowledgeSession statefulknowsession) {
 		this.statefulknowsession = statefulknowsession;
 	}
 
 
 	@Override
 	public Long triggerDynamicWorkFlow(String suppId,String suppName) throws DAMException {
 		// TODO Auto-generated method stub
 		ProcessInstance procIns = null;
 		try{
 		JPAWorkingMemoryDbLogger auditlogger = new JPAWorkingMemoryDbLogger(statefulknowsession);
 		DynamicWorkFlow dynamicWorkFlow = new DynamicWorkFlow();
 		
 		dynamicWorkFlow.setSupp_id(suppId);
 		dynamicWorkFlow.setSupp_name(suppName);
 		dynamicWorkFlow.setCurr_Step("");
 		
 		Map<String,Object> processMap = new HashMap<String,Object>();
 		processMap.put("dynamicworkflow", dynamicWorkFlow);
		procIns = statefulknowsession.startProcess(ConstantUtil.DYNAMIC_WORKFLOW_ID,processMap);
 		}
 		catch(Exception exp){
 			System.out.println(exp.getMessage());
 			throw new DAMException(exp.getMessage());
 		}
 		
 		return procIns.getId();
 	}
 
 
 	@Override
 	public void setNextWorkFlowStep(StatefulKnowledgeSession statefulSession)
 			throws DAMException {
 		// TODO Auto-generated method stub
 		try{
 			ArrayList<Object> arrList = new ArrayList<Object>(statefulSession.getObjects());
 			DynamicWorkFlow dynamicWorkFlow  = null;
 			for(Object ob:arrList){
 				dynamicWorkFlow = (DynamicWorkFlow)ob;
 			 }   
 			 System.out.println("setNextWorkFlowStep------------------------>Before"+dynamicWorkFlow);
 			 FactHandle factHandle = statefulSession.getFactHandle(dynamicWorkFlow);
 			 System.out.println("setNextWorkFlowStep------------------------>FactHandle"+factHandle);    
 			 dynamicWorkFlow.setCurr_Step(dynamicWorkFlow.getNext_Step());
 			 System.out.println("setNextWorkFlowStep------------------------>After"+dynamicWorkFlow);
 			 statefulSession.update(factHandle, dynamicWorkFlow);
 			 statefulSession.fireAllRules();
 		}
 		catch(Exception exp){
 			System.out.println(exp.getMessage());
 			throw new DAMException(exp.getMessage());
 		}
 		
 	}
 
 }
