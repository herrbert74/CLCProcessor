package hu.herrbert74.osm.clcprocessor.helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import hu.herrbert74.osm.clcprocessor.CLCProcessorConstants;
import hu.herrbert74.osm.clcprocessor.dao.CLCNodesHandler;
import hu.herrbert74.osm.clcprocessor.dao.CLCRelationsHandler;
import hu.herrbert74.osm.clcprocessor.dao.CLCWaysHandler;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

public class CLCReader implements CLCProcessorConstants {
	SettlementChooserModel scModel;
	
	public CLCReader(SettlementChooserModel scM){
		scModel = scM;
	}
	
	public void readCLCData() {

		scModel.setStatus("Reading CLC nodes");
		try {
			ArrayList<HashMap<Integer, CustomNode>> list = readNodes(new File(OSM_CLCDATA), scModel.borderPolygon, scModel.neighbourPolygon);
			scModel.clcMainNodes = list.get(0);
			scModel.clcNeighbourNodes = list.get(1);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		scModel.setStatus("Reading CLC ways");
		try {
			scModel.clcMainWays = readWays(new File(OSM_CLCDATA), scModel.clcMainNodes, scModel.clcNeighbourNodes);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		scModel.setStatus("Reading CLC relations");
		try {
			scModel.clcMainRelations = readRelations(new File(OSM_CLCDATA), scModel.clcMainWays);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		addUsedNeighbourNodesToMainNodes();
	}
	
	private void addUsedNeighbourNodesToMainNodes() {
		for (CustomWay vw : scModel.clcMainWays.values()) {
			for (int i = 0; i < vw.getMembers().size(); i++) {
				if (scModel.clcNeighbourNodes.containsKey(vw.getMembers().get(i))) {
					scModel.clcMainNodes.put(vw.getMembers().get(i), scModel.clcNeighbourNodes.get((Integer) vw
							.getMembers().get(i)));
				}
			}
		}

	}
	
	public ArrayList<HashMap<Integer, CustomNode>> readNodes(File file, ArrayList<CustomNode> mainPolygon,
			ArrayList<CustomNode> neighbourPolygon) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		HashMap<Integer, CustomNode> clcMainNodes = new HashMap<Integer, CustomNode>();
		HashMap<Integer, CustomNode> clcNeighbourNodes = new HashMap<Integer, CustomNode>();

		CLCNodesHandler nodesHandler = new CLCNodesHandler(mainPolygon, neighbourPolygon);
		parser.parse(file, nodesHandler);
		clcMainNodes = nodesHandler.getMainNodes();
		clcNeighbourNodes = nodesHandler.getNeighbourNodes();
		ArrayList<HashMap<Integer, CustomNode>> list = new ArrayList<HashMap<Integer, CustomNode>>();
		list.add(clcMainNodes);
		list.add(clcNeighbourNodes);
		return list;
	}

	public HashMap<Integer, CustomWay> readWays(File file, HashMap<Integer, CustomNode> clcMainNodes,
			HashMap<Integer, CustomNode> clcNeighbourNodes) throws ParserConfigurationException, SAXException,
			IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		HashMap<Integer, CustomWay> clcMainWays = new HashMap<Integer, CustomWay>();

		CLCWaysHandler waysHandler = new CLCWaysHandler(clcMainNodes, clcNeighbourNodes);
		parser.parse(file, waysHandler);
		clcMainWays = waysHandler.getWays();

		return clcMainWays;
	}

	public HashMap<Integer, CustomRelation> readRelations(File file, HashMap<Integer, CustomWay> clcMainWays)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		HashMap<Integer, CustomRelation> clcMainRelations = new HashMap<Integer, CustomRelation>();

		CLCRelationsHandler relationsHandler = new CLCRelationsHandler(clcMainWays);
		parser.parse(file, relationsHandler);
		clcMainRelations = relationsHandler.getRelations();

		return clcMainRelations;
	}
}
