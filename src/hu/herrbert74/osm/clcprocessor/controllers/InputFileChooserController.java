package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.CLCProcessorConstants;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.List;

public class InputFileChooserController implements CLCProcessorConstants, org.eclipse.swt.events.MouseListener,
		org.eclipse.swt.events.MouseWheelListener, org.eclipse.swt.events.MouseMoveListener,
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
		case "DUMMY":
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {
		String dataString = (String) e.widget.getData();

		switch (dataString) {
		case "DUMMY":
			break;
		default:
			break;
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
				lookup = scView.polygonList.getItem(i).substring(0, trimmedSearched.length()).toLowerCase();
			} catch (StringIndexOutOfBoundsException sioe) {
				trimmedSearched = trimmedSearched.substring(0, scView.polygonList.getItem(i).length());
				lookup = scView.polygonList.getItem(i).substring(0, trimmedSearched.length()).toLowerCase();
			}
			if (trimmedSearched.equals(lookup)) {
				int listItemHeight = scView.polygonList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
						/ scView.polygonList.getItemCount();
				scView.polygonList.setSelection(i);
				scView.polygonListSC.setOrigin(scView.polygonListSC.getOrigin().x, listItemHeight * i);
				break;
			}
		}
	}
}
