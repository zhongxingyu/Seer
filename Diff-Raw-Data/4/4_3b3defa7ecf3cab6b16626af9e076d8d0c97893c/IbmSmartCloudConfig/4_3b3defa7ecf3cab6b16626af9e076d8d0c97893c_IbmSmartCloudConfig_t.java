 package brooklyn.location.ibm.smartcloud;
 
 import brooklyn.config.ConfigKey;
 import brooklyn.entity.basic.ConfigKeys;
 import brooklyn.location.cloud.CloudLocationConfig;
 
 public interface IbmSmartCloudConfig extends CloudLocationConfig {
    public static final ConfigKey<String> LOCATION = ConfigKeys.newStringConfigKey("location",
            "Override the location configured (default 'Raleigh')", "Raleigh");
    public static final ConfigKey<String> IMAGE = ConfigKeys.newStringConfigKey("image",
           "Override the image configured (default 'Red Hat Enterprise Linux 6.4 (64-bit)')",
           "Red Hat Enterprise Linux 6.4 (64-bit)");
    public static final ConfigKey<String> INSTANCE_TYPE_LABEL = ConfigKeys.newStringConfigKey("instanceType",
            "Override the instanceType configured (default 'Copper')", "Copper");
    
    public static final ConfigKey<Long> CLIENT_POLL_TIMEOUT_MILLIS =
            ConfigKeys.newLongConfigKey("sce.client.poll.timeout", "how long to wait for the machine to be known via the SCE client, in millis", 90*60*1000L);
    public static final ConfigKey<Long> CLIENT_POLL_PERIOD_MILLIS =
            ConfigKeys.newLongConfigKey("sce.client.poll.period", "how long to wait between ssh loop iterations (default 30 seconds)", 30*1000L);
 
    public static final ConfigKey<Integer> INSTANCE_CREATION_RETRIES = 
            ConfigKeys.newIntegerConfigKey("instance.creation.retries", "how many retries to attempt to create a new instance (default 5 times)", 5);
 
    public static final ConfigKey<Boolean> SELINUX_DISABLED =
            ConfigKeys.newBooleanConfigKey("selinux.disabled", "whether to disable SElinux", false);
    public static final ConfigKey<Boolean> STOP_IPTABLES = 
            ConfigKeys.newBooleanConfigKey("stop.iptables", "whether to stop iptables", false);
    public static final ConfigKey<Long> SSH_REACHABLE_TIMEOUT_MILLIS =
          ConfigKeys.newLongConfigKey("ssh.reachable.timeout", "how long to wait for the machine to be sshable, in millis", 5*60*1000L);
    public static final ConfigKey<Boolean> SSHD_SUBSYSTEM_ENABLE =
          ConfigKeys.newBooleanConfigKey("sshd.subsystem.enable", "whether to ssh and reconfigure ssh_config so Subsystem line is enabled", true);
    
    public static final ConfigKey<String> KEYPAIR_NAME = 
            ConfigKeys.newStringConfigKey("ibm.sce.keypair.name");
    
    public static final ConfigKey<Boolean> INSTALL_LOCAL_AUTHORIZED_KEYS = 
            ConfigKeys.newBooleanConfigKey("ibm.sce.install.local.authorized_keys", "whether to install any locally authorized keys, for convenience", true);
 
 }
