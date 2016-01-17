// Created by Direct to Web's Project Builder Wizard
package com.ironkey.tracker.components;

import com.webobjects.appserver.*;
import com.ironkey.tracker.*;

public class PageWrapper extends WOComponent {
	private static final long serialVersionUID = 1L;
    protected boolean isWrapped = true;
    protected String bugNumber;

    public PageWrapper(WOContext aContext) {
        super(aContext);
    }
	
	public WOComponent goFindBug() {
        WOComponent redirectPage=pageWithName("WORedirect");        
		((WORedirect)redirectPage).setURL(((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id="+bugNumber());
        return redirectPage;
    }

    public String bugNumber() {
        return bugNumber;
    }

    public void setBugNumber(String newBugNumber) {
        bugNumber = newBugNumber;
    }
    public boolean isWrapped() {
        return isWrapped;
    }

    public void setIsWrapped(boolean newIsWrapped) {
        isWrapped = newIsWrapped;
    }


}