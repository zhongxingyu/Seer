 package org.cyclopsgroup.doorman.service.core;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cyclopsgroup.caff.util.UUIDUtils;
 import org.cyclopsgroup.doorman.api.SessionService;
 import org.cyclopsgroup.doorman.api.User;
 import org.cyclopsgroup.doorman.api.UserOperationResult;
 import org.cyclopsgroup.doorman.api.UserSession;
 import org.cyclopsgroup.doorman.api.UserSessionAttributes;
 import org.cyclopsgroup.doorman.api.UserSessionConfig;
 import org.cyclopsgroup.doorman.api.UserSignUpResult;
 import org.cyclopsgroup.doorman.service.dao.DAOFactory;
 import org.cyclopsgroup.doorman.service.dao.UserDAO;
 import org.cyclopsgroup.doorman.service.dao.UserSessionDAO;
 import org.cyclopsgroup.doorman.service.security.PasswordStrategy;
 import org.cyclopsgroup.doorman.service.storage.StoredUser;
 import org.cyclopsgroup.doorman.service.storage.StoredUserSession;
 import org.cyclopsgroup.doorman.service.storage.UserState;
 import org.cyclopsgroup.doorman.service.storage.UserType;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalDateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Default implementation of session service
  *
  * @author <a href="mailto:jiaqi@cyclopsgroup.org">Jiaqi Guo</a>
  */
 @Service
 public class DefaultSessionService
     implements SessionService
 {
     private static final Log LOG = LogFactory.getLog( DefaultSessionService.class );
 
     private static UserSession createUserSession( StoredUserSession s )
     {
         UserSession session = new UserSession();
         session.setCreationDate( new LocalDateTime( s.getCreationDate() ).toDateTime( DateTimeZone.UTC ) );
         session.setLastActivity( new LocalDateTime( s.getLastModified() ).toDateTime( DateTimeZone.UTC ) );
         session.setSessionId( s.getSessionId() );
 
         // Set attributes
         UserSessionAttributes attributes = new UserSessionAttributes();
         attributes.setAcceptLanguage( s.getAcceptLanguage() );
         attributes.setIpAddress( s.getIpAddress() );
         attributes.setUserAgent( s.getUserAgent() );
         session.setAttributes( attributes );
 
         // Set user
         StoredUser u = s.getUser();
         if ( u != null )
         {
             session.setUser( ServiceUtils.createUser( u ) );
         }
         return session;
     }
 
     private final UserSessionConfig config;
 
     private PasswordStrategy passwordStrategy = PasswordStrategy.MD5;
 
     private final UserDAO userDao;
 
     private final UserSessionDAO userSessionDao;
 
     /**
      * @param daoFactory Factory instance that creates necessary DAOs
      * @param config Configuration that provides real time settings
      */
     @Autowired
     public DefaultSessionService( DAOFactory daoFactory, UserSessionConfig config )
     {
         this.userSessionDao = daoFactory.createUserSessionDAO();
         this.userDao = daoFactory.createUserDAO();
         this.config = config;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserOperationResult confirmSignUp( String sessionId, String token )
     {
         StoredUser user = userDao.findPendingUserWithToken( token );
         if ( user == null )
         {
             return UserOperationResult.NO_SUCH_IDENTITY;
         }
         user.setActivationToken( null );
         user.setUserState( UserState.ACTIVE );
         userDao.saveUser( user );
         userSessionDao.updateUser( sessionId, user );
         return UserOperationResult.SUCCESSFUL;
     }
 
     private StoredUser createUserForSignUp( User user )
     {
         String userId = UUIDUtils.randomStringId();
         StoredUser storedUser = new StoredUser();
         ServiceUtils.copyUser( user, storedUser );
 
         storedUser.setDomainName( config.getDomainName() );
         storedUser.setUserType( UserType.LOCAL );
         storedUser.setPasswordStrategy( passwordStrategy );
        storedUser.setPassword( passwordStrategy.encode( user.getPassword(), userId ) );
 
         DateTime now = new DateTime();
         storedUser.setCreationDate( now );
         storedUser.setLastModified( now );
        storedUser.setUserId( userId );
         return storedUser;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserSession getSession( String sessionId )
     {
         StoredUserSession s = userSessionDao.pingSession( sessionId );
         if ( s == null )
         {
             return null;
         }
         return createUserSession( s );
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserSession pingSession( String sessionId )
     {
         StoredUserSession session = userSessionDao.pingSession( sessionId );
         if ( session == null )
         {
             throw new IllegalStateException( "Session " + sessionId + " doesn't exist" );
         }
         return createUserSession( session );
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserSignUpResult requestSignUp( String sessionId, User user )
     {
         StoredUser existingUser = userDao.findNonPendingUser( user.getUserName() );
         if ( existingUser != null )
         {
             return new UserSignUpResult( UserOperationResult.IDENTITY_EXISTED, user, null );
         }
         StoredUser storaduser = createUserForSignUp( user );
         String userId = storaduser.getUserId();
         String token = UUIDUtils.randomStringId() + userId;
 
         storaduser.setUserState( UserState.PENDING );
         storaduser.setActivationToken( token );
         userDao.saveUser( storaduser );
 
         LOG.info( "Sign up request " + userId + " is saved" );
         user.setUserId( userId );
         UserSignUpResult result = new UserSignUpResult( UserOperationResult.SUCCESSFUL, user, token );
         config.getListener().signUpRequested( sessionId, result );
         return result;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserOperationResult signIn( String sessionId, String userName, String password )
     {
        StoredUser u = userDao.findNonPendingUser( userName );
        if ( u == null )
         {
             return UserOperationResult.NO_SUCH_IDENTITY;
         }
        if ( !StringUtils.equals( password, u.getPassword() ) )
         {
             return UserOperationResult.AUTHENTICATION_FAILURE;
         }
        userSessionDao.updateUser( sessionId, u );
         return UserOperationResult.SUCCESSFUL;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserOperationResult signOut( String sessionId )
     {
         userSessionDao.updateUser( sessionId, null );
         return UserOperationResult.SUCCESSFUL;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserOperationResult signUp( String sessionId, User user )
     {
         StoredUser existing = userDao.findNonPendingUser( user.getUserName() );
         if ( existing != null )
         {
             return UserOperationResult.IDENTITY_EXISTED;
         }
         StoredUser storedUser = createUserForSignUp( user );
         storedUser.setUserState( UserState.ACTIVE );
 
         userDao.saveUser( storedUser );
         userSessionDao.updateUser( sessionId, storedUser );
         return UserOperationResult.SUCCESSFUL;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     @Transactional
     public UserSession startSession( String sessionId, UserSessionAttributes attributes )
     {
         Validate.notNull( sessionId, "Session ID can't be NULL" );
 
         StoredUserSession s = new StoredUserSession();
         s.setSessionId( sessionId );
         if ( attributes != null )
         {
             s.setAcceptLanguage( attributes.getAcceptLanguage() );
             s.setIpAddress( attributes.getIpAddress() );
             s.setUserAgent( attributes.getUserAgent() );
         }
         userSessionDao.createNew( s );
         return createUserSession( s );
     }
 }
