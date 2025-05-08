package com.example.sanitariuszapp;

public class AfternoonProc {

    // ***
    // Room number or location identifier
    // ***
    private String room;

    // ***
    // Patient's full name
    // ***
    private String name;

    // ***
    // Details of procedures with timestamps
    // ***
    private String procedures;

    // ***
    // Optional note for the afternoon procedure
    // ***
    private String note;

    // ***
    // Constructor to initialize all fields
    // ***
    public AfternoonProc(String room, String name, String procedures, String note) {
        this.room = room;
        this.name = name;
        this.procedures = procedures;
        this.note = note;
    }

    // ***
    // Getter for room
    // ***
    public String getRoom() { return room; }

    // ***
    // Getter for patient name
    // ***
    public String getName() { return name; }

    // ***
    // Getter for procedures description
    // ***
    public String getProcedures() { return procedures; }

    // ***
    // Getter for note
    // ***
    public String getNote() { return note; }
}