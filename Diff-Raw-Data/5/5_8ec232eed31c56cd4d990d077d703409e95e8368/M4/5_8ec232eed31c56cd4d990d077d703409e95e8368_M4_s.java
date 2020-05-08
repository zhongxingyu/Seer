 package chum.fp;
 
 /** 
  * A 4x4 Fixed-point matrix
  * <p>
  * Adapted from Android ApiDemos 'kube' sample (M4.java),
  * And from JCollada (Mat4.java)
  */
 public class M4 {
     public int[] m = new int[16];
     public static M4 xform = new M4();
 	
     public M4() {
     }
 	
     public M4(M4 other) {
         copy(other);
     }
 
     public final M4 copy(M4 other) {
         for (int i = 0; i < 16; ++i)
                 m[i] = other.m[i];
         return this;
     }
 
     public final void multiply(Vec3 src, Vec3 dest) {
         // x = src.x * m[0][0] + src.y * m[0][1] + src.z * m[0][2] + m[0][3]
         // y = src.x * m[1][0] + src.y * m[1][1] + src.z * m[1][2] + m[1][3]
         // z = src.x * m[2][0] + src.y * m[2][1] + src.z * m[2][2] + m[2][3]
         long lx = src.x, ly = src.y, lz = src.z;
         dest.x = (int) ( ((lx * (long)m[0]) >> 16) +
                          ((ly * (long)m[1]) >> 16) +
                          ((lz * (long)m[2]) >> 16) +
                          ((     (long)m[3])      ) );
         dest.y = (int) ( ((lx * (long)m[4]) >> 16) +
                          ((ly * (long)m[5]) >> 16) +
                          ((lz * (long)m[6]) >> 16) +
                          ((     (long)m[7])      ) );
         dest.z = (int) ( ((lx * (long)m[8]) >> 16) +
                          ((ly * (long)m[9]) >> 16) +
                          ((lz * (long)m[10]) >> 16) +
                          ((     (long)m[11])      ) );
     }
 
 
     /* Non-threadsafe local values for multiply() */
     private static long[] m1 = new long[16];
     private static long[] m2 = new long[16];
 
     public final void multiply(M4 other, M4 dest) {
         m1[0] = (long)(m[0]);
         m1[1] = (long)(m[1]);
         m1[2] = (long)(m[2]);
         m1[3] = (long)(m[3]);
         m1[4] = (long)(m[4]);
         m1[5] = (long)(m[5]);
         m1[6] = (long)(m[6]);
         m1[7] = (long)(m[7]);
         m1[8] = (long)(m[8]);
         m1[9] = (long)(m[9]);
         m1[10] = (long)(m[10]);
         m1[11] = (long)(m[11]);
         m1[12] = (long)(m[12]);
         m1[13] = (long)(m[13]);
         m1[14] = (long)(m[14]);
         m1[15] = (long)(m[15]);
 
         int[] om = other.m;
         m2[0] = (long)(om[0]);
         m2[1] = (long)(om[1]);
         m2[2] = (long)(om[2]);
         m2[3] = (long)(om[3]);
         m2[4] = (long)(om[4]);
         m2[5] = (long)(om[5]);
         m2[6] = (long)(om[6]);
         m2[7] = (long)(om[7]);
         m2[8] = (long)(om[8]);
         m2[9] = (long)(om[9]);
         m2[10] = (long)(om[10]);
         m2[11] = (long)(om[11]);
         m2[12] = (long)(om[12]);
         m2[13] = (long)(om[13]);
         m2[14] = (long)(om[14]);
         m2[15] = (long)(om[15]);
 
         dest.m[0 ] = (int)((m1[0 ]*m2[0] + m1[1 ]*m2[4] + m1[2 ]*m2[8 ] + m1[3 ]*m2[12]) >> 16);
         dest.m[1 ] = (int)((m1[0 ]*m2[1] + m1[1 ]*m2[5] + m1[2 ]*m2[9 ] + m1[3 ]*m2[13]) >> 16);
         dest.m[2 ] = (int)((m1[0 ]*m2[2] + m1[1 ]*m2[6] + m1[2 ]*m2[10] + m1[3 ]*m2[14]) >> 16);
         dest.m[3 ] = (int)((m1[0 ]*m2[3] + m1[1 ]*m2[7] + m1[2 ]*m2[11] + m1[3 ]*m2[15]) >> 16);
         dest.m[4 ] = (int)((m1[4 ]*m2[0] + m1[5 ]*m2[4] + m1[6 ]*m2[8 ] + m1[7 ]*m2[12]) >> 16);
         dest.m[5 ] = (int)((m1[4 ]*m2[1] + m1[5 ]*m2[5] + m1[6 ]*m2[9 ] + m1[7 ]*m2[13]) >> 16);
         dest.m[6 ] = (int)((m1[4 ]*m2[2] + m1[5 ]*m2[6] + m1[6 ]*m2[10] + m1[7 ]*m2[14]) >> 16);
         dest.m[7 ] = (int)((m1[4 ]*m2[3] + m1[5 ]*m2[7] + m1[6 ]*m2[11] + m1[7 ]*m2[15]) >> 16);
         dest.m[8 ] = (int)((m1[8 ]*m2[0] + m1[9 ]*m2[4] + m1[10]*m2[8 ] + m1[11]*m2[12]) >> 16);
         dest.m[9 ] = (int)((m1[8 ]*m2[1] + m1[9 ]*m2[5] + m1[10]*m2[9 ] + m1[11]*m2[13]) >> 16);
         dest.m[10] = (int)((m1[8 ]*m2[2] + m1[9 ]*m2[6] + m1[10]*m2[10] + m1[11]*m2[14]) >> 16);
         dest.m[11] = (int)((m1[8 ]*m2[3] + m1[9 ]*m2[7] + m1[10]*m2[11] + m1[11]*m2[15]) >> 16);
         dest.m[12] = (int)((m1[12]*m2[0] + m1[13]*m2[4] + m1[14]*m2[8 ] + m1[15]*m2[12]) >> 16);
         dest.m[13] = (int)((m1[12]*m2[1] + m1[13]*m2[5] + m1[14]*m2[9 ] + m1[15]*m2[13]) >> 16);
         dest.m[14] = (int)((m1[12]*m2[2] + m1[13]*m2[6] + m1[14]*m2[10] + m1[15]*m2[14]) >> 16);
         dest.m[15] = (int)((m1[12]*m2[3] + m1[13]*m2[7] + m1[14]*m2[11] + m1[15]*m2[15]) >> 16);
     }
 	
 
     public final M4 setIdentity() {
         for (int i = 1; i < 16; ++i)
             m[i] = 0;
        m[0] = m[5] = m[10] = m[15] = 1;
         return this;
     }
 
 
     public final M4 clear() {
         for (int i = 0; i < 16; ++i)
             m[i] = 0;
         return this;
     }
 
 
     public final void transpose(M4 dest) {
         //dest.m[0] = m[0];
         dest.m[1 ] = m[4];
         dest.m[2 ] = m[8];
         dest.m[3 ] = m[12];
         dest.m[4 ] = m[1];
         //dest.m[5 ] = m[5];
         dest.m[6 ] = m[6];
         dest.m[7 ] = m[7];
         dest.m[8 ] = m[2];
         dest.m[9 ] = m[6];
         //dest.m[10] = m[10];
         dest.m[11] = m[14];
         dest.m[12] = m[3];
         dest.m[13] = m[7];
         dest.m[14] = m[11];
         //dest.m[15] = m[15];
     }
 
     
     public final M4 scale(int x, int y, int z) {
         clear();
         m[0] = x;
         m[5] = y;
         m[10] = z;
         m[15] = FP.ONE;
         return this;
     }
 
     
     public final M4 scale(int scale) {
         clear();
         m[0] = scale;
         m[5] = scale;
         m[10] = scale;
         m[15] = FP.ONE;
         return this;
     }
 
     
     public final M4 translate(Vec3 v) {
         setIdentity();
         m[3] = v.x;
         m[7] = v.y;
         m[11] = v.z;
         return this;
     }
 
     
     public final M4 rotate(Vec3 v, int theta) {
         theta >>= 1; // * .5
         int s = FP.sin(theta);
         Vec4 q = new Vec4((int)(((long)v.x * (long)s) >> 16),
                           (int)(((long)v.y * (long)s) >> 16),
                           (int)(((long)v.z * (long)s) >> 16),
                           FP.cos(theta));
         return rotate(q);
     }
 
 
     /**
        Rotate by a Quarternion
        (see Mat4::MakeHRot() from SVL)
     */
     public final M4 rotate(Vec4 q) {
         //setIdentity();
 
         long i1  = (long)q.x;
         long j1  = (long)q.y;
         long k1  = (long)q.z;
         long l1 = (long)q.w;
         long i2 = i1 << 1;
         long j2 = j1 << 1;
         long k2 = k1 << 1;
         long ij = (i2 * j1) >> 16;
         long ik = (i2 * k1) >> 16;
         long jk = (j2 * k1) >> 16;
         long ri = (i2 * l1) >> 16;
         long rj = (j2 * l1) >> 16;
         long rk = (k2 * l1) >> 16;
 
         i2 = (i2 * i1) >> 16;
         j2 = (j2 * j1) >> 16;
         k2 = (k2 * k1) >> 16;
 
         m[0] = FP.ONE - (int)(j2 + k2);
         m[1] = (int)(ij - rk);
         m[2] = (int)(ik + rj);
         m[3] = 0;
 
         m[4] = (int)(ij + rk);
         m[5] = FP.ONE - (int)(i2 + k2);
         m[6] = (int)(jk - ri);
         m[7] = 0;
 
         m[8] = (int)(ik - rj);
         m[9] = (int)(jk + ri);
         m[10] = FP.ONE - (int)(i2 + j2);
         m[11] = 0;
 
         m[12] = 0;
         m[13] = 0;
         m[14] = 0;
        m[15] = 1;
 
         return this;
     }
 
     
     @Override
     public String toString() {
         return String.format("[%.3f %.3f %.3f %.3f\n"+
                              " %.3f %.3f %.3f %.3f\n"+
                              " %.3f %.3f %.3f %.3f\n"+
                              " %.3f %.3f %.3f %.3f]",
                              FP.toFloat(m[0]),
                              FP.toFloat(m[1]),
                              FP.toFloat(m[2]),
                              FP.toFloat(m[3]),
                              FP.toFloat(m[4]),
                              FP.toFloat(m[5]),
                              FP.toFloat(m[6]),
                              FP.toFloat(m[7]),
                              FP.toFloat(m[8]),
                              FP.toFloat(m[9]),
                              FP.toFloat(m[10]),
                              FP.toFloat(m[11]),
                              FP.toFloat(m[12]),
                              FP.toFloat(m[13]),
                              FP.toFloat(m[14]),
                              FP.toFloat(m[15]));
     }
 }
