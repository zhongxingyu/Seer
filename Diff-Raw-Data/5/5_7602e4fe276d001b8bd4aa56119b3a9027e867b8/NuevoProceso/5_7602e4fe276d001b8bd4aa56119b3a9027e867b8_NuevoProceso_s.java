 package gui.Nuevos;
 
 import gui.Util;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import persistence.Persistence;
 import core.Actuacion;
 import core.CampoPersonalizado;
 import core.Categoria;
 import core.Juzgado;
 import core.Persona;
 import core.Plantilla;
 import core.Proceso;
 
 public class NuevoProceso {
 
 	private Proceso _proceso;
 	private NuevoProcesoScreen _screen;
 
 	private Persona _demandante;
 	private Persona _demandado;
 	private Juzgado _juzgado;
 	private Vector _actuaciones;
 	private Vector _campos;
 	private Vector _categorias;
 
 	/**
 	 * Se crea un NuevoProceso y le asocia una pantalla, con este objeto se
 	 * crearan nuevos Procesos
 	 */
 	public NuevoProceso() {
 		_campos = new Vector();
 		_actuaciones = new Vector();
 		_screen = new NuevoProcesoScreen();
 		try {
 			Persistence p = new Persistence();
 
 			_screen.setDemandante(p.consultarPersona("1", 1).getNombre());
 			_screen.setDemandado(p.consultarPersona("1", 2).getNombre());
 			_screen.setJuzgado(p.consultarJuzgado("1").getNombre());
 
 			_categorias = p.consultarCategorias();
 			Categoria ninguna = p.consultarCategoria("1");
 			_categorias.removeElement(ninguna);
 			_categorias.insertElementAt(ninguna, 0);
 
 		} catch (NullPointerException e) {
 			Util.noSd();
 		} catch (Exception e) {
 			Util.alert(e.toString());
 		}
 
 		Object[] categorias = new Object[_categorias.size()];
 		_categorias.copyInto(categorias);
 		_screen.addCategorias(categorias);
 		_screen.setChangeListener(listener);
 	}
 
 	public NuevoProceso(Plantilla plantilla) {
 		this();
 		_demandante = plantilla.getDemandante();
 		_demandado = plantilla.getDemandado();
 		_juzgado = plantilla.getJuzgado();
 		_campos = plantilla.getCampos();
 		_screen.setDemandante(_demandante.getNombre());
 		_screen.setDemandado(_demandado.getNombre());
 		_screen.setJuzgado(_juzgado.getNombre());
 		Enumeration e = _campos.elements();
 		while (e.hasMoreElements()) {
 			CampoPersonalizado campo = (CampoPersonalizado) e.nextElement();
 			_screen.addCampo(campo, campo.getNombre(), campo.getValor());
 		}
 		_screen.setRadicado(plantilla.getRadicado());
 		_screen.setRadicadoUnico(plantilla.getRadicadoUnico());
 		_screen.setEstado(plantilla.getEstado());
 		_screen.setCategoria(plantilla.getCategoria());
 		_screen.setTipo(plantilla.getTipo());
 		_screen.setNotas(plantilla.getNotas());
 		_screen.setPrioridad(plantilla.getPrioridad());
 	}
 
 	FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (context == Util.ADD_DEMANDANTE) {
 				addDemandante();
 			} else if (context == Util.ADD_DEMANDADO) {
 				addDemandado();
 			} else if (context == Util.ADD_JUZGADO) {
 				addJuzgado();
 			} else if (context == Util.NEW_ACTUACION) {
 				newActuacion();
 			} else if (context == Util.NEW_CATEGORIA) {
 				newCategoria();
 			} else if (context == Util.ADD_CATEGORIA) {
 				addCategoria();
 			} else if (context == Util.GUARDAR) {
 				guardarProceso();
 			} else if (context == Util.ADD_CAMPO) {
 				addCampo();
 			} else if (context == Util.ELIMINAR_CAMPO) {
 				eliminarCampo();
 			} else if (context == Util.CERRAR) {
 				cerrarPantalla();
 			}
 		}
 	};
 
 	public Proceso getProceso() {
 		return _proceso;
 	}
 
 	/**
 	 * @return La pantalla asociada al objeto
 	 */
 	public NuevoProcesoScreen getScreen() {
 		return _screen;
 	}
 
 	/**
 	 * Se crea el nuevo Proceso, basado en los datos capturados por la pantalla
 	 * y se guarda en la base de datos
 	 */
 	private void guardarProceso() {
 		getValoresCampos();
 		if (_demandante == null) {
 			_screen.alert("El Demandante es obligatorio");
 		} else if (_demandado == null) {
 			_screen.alert("El Demandado es obligatorio");
 		} else if (_juzgado == null) {
 			_screen.alert("El juzgado es obligatorio");
 		} else if (isCampoObligatorio()) {
 		} else if (checkLonMin()) {
 		} else {
 			_proceso = new Proceso(_demandante, _demandado, _screen.getFecha(),
 					_juzgado, _screen.getRadicado(),
 					_screen.getRadicadoUnico(), _actuaciones,
 					_screen.getEstado(), (Categoria) _screen.getCategoria(),
 					_screen.getTipo(), _screen.getNotas(), _campos,
 					_screen.getPrioridad());
 			try {
 				new Persistence().guardarProceso(_proceso);
 			} catch (NullPointerException e) {
 				_screen.alert(Util.noSDString());
 				System.exit(0);
 			} catch (Exception e) {
 				_screen.alert(e.toString());
 			}
 			Util.popScreen(_screen);
 		}
 	}
 
 	private boolean checkLonMin() {
 		boolean ret = false;
 		Enumeration e = _campos.elements();
 		while (e.hasMoreElements()) {
 			CampoPersonalizado campo = (CampoPersonalizado) e.nextElement();
 			if (campo.getLongitudMin() > campo.getValor().length()
 					&& campo.getLongitudMin() != 0) {
 				Util.alert("El campo " + campo.getNombre()
 						+ " posee una longitud minima de "
 						+ campo.getLongitudMin()
 						+ " caracteres, y usted ingres "
 						+ campo.getValor().length());
 				ret = true;
 			}
 			if (campo.getLongitudMax() < campo.getValor().length()
 					&& campo.getLongitudMax() != 0) {
 				Util.alert("El campo " + campo.getNombre()
 						+ " posee una longitud mxima de "
 						+ campo.getLongitudMax()
 						+ " caracteres, y usted ingres "
 						+ campo.getValor().length());
 				ret = true;
 			}
 		}
 		return ret;
 	}
 
 	private boolean isCampoObligatorio() {
 		boolean ret = false;
 		Enumeration e = _campos.elements();
 		while (e.hasMoreElements()) {
 			CampoPersonalizado campo = (CampoPersonalizado) e.nextElement();
 			if (campo.isObligatorio().booleanValue()
 					&& campo.getValor().length() == 0) {
 				_screen.alert("El campo " + campo.getNombre()
 						+ " es obligatorio");
 				ret = true;
 				break;
 			}
 		}
 		return ret;
 	}
 
 	private void getValoresCampos() {
 		Object[][] campos = _screen.getCampos();
 
 		for (int i = 0; i < campos[0].length; i++) {
 			CampoPersonalizado campo = (CampoPersonalizado) campos[0][i];
 			String valor = (String) campos[1][i];
 			((CampoPersonalizado) _campos.elementAt(_campos.indexOf(campo)))
 					.setValor(valor);
 		}
 	}
 
 	private void addDemandante() {
 		Persona demandante = Util.listadoPersonas(1, true, 0);
 		if (demandante != null) {
 			_demandante = demandante;
 			_screen.setDemandante(_demandante.getNombre());
 		}
 	}
 
 	private void addDemandado() {
 		Persona demandado = Util.listadoPersonas(2, true, 0);
 		if (demandado != null) {
 			_demandado = demandado;
 			_screen.setDemandado(_demandado.getNombre());
 		}
 	}
 
 	private void addJuzgado() {
 		Juzgado juzgado = Util.listadoJuzgados(true, 0);
 		if (juzgado != null) {
 			_juzgado = juzgado;
 			_screen.setJuzgado(_juzgado.getNombre());
 		}
 	}
 
 	private void newActuacion() {
 		Actuacion actuacion = Util.nuevaActuacion();
 		if (actuacion != null) {
 			_actuaciones.addElement(actuacion);
 			Object[] actuaciones = new Object[_actuaciones.size()];
 			_actuaciones.copyInto(actuaciones);
 			_screen.addActuaciones(actuaciones);
 			_screen.selectActuacion(_actuaciones.indexOf(actuacion));
 		}
 	}
 
 	private void addCategoria() {
 		Categoria categoria = Util.listadoCategorias(true, 0);
 		if (categoria != null) {
 			_categorias.addElement(categoria);
 			Object[] categorias = new Object[_categorias.size()];
 			_categorias.copyInto(categorias);
 			_screen.addCategorias(categorias);
 			_screen.selectCategoria(_categorias.indexOf(categoria));
 		}
 	}
 
 	private void addCampo() {
 		CampoPersonalizado campo = Util.listadoCampos(true, 0);
 		if (campo != null) {
 			_campos.addElement(campo);
 			_screen.addCampo(campo, campo.getNombre());
 		}
 	}
 
 	private void eliminarCampo() {
 
 	}
 
 	private void newCategoria() {
 		Categoria categoria = Util.nuevaCategoria(true);
 		if (categoria != null) {
 			_categorias.addElement(categoria);
 			Object[] categorias = new Object[_categorias.size()];
 			_categorias.copyInto(categorias);
 			_screen.addCategorias(categorias);
 			_screen.selectCategoria(_categorias.indexOf(categoria));
 		}
 	}
 
 	private void cerrarPantalla() {
 		if (_actuaciones.size() != 0 || _campos.size() != 0
 				|| _demandante != null || _demandado != null
 				|| _juzgado != null) {
 			Object[] ask = { "Guardar", "Descartar", "Cancelar" };
 			int sel = _screen.ask(ask, "Se han detectado cambios", 2);
			if (sel == 1) {
 				guardarProceso();
			} else if (sel == 2) {
 				Util.popScreen(_screen);
 			}
 		} else {
 			Util.popScreen(_screen);
 		}
 	}
 }
