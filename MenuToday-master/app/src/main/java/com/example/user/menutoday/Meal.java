package com.example.user.menutoday;

import java.util.ArrayList;

/**
 * Created by user on 2016-09-17.
 */
public class Meal {
    String name;
    ArrayList<String> dishes;
    ArrayList<String> prices;

    public Meal(String name, ArrayList<String> dishes, ArrayList<String> prices) {
        this.name = name;
        this.dishes = dishes;
        this.prices = prices;
    }

}
