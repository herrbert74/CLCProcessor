package hu.herrbert74.osm.clcprocessor;

import java.util.ArrayList;

public class VillageRelation {
	String name;
	ArrayList<Integer> members;	
	
	public VillageRelation(){
		name = "";
		members = new ArrayList<Integer>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Integer> getMembers() {
		return members;
	}
	public void addMember(Integer member) {
		members.add(member);
	}
}
