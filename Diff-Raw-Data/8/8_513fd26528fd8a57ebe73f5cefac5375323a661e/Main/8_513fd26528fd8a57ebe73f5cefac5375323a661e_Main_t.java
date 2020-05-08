 package main;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * Parse a java file or directory of java files using the generated parser
  * ANTclassOrInterfaceDeclarationLR builds from java.g
  */
 class Main {
 
 	private static String[] methodModifiers = { "public", "private", "synchronized" };
 	private static Database database = new Database();
 
 	public static void main(String[] args) {
 		for (String modifier : methodModifiers) {
 			database.addAttribute("method_modifier_" + modifier);
 		}
 		
 		Collection<CommonTreePackage> treePackages = (new GenerateTreePackages()).generate();
 
 		for(CommonTreePackage treePackage : treePackages){
 			analyseTree(treePackage);
 		}		
 
 		System.out.println(database);
 	}
 
 	private static void analyseTree(CommonTreePackage treePackage) {
 		Record record = database.newRecord();
 
 		for (String modifier : methodModifiers) {
 			TreeVisitor visitor = new MethodModifierCounter(modifier);
 			int numberOfModifiers = visitor.visit(treePackage.getTree());
 			record.setValueForAttribute(numberOfModifiers, "method_modifier_" + modifier);
 		}
 
 		database.add(record);
 	}
 }
