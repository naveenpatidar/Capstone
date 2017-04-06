package naveenpatidar.mindwaverecorder.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.BodyDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import naveenpatidar.mindwaverecorder.databaseConnection.DataBaseHelper;

/**
 * Created by Naveen Patidar on 18-Feb-17.
 */

public class DatabaseService extends Service{
    private DataBaseHelper dbHelper;
    private TgStreamReader tgStreamReader;
    Context sharedContext = null;
    private String address = null;
    private BluetoothAdapter bluetoothAdapter;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        address = intent.getStringExtra("address");
        try {
            sharedContext = this.createPackageContext("com.ionicframework.mindwavereader717990", Context.CONTEXT_INCLUDE_CODE);
            if (sharedContext == null) {
                stopSelf();
            }
        } catch (Exception e) {
            String error = e.getMessage();
            Log.v("Context","Shared context is not null "+error);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbHelper=new DataBaseHelper(sharedContext);
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
                tgStreamReader = createStreamReader(remoteDevice);
                //  Log.d("Path","After createstream..");
                tgStreamReader.connectAndStart();
                //  Log.d("Path","After connect and start");
            }
        }).start();
        return START_REDELIVER_INTENT;
    }

    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {

            // TODO Auto-generated method stub
            //  Log.d("Path","Connection state value.. "+Integer.toString(connectionStates));
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTED:
                        Log.d("Path","STATE_CONNECTED");
                    break;
                case ConnectionStates.STATE_WORKING:
                    byte[] cmd = new byte[1];
                    cmd[0] = 's';
                    tgStreamReader.sendCommandtoDevice(cmd);
                       Log.d("Path","STATE_WORKING");
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    //get data time out
                    //   Log.d("Path","STATE_GET_DATA_TIME_OUT");
                    break;
                case ConnectionStates.STATE_COMPLETE:
                    //read file complete
                    //  Log.d("Path","STATE_COMPLETE");
                    break;
                case ConnectionStates.STATE_STOPPED:
                    //  Log.d("Path","STATE_STOPPED");
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    //   Log.d("Path","STATE_DISCONNECTED");
                    break;
                case ConnectionStates.STATE_ERROR:
                    //  Log.d("Path","STATE_ERROR");
                    break;
                case ConnectionStates.STATE_FAILED:
                    //  Log.d("Path","STATE_FAILED");
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);
            // Log.d("Path","Inside State changed.");

        }

        @Override
        public void onRecordFail(int a) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // TODO Auto-generated method stub

            badPacketCount ++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);
            //  Log.d("Path","Onchecksum");
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // TODO Auto-generated method stub
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
            // Log.d("Path","On data received.");
        }
    };

    private boolean isPressing = false;
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;
    private static final int MSG_CONNECT = 1003;
    private int badPacketCount = 0;
    int raw;
    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            switch (msg.what) {


                case BodyDataType.CODE_RAW:
                    if(!isPressing){
                        dbHelper.insertRawData(msg.arg1);
                        //raw_data.setText("Device in use : " + msg.arg1);
                        //Log.d("Data",Integer.toString(msg.arg1));
                    }else{
                        //raw_data.setText("Device not in use : " + msg.arg1);
                        //   Log.d("Path","Inside handler LinkDetectedHandler is pressing else");
                        dbHelper.insertRawData(msg.arg1);
                        Log.d("Data","Not is use"+Integer.toString(msg.arg1));
                    }
                    //count++;
                    break;
                case BodyDataType.CODE_HEATRATE:
                    //tv_hr.setText("" +msg.arg1 );
                    //   Log.d("Path","Inside handler LinkDetectedHandler CODE_HEATRATE ");
                    break;

                case BodyDataType.CODE_POOR_SIGNAL:
                    int poorSignal = msg.arg1;
                    //tv_ps.setText(""+msg.arg1);
                    //  Log.d("Path","Inside handler LinkDetectedHandler CODE_POOR_SIGNAL");
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
                    //  Log.d("Path","Inside handler LinkDetectedHandler MSG_UPDATE_BAD_PACKET");
                    break;

                case MSG_UPDATE_STATE:
                    //tv_connection.setText(""+msg.arg1);
                    //  Log.d("Path","Inside handler LinkDetectedHandler MSG_UPDATE_STATE");
                    break;

                default:
                    //   Log.d("Path","Inside handler LinkDetectedHandler default");
                    break;
            }
            super.handleMessage(msg);
            // Log.d("Path","Inside handler LinkDetectedHandler");
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
            // Log.d("Path","Inside createStream if");
            tgStreamReader.startLog();
        }else{
            // (1) Demo of changeBluetoothDevice
            tgStreamReader.changeBluetoothDevice(bd);
            //  Log.d("Path","Inside createStream else");
            // (4) Demo of setTgStreamHandler, you can change the data handler by this function
            tgStreamReader.setTgStreamHandler(callback);
        }
        //  Log.d("Path","Inside createStream");
        return tgStreamReader;
    }
}
