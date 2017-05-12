package naveenpatidar.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.DataType.MindDataType.FilterType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class BluetoothDeviceDemoActivity extends Activity {
	private static final String TAG = BluetoothDeviceDemoActivity.class.getSimpleName();
	private TgStreamReader tgStreamReader;
	private Context sharedContext = null;
	// TODO connection sdk
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private String address = null;
	private String sharedPath;
	private FileOutputStream rawdatafileOutputStream;
	private FileOutputStream attentiondatafileOutputStream;
	private FileOutputStream meditationdatafileOutputStream;
	private FileOutputStream eegpowerfileOutputStream;
	private static final int SIZE = 20000;
	private static final int ATTENTION_SIZE = 72;
	private static final int MEDITATION_SIZE = 72;
	private static final int EEGPOWER_SIZE = 72;
	String rawdataArray[] = new String[SIZE];
	private int rawdataCounter = 0;
	String attentiondataArray[] = new String[ATTENTION_SIZE];
	private int attentiondataCounter = 0;
	String meditationdataArray[] = new String[MEDITATION_SIZE];
	private int meditationdataCounter = 0;
	String eegpowerdataArray[] = new String[EEGPOWER_SIZE];
	private int eegpowerdataCounter = 0;
	SimpleDateFormat ft = new SimpleDateFormat("dd-MMM-yyyy hh-mm-ss-SSS");
	SimpleDateFormat ft2 = new SimpleDateFormat("hh-mm-ss-SSS");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.bluetoothdevice_view);
		try {
			sharedContext = this.createPackageContext("com.ionicframework.mindwavereader717990", Context.CONTEXT_INCLUDE_CODE);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		sharedPath = sharedContext.getExternalFilesDir(null).getAbsolutePath() + "/naveen1/";
		createDirectoryIfNeeded(sharedPath);
		initView();
		setUpDrawWaveView();

		try {
			// TODO	
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				Toast.makeText(
						this,
						"Please enable your Bluetooth and re-run this program !",
						Toast.LENGTH_LONG).show();
				finish();
//				return;
			}  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Log.i(TAG, "error:" + e.getMessage());
			return;
		}
	}

	//private TextView tv_ps = null;
	private TextView tv_attention = null;
	private TextView tv_meditation = null;
	private TextView tv_delta = null;
	private TextView tv_theta = null;
	private TextView tv_lowalpha = null;
	
	private TextView  tv_highalpha = null;
	private TextView  tv_lowbeta = null;
	private TextView  tv_highbeta = null;
	
	private TextView  tv_lowgamma = null;
	private TextView  tv_middlegamma  = null;
	//private TextView  tv_badpacket = null;
	
	private Button btn_start = null;
	private Button btn_stop = null;
	private Button btn_selectdevice = null;
	private LinearLayout wave_layout;
	
	private int badPacketCount = 0;

	private void initView() {
		//tv_ps = (TextView) findViewById(R.id.tv_ps);
		tv_attention = (TextView) findViewById(R.id.tv_attention);
		tv_meditation = (TextView) findViewById(R.id.tv_meditation);
		tv_delta = (TextView) findViewById(R.id.tv_delta);
		tv_theta = (TextView) findViewById(R.id.tv_theta);
		tv_lowalpha = (TextView) findViewById(R.id.tv_lowalpha);
		
		tv_highalpha = (TextView) findViewById(R.id.tv_highalpha);
		tv_lowbeta= (TextView) findViewById(R.id.tv_lowbeta);
		tv_highbeta= (TextView) findViewById(R.id.tv_highbeta);
		
		tv_lowgamma = (TextView) findViewById(R.id.tv_lowgamma);
		tv_middlegamma= (TextView) findViewById(R.id.tv_middlegamma);
		//tv_badpacket = (TextView) findViewById(R.id.tv_badpacket);
		
		
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		wave_layout = (LinearLayout) findViewById(R.id.wave_layout);
		
		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				badPacketCount = 0;
				showToast("connecting ...",Toast.LENGTH_SHORT);
				start();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(tgStreamReader != null){
					tgStreamReader.stop();
				}
			}

		});
		
		btn_selectdevice =  (Button) findViewById(R.id.btn_selectdevice);
		
		btn_selectdevice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				scanDevice();
			}

		});
	}
	
	private void start(){
		if(address != null){
			BluetoothDevice bd = mBluetoothAdapter.getRemoteDevice(address);
			createStreamReader(bd);

			tgStreamReader.connectAndStart();
		}else{
			showToast("Please select device first!", Toast.LENGTH_SHORT);
		}
	}

	public void stop() {
		if(tgStreamReader != null){
			tgStreamReader.stop();
			tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate 
			tgStreamReader = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(tgStreamReader != null){
			tgStreamReader.close();
			tgStreamReader = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		stop();
	}

	// TODO view
	DrawWaveView waveView = null;
	// (2) demo of drawing ECG, set up of view
	public void setUpDrawWaveView() {
		
		waveView = new DrawWaveView(getApplicationContext());
		wave_layout.addView(waveView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		waveView.setValue(2048, 2048, -2048);
	}
	// (2) demo of drawing ECG, update view
	public void updateWaveView(int data) {
		if (waveView != null) {
			waveView.updateData(data);
		}
	}
	private int currentState = 0;
	private TgStreamHandler callback = new TgStreamHandler() {

		@Override
		public void onStatesChanged(int connectionStates) {
			// TODO Auto-generated method stub
			//Log.d(TAG, "connectionStates change to: " + connectionStates);
			currentState  = connectionStates;
			switch (connectionStates) {
			case ConnectionStates.STATE_CONNECTED:
				//sensor.start();
				showToast("Connected", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_WORKING:
				//byte[] cmd = new byte[1];
				//cmd[0] = 's';
				//tgStreamReader.sendCommandtoDevice(cmd);
				LinkDetectedHandler.sendEmptyMessageDelayed(1234, 5000);
				break;
			case ConnectionStates.STATE_GET_DATA_TIME_OUT:
				//get data time out
				break;
			case ConnectionStates.STATE_COMPLETE:
				//read file complete
				break;
			case ConnectionStates.STATE_STOPPED:
				break;
			case ConnectionStates.STATE_DISCONNECTED:
				break;
			case ConnectionStates.STATE_ERROR:
				//Log.d(TAG,"Connect error, Please try again!");
				break;
			case ConnectionStates.STATE_FAILED:
				//Log.d(TAG,"Connect failed, Please try again!");
				break;
			}
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_STATE;
			msg.arg1 = connectionStates;
			LinkDetectedHandler.sendMessage(msg);
			

		}

		@Override
		public void onRecordFail(int a) {
			// TODO Auto-generated method stub
			Log.e(TAG,"onRecordFail: " +a);

		}

		@Override
		public void onChecksumFail(byte[] payload, int length, int checksum) {
			// TODO Auto-generated method stub
			
			badPacketCount ++;
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_BAD_PACKET;
			msg.arg1 = badPacketCount;
			LinkDetectedHandler.sendMessage(msg);

		}

		@Override
		public void onDataReceived(int datatype, int data, Object obj) {
			// TODO Auto-generated method stub
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = datatype;
			msg.arg1 = data;
			msg.obj = obj;
			LinkDetectedHandler.sendMessage(msg);
			//Log.i(TAG,"onDataReceived");
		}

	};

	private boolean isPressing = false;
	private static final int MSG_UPDATE_BAD_PACKET = 1001;
	private static final int MSG_UPDATE_STATE = 1002;
	private static final int MSG_CONNECT = 1003;
	private boolean isReadFilter = false;

	int raw;
	private Handler LinkDetectedHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1234:
        		tgStreamReader.MWM15_getFilterType();
        		isReadFilter = true;
        		//Log.d(TAG,"MWM15_getFilterType ");
        		
        		break;
        	case 1235:
        		tgStreamReader.MWM15_setFilterType(FilterType.FILTER_60HZ);
        		//Log.d(TAG,"MWM15_setFilter  60HZ");
        		LinkDetectedHandler.sendEmptyMessageDelayed(1237, 1000);
        		break;
        	case 1236:
        		tgStreamReader.MWM15_setFilterType(FilterType.FILTER_50HZ);
        		//Log.d(TAG,"MWM15_SetFilter 50HZ ");
        		LinkDetectedHandler.sendEmptyMessageDelayed(1237, 1000);
        		break;
        		
			case 1237:
        		tgStreamReader.MWM15_getFilterType();
        		//Log.d(TAG,"MWM15_getFilterType ");
        		
        		break;
        		
        	case MindDataType.CODE_FILTER_TYPE:
        		//Log.d(TAG,"CODE_FILTER_TYPE: " + msg.arg1 + "  isReadFilter: " + isReadFilter);
        		if(isReadFilter){
        			isReadFilter = false;
        			if(msg.arg1 == FilterType.FILTER_50HZ.getValue()){
        				LinkDetectedHandler.sendEmptyMessageDelayed(1235, 1000);
        			}else if(msg.arg1 == FilterType.FILTER_60HZ.getValue()){
        				LinkDetectedHandler.sendEmptyMessageDelayed(1236, 1000);
        			}else{
        				Log.e(TAG,"Error filter type");
        			}
        		}
        		
        		break;
        		
        		
        		
			case MindDataType.CODE_RAW:
				if(rawdataCounter >= SIZE)
				{
					writeDataToFile(rawdataArray,0);
					rawdataArray = new String[SIZE];
					rawdataCounter = 0;
				}
				if (!isPressing) {
					rawdataArray[rawdataCounter++] = Integer.toString(msg.arg1);
					rawdataArray[rawdataCounter++] = ft2.format(new Date());
					//Log.d("Data", Integer.toString(msg.arg1));
				} else {
					rawdataArray[rawdataCounter++] = Integer.toString(msg.arg1);
					rawdataArray[rawdataCounter++] = ft2.format(new Date());
					//Log.d("Data", "Not is use" + Integer.toString(msg.arg1));
				}
					updateWaveView(msg.arg1);
				break;
			case MindDataType.CODE_MEDITATION:
				if(meditationdataCounter >= MEDITATION_SIZE)
				{
					writeDataToFile(meditationdataArray,1);
					meditationdataArray = new String[MEDITATION_SIZE];
					meditationdataCounter = 0;
				}
				if (!isPressing) {
					meditationdataArray[meditationdataCounter++] = Integer.toString(msg.arg1);
					meditationdataArray[meditationdataCounter++] = ft2.format(new Date());
					//Log.d("Data", Integer.toString(msg.arg1));
				} else {
					meditationdataArray[meditationdataCounter++] = Integer.toString(msg.arg1);
					meditationdataArray[meditationdataCounter++] = ft2.format(new Date());
					//Log.d("Data", "Not is use" + Integer.toString(msg.arg1));
				}
				//Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
				tv_meditation.setText("" +msg.arg1 );
				break;
			case MindDataType.CODE_ATTENTION:
				if(attentiondataCounter >= ATTENTION_SIZE)
				{
					writeDataToFile(attentiondataArray,2);
					attentiondataArray = new String[ATTENTION_SIZE];
					attentiondataCounter = 0;
				}
				if (!isPressing) {
					attentiondataArray[attentiondataCounter++] = Integer.toString(msg.arg1);
					attentiondataArray[attentiondataCounter++] = ft2.format(new Date());
					//Log.d("Data", Integer.toString(msg.arg1));
				} else {
					attentiondataArray[attentiondataCounter++] = Integer.toString(msg.arg1);
					attentiondataArray[attentiondataCounter++] = ft2.format(new Date());
					//Log.d("Data", "Not is use" + Integer.toString(msg.arg1));
				}
				//Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
				tv_attention.setText("" +msg.arg1 );
				break;
			case MindDataType.CODE_EEGPOWER:
				if(eegpowerdataCounter >= EEGPOWER_SIZE)
				{
					writeDataToFile(eegpowerdataArray,3);
					eegpowerdataArray = new String[EEGPOWER_SIZE];
					eegpowerdataCounter = 0;
				}
				EEGPower power = (EEGPower)msg.obj;
				if(power.isValidate()){
					if (!isPressing) {
						eegpowerdataArray[eegpowerdataCounter++] = Integer.toString(power.delta)+","+ Integer.toString(power.theta)+","+
								Integer.toString(power.lowAlpha)+","+Integer.toString(power.highAlpha)+","+
								Integer.toString(power.lowBeta)+","+Integer.toString(power.highBeta)+","+
								Integer.toString(power.lowGamma)+","+Integer.toString(power.middleGamma);
						eegpowerdataArray[eegpowerdataCounter++] = ft2.format(new Date());
					} else {
						eegpowerdataArray[eegpowerdataCounter++] = Integer.toString(power.delta)+","+ Integer.toString(power.theta)+","+
								Integer.toString(power.lowAlpha)+","+Integer.toString(power.highAlpha)+","+
								Integer.toString(power.lowBeta)+","+Integer.toString(power.highBeta)+","+
								Integer.toString(power.lowGamma)+","+Integer.toString(power.middleGamma);
						eegpowerdataArray[eegpowerdataCounter++] = ft2.format(new Date());
					}
					tv_delta.setText("" +power.delta);
					tv_theta.setText("" +power.theta);
					tv_lowalpha.setText("" +power.lowAlpha);
					tv_highalpha.setText("" +power.highAlpha);
					tv_lowbeta.setText("" +power.lowBeta);
					tv_highbeta.setText("" +power.highBeta);
					tv_lowgamma.setText("" +power.lowGamma);
					tv_middlegamma.setText("" +power.middleGamma);
				}
				break;
			case MindDataType.CODE_POOR_SIGNAL://
				int poorSignal = msg.arg1;
				//Log.d(TAG, "poorSignal:" + poorSignal);
				//tv_ps.setText(""+msg.arg1);
				if (poorSignal == 200) {
					isPressing = true;  // when device not in use
				}
				if (poorSignal == 0) {//
					if (isPressing) {
						isPressing = false;   // when device in use
					}
				}
				break;
			case MSG_UPDATE_BAD_PACKET:
				//tv_badpacket.setText("" + msg.arg1);
				
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
	public void showToast(final String msg,final int timeStyle){
		BluetoothDeviceDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
	
	//show device list while scanning
	private ListView list_select;
	private BTDeviceListAdapter deviceListApapter = null;
	private Dialog selectDialog;
	
	// (3) Demo of getting Bluetooth device dynamically
    public void scanDevice(){

    	if(mBluetoothAdapter.isDiscovering()){
    		mBluetoothAdapter.cancelDiscovery();
    	}
    	
    	setUpDeviceListView();
    	//register the receiver for scanning
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
    	
    	mBluetoothAdapter.startDiscovery();
    }
    
 private void setUpDeviceListView(){
    	
    	LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.dialog_select_device, null);
		list_select = (ListView) view.findViewById(R.id.list_select);
		selectDialog = new Dialog(this, R.style.dialog1);
		selectDialog.setContentView(view);
    	//List device dialog

    	deviceListApapter = new BTDeviceListAdapter(this);
    	list_select.setAdapter(deviceListApapter);
    	list_select.setOnItemClickListener(selectDeviceItemClickListener);
    	
    	selectDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				Log.e(TAG,"onCancel called!");
				BluetoothDeviceDemoActivity.this.unregisterReceiver(mReceiver);
			}
    		
    	});
    	
    	selectDialog.show();
    	
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	for(BluetoothDevice device: pairedDevices){
    		deviceListApapter.addDevice(device);
    	}
		deviceListApapter.notifyDataSetChanged();
    }
 
 //Select device operation
 private OnItemClickListener selectDeviceItemClickListener = new OnItemClickListener(){
	 
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
			// TODO Auto-generated method stub
			//Log.d(TAG, "Rico ####  list_select onItemClick     ");
	    	if(mBluetoothAdapter.isDiscovering()){
	    		mBluetoothAdapter.cancelDiscovery();
	    	}
	    	//unregister receiver
	    	BluetoothDeviceDemoActivity.this.unregisterReceiver(mReceiver);

	    	mBluetoothDevice =deviceListApapter.getDevice(arg2);
	    	selectDialog.dismiss();
	    	selectDialog = null;
	    	
			//Log.d(TAG,"onItemClick name: "+mBluetoothDevice.getName() + " , address: " + mBluetoothDevice.getAddress() );
			address = mBluetoothDevice.getAddress().toString();
			
			//ger remote device
			BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDevice.getAddress().toString());
         
			//bind and connect
			//bindToDevice(remoteDevice); // create bond works unstable on Samsung S5
			//showToast("pairing ...",Toast.LENGTH_SHORT);

			tgStreamReader = createStreamReader(remoteDevice); 
			tgStreamReader.connectAndStart();
		
		}
	
 };
 
 /**
	 * If the TgStreamReader is created, just change the bluetooth
	 * else create TgStreamReader, set data receiver, TgStreamHandler and parser
	 * @param bd
	 * @return TgStreamReader
	 */
	public TgStreamReader createStreamReader(BluetoothDevice bd){

		if(tgStreamReader == null){
			// Example of constructor public TgStreamReader(BluetoothDevice mBluetoothDevice,TgStreamHandler tgStreamHandler)
			tgStreamReader = new TgStreamReader(bd,callback);
			tgStreamReader.startLog();
		}else{
			// (1) Demo of changeBluetoothDevice
			tgStreamReader.changeBluetoothDevice(bd);
			
			// (4) Demo of setTgStreamHandler, you can change the data handler by this function
			tgStreamReader.setTgStreamHandler(callback);
		}
		return tgStreamReader;
	}
 
 /**
  * Check whether the given device is bonded, if not, bond it 
  * @param bd
  */
 public void bindToDevice(BluetoothDevice bd){
 	    int ispaired = 0;
		if(bd.getBondState() != BluetoothDevice.BOND_BONDED){
			//ispaired = remoteDevice.createBond();
			try {
				//Set pin
				if(Utils.autoBond(bd.getClass(), bd, "0000")){
					ispaired += 1;
				}
				//bind to device
				if(Utils.createBond(bd.getClass(), bd)){
					ispaired += 2;
				}
				Method createCancelMethod=BluetoothDevice.class.getMethod("cancelBondProcess");
                boolean bool=(Boolean)createCancelMethod.invoke(bd);
                //Log.d(TAG,"bool="+bool);
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//Log.d(TAG, " paire device Exception:    " + e.toString());
			}
		}
		//Log.d(TAG, " ispaired:    " + ispaired);

 }
 
//The BroadcastReceiver that listens for discovered devices 
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
				//Log.d(TAG, "mReceiver()");
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//Log.d(TAG,"mReceiver found device: " + device.getName());
				
				// update to UI
				deviceListApapter.addDevice(device);
				deviceListApapter.notifyDataSetChanged();

			} 
		}
	};

	private void createDirectoryIfNeeded(String path)
	{
		path = path.substring(0,path.length()-1);
		File file = new File(path);
		if (!file.exists()) {
			if (file.mkdir()) {
				//System.out.println("Directory is created!");
			} else {
				//System.out.println("Failed to create directory!");
			}
		}
	}

	public void writeDataToFile(String a[],int flag) {
		try {
			Date dNow = new Date( );
			SimpleDateFormat ft1 = new SimpleDateFormat("dd-MMM-yyyy");
			String name = ft.format(dNow) + ".txt";
			createDirectoryIfNeeded(sharedPath+ft1.format(dNow)+"/");
			if(flag == 0)
			{
				createDirectoryIfNeeded(sharedPath+ft1.format(dNow)+"/rawdata/");
				File file = new File(sharedPath +ft1.format(dNow)+"/rawdata/"+ name);
				rawdatafileOutputStream = new FileOutputStream(file, true);
				for (int i = 0; i < a.length; i++) {
					rawdatafileOutputStream.write(a[i].getBytes());
					rawdatafileOutputStream.write(",".getBytes());
					rawdatafileOutputStream.write(a[++i].getBytes());
					rawdatafileOutputStream.write(System.getProperty("line.separator").getBytes());
				}
				rawdatafileOutputStream.close();
				Log.d("Data", "Raw Data File Write successfully");
			}
			else if(flag == 1)
			{
				createDirectoryIfNeeded(sharedPath+ft1.format(dNow)+"/meditation/");
				File file = new File(sharedPath +ft1.format(dNow)+"/meditation/"+ name);
				meditationdatafileOutputStream = new FileOutputStream(file, true);
				for (int i = 0; i < a.length; i++) {
					meditationdatafileOutputStream.write(a[i].getBytes());
					meditationdatafileOutputStream.write(",".getBytes());
					meditationdatafileOutputStream.write(a[++i].getBytes());
					meditationdatafileOutputStream.write(System.getProperty("line.separator").getBytes());
				}
				meditationdatafileOutputStream.close();
				Log.d("Data", "Meditation Data File Write successfully");
			}
			else if(flag == 2)
			{
				createDirectoryIfNeeded(sharedPath+ft1.format(dNow)+"/attention/");
				File file = new File(sharedPath +ft1.format(dNow)+"/attention/"+ name);
				attentiondatafileOutputStream = new FileOutputStream(file, true);
				for (int i = 0; i < a.length; i++) {
					attentiondatafileOutputStream.write(a[i].getBytes());
					attentiondatafileOutputStream.write(",".getBytes());
					attentiondatafileOutputStream.write(a[++i].getBytes());
					attentiondatafileOutputStream.write(System.getProperty("line.separator").getBytes());
				}
				attentiondatafileOutputStream.close();
				Log.d("Data", "Attention Data File Write successfully");
			}
			else if(flag == 3)
			{
				createDirectoryIfNeeded(sharedPath+ft1.format(dNow)+"/eegpower/");
				File file = new File(sharedPath +ft1.format(dNow)+"/eegpower/"+ name);
				eegpowerfileOutputStream = new FileOutputStream(file, true);
				for (int i = 0; i < a.length; i++) {
					eegpowerfileOutputStream.write(a[i].getBytes());
					eegpowerfileOutputStream.write(",".getBytes());
					eegpowerfileOutputStream.write(a[++i].getBytes());
					eegpowerfileOutputStream.write(System.getProperty("line.separator").getBytes());
				}
				eegpowerfileOutputStream.close();
				Log.d("Data", "EEG Power Data File Write successfully");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
