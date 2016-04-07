package com.example.downloadpicture.activity;

import com.example.downloadpicture.R;
import com.example.downloadpicture.urlpath.URLPath;
import com.example.downloadpicture.utils.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private ImageView iv;
	private ImageLoader imageLoader;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		iv = (ImageView) findViewById(R.id.iv);	
		imageLoader = new ImageLoader(this);//初始化imageLoader
		imageLoader.setLoadingBitmap(R.drawable.loading);//设置载入时图片
		imageLoader.setErrorBitmap(R.drawable.error);//设置载入错误图片
//		imageLoader.isSquare(true,500);//是否显示正方形图片
		imageLoader.isRounded(true,100);//是否显示圆形图片
		imageLoader.loadImage(iv, URLPath.path);
		
		
	}





}
