 package org.giavacms.richcontent.controller.request;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Random;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.giavacms.common.annotation.OwnRepository;
 import org.giavacms.richcontent.model.RichContent;
 import org.giavacms.richcontent.repository.RichContentRepository;
 
 @Named
 @RequestScoped
 public class RichContentRandomRequestController extends
          RichContentRequestController implements Serializable
 {
 
    private static final long serialVersionUID = 1L;
 
    Random random = new Random();
   
    @Inject
    @OwnRepository(RichContentRepository.class)
    RichContentRepository richContentRepository;
 
    @Override
    public List<RichContent> loadPage(int startRow, int pageSize)
    {
       int size = richContentRepository.getListSize(getSearch());
       startRow = random.nextInt(size);
       int maxIterations = 10;
       while ((maxIterations > 0) && ((size - startRow) < pageSize))
       {
          startRow = random.nextInt(size);
          maxIterations--;
       }
       return super.loadPage(startRow, pageSize);
    }
 }
