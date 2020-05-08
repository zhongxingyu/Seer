 import java.util.ArrayList;
 import java.util.List;
 
 
 public abstract class CuFunC {
 	protected String text = "";
 	@Override public String toString() {
 		return text;
 	}
 	public void add(CuVvc v, CuTypeScheme ts) {}
 }
 
class Function extends CuFunC {
 	public List<CuVvc> v = new ArrayList<CuVvc>();
 	public List<CuTypeScheme> ts = new ArrayList<CuTypeScheme>();
 	
	public Function() {
 	}
 	
 	@Override public void add(CuVvc v, CuTypeScheme ts) {
 		this.v.add(v);
 		this.ts.add(ts);
 		super.text += String.format(" , %s %s", v.toString(), ts.toString());
 	}
 }
