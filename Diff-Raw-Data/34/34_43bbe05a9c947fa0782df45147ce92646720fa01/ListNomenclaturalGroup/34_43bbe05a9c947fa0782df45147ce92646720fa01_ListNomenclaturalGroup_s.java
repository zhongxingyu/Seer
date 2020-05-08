 /* Ara - capture species and specimen data
  *
  * Copyright (C) 2009  INBio (Instituto Naciona de Biodiversidad)
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
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import java.util.ArrayList;
 import java.util.Locale;
 import javax.faces.FacesException;
 import javax.faces.component.html.HtmlDataTable;
 import org.inbio.ara.AraSessionBean;
 import org.inbio.ara.dto.security.NomenclaturalGroupDTO;
 import org.inbio.ara.util.AddRemoveList;
 import org.inbio.ara.util.MessageBean;
 
 /**
  * <p>Page bean that corresponds to ListNomenclaturalGroup.</p
  *
  * @version Created on 23/11/2009, 03:54:22 PM
  * @author asanabria
  */
 
 public class ListNomenclaturalGroup extends AbstractPageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
 
     // </editor-fold>
 
     // GUI bindings --  Table
     private HtmlDataTable dataTable = new HtmlDataTable();
 
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public ListNomenclaturalGroup() {
     }
 
     /**
      * <p>Callback method that is called whenever a page is navigated to,
      * either directly via a URL, or indirectly via page navigation.</p>
      * 
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
             log("ListNomenclaturalGroup Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
     }
 
     /**
      * <p>Callback method that is called after the component tree has been
      * restored, but before any event processing takes place.</p>
      */
     @Override
     public void preprocess() {
     }
 
     /**
      * <p>Callback method that is called just before rendering takes place.</p>
      */
     @Override
     public void prerender() {
         NomenclaturalGroupSessionBean ngsb =
             this.getNomenclaturalGroupSessionBean();
         
         if(ngsb.getPagination() == null)
             ngsb.initDataProvider();
     }
 
     /**
      * <p>Callback method that is called after rendering is completed for
      * this request, if <code>init()</code> was called (regardless of whether
      * or not this was the page that was actually rendered).</p>
      */
     @Override
     public void destroy() {
     }
 
     public String btnNewAction(){
         this.getNomenclaturalGroupSessionBean().setCurrentNomenclaturalGroupDTO
                 (new NomenclaturalGroupDTO());
         this.getNomenclaturalGroupSessionBean().
                 setArRegions(new AddRemoveList());
         this.getNomenclaturalGroupSessionBean().
                 setArTaxons(new AddRemoveList());
         this.getNomenclaturalGroupSessionBean().setSelectedCertifier(null);
         this.getNomenclaturalGroupSessionBean().setSelectedCollection(null);
         this.getNomenclaturalGroupSessionBean().setSelectedCommon(null);
         return "new";
     }
 
     public String btnEditAction(){
         NomenclaturalGroupSessionBean nsb = this.getNomenclaturalGroupSessionBean();
 
         int n = this.getDataTable().getRowCount();
         ArrayList<NomenclaturalGroupDTO> selectedNomenclatural = new ArrayList();
         for (int i = 0; i < n; i++) { //Obtener elementos seleccionados
             this.getDataTable().setRowIndex(i);
             NomenclaturalGroupDTO aux = (NomenclaturalGroupDTO) this.
                     getDataTable().getRowData();
             if (aux.isSelected()) {
                 selectedNomenclatural.add(aux);
             }
         }
         if(selectedNomenclatural == null || selectedNomenclatural.size() == 0){
             //En caso de que no se seleccione ningun elemento
             MessageBean.setErrorMessageFromBundle("not_selected",
                     this.getMyLocale());
             return null;
         }
         else if(selectedNomenclatural.size() == 1){
             //En caso de que solo se seleccione un elemento
             //Reestablecer el valor default del DTO y de los add remove components
             nsb.setCurrentNomenclaturalGroupDTO(new NomenclaturalGroupDTO());
             nsb.setArRegionsEdit(new AddRemoveList());
             nsb.setArTaxonsEdit(new AddRemoveList());
             this.getNomenclaturalGroupSessionBean().setSelectedCertifier(null);
             this.getNomenclaturalGroupSessionBean().setSelectedCollection(null);
             this.getNomenclaturalGroupSessionBean().setSelectedCommon(null);
             /*Indicar a la pantalla de edit que cargue 1 sola ves los datos
             seleccionados de los AddRemove*/
             nsb.setFirstTime(true);
             //Setear en el bean de session los datos correspondientes
             NomenclaturalGroupDTO myDTO = selectedNomenclatural.get(0);
             nsb.setCurrentNomenclaturalGroupDTO(myDTO);
             //Llamada al jsp encargado de la edicion de recolecciones
             return "edit";
         }
         else{ //En caso de que sea seleccion multiple
             MessageBean.setErrorMessageFromBundle("not_yet",
                     this.getMyLocale());
             return null;
         }
     }
 
 
     public String btnDeleteAction(){
 
 
         NomenclaturalGroupSessionBean ngsb =
             this.getNomenclaturalGroupSessionBean();
 
         int n = dataTable.getRowCount();
         ArrayList<NomenclaturalGroupDTO> selectedList = new ArrayList();
 
         // gets the selected elements
         for (int i = 0; i < n; i++) {
 
             dataTable.setRowIndex(i);
 
             NomenclaturalGroupDTO aux =
                 (NomenclaturalGroupDTO) this.dataTable.getRowData();
 
             if (aux.isSelected()) {
                 selectedList.add(aux);
             }
         }
 
         // if no item was selected prints error message.
         if(selectedList == null || selectedList.size() == 0){
             MessageBean.setErrorMessageFromBundle("not_selected"
                                                     , this.getMyLocale());
             return null;
         }
         // if just one item was selected by the user
         else if(selectedList.size() == 1){
             NomenclaturalGroupDTO selected = selectedList.get(0);
 
             try{ // try to delete the nomenclatural group
                 ngsb.deleteNomenclaturalGroup(selected.getNomenclaturalGroupId());
             }
             catch(Exception e){// the nomenclatural group is imposible to delete
                 MessageBean.setErrorMessageFromBundle("imposible_to_delete"
                                                         , this.getMyLocale());
                 return null;
             }
 
             //Refrescar la lista de audiencias
             ngsb.getPagination().deleteItem();
             ngsb.getPagination().refreshList();
 
             //Notificar al usuario
             MessageBean.setSuccessMessageFromBundle("delete_success"
                                                     , this.getMyLocale());
             return null;
         }
         else{ //En caso de que sea seleccion multiple
             MessageBean.setErrorMessageFromBundle("not_yet"
                                                     , this.getMyLocale());
             return null;
         }
     }
     
      /**
      * <p>Return a reference to NomenclaturalGroupSessionBean</p>
      * @return NomenclaturalGroupSessionBean Instance
      */
     protected NomenclaturalGroupSessionBean getNomenclaturalGroupSessionBean() {
         return (NomenclaturalGroupSessionBean)getBean("taxonomy$NomenclaturalGroupSessionBean");
     }
 
      /**
      * <p>Return a reference AraSessionBean</p>
      * @return AraSessionBean Instance
      */
     protected AraSessionBean getAraSessionBean() {
         return (AraSessionBean) getBean("AraSessionBean");
     }
 
 
     /**
      * @return the myLocale
      */
     public Locale getMyLocale() {
         return this.getAraSessionBean().getCurrentLocale();
     }
 
 
     /* Getters and setters */
     public HtmlDataTable getDataTable() {
         return dataTable;
     }
 
     public void setDataTable(HtmlDataTable dataTable) {
         this.dataTable = dataTable;
     }
 
 }
