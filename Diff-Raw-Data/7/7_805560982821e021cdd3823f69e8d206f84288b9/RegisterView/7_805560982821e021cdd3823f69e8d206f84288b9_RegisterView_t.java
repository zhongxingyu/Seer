 package net.thesocialos.client.view;
 
 import net.thesocialos.client.TheSocialOS;
 import net.thesocialos.client.presenter.RegisterPresenter.Display;
 
 import com.google.gwt.core.client.GWT;
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
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.Label;
 
 public class RegisterView extends Composite implements Display {
 	
 	interface RegisterViewUiBinder extends UiBinder<HTMLPanel, RegisterView> {
 	}
 	
 	private static RegisterViewUiBinder uiBinder = GWT.create(RegisterViewUiBinder.class);
 	// @UiField Label lblIncorrect;
 	@UiField HTMLPanel html;
 	@UiField ImageElement background;
 	@UiField Label incorrect;
 	@UiField InputElement email;
 	@UiField InputElement password;
 	@UiField InputElement password2;
 	@UiField InputElement firstName;
 	@UiField InputElement secondName;
 	@UiField InputElement terms;
 	@UiField DivElement helpers;
 	@UiField DivElement footer;
 	@UiField TableCellElement flags;
 	Anchor spainFlag;
 	Anchor usaFlag;
 	
 	@UiField InputElement registerButton;
 	Timer timer = null;
 	
 	public RegisterView() {
 		initWidget(uiBinder.createAndBindUi(this));
 		/*
 		 * lblPass.setText(TheSocialOS.getConstants().password());
 		 * lblPass2.setText(TheSocialOS.getConstants().password2()); lblName.setText(TheSocialOS.getConstants().name());
 		 * lblLastName.setText(TheSocialOS.getConstants().lastName());
 		 * registerButton.setText(TheSocialOS.getConstants().register()); lblIncorrect.setVisible(false);
 		 */
 		registerButton.setValue(TheSocialOS.getConstants().register());
 		incorrect.setVisible(false);
 		
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
 		incorrect.getElement().getStyle().setTop(background.getHeight() - 180, Unit.PX);
 		firstName.getStyle().setTop(background.getHeight() - 150, Unit.PX);
 		secondName.getStyle().setTop(background.getHeight() - 150, Unit.PX);
 		email.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		password.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		password2.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		registerButton.getStyle().setTop(background.getHeight() - 100, Unit.PX);
 		terms.getStyle().setTop(background.getHeight() - 50, Unit.PX);
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
 	
 	@Override
 	public HasText getIncorrect() {
 		return null;
 	}
 	
 	@Override
 	public String getLastName() {
 		return secondName.getValue();
 	}
 	
 	@Override
 	public String getName() {
 		return firstName.getValue();
 	}
 	
 	@Override
 	public String getPassword() {
 		return password.getValue();
 	}
 	
 	@Override
 	public String getPassword2() {
 		return password2.getValue();
 	}
 	
 	@Override
 	public InputElement getRegisterButton() {
 		return registerButton;
 	}
 }
