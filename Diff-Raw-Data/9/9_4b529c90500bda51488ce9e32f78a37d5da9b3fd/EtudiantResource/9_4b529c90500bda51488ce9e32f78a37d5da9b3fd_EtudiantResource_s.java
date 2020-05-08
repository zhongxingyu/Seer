 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.infostat.data.beans;
 
 import java.io.ByteArrayInputStream;
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
 import javax.ws.rs.core.Response;
 import org.infostat.data.dto.EtudiantDTO;
 import org.infostat.data.entities.Etudiant;
 import org.infostat.data.entities.beans.EtudiantFacadeLocal;
 
 /**
  *
  * @author boubad
  */
 @Stateless
 @Path("etudiant")
 public class EtudiantResource extends BaseDataBean {
 
     @EJB
     private EtudiantFacadeLocal facade;
 
     /**
      * Creates a new instance of EtudiantResource
      */
     public EtudiantResource() {
     }
 
     @POST
     @Consumes({"application/xml", "application/json"})
     public void create(EtudiantDTO entity) {
         Etudiant p = convertEtudiant(entity);
         facade.create(p);
     }// Create
 
     @PUT
     @Consumes({"application/xml", "application/json"})
     public void edit(EtudiantDTO entity) {
         Etudiant p = convertEtudiant(entity);
         facade.edit(p);
     }// edit
 
     @DELETE
     @Path("{id}")
     public void remove(@PathParam("id") Long id) {
         Etudiant p = facade.find(id);
         facade.remove(p);
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     @Path("{id}")
     public EtudiantDTO find(@PathParam("id") Long id) {
         Etudiant p = facade.find(id);
         EtudiantDTO pRet = convertEtudiant(p);
         if (pRet != null) {
             pRet.setPhotodata(null);
         }
         return pRet;
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     @Path("user")
     public List<EtudiantDTO> findByLastnameFirstname(@QueryParam("lastname") String lastname,
            @QueryParam("lastname") String firstname) {
         List<Etudiant> oList = facade.findByLastnameFirstname(lastname, firstname);
         List<EtudiantDTO> oRet = new ArrayList<EtudiantDTO>();
         for (Etudiant p : oList) {
             EtudiantDTO pp = convertEtudiant(p);
             pp.setPhotodata(null);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Produces({"image/jpeg"})
     @Path("photo/{id}")
     public Response findPhoto(@PathParam("id") Long id) {
         byte[] data = null;
         try {
             Etudiant p = facade.find(id);
             if (p != null) {
                 data = p.getPhotodata();
             }
         } catch (Exception ex) {
         }
         if (data != null) {
             ByteArrayInputStream bs = new ByteArrayInputStream(data);
             return Response.ok(data).build();
         }
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     public List<EtudiantDTO> findAll() {
         List<Etudiant> oList = facade.findAll();
         List<EtudiantDTO> oRet = new ArrayList<EtudiantDTO>();
         for (Etudiant p : oList) {
             EtudiantDTO pp = convertEtudiant(p);
             pp.setPhotodata(null);
             oRet.add(pp);
         }
         return oRet;
     }
 
     @GET
     @Path("{from}/{to}")
     @Produces({"application/xml", "application/json"})
     public List<EtudiantDTO> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
         List<Etudiant> oList = facade.findRange(new int[]{from, to});
         List<EtudiantDTO> oRet = new ArrayList<EtudiantDTO>();
         for (Etudiant p : oList) {
             EtudiantDTO pp = convertEtudiant(p);
             pp.setPhotodata(null);
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
