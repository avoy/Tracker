package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;
import java.net.URL;

public class CustomerIssues extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup customerIssueDisplayGroup;
    public NSArray customerIssues;	
    public NSArray itemsWithMultipleParents;	
    public NSData dataForCheckboxOff;
    public NSData dataForCheckboxOn;
    public String customerId;
    public String bugId;
    public boolean isCustomerView = true;
    public boolean showProductSelector = true;
	public boolean emailFlag = false;
	/** @TypeInfo Item */
	public Item anIssue;
	/** @TypeInfo Item */
	public Item anItem;
	public NSTimestamp today;
	public NSArray potentialProducts= new NSArray(new Object[] {"Trusted Access", "Secure Storage"});
    public String aProduct;
    public NSArray selectedProducts = new NSArray(new Object[] {"Trusted Access"});	
	/** @TypeInfo Keyworddefs */
	public EOEnterpriseObject aKeyword;
	
    public CustomerIssues(WOContext context) {
        super(context);
		today = new NSTimestamp();
		initCustomerIssues();
		//itemsWithMultipleParents();
    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	/*
		public boolean isTaser() {
			return (selectedQueue.equals("Issues For Customers"));
		}
		
		public boolean isMultiple() {
			return (selectedQueue.equals("Customers affected by Issues"));
		}
	public boolean isIssueView() {
		return (selectedQueue.equals("Customers affected by Issues"));
	}
	*/
	public boolean isCustomerView() {
		return isCustomerView;
	}
	public void setIsCustomerView(boolean pVal) {
		isCustomerView = pVal;
	}
	
	public String cleanDescription() {
		int index = 0;
		String returnVal = (String)anIssue.valueForKey("shortDesc");
		boolean  hasPrefix = returnVal.startsWith("TASER EVAL: ");
		//System.out.println("index - " + index);
		if(hasPrefix == true) {
			index = 12;
		}
		return returnVal.substring(index);
	}

	public void initCustomerIssues() {
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
		
		// Single Customer
		if(customerId() != null) {
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugId=" + customerId(), null));
			showProductSelector = true;
		}
		else {
			// Products
			if(selectedProducts.count() == 1) {
				if(selectedProducts.contains("Trusted Access")) {
						qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Trusted Access'", null));
				}
				else if(selectedProducts.contains("Secure Storage")) {
						qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = 'Device: Secure Storage'", null));
				}
			}
		}


		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareAscending),
				EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		
		((EODatabaseDataSource)customerIssueDisplayGroup.dataSource()).setFetchSpecification(fs);
		customerIssueDisplayGroup.fetch();
    }
	
	public NSArray sortedOpenChildren() {
		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareAscending),
				EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};
		return EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)anIssue.valueForKey("allOpenChildren"), new NSArray(orderings));
	}
	
	public NSArray sortedClosedChildren() {
		Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareAscending),
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};
		return EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)anIssue.valueForKey("closedChildren"), new NSArray(orderings));
	}

	public NSArray customerIssues() {
        NSMutableArray allChildren = new NSMutableArray();
        NSMutableArray qual = new NSMutableArray();
		Item customer;
		Item child;

		if(customerIssues == null) {
		
			if(bugId() != null) {
				qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugId=" + bugId(), null));
				EOFetchSpecification spec = new EOFetchSpecification("Item", new EOAndQualifier(qual), null);

				// Perform actual fetch
				customerIssues = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(spec);
			
			}
			else {

				Enumeration enumer = ((NSArray)customerIssueDisplayGroup.allObjects()).objectEnumerator();
				// For each Customer
				while(enumer.hasMoreElements()) {
					customer = (Item)enumer.nextElement();
					NSArray children = (NSArray)customer.valueForKey("allChildren");
					
					// Add all of their issues (if they haven't been added already
					Enumeration enumer2 = children.objectEnumerator();
					while(enumer2.hasMoreElements()) {
						child = (Item)enumer2.nextElement();
						if(!allChildren.contains(child)) {
							allChildren.addObject(child);
						}
					}

				}
				Object orderings[]={
						EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareAscending),
						EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareAscending),
						EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
						EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				};
				
				// Sort the issues
				customerIssues =  EOSortOrdering.sortedArrayUsingKeyOrderArray(allChildren, new NSArray(orderings));
					
			}
			
		}
		return customerIssues;
	}
	
	public NSArray itemsWithMultipleParents() {
		if(itemsWithMultipleParents == null) {
			NSMutableArray temp = new NSMutableArray();
			Enumeration enumer = customerIssues().objectEnumerator();
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
						//System.out.println("Item - " + anItem.valueForKey("bugId")  + " - " + anItem.shortDesc());

        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
				//System.out.println("\t\tparentA - " + tempEo.bugId() + " - " + tempEo.shortDesc());

            if((tempEo.type().equals("Prospect")) || (tempEo.type().equals("Customer"))) {
                temp.addObject(tempEo);
				
				//System.out.println("\t\tparentB - " + tempEo.bugId() + " - " + tempEo.shortDesc());
            }
        }
        return temp;
    }
	

	public String customerId() { return customerId;
	}
	public void setCustomerId(String pVal) {
			customerId = pVal;
			initCustomerIssues();
	}
	public String bugId() { return bugId;
	}
	public void setBugId(String pVal) {
			bugId = pVal;
	}
	
	public boolean emailFlag() { return emailFlag;
	}
	public void setEmailFlag(boolean pVal) { emailFlag = true;
	}
	
	public NSArray selectedProducts() { return selectedProducts;
	}
	public void setSelectedProducts(NSArray pVal) { selectedProducts = pVal;
	}
	
	public String timeSinceOpened() {
		return elapsedTimeSimple((NSTimestamp)anItem.valueForKey("creationTs"), today);
	}
	public String timeSinceModified() {
		return elapsedTimeSimple((NSTimestamp)anItem.valueForKey("lastdiffed"), today);
	}
	public String filteredRelease() {
		String returnVal;
		
		String actualRelease =(String)anItem.valueForKey("version");
		if(actualRelease.equals("_Backlog")) {
			returnVal = "Uncommitted";
		}
		else if(actualRelease.equals("Proposed for the next release")) {
			returnVal = "Proposed";
		}
		else {
			returnVal = actualRelease;
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
	
    public CustomerIssues componentToEmail() {
        CustomerIssues comp = (CustomerIssues)pageWithName("CustomerIssues");
		comp.setEmailFlag(true);
		comp.setSelectedProducts(selectedProducts());
		comp.setCustomerId(customerId());
        return comp;
    }
	public WOComponent emailCurrentPage() {
        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");

       nextPage.takeValueForKey(componentToEmail(),"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");
        //nextPage.takeValueForKey("Customer Issue Report: " +  selectedQueue(),"subject");
        nextPage.takeValueForKey("Customer Issue Report ","subject");

        return nextPage;
    }
	
	public void invalidateObjects(NSArray pObjects) {
		//System.out.println("ReleasePlan.invalidateObjects()");

	    // Invalidate Objects
		if(pObjects != null) {
			//System.out.println("ReleasePlan.invalidateObjects() - 2");
		
			Enumeration enumer = pObjects.objectEnumerator();
			NSMutableArray temp = new NSMutableArray();
			while(enumer.hasMoreElements()) {
				Item i = (Item)enumer.nextElement();
				i.invalidateAllChildren();
				EOGlobalID id = (EOGlobalID)(session().defaultEditingContext().globalIDForObject(i));
				if(id != null) {
					temp.addObject(id);
				}
			}
			session().defaultEditingContext().invalidateObjectsWithGlobalIDs((NSArray)temp);
		}
	
	}
	
	public void  invalidateChildren() {
        /*
		Enumeration enumer = customerIssueDisplayGroup.allObjects().objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
			tempEo.invalidateAllChildren();

        }
		*/
		invalidateObjects(customerIssueDisplayGroup.allObjects());
    }
	public boolean isP1() {
		return (((String)anIssue.valueForKey("priority")).equals("1 - Urgent"))?true:false;
	}

	
    public WOComponent goUpdate()
    {
		invalidateChildren();
		initCustomerIssues();
		itemsWithMultipleParents = null;
        return null;
    }
	

    public WOComponent goIssueView()
    {
		setIsCustomerView(false);
		showProductSelector = false;
		setBugId("" + anItem.bugId());
		customerIssues = null;

        return null;
    }
    public WOComponent goCustomerView()
    {
		setIsCustomerView(true);
		showProductSelector = true;

        return null;
    }

}