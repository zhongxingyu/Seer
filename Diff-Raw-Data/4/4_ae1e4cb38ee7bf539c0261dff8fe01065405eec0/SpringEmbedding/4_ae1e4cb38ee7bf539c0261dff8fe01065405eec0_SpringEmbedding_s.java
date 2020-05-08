 package graph;
 
 import java.util.ArrayList;
 
 public class SpringEmbedding {
 
 	public final double DAMPING = 0.5;
 	public final double TIMESTEP = 1;
 
 	public void springEmbed(ArrayList<Vertex> vertices){
 		Tuple total_kinetic_energy = new Tuple();
 		for (Vertex v : vertices){
 			Tuple netto_sum = new Tuple();
 
 			/*for each other node
 			 * net-force := net-force + Coulomb_repulsion( this_node, other_node )
 			 * next node
 			 */
 			for (Vertex v_other : vertices){
 				if (v_other == v)
 					continue;
 				netto_sum.sum((Tuple.coulomb_repulsion(v,v_other)));
 			}
 			/*
 			 *   for each spring connected to this node
 			 *   net-force := net-force + Hooke_attraction( this_node, spring )
 			 *   next spring
 			 */
 			netto_sum.sum((Tuple.hooke_attraction(v,v.getToVertex())));
 			//v.velocity := (v.velocity + timestep * net-force) * damping
 			v.getVelocity().dx = (v.getVelocity().dx * TIMESTEP * netto_sum.dx) * DAMPING;
 			v.getVelocity().dy = (v.getVelocity().dy * TIMESTEP * netto_sum.dy) * DAMPING;
 			//v.position := v.position + timestep * this_node.velocity
			v.getPosition().x += v.getPosition().x + (TIMESTEP * v.getVelocity().dx);
			v.getPosition().y += v.getPosition().y + (TIMESTEP * v.getVelocity().dy);
 			//total_kinetic_energy := total_kinetic_energy + this_node.mass * (this_node.velocity)^2
 			total_kinetic_energy.dx =  total_kinetic_energy.dx + (v.getMass() * Math.sqrt(v.getVelocity().dx));
 			total_kinetic_energy.dy =  total_kinetic_energy.dy+ (v.getMass() * Math.sqrt(v.getVelocity().dy));
 		}
 	}
 }
