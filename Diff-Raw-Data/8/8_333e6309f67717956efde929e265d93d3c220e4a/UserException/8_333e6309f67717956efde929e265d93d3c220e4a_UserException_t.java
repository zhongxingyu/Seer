 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.core.exception;
 
 import org.romaframework.aspect.i18n.I18NAspect;
 import org.romaframework.aspect.i18n.I18NType;
 import org.romaframework.core.Roma;
 
 public class UserException extends RuntimeException {
 
 	private static final long	serialVersionUID	= 5363709029026933291L;
 	protected StringBuilder		message						= new StringBuilder();
 	protected Object					obj;
 
 	public UserException(Object iObject, String iMessage) {
 		init(iObject, iMessage);
 	}
 
 	public UserException(Object iObject, String iMessage, Throwable cause) {
 		super(cause);
 		init(iObject, iMessage);
 	}
 
 	@Override
 	public String getMessage() {
 		return message.toString();
 	}
 
 	private void init(Object iObject, String iText) {
 		message = new StringBuilder();
 		obj = iObject;
 		if (iText == null)
 			return;
		String i18NMessage;
		if (obj == null) {
			i18NMessage = Roma.component(I18NAspect.class).resolve(iText);
		} else {
			i18NMessage = Roma.component(I18NAspect.class).resolve(obj, iText, I18NType.EXCEPTION);
		}
 
 		if (i18NMessage != null)
 			message.append(i18NMessage);
 		else
 			message.append(iText);
 	}
 }
