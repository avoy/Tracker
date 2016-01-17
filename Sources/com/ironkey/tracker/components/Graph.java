package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class Graph extends WOComponent {
	private static final long serialVersionUID = 1L;
    protected NSMutableDictionary settings;
    protected NSMutableDictionary graphDimensions;
    protected NSMutableDictionary xAxis;
    protected NSMutableDictionary yAxis;
    protected NSMutableArray sizeInPixels;
    protected NSArray potentialTypes;
    protected String selectedType = "HISTOGRAM";  //   Type = LINE (line chart), DOT (dot chart), HISTOGRAM (bar chart)
    protected String fileName = "trenddata.graph";
    protected String aString;
    protected Integer height = new Integer(400);
    protected Integer width = new Integer(500);
    protected Integer yTics = new Integer(5);
    protected Integer yAbs = new Integer(10);
    public boolean isWrapped;

    public Graph(WOContext aContext) {
        super(aContext);
        graphDimensions = new NSMutableDictionary();
        xAxis = new NSMutableDictionary();
        yAxis = new NSMutableDictionary();
        sizeInPixels = new NSMutableArray();
        Object[] types = { "HISTOGRAM", "LINE", "DOT"};

        potentialTypes = new NSArray(types);
    }



   public NSData newImage() {
       setDimensions();
        String path = Application.application().resourceManager().pathForResourceNamed(fileName, null, null);
		return new NSData();
   }

    public void setSettings(NSDictionary theValue) {
        if(settings == null) {
            settings = new NSMutableDictionary(theValue);
        }
        else {
            settings.addEntriesFromDictionary(theValue);
        }
    }
	/*
    public void setTheMetrics(Metrics theValue) {
        theMetrics = theValue;
        setSettings(theMetrics.plotDictionary());
        //calculateXAxisSettings();
    }
	*/

    public void setDimensions() {
        if((width() != null) || (height() != null)) {
            sizeInPixels = new NSMutableArray();
           if(width() != null) {
                sizeInPixels.addObject(width());
            }
            else
                sizeInPixels.addObject(new Integer(400));
            
            if(height() != null) {
                sizeInPixels.addObject(height());
            }
            else
                sizeInPixels.addObject(new Integer(300));
            graphDimensions.setObjectForKey(sizeInPixels, "SizeInPixels");
        }
        if((yAbs() != null) || (yTics() != null)) {
            if(yAbs() != null) {
                yAxis.setObjectForKey(yAbs(), "NumbersDeltaAbs");
            }
            if(yTics() != null) {
                yAxis.setObjectForKey(yTics(), "TicsDeltaAbs");
            }
            graphDimensions.setObjectForKey(yAxis, "YAxis");
        }

        setSettings(graphDimensions);
    }

    public WOComponent adjustDimensions() {
        settings.setObjectForKey(selectedType, "Type");  // Add the type
        return null;
    }

    public Integer height() {
        return height;
    }
    public void setHeight(Integer newHeight) {
        height = newHeight;
    }

    public Integer width() {
        return width;
    }
    public void setWidth(Integer newWidth) {
        width = newWidth;
    }

    public Integer yTics() {
        return yTics;
    }
    public void setYTics(Integer newYTics) {
        yTics = newYTics;
    }

    public Integer yAbs() {
        return yAbs;
    }
    public void setYAbs(Integer newYAbs) {
        yAbs = newYAbs;
    }

    public void setSelectedType(String pType) {
        selectedType = pType;
    }
    public String selectedType() {
        return selectedType;
    }
    public void setFileName(String pValue) {
        fileName = pValue;

    }
    public String fileName() {
        return fileName;
    }
}
