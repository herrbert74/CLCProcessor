package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;

public class CustomRelation {
	String name;
	ArrayList<Integer> members;	
	
	public CustomRelation(){
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
