package com.ironkey.tracker.components;

import com.webobjects.appserver.*;

public class HomePage extends WOComponent {
	private static final long serialVersionUID = 1L;
    protected boolean isMyBugs = false;
    protected boolean isProject = true;
    protected boolean isTriage = false;
    protected boolean isRelease = false;
    protected boolean isUsability = false;
    public WOComponent selectedComponent;

    public HomePage(WOContext aContext) {
        super(aContext);
    }
        
    public boolean isMyBugs() {
        return isMyBugs;
    }
    
    public void setIsMyBugs(boolean newIsMyBugs) {
        if(newIsMyBugs == true) {
            setIsTriage(false);
            setIsProject(false);
            setIsRelease(false);
            setIsUsability(false);
        }

        isMyBugs = newIsMyBugs;
    }

    public boolean isProject() {
        return isProject;
    }
    
    public void setIsProject(boolean newIsProject) {
        if(newIsProject == true) {
            setIsMyBugs(false);
            setIsTriage(false);
            setIsRelease(false);
            setIsUsability(false);
        }

        isProject = newIsProject;
    }

    public boolean isTriage() {
        return isTriage;
    }
    
    public void setIsTriage(boolean newIsTriage) {
        if(newIsTriage == true) {
            setIsMyBugs(false);
            setIsProject(false);
            setIsRelease(false);
            setIsUsability(false);
        }
        isTriage = newIsTriage;
    }

    public boolean isRelease() {
        return isRelease;
    }
    
    public void setIsRelease(boolean newVal) {
        if(newVal == true) {
            setIsTriage(false);
            setIsProject(false);
            setIsMyBugs(false);
            setIsUsability(false);
        }
        isRelease = newVal;
    }
    public boolean isUsability() {
        return isUsability;
    }
    
    public void setIsUsability(boolean newIsUsability) {
        if(newIsUsability == true) {
            setIsTriage(false);
            setIsProject(false);
            setIsMyBugs(false);
            setIsRelease(false);
        }
        isUsability = newIsUsability;
    }

    public WOComponent goMyBugs() {
        setIsMyBugs(true);
        session().takeValueForKey("MyBugs", "currentPageName");
        return null;
    }

    public WOComponent goProject() {
        setIsProject(true);
        session().takeValueForKey("Project", "currentPageName");
        return null;
    }

    public WOComponent goTriage() {
        setIsTriage(true);
        session().takeValueForKey("Triage", "currentPageName");
        return null;
    }

    public WOComponent goSupport() {
        setIsRelease(true);
        session().takeValueForKey("Support", "currentPageName");
        return null;
    }
    public WOComponent goReleasePlan() {
        setIsRelease(true);
        session().takeValueForKey("ReleasePlan", "currentPageName");
        return null;
    }
    
    public WOComponent goUsability() {
        setIsUsability(true);
        session().takeValueForKey("Usability", "currentPageName");
        return null;
    }
    
    public WOComponent self() {
        return this;
    }
    
    public WOComponent selectedComponent() {
        String componentName = "Release";
        if(isMyBugs() == true) {
            componentName = "MyBugs";
        }
        else if(isProject() == true) {
            componentName = "Project";
        }
        else if(isTriage() == true) {
            componentName = "Triage";
        }
        else if(isRelease() == true) {
            componentName = "Release";
        }
        else if(isUsability() == true) {
            componentName = "Usability";
        }
        selectedComponent = (WOComponent)pageWithName(componentName);

        return selectedComponent;
    }
    
    public WOComponent goNoWhere() {
        return null;
    }

}