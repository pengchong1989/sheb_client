package com.nms.ui.ptn.systemconfig.dialog.qos.action;

import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.nms.db.bean.path.Segment;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Lsp;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.bean.ptn.qos.QosInfo;
import com.nms.db.bean.ptn.qos.QosQueue;
import com.nms.db.bean.ptn.qos.QosTemplateInfo;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EPwType;
import com.nms.db.enums.EQosDirection;
import com.nms.db.enums.EServiceType;
import com.nms.db.enums.QosCosLevelEnum;
import com.nms.db.enums.QosTemplateTypeEnum;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.path.tunnel.TunnelService_MB;
import com.nms.model.ptn.qos.QosQueueService_MB;
import com.nms.model.ptn.qos.QosTemplateService_MB;
import com.nms.model.util.CodeConfigItem;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.keys.StringKeysObj;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.basicinfo.dialog.segment.AddSegment;
import com.nms.ui.ptn.business.dialog.pwpath.AddPDialog;
import com.nms.ui.ptn.business.dialog.tunnel.AddTunnelPathDialog;
import com.nms.ui.ptn.business.pw.PwBusinessPanel;
import com.nms.ui.ptn.business.tunnel.TunnelBusinessPanel;
import com.nms.ui.ptn.ne.pw.view.PwAddDialog;
import com.nms.ui.ptn.ne.pw.view.PwPanel;
import com.nms.ui.ptn.ne.tunnel.view.TunnelAddDialog;
import com.nms.ui.ptn.ne.tunnel.view.TunnelPanel;
import com.nms.ui.ptn.systemconfig.dialog.qos.ComparableSort;
import com.nms.ui.ptn.systemconfig.dialog.qos.controller.QosConfigController;
import com.nms.ui.ptn.systemconfig.dialog.qos.dialog.QosCommonConfig;
import com.nms.ui.ptn.systemconfig.dialog.qos.dialog.QosConfigDialog;
import com.nms.ui.ptn.systemconfig.dialog.qos.dialog.QosSectionConfigDialog;

public class QosConfigAction {
	@SuppressWarnings("rawtypes")
	Vector datas = null;

	@SuppressWarnings("rawtypes")
	Vector sectionADatas = null;

	@SuppressWarnings("rawtypes")
	Vector sectionZDatas = null;
	List<Integer> idList = new ArrayList<Integer>();
	int payload = 4;
	int cosNum = 3552;
	
	public void setTableDatas(QosCommonConfig qosConfigDialog,QosConfigController qosController) {
		qosConfigDialog.getQosTableModel().getDataVector().clear();
		qosConfigDialog.getQosTableModel().fireTableDataChanged();
		Object data[] = new Object[] {};
		int rowCount = 0;
		String direction = "";
		datas = new Vector();
		if (qosConfigDialog.getQosTypeComboBox().getSelectedItem().toString().equals(QosTemplateTypeEnum.LLSP.toString())) {
			rowCount = 0;
			for (int i = 0; i < 2; i++) {
				if (i == 0) {
					direction = EQosDirection.FORWARD.toString();
				} else {
					direction = EQosDirection.BACKWARD.toString();
				}
				if (qosController.getObjType().equals(EServiceType.PW.toString()) && qosController.getChoosePwType() != EPwType.ETH) {
					data = new Object[] { ++rowCount, "EF", direction, 2448, 1, 0, 1, 2448 };
					payload = ((PwInfo) qosController.getObj()).getPayload();
					try {
						if (0 != payload) {
							payload = Integer.parseInt(UiUtil.getCodeById(payload).getCodeValue());
							if(0 == this.payload)
								cosNum = 3552;
							else if(1 == this.payload)
								cosNum = 2816;
							else if(2 == this.payload)
								cosNum = 2448;
							else if(3 == this.payload)
								cosNum = 2264;
							data = new Object[] { ++rowCount, "EF", direction, cosNum, 1, 0, 1, cosNum};
						}
					} catch (Exception e) {
						ExceptionManage.dispose(e, this.getClass());
					}
					setNewTableModel(qosConfigDialog,qosController);
				} else {

					data = new Object[] { ++rowCount, "EF", direction, 0, 1, 0, 1, 0 };
				}
				qosConfigDialog.getQosTableModel().addRow(data);
			}
		} else if (qosConfigDialog.getQosTypeComboBox().getSelectedItem().toString().equals(QosTemplateTypeEnum.ELSP.toString())) {
			rowCount = 0;
			for (QosCosLevelEnum level : QosCosLevelEnum.values()) {
				for (int i = 0; i < 2; i++) {
					if (i == 0) {
						direction = EQosDirection.FORWARD.toString();
					} else {
						direction = EQosDirection.BACKWARD.toString();
					}
					data = new Object[] { ++rowCount, level.toString(), direction, 0, 1, 0, 1, 0 };
					qosConfigDialog.getQosTableModel().addRow(data);
				}
			}
		}
		setDatas(qosConfigDialog.getQosTableModel().getDataVector());
	}

	/*
	 * 加载段的qos
	 */
	@SuppressWarnings("rawtypes")
	public void setSectionTableDatas(QosCommonConfig qosConfigDialog) {
		qosConfigDialog.getSectionAQosTableModel().getDataVector().clear();
		qosConfigDialog.getSectionZQosTableModel().getDataVector().clear();
		qosConfigDialog.getSectionAQosTableModel().fireTableDataChanged();
		qosConfigDialog.getSectionZQosTableModel().fireTableDataChanged();
		Object data[] = new Object[] {};
		int rowCount = 0;
		sectionADatas = new Vector();
		sectionZDatas = new Vector();
		if (CodeConfigItem.getInstance().getWuhan() == 1) {
			for (QosCosLevelEnum level : QosCosLevelEnum.values()) {
				data = new Object[] { ++rowCount, level.toString(), 0, 16, 50, 90, 100, 64, 96, 100, Boolean.FALSE, ResourceUtil.srcStr(StringKeysObj.QOS_UNLIMITED) };
				qosConfigDialog.getSectionAQosTableModel().addRow(data);
				qosConfigDialog.getSectionZQosTableModel().addRow(data);
			}
		} else {
			for (QosCosLevelEnum level : QosCosLevelEnum.values()) {
				data = new Object[] { ++rowCount, level.toString(), 0, 16, 96, 128, 100, 64, 96, 100, Boolean.TRUE, ResourceUtil.srcStr(StringKeysObj.QOS_UNLIMITED) };
				qosConfigDialog.getSectionAQosTableModel().addRow(data);
				qosConfigDialog.getSectionZQosTableModel().addRow(data);
			}
		}

		setSectionADatas(qosConfigDialog.getSectionAQosTableModel().getDataVector());
		setSectionZDatas(qosConfigDialog.getSectionZQosTableModel().getDataVector());
	}

	/*
	 * 加载模板到界面
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initNameList(QosCommonConfig qosConfigDialog ) {
		List<String> templateNameList = null;
		QosTemplateService_MB templateService = null;
		try {
			templateService = (QosTemplateService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosTemplate);
			templateNameList = new ArrayList(templateService.selectAll().keySet());
			if (templateNameList != null && templateNameList.size() != 0) {
				qosConfigDialog.getNameListComboBoxModel().addElement(new ControlKeyValue("", ""));
				for (String name : templateNameList) {
					ControlKeyValue key = new ControlKeyValue(name, name);
					qosConfigDialog.getNameListComboBoxModel().addElement(key);
				}
				qosConfigDialog.getNameList().setModel(qosConfigDialog.getNameListComboBoxModel());
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(templateService);
		}
	}
	/**
	 * 打开qos配置界面,需要判断该对象是否已经配置了qos
	 * @param controller
	 * @param panelOrDialog 
	 *		 新建或者修改时 操作qos 传入 新建或者修改的对话框对象，右键修改qos 传入 主界面panel对象，做刷新只用
	 * @throws Exception
	 */
	public void openQosConfigAction(QosConfigController controller,Object panelOrDialog) throws Exception {
		QosCommonConfig qosConfigDialog = null;
		if (controller.getObjType().equals(EServiceType.SECTION.toString())) {
			qosConfigDialog = new QosSectionConfigDialog(true, "");
			controller = new QosConfigController(qosConfigDialog, controller, controller.isNetwork(),panelOrDialog);
			checkQosHasConfig(qosConfigDialog, controller.getObj(),panelOrDialog);
			qosConfigDialog.setSize(900, 550);
		} else {
			qosConfigDialog = new QosConfigDialog(true, "");
			controller = new QosConfigController(qosConfigDialog, controller, controller.isNetwork(),panelOrDialog);
			checkQosHasConfig(qosConfigDialog, controller.getObj(),panelOrDialog);
			qosConfigDialog.setSize(550, 450);
		}
		qosConfigDialog.setLocation(UiUtil.getWindowWidth(qosConfigDialog.getWidth()), UiUtil.getWindowHeight(qosConfigDialog.getHeight()));
		qosConfigDialog.setVisible(true);

	}
	/**
	 *  判断路径上qos是否已经配置
	 * @param qosConfigDialog
	 * @param obj
	 * @param panelOrDialog
	 * 		 新建或者修改时 操作qos 传入 新建或者修改的对话框对象，右键修改qos 传入 主界面panel对象，做刷新只用
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void checkQosHasConfig(QosCommonConfig qosConfigDialog, Object obj,Object panelOrDialog) throws Exception {
		
		QosQueue aqosQueue = null;
		QosQueue zqosQueue = null;
		Map<Integer, List<QosQueue>> aqosMap = null;
		Map<Integer, List<QosQueue>> zqosMap = null;
		List<QosQueue> aqosList = null;
		List<QosQueue> zqosList = null;
		QosQueueService_MB qosQueueService = null;
		try {
			qosQueueService = (QosQueueService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosQueue);
			Object data[] = new Object[] {};
			if (obj instanceof Segment) {
				AddSegment segmentDialog=(AddSegment) panelOrDialog;
				Segment seg = (Segment) obj;
				aqosMap = new HashMap<Integer, List<QosQueue>>();
				aqosQueue = new QosQueue();
				aqosQueue.setObjId(seg.getAPORTID());
				aqosQueue.setObjType("SECTION");
				aqosMap = qosQueueService.queryByPortId(aqosQueue);
				ComparableSort sort = new ComparableSort();
				if (aqosMap.size() != 0) {
					for (int siteId : aqosMap.keySet()) {
						aqosList = new ArrayList<QosQueue>();
						if (siteId == seg.getASITEID()) {
							aqosList = aqosMap.get(siteId);
							aqosList = (List<QosQueue>) sort.compare(aqosList);
							qosConfigDialog.getSectionAQosQueueComboBox().setSelectedItem(aqosList.get(0).getQueueType());
							qosConfigDialog.getSectionAQosTableModel().getDataVector().clear();
							qosConfigDialog.getSectionAQosTableModel().fireTableDataChanged();
							for (int i = 0; i < aqosList.size(); i++) {
								int j = i + 1;
								aqosQueue = aqosList.get(i);
								data = new Object[] { j, QosCosLevelEnum.from(aqosQueue.getCos()), aqosQueue.getCir(), aqosQueue.getWeight(), aqosQueue.getGreenLowThresh(), aqosQueue.getGreenHighThresh(), aqosQueue.getGreenProbability(), aqosQueue.getYellowLowThresh(), aqosQueue.getYellowHighThresh(), aqosQueue.getYellowProbability(), aqosQueue.isWredEnable(), aqosQueue.getMostBandwidth() };
								segmentDialog.getaCosMap().put(aqosQueue.getCos(), aqosQueue.getId());
								qosConfigDialog.getSectionAQosTableModel().addRow(data);
							}
						}
					}
				}
				
				zqosMap = new HashMap<Integer, List<QosQueue>>();
				zqosQueue = new QosQueue();
				zqosQueue.setObjId(seg.getZPORTID());
				zqosQueue.setObjType("SECTION");
				zqosMap = qosQueueService.queryByPortId(zqosQueue);
				if (zqosMap.size() != 0) {
					for (int siteId : zqosMap.keySet()) {
						zqosList = new ArrayList<QosQueue>();
						if (siteId == seg.getZSITEID()) {
							zqosList = zqosMap.get(siteId);
							zqosList = (List<QosQueue>) sort.compare(zqosList);
							qosConfigDialog.getSectionZQosQueueComboBox().setSelectedItem(zqosList.get(0).getQueueType());
							qosConfigDialog.getSectionZQosTableModel().getDataVector().clear();
							qosConfigDialog.getSectionZQosTableModel().fireTableDataChanged();
							for (int i = 0; i < zqosList.size(); i++) {
								int j = i + 1;
								zqosQueue = zqosList.get(i);
								data = new Object[] { j, QosCosLevelEnum.from(zqosQueue.getCos()), zqosQueue.getCir(), zqosQueue.getWeight(), zqosQueue.getGreenLowThresh(), zqosQueue.getGreenHighThresh(), zqosQueue.getGreenProbability(), zqosQueue.getYellowLowThresh(), zqosQueue.getYellowHighThresh(), zqosQueue.getYellowProbability(), zqosQueue.isWredEnable(), zqosQueue.getMostBandwidth() };
								segmentDialog.getzCosMap().put(zqosQueue.getCos(), zqosQueue.getId());
								qosConfigDialog.getSectionZQosTableModel().addRow(data);
							}
						}
					}
				}
			} else if (obj instanceof Tunnel) { // 如果选中的是tunnel 判断tunnel是否是修改 如果是 加载tunnel的qos数据
				
				Tunnel tunnel = (Tunnel) obj;
				if (tunnel.getTunnelId() > 0) {
					qosConfigDialog.getNameList().setEnabled(false);
					qosConfigDialog.getQosTypeComboBox().setEnabled(false);
					this.tableData(tunnel.getQosList(), qosConfigDialog);
				}
				
			} else if (obj instanceof PwInfo) { // 如果选中的是pw 判断pw是否是修改 如果是 加载pw的qos数据
				PwInfo pwInfo = (PwInfo) obj;
				if (pwInfo.getPwId() > 0) {
					qosConfigDialog.getNameList().setEnabled(false);
					qosConfigDialog.getQosTypeComboBox().setEnabled(false);
					this.tableData(pwInfo.getQosList(), qosConfigDialog);
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			UiUtil.closeService_MB(qosQueueService);
		}
	}

	/**
	 * 绑定qostable数据
	 * 
	 * @param qosInfoList
	 * @param qosConfigDialog
	 */
	private void tableData(List<QosInfo> qosInfoList, QosCommonConfig qosConfigDialog) {
		if (qosInfoList.size() != 0) {
			qosConfigDialog.getQosTypeComboBox().setSelectedItem(qosInfoList.get(0).getQosType());
			qosConfigDialog.getQosTableModel().getDataVector().clear();
			qosConfigDialog.getQosTableModel().fireTableDataChanged();
			int i = 0;
			for (QosInfo qos : qosInfoList) {
				Object[] data = new Object[] { ++i, QosCosLevelEnum.from(qos.getCos()), qos.getDirection().equals("1") ? ResourceUtil.srcStr(StringKeysObj.STRING_FORWARD) : ResourceUtil.srcStr(StringKeysObj.STRING_BACKWARD), qos.getCir(), qos.getCbs(), qos.getEir(), qos.getEbs(), qos.getPir() };
				qosConfigDialog.getQosTableModel().addRow(data);
			}
		}
	}

	/*
	 * 根据选择模板,显示表中数据
	 */
	public void freshQosTable(QosCommonConfig qosConfigDialog, ControlKeyValue controlKeyValue,QosConfigController qosController) {
		List<QosTemplateInfo> infoList = null;
		qosConfigDialog.getQosTableModel().getDataVector().clear();
		qosConfigDialog.getQosTableModel().fireTableDataChanged();
		QosTemplateService_MB templateService = null;
		try {
			templateService = (QosTemplateService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosTemplate);
			if (!"".equals(controlKeyValue.getName())) {
				infoList = new ArrayList<QosTemplateInfo>();
				infoList = templateService.queryByCondition(controlKeyValue.getName());
				if (infoList!=null && infoList.size() != 0) {
					qosConfigDialog.getQosTypeComboBox().setSelectedItem(infoList.get(0).getQosType());
					qosConfigDialog.getQosTableModel().getDataVector().clear();
					qosConfigDialog.getQosTableModel().fireTableDataChanged();
					int i = 0;
					for (QosTemplateInfo qos : infoList) {
						Object[] data = new Object[] { ++i, QosCosLevelEnum.from(qos.getCos()), qos.getDirection(), qos.getCir(), qos.getCbs(), qos.getEir(), qos.getEbs(), qos.getPir() };
						qosConfigDialog.getQosTableModel().addRow(data);
					}
				}
			} else {
				setTableDatas(qosConfigDialog,qosController);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(templateService);
		}
	}

	/**
	 * 给对象配置qos
	 * @param qosCommonConfig
	 * @param objType
	 * @param obj
	 * @param isNetwork
	 * @param panelOrDialog
	 * 	新建或者修改时 操作qos 传入 新建或者修改的对话框对象(既：有对话框的 保存按钮事件--object 不用再判断是对话框还是panel)
	 * @throws Exception
	 */
	public void addQosInfo(QosCommonConfig qosCommonConfig, String objType, Object obj, boolean isNetwork,Object panelOrDialog) throws Exception {
		if (objType.equals("SECTION")) {
			defaultSaveToQosQueue(qosCommonConfig, objType, obj, isNetwork,panelOrDialog);
		} else {

			Tunnel tunnel = null;
			PwInfo pwInfo = null;
			boolean flag = true;
			try {
				if (obj instanceof Tunnel) {
					tunnel = (Tunnel) obj;
					if (tunnel.getTunnelId() > 0) {
						flag = false;
					}
				} else if (obj instanceof PwInfo) {
					pwInfo = (PwInfo) obj;
					if (pwInfo.getPwId() > 0) {
						flag = false;
					}
				}
				if (flag) {
					defaultSaveToQosInfo(qosCommonConfig, objType, obj, isNetwork,panelOrDialog);
					DialogBoxUtil.succeedDialog(qosCommonConfig, ResourceUtil.srcStr(StringKeysTip.TIP_SAVE_SUCCEED));
				} else {
					this.updateQos(qosCommonConfig, objType, obj, isNetwork, panelOrDialog);
				}

			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	 * 修改qos 下发设备
	 * 
	 * @author KK
	 * @param qosCommonConfig
	 *            qos配置界面 用此参数获取table
	 * @param objType
	 *            类型 pw或是tunnel
	 * @param obj
	 *            类型对应的bean pwinfo或是tunnel
	 * @param isNetwork
	 *            是否为网络层。 用此参数来决定修改后刷新哪个界面。
	 *@param panelOrDialog
	 *  		新建或者修改时 操作qos 传入 新建或者修改的对话框对象，右键修改qos 传入 主界面panel对象，做刷新只用
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private void updateQos(QosCommonConfig qosCommonConfig, String objType, Object obj, boolean isNetwork,Object panelOrDialog) throws Exception {
		List<QosInfo> qosInfoList = null;
		QosInfo info = null;
		DefaultTableModel tableModel = null;
		Vector dataVector = null;
		Iterator dataIterator = null;
		Vector vector = null;
		DispatchUtil qosDispatch = null;
		String resultStr = null;
		try {
			List<QosInfo> qosListBefore = null;
			if(EServiceType.PW.toString().equals(objType)){
				qosListBefore = ((PwInfo)obj).getQosList();
			}
			// 获取界面中qos的集合
			tableModel = qosCommonConfig.getQosTableModel();
			dataVector = tableModel.getDataVector();
			qosInfoList = new ArrayList<QosInfo>();
			dataIterator = dataVector.iterator();
			while (dataIterator.hasNext()) {
				vector = (Vector) dataIterator.next();
				info = new QosInfo();
				// info.setObjType(objType);
				info.setQosType((String) qosCommonConfig.getQosTypeComboBox().getSelectedItem());
				info.setCos(QosCosLevelEnum.from(vector.get(1).toString()));
				info.setDirection(ResourceUtil.srcStr(StringKeysObj.STRING_FORWARD).equals(vector.get(2).toString()) ? "1" : "2");
				info.setCir(new Integer(vector.get(3).toString()));
				info.setCbs(new Integer(vector.get(4).toString()));
				info.setEir(new Integer(vector.get(5).toString()));
				info.setEbs(new Integer(vector.get(6).toString()));
				info.setPir(new Integer(vector.get(7).toString()));
				qosInfoList.add(info);
			}
			
			//验证qos带宽是否充足
			if (!this.checkingUpdateQos(qosInfoList, obj)) {
				if (EServiceType.TUNNEL.toString().equals(objType)) {
					DialogBoxUtil.errorDialog(null, ResourceUtil.srcStr(StringKeysTip.TIP_QOSISNOTENOUGH));
				}else if(EServiceType.PW.toString().equals(objType)){
					DialogBoxUtil.errorDialog(null, ResourceUtil.srcStr(StringKeysTip.TIP_QOSISNOTENOUGH));
				}else{
					DialogBoxUtil.errorDialog(null, ResourceUtil.srcStr(StringKeysTip.TIP_QOSISNOTENOUGH));
				}
				return;
			}

			// 下发qos
			qosDispatch = new DispatchUtil(RmiKeys.RMI_QOS);
			resultStr = qosDispatch.excutionUpdate(qosInfoList, obj);
			DialogBoxUtil.succeedDialog(null, resultStr);

			// 下发后，调用界面的刷新列表方法
			if (EServiceType.TUNNEL.toString().equals(objType)) {
				if (isNetwork) {
					if(panelOrDialog instanceof TunnelBusinessPanel){
						TunnelBusinessPanel panel=(TunnelBusinessPanel) panelOrDialog;
						panel.getController().refresh();
					}
					
				} else {
					if(panelOrDialog instanceof TunnelPanel){
						TunnelPanel panel=(TunnelPanel) panelOrDialog;
						panel.getController().refresh();
					}
					
				}
			} else if (EServiceType.PW.toString().equals(objType)) {
				
				if (isNetwork) {
					if(panelOrDialog instanceof PwBusinessPanel){
						PwBusinessPanel panel=(PwBusinessPanel) panelOrDialog;
						panel.getController().refresh();
					}
					
				} else {
					if(panelOrDialog instanceof PwPanel){
						PwPanel panel =(PwPanel) panelOrDialog;
						panel.getController().refresh();
					}
					
				}
			}
			if (EServiceType.TUNNEL.toString().equals(objType)) {
				Tunnel tunnelBefore = new Tunnel();
				tunnelBefore.setQosList(((Tunnel)obj).getQosList());
				((Tunnel)obj).setQosList(qosInfoList);
				AddOperateLog.insertOperLog(null, EOperationLogType.TUNNELQOS.getValue(), resultStr, 
						tunnelBefore, ((Tunnel)obj), -1, ((Tunnel)obj).getTunnelName(), "qosUpdate");
			}else if(EServiceType.PW.toString().equals(objType)){
				PwInfo pwBefore = new PwInfo();
				pwBefore.setQosList(qosListBefore);
				((PwInfo)obj).setQosList(qosInfoList);
				AddOperateLog.insertOperLog(null, EOperationLogType.PWQOS.getValue(), resultStr, 
						pwBefore, ((PwInfo)obj), -1, ((PwInfo)obj).getPwName(), "qosUpdate");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			qosInfoList = null;
			info = null;
			tableModel = null;
			dataVector = null;
			dataIterator = null;
			vector = null;
			qosDispatch = null;
			resultStr = null;
		}

	}

	/**
	 * 验证修改的业务的上层路径的qos是否够用
	 * 
	 * @return true 可以修改 false qos不足。 不可修改
	 * @throws Exception
	 */
	private boolean checkingUpdateQos(List<QosInfo> qosInfoList, Object object) throws Exception {
		TunnelService_MB tunnelServiceMB = null;
		Tunnel tunnel = null;
		List<Lsp> lspList = null;
		int createNum = 0;
		PwInfo pwInfo=null;
		PwInfoService_MB pwInfoService=null;
		boolean result=true;
		
		try {
			//验证tunnel的qos
			if (object instanceof Tunnel) {
				tunnel = (Tunnel) object;
				tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
				lspList = tunnelServiceMB.getAllLsp(tunnel);
				createNum = tunnelServiceMB.getMinQosNum(lspList, qosInfoList,tunnel.getQosList());
				
				//如果可创建数量大于0 说明在端口侧带宽充足，此时验证此tunnel下的pw带宽是否充足
				if(createNum!=0){
					result = tunnelServiceMB.checkingQosInPw(qosInfoList, tunnel);
				}else{
					result=false;
				}
			}else if(object instanceof PwInfo){
				pwInfo=(PwInfo) object;
				pwInfoService=(PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
				result=pwInfoService.checkingQos(pwInfo, qosInfoList,pwInfo.getQosList());
				
				//如果pw的tunnel侧验证成功。 开始验证eline侧
				if(result){
					result=pwInfoService.checkQosPwAndAc(pwInfo, qosInfoList);
				}
				if(result){
					pwInfo.setQosList(qosInfoList);
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}finally{
			UiUtil.closeService_MB(tunnelServiceMB);
			UiUtil.closeService_MB(pwInfoService);
			tunnel = null;
			lspList = null;
			pwInfo=null;
		}

		return result;
	}

	/**
	 *  将段的qos信息保存
	 * @param qosCommonConfig
	 * @param objType
	 * @param obj
	 * @param isNetwork
	 * @param panelOrDialog
	 * 			新建或者修改时 操作qos 传入 新建或者修改的对话框对象，右键修改qos 传入 主界面panel对象，做刷新只用
	 */
	@SuppressWarnings("rawtypes")
	private void defaultSaveToQosQueue(QosCommonConfig qosCommonConfig, String objType, Object obj, boolean isNetwork,Object panelOrDialog) {
		Map<Integer, List<QosQueue>> qosQueueMap = new HashMap<Integer, List<QosQueue>>();
		List<QosQueue> qosQueueList = null;
		QosQueue queue = null;
		DefaultTableModel sectionATableModel = qosCommonConfig.getSectionAQosTableModel();
		DefaultTableModel sectionZTableModel = qosCommonConfig.getSectionZQosTableModel();
		Segment segment = (Segment) obj;
		Vector aDataVector = sectionATableModel.getDataVector();
		Vector zDataVector = sectionZTableModel.getDataVector();
		Iterator dataIterator = null;
		// A端
		qosQueueList = new ArrayList<QosQueue>();
		dataIterator = aDataVector.iterator();
		AddSegment segmentDialog=(AddSegment) panelOrDialog;
		while (dataIterator.hasNext()) {
			Vector vector = (Vector) dataIterator.next();
			queue = new QosQueue();
			queue.setSiteId(segment.getASITEID());
			queue.setObjType(objType);
			queue.setQueueType(qosCommonConfig.getSectionAQosQueueComboBox().getSelectedItem().toString());
			queue.setCos(QosCosLevelEnum.from(vector.get(1).toString()));
			if (segment.getId() != 0) {
				queue.setServiceId(segment.getId());
			}
			if (segmentDialog.getaCosMap() != null && !segmentDialog.getaCosMap().isEmpty()) {
				queue.setId(segmentDialog.getaCosMap().get(queue.getCos()));
			}
			queue.setCir(new Integer(vector.get(2).toString()));
			queue.setWeight(new Integer(vector.get(3).toString()));
			queue.setGreenLowThresh(new Integer(vector.get(4).toString()));
			queue.setGreenHighThresh(new Integer(vector.get(5).toString()));
			queue.setGreenProbability(new Integer(vector.get(6).toString()));
			queue.setYellowLowThresh(new Integer(vector.get(7).toString()));
			queue.setYellowHighThresh(new Integer(vector.get(8).toString()));
			queue.setYellowProbability(new Integer(vector.get(9).toString()));
			queue.setWredEnable(vector.get(10).toString().equals("true") ? Boolean.TRUE : Boolean.FALSE);
			queue.setMostBandwidth(ResourceUtil.srcStr(StringKeysObj.QOS_UNLIMITED));
			queue.setObjId(segment.getAPORTID());
			qosQueueList.add(queue);
		}
		qosQueueMap.put(segment.getASITEID(), qosQueueList);
		// Z端
		qosQueueList = new ArrayList<QosQueue>();
		dataIterator = zDataVector.iterator();
		while (dataIterator.hasNext()) {
			Vector vector = (Vector) dataIterator.next();
			queue = new QosQueue();
			queue.setSiteId(segment.getZSITEID());
			queue.setObjType(objType);
			queue.setQueueType(qosCommonConfig.getSectionZQosQueueComboBox().getSelectedItem().toString());
			queue.setCos(QosCosLevelEnum.from(vector.get(1).toString()));
			if (segment.getId() != 0) {
				queue.setServiceId(segment.getId());
			}
			if (segmentDialog.getzCosMap() != null && !segmentDialog.getzCosMap().isEmpty()) {
				queue.setId(segmentDialog.getzCosMap().get(queue.getCos()));
			}
			queue.setCir(new Integer(vector.get(2).toString()));
			queue.setWeight(new Integer(vector.get(3).toString()));
			queue.setGreenLowThresh(new Integer(vector.get(4).toString()));
			queue.setGreenHighThresh(new Integer(vector.get(5).toString()));
			queue.setGreenProbability(new Integer(vector.get(6).toString()));
			queue.setYellowLowThresh(new Integer(vector.get(7).toString()));
			queue.setYellowHighThresh(new Integer(vector.get(8).toString()));
			queue.setYellowProbability(new Integer(vector.get(9).toString()));
			queue.setWredEnable(vector.get(10).toString().equals("true") ? Boolean.TRUE : Boolean.FALSE);
			queue.setMostBandwidth(ResourceUtil.srcStr(StringKeysObj.QOS_UNLIMITED));
			queue.setObjId(segment.getZPORTID());
			qosQueueList.add(queue);
		}
		qosQueueMap.put(segment.getZSITEID(), qosQueueList);
		segmentDialog.setQosMap(qosQueueMap);
		// A端
		qosQueueList = new ArrayList<QosQueue>();
		dataIterator = aDataVector.iterator();
		
		qosQueueMap = null;
		qosQueueList = null;
		queue = null;
		sectionATableModel = null;
		sectionZTableModel = null;
		segment = null;
		aDataVector = null;
		zDataVector = null;
		dataIterator = null;
	}

	/*
	 * 若选择了模板,则将模板的qos配置复制给对象
	 */
//	private void fromTemplateSavetoQosInfo(QosCommonConfig qosCommonConfig, String objType, Object obj, boolean isNetwork) throws Exception {
//		String name = ((ControlKeyValue) qosCommonConfig.getNameList().getSelectedItem()).getName();
//		String qosType = qosCommonConfig.getQosTypeComboBox().getSelectedItem().toString();
//
//		List<QosTemplateInfo> infoList = new ArrayList<QosTemplateInfo>();
//		List<QosInfo> qosInfoList = new ArrayList<QosInfo>();
//		infoList = templateService.queryByCondition(name);
//		for (QosTemplateInfo templateInfo : infoList) {
//			QosInfo info = new QosInfo();
//			// info.setObjType(objType);
//			info.setQosType(qosType);
//			info.setCos(templateInfo.getCos());
//			info.setDirection(templateInfo.getDirection());
//			info.setCir(templateInfo.getCir());
//			info.setCbs(templateInfo.getCbs());
//			info.setEir(templateInfo.getEir());
//			info.setEbs(templateInfo.getEbs());
//			info.setPir(templateInfo.getPir());
//			qosInfoList.add(info);
//		}
//		if (EServiceType.PW.toString().equals(objType)) {
//			if (isNetwork) {
//				if (qosInfoList != null)
//					AddPDialog.getDialog().setQosList(qosInfoList);
//			} else {
//				if (qosInfoList != null)
//					PwAddDialog.getPwAddDialog().setQosList(qosInfoList);
//			}
//
//		} else if (EServiceType.TUNNEL.toString().equals(objType)) {
//			if (isNetwork) {
//				if (qosInfoList != null)
//					AddTunnelPathDialog.getDialog().setQosList(qosInfoList);
//			} else {
//				if (qosInfoList != null)
//					TunnelAddDialog.getDialog().setQosList(qosInfoList);
//			}
//		}
//	}
	/**
	 * 若没有选择模板,则按默认配置保存
	 * @param qosCommonConfig
	 * @param objType
	 * @param obj
	 * @param isNetwork
	 * @param dialog
	 * 		新建或者修改时 操作qos 传入 新建或者修改的对话框对象，右键修改qos 传入 主界面panel对象，做刷新只用
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	private void defaultSaveToQosInfo(QosCommonConfig qosCommonConfig, String objType, Object obj, boolean isNetwork,Object dialog) throws Exception {
		List<QosInfo> qosInfoList = null;
		QosInfo info = null;
		DefaultTableModel tableModel = qosCommonConfig.getQosTableModel();
		Vector dataVector = tableModel.getDataVector();
		Iterator dataIterator = null;
		qosInfoList = new ArrayList<QosInfo>();
		dataIterator = dataVector.iterator();
		while (dataIterator.hasNext()) {
			Vector vector = (Vector) dataIterator.next();
			info = new QosInfo();
			// info.setObjType(objType);
			info.setQosType((String) qosCommonConfig.getQosTypeComboBox().getSelectedItem());
			info.setCos(QosCosLevelEnum.from(vector.get(1).toString()));
			if (vector.get(2).toString().equals(EQosDirection.FORWARD.toString())) {
				info.setDirection(EQosDirection.FORWARD.getValue() + "");
			} else {
				info.setDirection(EQosDirection.BACKWARD.getValue() + "");
			}
			info.setCir(new Integer(vector.get(3).toString()));
			info.setCbs(new Integer(vector.get(4).toString()));
			info.setEir(new Integer(vector.get(5).toString()));
			info.setEbs(new Integer(vector.get(6).toString()));
			info.setPir(new Integer(vector.get(7).toString()));
			qosInfoList.add(info);
		}
		if (EServiceType.PW.toString().equals(objType)) {			
			if (isNetwork) {				
				if (qosInfoList != null){//将dialog转为网络层   AddPDialog
					AddPDialog pwDialog=(AddPDialog) dialog;
					pwDialog.setQosList(qosInfoList);
				}					
			} else {
				if (qosInfoList != null){
					PwAddDialog pwAddDialog=(PwAddDialog)dialog;
					pwAddDialog.setQosList(qosInfoList);
				}				
			}
		} else if (EServiceType.TUNNEL.toString().equals(objType)) {			
			if (isNetwork) {
				AddTunnelPathDialog tunnelDialog=(AddTunnelPathDialog) dialog;
				if (qosInfoList != null)
					tunnelDialog.setQosList(qosInfoList);
			} else {
				TunnelAddDialog tunnelAddDialog=(TunnelAddDialog) dialog;
				if (qosInfoList != null)
					tunnelAddDialog.setQosList(qosInfoList);
			}
		}
		 qosInfoList = null;
		 info = null;
		 tableModel =null;
		 dataVector =null;
		 dataIterator = null;
		
		
	}

	/*
	 * 若是EELP类型，则cos固定
	 */
	@SuppressWarnings("unused")
	public void qosIsELSP(QosCommonConfig qosCommonConfig,QosConfigController qosController) {
		if (qosCommonConfig.getQosTypeComboBox().getSelectedItem().equals(QosTemplateTypeEnum.ELSP.toString())) {
			setNewTableModel(qosCommonConfig,qosController);
			TableColumn cosColumn = qosCommonConfig.getQosTable().getColumn("COS");
			TableColumn eirColumn = qosCommonConfig.getQosTable().getColumn("EIR(kbps)");
			TableColumn ebsColumn = qosCommonConfig.getQosTable().getColumn(ResourceUtil.srcStr(StringKeysObj.EBS_BYTE));
			TableColumn pirColumn = qosCommonConfig.getQosTable().getColumn("PIR(kbps)");
			for (int i = 0; i < qosCommonConfig.getQosTable().getRowCount(); i++) {
				Object cosvalue = qosCommonConfig.getQosTable().getValueAt(i, 1);
				if (cosvalue.toString().equals(QosCosLevelEnum.EF.toString()) || cosvalue.toString().equals(QosCosLevelEnum.CS6.toString()) || cosvalue.toString().equals(QosCosLevelEnum.CS7.toString())) {
					JTextField jtebsColumn = new JTextField(ebsColumn.getCellEditor().getCellEditorValue() + "");
					jtebsColumn.setEditable(true);
					jtebsColumn.setEnabled(true);
					ebsColumn.setCellEditor(new DefaultCellEditor(jtebsColumn));

					JTextField jteirColumn = new JTextField(eirColumn.getCellEditor().getCellEditorValue() + "");
					jteirColumn.setEditable(true);
					jteirColumn.setEnabled(true);
					eirColumn.setCellEditor(new DefaultCellEditor(jteirColumn));

					JTextField pirJTextField = (JTextField) pirColumn.getCellEditor().getTableCellEditorComponent(qosCommonConfig.getQosTable(), 0, false, i, 7);
					pirJTextField.setEnabled(true);
					pirJTextField.setEditable(true);
				} else {
					JSpinner eirSpinner = (JSpinner) eirColumn.getCellEditor().getTableCellEditorComponent(qosCommonConfig.getQosTable(), 0, true, i, 5);
					eirSpinner.setEnabled(true);
					JSpinner ebsSpinner = (JSpinner) ebsColumn.getCellEditor().getTableCellEditorComponent(qosCommonConfig.getQosTable(), -1, true, i, 6);
					ebsSpinner.setEnabled(true);
					JTextField pirJTextField = (JTextField) pirColumn.getCellEditor().getTableCellEditorComponent(qosCommonConfig.getQosTable(), 0, true, i, 7);
					pirJTextField.setEnabled(true);
					pirJTextField.setEditable(true);
				}
			}
		} else {
			setNewTableModel(qosCommonConfig,qosController);
			TableColumn cosColumn = qosCommonConfig.getQosTable().getColumn("COS");
			for (int j = 0; j < qosCommonConfig.getQosTable().getRowCount(); j++) {
				JComboBox comboBox = (JComboBox) cosColumn.getCellEditor().getTableCellEditorComponent(qosCommonConfig.getQosTable(), cosColumn.getCellEditor().getCellEditorValue().toString(), false, j, 1);
				comboBox.setEnabled(true);
			}
		}

	}

	@SuppressWarnings("serial")
	private void setNewTableModel(QosCommonConfig qosCommonConfig,QosConfigController qosController) {
		DefaultTableModel tableModel = null;
		if (qosCommonConfig.getQosTypeComboBox().getSelectedItem().equals(QosTemplateTypeEnum.ELSP.toString())) {
			tableModel = new DefaultTableModel() {
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0 || column == 2 || column == 1) {
						return false;
					}
					return true;
				}
			};
		} else {
			// 如果pw为pdh类型
			if (qosController.getObjType().equals(EServiceType.PW.toString()) && qosController.getChoosePwType() != EPwType.ETH) {
				tableModel = new DefaultTableModel() {
					@Override
					public boolean isCellEditable(int row, int column) {
						if (column == 1) {
							return true;
						}
						return false;
					}
				};
			} else {
				tableModel = new DefaultTableModel() {
					@Override
					public boolean isCellEditable(int row, int column) {
						if (column == 0 || column == 2) {
							return false;
						}
						return true;
					}
				};
			}
		}
		tableModel.setDataVector(this.getDatas(), qosCommonConfig.getColumnName());
		qosCommonConfig.setQosTableModel(tableModel);
		qosCommonConfig.getQosTable().setModel(tableModel);
		if (qosController.getObjType().equals(EServiceType.PW.toString()) && qosController.getChoosePwType() != EPwType.ETH) {
			qosCommonConfig.getComboBox().removeAllItems();
			for (QosCosLevelEnum qos : QosCosLevelEnum.values()) {
				if (qos.getValue() > 4) {
					qosCommonConfig.getComboBox().addItem(qos);
				}
			}
		} else {
			qosCommonConfig.configCosComboBox();
		}
		qosCommonConfig.configSpinner(qosCommonConfig.getQosTable(), "");
	}

	/*
	 * 使表格数据瞬间变化
	 */
	public void commitTable(JTable table) {
		int selectR = -1;
		int selectC = -1;
		int oldValue = 0;
		int newValue = 0;
		JSpinner spinner = null;
		try {
			if (table.getEditorComponent() != null) {
				if (table.getEditorComponent() instanceof JSpinner) {
					spinner = (JSpinner) table.getEditorComponent();
					selectR = table.getSelectedRow();
					selectC = table.getSelectedColumn();
					if (selectR >= 0 && selectC >= 0) {
						oldValue = Integer.valueOf(table.getValueAt(selectR, selectC) + "");
					}
					JTextField ff = ((JSpinner.NumberEditor) (spinner.getComponents()[2])).getTextField();
					String value = ff.getText();
					((DefaultEditor) spinner.getEditor()).getTextField().setText(value);
					for (char di : value.replace(",", "").toCharArray()) {
						if (!Character.isDigit(di)) {
							return;
						}
					}
					if ("".equals(value.replace(",", ""))) {
						newValue = 0;
//					} else if (Long.parseLong(value.replace(",", "")) >= (CodeConfigItem.getInstance().getWuhan() == 1 ? ConstantUtil.QOS_CIR_MAX_10G / 1000 : ConstantUtil.QOS_CIR_MAX_10G)) {
					} else if (Long.parseLong(value.replace(",", "")) >= ConstantUtil.QOS_CIR_MAX_10G) {
//						if (CodeConfigItem.getInstance().getWuhan() == 1) {
//							newValue = ConstantUtil.QOS_CIR_MAX_10G / 1000;
//						} else {
							newValue = ConstantUtil.QOS_CIR_MAX_10G;
//						}
					} else if (Long.parseLong(value.replace(",", "")) <= 0) {
						newValue = 0;
					} else {
						newValue = Integer.parseInt(value.replace(",", ""));
					}
					if (selectC != 4 && selectC != 6) {
//						if (CodeConfigItem.getInstance().getWuhan() == 1) {
//							spinner.setModel(new SpinnerNumberModel(newValue, 0, ConstantUtil.QOS_CIR_MAX_10G / 1000, 1));
//						} else {
							spinner.setModel(new SpinnerNumberModel(newValue, 0, ConstantUtil.QOS_CIR_MAX_10G, 64));
//						}
					}

					if (selectC == 4 || selectC == 6) {
						if(newValue < 0 || newValue > ConstantUtil.CBS_MAXVALUE){
							table.setValueAt(1, selectR, selectC);
						}else{
							spinner.setModel(new SpinnerNumberModel(newValue, 0, ConstantUtil.CBS_MAXVALUE, 1));
						}
//						if (oldValue == -1) {
//							if (newValue > oldValue && newValue < 4096) {
//								table.setValueAt(4096, selectR, selectC);
//							} else if (newValue >= 4096) {
//								table.setValueAt(newValue, selectR, selectC);
//							} else if (newValue <= -1) {
//								table.setValueAt(-1, selectR, selectC);
//							}
//						} else if (oldValue >= 4096) {
//							if (newValue < 4096) {
//								table.setValueAt(-1, selectR, selectC);
//							} else if (newValue >= 4096) {
//								table.setValueAt(newValue, selectR, selectC);
//							}
//						}
					} else {
//						if (CodeConfigItem.getInstance().getWuhan() == 1) {
//							if (newValue % 1 != 0) {
//								if (newValue > 1) {
//									newValue = ((newValue / 1)) * 1;
//								} else {
//									newValue = 1;
//								}
//								table.setValueAt(newValue, selectR, selectC);
//							}
//						} else {
							if (newValue % 64 != 0) {
								if (newValue > 64) {
									newValue = ((newValue / 64)) * 64;
								} else {
									newValue = 64;
								}
								table.setValueAt(newValue, selectR, selectC);
							}
//						}

					}
					spinner.commitEdit();
					if (table.isEditing()) {
						table.getCellEditor().stopCellEditing();
					}
				}
			}
		} catch (Exception e) {
			((DefaultEditor) spinner.getEditor()).getTextField().setText(spinner.getValue() + "");
			ExceptionManage.dispose(e, this.getClass());
		}

	}

	/*
	 * 使表格数据瞬间变化
	 */
	public void commitSectionTable(JTable table, String type) {
		int selectR = -1;
		int selectC = -1;
		@SuppressWarnings("unused")
		int oldValue = 0;
		int newValue = 0;
		JSpinner spinner = null;
		try {
			if (table.getEditorComponent() instanceof JSpinner) {
				int maxCir = 0;
				if ("a".equals(type)) {
					maxCir = ConstantUtil.QOS_SEGMENT_A;
				} else {
					maxCir = ConstantUtil.QOS_SEGMENT_Z;
				}
				spinner = (JSpinner) table.getEditorComponent();
				JTextField ff = ((JSpinner.NumberEditor) (spinner.getComponents()[2])).getTextField();
				String value = ff.getText();
				((DefaultEditor) spinner.getEditor()).getTextField().setText(value);
				selectR = table.getSelectedRow();
				selectC = table.getSelectedColumn();
				if (selectR >= 0 && selectC >= 0) {
					oldValue = Integer.valueOf(table.getValueAt(selectR, selectC) + "");
				}
				for (char di : value.replace(",", "").toCharArray()) {
					if (!Character.isDigit(di)) {
						return;
					}
				}
				if ("".equals(value.replace(",", ""))) {
					newValue = 0;
				} else if (Long.parseLong(value.replace(",", "")) >= maxCir) {
					newValue = maxCir;
				} else if (Long.parseLong(value.replace(",", "")) <= 0) {
					newValue = 0;
				} else {
					newValue = Integer.parseInt(value.replace(",", ""));
				}
				if (selectC != 2 && selectC != 6 && selectC != 9) {
					spinner.setModel(new SpinnerNumberModel(newValue, 0, maxCir, 1));
				} else if (selectC == 2) {
//					if (CodeConfigItem.getInstance().getWuhan() == 1) {
//						spinner.setModel(new SpinnerNumberModel(newValue, 0, maxCir, 1));
//					} else {
						spinner.setModel(new SpinnerNumberModel(newValue, 0, maxCir, 64));
//					}
				} else if (selectC == 6 || selectC == 9) {
					if (newValue >= 100) {
						newValue = 100;
					}
					spinner.setModel(new SpinnerNumberModel(newValue, 0, 100, 1));
				}

				spinner.commitEdit();
				if (table.isEditing()) {
					table.getCellEditor().stopCellEditing();
				}
				if (selectC == 2) {
//					if (CodeConfigItem.getInstance().getWuhan() == 1) {
//						if (newValue % 1 != 0) {
//							if (newValue > 1) {
//								newValue = ((newValue / 1)) * 1;
//							} else {
//								newValue = 1;
//							}
//							table.setValueAt(newValue, selectR, selectC);
//						}
//					} else {
						if (newValue % 64 != 0) {
							if (newValue > 64) {
								newValue = ((newValue / 64)) * 64;
							} else {
								newValue = 64;
							}
							table.setValueAt(newValue, selectR, selectC);
						}
//					}

				} else {
					table.setValueAt(newValue, selectR, selectC);
				}
			}
		} catch (ParseException e) {
			((DefaultEditor) spinner.getEditor()).getTextField().setText(Integer.valueOf(spinner.getValue().toString()) + "");

			ExceptionManage.dispose(e, this.getClass());
		}

	}

	/*
	 * 当表格数据变化时，使得数据变化一致
	 */
	public void setDataIsConsistent(QosCommonConfig qosCommonConfig) {
		// pir设的初始值应该== cir + eir
		int selectR = qosCommonConfig.getQosTable().getSelectedRow();
		Object cirvalue = qosCommonConfig.getQosTable().getValueAt(selectR, 3);
		Object eirvalue = qosCommonConfig.getQosTable().getValueAt(selectR, 5);
		Integer value = Integer.parseInt(cirvalue.toString()) + Integer.parseInt(eirvalue.toString()) ;
		qosCommonConfig.getQosTableModel().setValueAt(value, selectR, 7);
	}

	/*
	 * 保证cos的一致性
	 */
	public void keepCosConsistent(QosCommonConfig qosCommonConfig,QosConfigController qosController) {
		if (qosCommonConfig.getQosTypeComboBox().getSelectedItem().equals(QosTemplateTypeEnum.LLSP.toString())) {
			if (qosCommonConfig.getQosTable().getSelectedColumn() == 1) {
				TableColumn cosColumn = qosCommonConfig.getQosTable().getColumn("COS");
				Object cosvalue = cosColumn.getCellEditor().getCellEditorValue();
				for (int i = 0; i < 2; i++) {
					if (((QosCosLevelEnum) cosvalue).getValue() <= 4) {
						qosCommonConfig.getQosTable().setValueAt(((QosCosLevelEnum) cosvalue).toString(), i, 1);
						qosCommonConfig.getQosTable().setValueAt(0, i, 3);
						qosCommonConfig.getQosTable().setValueAt(0, i, 7);
					} else {
						qosCommonConfig.getQosTable().setValueAt(((QosCosLevelEnum) cosvalue).toString(), i, 1);
						if (qosController.getObjType().equals(EServiceType.PW.toString()) && qosController.getChoosePwType() != EPwType.ETH) {
							qosCommonConfig.getQosTable().setValueAt(this.cosNum, i, 3);
							qosCommonConfig.getQosTable().setValueAt(this.cosNum, i, 7);
						} else {
							qosCommonConfig.getQosTable().setValueAt(0, i, 3);
							qosCommonConfig.getQosTable().setValueAt(0, i, 7);
						}
					}
				}
				setNewTableModel(qosCommonConfig,qosController);
			}
		}
	}

	public void qosIsSP(QosCommonConfig qosCommonConfig, ActionEvent evt) {
		@SuppressWarnings("unused")
		JComboBox comboBox = new JComboBox();
		if (evt.getActionCommand().equals("sectionA")) {
			comboBox = qosCommonConfig.getSectionAQosQueueComboBox();
		} else {
			comboBox = qosCommonConfig.getSectionZQosQueueComboBox();
		}
		setNewSectionTableModel(qosCommonConfig, evt.getActionCommand());

	}

	@SuppressWarnings("serial")
	private void setNewSectionTableModel(QosCommonConfig qosCommonConfig, String obj) {
		DefaultTableModel tableModel = null;
		JComboBox comboBox = new JComboBox();
		if (obj.equals("sectionA")) {
			comboBox = qosCommonConfig.getSectionAQosQueueComboBox();
		} else {
			comboBox = qosCommonConfig.getSectionZQosQueueComboBox();
		}
		if (comboBox.getSelectedItem().equals("SP")) {
			tableModel = new DefaultTableModel() {
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0 || column == 1 || column == 3 || column == 11) {
						return false;
					}
					return true;
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class getColumnClass(int c) {
					return getValueAt(0, c).getClass();
				}
			};
		} else {
			tableModel = new DefaultTableModel() {
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0 || column == 1 || column == 11) {
						return false;
					}
					return true;
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class getColumnClass(int c) {
					return getValueAt(0, c).getClass();
				}
			};
		}
		if (obj.equals("sectionA")) {
			tableModel.setDataVector(this.getSectionADatas(), qosCommonConfig.getColumnName());
			qosCommonConfig.setSectionAQosTableModel(tableModel);
			qosCommonConfig.getSectionAQosTable().setModel(tableModel);
			qosCommonConfig.configCosComboBox();
			qosCommonConfig.configSpinner(qosCommonConfig.getSectionAQosTable(), "");
			if (comboBox.getSelectedItem().equals("SP")) {
				@SuppressWarnings("unused")
//				TableColumn weightColumn = qosCommonConfig.getSectionAQosTable().getColumn("调度权重");
				TableColumn weightColumn = qosCommonConfig.getSectionAQosTable().getColumn(ResourceUtil.srcStr(StringKeysObj.SCHEDUL));
				//
//				ResourceUtil.srcStr(StringKeysObj.SCHEDUL)
				for (int i = 0; i < qosCommonConfig.getSectionAQosTable().getRowCount(); i++) {
					qosCommonConfig.getSectionAQosTableModel().setValueAt(16, i, 3);
				}
			}
		} else {
			tableModel.setDataVector(this.getSectionZDatas(), qosCommonConfig.getColumnName());
			qosCommonConfig.setSectionZQosTableModel(tableModel);
			qosCommonConfig.getSectionZQosTable().setModel(tableModel);
			qosCommonConfig.configCosComboBox();
			qosCommonConfig.configSpinner(qosCommonConfig.getSectionZQosTable(), "");
			if (comboBox.getSelectedItem().equals("SP")) {
				@SuppressWarnings("unused")
//				TableColumn weightColumn = qosCommonConfig.getSectionZQosTable().getColumn("调度权重");
				TableColumn weightColumn = qosCommonConfig.getSectionAQosTable().getColumn(ResourceUtil.srcStr(StringKeysObj.SCHEDUL));
				for (int i = 0; i < qosCommonConfig.getSectionZQosTable().getRowCount(); i++) {
					qosCommonConfig.getSectionZQosTableModel().setValueAt(16, i, 3);
				}
			}
		}
	}

	public List<Integer> getIdList() {
		return idList;
	}

	public void setIdList(List<Integer> idList) {
		this.idList = idList;
	}

	@SuppressWarnings("rawtypes")
	public Vector getDatas() {
		return datas;
	}

	@SuppressWarnings("rawtypes")
	public void setDatas(Vector datas) {
		this.datas = datas;
	}

	@SuppressWarnings("rawtypes")
	public Vector getSectionADatas() {
		return sectionADatas;
	}

	@SuppressWarnings("rawtypes")
	public void setSectionADatas(Vector sectionADatas) {
		this.sectionADatas = sectionADatas;
	}

	@SuppressWarnings("rawtypes")
	public Vector getSectionZDatas() {
		return sectionZDatas;
	}

	@SuppressWarnings("rawtypes")
	public void setSectionZDatas(Vector sectionZDatas) {
		this.sectionZDatas = sectionZDatas;
	}

}
