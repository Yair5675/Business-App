package com.example.finalproject.database.online.collections;

import java.util.Date;

public class Shift {
    // The shift's ID:
    private String shiftId;

    // The date of the shift:
    private Date date;

    // The starting and ending time (in minutes):
    private int startingTime, endingTime;

    public Shift() {
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(int startingTime) {
        this.startingTime = startingTime;
    }

    public int getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(int endingTime) {
        this.endingTime = endingTime;
    }
}
