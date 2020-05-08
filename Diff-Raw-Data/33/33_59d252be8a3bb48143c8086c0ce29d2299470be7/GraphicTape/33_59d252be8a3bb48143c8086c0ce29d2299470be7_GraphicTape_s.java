 package tape;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.HashMap;
 
 /**
  * 
  * @author Nessa Baier, Nils Breyer
  *
  */
 public class GraphicTape extends Tape {
 
 	/**
 	 * Panel for the tape.
 	 */
 	JPanel tapePanel;
 	/**
 	 * Label for the tape's name
 	 */
 	JLabel textlabel;
 	/**
 	 * TextArea for displaying the tapes content.
 	 */
 	JTextArea jTextAreaTape;
 	/**
 	 * Stores the current tape content
 	 */
 	HashMap<Integer, Character> memory = new HashMap<Integer, Character>();
 
 	/**
 	 * Creates a new graphic tape
 	 */
 	public GraphicTape() {
 		super("Default graphic tape");
 	}
 
 	/**
 	 * Creates a new graphic tape
 	 * @param allowInput true/false if allowed
 	 */
 	public GraphicTape(boolean allowInput) {
 		super("Default graphic tape", allowInput);
 	}
 
 	/**
 	 * Creates a new graphic tape with a specific name
 	 * @param name The tape name
 	 */
 	public GraphicTape(String name) {
 		super(name);
 	}
 
 	/**
 	 * Creates a new graphic tape with a specific name.
 	 * @param name The tape name
 	 * @param allowInput true/false if allowed
 	 */
 	public GraphicTape(String name, boolean allowInput) {
 		super(name, allowInput);
 	}
 
 	/**
 	 * Returns the tape's type.
 	 */
 	public Type getType() {
 
 		return Type.GRAPHIC;
 	}
 
 	/** 
 	 * This method initializes the tape.
 	 * @throws TapeException If the tape has already been initialized.
 	 */
 	public void init() throws TapeException {
 		if (this.ready) throw new TapeException(this, "Tape has already been initialized.");
 		
 		System.out.println(this.name + ": Tape ready.");
 		//add graphic stuff
 		this.tapePanel = new JPanel(new BorderLayout());
 		this.jTextAreaTape = new JTextArea();
 		this.jTextAreaTape.setFont(new Font("Courier",Font.PLAIN, this.jTextAreaTape.getFont().getSize()));
 		this.jTextAreaTape.setEditable(false);
 		this.textlabel = new JLabel(this.name);
 		this.tapePanel.add(textlabel,BorderLayout.NORTH);
 		this.tapePanel.add(this.jTextAreaTape, BorderLayout.CENTER);
 
 		//set ready
 		this.ready = true;
 	}
 	
 	/**
 	 * This method shuts the tape down.
 	 * @throws TapeException If the tape has not been initialized
 	 */
 	@Override
 	public void shutdown() throws TapeException {
 		if (!this.ready) throw new TapeException(this, "Tape has not been initialized.");
 
 		//reset tape
 		this.ready = false;
 		this.iWishToInterruptThisThread = false;
 		this.position = 0;
 		this.memory.clear();
 	}
 
 	/** 
 	 * This method returns the char at the current tape position
 	 * @return Character at the current position
 	 * @throws TapeException If the tape has not been initialized
 	 * @see #write(char)
 	 */
 	@Override
 	public char read() throws TapeException {
 		if (!this.ready) throw new TapeException(this, "Tape has not been initialized.");
 
 		if (this.memory.get(this.position) != null) {
 			return this.memory.get(this.position);
 		}
 		else {
 			return '#';
 		}
 	}
 
 	@Override
 	/** 
 	 * This method writes a symbol at the current tape position
 	 * @param c Character to write (allowed characters are #, 0, 1, 2)
 	 * @throws TapeException If the tape has not been initialized
 	 * @see #read()
 	 */
 	public void write(char c) throws TapeException {
 		if (!this.ready) throw new TapeException(this, "Tape has not been initialized.");
 		this.memory.put(this.position, c);
 		this.printTape();
 		try{
 			Thread.sleep(500);
 		}
 		catch(InterruptedException e){}
 
 	}
 
 	@Override
 	/** 
 	 * This method moves the tape one field to the left
 	 * @throws TapeException If the tape has not been initialized
 	 * @see #moveRight()
 	 */
 	public void moveLeft() throws TapeException {
 		if (!this.ready) throw new TapeException(this, "Tape has not been initialized.");
 
 		position--;
 		this.printTape();
 		try{
 			Thread.sleep(500);
 		}
 		catch(InterruptedException e){}
 
 	}
 
 	@Override
 	/** 
 	 * This method moves the tape one field to the right
 	 * @throws TapeException If the tape has not been initialized
 	 * @see #moveLeft()
 	 */
 	public void moveRight() throws TapeException {
 		if (!this.ready) throw new TapeException(this, "Tape has not been initialized.");
 
 		position++;
 		this.printTape();
 		try{
 			Thread.sleep(500);
 		}
 		catch(InterruptedException e){}
 	}
 
 	@Override
 	/**
 	 * This method runs a test on the tape. It is not specified what this method actually does.
 	 * @throws TapeException If the tape has not been initialized
 	 */
 	public void test() throws TapeException {
 		if (!this.ready) throw new TapeException(this, "Tape has not been initialized.");
 
 		System.out.println("Test"); 
 
 	}
 
 	@Override
 	/**
 	 * Writes the input word on the tape.
 	 * @throws TapeException If something went wrong with the tapes.
 	 */
 	public void writeInputWord() throws TapeException {
 		if (this.position != 0) {
 			throw new TapeException(this, "Input word can only be written when at position 0");
 		}
 		//write input word to tape
 		for (int i = 0; i < this.inputWord.length(); i++) {
 			if(this.iWishToInterruptThisThread){
 				System.out.println("thread wird gestoppt");
 				break;
 			}
 			
 			this.write(this.inputWord.charAt(i));
 			printTape();
 			this.moveRight();
 		}
 		for (int i = 0; i < this.inputWord.length(); i++) {
 			if(this.iWishToInterruptThisThread){
 				System.out.println("thread wird gestoppt");
 				break;
 			}
 			printTape();
 			this.moveLeft();
 		}
 		printTape();
 	}
 
 	/**
 	 * This method writes the tapestring into the textarea
 	 */
 	public void printTape() {
 		jTextAreaTape.setText(this.getMemoryAsFormattedString(-20,40));
 	}
 	
 	/**
 	 * Returns the tapePanel.
 	 * @return tapePanel
 	 */
 	public JPanel getTapePanel() {
 		return tapePanel;
 	}
 
 	/**
 	 * Returns the textArea of the taoe.
 	 * @return textAreaTape
 	 */
 	public JTextArea getjTextAreaTape() {
 		return jTextAreaTape;
 	}
 
 	/**
 	 * Returns a formatted string representing the current memory state.
 	 * It includes specified part of the memory and the head position.
 	 * @param offset First memory position to be included
 	 * @param length Number of memory fields to be included
 	 * @return The formatted string with the memory content
 	 */
 	public String getMemoryAsFormattedString(int offset, int length) {
 		if (length < 1) return "";	
 		//print top horizontal line
 		String text = "-";
 		for (int i=offset; i<=offset+length; i++) {
 			text += "--";
 		}
 		text += "\n";
 
 		//print memory
 		text += "|";
 		for (int i=offset; i<=offset+length; i++) {
 			if (this.memory.get(i) != null) {
 				text += this.memory.get(i);
 			}
 			else {
 				text += "#";
 			}
 			text += "|";
 		}
 		text += "\n";
 
 		//print head position
 		if (this.position >= offset && this.position <= offset+length) {
 			for (int i=offset; i<this.position; i++) {
 				text += "  ";
 			}
 			text += " ^";
 		}
 		text += "\n";
 
 		//print bottom horizontal line
 		text += "-";
 		for (int i=offset; i<=offset+length; i++) {
 			text += "--";
 		}
 		text += "\n";
 
 		return text;
 
 	}
 
 
 }
