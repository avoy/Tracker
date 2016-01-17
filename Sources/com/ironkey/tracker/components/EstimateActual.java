package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

import java.util.Enumeration;

public class EstimateActual extends WOComponent {
	private static final long serialVersionUID = 1L;	
	
	/** @TypeInfo Item */
    protected EOEnterpriseObject anEo;
    protected BugStat aBugStat;
    protected BugStat selectedBugStat;
    protected WODisplayGroup displayGroup;
    protected NSMutableArray bugStats;
    protected NSMutableArray distinctRows;
    protected NSMutableArray distinctColumns;
    protected boolean isSelected = false;
    protected boolean showLinks = true;
    
    public EstimateActual(WOContext aContext) {
        super(aContext);

        bugStats = new NSMutableArray();
    }
	
	

    public WODisplayGroup displayGroup() {

        return displayGroup;
    }
    public void setDisplayGroup(WODisplayGroup theDG) {
        displayGroup = theDG;
        setDisplayAttributes();
        CreateStatsFromBugs();
        sortByEstimate();
    }
    public void setDisplayAttributes(){
        Object orderings[]={EOSortOrdering.sortOrderingWithKey( "assignee.realname", EOSortOrdering.CompareCaseInsensitiveAscending)};

        displayGroup.setSortOrderings(new NSArray(orderings));
        displayGroup.setNumberOfObjectsPerBatch(displayGroup.allObjects().count());
        displayGroup.updateDisplayedObjects();
    }

    // 1 - make a copy of the original array into a temp array
    // 2 - search through temp array for the 'highest' estimate, remove it from the temp array.
    // 3 - add it to the front of a sorted array
    // 4 - reset the enumereration to use the new 'smaller' temp array and repeat the process.
    // This will result in the sorted array being sorted as highest to smallest.
    public void sortByEstimate(){
        //--System.out.println("EstimateActual.sortByEstimate()");

        NSMutableArray tempArray = new NSMutableArray(bugStats);
        NSMutableArray sort = new NSMutableArray();
        Enumeration enumer;
        BugStat currentHighest = null;
        BugStat tempBugStat;
        double currentHighestValue = 0.0;
        int numElements;

        numElements = tempArray.count();  // number of elements in the original array.
        for(int i = 0; i < numElements; i++) {  // Need to sort all elements
            // need to sort the bugStats by highest estimate at the top.

            // Iterate through all of the elements and get the highest
            enumer = tempArray.objectEnumerator();
            while(enumer.hasMoreElements()) {
                tempBugStat = (BugStat)enumer.nextElement();

                // Find the highest estimate
                if(tempBugStat.estimate() >= currentHighestValue) {
                    currentHighestValue = tempBugStat.estimate();
                    currentHighest = tempBugStat;
                }
            }
            if(currentHighest != null) {
                tempArray.removeObject(currentHighest);  // remove it from the temp array, so we can process the rest
            }

            sort.addObject(currentHighest);  // Add to the end of the array
            currentHighestValue = 0.0;  // reset for next pass through
            // Can't remove the last item
            if(tempArray.count() == 0) {
                break;
            }
        }

        bugStats = new NSMutableArray(sort);
    }

    //The display group is presorted for row label (assignee)
    //1 bugstat per row label (assignee)
    //once the bugstat is set, iterate through the bugs until the row label (assignee) value changes (e.g. 'Anson Mah', then 'Satnam Alag')
    // - keep adding bugs to the current bugStat.
    // This could be changed to use a dictionary and then they wouldn't need to be presorted,
    public void CreateStatsFromBugs() {
        EOEnterpriseObject eo;
        EOEnterpriseObject assigneeEo;
        String assignee;
        BugStat tempBugStat = null;
        distinctRows = new NSMutableArray();
        distinctColumns = new NSMutableArray();
        distinctColumns.addObject("Count");
        bugStats = new NSMutableArray();

        Enumeration enumer = displayGroup().displayedObjects().objectEnumerator();
        while(enumer.hasMoreElements()) {
            eo = (EOEnterpriseObject)enumer.nextElement();
            assignee = (String)eo.valueForKeyPath("assignee.realname");
            if(assignee == null) {
                assignee = "Unknown";
            }
            // set the current bugStat
            if(distinctRows.containsObject(assignee) == false) {

                distinctRows.addObject(assignee);
                tempBugStat = new BugStat();
                bugStats.addObject(tempBugStat);
                tempBugStat.setLabel(assignee);
                tempBugStat.setSortKeys(distinctColumns);
            }
            else {
                // This may not be necessary as tempBugStat is defined during the first pass above
                // Note: The list of bugs in sorted by assignee.
                tempBugStat = getBugStatFromArray(assignee);
            }

            // add bugs to the current bugStat until the assignee changes.
            tempBugStat.addBugWithKey(eo,"Count" );
        }
    }

    public BugStat getBugStatFromArray(String pLabel) {
        BugStat returnVal = null;
        Enumeration enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat temp = (BugStat)enumer.nextElement();
            if(temp.label().equals(pLabel)) {
                returnVal = temp;
                break;
            }
        }
        return returnVal;
    }
    public double estimateTotal() {
        double returnVal = 0.0;
        Enumeration enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat temp = (BugStat)enumer.nextElement();
            returnVal += temp.estimate();
        }
        return  returnVal;
    }
    public double actualTotal() {
        double returnVal = 0.0;
        Enumeration enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat temp = (BugStat)enumer.nextElement();
            returnVal += temp.actual();
        }
        return returnVal;
    }
    public double countTotal() {
        int returnVal = 0;
        Enumeration enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat temp = (BugStat)enumer.nextElement();
            returnVal += temp.allBugs.count();
        }
        return returnVal;
    }
    public double noEstTotal() {
        int returnVal = 0;
        Enumeration enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat temp = (BugStat)enumer.nextElement();
            returnVal += temp.numNotEstimated;
        }
        return returnVal;
    }
    public double noActTotal() {
        int returnVal = 0;
        Enumeration enumer = bugStats.objectEnumerator();
        while(enumer.hasMoreElements()) {
            BugStat temp = (BugStat)enumer.nextElement();
            returnVal += temp.numNotActual;
        }
        return returnVal;
    }
    public WOComponent viewBugsForSelected() {
        setIsSelected(true);
        setSelectedBugStat(aBugStat);
        return null;
    }
    public WOComponent viewAllBugs() {
        setIsSelected(false);
        return null;
    }

    public String bugURL() {
    	String URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/show_bug.cgi?id=" + anEo.valueForKey("bugId");
        return URL;
     }
    public String bugListURL() {
        String URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" + aBugStat.bugNumbersForAll();

        return URL;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public void setIsSelected(boolean pVal) {
        isSelected = pVal;
    }
    public boolean showLinks() {
        return showLinks;
    }
    public void setShowLinks(boolean pVal) {
        showLinks = pVal;
    }

    public BugStat selectedBugStat() {
        return selectedBugStat;
    }
    public void setSelectedBugStat(BugStat pVal) {
        selectedBugStat = pVal;
    }


}
