package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class TopIssuesList extends WOComponent {
	private static final long serialVersionUID = 1L;

	public WODisplayGroup itemDisplayGroup;
	public boolean emailFlag = false;
	public Item anItem;
	public NSTimestamp today;
	public String title = "Top Issues: Hot Issues";
	public String queryDesc = "<b>Open Priority 1 Bugs </b><font size=2>(not Feature Requests)</font><br><b>Severity = 'blocker' or 'critical'</b>";

    public TopIssuesList(WOContext context) {	
        super(context);
		today = new NSTimestamp();
    }
		
	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = true;
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
	
	public String title() {
		return title;
	}
	public void setTitle(String pVal) {
		title = pVal;
	}
	public String queryDesc() {
		return queryDesc;
	}
	public void setQueryDesc(String pVal) {
		queryDesc = pVal;
	}
}