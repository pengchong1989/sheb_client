package com.nms.ui.ptn.systemManage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import com.nms.db.bean.system.LogManager;
import com.nms.db.bean.system.UnLoading;
import com.nms.model.system.LogManagerService_MB;
import com.nms.model.system.UnloadService_MB;
import com.nms.model.util.Services;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.UiUtil;

/**
 * 读取 转储的XML文件
 * @author sy
 *
 */
public class ReadUnloadXML {
	
	static String  file="config/unload.xml";
	/**
	 * 查询  unloadXML文件
	 */
	public static List<UnLoading> selectUnloadXML(){
		List<UnLoading> unloadList=null;
		UnloadService_MB unloadService = null;
		try {
			unloadList = new ArrayList<UnLoading>();
			unloadService = (UnloadService_MB) ConstantUtil.serviceFactory.newService_MB(Services.UNLOADING);
			unloadList = unloadService.selectAll();
			if(unloadList == null){
				unloadList = new ArrayList<UnLoading>();			
			}	

		} catch (Exception e) {
			ExceptionManage.dispose(e, null);
		}finally{
			UiUtil.closeService_MB(unloadService);
		}		
		
		return unloadList;
	}
	
	public static List<LogManager> selectLog(){
		List<LogManager> logManagerList=null;
		LogManagerService_MB logManagerService = null;
		try {
			logManagerList = new ArrayList<LogManager>();
			logManagerService = (LogManagerService_MB) ConstantUtil.serviceFactory.newService_MB(Services.LOGMANAGER);
			logManagerList = logManagerService.selectAll();
			if(logManagerList == null){
				logManagerList = new ArrayList<LogManager>();			
			}	

		} catch (Exception e) {
			ExceptionManage.dispose(e, null);
		}finally{
			UiUtil.closeService_MB(logManagerService);
		}				
		return logManagerList;
	}
	/*
	public static List<UnLoading> selectUnloadXML(){
		List<UnLoading> unloadList=null;
		UnLoading unload=null;
		try{
			unloadList=new ArrayList<UnLoading>();
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document doc=builder.parse(new File(file));
			NodeList nl=doc.getElementsByTagName("unload");
			for(int i=0;i<nl.getLength();i++){
				unload=new UnLoading();
				unload.setUnloadType(Integer.parseInt(doc.getElementsByTagName("unloadType").item(i).getFirstChild().getNodeValue()));
				unload.setCellType(Integer.parseInt(doc.getElementsByTagName("cellType").item(i).getFirstChild().getNodeValue()));
				unload.setUnloadLimit(Integer.parseInt(doc.getElementsByTagName("unloadLimit").item(i).getFirstChild().getNodeValue()));
				unload.setSpillEntry(Integer.parseInt(doc.getElementsByTagName("spillEntry").item(i).getFirstChild().getNodeValue()));
				unload.setHoldEntry(Integer.parseInt(doc.getElementsByTagName("holdEntry").item(i).getFirstChild().getNodeValue()));
				unload.setUnloadMod(Integer.parseInt(doc.getElementsByTagName("unloadMod").item(i).getFirstChild().getNodeValue()));
				unload.setFileWay(doc.getElementsByTagName("fileWay").item(i).getFirstChild().getNodeValue());
//				if(unload.getUnloadType() != 3 && unload.getUnloadType() != 4){
				unload.setIsAuto(Integer.parseInt(doc.getElementsByTagName("isAuto").item(i).getFirstChild().getNodeValue()));
				unload.setAutoStartTime(doc.getElementsByTagName("autoTime").item(i).getFirstChild().getNodeValue());
				unload.setTimeInterval(Integer.parseInt(doc.getElementsByTagName("timeInterval").item(i).getFirstChild().getNodeValue()));
//				}
				unloadList.add(unload);
			}
		}catch (Exception e) {
			ExceptionManage.dispose(e,ReadUnloadXML.class);
		}
		return unloadList;
	}
	*/
	/**
	 *修改     转储   管理
	 *数据保存到 XML中
	 * @param document
	 * @param filename
	 * @return
	 */
	public  boolean doc2XmlFile(Document document,String filename) 
    { 
      boolean flag = true; 
      try 
       { 
            /** 将document中的内容写入文件中   */ 
             TransformerFactory tFactory = TransformerFactory.newInstance();    
             Transformer transformer = tFactory.newTransformer();  
            /** 编码 */ 
            //transformer.setOutputProperty(OutputKeys.ENCODING, "GB2312"); 
             DOMSource source = new DOMSource(document);  
             StreamResult result = new StreamResult(filename);    
             transformer.transform(source, result);  
         }catch(Exception ex) 
         { 
             flag = false; 
             ex.printStackTrace(); 
         } 
        return flag;       
    }
 
 public  Document load(String filename)   { 	
       Document document = null; 
      try  
       {  
            DocumentBuilderFactory   factory = DocumentBuilderFactory.newInstance();    
            DocumentBuilder builder=factory.newDocumentBuilder();    
            document=builder.parse( new  File(filename));    
            document.normalize(); 
       } 
      catch (Exception ex){ 
           ex.printStackTrace(); 
       }   
      return document; 
    }
 

 /** 
     *   演示修改文件的具体某个节点的值  
     */ 
   public  void updateUnloadXML(UnLoading unload) {
	   UnloadService_MB unloadService = null;
		try {
			unloadService = (UnloadService_MB) ConstantUtil.serviceFactory.newService_MB(Services.UNLOADING);
			unloadService.update(unload);

		} catch (Exception e) {
			ExceptionManage.dispose(e, null);
		}finally{
			UiUtil.closeService_MB(unloadService);
		}
   }			   
   public  void updateUnloadXML(LogManager unload) {
	   LogManagerService_MB logService = null;
		try {
			logService = (LogManagerService_MB) ConstantUtil.serviceFactory.newService_MB(Services.LOGMANAGER);
			logService.update(unload);

		} catch (Exception e) {
			ExceptionManage.dispose(e, null);
		}finally{
			UiUtil.closeService_MB(logService);
		}
   }	   	   	   
	   /*
	   Document document = load(file);

       try{
          //获取叶节点
           NodeList nodeList = document.getElementsByTagName("unload");
          //遍历叶节点
           for(int i=0; i<nodeList.getLength(); i++){
        	  String unloadType= document.getElementsByTagName("unloadType").item(i).getFirstChild().getNodeValue();
        	  if(unloadType.equals(unload.getUnloadType()+"")){
       		   document.getElementsByTagName("cellType").item(i).getFirstChild().setNodeValue(unload.getCellType()+"");
           	   document.getElementsByTagName("unloadLimit").item(i).getFirstChild().setNodeValue(unload.getUnloadLimit()+"");
           	   document.getElementsByTagName("spillEntry").item(i).getFirstChild().setNodeValue(unload.getSpillEntry()+"");
           	   document.getElementsByTagName("holdEntry").item(i).getFirstChild().setNodeValue(unload.getHoldEntry()+"");
           	   document.getElementsByTagName("unloadMod").item(i).getFirstChild().setNodeValue(unload.getUnloadMod()+"");
           	   document.getElementsByTagName("fileWay").item(i).getFirstChild().setNodeValue(unload.getFileWay());   
//           	   if(unload.getUnloadType() ==1 ||unload.getUnloadType() ==2 ){
           		 document.getElementsByTagName("isAuto").item(i).getFirstChild().setNodeValue(unload.getIsAuto()+"");  
           		 document.getElementsByTagName("autoTime").item(i).getFirstChild().setNodeValue(unload.getAutoStartTime());  
           		 document.getElementsByTagName("timeInterval").item(i).getFirstChild().setNodeValue(unload.getTimeInterval()+"");  
//           	   }
        	  }        
           }
           doc2XmlFile(document, file);
          
       }catch(Exception e){
           ExceptionManage.dispose(e,this.getClass());
       }
     */  
  
   
}
