// Created by Direct to Web's Project Builder Wizard
package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class MenuHeader extends WOComponent {
	private static final long serialVersionUID = 1L;
    public String entityNameInList;
    private String _manipulatedEntityName;

    public MenuHeader(WOContext aContext) {
        super(aContext);
    }

    public String manipulatedEntityName() {
        if (_manipulatedEntityName == null) {
            WOComponent currentPage = context().page();
            _manipulatedEntityName = D2W.factory().entityNameFromPage(currentPage);
        }
        return _manipulatedEntityName;
    }

    public void setManipulatedEntityName(String newValue) {
        _manipulatedEntityName = newValue;
    }

    public NSArray visibleEntityNames() {
        return D2W.factory().visibleEntityNames(session());
    }

    public WOComponent findEntityAction() {
        QueryPageInterface newQueryPage = D2W.factory().queryPageForEntityNamed(_manipulatedEntityName, session());
        return (WOComponent) newQueryPage;
    }

    public WOComponent newObjectAction() {
        WOComponent nextPage = null;
        try {
			if(_manipulatedEntityName.equals("Item")) {
			nextPage=pageWithName("WORedirect");
			((WORedirect)nextPage).setURL(((Application)Application.application()).bugzillaHostUrl() + "/enter_bug.cgi");
			}
			else {
				EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(_manipulatedEntityName, session());
				epi.setNextPage(context().page());
				nextPage = (WOComponent) epi;
			}
        } catch (IllegalArgumentException e) {
            ErrorPageInterface epf = D2W.factory().errorPage(session());
            epf.setMessage(e.toString());
            epf.setNextPage(context().page());
            nextPage = (WOComponent) epf;
        }
        return nextPage;
    }

    public WOComponent logout() {
        WOComponent redirectPage = pageWithName("WORedirect");
        ((WORedirect) redirectPage).setUrl(D2W.factory().homeHrefInContext(context()));
        session().terminate();
        return redirectPage;
    }

    public WOComponent homeAction() {
	    WOComponent homepage = pageWithName("HomePage");

        //return D2W.factory().defaultPage(session());
		return homepage;
    }

    public WOComponent showWebAssistant() {
        return D2W.factory().webAssistantInContext(context());
    }

    public boolean isWebAssistantEnabled () {
        return D2W.factory().isWebAssistantEnabled();
    }
}