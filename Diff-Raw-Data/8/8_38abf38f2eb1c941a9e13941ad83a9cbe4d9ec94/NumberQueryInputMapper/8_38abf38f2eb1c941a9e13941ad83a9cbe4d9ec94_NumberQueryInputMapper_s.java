 package com.flexive.shared.value.mapper;
 
 import com.flexive.shared.value.*;
 import com.flexive.shared.search.query.ValueComparator;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxProperty;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.content.FxPK;
 import com.google.common.collect.Lists;
 
 import java.util.List;
 import java.util.Arrays;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * An abstract input mapper between discrete values (e.g. IDs) and unique String representations.
  * For example, this can be used to map account IDs to login names.
  * This is also used for most auto-complete inputs for flexive structure propreties.
  *
  * @param <T>   the actual value type of the FxValue implementation (e.g. Long)
  * @param <BaseType>    the FxValue class to be used as input (e.g. FxLargeNumber or FxReference)
  *
  * @author Daniel Lichtenberger, UCS
  * @version $Rev$
  */
 public abstract class NumberQueryInputMapper<T, BaseType extends FxValue<T, ?>> extends InputMapper<BaseType, FxString> {
     /**
      * Maps account IDs to login names. The autocomplete options are provided by
      * com.flexive.faces.javascript.AutoCompleteProvider#userQuery().
      */
     public static class AccountQueryInputMapper extends NumberQueryInputMapper<Long, FxLargeNumber> {
         private static final long serialVersionUID = 6817350554032949604L;
 
         public AccountQueryInputMapper() {
             buildAutocompleteHandler("AutoCompleteProvider.userQuery");
         }
 
         @Override
         public String encodeId(Long id) {
             if (id == null) {
                 return "";
             }
             try {
                 return EJBLookup.getAccountEngine().load(id).getLoginName();
             } catch (FxApplicationException e) {
                 return "";  // account does not exist or cannot be loaded - ignore and return an empty value
             }
         }
 
         @Override
         protected FxLargeNumber doDecode(FxString value) {
             if (value.isMultiLanguage()) {
                 final FxLargeNumber decoded = new FxLargeNumber(value.getDefaultLanguage(), decodeQuery(value, value.getDefaultLanguage()));
                 for (long languageId : value.getTranslatedLanguages()) {
                     final Long result = decodeQuery(value, languageId);
                     if (result != null) {
                         decoded.setTranslation(languageId, result);
                     }
                 }
                 return decoded;
             }
             final Long result = decodeQuery(value, -1);
             return applySettings(
                     result != null ? new FxLargeNumber(false, result) : new FxLargeNumber(false, FxLargeNumber.EMPTY).setEmpty(),
                     value
             );
         }
 
         private Long decodeQuery(FxString value, long languageId) {
             final String query = languageId != -1 ? value.getTranslation(languageId) : value.getDefaultTranslation();
             if (StringUtils.isNotBlank(query)) {
                 try {
                     return EJBLookup.getAccountEngine().load(query).getId();
                 } catch (FxApplicationException e) {
                     return null; // fail silently - reset input
                 }
             }
             return null;
         }
     }
 
     public static class ReferenceQueryInputMapper extends NumberQueryInputMapper<ReferencedContent, FxReference> {
         private static final long serialVersionUID = -4520178265422619418L;
 
         /** Separator between primary key and caption */
         private static final String SEP_PK_CAPTION = " - ";
 
         public ReferenceQueryInputMapper(FxProperty property) {
             buildAutocompleteHandler("AutoCompleteProvider.pkQuery", property.getReferencedType() != null
                     ? String.valueOf(property.getReferencedType().getId()): "-1");
         }
 
         public static ReferencedContent getReferencedContent(String query) {
             if (StringUtils.isBlank(query)) {
                 return new ReferencedContent();
             }
             final int sepIndex = query.indexOf(SEP_PK_CAPTION);
             if (sepIndex == -1) {
                 // not properly formatted, return a temporary reference that uses the entire query in the caption
                 return new ReferencedContent(new FxPK(-1), query, null, Lists.<ACL>newArrayList());
             }
            final FxPK pk = FxPK.fromString(query.substring(0, sepIndex));
             return new ReferencedContent(pk, query.substring(sepIndex + SEP_PK_CAPTION.length()), null, Lists.<ACL>newArrayList());
         }
 
         @Override
         public String encodeId(ReferencedContent rc) {
             if (rc == null || rc.getId() == -1) {
                 return "";
             }
             return rc.getId() + "." + rc.getVersion() + SEP_PK_CAPTION + rc.getCaption();
         }
 
         @Override
         protected FxReference doDecode(FxString value) {
             if (value.isMultiLanguage()) {
                 final FxReference reference = createReference(value);
                 for (long languageId : value.getTranslatedLanguages()) {
                     final ReferencedContent referencedContent = getReferencedContent(value.getTranslation(languageId));
                     if (!referencedContent.isNew()) {
                         reference.setTranslation(languageId, referencedContent);
                     }
                 }
                 return reference;
             } else {
                 return createReference(value);
             }
         }
 
         private FxReference createReference(FxString value) {
             final ReferencedContent rc = getReferencedContent(value.getDefaultTranslation());
             final FxReference reference = value.isMultiLanguage()
                     ? new FxReference(value.getDefaultLanguage(), rc)
                     : new FxReference(false, rc);
             // FX-360 - set empty flag on decoded value
             if (rc.isNew()) {
                 reference.setEmpty();
             }
             return applySettings(reference, value);
         }
     }
 
     public abstract String encodeId(T id);
 
     @Override
     protected FxString doEncode(BaseType value) {
         if (value.isMultiLanguage()) {
             final FxString encoded = new FxString(value.getDefaultLanguage(), encodeId(value.isEmpty() ? null : value.getDefaultTranslation()));
             for (long languageId : value.getTranslatedLanguages()) {
                 encoded.setTranslation(languageId, encodeId(value.getTranslation(languageId)));
             }
             return applySettings(encoded, value);
         } else {
             return applySettings(new FxString(false, encodeId(value.isEmpty() ? null : value.getDefaultTranslation())), value);
         }
     }
 
     @Override
     public List<? extends ValueComparator> getAvailableValueComparators() {
         return PropertyValueComparator.getAvailable(FxDataType.SelectOne);
     }
 }
