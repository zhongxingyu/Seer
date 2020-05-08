 package org.ritsuka.youji;
 
 import org.ritsuka.natsuo.yaconfig.ConfigKey;
import org.ritsuka.natsuo.yaconfig.IConfigKey;
 import org.ritsuka.natsuo.yaconfig.IKeyVerifier;
 
 import java.net.URI;
 import java.util.List;
 
 /**
  * Date: 10/1/11
  * Time: 3:46 PM
  */
 public final class Config {
     private Config() {
     }
 
    public static final IConfigKey<List<String>> INITIAL_ACCOUNTS =
            new ConfigKey<List<String>>("youji.initial-accounts")
                .setVerifier(new IKeyVerifier.ListVerifier<String>() {});
 
    public static final IConfigKey<Integer> RECONNECT_INTERVAL =
             new ConfigKey<Integer>("youji.reconnect.interval");
 
    public static final IConfigKey<Integer> MUC_REJOIN_INTERVAL =
             new ConfigKey<Integer>("youji.muc.rejoin.interval");
 
    public static final IConfigKey<Integer> MUC_REJOIN_MAXINTERVAL =
             new ConfigKey<Integer>("youji.muc.rejoin.maxinterval");
 
    public static final IConfigKey<URI> DB_LOGS =
             new ConfigKey<URI>("youji.db.logs");
 
    public static final IConfigKey<URI> DB_CONFIGS =
             new ConfigKey<URI>("youji.db.config");
 }
