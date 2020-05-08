 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.haiba.lprimporter.rules;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import dk.nsi.haiba.lprimporter.log.Log;
 import dk.nsi.haiba.lprimporter.message.MessageResolver;
 import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
 import dk.nsi.haiba.lprimporter.model.lpr.Administration;
 
 /*
  * This is the 4. rule to be applied to LPR data
  * It takes a list of contacts from a single CPR number, and processes the data with the Remove Identical Contacts rule
  * See the solution document for details about this rule.
  */
 public class RemoveIdenticalContactsRule implements LPRRule {
 
 	private static Log log = new Log(Logger.getLogger(RemoveIdenticalContactsRule.class));
 	private List<Administration> contacts;
 
 	@Autowired
 	ExtendContactEndtimeRule extendContactEndtimeRule;
 
 	@Autowired
 	MessageResolver resolver;
 
 	@Override
 	public LPRRule doProcessing() {
 
 		// if identical procedures and diagnoses exists on the identical contacts, they are cleaned up in a later rule
 		
 		Map<Administration, Administration> items = new HashMap<Administration,Administration>();
 		for (Administration item : contacts) {
 			if (items.values().contains(item)) {
				log.debug("Found duplicate contact with recordnumber: "+item.getRecordNumber());
 				//preserve linked diagnoses and procedures before its removed.
 				Administration preserve = items.get(item);
 				preserve.getLprDiagnoses().addAll(item.getLprDiagnoses());
 				preserve.getLprProcedures().addAll(item.getLprProcedures());
 				// save references to the removed contact
 				preserve.getLprReferencer().add(new LPRReference(item.getRecordNumber()));
 				preserve.getLprReferencer().addAll(item.getLprReferencer());
 			} else {
 				items.put(item,item);
 			}
 		}
 		contacts = new ArrayList<Administration>(items.values());
 		
 		// setup the next rule in the chain
 		extendContactEndtimeRule.setContacts(contacts);
 
 		return extendContactEndtimeRule;
 	}
 
 	public void setContacts(List<Administration> contacts) {
 		this.contacts = contacts;
 	}
 
 	/*
 	 * Package scope for testing purpose
 	 */
 	List<Administration> getContacts() {
 		return contacts;
 	}
 }
