 package org.iucn.sis.server.restlets.utils;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.debug.Debug;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.NodeCollection;
 import com.solertium.util.restlet.InternalRequest;
 
 public class MultiDocumentRestlet extends BaseServiceRestlet {
 	
 	public MultiDocumentRestlet(Context context) {
 		super(context);
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/utils/documents");
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final Document requestDoc = getEntityAsDocument(entity);
 		final NodeCollection nodes = new NodeCollection(requestDoc.getDocumentElement().getChildNodes());
 		
 		final Document document = BaseDocumentUtils.impl.newDocument();
 		document.appendChild(document.createElement("root"));
 		
 		for (Node node : nodes) {
 			if ("uri".equals(node.getNodeName())) {
				String host = request.getResourceRef().getHostIdentifier();
				//Can fall back to real host, but raip is faster
				host = "riap://host";
				
				String uri = host + node.getTextContent();
 				
 				Request internal = new InternalRequest(request, Method.GET, uri);
 				Response resp = getContext().getClientDispatcher().handle(internal);
 				
 				Element respNode = document.createElement("response");
 				respNode.setAttribute("status", resp.getStatus().getCode()+"");
 				respNode.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "uri", node.getTextContent()));
 				if (resp.getStatus().isSuccess()) {
 					final Document respEntity;
 					try {
 						respEntity = getEntityAsDocument(resp.getEntity());
						
						Element head = respEntity.getDocumentElement();
						respNode.appendChild(document.importNode(head, true));
 					} catch (Exception e) {
						respNode.setAttribute("status", Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY.getCode()+"");
						Debug.println(e);
 					}
 				}
 				
 				document.getDocumentElement().appendChild(respNode);
 			}
 		}
 		
 		response.setStatus(Status.SUCCESS_MULTI_STATUS);
 		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
 	}
 
 }
