package com.nms.ui.ptn.systemconfig.Template.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import com.nms.db.bean.ptn.qos.VlanpriToColor;
import com.nms.db.enums.EOperationLogType;
import com.nms.ui.frame.ContentView;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysTab;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.safety.roleManage.RootFactory;
import com.nms.ui.ptn.systemconfig.Template.controller.VlanpriToColorController;
import com.nms.ui.ptn.systemconfig.Template.view.dialog.EditeVlanpriToColorDialog;

/**
 * VLANPRI到颜色映射界面
 * 
 * @author dzy
 * 
 */
public class VlanpriToColorPanel extends ContentView<VlanpriToColor> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3886427091477110017L;
	
	/**
	 * 创建一个新实例
	 */
	public VlanpriToColorPanel() {
		super("vlanpriToColorTemplate",RootFactory.DEPLOY_MANAGE);
		try {
			init();
			this.addListention();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	
	/**
	 * 初始化
	 * @throws Exception
	 */
	private void init() throws Exception{
		getContentPanel().setBorder(BorderFactory.createTitledBorder(ResourceUtil.srcStr(StringKeysTab.TAB_VLANPRITOCOLORTEMPLATE)));
		setLayout();
		super.getController().refresh();
	}

	/**
	 * 设置布局
	 */
	private void setLayout() {
		GridBagLayout panelLayout = new GridBagLayout();
		this.setLayout(panelLayout);
		GridBagConstraints c = null;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		panelLayout.setConstraints(getContentPanel(), c);
		this.add(getContentPanel());
		
		
	}
	
	/**
	 * 添加监听
	 */
	private void addListention(){
		//添加table点击行事件
		super.getTable().addElementClickedActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (getSelect() == null) {
					// 清除详细面板数据
//					getMspPortPanel().clear();
					return;
				} else {
					getController().initDetailInfo();
				}
			}
		});
	}
	
	/**
	 * 设置倒换按钮
	 * 
	 * @return
	 */
	private JButton btnDataDownLoad() {
		JButton jButton = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_BATCH_DOWNLOAD),RootFactory.DEPLOY_MANAGE);

		// 倒换按钮事件
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnDataDownLoadActionPerformed();
			}
		});

		return jButton;
	}
	
	/**
	 * 数据下发设备页面
	 */
	private void btnDataDownLoadActionPerformed() {
		try {
			if(null==this.getSelect()){
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_DATA_MORE));
				return;
			}
			new EditeVlanpriToColorDialog(this.getSelect().getQosMappingMode(),this,ResourceUtil.srcStr(StringKeysBtn.BTN_BATCH_DOWNLOAD));
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
	}
	
	/**
	 * 给此界面添加控制类
	 */
	@Override
	public void setController() {
		controller = new VlanpriToColorController(this);
		
	}
	
	
	/**
	 * 添加按钮
	 */
	@Override
	public List<JButton> setAddButtons() {
		List<JButton> needRemoveButtons = new ArrayList<JButton>();
		needRemoveButtons.add(this.btnDataDownLoad());
		return needRemoveButtons;
	}
	
	/**
	 * 按钮控制
	 */
	@Override
	public List<JButton> setNeedRemoveButtons() {
		List<JButton> needRemoveButtons = new ArrayList<JButton>();
		needRemoveButtons.add(getSearchButton());
		needRemoveButtons.add(getSynchroButton());
		return needRemoveButtons;
	}
	
	/**
	 * 设置controller
	 */
	@Override
	public VlanpriToColorController getController() {
		return (VlanpriToColorController) super.controller;
	}
}
