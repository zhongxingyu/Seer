 package oop;
 
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.HashSet;
 import java.util.Properties;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.lang.JavaFiveParser;
 
 /**
  * A tool to check dependencies when translating files from Java to C++
  * 
  * -Checks to see if argument files exist
  * -Checks to see if all dependencies are accounted for
  *
  * @author Justin Bernegger
  * @version 1.0
  */
 public class DependencyMaster extends xtc.util.Tool {
 	
 	private HashSet<String> qualifiedFileNames;
 	
 	public DependencyMaster() {
 	}
 	
 	public String getName() {
 		return "Dependency Master";
 	}
 	
 	public String copy(){
 		return "Justin Bernegger";
 	}
 	
 	/**
 	 * @param String[] args, the list of filenames
 	 * @return boolean, all the dependencies are accounted for
 	 */
 	public boolean checkDependencies(String[] args){
 		qualifiedFileNames = new HashSet<String>();
 		qualifiedFileNames.add("String");
 		qualifiedFileNames.add("Class");
 		qualifiedFileNames.add("Object");
 		for(String s : args){
 			if (s.contains("/")){
 				qualifiedFileNames.add(s.substring(s.lastIndexOf('/'),s.indexOf(".java")));
 			}
 			else{
 				qualifiedFileNames.add(s.substring(0,s.indexOf(".java")));
 			}
 		}
 		for(String s : args){
 			try {
 				process(s);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ParseException e) {
 				e.printStackTrace();
 			};
 		}
 		return true;
 	}
 	
 	/**
 	 * @param String[] args, the list of filenames
 	 * @return boolean, all the files exist
 	 */
 	public boolean checkForFiles(String[] args){
 		String currentDir = System.getProperty("user.dir");
 		for(String s : args){
 			try{
 				if(!((new File(s).exists())||(new File(currentDir + '/' + s).exists()))){
 					throw new FileNotFoundException(s);
 				}
 			}
 			catch(FileNotFoundException e){
				System.out.println("File " + s + "cannot be found.");
 				e.printStackTrace();
 			}	
 		}
 		return true;
 	}
 	
 	public Node parse(Reader in, File file) throws IOException, ParseException {
 		JavaFiveParser parser =
 			new JavaFiveParser(in, file.toString(), (int)file.length());
 		Result result = parser.pCompilationUnit(0);
 		return (Node)parser.value(result);
 	}
 
 	public void process(Node node) {
 		
 		new Visitor() {
 			
 			private HashSet<Node> packagesAndImports;
 			
 			public void visitCompilationUnit(GNode n){
 				packagesAndImports = new HashSet<Node>();
 				visit(n);
 			}
 			
 			public void visitPackageDeclaration(GNode n){
 				packagesAndImports.add(n.getNode(1));
 				visit(n);
 			}
 			
 			public void visitImportDeclaration(GNode n){
 				packagesAndImports.add(n.getNode(1));
 				visit(n);
 			}
 			
 			public void visitQualifiedIdentifier(GNode n){
 				try{
 					if (!packagesAndImports.contains(n)){
 						if (!qualifiedFileNames.contains(n.getString(n.size()-1))){
 							throw new ClassNotFoundException();
 						}
 					}
 				}
 				catch(ClassNotFoundException e){
					System.out.println("Class " + (n.getString(n.size()-1)) + " not found in argument list." );
 					//e.printStackTrace();
 				}
 				visit(n);
 			}
 			
 			public void visit(Node n) {
 				for(Object o : n) if (o instanceof Node){
 					dispatch((Node)o);
 				}
 			}
 			
 		}.dispatch(node);
 	}
 }
 
