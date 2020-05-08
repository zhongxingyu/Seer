 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.client;
 
 import org.accesointeligente.client.events.*;
 import org.accesointeligente.shared.AppPlace;
 
 import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.TokenFormatter;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.History;
 
 import javax.inject.Inject;
 
 public class MyPlaceManager extends PlaceManagerImpl implements LoginSuccessfulEventHandler, LoginRequiredEventHandler {
 	private final String DEFAULT_PLACE = AppPlace.HOME;
 
 	@Inject
 	public MyPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter) {
 		super(eventBus, tokenFormatter);
 		eventBus.addHandler(LoginSuccessfulEvent.TYPE, this);
 		eventBus.addHandler(LoginRequiredEvent.TYPE, this);
 	}
 
 	@Override
 	public void revealDefaultPlace() {
 		revealPlace(new PlaceRequest(DEFAULT_PLACE));
 	}
 
 	@Override
 	public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
 		if (AppPlace.REQUEST.equals(unauthorizedHistoryToken)) {
 			revealPlace(new PlaceRequest(AppPlace.LOGIN));
 		} else {
 			revealDefaultPlace();
 		}
 	}
 
 	@Override
 	public void loginSuccessful(LoginSuccessfulEvent event) {
		if (getCurrentPlaceRequest().getNameToken().equals(AppPlace.LOGIN)) {
			History.back();
		}
 	}
 
 	@Override
 	public void loginRequired(LoginRequiredEvent event) {
 		ClientSessionUtil.destroySession();
 
 		if (AppPlace.REQUEST.equals(getCurrentPlaceRequest().getNameToken())) {
 			revealDefaultPlace();
 		} else {
 			revealCurrentPlace();
 		}
 	}
 
 }
