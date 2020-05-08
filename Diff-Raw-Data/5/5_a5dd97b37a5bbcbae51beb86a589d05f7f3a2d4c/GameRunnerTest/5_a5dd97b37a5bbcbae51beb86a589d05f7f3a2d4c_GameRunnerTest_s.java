 package com.adaptionsoft.games.trivia.runner;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.Before;
 import com.adaptionsoft.games.uglytrivia.Game;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Random;
 import java.util.zip.CRC32;
 import java.util.zip.Checksum;
 import java.io.ByteArrayOutputStream;
 
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
 	}
 	
 	@Test
 	public void testMain() {
 		String[] listaDeJugadores = {"Chet","Pat","Sue"};	
 		game = GameRunner.initialize(listaDeJugadores);
 		
 		GameRunner.run(game, rand);	
		assertEquals(1763398543, checker.checksum.getValue());
 	}
 	
 	@Test
 	public void testSixPlayers() {
 		String[] listaDeJugadores = {"Chet","Pat","Sue","Joe","Tim","Tom"};	
 		game = GameRunner.initialize(listaDeJugadores);
 		
 		GameRunner.run(game, rand);	
		assertEquals(1512821208, checker.checksum.getValue());
 	}
 	
 	@Test
 	public void testAnswerWasCorrectMessage()
 	{	
 		Game game = new Game();
 		assertEquals("Answer was correct!!!!", game.messageAnswerWasCorrect());
 	}
 }
