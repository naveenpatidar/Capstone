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
import android.widget.Toast;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.BodyDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Naveen Patidar on 23-Feb-17.
 */

public class FileWriterService extends Service {
    private TgStreamReader tgStreamReader;
    private String address = null;
    private BluetoothAdapter bluetoothAdapter;
    private FileOutputStream fileOutputStream;
    private final String TAG = "FileWriterService";
    private Context sharedContext = null;
    private static final int SIZE = 10000;
    int dataArray[] = new int[SIZE];
   // private int fileCounter = 0;
    private int dataCounter = 0;
    private String sharedPath;
    private String file_name = "Naveen";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (tgStreamReader != null) {
            tgStreamReader.stop();
        }
        Log.d("serviceStatus", "service is stopped..");
        Toast.makeText(getApplicationContext(), "Data recording stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("serviceStatus", "service started");
        address = intent.getStringExtra("address");
        try {


            //fileOutputStream = getApplicationContext().openFileOutput("Naveen.txt",Context.MODE_APPEND);
            //String string = "Hello Naveen Patidar inside service!";

 /*
            fileOutputStream.write("Daka".getBytes());
            //fileOutputStream.write(string.getBytes());
            fileOutputStream.write(Integer.toString(-121).getBytes());
            fileOutputStream.close();
            */
            sharedContext = this.createPackageContext("com.ionicframework.mindwavereader717990", Context.CONTEXT_INCLUDE_CODE);
            if(sharedContext == null)
                stopSelf();
            //sharedPath = sharedContext.getFilesDir().getAbsolutePath() + "/naveen/";
            sharedPath = sharedContext.getExternalFilesDir(null).getAbsolutePath() + "/naveen/";
            createDirectoryIfNeeded(sharedPath); // if naveen folder is not present in Mindwave reader directory
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
            tgStreamReader = createStreamReader(remoteDevice);
            tgStreamReader.connectAndStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_REDELIVER_INTENT;
    }

    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {

            // TODO Auto-generated method stub
            //  Log.d("Path","Connection state value.. "+Integer.toString(connectionStates));
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTED:
                    Log.d("Path", "STATE_CONNECTED");
                    break;
                case ConnectionStates.STATE_WORKING:
                    byte[] cmd = new byte[1];
                    cmd[0] = 's';
                    tgStreamReader.sendCommandtoDevice(cmd);
                    Log.d("Path", "STATE_WORKING");
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
        }

        @Override
        public void onRecordFail(int a) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // TODO Auto-generated method stub

            badPacketCount++;
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
            switch (msg.what) {
                case BodyDataType.CODE_RAW:
                    if(dataCounter >= SIZE)
                    {
                        writeDataToFile(dataArray);
                        dataArray = new int[SIZE];
                        dataCounter = 0;
                    }

                    if (!isPressing) {
                            dataArray[dataCounter++] = msg.arg1;
//                            fileOutputStream.write(Integer.toString(msg.arg1).getBytes());
//                            fileOutputStream.write(System.getProperty("line.separator").getBytes());
                        //raw_data.setText("Device in use : " + msg.arg1);
                        Log.d("Data", Integer.toString(msg.arg1));
                    } else {
                        //raw_data.setText("Device not in use : " + msg.arg1);
                        //   Log.d("Path","Inside handler LinkDetectedHandler is pressing else");
//                        try {
//                            fileOutputStream.write(Integer.toString(msg.arg1).getBytes());
//                            fileOutputStream.write(System.getProperty("line.separator").getBytes());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        dataArray[dataCounter++] = msg.arg1;
                        Log.d("Data", "Not is use" + Integer.toString(msg.arg1));
                    }
                    break;
                case BodyDataType.CODE_HEATRATE:
                    //tv_hr.setText("" +msg.arg1 );
                    break;

                case BodyDataType.CODE_POOR_SIGNAL:
                    int poorSignal = msg.arg1;
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

                case MSG_UPDATE_STATE:
                    //tv_connection.setText(""+msg.arg1);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    /**
     * If the TgStreamReader is created, just change the bluetooth
     * else create TgStreamReader, set data receiver, TgStreamHandler and parser
     *
     * @param bd
     * @return TgStreamReader
     */
    public TgStreamReader createStreamReader(BluetoothDevice bd) {

        if (tgStreamReader == null) {
            // Example of constructor public TgStreamReader(BluetoothDevice mBluetoothDevice,TgStreamHandler tgStreamHandler)
            tgStreamReader = new TgStreamReader(bd, callback);
            tgStreamReader.startLog();
        } else {
            // (1) Demo of changeBluetoothDevice
            tgStreamReader.changeBluetoothDevice(bd);
            // (4) Demo of setTgStreamHandler, you can change the data handler by this function
            tgStreamReader.setTgStreamHandler(callback);
        }
        return tgStreamReader;
    }

    public void writeDataToFile(int a[]) {
        try {
            //String name = file_name + (fileCounter) + ".txt";
            Date dNow = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat("dd-MMM-yyyy hh-mm-ss-SSSS");
            SimpleDateFormat ft1 = new SimpleDateFormat("dd-MMM-yyyy");
            String name = ft.format(dNow) + ".txt";
            createDirectoryIfNeeded(sharedPath+ft1.format(dNow)+"/");
            File file = new File(sharedPath +ft1.format(dNow)+"/"+ name);
            fileOutputStream = new FileOutputStream(file, true);
            for (int i = 0; i < a.length; i++) {
                fileOutputStream.write(Integer.toString(a[i]).getBytes());
                fileOutputStream.write(System.getProperty("line.separator").getBytes());
            }
            //fileCounter++;
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

}

//-----------------------------------------------------------------------------------------------------------------------
/*
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

        import java.io.FileOutputStream;
        import java.io.IOException;

*/
/**
 * Created by Naveen Patidar on 23-Feb-17.
 * <p>
 * If the TgStreamReader is created, just change the bluetooth
 * else create TgStreamReader, set data receiver, TgStreamHandler and parser
 *
 * @param bd
 * @return TgStreamReader
 *//*


public class FileWriterService extends Service {
    private TgStreamReader tgStreamReader;
    private String address = null;
    private BluetoothAdapter bluetoothAdapter;
    private FileOutputStream fileOutputStream;
    private final String TAG = "FileWriterService";
    private Context sharedContext = null;
    int dataArray[] = new int[30000];
    private int dataCounter = 0;
    private String sharedPath;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(tgStreamReader != null){
            tgStreamReader.stop();
        }
        Log.d("serviceStatus","service is stopped..");
        super.onDestroy();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("serviceStatus","service started");
        address = intent.getStringExtra("address");
        try {
            fileOutputStream = getApplicationContext().openFileOutput("Naveen.txt",Context.MODE_APPEND);
            //String string = "Hello Naveen Patidar inside service!";

 */
/*
            fileOutputStream.write("Daka".getBytes());
            //fileOutputStream.write(string.getBytes());
            fileOutputStream.write(Integer.toString(-121).getBytes());
            fileOutputStream.close();
            *//*


            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
            tgStreamReader = createStreamReader(remoteDevice);
            tgStreamReader.connectAndStart();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // TODO Auto-generated method stub
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
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
            switch (msg.what) {
                case BodyDataType.CODE_RAW:
                    if(!isPressing){
                        try {
                            fileOutputStream.write(Integer.toString(msg.arg1).getBytes());
                            fileOutputStream.write(System.getProperty("line.separator").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //raw_data.setText("Device in use : " + msg.arg1);
                        Log.d("Data",Integer.toString(msg.arg1));
                    }else{
                        //raw_data.setText("Device not in use : " + msg.arg1);
                        //   Log.d("Path","Inside handler LinkDetectedHandler is pressing else");
                        try {
                            fileOutputStream.write(Integer.toString(msg.arg1).getBytes());
                            fileOutputStream.write(System.getProperty("line.separator").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("Data","Not is use"+Integer.toString(msg.arg1));
                    }
                    break;
                case BodyDataType.CODE_HEATRATE:
                    //tv_hr.setText("" +msg.arg1 );
                    break;

                case BodyDataType.CODE_POOR_SIGNAL:
                    int poorSignal = msg.arg1;
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

                case MSG_UPDATE_STATE:
                    //tv_connection.setText(""+msg.arg1);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };



    */
/**
 * If the TgStreamReader is created, just change the bluetooth
 * else create TgStreamReader, set data receiver, TgStreamHandler and parser
 * @param bd
 * @return TgStreamReader
 *//*

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
}
*/
