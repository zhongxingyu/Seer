 package specs.junit.lonewolf.core.combat.combatmanager;
 
 import com.hoffenkloffen.lonewolf.core.combat.CombatResultList;
 import com.hoffenkloffen.lonewolf.core.combat.Enemy;
 import org.junit.Test;
 
 import static org.junit.Assert.assertNotNull;
 
 public class When_fight_1 extends Given_CombatManager {
 
     protected void given() {
         super.given();
 
         combat.add(new Enemy("Enemy1", 1, 1));
         combat.add(new Enemy("Enemy2", 2, 2));
     }
 
     protected void when() {
         manager.fight("0");
         manager.fight("1");
     }
 
     @Test
    public void then_the_section_state_should_contain_a_CombatResultList_with_two_values() {
         CombatResultList state = sectionManager.getCurrent().getState(CombatResultList.class);
         assertNotNull(state);
         assertNotNull(state.get(1));
     }
 }
