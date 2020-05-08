 package com.fountainhead.client.gin;
 
 import com.fountainhead.client.core.AdminGatekeeper;
 import com.fountainhead.client.core.IsAdminGatekeeper;
 import com.fountainhead.client.core.LoggedInGatekeeper;
 import com.fountainhead.client.core.MyConstants;
 import com.fountainhead.client.place.ClientPlaceManager;
 import com.fountainhead.client.place.DefaultPlace;
 import com.fountainhead.client.place.NameTokens;
 import com.fountainhead.client.presenter.AdminAreaPresenter;
 import com.fountainhead.client.presenter.DialogSamplePresenter;
 import com.fountainhead.client.presenter.GlobalDialogPresenterWidget;
 import com.fountainhead.client.presenter.HomeInfoPresenter;
 import com.fountainhead.client.presenter.HomeNewsPresenter;
 import com.fountainhead.client.presenter.HomePresenter;
 import com.fountainhead.client.presenter.InfoPopupPresenterWidget;
 import com.fountainhead.client.presenter.LocalDialogPresenterWidget;
 import com.fountainhead.client.presenter.MainPagePresenter;
 import com.fountainhead.client.presenter.ReportPresenter;
 import com.fountainhead.client.presenter.SignInPagePresenter;
 import com.fountainhead.client.view.AdminAreaView;
 import com.fountainhead.client.view.DialogSampleView;
 import com.fountainhead.client.view.GlobalDialogView;
 import com.fountainhead.client.view.HomeInfoView;
 import com.fountainhead.client.view.HomeNewsView;
 import com.fountainhead.client.view.HomeView;
 import com.fountainhead.client.view.InfoPopupView;
 import com.fountainhead.client.view.LocalDialogView;
 import com.fountainhead.client.view.MainPageView;
 import com.fountainhead.client.view.ReportView;
 import com.fountainhead.client.view.SignInPageView;
 import com.fountainhead.shared.CurrentUser;
 import com.google.inject.Singleton;
 import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
 import com.gwtplatform.mvp.client.gin.DefaultModule;
 
 public class ClientModule extends AbstractPresenterModule {
 
 	@Override
 	protected void configure() {
 		install(new DefaultModule(ClientPlaceManager.class));
 
 		bindPresenter(MainPagePresenter.class, MainPagePresenter.MyView.class,
 				MainPageView.class, MainPagePresenter.MyProxy.class);
 
 		bind(MyConstants.class).in(Singleton.class);
 		bind(CurrentUser.class).in(Singleton.class);
 		bind(IsAdminGatekeeper.class).in(Singleton.class);
 
 		// Constants
 		bind(LoggedInGatekeeper.class).in(Singleton.class);
 		bind(AdminGatekeeper.class).in(Singleton.class);
 
 		// Constants
 		// Bind the Sign In page to the default place
 		bindConstant().annotatedWith(DefaultPlace.class).to(
 				NameTokens.signInPage);
 
 		// Presenters
 
 		// Presenters
 
 		// Sign In
 		bindPresenter(SignInPagePresenter.class,
 				SignInPagePresenter.MyView.class, SignInPageView.class,
 				SignInPagePresenter.MyProxy.class);
 		// Presenters
 		// bindPresenter(MainPagePresenter.class,
 		// MainPagePresenter.MyView.class,
 		// MainPageView.class, MainPagePresenter.MyProxy.class);
 		bindPresenter(HomePresenter.class, HomePresenter.MyView.class,
 				HomeView.class, HomePresenter.MyProxy.class);
 		bindPresenter(DialogSamplePresenter.class,
 				DialogSamplePresenter.MyView.class, DialogSampleView.class,
 				DialogSamplePresenter.MyProxy.class);
 		bindPresenter(ReportPresenter.class, ReportPresenter.MyView.class,
 				ReportView.class, ReportPresenter.MyProxy.class);
 		bindPresenter(AdminAreaPresenter.class,
 				AdminAreaPresenter.MyView.class, AdminAreaView.class,
 				AdminAreaPresenter.MyProxy.class);
 
 		bindPresenter(HomeNewsPresenter.class, HomeNewsPresenter.MyView.class,
 				HomeNewsView.class, HomeNewsPresenter.MyProxy.class);
 		bindPresenter(HomeInfoPresenter.class, HomeInfoPresenter.MyView.class,
 				HomeInfoView.class, HomeInfoPresenter.MyProxy.class);
 		bindSingletonPresenterWidget(InfoPopupPresenterWidget.class,
 				InfoPopupPresenterWidget.MyView.class, InfoPopupView.class);
 		bindSingletonPresenterWidget(LocalDialogPresenterWidget.class,
 				LocalDialogPresenterWidget.MyView.class, LocalDialogView.class);
 		bindSingletonPresenterWidget(GlobalDialogPresenterWidget.class,
 				GlobalDialogPresenterWidget.MyView.class,
 				GlobalDialogView.class);
 
 	}
 }
