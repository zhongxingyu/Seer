 package gui;
 
 import persistence.Persistence;
 import core.Categoria;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.container.MainScreen;
 
 public class ListadoCategoriasScreen extends MainScreen {
 	
 	private Categoria _selected;
 	private ListadoCategoriasLista _lista;
 
 	public ListadoCategoriasScreen() {
 		super(MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR);
 		setTitle("Listado de categoras");
 		
 		_lista = new ListadoCategoriasLista() {
 			protected boolean navigationClick(int status, int time) {
 				if (String.class.isInstance(get(_lista, getSelectedIndex()))) {
 					NuevaCategoria n = new NuevaCategoria();
 					UiApplication.getUiApplication().pushModalScreen(
 							n.getScreen());
 					try {
 						addCategoria(n.getCategoria());
 					} catch (Exception e) {
 						return true;
 					}
 					return true;
 				} else {
 					_selected = (Categoria)get(_lista, getSelectedIndex());
 					UiApplication.getUiApplication().popScreen(getScreen());
 					return true;
 				}
 			}
 		};
 		
 		_lista.insert(0, "Nueva categora");
 		add(_lista);
 		addMenuItem(menuVer);
 		addMenuItem(menuDelete);
 	}
 	
 	private final MenuItem menuVer = new MenuItem("Ver", 0, 0) {
 
 		public void run() {
 			int index = _lista.getSelectedIndex();
 			VerCategoria verCategoria = new VerCategoria(
 					(Categoria) _lista.get(_lista, index));
 			UiApplication.getUiApplication().pushModalScreen(
 					verCategoria.getScreen());
 			try {
 				verCategoria.actualizarCategoria();
				_lista.delete(index);
				_lista.insert(index, verCategoria.getCategoria());
				_lista.setSelectedIndex(index);
			} catch(NullPointerException e) {
				
 			} catch (Exception e) {
 				_lista.delete(index);
 			}
 		}
 	};
 	
 	private final MenuItem menuDelete = new MenuItem("Eliminar", 0, 0) {
 
 		public void run() {
 			Persistence persistence = null;
 			try {
 				persistence = new Persistence();
 			} catch (Exception e) {
 				Dialog.alert(e.toString());
 			}
 			int index = _lista.getSelectedIndex();
 			try {
 				persistence.borrarCategoria((Categoria) _lista.get(_lista, index));
 			} catch (Exception e) {
 				Dialog.alert(e.toString());
 			}
 
 			_lista.delete(index);
 		}
 	};
 	
 	public void addCategoria(Categoria categoria) {
 		_lista.insert(_lista.getSize(), categoria);
 	}
 
 	public Categoria getSelected() {
 		return _selected;
 	}
 
 	public boolean onClose() {
 		UiApplication.getUiApplication().popScreen(getScreen());
 		return true;
 	}
 }
