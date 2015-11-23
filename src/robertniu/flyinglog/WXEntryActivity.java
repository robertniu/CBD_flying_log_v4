package robertniu.flyinglog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

import robertniu.flyinglog.Constants;
import robertniu.flyinglog.GetFromWXActivity;
import robertniu.flyinglog.SendToWXActivity;
import robertniu.flyinglog.ShowFromWXActivity;
import robertniu.flyinglog.R;
import robertniu.flyinglog.Util;
import robertniu.flyinglog.uikit.CameraUtil;
import robertniu.flyinglog.uikit.MMAlert;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	
	private Button gotoBtn, regBtn, launchBtn, checkBtn,sendtxt_t;
	
	// IWXAPI �ǵ�����app��΢��ͨ�ŵ�openapi�ӿ�
    private IWXAPI api;
    private static final int THUMB_SIZE = 150;

	private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	private static final int MMAlertSelect1  =  0;
	private static final int MMAlertSelect2  =  1;
	private static final int MMAlertSelect3  =  2;

	private CheckBox isTimelineCb;
	private TextView tvLatitude_test;
	///////////////////////
	private String Latitude_value="�����"; //û��gps����ʱ��Ĭ��text�ı�
	private double TotalDistance =0;
	private String title="�û�Ա���ڷ�����"; //����text�ı�����ͷ
	private LocationManager lm;
	private Location loc;
	private Criteria ct;
	private String provider;

	private TextView tvLatitude;
	private TextView tvLongitude;
	private TextView tvHigh;
	private TextView tvDirection;
	private TextView tvSpeed;
	private TextView tvPace;
	private TextView tvDistance;
	private TextView tvGpsTime;
	private TextView tvInfoType;
	private EditText etSetTimeSpace;
	private Button btnmanual;
	private Button letsgoBtn;
	private Button btnsettimespace;
	private Button btnexit;

	private DBGps dbgps = new DBGps(this);
	private WriteGPX writegpx = new WriteGPX(this);
	private String GpxFileNameHead = "/CBD_";
	private String GpxFileNameEnd = ".gpx";
	private String GpxFileName = "";
	private Location locationA = new Location("point A");
	private Location locationB = new Location("point B");
	private boolean letsgobtnpressed = false;
	private double latitude,longitude,high;
	private String gpstime="0";
	Handler handler=new Handler();
	///////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry);

        // ͨ��WXAPIFactory��������ȡIWXAPI��ʵ��
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
    	Time t=new Time();
    	t.setToNow(); 
    	GpxFileName=GpxFileNameHead+t.year+"_"+(t.month+1)+"_"+t.monthDay+"_"+t.hour+"_"+t.minute+"_"+t.second+GpxFileNameEnd;

    	///////////////////
    	tvLatitude = (TextView) findViewById(R.id.tvlatitude);
		tvLongitude = (TextView) findViewById(R.id.tvlongitude);
		tvHigh = (TextView) findViewById(R.id.tvhigh);
		tvDirection = (TextView) findViewById(R.id.tvdirection);
		tvSpeed = (TextView) findViewById(R.id.tvspeed);
		tvPace = (TextView) findViewById(R.id.tvpace);
		tvDistance = (TextView) findViewById(R.id.tvdistance);
		tvGpsTime = (TextView) findViewById(R.id.tvgpstime);
		tvInfoType = (TextView) findViewById(R.id.tvinfotype);
		//etSetTimeSpace = (EditText) findViewById(R.id.ettimespace);
		////////////////////////////////////////////////////////////////
		//�ֶ���ȡλ��
		btnmanual = (Button) findViewById(R.id.btnmanual);
		btnmanual.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				//showInfo(getLastPosition(), 1);
				writeInfo(getLastPosition(), 1);
			}
		});
		
		/*����ʱ���� entry.xml
		btnsettimespace = (Button) findViewById(R.id.btnsettimespace);
		
		btnsettimespace.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				
			}
		});
		*/
	
    	/////////////////////////////////////////////////////////////////
		//��ʼ�ܲ���ť
    	letsgoBtn = (Button) findViewById(R.id.letsgo_btn);
    	letsgoBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ����appע�ᵽ΢��
				letsgobtnpressed = true;
				handler.postDelayed(runnable, 3000);//ÿ����ִ��һ��runnable.
				letsgoBtn.setEnabled(false);
				writegpx.WriteGpxFile(writegpx.WriteGpxFileHead(),GpxFileName);
				writegpx.WriteGpxFile("bp159",GpxFileName);
			}
		});
		
		
    	/////////////////////////////////////////////////////////////////
		//�ر�	
		
		btnexit = (Button) findViewById(R.id.btnexit);		
		btnexit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				if(letsgobtnpressed == true)
				{
				writegpx.WriteGpxFile(writegpx.WriteGpxFileEnd(),
						GpxFileName);
				handler.removeCallbacks(runnable);
				}
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		




		////////////////////////////////////////////////////////////////
    	regBtn = (Button) findViewById(R.id.reg_btn);
    	regBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ����appע�ᵽ΢��
			    api.registerApp(Constants.APP_ID);    	
			}
		});
    	

    	///////////////////////////////////////////////////////////////////////
    	//ֱ�ӷ����ı���Ϣ
    	sendtxt_t = (Button) findViewById(R.id.sendtxt_t_btn);
    	sendtxt_t.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
						String text = Latitude_value;
						if (text == null || text.length() == 0) {
							return;
						}
						
						// ��ʼ��һ��WXTextObject����
						WXTextObject textObj = new WXTextObject();
						textObj.text = text;

						// ��WXTextObject�����ʼ��һ��WXMediaMessage����
						WXMediaMessage msg = new WXMediaMessage();
						msg.mediaObject = textObj;
						// �����ı����͵���Ϣʱ��title�ֶβ�������
						// msg.title = "Will be ignored";
						msg.description = text;

						// ����һ��Req
						SendMessageToWX.Req req = new SendMessageToWX.Req();
						req.transaction = buildTransaction("text"); // transaction�ֶ�����Ψһ��ʶһ������
						req.message = msg;
						
						//req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
						
						// ����api�ӿڷ������ݵ�΢��
						api.sendReq(req);
						//finish();
					
				
			}
		});
    	//////////////////////////////////////////////////////////////////////////
    	
    	//ת������ҳ
        gotoBtn = (Button) findViewById(R.id.goto_send_btn);
        gotoBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        startActivity(new Intent(WXEntryActivity.this, SendToWXActivity.class));
		        //WXEntryActivity.this.finish();
			}
		});
        
        //����΢��
        /*
        launchBtn = (Button) findViewById(R.id.launch_wx_btn);
        launchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(WXEntryActivity.this, "launch result = " + api.openWXApp(), Toast.LENGTH_LONG).show();
			}
		});
        
        //����Ƿ�֧��΢��Ȧ
        checkBtn = (Button) findViewById(R.id.check_timeline_supported_btn);
        checkBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int wxSdkVersion = api.getWXAppSupportAPI();
				if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
					Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline supported", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline not supported", Toast.LENGTH_LONG).show();
				}
			}
		});
        */

        	
		dbgps.openDB();
		initLocation();
        api.handleIntent(getIntent(), this);
		writegpx.WriteGpxFile("bp277",GpxFileName);
    }

    
    Runnable runnable=new Runnable(){
    	@Override
    	public void run() {
    	// TODO Auto-generated method stub
    	//Ҫ��������
    	//	if(gpstime !="0")
    	//	writegpx.WriteGpxFile(writegpx.WriteGpxString(latitude,longitude,high,gpstime),GpxFileName);
    	handler.postDelayed(this, 2000);
    	}
    	};
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case 0x101: {
			final WXAppExtendObject appdata = new WXAppExtendObject();
			final String path = CameraUtil.getResultPhotoPath(this, data, SDCARD_ROOT + "/tencent/");
			appdata.filePath = path;
			appdata.extInfo = "this is ext info";

			final WXMediaMessage msg = new WXMediaMessage();
			msg.setThumbImage(Util.extractThumbNail(path, 150, 150, true));
			msg.title = "this is title";
			msg.description = "this is description";
			msg.mediaObject = appdata;
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("appdata");
			req.message = msg;
			req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
			api.sendReq(req);
			
			finish();
			break;
		}
		default:
			break;
		}
	}
    
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
    
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	// ΢�ŷ������󵽵�����Ӧ��ʱ����ص����÷���
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			goToGetMsg();		
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
	}

	// ������Ӧ�÷��͵�΢�ŵ�����������Ӧ�������ص����÷���
	@Override
	public void onResp(BaseResp resp) {
		int result = 0;
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
		
		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}
	
	private void goToGetMsg() {
		Intent intent = new Intent(this, GetFromWXActivity.class);
		intent.putExtras(getIntent());
		startActivity(intent);
		finish();
	}
	
	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		WXMediaMessage wxMsg = showReq.message;		
		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
		
		StringBuffer msg = new StringBuffer(); // ��֯һ������ʾ����Ϣ����
		msg.append("description: ");
		msg.append(wxMsg.description);
		msg.append("\n");
		msg.append("extInfo: ");
		msg.append(obj.extInfo);
		msg.append("\n");
		msg.append("filePath: ");
		msg.append(obj.filePath);
		
		Intent intent = new Intent(this, ShowFromWXActivity.class);
		intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
		intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
		intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
		startActivity(intent);
		finish();
	}
	///////////////////////////////////////////////////////////////////////////////////////
	//Download by http://www.codefans.net
		private final LocationListener locationListener = new LocationListener()
		{

			@Override
			public void onLocationChanged(Location arg0)
			{
				if (letsgobtnpressed == false)
				{
				showInfo(getLastPosition(), 2);
				}
				else if(letsgobtnpressed == true)
				{
				writeInfo(getLastPosition(), 2);	
				writegpx.WriteGpxFile("bp406",GpxFileName);
				}
					
			}

			@Override
			public void onProviderDisabled(String arg0)
			{
				showInfo(null, -1);
			}

			@Override
			public void onProviderEnabled(String arg0)
			{
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2)
			{
			}
			

		};

		private void initLocation()
		{
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				ct = new Criteria();
				ct.setAccuracy(Criteria.ACCURACY_FINE);// �߾���
				ct.setAltitudeRequired(true);// ��ʾ����
				ct.setBearingRequired(true);// ��ʾ����
				ct.setSpeedRequired(true);// ��ʾ�ٶ�
				ct.setCostAllowed(false);// �������л���
				ct.setPowerRequirement(Criteria.POWER_LOW);// �͹���
				provider = lm.getBestProvider(ct, true);
				// λ�ñ仯����,Ĭ��5��һ��,����10������
				lm.requestLocationUpdates(provider, 10000, 30, locationListener);
			} else
				showInfo(null, -1);
			writegpx.WriteGpxFile("bp466",GpxFileName);
		}

		private gpsdata getLastPosition()
		{
			
			gpsdata result = new gpsdata();
			loc = lm.getLastKnownLocation(provider);
			
			/////test
			/*
			locationB.setLatitude(10);
			locationB.setLongitude(10);
			locationA.setLatitude(20);
			locationA.setLongitude(20);
			if((locationA.getLatitude()) != 0)
			{
			result.Distance = locationA.distanceTo(locationB); //locationB is the destination location
			}
			*/
			///////
		
			if (loc != null)
			{
				locationB.setLatitude(loc.getLatitude());
				locationB.setLongitude(loc.getLongitude());
				result.Latitude = loc.getLatitude();
				result.Longitude = loc.getLongitude();
				result.High = loc.getAltitude();
				result.Direct = loc.getBearing();
				result.Speed = loc.getSpeed();
				result.Pace = (1000/(loc.getSpeed()))/60; //��������
				
				Date d = new Date();
				//d.setTime(loc.getTime() + 28800000);// UTCʱ��,ת����ʱ��+8Сʱ
				d.setTime(loc.getTime());
				result.GpsTime = DateFormat.format("yyyy-MM-dd", d).toString()+"T"+ DateFormat.format("kk:mm:ss", d).toString()+"Z";
				d = null;
				
				//����������֮��ľ���
				if((locationA.getLatitude()) != 0)
				{
				result.Distance = locationA.distanceTo(locationB); //locationB is the destination location =last location
				}
				locationA.setLatitude(loc.getLatitude());
				locationA.setLongitude(loc.getLongitude());
				
			}
			writegpx.WriteGpxFile("bp493",GpxFileName);
			return result;
		}

		private void showInfo(gpsdata cdata, int infotype)
		{
			if (cdata == null)
			{
				if (infotype == -1)
				{
					tvLatitude.setText("GPS�����ѹر�");
					tvLongitude.setText("");
					tvHigh.setText("");
					tvDirection.setText("");
					tvSpeed.setText("");
					tvPace.setText("");
					tvDistance.setText("");
					tvGpsTime.setText("");
					
					tvInfoType.setText("");
					btnmanual.setEnabled(false);
					//btnsettimespace.setEnabled(false);
					//etSetTimeSpace.setEnabled(false);
				}
			} else
			{
				TotalDistance=TotalDistance+cdata.Distance;
				tvLatitude.setText(String.format("γ��:%f", cdata.Latitude));
				tvLongitude.setText(String.format("����:%f", cdata.Longitude));
				tvHigh.setText(String.format("����:%f", cdata.High));
				tvDirection.setText(String.format("����:%f", cdata.Direct));
				tvSpeed.setText(String.format("�ٶ�:%f", cdata.Speed));
				tvPace.setText(String.format("����:%f",cdata.Pace));
				tvDistance.setText(String.format("����:%f",TotalDistance));
				tvGpsTime.setText(String.format("GPSʱ��:%s", cdata.GpsTime));
				//
				Latitude_value="###"+title+"####"
						+String.format("γ��:%f", cdata.Latitude)+"#"
						+String.format("����:%f", cdata.Longitude)+"#"
						+String.format("����:%f", cdata.High)+"#"
						+String.format("����:%f", cdata.Direct)+"#"
						+String.format("�ٶ�:%f", cdata.Speed)+"#"
						+String.format("����:%f", cdata.Pace)+"#"
						+String.format("����:%f", TotalDistance)+"#"
						+String.format("GPSʱ��:%s", cdata.GpsTime);
				
				//
				/*
				cdata.InfoType = infotype;
				switch (infotype)
				{
				case 1:
					tvInfoType.setText("��Ϣ��Դ״̬:�ֶ���ȡ����");
					break;
				case 2:
					tvInfoType.setText("��Ϣ��Դ״̬:λ�øı����");
					break;
					*/
				/*
				 * case 3: tvInfoType.setText("��Ϣ��Դ״̬:λ�øı����"); break;
				 */
				//}

	
			}

		}

///////////////////////////////////////////////////////////////////////////////////////		
		private void writeInfo(gpsdata cdata, int infotype)
		{
			String temp_info="";
			if (cdata == null)
			{
				if (infotype == -1)
				{
					tvLatitude.setText("GPS�����ѹر�");
					tvLongitude.setText("");
					tvHigh.setText("");
					tvDirection.setText("");
					tvSpeed.setText("");
					tvPace.setText("");
					tvDistance.setText("");
					tvGpsTime.setText("");
					
					tvInfoType.setText("");
					btnmanual.setEnabled(false);
					//btnsettimespace.setEnabled(false);
					//etSetTimeSpace.setEnabled(false);
				}
			} 
			else 
			{
				TotalDistance=TotalDistance+cdata.Distance;
				tvLatitude.setText(String.format("γ��:%f", cdata.Latitude));
				tvLongitude.setText(String.format("����:%f", cdata.Longitude));
				tvHigh.setText(String.format("����:%f", cdata.High));
				tvDirection.setText(String.format("����:%f", cdata.Direct));
				tvSpeed.setText(String.format("�ٶ�:%f", cdata.Speed));
				tvPace.setText(String.format("����:%f",cdata.Pace));
				tvDistance.setText(String.format("����:%f",TotalDistance));
				tvGpsTime.setText(String.format("GPSʱ��:%s", cdata.GpsTime));
				//
				Latitude_value="###"+title+"####"
						+String.format("γ��:%f", cdata.Latitude)+"#"
						+String.format("����:%f", cdata.Longitude)+"#"
						+String.format("����:%f", cdata.High)+"#"
						+String.format("����:%f", cdata.Direct)+"#"
						+String.format("�ٶ�:%f", cdata.Speed)+"#"
						+String.format("����:%f", cdata.Pace)+"#"
						+String.format("����:%f", TotalDistance)+"#"
						+String.format("GPSʱ��:%s", cdata.GpsTime);


				dbgps.addGpsData(cdata);
				
				if(cdata.GpsTime==null)
				{
					cdata.GpsTime="0";
				}
				latitude=cdata.Latitude;
				longitude=cdata.Longitude;
				high=cdata.High;
				gpstime=cdata.GpsTime;
				temp_info=writegpx.WriteGpxString(cdata.Latitude,cdata.Longitude,cdata.High,cdata.GpsTime);
				writegpx.WriteGpxFile(temp_info,GpxFileName);
				writegpx.WriteGpxFile("bp612",GpxFileName);
			}

		}
		
		
		/*
		@Override
		protected void onStop()
		{
			if (dbgps != null)
			{
				dbgps.closeDB();
				dbgps = null;
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			super.onStop();
		}
		*/
		
		
		@Override
		protected void onDestroy()
		{
			if (dbgps != null)
			{
				dbgps.closeDB();
				dbgps = null;
			}
			super.onDestroy();
		}

	/////////////////////////////////////////////////////////

	
}