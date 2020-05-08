 package org.mutoss;
 
 import java.text.Collator;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mutoss.gui.MuTossGUI;
 import org.mutoss.gui.dialogs.MethodDialog;
 
 public class MethodHandler {
 	
 	private static final Log logger = LogFactory.getLog(MethodHandler.class);
 	
 	static List<String> methodNames;
 	static List<Method> methods;
 	
 	static MethodHandler mh = null;
 	
 	public static void updateMethods() {
 		mh = new MethodHandler();
 	}
 	
 	public static MethodHandler getMethodHandler() {
 		if (mh == null) {
 			mh = new MethodHandler();
 		}
 		return mh;
 	}
 	
 	protected MethodHandler() {
 		methodNames = Arrays.asList(MuTossControl.getInstance().getR().eval("c(apropos(\"^mutoss\"), ls( asNamespace( \"mutoss\" ), pattern=\"mutoss.*\", all = TRUE ))").asRChar().getData());
 		methods = new Vector<Method>();
 		for (String methodname : methodNames) {
			if (MuTossControl.getR().eval("class(try(class(mutoss:::"+methodname+"())==\"MutossMethod\",silent=TRUE))==\"try-error\")").asRLogical().getData()[0]) continue;
			result = MuTossControl.getR().eval("class(mutoss:::"+methodname+"())==\"MutossMethod\"");
			if (result==null || !MuTossControl.getR().eval("class(mutoss:::"+methodname+"())==\"MutossMethod\"").asRLogical().getData()[0]) continue;
 			// TODO Test whether methodNames() gives really back an MuTossMethod object.
 			Method method = new Method(methodname);
 			methods.add(method);
 			logger.info("Added method \""+method.label+"\" from "+method.name+".");
 			
 			Collections.sort(methods, new MethodSorter());
 		}
 		
 	}
 	
 	public List<Method> getAdjustedPValueMethods() {
 		List<Method> pAdjMethods = new Vector<Method>();
 		for (Method m : methods) {
 			if (m.isApplicable() && m.returnsAdjPValues()) {
 				pAdjMethods.add(m);
 			}
 		}
 		return pAdjMethods;
 	}
 	
 	public static Method getMethod(String method) {
 		for (Method m : methods) {
 			if (m.name.equals(method)) return m;
 		}
 		return null;
 	}
 
 	public void apply(String methodname) {
 		Method method = getMethod(methodname);
 		new MethodDialog(MuTossGUI.getGUI(), method);
 	}
 
 	public List<Method> getRejectedMethods() {
 		List<Method> pAdjMethods = new Vector<Method>();
 		for (Method m : methods) {
 			if (m.isApplicable() && m.returnsRejected()) {
 				pAdjMethods.add(m);
 			}
 		}
 		return pAdjMethods;
 	}
 
 	public List<Method> getPValueMethods() {
 		List<Method> pMethods = new Vector<Method>();
 		for (Method m : methods) {
 			if (m.isApplicable() && m.returnsPValues()) {
 				pMethods.add(m);
 			}
 		}
 		return pMethods;
 	}
 
 	public List<Method>  getCIMethods() {
 		List<Method> pMethods = new Vector<Method>();
 		for (Method m : methods) {
 			if (m.isApplicable() && m.returnsCI()) {
 				pMethods.add(m);
 			}
 		}
 		return pMethods;
 	}
 }
