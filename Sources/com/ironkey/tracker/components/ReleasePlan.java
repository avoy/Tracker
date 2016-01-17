package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;

import java.util.Enumeration;
import java.math.BigDecimal;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class ReleasePlan extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup displayGroup;
    public NSArray issues;    
    public NSArray storiesForSelectedRelease;
    public NSArray bugsAndEnhancementsForSelectedRelease;
    public NSArray openBugsAndEnhancementsForSelectedRelease;
    public NSArray resolvedbugsAndEnhancementsForSelectedRelease;
    public NSArray verifiedAndClosedBugsAndEnhancementsForSelectedRelease;
    public NSArray supportEscalationBugsAndEnhancementsForSelectedRelease;
    public NSArray customerReportedBugsAndEnhancementsForSelectedRelease;
    public NSArray betaBugsAndEnhancementsForSelectedRelease;
    public NSArray ocmsForSelectedRelease;
    public NSArray taserBugsAndEnhancementsForSelectedRelease;
    public String label;
    public String aRow = "Priority";
    public String aColumn = "Milestone";
    public boolean hideController = false;
    public String projectLimitCharacter = "A";
    public NSArray itemProducts;
    public NSArray potentialProjects;
    protected NSArray selectedItemProducts = new NSArray(new Object[] {"Media Platform"});
    //public NSArray potentialInclusions = new NSArray(new Object[] {"Include 'IronKey Services'"});
    //public NSArray selectedInclusions = new NSArray(new Object[] {"Include 'IronKey Services'"});
    /** @TypeInfo Item */
    public Item anEo;
    public Object aKey;
    public String aString;
    public String sprintKey;
    public String selectedProject;
    //protected MilestoneDates aMilestone;
    public String selectedMilestone;
    public NSMutableDictionary storiesForSprints;
    public NSMutableDictionary childrenForSprints;
    public boolean collapsibleView = true;
    public NSArray milestoneDatesForProject;
    public NSMutableDictionary datesForMilestones;
    public NSTimestamp now;
    public String currentSprint;
    public boolean showBySprint = false;
    public boolean isPerson = false;
    public boolean isStory = true;
    public boolean isList = false;
    public NSTimestamp projectStartDate;
    public SprintPersonStatus personStatus;
    public TopicStatus aTopicStatus;
    public String topicString;
    public String aPerson;
    public String anchor;
	public boolean handleNamedAnchor = false;
	
    public ReleasePlan(WOContext aContext) {
        super(aContext);
		//System.out.println("\n\n============ ReleasePlan.ReleasePlan()");
		
        Session mysession = (Session)session();
		getDefaultsFromCookies();
        selectedProject = mysession.selectedProject();
	    //openBugsAndEnhancementsForSelectedRelease();  // need to ensure that displayGroup gets set properly
		storiesForSelectedRelease();
		now = new NSTimestamp();
		//bugsAndEnhancementsSortedByFoundIn();
    }

	//public boolean isIronKeyServices() {
	//	return selectedInclusions.containsObject("Include 'IronKey Services'")? true: false;
	//}
	
	public void takeValuesFromRequest( WORequest aRequest, WOContext aContext) {
//System.out.println("ReleasePlan.takeValuesFromRequest()");
        try {
			/*
			System.out.println("ReleasePlan.takeValuesFromRequest()") ;
			NSDictionary theDict = aRequest.formValues();
			Enumeration enum1 = theDict.keyEnumerator();
			while(enum1.hasMoreElements()) {
				String theKey = (String)enum1.nextElement();
				System.out.println("theKey = " + theKey + " = " + theDict.objectForKey(theKey));
			}
			*/       	
			NSArray productIndices = (NSArray)aRequest.formValuesForKey("products");
			if((aRequest != null) && (productIndices != null)) {			
				NSArray products = convertIndicesToProducts(productIndices);
				if(!products.equals(selectedItemProducts())) {
					setSelectedItemProducts(products);
					potentialProjects = null;
					//System.out.println("about to take values from request");
					//super.takeValuesFromRequest(aRequest, aContext);
				}
				else {
					super.takeValuesFromRequest(aRequest, aContext);
				}
			}
			else{
				// set all keys and submit normally
				super.takeValuesFromRequest(aRequest, aContext);
			}
        }
        catch(Exception e) {
            System.err.println("++++  Project.takeValuesFromRequest() - " + e);
        }
    }
	
	public void appendToResponse(WOResponse response, WOContext context) {
		if(handleNamedAnchor == true) {
			if(anchor == null) {
				anchor = "Bugs_Enhancements";
			}
			response.setHeader(context.componentActionURL() + "#" + anchor, "location");
			response.setHeader("text/html", "content-type");
			response.setHeader("0", "content-length");
			response.setStatus(302);
			anchor = null;
			handleNamedAnchor = false;
		} else {
			super.appendToResponse(response, context);
		}
	} // appendToResponse

	public SprintPersonStatus personStatus() {
		//System.out.println("ReleasePlan.personStatus()");
		if(personStatus == null) {
			
			personStatus = new SprintPersonStatus(selectedProject(), session().defaultEditingContext(), selectedItemProducts());
			//System.out.println("personStatus - " + personStatus.toString());
		}

		return personStatus;
	}
	public NSArray peopleForCurrentSprint() {	
		//System.out.println("ReleasePlan.peopleForCurrentSprint()");
		
		return (NSArray)personStatus().topicKeysForSprint(sprintKey);

	}
	
	public TopicStatus topicForCurrentPersonForCurrentSprint() {
		//System.out.println("ReleasePlan.topicForCurrentPersonForCurrentSprint()");
		return personStatus().topicForKeyForSprint(aPerson, sprintKey);
	}

	
	public NSArray convertIndicesToProducts(NSArray pIndices) {
		//System.out.println("ReleasePlan.convertIndicesToProducts()");
		
		NSMutableArray products = new NSMutableArray();
		Enumeration enum1 = pIndices.objectEnumerator();
		while(enum1.hasMoreElements()) {
		
		   String theKey = (String)enum1.nextElement();
		   String aProduct = (String)itemProducts.objectAtIndex(Integer.parseInt(theKey));

		   products.addObject(aProduct);
		}
		return (NSArray)products;
	}
	
	
	public NSArray potentialProjects() {
		
		Session s;
		String productIdString = "(";
		String sqlString;
		boolean success = false;
		int prodId = -1;
		
		if(potentialProjects == null) {
			s = (Session)session();

				productIdString = selectedProductIds();
				// Also exclude versions that start with '_'

				//sqlString = "select distinct(value) from versions where product_id in " + productIdString + " and value>'" + projectLimitCharacter +"' and value not like '\\_%' order by value";
				//sqlString = "select distinct(value) from versions where product_id in " + productIdString + " and value>'" + projectLimitCharacter +"' order by value";
				sqlString = "select distinct(value) from versions where product_id in " + productIdString  +" order by value";
				
				potentialProjects =  (NSArray)s.rowsForSql(sqlString);
				NSMutableArray temp = new NSMutableArray(potentialProjects);
				/*
				if(productIdString.contains(2) == true) {
					NSMutableArray temp = new NSMutableArray(potentialProjects);
					//temp.insertObjectAtIndex("Dolomites (Patch9)", 0);
					temp.removeObject("Dolomites (Accenture)");
					temp.removeObject("Dolomites (General)");
					temp.removeObject("Dolomites (Patch1)");
					temp.removeObject("Dolomites (Patch2)");
					temp.removeObject("Dolomites (Patch3)");
					temp.removeObject("Dolomites (Patch4)");
					temp.removeObject("Dolomites (Patch5)");
					temp.removeObject("Dolomites (Patch6)");
					temp.removeObject("Dolomites (Patch7)");
					temp.removeObject("Dolomites (Patch8)");
					temp.removeObject("Eiger");
					temp.removeObject("Guns-n-Roses (Q2_Sustaining)");
					temp.removeObject("Hendrix H2-1");
					potentialProjects = (NSArray)temp;
				}
				*/

			potentialProjects = (NSArray)temp;
		}
		
		return potentialProjects;
	}
	/*
	    public NSArray projects() {
        NSMutableArray values;

        if(projects == null) {
            Session s = (Session)session();
            //values =  (NSMutableArray)s.rowsForSql("select distinct(version)from bugs where version>='"+ s.supportProject+ "'");
            values =  (NSMutableArray)s.rowsForSql("select distinct(value)from versions where product_id in (2,3,14,17,18) and value>='"+ s.supportProject+ "' order by value");
            //values.removeObject("_Future");
            values.removeObject("OTP-Demo");
            projects = new NSArray(values);
        }
        return projects;
    }
	*/

	// need to cache at session or application level
	public int idForProduct(String pProductName) {
		//System.out.println("\n\n============ ReleasePlan.idForProduct()");

		int returnVal = -1;
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select id from products where name='"+ pProductName+ "'");
		if(values.count() > 0) {
			Short val = (Short)values.objectAtIndex(0);
			returnVal =  val.intValue();
		}
		return returnVal;
    }
	
	public NSMutableDictionary storiesForSprints() {
		//System.out.println("ReleasePlan.storiesForSprints()");
		
		NSMutableArray storiesForSprint;
		
		if(storiesForSprints == null) {
			storiesForSprints = new NSMutableDictionary();
			NSArray stories = storiesForSelectedRelease();
			if(stories != null) {
				Enumeration enumer = stories.objectEnumerator();
				while(enumer.hasMoreElements()) {
					EOEnterpriseObject eo = (EOEnterpriseObject)enumer.nextElement();
					String sprint = (String)eo.valueForKey("targetMilestone");
					storiesForSprint = (NSMutableArray)storiesForSprints.valueForKey(sprint);
					if(storiesForSprint == null) {
						storiesForSprint = new NSMutableArray();
						storiesForSprint.addObject(eo);
						storiesForSprints.setObjectForKey(storiesForSprint, sprint);
					}
					else {
						storiesForSprint.addObject(eo);
					}
				}
			}
			
		}

		return storiesForSprints;
	}
	
	public NSArray storiesForCurrentSprint() {
		//System.out.println("ReleasePlan.storiesForCurrentSprint() - " + sprintKey);
		
		return (NSArray)storiesForSprints.objectForKey(sprintKey);
	}
	// Adding an Other Bugs Dictionary
	public NSArray storiesPlusForCurrentSprint() {
		NSMutableArray temp = new NSMutableArray();
		temp.addObjectsFromArray((NSArray)storiesForSprints.objectForKey(sprintKey));
		
		// Add a Dictionary for bugs in the current Sprint that are not a part of stories
		NSMutableDictionary tempDict = new NSMutableDictionary();
		//tempDict.setObjectForKey(bugsAndEnhancementsNotInStoryForSprint(sprintKey), "Other Bugs in " + sprintKey);
		tempDict.setObjectForKey(bugsAndEnhancementsNotInStoryForSprint(sprintKey), "Other Bugs in " + labelForSprintKey());
		temp.addObject(tempDict);
		return (NSArray)temp;
	}
	public boolean isItem() {
			//if((className.equals("com.webobjects.eocontrol.EOGenericRecord")) || (className.equals("er.extensions.ERXGenericRecord"))) {
		boolean returnVal = false;
		String className = aKey.getClass().getName();	
		//System.out.println("className - " + className);	
		if(className.equals("com.ironkey.tracker.Item")) {
			returnVal = true;
		}
		//else if(className.equals("com.webobjects.foundation.NSMutableDictionary")) {

		return returnVal;
	}
	
	//storyPlus was added because of the 'Other Bugs not in a story'
	public String storyPlus_Description() {
		String returnVal = "";
		if(isItem() == true) {
			returnVal = (String)((Item)aKey).valueForKey("shortDesc");
		}
		else {
			NSArray tempArray = (NSArray)((NSDictionary)aKey).allKeys();
			returnVal = (String)tempArray.objectAtIndex(0);
		}

		return returnVal;
	}
	public String storyPlus_Status() {
		String returnVal = "---";
		if(isItem() == true) {
			//returnVal = (String)((Item)aKey).valueForKey("bugStatus");
			returnVal = (String)((Item)aKey).valueForKey("state");
		}
		else if( storyPlus_ClosedCount() < storyPlus_AllCount()) {   // There are bugs open
			try {
				int days = Integer.parseInt(weekdaysRemainingInSprint());
				if(days <= 0) {
					returnVal = "Late";
				}
				else if(days < 2) {
					returnVal = "Risk-High";
				}
				else if(days <= 3) {
					returnVal = "Risk-Low";
				}
				else {
					returnVal = "In Progress";
				}
			}
			catch(Exception e) {
				//System.err.println("ReleasePlan.storyPlus_Status() - exception - " + e);
			}
		}
		else if((storyPlus_AllCount() > 0) && (storyPlus_ClosedCount() == storyPlus_AllCount())) {  // All bugs complete
			returnVal = "Complete";
		}
		return returnVal;
	}
	
	public int storyPlus_ClosedCount() {
		int returnVal = 0;
		if(isItem() == true) {
			returnVal = (int)((NSArray)((Item)aKey).allClosedChildren()).count();
		}
		else {
			NSArray tempArray = (NSArray)((NSDictionary)aKey).objectForKey(storyPlus_Description());
			returnVal = closedInArray(tempArray).count();
		}

		return returnVal;
	}
	public int storyPlus_AllCount() {
		int returnVal = 0;
		if(isItem() == true) {
			returnVal = (int)((NSArray)((Item)aKey).valueForKey("allChildren")).count();
		}
		else {
			NSArray tempArray = (NSArray)((NSDictionary)aKey).objectForKey(storyPlus_Description());
			returnVal = tempArray.count();
		}

		return returnVal;
	}
	
	public String storyPlus_percentCompleteCount() {
		double all = (double)storyPlus_AllCount();
		double closed = (double)storyPlus_ClosedCount();

        return percentCompleteString(((double)closed/(double)all) * 100);
    }
	
	public String storyPlus_percentCompleteHours() {
		double all = (double)storyPlus_allHours();
		double closed = (double)storyPlus_hoursWorked();

        return percentCompleteString(((double)closed/(double)all) * 100);
    }
	
	public NSArray closedInArray(NSArray pArray) {
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(pArray, new EOOrQualifier(qual)) ;
	}
	
	public String urlForBugsNotInCurrentSprint() {
		return listURLForArray((NSArray)((NSDictionary)aKey).objectForKey(storyPlus_Description()));
	}

	public int storyPlus_hoursWorked() {
		int returnVal = 0;
		if(isItem() == true) {
			returnVal = (int)((Item)aKey).allChildrenHoursWorked();
		}
		else {
			NSArray tempArray = (NSArray)((NSDictionary)aKey).objectForKey(storyPlus_Description());
			returnVal = hoursWorkedInArray(tempArray);
		}

		return returnVal;
	}
	public int storyPlus_allHours() {
		int returnVal = 0;
		if(isItem() == true) {
			returnVal = (int)((Item)aKey).allChildrenCurrentEstimate();
		}
		else {
			NSArray tempArray = (NSArray)((NSDictionary)aKey).objectForKey(storyPlus_Description());
			returnVal = currentEstimateInArray(tempArray);
		}

		return returnVal;
	}

	public int hoursWorkedInArray(NSArray pArray) {
		int hoursWorked = 0;
		Enumeration enumer = pArray.objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			hoursWorked += (Integer)aTask.valueForKey("numHoursWorked");
		}
		return hoursWorked;
	}
	public int currentEstimateInArray(NSArray pArray) {
		int currEst = 0;
		Enumeration enumer = pArray.objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			currEst += (Integer)aTask.valueForKey("currentEstimate");
		}
		return currEst;
	}

	public NSMutableDictionary childrenForSprints() {
		//System.out.println("ReleasePlan.childrenForSprints()");
		
		NSMutableArray children;
		if(childrenForSprints == null) {
			childrenForSprints = new NSMutableDictionary();
			Enumeration enumer1 = sprintKeys().objectEnumerator();
			// For all Sprints
			while(enumer1.hasMoreElements()) {
				String key = (String)enumer1.nextElement();
				
				children  = new NSMutableArray();
				// For all stories in  the current sprint
				Enumeration enumer = ((NSArray)storiesForSprints().objectForKey(key)).objectEnumerator();
				while(enumer.hasMoreElements()) {
					Item story = (Item)enumer.nextElement();
					// get all children (bugs, tasks, etc.)
					//children.addObjectsFromArray(story.filterSameVersion(story.allChildren()));
					children.addObjectsFromArray(story.allChildren());
				}
				childrenForSprints.setObjectForKey(children,key);
			}
		}
		return childrenForSprints;
	}
	
	public NSArray childrenForCurrentSprint() {
		return (NSArray)childrenForSprints().objectForKey(sprintKey);
	}
	
	
	public NSArray verifiedAndClosedChildrenForCurrentSprint() {
        NSMutableArray qual = new NSMutableArray();

		// 'Closed items - Closed or Verified'
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));

		Object orderings[]={
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

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

	public NSArray tasksForCurrentSprint() {
		NSMutableArray tasks = new NSMutableArray();
		Enumeration enumer = storiesForCurrentSprint().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item currentStory = (Item)enumer.nextElement();
			//tasks.addObjectsFromArray(currentStory.tasks());
			tasks.addObjectsFromArray(currentStory.tasksAndQATasks());
			tasks.addObjectsFromArray(bugsForCurrentSprint());
		}

		return (NSArray)tasks;
	
	}
	
	public NSArray bugsForCurrentSprint() {
        NSMutableArray qual = new NSMutableArray();
		NSArray bugs = new NSArray();
					
		//Bugs and Enhancements
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Bug'", null));
		
		// Release
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

		// Sprint
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ sprintKey + "' ", null));

		bugs = (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
		//System.out.println("Number of bugs in sprint '" + sprintKey + "' - " + bugs.count());
		return bugs;
    }


	
	public NSArray sprintKeys() {
		NSArray sprintValues= new NSArray(new Object[] { "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14", "---", "Triage"});

		return sortItemsAsDefined((NSArray)storiesForSprints().allKeys(), sprintValues);
	}
	public boolean isCurrentSprint() {
		boolean returnVal = false;		
		
		if(sprintKey.equals(currentSprint()))  {// Need to check this using dates - hardcode for now
			returnVal = true;
		}
	
		return returnVal;
	}
	
   public String rightArrowName() {
		return labelForSprintKey() + "RightImage";
   }
   public String downArrowName() {
		return labelForSprintKey() + "DownImage";
   }
   
	public String downToggleStyle() {
		String returnVal = "";
		if(isCurrentSprint() == false) {
			returnVal = "display:none;";
		}
		return returnVal;
	}
	public String rightToggleStyle() {
		String returnVal = "";
		if(isCurrentSprint() == true) {
			returnVal = "display:none;";
		}
		return returnVal;

	}
	public String currentSprint() {
		String current = null;
		
		if(currentSprint == null) {
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
		}
		//System.out.println("currentSprint - " + currentSprint);
		return currentSprint;
	}

	public String labelForSprintKey() {
		String returnVal;
		
		if(sprintKey.equals("S1"))
			returnVal = "Sprint 1";
		else if(sprintKey.equals("S2"))
			returnVal = "Sprint 2";
		else if(sprintKey.equals("S3"))
			returnVal = "Sprint 3";
		else if(sprintKey.equals("S4"))
			returnVal = "Sprint 4";
		else if(sprintKey.equals("S5"))
			returnVal = "Sprint 5";
		else if(sprintKey.equals("S6"))
			returnVal = "Sprint 6";
		else if(sprintKey.equals("S7"))
			returnVal = "Sprint 7";
		else if(sprintKey.equals("S8"))
			returnVal = "Sprint 8";
		else if(sprintKey.equals("S9"))
			returnVal = "Sprint 9";
		else if(sprintKey.equals("S10"))
			returnVal = "Sprint 10";
		else if(sprintKey.equals("S11"))
			returnVal = "Sprint 11";
		else if(sprintKey.equals("S12"))
			returnVal = "Sprint 12";
		else if(sprintKey.equals("S13"))
			returnVal = "Sprint 13";
		else if(sprintKey.equals("S14"))
			returnVal = "Sprint 14";
		else if(sprintKey.equals("---"))
			returnVal = "Backlog";
		else
			returnVal = sprintKey;
			
		return returnVal;

	}
	
	public NSMutableArray sortItemsAsDefined(NSArray unorderedArray, NSArray orderingDefinitionArray) {
        NSMutableArray sortedArray = new NSMutableArray();
        boolean returnVal = false;
        Enumeration enumer = orderingDefinitionArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            Object value = (Object)enumer.nextElement();
            if(unorderedArray.containsObject(value)) {
                sortedArray.addObject(value);
            }
        }
        return sortedArray;
    }

	
	public void openBugsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;
		
		//System.out.println("\n\n============ ReleasePlan.openBugsForSelectedRelease()");
		

		//Bugs and Enhancements
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
		//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Enhancement'", null));
		//qual.addObject(new EOOrQualifier(qual1));
		
        // Release
        qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

		// 'Open items'
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));


		Object orderings[]={
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),				
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		((EODatabaseDataSource)displayGroup.dataSource()).setFetchSpecification(fs);
		displayGroup.fetch();
    }

	
	public NSArray storiesForSelectedRelease() {
        EOFetchSpecification fs;
		if(storiesForSelectedRelease == null) {
			NSMutableArray qual = new NSMutableArray();
			NSMutableArray qual1 = new NSMutableArray();

			//Stories
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Story'", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareAscending),
				
					//EOSortOrdering.sortOrderingWithKey("relNum", EOSortOrdering.CompareAscending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),					
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			storiesForSelectedRelease= (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

		}
		return storiesForSelectedRelease;
    }	
	
	public NSArray issues() {
        EOFetchSpecification fs;
		if(issues == null) {
			NSMutableArray qual = new NSMutableArray();
			NSMutableArray qual1 = new NSMutableArray();

			//Stories
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Issue'", null));
			
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareCaseInsensitiveAscending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),					
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			issues = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return issues;
    }
	
	// only get top most stories (epics or stories with no parents)
	public NSArray logicalStoriesForRelease() {
		//System.out.println("ReleasePlan.logicalStoriesForRelease()");
	
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer = storiesForSelectedRelease().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item story = (Item)enumer.nextElement();
            if(story.hasParents() == true) {
                boolean storyIsChildStory = false;
                // no checking to ensure these items are in the current release
                //Item epic = (Item)((NSArray)story.topMostParents()).objectAtIndex(0);
                Enumeration enumer2 = story.topMostParents().objectEnumerator();
                while(enumer2.hasMoreElements()) {
                        Item epic = (Item)enumer2.nextElement();
                        String epicType = (String)epic.valueForKey("type");
                        if((epicType.equals("Epic")) || (epicType.equals("Story"))) {
                                storyIsChildStory = true;
                                if(tempArray.containsObject(epic) == false) {
                                        tempArray.addObject(epic);
                                }
                                break;
                        }
                }
                if(storyIsChildStory == false) {
                        if(tempArray.containsObject(story) == false) {
                                tempArray.addObject(story);
                        }
                }
            }
			else if(tempArray.containsObject(story) == false) {
				tempArray.addObject(story);
				//System.out.println("============ ReleasePlan.logicalStoriesForRelease() - " + story.valueForKey("shortDesc"));

			}
		}
		
		//System.out.println("============ ReleasePlan.logicalStoriesForRelease() - " + tempArray.count());
		Object orderings[]={
			//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(tempArray, new NSArray(orderings));
	}
	
	public NSArray storiesForSelectedReleaseSortedBySprint() {
		NSMutableArray backlog = new NSMutableArray();
		NSMutableArray sorted = new NSMutableArray();
		Enumeration enumer = storiesForSelectedRelease().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item story = (Item)enumer.nextElement();
			String sprint = (String)story.valueForKey("targetMilestone");
			if(sprint.equals("---")) {
				backlog.addObject(story);
			}
			else {
				sorted.addObject(story);
			}
		}
		sorted.addObjectsFromArray(backlog);
		return (NSArray)sorted;
	}
	


	public NSArray bugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();        
        EOQualifier qualifier;

		if(bugsAndEnhancementsForSelectedRelease == null) {
			//System.out.println("\n\n============ ReleasePlan.bugsAndEnhancementsForSelectedRelease()");
			//Bugs and Enhancements
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Enhancement'", null));
			//qual.addObject(new EOOrQualifier(qual1));

			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			bugsAndEnhancementsForSelectedRelease = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return bugsAndEnhancementsForSelectedRelease;
    }
	
	public String urlForSupportEscalationBugsAndEnhancementsForSelectedRelease() {
		return listURLForArray(supportEscalationBugsAndEnhancementsForSelectedRelease());
	}
	public String urlForCustomerReportedBugsAndEnhancementsForSelectedRelease() {
		return listURLForArray(customerReportedBugsAndEnhancementsForSelectedRelease());
	}
	public String urlForBetaBugsAndEnhancementsForSelectedRelease() {
		return listURLForArray(betaBugsAndEnhancementsForSelectedRelease());
	}	

	public String urlForOcmsForSelectedRelease() {
		return listURLForArray(ocmsForSelectedRelease());
	}
	
	public String urlForTaserBugsAndEnhancementsForSelectedRelease() {
		return listURLForArray(taserBugsAndEnhancementsForSelectedRelease());
	}
	
	public NSArray bugsAndEnhancementsNotInStoryForSelectedRelease() {
		NSMutableArray temp = new NSMutableArray();
		
		Enumeration enumer = bugsAndEnhancementsForSelectedRelease().objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			boolean hasStory = false;
			Item anItem = (Item)enumer.nextElement();
			Enumeration enumer2 = anItem.allParents().objectEnumerator();
			
			while(enumer2.hasMoreElements() == true) {
				Item aParent = (Item)enumer2.nextElement();
				if(  (aParent.version().equals(selectedProject())) && ( (aParent.type().equals("Story")) || ((aParent.type().equals("Epic")))) ) {
					hasStory = true;
					break;
				}
			}
			if(hasStory == false) {
				temp.addObject(anItem);
			}
		}
		return temp;
	}
	
	public NSArray bugsAndEnhancementsNotInStoryForSprint(String pSprintKey) {
		NSMutableArray temp = new NSMutableArray();
		
		Enumeration enumer = bugsAndEnhancementsForSprint(pSprintKey).objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			boolean hasStory = false;
			Item anItem = (Item)enumer.nextElement();
			Enumeration enumer2 = anItem.allParents().objectEnumerator();
			
			while(enumer2.hasMoreElements() == true) {
				Item aParent = (Item)enumer2.nextElement();
				if(  (aParent.version().equals(selectedProject())) && ( (aParent.type().equals("Story")) || ((aParent.type().equals("Epic")))) ) {
					hasStory = true;
					break;
				}
			}
			if(hasStory == false) {
				temp.addObject(anItem);
			}
		}
		return temp;
	}
	
	public void bugsAndEnhancementsSortedByFoundIn() {
	//System.out.println("ReleasePlan.bugsAndEnhancementsSortedByFoundIn()");
		NSMutableArray currentSortedArray;
		NSMutableDictionary foundInVersionDictionary = new NSMutableDictionary();
		
		Enumeration enumer = bugsAndEnhancementsForSelectedRelease().objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			Item anItem = (Item)enumer.nextElement();
			String foundIn =  (String)anItem.versionFoundIn();
			Integer bugId =  (Integer)anItem.bugId();
			currentSortedArray = (NSMutableArray)foundInVersionDictionary.objectForKey(foundIn);
			if(currentSortedArray == null) {
				currentSortedArray =  new NSMutableArray();
				foundInVersionDictionary.setObjectForKey(currentSortedArray,foundIn);
			}
			
			currentSortedArray.addObject(anItem);
			////System.out.println("" + (Integer)releaseNoteBug.valueForKey("bugId") + " - Found In: " + (String)releaseNoteBug.versionFoundIn()  + "\n");
		}
		
		// Print all found in
		
		//System.out.println("FoundIn Release");
		Enumeration enumer2 = foundInVersionDictionary.keyEnumerator();
		while(enumer2.hasMoreElements() == true) {
			String aKey = (String)enumer2.nextElement();
			NSMutableArray items = (NSMutableArray)foundInVersionDictionary.objectForKey(aKey);
			
			//System.out.println("Release: " + aKey + " numItems - " + items.count());
			Enumeration enumer3 = items.objectEnumerator();
			while(enumer3.hasMoreElements() == true) {
				Item anItem = (Item)enumer3.nextElement();
				Integer bugId =  (Integer)anItem.bugId();
				String desc =  (String)anItem.shortDesc();
				//System.out.println("\tBug: " + bugId + " - " + desc);
			}
			
		}
		//System.out.print("\n");
		
		
	}
	
	public NSArray bugsAndEnhancementsForSprint(String pSprintKey) {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
					
		//sprint
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='" + pSprintKey + "'", null));			
		// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
		
		Enumeration enumer = selectedItemProducts().objectEnumerator();
		while(enumer.hasMoreElements()) {
			qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
		}
		qual.addObject(new EOOrQualifier(qual1));
		
		Object orderings[]={
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		return (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
    }


	public NSArray resolvedbugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();        
        EOQualifier qualifier;
		if( resolvedbugsAndEnhancementsForSelectedRelease == null) {
			
			//Bugs and Enhancements
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CONFIRMED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			//fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			//fs.setRefreshesRefetchedObjects(true);
			//resolvedbugsAndEnhancementsForSelectedRelease = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);
			resolvedbugsAndEnhancementsForSelectedRelease = (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
		}
		return resolvedbugsAndEnhancementsForSelectedRelease;
    }
	
	public NSArray verifiedAndClosedBugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(verifiedAndClosedBugsAndEnhancementsForSelectedRelease == null) {
			
			//Bugs and Enhancements
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			
			// Release
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CONFIRMED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			verifiedAndClosedBugsAndEnhancementsForSelectedRelease = (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
		}
		return verifiedAndClosedBugsAndEnhancementsForSelectedRelease;
    }
	
	
	public NSArray supportEscalationBugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		if(supportEscalationBugsAndEnhancementsForSelectedRelease == null) {
						
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));
			// Support Escalations
			//qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords like '*support_escalation*'", null));
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'support_escalation'", null));

			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);

			supportEscalationBugsAndEnhancementsForSelectedRelease = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
			//supportEscalationBugsAndEnhancementsForSelectedRelease = (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
		}
		return supportEscalationBugsAndEnhancementsForSelectedRelease;
    }
	public NSArray customerReportedBugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();

		if(customerReportedBugsAndEnhancementsForSelectedRelease == null) {
						
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));
			
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'customerreported'", null));
	
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			customerReportedBugsAndEnhancementsForSelectedRelease = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return customerReportedBugsAndEnhancementsForSelectedRelease;
    }
	public NSArray betaBugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();

		if(betaBugsAndEnhancementsForSelectedRelease == null) {
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));

			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'beta'", null));

			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			betaBugsAndEnhancementsForSelectedRelease = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return betaBugsAndEnhancementsForSelectedRelease;
    }
	
	public NSArray ocmsForSelectedRelease() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();

		if(ocmsForSelectedRelease == null) {
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='OCM'", null));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='Ops Change Management (OCM)'", null));

			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			ocmsForSelectedRelease = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return ocmsForSelectedRelease;
    }

	public NSArray taserBugsAndEnhancementsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        EOQualifier qualifier;

		if(taserBugsAndEnhancementsForSelectedRelease == null) {
						
			// Support Escalations
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords like '*taser*'", null));


			Object orderings[]={
					//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			taserBugsAndEnhancementsForSelectedRelease = (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
		}
		return taserBugsAndEnhancementsForSelectedRelease;
    }
	
	
	
	public NSArray openBugsAndEnhancementsForSelectedRelease() {
        NSMutableArray qual = new NSMutableArray();

		if(openBugsAndEnhancementsForSelectedRelease == null) {
			
			//Bugs and Enhancements
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='RESOLVED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='CLOSED'", null));


			openBugsAndEnhancementsForSelectedRelease = (NSArray)EOQualifier.filteredArrayWithQualifier(bugsAndEnhancementsForSelectedRelease(), new EOAndQualifier(qual)) ;
			displayGroup.setObjectArray(openBugsAndEnhancementsForSelectedRelease);
		}
		return openBugsAndEnhancementsForSelectedRelease;
    }
	

	public String urlForAll() {
		return listURLForArray(bugsAndEnhancementsForSelectedRelease());
	}

	public String urlForOpen() {
		return listURLForArray(openBugsAndEnhancementsForSelectedRelease());
	}
	public String urlForResolved() {
		return listURLForArray(resolvedbugsAndEnhancementsForSelectedRelease());
	}

	public String urlForVerifiedAndClosed() {
		return listURLForArray(verifiedAndClosedBugsAndEnhancementsForSelectedRelease());
	}

	public String percentCompleteOpen() {
		double open = (double)openBugsAndEnhancementsForSelectedRelease().count();
		double all = (double)(bugsAndEnhancementsForSelectedRelease().count());
		

        return percentCompleteString(((double)open/(double)all) * 100);
    }
	public String percentCompleteResolved() {
		double resolved = (double)resolvedbugsAndEnhancementsForSelectedRelease().count();
		double all = (double)(bugsAndEnhancementsForSelectedRelease().count());
		

        return percentCompleteString(((double)resolved/(double)all) * 100);
    }
	public String percentCompleteVerifiedAndClosed() {
		double verified = (double)verifiedAndClosedBugsAndEnhancementsForSelectedRelease().count();
		double all = (double)(bugsAndEnhancementsForSelectedRelease().count());
		

        return percentCompleteString(((double)verified/(double)all) * 100);
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
	

    public void refreshData() {
        displayGroup.fetch();
    }

    public WOComponent resetDisplayedBugs() {
		//System.out.println("ReleasePlan.resetDisplayedBugs()");
        session().takeValueForKey(selectedProject, "selectedProject");
		invalidateAllStories();
		invalidateLogicalStories();
		storiesForSelectedRelease = null;
		storiesForSprints = null;
		bugsAndEnhancementsForSelectedRelease = null;
		openBugsAndEnhancementsForSelectedRelease = null;
		resolvedbugsAndEnhancementsForSelectedRelease = null;
		verifiedAndClosedBugsAndEnhancementsForSelectedRelease = null;
		supportEscalationBugsAndEnhancementsForSelectedRelease = null;
		customerReportedBugsAndEnhancementsForSelectedRelease = null;
		betaBugsAndEnhancementsForSelectedRelease = null;
		taserBugsAndEnhancementsForSelectedRelease = null;
		milestoneDatesForProject = null;
		datesForMilestones = null;
		childrenForSprints = null;
		personStatus = null;
		issues = null;
		//session().setDefaultEditingContext(new EOEditingContext());
        //openBugsForSelectedRelease();
        //session().takeValueForKey(context().page(), "homePage");

        return null;
    }

    public void invalidateAllStories() {
			//System.out.println("ReleasePlan.invalidateAllStories()");

        // Invalidate All Stories
		if(storiesForSelectedRelease() != null) {
			invalidateObjects(storiesForSelectedRelease());
		}

    }

    public void invalidateLogicalStories() {
			//System.out.println("ReleasePlan.invalidateAllStories()");

        // Invalidate All logicalStoriesForRelease
		if(logicalStoriesForRelease() != null) {
			invalidateObjects(logicalStoriesForRelease());
		}

    }

    public void invalidateObjects(NSArray pObjects) {
			//System.out.println("ReleasePlan.invalidateObjects()");

        // Invalidate Objects
		if(pObjects != null) {
			//System.out.println("ReleasePlan.invalidateObjects() - 2");
		
			Enumeration enumer = pObjects.objectEnumerator();
			NSMutableArray temp = new NSMutableArray();
			while(enumer.hasMoreElements()) {
				Item i = (Item)enumer.nextElement();
				i.invalidateAllChildren();
				EOGlobalID id = (EOGlobalID)(session().defaultEditingContext().globalIDForObject(i));
				if(id != null) {
					temp.addObject(id);
				}
			}
			session().defaultEditingContext().invalidateObjectsWithGlobalIDs((NSArray)temp);
		}

    }


    public void  setDisplayGroup(WODisplayGroup pDisplayGroup) {
        displayGroup = pDisplayGroup;
    }
	
	public WODisplayGroup displayGroup() {
		if(displayGroup.allObjects().count() == 0) {
			openBugsAndEnhancementsForSelectedRelease(); // ensure that displayGroup gets setup correctly
		}
		return displayGroup;
	}
    public String aRow() {
        return aRow;
    }
    public void setARow(String newRowType) {
        aRow = newRowType;
    }
    public boolean isListEmpty()
    {
        return (listSize()==0);
    }

    public int listSize()
    {
        return displayGroup.allObjects().count();
    }
    public boolean isToolBar() {
        return true;
    }

    public WOComponent componentToEmail() {
        WOComponent comp = null;
		comp = (ReportGenerator)pageWithName("ReportGenerator");
		comp.takeValueForKey(aColumn, "selectedColumn");
		comp.takeValueForKey(aRow, "selectedRow");
		comp.takeValueForKey(displayGroup, "displayGroup");
		((ReportGenerator)comp).setHideReportSpecifier(true);
		((ReportGenerator)comp).setHideGraphLink(true);
        return (WOComponent)comp;
    }
	

    public void setComponentToEmail(WOComponent pComponent) {
        // do nothing
    }
    public WOComponent currentComponent() {
        Session s;
        s = (Session)session();

        return s.homePage;
    }

	
	public String selectedProject() {
        return selectedProject;
    }
	public void setSelectedProject(String aVal) {
		if((selectedProject != null) && (!selectedProject.equals(aVal))) {
			setDefaultsCookie("default_project",aVal);
		}
		
		selectedProject = aVal;
    }
	public void getDefaultsFromCookies() {
        WOContext context = context();
        WORequest request = context.request();
		Session s;
		String default_project = null;
		String default_products = null;
		        
		s = (Session)session();
		default_project = request.cookieValueForKey("default_project");
		default_products = request.cookieValueForKey("default_products");
		
		if(default_project != null) {
			s.setSelectedProject(default_project);
        }

		if(default_products != null) {
			StringTokenizer tokens = new StringTokenizer(default_products, "+");
			NSMutableArray productArray = new NSMutableArray();
			while(tokens.hasMoreElements()) {
				String aProduct = tokens.nextToken();
				if(aProduct.equals("IronKey Drive")) {
					aProduct = "Device: Secure Storage";
				}
				productArray.addObject(aProduct);
			}
			setSelectedItemProducts(productArray);
        }
		
    }
	
	
	public void setDefaultsCookie(String pCookieName, String pValue) {
        NSTimestamp now = new NSTimestamp();
        NSTimestamp yearFromNow = now.timestampByAddingGregorianUnits(1,0,0,0,0,0);
        WOContext context = context();
        WOResponse response = context.response();
        WORequest request = context.request();

        WOCookie cookie = WOCookie.cookieWithName(pCookieName, pValue);
        cookie.setExpires(yearFromNow);
        cookie.setPath(request.adaptorPrefix() + "/");
        //cookie.setPath(request.adaptorPrefix() + "/" + request.applicationName());
		if(response != null) {
			response.addCookie(cookie);
		}
	}



	
	public String selectedProductIds() {
		int count = selectedItemProducts().count();
		String productIdString = "(";

		for(int i=0; i< count; i++) {
		   String aProduct = (String)selectedItemProducts().objectAtIndex(i);
		   int prodId = idForProduct(aProduct);
		   if(prodId > 0) {
			   productIdString += prodId;
			   
			   if(i< count-1) {
					productIdString += ",";
				}
			}
		}
		 productIdString += ")";
		 return productIdString;
	}
	
	/*
    public int itemsInTriage() {
        Session s = (Session)session();

       NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and product_id in " + selectedProductIds() + " and assigned_to='23' and version='"+ selectedProject()+ "'");
    //    NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and assigned_to='23' and version='"+ selectedProject()+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
	*/

	public WOComponent emailCurrentPage() {
        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");

       nextPage.takeValueForKey(componentToEmail(),"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");

        return nextPage;
    }

    public NSArray itemProducts() {
        if(itemProducts == null) {
            Session s = (Session)session();
            // NSMutableArray tempTypes = new NSMutableArray(new Object[] {"Client: Trusted Access - Desktop", "Client: Trusted Access - Mobile", "Marble Services", "Device: Trusted Access", "Risk Analytics Platform"});
            NSMutableArray tempTypes = new NSMutableArray();
			
			Enumeration enumer = s.potentialProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				String aProduct = (String)enumer.nextElement();
				if((!aProduct.startsWith("_"))
				&& (!aProduct.startsWith("IronKey"))	
				&& (!aProduct.startsWith("Imation"))	
				) {
					tempTypes.addObject(aProduct);
				}
			}
			
			itemProducts = new NSArray(tempTypes);
        }
        return itemProducts;
    }
	
	public NSArray selectedItemProducts() {
		return selectedItemProducts;
	}
	public void setSelectedItemProducts(NSArray pValues) {
		NSMutableArray temp;
		// if different that current
		temp = new NSMutableArray(pValues);
		
		/*
		
		// If the checkbox is selected (default) add IronKey Services
		if((isIronKeyServices() == true) && (!pValues.contains("IronKey Services"))) {
			temp.addObject("IronKey Services");
		}
		else if((isIronKeyServices() == false) && (pValues.contains("IronKey Services")) && (pValues.count() > 1)) {
			temp.removeObject("IronKey Services");
		}
		*/
		
		if((selectedItemProducts != null) && (!selectedItemProducts.equals(pValues))) {
			setDefaultsCookie("default_products", tokenizeProductArray((NSArray)temp));
		}
		selectedItemProducts = (NSArray)temp;

	}
	

	
	public String tokenizeProductArray(NSArray pValues) {
		String returnVal = "";
		int count = pValues.count();
		for(int i = 0; i<count; i++) {
			returnVal += pValues.objectAtIndex(i);
			if(i<count-1) {
				returnVal += "+";
			}
		}
		return returnVal;
	}
	
    public String listURLForArray(NSArray pArray) {
        Enumeration enumer;
        String allBugNumbers = "";

        enumer = pArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject currObject = (EOEnterpriseObject)enumer.nextElement();
            allBugNumbers += currObject.valueForKey("bugId") + ",";
        }

        String URL =  ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" + allBugNumbers;
        return URL;
	}
    public NSTimestamp projectStartDate() {
		return dateForMilestone("S1", "start");
	}
    public NSTimestamp rtpDate() {
		NSTimestamp rtp = dateForMilestone("RTP", "actual");
		if(rtp == null) {
			rtp = dateForMilestone("RTP", "forecast");
		}
	
		return rtp;
	}
	
    public NSTimestamp rc1Date() {
		NSTimestamp rc1 = dateForMilestone("RC1", "actual");
		if(rc1 == null) {
			rc1 = dateForMilestone("RC1", "forecast");
		}
	
		return rc1;
	}
	
    public NSTimestamp ccDate() {
		NSTimestamp cc = dateForMilestone("CC", "actual");
		if(cc == null) {
			cc = dateForMilestone("CC", "forecast");
		}
	
		return cc;
	}
	
	public String ccDateType() {
		String returnVal = "";
		NSTimestamp cc = dateForMilestone("CC", "actual");
		if(cc != null) {
			returnVal = "actual";
		}
		else {
			cc = dateForMilestone("CC", "forecast");
			if(cc != null) {
				returnVal = "forecast";
			}
		}
	
		return returnVal;
	}
	public String rc1DateType() {
		String returnVal = "";
		NSTimestamp cc = dateForMilestone("RC1", "actual");
		if(cc != null) {
			returnVal = "actual";
		}
		else {
			cc = dateForMilestone("RC1", "forecast");
			if(cc != null) {
				returnVal = "forecast";
			}
		}
	
		return returnVal;
	}
	public String rtpDateType() {
		String returnVal = "";
		NSTimestamp cc = dateForMilestone("RTP", "actual");
		if(cc != null) {
			returnVal = "actual";
		}
		else {
			cc = dateForMilestone("RTP", "forecast");
			if(cc != null) {
				returnVal = "forecast";
			}
		}
	
		return returnVal;
	}
	
	public boolean isRTP() {
		return (rtpDate() != null)?true:false;
	}
	
	public boolean isRC1() {
		return (rc1Date() != null)?true:false;
	}
	
	public boolean isCC() {
		return (ccDate() != null)?true:false;
	}
	
	public boolean isStatusWhiteboard() {
		boolean returnVal = false;
		
		String aVal = (String)anEo.valueForKey("statusWhiteboard");
		if((aVal != null) && (!aVal.equals(""))) {
			returnVal = true;
		}
		return returnVal;
	}
	
	public boolean isStatusWhiteboard2() {
		boolean returnVal = false;
		
		String aVal = (String)((Item)aKey).valueForKey("statusWhiteboard");
		if((aVal != null) && (!aVal.equals(""))) {
			returnVal = true;
		}
		return returnVal;
	}

	public NSTimestamp startDateForCurrentSprint() {
		return dateForMilestone(sprintKey, "start");
	}

    public NSTimestamp endDateForCurrentSprint() {
		return dateForMilestone(sprintKey, "end");
	}
	
	public String weekdaysRemainingInSprint() {
		String daysRemain = "NA";
		
		NSTimestamp end = endDateForCurrentSprint();
		if(end != null) {
			NSTimestamp now = new NSTimestamp();

			DateUtil dateUtil = new DateUtil(now, end);
			int weekdays = (int)dateUtil.weekdays();
			//if(weekdays > 0) {
				daysRemain = "" + weekdays;
			//}
			
			/*
			//System.out.println("weekdays - " + weekdays);

			int hours = (int)dateUtil.hours;
			//System.out.println("weekdayhours - " + hours);
			int minutes = (int)dateUtil.minutes;
			//System.out.println("weekdayminutes - " + minutes);
			int seconds = (int)dateUtil.seconds;
			//System.out.println("weekdayseconds - " + seconds);
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

			SimpleDateFormat theFormat = new SimpleDateFormat("MMM/dd/yyyy H:m:s");
			//System.out.println("Now - " + theFormat.format(now));

			DateUtil dateUtil = new DateUtil(now, end);
			int days = (int)dateUtil.calendardays();
			if(days > 0) {
				daysRemain = "" + days;
			}

			//int hours = (int)dateUtil.hours;
			/*
			//System.out.println("hours - " + hours);
			int minutes = (int)dateUtil.minutes;
			//System.out.println("minutes - " + minutes);
			int seconds = (int)dateUtil.seconds;
			//System.out.println("seconds - " + seconds);
			*/
			
		}
		return daysRemain;
	}

	
    public NSTimestamp dateForMilestone(String pMilestone, String dateType) {
		NSTimestamp returnVal = null;
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

        if((milestoneDatesForProject == null) && (selectedProject() != null)) {
			//System.out.println("\n\n============ ReleasePlan.milestoneDatesForProject()");
			//System.out.println("\n\n============ ReleasePlan.milestoneDatesForProject()" + selectedProject);

			// Tasks - Defects, Enhancements, WorkItems
			bindings = new NSDictionary(new Object[] {selectedProject()}, new Object[] { "version"});
			fs = EOFetchSpecification.fetchSpecificationNamed( "milestoneDates", "MilestoneDates").fetchSpecificationWithQualifierBindings( bindings );
			milestoneDatesForProject =  (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

        }
        return milestoneDatesForProject;
    }
    
	public String bgColorForState() {
		String returnVal = "#ffffff";
		String state =  (String)anEo.valueForKey("state");
		
		if(state.equals("Late")) {
			returnVal = "#ff0000";				// Red
		}
		else if(state.equals("Risk-High")) {  
			returnVal = "#ff9900";				// Orange
		}
		else if(state.equals("Risk-Low")) {
			returnVal = "#ffff00";				 // Yellow
		}
		else if(state.equals("Complete")) { 
			returnVal = "#00ff00";				// Green
		}
		
		return returnVal;
	}
	
	public String bgColorForState2() {
		String returnVal = "#ffffff";
		//String state =  (String)anEo.valueForKey("state");
		String state =  storyPlus_Status();
		
		if(state.equals("Late")) {
			returnVal = "#ff0000";				// Red
		}
		else if(state.equals("Risk-High")) {  
			returnVal = "#ff9900";				// Orange
		}
		else if(state.equals("Risk-Low")) {
			returnVal = "#ffff00";				 // Yellow
		}
		else if(state.equals("Complete")) { 
			returnVal = "#00ff00";				// Green
		}
		
		return returnVal;
	}

	public String colorBoxClass() {
		String returnVal = "useColorboxMedium";
		if(anEo != null) {
			int numChildren = ((Item)anEo).allChildren().count();
			if(numChildren > 12) {
				returnVal = "useColorboxLarge";
			} 
			else if(numChildren > 4) {
				returnVal = "useColorboxMedium";
			} 
		}
		return returnVal;
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

	public boolean isPerson() {
        return isPerson;
    }
    
    public void setIsPerson(boolean pVal) {
        if(pVal == true) {
            setIsStory(false);
            setIsList(false);
        }
        isPerson = pVal;
    }
	public boolean isStory() {
        return isStory;
    }
    
    public void setIsStory(boolean pVal) {
        if(pVal == true) {
            setIsPerson(false);
            setIsList(false);
        }
        isStory = pVal;
    }
	public boolean isList() {
        return isList;
    }
    
    public void setIsList(boolean pVal) {
        if(pVal == true) {
            setIsPerson(false);
            setIsStory(false);
        }
        isList = pVal;
    }
    public TasksForStory goTaskDetails()
    {
        TasksForStory nextPage = (TasksForStory)pageWithName("TasksForStory");
        anEo.invalidateAllChildren();  // just to clear caching
		nextPage.takeValueForKey(anEo, "parentItem");
		nextPage.takeValueForKey(context().page(), "nextPage");
       

        return nextPage;
    }
    
    public TasksForStory goTaskDetails2()
    {
        TasksForStory nextPage = (TasksForStory)pageWithName("TasksForStory");
		((Item)aKey).invalidateAllChildren();  // just to clear caching
		nextPage.takeValueForKey(((Item)aKey), "parentItem");
		nextPage.takeValueForKey(context().page(), "nextPage");
       

        return nextPage;
    }

    public ItemsForPerson goItemsForPerson()
    {
        ItemsForPerson nextPage = (ItemsForPerson)pageWithName("ItemsForPerson");
		nextPage.takeValueForKey(sprintKey, "sprintKey");
		nextPage.takeValueForKey(daysRemainingInSprint(), "daysRemainingInSprint");
		nextPage.takeValueForKey(weekdaysRemainingInSprint(), "weekdaysRemainingInSprint");		
		nextPage.takeValueForKey(selectedProject, "selectedProject");
		nextPage.takeValueForKey(topicForCurrentPersonForCurrentSprint(), "personTopic");
		nextPage.takeValueForKey(context().page(), "nextPage");
       

        return nextPage;
    }
	/*
	public WOComponent goToggleView() {
		if (collapsibleView == false) {
			collapsibleView = true;
		}
		else {
			collapsibleView = false;
		}
		return null;						
	}
	*/
	public boolean collapsibleView() {
		boolean returnVal = false;
		if((isPerson()== true) || (isStory() == true)) {
			returnVal = true;
		}
		return returnVal;
	}
	public WOComponent goPersonView() {
		setIsPerson(true);
		personStatus(); //processes all the data 
        anchor = "sprint_info";
		handleNamedAnchor = true;
		
		return null;						
	}
	public WOComponent goStoryView() {
		setIsStory(true);
        anchor = "sprint_info";
		handleNamedAnchor = true;
		
		return null;						
	}
	public WOComponent goListView() {
		setIsList(true);
		return null;						
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

    public MilestoneForProject goViewMilestoneDates()
    {
        MilestoneForProject nextPage = (MilestoneForProject)pageWithName("MilestoneForProject");
		nextPage.takeValueForKey(selectedProject(), "selectedProject");
        

        return nextPage;
    }

    public EpicStorySummary goEpicStorySummary()
    {
        EpicStorySummary nextPage = (EpicStorySummary)pageWithName("EpicStorySummary");
		nextPage.takeValueForKey(selectedProject(), "selectedProject");
		nextPage.takeValueForKey(logicalStoriesForRelease(), "storiesForRelease");

        return nextPage;
    }

    public ReleaseNotes goReleaseNotes()
    {
        ReleaseNotes nextPage = (ReleaseNotes)pageWithName("ReleaseNotes");
		nextPage.takeValueForKey(selectedProject(), "selectedProject");

        return nextPage;
    }

    public StoryDetails goStoryDetails()
    {
        StoryDetails nextPage = (StoryDetails)pageWithName("StoryDetails");
		nextPage.setCurrentObject((Item)anEo);
		nextPage.setNextPage(context().page());
		//System.out.println("Story - " + (String)anEo.valueForKey("shortDesc"));
		
        return nextPage;
    }
    
	//public NSArray selectedInclusions() {
	//	return selectedInclusions;
	//}
	/*
	public void setSelectedInclusions(NSArray pValue) {
		NSMutableArray temp;
		selectedInclusions = pValue;
		
		//System.out.println("ReleasePlan.setSelectedInclusions()");
		
		// Do they want 'IronKey Services' included?
		if(isIronKeyServices() == true) {
			if(!selectedItemProducts().contains("IronKey Services")) {  // not currently included
				temp = new NSMutableArray(selectedItemProducts());
				temp.addObject("IronKey Services");
				setSelectedItemProducts((NSArray)temp);
			}
			// else - they want it and it is already included
		
		}
		else {   // don't include auto include 'IronKey Services'  (what if they manually add it, currently we will remove it.)
			if((selectedItemProducts().contains("IronKey Services")) && (selectedItemProducts().count() >1)) {  // currently included
				temp = new NSMutableArray(selectedItemProducts());
				temp.removeObject("IronKey Services");
				setSelectedItemProducts((NSArray)temp);
			}
		}
		
	}
    */
    public Comment goDisplayComment()
    {
        Comment nextPage = (Comment)pageWithName("Comment");

        nextPage.setAComment((String)anEo.valueForKey("statusWhiteboard"));

        return nextPage;
    }
    
    public Comment goDisplayComment2()
    {
        Comment nextPage = (Comment)pageWithName("Comment");

        nextPage.setAComment((String)((Item)aKey).valueForKey("statusWhiteboard"));

        return nextPage;
    }
}