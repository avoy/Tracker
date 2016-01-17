package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.math.BigDecimal;

public class SprintDetails extends WOComponent {
	private static final long serialVersionUID = 1L;		
	public NSArray storiesForCurrentSprint;
	public NSMutableArray childrenForCurrentSprint;
	public String selectedProject;
	public String currentProduct;
	public NSArray selectedProducts = new NSArray(new Object[] {"Client: Trusted Access - Desktop","Client: Trusted Access - Mobile", "Marble Services"});;
	public String sprint;
	public NSArray bugsForCurrentSprint;
	public NSArray milestoneDatesForProject;
	public NSMutableDictionary datesForMilestones;
	public NSTimestamp now;
    /** @TypeInfo Item */
    protected Item anEo;
	protected String currentSprint;
	protected boolean showHeader = false;
	

    public SprintDetails(WOContext context) {
        super(context);
		now = new NSTimestamp();

    }

	public NSArray storiesForCurrentSprint() {
        EOFetchSpecification fs;

		//System.out.println("\n\n============ SprintDetails.storiesForCurrentSprint() - null");
	//	if(storiesForCurrentSprint == null) {
			NSMutableArray qual = new NSMutableArray();

			//Stories
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Story'", null));
		
			//Sprint
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone ='" + sprint() + "'", null));
			
			if(currentProduct != null) {
				// Product
				qual.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + currentProduct + "'", null));
			}
			
			// Release
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version like '*" + selectedProject() + "*'", null));


			//Sort
			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),					
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			storiesForCurrentSprint= (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		//}
		return storiesForCurrentSprint;
    }
	
	public NSArray bugsForCurrentSprint() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();

		//if(bugsForCurrentSprint == null) {
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Bug'", null));
		
		if(currentProduct != null) {
			// Product 
			qual.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + currentProduct + "'", null));
		}
		
		// Release
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("version like '*" + selectedProject() + "*'", null));

		// Sprint
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ sprint() + "' ", null));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("bugId", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		bugsForCurrentSprint = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		//}
		return bugsForCurrentSprint;
    }	
	
	public NSArray childrenForCurrentSprint() {
	//	if(childrenForCurrentSprint == null) {
			childrenForCurrentSprint  = new NSMutableArray();
			// For all stories in  the current sprint
			Enumeration enumer = storiesForCurrentSprint().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item story = (Item)enumer.nextElement();
				// get all children (bugs, tasks, etc.)
				//childrenForCurrentSprint.addObjectsFromArray(story.filterSameVersion(story.allChildren()));
				childrenForCurrentSprint.addObjectsFromArray(story.allChildren());

			}
		//}
		return childrenForCurrentSprint;
	}
	
	public NSArray verifiedAndClosedChildrenForCurrentSprint() {
        NSMutableArray qual = new NSMutableArray();

		// 'Closed items - Closed or Verified'
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));

		return (NSArray)EOQualifier.filteredArrayWithQualifier(childrenForCurrentSprint(), new EOAndQualifier(qual));
    }
	
	public String percentCompleteChildrenForCurrentSprint() {
		double closed = 0.0;
		double all = 0.0;
	
		NSArray children = childrenForCurrentSprint();
		NSArray closedchildren = verifiedAndClosedChildrenForCurrentSprint();
		
		if((children != null) && (closedchildren != null)) {
			closed = (double)closedchildren.count();
			all = (double)children.count();
		}
		

        return percentCompleteString(((double)closed/(double)all) * 100);
    }

	public String percentCompleteString(double pVal) {
        String returnVal = null;
        if(Double.isNaN(pVal)) {
                returnVal = "---";
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(pVal)) + "%";
            }
            catch(Exception e) {
                System.err.println("ReleasePlan.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }
	
    public NSTimestamp startDateForCurrentSprint() {
		return dateForMilestone(sprint(), "start");
	}

    public NSTimestamp endDateForCurrentSprint() {
		return dateForMilestone(sprint(), "end");
	}
	public String weekdaysRemainingInSprint() {
		String daysRemain = "NA";
		
		NSTimestamp end = endDateForCurrentSprint();
		if(end != null) {
			NSTimestamp now = new NSTimestamp();

			DateUtil dateUtil = new DateUtil(now, end);
			int weekdays = (int)dateUtil.weekdays();
			if(weekdays > 0) {
				daysRemain = "" + weekdays;
			}
			
			/*
			System.out.println("weekdays - " + weekdays);

			int hours = (int)dateUtil.hours;
			System.out.println("weekdayhours - " + hours);
			int minutes = (int)dateUtil.minutes;
			System.out.println("weekdayminutes - " + minutes);
			int seconds = (int)dateUtil.seconds;
			System.out.println("weekdayseconds - " + seconds);
			*/
		}
		return daysRemain;
	}
	public String daysRemainingInSprint() {
		String daysRemain = "NA";
		
		NSTimestamp end = endDateForCurrentSprint();

		if(end != null) {
		//if(true) {
			//GregorianCalendar curr = new GregorianCalendar();
			//NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);

			NSTimestamp now = new NSTimestamp();
			//int year = curr.get(GregorianCalendar.YEAR);
			//int month = curr.get(GregorianCalendar.MONTH);
			//int day = curr.get(GregorianCalendar.DAY_OF_MONTH);
			//NSTimestamp morning = new NSTimestamp(year, month+1, day, 0, 0, 0, tz);

			//SimpleDateFormat theFormat = new SimpleDateFormat("MMM/dd/yyyy H:m:s");
			//System.out.println("Now - " + theFormat.format(now));

			DateUtil dateUtil = new DateUtil(now, end);
			int days = (int)dateUtil.calendardays();
			if(days > 0) {
				daysRemain = "" + days;
			}

			//int hours = (int)dateUtil.hours;
			/*
			System.out.println("hours - " + hours);
			int minutes = (int)dateUtil.minutes;
			System.out.println("minutes - " + minutes);
			int seconds = (int)dateUtil.seconds;
			System.out.println("seconds - " + seconds);
			*/
			
		}
		return daysRemain;
	}

	
    public NSTimestamp dateForMilestone(String pMilestone, String dateType) {
		NSTimestamp returnVal = null;
		//System.out.println("pMilestone - " + pMilestone);
		//System.out.println("dateType - " + dateType);
		EOEnterpriseObject milestone = (EOEnterpriseObject)datesForMilestones().objectForKey(pMilestone);

		if((dateType != null) && (dateType.equals("end"))) {
			if(milestone != null) {
				returnVal =  (NSTimestamp)milestone.valueForKey("endDate");
			}
		}
		else if((dateType != null) && (dateType.equals("planned"))) {
			if(milestone != null) {
				returnVal =  (NSTimestamp)milestone.valueForKey("plannedDate");
			}
		}
		else if((dateType != null) && (dateType.equals("forecast"))) {
			if(milestone != null) {
				returnVal =  (NSTimestamp)milestone.valueForKey("forecastDate");
			}
		}
		else if((dateType != null) && (dateType.equals("actual"))) {
			if(milestone != null) {
				returnVal =  (NSTimestamp)milestone.valueForKey("actualDate");
			}
		}
		else {
			if(milestone != null) {
				returnVal =  (NSTimestamp)milestone.valueForKey("startDate");
			}
		}
	  return returnVal;
    }
	public NSMutableDictionary datesForMilestones() {
		if(datesForMilestones == null) {
			datesForMilestones = new NSMutableDictionary();
			if(milestoneDatesForProject() != null) {
				Enumeration enumer = milestoneDatesForProject().objectEnumerator();
				while(enumer.hasMoreElements()) {
					EOEnterpriseObject eo = (EOEnterpriseObject)enumer.nextElement();
					datesForMilestones.setObjectForKey(eo, (String)eo.valueForKey("milestone"));
				}
			}
		}
		return datesForMilestones;
	
	}
    public NSArray milestoneDatesForProject() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
			//System.out.println("\n\n============ SprintDetails.milestoneDatesForProject()");
        if((milestoneDatesForProject == null) && (selectedProject() != null)) {
			//System.out.println("\n\n============ SprintDetails.milestoneDatesForProject()" + selectedProject);

			// Tasks - Defects, Enhancements, WorkItems
			bindings = new NSDictionary(new Object[] {selectedProject()}, new Object[] { "version"});
			fs = EOFetchSpecification.fetchSpecificationNamed( "milestoneDates", "MilestoneDates").fetchSpecificationWithQualifierBindings( bindings );
			milestoneDatesForProject =  (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

        }
        return milestoneDatesForProject;
    }
	public boolean isCurrentSprint() {
		boolean returnVal = false;		
		
		if(sprint().equals(currentSprint()))  {// Need to check this using dates - hardcode for now
			returnVal = true;
		}
	
		return returnVal;
	}

	public String currentSprint() {
		String current = null;
		
		//if(currentSprint == null) {
			currentSprint = "";  // will not be null again
			Enumeration enumer = milestoneDatesForProject().objectEnumerator();
			while(enumer.hasMoreElements()) {
				MilestoneDates temp = (MilestoneDates)enumer.nextElement();
				current = (String)temp.valueForKey("milestone");
				NSTimestamp start = (NSTimestamp)temp.valueForKey("startDate");
				NSTimestamp end = (NSTimestamp)temp.valueForKey("endDate");
				if((start != null) && (end != null)) {
					if((start.compare(now) == -1) && (end.compare(now) == 1)) {
						currentSprint = current;
						break;
					}
				
				}
			}
	//	}
		//System.out.println("currentSprint - " + currentSprint);
		return currentSprint;
	}


	public String labelForSprint() {
		String returnVal;
		
		if(sprint().equals("S1"))
			returnVal = "Sprint 1";
		else if(sprint().equals("S2"))
			returnVal = "Sprint 2";
		else if(sprint().equals("S3"))
			returnVal = "Sprint 3";
		else if(sprint().equals("S4"))
			returnVal = "Sprint 4";
		else if(sprint().equals("S5"))
			returnVal = "Sprint 5";
		else if(sprint().equals("S6"))
			returnVal = "Sprint 6";
		else if(sprint().equals("S7"))
			returnVal = "Sprint 7";
		else if(sprint().equals("S8"))
			returnVal = "Sprint 8";
		else if(sprint().equals("S9"))
			returnVal = "Sprint 9";
		else if(sprint().equals("S10"))
			returnVal = "Sprint 10";
		else if(sprint().equals("S11"))
			returnVal = "Sprint 11";
		else if(sprint().equals("S12"))
			returnVal = "Sprint 12";
		else if(sprint().equals("S13"))
			returnVal = "Sprint 13";
		else if(sprint().equals("S14"))
			returnVal = "Sprint 14";
		else if(sprint().equals("---"))
			returnVal = "Backlog";
		else
			returnVal = sprint();
			
		return returnVal;

	}
	public NSArray tasksForCurrentSprint() {
		NSMutableArray tasks = new NSMutableArray();
		Enumeration enumer = storiesForCurrentSprint().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item currentStory = (Item)enumer.nextElement();
			tasks.addObjectsFromArray(currentStory.tasks());
		}

		return (NSArray)tasks;
	
	}
	
	public boolean isHoursInCurrentSprint() {
		boolean returnVal = false;
		Enumeration enumer = tasksForCurrentSprint().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item task = (Item)enumer.nextElement();
			BigDecimal currEstimate = (BigDecimal)task.valueForKey("estimatedTime");
			if(currEstimate.doubleValue()  > 0.0) {  
				returnVal = true;
				break;
			}
		}
		return returnVal;
	}

	public boolean isStatusWhiteboard() {
		boolean returnVal = false;
		
		String aVal = (String)anEo.valueForKey("statusWhiteboard");
		if((aVal != null) && (!aVal.equals(""))) {
			returnVal = true;
		}
		return returnVal;
	}
	
   public String rightArrowName() {
		return labelForSprint() + "RightImage";
   }
   
   public String downArrowName() {
		return labelForSprint() + "DownImage";
   }
  
   public String downToggleStyle() {
		String returnVal = "";
		//if(isCurrentSprint() == false) {
		//	returnVal = "display:none;";
		//}
		return returnVal;
	}
	
	public String rightToggleStyle() {
		String returnVal = "";
		//if(isCurrentSprint() == true) {
			returnVal = "display:none;";
		//}
		return returnVal;

	}	
	public String bgColorForState() {
		String returnVal = "#ffffff";
		String state =  (String)anEo.valueForKey("state");
		
		if(state.equals("LATE")) {
			returnVal = "#ff0000";				// Red
		}
		else if(state.equals("RISK-HIGH")) {  
			returnVal = "#ff9900";				// Orange
		}
		else if(state.equals("RISK-LOW")) {
			returnVal = "#ffff00";				 // Yellow
		}
		else if(state.equals("COMPLETE")) { 
			returnVal = "#00ff00";				// Green
		}
		
		return returnVal;
	}	

	// Navigation
    public BurnDown goSprintBugMetrics()
    {
        BurnDown nextPage = (BurnDown)pageWithName("BurnDown");
		nextPage.takeValueForKey("count", "chartType");
		if(startDateForCurrentSprint() != null) {
			nextPage.takeValueForKey(startDateForCurrentSprint(), "startDate");
		}
		if(endDateForCurrentSprint() != null) {
			nextPage.takeValueForKey(endDateForCurrentSprint(), "endDate");
		}

		nextPage.takeValueForKey(bugsForCurrentSprint(), "itemsToGraph");

        // Initialize your component here

        return nextPage;
    }
    public BurnDown goBurnDown()
    {
        BurnDown nextPage = (BurnDown)pageWithName("BurnDown");
		nextPage.takeValueForKey("hours", "chartType");
		if(startDateForCurrentSprint() != null) {
			nextPage.takeValueForKey(startDateForCurrentSprint(), "startDate");
		}
		if(endDateForCurrentSprint() != null) {
			nextPage.takeValueForKey(endDateForCurrentSprint(), "endDate");
		}

		nextPage.takeValueForKey(tasksForCurrentSprint(), "itemsToGraph");

        // Initialize your component here

        return nextPage;
    }
	
    public TasksForStory goTaskDetails()
    {
        TasksForStory nextPage = (TasksForStory)pageWithName("TasksForStory");
		anEo.invalidateAllChildren();  // just to clear caching
		nextPage.takeValueForKey(anEo, "parentItem");
		nextPage.takeValueForKey(context().page(), "nextPage");
       

        return nextPage;
    }
	
    public Comment goDisplayComment()
    {
        Comment nextPage = (Comment)pageWithName("Comment");

        nextPage.setAComment((String)anEo.valueForKey("statusWhiteboard"));

        return nextPage;
    }
	
	public void setReleaseStartsWith(String aVal) {
		NSArray versions;
		Session s = (Session)session();
		
		versions = s.rowsForSql("select distinct(value) from versions where value like '" + aVal + "%'");
		
		if((versions != null) && (versions.count() > 0)) {
			setSelectedProject((String)versions.objectAtIndex(0));
		}
		else {
			setSelectedProject("Amsterdam (4.10 - Desktop)");
		}
	}	
	
	// Accessors
	public String selectedProject() { return selectedProject;}
	public void setSelectedProject(String aVal) {selectedProject = aVal;}
	
	public String currentProduct() { return currentProduct;}
	public void setProducts(String aVal) {
		if(aVal.equals("all")) {
			setSelectedProducts( new NSArray(new Object[] {"Client: Trusted Access - Desktop","Client: Trusted Access - Mobile", "Marble Services"}));
			setShowHeader(true);
		}
		else {
			setSelectedProducts( new NSArray(new Object[] {aVal}));
		}
		
	}
	public NSArray selectedProducts() { return selectedProducts;}
	public void setSelectedProducts(NSArray aVal) {selectedProducts = aVal;}
	public String sprint() { return sprint;}
	public void setSprint(String aVal) {sprint = aVal;}
	public boolean showHeader() { return showHeader;}
	public void setShowHeader(boolean aVal) {showHeader = aVal;}


}
