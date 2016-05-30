package com.example.downloadpicture.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Bitmap对象内存缓存的工具类 此类采用了最近最少使用算法存储Bitemap,防止内存移除
 */
public class MemoryCacheUtils {

	/**
	 * 使用最近最少使用算法的集合
	 */
	private LruCache<String, Bitmap> cache;

	public MemoryCacheUtils() {
		// 虚拟机的8分之一
		int maxSize = (int) Runtime.getRuntime().maxMemory();// 16MB
		cache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
	}

	/**
	 * 把图片保存到内存中
	 * 
	 * @param url
	 * @param bitmap
	 */
	public void putBitmap(String url, Bitmap bitmap) {
		cache.put(url, bitmap);
	}

	/**
	 * 根据Url到内存中查找图片
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getBitmap(String url) {
		Bitmap bitmap = cache.get(url);
		return bitmap;
	}

}
