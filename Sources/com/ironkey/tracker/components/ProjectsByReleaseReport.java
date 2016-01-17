package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.*;

public class ProjectsByReleaseReport extends WOComponent {
	private static final long serialVersionUID = 1L;
	public WOComponent theNextPage;
    public NSArray newBugs; 
	public NSArray criticalBugs; 
	public NSArray importantBugs; 
	public NSArray laterBugs; 
	public NSArray ongoingBugs; 
	public NSArray completeBugs; 
	public NSTimestamp today;
	public Item currentItem;
    public boolean isEmail;
    public String aState;
	public NSArray selectedStates;
	public NSArray PotentialStates;
	public boolean showLater=true;
	public int itemNumber = 0;
    protected NSMutableDictionary planOfRecordURLForReleaseProduct;

    public ProjectsByReleaseReport(WOContext context) {
        super(context);
		today = new NSTimestamp();		
		Session s = (Session)session();
        selectedStates = new NSArray(s.potentialStates());
		planOfRecordURLForReleaseProduct = new NSMutableDictionary();
    }

	public void awake() {
		itemNumber = 0;
	}

    public NSArray newBugs() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray temp;
		Enumeration enumer;

		if(newBugs == null) {
			// Type = 'Project'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type='Project'", null));
			
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED')", null));

			// Not 'COMPLETE' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("state !='COMPLETE'", null));

			// 'Unassigned' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("level='---'", null));
			
			// Users can select which 'states' to view
			int numStates = selectedStates.count();
			if(numStates < s.potentialStates().count()) {
				temp = new NSMutableArray();
				enumer = selectedStates.objectEnumerator();
				while(enumer.hasMoreElements()) {
					String theState = (String)enumer.nextElement();
					temp.addObject(EOQualifier.qualifierWithQualifierFormat("state='" + theState + "'", null));
				}
				qual.addObject(new EOOrQualifier(temp));
			}


			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
			
			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);

			newBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return newBugs;
    }
	
	public int itemNumber() {
		return ++itemNumber;
	}

    public NSArray criticalBugs() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;
		Enumeration enumer;
        NSMutableArray temp;

		if(criticalBugs == null) {
			// Type = 'Project'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type='Project'", null));

			// level=Critical
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("level='Critical'", null));
			//qualifier = EOQualifier.qualifierWithQualifierFormat("type='Project'", null);
			
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED')", null));

			// Not 'COMPLETE' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("state !='COMPLETE'", null));

			// Users can select which 'states' to view
			int numStates = selectedStates.count();
			if(!isOnlyComplete()) {
				if(numStates < s.potentialStates().count()) {
					temp = new NSMutableArray();
					enumer = selectedStates.objectEnumerator();
					while(enumer.hasMoreElements()) {
						String theState = (String)enumer.nextElement();
						if(!theState.equals("COMPLETE")) {
							temp.addObject(EOQualifier.qualifierWithQualifierFormat("state='" + theState + "'", null));
						}
					}
					qual.addObject(new EOOrQualifier(temp));
				}

				Object orderings[]={
						EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
				};

				fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
				//fs = new EOFetchSpecification("Item", qualifier,null);
				fs.setRefreshesRefetchedObjects(true);

				criticalBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
			}
		}
		return criticalBugs;
    }

    public NSArray importantBugs() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;
		Enumeration enumer;
        NSMutableArray temp;

		if(importantBugs == null) {
			// Type = 'Project'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type='Project'", null));

			// Level=Important
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("level='Important'", null));
			//qualifier = EOQualifier.qualifierWithQualifierFormat("type='Project'", null);

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED')", null));

			// Not 'COMPLETE' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("state !='COMPLETE'", null));

			// Users can select which 'states' to view
			int numStates = selectedStates.count();
			if(!isOnlyComplete()) {
				if(numStates < s.potentialStates().count()) {
					temp = new NSMutableArray();
					enumer = selectedStates.objectEnumerator();
					while(enumer.hasMoreElements()) {
						String theState = (String)enumer.nextElement();
						if(!theState.equals("COMPLETE")) {
							temp.addObject(EOQualifier.qualifierWithQualifierFormat("state='" + theState + "'", null));
						}
					}
					qual.addObject(new EOOrQualifier(temp));
				}

				Object orderings[]={
						EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
				};
				
				fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),new NSArray(orderings));
				fs.setRefreshesRefetchedObjects(true);

				importantBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
			}
		}
		return importantBugs;

    }

    public NSArray laterBugs() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;
		Enumeration enumer;
        NSMutableArray temp;

		if(laterBugs == null) {
			// Type = 'Project'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type='Project'", null));

			// level=later'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("level='Later'", null));

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED')", null));

			// Not 'COMPLETE' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("state !='COMPLETE'", null));

			// Users can select which 'states' to view
			int numStates = selectedStates.count();
			if(!isOnlyComplete()) {
				if(numStates < s.potentialStates().count()) {
					temp = new NSMutableArray();
					enumer = selectedStates.objectEnumerator();
					while(enumer.hasMoreElements()) {
						String theState = (String)enumer.nextElement();
						if(!theState.equals("COMPLETE")) {
							temp.addObject(EOQualifier.qualifierWithQualifierFormat("state='" + theState + "'", null));
						}
					}
					qual.addObject(new EOOrQualifier(temp));
				}

				Object orderings[]={
						EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
				};

				fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),new NSArray(orderings));
				fs.setRefreshesRefetchedObjects(true);
				
				laterBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
			}
		}
		return laterBugs;
    }
    public NSArray ongoingBugs() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;
		Enumeration enumer;
        NSMutableArray temp;

		if(ongoingBugs == null) {
			// Type = 'Project'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type='Project'", null));

			// level=later'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("level='Ongoing'", null));

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED')", null));

			// Not 'COMPLETE' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("state !='COMPLETE'", null));
			// Users can select which 'states' to view
			int numStates = selectedStates.count();
			if(!isOnlyComplete()) {
				if(numStates < s.potentialStates().count()) {
					temp = new NSMutableArray();
					enumer = selectedStates.objectEnumerator();
					while(enumer.hasMoreElements()) {
						String theState = (String)enumer.nextElement();
						if(!theState.equals("COMPLETE")) {
							temp.addObject(EOQualifier.qualifierWithQualifierFormat("state='" + theState + "'", null));
						}
					}
					qual.addObject(new EOOrQualifier(temp));
				}

				Object orderings[]={
						EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
				};

				fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),new NSArray(orderings));
				fs.setRefreshesRefetchedObjects(true);
				
				ongoingBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
			}
		}
		return ongoingBugs;
    }

    public NSArray completeBugs() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(completeBugs == null) {
			// Type = 'Project'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type='Project'", null));

			// level=later'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("level != 'Hidden'", null));

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED')", null));

			// Not 'COMPLETE' Projects
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("state ='COMPLETE'", null));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("level", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
		   
			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			completeBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return completeBugs;
    }
	
	public String planOfRecordURL() {
		return planOfRecordURLForItem(currentItem);
	}

	public String planOfRecordURLForItem(Item pItem) {
		String porUrl = null;
		
		if(pItem != null) {
			String product = (String)pItem.valueForKeyPath("product.productName");
			String version = (String)pItem.valueForKeyPath("version");
			porUrl = (String)planOfRecordURLForReleaseProduct.objectForKey(version+product);
			if(porUrl == null) {
				
				EOEnterpriseObject pfr = productForRelease(product, version);
				if(pfr != null)	{
					porUrl = (String)pfr.valueForKey("planOfRecordURL");
					if(porUrl == null) {
						porUrl = "";
					}	
					planOfRecordURLForReleaseProduct.setObjectForKey(porUrl,version+product);
				}
			}
		}
		return porUrl;
	}
	
	public boolean isOnlyComplete() {
		boolean returnVal = false;
		int numStates = selectedStates.count();

		if(numStates == 1 && selectedStates.contains("COMPLETE")) {						
			returnVal = true;
		}
		return returnVal;
	}
	
	public boolean isPorURL() {
		boolean returnVal = false;
		String porUrl = planOfRecordURL();
		if((porUrl != null) && (!porUrl.equals(""))) {
			returnVal = true;
		}
		return returnVal;
	}
	
	public EOEnterpriseObject productForRelease( String pProduct, String pRelease) {
        EOFetchSpecification fs = null;
        NSDictionary bindings = null;
        Session s = (Session)session();
		NSArray productsForRelease;
		EOEnterpriseObject returnVal = null;

        bindings = new NSDictionary(new Object[] {pRelease, pProduct}, new Object[] { "release", "product"});
		fs = EOFetchSpecification.fetchSpecificationNamed( "productForRelease", "ReleaseProduct").fetchSpecificationWithQualifierBindings( bindings );


        productsForRelease = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
		if(productsForRelease.count()==1) {
				returnVal = (EOEnterpriseObject)productsForRelease.lastObject();
		}
		return returnVal;
    }


    public WOComponent emailCurrentPage() {
        Session s = (Session)session();
        String from = "kavoy@marblesecurity.com";

        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");
        ProjectsByReleaseReport component = (ProjectsByReleaseReport)pageWithName("ProjectsByReleaseReport");
		component.setSelectedStates(selectedStates);
		component.setIsEmail(true);
		
		// time string
		NSTimestampFormatter formatter = new NSTimestampFormatter("%m/%d/%Y");
		String timeString = formatter.format(today);
 
        nextPage.takeValueForKey((WOComponent)component,"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");
        nextPage.takeValueForKey(from,"from");
        nextPage.takeValueForKey("IronKey Priorities List - " + timeString, "subject");

        return nextPage;
    }
	
	public boolean showLater() {
		return showLater;
	}
	public void setShowLater(boolean pVal) {
		showLater = pVal;
	 }
	
	public boolean isEmail() {
	   return isEmail;
	}
	public void setIsEmail(boolean pVal) {
	   isEmail = pVal;
	}
	public boolean isComplete() {
		return (selectedStates.contains("COMPLETE"))?true:false;
	}

    public WOComponent goUpdate() {
		newBugs = null; 
		criticalBugs = null; 
		importantBugs = null; 
		laterBugs = null; 
		completeBugs = null; 
		ongoingBugs = null;
        return null;
    }

	public void setSelectedStates(NSArray pValues) {
		selectedStates = pValues;
	}
	
	public String showLaterLabel() {
		String returnVal = "";
		if(showLater() == true) {
			returnVal = "hide";
		}
		else {
			returnVal = "show";
		}
		return returnVal;
	}
    public WOComponent toggleShowLater()
    {
		if(showLater() == true) {
			setShowLater(false);
		}
		else {
			setShowLater(true);
		}
        return null;
    }

}




