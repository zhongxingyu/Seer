 package demo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.pushState.PopupStateEvent;
 import org.zkoss.pushState.PushState;
 
 public class ViewModel {
 	private String filter1 = null;
 	private String filter2 = null;
 	private String filter3 = null;
 	private List<Food> gridModel = FoodData.getAllFoods();
 	
 	public List<Food> getGridModel(){
 		return gridModel;
 	}
 	
 	public String getFilter1() {
 		return filter1;
 	}
 
 	public String getFilter2() {
 		return filter2;
 	}
 
 	public String getFilter3() {
 		return filter3;
 	}
 	
 	public String getCategory(){
 		return gridModel.size() == 0 ? 
 			"" : "A Total of " + gridModel.size() + " Food Items";
 	}
 	
 	public String getMsg(){
 		return gridModel.size() == 0 ?
 			"Nothing found..." : "";
 	}
 	
 	@Command
 	@NotifyChange("*")
 	public void filter(@BindingParam("f1") String f1, @BindingParam("f2") String f2, @BindingParam("f3") String f3){
		doFilter(f1, f2, f3);
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("f1", f1);
 		map.put("f2", f2);
 		map.put("f3", f3);
 		PushState.push(map, "Search results", "?q="+f1+f2+f3);
 	}
 
 	@Command
 	@NotifyChange("*")
 	public void popupState(@BindingParam("event") PopupStateEvent event){
 		Map<String, ?> state = event.getState();
		doFilter(
 			state.get("f1").toString(), 
 			state.get("f2").toString(), 
 			state.get("f3").toString()
 		);
 	}
 
	private void doFilter(String f1, String f2, String f3) {
 		filter1 = f1;
 		filter2 = f2;
 		filter3 = f3;
 		gridModel = FoodData.getFilterFoods(f1, f2, f3);
 	}
 }
