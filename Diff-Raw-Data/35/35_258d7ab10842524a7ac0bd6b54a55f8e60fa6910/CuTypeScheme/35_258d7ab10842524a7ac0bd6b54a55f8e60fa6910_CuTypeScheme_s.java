 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 
 public abstract class CuTypeScheme {
 	protected String text = "";
 	List<String>        data_kc;
 	Map<String, CuType>  data_tc;
 	CuType              data_t;
 	@Override public String toString() {
 		return text;
 	}
 	public void calculateType(CuContext context) throws NoSuchTypeException {}
 	public boolean sameAs (CuTypeScheme that, CuContext context) {
 		return false;}
 }
 
 class TypeScheme extends CuTypeScheme {
 	public TypeScheme(List<String> kc, Map<String, CuType> tc , CuType t){
 		super.data_kc=kc;
 		super.data_tc=tc;
 		super.data_t=t;
 		super.text=Helper.printList("<", data_kc, ">", ",")+" "+Helper.printMap("(", data_tc, ")", ",")+" : "+t.toString();
 	}
 	
 	@Override public void calculateType(CuContext context) throws NoSuchTypeException {
 		CuContext temp_context = new CuContext(context);
 		temp_context.updateKc(super.data_kc);
 		for (String key : super.data_tc.keySet()) {
 			CuType value = super.data_tc.get(key);
 			value.calculateType(temp_context);
 		}
 		super.data_t.calculateType(temp_context);
 	}
 	
 	public boolean sameAs (CuTypeScheme that, CuContext context) {
 		if (super.data_tc.size() != that.data_tc.size()) {
 			return false;
 		}
 		CuContext original_context = new CuContext(context);
 		context.mKind.addAll(data_kc);
 		//tc is a LinkedHashMap
 		ArrayList<CuType> this_types = (ArrayList<CuType>) super.data_tc.values();
 		ArrayList<CuType> that_types = (ArrayList<CuType>) super.data_tc.values();
 		for (int i=0; i<this_types.size(); i++) {
 			if (!this_types.get(i).equals(that_types.get(i))) {
 				//restore original context
 				context = original_context;
 				return false;
 			}
 		}
 		if (!super.data_t.equals(that.data_t)) {
 			//restore original context
 			context = original_context;
 			return false;
 		}
 		//restore original context
 		context = original_context;
 		return true;
 	}
 }
