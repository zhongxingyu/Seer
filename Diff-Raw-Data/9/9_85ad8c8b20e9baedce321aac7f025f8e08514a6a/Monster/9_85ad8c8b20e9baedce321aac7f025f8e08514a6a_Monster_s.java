 package net.sf.laja.cdd.behaviour.monster;
 
 import net.sf.laja.cdd.behaviour.terrestrial.Terrestrial;
 import net.sf.laja.cdd.behaviour.head.Head;
 import net.sf.laja.cdd.state.monster.MonsterStateBuilder;
 import net.sf.laja.cdd.state.monster.MonsterState;
 
 public class Monster extends MonsterFactory {
     private final Head head;
 
     public Monster(MonsterState state, MonsterStateBuilder stateBuilder) {
         super(state, stateBuilder);
 
         head = new Head(state, stateBuilder);
     }
 
     // (factory)
     public Monster asMonster() {
         return new Monster(state, stateBuilder);
     }
 
     public Terrestrial asTerrestrial() {
        return new Terrestrial(state.getTerrestrial(), stateBuilder.terrestrialStateBuilder());
     }
 }
