 package supersql.codegenerator.HTML;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Hashtable;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.parser.Tag;
 
 import supersql.codegenerator.CodeGenerator;
 import supersql.codegenerator.DecorateList;
 import supersql.codegenerator.FuncArg;
 import supersql.codegenerator.Function;
 import supersql.codegenerator.Manager;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.dataconstructor.DataConstructor;
 import supersql.extendclass.ExtList;
 import supersql.parser.SSQLparser;
 
 public class HTMLFunction extends Function {
 
 	protected HTMLEnv htmlEnv;
 	protected HTMLEnv htmlEnv2;
 	private static int meter_id=0;
 	private static String updateFile;
 
     public HTMLFunction()
     {
 
     }
     //󥹥ȥ饯
     public HTMLFunction(Manager manager, HTMLEnv henv, HTMLEnv henv2) {
         super();
         this.htmlEnv = henv;
         this.htmlEnv2 = henv2;
     }
     
     
 
     @Override
 	public Element createNode(ExtList<ExtList<String>> data_info) {
     	this.setDataList(data_info);
     	String FuncName = this.getFuncName();
     	if (FuncName.equalsIgnoreCase("imagefile")) {
             return FuncImagefileForJsoup();
         } else if (FuncName.equalsIgnoreCase("invoke")) {
             return FuncInvokeForJsoup();
         } else if (FuncName.equalsIgnoreCase("foreach")) {
             return FuncForeachForJsoup(data_info);
         } else if (FuncName.equalsIgnoreCase("sinvoke") || FuncName.equalsIgnoreCase("link")) {
             return FuncSinvokeForJsoup(data_info);
         } else if (FuncName.equalsIgnoreCase("null")) {
             return FuncNullForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("submit")) {
             return FuncSubmitForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("select")) {
             return FuncSelectForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("checkbox")) {
             return FuncCheckboxForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("radio")) {
             return FuncRadioForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("inputtext")) {
             return FuncInputtextForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("textarea")) {
             return FuncTextareaForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("hidden")) {
             return FuncHiddenForJsoup();
         }
         else if (FuncName.equalsIgnoreCase("session")) {
             //Func_session(); not use
         	return new Element(Tag.valueOf(""), "");
         }
         //tk start//////////////////////////////////
         else if (FuncName.equalsIgnoreCase("embed")) {
         	Log.out("[enter embed]");
         	return FuncEmbedForJsoup(data_info);
         }
         //tk end////////////////////////////////////
         Log.out("TFEId = " + HTMLEnv.getClassID(this));
         htmlEnv.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
         return null;
 	}
     
 	//Functionwork᥽å
     public void work(ExtList data_info) {
         this.setDataList(data_info);
         //    	Log.out("FuncName= " + this.getFuncName());
         //    	Log.out("filename= " + this.getAtt("filename"));
         //    	Log.out("condition= " + this.getAtt("condition"));
 
         String FuncName = this.getFuncName();
 
         if (FuncName.equalsIgnoreCase("imagefile")) {
             Func_imagefile();
         } else if (FuncName.equalsIgnoreCase("invoke")) {
             Func_invoke();
         } else if (FuncName.equalsIgnoreCase("foreach")) {
             try {
     			Func_foreach(data_info);
     		} catch (UnsupportedEncodingException e) {
     			e.printStackTrace();
     		}
         } else if (FuncName.equalsIgnoreCase("sinvoke") || FuncName.equalsIgnoreCase("link")) {
             Func_sinvoke(data_info);
         } else if (FuncName.equalsIgnoreCase("null")) {
             Func_null();
         }
         //chie
         else if (FuncName.equalsIgnoreCase("submit")) {
             Func_submit();
         }
         else if (FuncName.equalsIgnoreCase("select")) {
             Func_select();
         }
         else if (FuncName.equalsIgnoreCase("checkbox")) {
             Func_checkbox();
         }
         else if (FuncName.equalsIgnoreCase("radio")) {
             Func_radio();
         }
         else if (FuncName.equalsIgnoreCase("inputtext")) {
             Func_inputtext();
         }
         else if (FuncName.equalsIgnoreCase("textarea")) {
             Func_textarea();
         }
         else if (FuncName.equalsIgnoreCase("hidden")) {
             Func_hidden();
         }
         else if (FuncName.equalsIgnoreCase("session")) {
             //Func_session(); not use
         }//ishizaki//
         else if (FuncName.equalsIgnoreCase("youtube")){
         	Func_youtube();
         } else if (FuncName.equalsIgnoreCase("movie")){
         	Func_moviefile();
         } else if (FuncName.equalsIgnoreCase("meter")){
         	Func_meter();
         }
        //added by goto 20130308  "url"
        else if(FuncName.equalsIgnoreCase("url") || FuncName.equalsIgnoreCase("anchor") || FuncName.equalsIgnoreCase("a")){
        	Func_url(false);
        }
        //added by goto 20130417  "mail"
        else if(FuncName.equalsIgnoreCase("mail")){
        	Func_url(true);
        }
         //ishizaki//
         //tk start//////////////////////////////////
         else if (FuncName.equalsIgnoreCase("embed")) {
         	Log.out("[enter embed]");
         	Func_embed(data_info);
         }
         //tk end////////////////////////////////////
 
         Log.out("TFEId = " + HTMLEnv.getClassID(this));
         htmlEnv.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
     }
 
     protected Element FuncEmbedForJsoup(ExtList<ExtList<String>> data_info) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     protected Element FuncHiddenForJsoup() {
 		return FuncFormCommonForJsoup("hidden");
 	}
 	protected Element FuncTextareaForJsoup() {
 		return FuncFormCommonForJsoup("textarea");
 	}
 	protected Element FuncInputtextForJsoup() {
 		return FuncFormCommonForJsoup("text");
 	}
 	protected Element FuncRadioForJsoup() {
 		if(!this.getAtt("checked").equals("")){
         	HTMLEnv.setChecked(this.getAtt("checked"));
         }
         return FuncFormCommonForJsoup("radio");
 	}
 	protected Element FuncCheckboxForJsoup() {
 		Element result = FuncFormCommonForJsoup("checkbox");
 
 		if(!this.getAtt("checked").equals("")){
         	HTMLEnv.setChecked(this.getAtt("checked"));
         }
 
         return result;
 	}
 
 	protected Element FuncSelectForJsoup() {
         if(!this.getAtt("selected").equals("")){
         	HTMLEnv.setSelected(this.getAtt("selected"));
         }
         return FuncFormCommonForJsoup("select");
 	}
 	
 	protected Element FuncSubmitForJsoup() {
 		Element result = new Element(Tag.valueOf("form"), "");
     	boolean openFormInThis = false;
 
     	//submit only ----- no "@{form}"
     	if(!HTMLEnv.getFormItemFlg() && !decos.containsKey("form")){
     		result = createFormForJsoup();
         	openFormInThis = true;
     	}else if(decos.containsKey("form")){
     		result = createFormForJsoup(decos);
         	openFormInThis = true;
     	}
 
 
 		HTMLEnv.setFormItemFlg(true,"submit");
 
     	String value = "";
     	if(!this.getAtt("default").equals(null)){
         	value = this.getAtt("default");
         }
 
     	result.appendChild(JsoupFactory.createInput("submit", "", value));
     	if(openFormInThis == true){
     		HTMLEnv.setFormItemFlg(false,null);
     		openFormInThis = false;
     	}else{
     		HTMLEnv.setFormItemFlg(true,null);
     	}
 
         return result;
 	}
 	protected Element FuncNullForJsoup() {
 		return new Element(Tag.valueOf("span"), "");
 	}
 	protected Element FuncSinvokeForJsoup(ExtList<ExtList<String>> data_info) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	protected Element FuncForeachForJsoup(ExtList<ExtList<String>> data_info) {
 		String att = new String();
     	for (int i = 0; i < this.countconnectitem(); i++) {
     		att = att + "_" + this.getAtt(Integer.toString(i));
     	}
     	try {
 			att = URLEncoder.encode(att, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
     	String filename = htmlEnv.outFile + att + ".html";
 
         htmlEnv.fileName = filename;
 		return null;
 	}
 	protected Element FuncInvokeForJsoup() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	protected Element FuncImagefileForJsoup() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	protected void Func_imagefile() {
 
         /*
          * ImageFile function : <td> <img src="${imgpath}/"+att /> </td>
          */
 
         String path = this.getAtt("path", ".");
         if (!path.startsWith("/")) {
             String basedir = GlobalEnv.getBaseDir();
             if (basedir != null && basedir != "") {
                 path = GlobalEnv.getBaseDir() + "/" + path;
             }
         }
         if(GlobalEnv.isServlet()){
         	path = GlobalEnv.getFileDirectory() + path;
         }
 
 
     	//System.out.println(GlobalEnv.isServlet());
 
 
 
         //tk to make hyper link to image//////////////////////////////////////////////////////////////////////////////////
         if (htmlEnv.linkFlag > 0 || htmlEnv.sinvokeFlag) {
 			//added by goto 20121222 start
         	//ʲϡ-fΥե꤬̾ХѥˤʤäƤν(?)
 			//[%Ϣ] hrefλХѥХѥפѹ
 			//20120622νȡ-f եѥե̾פѤƤ硢Хѥˤʤʤ
 			String fileDir = new File(htmlEnv.linkUrl).getAbsoluteFile().getParent();
 			
 			if(fileDir.length() < htmlEnv.linkUrl.length()
 			&& fileDir.equals(htmlEnv.linkUrl.substring(0,fileDir.length()))){
 				String relative_path = htmlEnv.linkUrl.substring(fileDir.length()+1);
 				htmlEnv.code.append("<A href=\"" + relative_path + "\" ");
 			}else
 				htmlEnv.code.append("<A href=\"" + htmlEnv.linkUrl + "\" ");
 			
             //html_env.code.append("<A href=\"" + html_env.linkurl + "\" ");
 			//added by goto 20121222 end
 
             if(decos.containsKey("target"))
             	htmlEnv.code.append(" target=\"" + decos.getStr("target")+"\" ");
             if(decos.containsKey("class"))
             	htmlEnv.code.append(" class=\"" + decos.getStr("class") + "\" ");
             htmlEnv.code.append(">\n");
 
             Log.out("<A href=\"" + htmlEnv.linkUrl + "\">");
         }
         //tk/////////////////////////////////////////////////////////////////////////////////
 
 
         
         if(decos.containsKey("lightbox"))
         {
     		Date d1 = new Date();
     		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
     		String today = sdf.format(d1);
 
         	htmlEnv.code.append("<a href=\"" + path+"/" + this.getAtt("default")
         			+"\" rel=\"lightbox[lb"+today+"]\">");
 
         	if(decos.getStr("lightbox").compareTo("root") == 0 || decos.getStr("lightbox").compareTo("thumb") == 0)
         	{
             	htmlEnv.code.append("<img class=\"" + HTMLEnv.getClassID(this) +" ");
 
                 if(decos.containsKey("class"))
                 	htmlEnv.code.append(decos.getStr("class"));
 
                 htmlEnv.code.append(" \" src=\"" + path + "/" + this.getAtt("default") + "\" onLoad=\"initLightbox()\"/>");
 
         	}
         	htmlEnv.code.append("</a>");
         }
         else{
         	htmlEnv.code.append("<img class=\"" + HTMLEnv.getClassID(this) +" ");
         	htmlEnv2.code.append("<VALUE type=\"img\" class=\"" + HTMLEnv.getClassID(this) +" ");
         	if(decos.containsKey("class"))
         		htmlEnv.code.append(decos.getStr("class"));
 
             //System.out.println("out:path:"+this.getAtt("default"));
         	htmlEnv.code.append(" \" src=\"" + path + "/" + this.getAtt("default") + "\"/>");
         	htmlEnv2.code.append(" \" src=\"" + path + "/" + this.getAtt("default") + "\" ");
         	if(decos.containsKey("width")){
         		htmlEnv2.code.append("width=\"" + decos.getStr("width").replace("\"", "")+"\" " );
         	}
         	if(decos.containsKey("height")){
         		htmlEnv2.code.append("height=\"" + decos.getStr("height").replace("\"", "") +"\" " );
         	}
         	htmlEnv2.code.append(" ></VALUE>");
         }
         //tk  to make hyper link to image///////////////////////////////////////////////////////////////////////////////////
         if (htmlEnv.linkFlag > 0 || htmlEnv.sinvokeFlag) {
         	htmlEnv.code.append("</a>");
         }
         //tk///////////////////////////////////////////////////////////////////////////////////
         return;
     }
     
     //added by goto 20130308 start  "anchor"  anchor(), a(), url(), mail()
     /** anchorؿ: anchor( name/button-name/button-url, url, type(bt/button/img/image) )
      *          @{ width=~, height=~, transition=~ } 
     /*    url("title", "detail/imgURL", int type), anchor(), a()    */
     /*    <type:1> a(󥯸̾, URL) <=> a(󥯸̾, URL, 1)    */
     /*    <type:2> a(URL, URL, 2)    	   	*/
     /*    <type:3> a(ܥ̾, URL, 3)        	*/
     /*    mail()Ǥ							        */
     private void Func_url(boolean mailFncFlg) {
     	String statement = "";
     	FuncArg fa1 = (FuncArg) this.getArgs().get(0), fa2, fa3;
     	String url, name, type;
     	
     	try{					//2 or 3Ĥξ
     		fa2 = (FuncArg) this.getArgs().get(1);
     		url = ((mailFncFlg)?("mailto:"):("")) + fa2.getStr();
     		name = fa1.getStr();
         	
         	try{						//3Ĥξ
         		fa3 = (FuncArg) this.getArgs().get(2);
         		type = fa3.getStr();
         		
         		//type=1 -> ʸ
         		if(type.equals("1") || type.equals("text") || type.equals("")){
         			statement = getTextAnchor(url, name);
         			//statement = "<a href=\""+url+"\""+transition()+prefetch()+target(url)+">"+name+"</a>";
         		
 //        		//type=2 -> urlХܥ
 //        		}else if(type.equals("3") || type.equals("button") || type.equals("bt")){
 //            		statement = "<a href=\""+url+"\" data-role=\"button\""+transition()+prefetch()+target(url)+">"+name+"</a>";
 
             	//urlܥ(ǥȥåסХ붦)
 //        		}else if(type.equals("dbutton") || type.equals("dbt")){
         		}else if(type.equals("3") || type.equals("button") || type.equals("bt")){
             		statement = "<input type=\"button\" value=\""+name+"\" onClick=\"location.href='"+url+"'\"";
             		
             		//urlܥ width,heightν
             		if(decos.containsKey("width") || decos.containsKey("height")){
             			statement += " style=\"";
             			if(decos.containsKey("width"))	statement += "WIDTH:"+decos.getStr("width").replace("\"", "")+"; ";
             			if(decos.containsKey("height"))	statement += "HEIGHT:"+decos.getStr("height").replace("\"", "")+"; ";	//100; ";
             			statement += "\"";
                 	}
             		statement += ">";
             	
             	//type=3 -> url
             	}else if(type.equals("2") || type.equals("image") || type.equals("img")){
             		statement = "<a href=\""+url+"\""+transition()+prefetch()+target(url)+"><img src=\""+name+"\"";
     		        
         			//url width,heightν
             		if(decos.containsKey("width"))	statement += " width="+decos.getStr("width").replace("\"", "");
             		else{
             	        //added by goto 20130312  "Default width: 100%"
             			statement += " width=\"100%\"";
             		}
         			if(decos.containsKey("height"))	statement += " height="+decos.getStr("height").replace("\"", "");	//100; ";
         			statement += "></a>";
             	}
         		
         	}catch(Exception e){		//2Ĥξ
     			statement = getTextAnchor(url, name);
         		//statement = "<a href=\""+url+"\""+transition()+prefetch()+target(url)+">"+name+"</a>";
         	}
         	
     	}catch(Exception e){	//1Ĥξ
     		url = fa1.getStr();
     		statement = "<a href=\""+((mailFncFlg)?("mailto:"):("")) + url+"\""+transition()+prefetch()+target(url)+">"+url+"</a>";
     	}
     	
     	// ư˽̤HTML˽񤭤
     	htmlEnv.code.append(statement);
     	return;
     }
     private String getTextAnchor(String url, String name) {
     	//[ ]ǰϤ줿ʬϥѡ󥯤ˤ
     	//ex) a("[This] is anchor.","URL")
     	String A="",notA1="",notA2="";
     	int a1 = 0, a2 = name.length()-1;
     	try{
     		for(int i=0;i<name.length();i++){
     			if(i>0 && name.charAt(i)=='[' && name.charAt(i-1)!='\\')		a1=i;
     			else if(i>0 && name.charAt(i)==']' && name.charAt(i-1)!='\\')	a2=i;
     		}
     		if(a1==0 && a2==name.length()-1)	A=name.substring(a1,a2+1);
     		else								A=name.substring(a1+1,a2);
     		A=A.replaceAll("\\\\\\[", "[").replaceAll("\\\\\\]", "]");
     		notA1=name.substring(0,a1).replaceAll("\\\\\\[", "[").replaceAll("\\\\\\]", "]");
     		notA2=name.substring(a2+1).replaceAll("\\\\\\[", "[").replaceAll("\\\\\\]", "]");
     	}catch(Exception e){}
     	
     	return notA1+"<a href=\""+url+"\""+transition()+prefetch()+target(url)+">"+A+"</a>"+notA2;
     }
     protected String transition() {
     	//ܥ˥᡼(data-transition)ν
     	//ڡؤܤˤбƤʤ
     	if (decos.containsKey("transition"))
     		return " data-transition=\"" + decos.getStr("transition") + "\"";
     	if (decos.containsKey("trans"))
     		return " data-transition=\"" + decos.getStr("trans") + "\"";
 		return "";
     }
     protected String prefetch() {
     	//ڡץեå(data-prefetch)ν
     	//ڡؤܤ˻ѤƤϤʤޤ꤬
     	if (decos.containsKey("prefetch") || decos.containsKey("pref"))
     		return " data-prefetch";
 		return "";
     }
     protected String target(String url) {
     	//ɥɽ(target="_blank")ν=> _blankW3CǶػߤƤ뤿ᡢJS + rel=external
     	//ֳڡ־( http(s)://ǻϤޤ)פΤ߿ɥɽ
     	if (url.matches("\\s*(http|https)://.*"))
     		return "  rel=\"external\"";
     		//return " target=\"_blank\"";
 		return " target=\"_self\"";
     }
     //added by goto 20130308 end
 
     
     
 
 //    // for practice 2012/02/09
 //    private void Func_button() {
 //    	String statement ="";
 //    	String button_media = this.getArgs().get(0).toString();
 //    	if (button_media.equals("\"goback\"")){
 //    		// ܥ
 //			statement = "<form><INPUT type=\"button\" onClick='history.back();' value=\"\"></form>";
 //    	}else if(button_media.equals("\"bookmark\"")){
 //    		// ˥֥åޡ򵭽Ҥ
 //    	}else if(button_media.equals("\"facebook\"")){
 //    		// facebookΤ͡ܥν򵭽Ҥ
 //    	}else{
 //    		// ä˻꤬ʤܥˤ
 //    		statement = "<form><INPUT type=\"button\" onClick='history.back();' value=\"\"></form>";
 //    	}
 //    	// ư˽̤HTML˽񤭤
 //    	html_env.code.append(statement);
 //    	return;
 //    }
 
     protected void Func_null() {
         return;
     }
 
     //added by chie 2009 func form submit
     protected void Func_submit() {
     	String form = new String();
     	boolean openFormInThis = false;
 
     	//submit only ----- no "@{form}"
     	if(!HTMLEnv.getFormItemFlg() && !decos.containsKey("form")){
     		form = createForm(decos);
         	openFormInThis = true;
     	}else if(decos.containsKey("form")){
     		form = createForm(decos);
         	openFormInThis = true;
     	}
 
 
 		HTMLEnv.setFormItemFlg(true,"submit");
 
     	String option = new String();
     	if(!this.getAtt("default").equals(null)){
         	option += "value=\"" + this.getAtt("default") + "\"";
         }
 
         form += "<input type=\"submit\" " + option + " />";
 
     	if(openFormInThis == true){
     		form += "</form>";
     		HTMLEnv.setFormItemFlg(false,null);
     		openFormInThis = false;
     	}else{
     		HTMLEnv.setFormItemFlg(true,null);
     	}
 
         htmlEnv.code.append(form);
         htmlEnv2.code.append("<VALUE type=\"form\">"+form+"</VALUE>");
         return;
     }
 
   //added by chie 2009 func form select
     protected void Func_select() {
         if(!this.getAtt("selected").equals("")){
         	HTMLEnv.setSelected(this.getAtt("selected"));
         }
 
 		Func_FormCommon("select");
 
         return;
     }
   //added by chie 2009 func form checkbox
     protected void Func_checkbox() {
 		Func_FormCommon("checkbox");
 
 		if(!this.getAtt("checked").equals("")){
         	HTMLEnv.setChecked(this.getAtt("checked"));
         }
 
         return;
     }
     //added by chie 2009 func form radio
     protected void Func_radio() {
 
 		if(!this.getAtt("checked").equals("")){
         	HTMLEnv.setChecked(this.getAtt("checked"));
         }
 
 		Func_FormCommon("radio");
 
         return;
     }
     //added by chie 2009 func form inputtext
     protected void Func_inputtext() {
 		Func_FormCommon("text");
         return;
     }
     //added by chie 2009 func form textarea
     protected void Func_textarea() {
 		Func_FormCommon("textarea");
         return;
     }
 
   //added by chie 2009 func form hidden
     protected void Func_hidden() {
 		Func_FormCommon("hidden");
         return;
     }
     
     protected Element FuncFormCommonForJsoup(String s){
     	Element result = new Element(Tag.valueOf("div"),  "");
     	
     	String form = new String();
 
     	boolean openFormInThis = false;
 
     	if(!HTMLEnv.getFormItemFlg()){
     		form = createForm(decos);
         	openFormInThis = true;
     	}
 
 		HTMLEnv.setFormItemFlg(true,s);
 
         String att = new String();
         Integer attNo = 1;
         while (!this.getAtt("att"+attNo).equals("")){
         	if(attNo > 1)
         		att += ",";
         	att += this.getAtt("att"+attNo);
         	Log.out("att:" + att + " attNo:" + attNo);
         	attNo ++;
         }
         if(attNo == 1 && !this.getAtt("att").equals("")){
         	att += this.getAtt("att");
         	Log.out("att:" + att + " attNo:" + attNo);
         }
 
         if(!this.getAtt("name").equals("")){
         	HTMLEnv.setFormPartsName(this.getAtt("name"));
         	HTMLEnv.exFormName();
         }else{
         	HTMLEnv.setFormPartsName(null);
         }
 
         if(!this.getAtt("id").equals("")){
         	HTMLEnv.nameId = this.getAtt("id");
         }
 
         if(!this.getAtt("cond_name").equals("")){
         	HTMLEnv.condName = this.getAtt("cond_name");
         }
         if(!this.getAtt("cond").equals("")){
         	HTMLEnv.cond = this.getAtt("cond");
         }
 
 
         htmlEnv.code.append(form);
 
         if(this.getArgs().get(0) instanceof FuncArg)
         {
         	//HTMLEnv.setSelectFlg(true,(String)this.decos.get("select"));
         	HTMLEnv.setFormValueString(att);
         	Log.out("ARGS are function");
         	FuncArg fa = (FuncArg) this.getArgs().get(0);
         	fa.workAtt();
         }
         else{
             this.workAtt("default");
         }
 
     	if(openFormInThis == true){
     		htmlEnv.code.append("</form>");
     		HTMLEnv.setFormItemFlg(false,null);
         	openFormInThis = false;
     	}else{
     		HTMLEnv.setFormItemFlg(true,null);
     	}
         return result;
     }
 
     protected void Func_FormCommon(String s){
     	String form = new String();
 
     	boolean openFormInThis = false;
 
     	if(!HTMLEnv.getFormItemFlg()){
     		form = createForm(decos);
         	openFormInThis = true;
     	}
 
 		HTMLEnv.setFormItemFlg(true,s);
 
         String att = new String();
         Integer attNo = 1;
         while (!this.getAtt("att"+attNo).equals("")){
         	if(attNo > 1)
         		att += ",";
         	att += this.getAtt("att"+attNo);
         	Log.out("att:" + att + " attNo:" + attNo);
         	attNo ++;
         }
         if(attNo == 1 && !this.getAtt("att").equals("")){
         	att += this.getAtt("att");
         	Log.out("att:" + att + " attNo:" + attNo);
         }
 
         if(!this.getAtt("name").equals("")){
         	HTMLEnv.setFormPartsName(this.getAtt("name"));
         	HTMLEnv.exFormName();
         }else{
         	HTMLEnv.setFormPartsName(null);
         }
 
         if(!this.getAtt("id").equals("")){
         	HTMLEnv.nameId = this.getAtt("id");
         }
 
         if(!this.getAtt("cond_name").equals("")){
         	HTMLEnv.condName = this.getAtt("cond_name");
         }
         if(!this.getAtt("cond").equals("")){
         	HTMLEnv.cond = this.getAtt("cond");
         }
 
 
         htmlEnv.code.append(form);
 
         if(this.getArgs().get(0) instanceof FuncArg)
         {
         	//HTMLEnv.setSelectFlg(true,(String)this.decos.get("select"));
         	HTMLEnv.setFormValueString(att);
         	Log.out("ARGS are function");
         	FuncArg fa = (FuncArg) this.getArgs().get(0);
         	fa.workAtt();
         }
         else{
             this.workAtt("default");
         }
 
     	if(openFormInThis == true){
     		htmlEnv.code.append("</form>");
     		HTMLEnv.setFormItemFlg(false,null);
         	openFormInThis = false;
     	}else{
     		HTMLEnv.setFormItemFlg(true,null);
     	}
         return;
     }
 
     
     protected Element createFormForJsoup(){
     	Element result = new Element(Tag.valueOf("form"), "");
     	String path = new String();
     	if(this.getAtt("path") != null &&  !this.getAtt("path").isEmpty()){
     		 path =  this.getAtt("path").replaceAll("\"", "");
     	}else{
     		path = ".";
     	}
     	
     	result.attr("method", "POST").attr("action", path+"/servlet/supersql.form.FormServlet");
     	result.appendChild(JsoupFactory.createInput("hidden", "configfile", path+"/config.ssql"));
 
         if(this.getAtt("link") != null && !this.getAtt("link").isEmpty()){
         	result.appendChild(JsoupFactory.createInput("hidden", "sqlfile",path + "/" + this.getAtt("link").replaceAll("\"", "")));
         }else if(this.getAtt("linkfile") != null && !this.getAtt("linkfile").isEmpty()){
         	result.appendChild(JsoupFactory.createInput("hidden", "sqlfile", path + "/" + this.getAtt("linkfile").replaceAll("\"", "")));
         }
 
         if(this.getAtt("cond")!= null && !this.getAtt("cond").isEmpty()){
         	if(!this.getAtt("cond").replaceAll("\"", "").isEmpty())
         		result.appendChild(JsoupFactory.createInput("hidden", "cond1", this.getAtt("cond").replaceAll("\"", "")));
         }
 
         String att = new String();
         Integer attNo = 1;
         while (!this.getAtt("att"+attNo).equals("")){
         	if(attNo > 1)
         		att += ",";
         	att += this.getAtt("att"+attNo);
         	attNo ++;
         	Log.out("att:" + att + " attNo:" + attNo);
         }
 
         if(attNo == 1 && !this.getAtt("att").equals("")){
         	att += this.getAtt("att");
         	Log.out("att:" + att + " attNo:" + attNo);
         }
 
         if(this.getAtt("update")!=null && !this.getAtt("update").isEmpty()){
         	result.appendChild(JsoupFactory.createInput("hidden", "updatefile", path + "/" +this.getAtt("update").replaceAll("\"", "")+"(" + att + ")"));
         }else if(this.getAtt("updatefile")!=null && !this.getAtt("updatefile").isEmpty()){
         	result.appendChild(JsoupFactory.createInput("hidden", "updatefile", path + "/" +this.getAtt("updatefile").replaceAll("\"", "")+"(" + att + ")"));
         }
         return result;
     }
 
     
     public static String createForm(DecorateList decos) {
     	new String();
     	String path = new String();
     	String form = new String();
     	//System.out.println(this.getAtt("label"));
     	if(decos.containsKey("path")){
     		path =  decos.getStr("path").replaceAll("\"", "");
     	}else{
     		path = ".";
     	}
 
     	form = "<form method=\"POST\" action=\"" + path + "/supersql.form.FormServlet\" " + "name=\""+ HTMLEnv.getFormName() + "\" " +">";
 
 
     	form += "<input type=\"hidden\" name=\"configfile\" value=\"" +
 		GlobalEnv.getFileDirectory() + "/config.ssql\" />";
 
         if(decos.containsKey("link")){
         	opt(decos.getStr("link"));
         	form += "<input type=\"hidden\" name=\"sqlfile\" value=\"" + path + "/" + decos.getStr("link").replaceAll("\"", "") + "\" />";
         }
 
         if(decos.containsKey("cond")){
         	form += "<input type=\"hidden\" name=\"cond1\" value=\""+decos.getStr("cond").replaceAll("\"", "")+"\" />";
         }
 
 
         if(decos.containsKey("updatefile")){
         	String tmp = opt(decos.getStr("updatefile"));
         	updateFile = "<input type=\"hidden\" name=\"updatefile\" value=\"" + path + "/" +tmp+"\" />";
         	form += updateFile;
         }
         if(decos.containsKey("linkfile")){
         	opt(decos.getStr("linkfile"));
         	form += "<input type=\"hidden\" name=\"linkfile\" value=\"" + path + "/" +decos.getStr("linkfile").replaceAll("\"", "")+"\" />";
         }
         if(decos.containsKey("cond")){
         	form += "<input type=\"hidden\" name=\"linkcond\" value=\"" + decos.getStr("cond").replaceAll("\"", "")+"\" />";
         }
         Log.out(form);
         HTMLEnv.setFormDetail(form);
         return form;
     }
     
     protected static Element createFormForJsoup(DecorateList decos){
     	Element result = new Element(Tag.valueOf("form"), "");
     	String path = new String();
     	if(decos.containsKey("path")){
     		path =  decos.getStr("path").replaceAll("\"", "");
     	}else{
     		path = ".";
     	}
     	
     	result.attr("method", "POST");
     	result.attr("action", path + "/supersql.form.FormServlet");
     	result.attr("name", HTMLEnv.getFormName());
     	
     	result.appendChild(JsoupFactory.createInput("hidden","configFile", GlobalEnv.getFileDirectory() + "/config.ssql"));
     	
         if(decos.containsKey("link")){
         	opt(decos.getStr("link"));
         	result.appendChild(JsoupFactory.createInput("hidden","sqlfile", path + "/" + decos.getStr("link").replaceAll("\"", "")));
         }
 
         if(decos.containsKey("cond")){
         	result.appendChild(JsoupFactory.createInput("hidden","cond1", path + "/" + decos.getStr("link").replaceAll("\"", "")));
         }
 
         
         if(decos.containsKey("updatefile")){
         	result.appendChild(JsoupFactory.createInput("hidden","updateFile", path + "/" + opt(decos.getStr("updatefile"))));
         }
         if(decos.containsKey("linkfile")){
         	opt(decos.getStr("linkfile"));
         	result.appendChild(JsoupFactory.createInput("hidden","linkfile", path + "/" + decos.getStr("linkfile").replaceAll("\"", "")));
         }
         if(decos.containsKey("cond")){
         	result.appendChild(JsoupFactory.createInput("hidden","linkcond", decos.getStr("cond").replaceAll("\"", "")));
         }
         return result;
     }
 
     protected void Func_invoke() {
 
         /*
          * Invoke function : <td> <a
          * href="${server_path}/supersql.invoke.InvokeServlet?
          * ${dbname}+${query_filename}+${added_condition}"> TFE </a> </td>
          */
 
     	String path = this.getAtt("path", ".");
     	if(!GlobalEnv.getFileDirectory().equals(".")){
     		path = GlobalEnv.getFileDirectory();
     	}
         String filename = this.getAtt("filename");
         if (!filename.startsWith("/") && (path != null)) {
             filename = path + "/" + filename;
         }
 
 
 
         Log.out("invoke filename:"+filename);
 
 
         //start tk/////////////////////////////////
         /*
         html_env.linkurl = this.getAtt("server_path", GlobalEnv
                 .getInvokeServletPath())
                 + "?"
                 + this.getAtt("dbname", GlobalEnv.getdbname())
                 + "+"
                 + filename + "+" + this.getAtt("condition");
         */
         /*
         html_env.linkurl = "http://localhost:8080/invoke/servlet/supersql.invoke.InvokeServlet2"
                 + "?"
                 + "config=http://localhost:8080/invoke/config.ssql"
                 + "&"
                 + "query=" + filename
                 + "&"
                 + "cond=" + this.getAtt("condition");
 		*/
         //change chie
         htmlEnv.linkUrl = this.getAtt("server_path", GlobalEnv
                 .getInvokeServletPath())
                 + "?"
                 + "config="+path+"/config.ssql"
                 + "&"
                 + "query=" + filename
                 + "&"
                 + "cond=" + this.getAtt("condition");
         // end tk//////////////////////////////////////////////////
 
         htmlEnv.linkFlag=1;
         this.workAtt("default");
         htmlEnv.linkFlag=0;
 
         return;
     }
 
     protected void Func_foreach(ExtList data_info) throws UnsupportedEncodingException {
     	String att = new String();
     	for (int i = 0; i < this.countconnectitem(); i++) {
     		att = att + "_" + this.getAtt(Integer.toString(i));
     	}
         //String filename = html_env.outfile + "_" + this.getAtt("default") + ".html";
     	att = URLEncoder.encode(att, "UTF-8");
     	String filename = htmlEnv.outFile + att + ".html";
 
         htmlEnv.fileName = filename;
         //System.out.println(filename);
         return;
     }
 
     //tk start//////////////////////////////////////////////////////////////////////////////
     protected void Func_embed(ExtList data_info){
     	String file = this.getAtt("file");
     	String where = this.getAtt("where");
     	String att = this.getAtt("att");
     	this.getAtt("border");
     	String att2 = this.getAtt("attString");
     	String condition = new String();
     	this.getAtt("defcond");
 
 
     	Log.out("function embed");
 		Log.out("isNewEmbed:"+GlobalEnv.isNewEmbed());
 
 		boolean is_hidden = false;
 
 		if(decos.containsKey("status"))
         	if(decos.getStr("status").compareTo("hidden") == 0)
         		is_hidden = true;
 
 		//for tab
 		if(decos.containsKey("tab"))
 		{
 			htmlEnv.code.append("<div id=\"myTab\" ");
 
 			if(decos.containsKey("class"))
 				htmlEnv.code.append("class=\""+decos.getStr("class")+"\"");
 
 			htmlEnv.code.append(">\n");
 			htmlEnv.code.append("<div id=\"mTab\" class=\"yui-navset\">\n");
 
 			htmlEnv.code.append("</div></div>\n");
 
 			htmlEnv.script.append("var mTab = new YAHOO.widget.TabView(\"mTab\");");
     		htmlEnv.script.append("new YAHOO.util.DDTarget(\"myTab\", \"myTab\");");
 
 			return;
 		}
 
         if(!is_hidden)
         {
         	htmlEnv.code.append("<table class=\"att " + htmlEnv.getOutlineModeAtt() + " ");
 
         	if(decos.containsKey("class"))
         		htmlEnv.code.append(decos.getStr("class"));
         	else
         		htmlEnv.code.append(HTMLEnv.getClassID(this));
 
         	htmlEnv.code.append("\"");
         	htmlEnv.code.append("><tr><td>");
         }
 
         // for ajax div id //////////////////////////////////////////////////////
 
         String divname = new String();
         boolean has_divid = false;
 
         if(decos.containsKey("divid"))
 		{
 			has_divid = true;
 			Log.out("embed contains decos with divid");
 			String tmpdivid = decos.getStr("divid");
 			String tmp;
 			String ans;
 
 			if(tmpdivid.contains("+"))
 			{
 				ans = tmpdivid.substring(0,tmpdivid.indexOf("+"));
 				tmp = tmpdivid.substring(tmpdivid.indexOf("+")+1,tmpdivid.length());
 
 				if(tmp.compareTo("att") == 0)
 				{
 					tmp = att;
 				}
 				divname = ans + "_" + tmp;
 				Log.out("ans :"+ans+" tmp:"+tmp+" divname:"+divname);
 			}
 			else{
 				divname = decos.getStr("divid");
 			}
 		}/*else
 		{
 			//online file
 			if(file.contains("/"))
 			{
 				divname = file.substring(file.lastIndexOf("/")+1,file.indexOf(".sql"));
 			}
 			//ofline file
 			else if(file.contains("\\"))
 			{
 				Log.out(" // index"+file.indexOf(".sql"));
 				divname = file.substring(file.lastIndexOf("\\")+1,file.indexOf(".sql"));
 			}
 			//only file name
 			else
 			{
 				divname = file.substring(0,file.indexOf(".sql"));
 			}
 
 		}
         */
         if(GlobalEnv.isAjax() && decos.containsKey("droppable"))
         {
         	htmlEnv.script.append("new YAHOO.util.DDTarget(\""+divname+"\", \""+divname+"\");");
         }
         //ajax & decos contains status=hidden
         if(is_hidden && GlobalEnv.isAjax()){
 
 			htmlEnv.code.append("<div id=\""+divname+"\" ");
 
 			if(decos.containsKey("class"))
 				htmlEnv.code.append("class=\""+decos.getStr("class")+ "\" ");
 
 			htmlEnv.code.append("></div>");
 			Log.out("<div id="+divname+"></div>");
 
 			return;
         }
     	if(att.compareTo("") != 0 ){
     		condition = condition + where+att;
     	}
     	else if(att2.compareTo("") != 0){
     		condition = condition + where+"'"+att2+"'";
     	}
     	//store original config
     	Hashtable tmphash = GlobalEnv.getEnv();
 
     	//set new config for embed
 //    	String[] args = {"-f",file,"-cond",condition,"-debug"};
 //    	Log.out("cond:"+condition);
     	String[] args;
     	if(GlobalEnv.isAjax())
     	{
     		if(condition.equals(""))
     		{
     	   		args = new String[3];
         		args[0] = "-f";
         		args[1] = file;
         		args[2] = "-ajax";
 //        		args[3] = "-debug";
 
     		}
     		else
     		{
     	   		args = new String[5];
         		args[0] = "-f";
         		args[1] = file;
         		args[2] = "-cond";
        			args[3] = condition;
         		args[4] = "-ajax";
 //        		args[5] = "-debug";
     		}
     	}
     	else
     	{
     		if(GlobalEnv.isOpt()){
     			args = new String[5];
 	    		args[0] = "-f";
 	    		args[1] = file;
 	    		args[2] = "-cond";
 	    		args[3] = condition;
 	    		args[4] = "-optimizer";
 	//    		args[5] = "-debug";
     		}else{
 	    		args = new String[4];
 	    		args[0] = "-f";
 	    		args[1] = file;
 	    		args[2] = "-cond";
 	    		args[3] = condition;
 	//    		args[4] = "-debug";
     		}
     	}
 
     	htmlEnv.embedCount++;
 
     	if(file.contains(".sql"))
     	{
 
     		String makedfilename = file.substring(file.lastIndexOf("\\")+1, file.indexOf("."));
 
     		if(att.compareTo("") != 0)
     			makedfilename = makedfilename.concat("_"+att);
     		if(att2.compareTo("") != 0)
     			makedfilename = makedfilename.concat("_"+att2);
 
     		makedfilename= makedfilename.concat(".html");
 
     		Log.out("embed tmpfilename:"+makedfilename+" option:"+GlobalEnv.getEmbedOption());
 
     		File makedfile = new File(GlobalEnv.getEmbedTmp(), makedfilename);
 
     		if(makedfile.exists() && GlobalEnv.isNewEmbed() == 1)
     		{
     			Log.out("[Enter new Embed]");
     			Log.out("embed read tmp file");
     			BufferedReader dis;
     			String line = new String();
     			try{
     				dis = new BufferedReader(new FileReader(makedfile));
 
                		try{
                			while(!line.equalsIgnoreCase(" "))
                    	{
                    		Log.out("line : "+line);
                    		line = dis.readLine();
                    		if(line != null)
                    			htmlEnv.code.append(line);
                    	}
                		}catch(NullPointerException e)
                		{
                			Log.out("no more lines");
                		}
 
                     dis.close();
     			}
     			catch (IOException ioe) {
                      System.out.println("IOException: " + ioe);
                 }
     		}
     		else
     		{
     			Log.out("embed make file");
 
     			GlobalEnv.setGlobalEnvEmbed(args);
 
 
     			SSQLparser parser;
     			if(file.contains("http"))
     			{
     				parser = new SSQLparser("online");
     			}
     			else
     			{
 	    			parser = new SSQLparser(10000*(htmlEnv.embedCount+1));
 	    		}
 
 	    		CodeGenerator codegenerator = parser.getcodegenerator();
 				DataConstructor dc = new DataConstructor(parser);
 
 				StringBuffer returnedcode = codegenerator.generateCode2(parser,dc.getData());
 
 				//ajax add div tag////////////////////////////////////////////////////////////////////
 				if(GlobalEnv.isAjax())
 				{
 					if(!has_divid)
 					{
 						//online file
 						if(file.contains("/"))
 						{
 							divname = file.substring(file.lastIndexOf("/")+1,file.indexOf(".sql"));
 						}
 						//ofline file
 						else if(file.contains("\\"))
 						{
 							divname = file.substring(file.lastIndexOf("\\")+1,file.indexOf(".sql"));
 						}
 						//only file name
 						else
 						{
 							divname = file.substring(0,file.indexOf(".sql"));
 						}
 					}
 
 					htmlEnv.code.append("<div id=\""+divname+"\" ");
 
 					if(decos.containsKey("class"))
 						htmlEnv.code.append("class=\""+decos.getStr("class")+ "\" ");
 
 					htmlEnv.code.append(">");
 //	    			html_env.code.append("<br><a href=\"close.html\" class=\"bottom_close_"+divname+"\" onClick=\"return closeDiv('"+divname+"')\">close</a><br>");
 					Log.out("<div id="+divname+">");
 				}
 
 				//xml
 				if(!is_hidden){
 					htmlEnv2.code.append("<EMBED>");
 					htmlEnv.code.append(returnedcode);
 					htmlEnv2.code.append(returnedcode);
 					htmlEnv2.code.append("</EMBED>");
 				}
 
 				if(GlobalEnv.isAjax())
 					htmlEnv.code.append("</div>");
 				// end ajax /////////////////////////////////////////////////////////////////
 
 				if(htmlEnv.embedCount >= 1)
 				{
 					htmlEnv.css.append(codegenerator.generateCode3(parser,dc.getData()));
 					htmlEnv.cssFile.append(codegenerator.generateCssfile(parser,dc.getData()));
 				}
 
 				//restore original config
 				GlobalEnv.setEnv(tmphash);
 
 				//writing tmpfile
 				Log.out("embed hogehoge:"+GlobalEnv.isNewEmbed());
 				Log.out("enb:"+GlobalEnv.getEnv());
 
 				if(GlobalEnv.isNewEmbed() == 1)
 				{
 					GlobalEnv.addEmbedFile(makedfilename);
 					Log.out("embed start writing");
 					String filename = GlobalEnv.getEmbedTmp();
 
 					if(filename.endsWith("/") || filename.endsWith("\\"))
 						filename = filename + makedfilename;
 					else
 						filename = filename + "/" + makedfilename;
 
 					try {
 						OutputStream fout = new FileOutputStream(filename);
 			        	OutputStream bout = new BufferedOutputStream(fout);
 			        	OutputStreamWriter out = new OutputStreamWriter(bout,"UTF-8");
 
 			        	out.write(htmlEnv.header.toString());
 			        	out.write(returnedcode.toString());
 			        	out.write(htmlEnv.footer.toString());
 
 			        	out.close();
 						/*
 						PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
 			                    filename)));
 			            Log.out("filename:"+filename);
 			            pw.println(html_env.header);
 			            pw.println(returnedcode);
 			            pw.println(html_env.footer);
 			            pw.close();
 						 */
 			        } catch (FileNotFoundException fe) {
 
 			        	fe.printStackTrace();
 			        	System.err.println("Error: specified embedtmp outdirectory \""
 			                    + GlobalEnv.getEmbedTmp() + "\" is not found to write " + htmlEnv.fileName );
 
 		                GlobalEnv.addErr("Error: specified embedtmp outdirectory \""
 			                    + GlobalEnv.getEmbedTmp() + "\" is not found to write " + htmlEnv.fileName);
 	                    //comment out by chie
 			        	//System.exit(-1);
 			        } catch (IOException e) {
 			            System.err.println("Error[HTMLManager]: File IO Error in HTMLManager at embed");
 			            e.printStackTrace();
 			            GlobalEnv.addErr("Error[HTMLManager]: File IO Error in HTMLManager at embed");
 			            //comment out by chie
 			            //System.exit(-1);
 			        }
 				}
 
     		}
     	}
     	//embed html file
     	else if(file.contains(".html"))
     	{
             String line = new String();
 
             if(decos.containsKey("divid"))
             	divname = decos.getStr("divid");
             else if(file.contains("\\"))
             	divname = file.substring(file.lastIndexOf("\\")+1,file.indexOf(".html"));
             else if(file.contains("/"))
             	divname = file.substring(file.lastIndexOf("/")+1,file.indexOf(".html"));
             else
             	divname = file.substring(0,file.indexOf(".html"));
 
             BufferedReader dis;
             try {
             	if(file.contains("http://"))
             	{
             		URL fileurl = new URL(file);
 
             		URLConnection fileurlConnection = fileurl.openConnection();
             		dis = new BufferedReader(new InputStreamReader(fileurlConnection.getInputStream()));
             	}
             	else{
             		try{
             			Log.out("embed file (html):"+file);
             			dis = new BufferedReader(new FileReader(new File(file)));
             		}catch(IOException ioe){
             			String path = htmlEnv.outFile;
             			if(path.contains("\\"))
             				path = path.substring(0,path.lastIndexOf("\\")+1);
             			else if(path.contains("/"))
             				path = path.substring(0,path.lastIndexOf("/")+1);
             			if(file.startsWith("./")){
             				file = file.substring(1,file.length());
             			}
             			Log.out("embed file (html):"+path+file);
 	            			if(path.startsWith("http:")){
 	            				URL fileurl = new URL(path + file);
 	                    		URLConnection fileurlConnection = fileurl.openConnection();
 	            				dis = new BufferedReader(new InputStreamReader(fileurlConnection.getInputStream()));
 	            			}else{
 	                			dis = new BufferedReader(new FileReader(new File(path+file)));
 
 	            			}
             		}
             	}
                /* DataInputStream dis = new
                 DataInputStream(fileurlConnection.getInputStream());*/
                 line = dis.readLine(); //read <BODY> and/or <HEAD>
                 if(line.contains("<head>"))
                 {
                 }
                 else
                 {
                 	line = dis.readLine(); //read <HEAD>
                 }
 
 
                	while(!line.equalsIgnoreCase("</head>"))
                	{
                		line = dis.readLine();
                		if(!line.equalsIgnoreCase("</head>"))
                			htmlEnv.header.append(line+"\n");
                	}
                	line = dis.readLine(); //read <body>
 
     			htmlEnv.code.append("<div id=\""+divname+"\" ");
 
     			if(decos.containsKey("class"))
     				htmlEnv.code.append("class=\""+decos.getStr("class")+ "\" ");
 
     			htmlEnv.code.append(">");
 
 
        			htmlEnv2.code.append("<EMBED>");
                	while(!line.equalsIgnoreCase("</body>"))
                	{
                		Log.out("line : "+line);
                		line = dis.readLine();
                		if(!line.equalsIgnoreCase("</body>")){
                			htmlEnv.code.append(line);
                	        if(line.contains("&"))
                	        	line = line.replace("&", "&amp;");
                			if(line.contains("<"));
                				line = line.replace("<", "&lt;");
                			if(line.contains(">"))
                		        line = line.replace(">", "&gt;");
                	        if(line.contains(""))
                	        	line = line.replace("", "&#65374;");
                			htmlEnv2.code.append(line);
                		}
                	}
        			htmlEnv2.code.append("</EMBED>");
 //    			html_env.code.append("<br><a href=\"close.html\" class=\"bottom_close_"+divname+"\" onClick=\"return closeDiv('"+divname+"')\">close</a><br>");
 
                	htmlEnv.code.append("</div>");
                 dis.close();
 
             } catch (MalformedURLException me) {
                 System.out.println("MalformedURLException: " + me);
             } catch (IOException ioe) {
                 System.out.println("HTMLFuncEmbed:IOException: " + ioe);
             }
 
     	}
     	if(!is_hidden)
     		htmlEnv.code.append("</td></tr></table>");
 
     	htmlEnv.embedCount += 1;
     }
     //tk end////////////////////////////////////////////////////////////////////////////
 
     protected void Func_sinvoke(ExtList data_info) {
         String file = this.getAtt("file");
         String action = this.getAtt("action");
         int attNo = 1;
         String att = new String();
         Log.out("sinvoke file 3: "+file);
 
         //tk start/////////////////////////////////////////////////////////////
         /*
         if (file.indexOf("/") > 0) {
             file = file.substring(file.lastIndexOf("/") + 1);
         }
 */
         //tk end//////////////////////////////////////////////////////////////
       	Log.out("1 att:" + att + " attNo:" + attNo + " att1:" + this.getAtt("att1"));
 
         while (!this.getAtt("att"+attNo).equals("")){
         	att = att + "_" + this.getAtt("att"+attNo);
         	attNo ++;
         	Log.out("att:" + att + " attNo:" + attNo);
         	//System.out.println(att);
         }
     	try {
 			att = URLEncoder.encode(att, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		if(this.getAtt("action").equals("")){
 		try{
 			if(file.toLowerCase().contains(".sql")){
 				file = file.substring(0, file.indexOf(".sql"));
 			}else if(file.toLowerCase().contains(".html")){
 				file = file.substring(0, file.indexOf(".html"));
 			}
 		}catch(Exception e){
 			GlobalEnv.addErr("Error["+getClassName()+"]: filename is invalid.");
 			System.err.println("Error["+getClassName()+"]: filename is invalid.");
 		}
 
         String filename = new String();
         if(!this.getAtt("att").equals("")){
         	if(this.getAtt("att").toLowerCase().startsWith("http://"))
             	filename = this.getAtt("att");
         	else if(this.getAtt("att").toLowerCase().endsWith(".html"))
             	filename = this.getAtt("att");
             else
             	filename = file + "_" + this.getAtt("att") + ".html";
         }else{
         	filename = file + att + ".html";
         }
 
         filename.replace("\\\\","\\");
         htmlEnv.linkUrl = filename;
         htmlEnv.sinvokeFlag = true;
 
 		}else{
 			String filename = new String();
 	        if(!this.getAtt("att").equals(""))
 	        	filename = action + "/" + this.getAtt("att");
 	        else
 	        	filename = action + att;
 
 	        filename.replace("\\\\","\\");
 	        htmlEnv.linkUrl = filename;
 	        htmlEnv.sinvokeFlag = true;
 		}
 
         //tk to make hyper link to image///////////////////////////////////////////////////
         //tk to ajax
         if(GlobalEnv.isAjax())
         {
         	htmlEnv.linkUrl =  file+".html";
         	htmlEnv.ajaxQuery = file+".sql";
 //        	html_env.ajaxatt = this.getAtt("att");
         	htmlEnv.ajaxCond = this.getAtt("ajaxcond")+"="+this.getAtt("att");
 
     		Date d2 = new Date();
     		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyymmddHHmmss");
     		String today2 = sdf2.format(d2);
 
         	htmlEnv.dragDivId = htmlEnv.ajaxQuery+"+"+htmlEnv.ajaxCond+"&"+today2;
 
         	if(decos.containsKey("in"))
         	{
         		String effect = decos.getStr("in");
 
         		if(effect.equalsIgnoreCase("blind"))
         			htmlEnv.inEffect = 1;
         		if(effect.equalsIgnoreCase("fade"))
         			htmlEnv.inEffect = 2;
         	}
         	if(decos.containsKey("out"))
         	{
         		String effect = decos.getStr("out");
 
         		if(effect.equalsIgnoreCase("blind"))
         			htmlEnv.outEffect = 1;
         		if(effect.equalsIgnoreCase("fade"))
         			htmlEnv.outEffect = 2;
         	}
 
         	if(decos.containsKey("panel"))
         	{
         		htmlEnv.isPanel = true;
         	}
         	if(decos.containsKey("dispdiv"))
         	{
             	String dispdiv = decos.getStr("dispdiv");
             	if(dispdiv.contains("+"))
             	{
             		String tmp2 = dispdiv.substring(0,dispdiv.lastIndexOf("+"));
             		String tmp3 = dispdiv.substring(dispdiv.lastIndexOf("+")+1,dispdiv.length());
 
             		if(tmp3.compareTo("att") == 0)
             		{
             			htmlEnv.ajaxtarget = tmp2 + "_" + this.getAtt("att");
             		}
             		else
             			htmlEnv.ajaxtarget = dispdiv;
             	}
             	else
             	{
             		htmlEnv.ajaxtarget = dispdiv;
             	}
             	htmlEnv.hasDispDiv = true;
             	Log.out("html_env.ajaxtarget:"+htmlEnv.ajaxtarget);
         	}
         	else if(decos.containsKey("dragto"))
         	{
         		Log.out("draggable = ture");
         		htmlEnv.draggable = true;
 
 
         		//drag to
         		String value = decos.getStr("dragto");
     			String[] droptarget = new String[100];
     			int targetnum = 0;
 
         		if(value.contains("+"))
         		{
         			while(true)
         			{
         				if(!value.contains("+"))
         				{
         					droptarget[targetnum] = value;
         					targetnum++;
         					break;
         				}
         				droptarget[targetnum] = value.substring(0,value.indexOf("+"));
         				value = value.substring(value.indexOf("+")+1,value.length());
 
         				targetnum++;
         			}
         		}else
         			droptarget[0] = value;
 
 
         		//script 
         		Date d1 = new Date();
         		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
         		String today = sdf.format(d1);
 
         		String scriptname = "drop"+today + htmlEnv.scriptNum;
         		htmlEnv.script.append(scriptname+" = new DragDrop(\""+
         				htmlEnv.dragDivId+"\", \""+droptarget[0]+"\");\n");
 
         		Log.out(scriptname+" = new DragDrop(\""+
         				htmlEnv.dragDivId+"\", \""+droptarget[0]+"\");\n");
 
         		//for tab
         		htmlEnv.script.append(scriptname+".addToGroup(\"myTab\");\n");
 
         		for(int i = 1; i < targetnum ; ++i)
         		{
         			htmlEnv.script.append(scriptname+".addToGroup(\""+droptarget[i]+"\");\n");
         		}
 
         		htmlEnv.scriptNum++;
         	}
         }
         if(this.getArgs().get(0) instanceof FuncArg)
         {
         	Log.out("ARGS are function");
         	FuncArg fa = (FuncArg) this.getArgs().get(0);
         	fa.workAtt();
         }
         else
             this.workAtt("default");
         //tk//////////////////////////////////////////////////
 
         htmlEnv.sinvokeFlag = false;
         return;
     }
 
     public static String opt(String s){
     	if(s.contains("\"")){
     		s = s.replaceAll("\"","");
     	}
     	if(s.startsWith("./")){
     		s = s.substring(2,s.length());
     	}
     	if(s.startsWith("/")){
     		s = s.substring(1,s.length());
     	}
     	return s;
     }
     
 
     private void Func_youtube(){
     	/*
     	 * <object width="480" height="385"><param name="movie" value="http://www.youtube.com/v/f3Afu5CBZUA?fs=1&amp;hl=ja_JP"></param><param name="allowFullScreen" value="true"></param><param name="allowscriptaccess" value="always"></param><embed src="http://www.youtube.com/v/f3Afu5CBZUA?fs=1&amp;hl=ja_JP" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="480" height="385"></embed></object>
          */
 
         String path = this.getAtt("default");
     	String a = "<object width=\"480\" height=\"385\">" +
     			"<param name=\"movie\" value=\"http://www.youtube.com/v/" +
     			path +
     			"?fs=1&amp;hl=ja_JP\"></param><param name=\"allowFullScreen\" value=\"true\">" +
     			"</param><param name=\"allowscriptaccess\" value=\"always\"></param><embed src=\"http://www.youtube.com/v/" +
     			path +
     			"?fs=1&amp;hl=ja_JP\" type=\"application/x-shockwave-flash\" " +
     			"allowscriptaccess=\"always\" allowfullscreen=\"true\" width=\"480\" height=\"385\">" +
     			"</embed></object>";
     	htmlEnv.code.append(a);
     	return;
     }
 
     private void Func_moviefile(){
 
         String path = this.getAtt("path", ".");
         if (!path.startsWith("/")) {
             String basedir = GlobalEnv.getBaseDir();
             if (basedir != null && basedir != "") {
                 path = GlobalEnv.getBaseDir() + "/" + path;
             }
         }
 
        	htmlEnv.code.append("<video class=\"" + HTMLEnv.getClassID(this) +"\" ");
        	if(decos.containsKey("class"))
     		htmlEnv.code.append(decos.getStr("class"));
 
     	htmlEnv.code.append(" \" src=\"" + path + "/" + this.getAtt("default")+"\"");
     	htmlEnv.code.append("autobuffer controls poster=\"http://www.db.ics.keio.ac.jp/ssql/img/ssqllogo.gif\">" +
     			"<p>Try this page in Safari 4! Or you can <a href=\"" +
     			path + "/" + this.getAtt("default")+"\"" +
     			">download the video</a> instead.</p>");
 
     	return;
     }
     private void Func_meter(){
  /*   	<script type="text/javascript">addEvent(window,"load",function() {
 			var layout = [
 				200,				//width
 				40,					//height
 				170,					//m_width
 				25,					//m_height
 				100,				//max
 				0,				//min
 				30,				//low
 				70				//high
 				]
 			var color = [
 				"#FF0000",				//max_color
 				"#00FFFF",				//low_color
 				"#00FFFF",				//high_color
 				"#00FF00",				//mid_color
 				"#CCCCCC"				//bg_color
 			]
 			draw("meter2",layout,color,70);
 			});</script>
 
 <div><canvas width="200" height="40" id="meter2"></canvas></div>
 */
 
     	meter_id++;
 
 
        	htmlEnv.code.append("<script type=\"text/javascript\">addEvent(window,\"load\",function() { \nvar layout = [");
       	if(decos.containsKey("width"))
        		htmlEnv.code.append(decos.getStr("width"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("height"))
        		htmlEnv.code.append(decos.getStr("height"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("m_width"))
        		htmlEnv.code.append(decos.getStr("m_width"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("m_height"))
        		htmlEnv.code.append(decos.getStr("m_height"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("max"))
        		htmlEnv.code.append(decos.getStr("max"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("min"))
        		htmlEnv.code.append(decos.getStr("min"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("low"))
        		htmlEnv.code.append(decos.getStr("low"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("high"))
        		htmlEnv.code.append(decos.getStr("high"));
       	htmlEnv.code.append("] \nvar color = [");
       	if(decos.containsKey("max_color"))
        		htmlEnv.code.append(decos.getStr("max_color"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("low_color"))
        		htmlEnv.code.append(decos.getStr("low_color"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("high_color"))
        		htmlEnv.code.append(decos.getStr("high_color"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("mid_color"))
        		htmlEnv.code.append(decos.getStr("mid_color"));
       	htmlEnv.code.append(",");
       	if(decos.containsKey("bg_color"))
       		htmlEnv.code.append(decos.getStr("bg_color"));
       	htmlEnv.code.append("] \ndraw(\"meter"+meter_id+"\",layout,color,"+this.getAtt("default") +");});</script>");
 
       	htmlEnv.code.append("<div><canvas ");
     	if(decos.containsKey("width"))
        		htmlEnv.code.append("width=" + decos.getStr("width") + " ");
        	else
        		htmlEnv.code.append("width = 200 ");
     	if(decos.containsKey("height"))
        		htmlEnv.code.append("height=" + decos.getStr("height") + " ");
        	else
        		htmlEnv.code.append("height = 40 ");
      	htmlEnv.code.append("id=\"meter"+meter_id+"\"></canvas></div>");
     }
 
 }
