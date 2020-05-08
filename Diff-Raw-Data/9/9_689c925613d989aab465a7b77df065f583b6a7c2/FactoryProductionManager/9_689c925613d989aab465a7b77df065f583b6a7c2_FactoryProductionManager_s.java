 //Ben Mayeux and Stephanie Reagle
 //CS 200
 package factory.managers;
 
 import javax.swing.JPanel;
 
 import factory.client.Client;
 import factory.swing.FactoryProdManPanel;
 
 public class FactoryProductionManager extends Client {
 	static final long serialVersionUID = -2074747328301562732L;
 
 		public FactoryProductionManager(JPanel buttons) {
 			super(Client.Type.FACTORYPRODUCTIONMANAGER, buttons, null);
 			setInterface();
 		}
 		public static void main(String[] args){
 		    FactoryProdManPanel buttons = new FactoryProdManPanel();
 			FactoryProductionManager f = new FactoryProductionManager(buttons);
 		}
 
 		@Override
 		public void setInterface() {
 			this.setSize(800, 800);
 			this.add(UI);
 			this.setVisible(true);
 			
 		}
 
 		public void sendOrder(String kitName,String quantity) {
 	
 //output.println("kmg fpm sendorder" + kitnameBox.getSelectedItem() + spinner.getValue());
 					
 	}
 		
		public void populateKitList(ArrayList<String> kitList) {
 			for (int i = 0; i < kitList.size();i++) {
 				buttons.add(
 			}
 		}
 		*/
 }
