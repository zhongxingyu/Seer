 package machine.brainfuck;
 
 import gui.MachineEditor;
 import gui.brainfuck.BrainfuckEditor;
 
 import java.io.*;
 import tape.*;
 import machine.*;
 
 /**
  * Represents a brainfuck machine and provides methods to handle it.
  * @author Sven Schuster
  * 
  */
 public class BrainfuckMachine extends Machine {
 	public static final String FILE_EXTENSION = ".bf";
 
 	private BrainfuckEditor brainfuckEditor;
 	private String code = "Type your brainfuck code here";
 
 	/**
 	 * Creates a new brainfuck machine with input-, output- and actiontape.
 	 */
 	public BrainfuckMachine() {
 		super();
 		tapes.add(new GraphicTape("Input", true));
 		tapes.add(new GraphicTape("Output", false));
 		tapes.add(new GraphicTape("Action", false));		
 	}
 
 	/**
 	 * Creates a new brainfuck machine with given name, input-, output- and actiontape.
 	 * @param name Name of machine
 	 */
 	public BrainfuckMachine(String name) {
 		super(name);
 		tapes.add(new GraphicTape("Input", true));
 		tapes.add(new GraphicTape("Output", false));
 		tapes.add(new GraphicTape("Action", false));
 	}
 
 	/**
 	 * Saves the brainfuck code to given filename.
 	 * @param filename Filename
 	 */
 	@Override
 	public void save(String filename) throws IOException {
 		if (brainfuckEditor != null) {
 			this.code = brainfuckEditor.getCode();
 		}
 		FileWriter fstream = new FileWriter(filename);
 		BufferedWriter out = new BufferedWriter(fstream);
 		out.write(code);
 		out.close();
 	}
 
 	/**
 	 * Loads brainfuck code out of given filename.
	 * @param filename Filename to load code from.
 	 * @throws FileNotFoundException If the file does not exist.
 	 * @throws IOException If file could not be read.
 	 */
 	@Override
 	public void load(String fileName) throws FileNotFoundException, IOException {
 		File file = new File(fileName);
 		byte[] buffer = new byte[(int) file.length()];
 		BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
 		f.read(buffer);
 		code = new String(buffer);
 		if (brainfuckEditor != null) {
 			brainfuckEditor.setCode(code);
 		}
 	}
 
 	/**
 	 * Creates a new brainfuck simulation.
 	 * @return BrainfuckSimulation
 	 * @throws TapeException If the tape failed to initialize.
 	 */
 	@Override
 	public Simulation createSimulation() throws TapeException {
 		BrainfuckSimulation brainfuckSimulation = new BrainfuckSimulation(this);
 		return brainfuckSimulation;
 	}
 
 	/**
 	 * Creates a new brainfuck editor.
 	 * @return BrainfuckEditor
 	 */
 	@Override
 	protected MachineEditor createEditor() {
 		brainfuckEditor = new BrainfuckEditor();
 		brainfuckEditor.setCode(code);
 		return brainfuckEditor;
 	}
 
 	@Override
 	public MachineType getType() {
 		return Machine.MachineType.BrainfuckMachine;
 	}
 	
 	@Override
 	public String getFileExtension() {
 		return BrainfuckMachine.FILE_EXTENSION;
 	}
 }
