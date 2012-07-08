package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;

public class CustomWay {
	int id;
	ArrayList<Integer> members;	
	
	public CustomWay(){
		id = 0;
		members = new ArrayList<Integer>();
	}
	
	public int getWayId() {
		return id;
	}
	public void setWayId(int id) {
		this.id = id;
	}
	public ArrayList<Integer> getMembers() {
		return members;
	}
	public void addMember(Integer member) {
		members.add(member);
	}
	
	public boolean containsNode(CustomNode vn){
		boolean result = false;
		for(int nodeIDToCompare : members){
			if(vn.getNodeId() == nodeIDToCompare){
				result = true;
			}
		}
		return result;
	}
}
