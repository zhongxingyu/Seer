 /**
  * 
  */
 package client.connectionPipe;
 
 import javax.swing.event.EventListenerList;
 
 import org.gstreamer.Bus;
 import org.gstreamer.Caps;
 import org.gstreamer.Element;
 import org.gstreamer.ElementFactory;
 import org.gstreamer.GstObject;
 import org.gstreamer.Pipeline;
 
 /**
  * @author marc
  * 
  * This gstreamer pipe connects to the given server and informs all connected listeners by pipeline changes.
  * The pipeline has to be initialized {@link #init()} and then set to run {@link #run()}
  */
 public class ConnectionPipe {
 
 	/**
 	 * The pipeline containing all elements and is used to control the gstreamer connection to the server
 	 */
 	private Pipeline pipe;
 	private String host;
 	private int port;
 	private boolean stopped;
 	private EventListenerList listeners;
 
 	/**
 	 * 	The default constructor for this connection pipeline.
 	 * 
 	 * 
 	 *
 	 * @param host the host to which this pipeline should connect
 	 * @param port the port on the host where the pipeline needs to connect
 	 */
 	public ConnectionPipe(String host, int port) {
 		this.host = host;
 		this.port = port;
 		this.stopped=false;
 		this.listeners = new EventListenerList();
 	}
 
 	public void addConnectionPipeListener( ConnectionPipeListener listener ) {
 		listeners.add(ConnectionPipeListener.class, listener);
 	}
 
 	public void removeConnectionPipeListener( ConnectionPipeListener listener ) {
 		listeners.remove( ConnectionPipeListener.class, listener );
 	}
 
 	private synchronized void notifyPipelineEvent(ConnectionPipeEvent event ) {
 		for ( ConnectionPipeListener l : listeners.getListeners( ConnectionPipeListener.class) ) {
 			l.eventAppeared(event);
 			System.out.println(event.getEventType().name() + " Client (" + event.getGstSource().getName() + "): " +event.getMessage());
 		}
 	}
 
 	public void init() {
 		//create the main connection bin
 		Pipeline pipe = new Pipeline("Connection Pipe on the Client");
 		//add error, warning, eos and info listeners
 		this.addBusMessageListeners(pipe);
 
 		//create video source
 		Element src = ElementFactory.make("autovideosrc", "video capturing source");
 		//Element src = ElementFactory.make("videotestsrc", "video capturing test source");
 		pipe.add(src);
 
 		// create a video filter to adapt the framerate
 		Element videorate = ElementFactory.make("videorate", "framerate adaption");
 		pipe.add(videorate);
 		Element ffmpeg1 = ElementFactory.make("ffmpegcolorspace", "color switcher for video filter");
 		pipe.add(ffmpeg1);
 		Element videoFilter = ElementFactory.make("capsfilter", "filter for framerate adaption");
 		videoFilter.setCaps(Caps.fromString("video/x-raw-yuv, framerate=15/1"));
 		pipe.add(videoFilter);
 		Element videoscale = ElementFactory.make("videoscale", "video scaler");
 		pipe.add(videoscale);
 		Element videoFilter2 = ElementFactory.make("capsfilter", "filter for video scaling");
 		videoFilter2.setCaps(Caps.fromString("video/x-raw-yuv, width=320,height=240"));
 		pipe.add(videoFilter2);
 
 		//create a queue if the encoder is a little slow
 		Element enc_queue = ElementFactory.make("queue", "queue in front of the client encoder");
 		pipe.add(enc_queue);
 
 		//create a color space changer if the video is not in the correct color space for the encoder
 		Element ffmpeg2 = ElementFactory.make("ffmpegcolorspace", "color switcher for client encoding");
 		pipe.add(ffmpeg2);
 
 		//create encoder part to send video over the network
 		Element enc= ElementFactory.make ("theoraenc", "theory encoder on client");
 		pipe.add(enc);
 		Element mux = ElementFactory.make("oggmux", "ogg muxer on client");
 		pipe.add(mux);
 
 		// create a queue for the network connection
 		Element net_queue= ElementFactory.make ("queue", "queue for the network on client");
 		pipe.add(net_queue);
 
 		//create the network connection over tcp
 		Element netSink = ElementFactory.make ("tcpclientsink", "tcp client sink");
 		pipe.add(netSink);
 		netSink.set("host", this.host);
 		netSink.set("port", this.port);
		//bugfix netsink internal dataflow problem (because timestamps or so):
		netSink.set("sync", "false");
		
		
 		// link all elements
 		Element.linkMany(src, videorate, ffmpeg1, videoFilter, videoscale, videoFilter2, enc_queue, ffmpeg2, enc, mux, net_queue, netSink);
 
 		//set the pipe
 		this.pipe=pipe;
 	}
 
 	public void run() {
 		if (this.pipe == null) {
 			throw new IllegalStateException("pipe not initialized");
 		}
 
 		if (this.stopped) {
 			throw new IllegalStateException("pipe only once useable. Build a new pipe");
 		}
 
 		//run the pipe
 		this.pipe.play();
 		pipe.debugToDotFile(Pipeline.DEBUG_GRAPH_SHOW_NON_DEFAULT_PARAMS, "client_running_playback_on_port_" + this.port);
 	}
 
 	private void addBusMessageListeners(Pipeline pipe) {
 
 		//get the bus
 		Bus bus = pipe.getBus();
 
 		//connect error messages na stop the pipe if an error occured
 		bus.connect(new Bus.ERROR() {
 
 			@Override
 			public void errorMessage(GstObject source, int code, String message) {
 				ConnectionPipeEvent event = new ConnectionPipeEvent(ConnectionPipe.this, source, ConnectionPipeEventType.GST_ERROR, message);
 				ConnectionPipe.this.notifyPipelineEvent(event);
 				ConnectionPipe.this.stop();
 			}
 		});
 
 		//connect info messages
 		bus.connect(new Bus.INFO() {
 
 			@Override
 			public void infoMessage(GstObject source, int code, String message) {
 				ConnectionPipeEvent event = new ConnectionPipeEvent(ConnectionPipe.this, source, ConnectionPipeEventType.GST_INFO, message);
 				ConnectionPipe.this.notifyPipelineEvent(event);
 			}
 		});
 
 		//connect warnings
 		bus.connect(new Bus.WARNING() {
 
 			@Override
 			public void warningMessage(GstObject source, int code, String message) {
 				ConnectionPipeEvent event = new ConnectionPipeEvent(ConnectionPipe.this, source, ConnectionPipeEventType.GST_WARNING, message);
 				ConnectionPipe.this.notifyPipelineEvent(event);
 			}
 		});
 
 		//connect EOS detection and stop the pipe if EOS detected
 		bus.connect(new Bus.EOS() {
 
 			private boolean reantrance = false;
 
 			@Override
 			public void endOfStream(GstObject source) {
 
 				if (!this.reantrance) {
 					this.reantrance=true;
 					ConnectionPipe.this.stop();
 					ConnectionPipeEvent event = new ConnectionPipeEvent(ConnectionPipe.this, source, ConnectionPipeEventType.STOP, "EOS detected");
 					ConnectionPipe.this.notifyPipelineEvent(event);
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * stops the connection pipeline. After that it is not able to be activated again. (TImeStamp-Problems?)
 	 */
 	public void stop() {
 		//stop everything
 		//check for reentrance and stop
 		if (!this.stopped) {
 			// set flag to avoid reentrance and stop the pipe.
 			// Stopping the pipe should end in an EOS detected message on the bus, which will inform all listeners
 			this.stopped = true;
 			this.pipe.stop();
 		}
 	}
 
 }
