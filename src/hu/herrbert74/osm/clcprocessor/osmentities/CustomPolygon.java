package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;

public class CustomPolygon implements Comparable<CustomPolygon> {
	String name;
	ArrayList<CustomNode> villageNodes = new ArrayList<CustomNode>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<CustomNode> getVillageNodes() {
		return villageNodes;
	}

	public void setVillageNodes(ArrayList<CustomNode> villageNodes) {
		this.villageNodes = villageNodes;
	}

	public void addVillageNode(CustomNode vn) {
		villageNodes.add(vn);
	}

	@Override
	public int compareTo(CustomPolygon o) {
		int nameCmp = name.compareTo(o.getName());
		return nameCmp;
	}

	public ArrayList<CustomWay> split(ArrayList<CustomNode> intersections) {
		ArrayList<CustomWay> result = new ArrayList<CustomWay>();
		boolean buildingBorder = false;
		CustomWay newWay = new CustomWay();
		for (int i = 0; i < villageNodes.size(); i++) {
			if (intersections.indexOf(villageNodes.get(i)) == -1) {
				newWay.addMember(villageNodes.get(i).getNodeId());
				buildingBorder = true;
				// System.out.println("Node added: " +
				// Integer.toString(villageNodes.get(i).getNodeId()));
			} else {
				if (buildingBorder) {
					newWay.addMember(villageNodes.get(i).getNodeId());
					result.add(newWay);
					newWay = new CustomWay();
				} else {
					newWay = new CustomWay();
					newWay.addMember(villageNodes.get(i).getNodeId());
				}
				buildingBorder = false;
				// System.out.println("Intersection node added: " +
				// Integer.toString(villageNodes.get(i).getNodeId()));
			}
		}
		if (newWay.getMembers().size() > 0) {
			result.add(newWay);
		}
		// System.out.println("Way closed");

		return result;
	}
}