 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.infostat.data.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PUT;
 import javax.ws.rs.GET;
 import javax.ws.rs.Produces;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import org.infostat.data.dto.MatiereDTO;
 import org.infostat.data.entities.Matiere;
 import org.infostat.data.entities.beans.MatiereFacadeLocal;
 
 /**
  *
  * @author boubad
  */
 @Stateless
 @Path("matiere")
 public class MatiereResource extends BaseDataBean {
 
     @EJB
     private MatiereFacadeLocal facade;
 
     public MatiereResource() {
     }
 
     @POST
     @Consumes({"application/xml", "application/json"})
     public void create(MatiereDTO entity) {
         Matiere p = convertMatiere(entity);
         facade.create(p);
     }// Create
 
     @PUT
     @Consumes({"application/xml", "application/json"})
     public void edit(MatiereDTO entity) {
         Matiere p = convertMatiere(entity);
         facade.edit(p);
     }// edit
 
     @DELETE
     @Path("{id}")
     public void remove(@PathParam("id") Long id) {
         Matiere p = facade.find(id);
         facade.remove(p);
     }
 
     @GET
     @Path("{id}")
     @Produces({"application/xml", "application/json"})
     public MatiereDTO find(@PathParam("id") Long id) {
         Matiere p = facade.find(id);
         return convertMatiere(p);
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     public List<MatiereDTO> findAll() {
         List<Matiere> oList = facade.findAll();
         List<MatiereDTO> oRet = new ArrayList<MatiereDTO>();
         for (Matiere p : oList) {
             MatiereDTO pp = convertMatiere(p);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Path("{from}/{to}")
     @Produces({"application/xml", "application/json"})
     public List<MatiereDTO> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
         List<Matiere> oList = facade.findRange(new int[]{from, to});
         List<MatiereDTO> oRet = new ArrayList<MatiereDTO>();
         for (Matiere p : oList) {
             MatiereDTO pp = convertMatiere(p);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Path("departement/{id}")
     @Produces({"application/xml", "application/json"})
     public List<MatiereDTO> findByDepartement(@PathParam("id") Integer id) {
         List<Matiere> oList =
                 facade.findByDepartementId(id);
         List<MatiereDTO> oRet = new ArrayList<MatiereDTO>();
         for (Matiere p : oList) {
             MatiereDTO pp = convertMatiere(p);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Path("unite/{id}")
     @Produces({"application/xml", "application/json"})
     public List<MatiereDTO> findByUnite(@PathParam("id") Integer id) {
         List<Matiere> oList = facade.findByUniteId(id);
         List<MatiereDTO> oRet = new ArrayList<MatiereDTO>();
         for (Matiere p : oList) {
             MatiereDTO pp = convertMatiere(p);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Path("user")
     @Produces({"application/xml", "application/json"})
     public List<MatiereDTO> findByUniteSigle(@QueryParam("id") Integer id,
            @QueryParam("id") String sigle) {
         List<Matiere> oList = facade.findByUniteSigle(id, sigle);
         List<MatiereDTO> oRet = new ArrayList<MatiereDTO>();
         for (Matiere p : oList) {
             MatiereDTO pp = convertMatiere(p);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Path("count")
     @Produces({"text/plain", "application/xml", "application/json"})
     public String count() {
         int nRet = facade.count();
         return String.valueOf(nRet);
         //return String.valueOf(super.count());
     }
 }
