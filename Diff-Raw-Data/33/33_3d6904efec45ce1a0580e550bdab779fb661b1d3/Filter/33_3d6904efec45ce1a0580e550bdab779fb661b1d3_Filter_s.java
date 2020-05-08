 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The ISO21090
  * Software was developed in conjunction with the National Cancer Institute
  * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
  * government employees are authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  *
  * This ISO21090 Software License (the License) is between NCI and You. You (or
  * Your) shall mean a person or an entity, and all other entities that control,
  * are controlled by, or are under common control with the entity. Control for
  * purposes of this definition means (i) the direct or indirect power to cause
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares,
  * or (iii) beneficial ownership of such entity.
  *
  * This License is granted provided that You agree to the conditions described
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  * no-charge, irrevocable, transferable and royalty-free right and license in
  * its rights in the ISO21090 Software to (i) use, install, access, operate,
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the ISO21090 Software; (ii) distribute and
  * have distributed to and by third parties the ISO21090 Software and any
  * modifications and derivative works thereof; and (iii) sublicense the
  * foregoing rights set out in (i) and (ii) to third parties, including the
  * right to license such rights to further third parties. For sake of clarity,
  * and not by way of limitation, NCI shall have no right of accounting or right
  * of payment from You or Your sub-licensees for the rights granted under this
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the
  * above copyright notice, this list of conditions and the disclaimer and
  * limitation of liability of Article 6, below. Your redistributions in object
  * code form must reproduce the above copyright notice, this list of conditions
  * and the disclaimer of Article 6 in the documentation and/or other materials
  * provided with the distribution, if any.
  *
  * Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: This product includes software
  * developed by 5AM and the National Cancer Institute. If You do not include
  * such end-user documentation, You shall include this acknowledgment in the
  * Software itself, wherever such third-party acknowledgments normally appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
  * to endorse or promote products derived from this Software. This License does
  * not authorize You to use any trademarks, service marks, trade names, logos or
  * product names of either NCI or 5AM, except as required to comply with the
  * terms of this License.
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this
  * Software into Your proprietary programs and into any third party proprietary
  * programs. However, if You incorporate the Software into third party
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software
  * into such third party proprietary programs and for informing Your
  * sub-licensees, including without limitation Your end-users, of their
  * obligation to secure any required permissions from such third parties before
  * incorporating the Software into such third party proprietary software
  * programs. In the event that You fail to obtain such permissions, You agree
  * to indemnify NCI for any claims against NCI by such third parties, except to
  * the extent prohibited by law, resulting from Your failure to obtain such
  * permissions.
  *
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the Software, or any derivative works of the
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package gov.nih.nci.iso21090.grid.ser;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.xml.namespace.QName;
 import javax.xml.rpc.NamespaceConstants;
 
 import org.apache.axis.encoding.SerializationContext;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  *
  * @author gax
  */
 public class Filter implements ContentHandler {
 	private final SerializationContext context;
 
 	private final Stack<Map<String, String>> prefixes = new Stack<Map<String, String>>();
 	private Map<String, String> head = new HashMap<String, String>();
 
 	/**
 	 * Constructs a new filter, based on the given context.
 	 * @param context serialization context to use
 	 */
 	public Filter(SerializationContext context) {
 		this.context = context;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void startPrefixMapping(String prefix, String uri) throws SAXException {
 		head.put(prefix, uri);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void endPrefixMapping(String prefix) throws SAXException {
 		head.remove(prefix);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
 
 		prefixes.push(head);
 		head = new HashMap<String, String>();

 		AttributesImpl fixed = new AttributesImpl();
 		for (int i = 0; i < atts.getLength(); i++) {
 			String au = atts.getURI(i);
 			String av = atts.getValue(i);
 			String aln = atts.getLocalName(i);
			String aq = atts.getQName(i);
 			if (NamespaceConstants.NSURI_SCHEMA_XSI.equals(au) && "type".equals(aln)) {
 				int idx = av.indexOf(':');
 				if (idx != -1) {
					String p = av.substring(0, idx);
					String q = av.substring(av.indexOf(':') + 1);
					//					int idx = aq.indexOf(':');
					//					String p = aq.substring(0, idx);
					//					String q = aq.substring(av.indexOf(':') + 1);
					String ns = getNS(p);
					String axisPrefix = context.getPrefixForURI(ns);
					av = axisPrefix + ':' + q;
 				}

 			}
 			fixed.addAttribute(au, aln, atts.getQName(i), atts.getType(i), av);
 		}
 
 		try {
 			context.startElement(new QName(uri, localName), fixed);
 		} catch (IOException ioe) {
 			throw new SAXException(ioe);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		head = prefixes.pop();
 		try {
 			context.endElement();
 		} catch (IOException ioe) {
 			throw new SAXException(ioe);
 		}
 	}
 
 	private String getNS(String prefix) {
 		String ns = null;
 		for (int i = prefixes.size() - 1; i >= 0 && ns == null; i--) {
 			ns = prefixes.get(i).get(prefix);
 		}
 		return ns;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		try {
 			context.writeChars(ch, start, length);
 		} catch (IOException ioe) {
 			throw new SAXException(ioe);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setDocumentLocator(Locator locator) {
 		//
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void startDocument() throws SAXException {
 		//
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void endDocument() throws SAXException {
 		//
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
 		//
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void processingInstruction(String target, String data) throws SAXException {
 		//
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void skippedEntity(String name) throws SAXException {
 		//
 	}
 
 }
