 package com.seitenbau.stu.database.generator.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Optional;
 import com.seitenbau.stu.database.generator.Column;
 import com.seitenbau.stu.database.generator.DatabaseModel;
 import com.seitenbau.stu.database.generator.Edge;
 import com.seitenbau.stu.database.generator.Table;
import com.seitenbau.stu.database.generator.data.EntityCreationMode.Direction;
 import com.seitenbau.stu.database.generator.values.ValueGenerator;
 import com.seitenbau.stu.logger.Logger;
 import com.seitenbau.stu.logger.TestLoggerFactory;
 
 public class EntityFactory
 {
   private final Logger log = TestLoggerFactory.get(EntityFactory.class);
 
   private final Entities blueprints;
 
   private final Map<Column, ValueGenerator> valueGenerators;
 
   private final DatabaseModel model;
 
   private final long modelSeed;
 
   public EntityFactory(DatabaseModel model)
   {
     this.model = model;
     modelSeed = model.getSeed();
     blueprints = new Entities(model);
     valueGenerators = new HashMap<Column, ValueGenerator>();
   }
 
   public Entities getEntities()
   {
     return blueprints;
   }
 
   /**
    * gets or creates an entity blueprint...
    * @param table
    * @param edge
    * @param mode
    * @return
    */
   public EntityBlueprint getEntity(Table table, Edge edge, EntityCreationMode mode, EntityBlueprint referencedEntity)
   {
     log.info("Requesting blueprint for table " + table.getName() + " and edge " + edge + " and mode " + mode + ", to be referenced by " + referencedEntity);
     List<EntityBlueprint> list = blueprints.getTableBlueprints(table);
 
     // check if there is a blueprint, which can be used
     for (EntityBlueprint blueprint : list) {
       if (blueprint == referencedEntity) {
         continue;
       }

       Optional<EntityCreationMode> creationInformation = blueprint.getCreationInformation(edge);
 
       // if the blueprint does not have any relation, use it...
       if (!creationInformation.isPresent())
       {
         log.info("Returning existing but unrelated blueprint " + blueprint);
         blueprint.setCreationInformation(edge, mode);
         return blueprint;
       }
 
       EntityCreationMode existingMode = creationInformation.get();
       int referencedCount = blueprint.getReferencedByList(edge).size();
 
       // TODO any does not check if constraints are valid...
       if (isValidMode(mode, existingMode, referencedCount))
       {
         log.info("Returning existing blueprint " + blueprint);
         return blueprint;
       }
     }
 
 
     EntityBlueprint result = createEntity(table);
     result.setCreationInformation(edge, mode);
 
     log.info("Returning new blueprint " + result);
 
     return result;
   }
 
   private boolean isValidMode(EntityCreationMode mode, EntityCreationMode existingMode, int referencedCount)
   {
    if (!mode.getDirection().equals(existingMode.getDirection()) && !existingMode.getDirection().equals(Direction.ANY)) {
       return true;
     }
 
     if (existingMode.getMax() > 0 && referencedCount >= existingMode.getMax())
     {
       return false;
     }
 
     if (isBothAny(mode, existingMode) || isBothNoRelation(mode, existingMode))
     {
       return true;
     }
 
     // TODO check if the mode matches with the existing mode
     // if it is an open 0/1..max relation and we want an 0/1..max2 relation, use it ?
     if (mode.getMax() == existingMode.getMax())
     {
       if (referencedCount < mode.getMax()) {
         //return true;
       }
     }
 
     return false;
   }
 
   private boolean isBothAny(EntityCreationMode mode, EntityCreationMode existingMode)
   {
     return mode.isAny() && existingMode.isAny();
   }
 
   private boolean isBothNoRelation(EntityCreationMode mode, EntityCreationMode existingMode)
   {
     return mode.getMax() == 0 && existingMode.getMax() == 0;
   }
 
   public void ensureEntityCount()
   {
     for (Table table : model.getTables()) {
       List<EntityBlueprint> list = blueprints.getTableBlueprints(table);
       while (list.size() < table.getMinEntities()) {
         createEntity(table);
       }
     }
   }
 
   private EntityBlueprint createEntity(Table table)
   {
     List<EntityBlueprint> list = blueprints.getTableBlueprints(table);
     final String refName = table.getJavaName().toUpperCase() + "_" + String.valueOf(list.size() + 1);
     EntityBlueprint result = new EntityBlueprint(table, refName, this);
     list.add(result);
     return result;
   }
 
   public List<EntityBlueprint> getUnrelatedBlueprints(Table table, Edge edge)
   {
     List<EntityBlueprint> result = new ArrayList<EntityBlueprint>();
     List<EntityBlueprint> list = blueprints.getTableBlueprints(table);
     for (EntityBlueprint bp : list) {
       if (!bp.getCreationInformation(edge).isPresent()) {
         result.add(bp);
       }
     }
 
     return result;
   }
 
   public ValueGenerator getValueGenerator(Column column)
   {
     if (valueGenerators.containsKey(column)) {
       return valueGenerators.get(column);
     }
 
     long seed = modelSeed + column.getTable().getSeed() + column.getSeed();
 
     ValueGenerator result = column.getGenerator();
     result.initialize(seed);
     valueGenerators.put(column, result);
     return result;
   }
 
 }
