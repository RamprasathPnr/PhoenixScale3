package net.nitiraj.phoenixscale3;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ScaleControlActivity extends AppCompatActivity implements BLEControllerCallback
{

    private TextView WeightDisplayM;
    private TextView MuDisplayM;

    View TareLEDM;
    View ZeroLEDM;
    View MRLEDM;
    View BattLEDM;
    View ACLEDM;

    TextView TareTextM;
    TextView ZeroTextM;
    TextView MRTextM;
    TextView BattTextM;
    TextView ACTextM;
    TextView DataFileNameM;

    String LastWeightM = "";
    String LastMuM = "";
    String ConnectedDeviceNameM;

    public String CurrentDataFileNameM = "";

    public String GetCurrentDataFileName()
    {
        return CurrentDataFileNameM;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        WeightDisplayM = (TextView) findViewById(R.id.WtDisplay);
        MuDisplayM = (TextView) findViewById(R.id.MuDisplay);

        TareLEDM = (View) findViewById(R.id.TareLED);
        ZeroLEDM = (View) findViewById(R.id.ZeroLED);
        MRLEDM = (View) findViewById(R.id.MRLED);
        BattLEDM = (View) findViewById(R.id.BATTLED);
        ACLEDM = (View) findViewById(R.id.ACLED);

        TareTextM = (TextView) findViewById(R.id.TareText);
        ZeroTextM = (TextView) findViewById(R.id.ZeroText);
        MRTextM = (TextView) findViewById(R.id.MRText);
        BattTextM = (TextView) findViewById(R.id.BATTText);
        ACTextM = (TextView) findViewById(R.id.ACText);

        BLEController TheBleControllerL = MainActivity.BLEControllerM;
        TheBleControllerL.SetCallBack(this);

        ConnectedDeviceNameM = TheBleControllerL.getConnectedDeviceName();

        setTitle(ConnectedDeviceNameM);

        mHandler.postDelayed(CheckDateNotification, 1000);
    }

    Date LastDateTimeM = new Date();
    private Runnable CheckDateNotification = new Runnable()
    {
        @Override
        public void run()
        {

            float TimeOffSetAllowedL = 2.0f;
            Date CurrentDateL = new Date();
            long MilliSecondsL = CurrentDateL.getTime() - LastDateTimeM.getTime();
            float SecondsL = (float)MilliSecondsL/1000f;
            if (SecondsL > TimeOffSetAllowedL)
            {
                LastWeightM = "";
                UpdateWeight("-----");
                MuDisplayM.setText("--");
                BLEController TheBleControllerL = MainActivity.BLEControllerM;
                TheBleControllerL.Disconnect();
                mHandler.postDelayed(ReconnectAttempt, 3000);
            }
            else
            {
                mHandler.postDelayed(CheckDateNotification, 1000);
            }
        }
    };


    boolean ActivityIsActiveM = false;
    private Runnable ReconnectAttempt = new Runnable()
    {
        @Override
        public void run()
        {
            if (!ActivityIsActiveM)
            {
                return;
            }
            BLEController TheBleControllerL = MainActivity.BLEControllerM;
            if (TheBleControllerL.IsConnectedM)
            {
                return;
            }
            TheBleControllerL.StartScan(false, 2);
            mHandler.postDelayed(ReconnectAttempt, 1000);
        }
    };

    @Override
    public void onResume()
    {
        ActivityIsActiveM = true;
        super.onResume();
    }



    @Override
    public void onPause()
    {
        BLEController TheBleControllerL = MainActivity.BLEControllerM;
        TheBleControllerL.Disconnect();
        ActivityIsActiveM = false;

        super.onPause();

    }
    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        // View is now detached, and about to be destroyed
    }

    @Override
    public void NotificationReceived(byte[] NotificationDataP)
    {
        mHandler.sendMessage(Message.obtain(null, MSG_NOTIFICATION, NotificationDataP));
    }

    @Override
    public void ErrorsOccured(String ErrorsP)
    {

    }

    @Override
    public void DeviceIsDisconnected()
    {

    }

    @Override
    public void OnBleScanComplete(HashMap<String, BluetoothDevice> DevicesP)
    {
        BLEController TheBleControllerL = MainActivity.BLEControllerM;
        if (!TheBleControllerL.IsConnectedM)
        {
            TheBleControllerL.ConnectToDevice(ConnectedDeviceNameM, false);
            if (TheBleControllerL.IsConnectedM)
            {
                mHandler.postDelayed(CheckDateNotification, 1000);
            }
        }
    }

    @Override
    public void ShowProgressMessage(String MessageP)
    {
    }

    private static final int MSG_NOTIFICATION = 101;
    private static final int MSG_PROGRESS = 201;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what)
            {
                case MSG_NOTIFICATION:
                    LastDateTimeM = new Date();
                    byte[] NotificationDataL = (byte[]) msg.obj;
                    ProcessNotification(NotificationDataL);

                    break;
                case MSG_PROGRESS:
                    String TraceMsgL = (String) msg.obj;
                    break;

            }
        }
    };



    void ProcessNotification(byte[] DataP)
    {
        int DataLengthL = DataP.length;
        if (DataLengthL < 2)
        {
            //packet needs to at least contain start byte and end byte
            return;
        }


        int PacketLengthL = DataP.length;
        int StartPacketIndexL = 0;
        while (DataP [StartPacketIndexL] != 0x21)
        {
            StartPacketIndexL++;
            if (StartPacketIndexL >= PacketLengthL)
            {
                return;//"!" not found in packet
            }
        }

        int EndPacketIndexL = StartPacketIndexL;
        while (DataP [EndPacketIndexL] != 0x23)
        {
            EndPacketIndexL++;
            if (EndPacketIndexL >= PacketLengthL)
            {
                return;//"#" not found in packet
            }
        }

        //strip the start and end bytes
        byte[] CleanPacketL = new byte[(EndPacketIndexL-StartPacketIndexL) - 1];
        System.arraycopy( DataP, StartPacketIndexL+1, CleanPacketL, 0, CleanPacketL.length );
        DataP = CleanPacketL;
        DataLengthL = DataP.length;

        boolean LEDDataPresentL = false;
        byte LastByteL = DataP[DataP.length - 1];
        String MeasuringUnitL = "";
        if (LastByteL <= 0x0F)
        {
            DataLengthL -= 1;
            LEDDataPresentL = true;

            if (DataLengthL < 3)
            {
                return;
            }

            if (DataP[DataLengthL-3]>0x39)
            {
                MeasuringUnitL = new String(DataP, DataLengthL - 4, 4);
                DataLengthL -= 4;
            }
        }
        String WeightDataL = new String(DataP, 0, DataLengthL);


        UpdateWeight(WeightDataL);
        MuDisplayM.setText(MeasuringUnitL.trim());

        LastWeightM = WeightDataL;
        LastMuM = MeasuringUnitL.trim();

        if (LEDDataPresentL)
        {
            UpdateLEDStatus(DataP[DataP.length - 1]);
        }
    }

    private void UpdateWeight(String WeightStringP)
    {
        WeightDisplayM.setText(WeightStringP);
    }

    private void ShowMessage(String MessageP)
    {
        AlertDialog.Builder MessageBoxBuilderL = new AlertDialog.Builder(this);
        MessageBoxBuilderL.setMessage(MessageP);

        MessageBoxBuilderL.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog MessageBoxL = MessageBoxBuilderL.create();
        MessageBoxL.show();
    }


    private void UpdateLEDStatus(Byte LedStatusByteP)
    {
        //:- TARE LED data = 0x04 (3rd  bit), ZERO LED data = 0x08 (4th  bit), COUNT LED data  = 0x10(5th  bit). BATT LED data = 0x20(6th  bit).
        boolean TareONL = false;
        boolean ZeroONL = false;
        boolean MRONL = false;
        boolean BattONL = false;
        boolean ACONL = false;

        TareONL = (LedStatusByteP & 0x01) != 0;
        ZeroONL = (LedStatusByteP & 0x02) != 0;
        MRONL = (LedStatusByteP & 0x4) != 0;
        BattONL = (LedStatusByteP & 0x8) != 0;
        ACONL = (LedStatusByteP & 0x8) == 0;

        TareLEDM.setVisibility(TareONL ? View.VISIBLE : View.INVISIBLE);
        ZeroLEDM.setVisibility(ZeroONL ? View.VISIBLE : View.INVISIBLE);
        MRLEDM.setVisibility(MRONL ? View.VISIBLE : View.INVISIBLE);
        BattLEDM.setVisibility(BattONL ? View.VISIBLE : View.INVISIBLE);
        ACLEDM.setVisibility(ACONL ? View.VISIBLE : View.INVISIBLE);

        int InactiveColorL = getResources().getColor(R.color.InactiveLedText);
        int ActiveColorL = getResources().getColor(R.color.ActiveLedText);
        if (TareONL) TareTextM.setTextColor(ActiveColorL); else TareTextM.setTextColor(InactiveColorL);
        if (ZeroONL) ZeroTextM.setTextColor(ActiveColorL); else ZeroTextM.setTextColor(InactiveColorL);
        if (MRONL) MRTextM.setTextColor(ActiveColorL); else MRTextM.setTextColor(InactiveColorL);
        if (BattONL) BattTextM.setTextColor(ActiveColorL); else BattTextM.setTextColor(InactiveColorL);
        if (ACONL) ACTextM.setTextColor(ActiveColorL); else ACTextM.setTextColor(InactiveColorL);


    }
}
