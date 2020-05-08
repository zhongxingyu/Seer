 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.ParlamentarianCommentServiceAsync;
 import cl.votainteligente.inspector.client.services.ParlamentarianServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.ParlamentarianCommentUiHandlers;
 import cl.votainteligente.inspector.model.Parlamentarian;
 import cl.votainteligente.inspector.model.ParlamentarianComment;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 
 import java.util.List;
 
 public class ParlamentarianCommentPresenter extends Presenter<ParlamentarianCommentPresenter.MyView, ParlamentarianCommentPresenter.MyProxy> implements ParlamentarianCommentUiHandlers {
 	public static final String PLACE = "parlamentarianComment";
 	public static final String PARAM_PARLAMENTARIAN_ID = "parlamentarianId";
 
 	public interface MyView extends View, HasUiHandlers<ParlamentarianCommentUiHandlers> {
 		Long getSelectedParlamentarianId();
 		void setSelectedParlamentarian(Long parlamentarianId);
 		void setupParlamentarianList();
 		void addParlamentarian(Parlamentarian parlamentarian);
 		String getCommentSubject();
 		String getCommentBody();
 		void clearForm();
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<ParlamentarianCommentPresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private ParlamentarianServiceAsync parlamentarianService;
 	@Inject
 	private ParlamentarianCommentServiceAsync parlamentarianCommentService;
 	private Long parlamentarianId;
 
 	@Inject
 	public ParlamentarianCommentPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 	}
 
 	@Override
 	protected void onReveal() {
 		getView().clearForm();
		if (parlamentarianId != null) {
			getView().setSelectedParlamentarian(parlamentarianId);
		}
 		getParlamentarianList();
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			parlamentarianId = Long.parseLong(placeRequest.getParameter(PARAM_PARLAMENTARIAN_ID, null));
 		} catch (NumberFormatException nfe) {
 			parlamentarianId = null;
 		}
 	}
 
 	public void getParlamentarianList() {
 		parlamentarianService.getAllParlamentarians(new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianList());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					getView().setupParlamentarianList();
 					for (Parlamentarian parlamentarian : result) {
 						getView().addParlamentarian(parlamentarian);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void saveParlamentarianComment() {
 		parlamentarianService.getParlamentarian(getView().getSelectedParlamentarianId(), new AsyncCallback<Parlamentarian>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarian());
 			}
 
 			@Override
 			public void onSuccess(Parlamentarian result) {
 				ParlamentarianComment parlamentarianComment = new ParlamentarianComment();
 				parlamentarianComment.setSubject(getView().getCommentSubject());
 				parlamentarianComment.setBody(getView().getCommentBody());
 
 				parlamentarianCommentService.saveParlamentarianComment(parlamentarianComment, result.getId(), new AsyncCallback<ParlamentarianComment>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(applicationMessages.getErrorParlamentarianCommentSave());
 					}
 
 					@Override
 					public void onSuccess(ParlamentarianComment result) {
 						Window.alert("Comentario guardado exitosamente");
 					}
 				});
 			}
 		});
 	}
 }
