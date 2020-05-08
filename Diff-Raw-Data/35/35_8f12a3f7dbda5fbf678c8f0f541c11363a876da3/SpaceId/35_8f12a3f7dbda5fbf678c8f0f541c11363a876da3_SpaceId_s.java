 package ddddbb.comb;
 
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 import ddddbb.math.OHalfSpace;
 
 /** We want to have the same SpaceId for equal spaces */
 public class SpaceId {
 	static Hashtable<DSpace, SpaceId> dspaceMemo = new Hashtable<DSpace, SpaceId>(); 
 	
	HashMap<OHalfSpace,SpaceId> memo = new HashMap<OHalfSpace,SpaceId>();
 	
 	//for debuggin purpose only
	Vector<OHalfSpace> halfSpaces;
 	SpaceId base;
 	DSpace dspace;
 	
 	static SpaceId from(DSpace dspace) {
 		if (!dspaceMemo.containsKey(dspace)) {
 			dspaceMemo.put(dspace, new SpaceId(dspace));
 		}
 		return dspaceMemo.get(dspace);
 	}
 	
 	private SpaceId(DSpace _dspace) {
		halfSpaces = new Vector<OHalfSpace>();
 		dspace = _dspace.clone(); 
 	}
 	
 	protected SpaceId() {
 		System.out.println("n:" + dspace);
		halfSpaces = new Vector<OHalfSpace>();
 		base = null;
 	}
	private SpaceId(SpaceId b, Vector<OHalfSpace> hs) {
 		base = b;
		halfSpaces = new Vector<OHalfSpace>(hs);
 	}
 	
	public SpaceId cut(OHalfSpace e) {
 		if (memo.containsKey(e)) {
 			return memo.get(e);
 		}
 		SpaceId res = new SpaceId(this,halfSpaces);
 		res.halfSpaces.add(e);
 		
 		memo.put(e, res);
 		return res;
 	}
 	
 	public static void main(String[] args) {
 		DLocation a,b,c;
 		a = new DLocation(new int[] {0,0,0,0}, new int[] {1,2,3});
 		b = new DLocation(new int[] {0,0,0,0}, new int[] {3,1,2});
 		c = new DLocation(new int[] {0,2,1,1}, new int[] {1,2,2});
 		assert a.equals(b);
 		assert ! a.equals(c);
 		assert a.hashCode() == b.hashCode() : a.hashCode() + "," + b.hashCode();
 		assert a.hashCode() == c.hashCode() : a.hashCode() + "," + c.hashCode();
 		Hashtable<DLocation,SpaceId> ht = new Hashtable<DLocation, SpaceId>();
 		ht.put(a, new SpaceId());
 		assert ht.containsKey(b);
 		assert ! ht.containsKey(c);
 		
 		HashMap<DLocation,SpaceId> hm = new HashMap<DLocation, SpaceId>();
 		hm.put(a,new SpaceId());
 		assert hm.containsKey(b);
 		assert ! hm.containsKey(c);
 	}
 }
