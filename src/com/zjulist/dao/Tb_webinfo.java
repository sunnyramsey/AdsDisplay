package com.zjulist.dao;

public class Tb_webinfo {
	public String getNettype() {
		return nettype;
	}
	public void setNettype(String nettype) {
		this.nettype = nettype;
	}
	public int getSignal() {
		return signal;
	}
	public void setSignal(int signal) {
		this.signal = signal;
	}
	public long getMain_id() {
		return main_id;
	}
	public void setMain_id(long main_id) {
		this.main_id = main_id;
	}
	public int getDom() {
		return dom;
	}
	public void setDom(int dom) {
		this.dom = dom;
	}
	public int getLoad() {
		return load;
	}
	public void setLoad(int load) {
		this.load = load;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	private long main_id;
	private int dom;
	private int load;
	private String timestamp;
	private String nettype;
	private int signal;
	

}
