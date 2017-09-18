package com.nms.ui.ptn.clock.view.cx.clockinterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import com.nms.db.bean.ptn.clock.LineClockInterface;
import com.nms.ui.frame.ContentView;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.keys.StringKeysPanel;
import com.nms.ui.ptn.clock.controller.LineCLPanelControllerCX;
import com.nms.ui.ptn.safety.roleManage.RootFactory;

public class TabPanelTwoTICX extends ContentView<LineClockInterface> {

	private static final long serialVersionUID = -5562534280356500555L;

	private GridBagLayout gridBagLayout = null;

	public TabPanelTwoTICX() {

		super("LineClockInterface",RootFactory.CORE_MANAGE);
		try {
			init();
			super.getController().refresh();/* 加载页面数据 */
		} catch (Exception e) {

			ExceptionManage.dispose(e,this.getClass());
		}
	}

	@Override
	public void setController() {

		controller = new LineCLPanelControllerCX(this);

	}

	@Override
	public Dimension setDefaultSize() {
		return new Dimension(160, ConstantUtil.INT_WIDTH_THREE);
	}
	
	/**
	 * <p>
	 * 去掉不需要用到的按钮‘search’、‘add’和‘delete’
	 * </p>
	 */
	@Override
	public List<JButton> setNeedRemoveButtons() {

		List<JButton> needButtons = new ArrayList<JButton>();
		needButtons.add(super.getSearchButton());
		needButtons.add(super.getDeleteButton());
		needButtons.add(super.getAddButton());
		return needButtons;
	}
	
	private void init() throws Exception {

		try {

			gridBagLayout = new GridBagLayout();
			setGridBagLayout();

			this.setLayout(gridBagLayout);
			this.setBorder(BorderFactory.createTitledBorder(ResourceUtil.srcStr(StringKeysPanel.PANEL_NE_TOD_CONFIG)));
			this.setBackground(Color.WHITE);
			this.add(this.getContentPanel());
		} catch (Exception e) {

			throw e;
		}
	}

	private void setGridBagLayout() throws Exception {

		GridBagConstraints gridBagConstraints = null;
		try {

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridheight = 1;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagLayout.setConstraints(this.getContentPanel(), gridBagConstraints);
		} catch (Exception e) {

			throw e;
		}
	}

}
