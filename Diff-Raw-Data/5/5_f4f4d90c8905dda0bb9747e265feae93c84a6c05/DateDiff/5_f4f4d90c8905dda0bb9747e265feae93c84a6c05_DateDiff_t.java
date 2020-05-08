 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.expr;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.araqne.logdb.Row;
 import org.araqne.logdb.QueryParseException;
 
 /**
  * @since 1.7.2
  * @author xeraph
  * 
  */
 public class DateDiff implements Expression {
 	private final List<Expression> exprs;
 	private final Expression start;
 	private final Expression end;
 	private final int calField;
 
 	private static final long YEAR_DIV = 31536000000L;
 	private static final long MON_DIV = 2592000000L;
 	private static final long DAY_DIV = 86400000L;
 	private static final long HOUR_DIV = 3600000L;
 	private static final long MIN_DIV = 60000L;
 	private static final long SEC_DIV = 1000L;
 
 	public DateDiff(List<Expression> exprs) {
 		this.exprs = exprs;
 
 		if (exprs.size() != 3)
 			throw new QueryParseException("invalid-datediff-args", -1);
 
 		start = exprs.get(0);
 		end = exprs.get(1);
 
 		String s = exprs.get(2).eval(null).toString();
 		if (s.equals("day"))
 			calField = Calendar.DAY_OF_YEAR;
 		else if (s.equals("mon"))
 			calField = Calendar.MONTH;
 		else if (s.equals("year"))
 			calField = Calendar.YEAR;
 		else if (s.equals("hour"))
 			calField = Calendar.HOUR;
 		else if (s.equals("min"))
 			calField = Calendar.MINUTE;
 		else if (s.equals("sec"))
 			calField = Calendar.SECOND;
		else if (s.equals("msec"))
			calField = Calendar.MILLISECOND;
 		else
 			throw new QueryParseException("invalid-datediff-unit", -1);
 	}
 
 	@Override
 	public Object eval(Row map) {
 		Object o1 = start.eval(map);
 		if (o1 == null)
 			return null;
 
 		Object o2 = end.eval(map);
 		if (o2 == null)
 			return null;
 
 		if (o1 instanceof Date && o2 instanceof Date) {
 			Date d1 = (Date) o1;
 			Date d2 = (Date) o2;
 
 			long interval = d2.getTime() - d1.getTime();
 			switch (calField) {
 			case Calendar.YEAR:
 				return interval / YEAR_DIV;
 			case Calendar.MONTH:
 				return interval / MON_DIV;
 			case Calendar.DAY_OF_YEAR:
 				return interval / DAY_DIV;
 			case Calendar.HOUR:
 				return interval / HOUR_DIV;
 			case Calendar.MINUTE:
 				return interval / MIN_DIV;
 			case Calendar.SECOND:
 				return interval / SEC_DIV;
			case Calendar.MILLISECOND:
				return interval;
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return "datediff(" + start + "," + end + "," + exprs.get(2) + ")";
 	}
 }
