 package org.meta_environment.eclipse.modules;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import nl.cwi.sen1.graph.types.Graph;
 import nl.cwi.sen1.moduleapi.Factory;
 import nl.cwi.sen1.moduleapi.types.AttributeStore;
 import nl.cwi.sen1.moduleapi.types.Dependency;
 import nl.cwi.sen1.moduleapi.types.DependencyList;
 import nl.cwi.sen1.moduleapi.types.ModuleId;
 import nl.cwi.sen1.moduleapi.types.ModuleIdList;
 import toolbus.adapter.eclipse.EclipseTool;
 import aterm.ATerm;
 import aterm.ATermList;
 
 public class ModuleManager extends EclipseTool implements  AttributeSetListener {
 	private static final String TOOL_NAME = "module-manager";
 	
 	private ModuleGraph moduleDB;
 
 	private Factory factory = Factory.getInstance(EclipseTool.factory);
 
 	private static class InstanceKeeper {
 		private static ModuleManager sInstance = new ModuleManager();
 		static {
 			sInstance.connect();
 		}
 	}
 
 	public static ModuleManager getInstance() {
 		return InstanceKeeper.sInstance;
 	}
 	
 	public ModuleManager() {
 		super(TOOL_NAME);
 		moduleDB = new ModuleGraph(EclipseTool.factory, this);
 	}
 
 	public ATerm createModule() {
 		ModuleId moduleId = factory.makeModuleId_ModuleId(moduleDB
 				.getNextModuleId());
 		moduleDB.addModule(new Module(factory), moduleId);
 		return EclipseTool.factory.make("module-id(<term>)", moduleId
 				.toTerm());
 	}
 
 	public ATerm getModuleIdByAttribute(ATerm namespace, ATerm key, ATerm value) {
 		ModuleId moduleId = moduleDB.getModuleIdByAttribute(namespace, key,
 				value);
 
 		if (moduleId == null) {
 			return EclipseTool.factory.parse("no-such-module");
 		}
 
 		return EclipseTool.factory.make("module-id(<term>)", moduleId
 				.toTerm());
 	}
 
 	public ATerm getAllModules() {
 		Set<ModuleId> modules = moduleDB.getAllModules();
 
 		return EclipseTool.factory.make("modules(<list>)",
 				extractATermList(modules));
 	}
 
 	public ATerm getAllAttributes(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 			AttributeStore attributes = moduleDB.getAllAttributes(moduleId);
 
 			return EclipseTool.factory.make("attributes(<term>)", attributes
 					.toTerm());
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 			return EclipseTool.factory.make("no-such-module");
 		}
 	}
 
 	public void deleteModule(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 			moduleDB.removeModule(moduleId);
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 		}
 	}
 
 	public void addAttribute(ATerm id, ATerm namespace, ATerm key, ATerm value) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 			moduleDB.setAttribute(moduleId, namespace, key, value);
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 		}
 	}
 
 	public ATerm getAttribute(ATerm id, ATerm namespace, ATerm key) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 			ATerm value = moduleDB.getModuleAttribute(moduleId, namespace, key);
 
 			if (value == null) {
 				return EclipseTool.factory.parse("no-such-key");
 			}
 
 			return EclipseTool.factory.make("attribute(<term>)", value);
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 			return EclipseTool.factory.parse("no-such-key");
 		}
 	}
 
 	public void deleteAttribute(ATerm id, ATerm namespace, ATerm key) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 
 			moduleDB.deleteModuleAttribute(moduleId, namespace, key);
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 		}
 	}
 
 	public void addDependency(ATerm from, ATerm to) {
 		try {
 			ModuleId moduleFromId = factory.ModuleIdFromTerm(from);
 			ModuleId moduleToId = factory.ModuleIdFromTerm(to);
 
 			moduleDB.addDependency(moduleFromId, moduleToId);
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 		}
 	}
 
 	public ATerm getChildrenModules(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 
 			Set<ModuleId> dependencies = moduleDB.getChildren(moduleId);
 
 			if (dependencies == null) {
 				return EclipseTool.factory.parse("no-such-module");
 			}
 
 			return EclipseTool.factory.make("children-modules(<list>)",
 					extractATermList(dependencies));
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 			return EclipseTool.factory.make("children-modules([])");
 		}
 	}
 
 	public ATerm getAllParentModules(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 
 			Set<ModuleId> dependencies = moduleDB.getAllParents(moduleId);
 
 			if (dependencies == null) {
 				return EclipseTool.factory.parse("no-such-module");
 			}
 
 			return EclipseTool.factory.make("all-parent-modules(<list>)",
 					extractATermList(dependencies));
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 
 			return EclipseTool.factory.make("all-parent-modules([])");
 		}
 	}
 
 	public ATerm getParentModules(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 
 			Set<ModuleId> dependencies = moduleDB.getParents(moduleId);
 
 			if (dependencies == null) {
 				return EclipseTool.factory.parse("no-such-module");
 			}
 
 			return EclipseTool.factory.make("parent-modules(<list>)",
 					extractATermList(dependencies));
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 			return EclipseTool.factory.parse("no-such-module");
 		}
 	}
 
 	public ATerm getAllChildrenModules(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 
 			Set<ModuleId> dependencies = moduleDB.getAllChildren(moduleId);
 
 			if (dependencies == null) {
 				return EclipseTool.factory.parse("no-such-module");
 			}
 
 			return EclipseTool.factory.make("all-children-modules(<list>)",
 					extractATermList(dependencies));
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 			return EclipseTool.factory.parse("no-such-module");
 		}
 	}
 
 	public ATerm getClosableModules(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 
 			Set<ModuleId> closableModules = moduleDB
 					.getClosableModules(moduleId);
 
 			return EclipseTool.factory.make("closable-modules(<list>)",
 					extractATermList(closableModules));
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 			return EclipseTool.factory.make("closable-modules([])");
 		}
 	}
 
 	public ATerm getDependencies() {
 		DependencyList list = factory.makeDependencyList();
 		Map<ModuleId, Set<ModuleId>> dependencies = moduleDB.getDependencies();
 
 		for (Iterator<ModuleId> iter = dependencies.keySet().iterator(); iter
 				.hasNext();) {
 			ModuleId moduleId = iter.next();
 			Set<ModuleId> deps = dependencies.get(moduleId);
 
 			ModuleIdList moduleList = factory.makeModuleIdList();
 			for (Iterator<ModuleId> depsIter = deps.iterator(); depsIter
 					.hasNext();) {
 				moduleList = moduleList.append(depsIter.next());
 			}
 
 			Dependency dependency = factory.makeDependency_Dependency(moduleId,
 					moduleList);
 			list = list.append(dependency);
 		}
 		return EclipseTool.factory.make("dependencies(<term>)", list
 				.toTerm());
 	}
 
 	private ATermList extractATermList(Set<ModuleId> dependencies) {
 		ATermList list = EclipseTool.factory.makeList();
 		for (Iterator<ModuleId> iter = dependencies.iterator(); iter.hasNext();) {
 			list = list.append(iter.next().toTerm());
 		}
 		return list;
 	}
 
 	public void deleteDependency(ATerm from, ATerm to) {
 		ModuleId moduleFromId = factory.ModuleIdFromTerm(from);
 		ModuleId moduleToId = factory.ModuleIdFromTerm(to);
 
 		moduleDB.deleteDependency(moduleFromId, moduleToId);
 	}
 
 	public void deleteDependencies(ATerm id) {
 		try {
 			ModuleId moduleId = factory.ModuleIdFromTerm(id);
 			moduleDB.deleteDependencies(moduleId);
 		} catch (IllegalArgumentException e) {
 			System.err.println("warning:" + e);
 		}
 	}
 
 	public ATerm getModuleGraph(ATerm namespace) {
 		Graph graph = moduleDB.getModuleGraph(namespace);
 
 		return EclipseTool.factory.make("module-graph(<term>)", graph
 				.toTerm());
 	}
 
 	public void recTerminate(ATerm t0) {
 	}
 
 	public void attributeSet(ModuleId id, ATerm namespace, ATerm key,
 			ATerm oldValue, ATerm newValue) {
 
 		if (oldValue == null) {
 			/*
 			 * The old value is unknown, so we construct a pattern that may mean
 			 * any term
 			 */
 			oldValue = key.getFactory().parse("undefined");
 		}
 
 		if (newValue == null) {
 			/*
 			 * The new value is unknown, so we construct a pattern that may mean
 			 * any term
 			 */
 			newValue = key.getFactory().parse("undefined");
 		}
 
 		
 	  sendEvent(EclipseTool.factory.make(
 				"attribute-changed(<term>,<term>,<term>,<term>,<term>)", id
 						.toTerm(), namespace, key, oldValue, newValue));
 	}
 
 	public void registerAttributeUpdateRule(ATerm namespace, ATerm key,
 			ATerm rule, ATerm value) {
 		//ATerm namespace = Tool.factory.makeAppl(Tool.factory.makeAFun(namespaceStr, 0, true));
 		//ATerm key = Tool.factory.makeAppl(Tool.factory.makeAFun(keyStr, 0, true));
 		moduleDB.registerAttributeUpdateRule(namespace, key, rule, value);
 	}
 
 	public void recAckEvent(ATerm t0) {
 		// intentionally empty
 	}
 }
