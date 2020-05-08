 package org.accesointeligente.client.presenters;
 
 import org.accesointeligente.client.ClientSessionUtil;
 import org.accesointeligente.client.services.RPC;
 import org.accesointeligente.client.views.RequestView.State;
 import org.accesointeligente.model.Institution;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.RequestCategory;
 import org.accesointeligente.shared.RequestStatus;
 
 import net.customware.gwt.presenter.client.EventBus;
 import net.customware.gwt.presenter.client.widget.WidgetDisplay;
 import net.customware.gwt.presenter.client.widget.WidgetPresenter;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.*;
 
 public class RequestPresenter extends WidgetPresenter<RequestPresenter.Display> implements RequestPresenterIface {
 	public interface Display extends WidgetDisplay {
 		void setPresenter(RequestPresenterIface presenter);
 		void displayMessage(String message);
 		State getState();
 		void setState(State state);
 		void setInstitutions(Map<String, Institution> institutions);
 		void cleanRequestCategories();
 		void addRequestCategories(RequestCategory category);
 		Institution getInstitution();
 		String getRequestInfo();
 		String getRequestContext();
 		String getRequestTitle();
 		Set<RequestCategory> getRequestCategories();
 		Boolean getAnotherInstitutionYes();
 		Boolean getAnotherInstitutionNo();
 	}
 
 	private Request request;
 
 	public RequestPresenter(Display display, EventBus eventBus) {
 		super(display, eventBus);
 	}
 
 	@Override
 	protected void onBind() {
 		display.setPresenter(this);
 		display.setState(State.REQUEST);
 		getRequestCategories();
 		getInstitutions();
 	}
 
 	@Override
 	protected void onUnbind() {
 	}
 
 	@Override
 	protected void onRevealDisplay() {
 	}
 
 	@Override
 	public void getRequestCategories() {
 		display.cleanRequestCategories();
 
 		RPC.getRequestService().getCategories(new AsyncCallback<List<RequestCategory>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				display.displayMessage("Error obteniendo actividades");
 			}
 
 			@Override
 			public void onSuccess(List<RequestCategory> result) {
 				for (RequestCategory category : result) {
 					display.addRequestCategories(category);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void getInstitutions() {
 		RPC.getInstitutionService().getInstitutions(new AsyncCallback<List<Institution>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				display.displayMessage("No es posible recuperar las instituciones");
 			}
 
 			@Override
 			public void onSuccess(List<Institution> result) {
 				Map<String, Institution> institutions = new HashMap<String, Institution>();
 
 				for (Institution institution: result) {
 					institutions.put(institution.getName(), institution);
 				}
 
 				display.setInstitutions(institutions);
 			}
 		});
 	}
 
 	@Override
 	public void submitRequest() {
 		Institution institution = display.getInstitution();
 
 		if (institution == null) {
 			display.displayMessage("Por favor complete el campo de Institución");
 			return;
 		}
 
 		String requestInfo = display.getRequestInfo();
 		String requestContext = display.getRequestContext();
 
 		if (requestInfo == null || requestInfo.trim().length() == 0) {
 			display.displayMessage("Por favor complete el campo de Información");
 			return;
 		}
 
 		if (requestContext == null || requestContext.trim().length() == 0) {
 			display.displayMessage("Por favor complete el campo de Contexto");
 			return;
 		}
 
 		String requestTitle = display.getRequestTitle();
 		Set<RequestCategory> categories = display.getRequestCategories();
 		Boolean anotherInstitutionYes = display.getAnotherInstitutionYes();
 		Boolean anotherInstitutionNo = display.getAnotherInstitutionNo();
 
 		if (requestTitle == null || requestTitle.trim().length() == 0) {
 			display.displayMessage("Por favor complete el campo de Titulo de la solicitud");
 			return;
 		}
 
 		if (categories.size() == 0) {
 			display.displayMessage("Por favor seleccione al menos una categoria");
 			return;
 		}
 
 		if(anotherInstitutionYes == false && anotherInstitutionNo == false) {
 			display.displayMessage("Por favor seleccione si desea solicitar esta información a otro organismo");
 			return;
 		}
 
 		request = new Request();
 		request.setInstitution(institution);
 		request.setInformation(requestInfo);
 		request.setContext(requestContext);
 		request.setTitle(requestTitle);
 		request.setCategories(categories);
 		request.setAnotherInstitution(anotherInstitutionYes);
 		request.setUser(ClientSessionUtil.getUser());
 		request.setStatus(RequestStatus.NEW);
 		request.setDate(new Date());
 
 		RPC.getRequestService().saveRequest(request, new AsyncCallback<Request>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				caught.printStackTrace(System.err);
 				display.displayMessage("No se ha podido almacenar su solicitud, intente nuevamente");
 			}
 
 			@Override
 			public void onSuccess(Request result) {
 				request = result;
 				display.setState(State.SUCCESS);
 			}
 		});
 	}
 
 	@Override
 	public void showRequest() {
 		if (request != null) {
			History.newItem("status?requestId=" + request.getId());
 		} else {
 			display.displayMessage("No se ha podido cargar la solicitud");
 		}
 	}
 }
