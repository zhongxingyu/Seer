 package zombiefu.creature;
 
 import jade.core.World;
 import zombiefu.items.Waffe;
 import jade.fov.RayCaster;
 import jade.ui.Camera;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Direction;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import zombiefu.fov.ViewEverything;
 import zombiefu.items.CannotBeConsumedException;
 import zombiefu.items.ConsumableItem;
 import zombiefu.items.Item;
 import zombiefu.level.Level;
 import zombiefu.util.NoDirectionGivenException;
 import zombiefu.util.ZombieGame;
 import zombiefu.util.ZombieTools;
 import zombiefu.util.Action;
 import zombiefu.util.ConfigHelper;
 
 public class Player extends Creature implements Camera {
 
     private int intelligenceValue;
     private int money;
     private int ects;
     private int semester;
     private int maximalHealthPoints;
     private ArrayList<ConsumableItem> inventar;
     private HashMap<String, Waffe> waffen;
     private ArrayList<String> waffenListe;
 
     public Player(ColoredChar face, String name, int healthPoints,
             int attackValue, int defenseValue, int intelligenceValue,
             ArrayList<String> waffe) {
 
         super(face, name, healthPoints, attackValue, defenseValue);
 
         this.maximalHealthPoints = healthPoints;
         this.intelligenceValue = intelligenceValue;
         this.godMode = true;
         this.money = 10;
         this.ects = 0;
         this.semester = 1;
 
         this.inventar = new ArrayList<ConsumableItem>();
         this.waffen = new HashMap<String, Waffe>();
         this.waffenListe = new ArrayList<String>();
         for (String wName : waffe) {
             waffen.put(wName, ConfigHelper.newWaffeByName(wName));
             waffenListe.add(wName);
         }
 
         this.sichtweite = 20;
         this.fov = new RayCaster();
     }
 
     public int getSemester() {
         return semester;
     }
 
     public int getECTS() {
         return ects;
     }
 
     public int getMoney() {
         return money;
     }
 
     public int getMaximalHealthPoints() {
         return maximalHealthPoints;
     }
 
     public int getIntelligenceValue() {
         return intelligenceValue;
     }
 
     public ArrayList<ConsumableItem> getInventar() {
         return inventar;
     }
 
     @Override
     public void act() {
         try {
             char key = ZombieGame.askPlayerForKey();
 
             // Diese Keys haben noch keine Property sie sind fürs Debuggen und
             // werden später abgeschaltet. FIX (macht sgs)
             if (key == 'f') {
                 if (fov instanceof RayCaster) {
                     fov = new ViewEverything();
                 } else {
                     fov = new RayCaster();
                 }
                 ZombieGame.refreshMainFrame();
                 act();
             }
 
             if (key == 27) {
                 System.exit(0);
             }
 
             if (key == 'g') {
                 godMode = !godMode;
                 act();
             }
 
             Action action = ZombieTools.keyToAction(ZombieGame.getSettings().keybindings, key);
 
             if (action != null) {
 
                 switch (action) {
 
                     case PREV_WEAPON:
                         switchWeapon(true);
                         ZombieGame.refreshBottomFrame();
                         act();
                         break;
 
                     case NEXT_WEAPON:
                         switchWeapon(false);
                         ZombieGame.refreshBottomFrame();
                         act();
                         break;
 
                     case INVENTORY:
                         ConsumableItem it = ZombieGame.askPlayerForItem();
                         if (it == null) {
                             act();
                         } else {
                             consumeItem(it);
                         }
                         break;
 
                     case ATTACK:
                         try {
                             attack();
                         } catch (NoDirectionGivenException ex) {
                             act();
                         }
                         break;
 
                     case UP:
                         tryToMove(Direction.NORTH);
                         break;
 
                     case DOWN:
                         tryToMove(Direction.SOUTH);
                         break;
 
                     case LEFT:
                         tryToMove(Direction.WEST);
                         break;
 
                     case RIGHT:
                         tryToMove(Direction.EAST);
                         break;
 
                    case NOOP:
                         break;
                 }
            } else {
                act();
             }
         } catch (InterruptedException e) {
         } catch (CannotMoveToIllegalFieldException ex) {
             act();
         } catch (WeaponHasNoMunitionException ex) {
             ZombieGame.newMessage("Du hast keine Munition für " + getActiveWeapon().getName());
             act();
         }
     }
 
     public void switchWeapon(boolean backwards) {
         if (backwards) {
             String tmp = waffenListe.remove(waffen.size() - 1);
             waffenListe.add(0, tmp);
         } else {
             String tmp = waffenListe.remove(0);
             waffenListe.add(tmp);
         }
     }
 
     public void changeWorld(World world) {
         world().removeActor(this);
         world.addActor(this);
         ((Level) world).fillWithEnemies();
     }
 
     public void obtainItem(Item i) {
         if (i instanceof Waffe) {
             Waffe w = (Waffe) i;
             if (waffenListe.contains(w.getName())) {
                 waffen.get(w.getName()).addMunition(w.getMunition());
                 w.expire();
             } else {
                 waffen.put(w.getName(), w);
                 waffenListe.add(w.getName());
             }
         } else if (i instanceof ConsumableItem) {
             inventar.add((ConsumableItem) i);
         } else {
             throw new IllegalStateException(
                     "Items should either be Weapons or consumable.");
         }
     }
 
     @Override
     public Waffe getActiveWeapon() {
         return waffen.get(waffenListe.get(0));
     }
 
     public void heal(int i) throws MaximumHealthPointException {
         System.out.print(getName() + " hat " + i + " HP geheilt. ");
         if (healthPoints == maximalHealthPoints) {
             throw new MaximumHealthPointException();
         }
         healthPoints += i;
         if (healthPoints >= maximalHealthPoints) {
             healthPoints = maximalHealthPoints;
         }
         System.out.println("HP: " + healthPoints);
     }
 
     private void consumeItem(ConsumableItem it) {
         ZombieGame.newMessage("Du benutzt '" + it.getName() + "'.");
         System.out.println(getName() + " benutzt Item " + it.getName());
         try {
             it.getConsumedBy(this);
             inventar.remove(it);
             it.expire();
         } catch (CannotBeConsumedException ex) {
         }
         ZombieGame.refreshBottomFrame();
     }
 
     @Override
     protected void killed(Creature killer) {
         ZombieGame.endGame();
     }
 
     public void addMoney(int m) {
         this.money += m;
     }
 
     @Override
     protected Direction getAttackDirection() throws NoDirectionGivenException {
         return ZombieGame.askPlayerForDirection();
     }
 }
