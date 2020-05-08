 package org.eclipse.robotml.generators.acceleo.mmqueries;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.uml2.uml.Port;
 import org.eclipse.uml2.uml.DataType;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.Model;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.PrimitiveType;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.Type;
 
 public class DataTypeQueries{
 	
 	public List<DataType> getMetamodelComposedDataTypes(Element rootelt) {
 		LinkedList<DataType> results = new LinkedList<DataType>();
 		if (rootelt instanceof Model) {
 			EList<Package> packages = ((Model)rootelt).getImportedPackages();
 			for (Package p : packages ) {
 				EList<Element> elts = p.getOwnedElements();
 				for (Element elt : elts) {
 					if (elt instanceof PrimitiveType) {
 					} else if (elt instanceof DataType)  {
 						results.add((DataType)elt);
 					} else if (elt instanceof Package) {
 						results.addAll(getMetamodelComposedDataTypes(elt));
 					}
 				}
 			}
 		} else if (rootelt instanceof Package) {
 			Package p = (Package)rootelt;
 			EList<Element> elts = p.getOwnedElements();
 			for (Element elt : elts) {
 				if (elt instanceof PrimitiveType) {
 				} else if (elt instanceof DataType)  {
 					results.add((DataType)elt);
 				} else if (elt instanceof Package) {
 					results.addAll(getMetamodelComposedDataTypes(elt));
 				}
 			}			
 		}
 		return results;
 	}
 	
 	/**
 	 * Is the provided DataType one of the RobotML metamodel datatypes ?
 	 * @param dt
 	 * @return
 	 */
 	public boolean isRobotMLDataType(Element root_model, String datatype_name) {
 		try {
 		if (root_model instanceof Model) {
 			EList<Package> packages = ((Model)root_model).getImportedPackages();
 			for (Package p : packages ) {
 				EList<Element> elts = p.getOwnedElements();
 				for (Element elt : elts) {
 					if (elt instanceof DataType && datatype_name.compareTo(((DataType)elt).getName())==0)  {
 						return true;
 					} else if (elt instanceof Package) {
 						if (isRobotMLDataType(elt,datatype_name))
 							return true;
 					}
 				}
 			}
 		} else if (root_model instanceof Package) {
 			EList<Element> elts = ((Package)root_model).getOwnedElements();
 			for (Element elt : elts) {
 				if (elt instanceof DataType && datatype_name.compareTo(((DataType)elt).getName())==0)  {
 					return true;
 				} else if (elt instanceof Package) {
 					if (isRobotMLDataType(elt,datatype_name))
 						return true;
 				}
 			}			
 		}
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 		return false;
 	}
 	
 	public String getCppClassForDatatType(DataType dt) {
 		try {
 		String s = "class " + dt.getName() + " {\n\tpublic:\n";
 		EList<Property> members = dt.getAllAttributes();			
 		for (Property member : members) {
 			String type_name;
 			if (member.getType() != null)
 				type_name = member.getType().getName();
 			else
 				type_name = "Unknown_NULL_IN_MODEL";
 			if (member.getUpper()<0) {
 				s+="\tstd::vector<" + type_name + "> " + member.getName() + ";\n";
			} else if (member.getUpper() <= 1){
 				s+="\t" + type_name + " " + member.getName() + ";\n";
			} else if (member.getUpper() == member.getLower()) {
				s+="\t" + type_name + " " + member.getName() + "[" + member.getUpper() + "];\n";
			} else {
				s+="\tstd::vector<" + type_name + "> " + member.getName() + ";\n";
 			}
 		}
 		s+= "};\n";
 		return s;
 		} catch (Exception e) {
 			java.lang.System.out.println(e.getMessage());
 		}
 		return null;
 	}
 	/**
 	 * Get all user-defined datatypes.
 	 * @return A list of datatypes
 	 */
 	public List<NamedElement> getElementsDataType(Model model)
 	{		
 		LinkedList<NamedElement> found_elts = new LinkedList<NamedElement>();
 		for (NamedElement ne : model.getOwnedMembers())
 		{
 			if (ne instanceof org.eclipse.uml2.uml.DataType) {
 				found_elts.add((NamedElement)ne);
 			}
 			getElementsDataType(ne,found_elts);
 
 		}
 		return found_elts;
 	}
 	
 	/**
 	 * Recursive sub-function to browse model and get all user-defined datatypes.
 	 */
 	private void getElementsDataType(Element parent_elt, LinkedList<NamedElement> target_list)
 	{
 		for (Element ne : parent_elt.getOwnedElements())
 		{
 			if (ne instanceof org.eclipse.uml2.uml.DataType)
 			{
 				target_list.add((NamedElement)ne);
 			}
 			getElementsDataType(ne,target_list);
 		}
 		return;
 	}
 	
 	public DataType getPortDataType(Port port) {
 		Type t = port.getType();
 		if (t!= null && t instanceof DataType) {
 			return (DataType)t;
 		}
 		return null;
 	}
 	
 	public boolean isPrimitiveType(Element elt) {
 		if (elt instanceof PrimitiveType)
 			return true;
 		return false;
 	}
 }
