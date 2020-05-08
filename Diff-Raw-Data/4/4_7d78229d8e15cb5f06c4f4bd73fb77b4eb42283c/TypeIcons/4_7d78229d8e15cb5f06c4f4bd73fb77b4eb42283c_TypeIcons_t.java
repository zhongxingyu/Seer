 package ch.zhaw.simulation.diagram;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 import ch.zhaw.simulation.editor.elements.GuiImage;
 import ch.zhaw.simulation.editor.elements.global.GlobalImage;
 import ch.zhaw.simulation.editor.flow.connector.flowarrow.FlowArrowImage;
 import ch.zhaw.simulation.editor.flow.elements.container.ContainerImage;
 import ch.zhaw.simulation.editor.flow.elements.density.DensityContainerImage;
 import ch.zhaw.simulation.editor.flow.elements.parameter.ParameterImage;
 import ch.zhaw.simulation.icon.IconLoader;
 import ch.zhaw.simulation.plugin.data.SimulationSerie.SerieSource;
 import ch.zhaw.simulation.sysintegration.GuiConfig;
 import ch.zhaw.simulation.sysintegration.Sysintegration;
 
 public class TypeIcons {
 	public final Icon ICON_TIME;
 	public final ImageIcon ICON_GLOBAL;
 	public final ImageIcon ICON_CONTAINER;
 	public final ImageIcon ICON_PARAMETER;
 	public final ImageIcon ICON_ARROW;
 	public final ImageIcon ICON_DENSITY;
 
 	public TypeIcons(Sysintegration sys) {
 		GuiConfig config = sys.getGuiConfig();
 
 		ICON_TIME = IconLoader.getIcon("type/time", 22);
 		ICON_GLOBAL = new ImageIcon(GuiImage.drawToImage(new GlobalImage(22, config)));
 		ICON_CONTAINER = new ImageIcon(GuiImage.drawToImage(new ContainerImage(22, 27, config)));
 		ICON_PARAMETER = new ImageIcon(GuiImage.drawToImage(new ParameterImage(22, config)));
 		ICON_ARROW = new ImageIcon(GuiImage.drawToImage(new FlowArrowImage(22, config)));
 		ICON_DENSITY = new ImageIcon(GuiImage.drawToImage(new DensityContainerImage(22, 27, config)));
 	}
 
 	public Icon iconFor(SerieSource type) {
		if (type == null) {
			return null;
		}

 		switch (type) {
 		case GLOBAL:
 			return ICON_GLOBAL;
 		case CONTAINER:
 			return ICON_CONTAINER;
 		case PARAMETER:
 			return ICON_PARAMETER;
 		case FLOW:
 			return ICON_ARROW;
 		case DENSITY_CONTAINER:
 			return ICON_DENSITY;
 
 		default:
 			return null;
 		}
 	}
 }
