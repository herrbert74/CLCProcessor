package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.CLCProcessorConstants;
import hu.herrbert74.osm.clcprocessor.dao.OsmNodesHandler;
import hu.herrbert74.osm.clcprocessor.dao.OsmRelationsHandler;
import hu.herrbert74.osm.clcprocessor.dao.OsmWaysHandler;
import hu.herrbert74.osm.clcprocessor.dao.XMLFunctions;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.List;
import org.xml.sax.SAXException;

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
			CLCCreator clcCreator = new CLCCreator();
			clcCreator.createCLC(scModel, scView);
			scView.progressLabel.setText("Created CLC");
			break;
		default:
			break;
		}
	}
	
	public void readBorders(File file) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();

		OsmRelationsHandler relationsHandler = new OsmRelationsHandler();
		parser.parse(file, relationsHandler);
		scModel.villageRelationsMap = relationsHandler.getRelations();

		OsmWaysHandler waysHandler = new OsmWaysHandler();
		parser.parse(file, waysHandler);
		scModel.villageWaysMap = waysHandler.getWays();

		OsmNodesHandler nodesHandler = new OsmNodesHandler();
		parser.parse(file, nodesHandler);
		scModel.villageNodesMap = nodesHandler.getNodes();

		scModel.createVillagePolygons();
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
		/*TODO
		 * villagePolygon has changeed from ArrayList to Map. Change this method accordingly!!!*/
		scView.neighbourList.removeAll();
		for (String z : scView.settlementList.getItems()) {
			CustomPolygon vpFound = new CustomPolygon();
			for (CustomPolygon vp : scModel.villagePolygons.values()) {
				if (vp.getName().equals(z)) {
					vpFound = vp;
				}
			}
			for (CustomNode vn : vpFound.getVillageNodes()) {
				for (CustomPolygon vp : scModel.villagePolygons.values()) {
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
