 package org.intellij.alt.runconfiguration;
 
 import com.google.common.base.Optional;
 import com.intellij.execution.Location;
 import com.intellij.execution.RunnerAndConfigurationSettings;
 import com.intellij.execution.actions.ConfigurationContext;
 import com.intellij.execution.application.ApplicationConfiguration;
 import com.intellij.execution.application.ApplicationConfigurationType;
 import com.intellij.execution.junit.RuntimeConfigurationProducer;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.util.ClassUtil;
 import org.dynjs.Config;
 import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.DynObject;
 import org.dynjs.runtime.JSFunction;
 import org.dynjs.runtime.builtins.Require;
 import org.jetbrains.annotations.Nullable;
 
 public class ProducerShim extends RuntimeConfigurationProducer implements Cloneable {
     @SuppressWarnings("unused")
     private PsiFile containingFile;
 
     public ProducerShim() {
         super(ApplicationConfigurationType.getInstance());
     }
 
     @Override
     public PsiElement getSourceElement() {
         return containingFile;
     }
 
     @Nullable
     @Override
     protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, final ConfigurationContext context) {
         Module module = context.getModule();
         if (module == null) { return null; }
 
         JavascriptRuntime runtime = new JavascriptRuntime(context.getProject());
         ScriptedProducer scriptedProducer = new ScriptedProducer(runtime);
 
         Optional<? extends Configuration> configuration = scriptedProducer.produce(context);
         if(!configuration.isPresent()) {
             return null;
         }
 
         PsiClass mainClass = ClassUtil.findPsiClass(PsiManager.getInstance(context.getProject()), configuration.get().main());
         if(mainClass == null) { return null; }
 
         RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(context.getProject(), context);
         ApplicationConfiguration intellijConfiguration = (ApplicationConfiguration) settings.getConfiguration();
         intellijConfiguration.setMainClass(mainClass);
         intellijConfiguration.setProgramParameters(configuration.get().arguments());
         intellijConfiguration.setName(configuration.get().name());
         return settings;
 
     }
 
     @Override
     public int compareTo(Object o) {
         return PREFERED;
     }
 
     public static class JavascriptRuntime {
         private final DynJS runtime;
 
         public JavascriptRuntime(Project project) {
             Config config = new Config(this.getClass().getClassLoader());
             config.setCompileMode(Config.CompileMode.OFF);
             this.runtime = new DynJS(config);
             ((Require)runtime.getExecutionContext().getGlobalObject().get("require"))
                     .addLoadPath(project.getBasePath());
         }
 
         public Object call(String script, Object... args) {
             JSFunction function = (JSFunction)runtime.evaluate(script);
             return runtime.getExecutionContext().call(function, (Object)null, args);
         }
     }
 }
