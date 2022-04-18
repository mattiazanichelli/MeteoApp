package ch.supsi.dti.isin.meteoapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.Objects;
import java.util.UUID;

import ch.supsi.dti.isin.meteoapp.R;
import ch.supsi.dti.isin.meteoapp.activities.MainActivity;
import ch.supsi.dti.isin.meteoapp.database.DBHelper;
import ch.supsi.dti.isin.meteoapp.database.DBSchema;
import ch.supsi.dti.isin.meteoapp.model.Location;
import ch.supsi.dti.isin.meteoapp.model.LocationsHolder;
import ch.supsi.dti.isin.meteoapp.service.Fetcher;
import ch.supsi.dti.isin.meteoapp.utility.VolleyCallback;

public class DetailLocationFragment extends Fragment implements VolleyCallback {
    private static final String ARG_LOCATION_ID = "location_id";

    private Location mLocation;

    private TextView city, temperature, humidity, description;

    public static DetailLocationFragment newInstance(UUID locationId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_LOCATION_ID, locationId);

        DetailLocationFragment fragment = new DetailLocationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        UUID locationId = (UUID) getArguments().getSerializable(ARG_LOCATION_ID);
        mLocation = LocationsHolder.get(getActivity()).getLocation(locationId);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        JsonObjectRequest jsonObjectRequest = Fetcher.getLocationInfo(mLocation.getName(), DetailLocationFragment.this);
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        queue.add(jsonObjectRequest);

        View v = inflater.inflate(R.layout.fragment_detail_location, container, false);

        ImageView mIdImageView = v.findViewById(R.id.weatherIcon);
        city = v.findViewById(R.id.citytv);
        temperature = v.findViewById(R.id.temperaturetv);
        humidity = v.findViewById(R.id.humiditytv);
        description = v.findViewById(R.id.descriptiontv);

        Location currentLocation = LocationsHolder.get(getActivity()).getLocation(mLocation.getId());

        mIdImageView.setImageDrawable(getDrawableById(currentLocation.getWeatherId()));
        city.setText(currentLocation.getName());
        temperature.setText(currentLocation.getTemperature());
        humidity.setText(currentLocation.getHumidity());
        description.setText(currentLocation.getDescription());

        return v;
    }

    private Drawable getDrawableById(int id) {
        Log.i("Weather ID", ""+id);

        Drawable drawable;

        switch(id - (id%100)) {
            case 200:
                drawable = getResources().getDrawable(R.drawable.thunderstorm);
                break;
            case 300:
                drawable = getResources().getDrawable(R.drawable.snowerrain);
                break;
            case 500:
                drawable = getResources().getDrawable(R.drawable.rain);
                break;
            case 600:
                drawable = getResources().getDrawable(R.drawable.snow);
                break;
            case 700:
                drawable = getResources().getDrawable(R.drawable.mist);
                break;
            default:
                if(id == 800) {
                    drawable = getResources().getDrawable(R.drawable.clearsky);
                } else {
                    drawable = getResources().getDrawable(R.drawable.scatteredclouds);
                }
                break;
        }
        return drawable;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_detail_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.menu_remove) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Remove Location");
            builder.setPositiveButton("Remove", (dialog, which) -> {
                LocationsHolder.get(getActivity()).getLocation(mLocation.getId());
                SQLiteDatabase mDatabase = new DBHelper(getContext()).getWritableDatabase();
                String selection = DBSchema.LocationsTable.Cols.NAME + " LIKE ?";
                String[] selectionArgs = {mLocation.getName()};
                int deletedRows = mDatabase.delete(DBSchema.LocationsTable.TABLE_NAME, selection, selectionArgs);
                Log.i("Deleted ", "nr: " + deletedRows);
                LocationsHolder.get(getContext()).getLocations().remove(mLocation);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSuccess(Location location) {
        int i = LocationsHolder.get(getContext()).getLocations().indexOf(mLocation);
        LocationsHolder.get(getActivity()).updateLocation(i, location);
        LocationsHolder.get(getActivity()).getLocations().get(i).setName(location.getName());
        Location current = LocationsHolder.get(getActivity()).getLocation(mLocation.getId());

        city.setText(current.getName());
        temperature.setText(current.getTemperature());
        humidity.setText(current.getHumidity());
        description.setText(current.getDescription());
    }
}

