 package org.jeecgframework.core.extend.template;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jeecg.system.pojo.base.TSTemplate;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.jeecgframework.core.common.service.CommonService;
 import org.jeecgframework.core.util.ResourceUtil;
 import org.jeecgframework.core.util.StreamUtils;
 import org.jeecgframework.core.util.oConvertUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 
 /**
  * 根据模板生成文件处理类
  * 
  * @author Administrator
  * 
  */
 public class TemplateUtil {
 	public static StringBuffer stb = null;
 	public static Map<Object, Object> teprems;
 	public static Map<Object, Object> eachteprems;
 	public static Map<String, String> dataSourceMap = null;
 	public static BufferedOutputStream bos = null;
 	public static ByteArrayOutputStream baos;// 模板内容输出对象
 	public static FileInputStream fis;// 模板文件输入对象
 	public static InputStreamReader isr = null;// 模板文件读取对象
 	public static BufferedReader br = null;
 	public static CommonService commonService;
 	public static Pattern pattern;
 	public static Pattern fnParm;
 	public static List dataSet;
 	public static TSTemplate ttTemplate = null;
 
 	@Autowired
	public static void setCommonService(CommonService commonService) {
 		TemplateUtil.commonService = commonService;
 	}
 
 	/**
 	 * 根据模板对象动态获取模板内容
 	 * 
 	 * @param template
 	 * @return
 	 */
 	public static String getTemplateContent(Template template) {
 		builderDoc(template);
 		String content = baos.toString();
 		content = baos.toString().replace("?", "");
 		if (baos != null) {
 			try {
 				baos.close();
 				baos.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return content;
 	}
 
 	/**
 	 * 初始化模板构建参数
 	 * 
 	 * @param template
 	 */
 	private static void init(Template template) {
 		baos = StreamUtils.getByteArrayOutputStream();
 		ttTemplate = commonService.findUniqueByProperty(TSTemplate.class, "templatecode", template.getTemplatecCode());
 		String templatepath = ResourceUtil.getSysPath();
 		// + ResourceUtil.getConfigByName("templatepath")
 		// + SystemPath.getSeparator();
 		if (ttTemplate != null) {
 			templatepath += ttTemplate.getTemplatepath();
 		}
 		if (ttTemplate != null) {
 			fis = StreamUtils.getFileInputStream(templatepath);
 		}
 
 		template.setParameterTagValue("dtp");
 		teprems = commonService.getParametMap(template);
 		// template.setParameterTagValue("each");
 		// eachteprems = commonService.getParametMap(template);
 		dataSourceMap = template.getDataSourceMap();
 		dataSet = template.getDataSet();
 		pattern = Pattern.compile("#\\w{2,50}#", Pattern.CASE_INSENSITIVE);
 		fnParm = Pattern.compile("<fn\\s+fnName\\s*=\\s*\\w+\\s*>\\s*.*\\s*</fn>", Pattern.CASE_INSENSITIVE);
 
 	}
 
 	/**
 	 * 根据模板对象构建模板
 	 * 
 	 * @param template
 	 */
 	private static void builderDoc(Template template) {
 		init(template);
 		if (!teprems.isEmpty()) {
 			replaceKeyparm(teprems);
 		}
 		try {
 			if (ttTemplate != null) {
 				isr = new InputStreamReader(fis, "utf-8");
 			} else {
 				isr = new InputStreamReader(StreamUtils.byteTOFInputStream(ttTemplate.getTemplatecontent()), "GBK");
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		br = new BufferedReader(isr);
 		stb = replacedtp(br);// 填充stb
 		if (eachteprems != null) {
 			replacedEach();
 		}
 		try {
 			backContent(baos, stb);// 返回
 		} catch (RuntimeException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				try {
 					isr.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * 返回模板内容输出流
 	 * 
 	 * @param os
 	 * @param stb
 	 */
 	private static void backContent(OutputStream os, StringBuffer stb) {
 		try {
 			bos = new BufferedOutputStream(os);
 			bos.write(stb.toString().getBytes());
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 
 			if (bos != null) {
 				try {
 					bos.close();
 					bos = null;
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * 循环对模板参数(#test#)加上前缀标记(如年度(#2005test#))
 	 */
 	private static void eachreplaceKeyparm(Map<Object, Object> parmMap) {
 		Map<Object, Object> tempkeyparmMap = new HashMap<Object, Object>();
 		Iterator itforeach = parmMap.entrySet().iterator();
 		while (itforeach.hasNext()) {
 			Map.Entry entry = (Map.Entry) itforeach.next();
 			List projectdetials = dataSet;
 			int j = 1;
 			for (Object object : projectdetials) {
 
 				String eachedMapkey = oConvertUtils.getString(j) + entry.getKey();
 				String eachedMapvalue = (String) entry.getValue();
 				Matcher m = pattern.matcher(eachedMapvalue);
 				while (m.find()) {
 					String tempstr = m.group();
 					String tempstrinner = tempstr.substring(1, tempstr.length() - 1);
 					String stringLetter = tempstrinner.substring(0, 1).toUpperCase();
 					String getName = "get" + stringLetter + tempstrinner.substring(1);
 					Method getMethod;
 					Object value;
 					try {
 						getMethod = object.getClass().getMethod(getName, new Class[] {});
 						value = getMethod.invoke(object, new Object[] {});
 						if (tempstrinner.indexOf("onlyone_") == 0)
 							eachedMapvalue = eachedMapvalue.replaceAll(tempstr, "#" + tempstrinner.substring(8) + "#");
 						else {
 							eachedMapvalue = eachedMapvalue.replaceAll(tempstr, oConvertUtils.getString(value));
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				Matcher fm = fnParm.matcher(eachedMapvalue);
 				while (fm.find()) {
 					String tempstr = fm.group();
 					// String head=tempstr.replaceFirst("\\s*</fn>",
 					// "")+","+years[i]+"</fn>";
 					eachedMapvalue = eachedMapvalue.replaceAll("\\s*</fn>", "") + "," + j + "</fn>";
 				}
 				// 2010buildingno ,#2010buildingno#
 				tempkeyparmMap.put(eachedMapkey, eachedMapvalue);
 				j++;
 			}
 
 		}
 		/*
 		 * for (int i = 0; i < items.length; i++) { String eachedMapkey = items[i] + entry.getKey(); String eachedMapvalue = (String) entry.getValue(); Matcher m = pattern.matcher(eachedMapvalue); while (m.find()) { String tempstr = m.group(); String tempstrinner = tempstr.substring(1, tempstr.length() - 1); if (tempstrinner.indexOf("onlyone_") == 0) eachedMapvalue = eachedMapvalue.replaceAll(tempstr, "#" + tempstrinner.substring(8) + "#"); else { eachedMapvalue = eachedMapvalue.replaceAll(tempstr, "#" + items[i] + tempstrinner + "#"); }
 		 * 
 		 * }
 		 */
 
 		// 2012firstfloorarea=#2012firstfloorarea#,
 		// replaceEachparm(tempkeyparmMap);
 
 	}
 
 	private static void replaceEachparm(Map<Object, Object> parmMap) {
 		Iterator itforitem = null;
 		itforitem = parmMap.entrySet().iterator();// 非循环子扩充
 		while (itforitem.hasNext()) {
 			Map.Entry<String, String> entry = (Map.Entry) itforitem.next();
 			String tepremkey = entry.getKey().toString();//
 			String tepremval = entry.getValue().toString();
 			boolean flag = true;
 			Matcher m = pattern.matcher(tepremval);
 			while (m.find()) {
 				String itemkeytemp = m.group();
 				String itemkey = itemkeytemp.substring(1, itemkeytemp.length() - 1);
 				String[] itempart = itemkey.split("__");// itempart[1]为参数的默认值
 				if (itempart[0].indexOf("year_") >= 0) {// 替换年度参数
 					itempart[0] = itempart[0].replaceAll("year_", oConvertUtils.getString(dataSourceMap.get("year"), "undefined"));
 				}
 
 				if (dataSourceMap.containsKey(itempart[0]) && dataSourceMap.get(itempart[0]) != null && !dataSourceMap.get(itempart[0]).equals("")) {
 					String itemvalue = dataSourceMap.get(itempart[0]);
 					tepremval = tepremval.replaceAll(itemkeytemp, itemvalue);
 
 				} else {
 					if (itempart.length > 1 && !itempart[1].equals("")) {
 						tepremval = tepremval.replaceAll(itemkeytemp, itempart[1]);
 					} else {
 						flag = false;
 					}
 				}
 			}
 			if (flag == false) {
 
 				eachteprems.put(tepremkey, "");
 
 			} else {
 				eachteprems.put(tepremkey, tepremval);// 替换表达式中的参数
 			}
 		}
 	}
 
 	/**
 	 * 对dtp模板参数(#test#)类型的参数进行替换处理
 	 */
 	private static void replaceKeyparm(Map<Object, Object> parmMap) {
 		Iterator itforitem = null;
 		itforitem = parmMap.entrySet().iterator();// 非循环子扩充
 		while (itforitem.hasNext()) {
 			Map.Entry<String, String> entry = (Map.Entry) itforitem.next();
 			String tepremkey = entry.getKey().toString();//
 			String tepremval = entry.getValue().toString();
 			boolean flag = true;
 			Matcher m = pattern.matcher(tepremval);
 			while (m.find()) {
 				String itemkeytemp = m.group();
 				String itemkey = itemkeytemp.substring(1, itemkeytemp.length() - 1);
 				String[] itempart = itemkey.split("__");// itempart[1]为参数的默认值
 				if (itempart[0].indexOf("year_") >= 0) {// 替换年度参数
 					itempart[0] = itempart[0].replaceAll("year_", oConvertUtils.getString(dataSourceMap.get("year"), "undefined"));
 				}
 
 				if (dataSourceMap.containsKey(itempart[0]) && dataSourceMap.get(itempart[0]) != null && !dataSourceMap.get(itempart[0]).equals("")) {
 					String itemvalue = dataSourceMap.get(itempart[0]);
 					tepremval = tepremval.replaceAll(itemkeytemp, itemvalue);
 
 				} else {
 					if (itempart.length > 1 && !itempart[1].equals("")) {
 						tepremval = tepremval.replaceAll(itemkeytemp, itempart[1]);
 					} else {
 						flag = false;
 					}
 				}
 			}
 			if (flag == false) {
 
 				teprems.put(tepremkey, "");
 
 			} else {
 				teprems.put(tepremkey, tepremval);// 替换表达式中的参数
 			}
 		}
 	}
 
 	/**
 	 * 填充dtp模板参数
 	 * 
 	 * @param br
 	 * @return
 	 */
 	private static StringBuffer replacedtp(BufferedReader br) {
 
 		Iterator itforitem = teprems.entrySet().iterator();
 		String line = "";
 		stb = new StringBuffer("");
 		// Pattern pParm =
 		// Pattern.compile("#\\w{2,20}#",Pattern.CASE_INSENSITIVE);//a-zA-Z_0-9
 		Pattern pParm = Pattern.compile("<dtp>[\\w]+</dtp>");
 		try {
 			while ((line = br.readLine()) != null) {
 
 				Matcher m = pParm.matcher(line);
 				while (m.find()) {
 					String tpTemp = m.group();
 					String tp = tpTemp.substring(5, tpTemp.length() - 6);
 					if (teprems.containsKey(tp)) {
 						/*
 						 * if ((oConvertUtils.getDouble( teprems.get(tp).toString(), 99999999999.99)) == 0.0) { line = line.replaceAll(tpTemp, teprems.get(tp).toString() ); } else { line = line.replaceAll(tpTemp, teprems.get(tp) .toString()); }
 						 */
 						line = line.replaceAll(tpTemp, teprems.get(tp).toString());
 						// line=line.replaceAll(tpTemp,
 						// teprems.get(tp).toString());
 					} else {
 						line = line.replaceAll(tpTemp, "");
 					}
 				}
 				stb.append(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return stb;
 
 	}
 
 	/**
 	 * 替换each模版参数
 	 */
 	private static void replacedEach() {
 		if (eachteprems != null) {
 			Pattern eachParmH = Pattern.compile("<each\\s+items=[\",\\w,]*\\s*>");
 			Pattern eachParmE = Pattern.compile("</each>");
 			String doccontent = stb.toString();
 			StringBuffer afterDtpEach = new StringBuffer("");
 			Matcher docMatH = eachParmH.matcher(doccontent);
 			Matcher docMatE = eachParmE.matcher(doccontent);
 			int str = 0;
 			int end = 0;
 			while (docMatH.find() && docMatE.find()) {
 				afterDtpEach.append(doccontent.substring(end, docMatH.start()));// 加上开始部分
 				str = docMatH.start();
 				end = docMatE.end();
 				// item=2005,2006,2007 var=itemyear itemindex=index
 				// 内容为“<each items=>”
 				String head = docMatH.group();
 				String headinner = head.substring(1, head.indexOf(">")).trim();
 				String eachtext = doccontent.substring(docMatH.end(), docMatE.start()).trim();
 				// String[] item = null;
 				// item = head.substring(headinner.indexOf("=") + 2,
 				// headinner.length() + 1).split(",");
 				// 在循环段内 完成参数的整理
 				Map<Object, Object> tempeachs = new HashMap();
 				tempeachs.putAll(eachteprems);
 				eachreplaceKeyparm(tempeachs);
 				// replaceHqlparm(tempeachs);
 				// replaceCalparm(tempeachs);
 				// replaceFnparm(tempeachs);
 				Pattern eachtextParm = Pattern.compile("<var>[\\w]+</var>");
 				StringBuffer eachedstr = new StringBuffer("");
 				for (int i = 1; i < dataSet.size() + 1; i++) {
 					Matcher m = eachtextParm.matcher(eachtext);
 					String tempitemstr = eachtext;
 					while (m.find()) {
 						String varTemp = m.group();
 						String varTempinner = varTemp.substring(5, varTemp.length() - 6);
 						if (eachteprems.containsKey(oConvertUtils.getString(i) + varTempinner)) {
 							/*
 							 * if ((oConvertUtils.getDouble( eachteprems.get(oConvertUtils.getString(i) + varTempinner) .toString(), 0.0)) == 0.0) { tempitemstr = tempitemstr .replace( varTemp, "<font color=#ff0000>" + eachteprems .get(oConvertUtils.getString(i) + varTempinner) .toString() + "</font>"); } else { tempitemstr = tempitemstr.replace(varTemp, eachteprems.get(oConvertUtils.getString(i)+ varTempinner) .toString()); }
 							 */
 							tempitemstr = tempitemstr.replace(varTemp, eachteprems.get(oConvertUtils.getString(i) + varTempinner).toString());
 						} else {
 							if ("itemindex".endsWith(varTempinner)) {
 								tempitemstr = tempitemstr.replace(varTemp, (i + 1) + "");
 							} else if ("item".endsWith(varTempinner)) {
 								tempitemstr = tempitemstr.replace(varTemp, oConvertUtils.getString(i));
 							} else {
 								tempitemstr = tempitemstr.replace(varTemp, "");
 							}
 						}
 					}
 					eachedstr.append(tempitemstr);// 各年度的数据文本
 
 					m.reset();
 
 				}
 
 				afterDtpEach.append(eachedstr);//
 
 			}
 			afterDtpEach.append(doccontent.substring(end, doccontent.length()));// 加上尾吧
 
 			stb = afterDtpEach;
 
 		}// end if(eachs!=null)
 
 	}
 
 	public static void replaceCalparm(Map calparmMap) {
 		Iterator itforcal = calparmMap.entrySet().iterator();
 		Pattern cParm = Pattern.compile("<cal>[E\\d\\.\\(\\)\\+\\-\\*\\/]*</cal>");
 		while (itforcal.hasNext()) {
 			Map.Entry entry = (Map.Entry) itforcal.next();
 			String cteprem = entry.getKey().toString();//
 			String caModel = (String) entry.getValue();
 			if (caModel.equals(""))
 				continue;
 
 			Matcher cm = cParm.matcher(caModel);
 			while (cm.find()) {
 				String caltemp = cm.group();
 				String cal = caltemp.substring(5, caltemp.length() - 6);
 				// System.out.println(cal);
 				String realValue = cal(cal);
 
 				caModel = caModel.replaceFirst("<cal>[E\\d\\.\\(\\)\\+\\-\\*\\/]*</cal>", realValue);
 
 			}
 
 			calparmMap.put(cteprem, caModel);
 
 		}
 
 	}
 
 	public void replaceHqlparm(HashMap hqlparmMap) {
 
 		Iterator itforhql = hqlparmMap.entrySet().iterator();
 
 		Pattern hParm = Pattern.compile("<hql\\s*\\w*\\=?\\w*\\s*>[\\w\\(\\)\\+\\.\\=\\s\\>\\<\\-\\,]*</hql>");
 		while (itforhql.hasNext()) {
 			Map.Entry entry = (Map.Entry) itforhql.next();
 			String hteprem = entry.getKey().toString();//
 			String hpModel = (String) entry.getValue();
 			// String hpModeltemp=new String(hpModel);
 			if (hpModel.equals(""))
 				continue;
 
 			Matcher hm = hParm.matcher(hpModel);
 			while (hm.find()) {
 
 				String hqltemp = hm.group();
 
 				String delhadhql = hqltemp.replaceFirst("^<hql\\s*(devalue)*\\s*\\=?\\w*\\s*>", "");
 
 				String deltailhql = delhadhql.replaceFirst("</hql>$", "");
 				// String hql=hqltemp.substring(5, hqltemp.length()-6);
 
 				String realValue = getRealValue(deltailhql);
 
 				if (realValue.equals("NULL"))// 没有查找结果，使用默认值——devalue
 				{
 					String devalue = "";
 					// Pattern
 					// tempp=Pattern.compile("^<hql\\s*(devalue)*\\s*\\=?\\w*\\s*>");
 					// Matcher tempm=tempp.matcher(hqltemp);
 					// if(tempm.find()){
 					// String hadstr = tempm.group();
 					// if(hadstr.indexOf("devalue")>0)
 					// {
 					// devalue=hadstr.substring(hadstr.indexOf("=")+1,
 					// hadstr.length()-1).trim();
 					//
 					// }
 					// }else{
 					devalue = "0";// 最终默认值
 					// }
 					hpModel = hpModel.replaceFirst("<hql\\s*(devalue)*\\s*\\=?\\w*\\s*>[\\w\\(\\)\\+\\.\\=\\s\\>\\<\\-\\,]*</hql>", devalue);
 				} else {
 
 					hpModel = hpModel.replaceFirst("<hql\\s*(devalue)*\\s*\\=?\\w*\\s*>[\\w\\(\\)\\+\\.\\=\\s\\>\\<\\-\\,]*</hql>", realValue);
 				}
 
 			}
 
 			hqlparmMap.put(hteprem, hpModel);
 
 		}
 	}
 
 	public String getRealValue(String hql)// 两次替换变成算式
 	{
 		Session session = commonService.getSession();
 		Query query = session.createQuery(hql);
 		String result = "NULL";
 		try {
 			Object obj = query.uniqueResult();
 			if (obj != null && !obj.toString().equals("")) {
 				result = obj.toString();
 			}
 		} catch (HibernateException e) {
 			System.out.println("Hql 语法有误！！！");
 		}
 		return result;
 	}
 
 	private static String cal(String calStr)// 表达式计算
 	{
 		String result = null;
 		Caculator calc = new Caculator();
 		calc.transInfixToPosfix(calStr);
 		result = calc.calc();
 		return result;
 	}
 }
