package gitlet;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;


public class Commit implements Serializable {
    /** message. */
    private String _message;
    /** timestamp. */
    private String _timestamp;
    /** tracked Files. */
    private HashMap<String, Blob> _trackedFiles;
    /** id. */
    private String _id;
    /** date. */
    private String _date;
    /** parents. */
    private ArrayList<String> _parents;


    public Commit(String message, ArrayList<String> parents,
                  HashMap<String, Blob> trackedFiles) {
        _message = message;
        _parents = parents;
        _trackedFiles = trackedFiles;
        _id = generateID();
        _date = generateTimestamp(ZonedDateTime.now());
    }
    public Commit() {
        _message = "initial commit";
        _trackedFiles = new HashMap<>();
        _date = "Thu Jan 1 00:00:00 1970 -0500";
        _id = generateID();
        _parents = null;
    }

    public String generateID() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String makeLog() {
        String temp = "===" + "\n" + "commit "
                + _id + "\n" + "Date: "
                + _date + "\n" + _message + "\n";
        return temp;
    }

    public String generateTimestamp(ZonedDateTime now) {
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern("E LLL d HH:mm:ss yyyy Z");
        return now.format(formatter);
    }

    public HashMap<String, Blob> getTrackedFiles() {
        return _trackedFiles;
    }

    public ArrayList<String> getParents() {
        return _parents;
    }

    public String getID() {
        return _id;
    }

    public String getMessage() {
        return _message;
    }
}
