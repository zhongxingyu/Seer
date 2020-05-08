 package org.giavacms.richcontent.repository;
 
 import java.math.BigInteger;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.inject.Named;
 import javax.persistence.Query;
 
 import org.giavacms.base.common.util.HtmlUtils;
 import org.giavacms.base.controller.util.TimeUtils;
 import org.giavacms.base.model.Page;
 import org.giavacms.base.model.attachment.Document;
 import org.giavacms.base.model.attachment.Image;
 import org.giavacms.base.repository.AbstractPageRepository;
 import org.giavacms.common.model.Search;
 import org.giavacms.common.util.StringUtils;
 import org.giavacms.richcontent.model.RichContent;
 import org.giavacms.richcontent.model.Tag;
 import org.giavacms.richcontent.model.type.RichContentType;
 
 @Named
 @Stateless
 @LocalBean
 public class RichContentRepository extends AbstractPageRepository<RichContent>
 {
 
    private static final long serialVersionUID = 1L;
 
    @Override
    protected RichContent prePersist(RichContent n)
    {
       n.setClone(true);
       if (n.getDate() == null)
          n.setDate(new Date());
       if (n.getRichContentType() != null
                && n.getRichContentType().getId() != null)
          n.setRichContentType(getEm().find(RichContentType.class,
                   n.getRichContentType().getId()));
       if (n.getDocuments() != null && n.getDocuments().size() == 0)
       {
          n.setDocuments(null);
       }
       if (n.getImages() != null && n.getImages().size() == 0)
       {
          n.setImages(null);
       }
       n.setDate(TimeUtils.adjustHourAndMinute(n.getDate()));
       n.setContent(HtmlUtils.normalizeHtml(n.getContent()));
       return super.prePersist(n);
    }
 
    @Override
    protected RichContent preUpdate(RichContent n)
    {
       n.setClone(true);
       if (n.getDate() == null)
          n.setDate(new Date());
       if (n.getRichContentType() != null
                && n.getRichContentType().getId() != null)
          n.setRichContentType(getEm().find(RichContentType.class,
                   n.getRichContentType().getId()));
       if (n.getDocuments() != null && n.getDocuments().size() == 0)
       {
          n.setDocuments(null);
       }
       if (n.getImages() != null && n.getImages().size() == 0)
       {
          n.setImages(null);
       }
       n.setDate(TimeUtils.adjustHourAndMinute(n.getDate()));
       n.setContent(HtmlUtils.normalizeHtml(n.getContent()));
       n = super.preUpdate(n);
       return n;
    }
 
    public RichContent findLast()
    {
       RichContent ret = new RichContent();
       try
       {
          ret = (RichContent) getEm()
                   .createQuery(
                            "select p from "
                                     + RichContent.class.getSimpleName()
                                     + " p order by p.date desc ")
                   .setMaxResults(1).getSingleResult();
          if (ret == null)
          {
             return null;
          }
          else
          {
             return this.fetch(ret.getId());
          }
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
       return ret;
    }
 
    public RichContent findHighlight()
    {
       RichContent ret = new RichContent();
       try
       {
          ret = (RichContent) getEm()
                   .createQuery(
                            "select p from "
                                     + RichContent.class.getSimpleName()
                                     + " p where p.highlight = :STATUS ")
                   .setParameter("STATUS", true).setMaxResults(1)
                   .getSingleResult();
          for (Document document : ret.getDocuments())
          {
             document.getName();
          }
 
          for (Image image : ret.getImages())
          {
             image.getName();
          }
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
       if (ret == null)
          return findLast();
       return ret;
    }
 
    @SuppressWarnings("unchecked")
    public void refreshEvidenza(String id)
    {
       List<RichContent> ret = null;
       try
       {
          ret = (List<RichContent>) getEm()
                   .createQuery(
                            "select p from "
                                     + RichContent.class.getSimpleName()
                                     + " p where p.id != :ID AND p.highlight = :STATUS")
                   .setParameter("ID", id).setParameter("STATUS", true)
                   .getResultList();
          if (ret != null)
          {
             for (RichContent richContent : ret)
             {
                richContent.setHighlight(false);
                update(richContent);
             }
          }
 
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
    }
 
    @SuppressWarnings("unchecked")
    public Image findHighlightImage()
    {
       try
       {
          List<RichContent> nl = getEm()
                   .createQuery(
                            "select p from "
                                     + RichContent.class.getSimpleName()
                                     + " p where p.highlight = :STATUS")
                   .setParameter("STATUS", true).getResultList();
          if (nl == null || nl.size() == 0 || nl.get(0).getImages() == null
                   || nl.get(0).getImages().size() == 0)
          {
             return null;
          }
          return nl.get(0).getImages().get(0);
 
       }
       catch (Exception e)
       {
          e.printStackTrace();
          return null;
       }
    }
 
    @Override
    protected String getDefaultOrderBy()
    {
       return "date desc";
    }
 
    @Override
    public int getListSize(Search<RichContent> search)
    {
       Map<String, Object> params = new HashMap<String, Object>();
       boolean count = true;
       StringBuffer string_query = getListNative(search, params, count);
       Query query = getEm().createNativeQuery(string_query.toString());
       for (String param : params.keySet())
       {
          query.setParameter(param, params.get(param));
       }
       return ((BigInteger) query.getSingleResult()).intValue();
    }
 
    @Override
    @SuppressWarnings("unchecked")
    public List<RichContent> getList(Search<RichContent> search, int startRow,
             int pageSize)
    {
       Map<String, Object> params = new HashMap<String, Object>();
       boolean count = false;
       StringBuffer stringbuffer_query = getListNative(search, params, count);
       String string_query = null;
       if (pageSize > 0)
       {
          string_query = _paginate(stringbuffer_query, startRow, pageSize);
       }
       else
       {
          string_query = stringbuffer_query.toString();
       }
       Query query = getEm().createNativeQuery(string_query);
       for (String param : params.keySet())
       {
          query.setParameter(param, params.get(param));
       }
 
       RichContent richContent = null;
       Map<String, Set<String>> imageNames = new HashMap<String, Set<String>>();
       Map<String, Set<String>> documentNames = new HashMap<String, Set<String>>();
       Map<String, RichContent> richContents = new HashMap<String, RichContent>();
 
       Iterator<Object[]> results = query.getResultList().iterator();
       while (results.hasNext())
       {
          if (richContent == null)
             richContent = new RichContent();
          Object[] row = results.next();
          int i = 0;
          String id = (String) row[i];
          if (id != null && !id.isEmpty())
             richContent.setId(id);
          i++;
          String title = (String) row[i];
          if (title != null && !title.isEmpty())
             richContent.setTitle(title);
          i++;
          String author = (String) row[i];
          if (author != null && !author.isEmpty())
             richContent.setAuthor(author);
          i++;
          String content = (String) row[i];
          if (content != null && !content.isEmpty())
             richContent.setContent(content);
          i++;
          Timestamp date = (Timestamp) row[i];
          if (date != null)
          {
             richContent.setDate(new Date(date.getTime()));
          }
          i++;
          if (row[i] != null && row[i] instanceof Short)
          {
             richContent.setHighlight(((Short) row[i]) > 0 ? true : false);
          }
          else if (row[i] != null && row[i] instanceof Boolean)
          {
             richContent.setHighlight(((Boolean) row[i]).booleanValue());
          }
          i++;
          String preview = (String) row[i];
          if (preview != null && !preview.isEmpty())
             richContent.setPreview(preview);
          i++;
          String tags = (String) row[i];
          if (tags != null && !tags.isEmpty())
             richContent.setTags(tags);
          i++;
          BigInteger richContentType_id = null;
          if (row[i] instanceof BigInteger)
          {
             richContentType_id = (BigInteger) row[i];
             richContent.getRichContentType().setId(richContentType_id.longValue());
          }
          i++;
          String richContentType = (String) row[i];
          if (richContentType != null && !richContentType.isEmpty())
             richContent.getRichContentType().setName(richContentType);
          i++;
          String imagefileName = (String) row[i];
          if (imagefileName != null && !imagefileName.isEmpty())
          {
             if (imageNames.containsKey(id))
             {
                HashSet<String> set = (HashSet<String>) imageNames.get(id);
                set.add(imagefileName);
             }
             else
             {
                HashSet<String> set = new HashSet<String>();
                set.add(imagefileName);
                imageNames.put(id, set);
             }
          }
          i++;
          String documentfileName = (String) row[i];
          if (documentfileName != null && !documentfileName.isEmpty())
          {
             if (documentNames.containsKey(id))
             {
                HashSet<String> set = (HashSet<String>) documentNames.get(id);
                set.add(documentfileName);
             }
             else
             {
                HashSet<String> set = new HashSet<String>();
                set.add(documentfileName);
                documentNames.put(id, set);
             }
          }
          if (!richContents.containsKey(id))
          {
             richContents.put(id, richContent);
          }
 
       }
       for (String id : documentNames.keySet())
       {
          RichContent rich = null;
          if (richContents.containsKey(id))
          {
             rich = richContents.get(id);
             Set<String> docs = documentNames.get(id);
             for (String docFileName : docs)
             {
                Document document = new Document();
                document.setFilename(docFileName);
                rich.addDocument(document);
             }
          }
          else
          {
             System.out.println("ERRORE - DOCS CYCLE non trovo id:" + id);
          }
 
       }
       for (String id : imageNames.keySet())
       {
          RichContent rich = null;
          if (richContents.containsKey(id))
          {
             rich = richContents.get(id);
             Set<String> docs = imageNames.get(id);
             for (String imgFileName : docs)
             {
                Image image = new Image();
                image.setFilename(imgFileName);
                rich.addImage(image);
             }
          }
          else
          {
             System.out.println("ERRORE IMGS CYCLE non trovo id:" + id);
          }
 
       }
       return new ArrayList<RichContent>(richContents.values());
    }
 
    private String _paginate(StringBuffer sb, int startRow, int pageSize)
    {
       if (pageSize > 0)
       {
          return sb.append(" limit ").append(startRow).append(", ").append(pageSize).toString();
       }
       else
       {
          return sb.toString();
       }
    }
 
    protected StringBuffer getListNative(Search<RichContent> search, Map<String, Object> params, boolean count)
    {
       String pageAlias = "P";
 
       StringBuffer sb = new StringBuffer(
                "SELECT ");
       if (count)
       {
          sb.append(" count( distinct ").append(pageAlias).append(".id ) ");
       }
       else
       {
          sb.append(pageAlias).append(".id, ").append(pageAlias).append(".title, ");
          sb.append(" R.author, R.content, R.date, R.highlight, R.preview, R.tags, R.richContentType_id, ");
          sb.append(" RT.name AS richContentType, ");
          sb.append(" I.fileName AS image, ");
          sb.append(" D.filename AS document ");
       }
       sb.append(" FROM ").append(RichContent.TABLE_NAME).append(" AS R ");
      sb.append(" LEFT JOIN ").append(RichContentType.TABLE_NAME).append(" AS RT ON ( RT.id = R.id ) ");
       sb.append(" LEFT JOIN ").append(Page.TABLE_NAME).append(" as ").append(pageAlias).append(" on (R.id = ")
                .append(pageAlias).append(".id ) ");
       if (!count)
       {
          sb.append(" LEFT JOIN RichContent_Document AS RD ON ( RD.RichContent_id = R.id ) ");
          sb.append(" LEFT JOIN Document AS D ON ( RD.documents_id = D.id ) ");
          sb.append(" LEFT JOIN RichContent_Image AS RI ON ( RI.RichContent_id = R.id ) ");
          sb.append(" LEFT JOIN Image as I on ( I.id = RI.images_id ) ");
       }
       String separator = " where ";
 
       // ACTIVE TYPE
       if (true)
       {
          sb.append(separator).append(" RT.active = :activeContentType ");
          params.put("activeContentType", true);
          separator = " and ";
       }
 
       // TYPE BY NAME
       if (search.getObj().getRichContentType() != null
                && search.getObj().getRichContentType().getName() != null
                && search.getObj().getRichContentType().getName().length() > 0)
       {
          sb.append(separator).append("RT.name = :NAMETYPE ");
          params.put("NAMETYPE", search.getObj().getRichContentType()
                   .getName());
          separator = " and ";
       }
 
       // TYPE BY ID
       if (search.getObj().getRichContentType() != null
                && search.getObj().getRichContentType().getId() != null)
       {
          sb.append(separator).append("RT.id = :IDTYPE ");
          params.put("IDTYPE", search.getObj().getRichContentType().getId());
          separator = " and ";
       }
 
       // TAG
       if (search.getObj().getTag() != null
                && search.getObj().getTag().trim().length() > 0)
       {
          // try
          // {
          // params.put("TAGNAME",
          // URLEncoder.encode(search.getObj().getTag().trim(), "UTF-8"));
          // sb.append(separator).append(alias).append(".id in ( ");
          // sb.append(" select distinct rt.richContent.id from ").append(Tag.class.getSimpleName())
          // .append(" rt where rt.tagName = :TAGNAME ");
          // sb.append(" ) ");
          // separator = " and ";
          // }
          // catch (UnsupportedEncodingException e)
          {
             String tagName = search.getObj().getTag().trim();
             String tagNameCleaned = StringUtils.clean(
                      search.getObj().getTag().trim()).replace('-', ' ');
             boolean likeMatch = false;
             if (!tagName.equals(tagNameCleaned))
             {
                likeMatch = true;
             }
             sb.append(separator).append("R.id in ( ");
             sb.append(" select distinct T1.richContent_id from ")
                      .append(Tag.TABLE_NAME)
                      .append(" T1 where T1.tagName ")
                      .append(likeMatch ? "like" : "=").append(" :TAGNAME ");
             sb.append(" ) ");
             params.put("TAGNAME", likeMatch ? likeParam(tagNameCleaned)
                      : tagName);
             separator = " and ";
          }
       }
 
       // TAG LIKE
       if (search.getObj().getTagList().size() > 0)
       {
          sb.append(separator).append(" ( ");
          for (int i = 0; i < search.getObj().getTagList().size(); i++)
          {
             sb.append(i > 0 ? " or " : "");
 
             // da provare quale versione piu' efficiente
             boolean usaJoin = false;
             if (usaJoin)
             {
                sb.append("R.id in ( ");
                sb.append(" select distinct T2.richContent_id from ")
                         .append(Tag.TABLE_NAME)
                         .append(" T2 where upper ( T2.tagName ) like :TAGNAME")
                         .append(i).append(" ");
                sb.append(" ) ");
             }
             else
             {
                sb.append(" upper ( ").append("R.tags ) like :TAGNAME").append(i)
                         .append(" ");
             }
 
             params.put("TAGNAME" + i, likeParam(search.getObj().getTag()
                      .trim().toUpperCase()));
          }
          separator = " and ";
       }
 
       // ACTIVE
       if (true)
       {
          sb.append(separator).append(pageAlias).append(".active = :active ");
          params.put("active", true);
          separator = " and ";
       }
 
       // BASE PAGE
       if (search.getObj().getTemplate() != null && search.getObj().getTemplate().getId() != null)
       {
          sb.append(separator).append(pageAlias).append(".template_id = :BASEPAGE_TEMPLATE_ID ");
          params.put("BASEPAGE_TEMPLATE_ID", search.getObj().getTemplate().getId());
          separator = " and ";
       }
 
       // TITLE
       if (search.getObj().getTitle() != null
                && !search.getObj().getTitle().trim().isEmpty())
       {
          boolean likeSearch = likeSearch(likeParam(search.getObj().getTitle().trim().toUpperCase()), pageAlias,
                   separator,
                   sb, params);
          if (likeSearch)
          {
             separator = " and ";
          }
       }
 
       // LINGUA
       if (search.getObj().getLang() > 0)
       {
          if (search.getObj().getLang() == 1)
          {
             sb.append(separator).append(pageAlias).append(".id = ")
                      .append(pageAlias).append(".lang1id ");
          }
          else if (search.getObj().getLang() == 2)
          {
             sb.append(separator).append(pageAlias).append(".id = ")
                      .append(pageAlias).append(".lang2id ");
          }
          else if (search.getObj().getLang() == 3)
          {
             sb.append(separator).append(pageAlias).append(".id = ")
                      .append(pageAlias).append(".lang3id ");
          }
          else if (search.getObj().getLang() == 4)
          {
             sb.append(separator).append(pageAlias).append(".id = ")
                      .append(pageAlias).append(".lang4id ");
          }
          else if (search.getObj().getLang() == 5)
          {
             sb.append(separator).append(pageAlias).append(".id = ")
                      .append(pageAlias).append(".lang5id ");
          }
       }
 
       return sb;
    }
 
    protected boolean likeSearchNative(String likeText, String pageAlias, String richContentAlias,
             String separator, StringBuffer sb, Map<String, Object> params)
    {
       sb.append(separator).append(" ( ");
 
       sb.append(" upper ( ").append(pageAlias).append(".title ) LIKE :LIKETEXT ");
       sb.append(" or ").append(" upper ( ").append(richContentAlias)
                .append(".content ) LIKE :LIKETEXT ");
 
       sb.append(" ) ");
 
       // params.put("LIKETEXT", StringUtils.clean(likeText));
       params.put("LIKETEXT", likeText);
 
       return true;
    }
 
    public void fetchList(Object key)
    {
       RichContent richContent = null;
       Map<String, Set<String>> imageNames = new HashMap<String, Set<String>>();
       Map<String, Set<String>> documentNames = new HashMap<String, Set<String>>();
       Map<String, RichContent> richcontents = new HashMap<String, RichContent>();
 
       String nativeQuery = "SELECT  P.id, P.title, R.author, R.content, R.date, R.highlight, R.preview, R.tags, R.richContentType_id, RT.name as richContentType,"
                + "I.fileName as image, "
                + "D.filename as document "
                + "FROM `RichContent` as R "
               + "left join RichContentType as RT on (RT.id=R.id) "
                + "left join Page as P on (R.id=P.id) "
                + "left join RichContent_Document as RD on (RD.RichContent_id=R.id) "
                + "left join Document as D on (RD.documents_id=D.id) "
                + "left join RichContent_Image as RI on (RI.RichContent_id=R.id) "
                + "left join Image as I on (I.id=RI.images_id) "
                + "where R.id= :ID";
       @SuppressWarnings("unchecked")
       Iterator<Object[]> results = getEm()
                .createNativeQuery(nativeQuery).setParameter("ID", key).getResultList().iterator();
       while (results.hasNext())
       {
          if (richContent == null)
             richContent = new RichContent();
          Object[] row = results.next();
          int i = 0;
          String id = (String) row[i];
          if (id != null && !id.isEmpty())
             richContent.setId(id);
          i++;
          String title = (String) row[i];
          if (title != null && !title.isEmpty())
             richContent.setTitle(title);
          i++;
          String author = (String) row[i];
          if (author != null && !author.isEmpty())
             richContent.setAuthor(author);
          i++;
          String content = (String) row[i];
          if (content != null && !content.isEmpty())
             richContent.setContent(content);
          i++;
          Timestamp date = (Timestamp) row[i];
          if (date != null)
          {
             richContent.setDate(new Date(date.getTime()));
          }
          i++;
          if (row[i] != null && row[i] instanceof Short)
          {
             richContent.setHighlight(((Short) row[i]) > 0 ? true : false);
          }
          else if (row[i] != null && row[i] instanceof Boolean)
          {
             richContent.setHighlight(((Boolean) row[i]).booleanValue());
          }
          i++;
          String preview = (String) row[i];
          if (preview != null && !preview.isEmpty())
             richContent.setPreview(preview);
          i++;
          String tags = (String) row[i];
          if (tags != null && !tags.isEmpty())
             richContent.setTags(tags);
          i++;
          BigInteger richContentType_id = null;
          if (row[i] instanceof BigInteger)
          {
             richContentType_id = (BigInteger) row[i];
             richContent.getRichContentType().setId(richContentType_id.longValue());
          }
          i++;
          String richContentType = (String) row[i];
          if (richContentType != null && !richContentType.isEmpty())
             richContent.getRichContentType().setName(richContentType);
          i++;
          String imagefileName = (String) row[i];
          if (imagefileName != null && !imagefileName.isEmpty())
          {
             if (imageNames.containsKey(id))
             {
                HashSet<String> set = (HashSet<String>) documentNames.get(id);
                set.add(imagefileName);
             }
             else
             {
                HashSet<String> set = new HashSet<String>();
                set.add(imagefileName);
                imageNames.put(id, set);
             }
          }
          i++;
          String documentfileName = (String) row[i];
          if (documentfileName != null && !documentfileName.isEmpty())
          {
             if (documentNames.containsKey(id))
             {
                HashSet<String> set = (HashSet<String>) documentNames.get(id);
                set.add(documentfileName);
             }
             else
             {
                HashSet<String> set = new HashSet<String>();
                set.add(documentfileName);
                documentNames.put(id, set);
             }
          }
          if (!richcontents.containsKey(id))
          {
             richcontents.put(id, richContent);
          }
 
       }
       for (String id : documentNames.keySet())
       {
          RichContent rich = null;
          if (richcontents.containsKey(id))
          {
             rich = richcontents.get(id);
             Set<String> docs = documentNames.get(id);
             for (String docFileName : docs)
             {
                Document document = new Document();
                document.setFilename(docFileName);
                rich.addDocument(document);
             }
          }
          else
          {
             System.out.println("ERRORE - DOCS CYCLE non trovo id:" + id);
          }
 
       }
       for (String id : imageNames.keySet())
       {
          RichContent rich = null;
          if (richcontents.containsKey(id))
          {
             rich = richcontents.get(id);
             Set<String> docs = imageNames.get(id);
             for (String imgFileName : docs)
             {
                Image image = new Image();
                image.setFilename(imgFileName);
                rich.addImage(image);
             }
          }
          else
          {
             System.out.println("ERRORE IMGS CYCLE non trovo id:" + id);
          }
 
       }
       return;
    }
 
    @Override
    public RichContent fetch(Object key)
    {
       RichContent richContent = null;
       Set<String> imageNames = new HashSet<String>();
       Set<String> documentNames = new HashSet<String>();
 
       String nativeQuery = "SELECT  P.id, P.title, R.author, R.content, R.date, R.highlight, R.preview, R.tags, R.richContentType_id, RT.name as richContentType,"
                + "I.fileName as image, "
                + "D.filename as document "
                + "FROM `RichContent` as R "
                + "left join RichContentType as RT on (RT.id=R.id) "
                + "left join Page as P on (R.id=P.id) "
                + "left join RichContent_Document as RD on (RD.RichContent_id=R.id) "
                + "left join Document as D on (RD.documents_id=D.id) "
                + "left join RichContent_Image as RI on (RI.RichContent_id=R.id) "
                + "left join Image as I on (I.id=RI.images_id) "
                + "where R.id= :ID";
       @SuppressWarnings("unchecked")
       Iterator<Object[]> results = getEm()
                .createNativeQuery(nativeQuery).setParameter("ID", key).getResultList().iterator();
       while (results.hasNext())
       {
          if (richContent == null)
             richContent = new RichContent();
          Object[] row = results.next();
          int i = 0;
          String id = (String) row[i];
          if (id != null && !id.isEmpty())
             richContent.setId(id);
          i++;
          String title = (String) row[i];
          if (title != null && !title.isEmpty())
             richContent.setTitle(title);
          i++;
          String author = (String) row[i];
          if (author != null && !author.isEmpty())
             richContent.setAuthor(author);
          i++;
          String content = (String) row[i];
          if (content != null && !content.isEmpty())
             richContent.setContent(content);
          i++;
          Timestamp date = (Timestamp) row[i];
          if (date != null)
          {
             richContent.setDate(new Date(date.getTime()));
          }
          i++;
          if (row[i] != null && row[i] instanceof Short)
          {
             richContent.setHighlight(((Short) row[i]) > 0 ? true : false);
          }
          else if (row[i] != null && row[i] instanceof Boolean)
          {
             richContent.setHighlight(((Boolean) row[i]).booleanValue());
          }
          i++;
          String preview = (String) row[i];
          if (preview != null && !preview.isEmpty())
             richContent.setPreview(preview);
          i++;
          String tags = (String) row[i];
          if (tags != null && !tags.isEmpty())
             richContent.setTags(tags);
          i++;
          BigInteger richContentType_id = null;
          if (row[i] instanceof BigInteger)
          {
             richContentType_id = (BigInteger) row[i];
             richContent.getRichContentType().setId(richContentType_id.longValue());
          }
          i++;
          String richContentType = (String) row[i];
          if (richContentType != null && !richContentType.isEmpty())
             richContent.getRichContentType().setName(richContentType);
          i++;
          String imagefileName = (String) row[i];
          if (imagefileName != null && !imagefileName.isEmpty())
          {
             imageNames.add(imagefileName);
          }
          i++;
          String documentfileName = (String) row[i];
          if (documentfileName != null && !documentfileName.isEmpty())
          {
             documentNames.add(documentfileName);
          }
 
       }
       for (String doc : documentNames)
       {
          Document document = new Document();
          document.setFilename(doc);
          richContent.addDocument(document);
       }
       for (String img : imageNames)
       {
          Image image = new Image();
          image.setFilename(img);
          richContent.addImage(image);
       }
       return richContent;
    }
 
    @Deprecated
    public RichContent oldFetch(Object key)
    {
       try
       {
          RichContent richContent = find(key);
          for (Document document : richContent.getDocuments())
          {
             document.getName();
          }
          for (Image image : richContent.getImages())
          {
             image.getName();
          }
          return richContent;
       }
       catch (Exception e)
       {
          logger.error(e.getMessage(), e);
          return null;
       }
    }
 
    public RichContent getLast(String category)
    {
       Search<RichContent> r = new Search<RichContent>(RichContent.class);
       r.getObj().getRichContentType().setName(category);
       List<RichContent> list = getList(r, 0, 1);
       if (list != null && list.size() > 0)
       {
          RichContent ret = list.get(0);
          for (Document document : ret.getDocuments())
          {
             document.getName();
          }
 
          for (Image image : ret.getImages())
          {
             image.getName();
          }
          return ret;
       }
       return new RichContent();
    }
 
    @Deprecated
    public List<RichContent> oldGetList(Search<RichContent> ricerca, int startRow,
             int pageSize)
    {
       // TODO Auto-generated method stub
       List<RichContent> list = super.getList(ricerca, startRow, pageSize);
       for (RichContent richContent : list)
       {
          if (richContent.getImages() != null)
          {
             for (Image img : richContent.getImages())
             {
                img.getId();
                img.getFilename();
                img.getFilePath();
             }
          }
          if (richContent.getDocuments() != null)
          {
             for (Document doc : richContent.getDocuments())
             {
                doc.getId();
                doc.getFilename();
                doc.getType();
             }
          }
       }
       return list;
    }
 
 }
