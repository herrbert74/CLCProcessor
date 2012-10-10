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
		boolean isIntersection = false;
		ArrayList<Integer> iSStart = new ArrayList<Integer>();
		ArrayList<Integer> iSEnd = new ArrayList<Integer>();
		ArrayList<Integer> nonISStart = new ArrayList<Integer>();
		ArrayList<Integer> nonISEnd = new ArrayList<Integer>();
		for (int i = 0; i < resultNodes.size(); i++) {
			if (is.getIntersections().contains(villageNodes.get(i))) {
				if (!isIntersection) {
					iSStart.add(i);
				}
				isIntersection = true;
			} else {
				if (isIntersection) {
					iSEnd.add(i - 1);
				}
				isIntersection = false;
			}
		}
		if (iSStart.size() > iSEnd.size()) {
			// Remove false start node
			if (is.getIntersections().contains(villageNodes.get(0))) {

				iSStart.set(0, iSStart.get(iSStart.size() - 1));
				iSStart.remove(iSStart.size() - 1);
			}
			// Add end node if intersection starts right at node 0
			else {
				iSEnd.add(result.size() - 1);
			}
		}
		// We need the non-intersecting nodes
		for (int i = 0; i < iSStart.size(); i++) {
			nonISStart.add((iSEnd.get(i) == result.size() - 1) ? 0 : iSEnd.get(i) + 1);
			int i2 = i == iSStart.size() - 1 ? 0 : i + 1;
			nonISEnd.add((iSStart.get(i2) == 0) ? result.size() - 1 : iSStart.get(i2) - 1);
		}
		
		//Add forbiddenedges
		for (int i = 0; i < is.getForbiddenEdges().size(); i++) {
			nonISStart.add(is.getForbiddenEdges().get(i).getSecond());
			nonISEnd.add(is.getForbiddenEdges().get(i).getFirst());
		}
		Collections.sort(nonISEnd);
		Collections.sort(nonISStart);
		Collections.rotate(nonISEnd, -1);
		
		// New ways
		for (int i = 0; i < nonISStart.size(); i++) {
			ArrayList<CustomNode> copyOfResultNodes = new ArrayList<CustomNode>();
			copyOfResultNodes.addAll(getVillageNodes());
			// Starting node goes first
			Collections.rotate(copyOfResultNodes, -nonISStart.get(i));
			// Cut off the intersecting nodes on the end
			int nonISSize = nonISEnd.get(i) > nonISStart.get(i) ? nonISEnd.get(i) - nonISStart.get(i)
					: copyOfResultNodes.size() + nonISEnd.get(i) - nonISStart.get(i);
			resultNodes = new ArrayList<CustomNode>(copyOfResultNodes.subList(0, nonISSize + 1));
			CustomWay newWay = new CustomWay();
			for (CustomNode cn : resultNodes) {
				newWay.getMembers().add(cn.getNodeId());
			}
			result.add(newWay);
		}
		return result;
	}
}