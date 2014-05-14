package rms.android;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.http.util.ByteArrayBuffer;

import rms.android.util.SystemUiHider;
import android.R.string;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ShowActivity extends Activity 
{
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	//以下为自己声明的变量
	//显示图片的控件
	private ImageView imageView = null;  

	private ReceiveThread receiveThread;
	
	private String IP_PC="192.168.0.5";


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_show);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		
		imageView=(ImageView)findViewById(R.id.imageView1);
		imageView.setImageResource(R.drawable.rms_0);
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(
				new SystemUiHider.OnVisibilityChangeListener() 
				{
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) 
					{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) 
						{
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) 
							{
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) 
							{
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView.animate().translationY(visible ? 0 : mControlsHeight)
								.setDuration(mShortAnimTime);
						}
						else 
						{
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE: View.GONE);
						}

						if (visible && AUTO_HIDE) 
						{
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		receiveThread=new ReceiveThread();
		receiveThread.StartThread();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(500);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
  	
     //定义一个内部类,该内部类用于接受电脑端的图片，并且将其显示到对应控件
      public class ReceiveThread 
      {
    		//用于接收图片生成bitmap
    		 private Bitmap bmp=null;
    		 //用于接受socket消息的线程
    		 private Thread t = new Thread() 
    		 {   
    	          @Override  
    	          public void run() 
    	          {   
    	              super.run();  
    	              Socket socket=null;
    	              try 
    	              {   
    	            	  socket=new Socket(IP_PC, 20000);  
    	            	  //连接pc服务器端成功，添加连接信息到屏幕，待完成
    	            	  
    	                  DataInputStream dataInput = new DataInputStream(   
    	                          socket.getInputStream());   
    	                  DataOutputStream dataOutputStream=new DataOutputStream(
    	                		  socket.getOutputStream());
    	                  //图像大小
    	                  byte[] b=new byte[4];
    	                  dataInput.read(b);
    	                  char[] a=new char[4];
    	                  a[0]=(char)b[0];
    	                  a[1]=(char)b[1];
    	                  a[2]=(char)b[2];
    	                  a[3]=(char)b[3];
    	                  
    	                  int size = dataInput.readInt();   
    	                  byte[] data = new byte[size];   
    	                  // dataInput.readFully(data);   
    	                  int len = 0;   
    	                  while (len < size) {   
    	                      len += dataInput.read(data, len, size - len);   
    	                  }   

    	                  ByteArrayOutputStream outPut = new ByteArrayOutputStream();   
    	                  bmp = BitmapFactory.decodeByteArray(data, 0,   
    	                          data.length);   
    	                  bmp.compress(CompressFormat.PNG, 100, outPut);   
    	                  //imageView.setImageBitmap(bmp);   
    	                  ReceiveHandler.obtainMessage().sendToTarget();   
    	              } 
    	              catch (IOException e) 
    	              {   
    	            	  //处理连接失败，待完成
    	                  e.printStackTrace();   
    	              } 
    	              finally 
    	              {   
    	                  try 
    	                  {   
    	                      socket.close();   
    	                  } 
    	                  catch (IOException e) 
    	                  {   
    	                      e.printStackTrace();   
    	                  }   
    	              }  
    	          }   
    	      };   
    	      
    	    //用于显示接受的图片的handler
    	  	 public Handler ReceiveHandler = new Handler(){   
    	           public void handleMessage(android.os.Message msg) {
    	               imageView.setImageBitmap(bmp);   
    	           };   
    	       };   
    	      public void StartThread()
    	      {
    	    	  t.start();
    	      }
    	      
    	      
    	}

}
