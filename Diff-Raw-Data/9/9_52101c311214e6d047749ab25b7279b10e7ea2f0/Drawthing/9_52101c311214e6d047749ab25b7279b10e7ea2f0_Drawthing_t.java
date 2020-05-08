 import java.awt.event.*;
 import java.io.*;
 import javax.swing.event.*;
 import javax.swing.*;
 import java.awt.*;
import javax.swing.filechooser.*;
 
 /**
  * A simple drawing app using FreehandDrawPanel.
  * Supports saving to SVG.
  * @author Lorenz Diener
  */
 public class Drawthing extends JFrame {
 	// The freehand draw panel we let people draw on.
 	FreehandDrawPanel canvas;
 
 	// Creates the frame and displays it.
 	public Drawthing() {
 		// Drawing panel, wrapped in a scrolling container
 		this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.Y_AXIS ) );
 		canvas =  new FreehandDrawPanel();
 		canvas.setPreferredSize( new Dimension( 400, 400 ) );
 		JScrollPane scrollCanvas = new JScrollPane( canvas );
 		this.add( scrollCanvas );
 
 		// Spacer
 		this.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
 
 		// Control interface container
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout( new BoxLayout( controlPanel, BoxLayout.X_AXIS ) );
		controlPanel.setMaximumSize( new Dimension( Short.MAX_VALUE, 40 ) );
 		this.add( controlPanel );
 
 		// Spacer
 		controlPanel.add( Box.createGlue() );
 
 		// Color selection
 		JButton colorButton = new JButton( "Color" );
 		colorButton.addActionListener( new ColorChangeListener( this.canvas ) );
 		controlPanel.add( colorButton );
 	
 		// Spacer
 		controlPanel.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
 
 		// Brush size selection
 		controlPanel.add( new JLabel( "Size: " ) );
 		Integer[] brushSizes = new Integer[ 25 ];
 		for( int i = 0; i < 25; i++ ) {
 			brushSizes[ i ] = new Integer( i + 1 );
 		}
 		JComboBox brushSizeBox = new JComboBox( brushSizes );
 		brushSizeBox.addActionListener( new BrushSizeListener( this.canvas ) );
 		brushSizeBox.setMaximumSize( new Dimension( 40, 40 ) );
 		controlPanel.add( brushSizeBox );
 
 		// Spacer
 		controlPanel.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
 
 		// Undo
 		JButton undoButton = new JButton( "Undo" );
 		undoButton.addActionListener( new UndoListener( this.canvas ) );
 		controlPanel.add( undoButton );
 
 		// Reset canvas
 		JButton resetButton = new JButton( "Reset" );
 		resetButton.addActionListener( new ResetListener( this.canvas ) );
 		controlPanel.add( resetButton );
 
 		// Spacer
 		controlPanel.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
 
 		// Save to SVG
 		JButton saveButton = new JButton( "Save" );
 		saveButton.addActionListener( new SaveListener( this.canvas ) );
 		controlPanel.add( saveButton );
 
 		// Spacer
 		controlPanel.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
 
 		// Spacer
 		this.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
 
 		// Display frame
 		this.pack();
 		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
 	}
 
 	/**
 	 * Main function. Opens the main window and then lets swing do the rest.
 	 * @param args Ignored.
 	 */
 	public static void main( String args[] ) {
 		Drawthing thingWhichWeDrawOn = new Drawthing();
 		thingWhichWeDrawOn.setVisible( true );
 	}
 
 	/**
 	 * Inner class: Listens to clicks on the brush size combo box.
 	 */
 	private class BrushSizeListener implements ActionListener {
 		FreehandDrawPanel canvas;
 
 		/**
 		 * Constructor: Remembers canvas to delegate things to.
 		 * @param canvas The FreehandDrawPanel to do things with.
 		 */
 		BrushSizeListener( FreehandDrawPanel canvas ) {
 			this.canvas = canvas;
 		}
 
 		/**
 		 * Action handler: Tells the canvas to change its brush size.
 		 * @param e The action event recieved.
 		 */
 		public void actionPerformed( ActionEvent e ) {
 			this.canvas.setBrushSize( ((JComboBox)e.getSource()).getSelectedIndex() + 1 );
 		}
 	}
 
 	/**
 	 * Inner class: Listens to clicks on the reset button.
 	 */
 	private class ResetListener implements ActionListener {
 		FreehandDrawPanel canvas;
 
 		/**
 		 * Constructor: Remembers canvas to delegate things to.
 		 * @param canvas The FreehandDrawPanel to do things with.
 		 */
 		ResetListener( FreehandDrawPanel canvas ) {
 			this.canvas = canvas;
 		}
 
 		/**
 		 * Action handler: Tells the canvas to clear itself.
 		 * @param e The action event recieved.
 		 */
 		public void actionPerformed( ActionEvent e ) {
 			this.canvas.clear();
 		}
 	}
 
 	/**
 	 * Inner class: Listens to clicks on the undo button.
 	 */
 	private class UndoListener implements ActionListener {
 		FreehandDrawPanel canvas;
 
 		/**
 		 * Constructor: Remembers canvas to delegate things to.
 		 * @param canvas The FreehandDrawPanel to do things with.
 		 */
 		UndoListener( FreehandDrawPanel canvas ) {
 			this.canvas = canvas;
 		}
 
 		/**
 		 * Action handler: Tells the undo the last stroke.
 		 * @param e The action event recieved.
 		 */
 		public void actionPerformed( ActionEvent e ) {
 			this.canvas.undoLastStroke();
 		}
 	}
 
 	/**
 	 * Inner class: Listens to clicks on the save button.
 	 */
 	private class SaveListener implements ActionListener {
 		FreehandDrawPanel canvas;
 
 		/**
 		 * Constructor: Remembers canvas to delegate things to.
 		 * @param canvas The FreehandDrawPanel to do things with.
 		 */
 		SaveListener( FreehandDrawPanel canvas ) {
 			this.canvas = canvas;
 		}
 
 		/**
 		 * Action handler: Asks the canvas for SVG, then saves that SVG to a file.
 		 * @param e The action event recieved.
 		 */
 		public void actionPerformed( ActionEvent e ) {
 			JFileChooser saveFileDialog = new JFileChooser();
			FileNameExtensionFilter svgFiles = new FileNameExtensionFilter( "Scalable vector graphics (.svg)", "svg" );
			saveFileDialog.setFileFilter( svgFiles );
			if( saveFileDialog.showSaveDialog( this.canvas ) == JFileChooser.APPROVE_OPTION ) {
 				File saveFile = saveFileDialog.getSelectedFile();
 				try {
 					PrintWriter out = new PrintWriter(
 						new BufferedWriter(
 							new FileWriter(
 								saveFile
 							)
 						)
 					);
 					out.print( this.canvas.getSVG() );
 					out.flush();
 					out.close();
 				}
 				catch( IOException err ) {
 					JOptionPane.showMessageDialog(
 						null,
 						"Could not save!",
 						"IO Error",
 						JOptionPane.ERROR_MESSAGE
 					);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Inner class: Listens to clicks on the color selection button.
 	 */
 	private class ColorChangeListener implements ActionListener {
 		FreehandDrawPanel canvas;
 
 		/**
 		 * Constructor: Remembers canvas to delegate things to.
 		 * @param canvas The FreehandDrawPanel to do things with.
 		 */
 		ColorChangeListener( FreehandDrawPanel canvas ) {
 			this.canvas = canvas;
 		}
 
 		/**
 		 * Action handler: Lets the user select a color, then tells that color to the canvas.
 		 * @param e The action event recieved.
 		 */
 		public void actionPerformed( ActionEvent e ) {
 			Color newColor = JColorChooser.showDialog(
 				this.canvas,
 				"Select new brush color.",
 				this.canvas.getColor()
 			);
 			this.canvas.setColor( newColor );
 		}
 	}
 }
