package com.mygy.fitnesshelper.data;

import java.io.Serializable;
import java.util.ArrayList;

public class SportActivity implements Serializable {
    private String name;
    private float ccalsPerHour;
    private static ArrayList<SportActivity> allActivities = new ArrayList<>();

    public SportActivity(String name, float ccalsPerHour) {
        this.name = name;
        this.ccalsPerHour = ccalsPerHour;
        allActivities.add(this);
    }


    public String getName() {
        return name;
    }

    public float getCcalsPerHour() {
        return ccalsPerHour;
    }

    public static ArrayList<SportActivity> getAllActivities() {
        return allActivities;
    }
    public static SportActivity getActivityByName(String name){
        for(SportActivity a:allActivities){
            if(a.name.equals(name)) return a;
        }
        return null;
    }
}
