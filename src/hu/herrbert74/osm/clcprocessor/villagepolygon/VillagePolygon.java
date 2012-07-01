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
	
	public ArrayList<VillageWay> split(ArrayList<VillageNode> intersections){
		ArrayList<Integer> intersectionIndexes = new ArrayList<Integer>();
		ArrayList<VillageWay> result = new ArrayList<VillageWay>();
		for(VillageNode vn : villageNodes){
			int index = intersections.indexOf(vn);
			if(index >=0){
				intersectionIndexes.add(index);
			}
		}
		
		//Add members to avoid overflow
		intersections.add(intersections.get(0));
		intersectionIndexes.add(intersectionIndexes.get(0));
		int i = -1;
		do{
			i++;
			villageNodes.add(villageNodes.get(i));
		}while(intersections.indexOf(villageNodes.get(i)) == -1);
		
		for(i = 0; i < intersectionIndexes.size()-1; i++) {
			VillageWay newWay = new VillageWay();
			for(int j = intersectionIndexes.get(i); j < intersectionIndexes.get(i+1); j++){
				newWay.addMember(villageNodes.get(getOrdinalFromVillageNode(intersections.get(j))).getNodeId());
			}
			result.add(newWay);
		}
		return result;
	}

	private int getOrdinalFromVillageNode(VillageNode vn1) {
		for(VillageNode vn:villageNodes){
			if(vn.getNodeId() == vn1.getNodeId()){
				return villageNodes.indexOf(vn);
			}
		}
		return 0;
	}

	
}