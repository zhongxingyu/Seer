 package net.bubbaland.trivia.server;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 import javax.swing.Timer;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import net.bubbaland.trivia.Round;
 import net.bubbaland.trivia.ScoreEntry;
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaChartFactory;
 import net.bubbaland.trivia.TriviaInterface;
 import net.bubbaland.trivia.UserList;
 import net.bubbaland.trivia.UserList.Role;
 import org.jfree.chart.ChartUtilities;
 import org.jsoup.HttpStatusException;
 import org.jsoup.Jsoup;
 import org.jsoup.select.Elements;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * The main server that coordinates the trivia contest.
  * 
  * The <code>TriviaServer</code> class contains the <code>main</code> method to start the trivia server and handles all
  * interaction between the centralized <code>Trivia</code> data structure and remote clients. It also downloads the
  * hourly standings from KVSC, parses them and adds them into the <code>Trivia</code> data structure.
  * 
  * The class is also responsible for periodically saving the current <code>Trivia</code> state to an XML file and
  * loading a previous state. The save files are stored in <code>SAVE_DIR</code>, which much exist on the server.
  * 
  */
 @WebService
 public class TriviaServer implements TriviaInterface, ActionListener {
 
 	// File name to store window positions
 	final static private String				SETTINGS_FILENAME	= ".trivia-settings";
 
 	// The number of rounds
 	private static final int				N_ROUNDS			= 50;
 
 	// The number of questions in a normal round
 	private static final int				N_QUESTIONS_NORMAL	= 9;
 
 	// The number of questions in a speed round
 	private static final int				N_QUESTIONS_SPEED	= 18;
 
 	// The team name
 	private static final String				TEAM_NAME			= "Knee Deep in Theses";
 
 	// The server URL
 	private static final String				SERVER_URL			= "www.bubbaland.net";
 
 	// Frequency of backups (milliseconds)
 	private static final int				SAVE_FREQUENCY		= 5 * 60000;
 
 	// Directory to hold backups
 	private static final String				SAVE_DIR			= "saves";
 
 	// Directory to hold charts for publishing
 	private static final String				CHART_DIR			= "charts";
 
 	// Size of chart for web
 	private static final int				CHART_WIDTH			= 800;
 	private static final int				CHART_HEIGHT		= 600;
 
 	// Date format to use for backup file names
 	private static final SimpleDateFormat	fileDateFormat		= new SimpleDateFormat("yyyy_MMM_dd_HHmm");
 
 	// Date format to use inside backup files
 	private static final SimpleDateFormat	stringDateFormat	= new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
 
 	// Base URL for hourly standings
 	final public static String				baseURL				= "http://www.kvsc.org/trivia/points/hour";
 
 	// A user list to track last contact
 	final private UserList					userList;
 
 	// The Trivia object that holds all of the contest data
 	final private Trivia					trivia;
 
 	/**
 	 * Setup properties
 	 */
 	final static public Properties			PROPERTIES			= new Properties();
 	static {
 		/**
 		 * Default properties
 		 */
		final InputStream defaults = TriviaServer.class.getResourceAsStream(SETTINGS_FILENAME);
 		try {
 			PROPERTIES.load(defaults);
 		} catch (IOException e) {
 			System.out.println("Couldn't load default properties file, aborting!");
 			System.exit(-1);
 		}
 		TriviaChartFactory.loadProperties(PROPERTIES);
 	}
 
 	/**
 	 * Creates a new trivia server.
 	 * 
 	 * @throws RemoteException
 	 *             A remote exception
 	 */
 	public TriviaServer() throws RemoteException {
 		this.trivia = new Trivia(TEAM_NAME, N_ROUNDS, N_QUESTIONS_NORMAL, N_QUESTIONS_SPEED);
 		this.userList = new UserList();
 
 		// Create timer that will make save files
 		final Timer backupTimer = new Timer(SAVE_FREQUENCY, this);
 		backupTimer.start();
 	}
 
 	/**
 	 * Handle the save timer triggers.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		final int rNumber = this.trivia.getCurrentRoundNumber();
 		for (int r = 1; r < rNumber; r++) {
 			// For each past round, try to get announced standings if we don't have them
 			if (!this.trivia.isAnnounced(r)) {
 				final ScoreEntry[] standings = getStandings(r);
 				this.trivia.setStandings(r, standings);
 			}
 		}
 		this.saveState();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#callIn(int, java.lang.String)
 	 */
 	@Override
 	public void callIn(int queueIndex, String caller) throws RemoteException {
 		this.userList.updateUserActivity(caller);
 		this.trivia.callIn(queueIndex, caller);
 		this.log(caller + " is calling in item " + queueIndex + " in the answer queue.");
 	}
 
 	@Override
 	public void changeUser(String oldUser, String newUser) throws RemoteException {
 		this.userList.changeUser(oldUser, newUser);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#close(int)
 	 */
 	@Override
 	public void close(String user, int qNumber, String answer) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.close(qNumber, answer);
 		this.log("Question " + qNumber + " closed, "
 				+ this.trivia.getValue(this.trivia.getCurrentRoundNumber(), qNumber) + " points earned.");
 	}
 
 	@Override
 	public void editQuestion(String user, int rNumber, int qNumber, int value, String qText, String aText,
 			boolean isCorrect, String submitter, String operator) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.editQuestion(rNumber, qNumber, value, qText, aText, isCorrect, submitter, operator);
 	}
 
 	@Override
 	public Round[] getChangedRounds(String user, int[] oldVersions) throws RemoteException {
 		this.userList.userHandshake(user);
 		return this.trivia.getChangedRounds(oldVersions);
 	}
 
 	@Override
 	public int getCurrentRound() throws RemoteException {
 		return this.trivia.getCurrentRoundNumber();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#getTrivia()
 	 */
 	@Override
 	public Trivia getTrivia() throws RemoteException {
 		return this.trivia;
 	}
 
 	@Override
 	public Hashtable<String, Role> getActiveUsers(int window, int timeout) throws RemoteException {
 		return this.userList.getActive(window, timeout);
 	}
 
 	@Override
 	public Hashtable<String, Role> getIdleUsers(int window, int timeout) throws RemoteException {
 		return this.userList.getIdle(window, timeout);
 	}
 
 	@Override
 	public void login(String user) throws RemoteException {
 		this.userList.updateUserActivity(user);
 	}
 
 	/**
 	 * Get a list of the available saves.
 	 * 
 	 * @return Array of save file names
 	 */
 	@Override
 	public String[] listSaves() throws RemoteException {
 		final File folder = new File(SAVE_DIR);
 		final File[] files = folder.listFiles();
 		final int nFiles = files.length;
 		final String[] filenames = new String[nFiles];
 		for (int f = 0; f < nFiles; f++) {
 			filenames[f] = files[f].getName();
 		}
 		Arrays.sort(filenames, Collections.reverseOrder());
 		return filenames;
 	}
 
 	/**
 	 * Loads a trivia state from file.
 	 * 
 	 * @param stateFile
 	 *            The name of the file to load
 	 */
 	@Override
 	public void loadState(String user, String stateFile) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		// The full qualified file name
 		stateFile = SAVE_DIR + "/" + stateFile;
 		// Clear all data from the trivia contest
 		this.trivia.reset();
 
 		try {
 			// Open the save file
 			final File infile = new File(stateFile);
 			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			final Document doc = dBuilder.parse(infile);
 			doc.getDocumentElement().normalize();
 
 			// Get the top-level element
 			final Element triviaElement = doc.getDocumentElement();
 
 			// Read/set the trivia parameters
 			this.trivia.setNTeams(Integer.parseInt(triviaElement.getElementsByTagName("Number_of_Teams").item(0)
 					.getTextContent()));
 			this.trivia.setCurrentRound(Integer.parseInt(triviaElement.getElementsByTagName("Current_Round").item(0)
 					.getTextContent()));
 
 			// Get a list of the round elements
 			final NodeList roundElements = triviaElement.getElementsByTagName("Round");
 
 			for (int r = 0; r < roundElements.getLength(); r++) {
 				final Element roundElement = (Element) roundElements.item(r);
 				// Read the round number
 				final int rNumber = Integer.parseInt(roundElement.getAttribute("number"));
 
 				// Read/set if the round is a speed round
 				final boolean speed = roundElement.getElementsByTagName("Speed").item(0).getTextContent()
 						.equals("true");
 				if (speed) {
 					this.trivia.setSpeed(rNumber);
 				}
 
 				this.trivia.setDiscrepencyText(rNumber, roundElement.getElementsByTagName("Discrepancy_Text").item(0)
 						.getTextContent());
 
 				// Get a list of the question elements in this round
 				final NodeList questionElements = roundElement.getElementsByTagName("Question");
 
 				for (int q = 0; q < questionElements.getLength(); q++) {
 					final Element questionElement = (Element) questionElements.item(q);
 					// Read the question number
 					final int qNumber = Integer.parseInt(questionElement.getAttribute("number"));
 
 					// Read/set question parameters
 					final boolean beenOpen = questionElement.getElementsByTagName("Been_Open").item(0).getTextContent()
 							.equals("true");
 					final boolean isOpen = questionElement.getElementsByTagName("Is_Open").item(0).getTextContent()
 							.equals("true");
 					final boolean isCorrect = questionElement.getElementsByTagName("Is_Correct").item(0)
 							.getTextContent().equals("true");
 					final int value = Integer.parseInt(questionElement.getElementsByTagName("Value").item(0)
 							.getTextContent());
 					final String question = questionElement.getElementsByTagName("Question_Text").item(0)
 							.getTextContent();
 					final String answer = questionElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
 					final String submitter = questionElement.getElementsByTagName("Submitter").item(0).getTextContent();
 					final String operator = questionElement.getElementsByTagName("Operator").item(0).getTextContent();
 
 					if (beenOpen) {
 						this.trivia.open(rNumber, qNumber, value, question);
 
 						if (isCorrect) {
 							this.trivia.markCorrect(rNumber, qNumber, answer, submitter, operator);
 						} else if (!isOpen) {
 							this.trivia.close(rNumber, qNumber, answer);
 						}
 					}
 				}
 			}
 
 			final Element element = (Element) triviaElement.getElementsByTagName("Answer_Queue").item(0);
 
 			// Get the list of propsed answer elements in the answer queue
 			final NodeList answerElements = element.getElementsByTagName("Proposed_Answer");
 
 			for (int a = 0; a < answerElements.getLength(); a++) {
 				final Element answerElement = (Element) answerElements.item(a);
 
 				// Read/set parameters of the answer
 				final int qNumber = Integer.parseInt(answerElement.getElementsByTagName("Question_Number").item(0)
 						.getTextContent());
 				final String status = answerElement.getElementsByTagName("Status").item(0).getTextContent();
 				answerElement.getElementsByTagName("Timestamp").item(0).getTextContent();
 				final String answer = answerElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
 				final String submitter = answerElement.getElementsByTagName("Submitter").item(0).getTextContent();
 				final int confidence = Integer.parseInt(answerElement.getElementsByTagName("Confidence").item(0)
 						.getTextContent());
 				final String caller = answerElement.getElementsByTagName("Caller").item(0).getTextContent();
 				final String operator = answerElement.getElementsByTagName("Operator").item(0).getTextContent();
 
 				this.trivia.setAnswer(qNumber, answer, submitter, confidence, status, caller, operator);
 
 			}
 		} catch (final ParserConfigurationException e) {
 
 
 		} catch (final SAXException e) {
 			e.printStackTrace();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 
 		this.log("Loaded state from " + stateFile);
 
 		for (int r = 1; r < this.trivia.getCurrentRoundNumber(); r++) {
 			// For each past round, try to get announced standings if we don't have them
 			if (!this.trivia.isAnnounced(r)) {
 				final ScoreEntry[] standings = getStandings(r);
 				this.trivia.setStandings(r, standings);
 			}
 		}
 
 	}
 
 	/**
 	 * Print a message with timestamp to the console.
 	 * 
 	 * @param message
 	 *            The message
 	 */
 	private void log(String message) {
 		final Date date = new Date();
 		System.out.println(stringDateFormat.format(date) + ": " + message);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#markCorrect(int, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void markCorrect(int queueIndex, String caller, String operator) throws RemoteException {
 		this.userList.updateUserActivity(caller);
 		this.trivia.markCorrect(queueIndex, caller, operator);
 		this.log("Item "
 				+ queueIndex
 				+ " in the queue is correct, "
 				+ this.trivia.getValue(this.trivia.getCurrentRoundNumber(),
 						this.trivia.getAnswerQueueQNumbers()[queueIndex]) + " points earned!");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#markIncorrect(int, java.lang.String)
 	 */
 	@Override
 	public void markIncorrect(int queueIndex, String caller) throws RemoteException {
 		this.userList.updateUserActivity(caller);
 		this.trivia.markIncorrect(queueIndex, caller);
 		this.log("Item " + queueIndex + " in the queue is incorrect.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#markPartial(int, java.lang.String)
 	 */
 	@Override
 	public void markPartial(int queueIndex, String caller) throws RemoteException {
 		this.userList.updateUserActivity(caller);
 		this.trivia.markPartial(queueIndex, caller);
 		this.log("Item " + queueIndex + " in the queue is partially correct.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#markUncalled(String, int)
 	 */
 	@Override
 	public void markUncalled(String user, int queueIndex) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.markUncalled(queueIndex);
 		this.log("Item " + queueIndex + " status reset to uncalled.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#markDuplicate(String, int)
 	 */
 	@Override
 	public void markDuplicate(String user, int queueIndex) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.markDuplicate(queueIndex);
 		this.log("Item " + queueIndex + " marked as duplicate.");
 	}
 
 	// /**
 	// * Test.
 	// */
 	// public void test() {
 	// try {
 	// String[] timestamps = getAnswerQueueTimestamps();
 	// for ( int i = 0; i < timestamps.length; i++ ) {
 	// System.out.println( timestamps[i] );
 	// }
 	// } catch ( Exception e ) {
 	// e.getStackTrace();
 	// }
 	//
 	// }
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see net.bubbaland.trivia.server.TriviaInterface#setAnnounced(int, int, int)
 	// */
 	// @Override
 	// public void setAnnounced(int rNumber, int score, int place) throws RemoteException {
 	// this.trivia.setAnnounced(rNumber, score, place);
 	// this.log("Announced for round " + rNumber + ":");
 	// this.log("Score: " + score + "  Place: " + place);
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#newRound()
 	 */
 	@Override
 	@WebMethod
 	public void newRound(String user) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.log("New round starting...");
 		this.trivia.newRound();
 	}
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see net.bubbaland.trivia.server.TriviaInterface#setNTeams(int)
 	// */
 	// @Override
 	// public void setNTeams(int nTeams) throws RemoteException {
 	// this.trivia.setNTeams(nTeams);
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#open(int, int, java.lang.String)
 	 */
 	@Override
 	public void open(String user, int qNumber, int qValue, String question) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.open(qNumber, qValue, question);
 		this.log("Question " + qNumber + " opened for " + qValue + " Points:\n" + question);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#proposeAnswer(int, java.lang.String, java.lang.String, int)
 	 */
 	@Override
 	public void proposeAnswer(int qNumber, String answer, String submitter, int confidence) throws RemoteException {
 		this.userList.updateUserActivity(submitter);
 		this.trivia.proposeAnswer(qNumber, answer, submitter, confidence);
 		this.log(submitter + " submitted an answer for Q" + qNumber + " with a confidence of " + confidence + ":\n"
 				+ answer);
 	}
 
 	/**
 	 * Save the current trivia state to an xml file.
 	 */
 	private void saveState() {
 
 		// The current date/time
 		final Date time = new Date();
 
 		//
 		final String roundString = "Rd" + String.format("%02d", this.trivia.getCurrentRoundNumber());
 
 		// Timestamp used as part of the filename (no spaces, descending precision)
 		String filename = SAVE_DIR + "/" + roundString + "_" + fileDateFormat.format(time) + ".xml";
 		// Timestamp used in the save file
 		final String createTime = stringDateFormat.format(time);
 
 		try {
 			// Create a document
 			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			final Document doc = docBuilder.newDocument();
 
 			// Create the top-level element
 			final Element triviaElement = doc.createElement("Trivia");
 			doc.appendChild(triviaElement);
 
 			// Make the save time an attribute of Trivia
 			Attr attribute = doc.createAttribute("Save_Time");
 			attribute.setValue(createTime);
 			triviaElement.setAttributeNode(attribute);
 
 			// Save the number of teams
 			Element element = doc.createElement("Number_of_Teams");
 			element.appendChild(doc.createTextNode(this.trivia.getNTeams() + ""));
 			triviaElement.appendChild(element);
 
 			// Save the current round number
 			element = doc.createElement("Current_Round");
 			element.appendChild(doc.createTextNode(this.trivia.getCurrentRoundNumber() + ""));
 			triviaElement.appendChild(element);
 
 			for (int r = 0; r < this.trivia.getCurrentRoundNumber(); r++) {
 				// Create a round element
 				final Element roundElement = doc.createElement("Round");
 				triviaElement.appendChild(roundElement);
 
 				// The round number
 				attribute = doc.createAttribute("number");
 				attribute.setValue(( r + 1 ) + "");
 				roundElement.setAttributeNode(attribute);
 
 				// Whether it is a speed round
 				element = doc.createElement("Speed");
 				element.appendChild(doc.createTextNode(this.trivia.isSpeed(r + 1) + ""));
 				roundElement.appendChild(element);
 
 				// Whether the score has been announced for this round
 				element = doc.createElement("Announced");
 				element.appendChild(doc.createTextNode(this.trivia.isAnnounced(r + 1) + ""));
 				roundElement.appendChild(element);
 
 				// The announced score for this round
 				element = doc.createElement("Announced_Score");
 				element.appendChild(doc.createTextNode(this.trivia.getAnnouncedPoints(r + 1) + ""));
 				roundElement.appendChild(element);
 
 				// The announced place for this round
 				element = doc.createElement("Announced_Place");
 				element.appendChild(doc.createTextNode(this.trivia.getAnnouncedPlace(r + 1) + ""));
 				roundElement.appendChild(element);
 
 				// The discrepancy text for this round
 				element = doc.createElement("Discrepancy_Text");
 				element.appendChild(doc.createTextNode(this.trivia.getDiscrepancyText(r + 1) + ""));
 				roundElement.appendChild(element);
 
 				for (int q = 0; q < this.trivia.getNQuestions(r + 1); q++) {
 					// Create a question element
 					final Element questionElement = doc.createElement("Question");
 					roundElement.appendChild(questionElement);
 
 					// The question number
 					attribute = doc.createAttribute("number");
 					attribute.setValue(( q + 1 ) + "");
 					questionElement.setAttributeNode(attribute);
 
 					// Whether the question has been open
 					element = doc.createElement("Been_Open");
 					element.appendChild(doc.createTextNode(this.trivia.beenOpen(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// Whether the question is currently open
 					element = doc.createElement("Is_Open");
 					element.appendChild(doc.createTextNode(this.trivia.isOpen(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// The value of the question
 					element = doc.createElement("Value");
 					element.appendChild(doc.createTextNode(this.trivia.getValue(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// The question text
 					element = doc.createElement("Question_Text");
 					element.appendChild(doc.createTextNode(this.trivia.getQuestionText(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// The answer
 					element = doc.createElement("Answer_Text");
 					element.appendChild(doc.createTextNode(this.trivia.getAnswerText(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// Whether this question was answered correctly
 					element = doc.createElement("Is_Correct");
 					element.appendChild(doc.createTextNode(this.trivia.isCorrect(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// The submitter for a correctly answered question
 					element = doc.createElement("Submitter");
 					element.appendChild(doc.createTextNode(this.trivia.getSubmitter(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 
 					// The operator who accepted a correct answer
 					element = doc.createElement("Operator");
 					element.appendChild(doc.createTextNode(this.trivia.getOperator(r + 1, q + 1) + ""));
 					questionElement.appendChild(element);
 				}
 			}
 
 			// The size of the answer queue for the current round
 			final int queueSize = this.trivia.getAnswerQueueSize();
 
 			// Create a queue element
 			final Element queueElement = doc.createElement("Answer_Queue");
 			triviaElement.appendChild(queueElement);
 
 			// The size of the queue
 			attribute = doc.createAttribute("size");
 			attribute.setValue(queueSize + "");
 			queueElement.setAttributeNode(attribute);
 
 			for (int a = 0; a < queueSize; a++) {
 				// Create a proposed answer element
 				final Element answerElement = doc.createElement("Proposed_Answer");
 				queueElement.appendChild(answerElement);
 
 				// The question number for this answer
 				element = doc.createElement("Question_Number");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueQNumber(a) + ""));
 				answerElement.appendChild(element);
 
 				// The current status of this answer
 				element = doc.createElement("Status");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueStatus(a)));
 				answerElement.appendChild(element);
 
 				// The time stamp of this answer
 				element = doc.createElement("Timestamp");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueTimestamp(a)));
 				answerElement.appendChild(element);
 
 				// The proposed answer
 				element = doc.createElement("Answer_Text");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueAnswer(a)));
 				answerElement.appendChild(element);
 
 				// The submitter of this answer
 				element = doc.createElement("Submitter");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueSubmitter(a)));
 				answerElement.appendChild(element);
 
 				// The confidence in this answer
 				element = doc.createElement("Confidence");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueConfidence(a) + ""));
 				answerElement.appendChild(element);
 
 				// The user who called this answer in
 				element = doc.createElement("Caller");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueCaller(a)));
 				answerElement.appendChild(element);
 
 				// The operator who accepted the answer as correct
 				element = doc.createElement("Operator");
 				element.appendChild(doc.createTextNode(this.trivia.getAnswerQueueOperator(a)));
 				answerElement.appendChild(element);
 
 			}
 
 			// write the content into xml file
 			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			final Transformer transformer = transformerFactory.newTransformer();
 			final DOMSource source = new DOMSource(doc);
 			final StreamResult result = new StreamResult(new File(filename));
 			transformer.transform(source, result);
 
 			this.log("Saved state to " + filename);
 
 		} catch (final ParserConfigurationException | TransformerException e) {
 			System.out.println("Couldn't save data to file " + filename);
 		}
 
 		if (trivia.isAnnounced(1)) {
 			// Save place chart
 			filename = CHART_DIR + "/" + roundString + "_placeChart.png";
 			try {
 				File file = new File(filename);
 				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makePlaceChart(trivia), CHART_WIDTH,
 						CHART_HEIGHT);
 				this.log("Saved place chart to " + filename);
 			} catch (IOException exception) {
 				System.out.println("Couldn't save place chart to file " + filename);
 			}
 
 			// Save score by round chart
 			filename = CHART_DIR + "/" + roundString + "_scoreByRoundChart.png";
 			try {
 				File file = new File(filename);
 				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeScoreByRoundChart(trivia), CHART_WIDTH,
 						CHART_HEIGHT);
 				this.log("Saved score by round chart to " + filename);
 			} catch (IOException exception) {
 				System.out.println("Couldn't save score by round chart to file " + filename);
 			}
 
 			// Save cumulative score chart
 			filename = CHART_DIR + "/" + roundString + "_cumulativeScoreChart.png";
 			try {
 				File file = new File(filename);
 				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeCumulativePointChart(trivia), CHART_WIDTH,
 						CHART_HEIGHT);
 				this.log("Saved cumulative score chart to " + filename);
 			} catch (IOException exception) {
 				System.out.println("Couldn't save cumulative score chart to file " + filename);
 			}
 
 			// Save team comparison chart
 			filename = CHART_DIR + "/" + roundString + "_teamComparisonChart.png";
 			try {
 				File file = new File(filename);
 				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeTeamComparisonChart(trivia), CHART_WIDTH,
 						CHART_HEIGHT);
 				this.log("Saved team comparison chart to " + filename);
 			} catch (IOException exception) {
 				System.out.println("Couldn't save team comparison chart to file " + filename);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#setDiscrepancyText(int, java.lang.String)
 	 */
 	@Override
 	public void setDiscrepancyText(String user, int rNumber, String discrepancyText) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.setDiscrepencyText(rNumber, discrepancyText);
 	}
 
 	@Override
 	public void setRole(String user, Role role) throws RemoteException {
 		this.userList.updateRole(user, role);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#setSpeed()
 	 */
 	@Override
 	@WebMethod
 	public void setSpeed(String user) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.log("Making round " + this.trivia.getCurrentRoundNumber() + " a speed round");
 		this.trivia.setSpeed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.server.TriviaInterface#unsetSpeed()
 	 */
 	@Override
 	public void unsetSpeed(String user) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.log("Making round " + this.trivia.getCurrentRoundNumber() + " a normal round");
 		this.trivia.unsetSpeed();
 	}
 
 	public void resetQuestion(String user, int qNumber) throws RemoteException {
 		this.userList.updateUserActivity(user);
 		this.trivia.resetQuestion(qNumber);
 	}
 
 	/**
 	 * Fetches the standings for a round from KVSC.
 	 * 
 	 * @param rNumber
 	 *            The round number
 	 * @return Array of ScoreEntry that have the standing data
 	 */
 	public static ScoreEntry[] getStandings(int rNumber) {
 
 		final ArrayList<ScoreEntry> standingsList = new ArrayList<ScoreEntry>(0);
 
 		// The URL where the file is hosted
 		final String urlString = baseURL + String.format("%02d", rNumber) + ".htm";
 		try {
 			// Try to read the URL
 			final org.jsoup.nodes.Document htmlDoc = Jsoup.connect(urlString).get();
 			// Parse the table with the standings from the HTML file
 			final Elements table = htmlDoc.select("table");
 			// Get all rows after the first one (which is the header row)
 			for (final org.jsoup.nodes.Element row : table.select("tr:gt(0)")) {
 				// Get all of the columns in the row
 				final Elements rowData = row.select("td");
 				// Parse the zeroth element as the place
 				int place = Integer.parseInt(rowData.get(0).text());
 				// Parse the first element as the team name
 				final String team = rowData.get(1).text();
 				// Parse the second element as the score
 				final int score = Integer.parseInt(rowData.get(2).text().replaceAll(",", ""));
 
 				// If the score for this line is the same as the previous (a tie), overwrite place to be the same
 				final int entryNumber = standingsList.size();
 				if (entryNumber > 0) {
 					final int lastPlace = standingsList.get(entryNumber - 1).getPlace();
 					final int lastScore = standingsList.get(entryNumber - 1).getScore();
 					if (score == lastScore) {
 						place = lastPlace;
 					}
 				}
 
 				// Create a new ScoreEntry to hold the standing and add it to the list
 				standingsList.add(new ScoreEntry(team, score, place));
 			}
 
 		} catch (final HttpStatusException e) {
 			// The file doesn't exist yet
 			System.out.println("Standings for round " + rNumber + " not available yet.");
 			return null;
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Standings for round " + rNumber + " parsed.");
 		return standingsList.toArray(new ScoreEntry[standingsList.size()]);
 	}
 
 	/**
 	 * Entry point for the server application.
 	 * 
 	 * @param args
 	 *            Unused
 	 * @throws RemoteException
 	 *             A remote exception
 	 */
 	public static void main(String args[]) throws RemoteException {
 		// Replace the local IP with the real hostname
 		System.setProperty("java.rmi.server.hostname", SERVER_URL);
 
 		// Create a registry on port 1099
 		final Registry registry = LocateRegistry.createRegistry(1099);
 
 		// Create a new server
 		final TriviaServer server = new TriviaServer();
 
 		// Attach the server to port 1100
 		try {
 			registry.bind("TriviaInterface", UnicastRemoteObject.exportObject(server, 1100));
 			System.out.println("Trivia Server is Ready");
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		// server.test();
 
 	}
 
 	public void remapQuestion(int oldQNumber, int newQNumber) throws RemoteException {
 		this.trivia.remapQuestion(oldQNumber, newQNumber);
 	}
 
 }
