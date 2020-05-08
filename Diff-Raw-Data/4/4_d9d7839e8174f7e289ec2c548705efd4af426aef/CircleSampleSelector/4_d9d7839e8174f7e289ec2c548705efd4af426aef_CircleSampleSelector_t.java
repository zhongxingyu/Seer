 package com.lincbio.lincxmap.dip;
 
 import java.io.Serializable;
 
 
 public class CircleSampleSelector extends SampleSelector implements
 		Serializable {
 	private static final long serialVersionUID = 8718955066112977963L;
 
 	public float radius;
 
 	public CircleSampleSelector(float x, float y, float r) {
 		this.x = x;
 		this.y = y;
 		this.radius = r;
 	}
 
 	public final boolean contains(float x, float y) {
 		return new Point((int) x, (int) y).distance(this.x, this.y) <= this.radius;
 	}
 
 	@Override
 	public Rectangle getBounds() {
		return new Rectangle.Float(this.x - this.radius, this.y
				- this.radius, this.radius * 2, this.radius * 2);
 	}
 }
