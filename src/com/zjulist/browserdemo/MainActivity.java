package com.zjulist.browserdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.drive.query.internal.LogicalFilter;
import com.list.prophetclient.DelayRequestInfo;
import com.list.prophetclient.ProphetWebViewClient;
import com.list.prophetclient.WebInfo;
import com.list.prophetclient.ProphetWebViewClient.ProphetInterface;
import com.zjulist.addisplay.DemoActivity;
import com.zjulist.addisplay.R;






























import android.R.anim;
import android.R.bool;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceActivity.Header;
import android.renderscript.Element;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle bundle = intent.getExtras();
		isAdOpen = bundle.getBoolean("withAd", false);
		Log.i("DEMOUI",""+isAdOpen);
		//webUrlText.setText("");
		webView.clearFormData();
		webView.clearMatches();
		webView.clearSslPreferences();
		webView.clearView();
		webView.clearHistory();
		webView.clearCache(true);
		webView.loadUrl("");
		super.onNewIntent(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

			if(webView.canGoBack())
			{
				webView.goBack();
			}
			else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	ActionBar actionBar;
	EditText webUrlText;
	Button goButton;
	WebView webView;
	AdaptiveAd adaptiveAd;
	String host;
	MywebChromeClient webChromeClient = new MywebChromeClient();
	ProphetWebViewClient webViewClient;
	
	ImageButton backBtn;
	ImageButton fowardBtn;
	ImageButton adIndicatorBtn;
	ImageButton adsBtn;
	ImageButton settingBtn;
	ImageButton infoBtn;
	ImageButton homeBtn;
	
	private static Lock lock = new ReentrantLock();

	
    //ads switch
    boolean isAdOpen;
    //no ads progressbar 
    ToastTimer noAdProgressToast;
    
    
    //server setting
    String curServerAddress;
    int curServerPort;
    String curProxyAddress;
    int curProxyPort;
    
	//demo setting
    String curMainUrl;
    int curMainPort;
    String curRealHostName;
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		adaptiveAd.onStop();
		noAdProgressToast.disappear();
		finish();
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		noAdProgressToast.disappear();
		super.onPause();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        


//
//        Log.i("Predict",curProxyAddress+curProxyPort);
//        System.setProperty("http.proxyHost",curProxyAddress );
//        if(curProxyPort == 0)
//        	System.setProperty("http.proxyPort","");
//        else
//        	System.setProperty("http.proxyPort",String.valueOf(curProxyPort));

		setContentView(R.layout.activity_main);

		
		
		Bundle bundle = this.getIntent().getExtras();
		isAdOpen = bundle.getBoolean("withAd", false);
		
		
//		SharedPreferences preferences = getApplicationContext().getSharedPreferences("Server", Context.MODE_PRIVATE);  
//        curMainUrl =preferences.getString("mainurl", "");
//        curMainPort = preferences.getInt("port", 0);
//        curRealHostName = preferences.getString("realhostname", "");
		
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("Server", Context.MODE_PRIVATE);  
        curServerAddress =preferences.getString("address", "");  
        curServerPort = preferences.getInt("port", 0);
        curProxyAddress = preferences.getString("proxyaddress", "");
        curProxyPort = preferences.getInt("proxyport", 0);

//        Log.i("Predict",curProxyAddress+curProxyPort);
//        System.setProperty("http.proxyHost",curProxyAddress );
//        if(curProxyPort == 0)
//        	System.setProperty("http.proxyPort","");
//        else
//        	System.setProperty("http.proxyPort",String.valueOf(curProxyPort));
		
		host = "http://www.wably.com";
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle("");
		actionBar.setCustomView(R.layout.actionbar_view);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		
		//actionBar.hide();
		
		webUrlText = (EditText) actionBar.getCustomView().findViewById(
				R.id.WebUrlText);
		
		
		goButton = (Button) actionBar.getCustomView().findViewById(
				R.id.GoButton);
		
		ProphetWebViewClient.ProphetInterface prophetInterface = new ProphetWebViewClient.ProphetInterface() {
			
			@Override
			public void onPredictFinished(int predict_time) {
				// TODO Auto-generated method stub
				Log.e("Prophet:interface","onPredictFinished:"+predict_time);
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast toast = Toast.makeText(getApplicationContext(), "Predict Finish", Toast.LENGTH_SHORT);
						toast.show();
					}
				});

			}
			
			@Override
			public void onLoad() {
				// TODO Auto-generated method stub
				Log.e("Prophet:interface","onLoad");
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						webView.setVisibility(View.VISIBLE);
						adaptiveAd.endAd();
						noAdProgressToast.disappear();
					}
				});

			}
			
			@Override
			public void onDomContentLoaded() {
				// TODO Auto-generated method stub
				Log.e("Prophet:interface","onDomContentLoaded");
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						webView.setVisibility(View.VISIBLE);
						adaptiveAd.endAd();
						noAdProgressToast.disappear();
					}
				});
			}

			@Override
			public void onRealPageFinished() {
				// TODO Auto-generated method stub
				Log.e("Prophet:interface","onRealPageFinished");
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						webView.setVisibility(View.VISIBLE);
						adaptiveAd.endAd();
						noAdProgressToast.disappear();
					}
				});
				
			}

			@Override
			public void onVisitedTimeout() {
				// TODO Auto-generated method stub
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						webView.setVisibility(View.VISIBLE);
						adaptiveAd.endAd();
						noAdProgressToast.disappear();
					}
				});
				
			}
		};
		
		

		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.setWebChromeClient(webChromeClient);
		webViewClient = new MyProphetWebViewClient(getApplicationContext(), prophetInterface,webView);
		webView.setWebViewClient(webViewClient);
		
		adaptiveAd=new AdaptiveAd(MainActivity.this);
		
		


      if(curServerAddress.equals(""))
      {
      	Toast toast =Toast.makeText(getApplicationContext(), "Please Set Server", Toast.LENGTH_LONG);
      	toast.show();
      }else {
      	Toast toast =Toast.makeText(getApplicationContext(), "Server:"+curServerAddress
      			+":"+curServerPort, Toast.LENGTH_LONG);
      	toast.show();
			webViewClient.setPredictServer(curServerAddress, curServerPort);
		}

		goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = webUrlText.getText().toString().toLowerCase();
				if(url.equals(""))
					return;
				if (!url.startsWith("http")){
					url = "http://"+url;
				}
//				if(curWebInfo!= null &&curWebInfo.isSaved == false)
//				{
//					if(parseEndTag == false)
//					{
//						webView.loadUrl("javascript:window.dom.getHTMLSource('<html>'+" +
//			                    "document.getElementsByTagName('html')[0].innerHTML+'</html>',1);");
//					}
//	
//				}
				webView.loadUrl(url);

			}
		});
		
		
		
		webUrlText.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_ENTER&&event.getAction()==KeyEvent.ACTION_DOWN){
						String url = webUrlText.getText().toString().toLowerCase();
						if (!url.startsWith("http")){
							url = "http://"+url;
						}

						webView.loadUrl(url);
						webUrlText.setText(url);
						return false;
				}
				
				
				return false;
			}
			
		});
		
		LinearLayout toolBarLayout=(LinearLayout)findViewById(R.id.ToolbarLayout);
//		backBtn=(ImageButton)toolBarLayout.findViewById(R.id.backBtn);
//		fowardBtn=(ImageButton)toolBarLayout.findViewById(R.id.fowardBtn);
//		adsBtn =(ImageButton)toolBarLayout.findViewById(R.id.adsBtn);
		adIndicatorBtn = (ImageButton)toolBarLayout.findViewById(R.id.adsIndicatorBtn);
		settingBtn = (ImageButton)toolBarLayout.findViewById(R.id.settingBtn);
		infoBtn = (ImageButton)toolBarLayout.findViewById(R.id.infoBtn);
		homeBtn = (ImageButton)toolBarLayout.findViewById(R.id.homeBtn);
		
		
		homeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,DemoActivity.class);
				startActivity(intent);
			}
		});
		
		
		
		
		noAdProgressToast = new ToastTimer(getApplicationContext());
//		adsBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				isAdOpen = !isAdOpen;
//				if(isAdOpen == true)
//				{
//					adsBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_on_background));
//				}
//				else {
//					adsBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
//				}
//				
//			}
//		});
		
		adIndicatorBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(adaptiveAd.isLoaded() == true)
				{
					Toast toast = Toast.makeText(getApplicationContext(), "Ads is loaded...", Toast.LENGTH_SHORT);
					toast.show();
				}
				else{
					Toast toast = Toast.makeText(getApplicationContext(), "Ads is loading...", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
		
		//stateBtn.setEnabled(false);
		
		
//        backBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if(webView.canGoBack())
//				     webView.goBack();
//				
//			}
//		});
//        
//		
//		fowardBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if(webView.canGoForward())
//					webView.goForward();
//			}
//		});
		//webView.loadUrl(webUrlText.getText().toString());
		settingBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				settingServerDialog();
				//settingAddressDialog();
			}
		});
		
		infoBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				webinfoDialog();
			}
		});
		
	
//		if(curMainUrl.equals(""))
//		{
//			Toast toast = Toast.makeText(getApplicationContext(), "Use default setting...", Toast.LENGTH_SHORT);
//			toast.show();
//			curMainPort = 8080;
//			curMainUrl ="165.124.182.177";
//			curRealHostName = "www.buzzfeed.com";
//			Editor editor = preferences.edit();
//	        editor.putString("mainurl", curMainUrl);  
//	        editor.putInt("port",curMainPort);    
//	        editor.putString("realhostname", curRealHostName);
//	        editor.commit();
//			  
//		}
//		
//		if(!curMainUrl.equals(""))
//		{
//			String url ="";
//			if (!curMainUrl.startsWith("http")){
//				url = "http://"+curMainUrl+":"+String.valueOf(curMainPort);
//			}
//			URL url_temp;
//			Log.i("DEMOUI","URL"+url);
//			try {
//				url_temp = new URL(url);
//				if(url_temp.getHost().equals(curRealHostName)||((url_temp.getHost().equals(curMainUrl))&&(url_temp.getPort()==curMainPort)))
//				{
//					if(url_temp.getHost().equals(curRealHostName))
//					{
//						url = "http://"+curMainUrl+":"+String.valueOf(curMainPort)+"/"+url_temp.getFile();
//					}else {
//						url = url;
//					}
//					Log.i("DEMOUI","URL"+url);
//					webView.loadUrl(url);
//				}
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
		
		

	}
	
	public void updateAdState(boolean state) {
		// TODO Auto-generated method stub
		   if(state == false)
			   adIndicatorBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_off));
           if(adaptiveAd.isLoaded())
           {
        	   adIndicatorBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_on));
           }
           else {
        	   adIndicatorBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_off));
           }
           
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}
	


	class MywebChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// TODO Auto-generated method stub
			if(newProgress==100)
			    Log.i("ADS:browser","progress-100");
			if(isAdOpen == true)
			{
				adaptiveAd.updateProgress(newProgress);
				noAdProgressToast.updateProgress(newProgress);
			}else {
				noAdProgressToast.updateProgress(newProgress);
			}
			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			// TODO Auto-generated method stub
			Log.i("ADS:browser","received title");
			super.onReceivedTitle(view, title);
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, JsPromptResult result) {
			// TODO Auto-generated method stub
			return super.onJsPrompt(view, url, message, defaultValue, result);
		}

		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
			// TODO Auto-generated method stub
			super.onReceivedIcon(view, icon);
		}

	}
	
	
	private void writeToSD() throws IOException {
	    File sd = Environment.getExternalStorageDirectory();
	    String DB_PATH;
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        DB_PATH = getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
	    }
	    else {
	        DB_PATH = getApplicationContext().getFilesDir().getPath() + getApplicationContext().getPackageName() + "/databases/";
	    }
	    Log.i("DB",DB_PATH);

	    if (sd.canWrite()) {
	        String currentDBPath = "weblog.db";
	        String backupDBPath = "weblog_backup.db";
	        File currentDB = new File(DB_PATH, currentDBPath);
	        if(!currentDB.exists())
	        {
	        	Log.i("DB","No database");
	        	return;
	        }
	        File backupDB = new File(sd, backupDBPath);

	        if (currentDB.exists()) {
	            FileChannel src = new FileInputStream(currentDB).getChannel();
	            FileChannel dst = new FileOutputStream(backupDB).getChannel();
	            dst.transferFrom(src, 0, src.size());
	            src.close();
	            dst.close();
	        }
	    }
	}
	
	
	class MyProphetWebViewClient extends ProphetWebViewClient{

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			webUrlText.setText(url);
	
			noAdProgressToast.disappear();
			adaptiveAd.endAd();
			if(isAdOpen == false)
			{
				webView.setVisibility(View.INVISIBLE);
				noAdProgressToast.makeCircleToast(getApplicationContext(), 10000, 0, 0,MainActivity.this);
				noAdProgressToast.show();
			}else {
				if(adaptiveAd.isLoaded() == true)
				{
					boolean isAdsShown = adaptiveAd.showAd();  
					if(isAdsShown){
					//Log.i("ADS:browser","showProgressBar");
						adaptiveAd.updateProgress(0);
					}
				}else {
					webView.setVisibility(View.INVISIBLE);
					noAdProgressToast.makeCircleToast(getApplicationContext(), 10000, 0, 0,MainActivity.this);
					noAdProgressToast.show();
				}
				
			}
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			webUrlText.setText(url);
			super.onPageFinished(view, url);
		}

		public MyProphetWebViewClient(Context context,ProphetInterface prophetInterface,WebView webView) {
			super(context, prophetInterface,webView);
			// TODO Auto-generated constructor stub
		}
		
	}
	

	private void webinfoDialog()
	{
		if(webViewClient.curWebInfo== null)
			return;
		final LinearLayout infoDlg = (LinearLayout)getLayoutInflater().inflate(R.layout.info_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Web Information")
				.setView(infoDlg);
		TextView infoText = (TextView) infoDlg.findViewById(R.id.baseInfoText);
		infoText.setText(webViewClient.curWebInfo.toString());
		ListView rttListView = (ListView)infoDlg.findViewById(R.id.rttList);
		ListView historyView = (ListView)infoDlg.findViewById(R.id.historyList);
		TextView textView = (TextView)infoDlg.findViewById(R.id.rttTagText);
		TextView textView1 = (TextView)infoDlg.findViewById(R.id.historyTagText);
		
		if(webViewClient.curWebInfo.rttList.size()!=0)
		{
			rttListView.setVisibility(View.VISIBLE);
			textView.setVisibility(View.VISIBLE);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,webViewClient.curWebInfo.rttList);
			rttListView.setAdapter(adapter);
		}
		else {
			
			rttListView.setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
		}
		if(webViewClient.curWebInfo.historyList.size()!=0)
		{
			historyView.setVisibility(View.VISIBLE);
			textView1.setVisibility(View.VISIBLE);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,webViewClient.curWebInfo.historyList);
			historyView.setAdapter(adapter);
		}else {
			historyView.setVisibility(View.GONE);
			textView1.setVisibility(View.GONE);
		}
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
	
		});

		builder.create().show();
	}
	
	private void settingServerDialog()
	{
		final LinearLayout settingDlg = (LinearLayout)getLayoutInflater().inflate(R.layout.server_setting_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Server Setting")
				.setView(settingDlg);
		final TextView curServerTextView = (TextView)settingDlg.findViewById(R.id.curServerText);
		final TextView curProxyTextView = (TextView)settingDlg.findViewById(R.id.curProxyText);
		if(curServerAddress.equals(""))
			curServerTextView.setText("SERVER:NULL");
		else
			curServerTextView.setText("SERVER:"+curServerAddress+":"+curServerPort);
		if(curProxyAddress.equals(""))
			curProxyTextView.setText("PROXY:NULL");
		else
			curProxyTextView.setText("PROXY:"+curProxyAddress+":"+curProxyPort);
		
		Button claerbutton1 =(Button)settingDlg.findViewById(R.id.clearServerBtn);
		Button claerbutton2 =(Button)settingDlg.findViewById(R.id.clearProxyBtn);
		
		claerbutton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences preferences = getApplicationContext().getSharedPreferences("Server", Context.MODE_PRIVATE);  
		        Editor editor = preferences.edit();  
		        curServerAddress = "";
				curServerPort = 0;
		        editor.putString("address", "");  
		        editor.putInt("port",0);    
		        editor.commit();
		        curServerTextView.setText("SERVER:NULL");
			}
		});
		
		claerbutton2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences preferences = getApplicationContext().getSharedPreferences("Server", Context.MODE_PRIVATE);  
		        Editor editor = preferences.edit();  
		        curProxyAddress = "";
				curProxyPort = 0;
		        editor.putString("proxyaddress", "");  
		        editor.putInt("proxyport",0);    
		        editor.commit();
		        curProxyTextView.setText("PROXY:NULL");
			}
		});
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				RadioGroup radioGroup= (RadioGroup)settingDlg.findViewById(R.id.radioGroup1);
				int checkedId =radioGroup.getCheckedRadioButtonId();
				SharedPreferences preferences = getApplicationContext().getSharedPreferences("Server", Context.MODE_PRIVATE);  
		        Editor editor = preferences.edit();   
				if(checkedId == R.id.radio0)
				{
					webViewClient.setPredictServer("10.214.148.120",7778);
					curServerAddress = "10.214.148.120";
					curServerPort = 7778;
			        editor.putString("address", "10.214.148.120");  
			        editor.putInt("port",7778);    
			        editor.commit(); 
				}
				else if(checkedId == R.id.radio1)
				{
					webViewClient.setPredictServer("165.124.182.177",7778);
					curServerAddress = "165.124.182.177";
					curServerPort = 7778;
			        editor.putString("address", "165.124.182.177");  
			        editor.putInt("port",7778);    
			        editor.commit(); 
				}
				else{
					EditText editText_val1 =(EditText)settingDlg.findViewById(R.id.editText_val1);
					if(!editText_val1.getText().toString().contains(":"))
						return;
					String[] attr = editText_val1.getText().toString().split(":"); 
					webViewClient.setPredictServer(attr[0], Integer.parseInt(attr[1]));
					curServerAddress = attr[0];
					curServerPort = Integer.parseInt(attr[1]);					
			        editor.putString("address", attr[0]);  
			        editor.putInt("port",Integer.parseInt(attr[1]));    
			        editor.commit(); 
				
				}
				
				EditText editText2_val2 =(EditText)settingDlg.findViewById(R.id.editText2_val2);
				if(!editText2_val2.getText().toString().contains(":"))
					return;
				String[] attr = editText2_val2.getText().toString().split(":"); 
				curProxyAddress = attr[0];
				curProxyPort = Integer.parseInt(attr[1]);					
		        editor.putString("proxyaddress", curProxyAddress);  
		        editor.putInt("proxyport",curProxyPort);    
		        editor.commit();
		        System.setProperty("http.proxyHost",curProxyAddress );
		        System.setProperty("http.proxyPort",String.valueOf(curProxyPort));

			}
	
		});
		builder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
	
		});
		builder.create().show();
	}
	
	private void settingAddressDialog()
	{
		final LinearLayout settingDlg = (LinearLayout)getLayoutInflater().inflate(R.layout.address_setting_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Address Setting")
				.setView(settingDlg);
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("Server", Context.MODE_PRIVATE);  
        final Editor editor = preferences.edit();
		final EditText urlText =(EditText)settingDlg.findViewById(R.id.urlEditText);
		final EditText hostText =(EditText)settingDlg.findViewById(R.id.hostEditText);
		if(!curMainUrl.equals(""))
			urlText.setText(curMainUrl+":"+curMainPort);
		if(!curRealHostName.equals(""))
			hostText.setText(curRealHostName);
		//curMainUrl =preferences.getString("mainurl", "");
        //curMainPort = preferences.getInt("port", 0);
        //curRealHostName = preferences.getString("realhostname", "");
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
	
				if(!urlText.getText().toString().contains(":"))
				{
					Toast toast = Toast.makeText(getApplicationContext(), "URL FORMAT ERROR", Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				if(hostText.equals(""))
				{
					Toast toast = Toast.makeText(getApplicationContext(), "HOST NAME IS NULL", Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				String[] attr = urlText.getText().toString().split(":"); 
				curMainUrl = attr[0];
				curMainPort = Integer.parseInt(attr[1]);
				curRealHostName = hostText.getText().toString();
		        editor.putString("mainurl", curMainUrl);  
		        editor.putInt("port",curMainPort);    
		        editor.putString("realhostname", curRealHostName);
		        editor.commit();
				webView.loadUrl("http://"+curMainUrl+":"+curMainPort);
			}
	
		});
		builder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
	
		});
		builder.create().show();
	}
	
	
}
