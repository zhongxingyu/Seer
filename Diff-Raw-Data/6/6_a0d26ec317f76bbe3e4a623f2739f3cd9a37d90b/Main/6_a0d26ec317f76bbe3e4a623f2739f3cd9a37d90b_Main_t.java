 /**
  * Copyright (C) 2010  Marcellus C. Tavares
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
 import org.orcas.iocl.expression.imperativeocl.OclExpression;
 import org.orcas.ioclengine.IOCLEngine;
 import org.orcas.ioclgenerator.IOCLGenerator;
 
 import java.io.File;
 import java.io.IOException;
import java.util.HashMap;
 import java.util.Map;
 
 public class Main {
 
 	public static void saveXML(OclExpression exp) throws IOException {
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		Map<String, Object> map = 
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap();
 
 		map.put("xml", new  XMLResourceFactoryImpl());
 
 		File file = new File("expression.xml");
 
 		URI fileUri = URI.createFileURI(file.getAbsolutePath());
 
 		Resource resource = resourceSet.createResource(fileUri);
 
 		resource.getContents().add(exp);
 
 		resource.save(null);
 	}
 
 	public static void main(String[] args) throws Exception {
 		 OclExpression exp = IOCLEngine.parse(
 			 null, "a := 'Hello World!'; ");
 
 		 // saveXML(exp);
 
		 Map<String, String> context = new HashMap<String, String>();

		 System.out.println(IOCLGenerator.generate(context, exp));
 	}
 
 }
