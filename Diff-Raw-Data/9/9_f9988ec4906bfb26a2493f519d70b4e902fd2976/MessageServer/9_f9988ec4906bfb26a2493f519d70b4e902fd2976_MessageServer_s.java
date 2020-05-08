 /**
  * PAaneINt 
  * 
  * Created for the course intro Human-Computer Interaction at the
  * Radboud Universiteit Nijmegen
  * 
  * 2013
  */
 package nl.PAINt;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  * @author Luuk Scholten & Thom Wiggers
  * 
  */
 public class MessageServer {
 
 	ServerSocket server;
 	Socket socket;
 	Logger logger = Logger.getLogger(MessageServer.class);
 
 	BlockingQueue<String> outqueue = new LinkedBlockingQueue<>();
 	private CanvasPanel canvas;
 
 	/**
 	 * 
 	 */
 	public MessageServer(final MainWindow window) {
 		try {
 			logger.debug("opening server socket on port 2222");
 			server = new ServerSocket(2222);
 			server.setSoTimeout(0);
 
 		} catch (IOException e) {
 			logger.error("IO Error", e);
 			e.printStackTrace();
 		}
 
 		Thread t1 = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						logger.debug("trying to open a read socket");
 						socket = server.accept();
 					} catch (IOException e1) {
 						logger.error("IO Error when opening Socket", e1);
 						e1.printStackTrace();
 					}
 
 					try {
 						BufferedReader reader = new BufferedReader(new InputStreamReader(
 								socket.getInputStream(), "UTF-8"));
 
 						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
 								socket.getOutputStream(), "UTF-8"));
 
 						StreamReader sr = new StreamReader(reader, window);
 
 						logger.debug("starting threads");
 						Thread t1 = new Thread(sr);
 						t1.setName("Input Thread");
 						t1.setDaemon(true);
 						t1.start();
 
 						StreamWriter sw = new StreamWriter(writer, outqueue);
 
 						Thread t2 = new Thread(sw);
 						t2.setName("Output Thread");
 						t2.setDaemon(true);
 						t2.start();
 					} catch (IOException e) {
 						logger.error("IO error while starting threads", e);
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		t1.setDaemon(true);
 		t1.start();
 		this.canvas = window.getCanvas();
 		canvas.setApiServer(this);
 	}
 
 	class StreamReader implements Runnable {
 
 		private BufferedReader reader = null;
 		private MainWindow window = null;
 		private CanvasPanel canvas = null;
 		private Logger logger = Logger.getLogger(StreamReader.class);
 
 		public StreamReader(BufferedReader inputReader, MainWindow window) {
 			reader = inputReader;
 			this.window = window;
 			this.canvas = window.getCanvas();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		@Override
 		public void run() {
 			String line = null;
 			try {
 				while ((line = reader.readLine()) != null) {
 					logger.info("received line " + line);
 
 					line = line.toLowerCase();
 					String[] lineParts = line.split(" ");
 					switch (lineParts[0]) {
 					case "jemoeder":
 						System.out.println("Je moeder is een linepart");
 						break;
 					case "start":
 						window.connected();
 						break;
 					case "select":
 						canvas.setMode(PanelMode.SELECT);
 						break;
 					case "drawmode":
 						if (lineParts.length >= 2) {
 							switch (lineParts[1]) {
 							case "triangle":
 								canvas.setMode(PanelMode.TRIANGLE);
 								break;
 							case "rectangle":
 								canvas.setMode(PanelMode.RECTANGLE);
 								break;
 							case "ellipse":
 								canvas.setMode(PanelMode.ELLIPSE);
 								break;
 							case "line":
 								canvas.setMode(PanelMode.LINE);
 								break;
 							case "text":
 								canvas.setMode(PanelMode.TEXT);
 								break;
 							default:
 								throw new IllegalArgumentException(
 										"Illegal API call, operation drawmode " + lineParts[1]
 												+ " unknown");
 
 							}
 						} else
 							throw new IllegalArgumentException(
 									"Not enough parameters for operation drawmode");
 						break;
 					case "applycolor":
 						if (lineParts.length < 2)
 							throw new IllegalArgumentException(
 									"Not enough parameters for operation applycolor");
 						switch (lineParts[1]) {
 						case "line":
 							canvas.applyLineColor();
 							break;
 						case "fill":
 							canvas.applyFill();
 							break;
 						default:
 							throw new IllegalArgumentException(
 									"Illegal API call, operation applycolor doesn't support "
 											+ lineParts[1]);
 
 						}
 						break;
 					case "unfill":
 						canvas.removeFill();
 						break;
 					case "delete":
 						canvas.deleteSelected();
 						break;
 					case "rotate":
 						if (lineParts.length < 2) {
 							throw new IllegalArgumentException(
 									"not enough parameters for operation rotate");
 						}
 						switch (lineParts[1]) {
 						case "+":
 							canvas.rotateSelected(10);
 							break;
 						case "-":
 							canvas.rotateSelected(-10);
 							break;
 						default:
 							throw new IllegalArgumentException(
 									"Illegal parameter for operation rotate: " + lineParts[1]);
 						}
 					case "z-index":
 						if (lineParts.length < 2)
 							throw new IllegalArgumentException(
 									"missing parameter for z-index");
 						switch (lineParts[1]) {
 						case "+":
 							canvas.moveSelectedForward();
 							break;
 						case "-":
 							canvas.moveSelectedBackward();
 							break;
 						}
 						break;
 					default:
 						throw new IllegalArgumentException("Unknown command specified: ");
 					}
 				}
 			} catch (IOException e) {
 				logger.error("IO error in read thread", e);
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	class StreamWriter implements Runnable {
 
 		BufferedWriter writer;
 		BlockingQueue<String> queue;
 		Logger logger = Logger.getLogger(StreamReader.class);
 
 		public StreamWriter(BufferedWriter outputWriter, BlockingQueue<String> queue) {
 			writer = outputWriter;
 			this.queue = queue;
 			logger.setLevel(Level.ALL);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		@Override
 		public void run() {
 
 			try {
 				while (true) {
 
 					String poll = queue.take();
 					String line = null;
 					if (poll != null) {
 						line = poll.trim();
 					}
 
 					if (line != null) {
 						try {
 							logger.debug("Sending command " + line);
 							writer.write(line + "\r\n");
 							writer.flush();
 						} catch (IOException e) {
 							logger.error("IO error", e);
 							e.printStackTrace();
 						}
 					}
 
 				}
 			} catch (InterruptedException e) {
 				logger.debug("Interrupted", e);
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * @param mode
 	 */
 	public void sendContext(PanelMode mode) {
 		logger.info("Sending context for mode " + mode.toString());
 		switch (mode) {
 		case DELETE:
 			break;
 		case ELLIPSE:
 		case ELL_FILLED:
 			this.outqueue.add("context ellipse");
 			break;
 		case LINE:
 			this.outqueue.add("context line");
 			break;
 		case MOVE:
 		case SELECT:
 			this.outqueue.add("context select");
 			break;
 		case RECTANGLE:
 		case RECT_FILLED:
 			this.outqueue.add("context rectangle");
 			break;
 		case TEXT:
 			this.outqueue.add("context text");
 			break;
 		case TRIANGLE:
 			this.outqueue.add("context triangle");
 			break;
 		case NONE:
 			this.outqueue.add("context none");
 			break;
 		}
 	}
 
 }
