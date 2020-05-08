 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.computer.knauss.reqtDiscussion.Util;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
import org.computer.knauss.reqtDiscussion.model.IClassificationFilter;
 import org.computer.knauss.reqtDiscussion.model.metric.PatternMetric;
 import org.computer.knauss.reqtDiscussion.ui.DiscussionVisualizationPanel;
 
 public class ExportAllVisualizations extends AbstractDiscussionIterationCommand {
 
 	private static final String DIR_NAME = "all-visualizations/";
 	private static final long serialVersionUID = 1L;
 	private DiscussionVisualizationPanel discussionVisualization;
 	private PatternMetric patternMetric;
 
 	public ExportAllVisualizations(
 			DiscussionVisualizationPanel discussionVisualization) {
 		super("Export all visualizations");
 		this.discussionVisualization = discussionVisualization;
 	}
 
 	@Override
 	protected void processDiscussionHook(Discussion[] discussions) {
 		Util.sortByID(discussions);
 
 		// First, we need to know what kind of pattern we have.
 		getPatternMetric().initDiscussions(discussions);
 		String pattern = getPatternMetric().decode(
 				getPatternMetric().considerDiscussions(discussions));
 
 		// Create the image
 		this.discussionVisualization.setDiscussions(discussions);
 		BufferedImage bi = new BufferedImage(
 				this.discussionVisualization.getVisualizationDimension().width,
 				this.discussionVisualization.getVisualizationDimension().height,
 				BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = bi.createGraphics();
 		this.discussionVisualization.paint(g2);
 
 		// Store it
 		String filename = DIR_NAME + pattern + "/";
 		if (discussions != null && discussions.length > 0)
 			for (Discussion wi : discussions)
 				filename += wi.getID() + "-";
 		else
 			filename = "empty-";

		String ratername = IClassificationFilter.NAME_FILTER.getName()
				.replaceAll(",", "-");

		File f = new File(filename + ratername + ".png");
 		f.mkdirs();
 		try {
 			ImageIO.write(bi, "png", f);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private PatternMetric getPatternMetric() {
 		if (this.patternMetric == null)
 			this.patternMetric = new PatternMetric();
 		return this.patternMetric;
 	}
 
 }
