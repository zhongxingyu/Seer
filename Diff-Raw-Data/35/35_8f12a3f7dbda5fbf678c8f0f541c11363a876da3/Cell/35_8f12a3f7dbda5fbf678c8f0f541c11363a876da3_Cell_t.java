 package ddddbb.comb;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import ddddbb.math.D3Graphics;
 import ddddbb.math.HalfSpace;
 import ddddbb.math.OHalfSpace;
 import ddddbb.math.Point;
 import ddddbb.math.Point3d;
 import ddddbb.math.Space2d;
 
 public class Cell extends ACell {
 	protected Vector<OCell> facets = new Vector<OCell>(); //dim-1
 
 	//this cell is referenced from the following OCells
 	protected HashSet<OCell> referrers = new HashSet<OCell>(); //dim
 	
 	Location location;
 	SpaceId spaceId;
 
 	protected Cell inner; // dim
 	protected Cell outer; // dim
 	/* innerCut and outerCut are copies, they dont belong to any other facets */
 	protected Cell cut;
 	protected Vector<Cell> contained_facets;
 	
 	/** use the projection to 3d space for computations, regardless of spaceDim */
 	//public boolean as3d = true;
 	
 	protected int splitCellIs = PRISTINE;
 	
 	public static final int SPLITTED = 0;
 	public static final int INNER = -1;
 	public static final int OUTER = 1;
 	public static final int CONTAINED = 2;
 	public static final int PRISTINE = 3;
 
 //	private boolean internal = false;
 
 	public Cell(double[][][] _facets) {
 		this(pointCells(_facets),new Vector<Cell>(),new Vector<Cell>(),new Vector<Cell>());
 	}
 
 	private Cell(Cell[][][] _facets, Vector<Cell> f2, Vector<Cell> f1, Vector<Cell> f0) {
 		location = new Location(_facets[0][0][0].spaceDim(),3);
 		spaceId = new SpaceId();
 		for (Cell[][] f: _facets) {
 			Cell c = lookup(f2,new Cell(f,f1,f0));
 			new OCell(c,this);//this is twice
 		}
 		assert checkClosed();
 	}
 	
 	private Cell(Cell[][] _facets, Vector<Cell> f1, Vector<Cell> f0) {
 		location = new Location(_facets[0][0].spaceDim(),2);
 		spaceId = new SpaceId();
 		for (Cell[] f: _facets) {
 			Cell c = lookup(f1,new Cell(f,f0));
 			OCell fcell = new OCell(c,this);//this is twice
 			assert c.checkFaces();
 			assert fcell.checkFaces();
 		}
 		assert checkClosed();
 		for (OCell oc:facets) {
 			assert oc.checkFaces();
 		}
 	}
 
 	private Cell(Cell[] ab, Vector<Cell> f0) {
 		location = new Location(ab[0].spaceDim(),1);
 		spaceId = new SpaceId();
 		assert ab.length == 2 : ab.length;
 		new OCell(lookup(f0,ab[0]),this);
 		new OCell(lookup(f0,ab[1]),this);
 		assert facets().size() == 2 : facets().size();
 		assert checkClosed();
 		assert checkFaces();
 	}
 	
 	public static <A> A lookup(Vector<A> list,A src) {
 		for (A p:list) {
 			if (p.equals(src)) { return p; }
 		}
 		list.add(src);
 		return src;
 	}	
 	
 	public boolean equals(Object o) {
 		Cell c = (Cell)o;
 		if (dim()!=c.dim()) { return false; }
 		if (dim()==0) { 
 			return location.equals(c.location);
 		}
 		if (facets.size()!=c.facets.size()) { return false; }
 		for (int i=0;i<facets.size();i++) {
 			boolean found = false;
 			for (int j=0;j<facets.size();j++) {
 				if (facets.get(i).cell().equals(c.facets.get(j).cell())) { found = true; break; }
 			}
 			if (!found) { return false; }
 		}
 		return true;
 	}
 
 	public static Cell[][][] pointCells(double[][][] x) {
 		Cell[][][] res = new Cell[x.length][][];
 		for (int i=0;i<x.length;i++) {
 			res[i] = new Cell[x[i].length][2];
 			for (int j=0;j<x[i].length;j++) {
 				assert x[i][j].length == 6;
 				res[i][j][0] = new Cell(new Point3d(x[i][j][0],x[i][j][1],x[i][j][2]),new SpaceId());
 				res[i][j][1] = new Cell(new Point3d(x[i][j][3],x[i][j][4],x[i][j][5]),new SpaceId());
 			}
 		}
 		return res;
 	}
 	
 	public Cell(Point point, SpaceId _spaceId) {
 		location = new Location(point,true);
 		spaceId = _spaceId;
 	}
 	
 	protected Cell() {}
 	
 //	public Cell(Location _location) {
 //		assert _location.dim() == 0;
 //		location = _location;
 //	}
 	
 //	public Cell(Location a, Location b) {
 //		location = new Location(3,1);
 //		new OCell(new Cell(a),this,-1);
 //		new OCell(new Cell(b),this,+1);
 //	}
 	
 	
 	/** Make a cell out of the given _facets,
 	 *  Changes their parents to this cell!
 	 */
 //	private Cell(Collection<OCell> _facets,boolean _dummy) {
 //		assert ! _facets.isEmpty();
 //		assert _facets.iterator().next() != null :
 //			null;
 //		location = new Location(3,_facets.iterator().next().dim()+1);
 //		
 //		for (OCell of: _facets) {
 //			of.connectParent(this);
 //		}
 //		assert checkFacetsDim();
 //		if ( ! checkClosed() ) { 
 //			assert false;
 //		}
 //		assert dim() != 1 || facets().size() == 2 : facets().size();
 //	}
 //	
 	public static Cell create(Collection<OCell> facets, SpaceId _spaceId) {
 		Cell c = new Cell();
 		assert ! facets.isEmpty();
 		assert facets.iterator().next() != null :
 			null;
 		c.location = new Location(3,facets.iterator().next().dim()+1);
 		c.spaceId = _spaceId;
 		
 		for (OCell of: facets) {
 			of.connectParent(c);
 		}
 		assert c.checkFacetsDim();
 		if ( ! c.checkClosed() ) { 
 			assert false;
 		}
 		assert c.dim() != 1 || c.facets().size() == 2 : c.facets().size();
 		return c;
 	}
 
 	private Cell(Collection<Cell> _facets, SpaceId _spaceId) { //weird: Java can not distinguish between List<Cell> and List<OCell>
 		assert ! _facets.isEmpty();
 		location = new Location(3,_facets.iterator().next().dim()+1);
 		spaceId = _spaceId;
 		for (Cell f :_facets) {
 			new OCell(f,this);
 		}
 		assert checkFacetsDim();
 		if ( ! checkClosed() ) { assert false; }
 		assert dim() != 1 || facets.size() == 2 : 
 			facets;
 	}
 	
 	public List<? extends BCell> facets() {
 		Vector<OCell> res = new Vector<OCell>();
 		for (OCell f:facets) {
 			res.add(f);
 		}
 		return res;
 	}
 	
 	public boolean isSplitted() {
 		return splitCellIs == SPLITTED;
 	}
 	public boolean isInner() {
 		return splitCellIs == INNER;
 	}
 	public boolean isOuter() {
 		return splitCellIs == OUTER;
 	}
 	public boolean isContained() {
 		return splitCellIs == CONTAINED;
 	}
 	
 //	public List<Cell> facets() {
 //		Vector<Cell> res = new Vector<Cell>();
 //		for ( FragmentedCell f : facets) {
 //			res.add(f.shape());
 //		}
 //		return res;
 //	}
 
 	/** returns a copy of the normal of this Cell */
 	public Point normal() {
 		assert halfSpace().normal.isNormal();
 		return halfSpace().normal.clone();
 	}
 	
 	public ALocation location() {
 		return location;
 	}
 
 	//temporarily
 	//public Space2d space2d;
 
 	public void computeSpacesIN() {
 		assert spaceDim() == 3 : spaceDim();
 		assert dim() == 3;
 		Collection<ALocation> locations = getPointLocations(false);
 		for (OCell f2:facets) {
 			assert f2.cell().halfSpaces() != null || f2.orientation == 0 :
 				f2.cell().halfSpaces() + "|" + f2.orientation;
 			f2.cell().computeSpace2dIN();
 			if (f2.orientation != 0) { continue; }
 			f2.orientation = +1;
 			for ( ALocation l : locations ) {
 				if ( f2.cell().halfSpace().side(l.o()) == 1 ) {
 					f2.orientation = -1;
 					break;
 				}
 			}
 		}
 	}
 	
 	public void computeSpace2dIN() {
 		assert spaceDim() == 3;
 		assert dim() == 2;
 		if (halfSpace()!=null) { return; }
 		location.setHalfSpace(new Space2d(facets));
 	}
 
 	private Point cutPoint(OHalfSpace e) {
 		assert spaceDim() == 3;
 		assert dim() == 1;
 		Point a = a().o();
 		Point b = b().o();
 		double ad = e.dist(a);
 		double bd = e.dist(b);
 		return e.proj(a).multiply(bd/(ad+bd)).add(e.proj(b).multiply(ad/(ad+bd)));
 	}
 	
 //	/** INNER means INNER with possible touch
 //	 *  OUTER means OUTER with possible touch
 //	 */
 //	public int side(Space2d e, int orientation) {
 //		assert spaceDim() == 3;
 //		Collection<ALocation> locations = getPointLocations(false);
 //		int side = PRISTINE;
 //		for ( ALocation l : locations ) {
 //			if ( e.outer(l.p3)*orientation == -1 ) { //inner
 //				if ( side == PRISTINE ) { side = INNER; continue; }
 //				if ( side == CONTAINED ) { side = INNER; continue; }
 //				if ( side == OUTER ) { return SPLITTED; }
 //				// unchanged for side==INNER
 //				continue;
 //			}
 //			if ( e.outer(l.p3)*orientation ==  1 ) {
 //				if ( side == PRISTINE ) { side = OUTER; continue; }
 //				if ( side == CONTAINED ) { side = OUTER; continue; }
 //				if ( side == INNER) { return SPLITTED; }
 //				// unchanged for side==OUTER
 //				continue;
 //			}
 //			if ( e.outer(l.p3) == 0 ) {
 //				if ( side == PRISTINE ) { side = CONTAINED; continue; }
 //				//unchanged for side == INNER, OUTER, CONTAINED
 //				continue;
 //			}
 //		}
 //		return side;
 //	}
 	
 	public void split(OHalfSpace e,OCell referer) {
 		assert spaceId !=null : dim();
 		assert referer==null || referrers.contains(referer);
 		assert e != null;
 		if ( isSplitted()) { return; }
 
 		Set<Cell> cut_facets = new HashSet<Cell>(); // dim - 2
 		Vector<OCell> inner_facets = new Vector<OCell>(); // dim - 1
 		Vector<OCell> outer_facets = new Vector<OCell>(); // dim - 1
 		
 		OCell innerCut = null;
 		OCell outerCut = null;
 		
 		//if this cell was split, it will no more be used
 		//if it however was only touched, it will be reused and so we have to
 		//ascertain that the variables are in the initial state
 		cut = null;
 		inner = null;
 		outer = null;
 		splitCellIs = PRISTINE;
 		contained_facets = new Vector<Cell>(); // dim - 1
 
 		assert checkFaces();
 				
 		inner = null; outer = null; 
 		
 		if ( dim() == 0 ) {
 			Point p = o();
 			if (e.outer(p) == -1 ) {
 				splitCellIs = INNER;
 				return;
 			}
 			if (e.outer(p) ==  1 ) {
 				splitCellIs = OUTER;
 				return;
 			}
 			assert e.outer(p) == 0;
 			splitCellIs = CONTAINED;
 			return;
 		}
 		else if ( dim() == 1 ) {
 			assert facets().size() == 2;
 
 			OCell af = facets.get(0);
 			OCell bf = facets.get(1);
 			Point a = af.cell().location.p3;
 			Point b = bf.cell().location.p3;
 			int sideA = e.outer(a);
 			int sideB = e.outer(b);
 			if      (sideA==-1 && sideB== 1) {
				cut = new Cell(cutPoint(e),spaceId.cut(e.space()));
 				inner_facets.add(af);
 				outer_facets.add(bf);
 				splitCellIs = SPLITTED;
 				//continue below
 			}
 			else if (sideA== 1 && sideB==-1) {
				cut = new Cell(cutPoint(e),spaceId.cut(e.space()));
 				inner_facets.add(bf);
 				outer_facets.add(af);
 				splitCellIs = SPLITTED;
 				//continue below
 			}
 			else if (sideA== 0 && sideB== 1) {
 				contained_facets.add(a());
 				splitCellIs = OUTER;
 				return;
 			}
 			else if (sideA== 1 && sideB== 0) {
 				contained_facets.add(b());
 				splitCellIs = OUTER;
 				return;
 			}
 			else if (sideA== 0 && sideB==-1) {
 				contained_facets.add(a());
 				splitCellIs = INNER;
 				return;
 			}
 			else if (sideA==-1 && sideB== 0) {
 				contained_facets.add(b());
 				splitCellIs = INNER;
 				return;
 			}
 			else if (sideA== 0 && sideB== 0) {
 				splitCellIs = CONTAINED;
 				return;
 			}
 			else if (sideA== 1 && sideB== 1) {
 				splitCellIs = OUTER;
 				return;
 			}
 			else if (sideA==-1 && sideB==-1) {
 				splitCellIs = INNER;
 				return;
 			}
 			assert splitCellIs == SPLITTED;
 		}
 		else { assert dim() > 1;
 			for (OCell of : facets) {
 				Cell f = of.cell();
 				assert of.parent() == this;
 				of.cell().split(e,of);
 				assert of.cell().referrers.contains(of);
 				if ( f.isSplitted()) {
 					inner_facets.add(of.inner);
 					outer_facets.add(of.outer);
 					cut_facets.add(f.cut);
 				}
 				else if ( f.isInner() ) {
 					inner_facets.add(of);
 					cut_facets.addAll(f.contained_facets);
 				}
 				else if ( f.isOuter() ) {
 					outer_facets.add(of);
 					cut_facets.addAll(f.contained_facets);
 				}
 				else if ( f.isContained()) {
 					contained_facets.add(f);
 				}
 				else {
 					assert false;
 				}
 			}
 
 			if (inner_facets.isEmpty() && outer_facets.isEmpty()) {
 				assert contained_facets.size() == facets.size();
 				splitCellIs = CONTAINED;
 				assert checkFaces();
 				return;
 			}
 			else if (inner_facets.isEmpty()) {
 				assert contained_facets.size() < facets().size() :
 					contained_facets + "|" + facets();
 				splitCellIs = OUTER;
 				assert checkFaces();
 				return;
 			}
 			else if (outer_facets.isEmpty()) {
 				assert contained_facets.size() < facets().size();
 				splitCellIs = INNER;
 				assert checkFaces();
 				return;
 			}
 			else if ((! inner_facets.isEmpty()) && (! outer_facets.isEmpty())) {
				cut = new Cell(cut_facets,spaceId.cut(e.space()));
 				splitCellIs = SPLITTED;
 				//continue below
 			}
 			else {
 				assert false;
 			}
 			assert splitCellIs == SPLITTED;
 		}
 		
 		assert splitCellIs == SPLITTED;
 
 		innerCut = new OCell(cut);
 		outerCut = new OCell(cut);
 		
 		inner_facets.add(innerCut);
 		outer_facets.add(outerCut);
 
 		inner = create(inner_facets,spaceId);
 		outer = create(outer_facets,spaceId);
 		
 		if (dim()+1==spaceDim()) {
 			inner.location.setHalfSpace(halfSpace());
 			outer.location.setHalfSpace(halfSpace());
 		}
 
 		if (dim()==spaceDim()) {
 			cut.location.setHalfSpace(e.space());
 			outerCut.orientation = -e.orientation();
 			innerCut.orientation = e.orientation();
 		}
 
 		assert OCell.opposite(innerCut, outerCut);
 		assert innerCut.snappedTo() == outerCut;
 
 		assert referer==null || referrers.contains(referer);
 
 		for (OCell oc:referrers) {
 			oc.breakOpen();
 			//dont catch the parents that we just iterating
 			if (oc==referer) { continue; }
 			if (oc.parent()!=null) {
 				int i = oc.parent().facets.indexOf(oc);
 				oc.parent().facets.set(i,oc.inner);
 				oc.parent().facets.add(oc.outer);
 			}
 		}
 
 		assert innerCut.location() == outerCut.location();
 		assert inner_facets.iterator().next().dim() == innerCut.dim() : inner_facets.iterator().next().dim() + "," + innerCut.dim();
 		
 		assert inner.checkFaces();
 		assert outer.checkFaces();
 		
 		return;
 	}
 	
 	public void snapOut() {
 		for (OCell f: facets) {
 			f.snapOut();
 		}
 	}
 	
 	/** Removes all references to this and removes resulting unreferenced cells */
 	public void remove() {
 		for (OCell f: facets) {
 //			f.nullifyParent();
 			HashSet<OCell> frefs = f.cell().referrers;
 			frefs.remove(f);
 			if (frefs.isEmpty()) f.cell().remove();
 		}
 //		facets.clear();
 		for (OCell r:referrers) {
 			r.nullifyCell();
 		}
 //		referrers.clear();
 	}
 	
 	/** Convenience for 1d cell, returns the first point */
 	public Cell a() {
 		assert dim() == 1;
 		return facets.get(0).cell();
 	}
 	/** Convenience for 1d cell, returns the second point */
 	public Cell b() {
 		assert dim() == 1;
 		return facets.get(1).cell();
 	}
 	
 	/** paints this facet onto g3 assuming that it was already
 	 *  3-projected 
 	 */
 	public void paint(D3Graphics g3, boolean woInternals) {
 		if (isInternal() && woInternals) { return; }
 		if (isSplitted()) {
 			inner.paint(g3, woInternals);
 			outer.paint(g3, woInternals);
 			return;
 		}
 		if (dim()==1) {
 			g3.drawLine((Point3d)a().o(), (Point3d)b().o());
 			return;
 		}
 		for (OCell f : facets) {
 			f.paint(g3,woInternals);
 		}
 	}
 
 	/**
 	 * returns all unhidden faces of dimension d
 	 * @param d dimension
 	 * @return Collection of faces
 	 */
 	public Collection<Cell> getFaces(int d,boolean withInternalFaces) {
 		HashSet<Cell> res = new HashSet<Cell>();
 		if (!withInternalFaces && isInternal()) { return res; }
 		if (d>dim()) { return res; }
 		if (d==dim()) { 
 			res.add(this);
 			return res;
 		}
 		for (OCell f: facets) {
 			res.addAll(f.cell().getFaces(d,withInternalFaces));
 		}
 		return res;
 	}
 	
 	public Collection<Cell> getFacesDownTo(int d, boolean withInternalFaces) {
 		HashSet<Cell> res = new HashSet<Cell>();
 		if (!withInternalFaces && isInternal()) { return res; }
 		if (d>dim()) { return res; }
 		res.add(this);
 		for (OCell f: facets) {
 			assert f.cell() != null;
 			res.addAll(f.cell().getFacesDownTo(d,withInternalFaces));
 		}
 		return res;
 	}
 
 	public void addAllFaces(HashSet<OCell> res) {
 		for (OCell o: facets) {
 			o.cell().addAllFaces(res);
 		}
 		return;
 	}
 	
 	public Point center() {
 		Point center = Point.create(spaceDim());
 		int n=0;
 		for (OCell f: facets) {
 				center.add(f.cell().center());
 				n++;
 		}
 		center.multiply(1.0/n);
 		return center;
 	}
 	
 	public Point3d center3d() {
 		if (dim()==0) {
 			return (Point3d)o();
 		}
 		Point3d center = new Point3d();
 		int n=0;
 		for (OCell f: facets) {
 			center.add(f.cell().center3d());
 			n++;
 		}
 		center.multiply(1.0/n);
 		return center;
 	}
 
 //	public Vector<Cell> cutOut(Iterable<OHalfSpace> planes) {
 //		Vector<Cell> res = new Vector<Cell>();
 //		
 //		/* If this cell is outside one of the planes there is nothing to do*/
 //		for (OHalfSpace e:planes) {
 //			if (e.side(this) == Cell.OUTER) {
 //				res.add(this);
 //				return res;
 //			}
 //		}
 //		
 //		/* Otherwise it will be cut into pieces */
 //
 //		Cell innerCell = this;
 //
 //		for (OHalfSpace e:planes) {
 //			innerCell.split(e,null);
 //			res.add(innerCell.outer);
 //			innerCell = innerCell.inner;
 //			assert innerCell.checkPointRefs() :
 //				innerCell.getPointLocations();
 //		}
 //		
 //		assert innerCell != null;
 //		innerCell.snapOut();
 //		return res;
 //	}
 	
 	public boolean isInternal() {
 		
 		// if it is not referenced from anywhere it is a top level cell and visible
 		if (referrers.size()==0) { return false; }
 		
 		/* go through the OCells that refer to this Cell and determine 
 		 * whether they are all internal
 		 * If only one is visible this whole Cell is visible */
 		for (OCell oc:referrers) {
 			if (!oc.isInternal()) return false;
 			assert oc.snappedTo() ==null || referrers.contains(oc.snappedTo());
 		}
 		return true;
 	}
 	
 //	public String toString() {
 //		if (dim()==0) {
 //			return o().toString();
 //		}
 //		if ( dim() == 1 ) {
 //				return "[" + a() + "," + b() + "]";
 //		}
 //		return facets.toString();
 //	}
 
 	protected boolean checkFacetsDim() {
 		for (ACell fm1: facets()) {
 			if (fm1.dim() != dim() -1) { 
 				return false; 
 			} 
 		}
 		return true;
 	}
 
 	public Cell inner() {
 		return inner;
 	}
 	public Cell outer() {
 		return outer;
 	}
 	
 	boolean checkSnap() {
 		for (OCell f: facets) {
 			if ( ! f.checkSnap() ) { return false; }
 		}
 		return true;
 	}
 	
 	public boolean checkPointRefs() {
 		Vector<ALocation> pointrefs = getPointLocations();
 		for (int i=0;i<pointrefs.size();i++) {
 			for (int j=i+1;j<pointrefs.size();j++) {
 				if (pointrefs.get(i)!=pointrefs.get(j) && pointrefs.get(i).equals(pointrefs.get(j))) {
 					System.out.println(""+pointrefs.get(i)+pointrefs.get(i).hashCode()+pointrefs.get(j)+pointrefs.get(j).hashCode());
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public boolean checkCellrefs() {
 		Vector<OCell> cellrefs = new Vector<OCell>();
 		return checkCellrefs(cellrefs);
 	}
 
 	public boolean checkCellrefs(Vector<OCell> cellrefs) {
 		for (OCell of:facets) {
 			Cell f = of.cell();
 			for (OCell or:cellrefs) {
 				Cell r=or.cell();
 				if ( f!=r && f.equals(r)) {
 					assert or.src.location()._dst == or.cell();
 					System.out.println(or+""+or.src + "" + or.src.location().hashCode());
 					System.out.println(of+""+of.src + "" + of.src.location().hashCode());
 					assert false:
 						false;
 				}
 			}
 			assert f.checkCellrefs(cellrefs);
 			cellrefs.add(of);
 		}
 		return true;
 	}
 	
 	public boolean checkSnapCell() {
 		for(OCell of:facets) { if (! of.checkSnapCell()) { return false; } }
 		return true;
 	}
 
 	public HalfSpace halfSpace() {
 		assert dim()+1==spaceDim();
 		//assert halfSpaces().size() == 1;
 		return location.halfSpace();
 	}
 
 	public HashSet<HalfSpace> halfSpaces() {
 		HashSet<HalfSpace> res = new HashSet<HalfSpace>();
 		if (dim()==spaceDim()) { return res; }
 		if (dim()+1==spaceDim()) { res.add(halfSpace()); return res; }
 		for (OCell oc:referrers) {
 			res.addAll(oc.parent().halfSpaces());
 		}
 		return res;
 	}
 	
 	public HashSet<OCell> getReferrers() {
 		return referrers;
 	}
 	public SpaceId getSpace() {
 		return spaceId;
 	}
 }
