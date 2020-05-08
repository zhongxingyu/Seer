 package com.rathravane.drumlin.app.accounts;
 
 import java.util.Date;
 import java.util.logging.Logger;
 
 import com.rathravane.till.data.oneWayHasher;
 import com.rathravane.till.data.sha1HmacSigner;
 import com.rathravane.till.data.uniqueStringGenerator;
 import com.rathravane.till.logging.rrLogSetup;
 import com.rathravane.till.nv.rrNvReadable;
 import com.rathravane.till.nv.impl.nvWriteableTable;
 
 /**
  * A user and account manager. The application must provide implementations of
  * the drumlinAccountFactory interface and its associated interfaces for drumlinUser,
  * drumlinAccount, and drumlinApiKey.
  * 
  * @author peter
  *
  * @param <U>
  * @param <A>
  */
 public class drumlinAccountMgr<U extends drumlinUser<A>,A extends drumlinAccount>
 {
 	/**
 	 * Instantiate an account manager given the app's account factory interface.
 	 * 
 	 * @param f
 	 * @param appSpecificSalt
 	 */
 	public drumlinAccountMgr ( drumlinAccountFactory<U,A> f )
 	{
 		fFactory = f;
 	}
 
 	/**
 	 * Close the account manager.
 	 */
 	public void close ()
 	{
 		fFactory.close ();
 	}
 
 	/**
 	 * return true if the account exists
 	 * @param acctKey
 	 * @return true if the account exists
 	 * @throws msModelException
 	 */
 	public boolean accountExists ( String acctKey ) throws drumlinAccountsException
 	{
 		return fFactory.accountExists ( acctKey );
 	}
 
 	/**
 	 * load an account with the given key or throw if it doesn't exist
 	 * @param acctKey
 	 * @return the account object
 	 * @throws drumlinAccountsException 
 	 */
 	public A loadAccount ( String acctKey ) throws drumlinAccountsException
 	{
 		return fFactory.loadAccount ( acctKey );
 	}
 
 	/**
 	 * create an account with the given key
 	 * @param acctKey
 	 * @return the account object
 	 * @throws drumlinAccountsException 
 	 * @throws msModelException
 	 */
 	public A createAccount ( String acctKey ) throws drumlinAccountsException
 	{
 		return createAccount ( acctKey, null );
 	}
 
 	/**
 	 * Create an account with the given key and additional data.
 	 * @param acctKey
 	 * @param addlData
 	 * @return
 	 * @throws drumlinAccountsException
 	 */
 	public A createAccount ( String acctKey, rrNvReadable addlData ) throws drumlinAccountsException
 	{
 		final nvWriteableTable data = new nvWriteableTable ( addlData );
 		data.set ( "accountId", acctKey );
 		return fFactory.createAccount ( acctKey, data );
 	}
 
 	public boolean userExists ( String userId ) throws drumlinAccountsException
 	{
 		return fFactory.userExists ( userId );
 	}
 
 	public U loadUser ( String userId ) throws drumlinAccountsException
 	{
 		return fFactory.loadUser ( userId );
 	}
 
 	/**
 	 * authenticate a user with user id and password
 	 * @param userId
 	 * @param password
 	 * @return the user record
 	 * @throws drumlinAccountsException 
 	 */
 	public U authenticateUserWithPassword ( String userId, String password ) throws drumlinAccountsException
 	{
 		U authenticatedUser = null;
 	
 		final U u = fFactory.loadUser ( userId );
 		if ( u == null )
 		{
 			log.info ( "AUTH_FAIL: no such user " + userId + "." );
 		}
 
 		final A a = fFactory.loadAccount ( u.getAccountId () );
 		if ( a == null )
 		{
 			log.warning ( "AUTH_FAIL: no such account " + u.getAccountId() + ", referenced by " + userId + "." );
 		}
 	
 		if ( !u.isEnabled () )
 		{
 			log.info ( "AUTH_FAIL: " + userId + "; user is disabled." );
 		}
 		else if ( !isPasswordValid ( u, password ) )
 		{
 			log.info ( "AUTH_FAIL: " + userId + "; incorrect password." );
 		}
 		else if ( !a.isEnabled () )
 		{
 			log.info ( "AUTH_FAIL: " + userId + " ; account is disabled." );
 		}
 		else if ( !u.isConfirmed () )
 		{
 			log.info ( "AUTH_FAIL: " + userId + " ; user did not confirm on time." );
 		}
 		else
 		{
 			authenticatedUser = u;
 		}
 		return authenticatedUser;
 	}
 
 	public U createUser ( A acct, String userKey, String password, String userEmail ) throws drumlinAccountsException
 	{
 		return createUser ( acct, userKey, password, userEmail, null );
 	}
 
 	public U createUser ( A acct, String userId, String password, String userEmail, rrNvReadable addlData ) throws drumlinAccountsException
 	{
 		final U result = fFactory.createUser ( acct, userId, userEmail, addlData );
 		setPassword ( result, password );
 		return result;
 	}
 
 	/**
 	 * Require that the user is confirmed by a particular point in time. If confirmation does not
 	 * occur, the user account is disabled.
 	 * 
 	 * @param u
 	 * @param before
 	 * @param nonsense
 	 * @return a tag identifying the confirmation request (send this in an email, for example)
	 * @throws drumlinAccountsException 
 	 */
	public String requireUserConfirmation ( U u, Date before, String nonsense ) throws drumlinAccountsException
 	{
 		final String confirmTag = uniqueStringGenerator.createUrlKey ( nonsense );
 		u.storeUserConfirmTag ( confirmTag, before.getTime () / 1000 );
 		return confirmTag;
 	}
 
 	/**
 	 * Load and confirm a user record.
 	 * @param userId
 	 * @throws drumlinAccountsException
 	 */
 	public void confirmUser ( String userId ) throws drumlinAccountsException
 	{
 		fFactory.loadUser ( userId ).confirmUser ();
 	}
 	
 	/**
 	 * Confirm a user based on a confirmation tag.
 	 * @param tag
 	 * @throws drumlinAccountsException 
 	 */
 	public void confirmUserWithTag ( String tag ) throws drumlinAccountsException
 	{
 		final U u = fFactory.loadUserWithConfirmRequestTag ( tag );
 		if ( u != null )
 		{
 			u.confirmUser ();
 		}
 	}
 
 	/**
 	 * Set the user's password.
 	 * @param u
 	 * @param pwd
 	 * @throws drumlinAccountsException 
 	 */
 	public void setPassword ( U user, String pwd ) throws drumlinAccountsException
 	{
 		final String nonsense = fFactory.getAppSignature();
 		final String salt = generateSalt ( nonsense );
 		final String hashedPassword = passwdHash ( pwd, salt );
 		user.setPasswordData ( salt, hashedPassword );
 	}
 
 	/**
 	 * Request a password reset request that expires. The return value is a unique tag for the request
 	 * that must be presented to complete the transaction. The password is NOT changed until the
 	 * request is completed.
 	 * 
 	 * @param userKey
 	 * @param secondsUntilExpire
 	 * @param nonsense Arbitrary string to use in generating the returned reset tag.
 	 * @return a unique reset request tag
 	 * @throws drumlinAccountsException 
 	 * @throws msItemDoesNotExistException
 	 * @throws unexpectedException
 	 */
 	public String requestPasswordReset ( String userKey, long secondsUntilExpire, String nonsense ) throws drumlinAccountsException
 	{
 		// validate that the user key is legit, then generate a unique reset key for use in the calling app
 		// to create a URI, for example.
 
 		final U user = loadUser ( userKey );
 		final String tag = uniqueStringGenerator.createUrlKey ( nonsense );
 		final long expiresAtSeconds = ( System.currentTimeMillis () / 1000 ) + secondsUntilExpire;
 
 		user.storePendingPasswordResetRequest ( tag, expiresAtSeconds );
 
 		return tag;
 	}
 
 	/**
 	 * Complete the password reset using the new password. This also cancels any
 	 * pending account confirmation, because it presumably went through the same
 	 * email round-trip mechanism.
 	 * 
 	 * @param requestTag
 	 * @param newPassword
 	 * @throws drumlinAccountsException 
 	 * @throws unexpectedException
 	 */
 	public void completePasswordReset ( String requestTag, String newPassword ) throws drumlinAccountsException
 	{
 		U u = fFactory.loadUserWithPasswordRequestTag ( requestTag );
 		if ( u != null )
 		{
 			setPassword ( u, newPassword );
 			
 			fFactory.deletePasswordRequestTag ( requestTag );
 			u.confirmUser ();
 		}
 	}
 
 	/**
 	 * Load an API key record.
 	 * @param apiKey
 	 * @return an API key or null
 	 * @throws drumlinAccountsException
 	 */
 	public drumlinApiKey loadApiKey ( String apiKey ) throws drumlinAccountsException
 	{
 		return fFactory.loadApiKey ( apiKey );
 	}
 
 	/**
 	 * Authenticate a request using an api (public) key, a string of content that's signed, and the
 	 * signature.
 	 * 
 	 * @param apiKey
 	 * @param signedContent
 	 * @param signature
 	 * @return an API key record, which will indicate whether its an account or user access.
 	 * @throws drumlinAccountsException 
 	 */
 	public drumlinApiKey authenticateViaApiKey ( String apiKey, String signedContent, String signature ) throws drumlinAccountsException
 	{
 		drumlinApiKey result = null;
 
 		final drumlinApiKey key = fFactory.loadApiKey ( apiKey );
 		if ( key != null )
 		{
 			// use private key to sign content
 			final String expectedSignature = sha1HmacSigner.sign ( signedContent, key.getSecret () );
 
 			// compare
 			if ( expectedSignature.equals ( signature ) )
 			{
 				result = key;
 			}
 			// else: not signed correctly
 		}
 		// else: no account
 
 		return result;
 	}
 
 	/**
 	 * Create an API key for the given account.
 	 * @param acct
 	 * @return an API key
 	 * @throws drumlinAccountsException 
 	 */
 	public drumlinApiKey createApiKey ( A acct ) throws drumlinAccountsException
 	{
 		final String appSig = fFactory.getAppSignature();
 		final String newApiKey = generateKey ( 16, appSig );
 		final String newApiSecret = generateKey ( 24, appSig );
 		return establishApiKey ( acct, newApiKey, newApiSecret );
 	}
 
 	/**
 	 * Create an API key for the given user.
 	 * @param user
 	 * @return an API key
 	 * @throws drumlinAccountsException 
 	 */
 	public drumlinApiKey createApiKey ( U user ) throws drumlinAccountsException
 	{
 		final String appSig = fFactory.getAppSignature();
 		final String newApiKey = generateKey ( 16, appSig );
 		final String newApiSecret = generateKey ( 24, appSig );
 		return establishApiKey ( user, newApiKey, newApiSecret );
 	}
 
 	/**
 	 * This is provided for internal use, not use from user-facing code.
 	 * @param acct
 	 * @param apiKey
 	 * @param newApiSecret
 	 * @return
 	 * @throws drumlinAccountsException 
 	 * @throws msModelException
 	 */
 	public drumlinApiKey establishApiKey ( final A acct, final String apiKey, final String newApiSecret ) throws drumlinAccountsException
 	{
 		drumlinApiKey key = null;
 
 		// if the API key exists (mainly used for test), load it and update it
 		key = loadApiKey ( apiKey );
 		if ( key != null && key.getAccount () != null && key.getAccount().equals ( acct.getAccountId () ) )
 		{
 			// update the existing key
 			log.info ( "API key " + apiKey + " already exists for account [" + acct.getAccountId () + "]. Resetting API secret." );
 			fFactory.setApiSecret ( apiKey, newApiSecret );
 		}
 		else
 		{
 			// create a new key
 			key = new drumlinApiKey ()
 			{
 				@Override
 				public String getKey () { return apiKey; }
 
 				@Override
 				public String getSecret () { return newApiSecret; }
 
 				@Override
 				public drumlinApiKey.type getAccessType () { return drumlinApiKey.type.ACCOUNT; }
 
 				@Override
 				public String getAccount () { return acct.getAccountId (); }
 
 				@Override
 				public String getUser () { return null; }
 			};
 			fFactory.storeApiKey ( key );
 		}
 
 		return key;
 	}
 
 	/**
 	 * This is provided for internal use, not use from user-facing code.
 	 * @param acct
 	 * @param apiKey
 	 * @param newApiSecret
 	 * @return
 	 * @throws drumlinAccountsException 
 	 * @throws msModelException
 	 */
 	public drumlinApiKey establishApiKey ( final U user, final String apiKey, final String newApiSecret ) throws drumlinAccountsException
 	{
 		drumlinApiKey key = null;
 
 		// if the API key exists (mainly used for test), load it and update it
 		key = loadApiKey ( apiKey );
 		if ( key != null && key.getUser () != null && key.getUser().equals ( user.getUserId () ) )
 		{
 			// update the existing key
 			log.info ( "API key " + apiKey + " already exists for user [" + user.getUserId () + "]. Resetting API secret." );
 			fFactory.setApiSecret ( apiKey, newApiSecret );
 		}
 		else
 		{
 			// create a new key
 			key = new drumlinApiKey ()
 			{
 				@Override
 				public String getKey () { return apiKey; }
 
 				@Override
 				public String getSecret () { return newApiSecret; }
 
 				@Override
 				public drumlinApiKey.type getAccessType () { return drumlinApiKey.type.USER; }
 
 				@Override
 				public String getAccount () { return null; }
 
 				@Override
 				public String getUser () { return user.getUserId(); }
 			};
 			fFactory.storeApiKey ( key );
 		}
 
 		return key;
 	}
 
 	private final drumlinAccountFactory<U,A> fFactory;
 
 	private boolean isPasswordValid ( U u, String pwd )
 	{
 		boolean result = false;
 		final String hash = passwdHash ( pwd, u.getSalt () );
 		final String stored = u.getHashedPassword ();
 		if ( stored != null )
 		{
 			result = hash.equals ( stored );
 		}
 		return result;
 	}
 
 	private static String passwdHash ( String p, String salt )
 	{
 		return oneWayHasher.hash ( p, salt );
 	}
 
 	private static String generateSalt ( String nonsense )
 	{
 		return generateKey ( 8, nonsense );
 	}
 
 	private static String kKeyChars = "ABCDEFGHJIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
 	private static String generateKey ( int length, String nonsense  )
 	{
 		return uniqueStringGenerator.createKeyUsingAlphabet ( nonsense, kKeyChars, length );
 	}
 
 	private static final Logger log = rrLogSetup.getLog ( drumlinAccountMgr.class );
 }
