 /**
  * Copyright (c) 2009, Coral Reef Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of the Coral Reef Project nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package pl.graniec.coralreef.geometry;
 
 import junit.framework.TestCase;
 
 /**
  * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
  *
  */
 public class SegmentTest extends TestCase {
 
 	public void setUp() throws Exception {
 	}
 
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link pl.graniec.coralreef.geometry.Segment#intersectionPoint(pl.graniec.coralreef.geometry.Segment)}.
 	 */
 	public void testIntersectionPoint1() {
 		final Segment s1 = new Segment(-5, 0, 5, 0);
 		final Segment s2 = new Segment(0, -5, 0, 5);
 		
 		final Point2 iPoint = s1.intersectionPoint(s2);
 		
 		assertNotNull(iPoint);
 		
 		assertEquals(0f, iPoint.x, 0.001);
 		assertEquals(0f, iPoint.y, 0.001);
 	}
 	
 	public void testIntersectionPoint2() {
		// point on line test
 		final Segment s1 = new Segment(0, -5, 0, 5);
		final Segment s2 = new Segment(-5, 5, 5, 5);
 		
 		final Point2 iPoint = s1.intersectionPoint(s2);
 		
 		assertNotNull(iPoint);
 		
 		assertEquals(0f, iPoint.x, 0.001);
 		assertEquals(5f, iPoint.y, 0.001);
 	}
 	
 	public void testIntersectionPoint3() {
 		final Segment s1 = new Segment(-5, 6, 5, 6);
 		final Segment s2 = new Segment(0, -5, 0, 5);
 		
 		final Point2 iPoint = s1.intersectionPoint(s2);
 		
 		assertNull(iPoint);
 	}
 
 }
