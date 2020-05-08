 package uk.ac.cam.db538.dexter.hierarchy;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import lombok.Getter;
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
 
 public class ClassDefinition extends BaseClassDefinition {
 
 	private static final long serialVersionUID = 1L;
 	
 	private final List<InterfaceDefinition> _interfaces;
 	@Getter private final List<InterfaceDefinition> interfaces;
 
 	private final List<InstanceFieldDefinition> _instanceFields;
 	@Getter private final List<InstanceFieldDefinition> instanceFields;
 
 	public ClassDefinition(DexClassType classType, int accessFlags, boolean isInternal) {
 		super(classType, accessFlags, isInternal);
 		
 		this._interfaces = new ArrayList<InterfaceDefinition>();
 		this.interfaces = Collections.unmodifiableList(this._interfaces);
 
 		this._instanceFields = new ArrayList<InstanceFieldDefinition>();
 		this.instanceFields = Collections.unmodifiableList(this._instanceFields);
 	}
 	
 	public void addImplementedInterface(InterfaceDefinition iface) {
 		this._interfaces.add(iface);
 		iface._implementors.add(this);
 	}
 
 	public void addDeclaredInstanceField(InstanceFieldDefinition field) {
 		assert !field.isStatic();
 		assert field.getParentClass() == this;
 		
 		this._instanceFields.add(field);
 	}
 	
 	public boolean implementsInterface(InterfaceDefinition iface) {
 		BaseClassDefinition inspected = this;
 		
 		while (true) {
 			if (inspected instanceof ClassDefinition) {
 				for (val ifaceDef : ((ClassDefinition) inspected).getInterfaces()) 
 					if (ifaceDef.equals(iface))
 						return true;
 			}
 			
 			if (inspected.isRoot())
 				return false;
 			else
 				inspected = inspected.getSuperclass();
 		}  
 	}
 	
 	public InstanceFieldDefinition getInstanceField(DexFieldId fieldId) {
 		for (val fieldDef : this.instanceFields)
 			if (fieldDef.getFieldId().equals(fieldId))
 				return fieldDef;
 		return null;
 	}
 	
 	public InstanceFieldDefinition getAccessedInstanceField(DexFieldId fieldId) {
 		// Application can access an instance field on class X, but
 		// the field might actually be defined in one of X's parents
 		// This method will return the definition of the field 
 		// in itself or the closest parent
 
 		return iterateThroughParents(fieldId, extractorInstanceField, acceptorAlwaysTrue, false);
 	}
 	
 	private static final Extractor<DexFieldId, InstanceFieldDefinition> extractorInstanceField = new Extractor<DexFieldId, InstanceFieldDefinition>() {
 		@Override
 		public InstanceFieldDefinition extract(BaseClassDefinition clazz, DexFieldId fieldId) {
 			if (clazz instanceof ClassDefinition) {
 				return ((ClassDefinition) clazz).getInstanceField(fieldId);
 			} else
 				return null;
 		}
 	};

	@Override
	public StaticFieldDefinition getAccessedStaticField(DexFieldId fieldId) {
		// Extend the implementation of method that explores parents
		// of a class to find the definition of a static field
		// to also explore its interfaces.
		// Example: https://android.googlesource.com/platform/dalvik/+/master/tests/008-instanceof/src
		
		StaticFieldDefinition def = super.getAccessedStaticField(fieldId);
		if (def != null)
			return def;
		
		for (val iface : this.interfaces) {
			def = iface.iterateThroughParents(fieldId, extractorStaticField, acceptorAlwaysTrue, false);
			if (def != null)
				return def;
		}
		
		return null;
	}

	
	
 }
