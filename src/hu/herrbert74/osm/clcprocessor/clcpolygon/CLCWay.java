package hu.herrbert74.osm.clcprocessor.clcpolygon;

import java.util.ArrayList;

public class CLCWay {
	int id;
	ArrayList<Integer> members;	
	
	public CLCWay(){
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
