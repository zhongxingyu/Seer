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
 package fr.peralta.mycellar.interfaces.client.web.components.wine.edit;
 
 import org.apache.wicket.extensions.markup.html.form.select.Select;
 import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
 import org.apache.wicket.markup.html.form.NumberTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.StringResourceModel;
 
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.wine.WineColorEnum;
 import fr.peralta.mycellar.domain.wine.WineTypeEnum;
 import fr.peralta.mycellar.interfaces.client.web.behaviors.OnEventModelChangedAjaxBehavior;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.SearchFormModel;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.feedback.FormComponentFeedbackBorder;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.select.SelectEnumUtils;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.select.SelectRenderer;
 import fr.peralta.mycellar.interfaces.client.web.components.wine.autocomplete.AppellationComplexTypeahead;
 import fr.peralta.mycellar.interfaces.client.web.components.wine.autocomplete.ProducerComplexTypeahead;
 
 /**
  * @author speralta
  */
 public class WineEditPanel extends Panel {
 
     private static final long serialVersionUID = 201109081819L;
 
     /**
      * @param id
      */
     public WineEditPanel(String id) {
         super(id);
         SearchFormModel searchFormModel = new SearchFormModel(new SearchForm());
         add(new ProducerComplexTypeahead("producer",
                 new StringResourceModel("producer", this, null), searchFormModel));
         add(new AppellationComplexTypeahead("appellation", new StringResourceModel("appellation",
                 this, null), searchFormModel));
         add(new FormComponentFeedbackBorder("type").add(new Select<WineTypeEnum>("type")
                 .setRequired(true)
                 .add(new SelectOptions<WineTypeEnum>("options", SelectEnumUtils
                         .nullBeforeValues(WineTypeEnum.class), new SelectRenderer<WineTypeEnum>()))
                 .add(new OnEventModelChangedAjaxBehavior("select"))));
         add(new FormComponentFeedbackBorder("color")
                 .add(new Select<WineColorEnum>("color")
                         .setRequired(true)
                         .add(new SelectOptions<WineColorEnum>("options", SelectEnumUtils
                                 .nullBeforeValues(WineColorEnum.class),
                                 new SelectRenderer<WineColorEnum>()))
                         .add(new OnEventModelChangedAjaxBehavior("select"))));
        add(new FormComponentFeedbackBorder("vintage").add(new NumberTextField<Integer>("vintage")));
         add(new FormComponentFeedbackBorder("name").add(new TextField<String>("name")));
         add(new FormComponentFeedbackBorder("ranking").add(new TextField<String>("ranking")));
     }
 
 }
