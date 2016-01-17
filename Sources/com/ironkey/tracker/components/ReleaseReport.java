package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.util.Enumeration;
import java.math.BigDecimal;
//import er.extensions.eof.ERXEditingContextDelegate;
//import er.extensions.ERXEditingContextDelegate;
import java.util.regex.*;


public class ReleaseReport extends WOComponent {
	private static final long serialVersionUID = 1L;
	public EOEnterpriseObject aMilestone;
    public String selectedRelease;
    public String currentRelease;
    public NSArray releases;
    public NSMutableArray fullDateReleases;
    public NSTimestamp today;
    public NSMutableDictionary storiesForReleases;
    public NSMutableDictionary logicalStoriesForReleases;
    public NSMutableDictionary releaseDatesForReleases;
    public NSArray products;
    public String aProduct;    
	protected Pattern p3; 
    /** @TypeInfo Item */
	public EOEnterpriseObject currentItem;
    protected EOEditingContext editingContext;
    public boolean isEmail;
	public boolean currentReleases = true;
	
	
	
	public String header() {
		String returnVal = "";
		if(currentReleases == true) {
			 returnVal = "Current Releases";
		}
		else {
			returnVal = "All Releases";

		}
		return returnVal;
	}
	
	public String linkText() {
		String returnVal = "";
		if(currentReleases == true) {
			 returnVal = "Show All Releases";
		}
		else {
			returnVal = "Show Current Releases";

		}
		return returnVal;
	}
	
	public WOComponent toggleView() {
		if(currentReleases == true) {
			 currentReleases = false;
		}
		else {
			 currentReleases = true;
		}
		return null;
	}

    public ReleaseReport(WOContext aContext) {
		super(aContext);

		today = new NSTimestamp();
		editingContext = new EOEditingContext();
		//editingContext.setDelegate(new ERXEditingContextDelegate());  // set a delegate
		//editingContext = ERXExtensions.newEditingContext();
		fullDateReleases = new NSMutableArray();


		storiesForReleases = new NSMutableDictionary();
		logicalStoriesForReleases = new NSMutableDictionary();
		releaseDatesForReleases = new NSMutableDictionary();
		p3 =  Pattern.compile(".*\\((.*)\\)"); // only the text between the (), e.g. 'Lexus (TA 3.1)'

    }

	public NSArray products() {
        if(products == null) {
            Session s = (Session)session();
            //NSMutableArray tempTypes = new NSMutableArray(new Object[] {"Client: Trusted Access - Desktop", "Client: Trusted Access - Mobile", "IronKey Services", "Device: Trusted Access"});
            NSMutableArray tempTypes = new NSMutableArray(new Object[] {"Media Platform"});
			products = new NSArray(tempTypes);
        }
        return products;
    }

	public String filteredRelease() {
		String returnVal;
		
		String actualRelease =currentRelease;
		if((currentRelease.equals("_Backlog")) || (actualRelease.equals("__Graveyard"))) {
			returnVal = "Uncommitted";
		}
		else if(actualRelease.equals("Proposed for the next release")) {
			returnVal = "Proposed";
		}
		else {
				Matcher m = p3.matcher(actualRelease);
				if(m.find() == true) {
				//System.out.println("pattern1 - " + m.group(1));
					returnVal = m.group(1);
				}
				else {
					returnVal = actualRelease;
				}
		}
		
		return returnVal;
	}

	public NSArray storiesForSelectedRelease() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
		NSArray storiesForSelectedRelease = null;
		
		storiesForSelectedRelease = (NSArray)storiesForReleases.valueForKey(currentRelease);
		if(storiesForSelectedRelease == null) {
			//System.out.println("\n\n============ ReleasePlan.storiesForSelectedRelease()");

			Session s = (Session)session();
			NSMutableArray qual = new NSMutableArray();
			NSMutableArray qual1 = new NSMutableArray();
			EOQualifier qualifier;

			//Stories
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("type ='Story'", null));
			
			// Release
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ currentRelease + "' ", null));

			Object orderings[]={
					EOSortOrdering.sortOrderingWithKey("targetMilestone", EOSortOrdering.CompareCaseInsensitiveAscending),
					//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),					
					EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
					EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending),
			};

			fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);
			storiesForSelectedRelease= (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
			storiesForReleases.takeValueForKey(storiesForSelectedRelease, currentRelease);

		}
		return storiesForSelectedRelease;
    }
	
	// only get top most stories (epics or stories with no parents)
	public NSArray logicalStoriesForRelease() {
		NSArray logicalStoriesForSelectedRelease = null;
		
		logicalStoriesForSelectedRelease = (NSArray)logicalStoriesForReleases.valueForKey(currentRelease);
		if(logicalStoriesForSelectedRelease == null) {
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
				}
			}
			Object orderings[]={
				//EOSortOrdering.sortOrderingWithKey("rank", EOSortOrdering.CompareCaseInsensitiveAscending),		
				EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
				EOSortOrdering.sortOrderingWithKey("bugSeverity", EOSortOrdering.CompareCaseInsensitiveAscending)
			};
			logicalStoriesForSelectedRelease = EOSortOrdering.sortedArrayUsingKeyOrderArray(tempArray, new NSArray(orderings));
			logicalStoriesForReleases.takeValueForKey(logicalStoriesForSelectedRelease, currentRelease);

		}
		return logicalStoriesForSelectedRelease;
	}

	public NSArray productStoriesForRelease() {
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer = logicalStoriesForRelease().objectEnumerator();
		NSMutableArray qual = new NSMutableArray();
			
		qual.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + aProduct + "'", null));
		
		return (NSArray)EOQualifier.filteredArrayWithQualifier(logicalStoriesForRelease(), new EOAndQualifier(qual)) ;
		
	}

	public int countItemsForCurrentRelease() {
        Session s = (Session)session();

        NSArray values =  s.rowsForSql("select count(*) from bugs where version='" + currentRelease + "' and (cf_type='Bug' or cf_type='Enhancement' or cf_type='Task') ");
		
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
	public int countClosedItemsForCurrentRelease() {
        Session s = (Session)session();

        NSArray values =  s.rowsForSql("select count(*) from bugs where version='" + currentRelease + "' and (cf_type='Bug' or cf_type='Enhancement' or cf_type='Task') and (bug_status='CLOSED' or bug_status='VERIFIED')");
		
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
	public String percentCompleteCountChildrenSameVersion() {
		int fixed = countClosedItemsForCurrentRelease();
		int total = countItemsForCurrentRelease();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
	}
	
	public String percentCompleteCountChildren() {  // Note: this is identical to percentCompleteCountChildrenSameVersion() above
		int fixed = countClosedItemsForCurrentRelease();
		int total = countItemsForCurrentRelease();
		
        return percentCompleteStringFromDouble(((double)fixed/(double)total) * 100);
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
                System.err.println("ReleaseReport.percentCompleteStringFromDouble() - " + e);
            }
        }
        return returnVal;
    }
	public NSTimestamp releaseDateForRelease() {
		NSTimestamp returnVal = null;
		
		EOEnterpriseObject milestoneDate = milestoneDateForRelease();
		if(milestoneDate != null) {
			NSTimestamp releaseDate = (NSTimestamp)milestoneDate.valueForKey("actualDate");
			if(releaseDate != null) {
				returnVal = releaseDate;
			}
			else {
				returnVal = (NSTimestamp)milestoneDate.valueForKey("forecastDate");
			}
		}
		return returnVal;
	}

	public EOEnterpriseObject milestoneDateForRelease() {
		EOEditingContext ec;
        Session s;
        NSMutableArray qual = new NSMutableArray();
        NSArray dates;
        NSTimestamp releaseDate;
        EOQualifier qualifier;
		EOFetchSpecification specification;
		EOEnterpriseObject milestoneDate;
		
		milestoneDate = (EOEnterpriseObject)releaseDatesForReleases.valueForKey(currentRelease);
		if(milestoneDate == null) {

			s = (Session)session();
			ec = s.defaultEditingContext();
			qualifier = EOQualifier.qualifierWithQualifierFormat("milestone='RTP' and releaseName='"+ currentRelease + "'", null);
						
			specification = new EOFetchSpecification("MilestoneDates", qualifier,null);
			//specification.setIsDeep(false);
			// Perform actual fetch
			dates = (NSArray)ec.objectsWithFetchSpecification(specification);
			milestoneDate = (EOEnterpriseObject)dates.lastObject();
			if(milestoneDate != null) {
				//releaseDate = (NSTimestamp)milestoneDate.valueForKey("startDate");
				releaseDatesForReleases.takeValueForKey(milestoneDate, currentRelease);
			}
		}
        return (EOEnterpriseObject) milestoneDate;
    }

	
    public NSArray releases() {
        NSMutableArray values;

        if(releases == null) {
            Session s = (Session)session();
            //values =  (NSMutableArray)s.rowsForSql("select distinct(version)from bugs where version>='"+ s.supportProject+ "' order by version");
            //values =  (NSMutableArray)s.rowsForSql("select distinct(value)from versions where product_id in  (17,23,24,14) order by value");
            values =  (NSMutableArray)s.rowsForSql("select distinct(value)from versions order by value");
            values.removeObject("__Graveyard");

            releases = new NSArray(values);
        }
        return releases;
    }
	
	
	
	public boolean isReleased() {
		boolean returnVal = false;
		EOEnterpriseObject milestoneDate = milestoneDateForRelease();
		if(milestoneDate != null) {
			NSTimestamp releaseDate = (NSTimestamp)milestoneDate.valueForKey("actualDate");
			if(releaseDate != null) {
				returnVal = true;
			}
		}

		/*
		NSTimestamp rtp = dateForMilestone(currentRelease, "RTP");
		if(rtp != null) {
			DateUtil dateUtil = new DateUtil(today, rtp );
			int numdays = (int)dateUtil.days;
			if (numdays < 0) {
				returnVal = true;
			}
		}
		
		*/
		return returnVal;
	}
	
	
	public NSArray currentReleases() {
		NSArray returnVal;
		if (currentReleases == true) {
			NSMutableArray currentReleases = new NSMutableArray();
			// for each release
			// get the 'RTP' date
			// Compare to today
			// if more than 30 days - drop it.
			Enumeration enumer = releases().objectEnumerator();
			while (enumer.hasMoreElements()) {
				String aRelease = (String)enumer.nextElement();

					NSTimestamp rtp = dateForMilestone(aRelease, "RTP");
					if(rtp != null) {
						DateUtil dateUtil = new DateUtil(today, rtp );
						int numdays = (int)dateUtil.days;
						//System.out.println("Release: " + aRelease + " - days from today - " + numdays);
						if (numdays > -30) {
							currentReleases.addObject(aRelease);
							if(numdays < 30) {
								if(!fullDateReleases.containsObject(aRelease)) {
									fullDateReleases.addObject(aRelease);
								}
							}
						}
						else {
							if(!fullDateReleases.containsObject(aRelease)) {
								fullDateReleases.addObject(aRelease);
							}
						}
					}
					else {;} // don't add null 'RTP' releases

			}
			returnVal =  (NSArray)currentReleases;
		}
		else {
			returnVal = releases();
		}
		return returnVal;
	}
	
	public boolean isFullDate() {
		return (fullDateReleases.containsObject(currentRelease))?true:false;
	}
	
    public NSArray milestoneDatesForProject(String pProject) {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        NSArray milestoneDatesForProject;

        bindings = new NSDictionary(new Object[] {pProject}, new Object[] { "version"});
        fs = EOFetchSpecification.fetchSpecificationNamed( "milestoneDates", "MilestoneDates").fetchSpecificationWithQualifierBindings( bindings );
        fs.setRefreshesRefetchedObjects(true);

        milestoneDatesForProject =  (NSArray)editingContext().objectsWithFetchSpecification(fs);

        return milestoneDatesForProject;
    }

    public NSTimestamp dateForMilestone(String pRelease, String pMilestone) {
        ////System.out.println("pMilestone - " + pMilestone);
        NSTimestamp returnVal = null;
        Enumeration enumer = milestoneDatesForProject(pRelease).objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject eo = (EOEnterpriseObject)enumer.nextElement();
            if(pMilestone.equals((String)eo.valueForKey("milestone"))) {
				if((pMilestone.equals("RTP")) || (pMilestone.equals("RTM")) || (pMilestone.equals("CC")) || (pMilestone.equals("RC1"))) {
					returnVal = (NSTimestamp)eo.valueForKey("actualDate");
					if(returnVal == null) {
						returnVal = (NSTimestamp)eo.valueForKey("forecastDate");
					}
				}
				else {
					returnVal = (NSTimestamp)eo.valueForKey("startDate");
				}
                break;
            }
        }
        return returnVal;
    }


    public String selectedRelease() {
        return selectedRelease;
    }
	
    public WOComponent emailCurrentPage() {
        Session s = (Session)session();
        //EOEnterpriseObject u = (EOEnterpriseObject)s.getUser();
        //String email = (String)u.valueForKey("loginName");
        //String realName = (String)u.valueForKey("realname");
        //String from = realName + " <"+email+">";
        String from = "kavoy@marblesecurity.com";

        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");
        ReleaseReport component = (ReleaseReport)pageWithName("ReleaseReport");
		component.setIsEmail(true);

        nextPage.takeValueForKey(component,"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");
        nextPage.takeValueForKey(from,"from");
        nextPage.takeValueForKey("Project Report", "subject");

        return nextPage;
    }
	public boolean isEmail() {
	   return isEmail;
	}
	public void setIsEmail(boolean pVal) {
	   isEmail = pVal;
	}

/*
    public WOComponent refresh() {
        editingContext().refaultObjects(); 
        supportTickets = null;
        incidents = null;
        return null;
    }
	
	*/
    public EOEditingContext editingContext() {
        return editingContext;
    }
    public void setEditingContext(EOEditingContext pValue) {
        editingContext = pValue;
    }
    

    public ReleasePlan goReleasePlan()
    {
		session().takeValueForKey(currentRelease, "selectedProject");

        ReleasePlan nextPage = (ReleasePlan)pageWithName("ReleasePlan");
		nextPage.takeValueForKey(currentRelease, "selectedProject");
        // Initialize your component here

        return nextPage;
    }

    public WOComponent goUpdate()
    {
		fullDateReleases = new NSMutableArray();
		storiesForReleases = new NSMutableDictionary();
		logicalStoriesForReleases = new NSMutableDictionary();
		releaseDatesForReleases = new NSMutableDictionary();

        return null;
    }

}
