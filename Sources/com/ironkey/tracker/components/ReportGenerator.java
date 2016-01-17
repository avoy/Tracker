package com.ironkey.tracker.components;


import com.ironkey.tracker.*;
import com.ironkey.tracker.BugStat;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

public class ReportGenerator extends WOComponent {
	private static final long serialVersionUID = 1L;
	public NSArray<String> rowsAxisList;
	public NSArray<String> columnsAxisList;
    public WODisplayGroup displayGroup;
    public String aRow;
    public String aColumn;
    public String selectedRow = "Component";
    public String actualRow;
    public String sortRow;
    public String displayRow;
    public String selectedColumn = "Status";
    public String actualColumn;
    public String sortColumn;
    public String displayColumn;
    public NSDictionary actualKeys;
    public NSDictionary sortKeys;
    public NSDictionary displayKeys;
    public EOEnterpriseObject anEo;
    public NSMutableArray<Object> distinctRows;
    public NSMutableArray<Object> distinctColumns;
    public String aString;
    public Integer aValue;
    public NSMutableArray<BugStat> bugStats;
    public NSMutableDictionary columnBugs;
    public BugStat aBugStat;
    public boolean showReport;
    public boolean hideReportSpecifier =false;
    public boolean hideGraphLink =false;
    public int index;
    public boolean needUpdate = true;
    public boolean isError;
    public String errorMessage;
    public boolean showContent = false;
    public boolean resubmit = false;

    public void awake() {
		//System.out.println("ReportGenerator.awake()");
	
    }
    public void sleep() {
		//System.out.println("ReportGenerator.sleep()");

    }

    public void appendToResponse(WOResponse r, WOContext c) {
		//System.out.println("ReportGenerator.appendToResponse()");
		if(needUpdate == true) {
			processBugs();
			needUpdate = false;

		}
		super.appendToResponse(r, c);
    }


    public ReportGenerator(WOContext aContext) {
        super(aContext);
		//System.out.println("ReportGenerator.ReportGenerator()");
        
        // Display keys for rows
        Object rows[] =  {"Status", "Milestone", "Product", "Priority","Severity", "Resolution", "Reporter", "QA Contact","Assignee", "Component", "Version", "Keywords", "Type", "Category", "Hardware"};
        rowsAxisList = new NSArray(rows);

        // Display keys for columns
        Object columns[] = {"Hardware", "Category", "Type", "Keywords","Version","Component","Assignee", "QA Contact", "Reporter","Resolution","Severity","Priority", "Product", "Milestone", "Status"};
        columnsAxisList = new NSArray(columns);

        // Mapping for row display keys to db column names
        Object values[] = {"bugStatus","targetMilestone", "product", "priority","bugSeverity", "resolution","reporter", "qaContact","assignee", "component", "version", "keywords", "type", "category", "repPlatform"};
        actualKeys = new NSDictionary(values,rows);
        
        // Mapping for row display keys are a db column to be used for sorting
        Object sort[] = {"bugStatus","targetMilestone","product.productName", "priority","bugSeverity", "resolution","reporter.realname", "qaContact.realname","assignee.realname", "component", "version", "keywords.keywordName", "type", "category", "repPlatform"};
        sortKeys = new NSDictionary(sort, values);

       //Object display[] = {"bugStatus","targetMilestone", "productName", "priority","bugSeverity", "resolution","realname","realname","realname", "componentName", "version", "keywords", "type", "category", "repPlatform"};
        Object display[] = {"bugStatus","targetMilestone", "productName", "priority","bugSeverity", "resolution","realname","realname","realname", "componentName", "version", "keywordName", "type", "category", "repPlatform"};

        displayKeys = new NSDictionary(display, values);
    }

    public WODisplayGroup displayGroup() {
        return displayGroup;
    }
	
	
	public void setDisplayGroup(WODisplayGroup theDG) {
        displayGroup = theDG;
		if((displayGroup != null) && (displayGroup.allObjects().count() > 0)) {
		    needUpdate = true;
		}
    }
	

    public void processBugs() {
		//System.out.println("ReportGenerator.processBugs()");
    	
        bugStats = new NSMutableArray<BugStat>();
        isError = false;
        errorMessage = "";
        if((rowsAxisList.containsObject(selectedRow)) && (rowsAxisList.containsObject(selectedColumn))) {

            // from value that user selected, return actual key
            actualRow = (String)actualKeys.valueForKey(selectedRow);
            actualColumn = (String)actualKeys.valueForKey(selectedColumn);

            // from actual key to sort key
            sortRow = (String)sortKeys.valueForKey(actualRow); 
            sortColumn = (String)sortKeys.valueForKey(actualColumn);

            // from actual key to display key
            displayRow = (String)displayKeys.valueForKey(actualRow);
            displayColumn = (String)displayKeys.valueForKey(actualColumn);

            // Set sort order
            EOSortOrdering orderings[]={EOSortOrdering.sortOrderingWithKey( sortRow, EOSortOrdering.CompareCaseInsensitiveAscending), EOSortOrdering.sortOrderingWithKey(sortColumn, EOSortOrdering.CompareCaseInsensitiveAscending)};
            displayGroup.setSortOrderings(new NSArray<EOSortOrdering>(orderings));

            // Ensure all object are used
            displayGroup.setNumberOfObjectsPerBatch(displayGroup.allObjects().count());
			displayGroup.updateDisplayedObjects();
			
            //  Process all the bugs in the displayGroup
            sortIt();
        }
        else {
            isError = true;
            errorMessage = "Invalid 'row' or 'column' key";
        }
        setShowReport(true);
    }

    /*
     @Description :  	BugStat objects are created for each unique row value
     //			For example:
     //			The user selected 'State' as the row and 'Priority' as the column
     //			 We iterate through all the bugs
     //			   - take the value for the row (in this case it may be 'NEW','ASSIGNED', etc
     //			   - the initial time we get a specific value - a new BugStat is created.
     //				- depending on the Column type that is selected the bug is added to the BugStat
     //                         - each bug stat has a dictionary for each column
     //   		Result BugStat1='NEW'  - ['P1', (12345, 11111, 22222)],['P2',(112233, 334455, 556677)]
     //				BugStat2='ASSIGNED  - ['P1', (2222,33333,45678)], ['P2', (76543, 12334, 54432)] 
     */
    public void sortIt() {
        BugStat currBugStat = null;
        distinctRows = new NSMutableArray();
        distinctColumns = new NSMutableArray();
        columnBugs = new NSMutableDictionary();
        Enumeration enumer = displayGroup.displayedObjects().objectEnumerator();  // using displayedObjects retains sort order

        while (enumer.hasMoreElements()) {
            EOEnterpriseObject bug = (EOEnterpriseObject)enumer.nextElement();
            //System.out.println("bugId - " + (Number)bug.valueForKey("bugId") + ", ");
            //System.out.println("actualRow - " + actualRow);
            // Get the value e.g. user selected 'State' then this may be 'NEW', 'ASSIGNED', etc.
            //   - if this user selected 'Assignee' - then this may be '{eo-kevin}', '{eo-anson}'
            Object rowObj = (Object)bug.valueForKey(actualRow);
			//System.out.println("rowObj.getClass().getName() - " + rowObj.getClass().getName());
			//System.out.println("rowObj - " + rowObj);
            if(rowObj != null) {
                // Need to see if it is a String or an EO
                //System.out.println("rowObj.getClass().getName() - " + rowObj.getClass().getName());
				String className = rowObj.getClass().getName();
                //if((className.equals("com.webobjects.eocontrol.EOGenericRecord")) || (className.equals("er.extensions.ERXGenericRecord"))) {
                if(className.equals("com.webobjects.eocontrol.EOGenericRecord")) {

                    EOEnterpriseObject rowEO = (EOEnterpriseObject)rowObj;
                    if(distinctRows.containsObject((String)rowEO.valueForKey(displayRow)) == false) {

                        if((String)rowEO.valueForKey(displayRow) != null) { // Note: if a person is disabled, they will become null as we filter out all disabled employees in eomodeler

                            distinctRows.addObject((String)rowEO.valueForKey(displayRow));
                            currBugStat = new BugStat();
                            bugStats.addObject(currBugStat);
                            currBugStat.setLabel((String)rowEO.valueForKey(displayRow));
                        }
                        else {
                            if(distinctRows.containsObject("Unknown") == false) {
                                distinctRows.addObject("Unknown");
                                currBugStat = new BugStat();
                                bugStats.addObject(currBugStat);
                                currBugStat.setLabel("Unknown");
                            }
                            else {
                                if(!currBugStat.label().equals("Unknown")) {
                                    currBugStat = bugStatForLabel("Unknown");
                                }
                            }
                        }
                    }
                }
                // String
                else if(rowObj.getClass().getName().equals("java.lang.String")) {

                    if(distinctRows.containsObject(rowObj) == false) {
                        distinctRows.addObject(rowObj);
                        currBugStat = new BugStat();
                        bugStats.addObject(currBugStat);
                        currBugStat.setLabel((String)rowObj);
                    }
                }
                // _EOCheapCopyMutableArray
                else if(rowObj.getClass().getName().equals("com.webobjects.eocontrol._EOCheapCopyMutableArray")) {
					
					if(actualRow.equals("keywords")) {
						String allKeywords = "";
						int numKeywords = ((_EOCheapCopyMutableArray)rowObj).count();
						int count = 0;
						Enumeration enumer2 = ((_EOCheapCopyMutableArray)rowObj).objectEnumerator();
						while(enumer2.hasMoreElements()) {
							count++;
							EOEnterpriseObject temp = (EOEnterpriseObject)enumer2.nextElement();
							allKeywords += (String)temp.valueForKey("keywordName");
							if(count<numKeywords) {
								allKeywords += ", ";
							}
							
						}
						if(numKeywords == 0) {
							allKeywords = "Unknown";
						}

						//System.out.println("allKeywords -'" + allKeywords + "'");
						if(distinctRows.containsObject(allKeywords) == false) {
							distinctRows.addObject(allKeywords);
							currBugStat = new BugStat();
							bugStats.addObject(currBugStat);
							currBugStat.setLabel((String)allKeywords);
						}
					}
                }
            }
            else  {
                if( distinctRows.containsObject("Unknown") == false) {
                    distinctRows.addObject("Unknown");
                    currBugStat = new BugStat();
                    bugStats.addObject(currBugStat);
                    currBugStat.setLabel("Unknown");
                }
            }

            Object columnObj = (Object)bug.valueForKey(actualColumn);
            if(columnObj != null) {
               //System.out.println("columnObj.getClass().getName() - " + columnObj.getClass().getName());
				String className = columnObj.getClass().getName();
               // if((className.equals("com.webobjects.eocontrol.EOGenericRecord")) || (className.equals("er.extensions.ERXGenericRecord"))) {
                if(className.equals("com.webobjects.eocontrol.EOGenericRecord")) {
                    EOEnterpriseObject columnEO = (EOEnterpriseObject)columnObj;
                    String value = (String)columnEO.valueForKey(displayColumn);
                    if(value == null) {
                        value = "Unknown";
                    }
                    if(distinctColumns.containsObject(value) == false) {
                        distinctColumns.addObject(value);
                    }

                    currBugStat.addBugWithKey(bug,value );
                    addBugForColumn(bug,value);
                }
                else if(columnObj.getClass().getName().equals("java.lang.String")) {
				
                    if(distinctColumns.containsObject((String)columnObj) == false) {
                        distinctColumns.addObject((String)columnObj);
                    }
					//System.out.println("columnObj - " + columnObj);
					//System.out.println("currBugStat - " + currBugStat);
                    currBugStat.addBugWithKey(bug,(String)columnObj );
                    addBugForColumn(bug,(String)columnObj );
                }
                // _EOCheapCopyMutableArray
                else if(columnObj.getClass().getName().equals("com.webobjects.eocontrol._EOCheapCopyMutableArray")) {
					//System.out.println("columnObj - " + columnObj);
					//if(actualRow.equals("keywords")) {
						String allKeywords = "";
						int numKeywords = ((_EOCheapCopyMutableArray)columnObj).count();
						int count = 0;
						Enumeration enumer2 = ((_EOCheapCopyMutableArray)columnObj).objectEnumerator();
						while(enumer2.hasMoreElements()) {
							count++;
							EOEnterpriseObject temp = (EOEnterpriseObject)enumer2.nextElement();
							allKeywords += (String)temp.valueForKey("keywordName");
							if(count<numKeywords) {
								allKeywords += ", ";
							}
							
						}
						if(numKeywords == 0) {
							allKeywords = "Unknown";
						}

						//System.out.println("allKeywords -'" + allKeywords + "'");
						if(distinctColumns.containsObject(allKeywords) == false) {
							distinctColumns.addObject(allKeywords);
							addBugForColumn(bug,allKeywords);
						}
					//}					
				}
            }
            else {
                if (distinctColumns.containsObject("Unknown" ) == false) {
                    distinctColumns.addObject("Unknown");
                }
                currBugStat.addBugWithKey(bug,"Unknown");
                addBugForColumn(bug,"Unknown" );
            }
        }
        distinctColumns = sortArray(distinctColumns);
        addSortKeys();
    }
    
    public void addSortKeys() {

        Enumeration<BugStat> enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat bs = (BugStat)enumer.nextElement();
            bs.setSortKeys(distinctColumns);
        }
    }

    public NSMutableArray<Object> sortArray(NSMutableArray<Object> pUnsorted) {
        NSMutableArray<Object> sortedArray = new NSMutableArray<Object>();
        NSMutableArray<Object> copyOrig = new NSMutableArray<Object>(pUnsorted);
        int numItems;
        String tempLowest = null;
        String tempString;

        // if unsorted contains any in predefined array, use the predefined sorting.
        NSArray<Object> stateValues= new NSArray<Object>(new String[] {"NEW", "ASSIGNED", "REOPENED", "RESOLVED", "VERIFIED", "CLOSED"});
       // NSArray milestoneValues= new NSArray(new Object[] {"---","M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "CCH", "CC", "RC1", "RTP", "patch-1","patch-2","patch-3","patch-4","patch-5","patch-6","patch-7","patch-8","patch-9","patch-10","patch-11","patch-12","hotfix","datafix"});


        if(stateValues.firstObjectCommonWithArray(pUnsorted) != null) {
            sortedArray = sortItemsAsDefined(pUnsorted, stateValues);
        }
       // else if(milestoneValues.firstObjectCommonWithArray(pUnsorted) != null) {
       //     sortedArray = sortItemsAsDefined(pUnsorted, milestoneValues);
       // }
        else {  // Sort alphabetically
            numItems = pUnsorted.count();
            for(int i = 0; i<numItems; i++) {

                Enumeration<Object> enumer2 = copyOrig.objectEnumerator();
                while(enumer2.hasMoreElements()) {
                    tempString = (String)enumer2.nextElement();
                    if(tempLowest == null) {
                        tempLowest = tempString;  // initialize
                    }
                    else {
                        tempLowest = lower(tempString,tempLowest);  // initialize

                    }
                }
                copyOrig.removeObject(tempLowest);
                sortedArray.addObject(tempLowest);
                tempLowest = null;
            }
        }
        return sortedArray;
    }

    public NSMutableArray<Object> sortItemsAsDefined(NSArray<Object> unorderedArray, NSArray<Object> orderingDefinitionArray) {
        NSMutableArray<Object> sortedArray = new NSMutableArray<Object>();
        Enumeration<Object> enumer = orderingDefinitionArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            Object value = (Object)enumer.nextElement();
            if(unorderedArray.containsObject(value)) {
                sortedArray.addObject(value);
            }
        }
        return sortedArray;
    }
    private String lower(String str1, String str2) {
        return (str1.compareTo(str2) <0 ? str1 : str2);
    }
    public void setShowReport(boolean pValue) {
        showReport = pValue;
    }
    public boolean showReport() {
        return showReport;
    }
    public void setBugStats(NSMutableArray<BugStat> pValue) {
        bugStats = pValue;
    }
    public NSMutableArray<BugStat> bugStats() {
        return bugStats;
    }
    public BugStat bugStatForLabel(String pLabel) {

        BugStat returnVal = null;
        BugStat tempBugStat = null;
        Enumeration<BugStat> enumer = bugStats().objectEnumerator();
        while(enumer.hasMoreElements()) {
            tempBugStat = (BugStat)enumer.nextElement();
            if(tempBugStat.label().equals(pLabel)) {
                returnVal = tempBugStat;
                break;
            }
        }
        return returnVal;
    }
    public void setDistinctColumns(NSMutableArray<Object> pValue) {
        distinctColumns = pValue;
    }
    public NSMutableArray<Object> distinctColumns() {
        return distinctColumns;
    }
    public boolean hideReportSpecifier() {
        return hideReportSpecifier;
    }
    public void setHideReportSpecifier(boolean pValue) {
        hideReportSpecifier = pValue;
    }


    public String displayBugs() {
        String URL = "";
        String theKey = (String)distinctColumns().objectAtIndex(index);
		URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" +aBugStat.bugNumbersForKey(theKey) ;
        return URL;
    }

    public String displayAllBugs() {
        String URL = "";
        if(aBugStat != null) {
            URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" +aBugStat.bugNumbersForAll() ;
        }
        return URL;
    }
	
    public String urlForColumnBugs() {
        return urlForBugs(columnForKey());
    }
	
    public String urlForAll() {
        return urlForBugs(displayGroup().allObjects());
    }
	public String urlForRow() {
        return urlForBugs(aBugStat.allBugs());
    }

    public String urlForBugs(NSArray<EOEnterpriseObject> bugs) {
        String bugNumbers = "";
        String URL = "#";
        Enumeration<EOEnterpriseObject> enumer;
        EOEnterpriseObject bug;
        
        NSArray<EOEnterpriseObject> columnBugs = bugs;
        if((columnBugs != null) && (columnBugs.count() > 0)) {
            enumer = columnBugs.objectEnumerator();
            while(enumer.hasMoreElements()) {
                bug = (EOEnterpriseObject)enumer.nextElement();
                bugNumbers += (Integer)bug.valueForKey("bugId") + ",";
            }
            URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" +bugNumbers;
        }
        return URL;
    }
    
    public void addBugForColumn(EOEnterpriseObject pBug, String pKey) {
        NSMutableArray<EOEnterpriseObject> columnArray;
        columnArray = (NSMutableArray<EOEnterpriseObject>)columnBugs.objectForKey(pKey);
        if(columnArray == null) {
            columnArray = new NSMutableArray<EOEnterpriseObject>();
            columnBugs.setObjectForKey(columnArray, pKey);
        }
        columnArray.addObject(pBug);
    }
    public NSMutableArray<EOEnterpriseObject> columnForKey() {
        return (NSMutableArray<EOEnterpriseObject>)columnBugs.objectForKey(aColumn);
    }
    public int columnCount() {
        int returnVal = 0;
        if((NSMutableArray<EOEnterpriseObject>)columnBugs.objectForKey(aColumn) != null) {
            returnVal = ((NSMutableArray<EOEnterpriseObject>)columnBugs.objectForKey(aColumn)).count();
        }
        return returnVal;
        
    }
    public boolean hideGraphLink() {
        return hideGraphLink;
    }
    public void setHideGraphLink(boolean pValue) {
        hideGraphLink = pValue;
    }

    public boolean displayHyperlink() {
        return (aValue.intValue() > 0 ) ? true : false;
    }
	
    public WOComponent createReport() {
        processBugs();
		setResubmit(true);
        return null;
    }
	
	public boolean resubmit() {
		return resubmit;
	}
	public void setResubmit(boolean pVal) {
		resubmit = pVal;
	}
	
}