 package nogbeter.demo;
 
 import bezier.image.generated.ColorsAlpha;
 import sun.java2d.loops.DrawLine;
 import nogbeter.demo.awt.DemoBase;
 import nogbeter.paths.Path;
 import nogbeter.paths.PathIndex;
 import nogbeter.paths.compound.ClosedPath;
import nogbeter.paths.compound.NotClosedException;
 import nogbeter.paths.factory.PathFactory;
 import nogbeter.paths.factory.TextFactory;
 import nogbeter.paths.results.intersections.IIntersections;
 import nogbeter.paths.results.intersections.Intersection;
 import nogbeter.paths.results.intersections.Intersections;
 import nogbeter.paths.results.project.BestProject;
 import nogbeter.paths.results.project.BestProjectTup;
 import nogbeter.points.twod.Vec;
 import static nogbeter.paths.factory.PathFactory.*;
 import static nogbeter.transform.AffineTransformation.*;
 
 public class InsideTest extends DemoBase{
 
 		public static void main(String[] argv){
 			new InsideTest();
 		}
 
 
 		private Path r;
 		
 		public InsideTest() {
 			r = TextFactory.text2Paths("wso").transform(id.scale(5).translate(400, 400));
 		}
 		
 		@Override
 		public void draw() {
 
 			draw(r,r.isInside(mouse) ? ColorsAlpha.green : ColorsAlpha.red);
 			BestProject<PathIndex> best = r.project(mouse);
 			drawLine(r.getAt(best.t),mouse);
 
 			System.out.println("");
 		}
 
 		
 
		public ClosedPath rectangle() throws NotClosedException{
 			Vec a = new Vec(100,100);
 			Vec b = new Vec(200,100);
 			Vec c = new Vec(200,200);
 			Vec d = new Vec(100,200);
 			Vec e = new Vec(150,50);
 			return createClosedPath(createLine(a,b), createLine(b,c), createLine(c,d),createLine(d,a));
 		}
 		
 	}
