 package nl.rutgerkok.BetterEnderChest.converter;
 
 import java.io.File;
 
 public class BECConsole {
     public BECConsole(String[] startupArgs)
     {
         
         // Validate the input
         try{
             validateString(startupArgs[0],"from","to");
             
             File levelDat = new File(startupArgs[1]);
             validateLevelDat(levelDat);
             
             validateString(startupArgs[2],"SERVER_ROOT","PLUGIN_FOLDER");
             
             
             //Convert
             startConversion(startupArgs);
         }
         catch(IllegalArgumentException e)
         {
             System.out.println("Invalid input: "+e.getMessage());
             System.out.println("Exiting...");
             System.exit(0);
         }
         
         
         
     }
     
     
     private void startConversion(String[] startupArgs) {
         // startupArgs MUST BE valid
         
         boolean toPluginFileFormat = startupArgs[0].equalsIgnoreCase("to");
         File levelDat = new File(startupArgs[1]);
         boolean useServerRoot = startupArgs[2].equalsIgnoreCase("SERVER_ROOT");
         
         // Chest directory
         File chestDirectory;
         if(useServerRoot)
            chestDirectory = new File(levelDat.getParentFile().getPath() + "/chests");
         else
            chestDirectory = new File(levelDat.getParentFile().getPath() + "/plugins/BetterEnderChest/chests");
         
         // Player directory
         File playerDirectory = new File(levelDat.getParentFile().getPath() + "/players");
         
         // Start!
         if (!toPluginFileFormat) {
             new BECConvertDirectoryBECVanilla(chestDirectory, playerDirectory);
         } else {
             new BECConvertDirectoryVanillaBEC(chestDirectory, playerDirectory);
         }
     }
     
     /**
      * Makes sure toCheck is in strings (case-insenstive).
      * @param toCheck
      * @param strings
      * @throws IllegalArgumentException If toCheck isn't in strings (case-insensitve)
      */
     public void validateString(String toCheck, String... strings)
     {
         for(String string: strings)
         {
             if(string.equalsIgnoreCase(toCheck)){
                 return;
             }
         }
         
         // toCheck is not in the possible values
         throw new IllegalArgumentException(toCheck + " is not a valid value!");
     }
     
     /**
      * Makes sure that the file is an existing level.dat
      * @param file
      * @throws IllegalArgumentException If it is invalid.
      */
     public void validateLevelDat(File file)
     {
         if(!file.exists()) {
             throw new IllegalArgumentException(file.getAbsolutePath() + " doesn't exist!");
         }
         
         if(!file.getName().equals("level.dat")) {
             throw new IllegalArgumentException(file.getAbsolutePath() + " is not a level.dat!");
         }
     }
 }
