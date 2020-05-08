 package com.tehbeard.forge.schematic.shell.commands;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import com.tehbeard.forge.schematic.Blueprint;
 import com.tehbeard.forge.schematic.SchVector;
 import com.tehbeard.forge.schematic.SchematicFile;
 import com.tehbeard.forge.schematic.extensions.WorldEditVectorExtension;
 import com.tehbeard.forge.schematic.shell.LibSchematicShell;
 import com.tehbeard.forge.schematic.shell.commands.BCommand.PermLevel;
 

 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 


 @BCommand(command="savesch",level=PermLevel.none,usage="/savesch filename")
 public class SaveCommand extends PlayerCommand {
 
     @Override
     public void processCommand(ICommandSender icommandsender, String[] astring) {
         
         EntityPlayerMP player = getCommandSenderAsPlayer(icommandsender);
         ItemStack currentItem = player.inventory.getCurrentItem();
         if(currentItem == null){return;}
         if(currentItem.itemID == LibSchematicShell.setSquareItem.itemID){
             SchVector p1 = LibSchematicShell.setSquareItem.getPos1(currentItem);
             SchVector p2 = LibSchematicShell.setSquareItem.getPos2(currentItem);
             
             System.out.println("NBT tag says: " + p1 + " :: " + p2);
 
            SchematicFile file = new Blueprint(false, player.worldObj, p1, p2).createSchematicFile();
             
             if(astring.length > 1){
                 WorldEditVectorExtension vectors = new WorldEditVectorExtension();
                 
                 SchVector playerPos = new SchVector((int)Math.floor(player.posX),(int) Math.floor(player.posY),(int) Math.floor(player.posZ));
                 SchVector minPos = SchVector.min(p1, p2);
                 
                 vectors.setOffset(minPos.sub(playerPos));
                 file.addExtension(vectors);
             }
             
             //FMLCommonHandler.instance().getMinecraftServerInstance().getFile(par1Str)
             try {
                 file.saveSchematic(new FileOutputStream(new File("./sch/" + astring[0] + ".schematic")));
                 
                 
                 
                 player.addChatMessage("Saved to " + astring[0] + ".schematic");
                 return;
             } catch (FileNotFoundException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         
         player.addChatMessage("No SetSquare in hand!");
     }
 
 }
