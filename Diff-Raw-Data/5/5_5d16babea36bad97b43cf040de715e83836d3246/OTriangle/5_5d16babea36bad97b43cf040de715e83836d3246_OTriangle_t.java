 /* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
    modeler, Finit element mesher, Plugin architecture.
 
     Copyright (C) 2004 Jerome Robert <jeromerobert@users.sourceforge.net>
 
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
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */
 
 package org.jcae.mesh.amibe.ds;
 
 import java.util.Random;
 import org.jcae.mesh.amibe.metrics.Metric2D;
 import org.apache.log4j.Logger;
 
 /*
  * This class is derived from Jonathan Richard Shewchuk's work
  * on Triangle, see
  *       http://www.cs.cmu.edu/~quake/triangle.html
  * His data structure is very compact, and similar ideas were
  * developed here, but due to Java constraints, this version is a
  * little bit less efficient than its C counterpart.
  *
  * Geometrical primitives and basic routines have been written from
  * scratch, but are in many cases very similar to those defined by
  * Shewchuk since data structures are almost equivalent and there
  * are few ways to achieve the same operations.
  *
  * Main algorithms are taken from Bamg, written by Frederic Hecht
  *       http://www-rocq1.inria.fr/gamma/cdrom/www/bamg/eng.htm
  */
 /**
  * This class is a handle to {@linkorg.jcae.mesh.amibe.ds.Triangle}.
  *
  * Jonathan Richard Shewchuk explains in
  *       http://www.cs.cmu.edu/~quake/triangle.html
  * why triangle-based data structures are more efficient than their
  * edge-based counterparts.  But mesh operations make heavy use of edges,
  * and informations about adges are not stored in this data structure in
  * order to be compact.
  *
  * A triangle is composed of three edges, so a triangle and a number
  * between 0 and 2 can represent an edge.  This <code>OTriangle</code>
  * class plays this role, it defines an <em>oriented triangle</em>, or
  * in other words an oriented edge.  Instances of this class are tied to
  * their underlying {@linkorg.jcae.mesh.amibe.ds.Triangle} instances, so
  * modifications are not local to this class!
  */
 public class OTriangle
 {
 	private static Logger logger = Logger.getLogger(OTriangle.class);
 	private static final int [] next3 = { 1, 2, 0 };
 	private static final int [] prev3 = { 2, 0, 1 };
 	private static final int [] bitmaskclear = {  0xffffff3c,  0xffffff33,  0xffffff0f };
 	// private static final int [] bitmaskclear = {  60,  51,  15 };
 	private static final int [] bitmaskclearAttr = {  0xffff00ff,  0xff00ffff,  0x00ffffff };
 	
 	public static final int BOUNDARY = 1;
 	public static final int OUTER = BOUNDARY << 1;
 	public static final int MARKED = OUTER << 1;
 	public static final int SWAPPED = MARKED << 1;
 	
 	//  Complex algorithms require several OTriangle, they are
 	//  allocated here to prevent allocation/deallocation overhead.
 	private static OTriangle [] work = new OTriangle[4];
 	static {
 		for (int i = 0; i < 4; i++)
 			work[i] = new OTriangle();
 	}
 	
 	private static final Random rand = new Random(139L);
 	
 	/*
 	 * Vertices can be accessed through
 	 *        origin = tri.vertex[next3[orientation]]
 	 *   destination = tri.vertex[prev3[orientation]]
 	 *          apex = tri.vertex[orientation]
 	 * Adjacent triangle is tri.adj[orientation].tri and its orientation
 	 * is ((tri.adjPos >> (2*orientation)) & 3)
 	 */
 	private Triangle tri;
 	private int orientation;
 	private int attributes;
 	
 	/**
 	 * Dummy constructor.
 	 */
 	public OTriangle()
 	{
 		tri = null;
 		orientation = 0;
 		attributes = 0;
 	}
 	
 	/**
 	 * Creates an object to handle data about a triangle.
 	 *
 	 * @param t  geometrical triangle.
 	 * @param o  a number between 0 and 2 determining an edge.
 	 */
 	public OTriangle(Triangle t, int o)
 	{
 		tri = t;
 		orientation = o;
 		attributes = (tri.adjPos >> (8*(1+orientation))) & 0xff;
 	}
 	
 	/**
 	 * Creates a standalone triangle defined by three vertices.
 	 *
 	 * The triangle is created, and a handle on it is returned.
 	 * No adjacency relations are defined.
 	 *
 	 * @param v0  first vertex.
 	 * @param v1  second vertex.
 	 * @param v2  third vertex.
 	 */
 	public OTriangle(Vertex v0, Vertex v1, Vertex v2)
 	{
 		tri = new Triangle(v0, v1, v2);
 		//  By default, edge is (v0, v1)
 		orientation = 2;
 		attributes = (tri.adjPos >> 24) & 0xff;
 		//  v0.tri and the like are not updated, because
 		//  vertices may be Vertex.outer.
 	}
 	
 	/**
 	 * Checks whether oriented triangles are identical.
 	 *
 	 * @param that  the triangle to compare with.
 	 * @return <code>true</code> if both objects represent the same
 	 * oriented triangle, <code>false</code> otherwise.
 	 */
 	public final boolean isSame(OTriangle that)
 	{
 		return (tri == that.tri && orientation == that.orientation);
 	}
 	
 	/**
 	 * Returns the triangle tied to this object.
 	 *
 	 * @return the triangle tied to this object.
 	 */
 	public final Triangle getTri()
 	{
 		return tri;
 	}
 	
 	/**
 	 * Sets the triangle tied to this object, and resets orientation.
 	 *
 	 * @param t  the triangle tied to this object.
 	 */
 	public final void bind(Triangle t)
 	{
 		tri = t;
 		orientation = 0;
 		attributes = (tri.adjPos >> 8) & 0xff;
 	}
 	
 	/**
 	 * Returns the orientation of this oriented triangle.
 	 *
 	 * @return the orientation of this oriented triangle.
 	 */
 	public final int getOrientation()
 	{
 		return orientation;
 	}
 	
 	/**
 	 * Returns the oattributes of this oriented triangle.
 	 * Attributes are a bit field.
 	 *
 	 * @param attr  the attributes to check
 	 * @return <code>true</code> if this OTriangle has all these
 	 * attributes set, <code>false</code> otherwise.
 	 */
 	public final boolean hasAttributes(int attr)
 	{
 		return (attributes & attr) == attr;
 	}
 	
 	/**
 	 * Sets the oattributes of this oriented triangle.
 	 *
 	 * @param attr  the attribute of this oriented triangle.
 	 */
 	public final void setAttributes(int attr)
 	{
 		attributes |= attr;
 		updateAttributes();
 	}
 	
 	/**
 	 * Returns the oattributes of this oriented triangle.
 	 *
 	 * @return the attributes of this oriented triangle.
 	 */
 	public final void clearAttributes(int attr)
 	{
 		attributes &= ~attr;
 		updateAttributes();
 	}
 	
 	private final void updateAttributes()
 	{
 		tri.adjPos &= bitmaskclearAttr[orientation];
 		tri.adjPos |= ((attributes & 0xff) << (8*(1+orientation)));
 	}
 	
 	//  These geometrical primitives have 2 signatures:
 	//      fct(this, that)   applies fct to 'this' and stores result
 	//                        in an already allocated object 'that'.
 	//      fct() transforms current object.
 	//  This is definitely not an OO approach, but it is much more
 	//  efficient by preventing useless memory allocations.
 	//  They do not return any value to make clear that calling
 	//  these routines requires extra care.
 	public static final void symOTri(OTriangle o, OTriangle that)
 	{
 		that.tri = o.tri.adj[o.orientation];
 		that.orientation = ((o.tri.adjPos >> (2*o.orientation)) & 3);
 		that.attributes = (that.tri.adjPos >> (8*(1+that.orientation))) & 0xff;
 	}
 	
 	public final void symOTri()
 	{
 		int neworient = ((tri.adjPos >> (2*orientation)) & 3);
 		tri = tri.adj[orientation];
 		orientation = neworient;
 		attributes = (tri.adjPos >> (8*(1+orientation))) & 0xff;
 	}
 	
 	public static final void nextOTri(OTriangle o, OTriangle that)
 	{
 		that.tri = o.tri;
 		that.orientation = next3[o.orientation];
 		that.attributes = (that.tri.adjPos >> (8*(1+that.orientation))) & 0xff;
 	}
 	
 	public final void nextOTri()
 	{
 		orientation = next3[orientation];
 		attributes = (tri.adjPos >> (8*(1+orientation))) & 0xff;
 	}
 	
 	public static final void prevOTri(OTriangle o, OTriangle that)
 	{
 		that.tri = o.tri;
 		that.orientation = prev3[o.orientation];
 		that.attributes = (that.tri.adjPos >> (8*(1+that.orientation))) & 0xff;
 	}
 	
 	public final void prevOTri()
 	{
 		orientation = prev3[orientation];
 		attributes = (tri.adjPos >> (8*(1+orientation))) & 0xff;
 	}
 
 	public static final void nextOTriOrigin(OTriangle o, OTriangle that)
 	{
 		prevOTri(o, that);
 		that.symOTri();
 	}
 	
 	public final void nextOTriOrigin()
 	{
 		prevOTri();
 		symOTri();
 	}
 	
 	public static final void prevOTriOrigin(OTriangle o, OTriangle that)
 	{
 		symOTri(o, that);
 		that.nextOTri();
 	}
 	
 	public final void prevOTriOrigin()
 	{
 		symOTri();
 		nextOTri();
 	}
 	
 	public static final void nextOTriDest(OTriangle o, OTriangle that)
 	{
 		symOTri(o, that);
 		that.prevOTri();
 	}
 	
 	public final void nextOTriDest()
 	{
 		symOTri();
 		prevOTri();
 	}
 	
 	public static final void prevOTriDest(OTriangle o, OTriangle that)
 	{
 		nextOTri(o, that);
 		that.symOTri();
 	}
 	
 	public final void prevOTriDest()
 	{
 		nextOTri();
 		symOTri();
 	}
 	
 	public static final void nextOTriApex(OTriangle o, OTriangle that)
 	{
 		nextOTri(o, that);
 		that.symOTri();
 		that.nextOTri();
 	}
 	
 	public final void nextOTriApex()
 	{
 		nextOTri();
 		symOTri();
 		nextOTri();
 	}
 	
 	public static final void prevOTriApex(OTriangle o, OTriangle that)
 	{
 		prevOTri(o, that);
 		that.symOTri();
 		that.prevOTri();
 	}
 	
 	public final void prevOTriApex()
 	{
 		prevOTri();
 		symOTri();
 		prevOTri();
 	}
 	
 	public final Vertex origin()
 	{
 		return tri.vertex[next3[orientation]];
 	}
 	
 	public final Vertex destination()
 	{
 		return tri.vertex[prev3[orientation]];
 	}
 	
 	public final Vertex apex()
 	{
 		return tri.vertex[orientation];
 	}
 	
 	//  The following 3 methods change the underlying triangle.
 	//  So they also modify all OTriangle bound to this one.
 	public final Vertex setOrigin(Vertex v)
 	{
 		tri.vertex[next3[orientation]] = v;
 		return v;
 	}
 	
 	public final Vertex setDestination(Vertex v)
 	{
 		tri.vertex[prev3[orientation]] = v;
 		return v;
 	}
 	
 	public final Vertex setApex(Vertex v)
 	{
 		tri.vertex[orientation] = v;
 		return v;
 	}
 	
 	/**
 	 * Sets adjacency relations between two triangles.
 	 *
 	 * @param sym  the triangle bond to this one.
 	 */
 	public final void glue(OTriangle sym)
 	{
 		tri.adj[orientation] = sym.tri;
 		sym.tri.adj[sym.orientation] = tri;
 		//  Clear previous adjacent position ...
 		tri.adjPos &= bitmaskclear[orientation];
 		//  ... and set it right
 		tri.adjPos |= (sym.orientation << (2*orientation));
 		sym.tri.adjPos &= bitmaskclear[sym.orientation];
 		sym.tri.adjPos |= (orientation << (2*sym.orientation));
 	}
 	
 	public final void dissolve()
 	{
 		tri.adj[orientation] = null;
 		tri.adjPos &= bitmaskclear[orientation];
 	}
 	
 	/*
 	 *                         a
 	 *                         ,
 	 *                        /|\
 	 *                       / | \
 	 *              oldLeft /  |  \ oldRight
 	 *                     /  v+   \
 	 *                    /   / \   \
 	 *                   /  /     \  \
 	 *                  / /         \ \
 	 *               o '---------------` d
 	 *                       (this)
 	 */
 	/**
 	 * Splits a triangle into three new triangles by inserting a vertex.
 	 *
 	 * Two new triangles have to be created, the last one is
 	 * updated.  For efficiency reasons, no checks are performed to
 	 * ensure that the vertex being inserted is contained by this
 	 * triangle.  Once triangles are created, edges are swapped if
 	 * they are not Delaunay.
 	 * Origin and destination points must not be at infinite, which
 	 * is the case when current triangle is returned by
 	 * getSurroundingTriangle().  If apex is Vertex.outer, then
 	 * getSurroundingTriangle() ensures that v.onLeft(o,d) &gt; 0.
 	 *
 	 * @param v  the vertex being inserted.
 	 */
 	public final boolean split3(Vertex v)
 	{
 		return split3(v, false);
 	}
 	
 	public final boolean split3(Vertex v, boolean force)
 	{
 		// Aliases
 		OTriangle oldLeft = work[0];
 		OTriangle oldRight = work[1];
 		OTriangle oldSymLeft = work[2];
 		OTriangle oldSymRight = work[3];
 		
 		prevOTri(this, oldLeft);         // = (aod)
 		nextOTri(this, oldRight);        // = (dao)
 		symOTri(oldLeft, oldSymLeft);    // = (oa*)
 		symOTri(oldRight, oldSymRight);  // = (ad*)
 		//  Set vertices of newly created and current triangles
 		Vertex o = origin();
 		assert o != Vertex.outer;
 		Vertex d = destination();
 		assert d != Vertex.outer;
 		Vertex a = apex();
 		
 		OTriangle newLeft  = new OTriangle(a, o, v);
 		OTriangle newRight = new OTriangle(d, a, v);
 		if (oldLeft.attributes != 0)
 		{
 			newLeft.attributes = oldLeft.attributes;
 			newLeft.updateAttributes();
 			oldLeft.attributes = 0;
 			oldLeft.updateAttributes();
 		}
 		if (oldRight.attributes != 0)
 		{
 			newRight.attributes = oldRight.attributes;
 			newRight.updateAttributes();
 			oldRight.attributes = 0;
 			oldRight.updateAttributes();
 		}
 		Triangle iniTri = tri;
 		v.tri = tri;
 		a.tri = newLeft.tri;
 		//  Move apex of current OTriangle.  As a consequence,
 		//  oldLeft is now (vod) and oldRight is changed to (dvo).
 		setApex(v);
 		
 		newLeft.glue(oldSymLeft);
 		newRight.glue(oldSymRight);
 		
 		//  Creates 3 inner links
 		newLeft.nextOTri();              // = (ova)
 		newLeft.glue(oldLeft);
 		newRight.prevOTri();             // = (vda)
 		newRight.glue(oldRight);
 		newLeft.nextOTri();              // = (vao)
 		newRight.prevOTri();             // = (avd)
 		newLeft.glue(newRight);
 		
 		//  Data structures have been created, search now for non-Delaunay
 		//  edges.  Re-use newLeft to walk through new vertex ring.
 		newLeft.nextOTri();              // = (aov)
 		Triangle newTri1 = newLeft.tri;
 		Triangle newTri2 = newRight.tri;
 		if (force)
 			CheckAndSwap(newLeft, oldRight);
 		else if (0 == CheckAndSwap(newLeft, oldRight))
 		{
 			//  v has been inserted and no edges are swapped,
 			//  thus global quality has been decreased.
 			//  Remove v in such cases.
 			o.tri = iniTri;
 			d.tri = iniTri;
 			a.tri = iniTri;
 			setApex(a);
 			nextOTri(this, oldLeft);         // = (oad)
 			oldLeft.glue(oldSymRight);
 			oldLeft.nextOTri();              // = (ado)
 			oldLeft.glue(oldSymLeft);
 			return false;
 		}
 		newTri1.addToMesh();
 		newTri2.addToMesh();
 		return true;
 	}
 	
 	private int CheckAndSwap(OTriangle newLeft, OTriangle newRight)
 	{
 		int nrSwap = 0;
 		Vertex v = newLeft.apex();
 		Vertex firstVertex = newLeft.origin();
 		while (firstVertex == Vertex.outer)
 		{
 			newLeft.nextOTriApex();
 			firstVertex = newLeft.origin();
 		}
 		//  Loops around v
 		Vertex a, o, d;
 		while (true)
 		{
 			boolean swap = false;
 			symOTri(newLeft, newRight);
 			o = newLeft.origin();
 			d = newLeft.destination();
 			a = newRight.apex();
 			if (o == Vertex.outer)
 				swap = (v.onLeft(d, a) < 0L);
 			else if (d == Vertex.outer)
 				swap = (v.onLeft(a, o) < 0L);
 			else if (a == Vertex.outer)
 				swap = (v.onLeft(o, d) == 0L);
 			else
 				swap = newLeft.isMutable() && !newLeft.isDelaunay(a);
 			
 			if (swap)
 			{
 				newLeft.swapOTriangle(v, a);
 				nrSwap++;
 			}
 			else
 			{
 				newLeft.nextOTriApex();
 				if (newLeft.origin() == firstVertex)
 					break;
 			}
 		}
 		return nrSwap;
 	}
 	
 	public final boolean isMutable()
 	{
 		return !(hasAttributes(BOUNDARY) || hasAttributes(OUTER));
 	}
 	
 	/**
 	 * Checks whether an edge is Delaunay.
 	 *
 	 * As apical vertices are already computed by calling routines,
 	 * they are passed as parameters for efficiency reasons.
 	 *
 	 * @param apex2  apex of the symmetric edge
 	 * @return <code>true</code> if edge is Delaunay, <code>false</code>
 	 * otherwise.
 	 */
 	public final boolean isDelaunay(Vertex apex2)
 	{
 		if (apex2.isPseudoIsotropic())
 			return isDelaunay_isotropic(apex2);
 		return isDelaunay_anisotropic(apex2);
 	}
 	
 	private final boolean isDelaunay_isotropic(Vertex apex2)
 	{
 		assert Vertex.outer != origin();
 		assert Vertex.outer != destination();
 		assert Vertex.outer != apex();
 		Vertex vA = origin();
 		Vertex vB = destination();
 		Vertex v1 = apex();
 		long tp1 = vA.onLeft(vB, v1);
 		long tp2 = vB.onLeft(vA, apex2);
 		long tp3 = apex2.onLeft(vB, v1);
 		long tp4 = v1.onLeft(vA, apex2);
 		if (Math.abs(tp3) + Math.abs(tp4) < Math.abs(tp1)+Math.abs(tp2) )
 			return true;
		if (tp1 > 0L && tp4 > 0L)
 		{
			if (tp1 <= 0L || tp2 <= 0L)
 				return true;
 		}
 		return !apex2.inCircleTest2(this);
 	}
 	
 	private final boolean isDelaunay_anisotropic(Vertex apex2)
 	{
 		assert Vertex.outer != origin();
 		assert Vertex.outer != destination();
 		assert Vertex.outer != apex();
 		if (apex2 == Vertex.outer)
 			return true;
 		if (hasAttributes(SWAPPED))
 			return true;
 		if (apex2.inCircleTest3(this))
 		{
 			setAttributes(SWAPPED);
 			return false;
 		}
 		return true;
 	}
 	
 	public final long get2Area()
 	{
 		return tri.vertex[0].onLeft(tri.vertex[1], tri.vertex[2]);
 	}
 	
 	/*
 	 * Swaps an edge.
 	 *
 	 * This routine swaps an edge (od) to (na), updates
 	 * adjacency relations and backward links between vertices and
 	 * triangles.  Current object is transformed from (oda) to (ona)
 	 * and not (nao), because this helps turning around o, e.g.
 	 * at the end of split3.
 	 *
 	 * @param a  apex of the current edge
 	 * @param n  apex of the symmetric edge
 	 * @return a handle to (ona) oriented triangle.
 	 * otherwise.
 	 */
 	private final OTriangle swapOTriangle(Vertex a, Vertex n)
 	{
 		Vertex o = origin();
 		Vertex d = destination();
 		/*
 		 *            d                    d
 		 *            .                    .
 		 *           /|\                  / \
 		 *       a1 / | \ a4         a1  /   \ a4
 		 *         /  |  \              /     \
 		 *      a +   |   + n        a +-------+ n
 		 *         \  |  /              \     /
 		 *       a2 \ | / a3         a2  \   / a3
 		 *           \|/                  \ /
 		 *            '                    '
 		 *            o                    o
 		 *                                 .
 		 *            |                   /|\
 		 *            |                    |
 		 *           \|/                   |
 		 *            '
 		 *            d                    n
 		 *            .                    .
 		 *           /|\                  / \
 		 *       a2 / | \ a1         a1  /   \ a4
 		 *         /  |  \              /     \
 		 *      a +   |   + n   is   d +-------+ o
 		 *         \  |  /              \     /
 		 *       a3 \ | / a4         a2  \   / a3
 		 *           \|/                  \ /
 		 *            '                    '
 		 *            o                    a
 		 */
 		// this = (oda)
 		symOTri(this, work[0]);         // (don)
 		nextOTri(this, work[1]);        // (dao)
 		int attr1 = work[1].attributes;
 		work[1].symOTri();              // a1 = (ad*)
 		prevOTri(this, work[2]);        // (aod)
 		int attr2 = work[2].attributes;
 		work[2].symOTri();              // a2 = (oa*)
 		nextOTri();                     // (dao)
 		work[2].glue(this);    // a2 and (dao)
 		nextOTri(work[0], work[2]);     // (ond)
 		int attr3 = work[2].attributes;
 		work[2].symOTri();              // a3 = (no*)
 		nextOTri();                     // (aod)
 		work[2].glue(this);    // a3 and (aod)
 		//  Reset 'this' to (oda)
 		nextOTri();                     // (oda)
 		prevOTri(work[0], work[2]);     // (ndo)
 		int attr4 = work[2].attributes;
 		work[2].symOTri();              // a4 = (dn*)
 		work[0].nextOTri();             // (ond)
 		work[2].glue(work[0]); // a4 and (ond)
 		work[0].nextOTri();             // (ndo)
 		work[1].glue(work[0]); // a1 and (ndo)
 		work[0].nextOTri();             // (don)
 		//  Adjust vertices
 		setOrigin(n);
 		setDestination(a);
 		setApex(o);
 		work[0].setOrigin(a);
 		work[0].setDestination(n);
 		work[0].setApex(d);
 		//  Fix links to triangles
 		n.tri = tri;
 		a.tri = tri;
 		o.tri = tri;
 		d.tri = work[0].tri;
 		//  Fix attributes
 		tri.adjPos &= 0xff;
 		tri.adjPos |= ((attr2 & 0xff) << (8*(1+next3[orientation])));
 		tri.adjPos |= ((attr3 & 0xff) << (8*(1+prev3[orientation])));
 		work[0].tri.adjPos &= 0xff;
 		work[0].tri.adjPos |= ((attr4 & 0xff) << (8*(1+next3[work[0].orientation])));
 		work[0].tri.adjPos |= ((attr1 & 0xff) << (8*(1+prev3[work[0].orientation])));
 		//  Eventually change 'this' to (ona) to ease moving around o.
 		prevOTri();                     // (ona)
 		return this;
 	}
 	
 	/**
 	 * Tries to rebuild a boundary edge by swapping edges.
 	 *
 	 * This routine is applied to an oriented triangle, its origin
 	 * is an end point of the boundary edge to rebuild.  The other end
 	 * point is passed as an argument.  Current oriented triangle has
 	 * been set up by calling routine so that it is the leftmost edge
 	 * standing to the right of the boundary edge.
 	 * A traversal between end points is performed, and intersected
 	 * edges are swapped if possible.  At exit, current oriented
 	 * triangle has <code>end</code> as its origin, and is the
 	 * rightmost edge standing to the left of the inverted edge.
 	 * This algorithm can then be called iteratively back and forth,
 	 * and it is known that it is guaranteed to finish.
 	 *
 	 * @param end  end point of the boundary edge.
 	 * @return the number of intersected edges.
 	 */
 	public final int forceBoundaryEdge(Vertex end)
 	{
 		long newl, oldl;
 		int count = 0;
 		
 		Vertex start = origin();
 
 		nextOTri();
 		while (true)
 		{
 			count++;
 			Vertex o = origin();
 			Vertex d = destination();
 			Vertex a = apex();
 			symOTri(this, work[0]);
 			work[0].nextOTri();
 			Vertex n = work[0].destination();
 			newl = n.onLeft(start, end);
 			oldl = a.onLeft(start, end);
 			boolean canSwap = (n != Vertex.outer) && (a.onLeft(n, d) > 0L) && (a.onLeft(o, n) > 0L) && !hasAttributes(BOUNDARY);
 			if (newl > 0L)
 			{
 				//  o stands to the right of (start,end), d and n to the left.
 				if (!canSwap)
 					prevOTriOrigin();    // = (ond)
 				else if (oldl >= 0L)
 				{
 					//  a stands to the left of (start,end).
 					swapOTriangle(a, n); // = (ona)
 				}
 				else if (rand.nextBoolean())
 					swapOTriangle(a, n); // = (ona)
 				else
 					prevOTriOrigin();    // = (ond)
 			}
 			else if (newl < 0L)
 			{
 				//  o and n stand to the right of (start,end), d to the left.
 				if (!canSwap)
 					nextOTriDest();      // = (ndo)
 				else if (oldl <= 0L)
 				{
 					//  a stands to the right of (start,end).
 					swapOTriangle(a, n); // = (ona)
 					nextOTri();          // = (nao)
 					prevOTriOrigin();    // = (nda)
 				}
 				else if (rand.nextBoolean())
 				{
 					swapOTriangle(a, n); // = (ona)
 					nextOTri();          // = (nao)
 					prevOTriOrigin();    // = (nda)
 				}
 				else
 					nextOTriDest();      // = (ndo)
 			}
 			else
 			{
 				//  n is the end point.
 				if (!canSwap)
 					nextOTriDest();      // = (ndo)
 				else
 				{
 					swapOTriangle(a, n); // = (ona)
 					nextOTri();          // = (nao)
 					if (oldl < 0L)
 						prevOTriOrigin();// = (nda)
 				}
 				break;
 			}
 		}
 		if (origin() != end)
 		{
 			//  A midpoint is aligned with start and end, this should
 			//  never happen.
 			throw new RuntimeException("Point "+origin()+" is aligned with "+start+" and "+end);
 		}
 		return count;
 	}
 	
 	public final String toString()
 	{
 		String r = "Orientation: "+orientation+"\n";
 		r += "Attributes: "+attributes+" "+Integer.toHexString(tri.adjPos >> 8)+"\n";
 		r += "Vertices:\n";
 		r += "  Origin: "+origin()+"\n";
 		r += "  Destination: "+destination()+"\n";
 		r += "  Apex: "+apex()+"\n";
 		return r;
 	}
 
 }
