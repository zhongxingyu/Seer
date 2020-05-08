 /*
  * The MIT License
  *
  * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package cz.jirutka.atom.jaxb;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementRef;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 
 import static cz.jirutka.atom.jaxb.Namespaces.ATOM_NS;
 
 /**
  * Elements common for both {@linkplain Feed Atom Feed} and
  * {@linkplain Entry Atom Entry}.
  *
  * <p><b>This class does not contain all elements defined in RF 4287, but only
  * some subset that is relevant for our purpose!</b></p>
  *
  * @see <a href="http://tools.ietf.org/html/rfc4287#page-24">RFC 4287, Page 24</a>
  * @author Jakub Jirutka <jakub@jirutka.cz>
  */
 @XmlType(name = "atomSource")
 public abstract class AtomSource extends CommonAttributes {
 
     /**
      * The "atom:author" element is a Person construct that indicates the
      * author of the entry or feed.
      *
      * @see http://tools.ietf.org/html/rfc4287#section-4.2.1
      */
     @XmlElement(name = "author", namespace = ATOM_NS)
     @XmlSchemaType(name = "atomAuthor")
    private List<AtomPerson> authors = new ArrayList<AtomPerson>();
 
     /**
      * The "atom:id" element conveys a permanent, universally unique identifier
      * for an entry or feed.
      *
      * @see http://tools.ietf.org/html/rfc4287#section-4.2.6
      */
     @XmlElement(namespace = ATOM_NS)
     @XmlSchemaType(name = "atomId")
     private String id;
 
     /**
      * The "atom:link" element defines a reference from an entry or feed to a 
      * Web resource.  This specification assigns no meaning to the content (if 
      * any) of this element.
      * 
      * @see http://tools.ietf.org/html/rfc4287#section-4.2.7
      */
     @XmlElementRef
     private List<AtomLink> links = new ArrayList<AtomLink>();
 
     /**
      * The "atom:title" element is a Text construct that conveys a
      * human-readable title for an entry or feed.
      *
      * @see http://tools.ietf.org/html/rfc4287#section-4.2.7.5
      */
     @XmlElement(namespace = ATOM_NS)
     @XmlSchemaType(name = "atomTitle")
     private String title;
 
     /**
      * The "atom:updated" element is a Date construct indicating the most recent
      * instant in time when an entry or feed was modified in a way the publisher
      * considers significant.</p>
      *
      * @see http://tools.ietf.org/html/rfc4287#section-4.2.15
      */
     @XmlElement(namespace = ATOM_NS)
     @XmlSchemaType(name = "atomUpdated")
     private Date updated;
 
 
 
     /**
      * Returns absolute URI of feed or entry from {@linkplain #getBase() Base URI}
      * and Atom Link with rel 'self', or null if there's not any self Link.
      *
      * @return URI
      */
     public URI getSelfURI() {
         URI uri = null;
 
         for (AtomLink link : links) {
             if (AtomLink.SELF.equals(link.getRel())) {
                 uri = link.getHref();
             }
         }
         if (uri != null && !uri.isAbsolute()) {
             uri = getBase() != null ? getBase().resolve(uri) : uri;
         }
         return uri;
     }
 
     /** @param author {@link #authors} */
     public void addAuthor(AtomPerson author) {
         this.authors.add(author);
     }
 
     /** @param link {@link #links} */
     public void addLink(AtomLink link) {
         links.add(link);
     }
 
 
     ////////  Accessors  ////////
 
     /** @return {@link #authors} */
    public List<AtomPerson> getAuthors() { return authors; }
 
     /** @param authors {@link #authors} */
    public void setAuthors(List<AtomPerson> authors) { this.authors = authors; }
 
     /** @return {@link #id} */
     public String getId() { return id; }
 
     /** @param id {@link #id} */
     public void setId(String id) { this.id = id; }
 
     /** @return {@link #links} */
     public List<AtomLink> getLinks() { return links; }
 
     /** @param links {@link #links} */
     public void setLinks(List<AtomLink> links) { this.links = links; }
 
     /** @return {@link #title} */
     public String getTitle() { return title; }
 
     /** @param title {@link #title} */
     public void setTitle(String title) { this.title = title; }
 
     /** @return {@link #updated} */
     public Date getUpdated() { return updated; }
 
     /** @param updated {@link #updated} */
     public void setUpdated(Date updated) { this.updated = updated; }
 
 }
