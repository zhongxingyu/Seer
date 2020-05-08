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
 package fr.peralta.mycellar.interfaces.client.web.pages.pedia;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.peralta.mycellar.domain.wine.Appellation;
 import fr.peralta.mycellar.domain.wine.Country;
 import fr.peralta.mycellar.domain.wine.Region;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.Action;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.AjaxTool;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.multiple.MultiplePanel;
 import fr.peralta.mycellar.interfaces.client.web.pages.shared.PediaSuperPage;
 import fr.peralta.mycellar.interfaces.client.web.shared.LoggingUtils;
 import fr.peralta.mycellar.interfaces.facades.wine.WineServiceFacade;
 
 /**
  * @author speralta
  */
 public class PediaHomePage extends PediaSuperPage {
 
     private static final long serialVersionUID = 201108102315L;
     private static Logger logger = LoggerFactory.getLogger(PediaHomePage.class);
 
     private static final String FORM_COMPONENT_ID = "form";
     private static final String COUNTRIES_COMPONENT_ID = "countries";
     private static final String REGIONS_COMPONENT_ID = "regions";
     private static final String APPELLATIONS_COMPONENT_ID = "appellations";
 
     private final IModel<ArrayList<Country>> countries = new Model<ArrayList<Country>>();
     private final IModel<ArrayList<Region>> regions = new Model<ArrayList<Region>>();
     private final IModel<ArrayList<Appellation>> appellations = new Model<ArrayList<Appellation>>();
 
     @SpringBean
     private WineServiceFacade wineServiceFacade;
 
     /**
      * @param parameters
      */
     public PediaHomePage(PageParameters parameters) {
         super(parameters);
         setOutputMarkupId(true);
         Form<?> form = new Form<Object>(FORM_COMPONENT_ID);
         form.add(new MultiplePanel<Country>(COUNTRIES_COMPONENT_ID, wineServiceFacade
                 .getCountriesWithCounts()));
         form.add(new MultiplePanel<Region>(REGIONS_COMPONENT_ID, wineServiceFacade
                 .getRegionsWithCounts()));
         form.add(new MultiplePanel<Appellation>(APPELLATIONS_COMPONENT_ID, wineServiceFacade
                 .getAppellationsWithCounts()));
         add(form);
         countries.setObject(new ArrayList<Country>());
         regions.setObject(new ArrayList<Region>());
         get(FORM_COMPONENT_ID + PATH_SEPARATOR + COUNTRIES_COMPONENT_ID).setDefaultModel(countries);
         get(FORM_COMPONENT_ID + PATH_SEPARATOR + REGIONS_COMPONENT_ID).setDefaultModel(regions);
         get(FORM_COMPONENT_ID + PATH_SEPARATOR + APPELLATIONS_COMPONENT_ID).setDefaultModel(
                 appellations);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     public void onEvent(IEvent<?> event) {
         LoggingUtils.logEventReceived(logger, event);
         if (event.getPayload() instanceof Action) {
             Action action = (Action) event.getPayload();
             switch (action) {
             case MODEL_CHANGED:
                 if (event.getSource() instanceof MultiplePanel) {
                     MultiplePanel<?> component = (MultiplePanel<?>) event.getSource();
                     if (COUNTRIES_COMPONENT_ID.equals(component.getId())) {
                         List<Country> countries = (List<Country>) component.getDefaultModelObject();
                        ((MultiplePanel<Region>) get(FORM_COMPONENT_ID + PATH_SEPARATOR
                                + REGIONS_COMPONENT_ID)).setChoices(wineServiceFacade
                                 .getRegionsWithCounts(countries.toArray(new Country[countries
                                        .size()])));
                        List<Region> regions = new ArrayList<Region>();
                        for (Country country : countries) {
                            regions.addAll(country.getRegions());
                        }
                         ((MultiplePanel<Appellation>) get(FORM_COMPONENT_ID + PATH_SEPARATOR
                                 + APPELLATIONS_COMPONENT_ID)).setChoices(wineServiceFacade
                                 .getAppellationsWithCounts(regions.toArray(new Region[regions
                                         .size()])));
                     } else if (REGIONS_COMPONENT_ID.equals(component.getId())) {
                         List<Region> regions = (List<Region>) component.getDefaultModelObject();
                         ((MultiplePanel<Appellation>) get(FORM_COMPONENT_ID + PATH_SEPARATOR
                                 + APPELLATIONS_COMPONENT_ID)).setChoices(wineServiceFacade
                                 .getAppellationsWithCounts(regions.toArray(new Region[regions
                                         .size()])));
                     }
                 }
                 break;
             }
             AjaxTool.ajaxReRender(this);
         }
         LoggingUtils.logEventProcessed(logger, event);
     }
 }
