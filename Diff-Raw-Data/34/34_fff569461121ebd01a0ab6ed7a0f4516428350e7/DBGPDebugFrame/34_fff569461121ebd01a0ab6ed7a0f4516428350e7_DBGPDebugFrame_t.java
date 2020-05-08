 package org.eclipse.dltk.rhino.dbgp;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.NativeJavaArray;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
 import org.mozilla.javascript.debug.DebugFrame;
 import org.mozilla.javascript.debug.DebuggableScript;
 import org.mozilla.javascript.debug.Debugger;
 
 public final class DBGPDebugFrame implements DebugFrame {
 
 	private final String sourceName;
 	private final DBGPStackManager stackManager;
 	private int lineNumber;
 	private String where;
 	private Scriptable thisObj;
 	private Scriptable scope;
 	private DebuggableScript script;
 	private Object[] args;
 	private Context context;
 	private boolean suspend;
 
 	public boolean isSuspend() {
 		return suspend;
 	}
 
 	public void setSuspend(boolean suspend) {
 		this.suspend = suspend;
 	}
 
 	public DBGPDebugFrame(Context ct, DebuggableScript node) {
 		sourceName = node.getSourceName();
 		this.context = ct;
 		stackManager = DBGPStackManager.getManager(ct);
 		where = node.getFunctionName();
 		this.script = node;
 		if (where == null) {
 			where = "module";
 		}
 
 	}
 
 	public String[] getParametersAndVars() {
 		String[] result = new String[script.getParamAndVarCount()];
 		for (int a = 0; a < result.length; a++) {
 			result[a] = script.getParamOrVarName(a);
 		}
 		return result;
 	}
 
 	public void onEnter(Context cx, Scriptable activation, Scriptable thisObj,
 			Object[] args) {
 		this.args = args;
 		this.scope = activation;
 
 		this.thisObj = thisObj;
 		stackManager.enter(this);
 	}
 
 	public void onExceptionThrown(Context cx, Throwable ex) {
 		stackManager.exceptionThrown(ex);
 	}
 
 	public void onExit(Context cx, boolean byThrow, Object resultOrException) {
 		
 		stackManager.exit(this);
 	}
 
 	public void onLineChange(Context cx, int lineNumber) {		
 		this.lineNumber = lineNumber;
 		stackManager.changeLine(this, lineNumber);
 	}
 
 	public String getSourceName() {
 		return sourceName;
 	}
 
 	public int getLineNumber() {
 		return lineNumber;
 	}
 
 	public void setLineNumber(int lineNumber) {
 		this.lineNumber = lineNumber;
 	}
 
 	public String getWhere() {
 		return where;
 	}
 
 	public Object getValue(int num) {
 		Object object = scope.get(script.getParamOrVarName(num), thisObj);
 		return object;
 	}
 
 	public Scriptable getThis() {
 		return thisObj;
 	}
 
 	public void setValue(String name, String value) {
 		if (name.startsWith("this.")) {
 			name = name.substring("this.".length());
 			thisObj.put(name, thisObj, eval( value));
 		}
 		else
 		scope.put(name, scope, value);
 	}
 
 	public Object eval(String value) {
 		Debugger debugger = context.getDebugger();
 
 		try {
 			context.setDebugger(null, null);
 			Scriptable cs = scope;
 			if (value.startsWith("this.")) {
 
 				value = value.substring("this.".length());
 				cs = thisObj;
 			}
 			Object evaluateString = context.evaluateString(cs, value, "eval",
 					0, null);
 			return evaluateString;
 
 		} catch (Throwable e) {
 			return "Error during evaluation:" + e.getMessage();
 		} finally {
 
 			context.setDebugger(debugger, null);
 		}
 	}
 
 	public Object getValue(String longName) {
 		if (longName.startsWith("this")){
 			int indexOf = longName.indexOf('.');
 			if (indexOf==-1)return thisObj;
 			longName=longName.substring("this.".length());
 			return getProperty(thisObj,longName);
 		}
 		return getProperty(scope, longName);		
 	}
 
 	private Object getProperty(Scriptable obj, String longName) {
 		int k=longName.indexOf('.');
 		if (k==-1)return shortGet(obj, longName);
 		String shortName=longName.substring(0,k);
 		String sm=longName.substring(k+1);
 		Object property = shortGet(obj, shortName);
 		if (property instanceof Scriptable){
 			return getProperty((Scriptable) property, sm);
 		}
 		return null;
 	}
 
 	private Object shortGet(Scriptable obj, String longName) {
 		if (obj instanceof NativeJavaArray){
 			int parseInt = Integer.parseInt(longName);
 			NativeJavaArray na=(NativeJavaArray) obj;
 			return na.get(parseInt, na);
 		}
		Scriptable parent = obj;
		while(parent != null)
		{
			Object o = ScriptableObject.getProperty(parent, longName);
			if (o != null && o != Scriptable.NOT_FOUND ) return o;
			parent = parent.getParentScope();
				
		}
		return null;
 	}
 }
