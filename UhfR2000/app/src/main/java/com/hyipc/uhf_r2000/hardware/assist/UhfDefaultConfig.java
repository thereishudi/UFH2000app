package com.hyipc.uhf_r2000.hardware.assist;

public class UhfDefaultConfig {
	//read
	public static final int MEM_READ = 1;//EPC
	public static final int START_ADDR_READ = 0;
	public static final int NUM_READ = 6;
	public static final int VOICE = UhfSharedPreferenceUtil.VOICE_CUSTOM;
	
	//readerInfo
	public static final int BAUD =5;// 默认5(57600)   0(9600)、1(19200)、 2(38400)、 5(57600)、 6(115200)
	public static final byte MIN_FRE = -128;
	public static final byte MAX_FRE = 49;
	public static final byte RF_POWER = 30;
	public static final byte SCAN_TIME = 10;
}
