package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;
import java.util.HashSet;

public class Intersections {
	HashSet<CustomNode> intersectionNodes;
	/**
	 * Two neighboring intersection nodes does not share the neighbor polygon,
	 * so the stretch between them should be added to the common border!
	 */
	HashSet<CustomNode> twoNodeNonIntersections;
	
	/**
	 * The intersection consists of only two nodes.
	 * No need to be here, can be detected at border creation!
	 */
	//HashSet<CustomNode> twoNodeIntersections;


	public Intersections() {
		intersectionNodes = new HashSet<CustomNode>();
		twoNodeNonIntersections = new HashSet<CustomNode>();
	}

	public HashSet<CustomNode> getTwoNodeNonIntersections() {
		return twoNodeNonIntersections;
	}

	public void setTwoNodeNonIntersections(HashSet<CustomNode> twoNodeNonIntersections) {
		this.twoNodeNonIntersections = twoNodeNonIntersections;
	}

	public void addTwoNodeNonIntersections(HashSet<CustomNode> twoNodeNonIntersections) {
		this.twoNodeNonIntersections.addAll(twoNodeNonIntersections);
	}

	public HashSet<CustomNode> getIntersections() {
		return intersectionNodes;
	}

	public void setIntersections(HashSet<CustomNode> intersections) {
		this.intersectionNodes = intersections;
	}

	public void addIntersections(HashSet<CustomNode> intersections) {
		this.intersectionNodes.addAll(intersections);
	}

	/**
	 * Finds the intersection objects
	 * 
	 * Returns intersection nodes (regardless if they are junctions or in the
	 * middle of a way), start and end nodes, forbidden edges(two node
	 * non-intersections), two node intersections
	 * 
	 * @param polygonList
	 *            An arraylist of CustomPolygons for which we search the
	 *            intersections
	 * @return An intersections object with intersection nodes
	 */
	public Intersections findIntersections(ArrayList<CustomPolygon> polygonList) {
		for (CustomPolygon vp : polygonList) {

			boolean[] isNodes = getIntersectionNodes(polygonList, vp);
			HashSet<CustomNode> nodes = new HashSet<CustomNode>();
			for (int i = 0; i < vp.getVillageNodes().size(); i++) {
				if (isNodes[i]) {
					nodes.add(vp.getVillageNodes().get(i));
				}
			}
			addIntersections(nodes);
			addTwoNodeNonIntersections(getTwoNodeNonIntersections(vp, getStartNodes(isNodes), getEndNodes(isNodes)));
		}
		return this;
	}

	/**
	 * Simply search for intersection nodes
	 * 
	 * @param aggregatePolygonMembers
	 * @param vp
	 * @return This polygon's intersection nodes in a boolean array. True if
	 *         intersection.
	 */
	private boolean[] getIntersectionNodes(ArrayList<CustomPolygon> aggregatePolygonMembers, CustomPolygon vp) {
		boolean[] result = new boolean[vp.getVillageNodes().size()];
		for (int i = 0; i < vp.getVillageNodes().size(); i++) {
			searchIntersection(aggregatePolygonMembers, vp, result, i);
		}
		return result;
	}

	/**
	 * Sub method for intersection node search
	 * 
	 * @param aggregatePolygonMembers
	 * @param vp
	 * @param result
	 * @param i
	 */
	private void searchIntersection(ArrayList<CustomPolygon> aggregatePolygonMembers, CustomPolygon vp, boolean[] result, int i) {
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

	private boolean[] getStartNodes(boolean[] isNodes) {
		boolean[] result = new boolean[isNodes.length];
		// Check first node too
		result[0] = isNodes[0] && !isNodes[isNodes.length - 1] ? true : false;
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
		// Check last node too
		result[isNodes.length - 1] = isNodes[isNodes.length - 1] && !isNodes[0] ? true : false;
		return result;
	}

	// TwoNodeIntersections: just remove the edge, not the nodes from resultset.
	// Have to split the resultset here
	private HashSet<CustomNode> getTwoNodeIntersections(CustomPolygon vp, boolean[] isStart, boolean[] isEnd) {
		HashSet<CustomNode> result = new HashSet<CustomNode>();
		for (int i = 0; i < isStart.length - 1; i++) {
			if (isStart[i] && isEnd[i + 1]) {
				result.add(vp.getVillageNodes().get(i));
				result.add(vp.getVillageNodes().get(i + 1));
			}
		}
		return result;
	}

	private HashSet<CustomNode> getTwoNodeNonIntersections(CustomPolygon vp, boolean[] isStart, boolean[] isEnd) {
		HashSet<CustomNode> result = new HashSet<CustomNode>();
		for (int i = 0; i < isStart.length - 1; i++) {
			if (isStart[i] && isEnd[i + 1]) {
				result.add(vp.getVillageNodes().get(i));
				result.add(vp.getVillageNodes().get(i + 1));
			}
		}
		// Check last node too
		if (isStart[isStart.length - 1] && isEnd[0]) {
			result.add(vp.getVillageNodes().get(isStart.length - 1));
			result.add(vp.getVillageNodes().get(0));
		}
		return result;
	}

}
