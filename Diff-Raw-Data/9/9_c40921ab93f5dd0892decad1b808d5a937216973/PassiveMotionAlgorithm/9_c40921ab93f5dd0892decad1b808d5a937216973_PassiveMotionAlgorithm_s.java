 /*
  * GRAIL Real Time Localization System
  * Copyright (C) 2011 Rutgers University and Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package com.owlplatform.solver.passivemotion;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.print.attribute.standard.Finishings;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.common.SampleMessage;
 import com.owlplatform.common.util.HashableByteArray;
 import com.owlplatform.common.util.NumericUtils;
 import com.owlplatform.solver.passivemotion.StdDevFingerprintGenerator;
 
 public class PassiveMotionAlgorithm {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(PassiveMotionAlgorithm.class);
 
 	private static final char[] MOTION_SYMBOLS = { ' ', '.', ':', '+', '#' };
 
 	protected String regionUri;
 
 	protected float regionXMax;
 
 	protected float regionYMax;
 
 	protected int numXTiles;
 
 	protected int numYTiles;
 
 	protected AlgorithmConfig config;
 
 	protected ConcurrentHashMap<String, Receiver> receivers = new ConcurrentHashMap<String, Receiver>();
 
 	protected ConcurrentHashMap<String, Transmitter> transmitters = new ConcurrentHashMap<String, Transmitter>();
 
 	protected ScoredTile[] tiles;
 
 	protected StdDevFingerprintGenerator stdDevFingerprinter = new StdDevFingerprintGenerator();
 
 	public PassiveMotionAlgorithm(AlgorithmConfig config) {
 		super();
 		this.config = config;
 		this.stdDevFingerprinter.setMaxNumSamples(3);
 		this.stdDevFingerprinter.setMaxSampleAge(5000l);
 	}
 
 	
 	public void addVariance(final String receiver, final String transmitter, final float variance, final long timestamp){
 	 
 	  this.stdDevFingerprinter.addVariance(transmitter, receiver, variance, timestamp);
 	}
 
 	public String getRegionId() {
 		return regionUri;
 	}
 
 	public void setRegionUri(String regionUri) {
 		this.regionUri = regionUri;
 	}
 
 	public float getRegionXMax() {
 		return regionXMax;
 	}
 
 	public void setRegionXMax(float regionXMax) {
 		this.regionXMax = regionXMax;
 	}
 
 	public float getRegionYMax() {
 		return regionYMax;
 	}
 
 	public void setRegionYMax(float regionYMax) {
 		this.regionYMax = regionYMax;
 	}
 
 	public int getNumXTiles() {
 		return numXTiles;
 	}
 
 	public void setNumXTiles(int numXTiles) {
 		this.numXTiles = numXTiles;
 	}
 
 	public int getNumYTiles() {
 		return numYTiles;
 	}
 
 	public void setNumYTiles(int numYTiles) {
 		this.numYTiles = numYTiles;
 	}
 
 	public ScoredTile[] getTiles() {
 		return tiles;
 	}
 
 	public void setTiles(ScoredTile[] tiles) {
 		this.tiles = tiles;
 	}
 
 	
 
 	public void addReceiver(Receiver receiver) {
 		
 		this.receivers.put(receiver.getDeviceId(), receiver);
 		log.debug("Added {}", receiver);
 	}
 
 	public void addTransmitter(Transmitter transmitter) {
 		
 		this.transmitters.put(transmitter.getDeviceId(), transmitter);
 		log.debug("Added {}", transmitter);
 	}
 
 	public FilteredTileResultSet generateResults() {
 		if (this.regionXMax == 0 || this.regionYMax == 0) {
 			return null;
 		}
 
 		if (this.config.desiredTileWidth > 0) {
 			this.numXTiles = (int) Math.ceil(this.regionXMax
 					/ this.config.desiredTileWidth);
 			this.numXTiles += (this.numXTiles - 1);
 		}
 		if (this.config.desiredTileHeight > 0) {
 			this.numYTiles = (int) Math.ceil(this.regionYMax
 					/ this.config.desiredTileWidth);
 			this.numYTiles += (this.numYTiles - 1);
 		}
 
 		FilteredTileResultSet resultSet = new FilteredTileResultSet();
 
 		// Calculate fingerprints
 		ArrayList<Fingerprint> fingerprints = this.calculateFingerprints();
 
 		// Create RSSI lines
 		ArrayList<RSSILine> allLines = this.createRSSILines(fingerprints);
 
 		resultSet.setLines(allLines);
 		
 		FilteredTileResult result;
 		
 		float[][] tileFilterKernel3x3 = new float[3][];
 		tileFilterKernel3x3[0] = new float[] { -.25f, -.25f, -.25f };
 		tileFilterKernel3x3[1] = new float[] { -.25f, 2.0f, -.25f };
 		tileFilterKernel3x3[2] = new float[] { -.25f, -.25f, -.25f };
 
 		float[][] tileFilterKernel3x3b = new float[3][];
 		tileFilterKernel3x3b[0] = new float[] { 0.05f, 0.05f, 0.05f };
 		tileFilterKernel3x3b[1] = new float[] { 0.05f, 0.25f, 0.05f };
 		tileFilterKernel3x3b[2] = new float[] { 0.05f, 0.05f, 0.05f };
 		
 		float[][] tileFilterKernel3x3c = new float[3][];
 		tileFilterKernel3x3c[0] = new float[] { -.25f, -.15f, -.25f };
 		tileFilterKernel3x3c[1] = new float[] { -.15f,   2f, -.15f };
 		tileFilterKernel3x3c[2] = new float[] { -.25f, -.15f, -.25f };
 
 		float[][] tileFilterKernel5x5 = new float[5][];
 		tileFilterKernel5x5[0] = new float[] { -1f, -1f, -1f, -1f,  -1f };
 		tileFilterKernel5x5[1] = new float[] { -1f,  2f,  2f,  2f,  -1f };
 		tileFilterKernel5x5[2] = new float[] { -1f,  2f,  2f,  2f,  -1f };
 		tileFilterKernel5x5[3] = new float[] { -1f,  2f,  2f,  2f,  -1f };
 		tileFilterKernel5x5[4] = new float[] { -1f, -1f, -1f, -1f,  -1f };
 		
 		
 		
 		ScoredTile[][] baseRaw = this.createUnscoredTiles();
 		result = new FilteredTileResult();
 		result.setTiles(baseRaw);
 		resultSet.setTiles("base-raw", result);
 		
 		ScoredTile[][] baseK3x3a = this.createUnscoredTiles();
 		result = new FilteredTileResult();
 		result.setTiles(baseK3x3a);
 		result.setKernel(tileFilterKernel3x3);
 		resultSet.setTiles("base-k3x3a", result);
 		
 		ScoredTile[][] baseK3x3c = this.createUnscoredTiles();
 		result = new FilteredTileResult();
 		result.setTiles(baseK3x3c);
 		result.setKernel(tileFilterKernel3x3c);
 		resultSet.setTiles("base-k3x3c", result);
 		
 		result = new FilteredTileResult();
 		ScoredTile[][] baseK5x5a = this.createUnscoredTiles();
 		result.setTiles(baseK5x5a);
 		result.setKernel(tileFilterKernel5x5);
 		resultSet.setTiles("base-k5x5a", result);
 
 		
 
 		ArrayList<ScoredTile> baseTiles = this.calculateTileScores(baseRaw,
 				allLines);
 
 		ScoredTile[][] microRaw = this.createMicroTiles(baseRaw);
 		result = new FilteredTileResult();
 		result.setTiles(microRaw);
 		resultSet.setTiles("micro-raw", result);
 		
 		ScoredTile[][] microk3x3a = this.createMicroTiles(baseRaw);
 		result = new FilteredTileResult();
 		result.setTiles(microk3x3a);
 		result.setKernel(tileFilterKernel3x3);
 		resultSet.setTiles("micro-k3x3a", result);
 		
 		ScoredTile[][] microk3x3c = this.createMicroTiles(baseRaw);
 		result = new FilteredTileResult();
 		result.setTiles(microk3x3c);
 		result.setKernel(tileFilterKernel3x3c);
 		resultSet.setTiles("micro-k3x3c", result);
 		
 				
 		ScoredTile[][] microk5x5a = this.createMicroTiles(baseRaw);
 		result = new FilteredTileResult();
 		result.setTiles(microk5x5a);
 		result.setKernel(tileFilterKernel5x5);
 		resultSet.setTiles("micro-k5x5a", result);
 
 		// log.info("Macro tiles:\n{}", this.printScoreMap(unfilteredTiles));
 		// log.info("Micro tiles:\n{}", this.printScoreMap(microTiles));
 
 		
 
 		ArrayList<ScoredTile> publishedTiles;
 
 		this.applyKernel(tileFilterKernel3x3, baseRaw, baseK3x3a);
 
 		publishedTiles = this.applyKernel(tileFilterKernel3x3, microRaw,
 				microk3x3a);
 		
 		this.applyKernel(tileFilterKernel3x3c, baseRaw, baseK3x3c);
 
 		publishedTiles = this.applyKernel(tileFilterKernel3x3c, microRaw,
 				microk3x3c);
 
 
 		this.applyKernel(tileFilterKernel5x5, baseRaw, baseK5x5a);
 
 		this.applyKernel(tileFilterKernel5x5, microRaw, microk5x5a);
 		
 //		this.applyHighPass(baseK5x5a, 5);
 		
 //		this.applyHighPass(microk5x5a, 1);
 
 		resultSet.setTilesToPublish(publishedTiles);
 
		log.info(this.printFancyMap(baseRaw));
 		
 		return resultSet;
 
 	}
 	
 	protected void applyHighPass(final ScoredTile[][] tiles, final float minScore)
 	{
 		for(int i = 0; i < tiles.length; ++i)
 		{
 			for(int j = 0; j < tiles[i].length; ++j)
 			{
 				if(tiles[i][j].getScore() <= minScore){
 					tiles[i][j].setScore(0f);
 				}
 			}
 		}
 	}
 
 	protected ScoredTile[][] createMicroTiles(ScoredTile[][] macroTiles) {
 		ScoredTile[][] microTiles = new ScoredTile[macroTiles.length + 1][];
 		for (int i = 0; i < microTiles.length; ++i)
 			microTiles[i] = new ScoredTile[macroTiles[0].length + 1];
 
 		for (int x = 0; x < microTiles.length; ++x) {
 			for (int y = 0; y < microTiles[x].length; ++y) {
 				microTiles[x][y] = new ScoredTile();
 				microTiles[x][y].setScore(0f);
 				Rectangle2D dimensions = new Rectangle2D.Float();
 
 				// Check odd or even x, y
 				// even means first microtile
 				// odd mean second microtile
 
 				int macroX = x;
 				int macroY = y;
 				if (x >= macroTiles.length) {
 					macroX = macroTiles.length - 1;
 				}
 				if (y >= macroTiles[macroX].length) {
 					macroY = macroTiles[macroX].length - 1;
 				}
 
 				double newX = macroTiles[macroX][macroY].getTile().getX();
 				double newY = macroTiles[macroX][macroY].getTile().getY();
 				double newWidth = macroTiles[macroX][macroY].getTile()
 						.getWidth() / 2;
 				double newHeight = macroTiles[macroX][macroY].getTile()
 						.getHeight() / 2;
 
 				if (x == microTiles.length - 1) {
 					newX += newWidth;
 				}
 				if (y == microTiles[x].length - 1) {
 					newY += newHeight;
 				}
 
 				microTiles[x][y].setTile(new Rectangle2D.Float((float) newX,
 						(float) newY, (float) newWidth, (float) newHeight));
 
 				float tileScore = 0;
 
 				if (x < macroTiles.length - 1 && y < macroTiles[x].length) {
 					tileScore += macroTiles[x][y].getScore();
 				}
 				if (x - 1 >= 0 && y < macroTiles[x - 1].length) {
 					tileScore += macroTiles[x - 1][y].getScore();
 				}
 				if (x < macroTiles.length && y - 1 >= 0) {
 					tileScore += macroTiles[x][y - 1].getScore();
 				}
 				if (x - 1 >= 0 && y - 1 >= 0) {
 					tileScore += macroTiles[x - 1][y - 1].getScore();
 				}
 //				microTiles[x][y].setScore((float)Math.log10(tileScore));
 				microTiles[x][y].setScore(tileScore);
 				if(Float.isInfinite(microTiles[x][y].getScore()))
 				{
 					microTiles[x][y].setScore(0);
 				}
 				if(microTiles[x][y].getScore() < 0)
 				{
 					microTiles[x][y].setScore(0);
 				}
 
 			}
 		}
 
 		return microTiles;
 	}
 
 	protected ArrayList<ScoredTile> applyKernel(final float[][] kernel,
 			final ScoredTile[][] inTiles, ScoredTile[][] outTiles) {
 		ArrayList<ScoredTile> solutionTiles = new ArrayList<ScoredTile>();
 		if (outTiles.length != inTiles.length) {
 			throw new IllegalArgumentException(
 					"Must provide same-sized arrays for inTiles and outTiles.");
 		}
 
 		float kernelSum = 0;
 		for(int i = 0; i < kernel.length; ++i)
 		{
 			for(int j = 0; j < kernel[i].length; ++j)
 			{
 				kernelSum += kernel[i][j];
 			}
 		}
 		
 		// Assume kernel to be square, and really 3x3 right now
 		int kernelMidX = kernel.length / 2;
 		int kernelMidY = kernel[0].length / 2;
 		for (int x = 0; x < inTiles.length; ++x) {
 			if (outTiles[x].length != inTiles[x].length) {
 				throw new IllegalArgumentException(
 						"Must provide same-sized arrays for inTiles and outTiles.");
 			}
 
 			for (int y = 0; y < inTiles[x].length; ++y) {
 				outTiles[x][y].setScore(0f);
 
 				for (int i = 0; i < kernel.length; ++i) {
 					// allTiles[x + (i - kernelMidX)][] * kernel[i][]
 					for (int j = 0; j < kernel[i].length; ++j) {
 						int tilesX = x + i - kernelMidX;
 						int tilesY = y + j - kernelMidY;
 
 						// Extend edge cells out to infinity
 						if (tilesX < 0) {
 							tilesX = 0;
 						}
 						if (tilesX >= inTiles.length) {
 							tilesX = inTiles.length - 1;
 						}
 
 						if (tilesY < 0) {
 							tilesY = 0;
 						}
 						if (tilesY >= inTiles[x].length) {
 							tilesY = inTiles[x].length - 1;
 						}
 
 						outTiles[x][y].score += inTiles[tilesX][tilesY]
 								.getScore()
 								* kernel[i][j];
 					}
 				}
 
 				if(Math.abs(kernelSum) > 0.001)
 				{
 					outTiles[x][y].score /= kernelSum;
 				}
 				
 				if (outTiles[x][y].getScore() < 0) {
 					outTiles[x][y].setScore(0f);
 				}
 				// if(outTiles[x][y].getScore() < this.tileScoreThreshold)
 				// {
 				// outTiles[x][y].setScore(0f);
 				// }
 
 				if (outTiles[x][y].getScore() > 0) {
 					solutionTiles.add(outTiles[x][y]);
 				}
 
 			}
 		}
 
 		return solutionTiles;
 	}
 
 	/**
 	 * Calculates tile scores based on the intersecting lines. This method will
 	 * modify the scores of the allTiles parameter.
 	 * 
 	 * @param allTiles
 	 * @param allLines
 	 * @return an ArrayList of ScoredTile objects that have non-zero scores.
 	 */
 	protected ArrayList<ScoredTile> calculateTileScores(
 			final ScoredTile[][] allTiles, final Collection<RSSILine> allLines) {
 		ArrayList<ScoredTile> solutionTiles = new ArrayList<ScoredTile>();
 		// Calculate raw scores for each tile
 		for (int x = 0; x < allTiles.length; ++x) {
 			for (int y = 0; y < allTiles[x].length; ++y) {
 				// Clear the score to 0
 				allTiles[x][y].setScore(0);
 				for (RSSILine line : allLines) {
 					// Make sure line isn't too far away
 					Rectangle2D.Float theTile = allTiles[x][y].getTile();
 					Point2D.Float tileCenter = new Point2D.Float(theTile.x
 							+ theTile.width / 2, theTile.y + theTile.height / 2);
 					// Check P1 distance (receiver)
 					if (Math.sqrt(Math.pow(tileCenter.x - line.getLine().x1, 2)
 							+ Math.pow(tileCenter.y - line.getLine().y1, 2)) > this.config.radiusThreshold) {
 						continue;
 					}
 
 					// Check P2 distance (transmitter)
 					if (Math.sqrt(Math.pow(tileCenter.x - line.getLine().x2, 2)
 							+ Math.pow(tileCenter.y - line.getLine().y2, 2)) > this.config.radiusThreshold) {
 						continue;
 					}
 
 					// Check intersection
 					if (!line.getLine().intersects(theTile)) {
 						continue;
 					}
 
 					float lineLength = (float) Math.sqrt(Math.pow(line
 							.getLine().x1
 							- line.getLine().x2, 2)
 							+ Math
 									.pow(line.getLine().y1 - line.getLine().y2,
 											2));
 
 					float numerator = line.getValue()
 							- this.config.stdDevNoiseThreshold;
 					allTiles[x][y].score += (float) (numerator / (Math.pow(
 							lineLength, this.config.lineLengthPower)));
 
 				}
 
 				// allTiles[x][y].score = allTiles[x][y].score *100f /
 				// (allTiles[x][y].tile.height*allTiles[x][y].tile.width);
 
 				// Make sure the tile score is above the threshold
 				if (allTiles[x][y].getScore() <= this.config.tileScoreThreshold) {
 					// log.debug("{} score is below threshold {}.  Skipping...",allTiles[x][y],this.tileScoreThreshold);
 					allTiles[x][y].setScore(0f);
 					continue;
 				}
 
 				solutionTiles.add(allTiles[x][y]);
 
 			}
 		}
 		return solutionTiles;
 	}
 
 	protected ArrayList<RSSILine> createRSSILines(
 			final Collection<Fingerprint> fingerprints) {
 		// Create RSSI lines
 		ArrayList<RSSILine> allLines = new ArrayList<RSSILine>();
 
 		for (Fingerprint fingerprint : fingerprints) {
 		  // FIXME: What the hell is this? Left over from old code, but is it receiver or transmitter?
 			Receiver receiver = this.receivers.get(fingerprint.getReceiverId());
 			if (receiver  == null) {
 				log.debug("Unknown receiver {}. Skipping...", fingerprint.getReceiverId());
 				continue;
 			}
 
 			for (String transmitterId : fingerprint.getRssiValues()
 					.keySet()) {
 				RSSILine line = new RSSILine();
 				Transmitter transmitter = this.transmitters.get(transmitterId);
 				if (transmitter == null) {
 					log.warn("Unknown transmitter {}. Skipping...", transmitterId);
 					continue;
 				}
 
 				Float value = fingerprint.getRssiValues().get(transmitterId);
 				if (value == null) {
 					log
 							.warn(
 									"Missing Std. Dev. Value for Tx: {}, Rx: {}. Skipping...",
 									fingerprint
 											.getReceiverId(),
 									transmitter
 											.getDeviceId());
 					continue;
 				}
 				if (value.floatValue() <= this.config.stdDevNoiseThreshold) {
 					continue;
 				}
 				Line2D.Float theLine = new Line2D.Float();
 				theLine.setLine(receiver.getxLocation(), receiver
 						.getyLocation(), transmitter.getxLocation(),
 						transmitter.getyLocation());
 
 				line.setLine(theLine);
 				line.setReceiver(receiver);
 				line.setTransmitter(transmitter);
 				line.setValue(value.floatValue());
 				allLines.add(line);
 
 			}
 		}
 
 		log.debug("Generated {} lines.", allLines.size());
 		return allLines;
 	}
 
 	protected ArrayList<Fingerprint> calculateFingerprints() {
 		ArrayList<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
 		for (Receiver receiver : this.receivers.values()) {
 			Fingerprint fingerprint = this.stdDevFingerprinter
 					.generateFingerprint(receiver.getDeviceId());
 			if (fingerprint != null) {
 				fingerprints.add(fingerprint);
 			} else {
 				log.warn("{} cannot be heard.", receiver);
 			}
 		}
 
 		log.debug("Generated {}/{} fingerprints.", fingerprints.size(),
 				this.transmitters.size());
 
 		return fingerprints;
 	}
 
 	protected ScoredTile[][] createUnscoredTiles() {
 		// Create unscored tiles
 		ScoredTile[][] allTiles = new ScoredTile[this.numXTiles][this.numYTiles];
 
 		float tileXStep = this.getRegionXMax() / (this.numXTiles + 1.0f);
 		float tileYStep = this.getRegionYMax() / (this.numYTiles + 1.0f);
 
 		for (int x = 0; x < allTiles.length; ++x) {
 			for (int y = 0; y < allTiles[x].length; ++y) {
 				ScoredTile theTile = new ScoredTile();
 				Rectangle2D.Float dimensions = new Rectangle2D.Float();
 				dimensions.setRect(tileXStep * x, tileYStep * y,
 						tileXStep * 2f, tileYStep * 2f);
 				theTile.setTile(dimensions);
 				theTile.setScore(0f);
 				allTiles[x][y] = theTile;
 			}
 		}
 
 		log.debug("Created tiles {} x {}", allTiles.length, allTiles[0].length);
 		return allTiles;
 	}
 
 	private final String printScoreMap(ScoredTile[][] allTiles) {
 		StringBuffer sb = new StringBuffer();
 		for (int y = allTiles[0].length - 1; y >= 0; --y) {
 			for (int x = 0; x < allTiles.length; ++x) {
 				sb.append(String.format("[%05.2f]", allTiles[x][y].score));
 			}
 			sb.append('\n');
 		}
 
 		return sb.toString();
 	}
 
 	private final String printFancyMap(ScoredTile[][] allTiles) {
 		StringBuffer sb = new StringBuffer();
 		sb.append('+');
 		for (int x = 0; x < allTiles.length; ++x) {
 			sb.append("--");
 		}
 		sb.append("+\n");
 		for (int y = allTiles[0].length - 1; y >= 0; --y) {
 			sb.append('|');
 			for (int x = 0; x < allTiles.length; ++x) {
 				char motionSymbol = MOTION_SYMBOLS[0];
 				float score = allTiles[x][y].getScore();
 				if (score > 2f * this.config.tileScoreThreshold) {
 					motionSymbol = MOTION_SYMBOLS[4];
 				} else if (score > 1.66f * this.config.tileScoreThreshold) {
 					motionSymbol = MOTION_SYMBOLS[3];
 				} else if (score > 1.33f * this.config.tileScoreThreshold) {
 					motionSymbol = MOTION_SYMBOLS[2];
 				} else if (score > this.config.tileScoreThreshold) {
 					motionSymbol = MOTION_SYMBOLS[1];
 				}
 				sb.append(motionSymbol).append(motionSymbol);
 			}
 			if (y == 0) {
 				sb.append(String.format("| %4.2f\n",
 						allTiles[0][0].getTile().height));
 			} else {
 				sb.append("|\n");
 			}
 		}
 		sb.append('+');
 		for (int x = 0; x < allTiles.length; ++x) {
 			sb.append("--");
 		}
 		sb.append("+\n");
 		sb.append(String.format(" %4.2f\n", allTiles[0][0].getTile().width));
 		return sb.toString();
 	}
 
 
   public StdDevFingerprintGenerator getStdDevFingerprinter() {
     return stdDevFingerprinter;
   }
 
   public void setStdDevFingerprinter(
       StdDevFingerprintGenerator stdDevFingerprinter) {
     this.stdDevFingerprinter = stdDevFingerprinter;
   }
 }
