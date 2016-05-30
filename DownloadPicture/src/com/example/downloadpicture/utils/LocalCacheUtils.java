package com.example.downloadpicture.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class LocalCacheUtils {
	private static String FOLDER = "/图片";// 这里填存储图片的地址
	private static String dirPath = getSDCardPath() + FOLDER;
	private static LocalCacheUtils mLocalCacheUtils = null;

	private LocalCacheUtils() {
		super();
	}

	public static LocalCacheUtils getInstance() {
		if (mLocalCacheUtils == null) {
			mLocalCacheUtils = new LocalCacheUtils();
		}
		return mLocalCacheUtils;
	}

	/**
	 * 获取sd卡路径 双sd卡时，获得的是外置sd卡
	 * 
	 * @return
	 */
	public static String getSDCardPath() {
		String cmd = "cat /proc/mounts";
		Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
		BufferedInputStream in = null;
		BufferedReader inBr = null;
		try {
			Process p = run.exec(cmd);// 启动另一个进程来执行命令
			in = new BufferedInputStream(p.getInputStream());
			inBr = new BufferedReader(new InputStreamReader(in));

			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				if (lineStr.contains("sdcard") && lineStr.contains(".android_secure")) {
					String[] strArray = lineStr.split(" ");
					if (strArray != null && strArray.length >= 5) {
						String result = strArray[1].replace("/.android_secure", "");
						return result;
					}
				}
				// 检查命令是否执行失败。
				if (p.waitFor() != 0 && p.exitValue() == 1) {
					// p.exitValue()==0表示正常结束，1：非正常结束
				}
			}
		} catch (Exception e) {
			// return Environment.getExternalStorageDirectory().getPath();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (inBr != null) {
					inBr.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 根据url保存图片
	 * 
	 * @param url
	 * @param bitmap
	 */
	public void putBitmap(String url, Bitmap bitmap) {
		try {

			String fileName = url.substring(url.lastIndexOf("/"), url.length());
			File file = new File(dirPath, fileName);
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			FileOutputStream fls = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, fls);
			fls.flush();
			fls.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 根据url从网络取图片
	 * 
	 * @param listimage
	 * @return
	 */
	public Bitmap getBitmapFromUrl(String url) {
		try {
			String fileName = url.substring(url.lastIndexOf("/"), url.length());
			File file = new File(dirPath, fileName);
			FileInputStream fis = new FileInputStream(file);
			Bitmap bitmap = BitmapFactory.decodeStream(fis);
			fis.close();
			if (bitmap != null) {
				return bitmap;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
