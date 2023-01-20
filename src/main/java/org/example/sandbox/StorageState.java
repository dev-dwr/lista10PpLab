package org.example.sandbox;

public class StorageState {
    private int waterLiters;
    private int grapesKg;
    private int sugarKg;
    private int bottles;


    public StorageState(int waterLiters, int grapesKg, int sugarKg, int bottles) {
        this.waterLiters = waterLiters;
        this.grapesKg = grapesKg;
        this.sugarKg = sugarKg;
        this.bottles = bottles;
    }

    public int getWaterLiters() {
        return waterLiters;
    }

    public int getGrapesKg() {
        return grapesKg;
    }

    public int getSugarKg() {
        return sugarKg;
    }

    public int getBottles() {
        return bottles;
    }

    @Override
    public String toString() {
        return "StorageState{" +
                "waterLiters=" + waterLiters +
                ", grapesKg=" + grapesKg +
                ", sugarKg=" + sugarKg +
                ", bottles=" + bottles +
                '}';
    }
}
