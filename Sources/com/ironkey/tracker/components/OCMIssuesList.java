package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class OCMIssuesList extends WOComponent {
	private static final long serialVersionUID = 1L;
	public WODisplayGroup itemDisplayGroup;
	public boolean emailFlag = false;
	public Item anItem;
	public NSTimestamp today;

    public OCMIssuesList(WOContext context) {	
        super(context);
		today = new NSTimestamp();

    }

		
	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = pVal;
	}

	public String bugURL() {
		return ((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id=" +anItem.valueForKey("bugId");
	 }
	 
	public void displayAll() {
		if(itemDisplayGroup != null) {
			itemDisplayGroup.setNumberOfObjectsPerBatch(itemDisplayGroup.allObjects().count());
			itemDisplayGroup.updateDisplayedObjects();
		}
	}
	
	public boolean isItems() {
		return (itemDisplayGroup.allObjects().count()>0)?true:false;
	}
}