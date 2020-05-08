 /*******************************************************************************
  * Copyright (c) 2012 BragiSoft, Inc.
  * This source is subject to the BragiSoft Permissive License.
  * Please see the License.txt file for more information.
  * All other rights reserved.
  * 
  * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY 
  * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
  * PARTICULAR PURPOSE.
  * 
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * Contributors:
  * Jan-Christoph Klie - Everything
  * 
  *******************************************************************************/
 
 package com.bragi.sonify.composer.riffology;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Sequence;
 
 import junit.framework.TestCase;
 import junitx.framework.FileAssert;
 
 import com.bragi.sonify.composer.AComposer;
 
 /**
  * This unit test is home of the tests which are described in chapter 10 of the
  * functional specifications document. (As far as software tests can achieve it).
  */
 public class TestScenarioTest extends TestCase {
 	
 	private File getTestFile(String name) {
 		URL url = this.getClass().getResource("/literature/" + name);
 		return new File(url.getFile());
 	}
 	
 	private final File drama = getTestFile("drama.txt");
 	private final File kidsBook = getTestFile("kinderbuch.txt");
 	private final File lyric = getTestFile("lyrik.txt");
 	private final File novel = getTestFile("roman.txt");
 	private final File nonFiction = getTestFile("sachbuch.txt");
 	
 	private static final long THIRTY_SECONDS = 30000;
 	private static final long TEN_MINUTES =  600000000;
 	
 	private List<File> tempFiles;
 	
 	public TestScenarioTest(String testName) {
 		super(testName);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		tempFiles = new LinkedList<File>();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		for( File f : tempFiles ) {
 			f.delete();
 		}
 	}
 	
 	/**
 	 * /TS0010/ Generating MIDI
 	 * 
 	 * This tests uses every implementation of the AComposer, generates a MIDI file
 	 * and saves it afterwards. It is then imported with the MidiFileReader class.
 	 * 
 	 *  If the file is invalid MIDI, an InvalidMidiDataException is thrown and the test fails.
 	 */
 	public void testGeneratingMidi() {
 		
 		
 	}
 	
 	/**
 	 * /TS0020/ Playing time
 	 * 
 	 * The playing time of a sequence has to be at least 30 seconds and at most 10 minutes.
 	 * 
 	 * @throws IOException 
 	 * @throws InvalidMidiDataException 
 	 */
 	public void testPlayingTime() throws IOException, InvalidMidiDataException {
 		AComposer composer;
 		Sequence sequence;
 		long playtime;
 		
 		/*
 		 * Mozart trio composer
 		 */
 		
 		composer =  new MozartTrioDiceGame(drama);		
 		sequence = composer.createSequence();	
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartTrioDiceGame(kidsBook);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartTrioDiceGame(lyric);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartTrioDiceGame(novel);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartTrioDiceGame(nonFiction);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		/*
 		 * Mozart waltz composer
 		 */
 		
 		composer =  new MozartWaltzDiceGame(drama);		
 		sequence = composer.createSequence();	
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartWaltzDiceGame(kidsBook);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartWaltzDiceGame(lyric);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartWaltzDiceGame(novel);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new MozartWaltzDiceGame(nonFiction);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		/*
 		 * Kirnberger composer
 		 */
 		
 		composer =  new KirnbergerDiceGame(drama);		
 		sequence = composer.createSequence();	
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new KirnbergerDiceGame(kidsBook);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new KirnbergerDiceGame(lyric);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new KirnbergerDiceGame(novel);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);
 		
 		composer =  new KirnbergerDiceGame(nonFiction);		
 		sequence = composer.createSequence();		
 		playtime = sequence.getMicrosecondLength();
 		assertTrue(playtime > THIRTY_SECONDS && playtime <= TEN_MINUTES);		
 	}
 	
 	/**
 	 * /TS0030/ Reproducibility
 	 * 
 	 * Identic input yields identic output.
 	 * 
 	 * In this test, a text is sonified twice with the same text and the both
 	 * results are compared binary. Since the result of sonifiing the same text
 	 * twice should yield the exact same file, this case is asserted.
 	 * 
 	 * @throws IOException
 	 * @throws InvalidMidiDataException
 	 */
 	public void testReproducibility() throws IOException, InvalidMidiDataException {
 		AComposer firstComposer;
 		AComposer secondComposer;
 		
 		Sequence firstSequence;
 		Sequence secondSequence;
 		
 		Path firstTempPath;
 		Path secondTempPath;
 		
 		File firstFile;
 		File secondFile;
 		
 		/*
 		 * Mozart trio composer
 		 */
 		
 		// Drama
 		
 		firstComposer = new MozartTrioDiceGame(drama);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartTrioDiceGame(drama);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
 		// Non-Fiction		
 		
 		firstComposer = new MozartTrioDiceGame(nonFiction);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartTrioDiceGame(nonFiction);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
 		/*
 		 * Mozart trio composer
 		 */
 		
 		// Drama
 		
 		firstComposer = new MozartTrioDiceGame(drama);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartTrioDiceGame(drama);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
 		// Non-Fiction		
 		
 		firstComposer = new MozartTrioDiceGame(nonFiction);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartTrioDiceGame(nonFiction);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
 		/*
 		 * Mozart trio composer
 		 */
 		
 		// Kids book
 		
 		firstComposer = new MozartWaltzDiceGame(kidsBook);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartWaltzDiceGame(kidsBook);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
		// Novel
 		
 		firstComposer = new MozartWaltzDiceGame(novel);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartWaltzDiceGame(novel);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
 		/*
 		 * Kirnberger composer
 		 */
 		
 		// Lyric
 		
 		firstComposer = new MozartWaltzDiceGame(lyric);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartWaltzDiceGame(lyric);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);
 		
		// Novel
 		
 		firstComposer = new MozartWaltzDiceGame(novel);
 		firstSequence = firstComposer.createSequence();
 		firstTempPath = Files.createTempFile("test", null );
 		firstFile = firstTempPath.toFile();
 		MidiSystem.write( firstSequence, 1, firstFile);
 		
 		secondComposer = new MozartWaltzDiceGame(novel);
 		secondSequence = secondComposer.createSequence();
 		secondTempPath = Files.createTempFile("test", null );
 		secondFile = secondTempPath.toFile();
 		MidiSystem.write( secondSequence, 1, secondFile);
 		
 		tempFiles.add(firstFile);
 		tempFiles.add(secondFile);
 		
 		FileAssert.assertBinaryEquals(firstFile, secondFile);		
 	}
 	
 	
 	
 	
 
 
 
 }
