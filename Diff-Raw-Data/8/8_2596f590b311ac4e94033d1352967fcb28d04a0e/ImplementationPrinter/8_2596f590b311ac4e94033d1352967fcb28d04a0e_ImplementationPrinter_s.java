 package qimpp;
 
 import java.util.Iterator;
 import java.util.HashMap;
 
 import xtc.tree.LineMarker;
 import xtc.tree.Node;
 import xtc.tree.GNode;
 import xtc.tree.Pragma;
 import xtc.tree.Printer;
 import xtc.tree.SourceIdentity;
 import xtc.tree.Token;
 import xtc.tree.Visitor;
 
 /**
  * A pretty printer for C++ implementation.
  *
  * @author QIMPP
  */
 public class ImplementationPrinter extends Visitor {
 
   /**
    * The printer.
    */
   protected final Printer printer;
 
 	/**
 	 * The current class in the traversal.
 	 */
 	protected String currentClass;
 
   /**
    * The GNode of the current class
    */
   protected GNode currentClassNode;
 
   /**
    *  The namespace of the current class (with a trailing :: for convenience)
    */
   protected String currentNamespace;
 
   /**
    * The main method that is printed at the end of the C++ file.
    */
   protected GNode mainMethod;
 
    /**
    * The flag for whether to line up declarations and statements with
    * their source locations.
    */
   protected final boolean lineUp;
 
   /** The operator precedence level for the current expression. */
   protected int precedence;
   
   
   /** The flag for whether we just printed a declaration. */
   protected boolean isDeclaration;
   
   
   /** The flag for whether we just printed a statement. */
   protected boolean isStatement;
   
   
   /** The flag for whether the last statement ended with an open line. */
   protected boolean isOpenLine;
   
   /**
    * The flag for whether the current statement requires nesting or
    * for whether the current declaration is nested within a for
    * statement.
    */
   protected boolean isNested;
 
   /**
    * The flag for whether this statement is the else clause of an
    * if-else statement.
    */
   protected boolean isIfElse;
 
   /**
    * The flag for whether you are in the main method
    * 
    */
   protected boolean inMain;
 
   /**
    * The flag for whether we're making a reference to a class name or an instance
    */
   protected boolean isTypeStaticReference;
 
   /**
    * The list precedence level.  This level corresponds to the
    * assignment expression nonterminal.
    */
   public static final int PREC_LIST = 10;
 
   /**
    * The base precedence level. This level corresponds to the
    * expression nonterminal.
    */
   public static final int PREC_BASE = 0;
   
   /** The flag for any statement besides an if or if-else statement. */
   public static final int STMT_ANY = 0;
 
   /** The flag for an if statement. */
   public static final int STMT_IF = 1;
 
   /** The flag for an if-else statement. */
   public static final int STMT_IF_ELSE = 2;
 
   /** The inheritance tree */
   public InheritanceTreeManager inheritanceTree;
 
   /** The root of the CPP AST*/
   public GNode compilationUnit;
 
   /** 
 	 * Create a new C++ printer.
 	 *
 	 * @param printer The printer.
    * @param lineUp The flag for whether to line up declarations and 
    * statements with their source locations.
 	 */
 	public ImplementationPrinter(Printer printer, InheritanceTreeManager inheritanceTree, GNode compilationUnit) {
 		this.printer = printer;
 		this.lineUp = true;
     this.inheritanceTree = inheritanceTree;
     this.compilationUnit = compilationUnit;
     printer.register(this);
 	}
 
 
    /**
    * Enter an expression contexti (Java AST).  The new context has the specified
    * precedence level.
    *
    * @see #exitContext(int)
    *
    * @param prec The precedence level for the expression context.
    * @return The previous precedence level.
    */
   protected int enterContext(int prec) {
     int old    = precedence;
     precedence = prec;
     return old;
   }
 
   /**
    * Enter an expression context.  The new context is appropriate for
    * an operand opposite the associativity of the current operator.
    * For example, when printing an additive expression, this method
    * should be called before printing the second operand, as additive
    * operators associate left-to-right.
    *
    * @see #exitContext(int)
    *
    * @return The previous precedence level.
    */
   protected int enterContext() {
     int old     = precedence;
     precedence += 1;
     return old;
   }
 
 
 
 /**
    * Exit an expression context.
    *
    * @see #enterContext(int)
    * @see #enterContext()
    *
    * @param prec The previous precedence level.
    */
   protected void exitContext(int prec) {
     precedence = prec;
   }
 
  /**
    * Print an expression as a truth value.  This method prints the
    * specified node.  If that node represents an assignment expression
    * and {@link #EXTRA_PARENTHESES} is <code>true</code>, this method
    * adds an extra set of parentheses around the expression to avoid
    * gcc warnings.
    *
    * @param n The node to print.
    */
  
   protected void formatAsTruthValue(Node n) {
     if (GNode.cast(n).hasName("AssignmentExpression")) {
       printer.p('(').p(n).p(')');
     } else {
       printer.p(n);
     }
   }
 
   /**
    * Start a new statement (C AST).  This method and the corresponding {@link
    * #prepareNested()} and {@link #endStatement(boolean)} methods
    * provide a reasonable default for newlines and indentation when
    * printing statements.  They manage the {@link #isDeclaration},
    * {@link #isStatement}, {@link #isOpenLine}, {@link #isNested}, and
    * {@link #isIfElse} flags.
    *
    * @param kind The kind of statement, which must be one of the
    *   three statement flags defined by this class.
    * @param node The statement's node.
    * @return The flag for whether the current statement is nested.
    */
   protected boolean startStatement(int kind, Node node) {
     if (isIfElse && ((STMT_IF == kind) || (STMT_IF_ELSE == kind))) {
       isNested = false;
     } else {
       if (lineUp) {
         if (isOpenLine) printer.pln();
         printer.lineUp(node);
       } else {
         if (isDeclaration || isOpenLine) {
           printer.pln();
         }
       }
       if (isNested) {
         printer.incr();
       }
     }
 
     isOpenLine     = false;
     boolean nested = isNested;
     isNested       = false;
 
     return nested;
   }
 
   // Java AST
   protected boolean startStatement(int kind) {
     if (isIfElse && ((STMT_IF == kind) || (STMT_IF_ELSE == kind))) {
       isNested = false;
     } 
     else {
       if (isOpenLine) printer.pln();
       if (isDeclaration) printer.pln();
       if (isNested) printer.incr();
     }
     isOpenLine = false;
     boolean nested = isNested;
     isNested = false;
                  
     return nested;
   }
 
   /**
    * Prepare for a nested statement.
    *
    * @see #startStatement
    */
   protected void prepareNested() {
     isDeclaration = false;
     isStatement   = false;
     isOpenLine    = true;
     isNested      = true;
   }
 
   protected void endStatement(boolean nested) {
     if (nested) {
       printer.decr();
     }
     isDeclaration = false;
     isStatement   = true;
   }
 
   /**
    * Start printing an expression at the specified operator precedence
    * level.
    *
    * @see #endExpression(int)
    *
    * @param prec The expression's precedence level.
    * @return The previous precedence level.
    */
   protected int startExpression(int prec) {
     if (prec < precedence) {
       printer.p('(');
     }
 		
     int old    = precedence;
     precedence = prec;
     return old;
   }
 
   /**
    * Stop printing an expression.
    *
    * @see #startExpression(int)
    *
    * @param prec The previous precedence level.
    */
   protected void endExpression(int prec) {
     if (precedence < prec) {
       printer.p(')');
     }
     precedence = prec;
   }
 
    /**
    * Print empty square brackets for the given number of dimensions.
    * 
    * @param n Number of dimensions to print.
    */
   protected void formatDimensions(final int n) {
     for (int i=0; i<n; i++) printer.p("[]");
   }
 
   /** Visit the specified compilation unit node. */
 	public void visitCompilationUnit(GNode n) {
 		printer.p("#include <iostream>\n");
     printer.p("#include <sstream>\n");
     printer.p("#include <string>\n");
 		printer.p("#include \"out.h\"\n\n");
     printer.pln();
     visit(n);
     		printer.flush();
 	}
 
   /** Visit the specified define preprocessing directive node. */
 	public void visitDefineDirective(GNode n) {
     // Do nothing for now.
 	}
 
   /** Visit the specified using preprocessing node. */
 	public void visitUsing(GNode n) {
     // Do nothing for now.
 	}
 
   /** Visit the specified namespace node. */
 	public void visitNamespace(GNode n) {
     // Do nothing for now.
 	}
 
   //TODO: HACK
   boolean inClassDeclaration = false;
 
   /** Visit the specified class declaration node. */
 	public void visitClassDeclaration(GNode n) {
 		this.currentClass = getClassName(n.getString(0)); 
     this.currentNamespace =  getNamespace(n.getString(0));
     this.currentClassNode = n;
 
 		// .class
 		printer.p("java::lang::Class").p(" ").p(currentNamespace).p("__").p(this.currentClass)
 			.pln("::__class() {");
     printer.incr();
     indentOut()
       .p("static java::lang::Class k = ")
       .p("new java::lang::__Class(__rt::literal(\"")
 			.p(this.currentClass).p("\"), ");
     //TODO: HACK
     isTypeStaticReference = true;
 		dispatch(n.getGeneric(1));
     isTypeStaticReference = false;
 		printer.pln("::__class());");
     indentOut().pln("return k;").pln("}\n");
 
 		// vtable
 		printer.p(currentNamespace).p("__").p(this.currentClass).p("_VT ")
 			.p(currentNamespace).p("__").p(this.currentClass).pln("::__vtable;\n");
     
     printer.decr();
 		visit(n.getGeneric(2));
 		
 		//visit(n.getGeneric(3));
 		
 		visit(n.getGeneric(4));
 		printer.flush();
     printer.pln();
 	}
 
   //TODO:HACK - We want a consistent syntax for "this" in constructor
   boolean inConstructor = false;
 
   /** Visit the specified constructor declaration node. */  
 	public void visitConstructorDeclaration(GNode n){
 	  // class constructor
     inConstructor = true;
 	  printer.p(currentNamespace).p("__").p(this.currentClass).p("::__")
 			.p(this.currentClass)
 			.p("() : __vptr(&__vtable) ");
     printer.incr();
     indentOut();
 	  dispatch(n.getGeneric(1));
     if (n.getGeneric(1) == null || !n.getGeneric(1).getName().equals("Block")){
       printer.p("{}");
     }
     printer.decr();
     printer.pln();
     inConstructor = false;
 	}
 
   /** Visit the specified parent class node. */
 	public void visitParent(GNode n) {
 		visit(n);
 	}
 
   /** Refrain from going deeper when visiting an inherited method
    *  - we don't need to print its implementation */
   public void visitInheritedMethodContainer(GNode n){
     return;
   }
 
   boolean inMethod = false;
   boolean didMain = false;
   boolean staticMethod = false;
 	/** 
    * Visit the specified method declaration node.
    * Only visited in implemented methods.
    */
 	public void visitImplementedMethodDeclaration(GNode n) {
     inMain = false;
     inMethod = true;
 
   	if (n.getString(0).equals("main")) {
         //TODO:HACK
         // We only want the main method of the entry class to be the official main
         // so set this only once
         // Also, this will be confused by methods called main with the wrong signature in
         // Java.
         if (!didMain){
           mainMethod = n;
           inMain = true;
           didMain = true;
         }
     }
 
     dispatch(n.getGeneric(1)); // return type
 
     if (!inMain) {
       printer.p(" ").p(currentNamespace).p("__").p(this.currentClass);
       printer.p("::").p(Type.getCppMangledMethodName(n)); // method name  
       if (n.getProperty("static") != null)
         staticMethod = true;
       //Print the FormalParameters
       dispatch(n.getGeneric(2));
       staticMethod = false;
     } 
     else {
       printer.p(" main(int argc, char** argv)"); // method name
     }
     dispatch(n.getGeneric(3)); // block
 		printer.flush();
 
     inMethod = false;
     inMain = false;
 	}
 
   boolean printedInitializers;
 
   public void visitBlock(GNode n) {
     
     printer.pln(" {");
     printer.incr();
     if (inMain && !printedInitializers){
       StaticInitializerPrinter sip = new StaticInitializerPrinter(printer);
       sip.dispatch(compilationUnit);      
       printedInitializers = true;
     }
     indentOut();
     visit(n); // block
     printer.decr(); 
     printer.pln("}\n");
   }
 
   //TODO: HACK
   boolean inPrintStatement = false;
 
   public void visitArguments(GNode n){
     for (int i = 0; i < n.size(); i++){
       if (n.get(i) instanceof String){
         printer.p(n.getString(i));
       }
       else {
         dispatch(n.getNode(i));
       }
       if ( i < (n.size() - 1))
         printer.p(", ");
     }
   }
 
   // TODO: HACK
   boolean inCallExpression = true;
 
   public void visitCallExpression(GNode n) {
     //If we're using a local call
     inCallExpression   = true;
     boolean staticCall = false;
     if (n.getGeneric(0) == null){
       //Print the call
       if (n.getProperty("static") != null && n.getProperty("private") != null){        
         printer.p(" __this->__vptr->");
       } else {
         printer.p(Type.getClassTypeName(currentClassNode.getString(0))).p("::");
       }
       printer.p(n.getString(2));
       printer.p("(");
       // Print the parameters
       if (n.getProperty("static") == null){
           printer.p(" __this ");
           if (n.getGeneric(3).size()!= 0)
             printer.p(", ");
       }
       dispatch(n.getGeneric(3));
       printer.p(")");
     }
     else if (n.getGeneric(0).getProperty(Constants.IDENTIFIER_TYPE) == Constants.PRINT_IDENTIFIER)
     {
       indentOut().p("std::cout << ");
       inPrintStatement = true;
       inCallExpression = false;
       if(0 == n.getGeneric(3).size()) printer.p("\"\""); 
       else visit(n); 
       inCallExpression = true;
       inPrintStatement = false;
      // printer.p(")");
       if (n.getString(2).equals("println")){
         printer.p(" << std::endl");
       } 
     }
 
     else {
       if (n.getProperty("static") == null){        
         //TODO: Chained calls
         // Print the correct call here
         // Get the type of the calling expression or field name, and make a _this to reference it
         // It is necessarily an instance, and should be associated with a QualifiedIdentifier
         GNode callingTypeNode = (GNode)n.getGeneric(0).getProperty(Constants.IDENTIFIER_TYPE_NODE);
         printer.p("({ ");
         dispatch(callingTypeNode);
         printer.p(" _this = ");
         //Print the nested expression
         dispatch(n.getGeneric(0));
         //End the expression;
         printer.p(" ;");
 
         // Print the actual call
         if (n.getProperty("private") == null){
           printer.p(" _this->__vptr->");
         }
         else{
           printer.p(" _this->");
         }
         printer.p(n.getString(2)).p("( _this ");
 
         // We don't want to print the comma if there are not more arguments
         if (n.getGeneric(3).size()!= 0){
           printer.p(", ");
           dispatch(n.getGeneric(3));
         }
         printer.p(");").p(" })");
       }
       else {
         GNode callingTypeNode = (GNode)n.getGeneric(0).getProperty(Constants.IDENTIFIER_TYPE_NODE);
         isTypeStaticReference = true;
         dispatch(callingTypeNode);
         isTypeStaticReference = false;
 
         printer.p("::").p(n.getString(2)).p("(");
         dispatch(n.getGeneric(3));
         printer.p(")");        
       }
     }
     inCallExpression = false;
     printer.flush();
   }
 
   /**
    * Visit the specified class instantiation, and print internal types in
    * varying modes, static for the instantiated type, instance for the argument
    * types
    */ 
   public void visitNewClassExpression(GNode n){
     //Indicate that the reference to the type is the underscore name, not an instance
     isTypeStaticReference = true;
     printer.p(" new ");
     // Dispatch on the Type node
     dispatch(n.getGeneric(2));
     printer.p("(");
     isTypeStaticReference = false;
     dispatch(n.getGeneric(3));
     printer.p(")");
   }
   
   /**
    * Visit the specified array instantiantiation
    */
   public void visitNewArrayExpression(GNode n){
     int arrayDimCount = 0;
     int concreteDimCount;
     // Total dimensions is the number of ConcreteDimensions + the number of Dimensions
     arrayDimCount += n.getGeneric(1).size();
     concreteDimCount = arrayDimCount;
     if (n.getGeneric(2) != null){
       arrayDimCount += n.getGeneric(2).size();
     }    
 
     printer.p(" ({ ")
     .p(" int32_t dim = ");
    
     // Get the first dimension in the concrete dimensions node.
     dispatch(n.getGeneric(1).getGeneric(0));
     printer.p(";");
     
     for (int i = 0; i < arrayDimCount; i++)
       printer.p(" __rt::Ptr<__rt::Array< ");
 
     // Dispatch on the Type node
     dispatch(n.getGeneric(0));
 
     for (int i = 0; i < arrayDimCount; i++)
       printer.p(" > > ");
 
     printer.p(" temp = new __rt::Array< ");
     
     for (int i = 1; i < arrayDimCount; i++)
       printer.p(" __rt::Ptr<__rt::Array< ");
 
     // Dispatch on the Type node
     dispatch(n.getGeneric(0));
 
     for (int i = 1; i < arrayDimCount; i++)
       printer.p(" > > ");
 
     printer.p("> (dim); ");
 
     if (concreteDimCount > 1){
       
       // Print the for loop to fill it, recursing on a smaller type and dimensions node
       printer.p("for (int32_t i = 0; i < dim; i++){\n temp->__data[i] = ");
 
       GNode newConcreteDimensions = GNode.create("ConcreteDimensions");
       GNode newDimensions = GNode.create("Dimensions");
       for (int i = 1; i < concreteDimCount; i++)
         newConcreteDimensions.add(n.getGeneric(1).get(i));
       for (int i = 1; i < (arrayDimCount - concreteDimCount); i++)
         newDimensions.add(n.getGeneric(1));
 
       
       GNode newArrayExpression = GNode.create("NewArrayExpression", n.getGeneric(0), newConcreteDimensions, newDimensions, null);
       visitNewArrayExpression(newArrayExpression);
 
       printer.p(" ; } ; ");
     }
 
     // Make sure the returned value is the array
     printer.p(" temp ; })");
   }
 
   boolean inReturnType = false;
 
   /** Visit the specified return type node. */  
 	public void visitReturnType(GNode n) {
     inReturnType = true;
     try {
       if (n.get(0) != null) {
         visit(n);
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     inReturnType = false;
 	}
 
   /** Visit the specified from class node. */
 	public void visitFrom(GNode n) {
 		visit(n);
 	}
 
   boolean dontCheckNull;
 
   /** Visit the specified expression node. */
 	public void visitExpression(GNode n) {
 	  dontCheckNull = true;	
     dispatch(n.getGeneric(0));
     dontCheckNull = false;
 		printer.p(' ').p(n.getString(1)).p(' ');
 		dispatch(n.getGeneric(2));
 	}
 
   public void visitExpressionStatement(GNode n) {
     visit(n);
     printer.pln(";");
   }
 
   int selectionExpressionDepth = 0;
 
   /** Visit a SelectionExpression node, and print at the most shallow level */
   public void visitSelectionExpression(GNode n){
     // Don't do anything for print commands
     if (n.getProperty(Constants.IDENTIFIER_TYPE) == Constants.PRINT_IDENTIFIER)
       return;
     
     String childIdentifierType = n.getGeneric(0).getStringProperty(Constants.IDENTIFIER_TYPE);
     GNode childIdentifierDeclaration = (GNode) n.getGeneric(0).getProperty(Constants.IDENTIFIER_DECLARATION); 
    
     if(childIdentifierType == Constants.QUALIFIED_CLASS_IDENTIFIER){
       printer.p(Type.getClassTypeName(n.getGeneric(0).getString(0)));
     } else {
 
       selectionExpressionDepth++;
       GNode type = (GNode)dispatch(n.getGeneric(0));
       selectionExpressionDepth--;
     }
     // Note: Speeding up String comparisons since we're using constants. USE THEM!
     if (childIdentifierType == Constants.CLASS_IDENTIFIER 
         || childIdentifierType == Constants.STACKVAR_IDENTIFIER
         || childIdentifierType == Constants.FIELD_IDENTIFIER
         || childIdentifierType == Constants.FOREIGN_CLASS_FIELD_IDENTIFIER)
     {
       printer.p("->").p(n.getString(1));
     }
     else if ( childIdentifierType == Constants.QUALIFIED_CLASS_IDENTIFIER ) {
       printer.p("::").p(n.getString(1));
     } 
     
   }
 
   public void visitSubscriptExpression(GNode n){
     dispatch(n.getGeneric(0));
     printer.p("->__data[");
     dispatch(n.getGeneric(1));
     printer.p("]");
   }
 
 
   /** 
    * Visit the specified primary identifier node.
    * 
    * @return A node containing the name, the Type, and whether this variable 
    *  is stack allocated GNode("Name", Type(...), true) 
    */
 	public void visitPrimaryIdentifier(GNode n) {
     boolean isQualifiedIdentifier = false;
     GNode typeNode = (GNode)n.getProperty(Constants.IDENTIFIER_TYPE_NODE);
     if (typeNode != null && typeNode.getGeneric(0).getName().equals("QualifiedIdentifier"))  
       isQualifiedIdentifier = true;
     if (inCallExpression && !inConstructor && !dontCheckNull && isQualifiedIdentifier) {
 
       printer.p("({").p(" __rt::checkNotNull(");
      
       if (null != n.getProperty(Constants.IDENTIFIER_DECLARATION) && null == ((GNode)n.getProperty(Constants.IDENTIFIER_DECLARATION))
           .getProperty("static") &&
           n.getProperty(Constants.IDENTIFIER_TYPE) == Constants.FIELD_IDENTIFIER) {
         if (inConstructor)
           printer.p("this->");
         else 
           printer.p("__this->");
       } else if (null != n.getProperty(Constants.IDENTIFIER_DECLARATION) && null != ((GNode)n.getProperty(Constants.IDENTIFIER_DECLARATION))
           .getProperty("static")) {
         String className = currentClassNode.getString(0);
          printer.p(Type.getClassTypeName(className)).p("::");
       } /*else if (null != n.getProperty(Constants.IDENTIFIER_DECLARATION) && null != ((GNode)n.getProperty(Constants.IDENTIFIER_DECLARATION)).getProperty("static")){
          int colonIndex = n.getString(0).indexOf("::");
          if(colonIndex != -1){
          String className = n.getString(0).substring(0, n.getString(0).indexOf("::"));
 
          n.set(0, Type.getClassTypeName(className) + n.getString(0).substring(n.getString(0).indexOf("::")));
          }
       }*/
 
       //TODO: change this
       //GNode typeNode = (GNode)n.getProperty(Constants.IDENTIFIER_TYPE_NODE);
       //if (inPrintStatement && typeNode != null) {
       //  if (typeNode.getGeneric(0).getString(0).equals("boolean")) {
       //    System.err.println("IT IS A BOOLEAN");
       //    printer.p("str(");
       //  }
       //}
      
       // Make sure to delimit fully-qualified names correctly
       printer.p(n.getString(0).replace(".", "::"));
       printer.p("); ");
     }
     
     if (typeNode != null && typeNode.getGeneric(0).getString(0).equals("byte") 
         && (inPrintStatement || inConcatExpression)) {
       printer.p("(int)");
     }
     
     if (null == ((GNode)n.getProperty(Constants.IDENTIFIER_DECLARATION))
         .getProperty("static") &&
         n.getProperty(Constants.IDENTIFIER_TYPE) == Constants.FIELD_IDENTIFIER) {
       if (inConstructor) printer.p("this->");
       else printer.p("__this->");
     } else if ( null != n.getProperty(Constants.IDENTIFIER_DECLARATION) && null != ((GNode)n.getProperty(Constants.IDENTIFIER_DECLARATION))
         .getProperty("static")) {
       String className = currentClassNode.getString(0);
        printer.p(Type.getClassTypeName(className)).p("::");
     } /* else if (null != n.getProperty(Constants.IDENTIFIER_DECLARATION) && null != ((GNode)n.getProperty(Constants.IDENTIFIER_DECLARATION)).getProperty("static")){
          int colonIndex = n.getString(0).indexOf("::");
          if(colonIndex != -1){
          String className = n.getString(0).substring(0, n.getString(0).indexOf("::"));
 
          n.set(0, Type.getClassTypeName(className) + n.getString(0).substring(n.getString(0).indexOf("::")));
          }
     }*/
 
     printer.p(n.getString(0).replace(".", "::"));     
     //if (inPrintStatement && typeNode != null) {
     //  if (typeNode.getGeneric(0).getString(0).equals("boolean")) {
     //  printer.p(")");
     //  } 
     //}
     if (inCallExpression && !inConstructor && !dontCheckNull && isQualifiedIdentifier) {
       printer.p("; })");
     }
   }
 
   public void visitThisExpression(GNode n){
     if (inConstructor)
       printer.p("this");
     else
       printer.p("__this");
   }
 
   /** Visit the specified instance node. */
 	public void visitInstance(GNode n) {
 		printer.p("__this->");
 		visit(n);
 	}
 
   /** Visit the specified string literal node. */
 	public void visitStringLiteral(GNode n) {
 		final int prec = startExpression(160);
     // if (!inPrintStatement) 
     printer.p("__rt::literal(");
     printer.p(n.getString(0));
     //if (!inPrintStatement)
     printer.p(")");
 	  endExpression(prec);
   }
 
   /** Visit the specified boolean literal. */
   public void visitBooleanLiteral(GNode n) {
     final int prec = startExpression(160);
     printer.p(n.getString(0));
     endExpression(prec);
   }
 
 
   /** Visit the specified formal parameters node. */
 	public void visitFormalParameters(GNode n) {
 		printer.p('(');
     boolean firstArg = true;
     if (!staticMethod){
       printer.p(this.currentClass).p(" __this");
       firstArg = false;
     }
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
       if(!firstArg)
 				printer.p(", ");
       firstArg = false;
 
       printer.p((Node)iter.next());
       
 		}
 		printer.p(')');
 	}
   
   /** Visit the specified type node. */
 	public void visitType(GNode n) {
     GNode dimensions = n.getGeneric(1);
     if (dimensions != null) {
       for (int i = 0; i < dimensions.size(); i++) {
         printer.p(" __rt::Ptr<__rt::Array<");
       }
     }
       
 		visit(n);
 
     if (dimensions != null) {
       for (int i = 0; i < dimensions.size(); i++) {
         printer.p(" > > ");
       }
     }
 	}
 
   /** Visit the specified field declaration. */
   public void visitFieldDeclaration(GNode n) {
     printer.indent().p(n.getNode(0)).p(n.getNode(1)).p(' ').p(n.getNode(2)).
       p(';').pln();
     isDeclaration = true;
     isOpenLine    = false;
   }
 
   /** Visit the specified primitive type node. */
   public void visitPrimitiveType(GNode n) {
     printer.p(Type.primitiveType(n.getString(0)));
   }
 
   /** Visit the specified qualified identifier node. */
   public void visitQualifiedIdentifier(GNode n) { 
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			String identifierName = (String)iter.next();
 			if (iter.hasNext()) {
         printer.p(identifierName);
 				printer.p("::");
 			}
       else {
         //TODO: HACK
         if ( isTypeStaticReference ) {
             //(inMethod && !inReturnType) || (inClassDeclaration)) {
 
           printer.p("__").p(identifierName);
         }
         else {
           printer.p(identifierName);
         }
       }
 		}
   }
  
 
   /** Visit the specified formal parameter node. */
 	public void visitFormalParameter(GNode n) {
 		dispatch(n.getGeneric(1));
 		printer.p(' ').p(n.getString(0));
 	}
 
   /** Visit the specified break statement node. */
 	public void visitBreakStatement(GNode n) {
 		printer.pln("break;\n");
 	}
 
   /** Visit the specified continue statement node. */
 	public void visitContinueStatement(GNode n) {
 		printer.pln("continue;\n");
 	}
 
   /** Visit the specified return statement node. */
 	public void visitReturnStatement(GNode n) {
 		printer.p("return");
 		if (null != n.getNode(0)) {
 			printer.p(' ');
 			dispatch(n.getNode(0));
 		}
 		printer.p(";\n");
 	}
 
   /** Visit the specified print expression node. */
   public void visitPrintExpression(GNode n) {
     printer.p("cout <<");
     visit(n);
     printer.pln(";\n");
   }
 
   /** Visit the specified option node. */
   public void visitOption(GNode n) {
     // Do nothing for now
   }
 
   /** Visit the specified arguments. */
   /*
   public void visitArguments(GNode n) {
     if (!inPrintStatement)
       printer.p('(');
     for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
       final int prec = enterContext(PREC_LIST);
       printer.p((Node)iter.next());
       exitContext(prec);
       if (iter.hasNext()) printer.p(", ");
     }
     if(!inPrintStatement)
       printer.p(')');
   } 
 */
 
 
 // TODO: CHANGE THIS BACK
   /** Visit the specified arguments node. */
 /*  public void visitArguments(GNode n) {
     visit(n); // one string literal for now
   } */
 
   /** Visit the specified string concatination expression node. */
 	public void visitStringConcatExpression(GNode n) {
 		printer.p("new java::lang::__String(");
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			dispatch((Node)iter.next());
 			printer.p("->data");
 			if (iter.hasNext()) {
 				printer.p(" + ");
 			}
 		}
 	}
 
   boolean inConcatExpression = false;
 
   /** Visit the specified additive expression. */
   public void visitAdditiveExpression(GNode n) {
     final int prec1 = startExpression(120);
     boolean isConcatExpression = false;
     
     GNode leftTypeNode = (GNode)n.getGeneric(0)
             .getProperty(Constants.IDENTIFIER_TYPE_NODE);
     GNode rightTypeNode = (GNode)n.getGeneric(2)
             .getProperty(Constants.IDENTIFIER_TYPE_NODE);
     
     boolean rightIsChar = false;
     if (leftTypeNode != null && leftTypeNode.getGeneric(0).getName()
         .equals("QualifiedIdentifier")) {
       isConcatExpression = leftTypeNode.getGeneric(0).getString(2)
             .equals("String");
     }
     if (rightTypeNode != null && rightTypeNode.getGeneric(0).getName()
           .equals("PrimitiveType")) {
       rightIsChar = leftTypeNode.getGeneric(0).getString(0)
           .equals("char");
     }
     if (n.getGeneric(2).getName().equals("CharacterLiteral")) {
       rightIsChar = true; 
     }
     if (!rightIsChar && leftTypeNode != null && leftTypeNode.getGeneric(0).getName()
         .equals("PrimitiveType")) {
       isConcatExpression = leftTypeNode.getGeneric(0).getString(0)
         .equals("char");
     }
    if (n.getGeneric(0).getName().equals("BasicCastExpression")) {
       System.err.println(">>> IN BASIC CAST EXPRESSION");
       isConcatExpression = n.getGeneric(0).getGeneric(0)
           .getString(0).equals("char"); 
     }
 
     if (n.getGeneric(0).getName().equals("StringLiteral") 
         || (n.getGeneric(0).getName().equals("CharacterLiteral") 
           && !rightIsChar)) {
       isConcatExpression = true;
     }
     if (isConcatExpression) {
       inConcatExpression = true;
       printer.p("__rt::literal(({\nstd::stringstream __sstr;\n__sstr <<");
     }
 
     printer.p(' ');
     dispatch(n.getGeneric(0));
     if ((n.getStringProperty(Constants.IDENTIFIER_TYPE) 
         == Constants.CLASS_IDENTIFIER) || isConcatExpression) {
       printer.p(" << ");
     }
     else {
       printer.p(" ").p(n.getString(1)).p(" ");
     }
     printer.p(' ');
 
     final int prec2 = enterContext();
     dispatch(n.getGeneric(2)); 
     exitContext(prec2);
 
     if (isConcatExpression) {
       printer.p(";\nstd::string __s = __sstr.str();\n__s.c_str();\n}))");
       inConcatExpression = false;
     }
     endExpression(prec1);
   }
 
   /** Visit the specified multiplicative expression. */
   public void visitMultiplicativeExpression(GNode n) {
     final int prec1 = startExpression(130); 
     //printer.p(((GNode)dispatch(n.getGeneric(0))).getString(0)).p(' ').p(n.getString(1)).p(' ');
     dispatch(n.getGeneric(0)); 
     printer.p(" ").p(n.getString(1)).p(" ");
     dispatch(n.getGeneric(2));
 
     final int prec2 = enterContext();
     //printer.p(((GNode)dispatch(n.getGeneric(2))).getString(0));
     exitContext(prec2);
 
     endExpression(prec1);
   }
 
   /** Visit the specified cast expression. */
   public void visitCastExpression(GNode n) {
     final int prec1 = startExpression(170);
     printer.p("__rt::java_cast< ")
       .p(n.getNode(0))
       .p(" >(").p(n.getNode(1)).p(")");
   }
 
   boolean inBasicCastExpression = false;
   /** Visit the specified basic cast expression. */
   public void visitBasicCastExpression(GNode n) {
     final int prec = startExpression(140);
     inBasicCastExpression = true;
     printer.p('(').p(n.getNode(0));
     if(null != n.get(1)) {
       printer.p(n.getNode(1));
     }
     printer.p(')').p(n.getNode(2));  
     
     inBasicCastExpression = false;
     endExpression(prec);
   }
  
 
   /** Visit the specified conditional statement. */
   public void visitConditionalStatement(GNode n) {
     final int     flag   = null == n.get(2) ? STMT_IF : STMT_IF_ELSE;
     final boolean nested = startStatement(flag);
     if (isIfElse) {
       printer.p(' ');
     } else {
       printer.indent();
     }
     printer.p("if (").p(n.getNode(0)).p(')');
     prepareNested();
     printer.p(n.getNode(1));
     if (null != n.get(2)) {
       if (isOpenLine) {
         printer.p(" else");
       } else {
         printer.indent().p("else");
       }
       prepareNested();
       boolean ifElse = isIfElse;
       isIfElse       = true;
       printer.p(n.getNode(2));
       isIfElse       = ifElse;
     }
     endStatement(nested);
   }
 
   /** Visit the specified logical or expression. */
   public void visitLogicalOrExpression(GNode n) {
     final int prec1 = startExpression(30);
     printer.p(n.getNode(0));
     printer.p(" || ");
     final int prec2 = enterContext();
     printer.p(n.getNode(1));
     exitContext(prec2);
     endExpression(prec1);
   }
   /** Visit the specified logical and expression. */
   public void visitLogicalAndExpression(GNode n) {
     final int prec1 = startExpression(40);
     printer.p(n.getNode(0));
     printer.p(" && ");
     int prec2 = enterContext();
     printer.p(n.getNode(1));
     exitContext(prec2);
     endExpression(prec1);
   }
  
   /** Visit the specified logical negation expression. */
   public void visitLogicalNegationExpression(GNode n) {
     final int prec = startExpression(150);
     printer.p('!').p(n.getNode(0));
     endExpression(prec);
   }
 
   /** Visit instanceof expression. */
   public void visitInstanceOfExpression(GNode n) {
     final int prec1 = startExpression(40);
     System.err.println("ERROR ERROR : " + n);
 
     if(n.get(1) instanceof String) printer.p(n.getString(1));
     else printer.p(n.getNode(1));
     printer.p(Constants.QUALIFIER)
       .p("__class()->isInstance(");
     if(n.get(1) instanceof String) printer.p(n.getString(1));
     else printer.p(n.getNode(1));
     printer.p(Constants.QUALIFIER).p("__class(), ");
     dispatch(n.getGeneric(0));
     printer.p(')');
     endExpression(prec1);
   }
  
   /** Visit the specified for statement. */
   public void visitForStatement(GNode n) { 
     final boolean nested = startStatement(STMT_ANY);
 
     printer.indent().p("for (").p(n.getNode(0)).p(')');
     prepareNested();
     printer.p(n.getNode(1));
                   
     endStatement(nested);
   }
 
   /** Visit the specified basic for control. */
   public void visitBasicForControl(GNode n) { 
     printer.p(n.getNode(0));
     if (null != n.get(1)) printer.p(n.getNode(1)).p(' ');
 
     final int prec1 = enterContext(PREC_BASE);
     printer.p(n.getNode(2)).p("; ");
     exitContext(prec1);
 
     if (null != n.get(3)) {
       final int prec2 = enterContext(PREC_BASE);
       formatAsTruthValue(n.getNode(3));
       exitContext(prec2);
     }    
     
     printer.p("; ");
     final int prec3 = enterContext(PREC_BASE);
     printer.p(n.getNode(4));
     exitContext(prec3);
   } 
 
   /** Visit the specified while statement. */
   public void visitWhileStatement(GNode n) {
     final boolean nested = startStatement(STMT_ANY);
     printer.indent().p("while (").p(n.getNode(0)).p(')');
     prepareNested();
     printer.p(n.getNode(1));
     endStatement(nested);
   }
 
   /** Visit the specified try catch finally statement. */
   public void visitTryCatchFinallyStatement(GNode n) {
     final boolean nested = startStatement(STMT_ANY);
 
     printer.indent().p("try");
     if (null != n.get(0)) printer.p(" (").p(n.getNode(0)).p(')');
 
     isOpenLine = true;
     printer.p(n.getNode(1)).p(' ');
 
     final Iterator<Object> iter = n.iterator();
     iter.next(); // Skip resource specification.
     iter.next(); // Skip try block.
     while (iter.hasNext()) {
       final GNode clause = GNode.cast(iter.next());
 
       isOpenLine = true;
       if (iter.hasNext()) {
         printer.p(clause).p(' ');
       } else if (null != clause) {
         printer.p("finally").p(clause);
       }
     }
 
     endStatement(nested);
   }
 
   /** Visit the specified catch clause. */
   public void visitCatchClause(GNode n) {
     GNode formal_parameters = n.getGeneric(0);
     GNode type = formal_parameters.getGeneric(1);
     String exception = formal_parameters.getString(3);
     //printer.p("catch (").p(type.getNode(0)).p(" ").p(exception).p(")").p(n.getNode(1));
     printer.p("catch (...)").p(n.getNode(1));
   }
 
   /** Visit the specified equality expression. */
   public void visitEqualityExpression(GNode n) {
     final int prec1 = startExpression(80);
     printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');
     final int prec2 = enterContext();
     printer.p(n.getNode(2));
     exitContext(prec2);
     endExpression(prec1);
   }
 
 
   /** Visit the specified declarator. */
   public void visitDeclarator(GNode n) {
     printer.p(n.getString(0));
     if(null != n.get(1)) {
       if (Token.test(n.get(1))) {
         formatDimensions(n.getString(1).length());
       } else {
         printer.p(n.getNode(1));
       }
     }
     if(null != n.get(2)) {
       printer.p(" = ").p(n.getNode(2));
     }
   }
 
   /** Visit the specified floating point literal. */
   public void visitFloatingPointLiteral(GNode n) {
     final int prec = startExpression(160);
     printer.p(n.getString(0));
     endExpression(prec);
   }
 
   /** Visit the specified character literal. */
   public void visitCharacterLiteral(GNode n) {
     final int prec = startExpression(160);
     printer.p(n.getString(0));
     endExpression(prec);
   }
 
   /** Visit the specified unary expression. */                                                       
   public void visitUnaryExpression(GNode n) {
     final int prec = startExpression(150);
     printer.p(n.getString(0)).p(n.getNode(1));
     endExpression(prec);
   }                 
 
   /** Visit the specified null literal. */
   public void visitNullLiteral(GNode n) {
     final int prec = startExpression(160);
     printer.p("__rt::null()");
     endExpression(prec);
   }
 
   /** Visit the specified integer literal. */
   public void visitIntegerLiteral(GNode n) {
     final int prec = startExpression(160);
     //if (inPrintStatement)
     //  printer.p("std::to_string(");
     printer.p(n.getString(0));
     //if (inPrintStatement)
     //  printer.p(")");
     endExpression(prec);
   }
 
   /** Visit the specified postfix expression. */
   public void visitPostfixExpression(GNode n) {
     final int prec = startExpression(160);
     printer.p(n.getNode(0)).p(n.getString(1));
     endExpression(prec);
   }
 
 
 
   /** Visit the specified expression list. */
   public void visitExpressionList(GNode n) {
     for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
       final int prec = enterContext(PREC_LIST);
       printer.p((Node)iter.next());
       exitContext(prec);
       if (iter.hasNext()) printer.p(", ");
     }
   }
  
 
   /** Visit the specified relational expression. */
   public void visitRelationalExpression(GNode n) {
     final int prec1 = startExpression(100);
     printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');
     final int prec2 = enterContext();
     printer.p(n.getNode(2));
     exitContext(prec2);
 
     endExpression(prec1);
   }
 
 
 
   /** Visit the specified declarators. */
   public void visitDeclarators(GNode n) {
     for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
       printer.p((Node)iter.next());
       if (iter.hasNext()) printer.p(", ");
     }
   }
 
 
  
 /** Visit the specified for statement node. */
 /*
   public void visitForStatement(GNode n) {
     boolean nested = startStatement(STMT_ANY, n);
 
     printer.indent().p("for (");
     if (null != n.get(0)) {
       int prec = enterContext(PREC_BASE);
       printer.p(n.getNode(0));
       exitContext(prec);
     }
     printer.p(';');
 
     if (null != n.get(1)) {
       int prec = enterContext(PREC_BASE);
       printer.p(' ');
       formatAsTruthValue(n.getNode(1));
       exitContext(prec);
     }
 
     printer.p(';');
     if (null != n.get(2)) {
       int prec = enterContext(PREC_BASE);
       printer.p(' ').p(n.getNode(2));
       exitContext(prec);
     }
     printer.p(')');
 
     prepareNested();
     printer.p(n.getNode(3));
 
     endStatement(nested);
   }
 
 */
   /** Visit the specified Node. */
 	public void visit(Node n) {
 		for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 	}
 
 
 
   /** Utility methods **/
 
   private GNode resolveScopes(GNode primaryIdentifier){
     SymbolTable.Scope scope = (SymbolTable.Scope)primaryIdentifier.getProperty(Constants.SCOPE);
     
     if ( scope == null ){
       return null;
     }
 
     GNode result = (GNode)scope.lookup(primaryIdentifier.getString(0)); 
     if (result == null){
       System.err.println("Could not locate " + primaryIdentifier.getString(0));
     }
     return result;
   }
 
   private GNode resolveClassField(String fieldName){
     HashMap<String, GNode> fieldNameMap = (HashMap<String, GNode>)currentClassNode.getProperty("FieldMap");
     GNode fieldDeclaration = fieldNameMap.get(fieldName);
     return fieldDeclaration;
   }
 
   private Printer indentOut() {
     return printer.indent();
   }
 
   private String getNamespace(String qualifiedName) {
     String[] qualifiers = qualifiedName.split("\\.");
     StringBuilder namespace = new StringBuilder();
     for ( int i = 0; i < qualifiers.length - 1; i++ ){
       namespace.append(qualifiers[i]);
       namespace.append("::");
     }
     return namespace.toString();
   }
 
   private String getClassName(String qualifiedName){
     String[] qualifiers = qualifiedName.split("\\.");
     return qualifiers[qualifiers.length - 1];
   }
 
 }
