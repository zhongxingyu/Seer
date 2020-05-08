 package com.isocraft.lib;
 
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Throwables;

 /**
  * ISOCraft
  * 
  * A libary containing general references for the mod
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class Reference {
 
	static{

        Properties prop = new Properties();

        try{
            InputStream stream = Reference.class.getClassLoader().getResourceAsStream("version.properties");
            prop.load(stream);
            stream.close();
        }
        catch (Exception e){
            Throwables.propagate(e);
        }

        VERSION = prop.getProperty("version") + " (build " + prop.getProperty("build_number") + ")";
    }
	
    public static final String VERSION;
     public static final String MOD_ID = "isocraft";
     public static final String MOD_NAME = "ISOCraft";
     public static final String MOD_CHANNEL = MOD_ID;
     public static final String DEPENDENCIES = "required-after:Forge@[9.11.1.964,)";
 
     public static final String CLIENT_PROXY_CLASS = "com.isocraft.core.proxy.ClientProxy";
     public static final String COMMON_PROXY_CLASS = "com.isocraft.core.proxy.CommonProxy";
 }
