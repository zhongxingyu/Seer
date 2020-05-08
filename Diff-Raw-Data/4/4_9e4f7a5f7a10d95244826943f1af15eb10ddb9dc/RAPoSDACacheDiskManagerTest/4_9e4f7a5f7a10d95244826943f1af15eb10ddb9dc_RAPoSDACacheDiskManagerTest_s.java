 package sim.storage.manager.cdm;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import sim.Block;
 import sim.Parameter;
 import sim.storage.CacheResponse;
 import sim.storage.DiskResponse;
 import sim.storage.HDDParameter;
 import sim.storage.HardDiskDrive;
 import sim.storage.state.DiskStateParameter;
 import sim.storage.state.WithoutSleepDiskStateManager;
 
 import java.util.HashMap;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 
 @RunWith(JUnit4.class)
 public class RAPoSDACacheDiskManagerTest {
 
     private static HDDParameter dParam;
     private static DiskStateParameter dstParam;
     private static double aBlockResp;
 
     private static final int BLOCK_SIZE = 1;
     private static final int HDD_SIZE = 2;
 
     @BeforeClass
     public static void setUp() {
 
         dParam = new HDDParameter(
                 HDD_SIZE, //size
                 Parameter.HDD_NUMBER_OF_PLATTER,
                 Parameter.HDD_RPM,
                 Parameter.HDD_CACHE_SIZE,
                 Parameter.HDD_TRANSFER_RATE,
                 Parameter.HDD_SECTORS_PER_TRACK,
                 Parameter.HDD_FULL_STROKE_SEEK_TIME,
                 Parameter.HDD_HEAD_SWITCH_OVERHEAD,
                 Parameter.HDD_COMMAND_OVERHEAD
         );
 
         dstParam = new DiskStateParameter(
                 Parameter.HDD_ACTIVE_POWER,
                 Parameter.HDD_IDLE_POWER,
                 Parameter.HDD_STANDBY_POWER,
                 Parameter.HDD_SPINDOWN_ENERGY,
                 Parameter.HDD_SPINUP_ENERGY,
                 Parameter.HDD_SPINDOWN_TIME,
                 Parameter.HDD_SPINUP_TIME);
 
         HardDiskDrive hdd = new HardDiskDrive(0, dParam);
         aBlockResp = hdd.write(
                 new Block[]{new Block(0, 0.0, 0)});
     }
 
     private HashMap<Integer, CacheDisk> getCacheDiskMap(int numcd) {
         HashMap<Integer, CacheDisk> result = new HashMap<Integer, CacheDisk>();
         for (int i = 0; i < numcd; i++) {
             result.put(i, new CacheDisk(i, BLOCK_SIZE, dParam, getCDStm()));
         }
         return result;
     }
 
     private WithoutSleepDiskStateManager getCDStm() {
         return new WithoutSleepDiskStateManager(dstParam);
     }
 
     @Test
     public void writeIntoCDM() {
         int numcd = 3;
         RAPoSDACacheDiskManager cdm = new RAPoSDACacheDiskManager(numcd, getCacheDiskMap(numcd));
 
         Block[] blocks;
         DiskResponse cdres;
 
         Block b01 = new Block(0, 0.0, 0);
         Block b02 = new Block(1, 0.0, 0);
         blocks = new Block[]{b01};
         cdres = cdm.write(blocks);
         assertThat(cdres.getResults(), is(blocks));
         assertThat(cdres.getResults(), not(new Block[]{b02}));
     }
 
     @Test
     public void writeIdempotent() {
         int numcd = 2;
         RAPoSDACacheDiskManager cdm = new RAPoSDACacheDiskManager(numcd, getCacheDiskMap(numcd));
 
         Block[] blocks;
         DiskResponse cdres;
 
         Block b00 = new Block(0, 0.0, 0);
         Block b01 = new Block(1, 0.1, 0);
         Block b02 = new Block(2, 0.2, 0);
         Block b03 = new Block(3, 0.3, 0);
         blocks = new Block[]{b00, b01, b02, b03};
         cdres = cdm.write(blocks);
         assertThat(cdres.getResults(), is(blocks));
 
         // replace check.
         Block b05 = new Block(4, 0.4, 0);
         blocks = new Block[]{b05};
         cdres = cdm.write(blocks);
         assertThat(cdres.getResults(), is(new Block[]{b00}));
 
         // idempotent check.
         Block b04 = new Block(0, 0.5, 0);
         blocks = new Block[]{b04};
         cdres = cdm.write(blocks);
         assertThat(cdres.getResults(), is(new Block[]{b01}));
 
         Block b06 = new Block(0, 0.6, 0);
         blocks = new Block[]{b06};
         cdres = cdm.write(blocks);
         assertThat(cdres.getResults(), is(blocks));
     }
 
     @Test
     public void readFromCDM() {
         int numcd = 3;
         RAPoSDACacheDiskManager cdm = new RAPoSDACacheDiskManager(numcd, getCacheDiskMap(numcd));
 
         Block[] blocks;
         CacheResponse rres;
 
         Block b01 = new Block(0, 0.0, 0);
         Block b02 = new Block(1, 1.0, 0);
         blocks = new Block[]{b01, b02};
         cdm.write(blocks);
 
         Block rd01 = new Block(0, 2.0, 1);
         rres = cdm.read(rd01);
         assertThat(rres.getResponseTime(), is(aBlockResp));
         assertThat(rres.getResult(), is(rd01));
 
         // read a data before cached
         Block rd02before = new Block(1, 0.5, 2);
         rres = cdm.read(rd02before);
         assertThat(rres.getResponseTime(), is(Double.MAX_VALUE));
         assertThat(rres.getResult(), is(Block.NULL));
 
         Block rd02 = new Block(1, 2.0, 2);
         rres = cdm.read(rd02);
         assertThat(rres.getResponseTime(), is(aBlockResp));
         assertThat(rres.getResult(), is(rd02));
     }
 
     @Test
     public void readFromCDMWthNoCacheDisk() {
         int numcd = 0;
         RAPoSDACacheDiskManager cdm = new RAPoSDACacheDiskManager(numcd, getCacheDiskMap(numcd));
 
         Block[] blocks;
         CacheResponse cacheRes;
         DiskResponse diskRes;
 
         Block b01 = new Block(0, 0.0, 0);
         Block b02 = new Block(1, 1.0, 0);
         blocks = new Block[]{b01, b02};
         diskRes = cdm.write(blocks);
         assertThat(diskRes.getResponseTime(), is(0.0));
         assertThat(diskRes.getResults(), is(blocks));
 
         Block rd01 = new Block(0, 2.0, 1);
         cacheRes = cdm.read(rd01);
        assertThat(cacheRes.getResponseTime(), is(0.0));
        assertThat(cacheRes.getResult(), is(rd01));
     }
 
     @Test
     public void LRUreplace() {
         int numcd = 3;
         RAPoSDACacheDiskManager cdm = new RAPoSDACacheDiskManager(numcd, getCacheDiskMap(numcd));
 
         Block[] blocks;
         DiskResponse wres;
         CacheResponse rres;
 
         // there are 6 blocks spaces in cache disks.
         Block b00 = new Block(0, 0.0, 0);
         Block b01 = new Block(1, 0.0, 0);
         Block b02 = new Block(2, 0.0, 0);
         Block b03 = new Block(3, 0.1, 0);
         Block b04 = new Block(4, 0.0, 0);
         Block b05 = new Block(5, 0.0, 0);
 
         blocks = new Block[]{b00, b01, b02, b03, b04, b05};
         wres = cdm.write(blocks);
         assertThat(wres.getResponseTime(), is(aBlockResp * 2));
         assertThat(wres.getResults(), is(blocks));
 
         Block b06 = new Block(6, 1.0, 0);
         blocks = new Block[]{b06};
         wres = cdm.write(blocks);
         // replaced b00 on disk cd0
         assertThat(wres.getResponseTime(), is(aBlockResp));
         assertThat(wres.getResults()[0], is(b00));
 
         Block rd00 = new Block(0, 2.0, 0);
         rres = cdm.read(rd00);
         assertThat(rres.getResponseTime(), is(Double.MAX_VALUE));
         assertThat(rres.getResult(), is(Block.NULL));
 
         Block rd03 = new Block(3, 2.5, 0);
         rres = cdm.read(rd03);
         assertThat(rres.getResponseTime(), is(aBlockResp));
         assertThat(rres.getResult(), is(rd03));
 
         Block rd06 = new Block(6, 3.0, 0);
         rres = cdm.read(rd06);
         assertThat(rres.getResponseTime(), is(aBlockResp));
         assertThat(rres.getResult(), is(rd06));
     }
 
 }
