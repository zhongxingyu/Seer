 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 public class Screen {
 
 	public static int MAXCHAR = 10;
 	public static URL z = null; 
 	private static String URLstring = "";
 	JPanel contentPane;
 	final static String READER = "Card where words are displayed";
 	final static String TYPER = "Where you type in you URL";
 	private JTextField inputTextField;
 	private JTextArea wordFrequencyTextArea;
 	private static JFrame frame;
 
 	public void createCards() throws IOException {
 		JPanel inputPanel = new JPanel();
 		inputTextField = new JTextField();
 		inputTextField.setFont(new Font("Serif", Font.ITALIC, 16));
 		inputTextField.setColumns(20);
 		inputPanel.add(inputTextField);
 
 		JButton enter = new JButton("enter");
 		SubmitButtonActionListener v = new SubmitButtonActionListener(this.inputTextField, this);
 		enter.addActionListener(v);
 		inputPanel.add(enter);
 
 		
 		contentPane = new JPanel();
 		contentPane.setLayout(new GridLayout(0, 1));
 		contentPane.add(inputPanel);
 		//contentPane.add(myCards);
 		
 		
 		try{
 		}
 		catch(Exception e){
 			createErrorLabel(e.getMessage());
 		}
 
 		// create main pane
 		
 		
 		frame.setContentPane(contentPane);
 	}
 
 	public static void main(String[] args) throws Exception {
 		try {
 			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException ex) {
 			ex.printStackTrace();
 		} catch (InstantiationException ex) {
 			ex.printStackTrace();
 		} catch (ClassNotFoundException ex) {
 			ex.printStackTrace();
 		}
 
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					setUp();
 				} catch (IOException e) {
 
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	public static void setUp() throws IOException {
 		frame = new JFrame("LeaderBoard");
 		// frame.setBounds(50, 50, 600, 400);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Screen scr = new Screen();
 		scr.createCards();
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static String format(String rough) {
 		while(rough.contains(" "))
 			rough = rough.replace(" ", "");
 		rough = rough.replace("\"", "");
 		rough = rough.replace("(", "");
 		rough = rough.replace(")", "");
 		rough = rough.replace(",", "");
 		rough = rough.replace(":", "");
 		rough = rough.replace("^", "");
 		rough = rough.replace(";", "");
 		rough = rough.replaceAll("\t", "");
 		return rough;
 	}
 
 	public static void setURL(String url) throws MalformedURLException {	
 		URLstring = url;
 		z = new URL(URLstring);
 			
 	}
 	public static BufferedReader buff() throws IOException{
 		BufferedReader in;
 		in = new BufferedReader(new InputStreamReader(z.openStream()));
 		return in;
 	}
 	
 	public static String parse(BufferedReader b) throws IOException{
 		String htmlLine;
 		StringBuilder massive = new StringBuilder();
 		while ((htmlLine = b.readLine()) != null) {
 			massive.append(htmlLine);
 		}
 		b.close();
 
 		String massiveString = massive.toString();
 		MyHashMap w = new MyHashMap(MAXCHAR);
 		ArrayList<String> fixedText = new ArrayList<String>();
 		fixedText = (ArrayList<String>) Splitter.split(massiveString);
 		w = WordCounter.reader(fixedText, w);
 		
		ArrayList<KeyValuePairs> x = (ArrayList<KeyValuePairs>) BubbleSort.sort(w.getKeyValuePairs());
 		MyHashMap t = new MyHashMap(MAXCHAR);
 		for (int i = 0; i < x.size(); i++){
 			String key = x.get(i).getKey();
 			String value = x.get(i).getValue();
 			t.set(key,value);
 		}
 		
 		
 		StringBuilder resort = new StringBuilder();
 
 		List<String> allList = t.getKeys();
 		for (int i = 0; i < allList.size(); i++){
 			String myWord;
 			String myValue;
 			myWord = allList.get(i);
 			myValue = t.get(myWord);
 			resort.append(format(myWord) + " " + myValue + "\n");
 		}
 		
 		String finalSort = resort.toString();
 		return finalSort;
 
 	}
 	public void createErrorLabel(String e){
 		JLabel x = new JLabel(e);
 		contentPane.add(x);
 	}
 	public void action() {
 		String finalSort = null;
 		try {
 			finalSort = parse(buff());
 		} catch (IOException e) {
 			String q = e.getMessage();
 			createErrorLabel(q);
 			
 		}
 		contentPane = new JPanel();
 		wordFrequencyTextArea = new JTextArea(finalSort);
 		wordFrequencyTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
 		wordFrequencyTextArea.setLineWrap(true);
 		wordFrequencyTextArea.setWrapStyleWord(true);
 		JScrollPane textAreaScrollPane = new JScrollPane(wordFrequencyTextArea);
 		textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		textAreaScrollPane.setPreferredSize(new Dimension(400, 600));
 		contentPane.add(textAreaScrollPane);
 		
 		frame.setContentPane(contentPane);
 		frame.pack();
 	}
 }
