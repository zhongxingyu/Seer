 package org.accesointeligente.client.presenters;
 
 import org.accesointeligente.client.AppController;
 import org.accesointeligente.client.ClientSessionUtil;
 import org.accesointeligente.client.services.RPC;
 import org.accesointeligente.model.*;
 import org.accesointeligente.shared.*;
 
 import net.customware.gwt.presenter.client.EventBus;
 import net.customware.gwt.presenter.client.widget.WidgetDisplay;
 import net.customware.gwt.presenter.client.widget.WidgetPresenter;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 
 import java.util.Date;
 import java.util.List;
 
 public class RequestResponsePresenter extends WidgetPresenter<RequestResponsePresenter.Display> implements RequestResponsePresenterIface {
 	public interface Display extends WidgetDisplay {
 		void setPresenter(RequestResponsePresenterIface presenter);
 		// Request
 		void setStatus(RequestStatus status);
 		void setRequestTitle(String title);
 		void setRequestDate(Date date);
 		void setResponseDate(Date date);
 		void setInstitutionName(String name);
 		void setRequestInfo(String info);
 		void setRequestContext(String context);
 		// Response
 		void setResponseInfo(String info);
 		void setResponseAttachments(ListDataProvider<Attachment> data);
 		void initTable();
 		void initTableColumns();
 		void setComments(List<RequestComment> comments);
 		void showNewCommentPanel();
 		void cleanNewCommentText();
 		void setRatingValue(Integer rate);
 		void setRatingReadOnly(Boolean readOnly);
 	}
 
 	private Request request;
 
 	public RequestResponsePresenter(Display display, EventBus eventBus) {
 		super(display, eventBus);
 	}
 
 	@Override
 	protected void onBind() {
 		display.setPresenter(this);
 		display.setRatingReadOnly(true);
 	}
 
 	@Override
 	protected void onUnbind() {
 	}
 
 	@Override
 	protected void onRevealDisplay() {
 	}
 
 	@Override
 	public void showRequest(Integer requestId) {
 		RPC.getRequestService().getRequest(requestId, new AsyncCallback<Request>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar la solicitud", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(Request result) {
 				if (result != null) {
 					display.setStatus(result.getStatus());
 					display.setRequestTitle(result.getTitle());
 					display.setRequestDate(result.getDate());
 					display.setInstitutionName(result.getInstitution().getName());
 					display.setRequestInfo(result.getInformation());
 					display.setRequestContext(result.getContext());
 					if (result.getResponse() != null) {
 						display.setResponseDate(result.getResponse().getDate());
 						display.setResponseInfo(result.getResponse().getInformation());
 						loadAttachments(result.getResponse());
 					} else {
 						display.setResponseInfo("Esperando Respuesta");
 					}
 					request = result;
 					loadComments(result);
 					display.setRatingValue(request.getQualification().intValue());
 					if (ClientSessionUtil.checkSession()) {
 						display.showNewCommentPanel();
 						display.setRatingReadOnly(false);
 					}
 				} else {
 					showNotification("No se puede cargar la solicitud", NotificationEventType.ERROR);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void loadComments(Request request) {
 		RPC.getRequestService().getRequestComments(request, new AsyncCallback<List<RequestComment>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar los archivos adjuntos", NotificationEventType.ERROR);
 				History.back();
 			}
 
 			@Override
 			public void onSuccess(List<RequestComment> comments) {
 				display.setComments(comments);
 			}
 		});
 	}
 
 	@Override
 	public void saveComment(String commentContent) {
 		RequestComment comment = new RequestComment();
 		comment.setDate(new Date());
 		comment.setText(commentContent);
 		comment.setUser(ClientSessionUtil.getUser());
 		comment.setRequest(request);
 
 		RPC.getRequestService().createRequestComment(comment, new AsyncCallback<RequestComment>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible publicar su comentario", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(RequestComment comment) {
 				showNotification("Se ha publicado su comentario", NotificationEventType.SUCCESS);
 				display.cleanNewCommentText();
 				loadComments(comment.getRequest());
 			}
 		});
 
 	}
 
 	@Override
 	public void loadAttachments(Response response) {
 		RPC.getRequestService().getResponseAttachmentList(response, new AsyncCallback<List<Attachment>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar los archivos adjuntos", NotificationEventType.ERROR);
 				History.back();
 			}
 
 			@Override
 			public void onSuccess(List<Attachment> attachments) {
 				if (attachments.size() > 0) {
 					display.initTable();
 					ListDataProvider<Attachment> data = new ListDataProvider<Attachment>(attachments);
 					display.setResponseAttachments(data);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void saveQualification(Integer rate) {
 		UserRequestQualification qualification = new UserRequestQualification();
 		qualification.setQualification(rate);
 		qualification.setRequest(this.request);
 		qualification.setUser(ClientSessionUtil.getUser());
 
 		RPC.getRequestService().saveUserRequestQualification(qualification, new AsyncCallback<UserRequestQualification>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No se puede almacenar su calificacion", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(UserRequestQualification result) {
 				display.setRatingValue(result.getRequest().getQualification().intValue());
 			}
 		});
 	}
 
 	@Override
 	public String getListLink() {
 		String link = null;
 		List<String> tokenList = AppController.getHistoryTokenList();
 
		for(int i = tokenList.size(); i >= 0; i--) {
			if (AppController.getPlace(tokenList.get(i)).toString().equals(AppPlace.LIST)) {
 				link = tokenList.get(i);
 			}
 		}
 
 		return link;
 	}
 
 	@Override
 	public void showNotification(String message, NotificationEventType type) {
 		NotificationEventParams params = new NotificationEventParams();
 		params.setMessage(message);
 		params.setType(type);
 		eventBus.fireEvent(new NotificationEvent(params));
 	}
 }
