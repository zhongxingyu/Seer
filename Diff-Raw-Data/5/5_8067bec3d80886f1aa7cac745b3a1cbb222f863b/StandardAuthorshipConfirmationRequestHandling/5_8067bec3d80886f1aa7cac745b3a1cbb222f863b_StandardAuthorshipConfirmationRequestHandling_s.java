 package om.tnavigator.request.authorship;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.Log;
 import om.RenderedOutput;
 import om.RequestAssociates;
 import om.RequestHandlingException;
 import om.RequestParameterNames;
 import om.RequestResponse;
 import om.tnavigator.NavigatorServlet;
 import om.tnavigator.TestDefinition;
 import om.tnavigator.TestDeployment;
 import om.tnavigator.UserSession;
 import om.tnavigator.db.DatabaseAccess;
 import om.tnavigator.db.OmQueries;
 
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import util.misc.FinalizedResponse;
 import util.misc.RequestHelpers;
 import util.misc.StandardFinalizedResponse;
 import util.misc.UtilityException;
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
  * Basic implementation of request handling for the Authorship confirmation
  *  required for certain Accessed tests.
  * @author Trevor Hinson
  */
 
 public class StandardAuthorshipConfirmationRequestHandling
 	implements AuthorshipConfirmationRequestHandler {
 
 	private static final long serialVersionUID = -7226664092547045144L;
 
 	private static String ROOT_NODE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><div class=\"authority-confirmation\">";
 
 	private static String DIV = "</div>";	
 
 	private static String REQUEST_ASSOCIATES = "RequestAssociates";
 
 	private static String RENDERED_OUTPUT = "RenderedOutput";
 
 	private static String XML_OUTPUT_RESPONSE = "Document";
 
 	private static String POST = "POST";
 
 	private static String BODY = "body";
 
 	private static String ID = "id";
 
 	private static String QUESTION = "question";
 
 	private static String ON_LOAD = "onLoad";
 
 	private static String ESTABLISH = "establish();";
 
 	private static Map<String, Class<?>> requiresValidation = new LinkedHashMap<String, Class<?>>();
 
 	static {
 		requiresValidation.put(RequestParameterNames.DatabaseAccess.toString(), DatabaseAccess.class);
 		requiresValidation.put(RequestParameterNames.OmQueries.toString(), OmQueries.class);
 		requiresValidation.put(RequestParameterNames.Log.toString(), Log.class);
 		requiresValidation.put(RequestParameterNames.PostLocation.toString(), String.class);
 		requiresValidation.put(RequestParameterNames.AuthorshipQueryBean.toString(), AuthorshipQueryBean.class);
 		requiresValidation.put(RequestParameterNames.AuthorshipXMLDocument.toString(), Document.class);
 		requiresValidation.put(RequestParameterNames.UserSession.toString(), UserSession.class);
 		requiresValidation.put(RequestParameterNames.AccessibilityCookie.toString(), String.class);
 		requiresValidation.put(RequestParameterNames.ParentTemplate.toString(), Document.class);
 		Collections.unmodifiableMap(requiresValidation);
 	}
 
 	private AuthorshipRenderedResponseBuilder authorshipRenderedResponseBuilder
 		= new XMLAuthorshipConfirmationBuilder();
 
 	private AuthorshipRenderedResponseBuilder getAuthorshipRenderedResponseBuilder()
 		throws RequestHandlingException {
 		return authorshipRenderedResponseBuilder;
 	}
 
 	@Override
 	public RequestAssociates generateRequiredRequestAssociates(
 		HttpServlet servlet, HttpServletRequest request,
 		HttpServletResponse response, UserSession userSession)
 		throws RequestHandlingException {
 		RequestAssociates ra = null;
 		if (null != servlet && null != request && null != response) {
 			if (servlet instanceof NavigatorServlet) {
 				String method = request.getMethod();
 				boolean fromAPost = null != method ? POST.equalsIgnoreCase(method) : false;
 				ra = new RequestAssociates(servlet.getServletContext(),
 					request.getPathInfo(), fromAPost,
 					new HashMap<String, Object>());
 				NavigatorServlet ns = (NavigatorServlet) servlet;
 				ra.getPrincipleObjects().put(
 					RequestParameterNames.OmQueries.toString(), ns.getOmQueries());
 				ra.getPrincipleObjects().put(
 					RequestParameterNames.DatabaseAccess.toString(),
 					ns.getDatabaseAccess());
 				ra.getPrincipleObjects().put(RequestParameterNames.Log.toString(),
 					ns.getLog());
 				ra.getPrincipleObjects().put(RequestParameterNames.AuthorshipQueryBean.toString(),
 					getAuthorshipQueryBean(ns.getOmQueries()));
 				ra.getPrincipleObjects().put(RequestParameterNames.UserSession.toString(), userSession);
 				ra.getPrincipleObjects().put(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(),
 					request.getParameter(RequestParameterNames.UserAuthorshipConfirmationResponse.toString()));
 				ra.getPrincipleObjects().put(RequestParameterNames.AuthorshipXMLDocument.toString(),
 					ns.getAuthorshipConfirmation());
 				ra.getPrincipleObjects().put(RequestParameterNames.AccessCSSAppend.toString(),
 						RequestHelpers.getAccessCSSAppend(request));
 				String contextPath = servlet.getServletContext().getContextPath();
 				ra.getPrincipleObjects().put(RequestParameterNames.PostLocation.toString(),
 					contextPath + request.getPathInfo());
 				ra.getPrincipleObjects().put(RequestParameterNames.AccessibilityCookie.toString(),
 					RequestHelpers.getAccessibilityCookie(request));
 				try {
 					Document parentTemplate = ns.getTemplate(RequestHelpers.inPlainMode(request),
 						userSession.isSingle(), false);
 					ra.getPrincipleObjects().put(RequestParameterNames.ParentTemplate.toString(), parentTemplate);
 				} catch (XMLException x) {
 					throw new RequestHandlingException(x);
 				}
 			}
 		}
 		return ra;
 	}
 
 	private AuthorshipQueryBean getAuthorshipQueryBean(OmQueries om) {
 		return new AuthorshipQueryBean(om);
 	}
 
 	@Override
 	public RequestResponse handle(HttpServletRequest request,
 		HttpServletResponse response, RequestAssociates associates)
 		throws RequestHandlingException {
 		RequestResponse rr = null;
 		if (shouldRun(associates)) {
 			addUsersResponseToRequestAssociates(request, associates);
 			AuthorshipConfirmationChecking checking = createAuthorshipConfirmationChecking(associates);
 			if (!hasConfirmedAlready(associates, checking)) {
 				rr = handleAuthorshipConfirmationChecking(request, associates, checking);
 			} else {
 				rr = new RenderedOutput();
 			}
 		} else {
 			rr = new RenderedOutput();
 		}
 		if (null == rr) {
 			throw new RequestHandlingException("There was an issue"
 				+ " processing the Authorship Confirmation.");
 		}
 		return rr;
 	}
 
 	/**
 	 * Here we check that the test meets the criteria for running this
 	 *  RequestHandler.  In this instance we identify if the TestDeployment
 	 *  specifies that the test is Accessed and therefore we need to run.
 	 * @param associates
 	 * @return
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	protected boolean shouldRun(RequestAssociates associates)
 		throws RequestHandlingException {
 		boolean should = false;
 		UserSession us = getUserSession(associates);
 		if (null != us ? null != us.getTestDeployment() : false) {
			if (TestDeployment.TYPE_ASSESSED_REQUIRED
 				== us.getTestDeployment().getType()) {
 				should = true;
 			}
 		}
 		return should;
 	}
 
 	private boolean hasConfirmedAlready(RequestAssociates associates,
 		AuthorshipConfirmationChecking checking) throws RequestHandlingException {
 		RenderedOutput ro = null;
 		try {
 			ro = checking.hasSuccessfullyConfirmed(getUserSession(associates));
 		} catch (AuthorshipConfirmationException x) {
 			throw new RequestHandlingException(x);
 		}
 		return null != ro ? ro.isSuccessful() : false;
 	}
 
 	/**
 	 * Applies the users response from the Authorship confirmation to the 
 	 *  RequestAssociates object which we use later on.
 	 * @param request
 	 * @param associates
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	protected void addUsersResponseToRequestAssociates(HttpServletRequest request,
 		RequestAssociates associates) throws RequestHandlingException {
 		if (null != request && null != associates) {
 			String key = RequestParameterNames.UserAuthorshipConfirmationResponse.toString();
 			String response = request.getParameter(key);
 			associates.putPrincipleObject(key, response);
 			if (RequestParameterNames.confirmed.toString().equals(response)) {
 				associates.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(),
 					AuthorshipAction.confirmingAuthorship);
 			}
 		} else {
 			throw new RequestHandlingException("Invalid request as either the"
 				+ " HttpServletRequest object or the RequestAssociates object was null :"
 				+ "\n > HttpServletRequest : " + request
 				+ "\n > RequestAssociates : " + associates);
 		}
 	}
 
 	/**
 	 * Checks that everything is in place so that processing can continue.
 	 * @param ra
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	protected void validatePrincipleObjects(RequestAssociates ra)
 		throws RequestHandlingException {
 		if (null != ra ? null != ra.getPrincipleObjects() : false) {
 			for (String key : requiresValidation.keySet()) {
 				Class<?> cls = requiresValidation.get(key);
 				if (StringUtils.isNotEmpty(key) && null != cls) {
 					Object obj = ra.getPrincipleObjects().get(key);
 					if (!(null != obj ? cls.isAssignableFrom(obj.getClass()) : false)) {
 						throwProblemBack(key, obj);
 					}
 				}
 			}
 		} else {
 			throwProblemBack(REQUEST_ASSOCIATES, ra);
 		}
 	}
 
 	/**
 	 * Runs through the RequestAssociates argument to check that we have what
 	 *  is needed to start processing the request here.  Will guarantee a
 	 *  valid AuthorshipConfirmationChecking object is returned or will throw
 	 *  an Exception.
 	 * @param ra
 	 * @return
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	protected AuthorshipConfirmationChecking createAuthorshipConfirmationChecking(
 		RequestAssociates ra) throws RequestHandlingException {
 		AuthorshipConfirmationChecking checking = null;
 		validatePrincipleObjects(ra);
 		Object bn = ra.getPrincipleObjects().get(
 			RequestParameterNames.AuthorshipQueryBean.toString());
 		if (null != bn ? bn instanceof AuthorshipQueryBean : false) {
 			checking = new AuthorshipConfirmationChecking(
 				getDatabaseAccess(ra), getLog(ra), (AuthorshipQueryBean) bn);
 		} else {
 			throwProblemBack(RequestParameterNames.
 				AuthorshipQueryBean.toString(), bn);
 		}
 		if (null == checking) {
 			throw new RequestHandlingException("Unable to create the"
 				+ " AuthorshipConfirmationChecking bean. Please check the"
 				+ " request paramters.");
 		}
 		return checking;
 	}
 
 	protected Object getPrincipleObject(RequestAssociates ra, String name) {
 		Object obj = null;
 		if (null != ra ? null != ra.getPrincipleObjects() : false) {
 			obj = ra.getPrincipleObjects().get(name);
 		}
 		return obj;
 	}
 
 	/**
 	 * Retrieves the parent template from the RequestAssociate arguments and
 	 *  then creates a clone of it that can be used for manipulation.
 	 * @param ra
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	protected Document getParentTemplate(RequestAssociates ra) {
 		Document d = null;
 		Object o = getPrincipleObject(ra,
 			RequestParameterNames.ParentTemplate.toString());
 		if (null != o ? o instanceof Document : false) {
 			d = XML.clone((Document) o);
 		}
 		return d;
 	}
 
 	protected Log getLog(RequestAssociates ra) {
 		Log log = null;
 		Object o = getPrincipleObject(ra, RequestParameterNames.Log.toString());
 		if (null != o ? o instanceof Log : false) {
 			log = (Log) o;
 		}
 		return log;
 	}
 
 	protected DatabaseAccess getDatabaseAccess(RequestAssociates ra) {
 		DatabaseAccess da = null;
 		Object o = getPrincipleObject(ra, RequestParameterNames.DatabaseAccess.toString());
 		if (null != o ? o instanceof DatabaseAccess : false) {
 			da = (DatabaseAccess) o;
 		}
 		return da;
 	}
 
 	protected UserSession getUserSession(RequestAssociates ra) {
 		UserSession us = null;
 		Object o = getPrincipleObject(ra, RequestParameterNames.UserSession.toString());
 		if (null != o ? o instanceof UserSession : false) {
 			us = (UserSession) o;
 		}
 		return us;
 	}
 
 	/**
 	 * A generalised means of throwing the exception based on differing object
 	 *  type issues.
 	 * @param nameOfNullObject
 	 * @param obj
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	private void throwProblemBack(String nameOfNullObject, Object obj)
 		throws RequestHandlingException {
 		throw new RequestHandlingException("Unable to continue as the "
 				+ nameOfNullObject + " object was either null or of "
 				+ "the wrong expected type : " + obj);
 	}
 
 	/**
 	 * Based on the action performed by the user we check to see if they either
 	 *  have already successfully confirmed the Authorship or if they are making
 	 *  confirmation at this point.
 	 * @param request
 	 * @param associates
 	 * @param checking
 	 * @param checking
 	 * @return
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	protected RequestResponse handleAuthorshipConfirmationChecking(
 		HttpServletRequest request, RequestAssociates associates,
 		AuthorshipConfirmationChecking checking) throws RequestHandlingException {
 		RequestResponse rr = null;
 		Object us = associates.getPrincipleObjects().get(
 			RequestParameterNames.UserSession.toString());
 		if (null != us ? us instanceof UserSession : false) {
 			Object o = associates.getPrincipleObjects()
 				.get(RequestParameterNames.AuthorshipAction.toString());
 			try {
 				if (null != o ? o instanceof AuthorshipAction : false) {
 					AuthorshipAction aa = (AuthorshipAction) o;
 					rr = isMakingConfirmation(aa)
 						? checking.makeConfirmation((UserSession) us)
 							: checking.hasSuccessfullyConfirmed((UserSession) us);
 					if (null != rr) {
 						if (!rr.isSuccessful()) {
 							retrieveAuthorshipConfirmationXML(request, associates,
 								(RenderedOutput) rr);
 						}
 					}
 				} else {
 					//rr = checking.hasSuccessfullyConfirmed((UserSession) us);
 					rr = new RenderedOutput();
 					((RenderedOutput) rr).setSuccessful(false);
 					retrieveAuthorshipConfirmationXML(request,
 						associates, (RenderedOutput) rr);
 				}
 			} catch (AuthorshipConfirmationException x) {
 				throw new RequestHandlingException(x);
 			}
 		} else {
 			throwProblemBack(RequestParameterNames.UserSession.toString(), us);
 		}
 		return rr;
 	}
 
 	/**
 	 * Here we simply check to see if the user has actually selected one of the
 	 *  options presented or not.  If nothing is specified then we say the user
 	 *  has specified Cancel to the question.
 	 * @param ra
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	protected boolean userSpecifiedCancelToQuestion(RequestAssociates ra) {		
 		boolean has = false;
 		Object obj = ra.getPrincipleObjects().get(
 			RequestParameterNames.UserAuthorshipConfirmationResponse.toString());
 		if (null != obj ? obj instanceof String : false) {
 			if (RequestParameterNames.Cancelled.toString()
 				.equalsIgnoreCase((String) obj)) {
 				has = true;
 			}
 		}
 		return has;
 	}
 
 	/**
 	 * Delegates to the composite XMLAuthorshipConfirmationBuilder so to provide
 	 *  the neccessary output to the user.
 	 * @param ra
 	 * @param ro
 	 * @return
 	 * @throws RequestHandlingException
 	 * @throws AuthorshipConfirmationException
 	 * @author Trevor Hinson
 	 */
 	protected String retrieveAuthorshipConfirmationXML(HttpServletRequest request,
 		RequestAssociates ra, RenderedOutput ro) throws RequestHandlingException,
 		AuthorshipConfirmationException {
 		String xml = null;
 		if (null != ra) {
 			if (null != ro) {
 				ro.setSuccessful(false);
 				Object o = ra.getPrincipleObjects()
 					.get(RequestParameterNames.AuthorshipXMLDocument.toString());
 				if (null != o ? o instanceof Document : false) {
 					Document template = (Document) o;
 					boolean showError = userSpecifiedCancelToQuestion(ra);
 					Document output = getAuthorshipRenderedResponseBuilder()
 						.renderForDisplay(template, showError, ra);
 					if (null != output) {
 						try {
 							xml = XML.saveString(output);
 							xml = stripForDisplay(xml);
 							Document merged = applyResponseDetails(request,
 								xml, ra, ro);
 							if (null != merged) {
 								applyBodyOnLoadJavascript(merged);
 								ro.append(XML.saveString(merged));
 								ro.setResponse(merged);
 							} else {
 								throw new RequestHandlingException("Unable to "
 									+ "continue with the Authorship processing "
 									+ "as the merged document returned was null.");
 							}
 						} catch (IOException x) {
 							throw new RequestHandlingException(x);
 						}
 					} else {
 						throwProblemBack(XML_OUTPUT_RESPONSE, output);
 					}
 				} else {
 					throwProblemBack(RequestParameterNames.AuthorshipXMLDocument.toString(), o);
 				}
 			} else {
 				throwProblemBack(RENDERED_OUTPUT, ro);
 			}
 		} else {
 			throwProblemBack(REQUEST_ASSOCIATES, ra);
 		}
 		return xml;
 	}
 
 	/**
 	 * Applies additional javascript for the onLoad function to the Document
 	 *  onLoad="establish();"
 	 * @param doc
 	 * @exception
 	 * @author Trevor Hinson
 	 */
 	protected void applyBodyOnLoadJavascript(Document doc)
 		throws RequestHandlingException {
 		if (null != doc ? XML.hasChild(doc.getFirstChild(), BODY) : false) {
 			try {
 				Element e = XML.getChild(doc.getFirstChild(), BODY);
 				e.setAttribute(ON_LOAD, ESTABLISH);
 			} catch (XMLException e) {
 				throw new RequestHandlingException("Unable to continue as the"
 					+ " body of the response was not present.");
 			}
 		}
 	}
 
 	protected Document applyResponseDetails(HttpServletRequest request,
 		String authorship, RequestAssociates ra, RenderedOutput ro)
 		throws RequestHandlingException {
 		Document parentTemplate = getParentTemplate(ra);
 		if (null != ra && null != ro && null != parentTemplate
 			&& StringUtils.isNotEmpty(authorship)) {
 			authorship = "<div id=\"question\"><div class=\"basicpage\">" + authorship + "</div></div>";
 			Map<String, Object> mReplace = setPreProcessingMapItems(ra);
 			try {
 				Element txt = (Element) parentTemplate.importNode(XML.parse(authorship)
 					.getDocumentElement(), true);
 				Element questionDiv = XML.find(parentTemplate, ID, QUESTION);
 				Node parent = questionDiv.getParentNode();
 				parent.replaceChild(txt, questionDiv);
 				XML.replaceTokens(parentTemplate, mReplace);
 				buildPreProcessedPageOutput(request, parentTemplate, getUserSession(ra));
 			} catch (XMLException x) {
 				throw new RequestHandlingException(x);
 			} catch (DOMException x) {
 				throw new RequestHandlingException(x);
 			} catch (IOException x) {
 				throw new RequestHandlingException(x);
 			}
 		} else {
 			// ...
 		}
 		return parentTemplate;
 	}
 
 	private void buildPreProcessedPageOutput(HttpServletRequest request,
 		Document d, UserSession us) throws IOException {
 		if (us.getTestDefinition().getNavLocation()
 			== TestDefinition.NAVLOCATION_LEFT) {
 			XML.remove(XML.find(d, ID, "progressBottom"));
 			Element eProgress = XML.find(d, ID, "progressLeft");
 			eProgress.setAttribute(ID, "progress");
 			NavigatorServlet.addAccessibilityClasses(d, request, "progressleft");
 		} else {
 			XML.remove(XML.find(d, ID, "progressLeft"));
 			Element eProgress = XML.find(d, ID, "progressBottom");
 			eProgress.setAttribute(ID, "progress");
 			if (us.getTestDefinition().getNavLocation()
 				== TestDefinition.NAVLOCATION_WIDE) {
 				NavigatorServlet.addAccessibilityClasses(d, request, "progresswide");
 			} else {
 				NavigatorServlet.addAccessibilityClasses(d, request, "progressbottom");
 			}
 		}
 	}
 
 	private Map<String, Object> setPreProcessingMapItems(RequestAssociates ra) {
 		Map<String, Object> mReplace = new HashMap<String, Object>();
 		UserSession us = getUserSession(ra);
 		mReplace.put("TITLEBAR", "Authorship Confirmation");
 		mReplace.put("CSSINDEX", "" + us.getICSSIndex());
 		mReplace.put("RESOURCES", "resources/" + us.getTestPosition());
 		mReplace.put("ACCESS", getPrincipleObject(ra,
 			RequestParameterNames.AccessCSSAppend.toString()));
 		mReplace.put("TESTTITLE", us.getTestDefinition().getName());
 		mReplace.put("TITLE", "Authorship Confirmation");
 		mReplace.put("AUXTITLE", " ");
 		mReplace.put("PROGRESSINFO", " ");
 		mReplace.put("RESOURCES", "resources/" + us.getTestPosition());
 		mReplace.put("IDPREFIX", "");
 		return mReplace;
 	}
 
 	/**
 	 * Removed certain elements of the Authorship template output so that it can
 	 *  be used within the parent template.
 	 * @param original
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	public String stripForDisplay(String original) {
 		String changed = original;
 		if (StringUtils.isNotEmpty(original)) {
 			if (changed.startsWith(ROOT_NODE)) {
 				changed = changed.substring(ROOT_NODE.length(), original.length());
 				if (changed.endsWith(DIV)) {
 					changed = changed.substring(0,
 						changed.length() - DIV.length());
 				}
 			}
 		}
 		return changed;
 	}
 
 	/**
 	 * Checks the current request AuthorshipAction to see if the request is to
 	 *  confirm or not.
 	 * @param aa
 	 * @return
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	private boolean isMakingConfirmation(AuthorshipAction aa)
 		throws RequestHandlingException {
 		return AuthorshipAction.confirmingAuthorship.equals(aa);
 	}
 
 	@Override
 	public FinalizedResponse close(Object o) throws UtilityException {
 		return new StandardFinalizedResponse(true);
 	}
 
 }
