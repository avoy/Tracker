package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.math.BigDecimal;

public class StatusItem {
    protected String displayName;
    protected NSMutableDictionary openItems;
    protected NSMutableDictionary resolvedItems;
    protected NSMutableDictionary closedItems;
    protected String currentKey;
    protected NSMutableDictionary daysForMilestone;
    protected int numOpenItems = 0;
    protected int numResolvedItems = 0;
    public int numClosedItems = 0;
    protected int origEstimateHoursAll = -1;
    protected int currentEstimateHoursAll = -1;
    protected int remainingHoursOpen = -1;
    protected int hoursWorkedAll = -1;
    //protected double numOpenHours = 0.0;
    //protected double numClosedHours = 0.0;
    protected NSMutableDictionary openEstimated;
    protected NSMutableArray sortedMilestones;
    protected NSMutableDictionary openEstimatesForPeopleForMilestone; //  m1 = ([anson,5], [gautam,7], ...) m2 = ([anson,6], [gautam,8])
    protected NSMutableDictionary totalHoursForPeopleForMilestone; //  m1 = ([anson,5], [gautam,7], ...) m2 = ([anson,11], [gautam,15])
    public int numNotEstimated = -1;
    
    // Note: Items will be grouped in two ways:
    // a) open/closed
    // b) by some attribute, e.g. target milestone (within the openItems and closedItems)
    public StatusItem() {
        //System.out.println("StatusItem.StatusItem()");
        openItems = new NSMutableDictionary();
        resolvedItems = new NSMutableDictionary();
        closedItems = new NSMutableDictionary();
        openEstimated = new NSMutableDictionary();
    }
	
	
    public StatusItem(String pDisplayName) {
        //System.out.println("StatusItem.StatusItem(pDisplayName)");

        setDisplayName(pDisplayName);
        openItems = new NSMutableDictionary();
        resolvedItems = new NSMutableDictionary();		
        closedItems = new NSMutableDictionary();
        openEstimated = new NSMutableDictionary();
    }
	public int origEstimateHoursAll() {
		if(origEstimateHoursAll == -1) {
			origEstimateHoursAll = 0;
			Enumeration enumer = allItems().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item eo = (Item)enumer.nextElement();
				origEstimateHoursAll += eo.estimatedTime().intValue();
			}
		}
		return origEstimateHoursAll;
	 } 
    
	public int currentEstimateHoursAll() {
		if(currentEstimateHoursAll == -1) {
			currentEstimateHoursAll = 0;
			Enumeration enumer = allItems().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item eo = (Item)enumer.nextElement();
				currentEstimateHoursAll += eo.currentEstimate();
			}
		}
		return currentEstimateHoursAll;
	 } 
	public int remainingHoursOpen() {
		if(remainingHoursOpen == -1) {
			remainingHoursOpen = 0;
	
			Enumeration enumer = allOpenItems().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item eo = (Item)enumer.nextElement();
				remainingHoursOpen += eo.remainingTime().intValue();
			}
		}
		return remainingHoursOpen;
	 } 
	public int hoursWorkedAll() {
		if(hoursWorkedAll == -1) {
			hoursWorkedAll = 0;
			Enumeration enumer = allItems().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item eo = (Item)enumer.nextElement();
				hoursWorkedAll += eo.numHoursWorked();
			}
		}
		return hoursWorkedAll;
	 } 	 
	
    public NSArray arrayFromDictionary(NSMutableDictionary pDict) {
        String currKey;
        NSMutableArray currArray = new NSMutableArray();

        Enumeration enumer = pDict.keyEnumerator();
        while(enumer.hasMoreElements()){
            currKey = (String)enumer.nextElement();
            currArray.addObjectsFromArray((NSMutableArray)pDict.valueForKey(currKey));
        }
        return new NSArray(currArray);
    }

    public int numNotEstimatedForDictionary(NSMutableDictionary pDict) {
        if(pDict != null) {
            return numNotEstimatedForArray(arrayFromDictionary(pDict));
        }
        else {
            return 0;
        }
    }
    public int numNotEstimatedForArray(NSArray pArray) {
        Enumeration enumer;
        int num = 0;
        if((pArray != null) && (pArray.count() > 0)) {
            enumer = pArray.objectEnumerator();
            while(enumer.hasMoreElements()) {
                Item currObject = (Item)enumer.nextElement();
            //if(((String)currObject.valueForKey("estimate") == null) || (((String)currObject.valueForKey("estimate")).equals("Unknown"))) {
				double currEstimate = ((BigDecimal)currObject.valueForKey("estimatedTime")).doubleValue();
				if( currEstimate == 0.0)  {
                    num++;
                }
            }
        }
        return num;
    }
    public int numNoMilestoneForArray(NSArray pArray) {
        Enumeration enumer;
        int num = 0;
        if((pArray != null) && (pArray.count() > 0)) {
            enumer = pArray.objectEnumerator();
            while(enumer.hasMoreElements()) {
                Item currObject = (Item)enumer.nextElement();
                if(((String)currObject.valueForKey("targetMilestone")).equals("---")) {
                    num++;
                }
            }
        }
        return num;
    }

    public int numNotEstimated() {
        if(numNotEstimated == -1) {
            numNotEstimated = 0;
            numNotEstimated += numNotEstimatedForDictionary(openItems());
            numNotEstimated += numNotEstimatedForDictionary(resolvedItems());
            numNotEstimated += numNotEstimatedForDictionary(closedItems());
        }
        return numNotEstimated;
    }
	

    public void addValuesFromStatusItem(StatusItem pStatus) {
        String currKey;
        NSMutableArray currArray;
        
        //Add iVars
        numOpenItems += pStatus.numOpenItems;
        numResolvedItems += pStatus.numResolvedItems;
        numClosedItems += pStatus.numClosedItems;
       // numOpenHours += pStatus.numOpenHours;
        //numClosedHours += pStatus.numClosedHours;

        //Add OpenItems
        Enumeration enumer = pStatus.openItems().keyEnumerator();
        while(enumer.hasMoreElements()){
            currKey = (String)enumer.nextElement();
            currArray = (NSMutableArray)this.openItems().valueForKey(currKey);
            if(currArray == null) { // if this does not contain the value set it from pStatus
                openItems.setObjectForKey(new NSMutableArray((NSMutableArray)pStatus.openItems().valueForKey(currKey)), currKey);
            }
            else {
                currArray.addObjectsFromArray((NSMutableArray)pStatus.openItems().valueForKey(currKey));
                openItems.setObjectForKey(currArray, currKey);
            }
        }
        //Add ResolvedItems
		enumer = pStatus.resolvedItems().keyEnumerator();
        while(enumer.hasMoreElements()){
            currKey = (String)enumer.nextElement();
            currArray = (NSMutableArray)this.resolvedItems().valueForKey(currKey);
            if(currArray == null) { // if this does not contain the value set it from pStatus
                resolvedItems.setObjectForKey(new NSMutableArray((NSMutableArray)pStatus.resolvedItems().valueForKey(currKey)), currKey);
            }
            else {
                currArray.addObjectsFromArray((NSMutableArray)pStatus.resolvedItems().valueForKey(currKey));
                resolvedItems.setObjectForKey(currArray, currKey);
            }
        }
        //Add ClosedItems
        enumer = pStatus.closedItems().keyEnumerator();
        while(enumer.hasMoreElements()){
            currKey = (String)enumer.nextElement();
            currArray = (NSMutableArray)this.closedItems().valueForKey(currKey);
            if(currArray == null) { // if this does not contain the value set it from pStatus
                closedItems.setObjectForKey(new NSMutableArray((NSMutableArray)pStatus.closedItems().valueForKey(currKey)), currKey);
            }
            else {
                currArray.addObjectsFromArray((NSMutableArray)pStatus.closedItems().valueForKey(currKey));
                closedItems.setObjectForKey(currArray, currKey);
            }
        }
    }

/*
    public int numTotalOpenDaysForMilestone(String pMilestone) {
        //System.out.println("numTotalOpenDaysForMilestone() - " + pMilestone);
        boolean isTotalDaysForMilestoneEstimated = true;
        int numTotal = 0;
        Integer numHours = null ;
        //Integer numHours = (Integer)(daysForMilestone().objectForKey("total-"+ pMilestone));  // This may not work if it changes
        if(numHours == null) {
            //Enumeration enumer = openItems.keyEnumerator();  // all milestones
            //while(enumer.hasMoreElements()) {
            int numMilestones = sortedMilestones().count();
            for(int i = 0; i < numMilestones; i++) {

                String aKey = (String)sortedMilestones().objectAtIndex(i);

                // Add the days
                //numHours = (Integer)(daysForMilestone().objectForKey(aKey));
                numHours = new Integer(numOpenHoursForKey(aKey));
                if(numHours != null) {
                    numTotal += numHours.intValue();
                }

                // check if all estimated
                boolean tempEst =  isOpenEstimatedForKey(aKey);
                if(tempEst == false) {
                    isTotalDaysForMilestoneEstimated = false;
                }
                // Only add the number of days for the current milestone and all previous milestones
                if(aKey.equals(pMilestone)) {
                    break;
                }
            }

            daysForMilestone().setObjectForKey(new Integer(numTotal), "total-"+ pMilestone);
            addOpenEstimated(new Boolean(isTotalDaysForMilestoneEstimated), "total-"+ pMilestone);
        }
        else {
            numTotal = numHours.intValue();
        }
        return numTotal;
    }
    public int numTotalOpenItemsForMilestone(String pMilestone) {
        int numTotal = 0;
        int numMilestones = sortedMilestones().count();
        for(int i = 0; i < numMilestones; i++) {

            String aKey = (String)sortedMilestones().objectAtIndex(i);
            // Add the items
            numTotal += numOpenForKey(aKey);

            // Only add the number of days for the current milestone and all previous milestones
            if(aKey.equals(pMilestone)) {
                break;
            }
        }
        return numTotal;
    }
*/
    public int numOpenHoursForCurrentKey() {
        //System.out.println("StatusItem.numTotalDaysForCurrentKey()");

        return numOpenHoursForKey(currentKey);
    }
    public int numOpenHoursForKey(String pKey) {

        boolean isAllEstimate = true;
        int returnVal = 0;
        double tempSum = 0.0;
        Integer numHours = (Integer)(daysForMilestone().objectForKey(pKey));
        if(numHours != null) {
            returnVal = numHours.intValue();
        }
        else if(pKey != null) {
            NSMutableArray openCI = openForKey(pKey);
            if(openCI != null) {
                Enumeration enumer = openCI.objectEnumerator();
                while(enumer.hasMoreElements()) {
                    Item eo = (Item)enumer.nextElement();					
					double currEstimate = ((BigDecimal)eo.valueForKey("estimatedTime")).doubleValue();
					if(currEstimate == 0.0) {
						isAllEstimate = false;
					}
					tempSum +=currEstimate;
                }
                addOpenEstimated(new Boolean(isAllEstimate),pKey );
                returnVal = (int)tempSum;
                // Round up
                double remainder = tempSum - (double)returnVal;
                if(remainder > 0) {
                    returnVal++;
                }
            }

           daysForMilestone.setObjectForKey(new Integer(returnVal), pKey);
        }
        //System.out.println("numTotalDaysForKey() - "+ displayName + " - " + pKey + " - " +  returnVal + "\n");

        return returnVal;
    }
	
    public int numClosedHoursForCurrentKey() {
        return numClosedHoursForKey(currentKey);
    }

    public int numClosedHoursForKey(String pKey) {

        boolean isAllEstimate = true;
        int returnVal = 0;
        double tempSum = 0.0;
        //Integer numHours = (Integer)(daysForMilestone().objectForKey(pKey));
        Integer numHours = null;
        if(numHours != null) {
            returnVal = numHours.intValue();
        }
        else if(pKey != null) {
            NSMutableArray closedCI = closedForKey(pKey);
            if(closedCI != null) {
                Enumeration enumer = closedCI.objectEnumerator();
                while(enumer.hasMoreElements()) {
                    Item eo = (Item)enumer.nextElement();										
					double currEstimate = ((BigDecimal)eo.valueForKey("estimatedTime")).doubleValue();
					if(currEstimate == 0.0) {
						isAllEstimate = false;
					}
					tempSum +=currEstimate;

                }
                returnVal = (int)tempSum;
                // Round up
                double remainder = tempSum - (double)returnVal;
                if(remainder > 0) {
                    returnVal++;
                }
            }

        }
        //System.out.println("numTotalDaysForKey() - "+ displayName + " - " + pKey + " - " +  returnVal + "\n");

        return returnVal;
    }
    public int numTotalDaysForCurrentKey() {
        return numOpenHoursForCurrentKey() + numClosedHoursForCurrentKey();
    }
    public int numTotalDaysForKey(String pKey) {
        return numOpenHoursForKey(pKey) + numClosedHoursForKey(pKey);
    }

    public NSMutableDictionary daysForMilestone() {
        //System.out.println("StatusItem.daysForMilestone()" );

        if(daysForMilestone == null) {
            daysForMilestone = new NSMutableDictionary();
        }
        
        return daysForMilestone;
    }
    public boolean isTotalDaysForMilestoneEstimated(String pMilestone) {
        boolean returnVal = true;
        int numMilestones = sortedMilestones().count();
        //System.out.println(displayName);
        for(int i = 0; i < numMilestones; i++) {
            String aKey = (String)sortedMilestones().objectAtIndex(i);
            // check if all estimated
            boolean tempEst =  isOpenEstimatedForKey(aKey);
            //System.out.println("isTotalDaysForMilestoneEstimated - aKey - " + aKey  + "=" + tempEst);

            if(tempEst == false) {
                returnVal = false;
                break;
            }
            if(aKey.equals(pMilestone)) {
                break;
            }
        }
       // System.out.println();

        return returnVal;

    }
    public boolean isOpenItemsForMilestoneForTotal(String pMilestone) {
        boolean returnVal = false;
        int numMilestones = sortedMilestones().count();
        //System.out.println(displayName);
        for(int i = 0; i < numMilestones; i++) {
            String aKey = (String)sortedMilestones().objectAtIndex(i);
            // check if all estimated
            returnVal =  (numOpenForKey(aKey)>0); //isOpenForKey() ??
            if( returnVal == true) {
                break;
            }
            if(aKey.equals(pMilestone)) {
                break;
            }
        }
       // System.out.println();

        return returnVal;

    }
    public boolean isNoUndefinedMilestonesForTotalDays(String pMilestone) {
        boolean returnVal = true;
        int numMilestones = sortedMilestones().count();
        //System.out.println(displayName);
        for(int i = 0; i < numMilestones; i++) {
            String aKey = (String)sortedMilestones().objectAtIndex(i);
            // check if all estimated
            boolean tempEst =  isNotEstimatedForKey(aKey);
          //  System.out.println("isNoUndefinedMilestonesForTotalDays - aKey - " + aKey  + "=" + tempEst);

            if(tempEst == false) {
                returnVal = false;
                break;
            }
            if(aKey.equals(pMilestone)) {
                break;
            }
        }
        //System.out.println();

        return returnVal;

    }
    boolean isItemsForCurrentKey() {
        boolean returnVal = false;
        if((numOpenForCurrentKey() > 0) || (numClosedForCurrentKey() > 0)) {
            returnVal = true;
        }
        return returnVal;
    }

    boolean isOpenEstimatedForCurrentKey() {
        //System.out.println("StatusItem.isOpenEstimatedForCurrentKey() - " + currentKey );

        return isOpenEstimatedForKey(currentKey);
    }
    public boolean isOpenEstimatedForKey(String pKey) {
        int count = 0;
        count += numNotEstimatedForArray(openForKey(pKey));

        return count==0?true:false;
    }
    public boolean isNotEstimatedForKey(String pKey) {
        int count = 0;
        count += numNoMilestoneForArray(openForKey(pKey));

        return count==0?true:false;
    }
    public boolean isAllEstimatedForKey(String pKey) {
        int count = 0;
        count += numNotEstimatedForArray(openForKey(pKey));
        count += numNotEstimatedForArray(closedForKey(pKey));
        
        return count==0?true:false;
    }

    public boolean isAllEstimatedForCurrentKey() {
        return isAllEstimatedForKey(currentKey);
    }

    public int numOpenForCurrentKey() {
        //System.out.println("StatusItem.numOpenForCurrentKey() - " + displayName + " - " + currentKey + "/" + numOpenForKey(currentKey) );
        return numOpenForKey(currentKey);
    }
    public boolean isCompleteForCurrentKey() {
        return numOpenForCurrentKey()>0?false:true;
    }

    public int numOpenForKey(String pKey) {
        //System.out.println("StatusItem.numOpenForKey() - " + pKey );

        int returnVal = 0;

        if((NSMutableArray)openItems().objectForKey(pKey) != null) {
            returnVal = ((NSMutableArray)openItems().objectForKey(pKey)).count();
        }
        return returnVal;
    }
    public NSMutableArray openForCurrentKey() {
        return openForKey(currentKey);
    }
    public NSMutableArray openForKey(String pKey) {
        NSMutableArray temp = (NSMutableArray)openItems().objectForKey(pKey);
        return temp;
    }
    public NSMutableArray closedForKey(String pKey) {
        NSMutableArray temp = (NSMutableArray)closedItems().objectForKey(pKey);
        return temp;
    }
    public NSMutableArray closedForCurrentKey() {
        return closedForKey(currentKey);
    }
    public int numClosedForKey(String pKey) {
        int returnVal = 0;
        if((NSMutableArray)closedItems().objectForKey(pKey) != null) {
            returnVal = ((NSMutableArray)closedItems().objectForKey(pKey)).count();
        }
        return returnVal;
    }
    public int numClosedForCurrentKey() {
        return numClosedForKey(currentKey);
    }
    public int numItemsForCurrentKey() {
        return numClosedForCurrentKey() + numOpenForCurrentKey();
    }
    public int numItemsForKey(String pKey) {
        return numClosedForKey(pKey) + numOpenForKey(pKey);
    }
	/*


    public int estimateForPersonForCurrentKey(String pPerson) {
        int returnVal = 0;
        NSMutableDictionary estimatesForPeople;
        estimatesForPeople = (NSMutableDictionary)estimatesForPeopleForCurrentKey();
        if(estimatesForPeople != null) {
            Double val = (Double)estimatesForPeople.objectForKey(pPerson);
            if(val != null) {
                returnVal = val.intValue();
                // Round up
                double remainder = val.doubleValue() - (double)returnVal;
                if(remainder > 0) {
                    returnVal++;
                }
            }
        }
        return returnVal;
    }
    public NSMutableDictionary estimatesForPeopleForCurrentKey() {
        return estimatesForPeopleForMilestone(currentKey());
    }
    public NSMutableDictionary estimatesForPeopleForMilestone(String pMilestone) {
        NSMutableDictionary estimatesForPeople;

        estimatesForPeople = (NSMutableDictionary)openEstimatesForPeopleForMilestone().objectForKey(pMilestone);
        if(estimatesForPeople == null) {
            estimatesForPeople = new NSMutableDictionary();
            NSMutableArray openItems = openForCurrentKey();  // uses currentKey which will be the milestone
            if(openItems != null) {
                Enumeration enumer = openItems.objectEnumerator();

                while(enumer.hasMoreElements()) {
                    Item currItem = (Item)enumer.nextElement();
                    String assignee = (String)currItem.valueForKeyPath("assignee.realname");
                    Double val = (Double)estimatesForPeople.objectForKey(assignee);
                    if(val == null) {
                        estimatesForPeople.setObjectForKey(new Double(((BigDecimal)currItem.valueForKey("estimatedtime")).doubleValue()),assignee);
                    }
                    else {
                        double d = val.doubleValue();
                        d += currItem.numericEstimate();
                        estimatesForPeople.setObjectForKey(new Double(d),assignee);
                    }
                }
                openEstimatesForPeopleForMilestone().setObjectForKey(estimatesForPeople, currentKey());
            }
        }
        return estimatesForPeople;
    }
    // dictionary will be m1 = ([anson,14], [gautam,23], [murali,7])
    public NSMutableDictionary openEstimatesForPeopleForMilestone() {
        if(openEstimatesForPeopleForMilestone == null) {
            openEstimatesForPeopleForMilestone = new NSMutableDictionary();
        }
        return openEstimatesForPeopleForMilestone;
    }
    public double maxDaysForMilestone() {
        //System.out.println("maxDaysForMilestone - ");

        double maxVal = 0.0;

        NSMutableDictionary personDict = (NSMutableDictionary)estimatesForPeopleForCurrentKey();
        if(personDict != null) {
            Enumeration enumer = personDict.keyEnumerator();
            while(enumer.hasMoreElements()) {
                String currItem = (String)enumer.nextElement();

                double valForPerson = ((Double)personDict.objectForKey(currItem)).doubleValue();
                if(valForPerson > maxVal) {
                    maxVal = valForPerson;
                }
            }
        }
        return maxVal;
    }
    
    // culumative totals for each person for each milestone
    public NSMutableDictionary totalHoursForPeopleForMilestone() {
       if(totalHoursForPeopleForMilestone == null) {
           totalHoursForPeopleForMilestone = new NSMutableDictionary();
            NSMutableDictionary currTotals = new NSMutableDictionary();
            NSMutableDictionary previousTotals = null;
            NSMutableArray allPeople = new NSMutableArray();

            Enumeration enumer = sortedMilestones().objectEnumerator(); // go through all of the milestones until the selected one
            while(enumer.hasMoreElements()) {
                String milestone = (String)enumer.nextElement();

                NSMutableDictionary currMilestoneDict = (NSMutableDictionary)estimatesForPeopleForMilestone(milestone);
                if(currMilestoneDict != null) {
                    Enumeration e = currMilestoneDict.keyEnumerator(); // m1 = ([anson=5], [gautam=6]) // need to get the people who were in the original
                    while(e.hasMoreElements()) {
                        String currPerson = (String)e.nextElement();
                        if(allPeople.containsObject(currPerson) == false) {
                            allPeople.addObject(currPerson);
                        }
                    }


                    Enumeration e2 = allPeople.objectEnumerator();
                    while(e2.hasMoreElements()) {
                        String currPerson = (String)e2.nextElement();
                        Double d = (Double)currMilestoneDict.objectForKey(currPerson);

                        if(previousTotals == null) {
                            currTotals.setObjectForKey(d, currPerson);
                        }
                        else {
                            Double cum;
                            // get total from previous and add it to the current total
                            Double d2 = (Double)previousTotals.objectForKey(currPerson);
                            if(d == null) {
                                cum = d2;
                            }
                            else if(d2 == null) {
                                cum = d;
                            }
                            else {
                                cum = new Double(d2.doubleValue() + d.doubleValue());  // Add the current estimate to the previous estimate
                            }
                            currTotals.setObjectForKey(cum, currPerson);
                        }
                    }
                    previousTotals = new NSMutableDictionary(currTotals); // save the previous dictionary to add the totals
                    totalHoursForPeopleForMilestone.setObjectForKey(currTotals, milestone);  // all people with cumulative estimates for a milestone
                    currTotals = new NSMutableDictionary();

                }
            }
       }
        return totalHoursForPeopleForMilestone;
    }
    public double maxTotalDaysForMilestone(String pMilestone) {
        //System.out.println("maxTotalDaysForMilestone - ");

        double maxVal = 0.0;

        NSMutableDictionary personDict = (NSMutableDictionary)totalHoursForPeopleForMilestone().objectForKey(pMilestone);
        if(personDict != null) {
            Enumeration enumer = personDict.keyEnumerator();
            while(enumer.hasMoreElements()) {
                String currItem = (String)enumer.nextElement();
                double valForPerson = ((Double)personDict.objectForKey(currItem)).doubleValue();
                if(valForPerson > maxVal) {
                    maxVal = valForPerson;
                }
            }
        }

        return maxVal;
    }
*/

    public boolean isEstimated() {
		boolean returnVal = false;
		if(isItems() == true) {
			returnVal = numNotEstimated()>0?false:true;
		}
		else {
			returnVal = true;  // no items so estimates are 'complete'
		}
        return returnVal;
    }

    public boolean isItems() {

        boolean returnVal = false;
        if((isOpenItems() == true) || (isResolvedItems() == true) || (isClosedItems() == true)) {
            returnVal = true;
        }
			
        return returnVal;
    }
    public boolean isComplete() {
        return isOpenItems()?false:true;
    }
    public boolean isOpenItems() {
        return openItems().count() >0?true:false;
    }
    public boolean isResolvedItems() {
        return resolvedItems().count() >0?true:false;
    }
    public boolean isClosedItems() {
        return closedItems().count() >0?true:false;
    }

    public String toString() {
        String returnVal = "";
        returnVal += "Name = " + displayName() + "\n";
        returnVal += "Open: "+ "\n";
        Enumeration enumer = openItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String key = (String)enumer.nextElement();
            NSMutableArray open = (NSMutableArray)openItems().objectForKey(key);
            int numItems = open.count();
            for(int i = 0; i < numItems; i++) {
			
               // EOEnterpriseObject eo = (EOEnterpriseObject)open.objectAtIndex(i);
                EOEnterpriseObject eo = (EOEnterpriseObject)open.objectAtIndex(i);
                returnVal += " - open - " + key + " - " + (Integer)eo.valueForKey("bugId") + " - " + (String)eo.valueForKeyPath("product.productName") + " - " + (String)eo.valueForKey("shortDesc") + "\n";
            }
        }

        returnVal += "Resolved: "+ "\n";
		enumer = resolvedItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String key = (String)enumer.nextElement();
            NSMutableArray resolved = (NSMutableArray)resolvedItems().objectForKey(key);
            int numItems = resolved.count();
            for(int i = 0; i < numItems; i++) {
			
                EOEnterpriseObject eo = (EOEnterpriseObject)resolved.objectAtIndex(i);
                returnVal += " - resolved - " + key + " - " + (Integer)eo.valueForKey("bugId") + " - " + (String)eo.valueForKeyPath("product.productName") + " - " + (String)eo.valueForKey("shortDesc") + "\n";
            }
        }

        returnVal += "Closed: "+ "\n";
        enumer = closedItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String key = (String)enumer.nextElement();
            NSMutableArray closed = (NSMutableArray)closedItems().objectForKey(key);
            int numItems = closed.count();
            for(int i = 0; i < numItems; i++) {
               // EOEnterpriseObject eo = (EOEnterpriseObject)closed.objectAtIndex(i);
                EOEnterpriseObject eo = (EOEnterpriseObject)closed.objectAtIndex(i);
                returnVal += " - closed - " + key + " - " + (Integer)eo.valueForKey("bugId") + " - " + (String)eo.valueForKeyPath("product.productName") + " - " + (String)eo.valueForKey("shortDesc") + "\n";
            }
        }

        return returnVal;
    }
	
	

    public NSArray allOpenItems() {
        Enumeration enumer;
        NSMutableArray allOpen = new NSMutableArray();

        enumer = openItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String key = (String)enumer.nextElement();
            allOpen.addObjectsFromArray((NSArray)openItems().objectForKey(key));
        }
        return new NSArray(allOpen);
    }
    public NSArray allResolvedItems() {
        Enumeration enumer;
        NSMutableArray allResolved= new NSMutableArray();

        enumer = resolvedItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String key = (String)enumer.nextElement();
            allResolved.addObjectsFromArray((NSArray)resolvedItems().objectForKey(key));
        }
        return new NSArray(allResolved);
    }
    public NSArray allClosedItems() {
        Enumeration enumer;
        NSMutableArray allClosed = new NSMutableArray();

        enumer = closedItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String key = (String)enumer.nextElement();
            allClosed.addObjectsFromArray((NSArray)closedItems().objectForKey(key));
        }
        return new NSArray(allClosed);
    }

    public NSArray allItems() {
        NSMutableArray allItems = new NSMutableArray();
        allItems.addObjectsFromArray(allOpenItems());
        allItems.addObjectsFromArray(allResolvedItems());
        allItems.addObjectsFromArray(allClosedItems());

        return new NSArray(allItems);
    }
    
	// find the last modified child
	public String lastModifiedAll() {
		NSTimestamp lastMod = null;
		NSTimestamp	today = new NSTimestamp();
	
		Enumeration enumer = allItems().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item anItem = (Item)enumer.nextElement();
			if(lastMod == null) {
				lastMod = anItem.lastdiffed();
			}
			else {
				if(lastMod.compare(anItem.lastdiffed()) < 0) {
					lastMod = anItem.lastdiffed();
				}
			}
		}
		return elapsedTimeSimple(lastMod, today);
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

    public String urlForAllItems() {
        return listURLForArray(allItems());
    }
    public String urlForOpenItems() {
        return listURLForArray(allOpenItems());
    }
    public String urlForResolvedItems() {
        return listURLForArray(allResolvedItems());
    }
    public String urlForClosedItems() {
        return listURLForArray(allClosedItems());
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


    // Accessors
    public String displayName() { return displayName; }
    public void setDisplayName(String pValue) { displayName = pValue;}
    public NSMutableDictionary openItems() { return openItems; }
    public void setOpenItems(NSMutableDictionary pValue) { openItems = pValue;}
    public void addOpenItemForKey(Item eo, String pKey) {
        //System.out.println("addOpenItemForKey - pKey - " + pKey);
        NSMutableArray openArray;
		currentEstimateHoursAll = -1;  // 
		remainingHoursOpen = -1;  // 
		origEstimateHoursAll = -1;	
        openArray = (NSMutableArray)openItems.objectForKey(pKey);
        if(openArray == null) {
            openArray = new NSMutableArray();
        }
        
        // Don't add duplicates
        if(openArray.containsObject(eo) == false) {
            numOpenItems++;
          //  numOpenHours += eo.numericEstimate();
            openArray.addObject(eo);
            openItems.setObjectForKey(openArray, pKey);
        }
    }
    public NSMutableDictionary resolvedItems() { return resolvedItems; }
    public void setResolvedItems(NSMutableDictionary pValue) { resolvedItems = pValue;}
    public void addResolvedItemForKey(Item eo, String pKey) {
        NSMutableArray resolvedArray;
		currentEstimateHoursAll = -1;
		remainingHoursOpen = -1;
		origEstimateHoursAll = -1;
        resolvedArray = (NSMutableArray)resolvedItems.objectForKey(pKey);
        if(resolvedArray == null) {
            resolvedArray = new NSMutableArray();
        }
        
        // Don't add duplicates
        if(resolvedArray.containsObject(eo) == false) {
            resolvedArray.addObject(eo);
            resolvedItems.setObjectForKey(resolvedArray, pKey);
            numResolvedItems++;
        }
         
    }
    public NSMutableDictionary closedItems() { return closedItems; }
    public void setClosedItems(NSMutableDictionary pValue) { closedItems = pValue;}
    public void addClosedItemForKey(Item eo, String pKey) {
        NSMutableArray closedArray;
		currentEstimateHoursAll = -1;
		remainingHoursOpen = -1;
        origEstimateHoursAll = -1;
		//System.out.println("Assignee - " + eo.valueForKeyPath("assignee.realname") + " - " + eo.valueForKeyPath("bugId"));
        closedArray = (NSMutableArray)closedItems.objectForKey(pKey);
        if(closedArray == null) {
            closedArray = new NSMutableArray();
        }
        
        // Don't add duplicates
        if(closedArray.containsObject(eo) == false) {
            closedArray.addObject(eo);
            closedItems.setObjectForKey(closedArray, pKey);
            numClosedItems++;
   //         numClosedHours += eo.numericEstimate();
        }
         
    }

    public NSMutableDictionary openEstimated() { return openEstimated;  }
    public void setOpenEstimated(NSMutableDictionary pVal) { openEstimated = pVal; }
    public void addOpenEstimated(Boolean pOpenEstimatedFlag, String pKey) { openEstimated.setObjectForKey(pOpenEstimatedFlag, pKey); }
    public String currentKey() { return currentKey; }
    public void setCurrentKey(String pVal) {
        //System.out.println("setCurrentKey() - " + pVal);
        addToSortedMilestones(pVal);
        currentKey = pVal;
    }
    public NSMutableArray sortedMilestones() {
        if(sortedMilestones == null) {
            sortedMilestones = new NSMutableArray();
        }
        return sortedMilestones;
    }
    public void addToSortedMilestones(String pKey) {
        if((pKey != null) && (sortedMilestones().containsObject(pKey) == false)) {
            sortedMilestones().addObject(pKey);
        }
    }
    public void setSortedMilestones(NSMutableArray pVal) {  sortedMilestones = pVal; }
    public int numItems() {return numOpenItems + numClosedItems;}
  //  public double numHours() {return numOpenHours + numClosedHours;}
	
	/*
    public double percentCompleteForKey(String pKey) {
        return (numClosedHoursForKey(pKey)/(numTotalDaysForKey(pKey))) * 100;
    }

    public double percentCompleteForCurrentKey() {
        return (numClosedHoursForCurrentKey()/(double)numTotalDaysForCurrentKey()) * 100;
    }
    public double percentCompleteItemCountForCurrentKey() {
        return (numClosedForCurrentKey()/(double)numItemsForCurrentKey()) * 100;
    }

    public String percentCompleteForCurrentKeyString() {
        String returnVal = null;
        double val = percentCompleteForCurrentKey();
        if(Double.isNaN(val)) {
            if(numItems() > 0) {
                returnVal = percentCompleteItemsForCurrentKeyString();
            }
            else {
                returnVal = "---";
            }
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(val)) + "%";
            }
            catch(Exception e) {
                System.err.println("StatusItem.percentCompleteForCurrentKeyString() - " + e);
            }
        }
        return returnVal;
    }
    public String percentCompleteItemsForCurrentKeyString() {
        String returnVal = null;
        double val = percentCompleteItemCountForCurrentKey();
        if(Double.isNaN(val)) {
            if(numItems() > 0) {
                returnVal =  "Incomplete";
            }
            else {
                returnVal = "---";
            }
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(val)) + "%";
            }
            catch(Exception e) {
                System.err.println("StatusItem.percentCompleteItemsForCurrentKeyString() - " + e);
            }
        }
        return returnVal;
    }


    public double percentComplete() {
        return (numClosedHours/numHours()) * 100;
    }
    public double percentCompleteItemCount() {
        return (numClosedItems/(double)numItems()) * 100;
    }
	
    public String percentCompleteString() {
        String returnVal = null;
        double val = percentComplete();
        if(Double.isNaN(val)) {
            if(numItems() > 0) {
                returnVal = percentCompleteItemsString();
            }
            else {
                returnVal = "---";
            }
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(val)) + "%";
            }
            catch(Exception e) {
                System.err.println("StatusItem.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }
	*/
	public String percentCompleteCount() {
		int fixed = allClosedItems().count();
		int total = allItems().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
    public String percentCompleteHours() {
        return percentCompleteStringFromDouble(((double)(currentEstimateHoursAll()-remainingHoursOpen()) /(double)currentEstimateHoursAll()) * 100);
    }
	

	public String percentCompleteStringFromDouble(double pPercentComplete) {
        String returnVal = null;
        if(Double.isNaN(pPercentComplete)) {
                returnVal = "---";
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(pPercentComplete)) + "%";
            }
            catch(Exception e) {
                System.err.println("Item.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }
	/*
    public String percentCompleteItemsString() {
        String returnVal = null;
        double val = percentCompleteItemCount();
        if(Double.isNaN(val)) {
            if(numItems() > 0) {
                returnVal = "Incomplete";
            }
            else {
                returnVal = "---";
            }
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(val)) + "%";
            }
            catch(Exception e) {
                System.err.println("StatusItem.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }
	*/
    
}

