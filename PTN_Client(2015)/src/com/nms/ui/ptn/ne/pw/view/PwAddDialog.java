﻿package com.nms.ui.ptn.ne.pw.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.nms.db.bean.alarm.CurrentAlarmInfo;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.bean.ptn.oam.OamMepInfo;
import com.nms.db.bean.ptn.path.protect.DualProtect;
import com.nms.db.bean.ptn.path.pw.MsPwInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.bean.ptn.qos.QosInfo;
import com.nms.db.bean.system.code.Code;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EManufacturer;
import com.nms.db.enums.EObjectType;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EPwType;
import com.nms.db.enums.EQosDirection;
import com.nms.db.enums.EServiceType;
import com.nms.model.alarm.CurAlarmService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.ptn.LabelInfoService_MB;
import com.nms.model.ptn.oam.OamInfoService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.path.tunnel.LspInfoService_MB;
import com.nms.model.ptn.path.tunnel.TunnelService_MB;
import com.nms.model.ptn.port.DualProtectService_MB;
import com.nms.model.ptn.qos.QosInfoService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.AutoNamingUtil;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.CustomException;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ListingFilter;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.VerifyNameUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.control.PtnDialog;
import com.nms.ui.manager.control.PtnSpinner;
import com.nms.ui.manager.control.PtnTextField;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.manager.keys.StringKeysTitle;
import com.nms.ui.ptn.business.dialog.pwpath.MappingConfigDialog;
import com.nms.ui.ptn.systemconfig.dialog.qos.controller.QosConfigController;

/**
 * 
 * @author kangkai
 * 
 */
public class PwAddDialog extends PtnDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -414570193884203274L;
	private PwInfo pwInfo;
	private PwPanel pwPanel;
	

	private List<QosInfo> qosList = new ArrayList<QosInfo>();
	public EPwType choosePwType;
	private int inLabel = 0;
	private int outLabel = 0;
	private JPanel buttonPanel;
	private String pwServiceType = "";
//	private static PwAddDialog pwAddDialog;

	public PwAddDialog(PwInfo pwInfo, PwPanel pwPanel) {

		try {
			//this.pwAddDialog = this;
			this.pwInfo = pwInfo;
			this.pwPanel = pwPanel;
			this.initComponent();
			this.setLayout();
			this.comboBoxData();
			this.different();
			this.initData();
			this.addListener();
			UiUtil.showWindow(this, 480, 520);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	/****************无参构造器****************************************/
	public PwAddDialog(){}
	
	/**
	 * 处理武汉晨晓不相同部�?	 * 
	 * @throws Exception
	 */
	private void different() throws Exception {
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(ConstantUtil.siteId) == EManufacturer.CHENXIAO.getValue()) {
				this.lblMapping.setVisible(false);
				this.btnMapping.setVisible(false);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(siteService);
		}
	}

//	public static PwAddDialog getPwAddDialog() {
//		if (null == pwAddDialog) {
//			pwAddDialog = new PwAddDialog(null, null);
//		}
//		return pwAddDialog;
//	}

	private void initData() throws Exception {
		if (null == this.pwInfo) {
			this.pwInfo = new PwInfo();
			this.setTitle(ResourceUtil.srcStr(StringKeysTitle.TIT_CREATE_PW));
		} else {
			this.ptnSpinnerNumber.setEnabled(false);
			QosInfoService_MB qosInfoService = null;
			try {
				qosInfoService=(QosInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosInfo);
				this.setTitle(ResourceUtil.srcStr(StringKeysTitle.TIT_UPDATE_PW));
				this.setQosList(this.pwInfo.getQosList());
				this.txtName.setText(this.pwInfo.getPwName());
				super.getComboBoxDataUtil().comboBoxSelectByValue(this.cmbType, this.pwInfo.getType().getValue() + "");
				super.getComboBoxDataUtil().comboBoxSelect(this.cmbTunnel, this.pwInfo.getTunnelId() + "");
				txtQos.setText(qosInfoService.qosCirSum(this.pwInfo.getQosList()));
				this.chbActivate.setSelected(this.pwInfo.getPwStatus() == 1 ? true : false);
				super.getComboBoxDataUtil().comboBoxSelectByValue(modelJComboBox, this.pwInfo.getQosModel()+"");
//				this.updatePwNni(pwInfo.getPwId());
				
				this.txtInlabel.setEditable(true);
				this.txtOutlabel.setEditable(true);
				if (this.pwInfo.getASiteId() == ConstantUtil.siteId) {
//					UiUtil.comboBoxSelect(this.cmbPwport, this.pwInfo.getaPortConfigId() + "");
					this.txtOppositeId.setText(this.pwInfo.getAoppositeId());
					// UiUtil.comboBoxSelect(this.cmbSite, this.pwInfo.getZSiteId() + "");
					this.txtInlabel.setText(this.pwInfo.getInlabelValue() + "");
					this.txtOutlabel.setText(this.pwInfo.getOutlabelValue() + "");
					inLabel = this.pwInfo.getInlabelValue();
					outLabel = this.pwInfo.getOutlabelValue();
					
					vlanValuePtnTextField.setText(pwInfo.getaOutVlanValue()+"");
					super.getComboBoxDataUtil().comboBoxSelectByValue(this.vlanEnableComboBox, this.pwInfo.getaVlanEnable() + "");
					super.getComboBoxDataUtil().comboBoxSelectByValue(this.tp_idJComboBox, this.pwInfo.getAtp_id() + "");
					sourceMacField.setText(this.pwInfo.getaSourceMac());
					targetMacField.setText(this.pwInfo.getAtargetMac());
					
				} else if(this.pwInfo.getZSiteId() == ConstantUtil.siteId){
//					UiUtil.comboBoxSelect(this.cmbPwport, this.pwInfo.getzPortConfigId() + "");
					this.txtOppositeId.setText(this.pwInfo.getZoppositeId());
					// UiUtil.comboBoxSelect(this.cmbSite, this.pwInfo.getASiteId() + "");
					this.txtInlabel.setText(this.pwInfo.getOutlabelValue() + "");
					this.txtOutlabel.setText(this.pwInfo.getInlabelValue() + "");
					inLabel = this.pwInfo.getOutlabelValue();
					outLabel = this.pwInfo.getInlabelValue();
					
					vlanValuePtnTextField.setText(pwInfo.getzOutVlanValue()+"");
					super.getComboBoxDataUtil().comboBoxSelectByValue(this.vlanEnableComboBox, this.pwInfo.getzVlanEnable() + "");
					super.getComboBoxDataUtil().comboBoxSelectByValue(this.tp_idJComboBox, this.pwInfo.getZtp_id() + "");
					sourceMacField.setText(this.pwInfo.getzSourceMac());
					targetMacField.setText(this.pwInfo.getZtargetMac());
				}
				else
				/****************多段赋�?*******************/
				{
					
					managerButoon.setEnabled(true);
					//入标�?					
					txtInlabel.setEditable(false);
					txtInlabel.setText("17");
					//出标�?					
					txtOutlabel.setEditable(false);
					txtOutlabel.setText("17");
					//可选择tunnel
					cmbTunnel.setEnabled(false);
					managerButoon.setEnabled(true);
					super.getComboBoxDataUtil().comboBoxSelectByValue(pwTypeComboBox, this.pwInfo.getBusinessType());
					modelJComboBox.setEnabled(false);
				}
				pwTypeComboBox.setEnabled(false);
				this.cmbType.setEnabled(false);
				this.cmbTunnel.setEnabled(false);
				this.btnQos.setEnabled(false);
//				this.cmbPwport.setEnabled(false);
				this.txtOppositeId.setEnabled(false);
				this.btnMapping.setEnabled(false);
				super.getComboBoxDataUtil().comboBoxSelect(payloadCombo, this.pwInfo.getPayload()+ "");
				this.payloadCombo.setEnabled(false);
			} catch (Exception e) {
				throw e;
			} finally {
				UiUtil.closeService_MB(qosInfoService);
			}
		}
	}

	
	/**
	 * 添加监听
	 */
	private void addListener() {
		this.btnCanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PwAddDialog.this.dispose();
			}
		});

		this.btnQos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				QosInfoService_MB qosInfoService = null;
				try {
					qosInfoService=(QosInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosInfo);
					ControlKeyValue controlKeyValue = (ControlKeyValue) cmbType.getSelectedItem();
					Code code = (Code) controlKeyValue.getObject();
					setChoosePwType(EPwType.forms(Integer.parseInt(code.getCodeValue())));

					Code payload = (Code) ((ControlKeyValue) payloadCombo.getSelectedItem()).getObject();
					pwInfo.setPayload(payload.getId());
					QosConfigController qoscontroller = new QosConfigController();
					qoscontroller.setNetwork(false);
					qoscontroller.openQosConfig(qoscontroller, "PW", pwInfo,EPwType.forms(Integer.parseInt(code.getCodeValue())),PwAddDialog.this);
					txtQos.setText(qosInfoService.qosCirSum(getQosList()));
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				} finally {
					UiUtil.closeService_MB(qosInfoService);
				}
			}
		});

		this.btnSave.addActionListener(new MyActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					savePwinfo();
				} catch (Exception e1) {
					ExceptionManage.dispose(e1,this.getClass());
				}
			}

			@Override
			public boolean checking() {
				
				return true;
			}
		});
		
		this.btnMapping.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					phbMapping();
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
				
			}
		});
		autoNamingBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				autoNamingActionPerformed();
			}
		});
		this.cmbType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TypeActionPerformed();
			}
		});
		
		/***************业务类型 下拉列表事件***************************/
		this.pwTypeComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pwTypeActionPerformed();
			}
		});
		
		/***************多段配置属性界�?**************************/
		managerButoon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					new PWMultiViewDialog(pwInfo);
			}
		});
		
	}
	
	 /**
	   * 
	   ***业务类型 下拉列表事件
	   */
		private void pwTypeActionPerformed() 
		{
			Code codePwServiceType = null;
			try 
			{
				codePwServiceType = (Code) ((ControlKeyValue)this.pwTypeComboBox.getSelectedItem()).getObject();
				if(codePwServiceType.getCodeValue().equals("0"))
				{
					managerButoon.setEnabled(false);
					//入标�?					
					txtInlabel.setEditable(true);
					//出标�?					
					txtOutlabel.setEditable(true);
					//可选择tunnel
					cmbTunnel.setEnabled(true);
					txtQos.setEnabled(true);
				    btnQos.setEnabled(true);
				    cmbType.setEnabled(true);
				    btnMapping.setEnabled(true); 	
				    modelJComboBox.setEnabled(true);
				    txtOppositeId.setEnabled(true);
				    txtInlabel.setText("");
				    txtOutlabel.setText("");
				}else
				{
					if(pwInfo == null)
					{
						pwInfo = new PwInfo();
					}
					
					managerButoon.setEnabled(true);
					//入标�?					
					txtInlabel.setEditable(false);
					txtInlabel.setText("17");
					//出标�?					
					txtOutlabel.setEditable(false);
					txtOutlabel.setText("17");
					//可选择tunnel
					cmbTunnel.setEnabled(false);
					txtQos.setEnabled(false);
				    btnQos.setEnabled(false);
				    cmbType.setEnabled(false);
				    btnMapping.setEnabled(false);
				    modelJComboBox.setEnabled(false);
				    txtOppositeId.setEnabled(false);
				}
			} catch (Exception e) 
			{
				ExceptionManage.dispose(e, getClass());
			}finally
			{
				codePwServiceType = null;
			}
			
		}
		
		
	private void TypeActionPerformed() {
		Code codePwType;
		try {
			codePwType = (Code) ((ControlKeyValue) this.cmbType.getSelectedItem()).getObject();
			if("1".equals(codePwType.getCodeValue())){
				payloadCombo.setEnabled(false);
			}else{
				payloadCombo.setEnabled(true);
			}
			this.initTunnelComboBox(this.cmbTunnel);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	/**
	 * 自动命名
	 */
	private void autoNamingActionPerformed() {
		PwInfo pwInfo ;
		String autoNaming;
		Code pwType;
		try {
			pwType = (Code) ((ControlKeyValue) this.cmbType.getSelectedItem()).getObject();
			pwInfo= new PwInfo();
			pwInfo.setIsSingle(1);
			pwInfo.setASiteId(ConstantUtil.siteId);
			pwInfo.setType(EPwType.forms(Integer.parseInt(pwType.getCodeValue())));
			AutoNamingUtil autoNamingUtil=new AutoNamingUtil();
			autoNaming = (String) autoNamingUtil.autoNaming(pwInfo, null, null);
			txtName.setText(autoNaming);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	/**
	 * 映射表配置管�?	 * @throws Exception
	 */
	private void phbMapping() throws Exception {
		new MappingConfigDialog(pwInfo);
	}

	/**
	 * 绑定下拉列表数据
	 * 
	 * @throws Exception
	 */
	private void comboBoxData() throws Exception {
		try {
			super.getComboBoxDataUtil().comboBoxData(this.cmbType, "PWTYPESITE");
			super.getComboBoxDataUtil().comboBoxData(this.payloadCombo, "PAYLOAD");
			super.getComboBoxDataUtil().comboBoxSelect(this.payloadCombo, "479");
			super.getComboBoxDataUtil().comboBoxData(this.vlanEnableComboBox, "ENABLEDSTATUEOAM");
			// this.intalRemoteSiteCombox(this.cmbSite);
			this.initTunnelComboBox(this.cmbTunnel);
			super.getComboBoxDataUtil().comboBoxData(this.tp_idJComboBox, "TP_ID");
//			this.initPwportComboBox();
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * 初始化控�?	 * @throws Exception 
	 */
	private void initComponent() throws Exception {
		this.lblMessage=new JLabel();
		buttonPanel=new javax.swing.JPanel();
		this.btnSave = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SAVE),true);
		this.btnCanel = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_CANEL));
		this.lblName = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_NAME));
		this.txtName = new PtnTextField(true,PtnTextField.STRING_MAXLENGTH,this.lblMessage,this.btnSave,this);
		this.lblType = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_TYPE));
		this.cmbType = new JComboBox();
	/********2015-3-2 张坤 增加单站测pw类型(普�?多段)*****************************************/	
		pwType = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SERVICENAME_TYPE));
		pwTypeComboBox = new JComboBox();
		super.getComboBoxDataUtil().comboBoxData(this.pwTypeComboBox, "BUSINESSTYPE");
		
		doubleManagerLabel = new JLabel(ResourceUtil.srcStr(StringKeysTip.SING1_MULTE_MANAGER));
		managerButoon = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_CONFIG));
		managerButoon.setEnabled(false);
		
		this.lblTunnel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_LOAD_TUNNEL));
		this.cmbTunnel = new JComboBox();
//		this.lblPwport = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_LOAD_PORT));
//		this.cmbPwport = new JComboBox();
		this.lblInlabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_INLABEL));
		this.txtInlabel = new PtnTextField(true,PtnTextField.TYPE_INT,PtnTextField.INT_MAXLENGTH, this.lblMessage, this.btnSave,this);
		this.txtInlabel.setCheckingMaxValue(true);
		this.txtInlabel.setCheckingMinValue(true);
		this.txtInlabel.setMaxValue(ConstantUtil.LABEL_MAXVALUE);
		this.txtInlabel.setMinValue(ConstantUtil.LABEL_MINVALUE);
		this.lblOutlabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_OUTLABEL));
		this.txtOutlabel = new PtnTextField(true,PtnTextField.TYPE_INT,PtnTextField.INT_MAXLENGTH, this.lblMessage, this.btnSave,this);
		this.txtOutlabel.setCheckingMaxValue(true);
		this.txtOutlabel.setCheckingMinValue(true);
		this.txtOutlabel.setMaxValue(ConstantUtil.LABEL_MAXVALUE2);
		this.txtOutlabel.setMinValue(ConstantUtil.LABEL_MINVALUE);
		this.lblSite = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_OPPOSITE_SITE));
		this.lblQos = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_QOS));
		this.txtQos = new PtnTextField();
		this.txtQos.setEditable(false);
		this.btnQos = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_CONFIG));
		// this.cmbSite = new JComboBox();
		this.lblActivate = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_ACTIVITY_STATUS));
		this.chbActivate = new JCheckBox();
		this.txtOppositeId = new PtnTextField(false, PtnTextField.TYPE_IP, PtnTextField.IP_MAXLENGTH, this.lblMessage, this.btnSave, this);
		this.txtOppositeId.setText("0.0.0.0");
		//新增映射表管�?		
		this.lblMapping = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_MAPPING_MANAGE));
		this.btnMapping = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_CONFIG));
		this.autoNamingBtn = new JButton(ResourceUtil.srcStr(StringKeysLbl.LBL_AUTO_NAME));
		this.payloadTxt = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_PAYLOAD));
		this.payloadCombo = new JComboBox();
		payloadCombo.setEnabled(false);
		sourceMac = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SOURCE_MAC));
		sourceMacField = new PtnTextField(false, PtnTextField.TYPE_MAC, PtnTextField.MAC_MAXLENGTH, this.lblMessage, this.btnSave, this);
		sourceMacField.setText("00-00-00-33-44-55");
		targetMac = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_PURPOSE_MAC));
		targetMacField = new PtnTextField(false, PtnTextField.TYPE_MAC, PtnTextField.MAC_MAXLENGTH, this.lblMessage, this.btnSave, this);
		targetMacField.setText("00-00-00-AA-BB-CC");
		vlanEnable = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_VLAN_ENABLE));
		vlanEnableComboBox = new JComboBox();
		vlanValue = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_OUT_VLAN_VALUE));
		vlanValuePtnTextField = new PtnTextField(true,PtnTextField.TYPE_INT,PtnTextField.INT_MAXLENGTH, this.lblMessage, this.btnSave,this);
		this.vlanValuePtnTextField.setCheckingMaxValue(true);
		this.vlanValuePtnTextField.setCheckingMinValue(true);
		this.vlanValuePtnTextField.setMaxValue(4095);
		this.vlanValuePtnTextField.setMinValue(2);
		this.vlanValuePtnTextField.setText(2+"");
		tp_idJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.TP_ID));
		tp_idJComboBox = new JComboBox();
		modelJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_MODAL));
		modelJComboBox = new JComboBox();
		super.getComboBoxDataUtil().comboBoxData(this.modelJComboBox, "MODEL");
		this.lblNumber = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_CREATE_NUM));
		this.ptnSpinnerNumber = new PtnSpinner(1, 1, 1000, 1);
	}
	private void setButtonLayout() {
		GridBagLayout componentLayout = new GridBagLayout();
		componentLayout.columnWidths = new int[] { 140,30,30 };
		componentLayout.columnWeights = new double[] { 0.1,0, 0};
		componentLayout.rowHeights = new int[] {  30 };
		componentLayout.rowWeights = new double[] { 0 };
		this.setLayout(componentLayout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		// 第一�?名称
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		componentLayout.setConstraints(this.btnSave, c);
		buttonPanel.add(this.btnSave);
		c.gridx=2;
		componentLayout.setConstraints(this.btnCanel, c);
		buttonPanel.add(this.btnCanel);
		
	}
	/**
	 * 设置布局
	 */
	private void setLayout() {
		this.setButtonLayout();
		GridBagLayout componentLayout = new GridBagLayout();
		componentLayout.columnWidths = new int[] { 70, 270, 80 };
		componentLayout.columnWeights = new double[] { 0, 0, 0 };
		componentLayout.rowHeights = new int[] { 25, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,30 };
		componentLayout.rowWeights = new double[] { 0.1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		this.setLayout(componentLayout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		int i = 0;
		// 第一�?错误提示
		c.gridx = 0;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.insets = new Insets(5, 5, 5, 5);
		componentLayout.setConstraints(this.lblMessage, c);
		this.add(this.lblMessage);
		
		// 第一�?名称
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 10);
		componentLayout.setConstraints(this.lblName, c);
		this.add(this.lblName);
		c.gridx = 1;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.txtName, c);
		this.add(this.txtName);
		
		c.gridx = 2;
		c.gridy = i++;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		componentLayout.setConstraints(this.autoNamingBtn, c);
		this.add(this.autoNamingBtn);
		

		// 第二�?类型
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblType, c);
		this.add(this.lblType);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.cmbType, c);
		this.add(this.cmbType);

		
		// 第三�?业务类型
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.pwType, c);
		this.add(this.pwType);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.pwTypeComboBox, c);
		this.add(this.pwTypeComboBox);
		
		
		// 第三�? 负载净�?		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.payloadTxt, c);
		this.add(this.payloadTxt);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.payloadCombo, c);
		this.add(this.payloadCombo);

		// �?�?承载tunnel
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblTunnel, c);
		this.add(this.lblTunnel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.cmbTunnel, c);
		this.add(this.cmbTunnel);

//		// 第四�?端口配置
//		c.gridx = 0;
//		c.gridy = 4;
//		c.gridwidth = 1;
//		componentLayout.setConstraints(this.lblPwport, c);
//		this.add(this.lblPwport);
//		c.gridx = 1;
//		c.gridwidth = 2;
//		componentLayout.setConstraints(this.cmbPwport, c);
//		this.add(this.cmbPwport);

		// 第五�?入标�?		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblInlabel, c);
		this.add(this.lblInlabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.txtInlabel, c);
		this.add(this.txtInlabel);

		// 第六�?出标�?		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblOutlabel, c);
		this.add(this.lblOutlabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.txtOutlabel, c);
		this.add(this.txtOutlabel);
		
		//源mac
//		c.gridx = 0;
//		c.gridy = i;
//		c.gridwidth = 1;
//		componentLayout.setConstraints(this.sourceMac, c);
//		this.add(this.sourceMac);
//		c.gridx = 1;
//		c.gridy = i++;
//		c.gridwidth = 2;
//		componentLayout.setConstraints(this.sourceMacField, c);
//		this.add(this.sourceMacField);
		
		//外层tp_id
//		c.gridx = 0;
//		c.gridy = i;
//		c.gridwidth = 1;
//		componentLayout.setConstraints(this.tp_idJLabel, c);
//		this.add(this.tp_idJLabel);
//		c.gridx = 1;
//		c.gridy = i++;
//		c.gridwidth = 2;
//		componentLayout.setConstraints(this.tp_idJComboBox, c);
//		this.add(this.tp_idJComboBox);
		
		//外层vlan使能
//		c.gridx = 0;
//		c.gridy = i;
//		c.gridwidth = 1;
//		componentLayout.setConstraints(this.vlanEnable, c);
//		this.add(this.vlanEnable);
//		c.gridx = 1;
//		c.gridy = i++;
//		c.gridwidth = 2;
//		componentLayout.setConstraints(this.vlanEnableComboBox, c);
//		this.add(this.vlanEnableComboBox);
		
		//外层vlan�?
		//c.gridx = 0;
//		c.gridy = i;
//		c.gridwidth = 1;
//		componentLayout.setConstraints(this.vlanValue, c);
//		this.add(this.vlanValue);
//		c.gridx = 1;
//		c.gridy = i++;
//		c.gridwidth = 2;
//		componentLayout.setConstraints(this.vlanValuePtnTextField, c);
//		this.add(this.vlanValuePtnTextField);

		// 第七�?对端网元
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblSite, c);
		this.add(this.lblSite);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 2;
		componentLayout.setConstraints(this.txtOppositeId, c);
		this.add(this.txtOppositeId);

		// 第八�?qos
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblQos, c);
		this.add(this.lblQos);
		c.gridx = 1;
		c.gridy = i;
		componentLayout.setConstraints(this.txtQos, c);
		this.add(this.txtQos);
		c.gridx = 2;
		c.gridy = i++;
		componentLayout.setConstraints(this.btnQos, c);
		this.add(this.btnQos);
		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.modelJLabel, c);
		this.add(this.modelJLabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.modelJComboBox, c);
		this.add(this.modelJComboBox);
		
		
		// �?0�?激活状�?		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblActivate, c);
		this.add(this.lblActivate);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.chbActivate, c);
		this.add(this.chbActivate);
		
		//插入一�?映射表管�?		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblMapping, c);
		this.add(this.lblMapping);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 5, 5, 5);
		componentLayout.setConstraints(this.btnMapping, c);
		this.add(this.btnMapping);
		
		//插入一�?映射表管�?		
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.doubleManagerLabel, c);
		this.add(this.doubleManagerLabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 5, 5, 5);
		componentLayout.setConstraints(this.managerButoon, c);
		this.add(this.managerButoon);
		
		// 批量创建
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 1;
		componentLayout.setConstraints(this.lblNumber, c);
		this.add(this.lblNumber);
		c.gridx = 1;
		c.gridy = i++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 5, 5, 5);
		componentLayout.setConstraints(this.ptnSpinnerNumber, c);
		this.add(this.ptnSpinnerNumber);
		
		// �?2�?按钮
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
//		c.gridx = 1;
		c.gridy = i;
		c.insets = new Insets(5, 5, 5, 5);
//		componentLayout.setConstraints(this.btnSave, c);
//		this.add(this.btnSave);
		c.gridx = 0;
		c.gridy = i++;
		c.gridwidth=3;
		componentLayout.setConstraints(this.buttonPanel, c);
		this.add(this.buttonPanel);

	}
	
//	/**
//	 * 修改pw时，pwnni端口赋�?//	 */
//	private void updatePwNni(int pwId){
//		
//		PwNniInfo pwNniInfo = null;
//		PwNniBufferService pwNniService = null;
//		DefaultComboBoxModel portBoxModel = null;
//		List<PwNniInfo> pwnniInfoList = null;
//		cmbPwport.removeAllItems();
//		try {
//			pwNniService = (PwNniBufferService) ConstantUtil.serviceFactory.newService(Services.PwNniBuffer);
//			// 查询pwnni配置
//			pwNniInfo = new PwNniInfo();
//			pwNniInfo.setSiteId(ConstantUtil.siteId);
//			pwNniInfo.setPwId(pwId);
//			pwnniInfoList = pwNniService.select(pwNniInfo);
//			// 绑定port下拉列表数据
//			portBoxModel = new DefaultComboBoxModel();
//			portBoxModel.addElement(new ControlKeyValue("0", "", null));
//			for (PwNniInfo pwnniInfo : pwnniInfoList) {
//				portBoxModel.addElement(new ControlKeyValue(pwnniInfo.getId() + "", pwnniInfo.getName(), pwnniInfo));
//			}		
//			this.cmbPwport.setModel(portBoxModel);
//		} catch (Exception e) {
//			ExceptionManage.dispose(e,this.getClass());
//		}finally{
//			
//		}
//	}

	/**
	 * 绑定对端网元下拉列表
	 * 
	 * @param jComboBox
	 * @throws Exception
	 */
	private void intalRemoteSiteCombox(JComboBox jComboBox) throws Exception {
		DefaultComboBoxModel defaultComboBoxModel = (DefaultComboBoxModel) jComboBox.getModel();
		SiteService_MB siteService = null;
		List<SiteInst> siteInstList = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			siteInstList = siteService.select();
			for (SiteInst siteInst : siteInstList) {
				if (siteInst.getSite_Inst_Id() != ConstantUtil.siteId) {
					defaultComboBoxModel.addElement(new ControlKeyValue(siteInst.getSite_Inst_Id() + "", siteInst.getCellDescribe(), siteInst));
				}
			}
			jComboBox.setModel(defaultComboBoxModel);
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(siteService);
		}
	}

	/**
	 * 绑定tunnel下拉列表数据
	 * 
	 * @param jComboBox
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void initTunnelComboBox(JComboBox jComboBox) throws Exception {
		OamInfoService_MB oamInfoService = null;
		TunnelService_MB tunnelServiceMB = null;
		DualProtectService_MB dualProtectService = null;
		List<Tunnel> tunnelList = null;
		List<DualProtect> dualProtectList = null;
		DefaultComboBoxModel defaultComboBoxModel = null;
		Map<Integer, Tunnel> tunnelMap = new HashMap<Integer, Tunnel>();
		ListingFilter filter=null;
		Code codePwType;
		try {
			filter=new ListingFilter();
			codePwType = (Code) ((ControlKeyValue) this.cmbType.getSelectedItem()).getObject();
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			dualProtectService = (DualProtectService_MB) ConstantUtil.serviceFactory.newService_MB(Services.DUALPROTECTSERVICE);
			tunnelList = (List<Tunnel>) filter.filterList(tunnelServiceMB.selectNodesBySiteId(ConstantUtil.siteId));
			oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo);
			if(!"1".equals(codePwType.getCodeValue())){
				//过滤非eth条件�?的tunnel  ，因为双规保护占用的tunnel只能用于eth时的pw
				dualProtectList = dualProtectService.selectBySite(ConstantUtil.siteId);
				for(DualProtect dualProtect:dualProtectList){
					tunnelMap.put(dualProtect.getBreakoverTunnel().getTunnelId(), dualProtect.getBreakoverTunnel());
					if(null!=dualProtect.getRelevanceTunnelList()){
						for(Tunnel tunnel:dualProtect.getRelevanceTunnelList()){
							tunnelMap.put(tunnel.getTunnelId(), tunnel);
						}
					}
				}
			}
			defaultComboBoxModel = new DefaultComboBoxModel();
			for (Tunnel tunnel : tunnelList) {
				// 如果不是xc 就添加到下拉列表�?				
				if (tunnel.getASiteId() == ConstantUtil.siteId || tunnel.getZSiteId() == ConstantUtil.siteId) {
					//过滤非eth条件�?的tunnel  ，因为双规保护占用的tunnel只能用于eth时的pw;
					if(null==tunnelMap.get(tunnel.getTunnelId())){
						defaultComboBoxModel.addElement(new ControlKeyValue(tunnel.getTunnelId() + "", tunnel.getTunnelName(), tunnel));
					}
				}
			}
			jComboBox.setModel(defaultComboBoxModel);
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(oamInfoService);
			UiUtil.closeService_MB(dualProtectService);
			UiUtil.closeService_MB(tunnelServiceMB);
		}
	}

	
//	/**
//	 * 绑定pwport下拉列表数据
//	 * 
//	 * @throws Exception
//	 */
//	private void initPwportComboBox() throws Exception {
//		ControlKeyValue controlKeyValue = null;
//		Tunnel tunnel = null;
//		PwNniInfo pwNniInfo = null;
//		PwNniBufferService pwNniService = null;
//		List<PwNniInfo> pwnniInfoList = null;
//		DefaultComboBoxModel portBoxModel = null;
//		try {
//			cmbPwport.removeAllItems();
//			pwNniService = (PwNniBufferService) ConstantUtil.serviceFactory.newService(Services.PwNniBuffer);
//			portBoxModel = new DefaultComboBoxModel();
//			if(null!=this.cmbTunnel.getSelectedItem()){
//				controlKeyValue = (ControlKeyValue) this.cmbTunnel.getSelectedItem();
//				tunnel = (Tunnel) controlKeyValue.getObject();
//
//				// 查询pwnni配置
//				pwNniInfo = new PwNniInfo();
//				pwNniInfo.setSiteId(ConstantUtil.siteId);
//				if (tunnel.getASiteId() == ConstantUtil.siteId) {
//					pwNniInfo.setPortId(tunnel.getAPortId());
//				} else {
//					pwNniInfo.setPortId(tunnel.getZPortId());
//				}
//				pwnniInfoList = pwNniService.select(pwNniInfo);
//
//				// 绑定port下拉列表数据
//				portBoxModel.addElement(new ControlKeyValue("0", "", null));
//				for (PwNniInfo pwnniInfo : pwnniInfoList) {
//					if (pwnniInfo.getPwId() == 0)
//						portBoxModel.addElement(new ControlKeyValue(pwnniInfo.getId() + "", pwnniInfo.getName(), pwnniInfo));
//				}
//			}
//			this.cmbPwport.setModel(portBoxModel);
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			controlKeyValue = null;
//			tunnel = null;
//			pwNniInfo = null;
//			pwNniService = null;
//			pwnniInfoList = null;
//			portBoxModel = null;
//		}
//	}

	/**
	 * 保存pw信息
	 * 
	 * @throws Exception
	 */
	private void savePwinfo() throws Exception {
		OamInfoService_MB oamInfoService = null;
		Tunnel tunnel = null;
		Code codePwType = null;
		DispatchUtil pwDispatch = null;
		String result = null;
		String beforeName=null;
		Code payload = null;
		Code vlanEnableCode = null;
		PwInfoService_MB pwInfoService=null;
		Code tp_id = null;
		ControlKeyValue model = null;
		Code codeModel = null;
		try {
			Code codePwServiceType = (Code) ((ControlKeyValue)this.pwTypeComboBox.getSelectedItem()).getObject();
			pwServiceType = codePwServiceType.getCodeValue();
			pwInfo.setBusinessType(codePwServiceType.getCodeValue());
			
			if(!pwServiceType.equals("0"))
			{
				if(pwInfo.getMsPwInfos()== null || pwInfo.getMsPwInfos().size() == 0){
					DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.SING1_MULTE_MSPWLABELError));
					return;
				}
			}
			
			if (!this.isFull()) {
				return;
			}
			//验证名称是否存在
			if(this.pwInfo.getPwId()!=0){
				beforeName = this.pwInfo.getPwName();
			}
			VerifyNameUtil verifyNameUtil=new VerifyNameUtil();
			if(verifyNameUtil.verifyNameBySingle(EServiceType.PW.getValue(), this.txtName.getText().trim(), beforeName,ConstantUtil.siteId)){
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_NAME_EXIST));
				return;
			}
			if(pwServiceType.equals("0")){
				/************为普通PW判断*************/
			String usableLabel = this.checkLabelUsable();
				if (usableLabel.length() > 0) {
					DialogBoxUtil.errorDialog(this, usableLabel + ResourceUtil.srcStr(StringKeysTip.TIP_LABEL_OCCUPY));
					return;
				}
				tunnel = (Tunnel) ((ControlKeyValue) this.cmbTunnel.getSelectedItem()).getObject();
				//验证改tunnel上是否有lck告警
				if(this.pwInfo.getPwId() == 0){
					oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo);
					OamMepInfo oamMepInfo = new OamMepInfo();
					oamMepInfo.setObjId(tunnel.getTunnelId());
					oamMepInfo.setObjType("TUNNEL_TEST");
					oamMepInfo.setLck(true);
					if((oamInfoService.selectByOamMepInfo(oamMepInfo).size()>0)){
						DialogBoxUtil.errorDialog(this, tunnel.getTunnelName()+ResourceUtil.srcStr(StringKeysTip.TIP_TUNNEL_LCK));
						return ;
					}
				}
			}
			payload = (Code) ((ControlKeyValue) this.payloadCombo.getSelectedItem()).getObject();
			codePwType = (Code) ((ControlKeyValue) this.cmbType.getSelectedItem()).getObject();
			vlanEnableCode = (Code) ((ControlKeyValue) this.vlanEnableComboBox.getSelectedItem()).getObject();
			tp_id = (Code) ((ControlKeyValue) this.tp_idJComboBox.getSelectedItem()).getObject();
			model = (ControlKeyValue) this.modelJComboBox.getSelectedItem();
			codeModel = (Code) model.getObject();
			this.pwInfo.setQosModel(Integer.parseInt(codeModel.getCodeValue()));
			this.pwInfo.setPwName(this.txtName.getText().trim());
			this.pwInfo.setPwStatus(this.chbActivate.isSelected() == true ? EActiveStatus.ACTIVITY.getValue() : EActiveStatus.UNACTIVITY.getValue());
			if(!(pwInfo.getPwId()>0)){
				if(pwServiceType.equals("0"))
				{
					this.pwInfo.setTunnelId(tunnel.getTunnelId());
					this.pwInfo.setTunnelName(tunnel.getTunnelName());
					this.pwInfo.setQosList(this.getQosList());
				}else
				{
					this.pwInfo.setTunnelId(0);
				}
				this.pwInfo.setType(EPwType.forms(Integer.parseInt(codePwType.getCodeValue())));
				this.pwInfo.setIsSingle(1);
				this.pwInfo.setCreateTime(DateUtil.getDate(DateUtil.FULLTIME));
				this.pwInfo.setCreateUser(ConstantUtil.user.getUser_Name());
				if(EPwType.forms(Integer.parseInt(codePwType.getCodeValue())).getValue()!=EPwType.ETH.getValue()){
					this.pwInfo.setPayload(payload.getId());
				}
			}
			this.pwInfo.setASiteId(0);
			this.pwInfo.setZSiteId(0);
			if(pwServiceType.equals("0"))
			{
				if (tunnel.getASiteId() == ConstantUtil.siteId) {
					this.pwInfo.setASiteId(tunnel.getASiteId());
					if(!(pwInfo.getPwId()>0)){
						if (this.txtOppositeId.getText().trim().length() > 0) {
							this.pwInfo.setAoppositeId(this.txtOppositeId.getText().trim());
						} else {
							this.pwInfo.setAoppositeId("0.0.0.0");
						}
						pwInfo.setZoppositeId("0.0.0.0");
					}
					
					// this.pwInfo.setZSiteId(siteId);
					this.pwInfo.setInlabelValue(Integer.parseInt(this.txtInlabel.getText().trim()));
					this.pwInfo.setOutlabelValue(Integer.parseInt(this.txtOutlabel.getText().trim()));
					this.pwInfo.setAtp_id(Integer.parseInt(tp_id.getCodeValue()));
					this.pwInfo.setaOutVlanValue(Integer.parseInt(vlanValuePtnTextField.getText()));
					this.pwInfo.setaVlanEnable(Integer.parseInt(vlanEnableCode.getCodeValue()));
					this.pwInfo.setaSourceMac(sourceMacField.getText());
					this.pwInfo.setAtargetMac(targetMacField.getText());
				} else {
					this.pwInfo.setZSiteId(tunnel.getZSiteId());
					if(!(pwInfo.getPwId()>0)){
						if (this.txtOppositeId.getText().trim().length() > 0) {
							this.pwInfo.setZoppositeId(this.txtOppositeId.getText().trim());
						} else {
							this.pwInfo.setZoppositeId("0.0.0.0");
						}
					}
					// this.pwInfo.setASiteId(siteId);
					this.pwInfo.setInlabelValue(Integer.parseInt(this.txtOutlabel.getText().trim()));
					this.pwInfo.setOutlabelValue(Integer.parseInt(this.txtInlabel.getText().trim()));
					this.pwInfo.setZtp_id(Integer.parseInt(tp_id.getCodeValue()));
					this.pwInfo.setzOutVlanValue(Integer.parseInt(vlanValuePtnTextField.getText()));
					this.pwInfo.setzVlanEnable(Integer.parseInt(vlanEnableCode.getCodeValue()));
					this.pwInfo.setzSourceMac(sourceMacField.getText());
					this.pwInfo.setZtargetMac(targetMacField.getText());
				}
				pwInfo.setMsPwInfos(null);
			}

			pwDispatch = new DispatchUtil(RmiKeys.RMI_PW);
			if(this.pwInfo.getPwId()==0 ){
				
				if(pwServiceType.equals("0"))
				{
					//新建�? 验证qos是否充足
					pwInfoService=(PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
					if(!pwInfoService.checkingQos(pwInfo, pwInfo.getQosList(),null)){
						if(Integer.parseInt(codePwType.getCodeValue()) == EPwType.PDH.getValue()) {
							DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_QOSISNOTENOUGH_E1));
						}else{
							DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_TUNNEL_QOS_ALARM));
						}
						return;
					}
				}
				List <PwInfo> pwList = new ArrayList<PwInfo>();
				pwList.add(this.pwInfo);
				// 批量创建
				int num = Integer.parseInt(this.ptnSpinnerNumber.getTxt().getText());
				if(num > 1){
					this.createPWOnCopy(pwList, num-1);
				}
				
				result = pwDispatch.excuteInsert(pwList);
				this.btnSave.setOperateKey(EOperationLogType.PWINSERT.getValue());
				this.getTunnelName(pwInfo);
				this.insertOpeLog(EOperationLogType.PWINSERT.getValue(), result, null, pwInfo);
				
			}else{
				PwInfo pwForLog = this.getpwInfoForLog();
				result = pwDispatch.excuteUpdate(this.pwInfo);
				this.btnSave.setOperateKey(EOperationLogType.PWUPDATE.getValue());
				this.insertOpeLog(EOperationLogType.PWUPDATE.getValue(), result, pwForLog, pwInfo);
			}
//			//添加日志记录
//			int operationResult=0;
//			if(result.contains(ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS))){
//				operationResult=1;
//			}else{
//				operationResult=2;
//			}
//			btnSave.setResult(operationResult);
			DialogBoxUtil.succeedDialog(this, result);
			this.pwPanel.getController().refresh();
			this.dispose();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally{
			UiUtil.closeService_MB(pwInfoService);
			UiUtil.closeService_MB(oamInfoService);
		}
	}
	
	private void createPWOnCopy(List<PwInfo> pwList, int num) {
		List<QosInfo> qosList = new ArrayList<QosInfo>();
		qosList.add(this.createQos(EQosDirection.FORWARD.getValue() + ""));
		qosList.add(this.createQos(EQosDirection.BACKWARD.getValue() + ""));
		PwInfo pwInfo = null;
		for(int i = 0; i < num; i++){
			pwInfo = new PwInfo();
			pwInfo.setBusinessType(this.pwInfo.getBusinessType());
			pwInfo.setQosModel(this.pwInfo.getQosModel());
			pwInfo.setPwName(this.pwInfo.getPwName()+"_copy"+(i+1));
			pwInfo.setPwStatus(this.pwInfo.getPwStatus());
			pwInfo.setASiteId(this.pwInfo.getASiteId());
			pwInfo.setZSiteId(this.pwInfo.getZSiteId());
			pwInfo.setAoppositeId(this.pwInfo.getAoppositeId());
			pwInfo.setZoppositeId(this.pwInfo.getZoppositeId());
			pwInfo.setInlabelValue(0);
			pwInfo.setOutlabelValue(0);
			pwInfo.setAtp_id(this.pwInfo.getAtp_id());
			pwInfo.setaOutVlanValue(this.pwInfo.getaOutVlanValue());
			pwInfo.setaVlanEnable(this.pwInfo.getaVlanEnable());
			pwInfo.setzOutVlanValue(this.pwInfo.getzOutVlanValue());
			pwInfo.setzVlanEnable(this.pwInfo.getzVlanEnable());
			pwInfo.setaSourceMac(this.pwInfo.getaSourceMac());
			pwInfo.setAtargetMac(this.pwInfo.getAtargetMac());
			pwInfo.setzSourceMac(this.pwInfo.getzSourceMac());
			pwInfo.setZtargetMac(this.pwInfo.getZtargetMac());
			pwInfo.setType(this.pwInfo.getType());
			pwInfo.setIsSingle(1);
			pwInfo.setTunnelName(this.pwInfo.getTunnelName());
			pwInfo.setTunnelId(this.pwInfo.getTunnelId());
			pwInfo.setCreateTime(this.pwInfo.getCreateTime());
			pwInfo.setCreateUser(ConstantUtil.user.getUser_Name());
			pwInfo.setPayload(this.pwInfo.getPayload());
			pwInfo.setMsPwInfos(null);
			pwInfo.setQosList(qosList);
			pwList.add(pwInfo);
		}
	}
	
	private QosInfo createQos(String direction) {
		QosInfo info = new QosInfo();
		info.setQosType(this.getQosList().get(0).getQosType());
		info.setCos(this.getQosList().get(0).getCos());
		info.setDirection(direction);
		info.setCir(0);
		info.setCbs(1);
		info.setEir(0);
		info.setEbs(1);
		info.setPir(0);
		return info;
	}
	
	private void insertOpeLog(int operationType, String result, PwInfo oldPw, PwInfo newPw){
		AddOperateLog.insertOperLog(btnSave, operationType, result, oldPw, newPw, ConstantUtil.siteId, newPw.getPwName(), "pwInfo");
	}
	
	private PwInfo getpwInfoForLog() {
		PwInfoService_MB pwService = null;
		PwInfo pw = null;
		try {
			pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			PwInfo condition = new PwInfo();
			condition.setPwId(this.pwInfo.getPwId());
			pw = pwService.selectBypwid_notjoin(condition);
			this.getTunnelName(pw);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(pwService);
		}
		return pw;
	}
	
	private void getTunnelName(PwInfo pw){
		SiteService_MB siteService = null;
		TunnelService_MB tunnelService = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			tunnelService = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			List<MsPwInfo> msPwInfoList = pw.getMsPwInfos();
			if(msPwInfoList != null && msPwInfoList.size() > 0){
				for (MsPwInfo msPwInfo : msPwInfoList) {
					msPwInfo.setSiteName(siteService.getSiteName(msPwInfo.getSiteId()));
					msPwInfo.setFrontTunnelName(tunnelService.getTunnelName(msPwInfo.getFrontTunnelId()));
					msPwInfo.setBackTunnelName(tunnelService.getTunnelName(msPwInfo.getBackTunnelId()));
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
			UiUtil.closeService_MB(tunnelService);
		}
	}
	
	/**
	 * 验证标签是否可用
	 * 
	 * @return
	 * @throws Exception
	 */
	private String checkLabelUsable() 
	{
		String result = null;
		List<Integer> labelValues = null;
		Tunnel tunnel = null;
		try {
			labelValues = this.getLabelValues(inLabel,this.txtInlabel.getText().trim());
			tunnel = (Tunnel) ((ControlKeyValue) this.cmbTunnel.getSelectedItem()).getObject();
			int label = Integer.parseInt(this.txtOutlabel.getText().trim());
			result = isLabelUsable(labelValues,tunnel,label,outLabel);
		} catch (Exception e) 
		{
			ExceptionManage.dispose(e, getClass());
		}finally{
			labelValues = null;
			tunnel = null;
		}
	return result;
 }

	/**
	 * 验证标签是否可用
	 * 
	 * @return
	 * @throws Exception
	 */
	public String isLabelUsable(List<Integer> labelValues ,Tunnel tunnel,int label,int outLabelValue) throws Exception {
		String result = null;
		LabelInfoService_MB labelInfoService = null;
//		List<Integer> labelValues = null;
		boolean b = true;
//		Tunnel	tunnel  = null;
		try {
			labelInfoService = (LabelInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.LABELINFO);
//			labelValues = this.getLabelValues();
//			for(Integer integer : labelValues){
//				if(integer<16 || integer>1048575){//验证标签范围
//					b = false;
//				}
//			}
//			if(b){
				result = labelInfoService.select(labelValues, ConstantUtil.siteId, "PW");
				/*目前设备的芯片不支持同一端口的lsp的入标签和该端口上的pw的入标签一�?所以要加上下面的代�?******************************/
				if(result == null || "".equals(result)){
					if(labelValues.size() > 0){
						if(!this.verifyInLabel(labelValues.get(0),tunnel)){
							result = labelValues.get(0)+",";
						}
					}
				}
				/*如果以后芯片支持同一端口的lsp的入标签和该端口上的pw的入标签一�?就把上面的代码关�?*************************/
				//出标签单独验�?//				tunnel = (Tunnel) ((ControlKeyValue) this.cmbTunnel.getSelectedItem()).getObject();
//				int label = Integer.parseInt(this.txtOutlabel.getText().trim());
				result += this.checkOutLabelUsable(tunnel,label,outLabelValue);
//			}
		} catch (CustomException e) {
			throw e;
		} catch (NumberFormatException e1) {
			throw e1;
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(labelInfoService);
		}
		
		if(result.length() > 0){
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	/**
	 * 验证pw的入标签,如果同一端口的lsp的入标签和该端口上的pw的入标签一�?返回false,如果不一�?返回true
	 */
	private boolean verifyInLabel(int inLabel,Tunnel tunnel) {
		LspInfoService_MB lspService = null;
		try {
			lspService = (LspInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.LSPINFO);
//			Tunnel tunnel = (Tunnel) ((ControlKeyValue) this.cmbTunnel.getSelectedItem()).getObject();
			if(tunnel.getaSiteId() == ConstantUtil.siteId){
				return lspService.verifyInLabel(tunnel.getaSiteId(), tunnel.getaPortId(), inLabel);
			}else if(tunnel.getzSiteId() == ConstantUtil.siteId){
				return lspService.verifyInLabel(tunnel.getzSiteId(), tunnel.getzPortId(), inLabel);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(lspService);
		}
		return true;
	}


	/**
	 * 验证出标签是否可�?	 * 同一个端口的出标签不能一�?	 * @throws Exception
	 */
	private String checkOutLabelUsable(Tunnel tunnel,int label,int outLabelValue) throws Exception  {
		PwInfoService_MB pwService = null;
		TunnelService_MB tunnelServiceMB = null;
//		Tunnel tunnel = null;
		int portId = 0;
		List<Integer> tunnelIdList = null;
		List<PwInfo> pwList = null;
		String result = "";
		try {
			 pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			 tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			if(label != outLabelValue){
				//同一个端口的出标签不能一�?//				tunnel = (Tunnel) ((ControlKeyValue) this.cmbTunnel.getSelectedItem()).getObject();
				if(tunnel.getASiteId() == ConstantUtil.siteId){
					portId = tunnel.getAPortId();
				}else{
					portId = tunnel.getZPortId();
				}
				tunnelIdList = tunnelServiceMB.checkPortUsable(portId);
				for (int tunnelId : tunnelIdList) {
					pwList = pwService.selectSamePortByTunnelId(tunnelId, ConstantUtil.siteId);
					if(pwList != null){
						for (PwInfo pwInfo : pwList) {
							int outLabel = 0;
							if(pwInfo.getASiteId() == ConstantUtil.siteId){
								outLabel = pwInfo.getInlabelValue();
							}else{
								outLabel = pwInfo.getOutlabelValue();
							}
							if(outLabel == label){
								result = label+",";
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			UiUtil.closeService_MB(pwService);
			UiUtil.closeService_MB(tunnelServiceMB);
		}
		return result;
	}
	

	/**
	 * 获取入标签集�?	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getLabelValues(int inLabel,String inLabelValue) throws Exception {
		List<Integer> labelvalues = null;
		try {
			labelvalues = new ArrayList<Integer>();
			//如过修改pw时，未修改此标签就不用验证，即可�?//			
			if(Integer.parseInt(this.txtInlabel.getText().trim()) != inLabel){
				if(Integer.parseInt(inLabelValue) != inLabel){
					this.setLabelValues(labelvalues, Integer.parseInt(inLabelValue));
				}
			}
		} catch (CustomException e) {
			throw e;
		} catch (NumberFormatException e) {
			throw new NumberFormatException(ResourceUtil.srcStr(StringKeysTip.TIP_LABEL_NUMBER));
		} catch (Exception e) {
			throw e;
		}
		return labelvalues;
	}

	private void setLabelValues(List<Integer> labelvalues, int labelValue) throws Exception {

		if (!labelvalues.contains(labelValue)) {
			labelvalues.add(labelValue);
		} else {
			throw new CustomException(ResourceUtil.srcStr(StringKeysTip.TIP_LABEL_REPEAT));
		}

	}

	/**
	 * 验证是否完整
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean isFull() throws Exception {
		boolean flag=true;
		try {
			/************为普通PW判断*************/
			if(pwServiceType.equals("0")){
				if(null == this.cmbTunnel.getSelectedItem()){
					DialogBoxUtil.succeedDialog(this,ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_TUNNEL));
					flag=false;
				} 
				if (this.txtQos.getText().trim().length() == 0 || this.qosList == null || this.qosList.size() == 0) {
					DialogBoxUtil.succeedDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_QOS_FILL));
					flag=false;
				}
			}
//				if(null==this.cmbPwport.getSelectedItem()){
//				DialogBoxUtil.succeedDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_MUSTNETWORK_BEFORE));
//				flag = false;
//			} else
			// if(null==this.cmbSite.getSelectedItem()){
			// DialogBoxUtil.succeedDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_SITE));
			// flag=false;
			// }else
		} catch (Exception e) {
			throw e;
		}
		return flag;
	}
	

	/**
	 * 武汉
	 * @param 验证是否能在该tunnel上创建pw
	 * @return
	 */
	public boolean tunnelUsed(Tunnel tunnel){
		TunnelService_MB tunnelServiceMB = null;
		CurAlarmService_MB curAlarmService = null;
		List<Integer> siteList = null;
		CurrentAlarmInfo currentAlarmInfo = null;
		List<CurrentAlarmInfo> alarmInfos = null;
		try {
			curAlarmService = (CurAlarmService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CurrentAlarm);
			tunnelServiceMB=(TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			siteList = tunnelServiceMB.getSiteIds(tunnel,false);
			if(siteList != null && siteList.size()>0){//选择的tunnel包含武汉的设�?				
				currentAlarmInfo = new CurrentAlarmInfo();
				currentAlarmInfo.setAlarmCode(183);
				currentAlarmInfo.setAlarmLevel(2);
				if(ConstantUtil.siteId == tunnel.getASiteId()){
					currentAlarmInfo.setObjectId(tunnel.getLspParticularList().get(0).getAtunnelbusinessid());
					currentAlarmInfo.setObjectType(EObjectType.TUNNEL);
					currentAlarmInfo.setSiteId(ConstantUtil.siteId);
					alarmInfos = curAlarmService.select(currentAlarmInfo);
					if(alarmInfos != null && alarmInfos.size()>0){//该tunnel存在TMP_LCK告警，则该tunnel不能再添加pw
						return false;
					}
					currentAlarmInfo.setObjectId(tunnel.getLspParticularList().get(tunnel.getLspParticularList().size()-1).getAtunnelbusinessid());
					currentAlarmInfo.setSiteId(ConstantUtil.siteId);
					alarmInfos = curAlarmService.select(currentAlarmInfo);
					if(alarmInfos != null && alarmInfos.size()>0){//该tunnel存在TMP_LCK告警，则该tunnel不能再添加pw
						return false;
					}
				}else{
					currentAlarmInfo.setObjectId(tunnel.getLspParticularList().get(0).getZtunnelbusinessid());
					currentAlarmInfo.setSiteId(ConstantUtil.siteId);
					alarmInfos = curAlarmService.select(currentAlarmInfo);
					if(alarmInfos != null && alarmInfos.size()>0){//该tunnel存在TMP_LCK告警，则该tunnel不能再添加pw
						return false;
					}
					currentAlarmInfo.setObjectId(tunnel.getLspParticularList().get(tunnel.getLspParticularList().size()-1).getZtunnelbusinessid());
					currentAlarmInfo.setSiteId(ConstantUtil.siteId);
					alarmInfos = curAlarmService.select(currentAlarmInfo);
					if(alarmInfos != null && alarmInfos.size()>0){//该tunnel存在TMP_LCK告警，则该tunnel不能再添加pw
						return false;
					}
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}finally{
			UiUtil.closeService_MB(tunnelServiceMB);
			UiUtil.closeService_MB(curAlarmService);
			siteList = null;
			currentAlarmInfo = null;
			alarmInfos = null;
		}
		return true;
	}
	
	public List<QosInfo> getQosList() {
		return qosList;
	}

	public void setQosList(List<QosInfo> qosList) {
		this.qosList = qosList;
	}

	public EPwType getChoosePwType() {
		return choosePwType;
	}

	public void setChoosePwType(EPwType choosePwType) {
		this.choosePwType = choosePwType;
	}
	public PwPanel getPwPanel() {
		return pwPanel;
	}

	public void setPwPanel(PwPanel pwPanel) {
		this.pwPanel = pwPanel;
	}
	private JLabel lblName;
	private JTextField txtName;
	private JLabel lblType;
	private JComboBox cmbType;
	/************PW类型 普通还是多段的leble标签*************************/
	private JLabel pwType;
	/************PW类型 普通还是多�?************************/
	private JComboBox pwTypeComboBox;
	private JLabel doubleManagerLabel;
	private JButton managerButoon;
	private JLabel lblTunnel;
	private JComboBox cmbTunnel;
//	private JLabel lblPwport;
//	private JComboBox cmbPwport;
	private JLabel lblInlabel;
	private PtnTextField txtInlabel;
	private JLabel lblOutlabel;
	private PtnTextField txtOutlabel;
	private JLabel lblSite;
	// private JComboBox cmbSite;
	private JLabel lblActivate;
	private JCheckBox chbActivate;
	private JLabel lblQos;
	private JTextField txtQos;
	private JButton btnQos;
	private PtnButton btnSave;
	private JButton btnCanel;
	private JLabel lblMessage;
	private PtnTextField txtOppositeId;
	private JLabel lblMapping;//新增单网元pw下映射表管理
	private JButton btnMapping;
	private JButton autoNamingBtn;
	private JLabel payloadTxt;
	private JComboBox payloadCombo;
	private JLabel sourceMac;
	private PtnTextField sourceMacField;
	private JLabel targetMac;
	private PtnTextField targetMacField;
	private JLabel vlanEnable;//外层vlan使能
	private JComboBox vlanEnableComboBox;
	private JLabel vlanValue;//外层vlan�?	
	private PtnTextField vlanValuePtnTextField;
	private JLabel tp_idJLabel;//TP_ID
	private JComboBox tp_idJComboBox;
	private JLabel modelJLabel;//模式
	private JComboBox modelJComboBox;
	private JLabel lblNumber;
	private PtnSpinner ptnSpinnerNumber;
}
