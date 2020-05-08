 package uk.ac.cam.db538.dexter.dex;
 
 import java.util.List;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.FieldIdItem;
 import org.jf.dexlib.MethodIdItem;
 import org.jf.dexlib.ProtoIdItem;
 import org.jf.dexlib.StringIdItem;
 import org.jf.dexlib.TypeIdItem;
 import org.jf.dexlib.TypeListItem;
 
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexType;
 import uk.ac.cam.db538.dexter.utils.Cache;
 import uk.ac.cam.db538.dexter.utils.Triple;
 
 public class DexAssemblingCache {
 
   @Getter private final DexParsingCache parsingCache;
   private final Cache<DexType, TypeIdItem> types;
   private final Cache<List<DexRegisterType>, TypeListItem> typeLists;
   private final Cache<String, StringIdItem> stringConstants;
   private final Cache<DexPrototype, ProtoIdItem> prototypes;
   private final Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem> fields;
   private final Cache<Triple<DexReferenceType, DexPrototype, String>, MethodIdItem> methods;
 
   public DexAssemblingCache(final DexFile outFile, DexParsingCache parsingCache) {
     this.parsingCache = parsingCache;
 
     val cache = this;
 
     types = DexType.createAssemblingCache(this, outFile);
     typeLists = DexType.createAssemblingCacheForLists(this, outFile);
 
     stringConstants = new Cache<String, StringIdItem>() {
       @Override
       protected StringIdItem createNewEntry(String constant) {
         return StringIdItem.internStringIdItem(outFile, constant);
       }
     };
 
     prototypes = DexPrototype.createAssemblingCache(cache, outFile);
     fields = DexField.createAssemblingCache(cache, outFile);
     methods = DexMethod.createAssemblingCache(cache, outFile);
   }
 
   public TypeIdItem getType(DexType key) {
     return types.getCachedEntry(key);
   }
 
   public TypeListItem getTypeList(List<DexRegisterType> key) {
	// According to http://source.android.com/tech/dalvik/dex-format.html
	// empty type_list should be null, not a 0-length list
	if (key.size() == 0)
		return null;
	else
        return typeLists.getCachedEntry(key);
   }
 
   public StringIdItem getStringConstant(String key) {
     return stringConstants.getCachedEntry(key);
   }
 
   public ProtoIdItem getPrototype(DexPrototype prototype) {
     return prototypes.getCachedEntry(prototype);
   }
 
   public FieldIdItem getField(DexClassType classType, DexRegisterType fieldType, String name) {
     return fields.getCachedEntry(new Triple<DexClassType, DexRegisterType, String>(classType, fieldType, name));
   }
 
   public MethodIdItem getMethod(DexReferenceType classType, DexPrototype methodPrototype, String name) {
     return methods.getCachedEntry(new Triple<>(classType, methodPrototype, name));
   }
 }
