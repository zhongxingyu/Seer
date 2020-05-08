 package view.component;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.swing.ActionMap;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import model.figure.DrawFigureFactory;
 import net.imglib2.RandomAccessibleInterval;
 import net.imglib2.RealRandomAccessible;
 import net.imglib2.converter.RealARGBConverter;
 import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
 import net.imglib2.realtransform.AffineTransform2D;
 import net.imglib2.type.NativeType;
 import net.imglib2.type.numeric.RealType;
 import net.imglib2.type.numeric.integer.LongType;
 import net.imglib2.type.numeric.real.DoubleType;
 import net.imglib2.view.IntervalView;
 import net.imglib2.view.Views;
 
 import org.jhotdraw.app.action.edit.RedoAction;
 import org.jhotdraw.app.action.edit.UndoAction;
 import org.jhotdraw.draw.DefaultDrawingEditor;
 import org.jhotdraw.draw.Drawing;
 import org.jhotdraw.draw.DrawingEditor;
 import org.jhotdraw.draw.Figure;
 import org.jhotdraw.draw.QuadTreeDrawing;
 import org.jhotdraw.draw.action.ButtonFactory;
 import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
 import org.jhotdraw.draw.io.InputFormat;
 import org.jhotdraw.draw.io.OutputFormat;
 import org.jhotdraw.draw.tool.Tool;
 import org.jhotdraw.gui.Worker;
 import org.jhotdraw.gui.filechooser.ExtensionFileFilter;
 import org.jhotdraw.undo.UndoRedoManager;
 import org.jhotdraw.util.ResourceBundleUtil;
 
 import view.display.InteractiveDrawingView;
 import view.viewer.InteractiveRealViewer2D;
 
 import com.jug.util.ImglibUtil;
 
 import controller.tool.NullTool;
 
 /**
  * A swing panel that can show imglib2 image data and annotate them using
  * JHotDraw figures.
  * 
  * @author HongKee Moon, Florian Jug, Tobias Pietzsch
  * @since 9/4/13
  */
 
 //TODO This component should be totally generic, being able to host all image types (not only LongType and DoubleType)
 
 public class IddeaComponent extends JPanel implements ActionListener, ChangeListener {
 
 	private static final long serialVersionUID = -3808140519052170304L;
 
 	// The everything containing scroll-bar
 	private JScrollPane scrollPane;
 	
 	// JSlider for time series
 	private boolean isTimeSliderVisible = false;
 	private JSlider timeSlider;
 	private int tIndex = 0;
 	private int tMax = 0;
 	
 	// JSlider for volume stack
 	private boolean isStackSliderVisible = false;
 	private JSlider stackSlider;
 	private int zIndex = 0;
 	private int zMax = 0;
 
 	// InteractiveViewer2D for the imglib2 data to be shown.
 	private InteractiveRealViewer2D< DoubleType > interactiveViewer2D;
 
 	// The imglib2 image data container
 	private IntervalView< DoubleType > ivSourceImage = null;
 
 	// JHotDraw related stuff
 	private DrawingEditor editor;
 	private InteractiveDrawingView view;
 	private Drawing drawing;
     /**
      * Each DrawView uses its own undo redo manager.
      * This allows for undoing and redoing actions per view.
      */
     private UndoRedoManager undo;
     
 	// Toolbar setup and the toolbar itself
 	private boolean isToolbarVisible = false;
 	private String toolbarLocation;
 	private JToolBar tb;
 	
 	// Menu related stuff
 	private final boolean isMenuVisible = false;
 	private JMenuBar menuBar;
 	private JMenu fileMenu;
 	private JMenuItem menuItemOpen;
 	private JMenuItem menuItemSaveAs;
 	private JMenu editMenu;
 	private JMenuItem menuItemUndo;
 	private JMenuItem menuItemRedo;
 
 	// File chooser for saving and loading
 	private JFileChooser openChooser;
 	private JFileChooser saveChooser;
 	/** Holds the currently opened file. */
 	private File file;
 	private HashMap< FileFilter, InputFormat > fileFilterInputFormatMap;
 	private HashMap< FileFilter, OutputFormat > fileFilterOutputFormatMap;
 
 	//////////////////////////// CONSTUCTION /////////////////////////////////
 
 	/**
 	 * Creates an <code>IddeaComponent</code> that does not (yet) display any
 	 * image.
 	 */
 	public IddeaComponent() {
 		
 		view = buildInteractiveDrawingView();
 
 		initComponents(view);
 	}
 
 	/**
 	 * Creates an <code>IddeaComponent</code> and adds the given
 	 * <code>soureImage</code> to it.
 	 */
 	public IddeaComponent( final IntervalView< DoubleType > sourceImage ) {
 
 		this.ivSourceImage = sourceImage;
 
 		view = buildInteractiveDrawingView( ivSourceImage );
 
 		initComponents(view);
 	}
 	
 	/**
 	 * Creates an <code>IddeaComponent</code> and adds the given
 	 * <code>dimension</code> to it. Otherwise, 300x200 default screen appears.
 	 */
 	public IddeaComponent( final Dimension dim ) {
 
 		view = buildInteractiveDrawingView( dim );
 
 		initComponents(view);
 	}
 	
 	private void initComponents(InteractiveDrawingView view)
 	{
 		editor = new DefaultDrawingEditor();
 		createEmptyToolbar();
 
 		scrollPane = new javax.swing.JScrollPane();
 		
 		setLayout( new java.awt.BorderLayout() );
 
 		scrollPane.setHorizontalScrollBarPolicy( javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
 		scrollPane.setVerticalScrollBarPolicy( javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER );
 		scrollPane.setViewportView( view );
 		
 		// Sliders initialization
 		timeSlider = new JSlider( JSlider.HORIZONTAL, 0, 0, 0 );
 		timeSlider.setName( "TimeSlider" );
 		timeSlider.addChangeListener( this );
 		
 		stackSlider = new JSlider( JSlider.VERTICAL, 0, 0, 0 );
 		stackSlider.setName( "StackSlider" );
 		stackSlider.addChangeListener( this );
 
 		menuBar = new JMenuBar();
 		
 		// FileMenu 
 		fileMenu = new JMenu();
 		menuItemOpen = new JMenuItem();
 		menuItemSaveAs = new JMenuItem();
 
 		fileMenu.setText( "File" );
 
 		menuItemOpen.setText( "Open..." );
 		menuItemOpen.addActionListener( this );
 		fileMenu.add( menuItemOpen );
 
 		menuItemSaveAs.setText( "Save As..." );
 		menuItemSaveAs.addActionListener( this );
 		fileMenu.add( menuItemSaveAs );
 
 		menuBar.add( fileMenu );
 		
 		// EditMenu        
 		editMenu = new JMenu();
 		menuItemUndo = new JMenuItem();
 		menuItemRedo = new JMenuItem();
 		
 		editMenu.setText( "Edit" );
 		menuItemUndo.setText( "Undo" );
 		menuItemUndo.addActionListener( this );
 		editMenu.add( menuItemUndo );
 		
 		menuItemRedo.setText( "Redo" );
 		menuItemRedo.addActionListener( this );
 		editMenu.add( menuItemRedo );
 		
 		menuBar.add( editMenu );
 		
 
 		if ( isMenuVisible ) this.add( menuBar, BorderLayout.NORTH );
 		
         if ( isTimeSliderVisible ) {
             this.add(timeSlider, BorderLayout.SOUTH);
             timeSlider.setMaximum(tMax);
         }
 
         if ( isStackSliderVisible ) {
             this.add( stackSlider, BorderLayout.EAST );
             stackSlider.setMaximum(zMax);
         }
         
 		add( scrollPane, java.awt.BorderLayout.CENTER );
 
 		if ( isToolbarVisible ) {
 			add( tb, toolbarLocation );
 		}
 
 		setEditor( editor );
 		view.setDrawing( createDrawing() );
 		
 		// Install undoRedoManager
         undo = new UndoRedoManager();
         view.getDrawing().addUndoableEditListener( undo );
 		getActionMap().put( UndoAction.ID, undo.getUndoAction() );
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.Event.META_MASK ), UndoAction.ID );
 		
 		getActionMap().put( RedoAction.ID, undo.getRedoAction() );
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.Event.META_MASK + java.awt.Event.SHIFT_MASK), RedoAction.ID );
 	}
 
 	//////////////////////////// GETTERS AND SETTERS /////////////////////////////////
 
 	/**
 	 * @return The <code>AffineTransform2D</code> describing the transformation
 	 *         of the <code>ivSourceImage</code> on screen.
 	 */
 	public AffineTransform2D getViewerTransform() {
 		return interactiveViewer2D.getViewerTransform();
 	}
 
 	/**
 	 * @return All JHotDraw annotations made in this component.
 	 */
 	public Set< Figure > getAllAnnotationFigures() {
 		return interactiveViewer2D.getJHotDrawDisplay().getAllFigures();
 	}
 
 	/**
 	 * Returns the current screen image.
 	 * 
 	 * @return
 	 */
 	public IntervalView< DoubleType > getSourceImage() {
 		return this.ivSourceImage;
 	}
 
 	/**
 	 * @return Returns the currently installed toolbar.
 	 */
 	public JToolBar getInstalledToolbar() {
 		return this.tb;
 	}
 
 	/**
 	 * Sets the location of the toolbar.
 	 * 
 	 * @param location
 	 *            Either <code>BorderLayout.NORTH</code>,
 	 *            <code>BorderLayout.EAST</code>,
 	 *            <code>BorderLayout.SOUTH</code>, or
 	 *            <code>BorderLayout.WEST</code>
 	 */
 	public void setToolBarLocation( final String location ) {
 		if ( location.equals( BorderLayout.WEST ) || location.equals( BorderLayout.EAST ) ) {
 			tb.setOrientation( JToolBar.VERTICAL );
 		} else {
 			tb.setOrientation( JToolBar.HORIZONTAL );
 		}
 
 		toolbarLocation = location;
 
 		if ( isToolbarVisible ) {
 			setToolBarVisible( false );
 			setToolBarVisible( true );
 		}
 	}
 
 	/**
 	 * Replaces the current
 	 * 
 	 * @param viewImg
 	 */
 	public < T extends RealType< T > & NativeType< T >> void setSourceImage( final IntervalView< T > viewImg ) {
 		final T min = Views.iterable( viewImg ).firstElement().copy();
 		final T max = min.copy();
 		ImglibUtil.computeMinMax( viewImg, min, max );
 
 		RealRandomAccessible< T > interpolated = null;
 		if ( viewImg.numDimensions() > 2 ) {
 			interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( viewImg, 2, 0 ) ), new NearestNeighborInterpolatorFactory< T >() );
 		} else {
 			interpolated = Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< T >() );
 		}
 
 		final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue() );
 
 		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param sourceImage
 	 *            an IntervalView<DoubleType> containing the desired view
 	 *            onto the raw image data
 	 */
 	public void setDoubleTypeSourceImage( final IntervalView< DoubleType > sourceImage ) {
 		this.ivSourceImage = sourceImage;
 		
 		final DoubleType min = new DoubleType();
 		final DoubleType max = new DoubleType();
 		ImglibUtil.computeMinMax( sourceImage, min, max );
 
 		RealRandomAccessible< DoubleType > interpolated = null;
 		
 		switch(sourceImage.numDimensions())
 		{
 			case 2: interpolated = Views.interpolate( Views.extendZero( sourceImage ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 					break;
 			case 3: timeSlider.setMaximum( (int) sourceImage.max( 2 ) ); tIndex = 0; showTimeSlider( true );				
 					interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( sourceImage, 2, tIndex ) ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 					break;
 			case 4: timeSlider.setMaximum( (int) sourceImage.max( 3 ) ); tIndex = 0; showTimeSlider( true );
 					stackSlider.setMaximum( (int) sourceImage.max( 2 ) ); zIndex = 0; showStackSlider( true ); 
 					interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( Views.hyperSlice( sourceImage, 3, tIndex ), 2, zIndex ) ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 					break;
 			default : throw new IllegalArgumentException("" + sourceImage.numDimensions() + " Dimension size is not supported!");
 		}
 
 		final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );
 
 		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param raiSource
 	 *            an RandomAccessibleInterval<DoubleType> containing the desired
 	 *            view
 	 *            onto the raw image data
 	 */
 	public void setDoubleTypeSourceImage( final RandomAccessibleInterval< DoubleType > raiSource ) {
 		this.setDoubleTypeSourceImage( Views.interval( raiSource, raiSource ) );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param glf
 	 *            the GrowthLineFrameto be displayed
 	 * @param sourceImage
 	 *            an IntervalView<LongType> containing the desired view
 	 *            onto the raw image data
 	 */
 	public void setLongTypeSourceImage( final IntervalView< LongType > sourceImage ) {
 		
 		final LongType min = new LongType();
 		final LongType max = new LongType();
 		ImglibUtil.computeMinMax( sourceImage, min, max );
 
 		final RealRandomAccessible< LongType > interpolated = Views.interpolate( Views.extendZero( sourceImage ), new NearestNeighborInterpolatorFactory< LongType >() );
 
 		final RealARGBConverter< LongType > converter = new RealARGBConverter< LongType >( min.get(), max.get() );
 
 		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param raiSource
 	 *            an RandomAccessibleInterval<LongType> containing the desired
 	 *            view
 	 *            onto the raw image data
 	 */
 	public void setLongTypeSourceImage( final RandomAccessibleInterval< LongType > raiSource ) {
 		this.setLongTypeSourceImage( Views.interval( raiSource, raiSource ) );
 	}
 
 	//////////////////////////// OVERRIDDEN /////////////////////////////////
 
     /**
      * Set the {@link Dimension} that contains the input image's dimension
      * information and propagate the dimension information to the JHotDrawDisplay.
      * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
      */
 	@Override
 	public void setPreferredSize( final Dimension dim ) {
 		interactiveViewer2D.getJHotDrawDisplay().setImageDim( dim );
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed( final ActionEvent e ) {
 		if ( e.getSource().equals( menuItemSaveAs ) ) {
 			final JFileChooser fc = getSaveChooser();
 			if ( file != null ) {
 				fc.setSelectedFile( file );
 			}
 
 			if ( fc.showSaveDialog( this ) == JFileChooser.APPROVE_OPTION ) {
 				this.setEnabled( false );
 				final File selectedFile;
 				if ( fc.getFileFilter() instanceof ExtensionFileFilter ) {
 					selectedFile = ( ( ExtensionFileFilter ) fc.getFileFilter() ).makeAcceptable( fc.getSelectedFile() );
 				} else {
 					selectedFile = fc.getSelectedFile();
 				}
 				final OutputFormat selectedFormat = fileFilterOutputFormatMap.get( fc.getFileFilter() );
 				new Worker< Object >() {
 
 					@Override
 					protected Object construct() throws IOException {
 						IddeaComponent.this.write( selectedFile.toURI(), selectedFormat );
 						return null;
 					}
 
 					@Override
 					protected void done( final Object value ) {
 						file = selectedFile;
 					}
 
 					@Override
 					protected void failed( final Throwable error ) {
 						error.printStackTrace();
 						JOptionPane.showMessageDialog( IddeaComponent.this, "<html><b>Couldn't save to file \"" + selectedFile.getName() + "\"<br>" + error.toString(), "Save As File", JOptionPane.ERROR_MESSAGE );
 					}
 
 					@Override
 					protected void finished() {
 						IddeaComponent.this.setEnabled( true );
 					}
 				}.start();
 			}
 		} else if ( e.getSource().equals( menuItemOpen ) ) {
 			final JFileChooser fc = getOpenChooser();
 			if ( file != null ) {
 				fc.setSelectedFile( file );
 			}
 
 			if ( fc.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION ) {
 				this.setEnabled( false );
 				final File selectedFile = fc.getSelectedFile();
 				final InputFormat selectedFormat = fileFilterInputFormatMap.get( fc.getFileFilter() );
 				new Worker< Object >() {
 
 					@Override
 					protected Object construct() throws IOException {
 						IddeaComponent.this.read( selectedFile.toURI(), selectedFormat );
 						return null;
 					}
 
 					@Override
 					protected void done( final Object value ) {
 						file = selectedFile;
 					}
 
 					@Override
 					protected void failed( final Throwable error ) {
 						error.printStackTrace();
 						JOptionPane.showMessageDialog( IddeaComponent.this, "<html><b>Couldn't open file \"" + selectedFile.getName() + "\"<br>" + error.toString(), "Open File", JOptionPane.ERROR_MESSAGE );
 					}
 
 					@Override
 					protected void finished() {
 						IddeaComponent.this.setEnabled( true );
 					}
 				}.start();
 			}
 		} else if ( e.getSource().equals( menuItemUndo ) ) {
 			getActionMap().get(UndoAction.ID).actionPerformed( e );
 		} else if ( e.getSource().equals( menuItemRedo ) ) {
 			getActionMap().get(RedoAction.ID).actionPerformed( e );
 		}
 	}
 
 	//////////////////////////// FUNCTIONS /////////////////////////////////
 
 	public void showMenu( final boolean visible ) {
 		this.remove( menuBar );
 		if ( visible ) {
 			this.add( menuBar, BorderLayout.NORTH );
 		}
 	}
 	
 	public void showTimeSlider( final boolean visible ) {
 		this.remove( timeSlider );
 		if ( visible ) {
 			this.add( timeSlider, BorderLayout.SOUTH );
 		}
 		this.updateUI();
 	}
 	
 	public void showStackSlider( final boolean visible ) {
 		this.remove( stackSlider );
 		if ( visible ) {
 			this.add( stackSlider, BorderLayout.EAST );
 		}
 		this.updateUI();
 	}
 
 	/**
 	 * Installs a toolbar that contains no annotation functionality at all.
 	 */
 	public void installDefaultToolBar() {
 		this.tb.removeAll();
 
 		ButtonFactory.addSelectionToolTo( tb, editor, ButtonFactory.createDrawingActions( editor ), ButtonFactory.createSelectionActions( editor ) );
 
 		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "model.Labels" );
 		ButtonFactory.addToolTo( tb, editor, new NullTool(), "edit.handleImageData", labels );
 	}
 
 	/**
 	 * Shows or hides the currently installed toolbar.
 	 * 
 	 * @param visible
 	 */
 	public void setToolBarVisible( final boolean visible ) {
 		isToolbarVisible = visible;
 		if ( isToolbarVisible ) {
 			add( tb, toolbarLocation );
 		} else {
 			remove( tb );
 		}
 	}
 
 	/**
 	 * @param loadedDrawing
 	 */
 	protected void setDrawing( final Drawing loadedDrawing ) {
 		if ( view.getDrawing() != null ) {
             view.getDrawing().removeUndoableEditListener(undo);
 		}
 		view.setDrawing( loadedDrawing );
         view.getDrawing().addUndoableEditListener(undo);
         undo.discardAllEdits();
 		this.drawing = loadedDrawing;
 	}
 
 	//////////////////////////// PRIVATE STUFF /////////////////////////////////
 
 	/**
 	 * Builds an Interactive Drawing view from a given <code>sourceImage</code>
 	 * Caution: this function also creates a new
 	 * <code>interactiveViewer2D</code>.
 	 * 
 	 * @param sourceImage
 	 * @return
 	 */
 	private InteractiveDrawingView buildInteractiveDrawingView( final IntervalView< DoubleType > sourceImage ) {
         final AffineTransform2D transform = new AffineTransform2D();
 
         final DoubleType min = new DoubleType();
         final DoubleType max = new DoubleType();
         ImglibUtil.computeMinMax(sourceImage, min, max);
 
         RealRandomAccessible< DoubleType > interpolated = null;
 
         switch(sourceImage.numDimensions())
         {
             case 2: interpolated = Views.interpolate( Views.extendZero( sourceImage ), new NearestNeighborInterpolatorFactory< DoubleType >() );
                 break;
             case 3: tMax = (int) sourceImage.max( 2 ); isTimeSliderVisible = true; tIndex = 0;
                 interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( sourceImage, 2, tIndex ) ), new NearestNeighborInterpolatorFactory< DoubleType >() );
                 break;
             case 4: tMax = (int) sourceImage.max( 3 ); isTimeSliderVisible = true; tIndex = 0;
                 zMax = (int) sourceImage.max( 2 ); isStackSliderVisible = true; zIndex = 0;
                 interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( Views.hyperSlice( sourceImage, 3, tIndex ), 2, zIndex ) ), new NearestNeighborInterpolatorFactory< DoubleType >() );
                 break;
             default : throw new IllegalArgumentException("" + sourceImage.numDimensions() + " Dimension size is not supported!");
         }
 
         final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );
 
         int width = ( int ) sourceImage.max( 0 );
         int height = ( int ) sourceImage.max( 1 );
 
         interactiveViewer2D = new InteractiveRealViewer2D< DoubleType >( width, height, interpolated, transform, converter );
 
         return interactiveViewer2D.getJHotDrawDisplay();
 	}
 	
 	/**
 	 * Builds default Interactive Drawing view
 	 * Caution: this function also creates a new
 	 * <code>interactiveViewer2D</code>.
 	 * 
 	 * @return
 	 */
 	private InteractiveDrawingView buildInteractiveDrawingView( ) {
 
 		final AffineTransform2D transform = new AffineTransform2D();
 		final RealRandomAccessible< DoubleType > dummy = new DummyRealRandomAccessible();
 		final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( 0, 0 );
 
 		interactiveViewer2D = new InteractiveRealViewer2D< DoubleType >( 300, 200, dummy, transform, converter );
 
 		return interactiveViewer2D.getJHotDrawDisplay();
 	}
 	
 	/**
 	 * Builds an Interactive Drawing view from a given Dimension <code>dim</code>
 	 * Caution: this function also creates a new
 	 * <code>interactiveViewer2D</code>.
 	 * 
 	 * @param dim
 	 * @return
 	 */
 	private InteractiveDrawingView buildInteractiveDrawingView( final Dimension dim ) {
 
 		final AffineTransform2D transform = new AffineTransform2D();
 		final RealRandomAccessible< DoubleType > dummy = new DummyRealRandomAccessible();
 		final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( 0, 0 );
 
 		interactiveViewer2D = new InteractiveRealViewer2D< DoubleType >( dim.width, dim.height, dummy, transform, converter );		
 
 		return interactiveViewer2D.getJHotDrawDisplay();
 	}
 
 	/**
 	 * Creates a new Drawing for this view.
 	 */
 	private Drawing createDrawing() {
 		drawing = new QuadTreeDrawing();
 		final DOMStorableInputOutputFormat ioFormat = new DOMStorableInputOutputFormat( new DrawFigureFactory() );
 
 		drawing.addInputFormat( ioFormat );
 		drawing.addOutputFormat( ioFormat );
 
 		return drawing;
 	}
 
 	/**
 	 * Sets a JHotDraw drawing editor for this component.
 	 */
 	private void setEditor( final DrawingEditor newValue ) {
 		if ( editor != null ) {
 			editor.remove( view );
 		}
 		editor = newValue;
 		if ( editor != null ) {
 			editor.add( view );
 		}
 	}
 
 	/**
 	 * Creates an empty toolbar.
 	 */
 	private void createEmptyToolbar() {
 		this.tb = new JToolBar();
 		this.tb.setOrientation( JToolBar.VERTICAL );
 
 		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" );
 		tb.setName( labels.getString( "window.drawToolBar.title" ) );
 	}
 
 	/**
 	 * Adds a <code>Tool</code> to the toolbar.
 	 * 
 	 * @param tool
 	 *            The tool to be added to the currently installed toolbar.
 	 * @param lableKey
 	 *            The key that points to the label resource.
 	 * @param labels
 	 *            All the labels.
 	 */
 	public void addTool( final Tool tool, final String labelKey, final ResourceBundleUtil labels ) {
 		ButtonFactory.addToolTo( tb, editor, tool, labelKey, labels );
 	}
 	
 
 	/**
 	 * Adds a <code>JToogleButton</code> to the toolbar.
 	 *
 	 * @param button the button
 	 */
 	public void addToolBar( JToggleButton button ) {
 		tb.add(button);
 	}
 
 	/**
 	 * @param strokes
 	 *            An array containing all available stroke thicknesses.
 	 */
 	public void addToolStrokeWidthButton( final double[] strokes ) {
 		tb.add( ButtonFactory.createStrokeWidthButton( editor, strokes, ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" ) ) );
 	}
 
 	/**
 	 * Adds an separator to the currently installed toolbar.
 	 */
 	public void addToolBarSeparator() {
 		tb.addSeparator();
 	}
 
 	/**
 	 * Update the realRandomSource with new source.
 	 * 
 	 * @param source
 	 */
 	private void updateDoubleTypeSourceAndConverter( final RealRandomAccessible source, final RealARGBConverter converter ) {
 		interactiveViewer2D.updateConverter( converter );
 		interactiveViewer2D.updateSource( source );
 		interactiveViewer2D.getJHotDrawDisplay().resetTransform();
 	}
 
 	/** Lazily creates a JFileChooser and returns it. */
 	private JFileChooser getOpenChooser() {
 		if ( openChooser == null ) {
 			openChooser = new JFileChooser();
 			fileFilterInputFormatMap = new HashMap< javax.swing.filechooser.FileFilter, InputFormat >();
 			javax.swing.filechooser.FileFilter firstFF = null;
 			for ( final InputFormat format : this.drawing.getInputFormats() ) {
 				final javax.swing.filechooser.FileFilter ff = format.getFileFilter();
 				if ( firstFF == null ) {
 					firstFF = ff;
 				}
 				fileFilterInputFormatMap.put( ff, format );
 				openChooser.addChoosableFileFilter( ff );
 			}
 			openChooser.setFileFilter( firstFF );
 			openChooser.addPropertyChangeListener( new PropertyChangeListener() {
 
 				@Override
 				public void propertyChange( final PropertyChangeEvent evt ) {
 					if ( "fileFilterChanged".equals( evt.getPropertyName() ) ) {
 						final InputFormat inputFormat = fileFilterInputFormatMap.get( evt.getNewValue() );
 						openChooser.setAccessory( ( inputFormat == null ) ? null : inputFormat.getInputFormatAccessory() );
 					}
 				}
 			} );
 		}
 		return openChooser;
 	}
 
 	/** Lazily creates a JFileChooser and returns it. */
 	private JFileChooser getSaveChooser() {
 		if ( saveChooser == null ) {
 			saveChooser = new JFileChooser();
 			fileFilterOutputFormatMap = new HashMap< javax.swing.filechooser.FileFilter, OutputFormat >();
 			javax.swing.filechooser.FileFilter firstFF = null;
 			for ( final OutputFormat format : this.drawing.getOutputFormats() ) {
 				final javax.swing.filechooser.FileFilter ff = format.getFileFilter();
 				if ( firstFF == null ) {
 					firstFF = ff;
 				}
 				fileFilterOutputFormatMap.put( ff, format );
 				saveChooser.addChoosableFileFilter( ff );
 			}
 			saveChooser.setFileFilter( firstFF );
 			saveChooser.addPropertyChangeListener( new PropertyChangeListener() {
 
 				@Override
 				public void propertyChange( final PropertyChangeEvent evt ) {
 					if ( "fileFilterChanged".equals( evt.getPropertyName() ) ) {
 						final OutputFormat outputFormat = fileFilterOutputFormatMap.get( evt.getNewValue() );
 						saveChooser.setAccessory( ( outputFormat == null ) ? null : outputFormat.getOutputFormatAccessory() );
 					}
 				}
 			} );
 		}
 		return saveChooser;
 	}
 
 	/**
 	 * Writes the drawing from the IddeaComponent into a file.
 	 * <p>
 	 * This method should be called from a worker thread. Calling it from the
 	 * Event Dispatcher Thread will block the user interface, until the drawing
 	 * is written.
 	 */
 	public void write( final URI uri ) throws IOException {
 		final Drawing saveDrawing = IddeaComponent.this.drawing;
 		if ( saveDrawing.getOutputFormats().size() == 0 ) { throw new InternalError( "Drawing object has no output formats." ); }
 
 		// Try out all output formats until we find one which accepts the
 		// filename entered by the user.
 		final File f = new File( uri );
 		for ( final OutputFormat format : saveDrawing.getOutputFormats() ) {
 			if ( format.getFileFilter().accept( f ) ) {
 				format.write( uri, saveDrawing );
 				// We get here if writing was successful.
 				// We can return since we are done.
 				return;
 
 			}
 
 		}
 		throw new IOException( "No output format for " + f.getName() );
 	}
 
 	/**
 	 * Writes the drawing from the IddeaComponent into a file using the
 	 * specified output format.
 	 * <p>
 	 * This method should be called from a worker thread. Calling it from the
 	 * Event Dispatcher Thread will block the user interface, until the drawing
 	 * is written.
 	 */
 	public void write( final URI f, final OutputFormat format ) throws IOException {
 		if ( format == null ) {
 			write( f );
 			return;
 		}
 
 		// Write drawing to file
 		final Drawing saveDrawing = IddeaComponent.this.drawing;
 		format.write( f, saveDrawing );
 	}
 
 	/**
 	 * Reads a drawing from the specified file into the SVGDrawingPanel.
 	 * <p>
 	 * This method should be called from a worker thread. Calling it from the
 	 * Event Dispatcher Thread will block the user interface, until the drawing
 	 * is read.
 	 */
 	public void read( final URI f ) throws IOException {
 		// Create a new drawing object
 		final Drawing newDrawing = createDrawing();
 		if ( newDrawing.getInputFormats().size() == 0 ) { throw new InternalError( "Drawing object has no input formats." ); }
 
 		// Try out all input formats until we succeed
 		IOException firstIOException = null;
 		for ( final InputFormat format : newDrawing.getInputFormats() ) {
 			try {
 				format.read( f, newDrawing );
 				final Drawing loadedDrawing = newDrawing;
 				final Runnable r = new Runnable() {
 
 					@Override
 					public void run() {
 						// Set the drawing on the Event Dispatcher Thread
 						setDrawing( loadedDrawing );
 					}
 				};
 				if ( SwingUtilities.isEventDispatchThread() ) {
 					r.run();
 				} else {
 					try {
 						SwingUtilities.invokeAndWait( r );
 					} catch ( final InterruptedException ex ) {
 						// suppress silently
 					} catch ( final InvocationTargetException ex ) {
 						final InternalError ie = new InternalError( "Error setting drawing." );
 						ie.initCause( ex );
 						throw ie;
 					}
 				}
 				// We get here if reading was successful.
 				// We can return since we are done.
 				return;
 				//
 			} catch ( final IOException e ) {
 				// We get here if reading failed.
 				// We only preserve the exception of the first input format,
 				// because that's the one which is best suited for this drawing.
 				if ( firstIOException == null ) {
 					firstIOException = e;
 				}
 			}
 		}
 		throw firstIOException;
 	}
 
 	/**
 	 * Reads a drawing from the specified file into the SVGDrawingPanel using
 	 * the specified input format.
 	 * <p>
 	 * This method should be called from a worker thread. Calling it from the
 	 * Event Dispatcher Thread will block the user interface, until the drawing
 	 * is read.
 	 */
 	public void read( final URI f, final InputFormat format ) throws IOException {
 		if ( format == null ) {
 			read( f );
 			return;
 		}
 
 		// Create a new drawing object
 		final Drawing newDrawing = createDrawing();
 		if ( newDrawing.getInputFormats().size() == 0 ) { throw new InternalError( "Drawing object has no input formats." ); }
 
 		format.read( f, newDrawing );
 		final Drawing loadedDrawing = newDrawing;
 		final Runnable r = new Runnable() {
 
 			@Override
 			public void run() {
 				// Set the drawing on the Event Dispatcher Thread
 				setDrawing( loadedDrawing );
 			}
 		};
 		if ( SwingUtilities.isEventDispatchThread() ) {
 			r.run();
 		} else {
 			try {
 				SwingUtilities.invokeAndWait( r );
 			} catch ( final InterruptedException ex ) {
 				// suppress silently
 			} catch ( final InvocationTargetException ex ) {
 				final InternalError ie = new InternalError( "Error setting drawing." );
 				ie.initCause( ex );
 				throw ie;
 			}
 		}
 	}
 
 	/**
 	 * States of TimeSlider or StackSlider are changed.
 	 *
 	 * @param changeEvent the change event
 	 */
 	@Override
 	public void stateChanged( ChangeEvent changeEvent ) {
         if( timeSlider.equals( changeEvent.getSource() ) )
         {
         	tIndex = timeSlider.getValue();
         	updateView();
         }
         else if( stackSlider.equals( changeEvent.getSource() ) )
         {
         	zIndex = stackSlider.getValue();
         	updateView();        	
         }
 	}
 	
 	/**
 	 * Update the display view.
 	 */
 	private void updateView() {
 
 		IntervalView< DoubleType > interval;
 		
 		switch(ivSourceImage.numDimensions())
 		{
 			case 2: interval = ivSourceImage;
 					break;
 			case 3:	interval = Views.hyperSlice( ivSourceImage, 2, tIndex );
 					break;
 			case 4: interval = Views.hyperSlice( Views.hyperSlice( ivSourceImage, 3, tIndex ), 2, zIndex );
 					break;
 			default : throw new IllegalArgumentException("" + ivSourceImage.numDimensions() + " Dimension size is not supported!");
 		}
 				
 		RealRandomAccessible< DoubleType > interpolated = Views.interpolate( Views.extendZero( interval ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 		interactiveViewer2D.updateSource( interpolated );
 		
 		// In case of needing to update MinMax values when user changes time or stack
 		
 //		final DoubleType min = new DoubleType();
 //		final DoubleType max = new DoubleType();
 //		ImglibUtil.computeMinMax( interval, min, max );
 //		final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );
 //
 //		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 }
