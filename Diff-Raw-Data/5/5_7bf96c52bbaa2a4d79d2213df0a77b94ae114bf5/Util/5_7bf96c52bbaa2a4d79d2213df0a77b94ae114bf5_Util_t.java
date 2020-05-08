 package gui;
 
 import gui.Listados.ListadoCampos;
 import gui.Listados.ListadoCategorias;
 import gui.Listados.ListadoJuzgados;
 import gui.Listados.ListadoPersonas;
 import gui.Listados.ListadoPlantillas;
 import gui.Listados.ListadoProcesos;
 import gui.Nuevos.NuevaActuacion;
 import gui.Nuevos.NuevaCategoria;
 import gui.Nuevos.NuevaPersona;
 import gui.Nuevos.NuevaPlantilla;
 import gui.Nuevos.NuevoCampo;
 import gui.Nuevos.NuevoJuzgado;
 import gui.Nuevos.NuevoProceso;
 import gui.Ver.VerActuacion;
 import gui.Ver.VerCampo;
 import gui.Ver.VerCategoria;
 import gui.Ver.VerJuzgado;
 import gui.Ver.VerPersona;
 import gui.Ver.VerPlantilla;
 import gui.Ver.VerProceso;
 
 import java.util.Calendar;
 import java.util.Vector;
 
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.UiEngine;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.container.PopupScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import persistence.Persistence;
 import core.Actuacion;
 import core.CampoPersonalizado;
 import core.Categoria;
 import core.Juzgado;
 import core.Persona;
 import core.Plantilla;
 import core.Proceso;
 
 public class Util {
 
 	public static final short ADD_DEMANDANTE = 1;
 	public static final short ADD_DEMANDADO = 2;
 	public static final short ADD_JUZGADO = 3;
 	public static final short ADD_CATEGORIA = 4;
 	public static final short ADD_CITA = 5;
 	public static final short ADD_CAMPO = 6;
 
 	public static final short VER_DEMANDANTE = 7;
 	public static final short VER_DEMANDADO = 8;
 	public static final short VER_JUZGADO = 9;
 	public static final short VER_ELEMENTO = 10;
 	public static final short VER_CITA = 11;
 	public static final short VER_CATEGORIA = 12;
 	public static final short VER_ACTUACION = 13;
 	public static final short VER_LISTADO_ACTUACIONES = 14;
 	public static final short VER_CAMPO = 15;
 
 	public static final short NEW_ACTUACION = 16;
 	public static final short NEW_CATEGORIA = 17;
 
 	public static final short ELIMINAR_DEMANDANTE = 18;
 	public static final short ELIMINAR_DEMANDADO = 19;
 	public static final short ELIMINAR_JUZGADO = 20;
 	public static final short ELIMINAR_CITA = 21;
 	public static final short ELIMINAR_CAMPO = 22;
 
 	public static final short GUARDAR = 23;
 	public static final short CERRAR = 24;
 	public static final short ELIMINAR = 25;
 
 	public static final short CLICK = 26;
 	public static final int LLAMAR = 27;
 
 	public static final short NEW_PROCESO = 28;
 
 	public static final short COPIA_SEGURIDAD = 29;
 	public static final short RESTAURAR_PREFERENCIAS = 30;
 
 	public static final short ROOT_SELECCIONADO = 31;
 
 	public static final short NEW = 32;
 
 	public static final short LLAVES = 33;
 
 	public static PopupScreen WAIT_SCREEN;
 
 	public static UiApplication UI_Application;
 
 	public static void popScreen(Screen screen) {
 		if (UI_Application == null) {
 			UI_Application = UiApplication.getUiApplication();
 		}
 		UI_Application.popScreen(screen);
 	}
 
 	public static void pushWaitScreen() {
 		if (WAIT_SCREEN == null) {
 			WAIT_SCREEN = new PopupScreen(new VerticalFieldManager());
 			WAIT_SCREEN.add(new LabelField("Procesando, espere por favor..."));
 		}
 		UiApplication.getUiApplication().pushGlobalScreen(WAIT_SCREEN, 0,
 				UiEngine.GLOBAL_SHOW_LOWER);
 	}
 
 	public static void popWaitScreen() {
 		if (!WAIT_SCREEN.isDisplayed()) {
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				alert(e.toString());
 			}
 		}
 		popScreen(WAIT_SCREEN);
 	}
 
 	public static String noSDString() {
 		return ("Tarjeta SD no presente, la aplicacin se cerrar, verifique e inicie nuevamente");
 	}
 
 	public static void citaErrorMessage() {
 		alert("No se pudo agregar la cita al calendario, intntelo nuevamente");
 	}
 
 	public static void noSd() {
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				Dialog.alert(noSDString());
 				System.exit(0);
 			}
 		});
 	}
 
 	public static void alert(final String alert) {
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				Dialog.alert(alert);
 			}
 		});
 	}
 
 	public static String delBDJuzgado() {
 		return "Desea eliminar el juzgado?. Se eliminar definitivamente y de cada proceso que lo contenga";
 	}
 
 	public static String delBDPersona() {
 		return "Desea eliminar la persona?. Se eliminar definitivamente y de cada proceso que la contenga";
 	}
 
 	public static String delBDCategoria() {
 		return "Desea eliminar la categora?. Se eliminar de definitivamente y cada proceso ligado a sta";
 	}
 
 	public static String delBDCampo() {
 		return "Desea eliminar el campo personalizado?. Se eliminar definitivamente y de cada proceso que lo contenga";
 	}
 
 	public static String delBDActuacion() {
 		return "Desea eliminar la actuacin?. Se eliminar definitivamente";
 	}
 
 	public static String delBDProceso() {
 		return "Desea eliminar el proceso?. Se eliminar definitivamente, incluyendo sus actuaciones.";
 	}
 
 	public static String delBDPlantilla() {
 		return "Desea eliminar la plantilla?. Se eliminar definitivamente";
 	}
 
 	public static String delJuzgado() {
 		return "Desea quitar el juzgado?";
 	}
 
 	public static String delPersona() {
 		return "Desea quitar la persona?";
 	}
 
 	public static String delCampo() {
 		return "Desea quitar el campo personalizado?";
 	}
 
 	public static Persona consultarPersonaVacia(int tipo) {
 		Persona persona = null;
 		try {
 			persona = new Persistence().consultarPersona("1", tipo);
 		} catch (NullPointerException e) {
 			noSd();
 		} catch (Exception e) {
 			alert(e.toString());
 		}
 		return persona;
 	}
 
 	public static Juzgado consultarJuzgadoVacio() {
 		Juzgado juzgado = null;
 		try {
 			juzgado = new Persistence().consultarJuzgado("1");
 		} catch (NullPointerException e) {
 			noSd();
 		} catch (Exception e) {
 			alert(e.toString());
 		}
 		return juzgado;
 	}
 
 	public static Vector consultarCategorias() {
 		Vector v = null;
 		try {
 			v = new Persistence().consultarCategorias();
 		} catch (NullPointerException e) {
 			noSd();
 		} catch (Exception e) {
 			alert(e.toString());
 		}
 		return v;
 	}
 
 	public static Persona verPersona(Persona persona) {
 		VerPersona v = new VerPersona(persona);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getPersona();
 	}
 
 	public static Juzgado verJuzgado(Juzgado juzgado) {
 		VerJuzgado v = new VerJuzgado(juzgado);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getJuzgado();
 	}
 
 	public static Categoria verCategoria(Categoria categoria) {
 		VerCategoria v = new VerCategoria(categoria);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getCategoria();
 	}
 
 	public static CampoPersonalizado verCampo(CampoPersonalizado campo) {
 		VerCampo v = new VerCampo(campo);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getCampo();
 	}
 
 	public static Actuacion verActuacion(Actuacion actuacion) {
 		VerActuacion v = new VerActuacion(actuacion);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getActuacion();
 	}
 
 	public static Proceso verProceso(Proceso proceso) {
 		VerProceso v = new VerProceso(proceso);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getProceso();
 	}
 
 	public static Plantilla verPlantilla(Plantilla plantilla) {
 		VerPlantilla v = new VerPlantilla(plantilla);
 		UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 		return v.getPlantilla();
 	}
 
 	public static Persona listadoPersonas(int tipo, boolean popup, int style) {
 		ListadoPersonas l = new ListadoPersonas(tipo, popup, style);
 		UiApplication.getUiApplication().pushModalScreen(l.getScreen());
 		return l.getSelected();
 	}
 
 	public static Juzgado listadoJuzgados(boolean popup, int style) {
 		ListadoJuzgados l = new ListadoJuzgados(popup, style);
 		UiApplication.getUiApplication().pushModalScreen(l.getScreen());
 		return l.getSelected();
 	}
 
 	public static Categoria listadoCategorias(boolean popup, int style) {
 		ListadoCategorias l = new ListadoCategorias(popup, style);
 		UiApplication.getUiApplication().pushModalScreen(l.getScreen());
 		return l.getSelected();
 	}
 
 	public static CampoPersonalizado listadoCampos(boolean popup, int style) {
 		ListadoCampos l = new ListadoCampos(popup, style);
 		UiApplication.getUiApplication().pushModalScreen(l.getScreen());
 		return l.getSelected();
 	}
 
 	public static Proceso listadoProcesos(boolean popup, int style) {
 		ListadoProcesos l = new ListadoProcesos(popup, style);
 		UiApplication.getUiApplication().pushModalScreen(l.getScreen());
 		return l.getSelected();
 	}
 
 	public static Plantilla listadoPlantillas(boolean popup, int style) {
 		ListadoPlantillas l = new ListadoPlantillas(popup, style);
 		pushModalScreen(l.getScreen());
 		return l.getSelected();
 	}
 
 	public static Actuacion nuevaActuacion(Proceso proceso) {
 		NuevaActuacion n = new NuevaActuacion(proceso);
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getActuacion();
 	}
 
 	public static Actuacion nuevaActuacion() {
 		NuevaActuacion n = new NuevaActuacion();
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getActuacion();
 	}
 
 	public static CampoPersonalizado nuevoCampoPersonalizado() {
 		NuevoCampo n = new NuevoCampo();
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getCampo();
 	}
 
 	public static Categoria nuevaCategoria(boolean popup) {
 		NuevaCategoria n = new NuevaCategoria(popup);
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getCategoria();
 	}
 
 	public static Juzgado nuevoJuzgado() {
 		NuevoJuzgado n = new NuevoJuzgado();
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getJuzgado();
 	}
 
 	public static Persona nuevaPersona(int tipo) {
 		NuevaPersona n = new NuevaPersona(tipo);
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getPersona();
 	}
 
 	public static Proceso nuevoProceso() {
 		NuevoProceso n = new NuevoProceso();
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		return n.getProceso();
 	}
 
 	public static Plantilla nuevaPlantilla() {
 		NuevaPlantilla n = new NuevaPlantilla();
 		pushModalScreen(n.getScreen());
 		return n.getPlantilla();
 	}
 
 	public static String calendarToString(Calendar calendar, boolean largo) {
 		String string = "";
 		if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
 			string = string + "0";
 		}
 		string = string + calendar.get(Calendar.DAY_OF_MONTH);
 		string = string + "/";
 		if ((calendar.get(Calendar.MONTH) + 1) < 10) {
 			string = string + "0";
 		}
 		string = string + (calendar.get(Calendar.MONTH) + 1);
 		string = string + "/";
 		string = string + calendar.get(Calendar.YEAR);
 		if (largo) {
 			if (calendar.get(Calendar.HOUR) == 1) {
 				string = string + " a la ";
 			} else {
 				string = string + " a las ";
 			}
 		} else {
 			string = string + " - ";
 		}
		if (calendar.get(Calendar.HOUR) > 0 && calendar.get(Calendar.HOUR) < 10) {
 			string = string + "0";
 		}
		string += (calendar.get(Calendar.HOUR) == 0)? "12": calendar.get(Calendar.HOUR) +"";
 		string = string + ":";
 		if (calendar.get(Calendar.MINUTE) < 10) {
 			string = string + "0";
 		}
 		string = string + calendar.get(Calendar.MINUTE);
 		string = string + " ";
 		if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
 			string = string + ("AM");
 		} else {
 			string = string + ("PM");
 		}
 		return string;
 	}
 
 	public static void pushModalScreen(Screen screen) {
 		if (UI_Application == null) {
 			UI_Application = UiApplication.getUiApplication();
 		}
 		UI_Application.pushModalScreen(screen);
 	}
 }
