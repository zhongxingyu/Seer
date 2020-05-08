 package app.dialogs;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FontDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 import app.Application;
 import app.Preferences;
 
 public class PreferencesDialog extends Dialog {
 
 	private Shell shell;
 	private Label sampleSourceFont;
 	private Label sampleConsoleFont;
 	private Preferences prefs;
 	
 	public PreferencesDialog( Shell parentShell ) {
 		super( parentShell, SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE );
 		
 		prefs = Application.getInstance().getPreferences();
 	}
 	
 	protected void createContents( ) {
 
 		GridData hSpacer = new GridData( SWT.FILL, SWT.FILL, true, false );
 		hSpacer.minimumWidth = 80;
 		
 		GridData vSpacer = new GridData( SWT.FILL, SWT.FILL, true, true );
 		vSpacer.minimumHeight = 80;
 		
 		GridData fontLayoutData = new GridData( SWT.CENTER, SWT.FILL, true, false );
 		fontLayoutData.minimumWidth = 150;
 		
 		
 		shell.setLayout( new GridLayout( 2, false ) );
 		
 		TabFolder tabs = new TabFolder( shell, SWT.NONE );
 		tabs.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
 		/*
 		 * TabItem colours = new TabItem( tabs, SWT.None );
 		colours.setText( "Colours" );
 		*/
 		
 		
 		TabItem fonts = new TabItem( tabs, SWT.None );
 		fonts.setText( "Fonts" );
 		
 
 		Composite fontsArea = new Composite( tabs, SWT.NONE );
 		fonts.setControl( fontsArea );
 		
 		fontsArea.setLayout( new GridLayout( 4, false ) );
 		
 		new Label( fontsArea, SWT.LEFT ).setText( "Editor Font: " );
 		
 		sampleSourceFont = new Label( fontsArea, SWT.CENTER );
 		String fontFace = prefs.getString( "source.font.face");
 		int fontSize = prefs.getInt( "source.font.size" );
 		
 		sampleSourceFont.setText( fontFace );
 		sampleSourceFont.setFont( new Font( shell.getDisplay(), fontFace, fontSize, SWT.NONE ) );
 		sampleSourceFont.setLayoutData( fontLayoutData );
 		
 		new Label( fontsArea, SWT.NONE ).setLayoutData( hSpacer );
 		
 		Button changeEditorFontButton = new Button( fontsArea, SWT.PUSH );
 		changeEditorFontButton.setText( "Change Font" );
 		changeEditorFontButton.addSelectionListener( new SelectionListener() {
 			public void widgetSelected( SelectionEvent e ) {
 				FontDialog fd = new FontDialog( shell, SWT.NONE );
 		        fd.setText( "Select Font" );
 				String fontFace = prefs.getString( "source.font.face");
 				int fontSize = prefs.getInt( "source.font.size" );
 				
 		        FontData defaultFont = new FontData( fontFace, fontSize, SWT.BOLD);
 		        fd.setFontList( new FontData[] { defaultFont } );
 		        FontData newFont = fd.open();
 		        if (newFont == null) {
 		        	return;
 		        }
 		        sampleSourceFont.setFont( new Font( shell.getDisplay( ), newFont ) );
 		        sampleSourceFont.setText( newFont.getName() );
 			}
 			public void widgetDefaultSelected( SelectionEvent e ) {
 			}
 		} );
 		
 		
 		// Console Font
 		
 		new Label( fontsArea, SWT.LEFT ).setText( "Build Console Font: " );
 		
 		sampleConsoleFont = new Label( fontsArea, SWT.CENTER );
 		fontFace = prefs.getString( "console.font.face");
 		fontSize = prefs.getInt( "console.font.size" );
 		
 		sampleConsoleFont.setText( fontFace );
 		sampleConsoleFont.setFont( new Font( shell.getDisplay(), fontFace, fontSize, SWT.NONE ) );
 		sampleConsoleFont.setLayoutData( fontLayoutData );
 		
 		new Label( fontsArea, SWT.NONE ).setLayoutData( hSpacer );
 		
 		Button changeConsoleFontButton = new Button( fontsArea, SWT.PUSH );
 		changeConsoleFontButton.setText( "Change Font" );
 		changeConsoleFontButton.addSelectionListener( new SelectionListener() {
 			public void widgetSelected( SelectionEvent e ) {
 				FontDialog fd = new FontDialog( shell, SWT.NONE );
 		        fd.setText( "Select Font" );
 				String fontFace = prefs.getString( "console.font.face" );
 				int fontSize = prefs.getInt( "console.font.size" );
 				
 		        FontData defaultFont = new FontData( fontFace, fontSize, SWT.BOLD);
 		        fd.setFontList( new FontData[] { defaultFont } );
 		        FontData newFont = fd.open();
 		        if (newFont == null) {
 		        	return;
 		        }
 		        sampleConsoleFont.setFont( new Font( shell.getDisplay( ), newFont ) );
 		        sampleConsoleFont.setText( newFont.getName() );
 			}
 			public void widgetDefaultSelected( SelectionEvent e ) {
 			}
 		} );
 		
 		new Label( fontsArea, SWT.NONE ).setLayoutData( vSpacer );
 		
		new Label( fontsArea, SWT.LEFT ).setText( "Changes to fonts will take effect on the next launch of the program." );
 		
 		new Label( shell, SWT.NONE ).setLayoutData( hSpacer );
 		
 		Button save = new Button( shell, SWT.PUSH );
 		save.setText( "Save Preferences" );
 		save.addSelectionListener( new SelectionListener( ) {
 			public void widgetDefaultSelected( SelectionEvent e ) {	
 			}
 			public void widgetSelected( SelectionEvent e ) {
 				prefs.setFont( "console", sampleConsoleFont.getFont().getFontData()[0] );
 				prefs.setFont( "source", sampleSourceFont.getFont().getFontData()[0] );
 				prefs.save( );
 			}
 		} );
 		
 		
 	}
 	
 	private void createShell( ) {
 	    shell = new Shell( getParent( ), getStyle( ) );
 	    shell.setText( "Preferences" );
 	    createContents( );
 	    shell.pack( );
 	}
 	
 	public void close( ) {
 		shell.close();
 	}
 	
 	public void open( ) {
 		if ( shell == null || shell.isDisposed() ) {
 			createShell( );
 		}
 		
 	    shell.open( );
 	    
 	    Display display = getParent( ).getDisplay( );
 	    
 	    while ( !shell.isDisposed( ) ) {
 	      if ( !display.readAndDispatch( ) ) {
 	        display.sleep( );
 	      }
 	    }
 	}
 
 }
