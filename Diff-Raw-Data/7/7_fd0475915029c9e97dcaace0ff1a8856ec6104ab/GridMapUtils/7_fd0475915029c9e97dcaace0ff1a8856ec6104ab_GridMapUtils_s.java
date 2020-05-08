 /*
  *  Copyright (c) 2009, Prasanna Velagapudi <pkv@cs.cmu.edu>
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are met:
  *      * Redistributions of source code must retain the above copyright
  *        notice, this list of conditions and the following disclaimer.
  *      * Redistributions in binary form must reproduce the above copyright
  *        notice, this list of conditions and the following disclaimer in the
  *        documentation and/or other materials provided with the distribution.
  *      * Neither the name of the project nor the
  *        names of its contributors may be used to endorse or promote products
  *        derived from this software without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ''AS IS'' AND ANY
  *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE PROJECT AND CONTRIBUTORS BE LIABLE FOR ANY
  *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package robotutils.data;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.util.Arrays;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import org.jgrapht.graph.SimpleWeightedGraph;
 import org.jgrapht.graph.UnmodifiableGraph;
 
 /**
  * Helper class that converts GridMaps into a variety of useful formats.
  * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
  */
 public class GridMapUtils {
 
     public static UnmodifiableGraph<Coordinate, DefaultWeightedEdge> toGraph(GridMap map) {
 
         SimpleWeightedGraph<Coordinate, DefaultWeightedEdge> graph =
                 new SimpleWeightedGraph<Coordinate, DefaultWeightedEdge>(DefaultWeightedEdge.class);
         
         int dims = map.dims();
         int length = map.length();
         int[] sizes = map.sizes();
         int[] idx = new int[dims];
 
         // Add every vertex that is not an obstacle
         for (int v = 0; v < length; v++) {
             if (map.get(idx) >= 0) {
                 graph.addVertex(new IntCoord(idx));
             }
 
             for (int i = 0; i < dims; i++) {
                 if (idx[i] < sizes[i] - 1) {
                     idx[i]++;
                     break;
                 } else {
                     idx[i] = 0;
                 }
             }
         }
 
         // Add every non-obstacle edge
         for (int v = 0; v < length; v++) {
             for (int i = 0; i < dims; i++) {
                 if (idx[i] < sizes[i] - 1) {
                     int[] idx1 = Arrays.copyOf(idx, idx.length);
                     idx1[i]++;
 
                     if (map.get(idx) >= 0 && map.get(idx1) >= 0) {
                         DefaultWeightedEdge e = graph.addEdge(new IntCoord(idx), new IntCoord(idx1));
                         graph.setEdgeWeight(e, ((double)map.get(idx) + (double)map.get(idx1))/2.0 + 1.0);
                     }
                 }
             }
 
             for (int i = 0; i < dims; i++) {
                 if (idx[i] < sizes[i] - 1) {
                     idx[i]++;
                     break;
                 } else {
                     idx[i] = 0;
                 }
             }
         }
 
         return new UnmodifiableGraph<Coordinate, DefaultWeightedEdge>(graph);
     }
 
     public static BufferedImage toImage(StaticMap map) {
         if (map.dims() != 2)
             throw new IllegalArgumentException("Cannot display " + map.dims() + "-D map as image.");
 
         BufferedImage image = new BufferedImage(map.size(0), map.size(1), BufferedImage.TYPE_BYTE_GRAY);
         image.getRaster().setDataElements(0, 0, map.size(0), map.size(1), map.getData());
 
         return image;
     }
 
     public static BufferedImage toImage(GridMap map) {
         if (map.dims() != 2)
             throw new IllegalArgumentException("Cannot display " + map.dims() + "-D map as image.");
 
         BufferedImage image = new BufferedImage(map.size(0), map.size(1), BufferedImage.TYPE_BYTE_GRAY);
         fillImage(image, map);
 
         return image;
     }
 
     public static void fillImage(BufferedImage image, GridMap map) {
        int height = Math.min(image.getHeight(), map.size(0));
        int width = Math.min(image.getWidth(), map.size(1));
 
         fillImage(image, map, 0, 0, width, height);
     }
 
     public static void fillImage(BufferedImage image, GridMap map, int x, int y, int width, int height) {
         WritableRaster wr = image.getRaster();
         for (int i = y; i < height; i++) {
             for (int j = x; j < width; j++) {
                wr.setSample(i, j, 0, map.get(i, j));
             }
         }
     }
 }
