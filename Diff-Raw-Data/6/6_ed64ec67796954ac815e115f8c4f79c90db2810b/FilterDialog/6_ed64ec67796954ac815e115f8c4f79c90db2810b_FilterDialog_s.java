 /*
  *  FilterDialog.java
  *  Eisenkraut
  *
  *  Copyright (c) 2004-2008 Hanns Holger Rutz. All rights reserved.
  *
  *	This software is free software; you can redistribute it and/or
  *	modify it under the terms of the GNU General Public License
  *	as published by the Free Software Foundation; either
  *	version 2, june 1991 of the License, or (at your option) any later version.
  *
  *	This software is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *	General Public License for more details.
  *
  *	You should have received a copy of the GNU General Public
  *	License (gpl.txt) along with this software; if not, write to the Free Software
  *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  *	For further information, please contact Hanns Holger Rutz at
  *	contact@sciss.de
  *
  *
  *  Changelog:
  *		14-Jul-05	created from de.sciss.meloncillo.render.FilterDialog
  *		31-Aug-05	supports clipboard contents
  */
 
 package de.sciss.eisenkraut.render;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 
 import de.sciss.app.AbstractApplication;
 import de.sciss.app.Application;
 import de.sciss.app.AbstractCompoundEdit;
 import de.sciss.common.AppWindow;
 import de.sciss.common.ProcessingThread;
 import de.sciss.eisenkraut.Main;
 import de.sciss.eisenkraut.edit.BasicCompoundEdit;
 import de.sciss.eisenkraut.io.AudioStake;
 import de.sciss.eisenkraut.io.AudioTrail;
 import de.sciss.eisenkraut.io.BlendContext;
 import de.sciss.eisenkraut.io.MarkerTrail;
 import de.sciss.eisenkraut.session.Session;
 import de.sciss.eisenkraut.timeline.AudioTracks;
 import de.sciss.eisenkraut.timeline.Track;
 import de.sciss.eisenkraut.util.PrefsUtil;
 import de.sciss.gui.AbstractWindowHandler;
 import de.sciss.gui.CoverGrowBox;
 import de.sciss.gui.GUIUtil;
 import de.sciss.gui.HelpButton;
 import de.sciss.io.Span;
 import de.sciss.util.Flag;
 
 /**
  *  @author		Hanns Holger Rutz
  *  @version	0.70, 07-Dec-07
  *
  *	@todo		an option to render the transformed data
  *				into the clipboard.
  *	@todo		support of variable output time span
  */
 public class FilterDialog
 extends AppWindow
 implements	RenderConsumer, RenderHost,
 			ProcessingThread.Client
 {
 	/*
 	 *  Session document reference
 	 */
 	private Session				doc;
 	
 	private boolean	guiCreated	= false;
 	
 	/*
 	 *	The central panel of the plug-in window.
 	 *	Subclasses should set the pane's view
 	 *	to display their GUI elements.
 	 */
 	private JScrollPane				ggSettingsPane;
 
 	private JComponent				bottomPanel;
 
 	/**
 	 *	Default to be used for the GUI elements.
 	 */
 	private RenderPlugIn			plugIn	= null;
 	private ProcessingThread		pt;
 	private RenderContext			context = null;
 	private	JButton					ggClose, ggRender;
 	private	Action					actionClose, actionRender; // , actionCancel;
 	private HelpButton				ggHelp;
 
 	private float	progress;
 
 	// context options map
 	private static final String	KEY_CONSC	= "consc";
 
 	/**
 	 *	Constructs a Trajectory-Filtering dialog.
 	 */
 	public FilterDialog()
 	{
 		super( SUPPORT );
 		
 		setLocationRelativeTo( null );
 
 		init();
 		AbstractApplication.getApplication().addComponent( Main.COMP_FILTER, this );
 	}
 	
 //	protected boolean restoreVisibility()
 //	{
 //		return false;
 //	}
 	
 	public void dispose()
 	{
 		AbstractApplication.getApplication().removeComponent( Main.COMP_FILTER );
 		super.dispose();
 	}
 
 	public void process( String plugInClassName, Session aDoc, boolean forceDisplay, boolean blockDisplay )
 	{
 		this.doc	= aDoc;
 
 		final JComponent view;
 
 		if( switchPlugIn( plugInClassName )) {
 			context = createRenderContext();
 			if( context == null ) return;
 			if( (!blockDisplay && plugIn.shouldDisplayParameters()) ||
 				(forceDisplay && plugIn.hasUserParameters()) ) {
 
 				// display settings
 				if( !guiCreated ) createGUI();
 				ggHelp.setHelpFile( plugInClassName.substring( plugInClassName.lastIndexOf( '.' ) + 1 ));
 				view = plugIn.getSettingsView( context );
 				AbstractWindowHandler.setDeepFont( view );
 				ggSettingsPane.setViewportView( view );
 				pack();
 				setTitle( plugIn.getName() );
 				setVisible( true );	// modal
 //				toFront();
 				
 			} else {	// process immediately
 			
 				processStart();
 			}
 		}
 	}
 	
 	public RenderPlugIn getPlugIn()
 	{
 		return plugIn;
 	}
 	
 	private void createGUI()
 	{
 //		createSettingsMenu();
 		createGadgets( 0 );
 		
 		final Container cp = getContentPane();
 //		cp.add( toptopPanel, BorderLayout.NORTH );
 		cp.add( ggSettingsPane, BorderLayout.CENTER );
 		if( bottomPanel != null ) {
 			cp.add( bottomPanel, BorderLayout.SOUTH );
 		}
 		
 		AbstractWindowHandler.setDeepFont( cp );
 		
 		// --- Listener ---
 //		new DynamicAncestorAdapter( new DynamicPrefChangeManager( classPrefs,
 //			new String[] { KEY_PLUGIN, KEY_SELECTIONONLY }, this )).addTo( getRootPane() );
 		guiCreated	= true;
 	}
 
 	private void createGadgets( int flags )
 	{
 		bottomPanel		= createBottomPanel( flags );
 		ggSettingsPane	= new JScrollPane( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 		              	                   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
 	}
 
 	/*
 	 *	Default implementation creates a panel
 	 *	with close and render buttons. A key shortcut
 	 *	is attached to the close button (escape)
 	 *	and to the render button (meta+return)
 	 */
 	private JComponent createBottomPanel( int flags )
 	{
 		final JPanel panel;
 
 		actionClose		= new ActionClose(  getResourceString( "buttonClose" ));
 //		actionCancel	= new actionCancelClass( getResourceString( "buttonCancel" ));
 		actionRender	= new ActionRender( getResourceString( "buttonRender" ));
 		
 		panel		= new JPanel( new FlowLayout( FlowLayout.TRAILING, 4, 2 ));
 //		bottomPanel.setLayout( new BorderLayout() );
 		ggClose			= new JButton( actionClose );
 		GUIUtil.createKeyAction( ggClose, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ));
 		ggRender		= new JButton( actionRender );
 		GUIUtil.createKeyAction( ggRender, KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ));
 //		ggRender.setEnabled( false );
 		ggHelp			= new HelpButton();
 		panel.add( ggClose );
 		panel.add( ggRender );
 		panel.add( ggHelp );
 		panel.add( CoverGrowBox.create() );
 
 		return panel;
 	}
 
 //	protected List getProducerTypes()
 //	{
 //		if( collProducerTypes.isEmpty() ) {
 //			Hashtable h;
 //			h = new Hashtable();
 //			h.put( Main.KEY_CLASSNAME, "de.sciss.meloncillo.render.TimeWarpFilter" );
 //			h.put( Main.KEY_HUMANREADABLENAME, "Time Warp" );
 //			collProducerTypes.add( h );
 //			h = new Hashtable();
 //			h.put( Main.KEY_CLASSNAME, "de.sciss.meloncillo.render.VectorTransformFilter" );
 //			h.put( Main.KEY_HUMANREADABLENAME, "Vector Transformation" );
 //			collProducerTypes.add( h );
 //			h = new Hashtable();
 //			h.put( Main.KEY_CLASSNAME, "de.sciss.meloncillo.render.LispFilter" );
 //			h.put( Main.KEY_HUMANREADABLENAME, "Lisp Plug-In" );
 //			collProducerTypes.add( h );
 //		}
 //		return collProducerTypes;
 //	}
 
 	/*
 	 *	@synchronization	attemptShared on DOOR_TIME
 	 */
 	private RenderContext createRenderContext()
 	{
 //		if( !doc.bird.attemptShared( Session.DOOR_TIME, 500 )) return null;
 //		try {
 			return new RenderContext( this, this, Track.getInfos( doc.selectedTracks.getAll(), doc.tracks.getAll() ),
 				doc.timeline.getSelectionSpan(), doc.timeline.getRate() );
 //		}
 //		finally {
 //			doc.bird.releaseShared( Session.DOOR_TIME );
 //		}
 	}
 
 	/**
 	 *	Checks if the render context is still valid.
 	 *
 	 *	@return	<code>false</code> if the context became invalid
 	 *			e.g. after a change in the selected time span
 	 *
 	 *	@synchronization	attemptShared on DOOR_TIMETRNS
 	 */
 //	protected boolean isRenderContextValid( RenderContext context )
 //	{
 //return true;
 /*
 		if( !doc.bird.attemptShared( Session.DOOR_TIMETRNS, 250 )) return false;
 		try {
 			return( doc.selectedTransmitters.getAll().equals( context.getTransmitters() ) &&
 					doc.timeline.getSelectionSpan().equals( context.getTimeSpan() ) &&
 					doc.timeline.getRate() == context.getSourceRate() );
 		}
 		finally {
 			doc.bird.releaseShared( Session.DOOR_TIMETRNS );
 		}
 */
 //	}
 
 // ---------------- concrete methods ---------------- 
 
 	/*
 	 *	This implementation sets itself as
 	 *	the consumer option in the context, then calls
 	 *	<code>prod.producerBegin( source )</code>
 	 *
 	 *	@see	RenderContext#KEY_CONSUMER
 	 */
 	private boolean invokeProducerBegin( RenderSource source, RenderPlugIn prod )
 	throws IOException
 	{
 //		context.setOption( RenderContext.KEY_CONSUMER, this );
 		source.context.getModifiedOptions();   // clear state
 
 		return prod.producerBegin( source );
 	}
 	
 	/*
 	 *	This implementation simply calls
 	 *	<code>prod.producerCancel( source )</code>
 	 */
 	private void invokeProducerCancel( ProcessingThread proc, RenderSource source, RenderPlugIn prod )
 	throws IOException
 	{
 		prod.producerCancel( source );
 	}
 
 	/*
 	 *	This implementation simply calls
 	 *	<code>prod.producerRender( source )</code>
 	 */
 	private boolean invokeProducerRender( ProcessingThread proc, RenderSource source, RenderPlugIn prod )
 	throws IOException
 	{
 		return prod.producerRender( source );
 	}
 
 	/*
 	 *	This implementation simply calls
 	 *	<code>prod.producerFinish( source )</code>
 	 */
 	private boolean invokeProducerFinish( ProcessingThread proc, RenderSource source, RenderPlugIn prod )
 	throws IOException
 	{
 		return prod.producerFinish( source );
 	}
 
 // ---------------- RenderConsumer interface ---------------- 
 
 	/**
 	 *  Initializes the consumption process.
 	 */
 	public boolean consumerBegin( RenderSource source )
 	throws IOException
 	{
 //System.err.println( "consumerBegin" );
 		final AudioTrail		at;
 		final ConsumerContext	consc   = (ConsumerContext) source.context.getOption( KEY_CONSC );
 		final Span				span	= source.context.getTimeSpan();
 		
 //		consc.edit		= new SyncCompoundSessionObjEdit(
 //			this, doc.bird, Session.DOOR_ALL, context.getTracks(),
 //			AudioTrack.OWNER_WAVE, null, null, consc.plugIn.getName() );
 		consc.edit		= new BasicCompoundEdit( consc.plugIn.getName() );
 		
 //		consc.bc		= BlendingAction.createBlendContext(
 //			AbstractApplication.getApplication().getUserPrefs().node( BlendingAction.DEFAULT_NODE ),
 //			context.getSourceRate(), context.getTimeSpan().getLength() / 2, context.getTimeSpan().getLength() / 2 ); // XXXXXXX
 
 		at				= consc.doc.getAudioTrail();
 // XXX
 //		consc.bs		= mte.beginOverwrite( context.getTimeSpan(), consc.bc, consc.edit );
 		consc.as		= at.alloc( span );
 		consc.progOff	= getProgression();
 		consc.progWeight= (1.0f - consc.progOff) / span.getLength();
 		
 		return true;
 	}
 
 	/**
 	 *	Finishes all multirate track editor write
 	 *	operations currently in progress. Closes
 	 *	the compound edit.
 	 */
 	public boolean consumerFinish( RenderSource source )
 	throws IOException
 	{
 //System.err.println( "consumerFinish " + java.awt.EventQueue.isDispatchThread() );
 		final ConsumerContext		consc   = (ConsumerContext) source.context.getOption( KEY_CONSC );
 		final AudioTrail			at		= consc.doc.getAudioTrail();
 
 // UUU
 //		if( (consc.bs != null) && (consc.edit != null) ) {
 //			mte.finishWrite( consc.bs, consc.edit, pt, 0.9f, 0.1f );
 		if( consc.edit != null ) {
 			
 			ProcessingThread.flushProgression();
 			ProcessingThread.setNextProgStop( 1.0f );
 			
 //			if( consc.as != null ) {
 			if( source.validAudio ) {
 				consc.as.flush();
 				at.editBegin( consc.edit );
 				at.editRemove( this, consc.as.getSpan(), consc.edit );
 				at.editInsert( this, consc.as.getSpan(), consc.edit );
 				at.editAdd( this, consc.as, consc.edit );
 //				if( !audioTrail.copyRangeFrom( (AudioTrail) srcTrail, copySpan, insertPos, mode, this, edit, trackMap2, bcPre, bcPost )) return CANCELLED;
 				at.editEnd( consc.edit );
 			}
 			if( source.validMarkers ) {
 				doc.markers.editBegin( consc.edit );
 				doc.markers.editClear( this, source.context.getTimeSpan(), consc.edit );
 				doc.markers.editAddAll( this, source.markers.getAll( true ), consc.edit );
 				doc.markers.editEnd( consc.edit );
 			}
 //			consc.edit.perform();
 //			consc.edit.end();
 //			doc.getUndoManager().addEdit( consc.edit );
 		}
 //		setTrueProgression( 1.0f );
 
 		return true;
 	}
 
 	/**
 	 *	Writes a block of the transformed data back
 	 *	to the transmitter trajectory tracks.
 	 */
 	public boolean consumerRender( RenderSource source )
 	throws IOException
 	{
 //System.err.println( "consumerRender" );
 		final ConsumerContext		consc   = (ConsumerContext) source.context.getOption( KEY_CONSC );
 		final AudioTrail			at		= consc.doc.getAudioTrail();
 		final boolean				preFade, postFade;
 
 // UUU
 //		if( consc.bs == null ) {
 		if( consc.as == null ) {
 			source.context.getHost().showMessage( JOptionPane.ERROR_MESSAGE,
 				AbstractApplication.getApplication().getResourceString( "renderEarlyConsume" ));
 			return false;
 		}
 //		mte.continueWrite( consc.bs, source.blockBuf, source.blockBufOff, source.blockBufLen );
 		
 		preFade		= source.blockSpan.overlaps( consc.blendPreSpan );
 		postFade	= source.blockSpan.overlaps( consc.blendPostSpan );
 
 		// outBuf is audioBlockBuf but with the unused
 		// channels set to null, so they won't be faded
 		if( preFade || postFade || consc.restoreUnused ) {
 			for( int ch = 0; ch < source.numAudioChannels; ch++ ) {
 				if( source.audioTrackMap[ ch ]) {
 //					System.out.println( "Yes for chan " + ch );
 					consc.outBuf[ ch ] = source.audioBlockBuf[ ch ];
 				} else {
 					consc.outBuf[ ch ] = null;
 				}
 			}
 			at.readFrames( consc.inBuf, 0, source.blockSpan );
 		}
 		
 		if( preFade ) {
 //			System.out.println( "pre fade" );
 //			at.readFrames( consc.inBuf, 0, source.blockSpan );
 			consc.bcPre.blend( source.blockSpan.start - consc.blendPreSpan.start,
 			                   consc.inBuf, 0, 
 			                   source.audioBlockBuf, source.audioBlockBufOff,
 			                   consc.outBuf, source.audioBlockBufOff,
 			                   source.audioBlockBufLen );
 		}
 		if( postFade ) {
 //			System.out.println( "post fade" );
 //			at.readFrames( consc.inBuf, 0, source.blockSpan );
 			consc.bcPost.blend( source.blockSpan.start - consc.blendPostSpan.start,
 			                    source.audioBlockBuf, source.audioBlockBufOff,
 			                    consc.inBuf, 0,
 			                    consc.outBuf, source.audioBlockBufOff,
 			                    source.audioBlockBufLen );
 		}
 		if( consc.restoreUnused ) {
 			for( int ch = 0; ch < source.numAudioChannels; ch++ ) {
 				if( !source.audioTrackMap[ ch ]) {
 					consc.outBuf[ ch ] = consc.inBuf[ ch ];
 				}
 			}
 			consc.as.writeFrames( consc.outBuf, source.audioBlockBufOff, source.blockSpan );
 		} else {
 			consc.as.writeFrames( source.audioBlockBuf, source.audioBlockBufOff, source.blockSpan );
 		}
 //float test = 0f;
 //for( int i = source.audioBlockBufOff, k = source.audioBlockBufOff + (int) source.blockSpan.getLength(); i < k; i++ ) {
 //	for( int j = 0; j < source.audioBlockBuf.length; j++ ) {
 //		test = Math.max( test, Math.abs( source.audioBlockBuf[ j ][ i ]));
 //	}
 //}
 //System.out.println( "in " + source.blockSpan + " maxAmp is " + test );
 
 		consc.framesWritten += source.audioBlockBufLen;
 
 		setProgression( consc.progOff + consc.progWeight * consc.framesWritten );
 
 		return true;
 	}
 
 	/**
 	 *	Cancels the re-importing of transformed data.
 	 *	Aborts and undos the compound edit.
 	 */
 	public void consumerCancel( RenderSource source )
 	throws IOException
 	{
 //		ConsumerContext	consc   = (ConsumerContext) source.context.getOption( KEY_CONSC );
 //
 //		if( consc != null && consc.edit != null ) {
 //			consc.edit.cancel();
 //			consc.edit = null;
 //		}
 	}
 
 	/*
 	 *	Default implementation creates a new
 	 *	instance of the plug-in class and initializes it.
 	 */
 	private boolean switchPlugIn( String className )
 	{
 		boolean					success	= false;
 		final Application		app		= AbstractApplication.getApplication();
 
 		plugIn = null;
 		if( className != null ) {
 			try {
 				plugIn	= (RenderPlugIn) Class.forName( className ).newInstance();
 //				plugIn.init( root, doc );
 				plugIn.init( app.getUserPrefs().node( PrefsUtil.NODE_PLUGINS ).node(
 					className.substring( className.lastIndexOf( '.' ) + 1 )));
 				success	= true;
 			}
 			catch( InstantiationException e1 ) {
 				GUIUtil.displayError( getWindow(), e1, app.getResourceString( "errInitPlugIn" ));
 			}
 			catch( IllegalAccessException e2 ) {
 				GUIUtil.displayError( getWindow(), e2, app.getResourceString( "errInitPlugIn" ));
 			}
 			catch( ClassNotFoundException e3 ) {
 				GUIUtil.displayError( getWindow(), e3, app.getResourceString( "errInitPlugIn" ));
 			}
 			catch( ClassCastException e4 ) {
 				GUIUtil.displayError( getWindow(), e4, app.getResourceString( "errInitPlugIn" ));
 			}
 		}
 		return success;
 	}
 	
 	protected void hideAndDispose()
 	{
 		if( ggSettingsPane != null ) ggSettingsPane.setViewportView( null );
 		setVisible( false );
 		dispose();
 	}
 	
 //	private void processStop()
 //	{
 //		pt.cancel( true );
 //	}
 	
 	// to be called in event thread
 	protected void processStart()
 	{
 		hideAndDispose();
 	
 		if( (context == null) || (plugIn == null) || !doc.checkProcess() ) return;
 	
 		final ConsumerContext	consc;
 		final Flag				hasSelectedAudio	= new Flag( false );
 		final RenderSource		source;
 		final List				tis					= context.getTrackInfos();
 		final int				inTrnsLen, outTrnsLen;
 		final int				minBlockSize, maxBlockSize, prefBlockSize;
 		final Set				newOptions;
 		final RandomAccessRequester rar;
 		final long				pasteLength, preMaxLen, postMaxLen;
 		final Span				span;
 //		final int				numClipChannels		= 0;
 		boolean					hasSelectedMarkers	= false;
 		Track.Info				ti;
 		Object					val;
 
 		consc			= new ConsumerContext();
 		consc.plugIn	= plugIn;
 		consc.doc		= doc;
 		context.setOption( KEY_CONSC, consc );
 		
 		source			= new RenderSource( context );
 		
 		if( !AudioTracks.checkSyncedAudio( tis, consc.plugIn.getLengthPolicy() == RenderPlugIn.POLICY_MODIFY, null, hasSelectedAudio )) {
 			return; // FAILED;
 		}
 		source.validAudio = (consc.plugIn.getAudioPolicy() == RenderPlugIn.POLICY_MODIFY) && hasSelectedAudio.isSet();
 		for( int i = 0; i < tis.size(); i++ ) {
 			ti = (Track.Info) tis.get( i );
 			if( (ti.trail instanceof MarkerTrail) && ti.selected ) {
 				hasSelectedMarkers = true;
 				break;
 			}
 		}
 		source.validMarkers	= (consc.plugIn.getMarkerPolicy() == RenderPlugIn.POLICY_MODIFY) && hasSelectedMarkers;
 		if( source.validMarkers ) source.markers = doc.markers.getCuttedTrail( context.getTimeSpan(), doc.markers.getDefaultTouchMode(), 0 );
 		
 		consc.restoreUnused = plugIn.getUnselectedAudioPolicy() == RenderPlugIn.POLICY_MODIFY;
 		
 		try {
 			if( !invokeProducerBegin( source, plugIn )) {
 				GUIUtil.displayError( getWindow(), new IOException( getResourceString( "errAudioWillLooseSync" )), plugIn.getName() );
 				return; // FAILED;
 			}
 //			consStarted			= true;
 //			remainingRead		= context.getTimeSpan().getLength();
 			newOptions			= context.getModifiedOptions();
 			if( newOptions.contains( RenderContext.KEY_MINBLOCKSIZE )) {
 				val				= context.getOption( RenderContext.KEY_MINBLOCKSIZE );
 				minBlockSize	= ((Integer) val).intValue();
 			} else {
 				minBlockSize	= 1;
 			}
 			if( newOptions.contains( RenderContext.KEY_MAXBLOCKSIZE )) {
 				val				= context.getOption( RenderContext.KEY_MAXBLOCKSIZE );
 				maxBlockSize	= ((Integer) val).intValue();
 			} else {
 				maxBlockSize	= 0x7FFFFF;
 			}
 			if( newOptions.contains( RenderContext.KEY_PREFBLOCKSIZE )) {
 				val				= context.getOption( RenderContext.KEY_PREFBLOCKSIZE );
 				prefBlockSize	= ((Integer) val).intValue();
 			} else {
 				prefBlockSize   = Math.max( minBlockSize, Math.min( maxBlockSize, 1024 ));
 			}
 			if( newOptions.contains( RenderContext.KEY_RANDOMACCESS )) {
 				rar				= (RandomAccessRequester) context.getOption( RenderContext.KEY_RANDOMACCESS );
 //				randomAccess	= true;
 			} else {
 				rar				= null;
 			}
 			if( newOptions.contains( RenderContext.KEY_CLIPBOARD )) {
 				return; // FAILED;
 			}
 			assert minBlockSize <= maxBlockSize : "minmaxblocksize";
 			
 			inTrnsLen		= prefBlockSize;
 			outTrnsLen		= inTrnsLen;
 
 			// ---  ---
 			for( int ch = 0; ch < source.numAudioChannels; ch++ ) {
 				source.audioBlockBuf[ ch ]	= new float[ outTrnsLen ];
 			}
 		}
 		catch( IOException e1 ) {
 			GUIUtil.displayError( getWindow(), e1, plugIn.getName() );
 			return;
 		}
 
 		consc.inBuf		= new float[ source.numAudioChannels ][ inTrnsLen ];
 		consc.outBuf	= new float[ source.numAudioChannels ][];
 		pasteLength		= context.getTimeSpan().getLength();
 		preMaxLen		= pasteLength >> 1;
 		postMaxLen		= pasteLength - preMaxLen;
 		consc.bcPre		= doc.createBlendContext( preMaxLen, 0, source.validAudio );
 		consc.bcPost	= doc.createBlendContext( postMaxLen, 0, source.validAudio );
 		span			= context.getTimeSpan();
		consc.blendPreSpan = consc.bcPre == null ? new Span() :
 			span.replaceStop( span.start + consc.bcPre.getLen() );
		consc.blendPostSpan = consc.bcPost == null ? new Span() :
 			span.replaceStart( span.stop - consc.bcPost.getLen() );
 
 		progress		= 0.0f;
 //		pt  = new ProcessingThread( this, doc.getFrame(), doc.bird, plugIn.getName(), new Object[] { context, null },
 //									Session.DOOR_ALL );
 		pt  = new ProcessingThread( this, doc.getFrame(), plugIn.getName() );
 		pt.putClientArg( "context", context );
 		pt.putClientArg( "source", source );
 		pt.putClientArg( "rar", rar );
 		pt.putClientArg( "inTrnsLen", new Integer( inTrnsLen ));
 //		pt.putClientArg( "tis", Track.getInfos( doc.selectedTracks.getAll(), doc.tracks.getAll() ));
 		doc.start( pt );
 
 //		ggClose.setEnabled( false );
 //		ggRender.setAction( actionCancel );
 //		ggRender.requestFocus();
 //		hibernation( true );
 	}
 
 	/**
 	 *	Default implementation calls <code>isRenderContextValid</code>.
 	 *	If that returns <code>false</code>, a new context is
 	 *	created and GUI is updated.
 	 */
 //	protected void checkReContext()
 //	{
 //		if( context != null && !isRenderContextValid( context )) {
 //			boolean success = false;
 //			try {
 //				success = reContext();
 //			}
 //			finally {
 //				ggRender.setEnabled( success );
 //			}
 //		}
 //	}
 	
 	private String getResourceString( String key )
 	{
 		return AbstractApplication.getApplication().getResourceString( key );
 	}
 
 // ---------------- RenderHost interface ---------------- 
 
 	public void	showMessage( int type, String text )
 	{
 		doc.getFrame().showMessage( type, text );
 	}
 	
 	public void setProgression( float p )
 	{
 		try {
 			ProcessingThread.update( p );
 		} catch( ProcessingThread.CancelledException e1 ) { /* ignore */ }
 //		doc.getFrame().setProgression( p ); // p * 0.9f;
 		this.progress	= p;
 	}
 
 //	private void setTrueProgression( float p )
 //	{
 //		doc.getFrame().setProgression( p );
 //		this.progress	= p;
 //	}
 	
 	private float getProgression()
 	{
 		return progress;
 	}
 	
 	public void setException( Exception e )
 	{
 		pt.setException( e );
 	}
 
 	public boolean isRunning()
 	{
 //		return( (pt != null) && pt.isAlive() );
 		return( (pt != null) && pt.isRunning() );
 	}
 
 // ---------------- RunnableProcessing interface ---------------- 
 
 /**
  *  RunnableProcessing interface core of data processing.
  *	The default implementation will handle all stream data
  *	requests. Subclasses don't usually need to overwrite this
  *	method and instead implement the methods
  *	<code>invokeProducerBegin</code>, <code>invokeProducerRender</code>,
  *	<code>invokeProducerCancel</code> and <code>invokeProducerFinish</code>.
  *	<p>
  *  If resampling is active, here's the scheme of the
  *  buffer handling:<br>
  *  <PRE>
  *		structure of the inBuf buffer:
  *
  *		(initially empty)
  *
  *		+--------+----------------------+--------+--------+
  *		| fltLenI|          >= 0        | fltLenI| fltLenI|
  *		+--------+----------------------+--------+--------+
  *										|<--overlapLen--->|
  *										|=overlapOff
  *				 |<-------trnsInside------------>|
  *
  *		first buffer read (mte.read()):
  *
  *				 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  *		+--------+----------------------+--------+--------+
  *		| fltLen |                      | fltLen | fltLen |
  *		+--------+----------------------+--------+--------+
  *
  *		// begin loop //
  *
  *		resampling:
  *
  *				 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  *		+--------+----------------------+--------+--------+
  *		| fltLen |                      | fltLen | fltLen |
  *		+--------+----------------------+--------+--------+
  *
  *		overlap handling:
  *
  *					 +----------------  %%%%%%%%%%%%%%%%%% (source)
  *					 V
  *		%%%%%%%%%%%%%%%%%% (destination)
  *		+--------+----------------------+--------+--------+
  *		| fltLen |                      | fltLen | fltLen |
  *		+--------+----------------------+--------+--------+
  *
  *		sucessive reads:
  *
  *		%%%%%%%%%%%%%%%%%% (old overlap)
  *						  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% (new read)
  *		+--------+----------------------+--------+--------+
  *		| fltLen |                      | fltLen | fltLen |
  *		+--------+----------------------+--------+--------+
  *
  *		// end loop //
  *  </PRE>
  *
  *	@see	#invokeProducerBegin( ProcessingThread, RenderContext, RenderSource, RenderPlugIn )
  *	@see	#invokeProducerCancel( ProcessingThread, RenderContext, RenderSource, RenderPlugIn )
  *	@see	#invokeProducerRender( ProcessingThread, RenderContext, RenderSource, RenderPlugIn )
  *	@see	#invokeProducerFinish( ProcessingThread, RenderContext, RenderSource, RenderPlugIn )
  */
 	public int processRun( ProcessingThread proc )
 	throws IOException
 	{
 		final RenderContext				rc					= (RenderContext) proc.getClientArg( "context" );
 //		final List						tis					= (List) pt.getClientArg( "tis" );
 		final ConsumerContext			consc				= (ConsumerContext) rc.getOption( KEY_CONSC );
 		final RenderSource				source				= (RenderSource) proc.getClientArg( "source" );
 		final AudioTrail				at					= consc.doc.getAudioTrail();
 
 		final int						inTrnsLen			= ((Integer) proc.getClientArg( "inTrnsLen" )).intValue();
 		final RandomAccessRequester		rar					= (RandomAccessRequester) proc.getClientArg( "rar" );
 		final boolean					randomAccess		= rar != null;
 		int								readLen, writeLen;
 //		Span							span;
 		long							readOffset, remainingRead;
 //		String							className;
 		boolean							consStarted			= false;
 		boolean							consFinished		= false;
 
 		// --- clipboard related ---
 //		Span							clipSpan			= null;
 //		long							clipShift			= 0;
 		
 		// --- resampling related ---
 		final int						inOff				= 0;
 
 		// --- init ---
 		
 		remainingRead = context.getTimeSpan().getLength();
 		if( source.validAudio ) ProcessingThread.setNextProgStop( 0.9f ); // XXX arbitrary
 
 //		inOff		= 0;
 		readOffset	= context.getTimeSpan().getStart();
 
 		try {
 			// --- rendering loop ---
 			
 prodLp:		while( !ProcessingThread.shouldCancel() ) {
 				if( randomAccess ) {
 					source.blockSpan = rar.getNextSpan();
 					readLen			 = (int) source.blockSpan.getLength();
 				} else {
 					readLen			 = (int) Math.min( inTrnsLen - inOff, remainingRead );
 					source.blockSpan = new Span( readOffset, readOffset + readLen );
 					// preparation for next loop iteration
 					remainingRead   -= readLen;
 					readOffset      += readLen;
 				}
 				if( readLen == 0 ) break prodLp;
 				writeLen			= readLen;
 				source.audioBlockBufLen  = writeLen;
 
 				// XXX optimization possibilities here:
 				// leave some channels null (both in readFrames
 				// as well as in arraycopy) depending on some
 				// kind of policy (maybe a new value of audioPolicy: POLICY_READONLY
 				// and POLICY_BYPASS would become POLICY_IGNORE
 				at.readFrames( consc.inBuf, inOff, source.blockSpan );
 				// looks like a bit of overload but in future
 				// versions, channel arrangement might be different than 1:1 from mte
 				for( int ch = 0; ch < source.numAudioChannels; ch++ ) {
 					System.arraycopy( consc.inBuf[ ch ], inOff, source.audioBlockBuf[ ch ], 0, writeLen );
 				}
 				
 				// --- handle thread ---
 				if( ProcessingThread.shouldCancel() ) break prodLp;
 
 				// --- producer rendering ---
 				if( !invokeProducerRender( proc, source, plugIn )) return FAILED;
 			} // while( isRunning() )
 
 			// --- finishing ---
 			consFinished = true;
 			if( ProcessingThread.shouldCancel() ) {
 				invokeProducerCancel( proc, source, plugIn );
 				return CANCELLED;
 			} else {
 				return( invokeProducerFinish( proc, source, plugIn ) ? DONE : FAILED );
 			}
 		}
 		finally {
 			if( consStarted && !consFinished ) {	// on failure cancel rendering and undo edits
 				try {
 					invokeProducerCancel( proc, source, plugIn );
 				}
 				catch( IOException e2 ) {
 					proc.setException( e2 );
 				}
 			}
 			if( source.markers != null ) {
 				source.markers.dispose();
 				source.markers = null;
 			}
 		}
 	}
 	
 	/**
 	 *	Re-enables the frame components.
 	 */
 	public void processFinished( ProcessingThread proc )
 	{
 		ConsumerContext	consc   = (ConsumerContext) context.getOption( KEY_CONSC );
 
 		if( proc.getReturnCode() == DONE ) {
 			if( consc != null && consc.edit != null ) {
 				consc.edit.perform();
 				consc.edit.end();
 				doc.getUndoManager().addEdit( consc.edit );
 			}
 		} else {
 			if( consc != null && consc.edit != null ) {
 				consc.edit.cancel();
 			}
 			if( proc.getReturnCode() == FAILED ) {
 				final Object message = proc.getClientArg( "error" );
 				if( message != null ) {
 					JOptionPane.showMessageDialog( getWindow(), message, plugIn == null ? null : plugIn.getName(), JOptionPane.ERROR_MESSAGE );
 				}
 			}
 		}
 	}
 
 	// we'll check shouldCancel() from time to time anyway
 	public void processCancel( ProcessingThread proc ) { /* ignore */ }
 
 // ---------------- Action objects ---------------- 
 
 	private class ActionClose
 	extends AbstractAction
 	{
 		protected ActionClose( String text )
 		{
 			super( text );
 		}
 
 		public void actionPerformed( ActionEvent e )
 		{
 			hideAndDispose();
 		}
 	}
 
 	private class ActionRender extends AbstractAction
 	{
 		protected ActionRender( String text )
 		{
 			super( text );
 		}
 
 		public void actionPerformed( ActionEvent e )
 		{
 			processStart();
 		}
 	}
 
 //	private class actionCancelClass
 //	extends AbstractAction
 //	{
 //		private actionCancelClass( String text )
 //		{
 //			super( text );
 //		}
 //
 //		public void actionPerformed( ActionEvent e )
 //		{
 //			processStop();
 //		}
 //	}
 
 // -------- ConsumerContext internal class --------
 	private static class ConsumerContext
 	{
 		protected Session					doc;
 		protected RenderPlugIn				plugIn;
 		protected AbstractCompoundEdit		edit;
 		protected BlendContext				bcPre, bcPost;
 		protected Span						blendPreSpan, blendPostSpan;
 		protected float						progOff, progWeight;
 		protected long						framesWritten;
 		protected AudioStake				as;
 		protected boolean					restoreUnused;
 		protected float[][]					inBuf, outBuf;
 		
 		protected ConsumerContext() { /* empty */ }
 	}
 }
