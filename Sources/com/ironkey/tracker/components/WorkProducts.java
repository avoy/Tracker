package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.*;

public class WorkProducts extends WOComponent {
	private static final long serialVersionUID = 1L;	
	public WOComponent theNextPage;
	public NSArray<EOEnterpriseObject> newBugs; 
	public NSArray<EOEnterpriseObject> preConceptItems; 
	public NSArray<EOEnterpriseObject> conceptItems; 
	public NSArray<EOEnterpriseObject> planningItems; 
	public NSArray<EOEnterpriseObject> developmentItems; 
	public NSArray<EOEnterpriseObject> launchItems; 
	public NSArray<EOEnterpriseObject> measurementItems; 
	public NSTimestamp today;
	public Item currentItem;
    public boolean isEmail;
    public String aState;
    public NSArray selectedStates;
    public NSArray PotentialStates;
	public boolean showLater=true;
	public int itemNumber = 0;
	public NSMutableDictionary planOfRecordURLForReleaseProduct;

    public WorkProducts(WOContext context) {
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
        EOQualifier qualifier;
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
	
	public String assignee() {
		String returnVal = (String)currentItem.valueForKeyPath("assignee.realname");
		if(returnVal.equals("Bug Master")) {
			returnVal = "---";
		}
		return returnVal;
	}
	
	
	
	public boolean isComplete() {
		boolean returnVal = false;
		
		String status = (String)currentItem.bugStatus();
		if((status.equals("VERIFIED")) || (status.equals("CLOSED"))) {
			returnVal = true;
		}
		
		return returnVal;
	}
	
	
    public NSArray fetchItems(EOQualifier pQual) {
        EOFetchSpecification fs;
        Session s = (Session)session();
		NSArray items = null;

		if(pQual != null) {
			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
			fs = new EOFetchSpecification("Item", pQual, new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);

			items = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return items;
    }

    public NSArray preConceptItems() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(preConceptItems == null) {
			// Work Products			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='_Work Product'", null));
			// Release			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ "_Template" + "' ", null));
			// milestone			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ "Pre-Concept" + "' ", null));
			
			
			preConceptItems = fetchItems(new EOAndQualifier(qual));
		}
		return preConceptItems;
    }

    public NSArray conceptItems() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(conceptItems == null) {
			// Work Products			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='_Work Product'", null));
			// Release			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ "_Template" + "' ", null));
			// milestone			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ "Concept" + "' ", null));
			
			
			conceptItems = fetchItems(new EOAndQualifier(qual));
		}
		return conceptItems;
    }

    public NSArray planningItems() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(planningItems == null) {
			// Work Products			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='_Work Product'", null));
			// Release			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ "_Template" + "' ", null));
			// milestone			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ "Planning" + "' ", null));
			
			
			planningItems = fetchItems(new EOAndQualifier(qual));
		}
		return planningItems;
    }
    public NSArray developmentItems() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(developmentItems == null) {
			// Work Products			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='_Work Product'", null));
			// Release			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ "_Template" + "' ", null));
			// milestone			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ "Development" + "' ", null));
			
			
			developmentItems = fetchItems(new EOAndQualifier(qual));
		}
		return developmentItems;
    }

    public NSArray launchItems() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(launchItems == null) {
			// Work Products			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='_Work Product'", null));
			// Release			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ "_Template" + "' ", null));
			// milestone			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ "Launch" + "' ", null));
			
			
			launchItems = fetchItems(new EOAndQualifier(qual));
		}
		return launchItems;
    }
	
    public NSArray measurementItems() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(measurementItems == null) {
			// Work Products			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='_Work Product'", null));
			// Release			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ "_Template" + "' ", null));
			// milestone			
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ "Measurement" + "' ", null));
			
			
			measurementItems = fetchItems(new EOAndQualifier(qual));
		}
		return measurementItems;
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
        WorkProducts component = (WorkProducts)pageWithName("WorkProducts");
		//component.setSelectedStates(selectedStates);
		component.setIsEmail(true);
		
		// time string
		NSTimestampFormatter formatter = new NSTimestampFormatter("%m/%d/%Y");
		String timeString = formatter.format(today);
 
        nextPage.takeValueForKey((WOComponent)component,"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");
        nextPage.takeValueForKey(from,"from");
        nextPage.takeValueForKey("IronKey - Work Products - " + timeString, "subject");

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
	/*
	public boolean isComplete() {
		return (selectedStates.contains("COMPLETE"))?true:false;
	}
	*/

    public WOComponent goUpdate() {
		newBugs = null; 
		preConceptItems = null; 
		conceptItems = null; 
		planningItems = null; 
		launchItems = null; 
		developmentItems = null;
		measurementItems = null;
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




