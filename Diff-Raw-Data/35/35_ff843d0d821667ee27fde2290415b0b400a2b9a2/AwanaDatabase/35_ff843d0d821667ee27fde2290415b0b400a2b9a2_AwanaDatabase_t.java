 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package awana.database;
 
 /**
  *
  * @author Justin VanDeBrake
  */
 public class AwanaDatabase {
 
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String args[]) {
 		/* Set the Nimbus look and feel */
 		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
 		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
 		 */
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(DirectoryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 		}
 		//</editor-fold>
		backupDatabase();
 
		DatabaseWrapper w = new DatabaseWrapper();
 		Record.loadMasterData(); //do not remove temporary record load fix will be replaced with dynamic loading once variable yml field loading is supproted
 
 		/* Create and display the form */
 		DirectoryPage page;
				page = new DirectoryPage(w);
 				page.setVisible(true);
 		Thread t = new Thread(page.shutdown);
 		Runtime.getRuntime().addShutdownHook(t);
 	}

	private static void backupDatabase() {
	}
 }
