package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class MilestoneDates extends EOGenericRecord {
	private static final long serialVersionUID = 1L;

	public MilestoneDates() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public MilestoneDates(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
        super(context, classDesc, gid);
    }

    // If you add instance variables to store property values you
    // should add empty implementions of the Serialization methods
    // to avoid unnecessary overhead (the properties will be
    // serialized for you in the superclass).
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    }
*/

    public String releaseName() {
        return (String)storedValueForKey("releaseName");
    }

    public void setReleaseName(String value) {
        takeStoredValueForKey(value, "releaseName");
    }

    public String milestone() {
        return (String)storedValueForKey("milestone");
    }

    public void setMilestone(String value) {
        takeStoredValueForKey(value, "milestone");
    }

    public NSTimestamp startDate() {
        return (NSTimestamp)storedValueForKey("startDate");
    }

    public void setstartDate(NSTimestamp value) {
        takeStoredValueForKey(value, "startDate");
    }
}
