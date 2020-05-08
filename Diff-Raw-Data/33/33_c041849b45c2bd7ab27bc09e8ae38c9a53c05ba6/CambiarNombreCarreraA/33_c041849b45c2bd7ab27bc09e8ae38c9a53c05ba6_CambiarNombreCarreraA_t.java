 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Actions.Carrera;
 
 import Clases.Carrera;
import Clases.Decanato;
import Clases.Usuario;
 import DBMS.DBMS;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 
 /**
  *
  * @author admin
  */
 public class CambiarNombreCarreraA extends org.apache.struts.action.Action {
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "success";
     private static final String FAILURE = "failure";
 
     /**
      * This is the action called from the Struts framework.
      *
      * @param mapping The ActionMapping used to select this instance.
      * @param form The optional ActionForm bean for this request.
      * @param request The HTTP Request we are processing.
      * @param response The HTTP Response we are processing.
      * @throws java.lang.Exception
      * @return
      */
     @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         Carrera u = (Carrera) form;
         HttpSession session = request.getSession(true);
 
         ActionErrors error = new ActionErrors();
 
         //valido los campos de formulario
         error = u.validate(mapping, request);
         boolean huboError = false;
 
 
         if (error.size() != 0) {
             huboError = true;
         }
 
         //si los campos no son validos
         if (huboError) {
             saveErrors(request, error);
             return mapping.findForward(FAILURE);
             //si los campos son validos
         } else {
 
             boolean actualizo = DBMS.getInstance().actualizarNombreCarrera(u);
 
             if (actualizo) {
                ArrayList<Carrera>[] carreras = null;
                Usuario usuario = (Usuario) session.getAttribute("usuario");

                if (usuario.getTipousuario().equals("decanato")) {
                    carreras = DBMS.getInstance().listarCarrerasDecanato(usuario.getUsbid());
                } else if (usuario.getTipousuario().equals("administrador")) {
                    Decanato decanato = (Decanato) session.getAttribute("decanato_actual");
                    carreras = DBMS.getInstance().listarCarrerasDecanato(decanato.getCodigo());
                }

                session.setAttribute("carreras_visibles", carreras[0]);
                session.setAttribute("carreras_ocultas", carreras[1]);
                request.setAttribute("modificacion", SUCCESS);
                return mapping.findForward(usuario.getTipousuario());
             } else {
                 error.add("actualizacion", new ActionMessage("error.actualizacion.errada"));
                 saveErrors(request, error);
                 return mapping.findForward(FAILURE);
             }
         }
 
 
 
 
 
 
     }
 }
