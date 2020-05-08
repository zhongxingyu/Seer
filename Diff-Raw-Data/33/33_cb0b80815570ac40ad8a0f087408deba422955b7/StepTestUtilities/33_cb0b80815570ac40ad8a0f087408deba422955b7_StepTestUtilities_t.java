 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.steps.tests;
 
 import java.util.ArrayList;
import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.ui.tests.quickfix.QuickFixTest;
 import org.eclipse.jdt.ui.text.java.IInvocationContext;
 
 /**
  * 
  * This class makes some of the protected members of {@link QuickFixTest} accessible via
  * composition.
  * 
  */
 @SuppressWarnings("restriction")
 public class StepTestUtilities extends QuickFixTest {
 
 	public StepTestUtilities(String name) {
 		super(name);
 	}
 
 	public static ArrayList<?> doCollectAssists(IInvocationContext context, boolean includeLinkedRename) throws CoreException {
 		return QuickFixTest.collectAssists(context, includeLinkedRename);
 	}
 
	public static void assertProposalExists(List actualProposals, String proposalName) {
		assertNotNull(findProposalByName(proposalName, actualProposals));
	}

 }
