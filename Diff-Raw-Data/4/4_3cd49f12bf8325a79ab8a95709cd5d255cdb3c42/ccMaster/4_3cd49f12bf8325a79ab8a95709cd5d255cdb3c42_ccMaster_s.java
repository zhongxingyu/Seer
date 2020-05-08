 package oop;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.util.Pair;
 
 public class ccMaster extends Visitor {
 	
 	private ccClass currentClass;
 	private ccMainMethod mainMethod;
 	private LinkedList<ccClass> classList;
 	private LinkedList<String> modifierList;
 	private String[] argumentType;
 	private String[] argumentName;
	private LinkedList<Object> BlockText;
 	private ccBlock latestBlock;
 	
 	
 	public ccMaster(){
 		modifierList = new LinkedList<String>();
 		classList = new LinkedList<ccClass>();
 	}
 	/**
 	 * Printy Thingy
 	 * 
 	 * @throws IOException
 	 */
 	public void publishToFiles() throws IOException{
 		LinkedList<String> blockLines;
 		File file = new File("main.cc");
 		FileWriter fw = new FileWriter(file);
 		BufferedWriter out = new BufferedWriter(fw);
 		out.write(mainMethod.publishDeclaration() + "{\n");
 		blockLines = mainMethod.publishBlock();
 		while(!blockLines.isEmpty()){
 			out.write(blockLines.remove(0));
 		}
 		out.write("}\n");
 		out.close();
 		
 		for(int i=0; i < classList.size(); i++){
 			file = new File(classList.get(i).getName() + ".cc");
 			fw = new FileWriter(file);
 			out = new BufferedWriter(fw);
 			for(int j=0; j < classList.get(i).getMethodCount(); j++){
				out.write(classList.get(i).getMethodAtIndex(j).publishDeclaration() + "{\n");
 				blockLines = classList.get(i).getMethodAtIndex(j).publishBlock();
 				while(!blockLines.isEmpty()){
 					out.write(blockLines.remove(0));
 				}
 				out.write("}\n");
 				out.close();
 			}
 		}
 	}
 	
 	public ccMainMethod getMainMethod(){
 		return mainMethod;
 	}
 	
 	public LinkedList<ccClass> getClassList(){
 		return classList;
 	}
 	
 
 	public void visitCompilationUnit(GNode n){
 		visit(n);
 	}
 	public void visitClassDeclaration(GNode n){
 		String name = (String)n.getString(1);
 		String access = "public";
 		boolean isStatic = false;
 		dispatch(n.getNode(0));
 		for(int i = 0; i < modifierList.size(); i++){
 			if(modifierList.get(i).matches("public|private|protected")){
 				access = modifierList.get(i);
 			}
 			else if(modifierList.get(i).matches("static")){
 				isStatic = true;
 			}
 		}
 		modifierList.clear();
 		classList.add(new ccClass(name, access, isStatic));
 		currentClass = classList.getLast();
 		visit(n);
 	}
 	public void visitConstructorDeclaration(GNode n){
 		String name = (String)n.getString(2);
 		String access = "public";
 		dispatch(n.getNode(0));
 		for(int i = 0; i < modifierList.size(); i++){
 			if(modifierList.get(i).matches("public|private|protected")){
 				access = modifierList.get(i);
 			}
 		}
 		modifierList.clear();
 		Node param = n.getNode(3);
 		argumentType = new String[param.size()];
 		argumentName = new String[param.size()];
 		for(int i = 0; i < param.size(); i++){
 			argumentType[i] = param.getNode(i).getNode(1).getNode(0).getString(0);
 			argumentName[i] = param.getNode(i).getString(3);
 		}
 		currentClass.addConstructor(new ccConstructor(name, access, argumentType, argumentName));
 	}
 	public void visitMethodDeclaration(GNode n){
 		String name = (String)n.getString(3);
 		String access = "public";
 		String returnType = "void";
 		boolean isStatic = false;
 		dispatch(n.getNode(0));
 		for(int i = 0; i < modifierList.size(); i++){
 			if(modifierList.get(i).matches("public|private|protected")){
 				access = modifierList.get(i);
 			}
 			else if(modifierList.get(i).matches("static")){
 				isStatic = true;
 			}
 		}
 		modifierList.clear();
 		if(n.getNode(2).hasName("VoidType")){ /* nope, we already good */}
 		else{
 			returnType = n.getNode(2).getNode(0).getString(0);
 		}
 		
 		Node param = n.getNode(4);
 		argumentType = new String[param.size()];
 		argumentName = new String[param.size()];
 		for(int i = 0; i < param.size(); i++){
 			argumentType[i] = param.getNode(i).getNode(1).getNode(0).getString(0);
 			argumentName[i] = param.getNode(i).getString(3);
 		}
 		visit(n);							//After the method's meta-info is collected, n visits the block, where the "guts" are assembled"
 		if(name.matches("main")){
 			mainMethod = new ccMainMethod(currentClass, access, returnType, argumentType, argumentName, isStatic, latestBlock);
 		}
 		else{
 			currentClass.addMethod(new ccMethod(name, currentClass, access, returnType, argumentType, argumentName, isStatic, latestBlock));
 		}
 	}
 	public void visitModifier(GNode n){
 		for(Object s: n){
 			if (s instanceof String)
 				modifierList.add((String)s);
 		}
 	}
 	/**visitBlock
 	 * A block is always visited after a method. This class creates a ccBlock, which will eventually 
 	 * consist of the method's "guts" and be added to the ccMethod in the form of a list or other straightforward
 	 * structure
 	 * 
 	 * Problem: visitBlock will assemble cc code, but we need to assign that block to the current method. The quick solution is to 
 	 * add a parameter to addMethod. Anyone have any other insight?
 	 */
 	public void visitBlock (GNode n){
 		latestBlock = new ccBlock(n);
 //		BlockText = new LinkedList<Object>();		
 	}
 	
 	public void visit(Node n) {
 		for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 	}
 }
