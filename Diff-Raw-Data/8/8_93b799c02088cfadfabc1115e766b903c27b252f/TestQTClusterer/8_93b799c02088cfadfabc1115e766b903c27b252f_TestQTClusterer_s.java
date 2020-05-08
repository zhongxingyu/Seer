 /*
  * Copyright (C) 2005-2007 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.cornell.med.icb.clustering;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 /**
  * User: Fabien Campagne
  * Date: Oct 2, 2005
  * Time: 6:59:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public final class TestQTClusterer extends TestCase {
     /**
      * Check that data structures used to store clusters work as they should.
      */
     public void testDataStorage() {
         // put one instance in each cluster, total two instances
         QTClusterer clusterer = new QTClusterer(2);
         clusterer.addToCluster(0, 0);
         clusterer.addToCluster(1, 1);
         List<int []> clusters = clusterer.getClusters();
         assertNotNull(clusters.get(0));
         assertNotNull(clusters.get(1));
         assertEquals(1, clusters.get(0).length);
         assertEquals(1, clusters.get(1).length);
         assertEquals(0, clusters.get(0)[0]);
         assertEquals(1, clusters.get(1)[0]);
 
         // put two instances in the first cluster, none in the second, total two
         // instances
         clusterer = new QTClusterer(2);
         clusterer.addToCluster(0, 0);
         clusterer.addToCluster(1, 0);
         clusters = clusterer.getClusters();
         assertNotNull(clusters.get(0));
         assertNotNull(clusters.get(1));
         assertEquals(clusters.get(0).length, 2);
         assertEquals(clusters.get(1).length, 0);
         assertEquals(0, clusters.get(0)[0]);
         assertEquals(1, clusters.get(0)[1]);
     }
 
     public void testOneInstancePerCluster() {
         // put one instance in each cluster, total two instances
         final QTClusterer clusterer = new QTClusterer(2);
         final SimilarityDistanceCalculator distanceCalculator = new MaxLinkageDistanceCalculator() {
             @Override
             public double distance(final int instanceIndex, final int otherInstanceIndex) {
                 if (instanceIndex != otherInstanceIndex) {
                     return 100;
                 } else {
                     return 0;
                 }
             }
         };
 
         assertEquals(100d, distanceCalculator.distance(0, 1));
         assertEquals(100d, distanceCalculator.distance(1, 0));
         assertEquals(0d, distanceCalculator.distance(0, 0));
         assertEquals(0d, distanceCalculator.distance(1, 1));
 
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 2);
         assertNotNull(clusters);
         assertEquals(2, clusters.size());
         assertEquals(1, clusters.get(0).length);
         assertEquals(1, clusters.get(1).length);
         assertEquals(0, clusters.get(0)[0]);
         assertEquals(1, clusters.get(1)[0]);
 
     }
 
     public void testFourInstanceClusteringInOneCluster() {
         // put one instance in each cluster, total two instances
         final QTClusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator = new MaxLinkageDistanceCalculator() {
             @Override
             public double distance(final int instanceIndex, final int otherInstanceIndex) {
                 if (instanceIndex == 0 && otherInstanceIndex == 1 ||
                         instanceIndex == 1 && otherInstanceIndex == 0) {
                     return 0;    // instances 0 and 1 belong to same cluster
                 } else {
                     return 10;
                 }
             }
         };
         // instances 0,1,2,3 go to cluster 1 (distance(0,1)=0; distance(2,0)=10<=threshold)
 
         assertEquals(0d, distanceCalculator.distance(0, 1));
         assertEquals(0d, distanceCalculator.distance(1, 0));
         assertEquals(10d, distanceCalculator.distance(0, 2));
         assertEquals(10d, distanceCalculator.distance(2, 0));
         assertEquals(10d, distanceCalculator.distance(2, 3));
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 10);
         assertNotNull(clusters);
        assertEquals("Expected one cluster", 1, clusters.size());
         assertEquals("First cluster must have size 2", 4, clusters.get(0).length);
         assertEquals("Instance 0 in cluster 0", 0, clusters.get(0)[0]);
         assertEquals("Instance 1 in cluster 0", 1, clusters.get(0)[1]);
         assertEquals("Instance 2 in cluster 0", 2, clusters.get(0)[2]);
         assertEquals("Instance 3 in cluster 0", 3, clusters.get(0)[3]);
     }
 
     public void testFourInstanceClusteringInThreeClusters() {
         // put one instance in each cluster, total two instances
         final QTClusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator = new MaxLinkageDistanceCalculator() {
 
             @Override
             public double distance(final int instanceIndex, final int otherInstanceIndex) {
                 if (instanceIndex == 0 && otherInstanceIndex == 1 ||
                         instanceIndex == 1 && otherInstanceIndex == 0) {
                     return 0;    // instances 0 and 1 belong to same cluster
                 } else {
                     return 11;
                 }
             }
         };
         assertEquals(0d, distanceCalculator.distance(0, 1));
         assertEquals(0d, distanceCalculator.distance(1, 0));
         assertEquals(11d, distanceCalculator.distance(0, 2));
         assertEquals(11d, distanceCalculator.distance(2, 0));
         assertEquals(11d, distanceCalculator.distance(2, 3));
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 10);
         assertNotNull(clusters);
        assertEquals("Expected only two clusters", 3, clusters.size());
         assertEquals("First cluster must have size 2", 2, clusters.get(0).length);
         assertEquals("Second cluster must have size 1", 1, clusters.get(1).length);
         assertEquals("Third cluster must have size 1", 1, clusters.get(2).length);
         assertEquals("Instance 0 in cluster 0", 0, clusters.get(0)[0]);
         assertEquals("Instance 1 in cluster 0", 1, clusters.get(0)[1]);
         assertEquals("Instance 2 in cluster 1", 2, clusters.get(1)[0]);
         assertEquals("Instance 3 in cluster 2", 3, clusters.get(2)[0]);
     }
 
     public void testFourInstanceClusteringInFourClusters() {
         // put one instance in each cluster, total two instances
         final QTClusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator = new MaxLinkageDistanceCalculator() {
 
             @Override
             public double distance(final int instanceIndex, final int otherInstanceIndex) {
                 if (instanceIndex == 0 && otherInstanceIndex == 1 ||
                         instanceIndex == 1 && otherInstanceIndex == 0) {
                     return 0;    // instances 0 and 1 belong to same cluster
                 } else {
                     return 10;
                 }
             }
         };
 
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 2);
         assertNotNull(clusters);
        assertEquals("Expected four clusters", 3, clusters.size());
         assertEquals("First cluster must have size 2", 2, clusters.get(0).length);
         assertEquals("Second cluster must have size 1", 1, clusters.get(1).length);
         assertEquals("Third cluster must have size 1", 1, clusters.get(2).length);
         assertEquals("Instance 0 in cluster 0", 0, clusters.get(0)[0]);
         assertEquals("Instance 1 in cluster 0", 1, clusters.get(0)[1]);
         assertEquals("Instance 2 in cluster 2", 2, clusters.get(1)[0]);
         assertEquals("Instance 3 in cluster 3", 3, clusters.get(2)[0]);
     }
 
     public void testOther() {
         final QTClusterer clusterer = new QTClusterer(4);
         final SimilarityDistanceCalculator distanceCalculator = new MaxLinkageDistanceCalculator() {
 
             @Override
             public double distance(final int instanceIndex, final int otherInstanceIndex) {
                 return 0;    // instances 0-3 belong to the same cluster
             }
         };
         final List<int[]> clusters = clusterer.cluster(distanceCalculator, 2);
         assertNotNull(clusters);
         assertEquals("Expected one cluster", 1, clusters.size());
         assertEquals("First cluster must have size 4", 4, clusters.get(0).length);
 
         assertEquals("Instance 0 in cluster 0", 0, clusters.get(0)[0]);
         assertEquals("Instance 1 in cluster 0", 1, clusters.get(0)[1]);
         assertEquals("Instance 2 in cluster 0", 2, clusters.get(0)[2]);
         assertEquals("Instance 3 in cluster 0", 3, clusters.get(0)[3]);
     }
 }
