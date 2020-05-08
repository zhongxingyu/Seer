 package hudson.plugins.crap4j;
 
 import hudson.XmlFile;
 import hudson.model.AbstractBuild;
 import hudson.model.ModelObject;
 import hudson.plugins.crap4j.display.DecreasingCrapLoadComparator;
 import hudson.plugins.crap4j.model.ICrapMethodPresentation;
 import hudson.plugins.crap4j.model.IMethodCrap;
 import hudson.plugins.crap4j.model.ProjectCrapBean;
 import hudson.util.XStream2;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import com.thoughtworks.xstream.XStream;
 
 public class CrapBuildResult implements ModelObject, ICrapMethodPresentation {
 	
 	private transient WeakReference<ProjectCrapBean> crap; 
 	private AbstractBuild<?, ?> owner;
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(CrapBuildResult.class.getName());
     /** Serialization provider. */
     private static final XStream XSTREAM = new XStream2();
     static {
         XSTREAM.alias("crap", ProjectCrapBean.class);
     }
 
 	public CrapBuildResult(AbstractBuild<?, ?> owner,
 			ProjectCrapBean crap) {
 		super();
 		this.owner = owner;
 		this.crap = new WeakReference<ProjectCrapBean>(crap);
         try {
             getDataFile().write(crap);
         } catch (IOException e) {
             LOGGER.log(Level.WARNING, "Failed to serialize the crap4j result.", e);
         }
 	}
 	
 	public void setOwner(AbstractBuild<?, ?> owner) {
 		this.owner = owner;
 	}
 	
 	public AbstractBuild<?, ?> getOwner() {
 		return this.owner;
 	}
 	
 	public ProjectCrapBean getResultData() {
 		if (null == this.crap) {
 			loadCrap();
 		}
 		ProjectCrapBean result = this.crap.get();
 		if (null == result) {
 			loadCrap();
 		}
 		return this.crap.get();
 	}
 	
 	private void loadCrap() {
 		try {
 			this.crap = new WeakReference<ProjectCrapBean>((ProjectCrapBean) getDataFile().read());
 		} catch (IOException e) {
             LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), e);
 		}
 	}
 	
 	public String getSummary() {
 		return buildSummary();
 	}
 	
 	public String getDetails() {
 		return buildDetails();
 	}
 	
 	//@Override
 	public String getDisplayName() {
 		return "Crap Report";
 	}
 	
 	//@Override
 	public String getTitle() {
		return "All Crappy Methods for <a href=\"/" + getOwner().getUrl() + "\">" + getOwner().getDisplayName() + "</a>";
 	}
 	
 	//@Override
 	public Collection<IMethodCrap> getMethods() {
 		List<IMethodCrap> result = new ArrayList<IMethodCrap>();
 		Collections.addAll(result, getResultData().getCrapMethods());
 		Collections.sort(result, new DecreasingCrapLoadComparator());
 		return result;
 	}
 
 	private String buildListEntry(String url, int count, String denotation) {
 		StringBuilder result = new StringBuilder();
 		result.append("<li><a href=\"");
 		result.append(url);
 		result.append("\">");
 		result.append(count);
 		result.append(" ");
 		result.append(denotation);
 		result.append("</a></li>");
 		return result.toString();
 	}
 	
 	public boolean hasNewCrappyMethods() {
 		return (getResultData().getNewCrapMethodsCount() > 0);
 	}
 	
 	public boolean hasFixedCrappyMethods() {
 		return (getResultData().getFixedCrapMethodsCount() > 0);
 	}
 	
 	public boolean hasChangesAtCrappyMethods() {
 		return (hasNewCrappyMethods() || hasFixedCrappyMethods());
 	}
 	
 	private String buildDetails() {
 		StringBuilder result = new StringBuilder();
 		if (hasNewCrappyMethods()) {
 			result.append(buildListEntry("crapResult/new",
 					getResultData().getNewCrapMethodsCount(),
 					"new crap methods"));
 		}
 		if (hasFixedCrappyMethods()) {
 			result.append(buildListEntry("crapResult/fixed",
 					getResultData().getFixedCrapMethodsCount(),
 					"fewer crap methods"));
 		}
 		return result.toString();
 	}
 	
 	private String buildSummary() {
         StringBuilder result = new StringBuilder();
         result.append("Crap4J: ");
         int crapMethods = getResultData().getCrapMethodCount();
         if (0 == crapMethods) {
         	result.append("No crappy methods in this project.");
         } else {
         	result.append("<a href=\"crapResult\">");
         	result.append(crapMethods);
         	result.append(" crappy methods (");
         	result.append(getResultData().getCrapMethodPercent());
         	result.append("%)</a> out of ");
         	result.append(getResultData().getMethodCount());
         	result.append(" methods in this project.");
         }
         return result.toString();
 	}
 	
 	public ICrapMethodPresentation getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
         if ("new".equals(link)) {
         	return new NewCrapMethodsResult(getOwner(),
         			getResultData().getNewMethods(getPreviousCrap()));
         }
         if ("fixed".equals(link)) {
         	return new FixedCrapMethodsResult(getOwner(),
         			getResultData().getFixedMethods(getPreviousCrap()));
         }
         return this;
     }
 	
 	private XmlFile getDataFile() {
 		return new XmlFile(new File(getOwner().getRootDir(), "crap.xml"));
 	}
 	
 	private ProjectCrapBean getPreviousCrap() {
 		CrapBuildResult previousResult = getPrevious();
 		if (null == previousResult) {
 			return null;
 		}
 		return previousResult.getResultData();
 	}
 	
 	public CrapBuildResult getPrevious() {
 		return getPrevious(getOwner());
 	}
 	
 	public static CrapBuildResult getPrevious(AbstractBuild<?, ?> currentBuild) {
 		AbstractBuild<?,?> previous = currentBuild.getPreviousBuild();
 		while (null != previous) {
 			Crap4JBuildAction action = previous.getAction(Crap4JBuildAction.class);
 			if (null != action) {
 				return action.getResult();
 			}
 			previous = previous.getPreviousBuild();
 		}
 		return null;
 	}
 }
