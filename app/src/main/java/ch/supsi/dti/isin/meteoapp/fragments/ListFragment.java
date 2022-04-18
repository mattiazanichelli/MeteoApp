package ch.supsi.dti.isin.meteoapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;
import java.util.Objects;

import ch.supsi.dti.isin.meteoapp.R;
import ch.supsi.dti.isin.meteoapp.activities.DetailActivity;
import ch.supsi.dti.isin.meteoapp.database.DBHelper;
import ch.supsi.dti.isin.meteoapp.database.DBSchema;
import ch.supsi.dti.isin.meteoapp.model.Location;
import ch.supsi.dti.isin.meteoapp.model.LocationsHolder;
import ch.supsi.dti.isin.meteoapp.service.Fetcher;
import ch.supsi.dti.isin.meteoapp.utility.VolleyCallback;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

public class ListFragment extends DialogFragment implements VolleyCallback {

    private static final String ARG_LOCATION = "location";
    private static final String TAG = "SML";

    private RecyclerView mLocationRecyclerView;
    private LocationAdapter mAdapter;
    private SQLiteDatabase mDatabase;

    public static ListFragment newInstance(Location location) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_LOCATION, location);
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission not Granted");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            Log.i(TAG, "Permission Granted");
            mDatabase = new DBHelper(getContext()).getWritableDatabase();
            startLocationListener();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mLocationRecyclerView = view.findViewById(R.id.recycler_view);

        mLocationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Location> locations = LocationsHolder.get(getActivity()).getLocations();

        mAdapter = new LocationAdapter(locations);
        mLocationRecyclerView.setAdapter(mAdapter);

        return view;
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_list, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ContentValues values = new ContentValues();

        if (item.getItemId() == R.id.menu_add) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add New Location");
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                Location location = new Location();
                location.setName(input.getText().toString());
                LocationsHolder.get(getContext()).addLocation(location);
                mAdapter.notifyItemInserted(LocationsHolder.get(getContext()).getLocations().size());

                values.put(DBSchema.LocationsTable.Cols.NAME, input.getText().toString());
                mDatabase.insert(DBSchema.LocationsTable.TABLE_NAME, null, values);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == 0) {
            Location location = (Location) data.getSerializableExtra("return_location");
            LocationsHolder.get(getContext()).addLocation(location);

            List<Location> locations = LocationsHolder.get(getActivity()).getLocations();
            mAdapter = new LocationAdapter(locations);
            mLocationRecyclerView.setAdapter(mAdapter);
        }
    }

    // Holder

    private class LocationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mNameTextView;
        private Location mLocation;

        public LocationHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            itemView.setOnClickListener(this);
            mNameTextView = itemView.findViewById(R.id.name);
        }

        @Override
        public void onClick(View view) {
            Intent intent = DetailActivity.newIntent(getActivity(), mLocation.getId());
            startActivity(intent);
        }

        public void bind(Location location) {
            mLocation = location;
            mNameTextView.setText(mLocation.getName());
        }
    }

    // Adapter

    private class LocationAdapter extends RecyclerView.Adapter<LocationHolder> {
        private final List<Location> mLocations;

        public LocationAdapter(List<Location> locations) {
            mLocations = locations;
        }

        @NonNull
        @Override
        public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new LocationHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(LocationHolder holder, int position) {
            Location location = mLocations.get(position);
            holder.bind(location);
        }

        @Override
        public int getItemCount() {
            return mLocations.size();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void startLocationListener() {
        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH)
                .setDistance(0)
                .setInterval(5000);
        SmartLocation.with(getActivity()).location().continuous().config(builder.build())
                .start(location -> {
                    Log.i("Meteo onLocationUpdated", location.toString());
                    JsonObjectRequest jsonObjectRequest =
                            Fetcher.getLocationInfo(location.getLatitude(), location.getLongitude(),
                                    ListFragment.this);
                    RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
                    queue.add(jsonObjectRequest);
                    mAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onSuccess(Location location) {
        Log.i("Meteo onSuccess", location.toString());
        LocationsHolder.get(getActivity()).updateLocation(0, location);
        LocationsHolder.get(getActivity()).getLocations().get(0).setName(location.getName());
    }
}
