package com.example.yourclass;

public class AttendanceModel {
    String courseCode;
    int present;
    int total;
    float percentage;

    public AttendanceModel(String courseCode, int present, int total, float percentage) {
        this.courseCode = courseCode;
        this.present = present;
        this.total = total;
        this.percentage = percentage;
    }

    public String getCourseCode() { return courseCode; }
    public int getPresent() { return present; }
    public int getTotal() { return total; }
    public float getPercentage() { return percentage; }
}
