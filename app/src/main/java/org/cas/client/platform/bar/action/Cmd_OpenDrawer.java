package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.print.Command;
import org.cas.client.platform.bar.print.PrintUtil;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.casutil.L;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class Cmd_OpenDrawer implements SamActionListener {

	private static Cmd_OpenDrawer instance;
	private Cmd_OpenDrawer() {}
	public static Cmd_OpenDrawer getInstance() {
		if(instance == null)
			instance = new Cmd_OpenDrawer();
		return instance;
	}
	
	private ISButton sourceBtn;
	
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(BarFrame.menuPanel.getPrinters().length == 0) {
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.AddPrinterFirst());
			return;
		}
		String key = BarFrame.menuPanel.getPrinters()[0].getIp();
		if("".equals(key)) {
			return;// false;
		}
		
		if(key.equalsIgnoreCase("mev")) {
			try{
				PrintUtil.printThroughOSdriver(PrintUtil.getMevCommandFilePath("mevOpenCashierCommand.xml", Command.OPEN_CASHIER), false);
				return;// true;
			} catch (Exception exp) {
				L.e("CMD_OpenDrawer", "mev:printThroughOSdriver", exp);
				return;// false;
			}
			
		}else if (key.equalsIgnoreCase("serial2")){
			CommPortIdentifier commPortIdentifier;
			try{
				Enumeration tPorts = CommPortIdentifier.getPortIdentifiers();
		        if (tPorts == null || !tPorts.hasMoreElements()) {
		        	JOptionPane.showMessageDialog(BarFrame.instance, "no comm ports found! please check the printer connection.");
		        	return;// false;
		        }

		        while (tPorts.hasMoreElements()) {
		        	commPortIdentifier = (CommPortIdentifier) tPorts.nextElement();
		        	if (commPortIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL)
		                    continue;
					SerialPort tSerialPort = (SerialPort)commPortIdentifier.open("PrintService", 10000);//并口用"ParallelBlackBox"
					OutputStream outputStream = new DataOutputStream(tSerialPort.getOutputStream());
	                int[] cmd = BarOption.getOpenDrawerCommand();
	 				for (int i : cmd) {
	 					outputStream.write(i);
	 				}
	 				return;// true;
		        }
		        
		        return;// false;
			} catch (Exception exp) {
				L.e("CMD_OpenDrawer", "serial2", exp);
				return;// false;
			}
		}else {
	    	try{
				Socket socket = new Socket(key != null ? key : BarFrame.menuPanel.getPrinters()[0].getIp(), 9100);
				BarFrame.setStatusMes("sockeet connected!");
				OutputStream outputStream = socket.getOutputStream();
				int[] cmd = BarOption.getOpenDrawerCommand();
				for (int i : cmd) {
					outputStream.write(i);
				}
				
				BarFrame.setStatusMes("Command Send!");
				outputStream.flush();
				socket.close();
				BarFrame.setStatusMes("Drawer Openned!");
				return;// true;
			} catch (Exception exp) {
				L.e("CMD_OpenDrawer", "other case", exp);
				return;// false;
			}
		}
	}
}
