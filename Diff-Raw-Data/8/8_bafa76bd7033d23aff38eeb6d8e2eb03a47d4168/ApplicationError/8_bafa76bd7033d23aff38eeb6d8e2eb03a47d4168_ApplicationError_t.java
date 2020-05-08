 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.core.api.exception;
 
 import static java.util.Arrays.asList;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 
 import edu.utah.further.core.api.constant.ErrorCode;
 import edu.utah.further.core.api.context.Api;
 import edu.utah.further.core.api.xml.XmlNamespace;
 
 /**
  * A web service error message representation.
  * <p>
  * -------------------------------------------------------------------------<br>
  * (c) 2008-2013 FURTHeR Project, AVP Health Sciences IT Office, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-581-4080<br>
  * -------------------------------------------------------------------------
  *
  * @author Oren E. Livne {@code <oren.livne@utah.edu>}
  * @version Oct 10, 2008
  */
 @Api
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(namespace = XmlNamespace.CORE_WS, name = ApplicationError.ENTITY_NAME, propOrder =
 { "code", "message", "arguments" })
 @XmlRootElement(namespace = XmlNamespace.CORE_WS, name = ApplicationError.ENTITY_NAME)
 public final class ApplicationError implements HasErrorCode, HasErrorMessage
 {
 	// ========================= CONSTANTS =================================
 
 	/**
 	 * @serial Serializable version identifier.
 	 */
 	@SuppressWarnings("unused")
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * JAXB name of this entity.
 	 */
 	static final String ENTITY_NAME = "error";
 
 	// ========================= NESTED TYPES ==============================
 
 	// ========================= FIELDS ====================================
 
 	/**
 	 * Error code.
 	 */
	@XmlElement(name = "code", required = true, namespace = XmlNamespace.CORE_WS)
 	private ErrorCode code;
 
 	/**
 	 * Error message.
 	 */
	@XmlElement(name = "message", required = false, namespace = XmlNamespace.CORE_WS)
 	private String message;
 
 	/**
 	 * Error message arguments.
 	 */
 	@XmlElementWrapper(name = "arguments")
	@XmlElement(name = "argument", required = false, namespace = XmlNamespace.CORE_WS)
 	private List<String> arguments;
 
 	// ========================= CONSTRUCTORS ==============================
 
 	/**
 	 * Construct an empty error representation.
 	 */
 	public ApplicationError()
 	{
 		super();
 	}
 
 	/**
 	 * Construct an error representation.
 	 *
 	 * @param code
 	 *            error code
 	 * @param message
 	 *            error message
 	 */
 	public ApplicationError(final ErrorCode code, final String message)
 	{
 		super();
 		this.code = code;
 		this.message = message;
 	}
 
 	/**
 	 * Construct an error representation.
 	 *
 	 * @param code
 	 *            error code
 	 * @param message
 	 *            error message
 	 * @param arguments
 	 *            error message arguments
 	 */
 	public ApplicationError(final ErrorCode code, final String message, final String... arguments)
 	{
 		super();
 		this.code = code;
 		this.message = message;
 		this.arguments = asList(arguments);
 	}
 
 	// ========================= IMPLEMENTATION: Object ====================
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return ReflectionToStringBuilder.toString(this);
 	}
 
 	// ========================= GETTERS & SETTERS =========================
 
 	// ========================= PRIVATE METHODS ===========================
 
 	/**
 	 * Return the code property.
 	 *
 	 * @return the code
 	 */
 	@Override
 	public ErrorCode getCode()
 	{
 		return code;
 	}
 
 	/**
 	 * Set a new value for the code property.
 	 *
 	 * @param code
 	 *            the code to set
 	 */
 	public void setCode(final ErrorCode code)
 	{
 		this.code = code;
 	}
 
 	/**
 	 * Return the message property.
 	 *
 	 * @return the message
 	 */
 	@Override
 	public String getMessage()
 	{
 		return message;
 	}
 
 	/**
 	 * Set a new value for the message property.
 	 *
 	 * @param message
 	 *            the message to set
 	 */
 	public void setMessage(final String message)
 	{
 		this.message = message;
 	}
 
 	/**
 	 * Return the arguments property.
 	 *
 	 * @return the arguments
 	 */
 	public List<String> getArguments()
 	{
 		return arguments;
 	}
 
 	/**
 	 * Set a new value for the arguments property.
 	 *
 	 * @param arguments
 	 *            the arguments to set
 	 */
 	public void setArguments(final List<String> arguments)
 	{
 		this.arguments = arguments;
 	}
 
 	/**
 	 * @param index
 	 * @return
 	 * @see java.util.List#get(int)
 	 */
 	public String getArgument(final int index)
 	{
 		return arguments.get(index);
 	}
 
 	// ========================= PRIVATE METHODS ===========================
 }
