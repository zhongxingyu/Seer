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
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.myreality.chronos.util.ROVector3f;
 import de.myreality.chronos.util.Vector3f;
 
 /**
  * Test case for bounds
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 0.8alpha
  * @version 0.8alpha
  */
 public class BoundsTest {
 
 	Bounds vb, fb;
 
 	static final float RADIUS = 100f;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {		
 		ROVector3f[] vertices = new ROVector3f[] { new Vector3f(-RADIUS, -RADIUS),
 				new Vector3f(-RADIUS / 2, -RADIUS / 2),
 				new Vector3f(RADIUS / 5, RADIUS / 4), new Vector3f(0, RADIUS / 6),
 				new Vector3f(RADIUS, RADIUS) };
 		vb = new BasicBounds(vertices);
 		fb = new BasicBounds(-RADIUS, -RADIUS, RADIUS, RADIUS);
 	}
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	/**
 	 * Test method for
 	 * {@link de.myreality.chronos.models.Bounds#get(de.myreality.chronos.models.Edge)}
 	 * .
 	 */
 	@Test
 	public void testGetEdge() {
 
 		// Initialization
 
 		ROVector3f topLeftVertices = vb.get(Edge.TOP_LEFT);
 		ROVector3f bottomLeftVertices = vb.get(Edge.BOTTOM_LEFT);
 		ROVector3f bottomRightVertices = vb.get(Edge.BOTTOM_RIGHT);
 		ROVector3f topRightVertices = vb.get(Edge.TOP_RIGHT);
 
 		ROVector3f topLeftFloating = fb.get(Edge.TOP_LEFT);
 		ROVector3f bottomLeftFloating = fb.get(Edge.BOTTOM_LEFT);
 		ROVector3f bottomRightFloating = fb.get(Edge.BOTTOM_RIGHT);
 		ROVector3f topRightFloating = fb.get(Edge.TOP_RIGHT);
 
 		// Testing
 
 		assertTrue("The both bounds should be the same.",
 				vb.equals(fb));
 
 		boolean topLeftCondition = topLeftVertices.equals(topLeftFloating)
 				&& topLeftVertices.getX() == -RADIUS
 				&& topLeftVertices.getY() == -RADIUS;
 		
 		assertTrue("Top left bound does not match. " + topLeftVertices, topLeftCondition);
 
 		boolean bottomLeftCondition = bottomLeftVertices.equals(bottomLeftFloating)
 				&& bottomLeftVertices.getX() == -RADIUS
 				&& bottomLeftVertices.getY() == RADIUS;
 		
 		assertTrue("Bottom left bound does not match." + bottomLeftVertices, bottomLeftCondition);
 
 		boolean bottomRightCondition = bottomRightVertices.equals(bottomRightFloating)
 				&& bottomRightVertices.getX() == RADIUS
 				&& bottomRightVertices.getY() == RADIUS;
 		
 		assertTrue("Bottom right bound does not match." + bottomRightVertices, bottomRightCondition);
 
 		boolean topRightCondition = topRightVertices.equals(topRightFloating)
 				&& topRightVertices.getX() == RADIUS
 				&& topRightVertices.getY() == -RADIUS;
 		
 		assertTrue("Top right bound does not match." + topRightVertices, topRightCondition);
		
		
		vb.setPosition(10f, 10f);
		
		assertTrue("global X position should be 10f instead of " + vb.get(Edge.TOP_LEFT).getX(), vb.get(Edge.TOP_LEFT).getX() == 10f);
		assertTrue("global Y position should be 10f instead of " + vb.get(Edge.TOP_LEFT).getY(), vb.get(Edge.TOP_LEFT).getY() == 10f);
		assertTrue("global X position should be " + vb.getX() + " instead of " + vb.get(Edge.TOP_LEFT).getX(), vb.get(Edge.TOP_LEFT).getX() == vb.getX());
		assertTrue("global Y position should be " + vb.getY() + " instead of " + vb.get(Edge.TOP_LEFT).getY(), vb.get(Edge.TOP_LEFT).getY() == vb.getY());
		
 	}
 
 	/**
 	 * Test method for {@link de.myreality.chronos.models.Bounds#clear()}.
 	 */
 	@Test
 	public void testClear() {
 		
 		ROVector3f expected = new Vector3f();
 		
 		vb.clear();
 		fb.clear();
 		
 		for (ROVector3f v : vb) {
 			assertTrue("Bound vector should be at position [0|0|0]", v.equals(expected));
 		}
 	}
 
 	/**
 	 * Test method for {@link de.myreality.chronos.models.Bounds#getWidth()}.
 	 */
 	@Test
 	public void testGetWidth() {
 		
 		final float WIDTH = RADIUS * 2;
 		
 		assertTrue("The width of the vertices bounds does not match.", vb.getWidth() == WIDTH);
 		assertTrue("The width of the floating bounds does not match.", fb.getWidth() == WIDTH);
 	}
 
 	/**
 	 * Test method for {@link de.myreality.chronos.models.Bounds#getHeight()}.
 	 */
 	@Test
 	public void testGetHeight() {
 		
 		final float HEIGHT = RADIUS * 2;
 		
 		assertTrue("The height of the vertices bounds does not match." + vb.getHeight() + "!=" + HEIGHT, vb.getHeight() == HEIGHT);
 		assertTrue("The height of the floating bounds does not match." + fb.getHeight() + "!=" + HEIGHT, fb.getHeight() == HEIGHT);
 	}
 
 	/**
 	 * Test method for
 	 * {@link de.myreality.chronos.models.Bounds#setRotation(float)}.
 	 */
 	@Test
 	public void testSetRotation() {
 		
 		assertTrue("Rotation should be 0f", vb.getRotation() == 0f);
 		
 		ROVector3f oldTopLeft = vb.get(Edge.TOP_LEFT);
 		ROVector3f oldTopRight = vb.get(Edge.TOP_RIGHT);
 		ROVector3f oldBottomLeft = vb.get(Edge.BOTTOM_LEFT);
 		ROVector3f oldBottomRight = vb.get(Edge.BOTTOM_RIGHT);		
 		
 		vb.setRotation(90f);
 				
 		assertTrue("Rotation should be 90f", vb.getRotation() == 90f);
 		
 		assertTrue("The old top left should be the new top right." + oldTopLeft + "!=" + oldTopRight, vb.get(Edge.TOP_LEFT).equals(oldTopRight));
 		assertTrue("The old top right should be the new bottom right", vb.get(Edge.TOP_RIGHT).equals(oldBottomRight));
 		assertTrue("The old bottom right should be the new bottom left", vb.get(Edge.BOTTOM_RIGHT).equals(oldBottomLeft));
 		assertTrue("The old bottom left should be the new top left", vb.get(Edge.BOTTOM_LEFT).equals(oldTopLeft));
 		
 		
 	}
 
 	/**
 	 * Test method for {@link de.myreality.chronos.models.Bounds#rotate(float)}.
 	 */
 	@Test
 	public void testRotate() {
 		vb.rotate(300f);
 		
 		assertTrue("Rotation should be 300", vb.getRotation() == 300f);
 		
 		vb.rotate(70f);
 		
 		assertTrue("Rotation should be 10f", vb.getRotation() == 10f);
 		
 		vb.rotate(-20f);
 		
 		assertTrue("Rotation should be 350f instead of " + vb.getRotation(), vb.getRotation() == 350f);
 		
 		
 		Bounds parent = new BasicBounds();
 		parent.setPosition(1f, 1f);
		vb.setPosition(2f, 1f);
 		vb.attachTo(parent);
 		
 		assertTrue("Parent rotation should be 0f", parent.getRotation() == 0f);
 		assertTrue("Parent x position should be 0", parent.getX() == 1f);
 		assertTrue("Parent y position should be 0", parent.getY() == 1f);

 		parent.rotate(90f);
 		
 		assertTrue("Parent rotation should be 90.0", parent.getRotation() == 90f);
 		
 		assertTrue("Current rotation should be 80.0 instead of " + vb.getRotation(), vb.getRotation() == 80f);
 		assertTrue("Current x position should be 1 instead of " + vb.getX(), vb.getX() == 1f);
 		assertTrue("Current y position should be 2 instead of " + vb.getY(), vb.getY() == 2f);
 		
 	}
 
 	/**
 	 * Test method for
 	 * {@link de.myreality.chronos.models.Bounds#setScale(float)}.
 	 */
 	@Test
 	public void testSetScale() {
 		vb.setScale(2f);		
 		assertTrue("Scale should be 2f", vb.getScale() == 2f);
 		
 		// TODO: Implement scaling of bounds
 	}
 
 	/**
 	 * Test method for {@link de.myreality.chronos.models.Bounds#scale(float)}.
 	 */
 	@Test
 	public void testScale() {
 		vb.setScale(2f);
 		vb.scale(-2f);
 		assertTrue("Scale should be 4f", vb.getScale() == 4f);
 	}
 
 }
