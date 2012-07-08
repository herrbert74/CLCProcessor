package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.CLCProcessorConstants;
import hu.herrbert74.osm.clcprocessor.clcpolygon.CLCNode;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.utils.Functions;
import hu.herrbert74.osm.clcprocessor.utils.XMLFactory;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.util.CollectionUtil.Function;

public class SettlementChooserController implements CLCProcessorConstants,
		org.eclipse.swt.events.MouseListener,
		org.eclipse.swt.events.MouseWheelListener,
		org.eclipse.swt.events.MouseMoveListener,
		org.eclipse.swt.events.KeyListener {

	SettlementChooserModel scModel;
	SettlementChooserView scView;

	@Override
	public void mouseScrolled(MouseEvent e) {
	}

	public void addModel(SettlementChooserModel m) {
		this.scModel = m;
	}

	public void addView(SettlementChooserView v) {
		this.scView = v;
	}

	public void initModel() {
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		String dataString = (String) e.widget.getData();

		switch (dataString) {
		case "POLYGONLIST":
			checkNeighbours(scView.polygonList);
			addNeighbours();
			break;
		case "NEIGHBOURLIST":
			checkNeighbours(scView.neighbourList);
			addNeighbours();
			break;
		case "SETTLEMENTLIST":
			int z = scView.settlementList.getItemCount();
			if (scView.settlementList.getItemCount() > scView.settlementList
					.getSelectionIndex()
					&& scView.settlementList.getSelectionIndex() != -1) {
				scView.settlementList.remove(scView.settlementList
						.getSelectionIndex());
				addNeighbours();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {
		String dataString = (String) e.widget.getData();

		switch (dataString) {
		case "ADD":
			checkNeighbours(scView.polygonList);
			addNeighbours();
			break;
		case "REMOVE":
			if (scView.settlementList.getItemCount() > scView.settlementList
					.getSelectionIndex()
					&& scView.settlementList.getSelectionIndex() != -1) {
				scView.settlementList.remove(scView.settlementList
						.getSelectionIndex());
				addNeighbours();
			}
			addNeighbours();
			break;
		case "EXCLUDE":
			if (scView.neighbourList.getItemCount() > scView.neighbourList
					.getSelectionIndex()
					&& scView.neighbourList.getSelectionIndex() != -1) {
				scView.excludedNeighbourList.add(scView.neighbourList
						.getItem(scView.neighbourList.getSelectionIndex()));
				scView.neighbourList.remove(scView.neighbourList
						.getSelectionIndex());
			}
			break;
		case "CREATECLC":
			createCLC();
			break;
		default:
			break;
		}
	}

	private void createCLC() {
		ArrayList<CustomNode> borderPolygon = createBorderPolygon(extractPolygonsForBorder());
		XMLFactory.writePolygon(borderPolygon, "abda_out.osm");
		ArrayList<CustomNode> neighbourPolygon = createBorderPolygon(extractNeighbourPolygon());
		XMLFactory.writePolygon(neighbourPolygon, "abda_neighbours.osm");
		Map<Integer, CustomNode> clcNodes = new HashMap<Integer, CustomNode>();
		try {
			clcNodes = readNodes(new File(OSM_CLCDATA), borderPolygon);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		scView.progressLabel.setText("Created CLC");
		/*
		 * ArrayList<VillageNode> NeighBourBorderPolygon =
		 * createNeighbourBorderPolygon(); findMainPoints();
		 * findNeighbourPoints(); findWays();
		 */
	}

	public Map<Integer, CustomNode> readNodes(File file, ArrayList<CustomNode> polygon)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		Map<Integer, CustomNode> CLCNodesMap = new HashMap<Integer, CustomNode>();

		/*
		 * OsmRelationsHandler relationsHandler = new OsmRelationsHandler();
		 * parser.parse(file, relationsHandler); villageRelationsMap =
		 * relationsHandler.getRelations();
		 * 
		 * OsmWaysHandler waysHandler = new OsmWaysHandler(); parser.parse(file,
		 * waysHandler); villageWaysMap = waysHandler.getWays();
		 */

		CLCNodesHandler nodesHandler = new CLCNodesHandler(polygon);
		parser.parse(file, nodesHandler);
		CLCNodesMap = nodesHandler.getNodes();

		return CLCNodesMap;
	}

	private static class CLCNodesHandler extends DefaultHandler {
		
		ArrayList<CustomNode> polygon;
		Map<Integer, CustomNode> villageNodesMap = new HashMap<Integer, CustomNode>();
		private final ArrayList<CustomNode> villageNodes = new ArrayList<CustomNode>();
		private final Stack<String> eleStack = new Stack<String>();
		private CustomNode vn = new CustomNode();
		boolean nodeSet = false;
		
		public CLCNodesHandler(ArrayList<CustomNode> polygon){
			super();
			this.polygon = polygon;
		}
		
		public Map<Integer, CustomNode> getNodes() {
			return villageNodesMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if ("node".equals(qName) && Functions.isNodeInsidePolygon(polygon, new CustomNode(Double.parseDouble(attrs.getValue("lon")), Double.parseDouble(attrs.getValue("lat"))))) {
				nodeSet = true;
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
			if ("node".equals(qName) && nodeSet) {
				villageNodesMap.put((Integer) vn.getNodeId(), vn);
				vn = new CustomNode();
				nodeSet = false;
			}

		}
	}

	private ArrayList<CustomNode> createBorderPolygon(
			ArrayList<CustomPolygon> aggregatePolygonMembers) {
		ArrayList<CustomNode> intersections = new ArrayList<CustomNode>();
		ArrayList<CustomWay> ways = new ArrayList<CustomWay>();
		intersections = findIntersections(aggregatePolygonMembers);
		for (CustomPolygon vp : aggregatePolygonMembers) {
			ArrayList<CustomWay> splitWays = vp.split(intersections);
			for (CustomWay vw : splitWays) {
				if (vw.getMembers().size() > 2) {
					ways.add(vw);
				}
			}
		}
		ArrayList<CustomNode> result = concatenateWays(ways);
		result.add(result.get(0));
		return result;
	}

	private ArrayList<CustomPolygon> extractPolygonsForBorder() {
		ArrayList<CustomPolygon> aggregatePolygonMembers = new ArrayList<CustomPolygon>();
		for (int i = 0; i < scModel.villagePolygons.size(); i++) {
			for (String village : scView.settlementList.getItems()) {
				if (scModel.villagePolygons.get(i).getName().equals(village)) {
					aggregatePolygonMembers.add(scModel.villagePolygons.get(i));
					// XMLFactory.writePolygon(scModel.villagePolygons.get(i).getVillageNodes(),
					// scModel.villagePolygons.get(i).getName() + ".osm");
				}
			}
		}
		return aggregatePolygonMembers;
	}

	private ArrayList<CustomPolygon> extractNeighbourPolygon() {
		ArrayList<CustomPolygon> result = new ArrayList<CustomPolygon>();
		for (int i = 0; i < scModel.villagePolygons.size(); i++) {
			for (String village : scView.neighbourList.getItems()) {
				if (scModel.villagePolygons.get(i).getName().equals(village)) {
					result.add(scModel.villagePolygons.get(i));
					// XMLFactory.writePolygon(scModel.villagePolygons.get(i).getVillageNodes(),
					// scModel.villagePolygons.get(i).getName() + ".osm");
				}
			}
		}
		return result;
	}

	private ArrayList<CustomNode> concatenateWays(ArrayList<CustomWay> ways) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		do {
			if (result.size() == 0) {
				for (int i = 0; i < ways.get(0).getMembers().size(); i++) {
					result.add(scModel.villageNodesMap.get(ways.get(0)
							.getMembers().get(i)));
				}
				ways.remove(0);
			} else {
				int i = -1;
				CustomNode lastNode = result.get(result.size() - 1);
				do {
					i++;
					if (ways.get(i).containsNode(lastNode)) {
						result.remove(result.size() - 1);
						result.addAll(getVillageNodes(ways.get(i)));
					}
				} while (!ways.get(i).containsNode(lastNode));
				ways.remove(i);
			}
		} while (ways.size() > 0);
		return result;
	}

	private Collection<? extends CustomNode> getVillageNodes(
			CustomWay villageWay) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		for (int vnID : villageWay.getMembers()) {
			result.add(scModel.villageNodesMap.get(vnID));
		}
		return result;
	}

	private ArrayList<CustomNode> findIntersections(
			ArrayList<CustomPolygon> aggregatePolygonMembers) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		for (CustomPolygon vp : aggregatePolygonMembers) {
			for (CustomNode vn : vp.getVillageNodes()) {
				boolean isThisNodeUnique = true;
				for (CustomPolygon vpToCompare : aggregatePolygonMembers) {
					if (!vp.equals(vpToCompare)) {
						for (CustomNode vnToCompare : vpToCompare
								.getVillageNodes()) {
							if (vn.getNodeId() == vnToCompare.getNodeId()) {
								isThisNodeUnique = false;
							}
						}
					}
				}
				if (!isThisNodeUnique && !result.contains(vn)) {
					result.add(vn);
				}
			}
		}
		return result;
	}

	private void checkNeighbours(List list) {
		String addition = list.getItem(list.getSelectionIndex());
		boolean ok = true;
		if (scView.settlementList.getItems().length > 0) {
			for (String s : scView.settlementList.getItems()) {
				if (s.equals(addition)) {
					ok = false;
				}
			}
		}
		if (ok) {
			scView.settlementList.add(addition);
		}
	}

	private void addNeighbours() {
		scView.neighbourList.removeAll();
		for (String z : scView.settlementList.getItems()) {
			CustomPolygon vpFound = new CustomPolygon();
			for (CustomPolygon vp : scModel.villagePolygons) {
				if (vp.getName().equals(z)) {
					vpFound = vp;
				}
			}
			for (CustomNode vn : vpFound.getVillageNodes()) {
				for (CustomPolygon vp : scModel.villagePolygons) {
					if (vp.getVillageNodes().contains(vn)
							&& scView.settlementList.indexOf(vp.getName()) == -1
							&& scView.neighbourList.indexOf(vp.getName()) == -1
							&& scView.excludedNeighbourList.indexOf(vp
									.getName()) == -1) {
						scView.neighbourList.add(vp.getName());
					}
				}
			}
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}

	@Override
	public void mouseMove(MouseEvent e) {
		String dataString = (String) e.widget.getData();

		switch (dataString) {
		case "POLYGONLIST":
			scView.polygonList.setFocus();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		String searched = scView.searchText.getText().toLowerCase();
		for (int i = 0; i < scView.polygonList.getItemCount(); i++) {
			String trimmedSearched = searched;
			String lookup;
			try {
				lookup = scView.polygonList.getItem(i)
						.substring(0, trimmedSearched.length()).toLowerCase();
			} catch (StringIndexOutOfBoundsException sioe) {
				trimmedSearched = trimmedSearched.substring(0,
						scView.polygonList.getItem(i).length());
				lookup = scView.polygonList.getItem(i)
						.substring(0, trimmedSearched.length()).toLowerCase();
			}
			if (trimmedSearched.equals(lookup)) {
				int listItemHeight = scView.polygonList.computeSize(
						SWT.DEFAULT, SWT.DEFAULT).y
						/ scView.polygonList.getItemCount();
				scView.polygonList.setSelection(i);
				scView.polygonListSC.setOrigin(
						scView.polygonListSC.getOrigin().x, listItemHeight * i);
				break;
			}
		}
	}
}
