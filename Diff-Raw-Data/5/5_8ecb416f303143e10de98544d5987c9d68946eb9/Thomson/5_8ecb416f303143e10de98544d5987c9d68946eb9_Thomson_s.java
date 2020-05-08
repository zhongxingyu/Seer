 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author martins
  */
 public class Thomson {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 
 		int point_count = Integer.parseInt(args[0]);
 		int run_count = Integer.parseInt(args[1]);
 		int display_sphere = Integer.parseInt(args[2]);
 		
 		ThomsonSphere sphere = new ThomsonSphere(point_count);
 
 		double smallest_found_energy = -1;
 		
 		// Run width formpanel
 		if (display_sphere==1) {
 
 			ThomsonPreview preview = new ThomsonPreview(sphere);
 
			for (int i = 0; i <= run_count; i++) {
 
 				double sphere_energy = sphere.get_energy();
 				if (smallest_found_energy > sphere_energy || smallest_found_energy == -1) {
 					smallest_found_energy = sphere_energy;
 				}
 
 				sphere.arrange_points();
 				preview.repaint();
 			}
 		} 
 		else {
			for (int i = 0; i <= run_count; i++) {
 
 				double sphere_energy = sphere.get_energy();
 				if (smallest_found_energy > sphere_energy || smallest_found_energy == -1) {
 					smallest_found_energy = sphere_energy;
 				}
 				
 				sphere.arrange_points();
 			}
 		}
 
 		// Print results
 		System.out.println("Smallest found energy " + smallest_found_energy);
 	}
 }
