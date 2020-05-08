 package io.cloudsoft.mapr.m3;
 
 import brooklyn.entity.Entity;
 import brooklyn.entity.basic.Lifecycle;
 import brooklyn.entity.basic.SoftwareProcessEntity;
 import brooklyn.entity.trait.Startable;
 import brooklyn.event.basic.BasicAttributeSensor;
 import brooklyn.event.basic.BasicConfigKey;
 import brooklyn.location.MachineProvisioningLocation;
 import brooklyn.location.basic.jclouds.templates.PortableTemplateBuilder;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jclouds.compute.domain.OsFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
 
 public abstract class AbstractM3Node extends SoftwareProcessEntity implements Startable {
 
    public static final Logger log = LoggerFactory.getLogger(AbstractM3Node.class);
 
    public static BasicConfigKey<DiskSetupSpec> DISK_SETUP_SPEC = new BasicConfigKey<DiskSetupSpec>(DiskSetupSpec
            .class, "mapr.node.disk.setup");
    public static final BasicAttributeSensor<Boolean> ZOOKEEPER_UP = new BasicAttributeSensor<Boolean>(Boolean.class,
            "mapr.zookeeper.serviceUp", "whether zookeeper has been started");
 
    public static BasicConfigKey<String> MAPR_USERNAME = new BasicConfigKey<String>(String.class, "mapr.username",
            "initial user to create for mapr", "mapr");
    public static BasicConfigKey<String> MAPR_PASSWORD = new BasicConfigKey<String>(String.class, "mapr.password",
            "initial password for initial user");
 
    // TODO config param?  note, if this is not 'ubuntu', we have to create the user; see jclouds AdminAccess
    public String getUser() { return getConfig(MAPR_USERNAME); }
 
    public String getPassword() { return getConfig(MAPR_PASSWORD); }
 
    public void configureMetrics(String hostname) {
       getDriver().exec(ImmutableList.of("sudo /opt/mapr/server/configure.sh -R -d " + hostname + ":3306 -du " +
               getUser()
               + " -dp " +
               getPassword() + " -ds metrics"));
    }
 
    public void setupMapRUser() {
       getDriver().exec(ImmutableList.of(
               "sudo adduser " + getUser() + " < /dev/null || true",
               "echo \"" + getPassword() + "\n" + getPassword() + "\" | sudo passwd " + getUser() + ""));
    }
 
    public AbstractM3Node(Entity owner) {
       this(ImmutableMap.of(), owner);
    }
 
    public AbstractM3Node(Map properties, Entity owner) {
       super(properties, owner);
       setAttribute(SERVICE_UP, false);
       setAttribute(SERVICE_STATE, Lifecycle.CREATED);
    }
 
    public boolean isZookeeper() { return false; }
 
    public boolean isMaster() { return false; }
 
    public List<String> getAptPackagesToInstall() {
       return ImmutableList.of("mapr-metrics");
    }
 
    @Override
    public Class<? extends M3NodeDriver> getDriverInterface() {
       return M3NodeDriver.class;
    }
 
    @Override
    public M3NodeDriver getDriver() {
       return (M3NodeDriver) super.getDriver();
    }
 
    public static final BasicAttributeSensor<String> SUBNET_HOSTNAME = new BasicAttributeSensor<String>(String.class,
            "machine.subnet.hostname", "internally resolvable hostname");
 
    protected Map<String, Object> getProvisioningFlags(MachineProvisioningLocation location) {
       return obtainProvisioningFlags(location);
    }
 
    protected Map<String, Object> obtainProvisioningFlags(MachineProvisioningLocation location) {
      Map<String, Object> flags = Maps.newLinkedHashMap();

       flags.put("templateBuilder", new PortableTemplateBuilder().
               osFamily(OsFamily.UBUNTU).osVersionMatches("11.04").os64Bit(true).
               minRam(2560));
 
       flags.put("userName", "ubuntu");
      flags.put("inboundPorts", ImmutableList.of(22, 2048, 3306, 5660, 5181, 7221, 7222, 8080, 8443, 9001, 9997, 9998,
              50030, 50060, 60000, 2888, 3888));
       return flags;
    }
 
    public abstract void startServices();
 
 }
