 package org.iucn.sis.server.extensions.images;
 
 import java.io.IOException;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.api.utils.FilenameStriper;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.InputRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.solertium.util.ElementCollection;
 import com.solertium.vfs.VFS;
 import com.solertium.vfs.VFSPath;
 
 public class ImageViewerRestlet extends BaseServiceRestlet {
 	
 	private final VFS vfs;
 	private final ImageUtils imageUtils;
 
 	public ImageViewerRestlet(Context context) {
 		super(context);
 		vfs = SIS.get().getVFS();
		ImageUtils imageUtils = null;
		try {
			imageUtils = new ImageUtils(context);
		} catch (UnsupportedOperationException e) {
			imageUtils = null;
		}
		
		this.imageUtils = imageUtils;
 	}
 
 	@Override
 	public void definePaths() {
 		paths.add("/images/view/{mode}/{taxonid}");
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		final String mode = (String) request.getAttributes().get("mode");
 		final Integer taxonID = getTaxonID(request);
 		final String uri = request.getResourceRef().getLastSegment();
 		
 		
 		final VFSPath stripedPath = 
 			new VFSPath("/images/" + FilenameStriper.getIDAsStripedPath(taxonID) + ".xml");
 		
 		if (!vfs.exists(stripedPath)) {
 			return notAvailable();
 		}
 		
 		final Document document;
 		try {
 			document = vfs.getDocument(stripedPath);
 		} catch (IOException e) {
 			Debug.println(e);
 			return notAvailable();
 		}
 		
 		final ElementCollection nodes = 
 			new ElementCollection(document.getDocumentElement().getElementsByTagName("image"));
 		for (Element node : nodes) {
 			String encoding = node.getAttribute("encoding");
 			
 			String extension;
 			MediaType mt;
 			if ("image/jpeg".equals(encoding)) {
 				extension = "jpg";
 				mt = MediaType.IMAGE_JPEG;
 			}
 			else if ("image/gif".equals(encoding)) {
 				extension = "gif";
 				mt = MediaType.IMAGE_GIF;
 			}
 			else if ("image/png".equals(encoding)) {
 				extension = "png";
 				mt = MediaType.IMAGE_PNG;
 			}
 			else {
 				Debug.println("Invalid file encoding {0} was found for taxon {1}, skipping...", encoding, taxonID);
 				continue;
 			}
 			
 			String currentItemUri = node.getAttribute("id") + "." + extension;
 			boolean isPrimary = "true".equals(node.getAttribute("primary"));
 			
 			if (uri.equals(currentItemUri) || (uri.equals("primary") && isPrimary)) {
 				final String path = "/images/bin/" + currentItemUri;
 				if (!vfs.exists(new VFSPath(path))) {
 					Debug.println("Reportedly existing image at {0} does not exist.", path);
 					continue;
 				}
 				
 				if ("thumb".equals(mode)) {
 					String sizeStr = request.getResourceRef().getQueryAsForm().getFirstValue("size", "100");
 					int size;
 					try {
 						size = Integer.parseInt(sizeStr);
 					} catch (Exception e) {
 						size = 100;
 					}
 					
 					try {
 						return represent(imageUtils.getResizedURI(path, size), mt);
 					} catch (Throwable e) {
 						return represent(path, mt);
 					}
 				}
 				else {
 					return represent(path, mt);
 				}
 			}
 		}
 		
 		return notAvailable();
 	}
 	
 	private Representation represent(String path, MediaType mt) throws ResourceException {
 		try {
 			return new InputRepresentation(
 				vfs.getInputStream(new VFSPath(path)), mt
 			);
 		} catch (IOException e) {
 			Debug.println("Failed to represent {0}\r\n{1}", path, e);
 			return notAvailable();
 		}
 	}
 	
 	private Representation notAvailable() {
 		return new InputRepresentation(
 			getClass().getResourceAsStream("unavailable.png"), 
 			MediaType.IMAGE_PNG
 		);
 	}
 	
 	private Integer getTaxonID(Request request) throws ResourceException {
 		try {
 			return Integer.valueOf((String)request.getAttributes().get("taxonid"));
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid taxon ID supplied.", e);
 		}
 	}
 
 }
