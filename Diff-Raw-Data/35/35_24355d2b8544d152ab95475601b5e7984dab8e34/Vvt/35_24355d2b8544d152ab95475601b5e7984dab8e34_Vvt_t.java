 
 public class Vvt extends CuVvt{
 	String text="";
 	String data_s;
 	CuType data_t;
 	public Vvt(String s,CuType t){
 		data_s=s;
 		data_t=t;
 		text=s+" : "+t.toString();
 	}
 }
