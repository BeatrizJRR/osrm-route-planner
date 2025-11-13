package com.myapp.model;

public class POI {
    private final String name;
    private final String category;
    private final Point coordinate;

    public POI(String name, String category, Point coordinate) {
        this.name = name; this.category = category; this.coordinate = coordinate;
    }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Point getCoordinate() { return coordinate; }
}
