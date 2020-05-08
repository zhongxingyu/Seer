 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.util.Map;
 
 /**
  * Interface for API function calling functionality.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.33
  */
 public interface FunctionCaller {
 
    /**
     * Calls the specified session-less API function, with no parameters.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     *
     * @throws IOException
     *    if the API could not be contacted due to an I/O error.
     *
     * @throws InvalidCallResultException
     *    if the calling of the function failed or if the result from the
     *    function was invalid.
     */
    CallResult call(String functionName)
   throws IllegalArgumentException, IOException, InvalidCallResultException;
 
    /**
     * Calls the specified session-less API function with the specified
     * parameters.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed to that function, or
     *    <code>null</code>; keys must be {@link String Strings}, values can be
     *    of any class.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     *
     * @throws IOException
     *    if the API could not be contacted due to an I/O error.
     *
     * @throws InvalidCallResultException
     *    if the calling of the function failed or if the result from the
     *    function was invalid.
     */
    CallResult call(String functionName,
                    Map    parameters)
    throws IllegalArgumentException, IOException, InvalidCallResultException;
 
    /**
     * Calls the specified API function with the specified parameters.
     *
     * @param sessionID
     *    the session identifier, if any, or <code>null</code> if the function
     *    is session-less.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed to that function, or
     *    <code>null</code>; keys must be {@link String Strings}, values can be
     *    of any class.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     *
     * @throws IOException
     *    if the API could not be contacted due to an I/O error.
     *
     * @throws InvalidCallResultException
     *    if the calling of the function failed or if the result from the
     *    function was invalid.
     */
    CallResult call(String sessionID,
                    String functionName,
                    Map    parameters)
    throws IllegalArgumentException, IOException, InvalidCallResultException;
 
    /**
     * Executes the specified request.
     *
     * @param request
     *    the request to execute, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws IOException
     *    if the API could not be contacted due to an I/O error.
     *
     * @throws InvalidCallResultException
     *    if the calling of the function failed or if the result from the
     *    function was invalid.
     */
    CallResult call(CallRequest request)
    throws IllegalArgumentException, IOException, InvalidCallResultException;
 }
