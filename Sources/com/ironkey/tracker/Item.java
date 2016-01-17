package com.ironkey.tracker;
// Item.java
// Created on Fri Jul 20 12:15:19 US/Pacific 2007 by Apple EOModeler Version 5.2

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.math.BigDecimal;
import java.util.*;
import er.extensions.eof.ERXGenericRecord;;


public class Item extends ERXGenericRecord {
	private static final long serialVersionUID = 1L;

    protected NSArray<Item> sortedChildren;
    protected NSArray<Item> allChildren;
    protected NSArray<Item> allParents;
    protected int numHoursWorked = -1;
    protected String bugzillaHostUrl;
   

    public Item() {
        super();
		bugzillaHostUrl = ((Application)Application.application()).bugzillaHostUrl();  
		//bugzillaHostUrl = (new Application()).bugzillaHostUrl();  
    }

	public boolean isURL() {
		return (bugFileLoc().equals(""))?false:true;
	}	
	

    public void invalidateAllChildren() {
        // Invalidate All
        Enumeration enumer = allChildren().objectEnumerator();
        NSMutableArray temp = new NSMutableArray();
        while(enumer.hasMoreElements()) {
			Item child = (Item)enumer.nextElement();
            EOGlobalID id = editingContext().globalIDForObject(child);
            if(id != null) {
                temp.addObject(id);
            }
			child.numHoursWorked = -1;
        }
		
		// invalidate hours
		Enumeration enumer2 = hoursWorked().objectEnumerator();
        while(enumer2.hasMoreElements()) {
            EOGlobalID id = editingContext().globalIDForObject((EOEnterpriseObject)enumer2.nextElement());
            if(id != null) {
                temp.addObject(id);
            }
        }

        editingContext().invalidateObjectsWithGlobalIDs(temp);
		dropCachedChildren();
		numHoursWorked = -1;  // reset to recalculate hours worked

    }
	
	public void dropCachedChildren() {
        sortedChildren = null;
        allChildren = null;
        allParents = null;
    }

    public NSArray allChildren() {
        if(allChildren == null) {
            NSMutableArray<Item> all = new NSMutableArray<Item>();
            NSMutableArray<Item> tempArray = new NSMutableArray<Item>();
            all.addObjectsFromArray(children());
            all.addObjectsFromArray(grandChildren());

            // Remove duplicates
            Enumeration<Item> enumer = all.objectEnumerator();
            while(enumer.hasMoreElements()) {
                Item tempEo = (Item)enumer.nextElement();

                if(tempArray.containsObject(tempEo) == false) {
                    tempArray.addObject(tempEo);
                }
            }
            allChildren = new NSArray(tempArray);
        }

        return allChildren;
    }
	
    public NSArray<Item> children() {
        willRead();
        return (NSArray<Item>)storedValueForKey("children");
    }
	
    public NSArray childrenSameVersion() {
        return (NSArray)filterSameVersion(children());
    }
    public NSArray allChildrenSameVersion() {
        return (NSArray)filterSameVersion(allChildren());
    }
	
	public NSArray closedChildren() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOOrQualifier(qual)) ;
    }
	
	public NSArray tasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
		
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Task'", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version ='" + version() + "'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	

	public NSArray openTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(tasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray<Item> openAndResolvedTasks() {
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));

		return (NSArray<Item>)EOQualifier.filteredArrayWithQualifier(tasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray resolvedTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(tasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray closedTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(tasks(), new EOOrQualifier(qual)) ;
    }

	public NSArray stories() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
		
		//Stories
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Story'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(children(), new EOAndQualifier(qual)) ;
    }
	
	public NSArray allStories() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
		
		//Stories
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Story'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	
	public NSArray openNonTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray openAndResolvedNonTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));		
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray resolvedNonTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray closedNonTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasks(), new EOOrQualifier(qual)) ;
    }
	
	public NSArray nonTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Non Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type != 'Task'", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version ='" + version() + "'", null));

		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	
	public NSArray tasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
		
		//QA Tasks and Tasks
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type ='QA Task'", null));
		qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Task'", null));
		qual.addObject(new EOOrQualifier(qual1));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version ='" + version() + "'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	public NSArray openTasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(tasksAndQATasks(), new EOOrQualifier(qual)) ;
    }

	
	public NSArray  closedTasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(tasksAndQATasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray nonTasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
		
		//QA Tasks and Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type !='QA Task'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type !='Task'", null));
		//qual.addObject(new EOAndQualifier(qual1));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version ='" + version() + "'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	
	public NSArray openNonTasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasksAndQATasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray resolvedNonTasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasksAndQATasks(), new EOOrQualifier(qual)) ;
    }
	public NSArray  closedNonTasksAndQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonTasksAndQATasks(), new EOOrQualifier(qual)) ;
    }
	
	public NSArray qaTasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
		
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='QA Task'", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version ='" + version() + "'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	
	public NSArray openQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(qaTasks(), new EOOrQualifier(qual)) ;
    }
	
	public NSArray resolvedQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(qaTasks(), new EOOrQualifier(qual)) ;
    }


	public NSArray closedQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(qaTasks(), new EOOrQualifier(qual)) ;
    }

	public NSArray nonQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Non Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("type != 'QA Task'", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("version ='" + version() + "'", null));

		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOAndQualifier(qual)) ;
    }
	
	public NSArray closedNonQATasks() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Tasks
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(nonQATasks(), new EOOrQualifier(qual)) ;
    }
	
	/*
	
	public NSArray openNonTasksSameVersion() {
		return filterSameVersion(openNonTasks());
	} 
	public NSArray closedNonTasksSameVersion() {
		return filterSameVersion(closedNonTasks());
	} 
	public NSArray nonTasksSameVersion() {
		return filterSameVersion(nonTasks());
	} 
	*/
	public NSArray filterSameVersion(NSArray pUnfiltered) {
        NSMutableArray filtered = new NSMutableArray();
        Enumeration enumer;

        enumer = pUnfiltered.objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
			if(version().equals(tempEo.version())) {  // only add if the child is the same version as the parent.
				filtered.addObject(tempEo);
			}
        }
		return (NSArray)filtered;
	}

	
	public NSArray grandChildren() {
        Enumeration enumer;
        NSMutableArray grand = new NSMutableArray();

        enumer = children().objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            grand.addObjectsFromArray(tempEo.allChildren());
        }
        return (NSArray)grand;
    }

    public String allChildrenString() {
        StringBuffer allItems = new StringBuffer();

        Enumeration enumer = allChildren().objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            allItems.append((Number)tempEo.valueForKey("bugId") + ",");

        }
        return allItems.toString();

    }
	
	public boolean isWontFix() {
		/*String resolution = resolution();
		if(resolution == null) {
			resolution = "";
		}
		*/
		return resolution().equals("WONTFIX")?true:false;
	}

	
	public boolean isOpen() {
		boolean returnVal = false;
		String status = bugStatus();
		if((status.equals("NEW")) || (status.equals("ASSIGNED"))|| (status.equals("REOPENED"))|| (status.equals("RESOLVED")) ) {
			returnVal = true;
		}
		return returnVal;
	}
	public NSArray allOpenChildren() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Open
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='NEW'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CONFIRMED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='ASSIGNED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='REOPENED'", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='RESOLVED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOOrQualifier(qual)) ;
    }	
	
	public NSArray allClosedChildren() {
        EOFetchSpecification fs;
        NSMutableArray qual = new NSMutableArray();
			
		//Closed
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='VERIFIED'", null));		
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus ='CLOSED'", null));
			
		return (NSArray)EOQualifier.filteredArrayWithQualifier(allChildren(), new EOOrQualifier(qual)) ;
    }
	
	public NSArray allOpenChildrenSameVersion() {
        return (NSArray)filterSameVersion(allOpenChildren());
    }

    public int numOpenChildren() {
        return allOpenChildren().count();
    }

    public int numChildren() {
        return allChildren().count();
    }
    public int numChildrenSameVersion() {
        return allChildrenSameVersion().count();
    }
    public boolean hasChildren() {
        return numChildren() > 0 ? true:false;
    }
    public boolean hasChildrenSameVersion() {
        return numChildrenSameVersion() > 0 ? true:false;
    }
	
	public int currentEstimate() {		
		return numHoursWorked() + remainingTime().intValue();
	}
	
	public int allChildrenCurrentEstimate() {
		int childrenCurrentEstimate = 0;
		Enumeration enumer = allChildren().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item aChild = (Item)enumer.nextElement();
			childrenCurrentEstimate += aChild.currentEstimate();
		}
		return childrenCurrentEstimate;
		
	}
	
	public int allChildrenSameVersionCurrentEstimate() {
		int childrenCurrentEstimate = 0;
		Enumeration enumer = allChildrenSameVersion().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item aChild = (Item)enumer.nextElement();
			childrenCurrentEstimate += aChild.currentEstimate();
		}
		return childrenCurrentEstimate;
		
	}
	
	public int allChildrenHoursWorked() {
		int hoursWorked = 0;
		Enumeration enumer = allChildren().objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			hoursWorked += (Integer)aTask.valueForKey("numHoursWorked");
		}
		return hoursWorked;
		
	}
	
	public int allChildrenHoursRemaining() {
		int tasksHoursRemaining = 0;
		Enumeration enumer = allChildren().objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			tasksHoursRemaining += ((BigDecimal)aTask.valueForKey("remainingTime")).intValue();
		}
		return tasksHoursRemaining;
		
	}
	
	public int allChildrenSameVersionHoursRemaining() {
		int tasksHoursRemaining = 0;
		Enumeration enumer = allChildrenSameVersion().objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			tasksHoursRemaining += ((BigDecimal)aTask.valueForKey("remainingTime")).intValue();
		}
		return tasksHoursRemaining;
		
	}
	
	// find the last modified child
	public String lastModifiedAll() {
		NSTimestamp lastMod = null;
		NSTimestamp	today = new NSTimestamp();

		Enumeration enumer = allChildren().objectEnumerator();
		while(enumer.hasMoreElements()) {
			Item anItem = (Item)enumer.nextElement();
			if(lastMod == null) {
				lastMod = anItem.lastdiffed();
			}
			else {
				if(lastMod.compare(anItem.lastdiffed()) < 0) {
					lastMod = anItem.lastdiffed();
				}
			}
		}
		return elapsedTimeSimple(lastMod, today);
	}
	public String lastModified() {
		return elapsedTimeSimple(lastdiffed(), new NSTimestamp());
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


    public double numericEstimate() {
        return estimatedTime().doubleValue();
		//return 0.0;
    }
	
	public String percentCompleteCountChildren() {
		int fixed = closedChildren().count();
		int total = allChildren().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
	public String percentCompleteCountChildrenSameVersion() {
		int fixed = filterSameVersion(closedChildren()).count();
		int total = filterSameVersion(allChildren()).count();
		
		
		// Special case for No children and story is closed - Forcing 100%
		if((fixed==0) && (total==0) && (bugStatus().equals("CLOSED") || bugStatus().equals("VERIFIED"))) {
			fixed = 1;
			total = 1;
		}
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}

	public String percentCompleteCountTasks() {
		int fixed = closedTasks().count();
		int total = tasks().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}

	public String percentCompleteCountNonTasks() {
		int fixed = closedNonTasks().count();
		int total = nonTasks().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
	
	public String percentCompleteCountQATasks() {
		//int fixed = filterSameVersion(closedQATasks()).count();
		//int total = filterSameVersion(qaTasks()).count();
		int fixed = closedQATasks().count();
		int total = qaTasks().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}

	public String percentCompleteCountNonQATasks() {
		int fixed = closedNonQATasks().count();
		int total = nonQATasks().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
	//---
	
	public String percentCompleteCountTasksAndQATasks() {
		int fixed = closedTasksAndQATasks().count();
		int total = tasksAndQATasks().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}

	public String percentCompleteCountNonTasksAndQATasks() {
		int fixed = closedNonTasksAndQATasks().count();
		int total = nonTasksAndQATasks().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
	
	/*
	public String percentCompleteCountNonTasksSameVersion() {
		int fixed = closedNonTasksSameVersion().count();
		int total = nonTasksSameVersion().count();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
	*/
	
	public double percentCompleteChildren() {
        return percentCompleteForItemsInArray(allChildren());
    }

	public String percentCompleteChildrenString() {
        return percentCompleteStringFromDouble(percentCompleteChildren());
    }
	public String percentCompleteHoursTasks() {
        return percentCompleteStringFromDouble(percentCompleteForItemsInArray(tasks()));
    }
	public String percentCompleteHoursTasksAndQATasks() {
        return percentCompleteStringFromDouble(percentCompleteForItemsInArray(tasksAndQATasks()));
    }
	public String percentCompleteHoursNonTasks() {
        return percentCompleteStringFromDouble(percentCompleteForItemsInArray(nonTasks()));
    }
	public String percentCompleteHoursAllChildren() {
        return percentCompleteStringFromDouble(percentCompleteForItemsInArray(allChildren()));
    }	
	public String percentCompleteHoursAllChildrenSameVersion() {
        return percentCompleteStringFromDouble(percentCompleteForItemsInArray(allChildrenSameVersion()));
    }
    public double percentComplete() {
        return ((double)numHoursWorked()/currentEstimate()) * 100;
    }
	public String percentCompleteString() {
        return percentCompleteStringFromDouble(percentComplete());
    }

	public double percentCompleteForItemsInArray(NSArray pArray) {
		double hours = 0.0;
		double current = 0.0;
		
        Enumeration enumer = pArray.objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item temp = (Item)enumer.nextElement();
            hours += (double)temp.numHoursWorked();
            current += (double)temp.currentEstimate();
        }
        return ((double)hours/(double)current) * 100;
    }
	
	
	public String percentCompleteStringFromDouble(double pPercentComplete) {
        String returnVal = null;
        if(Double.isNaN(pPercentComplete)) {
                returnVal = "---";
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(pPercentComplete)) + "%";
            }
            catch(Exception e) {
                System.err.println("Item.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }
	
	public int numHoursWorked() {
	
		if(numHoursWorked == -1) {
			//System.out.println("Item.numHoursWorked() numHoursWorked == -1");
			
			numHoursWorked = 0;
			Enumeration enumer = hoursWorked().objectEnumerator();
			while(enumer.hasMoreElements()) {
				EOEnterpriseObject tempActivity = (EOEnterpriseObject)enumer.nextElement();
				BigDecimal val = new BigDecimal((String)tempActivity.valueForKey("added"));
				numHoursWorked += val.intValue();
			}
		}
		return numHoursWorked;
		
	}
	public int tasksHoursRemaining() {
		int tasksHoursRemaining = 0;
		Enumeration enumer = openTasks().objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			tasksHoursRemaining += ((BigDecimal)aTask.valueForKey("remainingTime")).intValue();
		}
		return tasksHoursRemaining;
		
	}
	public int tasksAndQATasksHoursRemaining() {
		int tasksHoursRemaining = 0;
		Enumeration enumer = openTasksAndQATasks().objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aTask = (EOEnterpriseObject)enumer.nextElement();
			tasksHoursRemaining += ((BigDecimal)aTask.valueForKey("remainingTime")).intValue();
		}
		return tasksHoursRemaining;
		
	}

	public NSArray hoursWorked() {
		//System.out.println("Item.hoursWorked() - " + bugId());
		NSArray hoursWorked;
		NSDictionary resolutionBindings = new NSDictionary(new Object[] {bugId()}, new Object[] { "bugId",});

		EOFetchSpecification fs = EOFetchSpecification.fetchSpecificationNamed( "hoursWorked", "BugsActivity").fetchSpecificationWithQualifierBindings( resolutionBindings );
		fs.setRefreshesRefetchedObjects(true);
		hoursWorked = (NSArray)editingContext().objectsWithFetchSpecification(fs);
		//System.out.println("\tItem.hoursWorked() count - " + hoursWorked.count());

		return hoursWorked;

    }
	
    public NSArray versions() {
		NSDictionary bindings = new NSDictionary(new Object[] {bugId()}, new Object[] { "bugId"});

		EOFetchSpecification fs = EOFetchSpecification.fetchSpecificationNamed( "versions", "BugsActivity").fetchSpecificationWithQualifierBindings( bindings );
		return (NSArray)editingContext().objectsWithFetchSpecification(fs);

    }
	
	public String versionFoundIn() {
		String returnVal = null;
		Enumeration enumer = versions().objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject tempActivity = (EOEnterpriseObject)enumer.nextElement();
			//Integer fieldId = (Integer)tempActivity.valueForKey("fieldid");
			//System.out.print("\tWho: " + (Integer)tempActivity.valueForKey("who"));
			//System.out.print("\tFieldId: " + fieldId);
			//System.out.print("\tAdded: " + (String)tempActivity.valueForKey("added"));
			//System.out.println("\tRemoved: " + (String)tempActivity.valueForKey("removed"));
			//if(fieldId.intValue() == 5) { // (5 = version)
				returnVal = (String)tempActivity.valueForKey("removed");
				break;
			//}
		}
		if(returnVal == null) {
			returnVal = version();
		}
		return returnVal;
	}
	
	public NSArray activities() {
        willRead();
		
		NSArray sortedObjects = EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)storedValueForKey("activities"), new NSArray(new Object[] {EOSortOrdering.sortOrderingWithKey("bugWhen", EOSortOrdering.CompareAscending)}));

        //return (NSArray)storedValueForKey("activities");
		return sortedObjects;
    }
	
	public NSArray parents() {
        willRead();
        return (NSArray)storedValueForKey("parents");
    }

	public NSArray parentStories() {
        Enumeration enumer = parents().objectEnumerator();
        NSMutableArray temp = new NSMutableArray();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            if((temp.containsObject(tempEo) == false) && ((tempEo.type().equals("Story")) || (tempEo.type().equals("Epic")))) {
                temp.addObject(tempEo);
            }
        }
        return temp;
    }

	public NSArray allParents() {
        if(allParents == null) {
            NSMutableArray all = new NSMutableArray();
            NSMutableArray tempArray = new NSMutableArray();
            all.addObjectsFromArray(parents());
            all.addObjectsFromArray(grandParents());

            // Remove duplicates
            Enumeration enumer = all.objectEnumerator();
            while(enumer.hasMoreElements()) {
                Item tempEo = (Item)enumer.nextElement();

                if(tempArray.containsObject(tempEo) == false) {
                    tempArray.addObject(tempEo);
                }
            }
            allParents = new NSArray(tempArray);
        }
        return allParents;
    }

    public NSArray topMostParents() {
        Enumeration enumer = allParents().objectEnumerator();
        NSMutableArray temp = new NSMutableArray();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            if((temp.containsObject(tempEo) == false) && (tempEo.parents().count() == 0)) {
                temp.addObject(tempEo);
            }
        }
        return temp;
    }
    
    public NSArray topMostStory() {
        Enumeration enumer = allParents().objectEnumerator();
        NSMutableArray temp = new NSMutableArray();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            if((temp.containsObject(tempEo) == false) && (tempEo.parents().count() == 0) && ((tempEo.type().equals("Story")) || (tempEo.type().equals("Epic")))) {
                temp.addObject(tempEo);
            }
        }
        return temp;
    }

    public boolean isTopMostParents() {
        return (topMostParents().count() > 0)?true:false;
    }
    public int numParents() {
        return allParents().count();
    }
    public boolean hasParents() {
        return numParents() > 0 ? true:false;
    }
    public NSArray grandParents() {
        Enumeration enumer;
        NSMutableArray grand = new NSMutableArray();

        enumer = parents().objectEnumerator();
        while(enumer.hasMoreElements()) {
            Item tempEo = (Item)enumer.nextElement();
            grand.addObjectsFromArray(tempEo.allParents());
        }
        return (NSArray)grand;
    }
    public String bugURL() {
        return bugzillaHostUrl + "/bugzilla/show_bug.cgi?id=" +storedValueForKey("bugId");
    }
    public String treeURL() {
        return bugzillaHostUrl + "/bugzilla/showdependencytree.cgi?id=" +storedValueForKey("bugId");
    }	
	
    public String targetMilestone() {
        return (String)storedValueForKey("targetMilestone");
    }

    public void setTargetMilestone(String value) {
        takeStoredValueForKey(value, "targetMilestone");
    }
    
	public String targetMilestoneLabel() {
		String returnVal;
		
		if(targetMilestone().equals("---"))
			returnVal = "---";
		else if(targetMilestone().equals("S1"))
			returnVal = "Sprint 1";
		else if(targetMilestone().equals("S2"))
			returnVal = "Sprint 2";
		else if(targetMilestone().equals("S3"))
			returnVal = "Sprint 3";
		else if(targetMilestone().equals("S4"))
			returnVal = "Sprint 4";
		else if(targetMilestone().equals("S5"))
			returnVal = "Sprint 5";
		else if(targetMilestone().equals("S6"))
			returnVal = "Sprint 6";
		else if(targetMilestone().equals("S7"))
			returnVal = "Sprint 7";
		else if(targetMilestone().equals("S8"))
			returnVal = "Sprint 8";
		else if(targetMilestone().equals("S9"))
			returnVal = "Sprint 9";
		else if(targetMilestone().equals("S10"))
			returnVal = "Sprint 10";
		else if(targetMilestone().equals("S11"))
			returnVal = "Sprint 11";
		else if(targetMilestone().equals("S12"))
			returnVal = "Sprint 12";
		else if(targetMilestone().equals("S13"))
			returnVal = "Sprint 13";
		else if(targetMilestone().equals("S14"))
			returnVal = "Sprint 14";
		else
			returnVal = targetMilestone();
			
		return returnVal;

	}

	public NSArray salesforceCases() {
		StringTokenizer cases;
		NSMutableArray caseArray;
		 
		cases = new StringTokenizer(sfCases(), ",");
		caseArray = new NSMutableArray();
			
		while(cases.hasMoreElements()) {
			caseArray.addObject((String)cases.nextToken());
		}
			
		return (NSArray)caseArray;
	}
	
    public String sfCases() {
        return (String)storedValueForKey("sfCases");
    }
	
    public String resolution() {
        return (String)storedValueForKey("resolution");
    }

    public void setResolution(String value) {
        takeStoredValueForKey(value, "resolution");
    }

    public BigDecimal remainingTime() {
        return (BigDecimal)storedValueForKey("remainingTime");
    }

    public void setRemainingTime(BigDecimal value) {
        takeStoredValueForKey(value, "remainingTime");
    }

    public String bugFileLoc() {
        return (String)storedValueForKey("bugFileLoc");
    }

    public void setBugFileLoc(String value) {
        takeStoredValueForKey(value, "bugFileLoc");
    }

    public NSTimestamp creationTs() {
        return (NSTimestamp)storedValueForKey("creationTs");
    }

    public void setCreationTs(NSTimestamp value) {
        takeStoredValueForKey(value, "creationTs");
    }

    public Number votes() {
        return (Number)storedValueForKey("votes");
    }

    public void setVotes(Number value) {
        takeStoredValueForKey(value, "votes");
    }

    public String alias() {
        return (String)storedValueForKey("alias");
    }

    public void setAlias(String value) {
        takeStoredValueForKey(value, "alias");
    }

    public String bugSeverity() {
        return (String)storedValueForKey("bugSeverity");
    }

    public void setBugSeverity(String value) {
        takeStoredValueForKey(value, "bugSeverity");
    }

    public BigDecimal estimatedTime() {
        return (BigDecimal)storedValueForKey("estimatedTime");
    }

    public void setEstimatedTime(BigDecimal value) {
        takeStoredValueForKey(value, "estimatedTime");
    }

    public String bugStatus() {
        return (String)storedValueForKey("bugStatus");
    }

    public void setBugStatus(String value) {
        takeStoredValueForKey(value, "bugStatus");
    }

    public String repPlatform() {
        return (String)storedValueForKey("repPlatform");
    }

    public void setRepPlatform(String value) {
        takeStoredValueForKey(value, "repPlatform");
    }

    public Number productId() {
        return (Number)storedValueForKey("productId");
    }

    public void setProductId(Number value) {
        takeStoredValueForKey(value, "productId");
    }

    public Number qaContactId() {
        return (Number)storedValueForKey("qaContactId");
    }

    public void setQaContactId(Number value) {
        takeStoredValueForKey(value, "qaContactId");
    }

    public NSTimestamp deltaTs() {
        return (NSTimestamp)storedValueForKey("deltaTs");
    }

    public void setDeltaTs(NSTimestamp value) {
        takeStoredValueForKey(value, "deltaTs");
    }

    public String shortDesc() {
        return (String)storedValueForKey("shortDesc");
    }

    public void setShortDesc(String value) {
        takeStoredValueForKey(value, "shortDesc");
    }

    public NSTimestamp deadline() {
        return (NSTimestamp)storedValueForKey("deadline");
    }

    public void setDeadline(NSTimestamp value) {
        takeStoredValueForKey(value, "deadline");
    }

    public Number bugId() {
        return (Number)storedValueForKey("bugId");
    }

    public void setBugId(Number value) {
        takeStoredValueForKey(value, "bugId");
    }

    public String statusWhiteboard() {
        return (String)storedValueForKey("statusWhiteboard");
    }

    public void setStatusWhiteboard(String value) {
        takeStoredValueForKey(value, "statusWhiteboard");
    }

    public Number everconfirmed() {
        return (Number)storedValueForKey("everconfirmed");
    }

    public void setEverconfirmed(Number value) {
        takeStoredValueForKey(value, "everconfirmed");
    }

    public String opSys() {
        return (String)storedValueForKey("opSys");
    }

    public void setOpSys(String value) {
        takeStoredValueForKey(value, "opSys");
    }

    public String simplePriority() {
        return priority().substring(0,1);
    }
    public String priority() {
        return (String)storedValueForKey("priority");
    }

    public void setPriority(String value) {
        takeStoredValueForKey(value, "priority");
    }
    public String type() {
        return (String)storedValueForKey("type");
    }

    public void setType(String value) {
        takeStoredValueForKey(value, "type");
    }
    
    public String relNum() {
        return (String)storedValueForKey("relNum");
    }

    public void setRelNum(String value) {
        takeStoredValueForKey(value, "relNum");
    }    
    
/*
    public String keywords() {
        return (String)storedValueForKey("keywords");
    }

    public void setKeywords(String value) {
        takeStoredValueForKey(value, "keywords");
    }
*/
    public Number cclistAccessible() {
        return (Number)storedValueForKey("cclistAccessible");
    }

    public void setCclistAccessible(Number value) {
        takeStoredValueForKey(value, "cclistAccessible");
    }

    public String versionShort() {
        String[] temp = version().split("\\(");
		return temp[0].trim();
    }
    
    public String version() {
        return (String)storedValueForKey("version");
    }

    public void setVersion(String value) {
        takeStoredValueForKey(value, "version");
    }

    public NSTimestamp lastdiffed() {
        return (NSTimestamp)storedValueForKey("lastdiffed");
    }

    public void setLastdiffed(NSTimestamp value) {
        takeStoredValueForKey(value, "lastdiffed");
    }

    public EOEnterpriseObject qaContact() {
        return (EOEnterpriseObject)storedValueForKey("qaContact");
    }

    public void setQaContact(EOEnterpriseObject value) {
        takeStoredValueForKey(value, "qaContact");
    }

    public EOEnterpriseObject component() {
        return (EOEnterpriseObject)storedValueForKey("component");
    }

    public void setComponent(EOEnterpriseObject value) {
        takeStoredValueForKey(value, "component");
    }

    public EOEnterpriseObject product() {
        return (EOEnterpriseObject)storedValueForKey("product");
    }

    public void setProduct(EOEnterpriseObject value) {
        takeStoredValueForKey(value, "product");
    }

	public String productShort() {
		EOEnterpriseObject product =  (EOEnterpriseObject)storedValueForKey("product");
		String productName = (String)product.valueForKey("productName");
				
		if(productName.contains("Desktop")){
			productName = "Desktop";
		}
		else if(productName.contains("Mobile")){
			productName = "Mobile";
		}
		else if(productName.contains("Trusted Access")){
			productName = "TAB";
		}
		else if(productName.contains("Services")){
			productName = "Services";
		}
		else if(productName.contains("OCM")){
			productName = "OCM";
		}
		
		return productName;
	}

    public EOEnterpriseObject assignee() {
        return (EOEnterpriseObject)storedValueForKey("assignee");
    }

    public void setAssignee(EOEnterpriseObject value) {
        takeStoredValueForKey(value, "assignee");
    }

    public EOEnterpriseObject reporter() {
        return (EOEnterpriseObject)storedValueForKey("reporter");
    }

    public void setReporter(EOEnterpriseObject value) {
        takeStoredValueForKey(value, "reporter");
    }
    public String hot() {
        return (String)storedValueForKey("hot");
    }

    public void setHot(String value) {
        takeStoredValueForKey(value, "hot");
    }
	public boolean isHot() {
		return (hot().equals("true")?true:false);
	}
    public String storyReviewed() {
        return (String)storedValueForKey("storyReviewed");
    }
    public void setStoryReviewed(String value) {
        takeStoredValueForKey(value, "storyReviewed");
    }
	public boolean isStoryReviewedSet() {		
		return (storyReviewed().equals("---"))?true:false;
	}
	public boolean isStoryReviewed() {		
		return (storyReviewed().equals("Yes"))?true:false;
	}
	public boolean isStoryNotReviewed() {		
		return (storyReviewed().equals("No"))?true:false;
	}
	public boolean isStoryReviewedNA() {		
		return (storyReviewed().equals("N/A"))?true:false;
	}
	
    public String engDocsCompleted() {
        return (String)storedValueForKey("engDocsCompleted");
    }
    public void setEngDocsCompleted(String value) {
        takeStoredValueForKey(value, "engDocsCompleted");
    }
	
	public boolean isEngDocsCompletedSet() {		
		return (engDocsCompleted().equals("---"))?true:false;
	}
	public boolean isEngDocsCompleted() {		
		return (engDocsCompleted().equals("Yes"))?true:false;
	}
	public boolean isEngDocsNotCompleted() {		
		return (engDocsCompleted().equals("No"))?true:false;
	}
	public boolean isEngDocsCompletedNA() {		
		return (engDocsCompleted().equals("N/A"))?true:false;
	}

    public String codeReviewed() {
        return (String)storedValueForKey("codeReviewed");
    }
    public void setCodeReviewed(String value) {
        takeStoredValueForKey(value, "codeReviewed");
    }
	
	public boolean isCodeReviewedSet() {		
		return (codeReviewed().equals("---"))?true:false;
	}
	public boolean isCodeReviewed() {		
		return (codeReviewed().equals("Yes"))?true:false;
	}
	public boolean isCodeNotReviewed() {		
		return (codeReviewed().equals("No"))?true:false;
	}
	public boolean isCodeReviewedNA() {		
		return (codeReviewed().equals("N/A"))?true:false;
	}

    public String unitTestsCompleted() {
        return (String)storedValueForKey("unitTestsCompleted");
    }
    public void setUnitTestsCompleted(String value) {
        takeStoredValueForKey(value, "unitTestsCompleted");
    }
	
	public boolean isUnitTestsCompletedSet() {		
		return (unitTestsCompleted().equals("---"))?true:false;
	}
	public boolean isUnitTestsCompleted() {		
		return (unitTestsCompleted().equals("Yes"))?true:false;
	}
	public boolean isUnitTestsNotCompleted() {		
		return (unitTestsCompleted().equals("No"))?true:false;
	}
	public boolean isUnitTestsCompletedNA() {		
		return (unitTestsCompleted().equals("N/A"))?true:false;
	}
	
}
