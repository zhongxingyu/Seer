 package com.xiuhao.commons.search;
 
 import java.util.List;
 
 public interface KeyFinder<V> {
 	/**
 	 * 添加关键字
 	 * 
 	 * @param key
 	 *            添加关键字 如：普8邮资片、纪1、T46、普4、普12邮资封
 	 */
 	void addKey(String key, V value);
 
 	/**
 	 * 根据传入的目标文字，找到包含的关键字
 	 * 
 	 * @param target
 	 *            目标文字 如：贴T46八分一枚，普四四百分两枚，1955.10.20北京寄上海
 	 * @return 从目标文字内找到的关键字列表, 不去重
 	 */
 	List<String> findKeys(String target);
 
 	/**
 	 * 根据传入的目标文字，找到包含的关键字的值的列表
 	 * 
 	 * @param target
	 *            目标文字 如：贴T46八分一枚，普四四百分两枚，1955.10.20北京寄上海
	 * @return 从目标文字内找到的关键字列表, 不去重
 	 */
 	List<V> findValues(String target);
 
 }
