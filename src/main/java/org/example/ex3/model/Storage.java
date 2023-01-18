package org.example.ex3.model;

import org.example.ex3.products.Bottels;
import org.example.ex3.products.Grapes;
import org.example.ex3.products.Suger;
import org.example.ex3.products.Water;

public class Storage {
    private Water waterLiters;
    private Grapes grapesKg;
    private Suger sugarKg;
    private Bottels bottles;

    public Storage(Water waterLiters, Grapes grapesKg, Suger sugarKg, Bottels bottles) {
        this.waterLiters = waterLiters;
        this.grapesKg = grapesKg;
        this.sugarKg = sugarKg;
        this.bottles = bottles;
    }

    public Water getWaterLiters() {
        return waterLiters;
    }

    public void setWaterLiters(Water waterLiters) {
        this.waterLiters = waterLiters;
    }

    public Grapes getGrapesKg() {
        return grapesKg;
    }

    public void setGrapesKg(Grapes grapesKg) {
        this.grapesKg = grapesKg;
    }

    public Suger getSugarKg() {
        return sugarKg;
    }

    public void setSugarKg(Suger sugarKg) {
        this.sugarKg = sugarKg;
    }

    public Bottels getBottles() {
        return bottles;
    }

    public void setBottles(Bottels bottles) {
        this.bottles = bottles;
    }
}
