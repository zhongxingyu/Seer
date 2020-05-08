 package com.solidstategroup.radar.web.pages.admin;
 
 import com.solidstategroup.radar.model.enums.ExportType;
 import com.solidstategroup.radar.service.ExportManager;
 import com.solidstategroup.radar.web.dataproviders.ConsultantsDataProvider;
 import com.solidstategroup.radar.web.resources.RadarResourceFactory;
 import org.apache.wicket.markup.html.link.ResourceLink;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import com.solidstategroup.radar.service.UtilityManager;
 import com.solidstategroup.radar.web.components.SortLink;
 import com.solidstategroup.radar.model.Consultant;
 import com.solidstategroup.radar.model.filter.ConsultantFilter;
 
 import java.util.Map;
 import java.util.HashMap;
 
 public class AdminConsultantsPage extends AdminsBasePage {
 
     @SpringBean
     private UtilityManager utilityManager;
     @SpringBean
     private ExportManager exportManager;
 
     private static final int RESULTS_PER_PAGE = 10;
 
     public AdminConsultantsPage() {
         final ConsultantsDataProvider consultantsDataProvider = new ConsultantsDataProvider(utilityManager);
 
         // TODO: need to hook these up
         add(new ResourceLink("exportPdf", RadarResourceFactory.getExportResource(
                 exportManager.getConsultantsExportData(ExportType.PDF), "consultants" +
                 AdminsBasePage.EXPORT_FILE_NAME_SUFFIX, ExportType.PDF)));
 
         add(new ResourceLink("exportCsv", RadarResourceFactory.getExportResource(
                 exportManager.getConsultantsExportData(ExportType.CSV), "consultants" +
                 AdminsBasePage.EXPORT_FILE_NAME_SUFFIX, ExportType.CSV)));
 
         add(new BookmarkablePageLink<AdminConsultantPage>("addNewConsultant", AdminConsultantPage.class));
 
         final WebMarkupContainer consultantsContainer = new WebMarkupContainer("consultantsContainer");
         consultantsContainer.setOutputMarkupId(true);
         add(consultantsContainer);
 
         final DataView<Consultant> consultantList = new DataView<Consultant>("consultants",
                 consultantsDataProvider) {
             @Override
             protected void populateItem(Item<Consultant> item) {
                 builtDataViewRow(item);
             }
         };
         consultantList.setItemsPerPage(RESULTS_PER_PAGE);
         consultantsContainer.add(consultantList);
 
         // add paging element
         consultantsContainer.add(new AjaxPagingNavigator("navigator", consultantList));
 
         // add sort links to the table column headers
         for (Map.Entry<String, String> entry : getSortFields().entrySet()) {
             add(new SortLink(entry.getKey(), entry.getValue(), consultantsDataProvider) {
                 @Override
                 public void onClicked(AjaxRequestTarget ajaxRequestTarget) {
                     consultantList.setCurrentPage(0);
                     ajaxRequestTarget.add(consultantsContainer);
                 }
             });
         }
     }
 
     /**
      * Build a row in the dataview from the object
      *
      * @param item Item<Consultant>
      */
     private void builtDataViewRow(Item<Consultant> item) {
         Consultant consultant = item.getModelObject();
 
         item.add(new BookmarkablePageLink<AdminConsultantPage>("edit", AdminConsultantPage.class,
                 AdminConsultantPage.getPageParameters(consultant)));
         item.add(new Label("surname", consultant.getSurname()));
         item.add(new Label("forename", consultant.getForename()));
 
         String centreName;
         try {
             centreName = consultant.getCentre().getName();
         } catch (Exception e) {
             centreName = "";
         }
 
         item.add(new Label("centre", centreName));
 
         int numberOfPatients = -1;
         try {
             numberOfPatients = utilityManager.getPatientCountByUnit(consultant.getCentre());
         } catch (Exception e) {
             numberOfPatients = 0;
         }
 
         item.add(new Label("numberOfPatients", Integer.toString(numberOfPatients)));
     }
 
     /**
      * List of columns that can be used to sort the results - will return ID of el to be bound to and the field to sort
      *
      * @return Map<String, ProfessionalUserFilter.UserField>
      */
     private Map<String, String> getSortFields() {
         return new HashMap<String, String>() {
             {
                 put("orderBySurname", ConsultantFilter.UserField.SURNAME.getDatabaseFieldName());
                 put("orderByForename", ConsultantFilter.UserField.FORENAME.getDatabaseFieldName());
                 put("orderByCentre", ConsultantFilter.UserField.CENTRE.getDatabaseFieldName());
             }
         };
     }
 }
