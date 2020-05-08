 package org.idiginfo.docsvc.model.apisvc;
 
 import java.util.Date;
 
 public interface Annotation {
 
     public String getPageurl();
 
     public void setPageurl(String pageurl);
 
     public String getType();
 
     public void setType(String type);
 
     public String getPagetitle();
 
     public void setPagetitle(String pagetitle);
 
     public String getContext();
 
     public void setContext(String context);
 
     public String getSubject();
 
     public void setSubject(String subject);
 
     public String getNotetext();
 
     public void setNotetext(String notetext);
 
     public String getAuthor();
 
     public void setAuthor(String author);
 
     public String getSigned();
 
     public void setSigned(String signed);
 
     public String getDateString();
 
     public void setDate(String date);
 
     public Date getDate();
 
     public String getTags();
 
     public void setTags(String tags);
 
     public String getMatch();
 
     public void setMatch(String match);
 
     public String getNum();
 
     public void setNum(String num);
 
     public String getGid();
 
     public void setGid(String gid);
 
     public String getColor();
 
     public void setColor(String color);
 
     public String getMark();
 
     public void setMark(String mark);
 
     public String getState();
 
     public void setState(String state);
 
     public String getFinalTags();
 
     public void setFinalTags(String finalTags);
 
    public Annotation[] getReplies();
 
    public void setReplies(Annotation[] replies);
 
     public String getFullPageUrl();
 
     public Integer getPagenum();
 
 }
