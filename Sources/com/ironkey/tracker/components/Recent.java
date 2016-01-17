package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class Recent extends WOComponent {
	private static final long serialVersionUID = 1L;	
    public WODisplayGroup displayGroup;
    protected EODatabaseDataSource _queryDataSource;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    public String aRow = "Version";
    public String aColumn = "Priority";
    public String selectedProject = "All";
    public NSArray<String> selectedStatus;
    public String aStatus;
    public boolean needUpdate = false;
    public boolean isList = false;
    public boolean isReport = true;
    public int numDays = 7;
    public String aProject;

    public Recent(WOContext aContext) {
        super(aContext);

        Session mysession = (Session)session();
        _queryDataSource =new EODatabaseDataSource(mysession.defaultEditingContext(), "Item");
        //selectedProject = mysession.selectedProject;
		
        String initalStatus[] = {"NEW","ASSIGNED","REOPENED"};
        selectedStatus = new NSArray<String>(initalStatus);

		performSearch();
		needUpdate = true;

		// sort order
		EOSortOrdering orderings[]={
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("product.productName", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		displayGroup.setSortOrderings(new NSArray<EOSortOrdering>(orderings));
		displayGroup.setNumberOfObjectsPerBatch(25);
		displayGroup.updateDisplayedObjects();
    }

    public NSArray<String> potentialProjects() {
        Session s = (Session)session();
        NSMutableArray<String> temp  = new NSMutableArray<String>(s.potentialVersions());
        temp.insertObjectAtIndex("All", 0);
        return(NSArray<String>)temp;
    }

    public void performSearch() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        Session s = (Session)session();
		
		NSMutableArray<NSTimestamp> args = new NSMutableArray<NSTimestamp>();		
		args.addObject(startDate());
		// Number of days ago to find bugs that were created.
       qual.addObject(EOQualifier.qualifierWithQualifierFormat("creationTs > %@", args));

        // Set the selected 'Version'
        if(!selectedProject.equals("All")) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject + "' ", null));
        }

        // Set the selected 'Type' (product)
        if(!s.selectedItemType.equals("All")) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("product='"+ s.selectedItemType + "' ", null));
        }
/*
        // Status ('NEW', 'ASSIGNED', 'REOPENED', 'RESOLVED','VERIFIED', 'CLOSED')
        int numStatus = selectedStatus.count();
        if(numStatus < 6) {
            NSMutableArray temp = new NSMutableArray();
            Enumeration enumer = selectedStatus.objectEnumerator();
            while(enumer.hasMoreElements()) {
                String theStatus = (String)enumer.nextElement();
                temp.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus='" + theStatus + "'", null));
            }
            qual.addObject(new EOOrQualifier(temp));
        }
		*/

        fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
		fs.setRefreshesRefetchedObjects(true);

        _queryDataSource.setFetchSpecification(fs);
        displayGroup = new WODisplayGroup();
        displayGroup.setDataSource(_queryDataSource);

        displayGroup.fetch();
    }
	
	public NSTimestamp startDate() {
        NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);

        NSTimestamp currDate = new NSTimestamp();
        NSTimestamp currDatePacific = new NSTimestamp(currDate.getTime(), tz);
        NSTimestamp daysAgoDate =  currDatePacific.timestampByAddingGregorianUnits (0, 0, (numDays*-1), 0, 0, 0);
        NSTimestamp daysAgoMidnight =  daysAgoDate.timestampByAddingGregorianUnits (0, 0, 0, (daysAgoDate.hourOfDay()*-1), (daysAgoDate.minuteOfHour()*-1), (daysAgoDate.secondOfMinute()*-1));
		//System.out.println("daysAgoMidnight - " + daysAgoMidnight);

		return daysAgoMidnight;
    }

    public void appendToResponse(WOResponse r, WOContext c) {

        if (_hasToUpdate) {
            //displayGroup.fetch();
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

}