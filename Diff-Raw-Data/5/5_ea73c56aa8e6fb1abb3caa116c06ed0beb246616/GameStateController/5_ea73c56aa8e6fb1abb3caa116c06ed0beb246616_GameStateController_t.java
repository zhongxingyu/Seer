 package server;
 
 import commonlib.gameObjects.Particle;
 import commonlib.gameObjects.Queen;
 import commonlib.gameObjects.Swarm;
 import commonlib.gameObjects2.nutrient;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Derek
  * Date: 11/16/12
  * Time: 1:13 AM
  * To change this template use File | Settings | File Templates.
  */
 public class GameStateController
 {
     public String calculateDamage(Player player1, Player player2)
     {
         Swarm swarm1 = player1.getSwarm();
         Swarm swarm2 = player2.getSwarm();
 
         int swarm1Damage = checkParticleDamage(swarm1, swarm2);
         int swarm2Damage = checkParticleDamage(swarm2, swarm1);
 
         swarm1.getQueen().takeDamage(swarm1Damage);
         swarm2.getQueen().takeDamage(swarm2Damage);
 
         boolean swarm1Dead = swarm1.getQueen().getHitPoints() < 1;
         boolean swarm2Dead = swarm2.getQueen().getHitPoints() < 1;
 
         if(swarm1Dead && swarm2Dead)
         {
             return("Draw");
         }
         else if(swarm1Dead)
         {
             return("Red Player Wins!");
         }
         else if(swarm2Dead)
         {
             return("Blue Player Wins!");
         }
         return null;
     }
 
     private int checkParticleDamage(Swarm defender, Swarm attacker)
     {
         int damage = 0;
         for(Particle particle : attacker.getParticles())
         {
             if(touchingParticle(defender.getQueen(), particle))
             {
                 damage++;
             }
         }
         return damage;
     }
 
     private boolean touchingParticle(Queen queen, Particle particle)
     {
         double distanceX = particle.getX() - queen.getX();
         double distanceY = particle.getY() - queen.getY();
         double totalDistance = Math.sqrt(distanceX*distanceX + distanceY*distanceY);
         double touchRange = Queen.RADIUS + Particle.RADIUS;
         return totalDistance < touchRange;
     }
 
     public void calculatePowerups(Swarm swarm, List<nutrient> nutrients)
     {
         Queen queen = swarm.getQueen();
         List<nutrient> touchedNutrients = new ArrayList<nutrient>();
         for(nutrient n : nutrients)
         {
             if(touchingNutrient(queen, n))
             {
                 touchedNutrients.add(n);
                if(swarm.getParticles().size() < 15)
                {
                    swarm.addParticle(new Particle(queen.getX(), queen.getY()));
                }
             }
         }
         for(nutrient n : touchedNutrients)
         {
             nutrients.remove(n);
         }
     }
 
     private boolean touchingNutrient(Queen queen, nutrient n)
     {
         double distanceX = n.getCor().getx() - queen.getX();
         double distanceY = n.getCor().gety() - queen.getY();
         double totalDistance = Math.sqrt(distanceX*distanceX + distanceY*distanceY);
         double touchRange = Queen.RADIUS + nutrient.RADIUS;
         return totalDistance < touchRange;
     }
 }
