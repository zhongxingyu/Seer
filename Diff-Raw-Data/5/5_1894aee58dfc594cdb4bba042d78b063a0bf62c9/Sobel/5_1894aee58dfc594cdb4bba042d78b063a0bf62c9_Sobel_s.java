 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 
 import java.io.IOException;
 import java.io.File;
 
 public class Sobel {
     static int[] intensity(int[] rgbs) {
         int r, g, b, l, luminances[];
 
         luminances = new int[rgbs.length];
 
         for (int i = 0; i < rgbs.length; i++) {
             r = 0xFF & (rgbs[i] >> 16);
             g = 0xFF & (rgbs[i] >> 8);
             b = 0xFF & (rgbs[i]);
             l = (r + g + b) / 3;
 
             luminances[i] = l;
         }
 
         return luminances;
     }
 
     static int intensityToGrayScale(int i) {
        i &= 0xFF;
         return 0xFF000000 | (i << 16) | (i << 8) | i;
     }
 
     static int[] sobel(int[] rgbs, int w, int h) {
         int rw, gx, gy, g[];
 
         g = new int[rgbs.length];
 
         for (int r = 1; r < h - 1; r ++) {
             rw = r * w;
             for (int c = 1; c < w - 1; c ++) {
                 gx = 2 * rgbs[rw + c + 1] 
                    - 2 * rgbs[rw + c - 1]
                    +     rgbs[rw - w + c + 1]
                    +     rgbs[rw + w + c + 1]
                    -     rgbs[rw - w + c - 1]
                    -     rgbs[rw + w + c - 1];
 
                 gy = 2 * rgbs[rw + w + c] 
                    - 2 * rgbs[rw - w + c]
                    +     rgbs[rw + w + c + 1]
                    +     rgbs[rw + w + c - 1]
                    -     rgbs[rw - w + c + 1]
                    -     rgbs[rw - w + c - 1];
 
                 g[rw + c] = (int) (Math.sqrt(gx * gx + gy * gy));
                 g[rw + c] = intensityToGrayScale(g[rw + c]);
             }
         }
 
         return g;
     }
 
 
     public static void main(String[] argv) throws IOException {
         int w, h, rgbs[], tmp[];
         BufferedImage in, out;
 
         in = ImageIO.read(new File(argv[0]));
         w = in.getWidth();
         h = in.getHeight();
         rgbs = new int[w * h];
         in.getRGB(0, 0, w, h, rgbs, 0, w);
 
 
         tmp = intensity(rgbs);
         rgbs = sobel(tmp, w, h);
 
 
         out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         out.setRGB(0, 0, w, h, rgbs, 0, w);
 
         ImageIO.write(out, "png", new File(argv[0] + "_sobel.png"));
     }
 }
