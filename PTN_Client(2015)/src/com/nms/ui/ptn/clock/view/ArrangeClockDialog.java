package com.nms.ui.ptn.clock.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import twaver.Element;
import twaver.Node;
import twaver.TDataBox;
import twaver.list.TList;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.util.Services;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.control.PtnDialog;
import com.nms.ui.manager.keys.StringKeysBtn;

/**
 * 频率配置对话框
 * 
 * @author lp
 * 
 */
public class ArrangeClockDialog extends PtnDialog {

	private static final long serialVersionUID = -800170517095171465L;
	private Map<String, String> items;
	private TDataBox needChooseBox;
	private TList needChooseList;
	private TDataBox choosedBox;
	private TList choosedList;
	private JButton addButton;
	private JButton addAllButton;
	private JButton removeButton;
	private JButton removeAllButton;
	private JPanel controlPane;
	private JScrollPane needChooseScrollPane;
	private JScrollPane choosedScrollPane;
	private JButton confirm;
	private JButton cancel;
	private JPanel bottomPanel;
	private Comparator<Element> comparator = new Comparator<Element>() {

		@Override
		public int compare(Element e1, Element e2) {
			Integer id1 = (Integer) e1.getID();
			Integer id2 = (Integer) e2.getID();
			return id1.compareTo(id2);
		}

	};

	public ArrangeClockDialog(String title, Map<String, String> items) {
		this.setModal(true);
		this.items = items;
		this.setTitle(title);
		init();

		needChooseList.setSortComparator(comparator);
	}

	private void init() {
		initComponents();
		setLayout();
		addActionLister();
	}

	@SuppressWarnings("unchecked")
	private void addActionLister() {
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				move(needChooseBox.getSelectionModel().getAllSelectedElement(), needChooseBox, choosedBox);
			}
		});
		addAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				move(needChooseBox.getAllElements(), needChooseBox, choosedBox);

			}
		});
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				move(choosedBox.getSelectionModel().getAllSelectedElement(), choosedBox, needChooseBox);
			}
		});
		removeAllButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				move(choosedBox.getAllElements(), choosedBox, needChooseBox);
			}
		});

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
	}

	/**
	 * 当点击确定后，验证用户操作是否完成
	 * 
	 * @return
	 */
	public boolean validated() {
		boolean flag = false;
		if (needChooseBox.getAllElements() == null || needChooseBox.getAllElements().size() == 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 获取界面数据
	 * @param type 
	 */
	@SuppressWarnings("unchecked")
	public String getInfo(int type) {
		StringBuilder builder = new StringBuilder();
		List<Element> elements = choosedBox.getAllElements();
		Element element = null;
		SiteService_MB siteService = null;
		SiteInst siteInst = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			siteInst = siteService.select(ConstantUtil.siteId);
			if(siteInst != null){
				for (int i = elements.size()-1; i >= 0; i--) {
					element = elements.get(i);
					if("703A".equals(siteInst.getCellType())){
						if(Integer.parseInt((String) element.getUserObject())>2){
							builder.append((Integer.parseInt((String) element.getUserObject())-2)+"");
						}else{
							builder.append((Integer.parseInt((String) element.getUserObject()))+"");
						}
					}else if("703B".equals(siteInst.getCellType()) || "703B2".equals(siteInst.getCellType())
							|| "703-1A".equals(siteInst.getCellType()) || "703-2A".equals(siteInst.getCellType())
							|| "703-4A".equals(siteInst.getCellType()) || "703-1".equals(siteInst.getCellType()) ||
							"703-2".equals(siteInst.getCellType()) || "703-3".equals(siteInst.getCellType()) ||
							"703-4".equals(siteInst.getCellType()) || "703-5".equals(siteInst.getCellType()) ||
							"ETN-200-204".equals(siteInst.getCellType()) || "ETN-200-204E".equals(siteInst.getCellType())){
						builder.append(element.getUserObject());
					}else if("710A".equals(siteInst.getCellType()) || "710".equals(siteInst.getCellType())|| "ETN-5000".equals(siteInst.getCellType())){
						if(Integer.parseInt((String) element.getUserObject())>10){
							builder.append((Integer.parseInt((String) element.getUserObject()))+"");
						}else{
							builder.append(element.getUserObject());
						}
					}else if("710B".equals(siteInst.getCellType())){
						if(Integer.parseInt((String) element.getUserObject())>10){
							builder.append((Integer.parseInt((String) element.getUserObject())-2)+"");
						}else{
							builder.append(element.getUserObject());
						}
					}
					builder.append("/");
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}finally{
			UiUtil.closeService_MB(siteService);
		}
		
		if(type == 1){
			//优先级排列
			for (int i = 0; i < 32-elements.size(); i++) {
				builder.append("255");
				builder.append("/");
			}
		}else{
			//输出时钟选择
			for (int i = 0; i < 33-elements.size(); i++) {
				builder.append("255");
				builder.append("/");
			}
		}
		// for(Element element : elements){
		// builder.append(element.getUserObject());
		// builder.append("/");
		// }
		builder.deleteCharAt(builder.toString().length() - 1);
		return builder.toString();
	}

	private void move(List<Element> elements, TDataBox source, TDataBox target) {
		Collections.sort(elements, comparator);
		Iterator<Element> it = elements.iterator();
		while (it.hasNext()) {
			Element element = it.next();
			if (!element.isSelected()) {
				it.remove();
			}
			source.removeElement(element);
			target.addElement(element);
		}
		if (elements.size() > 0) {
			target.getSelectionModel().setSelection(elements);
		}
	}

	private void setLayout() {
		initControlPane();
		initBottomPanel();
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 100, 40, 100 };
		layout.columnWeights = new double[] { 0.5, 0, 0.5 };
		layout.rowHeights = new int[] { 150, 20 };
		layout.rowWeights = new double[] { 1.0, 0 };
		this.setLayout(layout);
		GridBagConstraints c = null;
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(needChooseScrollPane, c);
		this.add(needChooseScrollPane);
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 0, 5, 0);
		layout.setConstraints(controlPane, c);
		this.add(controlPane);
		c.gridx = 2;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(choosedScrollPane, c);
		this.add(choosedScrollPane);
		// c.gridx = 1;
		// c.gridy = 1;
		// c.gridheight = 1;
		// c.gridwidth = 1;
		// c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.WEST;
		// c.insets = new Insets(5, 5, 5, 20);
		// layout.setConstraints(confirm, c);
		// this.add(confirm);
		// c.gridx = 2;
		// c.gridy = 1;
		// c.gridheight = 1;
		// c.gridwidth = 1;
		// c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.WEST;
		// c.insets = new Insets(5, 5, 5, 5);
		// layout.setConstraints(cancel, c);
		// this.add(cancel);
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.EAST;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(bottomPanel, c);
		this.add(bottomPanel);
	}

	private void initControlPane() {
		GridBagLayout controlLayout = new GridBagLayout();
		controlLayout.columnWidths = new int[] { 25 };
		controlLayout.columnWeights = new double[] { 0.0 };
		controlLayout.rowHeights = new int[] { 5, 5, 5, 5 };
		controlLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		controlPane.setLayout(controlLayout);
		GridBagConstraints c = null;
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(10, 10, 10, 10);
		controlLayout.setConstraints(addButton, c);
		controlPane.add(addButton);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(10, 10, 10, 10);
		controlLayout.setConstraints(addAllButton, c);
		controlPane.add(addAllButton);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(10, 10, 10, 10);
		controlLayout.setConstraints(removeButton, c);
		controlPane.add(removeButton);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(10, 10, 10, 10);
		controlLayout.setConstraints(removeAllButton, c);
		controlPane.add(removeAllButton);
	}

	private void initBottomPanel() {
		GridBagLayout layout = new GridBagLayout();
		bottomPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 3;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(confirm, c);
		bottomPanel.add(confirm);
		c.gridx = 4;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(cancel, c);
		bottomPanel.add(cancel);
	}

	private void initComponents() {
		needChooseBox = new TDataBox("NeedChooseList");
		needChooseList = new TList(needChooseBox);
		needChooseList.setTListSelectionMode(TList.CHECK_SELECTION);

		choosedBox = new TDataBox("ChoosedList");
		choosedList = new TList(choosedBox);
		choosedList.setTListSelectionMode(TList.CHECK_SELECTION);

		addButton = new JButton(">");
		addAllButton = new JButton(">>");
		removeButton = new JButton("<");
		removeAllButton = new JButton("<<");
		controlPane = new JPanel();
		needChooseScrollPane = new JScrollPane(needChooseList);
		needChooseScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		needChooseScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		needChooseScrollPane.setViewportView(needChooseList);

		choosedScrollPane = new JScrollPane(choosedList);
		choosedScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		choosedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		choosedScrollPane.setViewportView(choosedList);
		confirm = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SAVE));
		cancel = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_CANEL));
		bottomPanel = new JPanel();
		initChooseBox();
	}

	private void initChooseBox() {
		int i = 0;
		for (String key : items.keySet()) {
			Node node = new Node(i++);
			node.setName(items.get(key));
			node.setDisplayName(items.get(key));
			node.setUserObject(key);
			if(items.get(key) != null){
				needChooseBox.addElement(node);
			}
		}
	}

	public JButton getConfirm() {
		return confirm;
	}

	public JButton getCancel() {
		return cancel;
	}

}
