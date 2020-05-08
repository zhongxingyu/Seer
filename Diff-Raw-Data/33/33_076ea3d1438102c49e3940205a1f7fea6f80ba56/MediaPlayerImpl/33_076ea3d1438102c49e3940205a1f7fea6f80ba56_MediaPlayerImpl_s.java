 package com.senselessweb.soundcloud.mediasupport.service.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gstreamer.Pipeline;
 
 import com.senselessweb.soundcloud.domain.MediaSource;
 import com.senselessweb.soundcloud.mediasupport.domain.DefaultPlaylist;
 import com.senselessweb.soundcloud.mediasupport.gstreamer.GStreamerMessageListener;
 import com.senselessweb.soundcloud.mediasupport.gstreamer.GstreamerSupport;
 import com.senselessweb.soundcloud.mediasupport.gstreamer.PipelineBridge;
 import com.senselessweb.soundcloud.mediasupport.gstreamer.elements.EqualizerBridge;
 import com.senselessweb.soundcloud.mediasupport.gstreamer.elements.VolumeBridge;
 import com.senselessweb.soundcloud.mediasupport.gstreamer.pipeline.PipelineBuilder;
 import com.senselessweb.soundcloud.mediasupport.service.Equalizer;
 import com.senselessweb.soundcloud.mediasupport.service.MediaPlayer;
 import com.senselessweb.soundcloud.mediasupport.service.MessageListener;
 import com.senselessweb.soundcloud.mediasupport.service.Playlist;
 import com.senselessweb.soundcloud.mediasupport.service.VolumeControl;
 import com.senselessweb.storage.PersistencyService;
 
 /**
  * {@link MediaPlayer} implementation based on a gstreamer {@link Pipeline}.
  * 
  * @author thomas
  */
 public class MediaPlayerImpl implements MediaPlayer, GStreamerMessageListener
 {
 
 	/**
 	 * Logger
 	 */
 	private static final Log log = LogFactory.getLog(MediaPlayerImpl.class);
 	
 	/**
 	 * The current playlist
 	 */
 	private Playlist playlist = new DefaultPlaylist();
 	
 	/**
 	 * The currently used pipeline. Is rebuilt everytime a new song is played.  
 	 */
 	private PipelineBridge pipeline; 
 	
 	/**
 	 * The current mediasource
 	 */
 	private MediaSource current;
 	
 	/**
 	 * The volume brigde. Is reused for every build pipeline.
 	 */
 	private final VolumeBridge volume;
 	
 	/**
 	 * The equalizer brigde. Is reused for every build pipeline.
 	 */
 	private final EqualizerBridge equalizer;
 	
 	/**
 	 * The persistency service. Is used to store and restore pipeline properties.
 	 */
 	private final PersistencyService persistencyService;
 	
 	/**
 	 * The preconfigured pipeline builder. 
 	 */
 	private final PipelineBuilder pipelineBuilder;
 	
 	/**
 	 * The attached {@link MessageListener}s.
 	 */
 	private final MessageMediator messageMediator = new MessageMediator();
 	
 	/**
 	 * Constructor
 	 */
 	public MediaPlayerImpl()
 	{
 		this(null);
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param persistencyService The persistency service to use.  
 	 */
 	public MediaPlayerImpl(final PersistencyService persistencyService)
 	{
 		GstreamerSupport.initGst();
 		
 		this.persistencyService = persistencyService;
 		
 		// Initialize the pipeline elements
 		this.volume = new VolumeBridge(this.persistencyService);
 		this.equalizer = new EqualizerBridge(this.persistencyService);
 		
 		// Initialize the pipeline builder
 		this.pipelineBuilder = new PipelineBuilder().
 			withEqualizer(this.equalizer).
 			withMessageListener(this.messageMediator).
 			withGStreamerMessageListener(this).
 			withVolume(this.volume);
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#play()
 	 */
 	@Override
 	public synchronized void play()
 	{
 		if (this.pipeline == null && this.playlist.getCurrent() != null)
 		{
 			this.current = this.playlist.getCurrent();
 			this.pipeline = this.pipelineBuilder.createPipeline(this.current);
 		}
 		
 		if (this.pipeline != null) 
 		{
 			this.pipeline.play();
 			this.messageMediator.newSource(this.current);
 			this.messageMediator.stateChanged(State.PLAYING);
 		}
 	}
 	
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#stop()
 	 */
 	@Override
 	public synchronized void stop()
 	{
 		if (this.pipeline != null) 
 		{
 			this.pipeline.stop();
 			this.pipeline = null;
 			this.messageMediator.stateChanged(State.STOPPED);
 		}
 	}
 	
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#pause()
 	 */
 	@Override
 	public synchronized void pause()
 	{
 		if (this.pipeline != null) 
 		{
 			this.pipeline.pause();
 			this.messageMediator.stateChanged(State.PAUSED);
 		}
 	}
 	
 
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#next()
 	 */
 	@Override
 	public void next()
 	{
 		if (this.playlist.next())
 		{
 			this.stop();
 			this.play();
 		}
 	}	
 	
 
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#previous()
 	 */
 	@Override
 	public void previous()
 	{
 		if (this.playlist.previous())
 		{
 			this.stop();
 			this.play();
 		}
 	}	
 	
 
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.gstreamer.GStreamerMessageListener#endofStream()
 	 */
 	@Override
 	public void endofStream()
 	{
 		this.stop();
 		this.pipeline = null;
 		this.next();
 	}	
 
 
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#getState()
 	 */
 	@Override
 	public synchronized State getState()
 	{
 		return this.pipeline != null ? this.pipeline.getState() : State.STOPPED;
 	}
 	
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#getCurrentPlaylist()
 	 */
 	@Override
 	public Playlist getCurrentPlaylist()
 	{
 		return this.playlist;
 	}
 
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#getVolumeControl()
 	 */
 	@Override
 	public VolumeControl getVolumeControl()
 	{
 		return this.volume;
 	}
 	
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#getEqualizer()
 	 */
 	@Override
 	public Equalizer getEqualizer()
 	{
 		return this.equalizer;
 	}
 
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.gstreamer.GStreamerMessageListener#error(int, java.lang.String)
 	 */
 	@Override
 	public void error(final int errorcode, final String message)
 	{
 		if (this.pipeline.resetInErrorCase())
 		{
			this.stop();
			log.debug("Trying to restart the pipeline");
			this.pipeline = this.pipelineBuilder.createPipeline(this.current);
			this.play();
 		}
 		else
 		{
			this.stop();
 			this.messageMediator.error(message);
 		}
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#addMessageListener(com.senselessweb.soundcloud.mediasupport.service.MessageListener)
 	 */
 	@Override
 	public void addMessageListener(MessageListener listener)
 	{
 		this.messageMediator.addMessageListener(listener);
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MediaPlayer#shutdown()
 	 */
 	@Override
 	public synchronized void shutdown()
 	{
 		log.debug("Shutdown called");
 		if (this.pipeline != null) this.pipeline.stop();
 		GstreamerSupport.shutdown(false);
 	}
 
 	
 	/**
 	 * The deinit() Method may not be called from inside junit.
 	 * (No idea why...)
 	 * This method is used as destroy method by spring.  
 	 */
 	public synchronized void deinitAndShutdown()
 	{
 		log.debug("Shutdown called");
 		if (this.pipeline != null) this.pipeline.stop();
 		GstreamerSupport.shutdown(true);
 	}
 
 }
