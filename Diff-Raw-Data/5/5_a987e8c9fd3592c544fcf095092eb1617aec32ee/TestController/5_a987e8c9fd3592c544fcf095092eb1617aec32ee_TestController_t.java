 package testParticleSystem;
 
 import testParticleSystem.PhysicEngine;
 
 	public class TestController{
 
 	    public static void main(String str[]) {
 	    	PMgnt pm = new PMgnt(2,200,200,1);
 			pm.printParticleLocation();
 			
 			TestParticleGUI t1 = new TestParticleGUI(pm,20);
 			t1.setSize(600,600);
 			t1.setVisible(true);
			PhysicEngine pe = new PhysicEngine(pm.particlesystem,200,200);
 			int i=0;
 			while(i<100){
			pe.run();
 			pm.printParticleLocation();
 			i++;
 			}
 			
 //			for(Particle p :pm.particlesystem){
 //				p.setLocation(0, p.getLocation(0)+100);
 //				p.setLocation(1, p.getLocation(1)+100);
 //			}
 //			t1.setParticleMgnt(pm);
 //			t1.repaint();
 			
 	    }
 }
