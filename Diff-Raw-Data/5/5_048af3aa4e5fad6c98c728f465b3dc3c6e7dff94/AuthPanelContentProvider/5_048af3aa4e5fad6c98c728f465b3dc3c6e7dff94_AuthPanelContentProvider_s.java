 package tmitter.ui;
 
 
 import javax.servlet.http.Cookie;
 
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.rwt.RWT;
 import org.eclipse.rwt.graphics.Graphics;
 import org.eclipse.rwt.lifecycle.WidgetUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import tmitter.model.Monster;
 import tmitter.model.MonsterUtil;
 
 import com.codeaffine.example.rwt.osgi.ui.platform.PageService;
 import com.codeaffine.example.rwt.osgi.ui.platform.ServiceProvider;
 import com.codeaffine.example.rwt.osgi.ui.platform.UIContributor;
 
 
 public class AuthPanelContentProvider implements UIContributor {
 
   public static final String AUTH_CONTROL = AuthPanelContentProvider.class.getName() + "#AUTH";
   private static final String AUTH_COOKIE = "tmitter-auth";
   static final String LOGIN_VARIANT = "login";
   private static final String CREDENTIAL_IMAGE = "icons/status-fail.png";
   private Text userNameText;
   private Text passwordText;
   private Composite container;
   private final ServiceProvider serviceProvider;
   private Control control;
   private Monster currentMonster;
 
   public AuthPanelContentProvider( ServiceProvider serviceProvider ) {
     this.serviceProvider = serviceProvider;
   }
 
   @Override
   public String getId() {
     return AUTH_CONTROL;
   }
 
   @Override
   public Control contribute( Composite parent ) {
     container = new Composite( parent, SWT.INHERIT_NONE );
     container.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     container.setLayout( new FillLayout() );
     control = createControls( true );
     return container;
   }
 
   public Control createControls( boolean checkCookie ) {
     Control result = null;
     boolean hasCookie = checkCookie ? hasAuthCookie() : false;
     if( hasCookie || currentMonster != null ) {
       String username = currentMonster != null ? currentMonster.getName() : getUserNameFromCookie();
       login( username );
       result = createSignedInControls( username );
     } else {
       result = createSignInControls();
     }
     return result;
   }
 
   private boolean hasAuthCookie() {
     boolean result = false;
     Cookie[] cookies = RWT.getRequest().getCookies();
     if( cookies != null ) {
       for( Cookie cookie : cookies ) {
         if( cookie.getName().equals( AUTH_COOKIE ) ) {
           if( cookie.getValue() != null ) {
             result = true;
           }
         }
       }
     }
     return result;
   }
 
   private String getUserNameFromCookie() {
     String result = null;
     Cookie[] cookies = RWT.getRequest().getCookies();
     for( Cookie cookie : cookies ) {
       if( cookie.getName().equals( AUTH_COOKIE ) ) {
        result = cookie.getValue();
       }
     }
     return result;
   }
 
   private Control createSignedInControls( String username ) {
     Composite result = new Composite( container, SWT.NONE );
     result.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     result.setLayout( new FormLayout() );
     Label imageLabel = new Label( result, SWT.NONE );
     imageLabel.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     Label nameLabel = createNameLabel( username, result );
     layoutSignoutControls( result, imageLabel, nameLabel );
     return result;
   }
 
   private Label createNameLabel( String username, Composite result ) {
     Label nameLabel = new Label( result, SWT.NONE );
     nameLabel.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     nameLabel.setText( "Signed in as " + username + "." );
     return nameLabel;
   }
 
   private Button createSignoutButton( Composite result ) {
     Button signOutButton = new Button( result, SWT.PUSH );
     signOutButton.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     signOutButton.setText( "Sign Out" );
     signOutButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void widgetSelected( SelectionEvent e ) {
         signOut();
       }
     } );
     return signOutButton;
   }
 
   private void layoutSignoutControls( Composite result,
                                       Label imageLabel,
                                       Label nameLabel )
   {
     Image image = ImageUtil.createSmallMonsterImage( currentMonster );
     imageLabel.setImage( image );
     Button signOutButton = createSignoutButton( result );
     FormData signOutButtonData = new FormData();
     signOutButton.setLayoutData( signOutButtonData );
     signOutButtonData.right = new FormAttachment( 100, -5 );
     signOutButtonData.top = new FormAttachment( 0, 5 );
     FormData nameLabelData = new FormData();
     nameLabel.setLayoutData( nameLabelData );
     nameLabelData.right = new FormAttachment( signOutButton, -5 );
     nameLabelData.top = new FormAttachment( 0, 6 );
     FormData imageLabelData = new FormData();
     imageLabel.setLayoutData( imageLabelData );
     imageLabelData.right = new FormAttachment( nameLabel, -5 );
     imageLabelData.top = new FormAttachment( 0, 2 );
     imageLabelData.width = image.getBounds().width;
     imageLabelData.height = image.getBounds().height;
   }
 
   protected void signOut() {
     currentMonster = null;
     Cookie cookie = new Cookie( AUTH_COOKIE, null );
     RWT.getResponse().addCookie( cookie );
     if( control != null && !control.isDisposed() ) {
       control.dispose();
     }
     control = null;
     MenuBarProvider menuBarProvider = serviceProvider.get( MenuBarProvider.class );
     menuBarProvider.hideMenuEntries();
     control = createControls( false );
     container.layout( true, true );
     PageService pageService = serviceProvider.get( PageService.class );
     pageService.selectHomePage();
     if( hasHomePage( pageService ) ) {
       pageService.selectPage( PublicTimeLineTab.ID );
     }
   }
 
   private boolean hasHomePage( PageService pageService ) {
     boolean result = false;
     String[] pageIds = pageService.getPageIds();
     for( String id : pageIds ) {
       if( id.equals( PublicTimeLineTab.ID ) ) {
         result = true;
       }
     }
     return result;
   }
 
   private Control createSignInControls( ) {
     final Composite result = new Composite( container, SWT.NONE );
     result.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     result.setLayout( new FormLayout() );
     Button loginButton = createLoginButton( result );
     Button signupButton = createSignupButton( result );
     FormData loginbuttonData = new FormData();
     loginButton.setLayoutData( loginbuttonData );
     loginbuttonData.right = new FormAttachment( signupButton, -5 );
     loginbuttonData.top = new FormAttachment( 0, 5 );
     return result;
   }
 
   private Button createLoginButton( final Composite result ) {
     Button loginButton = new Button( result, SWT.PUSH );
     loginButton.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     loginButton.setText( "Sign In" );
     loginButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void widgetSelected( SelectionEvent e ) {
         openLoginDialog( container.getShell() );
       }
     } );
     return loginButton;
   }
 
   private Button createSignupButton( final Composite result ) {
     Button signupButton = new Button( result, SWT.PUSH );
     signupButton.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     signupButton.setText( "Register Today" );
     signupButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void widgetSelected( SelectionEvent e ) {
         RegistrationPanel registrationPanel = new RegistrationPanel();
         registrationPanel.open( result.getShell() );
       }
     } );
     FormData signupButtonData = new FormData();
     signupButton.setLayoutData( signupButtonData );
     signupButtonData.right = new FormAttachment( 100, -5 );
     signupButtonData.top = new FormAttachment( 0, 5 );
     return signupButton;
   }
 
   protected void openLoginDialog( Shell shell ) {
     Shell loginShell = new Shell( shell, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM );
     loginShell.setLayout( new GridLayout( 1, true ) );
     Composite loginParent = new Composite( loginShell, SWT.NONE );
     loginParent.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     GridData loginParentData = new GridData( SWT.CENTER, SWT.CENTER, true, true );
     loginParentData.widthHint = 250;
     loginParent.setLayoutData( loginParentData );
     GridLayout layout = new GridLayout( 2, false );
     layout.marginWidth = 15;
     layout.marginHeight = 15;
     loginParent.setLayout( layout );
     createLoginInputControls( loginParent, loginShell );
     createLoginButton( loginParent, loginShell );
     configureShell( loginShell );
     userNameText.setFocus();
     loginShell.pack();
     loginShell.open();
   }
 
   private void configureShell( Shell loginShell ) {
     loginShell.setText( "Sign in" );
     loginShell.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     loginShell.setSize( 250, 130 );
     Rectangle bounds = loginShell.getDisplay().getBounds();
     Rectangle rect = loginShell.getBounds();
     int x = bounds.x + ( bounds.width - rect.width ) / 2;
     int y = bounds.y + ( bounds.height - rect.height ) / 2;
     loginShell.setLocation( x, y );
   }
 
   private void createLoginInputControls( Composite loginParent, final Shell loginShell ) {
     Label userNameLabel = new Label( loginParent, SWT.NONE );
     userNameLabel.setText( "username" );
     userNameLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );
     userNameLabel.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     userNameText = new Text( loginParent, SWT.NONE );
     userNameText.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     userNameText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
     Label passwordLabel = new Label( loginParent, SWT.NONE );
     passwordLabel.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     passwordLabel.setText( "password" );
     passwordText = new Text( loginParent, SWT.PASSWORD );
     passwordText.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     passwordText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
     passwordText.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void widgetDefaultSelected( SelectionEvent e ) {
         handleLogin();
         if( currentMonster != null ) {
           loginShell.close();
         }
       }
     } );
   }
 
   private void createLoginButton( Composite loginParent, final Shell loginShell ) {
     final Button loginButton = new Button( loginParent, SWT.PUSH );
     loginButton.setData( WidgetUtil.CUSTOM_VARIANT, LOGIN_VARIANT );
     GridData buttonLayoutData = new GridData( SWT.RIGHT, SWT.CENTER, false, false );
     buttonLayoutData.horizontalSpan = 2;
     loginButton.setLayoutData( buttonLayoutData );
     loginButton.setText( "Login");
     loginButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void widgetSelected( SelectionEvent e ) {
         handleLogin();
         if( currentMonster != null ) {
           loginShell.close();
         }
       }
     } );
   }
 
   void handleLogin( ) {
     String userName = userNameText.getText();
     if( isCorrectUsername( userName ) && isCorrectPassword( userName ) ) {
       login( userName );
       updateUI();
     }
   }
 
   private boolean isCorrectUsername( String userName ) {
     boolean result = MonsterUtil.isRegistered( userName );
     if( !result ) {
       ControlDecoration decorator = new ControlDecoration( userNameText, SWT.RIGHT );
       decorator.setImage( Graphics.getImage( CREDENTIAL_IMAGE, getClass().getClassLoader() ) );
       decorator.setDescriptionText( "username does not exist" );
     }
     return result;
   }
 
   private boolean isCorrectPassword( String userName ) {
     boolean result;
     try {
       result = MonsterUtil.loadMonster( userName ).getPassword().equals( passwordText.getText() );
     } catch (IllegalArgumentException e) {
       result = false;
     }
     if( !result ){
       ControlDecoration decorator = new ControlDecoration( passwordText, SWT.RIGHT );
       decorator.setImage( Graphics.getImage( CREDENTIAL_IMAGE, getClass().getClassLoader() ) );
       decorator.setDescriptionText( "password not correct" );
       result = false;
     }
     return result;
   }
 
   private void login( String userName ) {
     currentMonster = MonsterUtil.loadMonster( userName );
     serviceProvider.register( Monster.class, currentMonster );
     if( userName != null && !userName.equals( "" ) ) {
       Cookie cookie = new Cookie( AUTH_COOKIE, userName );
       RWT.getResponse().addCookie( cookie );
     }
   }
 
   private void updateUI() {
     control.dispose();
     control = null;
     MenuBarProvider menuBarProvider = serviceProvider.get( MenuBarProvider.class );
     menuBarProvider.flushPageQueue();
     control = createControls( false );
     container.layout( true, true );
   }
 
 }
