 package superintents.util;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.eclipse.jdt.core.dom.*;
 import org.eclipse.jdt.core.dom.InfixExpression.Operator;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 
 import superintents.util.ASTNodeWrapper.NodeType;
 import intentmodel.impl.*;
 
 public class jIntent2AST {
 	
 	private static String intentName;
 	private static String requestCodeName;
 	private static int requestCodeValue;
 
 	public static SuperIntentImpl createTestSI() {
 		SuperIntentImpl bigRedButtonIntent = new SuperIntentImpl();
 		bigRedButtonIntent.setDescription("THIS IS AN INTENT DESCRIPTION");
 
 		DataImpl bigRedButtonOutput = new DataImpl();
 		bigRedButtonOutput.setMIMEType("THIS IS A MIME TYPE");
 		bigRedButtonOutput.setValue("THISISAVALUE");
 		
 		bigRedButtonIntent.setOutput(bigRedButtonOutput);
 		
 		IntentImpl newIntent = new IntentImpl();
 		newIntent.setAction("THIS IS AN ACTION");
 		
 		newIntent.getCategories().add("CATEGORY1");
 		newIntent.getCategories().add("CATEGORY2");
 		
 		newIntent.setComponent("String");
 		
 		DataImpl data = new DataImpl();
 		data.setValue("THISISDATA");
 		data.setMIMEType(".mp3");
 		newIntent.setData(data);
 		
 		bigRedButtonIntent.setIntent(newIntent);
 		
 		bigRedButtonIntent.getIntent().getExtras().put("SECOND URL", "YOUR TEXT HERE");
 		bigRedButtonIntent.getIntent().getExtras().put("THIRD URL", "YOUR TEXT HERE");
 		
 		return bigRedButtonIntent;
 	}
 	
 	
 	public static ArrayList<ASTNodeWrapper> transformSuperIntent(SuperIntentImpl si)
 	{
 		CompilationUnit cu = JDTHelper.getASTTupleHelper().compilationUnit;
 		
 		//Get at valid intent name
 		intentName = "i";
 		int nameCounter = 1;
 		while (doesVariableNameExist(cu, intentName)) {
 			intentName = intentName + nameCounter;
 			nameCounter++;
 		}	
 		
 		//result list
 		ArrayList<ASTNodeWrapper> resultList = new ArrayList<ASTNodeWrapper>();
 		
 		//AST for generating nodes
 		AST ast = AST.newAST(AST.JLS4);
 		
 		//Add imports
 		if(!doesImportExist(cu,"android.content.Intent"))
 			resultList.add(new ASTNodeWrapper(generateIntentImport(ast),NodeType.IMPORT));
 		
 		//Insert Input and OutPut Comments
 		if(si.getDescription() != null)
 			resultList.add(newCommentInsideMethod("Description: \n// " + si.getDescription()));
 		if(si.getOutput() != null)
 			resultList.add(newCommentInsideMethod("Output: \n// " + si.getOutput()));
 		
 		//Initialize the Intent
 		for (ASTNode astNode : initializeIntent(si, ast)) {
 			if(astNode.getClass().equals(ImportDeclaration.class) && !doesImportExist(cu,"android.net.Uri"))
 				resultList.add(new ASTNodeWrapper(astNode, NodeType.IMPORT));
			else if (!(astNode.getClass().equals(ImportDeclaration.class)))
 				resultList.add(new ASTNodeWrapper(astNode));
 		}
 		
 		//Set the data type
 		if(si.getIntent().getData() != null && si.getIntent().getData().getMIMEType() != null) {
 			resultList.add(new ASTNodeWrapper(setType(si, ast)));
 		}
 		
 		//Set Categories
 		for (String category : si.getIntent().getCategories()) {
 			resultList.add(new ASTNodeWrapper(setCategory(category, ast)));
 		}
 		
 		//Set Extras
 		for (String extra : si.getIntent().getExtras().keySet()) {
 			resultList.add(new ASTNodeWrapper(setExtra(extra, si.getIntent().getExtras().get(extra), ast)));
 		}
 		
 		//Add callback method
 		if((si.getOutput() != null) )
 		{
 			Random random = new Random();
 			requestCodeValue = random.nextInt(10000000);
 			requestCodeName = "REQUEST_CODE_" + intentName.toUpperCase();
 			//check for existing duplicate variables
 			int requestNameCounter = 1;
 			while (doesVariableNameExist(cu, requestCodeName)) {
 				requestCodeName = requestCodeName + requestNameCounter;
 				requestNameCounter++;
 			}		
 			
 			resultList.add(new ASTNodeWrapper(generateRequestCode(ast),NodeType.FIELD));
 			resultList.add(new ASTNodeWrapper(callStartActivity(ast)));
 			
 			MethodDeclaration md = doesMethodExist(cu, "onActivityResult");
 			if (md == null)
 				resultList.add(new ASTNodeWrapper(generateCallbackMethod(ast),NodeType.CALLBACK_METHOD));
 			else 
 				resultList.add(new ASTNodeWrapper(generateCallbackMethodBody(ast), NodeType.CALLBACK_METHOD, md));
 		}
 		else
 			resultList.add(new ASTNodeWrapper(startActivity(ast)));
 			
 		return resultList;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static ASTNode startActivity(AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setName(ast.newSimpleName("startActivity"));
 
 		// set argument
 		SimpleName sn = ast.newSimpleName(intentName);
 		mi.arguments().add(sn);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 
 	private static ASTNode generateIntentImport(AST ast) {
 		
 		//insert the import android.content.Intent;
 		ImportDeclaration i = ast.newImportDeclaration();
 		
 		QualifiedName q1 = ast.newQualifiedName(ast.newSimpleName("android"), ast.newSimpleName("content"));
 		QualifiedName q2 = ast.newQualifiedName(q1, ast.newSimpleName("Intent"));
 		
 		i.setName(q2);
 		
 		return i;
 	}
 	
 	private static ASTNode generateURIImport(AST ast) {
 		
 		//insert the import android.net.Uri;
 		ImportDeclaration i = ast.newImportDeclaration();
 		
 		QualifiedName q1 = ast.newQualifiedName(ast.newSimpleName("android"), ast.newSimpleName("net"));
 		QualifiedName q2 = ast.newQualifiedName(q1, ast.newSimpleName("Uri"));
 		
 		i.setName(q2);
 		
 		return i;
 	}
 
 	private static ArrayList<ASTNode> initializeIntent(SuperIntentImpl si, AST ast)
 	{
 		ArrayList<ASTNode> result = new ArrayList<ASTNode>();
 		
 		//If the componenet isn't a valid Java identifier we change it
 		if(si.getIntent().getComponent() != null && !isValidJavaIdentifier(si.getIntent().getComponent()))
 			si.getIntent().setComponent("NOT_A_VALID_IDENTIFIER_" + si.getIntent().getComponent());
 		
 		//Intent(String action, Uri uri, Context packageContext, Class<?> cls)
 		if(si.getIntent().getAction() != null && si.getIntent().getData() != null && si.getIntent().getComponent() != null)
 		{
 			result.add(generateURIImport(ast));
 			result.add(InitializeConstructor1(si, ast));
 		}
 		//Intent(Context packageContext, Class<?> cls)
 		else if(si.getIntent().getComponent() != null) 
 			result.add(InitializeConstructor2(si, ast));
 		//Intent(String action, Uri uri)
 		else if(si.getIntent().getAction() != null && si.getIntent().getData() != null)
 		{
 			result.add(generateURIImport(ast));
 			result.add(InitializeConstructor3(si, ast));
 		}
 		//Intent(String action)
 		else if(si.getIntent().getAction() != null)
 			result.add(InitializeConstructor4(si, ast));
 		//Intent(Intent o) is not supported
 		//Intent()
 		else 
 			result.add(InitializeConstructor5(si, ast));
 		
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor1(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		StringLiteral arg1 = ast.newStringLiteral();
 		arg1.setLiteralValue(si.getIntent().getAction());
 		cic.arguments().add(arg1);
 		
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName("Uri"));
 		mi.setName(ast.newSimpleName("parse"));
 		StringLiteral sl = ast.newStringLiteral();
 		sl.setLiteralValue(si.getIntent().getData().getValue());
 		mi.arguments().add(sl);
 		cic.arguments().add(mi);
 		
 		TypeLiteral arg3 = ast.newTypeLiteral();
 		arg3.setType(ast.newSimpleType(ast.newSimpleName(si.getIntent().getComponent())));
 		cic.arguments().add(arg3);
 		
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor2(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		TypeLiteral arg1 = ast.newTypeLiteral();
 		arg1.setType(ast.newSimpleType(ast.newSimpleName(si.getIntent().getComponent())));
 		cic.arguments().add(arg1);
 		
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor3(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		StringLiteral arg1 = ast.newStringLiteral();
 		arg1.setLiteralValue(si.getIntent().getAction());
 		cic.arguments().add(arg1);
 
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName("Uri"));
 		mi.setName(ast.newSimpleName("parse"));
 		StringLiteral sl = ast.newStringLiteral();
 		sl.setLiteralValue(si.getIntent().getData().getValue());
 		mi.arguments().add(sl);
 		cic.arguments().add(mi);
 
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor4(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		StringLiteral arg1 = ast.newStringLiteral();
 		arg1.setLiteralValue(si.getIntent().getAction());
 		cic.arguments().add(arg1);
 
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	private static ASTNode InitializeConstructor5(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode setType(SuperIntentImpl si, AST ast)
 	{
 		//set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(intentName));
 		mi.setName(ast.newSimpleName("setType"));
 		
 		//set argument
 		StringLiteral sl = ast.newStringLiteral();
 		sl.setLiteralValue(si.getIntent().getData().getMIMEType());
 		mi.arguments().add(sl);
 		
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 		
 		return es;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode setCategory(String category, AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(intentName));
 		mi.setName(ast.newSimpleName("addCategory"));
 
 		// set argument
 		StringLiteral sl = ast.newStringLiteral();
 		sl.setLiteralValue(category);
 		mi.arguments().add(sl);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode setExtra(String extra, String value, AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(intentName));
 		mi.setName(ast.newSimpleName("putExtra"));
 
 		// set argument
 		StringLiteral sl1 = ast.newStringLiteral();
 		sl1.setLiteralValue(extra);
 		mi.arguments().add(sl1);
 		
 		StringLiteral sl2 = ast.newStringLiteral();
 		sl2.setLiteralValue(value);
 		mi.arguments().add(sl2);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 	
 	private static ASTNodeWrapper newCommentInsideMethod(String comment) {
 		return new ASTNodeWrapper(comment);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode callStartActivity(AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setName(ast.newSimpleName("startActivityForResult"));
 
 		// set argument 1
 		SimpleName sl1 = ast.newSimpleName(intentName);
 		mi.arguments().add(sl1);
 		
 		// set argument 2
 		SimpleName sl2 = ast.newSimpleName(requestCodeName);
 		mi.arguments().add(sl2);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static ASTNode generateRequestCode(AST ast) {
 		VariableDeclarationFragment vdf =  ast.newVariableDeclarationFragment();
 		//set var name
 		vdf.setName(ast.newSimpleName(requestCodeName));
 	
 		//set value
 		vdf.setInitializer(ast.newNumberLiteral(requestCodeValue + ""));
 		
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		
 		//set final modifer
 		Modifier mo = ast.newModifier(ModifierKeyword.FINAL_KEYWORD);
 		f.modifiers().add(mo);
 		
 		//set int type
 		f.setType(ast.newPrimitiveType(PrimitiveType.INT));
 
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode generateCallbackMethod(AST ast) {
 		//method declaration
 		MethodDeclaration m = ast.newMethodDeclaration();
 		m.setName(ast.newSimpleName("onActivityResult"));
 		
 		//add the @Override annotation
 		MarkerAnnotation ma = ast.newMarkerAnnotation();
 		ma.setTypeName(ast.newSimpleName("Override"));
 		m.modifiers().add(ma);
 		
 		//add the "public" keyword
 		Modifier mo = ast.newModifier(ModifierKeyword.PROTECTED_KEYWORD);
 		m.modifiers().add(mo);
 		
 		//parameters
 		SingleVariableDeclaration svd1 = ast.newSingleVariableDeclaration();
 		svd1.setType(ast.newPrimitiveType(PrimitiveType.INT));
 		svd1.setName(ast.newSimpleName("requestCode"));
 		m.parameters().add(svd1);
 		
 		SingleVariableDeclaration svd2 = ast.newSingleVariableDeclaration();
 		svd2.setType(ast.newPrimitiveType(PrimitiveType.INT));
 		svd2.setName(ast.newSimpleName("resultCode"));
 		m.parameters().add(svd2);
 		
 		SingleVariableDeclaration svd3 = ast.newSingleVariableDeclaration();
 		svd3.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		svd3.setName(ast.newSimpleName("data"));
 		m.parameters().add(svd3);
 		
 		//Generate the internal if statement
 		m.setBody(ast.newBlock());
 		m.getBody().statements().add(generateCallbackMethodBody(ast));
 		
 		return m;
 	}
 	
 	private static ASTNode generateCallbackMethodBody(AST ast)
 	{	
 		IfStatement is = ast.newIfStatement();
 		InfixExpression ie1 = ast.newInfixExpression();
 		
 		InfixExpression leftIe = ast.newInfixExpression();
 		leftIe.setLeftOperand(ast.newSimpleName("resultCode"));
 		leftIe.setOperator(Operator.EQUALS);
 		leftIe.setRightOperand(ast.newSimpleName("RESULT_OK"));
 		ie1.setLeftOperand(leftIe);
 		
 		InfixExpression RightIe = ast.newInfixExpression();
 		RightIe.setLeftOperand(ast.newSimpleName("requestCode"));
 		RightIe.setOperator(Operator.EQUALS);
 		RightIe.setRightOperand(ast.newSimpleName(requestCodeName));
 		ie1.setRightOperand(RightIe);
 		
 		ie1.setOperator(Operator.CONDITIONAL_AND);
 		
 		is.setExpression(ie1);
 		
 		return is;
 	}
 	
 	private static boolean doesImportExist(CompilationUnit cu, String name)
 	{
 		ImportASTVisitor astv = new ImportASTVisitor(name);
 		
 		cu.accept(astv);
 		
 		return astv.getExists();
 	}
 	
 	//returns null if no method is found
 	public static MethodDeclaration doesMethodExist(CompilationUnit cu, String name)
 	{
 		MethodASTVisitor astv = new MethodASTVisitor(name);
 		
 		cu.accept(astv);
 		
 		return astv.getMethod();
 	}
 	
 	//returns null if no method is found
 	private static boolean doesVariableNameExist(CompilationUnit cu, String name)
 	{
 		VariableNameASTVisitor astv = new VariableNameASTVisitor(name);
 		
 		cu.accept(astv);
 		
 		return astv.getExists();
 	}
 	
 	public final static boolean isValidJavaIdentifier(String s) {
 	 // an empty or null string cannot be a valid identifier
 	 if (s == null || s.length() == 0)
 	    return false;
 
 	 char[] c = s.toCharArray();
 	 if (!Character.isJavaIdentifierStart(c[0])) {
 	    return false;
 	 }
 
 	 for (int i = 1; i < c.length; i++) {
 	    if (!Character.isJavaIdentifierPart(c[i]))
 	       return false;
 	 }
 
 	 return true;
 	}
 }
 	
