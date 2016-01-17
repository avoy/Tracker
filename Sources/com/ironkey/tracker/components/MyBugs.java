package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class MyBugs extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup displayGroup;
    protected EODatabaseDataSource _queryDataSource;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    public String aRow = "Component";
    public String aColumn = "Priority";
    public String selectedProject;
    public boolean needUpdate = false;		
	public boolean isList = true;
	public boolean isReport = false;
	public boolean releaseFlag = true;

    public MyBugs(WOContext aContext) {
        super(aContext);
        Session mysession = (Session)session();
        _queryDataSource =new EODatabaseDataSource(mysession.defaultEditingContext(), "Item");
        selectedProject = mysession.selectedProject();

        if (mysession.getUser()!=null) {
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
    }

    public NSArray<String> potentialProjects() {
        Session s = (Session)session();
        NSMutableArray<String> temp  = new NSMutableArray<String>(s.potentialProjects());
        temp.insertObjectAtIndex("All", 0);
        return(NSArray<String>)temp;

    }
    public void performSearch() {
        NSDictionary<Object, Object> bindings = null;
        EOFetchSpecification fs;
        Session s = (Session)session();

		if(!selectedProject.equals("All")) {
			bindings = new NSDictionary<Object, Object>(new Object[] {s.getUser(), selectedProject}, new Object[] {"user", "version"});
		}
		else {
			bindings = new NSDictionary<Object, Object>(new Object[] {s.getUser()}, new Object[] {"user"});
		}
        fs=EOFetchSpecification.fetchSpecificationNamed("myBugs","Item").fetchSpecificationWithQualifierBindings(bindings);

        _queryDataSource.setFetchSpecification(fs);
		displayGroup.setDataSource((EODataSource)_queryDataSource);
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
    }

	public String notLoggedIn() {
		return "You are not currently logged in, please login.";
	}
	
	public void setNeedUpdate(boolean pVal) {
	 // do nothing
	}
	
	public void setNotLoggedIn(String pValue) {
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

}