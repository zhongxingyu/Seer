 package org.esgf.filedownload;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.esgf.metadata.JSONArray;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 public class DocElement {
     
     private String dataset_id;
     private int count;
     private List<FileElement> fileElements;
     
     public DocElement() {
         this.dataset_id = new String("");
         this.fileElements = new ArrayList<FileElement>();
         FileElement blankElement = new FileElement();
         this.fileElements.add(blankElement);
        this.fileElements.add(blankElement);
         this.setCount(0);
     }
     
     
     public String getDataset_id() {
         return dataset_id;
     }
 
 
     public void setDataset_id(String dataset_id) {
         this.dataset_id = dataset_id;
     }
 
 
     public List<FileElement> getFileElements() {
         return fileElements;
     }
 
 
     public void setFileElements(List<FileElement> fileElements) {
         List<FileElement> newFileElements = new ArrayList<FileElement>();
         FileElement blankElement = new FileElement();
         newFileElements.add(blankElement);
        newFileElements.add(blankElement);
         newFileElements.addAll(fileElements);
         this.fileElements = newFileElements;
     }
 
     public String toString() {
         String str = "doc\n";
         
         str += "\tDataset_id: " + this.dataset_id + "\n";
         
         for(int i=0;i<fileElements.size();i++) {
             str += "\tFILE: " + i + " \n\t-----\n" + fileElements.get(i).toString() + "\n";
         }
         
         return str;
     }
     
     public Element toElement() {
         Element docEl = new Element("doc");
 
         Element datasetidEl = new Element("datasetId");
         datasetidEl.addContent(this.dataset_id);
         docEl.addContent(datasetidEl);
         
         Element countEl = new Element("count");
         countEl.addContent(Integer.toString(count));
         docEl.addContent(countEl);
         
         for(int i=0;i<this.fileElements.size();i++) {
             Element fileEl = this.fileElements.get(i).toElement();
             docEl.addContent(fileEl);
         }
         
         return docEl;
     }
     
     public String toXML() {
         String xml = "";
         
         Element docElement = this.toElement();
 
         XMLOutputter outputter = new XMLOutputter();
         xml = outputter.outputString(docElement);
         
         return xml;
     }
     
     public static void main(String [] args) {
         
         DocElement de = new DocElement();
         
         String dataset_id = "dataset_id1";
         de.setDataset_id(dataset_id);
         
         List<FileElement> listFe = new ArrayList<FileElement>();
         
         
         FileElement fe = new FileElement();
         
         ServicesElement se = new ServicesElement();
         String service = "service1";
         se.addService(service);
         service = "service2";
         se.addService(service);
         fe.setServicesElement(se);
         
         URLSElement ue = new URLSElement();
         String url = "url1";
         ue.addURL(url);
         url = "url2";
         ue.addURL(url);
         fe.setUrlsElement(ue);
 
         MIMESElement me = new MIMESElement();
         String mime = "mime1";
         me.addMIME(mime);
         mime = "mime2";
         me.addMIME(mime);
         fe.setMimesElement(me);
         
         String fileId = "fileId1";
         fe.setFileId(fileId);
         
         String title = "title1";
         fe.setTitle(title);
         
         String size = "size1";
         fe.setSize(size);
         
         String hasGrid = "true";
         fe.setHasGrid(hasGrid);
         
         String hasHttp = "true";
         fe.setHasHttp(hasHttp);
 
         listFe.add(fe);
         
         de.setFileElements(listFe);
         
         System.out.println(de);
         listFe.add(fe);
         System.out.println(de.toXML());
         
         
     }
 
 
     public void setCount(int count) {
         this.count = count;
     }
 
 
     public int getCount() {
         return count;
     }
     
 
 }
