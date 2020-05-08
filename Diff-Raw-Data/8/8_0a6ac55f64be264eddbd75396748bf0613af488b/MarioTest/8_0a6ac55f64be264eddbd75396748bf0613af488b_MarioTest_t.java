 package edu.ucsc.eis.mario.sprites;
 
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 import javax.imageio.ImageIO;
 
 import org.drools.base.RuleNameEqualsAgendaFilter;
 import org.drools.logger.KnowledgeRuntimeLoggerFactory;
 import org.drools.runtime.conf.ClockTypeOption;
 import org.drools.spi.AgendaFilter;
 import org.drools.runtime.rule.FactHandle;
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.runtime.KnowledgeSessionConfiguration;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.StatelessKnowledgeSession;
 import org.drools.time.SessionPseudoClock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mojang.sonar.FakeSoundEngine;
 import com.mojang.sonar.SonarSoundEngine;
 import com.mojang.sonar.SoundSource;
 import com.mojang.sonar.sample.SonarSample;
 
 import static org.junit.Assert.*;
 
 
 import edu.ucsc.eis.mario.events.Jump;
 import edu.ucsc.eis.mario.events.Landing;
 import edu.ucsc.eis.mario.level.LevelGenerator;
 import edu.ucsc.eis.mario.level.Pit;
 import edu.ucsc.eis.mario.*;
 import static org.mockito.Mockito.*;
 
 /**
  * These unit tests should fail when the rule engine is disabled.
  * When it is turned on, they should pass.
  * Probably need some qualitative evaluation (80K students? Web deployment?).
  * 
  * The Required Action not possible bug is the sort of thing we need
  * Drools Solver for... if there is no solution (eg. can't get over a jump),
  * then we fail.
  * @author cflewis
  *
  */
 public class MarioTest extends MarioRulesTest {
 	public MarioTest() {
 		rulesEnabled = false;
 	}
 	
 	@Test
 	public void testSetup() {
         assertFalse(Mario.large);
         assertFalse(Mario.fire);
         assertTrue(Mario.coins == 0);
         assertTrue(Mario.lives == 3);        
 	}
 	
 	/**
 	 * This satisfies the "Animation in wrong context" bug
 	 */
 	@Test
 	public void testAnimationSheet() {
 		assertTrue(mario.sheet == Art.smallMario);
 		testMushroom();
 		assertTrue(mario.sheet == Art.mario);
 		testFlower();
 		assertTrue(mario.sheet == Art.fireMario);
 	}
 	
 	@Test 
 	public void testMushroom() {
 		Mario.large = false;
 		Mario.fire = false;
 		assertFalse(Mario.large);
 		assertFalse(Mario.fire);
 				
 		mario.getMushroom();
 		tickScene(1000);
 		
 		assertTrue(Mario.large);
 		assertFalse(Mario.fire);
 	}
 	
 	@Test
 	public void testFlower() {
 		Mario.large = false;
 		Mario.fire = false;
 		assertFalse(Mario.large);
 		assertFalse(Mario.fire);
 				
 		mario.getFlower();
 		tickScene(1000);
 		
 		assertTrue(Mario.large);
 		assertTrue(Mario.fire);
 	}
 	
 	/**
 	 * This is an invalid position over time test.
 	 * Mario's normal jump lands after a second.
 	 * The longest jump time I've managed is less than two seconds.
 	 */
 	@Test
 	public void testJump() {
 		mario.keys[Mario.KEY_JUMP] = true;
 		tickScene(1);
 		assertTrue(mario.getJumpTime() > 0);
 		tickScene(2);
 		assertTrue(mario.getJumpTime() > 0);
 		tickScene(22);
 		assertTrue(mario.getJumpTime() <= 0);
 	}
 	
 	@Test
 	public void testCoinValue() {
 		//TODO: Implement this
 	}
 	
 	@Test
 	public void testDuck() {
 		Mario.large = false;
 		assertFalse(Mario.large);
 		tickScene(1);
 		
 		mario.keys[Mario.KEY_DOWN] = true;
 		tickScene(1);
 		
 		assertFalse(mario.isDucking());
 		
 		Mario.large = true;
 		assertTrue(Mario.large);
 		tickScene(1);
 		assertTrue(mario.isDucking());
 	}
 }
