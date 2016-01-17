package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

public class TopicStatus {
    protected String topicName;
    /** @TypeInfo Item */
    protected EOEnterpriseObject topicEo;
    protected NSMutableDictionary statusItems;
    protected String filterKey = "targetMilestone";
    protected EOEditingContext ec;
    protected double estimateForAll = 0.0;
    protected double remainForAll = 0.0;
    protected double closedForAll = 0.0;
    protected int countForAll = 0;
    protected int countOpenForAll = 0;
    protected NSArray taskTypes;
    protected StatusItem taskItem;
    protected StatusItem allItem;
   

    public TopicStatus() {
        statusItems = new NSMutableDictionary();
    }
    public TopicStatus(String pTopicName) {
        setTopicName(pTopicName);
        statusItems = new NSMutableDictionary();         
    }
    public TopicStatus(String ptopicName, EOEnterpriseObject pEo) {
        setTopicName(ptopicName);
        setTopicEo(pEo);
        statusItems = new NSMutableDictionary();
   }

    public boolean addItemForType(Item pEO, String pItemType) {
        EOFetchSpecification resolutionfs;
        NSDictionary resolutionBindings = null;
        NSTimestamp whenResolved;

        StatusItem tempStatus = (StatusItem)statusItems.objectForKey(pItemType);
        if(tempStatus == null) {
            tempStatus = statusItemFactory();
			statusItems.setObjectForKey(tempStatus, pItemType);
        }
		
        String key = (String)pEO.valueForKeyPath(filterKey);  // This will be 'M1', 'M2', 'CC',etc if filterKey is targetmilestone
        String status = (String)pEO.valueForKeyPath("bugStatus");
        if((status.equals("NEW")) || (status.equals("ASSIGNED")) || (status.equals("REOPENED")) || (status.equals("CONFIRMED")) || (status.equals("UNCONFIRMED"))) {
            tempStatus.addOpenItemForKey(pEO, key);
         }
        else if(status.equals("RESOLVED")) {
            tempStatus.addResolvedItemForKey(pEO, key);
         }
		 
        else {
                tempStatus.addClosedItemForKey(pEO, key);
        }
        return true;
    }
    
    public StatusItem itemForType(String pItemType) {
        //System.out.println("TopicStatus.itemForType - " + pItemType);

        return (StatusItem)statusItems.objectForKey(pItemType);
    }
    public StatusItem allItem() {
        //System.out.println("TopicStatus.allItem - " + topicName);

        if(allItem == null) {
            Enumeration enumer = statusItems().keyEnumerator();
            allItem = statusItemFactory();
            while(enumer.hasMoreElements()) {
                String currKey = (String)enumer.nextElement();
                allItem.addValuesFromStatusItem((StatusItem)statusItems().objectForKey(currKey));
            }
            //System.out.println("TopicStatus.allItem - " + allItem.toString());
        }
        return allItem;
    }
	/*
    public StatusItem taskItem() {

        if(taskItem == null) {
            //System.out.println("TopicStatus.taskItem() - "+topicName + " - " + taskTypes);

            Object itemTyp[]={"Task"};
            boolean added = false;
            NSArray itemTypes = new NSArray(itemTyp);

            // Default to WorkItems
            if((taskTypes == null) || (taskTypes.count() == 0)) {
                taskItem = itemForType("Task");
            }
            // If only one requested - return it directly
            else if((taskTypes.count() == 1) && (itemTypes.containsObject((String)taskTypes.objectAtIndex(0)))) {
                taskItem = itemForType((String)taskTypes.objectAtIndex(0));
            }
            else { // If more than one requested - create a new  StatusItem and merge them
                Enumeration enumer = taskTypes.objectEnumerator();
                taskItem = statusItemFactory();
                while(enumer.hasMoreElements()) {
                    String currType = (String)enumer.nextElement();
                    if(itemTypes.containsObject(currType) == true) {
                        taskItem.addValuesFromStatusItem((StatusItem)itemForType(currType));
                        added = true;
                        //System.out.println("TopicStatus.taskItem() - currType - "+ topicName + " - " + currType);
                    }
                }
                // Verify that atleast one was added
                if(added == false) {
                    taskItem = itemForType("Task");
                }
                //System.out.println("TopicStatus.taskItem() - " + taskItem.toString());
            }
        }

        return taskItem;
    }
	*/
    public StatusItem deferredItem() {
        return itemForType("Deferred");
    }
    public StatusItem taskItem() {
        return itemForType("Task");
    }
    public StatusItem bugItem() {
        return itemForType("Bug");
    }
    public StatusItem qaTaskItem() {
        return itemForType("QA Task");
    }
    public StatusItem enhancementsItem() {
        return itemForType("Enhancements");
    }
    public StatusItem riskItem() {
        return itemForType("Risk");
    }
    public StatusItem otherItem() {
        return itemForType("Other");
    }
    public StatusItem workItem() {
        return itemForType("Work Item");
    }

    public StatusItem statusItemFactory() {
        StatusItem tempStatusItem = new StatusItem(topicName());

        return  tempStatusItem;
    }
    public boolean hasItems() {
        boolean returnVal = false;
        Enumeration enumer = statusItems().allKeys().objectEnumerator();
        while(enumer.hasMoreElements()) {
           StatusItem tempStatus = (StatusItem)enumer.nextElement();
            if(tempStatus.isItems() == true) {
                returnVal = true;
                break;
            }
        }
        return returnVal;
    }
    public String toString() {
        String returnVal = "";
        //Enumeration enumer = statusItems().allKeys().objectEnumerator();
        Enumeration enumer = statusItems().keyEnumerator();
        while(enumer.hasMoreElements()) {
           String statusKey = (String)enumer.nextElement();
            //System.out.println("statusKey - " + statusKey);
            StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
            returnVal += "\n" + statusKey + "\n" + si.toString();
        }
        return returnVal;
    }
	/*
    public double estimateForAll() {
        if(estimateForAll == 0.0) {
            Enumeration enumer = statusItems().allKeys().objectEnumerator();
            while(enumer.hasMoreElements()) {
               String statusKey = (String)enumer.nextElement();
                StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
                if(!statusKey.equals("Deferred")== true) {
                    estimateForAll += si.numHours();
                }
            }
        }
        return estimateForAll;
    }
	*/

    public EOEnterpriseObject productManager() {
        NSArray ccList;
        EOEnterpriseObject pm = null;

        ccList = (NSArray)topicEo.valueForKey("ccPerson");
        if((ccList != null) && (ccList.count() >0)) {
            pm = (EOEnterpriseObject)ccList.objectAtIndex(0);
        }
        else {
            pm = (EOEnterpriseObject)topicEo.valueForKey("originator");
        }
        return pm;
    }
    public boolean isAllEstimated() {
        boolean returnVal = true;
        //System.out.println("-------------topicName - " + topicName);

        Enumeration enumer = statusItems().allKeys().objectEnumerator();
        while(enumer.hasMoreElements()) {
            String statusKey = (String)enumer.nextElement();
            if((!statusKey.equals("Deferred")== true) && (!statusKey.equals("Defects")== true) && (!statusKey.equals("Other")== true)){
                //System.out.println("statusKey - " + statusKey);
                StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
                if(si.isEstimated() == false) {
                    returnVal = false;
                    break;
                }
            }

        }
        //System.out.println();

        return returnVal;
    }
	
	
	public boolean isAllTasksEstimated() {
		boolean returnVal = false;
		StatusItem tasks = taskItem();
		if(tasks != null) {
			if(tasks.isItems() == true) {
				returnVal = tasks.numNotEstimated()>0?false:true;
				//System.out.println("StatusItem.isEstimated() - " + toString() );

			}
			else {
				returnVal = true;  // no items so estimates are 'complete'
			}
		}
		else {
			returnVal = true;  // no tasks so estimates are 'complete'
		}
        return returnVal;
    }

	/*
    
    public double remainForAll() {
        if(remainForAll == 0.0) {
            Enumeration enumer = statusItems().allKeys().objectEnumerator();
            while(enumer.hasMoreElements()) {
                String statusKey = (String)enumer.nextElement();
                //System.out.println("statusKey - " + statusKey);
                StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
                remainForAll += si.numOpenHours;
            }
        }
        return remainForAll;
    }
    public double closedForAll() {
        if(closedForAll == 0.0) {
            Enumeration enumer = statusItems().allKeys().objectEnumerator();
            while(enumer.hasMoreElements()) {
                String statusKey = (String)enumer.nextElement();
                //System.out.println("statusKey - " + statusKey);
                StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
                closedForAll += si.numClosedHours;
            }
        }
        return closedForAll;
    }
	*/
    public int countForAll() {
        if(countForAll == 0) {
            Enumeration enumer = statusItems().allKeys().objectEnumerator();
            while(enumer.hasMoreElements()) {
                String statusKey = (String)enumer.nextElement();
                //System.out.println("statusKey - " + statusKey);
                StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
                countForAll += si.numItems();
            }
        }
        return countForAll;
    }
    public int countOpenForAll() {
        if(countOpenForAll == 0) {
            Enumeration enumer = statusItems().allKeys().objectEnumerator();
            while(enumer.hasMoreElements()) {
                String statusKey = (String)enumer.nextElement();
                //System.out.println("statusKey - " + statusKey);
                StatusItem si = (StatusItem)statusItems().objectForKey(statusKey);
                countOpenForAll += si.numOpenItems;
            }
        }
        return countOpenForAll;
    }
	/*
    public double percentComplete() {
        //System.out.println(displayName + " - numOpenHours/numClosedHours - " + numOpenHours + " / " + numClosedHours);
        return (closedForAll()/estimateForAll()) * 100;}
    
    public String percentCompleteString() {
        String returnVal = null;
        double val = percentComplete();
        if(Double.isNaN(val)) {
            returnVal = "NA";
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(val)) + "%";
            }
            catch(Exception e) {
                System.err.println("TopicStatus.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }
	*/

    public String urlForChildren() {
        return ((Application)Application.application()).bugzillaHostUrl()  + "/bugzilla/buglist.cgi?bug_id=" +((String)((Item)topicEo).allChildrenString());

    }
    
    public NSMutableDictionary statusItems() {  return statusItems; }
    public String topicName() {  return topicName; }
    public void setTopicName(String pName) {  topicName = pName; }
    public EOEnterpriseObject topicEo() {  return topicEo; }
    public void setTopicEo(EOEnterpriseObject pValue) {  topicEo = pValue; }
    public EOEditingContext ec() {  return ec; }
    public void setEc(EOEditingContext pVal) {  ec = pVal; }
    public String filterKey() {  return filterKey; }
    public void setFilterKey(String pKey) {  filterKey = pKey; }
    public NSArray taskTypes() { return taskTypes;    }
    public void setTaskTypes(NSArray pArray) {
        //System.out.println("TopicStatus.setTaskTypes() - pArray - " + pArray);

        if((taskTypes != null) && (taskTypes.equals(pArray) == false)) {
            taskItem = null;
        }
        taskTypes = pArray;

    }
    //public void addTaskTypes(String pVal) { taskTypes.addObject(pVal);    }
   
}

