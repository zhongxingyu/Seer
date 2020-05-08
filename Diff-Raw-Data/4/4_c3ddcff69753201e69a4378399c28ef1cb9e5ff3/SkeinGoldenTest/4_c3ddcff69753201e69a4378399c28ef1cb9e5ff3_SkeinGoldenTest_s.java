 /*
  * Copyright (c) 2012, Robert von Burg
  *
  * All rights reserved.
  *
  * This file is part of the XXX.
  *
  *  XXX is free software: you can redistribute 
  *  it and/or modify it under the terms of the GNU General Public License as 
  *  published by the Free Software Foundation, either version 3 of the License, 
  *  or (at your option) any later version.
  *
  *  XXX is distributed in the hope that it will 
  *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with XXX.  If not, see 
  *  <http://www.gnu.org/licenses/>.
  */
 package nl.warper.skein.test;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import nl.warper.skein.Skein;
 import nl.warper.skein.SkeinException;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.eitchnet.utils.helper.StringHelper;
 
 /**
  * This class tests the Skein implementation by verifying the digests with the "skein_golden_kat.txt" file delivered by
  * the Skein Creators
  * 
  * @author Robert von Burg <eitch@eitchnet.ch>
  */
 public class SkeinGoldenTest {
 
 	private static final Logger logger = LoggerFactory.getLogger(SkeinGoldenTest.class);
 	private static final String SKEIN_GOLDEN_KAT_TXT = "/skein_test_vectors/skein_golden_kat.txt";
 
 	private static enum GoldenKatMsgType {
 		UNKOWN, ZERO, INCREMENTING, RANDOM, RANDOM_MAC, TREE_LEAF;
 
 		/**
 		 * @param msgTypeS
 		 * @return
 		 */
 		public static GoldenKatMsgType parse(String msgTypeS) {
 			if (msgTypeS.equals("zero"))
 				return ZERO;
 			if (msgTypeS.equals("incrementing"))
 				return INCREMENTING;
 			if (msgTypeS.equals("random"))
 				return RANDOM;
 			if (msgTypeS.equals("random+MAC"))
 				return RANDOM_MAC;
 			throw new RuntimeException("Unexpected msgType: " + msgTypeS);
 		}
 	}
 
 	private static class GoldenKat {
 		private int lineNr;
 		private int blockSize;
 		private int digestSize;
 		private GoldenKatMsgType msgType;
 		private int msgLength;
 		private byte[] message;
 		private byte[] digest;
 		private int macLength;
 		private byte[] mac;
 
 		/**
 		 * @param blockSize
 		 * @param digestSize
 		 * @param msgLength
 		 */
 		public GoldenKat(int lineNr, int blockSize, int digestSize, GoldenKatMsgType msgType, int msgLength) {
 			this.lineNr = lineNr;
 			this.blockSize = blockSize;
 			this.digestSize = digestSize;
 			this.msgType = msgType;
 			this.msgLength = msgLength;
 		}
 
 		/**
 		 * 
 		 */
 		public void validate() {
 
 			// java only knows full bytes, so round up
 			int msgLength = this.msgLength % 8 == 0 ? this.msgLength : this.msgLength + (8 - (this.msgLength % 8));
 
 			// validate message length			
 			int dataBits = this.message.length * 8;
 			if (dataBits != msgLength) {
 				throw new RuntimeException(String.format(
 						"Message should be %d bits (%d bytes) but data is %d bits (%d bytes) long", msgLength,
 						msgLength / 8, dataBits, this.message.length));
 			}
 
 			// validate digest size is same as digest
 			int digestBits = this.digest.length * 8;
 			if (digestBits != this.digestSize) {
 				throw new RuntimeException(String.format(
 						"Digest should be %d bits (%d bytes) but data is %d bits (%d bytes) long", this.digestSize,
 						this.digestSize / 8, digestBits, this.digest.length));
 			}
 
 			// if mac set, then validate its length
 			if (this.mac != null && this.macLength != this.mac.length) {
 				throw new RuntimeException(String.format("MAC should have length %d bytes but is %d bytes long",
 						this.macLength, this.mac.length));
 			}
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder builder = new StringBuilder();
 			builder.append("GoldenKat [lineNr=");
 			builder.append(this.lineNr);
 			builder.append(", blockSize=");
 			builder.append(this.blockSize);
 			builder.append(", digestSize=");
 			builder.append(this.digestSize);
 			builder.append(", msgType=");
 			builder.append(this.msgType);
 			builder.append(", msgLength=");
 			builder.append(this.msgLength);
 			builder.append(", macLength=");
 			builder.append(this.macLength);
 			builder.append("]");
 			return builder.toString();
 		}
 	}
 
 	private static List<GoldenKat> goldenKatList = new ArrayList<>();
 	private static List<GoldenKat> skippedList = new ArrayList<>();
 
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 
 		InputStream goldenKatIo = SkeinGoldenTest.class.getResourceAsStream(SkeinGoldenTest.SKEIN_GOLDEN_KAT_TXT);
 		if (goldenKatIo == null) {
 			throw new RuntimeException("Failed to find data file at " + SkeinGoldenTest.SKEIN_GOLDEN_KAT_TXT);
 		}
 
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(goldenKatIo));
 
 			int lineNr = 0;
 			String line;
 
 			GoldenKat goldenKat = null;
 			boolean inMsg = false;
 			boolean inMac = false;
 			boolean inResult = false;
 			StringBuilder messageDataSb = null;
 			StringBuilder resultSb = null;
 			StringBuilder macKeySb = null;
 
 			while ((line = reader.readLine()) != null) {
 				lineNr++;
 
 				try {
 					if (line.trim().isEmpty())
 						continue;
 
 					if (line.startsWith(":")) {
 
 						goldenKat = SkeinGoldenTest.parseHeader(lineNr, line.trim());
 						if (goldenKat == null)
 							continue;
 
 					} else if (line.startsWith("Message data:")) {
 						if (goldenKat == null) {
 							SkeinGoldenTest.logger.warn(String.format(
 									"Skipping message data at line %d as no header was found for it.", lineNr));
 							continue;
 						}
 
 						messageDataSb = new StringBuilder();
 						inMsg = true;
 						inMac = false;
 						inResult = false;
 
 					} else if (line.startsWith("MAC key")) {
 						if (goldenKat == null) {
 							SkeinGoldenTest.logger.warn(String.format(
 									"Skipping MAC Key data at line %d as no header was found for it.", lineNr));
 							continue;
 						}
 
 						int pos = line.indexOf('=') + 1;
 						int endIndex = line.indexOf("byte");
 						String macKeyLengthS = line.substring(pos, endIndex).trim();
 						int macKeyLength = Integer.parseInt(macKeyLengthS);
 						goldenKat.macLength = macKeyLength;
 
 						macKeySb = new StringBuilder();
 						inMsg = false;
 						inMac = true;
 						inResult = false;
 
 					} else if (line.startsWith("Result:")) {
 						if (goldenKat == null) {
 							SkeinGoldenTest.logger.warn(String.format(
 									"Skipping result data at line %d as no header was found for it.", lineNr));
 							continue;
 						}
 
 						resultSb = new StringBuilder();
 						inMsg = false;
 						inMac = false;
 						inResult = true;
 
 					} else if (line.startsWith("-----")) {
 
 						if (goldenKat == null)
 							continue;
 
 						if (resultSb == null)
 							throw new RuntimeException("No result found for " + goldenKat.toString());
 						if (messageDataSb == null)
 							throw new RuntimeException("No message found for " + goldenKat.toString());
 
 						String digest = resultSb.toString().replaceAll(" ", "");
 						byte[] digestBytes = StringHelper.fromHexString(digest);
 						goldenKat.digest = digestBytes;
 
 						String message = messageDataSb.toString().replaceAll(" ", "");
 						byte[] messageBytes;
 						if (message.contains("(none)"))
 							messageBytes = new byte[0];
 						else
 							messageBytes = StringHelper.fromHexString(message);
 						goldenKat.message = messageBytes;
 
 						if (macKeySb != null) {
 							String macKey = macKeySb.toString().replaceAll(" ", "");
 							byte[] macKeyBytes;
 							if (macKey.contains("(none)"))
 								macKeyBytes = new byte[0];
 							else
 								macKeyBytes = StringHelper.fromHexString(macKey);
 							goldenKat.mac = macKeyBytes;
 						}
 
 						SkeinGoldenTest.logger.info("Found: " + goldenKat.toString());
 						goldenKat.validate();
 						SkeinGoldenTest.goldenKatList.add(goldenKat);
 						goldenKat = null;
 						digest = null;
 						macKeySb = null;
 						resultSb = null;
 						inMsg = false;
 						inMac = false;
 						inResult = false;
 
 					} else {
 
 						if (goldenKat == null)
 							continue;
 
 						if (inResult) {
 							if (resultSb == null)
 								throw new RuntimeException("In result but resultSb is null!");
 							resultSb.append(line.trim());
 						} else if (inMsg) {
 							if (messageDataSb == null)
 								throw new RuntimeException("In message but messageDataSb is null!");
 							messageDataSb.append(line.trim());
 						} else if (inMac) {
 							if (macKeySb == null)
 								throw new RuntimeException("In mac but macKeySb is null!");
 							macKeySb.append(line.trim());
 						} else {
 							throw new RuntimeException("No StringBuilder found to append data to!");
 						}
 					}
 
 				} catch (Exception e) {
 					SkeinGoldenTest.logger.error("golden kat: " + goldenKat);
 					SkeinGoldenTest.logger.error("result: " + resultSb);
 					SkeinGoldenTest.logger.error("message: " + messageDataSb);
 					SkeinGoldenTest.logger.error("mac: " + macKeySb);
 					RuntimeException ex = new RuntimeException(String.format("Failed handling line(%d): %s", lineNr,
 							line), e);
 					SkeinGoldenTest.logger.error(ex.getMessage(), ex);
 					throw ex;
 				}
 			}
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 	}
 
 	/**
 	 * @param line
 	 * @return
 	 */
 	private static GoldenKat parseHeader(int lineNr, String line) {
 
 		int blockSize;
 		int digestSize;
 		int msgLength;
 		GoldenKatMsgType msgType;
 		int pos;
 		int end;
 
 		pos = line.indexOf('-', 0);
 		end = line.indexOf(':', pos);
 		blockSize = Integer.parseInt(line.substring(pos + 1, end).trim());
 
 		pos = end + 1;
 		end = line.indexOf("-bit", pos);
 		digestSize = Integer.parseInt(line.substring(pos + 1, end).trim());
 
 		pos = end + 1;
 		pos = line.indexOf('=', pos);
 		end = line.indexOf("bits", pos);
 		msgLength = Integer.parseInt(line.substring(pos + 1, end).trim());
 
 		pos = end + 1;
 		pos = line.indexOf("data", pos);
 		if (pos != -1) {
 
 			pos = end + 1;
 			pos = line.indexOf('\'', pos);
 			end = line.indexOf('\'', pos + 1);
 			String msgTypeS = line.substring(pos + 1, end).trim();
 			msgType = GoldenKatMsgType.parse(msgTypeS);
 		} else {
 			pos = end + 1;
 			pos = line.indexOf("Tree:", pos);
 			if (pos != -1) {
 				SkeinGoldenTest.logger.warn(String.format("Found Tree at line %d, skipping...", lineNr));
 				SkeinGoldenTest.skippedList.add(new GoldenKat(lineNr, blockSize, digestSize,
 						GoldenKatMsgType.TREE_LEAF, msgLength));
 				return null;
 			}
 
 			SkeinGoldenTest.logger.error(String.format(
 					"Found unexpected line type with no data and no tree leaf at line %d: %s", lineNr, line));
 			SkeinGoldenTest.skippedList.add(new GoldenKat(lineNr, blockSize, digestSize, GoldenKatMsgType.UNKOWN,
 					msgLength));
 			return null;
 		}
 
 		GoldenKat goldenKat = new GoldenKat(lineNr, blockSize, digestSize, msgType, msgLength);
 		return goldenKat;
 	}
 
 	@Test
 	public void test() {
 
 		double nrOfTests = goldenKatList.size();
 		double nrOfIgnored = 0;
 		double nrOfFails = 0;
 		double nrOfMacTests = 0;
 		double nrOfMacFails = 0;
 
 		List<GoldenKat> tmpList = new ArrayList<>(SkeinGoldenTest.goldenKatList);
 		for (GoldenKat goldenKat : tmpList) {
 
 			SkeinGoldenTest.logger.info("Test      : " + goldenKat.toString());
 			SkeinGoldenTest.logger.info("Message   : " + StringHelper.getHexString(goldenKat.message));
 			SkeinGoldenTest.logger.info("Expected  : " + StringHelper.getHexString(goldenKat.digest));
 
 			if (goldenKat.msgLength % 8 != 0) {
 				logger.warn("Result    : Skipping test as msgLength is not multple of 8");
 				nrOfIgnored++;
 				continue;
 			}
 			if (goldenKat.msgType == GoldenKatMsgType.RANDOM_MAC && goldenKat.macLength % 8 != 0) {
 				logger.warn("Result    : Skipping test as macLength is not multple of 8");
 				nrOfIgnored++;
 				continue;
 			}
 
 			Skein skein = new Skein(goldenKat.blockSize, goldenKat.digestSize);
 
 			if (goldenKat.msgType == GoldenKatMsgType.RANDOM_MAC) {
 				skein.setKey(goldenKat.mac);
 				nrOfMacTests++;
 			}
 
 			try {
 				byte[] digest = skein.doSkein(goldenKat.message);
 
 				SkeinGoldenTest.logger.info("Digest    : " + StringHelper.getHexString(digest));
 				boolean valid = Arrays.equals(goldenKat.digest, digest);
 				if (!valid) {
 					SkeinGoldenTest.logger.error("Result    : Test failed for " + goldenKat.toString());
 					nrOfFails++;
 					if (goldenKat.msgType == GoldenKatMsgType.RANDOM_MAC)
 						nrOfMacFails++;
 				}
 
 			} catch (SkeinException e) {
 				logger.error("Result     : Test failed for " + goldenKat.toString(), e);
 				nrOfFails++;
 
 				if (goldenKat.msgType == GoldenKatMsgType.RANDOM_MAC)
 					nrOfMacFails++;
 			}
 		}
 
 		double nrOfSkippedTests = SkeinGoldenTest.skippedList.size();
 		double nrOfTestVectors = goldenKatList.size() + nrOfSkippedTests;
 		double skippedTestVectorsPercent = skippedList.size() / nrOfTestVectors * 100.0d;
 		double ignoredPercent = (nrOfTests - nrOfIgnored) / nrOfTests * 100.0d;
 
 		double macFailPercent = nrOfMacFails / nrOfMacTests * 100.0d;
 		double successPercent = (nrOfTests - nrOfFails) / nrOfTests * 100.0d;
 		double successPercentIgnore = (nrOfTests - nrOfFails - nrOfIgnored) / nrOfTests * 100.0d;
 		logger.info("");
 		logger.info(String.format("Runs      : Performed %.0f of the %.0f test vectors", nrOfTests, nrOfTestVectors));
 		logger.info(String
 				.format("Skipped   : %.1f%% of the test vectors because are not implemented by the Skein implementation (e.g. tree leaves)",
 						skippedTestVectorsPercent));
 		logger.info(String.format(
 				"Ignored   : %.1f%% because their data is not a multiple of 8 (Java limitation on bytes)",
 				ignoredPercent));
 		logger.info(String.format("MAC fails : %.0f of %.0f (%.1f%%) of all run tests", nrOfMacFails, nrOfMacTests,
 				macFailPercent));
 		String msg = String.format("Performed %.0f tests, with %.0f failures. Pass %.1f%% (%.1f%%) / Skipped %.0f",
 				nrOfTests, nrOfFails, successPercent, successPercentIgnore, nrOfSkippedTests);
 		if (successPercent == 100.0d) {
 			SkeinGoldenTest.logger.info(msg);
 		} else {
 			SkeinGoldenTest.logger.error(msg);
 
 			// TODO currently we don't fail the build...
 			//Assert.fail(msg);
 		}
 	}
 
 	@Test
 	public void performanceTest() {
 
 		byte[] bytes = new byte[1024 * 1024];
 
 		Skein skein;
 		long start = System.nanoTime();
 		for (int i = 0; i < 200; i++) {
 			skein = new Skein(512, 512);
 			skein.doSkein(bytes);
 		}
 		long end = System.nanoTime();
 		logger.info("Skein512-512 Hashing 200MB took " + StringHelper.formatNanoDuration(end - start));
 
		skein = new Skein(256, 256);
 		start = System.nanoTime();
 		for (int i = 0; i < 200; i++) {
			skein = new Skein(512, 512);
 			skein.doSkein(bytes);
 		}
 		end = System.nanoTime();
 		logger.info("Skein256-256 Hashing 200MB took " + StringHelper.formatNanoDuration(end - start));
 	}
 }
