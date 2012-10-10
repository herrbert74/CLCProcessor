package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;
import java.util.HashSet;

public class Intersections {
	HashSet<CustomNode> intersections;
	HashSet<CustomNode> startsAndEnds;
	HashSet<CustomNode> twoNodeIntersections;
	ArrayList<NodePair> forbiddenEdges;

	public Intersections() {
		intersections = new HashSet<CustomNode>();
		forbiddenEdges = new ArrayList<NodePair>();
		startsAndEnds = new HashSet<CustomNode>();
		twoNodeIntersections = new HashSet<CustomNode>();
	}

	public HashSet<CustomNode> getTwoNodeIntersections() {
		return twoNodeIntersections;
	}

	public void setTwoNodeIntersections(HashSet<CustomNode> twoNodeIntersections) {
		this.twoNodeIntersections = twoNodeIntersections;
	}

	public void addTwoNodeIntersections(HashSet<CustomNode> twoNodeIntersections) {
		this.twoNodeIntersections.addAll(twoNodeIntersections);
	}
	
	public HashSet<CustomNode> getIntersections() {
		return intersections;
	}

	public void setIntersections(HashSet<CustomNode> intersections) {
		this.intersections = intersections;
	}

	public void addIntersections(HashSet<CustomNode> intersections) {
		this.intersections.addAll(intersections);
	}

	public ArrayList<NodePair> getForbiddenEdges() {
		return forbiddenEdges;
	}

	public void setForbiddenEdges(ArrayList<NodePair> forbiddenEdges) {
		this.forbiddenEdges = forbiddenEdges;
	}

	public void addForbiddenEdges(ArrayList<NodePair> forbiddenEdges) {
		this.forbiddenEdges.addAll(forbiddenEdges);
	}

	public void addStartsAndEnds(CustomPolygon vp, boolean[] isStart, boolean[] isEnd) {
		for (int i = 0; i < vp.getVillageNodes().size() - 1; i++) {
			if (isStart[i] || isEnd[i]) {
				startsAndEnds.add(vp.getVillageNodes().get(i));
			}
		}
	}

	public void unsetStartsAndEnds() {
		intersections.removeAll(startsAndEnds);
		
	}
}
