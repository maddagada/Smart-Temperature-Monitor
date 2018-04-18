package smartmetawear.smarttemperaturehumidcontroller;


import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mbientlab.metawear.CodeBlock;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;

import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.BarometerBosch;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Temperature.SensorType;

import bolts.Continuation;
import bolts.Task;

import com.mbientlab.metawear.android.BtleService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.RecursiveAction;

import com.mbientlab.metawear.module.Gpio;
import com.mbientlab.metawear.ForcedDataProducer;
import com.mbientlab.metawear.module.Timer;


public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private BtleService.LocalBinder serviceBinder;

    //Storing MAC address of MetaTracker, final key word will prevent this variable from updating from getting updated
    private final String MW_MAC_ADDRESS= "D9:FE:65:69:EE:2F";
    static MetaWearBoard mwBoard;
    static Logging logging;
    static boolean stopStream = false;
    ///Overriding onCreate method from the base class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///< Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);
        ///Adding listener for the button click
        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStream = false;
                readtemp();
            }
        });

        findViewById(R.id.downloadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //downloadfile();
                downloaddata();
            }
        });

        findViewById(R.id.startLogging).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logstart();
            }
        });
        // OnClick event for the stop streaming data.
        findViewById(R.id.stopStream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView vi = (TextView)findViewById(R.id.TmpDisplay);
                vi.setText("");
                stopStream =  true;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }
    //Override method for onServiceConnected
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("Temperature Monitor", "Service connected");
        /// Method to connect to the bluetooth
        connectBLE();
        //readtemp();
    }

    /// connectBLE method connects device using the MAC address of the MetaTracker
    private void connectBLE() {

        ///Creates an manager object for the android bluetooth service
        final BluetoothManager btManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        // Using the BluetoothManager it connects to the device with MAC Address defined in the variable (MW_MAC_ADDRESS).
        final BluetoothDevice remoteDevice =
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard = serviceBinder.getMetaWearBoard(remoteDevice);
        try {
            Task<Void> task = mwBoard.connectAsync();
            task.waitForCompletion();

            if (task.isFaulted()) {
                //throw task.getError();
            }
        }
        catch (InterruptedException e){}
        Log.i("Temeperature monitor", "Service Connected to ... " + MW_MAC_ADDRESS);

    }
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        //mwBoard.tearDown();
    }

    public void logstart()
    {
        TextView t = (TextView) (findViewById(R.id.TmpDisplay));
                t.setText("\n Started Logging. Press download button to stop logging on the sensor and download the file \n");
        logging = mwBoard.getModule(Logging.class);
        logging.start(true);
        logtemperature();
    }
    public void logstop()
    {
        logging.stop();
    }

    public void downloadfile()
    {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir("Documents", Context.MODE_PRIVATE);
            String filename = "meta-chmou.txt";

            File myInternalFile = new File(directory , filename);
            FileOutputStream fos = new FileOutputStream(myInternalFile);

            fos.write(((TextView)findViewById(R.id.TmpDisplay)).getText().toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
public void downloaddata() {
        if(logging != null) {
            logging.stop();
        }

    logging.downloadAsync(100, new Logging.LogDownloadUpdateHandler() {
        @Override
        public void receivedUpdate(long nEntriesLeft, long totalEntries) {
            TextView t = (TextView) (findViewById(R.id.TmpDisplay));
            t.setText("\n"+ totalEntries +" log entries written to the file. \n");

            Log.i("MainActivity", "Progress Update = " + nEntriesLeft + "/" + totalEntries);
        }
    }).continueWithTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
            Log.i("MainActivity", "Download completed");
            logging.clearEntries();
            return null;
        }
    });
    }

    // A method to read temperature from the Meta tracker
    public void readtemp()
    {
        //Creating the temperature module object from the Meta board
        Temperature temp = mwBoard.getModule(Temperature.class);
        //stopStream = false;
        //Logging number of temperature sensors
        Log.i("Tempsens", String.valueOf(temp.sensors().length));

        //getting the PRESET_THERMISTOR sensor. [0] gets the first PRESET_THERMISTOR sensor from the list
        final Temperature.Sensor tempSensor = temp.findSensors(SensorType.PRESET_THERMISTOR)[0];

        ///Creating a route Asynchronously to the PRESET_THERMISTOR
        tempSensor.addRouteAsync(new RouteBuilder() {
            @Override

            //Configuring the PRESET_THERMISTOR sensor
            public void configure(RouteComponent source) {
//                logstart();
                //Streams the Temperature data
                source.stream(new Subscriber() {

                    //Method to deal with the streamed data
                    @Override
                    public void apply(Data data, Object... env) {
                        if(stopStream){return;}
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();

                        //Getting the object TmpDisplay of type TextView
                        TextView tmpDisplay = (TextView) findViewById(R.id.TmpDisplay);
                        //tmpDisplay.append("STarting \n"  );
                        //Temperature c = data.value(Temperature.class);

                        String x = "CurrentTemperature (C) = " + data.value(Float.class);
                        Log.i("tempsens", x );

                        //tmpDisplay.setText("Temp is "+ String.valueOf( r.nextFloat())  + data.value(Float.class));
                        tmpDisplay.append("Temp reading at  "+ dateFormat.format(date) + String.valueOf(x) +"\n" );
                        Float value = data.value(Float.class);
                        if(value < 24 || value > 23) {
                            String phoneNo = "515-639-0144";
                            String message = "Temperature Alert  Current temperature reading " + value.toString();
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(phoneNo, null, message, null, null);
                        }
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                tempSensor.read();
                Log.i("task", "test");
                return null;
            }
        });

    }

    public void logtemperature()
    {
        //Creating the temperature module object from the Meta board
        Temperature temp = mwBoard.getModule(Temperature.class);

        //Logging number of temperature sensors
        Log.i("Tempsens", String.valueOf(temp.sensors().length));

        //getting the PRESET_THERMISTOR sensor. [0] gets the first PRESET_THERMISTOR sensor from the list
        final Temperature.Sensor tempSensor = temp.findSensors(SensorType.PRESET_THERMISTOR)[0];

        ///Creating a route Asynchronously to the PRESET_THERMISTOR
        tempSensor.addRouteAsync(new RouteBuilder() {
            @Override

            //Configuring the PRESET_THERMISTOR sensor
            public void configure(RouteComponent source) {
//                logstart();
                //Streams the Temperature data
                source.log(new Subscriber() {

                    //Method to deal with the streamed data
                    @Override
                    public void apply(Data data, Object... env) {
                        if(stopStream){ return;}
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();

                        //Getting the object TmpDisplay of type TextView
                        TextView tmpDisplay = (TextView) findViewById(R.id.TmpDisplay);
                        //tmpDisplay.append("STarting \n"  );
                        //Temperature c = data.value(Temperature.class);

                        String x = "CurrentTemperature (C) = " + data.value(Float.class);
                        Log.i("tempsens", x );

                        //tmpDisplay.setText("Temp is "+ String.valueOf( r.nextFloat())  + data.value(Float.class));
                        tmpDisplay.append("log reading at  "+ dateFormat.format(date) + String.valueOf(x) +"\n" );
                        // Alert the user when threshold limit exceeds or drop the value.
                        Float value = data.value(Float.class);
                        if(value > 25 || value < 24) {
                            String phoneNo = "515-639-0144";
                            String message = "Temperature log Alert  Current temperature reading " + value.toString();
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(phoneNo, null, message, null, null);
                        }
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                tempSensor.read();
                Log.i("task", "test");
                return null;
            }
        });

    }
}