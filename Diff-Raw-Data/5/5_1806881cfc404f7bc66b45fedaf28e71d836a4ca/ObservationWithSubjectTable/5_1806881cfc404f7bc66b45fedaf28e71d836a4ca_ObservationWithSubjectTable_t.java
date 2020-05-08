 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables;
 
 import com.google.common.eventbus.EventBus;
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.entities.IdObjectFactory;
 import com.vaadin.data.Container;
 import com.vaadin.data.util.converter.Converter;
 import com.vaadin.data.util.filter.Or;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.ui.Table;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Date: 3/17/13
  * Time: 4:45 PM
  */
 @Component
 @Scope("prototype")
 public class ObservationWithSubjectTable extends ObservationTable {
     @Autowired
     public ObservationWithSubjectTable(final ReadWriteDAO readWriteDAO, final IdObjectFactory idObjectFactory, final EventBus eventBus) {
         super(readWriteDAO, idObjectFactory, eventBus);
     }
 
     private static final List<HeaderInfo> headersWithSubject;
 
     static {
         headersWithSubject = new LinkedList<>(Arrays.asList(new HeaderInfo("observationSubject", "Subject", Table.Align.LEFT)));
         headersWithSubject.addAll(headers);
     }
 
     @Override
     protected List<HeaderInfo> getHeaderInfo() {
         return headersWithSubject;
     }
 
     @Override
     protected Container.Filter generateFilter(String searchFor) {
         //  TODO - this doesn't actually work due to the property search of objects
         return new Or(super.generateFilter(searchFor), new SimpleStringFilter("observationSubject", searchFor, true, false));
     }
 
     @Override
    protected String getDefaultSortField(List<String> properties) {
        return "observationTimestamp";
    }

    @Override
     protected void addColumnConverters() {
         super.addColumnConverters();
         entityTable.setConverter("observationSubject", new Converter<String, AppUserOwnedObject>() {
             @Override
             public AppUserOwnedObject convertToModel(final String value, final Locale locale) throws ConversionException {
                 //  TODO
                 return null;
             }
 
             @Override
             public String convertToPresentation(final AppUserOwnedObject value, final Locale locale) throws ConversionException {
                 return value == null ? null : value.getSummaryDescription();
             }
 
             @Override
             public Class<AppUserOwnedObject> getModelType() {
                 return AppUserOwnedObject.class;
             }
 
             @Override
             public Class<String> getPresentationType() {
                 return String.class;
             }
         });
     }
 
 
 }
