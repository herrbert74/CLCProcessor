package hu.herrbert74.osm.clcprocessor.villagepolygon;

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

public class VillagePolygonHelper {
	/*
	 * ArrayList<VillageNode> villageNodes = new ArrayList<VillageNode>();
	 * ArrayList<VillageRelation> villageRelations = new
	 * ArrayList<VillageRelation>(); ArrayList<VillageWay> villageWays = new
	 * ArrayList<VillageWay>();
	 */
	ArrayList<VillagePolygon> villagePolygons = new ArrayList<VillagePolygon>();
	Map<Integer, VillageNode> villageNodesMap = new HashMap<Integer, VillageNode>();
	Map<Integer, VillageWay> villageWaysMap = new HashMap<Integer, VillageWay>();
	Map<String, VillageRelation> villageRelationsMap = new HashMap<String, VillageRelation>();

	public void findWayNames(File file) throws ParserConfigurationException,
			SAXException, IOException {
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

		villagePolygons = createVillagePolygons();
	}

	private ArrayList<VillagePolygon> createVillagePolygons() {
		ArrayList<VillagePolygon> villagePolygons = new ArrayList<VillagePolygon>();
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
				boolean areNodesForward = (lastNode == 0 || vw.members.get(0) == lastNode);
				if (areNodesForward) {
					for (int i = 0; i < vw.members.size(); i++) {
						vp.addVillageNode(villageNodesMap.get(vw.members.get(i)));
						lastNode = vp.villageNodes.get(vp.villageNodes.size() - 1).getNodeId();
					}
				}else {
					for (int i = vw.members.size() -1; i >= 0; i--) {
						vp.addVillageNode(villageNodesMap.get(vw.members.get(i)));
						lastNode = vp.villageNodes.get(vp.villageNodes.size() - 1).getNodeId();
					}
				}
				vp.villageNodes.remove(vp.villageNodes.size()-1);
				vr.members.remove(vr.members.indexOf(vw.getWayId()));
			} while (vr.members.size() > 0);
			villagePolygons.add(vp);
		}
		return villagePolygons;
	}

	private int getNextVillageWay(VillageRelation vr,
			Map<Integer, VillageWay> villageWaysMap2, int lastVillageNode) {
		if (lastVillageNode == 0) {
			return vr.members.get(0);
		} else {
			for (int member : vr.members) {
				if (villageWaysMap2.get(member).members
						.contains(lastVillageNode)) {
					return member;
				}else{
					return vr.members.get(0);
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
				villageNodesMap.put((Integer) vn.nodeId, vn);
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
				villageRelationsMap.put(vr.name, vr);
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
				villageWaysMap.put(vw.id, vw);
				vw = new VillageWay();
			}

		}
	}
}
