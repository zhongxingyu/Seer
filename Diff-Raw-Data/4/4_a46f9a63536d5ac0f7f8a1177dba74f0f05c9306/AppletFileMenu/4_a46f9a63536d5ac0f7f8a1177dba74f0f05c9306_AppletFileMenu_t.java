 package com.lanl.application.TPTD.applet;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.forester.archaeopteryx.Configuration;
 import org.forester.archaeopteryx.Constants;
 import org.forester.archaeopteryx.ControlPanel;
 import org.forester.archaeopteryx.MainFrame;
 import org.forester.archaeopteryx.MainFrameApplet;
 import org.forester.archaeopteryx.MainFrameApplication;
 import org.forester.archaeopteryx.Options;
 import org.forester.archaeopteryx.TreePanel;
 import org.forester.archaeopteryx.Util;
 
 
 import org.forester.archaeopteryx.Options.PHYLOGENY_GRAPHICS_TYPE;
 import org.forester.archaeopteryx.webservices.PhylogeniesWebserviceClient;
 import org.forester.archaeopteryx.webservices.WebservicesManager;
 import org.forester.io.writers.PhylogenyWriter;
 import org.forester.phylogeny.Phylogeny;
 import org.forester.util.ForesterUtil;
 
 public class AppletFileMenu implements ActionListener {
 	JMenu                     _file_jmenu;
 	private JMenuItem                       _write_to_pdf_item;
     private JMenuItem                       _write_to_jpg_item;
     private JMenuItem                       _write_to_png_item;
     private JMenuItem                       _print_item;
     public final static NHFilter           nhfilter           = new NHFilter();
     public final static NHXFilter          nhxfilter          = new NHXFilter();
     public final static XMLFilter          xmlfilter          = new XMLFilter();
     public final static NexusFilter        nexusfilter        = new NexusFilter();
     public final static PdfFilter          pdffilter          = new PdfFilter();
     public final static GraphicsFileFilter graphicsfilefilter = new GraphicsFileFilter();
     private final JFileChooser              _save_filechooser;  
     private final JFileChooser              _writetopdf_filechooser;
     private final JFileChooser              _writetographics_filechooser;
     private File                            _open_dir;
    
     JMenuItem                 _save_item;
     
     final static String[]     DEFAULT_FONT_CHOICES                      = { "Verdana", "Tahoma", "Arial", "Helvetica",
         													"Dialog", "Lucida Sans", "SansSerif", "Sans-serif", "Sans" };
     final static Color        MENU_BACKGROUND_COLOR_DEFAULT             = new Color( 0, 0, 0 );
     final static Color        MENU_TEXT_COLOR_DEFAULT                   = new Color( 255, 255, 255 );
     private final static String[] AVAILABLE_FONT_FAMILIES_SORTED = GraphicsEnvironment.getLocalGraphicsEnvironment()
     .getAvailableFontFamilyNames();
     static {
     	Arrays.sort( AVAILABLE_FONT_FAMILIES_SORTED );
     }
     private static String                   DEFAULT_FONT_FAMILY           = "";
     static {
         for( final String font_name : DEFAULT_FONT_CHOICES ) {
             if ( Arrays.binarySearch( AVAILABLE_FONT_FAMILIES_SORTED, font_name ) >= 0 ) {
                 DEFAULT_FONT_FAMILY = font_name;
                 break;
             }
         }
         if ( ForesterUtil.isEmpty( DEFAULT_FONT_FAMILY ) ) {
             DEFAULT_FONT_FAMILY = DEFAULT_FONT_CHOICES[ DEFAULT_FONT_CHOICES.length - 1 ];
         }
     }
     final static String       PRG_NAME                                  = "Archaeopteryx";
     final static Font         menu_font                          = new Font( DEFAULT_FONT_FAMILY,
             														Font.PLAIN,
             														10 );
     
     public MainFrameApplet mainFrameApplet;
     
     public AppletFileMenu(MainFrameApplet mfa ){
     	this.mainFrameApplet = mfa;
     	_save_filechooser =new JFileChooser();
     	_save_filechooser.setCurrentDirectory( new File( "." ) );
         _save_filechooser.setMultiSelectionEnabled( false );
         _save_filechooser.setFileFilter( AppletFileMenu.xmlfilter );
         _save_filechooser.addChoosableFileFilter( AppletFileMenu.nhxfilter );
         _save_filechooser.addChoosableFileFilter( AppletFileMenu.nhfilter );
         _save_filechooser.addChoosableFileFilter( AppletFileMenu.nexusfilter );
         _save_filechooser.addChoosableFileFilter( _save_filechooser.getAcceptAllFileFilter() );
         
         
         _writetopdf_filechooser = new JFileChooser();
         _writetopdf_filechooser.addChoosableFileFilter( AppletFileMenu.pdffilter );
         _writetographics_filechooser = new JFileChooser();
         _writetographics_filechooser.addChoosableFileFilter( AppletFileMenu.graphicsfilefilter );
         
     }
 	public void buildFileMenu(JMenuBar _jmenubar,boolean conf_isUseNativeUI) {
 	    _file_jmenu = createMenu( "File", conf_isUseNativeUI );
 	    _file_jmenu.add( _save_item = new JMenuItem( "Save Tree As..." ) );
 	    _file_jmenu.addSeparator();
	    if(!AppletParams.isTreeDecoratorForAll()){
	    	_file_jmenu.add( _write_to_pdf_item = new JMenuItem( "Export to PDF file ..." ) );
	    }
 /*	    if ( Util.canWriteFormat( "tif" ) || Util.canWriteFormat( "tiff" ) || Util.canWriteFormat( "TIF" ) ) {
 	        _file_jmenu.add( _write_to_tif_item = new JMenuItem( "Export to TIFF file..." ) );
 	    }*/
 	    _file_jmenu.add( _write_to_png_item = new JMenuItem( "Export to PNG file..." ) );
 	    _file_jmenu.add( _write_to_jpg_item = new JMenuItem( "Export to JPG file..." ) );
 /*	    if ( Util.canWriteFormat( "gif" ) ) {
 	        _file_jmenu.add( _write_to_gif_item = new JMenuItem( "Export to GIF file..." ) );
 	    }
 	    if ( Util.canWriteFormat( "bmp" ) ) {
 	        _file_jmenu.add( _write_to_bmp_item = new JMenuItem( "Export to BMP file..." ) );
 	    }*/
 	    _file_jmenu.addSeparator();
 	    _file_jmenu.add( _print_item = new JMenuItem( "Print..." ) ); 
 	    
 	    
 	    customizeJMenuItem( _save_item,conf_isUseNativeUI );
 	    
 	    customizeJMenuItem( _write_to_pdf_item,conf_isUseNativeUI );
 	    customizeJMenuItem( _write_to_png_item,conf_isUseNativeUI );
 	    customizeJMenuItem( _write_to_jpg_item,conf_isUseNativeUI );
 	    customizeJMenuItem( _print_item,conf_isUseNativeUI );
 	    
 	    _jmenubar.add( _file_jmenu );
 	}
 	
 	static JMenu createMenu( final String title,boolean conf_isUseNativeUI ) {
         final JMenu jmenu = new JMenu( title );
         if ( !conf_isUseNativeUI ) {
             jmenu.setFont( menu_font );
             jmenu.setBackground(MENU_BACKGROUND_COLOR_DEFAULT );
             jmenu.setForeground(MENU_TEXT_COLOR_DEFAULT );
         }
         return jmenu;
     }
 	
 	void customizeJMenuItem( final JMenuItem jmi,boolean conf_isUseNativeUI ) {
         if ( jmi != null ) {
             jmi.setFont( menu_font );
             if ( !conf_isUseNativeUI ) {
                 jmi.setBackground( MENU_BACKGROUND_COLOR_DEFAULT );
                 jmi.setForeground( MENU_TEXT_COLOR_DEFAULT );
             }
             jmi.addActionListener( this );
         }
     }
 	
 	public void actionPerformed( final ActionEvent e ) {
         try {
             final Object o = e.getSource();
             
             if ( o == _save_item ) {
                 writeToFile( mainFrameApplet.get_main_panel().get_current_phylogeny() );
                 // If subtree currently displayed, save it, instead of complete
                 // tree.
             }
             else if ( o == _write_to_pdf_item ) {
                 printToPdf( mainFrameApplet.get_main_panel().get_current_phylogeny() );
             }
             else if ( o == _write_to_jpg_item ) {
                 writeToGraphicsFile( mainFrameApplet.get_main_panel().get_current_phylogeny(), GraphicsExportType.JPG );
             }
             else if ( o == _write_to_png_item ) {
                 writeToGraphicsFile( mainFrameApplet.get_main_panel().get_current_phylogeny(), GraphicsExportType.PNG );
             }
             else if ( o == _print_item ) {
                 print();
             }
             mainFrameApplet.get_content_pane().repaint();
         }
         catch ( final Exception ex ) {
         	unexpectedException(ex);
         }
         catch ( final Error err ) {
         	unexpectedError(err);
         }
     }
 	
 	private void writeToFile( final Phylogeny t ) {
 	    if ( t == null ) {
 	        return;
 	    }
 	    String initial_filename = null;
 	    if ( mainFrameApplet.get_main_panel().get_current_treePanel().get_tree_file() != null ) {
 	        try {
 	            initial_filename = mainFrameApplet.get_main_panel().get_current_treePanel().get_tree_file().getCanonicalPath();
 	        }
 	        catch ( final IOException e ) {
 	            initial_filename = null;
 	        }
 	    }
 	    if ( !ForesterUtil.isEmpty( initial_filename ) ) {
 	        _save_filechooser.setSelectedFile( new File( initial_filename ) );
 	    }
 	    else {
 	        _save_filechooser.setSelectedFile( new File( "" ) );
 	    }
 	    if ( _open_dir != null ) {
 	        _save_filechooser.setCurrentDirectory( _open_dir );
 	    }
 	    else {
 	        File dir = null;
 	        if ( System.getProperty( "user.home" ) != null ) {
 	            dir = new File( System.getProperty( "user.home" ) );
 	        }
 	        else if ( System.getProperty( "user.dir" ) != null ) {
 	            dir = new File( System.getProperty( "user.dir" ) );
 	        }
 	        _save_filechooser.setCurrentDirectory( dir );
 	    }
 	    final int result = _save_filechooser.showSaveDialog( mainFrameApplet.get_content_pane() );
 	    final File file = _save_filechooser.getSelectedFile();
 	    boolean exception = false;
 	    if ( ( file != null ) && ( result == JFileChooser.APPROVE_OPTION ) ) {
 	        if ( file.exists() ) {
 	            final int i = JOptionPane.showConfirmDialog( mainFrameApplet,
 	                                                         file + " already exists. Overwrite?",
 	                                                         "Warning",
 	                                                         JOptionPane.OK_CANCEL_OPTION,
 	                                                         JOptionPane.WARNING_MESSAGE );
 	            if ( i != JOptionPane.OK_OPTION ) {
 	                return;
 	            }
 	        }
 	        if ( _save_filechooser.getFileFilter() == nhfilter ) {
 	            exception = writeAsNewHampshire( t, exception, file );
 	        }
 	        else if ( _save_filechooser.getFileFilter() == nhxfilter ) {
 	            exception = writeAsNHX( t, exception, file );
 	        }
 	        else if ( _save_filechooser.getFileFilter() == xmlfilter ) {
 	            exception = writeAsPhyloXml( t, exception, file );
 	        }
 	        else if ( _save_filechooser.getFileFilter() == nexusfilter ) {
 	            exception = writeAsNexus( t, exception, file );
 	        }
 	        // "*.*":
 	        else {
 	            final String file_name = file.getName().trim().toLowerCase();
 	            if ( file_name.endsWith( ".nh" ) || file_name.endsWith( ".newick" ) || file_name.endsWith( ".phy" )
 	                    || file_name.endsWith( ".tree" ) ) {
 	                exception = writeAsNewHampshire( t, exception, file );
 	            }
 	            else if ( file_name.endsWith( ".nhx" ) ) {
 	                exception = writeAsNHX( t, exception, file );
 	            }
 	            else if ( file_name.endsWith( ".nex" ) || file_name.endsWith( ".nexus" ) ) {
 	                exception = writeAsNexus( t, exception, file );
 	            }
 	            // XML is default:
 	            else {
 	                exception = writeAsPhyloXml( t, exception, file );
 	            }
 	        }
 	        if ( !exception ) {
 	        	mainFrameApplet.get_main_panel().get_current_treePanel().set_tree_file( file );
 	        }
 	    }
 	}
 	private boolean writeAsNewHampshire( final Phylogeny t, boolean exception, final File file ) {
 	    try {
 	        final PhylogenyWriter writer = new PhylogenyWriter();
 	        writer.toNewHampshire( t, false, true, file );
 	    }
 	    catch ( final Exception e ) {
 	        exception = true;
 	        exceptionOccuredDuringSaveAs( e );
 	    }
 	    return exception;
 	}
 	private boolean writeAsNexus( final Phylogeny t, boolean exception, final File file ) {
 	    try {
 	        final PhylogenyWriter writer = new PhylogenyWriter();
 	        writer.toNexus( file, t );
 	    }
 	    catch ( final Exception e ) {
 	        exception = true;
 	        exceptionOccuredDuringSaveAs( e );
 	    }
 	    return exception;
 	}
 	private boolean writeAsNHX( final Phylogeny t, boolean exception, final File file ) {
 	    try {
 	        final PhylogenyWriter writer = new PhylogenyWriter();
 	        writer.toNewHampshireX( t, file );
 	    }
 	    catch ( final Exception e ) {
 	        exception = true;
 	        exceptionOccuredDuringSaveAs( e );
 	    }
 	    return exception;
 	}
 	private boolean writeAsPhyloXml( final Phylogeny t, boolean exception, final File file ) {
 	    try {
 	        final PhylogenyWriter writer = new PhylogenyWriter();
 	        writer.toPhyloXML( file, t, 0 );
 	    }
 	    catch ( final Exception e ) {
 	        exception = true;
 	        exceptionOccuredDuringSaveAs( e );
 	    }
 	    return exception;
 	}
 	void exceptionOccuredDuringSaveAs( final Exception e ) {
         try {
         	mainFrameApplet.get_main_panel().get_current_treePanel().set_arrow_cursor();
         }
         catch ( final Exception ex ) {
             // Do nothing.
         }
         JOptionPane.showMessageDialog( mainFrameApplet, "Exception" + e, "Error during File|SaveAs", JOptionPane.ERROR_MESSAGE );
     }
 	private void printToPdf( final Phylogeny t ) {
 	    if ( ( t == null ) || t.isEmpty() ) {
 	        return;
 	    }
 	    if ( ( mainFrameApplet.get_main_panel().get_current_treePanel().get_phylogeny_graphicsType() == PHYLOGENY_GRAPHICS_TYPE.CONVEX )
 	            || ( mainFrameApplet.get_main_panel().get_current_treePanel().get_phylogeny_graphicsType() == PHYLOGENY_GRAPHICS_TYPE.CURVED ) ) {
 	        JOptionPane.showMessageDialog( mainFrameApplet,
 	                                       "Cannot export this graphic type to PDF.",
 	                                       "Cannot export to PDF",
 	                                       JOptionPane.ERROR_MESSAGE );
 	        return;
 	    }
 	    String initial_filename = "";
 	    if ( mainFrameApplet.get_main_panel().get_current_treePanel().get_tree_file() != null ) {
 	        initial_filename = mainFrameApplet.get_main_panel().get_current_treePanel().get_tree_file().toString();
 	    }
 	    if ( initial_filename.indexOf( '.' ) > 0 ) {
 	        initial_filename = initial_filename.substring( 0, initial_filename.indexOf( '.' ) );
 	    }
 	    initial_filename = initial_filename + ".pdf";
 	    _writetopdf_filechooser.setSelectedFile( new File( initial_filename ) );
 	    final int result = _writetopdf_filechooser.showSaveDialog( mainFrameApplet.get_content_pane() );
 	    File file = _writetopdf_filechooser.getSelectedFile();
 	    if ( ( file != null ) && ( result == JFileChooser.APPROVE_OPTION ) ) {
 	        if ( !file.toString().toLowerCase().endsWith( ".pdf" ) ) {
 	            file = new File( file.toString() + ".pdf" );
 	        }
 	        if ( file.exists() ) {
 	            final int i = JOptionPane.showConfirmDialog( mainFrameApplet,
 	                                                         file + " already exists. Overwrite?",
 	                                                         "WARNING",
 	                                                         JOptionPane.OK_CANCEL_OPTION,
 	                                                         JOptionPane.WARNING_MESSAGE );
 	            if ( i != JOptionPane.OK_OPTION ) {
 	                return;
 	            }
 	        }
 	        printPhylogenyToPdf( file.toString() );
 	    }
 	}
 	private void printPhylogenyToPdf( final String file_name ) {
 	    if ( !mainFrameApplet.get_options().is_print_usingActualSize() ) {
 	    	mainFrameApplet.get_main_panel().get_current_treePanel().setParametersForPainting( mainFrameApplet.get_options().get_print_sizeX(),
 	    			mainFrameApplet.get_options().get_print_sizeY(),
 	                                                        true );
 	    	mainFrameApplet.get_main_panel().get_current_treePanel().resetPreferredSize();
 	    	mainFrameApplet.get_main_panel().get_current_treePanel().repaint();
 	    }
 	    String pdf_written_to = "";
 	    boolean error = false;
 	    try {
 	        if ( mainFrameApplet.get_options().is_print_usingActualSize() ) {
 	            pdf_written_to = AppletPdfWriter.writePhylogenyToPdf( file_name,
 	            												mainFrameApplet.get_main_panel().get_current_treePanel(),
 	                                                            mainFrameApplet.get_options(),
 	                                                            mainFrameApplet.get_main_panel().get_current_treePanel().getWidth(),
 	                                                            mainFrameApplet.get_main_panel().get_current_treePanel().getHeight() );
 	        }
 	        else {
 	            pdf_written_to = AppletPdfWriter.writePhylogenyToPdf( file_name,
 	            												mainFrameApplet.get_main_panel().get_current_treePanel(),
 	                                                            mainFrameApplet.get_options(),
 	                                                            mainFrameApplet.get_options().get_print_sizeX(),
 	                                                            mainFrameApplet.get_options().get_print_sizeY() );
 	        }
 	    }
 	    catch ( final IOException e ) {
 	        error = true;
 	        JOptionPane.showMessageDialog( mainFrameApplet, e.toString(), "Error", JOptionPane.ERROR_MESSAGE );
 	    }
 	    if ( !error ) {
 	        if ( !ForesterUtil.isEmpty( pdf_written_to ) ) {
 	            JOptionPane.showMessageDialog( mainFrameApplet,
 	                                           "Wrote PDF to: " + pdf_written_to,
 	                                           "Information",
 	                                           JOptionPane.INFORMATION_MESSAGE );
 	        }
 	        else {
 	            JOptionPane.showMessageDialog( mainFrameApplet,
 	                                           "There was an unknown problem when attempting to write to PDF file: \""
 	                                                   + file_name + "\"",
 	                                           "Error",
 	                                           JOptionPane.ERROR_MESSAGE );
 	        }
 	    }
 	    if ( !mainFrameApplet.get_options().is_print_usingActualSize() ) {
 	    	mainFrameApplet.get_main_panel().get_control_panel().show_whole();
 	    }
 	}
 	private void writeToGraphicsFile( final Phylogeny t, final GraphicsExportType type ) {
 	    if ( ( t == null ) || t.isEmpty() ) {
 	        return;
 	    }
 	    String initial_filename = "";
 	    if ( mainFrameApplet.get_main_panel().get_current_treePanel().get_tree_file() != null ) {
 	        initial_filename = mainFrameApplet.get_main_panel().get_current_treePanel().get_tree_file().toString();
 	    }
 	    if ( initial_filename.indexOf( '.' ) > 0 ) {
 	        initial_filename = initial_filename.substring( 0, initial_filename.indexOf( '.' ) );
 	    }
 	    initial_filename = initial_filename + "." + type;
 	    _writetographics_filechooser.setSelectedFile( new File( initial_filename ) );
 	    final int result = _writetographics_filechooser.showSaveDialog( mainFrameApplet.get_content_pane() );
 	    File file = _writetographics_filechooser.getSelectedFile();
 	    if ( ( file != null ) && ( result == JFileChooser.APPROVE_OPTION ) ) {
 	        if ( !file.toString().toLowerCase().endsWith( type.toString() ) ) {
 	            file = new File( file.toString() + "." + type );
 	        }
 	        if ( file.exists() ) {
 	            final int i = JOptionPane.showConfirmDialog( mainFrameApplet,
 	                                                         file + " already exists. Overwrite?",
 	                                                         "Warning",
 	                                                         JOptionPane.OK_CANCEL_OPTION,
 	                                                         JOptionPane.WARNING_MESSAGE );
 	            if ( i != JOptionPane.OK_OPTION ) {
 	                return;
 	            }
 	        }
 	        writePhylogenyToGraphicsFile( file.toString(), type );
 	    }
 	}
 	
 	private void writePhylogenyToGraphicsFile( final String file_name, final GraphicsExportType type ) {
 		mainFrameApplet.get_main_panel().get_current_treePanel().setParametersForPainting( mainFrameApplet.get_main_panel().get_current_treePanel().getWidth(),
 	    															mainFrameApplet.get_main_panel().get_current_treePanel().getHeight(),
 	                                                               true );
 	    String file_written_to = "";
 	    boolean error = false;
 	    try {
 	        file_written_to = writePhylogenyToGraphicsFile( file_name,
 	        													mainFrameApplet.get_main_panel().get_current_treePanel().getWidth(),
 	        													mainFrameApplet.get_main_panel().get_current_treePanel().getHeight(),
 	        													mainFrameApplet.get_main_panel().get_current_treePanel(),
 	        													mainFrameApplet.get_main_panel().get_control_panel(),
 	                                                             type,
 	                                                             mainFrameApplet.get_options());
 	    }
 	    catch ( final IOException e ) {
 	        error = true;
 	        JOptionPane.showMessageDialog( mainFrameApplet, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
 	    }
 	    if ( !error ) {
 	        if ( ( file_written_to != null ) && ( file_written_to.length() > 0 ) ) {
 	            JOptionPane.showMessageDialog( mainFrameApplet,
 	                                           "Wrote image to: " + file_written_to,
 	                                           "Graphics Export",
 	                                           JOptionPane.INFORMATION_MESSAGE );
 	        }
 	        else {
 	            JOptionPane.showMessageDialog( mainFrameApplet,
 	                                           "There was an unknown problem when attempting to write to an image file: \""
 	                                                   + file_name + "\"",
 	                                           "Error",
 	                                           JOptionPane.ERROR_MESSAGE );
 	        }
 	    }
 	    mainFrameApplet.get_content_pane().repaint();
 	}
 
 	
 	private void print() {
 	    if ( ( mainFrameApplet.get_main_panel().get_current_treePanel() == null ) ||
 	    		( mainFrameApplet.get_main_panel().get_current_treePanel().getCurrentPhylogeny() == null )
 	            || mainFrameApplet.get_main_panel().get_current_treePanel().getCurrentPhylogeny().isEmpty() ) {
 	        return;
 	    }
 	    if ( !mainFrameApplet.get_options().is_print_usingActualSize() ) {
 	    	mainFrameApplet.get_main_panel().get_current_treePanel().setParametersForPainting( mainFrameApplet.get_options().get_print_sizeX() - 80,
 	    												mainFrameApplet.get_options().get_print_sizeY() - 140,
 	                                                        true );
 	    	mainFrameApplet.get_main_panel().get_current_treePanel().resetPreferredSize();
 	    	mainFrameApplet.get_main_panel().get_current_treePanel().repaint();
 	    }
 	    final String job_name = PRG_NAME;
 	    boolean error = false;
 	    String printer_name = null;
 	    try {
 	        printer_name = AppletPrinter.print( mainFrameApplet.get_main_panel().get_current_treePanel(), job_name );
 	    }
 	    catch ( final Exception e ) {
 	        error = true;
 	        JOptionPane.showMessageDialog( mainFrameApplet, e.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE );
 	    }
 	    if ( !error && ( printer_name != null ) ) {
 	        String msg = "Printing data sent to printer";
 	        if ( printer_name.length() > 1 ) {
 	            msg += " [" + printer_name + "]";
 	        }
 	        JOptionPane.showMessageDialog( mainFrameApplet, msg, "Printing...", JOptionPane.INFORMATION_MESSAGE );
 	    }
 	    if ( !mainFrameApplet.get_options().is_print_usingActualSize() ) {
 	    	mainFrameApplet.get_main_panel().get_control_panel().show_whole();
 	    }
 	}
 	static String writePhylogenyToGraphicsFile( final String file_name,
 	                                            int width,
 	                                            int height,
 	                                            final TreePanel tree_panel,
 	                                            final ControlPanel ac,
 	                                            final GraphicsExportType type,
 	                                            final Options options ) throws IOException {
 	    if ( !options.is_graphics_exportUsingActualSize() ) {
 	        if ( options.is_graphics_exportVisibleOnly() ) {
 	            throw new IllegalArgumentException( "cannot export visible rectangle only without exporting in actual size" );
 	        }
 	        tree_panel.setParametersForPainting( options.get_print_sizeX(), options.get_print_sizeY(), true );
 	        tree_panel.resetPreferredSize();
 	        tree_panel.repaint();
 	    }
 	    final RenderingHints rendering_hints = new RenderingHints( RenderingHints.KEY_RENDERING,
 	                                                               RenderingHints.VALUE_RENDER_QUALITY );
 	    rendering_hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );
 	    if ( options.is_antialias_print() ) {
 	        rendering_hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
 	        rendering_hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
 	    }
 	    else {
 	        rendering_hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
 	        rendering_hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
 	    }
 	    final Phylogeny phylogeny = tree_panel.getCurrentPhylogeny();
 	    if ( ( phylogeny == null ) || phylogeny.isEmpty() ) {
 	        return "";
 	    }
 	    final File file = new File( file_name );
 	    if ( file.isDirectory() ) {
 	        throw new IOException( "\"" + file_name + "\" is a directory" );
 	    }
 	    Rectangle visible = null;
 	    if ( !options.is_graphics_exportUsingActualSize() ) {
 	        width = options.get_print_sizeX();
 	        height = options.get_print_sizeY();
 	    }
 	    else if ( options.is_graphics_exportVisibleOnly() ) {
 	        visible = tree_panel.getVisibleRect();
 	        width = visible.width;
 	        height = visible.height;
 	    }
 	    final BufferedImage buffered_img = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
 	    Graphics2D g2d = buffered_img.createGraphics();
 	    g2d.setRenderingHints( rendering_hints );
 	    int x = 0;
 	    int y = 0;
 	    if ( options.is_graphics_exportVisibleOnly() ) {
 	        g2d = ( Graphics2D ) g2d.create( -visible.x, -visible.y, visible.width, visible.height );
 	        g2d.setClip( null );
 	        x = visible.x;
 	        y = visible.y;
 	    }
 	    tree_panel.paintPhylogeny( g2d, false, true, width, height, x, y );
 	    ImageIO.write( buffered_img, type.toString(), file );
 	    g2d.dispose();
 	    System.gc();
 	    if ( !options.is_graphics_exportUsingActualSize() ) {
 	        tree_panel.getMainPanel().get_control_panel().show_whole();
 	    }
 	    String msg = file.toString();
 	    if ( ( width > 0 ) && ( height > 0 ) ) {
 	        msg += " [size: " + width + ", " + height + "]";
 	    }
 	    return msg;
 	}
 
 
 	static enum GraphicsExportType {
         GIF( "gif" ), JPG( "jpg" ), PDF( "pdf" ), PNG( "png" ), TIFF( "tif" ), BMP( "bmp" );
 
         private final String _suffix;
 
         private GraphicsExportType( final String suffix ) {
             _suffix = suffix;
         }
 
         @Override
         public String toString() {
             return _suffix;
         }
     }
 	
 	static void unexpectedError( final Error err ) {
         err.printStackTrace();
         final StringBuffer sb = new StringBuffer();
         for( final StackTraceElement s : err.getStackTrace() ) {
             sb.append( s + "\n" );
         }
         JOptionPane
                 .showMessageDialog( null,
                                     "An unexpected (possibly severe) error has occured - terminating. \n"+ 
                                     "Error: " + err + "\n" + sb,
                                     "Unexpected Severe Error",
                                     JOptionPane.ERROR_MESSAGE );
         System.exit( -1 );
     }
 	
 	static void unexpectedException( final Exception ex ) {
         ex.printStackTrace();
         final StringBuffer sb = new StringBuffer();
         for( final StackTraceElement s : ex.getStackTrace() ) {
             sb.append( s + "\n" );
         }
         JOptionPane.showMessageDialog( null, "An unexpected exception has occured.\n " +
         							"Exception: " + ex + "\n" + sb, 
         							"Unexpected Exception",
         							JOptionPane.ERROR_MESSAGE );
     }
 	
 	
 } //AppletFileMenu
 
 class GraphicsFileFilter extends FileFilter {
 
     @Override
     public boolean accept( final File f ) {
         final String file_name = f.getName().trim().toLowerCase();
         return file_name.endsWith( ".jpg" ) || file_name.endsWith( ".jpeg" ) || file_name.endsWith( ".png" )
                 || file_name.endsWith( ".gif" ) || file_name.endsWith( ".bmp" ) || f.isDirectory();
     }
 
     @Override
     public String getDescription() {
         return "Image files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)";
     }
 }
 class NexusFilter extends FileFilter {
 
     @Override
     public boolean accept( final File f ) {
         final String file_name = f.getName().trim().toLowerCase();
         return file_name.endsWith( ".nex" ) || file_name.endsWith( ".nexus" ) || file_name.endsWith( ".nx" )
                 || file_name.endsWith( ".tre" ) || f.isDirectory();
     }
 
     @Override
     public String getDescription() {
         return "Nexus files (*.nex, *.nexus, *.nx, *.tre)";
     }
 } // NexusFilter
 
 class NHFilter extends FileFilter {
 
     @Override
     public boolean accept( final File f ) {
         final String file_name = f.getName().trim().toLowerCase();
         return file_name.endsWith( ".nh" ) || file_name.endsWith( ".newick" ) || file_name.endsWith( ".phy" )
                 || file_name.endsWith( ".tr" ) || file_name.endsWith( ".tree" ) || file_name.endsWith( ".dnd" )
                 || f.isDirectory();
     }
 
     @Override
     public String getDescription() {
         return "New Hampshire - Newick files (*.nh, *.newick, *.phy, *.tree, *.dnd, *.tr)";
     }
 } // NHFilter
 
 class NHXFilter extends FileFilter {
 
     @Override
     public boolean accept( final File f ) {
         final String file_name = f.getName().trim().toLowerCase();
         return file_name.endsWith( ".nhx" ) || f.isDirectory();
     }
 
     @Override
     public String getDescription() {
         return "NHX files (*.nhx)";
     }
 }
 
 class PdfFilter extends FileFilter {
 
     public boolean accept( final File f ) {
         return f.getName().trim().toLowerCase().endsWith( ".pdf" ) || f.isDirectory();
     }
 
     @Override
     public String getDescription() {
         return "PDF files (*.pdf)";
     }
 } // PdfFilter
 
 class TolFilter extends FileFilter {
 
     @Override
     public boolean accept( final File f ) {
         final String file_name = f.getName().trim().toLowerCase();
         return ( file_name.endsWith( ".tol" ) || file_name.endsWith( ".tolxml" ) || file_name.endsWith( ".zip" ) || f
                 .isDirectory() )
                 && ( !file_name.endsWith( ".xml.zip" ) );
     }
 
     @Override
     public String getDescription() {
         return "Tree of Life files (*.tol, *.tolxml)";
     }
 } // TolFilter
 
 class XMLFilter extends FileFilter {
 
     @Override
     public boolean accept( final File f ) {
         final String file_name = f.getName().trim().toLowerCase();
         return file_name.endsWith( ".xml" ) || file_name.endsWith( ".phyloxml" ) || file_name.endsWith( ".px" )
                 || file_name.endsWith( ".pxml" ) || file_name.endsWith( ".zip" ) || f.isDirectory();
     }
 
     @Override
     public String getDescription() {
         return "phyloXML files (*.xml, *.phyloxml)";
     }
 } // XMLFilter
