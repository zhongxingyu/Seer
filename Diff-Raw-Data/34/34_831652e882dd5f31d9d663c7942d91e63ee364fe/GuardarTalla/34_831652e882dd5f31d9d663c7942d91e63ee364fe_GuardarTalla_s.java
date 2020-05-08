 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Equipo;
 
 import Clases.Equipo;
 import DBMS.DBMS;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *
  * @author ivan
  */
 public class GuardarTalla extends org.apache.struts.action.Action {
 
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
         
         Equipo e = (Equipo) form;
         
         
         request.setAttribute("equipo",e);
         
         
        if (e.getTalla().equals("")){
             request.setAttribute("tallaVacia","error");
             return mapping.findForward(FAILURE);
         }
         
         boolean agregado = DBMS.getInstance().agregarTalla(e);
         
         if (agregado){
             ArrayList<Equipo> listaTallas = DBMS.getInstance().obtenerTallasEquipo(e);
             request.setAttribute("listaTallas",listaTallas);
             request.setAttribute("tallaAgregada","mensaje");
             return mapping.findForward(SUCCESS);
         }
         else{
             request.setAttribute("tallaNoAgregada","mensaje");
             return mapping.findForward(FAILURE);           
         }                
     }
 }
