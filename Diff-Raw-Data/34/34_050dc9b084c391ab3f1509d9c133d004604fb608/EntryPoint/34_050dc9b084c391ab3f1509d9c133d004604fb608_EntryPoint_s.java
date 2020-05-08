 /*******************************************************************************
  * Copyright (c) 2009 Innoopract Informationssysteme GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Innoopract Informationssysteme GmbH - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rwt.widgets.styledtext.demo;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.rwt.graphics.Graphics;
 import org.eclipse.rwt.lifecycle.IEntryPoint;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 public class EntryPoint implements IEntryPoint {
 
   private static final String TXT =
     "Eclipse is an open source community whose projects are focused on building \n" +
     "an open development platform comprised of extensible frameworks, tools \n" +
     "and runtimes for building, deploying and managing software across the \n" +
     "lifecycle. (We started with the best Java IDE ever and we've grown \n" +
     "from there.)";
 
  private static final Color BG_COLOR_GREEN = Graphics.getColor( 154, 205, 50 );
  private static final Color BG_COLOR_BLUE = Graphics.getColor( 105, 89, 205 );
  private static final Color BG_COLOR_BROWN = Graphics.getColor( 192, 172, 137 );
  private static final Color FG_COLOR_RED = Graphics.getColor( 194, 0, 23 );
  private static final Color FG_COLOR_BLUE = Graphics.getColor( 28, 96, 141 );
  private static final Color FG_COLOR_ORANGE = Graphics.getColor( 249, 158, 0 );

   private Shell mainShell;
   private Composite widgetContainer;
   private Composite styleComp;
   private Text logger;
   private final StringBuffer content = new StringBuffer();
   private StyledText styledText;
   private boolean visible = true;
   protected Color[] bgColors;
   protected Color[] fgColors;
   private int fgIndex;
   private int bgIndex;
 
   public int createUI() {
    Display display = PlatformUI.createDisplay();
 
     mainShell = new Shell( display, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX );
     mainShell.setLayout( new FillLayout() );
     mainShell.setText( "StyledText Demo" );
 
     bgColors = new Color[] {
       null,
      BG_COLOR_GREEN,
      BG_COLOR_BLUE,
      BG_COLOR_BROWN
     };
     fgColors = new Color[] {
       null,
      FG_COLOR_RED,
      FG_COLOR_BLUE,
      FG_COLOR_ORANGE
     };
 
     createContent( mainShell );
 
     mainShell.addShellListener( new ShellAdapter() {
 
       public void shellClosed( ShellEvent e ) {
         mainShell.dispose();
       }
     } );
 
    mainShell.setBounds( 100, 50, 800, 650 );
     mainShell.open();
     while( !mainShell.isDisposed() ) {
       if( !display.readAndDispatch() ) {
         display.sleep();
       }
     }
 
     return 0;
   }
 
   private void createContent( final Composite parent ) {
     Composite container = new Composite( parent, SWT.BORDER );
 
     container.setLayout( new GridLayout( 2, false ) );
 
     // Container for widget
     widgetContainer = new Composite( container, SWT.NONE );
     widgetContainer.setLayout( new FillLayout() );
     GridDataFactory.fillDefaults().grab( true, true ).applyTo( widgetContainer );
 
     // Container for widget settings
     styleComp = new Composite( container, SWT.BORDER );
     styleComp.setLayout( new RowLayout( SWT.VERTICAL ) );
     GridDataFactory.fillDefaults().applyTo( styleComp );
 
     // Log text area
     logger = new Text( container, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI );
     logger.setText( "---" );
     GridData loggerGridData = new GridData();
     loggerGridData.heightHint = 40;
     loggerGridData.horizontalSpan = 2;
     loggerGridData.grabExcessHorizontalSpace = true;
     loggerGridData.horizontalAlignment = SWT.FILL;
     logger.setLayoutData( loggerGridData );
 
     createStyleButton( "SWT.BORDER", SWT.BORDER, true );
     createVisibilityButton();
     createTextGroup();
     createSelectionGroup();
     createStyleRangesGroup();
     createFgColorButton();
     createBgColorButton();
 
     createNew();
   }
 
   protected void createNew() {
     Control[] controls = widgetContainer.getChildren();
     for( int i = 0; i < controls.length; i++ ) {
       controls[ i ].dispose();
     }
 
     styledText = new StyledText( widgetContainer, getStyle() );
     styledText.setText( TXT );
     styledText.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent e ) {
         log( "SWT.Selection event: " + e );
       }
     } );
     styledText.addMouseListener( new MouseAdapter() {
       public void mouseDown( final MouseEvent e ) {
         log( "SWT.MouseDown event: " + e );
       }
       public void mouseUp( final MouseEvent e ) {
         log( "SWT.MouseUp event: " + e );
       }
     } );
     widgetContainer.layout();
   }
 
   private void createSelectionGroup() {
     Group group = new Group( styleComp, SWT.NONE );
    group.setLayoutData( new RowData( 270, 70 ) );
     group.setLayout( new RowLayout( SWT.VERTICAL ) );
     group.setText( "Selection" );
 
     createSelectionChooser( group );
 
     Button selectAllButton = new Button( group, SWT.PUSH );
     selectAllButton.setText( "Select all" );
     selectAllButton.addSelectionListener( new SelectionAdapter() {
 
       public void widgetSelected( final SelectionEvent e ) {
         styledText.selectAll();
       }
     } );
   }
 
   private void createStyleRangesGroup() {
     Group group = new Group( styleComp, SWT.NONE );
    group.setLayoutData( new RowData( 270, 150 ) );
     group.setLayout( new GridLayout( 2, false ) );
     group.setText( "Style Range" );
 
     Composite composite = new Composite( group, SWT.NONE );
     GridData data = new GridData( GridData.BEGINNING,
                                   GridData.CENTER,
                                   true,
                                   false,
                                   2,
                                   1 );
     composite.setLayoutData( data );
     composite.setLayout( new GridLayout( 5, false ) );
     Label lblStart = new Label( composite, SWT.NONE );
     lblStart.setText( "Start" );
     lblStart.setLayoutData( new GridData( 30, 15 ) );
     final Text txtStart = new Text( composite, SWT.BORDER );
     Label lblLength = new Label( composite, SWT.NONE );
     lblLength.setText( "Length" );
     lblLength.setLayoutData( new GridData( 35, 15 ) );
     final Text txtLength = new Text( composite, SWT.BORDER );
 
     final Button foreground = new Button( group, SWT.PUSH );
     foreground.setText( "Foreground" );
     foreground.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         fgIndex = ( fgIndex + 1 ) % fgColors.length;
         foreground.setBackground( fgColors[ fgIndex ] );
       }
     } );
 
     final Button background = new Button( group, SWT.PUSH );
     background.setText( "Background" );
     background.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         bgIndex = ( bgIndex + 1 ) % bgColors.length;
         background.setBackground( bgColors[ bgIndex ] );
       }
     } );
 
     final Button bold = new Button( group, SWT.CHECK );
     bold.setText( "Bold" );
 
     final Button italic = new Button( group, SWT.CHECK );
     italic.setText( "Italic" );
 
     final Button underline = new Button( group, SWT.CHECK );
     underline.setText( "Underline" );
 
     final Button strikeout = new Button( group, SWT.CHECK );
     strikeout.setText( "Strikeout" );
 
     Button setStyleButton = new Button( group, SWT.PUSH );
     setStyleButton.setText( "Set style" );
     setStyleButton.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( SelectionEvent e ) {
         StyleRange style = new StyleRange();
 
         int start = parseInt( txtStart.getText() );
         int length = parseInt( txtLength.getText() );
         if( start >= 0 && length >= 0  ) {
           style.start = start;
           style.length = length;
 
           style.foreground = fgColors[ fgIndex ];
           style.background = bgColors[ bgIndex ];
           if( bold.getSelection() ) {
             style.fontStyle |= SWT.BOLD;
           }
           if( italic.getSelection() ) {
             style.fontStyle |= SWT.ITALIC;
           }
           style.underline = underline.getSelection();
           style.strikeout = strikeout.getSelection();
 
           try {
             styledText.setStyleRange( style );
           } catch( IllegalArgumentException ex ) {
             String msg = "Invalid range. Range out of bounds 0 - "
                          + styledText.getCharCount();
             MessageDialog.openError( styledText.getShell(), "Error", msg );
           }
         }
       }
     } );
 
     Button clearStylesButton = new Button( group, SWT.PUSH );
     clearStylesButton.setText( "Clear styles" );
     clearStylesButton.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( SelectionEvent e ) {
         styledText.setStyleRange( null );
       }
     } );
   }
 
   protected int getStyle() {
     int result = SWT.NONE;
     Control[] ctrls = this.styleComp.getChildren();
     if( ctrls.length == 0 ) {
       result = SWT.NONE;
     } else {
       for( int i = 0; i < ctrls.length; i++ ) {
         if( ctrls[ i ] instanceof Button ) {
           Button button = ( Button )ctrls[ i ];
           if( button.getSelection() ) {
             Object data = button.getData( "style" );
             if( data != null && data instanceof Integer ) {
               int style = ( ( Integer )data ).intValue();
               result |= style;
             }
           }
         }
       }
     }
     return result;
   }
 
   protected Button createStyleButton( final String name,
                                       final int style,
                                       final boolean checked )
   {
     Button button = new Button( this.styleComp, SWT.CHECK );
     button.setText( name );
     button.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         createNew();
       }
     } );
     button.setData( "style", new Integer( style ) );
     button.setSelection( checked );
     return button;
   }
 
   protected Button createVisibilityButton() {
     final Button button = new Button( styleComp, SWT.CHECK );
     button.setText( "Visible" );
     button.setSelection( visible );
     button.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         visible = button.getSelection();
         styledText.setVisible( visible );
       }
     } );
     return button;
   }
 
   private void createTextGroup() {
     Group group = new Group( styleComp, SWT.NONE );
    group.setLayoutData( new RowData( 270, 145 ) );
     group.setLayout( new RowLayout( SWT.VERTICAL ) );
     group.setText( "Text" );
 
     Composite composite = new Composite( group, SWT.NONE );
     composite.setLayout( new GridLayout( 2, false ) );
     final Text text = new Text( composite, SWT.MULTI | SWT.BORDER );
     text.setLayoutData( new GridData( 220, 100 ) );
     Button btnChange = new Button( composite, SWT.PUSH );
     btnChange.setText( "Set" );
     btnChange.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         styledText.setText( text.getText() );
       }
     } );
     Button btnHugeText = new Button( composite, SWT.PUSH );
     btnHugeText.setText( "Set huge text" );
     btnHugeText.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         StringBuffer txt = new StringBuffer();
         int n = 5000;
         for( int i = 1; i <= n; i++ ) {
           txt.append( "This is line " + i + " of " + n + ". Thanks for reading.\n" );
         }
         styledText.setText( txt.toString() );
       }
     } );
   }
 
   private void createSelectionChooser( final Composite parent ) {
     Composite composite = new Composite( parent, SWT.NONE );
     composite.setLayout( new GridLayout( 5, false ) );
     Label lblSelectionFrom = new Label( composite, SWT.NONE );
     lblSelectionFrom.setText( "From" );
     lblSelectionFrom.setLayoutData( new GridData( 25, 15 ) );
     final Text txtSelectionFrom = new Text( composite, SWT.BORDER );
     Label lblSelectionTo = new Label( composite, SWT.NONE );
     lblSelectionTo.setText( "To" );
     lblSelectionTo.setLayoutData( new GridData( 15, 15 ) );
     final Text txtSelectionTo = new Text( composite, SWT.BORDER );
     Button btnChange = new Button( composite, SWT.PUSH );
     btnChange.setText( "Set" );
     btnChange.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         int from = parseInt( txtSelectionFrom.getText() );
         int to = parseInt( txtSelectionTo.getText() );
         if( to >= 0 && from <= to  ) {
           styledText.setSelection( from, to );
         }
       }
     } );
   }
 
   private Button createFgColorButton() {
     final Button button = new Button( styleComp, SWT.PUSH );
     button.setText( "Foreground" );
     button.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         fgIndex = ( fgIndex + 1 ) % fgColors.length;
         styledText.setForeground( fgColors[ fgIndex ] );
       }
 
     } );
     return button;
   }
 
   private Button createBgColorButton() {
     final Button button = new Button( styleComp, SWT.PUSH );
     button.setText( "Background" );
     button.addSelectionListener( new SelectionAdapter() {
       public void widgetSelected( final SelectionEvent event ) {
         bgIndex = ( bgIndex + 1 ) % bgColors.length;
         styledText.setBackground( bgColors[ bgIndex ] );
       }
     } );
     return button;
   }
 
   private int parseInt( final String text ) {
     int result;
     try {
       result = Integer.parseInt( text );
     } catch( NumberFormatException e ) {
       result = -1;
     }
     return result;
   }
 
   protected void log( final String msg ) {
     content.insert( 0, msg.trim() + logger.getLineDelimiter() );
     logger.setText( content.toString() );
   }
 }
