 /*******************************************************************************
  * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.databinding;
 
 import java.text.ParsePosition;
 
import com.ibm.icu.text.NumberFormat;

 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 
 /**
  * Validator for integers.
  */
 public class NumberValidator implements IValidator {
 	
 	
 	private NumberFormat fFormatter;
 	private int fMin;
 	private int fMax;
 	private boolean fAllowEmpty;
 	private String fMessage;
 	
 	
 	public NumberValidator(final int min, final int max, final String message) {
 		this(min, max, false, message);
 	}
 	
 	public NumberValidator(final int min, final int max, final boolean allowEmpty, final String message) {
 		fMin = min;
 		fMax = max;
 		fAllowEmpty = allowEmpty;
 		fMessage = message;
 		fFormatter = NumberFormat.getIntegerInstance();
 		fFormatter.setParseIntegerOnly(true);
 	}
 	
 	
 	public IStatus validate(final Object value) {
 		if (value instanceof String) {
 			final String s = ((String) value).trim();
 			if (fAllowEmpty && s.length() == 0) {
 				return Status.OK_STATUS;
 			}
 			final ParsePosition result = new ParsePosition(0);
 			final Number number = fFormatter.parse(s, result);
 			if (result.getIndex() == s.length() && result.getErrorIndex() < 0) {
 				final int n = number.intValue();
 				if (n >= fMin && n <= fMax) {
 					return Status.OK_STATUS;
 				}
 				// return range message
 			}
 		}
 		return ValidationStatus.error(fMessage);
 	}
 	
 }
