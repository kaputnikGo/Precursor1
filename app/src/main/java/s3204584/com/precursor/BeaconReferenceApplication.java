package s3204584.com.precursor;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Created by Sticks on 14-Apr-16.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconRefApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;
    private final static boolean LOGGING = true;

    public void onCreate() {
        super.onCreate();
        //super.onCreate(savedInstanceState);
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        // altbeacon finds only its beacons, to use other types unrem and proper the below:
       /*
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
            setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        */
        if (LOGGING) Log.d(TAG, "Background monitoring and power save setup.");
        // wake up app when beacon seen
        Region region = new Region("background region", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        // constructing this class and its reference cause auto power save (~60%)
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        // simulator test code below, unrem:
        /*
        BeaconManager.setBeaconSimulator(new TimedBeaconSimulator());
        ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
        */
    }

    @Override
    public void didEnterRegion(Region arg0) {
        // sends notification when region matches the defined region above
        if (LOGGING) Log.d(TAG, "Entered region");
        if (!haveDetectedBeaconsSinceBoot) {
            if (LOGGING) Log.d(TAG, "auto launch activity");
            // first time since boot to detect, launch activity
            Intent intent = new Intent(this, MonitoringActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // ensure is single instance in manifest
            this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;
        }
        else {
            if (monitoringActivity != null) {
                // if is visible, add a log entry to it
                monitoringActivity.logToDisplay("Beacon spotted.");
            }
            else {
                // if not visible, send notification
                if (LOGGING) Log.d(TAG, "Send beacon notification.");
                sendNotification();
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (monitoringActivity != null) {
            monitoringActivity.logToDisplay("Beacon no longer in range");
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        if (monitoringActivity != null) {
            monitoringActivity.logToDisplay("Beacon detection state: " + state);
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                    .setContentTitle("Beacon Reference Application")
                    .setContentText("A beacon is nearby.")
                    .setSmallIcon(R.mipmap.ic_launcher);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }
}
