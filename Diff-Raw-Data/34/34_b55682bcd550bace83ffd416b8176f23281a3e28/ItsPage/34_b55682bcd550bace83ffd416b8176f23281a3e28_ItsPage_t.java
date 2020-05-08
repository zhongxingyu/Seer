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
 package org.jabox.webapp.pages.tabs;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.persistence.provider.ConfigXstreamDao;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.jabox.model.DefaultConfiguration;
 import org.jabox.webapp.pages.TabPage;
 import org.jabox.webapp.panels.HeaderLinksPage;
 import org.jabox.webapp.panels.HeaderLinksPanel;
 
 /**
  * {@link ItsPage} is showing the current I.T.S. inside an <code>iframe</code>.
  * TopMenu is visible in order to navigate from one server to another easily.
  */
 public class ItsPage extends TabPage {
 
    public ItsPage() {
        // Configure Header URL
        PageParameters params = new PageParameters();
        params.add("selected", HeaderLinksPanel.ITS);
        CharSequence header = urlFor(HeaderLinksPage.class, params);
        WebMarkupContainer wmc2 = new WebMarkupContainer("header");
        wmc2.add(new AttributeModifier("src", new Model<String>(header
            .toString())));
        add(wmc2);
 
        // Configure Main body URL
        final DefaultConfiguration dc = ConfigXstreamDao.getConfig();
        String url = dc.getIts().getServer().getUrl();
        WebMarkupContainer wmc = new WebMarkupContainer("frame");
        wmc.add(new AttributeModifier("src", new Model<String>(url)));
        add(wmc);
    }
 }
