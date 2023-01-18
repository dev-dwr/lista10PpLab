package org.example.ex3.products;

public class Wine {
    private int liters;
    private boolean filtered;

    public Wine(int liters, boolean filtered) {
        this.liters = liters;
        this.filtered = filtered;
    }

    public int getLiters() {
        return liters;
    }

    public void setLiters(int liters) {
        this.liters = liters;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
}
