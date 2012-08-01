package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.CLCProcessorConstants;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelationMember;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.osmentities.NodePair;
import hu.herrbert74.osm.clcprocessor.osmentities.WayPair;
import hu.herrbert74.osm.clcprocessor.utils.Functions;
import hu.herrbert74.osm.clcprocessor.utils.XMLFactory;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class CLCCreator implements CLCProcessorConstants {

	SettlementChooserModel scModel;
	SettlementChooserView scView;
	HashMap<Integer, CustomNode> clcMainNodes = new HashMap<Integer, CustomNode>();
	HashMap<Integer, CustomNode> clcNeighbourNodes = new HashMap<Integer, CustomNode>();
	HashMap<Integer, CustomWay> clcMainWays = new HashMap<Integer, CustomWay>();
	HashMap<Integer, CustomRelation> clcMainRelations = new HashMap<Integer, CustomRelation>();
	ArrayList<WayPair> wayPairList = new ArrayList<WayPair>();

	public void createCLC(SettlementChooserModel scModel, SettlementChooserView scView) {
		this.scView = scView;
		this.scModel = scModel;
		ArrayList<CustomNode> borderPolygon = createBorderPolygon(extractPolygonsForBorder());
		XMLFactory.writePolygon(borderPolygon, "abda_out.osm");
		ArrayList<CustomNode> neighbourPolygon = createBorderPolygon(extractNeighbourPolygon());
		XMLFactory.writePolygon(neighbourPolygon, "abda_neighbours.osm");
		System.out.println("Reading nodes");
		try {
			ArrayList<HashMap<Integer, CustomNode>> list = readNodes(new File(OSM_CLCDATA), borderPolygon, neighbourPolygon);
			clcMainNodes = list.get(0);
			clcNeighbourNodes = list.get(1);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("Reading ways");
		try {
			clcMainWays = readWays(new File(OSM_CLCDATA), clcMainNodes, clcNeighbourNodes);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("Reading relations");
		try {
			clcMainRelations = readRelations(new File(OSM_CLCDATA), clcMainWays);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		convertPureWaysToRelations();
		createWayPairs();
		splitWaysAndUpdateRelations();
		XMLFactory.writeOSM(clcMainNodes, clcMainWays, clcMainRelations, "clc_out.osm");
	}

	private void convertPureWaysToRelations() {
		boolean pureWay;
		int newRelationId = -5000000;
		for (CustomWay cw : clcMainWays.values()) {
			pureWay = true;
			for (CustomRelation cr : clcMainRelations.values()) {
				for (CustomRelationMember crm : cr.getMembers()) {
					if (crm.getRef() == cw.getWayId()) {
						pureWay = false;
					}
				}
			}
			if (pureWay) {
				newRelationId--;
				CustomRelation cr = new CustomRelation(new CustomRelationMember("way", cw.getWayId(), "outer"),
						newRelationId);
				Iterator<Map.Entry<String, String>> it = cw.getTags().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
					cr.addTag(pairs.getKey(), pairs.getValue());
				}
				cr.addTag("type", "multipolygon");
				clcMainRelations.put(newRelationId, cr);
			}
		}
	}

	private void createWayPairs() {

		for (CustomRelation cr : clcMainRelations.values()) {
			for (int i = 0; i < cr.getMembers().size(); i++) {
				if (cr.getMembers().get(i).getRole().equals("outer")) {
					for (CustomRelation otherCr : clcMainRelations.values()) {
						/*
						 * System.out.println(
						 * "Checking way and relation for WayPair: " +
						 * Integer.toString(cr.getMembers().get(i).getRef()) +
						 * " relation: " +
						 * Integer.toString(otherCr.getRelationId()));
						 */
						int overLappingWayId = otherCr
								.getOverLappingMember(cr.getMembers().get(i).getRef(), clcMainWays, clcMainNodes);
						if (overLappingWayId != -1) {
							WayPair w = new WayPair(cr.getMembers().get(i).getRef(), overLappingWayId);
							if (!wayPairList.contains(w)) {
								System.out.println("Added WayPair: "
										+ Integer.toString(cr.getMembers().get(i).getRef())
										+ Integer.toString(overLappingWayId));
								wayPairList.add(w);
							} else {
								System.out.println("Discarded WayPair: "
										+ Integer.toString(cr.getMembers().get(i).getRef())
										+ Integer.toString(overLappingWayId));
							}
						}
					}
				}
			}
		}
	}

	private void splitWaysAndUpdateRelations() {
		HashSet<Integer> affectedWays = new HashSet<Integer>();
		ArrayList<Integer> fullyReplacedWays = new ArrayList<Integer>();
		int newWayId = -6000000;
		for (WayPair w : wayPairList) {
			affectedWays.add(w.getFirst());
			affectedWays.add(w.getSecond());
			determineJunctions(w);
		}

		Iterator<Integer> affectedIterator = affectedWays.iterator();
		while (affectedIterator.hasNext()) {
			int i = (int) affectedIterator.next();
			System.out.println("Split way: " + Integer.toString(i));

			ArrayList<NodePair> startEndPairs = new ArrayList<NodePair>();
			ArrayList<NodePair> compactedStartEndPairs = new ArrayList<NodePair>();
			for (WayPair w : wayPairList) {
				for (int i2 = 0; i2 < w.getNumberOfWays(); i2++) {
					if (w.getFirst() == i) {

						startEndPairs.add(new NodePair(w.getStartA(i2), w.getEndA(i2)));
					}
					if (w.getSecond() == i) {
						startEndPairs.add(new NodePair(w.getStartB(i2), w.getEndB(i2)));
					}
				}
			}
			Collections.sort(startEndPairs);
			compactedStartEndPairs.add(new NodePair(startEndPairs.get(0).getFirst(), startEndPairs.get(0).getSecond()));
			// Compact it!
			for (int i2 = 1; i2 < startEndPairs.size(); i2++) {
				if (startEndPairs.get(i2 - 1).getSecond() == startEndPairs.get(i2).getFirst()) {
					compactedStartEndPairs.get(compactedStartEndPairs.size() - 1).setSecond(startEndPairs.get(i2)
							.getSecond());
				} else {
					compactedStartEndPairs.add(new NodePair(startEndPairs.get(i2).getFirst(), startEndPairs.get(i2)
							.getSecond()));
				}
			}
			// Compact rotated pairs
			if (compactedStartEndPairs.get(compactedStartEndPairs.size() - 1).getSecond() == compactedStartEndPairs
					.get(0).getFirst() && compactedStartEndPairs.size() > 1) {
				compactedStartEndPairs.get(0).setFirst(compactedStartEndPairs.get(compactedStartEndPairs.size() - 1)
						.getFirst());
				compactedStartEndPairs.remove(compactedStartEndPairs.size() - 1);
			}
			Collections.sort(compactedStartEndPairs);
			// Remove duplicate stretches, add unpaired, clipped ways
			CustomWay affectedWay = clcMainWays.get((Integer) i);

			if (compactedStartEndPairs.size() == 1
					&& compactedStartEndPairs.get(0).getFirst() == compactedStartEndPairs.get(0).getSecond()) {
				fullyReplacedWays.add(i);
			} else if (!affectedWay.isFullRound()) {
				// New ways
				for (int i2 = 0; i2 < compactedStartEndPairs.size(); i2++) {
					CustomWay newWay = new CustomWay();
					if (i2 == compactedStartEndPairs.size() - 1) {
						newWay.addMembers(affectedWay.getMembers()
								.subList(compactedStartEndPairs.get(i2).getSecond(), affectedWay.getMembers().size()));
					} else {
						newWay.addMembers(affectedWay.getMembers()
								.subList(compactedStartEndPairs.get(i2).getSecond(), compactedStartEndPairs.get(i2 + 1)
										.getFirst() + 1));
					}
					if (!(i2 == 0 && compactedStartEndPairs.get(0).getFirst() == 0)) {
						newWay.setWayId(newWayId);
						clcMainWays.put(newWayId, newWay);
						clcMainRelations
								.get(Functions.getParentRelation(clcMainWays.get(affectedWay.getWayId()), clcMainRelations))
								.addMember(new CustomRelationMember("way", newWayId, "outer"));
						newWayId--;
					}
				}
				// Remove stretches
				int newStart = compactedStartEndPairs.get(0).getFirst() == 0 ? compactedStartEndPairs.get(0)
						.getSecond() : 0;
				int newEnd;
				if (compactedStartEndPairs.get(0).getFirst() == 0 && compactedStartEndPairs.size() == 1) {
					newEnd = affectedWay.getMembers().size();
				} else if (compactedStartEndPairs.get(0).getFirst() == 0 && compactedStartEndPairs.size() > 1) {
					newEnd = compactedStartEndPairs.get(1).getFirst() + 1;
				} else {
					newEnd = compactedStartEndPairs.get(0).getFirst() + 1;
				}
				affectedWay.setMembers(affectedWay.getMembers().subList(newStart, newEnd));

			} else {
				// New ways
				for (int i2 = 0; i2 < compactedStartEndPairs.size() - 1; i2++) {
					CustomWay newWay = new CustomWay();
					int start = compactedStartEndPairs.get(i2).getSecond();
					int end = compactedStartEndPairs.get(i2 + 1).getFirst() + 1;
					newWay.addMembers(affectedWay.getMembers().subList(start, end));
					newWay.setWayId(newWayId);
					clcMainWays.put(newWayId, newWay);
					int parent = Functions.getParentRelation(clcMainWays.get(affectedWay.getWayId()), clcMainRelations);
					clcMainRelations.get((Integer) parent)
							.addMember(new CustomRelationMember("way", newWayId, "outer"));
					newWayId--;
				}
				// Reduce affected way
				// Get a sublist if affectedWay stars with commonWay
				NodePair last = compactedStartEndPairs.get(compactedStartEndPairs.size() - 1);
				NodePair first = new NodePair(last.getSecond(), compactedStartEndPairs.get(0).getFirst());
				if (last.getFirst() > last.getSecond()) {
					affectedWay.setMembers(affectedWay.getMembers().subList(first.getFirst(), first.getSecond() + 1));
				}
				// Remove all other parts and rotate
				else {
					affectedWay.getMembers().removeAll(affectedWay.getMembers().subList(compactedStartEndPairs.get(0)
							.getFirst() + 1, last.getSecond()));
					Collections.rotate(affectedWay.getMembers(), -compactedStartEndPairs.get(0).getFirst() - 1);
				}
			}
		}
		// Add way to collections
		for (WayPair w : wayPairList) {
			for (int i = 0; i < w.getNumberOfWays(); i++) {
				w.setNewWayId(newWayId, i);
				clcMainWays.put(newWayId, w.getNewWay(i));
				clcMainRelations.get(Functions.getParentRelation(clcMainWays.get(w.getFirst()), clcMainRelations))
						.addMember(new CustomRelationMember("way", newWayId, "outer"));
				clcMainRelations.get(Functions.getParentRelation(clcMainWays.get(w.getSecond()), clcMainRelations))
						.addMember(new CustomRelationMember("way", newWayId, "outer"));
				newWayId--;
			}
		}
		// Remove fully Replaced ways
		for (Integer id : fullyReplacedWays) {
			CustomRelation cr = clcMainRelations
					.get(Functions.getParentRelation(clcMainWays.get(id), clcMainRelations));
			cr.removeMemberWithWayId(id);
			clcMainWays.remove((Integer) id);
		}
	}

	public void determineJunctions(WayPair w) {
		CustomWay cwA = clcMainWays.get(w.getFirst());
		CustomWay cwB = clcMainWays.get(w.getSecond());
		System.out.println("Determine junctions: " + Integer.toString(cwA.getWayId()) + " - "
				+ Integer.toString(cwB.getWayId()));
		boolean isCommon = false;

		// remove last (duplicate) node from circular ways
		if ((int) cwA.getMembers().get(0) == (int) cwA.getMembers().get(cwA.getMembers().size() - 1)) {
			cwA.getMembers().remove(cwA.getMembers().size() - 1);
			cwA.setFullRound(true);
		}
		if ((int) cwB.getMembers().get(0) == (int) cwB.getMembers().get(cwB.getMembers().size() - 1)) {
			cwB.getMembers().remove(cwB.getMembers().size() - 1);
			cwB.setFullRound(true);
		}

		// determine junctions
		boolean isBReversed = false;
		int posInBPrev = -1;
		for (int i = 0; i < cwA.getMembers().size(); i++) {
			int posInB = cwB.containsNode(cwA.getMembers().get(i));

			if (posInB >= 0) {
				if (!isCommon) {
					w.setNumberOfWays(w.getNumberOfWays() + 1);
					w.addStartA(i);
					w.addStartB(posInB);
				}
				if (posInBPrev > -1) {
					if (posInBPrev > posInB) {
						isBReversed = true;
					} else {
						isBReversed = false;
					}
				}
				isCommon = true;
				posInBPrev = posInB;
			} else {
				if (isCommon) {
					w.addEndA(i - 1);
					w.addEndB(posInBPrev);
				}
				isCommon = false;
				posInBPrev = -1;
			}
		}
		if (isCommon) {
			// if (!cwA.isFullRound()) {
			if (w.getEndASize() < w.getNumberOfWays()) {
				w.addEndA(cwA.getMembers().size() - 1);
			}
			/*
			 * if (!cwB.isFullRound()) { w.addEndB(isBReversed ? 0 :
			 * cwB.getMembers().size() - 1); } else
			 */

			if (w.getEndBSize() < w.getNumberOfWays()) {
				w.addEndB(posInBPrev);
			}
			if (w.getStartA(0) == 0) {
				w.setStartA(w.getStartA(w.getNumberOfWays() - 1), 0);
				w.setStartB(w.getStartB(w.getNumberOfWays() - 1), 0);
				w.setNumberOfWays(w.getNumberOfWays() - 1);
			}
		}
		setWayPairData(w, cwA, cwB, isBReversed);
	}

	private void setWayPairData(WayPair w, CustomWay cwA, CustomWay cwB, boolean isBReversed) {
		// Swap start and end if needed
		if (isBReversed) {
			for (int i = 0; i < w.getNumberOfWays(); i++) {
				int temp = w.getEndB(i);
				w.setEndB(w.getStartB(i), i);
				w.setStartB(temp, i);
			}
		}
		// Create new way
		for (int i = 0; i < w.getNumberOfWays(); i++) {
			CustomWay newWay = new CustomWay();
			if (w.getStartA(i) < w.getEndA(i)) {
				newWay.getMembers().addAll(cwA.getMembers().subList(w.getStartA(i), w.getEndA(i) + 1));
			} else {
				newWay.getMembers().addAll(cwA.getMembers().subList(w.getStartA(i), cwA.getMembers().size()));
				newWay.getMembers().addAll(cwA.getMembers().subList(0, w.getEndA(i) + 1));
			}
			w.addNewWay(newWay);
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

	private ArrayList<CustomNode> createBorderPolygon(ArrayList<CustomPolygon> aggregatePolygonMembers) {
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
					result.add(scModel.villageNodesMap.get(ways.get(0).getMembers().get(i)));
				}
				System.out.println("Add first way");
				System.out.println("Add way starting with : " + Integer.toString(ways.get(0).getMembers().get(0)));
				System.out.println("ending with with : "
						+ Integer.toString(ways.get(0).getMembers().get(ways.get(0).getMembers().size() - 1)));
				ways.remove(0);
			} else {
				int i = -1;
				CustomNode lastNode = result.get(result.size() - 1);
				do {
					i++;
					if (ways.get(i).containsNode(lastNode.getNodeId()) >= 0) {
						result.remove(result.size() - 1);
						result.addAll(getVillageNodes(ways.get(i), lastNode));
						System.out.println("Add way starting with : "
								+ Integer.toString(ways.get(i).getMembers().get(i)));
						System.out.println("ending with with : "
								+ Integer.toString(ways.get(i).getMembers().get(ways.get(i).getMembers().size() - 1)));
					}
				} while (!(ways.get(i).containsNode(lastNode.getNodeId()) >= 0));
				ways.remove(i);
			}
		} while (ways.size() > 0);
		return result;
	}

	private Collection<? extends CustomNode> getVillageNodes(CustomWay villageWay, CustomNode startNode) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		if (villageWay.getMembers().get(0) == startNode.getNodeId()) {
			for (int vnID : villageWay.getMembers()) {
				result.add(scModel.villageNodesMap.get(vnID));
			}
		}
		// Reverse way
		else {
			for (int i = villageWay.getMembers().size() - 1; i >= 0; i--) {
				result.add(scModel.villageNodesMap.get(villageWay.getMembers().get(i)));
			}
		}
		return result;
	}

	private static ArrayList<CustomNode> findIntersections(ArrayList<CustomPolygon> aggregatePolygonMembers) {
		ArrayList<CustomNode> result = new ArrayList<CustomNode>();
		for (CustomPolygon vp : aggregatePolygonMembers) {
			for (CustomNode vn : vp.getVillageNodes()) {
				boolean isThisNodeUnique = true;
				for (CustomPolygon vpToCompare : aggregatePolygonMembers) {
					if (!vp.equals(vpToCompare)) {
						for (CustomNode vnToCompare : vpToCompare.getVillageNodes()) {
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

}
