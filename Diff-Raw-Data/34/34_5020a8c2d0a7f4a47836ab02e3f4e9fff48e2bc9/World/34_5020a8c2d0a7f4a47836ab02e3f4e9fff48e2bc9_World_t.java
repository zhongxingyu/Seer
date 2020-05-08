 package com.clusterflux.concentric;
 
 import java.math.*;
 import java.util.*;
 import java.io.Serializable;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.FileOutputStream;
 import android.content.Context;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import android.util.Log;
 
 public class World implements Serializable {
 
 	public String world_name;
 	public int world_width;
 	public int world_height;
 	public int[][] world_map;
 	
 	public World(String world_name, int world_width, int world_height) {
 
 		//set the world attributes
 		this.world_name = world_name;
 		this.world_width = world_width;
 		this.world_height = world_height;
 		
 		//generate the map
 		world_map = createWorldMap(world_width, world_height);
 	
 	}
 	
 	public World(Context context, String world_name) throws IOException, ClassNotFoundException {
 		
 		//load world from file
 		FileInputStream fis = context.openFileInput(world_name);
 		ObjectInputStream ois = new ObjectInputStream(fis);
 		World world = (World)ois.readObject();
 		
 		//set the world attributes
 		this.world_name = world.world_name;
 		this.world_width = world.world_width;
 		this.world_height = world.world_height;
 		this.world_map = world.world_map;
 		
 	}
 	
 	private int[][] createWorldMap(int world_width, int world_height) {
 	
 		//create a local tile map 
 		int[][] world_map = new int[world_width][world_height];
 		
 		float[][] white_noise = generateWhiteNoise(world_width, world_height);
 		float[][] perlin = generatePerlinNoise(white_noise, 6);
 		
 		//get a randomizer to fill the array with - {temporary solution}
 		/*Random rand = new Random();*/
 
 		//fill the tile map array with random numbers between 0 and 2
 		
 		for (int row = 0; row < world_map.length; row++) {
 			for (int col = 0; col < world_map[row].length; col++) {
 				Log.d("LOGCAT", "perlin" + perlin[row][col]);
				if (perlin[row][col] < 0.15) {
					world_map[row][col] = 3;
 				}
 				
				if (perlin[row][col] > 0.15 && perlin[row][col] < 0.35) {
					world_map[row][col] = 0;
				}
				if (perlin[row][col] > 0.35 && perlin[row][col] < 0.75) {
 					world_map[row][col] = 1;
 				}
				if (perlin[row][col] > 0.75) {
 					world_map[row][col] = 2;
 				}
 				//world_map[row][col] = rand.nextInt(3);  //static number, needs variable!
 														//3 is the number of tile types
 			}
 		}
 		
 		return world_map;
 		
 	}	
 	
 	public void saveWorld(Context context, String world_name, World world) throws IOException {
 		
 		//save world object to file named world_name
 		FileOutputStream fos = context.openFileOutput(world_name, Context.MODE_PRIVATE);
 		ObjectOutputStream oos = new ObjectOutputStream(fos);
 		oos.writeObject(world);
 		oos.close();
 		
 	}
 	
 	public float[][] generateWhiteNoise(int world_width, int world_height) {
 	
 		Random rand = new Random(0); //seed to 0 for testing
 		float[][] whiteNoise = new float[world_width][world_height];
 		
 		for (int i = 0; i < world_width; i++) {
 		
 			for (int j = 0; j < world_height; j++ ) {
 			
 				whiteNoise[i][j] = (float)rand.nextDouble() % 1;
 				
 			}
 			
 		}
 		
 		return whiteNoise;
 		
 	}
 	
 	public float[][] generateSmoothNoise(float[][] baseNoise, int octave) {
 	
 		int world_width = baseNoise.length;
 		int world_height = baseNoise[0].length;
 		
 		float[][] smoothNoise = new float[world_width][world_height];
 		
 		int samplePeriod = 1 << octave; //bitwise left shift to get 1/2^octave
 		float sampleFrequency = 1.0f / samplePeriod;
 		
 		for (int i = 0; i < world_width; i++) {
 		
 			//calculate horizontal sampling dependencies
 			int sample_i0 = (i / samplePeriod) * samplePeriod;
 			int sample_i1 = (sample_i0 + samplePeriod) % world_width; //wrap around
 			float horizontal_blend = (i - sample_i0) * sampleFrequency;
 			
 			for (int j = 0; j < world_height; j++) {
 			
 				//calculate vertical sampling dependencies
 				int sample_j0 = (j / samplePeriod) * samplePeriod;
 				int sample_j1 = (sample_j0 + samplePeriod) % world_width; //wrap around
 				float vertical_blend = (j - sample_j0) * sampleFrequency;
 				
 				//blend the top two corners
 				float top = Interpolate(baseNoise[sample_i0][sample_j0], baseNoise[sample_i1][sample_j0], horizontal_blend);
 				
 				//blend the bottom two corners
 				float bottom = Interpolate(baseNoise[sample_i0][sample_j1], baseNoise[sample_i1][sample_j1], horizontal_blend);
 				
 				//final blend
 				smoothNoise[i][j] = Interpolate(baseNoise[sample_i0][sample_j1], baseNoise[sample_i1][sample_j1], horizontal_blend);
 				
 			}
 			
 		}
 		
 		return smoothNoise;
 	
 	}
 	
 	float Interpolate(float x0, float x1, float alpha) {
 	
 		return x0 * (1 - alpha) + alpha * x1;
 	
 	}
 	
 	float[][] generatePerlinNoise(float[][] baseNoise, int octaveCount) {
 	
 		int world_width = baseNoise.length;
 		int world_height = baseNoise[0].length;
 		
 		float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2d arrays
 		
 		float persistance = 0.5f;
 		
 		//generate smooth noise
 		for (int i = 0; i < octaveCount; i++) {
 	
 			smoothNoise[i] = generateSmoothNoise(baseNoise, i);
 		
 		}
 	
 		float[][] perlinNoise = new float[world_width][world_height];
 		float amplitude = 1.0f;
 		float totalAmplitude = 0.0f;
 		
 		//blend noise together
 		for (int octave = octaveCount - 1; octave >= 0; octave--) {
 		
 			amplitude *= persistance;
 			totalAmplitude += amplitude;
 			
 			for (int i = 0; i < world_width; i++) {
 			
 				for (int j = 0; j < world_height; j++) {
 				
 					perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
 					
 				}
 				
 			}
 			
 		}
 		
 		//normalization
 		for (int i = 0; i < world_width; i++) {
 		
 			for (int j = 0; j < world_height; j++) {
 			
 				perlinNoise[i][j] /= totalAmplitude;
 				
 			}
 			
 		}
 		
 		return perlinNoise;
 		
 	}
 	
 	
 
 	
 }
