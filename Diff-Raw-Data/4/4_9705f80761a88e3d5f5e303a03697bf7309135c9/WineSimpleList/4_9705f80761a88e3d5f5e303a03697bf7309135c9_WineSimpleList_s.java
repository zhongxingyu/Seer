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
 package fr.peralta.mycellar.interfaces.client.web.components.wine.list;
 
 import java.util.List;
 
 import org.apache.wicket.event.IEventSource;
 import org.apache.wicket.markup.html.form.NumberTextField;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDate;
 
 import fr.peralta.mycellar.domain.shared.repository.CountEnum;
 import fr.peralta.mycellar.domain.shared.repository.FilterEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.wine.Producer;
 import fr.peralta.mycellar.domain.wine.Wine;
 import fr.peralta.mycellar.domain.wine.WineTypeEnum;
 import fr.peralta.mycellar.domain.wine.repository.WineOrder;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.Action;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.OnBlurDefaultAjaxBehavior;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.list.SimpleList;
 import fr.peralta.mycellar.interfaces.client.web.components.wine.autocomplete.ProducerSimpleAutocomplete;
import fr.peralta.mycellar.interfaces.client.web.components.wine.cloud.AppellationComplexTagCloud;
 import fr.peralta.mycellar.interfaces.client.web.components.wine.cloud.AppellationSimpleTagCloud;
 import fr.peralta.mycellar.interfaces.client.web.components.wine.cloud.WineColorEnumFromProducerAndTypeTagCloud;
 import fr.peralta.mycellar.interfaces.client.web.components.wine.cloud.WineTypeEnumFromProducerTagCloud;
 import fr.peralta.mycellar.interfaces.facades.wine.WineServiceFacade;
 
 /**
  * @author speralta
  */
 public class WineSimpleList extends SimpleList<Wine> {
 
     private static final long serialVersionUID = 201109101937L;
 
     private static final String APPELLATION_COMPONENT_ID = "appellation";
     private static final String PRODUCER_COMPONENT_ID = "producer";
     private static final String TYPE_COMPONENT_ID = "type";
     private static final String COLOR_COMPONENT_ID = "color";
     private static final String VINTAGE_COMPONENT_ID = "vintage";
 
     private final IModel<SearchForm> searchFormModel;
 
     @SpringBean
     private WineServiceFacade wineServiceFacade;
 
     /**
      * @param id
      * @param label
      * @param searchFormModel
      */
     public WineSimpleList(String id, IModel<String> label, IModel<SearchForm> searchFormModel) {
         super(id, label);
         setOutputMarkupId(true);
         this.searchFormModel = searchFormModel;
         add(new AppellationSimpleTagCloud(APPELLATION_COMPONENT_ID, new StringResourceModel(
                 "appellation", this, null), searchFormModel, CountEnum.STOCK_QUANTITY));
         add(new ProducerSimpleAutocomplete(PRODUCER_COMPONENT_ID, new StringResourceModel(
                 "producer", this, null)));
         add(new EmptyPanel(TYPE_COMPONENT_ID).setOutputMarkupId(true));
         add(new EmptyPanel(COLOR_COMPONENT_ID).setOutputMarkupId(true));
         add(new NumberTextField<Integer>(VINTAGE_COMPONENT_ID).setMinimum(1800)
                 .setMaximum(new LocalDate().getYear()).add(new OnBlurDefaultAjaxBehavior()));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void internalConfigureComponent(Wine modelObject) {
         super.internalConfigureComponent(modelObject);
         if (modelObject == null) {
             setModelObject(new Wine());
         }
         get(APPELLATION_COMPONENT_ID).setVisibilityAllowed(!isValued());
         get(PRODUCER_COMPONENT_ID).setVisibilityAllowed(!isValued());
         get(TYPE_COMPONENT_ID).setVisibilityAllowed(!isValued());
         get(COLOR_COMPONENT_ID).setVisibilityAllowed(!isValued());
         get(VINTAGE_COMPONENT_ID).setVisibilityAllowed(!isValued());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean isReadyToSelect() {
         return ((ProducerSimpleAutocomplete) get(PRODUCER_COMPONENT_ID)).isValued()
                 || ((AppellationSimpleTagCloud) get(APPELLATION_COMPONENT_ID)).isValued();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected List<Wine> getList() {
         return wineServiceFacade.getWines(searchFormModel.getObject(), new WineOrder(), 0, 10);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     protected void onModelChanged(IEventSource source, Action action) {
         if (source instanceof ProducerSimpleAutocomplete) {
             ProducerSimpleAutocomplete producerSimpleAutocomplete = (ProducerSimpleAutocomplete) source;
             Producer sourceObject = producerSimpleAutocomplete.getModelObject();
             if ((sourceObject != null) && producerSimpleAutocomplete.isValued()) {
                 replace(new WineTypeEnumFromProducerTagCloud(TYPE_COMPONENT_ID,
                         new StringResourceModel("type", this, null),
                         (IModel<Producer>) producerSimpleAutocomplete.getDefaultModel(),
                         CountEnum.WINE));
             } else {
                 get(TYPE_COMPONENT_ID).setDefaultModelObject(null).replaceWith(
                         new EmptyPanel(TYPE_COMPONENT_ID));
                 get(COLOR_COMPONENT_ID).setDefaultModelObject(null).replaceWith(
                         new EmptyPanel(COLOR_COMPONENT_ID));
             }
             refreshList();
         } else if (source instanceof WineTypeEnumFromProducerTagCloud) {
             WineTypeEnumFromProducerTagCloud wineTypeEnumFromProducerTagCloud = (WineTypeEnumFromProducerTagCloud) source;
             WineTypeEnum sourceObject = wineTypeEnumFromProducerTagCloud.getModelObject();
             if ((sourceObject != null) && wineTypeEnumFromProducerTagCloud.isValued()) {
                 replace(new WineColorEnumFromProducerAndTypeTagCloud(COLOR_COMPONENT_ID,
                         new StringResourceModel("color", this, null), (IModel<Producer>) get(
                                 PRODUCER_COMPONENT_ID).getDefaultModel(),
                         (IModel<WineTypeEnum>) wineTypeEnumFromProducerTagCloud.getDefaultModel(),
                         CountEnum.WINE));
             } else {
                 get(COLOR_COMPONENT_ID).setDefaultModelObject(null).replaceWith(
                         new EmptyPanel(COLOR_COMPONENT_ID));
             }
             refreshList();
         } else if (source instanceof WineColorEnumFromProducerAndTypeTagCloud) {
             searchFormModel.getObject().replaceSet(FilterEnum.COLOR,
                     ((WineColorEnumFromProducerAndTypeTagCloud) source).getModelObject());
             refreshList();
         } else if (source instanceof AppellationSimpleTagCloud) {
             searchFormModel.getObject().replaceSet(FilterEnum.APPELLATION,
                    ((AppellationComplexTagCloud) source).getModelObject());
             refreshList();
         } else if (source instanceof NumberTextField) {
             searchFormModel.getObject().replaceSet(FilterEnum.VINTAGE,
                     ((NumberTextField<?>) source).getModelObject());
             refreshList();
         } else {
             super.onModelChanged(source, action);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void detachModels() {
         searchFormModel.detach();
         super.detachModels();
     }
 
 }
