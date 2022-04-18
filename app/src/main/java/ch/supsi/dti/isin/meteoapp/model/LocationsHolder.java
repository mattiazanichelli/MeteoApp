package ch.supsi.dti.isin.meteoapp.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.supsi.dti.isin.meteoapp.database.DBHelper;
import ch.supsi.dti.isin.meteoapp.database.DBSchema;
import ch.supsi.dti.isin.meteoapp.database.LocationCursorWrapper;

public class LocationsHolder {

    private static LocationsHolder sLocationsHolder;
    private final List<Location> mLocations = new ArrayList<>();
    private final SQLiteDatabase mDatabase;

    public static LocationsHolder get(Context context) {
        if (sLocationsHolder == null)
            sLocationsHolder = new LocationsHolder(context);

        return sLocationsHolder;
    }

    private LocationsHolder(Context context) {
        Location location = new Location();
        location.setName("Current Location");
        mLocations.add(location);

        mDatabase = new DBHelper(context).getWritableDatabase();
        readData();
        mDatabase.close();
    }

    public List<Location> getLocations() {
        return mLocations;
    }

    public Location getLocation(UUID id) {
        for (Location location : mLocations) {
            if (location.getId().equals(id)) {
                return location;
            }
        }
        return null;
    }

    public void addLocation(Location location) {
        mLocations.add(location);

    }

    public void updateLocation(int pos, Location newLocation) {
        mLocations.get(pos).setName(newLocation.getName());
        mLocations.get(pos).setDescription(newLocation.getDescription());
        mLocations.get(pos).setHumidity(newLocation.getHumidity());
        mLocations.get(pos).setLatitude(newLocation.getLatitude());
        mLocations.get(pos).setLongitude(newLocation.getLongitude());
        mLocations.get(pos).setTemperature(newLocation.getTemperature());
        mLocations.get(pos).setWeatherId(newLocation.getWeatherId());
    }

    private void readData() {
        Cursor cursor = mDatabase.query(DBSchema.LocationsTable.TABLE_NAME,
                null, null, null, null, null, null);
        LocationCursorWrapper cursorWrapper = new LocationCursorWrapper(cursor);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                Location entry = cursorWrapper.getEntry();
                mLocations.add(entry);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }
}
