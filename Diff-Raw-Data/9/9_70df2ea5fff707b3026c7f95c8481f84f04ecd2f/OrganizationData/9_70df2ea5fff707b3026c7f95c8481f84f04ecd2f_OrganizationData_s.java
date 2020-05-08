 package net.cyklotron.cms.structure.internal;
 
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.elm;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.selectFirstText;
 import static net.cyklotron.cms.structure.internal.ProposedDocumentData.stripTags;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dom4j.Node;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.templating.TemplatingContext;
 
 import net.cyklotron.cms.documents.DocumentMetadataHelper;
 
 public class OrganizationData
 {
     public static final OrganizationData BLANK = new OrganizationData();
 
     private String name = "";
 
     private String province = "";
 
     private String postCode = "";
 
     private String city = "";
 
     private String street = "";
 
     private String phone = "";
 
     private String fax = "";
 
     private String email = "";
 
     private String www = "";
 
     private String id = "";
 
     public static OrganizationData get(List<OrganizationData> organizations, int index)
     {
         if(index < organizations.size())
         {
             return organizations.get(index);
         }
         else
         {
             return BLANK;
         }
     }
 
     public boolean isBlank()
     {
         return name.length() + province.length() + postCode.length() + city.length()
             + street.length() + phone.length() + fax.length() + email.length() + www.length() == 0;
     }
 
     public void fromParmeters(Parameters parameters, String prefix)
     {
         name = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_name", "")));
         province = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_province", "")));
         postCode = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_postCode", "")));
         city = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_city", "")));
         street = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_street", "")));
         phone = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_phone", "")));
         fax = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_fax", "")));
         email = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_email", "")));
         www = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_www", "")));
         id = stripTags(DocumentMetadataHelper.dec(parameters.get(prefix + "_id", "0")));
        if(id.trim().length() == 0)
        {
            id = "0";
        }
     }
 
     public static List<OrganizationData> fromParameters(Parameters parameters)
     {
         List<OrganizationData> organizations = new ArrayList<OrganizationData>();
         int index = 1;
         while(parameters.isDefined("organization_" + index + "_name"))
         {
             OrganizationData organization = new OrganizationData();
             organization.fromParmeters(parameters, "organization_" + index);
             if(!organization.isBlank())
             {
                 organizations.add(organization);
             }
             index++;
         }
         return organizations;
     }
 
     public void toTemplatingContext(TemplatingContext templatingContext, String prefix)
     {
         templatingContext.put(prefix + "_name", DocumentMetadataHelper.enc(name));
         templatingContext.put(prefix + "_province", DocumentMetadataHelper.enc(province));
         templatingContext.put(prefix + "_postCode", DocumentMetadataHelper.enc(postCode));
         templatingContext.put(prefix + "_city", DocumentMetadataHelper.enc(city));
         templatingContext.put(prefix + "_street", DocumentMetadataHelper.enc(street));
         templatingContext.put(prefix + "_phone", DocumentMetadataHelper.enc(phone));
         templatingContext.put(prefix + "_fax", DocumentMetadataHelper.enc(fax));
         templatingContext.put(prefix + "_email", DocumentMetadataHelper.enc(email));
         templatingContext.put(prefix + "_www", DocumentMetadataHelper.enc(www));
         templatingContext.put(prefix + "_id", DocumentMetadataHelper.enc(id));
     }
 
     public static void toTemplatingContext(List<OrganizationData> organizations,
         TemplatingContext templatingContext)
     {
         int index = 1;
         for(OrganizationData organization : organizations)
         {
             organization.toTemplatingContext(templatingContext, "organization_" + index);
             index++;
         }
         templatingContext.put("organizations_count", organizations.size());
     }
 
     public void fromMeta(Node node)
     {
         name = DocumentMetadataHelper.dec(selectFirstText(node, "name"));
         province = DocumentMetadataHelper.dec(selectFirstText(node, "address/province"));
         postCode = DocumentMetadataHelper.dec(selectFirstText(node, "address/postcode"));
         city = DocumentMetadataHelper.dec(selectFirstText(node, "address/city"));
         street = DocumentMetadataHelper.dec(selectFirstText(node, "address/street"));
         phone = DocumentMetadataHelper.dec(selectFirstText(node, "tel"));
         fax = DocumentMetadataHelper.dec(selectFirstText(node, "fax"));
         email = DocumentMetadataHelper.dec(selectFirstText(node, "e-mail"));
         www = DocumentMetadataHelper.dec(selectFirstText(node, "url"));
         id = DocumentMetadataHelper.dec(selectFirstText(node, "id"));
     }
 
     public static List<OrganizationData> fromMeta(Node metaNode, String xpath)
     {
         List<Node> nodes = (List<Node>)metaNode.selectNodes(xpath + "/organization");
         List<OrganizationData> oragnisations = new ArrayList<OrganizationData>(nodes.size());
         for(Node node : nodes)
         {
             OrganizationData organization = new OrganizationData();
             organization.fromMeta(node);
             oragnisations.add(organization);
         }
         return oragnisations;
     }
 
     public Node toMeta()
     {
         return elm("organization", elm("name", name), elm("address", elm("street", street), elm(
             "postcode", postCode), elm("city", city), elm("province", province)),
            elm("tel", phone), elm("fax", fax), elm("e-mail", email), elm("url", www),
            elm("id", id));
     }
     
     public static String getOrganizationIds(List<OrganizationData> organizations)
     {
         StringBuilder buff = new StringBuilder();
         buff.append(',');
         for(OrganizationData organization : organizations)
         {
             buff.append(organization.id).append(',');
         }
         return buff.toString();
     }
 
     public static Node toMeta(List<OrganizationData> organizations)
     {
         Node[] nodes = new Node[organizations.size()];
         for(int i = 0; i < organizations.size(); i++)
         {
             nodes[i] = organizations.get(i).toMeta();
         }
         return elm("organizations", nodes);
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getProvince()
     {
         return province;
     }
 
     public void setProvince(String province)
     {
         this.province = province;
     }
 
     public String getPostCode()
     {
         return postCode;
     }
 
     public void setPostCode(String postCode)
     {
         this.postCode = postCode;
     }
 
     public String getCity()
     {
         return city;
     }
 
     public void setCity(String city)
     {
         this.city = city;
     }
 
     public String getStreet()
     {
         return street;
     }
 
     public void setStreet(String street)
     {
         this.street = street;
     }
 
     public String getPhone()
     {
         return phone;
     }
 
     public void setPhone(String phone)
     {
         this.phone = phone;
     }
 
     public String getFax()
     {
         return fax;
     }
 
     public void setFax(String fax)
     {
         this.fax = fax;
     }
 
     public String getEmail()
     {
         return email;
     }
 
     public void setEmail(String email)
     {
         this.email = email;
     }
 
     public String getWww()
     {
         return www;
     }
 
     public void setWww(String www)
     {
         this.www = www;
     }
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String id)
     {
         this.id = id;
     }
 
     public void dump(StringBuilder buff)
     {
         buff.append("Name: ").append(name).append("\n");
         buff.append("Province: ").append(province).append("\n");
         buff.append("Code: ").append(postCode).append("\n");
         buff.append("City: ").append(city).append("\n");
         buff.append("Street: ").append(street).append("\n");
         buff.append("Phone: ").append(phone).append("\n");
         buff.append("Fax: ").append(fax).append("\n");
         buff.append("Email: ").append(email).append("\n");
         buff.append("URL: ").append(www).append("\n");
         buff.append("Id: ").append(id).append("\n");
     }
 
     public static void dump(List<OrganizationData> organizations, StringBuilder buff)
     {
         int index = 1;
         for(OrganizationData organization : organizations)
         {
             buff.append("Oranisation " + index + ":\n");
             organization.dump(buff);
         }
     }
 }
