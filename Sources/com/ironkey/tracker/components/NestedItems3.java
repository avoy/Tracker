package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.regex.*;
import java.util.Enumeration;


public class NestedItems3 extends WOComponent {
	private static final long serialVersionUID = 1L;
    public Item anEo;
    public NSArray itemList;
    public Object aKey;
    public NSDictionary items;
    public boolean isChild = false;
    public int level = 0;
    public Pattern p; 
    public Pattern p2; 
	

    public NestedItems3(WOContext context) {
        super(context);
		p =  Pattern.compile("(\\[.*\\] )(.*)"); // anything in square braces, followed by a space	'[CLONE 15421] '
		p2 =  Pattern.compile("(.*: )(.*)"); // remove any of the leading qualifiers that occur prior to a ': '
		////System.out.println("NestedItems2.NestedItems2() - " + level());
	 }
	
	public void setItemList(NSArray pArray) {
		itemList = pArray;
	}
	public void setItems(NSDictionary pDict) {
		items = pDict;
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
		////System.out.println("level - " + level());
		
	}
	
		
	public void setAnEo(Item pItem) {
		anEo = pItem;
	}
	public String cleanDescription() {
		String returnVal;

		returnVal = (String)anEo.valueForKey("shortDesc");
		/*
		Matcher m = p.matcher(returnVal);
		if(m.find() == true) {
			////System.out.println("pattern1 - " + m.group(1));
			returnVal = m.group(2);
		}
		
		Matcher m2 = p2.matcher(returnVal);
		if(m2.find() == true) {
			////System.out.println("pattern2 - " + m2.group(1));
			returnVal = m2.group(2);
		}
		*/
		return returnVal;
	}
	
	public String sprintsForTopLevel() {
		String returnVal = "";
		
		String type = (String)anEo.valueForKey("type");
		String milestone = (String)anEo.valueForKey("targetMilestone");
		String release = (String)anEo.valueForKey("version");
		
		String releaseMile =  release.substring(0,1);
		releaseMile += milestone.substring(1,2);
		releaseMile = milestone; // KTA
		if(!type.equals("Epic")) {
			//returnVal = milestone;
			returnVal = releaseMile;
		}
		else {  // must be an Epic
			Session s = (Session)session();
			NSMutableArray sprintsForStory = new NSMutableArray();
			Enumeration enumer = anEo.allStories().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item theStory = (Item)enumer.nextElement();
				milestone = (String)theStory.valueForKey("targetMilestone");
				release = (String)theStory.valueForKey("version");
				releaseMile =  release.substring(0,1);
				releaseMile += milestone.substring(1,2);
				
				//if(!sprintsForStory.containsObject(milestone)) {
				//    sprintsForStory.addObject(milestone);
				//}
				if(!sprintsForStory.containsObject(releaseMile)) {
				    sprintsForStory.addObject(releaseMile);
				}
			}
			// Sort the String alphabetically
			sprintsForStory = s.sortArray(sprintsForStory);
			
			// Format the array into a string
			int numStories = sprintsForStory.count();
			int count = 0;
			Enumeration enumer2 = sprintsForStory.objectEnumerator();			
			while(enumer2.hasMoreElements()) {
				count++;
				returnVal += (String)enumer2.nextElement();
				if(count < numStories) {
					returnVal += ", ";
				}
			}
		}
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
		String returnVal = null;
		if(isEO()) {
			//returnVal =  "" + anEo.bugId() + anEo.bugId();
			returnVal =  "" + anEo.bugId();
		}
		else {
			returnVal = keyDescription();
		}
		return returnVal;
	}
	
	
	public String toggleString() {
		return "toggleView('" + uniqueID() + "')";
	}
	
	public boolean isEO() {
		//if((className.equals("com.webobjects.eocontrol.EOGenericRecord")) || (className.equals("er.extensions.ERXGenericRecord"))) {
		boolean returnVal = false;
		String className = aKey.getClass().getName();	
    	//System.out.println("NestedItems3.isEO() -  " + className);
		if(className.equals("com.ironkey.tracker.Item")) {
			setAnEo((Item)aKey);
			returnVal = true;
		}
		return returnVal;
	}
	public boolean isString() {
		boolean returnVal = false;
		String className = aKey.getClass().getName();		
		if(className.equals("java.lang.String")) {
			setAnEo(null);
			returnVal = true;
		}
		return returnVal;
	}
	
	public String keyDescription() {
		return (isString()== true)?(String)aKey: "";
	}
	
	public boolean isChild() {
		return isChild;
	}
	
	public NSArray listForKey() {
		NSArray returnVal = null;
		if(isEO() == true) {
			returnVal = anEo.children();
		}
		else if(isString() == true) {
			returnVal = (NSArray)items.valueForKey((String)aKey);
		}
		return returnVal;
	}
	
	public String urlForKey() {
		return listURLForArray(listForKey());
	}

	public void setIsChild(boolean pVal) {
		isChild = pVal;
	}
		
	public String committedForKey() {
		String returnVal = "Triage to determine";
		if(isDoneForKey() == true) {
			returnVal = "Committed";
		}
		return returnVal;
	}
	public String statusForKey() {
		String returnVal = "In Progress";
		if(isDoneForKey() == true) {
			returnVal = "Complete";
		}
		return returnVal;
	}
	
	public boolean isDoneForKey() {
		boolean returnVal = false;
		if(closedForKey() == listForKey().count()) {
			returnVal = true;
		}
		return returnVal;
	}
		
	public int closedForKey() {
		int numClosed = 0;
		Enumeration enumer = listForKey().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item bug = (Item)enumer.nextElement();
			String status = (String)bug.valueForKey("bugStatus");
			if((status.equals("CLOSED")) || (status.equals("VERIFIED"))) {
				numClosed++;
			}
		}
		return numClosed;
	}
   
   	public String percentCompleteForKey() {
		double closed = (double)closedForKey();
		double all = (double)(listForKey().count());
		

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


}
