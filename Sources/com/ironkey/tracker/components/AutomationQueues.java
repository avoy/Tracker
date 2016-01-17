package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.net.URL;


public class AutomationQueues extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup testDevDisplayGroup;
    public NSArray<EOEnterpriseObject> testDevResolvedItems;
    public WODisplayGroup testExecutionDisplayGroup;
    public NSArray<EOEnterpriseObject> testExecutionResolvedItems;
    public WODisplayGroup labDisplayGroup;
    public NSArray<EOEnterpriseObject> labResolvedItems;
    public NSData dataForCheckboxOff;
    public NSData dataForCheckboxOn;
	public boolean emailFlag = false;
	/** @TypeInfo Item */
	public Item anItem;
	public NSTimestamp today;
	//protected NSArray supportQueue;
	public NSArray<String> potentialQueues= new NSArray<String>(new String[] {"Test Development", "Test Execution", "Lab"});
	public String aQueue;
	public NSArray<String> selectedQueues = new NSArray<String>(new String[] {"Test Development", "Test Execution", "Lab"});
	
    public AutomationQueues(WOContext context) {
        super(context);
		today = new NSTimestamp();
		initTestDevelopment();
		initTestExecution();
		initLab();
    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	public boolean isTestDev() {
		return (selectedQueues.contains("Test Development"));
	}
	public boolean isTestExecution() {
		return (selectedQueues.contains("Test Execution"));
	}
	public boolean isLab() {
		return (selectedQueues.contains("Lab"));
	}

	public void initTestDevelopment() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();

		// 'Open items'
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// Tools ->Test Dev			
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '_Tools'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName = 'Automation Test Development'", null));


		EOSortOrdering orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray<EOSortOrdering>(orderings));
		fs.setRefreshesRefetchedObjects(true);
		
		((EODatabaseDataSource)testDevDisplayGroup.dataSource()).setFetchSpecification(fs);
		testDevDisplayGroup.fetch();
    }
	public NSArray<EOEnterpriseObject> testDevResolvedItems() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();

		if(testDevResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

			// Tools ->Test Dev			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '_Tools'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName = 'Automation Test Development'", null));

			EOSortOrdering orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray<EOSortOrdering>(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			testDevResolvedItems = (NSArray<EOEnterpriseObject>)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return testDevResolvedItems;
    }
	public void initTestExecution() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();

		// 'Open items'
		
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// Tools ->Test Execution			
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '_Tools'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName = 'Automation Test Execution'", null));

		EOSortOrdering orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray<EOSortOrdering>(orderings));
		fs.setRefreshesRefetchedObjects(true);
		((EODatabaseDataSource)testExecutionDisplayGroup.dataSource()).setFetchSpecification(fs);
		testExecutionDisplayGroup.fetch();
    }

	public NSArray<EOEnterpriseObject> testExecutionResolvedItems() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();


		if(testExecutionResolvedItems == null) {
			// 'Resolves items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));


			// Tools ->Test Execution			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '_Tools'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName = 'Automation Test Execution'", null));

			Object orderings[]={			
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			testExecutionResolvedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return testExecutionResolvedItems;
    }
	public void initLab() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();

		// 'Open items'
		
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// Tools ->Test Lab			
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '_Tools'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName = 'Automation Test Lab'", null));

		EOSortOrdering orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray<EOSortOrdering>(orderings));
		fs.setRefreshesRefetchedObjects(true);
		((EODatabaseDataSource)labDisplayGroup.dataSource()).setFetchSpecification(fs);
		labDisplayGroup.fetch();
    }

	public NSArray<EOEnterpriseObject> labResolvedItems() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();

		if(labResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));

			// Tools ->Test Lab			
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '_Tools'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName = 'Automation Test Lab'", null));
			

			EOSortOrdering orderings[]={			
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray<EOSortOrdering>(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			labResolvedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return labResolvedItems;
    }
	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = true;
	}
	public NSArray<String> selectedQueues() {
	  return selectedQueues;
	}
	
	public void setSelectedQueues(NSArray<String> pVal) {
	    selectedQueues = pVal;
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
    public WOComponent goUpdate()
    {
		// need to clean this up
		initTestDevelopment();
		initTestExecution();
		initLab();
		testDevResolvedItems = null; 
		testDevResolvedItems();
		testExecutionResolvedItems = null; 
		testExecutionResolvedItems();
		labResolvedItems = null; 
		labResolvedItems();
		//System.out.println("selectedQueues - " + selectedQueues);
        return null;
    }

}