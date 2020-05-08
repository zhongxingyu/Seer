 package pl.kwi.components.testComponent;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.sling.api.SlingHttpServletRequest;
 import org.apache.sling.api.resource.Resource;
 
 import com.cognifide.cq.api.Dao;
 import com.cognifide.cq.api.ModelObject;
 import com.cognifide.cq.model.dao.GenericSlingDao;
 import com.cognifide.cq.presenter.AbstractPresenter;
 
 public class TestComponentPresenter extends AbstractPresenter{
 	
 	private final Dao<TestComponentDto> dao = new GenericSlingDao<TestComponentDto>(TestComponentDto.class);
 
 	@Override
 	public ModelObject getModelFromResource(SlingHttpServletRequest request,
 			Resource resource) {
 		
 		TestComponentDto dto = dao.getDtoOrNewOnError(resource);
 		
		if(StringUtils.isEmpty(dto.getTestComponentText())){
			dto.setTestComponentText("Put some text here!!!");			
		}
				
 		return new TestComponentModel<TestComponentDto>(dto);
 	}
 
 }
