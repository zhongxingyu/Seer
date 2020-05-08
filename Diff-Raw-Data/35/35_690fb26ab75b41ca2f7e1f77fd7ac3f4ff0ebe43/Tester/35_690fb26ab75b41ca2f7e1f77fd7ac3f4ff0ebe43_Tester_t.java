 public class Tester
 {
     public static void main(String[] args) {
        Point[] p = {new Point(0, 0),
            
            new Point(10, 0),
            new Point(-20, 0),
            
            new Point(0, -10),
            new Point(0, 20),
            
            new Point(10, 15),
            new Point(5, 20),
            
            new Point(-7, 15),
            new Point(-5, 2),
            
            new Point(-5, -20),
            new Point(-1, -20),
            
            new Point(5, -7),
            new Point(7, -10),
        };

        int l = p.length;
        for (int i = 0; i < l; i++)
        {
            for (int j = i; j < l; j++)
            {
                p[i].drawTo(p[j]);
                String eq = (p[i].compareTo(p[j]) < 0) ? " < " : " >= ";
                StdOut.println(p[i].toString() + eq + p[j].toString());
                StdOut.println("Slope: " + p[i].slopeTo(p[j]));
                StdOut.println();
            }
        }
        
     }
 }
