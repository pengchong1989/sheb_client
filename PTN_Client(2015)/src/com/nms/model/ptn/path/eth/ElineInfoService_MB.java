package com.nms.model.ptn.path.eth;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.nms.db.bean.equipment.port.PortInst;
import com.nms.db.bean.ptn.Businessid;
import com.nms.db.bean.ptn.oam.OamInfo;
import com.nms.db.bean.ptn.oam.OamMepInfo;
import com.nms.db.bean.ptn.oam.OamMipInfo;
import com.nms.db.bean.ptn.path.ServiceInfo;
import com.nms.db.bean.ptn.path.eth.ElineInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.pw.PwNniInfo;
import com.nms.db.bean.ptn.port.AcPortInfo;
import com.nms.db.bean.ptn.port.PortLagInfo;
import com.nms.db.dao.ptn.BusinessidMapper;
import com.nms.db.dao.ptn.path.eth.ElineInfoMapper;
import com.nms.db.dao.ptn.path.pw.PwInfoMapper;
import com.nms.db.dao.ptn.port.AcPortInfoMapper;
import com.nms.db.enums.EActionType;
import com.nms.db.enums.EServiceType;
import com.nms.db.enums.OamTypeEnum;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.ptn.oam.OamInfoService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.port.AcPortInfoService_MB;
import com.nms.model.ptn.port.PortLagService_MB;
import com.nms.model.util.ObjectService_Mybatis;
import com.nms.model.util.ServiceFactory;
import com.nms.model.util.Services;
import com.nms.ui.manager.BusinessIdException;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.util.Mybatis_DBManager;

public class ElineInfoService_MB extends ObjectService_Mybatis {
	public void setPtnuser(String ptnuser) {
		super.ptnuser = ptnuser;
	}

	public void setSqlSession(SqlSession sqlSession) {
		super.sqlSession = sqlSession;
	}

	private ElineInfoMapper mapper;

	public ElineInfoMapper getElineInfoMapper() {
		return mapper;
	}

	public void setElineInfoMapper(ElineInfoMapper ElineInfoMapper) {
		this.mapper = ElineInfoMapper;
	}

	
	public List<ElineInfo> selectByCondition(ElineInfo eline) {
		List<ElineInfo> elineinfoList = null;
		PortService_MB portServiceMB = null;
		PortInst portInst = null;
		PortLagService_MB lagServiceMB = null;
		AcPortInfoService_MB acPortInfoServiceMB = null;
		List<Integer> acIds = new ArrayList<Integer>();
		List<AcPortInfo> acPortInfos = null;
		List<ElineInfo> returnElines = new ArrayList<ElineInfo>();
		try {
			elineinfoList = this.mapper.queryByCondition(eline);
			if (elineinfoList != null && !elineinfoList.isEmpty()) {
				for (ElineInfo elineInfo : elineinfoList) {
					elineInfo.setCreateTime(DateUtil.strDate(elineInfo.getCreateTime(), DateUtil.FULLTIME));
					elineInfo.setActivatingTime(DateUtil.strDate(elineInfo.getActivatingTime(), DateUtil.FULLTIME));
				}
			}
			if(eline.getAportId()>0){
				portServiceMB = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT,this.sqlSession);
				acPortInfoServiceMB = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo, this.sqlSession);
				portInst = portServiceMB.selectPortybyid(eline.getAportId());
				if(portInst.getLagId()>0){
					lagServiceMB = (PortLagService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORTLAG,this.sqlSession);
					PortLagInfo portLagInfo = new PortLagInfo();
					portLagInfo.setSiteId(portInst.getSiteId());
					portLagInfo.setId(portInst.getLagId());
					portLagInfo = lagServiceMB.selectLAGByCondition(portLagInfo).get(0);
					
					AcPortInfo acPortInfo = new AcPortInfo();
					acPortInfo.setSiteId(portInst.getSiteId());
					acPortInfo.setLagId(portLagInfo.getId());
					acPortInfos = acPortInfoServiceMB.selectByCondition(acPortInfo);
				}else{
					AcPortInfo acPortInfo = new AcPortInfo();
					acPortInfo.setSiteId(eline.getaSiteId());
					acPortInfo.setPortId(eline.getAportId());
					acPortInfos = acPortInfoServiceMB.selectByCondition(acPortInfo);
				}
				
				for (int i = 0; i < acPortInfos.size(); i++) {
					acIds.add(acPortInfos.get(i).getId());
				}
				for (int i = 0; i < elineinfoList.size(); i++) {
					if(acIds.contains(elineinfoList.get(i).getaAcId()) || acIds.contains(elineinfoList.get(i).getzAcId())){
						returnElines.add(elineinfoList.get(i));
					}
				}
			}else{
				return elineinfoList;
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		return returnElines;
	}

	public List<ElineInfo> selectBySiteId(int siteId) {
		List<ElineInfo> elineinfoList = null;
		try {
			elineinfoList = this.mapper.selectBySiteId(siteId);
			if (elineinfoList != null && !elineinfoList.isEmpty()) {
				for (ElineInfo elineInfo : elineinfoList) {
					elineInfo.setCreateTime(DateUtil.strDate(elineInfo.getCreateTime(), DateUtil.FULLTIME));
					elineInfo.setActivatingTime(DateUtil.strDate(elineInfo.getActivatingTime(), DateUtil.FULLTIME));
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		return elineinfoList;
	}

	public Object selectElineByCondition(ElineInfo elineInfo) {
		List<ElineInfo> elineinfoList = null;
		PortService_MB portServiceMB = null;
		PortInst portInst = null;
		List<ElineInfo> returnElines = new ArrayList<ElineInfo>();
		try {
			elineinfoList = mapper.querySingleByCondition(elineInfo); 
			if(elineInfo.getCardId() > 0){
				portServiceMB = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT,this.sqlSession);
				List<PortInst> portConList = new ArrayList<PortInst>();
				// 查询具体某块板卡的所有端�?				
				if(elineInfo.getAportId() == 0){
					PortInst condition = new PortInst();
					condition.setCardId(elineInfo.getCardId());
					List<PortInst> portList = portServiceMB.select(condition);// 查询该板卡下的所有port
					if(portList != null && !portList.isEmpty()){
						for(PortInst port : portList){
							if("UNI".equals(port.getPortType())){
								portConList.add(port);
							}
						}
					}
				}else{
					// 查询具体某块板卡的具体某个端�?					
					portInst = portServiceMB.selectPortybyid(elineInfo.getAportId());
					portConList.add(portInst);
				}
				
				if(!portConList.isEmpty()){
					return filterByPort(portConList, elineinfoList, elineInfo);
				}else{
					return new ArrayList<ElineInfo>();
//				if(elineInfo.getAportId()>0){
//					acPortInfoServiceMB = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo, this.sqlSession);
//					
//					portInst = portServiceMB.selectPortybyid(elineInfo.getAportId());
//					if(portInst.getLagId()>0){
//						lagServiceMB = (PortLagService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORTLAG,this.sqlSession);
//						PortLagInfo portLagInfo = new PortLagInfo();
//						portLagInfo.setSiteId(portInst.getSiteId());
//						portLagInfo.setId(portInst.getLagId());
//						portLagInfo = lagServiceMB.selectLAGByCondition(portLagInfo).get(0);
//						
//						AcPortInfo acPortInfo = new AcPortInfo();
//						acPortInfo.setSiteId(portInst.getSiteId());
//						acPortInfo.setLagId(portLagInfo.getId());
//						acPortInfos = acPortInfoServiceMB.selectByCondition(acPortInfo);
//					}else{
//						AcPortInfo acPortInfo = new AcPortInfo();
//						acPortInfo.setSiteId(elineInfo.getaSiteId());
//						acPortInfo.setPortId(elineInfo.getAportId());
//						acPortInfos = acPortInfoServiceMB.selectByCondition(acPortInfo);
//					}
//					
//					for (int i = 0; i < acPortInfos.size(); i++) {
//						acIds.add(acPortInfos.get(i).getId());
//					}
//					for (int i = 0; i < elineinfoList.size(); i++) {
//						if(acIds.contains(elineinfoList.get(i).getaAcId()) || acIds.contains(elineinfoList.get(i).getzAcId())){
//							returnElines.add(elineinfoList.get(i));
//						}
//					}
				}
			}else{
				return elineinfoList;
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
		}
		return returnElines;
	}

	private List<ElineInfo> filterByPort(List<PortInst> portConList, List<ElineInfo> elineinfoList, ElineInfo elineInfo) throws Exception {
		AcPortInfoService_MB acPortInfoServiceMB = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo, this.sqlSession);
		PortLagService_MB lagServiceMB = (PortLagService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORTLAG,this.sqlSession);
		List<Integer> acIds = new ArrayList<Integer>();
		List<AcPortInfo> acPortInfos = null;
		List<ElineInfo> returnElines = new ArrayList<ElineInfo>();
		for(PortInst portInst : portConList){
			try {
				if(portInst.getLagId() > 0){
					PortLagInfo portLagInfo = new PortLagInfo();
					portLagInfo.setSiteId(portInst.getSiteId());
					portLagInfo.setId(portInst.getLagId());
					portLagInfo = lagServiceMB.selectLAGByCondition(portLagInfo).get(0);
					
					AcPortInfo acPortInfo = new AcPortInfo();
					acPortInfo.setSiteId(portInst.getSiteId());
					acPortInfo.setLagId(portLagInfo.getId());
					acPortInfos = acPortInfoServiceMB.selectByCondition(acPortInfo);
				}else{
					AcPortInfo acPortInfo = new AcPortInfo();
					acPortInfo.setSiteId(elineInfo.getaSiteId());
					acPortInfo.setPortId(elineInfo.getAportId());
					acPortInfos = acPortInfoServiceMB.selectByCondition(acPortInfo);
				}
				if(acPortInfos != null){
					for (int i = 0; i < acPortInfos.size(); i++) {
						acIds.add(acPortInfos.get(i).getId());
					}
				}
			} catch (Exception e) {
				ExceptionManage.dispose(e, this.getClass());
			}
		}
		for (int i = 0; i < elineinfoList.size(); i++) {
			if(acIds.contains(elineinfoList.get(i).getaAcId()) || acIds.contains(elineinfoList.get(i).getzAcId())){
				returnElines.add(elineinfoList.get(i));
			}
		}
		return returnElines;
	}

	/**
	 * 根据网元查询此网元下所有单网元eline业务
	 * 
	 * @param siteId
	 *            网元主键
	 * @return
	 * @throws Exception
	 */
	public List<ElineInfo> selectBySite_node(int siteId) throws Exception {
		return this.mapper.selectBySiteAndisSingle(siteId, 1);
	}

	/**
	 * 根据网元查询此网元下所有网络eline业务
	 * 
	 * @param siteId
	 *            网元主键
	 * @return
	 * @throws Exception
	 */
	public List<ElineInfo> selectBySite_network(int siteId) throws Exception {
		return this.mapper.selectBySiteAndisSingle(siteId, 0);
	}

	/**
	 * 通过acId,siteId查询line
	 * 
	 * @param acId
	 * @return
	 * @throws SQLException
	 */
	public List<ElineInfo> selectByAcIdAndSiteId(int acId, int siteId) throws Exception {
		List<ElineInfo> elineInfos = null;
		elineInfos = this.mapper.queryByAcIdAndSiteIdCondition(acId, siteId);
		return elineInfos;
	}

	/**
	 * 查询单网元下的所有eline
	 * 
	 * @param siteId
	 *            网元id
	 * @return
	 * @throws Exception
	 */
	public List<ElineInfo> selectNodeBySiteAndServiceId(int siteId, int serviceId) throws Exception {

		List<ElineInfo> elineInfoList = null;
		try {
			elineInfoList = new ArrayList<ElineInfo>();
			elineInfoList = this.mapper.queryNodeBySiteAndServiceId(siteId, serviceId);
			for (ElineInfo elineInfo : elineInfoList) {
				elineInfo.setNode(true);
			}

		} catch (Exception e) {
			throw e;
		}
		return elineInfoList;
	}

	public List<ElineInfo> selectByCondition_nojoin(ElineInfo elineinfo) throws Exception {
		List<ElineInfo> elineinfoList = null;
		try {
			elineinfoList = mapper.queryByCondition_notjoin(elineinfo);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
		}
		return elineinfoList;
	}

	/**
	 * 查询单网元下的所有eline
	 * 
	 * @param siteId
	 *            网元id
	 * @return
	 * @throws Exception
	 */
	public List<ElineInfo> selectNodeBySite(int siteId) throws Exception {

		List<ElineInfo> elineInfoList = null;
		OamInfoService_MB oamInfoService = null;
		OamInfo oamInfo = null;
		OamMepInfo oamMepInfo = null;
		OamMipInfo oamMipInfo = null;
		try {
			oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo, this.sqlSession);
			elineInfoList = this.mapper.queryNodeBySite(siteId);
			for (ElineInfo elineInfo : elineInfoList) {
				elineInfo.setNode(true);

				oamInfo = new OamInfo();
				oamMepInfo = new OamMepInfo();
				oamMepInfo.setServiceId(elineInfo.getId());
				oamMepInfo.setObjType(EServiceType.ELINE.toString());
				oamInfo.setOamMep(oamMepInfo);

				oamMipInfo = new OamMipInfo();
				oamMipInfo.setServiceId(elineInfo.getId());
				oamMipInfo.setObjType(EServiceType.ELINE.toString());
				oamInfo.setOamMip(oamMipInfo);
				elineInfo.setOamList(oamInfoService.queryByServiceId(oamInfo));
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			// UiUtil.closeService(oamInfoService);
		}
		return elineInfoList;
	}

	/**
	 * 搜索eline
	 * 
	 * @param elineInfos
	 */
	public void doSearch(List<ElineInfo> elineInfos) {
		List<Integer> integers = new ArrayList<Integer>();
		for (ElineInfo elineInfo : elineInfos) {
			integers.add(elineInfo.getId());
		}
		String name = "eline_"+elineInfos.get(0).getaAcId() + "_" + System.currentTimeMillis();
		int s1Id = elineInfos.get(0).getId();
		int s2Id = elineInfos.get(1).getId();
		try {
			mapper.doSearche_insert(name, s1Id, s2Id);
			mapper.deleteByIds(integers);
			sqlSession.commit();
		} catch (Exception e) {
			sqlSession.rollback();
			ExceptionManage.dispose(e, this.getClass());
		}
	}

	public List<ElineInfo> select() throws Exception {
		List<ElineInfo> elineinfoList = null;
		OamInfo oamInfo;
		OamMepInfo oamMepInfo;
		OamMipInfo oamMipInfo;
		OamInfoService_MB oamInfoService = null;
		try {
			ElineInfo elineinfo = new ElineInfo();
			elineinfoList = new ArrayList<ElineInfo>();
			elineinfoList = this.mapper.queryByCondition(elineinfo);
			oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo, this.sqlSession);
			if (elineinfoList != null && elineinfoList.size() > 0) {
				for (ElineInfo elineInfo : elineinfoList) {
					oamInfo = new OamInfo();
					oamMepInfo = new OamMepInfo();
					oamMepInfo.setServiceId(elineInfo.getId());
					oamMepInfo.setObjType("ELINE");
					oamInfo.setOamMep(oamMepInfo);
					oamMipInfo = new OamMipInfo();
					oamMipInfo.setServiceId(elineInfo.getId());
					oamMipInfo.setObjType("ELINE");
					oamInfo.setOamMip(oamMipInfo);
					elineInfo.setOamList(oamInfoService.queryByServiceId(oamInfo));
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			// UiUtil.closeService(oamInfoService);
		}
		return elineinfoList;
	}

	/**
	 * 验证名字是否重复
	 * 
	 * @author kk
	 * 
	 * @param afterName
	 *            修改之后的名�?	 * @param beforeName
	 *            修改之前的名�?	 * 
	 * @return
	 * @throws Exception
	 * 
	 * @Exception 异常对象
	 */
	public boolean nameRepetition(String afterName, String beforeName, int siteId) throws Exception {

		int result = this.mapper.query_name(afterName, beforeName, siteId);
		if (0 == result) {
			return false;
		} else {
			return true;
		}
	}

	public List<ElineInfo> selectElineBySite(int siteId) {
		List<ElineInfo> elineInfoList = new ArrayList<ElineInfo>();
		AcPortInfoService_MB acService = null;
		PwInfoService_MB pwService = null;
		try {
			acService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo, this.sqlSession);
			pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo, this.sqlSession);
			elineInfoList = this.mapper.queryNodeBySite(siteId);
			for (ElineInfo elineInfo : elineInfoList) {
				elineInfo.setNode(true);
				elineInfo.getAcPortList().add(this.getAcInfo(siteId, elineInfo, acService));
				elineInfo.getPwNniList().add(this.getPwNniInfo(siteId, elineInfo, pwService));
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		return elineInfoList;
	}

	private AcPortInfo getAcInfo(int siteId, ElineInfo elineInfo, AcPortInfoService_MB acService) throws Exception {
		int id = 0;
		if (elineInfo.getaSiteId() == siteId) {
			id = elineInfo.getaAcId();
		} else {
			id = elineInfo.getzAcId();
		}
		return acService.selectById(id);
	}

	private PwNniInfo getPwNniInfo(int siteId, ElineInfo elineInfo, PwInfoService_MB pwService) throws Exception {
		PwInfo pw = new PwInfo();
		pw.setPwId(elineInfo.getPwId());
		pw = pwService.selectBypwid_notjoin(pw);
		if (pw != null) {
			if (pw.getASiteId() == siteId) {
				return pw.getaPwNniInfo();
			} else if (pw.getZSiteId() == siteId) {
				return pw.getzPwNniInfo();
			}
		}
		return null;
	}

	public void updateActiveStatusByType(int siteId, int status, int type) throws Exception {
		try {
			mapper.updateStatusByType(siteId, status, type);
			sqlSession.commit();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
	}

	/**
	 * 查询单网元下的所有eline
	 * 
	 * @param siteId
	 *            网元id
	 * @return
	 * @throws Exception
	 */
	public List<ElineInfo> select_synchro(int siteId, int xcid) throws Exception {

		List<ElineInfo> elineInfoList = null;
		try {
			elineInfoList = this.mapper.querySynchro(siteId, xcid);

		} catch (Exception e) {
			throw e;
		}
		return elineInfoList;
	}

	public int save(ElineInfo elineinfo) throws Exception, BusinessIdException {

		if (elineinfo == null) {
			throw new Exception("elineinfo is null");
		}

		int result = 0;
		OamInfoService_MB oamInfoService = null;
		SiteService_MB siteService = null;
		BusinessidMapper businessidMapper = null;
		PwInfoMapper pwInfoMapper = null;
		AcPortInfoMapper acportInfoMapper = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE, this.sqlSession);
			oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo, this.sqlSession);
			businessidMapper = sqlSession.getMapper(BusinessidMapper.class);
			pwInfoMapper = sqlSession.getMapper(PwInfoMapper.class);
			acportInfoMapper = sqlSession.getMapper(AcPortInfoMapper.class);
			// 如果A端配置网元和端口。取设备ID
			if (elineinfo.getaAcId() != 0) {
				Businessid aElineServiceId = null;
				if (elineinfo.getaXcId() == 0) {
					aElineServiceId = businessidMapper.queryList(elineinfo.getaSiteId(), "eline").get(0);
				} else {
					aElineServiceId = businessidMapper.queryByIdValueSiteIdtype(elineinfo.getaXcId(), elineinfo.getaSiteId(), "eline");
				}
				if (aElineServiceId == null) {
					throw new BusinessIdException(siteService.getSiteName(elineinfo.getaSiteId()) + ResourceUtil.srcStr(StringKeysTip.TIP_ELINEID));
				}

				elineinfo.setaXcId(aElineServiceId.getIdValue());
				Businessid bCondition = new Businessid();
				bCondition.setId(aElineServiceId.getId());
				bCondition.setIdStatus(1);
				businessidMapper.update(bCondition);
			}
			// 如果Z端配置网元和端口。取设备ID
			if (elineinfo.getzAcId() != 0) {
				Businessid zElineServiceId = null;
				if (elineinfo.getzXcId() == 0) {
					zElineServiceId = businessidMapper.queryList(elineinfo.getzSiteId(), "eline").get(0);
				} else {
					zElineServiceId = businessidMapper.queryByIdValueSiteIdtype(elineinfo.getzXcId(), elineinfo.getzSiteId(), "eline");
				}
				if (zElineServiceId == null) {
					throw new BusinessIdException(siteService.getSiteName(elineinfo.getzSiteId()) + ResourceUtil.srcStr(StringKeysTip.TIP_ELINEID));
				}

				elineinfo.setzXcId(zElineServiceId.getIdValue());
				Businessid bCondition = new Businessid();
				bCondition.setId(zElineServiceId.getId());
				bCondition.setIdStatus(1);
				businessidMapper.update(bCondition);
			}
			mapper.insert(elineinfo);
			result = elineinfo.getId();

			pwInfoMapper.setUser(elineinfo.getPwId(), result, EServiceType.ELINE.getValue());
			// 判断ac不等�? 就修改ac状�?			
			if (elineinfo.getaAcId() != 0) {
				acportInfoMapper.setUser(elineinfo.getaAcId(), 1);
			}
			if (elineinfo.getzAcId() != 0) {
				acportInfoMapper.setUser(elineinfo.getzAcId(), 1);
			}

			List<OamInfo> oamList = elineinfo.getOamList();
			for (OamInfo oamInfo : oamList) {
				if (oamInfo.getOamType() == OamTypeEnum.AMEP) {
					oamInfo.getOamMep().setServiceId(result);
					oamInfo.getOamMep().setObjId(elineinfo.getaXcId());
					oamInfo.setOamType(OamTypeEnum.AMEP);
				} else if (oamInfo.getOamType() == OamTypeEnum.ZMEP) {
					oamInfo.getOamMep().setServiceId(result);
					oamInfo.getOamMep().setObjId(elineinfo.getzXcId());
					oamInfo.setOamType(OamTypeEnum.ZMEP);
				}
				if (oamInfo.getOamType() == OamTypeEnum.MIP) {

				}
				oamInfoService.saveOrUpdate(oamInfo);
			}

			// 离线网元数据下载
			if (0 != elineinfo.getaSiteId()) {
				super.dateDownLoad(elineinfo.getaSiteId(), result, EServiceType.ELINE.getValue(), EActionType.INSERT.getValue());
			}
			if (0 != elineinfo.getzSiteId()) {
				super.dateDownLoad(elineinfo.getzSiteId(), result, EServiceType.ELINE.getValue(), EActionType.INSERT.getValue());
			}
			sqlSession.commit();
		} catch (BusinessIdException e) {
			throw e;
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			businessidMapper = null;
			pwInfoMapper = null;
			acportInfoMapper = null;
		}
		return result;
	}

	public int update(ElineInfo elineinfo) throws Exception {

		if (elineinfo == null) {
			throw new Exception("elineinfo is null");
		}

		int result = 0;
		ElineInfo elineBefore = null;
		Businessid businessId = null;
		SiteService_MB siteService = null;
		BusinessidMapper businessidMapper = null;
		PwInfoMapper pwInfoMapper = null;
		AcPortInfoMapper acportInfoMapper = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE, this.sqlSession);
			businessidMapper = sqlSession.getMapper(BusinessidMapper.class);
			pwInfoMapper = sqlSession.getMapper(PwInfoMapper.class);
			acportInfoMapper = sqlSession.getMapper(AcPortInfoMapper.class);

			elineBefore = new ElineInfo();
			elineBefore.setId(elineinfo.getId());
			elineBefore = mapper.queryByCondition_notjoin(elineBefore).get(0);

			result = mapper.updateByPrimaryKey(elineinfo);

			if (elineBefore.getPwId() != elineinfo.getPwId()) {
				if (!isRelatedPW(elineBefore.getPwId())) {
					pwInfoMapper.setUser(elineBefore.getPwId(), 0, 0);
				}
				pwInfoMapper.setUser(elineinfo.getPwId(), elineinfo.getId(), EServiceType.ELINE.getValue());
			}

			// 释放之前的id
			businessId = new Businessid();
			businessId.setIdStatus(0);
			businessId.setIdValue(elineBefore.getaXcId());
			businessId.setSiteId(elineBefore.getaSiteId());
			businessId.setType("eline");
			businessidMapper.updateBusinessid(businessId);
			// 释放之前的id
			businessId = new Businessid();
			businessId.setIdStatus(0);
			businessId.setIdValue(elineBefore.getzXcId());
			businessId.setSiteId(elineBefore.getzSiteId());
			businessId.setType("eline");
			businessidMapper.updateBusinessid(businessId);

			// 判断ac不等�? 就修改ac状�?			
			if (elineinfo.getaAcId() != 0) {
				if (elineBefore.getaAcId() != elineinfo.getaAcId()) {
					if (!isRelatedAC(elineBefore.getaAcId())) {
						acportInfoMapper.setUser(elineBefore.getaAcId(), 0);
					}
					acportInfoMapper.setUser(elineinfo.getaAcId(), 1);
				}

				if (elineinfo.getaXcId() != 0) {
					businessId = businessidMapper.queryByIdValueSiteIdtype(elineinfo.getaXcId(), elineinfo.getaSiteId(), "eline");
				} else {
					businessId = businessidMapper.queryList(elineinfo.getaSiteId(), "eline").get(0);
				}

				if (businessId == null) {
					throw new BusinessIdException(siteService.getSiteName(elineinfo.getaSiteId()) + ResourceUtil.srcStr(StringKeysTip.TIP_ELINEID));
				} else {
					elineinfo.setaXcId(businessId.getIdValue());
					businessId.setIdStatus(1);
					businessidMapper.update(businessId);
				}
			}
			if (elineinfo.getzAcId() != 0) {
				if (elineBefore.getzAcId() != elineinfo.getzAcId()) {
					if (!isRelatedAC(elineBefore.getzAcId())) {
						acportInfoMapper.setUser(elineBefore.getzAcId(), 0);
					}
					acportInfoMapper.setUser(elineinfo.getzAcId(), 1);
				}

				if (elineinfo.getzXcId() != 0) {
					businessId = businessidMapper.queryByIdValueSiteIdtype(elineinfo.getzXcId(), elineinfo.getzSiteId(), "eline");
				} else {
					businessId = businessidMapper.queryList(elineinfo.getzSiteId(), "eline").get(0);
				}
				if (businessId == null) {
					throw new BusinessIdException(siteService.getSiteName(elineinfo.getzSiteId()) + ResourceUtil.srcStr(StringKeysTip.TIP_ELINEID));
				} else {
					elineinfo.setzXcId(businessId.getIdValue());
					businessId.setIdStatus(1);
					businessidMapper.update(businessId);
				}
			}
			// 离线网元数据下载
			if (0 != elineinfo.getaSiteId()) {
				super.dateDownLoad(elineinfo.getaSiteId(), result, EServiceType.ELINE.getValue(), EActionType.UPDATE.getValue());
			}
			if (0 != elineinfo.getzSiteId()) {
				super.dateDownLoad(elineinfo.getzSiteId(), result, EServiceType.ELINE.getValue(), EActionType.UPDATE.getValue());
			}
			sqlSession.commit();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {

		}
		return result;
	}

	/**
	 * 在删除之前判断着PW是否存在其他的业务的关联
	 * 
	 * @param pwId
	 * @return true存在 false 不存�?	 */
	private boolean isRelatedPW(int pwId) {
		try {
			int i = this.mapper.isRelatedPW(pwId);
			if (i > 0) {
				return true;
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}
		return false;
	}

	/**
	 * 在删除之前判断着AC是否存在其他的业务的关联
	 * 
	 * @param AcId
	 * @return true存在 false 不存�?	 */
	private boolean isRelatedAC(int acId){
		List<ElineInfo> elineInfos = null;
		int isRelatedAc = 0;
		try {
			isRelatedAc = this.mapper.isRelatedAcByVPWS(acId);
			if(isRelatedAc >0)
			{
				return true;
			}else
			{
				elineInfos = this.mapper.isRelatedACByVPLS(acId);
				if(elineInfos.size()>0){
					for(ElineInfo elineInfo :elineInfos){
						if(elineInfo.getAmostAcId() != null && !elineInfo.getAmostAcId().equals("")){
							for(String str : elineInfo.getAmostAcId().split(",")){
								if(acId == Integer.parseInt(str)){
									return true;
								}
							}
						}
					}
				}else{
					return false;
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}
		return false;
	}
	
	public static void main(String[] args) {
		try {
			Mybatis_DBManager.init("10.18.1.10");
			ConstantUtil.serviceFactory = new ServiceFactory();
			ElineInfoService_MB tunnelServiceMB = (ElineInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Eline);
			BusinessidMapper businessidMapper = tunnelServiceMB.getSqlSession().getMapper(BusinessidMapper.class);
			businessidMapper.queryUseBySiteIDandType( 1, "eline",2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}

	}

	public List<ElineInfo> selectElineByPwId(List<Integer> pwIdList) {
		List<ElineInfo> list = null;
		try {
			list = this.mapper.queryAllElineByPwId(pwIdList);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
		return list;
	}
	
	public List<ElineInfo> selectByacids(List<Integer> integers){
		List<ElineInfo> elineInfos = null;
		elineInfos = this.mapper.selectByacids(integers);
		return elineInfos;
	}
	
	/**
	 * 根据网元id和业务id查询所有记录
	 * @param siteId
	 * @return
	 * @throws Exception
	 */
	public int queryEthBySiteAndServiceId(int siteId, int serviceId) throws Exception {
		List<ServiceInfo> infoList = null;
		try {
			infoList = this.mapper.queryEthBySiteAndServiceId(siteId, serviceId);
			if(infoList != null)
				return infoList.size();
		} catch (Exception e) {
			throw e;
		}
		return 0;
	}
}
