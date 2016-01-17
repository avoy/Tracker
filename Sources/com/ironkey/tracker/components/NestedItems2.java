package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.regex.*;
import java.util.Enumeration;


public class NestedItems2 extends WOComponent {
	private static final long serialVersionUID = 1L;
    public Item anEo;
    public NSArray itemList;
    public boolean isChild = false;
    public int level = 0;
	protected Pattern p; 
	protected Pattern p2; 

    public NestedItems2(WOContext context) {
        super(context);
		p =  Pattern.compile("(\\[.*\\] )(.*)"); // anything in square braces, followed by a space	'[CLONE 15421] '
		p2 =  Pattern.compile("(.*: )(.*)"); // remove any of the leading qualifiers that occur prior to a ': '
		//System.out.println("NestedItems2.NestedItems2() - " + level());
   
	 }
	
	public void setItemList(NSArray pArray) {
		itemList = pArray;
	}
	
	public int level() {
		return level; 
	}
	
	public void setLevel(int pVal) {
		if(isChild == true) {
			level = pVal+1;
		}
		else {
			level = 0;
		}
		//System.out.println("level - " + level());
		
	}
	
	
	public String cleanDescription() {
		String returnVal;

		returnVal = (String)anEo.valueForKey("shortDesc");
		/*
		Matcher m = p.matcher(returnVal);
		if(m.find() == true) {
			//System.out.println("pattern1 - " + m.group(1));
			returnVal = m.group(2);
		}
		
		Matcher m2 = p2.matcher(returnVal);
		if(m2.find() == true) {
			//System.out.println("pattern2 - " + m2.group(1));
			returnVal = m2.group(2);
		}
		*/
		return returnVal;
	}
	
	public boolean isTopLevelStoryCommitted() {
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
	
	public String rightArrowName() {
		return uniqueID() + "-RightImage";
   }
   public String downArrowName() {
		return uniqueID() + "-DownImage";
   }

	
	public String uniqueID() {
		return "" + anEo.bugId() + anEo.bugId();
	}
	
	public String toggleString() {
		return "toggleView('" + uniqueID() + "')";
	}
	
	public boolean isChild() {
		return isChild;
	}
	public void setIsChild(boolean pVal) {
		isChild = pVal;
	}

}
