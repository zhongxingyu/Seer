 package fr.imag.exschema;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 
 /**
  * 
  * @author jccastrejon
  * 
  */
 public class Util {
 
     /**
      * 
      * @param project
      * @throws JavaModelException
      */
     public static void discoverSchemas(final IJavaProject project) throws JavaModelException {
         Util.discoverRepositories(project);
         Util.discoverMongoObjects(project);
     }
 
     /**
      * 
      * @param node
      * @return
      */
     public static Block getInvocationBlock(final ASTNode node) {
         Block returnValue;
 
         returnValue = null;
         if (node != null) {
             if (Block.class.isAssignableFrom(node.getClass())) {
                 returnValue = (Block) node;
             } else {
                 returnValue = Util.getInvocationBlock(node.getParent());
             }
         }
 
         return returnValue;
     }
 
     /**
      * Spring-based repositories.
      * 
      * @param project
      * @throws JavaModelException
      */
     private static void discoverRepositories(final IJavaProject project) throws JavaModelException {
         SpringRepositoryVisitor annotationVisitor;
 
         // Identify model classes
         annotationVisitor = new SpringRepositoryVisitor();
         Util.analyzeJavaProject(project, annotationVisitor);
 
         // Analyze model classes
        System.out.println("Data model classes: ");
         for (IPackageFragment aPackage : project.getPackageFragments()) {
             if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                 for (ICompilationUnit compilationUnit : aPackage.getCompilationUnits()) {
                     for (IType type : compilationUnit.getAllTypes()) {
                         for (String domainClass : annotationVisitor.getDomainClasses()) {
                             if (type.getFullyQualifiedName().equals(domainClass)) {
                                 System.out.println("\n" + domainClass);
                                 Util.analyzeModelType(type);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * 
      * @param project
      * @throws JavaModelException
      */
     private static void discoverMongoObjects(final IJavaProject project) throws JavaModelException {
         String argumentName;
         String argumentClass;
         Block invocationBlock;
         MongoInsertVisitor insertVisitor;
         MongoUpdateVisitor updateVisitor;
 
         // Identify when objects are being saved
         insertVisitor = new MongoInsertVisitor();
         Util.analyzeJavaProject(project, insertVisitor);
 
         // Analyze save invocations
        System.out.println("Invocations: ");
         for (MethodInvocation methodInvocation : insertVisitor.getSaveInvocations()) {
             for (Object argument : methodInvocation.arguments()) {
                 argumentClass = ((Expression) argument).resolveTypeBinding().getQualifiedName();
                 if (argumentClass.equals("com.mongodb.BasicDBObject")) {
                     argumentName = argument.toString();
                     // Only work with variables
                     if (argumentName.matches("^[a-zA-Z][a-zA-Z0-9]*?$")) {
                         invocationBlock = Util.getInvocationBlock(methodInvocation);
                         if (invocationBlock != null) {
                             updateVisitor = new MongoUpdateVisitor(argumentName);
                             invocationBlock.accept(updateVisitor);
 
                             // TODO: Real analysis
                             System.out.println("\n Document: " + argumentName);
                             for (String field : updateVisitor.getFields()) {
                                 System.out.println("Field: " + field);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * 
      * @param type
      * @throws JavaModelException
      */
     private static void analyzeModelType(IType type) throws JavaModelException {
         // TODO: Real analysis...
         System.out.println("Fields:");
         for (IField field : type.getFields()) {
             System.out.println(field.getElementName() + ":" + field.getTypeSignature());
         }
     }
 
     /**
      * 
      * @param project
      * @param visitor
      * @throws JavaModelException
      */
     private static void analyzeJavaProject(final IJavaProject project, final ASTVisitor visitor)
             throws JavaModelException {
         CompilationUnit parsedUnit;
 
         for (IPackageFragment aPackage : project.getPackageFragments()) {
             if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                 for (ICompilationUnit compilationUnit : aPackage.getCompilationUnits()) {
                     parsedUnit = Util.parse(compilationUnit);
                     parsedUnit.accept(visitor);
                 }
             }
         }
     }
 
     /**
      * 
      * @param compilationUnit
      * @return
      */
     private static CompilationUnit parse(final ICompilationUnit compilationUnit) {
         ASTParser parser;
 
         parser = ASTParser.newParser(AST.JLS4);
         parser.setKind(ASTParser.K_COMPILATION_UNIT);
         parser.setSource(compilationUnit);
         parser.setResolveBindings(true);
         return (CompilationUnit) parser.createAST(null);
     }
 }
