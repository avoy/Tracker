package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.net.URL;

public class SecurityIssues extends WOComponent {
	private static final long serialVersionUID = 1L;	
    public WODisplayGroup trustedAccessDisplayGroup;
    public NSArray trustedAccessResolvedAndClosedItems;
    public WODisplayGroup secureStorageDisplayGroup;
    public NSArray secureStorageResolvedAndClosedItems;
    public NSArray openSecurityIssuesForTrustedAccess;
    public NSArray openSecurityIssuesForSecureStorage;
    public WODisplayGroup serviceDisplayGroup;
    public NSArray openSecurityIssuesForService;
    public NSArray serviceResolvedAndClosedItems;
    public NSData dataForCheckboxOff;
    public NSData dataForCheckboxOn;

	public boolean emailFlag = false;
	public Item anItem;
	public NSTimestamp today;
	public NSArray potentialQueues= new NSArray(new Object[] {"Media Platform" });
	//public NSArray potentialQueues= new NSArray(new Object[] {"Trusted Access", "Secure Storage", "Services" });
	public String aQueue;
	public String selectedQueue = "Media Platform";
    public int index;
	
    public SecurityIssues(WOContext context) {
        super(context);
		today = new NSTimestamp();
		initTrustedAccess();
		initSecureStorage();
    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	public int indexOneBase() {
		return index+1;
	}
	
	public boolean isTrustedAccess() {
		return (selectedQueue.equals("Media Platform"));
		//return (selectedQueue.equals("Trusted Access"));
	}
	public boolean isSecureStorage() {
		return (selectedQueue.equals("Secure Storage"));
	}
	public boolean isServices() {
		return (selectedQueue.equals("Services"));
	}

	public void initTrustedAccess() {

    }
	
	public NSArray openSecurityIssuesForTrustedAccess() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual2 = new NSMutableArray<EOQualifier>();

		if(openSecurityIssuesForTrustedAccess == null) {
		
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like '*security-issue*'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'security-issue'", null));

			// 'Open items'
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='UNCONFIRMED'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
			qual.addObject(new EOOrQualifier(qual2));


			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
						
			openSecurityIssuesForTrustedAccess = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}

		return openSecurityIssuesForTrustedAccess;
	}
	
	public WODisplayGroup trustedAccessDisplayGroup() {
		if(trustedAccessDisplayGroup.allObjects().count() == 0) {
			trustedAccessDisplayGroup.setObjectArray(openSecurityIssuesForTrustedAccess());
		}
		return trustedAccessDisplayGroup;
	}
	
	
	public NSArray topLevelStoriesForArray(NSArray pArray) {
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer = pArray.objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item story = (Item)enumer.nextElement();
			if(story.hasParents() == true) {
				Item epic = (Item)((NSArray)story.topMostParents()).objectAtIndex(0);
				if(tempArray.containsObject(epic) == false) {
					tempArray.addObject(epic);
				}
			}
			else if(tempArray.containsObject(story) == false) {
				tempArray.addObject(story);
			}
		}
		
		//System.out.println("tempArray - " + tempArray.count());
		Object orderings[]={
			//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(tempArray, new NSArray(orderings));
	}

	public NSArray trustedAccessResolvedAndClosedItems() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		if(trustedAccessResolvedAndClosedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CONFIRMED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='UNCONFIRMED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'security-issue'", null));

			Object orderings[]={
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			trustedAccessResolvedAndClosedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return trustedAccessResolvedAndClosedItems;
    }
	public void initSecureStorage() {
	}
	public NSArray openSecurityIssuesForSecureStorage() {
	
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;
		
		if(openSecurityIssuesForSecureStorage  == null) { 

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Secure Storage'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'security-issue'", null));
			

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			//((EODatabaseDataSource)secureStorageDisplayGroup.dataSource()).setFetchSpecification(fs);
			//secureStorageDisplayGroup.fetch();
			openSecurityIssuesForSecureStorage = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));

		}
		return openSecurityIssuesForSecureStorage;
    }

	public WODisplayGroup secureStorageDisplayGroup() {
		if(secureStorageDisplayGroup.allObjects().count() == 0) {
			secureStorageDisplayGroup.setObjectArray(openSecurityIssuesForSecureStorage());
		}
		return secureStorageDisplayGroup;
	}

	public NSArray secureStorageResolvedAndClosedItems() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		if(secureStorageResolvedAndClosedItems == null) {
			// 'Resolves items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CONFIRMED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='UNCONFIRMED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));

			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Secure Storage'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'security-issue'", null));
		

			Object orderings[]={			
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			secureStorageResolvedAndClosedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return secureStorageResolvedAndClosedItems;
    }
	public NSArray openSecurityIssuesForService() {
	
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;
		
		if(openSecurityIssuesForService  == null) { 

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'security-issue'", null));
			

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			//((EODatabaseDataSource)secureStorageDisplayGroup.dataSource()).setFetchSpecification(fs);
			//secureStorageDisplayGroup.fetch();
			openSecurityIssuesForService = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));

		}
		return openSecurityIssuesForService;
    }	
	
	public WODisplayGroup serviceDisplayGroup() {
		if(serviceDisplayGroup.allObjects().count() == 0) {
			serviceDisplayGroup.setObjectArray(openSecurityIssuesForService());
		}
		return serviceDisplayGroup;
	}
	
	public NSArray serviceResolvedAndClosedItems() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		if(serviceResolvedAndClosedItems == null) {
			// 'Resolves items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));

			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'security-issue'", null));
		

			Object orderings[]={			
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			serviceResolvedAndClosedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return serviceResolvedAndClosedItems;
    }
	
	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = true;
	}
	public String selectedQueue() {
	  return selectedQueue;
	}
	
	public void setSelectedQueue(String pVal) {
	    selectedQueue = pVal;
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
		return ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/show_bug.cgi?id=" +anItem.valueForKey("bugId");
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
    public WOComponent goUpdate()
    {
		// need to clean this up
		initTrustedAccess();
		initSecureStorage();
		openSecurityIssuesForTrustedAccess = null;
		trustedAccessResolvedAndClosedItems = null; 
		trustedAccessResolvedAndClosedItems();
		openSecurityIssuesForSecureStorage = null; 
		secureStorageResolvedAndClosedItems = null; 
		secureStorageResolvedAndClosedItems();
		//System.out.println("selectedQueues - " + selectedQueues);
        return null;
    }

    public WOComponent doNothing()
    {
        return null;
    }
    public WOComponent doSave()
    {
		try {
			Thread.sleep(1500);  // allow the javascript ajax call to complete before refetching
		}
		catch(Exception e) {
			System.err.println("StoryPriorities.doSave() exception - " + e);
		}
		if(isTrustedAccess()) {
			openSecurityIssuesForTrustedAccess = null;
			trustedAccessDisplayGroup.setObjectArray(topLevelStoriesForArray(openSecurityIssuesForTrustedAccess()));
		}
		else if(isSecureStorage()) {
			openSecurityIssuesForSecureStorage = null;
			secureStorageDisplayGroup.setObjectArray(topLevelStoriesForArray(openSecurityIssuesForSecureStorage()));
		}

		
        return null;
    }

}