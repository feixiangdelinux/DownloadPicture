package com.example.downloadpicture.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * 加载图片的工具类
 */
public class ImageLoader {
	private ACache mCache;// 数据缓存框架(主要作用是把bitmap对象保存到手机内部)
	private int loadingBitmap = -1;// 载入时显示的图片
	private int errorBitmap = -2;// 载入出错显示的图片
	private MemoryCacheUtils memoryCacheUtils;// 内存缓存的工具类(使用了最近最少使用算法,存储图片,避免内存溢出)
	private boolean isRounded = false;// 是否是圆形图片,默认不是
	private boolean isSquare = false;// 是否是正方形图片,默认不是
	private int side = 500;// 正方形边长
	private int radius = 500;// 圆形半径
	private boolean isShow = false;// 是否显式的保存到手机内存卡中（如果是显式保存，在手机图库中个可以看到下载的图片，如果不是显示保存则看不到）

	public ImageLoader(Context context) {
		super();
		memoryCacheUtils = new MemoryCacheUtils();
		mCache = ACache.get(context);

	}

	/**
	 * 设置载入时显示的图片
	 * 
	 * @param loadingBitmap
	 */
	public void setLoadingBitmap(int loadingBitmap) {
		this.loadingBitmap = loadingBitmap;
	}

	/**
	 * 设置载入出错时显示的图片
	 * 
	 * @param errorBitmap
	 */
	public void setErrorBitmap(int errorBitmap) {
		this.errorBitmap = errorBitmap;
	}

	/**
	 * 把图片显示在imageView中
	 * 
	 * @param imageView
	 *            需要显示图片的视图
	 * @param imagePath
	 *            URL地址
	 */
	public void loadImage(ImageView imageView, String imagePath) {
		// a. 每次getView()中都会将当前的imagPath保存到ImageView
		imageView.setTag(imagePath);
		if (memoryCacheUtils.getBitmap(imagePath) != null) {// 根据url在内存中取出Bitmap对象
			setImage(imageView, imagePath, memoryCacheUtils.getBitmap(imagePath));
		} else if (mCache.getAsBitmap(imagePath) != null) {// 如果内存中没有,则根据url从二级缓存中得到Bitmap对象(用的是ACache框架存储在手机中)
			setImage(imageView, imagePath, mCache.getAsBitmap(imagePath));
			memoryCacheUtils.putBitmap(imagePath, mCache.getAsBitmap(imagePath));
		}
		// 如果手机中没有存储则请求服务器下载图片,并保存在内存和手机内部
		loadFromThirdCache(imageView, imagePath);

	}

	/**
	 * 把图片显示在imageView中
	 * 
	 * @param imageView
	 *            需要显示图片的视图
	 * @param imagePath
	 *            URL地址
	 */
	public void loadImage(ImageView imageView, String imagePath, boolean isShow) {
		this.isShow = isShow;
		// a. 每次getView()中都会将当前的imagPath保存到ImageView
		imageView.setTag(imagePath);
		if (memoryCacheUtils.getBitmap(imagePath) != null) {// 根据url在内存中取出Bitmap对象
			setImage(imageView, imagePath, memoryCacheUtils.getBitmap(imagePath));
		} else if (mCache.getAsBitmap(imagePath) != null) {// 如果内存中没有,则根据url从二级缓存中得到Bitmap对象(用的是ACache框架存储在手机中)
			setImage(imageView, imagePath, LocalCacheUtils.getInstance().getBitmapFromUrl(imagePath));
			memoryCacheUtils.putBitmap(imagePath, LocalCacheUtils.getInstance().getBitmapFromUrl(imagePath));
		}
		// 如果手机中没有存储则请求服务器下载图片,并保存在内存和手机内部
		loadFromThirdCache(imageView, imagePath);

	}

	/**
	 * 根据Url请求服务(三缓存)获取Bitmap对象显示
	 * 
	 * @param imageView
	 *            需要显示图片的视图
	 * @param imagePath
	 *            URL地址
	 */
	private void loadFromThirdCache(final ImageView imageView, final String imagePath) {
		// 如果loadingBitmap不为空,则显示代表正在加载的图片
		if (loadingBitmap != -1) {
			imageView.setImageResource(loadingBitmap);
		}
		// 启动分线程请求服务器得到bitmap
		new AsyncTask<Void, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Void... params) {
				Bitmap bitmap = null;
				// a. 在分线程准备请求服务器之前, 判断
				// 判断前面传入的imagePath和ImageView中保存的ImagePah是否相同
				String newImagePath = (String) imageView.getTag();
				if (newImagePath != imagePath) {// 已经复用
					return null; // 直接结束
				}
				// 从服务器端得到图片对象
				bitmap = getBitmapFromNetwork(imagePath);
				if (bitmap != null) {
					// 保存到一级缓存
					memoryCacheUtils.putBitmap(imagePath, bitmap);
					// 保存到二级缓存
					if (isShow) {
						// 显式保存到sd卡中
						LocalCacheUtils.getInstance().putBitmap(imagePath, bitmap);
					} else {
						// 隐式保存到手机中
						mCache.put(imagePath, bitmap);
					}

				}
				return bitmap;
			}

			protected void onPostExecute(Bitmap bitmap) {

				setImage(imageView, imagePath, bitmap);

			}

		}.execute();

	}

	/**
	 * 从服务器端得到图片对象
	 * 
	 * @param imagePath
	 *            URL地址
	 * @return
	 */
	protected Bitmap getBitmapFromNetwork(String imagePath) {
		Bitmap bitmap = null;
		try {
			URL url = new URL(imagePath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.connect();
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				InputStream is = conn.getInputStream();
				// 加载一个图片流, 得到bitmap对象
				bitmap = BitmapFactory.decodeStream(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 设置是否显示为正方形图片
	 * 
	 * @param isSquare
	 *            是否是正方形图片
	 * @param side
	 *            正方形图片的边长
	 */
	public void isSquare(boolean isSquare, int side) {
		this.isSquare = isSquare;
		this.side = side;
	}

	/**
	 * 设置是否显示为圆角图片
	 * 
	 * @param isRounded
	 *            是否是圆形图片
	 * @param radius
	 *            圆角的半径
	 */
	public void isRounded(boolean isRounded, int radius) {
		this.isRounded = isRounded;
		this.radius = radius;

	}

	/**
	 * 设置图片显示的方法
	 * 
	 * @param imageView
	 *            需要显示图片的控件
	 * @param imagePath
	 *            URL地址
	 * @param bitmap
	 *            需要显示的图片
	 */
	private void setImage(final ImageView imageView, final String imagePath, Bitmap bitmap) {
		// 在主线程准备显示图片之前
		//// 判断前面传入的imagePath和ImageView中保存的ImagePah是否相同
		String newImagePath = (String) imageView.getTag();
		if (newImagePath == imagePath) { // item的视图没有被复用
			if (bitmap == null && errorBitmap != -2) {// 如果没有图片则显示加载出错的图片
				imageView.setImageResource(errorBitmap);
			} else {
				if (isSquare == true) {// 如果是显示正方形图片
					bitmap = BitmapUtils.zoomImage(bitmap, side, side);// 把图片设置成正方形
				}
				if (isRounded == true) {// 如果是显示圆形图片
					// 把图片设置成圆形
					bitmap = BitmapUtils.toRoundCorner(BitmapUtils.zoomImage(bitmap, 500, 500), radius);
				}
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}
