 package client;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 
 import messages.ChatMessage;
 import messages.ChooseSidesMessage;
 import messages.DisconnectMessage;
 import messages.LoseMessage;
 import messages.Message;
 import messages.PlaceMessage;
 import messages.WinMessage;
 import messages.YourTurnMessage;
 
 public class BoardJPanel extends JPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7710401119244316016L;
 
 	// zmienne dotyczace grafiki
 	int nImages = 3; // liczba obrazkow w grze (0=blank,1=black,2=blue)
 	int cSize = 16; // rozmiar komorki w pikselach
 	Image[] img; // tablica obrazkow poszczegolnych pol
 
 	// kody stanow komorek
 	public static final int cBlank = 0;
 	public static final int cBlack = 1;
 	public static final int cBlue = 2;
 
 	// pionek gracza
 	int cMine = cBlack;
 	int cHis = cBlue;
 
 	// gameplay variables
 	volatile int[] field; // tablica zawartosci komorki
 	int rows = 10; // ilosc wierszy
 	int cols = 10; // ilosc kolumn
 	int all_cells = rows * cols; // laczna ilosc komorek
 	int left_cells = all_cells; // komorki niezapelnione
 	volatile boolean isMyTurn = false; // id gracza wykonujacego ruch
 										// (true=local, false=socket)
 	// private static int ME = 1; private static int HIM = 2;
 
 	JLabel statusbar; // pasek stanu
 
 	Socket socket;
 	private ObjectInputStream inputStream;
 	private ObjectOutputStream outputStream;
 
 	private BufferedInputStream bis;
 
 	private BoardJFrame boardJFrame;
 
 	// ladujemy obrazki do tablicy
 	public BoardJPanel(JLabel statusbar, String host, int port, BoardJFrame bf)
 			throws UnknownHostException, IOException {
 		this.statusbar = statusbar;
 
 		socket = new Socket(host, port);
 		outputStream = new ObjectOutputStream(socket.getOutputStream());
 		bis = new BufferedInputStream(
 				socket.getInputStream());
 		inputStream = new ObjectInputStream(bis);
 
 		//JOptionPane.showMessageDialog(null, host + port);
 
 		img = new Image[nImages];
 
 		// pobieramy obrazki z paczki o nazwach 0.png, 1.png...nImages.png
 		for (int i = 0; i < nImages; i++) {
 			img[i] = (new ImageIcon(this.getClass().getResource((i) + ".png")))
 					.getImage();
 		}
 		setDoubleBuffered(true); // rysowanie odbywa sie w buforze poza ekranem
 
 		// podpinamy obserwatora i inicjujemy gre
 		addMouseListener(new BoardAdapter());
 		newGame();
 		
 		boardJFrame = bf;
 	}
 
 	// inicjalizacja nowej gry
 	public void newGame() {
 		// inicjalizacja tablicy zawartosci komorek
 		all_cells = rows * cols;
 		left_cells = all_cells;
 		field = new int[all_cells];
 
 		for (int i = 0; i < all_cells; i++)
 			field[i] = cBlank;
 
 		// pasek stanu
 		statusbar.setText(isMyTurn ? "Your Turn." : "Waiting for opponent...");
 
 		class Worker extends SwingWorker<Object, Object> {
 			volatile boolean done = false;
 			
 			@Override
 			protected Object doInBackground() throws Exception {
 				while (socket.isConnected() && done == false) {
 					try {
 						if (bis.available() > 0) {
 							Object o = inputStream.readObject();
 							System.err.println("<=" + o.toString());
 							if (o instanceof Message) {
 								if (o instanceof YourTurnMessage) {
 									new SwingWorker<Object, Object>(){
 										@Override
 										protected Object doInBackground() {
 											isMyTurn = true;
 											statusbar.setText("Your Turn.");
 											repaint();
 											return null;
 										}
 										
 									}.execute();
 									
 								} else if (o instanceof ChooseSidesMessage) {
 									outputStream.writeObject(Message
 											.getSidesMessage(0));
 								} else if (o instanceof ChatMessage) {
 									// TODO
 								} else if (o instanceof PlaceMessage) {
 									PlaceMessage m = (PlaceMessage) o;
 									field[getFieldId(m.getX(), m.getY())] = cHis;
 									repaint();
 								} else if (o instanceof WinMessage) {
 									new SwingWorker<Object, Object>(){
 										@Override
 										protected Object doInBackground() {
 											isMyTurn = false;
 											JOptionPane.showMessageDialog(new JFrame(), "You win!");
 											disconnect();
 											return null;
 										}
 										
 									}.execute();
 								} else if (o instanceof LoseMessage) {
 									new SwingWorker<Object, Object>(){
 										@Override
 										protected Object doInBackground() {
 											isMyTurn = false;
 											JOptionPane.showMessageDialog(new JFrame(), "You lose!");
 											disconnect();
 											return null;
 										}
 										
 									}.execute();
 								} else if (o instanceof DisconnectMessage) {
 									// TODO
 								} else {
 
 								}
 							}
 						} else {
 							Thread.sleep(50);
 						}
 					} catch (ClassNotFoundException | IOException
 							| InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				endOfGame();
 				return null;
 			}
 			protected void disconnect() {
 				done = true;
 			}
 
 		}
 
 		Worker worker = new Worker();
 		worker.execute();
 
 	}
 
 	public void endOfGame() {
 		boardJFrame.endOfGame();
 	}
 
 	// rysowanie planszy
 	public void paint(Graphics g) {
 		int cell = 0; // id aktualnie rysowanej komorki
 
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				cell = field[(i * cols) + j];
 				g.drawImage(img[cell], (j * cSize), (i * cSize), this);
 			}
 		}
 	}
 
 	int getFieldId(int cRow, int cCol) {
 		return (cRow * cols) + cCol;
 	}
 
 	Map<String, Integer> getCoordsFromFieldId(int id) {
 		Map<String, Integer> result = new HashMap<>();
 		result.put("x", new Integer(id % cols));
 		result.put("y", new Integer(id / cols));
 		return result;
 	}
 
 	// obserwator klikniec myszki
 	class BoardAdapter extends MouseAdapter {
 		public void mousePressed(MouseEvent e) {
 
 			// nie rob nic jezeli ruch nalezy do przeciwnika
 			if (!isMyTurn) return;
 
 			// zamiana wspolrzednych na konkretne pole
 			int x = e.getX();
 			int y = e.getY();
 
 			int cCol = x / cSize;
 			int cRow = y / cSize;
 
 			int field_id = getFieldId(cRow, cCol); // id pola, uzywany w tablicy
 													// fields[]
 
 			// flaga - koniecznosc aktualizacji planszy
 			boolean rep = false;
 
 			// przechwytujemy klikniecie myszki jezeli jest w zakresie planszy
 			if ((x < (cols * cSize)) && (y < (rows * cSize))) {
 				// chwytamy tylko lewy przycisk
 				if (e.getButton() == MouseEvent.BUTTON1) {
 					// poloz pionek tylko gdy pole jest puste
 					if (field[field_id] == cBlank) {
 						try {
 							Map<String, Integer> fieldId = getCoordsFromFieldId(field_id);
 							PlaceMessage m = Message.getPlaceMessage(
 									fieldId.get("y"), fieldId.get("x"));
 							System.err.println("=>" + m.toString());
 							outputStream.writeObject(m);
 
 							field[field_id] = cMine;
 							left_cells--;
 							rep = true; // zmien flage - trzeba zaktualizowac
 										// plansze
 							isMyTurn = false;
 							statusbar.setText("Waiting for opponent...");
 						} catch (IOException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 
 					} else
 						// TODO: negative feedback
 						return;
 				}
 
 				if (rep) {
 					repaint();
 				}
 
 			}
 		}
 	}
 
 }
