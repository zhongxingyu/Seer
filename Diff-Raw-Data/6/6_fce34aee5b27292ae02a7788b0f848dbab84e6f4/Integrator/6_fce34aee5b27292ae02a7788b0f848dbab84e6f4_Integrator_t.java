 package integration;
 
 import com.google.common.base.Function;
 
 import utilities.DoubleVector;
import utilities.Pair;
 
 public class Integrator {
 	public DoubleVector integrate(Function<DoubleVector, DoubleVector> f, Pair<Double, Double>... ranges) {
 		int dim = ranges.length;
		double gridSteps = 10;
 		DoubleVector minCoord = new DoubleVector(dim);
 		DoubleVector maxCoord = new DoubleVector(dim);
 		DoubleVector steps = new DoubleVector(dim);
 		for (int i = 0; i < dim; i++) {
 			minCoord = minCoord.setValue(i, ranges[i].a);
 			maxCoord = maxCoord.setValue(i, ranges[i].b);
 			steps = steps.setValue(i, (ranges[i].b - ranges[i].a) / gridSteps);
 		}
 		DoubleVector total = new DoubleVector(dim);
 		DoubleVector coord = minCoord.multiply(1);
 		while (coord.getValue(dim - 1) <= maxCoord.getValue(dim - 1)) {
 			double mult = Math.pow(2, numMinMax(coord, minCoord, maxCoord));
 			total = total.add(f.apply(coord).multiply(mult));
 			int index = 0;
 			while (index < dim - 1 && coord.getValue(index) >= maxCoord.getValue(index)) {
 				coord = coord.setValue(index, minCoord.getValue(index));
 				index++;
 			}
 			coord = coord.setValue(index, coord.getValue(index) + steps.getValue(index));
 		}
 		double prodSteps = 1 / Math.pow(2, dim);
 		for (int i = 0; i < dim; i++) {
 			prodSteps *= steps.getValue(i);
 		}
 		return total.multiply(prodSteps);
 	}
 	
 	private int numMinMax(DoubleVector check, DoubleVector min, DoubleVector max) {
 		int count = 0;
 		int dim = check.getSize();
 		if (min.getSize() != dim || max.getSize() != dim) {
 			throw new IllegalArgumentException("Arrays must be same length");
 		}
 		for (int i = 0; i < dim; i++) {
 			if (check.getValue(i) == min.getValue(i) || check.getValue(i) == max.getValue(i)) {
 				count++;
 			}
 		}
 		return count;
 	}
 }
