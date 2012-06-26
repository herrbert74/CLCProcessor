package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillageNode;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillagePolygon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class SettlementChooserController implements
		org.eclipse.swt.events.MouseListener,
		org.eclipse.swt.events.MouseWheelListener,
		org.eclipse.swt.events.MouseMoveListener,
		org.eclipse.swt.events.KeyListener{

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
	}

	@Override
	public void mouseDown(MouseEvent e) {
		String dataString = (String) e.widget.getData();

		switch (dataString) {
		case "ADD":
			String addition = scView.polygonList.getItem(scView.polygonList
					.getSelectionIndex());
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
			addNeighbours();
			break;
		case "REMOVE":
			scView.settlementList.remove(scView.settlementList
					.getSelectionIndex());
			break;
		default:
			break;
		}
	}

	private void addNeighbours() {
		for(String z : scView.settlementList.getItems()){
			VillagePolygon vpFound = new VillagePolygon();
			for(VillagePolygon vp: scModel.villagePolygons){
				if(vp.getName().equals(z)){
					vpFound = vp;
				}
			}
			for(VillageNode vn : vpFound.getVillageNodes()){
				for(VillagePolygon vp: scModel.villagePolygons){
					if(vp.getVillageNodes().contains(vn) && scView.settlementList.indexOf(vp.getName()) == -1 && scView.neighbourList.indexOf(vp.getName()) == -1){
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
		for(int i = 0; i < scView.polygonList.getItemCount(); i++){
			String trimmedSearched = searched;
			String lookup;
			try{
				lookup = scView.polygonList.getItem(i).substring(0, trimmedSearched.length()).toLowerCase();
			}catch (StringIndexOutOfBoundsException sioe) {
				trimmedSearched = trimmedSearched.substring(0, scView.polygonList.getItem(i).length());
				lookup = scView.polygonList.getItem(i).substring(0, trimmedSearched.length()).toLowerCase();
			}
			if(trimmedSearched.equals(lookup)){
				int listItemHeight = scView.polygonList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y / scView.polygonList.getItemCount();
				scView.polygonList.setSelection(i);
				scView.polygonListSC.setOrigin(scView.polygonListSC.getOrigin().x, listItemHeight * i);
				break;
			}
		}
	}
}
