 package bomberman.server.elements;
 
 import bomberman.server.Server;
 
 public class Wall extends Element {
 
    private double bonus_probability = 1;
 
     @Override
     public void burn() {
         if (this.active) {
             this.setActive(false);
            //this.bonus_probability = Math.random() / 2;
             this.delayRebirth();
             Server.sendAll("del_element", this.index);
 
             if (Math.random() < this.bonus_probability) {
                 System.out.println("Bonus");
                 Bonus bonus = new Bonus();
                 bonus.setIndex(this.index);
                 bonus.setType((int) Math.ceil(Math.random() * 3));
                 try {
                     System.out.println("MAILOL");
                     Server.board.setElement(bonus);
                     Server.sendAll("add_element", Element.export(bonus));
                 } catch (Exception e) {
                     System.out.println(e.getMessage());
                 }
             }
         }
     }
 
     public void setBonusProbability(double probability) {
         this.bonus_probability = probability;
     }
 }
