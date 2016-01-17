package com.ironkey.tracker;


import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;

public class Status {
    protected NSMutableDictionary topics;
    protected NSMutableArray topLevelTopics;
    protected NSArray sortedTopics;
    protected String selectedProject;
    protected EOEditingContext ec;
    protected NSTimestamp snapshotTime;
    protected Number itemId;
    protected NSArray taskTypes = new NSArray(new Object[] {"Tasks","Bugs"});

    public Status() {
        //System.out.println("Status.topics()" );

    }
    public Status(String pSelectedProject, EOEditingContext pEC) {
        //System.out.println("Status.topics()" );

        setSelectedProject(pSelectedProject);
        setEc(pEC);
    }

     /**
     * Performs the db queries for 
     * @return 	void
     */
	 //topLevelFeatures()
    public NSMutableArray topLevelTopics() {
        //System.out.println("++++++++++++++ Status.topLevelTopics() ++++++++++++++");
        if(topLevelTopics == null) {
            topLevelTopics = new NSMutableArray();
            ec().lock();

            Enumeration enumer;
            NSArray projectFeatures;
            EOFetchSpecification fs;
            NSDictionary bindings = null;
            snapshotTime = new NSTimestamp();

            if(itemId != null) {
                bindings = new NSDictionary(new Object[] { itemId}, new Object[] { "bugId"});
                fs=EOFetchSpecification.fetchSpecificationNamed("projectFeature", "Item").fetchSpecificationWithQualifierBindings(bindings);
            }
            else {
                //System.out.println("Status.topLevelTopics - selectedProject - " + selectedProject);
				// Set fetch spec to return all features
               // bindings = new NSDictionary(new Object[] { selectedProject, "Feature"}, new Object[] { "version", "type"});
                bindings = new NSDictionary(new Object[] { selectedProject}, new Object[] { "version"});
                fs=EOFetchSpecification.fetchSpecificationNamed("projectFeatures", "Item").fetchSpecificationWithQualifierBindings(bindings);
            }

            fs.setRefreshesRefetchedObjects(true);
          //  ec().refaultObjects(); 

            projectFeatures = (NSArray)ec().objectsWithFetchSpecification(fs); // sorted by assignee

            // Need to invalid all the objects because the parents relationship is cached.
            //NSMutableArray temp = new NSMutableArray();
            enumer = projectFeatures.objectEnumerator();
            while(enumer.hasMoreElements()) {
                Item item = (Item)enumer.nextElement();
                item.invalidateAllChildren();
                //temp.addObject(ec().globalIDForObject(item));
            }
           // ec().invalidateObjectsWithGlobalIDs((NSArray)temp);

            if(itemId == null) {
                enumer = projectFeatures.objectEnumerator();
                while(enumer.hasMoreElements()) {
                    EOEnterpriseObject anEo = (EOEnterpriseObject)enumer.nextElement();
                    NSArray parents = (NSArray)anEo.valueForKey("parents");
                    String priority  = (String)anEo.valueForKey("priority");
                    if(priority.equals("1 - Urgentblah")) {
                        //topLevelTopics.addObject(anEo);
                    }
                    else if(parents.count()==0) {  // all defects will fall into this category
                        topLevelTopics.addObject(anEo);
                    }
                }
            }
            else {
                topLevelTopics = new NSMutableArray(projectFeatures);

            }
            ec().unlock();

        }
        //System.out.println("topLevelTopics - " + topLevelTopics);
        return  topLevelTopics;
    }
	
	// Should rename this to featureStatus()
    public NSMutableDictionary topics() {
        //System.out.println("Status.topics()" );

        if(topics == null) {
            topics = new NSMutableDictionary();
            ec().lock();

            Enumeration enumer;
            int numItems = topLevelTopics().count();

            String itemName = null;
            StatusItem tempSI = null;
            // Start by iterating through top level features
            for (int i = 0; i < numItems; i++) {
                Item eo = (Item)topLevelTopics().objectAtIndex(i);
                eo.dropCachedChildren();
                itemName = (String)eo.valueForKeyPath("shortDesc");

                TopicStatus tempTS = (TopicStatus)topics.objectForKey(itemName);
                if(tempTS == null) {
                    tempTS = new TopicStatus(itemName, eo);
                    tempTS.setEc(ec());
                    topics.setObjectForKey(tempTS,itemName );
                    //addItemForAssignee(itemName, (String)eo.valueForKeyPath("assignee.realname")); // this is used to get sorted list of features
                    //addItemToSortedNames(itemName);
                }
                enumer = ((NSArray)eo.valueForKey("allChildren")).objectEnumerator();
                while(enumer.hasMoreElements()) {
                    Item child = (Item)enumer.nextElement();

                    // Check to see if the item is assigned to the current project or not
                    String version = (String)child.valueForKey("version");
                    String product = (String)child.valueForKeyPath("product.productName");
                    if((!selectedProject.equals("_ReleasePlanning")) &&(selectedProject.equals(version) != true)) {
                        tempTS.addItemForType(child, "Deferred");
                    }
                    else if(product.equals("Defects")) {
                        tempTS.addItemForType(child, "Defects");
                    }
                    else {
                      //  tempTS.addItemForType(child, "Other");
                        tempTS.addItemForType(child, "Defects");
                    }
                }
            }
            ec().unlock();

        }
        
        return topics;
    }


    public NSArray sortedTopics(){
        //System.out.println("Status.sortedTopics()" );

        if(sortedTopics == null) {
            // Sort Keys
            NSMutableArray sortedKeys = new NSMutableArray();
            Enumeration enumer = topLevelTopics().objectEnumerator();
            while(enumer.hasMoreElements()) {
                int i = 0;
                EOEnterpriseObject currItem = (EOEnterpriseObject)enumer.nextElement();
                String currTopic = (String)currItem.valueForKey("shortDesc");
                // Will reset the task types
                TopicStatus tempTS = (TopicStatus)topics().valueForKey(currTopic);
                if(taskTypes != null) {
                    tempTS.setTaskTypes(taskTypes);
                }

                int numItems = sortedKeys.count();
                for (; i < numItems; i++) {
                    String aItem = (String)sortedKeys.objectAtIndex(i);
                    if(aItem.compareTo(currTopic) > 0) {
                        break;
                    }
                }
                sortedKeys.insertObjectAtIndex(currTopic, i);
            }
            sortedTopics = new NSArray(sortedKeys);
        }
        return sortedTopics;
    }

    public void addTopicsFromStatus(Status pStatus) {
        //System.out.println("Status.addTopicsFromStatus()");
        Enumeration enumer;
        ec().lock();

        enumer=pStatus.topics().keyEnumerator();
        while(enumer.hasMoreElements()) {
            String topicName = (String)enumer.nextElement();
            addTopicToTopics((TopicStatus)pStatus.topics().objectForKey(topicName));
        }
        ec().unlock();
    }
    public String toString() {
        String returnVal = "";
        Enumeration enumer = topics.keyEnumerator();
        while(enumer.hasMoreElements()) {
            
            String tempKey = (String)enumer.nextElement();
            //System.out.println("Status.toString - " + tempKey);
            TopicStatus tempTS = (TopicStatus)topics.objectForKey(tempKey);
            returnVal += tempTS.toString() + "\n";
        }
        return returnVal;
    }


    // Accessors
    public String selectedProject() { return selectedProject; }
    public void setSelectedProject(String aProj) { selectedProject = aProj;}
    public EOEditingContext ec() {   return ec;}
    public void setEc(EOEditingContext pEC) { ec = pEC;}
    public Number itemId() { return itemId; }
    public void setItemId(Number pId) { itemId = pId;}
    public void setItemId(String pId) { itemId = new Integer(pId);}
    public void setTopics(NSMutableDictionary pDict) {
        topics = pDict;
    }
    public void addTopicToTopics(TopicStatus pTS) {
        //System.out.println("Status.addTopicToTopics() - Updating statusObject");
        ec().lock();
        topics.setObjectForKey(pTS, pTS.topicName());
        ec().unlock();
    }
    public TopicStatus topicFromTopicsForKey(String pItemName) {
        return (TopicStatus)topics.objectForKey(pItemName);
    }
    public NSArray taskTypes() { return taskTypes;}
    public void setTaskTypes(NSArray pValue) {
        if(taskTypes.equals(pValue) == false) {
            sortedTopics = null;
        }
        taskTypes = pValue;
    }
}

