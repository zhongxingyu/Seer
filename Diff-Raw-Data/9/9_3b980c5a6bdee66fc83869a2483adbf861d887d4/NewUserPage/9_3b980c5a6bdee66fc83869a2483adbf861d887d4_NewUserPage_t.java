 /*
  *   Copyright 2010, Maarten Billemont
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
 package com.lyndir.lhunath.snaplog.webapp.page;
 
import static com.google.common.base.Preconditions.checkNotNull;

 import com.google.inject.Inject;
 import com.lyndir.lhunath.lib.wayward.component.GenericPanel;
 import com.lyndir.lhunath.lib.wayward.component.WicketUtils;
 import com.lyndir.lhunath.lib.wayward.state.PageState;
 import com.lyndir.lhunath.snaplog.data.user.LinkID;
 import com.lyndir.lhunath.snaplog.error.UsernameTakenException;
 import com.lyndir.lhunath.snaplog.model.UserService;
 import com.lyndir.lhunath.snaplog.webapp.SnaplogSession;
 import com.lyndir.lhunath.snaplog.webapp.page.model.NewUserPanelModels;
 import com.lyndir.lhunath.snaplog.webapp.page.model.NewUserPanelModels.NewUserFormModels;
 import net.link.safeonline.sdk.auth.filter.LoginManager;
 import net.link.safeonline.wicket.util.RedirectToPageException;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.Panel;
 
 
 /**
  * <h2>{@link NewUserPage}<br>
  * <sub>[in short] (TODO).</sub></h2>
  *
  * <p>
  * <i>Mar 17, 2010</i>
  * </p>
  *
  * @author lhunath
  */
 public class NewUserPage extends LayoutPage {
 
     @Inject
     UserService userService;
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Panel getInitialContentPanel(String wicketId) {
 
         return new NewUserPanel( wicketId );
     }
 
 
     /**
      * <h2>{@link NewUserPanel}<br>
      * <sub>Content panel for the {@link NewUserPage}.</sub></h2>
      *
      * <p>
      * <i>Mar 17, 2010</i>
      * </p>
      *
      * @author lhunath
      */
     public class NewUserPanel extends GenericPanel<NewUserPanelModels> {
 
         NewUserPanel(String id) {
 
             super( id, new NewUserPanelModels().getModel() );
 
             add( new Form<NewUserFormModels>( "newUserForm", getModelObject().newUserForm().getModel() ) {
 
                 {
                     add( new RequiredTextField<String>( "userName", getModelObject().userName() ) );
                 }
 
 
                 @Override
                 protected void onSubmit() {
 
                     String linkID = checkNotNull( LoginManager.findUserId( WicketUtils.getServletRequest() ), //
                                                   "LinkID identifier must not be null." );
 
                     try {
                         userService.registerUser( new LinkID( linkID ), getModelObject().userName().getObject() );
                     }
 
                     catch (UsernameTakenException e) {
                         error( e.getLocalizedMessage() );
                     }
                 }
             } );
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         protected void onBeforeRender() {
 
             if (!new NewUserPageState().isNecessary())
                 throw new RedirectToPageException( LayoutPage.class );
 
             super.onBeforeRender();
         }
     }
 
 
     /**
      * <h2>{@link NewUserPageState}<br>
      * <sub>[in short] (TODO).</sub></h2>
      *
      * <p>
      * <i>Mar 21, 2010</i>
      * </p>
      *
      * @author lhunath
      */
     public static class NewUserPageState extends PageState {
 
         /**
          * Create a new {@link NewUserPageState} instance.
          */
         public NewUserPageState() {
 
             super( NewUserPage.class );
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean isNecessary() {
 
             return LoginManager.isAuthenticated( WicketUtils.getServletRequest() )
                    && !SnaplogSession.get().isAuthenticated();
         }
     }
 }
