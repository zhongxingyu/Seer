 package au.org.scoutmaster.application;
 
 import java.util.ArrayList;
 
 import org.vaadin.dialogs.ConfirmDialog;
 import org.vaadin.dialogs.DefaultConfirmDialogFactory;
 
 import au.com.vaadinutils.menu.MenuBuilder;
 import au.com.vaadinutils.menu.ViewMap;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.dao.access.UserDao;
 import au.org.scoutmaster.domain.converter.ScoutmasterConverterFactory;
 import au.org.scoutmaster.views.ActivityView;
 import au.org.scoutmaster.views.ContactView;
 import au.org.scoutmaster.views.ForgottenPasswordView;
 import au.org.scoutmaster.views.LoginView;
 import au.org.scoutmaster.views.ResetPasswordView;
 import au.org.scoutmaster.views.SectionBulkEmailWizard;
 import au.org.scoutmaster.views.TagView;
 import au.org.scoutmaster.views.UserView;
 import au.org.scoutmaster.views.wizards.importer.ImportWizardView;
 import au.org.scoutmaster.views.wizards.mailing.MailingWizardView;
 import au.org.scoutmaster.views.wizards.messaging.MessagingWizardView;
 import au.org.scoutmaster.views.wizards.setup.SetupWizardView;
 
 import com.vaadin.annotations.Push;
 import com.vaadin.annotations.Title;
 import com.vaadin.annotations.Widgetset;
 import com.vaadin.navigator.Navigator;
 import com.vaadin.navigator.ViewChangeListener;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.server.VaadinSession;
 import com.vaadin.ui.MenuBar;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * Check this out.
  * http://www.aceevo.com/reusable-patterns-for-vaadin-custom-form-layout/
  * 
  * @author bsutton
  * 
  */
 /*
  * UI class is the starting point for your app. You may deploy it with
  * VaadinServlet or VaadinPortlet by giving your UI class name a parameter. When
  * you browse to your app a web page showing your UI is automatically generated.
  * Or you may choose to embed your UI to an existing web page.
  */
 @Title("Scoutmaster")
 // @PreserveOnRefresh
 // @Theme("scoutmaster")
 @Push
 @Widgetset(value = "au.org.scoutmaster.AppWidgetSet")
 public class NavigatorUI extends UI
 {
 	private static final long serialVersionUID = 1L;
 
 	private MenuBar menubar;
 
 	private ArrayList<ViewMap> viewMap = new ArrayList<>();
 
 	private VerticalLayout mainLayout;
 
 	/*
 	 * After UI class is created, init() is executed. You should build and wire
 	 * up your user interface here.
 	 */
 	protected void init(VaadinRequest request)
 	{
 		VaadinSession.getCurrent().setConverterFactory(new ScoutmasterConverterFactory());
 		styleConfirmDialog();
 
 		viewMap.add(new ViewMap("", ContactView.class));
 		viewMap.add(new ViewMap(ContactView.NAME, ContactView.class));
 		// viewMap.add(new ViewMap(AppointmentView.NAME,
 		// AppointmentView.class));
 		viewMap.add(new ViewMap(ImportWizardView.NAME, ImportWizardView.class));
 		viewMap.add(new ViewMap(LoginView.NAME, LoginView.class));
 		viewMap.add(new ViewMap(ForgottenPasswordView.NAME, ForgottenPasswordView.class));
 		viewMap.add(new ViewMap(ResetPasswordView.NAME, ResetPasswordView.class));
 		viewMap.add(new ViewMap(MessagingWizardView.NAME, MessagingWizardView.class));
 		viewMap.add(new ViewMap(MailingWizardView.NAME, MailingWizardView.class));
 		viewMap.add(new ViewMap(SetupWizardView.NAME, SetupWizardView.class));
 		viewMap.add(new ViewMap(ActivityView.NAME, ActivityView.class));
 		viewMap.add(new ViewMap(TagView.NAME, TagView.class));
 		viewMap.add(new ViewMap(UserView.NAME, UserView.class));
		
 
 		mainLayout = new VerticalLayout();
 		mainLayout.setMargin(false);
 		mainLayout.setSpacing(true);
 		mainLayout.setSizeFull();
 
 		VerticalLayout viewContainer = new VerticalLayout();
 		viewContainer.setHeight("100%");
 
 		final Navigator navigator = new Navigator(this, viewContainer);
 
 		// Wire up the navigation
 		for (final ViewMap viewmap : this.viewMap)
 		{
 			navigator.addView(viewmap.getViewName(), viewmap.getView());
 		}
 
 		mainLayout.addComponent(viewContainer);
 		mainLayout.setExpandRatio(viewContainer, 1.0f);
 
 		this.setContent(mainLayout);
 
 		//
 		// We use a view change handler to ensure the user is always redirected
 		// to the login view if the user is not logged in.
 		//
 		getNavigator().addViewChangeListener(new ViewChangeListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean beforeViewChange(ViewChangeEvent event)
 			{
 
 				// Check if a user has logged in
 				boolean isLoggedIn = getSession().getAttribute("user") != null;
 				boolean isLoginView = event.getNewView() instanceof LoginView;
 				boolean isForgottenPasswordView = event.getNewView() instanceof ForgottenPasswordView;
 
 				// TODO: should we cache this?
 				// during dev its easier if we don't.
 				UserDao daoUser = new DaoFactory().getUserDao();
 				long userCount = daoUser.getCount();
 
 				if (userCount == 0)
 				{
 					// Deal with recursion. When we navigateTo the setupwizard it comes back through
 					// beforeViewChange so we have to check that aren't aready on our way to the
 					// setupview.
 					if (event.getNewView() instanceof SetupWizardView)
 						return true;
 					else
 					{
 						// Must be a first time login so lets go and run the
 						// setup wizard.
 						getNavigator().navigateTo(SetupWizardView.NAME);
 						return false;
 					}
 				}
 
 				if (!isLoggedIn && !isLoginView && !isForgottenPasswordView)
 				{
 					// Redirect to login view always if a user has not yet
 					// logged in
 					getNavigator().navigateTo(LoginView.NAME);
 					return false;
 
 				}
 				else if (isForgottenPasswordView)
 				{
 					return true;
 				}
 				else if (isLoggedIn && isLoginView)
 				{
 					// If someone tries to access to login view while logged in,
 					// then cancel
 					return false;
 				}
 				else if (isLoggedIn)
 				{
 					// check if the menu bar has been added and if not then add
 					// it.
 					if (NavigatorUI.this.menubar == null)
 					{
 						NavigatorUI.this.menubar = new MenuBuilder(navigator, viewMap).build();
 						NavigatorUI.this.menubar.setWidth("100%");
 						mainLayout.addComponentAsFirst(menubar);
 					}
 				}
 
 				return true;
 			}
 
 			@Override
 			public void afterViewChange(ViewChangeEvent event)
 			{
 				// For some reason the page title is set to null after each
 				// navigation transition.
 				getPage().setTitle("Scoutmaster");
 			}
 		});
 
 	}
 
 	void styleConfirmDialog()
 	{
 		ConfirmDialog.Factory df = new DefaultConfirmDialogFactory()
 		{
 			private static final long serialVersionUID = 1L;
 
 			// We change the default order of the buttons
 			@Override
 			public ConfirmDialog create(String caption, String message, String okCaption, String cancelCaption)
 			{
 				ConfirmDialog d = super.create(caption, message, okCaption, cancelCaption);
 				d.setStyleName("black");
 				d.setModal(true);
 
 				return d;
 			}
 
 		};
 		ConfirmDialog.setFactory(df);
 
 	}
 
 }
