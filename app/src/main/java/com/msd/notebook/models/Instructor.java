package com.msd.notebook.models;

public class Instructor {

    String id, instructorName;

    public Instructor(String id, String instructorName) {
        this.id = id;
        this.instructorName = instructorName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
}
