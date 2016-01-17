package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;
import java.net.URL;

public class TaserReport extends WOComponent {
	private static final long serialVersionUID = 1L;
	public WODisplayGroup taserDisplayGroup;
	public NSArray taserIssues;	
	public NSArray itemsWithMultipleParents;	
	public NSData dataForCheckboxOff;
	public NSData dataForCheckboxOn;
	public String taserId;

	public boolean emailFlag = false;
	/** @TypeInfo Item */
	public EOEnterpriseObject aTaser;
	/** @TypeInfo Item */
	public Item anItem;
	public NSTimestamp today;
	
	//protected NSArray supportQueue;
	public NSArray potentialQueues= new NSArray(new Object[] {"Issues For Banks", "Banks affected by Issues"});
    public String aQueue;
    public String selectedQueue = "Issues For Banks";
	

	
    public TaserReport(WOContext context) {
        super(context);
		today = new NSTimestamp();
		initTaser();
		//itemsWithMultipleParents();
    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	public boolean isTaser() {
		return (selectedQueue.equals("Issues For Banks"));
	}
	public boolean isMultiple() {
		return (selectedQueue.equals("Banks affected by Issues"));
	}

	public void initTaser() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		// 'Open items'
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// Taser			
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Prospect'", null));
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Customer'", null));
		qual.addObject(new EOOrQualifier(qual1));
		
		if(taserId() != null) {
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugId=" + taserId(), null));
		}


		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		
		((EODatabaseDataSource)taserDisplayGroup.dataSource()).setFetchSpecification(fs);
		taserDisplayGroup.fetch();
    }
	public NSArray sortedOpenChildren() {
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		return EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)aTaser.valueForKey("allOpenChildren"), new NSArray(orderings));
	}
	
	
	public NSArray taserIssues() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		if(taserIssues == null) {

			// Taser - want the items not the parents
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type != 'Prospect'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type != 'Customer'", null));

			qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'taser'", null));
			
			Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			taserIssues = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return taserIssues;
	}
	
	public NSArray itemsWithMultipleParents() {
		if(itemsWithMultipleParents == null) {
			NSMutableArray temp = new NSMutableArray();
			Enumeration enumer = taserIssues().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item taserIssue = (Item)enumer.nextElement();
				NSArray parents = (NSArray)taserIssue.topMostParents();  // will get some non-taser parents.    
				if((parents != null) && (parents.count() > 1)) {
					temp.addObject(taserIssue);
				}
			}
			itemsWithMultipleParents = new NSArray(temp);
		}
		return itemsWithMultipleParents;
	}
	
    public NSArray topMostTaserParents() {
        Enumeration enumer = anItem.topMostParents().objectEnumerator();
        NSMutableArray temp = new NSMutableArray();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            if((tempEo.type().equals("Prospect")) || (tempEo.type().equals("Customer"))) {
                temp.addObject(tempEo);
            }
        }
        return temp;
    }

	public String taserId() { return taserId;
	}
	public void setTaserId(String pVal) {
			taserId = pVal;
			initTaser();
	}
	
	public boolean emailFlag() { return emailFlag;
	}
	public void setEmailFlag(boolean pVal) { emailFlag = true;
	}
	
	public String selectedQueue() { return selectedQueue;
	}
	public void setSelectedQueue(String pVal) { selectedQueue = pVal;
	}
	
	public String timeSinceOpened() {
		return elapsedTime((NSTimestamp)anItem.valueForKey("creationTs"), today);
	}
	public String timeSinceModified() {
		return elapsedTime((NSTimestamp)anItem.valueForKey("lastdiffed"), today);
	}
	
	
			/**
	 * Returns the number of days from a start date to an end date
     *
     * @param  NSGregorianDate  - startDate
     * @param  NSGregorianDate  - endDate
     * @return 	int
     */
	public String elapsedTime(NSTimestamp startTime, NSTimestamp endTime) {
		String returnVal = "";
		//System.out.println("startTime() - " + startTime() );
		//System.out.println("endTime() - " + endTime() );
		
		if((startTime != null) && (endTime != null)) {
			DateUtil dateUtil = new DateUtil(startTime, endTime);
			int numdays = (int)dateUtil.days;
			int numhours = (int)dateUtil.hours;
			int numMinutes = (int)dateUtil.minutes;
			
			if(numdays != 0) {
				if(numdays == 1) {
					returnVal += "" + numdays + " day";
				}
				else {
					returnVal += "" + numdays + " days";
				}
			}
			if(numhours != 0) {
				if(numdays != 0) {
					returnVal += ", ";
				}
				if(numhours == 1) {
					returnVal += "" + numhours + " hour";
				}
				else {
					returnVal += "" + numhours + " hours";
				}
			}
			if(numMinutes != 0) {
				if((numhours != 0) || (numdays != 0)) {
					returnVal += " and ";
				}
				returnVal += "" + numMinutes + " minutes";
			}	
			if(returnVal.equals("")) {
				returnVal = "NA";
			}
		}
		else {
			returnVal = "NA";
		}
				
		return returnVal;
	}


	public String bugURL() {
		return ((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id=" +anItem.valueForKey("bugId");
	 }
	 
	public void displayAll() {
	}

	public NSData dataForCheckboxOn() {
		WOResourceManager rm;
		URL url;
		
		if(dataForCheckboxOn == null) {
			rm = application().resourceManager();
			url = rm.pathURLForResourceNamed("CheckboxOn.gif", "JavaDirectToWeb", null);

			try {
				//System.out.println(new NSData(url));
				dataForCheckboxOn = new NSData(url);
			}
			catch(Exception e) {
				System.err.println("dataForCheckbox - " + e);
			}
		}
		return dataForCheckboxOn;
	}
	public NSData dataForCheckboxOff() {
		WOResourceManager rm;
		URL url;
		
		if(dataForCheckboxOff == null) {
			rm = application().resourceManager();
			url = rm.pathURLForResourceNamed("CheckboxOff.gif", "JavaDirectToWeb", null);

			try {
				//System.out.println(new NSData(url));
				dataForCheckboxOff = new NSData(url);
			}
			catch(Exception e) {
				System.err.println("dataForCheckbox - " + e);
			}
		}
		return dataForCheckboxOff;
	}
	
    public TaserReport componentToEmail() {
        TaserReport comp = (TaserReport)pageWithName("TaserReport");
		comp.setEmailFlag(true);
		comp.setSelectedQueue(selectedQueue());
		comp.setTaserId(taserId());
        return comp;
    }
	public WOComponent emailCurrentPage() {
        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");

       nextPage.takeValueForKey(componentToEmail(),"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");
        nextPage.takeValueForKey("Taser Report: " +  selectedQueue(),"subject");

        return nextPage;
    }
	public void  invalidateChildren() {
        Enumeration enumer = taserDisplayGroup.allObjects().objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
			tempEo.invalidateAllChildren();

        }
    }
	public boolean isP1() {
		return (((String)aTaser.valueForKey("priority")).equals("1 - Urgent"))?true:false;
	}

	
    public WOComponent goUpdate()
    {
		// need to clean this up
		invalidateChildren();
		initTaser();
		itemsWithMultipleParents = null;
		//System.out.println("selectedQueues - " + selectedQueues);
        return null;
    }
	

}