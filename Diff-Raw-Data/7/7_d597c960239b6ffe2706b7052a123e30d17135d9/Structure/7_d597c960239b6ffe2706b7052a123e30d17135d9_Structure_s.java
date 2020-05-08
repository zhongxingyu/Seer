 public class Structure {
 	
 	private static ScholarshipModel model;
 	private static SelectionView view = new SelectionView();
	private ScholarPubController controller;
 	
 	public static void main(String args[]) {
		 ScholarPubController controller = new ScholarPubController(model, view);
 		model = new ScholarshipModel();
 		view.setModel(model);
		view.setModel(model);
 	}
 
 }
