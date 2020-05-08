 package viz;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import Interpreter.Global;
 
 //TODO: highlighting doesn't always work.
 /**
  * <code>XAALConnector</code> is used by the interpreters to convert the various
  * displayable entities (variables, scopes, source code) into the proper XAAL
  * script. <br>
  * <br>
  * XAALConnector has two phases:
  * <ol>
  * <li>A recording phase and</li>
  * <li>A drawing phase</li>
  * </ol>
  * <p>
  * The first phase happens as the source code is interpreted by the interpreter.
  * The interpreter calls the methods to perform add, show, hide, move and other
  * actions on the displayable entities. XAALConnector records each as a
  * <code>FutureAction</code>.
  * </p>
  * <p>
  * Once the first phase is finished, the interpreter calls the <code>draw</code>
  * method. <code>draw</code> writes the entity to the XAAL script and then
  * performs each <code>FutureAction</code>.
  */
 public class XAALConnector {
 
 	// how far the foo code block should move to the right.
 	private final int dxOnScopeReplace = 400;
 
 	// the colors scopes can be and the order in which they're used
 	private static LinkedList<String> scopeColors;
 	static {
 		scopeColors = new LinkedList<String>();
 		scopeColors.add("blue");
 		scopeColors.add("purple");
 		scopeColors.add("green");
 	}
 
 	// the number of the snap we're at
 	private int currentSnapNum;
 
 	// the color used to highlight text
 	public final String highlightColor = "red";
 
 	// the FutureActions to be performed in draw
 	private LinkedList<FutureAction> actions;
 
 	private XAALScripter scripter;
 
 	// a mapping of the Variable used in the interpreter to a Variable used for
 	// display
 	private HashMap<UUID, Variable> varToVar;
 	
 	//private HashMap<UUID, Variable> varToCacheVar;
 
 	// a mapping of the name of a scope to the the Scope used for display
 	private HashMap<String, Scope> scopes;
 	// the questions to be written to the script
 	private ArrayList<Question> questions;
 
 	private Scope globalScope;
 	
 	private String title;
 	
 	// holds all the CodePages used by the XAALConnector
 	private CodePageContainer cpc;
 
 	//snap then line
 	private HashMap<Integer, Integer> lineToHighlightOnSnap;
 	
 	private String[] previewPseudo = null;
 	
 	private int snapRegularPseudoStartsAt = -1;
 	
 	private String[] pseudoCode = null;
 	
 	private int lineToHighlight = -1;
 	
 	// the action that needs to be done
 	private CallByNameAction callByNameAction = null;
 
 	/**
 	 * Constructor for <code>XAALConnector</code>
 	 * 
 	 * @param psuedoCode
 	 *            the pseudocode used at the beginning of the visualization
 	 * @param title
 	 *            the title of the visualization
 	 */
 	public XAALConnector(String[] pseudoCode, String title) 
 	{
 		XAALSetup();
 		this.title = title;
 		
 		this.pseudoCode = pseudoCode;
 		this.snapRegularPseudoStartsAt = 0;
 	}
 	
 	public XAALConnector(String[] pseudoCode, String title, boolean codeIsPreview)
 	{
 		XAALSetup();
 		this.title = title;
 		
 		if (codeIsPreview)
 			this.previewPseudo = pseudoCode;
 		else
 		{
 			this.pseudoCode = pseudoCode;
 			this.snapRegularPseudoStartsAt = 0;
 		}
 		
 	}
 	
 	private void XAALSetup()
 	{
 		scripter = new XAALScripter();
 		varToVar = new HashMap<UUID, Variable>();
 		scopes = new HashMap<String, Scope>();
 		questions = new ArrayList<Question>();
 		currentSnapNum = 0;
 		
 		
 		actions = new LinkedList<FutureAction>();
 		this.cpc = new CodePageContainer();
 		this.lineToHighlightOnSnap = new HashMap<Integer,Integer>();
 	}
 	
 	//Tom added this and it sucks!!!!!
 	/*public void setPseudocode(String[] code)
 	{
 		pseudo.setPseudocode(code);
 	}*/
 
 	/**
 	 * Adds a <code>CodePage</code> for use by <code>XAALConnector</code>.
 	 * 
 	 * @param code
 	 *            the code to be displayed on screen.
 	 * @return the unique id of the <code>CodePage</code>.
 	 */
 	public String addCodePage(String[] code) {
 		return cpc.createCodePage(code);
 	}
 	
 	public String addCodePage(String[] code, boolean showAtStart)
 	{
 		return cpc.createCodePage(code, showAtStart);
 	}
 
 	/**
 	 * Performs animation for the first text substitution of the by-macro
 	 * visualization on the current snap.
 	 * 
 	 * @param codePageId
 	 *            the <code>CodePage</code> the animation is performed on.
 	 * @param fromLineNum
 	 *            the line where the highlighted copy moves from.
 	 * @param fromPos
 	 *            the position where the highlighted copy moves from.
 	 * @param fromStr
 	 *            the actual string to be moved.
 	 * @param toLineNum
 	 *            the line where the highlighted copy moves to.
 	 * @param toPos
 	 *            the position where the highlighted copy moves to.
 	 */
 	public void moveArgs(String codePageId, int fromLineNum, int fromPos,
 			String fromStr, int toLineNum, int toPos) {
 		/*
 		System.out.println();
 		System.out.println();
 		System.out.println();
 		System.out.println();
 		System.out.println();
 		System.out.println();
 		System.out.println();
 		System.out.println();
 		System.out.println("moveArgs call:");
 		System.out.println("\tcodePageId=" + codePageId);
 		System.out.println("\tfromLineNum=" + fromLineNum);
 		System.out.println("\tfromPos=" + fromPos);
 		System.out.println("\tfromStr=" + fromStr);
 		System.out.println("\toLineNum=" + toLineNum);
 		System.out.println("\ttoPos=" + toPos);
 		*/
 		
 		CodePage cp = cpc.get(codePageId);
 		// System.out.println(cp);
 		cp.setCallLineNum(fromLineNum);
 
 		cp.addCopy(fromPos, fromStr);
 
 		actions.offer(new MoveArgCodePageAction(cp, currentSnapNum,
 				fromLineNum, fromPos, toLineNum, toPos, fromStr));
 	}
 
 	/**
 	 * Performs a swap of two <code>CodePage</code>s on the current snap. This
 	 * version can only be used right after a <code>moveArgs</code> call.
 	 * 
 	 * @param prevCodePageId
 	 *            the id of the <code>CodePage</code> to be hidden.
 	 * @param newCodePageId
 	 *            the id of the <code>CodePage</code> to be shown.
 	 */
 	public void swapCodePage(String prevCodePageId, String newCodePageId) {
 		CodePage cp = cpc.get(prevCodePageId);
 		CodePage newCP = cpc.get(newCodePageId);
 
 		actions.offer(new SwapCodePageAction(cp, newCP, currentSnapNum));
 	}
 
 	/**
 	 * Performs a swap of two <code>CodePage</code>s on the current snap.
 	 * 
 	 * @param prevCodePageId
 	 *            the id of the <code>CodePage</code> to be hidden.
 	 * @param newCodePageId
 	 *            the id of the <code>CodePage</code> to be shown.
 	 * @param scopeReplace
 	 *            use true if this is right after a
 	 *            <code>replaceWithScope</code>, false if right after a
 	 *            <code>moveArgs</code> call. A bit of a hack to say the least.
 	 */
 	public void swapCodePage(String prevCodePageId, String newCodePageId,
 			boolean scopeReplace) {
 		CodePage cp = cpc.get(prevCodePageId);
 		CodePage newCP = cpc.get(newCodePageId);
 
 		actions.offer(new SwapCodePageAction(cp, newCP, scopeReplace,
 				currentSnapNum));
 	}
 
 	/**
 	 * Displays a <code>CodePage</code> for viewing on the current snap.
 	 * 
 	 * @param codePageId
 	 *            the id of the <code>CodePage</code> to be shown.
 	 * @return always returns true.
 	 */
 	public boolean showCodePage(String codePageId) {
 		actions.offer(new ShowHideCodePageAction(true, cpc.get(codePageId),
 				currentSnapNum));
 		return true;
 	}
 
 	/**
 	 * Hides a <code>CodePage</code> from view on the current snap.
 	 * 
 	 * @param codePageId
 	 *            codePageId the id of the <code>CodePage</code> to be hidden.
 	 * @return always returns true.
 	 */
 	public boolean hideCodePage(String codePageId) {
 		actions.offer(new ShowHideCodePageAction(false, cpc.get(codePageId),
 				currentSnapNum));
 		return true;
 	}
 
 	/**
 	 * Performs animation for the second text substitution of the by-macro
 	 * visualization.<br>
 	 * <br>
 	 * The params would be as marked for the following program:
 	 * 
 	 * <pre>
 	 * def foo(x, y)
 	 * startScopeLNum: 		{
 	 * 							x = 2;
 	 * 							y = x;
 	 * 							.
 	 * 							.
 	 * 							.
 	 * 							y = x + 2;
 	 * endScopeLNum 			}
 	 * 						def main()
 	 *  						{
 	 * callLineNum:				foo(a, b);
 	 * endOfMainBrktLNum: 	}
 	 * </pre>
 	 * 
 	 * @param codePageId
 	 *            the id of the codePage
 	 * @param callLineNum
 	 *            the line number of the function that is being replaced
 	 * @param startScopeLNum
 	 *            the line number at the beginning bracket of "foo"
 	 * @param endScopeLNum
 	 *            the line number at the ending bracket of "foo"
 	 * @param endOfMainBrktLNum
 	 *            the line number at the ending bracket of "main". Not used.
 	 * @return always returns true.
 	 */
 	public boolean replaceWithScope(String codePageId, int callLineNum,
 			int startScopeLNum, int endScopeLNum, int endOfMainBrktLNum) {
 
 		actions.offer(new ScopeReplaceCodePageAction(cpc.get(codePageId),
 				currentSnapNum, callLineNum, startScopeLNum, endScopeLNum,
 				endOfMainBrktLNum));
 		return true;
 	}
 
 	/**
 	 * Add a scope to the visualization. Also adds its parameters. Assumes that
 	 * the local symbol table has only the parameters, nothing else.
 	 * 
 	 * @param symbols
 	 *            <code>SymbolTable</code> containing all the parameters of the
 	 *            scope.
 	 * @param name
 	 *            name of function usually. If the scope has no name just make
 	 *            something up that's unique.
 	 * @param parent
 	 *            the parent scope of the scope being added.
 	 */
 	public void addScope(Interpreter.SymbolTable symbols, String name,
 			String parent) {
 		boolean isGlobal = parent == null;
 
 		Scope retScope = new Scope(name, scopeColors.pop(), isGlobal);
 
 		scopes.put(name, retScope);
 
 		if (isGlobal) {
 			globalScope = retScope;
 		} else {
 			scopes.get(parent).addScope(retScope);
 		}
 
 		retScope.setHidden(true);
 
 		if (!name.equals("Global") && Global.InterpreterType != Interpreter.InterpreterTypes.BY_NAME) // global's not in the function table so
 		// don't try to find it
 		{
 			Interpreter.ASTFunction func = Global.getFunction(name);
 			if (func != null) {
 				ArrayList<String> params = func.getParameters();
 
 				for (String p : params) {
 					Interpreter.Variable iv = symbols.getVariable(p);
 					Variable v = null;
 					try
 					{
 						if (iv instanceof Interpreter.ByCopyRestoreVariable) {
 							v = new Variable(p, null, iv.getValue(), true);
 							v.setIsCopyRestore();
 						} else if (iv instanceof Interpreter.ByRefVariable) {
 							v = new Variable(p, -255, true);
 							v.setIsReference(true);
 						} else {
 							v = new Variable(p, symbols.get(p), true);
 						}
 					}
 					catch (Exception e)
 					{
 						System.out.println(e);
 					}
 					retScope.addVariable(v);
 
 					// add a copy of the original
 					v.addCopy();
 
 					varToVar.put(iv.getUUID(), v);
 				}
 			}
 		}
 	}
 	
 	public void addScope(Interpreter.SymbolTable symbols, String name,
 			String parent, boolean cached) {
 		boolean isGlobal = parent == null;
 
 		Scope retScope = new Scope(name, scopeColors.pop(), isGlobal, cached);
 
 		scopes.put(name, retScope);
 
 		if (isGlobal) {
 			globalScope = retScope;
 		} else {
 			scopes.get(parent).addScope(retScope);
 		}
 
 		retScope.setHidden(true);
 
 		if (!name.equals("Global") && Global.InterpreterType != Interpreter.InterpreterTypes.BY_NAME) // global's not in the function table so
 		// don't try to find it
 		{
 			Interpreter.ASTFunction func = Global.getFunction(name);
 			if (func != null) {
 				ArrayList<String> params = func.getParameters();
 
 				for (String p : params) {
 					Interpreter.Variable iv = symbols.getVariable(p);
 					Variable v = null;
 					try
 					{
 						if (iv instanceof Interpreter.ByCopyRestoreVariable) {
 							v = new Variable(p, null, iv.getValue(), true);
 							v.setIsCopyRestore();
 						} else if (iv instanceof Interpreter.ByRefVariable) {
 							v = new Variable(p, -255, true);
 							v.setIsReference(true);
 						} else {
 							v = new Variable(p, symbols.get(p), true);
 						}
 					}
 					catch (Exception e)
 					{
 						System.out.println(e);
 					}
 					retScope.addVariable(v);
 
 					// add a copy of the original
 					v.addCopy();
 
 					varToVar.put(iv.getUUID(), v);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds a variable to the visualization.
 	 * 
 	 * @param var
 	 *            the <code>Interpreter.Variable</code> to be displayed.
 	 * @param varName
 	 *            the name of the variable.
 	 * @param scope
 	 *            the name of the scope containing the variable.
 	 */
 	public void addVariable(Interpreter.Variable var, String varName,
 			String scope)
 	{
 				Variable v = null;
 		try
 		{
 			if (var.getIsArray()) 
 			{
 				v = new Array(varName, var.getValues(), false);
 				Array vArray = (Array) v;
 	
 				for (int i = 0; i < var.getValues().size(); i++) 
 				{
 					vArray.addCopy(i);
 				}
 			}
 			else
 			{
 				v = new Variable(varName, var.getValue(), false);
 			}
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 
 		v.addCopy();
 
 		// addCopy of the original value
 
 		// setVarValue(v, var.getValue());
 
 		varToVar.put(var.getUUID(), v);
 /*
 		for (String key : scopes.keySet()) {
 			// System.out.println(key);
 		}*/
 		scopes.get(scope).addVariable(v);
 	}
 
 	/**
 	 * Adds a reference from the <code>src</code> variable to the
 	 * <code>dest</code>. A way to create a reference variable.
 	 * 
 	 * @param src
 	 *            the reference variable.
 	 * @param dest
 	 *            the variable being referenced.
 	 */
 	public void addVariableReference(Interpreter.Variable src,
 			Interpreter.Variable dest) {
 		Variable v1 = varToVar.get(src.getUUID());
 		Variable v2 = varToVar.get(dest.getUUID());
 
 		if (v1 == null || v2 == null) {
 			System.out.println("Bad variable");
 			return;
 		}
 
 		v1.setReference(v2);
 
 	}
 
 	/**
 	 * Adds a reference from the <code>src</code> variable to the
 	 * <code>index</code> of <code>dest</code>. A way to create a reference
 	 * variable to an array element.
 	 * 
 	 * @param src
 	 *            the reference variable.
 	 * @param dest
 	 *            the array variable containing the index being referenced.
 	 * @param index
 	 *            the index being referenced.
 	 */
 	public void addVariableReference(Interpreter.Variable src,
 			Interpreter.Variable dest, int index) {
 		Variable v1 = varToVar.get(src.getUUID());
 		Variable v2 = varToVar.get(dest.getUUID());
 
 		if (v1 == null || v2 == null) {
 			System.out.println("Bad variable");
 			return;
 		}
 
 		v1.setReference(v2, index);
 
 	}
 	
 	/**
 	 * It's your job to make sure the var makes sense in the scope
 	 * @param var
 	 * @param scope
 	 */
 	public void addVariableToCache(String name, int value, String scope)
 	{
 		Scope s = scopes.get(scope);
 		//Variable v = varToVar.get(var.getUUID());
 		//This didn't work, v was always null
 		Variable newVar = new Variable(name.replace("_", ""), value, true);
 		newVar.setHidden(true);
 		newVar.addCopy();
 		s.addVariableToCache(newVar);
 		
 		actions.offer(new ShowHideVarAction(true, newVar, currentSnapNum));
 	}
 	
 	/**
 	 * It's your job to make sure the var makes sense in the scope
 	 * @param var
 	 * @param index
 	 * @param scope
 	 */
 	public void addVariableToCache(Interpreter.Variable var, int index, String scope)
 	{
 		Scope s = scopes.get(scope);
 		Variable v = varToVar.get(var.getUUID());
 		Variable newVar = new Variable(v);
 		Array a = (Array) v;
 		
 		newVar.setName(v.name + "[" + index + "]");
 		newVar.setValue(a.getValue(index));
 		
 		s.addVariableToCache(newVar);
 		
 		
 		actions.offer(new ShowHideVarAction(true, newVar, currentSnapNum));
 	}
 
 	// TODO: check if you're actually on a slide
 	/**
 	 * Displays a <code>Scope</code> on the current slide.
 	 * 
 	 * @param s
 	 *            the id of the <code>Scope</code> to be shown.
 	 */
 	public void showScope(String s) {
 		actions.offer(new ShowHideScopeAction(true, s, currentSnapNum));
 	}
 
 	// TODO: check if you're actually on a slide
 	/**
 	 * Hides a <code>Scope</code> on the current slide.
 	 * 
 	 * @param s
 	 *            the id of the <code>Scope</code> to be hidden.
 	 */
 	public void hideScope(String s) {
 		actions.offer(new ShowHideScopeAction(false, s, currentSnapNum));
 	}
 
 	// TODO: check if you're actually on a slide
 	/**
 	 * Displays a <code>Variable</code> on the current slide.
 	 * 
 	 * @param var
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            <code>Variable</code> to be shown.
 	 */
 	public void showVar(Interpreter.Variable var) {
 
 		Variable v = varToVar.get(var.getUUID());
 		actions.offer(new ShowHideVarAction(true, v, currentSnapNum));
 	}
 
 	// TODO: check if you're actually on a slide
 	/**
 	 * Hides a <code>Variable</code> on the current slide.
 	 * 
 	 * @param var
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            <code>Variable</code> to be hidden.
 	 */
 	public void hideVar(Interpreter.Variable var) {
 		Variable v = varToVar.get(var.getUUID());
 		actions.offer(new ShowHideVarAction(false, v, currentSnapNum));
 	}
 	
 	public void highlightVarByName(Interpreter.Variable iv)
 	{
 		createByNameActionIfNeeded();
 		Variable v = varToVar.get(iv.getUUID());
 		//actions.offer(new HighlightVarAction(v, scripter.getIndexOfPar(), currentSnapNum));
 		v.createHighlight();
 		callByNameAction.addHighlightVar(v);
 	}
 	
 	public void highlightVarByName(Interpreter.Variable iv, int index)
 	{
 		createByNameActionIfNeeded();
 		Variable v = varToVar.get(iv.getUUID());
 		
 		
 		Array a = (Array)v;
 		a.createHighlightRect(index);
 		callByNameAction.addHighlightVar(v, index);
 	}
 	
 	public void highlightScopeByName(String scope)
 	{
 		
 		createByNameActionIfNeeded();
 		
 		scopes.get(scope).createHighlight();
 		callByNameAction.addHighlightScope(scope);
 	}
 	
 	public void greyScope(String scope)
 	{
 		createByNameActionIfNeeded();
 		scopes.get(scope).createFaded();
 		callByNameAction.addFadedScope(scope);
 	}
 	
 	public void modifyVarByName(Interpreter.Variable iv, int newValue)
 	{
 	
 			createByNameActionIfNeeded();
 			Variable v = varToVar.get(iv.getUUID());
 			v.setValue(newValue);
 	
 			v.addCopy();
 			
 			callByNameAction.setModifiedVar(v);
 			callByNameAction.setValue(newValue);
 		
 	
 	}
 	
 	public void modifyVarByName(Interpreter.Variable iv, int index, int newValue)
 	{
 		
 		
 			createByNameActionIfNeeded();
 			Variable v = varToVar.get(iv.getUUID());
 			Array vArray = (Array) v;
 	
 			vArray.setElem(index, newValue);
 	
 			vArray.addCopy(index);
 			
 			callByNameAction.setModifiedVar(v, index);
 			callByNameAction.setValue(newValue);
 		
 	}
 	
 	private void createByNameActionIfNeeded()
 	{
 		if (callByNameAction == null)
 		{
 			callByNameAction = new CallByNameAction(currentSnapNum);
 		}
 	}
 
 	/**
 	 * Starts a new snapshot for actions to take place.
 	 * 
 	 * @param lineNum
 	 *            the line number corresponds to the snapshot in the
 	 *            visualization.
 	 * @return true if the snap was started, false if something went wrong.
 	 */
 	public boolean startSnap(int lineNum) {
 		return startSnap(lineNum, null);
 	}
 
 	/**
 	 * Starts a new snapshot for actions to take place.
 	 * 
 	 * @param lineNum
 	 *            the line number corresponds to the snapshot in the
 	 *            visualization.
 	 * @param pseudocode
 	 *            array containing the lines of pseudocode that correspond to
 	 *            this slide. Only needed if the contents of the pseudocode pane
 	 *            is changed.
 	 * @return true if the snap was started, false if something went wrong.
 	 */
 	public boolean startSnap(int lineNum, String[] pseudocode) 
 	{
 		return startSnap(lineNum, pseudocode, false);
 	}
 	
 	/**
 	 * Starts a new snapshot for actions to take place.
 	 * 
 	 * @param lineNum the line number corresponds to the snapshot in the
 	 *            visualization.
 	 * @param pseudocode array containing the lines of pseudocode that correspond to
 	 *            this slide. Only needed if the contents of the pseudocode pane
 	 *            is changed.
 	 * @param neverReplace whether this pseudocode line will ever be replaced.
 	 * @return true if the snap was started, false if something went wrong.
 	 */
 	public boolean startSnap(int lineNum, String[] pseudocode, boolean neverReplace) 
 	{
 
 		if (currentSnapNum > 0)
 			return false;
 		try 
 		{
 			currentSnapNum = scripter.startSlide();
 			
 			if (pseudocode != null)
 			{
 				if (neverReplace) // its the preview pseudocode
 				{
 					previewPseudo = pseudoCode;
 				}
 				else // its normal pseudocode
 				{
 					this.pseudoCode = pseudocode;
 					if (snapRegularPseudoStartsAt == -1) // we haven't set the normal pseudocode yet
 						snapRegularPseudoStartsAt = currentSnapNum;
 				}
 			}
 			
 			
 			
 			lineToHighlightOnSnap.put(new Integer(currentSnapNum), lineNum);
 			//lineToHighlight = lineNum;
 		} 
 		catch (SlideException e) 
 		{
 			return false;
 		}
 		
 		return true;
 		
 	}
 	
 	public void modifyPseudocodeOnAll(String[] pseudocode)
 	{
 		pseudoCode = pseudocode;
 	}
 
 	/**
 	 * ends the current snapshot.
 	 * 
 	 * @return true if the snap was ended, false if something went wrong.
 	 */
 	public boolean endSnap() {
 
 		if (currentSnapNum < 0)
 			return false;
 
 		try 
 		{
 			
 			scripter.endSlide();
 		} 
 		catch (SlideException e) 
 		{
 			return false;
 		}
 		
 		if (callByNameAction != null)
 			actions.offer(callByNameAction);
 		
 		callByNameAction = null;
 		//lineToHighlight = -1;
 		currentSnapNum = -1;
 		return true;
 	}
 
 	/**
 	 * starts a parallel section inside the current snapshot. You need one in
 	 * each snap.
 	 * 
 	 * @return true if the par was started, false if something went wrong.
 	 */
 	public boolean startPar() {
 		if (currentSnapNum < 0)
 			return false;
 
 		try {
 			scripter.startPar();
 		} catch (Exception e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * ends the current par section.
 	 * 
 	 * @return true if the par was ended, false if something went wrong.
 	 */
 	public boolean endPar() {
 		if (currentSnapNum < 0)
 			return false;
 
 		try {
 			scripter.endPar();
 		} catch (Exception e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * adds a question to the slide BEFORE the current snapshot. Don't call this while
 	 * you're on the first slide; bad things could happen.
 	 * 
 	 * @param q
 	 *            the <code>Question</code> to add to the previous snapshot.
 	 * @return true if added, false otherwise.
 	 */
 	public boolean addQuestion(Question q) {
 		if (currentSnapNum < 0)
 			return false;
 
 		q.setSlideId(currentSnapNum - 1);
 
 		questions.add(q);
 
 		return true;
 	}
 	
 	/**
 	 * Adds a question to the current slide or the previous one, dependent on the value of
 	 * <code>addToCurrentSlide</code>.
 	 * @param q the <code>Question</code> to add to the previous snapshot.
 	 * @param addToCurrentSlide true if you want to add the question to the current slide, 
 	 * 			false if you want to add the question to the previous slide
 	 * @return true if added, false otherwise.
 	 */
 	public boolean addQuestion(Question q, boolean addToCurrentSlide)
 	{
 		if (currentSnapNum < 0)
 			return false;
 		if (addToCurrentSlide)
 		{
 			q.setSlideId(currentSnapNum);
 
 			questions.add(q);
 		}
 		else
 		{
 			return addQuestion(q);
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Animates the passing of the value of <code>Variable</code> to a
 	 * parameter.
 	 * 
 	 * @param from
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            <code>Variable</code> to get the value from.
 	 * @param to
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            <code>Variable</code> to send the value to.
 	 * @return true of added, false otherwise.
 	 */
 	public boolean moveValue(Interpreter.Variable from, Interpreter.Variable to) {
 		if (currentSnapNum < 0)
 			return false;
 
 		Variable fromVar = varToVar.get(from.getUUID());
 		Variable toVar = varToVar.get(to.getUUID());
 
 		// add a copy of the currentValue to fromVar
 		fromVar.addCopy();
 		actions.offer(new MoveVarAction(fromVar, toVar, currentSnapNum));
 
 		toVar.setValue(fromVar.getValue());
 
 		return true;
 	}
 
 	/**
 	 * Animates the passing of the value of <code>fromIndex</code> in
 	 * <code>Variable</code> to a parameter.
 	 * 
 	 * @param from
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            array <code>Variable</code> containing the index to get the
 	 *            value from.
 	 * @param fromIndex
 	 *            the index in <code>from</code> containing the value.
 	 * @param to
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            <code>Variable</code> to send the value to.
 	 * @return true of added, false otherwise.
 	 */
 	public boolean moveValue(Interpreter.Variable from, int fromIndex,
 			Interpreter.Variable to) {
 		if (currentSnapNum < 0)
 			return false;
 
 		Variable fromVar = varToVar.get(from.getUUID());
 		Variable toVar = varToVar.get(to.getUUID());
 
 		Array fromArray = (Array) fromVar;
 		// add a copy of the currentValue to fromVar[fromIndex
 		fromArray.addCopy(fromIndex);
 		actions.offer(new MoveVarIndexAction(fromVar, fromIndex, toVar,
 				currentSnapNum));
 
 		toVar.setValue(fromArray.getValue(fromIndex));
 
 		return true;
 	}
 
 	/**
 	 * Animates the passing of the value of a parameter <code>Variable</code>
 	 * back to the <code>toIndex</code> in <code>to</code>. Used in
 	 * copy-restore.
 	 * 
 	 * @param from
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            parameter <code>Variable</code> to get the value from.
 	 * @param to
 	 *            an <code>Interpreter.Variable</code> that corresponds with an
 	 *            array <code>Variable</code>.
 	 * @param toIndex
 	 *            the index in <code>to</code> the value should be moved to.
 	 * @return true if added, false otherwise.
 	 */
 	public boolean moveValue(Interpreter.Variable from,
 			Interpreter.Variable to, int toIndex) {
 		if (currentSnapNum < 0)
 			return false;
 
 		Variable fromVar = varToVar.get(from.getUUID());
 		Variable toVar = varToVar.get(to.getUUID());
 
 		fromVar.addCopy();
 
 		actions.offer(new MoveVarToIndexAction(fromVar, toVar, toIndex,
 				currentSnapNum));
 
 		Array toArray = (Array) toVar;
 		toArray.setElem(toIndex, fromVar.getValue());
 
 		return true;
 	}
 
 	/**
 	 * Modifies the value of a <code>Variable</code>.
 	 * 
 	 * @param iv
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            <code>Variable</code> to be modified.
 	 * @param newValue
 	 *            the new value of the <code>Variable</code>.
 	 * @return true if added, false otherwise.
 	 */
 	public boolean modifyVar(Interpreter.Variable iv, int newValue) {
 		if (currentSnapNum < 0)
 			return false;
 
 		Variable v = varToVar.get(iv.getUUID());
 		v.setValue(newValue);
 
 		v.addCopy();
 
 		actions.offer(new ModifyVarAction(newValue, v, currentSnapNum));
 		return true;
 
 	}
 
 	/**
 	 * Modifies the value at <code>newIndex</code> in an array
 	 * <code>Variable</code>.
 	 * 
 	 * @param iv
 	 *            an <code>Interpreter.Variable</code> that corresponds with the
 	 *            array <code>Variable</code> containing the index to be
 	 *            modified.
 	 * @param index
 	 *            index whose value should be modified.
 	 * @param newValue
 	 *            the new value of <code>index</code> in the array
 	 *            <code>Variable</code>.
 	 * @return true if added, false otherwise.
 	 */
 	public boolean modifyVar(Interpreter.Variable iv, int index, int newValue) {
 		if (currentSnapNum < 0)
 			return false;
 
 		Variable v = varToVar.get(iv.getUUID());
 		Array vArray = (Array) v;
 
 		vArray.setElem(index, newValue);
 
 		vArray.addCopy(index);
 
 		actions.offer(new ModifyVarIndexAction(newValue, v, index,
 				currentSnapNum));
 
 		return true;
 	}
 	
 	/**
 	 * Used to highlight the scope of variables being highlighted in the pseudocode.<br><br>
 	 * NOTE: You must call this method on longer variables before smaller methods. 
 	 * 	Ex: 'a[x]' before 'x' 
 	 * @param varStr the string to look for in the pseudocode.
 	 * @param scope the name of the scope that the variable named <code>varStr</code> belongs to.
 	 * @param lineToStart the line to begin to look for <code>varStr</code> for highlighting.
 	 * @param lineToEnd the line to begin to look for <code>varStr</code> for highlighting.
 	 */
 	public void highlightStrInPseudo(String varStr, String scope, int lineToStart, int lineToEnd)
 	{
 		int startIndex = lineToStart - 1;
 		int endIndex = lineToEnd - 1;
 		
 		Pattern var = Pattern.compile(".*var.*");
 		Pattern foundVar = Pattern.compile(".*(" + varStr + ").*");
 		
 		for (int i = startIndex; i <= endIndex; i++)
 		{
 			String temp = pseudoCode[i];
 			Matcher m = var.matcher(temp);
 			if (!m.find()) // if you don't find var, we check if the var is not in it 
 			{
 				Matcher foundVarM = foundVar.matcher(temp);
 				
 				while(foundVarM.find())
 				for (int j = 0; j < foundVarM.groupCount(); i++)
 				{
 					foundVarM.group(j);
 				}
 			}
 		}
 		
 		
 	}
 
 	/**
 	 * Writes the entities and action performed on those entities to the script.
 	 * The second phase of XAALConnector.
 	 * 
 	 * @param filename
 	 *            the name of the file where the script will be written to.
 	 */
 	public void draw(String filename) {
 
 		// first calls draw on the global scope which then draws all of the
 		// children
 		globalScope.draw(scripter);
 		// System.out.println("Drew global scope");
 
 		cpc.draw(scripter);
 		// System.out.println("Drew code pages");
 
 		
 		//write out pseudocode to each snap
 		for (Integer i : lineToHighlightOnSnap.keySet())
 		{
 			PseudoSerializer pseudo = null;
 			scripter.reopenSlide(i);
 			try {
 				int lineToHighlight = lineToHighlightOnSnap.get(i);
 				if (i < snapRegularPseudoStartsAt)// do preview code
 				{
 					pseudo = new PseudoSerializer(previewPseudo, title);
 					scripter.addPseudocodeUrl(pseudo.toPseudoPage(lineToHighlight, true));
 				}
 				else // its not preview, use normal
 				{
 					pseudo = new PseudoSerializer(pseudoCode, title);
 					scripter.addPseudocodeUrl(pseudo.toPseudoPage(lineToHighlight));
 				}
 				
 				
 			} catch (SlideException e) {
 				
 				e.printStackTrace();
 			}
 			scripter.recloseSlide();
 		}
 		
 		// perform and write future actions to the scripter
 		FutureAction action = null;
 		do {
 			action = actions.poll();
 			if (action == null)
 				break;
 
 			if (action instanceof VarAction) // its a variable
 			{
 				if (action instanceof ShowHideVarAction)// its a show or hide
 				// action
 				{
 					if (((ShowHideVarAction) action).isShow()) // its a show
 					// action
 					{
 						writeVarShow((ShowHideVarAction) action);
 					} else // its a hide action
 					{
 						writeVarHide((ShowHideVarAction) action);
 					}
 				} 
 				else if (action instanceof MoveVarAction) // this is a
 				// movement from one
 				// var to another
 				{
 					if (action instanceof MoveVarIndexAction) {
 						writeIndexMove((MoveVarIndexAction) action);
 					} else if (action instanceof MoveVarToIndexAction) {
 						writeIndexToMove((MoveVarToIndexAction) action);
 					} else {
 						writeMove((MoveVarAction) action);
 					}
 				}
 				
 				else if (action instanceof ModifyVarAction)// a variable is being set by a constant
 				{
 					if (action instanceof ModifyVarIndexAction) {
 						writeIndexModify((ModifyVarIndexAction) action);
 					} else {
 						writeVarModify((ModifyVarAction) action);
 					}
 				}
 				else if(action instanceof HighlightVarAction)
 				{
 					if (action instanceof HighlightVarIndexAction)
 					{
 						writeHighlightVarIndex((HighlightVarIndexAction)action);
 					}
 					else
 					{
 						writeHighlightVar((HighlightVarAction)action);
 					
 					}
 				}
 			} 
 			else if (action instanceof ScopeAction)// its a scope
 			{
 				if (action instanceof ShowHideScopeAction) // its a show or hide
 				// action
 				{
 					if (((ShowHideScopeAction) action).isShow())// its a show
 					// action
 					{
 						writeScopeShow((ShowHideScopeAction) action);
 					} else// its a hide action
 					{
 						writeScopeHide((ShowHideScopeAction) action);
 					}
 				}
 				else if (action instanceof HighlightScopeAction)
 				{
 					writeHighlightScope((HighlightScopeAction)action);
 				}
 				else if (action instanceof GreyScopeAction)
 				{
 					writeGreyScope((GreyScopeAction)action);
 				}
 			}
 			
 			else if (action instanceof CallByNameAction)
 			{
 				writeCallByName((CallByNameAction)action);
 			}
 			else // its a CodePageAction
 			{
 				if (action instanceof MoveArgCodePageAction) {
 					// System.out.println("Action: " + action);
 					writeMoveArgCodePage((MoveArgCodePageAction) action);
 				} else if (action instanceof ShowHideCodePageAction) {
 					if (((ShowHideCodePageAction) action).isShow()) // its a
 					// show
 					// action
 					{
 						writeCodePageShow((ShowHideCodePageAction) action);
 					} else // its a hide action
 					{
 						writeCodePageHide((ShowHideCodePageAction) action);
 					}
 				} else if (action instanceof SwapCodePageAction) {
 					writeSwapCodePage((SwapCodePageAction) action);
 				} else if (action instanceof ScopeReplaceCodePageAction) {
 					writeReplaceWithScopeCodePage((ScopeReplaceCodePageAction) action);
 				}
 
 			}
 
 		} while (true);
 
 		// write out all the questions
 
 		for (Question q : questions) {
 			q.draw(scripter);
 		}
 
 		// write to the file
 		FileWriter writer;
 		try {
 			writer = new FileWriter(filename);
 
 			writer.write(scripter.toString());
 
 			writer.close();
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Writes the variable move to the script.<br>
 	 * <br>
 	 * A move consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopening the slide
 	 * 1.5 reopen par
 	 * 2. getting a copy1 from the first variable.
 	 * 2.5 get a newCopy from the first Variable.
 	 * 3. performing a show on newCopy.
 	 * 3.25 change color of newCopy
 	 * 3.5 give ownership of newCopy back to the first variable.
 	 * 4. getting a copy from the second variable.
 	 * 5. hiding the copy from the second variable.
 	 * 6. perform the move
 	 * 7. give ownership to second variable.
 	 * 8. setting the value of the second variable to the new value.
 	 * 9. reclose par
 	 * 9.5 reclose slide
 	 * 10. reopen next slide
 	 * 10.5 reopen next par
 	 * 11. turn off highlighting of copy1
 	 * 12. reclose par
 	 * 12.5 reclose slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the MoveVarAction containing the information needed.
 	 */
 	private void writeMove(MoveVarAction action) {
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Variable from = action.getFrom();
 			Variable to = action.getTo();
 
 			// get copy for the first variable
 			String copy1 = from.popCopyId();
 
 			// get a new copy from the first variable.
 			String newCopy = from.popCopyId();
 
 			// show newCopy
 			scripter.addShow(newCopy);
 
 			// color newCopy
 			scripter.addChangeStyle(highlightColor, copy1);
 			from.receiveCopyOwnership(newCopy);
 
 			// get copy from second variable
 			String copy2 = to.popCopyId();
 
 			// hide copy2
 			scripter.addHide(copy2);
 			scripter.reclosePar();
 			// perform the move!!!
 
 			scripter.startPar();
 			int startX = from.getXPos();
 			int startY = from.getYPos();
 
 			if (from.getIsCopyRestore()) // you've gotta take it from right box
 			{
 				startX = from.getRightXPosCR();
 			}
 
 			int endX = to.getXPos();
 			int endY = to.getYPos();
 
 			if (to.getIsCopyRestore()) // you've gotta take it from right box
 			{
 				endX = to.getRightXPosCR();
 			}
 
 			int moveX = startX - endX;
 			int moveY = startY - endY;
 
 			scripter.addTranslate(-moveX, -moveY, copy1);
 
 			// give ownership of copy1 to second variable.
 			to.receiveCopyOwnership(copy1);
 
 			// set the value of 'to' to from's value
 			to.setValue(from.getValue());
 
 			// reclose the par
 			scripter.endPar();
 			// reclose the slide
 			scripter.recloseSlide();
 			// turn off highlighting on next slide
 			scripter.reopenSlide(action.getSnapNum() + 1);
 			scripter.reopenPar();
 
 			scripter.addChangeStyle("black", copy1);
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		} catch (Exception e) {
 
 		}
 
 		// after this method completes every variable's value must equal the
 		// head of
 		// its copiesOwned queue
 	}
 
 	/**
 	 * Writes the move FROM the index of an array to the script.<br>
 	 * <br>
 	 * An index move consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopening the slide
 	 * 1.5 reopen par
 	 * 2. getting a copy1 from the first variable at index.
 	 * 2.5 get a newCopy from the first Variable at index.
 	 * 3. performing a show on newCopy.
 	 * 3.25 highlight copy1
 	 * 3.5 give ownership of newCopy back to the first variable at index.
 	 * 4. getting a copy from the second variable.
 	 * 5. hiding the copy from the second variable.
 	 * 6. perform the move
 	 * 7. give ownership to second variable.
 	 * 8. setting the value of the second variable to the new value.
 	 * 9. reclose par
 	 * 9.5 reclose slide
 	 * 10. reopen next slide
 	 * 10.5 reopen next par
 	 * 11. unhighlight copy1
 	 * 12. reclose par
 	 * 12.5 reclose slide.
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the MoveVarIndexAction containing the information needed.
 	 */
 	private void writeIndexMove(MoveVarIndexAction action) {
 		try {
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Array from = (Array) action.getFrom();
 			Variable to = action.getTo();
 			int fromIndex = action.getIndex();
 
 			// get copy for the first variable
 			String copy1 = from.popCopyId(fromIndex);
 
 			// get a new copy from the first variable.
 			String newCopy = from.popCopyId(fromIndex);
 
 			// show newCopy
 			scripter.addShow(newCopy);
 
 			scripter.addChangeStyle(highlightColor, copy1);
 			from.receiveCopyOwnership(newCopy, fromIndex);
 
 			// get copy from second variable
 			String copy2 = to.popCopyId();
 
 			// hide copy2
 			scripter.addHide(copy2);
 			scripter.reclosePar();
 			// perform the move!!!
 
 			boolean parExists = false;
 			parExists = scripter.reopenPar(1);
 
 			if (!parExists)
 				scripter.startPar();
 
 			int startX = from.getXPos(fromIndex);
 			int startY = from.getYPos();
 
 			int endX = to.getXPos();
 			int endY = to.getYPos();
 
 			if (to.getIsCopyRestore()) // you've gotta take it from right box
 			{
 				endX = to.getRightXPosCR();
 			}
 
 			int moveX = startX - endX;
 			int moveY = startY - endY;
 
 			scripter.addTranslate(-moveX, -moveY, copy1);
 
 			// give ownership of copy1 to second variable.
 			to.receiveCopyOwnership(copy1);
 
 			// set the value of 'to' to from's value
 			to.setValue(from.getValue(fromIndex));
 
 			// reclose the par
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 			// reclose the slide
 			scripter.recloseSlide();
 			// turn off highlighting on next slide
 			scripter.reopenSlide(action.getSnapNum() + 1);
 			scripter.reopenPar();
 
 			scripter.addChangeStyle("black", copy1);
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 
 		} catch (Exception e) {
 			System.out.println("this is bad");
 		}
 	}
 
 	/**
 	 * Writes the move TO the index of an array to the script.
 	 * 
 	 * @param action
 	 *            the MoveVarToIndexAction containing the information needed.
 	 */
 	private void writeIndexToMove(MoveVarToIndexAction action) {
 		try {
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Variable from = action.getFrom();
 			Array to = (Array) action.getTo();
 			int toIndex = action.getIndex();
 
 			// get copy for the first variable
 			String copy1 = from.popCopyId();
 
 			// get a new copy from the first variable.
 			String newCopy = from.peekCopyId();
 
 			// show newCopy
 			scripter.addShow(newCopy);
 			scripter.addChangeStyle(highlightColor, copy1);
 
 			// get copy from second variable
 			String copy2 = to.popCopyId(toIndex);
 
 			// hide copy2
 			scripter.addHide(copy2);
 			scripter.reclosePar();
 
 			boolean parExists = false;
 			parExists = scripter.reopenPar(1);
 
 			if (!parExists)
 				scripter.startPar();
 			int startX = from.getXPos();
 			int startY = from.getYPos();
 
 			if (from.getIsCopyRestore()) // you've gotta take it from right box
 			{
 				startX = from.getRightXPosCR();
 			}
 
 			int endX = to.getXPos(toIndex);
 			int endY = to.getYPos();
 
 			int moveX = startX - endX;
 			int moveY = startY - endY;
 
 			scripter.addTranslate(-moveX, -moveY, copy1);
 
 			// give ownership of copy1 to second variable.
 			to.receiveCopyOwnership(copy1, toIndex);
 
 			// set the value of 'to' to from's value
 			to.setElem(toIndex, from.getValue());
 
 			// reclose the par
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 			// reclose the slide
 			scripter.recloseSlide();
 			// turn off highlighting on next slide
 			scripter.reopenSlide(action.getSnapNum() + 1);
 			scripter.reopenPar();
 
 			scripter.addChangeStyle("black", copy1);
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		} catch (Exception e) {
 			System.out.println("bad");
 		}
 	}
 
 	/**
 	 * Writes the modification of a variable to the script.<br>
 	 * <br>
 	 * A modify consists of(may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopening the slide
 	 * 1.5 reopen par
 	 * 2. pop the copy of the currentValue
 	 * 3. hide this copy
 	 * 4. pop the copy of the newValue
 	 * 5. show the new copy
 	 * 6. give ownership of this copy BACK to the variable (its a hack)
 	 * 7. set the value of the variable to its new value
 	 * 8. reclose the par
 	 * 8.5 reclose the slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the ModifyVarAction containing the information needed.
 	 */
 	private void writeVarModify(ModifyVarAction action) {
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Variable v = action.getTo();
 
 			// pop copy of current value
 			String oldCopy = v.popCopyId();
 
 			// hide oldCopy
 			scripter.addHide(oldCopy);
 
 			// pop copy of new value
 			String newCopy = v.popCopyId();
 
 			// show new copy
 			scripter.addShow(newCopy);
 
 			// highlight the change
 			scripter.addChangeStyle(highlightColor, newCopy);
 
 			// give ownership of newCopy back to variable
 			v.receiveCopyOwnership(newCopy);
 
 			// set the value of variable to its new value
 			v.setValue(action.getNewValue());
 
 			// reclose the par
 			scripter.reclosePar();
 			// reclose the slide
 			scripter.recloseSlide();
 
 			// turn off highlighting on next slide
 
 			scripter.reopenSlide(action.getSnapNum() + 1);
 			scripter.reopenPar();
 
 			scripter.addChangeStyle("black", newCopy);
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		} catch (Exception e) {
 			// we're in trouble
 		}
 
 	}
 
 	/**
 	 * Writes the modification of an index of a variable to the script.<br>
 	 * <br>
 	 * an index modify consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopening the slide
 	 * 1.5 reopen par
 	 * 2. pop the copy of the currentValue at index
 	 * 3. hide this copy
 	 * 4. pop the copy of the newValue at index
 	 * 5. show the new copy
 	 * 6. give ownership of this copy BACK to the variable (its a hack) at index
 	 * 7. set the value of the variable at index to its new value
 	 * 8. reclose the par
 	 * 8.5 reclose the slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the ModifyVarIndexAction containing the information needed.
 	 */
 	private void writeIndexModify(ModifyVarIndexAction action) {
 		// System.out.println("Writing index modify");
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			int toIndex = action.getIndex();
 			// System.out.println("Modifying index " + toIndex);
 			Array v = (Array) action.getTo();
 
 			// pop copy of current value
 			String oldCopy = v.popCopyId(toIndex);
 			// System.out.println("Current value = " + oldCopy);
 			// hide oldCopy
 			scripter.addHide(oldCopy);
 
 			// pop copy of new value
 			String newCopy = v.getCopyId(toIndex);
 
 			// System.out.println("New value " + newCopy);
 			// show new copy
 			scripter.addShow(newCopy);
 
 			// highlight the change
 			scripter.addChangeStyle(highlightColor, newCopy);
 
 			// give ownership of newCopy back to variable
 			// v.receiveCopyOwnership(newCopy);
 
 			// set the value of variable to its new value
 			v.setElem(toIndex, action.getNewValue());
 
 			// reclose the par
 			scripter.reclosePar();
 			// reclose the slide
 			scripter.recloseSlide();
 
 			// turn off highlighting on next slide
 
 			scripter.reopenSlide(action.getSnapNum() + 1);
 			scripter.reopenPar();
 
 			scripter.addChangeStyle("black", newCopy);
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		} catch (Exception e) {
 			// we're in trouble
 		}
 	}
 
 	/**
 	 * Writes the displaying of a variable to the script.<br>
 	 * <br>
 	 * A variable show consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopening the slide
 	 * 1.5 reopen par
 	 * ... show all the ids
 	 * 2. pop the copy of current value from the variable
 	 * 3. show the value.
 	 * 4. give ownership of this copy BACK to the variable (its a HACK)
 	 * 5. reclose par
 	 * 5.5 reclose slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the ShowHideVarAction containing the information needed.
 	 */
 	private void writeVarShow(ShowHideVarAction action) {
 		// System.out.println("Showing a var");
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Variable v = action.getTo();
 
 			// show all the ids
 			ArrayList<String> ids = v.getIds();
 			for (String id : ids) {
 				// System.out.println("Showing id: " + id);
 				try {
 					scripter.addShow(id);
 				} catch (Exception e) {
 					System.out.println(e);
 				}
 			}
 			if (v instanceof Array) {
 				Array vArray = (Array) v;
 				for (int i = 0; i < vArray.getValues().size(); i++) {
 					String copy = vArray.peekCopyId(i);
 					// System.out.println("Showing: " + copy + " on slide "
 					// + action.getSnapNum());
 					scripter.addShow(copy);
 
 					scripter.addChangeStyle(highlightColor, copy);
 
 				}
 
 				scripter.reclosePar();
 				// reclose the slide
 				scripter.recloseSlide();
 				
 				scripter.reopenSlide(action.getSnapNum() + 1);
				scripter.reclosePar();
 				for (int i = 0; i < vArray.getValues().size(); i++) 
 				{
 					String copy = vArray.peekCopyId(i);
 					
 					scripter.addChangeStyle("black", copy);
 				}
				scripter.reopenPar();
 				scripter.recloseSlide();
 				
 				
 				
 				
 			} else {
 				// pop copy of current value
 				String copy = v.popCopyId();
 
 				// show copy
 				scripter.addShow(copy);
 
 				scripter.addChangeStyle(highlightColor, copy);
 
 				// give ownership of the copy back
 				v.receiveCopyOwnership(copy);
 
 				// reclose the par
 				scripter.reclosePar();
 				// reclose the slide
 				scripter.recloseSlide();
 
 				// turn off highlighting on the next slide.
 				scripter.reopenSlide(action.getSnapNum() + 1);
 				scripter.reclosePar();
 
 				scripter.addChangeStyle("black", copy);
 
 				scripter.reopenPar();
 				scripter.recloseSlide();
 			}
 		} catch (Exception e) {
 			// we're in trouble
 		}
 	}
 
 	/**
 	 * Writes the hiding of a variable to the script.<br>
 	 * <br>
 	 * A variable hide consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopening the slide
 	 * 1.5 reopen par
 	 * ... hide all the ids
 	 * 2. pop the copy of current value from the variable
 	 * 3. hide the value.
 	 * 4. give ownership of this copy BACK to the variable (its a HACK)
 	 * 5. reclose par
 	 * 5.5 reclose slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the ShowHideVarAction containing the information needed.
 	 */
 	private void writeVarHide(ShowHideVarAction action) {
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Variable v = action.getTo();
 
 			// hide all the ids
 			ArrayList<String> ids = v.getIds();
 			for (String id : ids) {
 				try {
 					scripter.addHide(id);
 				} catch (Exception e) {
 					System.out.println(e);
 				}
 			}
 
 			// pop copy of current value
 			String copy = v.popCopyId();
 
 			// hide copy
 			scripter.addHide(copy);
 
 			// give ownership of the copy back
 			v.receiveCopyOwnership(copy);
 
 			// reclose the par
 			scripter.reclosePar();
 			// reclose the slide
 			scripter.recloseSlide();
 		} catch (Exception e) {
 			// we're in trouble
 		}
 	}
 
 	// TODO: make sure that all the params and values are shown correctly, its
 	// possible they might not be
 	/**
 	 * Writes the showing of a scope to the script.<br>
 	 * <br>
 	 * A scope show consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopen the slide
 	 * 1.5. reopen the par
 	 * 2. show all the ids
 	 * 3. loop through the params as follows:
 	 *     4. show all of the params ids
 	 *     5. pop a copy of the params value
 	 *     6. show the copy
 	 *     7. give ownership of the copy back to the param HACK
 	 * 8. reclose the par
 	 * 8.5 reclose the slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the ShowHideScopeAction containing the information needed.
 	 */
 	private void writeScopeShow(ShowHideScopeAction action) {
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			Scope scope = scopes.get(action.getScope());
 
 			ArrayList<String> scopeIds = scope.getIds();
 			// show all the ids
 			for (String id : scopeIds) {
 				scripter.addShow(id);
 			}
 
 			ArrayList<Variable> params = scope.getParams();
 
 			// loop through the params
 			for (Variable param : params) {
 				ArrayList<String> ids = param.getIds();
 				// show all param's ids
 				for (String id : ids) {
 					scripter.addShow(id);
 				}
 
 				if (!param.getIsReference()) {
 					// pop a copy of param's value
 					String copy = param.popCopyId();
 
 					// show the copy
 					scripter.addShow(copy);
 
 					// give ownership of copy back to param
 					param.receiveCopyOwnership(copy);
 				}
 			}
 			
 			/*ByNeedCache c = scope.getCache();
 			
 			if (c != null)
 			{
 				for (String s : c.getIds())
 				{
 					scripter.addShow(s);
 				}
 			}*/
 
 			// reclose par
 			scripter.reclosePar();
 
 			// reclose slide
 			scripter.recloseSlide();
 		} catch (Exception e) {
 
 		}
 	}
 
 	// TODO: make sure that all the params and values are shown correctly,
 	// its possible they might not be
 	/**
 	 * Writes the hiding of a scope to the script.<br>
 	 * <br>
 	 * A scope show consists of (may not be up to date!):
 	 * 
 	 * <pre>
 	 * 1. reopen the slide
 	 * 1.5. reopen the par
 	 * 2. hide all the ids
 	 * 3. loop through the params as follows:
 	 *     4. hide all of the params ids
 	 *     5. pop a copy of the params value
 	 *     6. hide the copy
 	 *     7. give ownership of the copy back to the param HACK
 	 * 8. reclose the par
 	 * 8.5 reclose the slide
 	 * </pre>
 	 * 
 	 * @param action
 	 *            the ShowHideScopeAction containing the information needed.
 	 */
 	private void writeScopeHide(ShowHideScopeAction action) {
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 
 			// reopen par
 			scripter.reopenPar();
 
 			
 			Scope scope = scopes.get(action.getScope());
 			ArrayList<String> scopeIds = scope.getIds();
 			// show all the ids
 			// System.out.println("TTT" + scopeIds.size());
 			for (String id : scopeIds) {
 				// System.out.println("QAQAQ" + id);
 				scripter.addHide(id);
 			}
 
 			ArrayList<Variable> params = scope.getParams();
 
 			// loop through the params
 			for (Variable param : params) {
 				ArrayList<String> ids = param.getIds();
 				// hide all param's ids
 				for (String id : ids) {
 					scripter.addHide(id);
 				}
 				if (!param.getIsReference()) {
 					// pop a copy of param's value
 					String copy = param.popCopyId();
 
 					// hide the copy
 					scripter.addHide(copy);
 
 					// give ownership of copy back to param
 					param.receiveCopyOwnership(copy);
 				}
 			}
 
 			ArrayList<Variable> locals = scope.getLocalVariables();
 
 			// loop through the locals
 			for (Variable local : locals) {
 				for (String id : local.getIds()) {
 					scripter.addHide(id);
 				}
 
 				if (local instanceof Array) {
 					Array a = (Array) local;
 					int arrayLength = a.getValues().size();
 					for (int i = 0; i < arrayLength; i++) {
 						scripter.addHide(a.peekCopyId(i));
 					}
 				} else {
 					scripter.addHide(local.peekCopyId());
 				}
 			}
 			
 			
 			
 			ArrayList<Variable> cache = scope.getCache();
 			
 			if (cache != null)
 			{
 				for (Variable v : cache)
 				{
 					ArrayList<String> vIds = v.getIds();
 					for (String id : vIds)
 					{
 						scripter.addHide(id);
 					}
 					
 					scripter.addHide(v.peekCopyId());
 				}
 			}
 
 			// reclose par
 			scripter.reclosePar();
 
 			// reclose slide
 			scripter.recloseSlide();
 		} catch (Exception e) {
 			// System.out.println(e);
 		}
 	}
 
 	/**
 	 * Writes the showing of a code page to the script.
 	 * 
 	 * @param action
 	 *            the ShowHideCodePageAction containing the information needed.
 	 */
 	private void writeCodePageShow(ShowHideCodePageAction action) {
 		try {
 			scripter.reopenSlide(action.getSnapNum());
 			scripter.reopenPar(0);
 			CodePage p = action.getCP();
 
 			for (String id : p.getIds()) {
 				scripter.addShow(id);
 			}
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		} catch (Exception e) {
 
 		}
 	}
 
 	/**
 	 * Writes the hiding of a code page to the script.
 	 * 
 	 * @param action
 	 *            the ShowHideCodePageAction containing the information needed.
 	 */
 	private void writeCodePageHide(ShowHideCodePageAction action) {
 		try {
 
 			scripter.reopenSlide(action.getSnapNum());
 			scripter.reopenPar(0);
 			CodePage p = action.getCP();
 
 			for (String id : p.getIds()) {
 				scripter.addHide(id);
 			}
 
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		} catch (Exception e) {
 
 		}
 	}
 
 	/**
 	 * Writes the moving of the arguments for the first text substitution for
 	 * by-macro.
 	 * 
 	 * @param action
 	 *            the MoveArgCodePageAction containing the information needed.
 	 */
 	private void writeMoveArgCodePage(MoveArgCodePageAction action) {
 		try {
 			scripter.reopenSlide(action.getSnapNum());
 			scripter.reopenPar();
 			CodePage cp = action.getCP();
 
 			// System.out.println(action.getCP());
 			// System.out.println("Moving arg");
 			// show a copy
 			// System.out.println(cp);
 			String id = cp.popCopy(action.getFromPos());
 			// System.out.println(id);
 			scripter.addShow(id);
 			scripter.addChangeStyle(highlightColor, id);
 			scripter.reclosePar();
 
 			// do the move!!!
 			boolean parExists = false;
 			parExists = scripter.reopenPar(1);
 			// System.out.println(1);
 			if (!parExists) {
 				scripter.startPar();
 			}
 			// System.out.println(2);
 			int startX = cp.x + cp.fromPosX[action.getFromPos()];
 			int startY = cp.y
 					+ (cp.getLineHeight() * (action.getFromLine() - 3));
 
 			int endX = cp.x + cp.toPosX[action.getToPos()];
 			int endY = cp.y + (cp.getLineHeight() * (action.getToLine() - 3));
 
 			int moveX = startX - endX;
 			int moveY = startY - endY;
 			// System.out.println(3);
 			scripter.addTranslate(-moveX, -moveY, id);
 
 			// System.out.println("Added a translate");
 			cp.receiveCopyOwnership(id);
 
 			// reclose the par
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 
 			parExists = false;
 
 			parExists = scripter.reopenPar(2);
 			if (!parExists) {
 				scripter.startPar();
 			}
 			scripter.addHide(id);
 			if (parExists) {
 				scripter.reclosePar();
 			} else {
 				scripter.endPar();
 			}
 
 			// reclose the slide
 			scripter.recloseSlide();
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 
 	/**
 	 * Writes the moving of the scope for the second text substitution for
 	 * by-macro
 	 * 
 	 * @param action
 	 *            the ScopeReplaceCodePageAction containing the information
 	 *            needed.
 	 */
 	private void writeReplaceWithScopeCodePage(ScopeReplaceCodePageAction action) {
 		try {
 			scripter.reopenSlide(action.getSnapNum());
 			scripter.reopenPar();
 			CodePage cp = action.getCP();
 
 			ArrayList<String> lineToXaal;
 			lineToXaal = cp.getLineToXaalId();
 
 			// move scope to left
 			int startLnIndex = action.getStartScopeLNum() - 1;
 			int endLnIndex = action.getEndScopeLNum() - 1;
 			int funcLnIndex = startLnIndex - 1;
 			int callLnIndex = action.getCallLineNum() - 1;
 			int mainBeginIndex = endLnIndex + 1;
 			int mainBefCallIndex = callLnIndex - 1;
 
 			// write a move for all of the scope lines
 			for (int i = startLnIndex; i <= endLnIndex; i++) {
 				int startX = cp.x;
 				int startY = cp.y + (cp.getLineHeight() * i);
 
 				int endX = cp.x + dxOnScopeReplace;
 				int endY = startY;
 
 				int moveX = startX - endX;
 				int moveY = startY - endY;
 
 				scripter.addTranslate(-moveX, -moveY, lineToXaal.get(i));
 			}
 
 			scripter.reclosePar();
 
 			// do all the hiding necessary
 			boolean parExists = false;
 			parExists = scripter.reopenPar(1);
 			if (!parExists)
 				scripter.startPar();
 
 			// hide the call line
 			scripter.addHide(lineToXaal.get(callLnIndex));
 
 			// hide the function line
 			scripter.addHide(lineToXaal.get(funcLnIndex));
 
 			// reclose the par
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 
 			// do the moving of main
 			parExists = false;
 			parExists = scripter.reopenPar(2);
 			if (!parExists)
 				scripter.startPar();
 
 			// write a move for all of the lines of main
 			for (int i = mainBeginIndex; i <= mainBefCallIndex; i++) {
 				int startX = cp.x;
 				int startY = cp.y + (cp.getLineHeight() * i);
 
 				int endX = startX;
 				int endY = cp.y + (cp.getLineHeight() * funcLnIndex)
 						+ (cp.getLineHeight() * (i - mainBeginIndex));
 				int moveX = startX - endX;
 				int moveY = startY - endY;
 
 				scripter.addTranslate(-moveX, -moveY, lineToXaal.get(i));
 			}
 
 			// reclose the par
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 
 			// move the new scope into position!
 			parExists = false;
 			parExists = scripter.reopenPar(3);
 			if (!parExists)
 				scripter.startPar();
 
 			// write a move for all of the scope lines into position
 			for (int i = startLnIndex; i <= endLnIndex; i++) {
 				int startX = cp.x + dxOnScopeReplace;
 				;
 				int startY = cp.y + (cp.getLineHeight() * i);
 
 				int endX = cp.x;
 				int endY = cp.y
 						+ (cp.getLineHeight() * funcLnIndex)
 						+ (cp.getLineHeight() * (mainBefCallIndex - mainBeginIndex))
 						+ (cp.getLineHeight() * 1)
 						+ (cp.getLineHeight() * (i - startLnIndex));
 
 				int moveX = startX - endX;
 				int moveY = startY - endY;
 
 				scripter.addTranslate(-moveX, -moveY, lineToXaal.get(i));
 			}
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 
 			scripter.recloseSlide();
 		} 
 		catch (Exception e) {
 		}
 	}
 
 	/**
 	 * Writes the swapping of a two CodePages to the script.
 	 * 
 	 * @param action
 	 *            the SwapCodePageAction containing the information needed.
 	 */
 	private void writeSwapCodePage(SwapCodePageAction action) {
 		try {
 			scripter.reopenSlide(action.getSnapNum());
 
 			boolean parExists = false;
 			if (action.getReplaceScope()) {
 				parExists = scripter.reopenPar(4);
 				if (!parExists) {
 					scripter.startPar();
 				}
 			} else {
 				parExists = scripter.reopenPar(2);
 				if (!parExists) {
 					scripter.startPar();
 				}
 			}
 
 			CodePage p = action.getCP();
 
 			for (String id : p.getIds()) {
 				scripter.addHide(id);
 			}
 
 			CodePage newCP = action.getNewCP();
 
 			for (String id : newCP.getIds()) {
 				scripter.addShow(id);
 			}
 
 			if (parExists)
 				scripter.reclosePar();
 			else
 				scripter.endPar();
 
 			scripter.recloseSlide();
 		} 
 		catch (Exception e) 
 		{
 		}
 	}
 
 	
 	private void writeCallByName(CallByNameAction action)
 	{
 		try 
 		{
 			Queue<Variable> highlightVars = action.getHighlightVars();
 			Queue<Integer> highlightVarIndexes = action.getHighlightVarIndexes();
 			Queue<String> fadedScopes = action.getFadedScopes();
 			Queue<String> highlightScopes = action.getHighlightScopes();
 			Variable modifyVar = action.getModifiedVar();
 			int modifyVarIndex = action.getModifiedVarIndex();
 			int newValue = action.getValue();
 			int snapNum = action.getSnapNum();
 			
 			
 			scripter.reopenSlide(snapNum);
 			String varHighId = null;
 			String scopeFadedId = null;
 			String scopeHighId = null;
 			
 			int i = 0;
 			for(i = 0; true; i += 6)
 			{
 				
 				Variable tempVar = highlightVars.poll();
 				
 				String tempFaded = fadedScopes.poll();
 				String tempHighScope = highlightScopes.poll();
 				
 				if (tempVar == null)
 					break;
 				int tempVarIndex = highlightVarIndexes.poll().intValue();
 				boolean parExists = reopenOrCreatePar(i);
 				
 				//do faded scope
 				Scope fadedScope = scopes.get(tempFaded);
 				scopeFadedId = fadedScope.getFadedId();
 				scripter.addShow(scopeFadedId);
 				
 				recloseOrEndPar(parExists);
 				
 				//do a quick pause
 				parExists = reopenOrCreatePar(i+1);
 				scripter.addPause(1500);
 				recloseOrEndPar(parExists);
 				
 				parExists = reopenOrCreatePar(i+2);
 				varHighId = null;
 				if (tempVarIndex > -1)
 				{
 					Array a= (Array)tempVar;
 					varHighId = a.getHighlightRectId(tempVarIndex);
 					scripter.addShow(varHighId);
 				}
 				else
 				{
 					varHighId = tempVar.getHighlightId();
 					scripter.addShow(varHighId);
 				}
 				//do highlight scope
 				Scope highScope = scopes.get(tempHighScope);
 				scopeHighId = highScope.getHighlightId();
 				scripter.addShow(scopeHighId);
 				
 				recloseOrEndPar(parExists);
 				
 				//we need to add the pause
 				parExists = reopenOrCreatePar(i+3);
 				scripter.addPause(2000);
 				recloseOrEndPar(parExists);
 				
 				if ( highlightVars.peek() == null)
 				{
 					i +=4;
 					break;
 				}
 				//turn off highlighting
 				parExists = reopenOrCreatePar(i+4);
 				scripter.addHide(varHighId);
 				scripter.addHide(scopeHighId);
 				scripter.addHide(scopeFadedId);
 				//scripter.add
 				recloseOrEndPar(parExists);
 				
 
 				parExists = reopenOrCreatePar(i+5);
 
 				scripter.addPause(2000);
 
 				recloseOrEndPar(parExists);
 			}
 			
 			
 			
 			
 			boolean parExists = reopenOrCreatePar(i);
 			
 			if (modifyVarIndex > -1) // its an array
 			{
 				writeModifyVarIndexInternal(modifyVar, modifyVarIndex, newValue);
 			}
 			else // its not an array!
 			{
 				writeModifyVarInternal(modifyVar, newValue);
 			}
 			
 			recloseOrEndPar(parExists);
 			
 			parExists = reopenOrCreatePar(i + 1);
 			
 			scripter.addPause(1500);
 			
 			recloseOrEndPar(parExists);
 			
 			parExists = reopenOrCreatePar(i + 2);
 		
 			if (varHighId != null)
 				scripter.addHide(varHighId);
 			
 			if (scopeHighId != null)
 				scripter.addHide(scopeHighId);
 			
 			if (scopeFadedId != null)
 				scripter.addHide(scopeFadedId);
 			
 			recloseOrEndPar(parExists);
 			scripter.recloseSlide();
 			
 			scripter.reopenSlide(snapNum + 1);
 			scripter.reopenPar(0);
 			
 			if (modifyVarIndex > -1) // its an array
 			{
 				writeModVarIndexUnhighInt(modifyVar, modifyVarIndex);
 			}
 			else // its not an array!
 			{
 				writeModVarUnhighInt(modifyVar);
 			}
 			scripter.reclosePar();
 			scripter.recloseSlide();
 		}
 		catch (XAALScripterException e)
 		{
 			System.out.println();
 		}
 		
 	}
 	
 	private void writeModifyVarInternal(Variable v, int newValue) throws XAALScripterException
 	{
 		// pop copy of current value
 		String oldCopy = v.popCopyId();
 
 		// hide oldCopy
 		scripter.addHide(oldCopy);
 		
 		String newCopy = v.peekCopyId();
 
 		// show new copy
 		scripter.addShow(newCopy);
 
 		// highlight the change
 		scripter.addChangeStyle(highlightColor, newCopy);
 		
 		// set the value of variable to its new value
 		v.setValue(newValue);
 	}
 	
 	private void writeModVarUnhighInt(Variable v) throws XAALScripterException
 	{
 		String newCopy = v.peekCopyId();
 		scripter.addChangeStyle("black", newCopy);
 	}
 	
 	private void writeModifyVarIndexInternal (Variable var, int index, int newValue) throws XAALScripterException
 	{
 		Array v = (Array) var;
 
 		// pop copy of current value
 		String oldCopy = v.popCopyId(index);
 		// System.out.println("Current value = " + oldCopy);
 		// hide oldCopy
 		scripter.addHide(oldCopy);
 
 		// pop copy of new value
 		String newCopy = v.getCopyId(index);
 		
 		scripter.addShow(newCopy);
 
 		// highlight the change
 		scripter.addChangeStyle(highlightColor, newCopy);
 
 		// set the value of variable to its new value
 		v.setElem(index, newValue);
 	}
 	
 	private void writeModVarIndexUnhighInt(Variable var, int index) throws XAALScripterException
 	{
 		Array v = (Array)var;
 		
 		String newCopy = v.getCopyId(index);
 		scripter.addChangeStyle("black", newCopy);
 	}
 	
 	/**
 	 * ASSUMES THERE WILL BE A PAUSE INBETWEEN THIS PAR AND THE NEXT
 	 * @param action
 	 */
 	private void writeHighlightScope(HighlightScopeAction action)
 	{
 		
 			Scope scope = scopes.get(action.getScope());
 			
 			String id = scope.getRectId();
 			
 			writeHighlightOnOff(id, action.getPar(), action.getSnapNum());
 		
 	}
 	
 	/**
 	 * ASSUMES THERE WILL BE A PAUSE INBETWEEN THIS PAR AND THE NEXT
 	 * @param action
 	 */
 	private void writeGreyScope(GreyScopeAction action)
 	{
 		try
 		{
 			scripter.reopenSlide(action.getSnapNum());
 			scripter.reopenPar(action.getPar());
 			
 			Scope scope = scopes.get(action.getScope());
 			
 			String id = scope.getRectId();
 			
 			scripter.changeFillColorSafe("grey", id);
 			
 			scripter.reclosePar();
 			
 			scripter.reopenPar(action.getPar()+ 2);
 			
 			scripter.changeFillColorSafe("white", id);
 			
 			scripter.reclosePar();
 			
 			scripter.recloseSlide();
 		}
 		catch (XAALScripterException e)
 		{
 			
 		}
 	}
 	
 	/**
 	 * ASSUMES THERE WILL BE A PAUSE INBETWEEN THIS PAR AND THE NEXT
 	 * @param action
 	 */
 	private void writeHighlightVar(HighlightVarAction action)
 	{
 		
 			Variable v = action.getTo();
 			
 			String id = v.getRectId();
 			
 		writeHighlightOnOff(id, action.getPar(), action.getSnapNum());
 	}
 	
 	/**
 	 * ASSUMES THERE WILL BE A PAUSE INBETWEEN THIS PAR AND THE NEXT
 	 * @param action
 	 */
 	private void writeHighlightVarIndex(HighlightVarIndexAction action)
 	{
 		Variable v = action.getTo();
 		Array a = (Array)v;
 		String id = a.getRect(action.getIndex());
 		
 		writeHighlightOnOff(id, action.getPar(), action.getSnapNum());
 	}
 	
 	private void writeHighlightOnOff(String id, int parNum, int snapNum)
 	{
 		try
 		{
 			scripter.reopenSlide(snapNum);
 			scripter.reopenPar(parNum);
 		
 			scripter.changeStyleSafe(StrokeType.solid, 3, id);
 			
 			scripter.reclosePar();
 			
 			scripter.reopenPar(parNum+ 2);
 			
 			scripter.changeStyleSafe(StrokeType.solid, 1, id);
 			
 			scripter.reclosePar();
 			
 			scripter.recloseSlide();
 		}
 		catch (XAALScripterException e)
 		{
 			
 		}
 	}
 	
 	/**
 	 * true if its a reopen, false if it was created
 	 * @param index
 	 * @return
 	 */
 	private boolean reopenOrCreatePar(int index)
 	{
 		boolean parExists = false;
 		parExists = scripter.reopenPar(index);
 		if (!parExists)
 		{
 			try {
 				scripter.startPar();
 				
 			} catch (XAALScripterException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return parExists;
 
 	}
 	
 	private void recloseOrEndPar(boolean oldPar)
 	{
 		if (oldPar)
 			scripter.reclosePar();
 		else
 			try {
 				scripter.endPar();
 			} catch (ParException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 }
