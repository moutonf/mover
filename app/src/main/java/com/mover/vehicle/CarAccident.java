package com.mover.vehicle;

/**
 * Created by LUKE on 2016/08/14.
 */
public class CarAccident {

    private double acceleration;
    private double lat;
    private double lng;

    public CarAccident(double acc, double lat, double lng) {
        this.acceleration = acc;
        this.lat = lat;
        this.lng = lng;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
