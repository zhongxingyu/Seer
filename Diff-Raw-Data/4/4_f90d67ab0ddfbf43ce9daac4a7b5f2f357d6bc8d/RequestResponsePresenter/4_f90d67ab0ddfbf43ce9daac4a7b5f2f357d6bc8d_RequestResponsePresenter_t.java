 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.client.presenters;
 
 import org.accesointeligente.client.AppController;
 import org.accesointeligente.client.ClientSessionUtil;
 import org.accesointeligente.client.services.RPC;
 import org.accesointeligente.client.widgets.ResponseWidget;
 import org.accesointeligente.client.widgets.UserResponseWidget;
 import org.accesointeligente.model.*;
 import org.accesointeligente.shared.*;
 
 import net.customware.gwt.presenter.client.EventBus;
 import net.customware.gwt.presenter.client.widget.WidgetDisplay;
 import net.customware.gwt.presenter.client.widget.WidgetPresenter;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.view.client.ListDataProvider;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class RequestResponsePresenter extends WidgetPresenter<RequestResponsePresenter.Display> implements RequestResponsePresenterIface {
 	public interface Display extends WidgetDisplay {
 		void setPresenter(RequestResponsePresenterIface presenter);
 		// Request
 		void setStatus(RequestStatus status);
 		void setRequestTitle(String title);
 		void setRequestDate(Date date);
 		void setInstitutionName(String name);
 		void setRequestInfo(String info);
 		void setRequestContext(String context);
 		// Response
 		void setResponses(List<Response> responses);
 		void setComments(List<RequestComment> comments);
 		void showNewCommentPanel();
 		void cleanNewCommentText();
 		void setRatingValue(Integer rate);
 		void setRatingReadOnly(Boolean readOnly);
 		void clearResponseWidget(ResponseWidget widget);
 		void setUserResponse(UserResponse userResponse, ResponseWidget widget);
 		void newUserResponse(Response response, ResponseWidget widget);
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
 					request = result;
 					display.setStatus(request.getStatus());
 					display.setRequestTitle(request.getTitle());
 					display.setRequestDate(request.getConfirmationDate());
 					display.setInstitutionName(request.getInstitution().getName());
 					display.setRequestInfo(request.getInformation());
 					display.setRequestContext(request.getContext());
 					List<Response> responses;
 					if (request.getResponses() != null && request.getResponses().size() > 0) {
 						responses = new ArrayList<Response>(request.getResponses());
 					} else {
 						responses = new ArrayList<Response>();
 						Response response = new Response();
 						response.setInformation("Esperando Respuesta");
 						responses.add(response);
 					}
 					display.setResponses(responses);
 
 					loadComments(request);
 					if (request.getQualification() != null) {
 						display.setRatingValue(request.getQualification().intValue());
 					}
 
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
 	public void loadAttachments(Response response, final ResponseWidget widget) {
 		RPC.getRequestService().getResponseAttachmentList(response, new AsyncCallback<List<Attachment>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar los archivos adjuntos", NotificationEventType.ERROR);
 				History.back();
 			}
 
 			@Override
 			public void onSuccess(List<Attachment> attachments) {
 				if (attachments != null && attachments.size() > 0) {
 					widget.initTableColumns();
 					ListDataProvider<Attachment> data = new ListDataProvider<Attachment>(attachments);
 					widget.setResponseAttachments(data);
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
 	public void getUserResponse(final Response response, final ResponseWidget widget) {
 		RPC.getRequestService().getUserResponse(response, new AsyncCallback<UserResponse>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No se puede cargar la respuesta del usuario", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(UserResponse result) {
 				if (result != null) {
 					display.setUserResponse(result, widget);
 				} else {
 					if (ClientSessionUtil.checkSession() && response.getRequest().getUser() != null && response.getRequest().getUser().equals(ClientSessionUtil.getUser())) {
 						display.newUserResponse(response, widget);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void saveUserResponse(String information, Response response, final ResponseWidget widget) {
 		UserResponse userResponse = new UserResponse();
 		userResponse.setResponse(response);
 		userResponse.setInformation(information);
 		userResponse.setDate(new Date());
 
 		RPC.getRequestService().saveUserResponse(userResponse, new AsyncCallback<UserResponse>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No se pudo guardar su respuesta, por favor intente nuevamente", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(UserResponse result) {
 				showNotification("Se ha guardado su respuesta", NotificationEventType.SUCCESS);
 				display.clearResponseWidget(widget);
 				display.setUserResponse(result, widget);
 			}
 		});
 	}
 
 	@Override
 	public String getListLink() {
		String link = AppPlace.LIST.toString() + "?type=" + RequestListType.GENERAL.toString();
 		List<String> tokenList = AppController.getHistoryTokenList();
 
 		for(int i = tokenList.size() - 1; i >= 0; i--) {
 			if (AppController.getPlace(tokenList.get(i)).toString().equals(AppPlace.LIST.toString())) {
 				link = tokenList.get(i);
				break;
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
