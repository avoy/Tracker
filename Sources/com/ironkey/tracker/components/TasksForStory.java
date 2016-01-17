package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

public class TasksForStory extends WOComponent {
	private static final long serialVersionUID = 1L;	
    public Item parentItem;
    /** @TypeInfo Item */
    public Item anEo;
    public WOComponent nextPage;
    public NSArray<String> statusValues= new NSArray<String>(new String[] {"NEW", "ASSIGNED", "REOPENED", "RESOLVED", "VERIFIED", "CLOSED"});
    public String errorMessage;

    public TasksForStory(WOContext context) {
        super(context);
    }
	
	public NSArray<Item> tasks() {
		//if(tasks == null) {
		//	tasks = sortItemsAsDefined("bugStatus",parentItem.tasks(), statusValues);
		//}
		return parentItem.tasks();
	}
	
	public NSArray<Item> openTasks() {
		return sortArray(parentItem.openTasks());
    }
	
	public NSArray<Item> resolvedTasks() {
		return sortArray(parentItem.resolvedTasks());
    }
	
	public NSArray<Item> closedTasks() {
		return sortArray(parentItem.closedTasks());
    }
	
	public NSArray qaTasks() {
		return parentItem.qaTasks();
	}
	
	public NSArray openQATasks() {
		return sortArray(parentItem.openQATasks());
    }
	
	public NSArray resolvedQATasks() {
		return sortArray(parentItem.resolvedQATasks());
    }
	
	public NSArray closedQATasks() {
		return sortArray(parentItem.closedQATasks());
    }
	
	public NSArray other() {
	
		//if(other == null) {
		//	other = sortItemsAsDefined("bugStatus", parentItem.nonTasks(), statusValues);
		//}
		return parentItem.nonTasksAndQATasks();
	}
	
	public NSArray openNontasks() {
		return sortArray(parentItem.openNonTasksAndQATasks());
    }
	
	public NSArray resolvedNontasks() {
		return sortArray(parentItem.resolvedNonTasksAndQATasks());
    }
	
	public NSArray closedNontasks() {
		return sortArray(parentItem.closedNonTasksAndQATasks());
    }
	
	public String urlForTasks() {
		return (listURLForArray(tasks()));
	}
	public String urlForQATasks() {
		return (listURLForArray(qaTasks()));
	}
	public String urlForOthers() {
		return (listURLForArray(other()));
	}
	public String urlForAll() {
		return (listURLForArray( parentItem.allChildren()));
	}
	
	public String listURLForArray(NSArray pArray) {
        Enumeration enumer;
        String allBugNumbers = "";

        enumer = pArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject currObject = (EOEnterpriseObject)enumer.nextElement();
            allBugNumbers += currObject.valueForKey("bugId") + ",";
        }
		
        String URL = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=" + allBugNumbers;
        return URL;
	}
	
	public NSArray<Item> filterAndSortArrayWithQualifier(NSArray pUnFilteredArray, EOQualifier pQualifier) {
	
		Object orderings[]={
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};
		return EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)EOQualifier.filteredArrayWithQualifier(pUnFilteredArray,pQualifier), new NSArray(orderings)); 
	}
	
	public NSArray<Item> sortArray(NSArray<Item> pUnSortedArray) {
		EOSortOrdering orderings[]={
				//EOSortOrdering.sortOrderingWithKey("hot", EOSortOrdering.CompareCaseInsensitiveDescending),
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
		};
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(pUnSortedArray, new NSArray<EOSortOrdering>(orderings)); 
	}

	public NSMutableArray sortItemsAsDefined(String pKey, NSArray unorderedArray, NSArray orderingDefinitionArray) {
        NSMutableArray sortedArray = new NSMutableArray();
        NSMutableDictionary sortDictionary = new NSMutableDictionary();
        boolean returnVal = false;
        Enumeration enumer = unorderedArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject anEo = (EOEnterpriseObject)enumer.nextElement();
			String key = (String)anEo.valueForKey(pKey);
			NSMutableArray tempArray = (NSMutableArray)sortDictionary.valueForKey(key);
            if(tempArray == null) {
				tempArray = new NSMutableArray();
				tempArray.addObject(anEo);
                sortDictionary.setObjectForKey(tempArray, key);
            }
			else {
				tempArray.addObject(anEo);
			}
        }
		Enumeration enumer2 = orderingDefinitionArray.objectEnumerator();
        while(enumer2.hasMoreElements()) {
            String tempkey = (String)enumer2.nextElement();
			NSMutableArray tempArray2 = (NSMutableArray)sortDictionary.valueForKey(tempkey);
			if(tempArray2 != null) {
				sortedArray.addObjectsFromArray(tempArray2);
			}
		}
        return sortedArray;
    }
	
	public String labelForSprint() {
		String returnVal;
		String sprintKey  = (String)parentItem.valueForKey("targetMilestone");
		
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
	
	public String releaseFontColor() {
		String returnVal = "Black";
		if(!parentItem.version().equals(anEo.version())) {
			returnVal = "Red";
		}
		return returnVal;
		
	}

	public Item parentItem() {
		return parentItem;
	}
	public void setParent(Item anEO) {	
		parentItem = anEO;
	}

    public WOComponent goBack()
    {
		return nextPage;
    }

    public AddChild addChild()
    {
        AddChild nextPage = (AddChild)pageWithName("AddChild");

        nextPage.setCurrentObject(parentItem);
        nextPage.setNextPage(context().page());

        return nextPage;
    }

}
