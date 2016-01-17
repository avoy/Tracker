package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class SubPlot extends EOCustomObject {
    protected NSMutableArray xData;
    protected NSMutableArray yData;
    protected String title;
    protected String graphType;

    public SubPlot() {
        xData = new NSMutableArray();
        yData = new NSMutableArray();
        title = "untitled";
    }

    public NSDictionary toDictionary() {
        NSMutableDictionary theDict = new NSMutableDictionary();
        theDict.setObjectForKey(xData, "XData");
        theDict.setObjectForKey(yData, "YData");
        theDict.setObjectForKey(title, "Title");
        theDict.setObjectForKey(Plots.getNextColor(), "Color");
        return (NSDictionary)theDict;
    }
    public void addXItem(Object theValue) {
        xData.addObject(theValue);
    }
    public void addYItem(Object theValue) {
        yData.addObject(theValue);
    }
    public String title() {
        return title;
    }
    public void setTitle(String myTitle) {
        title = myTitle;
    }
    public void setGraphType(String theType) {
        graphType = theType;
    }
    public NSMutableArray xData() {
        return xData;
    }
    public NSMutableArray yData() {
        return yData;
    }    
}
