 package common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.security.MessageDigest;
 import java.util.HashMap;
 import java.util.Map;
 
 import common.messages.MessageFactory.ErrorCode;
 import common.messages.MessageFactory.Result;
 
 public abstract class ActiveRDMA {
 	
 	final public static int REQUEST_TIMEOUT = 20000;
 	final public static int SERVER_PORT = 15712;
 	
 	/*
 	 * RDMA operations
 	 */
 	
 	/** Reads from the server's memory.
 	 * @param address - offset of the memory location
 	 * @return value of memory location *at the time of the read*
 	 */
 	public abstract Result _r(int address, int size);
 	
 	/** Writes the value into the server's memory.
 	 * @param address - offset of the memory location
 	 * @param value - the new value to be store
 	 * @return old value
 	 */
 	public abstract Result _w(int address, int[] values);
 	
 	/** Compare-And-Swap, if test is true it will atomically write value into 
 	 * the memory at location address.
 	 * @param address - offset of the memory location
 	 * @param test - condition that must be true for the assignment to occur
 	 * @param value - the new value to be store
 	 * @return boolean as int (0 - false, else - true) with the test result
 	 */
 	public abstract Result _cas(int address, int test, int value);
 
 	/*
 	 * Active operations
 	 */
 	
 	// mobile code interface expected to be:
 	// static public int execute(ActiveRDMA ardma, int arg); 
 	final public static String METHOD = "execute";
 	final public static Class<?>[] SIGNATURE = new Class[]{ActiveRDMA.class,int[].class};
 
 	/** Executes the previously loaded code in the server.
 	 * @param name - class to be executed, FIXME: this will change to something else, md5 maybe?
 	 * @param arg - argument supplied to the executing code
 	 * @return result of the executed method.
 	 */
 	public abstract Result _run(byte[] md5, int[] arg);
 	
 	/** Loads the code of a class
 	 * @param code - the bytecode of a class, not its serialization!
 	 * @return boolean as int (0 - false, else - true) with load success result
 	 */
 	public abstract Result _load(byte[] code);
 
     /** Reads a stream of bytes
       * @param address - memory address
       * @param count - number of bytes to read
       * @return data in Result.result_b
       */
     public abstract Result _readbytes(int address, int count);
 
     /** Writes a stream of bytes
       * @param address - memory address
       * @param values - array of bytes (zero or more) to write there
       * @return error code
       */
     public abstract Result _writebytes(int address, byte[] values);
 	
 	/*
 	 * exception based wrappers
 	 */
 	
 	static protected int[] unwrap(Result r){
 		switch(r.error){
 		case OUT_OF_BOUNDS:
 		case TIME_OUT:
 		case UNKNOWN_CODE:
 		case DUPLCIATED_CODE:
 		case ERROR:
 			throw new RuntimeException("Error: "+r.error);
 		case OK:
 		default:
 		}
 		return r.result;
 	}
 
     static protected byte[] unwrap_b(Result r) {
         switch (r.error) {
 		case OUT_OF_BOUNDS:
 		case TIME_OUT:
 		case UNKNOWN_CODE:
 		case DUPLCIATED_CODE:
 		case ERROR:
 			throw new RuntimeException("Error: "+r.error);
         case OK:
         default:
         }
         return r.result_b;
     }
 	
 	static protected int unwrap(int[] array){
 		return array == null ? 0 : array[0];
 	}
 	
 	//batch
 	public int[] r(int address, int lenght){
 		return unwrap( _r(address,lenght) );
 	}
 	
 	public int r(int address){
 		return unwrap( unwrap( _r(address,1) ) );
 	}
 
 	//batch
 	public int[] w(int address, int[] values){
 		return unwrap( _w(address, values) );
 	}
 	
 	public int w(int address, int value){
 		return unwrap( unwrap( _w(address,new int[]{value}) ) );
 	}
 	
 	public int cas(int address, int test, int value){
 		return unwrap( unwrap( _cas(address,test,value) ) );		
 	}
 	
 	public int run(byte[] md5, int[] arg){
 		return unwrap( unwrap( _run(md5,arg) ) );	
 	}
 	
 	public int[] runArray(byte[] md5, int[] arg){
 		return unwrap( _run(md5,arg) );	
 	}
 	
 	public int load(byte[] code){
 		return unwrap( unwrap( _load(code) ) );
 	}
 
     public byte[] readbytes(int address, int count) {
         return unwrap_b( _readbytes(address, count) );
     }
 
     public void writebytes(int address, byte[] values) {
         unwrap( _writebytes(address, values) ); 
     }
 	
 	/* ==============================================================
 	 * all these methods should just use/extend the above "interface"
 	 * ==============================================================
 	 */
 
 	
 	/*
 	 * Allocation function
 	 */
 	
 
 	//this integer is the RESERVED position for counting allocated blocks
 	//should not be used for anything else... or we will have bugs.
 	//FIXME: memory is never reclaimed! dealloc()?
 	
 	public static class Alloc{
 		static final int MEM_COUNTER = 0;
 		
 		public static void init(ActiveRDMA a){
 			a.w(MEM_COUNTER, 4);
 		}
 		
 		public static int execute(ActiveRDMA a, int[] args) {
 			int size = args[0];
 			int c = a.r(Alloc.MEM_COUNTER);
 			//retry until we were able to increase the claimed memory by 'size'
 			while( a.cas(Alloc.MEM_COUNTER,c,c+size) == 0 ){
 				//failed, get the new value
 				c = a.r(Alloc.MEM_COUNTER);
 			}
 			//System.out.println("Memcounter "+Alloc.MEM_COUNTER+" allocated "+c);
 			return c;
 		}
 	}
 	
 	public int alloc(int size){
 		return Alloc.execute(this, new int[]{size});
 	}
 	
 	/*
 	 * Simplification of load and run commands
 	 */
 	
 	protected Map<String,ByteArray> class_to_md5 = new HashMap<String, ByteArray>();
 	protected Map<ByteArray,Class<?>> md5_to_class = new HashMap<ByteArray, Class<?>>();
	
 	public static byte[] getCode(Class<?> cl) throws Exception{
 		String name = cl.getName().replaceAll("\\.", "/")+".class";
 		File classfile = new File( ActiveRDMA.class.getClassLoader().getResource(name).toURI() );
 
 		int len = (int)(classfile.length());
 		byte[] bytes = new byte[len];
 		FileInputStream i = new FileInputStream(classfile);
 		i.read(bytes, 0, len);
 		i.close();
 
 		return bytes;
 	}
 
     protected boolean haveCode(byte[] code) {
         MessageDigest digest = ByteArray.getDigest();
         ByteArray hash = new ByteArray ( code, digest );
         return md5_to_class.containsKey(hash);
     }
 
 	protected boolean tableClassAndMd5(Class<?> cl, byte[] code){
 		if( !class_to_md5.containsKey(cl.getName()) ){
 			MessageDigest digest = ByteArray.getDigest();
 			ByteArray hash = new ByteArray( code , digest );
 			class_to_md5.put(cl.getName(), hash);
 			md5_to_class.put(hash, cl);
 			return true;
 		}
 		//already contains
 		return false;
 	}
 	
 	public int run(Class<?> c, int[] arg) {
 		return run(class_to_md5.get( c.getName() ).array(),arg);
 	}
 	
 	public int[] runArray(Class<?> c, int[] arg) {
 		return runArray(class_to_md5.get( c.getName() ).array(),arg);
 	}
 
 	public int load(Class<?> c) {
 		return unwrap( unwrap( _load(c) ) );
 	}
 	
 	public Result _load(Class<?> c) {
 		try {
 			byte[] code = getCode(c);
 			tableClassAndMd5(c,code);
 			return _load( code );
 		} catch (Exception e) {
 			return new Result(ErrorCode.ERROR);
 		}
 	}
 	
 	/*
 	 * String wrapping/unwrapping
 	 */
 	
     public static String getString(int[] args, int off)
     {
         return new String(unpack(args, off));
     }
 
     public static String[] getStrings(int[] args, int off)
     {
         int off1 = off;
         int len1 = args[off1];
         int off2 = off1 + 1 + len1;
         int len2 = args[off2];
 
         String[] ret = new String[] { getString(args, off1), getString(args, off2) };
         return ret;
     }
     
 	
     public static int[] constructArgs(int prefix, String s)
     {
         byte[] sBytes = s.getBytes();
         return pack(prefix, sBytes, 0, sBytes.length);
     }
 
     public static int[] appendArray(int[] x, int[] y)
     {
         int[] ret = new int[x.length + y.length];
         System.arraycopy(x, 0, ret, 0, x.length);
         System.arraycopy(y, 0, ret, x.length, y.length);
         return ret;
     }
 
     public static int[] constructArgs(int prefix, String s1, String s2)
     {
         return appendArray(constructArgs(prefix, s1), constructArgs(0, s2));
     }
 
     public static int[] pack(int prefix, byte[] data, int offset, int len)
     {
         int intLen = (len+3) / 4;
         int[] ret = new int[prefix + intLen + 1];
         ret[prefix] = len;
 
         int dstPtr = prefix + 1;
         int srcPtr = offset;
         int remaining = len;
         int off = 0;
 
         while (remaining-- > 0)
         {
             ret[dstPtr] <<= 8;
             ret[dstPtr] |= ((int)data[srcPtr++]) & 0xff;
 
             if (++off == 4)
             {
                 off = 0;
                 dstPtr++;
             }
         }
         if (off > 0)
             while (off++ < 4)
                 ret[dstPtr] <<= 8;
 
         return ret;
     }
 
     public static int[] pack(int prefix, byte[] data)
     {
         return pack(prefix, data, 0, data.length);
     }
 
     public static void unpack(int[] data, int offset, byte[] ret, int dest_off)
     {
         int srcPtr = offset;
 
         int remaining = data[srcPtr];
         srcPtr++;
         int dstPtr = dest_off;
         int mod = 0;
 
         while (remaining-- > 0)
         {
             ret[dstPtr++] = (byte)((data[srcPtr] & 0xff000000) >> 24);
             data[srcPtr] <<= 8;
             if (++mod == 4)
             {
                 srcPtr++;
                 mod = 0;
             }
         }
     }
 
     public static byte[] unpack(int[] data, int offset)
     {
         byte[] ret = new byte[data[offset]];
         unpack(data, offset, ret, 0);
         return ret;
     }
 
 }
