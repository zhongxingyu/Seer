 package net.thesocialos.client.view;
 
 import net.thesocialos.client.TheSocialOS;
 import net.thesocialos.client.presenter.LoginPresenter.Display;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.AnchorElement;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.dom.client.InputElement;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.dom.client.TableCellElement;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class LoginView extends Composite implements Display {
 	
 	interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {
 	}
 	
 	private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);
 	
 	@UiField HTMLPanel html;
 	@UiField ImageElement background;
 	@UiField DivElement error;
 	@UiField InputElement email;
 	@UiField InputElement password;
 	@UiField InputElement loginButton;
 	@UiField DivElement helpers;
 	@UiField AnchorElement register;
 	@UiField AnchorElement forgotPass;
 	@UiField DivElement footer;
 	@UiField TableCellElement flags;
 	Anchor spainFlag;
 	Anchor usaFlag;
 	Timer timer = null;
 	
 	public LoginView() {
 		initWidget(uiBinder.createAndBindUi(this));
 		/*
 		 * lblPass.setText(TheSocialOS.getConstants().password());
 		 * keepLogged.setText(TheSocialOS.getConstants().keepLogged());
 		 * forgot.setText(TheSocialOS.getConstants().forgotPassword());
 		 * register.setText(TheSocialOS.getConstants().registerNow());
 		 * loginButton.setText(TheSocialOS.getConstants().login()); lblIncorrect.setVisible(false);
 		 */
 		
 		register.setInnerText(TheSocialOS.getConstants().registerNow());
 		forgotPass.setInnerText(TheSocialOS.getConstants().forgotPassword());
 		error.setInnerText(TheSocialOS.getConstants().error_login());
 		error.getStyle().setDisplay(com.google.gwt.dom.client.Style.Display.NONE);
 		
 		Window.addResizeHandler(new ResizeHandler() {
 			
 			@Override
 			public void onResize(ResizeEvent event) {
 				repositionElements();
 			}
 		});
 		
 		timer = new Timer() {
 			
 			@Override
 			public void run() {
 				repositionElements();
 			}
 		};
 		timer.schedule(10);
 		timer.schedule(50);
 		timer.scheduleRepeating(100);
 		
 		spainFlag = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
 				"<image class='flags spain' src='./images/flags/wes.gif'/>").toSafeHtml());
 		usaFlag = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
 				"<image class='flags spain' src='./images/flags/wus.gif'/>").toSafeHtml());
 		html.add(spainFlag, flags);
 		html.add(usaFlag, flags);
 	}
 	
 	protected void repositionElements() {
 		error.getStyle().setTop(background.getHeight() - 130, Unit.PX);
 		email.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		password.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		loginButton.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		helpers.getStyle().setTop(background.getHeight() - 50, Unit.PX);
 		footer.getStyle().setTop(background.getHeight() + 2, Unit.PX);
 		String top = footer.getStyle().getTop();
 		int iTop = Integer.parseInt(top.substring(0, top.indexOf("px")));
 		if (iTop > 100) timer.cancel();
 	}
 	
 	@Override
 	public String getEmail() {
 		return email.getValue();
 	}
 	
 	/*
 	 * @Override public boolean getKeepLoged() { return keepLogged.getValue(); }
 	 */
 	
 	@Override
 	public InputElement getLoginButton() {
 		return loginButton;
 	}
 	
 	@Override
 	public String getPassword() {
 		return password.getValue();
 	}
 	
 	@Override
 	public boolean getKeepLoged() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	
 	@Override
 	public Anchor getSpainFlag() {
 		return spainFlag;
 	}
 	
 	@Override
 	public Anchor getUsaFlag() {
 		return usaFlag;
 	}
 	
 	@Override
 	public DivElement getError() {
 		return error;
 	}
 }
