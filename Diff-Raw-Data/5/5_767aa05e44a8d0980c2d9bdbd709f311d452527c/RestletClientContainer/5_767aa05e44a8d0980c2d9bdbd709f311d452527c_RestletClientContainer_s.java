 /*******************************************************************************
 * Copyright (c) 2011 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.internal.provider.restlet.container;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.ecf.core.ContainerConnectException;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.internal.provider.restlet.identity.RestletNamespace;
 import org.eclipse.ecf.remoteservice.IRemoteCall;
 import org.eclipse.ecf.remoteservice.IRemoteService;
 import org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy;
 import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
 import org.eclipse.ecf.remoteservice.client.AbstractClientContainer;
 import org.eclipse.ecf.remoteservice.client.AbstractClientService;
 import org.eclipse.ecf.remoteservice.client.IRemoteCallParameter;
 import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
 import org.eclipse.ecf.remoteservice.client.IRemoteCallableRequestType;
 import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;
 import org.osgi.framework.InvalidSyntaxException;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Uniform;
 import org.restlet.data.ClientInfo;
 import org.restlet.data.Reference;
 import org.restlet.data.Status;
 import org.restlet.engine.resource.AnnotationInfo;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.Result;
 
 public class RestletClientContainer extends AbstractClientContainer {
 
 	protected IRemoteCallable[] createRemoteCallables(
 			@SuppressWarnings("rawtypes") Class serviceInterface) {
 		// Introspect the interface for Restlet annotations
 		final List<AnnotationInfo> annotations = org.restlet.engine.resource.AnnotationUtils
				.getAnnotations(serviceInterface);
 		if (annotations == null)
 			return null;
 		List<IRemoteCallable> results = new ArrayList<IRemoteCallable>();
 		for (AnnotationInfo annotation : annotations) {
 			IRemoteCallable callable = createRemoteCallable(annotation);
 			if (callable != null)
 				results.add(callable);
 		}
 		return results.toArray(new IRemoteCallable[results.size()]);
 	}
 
 	private IRemoteCallable createRemoteCallable(AnnotationInfo annotation) {
 		return new RestletRemoteCallable(annotation);
 	}
 
 	public RestletClientContainer(ID containerID,
 			Class<?>[] restletRemoteServiceInterfaces) {
 		super(containerID);
 		Assert.isNotNull(restletRemoteServiceInterfaces);
 		if (restletRemoteServiceInterfaces.length > 0) {
 			String[] serviceTypeNames = new String[restletRemoteServiceInterfaces.length];
 			IRemoteCallable[][] serviceTypeCallables = new IRemoteCallable[restletRemoteServiceInterfaces.length][];
 			for (int i = 0; i < restletRemoteServiceInterfaces.length; i++) {
 				serviceTypeNames[i] = restletRemoteServiceInterfaces[i]
 						.getName();
 				serviceTypeCallables[i] = createRemoteCallables(restletRemoteServiceInterfaces[i]);
 			}
 			registerCallables(serviceTypeNames, serviceTypeCallables, null);
 		}
 	}
 
 	public RestletClientContainer(Class<?>[] restletRemoteServiceInterfaces) {
 		this(IDFactory.getDefault().createGUID(),
 				restletRemoteServiceInterfaces);
 	}
 
 	public RestletClientContainer(Class<?> restletRemoteServiceInterface) {
 		this(IDFactory.getDefault().createGUID(),
 				new Class<?>[] { restletRemoteServiceInterface });
 	}
 
 	public boolean setRemoteServiceCallPolicy(IRemoteServiceCallPolicy policy) {
 		return false;
 	}
 
 	public Namespace getConnectNamespace() {
 		return IDFactory.getDefault().getNamespaceByName(RestletNamespace.NAME);
 	}
 
 	@Override
 	public IRemoteServiceReference[] getRemoteServiceReferences(ID target,
 			ID[] idFilter, String clazz, String filter)
 			throws InvalidSyntaxException, ContainerConnectException {
 		return super.getRemoteServiceReferences(target, null, clazz, filter);
 	}
 
 	@Override
 	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter,
 			String clazz, String filter) throws InvalidSyntaxException {
 		return super.getRemoteServiceReferences((ID[]) null, clazz, filter);
 	}
 
 	@Override
 	protected IRemoteService createRemoteService(
 			RemoteServiceClientRegistration registration) {
 		return new AbstractClientService(this, registration) {
 			@Override
 			protected Object invokeRemoteCall(IRemoteCall call,
 					IRemoteCallable callable) throws ECFException {
 				return ((RestletRemoteCallable) callable).invoke(
 						prepareEndpointAddress(call, callable),
 						call.getParameters());
 			}
 		};
 
 	}
 
 	@Override
 	protected String prepareEndpointAddress(IRemoteCall call,
 			IRemoteCallable callable) {
 		return getRemoteCallTargetID().getName();
 	}
 
 	public class RestletRemoteCallable implements IRemoteCallable {
 
 		public static final long defaultTimeout = 30000;
 
 		private org.restlet.engine.resource.AnnotationInfo annotation;
 
 		public RestletRemoteCallable(
 				org.restlet.engine.resource.AnnotationInfo annotation) {
 			this.annotation = annotation;
 		}
 
 		public Object invoke(String uri, Object[] args) throws ECFException {
 			RestletClientResource clientResource = new RestletClientResource(
 					uri);
 			return clientResource.invoke(annotation, args);
 		}
 
 		public String getMethod() {
 			return annotation.getJavaMethod().getName();
 		}
 
 		public String getResourcePath() {
 			return null;
 		}
 
 		public IRemoteCallableRequestType getRequestType() {
 			return null;
 		}
 
 		public IRemoteCallParameter[] getDefaultParameters() {
 			return null;
 		}
 
 		public long getDefaultTimeout() {
 			return defaultTimeout;
 		}
 
 		public class RestletClientResource extends ClientResource {
 
 			public RestletClientResource(String uri) {
 				super(uri);
 			}
 
 			protected void handle(Request request, Response response,
 					List<Reference> references, int retryAttempt, Uniform next) {
 				if (next != null) {
 					// Actually handle the call
 					next.handle(request, response);
 
 					// Check for redirections
 					if (isFollowingRedirects()
 							&& response.getStatus().isRedirection()
 							&& (response.getLocationRef() != null)) {
 						boolean doRedirection = false;
 
 						if (request.getMethod().isSafe()) {
 							doRedirection = true;
 						} else {
 							if (Status.REDIRECTION_SEE_OTHER.equals(response
 									.getStatus())) {
 								// The user agent is redirected using the GET
 								// method
 								request.setMethod(org.restlet.data.Method.GET);
 								request.setEntity(null);
 								doRedirection = true;
 							} else if (Status.REDIRECTION_USE_PROXY
 									.equals(response.getStatus())) {
 								doRedirection = true;
 							}
 						}
 
 						if (doRedirection) {
 							Reference newTargetRef = response.getLocationRef();
 
 							if ((references != null)
 									&& references.contains(newTargetRef)) {
 								getLogger().warning(
 										"Infinite redirection loop detected with URI: "
 												+ newTargetRef);
 							} else if (request.getEntity() != null
 									&& !request.isEntityAvailable()) {
 								getLogger()
 										.warning(
 												"Unable to follow the redirection because the request entity isn't available anymore.");
 							} else {
 								if (references == null) {
 									references = new ArrayList<Reference>();
 								}
 
 								// Add to the list of redirection reference
 								// to prevent infinite loops
 								references.add(request.getResourceRef());
 								request.setResourceRef(newTargetRef);
 								handle(request, response, references, 0, next);
 							}
 						}
 					} else if (isRetryOnError()
 							&& response.getStatus().isRecoverableError()
 							&& request.getMethod().isIdempotent()
 							&& (retryAttempt < getRetryAttempts())
 							&& ((getRequestEntity() == null) || getRequestEntity()
 									.isAvailable())) {
 						getLogger().log(
 								Level.INFO,
 								"A recoverable error was detected ("
 										+ response.getStatus().getCode()
 										+ "), attempting again in "
 										+ getRetryDelay() + " ms.");
 
 						// Wait before attempting again
 						if (getRetryDelay() > 0) {
 							try {
 								Thread.sleep(getRetryDelay());
 							} catch (InterruptedException e) {
 								getLogger().log(Level.FINE,
 										"Retry delay sleep was interrupted", e);
 							}
 						}
 
 						// Retry the call
 						handle(request, response, references, ++retryAttempt,
 								next);
 					}
 				}
 			}
 
 			public Response handle(Request request) {
 				Response response = createResponse(request);
 				Uniform next = getNext();
 
 				if (next != null) {
 					// Effectively handle the call
 					handle(request, response, null, 0, next);
 
 					// Update the last received response.
 					setResponse(response);
 				} else {
 					getLogger()
 							.warning(
 									"Unable to process the call for a client resource. No next Restlet has been provided.");
 				}
 
 				return response;
 
 			}
 
 			@SuppressWarnings("rawtypes")
 			public Object invoke(AnnotationInfo annotation, Object[] args)
 					throws ECFException {
 				Object result = null;
 				if (annotation != null) {
 					Representation requestEntity = null;
 					boolean isSynchronous = true;
 
 					if ((args != null) && args.length > 0) {
 						// Checks if the user has defined its own
 						// callback.
 						for (int i = 0; i < args.length; i++) {
 							Object o = args[i];
 
 							if (o == null) {
 								requestEntity = null;
 							} else if (Result.class.isAssignableFrom(o
 									.getClass())) {
 								// Asynchronous mode where a callback
 								// object is to be called.
 								isSynchronous = false;
 
 								Method javaMethod = annotation.getJavaMethod();
 								// Get the kind of result expected.
 								final Result rCallback = (Result<?>) o;
 								java.lang.reflect.Type[] genericParameterTypes = javaMethod
 										.getGenericParameterTypes();
 								java.lang.reflect.Type genericParameterType = genericParameterTypes[i];
 								java.lang.reflect.ParameterizedType parameterizedType = (genericParameterType instanceof java.lang.reflect.ParameterizedType) ? (java.lang.reflect.ParameterizedType) genericParameterType
 										: null;
 								final Class<?> actualType = (parameterizedType
 										.getActualTypeArguments()[0] instanceof Class<?>) ? (Class<?>) parameterizedType
 										.getActualTypeArguments()[0] : null;
 
 								// Define the callback
 								Uniform callback = new Uniform() {
 									@SuppressWarnings("unchecked")
 									public void handle(Request request,
 											Response response) {
 										if (response.getStatus().isError()) {
 											rCallback
 													.onFailure(new ResourceException(
 															response.getStatus()));
 										} else {
 											if (actualType != null) {
 												rCallback.onSuccess(toObject(
 														response.getEntity(),
 														actualType.getClass()));
 											} else {
 												rCallback.onSuccess(null);
 											}
 										}
 									}
 								};
 
 								setOnResponse(callback);
 							} else {
 								requestEntity = toRepresentation(args[i], null);
 							}
 						}
 					}
 
 					// Clone the prototype request
 					Request request = createRequest();
 
 					// The Java method was annotated
 					request.setMethod(annotation.getRestletMethod());
 
 					// Set the entity
 					request.setEntity(requestEntity);
 
 					// Updates the client preferences
 					List<org.restlet.representation.Variant> responseVariants = annotation
 							.getResponseVariants(getMetadataService(),
 									getConverterService());
 
 					if (responseVariants != null) {
 						request.setClientInfo(new ClientInfo(responseVariants));
 					}
 
 					// Effectively handle the call
 					Response response = handle(request);
 
 					// Handle the response
 					if (isSynchronous) {
 						if (response.getStatus().isError()) {
 							throw new ResourceException(response.getStatus());
 						}
 
 						if (!annotation.getJavaOutputType().equals(void.class)) {
 							result = toObject((response == null ? null
 									: response.getEntity()),
 									annotation.getJavaOutputType());
 						}
 					}
 				}
 				return result;
 			}
 		}
 
 	}
 
 }
