package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.utils.Functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class CLCNodesHandler extends DefaultHandler {

	ArrayList<CustomNode> mainPolygon;
	ArrayList<CustomNode> neighbourPolygon;
	HashMap<Integer, CustomNode> clcMainNodes = new HashMap<Integer, CustomNode>();
	HashMap<Integer, CustomNode> clcNeighbourNodes = new HashMap<Integer, CustomNode>();
	private final Stack<String> eleStack = new Stack<String>();
	private CustomNode vn = new CustomNode();
	boolean mainNodeSet = false;
	boolean neighbourNodeSet = false;

	public CLCNodesHandler(ArrayList<CustomNode> mainPolygon,
			ArrayList<CustomNode> neighbourPolygon) {
		super();
		this.mainPolygon = mainPolygon;
		this.neighbourPolygon = neighbourPolygon;
	}

	public HashMap<Integer, CustomNode> getMainNodes() {
		return clcMainNodes;
	}

	public HashMap<Integer, CustomNode> getNeighbourNodes() {
		return clcNeighbourNodes;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {
		if ("node".equals(qName)
				&& Functions.isNodeInsidePolygon(
						mainPolygon,
						new CustomNode(
								Double.parseDouble(attrs.getValue("lon")),
								Double.parseDouble(attrs.getValue("lat"))))) {
			mainNodeSet = true;
			vn.setNodeId(Integer.parseInt(attrs.getValue("id")));
			vn.setLat(Double.parseDouble(attrs.getValue("lat")));
			vn.setLon(Double.parseDouble(attrs.getValue("lon")));
		} else if ("node".equals(qName)
				&& Functions.isNodeInsidePolygon(
						neighbourPolygon,
						new CustomNode(
								Double.parseDouble(attrs.getValue("lon")),
								Double.parseDouble(attrs.getValue("lat"))))) {
			neighbourNodeSet = true;
			vn.setNodeId(Integer.parseInt(attrs.getValue("id")));
			vn.setLat(Double.parseDouble(attrs.getValue("lat")));
			vn.setLon(Double.parseDouble(attrs.getValue("lon")));
		}

		eleStack.push(qName);

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		eleStack.pop();
		if ("node".equals(qName)) {
			if (mainNodeSet) {
				clcMainNodes.put((Integer) vn.getNodeId(), vn);
				
			}
			else if (neighbourNodeSet) {
				clcNeighbourNodes.put((Integer) vn.getNodeId(), vn);
				
			}
			
			vn = new CustomNode();
			mainNodeSet = false;
			neighbourNodeSet = false;
		}

	}
}