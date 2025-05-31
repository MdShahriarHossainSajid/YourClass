package com.example.yourclass;

public class RoutineItem {
    private String subject;
    private String day;
    private String time;
    private String room;

    public RoutineItem() {
        // Default constructor needed for Firestore or Firebase
    }

    public RoutineItem(String subject, String day, String time, String room) {
        this.subject = subject;
        this.day = day;
        this.time = time;
        this.room = room;
    }

    public String getSubject() {
        return subject;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public String getRoom() {
        return room;
    }
}
