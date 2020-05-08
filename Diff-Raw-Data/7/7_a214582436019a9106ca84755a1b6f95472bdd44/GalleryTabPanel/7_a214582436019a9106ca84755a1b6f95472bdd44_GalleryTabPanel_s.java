 /*
  *   Copyright 2009, Maarten Billemont
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.lyndir.lhunath.snaplog.webapp.tab;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.markup.html.tabs.ITab;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 
import com.google.common.collect.ImmutableList;
 import com.google.inject.Inject;
 import com.lyndir.lhunath.lib.system.localization.LocalizerFactory;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.wayward.component.GenericPanel;
 import com.lyndir.lhunath.snaplog.data.Album;
 import com.lyndir.lhunath.snaplog.data.Media;
 import com.lyndir.lhunath.snaplog.data.Provider;
 import com.lyndir.lhunath.snaplog.data.User;
 import com.lyndir.lhunath.snaplog.data.Media.Quality;
 import com.lyndir.lhunath.snaplog.messages.Messages;
 import com.lyndir.lhunath.snaplog.model.AlbumProvider;
 import com.lyndir.lhunath.snaplog.model.AlbumService;
 import com.lyndir.lhunath.snaplog.model.UserService;
 import com.lyndir.lhunath.snaplog.webapp.SnaplogSession;
 import com.lyndir.lhunath.snaplog.webapp.servlet.ImageServlet;
 
 
 /**
  * <h2>{@link GalleryTabPanel}<br>
  * <sub>[in short] (TODO).</sub></h2>
  * 
  * <p>
  * <i>Mar 1, 2010</i>
  * </p>
  * 
  * @param <P>
  *            The type of {@link Provider} that we can interface with.
  * @author lhunath
  */
 public class GalleryTabPanel extends GenericPanel<User> {
 
     Messages               msgs = LocalizerFactory.getLocalizer( Messages.class, this );
 
     @Inject
     UserService<Provider>  userService;
 
     @Inject
     AlbumService<Provider> albumService;
 
 
     /**
      * Create a new {@link GalleryTabPanel} instance.
      * 
      * @param id
      *            The wicket ID that will hold the {@link GalleryTabPanel}.
      * @param userModel
      *            The user whose gallery to show.
      */
     public GalleryTabPanel(String id, IModel<User> userModel) {
 
         super( id, userModel );
 
         add( new Label( "albumsTitleUsername", new LoadableDetachableModel<String>() {
 
             @Override
             protected String load() {
 
                 return getModelObject().toString();
             }
         } ) );
         add( new Label( "albumsHelpUsername", new LoadableDetachableModel<String>() {
 
             @Override
             protected String load() {
 
                 return getModelObject().getUserName();
             }
         } ) );
 
         add( new DataView<Album<Provider>>( "albums", new UserAlbumsProvider<Provider>( userService, getModel() ) ) {
 
             @Override
             protected void populateItem(Item<Album<Provider>> item) {
 
                 item.add( new Link<Album<Provider>>( "link", item.getModel() ) {
 
                     {
                        ImmutableList<Media<Provider>> albumFiles = albumService.getFiles( getModelObject() );
                         add( new ContextImage( "cover",
                                 ImageServlet.getContextRelativePathFor( albumFiles.get( albumFiles.size() - 1 ),
                                                                         Quality.THUMBNAIL ) ) );
                         add( new Label( "title", getModelObject().getName() ) );
                         add( new Label( "description", getModelObject().getDescription() ).setVisible( getModelObject().getDescription() != null ) );
                     }
 
 
                     @Override
                     public void onClick() {
 
                         SnaplogSession.get().setFocussedAlbum( getModelObject() );
                         SnaplogSession.get().setActiveTab( Tab.ALBUM );
                     }
                 } );
             }
         } );
 
         final Form<Album<Provider>> newAlbumForm = new Form<Album<Provider>>( "newAlbumForm" ) {
 
             {
                 add( new DropDownChoice<AlbumProvider<Provider, Album<Provider>, Media<Provider>>>( "type" ) );
             }
         };
         add( new AjaxLink<Object>( "newAlbum" ) {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
 
                 target.addComponent( newAlbumForm );
             }
         } );
     }
 }
 
 
 /**
  * <h2>{@link GalleryTab}<br>
  * <sub>[in short] (TODO).</sub></h2>
  * 
  * <p>
  * [description / usage].
  * </p>
  * 
  * <p>
  * <i>May 31, 2009</i>
  * </p>
  * 
  * @author lhunath
  */
 class GalleryTab implements ITab {
 
     static final Logger logger = Logger.get( GalleryTab.class );
     Messages            msgs   = LocalizerFactory.getLocalizer( Messages.class );
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IModel<String> getTitle() {
 
         return new AbstractReadOnlyModel<String>() {
 
             @Override
             public String getObject() {
 
                 return msgs.galleryTab();
             }
         };
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Panel getPanel(String panelId) {
 
         return new GalleryTabPanel( panelId, new Model<User>() {
 
             @Override
             public User getObject() {
 
                 return SnaplogSession.get().getFocussedUser();
             }
 
             @Override
             public void setObject(User object) {
 
                 SnaplogSession.get().setFocussedUser( object );
             }
         } );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isVisible() {
 
         return SnaplogSession.get().getFocussedUser() != null;
     }
 }
