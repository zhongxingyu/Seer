 package com.pivotal.hamster.cli.parser;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.pivotal.hamster.cli.parser.HamsterParamBuilder;
 
 public class HamsterParamBuilderTest {
   @Test
   public void testBuildParam() throws IOException {
     String input = "mpirun --hamster-mem 512 --hamster-cpu 1 -mca mca_aaa   mca_aaa_v -np 2 -mca xxx yyy -mca routed hello --add-file file.0 --add-file file://tmp/data/file.1#file.1 --add-archive home/data.tar.gz#data0 --hamster-verbose mpi_hello hello_world";
     HamsterParamBuilder builder = new HamsterParamBuilder();
     builder.parse(input.split(" "));
     String cmd = builder.getUserCli(null);
    Assert.assertEquals("$JAVA_HOME/bin/java -Xmx512M -Xms16M -cp hamster-core.jar com.pivotal.hamster.appmaster.HamsterAppMaster mpirun -mca odls yarn -mca xxx yyy -mca mca_aaa mca_aaa_v -mca routed hello -mca plm yarn -mca ras yarn -np 2 mpi_hello hello_world 1><LOG_DIR>/stdout 2><LOG_DIR>/stderr", cmd);
     Assert.assertEquals("file.0", builder.getAddFiles().get(0));
     Assert.assertEquals("file://tmp/data/file.1#file.1", builder.getAddFiles().get(1));
     Assert.assertEquals("home/data.tar.gz#data0", builder.getAddArchives().get(0));
     Assert.assertEquals(true, builder.isVerbose());
     
     Assert.assertEquals(512, builder.getHamsterMemory());
     Assert.assertEquals(1, builder.getHamsterCPU());
     Assert.assertEquals(2, builder.getNp());
   }
 }
