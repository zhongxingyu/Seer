 package com.cse454.nel;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URI;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
 import com.cse454.nel.features.FeatureWeights;
 import com.cse454.nel.features.InLinkFeatureGenerator;
 import com.cse454.nel.search.CrossWikiSearcher;
 
 public class Demo {
 	private boolean isNeedCursorChange = true;
 
 	private final JPanel panel = new JPanel();
 	private DocumentProcessor processor;
 	private FeatureWeights weights;
 
 	private JTextArea input;
 	private JTextPane output;
 
 	private final String wikipedia = "http://en.wikipedia.org/wiki/";
 	private JTextField weight1;
 	private JTextField weight2;
 	private JTextField weight3;
 
 	// Default weights to use
 	private int inLinkWeight = 1;
 	private int crossWikiWeight = 1;
 	private int histogramWeight = 1;
 
 
 	public Demo() throws Exception {
		DocPreProcessor preProcessor = new DocPreProcessor();
		processor = new DocumentProcessor(preProcessor);
		
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				createAndShowGUI();
 			}
 		});
 
		
 
 		// Here's necessary feature weight input (values will come from gui)
 
 
 
 		// Process the document
 
 		// now you could compare each { token | entity } in a scroll bar on the
 		// gui or something
 
 	}
 
 	public static void main(String[] args) throws Exception {
 		new Demo();
 	}
 
 
 	public void Link() {
 		weights = new FeatureWeights();
 		int in;
 		int cross;
 		int hist;
 
 		try {
 			in = Integer.parseInt(weight1.getText());
 		}
 		catch(Exception e) {
 			in = inLinkWeight;
 		}
 
 		try {
 			cross = Integer.parseInt(weight2.getText());
 		}
 		catch(Exception e) {
 			cross = crossWikiWeight;
 		}
 
 		try {
 			hist = Integer.parseInt(weight3.getText());
 		}
 		catch(Exception e) {
 			hist = histogramWeight;
 		}
 
 		System.out.println("In: "+Integer.toString(in));
 		System.out.println("Cross: "+Integer.toString(cross));
 		System.out.println("Hist: "+Integer.toString(hist));
 
 		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, in);
 		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, cross);
 		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, hist);
 
 		// Here's the text to be linked (value to come from gui)
 		String text = input.getText();
 		List<Sentence> results;
 		try {
 			results = processor.ProcessDocument(weights, text);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			// JOptionPane.showMessageDialog(null, e.getMessage());
 			return;
 		}
 
 		StringBuilder builder = new StringBuilder();
 
 		System.out.println(results.size() + " sentences");
 		for (Sentence s : results) {
 			String[] ents = s.getEntities();
 			String[] tokens = s.getTokens();
 			if (ents == null) {
 				System.out.println("Entities is null");
 				continue;
 			}
 
 			for(int i = 0; i < tokens.length; i++) {
 				// Start a tag if we are an entity and the one before us wasn't the same entity
 				if (ents[i] != "0" && ((i-1 > 0) && (ents[i] != ents[i-1]))) {
 					builder.append("<a href=\""+wikipedia+ents[i]+"\">");
 				}
 				builder.append(tokens[i]);
 
 				// If the current token is entity AND we have a next token, and that next token isn't the same as this one
 				if (ents[i] != "0" && ((i+1 < ents.length) && (ents[i] != ents[i+1]))) {
 					builder.append("</a>");
 				}
 				builder.append(" ");
 			}
 			builder.append("<br />");
 
 		}
 		output.setText(builder.toString());
 		System.out.println();
 	}
 
 	private void createAndShowGUI() {
 		// Create and set up the window.
 		JFrame frmNamedEntityLinker = new JFrame("HelloWorldSwing");
 		frmNamedEntityLinker.getContentPane().setMaximumSize(new Dimension(2147483647, 400));
 		frmNamedEntityLinker.setMaximumSize(new Dimension(2147483647, 400));
 		frmNamedEntityLinker.setMinimumSize(new Dimension(650, 400));
 		frmNamedEntityLinker.getContentPane().setName("Named Entity Linker");
 		frmNamedEntityLinker.setTitle("Named Entity Linker");
 		frmNamedEntityLinker.getContentPane().setPreferredSize(
 				new Dimension(500, 350));
 		frmNamedEntityLinker.getContentPane().setMinimumSize(
 				new Dimension(500, 350));
 		frmNamedEntityLinker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Display the window.
 		frmNamedEntityLinker.pack();
 		frmNamedEntityLinker.getContentPane().add(panel, BorderLayout.NORTH);
 		panel.setLayout(new BorderLayout(0, 0));
 
 		JPanel panel_1 = new JPanel();
 		panel.add(panel_1, BorderLayout.NORTH);
 		panel_1.setLayout(new BorderLayout(0, 0));
 
 		JSplitPane splitPane = new JSplitPane();
 		panel_1.add(splitPane, BorderLayout.NORTH);
 		splitPane.setPreferredSize(new Dimension(500, 300));
 		splitPane.setMinimumSize(new Dimension(300, 150));
 		splitPane.setResizeWeight(0.5);
 
 		input = new JTextArea();
 		input.setLineWrap(true);
 		input.setMinimumSize(new Dimension(100, 22));
 		input.setPreferredSize(new Dimension(250, 22));
 		input.setText("input");
 		splitPane.setLeftComponent(input);
 
 		output = new JTextPane();
 		output.setContentType("text/html");
 		output.setEditable(false);
 		output.setMinimumSize(new Dimension(100, 22));
 		output.setPreferredSize(new Dimension(250, 22));
 		output.setText("output");
 		output.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent event) {
 				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					try {
 						browseTo(event.getURL().toURI());
 					} catch (Exception ioe) {
 						// Some warning to user
 					}
 				}
 			}
 		});
 		splitPane.setRightComponent(output);
 
 		JButton btnNewButton = new JButton("Link!");
 		panel_1.add(btnNewButton, BorderLayout.SOUTH);
 
 		JPanel panel_2 = new JPanel();
 		panel.add(panel_2, BorderLayout.SOUTH);
 		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 
 		JLabel lblWeight = new JLabel("InLink:");
 		panel_2.add(lblWeight);
 
 		weight1 = new JTextField();
 		weight1.setText(Integer.toString(inLinkWeight));
 		panel_2.add(weight1);
 		weight1.setColumns(2);
 
 		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
 		panel_2.add(horizontalStrut_1);
 
 		JLabel lblWeightW = new JLabel("CrossWiki:");
 		panel_2.add(lblWeightW);
 
 		weight2 = new JTextField();
 		weight2.setText(Integer.toString(crossWikiWeight));
 		weight2.setColumns(2);
 		panel_2.add(weight2);
 
 		Component horizontalStrut = Box.createHorizontalStrut(20);
 		panel_2.add(horizontalStrut);
 
 		JLabel lblWeight_1 = new JLabel("Histogram:");
 		panel_2.add(lblWeight_1);
 
 		weight3 = new JTextField();
 		panel_2.add(weight3);
 		weight3.setText(Integer.toString(histogramWeight));
 		weight3.setColumns(2);
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Linking");
 				Link();
 			}
 		});
 		frmNamedEntityLinker.setVisible(true);
 	}
 
 	private void browseTo(URI url) {
 		if( !java.awt.Desktop.isDesktopSupported() ) {
             System.err.println( "Desktop is not supported (fatal)" );
             System.exit( 1 );
         }
         java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
         if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
             System.err.println( "Desktop doesn't support the browse action (fatal)" );
             System.exit( 1 );
         }
 
             try {
                 desktop.browse( url );
             }
             catch ( Exception e ) {
                 System.err.println( e.getMessage() );
             }
 	}
 
 }
