 package com.sixrr.guiceyidea.module.extension;
 
 import javax.swing.JComponent;
 
 import org.consulo.module.extension.MutableModuleExtension;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
 
 /**
  * @author VISTALL
  * @since 17:33/28.05.13
  */
 public class GuiceyIDEAMutableModuleExtension extends GuiceyIDEAModuleExtension implements MutableModuleExtension<GuiceyIDEAModuleExtension>
 {
 	@NotNull
 	private final GuiceyIDEAModuleExtension moduleExtension;
 
 	public GuiceyIDEAMutableModuleExtension(@NotNull String id, @NotNull Module module, @NotNull GuiceyIDEAModuleExtension moduleExtension)
 	{
 		super(id, module);
 		this.moduleExtension = moduleExtension;
 		commit(moduleExtension);
 	}
 
 	@Nullable
 	@Override
	public JComponent createConfigurablePanel(@NotNull ModifiableRootModel modifiableRootModel, @Nullable Runnable runnable)
 	{
 		return null;
 	}
 
 	@Override
 	public void setEnabled(boolean b)
 	{
 		myIsEnabled = b;
 	}
 
 	@Override
 	public boolean isModified()
 	{
 		return myIsEnabled != moduleExtension.isEnabled();
 	}
 
 	@Override
 	public void commit()
 	{
 		moduleExtension.commit(this);
 	}
 }
