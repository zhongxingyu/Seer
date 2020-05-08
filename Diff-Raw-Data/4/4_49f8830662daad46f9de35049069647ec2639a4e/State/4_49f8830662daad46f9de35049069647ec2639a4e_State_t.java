 package model_example;
 
// For simulation, it's important to use a high-quality PRNG,
// with long cycles and good distribution.
 import org.uncommons.maths.random.MersenneTwisterRNG;
 
 public class State extends model_base.TimeState {
   public long dummyState = 0;
 
   public long seed;
   public MersenneTwisterRNG prng;
   
   public State(int id, model_base.Context ctx, long seed) {
     super(id, ctx);
     this.seed = seed;
     this.prng = new MersenneTwisterRNG(longToByteArray(seed));
   }
   
   public void update() {
     if (this.prng.nextBoolean()) {
       dummyState += 1;
     }
//    ctx.debug(
   }
 
   public static final byte[] longToByteArray(long value) {
     return new byte[] {
       (byte)0, (byte)0, (byte)0, (byte)0,
       (byte)0, (byte)0, (byte)0, (byte)0, 
       // the above could be obtained from a second long
       (byte)(value >>> 56), (byte)(value >>> 48),
       (byte)(value >>> 40), (byte)(value >>> 32),
       (byte)(value >>> 24), (byte)(value >>> 16),
       (byte)(value >>> 8), (byte)value
     };
   }
 }
