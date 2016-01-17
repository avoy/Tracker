package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.StringTokenizer;
import java.util.Enumeration;

public class CSVPlots extends WOComponent {
	private static final long serialVersionUID = 1L;		
    public Plots plot;
    public NSMutableArray xValues;
    public SubPlot aSubPlot;
    public String aKey;
    public Integer aY;

    public CSVPlots(WOContext aContext) {
        super(aContext);
    }

    // to encode for CSV -
    //1) any cell that contains commas or carriage returns need to be double-quoted,
    //2) if the string is double quoted - double-quotes inside the String need to be converted to double - double quotes.
    public String csvEncode(String origString) {
        StringTokenizer commas,carriage;
        String tempString = "";

        // 1st check for commas - if none, leave the sting alone.
        commas = new StringTokenizer(origString, ",");
        int numTokens = commas.countTokens();
        carriage = new StringTokenizer(origString, "\n");
        int numCarriage = carriage.countTokens();

        if((numTokens != 1) || (numCarriage > 1)) {
            // replace double quotes with ""
            tempString =  quoteDoubled(origString);

            // wrap the entire String in double quotes. (it contains commas or carriage returns)
            tempString = "\"" + tempString + "\"";
        }
        else {
            tempString = origString;  // do nothing
        }

        return tempString;

    }

    public String quoteDoubled(String origString) {
        StringBuffer updatedText;
        char[] values;

        updatedText = new StringBuffer();
        values = origString.toCharArray();
        for(int i = 0; i < origString.length(); i++) {
            char currChar = values[i];
            if(currChar == '"') {
                updatedText.append(currChar);
            }
            updatedText.append(currChar);
        }
        return updatedText.toString();
    }

    public void setPlot(Plots thePlot) {
        plot = thePlot;
        //System.out.println("PlotDate - " + plot.thePlotData());

    }

    //Plot format is d/m/Y - Excel expects m/d/Y
    public String convertPlotDateToExcelDate(String aPlotDate) {
	
	// nonOp - I am passing in values in the correct format now.
	/*
        StringTokenizer tok;
        String standardDate;

        // 1st check for commas - if none, leave the sting alone.
        tok = new StringTokenizer(aPlotDate, "/");
        String day = (String)tok.nextToken();
        String month = (String)tok.nextToken();
        String year = (String)tok.nextToken();
        standardDate = month+"/"+day+"/"+year;
        return standardDate;
		*/
		return aPlotDate;

    }

    public NSMutableArray xValues() {
        if(xValues == null) {
            if((plot.plots() != null) && (plot.plots().count() > 0)) {
                SubPlot first = (SubPlot)plot.plots().objectAtIndex(0);
                xValues = new NSMutableArray();
                Enumeration enumer = first.xData().objectEnumerator();
                while(enumer.hasMoreElements()) {
                    String theDate = convertPlotDateToExcelDate((String)enumer.nextElement());
                    xValues.addObject(theDate);
                }
            }
        }
        return xValues;
    }
}
