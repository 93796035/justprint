package org.cas.client.platform.bar.net.bean;

import java.util.Date;
import java.util.HashMap;

public class TextContent {
	public String content; //"\u003cdiv style=\u0022text-align: center;\u0022\u003e\u003cspan style=\u0022background-color: initial;\u0022\u003eNOODLES\u003c/span\u003e\u003c/div\u003e",
	public int id;			//787,
	public Date markDate;	//null,
	public Person person;
	public String posInPage;	//"en_service_3_1_0_1_description",
	public Person publisher;	//":null,
	public int recordStatus;//":0,
	public int version;//":0
	
	private String[] locationStrAry;
	public int getCategoryIdx() {
		if(locationStrAry == null) {
			locationStrAry = posInPage.split("_");
		}
		
		return Integer.valueOf(locationStrAry[3]);
	}
	public void putTitle(HashMap<String, String> titleMap) {
		if(locationStrAry == null) {
			locationStrAry = posInPage.split("_");
		}
		titleMap.put(locationStrAry[0], content);
	}
}
