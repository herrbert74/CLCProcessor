package hu.herrbert74.osm.clcprocessor.models;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelationMember;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SettlementChooserModel extends java.util.Observable {
	public ArrayList<CustomPolygon> villagePolygons = new ArrayList<CustomPolygon>();
	public Map<Integer, CustomNode> villageNodesMap = new HashMap<Integer, CustomNode>();
	Map<Integer, CustomWay> villageWaysMap = new HashMap<Integer, CustomWay>();
	Map<String, CustomRelation> villageRelationsMap = new HashMap<String, CustomRelation>();

	public void read(File file)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();

		OsmRelationsHandler relationsHandler = new OsmRelationsHandler();
		parser.parse(file, relationsHandler);
		villageRelationsMap = relationsHandler.getRelations();

		OsmWaysHandler waysHandler = new OsmWaysHandler();
		parser.parse(file, waysHandler);
		villageWaysMap = waysHandler.getWays();

		OsmNodesHandler nodesHandler = new OsmNodesHandler();
		parser.parse(file, nodesHandler);
		villageNodesMap = nodesHandler.getNodes();

		createVillagePolygons();
	}

	private void createVillagePolygons() {
		int z = 0;
		for (CustomRelation vr : villageRelationsMap.values()) {
			System.out.println("polygon: " + Integer.toString(++z) + "/"
					+ Integer.toString(villageRelationsMap.size()));
			CustomPolygon vp = new CustomPolygon();
			vp.setName(vr.getName());
			if(vr.getName().equals("Dunaszentpál")){
				vp.setName(vr.getName());
			}
			CustomNode firstNode = new CustomNode();
			int lastNode = 0;
			do {
				int way = getNextVillageWay(vr, villageWaysMap, lastNode);
				CustomWay vw = villageWaysMap.get(way);
				boolean areNodesForward = (lastNode == 0 || vw.getMembers().get(0) == lastNode);
				if (areNodesForward) {
					for (int i = 0; i < vw.getMembers().size(); i++) {
						if(firstNode.getNodeId() == 0){
							firstNode = villageNodesMap.get(vw.getMembers().get(i));
						}
						vp.addVillageNode(villageNodesMap.get(vw.getMembers().get(i)));
						lastNode = vp.getVillageNodes().get(
								vp.getVillageNodes().size() - 1).getNodeId();
					}
				} else {
					for (int i = vw.getMembers().size() - 1; i >= 0; i--) {
						if(firstNode.getNodeId() == 0){
							firstNode = villageNodesMap.get(vw.getMembers().get(i));
						}
						vp.addVillageNode(villageNodesMap.get(vw.getMembers().get(i)));
						lastNode = vp.getVillageNodes().get(
								vp.getVillageNodes().size() - 1).getNodeId();
					}
				}
				vp.getVillageNodes().remove(vp.getVillageNodes().size() - 1);
				vr.getMembers().remove(vr.getMembers().indexOf(vr.getMemberWithWayId(vw.getWayId())));
			} while (vr.getMembers().size() > 0);
			vp.addVillageNode(firstNode);
			villagePolygons.add(vp);
		}
		setChanged();
		notifyObservers(villagePolygons);
	}

	private int getNextVillageWay(CustomRelation vr,
			Map<Integer, CustomWay> villageWaysMap2, int lastVillageNode) {
		if (lastVillageNode == 0) {
			return vr.getMembers().get(0).getRef();
		} else {
			for (CustomRelationMember member : vr.getMembers()) {
				if (villageWaysMap2.get(member.getRef()).getMembers()
						.contains(lastVillageNode)) {
					return member.getRef();
				}
			}
			//Return the first member if nothing found. Have to be an exclave!
			return vr.getMembers().get(0).getRef();
		}
	}

	private static class OsmNodesHandler extends DefaultHandler {
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

	private static class OsmRelationsHandler extends DefaultHandler {
		Map<String, CustomRelation> villageRelationsMap = new HashMap<String, CustomRelation>();
		private final Stack<String> eleStack = new Stack<String>();
		private CustomRelation vr = new CustomRelation();

		public Map<String, CustomRelation> getRelations() {
			return villageRelationsMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if ("tag".equals(qName) && "relation".equals(eleStack.peek())) {
				String key = attrs.getValue("k");
				if ("name".equals(key)) {
					vr.setName(attrs.getValue("v"));
				}
			}
			if ("member".equals(qName) && "relation".equals(eleStack.peek())) {
				String key = attrs.getValue("type");
				if ("way".equals(key)) {
					vr.addMember(new CustomRelationMember("way", Integer.parseInt(attrs.getValue("ref")), "outer"));
				}
			}
			eleStack.push(qName);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			eleStack.pop();
			if ("relation".equals(qName)) {
				villageRelationsMap.put(vr.getName(), vr);
				vr = new CustomRelation();
			}
		}
	}

	private static class OsmWaysHandler extends DefaultHandler {
		Map<Integer, CustomWay> villageWaysMap = new HashMap<Integer, CustomWay>();
		private final Stack<String> eleStack = new Stack<String>();
		private CustomWay vw = new CustomWay();

		public Map<Integer, CustomWay> getWays() {
			return villageWaysMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if ("way".equals(qName)) {
				vw.setWayId(Integer.parseInt(attrs.getValue("id")));

			}
			if ("nd".equals(qName) && "way".equals(eleStack.peek())) {
				vw.addMember(Integer.parseInt(attrs.getValue("ref")));
			}
			eleStack.push(qName);

		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			eleStack.pop();
			if ("way".equals(qName)) {
				villageWaysMap.put(vw.getWayId(), vw);
				vw = new CustomWay();
			}

		}
	}
}
