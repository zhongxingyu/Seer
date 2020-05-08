 package uk.ac.cam.db538.dexter.dex.field;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.AnnotationDirectoryItem;
 import org.jf.dexlib.ClassDataItem.EncodedField;
 import org.jf.dexlib.ClassDefItem;
 import org.jf.dexlib.EncodedValue.EncodedValue;
 
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;
 
 public class DexStaticField extends DexField {
 
 	@Getter private final StaticFieldDefinition fieldDef;
 	@Getter private final EncodedValue initialValue; 
 
 	public DexStaticField(DexClass parentClass, StaticFieldDefinition fieldDef, EncodedValue initialValue) {
 		super(parentClass);
 		this.initialValue = initialValue;
 		this.fieldDef = fieldDef;
 	}
 	
 	public DexStaticField(DexClass parentClass, ClassDefItem classItem, EncodedField fieldItem, int fieldIndex, AnnotationDirectoryItem annoDir) {
 		super(parentClass, fieldItem, annoDir);
 		
 		this.initialValue = init_ParseInitialValue(classItem, fieldIndex);
 		this.fieldDef = init_FindFieldDefinition(parentClass, fieldItem);
 	}
 	
 	private static StaticFieldDefinition init_FindFieldDefinition(DexClass parentClass, EncodedField fieldItem) {
 		val hierarchy = parentClass.getParentFile().getHierarchy();
 		val classDef = parentClass.getClassDef();
 		
 		val name = fieldItem.field.getFieldName().getStringValue();
 		val type = DexRegisterType.parse(fieldItem.field.getFieldType().getTypeDescriptor(), hierarchy.getTypeCache()); 
 		
 		val fieldId = DexFieldId.parseFieldId(name, type, hierarchy.getTypeCache());
 		return classDef.getStaticField(fieldId);
 	}
 	
 	private static EncodedValue init_ParseInitialValue(ClassDefItem classItem, int fieldIndex) {
 		// extract data
 		val initValuesItem = classItem.getStaticFieldInitializers();
 		if (initValuesItem == null)
 			return null;
 		
 		// return the value
 		val initValues = initValuesItem.getEncodedArray().values;
		return initValues[fieldIndex];
 	}
 
 	@Override
 	protected FieldDefinition internal_GetFieldDef() {
 		return this.fieldDef;
 	}
 }
