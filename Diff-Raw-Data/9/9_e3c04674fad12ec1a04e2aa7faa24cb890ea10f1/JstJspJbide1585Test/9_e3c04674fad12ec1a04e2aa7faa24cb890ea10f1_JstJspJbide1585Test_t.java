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
import org.jboss.tools.common.base.test.contentassist.CATestUtil;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.test.util.TestProjectProvider;
 
 public class JstJspJbide1585Test extends ContentAssistantTestCase {
 	TestProjectProvider provider = null;
 	boolean makeCopy = false;
 	private static final String PROJECT_NAME = "JsfJbide1585Test"; //$NON-NLS-1$
 	private static final String PAGE_NAME = "/WebContent/pages/inputname.xhtml"; //$NON-NLS-1$
 	private static final String TAG_OPEN_STRING = "<"; //$NON-NLS-1$
 	private static final String PREFIX_STRING = "ui:in"; //$NON-NLS-1$
 	private static final String INSERTION_STRING = TAG_OPEN_STRING + PREFIX_STRING;
 
 	public static Test suite() {
 		return new TestSuite(JstJspJbide1585Test.class);
 	}
 
 	public void setUp() throws Exception {
 		provider = new TestProjectProvider("org.jboss.tools.jst.jsp.test", null, PROJECT_NAME, makeCopy);  //$NON-NLS-1$
 		project = provider.getProject();
 		Throwable exception = null;
 
 		assertNull("An exception caught: " + (exception != null? exception.getMessage() : ""), exception);  //$NON-NLS-1$//$NON-NLS-2$
 	}
 
 	protected void tearDown() throws Exception {
 		if(provider != null) {
 			provider.dispose();
 		}
 	}
 
 	public void testJstJspJbide1585() {
 		openEditor(PAGE_NAME);
 		
 		// Find start of <ui:define> tag
 		String documentContent = document.get();
 		int start = (documentContent == null ? -1 : documentContent.indexOf("<ui:define")); //$NON-NLS-1$
 		int offsetToTest = start + INSERTION_STRING.length();
 		
 		assertTrue("Cannot find the starting point in the test file  \"" + PAGE_NAME + "\"", (start != -1)); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		String documentContentModified = documentContent.substring(0, start) +
 			INSERTION_STRING + documentContent.substring(start);
 		
 		jspTextEditor.setText(documentContentModified);
 		
 		try {
 			List<ICompletionProposal> res = CATestUtil.collectProposals(contentAssistant, viewer, offsetToTest);
 	
 			assertTrue("Content Assistant returned no proposals", (res != null && res.size() > 0)); //$NON-NLS-1$
 			
 			for (ICompletionProposal p : res) {
 				assertTrue("Content Assistant returned proposals which type (" + p.getClass().getName() + ") differs from AutoContentAssistantProposal", (p instanceof AutoContentAssistantProposal));  //$NON-NLS-1$//$NON-NLS-2$
 				
 				AutoContentAssistantProposal proposal = (AutoContentAssistantProposal)p;
 				String proposalString = proposal.getReplacementString();
 				int proposalReplacementOffset = proposal.getReplacementOffset();
 				int proposalReplacementLength = proposal.getReplacementLength();
 	
 				assertTrue("The proposal replacement Offset is not correct.", proposalReplacementOffset == start + TAG_OPEN_STRING.length()); //$NON-NLS-1$
 				assertTrue("The proposal replacement Length is not correct.", proposalReplacementLength == PREFIX_STRING.length()); //$NON-NLS-1$
 				assertTrue("The proposal isn\'t filtered properly in the Content Assistant.", proposalString.startsWith(PREFIX_STRING)); //$NON-NLS-1$
 			}
 		} finally {
 			closeEditor();
 		}
 	}
 
 }
