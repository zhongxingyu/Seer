 package tape;
 
 import gui.ErrorDialog;
 
 import java.lang.String;
 import java.util.Observable;
 
 /**
  * This abstract class is the common base class for LEGO and Console/PC tapes.
  * It contains only few concrete methods.
  *
  * @author Nils Breyer, Nessa Baier
  */
 
 public abstract class Tape extends Observable implements Runnable{
 	/**
 	 * True if the tape has been initialized
 	 */
 	protected boolean ready = false;
 	/**
 	 * The current head position
 	 */
 	protected int position = 0;
 	/**
 	 * The name of the tape
 	 */
 	protected String name = "";
 	/**
 	 * The initial input word for the tape
 	 */
 	protected String inputWord = "";
 	/**
 	 * Allow input on the tape (default true)
 	 */
 	protected boolean allowInput = true;
 	
 	protected boolean iWishToInterruptThisThread;
 	
 	protected Thread writeInputWordThread;
 
 	/**
 	 * Enum representing the tape types
 	 */
 	public enum Type {
 		/**
 		 * LEGO tape
 		 */
 		LEGO, 
 		/**
 		 * Virtual tape on the console
 		 */
 		CONSOLE, 
 		/**
 		 * Virtual tape in the tapes window
 		 */
 		GRAPHIC
 	}
 	
 	public enum Event{
		INPUTFINISHED,
		INPUTABORTED
 	}
 
 	/**
 	 * Creates a new tape
 	 */
 	public Tape() {
 		this.name = "Default tape";
 		this.writeInputWordThread = new Thread(this);
 		this.iWishToInterruptThisThread = false;
 		
 	}
 
 	/**
 	 * Creates a new tape
 	 * @param allowInput true/false if allowed
 	 */
 	public Tape(boolean allowInput) {
 		this.name = "Default tape";
 		this.allowInput = allowInput;
 		this.iWishToInterruptThisThread = false;
 		this.writeInputWordThread = new Thread(this);
 	}
 
 	/**
 	 * Creates a new tape with a specific name
 	 * @param name The tape name
 	 */
 	public Tape(String name) {
 		this.name = name;
 		this.iWishToInterruptThisThread = false;
 		this.writeInputWordThread = new Thread(this);
 	}
 
 	/**
 	 * Creates a new tape with a specific name
 	 * @param name The tape name
 	 * @param allowInput true/false if allowed
 	 */
 	public Tape(String name, boolean allowInput) {
 		this.name = name;
 		this.allowInput = allowInput;
 		this.iWishToInterruptThisThread = false;
 		this.writeInputWordThread = new Thread(this);
 	}
 
 	/**
 	 * @return the iWishToInterruptThisThread
 	 */
 	public boolean isiWishToInterruptThisThread() {
 		return iWishToInterruptThisThread;
 	}
 
 	/**
 	 * @param iWishToInterruptThisThread the iWishToInterruptThisThread to set
 	 */
 	public void setiWishToInterruptThisThread(boolean iWishToInterruptThisThread) {
 		this.iWishToInterruptThisThread = iWishToInterruptThisThread;
 	}
 
 	/**
 	 * Initializes the tape
 	 * This method has to be called before any moving or read/write command can be executed.
 	 * @throws TapeException Thrown if the initialization process fails
 	 * @see #shutdown()
 	 */
 	public abstract void init() throws TapeException;
 
 	/**
 	 * Shutdown the tape
 	 * @throws TapeException Thrown if the shutdown process fails
 	 * @see #init()
 	 */
 	public abstract void shutdown() throws TapeException;
 
 	/**
 	 * Reads the symbol at the current tape position
 	 * 
 	 * @return Symbol at the current tape position (#,0,1,2)
 	 * @throws TapeException Thrown if the reading fails
 	 */
 	public abstract char read() throws TapeException;
 
 	/**
 	 * Writes a symbol to the current tape position
 	 * @param c Symbol to write (#,0,1,2)
 	 * @throws TapeException Thrown if the writing fails
 	 */
 	public abstract void write(char c) throws TapeException;
 
 	/**
 	 * Move the tape one field to the left
 	 * @throws TapeException Thrown if the moving fails
 	 */
 	public abstract void moveLeft() throws TapeException;
 
 	/**
 	 * Move the tape one field to the right
 	 * @throws TapeException Thrown if the moving fails
 	 */
 	public abstract void moveRight() throws TapeException;
 
 	/**
 	 * This method runs a test on the tape. It is not specified what this method actually does.
 	 * @throws TapeException Thrown if the test fails
 	 */
 	public abstract void test() throws TapeException; //TODO: remove
 
 	/**
 	 * @throws TapeException Thrown if writing the input word fails
 	 */
 	public abstract void writeInputWord() throws TapeException;
 	
 	public void run(){
 		try{
 			this.writeInputWord();
 			super.setChanged();
			super.notifyObservers(Event.INPUTFINISHED);
 			System.out.println("Writing input word finished; fire event");
 			
 		}
 		catch(TapeException e){
 			ErrorDialog.showError("Writing the input word failed.", e);
 		}
 		
 	}
 	
 	public void startWritingInputWord(){
 		this.writeInputWordThread = new Thread(this);
 		this.writeInputWordThread.start();
 	}
 
 	public Thread getWriteInputWordThread() {
 		return writeInputWordThread;
 	}
 //
 //	public void setWriteInputWordThread(Thread writeInputWordThread) {
 //		this.writeInputWordThread = writeInputWordThread;
 //	}
 
 	/**
 	 * This method returns the tape type
 	 * @return Either "LEGO" or "console" or "GUI" depending on the type of the tape
 	 */
 	public abstract Type getType();
 
 	/**
 	 * Checks if the tape has been initialized
 	 * @return Returns true, if the tape has been initialized and false if not.
 	 */
 	public boolean isReady() {
 		return this.ready;
 	}
 
 	/**
 	 * Get the current position of the tape
 	 * @return The head position on the tape
 	 */
 	public int getPosition() {
 		return this.position;
 	}
 
 	/**
 	 * Get the name of the tape
 	 * @return Returns the tape name string
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Set the name of the tape
 	 * @param name New tape name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Get the input word
 	 * @return Input word
 	 */
 	public String getInputWord() {
 		return inputWord;
 	}
 
 	/**
 	 * Set the input word
 	 * @param inputWord New input word
 	 * @throws TapeException Exception if error occurs while setting input word
 	 */
 	public void setInputWord(String inputWord) throws TapeException {
 		if (this.ready) throw new TapeException(this, "Tape has already been initialized. Input word can't be changed at this time anymore.");
 		this.inputWord = inputWord;
 	}
 
 	/**
 	 * Returns if input allowed
 	 */
 	public boolean isInputAllowed() {
 		return this.allowInput;
 	}
 
 }
