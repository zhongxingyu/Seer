 /*******************************************************************************
  * Copyright (c) 2007-2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.jsp.test.ca;
 
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.jboss.tools.common.text.xml.contentassist.test.CATestUtil;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.TestProjectProvider;
 
 public class JstJspJbide1641Test extends ContentAssistantTestCase {
 	TestProjectProvider provider = null;
 	boolean makeCopy = false;
 	private static final String PROJECT_NAME = "JsfJbide1641Test";
 	private static final String PAGE_NAME = "/WebContent/pages/greeting.xhtml";
 	private static final String PREFIX_STRING = "<h:commandButton a";
 	private static final String PROPOSAL_TO_APPLY_STRING = "action=\"\"";
 	private static final String ATTRIBUTE_TO_INSERT_STRING = "ction=\"\"";
 	private static final String POSTFIX_STRING = "></h:commandButton>";
 	private static final String INSERT_BEFORE_STRING = "<ui:composition";
 	private static final String INSERTION_STRING = PREFIX_STRING + POSTFIX_STRING;
 	private static final String COMPARE_STRING = PREFIX_STRING + ATTRIBUTE_TO_INSERT_STRING + POSTFIX_STRING;
 	
 	public static Test suite() {
 		return new TestSuite(JstJspJbide1641Test.class);
 	}
 
 	public void setUp() throws Exception {
 		provider = new TestProjectProvider("org.jboss.tools.jst.jsp.test", null, PROJECT_NAME, makeCopy); 
 		project = provider.getProject();
 	}
 
 	protected void tearDown() throws Exception {
 		if(provider != null) {
 			provider.dispose();
 		}
 	}
 
 	public void testJstJspJbide1641() {
 		
 		openEditor(PAGE_NAME);
 		
 		// Find start of <ui:composition> tag
 		String documentContent = document.get();
 		int start = (documentContent == null ? -1 : documentContent.indexOf(INSERT_BEFORE_STRING));
 		int offsetToTest = start + PREFIX_STRING.length();
 		
 		assertTrue("Cannot find the starting point in the test file  \"" + PAGE_NAME + "\"", (start != -1));
 		
 		String documentContentModified = documentContent.substring(0, start) +
 			INSERTION_STRING + documentContent.substring(start);
 		String documentContentToCompare = documentContent.substring(0, start) +
 			COMPARE_STRING + documentContent.substring(start);
 		
 		jspTextEditor.setText(documentContentModified);
 		
 		JobUtils.waitForIdle();
 		
 //		ICompletionProposal[] result= null;
 //		String errorMessage = null;
 
 		
 		List<ICompletionProposal> res = CATestUtil.collectProposals(contentAssistant, viewer, offsetToTest);
 
 		assertTrue("Content Assistant returned no proposals", (res != null && res.size() > 0));
 
 		boolean bPropoosalToApplyFound = false;
 		for (ICompletionProposal p : res) {
 			if (!(p instanceof AutoContentAssistantProposal)) 
 				continue;
 			AutoContentAssistantProposal proposal = (AutoContentAssistantProposal)p;
 			String proposalString = proposal.getReplacementString();
 
 			if (PROPOSAL_TO_APPLY_STRING.equals(proposalString)) {
 				bPropoosalToApplyFound = true;
 				proposal.apply(document);
 				break;
 			}
 		}
 		assertTrue("The proposal to apply not found.", bPropoosalToApplyFound);
 
 		try {
 			JobUtils.waitForIdle();
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue("Waiting for the jobs to complete has failed.", false);
 		} 
 
 		String documentUpdatedContent = document.get();
 		assertTrue("The proposal replacement is failed.", documentContentToCompare.equals(documentUpdatedContent));
 		
 		closeEditor();
 	}
 
 }
