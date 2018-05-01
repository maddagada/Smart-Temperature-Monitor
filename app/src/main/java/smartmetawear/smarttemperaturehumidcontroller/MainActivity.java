package smartmetawear.smarttemperaturehumidcontroller;


import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Environment;

import com.mbientlab.metawear.CodeBlock;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;

import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
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


public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private BtleService.LocalBinder serviceBinder;

    //Storing MAC address of MetaTracker, final key word will prevent this variable from updating from getting updated
    private final String MW_MAC_ADDRESS= "D9:FE:65:69:EE:2F";
    static MetaWearBoard mwBoard;
    static Logging logging;
    static boolean stopStream = false;
    private String fileName= "";
    private File file;
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
                try {
                    //downloadfile();
                    downloaddata();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        findViewById(R.id.startLogging).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    downloaddata();
                    SendEmail();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // OnClick event for the stop streaming data.
        findViewById(R.id.stopStream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView vi = (TextView)findViewById(R.id.TmpDisplay);
                //vi.setText("");
                stopStream =  true;
            }
        });

        // OnClick event for the stop streaming data.
        findViewById(R.id.Clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView vi = (TextView)findViewById(R.id.TmpDisplay);
                vi.setText("");
                //stopStream =  true;
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

    //Method to start logging data on the device
    public void logstart()
    {
        TextView t = (TextView) (findViewById(R.id.TmpDisplay));
                t.setText("\n Started Logging. Press download button to stop logging on the sensor and download the file \n");
        logging = mwBoard.getModule(Logging.class);
        logging.start(true);
        logtemperature();
    }
    //Method to stop logging data on the device
    public void logstop()
    {
        logging.stop();
    }

    //Method to download logged data from the device. Not using this method as device memory is low.
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

    public void downloaddata() throws IOException {
        if (logging != null) {
            logging.stop();
        }

        Date td = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd'-'hh:mm:ss");

        fileName = "METAWEARE_READING_" + ft.format(td) + ".csv";

        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/" + fileName);
        file.createNewFile();
        FileOutputStream outputStream = null;

        outputStream = new FileOutputStream(file, true);
        TextView t = (TextView) findViewById(R.id.TmpDisplay);

        outputStream.write((t.getText().toString()).getBytes());
        outputStream.close();
    }

    //Method to send email with an attachment of temperature readings
    // Some has to explicitly click send in order to send the email, its a security measure of the android system.
    public void SendEmail()
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        String message="File to be shared is .";
        intent.putExtra(Intent.EXTRA_SUBJECT, "Metware Report");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://"+file.getPath()));
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setData(Uri.parse("mailto:ch.mounikachowdary@gmail.com"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
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

                        String x = " (C) = " + data.value(Float.class);
                        Log.i("tempsens", x );

                        //tmpDisplay.setText("Temp is "+ String.valueOf( r.nextFloat())  + data.value(Float.class));
                        tmpDisplay.append("Temp reading at  "+ dateFormat.format(date) + String.valueOf(x) +"\n" );
                        Float value = data.value(Float.class);
                        if(value < 21 || value > 24) {
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