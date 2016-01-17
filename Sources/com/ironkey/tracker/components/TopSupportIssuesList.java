package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;
import java.io.File;
import java.net.URL;

public class TopSupportIssuesList extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup trustedAccessDisplayGroup;
    public NSArray trustedAccessResolvedItems;
    public WODisplayGroup secureStorageDisplayGroup;
    public NSArray secureStorageResolvedItems;
    public NSData dataForCheckboxOff;
    public NSData dataForCheckboxOn;
    public boolean emailFlag = false;
    public boolean collapseFlag = false;
	/** @TypeInfo Item **/
	public Item anItem;
	public NSTimestamp today;
	public NSArray potentialQueues = new NSArray(new Object[] {"Trusted Access", "Secure Storage"});
	public String aQueue;
	public NSArray selectedQueues;
	
	public NSArray potentialProducts = new NSArray(new Object[] { "Mobile", "Desktop", "Services", "OCM", "TAB"});
	public String aProduct;
	public NSArray selectedProducts;
    
	public NSArray potentialTypes = new NSArray(new Object[] {"Bug", "Enhancement"});
	public String aType;
	public String aCase;	
	public NSArray selectedTypes;
	public NSArray taServiceIssues;  // contain 'trustedaccess' keyword - meaning it is a service issue that only applies to TA
	public NSArray ssServiceIssues;  // contain 'securestorage' keyword - meaning it is a service issue that only applies to SS
	
	public boolean isTA() {
		return (selectedQueues.contains("Trusted Access"));
	}
	public boolean isSS() {
		return (selectedQueues.contains("Secure Storage"));
	}
	
	public NSTimestamp startDate() {
	       return today.timestampByAddingGregorianUnits(0,0,-30,0,0,0);
	}
	
    public TopSupportIssuesList(WOContext context) {
        super(context);
		today = new NSTimestamp();
		
		selectedTypes = new NSArray(new Object[] {"Bug"});
		selectedProducts = new NSArray(new Object[] { "Mobile", "Desktop", "Services", "OCM", "TAB"});
		selectedQueues = new NSArray(new Object[] {"Trusted Access"});

		initTrustedAccess();
		//initSecureStorage();
				
		/*
	 	if(((Application)Application.application()).isImation()) {  // Only set this for Imation, not IronKey
			selectedQueues = new NSArray(new Object[] {"Secure Storage"});
		}
		else {
			selectedQueues = new NSArray(new Object[] {"Trusted Access"});
			
		}
		*/
    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	public void initTrustedAccess() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();
        NSMutableArray allTrustedAccess;

		// 'Open items'
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// TA Products or Service
		//{ "Mobile", "Desktop", "Services", "OCM", "TAB"});
		
		if(selectedProducts.containsObject("Mobile")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Mobile'", null));
		}
		
		if(selectedProducts.containsObject("Desktop")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Desktop'", null));
		}
		
		if(selectedProducts.containsObject("Services")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
		}
		
		if(selectedProducts.containsObject("OCM")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Ops Change Management (OCM)'", null));
		}
		
		if(selectedProducts.containsObject("TAB")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
		}
		
		qual.addObject(new EOOrQualifier(qual1));

		// bugs and/or enhancements
		qual1 = new NSMutableArray();
		Enumeration enumer1 = selectedTypes.objectEnumerator();
		while(enumer1.hasMoreElements()) {
			String key = (String)enumer1.nextElement();
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = '" + key + "'", null));
		}
		qual.addObject(new EOOrQualifier(qual1));
		
		// 'new style support escalations'					
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		allTrustedAccess = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		
		Enumeration enumer = ssServiceIssues().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item ssItem = (Item)enumer.nextElement();
			if(allTrustedAccess.contains(ssItem)) {
				allTrustedAccess.removeObject(ssItem);
			}
		}

		trustedAccessDisplayGroup.setObjectArray(allTrustedAccess);		

    }
	public NSArray trustedAccessResolvedItems() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
		NSMutableArray allTrustedAccess;
        NSArray ssServiceIssues;  // contain SS keyword

        EOQualifier qualifier;

		if(trustedAccessResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));

			// TA Products or Service
			//{ "Mobile", "Desktop", "Services", "OCM", "TAB"});
			
			if(selectedProducts.containsObject("Mobile")) {
				qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Mobile'", null));
			}
			
			if(selectedProducts.containsObject("Desktop")) {
				qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Desktop'", null));
			}
			
			if(selectedProducts.containsObject("Services")) {
				qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
				qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
			}
			
			if(selectedProducts.containsObject("OCM")) {
				qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Ops Change Management (OCM)'", null));
			}
			
			if(selectedProducts.containsObject("TAB")) {
				qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
			}
			
			qual.addObject(new EOOrQualifier(qual1));
			
			// 'new style support escalations'					
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));
			
			// bugs and/or enhancements
			qual1 = new NSMutableArray();
			Enumeration enumer1 = selectedTypes.objectEnumerator();
			while(enumer1.hasMoreElements()) {
				String key = (String)enumer1.nextElement();
				qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = '" + key + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			allTrustedAccess = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
			
			Enumeration enumer = ssServiceIssues().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item ssItem = (Item)enumer.nextElement();
				if(allTrustedAccess.contains(ssItem)) {
					allTrustedAccess.removeObject(ssItem);
				}
			}
			trustedAccessResolvedItems	= new NSArray(allTrustedAccess);
		}
		return trustedAccessResolvedItems;
    }

	public NSArray trustedAccessItems() {  // used for the graph
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
		NSMutableArray allTrustedAccess;
        EOQualifier qualifier;


		// TA Products or Service
		// TA Products or Service
		//{ "Mobile", "Desktop", "Services", "OCM", "TAB"});
		
		if(selectedProducts.containsObject("Mobile")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Mobile'", null));
		}
		
		if(selectedProducts.containsObject("Desktop")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Desktop'", null));
		}
		
		if(selectedProducts.containsObject("Services")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
		}
		
		if(selectedProducts.containsObject("OCM")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Ops Change Management (OCM)'", null));
		}
		
		if(selectedProducts.containsObject("TAB")) {
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
		}
		
		qual.addObject(new EOOrQualifier(qual1));
		
		// 'new style support escalations'					
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));
		
		// bugs and/or enhancements
		qual1 = new NSMutableArray();
		Enumeration enumer1 = selectedTypes.objectEnumerator();
		while(enumer1.hasMoreElements()) {
			String key = (String)enumer1.nextElement();
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = '" + key + "'", null));
		}
		qual.addObject(new EOOrQualifier(qual1));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		allTrustedAccess = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		
		Enumeration enumer = ssServiceIssues().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item ssItem = (Item)enumer.nextElement();
			if(allTrustedAccess.contains(ssItem)) {
				allTrustedAccess.removeObject(ssItem);
			}
		}
		return (NSArray)allTrustedAccess;
    }
	
	public void initSecureStorage() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
		NSMutableArray allSecureStorage;		
        EOQualifier qualifier;

		// 'Open items'
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// Secure Storage Products or Service
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Secure Storage'", null));
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
		qual.addObject(new EOOrQualifier(qual1));
		
		// 'new style support escalations'					
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));

		// bugs and/or enhancements
		qual1 = new NSMutableArray();
		Enumeration enumer1 = selectedTypes.objectEnumerator();
		while(enumer1.hasMoreElements()) {
			String key = (String)enumer1.nextElement();
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = '" + key + "'", null));
		}
		qual.addObject(new EOOrQualifier(qual1));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		allSecureStorage = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		
		Enumeration enumer = taServiceIssues().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item taItem = (Item)enumer.nextElement();
			if(allSecureStorage.contains(taItem)) {
				allSecureStorage.removeObject(taItem);
			}
		}
		secureStorageDisplayGroup.setObjectArray(allSecureStorage);
    }

	public NSArray secureStorageResolvedItems() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
		NSMutableArray allSecureStorage;
        EOQualifier qualifier;

		if(secureStorageResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));


			// Secure Storage Products or Service
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Secure Storage'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			qual.addObject(new EOOrQualifier(qual1));
			
			// 'new style support escalations'					
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));
 
			// bugs and/or enhancements
			qual1 = new NSMutableArray();
			Enumeration enumer1 = selectedTypes.objectEnumerator();
			while(enumer1.hasMoreElements()) {
				String key = (String)enumer1.nextElement();
				qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = '" + key + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));

			Object orderings[]={			
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			allSecureStorage = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
			
			Enumeration enumer = taServiceIssues().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item taItem = (Item)enumer.nextElement();
				if(allSecureStorage.contains(taItem)) {
					allSecureStorage.removeObject(taItem);
				}
			}
			secureStorageResolvedItems	= new NSArray(allSecureStorage);
		}
		return secureStorageResolvedItems;
    }

	public NSArray taServiceIssues() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(taServiceIssues == null) {
			// Secure Storage Products or Service
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			
			// 'new style support escalations'					
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'trustedaccess'", null));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			taServiceIssues = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return taServiceIssues;
    }
	
	public NSArray ssServiceIssues() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(ssServiceIssues == null) {
			// Secure Storage Products or Service
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			
			// 'new style support escalations'					
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'securestorage'", null));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			ssServiceIssues = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return ssServiceIssues;
    }

	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = true;
	}
	
	public boolean collapseFlag() {
	  return collapseFlag;
	}
	
	public void setCollapseFlag(boolean pVal) {
	    collapseFlag = pVal;
	}

	public NSArray selectedQueues() {
	  return selectedQueues;
	}
	
	public void setSelectedQueues(NSArray pVal) {
	    selectedQueues = pVal;
	}
	
	public String timeSinceOpened() {
		return elapsedTimeSimple((NSTimestamp)anItem.valueForKey("creationTs"), today);
	}
	public String timeSinceModified() {
		return elapsedTimeSimple((NSTimestamp)anItem.valueForKey("lastdiffed"), today);
	}
	
	public String elapsedTimeSimple(NSTimestamp startTime, NSTimestamp endTime) {
		String returnVal = "";
		//System.out.println("startTime() - " + startTime() );
		//System.out.println("endTime() - " + endTime() );
		
		if((startTime != null) && (endTime != null)) {
			DateUtil dateUtil = new DateUtil(startTime, endTime);
			int numdays = (int)dateUtil.days;
			int numhours = (int)dateUtil.hours;
			int numMinutes = (int)dateUtil.minutes;
			
			if(numdays == 0) {
				returnVal += "" + "<1 day";
			}
			else if(numdays == 1) {
				returnVal += "" + numdays + " day";
			}
			else {
				returnVal += "" + numdays + " days";
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
	
	public String listURLForOpenTA() {
		return listURLForArray(trustedAccessDisplayGroup.displayedObjects());
	}
	
	public String listURLForArray(NSArray pArray) {
        Enumeration enumer;
        String allBugNumbers = "";

        enumer = pArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject currObject = (EOEnterpriseObject)enumer.nextElement();
            allBugNumbers += currObject.valueForKey("bugId") + ",";
        }

        String URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" + allBugNumbers;
        return URL;
     }
	 
	 //salesforceCases
	 public String urlForCase() {
		return "https://na12.salesforce.com/_ui/search/ui/UnifiedSearchResults?searchType=2&str=" + aCase;
		
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
    public WOComponent goUpdate()
    {
		// need to clean this up
		initTrustedAccess();
		initSecureStorage();
		trustedAccessResolvedItems = null; 
		trustedAccessResolvedItems();
		secureStorageResolvedItems = null; 
		secureStorageResolvedItems();
		//System.out.println("selectedQueues - " + selectedQueues);
        return null;
    }
    
	public WOComponent toggleCollapseFlag() {
		setCollapseFlag((!collapseFlag()));
        return null;
		
	}

}