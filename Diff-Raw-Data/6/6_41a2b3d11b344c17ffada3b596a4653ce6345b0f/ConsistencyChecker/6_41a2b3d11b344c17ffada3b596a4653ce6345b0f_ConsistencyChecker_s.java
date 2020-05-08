 /* Copyright 2009-2010 Yves Dubromelle
  *
  * This file is part of JenericAHP.
  *
  * JenericAHP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JenericAHP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JenericAHP.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.taeradan.ahp;
 
 import Jama.Matrix;
 
 import java.util.logging.Logger;
 
 /**
  * @author Yves Dubromelle
  * @author Jean-Pierre PRUNARET
  */
 public class ConsistencyChecker {
 
 	private static final double[] randomIndex = {0.00,
 												 0.00,
 												 0.58,
 												 0.90,
 												 1.12,
 												 1.24,
 												 1.32,
 												 1.41,
 												 1.45,
 												 1.49,
 												 1.51,
 												 1.48,
 												 1.56,
 												 1.57,
 												 1.59};
 	private double consistencyRatio;
 
 	public boolean isConsistent(final Matrix preferenceMatrix, final Matrix priorityVector) {
 		assert preferenceMatrix != null : "preferenceMatrix should be not null";
 		assert preferenceMatrix.getRowDimension() > 0;
 		assert preferenceMatrix.getColumnDimension() > 0;
 		assert priorityVector != null : "priorityVector should be not null";
 		assert priorityVector.getRowDimension() > 0;
 		assert priorityVector.getColumnDimension() > 0;
 
 		boolean isConsistent = false;
 
 		int dimension = 0;
 		if (preferenceMatrix.getRowDimension() == priorityVector.getRowDimension()) {
 			dimension = preferenceMatrix.getRowDimension();
 
 			if (dimension == 1) {
 				isConsistent = true;
 			}
 
 			if (dimension == 2) {
 				if (preferenceMatrix.get(0, 1) == (1 / preferenceMatrix.get(1, 0))) {
 					isConsistent = true;
 				} else {
 					isConsistent = false;
 				}
 			}
 
 			if (dimension > 2 && dimension < 15) {
 				final double[] lambdas = new double[dimension];
 
 				for (int i = 0; i < dimension; i++) {
 					double sum = 0;
 					for (int j = 0; j < dimension; j++) {
 						sum = sum + preferenceMatrix.get(i, j) * priorityVector.get(j, 0);
 					}
 					lambdas[i] = sum / priorityVector.get(i, 0);
 				}
 				double lambdaMax = Double.MIN_VALUE;
 				for (int index = 0; index < dimension; index++) {
 					if (lambdas[index] > lambdaMax) {
 						lambdaMax = lambdas[index];
 					}
 				}
 
 				final double consistencyIndex = (lambdaMax - dimension) / (dimension - 1);
 				consistencyRatio = consistencyIndex / randomIndex[dimension - 1];
 
 				if (consistencyRatio < 0.1) {
 					isConsistent = true;
 				}
			} else {
 				Logger.getAnonymousLogger().severe(
 						"Preference matrix and priority vector are too wide (15 max) or empty !!"
 						+ dimension);
 			}
 		} else {
 			Logger.getAnonymousLogger().severe(
 					"The matrix and vector dimension does not match !!"
 					+ preferenceMatrix.getRowDimension() + "," + priorityVector.getRowDimension());
 		}
 
 		return isConsistent;
 	}
 
 	public double getConsistencyRatio() {
 		return consistencyRatio;
 	}
 }
