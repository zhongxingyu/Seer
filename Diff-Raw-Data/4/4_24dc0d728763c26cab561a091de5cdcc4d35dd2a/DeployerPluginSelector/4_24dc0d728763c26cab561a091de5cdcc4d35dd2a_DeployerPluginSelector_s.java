 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
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
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.webapp.pages;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.PropertyModel;
 import org.jabox.apis.Connector;
 import org.jabox.apis.IManager;
 import org.jabox.model.DeployerConfig;
 import org.jabox.model.Server;
 
 import com.google.inject.Inject;
 
 public class DeployerPluginSelector extends Panel {
 	private static final long serialVersionUID = -222526477140616108L;
 
 	@Inject
 	private IManager _manager;
 
 	@SuppressWarnings("unchecked")
 	public DeployerPluginSelector(final String id,
 			final IModel<Server> article,
 			final Class<? extends Connector> connectorClass) {
 		super(id);
 		add(new WebMarkupContainer("editor"));
 		String pluginId = article.getObject().deployerConfig != null ? article
				.getObject().deployerConfig.pluginId : "-1";
		if (article.getObject().deployerConfig != null) {
 			Connector plugin = _manager.getEntry(pluginId);
 			DeployerPluginSelector.this.replace(plugin.newEditor("editor",
 					new PropertyModel<Server>(article, "deployerConfig")));
 
 		}
 
 		add(new PluginPicker("picker", new CompoundPropertyModel(pluginId),
 				connectorClass) {
 			private static final long serialVersionUID = -5528219523437017579L;
 
 			@Override
 			protected void onSelectionChanged(final Object pluginId) {
 				Connector plugin = _manager.getEntry((String) pluginId);
 				Server configuration = article.getObject();
 				DeployerConfig newConfig = plugin.newConfig();
 				configuration.setDeployerConfig(newConfig);
 
 				DeployerPluginSelector.this.replace(plugin.newEditor("editor",
 						new PropertyModel(article, "deployerConfig")));
 			}
 		});
 
 	}
 
 	private static abstract class PluginPicker<T> extends DropDownChoice<T> {
 		private static final long serialVersionUID = 1346317031364661388L;
 
 		@Inject
 		private IManager _manager;
 
 		@SuppressWarnings("unchecked")
 		public PluginPicker(final String id, final IModel<T> model,
 				final Class<? extends Connector> connectorClass) {
 			super(id);
 			setRequired(true);
 			setModel(model);
 			setChoices(new LoadableDetachableModel() {
 				private static final long serialVersionUID = 6694323103247193118L;
 
 				@Override
 				protected List<? extends String> load() {
 					// XXX TESTING
 					return _manager.getIds(connectorClass);
 				}
 			});
 
 			setChoiceRenderer(new IChoiceRenderer() {
 				private static final long serialVersionUID = 7954936699435378919L;
 
 				public Object getDisplayValue(final Object object) {
 					return _manager.getEntry((String) object).getName();
 				}
 
 				public String getIdValue(final Object object, final int index) {
 					return (String) object;
 				}
 			});
 		}
 
 		@Override
 		protected boolean wantOnSelectionChangedNotifications() {
 			return true;
 		}
 
 		@Override
 		protected abstract void onSelectionChanged(Object pluginId);
 	}
 
 }
