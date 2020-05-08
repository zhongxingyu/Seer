 public class Structure {
 	
 	private static ScholarshipModel model;
 	private static SelectionView view = new SelectionView();
	private static ScholarPubController controller;
 	
 	public static void main(String args[]) {
 		model = new ScholarshipModel();
 		view.setModel(model);
		controller = new ScholarPubController(model, view);
 	}
 
 }
