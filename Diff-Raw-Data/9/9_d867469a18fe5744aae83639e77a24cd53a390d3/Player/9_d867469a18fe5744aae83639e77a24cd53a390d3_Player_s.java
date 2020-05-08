 package zombiefu.player;
 
 import jade.core.World;
 import zombiefu.items.Weapon;
 import jade.fov.ViewField;
 import jade.ui.Camera;
 import jade.util.Guard;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Direction;
 import java.util.ArrayList;
 import java.util.HashMap;
 import zombiefu.creature.Creature;
 import zombiefu.actor.Door;
 import zombiefu.creature.Monster;
 import zombiefu.actor.NotPassableActor;
 import zombiefu.creature.AttributeSet;
 import zombiefu.exception.CannnotMoveToNonPassableActorException;
 import zombiefu.exception.CannotAffordException;
 import zombiefu.exception.CannotAttackWithoutMeleeWeaponException;
 import zombiefu.exception.CannotMoveToIllegalFieldException;
 import zombiefu.exception.WeaponHasNoMunitionException;
 import zombiefu.exception.CannotBeConsumedException;
 import zombiefu.exception.NoDirectionGivenException;
 import zombiefu.exception.DoesNotPossessThisItemException;
 import zombiefu.exception.MaximumHealthPointException;
 import zombiefu.exception.NoEnemyHitException;
 import zombiefu.fov.SquareRayCaster;
 import zombiefu.fov.ViewEverything;
 import zombiefu.human.Human;
 import zombiefu.items.ConsumableItem;
 import zombiefu.items.Item;
 import zombiefu.level.Level;
 import zombiefu.util.ZombieGame;
 import zombiefu.util.ZombieTools;
 import zombiefu.util.Action;
 
 public class Player extends Creature implements Camera {
 
     private final static int ECTS_FOR_NEXT_SEMESTER = 30;
     private final static ViewField DEFAULT_VIEWFIELD = new SquareRayCaster();
     private int money;
     private int ects;
     private int semester;
     private HashMap<String, ArrayList<ConsumableItem>> inventar;
     private HashMap<String, Weapon> weapons;
     private ArrayList<String> weaponsList;
 
     public Player(ColoredChar face, String name, Discipline discipline, AttributeSet attr) {
 
         super(face, name, attr);
 
        this.godMode = true;
         this.money = 0;
         this.ects = 0;
         this.semester = 1;
         this.discipline = discipline;
 
         this.inventar = new HashMap<>();
         this.weapons = new HashMap<>();
         this.weaponsList = new ArrayList<>();
 
         this.sichtweite = 20;
         this.fov = DEFAULT_VIEWFIELD;
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
 
     public HashMap<String, ArrayList<ConsumableItem>> getInventar() {
         return inventar;
     }
 
     @Override
     public void pleaseAct() {
         try {
             char key = ZombieGame.askPlayerForKey();
 
             // Diese Keys haben noch keine Property sie sind fürs Debuggen und
             // werden später abgeschaltet. FIX (macht sgs)
             if (key == 'f') {
                 if (fov == DEFAULT_VIEWFIELD) {
                     fov = new ViewEverything();
                 } else {
                     fov = DEFAULT_VIEWFIELD;
                 }
                 ZombieGame.refreshMainFrame();
                 act();
             }
 
             if (key == 27) {
                 System.exit(0);
             }
 
             if (key == 'g') {
                 godMode = !godMode;
                 ZombieGame.refreshBottomFrame();
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
                         String it = ZombieGame.askPlayerForItemInInventar();
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
 
                     case HELP:
                         ZombieGame.showHelp();
                         act();
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
 
 
                 }
             } else {
                 act();
             }
         } catch (CannotMoveToIllegalFieldException ex) {
             act();
         } catch (WeaponHasNoMunitionException ex) {
             ZombieGame.newMessage("Du hast keine Munition für " + getActiveWeapon().getName());
             act();
         } catch (CannotAttackWithoutMeleeWeaponException ex) {
             ZombieGame.newMessage("Du trägst keine Nahkampfwaffe!");
             act();
         } catch (CannnotMoveToNonPassableActorException ex) {
             NotPassableActor actor = ex.getActor();
             if (actor instanceof Human) {
                 ((Human) actor).talkToPlayer(this);
             } else if (actor instanceof Door) {
                 if (isGod()) {
                     ((Door) actor).open();
                     ZombieGame.newMessage("Die Tür wurde mit göttlicher Macht geöffnet.");
                 } else {
                     ZombieGame.newMessage("Diese Tür ist geschlossen. Du brauchst einen Schlüssel um sie zu öffnen");
                     act();
                 }
             }
         } catch (NoEnemyHitException ex) {
             ZombieGame.newMessage("Du hast verfehlt!");
             ex.close();
         }
         if (!getActiveWeapon().hasMunition()) {
             removeWeapon(getActiveWeapon().getName());
         }
     }
 
     public void switchWeapon(boolean backwards) {
         if (backwards) {
             String tmp = weaponsList.remove(weapons.size() - 1);
             weaponsList.add(0, tmp);
         } else {
             String tmp = weaponsList.remove(0);
             weaponsList.add(tmp);
         }
     }
 
     public void changeWorld(World world) {
 
         Guard.verifyState(world instanceof Level);
         Level lvl = (Level) world;
 
         if (bound()) {
             world().removeActor(this);
         }
         lvl.addActor(this);
         if (lvl.isGlobalMap()) {
             fov = new ViewEverything();
         } else {
             fov = DEFAULT_VIEWFIELD;
             lvl.refill();
         }
     }
 
     public void obtainItem(Item i) {
         if (i instanceof Weapon) {
             Weapon w = (Weapon) i;
             if (weaponsList.contains(w.getName())) {
                 weapons.get(w.getName()).addMunition(w.getMunition());
                 w.expire();
             } else {
                 weapons.put(w.getName(), w);
                 weaponsList.add(w.getName());
             }
         } else if (i instanceof ConsumableItem) {
             ConsumableItem c = (ConsumableItem) i;
             if (!inventar.containsKey(i.getName())) {
                 inventar.put(i.getName(), new ArrayList<ConsumableItem>());
             }
             inventar.get(i.getName()).add(c);
         } else {
             throw new IllegalStateException(
                     "Items should either be Weapons or consumable.");
         }
     }
 
     public ConsumableItem removeConsumableItemFromInventar(String s) throws DoesNotPossessThisItemException {
         if (inventar.containsKey(s) && !inventar.get(s).isEmpty()) {
             return inventar.get(s).remove(0);
         } else {
             throw new DoesNotPossessThisItemException();
         }
     }
 
     public void removeWeapon(String itemName) {
         if (weaponsList.contains(itemName)) {
             weaponsList.remove(itemName);
             weapons.remove(itemName);
         }
     }
 
     public Item removeItem(String itemName) throws DoesNotPossessThisItemException {
         if (weaponsList.contains(itemName)) {
             weaponsList.remove(itemName);
             return weapons.remove(itemName);
         } else {
             return removeConsumableItemFromInventar(itemName);
         }
     }
 
     @Override
     public Weapon getActiveWeapon() {
         return weapons.get(weaponsList.get(0));
     }
 
     private void consumeItem(String itemName) {
         ZombieGame.newMessage("Du benutzt '" + itemName + "'.");
         ConsumableItem it;
         try {
             it = removeConsumableItemFromInventar(itemName);
             try {
                 it.getConsumedBy(this);
                 it.expire();
             } catch (CannotBeConsumedException ex) {
                 obtainItem(it);
             }
         } catch (DoesNotPossessThisItemException ex) {
         }
         ZombieGame.refreshBottomFrame();
     }
 
     @Override
     public void kill(Creature killer) {
         ZombieGame.endGame();
     }
 
     public void addMoney(int m) {
         this.money += m;
     }
 
     public void pay(int m) throws CannotAffordException {
         if (m > money) {
             throw new CannotAffordException();
         } else {
             this.money -= m;
             ZombieGame.refreshBottomFrame();
         }
 
     }
 
     @Override
     protected Direction getAttackDirection() throws NoDirectionGivenException {
         return ZombieGame.askPlayerForDirection();
     }
 
     public void giveECTS(int e) {
         ects += e;
 
         while (ects >= semester * ECTS_FOR_NEXT_SEMESTER) {
             levelUp();
         }
     }
 
     public void levelUp() {
         semester += 1;
         ZombieGame.refreshBottomFrame();
         Attribute att = ZombieGame.askPlayerForAttrbuteToRaise();
         attributSet.put(att, attributSet.get(att) + att.getStep());
         ZombieGame.refreshBottomFrame();
     }
 
     public void heal(int i) throws MaximumHealthPointException {
         ZombieTools.log(getName() + " hat " + i + " HP geheilt. ");
         if (healthPoints == attributSet.get(Attribute.MAXHP)) {
             throw new MaximumHealthPointException();
         }
         healthPoints += i;
         if (healthPoints >= attributSet.get(Attribute.MAXHP)) {
             healthPoints = attributSet.get(Attribute.MAXHP);
         }
     }
 
     @Override
     protected boolean isEnemy(Creature enemy) {
         return enemy instanceof Monster;
     }
 
     @Override
     public boolean hasUnlimitedMunition() {
         return isGod();
     }
 }
