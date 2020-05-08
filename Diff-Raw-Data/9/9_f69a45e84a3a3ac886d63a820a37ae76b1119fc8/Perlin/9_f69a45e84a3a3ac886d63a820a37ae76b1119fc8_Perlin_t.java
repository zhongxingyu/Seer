 package maximusvladimir.dagen;
 
 @SuppressWarnings("unused")
 public class Perlin extends Generator {
 	private static int B = 0x1000;
     private static int BM = 0xff;
     private static int N = 0x1000;
     private static int DEFAULT_SAMPLE_SIZE = 256;
     private int[] p_imp;
     private int[] p;
     private float[][] g3;
     private float[][] g2;
     private float[] g1;
     
 	public Perlin(long seed, int width, int height) {
 		super(seed, width, height);
 		p_imp = new int[DEFAULT_SAMPLE_SIZE << 1];
         int i, j, k;
         for(i = 0; i < DEFAULT_SAMPLE_SIZE; i++)
             p_imp[i] = i;
         while(--i > 0)
         {
             k = p_imp[i];
             j = (rand.nextInt(DEFAULT_SAMPLE_SIZE));
             p_imp[i] = p_imp[j];
             p_imp[j] = k;
         }
         initPerlin1();
 	}
 	public float noise(float x, float z) {
 		return (float)noise2(x,z); //improvedNoise(x,Constants.MAIN_RAND.nextDouble(),z);
 	}
 
 	public void generate() {
 	}
     private double improvedNoise(double x, double y, double z)
     {
         int uc_x = (int)Math.floor(x) & 255;
         int uc_y = (int)Math.floor(y) & 255;
         int uc_z = (int)Math.floor(z) & 255;
         double xo = x - Math.floor(x);
         double yo = y - Math.floor(y);
         double zo = z - Math.floor(z);
         double u = fade(xo);
         double v = fade(yo);
         double w = fade(zo);
         int a =  p_imp[uc_x] + uc_y;
         int aa = p_imp[a] + uc_z;
         int ab = p_imp[a + 1] + uc_z;
         int b =  p_imp[uc_x + 1] + uc_y;
         int ba = p_imp[b] + uc_z;
         int bb = p_imp[b + 1] + uc_z;
         double c1 = grad(p_imp[aa], xo, yo, zo);
         double c2 = grad(p_imp[ba], xo - 1, yo, zo);
         double c3 = grad(p_imp[ab], xo, yo - 1, zo);
         double c4 = grad(p_imp[bb], xo - 1, yo - 1, zo);
         double c5 = grad(p_imp[aa + 1], xo, yo, zo - 1);
         double c6 = grad(p_imp[ba + 1], xo - 1, yo, zo - 1);
         double c7 = grad(p_imp[ab + 1], xo, yo - 1, zo - 1);
         double c8 = grad(p_imp[bb + 1], xo - 1, yo - 1, zo - 1);
         return lerp(w, lerp(v, lerp(u, c1, c2), lerp(u, c3, c4)),
                        lerp(v, lerp(u, c5, c6), lerp(u, c7, c8)));
     }
    public float noise1(float x)
     {
         float t = x + N;
         int bx0 = ((int) t) & BM;
         int bx1 = (bx0 + 1) & BM;
         float rx0 = t - (int) t;
         float rx1 = rx0 - 1;
 
         float sx = sCurve(rx0);
 
         float u = rx0 * g1[p[bx0]];
         float v = rx1 * g1[p[bx1]];
 
         return lerp(sx, u, v);
     }
     public float noise2(float x, float y)
     {
         float t = x + N;
         int bx0 = ((int)t) & BM;
         int bx1 = (bx0 + 1) & BM;
         float rx0 = t - (int)t;
         float rx1 = rx0 - 1;
 
         t = y + N;
         int by0 = ((int)t) & BM;
         int by1 = (by0 + 1) & BM;
         float ry0 = t - (int)t;
         float ry1 = ry0 - 1;
 
         int i = p[bx0];
         int j = p[bx1];
 
         int b00 = p[i + by0];
         int b10 = p[j + by0];
         int b01 = p[i + by1];
         int b11 = p[j + by1];
 
         float sx = sCurve(rx0);
         float sy = sCurve(ry0);
 
         float[] q = g2[b00];
         float u = rx0 * q[0] + ry0 * q[1];
         q = g2[b10];
         float v = rx1 * q[0] + ry0 * q[1];
         float a = lerp(sx, u, v);
 
         q = g2[b01];
         u = rx0 * q[0] + ry1 * q[1];
         q = g2[b11];
         v = rx1 * q[0] + ry1 * q[1];
         float b = lerp(sx, u, v);
         //System.out.print("sy="+sy + "a="+a+"b="+b+ "sx="+sx+"u"+u+"v"+v);
         return lerp(sy, a, b);
     }
     private float noise3(float x, float y, float z)
     {
         float t = x + N;
         int bx0 = ((int)t) & BM;
         int bx1 = (bx0 + 1) & BM;
         float rx0 = t - (int)t;
         float rx1 = rx0 - 1;
 
         t = y + N;
         int by0 = ((int)t) & BM;
         int by1 = (by0 + 1) & BM;
         float ry0 = t - (int)t;
         float ry1 = ry0 - 1;
 
         t = z + N;
         int bz0 = ((int)t) & BM;
         int bz1 = (bz0 + 1) & BM;
         float rz0 = t - (int)t;
         float rz1 = rz0 - 1;
 
         int i = p[bx0];
         int j = p[bx1];
 
         int b00 = p[i + by0];
         int b10 = p[j + by0];
         int b01 = p[i + by1];
         int b11 = p[j + by1];
 
         t  = sCurve(rx0);
         float sy = sCurve(ry0);
         float sz = sCurve(rz0);
 
         float[] q = g3[b00 + bz0];
         float u = (rx0 * q[0] + ry0 * q[1] + rz0 * q[2]);
         q = g3[b10 + bz0];
         float v = (rx1 * q[0] + ry0 * q[1] + rz0 * q[2]);
         float a = lerp(t, u, v);
 
         q = g3[b01 + bz0];
         u = (rx0 * q[0] + ry1 * q[1] + rz0 * q[2]);
         q = g3[b11 + bz0];
         v = (rx1 * q[0] + ry1 * q[1] + rz0 * q[2]);
         float b = lerp(t, u, v);
 
         float c = lerp(sy, a, b);
 
         q = g3[b00 + bz1];
         u = (rx0 * q[0] + ry0 * q[1] + rz1 * q[2]);
         q = g3[b10 + bz1];
         v = (rx1 * q[0] + ry0 * q[1] + rz1 * q[2]);
         a = lerp(t, u, v);
 
         q = g3[b01 + bz1];
         u = (rx0 * q[0] + ry1 * q[1] + rz1 * q[2]);
         q = g3[b11 + bz1];
         v = (rx1 * q[0] + ry1 * q[1] + rz1 * q[2]);
         b = lerp(t, u, v);
 
         float d = lerp(sy, a, b);
 
         return lerp(sz, c, d);
     }
     
 	private float noise3(double x, double y, double z) {
         return noise3((float)x,(float)y,(float)z);
     }
     private double imporvedTurbulence(double x,
                                      double y,
                                      double z,
                                      float loF,
                                      float hiF)
     {
         double p_x = x + 123.456f;
         double p_y = y;
         double p_z = z;
         double t = 0;
         double f;
 
         for(f = loF; f < hiF; f *= 2)
         {
             t += Math.abs(improvedNoise(p_x, p_y, p_z)) / f;
 
             p_x *= 2;
             p_y *= 2;
             p_z *= 2;
         }
 
         return t - 0.3;
     }
     private float turbulence2(float x, float y, float freq)
     {
         float t = 0;
 
         do
         {
             t += noise2(freq * x, freq * y) / freq;
             freq *= 0.5f;
         }
         while (freq >= 1);
 
         return t;
     }
     private float turbulence3(float x, float y, float z, float freq)
     {
         float t = 0;
 
         do
         {
             t += noise3(freq * x, freq * y, freq * z) / freq;
             freq *= 0.5f;
         }
         while (freq >= 1);
 
         return t;
     }
     private float tileableNoise1(float x, float w)
     {
         return (noise1(x)     * (w - x) +
                 noise1(x - w) *      x) / w;
     }
     private float tileableNoise2(float x, float y, float w, float h)
     {
         return (noise2(x,     y)     * (w - x) * (h - y) +
                 noise2(x - w, y)     *      x  * (h - y) +
                 noise2(x,     y - h) * (w - x) *      y  +
                 noise2(x - w, y - h) *      x  *      y) / (w * h);
     }
     private float fmod(float n, float d) {
 		float x=n/d;
 		return n-floor(x)*d;
 	}
     private int floor(float a) {
 		if (a>=0) return (int)a;
 		int x=(int)a;
 		return (a==x)?x:x-1;
 	}
     private float tileableNoise3(float x,
                                 float y,
                                 float z,
                                 float w,
                                 float h,
                                 float d)
     {
         x=fmod(x,w);
         y=fmod(y,h);
         z=fmod(z,d);
 
         return (noise3(x,     y,     z)     * (w - x) * (h - y) * (d - z) +
                 noise3(x - w, y,     z)     *      x  * (h - y) * (d - z) +
                 noise3(x,     y - h, z)     * (w - x) *      y  * (d - z) +
                 noise3(x - w, y - h, z)     *      x  *      y  * (d - z) +
                 noise3(x,     y,     z - d) * (w - x) * (h - y) *      z  +
                 noise3(x - w, y,     z - d) *      x  * (h - y) *      z  +
                 noise3(x,     y - h, z - d) * (w - x) *      y  *      z  +
                 noise3(x - w, y - h, z - d) *      x  *      y  *      z) /
                 (w * h * d);
     }
     private float tileableTurbulence2(float x,
                                      float y,
                                      float w,
                                      float h,
                                      float freq)
     {
         float t = 0;
 
         do
         {
             t += tileableNoise2(freq * x, freq * y, w * freq, h * freq) / freq;
             freq *= 0.5f;
         }
         while (freq >= 1);
 
         return t;
     }
     private float tileableTurbulence3(float x,
                                      float y,
                                      float z,
                                      float w,
                                      float h,
                                      float d,
                                      float freq)
     {
         float t = 0;
 
         do
         {
             t += tileableNoise3(freq * x,
                                 freq * y,
                                 freq * z,
                                 w * freq,
                                 h * freq,
                                 d * freq) / freq;
             freq *= 0.5f;
         }
         while (freq >= 1);
 
         return t;
     }
     private double lerp(double t, double a, double b)
     {
         return a + t * (b - a);
     }
     private float lerp(float t, float a, float b)
     {
         return a + t * (b - a);
     }
     private double fade(double t)
     {
         return t * t * t * (t * (t * 6 - 15) + 10);
     }
     private double grad(int hash, double x, double y, double z)
     {
         int h = hash & 15;
         double u = (h < 8 || h == 12 || h == 13) ? x : y;
         double v = (h < 4 || h == 12 || h == 13) ? y : z;
 
         return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
     }
     private float sCurve(float t)
     {
         return (t * t * (3 - 2 * t));
     }
     private void normalize2(float[] v)
     {
         float s = (float)(1 / Math.sqrt(v[0] * v[0] + v[1] * v[1]));
         v[0] *= s;
         v[1] *= s;
     }
     private void normalize3(float[] v)
     {
         float s = (float)(1 / Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
         v[0] *= s;
         v[1] *= s;
         v[2] *= s;
     }
     private void initPerlin1()
     {
         p = new int[B + B + 2];
         g3 = new float[B + B + 2][3];
         g2 = new float[B + B + 2][2];
         g1 = new float[B + B + 2];
         int i, j, k;
 
         for(i = 0; i < B; i++)
         {
             p[i] = i;
 
             g1[i] = (float)((rand.nextInt(B + B)) - B) / B;
 
             for(j = 0; j < 2; j++)
                 g2[i][j] = (float)((rand.nextInt(B + B)) - B) / B;
             normalize2(g2[i]);
 
             for(j = 0; j < 3; j++)
                 g3[i][j] = (float)((rand.nextInt(B + B)) - B) / B;
             normalize3(g3[i]);
         }
 
         while(--i > 0)
         {
             k = p[i];
             j = (rand.nextInt(B));
             p[i] = p[j];
             p[j] = k;
         }
 
         for(i = 0; i < B + 2; i++)
         {
             p[B + i] = p[i];
             g1[B + i] = g1[i];
             for(j = 0; j < 2; j++)
                 g2[B + i][j] = g2[i][j];
             for(j = 0; j < 3; j++)
                 g3[B + i][j] = g3[i][j];
         }
     }
 }
