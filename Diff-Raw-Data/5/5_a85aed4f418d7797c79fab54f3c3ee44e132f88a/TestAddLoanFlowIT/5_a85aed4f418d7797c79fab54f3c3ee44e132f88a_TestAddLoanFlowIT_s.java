 package org.gsoft.phoenix.webflow;
 
 import java.math.BigDecimal;
 
 import javax.annotation.Resource;
 
 import org.gsoft.phoenix.web.controller.addloan.AddLoanFlowController;
 import org.gsoft.phoenix.web.controller.addloan.model.LoanEntryModel;
 import org.gsoft.phoenix.web.person.PersonSearchCriteria;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.webflow.config.FlowDefinitionResource;
 import org.springframework.webflow.config.FlowDefinitionResourceFactory;
 import org.springframework.webflow.core.collection.LocalAttributeMap;
 import org.springframework.webflow.core.collection.MutableAttributeMap;
 import org.springframework.webflow.test.MockExternalContext;
 import org.springframework.webflow.test.MockFlowBuilderContext;
 import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:/META-INF/spring/application-context.xml")
 public class TestAddLoanFlowIT extends AbstractXmlFlowExecutionTests {
 	@Resource
 	private AddLoanFlowController addLoanFlowController;
 	
 	@Override
 	protected FlowDefinitionResource getResource(FlowDefinitionResourceFactory resourceFactory) {
 		return resourceFactory.createFileResource("src/main/webapp/WEB-INF/views/addloan/addloan-flow.xml");
 	}
 
 
 	@Override
 	protected void configureFlowBuilderContext(MockFlowBuilderContext builderContext) {
 	    builderContext.registerBean("addLoanFlowController", addLoanFlowController);
 	}
 
 	@Test
 	public void testStartBookingFlow() {
 
 	    MutableAttributeMap input = new LocalAttributeMap();
 	    MockExternalContext context = new MockExternalContext();
 	    startFlow(input, context);
 
 	    assertCurrentStateEquals("borrowerSearch");
 	    assertTrue(getRequiredFlowAttribute("personSearchCriteria") instanceof PersonSearchCriteria);
 	    
	    context.setEventId("submit");
 	    
 	    resumeFlow(context);
 	    assertCurrentStateEquals("enterLoanDetails");
 	    assertTrue(getRequiredFlowAttribute("loanModel") instanceof LoanEntryModel);
 	    
 	    LoanEntryModel loanModel = (LoanEntryModel)getRequiredFlowAttribute("loanModel");
 	    loanModel.setStartingFees(1000);
 	    loanModel.setStartingInterest(new BigDecimal(1000));
 	    loanModel.setStartingFees(1000);
 	    
	    context.setEventId("submit");
 	    
 	    resumeFlow(context);
 	    
 	    
 	}
 }
