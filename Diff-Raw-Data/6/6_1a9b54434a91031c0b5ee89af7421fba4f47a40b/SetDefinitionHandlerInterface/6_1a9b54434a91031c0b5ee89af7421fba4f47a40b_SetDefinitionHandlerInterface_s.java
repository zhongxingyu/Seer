 package de.escidoc.core.oai.service.interfaces;
 
 import java.util.Map;
 
 import de.escidoc.core.common.annotation.Validate;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 
 /**
  * Interface of an set definition handler.
  * 
  * @author ROF
  * 
  * @oai
  */
 public interface SetDefinitionHandlerInterface {
     /**
      * Create a set definition resource.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML-Schema of a set-definition.
      * </li>
      * <li>It's checked weather the set specification is unique within the
      * system.</li>
      * <li>Some new data is added to the input xml(see Chapter 4)</li>
      * <li>The XML representation of the set-definition corresponding to
      * XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param setDefinition
      *            The data of the resource.
      * @return The XML representation of the created set-definition
      *         corresponding to XML-schema "set-definition.xsd".
      * @throws UniqueConstraintViolationException
      *             If the specification of the created set-definition is not
      *             unique within the system.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws MissingMethodParameterException
      *             If a set-definition data is missing.
      * @throws SystemException
      *             If an error occurs
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      */
     @Validate(param = 0, resolver = "getSetDefinitionSchemaLocation")
     String create(final String setDefinition)
         throws UniqueConstraintViolationException, InvalidXmlException,
         MissingMethodParameterException, SystemException,
         AuthenticationException, AuthorizationException;
 
     /**
      * Retrieve a set definition.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The set-definition must exist<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The set-definition is accessed using the provided reference.</li>
      * <li>The XML representation of the set-definition corresponding to
      * XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the set-definition to be retrieved.
      * @return The XML representation of the retrieved set-definition
      *         corresponding to XML-schema "set-definition.xsd".
      * @throws ResourceNotFoundException
      *             Thrown if a set-definition with the specified id cannot be
      *             found.
      * @throws MissingMethodParameterException
      *             If a set-definition id is missing.
      * @throws SystemException
      *             If an error occurs.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      *@throws AuthorizationException
      *             Thrown if the authorization fails.
      *@oai
      */
     String retrieve(final String id) throws ResourceNotFoundException,
         MissingMethodParameterException, SystemException,
         AuthenticationException, AuthorizationException;
 
     /**
      * Update an set-definition<br/>
      * <b>Prerequisites:</b> <br/>
      * The set-definition must exist.<br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML-Schema of a set-definition.
      * </li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>If changed, a description and a name are updated.</li>
      * <li>The XML input data is updated.(see Chapter 4)</li>
      * <li>The XML representation of the set-definition corresponding to
      * XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the set-definition to be updated.
      * @param xmlData
      *            The XML representation of the set-definition to be updated
      *            corresponding to XML-schema "set-definition.xsd".
      * @return The XML representation of the updated set-definition
      *         corresponding to XML-schema "set-definition.xsd".
      * @throws ResourceNotFoundException
      *             Thrown if an set-definition with the specified id cannot be
      *             found.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws MissingMethodParameterException
      *             if some of data is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      *@oai
      */
     @Validate(param = 1, resolver = "getSetDefinitionSchemaLocation")
     String update(final String id, final String xmlData)
         throws ResourceNotFoundException, OptimisticLockingException,
         MissingMethodParameterException, SystemException,
         AuthenticationException, AuthorizationException;
 
     /**
      * Delete a set-definition.<br/>
      * <b>Prerequisites:</b><br/>
      * The set-definition must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The set-definition will be deleted from the system.</li>
      * </ul>
      * 
      * @param id
      *            The id of the set-definition to be deleted
      * @throws ResourceNotFoundException
      *             Thrown if a set-definition with the specified id cannot be
      *             found.
      * @throws MissingMethodParameterException
      *             If a set-definition id is missing.
      * @throws SystemException
      *             If an error occurs.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      *@oai
      */
     void delete(final String id) throws ResourceNotFoundException,
         MissingMethodParameterException, SystemException,
         AuthenticationException, AuthorizationException;
 
     /**
      * Retrieves a list of completes set-definitions applying filters.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>Check weather all filter names are valid.</li>
      * <li>The set-definitions are accessed using the provided filters.</li>
      * <li>The XML representation of the list of all set-definitions
      * corresponding to XML-schema is returned as output.</li>
      * </ul>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param filterXml
      *            Simple XML containing the filter definition. See functional
      *            specification.
      * @return Returns the XML representation of found set-definitions with a
      *         surrounding list element.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws InvalidXmlException
      *             If the given xml is not valid.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidContentException
      *             If a filter xml contains not allowed values for filter
      *
     * @deprecated replaced by {@link #rretrieveSetDefinitions(java.util.Map)}
      */
     @Validate(param = 0, resolver = "getFilterSchemaLocation")
     @Deprecated String retrieveSetDefinitions(final String filterXml)
         throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, InvalidContentException,
         InvalidXmlException, SystemException;
 
     /**
      * Retrieves a list of completes set-definitions applying filters.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>Check weather all filter names are valid.</li>
      * <li>The set-definitions are accessed using the provided filters.</li>
      * <li>The XML representation of the list of all set-definitions
      * corresponding to SRW schema is returned as output.</li>
      * </ul>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param filter
      *            Simple XML containing the filter definition. See functional
      *            specification.
      * @return Returns the XML representation of found set-definitions.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws SystemException
      *             If an error occurs.
      */
     String retrieveSetDefinitions(final Map<String, String[]> filter)
         throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, InvalidSearchQueryException,
         SystemException;
}
