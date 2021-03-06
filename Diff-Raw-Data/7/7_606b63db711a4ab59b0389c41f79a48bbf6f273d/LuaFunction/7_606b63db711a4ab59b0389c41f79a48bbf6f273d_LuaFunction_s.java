 // $Header$
 
 /**
  * Models a Lua function.
  * Note that whilst the class is public, its constructors are not.
  * Functions are created by loading Lua chunks (in source or binary
  * form) or executing Lua code which defines functions (and, for
  * example, places them in the global table).  {@link
  * Lua#load(InputStream, String) Lua.load} is used
  * to load a Lua chunk (it returns a <code>LuaFunction</code>),
 * and {@link Lua#call(Object, int) Lua.call} is used to call a function.
  * A generic Lua
  * value, one retrieved from a table for example, can be converted to a
  * <code>LuaFunction</code> using {@link Lua#toFunction(Object)
  * Lua.toFunction} or by a
 * narrowing reference conversion, which may of course throw an
 * exception.
  */
 public final class LuaFunction {
   private UpVal[] upval;
   private LuaTable env;
   private Proto p;
 
   /**
    * Constructs an instance from a triple of {Proto, upvalues,
    * environment}.  Deliberately not public, See {@link
    * Lua#load(InputStream, String) Lua.load} for
    * public construction.  All arguments are referenced from the
    * instance.  The <code>upval</code> array must have exactly the same
    * number of elements as the number of upvalues in <code>proto</code>
    * (the value of the <code>nups</code> parameter in the
    * <code>Proto</code> constructor).
    *
    * @param proto  A Proto object.
    * @param upval  Array of upvalues.
    * @param env    The function's environment.
    * @throws NullPointerException if any arguments are null.
    * @throws IllegalArgumentsException if upval.length is wrong.
    */
   LuaFunction(Proto proto, UpVal[] upval, LuaTable env) {
     if (null == proto || null == upval || null == env) {
       throw new NullPointerException();
     }
     if (upval.length != proto.getNUps()) {
       throw new IllegalArgumentException();
     }
 
     this.p = proto;
     this.upval = upval;
     this.env = env;
   }
 
   /** Get nth UpVal. */
   UpVal upVal(int n) {
     return upval[n];
   }
 
   /** Get the Proto object. */
   Proto proto() {
     return p;
   }
 
   /** Getter for environment. */
   LuaTable getEnv() {
     return env;
   }
   /** Setter for environment. */
   void setEnv(LuaTable env) {
     if (null == env) {
       throw new NullPointerException();
     }
 
     this.env = env;
   }
 
 
 }
