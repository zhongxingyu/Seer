 package sabazios.wala;
 
 import sabazios.util.FlexibleContext;
 import sabazios.util.U;
 
 import com.ibm.wala.classLoader.CallSiteReference;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.Context;
 import com.ibm.wala.ipa.callgraph.ContextKey;
 import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
 import com.ibm.wala.ipa.callgraph.impl.Everywhere;
 import com.ibm.wala.ipa.callgraph.propagation.ContainerUtil;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
 import com.ibm.wala.ipa.cha.IClassHierarchy;
 import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
 
 public final class CS extends nCFAContextSelector {
 
 	// public static final ContextKey NODE = new
 	// FlexibleContext.NamedContextKey("NODE");
 //	public static final ContextKey ELEMENT_VALUE = new FlexibleContext.NamedContextKey("ELEMENT_VALUE");
 	public static final ContextKey MAIN_ITERATION = new FlexibleContext.NamedContextKey("MAIN_ITERATION");
 	public static final ContextKey PARALLEL = new FlexibleContext.NamedContextKey("PARALLEL");
 	public static final ContextKey ARRAY = new FlexibleContext.NamedContextKey("ARR");
 	public static final ContextKey OPERATOR_CALL_SITE_REFERENCE = new FlexibleContext.NamedContextKey("OP_CALL_SITE");
 	public static final ContextKey OPERATOR_CALLER = new FlexibleContext.NamedContextKey("OP_CALLER");
 	public static final ContextKey RECEIVER_INSTANCE = new FlexibleContext.NamedContextKey("RECEIVER_INSTANCE");
 	public static final ContextKey EXTRA_CONTEXT = new FlexibleContext.NamedContextKey("EXTRA_CONTEXT");
 	public static int NCFA = 1;
 
 	ZeroXInstanceKeys keyFactory;
 
 	public CS(ZeroXInstanceKeys keyFactory, AnalysisOptions options, IClassHierarchy cha) {
 		super(NCFA, new DefaultContextSelector(options, cha));
 		this.keyFactory = keyFactory;
 	}
 
 	@Override
 	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] receiver) {
 
 		String calleeString = callee.toString();
 		Context context = caller.getContext();
 		if (calleeString.contains("replaceWith") || calleeString.contains("apply(Lextra166y")) {
 			System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			c.putItem(ARRAY, receiver[0]);
 			c.putItem(OPERATOR_CALL_SITE_REFERENCE, site);
 			c.putItem(OPERATOR_CALLER, caller);
 			// System.out.println(c);
 			return c;
 		}
 
 		String callerMethod = caller.getMethod().toString();
 		if (callerMethod.contains("replaceWithGeneratedValueSeq") && calleeString.contains("op")) {
 			// System.out.println(callerMethod);
 			// System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
 			c.putItem(MAIN_ITERATION, invoke.getDef() == 5);
 			c.putItem(PARALLEL, false);
 			// System.out.println(c);
 			return c;
 		}
 
 		if (callerMethod.contains("replaceWithGeneratedValue") && calleeString.contains("op")) {
 			// System.out.println(callerMethod);
 			// System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
 			c.putItem(MAIN_ITERATION, invoke.getDef() == 5);
 			c.putItem(PARALLEL, true);
 			// System.out.println(c);
 			return c;
 		}
 
 		if (callerMethod.contains("replaceWithMappedIndexSeq") && calleeString.contains("op")) {
 			// System.out.println(callerMethod);
 			// System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
 			c.putItem(MAIN_ITERATION, invoke.getDef() == 6);
 			c.putItem(PARALLEL, false);
 			// System.out.println(c);
 			return c;
 		}
 
 		if (callerMethod.contains("replaceWithMappedIndex") && calleeString.contains("op")) {
 			// System.out.println(callerMethod);
 			// System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
 			c.putItem(MAIN_ITERATION, invoke.getDef() == 6);
 			c.putItem(PARALLEL, true);
 			// System.out.println(c);
 			return c;
 		}
 
 		if (callerMethod.contains("applySeq(Lextra166y/Ops$Procedure;)V") && calleeString.contains("op")) {
 			// System.out.println(callerMethod);
 			// System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
 //			c.putItem(ELEMENT_VALUE, invoke.getUse(1));
 			c.putItem(MAIN_ITERATION, invoke.getUse(1) == 4);
 			c.putItem(PARALLEL, false);
 //			System.out.println(c);
 			return c;
 		}
 		if (callerMethod.contains("apply(Lextra166y/Ops$Procedure;)V") && calleeString.contains("op")) {
 			System.out.println(callerMethod);
 			System.out.println(calleeString);
 			FlexibleContext c = new FlexibleContext(context);
 			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
 			c.putItem(MAIN_ITERATION, invoke.getUse(1) == 4);
 			c.putItem(PARALLEL, true);
 //			System.out.println(c);
 			// System.out.println(invoke.getUse(1));
 			return c;
 		}
 
 		if (!keyFactory.isInteresting(callee.getDeclaringClass()))
 			return Everywhere.EVERYWHERE;
 
 		if (U.inApplicationScope(caller) && U.inPrimordialScope(callee) && isInterestingForUs(callee)) {
 			// FlexibleContext c = new FlexibleContext(context);
 			// c.putItem(RECEIVER_INSTANCE, receiver);
 			// System.out.println("here 1");
 			// return c;
 
 			if (context instanceof FlexibleContext && ((FlexibleContext) context).getItem(RECEIVER_INSTANCE) == null) {
 				FlexibleContext c = new FlexibleContext(context);
 				c.putItem(RECEIVER_INSTANCE, receiver[0]);
 				return c;
 			} else
 				return context;
 		}
 
 		if (callee.toString().contains("createUsingHandoff")) {
 			FlexibleContext c = new FlexibleContext(caller.getContext());
 			c.putItem(EXTRA_CONTEXT, caller.getMethod().toString() + site.toString());
 			return c;
 		}
 
 		if (callee.getReturnType().toString().contains("ParallelArray")) {
 			FlexibleContext c = new FlexibleContext(caller.getContext());
 			c.putItem(EXTRA_CONTEXT, caller.getMethod().toString() + site.toString());
 			return c;
 		}
 
 		// FlexibleContext c = new FlexibleContext();
 		//
 		// if (receiver instanceof AllocationSiteInNode) {
 		// AllocationSiteInNode r = (AllocationSiteInNode) receiver;
 		// c.putItem(RECEIVER_INSTANCE, r.getSite());
 		// return c;
 		// } else
 		// return context;
 
 		// if (NCFA == 0)
 		// return context;
 		// else
 		if (caller.getContext() instanceof FlexibleContext)
 			return context;
 		else
 			return super.getCalleeTarget(caller, site, callee, receiver);
 	}
 
 	private boolean isInterestingForUs(IMethod callee) {
 		return ContainerUtil.isContainer(callee.getDeclaringClass()) || callee.toString().contains("DateFormat");
 	}
 	
 	public static String[] threadSafeMethods = new String[] { 
 		"java/util/regex/Pattern", "java/lang/System, exit",
 		"java/io/PrintStream, ",
 		"java/io/PrintWriter, ",
 		"java/io/StringReader, ",
 		"java/util/Vector, ",
 		"java/lang/Throwable, ",
 		"java/security/AccessControlContext, getDebug", // not relevant
		"java/util/Random, <init>" , "Integer, <init>",
 		"java/lang/SecurityManager, ",
 		"java/lang/ClassLoader, initSystemClassLoader",
 		"java/util/Properties, "
 		};
 	
 	public static boolean threadSafe(CGNode n) {
 		for (String pattern : CS.threadSafeMethods)
 			if (n.getMethod().toString().contains(pattern))
 				return true;
 		return false;
 	}
 }
