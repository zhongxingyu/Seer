 package ch.cern.atlas.apvs.dosimeter.server;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.util.HashedWheelTimer;
 import org.jboss.netty.util.Timeout;
 import org.jboss.netty.util.Timer;
 import org.jboss.netty.util.TimerTask;
 
 import ch.cern.atlas.apvs.domain.Dosimeter;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.dosimeter.shared.DosimeterChangedEvent;
 import ch.cern.atlas.apvs.dosimeter.shared.DosimeterPtuChangedEvent;
 import ch.cern.atlas.apvs.dosimeter.shared.DosimeterSerialNumbersChangedEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.cedarsoftware.util.io.JsonWriter;
 
 public class DosimeterClientHandler extends SimpleChannelUpstreamHandler {
 	
 	private static final int RECONNECT_DELAY = 20;
 
 	private static final Logger logger = Logger
 			.getLogger(DosimeterClientHandler.class.getName());
 	private final ClientBootstrap bootstrap;
 	private final RemoteEventBus eventBus;
 	
 	private InetSocketAddress address;
 	private Channel channel;
 	private Timer timer;
 	private boolean reconnectNow;
 
 	private Map<Integer, Dosimeter> dosimeters;
 	private Map<Integer, String> dosimeterToPtu;
 	
 	private FileHandler logHandler;
 
 	@SuppressWarnings("serial")
 	public DosimeterClientHandler(ClientBootstrap bootstrap, final RemoteEventBus eventBus) {
 		this.bootstrap = bootstrap;
 		this.eventBus = eventBus;
 		init();
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(DosimeterSerialNumbersChangedEvent.class
 						.getName())) {
 					eventBus.fireEvent(new DosimeterSerialNumbersChangedEvent(
 							getDosimeterSerialNumbers()));
 				} else if (type.equals(DosimeterChangedEvent.class.getName())) {
 					for (Iterator<Integer> i = dosimeters.keySet().iterator(); i
 							.hasNext();) {
 						eventBus.fireEvent(new DosimeterChangedEvent(
 								getDosimeter(i.next())));
 					}
 				} else if (type.equals(MeasurementChangedEvent.class.getName())) {
 					for (Iterator<Integer> i = dosimeters.keySet().iterator(); i
 							.hasNext();) {
 						Dosimeter dosimeter = getDosimeter(i.next());
 						String ptuId = dosimeterToPtu.get(dosimeter.getSerialNo());
 						if (ptuId != null) {
 							sendMeasurements(ptuId, dosimeter);
 						}
 					}
 				}
 			}
 		});
 		
 		DosimeterPtuChangedEvent.subscribe(eventBus, new DosimeterPtuChangedEvent.Handler() {
 			
 			@Override
 			public void onDosimeterPtuChanged(DosimeterPtuChangedEvent event) {
 				dosimeterToPtu = event.getDosimeterToPtu();
 			}
 		});
 	}
 	
 	private void init() {
 		dosimeters = new HashMap<Integer, Dosimeter>();
 		dosimeterToPtu = new HashMap<Integer, String>();
 		
 		String dosimeterLogfile = "APVS-Dosimeter.log";
 		// setup the logger
 		try {
 			logHandler = new FileHandler(dosimeterLogfile, 1000000, 1000, true);
 			logHandler.setLevel(Level.INFO);
 			logHandler.setFormatter(new Formatter() {
 				
 				@Override
 				public String format(LogRecord record) {
 					return record.getMessage()+"\n";
 				}
 			});
 		} catch (SecurityException e) {
 			System.err.println("Cannot open "+dosimeterLogfile);
 		} catch (IOException e) {
 			System.err.println("Cannot open "+dosimeterLogfile);
 		}
 	}
 
 	@Override
 	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
 			throws Exception {
 		if (e instanceof ChannelStateEvent) {
 			logger.info(e.toString());
 		}
 		super.handleUpstream(ctx, e);
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		channel = e.getChannel();
 		super.channelConnected(ctx, e);
 	}
 
 	@Override
 	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// handle closed connection
 		System.err.println("Closed Dosimeter socket, invalidated DosiIds");
 		init();
 		eventBus.fireEvent(new DosimeterSerialNumbersChangedEvent(new ArrayList<Integer>()));
 
 		// handle (re)connection
 		channel = null;
 		if (reconnectNow) {
 			System.err.println("Immediate Reconnecting to Dosimeter on "+address);
 			bootstrap.connect(address);
 			reconnectNow = false;
 		} else {
 			System.err.println("Sleeping for: " + RECONNECT_DELAY + "s");
 			timer = new HashedWheelTimer();
 			timer.newTimeout(new TimerTask() {
 				public void run(Timeout timeout) throws Exception {
 					System.err.println("Reconnecting to Dosimeter on "+address);
 					bootstrap.connect(address);
 				}
 			}, RECONNECT_DELAY, TimeUnit.SECONDS);
 		}
 		
 		super.channelClosed(ctx, e);
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
 		// Print out the line received from the server.
 		String line = (String)e.getMessage();
 //		System.err.println(line);
 		Dosimeter dosimeter = DosimeterCoder.decode(line, new Date());
 		if (dosimeter == null) return;
 		
 		if (dosimeters.put(dosimeter.getSerialNo(), dosimeter) == null) {
 			eventBus.fireEvent(new DosimeterSerialNumbersChangedEvent(
 					getDosimeterSerialNumbers()));
 		}
 		eventBus.fireEvent(new DosimeterChangedEvent(dosimeter));
 		
 		try {
 			String jsonDosimeter = JsonWriter.objectToJson(dosimeter);
 		
 			if (logHandler != null) {
 				logHandler.publish(new LogRecord(Level.INFO, jsonDosimeter));
 			}
 		} catch (IOException ex) {
 			logger.warning(DosimeterClientHandler.class+" cannot convert to JSON "+ex.getMessage());
 		}
 		
 		String ptuId = dosimeterToPtu.get(dosimeter.getSerialNo());
 		if (ptuId != null) {
 			sendMeasurements(ptuId, dosimeter);
 		}		
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
 		if (e.getCause() instanceof ConnectException) {
 			logger.log(Level.WARNING, "Connection Refused");
 		} else if (e.getCause() instanceof SocketException) {
 			logger.log(Level.WARNING, "Network is unreachable");
 		} else {
 			logger.log(Level.WARNING, "Unexpected exception from downstream.",
 					e.getCause());
 		}
 		e.getChannel().close();
 	}
 
 	public void connect(InetSocketAddress newAddress) {
 		if (newAddress.equals(address)) return;
 		
 		address = newAddress;
 
 		if (channel != null) {
 			reconnect(true);
 		} else {
 			bootstrap.connect(address);
 		}
 	}
 
 	public void reconnect(boolean reconnectNow) {
 		this.reconnectNow = reconnectNow;
 		
 		if (timer != null) {
 			timer.stop();
 			timer = null;
 		}
 		
 		if (channel != null) {
 			channel.disconnect();
 			channel = null;
 		}
 	}	
 
 	private void sendMeasurements(String ptuId, Dosimeter dosimeter) {
 		Measurement rate = new Measurement(
 				ptuId, "Dosimeter Rate", dosimeter.getRate(), "&micro;Sv/h",
 				dosimeter.getDate());
 		Measurement dose = new Measurement(
 				ptuId, "Dosimeter Dose", dosimeter.getDose(), "&micro;Sv", dosimeter.getDate());
 		eventBus.fireEvent(new MeasurementChangedEvent(rate));
 		eventBus.fireEvent(new MeasurementChangedEvent(dose));
 	}
 
 	public List<Integer> getDosimeterSerialNumbers() {
 		List<Integer> list = new ArrayList<Integer>(dosimeters.keySet());
 		Collections.sort(list);
 		return list;
 	}
 
 	public Dosimeter getDosimeter(int serialNo) {
 		return dosimeters.get(serialNo);
 	}
 
 	public Map<Integer, Dosimeter> getDosimeterMap() {
 		return dosimeters;
 	}
 }
