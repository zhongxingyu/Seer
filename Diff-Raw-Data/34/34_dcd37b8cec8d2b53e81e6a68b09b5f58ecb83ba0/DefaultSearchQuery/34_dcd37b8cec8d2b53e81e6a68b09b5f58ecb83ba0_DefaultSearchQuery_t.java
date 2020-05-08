 package com.atlassian.sal.core.search.query;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.atlassian.sal.api.search.parameter.SearchParameter;
 import com.atlassian.sal.api.search.query.SearchQuery;
 import com.atlassian.sal.core.search.parameter.BasicSearchParameter;
 import com.atlassian.sal.core.util.URIUtil;
 
 /**
  */
 public class DefaultSearchQuery implements SearchQuery
 {
     private StringBuffer searchString = new StringBuffer();
     private Map<String, SearchParameter> parameters = new LinkedHashMap<String, SearchParameter>();
 
     public DefaultSearchQuery(String query)
 	{
         append(query);
 	}
 
     public SearchQuery setParameter(String name, String value)
     {
     	parameters.put(name, new BasicSearchParameter(name, value));
         return this;
     }
 
 	public String getParameter(String name)
 	{
 		final SearchParameter value = parameters.get(name);
 		
 		return value == null ? null : value.getValue();
 	}
 
 	public String buildQueryString()
 	{
 		StringBuilder builder = new StringBuilder(searchString);
 		for (SearchParameter parameter : parameters.values())
 		{
 			builder.append('&');
 			builder.append(parameter.buildQueryString());
 		}
 		return builder.toString();
 	}
 
 	public SearchQuery append(String query)
 	{
         if (StringUtils.isEmpty(query))
         {
             throw new IllegalArgumentException("Cannot parse empty query string!");
         }
         if (query.indexOf(SearchQuery.PARAMETER_SEPARATOR) == -1)
         {
             //looks like there's no params.
             searchString.append(query);
             return this;
         }
         
         final String[] strings = query.split(SearchQuery.PARAMETER_SEPARATOR);
         searchString.append(strings[0]);
         for (int i = 1; i < strings.length; i++)
         {
             String string = strings[i];
             BasicSearchParameter searchParam = new BasicSearchParameter(string);
             parameters.put(searchParam.getName(), searchParam);
         }
 		return this;
 	}
 
    public String getSearchString()
    {
        return URIUtil.decode(searchString.toString());
    }
 
    public int getParameter(String name, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getParameter(name));
        }
         catch (NumberFormatException e)
        {
            // ignore
        }
        return defaultValue;
    }

 }
