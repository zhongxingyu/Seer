 package org.consulo.lua.module.extension;
 
 import javax.swing.Icon;
 
 import org.consulo.module.extension.ModuleExtensionProvider;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import com.intellij.openapi.module.Module;
 import com.sylvanaar.idea.Lua.LuaIcons;
 
 /**
  * @author VISTALL
  * @since 12.07.13.
  */
 public class LuaModuleExtensionProvider implements ModuleExtensionProvider<LuaModuleExtension, LuaMutableModuleExtension>
 {
 	@Nullable
 	@Override
 	public Icon getIcon()
 	{
 		return LuaIcons.LUA_ICON;
 	}
 
 	@NotNull
 	@Override
 	public String getName()
 	{
 		return "Lua";
 	}
 
 	@NotNull
 	@Override
	public Class<LuaModuleExtension> getImmutableClass()
	{
		return LuaModuleExtension.class;
	}

	@NotNull
	@Override
 	public LuaModuleExtension createImmutable(@NotNull String s, @NotNull Module module)
 	{
 		return new LuaModuleExtension(s, module);
 	}
 
 	@NotNull
 	@Override
 	public LuaMutableModuleExtension createMutable(@NotNull String s, @NotNull Module module, @NotNull LuaModuleExtension moduleExtension)
 	{
 		return new LuaMutableModuleExtension(s, module, moduleExtension);
 	}
 }
