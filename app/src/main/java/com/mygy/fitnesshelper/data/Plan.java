package com.mygy.fitnesshelper.data;

import com.google.firebase.Timestamp;
import com.mygy.fitnesshelper.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class Plan implements Serializable {
    private String name;
    private String description;
    private Date time;
    private boolean done;
    private int weight;
    private static final Comparator<Plan> planComparator;
    private static OnPlanDoneChanged onPlanDoneChanged;

    static {
        planComparator = (o1, o2) -> {
            int c = o1.getWeight() - o2.getWeight();
            if (c == 0)
                return (o1.getTime().before(o2.getTime())) ? -1 : 1;
            return c;
        };
    }

    public Plan(String name, String description, Date time) {
        this.name = name;
        this.description = description;
        this.time = time;
        this.done = false;
        weight = 0;
    }

    public Plan(HashMap<String, Object> doc) {
        name = (String) doc.get("name");
        description = (String) doc.get("description");
        try {
            time = ((Timestamp) doc.get("time")).toDate();
        } catch (NullPointerException ex) {
            time = new Date();
        }
        try {
            done = (Boolean) doc.get("done");
        } catch (NullPointerException ex) {
            done = false;
        }
        try {
            weight = ((Long) doc.get("weight")).intValue();
        } catch (NullPointerException ex) {
            weight = 0;
        }
    }

    public static void setOnPlanDataChanged(OnPlanDoneChanged onPlanDoneChanged) {
        Plan.onPlanDoneChanged = onPlanDoneChanged;
    }

    public void setDone(boolean done) {
        this.done = done;
        weight = done ? 1 : 0;

        onPlanDoneChanged.processChanges(this);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getTime() {
        return time;
    }

    public boolean isDone() {
        return done;
    }

    public int getWeight() {
        return weight;
    }

    public static void sortPlansList(ArrayList<Plan> plans) {
        plans.sort(planComparator);
    }
    public static interface OnPlanDoneChanged{
        void processChanges(Plan planChanged);
    }
}
