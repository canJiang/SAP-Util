package weaver.interfaces.sap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.Util;

import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Pool;
import com.sap.mw.jco.JCO.PoolManager;

public class SAPConn {
	private static final int DEFAULT_MAXCONN = 128;
	private static final PoolManager poolManager;
	private static Log log = LogFactory.getLog(SAPConn.class);
	private static String defaultPoolName;
	private String poolName;

	static {
		poolManager = JCO.createPoolManager("dp");
		init();
	}

	public SAPConn() {
		this.poolName = defaultPoolName;
	}

	public SAPConn(String poolName) {
		if (poolName !=null && poolName.trim().length() > 0) {
			this.poolName = poolName;
		} else {
			this.poolName = defaultPoolName;
		}
	}

	/**
	 * 仅加载默认的, 数据库的设置优先
	 */
	public static void init() {
		RecordSet rs = new RecordSet();
		rs.executeSql("select * from SAPConn where isDefault=1");
		if (rs.getCounts() > 0) {
			while (rs.next()) {
				String poolName = rs.getString("code");
				Pool pool = poolManager.getPool(poolName);
				if (pool == null) {
					poolManager.addClientPool(poolName,
						Util.getIntValue(rs.getString("maxConn"), DEFAULT_MAXCONN),
						rs.getString("SAPClient"),
						rs.getString("Userid"),
						rs.getString("Password"),
						rs.getString("Language"),
						rs.getString("HostName"),
						rs.getString("SystemNumber"));
				}
				String isDefault = rs.getString("isDefault");
				if ("1".equals(isDefault)) {
					defaultPoolName = poolName;
				}
			}
		} else {
			String poolName = "default";
			Pool pool = poolManager.getPool(poolName);
			if (pool == null) {
				poolManager.addClientPool(poolName,
					Util.getIntValue(Prop.getPropValue("SAPConn", "maxconn"), DEFAULT_MAXCONN),
					Prop.getPropValue("SAPConn", "SAPClient"),
					Prop.getPropValue("SAPConn", "Userid"),
					Prop.getPropValue("SAPConn", "Password"),
					Prop.getPropValue("SAPConn", "Language"),
					Prop.getPropValue("SAPConn", "HostName"),
					Prop.getPropValue("SAPConn", "SystemNumber"));
			}
			defaultPoolName = poolName;
		}
	}

	/**
	 * 当要获取的不是默认的,则检查是否已存在,否则创建
	 * @return
	 */
	public JCO.Client getConnection() {
		if (!defaultPoolName.equalsIgnoreCase(poolName)) {
			synchronized (SAPConn.class) {
				Pool pool = poolManager.getPool(poolName);
				if (pool == null) {
					RecordSet rs = new RecordSet();
					rs.executeSql("select * from SAPConn where CODE='" + poolName + "'");
					if (rs.next()) {
						poolManager.addClientPool(poolName,
							Util.getIntValue(rs.getString("maxConn"), DEFAULT_MAXCONN),
							rs.getString("SAPClient"),
							rs.getString("Userid"),
							rs.getString("Password"),
							rs.getString("Language"),
							rs.getString("HostName"),
							rs.getString("SystemNumber"));
					}
				}
			}
		}
		JCO.Client client = poolManager.getClient(poolName);
		if (client == null) {
			log.error("获取SAP 连接失败");
		}
		return client;
	}

	public void releaseC(JCO.Client client) {
		if (client != null) {
			poolManager.releaseClient(client);
			client = null;
		}
	}

	public void removePool(String poolName) {
		poolManager.removeClientPool(poolName);
	}

	public JCO.Function excuteBapi(String bapiName, JCO.Client client) {
		if (client == null) {
			return null;
		}
		try {
			JCO.Repository localRepository =
				new JCO.Repository("sap", client);

			IFunctionTemplate localIFunctionTemplate =
				localRepository.getFunctionTemplate(bapiName);

			return new JCO.Function(localIFunctionTemplate);
		} catch (Exception localException) {
			log.error(localException);
			releaseC(client);
			return null;
		}
	}

	public JCO.Function excuteBapi(String bapiName) {
		JCO.Client client = getConnection();
		try {
			JCO.Repository localRepository =
				new JCO.Repository("sap", client);

			IFunctionTemplate localIFunctionTemplate =
				localRepository.getFunctionTemplate(bapiName);

			return new JCO.Function(localIFunctionTemplate);
		} catch (Exception localException) {
			log.error(localException);
			return null;
		} finally {
			releaseC(client);
		}
	}
}