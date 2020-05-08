 package org.mosaic.idea.runner.task;
 
 import com.intellij.execution.BeforeRunTaskProvider;
 import com.intellij.execution.configurations.ModuleRunProfile;
 import com.intellij.execution.configurations.RunConfiguration;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.openapi.actionSystem.DataContext;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.application.ModalityState;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModulePointer;
 import com.intellij.openapi.progress.ProgressManager;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.ui.DialogBuilder;
 import com.intellij.openapi.ui.DialogWrapper;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.Key;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.Icon;
 import org.jetbrains.annotations.Nullable;
 import org.mosaic.idea.module.actions.BuildModulesTask;
 import org.mosaic.idea.module.facet.ModuleFacet;
 
 import static com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT;
 import static java.util.Arrays.asList;
 
 /**
  * @author arik
  */
 public class BuildMosaicModulesBeforeRunTasksProvider extends BeforeRunTaskProvider<BuildMosaicModulesBeforeRunTask>
 {
     public static final String NAME = "Build Mosaic Modules";
 
     public static final String DESCRIPTION = NAME;
 
     public static final Key<BuildMosaicModulesBeforeRunTask> ID = Key.create( "Mosaic.BuildModulesBeforeRunTask" );
 
     @Override
     public Key<BuildMosaicModulesBeforeRunTask> getId()
     {
         return ID;
     }
 
     public String getName()
     {
         return NAME;
     }
 
     public String getDescription( BuildMosaicModulesBeforeRunTask buildMosaicModulesBeforeRunTask )
     {
         return DESCRIPTION;
     }
 
     @Nullable
     public Icon getIcon()
     {
        return IconLoader.getIcon( "/com/infolinks/rinku/idea/plugin/icons/rinku.png" );
     }
 
     @Nullable
     @Override
     public Icon getTaskIcon( BuildMosaicModulesBeforeRunTask task )
     {
        return IconLoader.getIcon( "/com/infolinks/rinku/idea/plugin/icons/rinku.png" );
     }
 
     public boolean isConfigurable()
     {
         return true;
     }
 
     public boolean canExecuteTask( RunConfiguration runConfiguration,
                                    BuildMosaicModulesBeforeRunTask buildMosaicModulesBeforeRunTask )
     {
         return true;
     }
 
     @Override
     public boolean isSingleton()
     {
         return true;
     }
 
     @Nullable
     @Override
     public BuildMosaicModulesBeforeRunTask createTask( RunConfiguration runConfiguration )
     {
         BuildMosaicModulesBeforeRunTask task = new BuildMosaicModulesBeforeRunTask( runConfiguration.getProject() );
         task.setEnabled( true );
         return task;
     }
 
     @Override
     public boolean configureTask( RunConfiguration runConfiguration, BuildMosaicModulesBeforeRunTask task )
     {
         SelectMosaicModulesForm chooser = new SelectMosaicModulesForm( runConfiguration.getProject() );
         List<ModulePointer> selectedModules = task.getModulesToBuild();
         if( selectedModules != null )
         {
             chooser.setSelectedModules( selectedModules );
         }
 
         DialogBuilder builder = new DialogBuilder( runConfiguration.getProject() );
         builder.setTitle( "Select modules to build" );
         builder.setDimensionServiceKey( "#BuildMosaicModulesBeforeRunChooser" );
         builder.addOkAction();
         builder.addCancelAction();
         builder.setCenterPanel( chooser.getPanel() );
         builder.setPreferredFocusComponent( chooser.getPreferredFocusComponent() );
         if( builder.show() == DialogWrapper.OK_EXIT_CODE )
         {
             task.setModulesToBuild( chooser.getSelectedModules() );
             return true;
         }
         else
         {
             return false;
         }
     }
 
     public boolean executeTask( DataContext dataContext,
                                 final RunConfiguration runConfiguration,
                                 ExecutionEnvironment executionEnvironment,
                                 BuildMosaicModulesBeforeRunTask beforeRunTask )
     {
         Project project = PROJECT.getData( dataContext );
         if( project == null )
         {
             return true;
         }
 
         List<Module> modules;
 
         List<ModulePointer> modulePointers = beforeRunTask.getModulesToBuild();
         if( modulePointers == null )
         {
             if( runConfiguration instanceof ModuleRunProfile )
             {
                 ModuleRunProfile moduleRunProfile = ( ModuleRunProfile ) runConfiguration;
                 modules = ModuleFacet.findMosaicModules( asList( moduleRunProfile.getModules() ) );
             }
             else
             {
                 modules = ModuleFacet.findMosaicModules( project );
             }
         }
         else
         {
             modules = new LinkedList<>();
             for( ModulePointer modulePointer : modulePointers )
             {
                 Module module = modulePointer.getModule();
                 if( module != null )
                 {
                     modules.add( module );
                 }
             }
         }
 
         final BuildModulesTask buildModulesTask = new BuildModulesTask( project, modules );
         ApplicationManager.getApplication().invokeAndWait( new Runnable()
         {
             @Override
             public void run()
             {
                 ProgressManager.getInstance().run( buildModulesTask );
             }
         }, ModalityState.any() );
 
         BuildMosaicModulesWhileRunning whileRunning = BuildMosaicModulesWhileRunning.getInstance( project );
         whileRunning.makeModulesForExecutionId( executionEnvironment.getExecutionId(), modules );
 
         return buildModulesTask.isSuccessful();
     }
 }
