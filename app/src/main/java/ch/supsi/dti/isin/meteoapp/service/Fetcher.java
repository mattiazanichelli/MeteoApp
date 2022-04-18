package ch.supsi.dti.isin.meteoapp.service;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import ch.supsi.dti.isin.meteoapp.fragments.ListFragment;
import ch.supsi.dti.isin.meteoapp.model.Location;
import ch.supsi.dti.isin.meteoapp.model.LocationsHolder;
import ch.supsi.dti.isin.meteoapp.utility.VolleyCallback;

public class Fetcher extends Worker{

    private static final String TAG = "MeteoApp";
    private static final String API_KEY = "13d162e0808841de659c750f9fb1dc1f";

    public Fetcher(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static JsonObjectRequest getLocationInfo(final double latitude, final double longitude, final VolleyCallback callback) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&units=metric&appid="+API_KEY;

        return new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                Log.i("Weather onResponse API", response.toString());
                JSONObject mainObject = response.getJSONObject("main");

                callback.onSuccess(new Location(response.getString("name"),
                        String.valueOf(latitude),
                        String.valueOf(longitude),
                        String.valueOf(mainObject.getDouble("temp")),
                        String.valueOf(mainObject.getDouble("humidity")),
                        response.getJSONArray("weather").getJSONObject(0).getString("description"),
                        response.getJSONArray("weather").getJSONObject(0).getInt("id")));

            } catch (JSONException ex) {
                Log.i("Weather onResponse API", ex.toString());
                ex.printStackTrace();
            }
        }, error -> Log.i("Weather API onError", error.toString()));
    }

    public static JsonObjectRequest getLocationInfo(String name, final VolleyCallback callback) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q="+name+"&units=metric&appid="+API_KEY;
        return new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                Log.i("Weather onResponse Name", response.toString());
                JSONObject mainObject = response.getJSONObject("main");
                JSONObject coordObject = response.getJSONObject("coord");

                callback.onSuccess(new Location(response.getString("name"),
                        String.valueOf(coordObject.getDouble("lat")),
                        String.valueOf(coordObject.getDouble("lon")),
                        String.valueOf(mainObject.getDouble("temp")),
                        String.valueOf(mainObject.getDouble("humidity")),
                        response.getJSONArray("weather").getJSONObject(0).getString("description"),
                        response.getJSONArray("weather").getJSONObject(0).getInt("id")));

            } catch (JSONException ex) {
                Log.i("Weather onResponse API", ex.toString());
                ex.printStackTrace();
            }
        }, error -> Log.i("Weather onResponse API", error.toString()));
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.v(TAG, "Inside Fetcher Worker");
        for(Location l : LocationsHolder.get(getApplicationContext()).getLocations()) {
            getLocationInfo(l.getName(), ListFragment.newInstance(l));
        }
        return Result.success();
    }
}