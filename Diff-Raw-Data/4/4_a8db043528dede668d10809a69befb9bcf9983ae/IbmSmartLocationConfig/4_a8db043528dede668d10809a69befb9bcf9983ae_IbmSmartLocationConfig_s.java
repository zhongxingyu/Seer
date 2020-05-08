 package brooklyn.location.ibm.smartcloud;
 
 import brooklyn.config.ConfigKey;
 import brooklyn.entity.basic.ConfigKeys;
 import brooklyn.location.cloud.CloudLocationConfig;
 
 public interface IbmSmartLocationConfig extends CloudLocationConfig {
    public static final ConfigKey<String> LOCATION = ConfigKeys.newStringConfigKey("location",
            "Override the location configured (default 'Raleigh')", "Raleigh");
    public static final ConfigKey<String> IMAGE = ConfigKeys.newStringConfigKey("image",
           "Override the image configured (default 'Red Hat Enterprise Linux 6.3 (64-bit)')",
           "Red Hat Enterprise Linux 6.3 (64-bit)");
    public static final ConfigKey<String> INSTANCE_TYPE_LABEL = ConfigKeys.newStringConfigKey("instanceType",
            "Override the instanceType configured (default 'Copper')",
            "Copper");
    
    public static final ConfigKey<Long> CLIENT_POLL_TIMEOUT_MILLIS =
            ConfigKeys.newLongConfigKey("sce.client.poll.timeout", "how long to wait for the machine to be known via the SCE client, in millis", 60*60*1000L);
    public static final ConfigKey<Long> CLIENT_POLL_PERIOD_MILLIS =
            ConfigKeys.newLongConfigKey("sce.client.poll.period", "how long to wait between ssh loop iterations (default 30 seconds)", 30L);
 
    public static final ConfigKey<Long> SSH_REACHABLE_TIMEOUT_MILLIS =
            ConfigKeys.newLongConfigKey("ssh.reachable.timeout", "how long to wait for the machine to be sshable, in millis", 5*60*1000L);
    public static final ConfigKey<Boolean> SSHD_SUBSYSTEM_ENABLE =
            ConfigKeys.newBooleanConfigKey("sshd.subsystem.enable", "whether to ssh and reconfigure ssh_config so Subsystem line is enabled", true);
 }
