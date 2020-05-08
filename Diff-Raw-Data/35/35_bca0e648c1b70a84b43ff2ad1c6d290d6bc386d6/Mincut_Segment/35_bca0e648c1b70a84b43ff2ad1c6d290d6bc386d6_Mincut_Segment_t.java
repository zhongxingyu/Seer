 package test;
 
 
 
 import graph.Edge;
 import graph.FordFulkerson;
 import graph.Graph;
 import graph.Vertex;
 import ij.ImagePlus;
 import ij.gui.Roi;
 import ij.plugin.filter.PlugInFilter;
 import ij.plugin.frame.RoiManager;
 import ij.process.ByteProcessor;
 import ij.process.ImageProcessor;
 import ij.process.ImageStatistics;
 import java.util.ArrayList;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author zian
  */
 public class Mincut_Segment implements PlugInFilter {
 
     private ImagePlus imp;
 
     @Override
     public int setup(String arg, ImagePlus imp) {
         this.imp = imp;
         return DOES_8G;
     }
 
     @Override
     public void run(ImageProcessor ip) {        
         int width = ip.getWidth();
         int heigth = ip.getHeight();        
         
         ip.resetRoi();
         ImageStatistics imStats = ip.getStatistics();
         double imSTDV = imStats.stdDev;
         double imMean = imStats.mean;
 
         System.out.println("s = " + imSTDV);
         System.out.println("m = " + imMean);
 
         RoiManager roiM = RoiManager.getInstance();
         Roi[] rois = roiM.getRoisAsArray();
 
         // background
         ip.resetRoi();
         ip.setRoi(rois[0]);
         ImageStatistics objStats = ip.getStatistics();
         double bkgSTDV = objStats.stdDev;
         double bkgMean = objStats.mean;
 
         System.out.println("bkg_s = " + bkgSTDV);
         System.out.println("bkg_m = " + bkgMean);
 
         // object        
         ip.resetRoi();
         ip.setRoi(rois[1]);
         ImageStatistics bkgStats = ip.getStatistics();
         double objSTDV = bkgStats.stdDev;
         double objMean = bkgStats.mean;
 
         System.out.println("obj_s = " + objSTDV);
         System.out.println("obj_m = " + objMean);
 
         byte[] pixels = (byte[]) ip.getPixels();
         Graph graph = new Graph(pixels.length + 2);
         Vertex[] V = new Vertex[pixels.length];
 
         Vertex source = new Vertex(0);
         Vertex sink = new Vertex(pixels.length + 1);
         
         // add source vertex into the graph and edges from the source        
         ArrayList<Edge> sourceEdges = new ArrayList<Edge>(pixels.length);
         for (int i = 0; i < pixels.length; i++) {
             Vertex v = new Vertex(i + 1);
             V[i] = v;
            double p = (double)(pixels[i] & 0xff);
            float weight = (float) Math.exp(-(((objMean - p) * (objMean - p)))
                     / (2 * objSTDV * objSTDV));
             sourceEdges.add(new Edge(source, v, weight));
         }
         graph.addConnectedVertex(source, sourceEdges);
 
 
 
         for (int y = 0; y < heigth; y++) {
             for (int x = 0; x < width; x++) {
                 int v_idx = (y * width) + x;
                double pv = (double)(pixels[v_idx] & 0xff);
                 ArrayList<Edge> vEdges = new ArrayList<Edge>(5);
 
                 // add edges from vertex v to it's neighbours vertex w 
                 if (x < width - 1) {
                    int w_idx = (y * width) + (x + 1);                    
                    double pw = (double)(pixels[w_idx] & 0xff);
                     float weight_vw = (float) Math.exp(
                            -(((pv - pw) * (pv - pw))) / (2 * imSTDV * imSTDV));
                     vEdges.add(new Edge(V[v_idx], V[w_idx], weight_vw));
                 }
                 if (x > 0) {
                     int w_idx = (y * width) + (x - 1);
                    double pw = (double)(pixels[w_idx] & 0xff);
                     float weight_vw = (float) Math.exp(
                            -(((pv - pw) * (pv - pw))) / (2 * imSTDV * imSTDV));
                     vEdges.add(new Edge(V[v_idx], V[w_idx], weight_vw));
                 }
                 if (y < heigth - 1) {
                     int w_idx = ((y + 1) * width) + x;
                    double pw = (double)(pixels[w_idx] & 0xff);
                     float weight_vw = (float) Math.exp(
                            -(((pv - pw) * (pv - pw))) / (2 * imSTDV * imSTDV));
                     vEdges.add(new Edge(V[v_idx], V[w_idx], weight_vw));
                 }
                 if (y > 0) {
                     int w_idx = ((y - 1) * width) + x;
                    double pw = (double)(pixels[w_idx] & 0xff);
                     float weight_vw = (float) Math.exp(
                            -(((pv - pw) * (pv - pw))) / (2 * imSTDV * imSTDV));
                     vEdges.add(new Edge(V[v_idx], V[w_idx], weight_vw));
                 }
 
                 // add edge from vertex v to sink
                 float weight_vt = (float) Math.exp(
                        -(((bkgMean - pv) * (bkgMean - pv))) / (2 * bkgSTDV * bkgSTDV));
                 vEdges.add(new Edge(V[v_idx], sink, weight_vt));
 
                 // add vertex to graph
                 graph.addConnectedVertex(V[v_idx], vEdges);
             }
         }
         
         graph.addVertex(sink);
 
         System.out.println("end image to graph");
 
         FordFulkerson ff = new FordFulkerson(graph, source, sink);
         ArrayList<Vertex> mincut = ff.maxFlowMinCut();
 
         byte[] segmented = new byte[pixels.length];
         for (Vertex v : mincut) {
             segmented[v.getName() - 1] = (byte) 255;
         }
 
         ByteProcessor imSegmented = new ByteProcessor(width, heigth, segmented);
         new ImagePlus("segmented", imSegmented).show();
         
     }
     
 }
