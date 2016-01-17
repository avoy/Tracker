package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.math.BigDecimal;

public class BugStat {
    protected String label;
    protected NSMutableDictionary<Object, Object> bugCategories;
    public NSMutableArray<EOEnterpriseObject> allBugs;
    protected NSArray<String> sortKeys;
    protected double estimate = 0.0;
    protected double actual = 0.0;
    public int numNotEstimated = 0;
    public int numNotActual = 0;
    

    public BugStat() {
        bugCategories = new NSMutableDictionary<Object, Object>();
        allBugs = new NSMutableArray<EOEnterpriseObject>();
    }
    public BugStat(String pLabel) {
        bugCategories = new NSMutableDictionary<Object, Object>();
        allBugs = new NSMutableArray<EOEnterpriseObject>();
		setLabel(pLabel);
    }
    public void setLabel(String pValue) {
        label = pValue;
    }
    public String label() {
        return label;
    }

    public void addBugWithKey(EOEnterpriseObject pBug, String pKey) {
        NSMutableArray categoryArray;
        categoryArray = (NSMutableArray)bugCategories.objectForKey(pKey);
        if(categoryArray == null) {
            categoryArray = new NSMutableArray();
            bugCategories.setObjectForKey(categoryArray, pKey);
        }
        categoryArray.addObject(pBug);
        allBugs.addObject(pBug);
    }

    public String toString() {
        String returnVal = "";

        returnVal += "Label - " + label + "\n";
        returnVal += "all bugs - " + allBugs.count() + "\n";
        returnVal += "bugCategories - " + bugCategories.allKeys().count() + "\n";
        return returnVal;
    }

    public NSArray sortedValues() {
        Enumeration enumer;
        NSMutableArray sv;
        sv = new NSMutableArray();
        enumer = sortKeys().objectEnumerator();
        while(enumer.hasMoreElements()) {
            String yKey = (String)enumer.nextElement();
            NSArray catArray = (NSMutableArray)bugCategories.objectForKey(yKey);
            if(catArray == null) {
                sv.addObject(new Integer(0));
            }
            else {
                sv.addObject(new Integer(catArray.count()));
            }
        }
        return sv;
    }
    public NSArray bugsForKey(String pKey) {
       return (NSArray)bugCategories.objectForKey(pKey);
    }
    public int bugCountForKey(String pKey) {
       return ((NSArray)bugCategories.objectForKey(pKey)).count();
    }
    public NSArray allBugs() {
       return (NSArray)allBugs;
    }
	
	public NSArray categories() {
		return bugCategories.allKeys();
	}

	public NSArray itemsForCategory(String pCategory) {
		return (NSArray)bugCategories.objectForKey(pCategory);
	}
	
    public String bugNumbersForKey(String pKey) {
        String returnVal = "";
        Enumeration enumer;
        EOEnterpriseObject bug;

        NSArray catArray = (NSMutableArray)bugCategories.objectForKey(pKey);
        if(catArray != null) {
            enumer = catArray.objectEnumerator();
            while(enumer.hasMoreElements()) {
                bug = (EOEnterpriseObject)enumer.nextElement();
                returnVal += (Integer)bug.valueForKey("bugId") + ",";
            }
        }
        return returnVal;
    }
    public String bugNumbersForAll() {
        String returnVal = "";
        Enumeration enumer;
        EOEnterpriseObject bug;

        if(allBugs != null) {
            enumer = allBugs.objectEnumerator();
            while(enumer.hasMoreElements()) {
                bug = (EOEnterpriseObject)enumer.nextElement();
                returnVal += (Integer)bug.valueForKey("bugId") + ",";
            }
        }
        return returnVal;
    }
    public double actual() {
        Enumeration enumer;
        EOEnterpriseObject bug;
        BigDecimal act;

        if(actual == 0.0) {
            numNotActual = 0;
            enumer = allBugs.objectEnumerator();
            while(enumer.hasMoreElements()) {
                bug = (EOEnterpriseObject)enumer.nextElement();
                act = (BigDecimal)bug.valueForKey("remainingTime");
                //double timeEst = valueForTimeString(act);
                double timeEst = act.doubleValue();

                addToActual(timeEst);
               // if((act == null) || (act.equals("Unknown"))) {
                if((timeEst == 0.0)) {
                    numNotActual++;  // count of bugs without actual
                }

            }
        }
        return actual;
    }

    public double estimate() {
        Enumeration enumer;
        EOEnterpriseObject bug;
        BigDecimal est;

        if(estimate == 0.0) {
            numNotEstimated = 0;
            enumer = allBugs.objectEnumerator();
            while(enumer.hasMoreElements()) {
                bug = (EOEnterpriseObject)enumer.nextElement();
                est = (BigDecimal)bug.valueForKey("estimatedTime");
                //double timeEst = valueForTimeString(est);
                double timeEst = est.doubleValue();
                addToEstimate(timeEst);
                if((timeEst == 0.0)) {
                    numNotEstimated++;// count of bugs without estimate
                }
            }
        }
        return estimate;
    }
/*
    public double valueForTimeString(String pValue) {
        double returnVal = 0.0;
        int indexOfDays;
        String subStrEstimate;
         if ((!pValue.equals("Unknown")) && (!pValue.equals(""))){
            indexOfDays = pValue.indexOf("day");
            subStrEstimate = pValue.substring(0,indexOfDays-1);

            if(subStrEstimate.equals("1/2")){
                returnVal = 0.5;
            }
            else if(subStrEstimate.equals("1/4")){
                returnVal = 0.25;
            }
            else if(subStrEstimate.equals("More than 5")){
                returnVal = 6;
            }
            else if(subStrEstimate.equals("More than 7")){
                returnVal = 8;
            }
            else{
                int tmpCurrentAddition = Integer.valueOf(subStrEstimate).intValue();
                returnVal = (double) tmpCurrentAddition;
            }
        }
        return returnVal;
    }
	*/
    public void setEstimate(double pVal) {
        estimate = pVal;
    }
    public void addToEstimate(double pVal){
        estimate += pVal;
    }

    public void setActual(double pVal) {
        actual = pVal;
    }
    public void addToActual(double pVal){
        actual += pVal;
    }

    public void setSortKeys(NSArray pKeys) {
        sortKeys = pKeys;
    }
    public NSArray sortKeys() {
        return sortKeys;
    }
}

