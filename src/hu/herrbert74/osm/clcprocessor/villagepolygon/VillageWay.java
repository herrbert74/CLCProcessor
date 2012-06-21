package hu.herrbert74.osm.clcprocessor.villagepolygon;

import java.util.ArrayList;

public class VillageWay {
	int id;
	ArrayList<Integer> members;	
	
	public VillageWay(){
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
}
