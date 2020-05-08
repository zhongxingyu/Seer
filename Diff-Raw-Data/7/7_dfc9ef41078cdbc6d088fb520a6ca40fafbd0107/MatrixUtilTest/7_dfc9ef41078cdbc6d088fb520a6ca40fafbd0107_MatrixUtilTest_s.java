 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.utils.math;
 
 
 import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
 import org.apache.commons.math3.linear.MatrixUtils;
 import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import junit.framework.TestCase;
 
 public class MatrixUtilTest extends TestCase {
 	public void testComputeDeterminant() {
 
 		double[][] matrixData = new double[3][3];
 		
 		matrixData[0][0] = -2;
 		matrixData[0][1] = 2;
 		matrixData[0][2] = 3;
 		
 		matrixData[1][0] = -1;
 		matrixData[1][1] = 1;
 		matrixData[1][2] = 3;
 
 		matrixData[2][0] = 2;
 		matrixData[2][1] = 0;
 		matrixData[2][2] = -1;
 
 		
 		RealMatrix m1 = MatrixUtils.createRealMatrix(matrixData);
 		assertEquals(6, (int)MatrixUtil.computeDeterminant(m1));	
 	}
 
 	public void testIsSingular() {
 		
 		final RealMatrix m = new Array2DRowRealMatrix(new double[][] {
 				{1, 0, 0},
 				{-2, 0, 0},
 				{4, 6, 1}});
 	
 		
 		assertTrue(MatrixUtil.isSingular(m));
 		
 	}
 }
