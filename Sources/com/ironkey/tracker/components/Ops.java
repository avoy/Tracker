package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

public class Ops extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup displayGroup;
    public WODisplayGroup displayGroupResolved;
    public WODisplayGroup displayGroupClosed;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    public String selectedProject = "All";
    public NSArray selectedStatus;
    public String aStatus;
    public boolean emailFlag = false;
    public boolean onlyOpen = false;
    public boolean needUpdate = false;
    public boolean isList = true;
    public boolean isReport = false;
    public NSTimestamp currDate;
    public NSTimestamp now;
	
    public Ops(WOContext aContext) {
        super(aContext);
		now = new NSTimestamp();
		
		performSearch();		
    }
	
	public void performSearch() {
		// 3 sections to the report
		//    Open, Resolved, Closed (in the last week)
		searchForOpenItems();
		searchForResolvedItems();
		searchForClosedInLastWeekItems();
		needUpdate = true;
	}
	
	
    public void searchForOpenItems() {
        EOFetchSpecification fs;
        NSDictionary bindings;
        NSMutableArray qual = new NSMutableArray();
        Session s = (Session)session();
        EODatabaseDataSource _queryDataSource;
        _queryDataSource =new EODatabaseDataSource(s.defaultEditingContext(), "Item");
        // Set the selected 'Version'
        if(!selectedProject.equals("All")) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject + "' ", null));
        }

        // Set the selected 'Type' (product)
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='Ops Change Management (OCM)'", null));

        // Status ('NEW', 'ASSIGNED', 'REOPENED', 'RESOLVED','VERIFIED', 'CLOSED')
		Object initalStatus[] = {"NEW","ASSIGNED","REOPENED"};
        selectedStatus = new NSArray(initalStatus);

		NSMutableArray temp = new NSMutableArray();
		Enumeration enumer = selectedStatus.objectEnumerator();
		while(enumer.hasMoreElements()) {
			String theStatus = (String)enumer.nextElement();
			temp.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus='" + theStatus + "'", null));
		}
		qual.addObject(new EOOrQualifier(temp));

        fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
		fs.setRefreshesRefetchedObjects(true);

        _queryDataSource.setFetchSpecification(fs);
        displayGroup = new WODisplayGroup();
        displayGroup.setDataSource(_queryDataSource);
		// sort order
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("product.productName", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		displayGroup.setSortOrderings(new NSArray(orderings));
		displayGroup.setNumberOfObjectsPerBatch(displayGroup.allObjects().count());
        displayGroup.fetch();
    }
	
	public int numOpen() {
		return displayGroup.allObjects().count();
	}


    public void searchForResolvedItems() {
        EOFetchSpecification fs;
        NSDictionary bindings;
        NSMutableArray qual = new NSMutableArray();
        Session s = (Session)session();
		EODatabaseDataSource _queryDataSourceResolved;

        _queryDataSourceResolved =new EODatabaseDataSource(s.defaultEditingContext(), "Item");

        // Set the selected 'Version'
        if(!selectedProject.equals("All")) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject + "' ", null));
        }

        // Set the selected 'Type' (product)
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='Ops Change Management (OCM)'", null));

        // Status ('NEW', 'ASSIGNED', 'REOPENED', 'RESOLVED','VERIFIED', 'CLOSED')
		Object initalStatus[] = {"RESOLVED","VERIFIED"};
        selectedStatus = new NSArray(initalStatus);
		
		NSMutableArray temp = new NSMutableArray();
		Enumeration enumer = selectedStatus.objectEnumerator();
		while(enumer.hasMoreElements()) {
			String theStatus = (String)enumer.nextElement();
			temp.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus='" + theStatus + "'", null));
		}
		qual.addObject(new EOOrQualifier(temp));

        fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
		fs.setRefreshesRefetchedObjects(true);
        _queryDataSourceResolved.setFetchSpecification(fs);

		// sort order
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("product.productName", EOSortOrdering.CompareCaseInsensitiveAscending),
		};
        displayGroupResolved.setDataSource(_queryDataSourceResolved);
		displayGroupResolved.setSortOrderings(new NSArray(orderings));
		displayGroupResolved.setNumberOfObjectsPerBatch(displayGroupResolved.allObjects().count());
        displayGroupResolved.fetch();
    }


    public void searchForClosedInLastWeekItems() {
        EOFetchSpecification fs;
        NSDictionary bindings;
		EODatabaseDataSource _queryDataSource;
        Session s = (Session)session();

        bindings = new NSDictionary(new Object[] { daysAgo(7)}, new Object[] {"startdate"});
        fs=EOFetchSpecification.fetchSpecificationNamed("ocmClosedInLastWeek","Item").fetchSpecificationWithQualifierBindings(bindings);
		fs.setRefreshesRefetchedObjects(true);

        _queryDataSource =new EODatabaseDataSource(s.defaultEditingContext(), "Item");
        _queryDataSource.setFetchSpecification(fs);
		
		// sort order
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("product.productName", EOSortOrdering.CompareCaseInsensitiveAscending),
		};
        displayGroupClosed.setDataSource(_queryDataSource);
		displayGroupClosed.setSortOrderings(new NSArray(orderings));
		displayGroupClosed.setNumberOfObjectsPerBatch(displayGroupClosed.allObjects().count());
        displayGroupClosed.fetch();
    }
	
	public NSTimestamp weekAgo() {
	  return daysAgo(7);
	}
	
    public NSTimestamp daysAgo(int pVal) {
		NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);
		currDate = new NSTimestamp(now.getTime(), tz);

        NSTimestamp daysAgoDate =  currDate.timestampByAddingGregorianUnits (0, 0, (pVal*-1), 0, 0, 0);
        NSTimestamp daysAgoMidnight =  daysAgoDate.timestampByAddingGregorianUnits (0, 0, 0, ((daysAgoDate.hourOfDay()*-1)+7), (daysAgoDate.minuteOfHour()*-1), (daysAgoDate.secondOfMinute()*-1));

        //System.out.println("now - " + now);
        //System.out.println("currDate - " + currDate);
        //System.out.println("daysAgoDate - " + daysAgoDate);
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
	   
	public NSArray potentialProjects() {
        Session s = (Session)session();
        NSMutableArray temp  = new NSMutableArray(s.potentialProjects());
        temp.insertObjectAtIndex("All", 0);
        return(NSArray)temp;
    }

	
	
	public void refreshData() {
        displayGroup.fetch();
        //totalActive = displayGroup.allObjects().count();
    }
	
	public void setNeedUpdate(boolean pVal) {
	 // do nothing
	}


    public WOComponent resetDisplayedBugs() {
        if(!selectedProject.equals("All")) {
            session().takeValueForKey(selectedProject, "selectedProject");
        }

        performSearch();
		needUpdate = true;
		
        return null;
    }
	
	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = true;
	}
	
	public boolean onlyOpen() {
	  return onlyOpen;
	}
	
	public void setOnlyOpen(boolean pVal) {
	    onlyOpen = pVal;
	}


}