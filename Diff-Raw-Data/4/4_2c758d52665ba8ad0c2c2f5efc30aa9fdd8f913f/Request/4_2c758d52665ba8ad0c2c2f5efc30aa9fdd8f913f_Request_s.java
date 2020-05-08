 /*
  * $Id$
  */
 package org.xins.util.service.ldap;
 
 /**
  * LDAP search request. Combines
 * {@link LDAPServiceCaller.AuthenticationDetails authentication details}
 * and a {@link LDAPServiceCaller.Query query}.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.115
  */
 final class Request extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Request</code> object.
     *
     * @param authenticationDetails
     *    the authentication details, can be <code>null</code>.
     *
     * @param query
     *    the query to be executed, can be <code>null</code>.
     */
    Request(AuthenticationDetails authenticationDetails, Query query) {
       _authenticationDetails = authenticationDetails;
       _query                 = query;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The authentication details. Can be <code>null</code>.
     */
    private final AuthenticationDetails _authenticationDetails;
 
    /**
     * The query. Can be <code>null</code>.
     */
    private final Query _query;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the authentication details.
     *
     * @return
     *    the authentication details, can be <code>null</code>.
     */
    AuthenticationDetails getAuthenticationDetails() {
       return _authenticationDetails;
    }
 
    /**
     * Returns the query.
     *
     * @return
     *    the qeury, can be <code>null</code>.
     */
    Query getQuery() {
       return _query;
    }
 }
