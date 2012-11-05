package hu.herrbert74.osm.clcprocessor.helpers;

import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.osmentities.Intersections;

import java.util.ArrayList;
import java.util.Collection;

public class BorderPolygonCreator {

	SettlementChooserModel scModel;
	Intersections is;

	public BorderPolygonCreator(SettlementChooserModel scM) {
		scModel = scM;
		is = new Intersections();
	}

	/**
	 * Backbone method for border creation
	 * 
	 * Get the objects from the Strings, find the intersections, create the
	 * ways, concatenate the ways and return them
	 * 
	 * @param items
	 * @return
	 */
	public ArrayList<CustomNode> createBorderPolygon(String[] items) {
		ArrayList<CustomPolygon> polygonList = getPolygonList(items);
		is.findIntersections(polygonList);
		ArrayList<CustomWay> ways = new ArrayList<CustomWay>();
		for (CustomPolygon vp : polygonList) {
			ways.addAll(vp.getBorderWays(is));
		}
		ArrayList<CustomNode> result = concatenateWays(ways);
		result.add(result.get(0));
		return result;
	}

	private ArrayList<CustomPolygon> getPolygonList(String[] items) {
		ArrayList<CustomPolygon> result = new ArrayList<CustomPolygon>();
		for (String village : items) {
			result.add(scModel.villagePolygons.get(village));
		}
		return result;
	}

	private ArrayList<CustomNode> concatenateWays(ArrayList<CustomWay> ways) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		for (int i = 0; i < ways.size(); i++) {
			System.out.println("Border ways: " + Integer.toString(ways.get(i).getMembers().get(0)) + "-"
					+ Integer.toString(ways.get(i).getMembers().get(ways.get(i).getMembers().size() - 1)));
		}
		do {
			if (result.size() == 0) {
				System.out.println("Concatenating border ways: " + Integer.toString(ways.get(0).getMembers().get(0)) + "-"
						+ Integer.toString(ways.get(0).getMembers().get(ways.get(0).getMembers().size() - 1)) + " size: "
						+ Integer.toString(ways.get(0).getMembers().size()));
				for (int i = 0; i < ways.get(0).getMembers().size(); i++) {
					result.add(scModel.villageNodesMap.get(ways.get(0).getMembers().get(i)));
				}
				ways.remove(0);
			} else {
				int i = -1;
				CustomNode lastNode = result.get(result.size() - 1);
				do {
					i++;
					if (ways.get(i).containsNode(lastNode.getNodeId()) >= 0) {
						System.out.println("Concatenating border ways: " + Integer.toString(ways.get(i).getMembers().get(0)) + "-"
								+ Integer.toString(ways.get(i).getMembers().get(ways.get(i).getMembers().size() - 1)) + " size: "
								+ Integer.toString(ways.get(i).getMembers().size()));
						result.remove(result.size() - 1);
						result.addAll(getVillageNodes(ways.get(i), lastNode));
					}
				} while (!(ways.get(i).containsNode(lastNode.getNodeId()) >= 0));
				ways.remove(i);
			}
		} while (ways.size() > 0);
		return result;
	}

	private Collection<? extends CustomNode> getVillageNodes(CustomWay villageWay, CustomNode startNode) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		if (villageWay.getMembers().get(0) == startNode.getNodeId()) {
			for (int vnID : villageWay.getMembers()) {
				result.add(scModel.villageNodesMap.get(vnID));
			}
		}
		// Reverse way
		else {
			for (int i = villageWay.getMembers().size() - 1; i >= 0; i--) {
				result.add(scModel.villageNodesMap.get(villageWay.getMembers().get(i)));
			}
		}
		return result;
	}

}
