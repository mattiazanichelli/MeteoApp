package ch.supsi.dti.isin.meteoapp.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import ch.supsi.dti.isin.meteoapp.model.Location;

public class LocationCursorWrapper extends CursorWrapper {

    public LocationCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Location getEntry() {
        String name = getString(getColumnIndex(DBSchema.LocationsTable.Cols.NAME));
        return new Location(name);
    }
}
