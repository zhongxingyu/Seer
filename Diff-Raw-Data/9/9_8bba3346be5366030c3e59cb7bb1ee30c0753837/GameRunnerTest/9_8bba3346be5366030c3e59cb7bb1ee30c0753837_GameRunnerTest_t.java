 package com.adaptionsoft.games.trivia.runner;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Random;
 import java.util.zip.CRC32;
 import java.util.zip.Checksum;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.adaptionsoft.games.uglytrivia.Game;
 
 public class GameRunnerTest {
 
 	Checker checker;
 	Random rand;
 	Game game;
 	
 	class Checker extends OutputStream {
 		Checksum checksum = new CRC32();
 		
 		public void write(int b) throws IOException {
 			checksum.update(b);
 		}
 	}
 
 	@Before
 	public void setUp() {
 		checker = new Checker();
 		System.setOut(new PrintStream(checker));
 		rand = new Random(0L);
 		game = new Game();
 		game.add("Chet");
 		game.add("Pat");
 		game.add("Sue");
 	}
 	
 	@Test
 	public void testMain() {
 		GameRunner.run(game, rand);	
 		assertEquals(590124755L, checker.checksum.getValue());
 		
 		GameRunner.run(game, rand);
 		assertEquals(220049483L, checker.checksum.getValue());
 		
 		GameRunner.run(game, rand);
 		assertEquals(2318374383L, checker.checksum.getValue());
 	}
 
 	@Test
 	public void testMainWithSixPlayers() {
 		game.add("John");
 		game.add("Mike");
 		game.add("Mary");
 		GameRunner.run(game, rand);
		assertEquals(2937927022L, checker.checksum.getValue());
 	}
 }
 
 
