 package pharabus.mods.aeperipheral;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import appeng.api.IAEItemStack;
 import appeng.api.IItemList;
 import appeng.api.Util;
 import appeng.api.WorldCoord;
 import appeng.api.events.GridTileLoadEvent;
 import appeng.api.events.GridTileUnloadEvent;
 import appeng.api.me.tiles.IGridMachine;
 import appeng.api.me.tiles.IStorageAware;
 import appeng.api.me.util.IGridInterface;
 import appeng.api.me.util.IMEInventory;
 import dan200.computer.api.IComputerAccess;
 import dan200.computer.api.ILuaContext;
 import dan200.computer.api.IPeripheral;
 
 public class TileEntityAEPeripheral extends TileEntity implements IPeripheral,
         IGridMachine, IStorageAware {
   
     public boolean hasPower = false;
     private IGridInterface myGrid;
     protected boolean isLoaded = false;
     private Map<String,Alarm> targets = new HashMap<String,Alarm>();
     
     
     @Override
     public void validate() {
       
        super.validate();
         if (!isLoaded) {
             if (AEPeripheralUtil.isClient()) {
                 aeperipheral.AEPeripheralGenericTick_client.TriggerInit(this);
             } else {
                 aeperipheral.AEPeripheralGenericTick_server.TriggerInit(this);
            }
         }
     }
 
     @Override
     public void invalidate() {
        super.invalidate();
         if (isLoaded) {
             isLoaded = false;
             terminate();
         }
     }
 
     @Override
     public String getType() {
 
         return "AEPeripheral";
     }
 
     @Override
     public String[] getMethodNames() {
 
         return new String[] { "GetInventory", "GetCraftables", "Craft","AddAlert" };
     }
     
     @Override
     public Object[] callMethod(IComputerAccess computer, ILuaContext context,
             int method, Object[] arguments) throws Exception {
       
         Map<String, String> ret = new HashMap<String, String>();
         if (!hasPower)
             return new Object[] { "No Power" };
         if (myGrid == null)
             return new Object[] { "No Contoller" };
         if (myGrid != null) {
 
             IGridInterface gi = this.getGrid();
             IMEInventory ginv = null;
             IItemList list = null;
             switch (method) {
                 case 0:
                     ginv = gi.getCellArray();
                     list = ginv.getAvailableItems();
                     for (IAEItemStack item : list) {
                         ret.put(item.getItemStack().getDisplayName(),
                                 Long.toString(item.getStackSize()));                        
                     }
                    break;
                 case 1:
                     ginv = gi.getCraftableArray();
                     list = ginv.getAvailableItems();
                     for (IAEItemStack item : list) {
                         ret.put(item.getItemStack().getDisplayName(),
                                 item.getItemID() + ":" + item.getItemDamage());
                     }
                     break;
                 case 2:
                     if(arguments.length < 2)
                     {
                         throw new Exception("Not enough arguments");
                     }
                     if(!(arguments[0] instanceof String) || !(arguments[1] instanceof Double))
                     {
                         throw new Exception("bad arguments, expected numbers");
                     }
                     String[] item = arguments[0].toString().split(":");
                     int Id = Integer.parseInt(item[0]);
                     int damage = Integer.parseInt(item[1]);
                     int howMany = (int) Math.floor((Double) arguments[1]);
                     gi.craftingRequest(new ItemStack(Id, howMany, damage));
                     break;
                 case 3:
                     if(arguments.length < 4)
                     {
                         throw new Exception("Not enough arguments");
                     }
                     String alarmName = (String) arguments[0];
                     String[] targetitem = arguments[1].toString().split(":");
                     int targetId = Integer.parseInt(targetitem[0]);
                     int targetDamage = Integer.parseInt(targetitem[1]);
                   
                     
                     int min = (int) Math.floor((Double) arguments[2]);
                     int max = (int) Math.floor((Double) arguments[3]);
                     IAEItemStack target = Util.createItemStack(new ItemStack(targetId,0,targetDamage));
                        Alarm alarm = new Alarm(alarmName,min,max,target,computer); 
 
                        this.targets.remove(alarmName);
                        this.targets.put(alarmName,alarm);
                        
                     break;
             }
 
         }
         return new Object[] { ret };
     }
     
 
     @Override
     public boolean canAttachToSide(int side) {        
       //  int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
      //   return side != meta /2;
         
         return true;
     }
 
     @Override
     public void attach(IComputerAccess computer) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void detach(IComputerAccess computer) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public WorldCoord getLocation() {
         // TODO Auto-generated method stub
         return new WorldCoord(xCoord, yCoord, zCoord);
     }
 
     @Override
     public boolean isValid() {
         return true;
     }
 
     @Override
     public void setPowerStatus(boolean _hasPower) {
             if (hasPower != _hasPower) {
                 hasPower = _hasPower;
                
             }  
         markForUpdate();
     }
 
     @Override
     public boolean isPowered() {
 
         return hasPower;
     }
 
     @Override
     public IGridInterface getGrid() {
         return myGrid.isValid() ? myGrid : null;
     }
 
     @Override
     public void setGrid(IGridInterface gi) {
             if (gi != myGrid) {
                 myGrid = gi;
     
                 if (myGrid == null) {
                     setPowerStatus(false);
                 }
              
             }
         markForUpdate();
     }
 
     @Override
     public World getWorld() {
 
         return worldObj;
     }
 
     public void markForUpdate() {
         if (AEPeripheralUtil.isServer()) {
             int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
             int facing = meta / 2;
             
            int active = this.isValid() ? 1 : 0;
             
             int newMeta = facing * 2 + active;      
             worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,newMeta,2);
         }
       //  worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
     }
 
     @Override
     public float getPowerDrainPerTick() {
 
         return 1.0F;
     }
 
     public void init() {
         if (isLoaded)
             return;
 
         isLoaded = true;
 
         MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj,
                 getLocation()));
 
        // worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.worldObj.getBlockId(xCoord,yCoord,zCoord));
  
     }
 
 
             
     protected void terminate() {
         isLoaded = false;
         setGrid(null);
         MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj,
                 getLocation()));
 
     }
 
     @Override
     public void onNetworkInventoryChange(IItemList iss) {
         for(Alarm value : targets.values())
         {
            
             IAEItemStack out = iss.findItem(value.getTarget());
             if(out != null)
             {
                 long level = out.getStackSize();
                 if(value.getMin() == 0)
                 {
                     if(level >= value.getMax())
                     {
                         value.Fire(level);
                     }
                 }
                 else if(value.getMax() == 0)
                 {
                     if(level <= value.getMin())
                     {
                         value.Fire(level);
                     } 
                 }
                 else
                 {
                     if(level >= value.getMin() && level <= value.getMax())
                     {
                         value.Fire(level);
                     }
                 }
                 
             }
         }
     }
     
    
     
     private class Alarm
     {
         private IComputerAccess computer;
         private int min;
         private int max;
         private IAEItemStack target;
         private String alarmName;
           protected Alarm(String alarmname,int min, int max,IAEItemStack target,IComputerAccess computer)
           {
               this.computer = computer;
               this.min = min;
               this.max = max;
               this.target = target;
               this.alarmName = alarmname;
           }
           
           protected void Fire(long level)
           {
               this.computer.queueEvent(this.alarmName, new Object[] {level});
           }
           
           public IAEItemStack getTarget()
           {
               return this.target;
           }
           
           public int getMin()
           {
               return this.min;
           }
           
           public int getMax()
           {
               return this.max;
           }
     }
 
    
   
    
 }
