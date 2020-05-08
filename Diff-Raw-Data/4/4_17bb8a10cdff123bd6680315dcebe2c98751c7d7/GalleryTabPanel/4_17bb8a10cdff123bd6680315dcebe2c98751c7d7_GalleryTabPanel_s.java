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
 
import static com.google.common.base.Preconditions.checkNotNull;

 import com.google.common.collect.ImmutableList;
 import com.google.inject.Inject;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.system.util.ObjectUtils;
 import com.lyndir.lhunath.lib.wayward.component.GenericPanel;
 import com.lyndir.lhunath.lib.wayward.i18n.BooleanKeyAppender;
 import com.lyndir.lhunath.lib.wayward.i18n.MessagesFactory;
 import com.lyndir.lhunath.lib.wayward.navigation.AbstractFragmentState;
 import com.lyndir.lhunath.lib.wayward.navigation.FragmentNavigationTab;
 import com.lyndir.lhunath.snaplog.data.object.media.Album;
 import com.lyndir.lhunath.snaplog.data.object.media.AlbumProviderType;
 import com.lyndir.lhunath.snaplog.data.object.media.Media.Quality;
 import com.lyndir.lhunath.snaplog.data.object.security.Permission;
 import com.lyndir.lhunath.snaplog.data.object.user.User;
 import com.lyndir.lhunath.snaplog.error.PermissionDeniedException;
 import com.lyndir.lhunath.snaplog.model.service.AlbumProvider;
 import com.lyndir.lhunath.snaplog.model.service.AlbumService;
 import com.lyndir.lhunath.snaplog.model.service.SecurityService;
 import com.lyndir.lhunath.snaplog.model.service.UserService;
 import com.lyndir.lhunath.snaplog.webapp.SnaplogSession;
 import com.lyndir.lhunath.snaplog.webapp.listener.GuiceContext;
 import com.lyndir.lhunath.snaplog.webapp.tab.model.GalleryTabModels;
 import com.lyndir.lhunath.snaplog.webapp.tab.model.GalleryTabModels.NewAlbumFormModels;
 import com.lyndir.lhunath.snaplog.webapp.tool.SnaplogTool;
 import com.lyndir.lhunath.snaplog.webapp.view.AbstractAlbumsView;
 import com.lyndir.lhunath.snaplog.webapp.view.MediaView;
 import java.util.List;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.*;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 
 
 /**
  * <h2>{@link GalleryTabPanel}<br> <sub>[in short] (TODO).</sub></h2>
  *
  * <p> <i>Mar 1, 2010</i> </p>
  *
  * @author lhunath
  */
 public class GalleryTabPanel extends GenericPanel<GalleryTabModels> {
 
     static final Logger logger = Logger.get( GalleryTabPanel.class );
     static final Messages msgs = MessagesFactory.create( Messages.class );
 
     static final int ALBUMS_PER_PAGE = 5;
 
     @Inject
     UserService userService;
 
     @Inject
     AlbumService albumService;
 
     @Inject
     SecurityService securityService;
 
     AbstractAlbumsView albums;
 
     /**
      * Create a new {@link GalleryTabPanel} instance.
      *
      * @param id        The wicket ID that will hold the {@link GalleryTabPanel}.
      * @param userModel The user whose gallery to show.
      */
     public GalleryTabPanel(final String id, final IModel<User> userModel) {
 
         super( id, new GalleryTabModels( userModel ).getModel() );
        checkNotNull( userModel.getObject(), "Model object of GalleryTabPanel must not be null" );
 
         // Page info
         add( new Label( "albumsTitleUsername", getModelObject().decoratedUsername() ) );
         add( new Label( "anothersAlbumsHelp",
                         msgs.anothersAlbumsHelp( SnaplogSession.get().isAuthenticated(), getModelObject().username() ) ) {
 
             @Override
             public boolean isVisible() {
 
                 return albums.getItemCount() > 0 && //
                        !ObjectUtils.equal( getModelObject().getObject(), SnaplogSession.get().getActiveUser() );
             }
         } );
         add( new Label( "ownAlbumsHelp", msgs.ownAlbumsHelp() ) {
 
             @Override
             public boolean isVisible() {
 
                 return albums.getItemCount() > 0 && //
                        ObjectUtils.equal( getModelObject().getObject(), SnaplogSession.get().getActiveUser() );
             }
         } );
         add( new Label( "noAlbumsHelp", new LoadableDetachableModel<String>() {
 
             @Override
             protected String load() {
 
                 return msgs.noAlbumsHelp( SnaplogSession.get().isAuthenticated(),
                                           userService.hasProfileAccess( SnaplogSession.get().newToken(), getModelObject().getObject() ),
                                           getModelObject().username() );
             }
         } ) {
 
             @Override
             public boolean isVisible() {
 
                 return albums.getItemCount() == 0;
             }
         }.setEscapeModelStrings( false ) );
 
         // List of albums
         // TODO: Make this data view top-level to provide Album enumeration elsewhere.
         add( albums = new AbstractAlbumsView( "albums", getModelObject(), ALBUMS_PER_PAGE ) {
 
             @Override
             protected void populateItem(final Item<Album> item) {
 
                 item.add( new AjaxLink<Album>( "link", item.getModel() ) {
 
                     {
                         add( new MediaView( "cover", cover( getModel() ), Quality.THUMBNAIL, false ) );
                         add( new Label( "title", getModelObject().getName() ) );
                         // TODO: Fix HTML injection.
                         add( new Label( "description", getModelObject().getDescription() ).setEscapeModelStrings( false ) );
                     }
 
                     @Override
                     public void onClick(final AjaxRequestTarget target) {
 
                         Tab.ALBUM.activateWithState( new AlbumTabPanel.AlbumTabState( getModelObject() ) );
                     }
                 } );
             }
         } );
 
         // New album
         add( new WebMarkupContainer( "newAlbumContainer" ) {
 
             {
                 final WebMarkupContainer container = this;
                 final Form<NewAlbumFormModels> newAlbumForm = new Form<NewAlbumFormModels>( "newAlbumForm",
                                                                                             getModelObject().newAlbumForm().getModel() ) {
 
                     {
                         add( new DropDownChoice<AlbumProviderType>( "type", getModelObject().type(), getModelObject().types(),
                                                                     new EnumChoiceRenderer<AlbumProviderType>() ) //
                                 .setRequired( true ) );
 
                         add( new RequiredTextField<String>( "name", getModelObject().name() ) );
                         add( new TextArea<String>( "description", getModelObject().description() ) );
                     }
 
                     @Override
                     protected void onSubmit() {
 
                         AlbumProvider<?, ?> albumProvider = getModelObject().type().getObject().getAlbumProvider();
                         Album album = albumProvider.newAlbum( GalleryTabPanel.this.getModelObject().getObject(), //
                                                               getModelObject().name().getObject(), //
                                                               getModelObject().description().getObject() );
 
                         try {
                             albumService.registerAlbum( SnaplogSession.get().newToken(), album );
 
                             // New album was created; reset and hide ourselves again.
                             getModelObject().name().setObject( null );
                             getModelObject().description().setObject( null );
                             setVisible( false );
                         }
 
                         catch (PermissionDeniedException e) {
                             error( e.getLocalizedMessage() );
                         }
                     }
                 };
 
                 add( new AjaxLink<Object>( "newAlbum" ) {
 
                     @Override
                     public void onClick(final AjaxRequestTarget target) {
 
                         newAlbumForm.setVisible( !newAlbumForm.isVisible() );
 
                         target.addComponent( container );
                     }
 
                     @Override
                     public boolean isVisible() {
 
                         return !newAlbumForm.isVisible();
                     }
                 } );
                 add( newAlbumForm.setVisible( false ) );
             }
 
             @Override
             public boolean isVisible() {
 
                 try {
                     // Only show when session has CONTRIBUTE permission to the user's profile.
                     // (otherwise he won't be able to create a new album anyway)
                     return securityService.hasAccess( Permission.CONTRIBUTE, SnaplogSession.get().newToken(),
                                                       userService.getProfile( SnaplogSession.get().newToken(),
                                                                               getModelObject().getObject() ) );
                 }
 
                 catch (PermissionDeniedException ignored) {
                     return false;
                 }
             }
         }.setOutputMarkupPlaceholderTag( true ) );
     }
 
     interface Messages {
 
         /**
          * @return Text on the interface tab to activate the {@link GalleryTabPanel}.
          */
         String galleryTab();
 
         /**
          * @param authenticated <code>true</code>: The current user has authenticated himself.<br> <code>false</code>: The current user has
          *                      not identified himself.
          * @param username      The name of the user whose gallery is being viewed.
          *
          * @return A text that explains to whom the albums in the gallery belong.
          */
         String anothersAlbumsHelp(@BooleanKeyAppender(y = "auth", n = "anon") boolean authenticated, IModel<String> username);
 
         /**
          * @return A text that explains that the visible gallery belongs to the current user.
          */
         String ownAlbumsHelp();
 
         /**
          * @param isAuthenticated <code>true</code>: The current user has authenticated himself.<br> <code>false</code>: The current user
          *                        has not identified himself.
          * @param hasAccess       <code>true</code>: The current user has access to see the gallery or any of the albums.<br>
          *                        <code>false</code>: The current user has insufficient access to see the gallery or any of the albums.
          * @param username        The name of the user whose gallery is being viewed.
          *
          * @return A text that explains that none of the user's albums are visible and what might be the cause.
          */
         String noAlbumsHelp(@BooleanKeyAppender(y = "auth", n = "anon") boolean isAuthenticated,
                             @BooleanKeyAppender(y = "access", n = "noaccess") boolean hasAccess, IModel<String> username);
     }
 
 
     /**
      * <h2>{@link GalleryTab}<br> <sub>[in short] (TODO).</sub></h2>
      *
      * <p> [description / usage]. </p>
      *
      * <p> <i>May 31, 2009</i> </p>
      *
      * @author lhunath
      */
     static class GalleryTab implements SnaplogTab<GalleryTabPanel, GalleryTabState> {
 
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
         public GalleryTabPanel getPanel(final String panelId) {
 
             return new GalleryTabPanel( panelId, new IModel<User>() {
 
                 @Override
                 public void detach() {
 
                 }
 
                 @Override
                 public User getObject() {
 
                     return SnaplogSession.get().getFocusedUser();
                 }
 
                 @Override
                 public void setObject(final User object) {
 
                     SnaplogSession.get().setFocusedUser( object );
                 }
             } );
         }
 
         @Override
         public Class<GalleryTabPanel> getPanelClass() {
 
             return GalleryTabPanel.class;
         }
 
         @Override
         public GalleryTabState getState(final String fragment) {
 
             return new GalleryTabState( fragment );
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean isVisible() {
 
             return SnaplogSession.get().getFocusedUser() != null;
         }
 
         @Override
         public List<? extends SnaplogTool> listTools() {
 
             return ImmutableList.of();
         }
 
         @Override
         public String getTabFragment() {
 
             return "gallery";
         }
 
         @Override
         public GalleryTabState getFragmentState(final GalleryTabPanel panel) {
 
             return new GalleryTabState( SnaplogSession.get().getFocusedUser() );
         }
 
         @Override
         public void applyFragmentState(final GalleryTabPanel panel, final GalleryTabState state) {
 
             SnaplogSession.get().setFocusedUser( state.getUser() );
         }
     }
 
 
     public static class GalleryTabState extends AbstractFragmentState<GalleryTabPanel, GalleryTabState> {
 
         private static final GalleryTab TAB = new GalleryTab();
 
         private final UserService userService = GuiceContext.getInstance( UserService.class );
 
         final String userName;
 
         public GalleryTabState() {
 
             userName = null;
         }
 
         public GalleryTabState(final String fragment) {
 
             super( fragment );
 
             userName = findFragment( 1 );
         }
 
         public GalleryTabState(final User user) {
 
             // Load fields and fragments from parameter.
             appendFragment( userName = user.getUserName() );
         }
 
         public User getUser() {
 
             return userName == null? null: userService.findUserWithUserName( userName );
         }
 
         @Override
         public FragmentNavigationTab<GalleryTabPanel, GalleryTabState> getFragmentTab() {
 
             return TAB;
         }
     }
 }
