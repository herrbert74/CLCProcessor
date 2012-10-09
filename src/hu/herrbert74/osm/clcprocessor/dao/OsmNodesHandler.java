package hu.herrbert74.osm.clcprocessor.dao;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class OsmNodesHandler extends DefaultHandler {
	Map<Integer, CustomNode> villageNodesMap = new HashMap<Integer, CustomNode>();
	private final Stack<String> eleStack = new Stack<String>();
	private CustomNode vn = new CustomNode();

	public Map<Integer, CustomNode> getNodes() {
		return villageNodesMap;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {
		if ("node".equals(qName)) {
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
			villageNodesMap.put((Integer) vn.getNodeId(), vn);
			vn = new CustomNode();
		}

	}
}