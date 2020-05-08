 /**
  * 
  */
 package org.purl.wf4ever.wf2ro;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.purl.wf4ever.rosrs.client.Annotable;
 import org.purl.wf4ever.rosrs.client.Annotation;
 import org.purl.wf4ever.rosrs.client.Folder;
 import org.purl.wf4ever.rosrs.client.ROSRService;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Resource;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import uk.org.taverna.scufl2.api.container.WorkflowBundle;
 
 /**
  * This class implements a Wf-RO converter uploading all created resources to
  * the RODL.
  * 
  * @author piotrekhol
  */
 public class RodlConverter extends Wf2ROConverter {
 
 	/** Logger. */
 	private static final Logger LOG = Logger.getLogger(RodlConverter.class);
 
 	/** RO URI. */
 	private final URI roURI;
 
 	/** RODL client. */
 	private final ROSRService rosrs;
 
 	/**
 	 * URI of RO Folder where to extract main workflow, or <code>null</code> to
 	 * not add extracted main workflow to any folder (the main workflow is still
 	 * extracted)
 	 */
 	private URI extractMain;
 
 	/**
 	 * URI of RO Folder where to extract nested workflows, or <code>null</code>
 	 * to not extract.
 	 */
 	private URI extractNested;
 
 	/**
 	 * URI of RO Folder where to extract scripts, or <code>null</code> to not
 	 * extract.
 	 */
 	private URI extractScripts;
 
 	/**
 	 * URI of RO Folder where to extract services, or <code>null</code> to not
 	 * extract
 	 */
 	private URI extractServices;
 
 	private ResearchObject ro;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param wfbundle
 	 *            the workflow bundle
 	 * @param wfUri
 	 *            workflow URI
 	 * @param roURI
 	 *            research object URI, will be created if doesn't exist
 	 * @param rodlToken
 	 *            the RODL access token for updating the RO
 	 * @param extractMain
 	 *            URI of RO Folder where to extract main workflow, or null to
 	 *            not add extracted main workflow to any folder (the main
 	 *            workflow is still extracted)
 	 * @param extractNested
 	 *            URI of RO Folder where to extract nested workflows, or null to
 	 *            not extract.
 	 * @param extractScripts
 	 *            URI of RO Folder where to extract scripts, or null to not
 	 *            extract.
 	 * @param extractServices
 	 *            URI of RO Folder where to extract services, or null to not
 	 *            extract.
 	 */
 	public RodlConverter(WorkflowBundle wfbundle, URI wfUri, URI roURI, String rodlToken,
 			URI extractMain, URI extractNested, URI extractScripts, URI extractServices) {
 		super(wfbundle, wfUri);
 		this.extractMain = extractMain;
 		this.extractNested = extractNested;
 		this.extractScripts = extractScripts;
 		this.extractServices = extractServices;
 		URI rodlURI = roURI.resolve(".."); // zrobic z tego metode i stala
 		this.rosrs = new ROSRService(rodlURI, rodlToken);
 		this.roURI = roURI;
 	}
 
 	@Override
 	protected ResearchObject createResearchObject(UUID wfUUID) throws ROSRSException {
 		try {
 			rosrs.getResourceHead(roURI);
 			LOG.debug("Research object " + roURI + " returned status 200 OK, will use this one");
 			return getResearchObject();
 		} catch (ROSRSException e) {
 			LOG.debug("Research object " + roURI + " returned status " + e.getStatus()
 					+ ", will create a new one");
 
 			String slug = uriToSlug(roURI);
 
 			if (slug.isEmpty()) {
 				slug = wfUUID.toString();
 			}
 			if (slug.endsWith("/")) {
 				slug = slug.substring(0, slug.length() - 1);
 			}
 			ro = ResearchObject.create(rosrs, slug);
 			return ro;
 		}
 	}
 
 	protected static String uriToSlug(URI uri) {
 		// Remove any trailing /'s
 		String path = uri.getPath().replaceAll("/+$", "");
 
 		// the "slug" is the name in the last element, so discard anything
 		// before
 		String slug = path.replaceAll(".*/+", "");
 		return slug;
 	}
 
 	protected ResearchObject getResearchObject() {
 		if (ro == null) {
 			ro = new ResearchObject(roURI, rosrs);
 		}
 		if (!ro.isLoaded()) {
 			try {
 				ro.load();
 			} catch (ROSRSException | ROException e) {
 				LOG.debug("Can't load RO from URI " + roURI, e);
 				throw new IllegalStateException("Can't load RO from URI " + roURI, e);
 			}
 		}
 		return ro;
 	}
 
 	@Override
 	protected Resource uploadAggregatedResource(ResearchObject ro, String path, InputStream in,
 			String contentType) throws IOException, ROSRSException, ROException {
 		return ro.aggregate(path, in, contentType);
 	}
 
 	@Override
 	protected Annotation uploadAnnotation(ResearchObject ro, String name, Annotable target,
 			InputStream in, String contentType) throws ROSRSException, ROException {
 		String bodyPath = createAnnotationBodyPath(target.getName() + "-" + name);
 		return target.annotate(bodyPath, in, contentType);
 	}
 
 	@Override
 	public Folder getExtractMain() {
 		if (extractMain == null) {
 			return null;
 		}
 		return getResearchObject().getFolder(extractMain);
 	}
 
 	@Override
 	public Folder getExtractNested() {
 		if (extractNested == null) {
 			return null;
 		}
 		return getResearchObject().getFolder(extractNested);
 	}
 
 	@Override
 	public boolean isExtractNested() {
 		if (roURI.equals(extractNested)) {
 			// If the nested URI equals the RO, then we extract
 			// without adding to a folder
 			return true;
 		}
 		// Otherwise, only extract if non-null and exists
 		return super.isExtractNested();
 	}
 
 	@Override
 	public boolean isExtractScripts() {
 		if (roURI.equals(extractScripts)) {
 			// If the nested URI equals the RO, then we extract
 			// without adding to a folder
 			return true;
 		}
 		// Otherwise, only extract if non-null and exists
		return super.isExtractNested();
 	}
 
 	@Override
 	public boolean isExtractServices() {
 		if (roURI.equals(extractServices)) {
 			// If the nested URI equals the RO, then we extract
 			// without adding to a folder
 			return true;
 		}
 		// Otherwise, only extract if non-null and exists
		return super.isExtractNested();
 	}
 
 	@Override
 	public Folder getExtractScripts() {
 		if (extractScripts == null) {
 			return null;
 		}
 		return getResearchObject().getFolder(extractScripts);
 	}
 
 	@Override
 	public Folder getExtractServices() {
 		if (extractServices == null) {
 			return null;
 		}
 		return getResearchObject().getFolder(extractServices);
 	}
 
 	/**
 	 * Generate a path for an annotation body of a resource. The template is
 	 * ["ro"|resource_name] + "-" + random_string.
 	 * 
 	 * @param targetName
 	 *            the last segment or full URI
 	 * @return an annotation body path relative to the RO URI
 	 */
 	private static String createAnnotationBodyPath(String targetName) {
 		if (targetName.endsWith("/")) {
 			targetName = targetName.substring(0, targetName.length() - 1);
 		}
 		String randomBit = "" + Math.abs(UUID.randomUUID().getLeastSignificantBits());
 		return ".ro/" + targetName + "-" + randomBit + ".ttl";
 	}
 
 	@Override
 	protected Resource aggregateExternal(ResearchObject ro, URI external) throws ROSRSException {
 		return Resource.create(ro, external);
 	}
 
 }
