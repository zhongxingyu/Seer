 package org.hadoop.tdg;
 
 import com.google.common.io.Files;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hdfs.MiniDFSCluster;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.util.Progressable;
 import org.junit.*;
 
 import java.io.*;
 import java.net.URL;
 
import static com.google.common.base.Preconditions.checkNotNull;

 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 public class TestPseudoHadoop {
 
     private static final String DST = "/user/" + System.getProperty("user.name");
     private static final String HOME = System.getProperty("user.home");
    private static final String DST_FILE = DST+"/test";
     private static final String HOME_FILE = HOME + "/test";
     private MiniDFSCluster cluster;
     private FileSystem fs;
 
     @BeforeClass
     public static void setUpClass() throws IOException {
         RandomAccessFile f = null;
         try {
             f = new RandomAccessFile(HOME_FILE, "rw");
             f.setLength(4096 * 1000);
         } finally {
             IOUtils.closeStream(f);
         }

     }
 
     @Before
     public void setUp() throws IOException {
        Configuration configuration = new Configuration();
         if (System.getProperty("test.build.data") == null) {
             System.setProperty("test.build.data", "/tmp");
         }
        cluster = new MiniDFSCluster(configuration, 1, true, null);
         fs = cluster.getFileSystem();
         copyFileWithProgress();
     }
 
     @After
     public void tearDown() throws IOException {
        checkNotNull(fs);
        fs.close();
        checkNotNull(cluster);
        cluster.shutdown();
     }
 
     public void copyFileWithProgress() throws IOException {
         InputStream in = null;
         OutputStream out = null;
         try {
             in = new BufferedInputStream(new FileInputStream(HOME_FILE));
 //            FileSystem fs = FileSystem.get(URI.create(DST), conf);
             out = fs.create(new Path(DST_FILE), new Progressable() {
                 @Override
                 public void progress() {
                     System.out.print("~");
                 }
             });
 
             IOUtils.copyBytes(in, out, 4096, true);
         } finally {
             IOUtils.closeStream(in);
             IOUtils.closeStream(out);
         }
     }
 
     @Test
    @Ignore("StackOverflowError with *-site.xml")
     public void readWithURLHandler() throws IOException {
        URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
        printStream(new URL(fs.getUri() + DST_FILE).openStream());
     }
 
     @Test
     public void readWithFileSystem() throws IOException {
 //        FileSystem fs = FileSystem.get(URI.create(DST_FILE), conf);
         FSDataInputStream is = fs.open(new Path(DST_FILE));
         is.seek(23);
         printStream(is);
     }
 
     private void printStream(InputStream is) throws IOException {
         File f1 = new File(HOME_FILE);
         File f2 = new File(HOME + "/test.cpy");
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(f2);
             IOUtils.copyBytes(is, fos, 4096, false);
             Files.equal(f1, f2);
         } finally {
             IOUtils.closeStream(is);
             IOUtils.closeStream(fos);
         }
     }
 
 }
