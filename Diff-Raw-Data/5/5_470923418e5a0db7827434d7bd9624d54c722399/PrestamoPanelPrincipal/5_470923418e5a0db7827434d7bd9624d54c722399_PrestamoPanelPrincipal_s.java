 package code.google.com.opengis.gestionVISUAL;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import code.google.com.opengis.gestion.Prestamo;
 import code.google.com.opengis.gestionDAO.ConectarDBA;
 import code.google.com.opengis.gestionDAO.Idioma;
 
 public class PrestamoPanelPrincipal extends GeneradorPanelPrincipal{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	static Object[] nombreColumna = {Idioma.getString("etLoanId"),Idioma.getString("etDeviceId"),Idioma.getString("etIdCard"),Idioma.getString("etDateRent"),Idioma.getString("etDateReturn")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 	public String auxdisp;
 	
 	
 	public PrestamoPanelPrincipal() {
 		super(false);
 		super.getBEliminar().setIcon(new ImageIcon("OpenGis/src/recursosVisuales/devolver.png"));
 		super.getBEliminar().setToolTipText(Idioma.getString("etReturn"));
 	}
 	
 	public void buscar(){
 		
 		
 		String texto = getTxtCriterioBusqueda().getText();
 		
 		try {
 			
 			modelo.setColumnCount(0);
 			modelo.setRowCount(0);
 			
 			ResultSet rs = ConectarDBA.buscar("SELECT * FROM `prestamo` WHERE id_prestamo LIKE '%"+texto+"%' OR iddispositivo LIKE '%"+texto+"%' OR dni_usuario LIKE '%"+texto+"%' OR fecha_alquiler LIKE '%"+texto+"%' OR fecha_devol LIKE '%"+texto+"%'");
 			int nColumnas = rs.getMetaData().getColumnCount();
 			modelo.setColumnIdentifiers(nombreColumna);
 			
 			while (rs.next()) {
 				
 				Object[] registro = new Object[nColumnas];
 
 				for (int i = 0; i < nColumnas; i++) {
 					registro[i] = rs.getObject(i + 1); // Guardamos todos los registros
 					
 				}
 				
 				modelo.addRow(registro); // Aadimos el registro a la tabla
 
 			}
 			rs.close();
 		} catch (SQLException e1) {
 			System.out.println(e1);
 
 		}
 		
 	}
 
 	
 	public void nuevo(){
 		
 	    PrestamoPanelGestion p = new PrestamoPanelGestion("alta"); // Creamos el panel de Alta de Prestamos //$NON-NLS-1$
 		
 		VentanaPrincipal.aadirPestaaNueva(Idioma.getString("etNewLoan"),p); // Aadimos el panel a la pestaa //$NON-NLS-1$
 		
 		
 	}
 	
 	
 	public void modificar(){
 		
 		int fila = getTablaPrincipal().getSelectedRow();
 		if (fila != -1) {
 			String[] rPrestamo = new String[5];
 			for (int i = 0; i < rPrestamo.length; i++) {
 				rPrestamo[i] = getTablaPrincipal().getValueAt(fila, i)
 						.toString();
 			}
 			PrestamoPanelGestion p = new PrestamoPanelGestion("modificar",rPrestamo[0].toString(),rPrestamo[1].toString(),rPrestamo[2].toString(), rPrestamo[1].toString()); // Creamos el panel de Modificacin de Prstamo //$NON-NLS-1$
 			
 			VentanaPrincipal.aadirPestaaNueva(Idioma.getString("etModifyLoan")+"("+rPrestamo[1].toString()+")",p); // Aadimos el panel a la pestaa //$NON-NLS-1$
 		}
 		
 	}
 	
 	public void eliminar(){
 		String[] rPrestamo = new String[5];
 		int fila = getTablaPrincipal().getSelectedRow();
 		if (fila != -1) {
 			for (int i = 0; i < rPrestamo.length; i++) {
 				rPrestamo[i] = getTablaPrincipal().getValueAt(fila, i)
 						.toString();
 				}
 			}
		int confirmacion = JOptionPane.showConfirmDialog(null, "Quieres cerrar este prstamo?");
 		if (confirmacion == JOptionPane.OK_OPTION){
			System.out.println("Voy a cerrar el prstamo.");
 			Prestamo.cerrarPrestamo(rPrestamo[1], rPrestamo[2]);
 			buscar();
 			super.getBEliminar().setEnabled(false);
 			super.getBModificar().setEnabled(false);
 		}
 		else{
 			System.out.println("Vale, no hago nada.");
 		}
 	}
 	
 }
