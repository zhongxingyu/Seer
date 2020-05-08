 /**
  * Copyright 2011 Jason Ferguson
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.client.view;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.ViewImpl;
 import org.jason.mapmaker.client.presenter.MapmakerDisclaimerViewPresenter;
 
 /**
  * MapmakerDisclaimerViewImpl.java
  *
  * HTMLPanel for the Mapmaker application, meant to contain a disclaimer to be displayed at the bottom of the
  * screen.
  *
  * The stack panel extends GWT's {@link com.google.gwt.user.client.ui.Composite} while implenting
  * GWT-Platform's {@link com.gwtplatform.mvp.client.View} in order to get the best of both worlds.
 *
 * @since 0.1
  * @author Jason Ferguson
 * @see org.jason.mapmaker.client.presenter.MapmakerDisclaimerViewPresenter
  */
 @Singleton
public class MapmakerDisclaimerViewImpl extends ViewImpl
        implements MapmakerDisclaimerViewPresenter.MyView, IsWidget {
 
     @UiTemplate("MapmakerDisclaimerView.ui.xml")
     interface Binder extends UiBinder<HTMLPanel, MapmakerDisclaimerViewImpl> {}
 
     private static Binder binder = GWT.create(Binder.class);
 
     @UiField HTMLPanel mapmakerDisclaimerView;
 
 
     public MapmakerDisclaimerViewImpl() {
        mapmakerDisclaimerView = binder.createAndBindUi(this);
     }
 
     @Override
     public Widget asWidget() {
        return mapmakerDisclaimerView;
     }
 }
