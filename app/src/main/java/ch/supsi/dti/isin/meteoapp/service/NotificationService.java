//package ch.supsi.dti.isin.meteoapp.service;
//
//import android.app.Activity;
//import android.app.AlarmManager;
//import android.app.IntentService;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.os.SystemClock;
//import android.util.Log;
//
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationCompat;
//
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//
//import org.json.JSONObject;
//
//import java.util.concurrent.TimeUnit;
//
//import ch.supsi.dti.isin.meteoapp.model.Location;
//import ch.supsi.dti.isin.meteoapp.utility.VolleyCallback;
//import io.nlopez.smartlocation.SmartLocation;
//import io.nlopez.smartlocation.location.config.LocationAccuracy;
//import io.nlopez.smartlocation.location.config.LocationParams;
//
//public class NotificationService extends IntentService {
//
//    private static final String TAG = "NotificationService";
//    private static final long POLL_INTERVALS_MS = TimeUnit.MINUTES.toMillis(1);
//    private Activity activity;
//
//    public NotificationService() {
//        super(TAG);
//    }
//
//    private void setActivity(Activity activity) {
//        this.activity = activity;
//    }
//
//    public static Intent newIntent(Context context) {
//        return new Intent(context, NotificationService.class);
//    }
//
//    public void setServiceAlarm(Context context, boolean isOn, Activity activity) {
//
//        setActivity(activity);
//
//        Intent intent = NotificationService.newIntent(context);
//        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        if(isOn) {
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
//                    POLL_INTERVALS_MS, pendingIntent);
//        } else {
//            alarmManager.cancel(pendingIntent);
//            pendingIntent.cancel();
//        }
//    }
//
//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        Log.i(TAG, "Received an intent: " + intent);
//
//        LocationParams.Builder builder = new LocationParams.Builder()
//                .setAccuracy(LocationAccuracy.HIGH)
//                .setDistance(0)
//                .setInterval(1000*60);
//        SmartLocation.with(activity).location().continuous().config(builder.build())
//                .start(location -> {
//                    Log.i("Weather Updated",location.toString());
//                    JsonObjectRequest jsonObjectRequest =
//                            Fetcher.getLocationInfo(location.getLatitude(), location.getLongitude(), NotificationService.this);
//                    RequestQueue  queue = Volley.newRequestQueue(activity);
//                    queue.add(jsonObjectRequest);
//                });
//    }
//
//    private void sendNotification(Location location) {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("default", "TEST_CHANNEL", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("Test Channel Description");
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
//                .setSmallIcon(android.R.drawable.ic_menu_report_image)
//                .setContentTitle("Current temperature")
//                .setContentText(location.getTemperature())
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        notificationManager.notify(0, builder.build());
//    }
//}
