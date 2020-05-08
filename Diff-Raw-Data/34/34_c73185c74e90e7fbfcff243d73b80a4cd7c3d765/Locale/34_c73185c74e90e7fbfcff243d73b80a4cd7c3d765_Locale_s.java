 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.conn.locales;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.databinding.conversion.Converter;
 
 /**
  * A single SAP Locale as defined by a <tt>locale</tt> element of the extension point 
  * <tt>net.sf.rcer.conn.saplocales</tt>. Use the {@link LocaleRegistry} to obtain instances of this class.
  * @author vwegert
  *
  */
 public class Locale {
 
 	/**
 	 * The internal ID used by the SAP R/3 system (usually a single character, case sensitive).
 	 */
 	private String id;
 
 	/**
 	 * The language code as specified by ISO 639 (usually two characters).
 	 */
 	private String isoCode;
 
 	/**
 	 * The human-readable description of the locale.
 	 */
 	private String description;
 
 	/**
 	 * The default constructor as used by {@link LocaleRegistry}.
 	 * @param id
 	 * @param isoCode
 	 * @param description
 	 */
 	Locale(String id, String isoCode, String description) {
 		super();
 		this.id = id;
 		this.isoCode = isoCode;
 		this.description = description;
 	}
 
 	/**
 	 * @return the internal ID used by the SAP R/3 system (usually a single character, case sensitive).
 	 */
 	public String getID() {
 		return id;
 	}
 
 	/**
 	 * @return the language code as specified by ISO 639 (usually two characters).
 	 */
 	public String getISOCode() {
 		return isoCode;
 	}
 
 	/**
 	 * @return the human-readable description of the locale.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param id the internal ID used by the SAP R/3 system (usually a single character, case sensitive) to set
 	 */
 	void setID(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @param isoCode the language code as specified by ISO 639 (usually two characters) to set
 	 */
 	void setISOCode(String isoCode) {
 		this.isoCode = isoCode;
 	}
 
 	/**
 	 * @param description the human-readable description of the locale to set
 	 */
 	void setDescription(String description) {
 		this.description = description;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return MessageFormat.format("{0} ({1})", getISOCode(), getDescription());
 	}
 
 
 	/**
 	 * A {@link Converter} that implements the {@link String} to {@link Locale} conversion.
 	 * @author vwegert
 	 */
	public static class ToStringConverter extends Converter {
 
 		/**
 		 * Default constructor.
 		 */
		public ToStringConverter() {
 			super(String.class, Locale.class);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
 		 */
 		public Object convert(Object fromObject) {
 			if (fromObject == null) {
 				return null;
 			} else if (fromObject instanceof String) {
 				String isoCode = (String) fromObject;
 				if (isoCode.equals("")) {
 					return null;
 				}
 				try {
 					return LocaleRegistry.getInstance().getLocaleByISO(isoCode);
 				} catch (LocaleNotFoundException e1) {
					// maybe this was a string that begins with the locale code (see FromStringConverter)
 					try {
 						return LocaleRegistry.getInstance().getLocaleByISO(isoCode.substring(0, isoCode.indexOf(' ')));
 					} catch (LocaleNotFoundException e) {
 						throw new IllegalArgumentException(e);
 					}
 				}
 			}
 			throw new IllegalArgumentException(MessageFormat.format("Unsupported value {0}", fromObject));
 		}
 	}
 
 	/**
 	 * A {@link Converter} that implements the {@link Locale} to {@link String} conversion.
 	 * @author vwegert
 	 *
 	 */
	public static class FromStringConverter extends Converter  {
 
 		private boolean includeDescription;
 
 		/**
 		 * Default constructor.
 		 * @param includeDescription whether to include the description in the output text. 
 		 */
		public FromStringConverter(boolean includeDescription) {
 			super(Locale.class, String.class);
 			this.includeDescription = includeDescription;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
 		 */
 		public Object convert(Object fromObject) {
 			if (fromObject == null) {
 				return "";
 			} else if (fromObject instanceof Locale) {
 				if (includeDescription) {
 					final Locale locale = (Locale) fromObject; 
 					return MessageFormat.format("{0} ({1})", locale.getISOCode(), locale.getDescription());
 				}
 				return ((Locale) fromObject).getISOCode();
 			}
 			throw new IllegalArgumentException(MessageFormat.format("Unsupported class {0}", fromObject.getClass().getName()));
 		}
 
 	}
 
 
 }
