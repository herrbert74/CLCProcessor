package hu.herrbert74.osm.clcprocessor.models;

import hu.herrbert74.osm.clcprocessor.villagepolygon.VillageNode;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillagePolygon;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillageRelation;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillageWay;

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
	public ArrayList<VillagePolygon> villagePolygons = new ArrayList<VillagePolygon>();
	public Map<Integer, VillageNode> villageNodesMap = new HashMap<Integer, VillageNode>();
	Map<Integer, VillageWay> villageWaysMap = new HashMap<Integer, VillageWay>();
	Map<String, VillageRelation> villageRelationsMap = new HashMap<String, VillageRelation>();

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
		for (VillageRelation vr : villageRelationsMap.values()) {
			System.out.println("polygon: " + Integer.toString(++z) + "/"
					+ Integer.toString(villageRelationsMap.size()));
			VillagePolygon vp = new VillagePolygon();
			vp.setName(vr.getName());
			int lastNode = 0;
			do {
				int way = getNextVillageWay(vr, villageWaysMap, lastNode);
				VillageWay vw = villageWaysMap.get(way);
				boolean areNodesForward = (lastNode == 0 || vw.getMembers().get(0) == lastNode);
				if (areNodesForward) {
					for (int i = 0; i < vw.getMembers().size(); i++) {
						vp.addVillageNode(villageNodesMap.get(vw.getMembers().get(i)));
						lastNode = vp.getVillageNodes().get(
								vp.getVillageNodes().size() - 1).getNodeId();
					}
				} else {
					for (int i = vw.getMembers().size() - 1; i >= 0; i--) {
						vp.addVillageNode(villageNodesMap.get(vw.getMembers().get(i)));
						lastNode = vp.getVillageNodes().get(
								vp.getVillageNodes().size() - 1).getNodeId();
					}
				}
				vp.getVillageNodes().remove(vp.getVillageNodes().size() - 1);
				vr.getMembers().remove(vr.getMembers().indexOf(vw.getWayId()));
			} while (vr.getMembers().size() > 0);
			villagePolygons.add(vp);
		}
		setChanged();
		notifyObservers(villagePolygons);
	}

	private int getNextVillageWay(VillageRelation vr,
			Map<Integer, VillageWay> villageWaysMap2, int lastVillageNode) {
		if (lastVillageNode == 0) {
			return vr.getMembers().get(0);
		} else {
			for (int member : vr.getMembers()) {
				if (villageWaysMap2.get(member).getMembers()
						.contains(lastVillageNode)) {
					return member;
				} else {
					return vr.getMembers().get(0);
				}
			}
		}
		return 0;
	}

	private static class OsmNodesHandler extends DefaultHandler {
		Map<Integer, VillageNode> villageNodesMap = new HashMap<Integer, VillageNode>();
		private final ArrayList<VillageNode> villageNodes = new ArrayList<VillageNode>();
		private final Stack<String> eleStack = new Stack<String>();
		private VillageNode vn = new VillageNode();

		public Map<Integer, VillageNode> getNodes() {
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
				vn = new VillageNode();
			}

		}
	}

	private static class OsmRelationsHandler extends DefaultHandler {
		Map<String, VillageRelation> villageRelationsMap = new HashMap<String, VillageRelation>();
		private final ArrayList<VillageRelation> villageRelations = new ArrayList<VillageRelation>();
		private final Stack<String> eleStack = new Stack<String>();
		private VillageRelation vr = new VillageRelation();

		public Map<String, VillageRelation> getRelations() {
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
					vr.addMember(Integer.parseInt(attrs.getValue("ref")));
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
				vr = new VillageRelation();
			}
		}
	}

	private static class OsmWaysHandler extends DefaultHandler {
		Map<Integer, VillageWay> villageWaysMap = new HashMap<Integer, VillageWay>();
		private final ArrayList<VillageWay> villageWays = new ArrayList<VillageWay>();
		private final Stack<String> eleStack = new Stack<String>();
		private VillageWay vw = new VillageWay();

		public Map<Integer, VillageWay> getWays() {
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
				vw = new VillageWay();
			}

		}
	}
}
