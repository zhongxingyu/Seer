 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.wordcount.common;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.ClassUtil;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 
 /**
  * 
  * 
  * @version 0.1 07.07.2009
  */
 
 abstract public class BaseCounter {
 	
 	private static BaseCounter counter = null;	
 	private static Logger logger = null;
 	
 	abstract protected long doGetCount(String text, LocaleId language);
 	
 	protected static long getCount(Class<? extends BaseCounter> classRef, Object text, LocaleId language) {
 	
 		if (text == null) return 0L;
 		if (Util.isNullOrEmpty(language)) return 0L;
 		
 		if (text instanceof TextUnit) {
 		
 			TextUnit tu = (TextUnit) text;
 			
 			if (tu.hasTarget(language))
 				return getCount(classRef, tu.getTarget(language), language);
 			else
 				return getCount(classRef, tu.getSource(), language);
 		} 
 		else if (text instanceof TextContainer) {
			
			TextContainer tc = (TextContainer) text;
			if ( tc.contentIsOneSegment() ) {
				return getCount(classRef, tc.getFirstPartContent(), language);
			}
			else {
				return getCount(classRef, tc.getUnSegmentedContentCopy(), language);
 			}
 		}
 		else if (text instanceof TextFragment) {
 			
 			TextFragment tf = (TextFragment) text;
 			
 			return getCount(classRef, TextUnitUtil.getText(tf), language);
 		}
 		else if (text instanceof String) {
 			
 			instantiateCounter(classRef);
 			if (counter == null) return 0L;
 			
 			return counter.doGetCount((String) text, language);
 		}
 		
 		return 0;		
 	}
 	
 	protected static void instantiateCounter(Class<? extends BaseCounter> classRef) {
 		
 		if (counter != null) return; // Already instantiated
 		
 		try {
 			counter = (BaseCounter) classRef.newInstance();
 			
 		} catch (InstantiationException e) {
 			
 			logMessage(classRef, Level.FINE, "Counter instantiation failed: " + e.getMessage());
 			
 		} catch (IllegalAccessException e) {
 			
 			logMessage(classRef, Level.FINE, "Counter instantiation failed: " + e.getMessage());
 		}
 	}
 	
 	protected static void logMessage(Class<? extends BaseCounter> classRef, Level level, String text) {
 		
 		if (logger == null) 
 			logger = Logger.getLogger(ClassUtil.getClassName(classRef));
 		
 		if (logger != null)
 			logger.log(level, text);
 	}
 	
 }
