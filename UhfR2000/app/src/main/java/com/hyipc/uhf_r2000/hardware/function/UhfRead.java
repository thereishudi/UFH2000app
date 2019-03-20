package com.hyipc.uhf_r2000.hardware.function;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hyipc.uhf_r2000.hardware.assist.UhfReadListener;
import com.hyipc.util.Logger;

/**
 * 读标签类
 */
public class UhfRead extends Thread {

	private byte mWordPtr;/** 起始地址 */
	private byte mNum;/** 读取的字的个数 */
	private byte mMem;/** 读取的存储区 */

	/** 监听器，接收标签信息和错误信息 */
	private UhfReadListener mListener;
	private int mIntervalTime = 5;// ms
	private boolean mIsLoop = true;
	private boolean mIsPause = false;
	private Context mCtx;

	public UhfRead(UhfReadListener listener,Context context) {
		mListener = listener;
		this.mCtx = context;
	}

	@Override
	public void run() {
		try {
			byte[] ComAdr = new byte[1];
			ComAdr[0] = (byte) 0x00;
			// === InventoryG2 的参数 start ====
			byte QValue = 4;// how many cards scan for one time
			byte Session = 0;  //0~3
			byte MaskMem = 1;// te:must be 1
			byte[] MaskAdr = new byte[2];
			MaskAdr[0] = MaskAdr[1] = 0;
			byte MaskLen = 8;
			byte[] MaskData = new byte[1];
			MaskData[0] = (byte) 0;
			byte MaskFlag = 0;
			byte AdrTID = 0;
			byte LenTID = 15;
			byte TIDFlag = 0;// te:must be zero EPC     1:TID
			byte[] pEPCList = new byte[4000];
			byte[] Ant1 = new byte[1];
			int[] Totallen = new int[1];
			int[] CardNum = new int[1];
			// === InventoryG2 的参数 end ====

			StringBuffer sbContent = new StringBuffer();

			// ==== ReadDataG2的参数 start ====
			int totalLength;
			byte[] pepList;

			byte[] EPC2;
			byte Enum2 = (byte) 6;
			byte[] Password2 = { (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
			byte MaskMem2 = (byte) 1;
			byte[] MaskAdr2 = { (byte) 0, (byte) 0 };
			byte MaskLen2 = (byte) 8;
			byte[] MaskData2 = { (byte) 0 };

			byte[] Data2 = new byte[256];
			int[] errorcode2 = new int[1];
			int dataLength;
			String strnum;
			String[] contents;
			// ==== ReadDataG2的参数 end ====
			while (mIsLoop) {
				if (!com.halio.r2000.InventoryG2(UhfComm.sAddr, QValue, Session, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, AdrTID, LenTID, TIDFlag, pEPCList, Ant1, Totallen, CardNum,errorcode2)) {
					Log.e("Tag","UhfRead  不符合标签协议 错误码： "+Integer.toHexString(errorcode2[0]&0xff));
				} else {
					if (Totallen[0] != 0 && !mIsPause) {
						if (mMem == 1) {
							// 读 EPC
							totalLength = Totallen[0];
							contents = new String[CardNum[0]];
							int index_contents = 0;
							int index = 0;
							while (totalLength > index && index < pEPCList.length) {
								int length = pEPCList[index];
								for (int i = index + 1; i < index + length + 1; i++){
									if (i >= pEPCList.length) {
										Logger.D("break");
										break;
									}
									strnum = Integer.toHexString(pEPCList[i] & 0xFF);
									if (strnum.length() == 1) {
										sbContent.append("0");
									}
									sbContent.append(strnum);
									sbContent.append("");
								}
								if (!TextUtils.isEmpty(sbContent.toString())) {
									contents[index_contents++] = sbContent.toString();
								}
								Logger.D("content:"+sbContent.toString());
								index += (length + 2);
								sbContent.setLength(0);
							}
							showContent(contents);
						}  else {   //注释过
							// 非 EPC 区内容

							totalLength = Totallen[0];
							contents = new String[1];
							pepList = new byte[totalLength - 2];
							for (int i = 0; i < totalLength - 2; i++) {
								pepList[i] = pEPCList[i + 1];
							}
							EPC2 = pepList;
							if (!com.halio.r2000.ReadDataG2(UhfComm.sAddr, EPC2, Enum2, mMem, mWordPtr, mNum, Password2, MaskMem2, MaskAdr2, MaskLen2, MaskData2, Data2, errorcode2)) {
								Log.e("Tag ","读数据失败的错误码值： "+Integer.toHexString(errorcode2[0]&0xff));
							} else {
								dataLength = mNum * 2;
								for (int i = 0; i < dataLength; i++) {
									strnum = Integer.toHexString(Data2[i] & 0xFF);
									if (strnum.length() == 1) {
										sbContent.append("0");
									}
									sbContent.append(strnum);
									sbContent.append("");
								}
								contents[0] = sbContent.toString();
								showContent(contents);
								sbContent.setLength(0);
							}
						}  //注释过
					}
					try {
						Thread.sleep(mIntervalTime);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (mIsPause) {
					try {
						synchronized (this) {
							this.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.D("e:"+e.getMessage());
		}
	}

	public synchronized void pause() {
		mIsPause = true;
	}

	public synchronized void reStart() {
		mIsPause = false;
		this.notify();
	}

	public synchronized void destroy() {
		mIsLoop = false;
		mIsPause = false;
		this.notify();
	}

	private void showContent(Object[] obj) {

		if (mListener != null) {
			mListener.onContentCaughted(obj);
		}
	}

	private void showErr(String err) {
		if (mListener != null) {
			mListener.onErrorCaughted(err);
		}
	}

	public byte getmWordPtr() {
		return mWordPtr;
	}

	public void setmWordPtr(byte mWordPtr) {
		this.mWordPtr = mWordPtr;
	}

	public byte getmNum() {
		return mNum;
	}

	public void setmNum(byte mNum) {
		this.mNum = mNum;
	}

	public byte getmMem() {
		return mMem;
	}

	public void setmMem(byte mMem) {
		this.mMem = mMem;
	}

	public void setmIntervalTime(int time) {
		this.mIntervalTime = time;
	}

}
