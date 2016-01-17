package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;


public class Triage extends WOComponent {
	private static final long serialVersionUID = 1L;
	public WODisplayGroup displayGroup;
    protected EODatabaseDataSource _queryDataSource;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    public String aRow = "Version";
    public String aColumn = "Priority";
    public String selectedProject = "All";
    public boolean needUpdate = false;
    public boolean isList = false;
    public boolean isReport = true;
  
    public Triage(WOContext aContext) {
        super(aContext);
        Session mysession = (Session)session();
        _queryDataSource =new EODatabaseDataSource(mysession.defaultEditingContext(), "Item");
        //selectedProject = mysession.selectedProject;
		
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
        //Object proj[]={"1.0"};
       // NSMutableArray temp  = new NSMutableArray(proj);
        temp.insertObjectAtIndex("All", 0);
        return(NSArray<String>)temp;

    }
    public void performSearch() {
        NSDictionary<Object, Object> bindings = null;
        EOFetchSpecification fs;
		
		if(selectedProject.equals("All")) {
			System.out.println("Triage.performSearch() - All " + selectedProject);
		
			fs=EOFetchSpecification.fetchSpecificationNamed("triageBugsAll","Item");
		}
		else {
			System.out.println("Triage.performSearch() - selectedProject: " + selectedProject);
			bindings = new NSDictionary<Object, Object>(new Object[] {selectedProject}, new Object[] { "version"});
			fs=EOFetchSpecification.fetchSpecificationNamed("triageBugs","Item").fetchSpecificationWithQualifierBindings(bindings);

		}
		fs.setRefreshesRefetchedObjects(true);

        _queryDataSource.setFetchSpecification(fs);
		displayGroup.setDataSource((EODataSource)_queryDataSource);
		displayGroup.fetch();
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
        displayGroup.fetch();
    }

	public void setNeedUpdate(boolean pVal) {
	 // do nothing
	}

    public WOComponent resetDisplayedBugs() {
        performSearch();
		needUpdate = true;

        return null;
    }
}