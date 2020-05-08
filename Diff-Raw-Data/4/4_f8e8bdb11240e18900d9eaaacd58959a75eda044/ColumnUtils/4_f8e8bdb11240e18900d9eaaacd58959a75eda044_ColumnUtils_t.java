 package jetbrick.schema.app.reader;
 
 import java.io.InputStream;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.util.List;
 import jetbrick.commons.bean.ClassConvertUtils;
 import jetbrick.commons.exception.SystemException;
 import jetbrick.commons.lang.CamelCaseUtils;
 import jetbrick.commons.xml.XmlAttribute;
 import jetbrick.commons.xml.XmlNode;
 import jetbrick.dao.dialect.SqlType;
 import jetbrick.dao.dialect.SubStyleType;
 import jetbrick.dao.schema.validator.Validator;
 import jetbrick.dao.schema.validator.ValidatorFactory;
 import jetbrick.schema.app.model.*;
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang3.StringUtils;
 
 public class ColumnUtils {
 
     public static void addDefaultPrimaryKey(TableInfo table, String typeName) {
         if (StringUtils.isBlank(typeName)) {
             typeName = table.getSchema().getProperty("primary.key.type");
         }
         if (StringUtils.isBlank(typeName)) {
             typeName = SubStyleType.UID;
         }
         SqlType type = SqlType.newInstance(typeName);
 
         TableColumn c = new TableColumn();
         c.setTable(table);
         c.setFieldName("id");
         c.setFieldClass(SubStyleType.getJavaClass(type.getName()));
         c.setColumnName("id");
         c.setTypeName(type.getName());
         c.setTypeLength(type.getLength());
         c.setTypeScale(type.getScale());
         c.setNullable(false);
         c.setDefaultValue(null);
         c.setDisplayName("ID");
         c.setDescription(null);
         c.setPrimaryKey(true);
         c.setJson(true);
 
         table.addColumn(c);
         table.getPrimaryKey().setColumn(c);
     }
 
     public static void mappingColumnList(TableInfo table, XmlNode root) {
         for (XmlNode node : root.elements("column")) {
             TableColumn c = mappingSchemaColumn(table, node);
             table.addColumn(c);
 
             // process enum
             if (SubStyleType.ENUM.equals(c.getTypeName())) {
                 Integer ref = node.attribute("enum-group-ref").asInt();
                 EnumGroup enumGroup;
                 if (ref != null) {
                     enumGroup = table.getSchema().getReferenceEnumGroup(ref);
                     if (enumGroup == null) {
                         String error = String.format("Enum group reference %d is not found in %s.%s.", ref, table.getTableName(), c.getColumnName());
                         throw new RuntimeException(error);
                     }
                 } else {
                     node = node.element("enum-group");
                     if (node == null) {
                         String error = String.format("Missing enum-group node for %s.%s.", table.getTableName(), c.getColumnName());
                         throw new RuntimeException(error);
                     }
                     enumGroup = EnumFileUtils.createSchemaEnumGroup(table.getSchema(), node);
                 }
                 enumGroup.setIdentifier(table.getTableClass() + "_" + CamelCaseUtils.toCapitalizeCamelCase(c.getColumnName()));
                 enumGroup.setDescription(table.getDisplayName() + "->" + c.getDisplayName());
 
                 c.setEnumGroup(enumGroup);
             }
         }
     }
 
     public static TableColumn mappingSchemaColumn(TableInfo table, XmlNode node) {
         TableColumn c = new TableColumn();
         c.setTable(table);
         c.setColumnName(node.attribute("name").asString());
         c.setNullable(node.attribute("nullable").asBool(false));
         c.setDisplayName(node.attribute("display-name").asString());
         c.setDescription(node.attribute("description").asString());
         c.setJson(node.attribute("json").asBool(true));
 
         String typeName = node.attribute("type").asString().toLowerCase();
        String aliasTypeName = table.getSchema().getTypeNameAlias().getProperty(typeName);
        if (aliasTypeName != null) {
            typeName = aliasTypeName;
        }
         SqlType type = SqlType.newInstance(typeName);
         c.setTypeName(type.getName());
         c.setTypeLength(type.getLength());
         c.setTypeScale(type.getScale());
 
         c.setFieldName(CamelCaseUtils.toCamelCase(c.getColumnName()));
         c.setFieldClass(SubStyleType.getJavaClass(type.getName()));
 
         String defaultValue = node.attribute("default").asString();
         if (defaultValue != null) {
             c.setDefaultValue(ClassConvertUtils.convert(defaultValue, c.getFieldClass()));
         }
 
         Class<?> typeClass = c.getFieldClass();
         if (byte[].class.equals(typeClass)) {
             c.setJson(false);
         } else if (InputStream.class.equals(typeClass)) {
             c.setJson(false);
         } else if (Clob.class.equals(typeClass)) {
             c.setJson(false);
         } else if (Blob.class.equals(typeClass)) {
             c.setJson(false);
         }
 
         if ("id".equals(c.getColumnName())) {
             throw new SystemException("id is duplicated with primary key for " + table.getTableName());
         }
 
         // 外键引用
         XmlNode ref = node.element("one-to-many");
         if (ref != null) {
             String refTable = ref.attribute("reference-table").asString();
             String importName = ref.attribute("import-name").asString();
             String exportName = ref.attribute("export-name").asString();
             OneToManyUtils.add(c, refTable, importName, exportName);
         }
 
         // support validator
         XmlNode validators = node.element("validators");
         if (validators == null) return c;
 
         for (XmlNode n : validators.elements()) {
             String validatorName = n.name();
             Validator validator = ValidatorFactory.createValidator(validatorName);
             if (validator == null) {
                 throw new SystemException("Invalid validator found at %s: %s.", c.getColumnName(), validatorName);
             }
             for (XmlAttribute attr : (List<XmlAttribute>) n.attributes()) {
                 try {
                     BeanUtils.setProperty(validator, attr.name(), attr.value());
                 } catch (Exception e) {
                     throw SystemException.unchecked(e);
                 }
             }
             c.getValidators().add(validator);
         }
 
         return c;
     }
 }
