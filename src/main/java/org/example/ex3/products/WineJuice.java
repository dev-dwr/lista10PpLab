package org.example.ex3.products;

public class WineJuice {
    private int liters;

    public WineJuice(int liters) {
        this.liters = liters;
    }

    public int getLiters() {
        return liters;
    }

    public void setLiters(int liters) {
        this.liters = liters;
    }

    @Override
    public String toString() {
        return "WineJuice{" +
                "liters=" + liters +
                '}';
    }
}
