package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomRelation {
	String name;
	int id;
	ArrayList<CustomRelationMember> members;
	HashMap<String, String> tags;

	public CustomRelation() {
		name = "";
		members = new ArrayList<CustomRelationMember>();
		tags = new HashMap<String, String>();
	}

	public CustomRelation(CustomRelationMember crm, int id) {
		name = "";
		this.id = id;
		members = new ArrayList<CustomRelationMember>();
		members.add(crm);
		tags = new HashMap<String, String>();
	}

	public int getRelationId() {
		return id;
	}

	public void setRelationId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<CustomRelationMember> getMembers() {
		return members;
	}

	public void addMember(CustomRelationMember member) {
		members.add(member);
	}

	public void addTag(String K, String V) {
		tags.put(K, V);
	}

	public HashMap<String, String> getTags() {
		return tags;
	}

	public int getOverLappingMember(int wayID, HashMap<Integer, CustomWay> clcMainWays,
			HashMap<Integer, CustomNode> clcMainNodes) {
		int overLappingWayId = -1;
		// System.out.println("Way ID: " + Integer.toString(wayID));
		CustomWay cw = clcMainWays.get(wayID);
		if (id == -4856392) {
			int u = 8;
			int z = u;

		}

		for (CustomRelationMember crm : getMembers()) {
			CustomWay otherCw = clcMainWays.get(crm.getRef());
			if (otherCw != null) {
				int same = 0;
				for (int nodeId : cw.getMembers()) {
					for (int otherNodeId : otherCw.getMembers()) {
						if (nodeId == otherNodeId && wayID != otherCw.getWayId()) {
							same++;
						}
					}
				}
				if (same > 1) {
					overLappingWayId = otherCw.getWayId();
					return overLappingWayId;
				}

			}
		}
		return overLappingWayId;
	}

	public CustomRelationMember getMemberWithWayId(int wayId) {
		CustomRelationMember result = members.get(0);
		for (CustomRelationMember crm : members) {
			if (crm.getRef() == wayId) {
				return crm;
			}
		}
		return result;
	}

	public void removeMemberWithWayId(int wayId) {
		CustomRelationMember removable = new CustomRelationMember("", 0, "");
		for (CustomRelationMember crm : members) {
			if (crm.getRef() == wayId) {
				removable = crm;
			}
		}
		members.remove(removable);
	}
}
