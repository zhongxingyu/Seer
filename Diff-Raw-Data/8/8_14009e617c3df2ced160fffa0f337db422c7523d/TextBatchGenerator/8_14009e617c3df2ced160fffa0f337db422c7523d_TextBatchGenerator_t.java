 package org.kernelab.txbg;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import org.kernelab.basis.Arbiter;
 import org.kernelab.basis.Arbiter.ConditionInterpreter;
 import org.kernelab.basis.ExtensionLoader;
 import org.kernelab.basis.JSON;
 import org.kernelab.basis.JSON.Context;
 import org.kernelab.basis.JSON.JSAN;
 import org.kernelab.basis.TextFiller;
 import org.kernelab.basis.Tools;
 
 public class TextBatchGenerator implements Runnable
 {
 	public static String							DEFAULT_CHARSET_NAME	= "GBK";
 
 	public static Iterable<ConditionInterpreter>	CONDITION_INTERPRETERS;
 
 	protected static final int						LOGICAL_NOT				= -1;
 
 	protected static final int						LOGICAL_OR				= 0;
 
 	protected static final int						LOGICAL_AND				= 1;
 
 	protected static final int						DEFAULT_LOGIC			= LOGICAL_OR;
 
 	protected static String							LAST_DIR				= ".";
 
 	static {
 		Context context = new Context();
 		context.read(new File("./lib/config.js"));
		if (context.attr("charSetName") != null) {
 			DEFAULT_CHARSET_NAME = context.attrString("charSetName");
 		}
		if (context.attr("extentionLibraries") != null) {
 			ExtensionLoader.getInstance().load(context.attrJSAN("extentionLibraries"));
 		}
		if (context.attr("conditionInterpreters") != null) {
 			CONDITION_INTERPRETERS = Arbiter.LoadInterpreters(context.attrJSAN("conditionInterpreters"));
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 
 	}
 
 	private TextBatchGeneratorGUI			gui				= null;
 
 	private TextFiller						filler			= new TextFiller();
 
 	private JSON.Context					jsons			= new JSON.Context();
 
 	private List<File>						list			= new LinkedList<File>();
 
 	private boolean							chain;
 
 	private boolean							generating		= false;
 
 	private Iterable<ConditionInterpreter>	interpreters	= CONDITION_INTERPRETERS;
 
 	public void acceptDataFile(File data)
 	{
 		if (data != null) {
 			acceptDataFiles(Tools.listOfArray(new LinkedList<File>(), new File[] { data }), false);
 		}
 	}
 
 	public void acceptDataFiles(List<File> list, boolean chain)
 	{
 		if (!generating()) {
 			generating(true);
 			this.list.clear();
 			this.list.addAll(list);
 			chain(chain);
 			new Thread(this).start();
 		}
 	}
 
 	protected void accumulate(JSON tags, JSAN aqm, JSAN aqms)
 	{
 		JSON ao = null;
 		JSAN ac = null;
 		boolean as = true;
 		JSAN aqmp = null;
 
 		TextFiller f = new TextFiller();
 
 		int i = 0;
 		int j = 0;
 
 		for (Object o : aqm) {
 
 			if (i++ % 2 == 0) {
 
 				if ((aqmp = JSON.AsJSAN(o)) != null && !aqmp.isEmpty()) {
 
 					for (Object oa : aqmp) {
 
 						if ((ac = JSON.AsJSAN(oa)) != null) {
 							as = Arbiter.Arbitrate(tags, ac, interpreters);
 						} else if ((ao = JSON.AsJSON(oa)) != null) {
 
 							if (as) {
 
 								JSON aq = aqms.attrJSON(j);
 
 								for (Entry<String, Object> entry : ao.entrySet()) {
 									String key = entry.getKey();
 									Object template = entry.getValue();
 									String result = null;
 									if (template != null) {
 										result = (aq.get(key) == null ? "" : aq.get(key))
 												+ f.reset(template.toString()).fillWith(tags).toString();
 									}
 									aq.attr(key, result);
 								}
 							}
 
 							j++;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public boolean chain()
 	{
 		return chain;
 	}
 
 	protected TextBatchGenerator chain(boolean chain)
 	{
 		this.chain = chain;
 		return this;
 	}
 
 	public File chooseResultFile(File file)
 	{
 		JFileChooser fc = new JFileChooser(LAST_DIR);
 
 		if (file != null) {
 			fc.setSelectedFile(file);
 		}
 
 		fc.setDialogTitle("将生成结果文件保存为...");
 
 		File result = null;
 
 		s: while (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
 			if (fc.getSelectedFile().exists()) {
 				switch (JOptionPane.showConfirmDialog(gui, "目标文件已存在，是否覆盖？", "保存目标冲突", JOptionPane.YES_NO_CANCEL_OPTION))
 				{
 					case JOptionPane.CANCEL_OPTION:
 						result = null;
 						break s;
 					case JOptionPane.YES_OPTION:
 						result = fc.getSelectedFile();
 						break s;
 				}
 			} else {
 				result = fc.getSelectedFile();
 				break;
 			}
 		}
 
 		return result;
 	}
 
 	protected JSON extendTag(JSON tag, JSON tags)
 	{
 		TextFiller filler = new TextFiller();
 
 		for (Entry<String, Object> entry : tag.entrySet()) {
 
 			Object value = entry.getValue();
 
 			if (value instanceof String) {
 
 				String tmp = (String) value;
 				String key = entry.getKey();
 
 				Object latest = tags.get(key);
 				if (latest != null) {
 					tmp = filler.reset(tmp).fillWith(key, latest).toString();
 					tag.put(key, tmp);
 				}
 			} else if (value instanceof JSAN) {
 				JSON t = null;
 				for (Object o : (JSAN) value) {
 					if ((t = JSON.AsJSON(o)) != null) {
 						extendTag(t, tags);
 					}
 				}
 			}
 		}
 
 		return tag;
 	}
 
 	protected void fillTemplate(String tmp, JSON tags)
 	{
 		Tools.debug(filler.reset(tmp).fillWith(tags).toString());
 	}
 
 	protected void fillTemplates(JSAN tmps, JSON tags)
 	{
 		if (tmps != null && !tmps.isEmpty()) {
 
 			JSAN tc = null;
 			JSON tt = null;
 			boolean ts = true;
 
 			for (Object t : tmps) {
 				if ((tc = JSON.AsJSAN(t)) != null) {
 					ts = Arbiter.Arbitrate(tags, tc, interpreters);
 				} else if (ts && (tt = JSON.AsJSON(t)) != null) {
 					tags.putAll(extendTag(tt.clone(), tags));
 				} else if (ts && t != null) {
 					fillTemplate(t.toString(), tags);
 				}
 			}
 		}
 	}
 
 	protected JSAN filterTagList(JSAN tags, JSON tag)
 	{
 		JSAN result = new JSAN();
 
 		if (tags != null && !tags.isEmpty()) {
 
 			JSON t = null;
 			JSAN tc = null;
 			boolean ts = true;
 
 			for (Object ot : tags) {
 
 				if ((tc = JSON.AsJSAN(ot)) != null) {
 					ts = Arbiter.Arbitrate(tag, tc, interpreters);
 				} else if (ts && (t = JSON.AsJSON(ot)) != null) {
 					result.add(t);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public void generate(JSON json, JSON tags)
 	{
 		JSAN tag = json.attrJSAN("tag");
 		JSAN aqm = json.attrJSAN("aqm");
 		JSAN tmp = json.attrJSAN("tmp");
 		JSAN sub = json.attrJSAN("sub");
 
 		if (tag != null && !tag.isEmpty()) {
 
 			JSON t = null;
 
 			if (aqm != null && !aqm.isEmpty()) {
 
 				JSAN aqms = new JSAN();
 
 				JSAN aqmp = null;
 
 				int i = 0;
 				for (Object oa : aqm) {
 					if (i++ % 2 == 0 && (aqmp = JSON.AsJSAN(oa)) != null) {
 						JSON ao = null;
 						for (Object o : aqmp) {
 							if (!JSON.IsJSAN(o) && (ao = JSON.AsJSON(o)) != null) {
 								JSON a = new JSON();
 								for (String k : ao.keySet()) {
 									a.put(k, null);
 								}
 								aqms.add(a);
 							}
 						}
 					}
 				}
 
 				JSON temp = null;
 
 				for (Object ot : tag) {
 					if (!JSON.IsJSAN(ot) && (t = JSON.AsJSON(ot)) != null) {
 						temp = tags.clone();
 						temp.putAll(extendTag(t.clone(), tags));
 						accumulate(temp, aqm, aqms);
 					}
 				}
 
 				i = 0;
 				int j = 0;
 				for (Object oa : aqm) {
 
 					aqmp = JSON.AsJSAN(oa);
 
 					switch (i++ % 2)
 					{
 						case 0:
 							if (aqmp != null && !aqmp.isEmpty()) {
 
 								JSON ao = null;
 								JSON at = new JSON();
 
 								for (Object o : aqmp) {
 
 									if (!JSON.IsJSAN(o) && (ao = JSON.AsJSON(o)) != null) {
 
 										JSON aq = aqms.attrJSON(j++);
 
 										for (Entry<String, Object> entry : aq.entrySet()) {
 											String k = entry.getKey();
 											Object v = entry.getValue();
 											if (v != null) {
 												v = (at.get(k) == null ? "" : at.get(k)) + v.toString();
 											}
 											if (v != null || ao.get(k) == null) {
 												at.put(k, v);
 											}
 										}
 									}
 								}
 
 								tags.putAll(extendTag(at, tags));
 							}
 							break;
 
 						case 1:
 							fillTemplates(aqmp, tags.clone());
 							break;
 					}
 				}
 			}
 
 			JSAN tc = null;
 			boolean ts = true;
 
 			for (Object ot : tag) {
 
 				if ((tc = JSON.AsJSAN(ot)) != null) {
 					ts = Arbiter.Arbitrate(tags, tc, interpreters);
 				} else if (ts && (t = JSON.AsJSON(ot)) != null) {
 
 					t = t.clone();
 
 					for (Entry<String, Object> entry : t.entrySet()) {
 						if (JSON.IsJSAN(entry.getValue())) {
 							t.put(entry.getKey(), filterTagList((JSAN) entry.getValue(), tags));
 						}
 					}
 
 					JSON temp = tags.clone();
 					temp.putAll(extendTag(t, temp));
 
 					fillTemplates(tmp, temp);
 
 					if (sub != null && !sub.isEmpty()) {
 
 						JSON s = null;
 						JSAN sc = null;
 						boolean ss = true;
 
 						for (Object os : sub) {
 
 							temp = tags.clone();
 							temp.putAll(t);
 
 							if ((sc = JSON.AsJSAN(os)) != null) {
 								ss = Arbiter.Arbitrate(temp, sc, interpreters);
 							} else if (ss && (s = JSON.AsJSON(os)) != null) {
 								generate(s, temp);
 							}
 						}
 					}
 				}
 			}
 		} else {
 			fillTemplates(tmp, tags);
 		}
 	}
 
 	public boolean generating()
 	{
 		return generating;
 	}
 
 	protected TextBatchGenerator generating(boolean generating)
 	{
 		this.generating = generating;
 		if (gui != null) {
 			gui.processing(generating);
 		}
 		return this;
 	}
 
 	public TextBatchGeneratorGUI gui()
 	{
 		return gui;
 	}
 
 	public TextBatchGenerator gui(TextBatchGeneratorGUI gui)
 	{
 		this.gui = gui;
 		return this;
 	}
 
 	public TextBatchGenerator interpreters(Iterable<ConditionInterpreter> interpreters)
 	{
 		this.interpreters = interpreters;
 		return this;
 	}
 
 	public void process(File data)
 	{
 		if (!chain) {
 			jsons.clear();
 		}
 		jsons.read(data, DEFAULT_CHARSET_NAME);
 		generate(jsons.attrJSON("main"), new JSON().outer(jsons));
 	}
 
 	public void run()
 	{
 		if (list != null && list.size() > 0) {
 
 			for (File file : list) {
 				LAST_DIR = file.getParent();
 				break;
 			}
 
 			File result = null;
 			PrintStream ps = null;
 
 			do {
 				result = chooseResultFile(result);
 
 				if (result == null) {
 					break;
 				}
 
 				String resultFilePath = result.getAbsolutePath();
 
 				boolean legal = true;
 
 				if (legal) {
 					for (File file : list) {
 						if (file.getAbsolutePath().equals(resultFilePath)) {
 							legal = false;
 							JOptionPane.showMessageDialog(gui, "生成结果文件\n" + resultFilePath + "\n不能覆盖模板数据文件，请重新选择！",
 									"错误", JOptionPane.ERROR_MESSAGE);
 							break;
 						}
 					}
 				}
 
 				if (legal) {
 					try {
 						ps = new PrintStream(result, DEFAULT_CHARSET_NAME);
 					} catch (FileNotFoundException e) {
 						legal = false;
 						JOptionPane.showMessageDialog(gui, "生成结果文件\n" + resultFilePath + "\n暂不可写入，请检查该文件是否被其他程序占用！",
 								"警告", JOptionPane.ERROR_MESSAGE);
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (legal) {
 					break;
 				}
 			} while (true);
 
 			if (result != null && ps != null) {
 
 				LAST_DIR = result.getParent();
 
 				for (File data : list) {
 
 					Tools.resetOuts();
 					Tools.debug(data.getAbsolutePath());
 					Tools.getOuts().add(ps);
 
 					try {
 
 						process(data);
 
 					} catch (RuntimeException e) {
 						e.printStackTrace();
 						if (JOptionPane.showConfirmDialog(gui, "不能正确解析模板数据文件：\n" + data.getAbsolutePath()
 								+ "\n是否继续处理剩余的文件？", "错误", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
 						{
 							break;
 						}
 					}
 				}
 
 				ps.close();
 
 				Tools.resetOuts();
 				Tools.debug(result.getAbsolutePath());
 
 				JOptionPane.showMessageDialog(gui, "文本填充结果保存在\n" + result.getAbsolutePath(), "完成",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 		generating(false);
 	}
 }
