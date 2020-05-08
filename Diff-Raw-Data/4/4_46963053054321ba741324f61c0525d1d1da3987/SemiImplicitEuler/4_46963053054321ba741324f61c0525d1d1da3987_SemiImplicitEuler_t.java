 package feynstein.properties.integrators;
 
 import feynstein.*;
 import feynstein.geometry.*;
 import feynstein.utilities.*;
 
 import java.util.ArrayList;
 
 public class SemiImplicitEuler extends Integrator<SemiImplicitEuler> {
     public SemiImplicitEuler(Scene scene) {
 	super(scene);
 	objectType = "SemiImplicitEuler";
     }
 	
     public void update() {
 		Scene scene = super.getScene();
 		// This is a list of applied force values (in Newtons), in 
 		// the x, y, and z directions. The size of this list will
 		// be the size of the number of particles in the simulation
 		//TODO make method in scene for getglobalforces
 		double[] F = scene.globalForceMagnitude();
 		// grab global list of particles for the scene
 		ArrayList<Particle> parts = scene.getMesh().getParticles();
 		
 	// TODO (sainsley) : remove this test
 	/*for (Particle p : parts) {
 	    // v[1] = v[0] + a*dt = v[0] + dt*f/m
 	    Vector3d newVel = p.getVel().plus(new Vector3d(0,0,0.5));
 	    // x[1] = x[0] + v*dt
 	    Vector3d newPos =p.getPos().plus(newVel.dot(h));
 	    p.update(newPos, newVel);
 	}*/
 		
 	
		for (int i = 0; i < parts.size(); i++) {
 			Vector3d force = new Vector3d(F[3*i],F[3*i+1],F[3*i+2]);
 			// v[1] = v[0] + a*dt = v[0] + dt*f/m
 			Vector3d newVel = parts.get(i).getVel().plus(force.dot(h/parts.get(i).getMass()));
 			// x[1] = x[0] + v*dt
 			Vector3d newPos = parts.get(i).getPos().plus(newVel.dot(h));
 			parts.get(i).update(newPos, newVel);
 		}
     }
 
 }
