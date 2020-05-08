 /*******************************************************************************
  * Copyright 2011 Lance Feagan
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package net.vectorcomputing.net.serialization.xml.java.lang;
 
 import net.vectorcomputing.property.node.PropertyNode;
 import net.vectorcomputing.serialization.xml.IXmlSerializerDescriptor;
 import net.vectorcomputing.serialization.xml.XmlSerializer;
 
 import org.eclipse.core.runtime.CoreException;
 
 public class StringXmlSerializer implements XmlSerializer {
 
 	@Override
 	public PropertyNode toPropertyNode(final Object object, final IXmlSerializerDescriptor descriptor) throws CoreException {
 		String value = (String) object;
 		final PropertyNode pnode = new PropertyNode(descriptor.getTag(), value.toString());
 		return pnode;
 	}
 
 	@Override
 	public Object read(final PropertyNode pnode, final IXmlSerializerDescriptor descriptor) {
		return pnode.getValue();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		return (StringXmlSerializer.class.equals(obj.getClass()));
 	}
 	
 }
