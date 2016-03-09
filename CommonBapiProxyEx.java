
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
			logger.error("执行SAP BAPI出错", e);
			setExceptionIntoRetMap(e, retMap);
		} catch (Exception e) {
			logger.error("执行SAP BAPI出错", e);
			setExceptionIntoRetMap(e, retMap);
		} finally {
			sapConn.releaseC(sapConnection);
		}
		return retMap;
	}

	/**
	 * dwr不能调用同名的getMap(), 只好改名为invokeBapi
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
	 * 把异常信息存放到返回的数据里
	 * @param ex 需要存放的异常
	 */
	private void setExceptionIntoRetMap(Exception ex, Map retMap) {
		StringWriter sw = new StringWriter();  
		ex.printStackTrace(new PrintWriter(sw, true));  
        String exceptionStr = sw.toString().replaceAll("\n", "<br/>");

        if (retMap == null) {
        	retMap = new HashMap<String, String>();
        }
    	retMap.put("retMessage", "出错：<br/>" + ex.getMessage() + "<br/>" + exceptionStr + "<br/>请联系信息部");
	}

	/**
	 * 传入参数, 获取数据
	 * 
	 * @param function
	 * @throws Exception
	 */
	protected abstract Map process(JCO.Client sapConnection, JCO.Function function, Map dataMap) throws Exception;

}
