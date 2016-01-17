package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.appserver.*;

public class EscalationEmail extends WOComponent {
	private static final long serialVersionUID = 1L;
    public EscalationEmail(WOContext context) {
        super(context);
    }
    public String summary;
    public String severity;
    public String priority;
    public String comment;
    public String aDescription;
    public String bugNumber;
    public String bugType;
    public boolean isHot;

    public String bugURL() {
		return ((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id=" + bugNumber;
    }
}
