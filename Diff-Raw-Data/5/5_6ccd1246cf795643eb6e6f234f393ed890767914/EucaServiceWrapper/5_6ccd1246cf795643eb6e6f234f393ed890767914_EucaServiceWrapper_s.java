 package com.eucalyptus.webui.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.shared.resource.Template;
 
 public class EucaServiceWrapper {
 
   static private EucaServiceWrapper instance = null;
   
   private AwsServiceImpl aws = null;
   private CmdServiceImpl cmd = null;
   
   private EucaServiceWrapper() {
     aws = new AwsServiceImpl();
     cmd = new CmdServiceImpl();
   }
   
   static public EucaServiceWrapper getInstance() {
     if (instance == null)
       instance = new EucaServiceWrapper();
     return instance;
   }
 
   /**
    * run a new virtual machine with eucalyptus
    * @param session
    * @param template Template.class
    * @param image DB vm_image_type euca_vit_id
    * @param keypair string
    * @param group string
    * @return euca id of vm
    */
  public String runVM(Session session, Template template, String keypair, String group) {
    return null;
   }
   
   /**
    * get all keypairs' name owned by user
    * @param session
    * @return
    */
   public List<String> getKeypairs(Session session) {
     List<SearchResultRow> data = aws.lookupKeypair(session);
     List<String> ret = new ArrayList<String>();
     for (SearchResultRow d: data) {
       ret.add(d.getField(0));
     }
     return ret;
   }
   
   /**
    * get all security groups' name can be used by user
    * @param session
    * @return
    */
   public List<String> getSecurityGroups(Session session) {
     List<SearchResultRow> data = aws.lookupSecurityGroup(session);
     List<String> ret = new ArrayList<String>();
     for (SearchResultRow d: data) {
       ret.add(d.getField(0));
     }
     return ret;
   }
   
 }
