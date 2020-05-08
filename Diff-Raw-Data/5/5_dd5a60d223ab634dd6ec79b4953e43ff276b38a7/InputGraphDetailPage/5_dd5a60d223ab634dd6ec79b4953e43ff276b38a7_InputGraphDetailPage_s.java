 package cz.cuni.mff.odcleanstore.webfrontend.pages.engine;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.basic.Label;
 
 import cz.cuni.mff.odcleanstore.model.EnumGraphState;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.en.InputGraph;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.BooleanLabel;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.StateLabel;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.TemporaryFileResourceLink;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.TimestampLabel;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.en.InputGraphDao;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 
 public class InputGraphDetailPage extends FrontendPage {
 
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger logger = Logger.getLogger(InputGraphDetailPage.class);
 	
 	private InputGraphDao inputGraphDao;
 
 	public InputGraphDetailPage(Integer graphId) {
 		super
 		(
 			"Home > Backend > Engine > Graphs > Detail", 
 			"Graph Detail"
 		);
 		
 		inputGraphDao = daoLookupFactory.getDao(InputGraphDao.class);
 		
 		addDetail(graphId);
 	}
 
 	private void addDetail (final Integer graphId) {
 		setDefaultModel(createModelForOverview(inputGraphDao, graphId));
 		
 		add(new Label("UUID"));
 		add(new Label("pipelineLabel"));
 		add(new StateLabel("stateLabel"));
 		add(new BooleanLabel("isInCleanDB"));
 		add(new TimestampLabel("updated"));
 		add(createDumpDownloadLink("dump", graphId));
 	}
 	
 	private Component createDumpDownloadLink(String componentId, final Integer graphId) {
 		TemporaryFileResourceLink.ITempFileCreator fileCreator = new TemporaryFileResourceLink.ITempFileCreator()
 		{
 			private static final long serialVersionUID = 1L;
 
 			public File createTempFile()
 			{
 				try {
 		            return inputGraphDao.getContentFile(graphId);
 		        } catch (Exception e) {
 		            getSession().error("Cannot dump graph");
 		            logger.error(e.getMessage());
 		        }		
 				return null;
 			}
 			
 			public String getFileName()
 			{
 				return String.format("dump-%d.ttl", graphId);
 			}
 		};
 		
 		TemporaryFileResourceLink<InputGraph> downloadLink = new TemporaryFileResourceLink<InputGraph>(componentId, "text/turtle", fileCreator) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isVisible()
 			{
 				InputGraph inputGraph = (InputGraph) InputGraphDetailPage.this.getDefaultModelObject();
 				return inputGraph.getStateLabel().equals(EnumGraphState.FINISHED.name());
 			}
 		};		
 		return downloadLink;
 	}
 }
