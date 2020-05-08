  package cpw.mods.fml.installer;
  
  import argo.format.PrettyJsonFormatter;
  import argo.jdom.JdomParser;
  import argo.jdom.JsonField;
  import argo.jdom.JsonNodeFactories;
  import argo.jdom.JsonRootNode;
  import argo.saj.InvalidSyntaxException;
  import com.google.common.base.Charsets;
  import com.google.common.base.Throwables;
  import com.google.common.collect.Maps;
  import com.google.common.io.Files;
  import java.io.BufferedWriter;
  import java.io.File;
  import java.io.IOException;
  import java.util.HashMap;
  import javax.swing.JOptionPane;
  
  public class ClientInstall
    implements ActionType
  {
    public boolean run(File target)
    {
      if (!target.exists())
      {
        JOptionPane.showMessageDialog(null, "There is no minecraft installation at this location!", "Error", 0);
        return false;
      }
      File launcherProfiles = new File(target, "launcher_profiles.json");
      if (!launcherProfiles.exists())
      {
        JOptionPane.showMessageDialog(null, "There is no minecraft launcher profile at this location, you need to run the launcher first!", "Error", 0);
        return false;
      }
  
      File versionRootDir = new File(target, "versions");
      File versionTarget = new File(versionRootDir, VersionInfo.getVersionTarget());
      if ((!versionTarget.mkdirs()) && (!versionTarget.isDirectory()))
      {
        if (!versionTarget.delete())
        {
          JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear " + versionTarget.getAbsolutePath() + " manually", "Error", 0);
        }
        else
        {
          versionTarget.mkdirs();
        }
      }
  
      File versionJsonFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".json");
      File versionJarFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".jar");
      File minecraftJarFile = VersionInfo.getMinecraftFile(versionRootDir);
      try
      {
        Files.copy(minecraftJarFile, versionJarFile);
      }
      catch (IOException e1)
      {
        JOptionPane.showMessageDialog(null, "You need to run the version " + VersionInfo.getMinecraftVersion() + " manually at least once", "Error", 0);
        return false;
      }
 
      File targetLibraryFile = VersionInfo.getLibraryPath(new File(target, "libraries"));
      if ((!targetLibraryFile.getParentFile().mkdirs()) && (!targetLibraryFile.getParentFile().isDirectory()))
      {
        if (!targetLibraryFile.getParentFile().delete())
        {
          JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear " + targetLibraryFile.getAbsolutePath() + " manually", "Error", 0);
          return false;
        }
  
        targetLibraryFile.getParentFile().mkdirs();
      }
  
      JsonRootNode versionJson = JsonNodeFactories.object(VersionInfo.getVersionInfo().getFields());
      try
      {
        BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
        PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson, newWriter);
        newWriter.close();
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", 0);
        return false;
      }
  
      try
      {
        VersionInfo.extractFile(targetLibraryFile);
      }
      catch (IOException e)
      {
        JOptionPane.showMessageDialog(null, "There was a problem writing the system library file", "Error", 0);
        return false;
      }
  
 	  JsonRootNode jsonProfileData;
      JdomParser parser = new JdomParser();
      try
      {
        jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
      }
      catch (InvalidSyntaxException e)
      {
        JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", 0);
        return false;
      }
      catch (Exception e)
      {
        throw Throwables.propagate(e);
      }
 
       File modsFolder = new File(target, "mods");
 	  try
 	  {
 		  if (!modsFolder.isDirectory() || !modsFolder.exists()) {
 			  modsFolder.mkdir();
 		  } else {
 			  for (File f : modsFolder.listFiles()) {
 				  if (f.getName().toLowerCase().contains("civwars")) {
 					  f.delete();
 				  }
 			  }
 		  }
 	  } catch (Exception e) {
 		  JOptionPane.showMessageDialog(null, "There was a problem removing old CivWars versions.", "Error", 0);
 		  return false;
 	  }
 	  
 	  try {
 		  VersionInfo.extractCivWarsFile(modsFolder);
 	  } catch (Exception e) {
 		  JOptionPane.showMessageDialog(null, "There was a problem extracting CivWars to the mods folder.", "Error", 0);
 		  return false;
 	  }
  
      JsonField[] fields = { JsonNodeFactories.field("name", JsonNodeFactories.string(VersionInfo.getProfileName())), JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(VersionInfo.getVersionTarget())) };
  
      HashMap profileCopy = Maps.newHashMap(jsonProfileData.getNode(new Object[] { "profiles" }).getFields());
      HashMap rootCopy = Maps.newHashMap(jsonProfileData.getFields());
      profileCopy.put(JsonNodeFactories.string(VersionInfo.getProfileName()), JsonNodeFactories.object(fields));
      JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);
  
      rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);
  
      jsonProfileData = JsonNodeFactories.object(rootCopy);
      try
      {
        BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
        PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
        newWriter.close();
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", 0);
        return false;
      }
  
      return true;
    }
  
    public boolean isPathValid(File targetDir)
    {
      if (targetDir.exists())
      {
        File launcherProfiles = new File(targetDir, "launcher_profiles.json");
        return launcherProfiles.exists();
      }
      return false;
    }
  
    public String getFileError(File targetDir)
    {
      if (targetDir.exists())
      {
        return "The directory is missing a launcher profile. Please run the minecraft launcher first";
      }
  
      return "There is no minecraft directory set up. Either choose an alternative, or run the minecraft launcher to create one";
    }
  
    public String getSuccessMessage()
    {
     return String.format("Successfully installed client profile %s for version %s into launcher", new Object[] { VersionInfo.getProfileName(), VersionInfo.getVersion() });
    }
  }
 
 /* Location:           C:\Users\User\Desktop\minecraftforge-installer-1.6.2-9.10.0.804.jar
  * Qualified Name:     cpw.mods.fml.installer.ClientInstall
  * JD-Core Version:    0.6.2
  */
