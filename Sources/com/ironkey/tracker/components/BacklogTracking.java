package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;


public class BacklogTracking extends WOComponent {
	private static final long serialVersionUID = 1L;

	public WODisplayGroup displayGroup;
	public WODisplayGroup displayGroup2;
    //protected EODatabaseDataSource _queryDataSource;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    public String aRow = "Component";
    public String aColumn = "Version";
    //protected String selectedProject = "All";
    public boolean needUpdate = false;
	public boolean isList = false;
	public boolean isReport = true;
    public NSArray tabSoftwareBugsAndEnhancements;
  
    public BacklogTracking(WOContext aContext) {
        super(aContext);
		tabSoftwareBugsAndEnhancements();		
		displayGroup();
		displayGroup2();
		needUpdate = true;
	}
	
	public WODisplayGroup displayGroup() {
		if(displayGroup.allObjects().count() == 0) {
			displayGroup.setObjectArray(tabSoftwareBugsAndEnhancements());
		}
		return displayGroup;
	}

	public NSArray tabSoftwareBugsAndEnhancements() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        NSMutableArray qual2 = new NSMutableArray();
        NSMutableArray qual3 = new NSMutableArray();
        NSMutableArray qual4 = new NSMutableArray();
        EOQualifier qualifier;

		if(tabSoftwareBugsAndEnhancements == null) {
		
			// Stories and Epics
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Bug'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Enhancement'", null));
			qual.addObject(new EOOrQualifier(qual1));

			qual4.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
			qual4.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
			qual.addObject(new EOOrQualifier(qual4));

			qual3.addObject(EOQualifier.qualifierWithQualifierFormat("resolution != 'WONTFIX'", null));
			qual3.addObject(EOQualifier.qualifierWithQualifierFormat("resolution != 'DUPLICATE'", null));
			qual3.addObject(EOQualifier.qualifierWithQualifierFormat("resolution != 'WORKSFORME'", null));
			qual3.addObject(EOQualifier.qualifierWithQualifierFormat("resolution != 'INVALID'", null));
			qual.addObject(new EOAndQualifier(qual3));

			// Versions
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("version ='Kia (TA 3.0 Software Only)'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("version ='Kia-patch-1 (TA 3.0.1)'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("version ='Kia-patch-1.1 (TA 3.0.1.1 - Arvest)'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("version ='Kia-patch-2 (TA 3.0.2)'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("version ='Kia-patch-3 (TA 3.0.3)'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("version ='_Backlog'", null));
			qual.addObject(new EOOrQualifier(qual2));


			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
						
			tabSoftwareBugsAndEnhancements = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}

		return tabSoftwareBugsAndEnhancements;
	}

	public WODisplayGroup displayGroup2() {
		if(displayGroup2.allObjects().count() == 0) {
			displayGroup2.setObjectArray(tabSoftwareBugsAndEnhancementsWithCustomerImpact());
		}
		return displayGroup2;
	}
	public NSArray tabSoftwareBugsAndEnhancementsWithCustomerImpact() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Customer impacting
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'customerreported'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'releasenote'", null));

			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(tabSoftwareBugsAndEnhancements(), new EOOrQualifier(qual)) ;
    }


    public void appendToResponse(WOResponse r, WOContext c) {

        if (_hasToUpdate) {
            //displayGroup.fetch();
            refreshData();
            _hasToUpdate=false;
        }
        super.appendToResponse(r,c);
    }
    public void refreshData() {
        displayGroup();
        displayGroup2();
    }

	public void setNeedUpdate(boolean pVal) {
	 // do nothing
	}

    public WOComponent resetDisplayedBugs() {
    //    performSearch();
		needUpdate = true;

        return null;
    }
}