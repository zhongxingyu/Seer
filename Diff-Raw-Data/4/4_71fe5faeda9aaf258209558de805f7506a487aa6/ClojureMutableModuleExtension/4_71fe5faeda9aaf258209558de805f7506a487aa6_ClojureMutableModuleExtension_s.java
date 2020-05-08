 package org.jetbrains.plugins.clojure.module.extension;
 
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.util.Comparing;
 import org.consulo.module.extension.MutableModuleExtension;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.jetbrains.plugins.clojure.config.ui.ClojureFacetSettingsTab;
 
 import javax.swing.*;
 
 /**
  * @author VISTALL
  * @since 14:37/12.06.13
  */
 public class ClojureMutableModuleExtension extends ClojureModuleExtension implements MutableModuleExtension<ClojureModuleExtension> {
   private final ClojureModuleExtension myModuleExtension;
 
   public ClojureMutableModuleExtension(@NotNull String id, @NotNull Module module, ClojureModuleExtension clojureModuleExtension) {
     super(id, module);
     myModuleExtension = clojureModuleExtension;
     commit(clojureModuleExtension);
   }
 
   public void setReplClass(@NotNull String replClass) {
     myReplClass = replClass;
   }
 
   public void setJvmOpts(@NotNull String jvmOpts) {
     myJvmOpts = jvmOpts;
   }
 
   public void setReplOpts(@NotNull String replOpts) {
     myReplOpts = replOpts;
   }
 
   @Nullable
   @Override
  public JComponent createConfigurablePanel(@Nullable Runnable runnable) {
     return new ClojureFacetSettingsTab(this);
   }
 
   @Override
   public void setEnabled(boolean b) {
     myIsEnabled = b;
   }
 
   @Override
   public boolean isModified() {
     boolean modified = false;
     modified |= isEnabled() != myModuleExtension.isEnabled();
     modified |= !Comparing.equal(getJvmOpts(), myModuleExtension.getJvmOpts());
     modified |= !Comparing.equal(getReplClass(), myModuleExtension.getReplClass());
     modified |= !Comparing.equal(getReplOpts(), myModuleExtension.getReplOpts());
     return modified;
   }
 
   @Override
   public void commit() {
     myModuleExtension.commit(this);
   }
 }
