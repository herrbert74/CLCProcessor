package hu.herrbert74.osm.clcprocessor.clcpolygon;

import java.util.ArrayList;

public class CLCRelation {
	String name;
	ArrayList<Integer> members;	
	
	public CLCRelation(){
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
