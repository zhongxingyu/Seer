 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.client.web.pages.cellar;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.peralta.mycellar.domain.shared.repository.CountEnum;
 import fr.peralta.mycellar.domain.shared.repository.FilterEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.stock.Arrival;
 import fr.peralta.mycellar.domain.stock.ArrivalBottle;
 import fr.peralta.mycellar.interfaces.client.web.behaviors.OnChangeDefaultAjaxBehavior;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.Action;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.AjaxTool;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.SearchFormModel;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.feedback.FeedbackPanel;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.feedback.FilteredContainerVisibleFeedbackMessageFilter;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.feedback.FormComponentFeedbackBorder;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.form.LocalDateTextField;
 import fr.peralta.mycellar.interfaces.client.web.components.stock.cloud.CellarComplexTagCloud;
 import fr.peralta.mycellar.interfaces.client.web.components.stock.edit.ArrivalBottlesEditPanel;
 import fr.peralta.mycellar.interfaces.client.web.components.stock.form.ArrivalBottleForm;
 import fr.peralta.mycellar.interfaces.client.web.pages.shared.CellarSuperPage;
 import fr.peralta.mycellar.interfaces.client.web.security.UserKey;
 import fr.peralta.mycellar.interfaces.client.web.shared.LoggingHelper;
 import fr.peralta.mycellar.interfaces.facades.stock.StockServiceFacade;
 
 /**
  * @author speralta
  */
 public class PackageArrivalPage extends CellarSuperPage {
 
     private static class ArrivalForm extends Form<Arrival> {
 
         private static final long serialVersionUID = 201108091934L;
 
         @SpringBean
         private StockServiceFacade stockServiceFacade;
 
         /**
          * @param id
          */
         public ArrivalForm(String id) {
             super(id, new CompoundPropertyModel<Arrival>(new Arrival()));
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         protected void onSubmit() {
             stockServiceFacade.arrival(getModelObject());
             setResponsePage(InputOutputPage.class);
         }
 
     }
 
     private static final long serialVersionUID = 201108091851L;
     private static final Logger logger = LoggerFactory.getLogger(PackageArrivalPage.class);
 
     private static final String FORM_COMPONENT_ID = "form";
     private static final String ARRIVAL_BOTTLES_COMPONENT_ID = "arrivalBottles";
     private static final String ARRIVAL_BOTTLE_COMPONENT_ID = "arrivalBottle";
 
     private ArrivalBottleForm arrivalBottleForm;
     private final ArrivalForm arrivalForm;
     private final ArrivalBottlesEditPanel arrivalBottlesEditPanel;
 
     /**
      * @param parameters
      */
     public PackageArrivalPage(PageParameters parameters) {
         super(parameters);
         setOutputMarkupId(true);
         arrivalForm = new ArrivalForm(FORM_COMPONENT_ID);
         arrivalForm.add(new FormComponentFeedbackBorder("date").add(new LocalDateTextField("date")
                 .setRequired(true).add(new OnChangeDefaultAjaxBehavior())));
         arrivalForm.add(new FormComponentFeedbackBorder("source").add(new TextField<String>(
                 "source").setRequired(true).add(new OnChangeDefaultAjaxBehavior())));
         arrivalForm.add(new FormComponentFeedbackBorder("otherCharges").add(new TextField<Float>(
                 "otherCharges").setRequired(true).add(new OnChangeDefaultAjaxBehavior())));
         arrivalForm.add(arrivalBottlesEditPanel = new ArrivalBottlesEditPanel(
                 ARRIVAL_BOTTLES_COMPONENT_ID));
         arrivalForm.add(new CellarComplexTagCloud("cellar", new StringResourceModel("cellar", this,
                 null), new SearchFormModel(new SearchForm().setCellarModification(true).addToSet(
                 FilterEnum.USER, UserKey.getUserLoggedIn())), CountEnum.STOCK_QUANTITY,
                 FilterEnum.USER));
         arrivalForm.add(createHiddenBottleForm());
         arrivalForm.add(new FeedbackPanel("feedback",
                 new FilteredContainerVisibleFeedbackMessageFilter(arrivalForm,
                         ARRIVAL_BOTTLE_COMPONENT_ID)));
         add(arrivalForm);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onEvent(IEvent<?> event) {
         LoggingHelper.logEventReceived(logger, event);
         if (event.getPayload() instanceof Action) {
             Action action = (Action) event.getPayload();
             switch (action) {
             case ADD:
                 if (arrivalBottlesEditPanel.isAddBottle(event.getSource())) {
                     arrivalBottleForm.displayForm();
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             case SAVE:
                 if (arrivalBottleForm == event.getSource()) {
                     arrivalBottlesEditPanel.getModelObject()
                             .add(arrivalBottleForm.getModelObject());
                     arrivalForm.replace(createHiddenBottleForm());
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             case CANCEL:
                 if (arrivalBottleForm.isCancelButton(event.getSource())) {
                     arrivalForm.replace(createHiddenBottleForm());
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             default:
                 break;
             }
         }
         LoggingHelper.logEventProcessed(logger, event);
     }
 
     /**
      * @return
      */
     private Component createHiddenBottleForm() {
         return (arrivalBottleForm = new ArrivalBottleForm(ARRIVAL_BOTTLE_COMPONENT_ID,
                new SearchFormModel(new SearchForm().setCellarModification(true).addToSet(
                        FilterEnum.USER, UserKey.getUserLoggedIn())), new ArrivalBottle()))
                .hideForm();
     }
 }
