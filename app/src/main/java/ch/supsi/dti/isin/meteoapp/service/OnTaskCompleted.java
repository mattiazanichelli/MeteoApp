package ch.supsi.dti.isin.meteoapp.service;

import java.util.List;

import ch.supsi.dti.isin.meteoapp.model.Location;

public interface OnTaskCompleted {
    void onTaskCompleted(List<Location> locations);
}