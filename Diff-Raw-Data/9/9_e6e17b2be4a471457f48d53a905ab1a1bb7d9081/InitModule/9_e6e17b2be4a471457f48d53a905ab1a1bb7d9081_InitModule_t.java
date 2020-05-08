 package com.laboki.eclipse.plugin.fastopen;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.TypeLiteral;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.name.Names;
 import com.google.inject.spi.InjectionListener;
 import com.google.inject.spi.TypeEncounter;
 import com.google.inject.spi.TypeListener;
 import com.laboki.eclipse.plugin.fastopen.opener.Factory;
 import com.laboki.eclipse.plugin.fastopen.opener.RecentResources;
 import com.laboki.eclipse.plugin.fastopen.opener.RecentResourcesFilter;
 import com.laboki.eclipse.plugin.fastopen.opener.files.AccessedFiles;
import com.laboki.eclipse.plugin.fastopen.opener.files.AccessedFilesSerializer;
 import com.laboki.eclipse.plugin.fastopen.opener.files.ModifiedFiles;
 import com.laboki.eclipse.plugin.fastopen.opener.files.RecentFiles;
import com.laboki.eclipse.plugin.fastopen.opener.files.WorkspaceFiles;
 import com.laboki.eclipse.plugin.fastopen.opener.ui.Dialog;
 
 final class InitModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		this.registerEventBusListeners();
 		this.bindConstant().annotatedWith(Names.named("true")).to(true);
 		this.bind(Dialog.class).asEagerSingleton();
 		this.bind(RecentResourcesFilter.class).asEagerSingleton();
 		this.bind(RecentResources.class).asEagerSingleton();
 		this.bind(RecentFiles.class).asEagerSingleton();
 		this.bind(AccessedFiles.class).asEagerSingleton();
		this.bind(AccessedFilesSerializer.class).asEagerSingleton();
		this.bind(ModifiedFiles.class).asEagerSingleton();
		this.bind(WorkspaceFiles.class).asEagerSingleton();
 		this.bind(Factory.class).asEagerSingleton();
 	}
 
 	private void registerEventBusListeners() {
 		this.bindListener(Matchers.any(), new TypeListener() {
 
 			@Override
 			public <I> void hear(final TypeLiteral<I> typeLiteral, final TypeEncounter<I> typeEncounter) {
 				typeEncounter.register(new InjectionListener<I>() {
 
 					@Override
 					public void afterInjection(final I i) {
 						EventBus.register(i);
 					}
 				});
 			}
 		});
 	}
 }
