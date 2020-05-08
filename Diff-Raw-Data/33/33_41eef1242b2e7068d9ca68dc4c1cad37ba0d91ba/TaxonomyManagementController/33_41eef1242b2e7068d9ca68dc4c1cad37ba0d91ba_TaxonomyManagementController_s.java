 package au.com.gaiaresources.bdrs.controller.taxonomy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSON;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONSerializer;
 import net.sf.json.xml.XMLSerializer;
 
 import org.apache.commons.beanutils.DynaBean;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import au.com.gaiaresources.bdrs.controller.AbstractController;
 import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
 import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormFieldFactory;
 import au.com.gaiaresources.bdrs.controller.insecure.taxa.ComparePersistentImplByWeight;
 import au.com.gaiaresources.bdrs.controller.record.WebFormAttributeParser;
 import au.com.gaiaresources.bdrs.file.FileService;
 import au.com.gaiaresources.bdrs.model.file.ManagedFile;
 import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
 import au.com.gaiaresources.bdrs.model.metadata.Metadata;
 import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
 import au.com.gaiaresources.bdrs.model.preference.Preference;
 import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
 import au.com.gaiaresources.bdrs.model.taxa.Attribute;
 import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
 import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
 import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;
 import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
 import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
 import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
 import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
 import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
 import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
 import au.com.gaiaresources.bdrs.security.Role;
 import au.com.gaiaresources.bdrs.service.property.PropertyService;
 import au.com.gaiaresources.bdrs.service.web.AtlasService;
 import au.com.gaiaresources.bdrs.util.StringUtils;
 
 /**
  * The <code>TaxonomyManagementControllers</code> handles all view requests 
  * pertaining to the creating and updating of taxonomy (indicator species) 
  * and taxonomy related objects.
  */
 @Controller
 public class TaxonomyManagementController extends AbstractController {
     
     public static final String DEFAULT_SPECIES_PROFILE = "taxonProfileTemplate.json";
 
     private static final String MV_ERROR_MAP = "errorMap";
     
     private Logger log = Logger.getLogger(getClass());
     
     @Autowired
     private TaxaDAO taxaDAO;
     @Autowired
     private AttributeDAO attributeDAO;
     @Autowired
     private SpeciesProfileDAO profileDAO;
     @Autowired
     private PreferenceDAO preferenceDAO;
     @Autowired
     private ManagedFileDAO managedFileDAO;
     @Autowired
     MetadataDAO metadataDAO;
     @Autowired
     private FileService fileService;
 
     @Autowired
     private PropertyService propertyService;
 
     private FormFieldFactory formFieldFactory = new FormFieldFactory();
     
     @Autowired
     AtlasService atlasService;
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/listing.htm", method = RequestMethod.GET)
     public ModelAndView setup(HttpServletRequest request,
             HttpServletResponse response) {
         
         ModelAndView mv = new ModelAndView("taxonomyList");
         return mv;
     }
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/edit.htm", method = RequestMethod.GET)
     public ModelAndView edit(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(required=false, value="pk", defaultValue="0") int taxonPk) throws IOException {
         
         IndicatorSpecies taxon;
         List<FormField> formFieldList;
         if(taxonPk == 0) {
             taxon = new IndicatorSpecies();
             formFieldList = new ArrayList<FormField>();
         } else {
             taxon = taxaDAO.getIndicatorSpecies(taxonPk);
            
             // Need to be careful that a taxon may have attribute values
             // that are no longer applicable for the currently assigned taxon group.
             Map<Attribute, IndicatorSpeciesAttribute> attributeValueMapping = 
                 new HashMap<Attribute, IndicatorSpeciesAttribute>();
             for(IndicatorSpeciesAttribute val : taxon.getAttributes()) {
                 attributeValueMapping.put(val.getAttribute(), val);
             }
             
             // We are only interested in the attributes from the currently
             // assigned group.
             formFieldList = new ArrayList<FormField>();
             for(Attribute attr : taxon.getTaxonGroup().getAttributes()) {
                 if(attr.isTag()) {
                     IndicatorSpeciesAttribute val = attributeValueMapping.get(attr);
                     formFieldList.add(formFieldFactory.createTaxonFormField(attr, val));
                 }
             }
         }
         
         Collections.sort(formFieldList);
         
         // Species Profile Template
         List<SpeciesProfile> speciesProfileTemplate = loadSpeciesProfileTemplate(taxon.getTaxonGroup(), taxon.getInfoItems());
         speciesProfileTemplate.addAll(taxon.getInfoItems());
         Collections.sort(speciesProfileTemplate, new ComparePersistentImplByWeight());
         
         ModelAndView mv = new ModelAndView("editTaxon");
         mv.addObject("taxon", taxon);
         mv.addObject("formFieldList", formFieldList);
         mv.addObject("taxonProfileList", speciesProfileTemplate);
         mv.addObject("newProfileIndex", Integer.valueOf(0));
         return mv;
     }
 
     public List<SpeciesProfile> loadSpeciesProfileTemplate(TaxonGroup taxonGroup, List<SpeciesProfile>existingProfileItems) throws IOException {
 
         List<SpeciesProfile> speciesProfileList = new ArrayList<SpeciesProfile>();
         
         // If there is no taxon group, then there is no profile template.
         if(taxonGroup != null) {
             InputStream profileInputStream = getSpeciesProfileTemplateConfiguration();
             
             // Cannot find the species profile template configuration file.
             if(profileInputStream != null) {
                 // Read in the configuration file.
                 BufferedReader reader = new BufferedReader(new InputStreamReader(profileInputStream));
                 StringBuilder profileTmplBuilder = new StringBuilder();
                 for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                     profileTmplBuilder.append(line);
                 }
                 reader.close();
     
                 JSON json = null;
                 try {
                     // First try loading the file as an XML file.
                     XMLSerializer xmlSerializer = new XMLSerializer();
                     json = xmlSerializer.read(profileTmplBuilder.toString());
                 } catch (JSONException xe) {
                     log.error("Unable to parse species profile template file as XML trying JSON.");
                     try {
                         // Otherwise try to load the file as a JSON file.
                         json = JSONSerializer.toJSON(profileTmplBuilder.toString());
                     } catch(JSONException je) {
                         log.error("Unable to parse species profile template file as JSON.");
                     }
                 }
                 
                 if(json != null) {
                     // Expecting an array of groups.
                     if(json.isArray()) {
                         
                         // Create a map of existing species profile items that
                         // will not be added again to the species profile template.
                         Map<String, SpeciesProfile> existingProfileItemsMap = new HashMap<String, SpeciesProfile>(existingProfileItems.size());
                         for(SpeciesProfile existingProfile : existingProfileItems) {
                             existingProfileItemsMap.put(existingProfile.getHeader(), existingProfile);
                             
                         }
                         
                         JSONArray jsonArray = (JSONArray) json;
                         List<DynaBean> groupDynaBeanList = (List<DynaBean>)JSONSerializer.toJava(jsonArray);
                         
                         SpeciesProfile profile;
                         SpeciesProfile existingProfile;
                         String type;
                         String description;
                         String header;
                         for(DynaBean groupDynaBean : groupDynaBeanList) {
                             // Iterate the groups looking for one that matches the taxonGroup parameter
                             if(taxonGroup.getName().equals(groupDynaBean.get("group_name").toString())) {
                                 
                                 List<DynaBean> profileDynaBeanList = (List<DynaBean>)groupDynaBean.get("profile_template");
                                 // Load each of the profile templates.
                                 int weight = 100;
                                 for(DynaBean profileDynaBean : profileDynaBeanList) {
                                     try {
                                         type = profileDynaBean.get("type").toString();
                                         description = profileDynaBean.get("description").toString();
                                         header = profileDynaBean.get("header").toString();
                                         
                                         existingProfile = existingProfileItemsMap.get(header);
                                         // Test if this is an existing profile.
                                         if(!(existingProfile != null && existingProfile.getType() != null && existingProfile.getType().equals(type))) {
                                             
                                             profile = new SpeciesProfile();
                                             profile.setType(type);
                                             profile.setDescription(description);
                                             profile.setHeader(header);
                                             profile.setWeight(weight);
                                             weight += 100;
                                             
                                             speciesProfileList.add(profile);
                                         }
                                     } catch(IllegalArgumentException iae) {
                                         log.error(iae);
                                     } catch(Exception e){
                                         log.error(e);
                                     }
                                 }
                             }
                         }
                         
                     } else {
                         log.error("JSON data does not start with a JSON array.");
                     }
                 }
             } else {
                 log.error("Unable to find species profile template config.");
             }
         }
         return speciesProfileList;
     }
     
     private InputStream getSpeciesProfileTemplateConfiguration() throws IOException {
         
         InputStream config = null;
         // See if there is a species profile template set in the preferences
         Preference pref = preferenceDAO.getPreferenceByKey(Preference.TAXON_PROFILE_TEMPLATE);
         if(pref != null) {
             try {
                 ManagedFile mf = managedFileDAO.getManagedFile(pref.getValue());
                 if(mf != null) {
                     config = fileService.getFile(mf, mf.getFilename()).getInputStream();
                 }
             } catch(IOException ioe) {
                 log.error("Unable to access taxon profile template specified by preferences.", ioe);
                 config = null;
             } catch(IllegalArgumentException iae) {
                 log.error("Unable to access taxon profile template specified by preferences.", iae);
                 config = null;
             }
         }
         
         // If there is no config for any reason, use the default.
         config = config == null ? getClass().getResourceAsStream(DEFAULT_SPECIES_PROFILE) : config;
         return config;
     }
 
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/edit.htm", method = RequestMethod.POST)
     public ModelAndView save(MultipartHttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(required=false, value="taxonPk", defaultValue="0") int taxonPk,
                              @RequestParam(required=true, value="scientificName") String scientificName,
                              @RequestParam(required=true, value="commonName") String commonName,
                              @RequestParam(required=true, value="taxonRank") String taxonRank,
                              @RequestParam(required=true, value="parentPk") String parentPkStr,
                              @RequestParam(required=true, value="taxonGroupPk") int taxonGroupPk,
                              @RequestParam(required=true, value="author") String author,
                              @RequestParam(required=true, value="year") String year,
                              @RequestParam(required=false, value="new_profile") int[] profileIndexArray,
                              @RequestParam(required=false, value="profile_pk") int[] profilePkArray,
                              @RequestParam(required=false, value="guid") String guid) throws ParseException, IOException {
         
         IndicatorSpecies taxon;
         if(taxonPk == 0) {
             taxon = new IndicatorSpecies();
         } else {
             taxon = taxaDAO.getIndicatorSpecies(taxonPk);
         }
         
         taxon.setScientificName(scientificName);
         taxon.setCommonName(commonName);
         taxon.setTaxonRank(TaxonRank.valueOf(taxonRank));
         taxon.setAuthor(author);
         taxon.setYear(year);
         
         // save the guid if it exists
         if (!StringUtils.nullOrEmpty(guid)) {
             Metadata md = taxon.getMetadataByKey(Metadata.TAXON_SOURCE_DATA_ID);
             if (md == null) {
                 md = new Metadata();
                 md.setKey(Metadata.TAXON_SOURCE_DATA_ID);
             } else {
                 taxon.getMetadata().remove(md);
             }
             md.setValue(guid);
             metadataDAO.save(md);
             taxon.getMetadata().add(md);
         }
         
         IndicatorSpecies parent = null;
         if(!parentPkStr.isEmpty()) {
             parent = taxaDAO.getIndicatorSpecies(Integer.parseInt(parentPkStr));
         }
         taxon.setParent(parent);
         
         taxon.setTaxonGroup(taxaDAO.getTaxonGroup(taxonGroupPk));
         
         // Species Profiles
         SpeciesProfile profile;
         List<SpeciesProfile> profileList = new ArrayList<SpeciesProfile>();
         
         // Existing Profiles
         Map<Integer, SpeciesProfile> profileMap = new HashMap<Integer, SpeciesProfile>();
         for (SpeciesProfile prof : taxon.getInfoItems()) {
             profileMap.put(prof.getId(), prof);
         }        
         
         if(profilePkArray != null) {
             for(int pk : profilePkArray) {
                 profile = profileMap.remove(pk);
                 profile.setType(request.getParameter(String.format("profile_type_%d", pk))); 
                 profile.setContent(request.getParameter(String.format("profile_content_%d", pk)));
                 profile.setDescription(request.getParameter(String.format("profile_description_%d", pk)));     
                 profile.setHeader(request.getParameter(String.format("profile_header_%d", pk)));
                 profile.setWeight(Integer.parseInt(request.getParameter(String.format("profile_weight_%d", pk))));
                 
                 profileDAO.save(profile);
                 profileList.add(profile);
             }
         }
         
         // New Profile
         if(profileIndexArray != null) {
             for(int index : profileIndexArray) {
                 profile = new SpeciesProfile();
                 profile.setType(request.getParameter(String.format("new_profile_type_%d", index))); 
                 profile.setContent(request.getParameter(String.format("new_profile_content_%d", index)));
                 profile.setDescription(request.getParameter(String.format("new_profile_description_%d", index)));     
                 profile.setHeader(request.getParameter(String.format("new_profile_header_%d", index)));
                 profile.setWeight(Integer.parseInt(request.getParameter(String.format("new_profile_weight_%d", index))));
                 
                 profileDAO.save(profile);
                 profileList.add(profile);
             }
         }
         taxon.setInfoItems(profileList);
         
         // Must save the taxon before saving the IndicatorSpeciesAttributes
         taxaDAO.save(taxon);
         
         Map<String, String[]> parameterMap = this.getModifiableParameterMap(request);
         
         // Taxon Attributes
         List<TypedAttributeValue> taxonAttrsToDelete = new ArrayList<TypedAttributeValue>();
         WebFormAttributeParser attributeParser = new WebFormAttributeParser();
         TypedAttributeValue taxonAttribute;
         Set taxonAttrs = taxon.getAttributes();
         for (Attribute attribute : taxon.getTaxonGroup().getAttributes()) {
             if (attribute.isTag()) {
                 taxonAttribute = attributeParser.parse(attribute, taxon, parameterMap, request.getFileMap());
 
                 if (attributeParser.isAddOrUpdateAttribute()) {
                     taxonAttribute = attributeDAO.save(taxonAttribute);
                     if (attributeParser.getAttrFile() != null) {
                         fileService.createFile(taxonAttribute, attributeParser.getAttrFile());
                     }
                     taxonAttrs.add(taxonAttribute);
                 } else {
                     taxonAttrs.remove(taxonAttribute);
                     taxonAttrsToDelete.add(taxonAttribute);
                 }
             }
         }
         
         taxaDAO.save(taxon);
         for(TypedAttributeValue ta : taxonAttrsToDelete) {
             // Must do a save here to sever the link in the join table.
             attributeDAO.save(ta);
             // And then delete.
             attributeDAO.delete(ta);
         }
         
         // Any profiles left in the map at this stage have been deleted.
         for (SpeciesProfile delProf : profileMap.values()) {
             profileDAO.delete(delProf);
         }
         
         getRequestContext().addMessage("taxonomy.save.success", new Object[]{ taxon.getScientificName() });
         return new ModelAndView(new RedirectView("/bdrs/admin/taxonomy/listing.htm", true));
     }
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/ajaxAddProfile.htm", method = RequestMethod.GET)
     public ModelAndView ajaxAddProfile(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(value="index", required=true) int index) {
         
         ModelAndView mv = new ModelAndView("taxonProfileRow");
         mv.addObject("index", index);
         mv.addObject("profile", new SpeciesProfile());
         return mv;
     }
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/import.htm", method = RequestMethod.GET)
     public ModelAndView ajaxImportProfile(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(required=false, value="pk", defaultValue="0") int taxonPk,
                                        @RequestParam(required=false, value="guid") String guid) throws IOException {
 
         IndicatorSpecies taxon;
         List<FormField> formFieldList;
         if(taxonPk == 0) {
             taxon = new IndicatorSpecies();
         } else {
             taxon = taxaDAO.getIndicatorSpecies(taxonPk);
         }
         
         // import the profile from the atlas service
         Map<String, String> errorMap = (Map<String, String>)getRequestContext().getSessionAttribute("errorMap");
         IndicatorSpecies importTaxon = atlasService.importSpecies(taxon, guid, false, errorMap);
         if (importTaxon == null) {
             // return the error that caused the import to fail
             String tmpl = propertyService.getMessage("bdrs.taxon.import.fail");
             getRequestContext().addMessage(String.format(tmpl, taxon.getGuid()));
         } else {
             taxon = importTaxon;
         }
         // Need to be careful that a taxon may have attribute values
         // that are no longer applicable for the currently assigned taxon group.
         Map<Attribute, IndicatorSpeciesAttribute> attributeValueMapping = 
             new HashMap<Attribute, IndicatorSpeciesAttribute>();
         for(IndicatorSpeciesAttribute val : taxon.getAttributes()) {
             attributeValueMapping.put(val.getAttribute(), val);
         }
         
         // We are only interested in the attributes from the currently
         // assigned group.
         formFieldList = new ArrayList<FormField>();
         for(Attribute attr : taxon.getTaxonGroup().getAttributes()) {
             if(attr.isTag()) {
                 IndicatorSpeciesAttribute val = attributeValueMapping.get(attr);
                 formFieldList.add(formFieldFactory.createTaxonFormField(attr, val));
             }
         }
         
         Collections.sort(formFieldList);
         
         // Species Profile Template
         List<SpeciesProfile> speciesProfileTemplate = loadSpeciesProfileTemplate(taxon.getTaxonGroup(), taxon.getInfoItems());
         speciesProfileTemplate.addAll(taxon.getInfoItems());
         Collections.sort(speciesProfileTemplate, new ComparePersistentImplByWeight());
         
         ModelAndView mv = new ModelAndView("editTaxon");
         mv.addObject("taxon", taxon);
         mv.addObject("formFieldList", formFieldList);
         mv.addObject("taxonProfileList", speciesProfileTemplate);
         mv.addObject("newProfileIndex", Integer.valueOf(0));
         return mv;
     }
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/ajaxTaxonAttributeTable.htm", method = RequestMethod.GET)
     public ModelAndView ajaxTaxonAttributeTable(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestParam(value="taxonPk", required=false, defaultValue="0") int taxonPk,
                                                @RequestParam(value="groupPk", required=true) int groupPk) {
         
         IndicatorSpecies taxon = taxonPk == 0 ? new IndicatorSpecies() : taxaDAO.getIndicatorSpecies(taxonPk);
         TaxonGroup group = taxaDAO.getTaxonGroup(groupPk);
         
         // Need to be careful that a taxon may have attribute values
         // that are no longer applicable for the currently assigned taxon group.
         Map<Attribute, IndicatorSpeciesAttribute> attributeValueMapping = 
             new HashMap<Attribute, IndicatorSpeciesAttribute>();
         for(IndicatorSpeciesAttribute val : taxon.getAttributes()) {
             attributeValueMapping.put(val.getAttribute(), val);
         }
         
         // We are only interested in the attributes from the currently
         // assigned group.
         ArrayList<FormField> formFieldList = new ArrayList<FormField>();
         for(Attribute attr : group.getAttributes()) {
             if(attr.isTag()) {
                 IndicatorSpeciesAttribute val = attributeValueMapping.get(attr);
                 formFieldList.add(formFieldFactory.createTaxonFormField(attr, val)); 
             }
         }
         
         ModelAndView mv = new ModelAndView("taxonAttributeTable");
         mv.addObject("formFieldList", formFieldList);
         return mv;
     }
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/ajaxTaxonProfileTemplate.htm", method = RequestMethod.GET)
     public ModelAndView ajaxSpeciesProfileTemplateRows(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         @RequestParam(value="taxonPk", required=false, defaultValue="0") int taxonPk,
                                                         @RequestParam(value="groupPk", required=true) int groupPk,
                                                         @RequestParam(value="index", required=true) int index) throws IOException {
         
         IndicatorSpecies taxon = taxonPk == 0 ? new IndicatorSpecies() : taxaDAO.getIndicatorSpecies(taxonPk);
         TaxonGroup group = taxaDAO.getTaxonGroup(groupPk);
         List<SpeciesProfile> speciesProfileTemplate = loadSpeciesProfileTemplate(group, taxon.getInfoItems());
         
         Collections.sort(speciesProfileTemplate, new ComparePersistentImplByWeight());
         
         ModelAndView mv = new ModelAndView("profileTableBody");
         mv.addObject("taxonProfileList", speciesProfileTemplate);
         mv.addObject("newProfileIndex", Integer.valueOf(index));
         return mv;
     }
     
     @RolesAllowed( { Role.ADMIN })
     @RequestMapping(value = "/bdrs/admin/taxonomy/importNewProfiles.htm", method = RequestMethod.GET)
     public ModelAndView edit(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(required=true, value="guids") String guids,
                              @RequestParam(required=false, value="shortProfile") String importShortProfile) throws IOException {
         Map<String, String> errorMap = (Map<String, String>)getRequestContext().getSessionAttribute(MV_ERROR_MAP);
         getRequestContext().removeSessionAttribute(MV_ERROR_MAP);
         String[] ids = guids.split(",");
         int speciesCount = 0;
         for (String id : ids) {
             if (id == null || StringUtils.nullOrEmpty(id.trim())) {
                 // protect against null or empty ids
                 continue;
             }
            if (atlasService.importSpecies(id, !StringUtils.nullOrEmpty(importShortProfile), errorMap) != null) {
                speciesCount++;
             } else {
                String tmpl = propertyService.getMessage("bdrs.taxon.import.fail");
                 getRequestContext().addMessage(String.format(tmpl, id));
             }
         }
         
         ModelAndView mv= new ModelAndView(new RedirectView("/bdrs/admin/taxonomy/listing.htm", true));
         mv.addObject(MV_ERROR_MAP, errorMap);
         String tmpl = propertyService.getMessage("bdrs.taxon.import.success");
         getRequestContext().addMessage(String.format(tmpl, speciesCount));
         return mv;
     }
 }
