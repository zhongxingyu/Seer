 /* Ara - capture species and specimen data
  *
  * Copyright (C) 2009  INBio (Instituto Nacional de Biodiversidad)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.inbio.ara.taxonomy;
 
 import com.sun.rave.web.ui.appbase.AbstractSessionBean;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.FacesException;
 import org.inbio.ara.util.AddRemoveList;
 import org.inbio.ara.AraSessionBean;
 import org.inbio.ara.dto.security.NomenclaturalGroupDTO;
 import org.inbio.ara.facade.taxonomy.TaxonomyFacadeRemote;
 import org.inbio.ara.util.PaginationControllerRemix;
 import org.inbio.ara.util.PaginationCoreInterface;
 
 /**
  * <p>Session scope data bean for your application. Corresponds to the
  * Nomenclatural Group subModule</p>
  *
  * @version Created on 23/11/2009, 04:30:46 PM
  * @author esmata
  */
 
 public class NomenclaturalGroupSessionBean extends AbstractSessionBean
     implements PaginationCoreInterface{
 
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     // </editor-fold>
 
     //Injections
     @EJB
     private TaxonomyFacadeRemote taxonomyFacadeImpl;
 
     //Objeto AddRemoveList para manejar los valores del tab de taxones
     private AddRemoveList arTaxons = new AddRemoveList();
     //Objeto AddRemoveList para manejar los valores del tab de regiones
     private AddRemoveList arRegions = new AddRemoveList();
     //Objeto AddRemoveList para manejar los valores del tab de taxones
     private AddRemoveList arTaxonsEdit = new AddRemoveList();
     //Objeto AddRemoveList para manejar los valores del tab de regiones
     private AddRemoveList arRegionsEdit = new AddRemoveList();
     // controls the pagination
     private PaginationControllerRemix pagination = null;
     // Number of elements to display in the list
     private int quantity = 10; // Just 10 elementos per default
     // Nomenclatural Group DTO to edit
     private NomenclaturalGroupDTO currentNomenclaturalGroupDTO = 
             new NomenclaturalGroupDTO();
 
     private Long selectedCollection = null;
     private Long selectedCertifier = null;
     private Character selectedCommon = null;
 
     /**
      * Le indica al prerender de la pantalla de edit que cargue los
      * datos seleccionados de los AddRemove solamnete UNA vez
      * true = cargar datos
      * false = ignorar la carga de datos seleccionados
      */
     private boolean firstTime = true;
 
     /**
      * <p>Construct a new session data bean instance.</p>
      */
     public NomenclaturalGroupSessionBean() {
     }
 
     /**
      * <p>This method is called when this bean is initially added to
      * session scope.</p>
      */
     @Override
     public void init() {
         // Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
         
         // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("NomenclaturalGroupSessionBean Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
     }
 
     /**
      * <p>This method is called when the session containing it is about to be
      * passivated.</p>
      */
     @Override
     public void passivate() {
     }
 
     /**
      * <p>This method is called when the session containing it was
      * reactivated.</p>
      */
     @Override
     public void activate() {
     }
 
     /**
      * <p>This method is called when this bean is removed from
      * session scope.</p>
      */
     @Override
     public void destroy() {
     }
 
     /**
      * @return the arTaxons
      */
     public AddRemoveList getArTaxons() {
         return arTaxons;
     }
 
     /**
      * @param arTaxons the arTaxons to set
      */
     public void setArTaxons(AddRemoveList arTaxons) {
         this.arTaxons = arTaxons;
     }
 
     /**
      * @return the arRegions
      */
     public AddRemoveList getArRegions() {
         return arRegions;
     }
 
     /**
      * @param arRegions the arRegions to set
      */
     public void setArRegions(AddRemoveList arRegions) {
         this.arRegions = arRegions;
     }
 
     /**
      * @return the arTaxonsEdit
      */
     public AddRemoveList getArTaxonsEdit() {
         return arTaxonsEdit;
     }
 
     /**
      * @param arTaxonsEdit the arTaxonsEdit to set
      */
     public void setArTaxonsEdit(AddRemoveList arTaxonsEdit) {
         this.arTaxonsEdit = arTaxonsEdit;
     }
 
     /**
      * @return the arRegionsEdit
      */
     public AddRemoveList getArRegionsEdit() {
         return arRegionsEdit;
     }
 
     /**
      * @param arRegionsEdit the arRegionsEdit to set
      */
     public void setArRegionsEdit(AddRemoveList arRegionsEdit) {
         this.arRegionsEdit = arRegionsEdit;
     }

    /**
     * @return un String que contiene el detalle de la paginacion
     */
    public String getQuantityTotal() {
        int actualPage = this.getPagination().getActualPage();
        int resultsPerPage = this.getPagination().getResultsPerPage();
        int totalResults = this.getPagination().getTotalResults();
        return "  " + (actualPage + 1) + " - " + (actualPage + resultsPerPage) + "  | " + totalResults + "  ";
    }
     
     /*
      * return the results of a consult of the pagination.
      */
     public List getResults(int firstResult, int maxResults) {
 
         Long collectionId = this.getAraSessionBean().getGlobalCollectionId();
 
         return getTaxonomyFacadeImpl().getAllNomenclaturalGroupsPaginated(
                                         firstResult
                                         , maxResults
                                         , collectionId);
     }
 
     /**
      * Inicializar el data provider
      */
     public void initDataProvider() {
         pagination =
             new PaginationControllerRemix(
                 this.getTaxonomyFacadeImpl().countAllNomenclaturalGroups().intValue()
                 , this.getQuantity()
                 , this);
     }
 
 
     /**
      * Deletes the Nomenclatural group specified by the provided id
      * @param id
      */
     public void deleteNomenclaturalGroup(Long id){
         getTaxonomyFacadeImpl().deleteNomenclaturalGroup(id);
     }
 
      /**
      * <p>Return a reference AraSessionBean</p>
      * @return AraSessionBean Instance
      */
     protected AraSessionBean getAraSessionBean() {
         return (AraSessionBean) getBean("AraSessionBean");
     }
 
     /* Getters and Setters */
     public NomenclaturalGroupDTO getCurrentNomenclaturalGroupDTO() {
         this.currentNomenclaturalGroupDTO.setUserName
                 (this.getAraSessionBean().getGlobalUserName());
         return currentNomenclaturalGroupDTO;
     }
 
     public void setCurrentNomenclaturalGroupDTO(NomenclaturalGroupDTO currentNomenclaturalGroupDTO) {
         this.currentNomenclaturalGroupDTO = currentNomenclaturalGroupDTO;
     }
 
     public PaginationControllerRemix getPagination() {
         return pagination;
     }
 
     public void setPagination(PaginationControllerRemix pagination) {
         this.pagination = pagination;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     /**
      * @return the taxonomyFacadeImpl
      */
     public TaxonomyFacadeRemote getTaxonomyFacadeImpl() {
         return taxonomyFacadeImpl;
     }
 
     /**
      * @param taxonomyFacadeImpl the taxonomyFacadeImpl to set
      */
     public void setTaxonomyFacadeImpl(TaxonomyFacadeRemote taxonomyFacadeImpl) {
         this.taxonomyFacadeImpl = taxonomyFacadeImpl;
     }
 
     /**
      * @return the firstTime
      */
     public boolean isFirstTime() {
         return firstTime;
     }
 
     /**
      * @param firstTime the firstTime to set
      */
     public void setFirstTime(boolean firstTime) {
         this.firstTime = firstTime;
     }
 
     /**
      * @return the selectedCollection
      */
     public Long getSelectedCollection() {
         return selectedCollection;
     }
 
     /**
      * @param selectedCollection the selectedCollection to set
      */
     public void setSelectedCollection(Long selectedCollection) {
         this.selectedCollection = selectedCollection;
     }
 
     /**
      * @return the selectedCommon
      */
     public Character getSelectedCommon() {
         return selectedCommon;
     }
 
     /**
      * @param selectedCommon the selectedCommon to set
      */
     public void setSelectedCommon(Character selectedCommon) {
         this.selectedCommon = selectedCommon;
     }
 
     /**
      * @return the selectedCertifier
      */
     public Long getSelectedCertifier() {
         return selectedCertifier;
     }
 
     /**
      * @param selectedCertifier the selectedCertifier to set
      */
     public void setSelectedCertifier(Long selectedCertifier) {
         this.selectedCertifier = selectedCertifier;
     }
 }
