package smartmetawear.smarttemperaturehumidcontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;

import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Temperature.SensorType;

import bolts.Continuation;
import bolts.Task;

import com.mbientlab.metawear.android.BtleService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private BtleService.LocalBinder serviceBinder;
    private final String MW_MAC_ADDRESS= "D9:FE:65:69:EE:2F";
    static MetaWearBoard mwBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///< Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);

        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readtemp();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("Temperature Monitor", "Service connected");
        connectBLE();
    }

    private void connectBLE() {
        final BluetoothManager btManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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
    public void onServiceDisconnected(ComponentName componentName) { }

    public void readtemp()
    {
        final Random r = new Random();

        Temperature temp = mwBoard.getModule(Temperature.class);
        Log.i("Tempsens", String.valueOf(temp.sensors().length));
        final Temperature.Sensor tempSensor = temp.findSensors(SensorType.PRESET_THERMISTOR)[0];
        tempSensor.addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        TextView tmpDisplay = (TextView) findViewById(R.id.TmpDisplay);
                        //tmpDisplay.append("STarting \n"  );
                        String x = "Temperature1987 (C) = " + data.value(Float.class);
                        //Log.i("tempsens", "Temperature1987 (C) = " + data.value(Float.class));
                        Log.i("tempsens", x );

                        //tmpDisplay.setText("Temp is "+ String.valueOf( r.nextFloat())  + data.value(Float.class));
                        tmpDisplay.append("Temp is "  + x.replace('.',' ') );
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                tempSensor.read();
                return null;
            }
        });

    }

}