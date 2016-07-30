package com.itheima.mobilesafe.domain;

/**
 * �����������ҵ��bean ����ʵ��
 * 
 * @author Administrator
 * 
 */
public class BlackNumber {
	private String number;
	/**
	 * ����ģʽ 0ȫ������ 1�绰���� 2��������
	 */
	private String mode;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		if ("0".equals(mode) || "1".equals(mode) || "2".equals(mode)) {
			this.mode = mode;
		}else{
			this.mode ="0";
		}
	}

	@Override
	public String toString() {
		return "BlackNumber [number=" + number + ", mode=" + mode + "]";
	}

}
