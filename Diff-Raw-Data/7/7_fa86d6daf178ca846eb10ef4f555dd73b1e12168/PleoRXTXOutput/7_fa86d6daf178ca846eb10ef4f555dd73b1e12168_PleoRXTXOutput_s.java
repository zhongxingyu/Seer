 package pleocmd.pipe.out;
 
 import gnu.io.CommPortIdentifier;
 
 import java.awt.EventQueue;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.concurrent.TimeoutException;
 
 import pleocmd.Log;
 import pleocmd.StringManip;
 import pleocmd.api.PleoCommunication;
 import pleocmd.cfg.ConfigItem;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.OutputException;
 import pleocmd.itfc.gui.MainFrame;
 import pleocmd.pipe.data.CommandData;
 import pleocmd.pipe.data.Data;
 
 public final class PleoRXTXOutput extends Output {
 
 	private final ConfigItem<String> cfgDevice;
 
 	private PleoCommunication pc;
 
 	private Thread thrUpdateStatusLabel;
 
 	public PleoRXTXOutput() {
 		ConfigItem<String> tmp;
 		try {
 			tmp = new ConfigItem<String>("Device", "", getAllDeviceNames());
 		} catch (final UnsatisfiedLinkError e) {
 			Log.error(e, "Cannot find external library in '%s'", System
 					.getProperty("java.library.path"));
 			tmp = new ConfigItem<String>("Device", "", new ArrayList<String>());
 		}
 		addConfig(cfgDevice = tmp);
 		constructed();
 	}
 
 	public PleoRXTXOutput(final String device) throws ConfigurationException {
 		this();
 		cfgDevice.setContent(device);
 	}
 
 	private static List<String> getAllDeviceNames() {
 		final List<String> names = new ArrayList<String>();
 		try {
 			final List<CommPortIdentifier> ports = PleoCommunication
 					.getAvailableSerialPorts();
 			for (final CommPortIdentifier port : ports)
 				names.add(port.getName());
 		} catch (final NoClassDefFoundError e) {
 			Log.error(e, "RXTX not available");
 		}
 		return names;
 	}
 
 	@Override
 	protected void init0() throws IOException, OutputException {
 		Log.detail("Initializing PleoRXTXOutput for device '%s'", cfgDevice
 				.getContent());
 		try {
 			pc = new PleoCommunication(PleoCommunication.getPort(cfgDevice
 					.getContent()));
 		} catch (final NoClassDefFoundError e) {
 			throw new OutputException(this, true, e, "RXTX not available");
 		}
 		pc.init();
 		thrUpdateStatusLabel = new Thread() {
 			@Override
 			public void run() {
 				while (isThrUpdateStatusLabel()) {
 					try {
 						final String res;
 						synchronized (PleoRXTXOutput.this) {
 							if (!MainFrame.hasGUI()
 									|| !isThrUpdateStatusLabel()) break;
 							getPC().send("STATS POWER");
 							res = getPC().readAnswer();
 						}
 						final StringTokenizer lineTok = new StringTokenizer(
 								res, "\n\r");
 						String power = "???";
 						while (lineTok.hasMoreTokens()) {
 							final String line = lineTok.nextToken();
 							if (line.contains("Battery sensor value")) {
 								final int idx1 = line.indexOf("=");
 								if (idx1 != -1) {
 									power = line.substring(idx1 + 1);
 									final int idx2 = power.indexOf("(");
 									if (idx2 != -1)
 										power = power.substring(0, idx2);
 								}
 								break;
 							}
 						}
 						final String powerFinal = String.format(
 								"Battery: %s%%", power.trim());
 						EventQueue.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								MainFrame.the().updateStatusLabel(
 										PleoRXTXOutput.this, powerFinal);
 							}
 						});
 					} catch (final TimeoutException e) {
						MainFrame.the().updateStatusLabel(this, "ERROR");
 					} catch (final IOException e) {
						MainFrame.the().updateStatusLabel(this, "ERROR");
 					}
 					try {
 						Thread.sleep(5000);
 					} catch (final InterruptedException e1) {
 						break;
 					}
 				}
 				EventQueue.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						MainFrame.the().updateStatusLabel(PleoRXTXOutput.this,
 								"");
 					}
 				});
 			}
 		};
 		thrUpdateStatusLabel.setDaemon(true);
 		thrUpdateStatusLabel.start();
 	}
 
 	protected synchronized boolean isThrUpdateStatusLabel() {
 		return thrUpdateStatusLabel != null;
 	}
 
 	protected PleoCommunication getPC() {
 		return pc;
 	}
 
 	@Override
 	protected void close0() {
 		Log.detail("Closing PleoRXTXOutput '%s' for device '%s'", pc, cfgDevice
 				.getContent());
 		synchronized (this) {
 			pc.close();
 			pc = null;
 			thrUpdateStatusLabel.interrupt();
 			thrUpdateStatusLabel = null;
 		}
 	}
 
 	@Override
 	public String getInputDescription() {
 		return "PMC";
 	}
 
 	@Override
 	protected String getShortConfigDescr0() {
 		return cfgDevice.getContent();
 	}
 
 	@Override
 	protected boolean write0(final Data data) throws OutputException,
 			IOException {
 		if (!CommandData.isCommandData(data, "PMC")) return false;
 		synchronized (this) {
 			if (Log.canLogInfo())
 				Log.info("<html>Sent to Pleo: "
 						+ StringManip.printSyntaxHighlightedAscii(data));
 			pc.send(CommandData.getArgument(data));
 			try {
 				Log.consoleOut(pc.readAnswer());
 			} catch (final TimeoutException e) {
 				throw new OutputException(this, true, e,
 						"Cannot read answer for command '%s'", data);
 			}
 		}
 		return true;
 	}
 
 	public static String help(final HelpKind kind) { // NO_UCD
 		switch (kind) {
 		case Name:
 			return "Pleo RXTX Output";
 		case Description:
 			return "Processes commands like 'PMC|foo' by sending 'foo' "
 					+ "without any modifications to the Pleo via an USB "
 					+ "connection by using the RXTX library";
 		case Config1:
 			return "Path to the device on which the Pleo is connected to "
 					+ "this computer";
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public String isConfigurationSane() {
 		try {
 			PleoCommunication.getPort(cfgDevice.getContent());
 		} catch (final NoClassDefFoundError e) {
 			return "RXTX not available";
 		} catch (final IOException e) {
 			return String.format("Device '%s' does not exist", cfgDevice
 					.getContent());
 		}
 		return null;
 	}
 
 	@Override
 	protected int getVisualizeDataSetCount() {
 		return 0;
 	}
 
 }
