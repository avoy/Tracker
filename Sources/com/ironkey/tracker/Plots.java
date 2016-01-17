package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;

public class Plots extends EOCustomObject {
	private static final long serialVersionUID = 1L;
    protected NSMutableArray plots = new NSMutableArray();
    protected static NSMutableArray theColors;
    protected String title;

    public Plots() {
        theColors = null;
    }

    public SubPlot getSubPlot(String pTitle) {
        SubPlot currSubPlot = null;
        Enumeration enumer = plots.objectEnumerator();
        while (enumer.hasMoreElements()) {
            currSubPlot = (SubPlot) enumer.nextElement();
            if(currSubPlot.title().equals(pTitle)) {
                break;
            }
        }
        return currSubPlot;

    }

    // High level all for top dictionary
    public NSMutableDictionary thePlotData() {
        NSMutableDictionary myPlot = new NSMutableDictionary();
        NSMutableArray subPlotDict = new NSMutableArray();

        Enumeration enumer = plots.objectEnumerator();
        while (enumer.hasMoreElements()) {
            SubPlot currSubPlot = (SubPlot) enumer.nextElement();
            subPlotDict.addObject(currSubPlot.toDictionary());

        }
        //    Title = "Defect Trends";
        if(title != null) {
            myPlot.setObjectForKey(title, "Title");
        }
        myPlot.setObjectForKey(subPlotDict, "Plots");
        return(myPlot);

    }

    // This adds the subplot to the plots dictionary before returning it.
    public SubPlot newSubPlot() {
        SubPlot theSubPlot = new SubPlot();
        plots.addObject(theSubPlot);
        return theSubPlot;
    }

    public void addToPlots(SubPlot theSubPlot) {
        plots.addObject(theSubPlot);
    }
    public void clearPlots() {
        plots = new NSMutableArray();
    }

    public static NSArray getNextColor() {
        initColors();
        NSArray theColor = (NSArray)theColors.objectAtIndex(0);
        theColors.removeObjectAtIndex(0);
        return theColor;
    }

    public static void initColors() {
        if(theColors == null) {
            theColors = new NSMutableArray();
        }
        if(theColors.count() == 0) {

            String[] aColor = {"0","0","255"};
            NSArray theColor = new NSArray(aColor);
            theColors.addObject(theColor);

            String[] bColor = {"255","0","0"};
            theColor = new NSArray( bColor);
            theColors.addObject(theColor);

            String[] cColor = {"0","255","0"};
            theColor = new NSArray(cColor);
            theColors.addObject(theColor);

            String[] dColor = {"255","0","255"};
            theColor = new NSArray(dColor);
            theColors.addObject(theColor);

            String[] eColor = {"0","0","0"};
            theColor = new NSArray(eColor);
            theColors.addObject(theColor);

            String[] fColor = {"125","0","50"};
            theColor = new NSArray(fColor);
            theColors.addObject(theColor);

            String[] gColor = {"50","125","0"};
            theColor = new NSArray(gColor);
            theColors.addObject(theColor);

            String[] hColor = {"50","0","125"};
            theColor = new NSArray(hColor);
            theColors.addObject(theColor);
        }
    }
    public NSMutableArray plots() {
        return plots;
    }
    public String title() {
        return title;
    }
    public void setTitle(String pValue) {
        title = pValue;
    }
}
