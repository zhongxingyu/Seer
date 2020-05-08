 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.richbeans.xml;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.content.IContentDescriber;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.ITextContentDescriber;
 
 /**
  * Tests XML Content for starting with a tag.
  * 
  * Highly linked to the castor XML at the moment as it 
  * reads the second line in the file to know the base tag.
  */
 public abstract class XMLBeanContentDescriber implements IContentDescriber, ITextContentDescriber {
 
 	/**
 	 * Registers the describer with the factory.
 	 */
 	public XMLBeanContentDescriber() {
 		XMLBeanContentDescriberFactory.getInstance().addDescriber(this);
 	}
 	
 	protected abstract String getBeanName();
 	
 	/**
 	 * ID must match editor class name and id of editor in extension registry
 	 * 
 	 * @return editor id
 	 */
 	protected abstract String getEditorId();
 	
 	@Override
 	public int describe(Reader contents, IContentDescription description) throws IOException {
 		
 		final BufferedReader reader = new BufferedReader(contents);
 		try {
 			// TODO Use castor to read the file and use instanceof
 			// on the bean type returned.
			reader.readLine(); // don't use the first line
 			final String tagLine   = reader.readLine();
 			final String tagName   = getTagName();
			if (tagLine.trim().equalsIgnoreCase("<"+tagName+">")||tagLine.trim().equalsIgnoreCase("<"+tagName+"/>")) {
 				return IContentDescriber.VALID;
			}
 			return IContentDescriber.INVALID;
 		} finally {
 			reader.close();
 		}
 	}
 	@Override
 	public int describe(InputStream contents, IContentDescription description) throws IOException {
 		return describe(new InputStreamReader(contents, "UTF-8"), description);
 	}
 
 	private String getTagName() {
 		String beanName = getBeanName();
 		return beanName.substring(beanName.lastIndexOf(".")+1);
 	}
 
 	@Override
 	public QualifiedName[] getSupportedOptions() {
 		return IContentDescription.ALL;
 	}
 
 }
