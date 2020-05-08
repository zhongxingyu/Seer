 package cards;
 
 import static org.junit.Assert.*;
 import gamePlay.Game;
 
import org.junit.After;
import org.junit.AfterClass;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 
 import player.Player;
 
 import character.Character;
 
 public class AttackPairTest {
 
 	AttackPair ap;
 	Base base;
 	Style style;
 	Character player, opponent;
 
 	private class TestBase extends Base {
 
 		public TestBase(String name, int minRange, int maxRange, int power,
 				int priority) {
 			super(name, minRange, maxRange, power, priority);
 		}
 
 		public void onReveal(Character player, Character opponent) {
 			player.addPriorityBonusNextBeat(1);
 			opponent.addPriorityBonusNextBeat(1);
 		}
 		
 		public void startOfBeat(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(2);
 			opponent.addPriorityBonusNextBeat(2);
 		};
 
 		public void beforeActivating(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(3);
 			opponent.addPriorityBonusNextBeat(3);
 		};
 
 		public void onHit(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(4);
 			opponent.addPriorityBonusNextBeat(4);
 		};
 
 		public void onDamage(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(5);
 			opponent.addPriorityBonusNextBeat(5);
 		};
 
 		public void afterActivating(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(6);
 			opponent.addPriorityBonusNextBeat(6);
 		};
 
 		public void endOfBeat(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(7);
 			opponent.addPriorityBonusNextBeat(7);
 		};
 	}
 	
 	private class TestStyle extends Style {
 
 		public TestStyle(String name, int minRange, int maxRange, int power,
 				int priority) {
 			super(name, minRange, maxRange, power, priority);
 		}
 
 		public void onReveal(Character player, Character opponent) {
 			player.addPriorityBonusNextBeat(10);
 			opponent.addPriorityBonusNextBeat(-10);
 		}
 		
 		public void startOfBeat(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(20);
 			opponent.addPriorityBonusNextBeat(-20);
 		};
 
 		public void beforeActivating(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(30);
 			opponent.addPriorityBonusNextBeat(-30);
 		};
 
 		public void onHit(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(40);
 			opponent.addPriorityBonusNextBeat(-40);
 		};
 
 		public void onDamage(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(50);
 			opponent.addPriorityBonusNextBeat(-50);
 		};
 
 		public void afterActivating(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(60);
 			opponent.addPriorityBonusNextBeat(-60);
 		};
 
 		public void endOfBeat(Character player, Character opponent){
 			player.addPriorityBonusNextBeat(70);
 			opponent.addPriorityBonusNextBeat(-70);
 		};
 	}
 	
 	private class TestCharacter extends Character {
 
 		public TestCharacter() {
 			super(null, null);
 		}
 
 		@Override
 		public Character clone(Player player, Game game) {
 			return null;
 		}
 
 		@Override
 		public Card createSpecial() {
 			return null;
 		}
 
 		@Override
 		public void setStyles() {
 		}
 
 		@Override
 		public boolean anteTokens() {
 			return false;
 		}
 
 		@Override
 		public Style getFirstStyle() {
 			return null;
 		}
 
 		@Override
 		public Style getSecondStyle() {
 			return null;
 		}
 		
 	}
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		base = new TestBase("TestBase", -1, 2, 3, 4);
 		style = new TestStyle("TestStyle", 0, 1, 2, 3);
 		ap = new AttackPair(base, style);
 		player = new TestCharacter();
 		opponent = new TestCharacter(); 
 	}
 
 
 	@Test
 	public void testGetBase() {
 		assertEquals("Should have gotten the base back", base, ap.getBase());
 	}
 	
 	@Test
 	public void testGetStyle() {
 		assertEquals("Should have gotten the style back", style, ap.getStyle());
 	}
 	
 	@Test
 	public void testOnReveal() {
 		ap.onReveal(player, opponent);
 		assertEquals("Player wasn't affected properly", 11, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -9, opponent.getPriorityModifierNextBeat());
 	}
 	
 	@Test
 	public void testStartOfBeat(){
 		ap.startOfBeat(player, opponent);
 		assertEquals("Player wasn't affected properly", 22, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -18, opponent.getPriorityModifierNextBeat());
 	};
 	
 	@Test
 	public void testBeforeActivating(){
 		ap.beforeActivating(player, opponent);
 		assertEquals("Player wasn't affected properly", 33, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -27, opponent.getPriorityModifierNextBeat());
 	};
 
 	@Test
 	public void testOnHit(){
 		ap.onHit(player, opponent);
 		assertEquals("Player wasn't affected properly", 44, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -36, opponent.getPriorityModifierNextBeat());
 	};
 
 	@Test
 	public void testOnDamage(){
 		ap.onDamage(player, opponent);
 		assertEquals("Player wasn't affected properly", 55, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -45, opponent.getPriorityModifierNextBeat());
 	};
 
 	@Test
 	public void testAfterActivating(){
 		ap.afterActivating(player, opponent);
 		assertEquals("Player wasn't affected properly", 66, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -54, opponent.getPriorityModifierNextBeat());
 	};
 
 	@Test
 	public void testEndOfBeat(){
 		ap.endOfBeat(player, opponent);
 		assertEquals("Player wasn't affected properly", 77, player.getPriorityModifierNextBeat());
 		assertEquals("Opponent wasn't affected properly", -63, opponent.getPriorityModifierNextBeat());
 	};
 	
 	@Test
 	public void testToString() {
		assertEquals("toString() returns wrong input", ap.name + ": " + ap.minRange + "/" + ap.maxRange + ", " + ap.power + ", " + ap.priority, ap.toString());
 	}
 }
