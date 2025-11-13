package com.myapp.model;

import java.util.List;

public class Route {
    private final double distanceKm;
    private final long durationSec;
    private final TransportMode mode;
    private final List<POI> pois;

    public Route(double distanceKm, long durationSec, TransportMode mode, List<POI> pois) {
        this.distanceKm = distanceKm;
        this.durationSec = durationSec;
        this.mode = mode;
        this.pois = pois;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public long getDurationSec() {
        return durationSec;
    }

    public TransportMode getMode() {
        return mode;
    }

    public List<POI> getPois() {
        return pois;
    }
}
