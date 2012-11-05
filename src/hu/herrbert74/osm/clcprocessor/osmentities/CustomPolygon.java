package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;
import java.util.Collections;

public class CustomPolygon implements Comparable<CustomPolygon> {
	String name;
	ArrayList<CustomNode> villageNodes = new ArrayList<CustomNode>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<CustomNode> getVillageNodes() {
		return villageNodes;
	}

	public void setVillageNodes(ArrayList<CustomNode> villageNodes) {
		this.villageNodes = villageNodes;
	}

	public void addVillageNode(CustomNode vn) {
		villageNodes.add(vn);
	}

	@Override
	public int compareTo(CustomPolygon o) {
		int nameCmp = name.compareTo(o.getName());
		return nameCmp;
	}

	public ArrayList<CustomWay> getBorderWays(Intersections is) {
		ArrayList<CustomNode> resultNodes = new ArrayList<CustomNode>();
		resultNodes.addAll(getVillageNodes());
		resultNodes.remove(resultNodes.size() - 1); // ResultNodes aren't
													// circular!
		ArrayList<CustomWay> result = new ArrayList<CustomWay>();
		ArrayList<Integer> startNodes = new ArrayList<Integer>();
		ArrayList<Integer> endNodes = new ArrayList<Integer>();
		boolean isNextNodeIntersection, isLastNodeIntersection, isLastNodeTwoNodeNonIntersection, isThisNodeTwoNodeNonIntersection, isNextNodeTwoNodeNonIntersection;
		for (int i = 0; i < resultNodes.size(); i++) {
			if (i == 0) {
				isLastNodeIntersection = is.getIntersections().contains(resultNodes.get(resultNodes.size() - 1)) ? true : false;
				isNextNodeIntersection = is.getIntersections().contains(resultNodes.get(i + 1)) ? true : false;
				isLastNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(resultNodes.size() - 1)) ? true
						: false;
				isThisNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i)) ? true : false;
				isNextNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i + 1)) ? true : false;
			} else if (i == resultNodes.size() - 1) {
				isLastNodeIntersection = is.getIntersections().contains(resultNodes.get(i - 1)) ? true : false;
				isNextNodeIntersection = is.getIntersections().contains(resultNodes.get(0)) ? true : false;
				isLastNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i - 1)) ? true : false;
				isThisNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i)) ? true : false;
				isNextNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(0)) ? true : false;
			} else {
				isLastNodeIntersection = is.getIntersections().contains(resultNodes.get(i - 1)) ? true : false;
				isNextNodeIntersection = is.getIntersections().contains(resultNodes.get(i + 1)) ? true : false;
				isLastNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i - 1)) ? true : false;
				isThisNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i)) ? true : false;
				isNextNodeTwoNodeNonIntersection = is.getTwoNodeNonIntersections().contains(resultNodes.get(i + 1)) ? true : false;
			}
			if (is.getIntersections().contains(resultNodes.get(i))) {
				if (!isLastNodeIntersection || (isThisNodeTwoNodeNonIntersection && isLastNodeTwoNodeNonIntersection)) {
					endNodes.add(i);
				}
				if (!isNextNodeIntersection || (isThisNodeTwoNodeNonIntersection && isNextNodeTwoNodeNonIntersection)) {
					startNodes.add(i);
				}
			}
		}
		// Order them
		if (endNodes.get(0) < startNodes.get(0)) {
			Collections.rotate(endNodes, -1);
		}
		// Remove one-node ways
		int iNode = 0;
		do {
			if (startNodes.get(iNode) == endNodes.get(iNode)) {
				startNodes.remove(iNode);
				endNodes.remove(iNode);
			} else {
				iNode++;
			}
		} while (iNode < startNodes.size());
		for (int i = 0; i < startNodes.size(); i++) {
			ArrayList<CustomNode> copyOfResultNodes = new ArrayList<CustomNode>();
			copyOfResultNodes.addAll(getVillageNodes());
			copyOfResultNodes.remove(copyOfResultNodes.size() - 1);
			// Starting node goes first
			Collections.rotate(copyOfResultNodes, -startNodes.get(i));
			// Cut off the intersecting nodes on the end
			int size = endNodes.get(i) > startNodes.get(i) ? endNodes.get(i) - startNodes.get(i) : copyOfResultNodes.size()
					+ endNodes.get(i) - startNodes.get(i);
			resultNodes = new ArrayList<CustomNode>(copyOfResultNodes.subList(0, size + 1));
			CustomWay newWay = new CustomWay();
			for (CustomNode cn : resultNodes) {
				newWay.getMembers().add(cn.getNodeId());
			}
			result.add(newWay);
		}
		return result;
	}
}