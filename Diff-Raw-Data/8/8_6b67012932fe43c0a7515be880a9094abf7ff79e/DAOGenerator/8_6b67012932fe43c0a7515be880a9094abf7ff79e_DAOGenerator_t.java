 package com.digitolio.jdbi.codegen.codemodel;
 
 import com.digitolio.jdbi.StrategyAwareMapBean;
 import com.digitolio.jdbi.annotations.AutoInsert;
 import com.digitolio.jdbi.annotations.AutoUpdateByPK;
 import com.digitolio.jdbi.annotations.PK;
 import com.digitolio.jdbi.auto.SqlDeleteByPk;
 import com.digitolio.jdbi.auto.SqlSelectByPK;
 import com.digitolio.jdbi.table.Column;
 import com.digitolio.jdbi.table.Table;
 import com.google.common.base.Optional;
 import com.google.common.io.Files;
 import com.sun.codemodel.*;
 import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.sqlobject.*;
 import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.List;
 
 /**
  * @author C.Koc
  */
 public class DAOGenerator {
 
     private final Table table;
     private final Class<?> clazz;
     private final File targetDir;
 
     public DAOGenerator(Class<?> aClass, Table table, File targetDir) {
         this.clazz = aClass;
         this.table = table;
         this.targetDir = targetDir;
     }
 
     public void generate() throws JClassAlreadyExistsException, IOException {
         JCodeModel codeModel = new JCodeModel();
 
         JDefinedClass classDefinition = createClassDefinition(codeModel, clazz);
         addSelectById(codeModel, classDefinition, table, clazz);
         addInsert(classDefinition, table, clazz);
         addUpdate(classDefinition, table, clazz);
         addDeleteById(classDefinition, table, clazz);
         Files.createParentDirs(targetDir);
         codeModel.build(targetDir);
 
     }
 
     private void addDeleteById(JDefinedClass classDefinition, Table table, Class clazz) {
         JMethod method = classDefinition.method(JMod.ABSTRACT, Integer.class, "delete" + clazz.getSimpleName());
         JAnnotationUse deleteAnnotation = method.annotate(SqlUpdate.class);
         SqlDeleteByPk sqlDeleteById = new SqlDeleteByPk(table);
         deleteAnnotation.param("value", sqlDeleteById.generate(new Binding()));
         for (Column column : table.getPrimaryKeyColumns()) {
             JVar param = method.param(column.getField().getType(), uncapitalize(column.getFieldName()));
             JAnnotationUse bind = param.annotate(Bind.class);
             bind.param("value", uncapitalize(column.getFieldName()));
         }
     }
 
     private void addSelectById(JCodeModel codeModel, JDefinedClass classDefinition, Table table, Class<?> clazz) {
         JClass jClass = codeModel.ref(Optional.class).narrow(codeModel.ref(clazz));
         JMethod method = classDefinition.method(JMod.ABSTRACT, jClass, "select"+ clazz.getSimpleName());
         method.type();
         method.annotate(StrategyAwareMapBean.class);
         method.annotate(SingleValueResult.class);
        JAnnotationUse selectAnnotation = method.annotate(SqlQuery.class);
         SqlSelectByPK sqlSelectById = new SqlSelectByPK(table);
         selectAnnotation.param("value", sqlSelectById.generate(new Binding()));
         for (Column column : table.getPrimaryKeyColumns()) {
             JVar param = method.param(column.getField().getType(), uncapitalize(column.getFieldName()));
             JAnnotationUse bind = param.annotate(Bind.class);
             bind.param("value", uncapitalize(column.getFieldName()));
         }
     }
 
     private void addUpdate(JDefinedClass classDefinition, Table table, Class entity) {
         JMethod method = classDefinition.method(JMod.ABSTRACT, Integer.class, "update");
         method.annotate(AutoUpdateByPK.class);
         method.annotate(SqlUpdate.class);
         JVar param = method.param(entity, uncapitalize(entity.getSimpleName()));
         param.annotate(BindBean.class);
     }
 
     private void addInsert(JDefinedClass classDefinition, Table table, Class entity) {
         List<Column> pkColumns = table.getPrimaryKeyColumns();
         Field field = getAutoIncrementField(pkColumns);
         Class returnType = field != null ? field.getType() : Integer.class;
         JMethod method = classDefinition.method(JMod.ABSTRACT, returnType, "insert");
         method.annotate(AutoInsert.class);
         if (field != null) {
             method.annotate(GetGeneratedKeys.class);
         }
         method.annotate(SqlUpdate.class);
         JVar param = method.param(entity, uncapitalize(entity.getSimpleName()));
         param.annotate(BindBean.class);
     }
 
     private Field getAutoIncrementField(List<Column> pkColumns) {
         for (Column pkColumn : pkColumns) {
             PK annotation = pkColumn.getField().getAnnotation(PK.class);
             if (annotation != null && annotation.autoIncrement()) {
                 return pkColumn.getField();
             }
         }
         return null;
     }
 
     private String uncapitalize(String s) {
         return s.substring(0, 1).toLowerCase().concat(s.substring(1, s.length()));
     }
 
     private JDefinedClass createClassDefinition(JCodeModel codeModel,
                                                 Class entityClass) throws JClassAlreadyExistsException {
         String daoPackage = getDaoPackage(clazz.getPackage());
         return codeModel._class(getFullDaoClassName(daoPackage, clazz), ClassType.INTERFACE);
     }
 
     private String getDaoPackage(Package aPackage) {
         String name = aPackage.getName();
         return name.concat(".").substring(0, name.lastIndexOf(".") + 1).concat("dao");
     }
 
     private String getFullDaoClassName(String aPackage, Class entity) {
         return aPackage.concat("._").concat(entity.getSimpleName().concat("DAO"));
     }
 
 }
