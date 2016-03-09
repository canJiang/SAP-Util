
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weaver.interfaces.sap.SAPConn;

import com.sap.mw.jco.JCO;

public abstract class CommonBapiProxyEx {
	private static final Log logger = LogFactory.getLog(CommonBapiProxyEx.class);

	private SAPConn sapConn;
	private String bapiName;

	public CommonBapiProxyEx() {
		sapConn = new SAPConn();
	}

	public CommonBapiProxyEx(String bapiName) {
		this.bapiName = bapiName;
		sapConn = new SAPConn();
	}

	public Map getMap(Map dataMap) {
		JCO.Client sapConnection = sapConn.getConnection();
		JCO.Function function = sapConn.excuteBapi(bapiName, sapConnection);
		Map retMap = null;
		try {
			retMap = process(sapConnection, function, dataMap);
		} catch (JCO.AbapException e) {
			logger.error("ִ��SAP BAPI����", e);
			setExceptionIntoRetMap(e, retMap);
		} catch (Exception e) {
			logger.error("ִ��SAP BAPI����", e);
			setExceptionIntoRetMap(e, retMap);
		} finally {
			sapConn.releaseC(sapConnection);
		}
		return retMap;
	}

	/**
	 * dwr���ܵ���ͬ����getMap(), ֻ�ø���ΪinvokeBapi
	 * @return
	 */
	public Map invokeBapi() {
		return getMap(null);
	}

	public String getBapiName() {
		return bapiName;
	}

	public void setBapiName(String bapiName) {
		this.bapiName = bapiName;
	}

	/**
	 * ���쳣��Ϣ��ŵ����ص�������
	 * @param ex ��Ҫ��ŵ��쳣
	 */
	private void setExceptionIntoRetMap(Exception ex, Map retMap) {
		StringWriter sw = new StringWriter();  
		ex.printStackTrace(new PrintWriter(sw, true));  
        String exceptionStr = sw.toString().replaceAll("\n", "<br/>");

        if (retMap == null) {
        	retMap = new HashMap<String, String>();
        }
    	retMap.put("retMessage", "����<br/>" + ex.getMessage() + "<br/>" + exceptionStr + "<br/>����ϵ��Ϣ��");
	}

	/**
	 * �������, ��ȡ����
	 * 
	 * @param function
	 * @throws Exception
	 */
	protected abstract Map process(JCO.Client sapConnection, JCO.Function function, Map dataMap) throws Exception;

}
