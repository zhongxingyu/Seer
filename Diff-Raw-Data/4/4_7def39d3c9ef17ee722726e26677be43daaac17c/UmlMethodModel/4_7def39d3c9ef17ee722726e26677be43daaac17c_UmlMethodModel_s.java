 package domain.UmlClass;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class UmlMethodModel {
 	private AccessModifier _accessModifier;
 	private String _returnType;
 	private String _name;
 	private List<UmlAttributeModel> _parameters;
 	
 	public UmlMethodModel() {
 		this(AccessModifier.Public, "Object", "newMethod", new ArrayList<UmlAttributeModel>());
 	}
 	
	public UmlMethodModel(AccessModifier accessMod, String _returnType, String name, List<UmlAttributeModel> params) {
 		this._accessModifier = accessMod;
 		this._name = name;
 		this._parameters = params;
 	}
 	
 	public AccessModifier getAccessModifier() {
 		return _accessModifier;
 	}
 	
 	public void setAccessModifier(AccessModifier accessModifier) {
 		this._accessModifier = accessModifier;
 	}
 	
 	public String getReturnType() {
 		return _returnType;
 	}
 	
 
 	public void setReturnType(String returnType) {
 		this._returnType = returnType;
 	}
 
 	public String getName() {
 		return _name;
 	}
 	
 	public void setName(String name) {
 		this._name = name;
 	}
 	
 	public List<UmlAttributeModel> getParameters() {
 		return _parameters;
 	}
 	
 	public void setParameters(List<UmlAttributeModel> parameters) {
 		this._parameters = parameters;
 	}
 }
