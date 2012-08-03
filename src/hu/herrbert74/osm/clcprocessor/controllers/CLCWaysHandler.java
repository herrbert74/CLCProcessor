package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class CLCWaysHandler extends DefaultHandler {

	HashMap<Integer, CustomNode> clcMainNodes = new HashMap<Integer, CustomNode>();
	HashMap<Integer, CustomNode> clcNeighbourNodes = new HashMap<Integer, CustomNode>();
	HashMap<Integer, CustomWay> clcMainWays = new HashMap<Integer, CustomWay>();
	private final Stack<String> eleStack = new Stack<String>();
	private CustomWay vw = new CustomWay();
	boolean isWayMember = false;

	public CLCWaysHandler(HashMap<Integer, CustomNode> clcMainNodes,
			HashMap<Integer, CustomNode> clcNeighbourNodes) {
		super();
		this.clcMainNodes = clcMainNodes;
		this.clcNeighbourNodes = clcNeighbourNodes;
	}

	// Return updated nodesMap
	public HashMap<Integer, CustomNode> getNodes() {
		return clcMainNodes;
	}

	public HashMap<Integer, CustomWay> getWays() {
		return clcMainWays;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {
		if ("way".equals(qName)) {
			vw.setWayId(Integer.parseInt(attrs.getValue("id")));
		}
		if ("nd".equals(qName) && "way".equals(eleStack.peek())) {
			if (clcMainNodes
					.containsKey(Integer.parseInt(attrs.getValue("ref")))
					|| clcNeighbourNodes.containsKey(Integer.parseInt(attrs
							.getValue("ref")))) {
				vw.addMember(Integer.parseInt(attrs.getValue("ref")));
				if (clcMainNodes.containsKey(Integer.parseInt(attrs.getValue("ref")))){
					isWayMember = true;
				}
			}
		}
		if ("tag".equals(qName) && "way".equals(eleStack.peek())) {
			vw.addTag(attrs.getValue("k"), attrs.getValue("v"));
		}
		eleStack.push(qName);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		eleStack.pop();
		if ("way".equals(qName)) {
			if (isWayMember) {
				clcMainWays.put(vw.getWayId(), vw);
			}
			vw = new CustomWay();
			isWayMember = false;
		}
	}
}