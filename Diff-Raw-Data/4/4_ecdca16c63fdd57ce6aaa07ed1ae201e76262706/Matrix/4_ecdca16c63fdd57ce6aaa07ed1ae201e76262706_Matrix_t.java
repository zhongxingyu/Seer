 package com.github.haw.ai.gkap.graph;
 
 public class Matrix implements Cloneable {
 	final private int width;
 	final private int height;
 	private Double[] values;
 	
 	public Matrix(int width, int height) {
 		this(width, height, new Double[width*height]);
 	}
 	
 	public Matrix(int width, int height, Double[] values) {
 		super();
 		this.width = width;
 		this.height = height;
 		this.values = values;		
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	
 	public Double get(int x, int y) {
 		if (x < 0 || x > this.getWidth() || y < 0 || y > this.getHeight()) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		return values[x + (y*getWidth())];
 	}
 	public void set(int x, int y, Double value) {
 		if (x < 0 || x > this.getWidth() || y < 0 || y > this.getHeight()) {
 			throw new ArrayIndexOutOfBoundsException();
 		}		
 		this.values[x + (y*getWidth())] = value;
 	}
 	
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		for (int y = 0; y < getHeight(); y++) {
 			for (int x = 0; x < getWidth(); x++) {
 				result.append(get(x,y));
 				result.append(" ");
 			}
 			result.append("\n");
 		}
 		return result.toString();
 	}
 	
 	public Matrix clone() {
 		return new Matrix(this.getWidth(), this.getHeight(), this.values);
 	}
 
 	public void clear() {
		for (int i = 0; i < values.length; i++) {
			values[i] = 0.0;
 		}
 	}
 }
