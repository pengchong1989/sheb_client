﻿package com.nms.ui.ptn.business.dialog.tunnel.action;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import twaver.Element;
import twaver.Link;
import twaver.Node;
import twaver.TDataBox;
import twaver.TWaverConst;

import com.nms.db.bean.equipment.port.PortInst;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.bean.path.Segment;
import com.nms.db.bean.ptn.oam.OamInfo;
import com.nms.db.bean.ptn.path.pw.MsPwInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Lsp;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.bean.ptn.qos.QosInfo;
import com.nms.db.bean.ptn.qos.QosQueue;
import com.nms.db.bean.system.code.Code;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EManufacturer;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EPwType;
import com.nms.db.enums.EQosDirection;
import com.nms.db.enums.EServiceType;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.path.SegmentService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.path.tunnel.TunnelService_MB;
import com.nms.model.ptn.qos.QosInfoService_MB;
import com.nms.model.ptn.qos.QosQueueService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.SegmentTopoPanel;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.VerifyNameUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.manager.util.ComboBoxDataUtil;
import com.nms.ui.ptn.business.dialog.tunnel.AddTunnelPathDialog;
import com.nms.ui.ptn.business.dialog.tunnel.ProtectTunnelDialg;
import com.nms.ui.ptn.oam.view.dialog.OamInfoDialog;
import com.nms.ui.ptn.systemconfig.dialog.qos.controller.QosConfigController;

/**
 * Tunnel 的 事件处理
 * 
 * @author Administrator
 * 
 */
public class TunnelAction {
	List RouteSegments = null;

	private String beforeName;
	public String getBeforeName() {
		return beforeName;
	}

	public void setBeforeName(String beforeName) {
		this.beforeName = beforeName;
	}

	/* 判断两个网元之间有几个段 */
	public int findSegmentNum(int siteInstIDA, int siteInstIDZ) {
		List<Segment> SegmentA = new ArrayList<Segment>();
		List<Segment> SegmentZ = new ArrayList<Segment>();
		SegmentA = querySegmentbySiteinst(siteInstIDA);
		SegmentZ = querySegmentbySiteinst(siteInstIDZ);
		int iCount = 0;
		for (int i = 0; i < SegmentA.size(); i++) {
			for (int j = 0; j < SegmentZ.size(); j++) {
				if (SegmentA.get(i).getASITEID() == SegmentZ.get(j).getASITEID() && SegmentA.get(i).getZSITEID() == SegmentZ.get(j).getZSITEID()) {
					iCount++;
				}
			}
		}

		return iCount;
	}

	private boolean unRelated(int idA, int idZ, Segment sg) {
		boolean unRelated = true;
		int sgAid = sg.getASITEID();
		int sgZid = sg.getZSITEID();
		if ((sgAid == idA && sgZid == idZ) || (sgAid == idZ && sgZid == idA)) {
			unRelated = false;
		}

		return unRelated;
	}

	/* 通过两个网元确定一个段 */
	public Segment querySegment(int siteInstIDA, int siteInstIDZ, Segment sgM) {
		List<Segment> SegmentA = new ArrayList<Segment>();
		List<Segment> SegmentZ = new ArrayList<Segment>();
		SegmentA = querySegmentbySiteinst(siteInstIDA);
		SegmentZ = querySegmentbySiteinst(siteInstIDZ);
		for (int i = 0; i < SegmentA.size(); i++) {
			for (int j = 0; j < SegmentZ.size(); j++) {
				if (SegmentA.get(i).getASITEID() == SegmentZ.get(j).getASITEID() && SegmentA.get(i).getZSITEID() == SegmentZ.get(j).getZSITEID()) {
					// 如果必经段为空
					if (sgM.getId() == 0) {
						return SegmentA.get(i);
					}

					if (sgM.getId() > 0) {
						if (SegmentA.get(i).getId() == sgM.getId()) {
							return SegmentA.get(i);
						}

						// 如果必经段与此两网元之间的段毫无关联
						if (unRelated(siteInstIDA, siteInstIDZ, sgM)) {
							return SegmentA.get(i);
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * 查一个网元上所有的段
	 */
	public List<Segment> querySegmentbySiteinst(int siteInstID) {
		SegmentService_MB serviceMB = null;
		List<Segment> segmentList = null;
		try {
			serviceMB = (SegmentService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SEGMENT);
			segmentList = serviceMB.queryBySiteId(siteInstID);
		}

		catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(serviceMB);
		}
		return segmentList;
	}

	/**
	 * 查一个网元上所有的段，通过网元??
	 */
	public List<Segment> autoRoute(SiteInst siteInst) {
		SegmentService_MB service = null;
		List<Segment> segmentList = null;
		try {
			service = (SegmentService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SEGMENT);
			segmentList = service.queryBySiteId(Integer.valueOf(siteInst.getSite_Inst_Id()));
		}

		catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
		return segmentList;
	}

	List<Segment> findSegment(Segment sgA, int zId) {
		List<Segment> Segments = new ArrayList<Segment>();
		List<Segment> SegmentZ = new ArrayList<Segment>();
		SegmentZ = querySegmentbySiteinst(zId);
		for (Segment sgZ : SegmentZ) {
			if (sgA.getASITEID() == sgZ.getASITEID() && sgA.getZSITEID() == sgZ.getZSITEID()) {
				Segments.add(sgA);
				return Segments;
			}
		}

		return null;
	}

	// 实现两次两个网元之间的段的组合
	private List findTwoComb(List<Segment> list1, List<Segment> list2) {
		List ll = new ArrayList();
		for (int i = 0; i < list1.size(); i++) {
			for (int j = 0; j < list2.size(); j++) {
				List<Segment> temp = new ArrayList<Segment>();
				temp.add(list1.get(i));
				temp.add(list2.get(j));
				ll.add(temp);
			}
		}

		return ll;
	}

	private List combListandSeg(List list, List<Segment> list2) {
		List ll = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list2.size(); j++) {
				List<Segment> temp = new ArrayList<Segment>();
				temp.addAll((Collection<? extends Segment>) list.get(i));
				temp.add(list2.get(j));
				ll.add(temp);
			}
		}

		return ll;
	}

	// 递归实现与后面的段进行组合排列
	private List allCombine(List list) {
		List ll = new ArrayList();
		if (list.size() == 2) {
			List listtemp = findTwoComb((List<Segment>) list.get(0), (List<Segment>) list.get(1));

			return listtemp;
		}

		if (list.size() > 2) {
			List temp = new ArrayList();
			for (int j = 0; j < list.size() - 1; j++) {
				temp.add(list.get(j));
			}

			ll = combListandSeg(allCombine(temp), (List<Segment>) list.get(list.size() - 1));
		}
		return ll;
	}

	private List dealSimilarWay(List list) {
		List ll = new ArrayList();
		String[] a = list.get(0).toString().split("-");
		List<Segment> SegmentA = new ArrayList<Segment>();
		if (a.length == 2) {
			SegmentA = querySegmentbySiteinst(Integer.valueOf(a[0]));

			for (Segment sgA : SegmentA) {
				ll.add(findSegment(sgA, Integer.valueOf(a[1])));
			}
		} else if (a.length > 2) {
			List tempList = new ArrayList();
			Segment sgSignle = new Segment();
			boolean isSingle = false;
			boolean isMulty = false;

			// 分别找出两个网元之间的一条段或者两条段
			for (int i = 0; i < a.length - 1; i++) {
				int num = findSegmentNum(Integer.valueOf(a[i]), Integer.valueOf(a[i + 1]));
				if (num == 1) {
					isSingle = true;
					isMulty = false;
					sgSignle = querySegment(Integer.valueOf(a[i]), Integer.valueOf(a[i + 1]), new Segment());

					if (isSingle && !isMulty) {
						List<Segment> temp = new ArrayList<Segment>();
						temp.add(sgSignle);
						tempList.add(temp);
					}
				}

				if (num > 1) {
					isMulty = true;
					isSingle = false;
					SegmentA = querySegmentbySiteinst(Integer.valueOf(a[i]));
					List<Segment> tempMulti = new ArrayList<Segment>();
					for (Segment sgA : SegmentA) {
						List<Segment> listtSg = findSegment(sgA, Integer.valueOf(a[i + 1]));
						if (listtSg != null) {
							tempMulti.addAll(listtSg);
						}
					}

					if (isMulty && !isSingle) {
						tempList.add(tempMulti);
					}
				}
			}

			// 如果两个网元之间都是两个以上的网元
			ll = allCombine(tempList);
			return ll;
		}

		// 此处需要照明为空的原因
		Iterator<List<Segment>> itr = ll.iterator();
		while (itr.hasNext()) {
			List<Segment> listsg = itr.next();
			if (listsg == null) {
				itr.remove();
			}
		}
		return ll;
	}

	/* list 中装了多个含有段ID的list 转换成多个含有段的list */
	private List<List<Segment>> id2Segment(List list, String type, AddTunnelPathDialog dialog) {
		List<List<Segment>> ll = new ArrayList<List<Segment>>();
		if (list == null) {
			return null;
		}

		// 处理保护情况下两条list相同的情况，如1-2-3，1-2-3
		if (list.size() == 2) {
			if (list.get(0).equals(list.get(1))) {
				return dealSimilarWay(list);
			} else {
				// todo 处理两条不一样的，如1-2和1-3-2
			}
		}

		// 如果2-1间有多个段，也应该找出多条段
		if (list.size() == 1) {
			String temp = list.get(0).toString();
			String[] a = temp.split("-");
			List<Segment> mysg = new ArrayList<Segment>();
			for (int j = 0; j < a.length - 1; j++) {
				int asiteid = Integer.valueOf(a[j]);
				int zsiteid = Integer.valueOf(a[j + 1]);
				if (findSegmentNum(asiteid, zsiteid) > 1) {
					ll = dealSimilarWay(list);
					break ;
				} else {
					mysg.add(querySegment(asiteid, zsiteid, new Segment()));
				}
			}

			if (mysg.size() > 0 && ll.size() == 0) {
				ll.add(mysg);
			}
		}

		if (dialog.getEquipmentTopology().getSgMust().size() > 0 && type.equals("work")) {
			for (int i = 0; i < list.size(); i++) {
				List<Segment> mysg = new ArrayList<Segment>();
				String temp = list.get(i).toString();
				String[] a = temp.split("-");
				for (int j = 0; j < a.length - 1; j++) {
					int srcId = Integer.valueOf(a[j]);
					int dstId = Integer.valueOf(a[j + 1]);

					for (int k = 0; k < dialog.getEquipmentTopology().getSgMust().size(); k++) {
						Segment sgM = dialog.getEquipmentTopology().getSgMust().get(k);
						int aid = sgM.getASITEID();
						int zid = sgM.getZSITEID();
						if ((aid == srcId && zid == dstId) || (aid == dstId && zid == srcId)) {
							Segment sg = querySegment(srcId, dstId, sgM);

							if (sg != null) {
								mysg.add(sg);
							}
						}
					}
				}
				if (mysg.size() > 0) {
					ll.add(mysg);
				}
			}
		}

		if (dialog.getEquipmentTopology().getSgproMust().size() > 0 && type.equals("protect")) {
			for (int i = 0; i < list.size(); i++) {
				List<Segment> mysg = new ArrayList<Segment>();
				String temp = list.get(i).toString();
				String[] a = temp.split("-");
				for (int j = 0; j < a.length - 1; j++) {
					int srcId = Integer.valueOf(a[j]);
					int dstId = Integer.valueOf(a[j + 1]);
					// 经过两个网元与必经段确定一个段，必经可能多条需遍历
					for (int k = 0; k < dialog.getEquipmentTopology().getSgproMust().size(); k++) {
						Segment sgM = dialog.getEquipmentTopology().getSgproMust().get(k);
						int aid = sgM.getASITEID();
						int zid = sgM.getZSITEID();
						if ((aid == srcId && zid == dstId) || (aid == dstId && zid == srcId)) {
							Segment sg = querySegment(srcId, dstId, sgM);

							if (sg != null) {
								mysg.add(sg);
							}
						}
					}
				}

				if (mysg.size() > 0) {
					ll.add(mysg);
				}
			}
		}
		
		return ll;
	}
	/*
	 * 自动路由实现 add by dxh
	 */
	public List autoRoute(String type, AddTunnelPathDialog dialog) {
		AutoRouteAction auto = new AutoRouteAction();
		List<Segment> Segments = new ArrayList<Segment>();
		RouteSegments = new ArrayList();
		int aPortid = dialog.getEquipmentTopology().getAPortId();
		int zPortid = dialog.getEquipmentTopology().getZPortId();
		try {
			Segments = dialog.getEquipmentTopology().getAllSegmentFromTopo();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		for (Segment sg : Segments) {
			auto.addRoute(sg.getASITEID(), sg.getZSITEID(), 1);
		}
		int from = dialog.getEquipmentTopology().getSiteA().getSite_Inst_Id();
		int to = dialog.getEquipmentTopology().getSiteZ().getSite_Inst_Id();
		if (dialog.getEquipmentTopology().isMust() || dialog.getEquipmentTopology().isSgMust() || dialog.getEquipmentTopology().isIsproSgMust()) {
			// 将必经分为网元必经和段必经，分别验证之
			List nePass = new ArrayList();
			List segPass = new ArrayList();
			List seprogPass = new ArrayList();
			for (SiteInst site : dialog.getEquipmentTopology().getSiteMust()) {
				nePass.add(site.getSite_Inst_Id());
			}

			if (type.equals("work")) {
				// 添加必经工作段所转换成的网元
				if (dialog.getEquipmentTopology().isSgMust() && dialog.getEquipmentTopology().getSgMust().size() > 0) {
					for (Segment sg : dialog.getEquipmentTopology().getSgMust()) {
						segPass.add(sg.getASITEID());
						segPass.add(sg.getZSITEID());
					}
				}
			}

			if (type.equals("protect")) {

				// 添加必经保护段所转换成的网元
				if (dialog.getEquipmentTopology().isIsproSgMust() && dialog.getEquipmentTopology().getSgproMust().size() > 0) {
					for (Segment sg : dialog.getEquipmentTopology().getSgproMust()) {
						seprogPass.add(sg.getASITEID());
						seprogPass.add(sg.getZSITEID());
					}
				}

				// 如果必经网元为工作，必经段为保护
				if (dialog.isWorkMust() && !dialog.isWorksgMust()) {
					nePass.clear();
				}
				// 如果必经网元为保护，必经段为工作
				if (!dialog.isWorkMust() && dialog.isWorksgMust()) {
					segPass.clear();
				}

				// 如果必经网元为工作，必经段也为工作
				if (dialog.isWorkMust() && dialog.isWorksgMust()) {
					nePass.clear();
					segPass.clear();
				}
			}

			RouteSegments = id2Segment(auto.show(from, nePass, segPass, seprogPass, to),type,  dialog);
		} else {
			RouteSegments = id2Segment(auto.show(from, type, to), type, dialog);
		}

		return RouteSegments;
	}

	public void initPort(AddTunnelPathDialog dialog, List<Segment> segments) {
		PortService_MB portServiceMB = null;
		PortInst Aport = null;
		PortInst Zport = null;
		try {
			portServiceMB = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);

			Aport = new PortInst();
			Zport = new PortInst();
			Segment sgA = segments.get(0);
			Segment sgZ = segments.get(segments.size() - 1);
			if (dialog.getEquipmentTopology().getSiteA().getSite_Inst_Id() == sgA.getASITEID()) {
				Aport.setPortId(sgA.getAPORTID());
				Aport.setSiteId(sgA.getASITEID());
			} else {
				Aport.setPortId(sgA.getZPORTID());
				Aport.setSiteId(sgA.getZSITEID());
			}

			if (dialog.getEquipmentTopology().getSiteZ().getSite_Inst_Id() == sgZ.getASITEID()) {
				Zport.setPortId(sgZ.getAPORTID());
				Zport.setSiteId(sgZ.getASITEID());
			} else {
				Zport.setPortId(sgZ.getZPORTID());
				Zport.setSiteId(sgZ.getZSITEID());
			}
			Aport = portServiceMB.select(Aport).get(0);
			dialog.setPortInst_A(Aport);
			Zport = portServiceMB.select(Zport).get(0);
			dialog.setPortInst_Z(Zport);

		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(portServiceMB);
			Aport = null;
			Zport = null;
		}
	}

	public void initLeftPanelData(Tunnel tunnel, AddTunnelPathDialog dialog) {
		PortService_MB portServiceMB = null;
		PortInst Aport = null;
		PortInst Zport = null;
		try {
			portServiceMB = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			Aport = new PortInst();
			Aport.setPortId(tunnel.getAPortId());
			Aport.setSiteId(tunnel.getASiteId());
			Aport = portServiceMB.select(Aport).get(0);
			dialog.setPortInst_A(Aport);
			Zport = new PortInst();
			Zport.setPortId(tunnel.getZPortId());
			Zport.setSiteId(tunnel.getZSiteId());
			Zport = portServiceMB.select(Zport).get(0);
			dialog.setPortInst_Z(Zport);
			dialog.getEquipmentTopology().setSiteA(new SiteInst());
			dialog.getEquipmentTopology().setSiteZ(new SiteInst());
			dialog.getEquipmentTopology().getSiteA().setSite_Inst_Id(tunnel.getASiteId());
			dialog.getEquipmentTopology().getSiteA().setCellId(tunnel.getShowSiteAname());
			dialog.getEquipmentTopology().getSiteZ().setSite_Inst_Id(tunnel.getZSiteId());
			dialog.getEquipmentTopology().getSiteZ().setCellId(tunnel.getShowSiteZname());
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(portServiceMB);
			Aport = null;
			Zport = null;
		}

		dialog.getResetBtn().setActionCommand("protect");
		dialog.getjTextField1().setText(tunnel.getTunnelName());

		dialog.getAutoTable().setModel(dialog.getDataModel(tunnel));

		dialog.getjCheckBox1().setSelected(tunnel.getTunnelStatus() == EActiveStatus.UNACTIVITY.getValue() ? false : true);
		dialog.getjCheckBox2().setSelected(tunnel.getIsReverse() == 0 ? false : true);
		dialog.getjTextField4().setText(tunnel.getProtectTunnelName());

		dialog.getjTextArea1().setText(tunnel.getDirection());
		ComboBoxDataUtil comboBoxDataUtil = new ComboBoxDataUtil();
		comboBoxDataUtil.comboBoxSelect(dialog.getCmbType(), tunnel.getTunnelType());
		dialog.getTxtWaitTime().getTxt().setText(tunnel.getWaittime() + "");
		dialog.getTxtDelayTime().getTxt().setText(tunnel.getDelaytime() + "");

		dialog.getChkAps().setSelected(tunnel.getApsenable() == 0 ? false : true);
		dialog.getProtectBack().setSelected(tunnel.getProtectBack() == 1 ? false : true);
		dialog.getCmbRotateWay().setSelectedItem(tunnel.getRotateWay());
		dialog.getCmbRotateLocation().setSelectedItem(tunnel.getRotateLocation());
		dialog.getCmbRotateMode().setSelectedItem(tunnel.getRotateMode());
		dialog.getSpinnerTnpLayer().getTxt().setText(tunnel.getTnpLayer()+"");
		dialog.getSpinnerRotateThreshold().getTxt().setText(tunnel.getRotateThreshold()+"");
		if (tunnel.getSourceMac() == null || "".equals(tunnel.getSourceMac())) {
			dialog.getSourceMacText().setText("00-00-00-00-00-01");
		} else {
			dialog.getSourceMacText().setText(tunnel.getSourceMac());
		}
		if (tunnel.getEndMac() == null || "".equals(tunnel.getEndMac())) {
			dialog.getEndMacText().setText("00-00-00-00-00-01");
		} else {
			dialog.getEndMacText().setText(tunnel.getEndMac());
		}
		if (tunnel.getProtectTunnelId() > 0) {
			Tunnel proTunnel = tunnel.getProtectTunnel();
			if (proTunnel.getSourceMac() == null || "".equals(proTunnel.getSourceMac())) {
				dialog.getSourceMacText_backup().setText("00-00-00-00-00-01");
			} else {
				dialog.getSourceMacText_backup().setText(proTunnel.getSourceMac());
			}
			if (proTunnel.getEndMac() == null || "".equals(proTunnel.getEndMac())) {
				dialog.getEndMacText_backup().setText("00-00-00-00-00-01");
			} else {
				dialog.getEndMacText_backup().setText(proTunnel.getEndMac());
			}
		}
	}

	/*
	 * 配置qos
	 */
	public void qosConfigButtonActionPerformed(java.awt.event.ActionEvent evt, AddTunnelPathDialog dialog) {
		QosConfigController controller = new QosConfigController();
		controller.openQosConfig(controller, "TUNNEL", dialog.getTunnel(), null, dialog);
	}

	/*
	 * 配置oam
	 */
	public void oamConfigButtonActionPerformed(ActionEvent evt, AddTunnelPathDialog dialog) {
		try {
			if(!(dialog.getTunnel() != null && dialog.getTunnel().getTunnelId()>0)){
				
				//如果是1:1，且没有保护路由给予提示
				if(dialog.getCmbType().getSelectedIndex()==1 && dialog.getProSg().size() == 0)
				{
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_PLEASE_PROTROUTER));
				}
				else if(dialog.getCmbType().getSelectedIndex()==0 && dialog.getWorkSg().size() == 0)
				{
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_PLEASE_WORKROUTER));
				}
				else
				{
					new OamInfoDialog(getTunnel(dialog), EServiceType.TUNNEL.toString(), 0, true, dialog);
				}
			}
			else
			{
				new OamInfoDialog(getTunnel(dialog), EServiceType.TUNNEL.toString(), 0, true, dialog);
			}
			
			
			
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
	}

	/*
	 * 路径检查
	 */
	public void pathCheckButtonActionPerformed(ActionEvent evt, AddTunnelPathDialog dialog) throws Exception {
		if (!dialog.getEquipmentTopology().checkAnddrawTopo()) {
			DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_NO_JOB_PATH));
			return;
		}

		Code code = (Code) ((ControlKeyValue) dialog.getCmbType().getSelectedItem()).getObject();
		if ("2".equals(code.getCodeValue())) {

			if (dialog.getEquipmentTopology().getNode_a_pro() == null || dialog.getEquipmentTopology().getNode_z_pro() == null) {
				DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_AZ_PROTECT_BEFORE));
				return;
			}

			if (!dialog.getEquipmentTopology().checkAnddrawTopo_protect()) {
				DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_NO_PROTECT_PATH));
				return;
			}
		}

		dialog.setHasCheck(true);
	}

	// 将路由的信息设置到tunnel中去
	public void setSegmenttoTunnel(AddTunnelPathDialog dialog, List<Segment> Segments) {
		Segment sgA = Segments.get(0);
		Segment sgZ = Segments.get(Segments.size() - 1);

		if (sgA.getASITEID() == dialog.getEquipmentTopology().getSiteA().getSite_Inst_Id()) {
			dialog.getPortInst_a().setPortId(sgA.getAPORTID());
			dialog.getPortInst_a().setSiteId(sgA.getASITEID());
		} else {
			dialog.getPortInst_a().setPortId(sgA.getZPORTID());
			dialog.getPortInst_a().setSiteId(sgA.getZSITEID());
		}

		if (sgZ.getASITEID() == dialog.getEquipmentTopology().getSiteZ().getSite_Inst_Id()) {
			dialog.getPortInst_z().setPortId(sgZ.getAPORTID());
			dialog.getPortInst_z().setSiteId(sgZ.getASITEID());
		} else {
			dialog.getPortInst_z().setPortId(sgZ.getZPORTID());
			dialog.getPortInst_z().setSiteId(sgZ.getZSITEID());
		}
	}

	// 从路由的段中找出lsp
	public List<Lsp> getLSPfromRoute(List<Segment> Segments, AddTunnelPathDialog dialog) throws Exception {
		SiteService_MB siteServiceMB = null;
		String realSiteAName = dialog.getEquipmentTopology().getSiteA().getCellId().trim();
		String realSiteZName = dialog.getEquipmentTopology().getSiteZ().getCellId().trim();
		List<Lsp> lspList = new ArrayList<Lsp>();
		try {

			siteServiceMB = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			if (Segments.size() == 1) {
				Lsp lsp = new Lsp();  
				String sgNextA = Segments.get(0).getShowSiteAname().trim();
				Segment segment0 = Segments.get(0);
				if (sgNextA.equals(realSiteAName)) {
					lsp.setAPortId(segment0.getAPORTID());
					lsp.setASiteId(segment0.getASITEID());
					lsp.setAoppositeId(siteServiceMB.select(segment0.getASITEID()).getCellDescribe());
					lsp.setZPortId(segment0.getZPORTID());
					lsp.setZSiteId(segment0.getZSITEID());
					lsp.setZoppositeId(siteServiceMB.select(segment0.getZSITEID()).getCellDescribe());
				} else {
					lsp.setAPortId(segment0.getZPORTID());
					lsp.setASiteId(segment0.getZSITEID());
					lsp.setAoppositeId(siteServiceMB.select(segment0.getZSITEID()).getCellDescribe());
					lsp.setZPortId(segment0.getAPORTID());
					lsp.setZSiteId(segment0.getASITEID());
					lsp.setZoppositeId(siteServiceMB.select(segment0.getASITEID()).getCellDescribe());
				}
				lsp.setFrontLabelValue(0);
				lsp.setBackLabelValue(0);
				if (dialog.getjCheckBox1().isSelected()) {
					lsp.setPathStatus(1);
				} else {
					lsp.setPathStatus(0);
				}

				lsp.setSegmentId(segment0.getId());
				lspList.add(lsp);
			} else if (Segments.size() > 1) {
				for (int i = 0; i < Segments.size() - 1; i++) {
					Lsp lsp = new Lsp();
					String sgZname = Segments.get(i).getShowSiteZname().trim();
					String sgNextA = Segments.get(i + 1).getShowSiteAname().trim();
					String sgNextZ = Segments.get(i + 1).getShowSiteZname().trim();

					Segment segmentA = Segments.get(i);
					// 如果一个段的Z端与下一个段的A或Z端相同，那么这个段就是正的，否则就是反的
					if (sgZname.equals(sgNextA) || sgZname.equals(sgNextZ)) {
						lsp.setAPortId(segmentA.getAPORTID());
						lsp.setASiteId(segmentA.getASITEID());
						lsp.setAoppositeId(siteServiceMB.select(segmentA.getASITEID()).getCellDescribe());
						lsp.setZPortId(segmentA.getZPORTID());
						lsp.setZSiteId(segmentA.getZSITEID());
						lsp.setZoppositeId(siteServiceMB.select(segmentA.getZSITEID()).getCellDescribe());
					} else {
						lsp.setAPortId(segmentA.getZPORTID());
						lsp.setASiteId(segmentA.getZSITEID());
						lsp.setAoppositeId(siteServiceMB.select(segmentA.getZSITEID()).getCellDescribe());
						lsp.setZPortId(segmentA.getAPORTID());
						lsp.setZSiteId(segmentA.getASITEID());
						lsp.setZoppositeId(siteServiceMB.select(segmentA.getASITEID()).getCellDescribe());
					}
					lsp.setPosition(0);
					lsp.setFrontLabelValue(0);
					lsp.setBackLabelValue(0);
					if (dialog.getjCheckBox1().isSelected()) {
						lsp.setPathStatus(1);
					} else {
						lsp.setPathStatus(0);
					}

					lsp.setSegmentId(segmentA.getId());
					lspList.add(lsp);
				}

				// 处理最后一条
				Lsp lsp = new Lsp();
				String sglastZname = Segments.get(Segments.size() - 1).getShowSiteZname().trim();
				Segment segmentLast = Segments.get(Segments.size() - 1);
				if (sglastZname.equals(realSiteZName)) {
					lsp.setAPortId(segmentLast.getAPORTID());
					lsp.setASiteId(segmentLast.getASITEID());
					lsp.setAoppositeId(siteServiceMB.select(segmentLast.getASITEID()).getCellDescribe());
					lsp.setZPortId(segmentLast.getZPORTID());
					lsp.setZSiteId(segmentLast.getZSITEID());
					lsp.setZoppositeId(siteServiceMB.select(segmentLast.getZSITEID()).getCellDescribe());
				} else {
					lsp.setAPortId(segmentLast.getZPORTID());
					lsp.setASiteId(segmentLast.getZSITEID());
					lsp.setAoppositeId(siteServiceMB.select(segmentLast.getZSITEID()).getCellDescribe());
					lsp.setZPortId(segmentLast.getAPORTID());
					lsp.setZSiteId(segmentLast.getASITEID());
					lsp.setZoppositeId(siteServiceMB.select(segmentLast.getASITEID()).getCellDescribe());
				}
				lsp.setPosition(0);
				lsp.setFrontLabelValue(0);
				lsp.setBackLabelValue(0);
				if (dialog.getjCheckBox1().isSelected()) {
					lsp.setPathStatus(1);
				} else {
					lsp.setPathStatus(0);
				}

				lsp.setSegmentId(segmentLast.getId());
				lspList.add(lsp);

			}
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(siteServiceMB);
		}
		return lspList;
	}
	private boolean checkNEChoosen(JComboBox jcmb)
	{
		boolean flag = false;
		try 
		{
			String id=((ControlKeyValue)jcmb.getSelectedItem()).getId();
			
			if(Integer.parseInt(id)>0)
			{
				flag = true;
			}
		}
		catch (Exception e1)
		{
			ExceptionManage.dispose(e1,this.getClass());
		}
		
		return flag;
	}
	

	/**
	 * 创建前 校验数据 所有创建前的验证都写到此方法中。
	 * 
	 * @param dialog
	 *            addTunnelPathDialog 对象
	 * @return true=验证成功，继续执行。 false=验证失败，页面提示
	 */
	public boolean checking(AddTunnelPathDialog dialog) {
		Tunnel tunnel = null;
		String beforeName = null;
		// int maxCreateNum = 0;
		int createNum = 0;
		int maxCreateTunnel = 0; // 创建tunnel的最大数量
		int maxCreateQos = 0; // 创建qos的最大数量
		List<Lsp> lspList = null;
		TunnelService_MB tunnelServiceMB = null;
		boolean fale = false;
		try {

			if(!(dialog.getTunnel() != null && dialog.getTunnel().getTunnelId()>0)){
				
				if (!checkNEChoosen(dialog.getCmbANe()) || !checkNEChoosen(dialog.getCmbZNe())) {
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_MUSTNETWORK_BEFORE));
					return false;
				}
				// 新建时，验证是否选择了路由
				if (dialog.getWorkSg() == null || !(dialog.getWorkSg().size() > 0)) {
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_PLEASE_WORKROUTER));
					return false;
				}
			}
			if(!(dialog.getTunnel() != null && dialog.getTunnel().getTunnelId()>0)){
				
				//如果是1:1，且没有保护路由给予提示
				if(dialog.getCmbType().getSelectedIndex()==1 && dialog.getProSg().size() == 0)
				{
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_PLEASE_PROTROUTER));
					return false;
				}
			}
			tunnel = this.getTunnel(dialog);
			// 验证是否配置了QOS
			if (dialog.isCreate()) {
				if (dialog.getQosList() == null || dialog.getQosList().size() == 0) {
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_QOS_FILL));
					return false;
				}
			}
			// 验证如果是sncp类型 是否包含晨晓网元
			if (this.checkingSncp(tunnel, dialog)) {
				return false;
			}
			// 验证是否配置了标签
			// 如果是1:1保护。 验证了是否配置了oam
			// 验证是tunnel而不是快速创建eline
			if (null == dialog.getAddComponentList()) {
				// 验证名称是否重复
				if (tunnel.getTunnelId() != 0) {
					beforeName = this.getBeforeName();
				}
				VerifyNameUtil verifyNameUtil = new VerifyNameUtil();
				if (verifyNameUtil.verifyName(EServiceType.TUNNEL.getValue(), dialog.getjTextField1().getText(), beforeName)) {
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_NAME_EXIST));
					return false;
				}

				// 验证Qos的带宽是否够用
				if (checkQosIsEnough(tunnel, dialog)) {
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_PORT_QOS_ALARM));
					return false;
				}
				// 如果是新建，验证创建数量
				if (null == tunnel || tunnel.getTunnelId() == 0) {
					createNum = Integer.parseInt(dialog.getPtnSpinnerNumber().getValue().toString());
					// //批量创建时的验证
					// if(createNum>1){
					// // 读取数据库中最大可创建数量
					// maxCreateNum = this.tunnelService.getCreateNum(tunnel);
					// // 如果最大数量小于界面填写数量。弹出提示
					// if (maxCreateNum < createNum) {
					// DialogBoxUtil.errorDialog(dialog,
					// ResourceUtil.srcStr(StringKeysTip.TIP_CREATE_MAX_NUM) +
					// maxCreateNum);
					// return false;
					// }
					// }else{
					// 创建一条时的验证
					tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
					lspList = tunnelServiceMB.getAllLsp(tunnel);
					// 验证设备支持的最大数量
					maxCreateTunnel = tunnelServiceMB.getMinBusinessIdNum(lspList);
					if (createNum > maxCreateTunnel) {
						DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_CREATE_MAX_NUM) + maxCreateTunnel);
						return false;
					}
					// 验证支持的qos数量
					maxCreateQos = tunnelServiceMB.getMinQosNum(lspList, tunnel.getQosList(), null);
					if (createNum > maxCreateQos && maxCreateQos != -1) {
						DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_PORT_QOS_ALARM));
						return false;
					}
					// }
				}
			}
			fale = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(tunnelServiceMB);
			beforeName = null;
		}
		return fale;

	}

	private Tunnel getTunnel(AddTunnelPathDialog dialog) throws Exception {
		Tunnel tunnel = null;
		List<Lsp> lspParticularList = null;
		List<QosInfo> qosList = null;

		Code code_type = null;
		List<Segment> Segments = dialog.getWorkSg();
		List<Segment> proSegments = dialog.getProSg();
		try {
			code_type = (Code) ((ControlKeyValue) dialog.getCmbType().getSelectedItem()).getObject();
			lspParticularList = new ArrayList<Lsp>();
			tunnel = dialog.getTunnel();

			if (tunnel.getTunnelId() == 0 && Segments.size() > 0) {
				// 设置端口
				this.initPort(dialog, Segments);

				this.setSegmenttoTunnel(dialog, Segments);
			}

			if (tunnel.getTunnelId() == 0) {// 创建tunnel
				// 给lsp的前后向标签赋值
				List<Lsp> lspList = this.setLabelToLsp(getLSPfromRoute(Segments, dialog), dialog.getLabelWorkList());
				lspParticularList.addAll(lspList);
				qosList = dialog.getQosList();
				tunnel.setQosList(qosList);
			} else {
				// 修改，此时topo中没有初始化lspparticular,因为路径信息不能修改
				lspParticularList.addAll(setLabelToLsp(tunnel.getLspParticularList(), dialog.getLabelWorkList()));
			}
			// 设置LSP
			tunnel.setOamList(dialog.getOamList());
			tunnel.setAPortId(dialog.getPortInst_a().getPortId());
			tunnel.setASiteId(dialog.getPortInst_a().getSiteId());
			tunnel.setDirection(dialog.getjTextArea1().getText());
			tunnel.setTunnelStatus(dialog.getjCheckBox1().isSelected() == true ? EActiveStatus.ACTIVITY.getValue() : EActiveStatus.UNACTIVITY.getValue());
			this.setBeforeName(tunnel.getTunnelName());
			tunnel.setTunnelName(dialog.getjTextField1().getText());
			tunnel.setZPortId(dialog.getPortInst_z().getPortId());
			tunnel.setZSiteId(dialog.getPortInst_z().getSiteId());
			tunnel.setIsReverse(dialog.getjCheckBox2().isSelected() == true ? 1 : 0);
			tunnel.setCreateUser(ConstantUtil.user.getUser_Name());
			tunnel.setLspParticularList(lspParticularList);
			tunnel.setTunnelType(code_type.getId() + "");
			tunnel.setPosition(1);
			tunnel.getLspParticularList().get(0).setPosition(1);
			tunnel.setInBandwidthControl(dialog.getInBandwidthControlCheckBox().isSelected()?1:0);
			tunnel.setOutBandwidthControl(dialog.getOutBandwidthControlCheckBox().isSelected()?1:0);
			if (tunnel.getaOutVlanValue() == 0) {
				tunnel.setaOutVlanValue(2);
			}
			if (tunnel.getzOutVlanValue() == 0) {
				tunnel.setzOutVlanValue(2);
			}
			tunnel.setSourceMac(dialog.getSourceMacText().getText().trim());
			tunnel.setEndMac(dialog.getEndMacText().getText().trim());
			if (dialog.getTxtWaitTime().getTxtData().trim().length() > 0) {
				tunnel.setWaittime(Integer.parseInt(dialog.getTxtWaitTime().getTxtData()));
			}

			if (dialog.getTxtDelayTime().getTxtData().trim().length() > 0) {
				tunnel.setDelaytime(Integer.parseInt(dialog.getTxtDelayTime().getTxtData()));
			}

			tunnel.setApsenable(dialog.getChkAps().isSelected() == true ? 1 : 0);
			tunnel.setProtectBack(dialog.getProtectBack().isSelected() == true ? 0 : 1);
			tunnel.setRotateWay(dialog.getCmbRotateWay().getSelectedItem().toString());
			tunnel.setRotateLocation(dialog.getCmbRotateLocation().getSelectedItem().toString());
			tunnel.setRotateMode(dialog.getCmbRotateMode().getSelectedItem().toString());
			tunnel.setTnpLayer(Integer.parseInt(dialog.getSpinnerTnpLayer().getTxtData()));
			tunnel.setRotateThreshold(Integer.parseInt(dialog.getSpinnerRotateThreshold().getTxtData()));
			// 设置保护路由
			if ("2".equals(code_type.getCodeValue()) || "3".equals(code_type.getCodeValue())) {
				this.getProtectTunnel(tunnel, proSegments, dialog);
				this.getOutVlan(tunnel.getProtectTunnel(), dialog);
			}

			if (tunnel.getTunnelId() > 0) {
				if ("1".equals(code_type.getCodeValue())) {
					tunnel.setOamList(dialog.getOamList());
				} else if ("2".equals(code_type.getCodeValue()) || "3".equals(code_type.getCodeValue())) {
					tunnel.getProtectTunnel().setOamList(dialog.getOamList_protect());
				}
			}
			if (dialog.getjCheckBoxSNCP().isSelected()) {
				// 给tunnel的sncpID和对应的网元id赋值
				setSNCPids(tunnel, dialog);
			} else {
				tunnel.setSncpIds(null);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			lspParticularList = null;
			qosList = null;
			Segments = null;
			proSegments = null;
		}
		return tunnel;
	}

	private void getOutVlan(Tunnel tunnel, AddTunnelPathDialog dialog) {
		tunnel.setSourceMac(dialog.getSourceMacText_backup().getText().trim());
		tunnel.setEndMac(dialog.getEndMacText_backup().getText().trim());
		if (tunnel.getaOutVlanValue() == 0) {
			tunnel.setaOutVlanValue(2);
		}
		if (tunnel.getzOutVlanValue() == 0) {
			tunnel.setzOutVlanValue(2);
		}
	}

	/**
	 * 给tunnel的sncpID和对应的网元id赋值
	 * 
	 * @param tunnel
	 * @param dialog
	 */
	public void setSNCPids(Tunnel tunnel, AddTunnelPathDialog dialog) {
		Tunnel tunnel2 = null;
		Tunnel sncpTunnel = null;
		TunnelService_MB tunnelServiceMB = null;
		try {
			tunnel2 = (Tunnel) ((ControlKeyValue) dialog.getjComboBoxSNCP().getSelectedItem()).getObject();
			sncpTunnel = new Tunnel();
			sncpTunnel.setTunnelId(tunnel2.getProtectTunnelId());
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			sncpTunnel = tunnelServiceMB.selectNodeByTunnelId(sncpTunnel).get(0);
			tunnel.setSncpIds(sncpTunnel.getASiteId() + "/" + sncpTunnel.getAprotectId() + "/" + sncpTunnel.getZSiteId() + "/" + sncpTunnel.getZprotectId());
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			tunnel2 = null;
			sncpTunnel = null;
			UiUtil.closeService_MB(tunnelServiceMB);
		}
	}

	/*
	 * 创建Tunnel
	 */
	public void createTunnel(ActionEvent evt, AddTunnelPathDialog dialog) {
		Tunnel tunnel = null;
		try {
			tunnel = this.getTunnel(dialog);
			String tunnelName = tunnel.getTunnelName();
			DispatchUtil tunnelOperationService = new DispatchUtil(RmiKeys.RMI_TUNNEL);
			String message = "";
			if (dialog.isCreate()) {
				int createNum = Integer.parseInt(dialog.getPtnSpinnerNumber().getValue().toString());
				if (createNum == 1) {
					List<Tunnel> tunnelList = new ArrayList<Tunnel>();
					tunnelList.add(tunnel);
					message = tunnelOperationService.excuteInsert(tunnelList);
					if(tunnel.getProtectTunnel() != null){
						tunnel.getProtectTunnel().setTunnelType(tunnel.getTunnelType());
						this.insertLog(dialog.getBtnSave(), EOperationLogType.TUNNELINSERT.getValue(), message, null, tunnel.getProtectTunnel());
					}
					tunnel.setProtectTunnel(null);
					this.insertLog(dialog.getBtnSave(), EOperationLogType.TUNNELINSERT.getValue(), message, null, tunnel);
				} else {
					message = this.batchCreate(tunnel, createNum, tunnelOperationService, dialog);
				}
				//如果被选中批量创建pw，并且创建tunnel成功之后，才能创建pw
				if(dialog.getJcbPW().isSelected()){
					if(message.contains(ResourceUtil.srcStr(StringKeysBtn.BTN_EXPORT_ISUCCESS))){
						tunnel.setTunnelName(tunnelName);
						this.buildPWBatch(createNum, tunnel, dialog.getBtnSave());
					}
				}
			} else {
				Tunnel tunnel_before = this.getTunnelBefore(tunnel.getTunnelId());
				message = tunnelOperationService.excuteUpdate(tunnel);
				// 添加日志记录
				if(tunnel.getProtectTunnel() != null){
					tunnel.getProtectTunnel().setTunnelType(tunnel.getTunnelType());
					tunnel_before.getProtectTunnel().setTunnelType(tunnel.getTunnelType());
					this.insertLog(dialog.getBtnSave(), EOperationLogType.TUNNELUPDATE.getValue(), message, tunnel_before.getProtectTunnel(), tunnel.getProtectTunnel());
				}
				tunnel.setProtectTunnel(null);
				tunnel_before.setProtectTunnel(null);
				this.insertLog(dialog.getBtnSave(), EOperationLogType.TUNNELUPDATE.getValue(), message, tunnel_before, tunnel);
			}

			DialogBoxUtil.succeedDialog(dialog, message);
			dialog.getTunneBusinessPanel().getController().refresh();
			dialog.dispose();
		} catch (NumberFormatException e) {
			ExceptionManage.dispose(e, this.getClass());
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			tunnel = null;
		}
	}
	
	private Tunnel getTunnelBefore(int tunnelId) {
		TunnelService_MB service = null;
		try {
			service = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			return service.selectId(tunnelId);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
		return null;
	}

	private void insertLog(PtnButton ptnButton, int operationType, String message, Tunnel tunnelBefore, Tunnel tunnel){
		List<Integer> siteIdList = new ArrayList<Integer>();
		SiteService_MB siteService = null;
		PortService_MB portService = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			if(tunnelBefore != null){
				tunnelBefore.setShowSiteAname(siteService.getSiteName(tunnelBefore.getaSiteId()));
				tunnelBefore.setShowSiteZname(siteService.getSiteName(tunnelBefore.getzSiteId()));
				for(Lsp lsp : tunnelBefore.getLspParticularList()){
					lsp.setaSiteName(siteService.getSiteName(lsp.getASiteId()));
					lsp.setzSiteName(siteService.getSiteName(lsp.getZSiteId()));
					lsp.setaPortName(portService.getPortname(lsp.getAPortId()));
					lsp.setzPortName(portService.getPortname(lsp.getZPortId()));
				}
				this.getOamSiteName(tunnelBefore, siteService);
			}
			tunnel.setShowSiteAname(siteService.getSiteName(tunnel.getaSiteId()));
			tunnel.setShowSiteZname(siteService.getSiteName(tunnel.getzSiteId()));
			this.getOamSiteName(tunnel, siteService);
			for(Lsp lsp : tunnel.getLspParticularList()){
				lsp.setaSiteName(siteService.getSiteName(lsp.getASiteId()));
				lsp.setzSiteName(siteService.getSiteName(lsp.getZSiteId()));
				lsp.setaPortName(portService.getPortname(lsp.getAPortId()));
				lsp.setzPortName(portService.getPortname(lsp.getZPortId()));
			}
			for(Lsp lsp : tunnel.getLspParticularList()){
				if(!siteIdList.contains(lsp.getASiteId())){
					siteIdList.add(lsp.getASiteId());
					AddOperateLog.insertOperLog(ptnButton, operationType, message, tunnelBefore, tunnel, lsp.getASiteId(), tunnel.getTunnelName(), "Tunnel");
				}
				if(!siteIdList.contains(lsp.getZSiteId())){
					siteIdList.add(lsp.getZSiteId());
					AddOperateLog.insertOperLog(ptnButton, operationType, message, tunnelBefore, tunnel, lsp.getZSiteId(), tunnel.getTunnelName(), "Tunnel");
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(siteService);
			UiUtil.closeService_MB(portService);
		}
	}
	
	private void getOamSiteName(Tunnel tunnel, SiteService_MB siteService){
		List<OamInfo> oamList = tunnel.getOamList();
		if(oamList != null && oamList.size() > 0){
			for (OamInfo oamInfo : oamList) {
				if(oamInfo.getOamMep() != null){
					if(oamInfo.getOamMep().getSiteId() == tunnel.getASiteId()){
						oamInfo.getOamMep().setSiteName(tunnel.getShowSiteAname());
					}else if(oamInfo.getOamMep().getSiteId() == tunnel.getZSiteId()){
						oamInfo.getOamMep().setSiteName(tunnel.getShowSiteZname());
					}
				}
				if(oamInfo.getOamMip() != null){
					oamInfo.getOamMip().setSiteName(siteService.getSiteName(oamInfo.getOamMip().getSiteId()));
				}
			}
		}
	}
	
	/**
	 * 批量创建pw
	 * @param tunnel 
	 * @param createNum 批量创建pw的数量
	 * @param saveBtn 
	 */
	private String buildPWBatch(int createNum, Tunnel tunnel, PtnButton saveBtn) {
		SiteService_MB siteServiceMB = null;
		TunnelService_MB tunnelServiceMB = null;
		PwInfoService_MB pwService = null;
		try {
			siteServiceMB = (SiteService_MB)ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			List<Integer> tIdList = new ArrayList<Integer>();
			for (int i = 0; i < createNum; i++) {
				if(i == 0){
					Tunnel firstTunnel = tunnelServiceMB.queryTunnelByName(tunnel.getTunnelName());
					if(firstTunnel != null){
						tIdList.add(firstTunnel.getTunnelId());
					}
				}else{
					Tunnel t = tunnelServiceMB.queryTunnelByName(tunnel.getTunnelName()+"_Copy"+i);
					if(t != null){
						tIdList.add(t.getTunnelId());
					}
				}
			}
			String aIP = siteServiceMB.getSiteID(tunnel.getaSiteId());
			String zIP = siteServiceMB.getSiteID(tunnel.getzSiteId());
			List<PwInfo> pwInfoList = new ArrayList<PwInfo>();
			if(tIdList.size() >= createNum){
				for(int i = 1; i <= createNum; i++){
					PwInfo pwInfo = new PwInfo();
					pwInfo.setASiteId(tunnel.getaSiteId());
					pwInfo.setZSiteId(tunnel.getzSiteId());
					pwInfo.setQosList(tunnel.getQosList());
					pwInfo.setAoppositeId(aIP);
					pwInfo.setZoppositeId(zIP);
					pwInfo.setInlabelValue(0);
					pwInfo.setOutlabelValue(0);
					pwInfo.setTunnelId(tIdList.get(i-1));
					pwInfo.setPayload(UiUtil.getCodeByValue("PAYLOAD", "2").getId());
					pwInfo.setIsSingle(0);
					pwInfo.setQosModel(0);//模式
					pwInfo.setType(EPwType.ETH);
					pwInfo.setCreateTime(DateUtil.getDate(DateUtil.FULLTIME));
					pwInfo.setCreateUser(ConstantUtil.user.getUser_Name());
					pwInfo.setPwStatus(tunnel.getTunnelStatus());
					pwInfo.setBusinessType(0+"");//业务类型为普通
					pwInfo.setaSourceMac("00-00-00-33-44-55");
					pwInfo.setAtargetMac("00-00-00-AA-BB-CC");
					pwInfo.setZtargetMac("00-00-00-33-44-55");
					pwInfo.setzSourceMac("00-00-00-AA-BB-CC");
					pwInfo.setPwName(pwInfo.getType().toString()+"/"+System.currentTimeMillis()+"_Copy" + i);
					pwInfoList.add(pwInfo);
				}
				DispatchUtil pwOperationImpl = new DispatchUtil(RmiKeys.RMI_PW);
				String result = pwOperationImpl.excuteInsert(pwInfoList);
				pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
				for (PwInfo pwInfo : pwInfoList) {
					if(result.contains(ResultString.CONFIG_SUCCESS)){
						pwInfo = pwService.selectBypwid_notjoin(pwInfo);
					}
					this.insertPwOpeLog(saveBtn, EOperationLogType.PWINSERT.getValue(), result, null, pwInfo);
				}
				return result;
			}else{
				return ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_FAIL);
			}
			
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}finally {
			UiUtil.closeService_MB(pwService);
			UiUtil.closeService_MB(siteServiceMB);
			UiUtil.closeService_MB(tunnelServiceMB);
		}
		return ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_FAIL);
	}
	
	private void insertPwOpeLog(PtnButton Confirm, int operationType, String result, PwInfo oldPw, PwInfo newPw){
		AddOperateLog.insertOperLog(Confirm, operationType, result, oldPw, newPw, newPw.getASiteId(), newPw.getPwName(), "pwInfo");
		AddOperateLog.insertOperLog(Confirm, operationType, result, oldPw, newPw, newPw.getZSiteId(), newPw.getPwName(), "pwInfo");
		if(newPw.getMsPwInfos() != null && newPw.getMsPwInfos().size() > 0){
			for (MsPwInfo msPw : newPw.getMsPwInfos()) {
				AddOperateLog.insertOperLog(Confirm, operationType, result, oldPw, newPw, msPw.getSiteId(), newPw.getPwName(), "pwInfo");
			}
		}
	}

	/**
	 * 给lsp的前后向标签赋值
	 * 
	 * @param lsPfromRoute
	 * @return
	 * @throws Exception
	 */
	public List<Lsp> setLabelToLsp(List<Lsp> lsPfromRoute, List<Integer[][]> labelList) throws Exception {
		int frontLabel = 0;
		int backLabel = 0;
		try {
			if (lsPfromRoute.size() > 0 && labelList.size() > 0) {
				for (int i = 0; i < lsPfromRoute.size(); i++) {
					frontLabel = labelList.get(i)[0][1];
					backLabel = labelList.get(i)[1][1];
					lsPfromRoute.get(i).setFrontLabelValue(frontLabel);
					lsPfromRoute.get(i).setBackLabelValue(backLabel);
					lsPfromRoute.get(i).setSourceMac("00-00-00-00-00-00");
					lsPfromRoute.get(i).setTargetMac("00-00-00-00-00-00");
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			frontLabel = 0;
			backLabel = 0;
		}
		return lsPfromRoute;
	}

	/**
	 * 获取保护tunnel
	 * 
	 * @author kk
	 * @param dialog
	 * 
	 * @param
	 * 
	 * @return
	 * 
	 * @Exception 异常对象
	 */
	public void getProtectTunnel(Tunnel jobTunnel, List<Segment> Segments, AddTunnelPathDialog dialog) throws Exception {
		int ASiteId = 0;
		int ZSiteId = 0;
		int APortId = 0;
		int ZPortId = 0;
		if (jobTunnel.getProtectTunnelId() == 0) {
			// List <Segment> Segments =
			// AddTunnelPathDialog.list.get(AddTunnelPathDialog.secondIndex);
			Segment sgA = Segments.get(0);
			Segment sgZ = Segments.get(Segments.size() - 1);

			if (sgA.getASITEID() == dialog.getEquipmentTopology().getSiteA().getSite_Inst_Id()) {
				ASiteId = sgA.getASITEID();
				APortId = sgA.getAPORTID();
			} else {
				ASiteId = sgA.getZSITEID();
				APortId = sgA.getZPORTID();
			}

			if (sgZ.getASITEID() == dialog.getEquipmentTopology().getSiteZ().getSite_Inst_Id()) {
				ZSiteId = sgZ.getASITEID();
				ZPortId = sgZ.getAPORTID();
			} else {
				ZSiteId = sgZ.getZSITEID();
				ZPortId = sgZ.getZPORTID();
			}
		}
		// 新建tunnel
		if (jobTunnel.getTunnelId() == 0) {
			Tunnel tunnel = jobTunnel.getProtectTunnel();
			if (tunnel == null) {
				tunnel = new Tunnel();
			}
			tunnel.setTunnelStatus(jobTunnel.getTunnelStatus());
			tunnel.setTunnelName(jobTunnel.getTunnelName()+"_protect");
			tunnel.setZSiteId(ZSiteId);
			tunnel.setASiteId(ASiteId);
			tunnel.setLspParticularList(setLabelToLsp(getLSPfromRoute(Segments, dialog), dialog.getLabelProtList()));
			tunnel.setPosition(0);
			tunnel.setQosList(jobTunnel.getQosList());
			tunnel.setTunnelType("0");
			tunnel.setAPortId(APortId);
			tunnel.setZPortId(ZPortId);
			tunnel.setOamList(dialog.getOamList_protect());
			tunnel.getLspParticularList().get(0).setPosition(0);
			jobTunnel.setProtectTunnel(tunnel);
			tunnel.setCreateUser(ConstantUtil.user.getUser_Name());
			tunnel.setRotateWay(dialog.getCmbRotateWay().getSelectedItem().toString());
			tunnel.setRotateLocation(dialog.getCmbRotateLocation().getSelectedItem().toString());
			tunnel.setRotateMode(dialog.getCmbRotateMode().getSelectedItem().toString());
			tunnel.setTnpLayer(Integer.parseInt(dialog.getSpinnerTnpLayer().getTxtData()));
			tunnel.setRotateThreshold(Integer.parseInt(dialog.getSpinnerRotateThreshold().getTxtData()));
		}
		// 修改tunnel
		else if (jobTunnel.getTunnelId() > 0) {
			Tunnel tunnel = jobTunnel.getProtectTunnel();
			// 如果是将普通类型修改成1:1类型，此时要创建保护tunnel
			if (tunnel == null) {
				tunnel = new Tunnel();
				tunnel.setZSiteId(ZSiteId);
				tunnel.setASiteId(ASiteId);
				tunnel.setLspParticularList(setLabelToLsp(getLSPfromRoute(Segments, dialog), dialog.getLabelProtList()));
				tunnel.setPosition(0);
				tunnel.setQosList(jobTunnel.getQosList());
				tunnel.setTunnelType("0");
				tunnel.setAPortId(APortId);
				tunnel.setZPortId(ZPortId);
				tunnel.setOamList(dialog.getOamList_protect());
				tunnel.getLspParticularList().get(0).setPosition(0);
			}
			tunnel.setRotateWay(dialog.getCmbRotateWay().getSelectedItem().toString());
			tunnel.setRotateLocation(dialog.getCmbRotateLocation().getSelectedItem().toString());
			tunnel.setRotateMode(dialog.getCmbRotateMode().getSelectedItem().toString());
			tunnel.setTnpLayer(Integer.parseInt(dialog.getSpinnerTnpLayer().getTxtData()));
			tunnel.setRotateThreshold(Integer.parseInt(dialog.getSpinnerRotateThreshold().getTxtData()));
			tunnel.setTunnelStatus(jobTunnel.getTunnelStatus());
			tunnel.setTunnelName(jobTunnel.getTunnelName()+"_protect");
			tunnel.setOamList(dialog.getOamList_protect());
			tunnel.setLspParticularList(setLabelToLsp(tunnel.getLspParticularList(), dialog.getLabelProtList()));
			jobTunnel.setProtectTunnel(tunnel);
		}
		// }
	}

	// 验证qos是否足够-- 创建tunnel 验证端口的带宽
	private boolean checkQosIsEnough(Tunnel tunnel, AddTunnelPathDialog dialog) {
		int aPortId = tunnel.getAPortId();
		int aSiteId = tunnel.getASiteId();
		int zPortId = tunnel.getZPortId();
		int zSiteId = tunnel.getZSiteId();
		List<QosInfo> qosList = null;
		List<QosQueue> qosQueueList = null;
		List<QosInfo> tunnelUsedqosList = null;
		List<Tunnel> tunnelList = null;
		QosQueue qosQueue = null;
		Map<Integer, Integer> qosPreMap = null;
		Map<Integer, Integer> qosNextMap = null;
		TunnelService_MB tunnelServiceMB = null;
		QosInfoService_MB qosInfoServiceMB = null;
		QosQueueService_MB qosQueueServiceMB = null;
		try {
			qosInfoServiceMB = (QosInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosInfo);
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			qosQueueServiceMB = (QosQueueService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosQueue);
			qosList = new ArrayList<QosInfo>();
			qosPreMap = new HashMap<Integer, Integer>();
			qosNextMap = new HashMap<Integer, Integer>();
			qosQueueList = new ArrayList<QosQueue>();
			tunnelList = new ArrayList<Tunnel>();
			qosList = dialog.getQosList();
			// 测a端的qos
			qosQueue = new QosQueue();
			qosQueue.setObjType(EServiceType.SECTION.toString());
			qosQueue.setObjId(aPortId);
			qosQueue.setQueueType("");
			qosQueue.setSiteId(aSiteId);
			qosQueueList = qosQueueServiceMB.queryByCondition(qosQueue);
			for (QosQueue qos : qosQueueList) {
				qosNextMap.put(qos.getCos(), qos.getCir());
				qosPreMap.put(qos.getCos(), qos.getCir());
			}

			// a端端口上所有在该端口上的tunnel所使用的qos
			int used = 0;
			int use = 0;
			tunnelList = tunnelServiceMB.select();
			if (tunnelList.size() > 0) {
				for (Tunnel t : tunnelList) {
					if (t.getAPortId() == tunnel.getAPortId() && t.getASiteId() == tunnel.getASiteId()) {
						tunnelUsedqosList = new ArrayList<QosInfo>();
						tunnelUsedqosList = qosInfoServiceMB.getQosByObj(EServiceType.TUNNEL.toString(), t.getTunnelId());
						for (QosInfo qos : tunnelUsedqosList) {
							if (Integer.parseInt(qos.getDirection()) == EQosDirection.FORWARD.getValue()) {
								used = qos.getCir();
								use = qosPreMap.get(qos.getCos());
								qosPreMap.put(qos.getCos(), use - used);
							}
							if (Integer.parseInt(qos.getDirection()) == EQosDirection.BACKWARD.getValue()) {
								used = qos.getCir();
								use = qosNextMap.get(qos.getCos());
								qosNextMap.put(qos.getCos(), use - used);
							}
						}
					}
				}
			}
			for (QosInfo qos : qosList) {

				if (Integer.parseInt(qos.getDirection()) == EQosDirection.FORWARD.getValue()) {
					if (qosPreMap.get(qos.getCos()) != null) {
						if (qosPreMap.get(qos.getCos()) < qos.getCir()) {
							return true;
						}
					}
				}
				if (Integer.parseInt(qos.getDirection()) == EQosDirection.BACKWARD.getValue()) {
					if (qosNextMap.get(qos.getCos()) != null) {
						if (qosNextMap.get(qos.getCos()) < qos.getCir()) {
							return true;
						}
					}
				}
			}
			// 测z端的qos
			qosQueue = new QosQueue();
			qosQueue.setObjType(EServiceType.SECTION.toString());
			qosQueue.setObjId(zPortId);
			qosQueue.setQueueType("");
			qosQueue.setSiteId(zSiteId);
			qosQueueList = qosQueueServiceMB.queryByCondition(qosQueue);
			qosPreMap.clear();
			qosNextMap.clear();
			for (QosQueue qos : qosQueueList) {
				qosNextMap.put(qos.getCos(), qos.getCir());
				qosPreMap.put(qos.getCos(), qos.getCir());
			}
			// z端端口上所有在该端口上的tunnel所使用的qos
			if (tunnelList.size() > 0) {
				for (Tunnel t : tunnelList) {
					if (t.getZPortId() == tunnel.getZPortId() && t.getZSiteId() == tunnel.getZSiteId()) {
						tunnelUsedqosList = new ArrayList<QosInfo>();
						tunnelUsedqosList = qosInfoServiceMB.getQosByObj(EServiceType.TUNNEL.toString(), t.getTunnelId());
						for (QosInfo qos : tunnelUsedqosList) {
							if (Integer.parseInt(qos.getDirection()) == EQosDirection.FORWARD.getValue()) {
								used = qos.getCir();
								use = qosPreMap.get(qos.getCos());
								qosPreMap.put(qos.getCos(), use - used);
							}
							if (Integer.parseInt(qos.getDirection()) == EQosDirection.BACKWARD.getValue()) {
								used = qos.getCir();
								use = qosNextMap.get(qos.getCos());
								qosNextMap.put(qos.getCos(), use - used);
							}
						}
					}
				}
			}
			for (QosInfo qos : qosList) {
				if (Integer.parseInt(qos.getDirection()) == EQosDirection.FORWARD.getValue()) {
					if (qosPreMap.get(qos.getCos()) != null) {
						if (qosPreMap.get(qos.getCos()) < qos.getCir()) {
							return true;
						}
					}
				}
				if (Integer.parseInt(qos.getDirection()) == EQosDirection.BACKWARD.getValue()) {
					if (qosNextMap.get(qos.getCos()) != null) {
						if (qosNextMap.get(qos.getCos()) < qos.getCir()) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(tunnelServiceMB);
			UiUtil.closeService_MB(qosInfoServiceMB);
			UiUtil.closeService_MB(qosQueueServiceMB);
		}
		return false;
	}

	public void closeDialog(AddTunnelPathDialog dialog) {
		dialog.dispose();
	}

	public void tunnelData(ProtectTunnelDialg dialog) throws Exception {
		Tunnel tunnel = null;
		// List<Tunnel> tunnelList = null;
		List<Tunnel> allTunnelList = null;
		DefaultComboBoxModel defaultComboBoxModel = null;
		TunnelService_MB tunnelServiceMB = null;
		try {
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			tunnel = new Tunnel();
			tunnel.setASiteId(dialog.getTunnelDialog().getPortInst_a().getSiteId());
			tunnel.setZSiteId(dialog.getTunnelDialog().getPortInst_z().getSiteId());
			allTunnelList = tunnelServiceMB.select(tunnel);
			/*
			 * 过滤掉自身
			 */
			// tunnelList = new ArrayList<Tunnel>();
			// if (!AddTunnelPathDialog.getDialog().isCreate()) {
			// Tunnel alreadyTunnel =
			// AddTunnelPathDialog.getDialog().getTunnel();
			// for (Tunnel t : allTunnelList) {
			// if (alreadyTunnel.getTunnelId() != t.getTunnelId()) {
			// tunnelList.add(t);
			// }
			// }
			// }
			defaultComboBoxModel = (DefaultComboBoxModel) dialog.getjComboBox2().getModel();
			defaultComboBoxModel.addElement(new ControlKeyValue("0", ""));
			for (int i = 0; i < allTunnelList.size(); i++) {
				defaultComboBoxModel.addElement(new ControlKeyValue(allTunnelList.get(i).getTunnelId() + "", allTunnelList.get(i).getTunnelName()));
			}
			dialog.getjComboBox2().setModel(defaultComboBoxModel);

		} catch (Exception e) {
			throw e;
		} finally {
			tunnel = null;
			// tunnelList = null;
			defaultComboBoxModel = null;
			UiUtil.closeService_MB(tunnelServiceMB);
		}
	}

	/*
	 * 选择保护Tunnel
	 */
	public void chooseTunnel(ItemEvent evt, ProtectTunnelDialg dialog) {
		if (evt.getStateChange() == 1) {

			try {
				ControlKeyValue controlKeyValue = (ControlKeyValue) evt.getItem();
				dialog.getTunnelTopoPanel().boxData(Integer.parseInt(controlKeyValue.getId()));

			} catch (NumberFormatException e) {
				ExceptionManage.dispose(e, this.getClass());
			} catch (Exception e) {
				ExceptionManage.dispose(e, this.getClass());
			}
		}
	}

	/*
	 * 确定选择保护Tunnel
	 */
	public void confirmTunnelPro(ActionEvent evt, ProtectTunnelDialg dialog) {
		if (dialog.getjComboBox2().getSelectedIndex() == 0) {
			DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_NOT_FULL));
			return;
		}
		ControlKeyValue controlType = null;
		ControlKeyValue controlTunnel = null;
		AddTunnelPathDialog addTunnelPathDialog = null;
		try {
			controlType = (ControlKeyValue) dialog.getjComboBox1().getSelectedItem();
			controlTunnel = (ControlKeyValue) dialog.getjComboBox2().getSelectedItem();
			addTunnelPathDialog = dialog.getTunnelDialog();
			addTunnelPathDialog.getTunnel().setProtectTunnelId(Integer.parseInt(controlTunnel.getId()));
			addTunnelPathDialog.getTunnel().setProtectTunnelName(controlTunnel.getName());
			addTunnelPathDialog.getTunnel().setProtectType(Integer.parseInt(controlType.getId()));
			addTunnelPathDialog.setProtectTunnelName(controlTunnel.getName());
			// 添加保护段
			SegmentTopoPanel segmentTop = dialog.getTunnelDialog().getEquipmentTopology().getSegmentTopo();
			TDataBox box = segmentTop.getBox();
			addProtectLink(addTunnelPathDialog, box);
			dialog.dispose();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			controlType = null;
			controlTunnel = null;
			addTunnelPathDialog = null;
		}

	}

	// 创建保护段
	@SuppressWarnings("unchecked")
	private void addProtectLink(AddTunnelPathDialog addTunnelPathDialog, TDataBox box) {
		Link link = null;
		int protectId = 0;
		Tunnel tunnel = null;
		List<Tunnel> tunnelList = null;
		TunnelService_MB service = null;
		// key为起点网元id，value为对端网元id
		Map<Integer, Integer> siteIdMap = new HashMap<Integer, Integer>();
		int asiteId = 0;
		int zsiteId = 0;
		try {
			Node anode = null;
			Node znode = null;
			asiteId = addTunnelPathDialog.getPortInst_a().getSiteId();
			zsiteId = addTunnelPathDialog.getPortInst_z().getSiteId();
			service = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			List<Element> elements = box.getAllElements();
			protectId = addTunnelPathDialog.getTunnel().getProtectTunnelId();
			tunnel = new Tunnel();
			tunnel.setTunnelId(protectId);
			tunnelList = service.select(tunnel);
			if (tunnelList != null && tunnelList.size() > 0) {
				tunnel = tunnelList.get(0);
				getLspSiteMap(siteIdMap, tunnel, asiteId, zsiteId);
			}
			for (Map.Entry<Integer, Integer> entrySet : siteIdMap.entrySet()) {
				asiteId = entrySet.getKey();
				zsiteId = entrySet.getValue();
				for (Element element : elements) {
					if (element instanceof Node) {
						if (((SiteInst) element.getUserObject()).getSite_Inst_Id() == asiteId) {
							anode = (Node) element;
						} else if (((SiteInst) element.getUserObject()).getSite_Inst_Id() == zsiteId) {
							znode = (Node) element;
						}
					} else if (element instanceof Link) {
						Link boxLink = (Link) element;
						// 判断是否已经配置了保护
						if (boxLink.getFrom() == anode && boxLink.getTo() == znode && boxLink.getBusinessObject() != null && boxLink.getBusinessObject().equals("protected")) {
							box.removeElement(element);
						}
					}
				}
				link = new Link();
				link.setFrom(anode);
				link.setTo(znode);
				link.putLinkBundleExpand(false);
				link.putLinkBundleSize(1);
				StringBuilder str = new StringBuilder();
				str.append("From : ").append(anode.getName()).append("/").append(addTunnelPathDialog.getPortInst_a()).append("<----->To :").append(znode.getName()).append("/").append(addTunnelPathDialog.getPortInst_z()).append("</html>");
				link.setLinkType(TWaverConst.LINE_TYPE_DEFAULT);
				link.setBundleExpand(true);
				link.putLinkColor(Color.red);
				link.putLinkOutlineColor(Color.white);
				link.putLinkWidth(5);
				link.setUserObject(getSegment(addTunnelPathDialog));
				link.setBusinessObject("protected");
				box.addElement(link);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
	}

	private Segment getSegment(AddTunnelPathDialog addTunnelPathDialog) {
		SegmentService_MB service = null;
		List<Segment> segmentList = null;
		Segment segment = null;
		try {
			service = (SegmentService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SEGMENT);
			segmentList = service.select();
			for (Segment seg : segmentList) {
				if (seg.getAPORTID() == addTunnelPathDialog.getPortInst_a().getPortId() && seg.getZPORTID() == addTunnelPathDialog.getPortInst_z().getPortId() && seg.getASITEID() == addTunnelPathDialog.getPortInst_a().getSiteId() && seg.getZSITEID() == addTunnelPathDialog.getPortInst_z().getSiteId()) {
					segment = seg;
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
		return segment;

	}

	/**
	 * 递归将网元存放在map中 key为起点网元的id，value为对端网元id
	 * 
	 * @param tunneId
	 * @return
	 */
	public void getLspSiteMap(Map<Integer, Integer> siteIdMap, Tunnel tunnel, int aSiteId, int zSiteId) {
		// key为起点网元id，value为对端网元id
		try {
			if (tunnel.getASiteId() == aSiteId) {
				// 找到起点网元
				if (aSiteId == zSiteId) {
					return;
				}
				List<Lsp> partList = tunnel.getLspParticularList();
				getPartSiteMap(siteIdMap, partList, aSiteId, zSiteId);
				getLspSiteMap(siteIdMap, tunnel, tunnel.getZSiteId(), zSiteId);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {

		}
	}

	/**
	 * 根据tunnelId获取中间网元的id的Map
	 * 
	 * @param tunneId
	 * @return
	 */
	public void getPartSiteMap(Map<Integer, Integer> siteIdMap, List<Lsp> partList, int aSiteId, int zSiteId) {
		try {
			// 找到起点网元
			for (Lsp part : partList) {
				if (part.getASiteId() == aSiteId) {
					if (aSiteId == zSiteId) {
						return;
					}
					siteIdMap.put(part.getASiteId(), part.getZSiteId());
					getPartSiteMap(siteIdMap, partList, part.getZSiteId(), zSiteId);
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {

		}
	}

	/**
	 * 批量创建TUNNEL
	 * 
	 * @author kk
	 * @param tunnel
	 *            tunnel对象
	 * @param number
	 *            创建数量
	 * @param dialog 
	 * @return 创建结果，几条成功，几条失败
	 * @throws Exception
	 */
	private String batchCreate(Tunnel tunnel, int number, DispatchUtil tunnelDispatch, AddTunnelPathDialog dialog) throws Exception {
		String tunnelName = null;
		String result = null;
		TunnelService_MB service = null;
		try {
			service = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			List<Tunnel> tunnelList = new ArrayList<Tunnel>();
			tunnelName = tunnel.getTunnelName();
			tunnelList.add(tunnel);
			for (int i = 2; i <= number; i++) {
				// 如果是不是第一次循环。 把tunnel主键，标签，businessId全部置为0
				if (i > 1) {
					Tunnel batchTunnel = (Tunnel) this.copy(tunnel);
					batchTunnel.setTunnelName(tunnelName + "_Copy" + (i - 1));
					batchTunnel.setTunnelId(0);
					for (Lsp lsp : batchTunnel.getLspParticularList()) {
						lsp.setFrontLabelValue(0);
						lsp.setBackLabelValue(0);
						lsp.setAtunnelbusinessid(0);
						lsp.setZtunnelbusinessid(0);
					}

					// 如果是保护，把标签置为0
					if ("2".equals(UiUtil.getCodeById(Integer.parseInt(batchTunnel.getTunnelType())).getCodeValue())) {
						batchTunnel.getProtectTunnel().setTunnelId(0);
						for (Lsp lsp : batchTunnel.getProtectTunnel().getLspParticularList()) {
							lsp.setFrontLabelValue(0);
							lsp.setBackLabelValue(0);
							lsp.setAtunnelbusinessid(0);
							lsp.setZtunnelbusinessid(0);
						}
					}
					for (QosInfo qosInfo : batchTunnel.getQosList()) {
						qosInfo.setQosname(null);
					}
					tunnelList.add(batchTunnel);
				}
			}
			//一次下发
			result = tunnelDispatch.excuteInsert(tunnelList);
			for (Tunnel tunnelBatch : tunnelList) {
				String tunnelNameBefore = tunnelBatch.getTunnelName();
				if(result.contains(ResultString.CONFIG_SUCCESS)){
					List<Integer> tunnelIdList = service.selectTunnelIdByTunnelName(tunnelBatch.getTunnelName());
					for (Integer tunnelId : tunnelIdList) {
						tunnelBatch = service.selectId(tunnelId);
						if(tunnelBatch.getTunnelName().equals(tunnelNameBefore)){
							break;
						}
					}
				}
				if(tunnelBatch.getProtectTunnel() != null){
					tunnelBatch.getProtectTunnel().setTunnelType(tunnelBatch.getTunnelType());
					this.insertLog(dialog.getBtnSave(), EOperationLogType.TUNNELINSERT.getValue(), result, null, tunnelBatch.getProtectTunnel());
				}
				tunnelBatch.setProtectTunnel(null);
				this.insertLog(dialog.getBtnSave(), EOperationLogType.TUNNELINSERT.getValue(), result, null, tunnelBatch);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(service);
		}
		return result;
	}
	
	/**
	 * 对象深度复制
	 */
	private Object copy(Tunnel tunnnel) throws IOException, ClassNotFoundException{
	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
	   ObjectOutputStream oos = new ObjectOutputStream(bos);
	   oos.writeObject(tunnnel);
	   ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
	   return ois.readObject();
	}

	/**
	 * 验证tunnel如果是SNCP类型，tunnel中是否包含晨晓网元
	 * 
	 * @author kk
	 * @param tunnel
	 *            界面收集的tunnel对象
	 * @return true 包含晨晓网元 false 不包含
	 * @throws Exception
	 */
	private boolean checkingSncp(Tunnel tunnel, AddTunnelPathDialog dialog) throws Exception {
		if (null == tunnel) {
			throw new Exception("tunnel is null");
		}
		Code code_type = null;
		boolean result = false;
		SiteService_MB siteServiceMB = null;
		try {
			siteServiceMB = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			code_type = (Code) ((ControlKeyValue) dialog.getCmbType().getSelectedItem()).getObject();
			// 如果是SNCP类型。 才走此验证
			if ("3".equals(code_type.getCodeValue())) {
				// 如果tunnel其中有一端是晨晓的 直接返回true
				if (siteServiceMB.getManufacturer(tunnel.getASiteId()) == EManufacturer.CHENXIAO.getValue() || siteServiceMB.getManufacturer(tunnel.getZSiteId()) == EManufacturer.CHENXIAO.getValue()) {
					result = true;
				}
				if(!dialog.getjCheckBoxSNCP().isSelected())
				{
					DialogBoxUtil.errorDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_SNCPPRO));
					return true;
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(siteServiceMB);
		}
		return result;
	}

}
