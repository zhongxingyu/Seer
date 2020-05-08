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
 package org.jabox.webapp.menubuttons;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.form.ImageButton;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.persistence.provider.ContainerXstreamDao;
import org.apache.wicket.persistence.provider.ServerXstreamDao;
 import org.apache.wicket.persistence.provider.UserXstreamDao;
 import org.apache.wicket.request.resource.ResourceReference;
 import org.apache.wicket.request.resource.SharedResourceReference;
 import org.jabox.apis.IBaseEntity;
 import org.jabox.model.Container;
import org.jabox.model.Server;
 import org.jabox.model.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DeleteEntityButton<T extends IBaseEntity> extends ImageButton {
 	private static final Logger LOGGER = LoggerFactory
 		.getLogger(DeleteEntityButton.class);
 
 	private static final ResourceReference DELETE_IMG = new SharedResourceReference(
 			DeleteEntityButton.class, "edit-delete.png");
 	private static final long serialVersionUID = 1L;
 	private final T _item;
 	private Class<? extends Page> _responsePage;
 
 	public DeleteEntityButton(final String id, final T item,
 			final Class<? extends Page> responsePage) {
 		super(id, DELETE_IMG);
 		_item = item;
 		_responsePage = responsePage;
 	}
 
 	public DeleteEntityButton(final String id, final ListItem<T> item,
 			final Class<? extends Page> responsePage) {
 		this(id, item.getModelObject(), responsePage);
 	}
 
 	/**
 	 * Delete from persistent storage, commit transaction.
 	 */
 	@Override
 	public void onSubmit() {
 		LOGGER.info("Deleting entity");
 		if (User.class.isInstance(_item)) {
 			UserXstreamDao.deleteUser((User) _item);
 		} else if (Container.class.isInstance(_item)) {
 			ContainerXstreamDao.deleteContainer((Container) _item);
		} else if (Server.class.isInstance(_item)) {
			ServerXstreamDao.deleteServer((Server) _item);
 		}
 

 		setResponsePage(_responsePage);
 	}
 }
