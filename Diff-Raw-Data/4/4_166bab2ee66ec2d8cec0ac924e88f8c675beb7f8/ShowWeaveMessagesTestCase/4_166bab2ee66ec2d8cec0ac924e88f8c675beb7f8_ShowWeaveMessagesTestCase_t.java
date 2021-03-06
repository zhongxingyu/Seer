 /* *******************************************************************
  * Copyright (c) 2004 Contributors.
  * All rights reserved. 
  * This program and the accompanying materials are made available 
  * under the terms of the Common Public License v1.0 
  * which accompanies this distribution and is available at 
  * http://www.eclipse.org/legal/cpl-v10.html 
  *  
  * Contributors: 
  *    Andy Clement     Initial version
  * ******************************************************************/
 
 package org.aspectj.ajde;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.aspectj.ajde.internal.CompilerAdapter;
 import org.aspectj.bridge.IMessage;
 import org.aspectj.util.FileUtil;
 
 /**
  * Weaving messages are complicated things.  There are multiple places where weaving
  * takes place and the places vary depending on whether we are doing a binary weave or
  * going from source.  All places that output weaving messages are tagged:
  *  // TAG: WeavingMessage
  * so you can easily find them!
  * 
  * Advice is the simplest to deal with as that is advice weaving is always done in the weaver.
  * 
  * Next is intertype declarations.  These are also always done in the weaver but in the case
  *   of a binary weave we don't know the originating source line for the ITD.
  * 
  * Finally, declares.
  *   Declare Parents: extends       Can only be done when going from source, if attempted by a 
  *                                  binary weave then an error message (compiler limitation) is
  *                                  produced.
  *   Declare Parents: implements    Is (currently!) done at both compile time and weave time.
  * 									If going from source then the message is produced by the
  * 								    code in the compiler.  if going from binary then the message
  * 									is produced by the weaver.
  *   Declare Soft:                  Comes out with 'advice' as a special kind of advice: softener advice
  * 
  * 
  * Q: Where are the messages turned on/off?
  * A: It is a bit messy.  See BuildArgParser.genBuildConfig().  Basically that method is the first time
  *    we parse the option set.  Whether weaving messages are on or off is stored in the build config.
  *    As soon as we have parser the options and determined that weave messages are on, we grab the 
  *    top level message handler and tell it not to ignore WeaveInfo messages.
  * 
  * 
  * TODO - Other forms of declare?  Do they need messages? e.g. declare precedence * 
  */
 public class ShowWeaveMessagesTestCase extends AjdeTestCase {

	private static boolean regenerate;
 	
 	static {
 		// Switch this to true for a single iteration if you want to reconstruct the
 		// 'expected weaving messages' files.
 		regenerate = false;
 	}
 	
 	private CompilerAdapter compilerAdapter;
 	public static final String PROJECT_DIR = "WeaveInfoMessagesTest";
 
 	public static final String binDir = "bin";
 	public static final String expectedResultsDir = "expected";
 			
 			
 	public ShowWeaveMessagesTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/*
 	 * Ensure the output directory in clean
 	 */
 	protected void setUp() throws Exception {
 		super.setUp(PROJECT_DIR);
 		FileUtil.deleteContents(openFile(binDir));
 	}
 	
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		FileUtil.deleteContents(openFile(binDir));
 	}
 
 
 
 
 	/**
 	 * Weave all the possible kinds of advice and verify the messages that come out.
 	 */
 	public void testWeaveMessagesAdvice() {
 		System.out.println("testWeaveMessagesAdvice: Building with One.lst");
 		compilerAdapter = new CompilerAdapter();
 		compilerAdapter.showInfoMessages(true);
 		compilerAdapter.compile((String) openFile("One.lst").getAbsolutePath(),new BPM(),false);
 		verifyWeavingMessages("advice",true);
 	}
 	
 
 
 
 	/**
 	 * Weave field and method ITDs and check the weave messages that come out.
 	 */
 	public void testWeaveMessagesITD() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesITD: Building with Two.lst");
 		compilerAdapter = new CompilerAdapter();
 		compilerAdapter.showInfoMessages(true);
 		compilerAdapter.compile((String) openFile("Two.lst").getAbsolutePath(),new BPM(),false);
 		verifyWeavingMessages("itd",true);
 	}
 	
 	
 	/**
 	 * Weave "declare parents: implements" and check the weave messages that come out.
 	 */
 	public void testWeaveMessagesDeclare() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesDeclare: Building with Three.lst");
 		compilerAdapter = new CompilerAdapter();
 		compilerAdapter.showInfoMessages(true);
 		compilerAdapter.compile((String) openFile("Three.lst").getAbsolutePath(),new BPM(),false);
 		verifyWeavingMessages("declare1",true);		
 	}
 	
 	/**
 	 * Weave "declare parents: extends" and check the weave messages that come out.
 	 * Can't do equivalent binary test - as can't do extends in binary.
 	 */
 	public void testWeaveMessagesDeclareExtends() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesDeclareExtends: Building with Four.lst");
 		compilerAdapter = new CompilerAdapter();
 		compilerAdapter.showInfoMessages(true);
 		compilerAdapter.compile((String) openFile("Four.lst").getAbsolutePath(),new BPM(),false);
 		verifyWeavingMessages("declare.extends",true);		
 	}
 	
 	/**
 	 * Weave "declare soft: type: pointcut" and check the weave messages that come out.
 	 */
 	public void testWeaveMessagesDeclareSoft() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesDeclareSoft: Building with Five.lst");
 		compilerAdapter = new CompilerAdapter();
 		compilerAdapter.showInfoMessages(true);
 		compilerAdapter.compile((String) openFile("Five.lst").getAbsolutePath(),new BPM(),false);
 		verifyWeavingMessages("declare.soft",true);		
 	}
 	
 	
 	// BINARY WEAVING TESTS
 	
 	/**
 	 * Binary weave variant of the advice weaving test above - to check messages are ok for
 	 * binary weave.  Unlike the source level weave, in this test we are using an aspect on
 	 * the aspectpath - which means it has already had its necessary parts woven - so the list 
 	 * of weaving messages we expect is less.
 	 */
 	public void testWeaveMessagesBinaryAdvice() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryAdvice: Simple.jar + AspectAdvice.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectAdvice.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
         List l = ideManager.getCompilationSourceLineTasks();
         verifyWeavingMessages("advice.binary",true);
 	}
 	
 	public void testWeaveMessagesBinaryITD() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryITD: Simple.jar + AspectITD.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectITD.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		List l = ideManager.getCompilationSourceLineTasks();
 		verifyWeavingMessages("itd",false);
 	}
 
 	
 	public void testWeaveMessagesBinaryDeclare() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryDeclare: Simple.jar + AspectDeclare.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectDeclare.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		verifyWeavingMessages("declare1",false);	
 	}
 	
 	/**
 	 * Weave "declare soft: type: pointcut" and check the weave messages that come out.
 	 */
 	public void testWeaveMessagesBinaryDeclareSoft() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryDeclareSoft: Simple.jar + AspectDeclareSoft.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectDeclareSoft.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		verifyWeavingMessages("declare.soft",false);	
 	}
 
 
 
 
 	// BINARY WEAVING WHEN WE'VE LOST THE SOURCE POINTERS
 
 	public void testWeaveMessagesBinaryAdviceNoDebugInfo() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryAdvice: Simple.jar + AspectAdvice.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple_nodebug.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectAdvice_nodebug.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		List l = ideManager.getCompilationSourceLineTasks();
 		verifyWeavingMessages("advice.binary.nodebug",true);
 	}
 	
 	public void testWeaveMessagesBinaryITDNoDebugInfo() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryITD: Simple.jar + AspectITD.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple_nodebug.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectITD_nodebug.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		List l = ideManager.getCompilationSourceLineTasks();
 		verifyWeavingMessages("itd.nodebug",true);
 	}
 	
 	public void testWeaveMessagesBinaryDeclareNoDebugInfo() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryDeclareNoDebugInfo: Simple.jar + AspectDeclare.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple_nodebug.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectDeclare_nodebug.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		verifyWeavingMessages("declare1.nodebug",true);	
 	}	
 	
 	/**
 	 * Weave "declare soft: type: pointcut" and check the weave messages that come out.
 	 */
 	public void testWeaveMessagesBinaryDeclareSoftNoDebugInfo() {
 		System.out.println("\n\n\n\n\n\ntestWeaveMessagesBinaryDeclareSoftNoDebugInfo: Simple.jar + AspectDeclareSoft.jar");
 		Set inpath = new HashSet();
 		inpath.add(openFile("Simple_nodebug.jar"));
 		ideManager.getProjectProperties().setInpath(inpath);
 		Set aspectpath = new HashSet();
 		aspectpath.add(openFile("AspectDeclareSoft_nodebug.jar"));
 		ideManager.getProjectProperties().setAspectPath(aspectpath);
 		assertTrue("Build failed", doSynchronousBuild("Empty.lst"));
 		verifyWeavingMessages("declare.soft.nodebug",true);	
 	}
 
 
 	private class BPM implements BuildProgressMonitor {
 		public void start(String configFile) {}
 
 				public void setProgressText(String text) {}
 
 				public void setProgressBarVal(int newVal) {	}
 
 				public void incrementProgressBarVal() {}
 
 				public void setProgressBarMax(int maxVal) {	}
 
 				public int getProgressBarMax() {
 					return 0;
 				}
 
 				public void finish() {}
 		
 	}
 	
 
 	public void verifyWeavingMessages(String testid,boolean source) {
 		File expectedF = openFile(expectedResultsDir+File.separator+testid+".txt");
 		if (regenerate && source) {
 			// Create the file
 			saveWeaveMessages(expectedF);
 		} else {
 			// Verify the file matches what we have
 			compareWeaveMessages(expectedF);
 		}
 	}
 	
 	/**
 	 * Compare weaving messages with what is in the file
 	 */
 	private void compareWeaveMessages(File f) {
 		List fileContents = new ArrayList();
 		BufferedReader fr;
 		try {
 			// Load the file in
 			fr = new BufferedReader(new FileReader(f));
 			String line = null;
 			while ((line=fr.readLine())!=null) fileContents.add(line);
 			
 			// See if the messages match
 			int msgCount = 0;
 			List l = ideManager.getCompilationSourceLineTasks();
 			for (Iterator iter = l.iterator(); iter.hasNext();) {
 				IMessage msg = ((NullIdeTaskListManager.SourceLineTask) iter.next()).message;
 				if (msg.getKind().equals(IMessage.WEAVEINFO)) {
 					if (!fileContents.contains(msg.getMessage())) {
 						fail("Could not find message '"+msg.getMessage()+"' in the expected results\n"+
 						stringify(fileContents));
 					} else {
 						fileContents.remove(msg.getMessage());
 					}
 					msgCount++;
 				}
 			}
 			assertTrue("Didn't get these expected messages: "+fileContents,fileContents.size()==0);
 			System.out.println("Successfully verified "+msgCount+" weaving messages");
 		} catch (Exception e) {
 			fail("Unexpected exception saving weaving messages:"+e);
 		}
 	}
 	
 	private String stringify(List l) {
 		StringBuffer result = new StringBuffer();
 		for (Iterator iter = l.iterator(); iter.hasNext();) {
 			String str = (String) iter.next();
 			result.append(str);result.append("\n");
 		}
 		return result.toString();
 	}
 	
 	/**
 	 * Store the weaving messages in the specified file.
 	 */
 	private void saveWeaveMessages(File f) {
 		System.out.println("Saving weave messages into "+f.getName());
 		FileWriter fw;
 		try {
 			fw = new FileWriter(f);	
 			List l = ideManager.getCompilationSourceLineTasks();
 			for (Iterator iter = l.iterator(); iter.hasNext();) {
 				IMessage msg = ((NullIdeTaskListManager.SourceLineTask) iter.next()).message;
 				if (msg.getKind().equals(IMessage.WEAVEINFO)) {
 			  		fw.write(msg.getMessage()+"\n");
 				}
 			}
 			fw.close();
 		} catch (Exception e) {
 			fail("Unexpected exception saving weaving messages:"+e);
 		}
 	}
 	
 	
 
 }
