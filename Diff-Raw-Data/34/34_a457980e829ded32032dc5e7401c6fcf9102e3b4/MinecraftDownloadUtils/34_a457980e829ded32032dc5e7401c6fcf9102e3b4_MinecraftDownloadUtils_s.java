 package org.spoutcraft.launcher;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.spoutcraft.diff.JBPatch;
 import org.spoutcraft.launcher.async.Download;
 import org.spoutcraft.launcher.async.DownloadListener;
 
 public class MinecraftDownloadUtils {
 
 	public static void downloadMinecraft(String user, String output, ModpackBuild build, DownloadListener listener) throws IOException {
 		int tries = 3;
 		File outputFile = null;
 		while (tries > 0) {
 			System.out.println("Starting download of minecraft, with " + tries + " tries remaining");
 			tries--;
 			Download download = new Download(build.getMinecraftURL(user), output);
 			download.setListener(listener);
 			download.run();
 			if (!download.isSuccess()) {
 				if (download.getOutFile() != null) {
 					download.getOutFile().delete();
 				}
 				System.err.println("Download of minecraft failed!");
 				listener.stateChanged("Download Failed, retries remaining: " + tries, 0F);
 			} else {
 				String minecraftMD5 = MD5Utils.getMD5(FileType.minecraft, build.getLatestMinecraftVersion());
 				String resultMD5 = MD5Utils.getMD5(download.getOutFile());
 				System.out.println("Expected MD5: " + minecraftMD5 + " Result MD5: " + resultMD5);
 
 				if (!resultMD5.equals(minecraftMD5)) {
 					continue;
 				}
 
 				if (!build.getLatestMinecraftVersion().equals(build.getMinecraftVersion())) {
 					File patch = new File(GameUpdater.tempDir, "mc.patch");
 					Download patchDownload = DownloadUtils.downloadFile(build.getPatchURL(), patch.getPath(), null, null, listener);
 					if (patchDownload.isSuccess()) {
 						File patchedMinecraft = new File(GameUpdater.tempDir, "patched_minecraft.jar");
 						patchedMinecraft.delete();
 						JBPatch.bspatch(download.getOutFile(), patchedMinecraft, patch);
 						String currentMinecraftMD5 = MD5Utils.getMD5(FileType.minecraft, build.getMinecraftVersion());
 						resultMD5 = MD5Utils.getMD5(patchedMinecraft);
 
 						if (currentMinecraftMD5.equals(resultMD5)) {
 							outputFile = download.getOutFile();
 							download.getOutFile().delete();
 							GameUpdater.copy(patchedMinecraft, download.getOutFile());
 							patchedMinecraft.delete();
 							patch.deleteOnExit();
 							patch.delete();
 							break;
 						}
 					}
 				} else {
 					outputFile = download.getOutFile();
 					break;
 				}
 			}
 		}
 		if (outputFile == null) { throw new IOException("Failed to download minecraft"); }
 		GameUpdater.copy(outputFile, new File(GameUpdater.cacheDir, "minecraft_" + build.getMinecraftVersion() + ".jar"));
 	}
 }
