package hu.herrbert74.osm.clcprocessor.models;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelationMember;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.osmentities.WayPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettlementChooserModel extends java.util.Observable {
	//public ArrayList<CustomPolygon> villagePolygons = new ArrayList<CustomPolygon>();
	public Map<String, CustomPolygon> villagePolygons = new HashMap<String, CustomPolygon>();
	public Map<Integer, CustomNode> villageNodesMap = new HashMap<Integer, CustomNode>();
	public Map<Integer, CustomWay> villageWaysMap = new HashMap<Integer, CustomWay>();
	public Map<String, CustomRelation> villageRelationsMap = new HashMap<String, CustomRelation>();

	public HashMap<Integer, CustomNode> clcMainNodes = new HashMap<Integer, CustomNode>();
	public HashMap<Integer, CustomNode> clcNeighbourNodes = new HashMap<Integer, CustomNode>();
	public HashMap<Integer, CustomWay> clcMainWays = new HashMap<Integer, CustomWay>();
	public HashMap<Integer, CustomRelation> clcMainRelations = new HashMap<Integer, CustomRelation>();
	public ArrayList<CustomNode> borderPolygon;
	public ArrayList<CustomNode> neighbourPolygon;
	public ArrayList<WayPair> wayPairList = new ArrayList<WayPair>();
	
	public String status = "";

	public void createVillagePolygons() {
		int z = 0;
		for (CustomRelation vr : villageRelationsMap.values()) {
			setStatus("polygon: " + Integer.toString(++z) + "/" + Integer.toString(villageRelationsMap.size()));
			CustomPolygon vp = new CustomPolygon();
			vp.setName(vr.getName());
			CustomNode firstNode = new CustomNode();
			int lastNode = 0;
			do {
				int way = getNextVillageWay(vr, villageWaysMap, lastNode);
				CustomWay vw = villageWaysMap.get(way);
				boolean areNodesForward = (lastNode == 0 || vw.getMembers().get(0) == lastNode);
				if (areNodesForward) {
					for (int i = 0; i < vw.getMembers().size(); i++) {
						if (firstNode.getNodeId() == 0) {
							firstNode = villageNodesMap.get(vw.getMembers().get(i));
						}
						vp.addVillageNode(villageNodesMap.get(vw.getMembers().get(i)));
						lastNode = vp.getVillageNodes().get(vp.getVillageNodes().size() - 1).getNodeId();
					}
				} else {
					for (int i = vw.getMembers().size() - 1; i >= 0; i--) {
						if (firstNode.getNodeId() == 0) {
							firstNode = villageNodesMap.get(vw.getMembers().get(i));
						}
						vp.addVillageNode(villageNodesMap.get(vw.getMembers().get(i)));
						lastNode = vp.getVillageNodes().get(vp.getVillageNodes().size() - 1).getNodeId();
					}
				}
				vp.getVillageNodes().remove(vp.getVillageNodes().size() - 1);
				vr.getMembers().remove(vr.getMembers().indexOf(vr.getMemberWithWayId(vw.getWayId())));
			} while (vr.getMembers().size() > 0);
			vp.addVillageNode(firstNode);
			villagePolygons.put(vp.getName(),vp);
		}
		setChanged();
		notifyObservers(villagePolygons);
	}
	
	public void setStatus(String status) {
		setChanged();
		notifyObservers(status);
	}

	private int getNextVillageWay(CustomRelation vr, Map<Integer, CustomWay> villageWaysMap2, int lastVillageNode) {
		if (lastVillageNode == 0) {
			return vr.getMembers().get(0).getRef();
		} else {
			for (CustomRelationMember member : vr.getMembers()) {
				if (villageWaysMap2.get(member.getRef()).getMembers().contains(lastVillageNode)) {
					return member.getRef();
				}
			}
			// Return the first member if nothing found. Have to be an exclave!
			return vr.getMembers().get(0).getRef();
		}
	}
}
