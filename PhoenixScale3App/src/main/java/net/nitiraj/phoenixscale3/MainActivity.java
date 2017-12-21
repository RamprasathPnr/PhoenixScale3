package net.nitiraj.phoenixscale3;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
    static BLEController BLEControllerM;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }


        BluetoothAdapter BluetoothAdapterL = BluetoothAdapter.getDefaultAdapter();
        if (BluetoothAdapterL == null)
        {
            ShowMessage("There is no Bluetooth support on this device");
            return;
        }

        if (!BluetoothAdapterL.isEnabled())
        {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }


        BLEControllerM = new BLEController();
        BLEControllerM.SetContext(this);
        BLEControllerM.Initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_BT)
        {
            BluetoothAdapter BluetoothAdapterL = BluetoothAdapter.getDefaultAdapter();
            if (!BluetoothAdapterL.isEnabled())
            {
                finish();
            }
        }

    }

    public void ShowMessage(String MessageP)
    {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(MessageP);
        dlgAlert.setTitle("Message");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                });
        dlgAlert.create().show();
    }

    public void ScanForDevices(View view)
    {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }
}
