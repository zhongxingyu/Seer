 package com.ifedorenko.m2e.nexusdev.internal.launch.ui;
 
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_APPLICATION_PORT;
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_INSTALLATION_LOCATION;
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_STANDARD_INSTALLATION;
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_STANDARD_INSTALLATION_ID;
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_WORKDIR_LOCATION;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import com.ifedorenko.m2e.nexusdev.internal.preferences.NexusInstallation;
 import com.ifedorenko.m2e.nexusdev.internal.preferences.NexusInstallations;
 
 public class NexusExternalInstanceLaunchTab
     extends AbstractLaunchConfigurationTab
 {
     private static final NexusInstallations installations = NexusInstallations.INSTANCE;
 
     private Text installationLocation;
 
     private Text workdirLocation;
 
     private Text applicationPort;
 
     private Combo standardInstallation;
 
     private Button btnRadioStandard;
 
     private Button btnRadioCustom;
 
     private Button btnBrowseInstallationLocation;
 
     private Group grpNexusInstallation;
 
     private Button btnStandardAutoupdate;
 
     /**
      * @wbp.parser.entryPoint
      */
     @Override
     public void createControl( Composite parent )
     {
         Composite composite = new Composite( parent, SWT.NONE );
         setControl( composite );
         composite.setLayout( new GridLayout( 2, false ) );
         final ModifyListener modifyListener = new ModifyListener()
         {
             public void modifyText( ModifyEvent e )
             {
                 setDirty( true );
                 updateLaunchConfigurationDialog();
             }
         };
 
         grpNexusInstallation = new Group( composite, SWT.NONE );
         grpNexusInstallation.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
         grpNexusInstallation.setText( "Nexus installation" );
         grpNexusInstallation.setLayout( new GridLayout( 3, false ) );
 
         btnRadioStandard = new Button( grpNexusInstallation, SWT.RADIO );
         btnRadioStandard.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 setStandard( true );
                 setDirty( true );
                 updateLaunchConfigurationDialog();
             }
         } );
         btnRadioStandard.setSelection( true );
         btnRadioStandard.setText( "Standard" );
 
         standardInstallation = new Combo( grpNexusInstallation, SWT.NONE );
         standardInstallation.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
 
         btnStandardAutoupdate = new Button( grpNexusInstallation, SWT.CHECK );
         btnStandardAutoupdate.setSelection( true );
         btnStandardAutoupdate.setEnabled( false );
         btnStandardAutoupdate.setText( "Autoupdate" );
 
         btnRadioCustom = new Button( grpNexusInstallation, SWT.RADIO );
         btnRadioCustom.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 setStandard( false );
                 setDirty( true );
                 updateLaunchConfigurationDialog();
             }
         } );
         btnRadioCustom.setText( "Custom" );
 
         installationLocation = new Text( grpNexusInstallation, SWT.BORDER );
         installationLocation.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
 
         btnBrowseInstallationLocation = new Button( grpNexusInstallation, SWT.NONE );
         btnBrowseInstallationLocation.addSelectionListener( new SelectionAdapter()
         {
             @Override
             public void widgetSelected( SelectionEvent e )
             {
                 DirectoryDialog dialog = new DirectoryDialog( getShell() );
                 String location = dialog.open();
                 if ( location != null )
                 {
                     installationLocation.setText( location );
                 }
             }
         } );
         btnBrowseInstallationLocation.setText( "Browse..." );
         installationLocation.addModifyListener( modifyListener );
         standardInstallation.addModifyListener( new ModifyListener()
         {
             public void modifyText( ModifyEvent e )
             {
                 setDirty( true );
                 updateLaunchConfigurationDialog();
             }
         } );
 
         Label lblWorkdirLocation = new Label( composite, SWT.NONE );
         lblWorkdirLocation.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 ) );
         lblWorkdirLocation.setText( "Workdir location" );
         for ( NexusInstallation installation : installations.getInstallations().values() )
         {
             standardInstallation.add( installation.getId() );
         }
 
         workdirLocation = new Text( composite, SWT.BORDER );
         workdirLocation.addModifyListener( modifyListener );
         workdirLocation.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
 
         Label lblApplicationPort = new Label( composite, SWT.NONE );
         lblApplicationPort.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 ) );
         lblApplicationPort.setText( "Application port" );
 
         applicationPort = new Text( composite, SWT.BORDER );
         applicationPort.addModifyListener( modifyListener );
         applicationPort.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
     }
 
     @Override
     public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
     {
         configuration.setAttribute( ATTR_STANDARD_INSTALLATION, true );
         configuration.setAttribute( ATTR_STANDARD_INSTALLATION_ID, installations.getDefaultInstallation().getId() );
     }
 
     @Override
     public void initializeFrom( ILaunchConfiguration configuration )
     {
         boolean standard;
         try
         {
             standard = configuration.getAttribute( ATTR_STANDARD_INSTALLATION, true );
         }
         catch ( CoreException e )
         {
             standard = false;
         }
         setStandard( standard );
 
         String installationId;
         try
         {
             installationId =
                 configuration.getAttribute( ATTR_STANDARD_INSTALLATION_ID,
                                             installations.getDefaultInstallation().getId() );
         }
         catch ( CoreException e )
         {
             installationId = installations.getDefaultInstallation().getId();
         }
 
         String[] items = standardInstallation.getItems();
         int selected = 0;
         for ( int i = 0; i < items.length; i++ )
         {
             if ( installationId.equals( items[i] ) )
             {
                 selected = i;
                 break;
             }
         }
         standardInstallation.select( selected );
 
         initializeFrom( configuration, installationLocation, ATTR_INSTALLATION_LOCATION );
         initializeFrom( configuration, workdirLocation, ATTR_WORKDIR_LOCATION );
         initializeFrom( configuration, applicationPort, ATTR_APPLICATION_PORT );
     }
 
     private void initializeFrom( ILaunchConfiguration configuration, Text field, String attr )
     {
         String value;
         try
         {
             value = configuration.getAttribute( attr, "" );
         }
         catch ( CoreException e )
         {
             value = "";
         }
         field.setText( value );
     }
 
     @Override
     public void performApply( ILaunchConfigurationWorkingCopy configuration )
     {
         configuration.setAttribute( ATTR_STANDARD_INSTALLATION, btnRadioStandard.getSelection() );
         configuration.setAttribute( ATTR_STANDARD_INSTALLATION_ID, nvl( standardInstallation.getText() ) );
         configuration.setAttribute( ATTR_INSTALLATION_LOCATION, toString( installationLocation ) );
         configuration.setAttribute( ATTR_WORKDIR_LOCATION, toString( workdirLocation ) );
         configuration.setAttribute( ATTR_APPLICATION_PORT, toString( applicationPort ) );
     }
 
     private String toString( Text field )
     {
         String text = field.getText();
         return nvl( text );
     }
 
     private String nvl( String text )
     {
         return text != null && !text.trim().isEmpty() ? text : null;
     }
 
     @Override
     public String getName()
     {
         return "Nexus installation";
     }
 
     @Override
     public boolean isValid( ILaunchConfiguration config )
     {
         try
         {
             if ( config.getAttribute( ATTR_STANDARD_INSTALLATION, true ) )
             {
                 String installationId = config.getAttribute( ATTR_STANDARD_INSTALLATION_ID, (String) null );
                 return installations.getInstallation( installationId ) != null;
             }
             String location = config.getAttribute( ATTR_INSTALLATION_LOCATION, (String) null );
             if ( location == null || "".equals( location.trim() ) )
             {
                 return false;
             }
             return new File( location ).isDirectory();
         }
         catch ( CoreException e )
         {
             return false;
         }
     }
 
     void setStandard( boolean standard )
     {
         btnRadioStandard.setSelection( standard );
         standardInstallation.setEnabled( standard );
         // btnStandardAutoupdate.setEnabled( standard );
 
         btnRadioCustom.setSelection( !standard );
         installationLocation.setEnabled( !standard );
        // btnBrowseInstallationLocation.setEnabled( !standard );
     }
 }
