 /*
  *  Copyright 2010, 2011 Christopher Pheby
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.jadira.usertype.dateandtime.joda;
 
 import org.jadira.usertype.dateandtime.joda.columnmapper.StringColumnDateTimeZoneWithOffsetMapper;
 import org.jadira.usertype.dateandtime.joda.util.DateTimeZoneWithOffset;
 import org.jadira.usertype.spi.shared.AbstractSingleColumnUserType;
 
 /**
 * Maps a {@link org.joda.time.DateTimeZone} to and from String for Hibernate.
  */
 public class PersistentDateTimeZoneWithOffsetAsString extends AbstractSingleColumnUserType<DateTimeZoneWithOffset, String, StringColumnDateTimeZoneWithOffsetMapper> {
 
     private static final long serialVersionUID = -8759152453256338787L;
 }
