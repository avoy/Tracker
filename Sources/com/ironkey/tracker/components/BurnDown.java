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
import java.math.BigDecimal;

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
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.Dataset;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import er.plot.ERPChart;


public class BurnDown extends WOComponent {
	private static final long serialVersionUID = 1L;
//public class BurnDown extends ERPChart {
	public JFreeChart chart = null;
	public NSArray itemsToGraph;
	public NSTimestamp endDate;
	public NSTimestamp startDate;
	public NSTimestamp currentDate;
	public double[] hoursRemain;
	public int NUMBER_OF_DAYS = 45;
	public int numberOfDays = -1;
	public Plots plot;
	public String selectedProject;
	public boolean isWrapped = true;
	public String _categoryKey;
	public String _yName;
	public String _xName;
	public String chartType = "count";
	public PlotOrientation _orientation;
	public AbstractDataset aDataset;
	//protected TimeSeriesCollection aDataset;
	public boolean onlyTopPriority = false;
	public boolean emailFlag = false;
    public static final NSArray SUPPORTED_TYPES = new NSArray(new Object[]{
            "BarChart", "StackedBarChart", "BarChart3D", "StackedBarChart3D", "AreaChart", 
            "StackedAreaChart", "LineChart", "WaterfallChart"
    });


	public BurnDown(WOContext aContext) {
		super(aContext);		
		//System.out.println("BurnDown.BurnDown()");
		//	Enddate will be 12am at the start of the next day, GMT. 
		GregorianCalendar myCalendar = new GregorianCalendar();
		int year = myCalendar.get(GregorianCalendar.YEAR);
		int day = myCalendar.get(GregorianCalendar.DAY_OF_MONTH);
		int month = myCalendar.get(GregorianCalendar.MONTH);

		NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);
		//startDate = endDate.timestampByAddingGregorianUnits(0, 0, -(NUMBER_OF_DAYS), 0, 0, 0); // 

		currentDate = new NSTimestamp(year, month+1, day, 23, 59, 0, tz);
		endDate = currentDate;
		startDate = endDate.timestampByAddingGregorianUnits(0, 0, -(NUMBER_OF_DAYS), 0, 0, 0); // 

		Session mysession = (Session)session();
        selectedProject = mysession.selectedProject();
		

    }
	
	public int numberOfDays() {
		//System.out.println("BurnDown.numberOfDays()");
		
		if(numberOfDays == -1) {
			if(startDate()  == null) {			
				startDate = endDate().timestampByAddingGregorianUnits(0, 0, -(NUMBER_OF_DAYS), 0, 0, 0); // 
			}
			DateUtil dateUtil = new DateUtil(startDate(),endDate());
			numberOfDays = (int)dateUtil.days;
			if(numberOfDays <=0) {
				startDate = endDate().timestampByAddingGregorianUnits(0, 0, -(NUMBER_OF_DAYS), 0, 0, 0); // 
				dateUtil = new DateUtil(startDate(),endDate());
				numberOfDays = (int)dateUtil.days;
			}
		}
		return numberOfDays;

	}
	public NSTimestamp endDate() {
		return endDate;
	}
	
	public void setEndDate(NSTimestamp pVal) {
		if(pVal != null) {
		
			if(currentDate.compare(pVal) < 0) {
				endDate = currentDate;  // enddate is end of current day
			}
			else {
				endDate = pVal;
			}
		}

	}
	
	public NSTimestamp startDate() {	
		return startDate;
	}
	public void setStartDate(NSTimestamp pVal) {
		//if(pVal != null) {
			startDate = pVal;
			numberOfDays = -1;
		//}
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
   // public void findFixOpenReport() {
    //}
	//public boolean synchronizeVariablesWithBindings() {
	//	return true;
	//}
	
	public void generateGraphs() {
		//System.out.println("\n\n================BurnDown.generateGraphs()");
			plot = new Plots();
			aDataset = null;
		
			if((chartType != null) && (chartType.equals("hours"))) {
				taskHoursRemaining();
			}
			else {
				numberOpen();
				findRate();
				fixRate();
				numberResolved();
			}
			createChart();
	}
	
	public String yLabel() {
		String returnVal = "count";
		if((chartType != null) && (chartType.equals("hours"))) {
			returnVal = "hours";
		}
		return returnVal;
	}
	public String title() {
		String returnVal = "Trends";
		if((chartType != null) && (chartType.equals("hours"))) {
			returnVal = "BurnDown";
		}
		return returnVal;
	}
	
	public void setItemsToGraph(NSArray pArray) {
		 if(!pArray.equals(itemsToGraph)) {
			itemsToGraph = pArray;
			generateGraphs();
		}
	}


    public void reset() {
        super.reset();
        _xName = null;
        _yName = null;
        _categoryKey = null;
        _orientation = null;
    }
    /*
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
        */
    protected NSArray supportedTypes() {
        return SUPPORTED_TYPES;
    }
    
    protected JFreeChart createChart() {
		//System.out.println("BurnDown.createChart()");
   	
         chart = ChartFactory.createTimeSeriesChart(
            title(),		// Title
            "Day",			// x-axis Label
            yLabel(),		// y-axis Label
			(TimeSeriesCollection)aDataset(),		// Dataset
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
	
	//public TimeSeriesCollection aDataset() {
	public AbstractDataset aDataset() {
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
		
	public NSArray itemsToGraph() {
		//System.out.println("BurnDown.itemsToGraph() - ");
	
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;


		//if(itemsToGraph == null) {
		if(false) {
			//Bugs and Enhancements
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Enhancement'", null));
			//qual.addObject(new EOOrQualifier(qual1));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));


			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			itemsToGraph = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

		}
		return itemsToGraph;
    }


    public void numberOpen() {
		//System.out.println("\n\n================BurnDown.numberOpen()");


		EOFetchSpecification resolutionfs;
        NSDictionary resolutionBindings = null;
        Session s = (Session)session();
        NSTimestamp whenResolved;

        int numBugs = itemsToGraph().count();
		
        int[] openCounts = new int[numberOfDays()]; //
//System.out.println("numBugs - " + numBugs);

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)itemsToGraph().objectAtIndex(i);
            NSTimestamp creationDate = (NSTimestamp)eo.valueForKey("creationTs");
            Integer id = (Integer)eo.valueForKey("bugId");
            String type = (String)eo.valueForKey("type");
			//System.out.println("creationDate - " + creationDate);

            int startIndex = 0;
            int endIndex = numberOfDays()-1; //

            DateUtil dateUtil = new DateUtil(startDate(), creationDate);
            int days = (int)dateUtil.days;
			days = days-1;  // array is 0 based

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

                DateUtil dateUtil2 = new DateUtil( startDate(), whenResolved);
               endIndex = ((int)dateUtil2.days-1); 
            }
            else {  
				// Item is still opened ("NEW" or 'ASSIGNED' or 'REOPENED')
            }
			//System.out.println("Item - " + id + " - " + type + " - " + status + " - " + startIndex + " / " + endIndex);
			if((startIndex >= 0) && (endIndex >= 0) && (endIndex < numberOfDays())) {
				////System.out.println("startIndex/endIndex - " + startIndex + "/" + endIndex);
				//  update bug stats
				for (int j = startIndex; j <= endIndex; j++) {
					openCounts[j]++;
				}
			}
        }
		
			//System.out.println("OpenCounts: ");
			for (int j = 0; j < numberOfDays(); j++) {
				//System.out.println(j + " : " + openCounts[j]);
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
        NSTimestamp whenDone;

        int numBugs = itemsToGraph().count();
        int[] resolvedCounts = new int[numberOfDays()]; //
		int numResolved = 0;

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)itemsToGraph().objectAtIndex(i);
            String type = (String)eo.valueForKey("type");
            Integer id = (Integer)eo.valueForKey("bugId");

			//1) startIndex = When moved to resoved
			//2) endIndex = When moved to closed
            int startIndex = 0;
            int endIndex = numberOfDays()-1; //


            // check Status, if closed - when?
            String status = (String)eo.valueForKey("bugStatus");

			if(status.equals("RESOLVED")) {
				numResolved ++;
			}

			
            if((status.equals("RESOLVED")) || (status.equals("VERIFIED")) || (status.equals("CLOSED"))) {

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


				if((status.equals("VERIFIED")) || (status.equals("CLOSED"))) {
					//When Closed
					closedfs = EOFetchSpecification.fetchSpecificationNamed( "closedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );
					NSArray endActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(closedfs);
					if(endActivities.count() == 0) {
						// No closed activities - check for Verified
						verifiedfs = EOFetchSpecification.fetchSpecificationNamed( "verifiedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );
						endActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(verifiedfs);
					}
					
					
					// if a bug was reopened, it may have moved to 'closed' more than once.  This will always select the last one.
					if(endActivities.count() > 0) {
						EOEnterpriseObject activity = (EOEnterpriseObject)endActivities.objectAtIndex(endActivities.count()-1);
						whenDone = (NSTimestamp)activity.valueForKey("bugWhen");

						DateUtil dateUtil3 = new DateUtil( startDate, whenDone);
						endIndex = ((int)dateUtil3.days); 
						if(endIndex >= numberOfDays()) {
							endIndex = numberOfDays()-1;
						}
						else if (endIndex < 0) {
							endIndex = -1;
						}
					}
				}
				else {
					// Must be still in 'Resolved' State (not closed and not verified)
				}
								
			//System.out.println("Item - " + id + " - " + type + " - " + status + " - " + startIndex + " / " + endIndex);
				////System.out.println("startIndex/endIndex = " + startIndex + " / " + endIndex);
				//  only update bug stats for non-open items
				for (int j = startIndex; j <= endIndex; j++) {
					resolvedCounts[j]++;
				}

            }
            else {  
				// Item is still opened ("NEW" or 'ASSIGNED' or 'REOPENED')
            }
        }
		
		
			//System.out.println("numberOfDays() : " + numberOfDays());
			//System.out.println("numResolved : " + numResolved);
			//System.out.println("resolvedCounts: ");
			for (int j = 0; j < numberOfDays(); j++) {
				//System.out.println(j + " : " + resolvedCounts[j]);
			}
		
		
        CreateSubPlot("Resolved", resolvedCounts);
    }
	

    public void findRate() {	
		//System.out.println("\n\n================BurnDown.findRate()");
        int numBugs = itemsToGraph().count();
        int[] findCounts = new int[numberOfDays()];//

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)itemsToGraph().objectAtIndex(i);
            NSTimestamp creationDate = (NSTimestamp)eo.valueForKey("creationTs");

            DateUtil dateUtil = new DateUtil(startDate(), creationDate);
            int days = (int)dateUtil.days;// kta 4/9/07
			days = days-1; // array is 0 based
            if((days <numberOfDays())&& (days >=0)) {
                findCounts[days]++;
            }
        }
		
		//System.out.println("Find Counts: ");
		for (int j = 0; j < numberOfDays(); j++) {
			//System.out.println(j + " : " + findCounts[j]);
		}
		
        CreateSubPlot("Found", findCounts);

    }
	
    public void fixRate() {	
		//System.out.println("\n\n================BurnDown.fixRate()");
	
        EOFetchSpecification resolutionfs;
        NSDictionary resolutionBindings = null;
        Session s = (Session)session();
        NSTimestamp whenResolved;

        int numBugs = itemsToGraph().count();
        int[] fixCounts = new int[numberOfDays()];//

        for (int i = 0; i < numBugs; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)itemsToGraph().objectAtIndex(i);

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
                DateUtil dateUtil = new DateUtil(startDate(),whenResolved);
                int days = (int)dateUtil.days;// kta 4/9/07
				days = days-1; // array is 0 based

                //  update bug stats
                if((days <numberOfDays())&& (days >=0)) {
                    fixCounts[days]++;
                }
            }
        }
		
		//System.out.println("Fix Counts: ");
		for (int j = 0; j < numberOfDays(); j++) {
			//System.out.println(j + " : " + fixCounts[j] );
		}
		

        CreateSubPlot("Fixed",fixCounts);

    }
	


    public void taskHoursRemaining() {
		//NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);
	
		//NSTimestamp sprintStart = new NSTimestamp(2011, 2, 1, 0, 0, 0, tz);
		
		CreateSubPlotHours(startDate(), "Hours", hoursRemain());
	}
	
		// Steps:
	// Get initial 'remaining time' -  this is the estimate
	//  a. Estimate that is set on bug creation 
	//  b. Estimate that is set after bug creation (Remaining Time should be set at this time as well)
	//  c. Remain time that is set after bug creation
	
	// Apply any changes that happen prior to Window start time
	// Track all changes that occur during the Window
	
	public double[] hoursRemain() {
		//System.out.println("\n\n================BurnDown.hoursRemain()");

        EOFetchSpecification resolutionfs, estimatefs,remainingfs, closedfs;
        NSDictionary bugIdBindings = null;
		DateUtil dateUtil;
        Session s = (Session)session();
        NSTimestamp whenResolved;
		int sprintDays = 0;
		
		if(hoursRemain == null) {
			sprintDays = numberOfDays();
			////System.out.println("sprintDays - " + sprintDays);
				
				int numBugs = itemsToGraph().count();
				hoursRemain = new double[sprintDays];//
				for (int i = 0; i < numBugs; i++) {
					EOEnterpriseObject eo = (EOEnterpriseObject)itemsToGraph.objectAtIndex(i);
					NSTimestamp creationDate = (NSTimestamp)eo.valueForKey("creationTs");
					BigDecimal currEstimate = (BigDecimal)eo.valueForKey("estimatedTime");
					BigDecimal remainingTime = (BigDecimal)eo.valueForKey("remainingTime");
					String status = (String)eo.valueForKey("bugStatus");
					bugIdBindings = new NSDictionary(new Object[] {eo.valueForKey("bugId")}, new Object[] { "bugId",});
					estimatefs = EOFetchSpecification.fetchSpecificationNamed( "estimatedTime", "BugsActivity").fetchSpecificationWithQualifierBindings( bugIdBindings );
					remainingfs = EOFetchSpecification.fetchSpecificationNamed( "remainingHours", "BugsActivity").fetchSpecificationWithQualifierBindings( bugIdBindings );
					closedfs = EOFetchSpecification.fetchSpecificationNamed( "closedActivities", "BugsActivity").fetchSpecificationWithQualifierBindings( bugIdBindings );
					BigDecimal initialEstimate = null;
					NSTimestamp initialDate = null;
					
				
					if(currEstimate.doubleValue()  > 0.0) {  //  if there is no estimate, no reason to process the task
					
						// Get the initial Estimate and date set
						// Two ways to set estimate 
						//							a) when the bug is created (no activities created at that time 
						//							b) after bug was created
						// if there are 'estimate' activities - then either it was added after the bug was created or it was changed later
						
						// Not handling changes to estimates
						// always assume estime is set at creation time.
						initialDate = creationDate;
						initialEstimate = currEstimate;
						
						// Multi-step process
						// An array will be used for each task, with each element in the array represents a single day. 
						// 1) Initialize the array to all zeros
						// 2) Calculate when the item was created and update the array with the initial estimate 
						//		example: task created on day 3 of the sprint, initial estimate = 5 days.  
						//				First 3 elements of array are 0, and the rest will be set to 5)
						//				[0,0,0,5,5,5,5,5,5,5,5]
						//3) Apply changes - make adjustments
						//	a) hours worked
						//  b) task is closed and estimates were not set correctly
						//     - bugs can be created without the estimate set.  Someone can then go in a update the estimate, but not set the hours remaining.
						//     - If this bug is then closed, it has to be handled properly.  It has an initial estimate, but none of the normal 'hours worked' entries are created to adjust it.
						//  c) initial estimate and hours worked are set at the same time in such a way that the task is completed. 
						//      - in this case, there is not remain time record generated 
						// d) Remaining time get set to zero, but person continues to work on the task. [currently this case is not handled] 
					 
						 
						 //1)  Initialize array for the estimate - start with 0
						double[] hoursForBug = new double[sprintDays];//
						
						// add initial estimate					
						dateUtil = new DateUtil(startDate(),initialDate);
						int days = (int)dateUtil.days;
						////System.out.println("" + (Integer)eo.valueForKey("bugId") + " - initialEstimate - " + initialEstimate + " - days - " + days);
						
						if(days < 0) {
							days = 0;
						}
						for(int m = days; m < sprintDays; m++) {
							hoursForBug[m] = initialEstimate.doubleValue();
						}
						
						////System.out.print("HoursForbugs:Initial Estimate - "  );
						//for(int k = 0; k < sprintDays; k++) {
						//	//System.out.print(hoursForBug[k] + ", " );
						//}
						////System.out.print("\n"  );

						// array should be set to Initial Estimate
						// Now we need to make any ADJUSTMENTS
						
						// The primary adjustments come from people working on issues are setting 'hours worked'  These create an update to the 'remaining_time' field
						 // Get all of the records for Remaining time
						 NSArray remainingActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(remainingfs);
						 boolean skipFirst = false;
						 if(remainingActivities.count() > 0) {
							//EOEnterpriseObject firstremaining = 	(EOEnterpriseObject)remainingActivities.objectAtIndex(0);
							//if(((String)firstremaining.valueForKey("removed")).equals("0.00")) {
							//	initialEstimate = new BigDecimal((String)firstremaining.valueForKey("added"));
							//	skipFirst = true;
							//}				 
							BigDecimal currentRemaining = initialEstimate;
							//NSTimestamp currentDate = sprintStart;

							Enumeration enumer = remainingActivities.objectEnumerator();
							while(enumer.hasMoreElements()) {
								EOEnterpriseObject currentRemainingEO = (EOEnterpriseObject)enumer.nextElement();
								if(skipFirst == true) {
									////System.out.println("SKIPPING Bug : " + (Integer)currentRemainingEO.valueForKey("bugId") + " - "+ (NSTimestamp)currentRemainingEO.valueForKey("bugWhen") + " - " +  (String)currentRemainingEO.valueForKey("removed")+ " - " +  (String)currentRemainingEO.valueForKey("added") );
									skipFirst = false;
								}
								else {
									NSTimestamp currentChange = (NSTimestamp)currentRemainingEO.valueForKey("bugWhen");
									// Apply changes prior to sprintStart
									// If there is a 'remaining' entry where 'removed' = 0 it means the remaining time was added after the bug was created.  It should be the first one if sorted properly.
							
									if(endDate.compare(currentChange) < 0) {  // Change is after the sprint end date, skip and don't process anymore 
										////System.out.println("AfterEnd Bug : " + (Integer)currentRemainingEO.valueForKey("bugId") + " - "+ (NSTimestamp)currentRemainingEO.valueForKey("bugWhen") + " - " +  (String)currentRemainingEO.valueForKey("removed")+ " - " +  (String)currentRemainingEO.valueForKey("added") );
										break; // changes are ordered by date, so don't process anymore 
									}
									else if(currentChange.compare(startDate()) < 0) {  // change is before Sprint start 
										////System.out.println("Before Start Bug : " + (Integer)currentRemainingEO.valueForKey("bugId") + " - "+ (NSTimestamp)currentRemainingEO.valueForKey("bugWhen") + " - " +  (String)currentRemainingEO.valueForKey("removed")+ " - " +  (String)currentRemainingEO.valueForKey("added") );
										currentRemaining = new BigDecimal((String)currentRemainingEO.valueForKey("added"));
										for(int j = 0; j < sprintDays; j++) {
											hoursForBug[j] = currentRemaining.doubleValue();
										}
										
									}
									else {  // change occurred between start and end
										dateUtil = new DateUtil(startDate(),currentChange);
										int numdays = (int)dateUtil.days;
										////System.out.println(" Bug : " + (Integer)currentRemainingEO.valueForKey("bugId") + " - "+ (NSTimestamp)currentRemainingEO.valueForKey("bugWhen") + " - " +  (String)currentRemainingEO.valueForKey("removed")+ " - " +  (String)currentRemainingEO.valueForKey("added") );
										currentRemaining = new BigDecimal((String)currentRemainingEO.valueForKey("added"));

										for(int k = numdays; k < sprintDays; k++) {
											hoursForBug[k] = currentRemaining.doubleValue();
										}
									}
							// SS.compare(SE) - -1
							// SE.compare(SS) - 1
								}
							}
						 }
						 // Handle cas where bug was closed but 'hours remaining' was never set properly
						 else if((status.equals("CLOSED")) && (remainingTime.doubleValue() == 0.0)) {
							NSArray closedActivities = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(closedfs);
							int numClosed = closedActivities.count();
							if(numClosed > 0) {
								EOEnterpriseObject lastClosed = (EOEnterpriseObject)closedActivities.objectAtIndex(numClosed-1);
								NSTimestamp whenClosed = (NSTimestamp)lastClosed.valueForKey("bugWhen");
								

								dateUtil = new DateUtil(startDate(),whenClosed);
								int daysFromBeginingOfSprint = (int)dateUtil.days;
								////System.out.println("" + (Integer)eo.valueForKey("bugId") + " - days - " + daysFromBeginingOfSprint);

								if(daysFromBeginingOfSprint < 0) {
									daysFromBeginingOfSprint = 0;
								}
								for(int m = daysFromBeginingOfSprint; m < sprintDays; m++) {
									hoursForBug[m] = 0.0;
								}
							}
						
						 }
						
						////System.out.print("HoursForbugs - "  );
						for(int k = 0; k < sprintDays; k++) {
						////System.out.print(hoursForBug[k] + ", " );
							hoursRemain[k] += hoursForBug[k];
						}
						////System.out.print("\n\n"  );

							 
					 } 	// no estimate
							
				} // for each bug
		} // if hoursRemain
		
		return hoursRemain;
	}

    public void CreateSubPlot(String ptitle, int[] counts) {	
        //SubPlot theSub;
		String dateString;
        // Create Plots
		//theSub =  plot().newSubPlot();
        //theSub.setTitle(ptitle);
         NSTimestamp dailyDate= startDate(); //initialize date
		 TimeSeries series = new TimeSeries(ptitle);
				 
         int i = counts.length;
         for (int k = 0; k < counts.length; k++) {
			 i--;
             //theSub.addYItem(new Integer(counts[k]));  // count
			 dateString = "" + dailyDate.monthOfYear() + "/" + dailyDate.dayOfMonth() + "/" + dailyDate.yearOfCommonEra();
			 
			 //theSub.addXItem(dateString);
			 Day current = new Day(dailyDate.dayOfMonth(), dailyDate.monthOfYear(), dailyDate.yearOfCommonEra());
			 series.add(current, counts[k]);
             dailyDate = dailyDate.timestampByAddingGregorianUnits(0, 0, 1, 0, 0, 0); // next date
			 ////System.out.println("title - " + ptitle + " / count - " + counts[k] + " / date - " + dateString + " / k - " + k);
         }
		 ((TimeSeriesCollection)aDataset()).addSeries(series);  //JFreeChart
    }
	
    public void CreateSubPlotHours(NSTimestamp pStartTime, String ptitle, double[] counts) {	
		String dateString;
        // Create Plots
         NSTimestamp dailyDate= pStartTime; //initialize date
		 TimeSeries series = new TimeSeries(ptitle);
				 
         int i = counts.length;
         for (int k = 0; k < counts.length; k++) {
			 i--;
			 dateString = "" + dailyDate.monthOfYear() + "/" + dailyDate.dayOfMonth() + "/" + dailyDate.yearOfCommonEra();
			 
			 Day current = new Day(dailyDate.dayOfMonth(), dailyDate.monthOfYear(), dailyDate.yearOfCommonEra());
			 series.add(current, counts[k]);
             dailyDate = dailyDate.timestampByAddingGregorianUnits(0, 0, 1, 0, 0, 0); // next date
			 ////System.out.println("title - " + ptitle + " / count - " + counts[k] + " / date - " + dateString + " / k - " + k);
         }
		 ((TimeSeriesCollection)aDataset()).addSeries(series);  //JFreeChart
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
        return null;
    }


    public String selectedProject() {	
        return selectedProject;
    }
    public void setSelectedProject(String pValue) {
		//System.out.println("BurnDown.setSelectedProject() - " + pValue);
		if((selectedProject != null) && (!selectedProject.equals(pValue))) {
			selectedProject = pValue;
		}
    }
    public boolean isWrapped() {
        return isWrapped;
    }

    public void setIsWrapped(boolean newIsWrapped) {
        isWrapped = newIsWrapped;
    }

    public NSData graphImage() {
        Graph graph = (Graph)pageWithName("Graph");
        graph.takeValueForKey(graphSettings(),"settings");
        graph.takeValueForKey("findfixdata.graph","fileName");
        return graph.newImage();
    }
	
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
