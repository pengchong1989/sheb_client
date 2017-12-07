﻿/*
 * AddPWDialog.java
 *
 * Created on __DATE__, __TIME__
 */

package com.nms.ui.ptn.business.dialog.elanpath;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import twaver.Element;
import twaver.Link;
import twaver.Node;
import twaver.PopupMenuGenerator;
import twaver.TUIManager;
import twaver.TView;
import twaver.TWaverUtil;
import twaver.network.TNetwork;

import com.nms.db.bean.client.Client;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.bean.ptn.CommonBean;
import com.nms.db.bean.ptn.path.eth.ElanInfo;
import com.nms.db.bean.ptn.path.eth.VplsInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.bean.ptn.port.AcPortInfo;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EPwType;
import com.nms.db.enums.EServiceType;
import com.nms.model.client.ClientService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.ptn.path.eth.ElanInfoService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.path.tunnel.TunnelService_MB;
import com.nms.model.ptn.port.AcPortInfoService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.ui.frame.ViewDataTable;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.AutoNamingUtil;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ListingFilter;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.TopoAttachment;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.VerifyNameUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.control.PtnDialog;
import com.nms.ui.manager.control.PtnTextField;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysMenu;
import com.nms.ui.manager.keys.StringKeysObj;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.manager.keys.StringKeysTitle;
import com.nms.ui.ptn.business.dialog.tunnel.TunnelTopoPanel;
import com.nms.ui.ptn.business.elan.ElanBusinessPanel;
import com.nms.ui.ptn.ne.ac.view.AcListDialog;
import com.nms.ui.ptn.safety.roleManage.RootFactory;

/**
 * 新建ELan 对话框
 * @author __USER__
 */
public class AddElanDialog extends PtnDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ElanBusinessPanel elanBusinessPanel;
	private ViewDataTable<AcPortInfo> branchAcTable; // 选择的叶子table
	private ViewDataTable<PwInfo> pwInfoTable; // 选择的pwtable
	private final String ACTABLENAME = "selectAcList";
	private final String PWTABLENAME = "selectPwList";
	private JScrollPane jscrollPane_ac;
	private JScrollPane jscrollPane_pw;
	private List<Node> selectNodeList = new ArrayList<Node>();
	private ElanInfo info = null;
	private List<ElanInfo> elanservice = null;
	Map<Integer, List<PwInfo>> tunnelIdAndPwInfoListMap = null;
	List<PwInfo> pwInfoLists = new ArrayList<PwInfo>();
	List<Tunnel> tunnelList = null; //
	Map<Integer, List<Tunnel>> siteIdAndTunnelsMap = null;
	private TNetwork network = null;
	private TunnelTopoPanel tunnelTopoPanel=null;
	private List<Integer> pwIdList_before = new ArrayList<Integer>();//log日志记录数据变化时需要用到修改前的pwId集合
	/** Creates new form AddPWDialog */

	public AddElanDialog(JPanel jPanel1, boolean modal, int elanId, ElanInfo info) {

		try {
			this.info = info;
			this.setModal(modal);
			super.setTitle(ResourceUtil.srcStr(StringKeysTitle.TIT_CREATE_ELAN));
			this.elanBusinessPanel = (ElanBusinessPanel) jPanel1;
			initComponents();
			setLayout();
			clientComboxData(this.clientComboBox);
			tunnelTopoPanel=new TunnelTopoPanel();
			jSplitPane1.setRightComponent(tunnelTopoPanel);
			network= tunnelTopoPanel.getNetWork();
			setBtnListeners();
			if (info != null) {
				initData(info.getServiceId());
			}
			showTopoByTunnel();
			UiUtil.showWindow(this, 1200, 700);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}

	private void setBtnListeners() {
		// 自动命名事件
		jButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonActionPerformed(evt);
			}
		});

		okBtn.addActionListener(new MyActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okBtnActionPerformed(evt);
			}
			@Override
			public boolean checking() {
				return checkValue();
			}
		});
		network.addElementClickedActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Element element = (Element) e.getSource();
				if(element!=null&&element instanceof Link){
					if(element.getUserObject()!=null&&element.getBusinessObject()==null){
						TUIManager.registerAttachment("SegmenttopoTitle", TopoAttachment.class,1, (int) element.getX(), (int) element.getY());
						PwInfo pwinfo =  (PwInfo)element.getUserObject();
						element.setBusinessObject(pwinfo.getPwName());
						element.addAttachment("SegmenttopoTitle");
					}else{
						element.removeAttachment("SegmenttopoTitle");
						element.setBusinessObject(null);
					}    
				}
			}
		});
	}
	private void showTopoByTunnel() throws Exception {
		TNetwork network = null;
		try {
			// 加载拓扑
			final List<PwInfo> pwInfoList = this.getPwList();
			if (info != null) {
//				tunnelTopoPanel.boxDataByPwsOther(pwInfoLists, selectNodeList);
				// 给PW列表赋初始值
				pwInfoTable.clear();
				pwInfoTable.initData(pwInfoLists);
			}
//			else {
				tunnelTopoPanel.boxDataByPws(pwInfoList);
//			}
			network = tunnelTopoPanel.getNetWork();
//			network.doLayout(TWaverConst.LAYOUT_CIRCULAR);
			initTopoData();
			this.setMenu();
		} catch (Exception e) {
			throw e;
		}finally{
			network=null;
		}
	}

	/**
	 * 修改时初始化拓扑数据，link为蓝色，网元有标识
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void initTopoData() throws Exception {
		List<Element> elementList = null;
		int pwid = 0;
		Link link = null;
		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		SiteInst siteInst = null;
		try {
			// 获取拓扑中全部element
			elementList = tunnelTopoPanel.getNetWork().getDataBox().getAllElements();

			for (Element element : elementList) {
				// 如果元素为link，验证pw是否存在，如果存在则把此pw变色，并且标注此link的from、to的node
				if (element instanceof Link) {
					link = (Link) element;
					pwid = ((PwInfo) link.getUserObject()).getPwId();
					if (this.isPwInEtree(pwid)) {
						link.putLinkColor(Color.BLUE);
						setSelectNodeList(link.getFrom(),nodeMap,siteInst);
						setSelectNodeList(link.getTo(),nodeMap,siteInst);
					}
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			elementList = null;
			link = null;
		}
	}
	
	
	private void setSelectNodeList(Node node,Map<Integer, Node> nodeMap,SiteInst siteInst)
	{
		node.setBusinessObject(ResourceUtil.srcStr(StringKeysObj.OBJ_SELECTED));
		node.addAttachment("topoTitle");
		siteInst = (SiteInst) node.getUserObject();
		if(null == nodeMap.get(siteInst.getSite_Inst_Id()))
		{
			nodeMap.put(siteInst.getSite_Inst_Id(),node);
			this.selectNodeList.add(node);
		}
	}
	
	/**
	 * 加载elan的菜单
	 */
	private void setMenu() {
		TNetwork network =tunnelTopoPanel.getNetWork();

		// 设置拓扑的右键菜单
		network.setPopupMenuGenerator(new PopupMenuGenerator() {
			@Override
			public JPopupMenu generate(TView tview, MouseEvent mouseEvent) {
				JPopupMenu menu = new JPopupMenu();
				AddElanMenu addElanMenu = new AddElanMenu(AddElanDialog.this, tview);
				Link link = null;
				try {
					if (!tview.getDataBox().getSelectionModel().isEmpty()) {
						final Element element = tview.getDataBox().getLastSelectedElement();
						if (element instanceof Node) {
							// 如果此node没有设置根或者叶子，加载菜单为 设置根节点、设置叶子节点
							if (element.getBusinessObject() == null || "".equals(element.getBusinessObject().toString())) {
								menu.add(addElanMenu.createMenu(StringKeysMenu.MENU_CONFIG, element, info,elanservice));
							} else { 
								// 否则加载取消设置、选择端口菜单
//								if(null==info){
								menu.add(addElanMenu.createMenu(StringKeysMenu.MENU_CANEL_CONFIG, element, info,elanservice));
//								}
								menu.add(addElanMenu.createMenu(StringKeysMenu.MENU_SELECT_PORT, element, info,elanservice));
							
							}
						} else if (element instanceof Link) {
							// 如果是link 并且颜色是绿色 说明没有被选中，加载 设置路径菜单
							link = (Link) element;
							if (link.getLinkColor() == Color.GREEN) {
								menu.add(addElanMenu.createMenu(StringKeysMenu.MENU_SELECT_PATH, element, info,elanservice));
							} else {
								// 否则加载取消设置菜单
								if(null==info){
									menu.add(addElanMenu.createMenu(StringKeysMenu.MENU_CANEL_CONFIG, element, info,elanservice));
								}
							}
						}

					}
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
				return menu;
			}

		});
	}
	/**
	 * 加载拓扑图时 获取pw集合
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<PwInfo> getPwList() throws Exception {
		List<PwInfo> pwinfoList = null;
		PwInfo pwInfo = null;
		PwInfoService_MB pwService = null;
		List<PwInfo> pwinfoList_result = null;
		ListingFilter filter=null;
		try {

			pwinfoList_result = new ArrayList<PwInfo>();
//			if(null == this.elanservice)
//			{
				pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
				pwInfo = new PwInfo();
				pwInfo.setType(EPwType.ETH);
				filter=new ListingFilter();
				pwinfoList = (List<PwInfo>) filter.filterList(pwService.selectbyType(pwInfo)); // 查询所有pw
				List<Integer> updatePwIdList = new ArrayList<Integer>();//修改时使用，存放修改的这条数据所包含的pw
				if(null != this.elanservice){
					for (ElanInfo elan : this.elanservice) {
						if(elan.getPwId() > 0){
							updatePwIdList.add(elan.getPwId());
						}
					}
				}
				for (PwInfo info : pwinfoList) {
					// 如果是pw被使用，从集合中移除
					if(null == this.elanservice){
						if (info.getRelatedServiceId() == 0) {
							pwinfoList_result.add(info);
						}
					}else{
						if (info.getRelatedServiceId() == 0 || updatePwIdList.contains(info.getPwId())) {
							pwinfoList_result.add(info);
						}
					}
				}	
//			}else
//			{
				// 修改操作，加载此根下的所有pw。
//				this.getRootPw(pwinfoList_result);
//			}

		} catch (Exception e) {
			throw e;
		} finally {
			pwInfo = null;
			UiUtil.closeService_MB(pwService);
			filter=null;
		}
		return pwinfoList_result;

	}

	/**
	 * 获取根下的所有pw
	 * 
	 * @param pwinfoList_result
	 *            pw结果集，把等到的结果放入此对象中
	 * @return
	 * @throws Exception
	 */
	private void getRootPw(List<PwInfo> pwinfoList_result) throws Exception {
		List<PwInfo> pwInfos = null;
		PwInfoService_MB pwService = null;
		PwInfo pwInfo = null;
		try {
			// 根据网元ID查询pw集合
			pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			pwInfos = new ArrayList<PwInfo>();
			for(ElanInfo elanInfo : this.elanservice)
			{
				pwInfo = new PwInfo();
				pwInfo.setASiteId(elanInfo.getaSiteId());
				pwInfo.setZSiteId(elanInfo.getzSiteId());
				pwInfos.addAll(pwService.select(pwInfo));
			}
			for (PwInfo pwInst : pwInfos) {
				// 如果pw没被使用，或者pw在要修改的etree中存在，就显示到拓扑中
				// 已经被使用，并且在etree中存在。 是要在拓扑中显示的
				if (pwInst.getRelatedServiceId() == 0 || this.isPwInEtree(pwInst.getPwId())) {
					if(pwInst.getType() != null && pwInst.getType() == EPwType.ETH){
						pwinfoList_result.add(pwInst);
					}
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(pwService);
			pwInfos = null;
			pwInfo = null;
		}
	}
	
	/**
	 * 验证pw是否在etree中存在
	 * 
	 * @return true 存在 false 不存在
	 * @throws Exception
	 */
	private boolean isPwInEtree(int pwid) throws Exception {
		boolean flag = false;
		try {
          if(null != this.elanservice && !this.elanservice.isEmpty())
          {
    		for (ElanInfo elanInfo : this.elanservice) {
				if (elanInfo.getPwId() == pwid) {
					flag = true;
					break;
				}
			}  
          }
		} catch (Exception e) {
			throw e;
		}
		return flag;
	}
	// 自动命名事件
	private void jButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			ElanInfo elaninfo = new ElanInfo();
			elaninfo.setIsSingle(0);
			AutoNamingUtil autoNamingUtil=new AutoNamingUtil();
			String autoNaming = (String) autoNamingUtil.autoNaming(elaninfo, null, null);
			nameTextField.setText(autoNaming);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}

	}

	/**
	 * 设置端口
	 * 
	 * @param acListDialog
	 * @param siteElement
	 * @throws Exception
	 */
	protected void setport(AcListDialog acListDialog, Element siteElement) throws Exception {
		List<AcPortInfo>  acPortInfoList= null;
		List<AcPortInfo> acPortList = null;
		try {
			acPortInfoList = acListDialog.getAcPortInfoList();

			if(acPortInfoList != null && acPortInfoList.size()>0)
			{
				if(acPortInfoList.size() > 10)
				{
					DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_EXCEEDACELANNUMBER));
					return;
				}
				// 如果有同网元的端口，清除掉。添加新的
				acPortList = new ArrayList<AcPortInfo>();
				for (AcPortInfo acPortInfo_table : this.branchAcTable.getAllElement()) {
					if(!isInsertTabel(acPortInfo_table.getSiteId(),acPortInfoList))
					{
						acPortList.add(acPortInfo_table);
					}
				}
				acPortList.addAll(acPortInfoList);
				this.branchAcTable.clear();
				this.branchAcTable.initData(acPortList);
			}

		} catch (Exception e) {
			throw e;
		} finally {
		    acPortInfoList= null;
			acPortList = null;
		}

	}

	
	private boolean isInsertTabel(int siteId,List<AcPortInfo> acPortList)
	{
		boolean flag = false;
		try 
		{
			for(AcPortInfo acPortInst :acPortList)
			{
				if (siteId == acPortInst.getSiteId()) {
					flag = true;
					break;
				}
			}
			
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}
		return flag;
	}
	
	private void initComponents() throws Exception {

		this.lblMessage = new JLabel();
		okBtn = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SAVE),true,RootFactory.COREMODU,this);
		jButton = new javax.swing.JButton(ResourceUtil.srcStr(StringKeysLbl.LBL_AUTO_NAME));
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel3 = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_NAME));
		portLabel = new javax.swing.JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_PORT_LIST));
		activateStatusLabel = new javax.swing.JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_ACTIVITY_STATUS));
		isActivateCBox = new javax.swing.JCheckBox();
		isActivateCBox.setEnabled(true);
		pwlist = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_PW_LIST));

		nameTextField = new PtnTextField(true, PtnTextField.STRING_MAXLENGTH, this.lblMessage, this.okBtn, this);
		client = new javax.swing.JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_CLIENT_NAME));
		clientComboBox = new javax.swing.JComboBox();

		// pw和ac叶子端口的tabel
		this.branchAcTable = new ViewDataTable<AcPortInfo>(this.ACTABLENAME);
		this.pwInfoTable = new ViewDataTable<PwInfo>(this.PWTABLENAME);

		this.branchAcTable.getTableHeader().setResizingAllowed(true);
		this.branchAcTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		this.branchAcTable.setTableHeaderPopupMenuFactory(null);
		this.branchAcTable.setTableBodyPopupMenuFactory(null);

		this.pwInfoTable.getTableHeader().setResizingAllowed(true);
		this.pwInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		this.pwInfoTable.setTableHeaderPopupMenuFactory(null);
		this.pwInfoTable.setTableBodyPopupMenuFactory(null);

		this.jscrollPane_ac = new JScrollPane();
		this.jscrollPane_pw = new JScrollPane();

		this.jscrollPane_ac.setViewportView(this.branchAcTable);
		this.jscrollPane_pw.setViewportView(this.pwInfoTable);

	}

	private void setLayout() {
		this.add(this.jSplitPane1);
		this.jSplitPane1.setLeftComponent(this.jPanel3);
		this.jPanel3.setPreferredSize(new Dimension(260, 700));
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 50, 160, 50 };
		layout.columnWeights = new double[] { 0, 0.1, 0 };
		layout.rowHeights = new int[] { 25, 30, 30, 150, 150, 30, 30, 15, 30, 10 };
		layout.rowWeights = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.2 };
		this.jPanel3.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 5, 5);
		layout.setConstraints(this.lblMessage, c);
		this.jPanel3.add(this.lblMessage);

		/** 第一行 名称 */
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 5, 5);
		layout.setConstraints(nameLabel, c);
		this.jPanel3.add(nameLabel);
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		layout.addLayoutComponent(nameTextField, c);
		this.jPanel3.add(nameTextField);
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		layout.addLayoutComponent(this.jButton, c);
		this.jPanel3.add(this.jButton);

		/** 第二行 客户关系 */
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 10, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		layout.setConstraints(this.client, c);
		this.jPanel3.add(this.client);
		c.gridx = 1;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		layout.addLayoutComponent(this.clientComboBox, c);
		this.jPanel3.add(this.clientComboBox);

		/** 第3行 ac列表 */
		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 10, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		layout.setConstraints(this.portLabel, c);
		this.jPanel3.add(this.portLabel);
		c.gridx = 1;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		layout.addLayoutComponent(this.jscrollPane_ac, c);
		this.jPanel3.add(this.jscrollPane_ac);
		/** 第4行 pw列表 */
		c.gridx = 0;
		c.gridy = 4;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 10, 5, 5);
		layout.setConstraints(this.pwlist, c);
		this.jPanel3.add(this.pwlist);
		c.gridx = 1;
		c.gridy = 4;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		layout.addLayoutComponent(this.jscrollPane_pw, c);
		this.jPanel3.add(this.jscrollPane_pw);

		/** 第7行 激活状态 */
		c.gridx = 0;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 10, 5, 5);
		layout.setConstraints(this.activateStatusLabel, c);
		this.jPanel3.add(this.activateStatusLabel);
		c.gridx = 1;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.CENTER;
		layout.addLayoutComponent(this.isActivateCBox, c);
		this.jPanel3.add(this.isActivateCBox);

		/** 第9行 确定按钮 空出一行 */
		c.gridx = 2;
		c.gridy = 7;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 10, 5, 5);
		c.fill = GridBagConstraints.NONE;
		layout.setConstraints(this.okBtn, c);
		this.jPanel3.add(this.okBtn);
	}

	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {
		DispatchUtil elanDispatch = null;
		String resultStr = "";
		ControlKeyValue client = null;
		List<AcPortInfo> acPortInfoList = null;
		List<PwInfo> pwInfoList = null;
		List<ElanInfo> elanInfoList = null;
		ElanInfo elanInfo = null;
		AcPortInfoService_MB acService = null;
		try {
			elanInfoList = new ArrayList<ElanInfo>();
			pwInfoList = this.pwInfoTable.getAllElement();
			acPortInfoList = this.branchAcTable.getAllElement();
			client = (ControlKeyValue) clientComboBox.getSelectedItem();
			acService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			
			// 封装下发
			for (int i = 0; i < pwInfoList.size(); i++) {
				PwInfo pwinfo = pwInfoList.get(i);
				elanInfo = new ElanInfo();
				elanInfo.setCreateTime(DateUtil.getDate(DateUtil.FULLTIME));
				if(isActivateCBox.isSelected()){
					elanInfo.setActivatingTime(elanInfo.getCreateTime());
				}else{
					elanInfo.setActivatingTime(null);
				}
				elanInfo.setCreateUser(ConstantUtil.user.getUser_Name());
				elanInfo.setPwId(pwinfo.getPwId());
				elanInfo.setActiveStatus(isActivateCBox.isSelected() ? EActiveStatus.ACTIVITY.getValue() : EActiveStatus.UNACTIVITY.getValue());
				elanInfo.setaSiteId(pwinfo.getASiteId());
				elanInfo.setzSiteId(pwinfo.getZSiteId());
				elanInfo.setName(nameTextField.getText());
				elanInfo.setAportId(pwinfo.getaPortConfigId());
				elanInfo.setZportId(pwinfo.getzPortConfigId());
				//-----------------------------------------------------为修改前的ac赋值
//				analysisAcUpdateBeforData(pwinfo.getASiteId(),elanInfo,acPortInfoList,acService,0);
//				analysisAcUpdateBeforData(pwinfo.getZSiteId(),elanInfo,acPortInfoList,acService,1);
				//-----------------------------------------------------
				//设置多AC 
				mostAcString(pwinfo,acPortInfoList,elanInfo);
				
				elanInfo.setServiceType(EServiceType.ELAN.getValue());
				if (!"".equals(client.getId())) {
					elanInfo.setClientId(Integer.parseInt(client.getId()));
				}
				elanInfoList.add(elanInfo);	
			}
			elanDispatch = new DispatchUtil(RmiKeys.RMI_ELAN);
			
			if(this.info!=null){
				integrateElanList(elanInfoList);
				VplsInfo vplsBefore = this.getVplsBefore(null, client, 0, null);
				List<Integer> siteIdList_before = this.getSiteIdList_before(vplsBefore.getElanInfoList());
				resultStr = elanDispatch.excuteUpdate(elanservice);
				//添加日志记录
				VplsInfo vplsInfo = this.getVplsBefore(null, client, 0, pwInfoList);
				List<Integer> siteIdList = this.getSiteIdList_before(vplsInfo.getElanInfoList());
				if(siteIdList_before.size() > siteIdList.size()){
					this.sortElanList(vplsBefore.getElanInfoList(), siteIdList);
					for (Integer siteId : siteIdList_before) {
						AddOperateLog.insertOperLog(okBtn, EOperationLogType.ELANUPDATE.getValue(), resultStr, vplsBefore, vplsInfo, siteId, vplsInfo.getVplsName(), "elan");
					}
				}else{
					this.sortElanList(vplsInfo.getElanInfoList(), siteIdList_before);
					for (Integer siteId : siteIdList) {
						AddOperateLog.insertOperLog(okBtn, EOperationLogType.ELANUPDATE.getValue(), resultStr, vplsBefore, vplsInfo, siteId, vplsInfo.getVplsName(), "elan");
					}
				}
			}else{
				resultStr = elanDispatch.excuteInsert(elanInfoList);
				//添加日志记录
				VplsInfo vplsInfo = this.getVplsBefore(elanInfoList, client, 1, pwInfoList);
				List<Integer> siteIdList = this.getSiteIdList_before(vplsInfo.getElanInfoList());
				for (Integer siteId : siteIdList) {
					AddOperateLog.insertOperLog(okBtn, EOperationLogType.ELANINSERT.getValue(), resultStr, null, vplsInfo, siteId, vplsInfo.getVplsName(), "elan");
				}
			}
			DialogBoxUtil.succeedDialog(this, resultStr);
			this.dispose();
			TWaverUtil.clearImageIconCache();
			if (null != this.elanBusinessPanel) {
				this.elanBusinessPanel.getController().refresh();
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(acService);
		}
	}
	
	/**
	 * 需要将List<ElanInfo>排序，便于日志记录时比对
	 * 如：修改前是1,2,3三个网元，修改后是1,3两个网元，需要将之前的集合调整为1,3,2的顺序
	 * @param elanInfoList需要调整顺序的集合
	 * @param siteIdList调整的顺序
	 */
	private void sortElanList(List<ElanInfo> elanInfoList, List<Integer> siteIdList) {
		List<ElanInfo> elanList = new ArrayList<ElanInfo>();
		for (int siteId : siteIdList) {
			for (ElanInfo elanInfo : elanInfoList) {
				if(elanInfo.getaSiteId() == siteId){
					elanList.add(elanInfo);
				}
			}
		}
		for (ElanInfo elanInfo : elanInfoList) {
			if(!siteIdList.contains(elanInfo.getaSiteId()) && elanInfo.getaSiteId() > 0){
				elanList.add(elanInfo);
			}
		}
		elanInfoList.clear();
		elanInfoList.addAll(elanList);
	}

	private VplsInfo getVplsBefore(List<ElanInfo> elanInfoList, ControlKeyValue client, int type, List<PwInfo> pwInfoList) {
		ElanInfoService_MB service = null;
		SiteService_MB siteService = null;
		ClientService_MB clientService = null;
		PwInfoService_MB pwService = null;
		VplsInfo vplsInfo = new VplsInfo();
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			if(type == 0){
				service = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
				if(pwInfoList == null){
					elanInfoList = service.selectElanbypwid(this.pwIdList_before);
				}else{
					List<Integer> pwIdList = new ArrayList<Integer>();
					for (PwInfo pwInfo : pwInfoList) {
						pwIdList.add(pwInfo.getPwId());
					}
					elanInfoList = service.selectElanbypwid(pwIdList);
				}
				clientService = (ClientService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CLIENTSERVICE);
				if(elanInfoList.get(0).getClientId() > 0){
					vplsInfo.setClientName(clientService.select(elanInfoList.get(0).getClientId()).get(0).getName());
				}
			}else{
				if(((Client)client.getObject()) != null){
					vplsInfo.setClientName(((Client)client.getObject()).getName());
				}
			}
			List<Integer> siteIdList = this.getSiteIdList_before(elanInfoList);
			List<ElanInfo> elanList_log = new ArrayList<ElanInfo>();
			for (int siteId : siteIdList) {
				ElanInfo elanLog = new ElanInfo();
				elanLog.setNodeName(siteService.getSiteName(siteId));
				for (ElanInfo elan : elanInfoList) {
					if(elan.getaSiteId() == siteId){
						elanLog.setAcNameList(this.getAcNameList(elan.getAmostAcId()));
						break;
					}else if(elan.getzSiteId() == siteId){
						elanLog.setAcNameList(this.getAcNameList(elan.getZmostAcId()));
						break;
					}
				}
				List<CommonBean> pwNameList = new ArrayList<CommonBean>();
				for (ElanInfo elan : elanInfoList) {
					if(elan.getaSiteId() == siteId || elan.getzSiteId() == siteId){
						CommonBean cb = new CommonBean();
						cb.setPwName4vpls(pwService.selectByPwId(elan.getPwId()).getPwName());
						pwNameList.add(cb);
					}
				}
				elanLog.setaSiteId(siteId);
				elanLog.setPwNameList(pwNameList);
				elanList_log.add(elanLog);
			}
			vplsInfo.setVplsName(elanInfoList.get(0).getName());
			vplsInfo.setActiveStatus(elanInfoList.get(0).getActiveStatus());
			vplsInfo.setElanInfoList(elanList_log);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(pwService);
			UiUtil.closeService_MB(service);
			UiUtil.closeService_MB(siteService);
			UiUtil.closeService_MB(clientService);
		}
		return vplsInfo;
	}

	private List<Integer> getSiteIdList_before(List<ElanInfo> elanInfoList) {
		List<Integer> siteIdList = new ArrayList<Integer>();
		for (ElanInfo elanInfo : elanInfoList) {
			if(!siteIdList.contains(elanInfo.getaSiteId()) && elanInfo.getaSiteId() > 0){
				siteIdList.add(elanInfo.getaSiteId());
			}
			if(!siteIdList.contains(elanInfo.getzSiteId()) && elanInfo.getzSiteId() > 0){
				siteIdList.add(elanInfo.getzSiteId());
			}
		}
		return siteIdList;
	}
	
	/**
	 * 根据acId数组获取ac名称
	 * @param amostAcId
	 * @return
	 */
	private List<CommonBean> getAcNameList(String amostAcId) {
		AcPortInfoService_MB acService = null;
		List<CommonBean> acNameList = null;
		try {
			if(amostAcId != null){
				acService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
				List<Integer> acIdList = new ArrayList<Integer>();
				if(amostAcId.length() > 1){
					for (String id : amostAcId.split(",")) {
						acIdList.add(Integer.parseInt(id.trim()));
					}
				}else{
					acIdList.add(Integer.parseInt(amostAcId));
				}
				acNameList = new ArrayList<CommonBean>();
				List<AcPortInfo> acList = acService.select(acIdList);
				for (AcPortInfo acInfo : acList) {
					CommonBean acName = new CommonBean();
					acName.setAcName(acInfo.getName());
					acNameList.add(acName);
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(acService);
		}
		return acNameList;
	}

	private void integrateElanList(List<ElanInfo> elanInfoList) {
		ElanInfo elanInfo_update = null;
		if(null==this.elanservice)
		{
			return ;
		}
		//先把所有修改的Elan数据改成删除状态,之后匹配数据,会把状态该成其他状态
		for(ElanInfo elanInst : elanservice)
		{
			elanInst.setAction(3);
		}
		
		//收集所有新数据
		for(ElanInfo elanInst : elanInfoList)
		{
			elanInfo_update = this.findElan(elanInst);
			if(null != elanInfo_update)
			{
				this.integrateElan(elanInfo_update,elanInst);
			}else
			{
				//如果为空 说明为新增加的PW
				elanInst.setCreateTime(elanservice.get(0).getCreateTime());
				if(isActivateCBox.isSelected()){
					elanInst.setActivatingTime(this.elanservice.get(0).getActivatingTime());
				}else{
					elanInst.setActivatingTime(null);
				}
				elanInst.setCreateUser(elanservice.get(0).getCreateUser());
				elanInst.setServiceId(elanservice.get(0).getServiceId());
				if(isExit(elanInst.getaSiteId()))
				{
					elanInst.setAxcId(elanservice.get(0).getAxcId());
				}else if(isExit(elanInst.getzSiteId()))
				{
					elanInst.setZxcId(elanservice.get(0).getZxcId());
				}
				elanInst.setAction(2);
				elanservice.add(elanInst);
			}
		}
		//如果是删除节点的话将改节点的网元ID取来
		for(ElanInfo elanInfo : elanservice)
		{
			if(elanInfo.getAction() == 3)
			{
				setElanInfoSiteID(elanInfo);
			}
		}
	}
	
	private void setElanInfoSiteID(ElanInfo elanInfo) {
	
		if(!isExit(elanInfo.getaSiteId()))
		{
			elanInfo.setSiteId(elanInfo.getaSiteId());
		}
		if(!isExit(elanInfo.getzSiteId()))
		{
			elanInfo.setSiteId(elanInfo.getzSiteId());
		}
     }
	
	private boolean isExit(int siteId)
	{
		boolean flag = false;
		SiteInst siteInst = null;
		try {
			for(Node node : selectNodeList)
			{
				siteInst = (SiteInst) node.getUserObject();
				if(siteId == siteInst.getSite_Inst_Id())
				{
					return true;
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			siteInst = null;
		}
		return flag;
	}

	private void integrateElan(ElanInfo elanInfoUpdate, ElanInfo elanInst) {
		PwInfoService_MB pwInfoService = null;
		PwInfo pwinfo = null;
		try {
			pwInfoService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			if(elanInfoUpdate.getPwId() != elanInst.getPwId())
			{
				pwinfo = new PwInfo();
				pwinfo.setPwId(elanInfoUpdate.getPwId());
				elanInfoUpdate.setBeforePw(pwInfoService.queryByPwId(pwinfo));
				elanInfoUpdate.setPwId(elanInst.getPwId());
				elanInfoUpdate.setAction(1);
			}
			// 如果修改了根端口 取之前的根端口对象，并且更新etreeInfo_update中的aAcId、BeforeRootAc、action=1字段
			if(null != elanInfoUpdate.getAmostAcId()&& !isSame(elanInfoUpdate.getAmostAcId(),elanInst.getAmostAcId()))
			{
				setBerforeAAcList(elanInfoUpdate,elanInst.getAmostAcId(),elanInfoUpdate.getAmostAcId(),0);
				elanInfoUpdate.setAmostAcId(elanInst.getAmostAcId());
				elanInfoUpdate.setAction(1);
			}
			if(null != elanInfoUpdate.getZmostAcId() &&!isSame(elanInfoUpdate.getZmostAcId(),elanInst.getZmostAcId()))
			{
				setBerforeAAcList(elanInfoUpdate,elanInst.getZmostAcId(),elanInfoUpdate.getZmostAcId(),1);
				elanInfoUpdate.setZmostAcId(elanInst.getZmostAcId());
				elanInfoUpdate.setAction(1);
			}
			
			elanInfoUpdate.setName(elanInst.getName());
			elanInfoUpdate.setActiveStatus(elanInst.getActiveStatus());
			elanInfoUpdate.setClientId(elanInst.getClientId());
			
			// 如果action还等于3 说明上面三个条件没有成立，此时给此属性赋0=没有改变pw
			if(elanInfoUpdate.getAction() == 3)
			{
				elanInfoUpdate.setAction(0);
			}
			
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			UiUtil.closeService_MB(pwInfoService);
		}
	}
	
	 private boolean isSame(String updateAcString,String newAcString)
	  {
		  boolean flag = false;
		  UiUtil uiutil = null;
		  Set<Integer> updateAcSet = null;
		  Set<Integer> newAcSet = null;
		  try 
		  {
	        uiutil = new UiUtil();
	        updateAcSet = uiutil.getAcIdSets(updateAcString);
	        newAcSet = uiutil.getAcIdSets(newAcString);
	        if(updateAcSet.size() == newAcSet.size())
	        {
	        	newAcSet.removeAll(updateAcSet);
	 	        if(newAcSet.size() == 0)
	 	        {
	 	        	flag = true;
	 	        }
	        }
		  } catch (Exception e) 
		  {
			ExceptionManage.dispose(e, getClass());
		 }finally
		 {
			  uiutil = null;
			  updateAcSet = null;
			  newAcSet = null;
		 }
		return flag;
	  }
	
	private ElanInfo findElan(ElanInfo elanInst) {
		for(ElanInfo elanInfo : elanservice)
		{
			
//			if(elanInfo.getPwId() == elanInst.getPwId())//这种比较方法有问题
			if((elanInfo.getaSiteId() == elanInst.getaSiteId() && elanInfo.getzSiteId() == elanInst.getzSiteId()) || 
					(elanInfo.getaSiteId() == elanInst.getzSiteId() && elanInfo.getzSiteId() == elanInst.getaSiteId())){
				return elanInfo;
			}
		}
		return null;
	}
	
	/**
	 * 给修改以前的AC赋值
	 * @param elanInfoAction
	 * @param mostAcId
	 * @param acIdList
	 */                           
	private void setBerforeAAcList(ElanInfo elanInfoAction, String mostAcId,String oldMostACId,int label) 
	{
		String[] acIds = oldMostACId.split(",");
		String[] acIdsUdate = mostAcId.split(",");
		Set<Integer> acSet = null;
		List<Integer> acList = null;
		AcPortInfoService_MB acInfoService = null;
		try {
			acInfoService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			acSet = new HashSet<Integer>();
			acList = new ArrayList<Integer>();
			for(String acId : acIds)
			{
				if(!isExist(acId.trim(),acIdsUdate))
				{
					acSet.add(Integer.parseInt(acId.trim()));
				}
			}
			acList = new ArrayList<Integer>(acSet);
			if(!acList.isEmpty())
			{
				if(label == 0)
				{
					elanInfoAction.setBeforeAAcList(acInfoService.select(acList));
				}else
				{
					elanInfoAction.setBeforeZAcList(acInfoService.select(acList));
				}
			}else if((acIdsUdate.length > acIds.length)&&acList.isEmpty())
			{
			   if(label == 0)
				{
					elanInfoAction.setBeforeAAcList(new ArrayList<AcPortInfo>());
				}else
				{
					elanInfoAction.setBeforeZAcList(new ArrayList<AcPortInfo>());
				}
			}
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			UiUtil.closeService_MB(acInfoService);
			acSet = null;
			acList = null;
			acIds = null;
		}
	}
	
	/**
	 * 对比之前使用的找出之前替换掉的AC
	 * @param acId
	 * @param useAcPortList
	 * @return
	 */
	private boolean isExist(String acId,String[]  useAcPortList)
	{
		boolean fglag = false;
		try 
		{
			for(String acIds : useAcPortList)
			{
				if(acIds.trim().equals(acId))
				{
					fglag = true;
					break;
				}
			}
			
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}
		return fglag;
	}
	private void analysisAcUpdateBeforData(int aSiteId, ElanInfo elanInfo,List<AcPortInfo> acPortInfoList,AcPortInfoService_MB acService,int label) {
		List<Integer> acAIds = new ArrayList<Integer>();
		Set<Integer> acIdSet = new HashSet<Integer>();
		List<Integer> acIdList = null;
		UiUtil uiutil = null;
		try {
			
			for (AcPortInfo acPortInfo : acPortInfoList) {
				if(aSiteId == acPortInfo.getSiteId())
				{
					acAIds.add(acPortInfo.getId());
				}
			}
			uiutil = new UiUtil();
			if(label ==0 )
			{
				acIdSet = uiutil.getAcIdSets(elanInfo.getAmostAcId());
			}else
			{
				acIdSet = uiutil.getAcIdSets(elanInfo.getZmostAcId());	
			}
			acIdList = new ArrayList<Integer>(acIdSet);
			acIdList.removeAll(acAIds);
			if(acIdList.size() >0)
			{
				if(label == 0)
				{
					elanInfo.setBeforeAAcList(acService.select(acIdList));
				}else
				{
					elanInfo.setBeforeZAcList(acService.select(acIdList));
				}
			}
			
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			acAIds = null;
			acIdSet = null;
			acIdList = null;
			uiutil = null;
		}
	}

	private void mostAcString(PwInfo pwinfo, List<AcPortInfo> acPortInfoList,ElanInfo elanInfo)
	{
		List<Integer> acAIds = new ArrayList<Integer>();
		List<Integer> aczIds = new ArrayList<Integer>();
		try {
			
			for (AcPortInfo acPortInfo : acPortInfoList) {
				if(pwinfo.getASiteId() == acPortInfo.getSiteId())
				{
					acAIds.add(acPortInfo.getId());
				}else if(pwinfo.getZSiteId() == acPortInfo.getSiteId())
				{
					aczIds.add(acPortInfo.getId());
				}
			}
		   if(acAIds.size()>0)
		   {
			   elanInfo.setAmostAcId(acAIds.toString().subSequence(1, acAIds.toString().length() -1).toString());
		   } 
		   if(aczIds.size()>0)
		   {
			   elanInfo.setZmostAcId(aczIds.toString().subSequence(1, aczIds.toString().length() -1).toString());
		   }
			
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			 acAIds = null;
			 aczIds = null;
		}
	}
	
	/**
	 * 验证值的正确性
	 * @return
	 */
	private boolean checkValue() {
		
		List<AcPortInfo> acPortInfoList = null;
		List<PwInfo> pwInfoList = null;
		boolean flag = false;
		try {
			pwInfoList = this.pwInfoTable.getAllElement();
			acPortInfoList = this.branchAcTable.getAllElement();
			// 验证名称
			VerifyNameUtil verifyNameUtil=new VerifyNameUtil();
			if (info != null) {
				if (!this.nameTextField.getText().trim().equals(this.elanservice.get(0).getName())) {
					if (verifyNameUtil.verifyName(EServiceType.ELAN.getValue(), this.nameTextField.getText().trim(), null)) {
						DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_NAME_EXIST));
						return false;
					}
				}
			} else {

				if (verifyNameUtil.verifyName(EServiceType.ELAN.getValue(), this.nameTextField.getText().trim(), null)) {
					DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_NAME_EXIST));
					return false;
				}
			}

			// 验证是否选择了节点
			if (this.selectNodeList.size() == 0) {
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_CHOOSE_SITE));
				return false;
			}

			// 验证AC数量
			
			if((this.selectNodeList.size() == 0 || acPortInfoList.size()== 0)||verifyAc())
			{
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_MUSTNETWORK_BEFORE));
				return false;	
			}

			if(pwInfoList==null||pwInfoList.size()==0){
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_PWANDACNOMATE));
				return false;
			}
			
			// 验证pw路径与叶子ac是否匹配
			if (!this.pwAndAcMate(pwInfoList, acPortInfoList)) {
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_PWANDACNOMATE));
				return false;
			}
			flag = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}
		return flag;
	}
	
   /***********验证AC的正确性******************/
	private boolean  verifyAc() 
	{
		boolean flga = false;
		SiteInst siteInst = null;
		try {
			for(Node siteNode : this.selectNodeList)
			{
				 siteInst = (SiteInst) siteNode.getUserObject();
				 if(!verifyExitAc(siteInst.getSite_Inst_Id()))
				 {
					 flga = true;
					 break;
				 }
			}
			
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, this.getClass());
		}finally
		{
			siteInst = null;
		}
		return flga;
	}

	private boolean verifyExitAc(int siteId) 
	{
		boolean flag = false;
		try {
			for(AcPortInfo acPortInfo :this.branchAcTable.getAllElement())
			{
				if(siteId == acPortInfo.getSiteId())
				{
					flag = true;
					break;
				}
			}
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}
	  return flag;
    }

	/**
	 *   验证pw和叶子网元AC是否匹配
	 * @param pwInfoList
	 *            所有pw路径
	 * @param acPortInfoList
	 *            叶子网元的ac集合
	 * @param rootSiteId
	 *            根网元id
	 * @return 通过验证true 没通过false
	 * @throws Exception
	 */
	private boolean pwAndAcMate(List<PwInfo> pwInfoList, List<AcPortInfo> acPortInfoList) throws Exception {

		boolean flag = true;
		List<Integer> siteIdList = null;
		try {
			// 把ac的所有网元放入集合中，做比较用
			siteIdList = new ArrayList<Integer>();
			for (AcPortInfo acPortInfo : acPortInfoList) {
				siteIdList.add(acPortInfo.getSiteId());
			}

			// 遍历pw,分别比较AZ端。如果有一端不在ac中。 说明验证不通过。返回false
			for (PwInfo pwInfo : pwInfoList) {

				if (!siteIdList.contains(pwInfo.getASiteId()) || !siteIdList.contains(pwInfo.getZSiteId())) {
					flag = false;
					break;
				}
			}
			

		} catch (Exception e) {
			throw e;
		} finally {
			siteIdList = null;
		}
		return flag;
	}

	/**
	 * 客户信息下拉列表
	 * 
	 * @param jComboBox1
	 */
	public void clientComboxData(JComboBox jComboBox1) {

		ClientService_MB service = null;
		List<Client> clientList = null;
		DefaultComboBoxModel defaultComboBoxModel = (DefaultComboBoxModel) clientComboBox.getModel();
		try {
			service = (ClientService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CLIENTSERVICE);
			clientList = service.refresh();
			defaultComboBoxModel.addElement(new ControlKeyValue("0", "", null));
			for (Client client : clientList) {
				defaultComboBoxModel.addElement(new ControlKeyValue(client.getId() + "", client.getName(), client));
			}
			clientComboBox.setModel(defaultComboBoxModel);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
			clientList = null;

		}
	}

	/**
	 * 初始化Elan修改界面
	 * 
	 * @param elanId
	 * @throws Exception
	 */
	private void initData(Integer elanId) throws Exception {
		super.setTitle(ResourceUtil.srcStr(StringKeysLbl.LBL_UPDATE_ELAN));
		this.elanservice = getElanInfo(elanId);
		List<Integer> pwIdList = new ArrayList<Integer>();
		if (elanservice != null) {
			this.nameTextField.setText(this.elanservice.get(0).getName());
			this.isActivateCBox.setSelected(this.elanservice.get(0).getActiveStatus() == 1 ? true : false);
			for (ElanInfo elanInfo : elanservice) {
				pwIdList.add(elanInfo.getPwId());
				pwIdList_before.add(elanInfo.getPwId());
			}
			pwInfoLists = this.getPwInfoListByPwId(pwIdList);
			tunnelList = this.getTunnelByPw();
			siteIdAndTunnelsMap = getSiteIdAndTunnelsMap(tunnelList);
			// showTopoByTunnelOther();
			// 初始化已选择的pw列表
			// initPwList();
			// 初始化ac端口
			initAcList(null);
			super.getComboBoxDataUtil().comboBoxSelect(this.clientComboBox, this.elanservice.get(0).getClientId()+"");
		}
	}

	// 初始化ac端口
	private void initAcList(ElanInfo elanInfoOther) {
		Set<Integer> acIdSet = new HashSet<Integer>();
		List<Integer> acIdList = new ArrayList<Integer>();
		AcPortInfoService_MB acService = null;
		List<AcPortInfo> acInfoList = null;
		UiUtil uiutil = null;
		try {
			uiutil = new UiUtil();
			acService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			if (elanInfoOther != null) {
				acIdSet.add(elanInfoOther.getaAcId());
				acIdSet.add(elanInfoOther.getzAcId());
				acIdList.addAll(acIdSet);
				acInfoList = acService.select(acIdList);
				this.branchAcTable.addData(acInfoList);
			} else {
				for (ElanInfo elanInfo : elanservice) {
					acIdSet.addAll(uiutil.getAcIdSets(elanInfo.getAmostAcId()));
					acIdSet.addAll(uiutil.getAcIdSets(elanInfo.getZmostAcId()));
				}
				acIdList.addAll(acIdSet);
				acInfoList = acService.select(acIdList);
				this.branchAcTable.clear();
				this.branchAcTable.initData(acInfoList);
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(acService);
			uiutil = null;
		}
	}


	private Map<Integer, List<Tunnel>> getSiteIdAndTunnelsMap(List<Tunnel> tunnelList) {
		Map<Integer, List<Tunnel>> map = new HashMap<Integer, List<Tunnel>>();
		Set<Integer> siteIdSet = new HashSet<Integer>();
		List<Tunnel> t = null;
		for (Tunnel tunnel : tunnelList) {
			siteIdSet.add(tunnel.getASiteId());
			siteIdSet.add(tunnel.getZSiteId());
		}
		for (Integer siteId : siteIdSet) {
			t = new ArrayList<Tunnel>();
			for (Tunnel tunnel : tunnelList) {
				if (siteId == tunnel.getASiteId() || siteId == tunnel.getZSiteId()) {
					t.add(tunnel);
				}
			}
			map.put(siteId, t);
		}
		return map;
	}

	/**
	 * 得到tunnel上有pw的所有tunnel
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Tunnel> getTunnelByPw() throws Exception {
		TunnelService_MB tunnelService = null;
		Set<Tunnel> tunnelSet = new HashSet<Tunnel>();
		List<Tunnel> tunnelList = new ArrayList<Tunnel>();
		ListingFilter filter=null;
		try {
			tunnelService = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			Tunnel tunnel = null;
			for (PwInfo info : pwInfoLists) {
				tunnel = new Tunnel();
				tunnel.setTunnelId(info.getTunnelId());
				filter=new ListingFilter();
				tunnelList=(List<Tunnel>)filter.filterList(tunnelService.select(tunnel));
				tunnelSet.addAll(tunnelList);
			}
			tunnelList.addAll(tunnelSet);
			return tunnelList;
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			filter=null;
			UiUtil.closeService_MB(tunnelService);
		}
		return tunnelList;
	}

	/**
	 * 得到pw
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<PwInfo> getPwInfoListByPwId(List<Integer> pwIdList) throws Exception {
		PwInfoService_MB service = null;
		List<PwInfo> pwInfoList = new ArrayList<PwInfo>();
		PwInfo pwInfo = null;

		try {
			service = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			pwInfo = new PwInfo();
			for (Integer pwId : pwIdList) {
				pwInfo.setPwId(pwId);
				pwInfoList.addAll(service.select(pwInfo));
			}
			tunnelIdAndPwInfoListMap = getTunnelIdAndPwInfoListMap(pwInfoList);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
			pwInfo = null;
		}
		return pwInfoList;
	}

	/**
	 * tunnel与承载在tunnel上没有被其他业务所用的pw列表映射
	 * 
	 * @param pwInfoList
	 * @return
	 */
	public Map<Integer, List<PwInfo>> getTunnelIdAndPwInfoListMap(List<PwInfo> pwInfoList) {
		if (pwInfoList == null) {
			return null;
		}
		Map<Integer, List<PwInfo>> tunnelIdAndPwListMap = new HashMap<Integer, List<PwInfo>>();
		for (PwInfo pwinfo : pwInfoList) {
			Integer tunnelId = pwinfo.getTunnelId();
			List<PwInfo> pwList = null;
			if (tunnelIdAndPwListMap.get(tunnelId) == null) {
				pwList = new ArrayList<PwInfo>();
				for (PwInfo pw : pwInfoList) {
					if (tunnelId == pw.getTunnelId()) {
						pwList.add(pw);
					}
				}
			}
			tunnelIdAndPwListMap.put(tunnelId, pwList);

		}
		return tunnelIdAndPwListMap;
	}

	/**
	 * 获取ElanInfo
	 * 
	 * @param elanId
	 * @throws Exception
	 */
	private List<ElanInfo> getElanInfo(int elanId) throws Exception {
		ElanInfoService_MB elanInfoservice = null;
		List<ElanInfo> elanInfoList = null;
		try {
			elanInfoservice = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
			elanInfoList = elanInfoservice.select(elanId);
		} catch (Exception e) {
			throw e;
		}finally{
			UiUtil.closeService_MB(elanInfoservice);
		}
		return elanInfoList;
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JLabel activateStatusLabel;
	private javax.swing.JButton jButton;
	private javax.swing.JCheckBox isActivateCBox;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private PtnButton okBtn;
	private javax.swing.JLabel portLabel;
	// End of variables declaration//GEN-END:variables
	private javax.swing.JLabel pwlist;
	private JLabel lblMessage;
	private JLabel client;
	private JComboBox clientComboBox;

	public ViewDataTable<PwInfo> getPwInfoTable() {
		return pwInfoTable;
	}

	public void setPwInfoTable(ViewDataTable<PwInfo> pwInfoTable) {
		this.pwInfoTable = pwInfoTable;
	}

	public ViewDataTable<AcPortInfo> getBranchAcTable() {
		return branchAcTable;
	}

	public void setBranchAcTable(ViewDataTable<AcPortInfo> branchAcTable) {
		this.branchAcTable = branchAcTable;
	}

	public List<Node> getSelectNodeList() {
		return selectNodeList;
	}

	public void setSelectNodeList(List<Node> selectNodeList) {
		this.selectNodeList = selectNodeList;
	}

}
