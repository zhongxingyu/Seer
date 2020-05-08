 package feynstein.properties.collision;
 
 import feynstein.*;
 import feynstein.properties.*;
 import feynstein.properties.integrators.*;
 import feynstein.utilities.*;
 import java.util.*;
 
 /* Questions for Sam, revised to be less angry and more intellible
    1. Semi-implicit time stepping seems to happen before impulse response,
    and arrays needed for impulse response (the q/Q/_dot stuff) seem to be
    defined as a result of time stepping. Do we need to have time stepping 
    happen before impulse response? If not, how do we define those arrays?
    
    2. Lines 88 - 89: please double-check that this is what we want--that
    the detector checks again for collisions and then we grab the new set.
    (The only reason I'm confused about this is because of question 3.)
 
    3. Line 83: original c++ filter() method took a reference to array V. 
    Does this mean we need to update scene.globalVelocities here? If we 
    don't update it, I don't see the point of iterating this while loop.
 
 */
 
 public class ImpulseResponder extends CollisionResponder<ImpulseResponder> {
 
     Integrator integrator;
     int iter;
 
     double X[];
     double M[];	
     double[] newPos; //q
     double[] newVels; //q_dot
     double[] midStepPos; //Q
     double[] midStepVel; //Q_dot
 
     public ImpulseResponder(Scene aScene) {
 	super(aScene);
 	integrator = scene.getIntegrator();
 	iter = 100;
 	midStepPos = new double[scene.getMesh().size() * 3];
 	midStepVel = new double[midStepPos.length];
     }
 
     public ImpulseResponder set_iterations(int iterations) {
 	iter = iterations;
 	return this;
     }
     
     @SuppressWarnings("unchecked")
     public void update() {
 	X = scene.getGlobalPositions();
 	M = scene.getGlobalMasses();
 
 	// No updating side effects, just calculation:
 	double[] newPos = integrator.predictPositions();
 	double[] newVels = integrator.predictVelocities();
 
 	/* old C++ for reference
 	//update velocity
 	for(int j = 0; j < dof; j ++){
 	  q_dot[j] = V[j] + (h/M[j])*(F)[j];
 	}
 	//update position
 	q = X + h*q_dot; */
 
 	double h = integrator.getStepSize();
 
 	//midstep velocity
 	for (int i = 0; i < midStepVel.length; i++) {
 	    midStepVel[i] = (newPos[i] - X[i]) * (1 / h); //was: Q_dot = (q-X)*(1/h);
 	}
 	
 	HashSet<Collision> cSet = detector.getCollisions();
 	
 	//iteration counter
 	int j = 0;
 	if (cSet.size() > 0) {
 	    //if count < max iterations or no cap
 	    while ( (cSet.size() > 0 && j < iter) || (cSet.size() > 0 && iter == -1) ) {
 		j++;
 		
 		X = scene.getGlobalPositions();
 		M = scene.getGlobalMasses();
 
 		//filter velocities
 		scene.setGlobalVelocities(filter(midStepVel, X, M, cSet));
 
 		//step forward
 		integrator.update(); // Q = X + h*Q_dot;
 
 		detector.update();
 		cSet = detector.getCollisions();
 
 	    }
 	}	
     }
 
     @SuppressWarnings("unchecked")
     private double[] filter(double[] V, double[] X, double[] M, HashSet<Collision> cSet) {
 	for (Collision col : cSet) {
 	    //if vertex-face collision
 	    if (col.getType() == Collision.VERTEX_FACE) {
 		//indicies
 		int[] parts = col.getParticles();
 		int p = parts[0];
 		int a = parts[1];
 		int b = parts[2];
 		int c = parts[3];
 		//barycentric coords
 		double[] coords = col.getBaryCoords();
 		double u = coords[0];
 		double v = coords[1];
 		double w = coords[2];
 
 		//collision points
 		Vector3d xa = new Vector3d(X[3*p], X[3*p+1], X[3*p+2]);
 		Vector3d xb = new Vector3d(u*X[3*a]+v*X[3*b]+w*X[3*c],
 					   u*X[3*a+1]+v*X[3*b+1]+w*X[3*c+1],
 					   u*X[3*a+2]+v*X[3*b+2]+w*X[3*c+2]);
 
 		//collision velocities
 		Vector3d va = new Vector3d(V[3*p], V[3*p+1], V[3*p+2]);
 		Vector3d vb = new Vector3d(u*V[3*a]+v*V[3*b]+w*V[3*c],
 					   u*V[3*a+1]+v*V[3*b+1]+w*V[3*c+1],
 					   u*V[3*a+2]+v*V[3*b+2]+w*V[3*c+2]);
 		
 		//weighted mass
 		double m = 1 / M[3*p] + u*u / M[3*a] + v*v / M[3*b] + w*w / M[3*c];
 
 		//collision normal
 		Vector3d norm = xa.minus(xb);
 		if(norm.norm() != 0)
 		    //following line was: norm = norm / norm.norm();
 		    norm = norm.dot(1 / norm.norm());
 
 		//impulse
		//TODO check math here?
 		double I = va.minus(vb).minus(.000001).dot(norm.dot(1 / m));
 		
 		//Apply impulse
 		//apply to vertex
 		V[3*p] -= I/M[3*p]*norm.x();
 		V[3*p+1] -= I/M[3*p]*norm.y();
 		V[3*p+2] -= I/M[3*p]*norm.z();
 		//apply to triangle vertices
 		V[3*a] += u*I /M[3*a]*norm.x();
 		V[3*a+1] += u*I/M[3*a]*norm.y();
 		V[3*a+2] += u*I/M[3*a]*norm.z();
 		V[3*b] += v*I/M[3*b]*norm.x();
 		V[3*b+1] += v*I/M[3*b]*norm.y();
 		V[3*b+2] += v*I/M[3*b]*norm.z();
 		V[3*c] += w*I/M[3*c]*norm.x();
 		V[3*c+1] += w*I/M[3*c]*norm.y();
 		V[3*c+2] += w*I/M[3*c]*norm.z();
 	    }
 
 	    //edge-edge
 	    if (col.getType() == Collision.EDGE_EDGE){
 		int[] parts = col.getParticles();
 		int p1 = parts[0];
 		int q1 = parts[1];
 		int p2 = parts[2];
 		int q2 = parts[3];
 		//barycentric coords
 		double[] coords = col.getBaryCoords();
 		double s = coords[0];
 		double t = coords[1];
 
 		//collision points
 		Vector3d xa = new Vector3d(s*X[3*p1]+(1-s)*X[3*q1],
 					   s*X[3*p1+1]+(1-s)*X[3*q1+1],
 					   s*X[3*p1+2]+(1-s)*X[3*q1+2]);
 		Vector3d xb = new Vector3d(t*X[3*p2]+(1-t)*X[3*q2],
 					   t*X[3*p2+1]+(1-t)*X[3*q2+1],
 					   t*X[3*p2+2]+(1-t)*X[3*q2+2]);
 
 		//collision velocities
 		Vector3d va = new Vector3d(s*V[3*p1]+(1-s)*V[3*q1],
 					   s*V[3*p1+1]+(1-s)*V[3*q1+1],
 					   s*V[3*p1+2]+(1-s)*V[3*q1+2]);
 		Vector3d vb = new Vector3d(t*V[3*p2]+(1-t)*V[3*q2],
 					   t*V[3*p2+1]+(1-t)*V[3*q2+1],
 					   t*V[3*p2+2]+(1-t)*V[3*q2+2]);
 
 		
 		//weighted mass
 		//TODO haven't touched this...
 		double m = s*s/M[3*p1]+(1-s)*(1-s)/M[3*q1]+t*t/M[3*p2]+(1-t)*(1-t)/M[3*q2];
 		
 		//collision norm
 		Vector3d norm = xa.minus(xb);
 		if(norm.norm() != 0)
 		    //following line was: norm = norm / norm.norm();
 		    norm = norm.dot(1 / norm.norm());
 		
 		//impulse
		//TODO check math here?
 		double I = va.minus(vb).minus(.000001).dot(norm.dot(1/m));
 		
 		//apply to first edge
 		V[3*p1] -= s*I/M[3*p1]*norm.x();
 		V[3*p1+1] -= s*I/M[3*p1]*norm.y();
 		V[3*p1+2] -= s*I/M[3*p1]*norm.z();
 		V[3*q1] -= (1-s)*I/M[3*q1]*norm.x();
 		V[3*q1+1] -= (1-s)*I/M[3*q1]*norm.y();
 		V[3*q1+2] -= (1-s)*I/M[3*q1]*norm.z();
 		//apply to next edge
 		V[3*p2] += t*I/M[3*p2]*norm.x();
 		V[3*p2+1] += t*I/M[3*p2]*norm.y();
 		V[3*p2+2] += t*I/M[3*p2]*norm.z();
 		V[3*q2] += (1-t)*I/M[3*q2]*norm.x();
 		V[3*q2+1] += (1-t)*I/M[3*q2]*norm.y();
 		V[3*q2+2] += (1-t)*I/M[3*q2]*norm.z();
 	    }
 	}
 
 	return V;
     }
 
 
 
 }
