package hu.herrbert74.osm.clcprocessor.villagepolygon;

import java.util.ArrayList;

public class VillagePolygon implements Comparable<VillagePolygon> {
	String name;
	ArrayList<VillageNode> villageNodes = new ArrayList<VillageNode>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<VillageNode> getVillageNodes() {
		return villageNodes;
	}

	public void setVillageNodes(ArrayList<VillageNode> villageNodes) {
		this.villageNodes = villageNodes;
	}

	public void addVillageNode(VillageNode vn) {
		villageNodes.add(vn);
	}

	@Override
	public int compareTo(VillagePolygon o) {
		int nameCmp = name.compareTo(o.getName());
        return nameCmp;
	}
	
}