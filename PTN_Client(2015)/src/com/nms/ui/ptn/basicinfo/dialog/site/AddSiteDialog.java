﻿/*
 * AddSiteDialog.java
 *
 * Created on __DATE__, __TIME__
 */

package com.nms.ui.ptn.basicinfo.dialog.site;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.nms.db.bean.equipment.shelf.EquipInst;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.bean.equipment.slot.SlotInst;
import com.nms.db.bean.system.Field;
import com.nms.db.bean.system.code.Code;
import com.nms.db.enums.EOperationLogType;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.system.FieldService_MB;
import com.nms.model.system.SubnetService_MB;
import com.nms.model.util.CodeConfigItem;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.SiteUtil;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.Verification;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.control.PtnComboBox;
import com.nms.ui.manager.control.PtnDialog;
import com.nms.ui.manager.control.PtnTextField;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysMenu;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.manager.keys.StringKeysTitle;
import com.nms.ui.manager.util.EquimentDataUtil;
import com.nms.ui.manager.xmlbean.EquipmentType;
import com.nms.ui.ptn.alarm.view.AlarmColorChooseDialog;
import com.nms.ui.topology.NetworkElementPanel;

/**
 * 
 * @author __USER__
 */
public class AddSiteDialog extends PtnDialog {
	/**
	 * 
	 */
	public static final long serialVersionUID = 2952032508164366902L;
	protected SiteInst siteInst = null;
	public int fieldId;
	public boolean copySite = false;
	private int label = 0;//增加网元右菜单网元信息功能 
	public AddSiteDialog(String siteId) {
		try {
			copySite = true;
			initComponents();
			addListener();
			initDate(siteId);
			this.showWindow();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	public AddSiteDialog(boolean modal, String siteId) {
		try {
			super.setTitle(ResourceUtil.srcStr(StringKeysTitle.TIT_CREATE_SMALLSITE));
			if (null != siteId && !"".equals(siteId) && !"0".equals(siteId)) {
				super.setTitle(ResourceUtil.srcStr(StringKeysTitle.TIT_UPDATE_SITE));
				this.getSite(siteId);
			}
			this.setModal(modal);
			initComponents();
			setLayout();
			addListener();
			initDate(siteId);
			this.showWindow();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	
	public AddSiteDialog(boolean modal, String siteId,int label) {
		try {
			if (null != siteId && !"".equals(siteId) && !"0".equals(siteId)) {
				super.setTitle(ResourceUtil.srcStr(StringKeysMenu.MENU_SITEINFO));
				this.getSite(siteId);
			}
			this.label = label;
			this.setModal(modal);
			initComponents();
			setLayout();
			addListener();
			initDate(siteId);
			this.showWindow();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}

	/*
	 * 初始化数据
	 */
	public void initDate(String siteId) throws Exception {
		super.getComboBoxDataUtil().comboBoxData(jComboBox3, "TIMEZONE");
		super.getComboBoxDataUtil().comboBoxData(jComboBox4, "MANAGENESTATUS");
		super.getComboBoxDataUtil().comboBoxData(siteTypeCombo, "siteType");
		
		CodeConfigItem	codeConfigItem = CodeConfigItem.getInstance();
		if(codeConfigItem.getValueByKey("IconImageShowOrHide").equals("1")){
			// 去掉java图标
			super.getComboBoxDataUtil().comboBoxData(cmbSiteManufacturer, "EDITION");
		}else{
			super.getComboBoxDataUtil().comboBoxData(cmbSiteManufacturer, "ZTEEDITION");
		}
		this.equipmentTypeDate();
			if (siteInst != null) {
				super.getComboBoxDataUtil().comboBoxSelect(cmbSiteManufacturer, this.siteInst.getCellEditon());
				super.getComboBoxDataUtil().comboBoxSelect(cmbSiteType, this.siteInst.getCellType());
				super.getComboBoxDataUtil().comboBoxSelect(jComboBox3, this.siteInst.getCellTimeZone());
				super.getComboBoxDataUtil().comboBoxSelect(subnetCombo, this.siteInst.getFieldID()+"");
				super.getComboBoxDataUtil().comboBoxSelect(groupJComboBox, getFieldId(this.siteInst.getFieldID())+"");
				SiteUtil siteUtil = new SiteUtil();
				int flag=0;			
				if(this.siteInst.getSiteType()== 370){
					//虚拟网元不能修改
					super.getComboBoxDataUtil().comboBoxSelect(siteTypeCombo, this.siteInst.getSiteType()+"");
					siteTypeCombo.setEnabled(false);
				}else if(this.siteInst.getSiteType()== 369){
					flag = ((SiteUtil) siteUtil).SiteTypeOnlineUtil(siteInst.getSite_Inst_Id());
				    if(flag!=1){
				    	//在线网元没有托管不可用修改
					    super.getComboBoxDataUtil().comboBoxSelect(siteTypeCombo, this.siteInst.getSiteType()+"");
					    siteTypeCombo.setEnabled(false);
				   }else{
					   //在线网元托管可以修改为离线网元
					    super.getComboBoxDataUtil().comboBoxSelect(siteTypeCombo, this.siteInst.getSiteType()+"");
					    siteTypeCombo.removeItemAt(1);
					    siteTypeCombo.setEnabled(true);
				    }
				}else if(this.siteInst.getSiteType()==678){
					//离线网元可以修改为在线网元
					super.getComboBoxDataUtil().comboBoxSelect(siteTypeCombo, this.siteInst.getSiteType()+"");
				    siteTypeCombo.removeItemAt(1);
				    siteTypeCombo.setEnabled(true);    
				}
//				super.getComboBoxDataUtil().comboBoxSelect(siteTypeCombo, this.siteInst.getSiteType()+"");
				super.getComboBoxDataUtil().comboBoxSelect(jComboBox4, this.siteInst.getType() + "");

//				txtSiteIp.setEnabled(false);
				cmbSiteType.setEnabled(false);
				cmbSiteManufacturer.setEnabled(false);
				if(siteInst.getCellIcccode() != null && !"".equals(siteInst.getCellIcccode())){ 
					this.siteColorJButton.setBackground(new Color(Integer.parseInt(siteInst.getCellIcccode()))); 
				} 
				this.switchLable.setText(this.siteInst.getSwich());
				this.txtSiteName.setText(this.siteInst.getCellId());
				this.txtSiteIp.setText(this.siteInst.getCellDescribe());
				this.jTextField5.setText(this.siteInst.getCellIcccode());
				this.jTextField6.setText(this.siteInst.getCellTimeServer());
				this.jTextField7.setText(this.siteInst.getCellTPoam());
				this.jTextField9.setText(this.siteInst.getCellTime());
				this.txtSiteId_wh.setText(this.siteInst.getSite_Hum_Id());
				this.username.setText(this.siteInst.getUsername());
				this.userpwd.setText(this.siteInst.getUserpwd());
				this.txtSiteLocation.setText(this.siteInst.getSiteLocation());
				if (this.siteInst.getIsGateway() == 1) {
					this.chkIsGateway.setSelected(true);
				}
				this.rackPtnTextField.setText(this.siteInst.getRack()+"");
				this.shelfPtnTextField.setText(this.siteInst.getShelf()+"");
				
				if(label != 0)
				{
					jComboBox3.setEnabled(false);
					subnetCombo.setEnabled(false);
					groupJComboBox.setEnabled(false);
					siteTypeCombo.setEnabled(false);
					jComboBox4.setEnabled(false);
					switchLable.setEnabled(false);
					txtSiteName.setEnabled(false);
					txtSiteIp.setEnabled(false);
					jTextField5.setEnabled(false);
					jTextField6.setEnabled(false);
					jTextField7.setEnabled(false);
					jTextField9.setEnabled(false);
					txtSiteId_wh.setEnabled(false);
					username.setEnabled(false);
					userpwd.setEnabled(false);
					chkIsGateway.setEnabled(false);
					rackPtnTextField.setEnabled(false);
					shelfPtnTextField.setEnabled(false);
					txtSiteLocation.setEnabled(false);
					jButton1.setEnabled(false);
				}
			} else {
			this.siteInst = new SiteInst();
		}
	}
	
	private int getFieldId(int siteFieldId)
	{
		FieldService_MB fieldService = null;
		List<Field> fieldList = null;
		Field field = null;
		try {
			fieldService = (FieldService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Field);
			fieldList = fieldService.selectByFieldId(siteFieldId);
			if(null != fieldList && !fieldList.isEmpty())
			{
				field = fieldList.get(0);
				if(!field.getObjectType().equals("field"))
				{
					return field.getParentId();
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			UiUtil.closeService_MB(fieldService);
			fieldList = null;
			field = null;
		}
		return siteFieldId;
	}
	
	public void showWindow() {
		Dimension dimension = new Dimension(450, 655);
		this.setSize(dimension);
		this.setMinimumSize(dimension);
	}

	public void addListener() {

		jButton1.addActionListener(new MyActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				jButton1ActionPerformed(e);
				
			}
			
			@Override
			public boolean checking() {
				
				return true;
			}
		});

		jButton2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		this.cmbSiteManufacturer.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == 1) {
					try {
						equipmentTypeDate();
						setIdMust();
					} catch (Exception e) {
						ExceptionManage.dispose(e,this.getClass());
					}
				}
			}
		});
		
		groupJComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == 1)
				{
					ControlKeyValue controlKeyValue = (ControlKeyValue) groupJComboBox.getSelectedItem();
					if(controlKeyValue != null){
						Field f = (Field) controlKeyValue.getObject();
						setComboBox(f.getId(),(DefaultComboBoxModel) subnetCombo.getModel(),subnetCombo);
					}
				}
			}
		});
		siteColorJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AlarmColorChooseDialog colorDialog = new AlarmColorChooseDialog(); 
				Color color = colorDialog.getColor(); 
				if(color != null){ 
					siteColorJButton.setBackground(color); 
				} 
			}
		});
	}
	
	/**
	 * 设置网元ID必填
	 * 
	* @author kk
	 * 
	* @param   
	 * 
	* @return 
	 * 
	* @Exception 异常对象
	 */
	public void setIdMust(){
		String manufacturer = null;
		try {
			manufacturer = ((Code) ((ControlKeyValue) this.cmbSiteManufacturer.getSelectedItem()).getObject()).getCodeValue();
			
			if("0".equals(manufacturer)){
				//控制武汉特有的网元ID 为必填项
				this.txtSiteId_wh.setEnabled(true);
				this.txtSiteId_wh.setMustFill(true);
				
				//控制晨晓特有的用户名、密码  为必填项
				this.username.setMustFill(false);
				this.username.setEnabled(false);
				this.userpwd.setEnabled(false);
				
				this.username.setText("");
				this.userpwd.setText("");
			}else{
				this.txtSiteId_wh.setText("");
				this.txtSiteId_wh.setEnabled(false);
				this.txtSiteId_wh.setMustFill(false);
				
				//控制晨晓特有的用户名、密码  为必填项
				this.username.setMustFill(true);
				this.username.setEnabled(true);
				this.userpwd.setEnabled(true);
			}
			
		}catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
		
	}

	public void getSite(String siteId) throws Exception {
		List<SiteInst> siteinstList = null;
		SiteService_MB siteService = null;
		try {
			siteInst = new SiteInst();
			siteInst.setSite_Inst_Id(Integer.parseInt(siteId));
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			siteinstList = siteService.select(siteInst);

			if (null != siteinstList && siteinstList.size() > 0) {
				siteInst = siteinstList.get(0);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(siteService);
		}
	}

	public void equipmentTypeDate() throws Exception {
		DefaultComboBoxModel defaultComboBoxModel = null;
		int manufacturer = 0;
		try {
			manufacturer = Integer.parseInt(((Code) ((ControlKeyValue) this.cmbSiteManufacturer.getSelectedItem()).getObject()).getCodeValue());

			defaultComboBoxModel = new DefaultComboBoxModel();
			for (EquipmentType equipmentType : ConstantUtil.equipmentTypeList) {
				if (manufacturer == equipmentType.getManufacturer()) {
					defaultComboBoxModel.addElement(new ControlKeyValue(equipmentType.getTypeName() + "", equipmentType.getTypeName(), null));
				}
			}
			cmbSiteType.setModel(defaultComboBoxModel);
		} catch (Exception e) {
			throw e;
		} finally {
			defaultComboBoxModel = null;
		}
	}

	public void setLayout() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 70, 180, 80 };
		layout.columnWeights = new double[] { 0, 0, 0};
		layout.rowHeights = new int[] { 25, 40, 40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40 };
		layout.rowWeights = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0.2 };
		this.jPanel1.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		int i = 0;
		/** 第0行 */
		c.gridx = 0;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(lblMessage, c);
		this.jPanel1.add(lblMessage);
		/** 第一行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 15, 5);
		layout.setConstraints(jLabel3, c);
		this.jPanel1.add(jLabel3);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(txtSiteName, c);
		this.jPanel1.add(txtSiteName);
		/** 第二行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(jLabel2, c);
		this.jPanel1.add(jLabel2);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(txtSiteIp, c);
		this.jPanel1.add(txtSiteIp);
		/** 第三行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(jLabel5, c);
		this.jPanel1.add(jLabel5);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(cmbSiteManufacturer, c);
		this.jPanel1.add(cmbSiteManufacturer);
		/** 第四行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(jLabel4, c);
		this.jPanel1.add(jLabel4);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(cmbSiteType, c);
		this.jPanel1.add(cmbSiteType);
		
		//所属组
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(groupJLabel, c);
		this.jPanel1.add(groupJLabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(groupJComboBox, c);
		this.jPanel1.add(groupJComboBox);
		
		//所属子网
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(belongSubnet, c);
		this.jPanel1.add(belongSubnet);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(subnetCombo, c);
		this.jPanel1.add(subnetCombo);
		
		//网元类型
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(siteType, c);
		this.jPanel1.add(siteType);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(siteTypeCombo, c);
		this.jPanel1.add(siteTypeCombo);
		
		//网元位置
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(siteLocation, c);
		this.jPanel1.add(siteLocation);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(txtSiteLocation, c);
		this.jPanel1.add(txtSiteLocation);
		
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(this.rackJLabel, c);
		this.jPanel1.add(this.rackJLabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(this.rackPtnTextField, c);
		this.jPanel1.add(this.rackPtnTextField);
		
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(this.shelfJLabel, c);
		this.jPanel1.add(this.shelfJLabel);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(this.shelfPtnTextField, c);
		this.jPanel1.add(this.shelfPtnTextField);

		/** 第10行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(this.lblSiteId_wh, c);
		this.jPanel1.add(this.lblSiteId_wh);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(this.txtSiteId_wh, c);
		this.jPanel1.add(this.txtSiteId_wh);
		
		/** 第12行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(jLabel14, c);
		this.jPanel1.add(jLabel14);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(username, c);
		this.jPanel1.add(username);
		/** 第13行 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(jLabel15, c);
		this.jPanel1.add(jLabel15);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(userpwd, c);
		this.jPanel1.add(userpwd);

		/** 是否是网关网元 */
		c.gridx = 0;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		layout.setConstraints(this.lblIsGateway, c);
		this.jPanel1.add(this.lblIsGateway);
		c.gridx = 1;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 2;
		layout.addLayoutComponent(this.chkIsGateway, c);
		this.jPanel1.add(this.chkIsGateway);

		c.gridx = 0; 
		c.gridy = i; 
		c.gridheight = 1; 
		c.gridwidth = 1; 
		layout.setConstraints(this.siteColorJLabel, c); 
		this.jPanel1.add(this.siteColorJLabel); 
		c.gridx = 1; 
		c.gridy = i++; 
		c.gridheight = 1; 
		c.gridwidth = 2; 
		layout.addLayoutComponent(this.siteColorJButton, c); 
		this.jPanel1.add(this.siteColorJButton); 

		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = i;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(jButton1, c);
		this.jPanel1.add(jButton1);
		c.gridx = 2;
		c.gridy = i++;
		c.gridheight = 1;
		c.gridwidth = 1;
	
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(jButton2, c);
		this.jPanel1.add(jButton2);
		this.add(jPanel1);
	}


	public void initComponents() throws Exception {
		jPanel1 = new JPanel();
		lblMessage = new JLabel();
		jButton1 = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SAVE),false);
		jButton2 = new JButton(ResourceUtil.srcStr(StringKeysBtn.BTN_CANEL));
		jLabel2 = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_IP));
		txtSiteName = new PtnTextField(true, PtnTextField.STRING_MAXLENGTH, this.lblMessage, this.jButton1, this);
		jLabel3 = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_NAME));
		txtSiteIp = new PtnTextField(true, PtnTextField.TYPE_IP, PtnTextField.IP_MAXLENGTH, this.lblMessage, this.jButton1, this);
		jLabel4 = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_TYPE));
		jLabel5 = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_MANUFACTURER));
		jTextField5 = new JTextField();		
		jTextField6 = new JTextField();		
		jTextField7 = new JTextField();
		jTextField9 = new JTextField();
		cmbSiteType = new PtnComboBox();
		cmbSiteManufacturer = new PtnComboBox();
		jComboBox3 = new JComboBox();
		jComboBox4 = new JComboBox();
		switchLable = new JTextField();
		switchLable.setEditable(false);
		jLabel14 = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_USERNAME));
		jLabel15 = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_USERPWD));
		username = new PtnTextField(true, PtnTextField.STRING_MAXLENGTH, this.lblMessage, this.jButton1, this);
		userpwd = new JPasswordField();
		this.chkIsGateway = new JCheckBox();
		this.lblIsGateway = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_IS_GATEWAY));
		this.lblSiteId_wh = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_EQUIPMENT_ID));
		this.txtSiteId_wh = new PtnTextField(true, PtnTextField.TYPE_INT, PtnTextField.INT_MAXLENGTH, this.lblMessage, this.jButton1, this);
		this.txtSiteId_wh.setCheckingMaxValue(true);
		this.txtSiteId_wh.setCheckingMinValue(true);
		this.txtSiteId_wh.setMaxValue(254);
		this.txtSiteId_wh.setMinValue(1);
		this.subnetCombo = new JComboBox();
		this.belongSubnet = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SUBNET_BELONG));
		this.groupJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_GROUP_BELONG));
		this.groupJComboBox = new JComboBox();
		this.siteType = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SIETTYPE));
		this.siteTypeCombo = new JComboBox();
		
		rackJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_RACK_LOCATION));
		rackPtnTextField = new PtnTextField(true, PtnTextField.TYPE_INT, PtnTextField.INT_MAXLENGTH, this.lblMessage, this.jButton1, this);
		this.rackPtnTextField.setCheckingMaxValue(true);
		this.rackPtnTextField.setCheckingMinValue(true);
		this.rackPtnTextField.setMaxValue(254);
		this.rackPtnTextField.setMinValue(1);
		
		shelfJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SHELF_LOCATION));
		shelfPtnTextField = new PtnTextField(true, PtnTextField.TYPE_INT, PtnTextField.INT_MAXLENGTH, this.lblMessage, this.jButton1, this);
		this.shelfPtnTextField.setCheckingMaxValue(true);
		this.shelfPtnTextField.setCheckingMinValue(true);
		this.shelfPtnTextField.setMaxValue(254);
		this.shelfPtnTextField.setMinValue(1);
		
		//新增网元位置 add by dxh
		this.siteLocation = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_lOCATION));
		txtSiteLocation = new JTextField();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initGroupCombobox(this.groupJComboBox);
		initCombobox(this.subnetCombo);
		siteColorJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_lOCATION)); 
		siteColorJButton = new JButton();
	}

	
	private void initGroupCombobox(JComboBox groupJComboBox) {

		FieldService_MB service = null;
		List<Field> fieldList = null ;
		Field f = null;
		DefaultComboBoxModel defaultComboBoxModel = (DefaultComboBoxModel) groupJComboBox.getModel();
		try {
			service = (FieldService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Field);
			if(siteInst != null){//网元管理修改网元时，会导致ConstantUtil.fieldId与网元所在域不一致
				f = service.selectByFieldId(siteInst.getFieldID()).get(0);
				if("subnet".equals(f.getObjectType())){
					f = service.selectByFieldId(f.getParentId()).get(0);
					fieldList = service.queryByNetWorkid(f.getNetWorkId());
				}else{
					fieldList = service.queryByNetWorkid(f.getNetWorkId());
				}
			}else{
			   fieldList = service.queryByNetWorkid(ConstantUtil.fieldId);
			}
			for (Field field : fieldList) {
//					defaultComboBoxModel.addElement(new ControlKeyValue(field.getGroupId() + "", field.getFieldName(), field));
					defaultComboBoxModel.addElement(new ControlKeyValue(field.getId() + "", field.getFieldName(), field));
					
			}
			groupJComboBox.setModel(defaultComboBoxModel);
		
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
	
	}
	public void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
		this.dispose();
	}

	@SuppressWarnings("deprecation")
	public void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		ControlKeyValue typeSelect = null;
		ControlKeyValue cellEditionSelect = null;
		ControlKeyValue cellTimezoneSelect = null;
		ControlKeyValue type = null;
		ControlKeyValue  subnetSelect ;
		ControlKeyValue siteTypeSelect;
		EquipmentType equipmentType = null;
		ControlKeyValue groupSelect = null;
		try {
			
			//验证有无所属组
			groupSelect = (ControlKeyValue) this.groupJComboBox.getSelectedItem();
			if(groupSelect == null || !(Integer.parseInt(groupSelect.getId())>0)){
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_GROUP_BELONG));
				UiUtil.insertOperationLog(EOperationLogType.CRATEASITE1.getValue());
				return;
			}
			
			Field field = (Field) groupSelect.getObject();
			// 校验域中是否已存在M类型的网元
			boolean checkM = checkFieldExistM(siteInst,field.getGroupId());
			if (this.chkIsGateway.isSelected() && checkM) {
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_FIELDEXISTMNE));
				UiUtil.insertOperationLog(EOperationLogType.CRATEASITE2.getValue());
				return;
			}
			if(this.siteInst.getSite_Inst_Id() == 0 && !this.chkIsGateway.isSelected() && !checkM){
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_NO_M_INSERT));
				return;
			}
			
			//如果是新建，验证最大管理网元数
			if(this.siteInst.getSite_Inst_Id() == 0){
				if(this.checkingSiteNum()){
					DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_SITENUM_BEYOND));
					UiUtil.insertOperationLog(EOperationLogType.CRATEASITE3.getValue());
					return;
				}
			}

			// 创建或是修改了Id才作校验
			if (!txtSiteName.getText().equals(this.siteInst.getCellId())||this.copySite) {
				if (isNenameExist(txtSiteName.getText().trim(), "name",field.getGroupId())) {
					DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_NAME_EXIST));
					UiUtil.insertOperationLog(EOperationLogType.CRATEASITE4.getValue());
					return;
				}
			}

			if (!this.txtSiteIp.getText().equals(this.siteInst.getCellDescribe())||this.copySite) {
				// 验证网元IP是否存在
				if (isNenameExist(this.txtSiteIp.getText().trim(), "ip",field.getGroupId())) {
					DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_SITE_IP_EXIST));
					UiUtil.insertOperationLog(EOperationLogType.CRATEASITE5.getValue());
					return;
				}
			}
			//界面中的网元ID域唯一，不做判断，去掉提示
			if(this.txtSiteId_wh.getText().length() > 0){
				if (!this.txtSiteId_wh.getText().equals(this.siteInst.getSite_Hum_Id())) {
					// 验证网元id是否存在
					if (isNenameExist(this.txtSiteId_wh.getText().trim(), "id",field.getGroupId())) {
						DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_SITE_ID_EXIST));
						UiUtil.insertOperationLog(EOperationLogType.CRATEASITE6.getValue());
						return;
					}
				}
			}

			if (Verification.jComboBoxNull(cmbSiteType)) {
				DialogBoxUtil.errorDialog(this, ResourceUtil.srcStr(StringKeysTip.TIP_NOT_FULL));
				UiUtil.insertOperationLog(EOperationLogType.CRATEASITE7.getValue());
				return;
			}

			

			typeSelect = (ControlKeyValue) cmbSiteType.getSelectedItem();
			cellEditionSelect = (ControlKeyValue) cmbSiteManufacturer.getSelectedItem();
			cellTimezoneSelect = (ControlKeyValue) jComboBox3.getSelectedItem();
			type = (ControlKeyValue) jComboBox4.getSelectedItem();
			subnetSelect =  (ControlKeyValue) this.subnetCombo.getSelectedItem();
			
			siteTypeSelect =  (ControlKeyValue) this.siteTypeCombo.getSelectedItem();
			
			this.siteInst.setCellId(txtSiteName.getText().trim());
			this.siteInst.setCellDescribe(txtSiteIp.getText().trim());
			this.siteInst.setCellType(typeSelect.getId());
			this.siteInst.setCellEditon(cellEditionSelect.getId());
			this.siteInst.setCellIcccode(jTextField5.getText());
			this.siteInst.setCellTimeServer(jTextField6.getText());
			this.siteInst.setCellTPoam(jTextField7.getText());
			this.siteInst.setCellTimeZone(cellTimezoneSelect.getId());
			this.siteInst.setCellTime(jTextField9.getText());
			this.siteInst.setType(Integer.valueOf(((Code) type.getObject()).getCodeValue()));
			String[] ip = txtSiteIp.getText().trim().split("\\.");
			this.siteInst.setSwich((Integer.parseInt(ip[2]) * 256 + Integer.parseInt(ip[3])) + "");
			//待测
			if(Integer.parseInt(((Code) ((ControlKeyValue) this.cmbSiteManufacturer.getSelectedItem()).getObject()).getCodeValue()) == 1){
				this.siteInst.setLoginstatus(1);
			}else{
				this.siteInst.setLoginstatus(0);
			}
			this.siteInst.setRack(Integer.parseInt(rackPtnTextField.getText()));
			this.siteInst.setShelf(Integer.parseInt(shelfPtnTextField.getText()));
			this.siteInst.setUsername(username.getText());
			this.siteInst.setUserpwd(userpwd.getText());
			this.siteInst.setSiteLocation(txtSiteLocation.getText());
			this.siteInst.setIsGateway(this.chkIsGateway.isSelected() ? 1 : 0);
			this.siteInst.setSite_Hum_Id(this.txtSiteId_wh.getText());
			if(UiUtil.isNull(subnetSelect.getId())){
				this.siteInst.setFieldID(Integer.parseInt(subnetSelect.getId()));
			}else{
				this.siteInst.setFieldID(field.getId());
			}
			siteInst.setCellIcccode(siteColorJButton.getBackground().getRGB()+""); 
			this.siteInst.setSiteType(Integer.parseInt(siteTypeSelect.getId()));
			this.siteInst.setManufacturer(Integer.parseInt(((Code) ((ControlKeyValue) this.cmbSiteManufacturer.getSelectedItem()).getObject()).getCodeValue()));//确定网元厂商类型
			if (siteInst.getSite_Inst_Id() == 0) {
				Random random = new Random();
				this.siteInst.setSiteX(random.nextInt(200) + 300);
				this.siteInst.setSiteY(random.nextInt(200) + 50);
			}
			EquimentDataUtil equimentDataUtil=new EquimentDataUtil();
			equipmentType = equimentDataUtil.getEquipmentType(siteInst.getCellType());
			if (equipmentType != null) {
				siteInst.setEquipInst(this.getEquipInst(equipmentType.getXmlPath()));
			}
			this.siteInst.setCreateUser(ConstantUtil.user.getUser_Name());
			String result = "";
			DispatchUtil dispatchUtil = new DispatchUtil(RmiKeys.RMI_SITE);
			if(siteInst.getSite_Inst_Id()>0){
				result = dispatchUtil.excuteUpdate(siteInst);
			}else{
				result = dispatchUtil.excuteInsert(siteInst);
			}
			
			//添加日志记录
			if(ResourceUtil.srcStr(StringKeysTitle.TIT_CREATE_SITE).equals(this.getTitle())){
				this.jButton1.setOperateKey(EOperationLogType.SITEINSERT.getValue());
			}else{
				this.jButton1.setOperateKey(EOperationLogType.SITELISTUPDATE.getValue());
			}
			int operationResult=0;
			if(ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS).equals(result)){
				operationResult=1;
			}else{
				operationResult=2;
			}
			jButton1.setResult(operationResult);
			DialogBoxUtil.succeedDialog(this, result);
			this.dispose();
			NetworkElementPanel.getNetworkElementPanel().showTopo(true);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
		}
	}

	public boolean isNenameExist(String text, String type,int groupID) {
		SiteService_MB siteService = null;
		SiteInst siteInst = null;
		List<SiteInst> list = null;
		List<Integer> fieldlist = null;
		FieldService_MB fieldService = null;
		try {
			// int id = Integer.valueOf(text.trim());

			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			fieldService = (FieldService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Field);
			siteInst = new SiteInst();
			if ("ip".equals(type)) {
				siteInst.setCellDescribe(text);
			} else if("name".equals(type)) {
				siteInst.setCellId(text);
			} else if("id".equals(type)){
				int humID = Integer.parseInt(text);
				fieldlist = siteService.selectFieldfromhumId(humID);
				for(Integer fieldId : fieldlist){//遍历所有组是否重复
					Field field = fieldService.selectByFieldId(fieldId).get(0);
					if(field.getObjectType().equals("field")){
						if(field.getGroupId() == groupID){
							return true;
						}
					}else {
						field = fieldService.selectByFieldId(field.getParentId()).get(0);
						if(field.getGroupId() == groupID){
							return true;
						}
					}
				}
				
				return false;
			}

			list = siteService.select(siteInst);
			if (list != null && list.size() > 0) {
				return true;
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(fieldService);
			UiUtil.closeService_MB(siteService);
		}
		return false;
	}

	/**
	 * 查看域或子网是否存在
	 * 
	 * @param site
	 *            网元
	 * @return
	 */
	public boolean checkFieldExistM(SiteInst site,int fieldId) {

		FieldService_MB fieldService = null;
		Field field = null;
		try {
			fieldService = (FieldService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Field);
			field = new Field();
			field.setGroupId(fieldId);
			field = fieldService.select(field).get(0);
			if (field.getmSiteId() > 0) {
				if (site.getSite_Inst_Id() == 0) {
					return true;
				} else {
					if (site.getSite_Inst_Id() != field.getmSiteId()) {
						return true;
					}
				}
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(fieldService);
		}

		return false;
	}

	/**
	 * 读取XML获取机架和槽
	 * 
	 * @return
	 * @throws Exception
	 */
	public EquipInst getEquipInst(String xmlPath) throws Exception {

		EquipInst equipInst = null;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document doc = null;
		org.w3c.dom.Element root = null;
		NodeList nodeList = null;
		Element parent = null;
		NodeList childList = null;
		Element child = null;
		List<SlotInst> slotInstList = null;
		SlotInst slotInst = null;

		try {
			equipInst = new EquipInst();
			factory = DocumentBuilderFactory.newInstance();
			// 使用DocumentBuilderFactory构建DocumentBulider
			builder = factory.newDocumentBuilder();
			// 使用DocumentBuilder的parse()方法解析文件
			doc = builder.parse(AddSiteDialog.class.getClassLoader().getResource(xmlPath).toString());
			root = doc.getDocumentElement();
			nodeList = root.getElementsByTagName("equipment");

			for (int i = 0; i < nodeList.getLength(); i++) {
				parent = (org.w3c.dom.Element) nodeList.item(i);

				equipInst.setImagePath(parent.getAttribute("imagePath"));
				equipInst.setEquipx(Integer.parseInt(parent.getAttribute("x")));
				equipInst.setEquipy(Integer.parseInt(parent.getAttribute("y")));

				slotInstList = new ArrayList<SlotInst>();
				childList = parent.getElementsByTagName("slot");
				for (int j = 0; j < childList.getLength(); j++) {
					child = (Element) childList.item(j);
					slotInst = new SlotInst();
					slotInst.setImagePath(child.getAttribute("imagePath"));
					slotInst.setSlotx(Integer.parseInt(child.getAttribute("x")));
					slotInst.setSloty(Integer.parseInt(child.getAttribute("y")));
					slotInst.setSlotType(child.getAttribute("type"));
					slotInst.setBestCardName(child.getAttribute("bestCardName"));
					slotInst.setMasterCardAddress(child.getAttribute("masterCardAddress"));
					if (child.getAttribute("number").length() > 0) {
						slotInst.setNumber(Integer.parseInt(child.getAttribute("number")));
					}
					slotInstList.add(slotInst);
				}
				equipInst.setSlotlist(slotInstList);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			factory = null;
			builder = null;
			doc = null;
			root = null;
			nodeList = null;
			parent = null;
			childList = null;
			child = null;
			slotInstList = null;
			slotInst = null;
		}
		return equipInst;
	}

	public void initCombobox(JComboBox comboBox){
		List<Field> fieldList = null ;
		DefaultComboBoxModel defaultComboBoxModel = (DefaultComboBoxModel) comboBox.getModel();
		FieldService_MB fieldService = null;
		try {
			fieldService = (FieldService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Field);
			if(siteInst != null){
				Field field = new Field();
				field.setId(siteInst.getFieldID());
				fieldList = fieldService.selectByFieldId(siteInst.getFieldID());
				field = fieldList.get(0);
				if("field".equals(field.getObjectType())){
					setComboBox(field.getId(),defaultComboBoxModel,comboBox);
				}else{
					setComboBox(field.getParentId(),defaultComboBoxModel,comboBox);
				}
			}else{
				ControlKeyValue controlKeyValue = (ControlKeyValue) groupJComboBox.getSelectedItem();
				if(controlKeyValue != null){
					Field f = (Field) controlKeyValue.getObject();
					setComboBox(f.getId(),defaultComboBoxModel,comboBox);
				}
			}
			
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(fieldService);
		}
	}
	
	private void setComboBox(int subnetId,DefaultComboBoxModel defaultComboBoxModel,JComboBox comboBox){
		List<Field> fieldList = null;
		SubnetService_MB subnetService = null;
		try {
			subnetService = (SubnetService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SUBNETSERVICE);
			defaultComboBoxModel.removeAllElements();
			fieldList = subnetService.subnetCombo(subnetId+"");
			defaultComboBoxModel.addElement(new ControlKeyValue("", "", null));
			for (Field field : fieldList) {
				defaultComboBoxModel.addElement(new ControlKeyValue(field.getId() + "", field.getFieldName(), field));
			}
			comboBox.setModel(defaultComboBoxModel);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}finally{
			UiUtil.closeService_MB(subnetService);
		}
		
	}
	/**
	 * 验证创建网元数量是否超过了最大网元数
	 * 
	 * @return true 超过了 false 没超过，可以创建
	 * @throws Exception
	 */
	public boolean checkingSiteNum() throws Exception {
		boolean flag = false;
		SiteService_MB siteService = null;
		List<SiteInst> siteInstList = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			siteInstList = siteService.select();

			// 如果等于最大管理网元数，就返回true
			if (null != siteInstList && null != ConstantUtil.serviceBean) {
				if (siteInstList.size() == ConstantUtil.serviceBean.getMaxSiteNumner()) {
					flag = true;
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(siteService);
		}
		return flag;
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	public PtnButton jButton1;
	public JButton jButton2;
	public JComboBox cmbSiteType;
	public JComboBox cmbSiteManufacturer;
	public JComboBox jComboBox3;
	public JComboBox jComboBox4;
	public JLabel lblMessage;
	public JLabel jLabel14;
	public JLabel jLabel15;
	public JLabel jLabel2;
	public JLabel jLabel3;
	public JLabel jLabel4;
	public JLabel jLabel5;
	public JPanel jPanel1;
	public JTextField txtSiteName;
	public JTextField txtSiteIp;
	public JTextField jTextField5;
	public JTextField jTextField6;
	public JTextField jTextField7;
	public JTextField jTextField9;
	public JTextField switchLable;
	public PtnTextField username;
	public JPasswordField userpwd;
	public JCheckBox chkIsGateway; // 是否是网关网元复选框
	public JLabel lblIsGateway; // 是否是网关网元label
	public JLabel lblSiteId_wh; // 武汉特有的网元ID
	public PtnTextField txtSiteId_wh;
	public JComboBox subnetCombo;
	public JLabel belongSubnet;
	public JComboBox siteTypeCombo;
	public JLabel siteType;
	public JLabel siteLocation;
	public JTextField txtSiteLocation;
	public JLabel groupJLabel;//组
	public JComboBox groupJComboBox;
	private JLabel rackJLabel;
	private PtnTextField rackPtnTextField;
	private JLabel shelfJLabel;
	private PtnTextField shelfPtnTextField;
	// End of variables declaration//GEN-END:variables
	private JLabel siteColorJLabel;
	private JButton siteColorJButton;
}