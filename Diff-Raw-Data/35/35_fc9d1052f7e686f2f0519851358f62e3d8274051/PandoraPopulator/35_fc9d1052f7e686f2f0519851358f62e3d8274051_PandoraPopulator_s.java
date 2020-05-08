 /*
  * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.pandora;
 
 //* IMPORTS: JDK/JRE
 	import java.util.ArrayList;
 	import java.util.List;
 	import java.util.Random;
 //* IMPORTS: BUKKIT
 	import org.bukkit.Chunk;
 	import org.bukkit.generator.BlockPopulator;
 	import org.bukkit.Location;
 	import org.bukkit.util.noise.SimplexNoiseGenerator;
 	import org.bukkit.World;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class PandoraPopulator extends BlockPopulator
 {
 	private List<PandoraBiomePopulator> populators = new ArrayList<PandoraBiomePopulator>();
 	private PandoraBiomePopulator lastPop, defaultPop, tempPop;
 	private SimplexNoiseGenerator noise;
 	private Location center, currentEdge, tempEdge;
 	private int lastX, lastZ, xPos, zPos, currentX, currentZ, cXPos, cZPos, xs, zs;
 	private int edgeMax, edgeXPos, edgeZPos, octaves;
 	private double temperature, humidity, tempRange, humidityRange;
 	private double range, scale, amplitude, frequency, cnoise;
 	private boolean useCustomMetrics = false;
 
 	public PandoraPopulator addBiome(PandoraBiomePopulator biome) {
 		if(biome == null)
 			return this;
 
 		populators.add(biome);
 		return this;
 	}
 
 	public double getBiomeNoise(World world, int x, int z, boolean invertSeed) {
 		if(world == null)
 			return 0D;
 
 		if(!useCustomMetrics)
 			return 0D;
 
 		if(!invertSeed)
 			this.noise = new SimplexNoiseGenerator(world.getSeed());
 		else
 			this.noise = new SimplexNoiseGenerator(~(world.getSeed()));
 
 		range /= 2D;
 		xs = (int) Math.round(x / scale);
 		zs = (int) Math.round(z / scale);
 		cnoise = noise.getNoise(xs, zs, octaves, frequency, amplitude);
 
 		return ((range * cnoise) + range);
 	}
 
 	public List<PandoraBiomePopulator> getDefaultBiomes(World world) {
 		return populators;
 	}
 
 	public double getHumidity(World world, int x, int z) {
 		if(useCustomMetrics)
 			return getBiomeNoise(world, x, z, true);
 
 		return world.getHumidity(x, z);
 	}
 
 	public Location getNearestEdge(World world, int x, int z, int period, int count) {
 		return getNearestEdge(world, x, z, period, count, false);
 	}
 
 	public Location getNearestEdge(World world, int x, int z, int period, int count, boolean inner) {
 		if(world == null)
 			return null;
 
 		getPopulator(world, x, z);
 
 		if(lastPop == null)
 			return null;
 
 		tempPop = lastPop;
 		edgeMax = period * count;
 
 		currentEdge = null;
 		tempEdge = null;
 
 		center = new Location(world, (double) x, 0D, (double) z);
 
 		for(edgeXPos = (x - edgeMax); edgeXPos <= (x + edgeMax); edgeXPos += period) {
 			for(edgeZPos = (z - edgeMax); edgeZPos <= (z + edgeMax); edgeZPos += period) {
 				getPopulator(world, edgeXPos, edgeZPos);
 
 				if(lastPop != tempPop && currentEdge == null) {
 					currentEdge = new Location(world, (double) edgeXPos, 0D, (double) edgeZPos);
 					continue;
 				}
 				else if(lastPop != tempPop) {
 					tempEdge = new Location(world, (double) edgeXPos, 0D, (double) edgeZPos);
 
 					if(center.distance(currentEdge) > center.distance(tempEdge))
 						currentEdge = tempEdge;
 				}
 			}
 		}
 
 		if(currentEdge == null) {
 			return null;
 		}
 		else if(!inner) {
 			edgeXPos = currentEdge.getBlockX();
 			edgeZPos = currentEdge.getBlockZ();
 
 			return getNearestEdge(world, edgeXPos, edgeZPos, 1, period, true);
 		}
 
 		return currentEdge;
 	}
 
 	public int getNearestEdgeBlockDistance(World world, int x, int z, int period, int count) {
 		return (int) getNearestEdgeDistance(world, x, z, period, count);
 	}
 
 	public double getNearestEdgeDistance(World world, int x, int z, int period, int count) {
 		if(getNearestEdge(world, x, z, period, count) == null)
 			return -1D;
 
 		return center.distance(currentEdge);
 	}
 
 	private void getPopulator(World world, int x, int z) {
 		if((x == lastX && z == lastZ && lastPop != null) || world == null)
 			return;
 
 		temperature = getTemperature(world, x, z);
 		humidity = getHumidity(world, x, z);
 
 
 		lastPop = defaultPop;
 
 		for(PandoraBiomePopulator currentPop : populators) {
 			if(currentPop.minTemperature > temperature)
 				continue;
 			else if(currentPop.maxTemperature < temperature)
 				continue;
 			else if(currentPop.minHumidity > humidity)
 				continue;
 			else if(currentPop.maxHumidity < humidity)
 				continue;
 			else if(lastPop == defaultPop) {
 				lastPop = currentPop;
 				tempRange = lastPop.maxTemperature - lastPop.minTemperature;
 				humidityRange = lastPop.maxHumidity - lastPop.minHumidity;
 				continue;
 			}
 			else if(tempRange <= (currentPop.maxTemperature - currentPop.minTemperature))
 				continue;
 			else if(humidityRange <= (currentPop.maxHumidity - currentPop.minHumidity))
 				continue;
 
 			lastPop = currentPop;
 			tempRange = lastPop.maxTemperature - lastPop.minTemperature;
 			humidityRange = lastPop.maxHumidity - lastPop.minHumidity;
 		}
 	}
 
 	public double getTemperature(World world, int x, int z) {
 		if(useCustomMetrics)
 			return getBiomeNoise(world, x, z, false);
 
 		return world.getTemperature(x, z);
 	}
 
 	public boolean isNearEdge(World world, int x, int z, int period, int count) {
 		if(world == null)
 			return false;
 
 		getPopulator(world, x, z);
 
 		if(lastPop == null)
 			return false;
 
 		tempPop = lastPop;
 		edgeMax = period * count;
 
 		for(edgeXPos = (x - edgeMax); edgeXPos <= (x + edgeMax); edgeXPos += period) {
 			for(edgeZPos = (z - edgeMax); edgeZPos <= (z + edgeMax); edgeZPos += period) {
 				getPopulator(world, edgeXPos, edgeZPos);
 
 				if(lastPop != tempPop)
 					return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isUsingCustomBiomeMetrics() {
 		return useCustomMetrics;
 	}
 
 	public void populate(World world, Random random, Chunk source) {
 		xPos = source.getX() * 16;
 		zPos = source.getZ() * 16;
 
 		for(currentX = 0; currentX < 16; currentX++) {
 			cXPos = currentX + xPos;
 			for(currentZ = 0; currentZ < 16; currentZ++) {
 				cZPos = currentZ + zPos;
 				getPopulator(world, cXPos, cZPos);
 
 				if(lastPop == null)
 					return;
 
 				lastPop.populate(world, random, source, cXPos, cZPos);
 			}
 		}
 	}
 
 	public PandoraPopulator setDefaultBiome(PandoraBiomePopulator biome) {
 		if(biome == null)
 			return this;
 
 		defaultPop = biome;
 		return this;
 	}
 
 	public void setUseCustomBiomeMetrics(boolean setting) {
 		setUseCustomBiomeMetrics(setting, 100D, 1D, 2, 100D, 15D);
 	}
 	
 	public void setUseCustomBiomeMetrics(boolean setting, double range, double scale, int octaves, double amplitude, double frequency) {
 		this.useCustomMetrics = setting;
 		this.range = range;
 		this.scale = scale;
 		this.octaves = octaves;
 		this.amplitude = amplitude;
 		this.frequency = (frequency / 1000D);
 	}
 }

