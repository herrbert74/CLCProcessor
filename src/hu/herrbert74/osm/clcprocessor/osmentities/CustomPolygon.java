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
		CustomWay newWay = new CustomWay();
		for (int i = 0; i < villageNodes.size() - 1; i++) {
			CustomNode otherVillageNode = new CustomNode(); 
			//Other node
			otherVillageNode = villageNodes.get(i + 1);
			//If either of the nodes is not an intersection, add the actual to the way
			if(intersections.indexOf(villageNodes.get(i)) == -1 || intersections.indexOf(otherVillageNode) == -1){
				newWay.addMember(villageNodes.get(i).getNodeId());
				System.out.println("Node added: " + Integer.toString(villageNodes.get(i).getNodeId()));
			}
			//If both nodes are in intersections but the last one was not, add the actual
			else if(i>0){
				if(intersections.indexOf(villageNodes.get(i-1)) == -1){
					newWay.addMember(villageNodes.get(i).getNodeId());
					System.out.println("Intersection node added: " + Integer.toString(villageNodes.get(i).getNodeId()));
				}
				
			}
			//if it's the last node of the polygon or the last non-intersection node, close the way
			if(((i == villageNodes.size() - 2) || 
					(intersections.indexOf(villageNodes.get(i)) > -1 && intersections.indexOf(otherVillageNode) > -1)) 
					&& newWay.getMembers().size() > 0){
				result.add(newWay);
				newWay = new CustomWay();
				System.out.println("Way closed");
			}
			
		}
		return result;
	}
}