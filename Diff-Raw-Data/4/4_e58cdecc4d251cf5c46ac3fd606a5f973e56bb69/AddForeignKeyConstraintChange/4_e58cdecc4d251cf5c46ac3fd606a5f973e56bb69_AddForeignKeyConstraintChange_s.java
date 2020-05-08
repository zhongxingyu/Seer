 package liquibase.change;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import liquibase.database.Database;
 import liquibase.database.MSSQLDatabase;
 import liquibase.database.SQLiteDatabase;
 import liquibase.database.sql.AddForeignKeyConstraintStatement;
 import liquibase.database.sql.SqlStatement;
 import liquibase.database.structure.Column;
 import liquibase.database.structure.DatabaseObject;
 import liquibase.database.structure.ForeignKey;
 import liquibase.database.structure.Table;
 import liquibase.exception.InvalidChangeDefinitionException;
 import liquibase.exception.UnsupportedChangeException;
 import liquibase.util.StringUtils;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Adds a foreign key constraint to an existing column.
  */
 public class AddForeignKeyConstraintChange extends AbstractChange
 {
     private String baseTableSchemaName;
     private String baseTableName;
     private String baseColumnNames;
 
     private String referencedTableSchemaName;
     private String referencedTableName;
     private String referencedColumnNames;
 
     private String constraintName;
 
     private Boolean deferrable;
     private Boolean initiallyDeferred;
 
     private Integer updateRule;
     private Integer deleteRule;
 
     public AddForeignKeyConstraintChange()
     {
         super("addForeignKeyConstraint", "Add Foreign Key Constraint");
     }
 
     public String getBaseTableSchemaName()
     {
         return baseTableSchemaName;
     }
 
     public void setBaseTableSchemaName(String baseTableSchemaName)
     {
         this.baseTableSchemaName = baseTableSchemaName;
     }
 
     public String getBaseTableName()
     {
         return baseTableName;
     }
 
     public void setBaseTableName(String baseTableName)
     {
         this.baseTableName = baseTableName;
     }
 
     public String getBaseColumnNames()
     {
         return baseColumnNames;
     }
 
     public void setBaseColumnNames(String baseColumnNames)
     {
         this.baseColumnNames = baseColumnNames;
     }
 
     public String getReferencedTableSchemaName()
     {
         return referencedTableSchemaName;
     }
 
     public void setReferencedTableSchemaName(String referencedTableSchemaName)
     {
         this.referencedTableSchemaName = referencedTableSchemaName;
     }
 
     public String getReferencedTableName()
     {
         return referencedTableName;
     }
 
     public void setReferencedTableName(String referencedTableName)
     {
         this.referencedTableName = referencedTableName;
     }
 
     public String getReferencedColumnNames()
     {
         return referencedColumnNames;
     }
 
     public void setReferencedColumnNames(String referencedColumnNames)
     {
         this.referencedColumnNames = referencedColumnNames;
     }
 
     public String getConstraintName()
     {
         return constraintName;
     }
 
     public void setConstraintName(String constraintName)
     {
         this.constraintName = constraintName;
     }
 
     public Boolean getDeferrable()
     {
         return deferrable;
     }
 
     public void setDeferrable(Boolean deferrable)
     {
         this.deferrable = deferrable;
     }
 
     public Boolean getInitiallyDeferred()
     {
         return initiallyDeferred;
     }
 
     public void setInitiallyDeferred(Boolean initiallyDeferred)
     {
         this.initiallyDeferred = initiallyDeferred;
     }
 
     //    public Boolean getDeleteCascade() {
     //        return deleteCascade;
     //    }
 
     public void setDeleteCascade(Boolean deleteCascade)
     {
         if (deleteCascade != null && deleteCascade)
         {
             setOnDelete("CASCADE");
         }
     }
 
     public void setUpdateRule(Integer rule)
     {
         this.updateRule = rule;
     }
 
     public Integer getUpdateRule()
     {
         return this.updateRule;
     }
 
     public void setDeleteRule(Integer rule)
     {
         this.deleteRule = rule;
     }
 
     public Integer getDeleteRule()
     {
         return this.deleteRule;
     }
 
     public void setOnDelete(String onDelete)
     {
         if (onDelete != null && onDelete.equalsIgnoreCase("CASCADE"))
         {
             setDeleteRule(DatabaseMetaData.importedKeyCascade);
         }
         else if (onDelete != null && onDelete.equalsIgnoreCase("SET NULL"))
         {
             setDeleteRule(DatabaseMetaData.importedKeySetNull);
         }
         else if (onDelete != null && onDelete.equalsIgnoreCase("SET DEFAULT"))
         {
             setDeleteRule(DatabaseMetaData.importedKeySetDefault);
         }
         else if (onDelete != null && onDelete.equalsIgnoreCase("RESTRICT"))
         {
             setDeleteRule(DatabaseMetaData.importedKeyRestrict);
         }
         else if (onDelete == null || onDelete.equalsIgnoreCase("NO ACTION"))
         {
             setDeleteRule(DatabaseMetaData.importedKeyNoAction);
         }
         else
         {
             throw new RuntimeException("Unknown onDelete action: " + onDelete);
         }
     }
 
     public void setOnUpdate(String onUpdate)
     {
         if (onUpdate != null && onUpdate.equalsIgnoreCase("CASCADE"))
         {
             setUpdateRule(DatabaseMetaData.importedKeyCascade);
         }
         else if (onUpdate != null && onUpdate.equalsIgnoreCase("SET NULL"))
         {
             setUpdateRule(DatabaseMetaData.importedKeySetNull);
         }
         else if (onUpdate != null && onUpdate.equalsIgnoreCase("SET DEFAULT"))
         {
             setUpdateRule(DatabaseMetaData.importedKeySetDefault);
         }
         else if (onUpdate != null && onUpdate.equalsIgnoreCase("RESTRICT"))
         {
             setUpdateRule(DatabaseMetaData.importedKeyRestrict);
         }
         else if (onUpdate == null || onUpdate.equalsIgnoreCase("NO ACTION"))
         {
             setUpdateRule(DatabaseMetaData.importedKeyNoAction);
         }
         else
         {
             throw new RuntimeException("Unknown onUpdate action: " + onUpdate);
         }
     }
 
     public void validate(Database database) throws InvalidChangeDefinitionException
     {
         if (StringUtils.trimToNull(baseTableName) == null)
         {
             throw new InvalidChangeDefinitionException("baseTableName is required", this);
         }
         if (StringUtils.trimToNull(baseColumnNames) == null)
         {
             throw new InvalidChangeDefinitionException("baseColumnNames is required", this);
         }
         if (StringUtils.trimToNull(referencedTableName) == null)
         {
             throw new InvalidChangeDefinitionException("referencedTableName is required", this);
         }
         if (StringUtils.trimToNull(referencedColumnNames) == null)
         {
             throw new InvalidChangeDefinitionException("referenceColumnNames is required", this);
         }
 
     }
 
     public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException
     {
 
         if (database instanceof SQLiteDatabase)
         {
             // return special statements for SQLite databases
             return generateStatementsForSQLiteDatabase(database);
         }
 
         if (database instanceof MSSQLDatabase)
         {
             // return statements to modify table triggers
             return generateStatementForMSSQLDatabase(database);
         }
 
         return new SqlStatement[]
         {
             createAddConstraintStmt(database)
         };
     }
 
     private AddForeignKeyConstraintStatement createAddConstraintStmt(Database database)
     {
         boolean deferrable = false;
         if (getDeferrable() != null)
         {
             deferrable = getDeferrable();
         }
 
         boolean initiallyDeferred = false;
         if (getInitiallyDeferred() != null)
         {
             initiallyDeferred = getInitiallyDeferred();
         }
 
         return new AddForeignKeyConstraintStatement(getConstraintName(), getBaseTableSchemaName() == null ? database.getDefaultSchemaName()
                 : getBaseTableSchemaName(), getBaseTableName(), getBaseColumnNames(), getReferencedTableSchemaName() == null ? database
                 .getDefaultSchemaName() : getReferencedTableSchemaName(), getReferencedTableName(), getReferencedColumnNames()).setDeferrable(
                 deferrable).setInitiallyDeferred(initiallyDeferred).setUpdateRule(updateRule).setDeleteRule(deleteRule);
     }
 
     private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) throws UnsupportedChangeException
     {
         // SQLite does not support foreign keys until now.
         // See for more information: http://www.sqlite.org/omitted.html
         // Therefore this is an empty operation...
         return new SqlStatement[] {};
     }
 
     /**
      * SQL Server databases need to have their triggers modified when new FK
      * constraints are added with ON DELETE SET NULL specified in the constraint
      * 
      * This method is not able to handle multi-column FKs and will probably error out.
      * 
      * @param database
      * @return
      * @throws UnsupportedChangeException
      */
     private SqlStatement[] generateStatementForMSSQLDatabase(Database database) throws UnsupportedChangeException
     {
         List<SqlStatement> stmts = new ArrayList<SqlStatement>();
         Connection conn = database.getConnection().getUnderlyingConnection();
 
         // add the add constraint statement
         stmts.add(createAddConstraintStmt(database));
 
         String tableName = getReferencedTableName();
 
         // only modify triggers when ON DELETE SET NULL is used
         try
         {
             if (this.deleteRule != null && this.deleteRule == DatabaseMetaData.importedKeySetNull)
             {
                 // read the current database trigger
                 String[] currentTrigger = SQLServerTriggerUtil.getDeleteTriggerForTable(tableName, conn);
 
                 Set<String[]> fks = new HashSet<String[]>();
 
                 // if no trigger exists, add a stmt to create one
                 if (currentTrigger != null)
                 {
                     String triggerQuery = currentTrigger[2];
 
                     // parse the trigger query to get the set of referencing tables and columns
                     Set<String[]> referencingPairs = SQLServerTriggerUtil.findCascadeUpdateStatements(currentTrigger[0], tableName, triggerQuery);
                     fks.addAll(referencingPairs);
                 }
 
                 String[] referencingFK = new String[2];
                 referencingFK[0] = getBaseTableName();
                 referencingFK[1] = getBaseColumnNames();
 
                 // add the current table/column pair to the set and generate if it does not already exist
                 if (fks.add(referencingFK))
                 {
                    // create stmt to drop current trigger if set previously contained other FKs
                    if (fks.size() > 1)
                     {
                         SqlStatement dropStmt = SQLServerTriggerUtil.generateDropTrigger(currentTrigger[0]);
                         stmts.add(dropStmt);
                     }
 
                     // add stmt to add/re-add the trigger this will depend on whether drop was executed)
                     SqlStatement stmt = SQLServerTriggerUtil.generateCreateTriggerStmt(tableName, getReferencedColumnNames(), fks);
                     stmts.add(stmt);
                 }
             }
 
         }
         catch (SQLException e)
         {
             throw new UnsupportedChangeException(e.getMessage(), e);
         }
         
         return stmts.toArray(new SqlStatement[stmts.size()]);
     }
 
     @Override
     protected Change[] createInverses()
     {
         DropForeignKeyConstraintChange inverse = new DropForeignKeyConstraintChange();
         inverse.setBaseTableSchemaName(getBaseTableSchemaName());
         inverse.setBaseTableName(getBaseTableName());
         inverse.setConstraintName(getConstraintName());
 
         return new Change[]
         {
             inverse
         };
     }
 
     public String getConfirmationMessage()
     {
         return "Foreign key contraint added to " + getBaseTableName() + " (" + getBaseColumnNames() + ")";
     }
 
     public Element createNode(Document currentChangeLogFileDOM)
     {
         Element node = currentChangeLogFileDOM.createElement(getTagName());
 
         if (getBaseTableSchemaName() != null)
         {
             node.setAttribute("baseTableSchemaName", getBaseTableSchemaName());
         }
 
         node.setAttribute("baseTableName", getBaseTableName());
         node.setAttribute("baseColumnNames", getBaseColumnNames());
         node.setAttribute("constraintName", getConstraintName());
 
         if (getReferencedTableSchemaName() != null)
         {
             node.setAttribute("referencedTableSchemaName", getReferencedTableSchemaName());
         }
         node.setAttribute("referencedTableName", getReferencedTableName());
         node.setAttribute("referencedColumnNames", getReferencedColumnNames());
 
         if (getDeferrable() != null)
         {
             node.setAttribute("deferrable", getDeferrable().toString());
         }
 
         if (getInitiallyDeferred() != null)
         {
             node.setAttribute("initiallyDeferred", getInitiallyDeferred().toString());
         }
 
         //        if (getDeleteCascade() != null) {
         //            node.setAttribute("deleteCascade", getDeleteCascade().toString());
         //        }
 
         if (getUpdateRule() != null)
         {
             switch (getUpdateRule())
             {
             case DatabaseMetaData.importedKeyCascade:
                 node.setAttribute("onUpdate", "CASCADE");
                 break;
             case DatabaseMetaData.importedKeySetNull:
                 node.setAttribute("onUpdate", "SET NULL");
                 break;
             case DatabaseMetaData.importedKeySetDefault:
                 node.setAttribute("onUpdate", "SET DEFAULT");
                 break;
             case DatabaseMetaData.importedKeyRestrict:
                 node.setAttribute("onUpdate", "RESTRICT");
                 break;
             default:
                 //don't set anything
                 //                    node.setAttribute("onUpdate", "NO ACTION");
                 break;
             }
         }
         if (getDeleteRule() != null)
         {
             switch (getDeleteRule())
             {
             case DatabaseMetaData.importedKeyCascade:
                 node.setAttribute("onDelete", "CASCADE");
                 break;
             case DatabaseMetaData.importedKeySetNull:
                 node.setAttribute("onDelete", "SET NULL");
                 break;
             case DatabaseMetaData.importedKeySetDefault:
                 node.setAttribute("onDelete", "SET DEFAULT");
                 break;
             case DatabaseMetaData.importedKeyRestrict:
                 node.setAttribute("onDelete", "RESTRICT");
                 break;
             default:
                 //don't set anything
                 //node.setAttribute("onDelete", "NO ACTION");
                 break;
             }
         }
         return node;
     }
 
     public Set<DatabaseObject> getAffectedDatabaseObjects()
     {
         Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();
 
         Table baseTable = new Table(getBaseTableName());
         returnSet.add(baseTable);
 
         for (String columnName : getBaseColumnNames().split(","))
         {
             Column baseColumn = new Column();
             baseColumn.setTable(baseTable);
             baseColumn.setName(columnName.trim());
 
             returnSet.add(baseColumn);
         }
 
         Table referencedTable = new Table(getReferencedTableName());
         returnSet.add(referencedTable);
 
         for (String columnName : getReferencedColumnNames().split(","))
         {
             Column referencedColumn = new Column();
             referencedColumn.setTable(baseTable);
             referencedColumn.setName(columnName.trim());
 
             returnSet.add(referencedColumn);
         }
 
         ForeignKey fk = new ForeignKey();
         fk.setName(constraintName);
         fk.setForeignKeyTable(baseTable);
         fk.setForeignKeyColumns(baseColumnNames);
         fk.setPrimaryKeyTable(referencedTable);
         fk.setPrimaryKeyColumns(referencedColumnNames);
         fk.setDeleteRule(this.deleteRule);
         fk.setUpdateRule(this.updateRule);
         returnSet.add(fk);
 
         return returnSet;
 
     }
 
 }
