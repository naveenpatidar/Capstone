package naveenpatidar.mindwaverecorder;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.neurosky.connection.TgStreamReader;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;

import naveenpatidar.mindwaverecorder.services.FileWriterService;

public class ScanDevice extends AppCompatActivity implements View.OnClickListener{
    private TextView mStatusTextView;
    private GoogleApiClient mGoogleApiClient;
    private ImageView imgProfilePic;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private BTDeviceListAdapter deviceListApapter = null;
    private ListView list_select;
    private Dialog selectDialog;
    private String address = null;
    private TgStreamReader tgStreamReader;
    private Intent serviceIntent;

    private final String TAG = "scanDevice";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mStatusTextView = (TextView) findViewById(R.id.logindetails);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Intent i = getIntent();
        String name = i.getStringExtra("name");
        String email = i.getStringExtra("email");
        String phototurl = i.getStringExtra("photourl");

        mStatusTextView.setText(name + "\n\n" + email);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
            new LoadImageTask(imgProfilePic,getApplicationContext())
                    .execute(phototurl);
    }

    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(getApplicationContext() , Login.class);
                        startActivity(i);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(getApplicationContext() , Login.class);
                        startActivity(i);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
                scandevices();
                break;
            case R.id.stop:
                stopService(serviceIntent);
/*                String dir = getFilesDir().getAbsolutePath();
                File f = new File(dir,"Naveen.txt");
                Log.d(TAG,Long.toString(f.length()));
                Toast.makeText(this, "Service Stopped.. "+Long.toString(f.length()), Toast.LENGTH_SHORT).show();*/
                break;
        }
    }


    //Scan Device code from mhealth
    public void scandevices()
    {
        if(bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
        }

        setUpDeviceListView();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();


    }

    private void setUpDeviceListView(){
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(
                    this,
                    "Please Enable bluetooth!!",
                    Toast.LENGTH_LONG).show();
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_select_device, null);
        list_select = (ListView) view.findViewById(R.id.list_select);
        selectDialog = new Dialog(this, R.style.dialog1);
        selectDialog.setContentView(view);
        //List device dialog

        deviceListApapter = new BTDeviceListAdapter(this);
        list_select.setAdapter(deviceListApapter);
        list_select.setOnItemClickListener(selectDeviceItemClickListener);

        selectDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface arg0) {
                // TODO Auto-generated method stub
                ScanDevice.this.unregisterReceiver(mReceiver);
            }

        });

        selectDialog.show();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device: pairedDevices){
            deviceListApapter.addDevice(device);
        }
        deviceListApapter.notifyDataSetChanged();
    }


    //The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // update to UI
                deviceListApapter.addDevice(device);
                deviceListApapter.notifyDataSetChanged();
            }
        }
    };


    // List item listener
    private AdapterView.OnItemClickListener selectDeviceItemClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
            // TODO Auto-generated method stub
            if(bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.cancelDiscovery();
            }
            //unregister receiver
            ScanDevice.this.unregisterReceiver(mReceiver);

            bluetoothDevice =deviceListApapter.getDevice(arg2);
            selectDialog.dismiss();
            selectDialog = null;

            address = bluetoothDevice.getAddress().toString();

            //get remote device
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress().toString());
            //bind and connect
            bindToDevice(remoteDevice); // create bond works unstable on Samsung S5
            Toast.makeText(ScanDevice.this, "pairing..", Toast.LENGTH_SHORT).show();

            serviceIntent= new Intent(getApplicationContext(), FileWriterService.class);
            serviceIntent.putExtra("address", address);
            getApplicationContext().startService(serviceIntent);
        }
    };

    // Check whether the given device is bonded, if not, bond it
    public void bindToDevice(BluetoothDevice bd){
        int ispaired = 0;
        if(bd.getBondState() != BluetoothDevice.BOND_BONDED){
            //ispaired = bd.createBond();
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

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                Toast.makeText(this, "Signing out !!!!", Toast.LENGTH_SHORT).show();
                signOut();
                return true;

            case R.id.revoke_access:
                Toast.makeText(this, "Revoking Access", Toast.LENGTH_SHORT).show();
                revokeAccess();
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}

class LoadImageTask extends AsyncTask<String, String, Bitmap> {
    ImageView bmImage;
    Context ctx;
    public LoadImageTask(ImageView bmImage,Context ctx)
    {
        this.ctx = ctx;
        this.bmImage = bmImage;
    }
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        if(mIcon11 == null)
        {
            mIcon11 = BitmapFactory.decodeResource(ctx.getResources(),R.drawable.profile);
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}
