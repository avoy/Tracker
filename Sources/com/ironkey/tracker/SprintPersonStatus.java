package com.ironkey.tracker;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;

public class SprintPersonStatus extends Status {
    public EOEnterpriseObject personItem;
    protected NSArray sortedOpenTaskTopics;
    protected NSArray sortedHasTaskTopics;
	protected NSMutableDictionary itemsForSprint;
	protected NSMutableDictionary topicsForSprints;
	public NSArray storiesForSelectedRelease;
	public NSArray itemsForSelectedRelease;	
	public NSArray sprintsWithItems;
	public NSArray selectedItemProducts;

	
//			NSArray sprintValues= new NSArray(new Object[] {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "---"});

// For each sprint
// - Get all the tasks and bugs (sort by person)
// - For each person - TopicStatus [bugs,tasks] [open,closed]


    public SprintPersonStatus(String pSelectedProject, EOEditingContext pEC, NSArray pProducts) {
        //System.out.println("Status.topics()" );

        setSelectedProject(pSelectedProject);
        setEc(pEC);
		selectedItemProducts = pProducts;
        
		//topicsForSprints();
       // ec().refaultObjects();
	   
		 /*
			Enumeration enumer = sprintsWithItems().objectEnumerator();
			while(enumer.hasMoreElements()) {
				String sprint = (String)enumer.nextElement();
				System.out.println("==== Sprint : " + sprint);
				
				NSArray people = (NSArray)topicKeysForSprint(sprint);
				Enumeration enumer2 = people.objectEnumerator();
				while(enumer2.hasMoreElements()) {
					String person = (String)enumer2.nextElement();
					System.out.println("\t====Person : " + person);
				}

			}
				
			*/
				
    }
	
	
	public NSArray storiesForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
		
		if(storiesForSelectedRelease == null) {
			//System.out.println("\n\n============ ReleasePlan.storiesForSelectedRelease()");

			//Session s = (Session)session();
			NSMutableArray qual = new NSMutableArray();
			NSMutableArray qual1 = new NSMutableArray();
			EOQualifier qualifier;

			//Stories
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Story'", null));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

			// Products
			Enumeration enumer = selectedItemProducts.objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			storiesForSelectedRelease= (NSArray)ec().objectsWithFetchSpecification(fs);
		}
		return storiesForSelectedRelease;
    }
	
	public NSArray itemsForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;

		if(itemsForSelectedRelease == null) {
			//System.out.println("\n\n============ ReleasePlan.bugsAndEnhancementsForSelectedRelease()");
			//Bugs and Enhancements
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
			//qual1.addObject(EOQualifier.qualifierWithQualifierFormat("type = 'Enhancement'", null));
			//qual.addObject(new EOOrQualifier(qual1));
			
			// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = selectedItemProducts.objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));

			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));


			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareCaseInsensitiveDescending),
					EOSortOrdering.sortOrderingWithKey("assignee.realname", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			itemsForSelectedRelease = (NSArray)ec().objectsWithFetchSpecification(fs);
		}
		return itemsForSelectedRelease;
    }

/*
 	public NSMutableDictionary itemsForSprint() {
 
		NSMutableArray tempArray;

		if(itemsForSprint == null) {
			itemsForSprint = new NSMutableDictionary();
			//NSArray sprintValues= new NSArray(new Object[] {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "---"});
			Enumeration enumer = storiesForSelectedRelease().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item story = (Item)enumer.nextElement();
				String sprint = (String)story.valueForKey("targetMilestone");
				tempArray = (NSMutableArray)itemsForSprint.objectForKey(sprint);
				if(tempArray == null) {
					tempArray = new NSMutableArray();
					itemsForSprint.setObjectForKey(tempArray, sprint);
				}
				// Remove duplicates
				Enumeration enumer2 = story.allChildren().objectEnumerator();
				while(enumer2.hasMoreElements()) {
					Item tempEo = (Item)enumer2.nextElement();

					if(tempArray.containsObject(tempEo) == false) {
						tempArray.addObject(tempEo);
					}
				}

			}
		}		
		return itemsForSprint;
	}
	*/
	public NSMutableDictionary itemsForSprint() {
		NSMutableArray tempArray;

		if(itemsForSprint == null) {
			itemsForSprint = new NSMutableDictionary();
			//NSArray sprintValues= new NSArray(new Object[] {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "---"});
			Enumeration enumer = itemsForSelectedRelease().objectEnumerator();
			while(enumer.hasMoreElements()) {
				Item item = (Item)enumer.nextElement();
				String sprint = (String)item.valueForKey("targetMilestone");
				tempArray = (NSMutableArray)itemsForSprint.objectForKey(sprint);
				if(tempArray == null) {
					tempArray = new NSMutableArray();
					itemsForSprint.setObjectForKey(tempArray, sprint);
				}
				
				tempArray.addObject(item);
			}
		}		
		return itemsForSprint;
	}

	public NSArray sprintsWithItems() {
		if(sprintsWithItems == null) {
			NSMutableArray sortedSprints = new NSMutableArray();
			NSArray sprintKeys= new NSArray(new Object[] {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9","S10","S11","S12","S13","S14", "---", "Triage"});
			Enumeration enumer = sprintKeys.objectEnumerator();
			while(enumer.hasMoreElements()) {
				String sprintKey = (String)enumer.nextElement();			
				if(itemsForSprint().allKeys().contains(sprintKey)) {
					sortedSprints.addObject(sprintKey);
				}
			}
			sprintsWithItems = new NSArray(sortedSprints);
		}
		return sprintsWithItems;
	}

	// itemsForSprint() will have a dictionary of all items in a sprint
	// topics() will convert those into a dictionary (of dictionaries) topicStatus objects for each sprint, where there is a topicStatus for each person who has an item in that sprint.
    public NSMutableDictionary topicsForSprints() {
        //System.out.println("SprintPersonStatus.topics - " + pType);
        String assignee = null;
		String type = null;
		if(topicsForSprints == null) {
			topicsForSprints = new NSMutableDictionary();
			NSMutableDictionary sprintTopics;
			
			//NSArray sprintKeys= new NSArray(new Object[] {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "---"});
			Enumeration enumer =  sprintsWithItems().objectEnumerator();
			while(enumer.hasMoreElements()) {
				String sprintKey = (String)enumer.nextElement();
				// Need to add a dictionary entry for each sprint which is an array of topicStatuses
				
				sprintTopics = (NSMutableDictionary)topicsForSprints.objectForKey(sprintKey);
				if(sprintTopics == null) {
					sprintTopics = new NSMutableDictionary();
					topicsForSprints.setObjectForKey(sprintTopics, sprintKey);
				}

				NSArray itemsForCurrentSprint = (NSArray)itemsForSprint().objectForKey(sprintKey);
				if(itemsForCurrentSprint != null) {
					Enumeration enumer2 = itemsForCurrentSprint.objectEnumerator();
					while(enumer2.hasMoreElements()) {
						Item eo = (Item)enumer2.nextElement();
						assignee = (String)eo.valueForKeyPath("assignee.realname");
						type = (String)eo.valueForKeyPath("type");

						TopicStatus tempTS = (TopicStatus)sprintTopics.objectForKey(assignee);
						if(tempTS == null) {
							tempTS = new TopicStatus(assignee, (EOEnterpriseObject)eo.valueForKey("assignee"));
							tempTS.setEc(ec());
							sprintTopics.setObjectForKey(tempTS,assignee ); // will add once
						}
						//if(type.equals("Task")) {
						//	tempTS.addItemForType(eo, "Task");
						//}
						//else if(type.equals("QA Task")) {
						//	tempTS.addItemForType(eo, "Task");
						//}
						//else {
							//tempTS.addItemForType(eo, "Bug");
							tempTS.addItemForType(eo, "Task");
						//}
					}
				}
			}
		}
		
		return topicsForSprints;
    }
	public NSArray topicKeysForSprint(String pSprint) {
		NSArray returnVal = null;
		NSMutableDictionary allTopicsForSprint = allTopicsForSprint(pSprint);
		if(allTopicsForSprint != null) {
			returnVal = sortArrayOfStringsAlphabetically(allTopicsForSprint.allKeys());  // These are be sorted
		}
		
		return returnVal;
	}
	
	public NSMutableDictionary allTopicsForSprint(String pSprint) {
		return (NSMutableDictionary)topicsForSprints().objectForKey(pSprint);
	}
	
	public TopicStatus topicForKeyForSprint(String pKey, String pSprint) {
		return (TopicStatus)(allTopicsForSprint(pSprint).objectForKey(pKey));
	}
	
	public NSArray sortArrayOfStringsAlphabetically(NSArray pUnsorted){
            NSMutableArray sorted = new NSMutableArray();

            Enumeration enumer = pUnsorted.objectEnumerator();
            while(enumer.hasMoreElements()) {
                int i = 0;
                String curr = (String)enumer.nextElement();

				int numItems = sorted.count();
				for (i = 0; i < numItems; i++) {
					String aItem = (String)sorted.objectAtIndex(i);
					if(aItem.compareTo(curr) > 0) {
						break;
					}
				}
				sorted.insertObjectAtIndex(curr, i);
            }

        return new NSArray(sorted);
    }

	
	public String toString() {
		String returnVal = "";
		Enumeration enumer = topicsForSprints().keyEnumerator();
		while(enumer.hasMoreElements()) {
			String sprintKey = (String)enumer.nextElement();
			returnVal += "\n" + sprintKey + "\n"; 
			
			NSMutableDictionary sprintTopics = (NSMutableDictionary)topicsForSprints.objectForKey(sprintKey);
			Enumeration enumer2 = sprintTopics.keyEnumerator();
			while(enumer2.hasMoreElements()) {
				TopicStatus ts = (TopicStatus)sprintTopics.objectForKey((String)enumer2.nextElement());
				if(ts != null) {
					returnVal += ts.toString();
				}
			}
		}
		return returnVal;
				
	}

/*
    public NSArray sortedTopics(){
        //System.out.println("EngineeringStatus.sortedTopics()" );

        if(sortedTopics == null) {
            // Sort Keys
            NSMutableArray sortedKeys = new NSMutableArray();
            Enumeration enumer = topics().keyEnumerator();

            while(enumer.hasMoreElements()) {
                int i = 0;
                String currTopic = (String)enumer.nextElement();
                //String currTopic = (String)currItem.valueForKeyPath("assignee.realname");
                //System.out.println("Status.sortedTopics()-2 - " + currTopic);
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
    public NSArray sortedOpenTaskTopics(){
        //System.out.println("EngineeringStatus.sortedOpenTaskTopics()" );

        if(sortedOpenTaskTopics == null) {
            // Sort Keys
            NSMutableArray sortedKeys = new NSMutableArray();
            //System.out.println("Status.sortedOpenTaskTopics()-1 ");
            Enumeration enumer = topics().keyEnumerator();
            //System.out.println("Status.sortedOpenTaskTopics()-2 ");

            while(enumer.hasMoreElements()) {
                int i = 0;
                String currTopic = (String)enumer.nextElement();
                //System.out.println("Status.sortedOpenTaskTopics()-3 - " + currTopic);

                // get the TopicStatus for the current key and see it has any open tasks
                TopicStatus tempTS = (TopicStatus)topics.valueForKey(currTopic);
                if(taskTypes != null) {
                    tempTS.setTaskTypes(taskTypes);
                }

                if(tempTS.taskItem().isOpenItems() == true){
                    int numItems = sortedKeys.count();
                    for (; i < numItems; i++) {
                        String aItem = (String)sortedKeys.objectAtIndex(i);
                        if(aItem.compareTo(currTopic) > 0) {
                            break;
                        }
                    }
                    //System.out.println("Status.sortedOpenTaskTopics()-4 - " + currTopic);
                    sortedKeys.insertObjectAtIndex(currTopic, i);
                }
            }
            sortedOpenTaskTopics = new NSArray(sortedKeys);
            //System.out.println("sortedOpenTaskTopics - " + sortedOpenTaskTopics);

        }

        return sortedOpenTaskTopics;
    }
    public NSArray sortedHasTaskTopics(){
        if(sortedHasTaskTopics == null) {
            // Sort Keys
            NSMutableArray sortedKeys = new NSMutableArray();
            Enumeration enumer = topics().keyEnumerator();

            while(enumer.hasMoreElements()) {
                int i = 0;
                String currTopic = (String)enumer.nextElement();

                // get the TopicStatus for the current key and see it has any open tasks
                TopicStatus tempTS = (TopicStatus)topics.valueForKey(currTopic);
                if(taskTypes != null) {
                    tempTS.setTaskTypes(taskTypes);
                }

                if(tempTS.taskItem().isItems() == true){
                    int numItems = sortedKeys.count();
                    for (; i < numItems; i++) {
                        String aItem = (String)sortedKeys.objectAtIndex(i);
                        if(aItem.compareTo(currTopic) > 0) {
                            break;
                        }
                    }
                    sortedKeys.insertObjectAtIndex(currTopic, i);
                }
            }
            sortedOpenTaskTopics = new NSArray(sortedKeys);

        }

        return sortedOpenTaskTopics;
    }

    public boolean isItemsForDisplay() {
        boolean returnVal = false;
        if((sortedOpenTaskTopics() != null) && (sortedOpenTaskTopics().count() > 0)) {
            returnVal = true;
        }
        return returnVal;
    }
    public EOEnterpriseObject personItem() {
        return personItem;
    }
    public void setPersonItem(EOEnterpriseObject pPersonItem) {
        personItem = pPersonItem;
    }
    public void setTaskTypes(NSArray pValue) {
        if(taskTypes.equals(pValue) == false) {
            sortedOpenTaskTopics = null;
        }
        taskTypes = pValue;
    }
*/

}
