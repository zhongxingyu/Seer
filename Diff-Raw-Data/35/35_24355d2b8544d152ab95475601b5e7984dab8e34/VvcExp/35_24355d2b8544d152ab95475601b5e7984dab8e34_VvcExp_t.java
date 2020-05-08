 import java.util.List;
 
 public class VvcExp extends CuExpr {
 	private CuVvc data_v; 
 	private List<CuType> data_pt;
 	private List<CuExpr> data_es;
 	public VvcExp(CuVvc v, List<CuType> pt, List<CuExpr> es){
 		this.data_v=v;
 		this.data_pt=pt;
 		this.data_es=es;
 		
 		super.text=v.toString()+" < "+printLst(pt)+" > ("+printLst(es)+" )";
 	}
	
	private <T>String printLst(List<T> es) {
		String s = "";
		for (T e : es) {
			s += e.toString() + " ";
 		}
		return s;
 	}
 }
