 package de.ismll.table.impl;
 
 import de.ismll.table.IntVector;
 import de.ismll.table.Vectors;
 
 public class VectorsTest {
 
 	//	@Test
 	public void testCovariance() {
 		DefaultVector a1 = new DefaultVector(10);
 		Vectors.fillUniformAtRandom(a1, 0.f, 1.f);
 		DefaultVector a2 = new DefaultVector(10);
 		Vectors.fillUniformAtRandom(a2, 9.f, 10.f);
 
 		double value = Vectors.covariance(a1, a2);
 
 		System.out.println(value);
 	}
 
 
 	//	@Test
 	public void testRemoveAll() {
 		IntVector base = new DefaultIntVector(100);
 		IntVector removeIndizes = new DefaultIntVector(10);
		IntVector assumedResult = new DefaultIntVector(90);
 		for (int i = 0; i < base.size(); i++)
 			base.set(i, i);
 		for (int i = 0; i < removeIndizes.size(); i++)
 			removeIndizes.set(i, i*i);
		int assumedIdx=0;
 
 		//		System.out.println(assumedResult);
 		//			base.set(i, i);
 
 		IntVector removeAll = Vectors.removeAll(base, removeIndizes);
 		System.out.println(removeAll);
 	}
 
 }
