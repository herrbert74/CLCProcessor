package hu.herrbert74.osm.clcprocessor.clcpolygon;

import java.util.ArrayList;

public class CLCPolygon {
	String name;
	ArrayList<CLCNode> villageNodes = new ArrayList<CLCNode>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<CLCNode> getVillageNodes() {
		return villageNodes;
	}

	public void setVillageNodes(ArrayList<CLCNode> villageNodes) {
		this.villageNodes = villageNodes;
	}

	public void addVillageNode(CLCNode vn) {
		villageNodes.add(vn);
	}
	
}