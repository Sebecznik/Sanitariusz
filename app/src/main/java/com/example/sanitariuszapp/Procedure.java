package com.example.sanitariuszapp;

/***
 * Represents a medical procedure with its details and status.
 * Tracks procedure time, duration, and progress state.
 */
public class Procedure {
    private String text;
    private String time;
    private int status;

    /***
     * Status constants for procedure states:
     * DEFAULT - Normal unmarked procedure
     * MARKED - Important/highlighted procedure
     * FINISHED - Completed procedure
     * IN_PROGRESS - Currently active procedure with timer
     */
    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_MARKED = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_IN_PROGRESS = 3;

    // Time tracking fields
    private long durationMillis = 0;
    private long remainingMillis = 0; // Currently unused but kept for potential future use
    private long startTimestamp = 0;  // Timestamp when procedure was started

    /***
     * Creates a new procedure with default status and empty time
     * @param text Description of the procedure
     */
    public Procedure(String text) {
        this.text = text;
        this.time = "";
        this.status = STATUS_DEFAULT;
    }

    /***
     * Creates a new procedure with specified time and default status
     * @param text Description of the procedure
     * @param time Scheduled time for the procedure
     */
    public Procedure(String text, String time) {
        this.text = text;
        this.time = time;
        this.status = STATUS_DEFAULT;
    }

    // Standard getters and setters with brief documentation

    /*** @return Procedure description text */
    public String getText() {
        return text;
    }

    /*** @param text New procedure description */
    public void setText(String text) {
        this.text = text;
    }

    /*** @return Scheduled time for the procedure */
    public String getTime() {
        return time;
    }

    /*** @param time New scheduled time */
    public void setTime(String time) {
        this.time = time;
    }

    /*** @return Current status of the procedure */
    public int getStatus() {
        return status;
    }

    /*** @param status New status to set */
    public void setStatus(int status) {
        this.status = status;
    }

    /*** @return Total duration in milliseconds */
    public long getDurationMillis() {
        return durationMillis;
    }

    /*** @param durationMillis New duration in milliseconds */
    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    /*** @return Remaining time in milliseconds */
    public long getRemainingMillis() {
        return remainingMillis;
    }

    /*** @param remainingMillis New remaining time in milliseconds */
    public void setRemainingMillis(long remainingMillis) {
        this.remainingMillis = remainingMillis;
    }

    /*** @return Timestamp when procedure was started */
    public long getStartTimestamp() {
        return startTimestamp;
    }

    /*** @param startTimestamp New start timestamp */
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    /***
     * Returns formatted string representation of the procedure
     * @return String in format "ProcedureText (HH:MM)" or "ProcedureText (00:00)" if time not set
     */
    @Override
    public String toString() {
        if (time == null || time.isEmpty()) {
            return text + " (00:00)";
        }
        return text + " (" + time + ")";
    }
}