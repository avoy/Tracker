package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.general.Dataset;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;


import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import er.plot.ERPChart;

public class FindFixReport extends ERPChart {
	private static final long serialVersionUID = 1L;	
    protected NSArray projectBugs;
    protected NSTimestamp endDate;
    protected NSTimestamp startDate;
    protected final int NUMBER_OF_DAYS = 130;
    protected Plots plot;
    protected String selectedProject;
    protected boolean isWrapped = true;
    
    protected String _categoryKey;
    protected String _yName;
    protected String _xName;
    protected PlotOrientation _orientation;
	protected TimeSeriesCollection aDataset;
	public boolean onlyTopPriority = false;
	public boolean emailFlag = false;

	
	
	public FindFixReport(WOContext aContext) {
		super(aContext);
		
		//	Enddate will be 12am at the start of the next day, GMT. 
		GregorianCalendar myCalendar = new GregorianCalendar();
		int year = myCalendar.get(GregorianCalendar.YEAR);
		int day = myCalendar.get(GregorianCalendar.DAY_OF_MONTH);
		int month = myCalendar.get(GregorianCalendar.MONTH);

		NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);
		endDate = new NSTimestamp(year, month+1, day, 23, 59, 0, tz);
		startDate = endDate.timestampByAddingGregorianUnits(0, 0, -(NUMBER_OF_DAYS), 0, 0, 0); // NUMBER_OF_DAYS days ago.
    }
	
	// This report has three plots to graph
    // a. number of bugs found daily
    // b. number of bugs where resolution='fixed' daily
    // c. number of open (new&assigned) per day

    // ---- Number of open bugs  --- 
    // - Fetch all bugs in project (only fetch once per day per project)
    // - sort by earliest creation date
    //  there are two dates we are interested in: 
    //   1. Start date
    //		a. initially - arbitrary date of NUMBER_OF_DAYS days ago
    //  	b. could be some other way of determining project start date.
    //	 2. End date
    //		a. current day (today)
    //
    // for each bug
    //	  -- determine the creation date and the resolved date
    //	  -- Calculate the number of days open of the numHours range.
    //    -- increment all of the days in the array, based on an offset from startdate.
    public void findFixOpenReport() {
        plot = new Plots();
        fetchItemsInProject();
        numberOpen();
        findRate();
        fixRate();
        numberResolved();
    }


    public static final NSArray SUPPORTED_TYPES = new NSArray(new Object[]{
            "BarChart", "StackedBarChart", "BarChart3D", "StackedBarChart3D", "AreaChart", 
            "StackedAreaChart", "LineChart", "WaterfallChart"
    });


    public void reset() {
        super.reset();
        _xName = null;
        _yName = null;
        _categoryKey = null;
        _orientation = null;
    }
    
    public String categoryKey() {
        if(_categoryKey == null) {
            _categoryKey = stringValueForBinding("categoryKey", null);
        }
        return _categoryKey;
    }
    
    public String xName() {
        if(_xName == null) {
            _xName = stringValueForBinding("xName", "Days");
        }
        return _xName;
    }
    
    public String yName() {
        if(_yName == null) {
            _yName = stringValueForBinding("yName", "Count");
        }
        return _yName;
    }
    
    public PlotOrientation orientation() {
        if(_orientation == null) {
            _orientation = ("horizontal".equals(stringValueForBinding("orientation", "vertical")) ? 
                    PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
        }
        return _orientation;
    }
    
    protected NSArray supportedTypes() {
        return SUPPORTED_TYPES;
    }
    
    protected JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Trends",		// Title
            "Day",			// x-axis Label
            "Count",		// y-axis Label
			aDataset(),		// Dataset
			//PlotOrientation.VERTICAL, // Plot Orientation
            true,			// Show Legend
            true,			// Use tooltips
            false			// Configure chart to generate URLs?
        );		
		return chart;
    }
	

    protected XYDataset createDataset() {
        return (XYDataset)aDataset();
    }
	public String chartType() {
	  return "LineChart";
	}
	
	public TimeSeriesCollection aDataset() {
		if(aDataset == null) {
			aDataset = new TimeSeriesCollection();
		}
		return aDataset;
	}

	
	public Plots plot() {
	  if(plot == null) {
			plot = new Plots();
	  }
	  return plot;
	}

    public void fetchItemsInProject() {
        EOFetchSpecification fs = null;
        NSDictionary bindings = null;
        Session s = (Session)session();
        if(selectedProject == null) {
            selectedProject = s.selectedProject;
        }
		
        //bindings = new NSDictionary(new Object[] {selectedProject, "Defects"}, new Object[] { "version", "type"});
        bindings = new NSDictionary(new Object[] {selectedProject}, new Object[] { "version"});
		
		// Only P1 and P2 bugs
		if(onlyTopPriority == true) {
           fs = EOFetchSpecification.fetchSpecificationNamed( "projectTopPriorityBugs", "Item").fetchSpecificationWithQualifierBindings( bindings );
		}
		else {
           fs = EOFetchSpecification.fetchSpecificationNamed( "projectBugs", "Item").fetchSpecificationWithQualifierBindings( bindings );
		}
        projectBugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);

    }
    public void numberOpen() {
        EOFetchSpecification resolutionfs;
        NSDictionary resolutionBindings = null;
        Session s = (Session)session();
        NSTimestamp whenResolved;

        int numBugs = projectBugs.count();
        int[] openCounts = new int[NUMBER_OF_DAYS]; //

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)projectBugs.objectAtIndex(i);
            NSTimestamp creationDate = (NSTimestamp)eo.valueForKey("creationTs");
			//System.out.println("creationDate - " + creationDate);

            int startIndex = 0;
            int endIndex = NUMBER_OF_DAYS; //

            DateUtil dateUtil = new DateUtil(startDate, creationDate);
            int days = (int)dateUtil.days;

            if(days < 0) {
                startIndex = 0;
            }
            else {
                startIndex = days;
            }
            // check Status, if closed - when?
            String status = (String)eo.valueForKey("bugStatus");
            if((status.equals("RESOLVED")) || (status.equals("VERIFIED")) || (status.equals("CLOSED"))) {

                resolutionBindings = new NSDictionary(new Object[] {eo.valueForKey("bugId")}, new Object[] { "bugId",});
                resolutionfs = EOFetchSpecification.fetchSpecificationNamed( "resolvedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );

                NSArray resolutionActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(resolutionfs);
                // if a bug was reopened, it may have moved to 'resolved' more than once.  This will always select the last one.
                EOEnterpriseObject resolvedActivity = (EOEnterpriseObject)resolutionActivities.objectAtIndex(resolutionActivities.count()-1);
                whenResolved = (NSTimestamp)resolvedActivity.valueForKey("bugWhen");

                DateUtil dateUtil2 = new DateUtil( startDate, whenResolved);
                endIndex = ((int)dateUtil2.days); 
            }
            else {  
				// Item is still opened ("NEW" or 'ASSIGNED' or 'REOPENED')
            }
			


            //  update bug stats
            for (int j = startIndex; j < endIndex; j++) {
                openCounts[j]++;
            }
        }
        CreateSubPlot("Open", openCounts);
    }
    public void numberResolved() {
        NSDictionary resolutionBindings = null;
        EOFetchSpecification resolutionfs;
        EOFetchSpecification closedfs;
        EOFetchSpecification verifiedfs;

        Session s = (Session)session();
        NSTimestamp whenResolved;
        NSTimestamp whenClosed;

        int numBugs = projectBugs.count();
        int[] resolvedCounts = new int[NUMBER_OF_DAYS]; //

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)projectBugs.objectAtIndex(i);
            NSTimestamp creationDate = (NSTimestamp)eo.valueForKey("creationTs");
			//System.out.println("creationDate - " + creationDate);

            int startIndex = 0;
            int endIndex = NUMBER_OF_DAYS; //

            //DateUtil dateUtil = new DateUtil(startDate, creationDate);
            //int days = (int)dateUtil.days;

            // check Status, if closed - when?
            String status = (String)eo.valueForKey("bugStatus");
            if((status.equals("RESOLVED")) || (status.equals("VERIFIED")) || (status.equals("CLOSED"))) {
				//1) startIndex = When moved to resoved
				//2) endIndex = When moved to closed

				//When Resolved
                resolutionBindings = new NSDictionary(new Object[] {eo.valueForKey("bugId")}, new Object[] { "bugId",});
                resolutionfs = EOFetchSpecification.fetchSpecificationNamed( "resolvedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );

                NSArray resolutionActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(resolutionfs);
                // if a bug was reopened, it may have moved to 'resolved' more than once.  This will always select the last one.
                EOEnterpriseObject resolvedActivity = (EOEnterpriseObject)resolutionActivities.objectAtIndex(resolutionActivities.count()-1);
                whenResolved = (NSTimestamp)resolvedActivity.valueForKey("bugWhen");

                DateUtil dateUtil2 = new DateUtil( startDate, whenResolved);
                startIndex = ((int)dateUtil2.days); 
				if(startIndex < 0) {
					startIndex = 0;
				}

				
				//When Closed
                closedfs = EOFetchSpecification.fetchSpecificationNamed( "closedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );
                NSArray closedActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(closedfs);
                // if a bug was reopened, it may have moved to 'closed' more than once.  This will always select the last one.
				if(closedActivities.count() > 0) {
					EOEnterpriseObject closedActivity = (EOEnterpriseObject)closedActivities.objectAtIndex(closedActivities.count()-1);
					whenClosed = (NSTimestamp)closedActivity.valueForKey("bugWhen");

					DateUtil dateUtil3 = new DateUtil( startDate, whenClosed);
					endIndex = ((int)dateUtil3.days); 
					if(endIndex > NUMBER_OF_DAYS) {
						endIndex = NUMBER_OF_DAYS;
					}
				}
				else {
						//System.out.println("closed not set - " + eo.valueForKey("bugId"));
						//When Verified
						verifiedfs = EOFetchSpecification.fetchSpecificationNamed( "verifiedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );
						NSArray verifiedActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(verifiedfs);
						// if a bug was reopened, it may have moved to 'Verified' more than once.  This will always select the last one.
						if(verifiedActivities.count() > 0) {
							EOEnterpriseObject verifiedActivity = (EOEnterpriseObject)verifiedActivities.objectAtIndex(verifiedActivities.count()-1);
							NSTimestamp whenVerified = (NSTimestamp)verifiedActivity.valueForKey("bugWhen");

							DateUtil dateUtil3 = new DateUtil( startDate, whenVerified);
							endIndex = ((int)dateUtil3.days); 
							if(endIndex > NUMBER_OF_DAYS) {
								endIndex = NUMBER_OF_DAYS;
							}
								
						}
						else {
							// Must be still in 'Resolved' State (not closed and not verified)
						}
				}
								
								
				//System.out.println("startIndex/endIndex = " + startIndex + " / " + endIndex);
				//  only update bug stats for non-open items
				for (int j = startIndex; j < endIndex; j++) {
					resolvedCounts[j]++;
				}

            }
            else {  
				// Item is still opened ("NEW" or 'ASSIGNED' or 'REOPENED')
            }
        }
        CreateSubPlot("Resolved", resolvedCounts);
    }

    public void findRate() {
        Session s = (Session)session();

        int numBugs = projectBugs.count();
        int[] findCounts = new int[NUMBER_OF_DAYS];//

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)projectBugs.objectAtIndex(i);
            NSTimestamp creationDate = (NSTimestamp)eo.valueForKey("creationTs");

          // kta   creationDate.gregorianUnitsSinceTimestamp(startDate, null, null, days, null, null, null);
            DateUtil dateUtil = new DateUtil(startDate, creationDate);
            int days = (int)dateUtil.days;// kta 4/9/07
            //System.out.println("findrate - " + days);
            if((days <=NUMBER_OF_DAYS)&& (days >=0)) {
                findCounts[days]++;
            }
        }
        CreateSubPlot("Found", findCounts);

    }

    public void fixRate() {
        EOFetchSpecification resolutionfs;
        NSDictionary resolutionBindings = null;
        Session s = (Session)session();
        NSTimestamp whenResolved;

        int numBugs = projectBugs.count();
        int[] fixCounts = new int[NUMBER_OF_DAYS];//

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)projectBugs.objectAtIndex(i);

            // check Status, if closed - when?
            String status = (String)eo.valueForKey("bugStatus");
            if((status.equals("RESOLVED")) || (status.equals("CLOSED"))) {

                resolutionBindings = new NSDictionary(new Object[] {eo.valueForKey("bugId")}, new Object[] { "bugId",});
                resolutionfs = EOFetchSpecification.fetchSpecificationNamed( "resolvedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );

                NSArray resolutionActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(resolutionfs);
                // if a bug was reopened, it may have moved to 'resolved' more than once.  This will always select the last one.
                EOEnterpriseObject resolvedActivity = (EOEnterpriseObject)resolutionActivities.objectAtIndex(resolutionActivities.count()-1);
                whenResolved = (NSTimestamp)resolvedActivity.valueForKey("bugWhen");

          // kta       whenResolved.gregorianUnitsSinceTimestamp(startDate, null, null, d, null, null, null);
                DateUtil dateUtil = new DateUtil(startDate,whenResolved);
                int days = (int)dateUtil.days;// kta 4/9/07


                //  update bug stats
                if((days <=NUMBER_OF_DAYS)&& (days >=0)) {
                    fixCounts[days]++;
                }
            }
        }
        CreateSubPlot("Fixed",fixCounts);

    }
    public void CreateSubPlot(String ptitle, int[] counts) {
        SubPlot theSub;
		String dateString;
        // Create Plots
		theSub =  plot().newSubPlot();
        theSub.setTitle(ptitle);
         NSTimestamp dailyDate= startDate; //initialize date
		 TimeSeries series = new TimeSeries(ptitle);
				 
         int i = counts.length;
         for (int k = 0; k < counts.length; k++) {
			 i--;
             theSub.addYItem(new Integer(counts[k]));  // count
			 dateString = "" + dailyDate.monthOfYear() + "/" + dailyDate.dayOfMonth() + "/" + dailyDate.yearOfCommonEra();
			 
			 theSub.addXItem(dateString);
			 Day current = new Day(dailyDate.dayOfMonth(), dailyDate.monthOfYear(), dailyDate.yearOfCommonEra());
			 series.add(current, counts[k]);
             dailyDate = dailyDate.timestampByAddingGregorianUnits(0, 0, 1, 0, 0, 0); // next date
			 //System.out.println("title - " + ptitle + " / count - " + counts[k] + " / date - " + dateString + " / k - " + k);
         }
		 aDataset().addSeries(series);  //JFreeChart
    }

 
    public NSDictionary graphSettings() {
        return plot.thePlotData();

    }
    public void  setGraphSettings(NSDictionary pSettings) {
       //
    }

    public CSVPlots goExport() {
        WOResponse response;
        WORequest request;

        SimpleDateFormat theFormat = new SimpleDateFormat("dd-MMM-yy");
        response = context().response();
        request = context().request();

        //response.setHeader("application/csv.ms-excel;", "Content-type");
		if(response != null) {
			response.setHeader("application/ms-excel;", "Content-type");
			response.setHeader("filename=\"trend_data-"+ theFormat.format(new Date()) + ".csv\"", "Content-Disposition");
        }
		CSVPlots nextPage = (CSVPlots)pageWithName("CSVPlots");
        nextPage.takeValueForKey(plot, "plot");
        return nextPage;
    }

	
    
    public WOComponent resetProject() {
        findFixOpenReport();
        return null;
    }


    public String selectedProject() {
        return selectedProject;
    }
    public void setSelectedProject(String pValue) {
	
		if((selectedProject != null) && (!pValue.equals(selectedProject))) {
		    selectedProject = pValue;
		    findFixOpenReport();
		}
    }
    public boolean isWrapped() {
        return isWrapped;
    }

    public void setIsWrapped(boolean newIsWrapped) {
        isWrapped = newIsWrapped;
    }
/*
    public NSData graphImage() {
        Graph graph = (Graph)pageWithName("Graph");
        graph.takeValueForKey(graphSettings(),"settings");
        graph.takeValueForKey("findfixdata.graph","fileName");
        return graph.newImage();
    }
*/
	
	public boolean emailFlag() {return emailFlag;}
	public void setEmailFlag(boolean pVal) {emailFlag = true;}
	public boolean onlyTopPriority() {return onlyTopPriority;}
	public void setOnlyTopPriority(boolean pVal) {onlyTopPriority = true;}
	

}

	//public static void fill(int[] a, int val)
//java.util.Arrays
//Arrays.fill(array, 0);
// Note: Each element in the array is initialized to the default value zero.
//    int[] x1 = new int[5];
