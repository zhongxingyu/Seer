 package org.softwareFm.collections.explorer.internal;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.softwareFm.card.card.composites.TextInBorder;
 import org.softwareFm.card.configuration.CardConfig;
 import org.softwareFm.card.configuration.ICardConfigurator;
 import org.softwareFm.card.constants.CardConstants;
 import org.softwareFm.collections.mySoftwareFm.IChangePassword;
 import org.softwareFm.collections.mySoftwareFm.IChangePasswordCallback;
 import org.softwareFm.collections.mySoftwareFm.IForgotPassword;
 import org.softwareFm.collections.mySoftwareFm.IForgotPasswordCallback;
 import org.softwareFm.collections.mySoftwareFm.ILogin;
 import org.softwareFm.collections.mySoftwareFm.ILoginCallback;
 import org.softwareFm.collections.mySoftwareFm.ILoginDisplayStrategy;
 import org.softwareFm.collections.mySoftwareFm.ILoginStrategy;
 import org.softwareFm.collections.mySoftwareFm.IRequestSaltCallback;
 import org.softwareFm.collections.mySoftwareFm.ISignUp;
 import org.softwareFm.collections.mySoftwareFm.ISignUpCallback;
 import org.softwareFm.display.composites.IHasComposite;
 import org.softwareFm.display.swt.Swts;
 import org.softwareFm.httpClient.api.IClientBuilder;
 import org.softwareFm.httpClient.api.IHttpClient;
 import org.softwareFm.server.IGitServer;
 import org.softwareFm.server.ISoftwareFmServer;
 import org.softwareFm.server.ServerConstants;
 import org.softwareFm.server.processors.AbstractLoginDataAccessor;
 import org.softwareFm.server.processors.IProcessCall;
 import org.softwareFm.utilities.arrays.ArrayHelper;
 import org.softwareFm.utilities.callbacks.ICallback;
 import org.softwareFm.utilities.functions.IFunction1;
 import org.softwareFm.utilities.services.IServiceExecutor;
 
 public class MySoftwareFm implements IHasComposite, ILoginDisplayStrategy {
 
 	private final Composite content;
 	private final ILoginStrategy loginStrategy;
 	private final CardConfig cardConfig;
 	protected String email;
 	protected String crypto;
 	protected String signupSalt;
 
 	protected ICallback<String> restart;
 
 	public MySoftwareFm(Composite parent, CardConfig cardConfig, ILoginStrategy loginStrategy) {
 		this.cardConfig = cardConfig;
 		this.loginStrategy = loginStrategy;
 		this.content = new Composite(parent, SWT.NULL);
 		content.setLayout(new FillLayout());
 		restart = new ICallback<String>() {
 			@Override
 			public void process(String email) throws Exception {
 				logout();
 				start(email);
 			}
 		};
 	}
 
 	public void start() {
 		start(email);
 	}
 
 	public void start(final String email) {
 		if (crypto != null)
 			showLoggedIn();
 		else
			display(CardConstants.loginCardType, CardConstants.contactingServerTitle, CardConstants.contactingServerText, email);
 		loginStrategy.requestSessionSalt(new IRequestSaltCallback() {
 			@Override
 			public void saltReceived(String salt) {
 				MySoftwareFm.this.signupSalt = salt;
 				showLogin(salt, email);
 			}
 
 			@Override
 			public void problemGettingSalt(String message) {
 				displayWithClickBackToStart(CardConstants.loginCardType, CardConstants.failedToContactServerTitle, CardConstants.failedToContactServerText, email, message);
 			}
 		});
 	}
 
 	@SuppressWarnings("unused")
 	public void showLoggedIn() {
 		Swts.removeAllChildren(content);
 		IChangePasswordCallback callback = null;
 		new MySoftwareFmLoggedIn(content, cardConfig, CardConstants.loggedInTitle, CardConstants.loggedInText, email, this, new Runnable() {
 			@Override
 			public void run() {
 				logout();
 				start();
 			}
 		}, callback);
 		content.layout();
 	}
 
 	@Override
 	public Control getControl() {
 		return content;
 	}
 
 	@Override
 	public Composite getComposite() {
 		return content;
 	}
 
 	public void display(final String cardType, final String title, final String text, final String... args) {
 		content.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				Swts.removeAllChildren(content);
 				TextInBorder textInBorder = new TextInBorder(content, SWT.WRAP | SWT.READ_ONLY, cardConfig);
 				textInBorder.setTextFromResourceGetter(cardType, title, text, (Object[]) args);
 				content.layout();
 			}
 		});
 	}
 
 	public void displayWithClickBackToStart(final String cardType, final String title, final String text, final String email, final String... args) {
 		displayWithClick(cardType, title, text, email, new Runnable() {
 			@Override
 			public void run() {
 				ICallback.Utils.call(restart, email);
 			}
 		}, ArrayHelper.insert(args, email));
 	}
 
 	public void displayWithClick(final String cardType, final String title, final String text, final String email, final Runnable runnable, final String... args) {
 		content.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				Swts.removeAllChildren(content);
 				TextInBorder textInBorder = new TextInBorder(content, SWT.WRAP | SWT.READ_ONLY, cardConfig);
 				textInBorder.setTextFromResourceGetter(cardType, title, text, (Object[]) args);
 				textInBorder.addClickedListener(runnable);
 				content.layout();
 			}
 		});
 	}
 
 	@Override
 	public void showLogin(String sessionSalt, String initialEmail) {
 		Swts.removeAllChildren(content);
 		ILogin.Utils.login(content, cardConfig, sessionSalt, initialEmail, loginStrategy, MySoftwareFm.this, new ILoginCallback() {
 			@Override
 			public void loggedIn(String email, String cryptoKey) {
 				MySoftwareFm.this.email = email;
 				MySoftwareFm.this.crypto = cryptoKey;
 				showLoggedIn();
 			}
 
 			@Override
 			public void failedToLogin(String email, String message) {
 				displayWithClickBackToStart(CardConstants.loginCardType, CardConstants.failedToLoginTitle, CardConstants.failedToLoginText, email, message);
 			}
 		});
 		content.layout();
 	}
 
 	@Override
 	public void showForgotPassword(String sessionSalt, String initialEmail) {
 		Swts.removeAllChildren(content);
 		IForgotPassword.Utils.forgotPassword(content, cardConfig, sessionSalt, initialEmail, loginStrategy, this, new IForgotPasswordCallback() {
 			@Override
 			public void emailSent(String email) {
 				displayWithClickBackToStart(CardConstants.forgotPasswordCardType, CardConstants.sentForgottenPasswordTitle, CardConstants.sentForgottenPasswordText, email);
 			}
 
 			@Override
 			public void failedToSend(String email, String message) {
 				displayWithClickBackToStart(CardConstants.forgotPasswordCardType, CardConstants.failedToSendForgottenPasswordTitle, CardConstants.failedToSendForgottenPasswordText, email, message);
 			}
 		});
 		content.layout();
 	}
 
 	@Override
 	public void showSignup(String sessionSalt, String initialEmail) {
 		Swts.removeAllChildren(content);
 		ISignUp.Utils.signUp(content, cardConfig, sessionSalt, initialEmail, loginStrategy, this, new ISignUpCallback() {
 			@Override
 			public void signedUp(String email, String crypto) {
 				MySoftwareFm.this.email = email;
 				MySoftwareFm.this.crypto = crypto;
 				showLoggedIn();
 			}
 
 			@Override
 			public void failed(String email, String message) {
 				displayWithClickBackToStart(CardConstants.signupCardType, CardConstants.failedToSignupTitle, CardConstants.failedToSignupText, email, message);
 			}
 		});
 		content.layout();
 	}
 
 	@Override
 	public void showChangePassword(final String email) {
 		Swts.removeAllChildren(content);
 		final String cardType = CardConstants.changePasswordCardType;
 		final Runnable showLoggedIn = new Runnable() {
 			@Override
 			public void run() {
 				showLoggedIn();
 			}
 		};
 		IChangePassword.Utils.changePassword(content, cardConfig, email, loginStrategy, this, new IChangePasswordCallback() {
 			@Override
 			public void changedPassword(String email) {
 				displayWithClick(cardType, CardConstants.changedPasswordTitle, CardConstants.changedPasswordText, email, showLoggedIn);
 			}
 
 			@Override
 			public void failedToChangePassword(String email, String message) {
 				displayWithClick(cardType, CardConstants.failedToChangePasswordTitle, CardConstants.failedToChangePasswordText, email, showLoggedIn, message);
 			}
 
 			@Override
 			public void cancelChangePassword() {
 				showLoggedIn();
 			}
 		});
 		content.layout();
 
 	}
 
 	public static void main(String[] args) {
 		final IServiceExecutor service = IServiceExecutor.Utils.defaultExecutor();
 		final IClientBuilder client = IHttpClient.Utils.builder("localhost", ServerConstants.testPort);
 		IGitServer gitServer = IGitServer.Utils.noGitServer();
 		File home = new File(System.getProperty("user.home"));
 		final File localRoot = new File(home, ".sfm");
 		ISoftwareFmServer server = ISoftwareFmServer.Utils.testServerPort(IProcessCall.Utils.softwareFmProcessCallWithoutMail(AbstractLoginDataAccessor.defaultDataSource(), gitServer, localRoot), ICallback.Utils.rethrow());
 		try {
 			Swts.Show.display(MySoftwareFm.class.getSimpleName(), new IFunction1<Composite, Composite>() {
 				@Override
 				public Composite apply(Composite from) throws Exception {
 					CardConfig cardConfig = ICardConfigurator.Utils.cardConfigForTests(from.getDisplay());
 					MySoftwareFm mySoftwareFm = new MySoftwareFm(from, cardConfig, ILoginStrategy.Utils.softwareFmLoginStrategy(from.getDisplay(), service, client));
 					mySoftwareFm.start();
 					return mySoftwareFm.getComposite();
 				}
 			});
 		} finally {
 			server.shutdown();
 			client.shutdown();
 			service.shutdown();
 		}
 	}
 
 	public String getSignupSalt() {
 		return signupSalt;
 	}
 
 	public void logout() {
 		crypto = null;
 		signupSalt = null;
 		email = null;
 	}
 
 	public void forceFocus() {
 		Control[] children = content.getChildren();
 		if (children.length>0)
 			children[0].forceFocus();
 		
 	}
 
 }
