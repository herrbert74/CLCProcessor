package hu.herrbert74.osm.clcprocessor.helpers;

import hu.herrbert74.osm.clcprocessor.CLCProcessorConstants;
import hu.herrbert74.osm.clcprocessor.dao.XMLFunctions;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelationMember;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.osmentities.NodePair;
import hu.herrbert74.osm.clcprocessor.osmentities.WayPair;
import hu.herrbert74.osm.clcprocessor.utils.Functions;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class CLCCreator implements CLCProcessorConstants {

	SettlementChooserModel scModel;
	SettlementChooserView scView;

	public void createCLC(SettlementChooserModel scM, SettlementChooserView scV) {
		this.scView = scV;
		this.scModel = scM;
		PolygonCreator pc = new PolygonCreator(scModel);
		scModel.setStatus("Creating main polygon");
		scModel.borderPolygon = pc.createBorderPolygon(scView.settlementList.getItems());
		scModel.setStatus("Creating neighbour polygon");
		scModel.neighbourPolygon = pc.createBorderPolygon(scView.neighbourList.getItems());
		CLCReader clcReader = new CLCReader(scModel);
		clcReader.readCLCData();
		convertPureWaysToRelations();
		createWayPairs();
		splitWaysAndUpdateRelations();
		XMLFunctions.writeOSM(scModel.clcMainNodes, scModel.clcMainWays, scModel.clcMainRelations, "clc_out.osm");
	}

	private void convertPureWaysToRelations() {
		boolean pureWay;
		int newRelationId = -5000000;
		for (CustomWay cw : scModel.clcMainWays.values()) {
			pureWay = true;
			for (CustomRelation cr : scModel.clcMainRelations.values()) {
				for (CustomRelationMember crm : cr.getMembers()) {
					if (crm.getRef() == cw.getWayId()) {
						pureWay = false;
					}
				}
			}
			if (pureWay) {
				newRelationId--;
				CustomRelation cr = new CustomRelation(new CustomRelationMember("way", cw.getWayId(), "outer"), newRelationId);
				Iterator<Map.Entry<String, String>> it = cw.getTags().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
					cr.addTag(pairs.getKey(), pairs.getValue());
				}
				cr.addTag("type", "multipolygon");
				scModel.clcMainRelations.put(newRelationId, cr);
			}
		}
	}

	private void createWayPairs() {

		for (CustomRelation cr : scModel.clcMainRelations.values()) {
			for (int i = 0; i < cr.getMembers().size(); i++) {
				if (cr.getMembers().get(i).getRole().equals("outer")) {
					for (CustomRelation otherCr : scModel.clcMainRelations.values()) {
						int overLappingWayId = otherCr.getOverLappingMember(cr.getMembers().get(i).getRef(), scModel.clcMainWays,
								scModel.clcMainNodes);
						if (overLappingWayId != -1) {
							WayPair w = new WayPair(cr.getMembers().get(i).getRef(), overLappingWayId);
							if (!scModel.wayPairList.contains(w)) {
								System.out.println("Added WayPair: " + Integer.toString(cr.getMembers().get(i).getRef())
										+ Integer.toString(overLappingWayId));
								scModel.wayPairList.add(w);
							} else {
								System.out.println("Discarded WayPair: " + Integer.toString(cr.getMembers().get(i).getRef())
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
		for (WayPair w : scModel.wayPairList) {
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
			for (WayPair w : scModel.wayPairList) {
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
					compactedStartEndPairs.get(compactedStartEndPairs.size() - 1).setSecond(startEndPairs.get(i2).getSecond());
				} else {
					compactedStartEndPairs.add(new NodePair(startEndPairs.get(i2).getFirst(), startEndPairs.get(i2).getSecond()));
				}
			}
			// Compact rotated pairs
			if (compactedStartEndPairs.get(compactedStartEndPairs.size() - 1).getSecond() == compactedStartEndPairs.get(0).getFirst()
					&& compactedStartEndPairs.size() > 1) {
				compactedStartEndPairs.get(0).setFirst(compactedStartEndPairs.get(compactedStartEndPairs.size() - 1).getFirst());
				compactedStartEndPairs.remove(compactedStartEndPairs.size() - 1);
			}
			Collections.sort(compactedStartEndPairs);
			// Remove duplicate stretches, add unpaired, clipped ways
			CustomWay affectedWay = scModel.clcMainWays.get((Integer) i);

			if (compactedStartEndPairs.size() == 1
					&& ((affectedWay.isFullRound() && compactedStartEndPairs.get(0).getFirst() == compactedStartEndPairs.get(0).getSecond()) || (!affectedWay
							.isFullRound() && compactedStartEndPairs.get(0).getFirst() == 0 && compactedStartEndPairs.get(0).getSecond() == affectedWay
							.getMembers().size() - 1))) {
				fullyReplacedWays.add(i);
			} else if (!affectedWay.isFullRound()) {
				// New ways
				for (int i2 = 0; i2 < compactedStartEndPairs.size(); i2++) {
					CustomWay newWay = new CustomWay();
					try {
						if (i2 == compactedStartEndPairs.size() - 1) {
							newWay.addMembers(affectedWay.getMembers().subList(compactedStartEndPairs.get(i2).getSecond(),
									affectedWay.getMembers().size()));
						} else {
							newWay.addMembers(affectedWay.getMembers().subList(compactedStartEndPairs.get(i2).getSecond(),
									compactedStartEndPairs.get(i2 + 1).getFirst() + 1));
						}
						if (!(i2 == 0 && compactedStartEndPairs.get(0).getFirst() == 0) && newWay.getMembers().size() > 1) {
							newWay.setWayId(newWayId);
							scModel.clcMainWays.put(newWayId, newWay);
							scModel.clcMainRelations.get(
									Functions.getParentRelation(scModel.clcMainWays.get(affectedWay.getWayId()), scModel.clcMainRelations))
									.addMember(new CustomRelationMember("way", newWayId, "outer"));
							newWayId--;
						}
					} catch (IllegalArgumentException e) {
						System.out.println("Fullround, New way 1. " + Integer.toString(i) + " " + e.getMessage());
					}
				}
				// Remove stretches
				int newStart = compactedStartEndPairs.get(0).getFirst() == 0 ? compactedStartEndPairs.get(0).getSecond() : 0;
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
					try {
						newWay.addMembers(affectedWay.getMembers().subList(start, end));

						newWay.setWayId(newWayId);
						scModel.clcMainWays.put(newWayId, newWay);
						int parent = Functions.getParentRelation(scModel.clcMainWays.get(affectedWay.getWayId()), scModel.clcMainRelations);
						scModel.clcMainRelations.get((Integer) parent).addMember(new CustomRelationMember("way", newWayId, "outer"));
						newWayId--;
					} catch (IllegalArgumentException e) {
						System.out.println("Not full round, New way 2. " + Integer.toString(i) + " " + e.getMessage());
					}
				}
				// Reduce affected way
				// Get a sublist if affectedWay starts with commonWay
				NodePair last = compactedStartEndPairs.get(compactedStartEndPairs.size() - 1);
				NodePair first = new NodePair(last.getSecond(), compactedStartEndPairs.get(0).getFirst());
				if (last.getFirst() > last.getSecond()) {
					try {

						affectedWay.setMembers(affectedWay.getMembers().subList(first.getFirst(), first.getSecond() + 1));
					} catch (IllegalArgumentException e) {
						System.out.println("CommonWay start. " + Integer.toString(i) + " " + e.getMessage());
					}
				}
				// Remove all other parts and rotate
				else {
					affectedWay.getMembers().removeAll(
							affectedWay.getMembers().subList(compactedStartEndPairs.get(0).getFirst() + 1, last.getSecond()));
					Collections.rotate(affectedWay.getMembers(), -compactedStartEndPairs.get(0).getFirst() - 1);
				}
			}
		}
		// Add common ways to collections
		for (WayPair w : scModel.wayPairList) {
			for (int i = 0; i < w.getNumberOfWays(); i++) {
				w.setNewWayId(newWayId, i);
				scModel.clcMainWays.put(newWayId, w.getNewWay(i));
				scModel.clcMainRelations.get(Functions.getParentRelation(scModel.clcMainWays.get(w.getFirst()), scModel.clcMainRelations))
						.addMember(new CustomRelationMember("way", newWayId, "outer"));
				scModel.clcMainRelations.get(Functions.getParentRelation(scModel.clcMainWays.get(w.getSecond()), scModel.clcMainRelations))
						.addMember(new CustomRelationMember("way", newWayId, "outer"));
				newWayId--;
			}
		}
		// Remove fully Replaced ways
		for (Integer id : fullyReplacedWays) {
			CustomRelation cr = scModel.clcMainRelations.get(Functions.getParentRelation(scModel.clcMainWays.get(id),
					scModel.clcMainRelations));
			cr.removeMemberWithWayId(id);
			scModel.clcMainWays.remove((Integer) id);
		}
	}

	public void determineJunctions(WayPair w) {
		CustomWay cwA = scModel.clcMainWays.get(w.getFirst());
		CustomWay cwB = scModel.clcMainWays.get(w.getSecond());
		System.out.print("Determine junctions: " + Integer.toString(cwA.getWayId()) + " - " + Integer.toString(cwB.getWayId()));
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
		int posInBPrev = -1;
		for (int i = 0; i < cwA.getMembers().size(); i++) {
			int posInB = cwB.containsNode(cwA.getMembers().get(i));

			if (posInB >= 0) {
				if (!isCommon) {
					w.setNumberOfWays(w.getNumberOfWays() + 1);
					w.addStartA(i);
					w.addStartB(posInB);
				} 
				//There is a two node interruption, where A has two nodes common with a way different to B
				else if(Math.abs(posInB - posInBPrev) > 1) {
					w.addEndA(i - 1);
					w.addEndB(posInBPrev);
					w.setNumberOfWays(w.getNumberOfWays() + 1);
					w.addStartA(i);
					w.addStartB(posInB);
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
			if (w.getStartA(0) == 0 && w.getNumberOfWays() > 1) {
				w.setStartA(w.getStartA(w.getNumberOfWays() - 1), 0);
				w.setStartB(w.getStartB(w.getNumberOfWays() - 1), 0);
				w.setNumberOfWays(w.getNumberOfWays() - 1);
			}
		}
		setWayPairData(w, cwA, cwB);
	}

	private void setWayPairData(WayPair w, CustomWay cwA, CustomWay cwB) {
		// Swap start and end if needed
		int commonLength = w.getEndA(0) > w.getStartA(0) ? w.getEndA(0) - w.getStartA(0) : cwA.getMembers().size() + w.getEndA(0)
				- w.getStartA(0);
		if (isBReversed(w.getStartB(0), w.getEndB(0), cwB.getMembers().size(), commonLength)) {
			for (int i = 0; i < w.getNumberOfWays(); i++) {
				int temp = w.getEndB(i);
				w.setEndB(w.getStartB(i), i);
				w.setStartB(temp, i);
			}
		}
		// Create new way
		for (int i = 0; i < w.getNumberOfWays(); i++) {
			System.out.println(" StartA: " + w.getStartA(i) + " EndA: " + w.getEndA(i) + " StartB: " + w.getStartB(i) + " EndB: " + w.getEndB(i));
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

	private boolean isBReversed(int startB, int endB, int lengthB, int commonLength) {
		if ((endB - startB) == commonLength) {
			return false;
		} else if ((lengthB + endB - startB) == commonLength) {
			return false;
		}
		return true;
	}
}
