 package framework.base.utils;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Random;
 
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * StringUtil User: Administrator Date: 14-3-12 Time: 下午9:58
  */
 public class StringUtil
 {
 	/**
 	 * string encoding by: new String(s.getBytes("iso-8859-1"), "utf-8");
 	 * 
 	 * @param str
 	 *            str to be encoding
 	 * @return result
 	 */
 	public static String getUTF8String(String str)
 	{
 		if (str == null)
 		{
 			return null;
 		}
 		if (str.equals(""))
 		{
 			return "";
 		}
 
 		try
 		{
 			return new String(str.getBytes("iso-8859-1"), "utf-8");
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			throw new IllegalArgumentException("unknown encoding");
 		}
 	}
 
 	public static String getShortStr(String str, int length)
 	{
 		if (str == null || str.equals(""))
 		{
 			return "";
 		}
 
 		if (str.length() < length)
 		{
 			return str;
 		}
 
 		return StringUtils.substring(str, 0, length) + "...";
 	}
 
 	public static String null2Empty(String str)
 	{
 		return str == null ? "" : str;
 	}
 
 	/**
 	 * 获取指定长度的随机数字
 	 * 
 	 * @param length
 	 * @return
 	 */
 	public static String getRandomNumber(int length)
 	{
 		String result = "";
 		String[] arr = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
 		Random r = new Random();
 		for (int i = 0; i < length; i++)
 		{
 			result += arr[r.nextInt(arr.length)];
 		}
 
 		return result;
 	}
 
 	/**
 	 * 数字左侧补零
 	 * 
 	 * @param str
 	 * @param length
 	 * @return
 	 */
 	public static String addZeroLeft(String str, int length)
 	{
 		if (str == null)
 		{
 			str = "";
 		}
 		String zeros = "";
 		for (int i = 0; i < length - str.length(); i++)
 		{
 			zeros += "0";
 		}
 
 		return zeros + str;
 	}
 
 	public static String htmlStringHandle(String content)
 	{
		if (StringUtils.isBlank(content))
        {
	        return "";
        }
 		String[] arr1 = { "<", ">", "\r\n", "\n", "&" };
 		String[] arr2 = { "&lt;", "&gt;", "<br/>", "<br/>", "&amp;" };
 		for (int i = 0; i < arr1.length; i++)
 		{
 			String str1 = arr1[i];
 			String str2 = arr2[i];
 			content = content.replace(str1, str2);
 		}
 		return content;
 	}
 
 	public static String objectToString(Object object)
 	{
 		if (object == null)
 		{
 			return "";
 		}
 		return object.toString();
 	}
 }
