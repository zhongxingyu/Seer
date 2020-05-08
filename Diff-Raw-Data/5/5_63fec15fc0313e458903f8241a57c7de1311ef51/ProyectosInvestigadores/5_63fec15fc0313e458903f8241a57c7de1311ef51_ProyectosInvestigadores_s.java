 package cidc.proyectos.servlet;
 
 import java.io.IOException;
 
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.displaytag.util.ParamEncoder;
 
 import cidc.general.db.CursorDB;
 import cidc.general.login.Usuario;
 import cidc.general.servlet.ServletGeneral;
 import cidc.proyectos.db.ProyectosInvestigadorDB;
 import cidc.proyectos.obj.BalanceGeneral;
 import cidc.proyectos.obj.Contratacion;
 import cidc.proyectos.obj.Parametros;
 import cidc.proyectos.obj.ProyectoGenerico;
 import cidc.proyectos.obj.Rubros;
import cidc.proyectosGeneral.ProyectosXml.ProyectoXML;
 import cidc.proyectosGeneral.db.ProyectosGeneralDB;
 
 
 
 public class ProyectosInvestigadores extends ServletGeneral {
 
 	       Usuario usuario = null;
 
 	public String [] operaciones(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
 		String irA="/grupos/proyectos/ListaProyectos.jsp";
 		cursor=new CursorDB();
 		int accion=0;
 		HttpSession sesion=req.getSession();
 		usuario=(Usuario)sesion.getAttribute("loginUsuario");
 		ProyectosInvestigadorDB proyectosDB=new ProyectosInvestigadorDB(cursor,usuario.getPerfil());
 		ProyectosGeneralDB proyGeneral=new ProyectosGeneralDB(cursor, usuario.getPerfil());
 		ProyectoGenerico proyecto =null;
 		if(sesion.getAttribute("proyectoInvestigador")!=null)
 			proyecto = (ProyectoGenerico)sesion.getAttribute("proyectoInvestigador");
 		if(req.getParameter("accion")!=null)
 			accion = Integer.parseInt(req.getParameter("accion"));
 		switch(accion){
 			case Parametros.cmdVerProyecto:
 				ProyectoGenerico proyectoGen=proyectosDB.getProyecto(req.getParameter("id"),req.getParameter("tipo"));
 				sesion.setAttribute("proyectoInvestigador", proyectoGen);
				ProyectoXML proy=new ProyectoXML();
 				//String resp2=proy.crearProyectoBizagi(proyectoGen);
 				//System.out.println("resp crear proy "+resp2);
 				sesion.setAttribute("proyectoDocumentos", proyGeneral.getListaDocAnexos(Long.parseLong(req.getParameter("id")),Integer.parseInt(req.getParameter("tipo"))));
 				sesion.setAttribute("proyectoInvestigador", proyectosDB.getProyecto(req.getParameter("id"),req.getParameter("tipo")));
 				irA="/grupos/proyectos/VerProyecto.jsp";
 			break;
 			case Parametros.cmdBalanceGral:
 				sesion.setAttribute("balanceProyecto",proyectosDB.getBalanceProyecto(""+proyecto.getIdProyecto(),""+proyecto.getTipo()));
 				irA="/grupos/proyectos/BalanceGeneral.jsp";
 			break;
 			case Parametros.cmdListaGastosRubro:
 				sesion.removeAttribute("tipoPersona");
 				sesion.removeAttribute("tipoContratacion");
 				sesion.removeAttribute("idContrato");
 				List registroGasto = null;
 				BalanceGeneral balanc =(BalanceGeneral)sesion.getAttribute("balanceProyecto");
 				List<Rubros> lista= balanc.getListaRubros();
 				for (Rubros rubro : lista) {
 					if((rubro.getIdRubro()==Integer.parseInt(req.getParameter("idRub")))&&(rubro.getNombreRubro().equals("Personal"))){
 						registroGasto=lista;
 					}
 				}
 				sesion.setAttribute("gasto", registroGasto);
 				sesion.setAttribute("idRub",req.getParameter("idRub"));
 				sesion.setAttribute("listaGastosRubro",proyectosDB.getGastosRubrosDeLista((BalanceGeneral)sesion.getAttribute("balanceProyecto"),req.getParameter("idRub")));
 				req.setAttribute("natural", "display:none");
 				req.setAttribute("juridica", "display:none");
 				req.setAttribute("basico", "display:none");
 				sesion.setAttribute("adjuntos", "display:none");
 				sesion.setAttribute("lista", "display:none");
 				sesion.setAttribute("formulario", "display:block");
 				sesion.removeAttribute("contratacion");
 				irA="/grupos/proyectos/ListaGastos.jsp";
 			break;
 			case Parametros.ajaxTipoPersona:
 				Contratacion cont=(Contratacion)sesion.getAttribute("contratacion");
 				sesion.setAttribute("adjuntos", "display:none");
 				sesion.setAttribute("lista", "display:block");
 				sesion.setAttribute("formulario", "display:none");
 				irA="/grupos/proyectos/ListaGastos.jsp";
 				break;
 			case Parametros.tipoContrato:
 //				cont=(Contratacion)sesion.getAttribute("contratacion");
 //				CargarDocumento carg= new CargarDocumento();
 //				Date date = new Date();
 //				String nombre =String.valueOf(date.getTime());
 //				carg.cargar(req, nombre, "Bizagi");
 //				irA="/grupos/proyectos/ListaGastos.jsp";
 				break;
 			case Parametros.infoRubroSolicitado:
 				System.out.println("ingreso al servlet");
 				irA="/grupos/proyectos/InfoSolicitud.jsp";
 				break;
 			default:
 				req.setAttribute("listaProyectos", proyectosDB.getListaProyectos(usuario.getIdUsuario()));
 			irA="/grupos/proyectos/ListaProyectos.jsp";
 			break;
 		}
 
 		retorno[0]="desviar";
 		retorno[1]=irA;
 		retorno[2]=mensaje;
 		return retorno;
 		}
 }
 
