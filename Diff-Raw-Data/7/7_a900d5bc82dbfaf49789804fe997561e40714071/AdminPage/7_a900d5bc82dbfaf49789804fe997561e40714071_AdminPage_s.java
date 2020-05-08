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
 package fr.peralta.mycellar.interfaces.client.web.pages.admin;
 
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.interfaces.client.web.components.stack.data.StackDataView;
 import fr.peralta.mycellar.interfaces.client.web.pages.shared.AdminSuperPage;
 import fr.peralta.mycellar.interfaces.client.web.resources.img.ImageReferences;
 import fr.peralta.mycellar.interfaces.facades.stack.StackServiceFacade;
 import fr.peralta.mycellar.interfaces.facades.user.UserServiceFacade;
 import fr.peralta.mycellar.interfaces.facades.wine.WineServiceFacade;
 
 /**
  * @author speralta
  */
 public class AdminPage extends AdminSuperPage {
 
     private static final long serialVersionUID = 201110101627L;
 
     @SpringBean
     private UserServiceFacade userServiceFacade;
 
     @SpringBean
     private WineServiceFacade wineServiceFacade;
 
     @SpringBean
     private StackServiceFacade stackServiceFacade;
 
     @SpringBean
     private DataSource dataSource;
 
     /**
      * @param parameters
      */
     public AdminPage(PageParameters parameters) {
         super(parameters);
         setOutputMarkupId(true);
         add(new Label("configType", WebApplication.get().getConfigurationType().name()));
         add(new Label("userCount", Long.toString(userServiceFacade.countUsers())));
         add(new Label("wineCount", Long.toString(wineServiceFacade.countWines(new SearchForm()))));
         add(new Label("stackCount", Long.toString(stackServiceFacade.countStacks())));
         add(new Image("db", ImageReferences.getDatabaseImage()));
         String dbUrl;
         String dbLogin;
         String dbDriver;
         try {
             DatabaseMetaData databaseMetaData = dataSource.getConnection().getMetaData();
             dbUrl = databaseMetaData.getURL();
             dbLogin = databaseMetaData.getUserName();
             dbDriver = databaseMetaData.getDriverName();
         } catch (SQLException e) {
             dbUrl = new StringResourceModel("metadataFailure", this, null).getObject();
             dbLogin = new StringResourceModel("metadataFailure", this, null).getObject();
             dbDriver = new StringResourceModel("metadataFailure", this, null).getObject();
         }
         add(new Label("dbDriver", dbDriver));
         add(new Label("dbUrl", dbUrl));
         add(new Label("dbLogin", dbLogin));
 
         add(new StackDataView("stacks"));
 
         add(new BookmarkablePageLink<Void>("lists", ListPage.class));
         add(new Link<Void>("cleanStacks") {
             private static final long serialVersionUID = 201111071322L;
 
             @Override
             public void onClick() {
                 stackServiceFacade.deleteAllStacks();
                 setResponsePage(AdminPage.class);
             }
         });
     }
 }
