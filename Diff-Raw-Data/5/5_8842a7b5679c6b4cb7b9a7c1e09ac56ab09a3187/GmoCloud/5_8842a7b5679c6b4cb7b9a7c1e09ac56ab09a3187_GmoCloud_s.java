 package gmo.cloud.api;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.StringUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.TreeMap;
 
 /**
  * GMO Cloud API client.
  * Pass null for the optional parameters, if you don't need it.
  * <p/>
  * Dependency: Apache HttpClient 4.x, Apache Commons Codec, Apache Commons Logging
  */
 public class GmoCloud {
     public static final String DOMAIN = "api.gmocloud.com";
     public static final String VERSION = "1.0";
 
     private static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00");
 
     private final String accessKeyId, secretAccessKey;
     private String cloudZoneID;
 
     static {
         initStatic();
     }
 
     public GmoCloud(String accessKeyId, String secretAccessKey, String cloudZoneID) {
         checkParameter("accessKeyId", accessKeyId);
         checkParameter("secretAccessKey", secretAccessKey);
         checkParameter("cloudZoneID", cloudZoneID);
 
         this.accessKeyId = accessKeyId;
         this.secretAccessKey = secretAccessKey;
         this.cloudZoneID = cloudZoneID;
     }
 
     public void setCloudZoneID(String cloudZoneID) {
         checkParameter("cloudZoneID", cloudZoneID);
 
         this.cloudZoneID = cloudZoneID;
     }
 
     //----------------------------------------------------------------------------------------------------
     // Transaction Log
     //----------------------------------------------------------------------------------------------------
 
     public String listTransactionLogsJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listTransactionLogs");
         return executeGetRequest(queryParameters);
     }
 
     public String listNodeTransactionLogsJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listNodeTransactionLogs");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Hypervisor
     //----------------------------------------------------------------------------------------------------
 
     public String listHypervisorsJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listHypervisors");
         return executeGetRequest(queryParameters);
     }
 
     public String getHypervisorJson(String hypervisor_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getHypervisor");
         addMandatoryParameter(queryParameters, "hypervisor_id", hypervisor_id);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Virtual Machine
     //----------------------------------------------------------------------------------------------------
 
     public String listNodesJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listNodes");
         return executeGetRequest(queryParameters);
     }
 
     public String getNodeJson(String virtual_machine_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getNode");
         addMandatoryParameter(queryParameters, "virtual_machine_id", virtual_machine_id);
         return executeGetRequest(queryParameters);
     }
 
     public String startupNodeJson(String Identifier,
                                   String Recovery)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("startupNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addOptionalParameter(queryParameters, "Recovery", Recovery);
         return executeGetRequest(queryParameters);
     }
 
    public String shutdownNodeJson(String Identifier,
                                   String Recovery)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("shutdownNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
        addMandatoryParameter(queryParameters, "Recovery", Recovery);
         return executeGetRequest(queryParameters);
     }
 
     public String buildNodeJson(String Identifier,
                                 String template_id,
                                 Integer required_startup)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("buildNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "template_id", template_id);
         addOptionalParameter(queryParameters, "required_startup", required_startup);
         return executeGetRequest(queryParameters);
     }
 
     public String rebuildNodeJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("rebuildNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String deployNodeJson(String template_id,
                                  Integer cpu_shares,
                                  int cpus,
                                  String data_store_group_primary_id,
                                  String data_store_group_swap_id,
                                  String hostname,
                                  String hypervisor_group_id,
                                  String hypervisor_id,
                                  String initial_root_password,
                                  String label,
                                  int memory,
                                  String note,
                                  int primary_disk_size,
                                  String primary_network_group_id,
                                  String primary_network_id,
                                  int rate_limit,
                                  Integer required_automatic_backup,
                                  Integer required_virtual_machine_build,
                                  Integer selected_ip_address_id,
                                  int swap_disk_size)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("deployNode");
         addMandatoryParameter(queryParameters, "template_id", template_id);
         addOptionalParameter(queryParameters, "cpu_shares", cpu_shares);
         addMandatoryParameter(queryParameters, "cpus", cpus);
         addOptionalParameter(queryParameters, "data_store_group_primary_id", data_store_group_primary_id);
         addOptionalParameter(queryParameters, "data_store_group_swap_id", data_store_group_swap_id);
         addMandatoryParameter(queryParameters, "hostname", hostname);
         addOptionalParameter(queryParameters, "hypervisor_group_id", hypervisor_group_id);
         addOptionalParameter(queryParameters, "hypervisor_id", hypervisor_id);
         addOptionalParameter(queryParameters, "initial_root_password", initial_root_password);
         addMandatoryParameter(queryParameters, "label", label);
         addMandatoryParameter(queryParameters, "memory", memory);
         addOptionalParameter(queryParameters, "note", note);
         addMandatoryParameter(queryParameters, "primary_disk_size", primary_disk_size);
         addOptionalParameter(queryParameters, "primary_network_group_id", primary_network_group_id);
         addOptionalParameter(queryParameters, "primary_network_id", primary_network_id);
         addMandatoryParameter(queryParameters, "rate_limit", rate_limit);
         addOptionalParameter(queryParameters, "required_automatic_backup", required_automatic_backup);
         addOptionalParameter(queryParameters, "required_virtual_machine_build", required_virtual_machine_build);
         addOptionalParameter(queryParameters, "selected_ip_address_id", selected_ip_address_id);
         addMandatoryParameter(queryParameters, "swap_disk_size", swap_disk_size);
         return executeGetRequest(queryParameters);
     }
 
     public String destroyNodeJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("destroyNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String editNodeJson(String Identifier,
                                String label,
                                String note)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("editNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addOptionalParameter(queryParameters, "label", label);
         addOptionalParameter(queryParameters, "note", note);
         return executeGetRequest(queryParameters);
     }
 
     public String resetPasswordNodeJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("resetPasswordNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String rebootNodeJson(String Identifier,
                                  String Recovery)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("rebootNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addOptionalParameter(queryParameters, "Recovery", Recovery);
         return executeGetRequest(queryParameters);
     }
 
     public String migrateNodeJson(String Identifier,
                                   String destination,
                                   Integer cold_migrate_on_rollback)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("migrateNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "destination", destination);
         addOptionalParameter(queryParameters, "cold_migrate_on_rollback", cold_migrate_on_rollback);
         return executeGetRequest(queryParameters);
     }
 
     public String resizeNodeJson(String Identifier,
                                  Integer allow_cold_resize,
                                  Integer cpu_shares,
                                  Integer cpus,
                                  Integer memory)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("resizeNode");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addOptionalParameter(queryParameters, "allow_cold_resize", allow_cold_resize);
         addOptionalParameter(queryParameters, "cpu_shares", cpu_shares);
         addOptionalParameter(queryParameters, "cpus", cpus);
         addOptionalParameter(queryParameters, "memory", memory);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Disks
     //----------------------------------------------------------------------------------------------------
 
     public String listDisksJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listDisks");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String getDiskJson(String disk_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getDisk");
         addMandatoryParameter(queryParameters, "disk_id", disk_id);
         return executeGetRequest(queryParameters);
     }
 
     public String attachDiskJson(String Identifier,
                                  String data_store_id,
                                  int disk_size,
                                  String is_swap,
                                  String mount_point,
                                  String add_to_linux_fstab,
                                  Integer require_format_disk)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("attachDisk");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "data_store_id", data_store_id);
         addMandatoryParameter(queryParameters, "disk_size", disk_size);
         addOptionalParameter(queryParameters, "is_swap", is_swap);
         addOptionalParameter(queryParameters, "mount_point", mount_point);
         addOptionalParameter(queryParameters, "add_to_linux_fstab", add_to_linux_fstab);
         addOptionalParameter(queryParameters, "require_format_disk", require_format_disk);
         return executeGetRequest(queryParameters);
     }
 
     public String detachDiskJson(String disk_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("detachDisk");
         addMandatoryParameter(queryParameters, "disk_id", disk_id);
         return executeGetRequest(queryParameters);
     }
 
     public String editDiskJson(String disk_id,
                                int disk_size,
                                String has_autobackups)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("editDisk");
         addMandatoryParameter(queryParameters, "disk_id", disk_id);
         addMandatoryParameter(queryParameters, "disk_size", disk_size);
         addMandatoryParameter(queryParameters, "has_autobackups", has_autobackups);
         return executeGetRequest(queryParameters);
     }
 
     public String createBackupJson(String disk_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("createBackup");
         addMandatoryParameter(queryParameters, "disk_id", disk_id);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Backup
     //----------------------------------------------------------------------------------------------------
 
     public String listBackupsJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listBackups");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String convertBackupJson(String backup_id,
                                     String label,
                                     Integer min_disk_size,
                                     Integer min_memory_size)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("convertBackup");
         addMandatoryParameter(queryParameters, "backup_id", backup_id);
         addMandatoryParameter(queryParameters, "label", label);
         addOptionalParameter(queryParameters, "min_disk_size", min_disk_size);
         addOptionalParameter(queryParameters, "min_memory_size", min_memory_size);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Network Interfaces
     //----------------------------------------------------------------------------------------------------
 
     public String listNetworkInterfacesJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listNetworkInterfaces");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String attachNetworkInterfaceJson(String Identifier,
                                              String label,
                                              int rate_limit,
                                              String network_id,
                                              int primary)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("attachNetworkInterface");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "label", label);
         addMandatoryParameter(queryParameters, "rate_limit", rate_limit);
         addMandatoryParameter(queryParameters, "network_id", network_id);
         addMandatoryParameter(queryParameters, "primary", primary);
         return executeGetRequest(queryParameters);
     }
 
     public String editNetworkInterfaceJson(String Identifier,
                                            String label,
                                            String network_interface_id,
                                            Integer rate_limit)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("editNetworkInterface");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addOptionalParameter(queryParameters, "label", label);
         addMandatoryParameter(queryParameters, "network_interface_id", network_interface_id);
         addOptionalParameter(queryParameters, "rate_limit", rate_limit);
         return executeGetRequest(queryParameters);
     }
 
     public String detachNetworkInterfaceJson(String Identifier,
                                              String network_interface_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("detachNetworkInterface");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "network_interface_id", network_interface_id);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Network
     //----------------------------------------------------------------------------------------------------
 
     public String listNetworksJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listNetworks");
         return executeGetRequest(queryParameters);
     }
 
     public String getNetworkJson(String network_id,
                                  String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getNetwork");
         addMandatoryParameter(queryParameters, "network_id", network_id);
         addOptionalParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String rebuildNetworkJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("rebuildNetwork");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // IP Address
     //----------------------------------------------------------------------------------------------------
 
     public String listIpAddressesJson(String network_id,
                                       String free,
                                       String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listIpAddresses");
         addMandatoryParameter(queryParameters, "network_id", network_id);
         addOptionalParameter(queryParameters, "free", free);
         addOptionalParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String getIpAddressJson(String network_id,
                                    String Identifier,
                                    String ip_address_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getIpAddress");
         addMandatoryParameter(queryParameters, "network_id", network_id);
         addOptionalParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "ip_address_id", ip_address_id);
         return executeGetRequest(queryParameters);
     }
 
     public String addIpJson(String Identifier,
                             String ip_address_id,
                             String network_interface_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("addIp");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addOptionalParameter(queryParameters, "ip_address_id", ip_address_id);
         addMandatoryParameter(queryParameters, "network_interface_id", network_interface_id);
         return executeGetRequest(queryParameters);
     }
 
     public String deleteIpJson(String Identifier,
                                String ip_address_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("deleteIp");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "ip_address_id", ip_address_id);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Firewall Rule
     //----------------------------------------------------------------------------------------------------
 
     public String listFirewallsJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listFirewalls");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String getFirewallJson(String Identifier,
                                   String firewall_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getFirewall");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "firewall_id", firewall_id);
         return executeGetRequest(queryParameters);
     }
 
     public String addFirewallJson(String Identifier,
                                   String address,
                                   String command,
                                   String port,
                                   String protocol,
                                   String network_interface_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("addFirewall");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "address", address);
         addMandatoryParameter(queryParameters, "command", command);
         addMandatoryParameter(queryParameters, "port", port);
         addMandatoryParameter(queryParameters, "protocol", protocol);
         addMandatoryParameter(queryParameters, "network_interface_id", network_interface_id);
         return executeGetRequest(queryParameters);
     }
 
     public String editFirewallJson(String Identifier,
                                    String address,
                                    String command,
                                    String firewall_id,
                                    String network_interface_id,
                                    String port,
                                    String protocol)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("editFirewall");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "address", address);
         addMandatoryParameter(queryParameters, "command", command);
         addMandatoryParameter(queryParameters, "firewall_id", firewall_id);
         addMandatoryParameter(queryParameters, "network_interface_id", network_interface_id);
         addOptionalParameter(queryParameters, "port", port);
         addOptionalParameter(queryParameters, "protocol", protocol);
         return executeGetRequest(queryParameters);
     }
 
     public String deleteFirewallJson(String Identifier,
                                      String firewall_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("deleteFirewall");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "firewall_id", firewall_id);
         return executeGetRequest(queryParameters);
     }
 
     public String setFirewallDefaultJson(String Identifier,
                                          String default_firewall_rule,
                                          String network_interface_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("setFirewallDefault");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "default_firewall_rule", default_firewall_rule);
         addOptionalParameter(queryParameters, "network_interface_id", network_interface_id);
         return executeGetRequest(queryParameters);
     }
 
     public String applyFirewallJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("applyFirewall");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Templates
     //----------------------------------------------------------------------------------------------------
 
     public String listImagesJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listImages");
         return executeGetRequest(queryParameters);
     }
 
     public String getImageJson(String template_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getImage");
         addMandatoryParameter(queryParameters, "template_id", template_id);
         return executeGetRequest(queryParameters);
     }
 
     public String destroyImageJson(String template_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("destroyImage");
         addMandatoryParameter(queryParameters, "template_id", template_id);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Data Store
     //----------------------------------------------------------------------------------------------------
 
     public String listDatastoresJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listDatastores");
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Load Balancers
     //----------------------------------------------------------------------------------------------------
 
     public String listClustersJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listClusters");
         return executeGetRequest(queryParameters);
     }
 
     public String getClusterJson(String cluster_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getCluster");
         addMandatoryParameter(queryParameters, "cluster_id", cluster_id);
         return executeGetRequest(queryParameters);
     }
 
     public String editClusterJson(String cluster_id,
                                   String label,
                                   Integer rate_limit,
                                   String ports)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("editCluster");
         addMandatoryParameter(queryParameters, "cluster_id", cluster_id);
         addOptionalParameter(queryParameters, "label", label);
         addOptionalParameter(queryParameters, "rate_limit", rate_limit);
         addOptionalParameter(queryParameters, "ports", ports);
         return executeGetRequest(queryParameters);
     }
 
     public String deployClusteringBalancerJson(String label,
                                                String hostname,
                                                String hypervisor_group_id,
                                                String hypervisor_id,
                                                String nodes,
                                                String primary_network_group_id,
                                                int rate_limit,
                                                String ports)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("deployClusteringBalancer");
         addMandatoryParameter(queryParameters, "label", label);
         addMandatoryParameter(queryParameters, "hostname", hostname);
         addOptionalParameter(queryParameters, "hypervisor_group_id", hypervisor_group_id);
         addOptionalParameter(queryParameters, "hypervisor_id", hypervisor_id);
         addOptionalParameter(queryParameters, "nodes", nodes);
         addOptionalParameter(queryParameters, "primary_network_group_id", primary_network_group_id);
         addMandatoryParameter(queryParameters, "rate_limit", rate_limit);
         addMandatoryParameter(queryParameters, "ports", ports);
         return executeGetRequest(queryParameters);
     }
 
     public String addClusteringNodeJson(String cluster_id,
                                         String nodes)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("addClusteringNode");
         addMandatoryParameter(queryParameters, "cluster_id", cluster_id);
         addMandatoryParameter(queryParameters, "nodes", nodes);
         return executeGetRequest(queryParameters);
     }
 
     public String removeClusteringNode(String cluster_id,
                                        String nodes)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("removeClusteringNodeJson");
         addMandatoryParameter(queryParameters, "cluster_id", cluster_id);
         addMandatoryParameter(queryParameters, "nodes", nodes);
         return executeGetRequest(queryParameters);
     }
 
     public String destroyClusterJson(String cluster_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("destroyCluster");
         addMandatoryParameter(queryParameters, "cluster_id", cluster_id);
         return executeGetRequest(queryParameters);
     }
 
     public String deployAutoScalingBalancerJson(Integer autoscaling_in_cpu,
                                                 Integer autoscaling_in_memory,
                                                 Integer autoscaling_out_cpu,
                                                 Integer autoscaling_out_memory,
                                                 String label,
                                                 String hostname,
                                                 String hypervisor_group_id,
                                                 String hypervisor_id,
                                                 int max_node_amount,
                                                 int min_node_amount,
                                                 Integer node_cpu_shares,
                                                 int node_cpus,
                                                 int node_memory,
                                                 int node_rate_limit,
                                                 String node_template_id,
                                                 String primary_network_group_id,
                                                 int rate_limit,
                                                 String ports)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("deployAutoScalingBalancer");
         addOptionalParameter(queryParameters, "autoscaling_in_cpu", autoscaling_in_cpu);
         addOptionalParameter(queryParameters, "autoscaling_in_memory", autoscaling_in_memory);
         addOptionalParameter(queryParameters, "autoscaling_out_cpu", autoscaling_out_cpu);
         addOptionalParameter(queryParameters, "autoscaling_out_memory", autoscaling_out_memory);
         addMandatoryParameter(queryParameters, "label", label);
         addMandatoryParameter(queryParameters, "hostname", hostname);
         addOptionalParameter(queryParameters, "hypervisor_group_id", hypervisor_group_id);
         addOptionalParameter(queryParameters, "hypervisor_id", hypervisor_id);
         addMandatoryParameter(queryParameters, "max_node_amount", max_node_amount);
         addMandatoryParameter(queryParameters, "min_node_amount", min_node_amount);
         addOptionalParameter(queryParameters, "node_cpu_shares", node_cpu_shares);
         addMandatoryParameter(queryParameters, "node_cpus", node_cpus);
         addMandatoryParameter(queryParameters, "node_memory", node_memory);
         addMandatoryParameter(queryParameters, "node_rate_limit", node_rate_limit);
         addMandatoryParameter(queryParameters, "node_template_id", node_template_id);
         addOptionalParameter(queryParameters, "primary_network_group_id", primary_network_group_id);
         addMandatoryParameter(queryParameters, "rate_limit", rate_limit);
         addMandatoryParameter(queryParameters, "ports", ports);
         return executeGetRequest(queryParameters);
     }
 
     public String editAutoScalingBalancerJson(String cluster_id,
                                               Integer autoscaling_in_cpu,
                                               Integer autoscaling_in_memory,
                                               Integer autoscaling_out_cpu,
                                               Integer autoscaling_out_memory,
                                               Integer max_node_amount,
                                               Integer min_node_amount)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("editAutoScalingBalancerJson");
         addMandatoryParameter(queryParameters, "cluster_id", cluster_id);
         addOptionalParameter(queryParameters, "autoscaling_in_cpu", autoscaling_in_cpu);
         addOptionalParameter(queryParameters, "autoscaling_in_memory", autoscaling_in_memory);
         addOptionalParameter(queryParameters, "autoscaling_out_cpu", autoscaling_out_cpu);
         addOptionalParameter(queryParameters, "autoscaling_out_memory", autoscaling_out_memory);
         addOptionalParameter(queryParameters, "max_node_amount", max_node_amount);
         addOptionalParameter(queryParameters, "min_node_amount", min_node_amount);
         return executeGetRequest(queryParameters);
     }
 
     public String listBalancersJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("listBalancers");
         return executeGetRequest(queryParameters);
     }
 
     public String getBalancerJson(String load_balancer_id)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getBalancer");
         addMandatoryParameter(queryParameters, "load_balancer_id", load_balancer_id);
         return executeGetRequest(queryParameters);
     }
 
     public String shutdownBalancerJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("shutdownBalancer");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String startupBalancerJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("startupBalancer");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String rebootBalancerJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("rebootBalancer");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String rebuildBalancerJson(String Identifier)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("rebuildBalancer");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         return executeGetRequest(queryParameters);
     }
 
     public String migrateBalancerJson(String Identifier,
                                       String destination,
                                       Integer cold_migrate_on_rollback)
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("migrateBalancer");
         addMandatoryParameter(queryParameters, "Identifier", Identifier);
         addMandatoryParameter(queryParameters, "destination", destination);
         addOptionalParameter(queryParameters, "cold_migrate_on_rollback", cold_migrate_on_rollback);
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Resources
     //----------------------------------------------------------------------------------------------------
 
     public String getResourcesJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getResources");
         return executeGetRequest(queryParameters);
     }
 
     public String getLimitsJson()
             throws InvalidKeyException, NoSuchAlgorithmException, IOException {
         TreeMap<String, String> queryParameters = createQueryParameters("getLimits");
         return executeGetRequest(queryParameters);
     }
 
     //----------------------------------------------------------------------------------------------------
     // Private methods
     //----------------------------------------------------------------------------------------------------
 
     private static void initStatic() {
         ISO8601.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
     }
 
     private String executeGetRequest(TreeMap<String, String> queryParameters)
             throws IOException, InvalidKeyException, NoSuchAlgorithmException {
         // Query String
         String queryString = toQueryString(queryParameters);
         String signature = toSignature("GET", queryString);
         queryString += "&Signature=" + urlEncode(signature);
 
         // HttpClient
         HttpClient httpclient = wrapClient(new DefaultHttpClient());
 
         // Execute HttpClient
         HttpGet httpGet = new HttpGet("https://" + DOMAIN + "/" + cloudZoneID + "/?" + queryString);
         // System.out.println("[doGetRequest] httpGet = " + httpGet.getURI());
         HttpResponse response = httpclient.execute(httpGet);
 
         // Check HTTP status code
         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode < 200 || statusCode >= 300) {
             throw new IOException("HTTP status code = " + statusCode);
         }
 
         // Return response
         HttpEntity entity = response.getEntity();
         return EntityUtils.toString(entity);
     }
 
     // http://tech.chitgoks.com/2011/04/24/how-to-avoid-javax-net-ssl-sslpeerunverifiedexception-peer-not-authenticated-problem-using-apache-httpclient/
     private static HttpClient wrapClient(HttpClient base) {
         try {
             // SSLSocketFactory
             SSLContext ctx = SSLContext.getInstance("TLS");
             X509TrustManager tm = new X509TrustManager() {
                 public void checkClientTrusted(X509Certificate[] xcs, String string)
                         throws CertificateException {
                 }
 
                 public void checkServerTrusted(X509Certificate[] xcs, String string)
                         throws CertificateException {
                 }
 
                 public X509Certificate[] getAcceptedIssuers() {
                     return null;
                 }
             };
             ctx.init(null, new TrustManager[]{tm}, null);
             SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
             // Register SSLSocketFactory
             ClientConnectionManager ccm = base.getConnectionManager();
             ccm.getSchemeRegistry().register(new Scheme("https", 443, ssf));
 
             return new DefaultHttpClient(ccm, base.getParams());
         } catch (Exception ex) {
             return null;
         }
     }
 
     private TreeMap<String, String> createQueryParameters(String action) {
         TreeMap<String, String> queryParameters = new TreeMap<String, String>();
         queryParameters.put("AccessKeyId", accessKeyId);
         queryParameters.put("Version", VERSION);
         queryParameters.put("Action", action);
         queryParameters.put("Timestamp", ISO8601.format(new Date()));
         return queryParameters;
     }
 
     private static String toQueryString(TreeMap<String, String> queryParameters) {
         StringBuilder sb = new StringBuilder();
         for (String key : queryParameters.navigableKeySet()) {
             sb.append(urlEncode(key)).append('=').append(urlEncode(queryParameters.get(key))).append('&');
         }
         sb.deleteCharAt(sb.length() - 1);
         return sb.toString();
     }
 
     private String toSignature(String httpMethod, String queryString)
             throws NoSuchAlgorithmException, InvalidKeyException {
         return calcHmacSHA256(secretAccessKey, httpMethod + "\n" +
                 DOMAIN + "\n" + "/" + cloudZoneID + "/\n" + queryString);
     }
 
     private static String urlEncode(String data) {
         try {
             return URLEncoder.encode(data, "UTF-8");
         } catch (UnsupportedEncodingException ignored) {
             return null;
         }
     }
 
     private static String calcHmacSHA256(String key, String data)
             throws NoSuchAlgorithmException, InvalidKeyException {
         byte[] keyBytes;
         try {
             keyBytes = key.getBytes("UTF-8");
         } catch (UnsupportedEncodingException ignored) {
             return null;
         }
 
         Mac sha256Hmac = Mac.getInstance("HmacSHA256");
         sha256Hmac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
 
         byte[] bytes = sha256Hmac.doFinal(data.getBytes());
         return StringUtils.newStringUtf8(Base64.encodeBase64(bytes, false));
     }
 
     private static void addMandatoryParameter(TreeMap<String, String> queryParameters,
                                               String key, String value) {
         checkParameter(key, value);
         queryParameters.put(key, value);
     }
 
     private static void addMandatoryParameter(TreeMap<String, String> queryParameters,
                                               String key, int value) {
         queryParameters.put(key, "" + value);
     }
 
     private static void addOptionalParameter(TreeMap<String, String> queryParameters,
                                              String key, String value) {
         if (value != null) {
             queryParameters.put(key, value);
         }
     }
 
     private static void addOptionalParameter(TreeMap<String, String> queryParameters,
                                              String key, Integer value) {
         if (value != null) {
             queryParameters.put(key, value.toString());
         }
     }
 
     private static void checkParameter(String name, String value) {
         if (isEmpty(value)) {
             throw new IllegalArgumentException(name + " = " + value);
         }
     }
 
     private static boolean isEmpty(String s) {
         return s == null || s.isEmpty();
     }
 }
