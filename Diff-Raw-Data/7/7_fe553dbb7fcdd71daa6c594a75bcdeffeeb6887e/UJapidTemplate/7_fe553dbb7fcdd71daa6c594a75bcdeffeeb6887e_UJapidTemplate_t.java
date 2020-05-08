 package cn.uc.play.japid.template;
 
 import java.io.File;
 import java.lang.instrument.ClassDefinition;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Date;
 
 import cn.bran.japid.compiler.NamedArgRuntime;
 import cn.bran.japid.template.JapidTemplateBaseWithoutPlay;
 import cn.bran.japid.template.RenderResult;
 import cn.bran.play.JapidTemplateBase;
 import cn.uc.play.japid.UJapidPlugin;
 import cn.uc.play.japid.exception.TemplateCompileException;
 import cn.uc.play.japid.util.FileUtils;
 
 import play.Play;
 import play.cache.Cache;
 import play.classloading.ApplicationClasses;
 import play.classloading.ApplicationClassloaderState;
 import play.classloading.BytecodeCache;
 import play.classloading.HotswapAgent;
 import play.classloading.ApplicationClasses.ApplicationClass;
 import play.exceptions.TemplateExecutionException;
 import play.templates.Template;
 import play.vfs.VirtualFile;
 
 /**
  * A japid template encapsulation.
  * 
  * @author Robin Han<sakuyahan@163.com>
  * @date 2012-4-29
  */
 public class UJapidTemplate extends Template {
 	public Date lastModifyTime;
 
 	public TemplateStoreMode mode;
 
 	public String nameWithPath;
 
 	public Date lastSyncTime;
 
 	private Class<JapidTemplateBase> templateClass;
 
 	private boolean hasCompiled = false;
 
 	public boolean hasCompiled() {
 		return this.hasCompiled;
 	}
 
 	public boolean syncSource() {
 		if (mode == TemplateStoreMode.File) {
 			lastSyncTime = new Date();
 			return true;
 		}
 
 		lastSyncTime = new Date();
 		return true;
 	}
 
 	public RenderResult renderWithNamedArgs(NamedArgRuntime... namedArgs) {
 		return TemplateRenderInvoker.invokeNamedArgsRender(templateClass,
 				namedArgs);
 	}
 
 	public RenderResult render(Object... objects) {
 
 		return TemplateRenderInvoker.invokeRender(templateClass, objects);
 	}
 
 	@Override
 	public void compile() {
 		UJapidTemplateCompiler compiler = UJapidTemplateCompiler.getInstance();
 		compiler.resetImports();
 
 		File javaFile = null;
 		try {
 			compiler.transformToJava(this.nameWithPath);
 
 			String japidViewsJavaPath = FileUtils.convertExtensionTo(
 					this.nameWithPath, ".java");
 
 			javaFile = new File(UJapidPlugin.ROOT + File.separator
 					+ japidViewsJavaPath);
 
 			VirtualFile file = Play.getVirtualFile(javaFile.getPath());
 
 			String className = FileUtils
 					.convertPathToPackage(japidViewsJavaPath);
 
 			ApplicationClass ac = Play.classes.getApplicationClass(className);
 			Class<JapidTemplateBase> c = (Class<JapidTemplateBase>) Play.classloader
 					.loadApplicationClass(className);
 			if (ac == null) {
 				ac = new ApplicationClass();
 				ac.javaFile = file;
 				ac.name = className;
 				ac.javaSource = FileUtils.readToEnd(javaFile.getPath());
 
 				Play.classes.add(ac);
 				ac.compile();
 				ac.enhance();
 			} else {
 				ac.refresh();
 				ac.compile();
 				ac.enhance();
 
 				if (Play.started) {
 					ac.javaClass = c;
 
 					BytecodeCache.cacheBytecode(ac.enhancedByteCode, ac.name,
 							ac.javaSource);
 					List<ClassDefinition> newDefinitions = new ArrayList<ClassDefinition>();
 					newDefinitions.add(new ClassDefinition(ac.javaClass,
 							ac.enhancedByteCode));
 					Play.classloader.currentState = new ApplicationClassloaderState();
 
 					Cache.clear();
 					if (HotswapAgent.enabled) {
 						try {
 							HotswapAgent.reload(newDefinitions
 									.toArray(new ClassDefinition[newDefinitions
 											.size()]));
 						} catch (Throwable e) {
 							e.printStackTrace();
 							throw new RuntimeException("Need reload");
 						}
 					} else {
 						throw new RuntimeException("Need reload");
 					}
 				}
 			}
 
 			int modifiers = c.getModifiers();
 			if (Modifier.isAbstract(modifiers)) {
 				throw new RuntimeException(
 						"Cannot init the template class since it's an abstract class: "
 								+ c.getName());
 			}
 
 			templateClass = c;
 
 			hasCompiled = true;
 		} catch (Exception e) {
 			if (Play.Mode.DEV == Play.mode && javaFile != null && javaFile.exists()) {
 				javaFile.delete();
 			}
 			throw new TemplateCompileException(nameWithPath, e);
 		}
 	}
 
 	@Override
 	protected String internalRender(Map<String, Object> args) {
 		if (args == null || args.size() == 0) {
 			return render(new Object[0]).getText();
 		} else {
 			NamedArgRuntime[] argsArray = new NamedArgRuntime[args.size()];
 
 			int i = 0;
 			for (Map.Entry<String, Object> arg : args.entrySet()) {
 				argsArray[i] = new NamedArgRuntime(arg.getKey(), arg.getValue());
 				i++;
 			}
 
 			return renderWithNamedArgs(argsArray).getText();
 		}
 	}
 	
 	public synchronized static void compileTemplate(UJapidTemplate template) {
 		if (template.hasCompiled()) {
 			return;
 		}
 
 		template.compile();
 	}
 }
