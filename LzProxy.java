package com.dongpeng.oa.hr.workflow.getLZ;

import java.util.HashMap;
import java.util.Map;

import com.sap.mw.jco.JCO;

public class LzProxy extends CommonBapiProxyEx {
	private Map<String, String> lzMap;

	public LzProxy() {
		super("ZHR_OA_13");
	}

	public void setLzMap(Map<String, String> lzMap) {
		this.lzMap = lzMap;
	}

	@Override
	protected Map process(JCO.Client sapConnection, JCO.Function function, Map dataMap)
			throws Exception {
		Map<String, String> mainMap = (Map<String, String>) dataMap.get("mainMap");
		String hrmSapCode = (String) dataMap.get("hrmSapCode");

		JCO.ParameterList input = function.getImportParameterList();
		input.setValue(mainMap.get("SQRGH"), "P_PERNR");
		input.setValue(mainMap.get("SJLZRQ"), "P_DATE");
		input.setValue(lzMap.get(mainMap.get("LZYY")), "P_MASSG");
		input.setValue(mainMap.get("LZYY1"), "P_ZZ_LZYY");//离职原因-1
		input.setValue(mainMap.get("LZYY2"), "P_ZZ_LZYY_3");//离职原因-2
		input.setValue(mainMap.get("LZYY3"), "P_ZZ_LZYY_4");//离职原因-3
		input.setValue(mainMap.get("LZYGJY"), "P_ZZ_LZJY");//离职者对公司建议
		input.setValue(mainMap.get("BZ"), "P_ZZ_BZ");//备注
		input.setValue(hrmSapCode, "P_ZZ_MTR");//离职面谈人

		// 执行BAPI
		sapConnection.execute(function);

		JCO.ParameterList output = function.getExportParameterList();
		int ret = output.getField("SUBRC").getInt();
		Map<String, String> retMap = new HashMap<String, String>();
		retMap.put("flag", (ret == 0) ? "1" : "0");
		retMap.put("message", output.getField("MESSAGE").getString());

		return retMap;
	}

}
