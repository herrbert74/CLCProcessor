package hu.herrbert74.osm.clcprocessor.views;

import hu.herrbert74.osm.clcprocessor.controllers.SettlementChooserController;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillagePolygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SettlementChooserView implements java.util.Observer {

	public Display display;
	public Shell shell;
	
	Button createCLCButton, addButton, removeButton;
	public Text searchText;
	Label progressLabel, searchLabel;
	public ScrolledComposite polygonListSC, settlementListSC;
	public List polygonList, settlementList;

	public SettlementChooserView() {
		display = new Display();
		shell = new Shell(display);
		Rectangle r = Display.getCurrent().getBounds();
		shell.setBounds((int)(r.width*0.1), (int)(r.height*0.1),(int)(r.width*0.8), (int)(r.height*0.8));
		Point shellSize = shell.getSize();
				
		FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		shell.setText("CLC processor");

		instantiateWidgets();
		
		layoutWidgets(shellSize);

		polygonListSC.setContent(polygonList);
		polygonListSC.setExpandHorizontal(true);
		polygonListSC.setExpandVertical(true);
		

		polygonList.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				int listItemHeight = polygonList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y / polygonList.getItemCount();
				if (e.count == -3) {
					int topIndex = polygonList.getSelectionIndex();
					//polygonList.setSelection(++topIndex);
					polygonListSC.setOrigin(polygonListSC.getOrigin().x, polygonListSC.getOrigin().y + listItemHeight * 10);
				} else if (e.count == 3) {
					int topIndex = polygonList.getSelectionIndex();
					//polygonList.setSelection(--topIndex);
					polygonListSC.setOrigin(polygonListSC.getOrigin().x, polygonListSC.getOrigin().y
							- listItemHeight * 10);
				}
			}
		});
		
		settlementListSC.setContent(settlementList);
		settlementListSC.setExpandHorizontal(true);
		settlementListSC.setExpandVertical(true);

		settlementList.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				int listItemHeight = settlementList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
						/ settlementList.getItemCount();
				if (e.count == -3) {
					int topIndex = settlementList.getSelectionIndex();
					settlementList.setSelection(++topIndex);
					settlementListSC.setOrigin(settlementListSC.getOrigin().x, settlementListSC.getOrigin().y
							+ listItemHeight);
				} else if (e.count == 3) {
					int topIndex = settlementList.getSelectionIndex();
					settlementList.setSelection(--topIndex);
					settlementListSC.setOrigin(settlementListSC.getOrigin().x, settlementListSC.getOrigin().y
							- listItemHeight);
				}
			}
		});
		
	}

	private void instantiateWidgets() {
		createCLCButton = new Button(shell, SWT.PUSH);
		createCLCButton.setText("Create CLC relations");
		createCLCButton.setData(new String("CREATECLC"));
		
		addButton = new Button(shell, SWT.PUSH);
		addButton.setText("Add");
		addButton.setData(new String("ADD"));

		removeButton = new Button(shell, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setData(new String("REMOVE"));
		
		searchText = new Text(shell, SWT.BORDER);
		
		progressLabel = new Label(shell, SWT.WRAP);
		progressLabel.setText("progress...");
		searchLabel = new Label(shell, SWT.CENTER);
		searchLabel.setText("Search");

		polygonListSC = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
		settlementListSC = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
		polygonList = new List(polygonListSC, SWT.SINGLE);
		polygonList.setData(new String("POLYGONLIST"));
		settlementList = new List(settlementListSC, SWT.SINGLE);
	}

	private void layoutWidgets(Point shellSize) {
		FormData createCLCData = new FormData((int) (shellSize.x * 0.1), 30);
		createCLCData.left = new FormAttachment(5);
		createCLCData.top = new FormAttachment(85);
		createCLCButton.setLayoutData(createCLCData);

		FormData addData = new FormData((int) (shellSize.x * 0.05), 30);
		addData.left = new FormAttachment(45);
		addData.top = new FormAttachment(45);
		addButton.setLayoutData(addData);
		
		FormData removeData = new FormData((int) (shellSize.x * 0.05), 30);
		removeData.left = new FormAttachment(45);
		removeData.top = new FormAttachment(55);
		removeButton.setLayoutData(removeData);
		
		FormData progressData = new FormData((int) (shellSize.x * 0.1), 30);
		progressData.left = new FormAttachment(5);
		progressData.top = new FormAttachment(95);
		progressLabel.setLayoutData(progressData);
		
		FormData searchLabelData = new FormData();
		searchLabelData.top = new FormAttachment(81);
		searchLabelData.left = new FormAttachment(7);
		searchLabelData.right = new FormAttachment(10);
		searchLabelData.bottom = new FormAttachment(84);
		searchLabel.setLayoutData(searchLabelData);
		
		FormData searchTextData = new FormData();
		searchTextData.top = new FormAttachment(81);
		searchTextData.left = new FormAttachment(12);
		searchTextData.right = new FormAttachment(38);
		searchTextData.bottom = new FormAttachment(84);
		searchText.setLayoutData(searchTextData);
				
		FormData polygonListData = new FormData();
		polygonListData.top = new FormAttachment(5);
		polygonListData.left = new FormAttachment(5);
		polygonListData.bottom = new FormAttachment(80);
		polygonListData.right = new FormAttachment(40);
		polygonListSC.setLayoutData(polygonListData);
		
		FormData settlementListData = new FormData();
		settlementListData.top = new FormAttachment(5);
		settlementListData.left = new FormAttachment(60);
		settlementListData.bottom = new FormAttachment(80);
		settlementListData.right = new FormAttachment(95);
		settlementListSC.setLayoutData(settlementListData);
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("notified");
		if(arg instanceof ArrayList<?>){
			ArrayList<VillagePolygon> vps = (ArrayList<VillagePolygon>) arg;
			Collections.sort(vps);
			for (VillagePolygon vp : vps) {
				//if(polygonList.getItemCount() < 250){
					polygonList.add(vp.getName());
				//}
			}
			polygonListSC.setMinSize(polygonListSC.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

	}

	public void addController(SettlementChooserController controller) {
		System.out.println("View      : adding controller");
		createCLCButton.addMouseListener(controller);
		addButton.addMouseListener(controller);
		removeButton.addMouseListener(controller);
		polygonList.addMouseMoveListener(controller);
		searchText.addKeyListener(controller);
	}
}