 package com.rhc.insurance;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.StringReader;
 
 import org.drools.decisiontable.InputType;
 import org.drools.decisiontable.SpreadsheetCompiler;
 
 /**
  * This class will create a drl file from excel sheet and then execute the
  * rules.
  */
 public class DroolsExcelConverter {
 
 	public BufferedReader buildReader(String relativeName) {
 		// Create knowledge builder
 		// final KnowledgeBuilder kbuilder =
 		// KnowledgeBuilderFactory.newKnowledgeBuilder();
 
 		// Create drl file from excel sheet
 		InputStream is = null;
 		// try
 		// {
 		// ResourceFactory.newClassPathResource(
 		// "rules/HealthQuadrantRules.xls", getClass() );
 		// is = new
 		// FileInputStream("/home/sklenkar/apps/BRMS_DEMO/bmeisele/java-helloword/drools-reference-implementation/examples/insurance/src/main/resources/rules/HealthQuadrantRules.xls");
 		// String path =
 		// this.getClass().getClassLoader().getResourceAsStream("rules/HealthQuadrantRules.xls").toString();
 		// System.out.println(path);
 		// is = new FileInputStream(path);
 		is = this.getClass().getClassLoader().getResourceAsStream(relativeName);
 		// } catch (FileNotFoundException e) {
 		// e.printStackTrace();
 		// }
 		// Create compiler class instance
 		SpreadsheetCompiler sc = new SpreadsheetCompiler();
 
 		// Compile the excel to generate the (.drl) file
 		String compiledString = sc.compile(is, InputType.XLS);
		// compiledString = compiledString.replace("“", "\"");
		// compiledString = compiledString.replace("”", "\"");
 		compiledString = compiledString.replace("Member.", "$member.");
 		compiledString = compiledString.replace("Member", "$member : Member");
 		compiledString = compiledString.replace(
 				"com.rhc.insurance.$member : Member",
 				"com.rhc.insurance.Member");
 		System.out.println("here is the compiled string: ");
 		System.out.println(compiledString);
 		StringReader sr = new StringReader(compiledString);
 		BufferedReader br = new BufferedReader(sr);
 		return br;
 		/*
 		 * //return sc.compile(is, InputType.XLS);
 		 * 
 		 * StringBuffer drl=new StringBuffer(sc.compile(is, InputType.XLS));
 		 * 
 		 * // Insert dialect value into drl file
 		 * drl.insert(drl.indexOf("DROOLS")+40,"dialect \"mvel\""+"\n");
 		 * 
 		 * // Check the generated drl file
 		 * System.out.println("Generate DRL file is showing below–: ");
 		 * System.out.println(drl);
 		 * 
 		 * // writing string into a drl file try { //BufferedWriter out = new
 		 * BufferedWriter(new FileWriter(
 		 * "/home/sklenkar/apps/BRMS_DEMO/bmeisele/java-helloword/drools-reference-implementation/examples/insurance/src/main/resources/rules/HealthQuadrantRules.drl"
 		 * )); URI basePath; try { // basePath =
 		 * this.getClass().getClassLoader()
 		 * .getResource("rules/HealthQuadrantRules.drl").toURI(); // File output
 		 * = new File(basePath); // BufferedWriter out = new BufferedWriter(new
 		 * FileWriter(output));
 		 * 
 		 * //getServletContext().getRealPath();
 		 * 
 		 * ServletContext ctx = ServletConfig.getServletContext(); String path =
 		 * ctx.getRealPath("rules/HealthQuadrantRules.drl"); //FileWriter fw =
 		 * new FileWriter(path); BufferedWriter out = new BufferedWriter(new
 		 * FileWriter(path));
 		 * 
 		 * out.write(drl.toString()); out.close();
 		 * 
 		 * } catch (URISyntaxException e) { // TODO Auto-generated catch block
 		 * e.printStackTrace();
 		 * System.out.println("ERROR: output file could not be created"); }
 		 * 
 		 * } catch (IOException e){ System.out.println("Exception "); } // Wait
 		 * before using the drl file in the next section.
 		 * 
 		 * try { //Thread.sleep(10000); } catch (InterruptedException e) {
 		 * e.printStackTrace(); } // End creation of drl file from excel sheet
 		 * 
 		 * // Using DRL file
 		 * kbuilder.add(ResourceFactory.newClassPathResource("RuleFile.drl",
 		 * DroolMessage.class ), ResourceType.DRL );
 		 * 
 		 * // Check the builder for errors if ( kbuilder.hasErrors() ) {
 		 * System.out.println("kbuilder has errors"); System.out.println(
 		 * kbuilder.getErrors().toString()); } // get the compiled packages
 		 * (which are serializable) final Collection pkgs =
 		 * kbuilder.getKnowledgePackages();
 		 * 
 		 * // add the packages to a knowledgebase (deploy the knowledge
 		 * packages). final KnowledgeBase kbase =
 		 * KnowledgeBaseFactory.newKnowledgeBase();
 		 * kbase.addKnowledgePackages(pkgs);
 		 * 
 		 * // Create stateful session final StatefulKnowledgeSession ksession =
 		 * kbase.newStatefulKnowledgeSession();
 		 * 
 		 * // Set event listeners ksession.addEventListener(new
 		 * DebugAgendaEventListener()); ksession.addEventListener(new
 		 * DebugWorkingMemoryEventListener());
 		 * 
 		 * // Create message text DroolMessage messagetxt = new DroolMessage();
 		 * messagetxt.setMessage(“FlightNumber”);
 		 * 
 		 * DroolMessage messagetxt1 = new DroolMessage();
 		 * messagetxt1.setMessage(“FlightCode”);
 		 * 
 		 * // Insert into session and fire rules System.out.println(“insert into
 		 * session”); ksession.insert(messagetxt); ksession.insert(messagetxt1);
 		 * System.out.println(“before firing rules”); ksession.fireAllRules();
 		 * System.out.println(“after firing rules”); ksession.dispose();
 		 * System.out.println(“after dispose”);
 		 */
 	}
 }
