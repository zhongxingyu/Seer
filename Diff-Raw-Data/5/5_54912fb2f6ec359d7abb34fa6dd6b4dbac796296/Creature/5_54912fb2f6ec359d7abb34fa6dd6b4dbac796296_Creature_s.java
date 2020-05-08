 package zombiefu.creature;
 
 import jade.core.Actor;
 import jade.fov.ViewField;
 import jade.util.Dice;
 import jade.util.Guard;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Coordinate;
 import jade.util.datatype.Direction;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import zombiefu.items.Waffe;
 import zombiefu.items.Waffentyp;
 import zombiefu.util.DamageAnimation;
 import zombiefu.util.NoDirectionGivenException;
 import zombiefu.util.ZombieGame;
 import zombiefu.util.ZombieTools;
 
 public abstract class Creature extends NotPassableActor {
 
     protected int healthPoints;
     protected int attackValue;
     protected int defenseValue;
     private int dazed;
     protected String name;
     protected ViewField fov;
     protected int sichtweite;
     protected boolean godMode;
 
     public Creature(ColoredChar face, String n, int h, int a, int d) {
         super(face);
         dazed = 0;
         name = n;
         healthPoints = h;
         attackValue = a;
         defenseValue = d;
     }
 
     public int getAttackValue() {
         return attackValue;
     }
 
     public int getDefenseValue() {
         return defenseValue;
     }
 
     public int getHealthPoints() {
         return healthPoints;
     }
 
     public Collection<Coordinate> getViewField() {
         return fov.getViewField(world(), pos(), sichtweite);
     }
 
     public Creature(ColoredChar face, String name) {
         this(face, name, 1, 1, 1);
     }
 
     public Creature(ColoredChar face) {
         this(face, "Zombie");
     }
 
     public abstract Waffe getActiveWeapon();
 
     @Override
     public void setPos(int x, int y) {
         if (world().passableAt(x, y)) {
             super.setPos(x, y);
         }
     }
 
     public String getName() {
         return name;
     }
 
     protected abstract Direction getAttackDirection()
             throws NoDirectionGivenException;
 
     public void attackCreature(Creature cr) {
         if (this.equals(cr)) {
             return;
         }
 
         // Wer keine Waffe hat, kann nicht angreifen!
         if (getActiveWeapon() == null) {
             return;
         }
 
         // Monster greifen keine Monster an!
         if (this instanceof Monster && cr instanceof Monster) {
             return;
         }
 
         System.out.println(getName() + " attacks " + cr.getName() + " with "
                 + getActiveWeapon().getName() + " (Damage: "
                 + getActiveWeapon().getDamage() + "). Attack value: "
                 + attackValue + ", Defense Value: " + cr.defenseValue);
 
         // Calculate damage
         int damage = (int) (((double) getActiveWeapon().getDamage())
                 * ((double) attackValue / (double) cr.defenseValue)
                 * (double) Dice.global.nextInt(20, 40) / 30);
         if (damage == 0) {
             damage = 1;
         }
 
         ZombieGame.newMessage(getName() + " hat " + cr.getName() + " " + damage
                 + " Schadenspunkte hinzugefügt.");
 
         cr.hurt(damage, this);
     }
 
     public void attackCoordinate(Coordinate coord) {
         Guard.argumentIsNotNull(coord);
         DamageAnimation anim = new DamageAnimation();
         world().addActor(anim, coord);
         Collection<Creature> actors = world()
                 .getActorsAt(Creature.class, coord);
         if (actors.isEmpty()) {
             ZombieGame.newMessage("Niemanden getroffen!");
         } else {
             Iterator<Creature> it = actors.iterator();
             while (it.hasNext()) {
                 attackCreature(it.next());
             }
         }
         world().removeActor(anim);
         anim.expire();
     }
 
     private void createDetonation(Coordinate c, double blastRadius,
             boolean includeCenter) {
         // TODO: Verschönern (mit RayCaster)
         Collection<Creature> targets = new HashSet<Creature>();
         Collection<DamageAnimation> anims = new HashSet<DamageAnimation>();
         int blastMax = (int) Math.ceil(blastRadius);
         for (int x = Math.max(0, c.x() - blastMax); x <= Math.min(c.x()
                 + blastMax, world().width() - 1); x++) {
             for (int y = Math.max(0, c.y() - blastMax); y <= Math.min(c.y()
                     + blastMax, world().height() - 1); y++) {
                 Coordinate neu = new Coordinate(x, y);
                 if (neu.distance(c) <= blastRadius
                         && (includeCenter || !c.equals(neu))) {
                     DamageAnimation anim = new DamageAnimation();
                     anims.add(anim);
                     world().addActor(anim, neu);
                     Collection<Creature> actors = world().getActorsAt(
                             Creature.class, neu);
                     Iterator<Creature> it = actors.iterator();
                     while (it.hasNext()) {
                         Creature next = it.next();
                         if (!equals(next)) {
                             targets.add(next);
                         }
                     }
                 }
             }
         }
         if (targets.isEmpty()) {
             ZombieGame.newMessage("Niemanden getroffen!");
         } else {
             for (Creature target : targets) {
                 attackCreature(target);
             }
         }
         for (DamageAnimation anim : anims) {
             world().removeActor(anim);
             anim.expire();
         }
 
     }
 
     private Coordinate findTargetInDirection(Direction dir, int maxDistance) {
         Coordinate nPos = pos();
         int dcounter = 0;
         do {
             nPos = nPos.getTranslated(dir);
             if (!world().insideBounds(nPos) || !world().passableAt(nPos)) {
                 return nPos
                         .getTranslated(ZombieTools.getReversedDirection(dir));
             }
             dcounter++;
         } while (world().getActorsAt(Creature.class, nPos).isEmpty()
                 && dcounter < maxDistance);
         return nPos;
     }
 
     public void attack(Direction dir) {
         Waffentyp typ = getActiveWeapon().getTyp();
         Coordinate ziel;
         if (typ.isRanged()) {
             ziel = findTargetInDirection(dir, getActiveWeapon().getRange());
         } else {
             ziel = pos().getTranslated(dir);
         }
         if (typ.isDirected()) {
             attackCoordinate(ziel);
         } else {
             createDetonation(ziel, getActiveWeapon().getBlastRadius(),
                     typ.isRanged());
         }
     }
 
     public void attack() throws NoDirectionGivenException {
         Direction dir;
         if (getActiveWeapon().getTyp() != Waffentyp.UMKREIS) {
             dir = getAttackDirection();
         } else {
             dir = Direction.ORIGIN;
         }
         attack(dir);
     }
 
     public void tryToMove(Direction dir)
             throws CannotMoveToIllegalFieldException {
         Guard.argumentIsNotNull(world());
         Guard.argumentIsNotNull(dir);
         if (dazed > 0) {
             dazed--;
             return;
         }
         if (dir == Direction.ORIGIN) {
             return;
         }
         Coordinate targetField = pos().getTranslated(dir);
         if (!world().insideBounds(targetField)
                 || !world().passableAt(targetField)) {
             throw new CannotMoveToIllegalFieldException();
         }
         NotPassableActor actor = world().getActorAt(NotPassableActor.class,
                 pos().getTranslated(dir));
         if (actor == null) {
             move(dir);
        } else if (actor instanceof NotPassableActor && !(actor instanceof Player) && this instanceof Monster) {
             throw new CannotMoveToIllegalFieldException();
        } else if (actor instanceof NotPassableActor && !(actor instanceof Creature) && this instanceof Player) {
             throw new CannotMoveToIllegalFieldException();
         } else if (getActiveWeapon().getTyp() == Waffentyp.NAHKAMPF) {
             attack(dir);
         } else {
             throw new CannotMoveToIllegalFieldException();
         }
     }
 
     protected abstract void killed(Creature killer);
 
     private void hurt(int i, Creature hurter) {
         System.out.println(getName() + " hat " + i + " HP verloren. ");
         if (godMode) {
             return;
         }
         if (i >= healthPoints) {
             killed(hurter);
         } else {
             healthPoints -= i;
         }
     }
 }
