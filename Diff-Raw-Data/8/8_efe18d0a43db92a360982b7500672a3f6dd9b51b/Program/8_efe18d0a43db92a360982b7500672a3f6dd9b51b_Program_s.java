 import defs.EdgeFormat;
 import defs.FormatHelper;
 import defs.VertexFormat;
 import logic.extlib.Edge;
 import logic.extlib.IncidenceListGraph;
 import logic.extlib.Vertex;
 import ui.MainGUI;
 
 public class Program {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		/* Set the Nimbus look and feel */
 		// <editor-fold defaultstate="collapsed"
 		// desc=" Look and feel setting code (optional) ">
 		/*
 		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
 		 * default look and feel. For details see
 		 * http://download.oracle.com/javase
 		 * /tutorial/uiswing/lookandfeel/plaf.html
 		 */
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
 					.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (ClassNotFoundException ex) {
 			java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (InstantiationException ex) {
 			java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (IllegalAccessException ex) {
 			java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		}
 		// </editor-fold>
 		final IncidenceListGraph<String, String> gr = new IncidenceListGraph<String, String>(
 				true);
 		// Create a graph
 		Vertex<String> prevV = null;
 		for (int i = 0; i < 10; i++) {
 			// Vertices
 			// Vertex Source
 			Vertex<String> vi = null;
 			if (null == prevV) {
 				// Vertex i
 				vi = gr.insertVertex("V " + i);
 				// Format Vertex i
 				VertexFormat vFi = new VertexFormat();
				vFi.setLabel("V " + i);
 				vi.set(FormatHelper.FORMAT, vFi);
 			} else {
 				vi = prevV;
 			}
 			// Vertex Target
 			Vertex<String> vi1 = gr.insertVertex("V " + i + 1);
 			VertexFormat vFi1 = new VertexFormat();
			vFi1.setLabel("V " + i + 1);
 			vi1.set(FormatHelper.FORMAT, vFi1);
 			prevV = vi1;
 
 			// Edge VSource => VTarget
 			if (i % 2 == 0) {
 				Edge<String> ei1 = gr.insertEdge(vi, vi1, "E " + i + 1);
 				EdgeFormat eF1 = new EdgeFormat();
 				eF1.setIsDirected(true);
				eF1.setLabel("E " + i + 1);
				eF1.setTextVisible(true);
 				ei1.set(FormatHelper.FORMAT, eF1);
 			} else {
 				Edge<String> eBC = gr.insertEdge(vi, vi1, "E " + i + 1);
 				EdgeFormat eF2 = new EdgeFormat();
				eF2.setTextVisible(true);
 				eBC.set(FormatHelper.FORMAT, eF2);
 			}
 		}
 
 		/* Create and display the form */
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			public void run() {
 
 				new MainGUI<String, String>(gr).setVisible(true);
 			}
 		});
 	}
 }
