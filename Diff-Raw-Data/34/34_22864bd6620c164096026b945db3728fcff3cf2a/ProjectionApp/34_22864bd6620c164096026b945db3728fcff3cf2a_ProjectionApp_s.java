 package br.usp.pf.app;
 
 import java.io.IOException;
 
 import matrix.AbstractMatrix;
 import projection.model.ProjectionModelComp;
 import projection.view.ProjectionFrameComp;
 import br.usp.pf.projection.FastForceScheme2D;
 import br.usp.pf.projection.ForceScheme2D;
 import distance.DistanceMatrix;
 
 public class ProjectionApp {
 
 	public static void project(String dmatFile, int printInterval, boolean fast) throws IOException {
 		
 		DistanceMatrix dmat = new DistanceMatrix(dmatFile);
 		AbstractMatrix projection = null;
 		
 		if (fast) {
 			FastForceScheme2D ff = new FastForceScheme2D();
 			ff.setPrintInterval(printInterval);
 			projection = ff.project(dmat);
 		} else {
 			ForceScheme2D ff = new ForceScheme2D();
 			ff.setPrintInterval(printInterval);
 			projection = ff.project(dmat);
 		}
 		
 		ProjectionModelComp model = new ProjectionModelComp();;
 		ProjectionFrameComp frame = new ProjectionFrameComp();;
 		
 		model.input(projection);
 		model.execute();
 
 		frame.setTitle(dmatFile);
 		frame.input(model.output());
 		frame.execute();
 		
 	}
 
 	public static void main(String[] args) throws IOException {
		ProjectionApp.project("/work1/wokspace/pf/data/0012-frust-cut=3/dmat-jumps.data", 100000, false);
 //		ProjectionApp.project("/home/fatore/workspace/pf/data/minimo-old/3873-jumps.dmat", 100100, true);
 	}
 }
