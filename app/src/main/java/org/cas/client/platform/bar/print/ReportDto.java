package org.cas.client.platform.bar.print;

public class ReportDto {

	private int qt;
	private float moneyRecieved;
	private float tip;
	private float total;
//	private String 
	

	public int getQt() {
		return qt;
	}
	public void setQt(int qt) {
		this.qt = qt;
	}
	public float getMoneyRecieved() {
		return moneyRecieved;
	}
	public void setMoneyRecieved(float moneyRecieved) {
		this.moneyRecieved = moneyRecieved;
	}
	public float getTip() {
		return tip;
	}
	public void setTip(float tip) {
		this.tip = tip;
	}
	public float getTotal() {
		return total;
	}
	public void setTotal(float total) {
		this.total = total;
	}
}
