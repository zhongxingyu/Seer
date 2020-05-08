 package org.vme.web.service;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import org.fao.fi.vme.domain.dto.VmeDto;
 import org.fao.fi.vme.domain.dto.ref.ReferenceYear;
 import org.fao.fi.vme.domain.model.Authority;
 import org.fao.fi.vme.domain.model.VmeCriteria;
 import org.fao.fi.vme.domain.model.VmeType;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.reference.ReferenceConcept;
 import org.vme.dao.ReferenceDAO;
 import org.vme.dao.VmeSearchDao;
 import org.vme.web.service.io.ObservationsRequest;
 import org.vme.web.service.io.ReferencesRequest;
 import org.vme.web.service.io.ServiceResponse;
 
 /**
  * 
  * @author Fabrizio Sibeni
  * 
  */
 public class ServiceInvoker {
 
 	public static ServiceResponse<?> invoke(VmeSearchDao service, ObservationsRequest request) throws Exception {
 		ServiceResponse<?> result = new ServiceResponse<VmeDto>(request);
 		if (request.getId() > 0) {
 			result.addElements(service.getVmeById(request.getId(), request.getYear()));
 		} else if (request.hasInventoryIdentifier()) {
			result.addElements(service.getVmeByInventoryIdentifier(request.getInventoryIdentifier(), request.getYear()));
 		} else if (request.hasGeographicFeatureId()) {
			result.addElements(service.getVmeByGeographicFeatureId(request.getGeographicFeatureId(), request.getYear()));
 		} else {
			result.addElements(service.searchVme(request.getAuthority(), request.getType(), request.getCriteria(),
					request.getYear(), request.getText()));
 		}
 		return result;
 	}
 
	public static ServiceResponse<?> invoke(ReferenceDAO service, ReferencesRequest request) throws Exception {
 		Class<? extends ReferenceConcept> conceptClass = service.getConcept(request.getConcept());
 		ServiceResponse<?> result = null;
 		if (conceptClass.equals(Authority.class)) {
 			result = new ServiceResponse<Authority>(request);
 		} else if (conceptClass.equals(VmeCriteria.class)) {
 			result = new ServiceResponse<VmeCriteria>(request);
 		} else if (conceptClass.equals(VmeType.class)) {
 			result = new ServiceResponse<VmeType>(request);
 		} else if (conceptClass.equals(ReferenceYear.class)) {
 			result = new ServiceResponse<ReferenceYear>(request);
 		} else {
 			throw new WebApplicationException(Response.Status.BAD_REQUEST);
 		}
 		result.addElements(service.getAllReferences(conceptClass));
 		return result;
 	}
 
 }
