 
 public class ParticleSystem {
 	public static java.util.Vector<Particle> particles;
 	public static boolean enabled;
 	public static final int DENSITY = 5;
 
 	public static void init(boolean enable){
 		System.err.println("Initializing Particles");
 		enabled = enable;
 		particles = new java.util.Vector<Particle>();
 	}
 
 	public static void addFireParticle(Actor ship){
 		if(enabled)
 			for(int i = 0; i < DENSITY; i++)
 				particles.add(new FireParticle(ship));
 	}
 
 	public static void addDebrisParticle(Actor actor){
 		if(enabled)
 			for(int i = 0; i < DENSITY * 6; i++)
 				particles.add(new DebrisParticle(actor));
 	}
 	
 	public static void addPlasmaParticle(Actor actor){
 		if(enabled)
 			for(int i = 0; i < DENSITY * 6; i++){
 				particles.add(new PlasmaParticle(actor));
 			}
 	}
 	
 	public static void addExplosion(Vector pos) {
 		if(enabled){
			for(int i = 0; i < DENSITY * 15; i++ ){
 				particles.add(new FireParticle(pos));
 			}
 		}
 	}
 
 	public static void updateParticles(){
 		for(int i = 0; i < particles.size(); i++){
 			Particle p = particles.get(i);
 			p.update();
 		}
 	}
 	
 	public static boolean isEnabled() {
 		return enabled;
 	}
 	
 	public static boolean isEnabled(boolean toggle) {
 		enabled = toggle;
 		
 		if (enabled == false)
 			particles.clear(); // Don't keep our particles around if the user disables them mid game
 			
 		return enabled;
 	}
 
 
 }
