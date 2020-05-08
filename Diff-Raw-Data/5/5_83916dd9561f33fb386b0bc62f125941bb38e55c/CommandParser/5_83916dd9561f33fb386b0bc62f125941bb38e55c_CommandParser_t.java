 package grouppractical.client.commands;
 
 import grouppractical.utils.map.Position;
 
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Semaphore;
 
 public class CommandParser {
 	/** Queue to store parsed Commands in */
 	private final Queue<Command> q;
 	
 	/** Array of chars that have been received already */
 	private char[] chars;
 	/** CommandType of the command which is currently being parsed */
 	private CommandType cmdType;
 	/** Number of characters that have currently been parsed, for this command */
 	private int i;
 	
 	private final Semaphore semWriter;
 	
 	/**
 	 * Constructs a new CommandParser
 	 */
 	public CommandParser() {
 		/* Thread-safe queue, allowing access from multiple threads, without blocking
 		 * Also means a second semaphore is not required for reading */
 		q = new ConcurrentLinkedQueue<Command>();
 		semWriter = new Semaphore(1);
 	}
 	/**
 	 * Adds an entire string to the buffer
 	 * @param s String to add
 	 * @throws InterruptedException Thrown if the acquisition of the semaphore has been interrupted
 	 */
 	public void enqueue(String s) throws InterruptedException {
 		enqueue(s.toCharArray());
 	}
 	/**
 	 * Adds an entire char[] to the buffer
 	 * @param cs Char array to add
 	 * @throws InterruptedException Thrown if the acquisition of the semaphore has been interrupted
 	 */
 	public void enqueue(char[] cs) throws InterruptedException {
 		for (int i = 0; i < cs.length; i++)
 			enqueue(cs[i]);
 	}
 	
 	public void enqueue(byte b) throws InterruptedException {
 		int i = (b + 127) % 256;
 		enqueue((char)i);
 	}
 	/**
 	 * Adds an extra char to the buffer to be processed
 	 * @param c Character to add
 	 * @throws InterruptedException Thrown if the acquisition of the semaphore has been interrupted 
 	 */
 	public void enqueue(char c) throws InterruptedException {
 		semWriter.acquire();
 		if ("true".equals(System.getenv("debug"))) {
 			System.out.println(Integer.toHexString(c));
 		}
 		// Forces the char to be an 8-bit char
 		c = validateChar(c);
 		/* If there is not an array of chars which have already been read, 
 		 * then this char should represent the CommandType */
 		if (chars == null) {
 			// Get the command type represented by the char
 			cmdType = CommandType.getByChar(c);
 			// If the char does not represent a valid command type, ignore it
 			if (cmdType == null) { semWriter.release(); return; }
 			/* Construct a new array of chars to hold the next values, only containing
 			 * this character */
 			chars = new char[cmdType.getSize()];
 			chars[0] = c;
 			i = 1;
 		// Otherwise, the character is part of another command
 		} else
 			chars[i++] = c;
 		
 		// If the array is full, i.e., all chars for this command have been read, generate the command
 		if (i >= chars.length) {
 			if (chars.length == cmdType.getSize() && chars[0] == cmdType.toChar()) {
 				switch (cmdType) {
 				case RDISTANCE:
 					q.add(parseRDistance(chars));
 					break;
 				case RROTATE:
 					q.add(parseRRotate(chars));
 					break;
 				case RSTOP:
 					q.add(new RStopCommand());
 					break;
 				case RSTATUS:
 					q.add(parseRStatus(chars));
 					break;
 				case RLOCK:
 					q.add(new RLockCommand());
 					break;
 				case RUNLOCK:
 					q.add(new RUnlockCommand());
 					break;
 				case MPOSITION:
 					q.add(parseMPosition(chars));
 					break;
 				case MINITIALISE:
 					q.add(new MInitialiseCommand());
 					break;
 				case CONNECT:
 					q.add(parseConnect(chars));
 					break;
 				}
 				if ("true".equals(System.getenv("debug"))) {
 					for (char x : chars) System.out.print(Integer.toHexString(x) + " ");
 					System.out.println("\t\t"+ q.peek().toString());
 				}
 			}
 			chars = null;
 		}
 		semWriter.release();
 	}
 	/**
 	 * Clears any unparsed characters from the input buffer
 	 * @throws InterruptedException Thrown if the acquisition of the semaphore is interrupted
 	 */
 	public void clearInput() throws InterruptedException {
 		semWriter.acquire();
 		chars = null;
 		semWriter.release();
 	}
 	/**
 	 * Gets the number of unparsed characters that ar stored in the object
 	 * @return Size of input buffer
 	 * @throws InterruptedException Thrown if the acquisition of the semaphore is interrupted
 	 */
 	public int inputSize() throws InterruptedException {
 		semWriter.acquire();
 		//If the chars array has not been  set then no chars have been entered.
 		int s = 0;
 		//Otherwise the size of the input buffer is the current position in the chars array, i
 		if (chars != null) s = this.i;
 		semWriter.release();
 		return s;
 	}
 	/**
 	 * Gets whether the CommandParser has a command waiting to be read
 	 * @return True if a command is ready, otherwise false 
 	 */
 	public boolean isOutputEmpty() {
 		//Note q.size() is O(n), q.isEmpty() is O(1)
 		return q.isEmpty();
 	}
 	/**
 	 * Gets the number of commands which have been parsed, but are waiting to be dequeued
 	 * @return Number of commands in output queue
 	 */
 	public int outputSize() {
 		//Note q.size() is O(n)
 		return q.size();
 	}
 	/**
 	 * 'Pops' the currently generated command off of the parser
 	 * @return Null if 
 	 */
 	public Command dequeue() {
 		Command r = q.poll();
 		return r;
 	}
 	/**
 	 * Checks that a char is valid
 	 * @param c Char to validate
 	 * @return True if the char represents an 8-bit unsigned integer, otherwise false
 	 */
 	private char validateChar(char c) {
 		if (c < 0) return 0;
 		else if (c > 255) return 255;
 		else return c;
 	}
 	/**
 	 * Constructs a RRotateCommand from an array of chars
 	 * @param chars An array of chars representing a rotate command
 	 * @return A rotate command, or null if the char array is invalid
 	 */
 	private RRotateCommand parseRRotate(char[] chars) {
 		//Generates the serialized angle from the MSB and the LSB
 		int x = (chars[1] << 7) + (chars[2] >> 1);
 		if ((chars[2] & 0x01) == 0) x = -x;
 		return new RRotateCommand((short) x);
 	}
 	/**
 	 * Constructs a RDistanceCommand from an array of chars
 	 * @param chars An array of chars representing a distance command
 	 * @return A distance command, or null if the char array is invalid
 	 */
 	private RDistanceCommand parseRDistance(char[] chars) {
 		//Generates the magnitude of the distance from the MSB and the LSB
 		int x = (chars[1] << 7) + ((chars[2] & 0xFE) >> 1);
 		if ((chars[2] & 0x01) == 0) x = -x;
 		return new RDistanceCommand((short) x);
 	}
 	/**
 	 * Constructs a MPostionCommand from an array of chars
 	 * @param chars An array of chars representing a Map Position command
 	 * @return A map position command, or null if the char array is invalid
 	 */
 	private MPositionCommand parseMPosition(char[] chars) {
 		//X-coordinate
 		int x = ((chars[1] & 0xFE) << 7) ^ chars[2];
 		if ((chars[1] & 0x01) > 0) x *= -1;
 		//Y-coordinate
 		int y = ((chars[3] & 0xFE) << 7) ^ chars[4];
 		if ((chars[3] & 0x01) > 0) y *= -1;
 		
 		int certainty = chars[5] >> 1;
 		boolean occupied = (chars[5] & 0x01) > 0;
 		
 		return new MPositionCommand(new Position(x,y,occupied,(short) certainty));
 	}
 	
 	/**
 	 * Constructs a MPostionCommand from an array of chars
 	 * @param chars An array of chars representing a Map Position command
 	 * @return A map position command, or null if the char array is invalid
 	 */
 	private RStatusCommand parseRStatus(char[] chars) {
 		//X-coordinate
 		int x = ((chars[1] & 0xFE) << 7) ^ chars[2];
 		if ((chars[1] & 0x01) > 0) x *= -1;
 		//Y-coordinate
 		int y = ((chars[3] & 0xFE) << 7) ^ chars[4];
 		if ((chars[3] & 0x01) > 0) y *= -1;
 		
 		float voltage = (float)chars[5] / 20;
 		
 		//Generates the serialized angle from the MSB and the LSB
 		int angle = ((chars[6] << 8) ^ chars[7]) + RStatusCommand.MIN_INT;
 		
 		return new RStatusCommand(new Position(x,y,false, Position.MAX_CERTAINTY), voltage, (short) angle);
 	}
 	
 	private ConnectCommand parseConnect(char[] chars) {
 		ClientType clientType = ClientType.getByChar(chars[1]);
 		
 		return new ConnectCommand(clientType);
 	}
 }
