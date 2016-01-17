package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.appserver.*;

public class ItemList extends WOComponent {
	private static final long serialVersionUID = 1L;
	public WODisplayGroup itemDisplayGroup;
	public boolean emailFlag = false;
	public boolean commentsFlag = false;
	public boolean releaseFlag = false;	
	public Item anItem;

	public ItemList(WOContext context) {
		super(context);
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
	
	public boolean emailFlag() {return emailFlag;}
	public void setEmailFlag(boolean pVal) {emailFlag = true;}
	public boolean commentsFlag() {return commentsFlag;}
	public void setCommentsFlag(boolean pVal) {commentsFlag = pVal;}
	public boolean releaseFlag() {return releaseFlag;}
	public void setReleaseFlag(boolean pVal) {releaseFlag = pVal;}
	
}