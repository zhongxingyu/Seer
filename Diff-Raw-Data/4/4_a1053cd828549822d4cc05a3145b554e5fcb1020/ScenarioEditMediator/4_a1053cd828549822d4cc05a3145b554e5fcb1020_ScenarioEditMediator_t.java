 package org.visico.neighborhoodpss.gwt.shared.patterns;
 
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.visico.neighborhoodpss.gwt.client.BuildingPolygon;
 import org.visico.neighborhoodpss.gwt.client.BuildingTable;
 import org.visico.neighborhoodpss.gwt.client.ChangeAddDataDlg;
 import org.visico.neighborhoodpss.gwt.client.EditMapPanel;
 import org.visico.neighborhoodpss.gwt.client.HierarchyPanel;
 import org.visico.neighborhoodpss.gwt.client.Map;
 import org.visico.neighborhoodpss.gwt.client.NetworkEdge;
 import org.visico.neighborhoodpss.gwt.client.NetworkTable;
 import org.visico.neighborhoodpss.gwt.client.NodeMarker;
 import org.visico.neighborhoodpss.domain.project.BuildingDTO;
 import org.visico.neighborhoodpss.domain.project.BuildingDataDTO;
 import org.visico.neighborhoodpss.domain.project.BuildingDataTypeDTO;
 import org.visico.neighborhoodpss.domain.project.EdgeDTO;
 import org.visico.neighborhoodpss.domain.project.GeoEdgeDTO;
 import org.visico.neighborhoodpss.domain.project.GeoNetworkDTO;
 import org.visico.neighborhoodpss.domain.project.GeoPointDTO;
 import org.visico.neighborhoodpss.domain.project.NetworkDTO;
 import org.visico.neighborhoodpss.domain.project.NodeDTO;
 import org.visico.neighborhoodpss.domain.project.ProjectDTO;
 import org.visico.neighborhoodpss.domain.project.ScenarioDTO;
 
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Overlay;
 import com.google.gwt.maps.client.overlay.PolyStyleOptions;
 import com.google.gwt.maps.client.overlay.Polygon;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 
 
 public class ScenarioEditMediator {
 	ScenarioDTO scenario;
 	ProjectMediator projectMed;
 	
 	Map map;
 	EditMapPanel editNetworkPanel;
 	private NetworkTable networkTable;
 	private BuildingTable buildingTable;
 	private ChangeAddDataDlg changeAddDataDlg;
 	
 	NetworkDTO selectedNetwork = null;
 	Set<NetworkDTO> visibleNetworks = new HashSet<NetworkDTO>();
 	
 	double inflow;
 	double outflow;
 	double capacity;
 	
 	private HashMap<BuildingPolygon, BuildingDTO> buildingMap = new HashMap<BuildingPolygon, BuildingDTO>();
 	private HashMap<NetworkEdge, EdgeDTO> edgeMap = new HashMap<NetworkEdge, EdgeDTO>();
 	private HashMap<NodeMarker, NodeDTO> nodeMap = new HashMap<NodeMarker, NodeDTO>();
 	private Set<Overlay> selected = new HashSet<Overlay>();
 	private HashMap<BuildingDataTypeDTO, TextBox> buildingDataMap;
 	
 	
 	
 	public ScenarioEditMediator(ScenarioDTO scenario, ProjectMediator projectMed)
 	{
 		this.scenario = scenario;
 		this.projectMed = projectMed;
 	}
 	
 	public void initializeOverlays() {
 		for (BuildingDTO b : scenario.getBuildingDTOs())
 		{
 			LatLng[] polyLatLng = new LatLng[b.getPoints().size()];
 	    	for (int i=0; i<b.getPoints().size(); i++)
 			{
 	    		polyLatLng[i] = LatLng.newInstance(b.getPoints().get(i).getLatitude(), 
 						b.getPoints().get(i).getLongitude());
 			}
 	    	
 	    	BuildingPolygon bldgPlg = new BuildingPolygon(polyLatLng);
 	    	map.getMap().addOverlay(bldgPlg);
 	    	buildingMap.put(bldgPlg, b);
 		}
 		
 		ArrayList<GeoNetworkDTO> networks = new ArrayList<GeoNetworkDTO>(scenario.getGeoNetworkDTOs()); 
 		networkTable.fillTable(networks);
 		
 		for (GeoNetworkDTO n : scenario.getGeoNetworkDTOs())
 		{
 			for (GeoEdgeDTO e : n.getEdges())
 			{
 				LatLng points[] = new LatLng[2];
 				points[0] = LatLng.newInstance(e.getStart_node().getLatitude(), e.getStart_node().getLongitude());
 				NodeMarker mStart = new NodeMarker(points[0], Map.getOptions());
 				map.getMap().addOverlay(mStart);
 				nodeMap.put(mStart, e.getStart_node());
 				
 				points[1] = LatLng.newInstance(e.getEnd_node().getLatitude(), e.getEnd_node().getLongitude());
 				NodeMarker mEnd = new NodeMarker(points[1], Map.getOptions());
 				map.getMap().addOverlay(mEnd);
 				nodeMap.put(mEnd, e.getEnd_node());
 				
 				NetworkEdge edge = new NetworkEdge(mStart, mEnd, points, n.getColor());
 				map.getMap().addOverlay(edge);
 				edgeMap.put(edge, e);
 			}
 		}
 	}
 
 	public NetworkDTO getSelectedNetwork() {
 		return selectedNetwork;
 	}
 
 	public void setSelectedNetwork(NetworkDTO selectedNetwork) {
 		if (selectedNetwork == null)
 		{
 			map.setMode(Map.editmodes.NO_NETWORK);
 			editNetworkPanel.cleanEditModes();
 		}
 		this.selectedNetwork = selectedNetwork;
 	}
 
 	public Set<NetworkDTO> getVisibleNetworks() {
 		return visibleNetworks;
 	}
 
 	public void setVisibleNetworks(Set<NetworkDTO> visibleNetworks) {
 		this.visibleNetworks = visibleNetworks;
 	}
 
 	public void selectionMode() {
 		if (selectedNetwork != null)
 			map.setMode(Map.editmodes.SELECTION);
 		
 			
 	}
 	
 	public void addNodeMode() {
 		if (selectedNetwork != null)
 			map.setMode(Map.editmodes.ADD_NODE);
 		
 	}
 	
 	public void addEdgeMode() {
 		if (selectedNetwork != null)
 			map.setMode(Map.editmodes.ADD_EDGE);
 	}
 
 	public void registerMap(Map map) {
 		this.map = map;
 	}
 
 	public void registerEditNetworkPanel(EditMapPanel edtNetwPnl) {
 		this.editNetworkPanel = edtNetwPnl;
 	}
 	
 	public void registerNetworkTable(NetworkTable table) {
 		this.networkTable = table;
 	}
 	
 	public void registerBuildingTable(BuildingTable buildingTable) {
 		this.buildingTable = buildingTable;
 		insertBuildingsInTable();
 	}
 
 	private void insertBuildingsInTable() {
 		Grid buildingGrid = buildingTable.getBuildingGrid();
 		buildingGrid.clear();
 		
 		buildingGrid.resizeRows(buildingGrid.getRowCount() + 1);
 		buildingGrid.resizeColumns(projectMed.getBuildingDataTypes().size() + 1);
 		
 		int column = 1;
 		for (BuildingDataTypeDTO dataType : projectMed.getBuildingDataTypes())  {
 			buildingGrid.setWidget(0, column, new Label(dataType.getName()));
 			int row = 1;
 			
 			for (BuildingDTO b : scenario.getBuildingDTOs())  {
 				buildingGrid.insertRow(buildingGrid.getRowCount());
 				buildingGrid.setText(row, 0, Integer.toString(b.getId()));
 				buildingGrid.setText(row, column, b.getData().get(dataType).getValue());
 				row ++;
 			}
 			column ++; 
 		}
 		
 		
 	}
 
 	public void noMode() {
 		map.setMode(Map.editmodes.NO_NETWORK);
 	}
 
 	public double getInflow() {
 		return inflow;
 	}
 
 	public void setInflow(double inflow) {
 		this.inflow = inflow;
 	}
 
 	public double getOutflow() {
 		return outflow;
 	}
 
 	public void setOutflow(double outflow) {
 		this.outflow = outflow;
 	}
 
 	public double getCapacity() {
 		return capacity;
 	}
 
 	public void setCapacity(double capacity) {
 		this.capacity = capacity;
 	}
 
 	public void addBuildingMode() {
 		map.setMode(Map.editmodes.ADD_BUILDING);
 	
 	}
 
 	public void changeSelected() {
 		for (Overlay o : selected)
 		{
 			if (o instanceof NodeMarker)
 			{
 				NodeDTO nodeDTO = nodeMap.get((NodeMarker)o);
 				nodeDTO.setInflow(getInflow());
 		     	nodeDTO.setOutflow(getOutflow());
 			}
 			else if (o instanceof NetworkEdge)
 			{
 				EdgeDTO edgeDTO = edgeMap.get((NetworkEdge)o);
 				edgeDTO.setCapacity(capacity);
 			}
 			
 		}
 		
 	}
 
 	public void deleteSelected() {
 		for (Overlay o : selected)
 		{
 			
 			if (o != null && o instanceof NetworkEdge)
 			{
 				EdgeDTO edgeDTO = edgeMap.get((NetworkEdge)o);
 				((GeoNetworkDTO)selectedNetwork).deleteEdge(edgeDTO);
 				edgeMap.remove(o);
 				map.getMap().removeOverlay(o);
 			}
 			else if (o != null && o instanceof BuildingPolygon)
 			{
 				BuildingDTO buildingDTO = buildingMap.get(((BuildingPolygon)o));
 				scenario.deleteBuilding(buildingDTO);
 				buildingMap.remove(o);
 				map.getMap().removeOverlay(o);
 			}
 		}
 		selected.clear();
 		editNetworkPanel.changeSelectedLabel(0,0,0);
 	}
 
 	public void addNewNode(NodeMarker node) {
 		  NodeDTO nodeDTO = new NodeDTO();
      	  nodeDTO.setLatitude(node.getLatLng().getLatitude());
      	  nodeDTO.setLongitude(node.getLatLng().getLongitude());
      	  nodeDTO.setInflow(getInflow());
      	  nodeDTO.setOutflow(getOutflow());
      	  ((GeoNetworkDTO)selectedNetwork).addNode(nodeDTO);
      	  nodeMap.put(node, nodeDTO);
 	}
 
 	public void addNewEdge(NetworkEdge edge) {
 		if (selectedNetwork instanceof GeoNetworkDTO)
 		{
 			GeoEdgeDTO edgeDTO = new GeoEdgeDTO();
 			edgeDTO.setCapacity(capacity);
 			edgeDTO.setStart_node(nodeMap.get(edge.getStart()));
 			edgeDTO.setEnd_node(nodeMap.get(edge.getEnd()));
 			((GeoNetworkDTO)selectedNetwork).addEdge(edgeDTO);
 			edgeMap.put(edge, edgeDTO);
 		}	
 	}
 
 	public void addNewBuilding(BuildingPolygon building) {
 		BuildingDTO buildingDTO = new BuildingDTO();
 		
 		for (int i=0; i < building.getVertexCount(); i++)
 		{
 			buildingDTO.addVertex( building.getVertex(i).getLatitude(), building.getVertex(i).getLongitude());
 		}
 		
 		buildingDTO.setArea(building.getArea());
 		
 		for (BuildingDataTypeDTO dt : projectMed.getBuildingDataTypes())
 		{
 			BuildingDataDTO data = new BuildingDataDTO();
 			data.setBuilding(buildingDTO);
 			data.setType(dt);
 			data.setValue(dt.getDefault_val());
 			buildingDTO.getData().put(data.getType(), data);
 		}
 		
 		scenario.addBuilingDTO(buildingDTO);
 		buildingMap.put(building, buildingDTO);
 		insertBuildingsInTable();
 	}
 
 	public void addMapSelection(Overlay overlay) {
 		selected.add(overlay);
 		if (overlay instanceof NetworkEdge)
 		{
 			((NetworkEdge) overlay).setOpacity(0.5);
 		}
 		else if (overlay instanceof NodeMarker)
 		{
 			((NodeMarker) overlay).setSelected();
 		}
 		changeSelectionString();
 	}
 	
 	public void removeSelection(Overlay overlay){
 		selected.remove(overlay);
 		if (overlay instanceof NetworkEdge)
 		{
 			((NetworkEdge) overlay).setOpacity(1.0);
 		}
 		else if (overlay instanceof NodeMarker)
 		{
 			((NodeMarker) overlay).setSelected();
 		}
 		changeSelectionString();
 	}
 	
 	public void clearAllSelected()
 	{
 		selected.clear();
 		changeSelectionString();
 	}
 	
 	
 	private void changeSelectionString()
 	{
 		int selectedNodes = 0, selectedEdges = 0, selectedBuildings = 0;
 		
 		for (Overlay o : selected)
 		{
 			if (o instanceof NodeMarker)
 				selectedNodes ++;
 			else if (o instanceof NetworkEdge)
 				selectedEdges ++;
 			else if (o instanceof BuildingPolygon)
 				selectedBuildings++;
 		}
 			
 		editNetworkPanel.changeSelectedLabel(selectedNodes, selectedEdges, selectedBuildings);
 	}
 
 	public void addGeoNetwork(String text, String hexColor) {
 		GeoNetworkDTO newNetwork = new GeoNetworkDTO();
 		newNetwork.setName(text);
 		newNetwork.setColor(hexColor);
 		scenario.addGeoNetworkDTO(newNetwork);
 		networkTable.fillTable(new ArrayList<GeoNetworkDTO>(scenario.getGeoNetworkDTOs()));
 	}
 
 	public void deleteGeoNetwork(GeoNetworkDTO networkToDelete) {
 		scenario.deleteGeoNetwork(networkToDelete);
 		networkTable.fillTable(new ArrayList<GeoNetworkDTO>(scenario.getGeoNetworkDTOs()));
 		
 		HashSet<NetworkEdge> edgesToDelete = new HashSet<NetworkEdge>();
 		HashSet<NodeMarker> nodesToDelete = new HashSet<NodeMarker>();
 		
 		for (EdgeDTO edge : networkToDelete.getEdges())
 		{
 			Iterator<Entry<NetworkEdge, EdgeDTO>> it = edgeMap.entrySet().iterator();
 			while (it.hasNext())
 			{
 				Entry<NetworkEdge, EdgeDTO> entry =  it.next();
 				if (entry.getValue() == edge)
 				{
 					NetworkEdge nwEdge = entry.getKey();
 					map.getMap().removeOverlay(nwEdge);
 					edgesToDelete.add(nwEdge);
 					
 					nodesToDelete.add(nwEdge.getStart());
 					nodesToDelete.add(nwEdge.getEnd());
 				}
 			}
 		}
 		
 		for (NetworkEdge e : edgesToDelete)
 		{
 			edgeMap.remove(e);
 			selected.remove(e);
 		}
 		
 		for (NodeMarker n : nodesToDelete)
 		{
 			map.getMap().removeOverlay(n);
 			nodeMap.remove(n);
 			selected.remove(n);
 		}
 		
 		changeSelectionString();
 		
 	}
 
 	public void deleteHangingNodes() {
 		if (selectedNetwork instanceof GeoNetworkDTO)
 		{
 			GeoNetworkDTO nw = (GeoNetworkDTO)selectedNetwork;
 			
 			HashSet<NodeDTO> connected = new HashSet<NodeDTO>();  
 			for (GeoEdgeDTO edge : nw.getEdges())
 			{
 				connected.add(edge.getStart_node());
 				connected.add(edge.getEnd_node());
 			}
 			
 			ArrayList<NodeMarker> markersToDelete = new ArrayList<NodeMarker>();
 			for (NodeDTO node : nw.getNodes())
 			{
 				if (connected.contains(node) == false)
 				{
 					
 					Iterator<Entry<NodeMarker, NodeDTO>> it = nodeMap.entrySet().iterator();
 					while (it.hasNext())
 					{
 						Entry<NodeMarker, NodeDTO> entry =  it.next();
 						if (entry.getValue() == node)
 						{
 							markersToDelete.add(entry.getKey());
 						}
 					}
 				}
 			}
 			
 			for (NodeMarker nm : markersToDelete)
 			{
 				nw.deleteNode(nodeMap.get(nm));
 				nodeMap.remove(nm);
 				map.getMap().removeOverlay(nm);
 			}
 		}
 		
 	}
 
 	public String getActiveNetworkColor() {
 		return selectedNetwork.getColor();
 	}
 
 	public double getLatitude() {
 		return HierarchyPanel.getInstance().getProject().getLatitude();
 	}
 
 	public double getLongitude() {
 		return HierarchyPanel.getInstance().getProject().getLongitude();
 	}
 
 	public void editAddElementData() {
 		changeAddDataDlg = new ChangeAddDataDlg(this);
 		setAddDataDlgTable();
 		changeAddDataDlg.center();
 		changeAddDataDlg.show();
 	}
 
 	public void setAddDataDlgTable() {
 		// collect the data 
 		buildingDataMap = new HashMap<BuildingDataTypeDTO, TextBox>();
 		
 		for (Overlay o : selected) {
 			if (o instanceof BuildingPolygon)  {
 				for (Entry<BuildingDataTypeDTO, BuildingDataDTO> data : buildingMap.get(o).getData().entrySet())  {
 					if (buildingDataMap.containsKey(data.getKey()) )  {
						TextBox dataBox = buildingDataMap.get(data.getKey());
						if (dataBox.getText().equals(data.getValue().getValue()) == false)  {
 							dataBox.setValue("*");
 							buildingDataMap.put(data.getKey(), dataBox);
 						}
 					}
 					else 
 					{
 						TextBox dataBox = new TextBox();
 						dataBox.setValue(data.getValue().getValue());
 						buildingDataMap.put(data.getKey(), dataBox);
 					}
 				}
 			}
 		}
 		
 		Grid buildingGrid = changeAddDataDlg.getBuildingDataGrid();
 		buildingGrid.clear();
 		
 		buildingGrid.resize(buildingDataMap.size() + 1, 2);
 		buildingGrid.setStyleName("BuildingTable");
 		buildingGrid.getRowFormatter().addStyleName(0, "BuildingTableHeader");
 		buildingGrid.setWidget(0, 0, new Label("Data Type"));
 		buildingGrid.setWidget(0, 1, new Label("Data Value"));
 		
 		int rowCount = 1;
 		for (Entry<BuildingDataTypeDTO, TextBox> type : buildingDataMap.entrySet())  {
 			buildingGrid.setWidget(rowCount, 0, new Label(type.getKey().getName()));
 			buildingGrid.setWidget(rowCount, 1, type.getValue());
 		}
 	}
 
 	public void changeAdditionalBuildingData() {
 		if (buildingDataMap != null)  {
 			for (Overlay o : selected)  {
 				if (o instanceof BuildingPolygon)  {
 					BuildingDTO building = buildingMap.get(o);
 					for (Entry<BuildingDataTypeDTO, BuildingDataDTO> data : building.getData().entrySet())  {
 						TextBox dataTextBox = buildingDataMap.get(data.getKey());
 						if (dataTextBox.getText().equals("*") == false)  {
 							BuildingDataDTO datadto = data.getValue();
 							datadto.setValue(dataTextBox.getText());
 							building.getData().put(data.getKey(), datadto);
 						}
 					}
 				}
 			}
 		}
 		
 		insertBuildingsInTable();
 	}
 }
