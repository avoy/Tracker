package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.net.URL;

public class TopSustainingList extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup supportDisplayGroup;
    public NSArray<EOEnterpriseObject> supportResolvedItems;
    public WODisplayGroup opsDisplayGroup;
    public NSArray<EOEnterpriseObject> opsResolvedItems;
    public WODisplayGroup mfgDisplayGroup;
    public NSArray<EOEnterpriseObject> mfgResolvedItems;
    public NSData dataForCheckboxOff;
    public NSData dataForCheckboxOn;

	public boolean emailFlag = false;
	public Item anItem;
	public NSTimestamp today;
	//protected NSArray supportQueue;
	public NSArray potentialQueues= new NSArray(new Object[] {"Support", "Ops", "MFG"});
	public String aQueue;
	public NSArray selectedQueues = new NSArray(new Object[] {"Support", "Ops", "MFG"});

	
	public boolean isSupport() {
		return (selectedQueues.contains("Support"));
	}
	public boolean isOps() {
		return (selectedQueues.contains("Ops"));
	}
	public boolean isMfg() {
		return (selectedQueues.contains("MFG"));
	}
	
    public TopSustainingList(WOContext context) {
        super(context);
		today = new NSTimestamp();
		initSupport();
		initOps();
		initMfg();
    }
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	public void initSupport() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		// 'Open items'
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// 'Old style escalations'			
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("assignee.loginName like '*escalation*'", null));
		//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("reporterId = 108", null));

		// 'top support'			
		//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like '*top-support*'", null));
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'top-support'", null));

		// 'new style support escalations'					
		//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like '*support_escalation*'", null));
		qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));

		qual.addObject(new EOOrQualifier(qual1));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setUsesDistinct(true);
		fs.setRefreshesRefetchedObjects(true);
		
		((EODatabaseDataSource)supportDisplayGroup.dataSource()).setFetchSpecification(fs);
		supportDisplayGroup.fetch();
    }
	public NSArray supportResolvedItems() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		if(supportResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));

			// 'Old style escalations'			
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("assignee.loginName like '*escalation*'", null));
			//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("reporterId = 108", null));

			// 'top support'			
			//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like '*top-support*'", null));
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'top-support'", null));

			// 'new style support escalations'					
			//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords like '*support_escalation*'", null));
			qual1.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));
			qual.addObject(new EOOrQualifier(qual1));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setUsesDistinct(true);
			fs.setRefreshesRefetchedObjects(true);
			
			supportResolvedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return supportResolvedItems;
    }
	public void initOps() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		// 'Open items'
		
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// 'top ops'			
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'ops'", null));
		qual.addObject(new EOOrQualifier(qual1));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setUsesDistinct(true);
		fs.setRefreshesRefetchedObjects(true);
		((EODatabaseDataSource)opsDisplayGroup.dataSource()).setFetchSpecification(fs);
		opsDisplayGroup.fetch();
    }

	public NSArray opsResolvedItems() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		if(opsResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));


			// 'Ops'			
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'ops'", null));
			
			// 'new style support escalations'					
			qual.addObject(new EOOrQualifier(qual1));

			Object orderings[]={			
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setUsesDistinct(true);
			fs.setRefreshesRefetchedObjects(true);
			
			opsResolvedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return opsResolvedItems;
    }
	public void initMfg() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		// 'Open items'
		
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

		// 'top mfg'			
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'mfg'", null));
		qual.addObject(new EOOrQualifier(qual1));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setUsesDistinct(true);
		fs.setRefreshesRefetchedObjects(true);
		((EODatabaseDataSource)mfgDisplayGroup.dataSource()).setFetchSpecification(fs);
		mfgDisplayGroup.fetch();
    }

	public NSArray mfgResolvedItems() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		if(mfgResolvedItems == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));


			// 'Mfg'			
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'mfg'", null));
			
			// 'new style support escalations'					
			qual.addObject(new EOOrQualifier(qual1));

			Object orderings[]={			
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setUsesDistinct(true);
			fs.setRefreshesRefetchedObjects(true);
			
			mfgResolvedItems = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}
		return mfgResolvedItems;
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
		initSupport();
		initOps();
		supportResolvedItems = null; 
		supportResolvedItems();
		opsResolvedItems = null; 
		opsResolvedItems();
		//System.out.println("selectedQueues - " + selectedQueues);
        return null;
    }

}