 /*
  * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos;
 
 import com.intellij.openapi.actionSystem.ActionManager;
 import com.intellij.openapi.actionSystem.Anchor;
 import com.intellij.openapi.actionSystem.Constraints;
 import com.intellij.openapi.actionSystem.DefaultActionGroup;
 import com.intellij.openapi.components.ProjectComponent;
 import com.intellij.openapi.options.Configurable;
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.InvalidDataException;
 import com.intellij.openapi.util.JDOMExternalizable;
 import com.intellij.openapi.util.WriteExternalException;
 import com.intellij.openapi.wm.ToolWindowManager;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import org.jdom.Element;
 import org.jetbrains.annotations.Nls;
 import org.qi4j.bootstrap.ApplicationAssemblyFactory;
 import org.qi4j.bootstrap.ApplicationFactory;
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.bootstrap.SingletonAssembly;
 import org.qi4j.chronos.action.task.TaskAssociationAction;
 import org.qi4j.chronos.service.Services;
 import org.qi4j.chronos.service.composites.ServicesComposite;
 import org.qi4j.composite.CompositeBuilder;
 import org.qi4j.composite.CompositeBuilderFactory;
 import org.qi4j.runtime.Energy4Java;
 import org.qi4j.runtime.Qi4jRuntime;
 import org.qi4j.runtime.structure.ApplicationInstance;
 import org.qi4j.runtime.structure.LayerInstance;
import org.qi4j.runtime.structure.ModuleContext;
 import org.qi4j.runtime.structure.ModuleInstance;
 
 public class ChronosProjectComponent
     implements ProjectComponent, Configurable, JDOMExternalizable
 {
     //TODO fix icon
     private static final Icon ICON = IconLoader.getIcon( "/org/qi4j/chronos/ui/setting/icon.png" );
 
     public final static String DISPLAY_NAME = "Chronos";
     public final static String COMPONENT_NAME = "ChronosSetting";
 
     private ChronosConfig chronosConfig;
 
     private ChronosApp chronosApp;
 
     private ToolWindowManager toolWindowManager;
 
     private CompositeBuilderFactory factory;
 
     private Services services;
     private Project project;
     private ActionManager actionManager;
 
     private ChronosToolWindow toolWindow;
     private TaskAssociationAction taskAssociationAction;
 
     public ChronosProjectComponent( ToolWindowManager toolWindowManager, ActionManager actionManager, Project project )
     {
         this.toolWindowManager = toolWindowManager;
         this.project = project;
         this.actionManager = actionManager;
 
         chronosConfig = new ChronosConfig();
     }
 
     public static ChronosProjectComponent getInstance( Project project )
     {
         return project.getComponent( ChronosProjectComponent.class );
     }
 
 //    public static ChronosProjectComponent getInstance( DataContext dataContext )
 //    {
 //        Project project = DataKeys.PROJECT.getData( dataContext );
 //        return getInstance( project );
 //    }
 //
 //    public static ChronosProjectComponent getInstance()
 //    {
 //        return getInstance( DataManager.getInstance().getDataContext() );
 //    }
 
     //
 
     public ChronosApp getChronosApp()
     {
         return chronosApp;
     }
 
     public void projectOpened()
     {
 
         chronosApp = new ChronosApp( project, chronosConfig, factory, services );
         chronosApp.addChronosAppListener( new ChronosAppListener()
         {
             public void chronosAppStarted()
             {
                 registerChronosComponents();
             }
 
             public void chronosAppClosed()
             {
                 unregisterChronosComponents( false );
             }
         } );
 
         chronosApp.start();
     }
 
     private void registerChronosComponents()
     {
         SwingUtilities.invokeLater( new Runnable()
         {
             public void run()
             {
                 toolWindow = new ChronosToolWindow( toolWindowManager, actionManager, project );
 
                 toolWindow.register();
 
                 taskAssociationAction = new TaskAssociationAction( project );
 
                 DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance().getAction( "MainToolBar" );
 
                 actionGroup.add( taskAssociationAction, new Constraints( Anchor.LAST, "after" ) );
             }
         } );
     }
 
     private void unregisterChronosComponents( boolean awtEventThread )
     {
         if( toolWindow != null )
         {
             if( awtEventThread )
             {
                 unregisterChronosComponents_0();
             }
             else
             {
                 SwingUtilities.invokeLater( new Runnable()
                 {
                     public void run()
                     {
                         unregisterChronosComponents_0();
                     }
                 } );
             }
         }
     }
 
     private void unregisterChronosComponents_0()
     {
         toolWindow.unregister();
 
         toolWindow = null;
 
         if( taskAssociationAction != null )
         {
             DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance().getAction( "MainToolBar" );
 
             actionGroup.remove( taskAssociationAction );
 
             taskAssociationAction = null;
         }
     }
 
     public void projectClosed()
     {
         unregisterChronosComponents( true );
 
         chronosApp.stop();
     }
 
     public String getComponentName()
     {
         return COMPONENT_NAME;
     }
 
     public void initComponent()
     {
         /*
           TODO
           if these codes were placed in projectOpened method, it will throw an
           ExceptionInInitializerError by Enhancer class in cglib. Fix it.
          */
 //        factory = new Energy4Java().newCompositeBuilderFactory();
 
         try
         {
             ApplicationInstance application = newApplication();
             LayerInstance layerInstance = application.getLayerInstances().get( 0 );
             ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
             factory = moduleInstance.getCompositeBuilderFactory();
             CompositeBuilder<ServicesComposite> serviceBuilder = factory.newCompositeBuilder( ServicesComposite.class );
             services = serviceBuilder.newInstance();
             services.initServices();
         }
        catch( AssemblyException e )
         {
             e.printStackTrace();  //TODO: Auto-generated, need attention.
         }
 
     }
 
     public void disposeComponent()
     {
         //nothing
     }
 
     @Nls public String getDisplayName()
     {
         return DISPLAY_NAME;
     }
 
     public Icon getIcon()
     {
         return ICON;
     }
 
     public static ImageIcon getIcon( String packageName, Class clazz ) throws IOException
     {
         return new ImageIcon( ImageIO.read( clazz.getClassLoader().getResourceAsStream( packageName ) ) );
     }
 
     public String getHelpTopic()
     {
         return null;
     }
 
     public JComponent createComponent()
     {
         return new ChronosProjectPanel( chronosApp );
     }
 
     public boolean isModified()
     {
         return false;
     }
 
     public void apply() throws ConfigurationException
     {
         //nothing
     }
 
     public void reset()
     {
         //nothing
     }
 
     public void disposeUIResources()
     {
         //nothing
     }
 
     public void readExternal( Element element ) throws InvalidDataException
     {
         chronosConfig.readExternal( element );
     }
 
     public void writeExternal( Element element ) throws WriteExternalException
     {
         chronosConfig.writeExternal( element );
     }
 
     protected ApplicationInstance newApplication()
         throws AssemblyException
     {
         Qi4jRuntime qi4j = new Energy4Java();
         SingletonAssembly assembly = new SingletonAssembly()
         {
             public void configure( ModuleAssembly module ) throws AssemblyException
             {
                 module.addComposites(
                     // TODO: Add all the Composites used.
                 );
             }
         };
         ApplicationFactory applicationFactory = new ApplicationFactory( qi4j, new ApplicationAssemblyFactory() );
         return applicationFactory.newApplication( assembly ).newApplicationInstance( "Chronos" );
     }
 }
