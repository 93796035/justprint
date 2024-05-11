package org.cas.client.platform.bar.net;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.TablesPanel;

import org.cas.client.platform.bar.BarOption;
//import org.cas.client.platform.bar.action.Cmd_ChangeTable;
//import org.cas.client.platform.bar.net.action.CreateNewOrderAction;
//import org.cas.client.platform.bar.pannel.BarFrame;
//import org.cas.client.platform.bar.pannel.TablesPanel;

public class BackGroundTimerThread extends Thread{
	
	static String url;
	String jsonStr;
	 
    @Override
    public void run() {
    	
    	reInitURLString();
		jsonStr = BarFrame.prepareLicenceJSONString();
		int counterForTableRefresh = 0;
    	while(true) {
    		try {
    			Thread.sleep(5000);
    		}catch(Exception e) {
    			//do nothing.
    		}
//    		if(url != null && url.trim().length() > 1 && !"null".equals(url.trim().toLowerCase())) {
//    			new HttpRequestClient(url, "POST", jsonStr, null, CreateNewOrderAction.getInstance()).start();
//    		}
//
//    		//if there's customer frame, refresh it.
//    		if(BarFrame.customerFrame != null) {
//    			BarFrame.customerFrame.refresh();
//			}
//
//			//refresh the table view every 1 mints
//			counterForTableRefresh++;
//			if (counterForTableRefresh >= 6) {
//				counterForTableRefresh = 0;
//				if (BarFrame.instance.curPanelIdx == 0 && !Cmd_ChangeTable.getInstance().getSourceBtn().isSelected()) {
//					((TablesPanel) BarFrame.instance.panels[0]).initContent();
//				}
//			}
//
    	}
    }

	public static void reInitURLString() {
		url = NetUtil.validateURL(BarOption.getServerHost());	//if not valid, the url will be return directly.
		if(url != null && url.trim().length() > 1 && !"null".equals(url.trim().toLowerCase())) {	//so need to check again. 
			if(!url.endsWith("/")) {
				url = url + "/";
			}
			
			String storeName = BarOption.getBillHeadInfo();
	    	int p = storeName.indexOf(":");
	    	if(p > 0) {
	    		storeName = storeName.substring(0, p).trim();
	    	}
	    	
			url =  url + storeName + "/requestNewOrders";
		}
	}
}
