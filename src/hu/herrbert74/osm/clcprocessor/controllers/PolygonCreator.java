package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.osmentities.Intersections;
import hu.herrbert74.osm.clcprocessor.osmentities.NodePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class PolygonCreator {

	SettlementChooserModel scModel;

	public PolygonCreator(SettlementChooserModel scM) {
		scModel = scM;
	}

	private ArrayList<CustomPolygon> getPolygonList(String[] items) {
		ArrayList<CustomPolygon> result = new ArrayList<CustomPolygon>();
		for (String village : items) {
			result.add(scModel.villagePolygons.get(village));
		}
		return result;
	}

	public ArrayList<CustomNode> createBorderPolygon(String[] items) {
		ArrayList<CustomPolygon> polygonList = getPolygonList(items);
		Intersections is = new Intersections();
		ArrayList<CustomWay> ways = new ArrayList<CustomWay>();
		is = findIntersections(polygonList);
		for (CustomPolygon vp : polygonList) {
			ways.addAll(vp.split(is));
		}
		ArrayList<CustomNode> result = concatenateWays(ways);
		result.add(result.get(0));
		return result;
	}

	private Intersections findIntersections(ArrayList<CustomPolygon> aggregatePolygonMembers) {
		Intersections result = new Intersections();
		for (CustomPolygon vp : aggregatePolygonMembers) {
			boolean[] isNodes = new boolean[vp.getVillageNodes().size()];
			isNodes = getIntersectionNodes(aggregatePolygonMembers, vp);
			boolean[] isStart = new boolean[vp.getVillageNodes().size()];
			isStart = getStartNodes(isNodes);
			boolean[] isEnd = new boolean[vp.getVillageNodes().size()];
			isEnd = getEndNodes(isNodes);
			ArrayList<NodePair> forbiddenEdges = new ArrayList<NodePair>();
			forbiddenEdges = getForbiddenEdges(isStart, isEnd);
			// isNodes = unsetStartsAndEnds(isNodes, isStart, isEnd);
			result.addStartsAndEnds(vp, isStart, isEnd);
			HashSet<CustomNode> nodes = new HashSet<CustomNode>();
			for (int i = 0; i < vp.getVillageNodes().size() - 1; i++) {
				if (isNodes[i]) {
					nodes.add(vp.getVillageNodes().get(i));
				}
			}
			result.addIntersections(nodes);
			result.addForbiddenEdges(forbiddenEdges);
		}
		result.unsetStartsAndEnds();
		return result;
	}

	private boolean[] getStartNodes(boolean[] isNodes) {
		boolean[] result = new boolean[isNodes.length];
		for (int i = 1; i < isNodes.length; i++) {
			if (isNodes[i] && !isNodes[i - 1]) {
				result[i] = true;
			} else {
				result[i] = false;
			}

		}

		return result;
	}

	private boolean[] getEndNodes(boolean[] isNodes) {
		boolean[] result = new boolean[isNodes.length];
		for (int i = 0; i < isNodes.length - 1; i++) {
			if (isNodes[i] && !isNodes[i + 1]) {
				result[i] = true;
			} else {
				result[i] = false;
			}
		}

		return result;
	}

	private ArrayList<NodePair> getForbiddenEdges(boolean[] isStart, boolean[] isEnd) {
		ArrayList<NodePair> np = new ArrayList<NodePair>();
		for (int i = 0; i < isStart.length - 1; i++) {
			if (isStart[i] && isEnd[i + 1]) {
				np.add(new NodePair(i, i + 1));
			}
		}
		return np;
	}

	private boolean[] unsetStartsAndEnds(boolean[] isNodes, boolean[] isStart, boolean[] isEnd) {
		for (int i = 0; i < isNodes.length; i++) {
			isNodes[i] = isNodes[i] && (!(isStart[i] || isEnd[i]));
		}
		return isNodes;
	}

	private boolean[] getIntersectionNodes(ArrayList<CustomPolygon> aggregatePolygonMembers, CustomPolygon vp) {
		boolean[] result = new boolean[vp.getVillageNodes().size()];
		for (int i = 0; i < vp.getVillageNodes().size(); i++) {
			searchIntersection(aggregatePolygonMembers, vp, result, i);
		}
		return result;
	}

	private ArrayList<CustomNode> concatenateWays(ArrayList<CustomWay> ways) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		for (int i = 0; i < ways.size(); i++) {
			System.out.println(Integer.toString(ways.get(i).getMembers().get(0)) + "-"
					+ Integer.toString(ways.get(i).getMembers().get(ways.get(i).getMembers().size() - 1)));

		}
		do {
			if (result.size() == 0) {
				System.out.println(Integer.toString(ways.get(0).getMembers().get(0)) + "-"
						+ Integer.toString(ways.get(0).getMembers().get(ways.get(0).getMembers().size() - 1))
						+ " size: " + Integer.toString(ways.get(0).getMembers().size()));
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
						System.out.println(Integer.toString(ways.get(i).getMembers().get(0)) + "-"
								+ Integer.toString(ways.get(i).getMembers().get(ways.get(i).getMembers().size() - 1))
								+ " size: " + Integer.toString(ways.get(i).getMembers().size()));
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

	private void searchIntersection(ArrayList<CustomPolygon> aggregatePolygonMembers, CustomPolygon vp,
			boolean[] result, int i) {
		for (CustomPolygon vpToCompare : aggregatePolygonMembers) {
			if (!vp.equals(vpToCompare)) {
				for (CustomNode vnToCompare : vpToCompare.getVillageNodes()) {
					if (vp.getVillageNodes().get(i).getNodeId() == vnToCompare.getNodeId()) {
						result[i] = true;
						return;
					}
				}
			}
		}
	}
}
