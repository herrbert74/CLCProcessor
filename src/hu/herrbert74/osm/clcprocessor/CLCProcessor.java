package hu.herrbert74.osm.clcprocessor;

import hu.herrbert74.osm.clcprocessor.controllers.SettlementChooserController;
import hu.herrbert74.osm.clcprocessor.helpers.CLCCreator;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomPolygon;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.xml.sax.SAXException;

public class CLCProcessor implements CLCProcessorConstants {

	static int canvasStyle = SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL;

	ArrayList<CustomPolygon> villagePolygons = new ArrayList<CustomPolygon>();

	// InputFileChooserView ifView;
	// InputFileChooserModel ifModel;

	SettlementChooserModel scModel = new SettlementChooserModel();
	SettlementChooserView scView = new SettlementChooserView();

	public CLCProcessor() {

		// ifView = new InputFileChooserView();
		// ifModel= new InputFileChooserModel();
		scModel.addObserver(scView);
		SettlementChooserController scController = new SettlementChooserController(scModel, scView);
		scController.initModel();

		scView.addController(scController);

		scView.shell.open();

		try {
			scController.readBorders(new File(OSM_VILLAGEBORDERS));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		if (MAIN_VILLAGES.length != 0){
			for(int i = 0; i < MAIN_VILLAGES.length; i++) {
				scView.settlementList.add(MAIN_VILLAGES[i]);
			}
			for(int i = 0; i < NEIGHBOUR_VILLAGES.length; i++) {
				scView.neighbourList.add(NEIGHBOUR_VILLAGES[i]);
			}
			for(int i = 0; i < EXCLUDED_VILLAGES.length; i++) {
				scView.excludedNeighbourList.add(EXCLUDED_VILLAGES[i]);
			}
			CLCCreator clcCreator = new CLCCreator();
			clcCreator.createCLC(scModel, scView);
			scView.progressLabel.setText("Created CLC");
		}
		// and Model,
		// this was only needed when the view inits the model
		// myView.addModel(myModel);

		while (!scView.shell.isDisposed()) {
			if (!scView.display.readAndDispatch()) {
				scView.display.sleep();
			}
		}
	}

	public void showInputFileChooser() {
		scModel.addObserver(scView);
		SettlementChooserController scController = new SettlementChooserController(new SettlementChooserModel(), new SettlementChooserView());
		scController.addModel(scModel);
		scController.addView(scView);
		scController.initModel();

		scView.addController(scController);

		scView.shell.open();

		try {
			scController.readBorders(new File(OSM_VILLAGEBORDERS));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		while (!scView.shell.isDisposed()) {
			if (!scView.display.readAndDispatch()) {
				scView.display.sleep();
			}
		}
	}

	public void showSettlementChooser() {

	}

	public static void main(String[] args) {
		CLCProcessor clcProcessor = new CLCProcessor();
		// clcProcessor.showInputFileChooser();
		// clcProcessor.showSettlementChooser();
	}
	/*
	 * public static void main(String[] args) { final Display display = new
	 * Display(); final Shell shell = new Shell(display); shell.setLayout(new
	 * FillLayout());
	 * shell.setBackground(display.getSystemColor((SWT.COLOR_CYAN)));
	 * shell.setText("Canvas Test"); FormLayout layout = new FormLayout();
	 * shell.setLayout(layout); FormData createCLCData = new FormData((int)
	 * (shell.getSize().x * 0.5), 300); createCLCData.left = new
	 * FormAttachment(5); createCLCData.top = new FormAttachment(5); final
	 * Canvas canvas = new Canvas (shell, canvasStyle);
	 * canvas.setLayoutData(createCLCData);
	 * canvas.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	 * canvas.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
	 * 
	 * final Point timelineSize = new Point(80, 1600); final Point offset = new
	 * Point(0,0); final ScrollBar hBar = canvas.getVerticalBar();
	 * 
	 * // Create a paint handler for the canvas canvas.addPaintListener(new
	 * PaintListener() { public void paintControl(PaintEvent e) {
	 * System.out.println("canvas"); for (int y = 100; y < timelineSize.y; y +=
	 * 100) { e.gc.drawLine(0, y + offset.y, 20, y + offset.y);
	 * e.gc.drawText(Integer.toString(y), 30, y + offset.y, true); } } });
	 * 
	 * // The below event handlers allow for horizontal scrolling functionality
	 * hBar.setIncrement(100); hBar.addListener(SWT.Selection, new Listener() {
	 * public void handleEvent(Event e) { System.out.println("hbar"); int
	 * hSelection = hBar.getSelection(); int destX = -hSelection - offset.y;
	 * canvas.scroll(0, destX , 0, 0, timelineSize.x, timelineSize.y, false);
	 * offset.y = -hSelection; } });
	 * 
	 * canvas.addListener(SWT.Resize, new Listener() { public void
	 * handleEvent(Event e) { System.out.println("canvaslistener"); Rectangle
	 * client = canvas.getClientArea(); hBar.setMaximum(timelineSize.y);
	 * hBar.setThumb(Math.min(client.height, timelineSize.y)); int hPage =
	 * timelineSize.x - client.height; int hSelection = hBar.getSelection(); if
	 * (hSelection >= hPage) { if (hPage <= 0) hSelection = 0; offset.y =
	 * -hSelection; } shell.redraw(); } });
	 * 
	 * shell.open(); while(!shell.isDisposed()) { if(!display.readAndDispatch())
	 * { display.sleep(); } } display.dispose();
	 * 
	 * }
	 */

}
