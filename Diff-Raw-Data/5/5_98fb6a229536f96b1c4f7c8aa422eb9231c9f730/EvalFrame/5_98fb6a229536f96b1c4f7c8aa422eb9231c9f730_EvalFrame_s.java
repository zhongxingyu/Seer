 package scximpl;
 
// FIXME: should package-private
public final class EvalFrame { // FIXME: this size is arbitrary
    public EvalFrame() { this( 10000 ); }
     public EvalFrame(int size) {
         b = new boolean[size];
         o = new byte[size];
         c = new char[size];
         w = new short[size];
         i = new int[size];
         l = new long[size];
         f = new float[size];
         d = new double[size];
         k = new Object[size];
     }
 
     public boolean[] b;
     public byte[] o;
     public char[] c;
     public short[] w;
     public int[] i;
     public long[] l;
     public float[] f;
     public double[] d;
     public Object[] k;
 
     public int ofs_b = -1;
     public int ofs_o = -1;
     public int ofs_c = -1;
     public int ofs_w = -1;
     public int ofs_i = -1;
     public int ofs_l = -1;
     public int ofs_f = -1;
     public int ofs_d = -1;
     public int ofs_k = -1;
 
     /*
     final void b_push( boolean value ) { ofs_b += 1; b[ ofs_b ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void b_pop() { ofs_b -= 1; }
     //    final void b_pushFrame( int count ) { ofs_b += count; }
     final void b_popFrame( int count ) { ofs_b -= count; }
     // TODO: ajouter version sans paramètre de setLocal getLocal (cas particulier, plus rapide si jamais scala n'inline pas la méthode)
     final boolean b_getLocal( int index ) { return b[ ofs_b + index ]; }
     final void b_setLocal( int index, boolean value ) { b[ ofs_b + index ] = value; }
 
     final void o_push( byte value ) { ofs_o += 1; o[ ofs_o ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void o_pop() { ofs_o -= 1; }
     //    final void o_pushFrame( int count ) { ofs_o += count; }
     final void o_popFrame( int count ) { ofs_o -= count; }
     final byte o_getLocal( int index ) { return o[ ofs_o + index ]; }
     final void o_setLocal( int index, byte value ) { o[ ofs_o + index ] = value; }
 
     final void c_push( char value ) { ofs_c += 1; c[ ofs_c ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void c_pop() { ofs_c -= 1; }
     //    final void c_pushFrame( int count ) { ofs_c += count; }
     final void c_popFrame( int count ) { ofs_c -= count; }
     final char c_getLocal( int index ) { return c[ ofs_c + index ]; }
     final void c_setLocal( int index, char value ) { c[ ofs_c + index ] = value; }
 
     final void w_push( short value ) { ofs_w += 1; w[ ofs_w  ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void w_pop() { ofs_w -= 1; }
     //    final void w_pushFrame( int count ) { ofs_w += count; }
     final void w_popFrame( int count ) { ofs_w -= count; }
     final short w_getLocal( int index ) { return w[ ofs_w + index ]; }
     final void w_setLocal( int index, short value ) { w[ ofs_w + index ] = value; }
 
     final void i_push( int value ) { ofs_i += 1; i[ ofs_i ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void i_pop() { ofs_i -= 1; }
     //    final void i_pushFrame( int count ) { ofs_i += count; }
     final void i_popFrame( int count ) { ofs_i -= count; }
     final int i_getLocal( int index ) { return i[ ofs_i + index ]; }
     final void i_setLocal( int index, int value ) { i[ ofs_i + index ] = value; }
 
     final void l_push( long value ) { ofs_l += 1; l[ ofs_l ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void l_pop() { ofs_l -= 1; }
     //    final void l_pushFrame( int count ) { ofs_l += count; }
     final void l_popFrame( int count ) { ofs_l -= count; }
     final long l_getLocal( int index ) { return l[ ofs_l + index ]; }
     final void l_setLocal( int index, long value ) { l[ ofs_l + index ] = value; }
 
     final void f_push( float value ) { ofs_f += 1; f[ ofs_f  ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void f_pop() { ofs_f -= 1; }
     //    final void f_pushFrame( int count ) { ofs_f += count; }
     final void f_popFrame( int count ) { ofs_f -= count; }
     final float f_getLocal( int index ) { return f[ ofs_f + index ]; }
     final void f_setLocal( int index, float value ) { f[ ofs_f + index ] = value; }
 
     final void d_push( double value ) { ofs_d += 1; d[ ofs_d ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void d_pop() { ofs_d -= 1; }
     //    final void d_pushFrame( int count ) { ofs_d += count; }
     final void d_popFrame( int count ) { ofs_d -= count; }
     final double d_getLocal( int index ) { return d[ ofs_d + index ]; }
     final void d_setLocal( int index, double value ) { d[ ofs_d + index ] = value; }
 
     final void k_push( Object value ) { ofs_k += 1; k[ ofs_k ] = value; } // TODO: voir si faire l'incrémentation APRES améliore les perfs
     final void k_pop() { ofs_k -= 1; }
     //    final void k_pushFrame( int count ) { ofs_k += count; }
     final void k_popFrame( int count ) { ofs_k -= count; }
     final Object k_getLocal( int index ) { return k[ ofs_k + index ]; }
     final void k_setLocal( int index, Object value ) { k[ ofs_k + index ] = value; }
 */
 }
