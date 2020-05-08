 
 public class WhileStat extends CuStat{
 	private CuExpr e;
 	private CuStat s1;
 	public WhileStat (CuExpr ex, CuStat st){
 		e = ex;
 		s1 = st;
		text = "while ( " + e.toString() + " ) " + s1.toString();
 	}
 }
