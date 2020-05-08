 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package database;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
  *
  * @author Dave
  */
 @Entity
 public class Monster implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     private String name, owner;
     private int health, strength, evade, price, genes;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     
     public String getName(){
         return name;
     }
     public void setName(String name){
         this.name = name;
     }
     
     public String getOwner(){
         return owner;
     }
     public void setOwner(String owner){
         this.owner = owner;
     }
     
     public int getHealth(){
         return health;
     }
     public void setHealth(int health){
         this.health = health;
     }
     
     public int getStrength(){
         return strength;
     }
     public void setStrength(int strength){
         this.strength = strength;
     }
     
     public int getEvade(){
         return evade;
     }
     public void setEvade(int evade){
         this.evade = evade;
     }
     
     public int getPrice(){
         return price;
     }
     public void setPrice(int price){
         this.price = price;
     }
     
     public int getGenes(){
         return genes;
     }
     public void setGenes(int genes){
         this.genes = genes;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Monster)) {
             return false;
         }
         Monster other = (Monster) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "database.Monster[ id=" + id + " ]";
     }
     
    public Monster generateRandom(){
         Monster m = new Monster();
         Random random = new Random();
         m.setStrength(random.nextInt(11));
         m.setHealth(random.nextInt(11));
         m.setEvade(random.nextInt(11));
         m.setGenes(random.nextInt(11));
         m.setPrice(m.getStrength()+m.getEvade()+m.getHealth()+m.getGenes());
         m.setName(JOptionPane().showInputDialog(null, "How would you like to name your monster?"));
        m.setOwner();
         return m;
     }
     
 }
