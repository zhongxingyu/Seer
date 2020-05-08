 package com.seitenbau.testing.dbunit.generator;
 
 import com.seitenbau.testing.util.CamelCase;
 
 public class ColumnReferenceBuilder
 {
   private final ColumnBuilder columnBuilder;
 
   private Column foreignColumn;
 
   /**
    * Describes the relation from the perspective of the current table.
    * The given name will be used to express relations with the Ref types.
    * <p>
    * When modeling associative tables, no local part is needed.
    * <p>
    * Example: A player belongs to a team
    * <code>
    * <pre class="groovyTestCase">
    * import com.seitenbau.testing.dbunit.generator.*;
    *
    * public class DemoDatabaseModel extends DatabaseModel {
    *
    *   public DemoDatabaseModel() {
    *     Table team = table("team")
    *         .column("id", DataType.BIGINT)
    *           .defaultIdentifier()
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     table("player")
    *         .column("team_id", DataType.BIGINT)
    *           .reference
    *             .local
    *               .name("belongsTo")
    *             .foreign(team)
    *               .name("hasMembers")
    *       .build();
    *   }
    * }
    *
    * DemoDatabaseModel model = new DemoDatabaseModel();
    * DataSetGenerator generator = model.getDataSetGenInstance();
    * DataSet dataSet = generator.getDataSet();
    *
    * assert dataSet.getTables().size() == 2;
    * </pre>
    * </code>
    * @return The builder to configure the local relation.
    */
   public final LocalReferenceBuilder local;
 
   private ForeignReferenceBuilder foreign;
 
   private String localName;
 
   private String localDescription;
 
   private String localMultiplicity;
 
   private String foreignName;
 
   private String foreignDescription;
 
   private String foreignMultiplicity;
 
   private ColumnReference reference;
 
   public ColumnReferenceBuilder(ColumnBuilder columnBuilder)
   {
     this.columnBuilder = columnBuilder;
 
     reference = null;
 
     local = new LocalReferenceBuilder(this);
 
     // initialize values
     localName = CamelCase.makeFirstLowerCase(columnBuilder.getColumnJavaName()) + "To";
     localDescription = "";
     localMultiplicity = "1";
 
     foreignName = CamelCase.makeFirstLowerCase(columnBuilder.getTableBuilder().getJavaName()) + "to";
     foreignDescription = "";
     foreignMultiplicity = "1";
   }
 
   /**
    * Describes the relation from the perspective of the foreign table.
    * The given name will be used to express relations with the Ref types.
    * <p>
    * When modeling associative tables, only the target names will be used as method names
    * for the involved Ref classes (see Example 2).
    * <p>
    * Example 1: Players are members of a team
    * <code>
    * <pre class="groovyTestCase">
    * import com.seitenbau.testing.dbunit.generator.*;
    *
    * public class DemoDatabaseModel extends DatabaseModel {
    *
    *   public DemoDatabaseModel() {
    *     Table team = table("team")
    *         .column("id", DataType.BIGINT)
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     table("player")
    *         .column("team_id", DataType.BIGINT)
    *           .reference
    *             .foreign(team.ref("id"))
    *               .name("hasMembers")
    *       .build();
    *   }
    * }
    *
    * DemoDatabaseModel model = new DemoDatabaseModel();
    * DataSetGenerator generator = model.getDataSetGenInstance();
    * DataSet dataSet = generator.getDataSet();
    *
    * assert dataSet.getTables().size() == 2;
    * </pre>
    * </code>
    * <p>
    * Example 2: Associative Table<br>
    * Any person can be member of any group.
    * <code>
    * <pre class="groovyTestCase">
    * import com.seitenbau.testing.dbunit.generator.*;
    *
    * public class DemoDatabaseModel extends DatabaseModel {
    *
    *   public DemoDatabaseModel() {
    *     Table person = table("person")
    *         .column("id", DataType.BIGINT)
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     Table group = table("group")
    *         .column("id", DataType.BIGINT)
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     table("person_group")
    *         .column("person_id", DataType.BIGINT)
    *           .reference
    *             .foreign(person.ref("id"))
    *               .name("belongsTo")
    *         .column("group_id", DataType.BIGINT)
    *           .reference
    *             .foreign(group.ref("id"))
    *               .name("hasMembers")
    *       .build();
    *   }
    * }
    *
    * DemoDatabaseModel model = new DemoDatabaseModel();
    * DataSetGenerator generator = model.getDataSetGenInstance();
    * DataSet dataSet = generator.getDataSet();
    *
    * assert dataSet.getTables().size() == 3;
    * </pre>
    * </code>
    * @param foreignColumn The referenced column
    * @return The builder to configure the relation in the foreign perspective.
    */
   public ForeignReferenceBuilder foreign(Column foreignColumn)
   {
     if (foreign != null)
     {
       throw new IllegalArgumentException("Multiple definition of target not allowed");
     }
     this.foreignColumn = foreignColumn;
     foreign = new ForeignReferenceBuilder(this);
     return foreign;
   }
 
   /**
    * Describes the relation from the perspective of the foreign table. The given name
    * will be used to express relations with the Ref types.
    * <p>
    * When modeling associative tables, only the target names will be used as method names
    * for the involved Ref classes (see Example 2).
    * <p>
    * Example 1: Players are members of a team
    * <code>
    * <pre class="groovyTestCase">
    * import com.seitenbau.testing.dbunit.generator.*;
    *
    * public class DemoDatabaseModel extends DatabaseModel {
    *
    *   public DemoDatabaseModel() {
    *     Table team = table("team")
    *         .column("id", DataType.BIGINT)
    *           .defaultIdentifier()
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     table("player")
    *         .column("team_id", DataType.BIGINT)
    *           .reference
    *             .foreign(team)
    *               .name("hasMembers")
    *       .build();
    *   }
    * }
    *
    * DemoDatabaseModel model = new DemoDatabaseModel();
    * DataSetGenerator generator = model.getDataSetGenInstance();
    * DataSet dataSet = generator.getDataSet();
    *
    * assert dataSet.getTables().size() == 2;
    * </pre>
    * </code>
    * <p>
    * Example 2: Associative Table<br>
    * Any person can be member of any group.
    * <code>
    * <pre class="groovyTestCase">
    * import com.seitenbau.testing.dbunit.generator.*;
    *
    * public class DemoDatabaseModel extends DatabaseModel {
    *
    *   public DemoDatabaseModel() {
    *     Table person = table("person")
    *         .column("id", DataType.BIGINT)
    *           .defaultIdentifier()
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     Table group = table("group")
    *         .column("id", DataType.BIGINT)
    *           .defaultIdentifier()
    *         .column("name", DataType.VARCHAR)
    *       .build();
    *
    *     table("person_group")
    *         .column("person_id", DataType.BIGINT)
    *           .reference
    *             .foreign(person)
    *               .name("belongsTo")
    *         .column("group_id", DataType.BIGINT)
    *           .reference
    *             .foreign(group)
    *               .name("hasMembers")
    *        .build();
    *   }
    * }
    *
    * DemoDatabaseModel model = new DemoDatabaseModel();
    * DataSetGenerator generator = model.getDataSetGenInstance();
    * DataSet dataSet = generator.getDataSet();
    *
    * assert dataSet.getTables().size() == 3;
    * </pre>
    * </code>
    * @param table The referenced table
    * @return The builder to configure the relation in the foreign perspective.
    */
   public ForeignReferenceBuilder foreign(Table table)
   {
     return foreign(table.getDefaultIdentifierColumn());
   }
 
   /**
    * Adds a further column to the table.
    * @param name The database name of the column.
    * @param dataType The column's data type.
    * @return The column builder
    */
   public ColumnBuilder column(String name, DataType dataType)
   {
     buildReference();
     return columnBuilder.column(name, dataType);
   }
 
   /**
    * Finalizes the creation of the table.
    * @return The created table
    */
   public Table build()
   {
     buildReference();
     return columnBuilder.build();
   }
 
   void buildReference()
   {
     reference = new ColumnReference(foreignColumn,
           localName, localDescription, localMultiplicity,
           foreignName, foreignDescription, foreignMultiplicity);
   }
 
   ColumnReference getReference()
   {
     return reference;
   }
 
   public static class LocalReferenceBuilder
   {
 
     private final ColumnReferenceBuilder parent;
 
     LocalReferenceBuilder(ColumnReferenceBuilder parent)
     {
       this.parent = parent;
     }
 
     /**
      * Configures the relation name from the perspective of the current table.
      * The name will be used to express relations with the Ref types.
      * <p>
      * Example: A player belongs to a team
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table team = table("team")
      *         .column("id", DataType.BIGINT)
      *           .defaultIdentifier()
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     table("player")
      *         .column("team_id", DataType.BIGINT)
      *           .reference
      *             .local
      *               .name("belongsTo")
      *             .foreign(team)
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 2;
      * </pre>
      * </code>
      * @param name The relation name used for the generated DSL.
      * @return The builder to configure the local relation.
      */
     public LocalReferenceBuilder name(String name)
     {
       parent.localName = name;
       return this;
     }
 
     public LocalReferenceBuilder description(String description)
     {
       parent.localDescription = description;
       return this;
     }
 
     public LocalReferenceBuilder multiplicity(String multiplicity)
     {
       parent.localMultiplicity = multiplicity;
       return this;
     }
 
     /**
      * Describes the relation from the perspective of the foreign table. The given name
      * will be used to express relations with the Ref types.
      * <p>
      * When modeling associative tables, only the target names will be used as method names
      * for the involved Ref classes (see Example 2).
      * <p>
      * Example 1: Players are members of a team
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table team = table("team")
      *         .column("id", DataType.BIGINT)
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     table("player")
      *         .column("team_id", DataType.BIGINT)
      *           .reference
      *             .foreign(team.ref("id"))
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 2;
      * </pre>
      * </code>
      * <p>
      * Example 2: Associative Table<br>
      * Any person can be member of any group.
      * <pre>
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table person = table("person")
      *         .column("id", DataType.BIGINT)
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     Table group = table("group")
      *         .column("id", DataType.BIGINT)
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
     *     table("person_group")
      *         .column("person_id", DataType.BIGINT)
      *           .reference
      *             .foreign(person.ref("id"))
      *               .name("belongsTo")
      *         .column("group_id", DataType.BIGINT)
      *           .reference
      *             .foreign(group.ref("id"))
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 3;
      * </pre>
      * </code>
      * @param foreignColumn The referenced column
      * @return The builder to configure the relation in the foreign perspective.
      */
     public ForeignReferenceBuilder foreign(Column foreignColumn)
     {
       return parent.foreign(foreignColumn);
     }
 
     /**
      * Describes the relation from the perspective of the foreign table. The given name
      * will be used to express relations with the Ref types.
      * <p>
      * When modeling associative tables, only the target names will be used as method names
      * for the involved Ref classes (see Example 2).
      * <p>
      * Example 1: Players are members of a team
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table team = table("team")
      *         .column("id", DataType.BIGINT)
      *           .defaultIdentifier()
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     table("player")
      *         .column("team_id", DataType.BIGINT)
      *           .reference
      *             .foreign(team)
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 2;
      * </pre>
      * </code>
      * <p>
      * Example 2: Associative Table<br>
      * Any person can be member of any group.
      * <pre>
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table person = table("person")
      *         .column("id", DataType.BIGINT)
      *           .defaultIdentifier()
     *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     Table group = table("group")
      *         .column("id", DataType.BIGINT)
      *           .defaultIdentifier()
     *         .column("name", DataType.VARCHAR)
      *       .build();
      *
     *     table("person_group")
      *         .column("person_id", DataType.BIGINT)
      *           .reference
      *             .foreign(person)
      *               .name("belongsTo")
      *         .column("group_id", DataType.BIGINT)
      *           .reference
      *             .foreign(group)
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 3;
      * </pre>
      * </code>
      * @param table The referenced table
      * @return The builder to configure the relation in the foreign perspective.
      */
     public ForeignReferenceBuilder foreign(Table table)
     {
       return parent.foreign(table.getDefaultIdentifierColumn());
     }
 
     /**
      * Adds a further column to the table.
      * @param name The database name of the column.
      * @param dataType The column's data type.
      * @return The column builder
      */
     public ColumnBuilder column(String name, DataType dataType)
     {
       return parent.column(name, dataType);
     }
 
     /**
      * Finalizes the creation of the table.
      * @return The created table
      */
     public Table build()
     {
       return parent.build();
     }
 
   }
 
   public static class ForeignReferenceBuilder
   {
     private final ColumnReferenceBuilder parent;
 
     /**
      * Describes the relation from the perspective of the current table.
      * The given name will be used to express relations with the Ref types.
      * <p>
      * When modeling associative tables, no local part is needed.
      * <p>
      * Example: A player belongs to a team
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table team = table("team")
      *         .column("id", DataType.BIGINT)
      *           .defaultIdentifier()
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     table("player")
      *         .column("team_id", DataType.BIGINT)
      *           .reference
      *             .local
      *               .name("belongsTo")
      *             .foreign(team)
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 2;
      * </pre>
      * </code>
      * @return The builder to configure the local relation.
      */
     public final LocalReferenceBuilder local;
 
     ForeignReferenceBuilder(ColumnReferenceBuilder parent)
     {
       this.parent = parent;
       local = parent.local;
     }
 
     /**
      * Configures the relation name from the perspective of the foreign table.
      * The name will be used to express relations with the Ref types.
      * <p>
      * Example: A team has members
      * <code>
      * <pre class="groovyTestCase">
      * import com.seitenbau.testing.dbunit.generator.*;
      *
      * public class DemoDatabaseModel extends DatabaseModel {
      *
      *   public DemoDatabaseModel() {
      *     Table team = table("team")
      *         .column("id", DataType.BIGINT)
      *           .defaultIdentifier()
      *         .column("name", DataType.VARCHAR)
      *       .build();
      *
      *     table("player")
      *         .column("team_id", DataType.BIGINT)
      *           .reference
      *             .local
      *               .name("belongsTo")
      *             .foreign(team)
      *               .name("hasMembers")
      *       .build();
      *   }
      * }
      *
      * DemoDatabaseModel model = new DemoDatabaseModel();
      * DataSetGenerator generator = model.getDataSetGenInstance();
      * DataSet dataSet = generator.getDataSet();
      *
      * assert dataSet.getTables().size() == 2;
      * </pre>
      * </code>
      * @param name The relation name used for the generated DSL.
      * @return The builder to configure the local relation.
      */
     public ForeignReferenceBuilder name(String name)
     {
       parent.foreignName = name;
       return this;
     }
 
     public ForeignReferenceBuilder description(String description)
     {
       parent.foreignDescription = description;
       return this;
     }
 
     public ForeignReferenceBuilder multiplicity(String multiplicitiy)
     {
       parent.foreignMultiplicity = multiplicitiy;
       return this;
     }
 
     /**
      * Adds a further column to the table.
      * @param name The database name of the column.
      * @param dataType The column's data type.
      * @return The column builder
      */
     public ColumnBuilder column(String name, DataType dataType)
     {
       return parent.column(name, dataType);
     }
 
     /**
      * Finalizes the creation of the table.
      * @return The created table
      */
     public Table build()
     {
       return parent.build();
     }
 
   }
 
 }
