 package editor.window;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import engine.Engine;
 import engine.entity.Entity;
 import engine.window.components.Tree;
 import engine.window.components.Window;
 import engine.window.tree.Model;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.DialogLayout.Group;
 
 public class EntityListMenu extends Window implements ActionListener {
 	private final Tree entitylist_window;
 	private final DialogLayout layout;
 	private Engine engine;
 	private TreeDragNodeEntity tsm;
 	//private ArrayList<ActionListener> action_listeners;
 
 	public EntityListMenu(Engine engine) {
 		super();
 		entitylist_window = new Tree();
 		tsm = new TreeDragNodeEntity(entitylist_window.getTable());
 		entitylist_window.setTreeSelectionManager(tsm);
 		layout = new DialogLayout();
 		this.engine = engine;
 		resourceMenuInit();
 		engine.getEntityList().addActionListener(this);
 	}
 
 	public EntityListMenu(Model m, Engine engine) {
 		super();
 		entitylist_window = new Tree(m);
 		layout = new DialogLayout();
 		this.engine = engine;
 		resourceMenuInit();
 	}
 
 	private void resourceMenuInit() {
		setTitle("Resource Editor");
 
 		Group hgroup = layout.createSequentialGroup().addGroup(
 			layout.createParallelGroup(entitylist_window));
 
 		Group vgroup = layout.createSequentialGroup().addGap()
 			.addWidget(entitylist_window).addGap();
 
 		layout.setHorizontalGroup(hgroup);
 		layout.setVerticalGroup(vgroup);
 
 		// textree.setSize(getWidth()/3, getHeight()/3);
 
 		add(layout);
 		
 		// update tree contents after it has been added.
 		//resource_window.createFromProjectResources();
 		createEntityList();
 	}
 	
 	public void createEntityList() {
 		for(Entity e : engine.getEntityList().getEntities()) {
 			if(!entitylist_window.contains((String)e.getProperty("name")))
 				entitylist_window.createNode((String)e.getProperty("name"), e, entitylist_window.getBase());
 			
 			//for(ResourceManager.ResourceItem resource: entity_list.getResourcesInCategory(category)) {
 			//	resource_window.createNode(resource.name, resource, found_node);
 			//}
 		}
 	}
 
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 		tsm.setEngine(engine);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		entitylist_window.removeAllNodes();
 		createEntityList();
 	}
 }
