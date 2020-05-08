 package net.blockscape.save;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import net.blockscape.Player;
 import net.blockscape.helper.FileHelper;
 import net.blockscape.helper.LogHelper;
 import net.blockscape.lib.MainReference;
 import net.blockscape.lib.Saves;
 import net.blockscape.registry.GameRegistry;
 import net.blockscape.world.WorldBlock;
 import processing.core.PApplet;
 
 public class SaveData
 {
     private static ArrayList<WorldSave> saves;
 
     public static void initDirectory()
     {
         saves = new ArrayList<WorldSave>();
         
         try
         {
             if (!Files.exists(FileHelper.getPathFromString(FileHelper.getFileDirectoryString())))
                 Files.createDirectories(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves"));
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
         }
     }
     
     private static int getWorldMatch(String name)
     {
         for (WorldSave save: saves)
         {
             if (save.getName() == name)
             {
                 return saves.indexOf(save);
             }
         }
         
         return -1;
     }
     
     public static void addWorld(WorldSave world)
     {
         saves.add(world);
         createWorldFile(saves.indexOf(world));
         saveGame(world);
     }
     
     private static void createWorldFile(int index)
     {
         try
         {
             if (!Files.exists(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName())))
                 Files.createDirectory(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName()));
             if (!Files.exists(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.WORLD_FILE_NAME)))
                 Files.createFile(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.WORLD_FILE_NAME));
             if (!Files.exists(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.PLAYER_FILE_NAME)))
                 Files.createFile(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.PLAYER_FILE_NAME));
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
             return;
         }
         
     }
     
     public static void saveGame(WorldSave newWorld)
     {
         int index = getWorldMatch(newWorld.getName());
         
         if (index != -1)
         {
             saves.set(index, newWorld);
             prepareForPlayerWrite(index);
             prepareForWorldWrite(index);
             return;
         }
 
         
         LogHelper.severe("Could not find matching world to save!!");
     }
     
     private static void prepareForWorldWrite(int index)
     {
         try
         {
             File worldFile = new File(FileHelper.getAbsoluteFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.WORLD_FILE_NAME);
             
             if (worldFile.exists())
             {
                 if (worldFile.delete())
                 {
                     File newWorldFile = new File(Files.createFile(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.WORLD_FILE_NAME)).toString());
                     writeToWorldFile(newWorldFile, saves.get(index).getBlocks());
                 }
             }
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
             return;
         }
     }
     
    public static void writeToWorldFile(File worldFile, ArrayList<WorldBlock> blocks) throws FileNotFoundException 
     {
         PrintWriter output = new PrintWriter(worldFile);
         
         try
         {
             for (WorldBlock b: blocks)
             {
                 output.print(b.getBlock().blockID + " ");
                 output.print((int) b.getCoords().x + " ");
                 output.println((int) b.getCoords().y);
             }
             
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
         }
         finally
         {
             output.close();
         }
     }
     
     private static void prepareForPlayerWrite(int index)
     {
         try
         {
             File playerFile = new File(FileHelper.getAbsoluteFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.PLAYER_FILE_NAME);
             
             if (playerFile.exists())
             {
                 if (playerFile.delete())
                 {
                     File newPlayerFile = new File(Files.createFile(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.PLAYER_FILE_NAME)).toString());
                     writeToPlayerFile(newPlayerFile);
                 }
             }
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
         }
     }
     
     public static void writeToPlayerFile(File playerFile) throws FileNotFoundException
     {
         PrintWriter output = new PrintWriter(playerFile);
         
         try
         {
             output.print((int) Player.getX());
             output.print(" ");
             output.print((int) Player.getY());
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
         }
         finally
         {
             output.close();
         }
     }
     
     public static int getPlayerX(String name) throws IOException
     {
         int index = getWorldMatch(name);
             
         if (index == -1)
             return 0;
         
         Scanner input = new Scanner(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.PLAYER_FILE_NAME));
         
         try
         {
             return input.nextInt();
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
             return 0;
         }
         finally
         {
             input.close();
         }
     }
     
     public static int getPlayerY(String name) throws IOException
     {
         int index = getWorldMatch(name);
             
         if (index == -1)
             return 0;
         
         Scanner input = new Scanner(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.PLAYER_FILE_NAME));
         
         try
         {
             input.nextInt();
             
             return input.nextInt();
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
             return 0;
         }
         finally
         {
             input.close();
         }
     }
     
     public static ArrayList<WorldBlock> getWorldSaveData(String name, PApplet host) throws IOException
     {
         int index = getWorldMatch(name);
         
         if (index == -1)
             return null;
         
         Scanner input = new Scanner(FileHelper.getPathFromString(FileHelper.getFileDirectoryString() + "saves" + File.separator + saves.get(index).getName() + File.separator + Saves.WORLD_FILE_NAME));
         
         ArrayList<WorldBlock> blocks = new ArrayList<WorldBlock>();
         
         try
         {
             while (input.hasNext())
             {
                 int id = input.nextInt();
                 int x = input.nextInt();
                 int y = input.nextInt();
                 
                 blocks.add(new WorldBlock(x, y, GameRegistry.getBlock(id), host));
             }
             
             return blocks;
         }
         catch (Exception e)
         {
             LogHelper.severe(MainReference.FILE_ERROR_MSG);
             e.printStackTrace();
             return null;
         }
         finally
         {
             input.close();
         }
     }
     
 }
