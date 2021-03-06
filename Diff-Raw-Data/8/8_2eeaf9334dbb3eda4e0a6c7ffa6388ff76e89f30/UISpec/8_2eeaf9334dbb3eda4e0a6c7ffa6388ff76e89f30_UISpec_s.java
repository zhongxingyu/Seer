 package org.collectionspace.chain.csp.webui.nuispec;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.collectionspace.chain.csp.config.ConfigException;
 
 import org.collectionspace.chain.csp.schema.Field;
 import org.collectionspace.chain.csp.schema.FieldSet;
 import org.collectionspace.chain.csp.schema.Group;
 import org.collectionspace.chain.csp.schema.Instance;
 import org.collectionspace.chain.csp.schema.Option;
 import org.collectionspace.chain.csp.schema.Record;
 import org.collectionspace.chain.csp.schema.Repeat;
 import org.collectionspace.chain.csp.schema.Spec;
 import org.collectionspace.chain.csp.schema.Structure;
 import org.collectionspace.chain.csp.webui.main.Request;
 import org.collectionspace.chain.csp.webui.main.WebMethod;
 import org.collectionspace.chain.csp.webui.main.WebUI;
 import org.collectionspace.csp.api.core.CSPRequestCache;
 import org.collectionspace.csp.api.persistence.ExistException;
 import org.collectionspace.csp.api.persistence.Storage;
 import org.collectionspace.csp.api.persistence.UnderlyingStorageException;
 import org.collectionspace.csp.api.persistence.UnimplementedException;
 import org.collectionspace.csp.api.ui.UIException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UISpec implements WebMethod {
 	private static final Logger log=LoggerFactory.getLogger(UISpec.class);
 	private Record record;
 	private Storage storage;
 	private JSONObject controlledCache;
 	String structureview;
 
 	public UISpec(Record record, String structureview) {
 		this.record=record;
 		this.structureview = structureview;
 		this.controlledCache = new JSONObject();
 	}
 
 	// XXX make common
 	static String plain(Field f) {
 		if(f.getParent().isExpander()){
 			return radio(f);
 		}
 
 		List<String> path=new ArrayList<String>();
 		String pad="fields";
 		for(String part : f.getIDPath()) {
 			path.add(pad);
 			pad="0";
 			path.add(part);
 		}
 		return "${"+StringUtils.join(path,'.')+"}";		
 	}
 	// XXX make common
 	static String radio(Field f) {
 		List<String> path=new ArrayList<String>();
 		String pad="{row}";
 		path.add(pad);
 		
 		String[] paths = f.getIDPath();
 		path.add(paths[paths.length - 1]);
 		return "${"+StringUtils.join(path,'.')+"}";		
 	}
 	// XXX make common
 	// ${items.0.name}
 	static String plainlist(Field f) {
 		List<String> path=new ArrayList<String>();
 		String name="items";
 		path.add(name);
 			String pad="0";
 			path.add(pad);
 			path.add(f.getID());
 		
 		return "${"+StringUtils.join(path,'.')+"}";		
 	}
 
 	static JSONObject linktext(Field f) throws JSONException  {
 		JSONObject number=new JSONObject();
 		number.put("linktext",f.getLinkText());
 		number.put("target",f.getLinkTextTarget());
 		return number;
 			
 	}
 	
 	private JSONObject generateOptionField(Field f) throws JSONException {
 		// Dropdown entry
 		JSONObject out=new JSONObject();
 		if("radio".equals(f.getUIType())) {
 			out.put("selection",radio(f));
 		}
 		else{
 			out.put("selection",plain(f));
 		}
 		JSONArray ids=new JSONArray();
 		JSONArray names=new JSONArray();
 		int idx=0,dfault=-1;
 		for(Option opt : f.getAllOptions()) {
 			ids.put(opt.getID());
 			names.put(opt.getName());
 			if(opt.isDefault())
 				dfault=idx;
 			idx++;
 		}
 		//currently only supports single select dropdowns and not multiselect
 		if(dfault!=-1)
 			out.put("default",dfault+"");
 		out.put("optionlist",ids);
 		out.put("optionnames",names);			
 		return out;
 	}
 	// XXX factor
 	private Object generateDataEntryField(Field f) throws JSONException {
 		if("plain".equals(f.getUIType())) {
 			// Plain entry
 			return plain(f);
 		} 
 		else if("list".equals(f.getUIType())){
 			return plainlist(f);
 		}
 		else if("linktext".equals(f.getUIType())){
 			return linktext(f);
 		}
 		else if("dropdown".equals(f.getUIType())) {
 					
 			return generateOptionField(f);
 		}
 		else if("enum".equals(f.getUIType())) {
 			//XXX cache the controlled list as they shouldn't be changing if they are hard coded into the uispec
 			//XXX they shouldn't really be in the uispec but they are here until the UI and App decide how to communicate about them
 			if(!controlledCache.has(f.getAutocompleteInstance().getID())){
 				JSONArray getallnames = controlledLists(f.getAutocompleteInstance().getID());
 				controlledCache.put(f.getAutocompleteInstance().getID(), getallnames);
 			}
 
 			JSONArray allnames = controlledCache.getJSONArray(f.getAutocompleteInstance().getID());
 			JSONArray ids=new JSONArray();
 			JSONArray names=new JSONArray();
 			int dfault = -1;
 			int spacer =0;
 			if(f.hasEnumBlank()){
 				ids.put("");
 				names.put(f.enumBlankValue());
 				spacer = 1;
 			}
 			
 			for(int i=0;i<allnames.length();i++) {
 				JSONObject namedata = allnames.getJSONObject(i);
 				String name = namedata.getString("displayName");
 				String shortId="";
 				if(namedata.has("shortIdentifier") && !namedata.getString("shortIdentifier").equals("")){
 					shortId = namedata.getString("shortIdentifier");
 				}
 				else{
 					shortId = name.replaceAll("\\W","");					
 				}
 				//currently only supports single select dropdowns and not multiselect
 				if(f.isEnumDefault(name)){
 					dfault = i + spacer;
 				}
 				ids.put(shortId.toLowerCase());
 				names.put(name);
 			}
 			// Dropdown entry pulled from service layer data
 			JSONObject out=new JSONObject();
 			out.put("selection",plain(f));
 
 			if(dfault!=-1)
 				out.put("default",dfault+"");
 			out.put("optionlist",ids);
 			out.put("optionnames",names);	
 			return out;
 		}
 		return plain(f);	
 	}
 
 	private JSONArray controlledLists(String vocabtype) throws JSONException{
 		JSONArray displayNames = new JSONArray();
 		try {
 		    // Get List
 			int resultsize =1;
 			int pagenum = 0;
 			int pagesize = 200;
 			while(resultsize >0){
 				Record vr = this.record.getSpec().getRecord("vocab");
 				JSONObject restriction=new JSONObject();
 				restriction.put("pageNum", pagenum);
 				restriction.put("pageSize", pagesize);
 				Instance n = vr.getInstance(vocabtype);
 				
 				String url = vr.getID()+"/"+n.getTitleRef();
 				JSONObject data = storage.getPathsJSON(vr.getID()+"/"+n.getTitleRef(),restriction);
 				if(data.has("listItems")){
 					String[] results = (String[]) data.get("listItems");
 					/* Get a view of each */
 					for(String result : results) {
 						//change csid into displayName
 						JSONObject namedata = getDisplayNameList(storage,vr.getID(),n.getTitleRef(),result);
 						displayNames.put(namedata);
 					}
 
 					Integer total = data.getJSONObject("pagination").getInt("totalItems");
 					pagesize = data.getJSONObject("pagination").getInt("pageSize");
 					Integer itemsInPage = data.getJSONObject("pagination").getInt("itemsInPage");
 					pagenum = data.getJSONObject("pagination").getInt("pageNum");
 					pagenum++;
 					//are there more results
 					if(total <= (pagesize * (pagenum))){
 						break;
 					}
 				}
 				else{
 					resultsize=0;
 				}
 			}
 		} catch (ExistException e) {
 			throw new JSONException("Exist exception");
 		} catch (UnimplementedException e) {
 			throw new JSONException("Unimplemented exception");
 		} catch (UnderlyingStorageException e) {
 			throw new JSONException("Underlying storage exception"+vocabtype);
 		}
 		return displayNames;
 	}
 
 	private JSONObject getDisplayNameList(Storage storage,String auth_type,String inst_type,String csid) throws ExistException, UnimplementedException, UnderlyingStorageException, JSONException {
 		//should be using cached results from the previous query.
 		JSONObject out=storage.retrieveJSON(auth_type+"/"+inst_type+"/"+csid+"/view");
 		return out;
 	}
 
 	private JSONObject generateRepeatExpanderEntry(Repeat r, String affix) throws JSONException {
 		JSONObject expander = new JSONObject();
 		expander.put("type", "fluid.renderer.repeat");
 		expander.put("controlledBy", "fields."+r.getID());
 		expander.put("pathAs", "row");
 		expander.put("repeatID", r.getSelector());
 
 		if(r.getChildren().length>0){
 			JSONObject tree = new JSONObject();
 			for(FieldSet child : r.getChildren()) {
 				generateDataEntry(tree,child, affix);
 			}
 			expander.put("tree", tree);
 		}
 		return expander;
 	}
 	private JSONObject generateSelectionExpanderEntry(Field f, String affix) throws JSONException {
 		JSONObject expander = new JSONObject();
 		expander.put("type", "fluid.renderer.selection.inputs");
 		expander.put("rowID", f.getSelector()+"-row:");
 		expander.put("labelID", f.getSelector()+"-label");
 		expander.put("inputID", f.getSelector()+"-input");
 		expander.put("selectID", f.getID());
 
 		JSONObject tree = new JSONObject();
 		tree = generateOptionField(f);
 		expander.put("tree", tree);
 		return expander;
 	}
 	private JSONObject generateNonExpanderEntry(Repeat r, String affix) throws JSONException {
 		JSONObject out = new JSONObject();
 		JSONObject expander = new JSONObject();
 		expander.put("type", "fluid.renderer.noexpand");
 
 		if(r.getChildren().length>0){
 			JSONObject tree = new JSONObject();
 			for(FieldSet child : r.getChildren()) {
 				generateDataEntry(tree,child, affix);
 			}
 			expander.put("tree", tree);
 		}
 		out.put("expander", expander);
 		return out;
 	}
 	
 	private JSONObject generateRepeatEntry(Repeat r, String affix) throws JSONException {
 
 		JSONObject out = new JSONObject();
 		if(r.isExpander()){
 			out =  generateRepeatExpanderEntry(r,affix);
 		}
 		else{
 			JSONArray decorators=new JSONArray();
 			JSONObject decorator=new JSONObject();
 			decorator.put("type","fluid");
 			int size = r.getChildren().length;
 			decorator.put("func","cspace.makeRepeatable");
 			JSONObject options=new JSONObject();
 			JSONObject protoTree=new JSONObject();
 			for(FieldSet child : r.getChildren()) {
 				generateDataEntry(protoTree,child, affix);
 			}
 
 			options.put("protoTree", protoTree);
 			options.put("elPath", "fields."+r.getID());
 			decorator.put("options",options);
 			decorators.put(decorator);
 			out.put("decorators",decorators);
 		}
 		return out;
 	}
 	
 	
 	private JSONObject generateAutocomplete(Field f) throws JSONException {
 		JSONObject out=new JSONObject();
 		JSONArray decorators=new JSONArray();
 		JSONObject decorator=new JSONObject();
 		decorator.put("type","fluid");
 		decorator.put("func","cspace.autocomplete");
 
 		if(!f.isRefactored()){
 			if(f.hasContainer()){
 				decorator.put("container",f.getSelector());
 			}
 		}
 		JSONObject options=new JSONObject();
 		String extra="";
 		if(f.getRecord().isType("authority"))
 			extra="vocabularies/";
 		options.put("queryUrl","../../chain/"+extra+f.getRecord().getWebURL()+"/autocomplete/"+f.getID());
 		options.put("vocabUrl","../../chain/"+extra+f.getRecord().getWebURL()+"/source-vocab/"+f.getID());
 		decorator.put("options",options);
 		decorators.put(decorator);
 		out.put("decorators",decorators);
 		if(f.isRefactored()){
 			out.put("valuebinding", generateDataEntryField(f));
 		}
 		return out;
 	}
 
 	private JSONObject generateChooser(Field f) throws JSONException {
 		JSONObject out=new JSONObject();
 		JSONArray decorators=new JSONArray();
 		JSONObject decorator=new JSONObject();
 		decorator.put("type","fluid");
 		decorator.put("func","cspace.numberPatternChooser");
 		if(!f.isRefactored()){
 			if(f.hasContainer()){
 				decorator.put("container",f.getContainerSelector());
 			}
 		}
 		JSONObject options=new JSONObject();
 		JSONObject selectors=new JSONObject();
 		selectors.put("numberField",f.getSelector());
 		options.put("selectors",selectors);
 		JSONObject model=new JSONObject();
 		JSONArray ids=new JSONArray();
 		JSONArray samples=new JSONArray();
 		JSONArray names=new JSONArray();
 		for(Option opt : f.getAllOptions()) {
 			ids.put(opt.getID());
 			samples.put(opt.getSample());
 			names.put(opt.getName());
 		}
 		model.put("list",ids);
 		model.put("samples",samples);
 		model.put("names",names);
 		options.put("model",model);
 		decorator.put("options",options);
 		decorators.put(decorator);
 		out.put("decorators",decorators);
 		if(f.isRefactored()){
 			out.put("valuebinding", generateDataEntryField(f));
 		}
 		return out;
 	}
 
 	private JSONObject generateDate(Field f) throws JSONException {
 		JSONObject out=new JSONObject();
 		JSONArray decorators=new JSONArray();
 		JSONObject decorator=new JSONObject();
 		decorator.put("type","fluid");
 		decorator.put("func","cspace.datePicker");
 		if(!f.isRefactored()){
 			if(f.hasContainer()){
 				decorator.put("container",f.getContainerSelector());
 			}
 		}
 		decorators.put(decorator);
 		out.put("decorators",decorators);
 		if(f.isRefactored()){
 			out.put("valuebinding", generateDataEntryField(f));
 		}
 		return out;
 	}
 
 	private JSONObject generateDataEntrySection(String affix) throws JSONException {
 		JSONObject out=new JSONObject();
 		for(FieldSet fs : record.getAllFields()) {
 			generateDataEntry(out,fs,affix);
 		}
 		return out;
 	}
 
 	
 	private JSONObject generateListSection(Structure s, String affix) throws JSONException {
 		JSONObject out=new JSONObject();
 		String id = s.getListSectionName();
 		if(s.getField(id) != null){
 			FieldSet fs = s.getField(id);
 			generateDataEntry(out,fs, affix);
 		}
 		
 		return out;
 	}
 	private void generateSubRecord(JSONObject out,Record subrecord, String affix) throws JSONException {
 		for(FieldSet fs2 : subrecord.getAllFields()) {
 			generateDataEntry(out,fs2, affix);
 		}
 		
 	}
 	
 	private void generateDataEntry(JSONObject out,FieldSet fs, String affix) throws JSONException {
 		if(fs.usesRecord()){
 			if(!fs.getSelectorAffix().equals("")){
 				if(!affix.equals("")){
 					affix = affix+"-"+fs.getSelectorAffix();
 				}
 				else{
 					affix = "-"+fs.getSelectorAffix();
 				}
 			}
 			generateSubRecord(out, fs.usesRecordId(), affix);
 		}
 		else{
 			
 			if(fs instanceof Field) {
 				// Single field
 				Field f=(Field)fs;
 				if(f.isExpander()){
 					if("radio".equals(f.getUIType())){
 						JSONObject obj = generateSelectionExpanderEntry(f,affix);
 						out.put("expander",obj);
 					}
 				}
 				//XXX when all uispecs have moved across we can delete most of this
 				else if(!f.isRefactored()){
 					// Single field
 					out.put(f.getSelector()+affix,generateDataEntryField(f));	
 					
 					if(f.hasAutocompleteInstance()) {
 						if("enum".equals(f.getUIType())){
 							out.put(f.getAutocompleteSelector()+affix,generateDataEntryField(f));
 						}
 						else{
 							out.put(f.getAutocompleteSelector()+affix,generateAutocomplete(f));
 						}
 					}
 					if("chooser".equals(f.getUIType())) {
 						out.put(f.getContainerSelector()+affix,generateChooser(f));
 					}
 					if("date".equals(f.getUIType())) {
 						out.put(f.getContainerSelector()+affix,generateDate(f));
 					}
 				}
 				else{
 					
 					if(f.hasAutocompleteInstance()) {
 						if("enum".equals(f.getUIType())){
 							out.put(f.getSelector()+affix,generateDataEntryField(f));
 						}
 						else{
 							out.put(f.getSelector()+affix,generateAutocomplete(f));
 						}
 					}
 					else if("chooser".equals(f.getUIType())) {
 						out.put(f.getSelector()+affix,generateChooser(f));
 					}
 					else if("date".equals(f.getUIType())) {
 						out.put(f.getSelector()+affix,generateDate(f));
 					}
 					else if("sidebar".equals(f.getUIType())) {
 						//out.put(f.getSelector()+affix,generateSideBar(f));
 					}
 					else{
 						out.put(f.getSelector()+affix,generateDataEntryField(f));	
 					}
 				}
 			} 
 			else if(fs instanceof Group) {
 				Group g = (Group)fs;
 				JSONObject contents=new JSONObject();
 				for(FieldSet child : g.getChildren()) {
 					generateDataEntry(contents,child, affix);
 				}
 				out.put(g.getSelector(),contents);
 			} 
 			else if(fs instanceof Repeat) {
 				// Container
 				Repeat r=(Repeat)fs;
 				if(r.getXxxUiNoRepeat()) {
 					FieldSet[] children=r.getChildren();
 					if(children.length==0)
 						return;
 					generateDataEntry(out,children[0], affix);
 				} else {
 					JSONObject row=new JSONObject();
 					JSONArray children=new JSONArray();
 					if(r.asSibling() && !r.hasServicesParent()){ // allow for row [{'','',''}]
 						JSONObject contents=new JSONObject();
 						for(FieldSet child : r.getChildren()) {
 							generateDataEntry(contents,child, affix);
 						}
 						children.put(contents);
 						row.put("children",children);
 						out.put(r.getSelector(),row);
 					}
 					else{
 						JSONObject contents=generateRepeatEntry(r, affix);
 						String selector = r.getSelector();
						if(((Repeat)fs).getChildren().length==1){
 							Field child = (Field)r.getChildren()[0];
 							selector = child.getSelector();
 						}
 						if(fs.isExpander()){
 							selector="expander";
 						}
 						
 						out.put(selector,contents);
 					}
 				}
 			}
 		}
 
 	}
 
 	private void generateTitleSectionEntry(JSONObject out,FieldSet fs, String affix) throws JSONException {
 		if(fs instanceof Field) {
 			Field f=(Field)fs;
 			if(!f.isInTitle())
 				return;
 			out.put(f.getTitleSelector()+affix,plain(f));
 		} else if(fs instanceof Repeat) {
 			for(FieldSet child : ((Repeat)fs).getChildren())
 				generateTitleSectionEntry(out,child, affix);
 		}
 	}
 
 	private JSONObject generateTitleSection(String affix) throws JSONException {
 		JSONObject out=new JSONObject();
 		for(FieldSet f : record.getAllFields()) {
 			generateTitleSectionEntry(out,f, affix);
 		}
 		return out;
 	}
 
 	// XXX refactor
 	static JSONObject generateSidebarPart(String url_frag,boolean include_type,boolean include_summary,boolean include_sourcefield) throws JSONException {
 		JSONObject out=new JSONObject();
 		JSONObject row=new JSONObject();
 		JSONArray children=new JSONArray();
 		JSONObject child=new JSONObject();
 		JSONObject number=new JSONObject();
 		number.put("linktext","${items.0.number}");
 		number.put("target",url_frag+"?csid=${items.0.csid}");
 		child.put(".csc-related-number",number);
 		if(include_type)
 			child.put(".csc-related-recordtype","${items.0.recordtype}");
 		if(include_summary)
 			child.put(".csc-related-summary","${items.0.summary}");
 		if(include_sourcefield)
 			child.put(".csc-related-field","${items.0.sourceFieldName}");
 		children.put(child);
 		row.put("children",children);
 		out.put(".csc-recordList-row:",row);
 		return out;
 	}
 
 	// XXX refactor
 	private JSONObject generateSideDataEntry(JSONObject out, FieldSet fs, String affix) throws JSONException {
 		Repeat f=(Repeat)fs;
 		JSONObject listrow=new JSONObject();
 		generateDataEntry(listrow,fs, affix);
 		out.put(f.getID(),listrow);
 		return out;
 	}
 	
 	private JSONObject generateSideDataEntry(Structure s, JSONObject out, String fieldName,String url_frag,boolean include_type,boolean include_summary,boolean include_sourcefield, String affix )throws JSONException {
 		FieldSet fs = s.getSideBarItems(fieldName);
 		if(fs == null){
 			//XXX default to show if not specified
 			out.put(fieldName,generateSidebarPart(url_frag,include_type,include_summary,include_sourcefield));
 		}
 		else if(fs instanceof Repeat){
 			if(((Repeat)fs).isVisible()){
 				if(s.getField(fs.getID()) != null){
 					generateSideDataEntry(out,s.getField(fs.getID()), affix);
 				}
 				else{
 					out.put(fieldName,generateSidebarPart(url_frag,include_type,include_summary,include_sourcefield));
 				}
 			}
 		}
 		
 		return out;
 	}
 
 	// XXX sidebar is partially fixed for now
 	//need to clean up this code - reduce duplication
 	private JSONObject generateSidebarSection(Structure s, String affix) throws JSONException {
 		JSONObject out=new JSONObject();
 		generateSideDataEntry(s, out,"termsUsed","${items.0.recordtype}.html",true,false,true, affix);
 		generateSideDataEntry(s, out,"relatedProcedures","${items.0.recordtype}.html",true,true,false, affix);
 		generateSideDataEntry(s, out,"relatedObjects","${items.0.recordtype}.html",false,true,false, affix);
 		return out;
 	}
 
 	private JSONObject uispec(Storage storage) throws UIException {
 		this.storage = storage;
 		String affix = "";
 		try {
 			JSONObject out=new JSONObject();
 			Structure s = record.getStructure(this.structureview);
 			if(s.showListSection()){
 				out.put(s.getListSectionName(),generateListSection(s,affix));
 			}
 			if(s.showEditSection()){
 				out.put(s.getEditSectionName(),generateDataEntrySection(affix));
 			}
 			if(s.showTitleBar()){
 				out.put("titleBar",generateTitleSection(affix));
 			}
 			
 			if(s.showSideBar()){
 				out.put("sidebar",generateSidebarSection(s, affix));
 			}
 			return out;
 		} catch (JSONException e) {
 			throw new UIException("Cannot generate UISpec due to JSONException",e);
 		}
 	}
 
 	public void configure() throws ConfigException {}
 
 	public void run(Object in, String[] tail) throws UIException {
 		Request q=(Request)in;
 		JSONObject out=uispec(q.getStorage());
 		q.getUIRequest().sendJSONResponse(out);
 	}
 
 	public void configure(WebUI ui,Spec spec) {}
 }
