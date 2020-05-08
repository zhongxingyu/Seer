 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.dip.komusubi.botter.gae.service.jal5971;
 
 import static jp.dip.komusubi.botter.gae.GaeContext.CONTEXT;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
import jp.dip.komusubi.botter.api.Resolver;
 import jp.dip.komusubi.botter.gae.model.Entry;
 
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 /**
  * 
  * @author jun.ozeki 
  * @since 2009/11/10
  * @version $Id: WeatherEntry.java 1357 2011-01-04 14:27:38Z jun $
  */
 public class WeatherEntry implements Entry { 
 	private static final long serialVersionUID = 5420575580792343051L;
 //	private static final String KEYWORD_TOMORROW = "【明日";
 //	private static final String KEYWORD_DAY = "日】";
 	private static final String DELIMITER_TAG = "【運航概況】";
 //	private AbstractFactory factory = TwitteeContext.SINGLETON.getTwitteeFactory();
 	private String message;
 	private Date date;
 	private String[] hashTags;
 	private String url;
 	private long id = System.currentTimeMillis();
 
 	/**
 	 * コンストラクタ。
 	 * @param content
 	 */
 	public WeatherEntry(String sourceUrl, String content, String... hashTags) {
 		this.url = sourceUrl;
 		EntryParser parser = new EntryParser();
 		message = parser.parse(content, DELIMITER_TAG);
 		Resolver<Date> resolver = CONTEXT.getResolverManager().getDateResolver();
 		this.date = resolver.resolve(); 
 		this.hashTags = hashTags;
 	}
 	
 	// parse
 //	private void parse(String content) {
 //		Calendar cal = Calendar.getInstance();
 //		cal.add(Calendar.DAY_OF_MONTH, 1);
 //		String keyword = KEYWORD_TOMORROW + DateFormatUtils.format(cal, "d") + KEYWORD_DAY;
 //		String block = content.substring(content.indexOf(keyword)).replaceAll("\\r\\n", "");
 //		String piece;
 //		if (block.contains(DELIMITER_TAG))
 //			piece = block.substring(0, block.indexOf(DELIMITER_TAG));
 //		else
 //			throw new ParseException("\"天気概況\"の終了デリミタの解析が出来ません。 : " + block);
 //		message = piece;
 //	}
 
 	@Override
 	public long getId() {
 		return id ;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public Date getDate() {
 		return date;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String getText() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(message);
 		if (hashTags != null) {
 			for (String tag: hashTags)
 				builder.append(" ").append(tag);
 		}
 //		return StringUtils.abbreviate(builder.toString(), 140);
 		return builder.toString();
 	}
 
 	@Override
 	public URL getUrl() {
 		try {
 			return new URL(url);
 		} catch (MalformedURLException e) {
 			// ignore exception.
 			return null;
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Entry))
 			return false;
 		Entry entry = (Entry) obj;
 		if (entry.getText() == null)
 			return getText() == null;
 		else
 			return entry.getText().equals(getText());
 	}
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 }
