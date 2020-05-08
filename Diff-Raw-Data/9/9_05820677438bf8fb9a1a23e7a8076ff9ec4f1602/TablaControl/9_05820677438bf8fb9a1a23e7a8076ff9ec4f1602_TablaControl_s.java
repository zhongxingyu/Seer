 package mereditor.control;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.draw2d.Connection;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 
 import mereditor.interfaz.swt.editores.EditorFactory;
 import mereditor.interfaz.swt.figuras.TablaFigure;
 import mereditor.interfaz.swt.figuras.Figura;
 import mereditor.representacion.PList;
 import mreleditor.modelo.Tabla;
 import mreleditor.modelo.Tabla.ClaveForanea;
 
 public class TablaControl extends Tabla implements Control<Tabla>,
 		MouseListener {
 	protected Map<String, TablaFigure> figures = new HashMap<>();
 
 	@Override
 	public Figura<Tabla> getFigura(String idDiagrama) {
 		if (!this.figures.containsKey(idDiagrama)) {
 			TablaFigure figura = new TablaFigure(this);
 			this.figures.put(idDiagrama, figura);
 
 			this.setPosicionInicial(figura);
 		}
 
 		this.figures.get(idDiagrama).actualizar();
 
 		return this.figures.get(idDiagrama);
 	}
 
 	public TablaControl(Tabla tab) {
 		super(tab);
 	}
 	public TablaControl(String nombre) {
 		super(new Tabla(nombre));
 	}
 	public TablaControl() {
 	}
 	
 	public TablaControl(Tabla tab, String idDiagrama) {
 		super(tab);
 	}
 
 
 	/**
 	 * Establecer la posición cuando es una nueva figura.
 	 * 
 	 * @param figura
 	 */
 	private void setPosicionInicial(Figure figura) {
 		figura.setBounds(figura.getBounds().getTranslated(100, 100));
 	}
 	
 	public void addFigure(Figure figura, String idDiagrama) {
 		if (!this.figures.containsKey(idDiagrama)) {
 			this.figures.put(idDiagrama, (TablaFigure)figura);
 		}
 	}
 
 	@Override
 	public void dibujar(Figure contenedor, String idDiagrama) {
 		TablaFigure figuraTabla = (TablaFigure) this.getFigura(idDiagrama);
 		contenedor.add(figuraTabla);
 		//this.figures.put(idDiagrama, figuraTabla);
 	}
 	
 	/**
 	 * Conectar tabla con tablas referenciadas por claves foraneas
 	 */
 	public void dibujarRelaciones(Figure contenedor, String idDiagrama, DiagramaLogicoControl derC) {
 		TablaFigure figuraTabla = (TablaFigure) this.getFigura(idDiagrama);
 		
 		if( this.getClavesForaneas().size() > 0 ) {
 			Iterator<ClaveForanea> itClaves = this.getClavesForaneas().iterator();
 			while( itClaves.hasNext() ) {
 				ClaveForanea claveFK = itClaves.next();
 				
 				String tablaReferenciada = claveFK.getTablaReferenciada();			
				TablaControl tablaRef = (TablaControl)derC.getTablaByName(tablaReferenciada);	
				TablaFigure figuraTablaRef = (TablaFigure) tablaRef.getFigura(idDiagrama);
				
				figuraTabla.conectarTabla(figuraTablaRef);
 			}
 		}
 	}
 
 	public Map<String, TablaFigure> getFiguras() {
 		return this.figures;
 	}
 
 	@Override
 	public String getNombreIcono() {
 		return "table.png";
 	}
 
 	@Override
 	public void mouseDoubleClicked(MouseEvent arg0) {
 		// EditorFactory.getEditor(this).open();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
