 package bfrc.java.jit;
 
 import bfrc.backend.Backend;
 
 /**
  * The context contains all state information during execution and takes
  * requests to compile new blocks.
  * 
  * @author oreissig
  */
public abstract class JITContext {
 
 	public static final byte COMPILE_THRESHOLD = 3;
 	public final byte[] mem;
 	public int ptr = 0;
 
 	protected JITContext() {
 		this(Backend.MEM_SIZE);
 	}
 
 	protected JITContext(int memSize) {
 		mem = new byte[memSize];
 	}
 
 	public abstract JITBlock compile(int blockID);
 
 	public abstract JITBlock interpret(int blockID);
 }
