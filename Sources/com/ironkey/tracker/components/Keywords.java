package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

public class Keywords extends WOComponent {
	private static final long serialVersionUID = 1L;	
    public WODisplayGroup displayGroup;
    protected EODatabaseDataSource _queryDataSource;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    public String aRow = "Component";
    public String aColumn = "Priority";
    public String selectedProject = "All";
    public NSArray<String> selectedStatus;
    public String aStatus;
    public boolean needUpdate = false;
    public String selectedKeyword = "beta";
    public String aKeyword;
	public boolean commentsFlag = true;
	public boolean releaseFlag = false;
	public boolean isList = true;
	public boolean isReport = false;

    public Keywords(WOContext aContext) {
        super(aContext);
		String allStatus[] = {"NEW","ASSIGNED","REOPENED", "RESOLVED","VERIFIED", "CLOSED"};
		String openStatus[] = {"NEW","ASSIGNED","REOPENED"};
		
        Session mysession = (Session)session();
        _queryDataSource =new EODatabaseDataSource(mysession.defaultEditingContext(), "Item");
        //selectedProject = mysession.selectedProject;
		
		getDefaultValuesFromCookie();
		
		if(selectedKeyword.equals("ops")) {
			selectedStatus = new NSArray<String>(openStatus);
		}
		else {
			selectedStatus = new NSArray<String>(allStatus);
		}

		performSearch();
		needUpdate = true;

    }

    public NSArray<String> potentialProjects() {
        Session s = (Session)session();
        NSMutableArray<String> temp  = new NSMutableArray<String>(s.potentialProjects());
        temp.insertObjectAtIndex("All", 0);
        return(NSArray<String>)temp;
    }

    public void performSearch() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        Session s = (Session)session();

        // Set the keyword
        //qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like 'beta'", null));
        //qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like '*" + selectedKeyword +"*'", null));
        qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = '" + selectedKeyword +"'", null));

        // Set the selected 'Version'
        if(!selectedProject.equals("All")) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject + "' ", null));
            releaseFlag = false;
        }
        else {
        	releaseFlag = true;
        }

        // Set the selected 'Type' (product)
        if(!s.selectedItemType.equals("All")) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("product='"+ s.selectedItemType + "' ", null));
        }

        // Status ('NEW', 'ASSIGNED', 'REOPENED', 'RESOLVED','VERIFIED', 'CLOSED')
        int numStatus = selectedStatus.count();
        if(numStatus < 6) {
            NSMutableArray<EOQualifier> temp = new NSMutableArray<EOQualifier>();
            Enumeration<String> enumer = selectedStatus.objectEnumerator();
            while(enumer.hasMoreElements()) {
                String theStatus = (String)enumer.nextElement();
                temp.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus='" + theStatus + "'", null));
            }
            qual.addObject(new EOOrQualifier(temp));
        }

        fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
		fs.setRefreshesRefetchedObjects(true);

        _queryDataSource.setFetchSpecification(fs);
        displayGroup = new WODisplayGroup();
        displayGroup.setDataSource(_queryDataSource);
		// sort order
        EOSortOrdering orderings[]={
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareAscending),
			EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareAscending),
			EOSortOrdering.sortOrderingWithKey("product.productName", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		displayGroup.setSortOrderings(new NSArray<EOSortOrdering>(orderings));
		displayGroup.setNumberOfObjectsPerBatch(25);
		displayGroup.updateDisplayedObjects();

        displayGroup.fetch();
    }
	
    public void appendToResponse(WOResponse r, WOContext c) {

        if (_hasToUpdate) {
            refreshData();
            _hasToUpdate=false;
        }
        super.appendToResponse(r,c);
    }
	
	
	public boolean isUser() {
		boolean returnVal = false;
	    Session session = (Session)session();
		
		if(session.getUser() != null) {
			returnVal = true;
		}
		return returnVal;
	}
	
	    public void refreshData() {
        displayGroup.fetch();
        //totalActive = displayGroup.allObjects().count();
    }
	public void setNeedUpdate(boolean pVal) {
	 // do nothing
	}
	

	public String notLoggedIn() {
		return "You are not currently logged in, please login.";
	}

    public WOComponent resetDisplayedBugs() {
        if(!selectedProject.equals("All")) {
            session().takeValueForKey(selectedProject, "selectedProject");
        }

        performSearch();
		needUpdate = true;
		
        return null;
    }
	
	
	
	public void getDefaultValuesFromCookie() {
        WOContext context = context();
        WORequest request = context.request();
		String default_keyword = null;
		String default_keyword_project = null;

		default_keyword = request.cookieValueForKey("default_keyword");
		default_keyword_project = request.cookieValueForKey("default_keyword_project");
		if(default_keyword != null) {
			selectedKeyword = default_keyword;
        }
		if(default_keyword_project != null) {
			selectedProject = default_keyword_project;
        }
    }
	public void setDefaultCookie(String pKeyword, String pCookieName) {
        NSTimestamp now = new NSTimestamp();
        NSTimestamp yearFromNow = now.timestampByAddingGregorianUnits(1,0,0,0,0,0);
        WOContext context = context();
        WOResponse response = context.response();
        WORequest request = context.request();

        //WOCookie cookie = WOCookie.cookieWithName("default_keyword", pKeyword);
        WOCookie cookie = new WOCookie(pCookieName, pKeyword);
        cookie.setExpires(yearFromNow);
        cookie.setPath(request.adaptorPrefix() + "/");
        //cookie.setPath(request.adaptorPrefix() + "/" + request.applicationName());
        response.addCookie(cookie);
    }
	
	public String selectedKeyword() {
		return selectedKeyword;
	}
	public void setSelectedKeyword(String pKeyword) {
		if((pKeyword != null) && (!pKeyword.equals(selectedKeyword))) {
			setDefaultCookie(pKeyword, "default_keyword");
        }
		selectedKeyword = pKeyword;
	}
	
	public String selectedProject() {
		return selectedProject;
	}
	public void setSelectedProject(String pProject) {
		if((pProject != null) && (!pProject.equals(selectedProject))) {
			setDefaultCookie(pProject, "default_keyword_project");
        }
		selectedProject = pProject;
	}


}