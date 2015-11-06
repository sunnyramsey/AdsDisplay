package com.zjulist.browserdemo;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.R.bool;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdaptiveAd {
	
	public AdaptiveAd(Activity activity)
	{
		
		int screenWidth  = activity.getWindowManager().getDefaultDisplay().getWidth();       
		int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();     
		
		Log.i("ADS", "screenWidth=" + screenWidth + "; screenHeight=" + screenHeight);  

		adleft_x = (int)(screenWidth*24/720);
		adleft_y = (int)(screenHeight*50/1280);
		adright_x = (int)(screenWidth*660/720);
		adright_y = (int)(screenHeight*50/1280);
		
		Log.i("ADS", "x=" + adleft_x + "; y=" + adleft_y); 
		Log.i("ADS", "x=" + adright_x + "; y=" + adright_y);
		
		this.activity=activity;
		this.time=5000;
		this.adListener=new TimerListener();
		this.catchedAdListener = new TimerListener();
		adRequestBuilder = new AdRequest.Builder();
		adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
		/*Change this to the device for test*/
		//adRequestBuilder.addTestDevice("014E0F500100D00B");

		interstitialAd = new InterstitialAd(activity.getApplicationContext());
	    interstitialAd.setAdUnitId("ca-app-pub-6441379973241331/9798568600");
	    interstitialAd.setAdListener(adListener);
	    interstitialAd.loadAd(adRequestBuilder.build());
	    iInLoading = true;
	    
	    catchedInterstitialAd = new InterstitialAd(activity.getApplicationContext());
	    catchedInterstitialAd.setAdUnitId("ca-app-pub-6441379973241331/9798568600");
	    catchedInterstitialAd.setAdListener(catchedAdListener);
	    catchedInterstitialAd.loadAd(adRequestBuilder.build());
	    cInLoading = true;
	    
		toastTimer=new ToastTimer(activity.getApplicationContext());
		calc = Calendar.getInstance();
		showFlag = false;
		adTimer=new Timer();
		Log.i("ADS","AdaptiveAd is created");
	}
	
	public boolean isLoaded(){
		if((interstitialAd!=null) &&interstitialAd.isLoaded())
			return true;
		else if((catchedInterstitialAd!=null) && catchedInterstitialAd.isLoaded())
			return true;
		return false;
	}
	
	public void loadAd()
	{
		if(!interstitialAd.isLoaded() && !iInLoading){
			Log.i("ADS", "start load interstitialAd");
			interstitialAd.loadAd(adRequestBuilder.build());
			iInLoading = true;
		}
	}
	public void loadCatchedAd()
	{
		if(!catchedInterstitialAd.isLoaded() && !cInLoading){
			Log.i("ADS", "start load interstitialAd");
			catchedInterstitialAd.loadAd(adRequestBuilder.build());
			cInLoading = true;
		}
	}
	
	public void endAd()
	{
	    closeAdThread=new CloseAdThread();
	    Log.i("ADS","start endAd");
	    closeAdThread.start();
//		if(closeAdThread==null)
//		{
//		    closeAdThread=new CloseAdThread();
//		    Log.i("ADS","start endAd");
//		    closeAdThread.start();
//		}
//		else {
//			if(closeAdThread.single==false)
//			{
//				//Log.i("ADS","single instance");
//				closeAdThread=new CloseAdThread();
//				Log.i("ADS","start endAd");
//				closeAdThread.start();
//			}
//			else {
//				Log.i("ADS","close has been prepared");
//			}
//				
//		}
		
		//(new CloseAdThread()).start();
		loadAd();
		loadCatchedAd();
	}
	
	public void updateProgress(int newProgress){
		if(!showFlag)
			return ;
		//Log.i("ADS","UpdateProgress "+newProgress);
		toastTimer.updateProgress(newProgress);
	}
	
	public boolean showAd(){
		try{	
			Log.i("ADS","start showAd");
			if(showFlag==false)
			{
				if((interstitialAd!=null) && interstitialAd.isLoaded())
				{
					interstitialAd.show();
					showFlag = true;
					//this.adsStartingTime = calc.get(Calendar.SECOND);
					this.adsStartingTime=System.currentTimeMillis();
					Log.i("ADS","showAds show interstitialAd ADS");
					toastTimer.makeToast(activity.getApplicationContext(), time,(MainActivity)activity);
					toastTimer.updateProgress(0);
					toastTimer.show();
					return true;
				}
				else if((catchedInterstitialAd!=null) && catchedInterstitialAd.isLoaded())
				{
					catchedInterstitialAd.show();
					showFlag = true;
					//this.adsStartingTime = calc.get(Calendar.SECOND);
					this.adsStartingTime=System.currentTimeMillis();
					Log.i("ADS","showAds show catchedInterstitialAd ADS");
					toastTimer.makeToast(activity.getApplicationContext(), time,(MainActivity)activity);
					toastTimer.updateProgress(0);
					toastTimer.show();
					return true;
				}
				else{
					Log.i("ADS","fail showAds cause neither is loaded");
					loadAd();
					loadCatchedAd();
				}
			}
			else{
				Log.i("ADS","fail showAds cause ad is already showing");
			}
		}
		catch(Exception e)
		{
			Log.e("ADS","showAds exception: "+e);
		}
		return false;
	}
	
	public void onStop()
	{
		if(showFlag)
			showFlag=false;
		toastTimer.disappear();
	}


	class TimerListener extends AdListener
	{

		@Override
		public void onAdClosed() {
			Log.i("ADS","onAdClosed");
			
			toastTimer.disappear();
			((MainActivity)activity).updateAdState(false);
			
			if(showFlag)
				showFlag=false;
			toastTimer.disappear();
			loadAd();
		
			super.onAdClosed();
		}

		@Override
		public void onAdFailedToLoad(int errorCode) {
			Log.e("ADS","onAdFailedToLoad errorCode"+errorCode);
			iInLoading = false;
			loadAd();
			
			super.onAdFailedToLoad(errorCode);
		}

		@Override
		public void onAdLeftApplication() {
			Log.i("ADS","onAdLeftApplication");
			super.onAdLeftApplication();
		}

		@Override
		public void onAdLoaded() {
			super.onAdLoaded();
			
			((MainActivity)activity).updateAdState(true);
			
			iInLoading = false;
			Log.i("ADS","onAdLoaded interstitialAd");
		}

		@Override
		public void onAdOpened() {
			Log.i("ADS","onAdOpened");
			if(!showFlag)
				showFlag=true;
			
			if(adTimer!=null)
				adTimer.cancel();
			adTimer=new Timer();
			adTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(showFlag==true)
					{
					try {
						Log.i("ADS","Timer Closes Ad");
						Instrumentation inst = new Instrumentation();  
				        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
				              MotionEvent.ACTION_DOWN, adleft_x, adleft_y, 0));  
				        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
				              MotionEvent.ACTION_UP, adleft_x, adleft_y, 0));
					} catch (Exception e) {
						// TODO: handle exception
						Log.i("ADS","Failed to close ad");
					}
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(showFlag == true)
					{
						try {
							Log.i("ADS","Timer Closes Ad");
							Instrumentation inst = new Instrumentation();  
					        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
					              MotionEvent.ACTION_DOWN, adright_x, adright_y, 0));  
					        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
					              MotionEvent.ACTION_UP, adright_x, adright_y, 0));
						} catch (Exception e) {
							// TODO: handle exception
							Log.i("ADS","Failed to close ad");
						}
					}

							
				}
			}, 30000);
			super.onAdOpened();
		}
		
	}
	
	class CatchedTimerListener extends AdListener
	{

		@Override
		public void onAdClosed() {
			Log.i("ADS","onAdClosed catchedAd");
			
			((MainActivity)activity).updateAdState(false);
			
			if(showFlag == true)
				showFlag=false;
			loadCatchedAd();
			toastTimer.disappear();
			super.onAdClosed();
		}

		@Override
		public void onAdFailedToLoad(int errorCode) {
			Log.e("ADS","onAdFailedToLoad catchedAd errorCode"+errorCode);
			cInLoading = false;
			loadCatchedAd();
			super.onAdFailedToLoad(errorCode);
		}

		@Override
		public void onAdLeftApplication() {
			Log.i("ADS","onAdLeftApplication catchedAd" );
			super.onAdLeftApplication();
		}

		@Override
		public void onAdLoaded() {
			super.onAdLoaded();
			cInLoading = false;
			
			((MainActivity)activity).updateAdState(true);
			
			Log.i("ADS","onAdLoaded catchedAd");

		}

		@Override
		public void onAdOpened() {
			Log.i("ADS","onAdOpened");
			if(!showFlag)
				showFlag=true;
			
			if(adTimer!=null)
				adTimer.cancel();
			adTimer=new Timer();
			adTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(showFlag==true)
					{
					try {
						Log.i("ADS","Timer Closes Ad");
						Instrumentation inst = new Instrumentation();  
				        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
				              MotionEvent.ACTION_DOWN, adleft_x, adleft_y, 0));  
				        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
				              MotionEvent.ACTION_UP, adleft_x, adleft_y, 0));
					} catch (Exception e) {
						// TODO: handle exception
						Log.i("ADS","Failed to close ad");
					}
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(showFlag == true)
					{
						try {
							Log.i("ADS","Timer Closes Ad");
							Instrumentation inst = new Instrumentation();  
					        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
					              MotionEvent.ACTION_DOWN, adright_x, adright_y, 0));  
					        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
					              MotionEvent.ACTION_UP, adright_x, adright_y, 0));
						} catch (Exception e) {
							// TODO: handle exception
							Log.i("ADS","Failed to close ad");
						}
					}
					
				}
			}, 30000);
			
			super.onAdOpened();
		}
		
	}
	

	class CloseAdThread extends Thread {
		public boolean single;
	    public CloseAdThread(){
	    	single=false;
	    }

	    public void run() {
	    	try{
	    		single=true;
				Log.i("ADS","start closing ads");
				if(!showFlag){
					Log.i("ADS","already been closed");
					return ;
				}
				//int currentTime = calc.get(Calendar.SECOND);
				
//				long currentTime= System.currentTimeMillis();
//				if(currentTime - adsStartingTime < 2000){
//					Log.i("ADS","ads too short, play at least two seconds, then close it");
//					Thread.sleep(2000);
//				}
				
//				while((System.currentTimeMillis()-adsStartingTime<2000)||toastTimer.getProgress()<100)
//				{
//					if(System.currentTimeMillis()-adsStartingTime<2000)
//					     Log.i("ADS","ads too short, sleep, then close it");
//					else 
//						Log.i("ADS","still loading page, sleep, then close it");
//					Thread.sleep(2000);
//					if(showFlag==false)
//						return;
//				}
				
				if(showFlag==true)
				{
				try {
					Log.i("ADS","Close Ad");
					Instrumentation inst = new Instrumentation();  
			        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
			              MotionEvent.ACTION_DOWN, adleft_x, adleft_y, 0));  
			        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
			              MotionEvent.ACTION_UP, adleft_x, adleft_y, 0));
				} catch (Exception e) {
					// TODO: handle exception
					Log.i("ADS","Failed to close ad");
				}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(showFlag == true)
				{
					try {
						Log.i("ADS","Close Ad");
						Instrumentation inst = new Instrumentation();  
				        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
				              MotionEvent.ACTION_DOWN, adright_x, adright_y, 0));  
				        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),   
				              MotionEvent.ACTION_UP, adright_x, adright_y, 0));
					} catch (Exception e) {
						// TODO: handle exception
						Log.i("ADS","Failed to close ad");
					}
				}
				
			    }
		    catch(Exception e)
		    {
		    	Log.e("ADS","Faile close ads "+e);
		    }
	    	finally
	    	{
	    		single=false;
	    	}
	    }

	}
	
	private InterstitialAd interstitialAd,catchedInterstitialAd;
	private long time;
	//private Timer tm;
	
	private Timer adTimer;
	private Calendar calc;
	private long adsStartingTime;
	private Activity activity;
    private TimerListener adListener, catchedAdListener;
    private ToastTimer toastTimer;
    private AdRequest.Builder adRequestBuilder;
	public boolean showFlag;
	private boolean iInLoading, cInLoading;
	private CloseAdThread closeAdThread;
	
	private int adleft_x,adleft_y,adright_x,adright_y;

}
