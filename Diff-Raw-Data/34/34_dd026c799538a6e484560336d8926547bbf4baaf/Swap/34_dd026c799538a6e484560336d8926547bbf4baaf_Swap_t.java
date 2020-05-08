 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease;
 
 import net.oneandone.maven.plugins.prerelease.core.Archive;
 import net.oneandone.maven.plugins.prerelease.core.Target;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.fs.filter.Filter;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 import java.util.List;
 
 /**
  * Wipes archives and moves prerelease directories from primary storage to secondary storage. Also wipes all archives.
 * Useful when you use ram disks: use the ramdisk as primary storage, and a hardisk as secondary storage.
  */
 @Mojo(name = "swap", requiresProject = false)
 public class Swap extends Base {
     /**
      * Location of the secondary storage.
      */
     @Parameter(property = "prerelease.swap", required = true)
     private String swap;
 
     @Override
     public void doExecute() throws Exception {
         FileNode primary;
         List<Node> directories;
         Archive archive;
         FileNode secondary;
         FileNode dest;
 
         primary = world.file(storage);
        // TODO: sushi bug?
        primary = world.file(primary.toPath().toFile().getCanonicalFile());
         if (!primary.exists()) {
             return;
         }
         secondary = world.file(swap);
         directories = primary.find("*/*");
         for (Node dir : directories) {
             if (!dir.isDirectory()) {
                 continue;
             }
             archive = Archive.tryOpen((FileNode) dir);
             if (archive == null) {
                 getLog().info("skipped: " + dir);
             } else {
                 try {
                     archive.wipe(keep, null);
                     for (Node src : dir.list()) {
                         if (src.isLink()) {
                             continue;
                         }
                         if (src.getName().equals(Target.REMOVE)) {
                             continue;
                         }
                         dest = secondary.join(dir.getRelative(primary), src.getName());
                         dest.mkdirs();
                        src.copyDirectory(dest, new Filter().includeAll());
                         src.deleteTree();
                        getLog().info("swapped " + ((FileNode) src).getAbsolute() + " -> " + dest.getAbsolute());
                        dest.link(src);
                     }
                 } finally {
                     archive.close();
                 }
             }
         }
         getLog().info(directories.size() + " archives processed.");
     }
 }
