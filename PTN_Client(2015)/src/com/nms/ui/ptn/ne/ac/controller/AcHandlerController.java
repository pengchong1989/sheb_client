﻿package com.nms.ui.ptn.ne.ac.controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import twaver.table.editor.SpinnerNumberEditor;

import com.nms.db.bean.equipment.port.PortInst;
import com.nms.db.bean.ptn.path.eth.ElineInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.port.AcPortInfo;
import com.nms.db.bean.ptn.port.Acbuffer;
import com.nms.db.bean.ptn.port.PortLagInfo;
import com.nms.db.bean.ptn.qos.QosInfo;
import com.nms.db.bean.ptn.qos.QosQueue;
import com.nms.db.bean.system.code.Code;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EManufacturer;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EServiceType;
import com.nms.db.enums.QosCosLevelEnum;
import com.nms.drive.service.impl.CoderUtils;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.ptn.path.eth.ElineInfoService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.port.AcBufferService_MB;
import com.nms.model.ptn.port.AcPortInfoService_MB;
import com.nms.model.ptn.port.PortLagService_MB;
import com.nms.model.ptn.qos.QosInfoService_MB;
import com.nms.model.ptn.qos.QosQueueService_MB;
import com.nms.model.system.code.CodeService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.AutoNamingUtil;
import com.nms.ui.manager.CheckingUtil;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.VerifyNameUtil;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysObj;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.manager.util.ComboBoxDataUtil;
import com.nms.ui.ptn.ne.ac.view.AddACDialog;
import com.nms.ui.ptn.ne.ac.view.AddBufferCXDialog;
import com.nms.ui.ptn.ne.ac.view.AddStreamDialog;

public class AcHandlerController {
	private AddACDialog dialog;
	private Map<Integer, String> codeIdAndCodeNameMap = new HashMap<Integer, String>();
	private AcPanelController acpanelController;
	private int siteId = 0;
	private ComboBoxDataUtil comboBoxDataUtil=new ComboBoxDataUtil();

	/**
	 * 新加siteID  因为要做在eline界面直接可创建AC。
	 * 
	 * @param acpanelController
	 * @param dialog
	 * @param siteId
	 * @throws Exception
	 */
	public AcHandlerController(AcPanelController acpanelController, AddACDialog dialog, int siteId) throws Exception {
		try {
			this.siteId = siteId;
			this.dialog = dialog;
			codeIdAndCodeNameMap = this.getCodeIdAndCodeNameMap();
			this.acpanelController = acpanelController;
			AddListeners();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}

	private void AddListeners() throws Exception {
		dialog.getStep1_cx().getNextBtnStep().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				step1_cx_nextBtnActionPerformed(evt);
			}
		});
		dialog.getStep1_cx().getCancelBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				closedialog2();
			}
		});
		dialog.getStep1_cx().getAutoNamingBtn().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				autoNamingActionPerformedXc();
				
			}
		});
		dialog.getStep2_cx().getPreviousBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				step2_cx_PreviousBtnActionPerformed(evt);
			}
		});
		dialog.getStep2_cx().getNextBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				step2_cx_nextBtnActionPerformed(evt);
			}
		});
		dialog.getStep2_cx().getCancelBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				closedialog2();
			}
		});

		dialog.getStep1().getNextBtnStep().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				step1_nextBtnActionPerformed(evt);
			}
		});
		dialog.getStep1().getCancelBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				closedialog();
			}
		});
		dialog.getStep1().getAutoNamingBtn().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				autoNamingActionPerformed();
				
			}
		});
		dialog.getStep1().getBatch().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				UiUtil.showWindow(dialog.getStep4(), 500, 300);
			}
		});
		
		dialog.getStep2().getPreviousBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				step2_PreviousBtnActionPerformed(evt);
			}
		});
		dialog.getStep2().getNextBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				step2_nextBtnActionPerformed(evt);
			}
		});
		dialog.getStep2().getCancelBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				closedialog();
			}
		});
		dialog.getStep3().getPreviousBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					step3_previousBtnActionPerformed(evt);
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
			}
		});
		dialog.getStep3().getOkBtn().addActionListener(new MyActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				// 完成
				try {
					step3_finishBtnActionPerformed(evt);
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
			}

			@Override
			public boolean checking() {
				return true;
			}
		});

		dialog.getStep3().getAddBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					addBtnActionPerformed(evt);
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
			}
		});

		dialog.getStep3().getUpdateBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					updateBtnActionPerformed(evt);
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
			}
		});
		dialog.getStep3().getDeleteBtn().addActionListener(new MyActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				deleteBtnActionPerformed(evt);
			}

			@Override
			public boolean checking() {
				
				return true;
			}
		});

		this.dialog.getStep3().getSimpleTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				if (dialog.getStep3().getSimpleTable().getSelectedColumn() > 1) {
					commitTable(dialog.getStep3().getSimpleTable());
				}
				if (dialog.getStep3().getSimpleTable().isEditing()) {
					// 返回活动单元格编辑器；如果该表当前没有被编辑，则返回 null。
					dialog.getStep3().getSimpleTable().getCellEditor().stopCellEditing();
				}
				if (dialog.getStep3().getSimpleTable().getSelectedColumn() == 3 || dialog.getStep3().getSimpleTable().getSelectedColumn() == 5||dialog.getStep3().getSimpleTable().getSelectedColumn() == 8) {
					setDataIsConsistent();
				}
			}

		});

		this.dialog.getStep1().getPortJCB().addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == 1) {
					Object object=null;
					try {
						object=((ControlKeyValue)dialog.getStep1().getPortJCB().getSelectedItem()).getObject();
						if(object instanceof PortInst){
							PortInst portInst = (PortInst)object;
							comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getModeJCB(), (portInst.getPortAttr().getPortUniAttr().getVlanRelevance() +4)+ "");
						}else if(object instanceof PortLagInfo){
							PortLagInfo portLagInfo = (PortLagInfo)object;
							comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getModeJCB(), (portLagInfo.getVlanRelating() +54)+ "");
						}
					} catch (Exception e) {
						ExceptionManage.dispose(e,this.getClass());
					}
				}
			}
		});
	}
	/**
	 * 自动命名XC
	 */
	private void autoNamingActionPerformedXc() {
		AcPortInfo acPortInfo = null ;
		String autoNaming;
		ControlKeyValue port;
		PortInst portInst = null;
		try {
			port=(ControlKeyValue) this.dialog.getStep1_cx().getCmbPort().getSelectedItem();
			if(null==port){
				DialogBoxUtil.errorDialog(dialog.getStep3(),ResourceUtil.srcStr(StringKeysTip.TIP_MUSTNETWORK_BEFORE));
				return;
			}
			if(port.getObject() instanceof PortInst){
			portInst = (PortInst) port.getObject();
			}else if(port.getObject() instanceof PortLagInfo){
				portInst = new PortInst();
				portInst.setPortName(port.getName());
			}
			acPortInfo= new AcPortInfo();
			acPortInfo.setSiteId(this.siteId);
			AutoNamingUtil autoNamingUtil=new AutoNamingUtil();
			autoNaming = (String) autoNamingUtil.autoNaming(acPortInfo,portInst, null);
			dialog.getStep1_cx().getTxtName().setText(autoNaming);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	/**
	 * 自动命名
	 */
	private void autoNamingActionPerformed() {
		AcPortInfo acPortInfo ;
		String autoNaming;
		ControlKeyValue port;
		PortInst portInst = null;
		try {
			port=(ControlKeyValue) this.dialog.getStep1().getPortJCB().getSelectedItem();
			if(null==port){
				DialogBoxUtil.errorDialog(dialog.getStep3(),ResourceUtil.srcStr(StringKeysTip.TIP_MUSTNETWORK_BEFORE));
				return;
			}
			if(port.getObject() instanceof PortInst){
			portInst = (PortInst) port.getObject();
				}else if(port.getObject() instanceof PortLagInfo){
					portInst = new PortInst();
					portInst.setPortName(port.getName());
				}
			acPortInfo= new AcPortInfo();
			acPortInfo.setSiteId(this.siteId);
			AutoNamingUtil autoNamingUtil=new AutoNamingUtil();
			autoNaming = (String) autoNamingUtil.autoNaming(acPortInfo,portInst, null);
			dialog.getStep1().getNameJTF().setText(autoNaming);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}

	/*
	 * 当表格数据变化时，使得数据变化一致
	 */
	public void setDataIsConsistent() {
		// pir设的初始值应该== cir + eir
		int selectR = dialog.getStep3().getSimpleTable().getSelectedRow();
		Object cirvalue = dialog.getStep3().getSimpleTable().getValueAt(selectR, 3);
		Object eirvalue = dialog.getStep3().getSimpleTable().getValueAt(selectR, 5);
		Integer value = (Integer) cirvalue + (Integer) eirvalue;
//		if (CodeConfigItem.getInstance().getWuhan() == 1) {
//			if(value>1000){
//				dialog.getStep3().getSimpleTable().setValueAt(1000, selectR, 8);
//				dialog.getStep3().getSimpleTable().setValueAt(1000, selectR, 3);
//				dialog.getStep3().getSimpleTable().setValueAt(0, selectR, 5);
//			}else{
//				dialog.getStep3().getSimpleTable().setValueAt(value, selectR, 8);
//			}
//		}else{
			if(value>1000000){
				dialog.getStep3().getSimpleTable().setValueAt(1000000, selectR, 8);
				dialog.getStep3().getSimpleTable().setValueAt(1000000, selectR, 3);
				dialog.getStep3().getSimpleTable().setValueAt(0, selectR, 5);
			}else{
				dialog.getStep3().getSimpleTable().setValueAt(value, selectR, 8);
			}
//		}
	}

	/*
	 * 使表格数据瞬间变化
	 * 
	 * public void commitTable(JTable simpleTable) { int selectR = -1; int selectC = -1; if (simpleTable.getEditorComponent() != null) { if (simpleTable.getEditorComponent() instanceof JSpinner) { JSpinner spinner = (JSpinner) simpleTable.getEditorComponent(); selectR = simpleTable.getSelectedRow(); selectC = simpleTable.getSelectedColumn(); JTextField ff = ((JSpinner.NumberEditor) (spinner.getComponents()[2])).getTextField(); String value = ff.getText(); ((DefaultEditor) spinner.getEditor()).getTextField().setText(value);
	 * 
	 * try { spinner.commitEdit(); } catch (ParseException e) { ((DefaultEditor) spinner.getEditor()).getTextField().setText((String) spinner.getValue()); } } } }
	 */

	/*
	 * 使表格数据瞬间变化
	 */
	public void commitTable(JTable simpleTable) {
		int selectR = -1;
		int selectC = -1;
		int oldValue = 0;
		int newValue = 0;
		JSpinner spinner = null;
		try {
			if (simpleTable.getEditorComponent() != null) {
				if (simpleTable.getEditorComponent() instanceof JSpinner) {
					spinner = (JSpinner) simpleTable.getEditorComponent();
					selectR = simpleTable.getSelectedRow();
					selectC = simpleTable.getSelectedColumn();
					if (selectR >= 0 && selectC >= 0) {
						oldValue = Integer.valueOf(simpleTable.getValueAt(selectR, selectC) + "");
					}
					JTextField ff = ((JSpinner.NumberEditor) (spinner.getComponents()[2])).getTextField();
					String value = ff.getText();
					((DefaultEditor) spinner.getEditor()).getTextField().setText(value);
					for (char di : value.replace(",", "").toCharArray()) {
						if (!Character.isDigit(di)) {
							return;
						}
					}
					if (selectC == 4 || selectC == 6 || selectC == 9) {
						if ("".equals(value.replace(",", ""))) {
							newValue = 0;
						} else if (Long.parseLong(value.replace(",", "")) >= ConstantUtil.CBS_MAXVALUE) {
							newValue = ConstantUtil.CBS_MAXVALUE;
						} else if (Long.parseLong(value.replace(",", "")) <= 0) {
							newValue = 0;
						} else {
							newValue = Integer.parseInt(value.replace(",", ""));
						}
						spinner.setModel(new SpinnerNumberModel(newValue,0,ConstantUtil.CBS_MAXVALUE, 1));
					} else {
//						if (CodeConfigItem.getInstance().getWuhan() == 1) {
//							if ("".equals(value.replace(",", ""))) {
//								newValue = 0;
//							} else if (Long.parseLong(value.replace(",", "")) >= 1000) {
//								newValue = 1000;
//							} else if (Long.parseLong(value.replace(",", "")) <= 0) {
//								newValue = 0;
//							} else {
//								newValue = Integer.parseInt(value.replace(",", ""));
//							}
//							spinner.setModel(new SpinnerNumberModel(newValue,0,1000,1));
//						}else{
							if ("".equals(value.replace(",", ""))) {
								newValue = 0;
							} else if (Long.parseLong(value.replace(",", "")) >= 1000000) {
								newValue = 1000000;
							} else if (Long.parseLong(value.replace(",", "")) <= 0) {
								newValue = 0;
							} else {
								newValue = Integer.parseInt(value.replace(",", ""));
							}
//						}
							if (newValue % 64 != 0) {
								if (newValue > 64) {
									newValue = ((newValue / 64)) * 64;
								} else {
									newValue = 64;
								}
							}
							spinner.setModel(new SpinnerNumberModel(newValue,0,1000000,64));
					}

					spinner.commitEdit();
					if (simpleTable.isEditing()) {
						simpleTable.getCellEditor().stopCellEditing();
					}
				}
			}
		} catch (Exception e) {
			((DefaultEditor) spinner.getEditor()).getTextField().setText(spinner.getValue() + "");
			ExceptionManage.dispose(e,this.getClass());
		}

	}

	private void step3_finishBtnActionPerformed(java.awt.event.ActionEvent evt) throws Exception {
		boolean b = true;
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {
				// 校验数据
				if (!verifyData()) {
					return;
				}
				if (dialog.getAcInfo() == null) {
					// 完成创建
					b = finishCreate();
				} else {
					// 完成修改
					b = finishUpdate();
				}
			} else {
				if (dialog.getAcInfo() == null) {
					// 完成创建
					finishCreate();
				} else {
					// 完成修改
					finishUpdate_cx();
				}
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
		}
		if(b){
			this.closedialog();
			if(null!=this.acpanelController){
			this.acpanelController.refresh();
		}
	}
	}

	private boolean finishUpdate() {
		// 收集ac信息
		AcPortInfo info = collectData();
		// 收集流数据
		List<Acbuffer> bufferInfos = collectBufferData();

		dialog.getAcInfo().setMacAddressLearn(info.getMacAddressLearn());
		dialog.getAcInfo().setPortModel(info.getPortModel());
		dialog.getAcInfo().setName(info.getName());
		dialog.getAcInfo().setPortId(info.getPortId());
		dialog.getAcInfo().setHorizontalDivision(info.getHorizontalDivision());
		dialog.getAcInfo().setManagerEnable(info.getManagerEnable());
		dialog.getAcInfo().setSiteId(info.getSiteId());
		// dialog.getAcInfo().setTagBehavior(info.getTagBehavior());
		// dialog.getAcInfo().setTagRecognition(info.getTagRecognition());
		dialog.getAcInfo().setTagAction(info.getTagAction());
		dialog.getAcInfo().setExitRule(info.getExitRule());
		dialog.getAcInfo().setVlanId(info.getVlanId());
		dialog.getAcInfo().setVlanpri(info.getVlanpri());
		dialog.getAcInfo().setBufferList(bufferInfos);
		dialog.getAcInfo().setModel(info.getModel());
		dialog.getAcInfo().setAcModelLog(info.getModel());
		dialog.getAcInfo().setMacCount(info.getMacCount());
		dialog.getAcInfo().setDownTpid(info.getDownTpid());
		// 收集简单qos信息
		collectSimpleQosData(info);
		dialog.getAcInfo().setSimpleQos(info.getSimpleQos());
		DispatchUtil acDispatch = null;
		String resultStr = "";
		AcBufferService_MB	uniBufferService = null;
		List<Acbuffer> acacbuffer = null;
		Acbuffer acbuffers = null;
		// 先下发ac,在下发流
		SiteService_MB siteService = null;
		AcPortInfoService_MB acService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {

				if(bufferInfos == null || bufferInfos.size() == 0){
					DialogBoxUtil.errorDialog(dialog.getStep1(), ResourceUtil.srcStr(StringKeysTip.LBL_BUFFER));
					return false;
				}
//				if("1".equals(UiUtil.getCodeById(info.getPortModel()).getCodeValue())){
//					// 验证多条流的vlanId不能一样
//					if (bufferInfos.size() > 0) {
//						List<Integer> vlanIds = new ArrayList<Integer>();
//						for (int i = 0; i < bufferInfos.size(); i++) {
//							int vlanId = bufferInfos.get(i).getVlanId();
//							if (!vlanIds.contains(vlanId)) {
//								vlanIds.add(vlanId);
//							}
//						}
//						if (vlanIds.size() < bufferInfos.size()) {
//							DialogBoxUtil.errorDialog(dialog.getStep1(), ResourceUtil.srcStr(StringKeysTip.TIP_VLANISNOTENOUGH));
//							return false;
//						}
//					}
//				}
				
				//获取该ac的port关联信息
				Object object = ((ControlKeyValue)dialog.getStep1().getPortJCB().getSelectedItem()).getObject();
//				if(object instanceof PortInst){
//					PortInst portInst =(PortInst)(object);
//					if(!("1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getVlanRelevance()).getCodeValue()) 
//							|| "1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getEightIpRelevance()).getCodeValue()) 
//							|| "1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceMacRelevance()).getCodeValue())
//							|| "1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationMacRelevance()).getCodeValue())
//							|| "1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceIpRelevance()).getCodeValue())
//							|| "1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationIpRelevance()).getCodeValue())
//							|| "1".equals(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDscpRelevance()).getCodeValue())
//							)){
//						if (bufferInfos.size() > 1) {
//							DialogBoxUtil.errorDialog(dialog.getStep1(), ResourceUtil.srcStr(StringKeysTip.TIP_NORELEVANCE));
//							return false;
//						}
//					}
//				}
//				
//				// 每个端口ac的vlan不能一样
//				if (info.getLagId() == 0) {
//					uniBufferService = (AcBufferService) ConstantUtil.serviceFactory.newService(Services.UniBuffer);
//					acacbuffer = new ArrayList<Acbuffer>();
//					for(Acbuffer acbuffer : bufferInfos){
//						acbuffers = new Acbuffer();
//						acbuffers.setPortId(info.getPortId());
//						acbuffers.setVlanId(acbuffer.getVlanId());
//						acacbuffer = uniBufferService.select(acbuffers);
//						if (acacbuffer != null && acacbuffer.size() > 0 && dialog.getAcInfo().getId() != acacbuffer.get(0).getAcId()) {
//							DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_VLANIDREPEAT));
//							return false;
//						}
//					}
//				}
				if(!this.checkRelevance(dialog.getAcInfo(), object, bufferInfos)){
					return false;
				}
			}

			//验证业务qos和流qos关系
			if(info.getManagerEnable() == 86 && info.getModel() != 474){
				int total = 0;//已使能流的总qos
				for(Acbuffer acbuffer :bufferInfos){//qos已使能的流
					if(acbuffer.getModel() != 0){
						total+= acbuffer.getCir();
					}
				}
				if(info.getSimpleQos().getPir() < total){
					DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_QOS_BUFFER));
					return false;
				}
			}
			
			if (checkQosEnough(dialog.getAcInfo())) {

				acDispatch = new DispatchUtil(RmiKeys.RMI_AC);
				/*************测试ac 为什么莫名修改被修改了值****2015-3-24********************/
				if(dialog.getAcInfo().getId() >0)
				{
					ExceptionManage.infor("修改AC  acIsUser= "+dialog.getAcInfo().getIsUser(), this.getClass());	
				}
				/*************************************/
				//查询修改前的数据，便于数据比较
				acService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
				AcPortInfo acInfoBefore = acService.selectById(dialog.getAcInfo().getId());
				acInfoBefore.setAcModelLog(acInfoBefore.getModel());
				acInfoBefore.getSimpleQos().setCosLog(acInfoBefore.getSimpleQos().getCos());
				acInfoBefore.getSimpleQos().setSeqLog(acInfoBefore.getSimpleQos().getSeq());
				acInfoBefore.getSimpleQos().setEirLog(acInfoBefore.getSimpleQos().getEir());
				acInfoBefore.getSimpleQos().setEbsLog(acInfoBefore.getSimpleQos().getEbs());
				for(Acbuffer acBuff : acInfoBefore.getBufferList()){
					acBuff.setModelLog(acBuff.getModel());
					acBuff.setStrategyLog(acBuff.getStrategy());
				}
				//************************************//
				resultStr = acDispatch.excuteUpdate(dialog.getAcInfo());
				DialogBoxUtil.succeedDialog(dialog.getStep3(), resultStr);
				//添加日志记录
				this.compareAcBufferList(acInfoBefore.getBufferList(), dialog.getAcInfo().getBufferList());
				AddOperateLog.insertOperLog(dialog.getStep3().getOkBtn(), EOperationLogType.ACUPDATE.getValue(), resultStr,
						acInfoBefore, dialog.getAcInfo(), dialog.getAcInfo().getSiteId(), dialog.getAcInfo().getName(), "ac");
			} else {
				DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_PORT_QOS_ALARM));
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(uniBufferService);
			UiUtil.closeService_MB(siteService);
			UiUtil.closeService_MB(acService);
		}
		return true;
	}

	/**
	 * 将修改前和修改后的细分流排序，便于日志记录比较
	 * @param bufferListBefore
	 * @param bufferList
	 */
	private void compareAcBufferList(List<Acbuffer> bufferListBefore, List<Acbuffer> bufferList) {
		List<Integer> acIdList = new ArrayList<Integer>();
		for(Acbuffer acbuffBefore : bufferListBefore){
			for(Acbuffer acbuff : bufferList){
				if(acbuffBefore.getId() == acbuff.getId()){
					acIdList.add(acbuffBefore.getId());
					break;
				}
			}
		}
		this.sortAcBufferList(bufferListBefore, acIdList);
		this.sortAcBufferList(bufferList, acIdList);
	}

	private void sortAcBufferList(List<Acbuffer> bufferList, List<Integer> acIdList){
		List<Acbuffer> acBuffList = new ArrayList<Acbuffer>();
		for (Integer acId : acIdList) {
			for(Acbuffer acbuff : bufferList){
				if(acbuff.getId() == acId){
					acBuffList.add(acbuff);
					break;
				}
			}
		}
		for(Acbuffer acbuff : bufferList){
			if(!acIdList.contains(acbuff.getId())){
				acBuffList.add(acbuff);
			}
		}
		bufferList.clear();
		bufferList.addAll(acBuffList);
	}
	
	private void finishUpdate_cx() {
		// 收集ac信息
		AcPortInfo info = collectData_cx();
		// 收集流数据
		List<Acbuffer> bufferInfos = collectBufferData();
		dialog.getAcInfo().setName(info.getName());
		dialog.getAcInfo().setSiteId(info.getSiteId());
		dialog.getAcInfo().setPortModel(info.getPortModel());
		dialog.getAcInfo().setOperatorVlanId(info.getOperatorVlanId());
		dialog.getAcInfo().setClientVlanId(info.getClientVlanId());
		dialog.getAcInfo().setManagerEnable(info.getManagerEnable());
		dialog.getAcInfo().setExitRule(info.getExitRule());
		dialog.getAcInfo().setVlanId(info.getVlanId());
		dialog.getAcInfo().setVlancri(info.getVlancri());
		dialog.getAcInfo().setVlanpri(info.getVlanpri());
		dialog.getAcInfo().setBufType(info.getBufType());
		dialog.getAcInfo().setBufferList(bufferInfos);
		// 收集简单qos信息
		collectSimpleQosData(dialog.getAcInfo());

		DispatchUtil acDispatch = null;
		String resultStr = "";
		// 先下发ac,在下发流
		try {

			// 验证多条流的vlanId不能一样
			if (bufferInfos.size() > 0) {
				List<Integer> vlanIds = new ArrayList<Integer>();
				for (int i = 0; i < bufferInfos.size(); i++) {
					int vlanId = bufferInfos.get(i).getVlanId();
					if (!vlanIds.contains(vlanId)) {
						vlanIds.add(vlanId);
					}
				}
				if (vlanIds.size() < bufferInfos.size()) {
					DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_VLANISNOTENOUGH));
					return;
				}
			}

			if (checkQosEnough(dialog.getAcInfo())) {

				acDispatch = new DispatchUtil(RmiKeys.RMI_AC);
				resultStr = acDispatch.excuteUpdate(dialog.getAcInfo());
				DialogBoxUtil.succeedDialog(dialog.getStep3(), resultStr);
				//添加日志记录
				dialog.getStep3().getOkBtn().setOperateKey(EOperationLogType.ACUPDATE.getValue()) ;
				int operationResult=0;
				if(ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS).equals(resultStr)){
					operationResult=1;
				}else{
					operationResult=2;
				}
				dialog.getStep3().getOkBtn().setResult(operationResult);
				// this.acPanel.tableData();
			} else {
				DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_PORT_QOS_ALARM));
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			
		}

	}

	private boolean finishCreate() throws Exception {
		AcPortInfo info = null;
		SiteService_MB siteService = null;
		try {
		  try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {
				// 完成，下发数据
				info = collectData();
			} else {
				// 完成，下发数据
				info = collectData_cx();
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
		// 收集简单qos
		collectSimpleQosData(info);
		// 收集细分流数据
		List<Acbuffer> bufferInfos = collectBufferData();

		DispatchUtil acDispatch = null;
		String resultStr = "";
		// 先下发ac,在下发流
			if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {

				if(bufferInfos == null || bufferInfos.size() == 0){
					DialogBoxUtil.errorDialog(dialog.getStep1(), ResourceUtil.srcStr(StringKeysTip.LBL_BUFFER));
					return false;
				}
				//获取该ac的port关联信息
				Object object = ((ControlKeyValue)dialog.getStep1().getPortJCB().getSelectedItem()).getObject();
				if(!this.checkRelevance(info, object, bufferInfos)){
					return false;
				}
			}
			//验证业务qos和流qos关系
			if(info.getManagerEnable() == 86 && info.getModel() != 474){
				int total = 0;
				for(Acbuffer acbuffer :bufferInfos){
					if(acbuffer.getModel() != 0){
						total+= acbuffer.getCir();
					}
				}
				if(info.getSimpleQos().getPir() < total){
					DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_QOS_BUFFER));
					return false;
				}
			}
			
			if (checkQosEnough(info)) {
				info.setBufferList(bufferInfos);
			} else {
				DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_PORT_QOS_ALARM));
			}
			List<AcPortInfo> acPortInfos = new ArrayList<AcPortInfo>();
			int batchNumber = Integer.parseInt(dialog.getStep1().getPtnSpinnerNumber().getTxt().getText());//批量数量
			int bufferSize = info.getBufferList().size();//每条ac的流数量
			if(batchNumber > 1){
				Object object = ((ControlKeyValue)dialog.getStep1().getPortJCB().getSelectedItem()).getObject();
				if(this.checkPortOrLag(object)){
					acPortInfos.add(info);
					int vlanStep = dialog.getStep4().getVlanStep();
					int sourceMacStep = dialog.getStep4().getSourceMacStep();
					int endMacStep = dialog.getStep4().getEndMacStep();
					int sourceIpStep = dialog.getStep4().getSourceIPStep();
					int endIpStep = dialog.getStep4().getEndIPStep();
					int vlanPriStep = dialog.getStep4().getVlanPriStep();
					for (int i = 1; i < batchNumber; i++) {
						AcPortInfo acPortInfo = info.deepCopy();
						for (int j = 0; j < bufferSize; j++) {
							acPortInfo.getBufferList().get(j).setVlanId(info.getBufferList().get(bufferSize-1).getVlanId()+(bufferSize*(i-1)+j+1)*vlanStep);
							acPortInfo.getBufferList().get(j).setSourceMac(this.long2Mac(this.mac2Long(info.getBufferList().get(bufferSize-1).getSourceMac())+(bufferSize*(i-1)+j+1)*sourceMacStep));
							acPortInfo.getBufferList().get(j).setTargetMac(this.long2Mac(this.mac2Long(info.getBufferList().get(bufferSize-1).getTargetMac())+(bufferSize*(i-1)+j+1)*endMacStep));
							acPortInfo.getBufferList().get(j).setSourceIp(CoderUtils.longToIpAddress(CoderUtils.ipTolong(info.getBufferList().get(bufferSize-1).getSourceIp())+(bufferSize*(i-1)+j+1)*sourceIpStep));
							acPortInfo.getBufferList().get(j).setTargetIp(CoderUtils.longToIpAddress(CoderUtils.ipTolong(info.getBufferList().get(bufferSize-1).getTargetIp())+(bufferSize*(i-1)+j+1)*endIpStep));
							acPortInfo.getBufferList().get(j).setEightIp(info.getBufferList().get(bufferSize-1).getEightIp()+(bufferSize*(i-1)+j+1)*vlanPriStep);
						}
						String result = this.checkBufferValue(acPortInfo.getBufferList());
						if(result == null){
							if(this.checkRelevance(acPortInfo, object, acPortInfo.getBufferList())){
								acPortInfos.add(acPortInfo);
								acPortInfo.setName(info.getName()+"_copy"+(acPortInfos.size()-1));
							}
						}else{
							DialogBoxUtil.errorDialog(dialog.getStep3(), result);
							break;
						}
					}
				}else{
					return false;
				}
			}else{
				acPortInfos.add(info);
			}
			acDispatch = new DispatchUtil(RmiKeys.RMI_AC);
			resultStr = acDispatch.excuteInsert(acPortInfos);
			String result = resultStr;
			if(resultStr.contains(ResultString.CONFIG_SUCCESS)){
				resultStr = ResourceUtil.srcStr(StringKeysTip.TIP_BATCH_CREATE_RESULT);
				resultStr = resultStr.replace("{C}", acPortInfos.size() + "");
				resultStr = resultStr.replace("{S}", (batchNumber - acPortInfos.size()) + "");
			}
			DialogBoxUtil.succeedDialog(dialog.getStep3(), resultStr);
			//添加日志记录
			for (AcPortInfo acPortInfo : acPortInfos) {
				AddOperateLog.insertOperLog(dialog.getStep3().getOkBtn(), EOperationLogType.ACINSERT.getValue(), result,
						null, acPortInfo, acPortInfo.getSiteId(), acPortInfo.getName(), "ac");
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
		}
		return true;
	}
	
	/**
	 * long型转mac地址
	 */
	private String long2Mac(long mac){
		String result = Long.toHexString(mac);
		int length = 12-result.length();
		for (int i = 0; i < length; i++) {
			result = "0"+result;
		}
		String str1 = result.substring(result.length()-2, result.length());
		String str2 = result.substring(result.length()-4, result.length()-2);
		String str3 = result.substring(result.length()-6, result.length()-4);
		String str4 = result.substring(result.length()-8, result.length()-6);
		String str5 = result.substring(result.length()-10, result.length()-8);
		String str6 = result.substring(0, result.length()-10);
		result = str6+"-"+str5+"-"+str4+"-"+str3+"-"+str2+"-"+str1;
		return result.toUpperCase();
	}
	
	/**
	 * mac地址转long型
	 */
	private long mac2Long(String mac){
		mac = mac.replace("-", "");
		return Long.parseLong(mac, 16);
	}
	
	/**
	 * 验证批量创建时vlan，mac，ip是否超出范围
	 * vlan 2-4095
	 * ip 4294967295(maxValue)
	 * mac 281474976710655(maxValue)
	 * vlanPri 0-7
	 */
	private String checkBufferValue(List<Acbuffer> bufferList) {
		for (Acbuffer acbuffer : bufferList) {
			if(acbuffer.getVlanId() > 4095){
				return ResourceUtil.srcStr(StringKeysTip.TIP_VLANID_OUT_OF_SCOPE);
			}
			if(!CheckingUtil.checking(acbuffer.getSourceMac(), CheckingUtil.MAC_REGULAR)){
				return ResourceUtil.srcStr(StringKeysTip.TIP_SOURCE_MAC_OUT_OF_SCOPE);
			}
			if(!CheckingUtil.checking(acbuffer.getTargetMac(), CheckingUtil.MAC_REGULAR)){
				return ResourceUtil.srcStr(StringKeysTip.TIP_END_MAC_OUT_OF_SCOPE);
			}
			if(!CheckingUtil.checking(acbuffer.getSourceIp(), CheckingUtil.IP_REGULAR)){
				return ResourceUtil.srcStr(StringKeysTip.TIP_SOURCE_IP_OUT_OF_SCOPE);
			}
			if(!CheckingUtil.checking(acbuffer.getTargetIp(), CheckingUtil.IP_REGULAR)){
				return ResourceUtil.srcStr(StringKeysTip.TIP_END_IP_OUT_OF_SCOPE);
			}
			if(acbuffer.getEightIp() > 7){
				return "vlanPri"+ResourceUtil.srcStr(StringKeysTip.TIP_OUT_LIMIT);
			}
		}
		return null;
	}
	
	/**
	 * 验证多关联规则下细分流的属性值是否符合要求
	 * 不通过/通过 = false/true
	 * 1  验证是否有关联规则，有/没有 = 可建多条流(进入第二步验证)/只能建一条流(如果有多条流验证不通过，否则验证通过);
	 * 2  验证当前AC是否有多条流，只有一条流，进入第四步验证;
	 * 3  验证当前AC是否有多条流，有多条流，验证是否重复，重复(验证不通过)/不重复(继续第四步验证);
	 * 4  验证该端口下或者该条LAG下的所有流是否重复，重复/不重复 = 验证不通过/验证通过.
	 */
	private boolean checkRelevance(AcPortInfo acInfo, Object obj, List<Acbuffer> bufferList){
		int vlanRelevance = 0;//0/1 = 不关联/关联
		int eightIpRelevance = 0;//0/1 = 不关联/关联
 		int sourMacRelevance = 0;//0/1 = 不关联/关联
		int endMacRelevance = 0;//0/1 = 不关联/关联
		int sourIPRelevance = 0;//0/1 = 不关联/关联
		int endIPRelevance = 0;//0/1 = 不关联/关联
		try {
			//获取关联规则
			if(obj instanceof PortInst){
				PortInst portInst = (PortInst) obj;
				vlanRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getVlanRelevance()).getCodeValue());
			    eightIpRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getEightIpRelevance()).getCodeValue());
			    sourMacRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceMacRelevance()).getCodeValue());
			    endMacRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationMacRelevance()).getCodeValue());
			    sourIPRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceIpRelevance()).getCodeValue());
			    endIPRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationIpRelevance()).getCodeValue());
			}else if(obj instanceof PortLagInfo){
				PortLagInfo lag = (PortLagInfo) obj;
				vlanRelevance = lag.getVlanRelating();
			    eightIpRelevance = lag.getRelatingSet();
			    sourMacRelevance = lag.getFountainMAC();
			    endMacRelevance = lag.getAimMAC();
			    sourIPRelevance = lag.getFountainIP();
			    endIPRelevance = lag.getAimIP();
			}
			//第一步验证
			if(vlanRelevance == 0 && eightIpRelevance == 0 && sourMacRelevance == 0 &&
			   endMacRelevance == 0 && sourIPRelevance == 0 && endIPRelevance == 0){
				if(bufferList.size() > 1){
					DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_NORELEVANCE));
					return false;
				}else{
					return true;
				}
			}else{
				//第二步验证和第三步验证合并
				List<String> relevanceValueList = new ArrayList<String>();
				List<Integer> relevanceList = new ArrayList<Integer>();
				relevanceList.add(vlanRelevance);
				relevanceList.add(eightIpRelevance);
				relevanceList.add(sourMacRelevance);
				relevanceList.add(endMacRelevance);
				relevanceList.add(sourIPRelevance);
				relevanceList.add(endIPRelevance);
				for (int i = 0; i < bufferList.size(); i++) {
					Acbuffer buffer = bufferList.get(i);
					String result = this.appendResult(buffer, relevanceList);
					if(!relevanceValueList.contains(result)){
						relevanceValueList.add(result);
					}
				}
				if(relevanceValueList.size() < bufferList.size()){
					//有重复值
					DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_MORE_BUFFER_VALUE_UNIQUENESS));
					return false;
				}else{
					//第四步验证
					return this.checkAcBuffer(acInfo, bufferList, relevanceValueList, relevanceList);
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		return false;
	}

	/**
	 * 验证端口或者lag是否有关联规则
	 */
	private boolean checkPortOrLag(Object obj) {
		int vlanRelevance = 0;//0/1 = 不关联/关联
		int eightIpRelevance = 0;//0/1 = 不关联/关联
 		int sourMacRelevance = 0;//0/1 = 不关联/关联
		int endMacRelevance = 0;//0/1 = 不关联/关联
		int sourIPRelevance = 0;//0/1 = 不关联/关联
		int endIPRelevance = 0;//0/1 = 不关联/关联
		try {
			//获取关联规则
			if(obj instanceof PortInst){
				PortInst portInst = (PortInst) obj;
				vlanRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getVlanRelevance()).getCodeValue());
			    eightIpRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getEightIpRelevance()).getCodeValue());
			    sourMacRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceMacRelevance()).getCodeValue());
			    endMacRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationMacRelevance()).getCodeValue());
			    sourIPRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceIpRelevance()).getCodeValue());
			    endIPRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationIpRelevance()).getCodeValue());
			}else if(obj instanceof PortLagInfo){
				PortLagInfo lag = (PortLagInfo) obj;
				vlanRelevance = lag.getVlanRelating();
			    eightIpRelevance = lag.getRelatingSet();
			    sourMacRelevance = lag.getFountainMAC();
			    endMacRelevance = lag.getAimMAC();
			    sourIPRelevance = lag.getFountainIP();
			    endIPRelevance = lag.getAimIP();
			}
			if(vlanRelevance == 0 && eightIpRelevance == 0 && sourMacRelevance == 0 &&
			    endMacRelevance == 0 && sourIPRelevance == 0 && endIPRelevance == 0){
				DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_PORT_ISNOT_RELEVANCE_CANT_BATCH));
				return false;
			}else{
				int vlanStep = dialog.getStep4().getVlanStep();
				int sourceMacStep = dialog.getStep4().getSourceMacStep();
				int endMacStep = dialog.getStep4().getEndMacStep();
				int sourceIpStep = dialog.getStep4().getSourceIPStep();
				int endIpStep = dialog.getStep4().getEndIPStep();
				int vlanPriStep = dialog.getStep4().getVlanPriStep();
				int flag1 = 0;
				int flag2 = 0;
				int flag3 = 0;
				int flag4 = 0;
				int flag5 = 0;
				int flag6 = 0;
				if(vlanRelevance > 0 && vlanStep > 0){
					flag1 = 1;
				}
				if(eightIpRelevance > 0 && vlanPriStep > 0){
					flag2 = 1;
				}
				if(sourMacRelevance > 0 && sourceMacStep > 0){
					flag3 = 1;
				}
				if(endMacRelevance > 0 && endMacStep > 0){
					flag4 = 1;
				}
				if(endIPRelevance > 0 && endIpStep > 0){
					flag5 = 1;
				}
				if(sourIPRelevance > 0 && sourceIpStep > 0){
					flag6 = 1;
				}
				if(flag1==0 && flag2==0 && flag3==0 && flag4==0 && flag5==0 && flag6==0){
					DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_MORE_AC_VALUE_UNIQUENESS));
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		return false;
	}

	/**
	 * 验证数据库中是否有细分流和界面的细分流重复
	 * 重复/不重复 = false/true
	 * @param endIPRelevance 
	 * @param sourIPRelevance 
	 * @param endMacRelevance 
	 * @param sourMacRelevance 
	 * @param eightIpRelevance 
	 * @param vlanRelevance 
	 * @param relevanceList 
	 */
	private boolean checkAcBuffer(AcPortInfo acInfo, List<Acbuffer> bufferList, List<String> relevanceValueList, List<Integer> relevanceList) {
		AcPortInfoService_MB service = null;
		try {
			service = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			AcPortInfo acCondition = new AcPortInfo();
			acCondition.setSiteId(acInfo.getSiteId());
			acCondition.setPortId(acInfo.getPortId());
			acCondition.setLagId(acInfo.getLagId());
			List<AcPortInfo> acList = service.queryByAcPortInfo(acCondition);
			if(acList != null && !acList.isEmpty()){
				List<Acbuffer> bufferDBList = new ArrayList<Acbuffer>();
				for (AcPortInfo acPortInfo : acList) {
					//如果是修改，不需要和自身数据比较
					if(acInfo.getId() != acPortInfo.getId()){
						bufferDBList.addAll(acPortInfo.getBufferList());
					}
				}
				for (Acbuffer buffer_db : bufferDBList) {
					String result = this.appendResult(buffer_db, relevanceList);
					if(relevanceValueList.contains(result)){
						//有重复值
						DialogBoxUtil.errorDialog(dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_MORE_AC_VALUE_UNIQUENESS));
						return false;
					}
				}
			}
			return true;
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
		return false;
	}

	private String appendResult(Acbuffer buffer, List<Integer> relevanceList) {
		StringBuffer sb = new StringBuffer();
		if(relevanceList.get(0) == 1){
			sb.append(buffer.getVlanId());
		}
		if(relevanceList.get(1) == 1){
			sb.append(buffer.getEightIp());
		}
		if(relevanceList.get(2) == 1){
			sb.append(buffer.getSourceMac());
		}
		if(relevanceList.get(3) == 1){
			sb.append(buffer.getTargetMac());
		}
		if(relevanceList.get(4) == 1){
			sb.append(buffer.getSourceIp());
		}
		if(relevanceList.get(5) == 1){
			sb.append(buffer.getTargetIp());
		}
		return sb.toString();
	}

	/**
	 * 判断该端口的qos是否用完
	 * 
	 * @param info
	 * @return
	 */
	public boolean checkQosEnough(AcPortInfo info) {
		int portId = info.getPortId();
		int allQos = 0;
		int cos = 0;
		AcPortInfoService_MB acInfoService = null;
		List<AcPortInfo> acPortInfoList = null;
		List<QosQueue> list = null;
		QosInfoService_MB qosInfoService = null;
		AcPortInfo queryAc = null;
		ElineInfoService_MB elineService = null;
		PwInfoService_MB pwInfoService = null;
		List<AcPortInfo> acPortInfos = null;
		try {
			queryAc = new AcPortInfo();
			queryAc.setPortId(info.getPortId());
			acPortInfos = new ArrayList<AcPortInfo>();
			acPortInfos.add(info);
			list = findQosQueueByPortId(portId);// 查询该端口下的QOS队列
			acInfoService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			acPortInfoList = acInfoService.queryByAcPortInfo(queryAc);// 查询该端口下的所有ac
			cos = info.getSimpleQos().getCos();
			for (AcPortInfo acPortInfo : acPortInfoList) {
				QosInfo qosInfo = new QosInfo();
				qosInfo = findQosInfoByAcId(acPortInfo.getId());
				if (cos == qosInfo.getCos()) {
					allQos = allQos + qosInfo.getCir();
				}
			}
			allQos = allQos + info.getSimpleQos().getCir();
			for (QosQueue qosQueue : list) {
				if (qosQueue.getCos() == cos) {
					if (qosQueue.getCir() > allQos) {
						return true;
					}
				}
			}
			
			AcPortInfo acPortInfo = acInfoService.selectById(info.getId());
			if(acPortInfo != null && acPortInfo.getIsUser() == 1){
				
//				EtreeService etreeService = null;
//				ElanInfoService elanInfoService = null;
				
				List<ElineInfo> elineInfos = null;
//				List<EtreeInfo> etreeInfos = null;
//				List<ElanInfo> elanInfos = null;
				elineService = (ElineInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Eline);
//				etreeService = (EtreeService) ConstantUtil.serviceFactory.newService(Services.EtreeInfo);
//				elanInfoService = (ElanInfoService) ConstantUtil.serviceFactory.newService(Services.ElanInfo);
				qosInfoService = (QosInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosInfo);
				pwInfoService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
				elineInfos = elineService.selectByAcIdAndSiteId(acPortInfo.getId(),acPortInfo.getSiteId());
				for(ElineInfo elineInfo : elineInfos){
					PwInfo pwinfo = new PwInfo();
					pwinfo.setPwId(elineInfo.getPwId());
					pwinfo = pwInfoService.selectBypwid_notjoin(pwinfo);
					if(!qosInfoService.checkPwAndAcQos(pwinfo, pwinfo.getQosList(),acPortInfos)){
						return false;
					}
					if(info.getBufferList().get(0).getCir()>pwinfo.getQosList().get(0).getCir()){
						return false;
					}
				}
//				etreeInfos = etreeService.selectByAcIdAndSiteId(acPortInfo.getId(),acPortInfo.getSiteId());
//				elanInfos = elanInfoService.selectByAcIdAndSiteId(acPortInfo.getId(),acPortInfo.getSiteId());
			}
			
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
			return false;
		} finally {
			UiUtil.closeService_MB(acInfoService);
			UiUtil.closeService_MB(pwInfoService);
			UiUtil.closeService_MB(qosInfoService);
			UiUtil.closeService_MB(elineService);
		}
		return true;
	}

	/**
	 * 根据ACID和类型查询qos
	 * 
	 * @param acId
	 * @return
	 */
	private QosInfo findQosInfoByAcId(int acId) {
		QosInfo qosInfo = new QosInfo();
		QosInfoService_MB qosInfoService = null;
		try {
			qosInfoService = (QosInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosInfo);
			qosInfo = qosInfoService.getQosByObj(EServiceType.ACPORT.toString(), acId).get(0);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(qosInfoService);
		}
		return qosInfo;

	}

	/**
	 * 根据端口ID和类型查询qos队列
	 * 
	 * @param portId
	 * @return
	 * @throws Exception
	 */
	private List<QosQueue> findQosQueueByPortId(int portId) throws Exception {
		QosQueueService_MB qosQueueService = null;
		QosQueue qos = null;
		List<QosQueue> list = null;
		try {
			qosQueueService = (QosQueueService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosQueue);
			qos = new QosQueue();
			qos.setObjId(portId);
			qos.setObjType("PORT");
			list = qosQueueService.queryByCondition(qos);
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(qosQueueService);
		}

		return list;

	}

	private void collectSimpleQosData(AcPortInfo info) {
		DefaultTableModel simpleTableModel = null;
		QosInfo simpleQos = null;
		ControlKeyValue bufType = null;
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			bufType = (ControlKeyValue) dialog.getStep3().getBufferTypeJCB().getSelectedItem();
			simpleQos = new QosInfo();
			simpleQos.setQosType(((Code) bufType.getObject()).getCodeName());
			if (info.getSimpleQos() != null) {
				simpleQos.setId(info.getSimpleQos().getId());
			}
			simpleTableModel = (DefaultTableModel) dialog.getStep3().getSimpleTable().getModel();
			Vector vector = simpleTableModel.getDataVector();
			Iterator it = vector.iterator();
			while (it.hasNext()) {
				Vector temp = (Vector) it.next();
				if (temp.get(1) != null) {
					simpleQos.setSeq(Integer.parseInt(temp.get(1).toString()));
					simpleQos.setSeqLog(simpleQos.getSeq());
				}
				if (temp.get(2) != null) {
					simpleQos.setCos(QosCosLevelEnum.from(temp.get(2).toString()));
					simpleQos.setCosLog(simpleQos.getCos());
				}
				if (temp.get(3) != null) {
					simpleQos.setCir(Integer.parseInt(temp.get(3).toString()));
				}
				if (temp.get(4) != null) {
					simpleQos.setCbs(Integer.parseInt(temp.get(4).toString()));
				}
				if (temp.get(5) != null) {
					simpleQos.setEir(Integer.parseInt(temp.get(5).toString()));
					simpleQos.setEirLog(simpleQos.getEir());
				}
				if (temp.get(6) != null) {
					simpleQos.setEbs(Integer.parseInt(temp.get(6).toString()));
					simpleQos.setEbsLog(simpleQos.getEbs());
				}
				if (temp.get(7) != null) {
					simpleQos.setColorSence((Boolean) temp.get(7) ? 1 : 0);
				}
				if (temp.get(8) != null) {
					simpleQos.setPir(Integer.parseInt(temp.get(8).toString()));
				}
				if (siteService.getManufacturer(info.getSiteId()) == EManufacturer.WUHAN.getValue()) {
					if (temp.get(9) != null) {
						simpleQos.setPbs(Integer.parseInt(temp.get(9).toString()));
					}
				}
			}
			info.setSimpleQos(simpleQos);

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
		}

	}

	private AcPortInfo collectData() {
		AcPortInfo info = new AcPortInfo();
		Object obj = null;
		ControlKeyValue controlKeyValue = null;
		PortInst portInfo = null;
		PortLagInfo lagInfo = null;

		try {
			controlKeyValue = (ControlKeyValue) dialog.getStep1().getPortJCB().getSelectedItem();
			obj = controlKeyValue.getObject();
			if (obj instanceof PortLagInfo) {
				lagInfo = (PortLagInfo) obj;
				info.setLagId(lagInfo.getId());
				info.setSiteId(lagInfo.getSiteId());
				info.setPortNameLog("lag/"+lagInfo.getLagID());
			} else {
				portInfo = (PortInst) obj;
				info.setPortId(portInfo.getPortId());
				info.setSiteId(portInfo.getSiteId());
				info.setPortNameLog(portInfo.getPortName());
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}

		info.setName(dialog.getStep1().getNameJTF().getText());
		ControlKeyValue portModel = (ControlKeyValue) dialog.getStep1().getModeJCB().getSelectedItem();
		ControlKeyValue tagBehavior = (ControlKeyValue) dialog.getStep2().getTagActionJCB().getSelectedItem();
		ControlKeyValue tagRecognition = (ControlKeyValue) dialog.getStep2().getTagReconfigJCB().getSelectedItem();
		ControlKeyValue MACLearn = (ControlKeyValue) dialog.getStep1().getMACLearnJCB().getSelectedItem();
		ControlKeyValue splitCut = (ControlKeyValue) dialog.getStep1().getSplitCutJCB().getSelectedItem();
		ControlKeyValue VCEnable = (ControlKeyValue) dialog.getStep1().getVCEnableJCB().getSelectedItem();
		ControlKeyValue bufType = (ControlKeyValue) dialog.getStep3().getBufferTypeJCB().getSelectedItem();
		ControlKeyValue model = (ControlKeyValue) dialog.getStep2().getCmbModel().getSelectedItem();
		ControlKeyValue tpid = (ControlKeyValue) dialog.getStep2().getTpidJbox().getSelectedItem();
		info.setModel(((Code)model.getObject()).getId());
		info.setAcModelLog(info.getModel());
		info.setPortModel(((Code) portModel.getObject()).getId());
		info.setExitRule(((Code) tagBehavior.getObject()).getId());
		info.setTagAction(((Code) tagRecognition.getObject()).getId());
		info.setMacAddressLearn(((Code) MACLearn.getObject()).getId());
		info.setHorizontalDivision(((Code) splitCut.getObject()).getId());
		info.setManagerEnable(((Code) VCEnable.getObject()).getId());
		info.setBufType(((Code) bufType.getObject()).getId());
		info.setVlanId(dialog.getStep2().getAddVlanIdJTF().getText().trim());
		info.setVlanpri(dialog.getStep2().getAddVlanPriJTF().getText().trim());
		info.setAcStatus(EActiveStatus.ACTIVITY.getValue());
		info.setMacCount(Integer.parseInt(dialog.getStep2().getMacCountField().getText()));
		info.setDownTpid(((Code)tpid.getObject()).getId());
		return info;
	}

	/**
	 * 晨晓数据收集
	 */
	private AcPortInfo collectData_cx() {
		AcPortInfo info = new AcPortInfo();
		Object obj = null;
		ControlKeyValue controlKeyValue = null;
		PortInst portInfo = null;
		PortLagInfo lagInfo = null;

		try {
			controlKeyValue = (ControlKeyValue) dialog.getStep1_cx().getCmbPort().getSelectedItem();
			obj = controlKeyValue.getObject();
			if (obj instanceof PortLagInfo) {
				lagInfo = (PortLagInfo) obj;
				info.setLagId(lagInfo.getId());
				info.setSiteId(lagInfo.getSiteId());
			} else {
				portInfo = (PortInst) obj;
				info.setPortId(portInfo.getPortId());
				info.setSiteId(portInfo.getSiteId());
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}

		info.setName(dialog.getStep1_cx().getTxtName().getText());

		ControlKeyValue mode = (ControlKeyValue) dialog.getStep1_cx().getCmbMode().getSelectedItem();
		ControlKeyValue managerEnable = (ControlKeyValue) dialog.getStep1_cx().getCmbManagerEnable().getSelectedItem();
		ControlKeyValue exitRule = (ControlKeyValue) dialog.getStep2_cx().getCmbExitRule().getSelectedItem();
		ControlKeyValue bufType = (ControlKeyValue) dialog.getStep3().getBufferTypeJCB().getSelectedItem();

		info.setPortModel(((Code) mode.getObject()).getId());
		info.setManagerEnable(((Code) managerEnable.getObject()).getId());
		info.setExitRule(((Code) exitRule.getObject()).getId());
		info.setOperatorVlanId(dialog.getStep1_cx().getTxtOperatorVlanId().getText());
		info.setClientVlanId(dialog.getStep1_cx().getTxtClientVlanId().getText());
		info.setVlanId(dialog.getStep2_cx().getTxtVlanId().getText());
		info.setVlancri(dialog.getStep2_cx().getTxtVlanCri().getText());
		info.setVlanpri(dialog.getStep2_cx().getTxtVlanPri().getText());
		info.setBufType(((Code) bufType.getObject()).getId());
		if(0==info.getId()){
			info.setAcStatus(EActiveStatus.ACTIVITY.getValue());
		}
		return info;
	}

	// 添加细分流
	private void addBtnActionPerformed(java.awt.event.ActionEvent evt) throws Exception  {
		SiteService_MB siteService = null;
		try {
			if (dialog.getBufferList().size() >= 10) {
				DialogBoxUtil.errorDialog(this.dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_BUFFER_MORETHAN_10));
				return;
			}
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(ConstantUtil.siteId) == EManufacturer.WUHAN.getValue()) {
				AddStreamDialog dialog = new AddStreamDialog(this.dialog, true, null);
				BufferController controller = new BufferController(dialog);
				controller.openAddStreamDialog();
			} else {
				AddBufferCXDialog bufferCx = new AddBufferCXDialog(this.dialog, true, null);
				//		BufferCXController controllerCX = new BufferCXController(bufferCx);
				//		controllerCX.openAddStreamDialog();
			}
		} catch (Exception e) {
			throw e;
		}finally{
			UiUtil.closeService_MB(siteService);
		}
	}

	// 更新细分流
	private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) throws Exception {
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (dialog.getStep3().getDetailTable().getSelectedRowCount() > 0) {
				Acbuffer selectBuffer = (Acbuffer) dialog.getStep3().getDetailTable().getValueAt(dialog.getStep3().getDetailTable().getSelectedRows()[0], 0);
				if (siteService.getManufacturer(ConstantUtil.siteId) == EManufacturer.WUHAN.getValue()) {
					AddStreamDialog dialog = new AddStreamDialog(this.dialog, true, selectBuffer);
					BufferController controller = new BufferController(dialog);
					controller.openAddStreamDialog();
				} else {
					AddBufferCXDialog bufferCx = new AddBufferCXDialog(this.dialog, true, selectBuffer);
				}
			} else {
				DialogBoxUtil.errorDialog(this.dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_DATA_ONE));
			}
		} catch (Exception e) {
			throw e;
		}finally{
			UiUtil.closeService_MB(siteService);
		}
	}

	// 删除细分流
	private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {
		int selectSize = dialog.getStep3().getDetailTable().getSelectedRows().length;
		AcBufferService_MB bufService = null;
		try {
			bufService = (AcBufferService_MB) ConstantUtil.serviceFactory.newService_MB(Services.UniBuffer);
			if (selectSize > 0) {
				for (int i = 0; i < selectSize; i++) {
					Acbuffer selectBuffer = (Acbuffer) dialog.getStep3().getDetailTable().getValueAt(dialog.getStep3().getDetailTable().getSelectedRows()[i], 0);
					dialog.getBufferList().remove(selectBuffer); // 移除内存中的数据
					// 如果是在更新ac界面信息的话，table表的流中在数据库中存在，则删除表中的流时，还得将数据库中对应的流删除
					if (selectBuffer.getId() > 0) {
//						bufService = (AcBufferService) ConstantUtil.serviceFactory.newService(Services.UniBuffer);
						bufService.deletebybufferId(selectBuffer.getId());
					}
				}
				this.detailTableDataBox();
				dialog.getStep3().getDetailTable().clearSelection();
				DialogBoxUtil.succeedDialog(this.dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));
			} else {
				DialogBoxUtil.errorDialog(this.dialog.getStep3(), ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_DATA_ONE));
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(bufService);
		}
	}

	public void detailTableDataBox() {
		DefaultTableModel defaultTableModel = null;
		try {
			defaultTableModel = (DefaultTableModel) dialog.getStep3().getDetailTable().getModel();
			defaultTableModel.getDataVector().clear();
			defaultTableModel.fireTableDataChanged();
			for (int i = 0; i < this.dialog.getBufferList().size(); i++) {
				Object[] obj = new Object[] { this.dialog.getBufferList().get(i), i + 1, this.codeIdAndCodeNameMap.get(this.dialog.getBufferList().get(i).getPhb()), this.dialog.getBufferList().get(i).getCir(), this.dialog.getBufferList().get(i).getCbs(), this.dialog.getBufferList().get(i).getCm() == 0 ? new Boolean(false) : new Boolean(true), this.dialog.getBufferList().get(i).getPir(), this.dialog.getBufferList().get(i).getPbs() };
				defaultTableModel.addRow(obj);
			}
			dialog.getStep3().getDetailTable().setModel(defaultTableModel);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			defaultTableModel = null;
		}

	}

	private Map<Integer, String> getCodeIdAndCodeNameMap() {
		Map<Integer, String> codeIdAndValueMap = null;
		CodeService_MB codeService = null;
		List<Code> codeList = null;
		try {
			codeIdAndValueMap = new HashMap<Integer, String>();
			codeService = (CodeService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Code);
			codeList = codeService.selectAll();
			for (Code code : codeList) {
				codeIdAndValueMap.put(code.getId(), code.getCodeName());
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(codeService);
		}
		return codeIdAndValueMap;
	}

	private boolean verifyData() {

		if (dialog.getStep1().getNameJTF().getText().trim().length() == 0) {
			DialogBoxUtil.errorDialog(dialog.getStep1(), ResourceUtil.srcStr(StringKeysTip.TIP_NOT_FULL));
			return false;
		}
		if (dialog.getStep1().getPortJCB().getSelectedItem() == null) {
			DialogBoxUtil.errorDialog(dialog.getStep1(), ResourceUtil.srcStr(StringKeysTip.TIP_PORTFORAC));
			return false;
		}

//		if (Integer.parseInt(dialog.getStep2().getAddVlanIdJTF().getText().trim()) < 2 || Integer.parseInt(dialog.getStep2().getAddVlanIdJTF().getText().trim()) > 2047) {
//			DialogBoxUtil.errorDialog(dialog.getStep2(), ResourceUtil.srcStr(StringKeysTip.TIP_DOWNADDVLANID));
//			return false;
//		}

		if (Integer.parseInt(dialog.getStep2().getAddVlanPriJTF().getText().trim()) < 0 || Integer.parseInt(dialog.getStep2().getAddVlanPriJTF().getText().trim()) > 7) {
			DialogBoxUtil.errorDialog(dialog.getStep2(), ResourceUtil.srcStr(StringKeysTip.TIP_DOWNADDVLANID));
			return false;
		}

		String beforeName = null;
		if (null != dialog.getAcInfo()) {
			beforeName = dialog.getAcInfo().getName();
		}
		VerifyNameUtil verifyNameUtil=new VerifyNameUtil();
		if (verifyNameUtil.verifyNameBySingle(EServiceType.ACPORT.getValue(), dialog.getStep1().getNameJTF().getText(), beforeName, this.siteId)) {
			DialogBoxUtil.errorDialog(dialog.getStep2(), ResourceUtil.srcStr(StringKeysTip.TIP_NAME_EXIST));
			return false;
		}

		// if (collectBufferData().isEmpty()) {
		// DialogBoxUtil.errorDialog(dialog.getStep3(),ResourceUtil.srcStr(StringKeysTip.TIP_BUFFER_DATA_ONE));
		// return false;
		// }
		return true;

	}

	private List<Acbuffer> collectBufferData() {
		List<Acbuffer> bufferinfos = new ArrayList<Acbuffer>();
		DefaultTableModel detailTableModel = null;

		detailTableModel = (DefaultTableModel) dialog.getStep3().getDetailTable().getModel();
		int detailTableSize = detailTableModel.getDataVector().size();
		if (detailTableSize > 0) {
			for (int i = 0; i < detailTableSize; i++) {
				Acbuffer buf = (Acbuffer) ((Vector) detailTableModel.getDataVector().elementAt(i)).elementAt(0);
				buf.setSiteId(this.siteId);
				bufferinfos.add(buf);
			}
		}
		return bufferinfos;
	}

	private void closedialog() {
		dialog.getStep1().dispose();
		dialog.getStep2().dispose();
		dialog.getStep3().dispose();
	}

	private void closedialog2() {
		dialog.getStep1_cx().dispose();
		dialog.getStep2_cx().dispose();
		dialog.getStep3().dispose();
	}

	private void step1_nextBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep1().getLocation();
		Dimension dimension = dialog.getStep1().getSize();
		dialog.getStep2().setLocation(point);
		dialog.getStep2().setSize(dimension);
		dialog.getStep1().dispose();
		dialog.getStep2().setVisible(true);
	}

	private void step1_cx_nextBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep1_cx().getLocation();
		Dimension dimension = dialog.getStep1_cx().getSize();
		dialog.getStep2_cx().setLocation(point);
		dialog.getStep2_cx().setSize(dimension);
		dialog.getStep1_cx().dispose();
		dialog.getStep2_cx().setVisible(true);
	}

	private void step2_PreviousBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep2().getLocation();
		Dimension dimension = dialog.getStep2().getSize();
		dialog.getStep1().setLocation(point);
		dialog.getStep1().setSize(dimension);
		dialog.getStep2().dispose();
		dialog.getStep1().setVisible(true);
	}

	private void step2_cx_PreviousBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep2_cx().getLocation();
		Dimension dimension = dialog.getStep2_cx().getSize();
		dialog.getStep1_cx().setLocation(point);
		dialog.getStep2_cx().setSize(dimension);
		dialog.getStep2_cx().dispose();
		dialog.getStep1_cx().setVisible(true);
	}

	private void step2_cx_nextBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep2_cx().getLocation();
		Dimension dimension = dialog.getStep2_cx().getSize();
		dialog.getStep3().setLocation(point);
		dialog.getStep3().setSize(dimension);
		dialog.getStep2_cx().dispose();
		dialog.getStep3().setVisible(true);
	}

	private void step2_nextBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep2().getLocation();
		Dimension dimension = dialog.getStep2().getSize();
		dialog.getStep3().setLocation(point);
		dialog.getStep3().setSize(dimension);
		dialog.getStep2().dispose();
		dialog.getStep3().setVisible(true);
	}

	private void step3_previousBtnActionPerformed(ActionEvent evt) throws Exception {
		Point point = dialog.getStep3().getLocation();
		Dimension dimension = dialog.getStep3().getSize();
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {
				dialog.getStep2().setLocation(point);
				dialog.getStep2().setSize(dimension);
				dialog.getStep3().dispose();
				dialog.getStep2().setVisible(true);
			} else {
				dialog.getStep2_cx().setLocation(point);
				dialog.getStep2_cx().setSize(dimension);
				dialog.getStep3().dispose();
				dialog.getStep2_cx().setVisible(true);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
		}

	}

	private void step3_nextBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep3().getLocation();
		Dimension dimension = dialog.getStep3().getSize();
		dialog.getStep4().setLocation(point);
		dialog.getStep4().setSize(dimension);
		dialog.getStep3().dispose();
		dialog.getStep4().setVisible(true);
	}
	
	private void step3__cx_previousBtnActionPerformed(ActionEvent evt) {
		Point point = dialog.getStep3().getLocation();
		Dimension dimension = dialog.getStep3().getSize();
		dialog.getStep2_cx().setLocation(point);
		dialog.getStep2_cx().setSize(dimension);
		dialog.getStep3().dispose();
		dialog.getStep2_cx().setVisible(true);
	}

	public void initData(boolean isAdd,int siteId) {
		configSimpleTable(siteId);
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {
				comboBoxDataUtil.comboBoxData(dialog.getStep1().getModeJCB(), "UNIPORTMODE");
				comboBoxDataUtil.comboBoxData(dialog.getStep2().getTagReconfigJCB(), "TAGRECOGNITION");
				comboBoxDataUtil.comboBoxData(dialog.getStep1().getMACLearnJCB(), "MACLEARN");
				comboBoxDataUtil.comboBoxSelectByValue(dialog.getStep1().getMACLearnJCB(), "1");
				comboBoxDataUtil.comboBoxData(dialog.getStep1().getSplitCutJCB(), "VCTRAFFICPOLICING");
				comboBoxDataUtil.comboBoxData(dialog.getStep1().getVCEnableJCB(), "VCTRAFFICPOLICING");
				// 初始化ac承载的port
				this.comboBoxData(dialog.getStep1().getPortJCB(), this.siteId, isAdd);
				// 绑定TAG行为
				comboBoxDataUtil.comboBoxData(dialog.getStep2().getTagActionJCB(), "PORTTAGBEHAVIOR");
				//初始化模式
				comboBoxDataUtil.comboBoxData(dialog.getStep2().getCmbModel(), "MODEL");
				comboBoxDataUtil.comboBoxData(dialog.getStep2().getTpidJbox(), "LAGVLANTPID");
				
			} else {
				comboBoxDataUtil.comboBoxData(dialog.getStep1_cx().getCmbMode(), "portModel");
				comboBoxDataUtil.comboBoxData(dialog.getStep1_cx().getCmbManagerEnable(), "ENABLEDSTATUE");
				comboBoxDataUtil.comboBoxData(dialog.getStep2_cx().getCmbExitRule(), "exitRule");
				this.comboBoxData(dialog.getStep1_cx().getCmbPort(), this.siteId, isAdd);
			}
			comboBoxDataUtil.comboBoxData(dialog.getStep3().getBufferTypeJCB(), "BUFTYPE");
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
		}
	}

	private void comboBoxData(JComboBox jComboBox, Integer siteId, boolean isAdd) {
		PortService_MB portService = null;
		List<PortInst> portInstList = null;
		DefaultComboBoxModel model = null;
		PortLagService_MB lagService = null;
		PortInst portInst = null;
		PortLagInfo portLagInfo = null;
		List<PortLagInfo> portLagInfoList = null;
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			lagService = (PortLagService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORTLAG);
			portInst = new PortInst();
			portInst.setPortType("UNI");
			if (siteId == 0) {
				portInst.setSiteId(this.siteId);
			} else {
				portInst.setSiteId(siteId);
			}
			portInst.setIsOccupy(0);
			portInstList = portService.select(portInst);

			model = (DefaultComboBoxModel) jComboBox.getModel();

			for (PortInst info : portInstList) {
				if (info.getLagId() == 0 && info.getIsEnabled_code() == 1) {

					AcPortInfo acPortInfo = new AcPortInfo();
					acPortInfo.setPortId(info.getPortId());
					if (isAdd) {
						if (this.acIsExist(acPortInfo)) {
							model.addElement(new ControlKeyValue(info.getPortId() + "", info.getPortName(), info));
						}
					} else {
						model.addElement(new ControlKeyValue(info.getPortId() + "", info.getPortName(), info));
					}
				}
			}
			// lag
			portLagInfo = new PortLagInfo();
			if (siteId == 0) {
				portLagInfo.setSiteId(this.siteId);
			} else {
				portLagInfo.setSiteId(siteId);
			}	
			if (siteService.getManufacturer(this.siteId) == EManufacturer.CHENXIAO.getValue()) {
				portLagInfo.setLagStatus(EActiveStatus.ACTIVITY.getValue());
			}
			portLagInfoList = lagService.selectLAGByCondition(portLagInfo);
			for (PortLagInfo lagInfo : portLagInfoList) {

				AcPortInfo acPortInfo = new AcPortInfo();
				acPortInfo.setLagId(lagInfo.getId());
				if (isAdd) {
					if (this.acIsExist(acPortInfo)) {
						model.addElement(new ControlKeyValue(lagInfo.getId() + "", "" + "lag/" + lagInfo.getLagID(), lagInfo));
					}
				} else {
					model.addElement(new ControlKeyValue(lagInfo.getId() + "", "" + "lag/" + lagInfo.getLagID(), lagInfo));
				}
			}
			dialog.getStep1().getPortJCB().setModel(model);

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(portService);
			UiUtil.closeService_MB(lagService);
			UiUtil.closeService_MB(siteService);
		}
	}

	/**
	 * 验证是否存在端口模式的AC 如果存在 返回true
	 * 
	 * @author kk
	 * 
	 * @param
	 * 
	 * @return
	 * @throws Exception
	 * 
	 * @Exception 异常对象
	 */
	private boolean acIsExist(AcPortInfo acPortInfo) throws Exception {

		AcPortInfoService_MB acInfoService = null;
		List<AcPortInfo> acPortInfoList = null;
		boolean flag = false;
		PortService_MB portService = null;
		PortInst portInst = null;
		SiteService_MB siteService = null;
		PortLagService_MB portLagService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			acInfoService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			acPortInfoList = acInfoService.selectByCondition(acPortInfo);
			portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			portLagService = (PortLagService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORTLAG);
			if (acPortInfoList != null && acPortInfoList.size()>0) {
				for (AcPortInfo acPortInfo_select : acPortInfoList) {
					if (siteService.getManufacturer(this.siteId) == EManufacturer.WUHAN.getValue()) {
						portInst = new PortInst();
						portInst.setSiteId(this.siteId);
						if(acPortInfo_select.getPortId()>0){
							portInst.setPortId(acPortInfo_select.getPortId());
							portInst = portService.select(portInst).get(0);
//							if("1".equals((UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getVlanRelevance())).getCodeValue())){
								if("1".equals(this.isRelevance(portInst)+"")){
								flag = true;
								break;
							}
						}else if(acPortInfo_select.getLagId()>0){
							PortLagInfo portLagInfo = new PortLagInfo();
							portLagInfo.setSiteId(ConstantUtil.siteId);
							portLagInfo.setId(acPortInfo_select.getLagId());
							List<PortLagInfo> portLagInfos = portLagService.selectLAGByCondition(portLagInfo);
//							if(portLagInfos != null && portLagInfos.size()>0 && portLagInfos.get(0).getVlanRelating()==1){
							if(portLagInfos != null && portLagInfos.size()>0){
								if(this.isRelevance(portLagInfos.get(0)) == 1){
									flag = true;
									break;
								}
							}
						}
						
					}else{
						if ("0".equals(UiUtil.getCodeById(acPortInfo_select.getPortModel()).getCodeValue())) {
							flag = false;
							break;
						}
					}
					
				}
			}else{
				flag = true;

			}

		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(portService);
			UiUtil.closeService_MB(acInfoService);
			UiUtil.closeService_MB(siteService);
			UiUtil.closeService_MB(portLagService);
		}
		return flag;
	}
	
	/**
	 * 判断该端口或者该LAG是否有关联规则
	 * 有/没有 = 1/0
	 */
	private int isRelevance(Object obj){
		int vlanRelevance = 0;//0/1 = 不关联/关联
		int eightIpRelevance = 0;//0/1 = 不关联/关联
 		int sourMacRelevance = 0;//0/1 = 不关联/关联
		int endMacRelevance = 0;//0/1 = 不关联/关联
		int sourIPRelevance = 0;//0/1 = 不关联/关联
		int endIPRelevance = 0;//0/1 = 不关联/关联
		if(obj instanceof PortInst){
			PortInst portInst = (PortInst) obj;
			try {
				vlanRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getVlanRelevance()).getCodeValue());
				eightIpRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getEightIpRelevance()).getCodeValue());
			    sourMacRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceMacRelevance()).getCodeValue());
			    endMacRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationMacRelevance()).getCodeValue());
			    sourIPRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getSourceIpRelevance()).getCodeValue());
			    endIPRelevance = Integer.parseInt(UiUtil.getCodeById(portInst.getPortAttr().getPortUniAttr().getDestinationIpRelevance()).getCodeValue());
			} catch (Exception e) {
				ExceptionManage.dispose(e, this.getClass());
			}
		}else if(obj instanceof PortLagInfo){
			PortLagInfo lag = (PortLagInfo) obj;
			vlanRelevance = lag.getVlanRelating();
		    eightIpRelevance = lag.getRelatingSet();
		    sourMacRelevance = lag.getFountainMAC();
		    endMacRelevance = lag.getAimMAC();
		    sourIPRelevance = lag.getFountainIP();
		    endIPRelevance = lag.getAimIP();
		}
		if(vlanRelevance == 1 || eightIpRelevance == 1 || sourMacRelevance == 1 ||
				endMacRelevance == 1 || sourIPRelevance == 1 || endIPRelevance == 1){
			return 1;
		}else{
			return 0;
		}
	}

	public void setLagId(String[] ports, int lagId) throws Exception {
		PortService_MB portService = null;
		try {
			portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			for (String portNumber : ports) {
				PortInst portInst = new PortInst();
				portInst.setSiteId(this.siteId);
				portInst.setNumber(Integer.parseInt(portNumber));
				portInst = portService.select(portInst).get(0);
				portInst.setLagId(lagId);
				portService.saveOrUpdate(portInst);
			}
		} catch (Exception e) {
			throw e;
		}finally{
			UiUtil.closeService_MB(portService);
		}
	}

	private void configSimpleTable(int siteId) {
		int actionSiteId;
		if(0==siteId){
			actionSiteId = ConstantUtil.siteId;
		}else{
			actionSiteId = siteId;
		}
		JComboBox cosCombox = new JComboBox();
		for (QosCosLevelEnum level : QosCosLevelEnum.values()) {
			cosCombox.addItem(level);
		}
		TableColumn cosColumn = dialog.getStep3().getSimpleTable().getColumn("COS");
		cosColumn.setCellEditor(new DefaultCellEditor(cosCombox));

		TableColumn seqColumn = dialog.getStep3().getSimpleTable().getColumn("SEQ");
		seqColumn.setCellEditor(new DefaultCellEditor(new JTextField()));

//		if (CodeConfigItem.getInstance().getWuhan() == 1) {
//			TableColumn cirColumn = dialog.getStep3().getSimpleTable().getColumn("CIR(Mbps)");
//			cirColumn.setCellEditor(new SpinnerNumberEditor("0", "1000", "1"));
//			
//			TableColumn eirColumn = dialog.getStep3().getSimpleTable().getColumn("EIR(Mbps)");
//			eirColumn.setCellEditor(new SpinnerNumberEditor("0", "1000", "1"));
//			
//			TableColumn pirColumn = dialog.getStep3().getSimpleTable().getColumn("PIR(Mbps)");
//			pirColumn.setCellEditor(new SpinnerNumberEditor("0", "100000", "1"));
//		}else{
			TableColumn cirColumn = dialog.getStep3().getSimpleTable().getColumn("CIR(kbps)");
			cirColumn.setCellEditor(new SpinnerNumberEditor("0", "1000000", "64"));
			
			TableColumn eirColumn = dialog.getStep3().getSimpleTable().getColumn("EIR(kbps)");
			eirColumn.setCellEditor(new SpinnerNumberEditor("0", "1000000", "64"));
			
			TableColumn pirColumn = dialog.getStep3().getSimpleTable().getColumn("PIR(kbps)");
			pirColumn.setCellEditor(new SpinnerNumberEditor("0", "100000000", "64"));
//		}
		
		

		TableColumn cbsColumn = dialog.getStep3().getSimpleTable().getColumn(ResourceUtil.srcStr(StringKeysLbl.LBL_CBS));
		cbsColumn.setCellEditor(new SpinnerNumberEditor("0", ConstantUtil.CBS_MAXVALUE+"", "1"));

		
//		eirColumn.setCellEditor(new SpinnerNumberEditor("0", "10000"));

		TableColumn ebsColumn = dialog.getStep3().getSimpleTable().getColumn(ResourceUtil.srcStr(StringKeysObj.EBS_BYTE));
		ebsColumn.setCellEditor(new SpinnerNumberEditor("0", ConstantUtil.CBS_MAXVALUE+"", "1"));
//		ebsColumn.setCellEditor(new SpinnerNumberEditor("-1", "10000"));
		SiteService_MB siteService = null;
		try {
			siteService=(SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (siteService.getManufacturer(actionSiteId) == EManufacturer.WUHAN.getValue()) {
				TableColumn pbsColumn = dialog.getStep3().getSimpleTable().getColumn(ResourceUtil.srcStr(StringKeysLbl.LBL_PBS));
				pbsColumn.setCellEditor(new SpinnerNumberEditor("0", ConstantUtil.CBS_MAXVALUE+"", "1"));
//		cbsColumn.setCellEditor(new SpinnerNumberEditor("-1", "524288"));
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
		}
		
//		pirColumn.setCellEditor(new SpinnerNumberEditor("0", "10000"));

	}

	// 修改ac是刷新界面
	public void refresh() {
		
		comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getMACLearnJCB(), dialog.getAcInfo().getMacAddressLearn() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep2().getTagReconfigJCB(), dialog.getAcInfo().getTagAction() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep2().getTagActionJCB(), dialog.getAcInfo().getExitRule() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getSplitCutJCB(), dialog.getAcInfo().getHorizontalDivision() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getVCEnableJCB(), dialog.getAcInfo().getManagerEnable() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep2().getCmbModel(), dialog.getAcInfo().getModel() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep2().getTpidJbox(), dialog.getAcInfo().getDownTpid() + "");
		// if(dialog.getAcInfo().getEEportType() == EEportType.ETH) {
		if (dialog.getAcInfo().getPortId() > 0) {
			comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getPortJCB(), String.valueOf(dialog.getAcInfo().getPortId()));
			PortInst portInst = (PortInst)((ControlKeyValue)dialog.getStep1().getPortJCB().getSelectedItem()).getObject();
			comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getModeJCB(), portInst.getPortAttr().getPortUniAttr().getVlanRelevance() + "");
		} else {
			comboBoxDataUtil.comboBoxSelect(dialog.getStep1().getPortJCB(), String.valueOf(dialog.getAcInfo().getLagId()));
		}
		dialog.getStep1().getPortJCB().setEnabled(false);
		/*
		 * } else if(dialog.getAcInfo().getEEportType() == EEportType.LAG ){ UiUtil.comboBoxSelect(dialog.getStep1().getPortJCB(), dialog.getAcInfo() .getPortId() + "lag")); }
		 */
		comboBoxDataUtil.comboBoxSelect(dialog.getStep3().getBufferTypeJCB(), dialog.getAcInfo().getBufType() + "");
		dialog.getStep2().getAddVlanIdJTF().setText(String.valueOf(dialog.getAcInfo().getVlanId()));
		dialog.getStep2().getAddVlanPriJTF().setText(String.valueOf(dialog.getAcInfo().getVlanpri()));
		dialog.getStep2().getMacCountField().setText(String.valueOf(dialog.getAcInfo().getMacCount()));
		dialog.getStep1().getNameJTF().setText(dialog.getAcInfo().getName());
		dialog.getStep1().getPtnSpinnerNumber().setEnabled(false);
		
		// 初始化简单流
		DefaultTableModel simpleTableModel = (DefaultTableModel) dialog.getStep3().getSimpleTable().getModel();
		simpleTableModel.getDataVector().clear();
		simpleTableModel.fireTableDataChanged();
		Object[] obj;
		if (dialog.getAcInfo().getSimpleQos() == null) {
			obj = new Object[] { 1, 0, QosCosLevelEnum.from(0).toString(), 0, 0, 0, 0, new Boolean(false), 0, 0 };
		} else {
			obj = new Object[] { 1, dialog.getAcInfo().getSimpleQos().getSeq(), QosCosLevelEnum.from(dialog.getAcInfo().getSimpleQos().getCos()).toString(), dialog.getAcInfo().getSimpleQos().getCir(), dialog.getAcInfo().getSimpleQos().getCbs(), dialog.getAcInfo().getSimpleQos().getEir(), dialog.getAcInfo().getSimpleQos().getEbs(), dialog.getAcInfo().getSimpleQos().getColorSence() == 0 ? Boolean.FALSE : Boolean.TRUE, dialog.getAcInfo().getSimpleQos().getPir(), dialog.getAcInfo().getSimpleQos().getPbs() };
		}
		simpleTableModel.addRow(obj);
		dialog.getStep3().getSimpleTable().setModel(simpleTableModel);
		// 初始化细分流
		detailTableDataBox();
	}

	// 修改ac是刷新界面
	public void refresh_cx() {
		comboBoxDataUtil.comboBoxSelect(dialog.getStep1_cx().getCmbMode(), dialog.getAcInfo().getPortModel() + "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep1_cx().getCmbManagerEnable(), dialog.getAcInfo().getManagerEnable()+ "");
		comboBoxDataUtil.comboBoxSelect(dialog.getStep2_cx().getCmbExitRule(), dialog.getAcInfo().getExitRule() + "");

		if (dialog.getAcInfo().getPortId() > 0) {
			comboBoxDataUtil.comboBoxSelect(dialog.getStep1_cx().getCmbPort(), String.valueOf(dialog.getAcInfo().getPortId()));
		} else {
			comboBoxDataUtil.comboBoxSelect(dialog.getStep1_cx().getCmbPort(), String.valueOf(dialog.getAcInfo().getLagId()));
		}
		dialog.getStep1_cx().getCmbPort().setEnabled(false);
		
		comboBoxDataUtil.comboBoxSelect(dialog.getStep3().getBufferTypeJCB(), dialog.getAcInfo().getBufType() + "");
		
		dialog.getStep1_cx().getTxtName().setText(String.valueOf(dialog.getAcInfo().getName()));
		dialog.getStep1_cx().getTxtOperatorVlanId().setText(String.valueOf(dialog.getAcInfo().getOperatorVlanId()));
		dialog.getStep1_cx().getTxtClientVlanId().setText(String.valueOf(dialog.getAcInfo().getClientVlanId()));
		dialog.getStep2_cx().getTxtVlanId().setText(String.valueOf(dialog.getAcInfo().getVlanId()));
		dialog.getStep2_cx().getTxtVlanCri().setText(String.valueOf(dialog.getAcInfo().getVlancri()));
		dialog.getStep2_cx().getTxtVlanPri().setText(String.valueOf(dialog.getAcInfo().getVlanpri()));

		// 初始化简单流
		DefaultTableModel simpleTableModel = (DefaultTableModel) dialog.getStep3().getSimpleTable().getModel();
		simpleTableModel.getDataVector().clear();
		simpleTableModel.fireTableDataChanged();
		Object[] obj;
		if (dialog.getAcInfo().getSimpleQos() == null) {
			obj = new Object[] { 1, 0, QosCosLevelEnum.from(0).toString(), 0, 0, 0, 0, new Boolean(false), 0, 0 };
		} else {
			obj = new Object[] { 1, dialog.getAcInfo().getSimpleQos().getSeq(), QosCosLevelEnum.from(dialog.getAcInfo().getSimpleQos().getCos()).toString(), dialog.getAcInfo().getSimpleQos().getCir(), dialog.getAcInfo().getSimpleQos().getCbs(), dialog.getAcInfo().getSimpleQos().getEir(), dialog.getAcInfo().getSimpleQos().getEbs(), dialog.getAcInfo().getSimpleQos().getColorSence() == 0 ? Boolean.FALSE : Boolean.TRUE, dialog.getAcInfo().getSimpleQos().getPir(), dialog.getAcInfo().getSimpleQos().getPbs() };
		}
		simpleTableModel.addRow(obj);
		dialog.getStep3().getSimpleTable().setModel(simpleTableModel);
		// 初始化细分流
		detailTableDataBox();
	}
	public String getComboBoxSelectId(JComboBox jComboBox, Integer codeValue) {
		for (int i = 0; i < jComboBox.getItemCount(); i++) {
			if (((Code) ((ControlKeyValue) jComboBox.getItemAt(i)).getObject()).getCodeValue().equals(codeValue.toString())) {
				return ((ControlKeyValue) jComboBox.getItemAt(i)).getId();
			}
		}
		return null;
	}
	/**
	 * 文本框验证是否为数字
	 * 
	 * @return
	 */
	private boolean checkingNumber(String txtData) throws Exception{
		if (txtData.trim().length() > 0) {
			Pattern pattern = Pattern.compile("^[0-9_]+$");
			Matcher matcher = pattern.matcher(txtData.trim());

			return matcher.find();
		} else {
			return true;
		}
	}
}
