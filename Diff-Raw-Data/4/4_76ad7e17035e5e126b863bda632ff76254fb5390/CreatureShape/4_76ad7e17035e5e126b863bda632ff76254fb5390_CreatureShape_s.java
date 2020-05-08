 package io.github.totemo.doppelganger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.util.Vector;
 
 // ----------------------------------------------------------------------------
 /**
  * Describes the shape of a creature in terms of the final, triggering block's
  * Material, and the relative positions and materials of the blocks that need to
  * be in place around it in order to summon the creature.
  */
 public class CreatureShape
 {
   // --------------------------------------------------------------------------
   /**
    * Load this CreatureShape from the specified section.
    * 
    * The expected format is:
    * 
    * <pre>
    *    head: pumpkin
    *    body:
    *    - material: diamond_block
    *      offset: [0, -1, 0]
    *    - material: diamond_block
    *      offset: [0, -2, 0]
    *    summon:
    *    - weight: 1.0
    *      spawn: ToughWitherSkeleton
    * </pre>
    * 
    * Note that the "body" can be omitted, in which case placing a named head
    * block will summon a creature. Also, the "summon" can be omitted, in which
    * case nothing will be summoned by default. This may still be useful if
    * specific player names override what is summoned.
    * 
    * @param section the configuration section to load.
    * @param logger logs messages.
    * @return a new CreatureShape instance, or null on error.
    */
   public static CreatureShape loadFromSection(ConfigurationSection section, Logger logger)
   {
     CreatureShape shape = null;
     try
     {
       Material headMaterial = Material.matchMaterial(section.getString("head", ""));
       if (headMaterial == null)
       {
         logger.warning("Shape " + section.getName() + "'s head material is invalid.");
         return null;
       }
       else
       {
         boolean enabled = section.getBoolean("enabled", true);
         if (!enabled)
         {
           logger.warning("Summoning doppelgangers by building shape " + section.getName() + " is disabled.");
         }
         shape = new CreatureShape(section.getName(), headMaterial, enabled);
         if (section.isList("body"))
         {
           try
           {
             List<Map<?, ?>> blocks = section.getMapList("body");
             for (int i = 0; i < blocks.size(); ++i)
             {
               ConfigMap map = new ConfigMap(blocks.get(i), logger, shape.getName() + " block " + i);
               String materialName = map.getString("material", "");
               Material mat = Material.matchMaterial(materialName);
               List<Number> offset = map.getNumberList("offset", null);
               if (mat != null && offset != null && offset.size() == 3)
               {
                 int x = offset.get(0).intValue();
                 int y = offset.get(1).intValue();
                 int z = offset.get(2).intValue();
                 if (x == 0 && y == 0 && z == 0)
                 {
                   logger.warning("block index " + i + " can't have offset (0,0,0) because that is where the head goes");
                   return null;
                 }
                 else
                 {
                   Vector vector = new Vector(x, y, z);
                   shape.addCreatureBlock(mat, vector);
                 }
               }
               else
               {
                 logger.warning("block index " + i + " is incorrectly specified");
                 return null;
               }
             } // for each block in the body
           }
           catch (Exception ex)
           {
             logger.warning(ex.getClass().getName() + " loading body in " + section.getCurrentPath());
           }
         } // body
 
         if (section.isList("summon"))
         {
           try
           {
             List<Map<?, ?>> summons = section.getMapList("summon");
             for (int i = 0; i < summons.size(); ++i)
             {
               ConfigMap map = new ConfigMap(summons.get(i), logger, shape.getName() + " summon " + i);
               double weight = map.getDouble("weight", 1.0);
               String spawn = map.getString("spawn", "");
               shape.addCreatureType(spawn, weight);
             }
           }
           catch (Exception ex)
           {
             logger.warning(ex.getClass().getName() + " loading summon in " + section.getCurrentPath());
           }
         } // summon
       } // head material is valid.
 
       // Compute the mandatory one-block horizontal border of air.
       shape.computeBorder();
       return shape;
     }
     catch (Exception ex)
     {
       logger.warning(ex.getClass().getName() + " defining creature shape " + section.getCurrentPath());
     }
     return null;
   } // loadFromSection
 
   // --------------------------------------------------------------------------
   /**
    * Constructor.
    * 
    * @param name the name of this shape in the configuration.
    * @param triggerMaterial the Material of the final placed block that triggers
    *          creature spawning.
    * @param enabled if true, building this shape will summon a doppelganger; if
    *          false, the creature can only be summoned by command.
    */
   public CreatureShape(String name, Material triggerMaterial, boolean enabled)
   {
     _name = name;
     _triggerMaterialId = triggerMaterial.getId();
     _enabled = enabled;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return the name of this shape in the configuration.
    * 
    * @return the name of this shape in the configuration.
    */
   public String getName()
   {
     return _name;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return the Material ID of the trigger.
    * 
    * @return the Material ID of the trigger.
    */
   public int getTriggerMaterialId()
   {
     return _triggerMaterialId;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return true if building this shape will summon a doppelganger.
    * 
    * @return true if building this shape will summon a doppelganger.
    */
   public boolean isEnabled()
   {
     return _enabled;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Signify that the {@link CreatureType} signified by its name can be summoned
    * by this shape, with the specified weighted probability.
    * 
    * The chance of a specific {@link CreatureType} being spawned is its weight
    * divided by the sum of the weights of all registered types for this shape.
    * 
    * @param type the name of the {@link CreatureType}.
    * @param weight the probability weight.
    */
   public void addCreatureType(String type, double weight)
   {
     _types.addChoice(type, weight);
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return the name of the spawned CreatureType.
    * 
    * If more than one {@link CreatureType} is associated with this shape, a type
    * will be selected at random according to weighted probability.
    * 
    * @return the type name of the spawned CreatureType.
    */
   public String chooseCreatureType()
   {
     return _types.choose();
   }
 
   // --------------------------------------------------------------------------
   /**
    * Define the Material its position relative to the trigger block.
    * 
    * @param material the Material of the pre-placed creature block.
    * @param offset the position of that relative to the final trigger block.
    */
   public void addCreatureBlock(Material material, Vector offset)
   {
     _materialIds.add(material.getId());
     _offsets.add(offset);
     if (offset.getBlockY() < _groundOffset)
     {
       _groundOffset = offset.getBlockY();
     }
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return true if this shape is enabled and placing an item of the specified
    * type at the specified Location would result in this creature shape.
    * 
    * @param loc the location where the trigger block would be placed.
    * @param placedItemId the ID of the placed block.
    * @return true if placing the specified block at the specified location would
    *         result in a complete creature shape.
    */
   public boolean isComplete(Location loc, int placedItemId)
   {
     return isEnabled() && _triggerMaterialId == placedItemId && isCreatureShape(loc);
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return true if placing the trigger block at the specified Location would
    * result in a complete creature shape.
    * 
    * @param loc the location where the trigger block would be placed.
    * @return true if placing the trigger block at the specified location would
    *         result in a complete creature shape.
    */
   public boolean isCreatureShape(Location loc)
   {
     for (int i = 0; i < _materialIds.size(); ++i)
     {
       if (getCreatureBlock(loc, i).getTypeId() != _materialIds.get(i))
       {
         return false;
       }
     }
     return true;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Set all of the blocks of the creature shape at the specified location to
    * air.
    * 
    * @param loc the location where the trigger block would be placed.
    */
   public void vaporise(Location loc)
   {
     for (int i = 0; i < _offsets.size(); ++i)
     {
       getCreatureBlock(loc, i).setTypeId(0);
     }
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return the offset in Y coordinate of the lowest blocks in the shape.
    * 
    * @return the offset in Y coordinate of the lowest blocks in the shape.
    */
   public int getGroundOffset()
   {
     return _groundOffset;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Print a description of this shape to the command sender.
    * 
    * @param sender the entity requesting the description.
    */
   public void describe(CommandSender sender)
   {
     sender.sendMessage(ChatColor.GOLD + "Shape " + getName() + ":");
     sender.sendMessage(ChatColor.GOLD + "    Enabled: " + ChatColor.YELLOW + isEnabled());
     sender.sendMessage(ChatColor.GOLD + "    Head material: " + ChatColor.YELLOW
                        + Material.getMaterial(getTriggerMaterialId()).toString());
     sender.sendMessage(ChatColor.GOLD + "    Y offset to ground: " + ChatColor.YELLOW + getGroundOffset());
     if (_materialIds.size() != 0)
     {
       sender.sendMessage(ChatColor.GOLD + "    Body: ");
       for (int i = 0; i < _materialIds.size(); ++i)
       {
         sender.sendMessage(String.format("%s        (%d) %s at (%d, %d, %d)",
           ChatColor.YELLOW, i + 1, Material.getMaterial(_materialIds.get(i)).toString(),
           _offsets.get(i).getBlockX(), _offsets.get(i).getBlockY(), _offsets.get(i).getBlockZ()));
       }
     }
     if (_border.size() != 0)
     {
       sender.sendMessage(ChatColor.GOLD + "    Border: ");
       StringBuilder border = new StringBuilder();
       border.append(ChatColor.YELLOW);
       border.append("        ");
       for (int i = 0; i < _border.size(); ++i)
       {
         Vector v = _border.get(i);
         border.append('(');
         border.append(v.getBlockX());
         border.append(',');
         border.append(v.getBlockY());
         border.append(',');
         border.append(v.getBlockZ());
         border.append(") ");
       }
       sender.sendMessage(border.toString());
     }
     if (!_types.entrySet().isEmpty())
     {
       sender.sendMessage(ChatColor.GOLD + "    Summoning probabilities: ");
       double lastWeight = 0.0;
       for (Entry<Double, String> summon : _types.entrySet())
       {
         double percent = 100 * (summon.getKey() - lastWeight) / _types.getTotalWeight();
         lastWeight = summon.getKey();
         sender.sendMessage(String.format("%s        %.1f%% %s", ChatColor.YELLOW, percent, summon.getValue()));
       }
     }
   } // describe
 
   // --------------------------------------------------------------------------
   /**
    * Check that there is 1 block wide border of air around the bounding box of
    * the creature shape in all horizontal directions (but not above or below the
    * bounding box).
    * 
    * The purpose of this is to prevent the use of Doppelganger spawning as a way
   * top grief a pre-existing structure that was not build for the purpose of
    * spawning a Doppelganger.
    * 
    * @param loc the Location of the final head block.
    */
   public boolean hasBorder(Location loc)
   {
     for (int i = 0; i < _border.size(); ++i)
     {
       Vector offset = _border.get(i);
       int x = loc.getBlockX() + offset.getBlockX();
       int y = loc.getBlockY() + offset.getBlockY();
       int z = loc.getBlockZ() + offset.getBlockZ();
       if (loc.getWorld().getBlockTypeIdAt(x, y, z) != 0)
       {
         return false;
       }
     }
     return true;
   } // hasBorder
 
   // --------------------------------------------------------------------------
   /**
    * Return the Block with the specified index comprising the creature shape,
    * relative to the location of the trigger block.
    * 
    * @param loc the Location of the trigger block.
    * @param index the 0-based index of the block in the shape; 0 is first one
    *          added.
    * @return the Block with the specified index comprising the creature shape,
    *         relative to the location of the trigger block.
    */
   protected Block getCreatureBlock(Location loc, int index)
   {
     Vector offset = _offsets.get(index);
     int x = loc.getBlockX() + offset.getBlockX();
     int y = loc.getBlockY() + offset.getBlockY();
     int z = loc.getBlockZ() + offset.getBlockZ();
     return loc.getWorld().getBlockAt(x, y, z);
   }
 
   // --------------------------------------------------------------------------
   /**
    * Compute the offsets of the blocks in the border around the shape (including
    * the head) which must be air in order for the creature to spawn.
    */
   protected void computeBorder()
   {
     _border.clear();
 
     // Compute the bounds of the body shape relative to the head location.
     int minX = 0, maxX = 0;
     int minY = 0, maxY = 0;
     int minZ = 0, maxZ = 0;
     for (int i = 0; i < _offsets.size(); ++i)
     {
       int x = _offsets.get(i).getBlockX();
       int y = _offsets.get(i).getBlockY();
       int z = _offsets.get(i).getBlockZ();
       minX = Math.min(minX, x);
       maxX = Math.max(maxX, x);
       minY = Math.min(minY, y);
       maxY = Math.max(maxY, y);
       minZ = Math.min(minZ, z);
       maxZ = Math.max(maxZ, z);
     }
 
     // Extend the bounded volume +/- one block in the X and Z dimensions only.
     --minX;
     ++maxX;
     --minZ;
     ++maxZ;
 
     // Iterate over all offset coordinates in the bounded volume adding an entry
     // to _border if the corresponding offset is not in _offsets. Iteration
     // proceeds from the ground up on the basis that ground obstacles offer the
     // earliest failure exit at creature spawn time.
     for (int y = minY; y <= maxY; ++y)
     {
       for (int x = minX; x <= maxX; ++x)
       {
         for (int z = minZ; z <= maxZ; ++z)
         {
           if (!hasBodyOffset(x, y, z))
           {
             _border.add(new Vector(x, y, z));
           }
         }
       }
     }
   } // computeBorder
 
   // --------------------------------------------------------------------------
   /**
    * Return true if the specified (x,y,z) offset from the head location is a
    * body block.
    * 
    * This method is only called from computeBorder(), which happens only while
    * the shape is being loaded.
    * 
    * @return true if the specified (x,y,z) offset from the head location is a
    *         body block.
    */
   protected boolean hasBodyOffset(int x, int y, int z)
   {
     if (x == 0 && y == 0 && z == 0)
     {
       // Offset (0,0,0) is the head, explicitly precluded by loadFromSection().
       return true;
     }
 
     for (int i = 0; i < _offsets.size(); ++i)
     {
       Vector o = _offsets.get(i);
       if (x == o.getBlockX() && y == o.getBlockY() && z == o.getBlockZ())
       {
         return true;
       }
     }
     return false;
   } // hasBodyOffset
 
   // --------------------------------------------------------------------------
   /**
    * Dump internal state to the specified Logger for debugging.
    * 
    * @param logger the Logger.
    */
   protected void dump(Logger logger)
   {
     logger.info("Trigger: " + Material.getMaterial(_triggerMaterialId).name());
     logger.info("Ground offset: " + _groundOffset);
     logger.info("Blocks:");
     for (int i = 0; i < _materialIds.size(); ++i)
     {
       String material = Material.getMaterial(_materialIds.get(i)).name();
       String offset = _offsets.get(i).toString();
       logger.info(i + ": " + material + " at " + offset);
     }
     for (Entry<Double, String> summon : _types.entrySet())
     {
       logger.info("summon " + summon.getValue());
     }
   } // dump
 
   // --------------------------------------------------------------------------
   /**
    * The name of this shape in the configuration.
    */
   protected String                    _name;
 
   /**
    * The material of the placed, named block that triggers a search for a
    * creature shape.
    */
   protected int                       _triggerMaterialId;
 
   /**
    * True if building this shape will summon a doppelganger.
    */
   protected boolean                   _enabled;
 
   /**
    * The relative Y offset of the lowest block(s) in the shape.
    */
   protected int                       _groundOffset;
 
   /**
    * Expected Material ID at corresponding offset from trigger block.
    */
   protected ArrayList<Integer>        _materialIds = new ArrayList<Integer>();
 
   /**
    * Offset relative to trigger block to look for corresponding _materials
    * element.
    */
   protected ArrayList<Vector>         _offsets     = new ArrayList<Vector>();
 
   /**
    * Offset relative to trigger block of all blocks around the shape that must
    * be air. These are automatically computed when the configuration is loaded.
    */
   protected ArrayList<Vector>         _border      = new ArrayList<Vector>();
 
   /**
    * Manages weighted random selection of creature type name to spawn.
    */
   protected WeightedSelection<String> _types       = new WeightedSelection<String>();
 } // class CreatureShape
