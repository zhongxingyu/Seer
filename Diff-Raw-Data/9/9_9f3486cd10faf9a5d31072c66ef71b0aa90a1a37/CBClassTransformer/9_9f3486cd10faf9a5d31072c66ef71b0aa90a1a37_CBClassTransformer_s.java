 package mod.culegooner;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class CBClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {
 
 	@Override
 	public byte[] transform(String arg0, String arg1, byte[] arg2) {
 		
 		if (arg0.equals("tc") || arg0.equals("net.minecraft.entity.monster.EntityCreeper")) {
			System.out.println("********* INSIDE TRANSFORMER: " + arg0);
 			arg2 = patchClassInJar(arg0, arg2, arg0, CBFMLLoadingPlugin.location);
         }
         return arg2;
 	}
 
 	 public byte[] patchClassInJar(String name, byte[] bytes, String ObfName, File location) {
 	        try {
 	            ZipFile zip = new ZipFile(location);
 	            ZipEntry entry = zip.getEntry(name.replace('.', '/') + ".class");
 	            if (entry == null) {
 	                System.out.println(name + " not found in " + location.getName());
 	            } else {
 	                InputStream zin = zip.getInputStream(entry);
 	                bytes = new byte[(int) entry.getSize()];
 	                zin.read(bytes);
 	                zin.close();
 	                System.out.println("[" + "CreeperBurnCore" + "]: " + "Class " + name + " patched!");
 	            }
 	            zip.close();
 	        } catch (Exception e) {
 	            throw new RuntimeException("Error overriding " + name + " from " + location.getName(), e);
 	        }
 	        return bytes;
 	    }
 }
