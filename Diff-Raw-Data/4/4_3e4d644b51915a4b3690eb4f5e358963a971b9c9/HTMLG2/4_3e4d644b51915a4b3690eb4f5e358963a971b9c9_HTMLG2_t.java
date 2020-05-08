 package supersql.codegenerator.Mobile_HTML5;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.Vector;
 
 import supersql.codegenerator.*;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 public class HTMLG2 extends Grouper {
 
     Manager manager;
 
     HTMLEnv html_env;
     HTMLEnv html_env2;
     
     //20130309
     //int gridInt = 0;
     //String[] gridString = {"a","b","c","d","e"};
     //static int ii =0, jj = 0;
     //static String buf = "";
     
     static boolean tableFlg = false;		//20130314  table
     static boolean table0Flg = false;		//20130325  table0
     static boolean divFlg = false;			//20130326  div
     
     //added by goto 20130413  "row Prev/Next"
     int j = 1;
     int row = 1;		//1ページごとの行数指定 (Default: 1, range: 1〜)
     int rowNum = 0;
 //    static int j = 1;
 //    static int row = 1;		//1ページごとの行数指定 (Default: 1, range: 1〜)
 //    static int rowNum = 0;
     //static boolean G2Flg = false;
     
     static int rowFileNum = 1;
     static boolean rowFlg = false;
     private String backfile = new String();
     private int countinstance = 0;
     StringBuffer codeBuf = new StringBuffer();
     
 //    static int codeCount = 0;	//対策
 //    static StringBuffer[] codeArray = new StringBuffer[100];	//対策
 //    
 //	static String parentfile = null;
 //    static String parentnextbackfile = null;
 //    static StringBuffer parentcode = null;
 //    static StringBuffer parentcss = null;
 //    static StringBuffer parentheader = null;
 //    static StringBuffer parentfooter = null;
     
 
     //���󥹥ȥ饯��
     public HTMLG2(Manager manager, HTMLEnv henv, HTMLEnv henv2) {
         this.manager = manager;
         this.html_env = henv;
         this.html_env2 = henv2;
 
     }
 
     //G2��work�᥽�å�
     @Override
 	public void work(ExtList data_info) {
         //G2Flg = true;
         int panelFlg = 0;	//20130503  Panel
     	
     	//added by goto 20130413  "row Prev/Next"
     	//1ページごとの行数指定 (Default: 1, range: 1〜)
     	String parentfile = null;
         String parentnextbackfile = null;
         StringBuffer parentcode = null;
         StringBuffer parentcss = null;
         StringBuffer parentheader = null;
         StringBuffer parentfooter = null;
         
         if(decos.containsKey("row")){
 //Log.i("first in!!");
         	parentfile = html_env.filename;
             parentnextbackfile = html_env.nextbackfile;
             parentcode = html_env.code;
             parentcss = html_env.css;
             parentheader = html_env.header;
             parentfooter = html_env.footer;
 	        html_env.css = new StringBuffer();
 	        html_env.header = new StringBuffer();
 	        html_env.footer = new StringBuffer();
         	//Log.info("row:"+decos.getStr("row").replace("\"", ""));
         	row = Integer.parseInt(decos.getStr("row").replace("\"", ""));
         	if(row<1){	//範囲外のとき
         		Log.info("<<Warning>> row指定の範囲は、1〜です。指定された「row="+row+"」は使用できません。デフォルト値(1)を使用します。");
         		row = 1;
         	}
 //            Log.info("1:	"+decos+"	"+codeCount);
 //        	//decos.remove("row");	//対策
         	rowFlg = true;
         }
 ////        codeCount++;
 //        Log.info("2:	"+decos+"	"+codeCount+"	"+rowFlg);
 
 //        Vector vector_local = new Vector();
  
         Log.out("------- G2 -------");
         this.setDataList(data_info);
         if(HTMLEnv.getSelectFlg())
         	data_info = (ExtList) data_info.get(0);
 
         //tk start////////////////////////////////////////////////////
         html_env.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
         
         //20130325  table0
         if(decos.containsKey("table0"))	table0Flg = true;
         else							table0Flg = false;
     	//20130314  table
         //if(decos.containsKey("table") || !decos.containsKey("div") || table0Flg){
 //        if(decos.containsKey("table") || table0Flg){
         if(decos.containsKey("table") || table0Flg || HTMLC1.tableFlg || HTMLC2.tableFlg || HTMLG1.tableFlg){
     		tableFlg = true;
     	}//else	tableFlg = false;
         
         //20130326  div
    		//if(decos.containsKey("div") || HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
         if(decos.containsKey("div")){
     		divFlg = true;
     		tableFlg = false;
     	}//else divFlg = false;
         
         if(!GlobalEnv.isOpt()){
         	//20130503  Panel
     	    panelFlg = HTMLC1.panelProcess1(decos, html_env);
         	
         	//20130330 tab
         	//tab1
         	if(decos.containsKey("tab1")){
             	html_env.code.append("<div data-role=\"content\"> <div id=\"tabs\">\n<ul>\n");
             	html_env.code.append("	<li><a href=\"#tabs-"+HTMLEnv.tabCount+"\">");
             	if(!decos.getStr("tab1").equals(""))	html_env.code.append(decos.getStr("tab1"));
             	else          							html_env.code.append("tab1");
             	html_env.code.append("</a></li>\n");
             	html_env.code.append("</ul>\n<div id=\"tabs-"+HTMLEnv.tabCount+"\">\n");
 //            	HTMLEnv.tabFlg = true;
             }
         	//tab2〜tab15
 //        	else if(HTMLEnv.tabFlg){
         	else{
         		int i=2;
         		while(i<=HTMLEnv.maxTab){		//HTMLEnv.maxTab=15
         			//Log.info("i="+i+" !!");
         			if(decos.containsKey("tab"+i) || (i==2 && decos.containsKey("tab"))){
     	        		//replace: </ul>の前に<li>〜</li>を付加
     	        		String a = "</ul>";
     	        		String b = "	<li><a href=\"#tabs-"+HTMLEnv.tabCount+"\">";
     	        		if(decos.containsKey("tab"+i))
 	    	        		if(!decos.getStr("tab"+i).equals(""))	b += decos.getStr("tab"+i);
 	    	            	else				            		b += "tab"+i;
     	        		else
     	        			if(!decos.getStr("tab").equals(""))		b += decos.getStr("tab");
 	    	            	else				            		b += "tab";
     	            	b += "</a></li>\n";
     	            	HTMLManager.replaceCode(html_env, a, b+a);
     	            	
     	            	//replace: 最後の</div></div></div>カット
     	        		HTMLManager.replaceCode(html_env, "</div></div></div>", "");
     	        		
     	        		//replace: 不要な「<div class=〜」をカット
     	        		HTMLManager.replaceCode(html_env, "<div class=\""+HTMLEnv.getClassID(this)+" \">", "");
     	        		
     	            	html_env.code.append("<div id=\"tabs-"+HTMLEnv.tabCount+"\">\n");
     	            	break;
     	        	}
         			i++;
 //        			if(i>HTMLEnv.maxTab)	HTMLEnv.tabFlg =false;
         		}
         	}
         	
         	//20130312 collapsible
         	if(decos.containsKey("collapse")){
             	html_env.code.append("<DIv data-role=\"collapsible\" data-content-theme=\"c\" style=\"padding: 0px 12px;\">\n");
             	
             	//header
             	if(!decos.getStr("collapse").equals(""))
             		html_env.code.append("	<h1>"+decos.getStr("collapse")+"</h1>\n");
             	else
             		html_env.code.append("<h1>Contents</h1>\n");
             }
         	
         	//20130309
         	//20130314  table
         	if(tableFlg){
         		html_env.code.append("<TABLE width=\"100%\" cellSpacing=\"0\" cellPadding=\"0\" border=\"");
         		//html_env.code.append("<TABLE width=\"100%\" align=\"center\" cellSpacing=\"0\" cellPadding=\"0\" border=\"");
 		        //html_env.code.append("<TABLE cellSpacing=\"0\" cellPadding=\"0\" border=\"");
 	        	//html_env.code.append( ((!decos.containsKey("table0"))? html_env.tableborder : "0") + "\" ");
 	        	if(table0Flg)	html_env.code.append("0" + "\"");	//20130325 table0
 	        	else			html_env.code.append(html_env.tableborder + "\"");
 //		        html_env.code.append(html_env.tableborder + "\" ");
 	        	
 	        	//classid������Ȥ��ˤ�������
 	        	if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 	        		html_env.code.append(" class=\"");
 	        		html_env.code.append(HTMLEnv.getClassID(this));
 	        	}
 	        	if(decos.containsKey("class")){
 	        		if(!html_env.written_classid.contains(HTMLEnv.getClassID(this)))
 	        			html_env.code.append(" class=\"");
 	        		else
 	        			html_env.code.append(" ");
 	        		html_env.code.append(decos.getStr("class")+"\" ");
 	        	}else if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 	        		html_env.code.append("\" ");
 	        	}
 //		        Log.out("embed flag :" + html_env.embedflag);        
 ////		        html_env.code.append(" class=\"");
 //		        if(html_env.embedflag)
 //		        	html_env.code.append(" embed ");
 //		        if(decos.containsKey("outborder"))
 //		        	html_env.code.append(" noborder ");
 //		        if(decos.containsKey("class")){
 //		        	//class=menu�Ȃǂ̎w�肪��������t��
 //		        	html_env.code.append(" class=\"");
 //		        	html_env.code.append(decos.getStr("class") + "\"");
 //		        }
 //		        if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 //		        	//TFE10000�Ȃǂ̎w�肪��������t��
 //		        	html_env.code.append(" class=\"");
 //		        	html_env.code.append(HTMLEnv.getClassID(this) + "\"");
 //		        }
 //	//	        html_env.code.append("nest\"");
 //	//	        html_env.code.append(html_env.getOutlineMode());
 		        html_env.code.append(">\n");		//added '\n' 20130110
         	}
         }
         //tk end/////////////////////////////////////////////////////
         
         Log.out("<TABLE class=\""+HTMLEnv.getClassID(this) + "\">");
 
         //html_env2.code.append("<tfe type=\"connect\" dimension=\"2\" >");
         int i = 0;
         while (this.hasMoreItems()) {
         	//[重要] For [ [], ]!        	
         	HTMLG1.jj = 0;
         	HTMLG1.gridInt = 0;
         	
             if(decos.containsKey("table0") || HTMLC1.table0Flg || HTMLC2.table0Flg || HTMLG1.table0Flg)	table0Flg = true;
             if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLC2.tableFlg || HTMLG1.tableFlg || table0Flg)	tableFlg=true;
             //if(decos.containsKey("div") || HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
         	if(decos.containsKey("div")){
         		divFlg = true;
         		tableFlg = false;
         	}
         	
             html_env.glevel++;
             
             //added by goto 20130413  "row Prev/Next"
             if(rowFlg){
 //            	codeArray[codeCount]=html_env.code;
 //対策	        
 //            	if(codeCount==1)	Log.i(html_env.code);
             	html_env.code = new StringBuffer();
 //            	//if(codeCount>0) html_env.code.append(codeArray[codeCount-1]);
 ////if(codeCount==0)
 //	codeCount++;
 
             	backfile = html_env.nextbackfile;
                 html_env.countfile++;
                 countinstance++;
                 html_env.filename = html_env.outfile + "_row" + rowFileNum + "_" + j + ".html";
                 html_env.nextbackfile = html_env.linkoutfile + "_row" + rowFileNum + "_" + j + ".html";
                 html_env.setOutlineMode();
             }
             
             Log.out("selectFlg"+HTMLEnv.getSelectFlg());
             Log.out("selectRepeatFlg"+HTMLEnv.getSelectRepeat());
             Log.out("formItemFlg"+HTMLEnv.getFormItemFlg());
             if( HTMLEnv.getSelectRepeat() ){//if form_select
             		//null
             		//in case "select" repeat : not write "<TR><TD>" between "<option>"s
             }else{
                 //20130312 collapsible
     	      	if(decos.containsKey("collapse"))
     	          	html_env.code.append("<p>\n");
             	
             	//20130309
             	//gridInt %= 5;
             	//html_env.code.append("\n	<div class=\"ui-block-"+gridString[gridInt]+"\">\n");
             	if(!tableFlg)	html_env.code.append("\n	<div class=\""+HTMLEnv.getClassID(tfe)+" \">\n");	//20130309  div
                 //20130314  table
             	else{
 		            html_env.code.append("<TR><TD class=\""
 		                    + HTMLEnv.getClassID(tfe) + " nest\">\n");
             	}
 	            Log.out("<TR><TD class=\""
 	                    + HTMLEnv.getClassID(tfe) + " nest\">");
             }
             String classid = HTMLEnv.getClassID(tfe);
 
             
             if(GlobalEnv.isOpt() && !HTMLEnv.getSelectRepeat()){
 	            html_env2.code.append("<tfe type=\"repeat\" dimension=\"2\"");
 	            html_env2.code.append(" border=\"" + html_env.tableborder + "\"");
 	
 	            if (decos.containsKey("tablealign") )
 	            	html_env2.code.append(" align=\"" + decos.getStr("tablealign") +"\"");
 	            if (decos.containsKey("tablevalign") )
 	            	html_env2.code.append(" valign=\"" + decos.getStr("tablevalign") +"\"");
 	            
 
 	            if(decos.containsKey("class")){
 		        	//class=menu�Ȃǂ̎w�肪��������t��
 	            	html_env2.code.append(" class=\"");
 		        	html_env2.code.append(decos.getStr("class") + " ");
 		        }
 	            if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 		        	//TFE10000�Ȃǂ̎w�肪��������t��
 	            	if(decos.containsKey("class")){
 	            		html_env2.code.append(HTMLEnv.getClassID(this) + "\"");
 	            	}else{
 	            		html_env2.code.append(" class=\""
 	            				+ HTMLEnv.getClassID(this) + "\"");
 	            	}
 	            }else if(decos.containsKey("class")){
 	            	html_env2.code.append("\"");
 	            }
 	            
 	            if(decos.containsKey("tabletype")){
 	            	html_env2.code.append(" tabletype=\"" + decos.getStr("tabletype") + "\"");
 	            	if(decos.containsKey("cellspacing")){
 	                	html_env2.code.append(" cellspacing=\"" + decos.getStr("cellspacing") + "\"");
 	                }
 	            	if(decos.containsKey("cellpadding")){
 	                	html_env2.code.append(" cellpadding=\"" + decos.getStr("cellpadding") + "\"");
 	                }
 	            }
 	            html_env2.code.append(">");
             }
 
             this.worknextItem();
             if(decos.containsKey("table0") || HTMLC1.table0Flg || HTMLC2.table0Flg || HTMLG1.table0Flg)	table0Flg = true;
             if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLC2.tableFlg || HTMLG1.tableFlg || table0Flg)	tableFlg=true;
             //if(decos.containsKey("div") || HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
         	if(decos.containsKey("div")){
         		divFlg = true;
         		tableFlg = false;
         	}
             
             if (html_env.not_written_classid.contains(classid) && html_env.code.indexOf(classid) >= 0 ){
             	html_env.code.delete(html_env.code.indexOf(classid),html_env.code.indexOf(classid)+classid.length()+1);
             }
             
             
             if(HTMLEnv.getSelectRepeat()){
             	
             }else{	 
                 //chie
                 html_env2.code.append("</tfe>");
                 //added by goto 20130110 start
                 if(HTMLFunction.slideshowFlg==true){
                 	html_env.code.append("</ul>\n</div>\n");
                 }
                 //added by goto 20130110 end
                 if(!tableFlg)	html_env.code.append("	</div>\n");		//20130309  div
                 else	html_env.code.append("</TD></TR>\n");			//20130314  table
                 Log.out("</TD></TR>");
                 
                 //20130312 collapsible
     	      	if(decos.containsKey("collapse"))
     	          	html_env.code.append("</p>\n");
             }
             
             
             i++;
             html_env.glevel--;
             
             //added by goto 20130413  "row Prev/Next"
             if(rowFlg){
             	codeBuf.append(html_env.code);
             	if((rowNum+1)%row==0){
 	                createHTMLfile();
 //Log.i(codeBuf);	
 	                j++;
 	                codeBuf = new StringBuffer();
                 }
                 rowNum++;
             }
         }
         
         //added by goto 20130413  "row Prev/Next"
         if(rowFlg){
         	if(rowNum%row!=0){	//最後の child HTML を create
 	            //codeBuf.append(html_env.code);
 	            createHTMLfile();
         	}
 //Log.info("last in!!");
 //Log.info("parentfile: "+parentfile);
         	//ファイル名・コード等をparent HTMLのものへ戻す
         	html_env.filename = parentfile;
         	html_env.code = parentcode;
             html_env.css = parentcss;
             html_env.header = parentheader;
             html_env.footer = parentfooter;
             html_env.nextbackfile = parentnextbackfile;
             Log.out("TFEId = " + HTMLEnv.getClassID(this));
             html_env.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
 //Log.info(row+"	"+rowNum);
             //parent HTMLへ<iframe>等を埋め込む
             int first = 1, last = ((rowNum%row!=0)? (rowNum/row+1):(rowNum/row));
             String divID="rowDiv"+rowFileNum+"-";
             String iframeName ="rowIframe"+rowFileNum;
             String HTMLfilename=html_env.filename.substring(0,html_env.filename.indexOf(".html"));
 //Log.i("HTMLfilename:	"+HTMLfilename);
 			//            if(html_env.linkoutfile.contains("/"))
 			//            	HTMLfilename = HTMLfilename.substring(HTMLfilename.lastIndexOf("/")+1);
 			//added by goto 20130417 start
 			//HTMLfilenameを絶対パスから「相対パス形式」へ変更
 			String fileDir = new File(HTMLfilename).getAbsoluteFile().getParent();
 //Log.i("fileDir:	"+fileDir);
 			if(fileDir.length() < HTMLfilename.length()
 			&& fileDir.equals(HTMLfilename.substring(0,fileDir.length()))){
 				HTMLfilename = HTMLfilename.substring(fileDir.length()+1);
 			}
 			//added by goto 20130417 end
             HTMLfilename = HTMLfilename+"_row"+rowFileNum+"_";
 //Log.i(HTMLfilename);
             html_env.code.append(
             		"	<script type=\"text/javascript\">\n" +
            		"		$(document).ready(function(){\n" +
            		"			rowIframePrevNext("+first+", "+last+", '"+divID+"', '"+iframeName+"', '"+HTMLfilename+"', '"+row+"', '"+rowNum+"');\n" +
            		"		});\n" +
             		"	</script>\n" +
             		"	<hr>\n" +
             		"	<div id=\""+divID+"1\"></div>\n" +
             		"	<hr>\n" +
             		"	<iframe src=\""+HTMLfilename+"1.html\" id=\"rowIframeAutoHeight"+rowFileNum+"\" name=\""+iframeName+"\" style=\"border:0; width:90%; overflow:hidden;\">\n" +
             		"	</iframe>\n" +
             		"	<script type=\"text/javascript\">\n" +
             		"	    $('#rowIframeAutoHeight"+rowFileNum+"').iframeAutoHeight();\n" +
 //            		"	    jQuery('#iframe_autoheight"+rowFileNum+"').iframeAutoHeight();\n" +
 //            		"	    jQuery('[id=iframe_autoheight"+rowFileNum+"]').iframeAutoHeight();\n" +
             		"	</script>\n" +
             		"	<hr>\n" +
             		"	<div id=\""+divID+"2\"></div>\n" +
             		"	<hr>\n");
         }
         
         if(HTMLEnv.getSelectRepeat()){		
 	        if(HTMLEnv.getSelectRepeat()){
 	        	//chie
 	            html_env2.code.append("</select></VALUE></tfe>");
 	        	html_env.code.append("</select></TD></TR>\n");
 	        	Log.out("</TD></TR>");
 	        	HTMLEnv.setSelectRepeat(false);
 	        	HTMLEnv.incrementFormPartsNumber();
 	        }else{
 	        	HTMLEnv.incrementFormPartsNumber();
 	        }
 		}
 
         //html_env2.code.append("</tfe>");
         //20130314  table
         if(tableFlg){
         	html_env.code.append("</TABLE>\n");		//20130309
         	tableFlg = false;
         	table0Flg = false;		//20130325 table0
         }
         Log.out("</TABLE>");
         
         //20130312 collapsible
       	if(decos.containsKey("collapse")){
           	html_env.code.append("</DIv>");
         }
       	
     	//20130330 tab
 //    	if(HTMLEnv.tabFlg){
     		int a=1;
 	    	while(a<=HTMLEnv.maxTab){
 	    		//Log.info("a="+a);
 	    		if(decos.containsKey("tab"+a) || (a==1 && decos.containsKey("tab"))){
 		    		html_env.code.append("</div></div></div>\n");
 		    		HTMLEnv.tabCount++;
 		    		break;
 		    	}
 		    	a++;
 	    	}
 //    	}
 	    	
     	//20130503  Panel
     	HTMLC1.panelProcess2(decos, html_env, panelFlg);
         
         if(divFlg)	divFlg = false;		//20130326  div
 
         //added by goto 20130413  "row Prev/Next"
         if(rowFlg){
         	rowFileNum++;
         	rowFlg = false;
         }
 
         Log.out("TFEId = " + HTMLEnv.getClassID(this));
         //html_env.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
         //G2Flg = false;
     }
     
     //added by goto 20130413  "row Prev/Next"
     private void createHTMLfile(){
     	html_env.getHeader();
         html_env.getFooter();
         //Log.info(html_env.filename);
     	
         try {
     		PrintWriter pw;
             if (html_env.charset != null)
 	        	pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
 	        			new FileOutputStream(html_env.filename),html_env.charset)));
             else
             	pw = new PrintWriter(new BufferedWriter(new FileWriter(
         	                    html_env.filename)));
             pw.println(html_env.header);
 //            pw.println(html_env.code);
             pw.println(codeBuf);
             //delete: 最後の<BR><BR>カット
             int a = html_env.footer.lastIndexOf("<BR><BR>");
             int b = a+"<BR><BR>".length();
             //Log.info(html_env.footer.delete(html_env.footer.indexOf("<BR><BR>"),html_env.footer.lastIndexOf("<BR><BR>")));
             //Log.info(html_env.footer.delete(a,b));
             pw.println(html_env.footer.delete(a,b));
             pw.close();
             html_env.header = new StringBuffer();
             html_env.footer = new StringBuffer();
         } catch (Exception e) { }
     }
 
     @Override
 	public String getSymbol() {
         return "HTMLG2";
     }
 
 }
