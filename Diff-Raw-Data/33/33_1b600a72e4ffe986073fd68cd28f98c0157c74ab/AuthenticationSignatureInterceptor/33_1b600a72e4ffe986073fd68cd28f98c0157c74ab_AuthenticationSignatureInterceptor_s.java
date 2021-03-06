 package xdi2.messaging.target.interceptor.impl.authentication.signature;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import xdi2.core.Literal;
 import xdi2.core.constants.XDIAuthenticationConstants;
 import xdi2.core.features.nodetypes.XdiAttribute;
 import xdi2.core.features.nodetypes.XdiAttributeSingleton;
 import xdi2.core.features.nodetypes.XdiValue;
 import xdi2.core.features.signatures.Signature;
 import xdi2.messaging.Message;
 import xdi2.messaging.MessageResult;
 import xdi2.messaging.exceptions.Xdi2AuthenticationException;
 import xdi2.messaging.exceptions.Xdi2MessagingException;
 import xdi2.messaging.target.ExecutionContext;
 import xdi2.messaging.target.MessagingTarget;
 import xdi2.messaging.target.Prototype;
 import xdi2.messaging.target.interceptor.AbstractInterceptor;
 import xdi2.messaging.target.interceptor.MessageInterceptor;
 import xdi2.messaging.target.interceptor.MessagingTargetInterceptor;
 
 /**
  * This interceptor looks for a signature on an incoming XDI message,
  * and invokes an instance of SignatureAuthenticator to authenticate the message.
  */
 public class AuthenticationSignatureInterceptor extends AbstractInterceptor implements MessagingTargetInterceptor, MessageInterceptor, Prototype<AuthenticationSignatureInterceptor> {
 
 	private static Logger log = LoggerFactory.getLogger(AuthenticationSignatureInterceptor.class.getName());
 
 	private SignatureAuthenticator signatureAuthenticator;
 
 	/*
 	 * Prototype
 	 */
 
 	@Override
 	public AuthenticationSignatureInterceptor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {
 
 		// create new interceptor
 
 		AuthenticationSignatureInterceptor interceptor = new AuthenticationSignatureInterceptor();
 
 		// set the authenticator
 
 		interceptor.setSignatureAuthenticator(this.getSignatureAuthenticator().instanceFor(prototypingContext));
 
 		// done
 
 		return interceptor;
 	}
 
 	/*
 	 * MessagingTargetInterceptor
 	 */
 
 	@Override
 	public void init(MessagingTarget messagingTarget) throws Exception {
 
 		this.getSignatureAuthenticator().init();
 	}
 
 	@Override
 	public void shutdown(MessagingTarget messagingTarget) throws Exception {
 
 		this.getSignatureAuthenticator().shutdown();
 	}
 
 	/*
 	 * MessageInterceptor
 	 */
 
 	@Override
 	public boolean before(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {
 
 		// look for signature on the message
 
		Signature<?, ?> signature = message.getSignature(false);
 		if (signature == null) return false;
 
 		// authenticate
 
 		if (log.isDebugEnabled()) log.debug("Authenticating via " + this.getSignatureAuthenticator().getClass().getSimpleName());
 
		
		SignatureAuthenticator s =  this.getSignatureAuthenticator();

		// TODO!!!!
		
		
		boolean authenticated = s.authenticate(message, signature);
 		if (! authenticated) throw new Xdi2AuthenticationException("Invalid signature.", null, executionContext);
 
 		XdiAttribute signatureValidXdiAttribute = XdiAttributeSingleton.fromContextNode(message.getContextNode().setDeepContextNode(XDIAuthenticationConstants.XRI_S_SIGNATURE_VALID));
 		XdiValue signatureValidXdiValue = signatureValidXdiAttribute.getXdiValue(true);
 		Literal signatureValidLiteral = signatureValidXdiValue.getContextNode().setLiteralBoolean(Boolean.valueOf(authenticated));
 
 		if (log.isDebugEnabled()) log.debug(signatureValidLiteral.getStatement().toString());
 
 		// done
 
 		return false;
 	}
 
 	@Override
 	public boolean after(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {
 
 		return false;
 	}
 
 	/*
 	 * Getters and setters
 	 */
 
 	public SignatureAuthenticator getSignatureAuthenticator() {
 
 		return this.signatureAuthenticator;
 	}
 
 	public void setSignatureAuthenticator(SignatureAuthenticator signatureAuthenticator) {
 
 		this.signatureAuthenticator = signatureAuthenticator;
 	}
 }
