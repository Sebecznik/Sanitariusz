package com.example.sanitariuszapp;

import java.util.List;

public class Patient {
    // ***
    // Patient properties
    // ***
    private int id;
    private String roomNumber;
    private String name;
    private List<Procedure> procedures;
    private String note;

    // ***
    // Constructors
    // ***
    public Patient(String roomNumber, String name, List<Procedure> procedures, String note) {
        this.roomNumber = roomNumber;
        this.name = name;
        this.procedures = procedures;
        this.note = note;
    }

    // ***
    // Patient properties
    // ***
    public Patient(int id, String roomNumber, String name, List<Procedure> procedures, String note) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.name = name;
        this.procedures = procedures;
        this.note = note;
    }

    // ***
    // Getters and setter
    // ***
    public int getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public String getName() { return name; }
    public List<Procedure> getProcedures() { return procedures; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}