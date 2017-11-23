package com.nms.db.bean.ptn.port;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nms.db.bean.ptn.oam.OamInfo;
import com.nms.db.bean.ptn.qos.QosInfo;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EManufacturer;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.ptn.port.PortLagService_MB;
import com.nms.model.util.Services;
import com.nms.ui.frame.ViewDataObj;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.keys.StringKeysObj;

public class AcPortInfo extends ViewDataObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1314793540284447175L;
	
	private int id;	//自增、主键
	private int siteId;	//关联网元表主键
	private int portId;	//关联端口表主键，当此条AC是port下时，此字段有值，否则是0
	private int lagId;	//关联lag表主键，当此条AC是lag下时，此字段有值，否则是0
	private String name;	//ac名称
	private String portNameLog;//端口名称，log日志使用
	private int portModel;	//端口模式，关联code表主键
	private int macAddressLearn;	//mac地址学习 关联code表主键
	private int horizontalDivision;	//水平分割 关联code表主键
	private int managerEnable;	//管理状态，关联code表主键 = TMC流量监管使能
	private String operatorVlanId; //运营商VLANID
	private String clientVlanId;	//客户运营商ID
	private int tagAction;	//上话tag行为
	private int exitRule;	//出口规则，关联code表主键 = 下话TAG行为
	private String vlanId;	//vlanid=下话增加VLAN ID
	private String vlancri;	//vlancri
	private String vlanpri;	//vlanpri=下话增加VLAN PRI
	private int model;//模式:0/1/2/=不使能/trTCM/Modified trTCM
	private int acModelLog;//log日志使用
	private int macCount = 0;//mac地址表容量	
	private String jobStatus;	//工作状态
	private int acBusinessId;	//设备ID 从businessid表中取
	private String simpleBuffer = "simpleBuffer";
	private int bufType;	//流类型，关联code表主键
	private int isUser;	//是否被使用 1=true 0=false
	private QosInfo simpleQos;	//qos对象
	private List<OamInfo> oamList = new ArrayList<OamInfo>();
	private List<Acbuffer> bufferList = new ArrayList<Acbuffer>();
	private int acStatus; //激活状态  1=激活，2=未激活
	private int lanId = 0;
	private int downTpid;//下话tpid
	public String toString(){
		StringBuffer sb=new StringBuffer().append(" id=").append(id)
		.append(" ;siteId=").append(siteId).append(" ;portId=").append(portId)
		.append(" ;lagId=").append(lagId).append(" ;name=").append(name)
		.append(" ;portModel=").append(portModel).append(" ;operatorVlanId=").append(operatorVlanId)
		.append(" ;clientVlanId=").append(clientVlanId).append(" ;managerEnable=").append(managerEnable)
		.append(" ;exitRule=").append(exitRule).append(" ;vlanId=").append(vlanId)
		.append(" ;vlancri=").append(vlancri).append(" ;vlanpri=").append(vlanpri)
		.append(" ;horizontalDivision=").append(horizontalDivision).append(" ;macAddressLearn=").append(macAddressLearn)
		.append(" ;tagAction=").append(tagAction).append(" ;jobStatus=").append(jobStatus)
		.append(" ;acBusinessId=").append(acBusinessId).append(" ;bufType=").append(bufType)
		.append(" ;isUser=").append(isUser).append(" ;isUser=").append(isUser)
		.append(" ;simpleQos={ ").append(simpleQos.toString()).append(" } ");
		return sb.toString();
	}

	public int getAcModelLog() {
		return acModelLog;
	}

	public void setAcModelLog(int acModelLog) {
		this.acModelLog = acModelLog;
	}

	public String getSimpleBuffer() {
		return simpleBuffer;
	}

	public void setSimpleBuffer(String simpleBuffer) {
		this.simpleBuffer = simpleBuffer;
	}

	public String getPortNameLog() {
		return portNameLog;
	}

	public void setPortNameLog(String portNameLog) {
		this.portNameLog = portNameLog;
	}

	public List<Acbuffer> getBufferList() {
		return bufferList;
	}

	public void setBufferList(List<Acbuffer> bufferList) {
		this.bufferList = bufferList;
	}

	public List<OamInfo> getOamList() {
		return oamList;
	}

	public void setOamList(List<OamInfo> oamList) {
		this.oamList = oamList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}

	public int getLagId() {
		return lagId;
	}

	public void setLagId(int lagId) {
		this.lagId = lagId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getOperatorVlanId() {
		return operatorVlanId;
	}

	public void setOperatorVlanId(String operatorVlanId) {
		this.operatorVlanId = operatorVlanId;
	}

	public String getClientVlanId() {
		return clientVlanId;
	}

	public void setClientVlanId(String clientVlanId) {
		this.clientVlanId = clientVlanId;
	}

	public int getManagerEnable() {
		return managerEnable;
	}

	public void setManagerEnable(int managerEnable) {
		this.managerEnable = managerEnable;
	}

	public int getExitRule() {
		return exitRule;
	}

	public void setExitRule(int exitRule) {
		this.exitRule = exitRule;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}

	public String getVlancri() {
		return vlancri;
	}

	public void setVlancri(String vlancri) {
		this.vlancri = vlancri;
	}

	public String getVlanpri() {
		return vlanpri;
	}

	public void setVlanpri(String vlanpri) {
		this.vlanpri = vlanpri;
	}

	public int getHorizontalDivision() {
		return horizontalDivision;
	}

	public void setHorizontalDivision(int horizontalDivision) {
		this.horizontalDivision = horizontalDivision;
	}

	public int getMacAddressLearn() {
		return macAddressLearn;
	}

	public void setMacAddressLearn(int macAddressLearn) {
		this.macAddressLearn = macAddressLearn;
	}

	public int getTagAction() {
		return tagAction;
	}

	public void setTagAction(int tagAction) {
		this.tagAction = tagAction;
	}

	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	public int getAcBusinessId() {
		return acBusinessId;
	}

	public void setAcBusinessId(int acBusinessId) {
		this.acBusinessId = acBusinessId;
	}

	public int getBufType() {
		return bufType;
	}

	public void setBufType(int bufType) {
		this.bufType = bufType;
	}

	public int getPortModel() {
		return portModel;
	}

	public void setPortModel(int portModel) {
		this.portModel = portModel;
	}

	public int getIsUser() {
		return isUser;
	}

	public void setIsUser(int isUser) {
		this.isUser = isUser;
	}

	public QosInfo getSimpleQos() {
		return simpleQos;
	}

	public void setSimpleQos(QosInfo simpleQos) {
		this.simpleQos = simpleQos;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}
	public int getLanId() {
		return lanId;
	}

	public void setLanId(int lanId) {
		this.lanId = lanId;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void putObjectProperty() {
		SiteService_MB siteService = null;
		PortLagService_MB portLagService = null;
		PortService_MB portService = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			portLagService = (PortLagService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORTLAG);
			portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			getClientProperties().put("id", getId());
			getClientProperties().put("acname", getName());
			if(this.getPortId()>0){
				getClientProperties().put("portName", portService.getPortname(this.getPortId()));
			}else if(this.getLagId()>0){
				getClientProperties().put("portName", portLagService.getLagName(this.getLagId()));
			}
			getClientProperties().put("jobstatus", super.getJobStatus(this.getJobStatus()));
			//			getClientProperties().put("acMode", getModel() == 0 ? "不使能" : getModel() == 1 ? "trTcm" : "Modified trTcm");
			getClientProperties().put("acMode", UiUtil.getCodeById(this.getPortModel()).getCodeName());
			getClientProperties().put("acMacLearn", UiUtil.getCodeById(this.getExitRule()).getCodeName());
			if(siteService.getManufacturer(getSiteId()) == EManufacturer.WUHAN.getValue()){
				getClientProperties().put("vlanid", this.getBufferList().get(0).getVlanId());
				getClientProperties().put("macCount", this.getMacCount());
			}else{
				getClientProperties().put("vlanid", this.getVlanId());
				getClientProperties().put("activeStatus", getAcStatus() == 1 ? EActiveStatus.ACTIVITY.toString() : EActiveStatus.UNACTIVITY.toString());
			}
			getClientProperties().put("isService", this.getIsUser()==0?ResourceUtil.srcStr(StringKeysObj.OBJ_NO):ResourceUtil.srcStr(StringKeysObj.OBJ_YES));
			getClientProperties().put("sitename", siteService.getSiteName(this.getSiteId()));
			getClientProperties().put("vlanpri", this.getBufferList().get(0).getEightIp());
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(portService);
			UiUtil.closeService_MB(siteService);
			UiUtil.closeService_MB(portLagService);
		}
	}

	public int getMacCount() {
		return macCount;
	}

	public void setMacCount(int macCount) {
		this.macCount = macCount;
	}

	public int getAcStatus() {
		return acStatus;
	}

	public void setAcStatus(int acStatus) {
		this.acStatus = acStatus;
	}
	
	public AcPortInfo deepCopy(){
		ByteArrayOutputStream arrayOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		ObjectInputStream inputStream = null;
		try {
			arrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ObjectOutputStream(arrayOutputStream);
			objectOutputStream.writeObject(this);
			
			inputStream = new ObjectInputStream(new ByteArrayInputStream(arrayOutputStream.toByteArray()));
			return (AcPortInfo) inputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				arrayOutputStream.close();
				objectOutputStream.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public int getDownTpid() {
		return downTpid;
	}

	public void setDownTpid(int downTpid) {
		this.downTpid = downTpid;
	}
	
	
}
