package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.net.URL;
import java.util.regex.*;

public class TopCustomerIssuesList extends WOComponent {
	private static final long serialVersionUID = 1L;	
    public WODisplayGroup trustedAccessDisplayGroup;
    public NSArray<EOEnterpriseObject> trustedAccessResolvedItems;
    public WODisplayGroup secureStorageDisplayGroup;
    public NSArray<EOEnterpriseObject> secureStorageResolvedItems;
    public NSData dataForCheckboxOff;
    public NSData dataForCheckboxOn;

	public boolean emailFlag = false;
	public boolean show = true;
	/** @TypeInfo Item */
	public Item anItem;
	/** @TypeInfo Item */
	public Item aCustomer;
	public NSTimestamp today;
	public NSArray<String> potentialQueues= new NSArray<String>(new String[] {"Trusted Access", "Secure Storage"});
	public String aQueue;
	public NSArray<String> selectedQueues = new NSArray<String>(new String[] {"Trusted Access"});
	protected Pattern p; 
	protected Pattern p2; 
	protected Pattern p3; 
	protected NSArray taServiceIssues;  // contain 'trustedaccess' keyword - meaning it is a service issue that only applies to TA
	protected NSArray ssServiceIssues;  // contain 'securestorage' keyword - meaning it is a service issue that only applies to SS

	public boolean isTA() {
		return (selectedQueues.contains("Trusted Access"));
	}
	public boolean isSS() {
		return (selectedQueues.contains("Secure Storage"));
	}
	
    public TopCustomerIssuesList(WOContext context) {
        super(context);
		today = new NSTimestamp();
		initTrustedAccess();
		initSecureStorage();
		p =  Pattern.compile("(\\[.*\\] )(.*)"); // anything in square braces, followed by a space	'[CLONE 15421] '
		p2 =  Pattern.compile("(.*: )(.*)"); // remove any of the leading qualifiers that occur prior to a ': '
		p3 =  Pattern.compile(".*\\((.*)\\)"); // only the text between the (), e.g. 'Lexus (TA 3.1)'
		
		if(((Application)Application.application()).isImation()) {  // Only set this for Imation, not IronKey
			selectedQueues = new NSArray(new Object[] {"Secure Storage"});
		}
		else {
			selectedQueues = new NSArray(new Object[] {"Trusted Access"});			
		}

    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	
	public NSArray customersFromItem() {
		NSArray topMostParents = anItem.topMostParents();
		
        Enumeration enumer = topMostParents.objectEnumerator();
        NSMutableArray temp = new NSMutableArray();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            if((temp.containsObject(tempEo) == false) && ((tempEo.type().equals("Customer") || (tempEo.type().equals("Prospect"))))) {
                temp.addObject(tempEo);
            }
        }
        return temp;	
	}

	public String rowColor() {
		String bgcolor = "#FFFFFF";
		if((anItem.bugStatus().equals("RESOLVED"))|| (anItem.bugStatus().equals("VERIFIED"))) {
		   bgcolor = "#eeeeee";
		}
        return bgcolor;
	}
	
	public String filteredRelease() {
		String returnVal;
		
		String actualRelease =(String)anItem.valueForKey("version");
		if((actualRelease.equals("_Backlog")) || (actualRelease.equals("__Graveyard"))) {
			returnVal = "Uncommitted";
		}
		else if(actualRelease.equals("Proposed for the next release")) {
			returnVal = "Proposed";
		}
		else {
				Matcher m = p3.matcher(actualRelease);
				if(m.find() == true) {
				//System.out.println("pattern1 - " + m.group(1));
					returnVal = m.group(1);
				}
				else {
					returnVal = actualRelease;
				}
		}
		
		return returnVal;
	}

	public String cleanCustomerDescription() {
		int index = 0;
		String returnVal = (String)aCustomer.valueForKey("shortDesc");
		boolean  hasPrefix = returnVal.startsWith("TASER EVAL: ");
		//System.out.println("index - " + index);
		if(hasPrefix == true) {
			index = 12;
		}
		return returnVal.substring(index);
	}
	
	public String cleanItemDescription() {
		String returnVal;

		returnVal = (String)anItem.valueForKey("shortDesc");
		Matcher m = p.matcher(returnVal);
		if(m.find() == true) {
			//System.out.println("pattern1 - " + m.group(1));
			returnVal = m.group(2);
		}
		
		Matcher m2 = p2.matcher(returnVal);
		if(m2.find() == true) {
			//System.out.println("pattern2 - " + m2.group(1));
			returnVal = m2.group(2);
		}
		
		return returnVal;
	}

	public void initTrustedAccess() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();
        NSMutableArray allTrustedAccess;

		// 'Not Closed items'
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		//qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		//qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// TA Products or Service
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Desktop'", null));
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Mobile'", null));		
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
		qual.addObject(new EOOrQualifier(qual1));

		
		// 'new style support escalations'	
		qual1 = new NSMutableArray();				
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'customerreported'", null));
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'sereported'", null));
		//qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords like '*support_escalation*'", null));
		qual.addObject(new EOOrQualifier(qual1));

		// not like SS
		//qual.addObject(EONotQualifier.qualifierWithQualifierFormat("keywords  like '*SS*'", null));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		//1) Fetch all 
		//2) filter for SS
		//3) remove SS from all
		//4) setObjectArray 
		allTrustedAccess = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		
		// Removing SS specific service issues.		
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
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();
		NSMutableArray allTrustedAccess;

//        trustedAccessResolvedItems= new NSArray();  // Hack to not return any items, remove this line to make this work properly

		if(trustedAccessResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));

			// TA Products or Service
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Desktop'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Client: Trusted Access - Mobile'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Marble Services'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'IronKey Services'", null));
			qual.addObject(new EOOrQualifier(qual1));
			
			// 'new style support escalations'					
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'customerreported'", null));
 
			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			//1) Fetch all 
			//2) filter for SS
			//3) remove SS from all
			allTrustedAccess = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

			// Removing SS specific service issues.
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
		qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'customerreported'", null));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		//((EODatabaseDataSource)secureStorageDisplayGroup.dataSource()).setFetchSpecification(fs);
		//secureStorageDisplayGroup.fetch();
		//1) Fetch all 
		//2) filter for SS
		//3) remove SS from all
		//4) setObjectArray 
		allSecureStorage = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

		// Removing TA specific service issues.
		//System.out.println("taServiceIssues - " + taServiceIssues().count());
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
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'customerreported'", null));
 
			Object orderings[]={			
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			//1) Fetch all 
			//2) filter for SS
			//3) remove SS from all
			//4) setObjectArray 
			allSecureStorage = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

			// Removing TA specific service issues.
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

}