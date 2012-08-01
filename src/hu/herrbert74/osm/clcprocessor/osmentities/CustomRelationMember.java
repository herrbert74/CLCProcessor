package hu.herrbert74.osm.clcprocessor.osmentities;

public class CustomRelationMember{
	String type;
	int ref;
	String role;
	public CustomRelationMember(String type, int ref, String role){
		this.type = type;
		this.ref = ref;
		this.role = role;
	}
	
	public int getRef() {
		return ref;
	}	
	
	public String getRole() {
		return role;
	}
	
	public String getType() {
		return type;
	}
}