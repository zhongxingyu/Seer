 package ch.epfl.flamemaker.ifs;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 import ch.epfl.flamemaker.geometry2d.AffineTransformation;
 import ch.epfl.flamemaker.geometry2d.Point;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 
 public class RenderTest {
 	public static void main(String args[]) throws Exception {
 		
 		
 		/*
 		 * Settings for fougère de Barnsley. Comment next line to test.
 		 */
		/*
 
 		ArrayList<AffineTransformation> transformations = new ArrayList<AffineTransformation>();
 		transformations.add(new AffineTransformation(0, 0, 0, 0, 0.16, 0));
 		transformations.add(new AffineTransformation(0.2, -0.26, 0, 0.23, 0.22, 1.6));
 		transformations.add(new AffineTransformation(-0.15, 0.28, 0, 0.26, 0.24, 0.44));
 		transformations.add(new AffineTransformation(0.85, 0.04, 0, -0.04, 0.85, 1.6));
 		
		Rectangle viewport = new Rectangle(new Point(0, 5), 1, 10);
 		int width = 120, height = 200, density = 150;
 		
 		//*/
 		
 		/*
 		 * Settings for triangle de Sierpinski. Comment next line to test.
 		 */
		//*
 		
 		ArrayList<AffineTransformation> transformations = new ArrayList<AffineTransformation>();
 		transformations.add(new AffineTransformation(0.5, 0, 0, 0, 0.5, 0));
 		transformations.add(new AffineTransformation(0.5, 0, 0.5, 0, 0.5, 0));
 		transformations.add(new AffineTransformation(0.5, 0, 0.25, 0, 0.5, 0.5));
 		
 		Rectangle viewport = new Rectangle(new Point(0.5, 0.5), 1, 1);
 		int width = 200, height = 200, density = 1;
 		//*/
 		
 		IFS fractal = new IFS(transformations);
 		IFSAccumulator result = fractal.compute(viewport, width, height, density);
 		
 		PrintStream file = new PrintStream("./fractal.pbm");
 		
 		file.println("P1");
 		file.println(result.width()+" "+result.height());
 		
 		for(int i = result.height()-1 ; i > 0 ; i--) {
 			for(int j = 0; j < result.width(); j++) {
 				file.print(result.isHit(j, i) ? "1 " : "0 ");
 			}
 			file.print("\n");
 		}
 		file.close();
 	}
 }
