 package com.github.rocketsurgery;
 
import java.util.Random;

 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.geom.Line;
 import org.newdawn.slick.geom.Shape;
 
 @SuppressWarnings("serial")
 public class Wire extends Line {
 
 	private Node end1, end2;
 	
 	private Color wireColor = Color.red;
 	private float wireWidth = 5f;
 	
 	private static int colorIterator = 0;
 	
 	public Wire(Node first, Node second) {
 		super(first.getCenterX(), first.getCenterY(), second.getCenterX(), second.getCenterY());
 		first.attach(this);
 		second.attach(this);
 		this.end1 = first;
 		this.end2 = second;
 		
 		/*Random rand = new Random();
 		int colorIndex = rand.nextInt(3);*/		
 		
 		switch (colorIterator) {
 		case 0: wireColor = Color.red; break;
 		case 1: wireColor = Color.blue; break;
 		case 2: wireColor = Color.green; break;
 		}
 		
 		colorIterator++;
 		colorIterator %= 3;
 		
 		//wireColor = new Color ((float)Math.random(),(float)Math.random(),(float)Math.random());
 	}
 	
 	public void resetEnds() {
 		this.set(end1.getCenterX(), end1.getCenterY(), end2.getCenterX(), end2.getCenterY());
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.setColor(wireColor);
 		g.setLineWidth(wireWidth);
 		g.setAntiAlias(true);
 		g.drawLine(end1.getCenterX(), end1.getCenterY(), end2.getCenterX(), end2.getCenterY());
 		g.fillOval(end1.getCenterX() - wireWidth * 3 / 2, end1.getCenterY() - wireWidth * 3 / 2, wireWidth * 3, wireWidth * 3);
 		g.fillOval(end2.getCenterX() - wireWidth * 3 / 2, end2.getCenterY() - wireWidth * 3 / 2, wireWidth * 3, wireWidth * 3);
 		
 	}
 	
 	@Override
 	public boolean intersects(Shape shape) {
 		if (!(shape instanceof Wire))
 			throw new IllegalArgumentException();
 		Wire wire = (Wire) shape;
 		if (super.intersects(wire)) {
 			return !(wire.hasEnd(end1) || wire.hasEnd(end2));
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean equals(Object object) {
 		if (!(object instanceof Wire))
 			throw new IllegalArgumentException();
 		Wire other = (Wire) object;
 		return other.hasEnds(end1, end2);
 	}
 	
 	public boolean hasEnd(Node node) {
 		return end1 == node || end2 == node;
 	}
 	
 	public boolean hasEnds(Node n1, Node n2) {
 		return (end1 == n1 && end2 == n2) || (end1 == n2 && end2 == n1);
 	}
 	
 }
