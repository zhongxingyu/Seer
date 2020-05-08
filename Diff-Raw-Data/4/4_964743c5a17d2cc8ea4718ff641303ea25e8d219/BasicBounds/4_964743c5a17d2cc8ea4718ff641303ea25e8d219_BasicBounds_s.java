 /* Chronos - Game Development Toolkit for Java game developers. The
  * original source remains:
  * 
  * Copyright (c) 2013 Miguel Gonzalez http://my-reality.de
  * 
  * This source is provided under the terms of the BSD License.
  * 
  * Copyright (c) 2013, Chronos
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the following 
  * conditions are met:
  * 
  *  * Redistributions of source code must retain the above 
  *    copyright notice, this list of conditions and the 
  *    following disclaimer.
  *  * Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution.
  *  * Neither the name of the Chronos/my Reality Development nor the names of 
  *    its contributors may be used to endorse or promote products 
  *    derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
  * OF SUCH DAMAGE.
  */
 package de.myreality.chronos.models;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import de.myreality.chronos.util.ROVector3f;
 import de.myreality.chronos.util.Vector3f;
 import de.myreality.chronos.util.VectorUtils;
 
 /**
  * Basic implementation of a bounds class. This implementation provides
  * alignment to another position vector by an internal vector implementation.
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 0.8alpha
  * @version 0.8alpha
  */
 public class BasicBounds extends BasicPositionable<Bounds> implements Bounds {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final long serialVersionUID = -8809555037858114766L;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private ROVector3f[] data, originalData;
 
 	private float scale, rotation;
 	
 	// Is used to ignore single and general alignment of positioning (performance)
 	private boolean ignoreSingleAdjustment = false, ignoreAdjustment = false;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	/**
 	 * Default constructor
 	 */
 	public BasicBounds() {
		this(null, null);
 	}
 
 	public BasicBounds(ROVector3f topLeft, ROVector3f bottomRight) {
 		this(topLeft, bottomRight, 0f);
 	}
 
 	public BasicBounds(ROVector3f topLeft, ROVector3f bottomRight,
 			float rotation) {
 		this(topLeft, bottomRight, rotation, 1f);
 	}
 
 	public BasicBounds(ROVector3f topLeft, ROVector3f bottomRight,
 			float rotation, float scale) {
 		set(topLeft, bottomRight, rotation, scale);
 	}
 
 	public BasicBounds(float topLeftX, float topLeftY, float bottomRightX,
 			float bottomRightY, float rotation, float scale) {
 		set(topLeftX, topLeftY, bottomRightX, bottomRightY, rotation, scale);
 	}
 
 	public BasicBounds(float topLeftX, float topLeftY, float bottomRightX,
 			float bottomRightY, float rotation) {
 		this(topLeftX, topLeftY, bottomRightX, bottomRightY, rotation, 1f);
 	}
 
 	public BasicBounds(float topLeftX, float topLeftY, float bottomRightX,
 			float bottomRightY) {
 		this(topLeftX, topLeftY, bottomRightX, bottomRightY, 0f, 1f);
 	}
 
 	public BasicBounds(ROVector3f[] vertices, float rotation, float scale) {
 		set(vertices, rotation, scale);
 	}
 
 	public BasicBounds(ROVector3f[] vertices, float rotation) {
 		this(vertices, rotation, 1f);
 	}
 
 	public BasicBounds(ROVector3f[] vertices) {
 		this(vertices, 0f);
 	}
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 	
 	private void setIgnoreSingleAdjustment(boolean value) {
 		ignoreSingleAdjustment = value;
 	}
 	
 	private void setIgnoreAdjustment(boolean value) {
 		ignoreAdjustment = value;
 	}
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Iterable#iterator()
 	 */
 	@Override
 	public Iterator<ROVector3f> iterator() {
 		return Arrays.asList(data).iterator();
 	}
 
 	@Override
 	public ROVector3f get(Edge edge) {
 		return get(edge, true);
 	}
 
 	@Override
 	public ROVector3f get(Edge edge, boolean rotated) {
 		return get(edge.getIndex(), rotated);
 	}
 
 	@Override
 	public ROVector3f get(int index) {
 		return get(index, false);
 	}
 
 	@Override
 	public ROVector3f get(int index, boolean rotated) {
 		if (rotated) {
 			return data[index].copy();
 		} else {
 			return originalData[index].copy();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#clear()
 	 */
 	@Override
 	public void clear() {
 		
 		if (data != null) {
 			for (ROVector3f v : data) {
 				v.setX(0f);
 				v.setY(0f);
 				v.setZ(0f);
 			}
 		}
 
 		if (originalData != null) {
 			for (ROVector3f v : originalData) {
 				v.setX(0f);
 				v.setY(0f);
 				v.setZ(0f);
 			}
 		}
 
 		setRotation(0f);
 		setScale(1f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#copy(de.myreality.chronos.models.Bounds
 	 * )
 	 */
 	@Override
 	public void copy(Bounds original) {
 		set(original.get(Edge.TOP_LEFT), original.get(Edge.BOTTOM_RIGHT),
 				original.getRotation());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#set(float, float, float, float)
 	 */
 	@Override
 	public void set(float topLeftX, float topLeftY, float bottomRightX,
 			float bottomRightY) {
 		set(topLeftX, topLeftY, bottomRightX, bottomRightY, 0f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#set(float, float, float, float,
 	 * float)
 	 */
 	@Override
 	public void set(float topLeftX, float topLeftY, float bottomRightX,
 			float bottomRightY, float rotation) {
 		set(topLeftX, topLeftY, bottomRightX, bottomRightY, rotation, 1f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#set(float, float, float, float,
 	 * float, float)
 	 */
 	@Override
 	public void set(float topLeftX, float topLeftY, float bottomRightX,
 			float bottomRightY, float rotation, float scale) {
 
 		if (data == null) {
 			data = generateBounds();
 		}
 
 		if (originalData == null) {
 			originalData = generateBounds();
 		}
 
 		this.rotation = rotation;
 		this.scale = scale;
 		
 		applyBounds(topLeftX, topLeftY, bottomRightX, bottomRightY, data, rotation, scale);
 		applyBounds(topLeftX, topLeftY, bottomRightX, bottomRightY, originalData, 0f, 1f);
 		
 		// Apply the new position
 		setIgnoreAdjustment(true);
 		setPosition(topLeftX, topLeftY);
 		setIgnoreAdjustment(false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#set(de.myreality.chronos.util.ROVector3f
 	 * , de.myreality.chronos.util.ROVector3f)
 	 */
 	@Override
 	public void set(ROVector3f topLeft, ROVector3f bottomRight) {
 		set(topLeft, bottomRight, 0f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#set(de.myreality.chronos.util.ROVector3f
 	 * , de.myreality.chronos.util.ROVector3f, float)
 	 */
 	@Override
 	public void set(ROVector3f topLeft, ROVector3f bottomRight, float rotation) {
 		set(topLeft, bottomRight, rotation, 1f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#set(de.myreality.chronos.util.ROVector3f
 	 * , de.myreality.chronos.util.ROVector3f, float, float)
 	 */
 	@Override
 	public void set(ROVector3f topLeft, ROVector3f bottomRight, float rotation,
 			float scale) {
 		if (topLeft != null && bottomRight != null) {
 			set(topLeft.getX(), topLeft.getY(), bottomRight.getX(),
 					bottomRight.getY(), rotation, scale);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#set(de.myreality.chronos.util.ROVector3f
 	 * [], float, float)
 	 */
 	@Override
 	public void set(ROVector3f[] vertices, float rotation, float scale) {
 		ROVector3f topLeft = new Vector3f();
 		ROVector3f bottomRight = new Vector3f();
 		VectorUtils.calculateBounds(topLeft, bottomRight, vertices);
 		set(topLeft, bottomRight, rotation, scale);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#set(de.myreality.chronos.util.ROVector3f
 	 * [], float)
 	 */
 	@Override
 	public void set(ROVector3f[] vertices, float rotation) {
 		set(vertices, rotation, 1f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.myreality.chronos.models.Bounds#set(de.myreality.chronos.util.ROVector3f
 	 * [])
 	 */
 	@Override
 	public void set(ROVector3f[] vertices) {
 		set(vertices, 0f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#getWidth()
 	 */
 	@Override
 	public float getWidth() {
 		return (float) VectorUtils.getDistanceBetween(get(Edge.TOP_LEFT, false),
 				get(Edge.TOP_RIGHT, false));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#getHeight()
 	 */
 	@Override
 	public float getHeight() {
 		return (float) VectorUtils.getDistanceBetween(get(Edge.TOP_LEFT, false),
 				get(Edge.BOTTOM_LEFT, false));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#getRotation()
 	 */
 	@Override
 	public float getRotation() {
 		return rotation;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#setRotation(float)
 	 */
 	@Override
 	public void setRotation(float rotation) {
 		// Reduce the angle and force it to be the positive remainder,
 		// so that 0 <= angle < 360
 		this.rotation = (float) (((rotation % 360.0) + 360.0) % 360.0);
 		
 		// Apply rotation to the bounds
 		if (originalData != null) {
 			ROVector3f topLeft = get(Edge.TOP_LEFT, false);
 			ROVector3f bottomRight = get(Edge.BOTTOM_RIGHT, false);			
 			set(topLeft, bottomRight, getRotation(), getScale());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#rotate(float)
 	 */
 	@Override
 	public void rotate(float angle) {
 		setRotation(getRotation() + angle);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#getScale()
 	 */
 	@Override
 	public float getScale() {
 		return scale;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#setScale(float)
 	 */
 	@Override
 	public void setScale(float scale) {
 		this.scale = Math.abs(scale);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.myreality.chronos.models.Bounds#scale(float)
 	 */
 	@Override
 	public void scale(float factor) {
 		setScale(getScale() * factor);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Bounds) {
 			Bounds other = (Bounds) obj;
 
 			return getRotation() == other.getRotation()
 					&& getScale() == other.getScale()
 					&& getHeight() == other.getHeight()
 					&& getWidth() == other.getWidth()
 					&& get(Edge.TOP_LEFT).equals(other.get(Edge.TOP_LEFT))
 					&& get(Edge.TOP_RIGHT).equals(other.get(Edge.TOP_RIGHT))
 					&& get(Edge.BOTTOM_LEFT)
 							.equals(other.get(Edge.BOTTOM_LEFT))
 					&& get(Edge.BOTTOM_RIGHT).equals(
 							other.get(Edge.BOTTOM_RIGHT));
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return get(Edge.TOP_LEFT) + "  " + get(Edge.TOP_RIGHT) + "  "
 				+ get(Edge.BOTTOM_RIGHT) + "  " + get(Edge.BOTTOM_LEFT);
 	}
 
 	@Override
 	public void setX(float x, Coordinate coord) {
 		applyPosition(getX(coord), x, coord, PositionType.X);
 		super.setX(x, coord);
 	}
 
 	@Override
 	public void setY(float y, Coordinate coord) {
 		applyPosition(getX(coord), y, coord, PositionType.Y);
 		super.setY(y, coord);		
 	}
 
 	@Override
 	public void setZ(float z, Coordinate coord) {
 		applyPosition(getX(coord), z, coord, PositionType.Z);
 		super.setZ(z, coord);		
 	}
 	
 	@Override
 	public void setPosition(float x, float y, float z, Coordinate coord) {
 		
 		float oldX = getX(coord), oldY = getY(coord), oldZ = getZ(coord);
 		
 		if (!ignoreAdjustment) {
 			applyAllPositions(oldX, oldY, oldZ, x, y, z, coord);
 		}
 		
 		setIgnoreSingleAdjustment(true);
 		super.setPosition(x, y, z, coord);
 		setIgnoreSingleAdjustment(false);
 		
 		
 	}
 	
 
 
 	@Override
 	public float getCenterX(Coordinate coord) {		
 		return getX(coord) + getWidth() / 2f;		
 		
 	}
 
 	@Override
 	public float getCenterX() {
 		return getCenterX(Coordinate.GLOBAL);
 	}
 
 	@Override
 	public float getCenterY(Coordinate coord) {
 		return getY(coord) + getHeight() / 2f;
 	}
 
 	@Override
 	public float getCenterY() {
 		return getCenterY(Coordinate.GLOBAL);
 	}
 
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	/**
 	 * Generates a fresh bound array
 	 * 
 	 * @return a new valid bound array
 	 */
 	private ROVector3f[] generateBounds() {
 		ROVector3f[] bounds = new ROVector3f[COUNT];
 
 		for (int index = 0; index < bounds.length; ++index) {
 			bounds[index] = new Vector3f();
 		}
 
 		return bounds;
 	}
 	
 	private void applyPosition(float oldValue, float newValue, Coordinate coord, PositionType type) {
 		float oldX = getX(coord);
 		float oldY = getY(coord);
 		float oldZ = getZ(coord);
 		float newX = getX(coord);
 		float newY = getY(coord);
 		float newZ = getZ(coord);
 		
 		switch (type) {
 			case X:
 				oldX = oldValue;
 				newX = newValue;
 				break;
 			case Y:
 				oldY = oldValue;
 				newY = newValue;
 				break;
 			case Z:
 				oldZ = oldValue;
 				newZ = newValue;
 				break;		
 		}
 		
 		applyAllPositions(oldX, oldY, oldZ, newX, newY, newZ, coord);
 	}
 	
 	private void applyAllPositions(float oldX, float oldY, float oldZ, float newX, float newY, float newZ, Coordinate coord) {
 		if (!ignoreSingleAdjustment) {
 			float diffX = oldX - newX;
 			float diffY = oldY - newY;
 			float diffZ = oldZ - newZ;
 			
 			for (int i = 0; i < COUNT; ++i) {
 				ROVector3f dataVector = data[i];
 				ROVector3f origVector = originalData[i];
 				dataVector.setX(dataVector.getX() + diffX);
 				origVector.setX(origVector.getX() + diffX);
 				dataVector.setY(dataVector.getY() + diffY);
 				origVector.setY(origVector.getY() + diffY);
 				dataVector.setZ(dataVector.getZ() + diffZ);
 				origVector.setZ(origVector.getZ() + diffZ);
 			}
 		}
 	}
 	
 	
 	private void applyBounds(float topLeftX, float topLeftY, float bottomRightX, float bottomRightY, ROVector3f[] data, float rotation, float scale) {
 		ROVector3f topLeft = data[Edge.TOP_LEFT.getIndex()];
 		ROVector3f topRight = data[Edge.TOP_RIGHT.getIndex()];
 		ROVector3f bottomLeft = data[Edge.BOTTOM_LEFT.getIndex()];
 		ROVector3f bottomRight = data[Edge.BOTTOM_RIGHT.getIndex()];
 		
 		topLeft.setX(topLeftX); topLeft.setY(topLeftY);
 		topRight.setX(bottomRightX); topRight.setY(topLeftY);
 		bottomRight.setX(bottomRightX); bottomRight.setY(bottomRightY);
 		bottomLeft.setX(topLeftX); bottomLeft.setY(bottomRightY);
 		
 		if (rotation != 0f) {
 			VectorUtils.rotate(getCenterX(), getCenterY(), topLeft, rotation);
 			VectorUtils.rotate(getCenterX(), getCenterY(), topRight, rotation);
 			VectorUtils.rotate(getCenterX(), getCenterY(), bottomLeft, rotation);
 			VectorUtils.rotate(getCenterX(), getCenterY(), bottomRight, rotation);
 		}
 	}
 	
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 }
