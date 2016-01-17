package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.net.URL;
import java.util.regex.*;

public class StoryPriorities extends WOComponent {
	private static final long serialVersionUID = 1L;
	public NSArray<Item> openItemsForSelectedProduct;
	public NSArray<Item> resolvedAndClosedItemsSelectedProduct;	
	public WODisplayGroup displayGroup;
	public NSData dataForCheckboxOff;
	public NSData dataForCheckboxOn;
	public boolean emailFlag = false;
	public Item anItem;
	public NSTimestamp today;
	//protected NSArray supportQueue;
    public NSArray potentialQueues;
	public String aQueue;
	protected String selectedQueue;
	public NSArray<String> potentialTypes= new NSArray<String>(new String[] {"Stories", "Epics", "Bugs", "Enhancements"});
	public String aType;
	public String selectedType = "Stories";
	public int index;
	public boolean isSortMode = false;
	public boolean isSortAdmin = true;
	public Pattern p2; 

    public StoryPriorities(WOContext context) {
        super(context);
		today = new NSTimestamp();
		p2 =  Pattern.compile("(.*: )(.*)"); // remove any of the leading qualifiers that occur prior to a ': '

		if(((Application)Application.application()).isImation()) {  // Only set this for Imation, not IronKey
			potentialQueues= new NSArray(new Object[] {"Device: El Camino", "Device: Secure Storage"});
			selectedQueue = "Device: El Camino";		
		}
		else {
			potentialQueues= new NSArray(new Object[] {"Client: Trusted Access - Enterprise", "Client: Trusted Access - Mobile", "Client: Trusted Access - Mac", "Device: Trusted Access"});
			selectedQueue = "Device: Trusted Access";
		}


    }
	
	
	public void awake() {
		today = new NSTimestamp();
	}
	
	public int indexOneBase() {
		return index+1;
	}
	
	public String displayPotentialQueue() {
		String returnVal;
		
		returnVal = aQueue;		
		Matcher m2 = p2.matcher(returnVal);
		if(m2.find() == true) {
			//System.out.println("pattern2 - " + m2.group(1));
			returnVal = m2.group(2);
		}
		
		return returnVal;
	}
	public String displaySelectedQueue() {
		String returnVal;
		
		returnVal = selectedQueue;		
		Matcher m2 = p2.matcher(returnVal);
		if(m2.find() == true) {
			//System.out.println("pattern2 - " + m2.group(1));
			returnVal = m2.group(2);
		}
		
		return returnVal;
	}
	
	public NSArray openItemsForSelectedProduct() {
        EOFetchSpecification fs;

        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual2 = new NSMutableArray<EOQualifier>();

		if(openItemsForSelectedProduct == null) {
		
			// Stories and Epics
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Story'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Epic'", null));
			qual.addObject(new EOOrQualifier(qual1));

			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '" + selectedQueue +"'", null));
		
			// 'Open items'
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			qual2.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
			qual.addObject(new EOOrQualifier(qual2));


			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
						
			openItemsForSelectedProduct = (NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs));
		}

		return openItemsForSelectedProduct;
	}
	public WODisplayGroup displayGroup() {
		if(displayGroup.allObjects().count() == 0) {
			displayGroup.setObjectArray(openTopLevelStoriesForArray(openItemsForSelectedProduct()));
		}
		return displayGroup;
	}

	public NSArray resolvedAndClosedItemsSelectedProduct() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        NSMutableArray<EOQualifier> qual1 = new NSMutableArray<EOQualifier>();

		if(resolvedAndClosedItemsSelectedProduct == null) {
			// 'Open items'
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='NEW'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='REOPENED'", null));
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='ASSIGNED'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus !='VERIFIED'", null));

			// Stories and Epics
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Story'", null));
			qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Epic'", null));
			qual.addObject(new EOOrQualifier(qual1));

			qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '" + selectedQueue +"'", null));

			Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),			
				EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			
			resolvedAndClosedItemsSelectedProduct = closedTopLevelStoriesForArray((NSArray)(session().defaultEditingContext().objectsWithFetchSpecification(fs)));
		}
		return resolvedAndClosedItemsSelectedProduct;
    }
	
	public NSArray openTopLevelStoriesForArray(NSArray pArray) {
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer = pArray.objectEnumerator();
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
			}
		}		
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(tempArray, new NSArray(orderings));
	}
	public NSArray closedTopLevelStoriesForArray(NSArray pArray) {
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer = pArray.objectEnumerator();
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
						if((epic.isOpen() == false) && (tempArray.containsObject(epic) == false)) {
							tempArray.addObject(epic);
						}
						break;
					}
				}
				if(storyIsChildStory == false) {
					if((story.isOpen() == false) && (tempArray.containsObject(story) == false)) {
						tempArray.addObject(story);
					}
				}
			}
			else if((story.isOpen() == false) && (tempArray.containsObject(story) == false)) {
				tempArray.addObject(story);
			}
		}		
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(tempArray, new NSArray(orderings));
	}
	/*
	public NSArray closedTopLevelStoriesForArray(NSArray pArray) {
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer = pArray.objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item story = (Item)enumer.nextElement();
			if(story.hasParents() == true) {
				Item epic = (Item)((NSArray)story.topMostParents()).objectAtIndex(0);
				if(epic.isOpen() == false) {
					if(tempArray.containsObject(epic) == false) {
						tempArray.addObject(epic);
					}
				}
			}
			else if(tempArray.containsObject(story) == false) {
				tempArray.addObject(story);
			}
		}
		
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(tempArray, new NSArray(orderings));
	}
	*/
	
	public String filteredRank() {
		String returnVal = null;
		Integer rank = (Integer)anItem.valueForKey("rank");
		if(rank != null) {
			if (rank.intValue() == 99999) {
				returnVal = "---";
			}
			else {
				returnVal = "" + rank;
			}
		}
		else {
			returnVal = "---";
		}
		return returnVal;
	}

	public boolean emailFlag() {
	  return emailFlag;
	}
	
	public void setEmailFlag(boolean pVal) {
	    emailFlag = pVal;
	}
	public String selectedQueue() {
	  return selectedQueue;
	}
	
	public void setSelectedQueue(String pVal) {
	    selectedQueue = pVal;
	}
	
	public String timeSinceOpened() {
		return elapsedTimeSimple((NSTimestamp)anItem.valueForKey("creationTs"), today);
	}
	public String timeSinceModified() {
		return elapsedTimeSimple((NSTimestamp)anItem.valueForKey("lastdiffed"), today);
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
			/**
	 * Returns the number of days from a start date to an end date
     *
     * @param  NSGregorianDate  - startDate
     * @param  NSGregorianDate  - endDate
     * @return 	int
     */
	public String elapsedTime(NSTimestamp startTime, NSTimestamp endTime) {
		String returnVal = "";
		//System.out.println("startTime() - " + startTime() );
		//System.out.println("endTime() - " + endTime() );
		
		if((startTime != null) && (endTime != null)) {
			DateUtil dateUtil = new DateUtil(startTime, endTime);
			int numdays = (int)dateUtil.days;
			int numhours = (int)dateUtil.hours;
			int numMinutes = (int)dateUtil.minutes;
			
			if(numdays != 0) {
				if(numdays == 1) {
					returnVal += "" + numdays + " day";
				}
				else {
					returnVal += "" + numdays + " days";
				}
			}
			if(numhours != 0) {
				if(numdays != 0) {
					returnVal += ", ";
				}
				if(numhours == 1) {
					returnVal += "" + numhours + " hour";
				}
				else {
					returnVal += "" + numhours + " hours";
				}
			}
			if(numMinutes != 0) {
				if((numhours != 0) || (numdays != 0)) {
					returnVal += " and ";
				}
				returnVal += "" + numMinutes + " minutes";
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


	public String bugURL() {
		return ((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id=" +anItem.valueForKey("bugId");
	 }
	 
	public void displayAll() {
	}

	public NSData dataForCheckboxOn() {
		WOResourceManager rm;
		URL url;
		
		if(dataForCheckboxOn == null) {
			rm = application().resourceManager();
			url = rm.pathURLForResourceNamed("CheckboxOn.gif", "JavaDirectToWeb", null);

			try {
				//System.out.println(new NSData(url));
				dataForCheckboxOn = new NSData(url);
			}
			catch(Exception e) {
				System.err.println("dataForCheckbox - " + e);
			}
		}
		return dataForCheckboxOn;
	}
	public NSData dataForCheckboxOff() {
		WOResourceManager rm;
		URL url;
		
		if(dataForCheckboxOff == null) {
			rm = application().resourceManager();
			url = rm.pathURLForResourceNamed("CheckboxOff.gif", "JavaDirectToWeb", null);

			try {
				//System.out.println(new NSData(url));
				dataForCheckboxOff = new NSData(url);
			}
			catch(Exception e) {
				System.err.println("dataForCheckbox - " + e);
			}
		}
		return dataForCheckboxOff;
	}
    public WOComponent goUpdate() {
		openItemsForSelectedProduct = null;
		displayGroup.setObjectArray(openTopLevelStoriesForArray(openItemsForSelectedProduct()));
		
		resolvedAndClosedItemsSelectedProduct = null; 
		resolvedAndClosedItemsSelectedProduct();
        return null;
    }

    public WOComponent doNothing() {
        return null;
    }
	
    public WOComponent doSave() {
		try {
			Thread.sleep(1500);  // allow the javascript ajax call to complete before refetching
		}
		catch(Exception e) {
			System.err.println("StoryPriorities.doSave() exception - " + e);
		}
		openItemsForSelectedProduct = null;
		displayGroup.setObjectArray(openTopLevelStoriesForArray(openItemsForSelectedProduct()));
		
        return null;
    }
	
	
	public boolean isSortMode() {
		return isSortMode;
	}
	public void setIsSortMode(boolean pVal) {
		isSortMode = pVal;
	}    
	
	public boolean isSortAdmin()
    {
		boolean returnVal = false;
		Session s = (Session)session();
		EOEnterpriseObject user = s.getUser();
		String realName = (String)user.valueForKey("realname");
		//System.out.println("real name - " + realName);
		
		if(realName.equals("Kevin Avoy")) {
			returnVal = true;
		}
//		else if((realName.equals("Kapil Raina")) && (isTrustedAccess() == true)) {
		else if(realName.equals("John Barco")) {
			returnVal = true;
		}
		else if(realName.equals("Sam Farsad")) {
			returnVal = true;
		}
		else if(realName.equals("Vipin Hedge")) {
			returnVal = true;
		}
		else if(realName.equals("Ajay Nigam")) {
			returnVal = true;
		}
		//else if((realName.equals("Brian Schussler")) && (isSS() == true)) {
		else if(realName.equals("Brian Schussler")) {
			returnVal = true;
		}
				
        return returnVal;
    }

    public WOComponent goTurnSortModeOn()
    {
		setIsSortMode (true);
		setEmailFlag(true);
		
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		

		displayGroup.setSortOrderings(new NSArray(orderings));
		displayGroup.updateDisplayedObjects();
		
        return null;
    }

    public WOComponent goTurnSortModeOff()
    {
		setIsSortMode(false);	
		setEmailFlag(false);
		
		Object orderings[]={
			EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
			EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
			EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
		};
		

		displayGroup.setSortOrderings(new NSArray(orderings));
		displayGroup.updateDisplayedObjects();
		
        return null;
    }




}