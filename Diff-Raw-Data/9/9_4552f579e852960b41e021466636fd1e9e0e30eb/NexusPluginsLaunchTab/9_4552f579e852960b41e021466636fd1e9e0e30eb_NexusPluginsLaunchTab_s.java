 package com.ifedorenko.m2e.nexusdev.internal.launch.ui;
 
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_ADD_REQUIRED_PLUGINS;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ICheckStateProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.m2e.core.MavenPlugin;
 import org.eclipse.m2e.core.project.IMavenProjectFacade;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 
 import com.ifedorenko.m2e.binaryproject.BinaryProjectPlugin;
 import com.ifedorenko.m2e.nexusdev.internal.launch.SelectedProjects;
 
 @SuppressWarnings( "restriction" )
 public class NexusPluginsLaunchTab
     extends AbstractLaunchConfigurationTab
 {
     private static int SELECTION_ALLWORKSPACE = 0;
 
     private static int SELECTION_SELECTED = 1;
 
     private Text pluginFilter;
 
     private CheckboxTreeViewer pluginViewer;
 
     private Combo selectionCombo;
 
     private SelectedProjects selectedProjects;
 
     private Button btnSelectAll;
 
     private Button btnDeselectAll;
 
     private Button btnOnlyShowSelected;
 
     private ViewerFilter selectedFilter = new ViewerFilter()
     {
         @Override
         public boolean select( Viewer viewer, Object parentElement, Object element )
         {
             if ( btnOnlyShowSelected.getSelection() && element instanceof IMavenProjectFacade )
             {
                 return selectedProjects.isSelected( (IMavenProjectFacade) element );
             }
             return true;
         }
     };
 
     private ViewerFilter nameFilter = new ViewerFilter()
     {
         @Override
         public boolean select( Viewer viewer, Object parentElement, Object element )
         {
             if ( element instanceof IMavenProjectFacade )
             {
                 String text = pluginFilter.getText();
                 if ( text != null )
                 {
                     text = text.trim();
                 }
                 if ( text != null && !text.isEmpty() )
                 {
                     return ( (IMavenProjectFacade) element ).getProject().getName().contains( text );
                 }
             }
             return true;
         }
     };
 
     private Button btnAddRequiredPlugins;
 
     /**
      * @wbp.parser.entryPoint
      */
     @Override
     public void createControl( Composite parent )
     {
         Composite composite = new Composite( parent, SWT.NONE );
         setControl( composite );
         composite.setLayout( new GridLayout( 2, false ) );
 
         Label lblLaunchWith = new Label( composite, SWT.NONE );
         lblLaunchWith.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 ) );
         lblLaunchWith.setText( "Launch with:" );
 
         selectionCombo = new Combo( composite, SWT.READ_ONLY );
         selectionCombo.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 boolean selectAll = selectionCombo.getSelectionIndex() == SELECTION_ALLWORKSPACE;
                 selectedProjects.setSelectAll( selectAll );
                 pluginViewer.refresh( true );
                 pluginViewer.getTree().setEnabled( !selectAll );
                 btnDeselectAll.setEnabled( !selectAll );
                 btnSelectAll.setEnabled( !selectAll );
                 setDirty( true );
                 updateLaunchConfigurationDialog();
             }
         } );
         selectionCombo.setItems( new String[] { "All workspace plugins", "Plugins selected below" } );
         selectionCombo.select( 0 );
 
         Composite pluginsComposite = new Composite( composite, SWT.NONE );
         GridLayout gl_pluginsComposite = new GridLayout( 2, false );
         gl_pluginsComposite.marginWidth = 0;
         gl_pluginsComposite.marginHeight = 0;
         pluginsComposite.setLayout( gl_pluginsComposite );
         pluginsComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
 
         pluginFilter = new Text( pluginsComposite, SWT.BORDER | SWT.H_SCROLL | SWT.SEARCH | SWT.CANCEL );
         pluginFilter.addModifyListener( new ModifyListener()
         {
             public void modifyText( ModifyEvent e )
             {
                 pluginViewer.refresh();
             }
         } );
         pluginFilter.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1 ) );
         new Label( pluginsComposite, SWT.NONE );
 
         pluginViewer = new CheckboxTreeViewer( pluginsComposite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION );
         pluginViewer.addCheckStateListener( new ICheckStateListener()
         {
             public void checkStateChanged( CheckStateChangedEvent event )
             {
                 selectedProjects.setSelect( (IMavenProjectFacade) event.getElement(), event.getChecked() );
                 setDirty( true );
                 updateLaunchConfigurationDialog();
             }
         } );
         Tree pluginTree = pluginViewer.getTree();
         pluginTree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
 
         pluginViewer.setContentProvider( new ITreeContentProvider()
         {
             @Override
             public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
             {
             }
 
             @Override
             public void dispose()
             {
             }
 
             @Override
             public boolean hasChildren( Object element )
             {
                 return false;
             }
 
             @Override
             public Object getParent( Object element )
             {
                 return null;
             }
 
             @Override
             public Object[] getElements( Object inputElement )
             {
                 if ( inputElement instanceof List<?> )
                 {
                     return ( (List<?>) inputElement ).toArray();
                 }
                 return null;
             }
 
             @Override
             public Object[] getChildren( Object parentElement )
             {
                 return null;
             }
         } );
 
         pluginViewer.setCheckStateProvider( new ICheckStateProvider()
         {
             @Override
             public boolean isGrayed( Object element )
             {
                 return false;
             }
 
             @Override
             public boolean isChecked( Object element )
             {
                 if ( element instanceof IMavenProjectFacade )
                 {
                     return selectedProjects.isSelected( (IMavenProjectFacade) element );
                 }
 
                 return false;
             }
         } );
 
         pluginViewer.setLabelProvider( new ILabelProvider()
         {
             @Override
             public void removeListener( ILabelProviderListener listener )
             {
             }
 
             @Override
             public boolean isLabelProperty( Object element, String property )
             {
                 return false;
             }
 
             @Override
             public void dispose()
             {
             }
 
             @Override
             public void addListener( ILabelProviderListener listener )
             {
             }
 
             @Override
             public String getText( Object element )
             {
                 if ( element instanceof IMavenProjectFacade )
                 {
                     return ( (IMavenProjectFacade) element ).getProject().getName();
                 }
                 return null;
             }
 
             @Override
             public Image getImage( Object element )
             {
                 return null;
             }
         } );
         pluginViewer.setFilters( new ViewerFilter[] { nameFilter, selectedFilter } );
 
         Composite buttonsComposite = new Composite( pluginsComposite, SWT.NONE );
         GridLayout gl_buttonsComposite = new GridLayout( 1, false );
         gl_buttonsComposite.verticalSpacing = 0;
         gl_buttonsComposite.marginWidth = 0;
         gl_buttonsComposite.horizontalSpacing = 0;
         gl_buttonsComposite.marginHeight = 0;
         buttonsComposite.setLayout( gl_buttonsComposite );
         buttonsComposite.setLayoutData( new GridData( SWT.LEFT, SWT.FILL, false, false, 1, 1 ) );
 
         btnSelectAll = new Button( buttonsComposite, SWT.NONE );
         btnSelectAll.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 selectAll( true );
             }
         } );
         btnSelectAll.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
         btnSelectAll.setText( "Select all" );
 
         btnDeselectAll = new Button( buttonsComposite, SWT.NONE );
         btnDeselectAll.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 selectAll( false );
             }
         } );
         btnDeselectAll.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
         btnDeselectAll.setText( "Deselect all" );
 
         Composite buttonsFillerComposite = new Composite( buttonsComposite, SWT.NONE );
         buttonsFillerComposite.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, true, 1, 1 ) );
 
         btnOnlyShowSelected = new Button( buttonsComposite, SWT.CHECK );
         btnOnlyShowSelected.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 pluginViewer.refresh();
             }
         } );
         btnOnlyShowSelected.setText( "Only show selected" );
 
         btnAddRequiredPlugins = new Button( composite, SWT.CHECK );
         btnAddRequiredPlugins.setToolTipText( "Automatically include workspace plugins required by the plugins selected in the list above" );
         btnAddRequiredPlugins.setSelection( true );
         btnAddRequiredPlugins.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 ) );
         btnAddRequiredPlugins.setText( "Add required workspace plugins" );
     }
 
     @Override
     public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
     {
     }
 
     @Override
     public void initializeFrom( ILaunchConfiguration configuration )
     {
         selectedProjects = SelectedProjects.fromLaunchConfig( configuration );
         try
         {
             btnAddRequiredPlugins.setSelection( configuration.getAttribute( ATTR_ADD_REQUIRED_PLUGINS, true ) );
         }
         catch ( CoreException e )
         {
             btnAddRequiredPlugins.setSelection( true );
         }
         boolean selectAll = selectedProjects.isSelectAll();
         selectionCombo.select( selectAll ? SELECTION_ALLWORKSPACE : SELECTION_SELECTED );
         pluginViewer.getTree().setEnabled( !selectAll );
         btnDeselectAll.setEnabled( !selectAll );
         btnSelectAll.setEnabled( !selectAll );
         pluginViewer.setInput( getAllWorkspacePlugins() );
     }
 
     @Override
     public void performApply( ILaunchConfigurationWorkingCopy configuration )
     {
         selectedProjects.toLaunchConfig( configuration );
         configuration.setAttribute( ATTR_ADD_REQUIRED_PLUGINS, btnAddRequiredPlugins.getSelection() );
     }
 
     @Override
     public String getName()
     {
         return "Plug-ins";
     }
 
     private List<IMavenProjectFacade> getAllWorkspacePlugins()
     {
         List<IMavenProjectFacade> plugins = new ArrayList<IMavenProjectFacade>();
         for ( IMavenProjectFacade facade : MavenPlugin.getMavenProjectRegistry().getProjects() )
         {
             if ( "nexus-plugin".equals( facade.getPackaging() ) )
             {
                 IProject project = facade.getProject();
                 try
                 {
                     if ( project.getPersistentProperty( BinaryProjectPlugin.QNAME_JAR ) != null )
                     {
                         // there is currently no way to import classified artifacts as binary projects
                         continue;
                     }
                 }
                 catch ( CoreException e )
                 {
                 }
                 plugins.add( facade );
             }
         }
         return plugins;
     }
 
     void selectAll( boolean select )
     {
         final ViewerFilter[] filters = pluginViewer.getFilters();
         @SuppressWarnings( "unchecked" )
         List<IMavenProjectFacade> projects = (List<IMavenProjectFacade>) pluginViewer.getInput();
         projects: for ( IMavenProjectFacade project : projects )
         {
             if ( filters != null )
             {
                 for ( ViewerFilter filter : filters )
                 {
                     if ( !filter.select( pluginViewer, null, project ) )
                     {
                         continue projects;
                     }
                 }
             }
             selectedProjects.setSelect( project, select );
         }
         pluginViewer.refresh();
         setDirty( true );
         updateLaunchConfigurationDialog();
     }
 }
