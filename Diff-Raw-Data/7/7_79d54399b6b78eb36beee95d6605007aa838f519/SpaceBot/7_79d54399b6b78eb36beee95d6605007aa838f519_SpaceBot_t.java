 package nl.nurdspace.irc.spacebot;
 
 import java.net.UnknownHostException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bff.javampd.MPD;
 import org.bff.javampd.MPDPlayer;
 import org.bff.javampd.exception.MPDConnectionException;
 import org.bff.javampd.exception.MPDPlayerException;
 import org.bff.javampd.exception.MPDResponseException;
 import org.bff.javampd.objects.MPDSong;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.hooks.Event;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.ActionEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.managers.ListenerManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * IRC bot to monitor space status and provide other services.
  * 
  * @author bjornl
  */
 public class SpaceBot extends ListenerAdapter implements Listener,
 		SpaceStatusChangeListener {
 	/** Logger. */
 	private static final Logger LOG = LoggerFactory.getLogger(SpaceBot.class);
 
 	private static final DecimalFormat TEMP_FORMAT = new DecimalFormat("##0.0");
 	private static final DecimalFormat SECONDS_FORMAT = new DecimalFormat("00");
 
 	private static final String[] KUTMUZIEKBERICHTEN = new String[]{"Niemand luistert nog naar %s",
 			"Van %s ga ik spontaan aan de spuitpoep",
 			"%s is voor lutsers",
 			"%s schaadt de gezondheid. Het kan longkanker en hartklachten veroorzaken.",
 			"Kut.mu.ziek de; v 1. %s"};
 	private final Channel channel;
 	private final Dimmer dimmer;
 	private final String mpdHost;
 	private int dmxChannel;
 	private int flashRepeats;
 	private int flashTimeOff;
 	private int flashTimeOn;
 	private int lightsOffDelay;
 	private List<Integer> dimmerChannels;
 	private List<DimmerDevice> dimmerDevices;
 
 	private static final Pattern REPLACE_COMMAND = Pattern
 			.compile("(^s|[ #!]s)/[^/]+/[^/]+/?"); // ^s/.+/.+|[
 													// |\\!|\\#]s/.+/.+
 
 	private List<Event> events = new ArrayList<Event>(100);
 
 	public SpaceBot(Channel channel, Dimmer dimmer, String mpdHost, int dimmerChannel, List<Integer> dimmerChannels, List<DimmerDevice> dimmerDevices) {
 		this(channel, dimmer, mpdHost, false, false, dimmerChannel, dimmerChannels, dimmerDevices);
 	}
 
 	public SpaceBot(Channel channel, Dimmer dimmer, String mpdHost, boolean automatic,
 			boolean open, int dimmerChannel, List<Integer> dimmerChannels, List<DimmerDevice> dimmerDevices) {
 		SpaceStatus.getInstance().setOpenAutomatically(automatic);
 		SpaceStatus.getInstance().setOpen(open);
 		this.channel = channel;
 		this.dimmer = dimmer;
 		this.dmxChannel = dimmerChannel;
 		this.mpdHost = mpdHost;
 		this.dimmerChannels = dimmerChannels;
 		this.dimmerDevices = dimmerDevices;
 	}
 
 	public void setDimmerChannel(int dimmerChannel) {
 		this.dmxChannel = dimmerChannel;
 	}
 
 	public void setLightsOffDelay(int lightsOffDelay) {
 		this.lightsOffDelay = lightsOffDelay;
 	}
 
 	public void setFlashRepeats(int flashRepeats) {
 		this.flashRepeats = flashRepeats;
 	}
 
 	public void setFlashTimeOn(int flashTimeOn) {
 		this.flashTimeOn = flashTimeOn;
 	}
 
 	public void setFlashTimeOff(int flashTimeOff) {
 		this.flashTimeOff = flashTimeOff;
 	}
 
 	/**
 	 * Change the topic according to the given status.
 	 * 
 	 * @param open
 	 *            true if the space is open, false if it is closed
 	 */
 	private void changeTopic(boolean open) {
 		LOG.trace("changeTopic: open=" + open);
 		String currentTopic = channel.getTopic();
 		String cleanTopic;
 		if (currentTopic.startsWith("Space is ")) {
 			// Remove current message
 			if (currentTopic.indexOf("|") > 0) {
 				cleanTopic = currentTopic.substring(
 						currentTopic.indexOf("|") + 1).trim();
 			} else {
 				cleanTopic = "";
 			}
 		} else {
 			cleanTopic = currentTopic;
 		}
 		LOG.debug("changeTopic: cleanTopic=[" + cleanTopic + "]");
 		String newTopic = getSpaceOpenMessage() + " | " + cleanTopic;
 		LOG.debug("changeTopic: newTopic=[" + cleanTopic + "]");
 		if (newTopic.equals(currentTopic)) {
 			LOG.trace("changeTopic: current topic matches new topic, ignoring");
 		} else {
 			channel.getBot().setTopic(channel, newTopic);
 			LOG.trace("changeTopic: changed topic");
 		}
 	}
 
 	private String getSpaceOpenMessage() {
 		LOG.trace("getSpaceOpenMessage: start");
 		String spaceOpenMessage = "Space is "
 				+ (SpaceStatus.getInstance().isOpen() ? "OPEN" : "CLOSED");
 		LOG.trace("getSpaceOpenMessage: spaceOpenMessage=[" + spaceOpenMessage
 				+ "]");
 		return spaceOpenMessage;
 	}
 
 	/**
 	 * Easy and recommended way to handle events: Override respective methods in
 	 * {@link ListenerAdapter}.
 	 * <p>
 	 * This example shows how to work with the waitFor ability of PircBotX.
 	 * Follow the inline comments for how this works
 	 * <p>
 	 * *WARNING:* This example requires using a Threaded listener manager (this
 	 * is PircBotX's default)
 	 * 
 	 * @param event
 	 *            A MessageEvent
 	 * @throws Exception
 	 *             If any Exceptions might be thrown, throw them up and let the
 	 *             {@link ListenerManager} handle it. This can be removed though
 	 *             if not needed
 	 */
 	public void onMessage(MessageEvent event) throws Exception {
 		String message = event.getMessage();
 		if (message.startsWith("!")) {
 			// Command
 			handleCommand(event);
 		} else {
 			if (isReplaceCommand(message)) {
 				replace(event);
 			} else {
 				// Add the message to the store for future replacements
 				if (events.size() >= 100) {
 					events.remove(0);
 				}
 				events.add(event);
 			}
 		}
 	}
 
 	@Override
 	public void onAction(ActionEvent event) throws Exception {
 		if (events.size() >= 100) {
 			events.remove(0);
 		}
 		events.add(event);
 	}
 
 	private void handleCommand(MessageEvent event) {
 		String message = event.getMessage();
 		String commandString = message.substring(1);
 		String[] commandArray = commandString.split(" ");
 		String command = commandArray[0];
 		String[] parameters = null;
 		if (commandArray.length > 1) {
 			parameters = new String[commandArray.length - 1];
 			System.arraycopy(commandArray, 1, parameters, 0, parameters.length);
 		}
 		if ("flash".equalsIgnoreCase(command)) {
 			this.flash(event, parameters);
 		} else if ("fade".equalsIgnoreCase(command)) {
 			this.fade(event, parameters);
 		} else if ("devices".equalsIgnoreCase(command)) {
 			StringBuffer devices = new StringBuffer();
 			for (int i = 0; i < this.dimmerDevices.size(); i++) {
 				DimmerDevice device = dimmerDevices.get(i);
 				devices.append(i).append(": [").append(device).append("] ");
 			};
 			event.getBot().sendMessage(event.getChannel(), devices.toString());
 		} else if ("lights".equalsIgnoreCase(command)) {
 			this.lights(event, parameters);
 		} else if ("status".equalsIgnoreCase(command)) {
 			this.status(event, parameters);
 		} else if ("temp".equalsIgnoreCase(command)) {
 			this.temp(event, parameters);
 		} else if ("brul".equalsIgnoreCase(command)) {
 			this.brul(event, parameters);
 		} else if ("beledig".equalsIgnoreCase(command)) {
 			this.beledig(event, parameters);
 		} else if ("kutmuziek".equalsIgnoreCase(command)) {
 			this.skipTrack(event, parameters);
 		} else if ("np".equalsIgnoreCase(command)) {
 			this.showSong(event, parameters);
 		} else if ("element".equalsIgnoreCase(command)) {
 			this.showElement(event, parameters);
 		} else if ("lock".equalsIgnoreCase(command)) {
 			this.showLocks(event, parameters);
 		} else if ("open".equalsIgnoreCase(command) || "state".equalsIgnoreCase(command)) {
 			this.showOpen(event, parameters);
 		}
 	}
 
 	private void showLocks(MessageEvent event, String[] parameters) {
 		event.getBot().sendMessage(event.getChannel(), "Front door is " + (SpaceStatus.getInstance().isFrontDoorLocked() ? "locked" : "unlocked"));
 		event.getBot().sendMessage(event.getChannel(), "Back door is " + (SpaceStatus.getInstance().isBackDoorLocked() ? "locked" : "unlocked"));
 	}
 	
 	private void showElement(MessageEvent event, String[] parameters) {
 		if (parameters.length > 0) {
 			Element element;
 			
 			// Try abbreviation
 			element = Elements.getInstance().getElementByAbbreviation(parameters[0]);
 			if (element == null) {
 				// Try number
 				try {
 					int number = Integer.parseInt(parameters[0]);
 					element = Elements.getInstance().getElementByNumber(number);
 				} catch (NumberFormatException e) {
 					// Not important
 				}
 			}
 			if (element == null) {
 				// Try name
 				element = Elements.getInstance().getElementByName(parameters[0]);
 				// TODO scan through list and check part of name
 			}
 			if (element == null) {
 				event.getBot().sendMessage(event.getChannel(), "element '" + parameters[0] + "' is unknown");
 			} else {
 				event.getBot().sendMessage(event.getChannel(), element.getNumber() + ". " + element.getName() + " (" + element.getAbbreviation() + "); atomic weight: " + element.getWeight());
 			}
 		}
 	}
 	
 	private void showOpen(MessageEvent event, String[] parameters) {
 		event.getBot().sendMessage(event.getChannel(), getSpaceOpenMessage());
 	}
 	
 	private void flash(MessageEvent event, String[] parameters) {
 		if (SpaceStatus.getInstance().isOpen()) {
			dimmer.flash(2 /* this.dmxChannel */, this.flashRepeats,
 					this.flashTimeOn, this.flashTimeOff);
 		} else {
 			event.respond("nope, space is closed");
 		}
 	}
 
 	private void fade(MessageEvent event, String[] parameters) {
 		if (SpaceStatus.getInstance().isOpen()) {
 			// Drie formaten: fade <level>; fade <level> <kanaal>; fade <level> #<device>
 			// Later nog: fade <r,g,b> #<device>
 			switch (parameters.length) {
 			case 0:
 				event.respond("give me a level or RGB value (and optionally a channel/device)");
 				break;
 			case 1:
 				if (parameters[0].startsWith("#")) {
 					// RGB kan niet op default channel
 					event.respond("give me a device if you want to use rgb!");
 				} else {
 					dimmer.fade(dmxChannel, Integer.parseInt(parameters[0]));
 				}
 				break;
 			case 2:
 				if (parameters[0].startsWith("#")) {
 					if (parameters[1].startsWith("#")) {
 						// RGB op device
 						// Check of device rgb is
 						int deviceNumber = Integer.parseInt(parameters[1].substring(1).trim());
 						if (dimmerDevices.get(deviceNumber) instanceof RGBDevice) {
 							RGBDevice device = (RGBDevice) dimmerDevices.get(deviceNumber);
 							if (parameters[0].length() != 7) {
 								event.respond("give me an RGB value in hex (rrggbb), like so: #FF0080");
 							} else {
 								String red = parameters[0].substring(1, 3);
 								String green = parameters[0].substring(3, 5);
 								String blue = parameters[0].substring(5);
 								dimmer.fade(device.getRed(), Integer.parseInt(red, 16));
								dimmer.fade(device.getGreen(), Integer.parseInt(green, 16));
								dimmer.fade(device.getBlue(), Integer.parseInt(blue, 16));
 							}
 						} else {
 							event.respond("give me an RGB device if you want to use rgb!");
 						}
 					} else {
 						event.respond("give me a device, not a channel, if you want to use rgb!");
 					}
 				} else {
 					// "Gewoon" level
 					int level = Integer.parseInt(parameters[0]);
 					if ("all".equals(parameters[1])) {
 						for (int channel : dimmerChannels) {
 							dimmer.fade(channel, level);
 						}
 					} else if (parameters[1].startsWith("#")) {
 						int deviceNumber = Integer.parseInt(parameters[1].substring(1).trim());
 						DimmerDevice device = dimmerDevices.get(deviceNumber);
 						for (int channel : device.getChannels()) {
 							dimmer.fade(channel, level);
 						}
 					} else {
 						// Ouderwets: level op channel
 						dimmer.fade(Integer.parseInt(parameters[1]), level);
 					}
 				}
 			}
 		} else {
 			event.respond("nope, space is closed");
 		}
 	}
 
 	private void lights(MessageEvent event, String[] parameters) {
 		event.getBot()
 				.sendMessage(
 						event.getChannel(),
 						"Fluorescent lighting "
 								+ SpaceStatus.getInstance()
 										.getFluorescentLighting()
 								+ "/1023 ("
 								+ (SpaceStatus.getInstance()
 										.isFluorescentLightOn() ? "ON" : "OFF")
 								+ ")");
 		event.getBot().sendMessage(
 				event.getChannel(),
 				"Dimmed light is at level "
 						+ SpaceStatus.getInstance().getDimmedLightLevel()
 						+ "/255");
 	}
 
 	private void temp(MessageEvent event, String[] parameters) {
 		event.getBot().sendMessage(
 				event.getChannel(),
 				"Space temperature is "
 						+ TEMP_FORMAT.format(SpaceStatus.getInstance()
 								.getTemperature()) + " degrees Celsius");
 	}
 
 	private void status(MessageEvent event, String[] parameters) {
 		event.getBot()
 				.sendMessage(
 						event.getChannel(),
 						getSpaceOpenMessage() + 
 						"; fluorescent lights: "
 								+ (SpaceStatus.getInstance()
 										.isFluorescentLightOn() ? "ON" : "OFF")
 								+ "; dimmed lights: "
 								+ SpaceStatus.getInstance()
 										.getDimmedLightLevel()
 								+ "; "
 								+ TEMP_FORMAT.format(SpaceStatus.getInstance()
 										.getTemperature()) + " C");
 	}
 
 	private void beledig(MessageEvent event, String[] parameters) {
 		// TODO implement
 	}
 
 	private void brul(MessageEvent event, String[] parameters) {
 		String brul = Colors.BOLD + combine(" ", parameters).toUpperCase()
 				+ "!" + Colors.NORMAL;
 		event.getBot().sendMessage(event.getChannel(), brul);
 	}
 
 	private void skipTrack(MessageEvent event, String[] parameters) {
 		try {
 			MPD mpd = new MPD(this.mpdHost, 6600);
 			MPDPlayer mpdPlayer = mpd.getMPDPlayer();
 			MPDSong song = mpdPlayer.getCurrentSong();
 			StringBuilder songInfo = new StringBuilder();
 			songInfo.append(song.getTitle()).append(" van ").append(song.getArtist());
 			int random = new Random(System.currentTimeMillis()).nextInt() % KUTMUZIEKBERICHTEN.length;
 			String message = String.format(KUTMUZIEKBERICHTEN[random], songInfo);
 			event.getBot().sendMessage(event.getChannel(), message);
 			mpdPlayer.playNext();
 			mpd.close();
 		} catch (MPDPlayerException e) {
 			event.respond("sorry, couldn't skip the song");
 			LOG.error("skipTrack: Error skipping", e);
 		} catch (MPDConnectionException e) {
 			event.respond("sorry, couldn't connect to MPD");
 			LOG.error("skipTrack: Error connecting", e);
 		} catch (MPDResponseException e) {
 			LOG.error("skipTrack: Error connecting", e);
 		} catch (UnknownHostException e) {
 			event.respond("sorry, couldn't find the MPD host");
 			LOG.error("skipTrack: Error connecting", e);
 		}
 	}
 
 	private void showSong(MessageEvent event, String[] parameters) {
 		try {
 			MPD mpd = new MPD(this.mpdHost, 6600);
 			MPDPlayer mpdPlayer = mpd.getMPDPlayer();
 			MPDSong song = mpdPlayer.getCurrentSong();
 			StringBuilder songInfo = new StringBuilder("np: ");
 			songInfo.append(song.getArtist()).append(" - ")
 					.append(song.getTitle());
 			songInfo.append(" [").append(song.getLength() / 60).append(":");
 			songInfo.append(SECONDS_FORMAT.format(song.getLength() % 60))
 					.append("]");
 			event.getBot().sendMessage(event.getChannel(), songInfo.toString());
 			mpd.close();
 		} catch (MPDPlayerException e) {
 			event.respond("sorry, couldn't show the song");
 			LOG.error("skipTrack: Error reading current song", e);
 		} catch (MPDConnectionException e) {
 			event.respond("sorry, couldn't connect to MPD");
 			LOG.error("skipTrack: Error connecting", e);
 		} catch (MPDResponseException e) {
 			LOG.error("skipTrack: Error connecting", e);
 		} catch (UnknownHostException e) {
 			event.respond("sorry, couldn't find the MPD host");
 			LOG.error("skipTrack: Error connecting", e);
 		}
 	}
 
 	private String combine(String glue, String... values) {
 		int length = values.length;
 		if (length == 0) {
 			return null;
 		}
 		StringBuilder out = new StringBuilder();
 		out.append(values[0]);
 		for (int x = 1; x < length; ++x)
 			out.append(glue).append(values[x]);
 		return out.toString();
 	}
 
 	@Override
 	public void spaceStatusChanged(int eventType, Object status) {
 		LOG.trace("spaceStatusChanged: " + eventType);
 		switch (eventType) {
 		case SpaceStatusChangeListener.EVENT_SPACE_OPEN_CLOSE:
 			LOG.trace("spaceStatusChanged: open=" + status);
 			if (SpaceStatus.getInstance().isOpenAutomatically()) {
 				this.changeTopic((Boolean) status);
 				if ((Boolean) status) {
 					LOG.info("spaceStatusChanged: opened, fading in");
 					new Thread() {
 						@Override
 						public void run() {
 							dimmer.fadeIn(dmxChannel);
 						}
 					}.start();
 					LOG.info("spaceStatusChanged: opened, fading in started in separate thread");
 				} else {
 					LOG.info("spaceStatusChanged: closed, fading out");
 					new Thread() {
 						@Override
 						public void run() {
 							int i = 0;
 							for (int channel : dimmerChannels) {
 								dimmer.fadeOut(channel, lightsOffDelay + 3 * i++);
 							}
 						}
 					}.start();
 					LOG.info("spaceStatusChanged: fading out started in separate thread");
 				}
 			}
 			break;
 		case SpaceStatusChangeListener.EVENT_BACK_DOOR_LOCK:
 			LOG.trace("spaceStatusChanged: backDoorLocked=" + status);
 			this.channel.getBot().sendMessage(this.channel, "back door was " + (((Boolean) status).booleanValue() ? "locked" : "unlocked"));
 			break;
 		case SpaceStatusChangeListener.EVENT_FRONT_DOOR_LOCK:
 			LOG.trace("spaceStatusChanged: frontDoorLocked=" + status);
 			this.channel.getBot().sendMessage(this.channel, "front door was " + (((Boolean) status).booleanValue() ? "locked" : "unlocked"));
 			break;
 		}
 	}
 
 	private void replace(final MessageEvent event) {
 		if (isReplaceCommand(event.getMessage())) {
 			LOG.debug("process: message contains a replace command");
 			String replaceCommand = getReplaceCommand(event.getMessage());
 			LOG.debug("process: replace command: " + replaceCommand);
 			String[] commandElements = replaceCommand.split("/");
 			String snippet = commandElements[1];
 			String replacement = commandElements[2];
 			String origin;
 
 			List<Event> reversedMessages = new ArrayList<Event>(100);
 			reversedMessages.addAll(events);
 			Collections.reverse(reversedMessages);
 			String messageText;
 			for (Event aMessage : reversedMessages) {
 
 				if (aMessage instanceof MessageEvent) {
 					messageText = ((MessageEvent) aMessage).getMessage();
 				} else if (aMessage instanceof ActionEvent) {
 					messageText = ((ActionEvent) aMessage).getAction();
 				} else {
 					LOG.error("process: unsupported message type in store: "
 							+ aMessage.getClass().getName());
 
 					throw new IllegalStateException(
 							"Unsupported message type in store");
 
 				}
 
 				if (messageText.contains(snippet)) {
 
 					String replaced = messageText.replace(snippet, replacement);
 					if (aMessage instanceof MessageEvent) {
 						MessageEvent message = (MessageEvent) aMessage;
 						aMessage.getBot().sendMessage(
 								message.getChannel(),
 								"<" + message.getUser().getNick() + "> " + replaced);
 						MessageEvent replacedMessage = new MessageEvent(message.getBot(), message.getChannel(), message.getUser(), replaced);
 						if (events.size() >= 100) {
 							events.remove(0);
 						}
 						events.add(replacedMessage);
 					} else if (aMessage instanceof ActionEvent) {
 						ActionEvent action = (ActionEvent) aMessage;
 						aMessage.getBot().sendMessage(
 								action.getChannel(), 
 								"* " + action.getUser().getNick() + " "
 								+ replaced);
 						ActionEvent replacedAction = new ActionEvent(action.getBot(), action.getUser(), action.getChannel(), replaced);
 						if (events.size() >= 100) {
 							events.remove(0);
 						}
 						events.add(replacedAction);
 					}
 
 					else {
 
 						// This cannot happen
 
 						LOG.error("process: unsupported message type in store: "
 								+ aMessage.getClass().getName());
 
 						throw new IllegalStateException(
 								"Unsupported message type in store");
 
 					}
 
 					break;
 
 				}
 
 			}
 
 		}
 
 		else {
 
 			LOG.debug("process: message is not a replace command");
 			//
 			// if (messageToProcess instanceof PublicMessage
 			// || messageToProcess instanceof Action) {
 			//
 			// LOG.debug("process: message is added to the store");
 			//
 			// messages.addMessage(messageToProcess);
 			//
 			// }
 			//
 			// return false;
 
 		}
 
 	}
 
 	/**
 	 * 
 	 * Detect whether the given message contains a replace command.
 	 * 
 	 * 
 	 @param message
 	 *            the message
 	 * 
 	 * 
 	 @return true if the message contains a replace command
 	 */
 
 	private boolean isReplaceCommand(final String message) {
 
 		return REPLACE_COMMAND.matcher(message).find();
 
 	}
 
 	/**
 	 * 
 	 * Retrieves the replace command from the given message.
 	 * 
 	 * 
 	 @param message
 	 *            the message
 	 * 
 	 * 
 	 @return true if the message contains a replace command
 	 */
 
 	private String getReplaceCommand(final String message) {
 
 		Matcher matcher = REPLACE_COMMAND.matcher(message);
 
 		if (!matcher.find()) {
 
 			throw new IllegalArgumentException(
 					"The message does not contain a replace command");
 
 		}
 
 		return matcher.group();
 
 	}
 }
