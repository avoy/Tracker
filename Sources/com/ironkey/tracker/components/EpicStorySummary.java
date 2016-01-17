package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;

public class EpicStorySummary extends WOComponent {
	private static final long serialVersionUID = 1L;
	public Item anEo;
    public Item aStory;
    public NSArray storiesForRelease;
    public NSArray otherBugs;
    public String selectedProject;
    public String labelForButton;

    public EpicStorySummary(WOContext context) {
        super(context);
		//System.out.println("EpicStorySummary.EpicStorySummary()");
       
    }
	
	public NSArray storiesPlusOther() {
		//System.out.println("EpicStorySummary.storiesPlusOther()");
		
		NSMutableArray tempArray = new NSMutableArray(storiesForRelease);
		
		// Adding an entry for 'Other Bugs'
		tempArray.addObject("Other Bugs");
		Enumeration enumer = tempArray.objectEnumerator();
		while(enumer.hasMoreElements()) {
			Object theStory = (Object)enumer.nextElement();
            if(theStory.getClass().getName().equals("java.lang.String")) {
            	//System.out.println("EpicStorySummary.storiesPlusOther() String -  " + (String)theStory);
            }
            else {
            	//System.out.println("EpicStorySummary.storiesPlusOther() - Item " + (String)((Item)theStory).valueForKey("shortDesc"));
            }
		}

		return (NSArray)tempArray;
	
	}
	
	public NSMutableDictionary itemsForNonEOs() {
		//System.out.println("EpicStorySummary.itemsForNonEOs()");
		NSMutableDictionary items = new NSMutableDictionary();
		items.setObjectForKey(otherBugs, "Other Bugs");
		return items;
	}
	
	
	public boolean isTopLevelStoryCommitted() {
		//System.out.println("EpicStorySummary.isTopLevelStoryCommitted()");
		
		boolean returnVal = false;
		
		String storyType = (String)anEo.valueForKey("type");
		String milestone = (String)anEo.valueForKey("targetMilestone");
		if(storyType.equals("Story")) {
			if(!milestone.equals("---")) {
				returnVal = true;
			}
		}
		else {  // must be an Epic
			int committed = 0;			
			int uncommitted = 0;			
			Enumeration enumer = anEo.stories().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item theStory = (Item)enumer.nextElement();
				milestone = (String)theStory.valueForKey("targetMilestone");
				if(milestone.equals("---")) {
					uncommitted++;
				}
				else {
					committed ++;
				}

			}
			
			if((uncommitted > 0) && (uncommitted > 0)) {
				returnVal = false;
			}
			else if(uncommitted == 0) {
				returnVal = true;
			}
			else {
				returnVal = false;
			}
		}
		return returnVal;
	}
	public boolean isStoryCommitted() {
		//System.out.println("EpicStorySummary.isStoryCommitted()");
		
		boolean returnVal = false;
		String milestone = (String)aStory.valueForKey("targetMilestone");
		if(!milestone.equals("---")) {
			returnVal = true;
		}
		return returnVal;
	}
	
	
	public String storyId() {
		//System.out.println("EpicStorySummary.storyId()");
		
		return "" + (Integer)anEo.valueForKey("bugId");
	}
	
   public String rightArrowName() {
		return storyId() + "-RightImage";
   }
   public String downArrowName() {
		return storyId() + "-DownImage";
   }
   
	public boolean isEpic() {
		
		return (((NSArray)anEo.valueForKey("stories")).count() > 0)?true:false;
	}
   

   
}
