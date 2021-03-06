 /* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
    modeler, Finite element mesher, Plugin architecture.
 
     Copyright (C) 2003,2004,2005,2006, by EADS CRC
     Copyright (C) 2007, by EADS France
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.jcae.mesh.amibe.algos2d;
 
 import org.jcae.mesh.amibe.ds.TriangleVH;
 import org.jcae.mesh.amibe.ds.Triangle;
 import org.jcae.mesh.amibe.ds.AbstractHalfEdge;
 import org.jcae.mesh.amibe.ds.Vertex;
 import org.jcae.mesh.amibe.patch.Mesh2D;
 import org.jcae.mesh.amibe.patch.VirtualHalfEdge2D;
 import org.jcae.mesh.amibe.patch.Vertex2D;
 import java.util.Iterator;
 import java.util.ArrayList;
 import gnu.trove.PrimeFinder;
 import org.apache.log4j.Logger;
 
 /**
  * Insert nodes to produce a unit mesh.  Process all edges; if an edge
  * is longer than sqrt(2), candidate vertices are added to a bucket
  * to virtually provide unit length subsegments.
  * The next step is to take vertices from the bucket in random order.
  * For each vertex <code>v</code>, the closest vertex <code>w</code>
  * already present in the mesh is returned by
  * {@link org.jcae.mesh.amibe.util.KdTree#getNearestVertex(Mesh, Vertex)}
  * If the distance between <code>v</code> and <code>w</code> is lower
  * than 1/sqrt(2), <code>v</code> is dropped, otherwise it is inserted
  * into the mesh.  Just after a vertex is inserted, incident edges are
  * swapped if they are not Delaunay.
  * The whole process is repeated until no new vertex is added.
  *
  * <p>
  * If all vertices of an edge were inserted at the same time, adjacent
  * edges may get in trouble because their candidate vertices could be
  * too near from these points.  In order to avoid this problem, vertices
  * are processed in a random order so that all edges have a chance to be
  * splitted.  As we want reproducible meshes, a pseudo-random order is
  * preferred.
  * </p>
  *
  * <p>
  * Triangle centroids are also inserted if they are not too near of
  * existing vertices.  This was added to try to improve triangle
  * quality, but is a bad idea.  Bad triangles should instead be sorted
  * (with {@link org.jcae.mesh.amibe.util.PAVLSortedTree}) and their
  * circumcenter added to the mesh if the overall quality is improved, 
  * </p>
  */
 public class Insertion
 {
 	private static Logger logger=Logger.getLogger(Insertion.class);
 	private Mesh2D mesh;
 	
 	private double minlen = 1.0 / Math.sqrt(2.);
 	private double maxlen = 1.0 * Math.sqrt(2.);
 	
 	/**
 	 * Creates a <code>Insertion</code> instance.
 	 *
 	 * @param m  the <code>Mesh2D</code> instance to refine.
 	 */
 	public Insertion(Mesh2D m)
 	{
 		mesh = m;
 	}
 	
 	public Insertion(Mesh2D m, double scale)
 	{
 		mesh = m;
 		minlen = scale / Math.sqrt(2.);
 		maxlen = scale * Math.sqrt(2.);
 	}
 	
 	/**
 	 * Iteratively insert inner nodes.
 	 */
 	public void compute()
 	{
 		int nrIter = 0;
 		int tooNearNodes = 0;
 		int kdtreeSplit = 0;
 		logger.debug(" Insert inner nodes");
 		ArrayList<Vertex2D> nodes = new ArrayList<Vertex2D>();
 		ArrayList<Vertex2D> triNodes = new ArrayList<Vertex2D>();
 		VirtualHalfEdge2D sym = new VirtualHalfEdge2D();
 		VirtualHalfEdge2D ot = new VirtualHalfEdge2D();
 		// We do not want to split boundary edges.
 		for(Iterator<Triangle> it = mesh.getTriangles().iterator(); it.hasNext(); )
 		{
 			TriangleVH t = (TriangleVH) it.next();
 			if (t.hasAttributes(AbstractHalfEdge.OUTER))
 				continue;
 			ot.bind(t);
 			for (int i = 0; i < 3; i++)
 			{
 				ot.next();
 				if (ot.hasAttributes(AbstractHalfEdge.BOUNDARY))
 					ot.setAttributes(AbstractHalfEdge.MARKED);
 				else
 					ot.clearAttributes(AbstractHalfEdge.MARKED);
 			}
 		}
 		// We try to insert new nodes by splitting large edges.  As edge collapse
 		// is costful, nodes are inserted only if it does not create small edges,
 		// which means that nodes are not deleted.
 		// We iterate over all edges, and put candidate nodes into triNodes.
 		// If an edge has no candidates, either because it is small or because no
 		// nodes can be inserted, it is tagged and will not have to be checked
 		// during next iterations.
 		while (true)
 		{
 			nrIter++;
 			// Maximal number of nodes which are inserted on an edge
 			int maxNodes = 0;
			// Number of checked edges
			int checked = 0;
 			nodes.clear();
 			for(Iterator<Triangle> it = mesh.getTriangles().iterator(); it.hasNext(); )
 			{
 				TriangleVH t = (TriangleVH) it.next();
 				if (t.hasAttributes(AbstractHalfEdge.OUTER))
 					continue;
 				ot.bind(t);
 				triNodes.clear();
 				// Maximal number of nodes which are inserted on edges of this triangle
 				int nrTriNodes = 0;
 				for (int i = 0; i < 3; i++)
 				{
 					ot.next();
 					if (ot.hasAttributes(AbstractHalfEdge.MARKED))
 					{
 						// This edge has already been checked and cannot be split
 						continue;
 					}
 					sym.bind((TriangleVH) ot.getTri(), ot.getLocalNumber());
 					sym.sym();
 					if (sym.hasAttributes(AbstractHalfEdge.MARKED))
 					{
 						// This edge has already been checked and cannot be split
 						continue;
 					}
 					double l = mesh.compGeom().length(ot);
 					if (l < maxlen)
 					{
 						// This edge is smaller than target size and is not split
 						ot.setAttributes(AbstractHalfEdge.MARKED);
 						sym.setAttributes(AbstractHalfEdge.MARKED);
 						continue;
 					}
 					// Tag symmetric edge so that edges are checked only once
 					sym.setAttributes(AbstractHalfEdge.MARKED);
 					//  Long edges are discretized, but do not create more than 4 subsegments
 					double lcrit = 1.0;
 					if (l > 4.0)
 						lcrit = l / 4.0;
 					Vertex2D start = (Vertex2D) ot.origin();
 					Vertex2D end = (Vertex2D) ot.destination();
 					double [] xs = start.getUV();
 					double [] xe = end.getUV();
 					int segments = (int) (2.0*l/lcrit) + 10;
 					Vertex [] np = new Vertex[segments-1];
 					for (int ns = 1; ns < segments; ns++)
 						np[ns-1] = mesh.createVertex(xs[0]+ns*(xe[0]-xs[0])/segments, xs[1]+ns*(xe[1]-xs[1])/segments);
 					
 					Vertex2D last = start;
 					int nrNodes = 0;
 					
 					l = 0.0;
 					for (int ns = 0; ns < segments-1; ns++)
 					{
 						l = mesh.compGeom().distance(last, (Vertex2D) np[ns]);
 						if (l > lcrit)
 						{
 							last = (Vertex2D) np[ns];
 							triNodes.add(last);
 							l = 0.0;
 							nrNodes++;
 						}
 					}
 					if (nrNodes > nrTriNodes)
 					{
 						nrTriNodes = nrNodes;
 					}
 					else if (nrNodes == 0)
 					{
 						ot.setAttributes(AbstractHalfEdge.MARKED);
 					}
 					checked++;
 				}
 				if (nrTriNodes > maxNodes)
 					maxNodes = nrTriNodes;
 				if (!triNodes.isEmpty())
 				{
 					//  Process in pseudo-random order
 					int prime = PrimeFinder.nextPrime(nrTriNodes);
 					int imax = triNodes.size();
 					while (imax % prime == 0)
 						prime = PrimeFinder.nextPrime(prime+1);
 					if (prime >= imax)
 						prime = 1;
 					int index = imax / 2;
 					for (int i = 0; i < imax; i++)
 					{
 						Vertex2D v = triNodes.get(index);
 						Vertex2D n = (Vertex2D) mesh.getQuadTree().getNearestVertex(mesh, v);
 						assert n == mesh.getQuadTree().getNearestVertexDebug(mesh, v);
 						if (mesh.compGeom().distance(v, n) > minlen)
 						{
 							mesh.getQuadTree().add(v);
 							nodes.add(v);
 						}
 						else
 							tooNearNodes++;
 						index += prime;
 						if (index >= imax)
 							index -= imax;
 					}
 				}
 			}
 			//  Try to insert triangle centroid after all other points.
 			Vertex2D c = null;
 			for (Iterator<Triangle> it = mesh.getTriangles().iterator(); it.hasNext(); )
 			{
 				TriangleVH t = (TriangleVH) it.next();
 				if (t.hasAttributes(AbstractHalfEdge.OUTER))
 					continue;
 				if (c == null)
 					c = (Vertex2D) mesh.createVertex(0.0, 0.0);
 				c.centroid((Vertex2D[]) t.vertex);
 				Vertex2D n = (Vertex2D) mesh.getQuadTree().getNearestVertex(mesh, c);
 				assert n == mesh.getQuadTree().getNearestVertexDebug(mesh, c);
 				if (mesh.compGeom().distance(c, n) > minlen)
 				{
 					mesh.getQuadTree().add(c);
 					nodes.add(c);
 					c = null;
 				}
 				else
 					tooNearNodes++;
 			}
 			if (nodes.isEmpty())
 				break;
 			for (Iterator<Vertex2D> it = nodes.iterator(); it.hasNext(); )
 			{
 				Vertex2D v = it.next();
 				//  These vertices are not bound to any triangles, so
 				//  they must be removed, otherwise getSurroundingOTriangle
 				//  may return a null pointer.
 				mesh.getQuadTree().remove(v);
 			}
 			//  Process in pseudo-random order.  There is at most maxNodes nodes
 			//  on an edge, we choose an increment step greater than this value
 			//  to try to split all edges.
 			int prime = PrimeFinder.nextPrime(maxNodes);
 			int imax = nodes.size();
 			while (imax % prime == 0)
 				prime = PrimeFinder.nextPrime(prime+1);
 			if (prime >= imax)
 				prime = 1;
 			int index = imax / 2;
 			int skippedNodes = 0;
 			for (int i = 0; i < imax; i++)
 			{
 				Vertex2D v = nodes.get(index);
 				VirtualHalfEdge2D vt = v.getSurroundingOTriangle(mesh);
 				if (!vt.split3(mesh, v, false))
 					skippedNodes++;
 				index += prime;
 				if (index >= imax)
 					index -= imax;
 			}
 			if (logger.isDebugEnabled())
 			{
 				logger.debug("Mesh now contains "+mesh.getTriangles().size()+" triangles");
 				if (checked > 0)
 					logger.debug(checked+" edges checked");
 				if (imax - skippedNodes > 0)
 					logger.debug((imax-skippedNodes)+" nodes added");
 				if (tooNearNodes > 0)
 					logger.debug(tooNearNodes+" nodes are too near from existing vertices and cannot be inserted");
 				if (skippedNodes > 0)
 					logger.debug(skippedNodes+" nodes cannot be inserted");
 				if (kdtreeSplit > 0)
 					logger.debug(kdtreeSplit+" quadtree cells split");
 			}
 			if (skippedNodes == nodes.size())
 				break;
 		}
 		logger.debug("Number of iterations to insert all nodes: "+nrIter);
 	}
 	
 }
