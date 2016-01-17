package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

public class ReleaseNotesReport extends WOComponent {
	private static final long serialVersionUID = 1L;	
    public String selectedRelease;
    public String currentRelease;
    public NSArray<String> releases;
    public NSTimestamp today;
    public Item currentItem;
    protected EOEditingContext editingContext;
    public boolean isEmail;
    public NSArray<Item> releaseNoteBugs;
    public NSArray<String> foundInVersions;
	protected NSMutableDictionary<Object, Object> foundInVersionReleaseNotes;
	protected NSMutableDictionary<Object, Object> assignedToVersionReleaseNotes;
	protected NSMutableDictionary<Object, Object> releaseDatesForReleases;
	public String aVersion;
	//public String product = "Device: Secure Storage";
	//protected NSMutableArray newReleaseNotes;
	//protected NSMutableDictionary projectsForReleases;
	//protected NSMutableDictionary releaseDatesForReleases;

// Release Note Sections	
// - Closed in this release - keyword=releasenote, status=Resolved/Closed, foundIn=any, version=current
// - New in this release - keyword=releasenote, status=any, foundIn=Current, version=future
// - Open in this release - keyword=releasenote, status=any, foundIn=Past, version=current+future

    public ReleaseNotesReport(WOContext aContext) {
		super(aContext);
	//System.out.println("ReleaseNotesReport.ReleaseNotesReport()");

		today = new NSTimestamp();
		foundInVersionReleaseNotes = new NSMutableDictionary<Object, Object>();
		assignedToVersionReleaseNotes = new NSMutableDictionary<Object, Object>();
		releaseDatesForReleases = new NSMutableDictionary<Object, Object>();


		foundInVersions();
		releaseNotesSortedByFoundIn();
		releaseNotesSortedByCurrentVersion();
		//printAll();

    }
	
// - New in this release - keyword=releasenote, status=any, foundIn=Current, version=future
	public NSMutableArray newReleaseNotes() {
	//System.out.println("ReleaseNotesReport.newReleaseNotes()");
		NSMutableArray foundArray;
		NSMutableArray tempArray = new NSMutableArray();
		Enumeration enumer;
		
		// Get only the ReleaseNotes created in this Release
		// filter out any Release notes that are currently assigned to (previous, current)
		foundArray = (NSMutableArray)foundInVersionReleaseNotes.objectForKey(aVersion); 
		if(foundArray != null) {
		
			enumer = foundArray.objectEnumerator();
			while(enumer.hasMoreElements() == true) {
				Item anItem = (Item)enumer.nextElement();
				String itemVersion = (String)anItem.valueForKey("version");
				if(aVersion.compareTo(itemVersion)<0 ) {  // if the itemVersion is greater than aVersion then it is a future version
					tempArray.addObject(anItem);
				}			
			}
		}
		return  tempArray;	// newReleaseNotes
	}


// - Open in this release - keyword=releasenote, status=any, foundIn=Past, version=future
	public NSMutableArray openReleaseNotes() {
	//System.out.println("ReleaseNotesReport.openReleaseNotes()");
	
		NSMutableArray foundArray;
		NSMutableArray tempArray = new NSMutableArray();
		NSMutableArray temp2Array = new NSMutableArray();
		Enumeration enumer;
		
		// Get all bugs filed in previous releases
		// filter only include bugs that are currently assigned to a future release 
		//to get future releases we do a string compare of 'aVersion' to the releases (keys, in foundInVersionReleaseNotes)
		enumer = foundInVersions().objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			String aVersionKey = (String)enumer.nextElement();
			
			if(aVersionKey.compareTo(aVersion)<0 ) {  
				tempArray.addObjectsFromArray((NSMutableArray)foundInVersionReleaseNotes.objectForKey(aVersionKey));
			}			
			else {
				break;
			}
		}

		if(tempArray != null) {
			enumer = tempArray.objectEnumerator();
			while(enumer.hasMoreElements() == true) {
				Item anItem = (Item)enumer.nextElement();
				String itemVersion = (String)anItem.valueForKey("version");
				//System.out.println(aVersion + ".compareTo( " + itemVersion + " ) : " + itemVersion.compareTo(aVersion));
				
				if(aVersion.compareTo(itemVersion)<0 ) {  
					temp2Array.addObject(anItem);
				}			
			}
		}
		
		return  temp2Array;	
	}


	public NSTimestamp releaseDateForRelease() {
		EOEditingContext ec;
        Session s;
        NSMutableArray qual = new NSMutableArray();
        NSArray dates;
        NSTimestamp releaseDate;
        EOQualifier qualifier;
		EOFetchSpecification specification;
		EOEnterpriseObject milestoneDate;
		
		releaseDate = (NSTimestamp)releaseDatesForReleases.valueForKey(aVersion);
		if(releaseDate == null) {

			s = (Session)session();
			ec = s.defaultEditingContext();
			qualifier = EOQualifier.qualifierWithQualifierFormat("milestone='RTP-Device' and release.releaseName='"+ aVersion + "'", null);
						
			specification = new EOFetchSpecification("MilestoneDates", qualifier,null);
			//specification.setIsDeep(false);
			// Perform actual fetch
			dates = (NSArray)ec.objectsWithFetchSpecification(specification);
			milestoneDate = (EOEnterpriseObject)dates.lastObject();
			if(milestoneDate != null) {
				releaseDate = (NSTimestamp)milestoneDate.valueForKey("startDate");
				releaseDatesForReleases.takeValueForKey(releaseDate, aVersion);
			}
		}
        return (NSTimestamp) releaseDate;
    }

	
	public NSMutableArray releaseNotesAssignedToVersion() {
		return (NSMutableArray)assignedToVersionReleaseNotes.objectForKey(aVersion);
	}

	public void releaseNotesSortedByFoundIn() {
	//System.out.println("ReleaseNotesReport.releaseNotesSortedByFoundIn()");
	
		Enumeration enumer;
		NSMutableArray currentSortedArray;
		enumer = releaseNoteBugs().objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			Item releaseNoteBug = (Item)enumer.nextElement();
			String foundIn =  (String)releaseNoteBug.versionFoundIn();
			Integer bugId =  (Integer)releaseNoteBug.bugId();
			currentSortedArray = (NSMutableArray)foundInVersionReleaseNotes.objectForKey(foundIn);
			if(currentSortedArray == null) {
				currentSortedArray =  new NSMutableArray();
				foundInVersionReleaseNotes.setObjectForKey(currentSortedArray,foundIn);
			}
			
			currentSortedArray.addObject(releaseNoteBug);
			//System.out.println("" + (Integer)releaseNoteBug.valueForKey("bugId") + " - Found In: " + (String)releaseNoteBug.versionFoundIn()  + "\n");
		}
		
		// Print all found in
		/*
		System.out.println("FoundIn Release Notes");
		Enumeration enumer2 = foundInVersions().objectEnumerator();
		while(enumer2.hasMoreElements() == true) {
			String aKey = (String)enumer2.nextElement();
			NSMutableArray releaseNotes = (NSMutableArray)foundInVersionReleaseNotes.objectForKey(aKey);
			
			System.out.println("Key: " + aKey + " numItems - " + releaseNotes.count());
		}
		System.out.print("\n");
		*/
		
	}
	public void releaseNotesSortedByCurrentVersion() {
	//System.out.println("ReleaseNotesReport.releaseNotesSortedByCurrentVersion()");
	
		Enumeration enumer;
		NSMutableArray currentSortedArray;
		enumer = releaseNoteBugs().objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			Item releaseNoteBug = (Item)enumer.nextElement();
			String version =  (String)releaseNoteBug.version();
			
			currentSortedArray = (NSMutableArray)assignedToVersionReleaseNotes.objectForKey(version);
			if(currentSortedArray == null) {
				currentSortedArray =  new NSMutableArray();
				assignedToVersionReleaseNotes.setObjectForKey(currentSortedArray,version);
			}
			
			currentSortedArray.addObject(releaseNoteBug);

			//System.out.println("" + (Integer)releaseNoteBug.valueForKey("bugId") + " - Found In: " + (String)releaseNoteBug.versionFoundIn()  + "\n");
		}
		
		// Print all Current Version
		/*
		System.out.println("Current Version Release Notes");
		Enumeration enumer2 = versionsCurrent().objectEnumerator();
		while(enumer2.hasMoreElements() == true) {
			String aKey = (String)enumer2.nextElement();
			NSMutableArray releaseNotes = (NSMutableArray)assignedToVersionReleaseNotes.objectForKey(aKey);
			
			System.out.println("Key: " + aKey + " numItems - " + releaseNotes.count());
		}
		System.out.print("\n");
		*/
	}
    public NSMutableArray foundInVersions() {
		return sortArray(new NSMutableArray(foundInVersionReleaseNotes.allKeys()));

	}
    public NSMutableArray versionsCurrent() {
		return sortArray(new NSMutableArray(assignedToVersionReleaseNotes.allKeys()));

	}
    public NSMutableArray versionsAll() {
	//System.out.println("ReleaseNotesReport.versionsAll()");
		NSMutableArray tempVersions = new NSMutableArray(new Object[] {"Amsterdam (4.10 - Desktop)", "Barcelona (5.0.0 - Desktop+Mobile-MVP)"});

		/*NSMutableArray tempArray = new NSMutableArray(assignedToVersionReleaseNotes.allKeys());
		Enumeration enumer;
		
		enumer = (foundInVersionReleaseNotes.allKeys()).objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			String tempVersion = (String)enumer.nextElement();
	
			if(tempArray.containsObject(tempVersion) == false) {
				tempArray.addObject(tempVersion);
			}
		}
		
		
		return sortArray(tempArray);
		*/
		return tempVersions;
	}
	public NSArray products() {
		NSMutableArray tempTypes = new NSMutableArray(new Object[] {"Client: Trusted Access - Desktop", "Client: Trusted Access - Mobile"});
		//NSMutableArray tempTypes = new NSMutableArray(new Object[] {"Client: Trusted Access - Desktop", "Client: Trusted Access - Mobile", "IronKey Services", "Device: Trusted Access"});
		NSArray products = new NSArray(tempTypes);
        return products;
    }

	
	public NSArray releaseNoteBugs() {
	//System.out.println("ReleaseNotesReport.releaseNoteBugs()");
		EOEditingContext ec;
        Session s;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;
		EOFetchSpecification specification;
		
		if(releaseNoteBugs == null) {

			s = (Session)session();
			ec = s.defaultEditingContext();
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'releasenote'", null));
			//qual.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '" + product + "'", null));
			
						// Product - Limit the returned bugs and stories to be the selected type, plus we are adding service bugs and stories
			Enumeration enumer = products().objectEnumerator();
			while(enumer.hasMoreElements()) {
				qual1.addObject(EOOrQualifier.qualifierWithQualifierFormat("product.productName='" + (String)enumer.nextElement() + "'", null));
			}
			qual.addObject(new EOOrQualifier(qual1));

			
			specification = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
			specification.setRefreshesRefetchedObjects(true);

			releaseNoteBugs = (NSArray)ec.objectsWithFetchSpecification(specification);
		}
        return releaseNoteBugs;
    }
	
	
	public void printAll() {
		Enumeration enumer;
		
		NSMutableArray allkeys = new NSMutableArray(foundInVersionReleaseNotes.allKeys());
		NSMutableArray keys = sortArray(allkeys);
		enumer = keys.objectEnumerator();

		//enumer = foundInVersionReleaseNotes.keyEnumerator();
		while(enumer.hasMoreElements() == true) {
			String aKey = (String)enumer.nextElement();
			NSMutableArray releaseNotes = (NSMutableArray)foundInVersionReleaseNotes.objectForKey(aKey);
			
			System.out.println("Key: " + aKey + " numItems - " + releaseNotes.count());
			Enumeration enumer2 = releaseNotes.objectEnumerator();
			while(enumer2.hasMoreElements() == true) {
				Item abug = (Item)enumer2.nextElement();
				System.out.println("\t Bug:  " + abug.bugId() + " - " + abug.shortDesc());
			}

		}
	}
	
	
    public NSMutableArray sortArray(NSMutableArray pUnsorted) {
        NSMutableArray sortedArray = new NSMutableArray();
        NSMutableArray copyOrig = new NSMutableArray(pUnsorted);
        int numItems;
        String tempLowest = null;
        String tempString;

         // Sort alphabetically
		numItems = pUnsorted.count();
		for(int i = 0; i<numItems; i++) {
			Enumeration enumer2 = copyOrig.objectEnumerator();
			while(enumer2.hasMoreElements()) {
				tempString = (String)enumer2.nextElement();
				if(tempLowest == null) {
					tempLowest = tempString;  // initialize
				}
				else {
					tempLowest = lower(tempString,tempLowest);  // initialize

				}
			}
			copyOrig.removeObject(tempLowest);
			sortedArray.addObject(tempLowest);
			tempLowest = null;
		}
        return sortedArray;
    }

    private String lower(String str1, String str2) {
        return (str1.compareTo(str2) <0 ? str1 : str2);
    }


	
    public NSMutableArray releases() {
		return sortArray(new NSMutableArray(foundInVersionReleaseNotes.allKeys()));

	}
	
    public NSArray releases2() {
        NSMutableArray values;

        if(releases == null) {
            Session s = (Session)session();
            values =  (NSMutableArray)s.rowsForSql("select distinct(version)from bugs where version>='"+ s.supportProject+ "' order by version");
            releases = new NSArray(values);
        }
        return releases;
    }


    public String selectedRelease() {
        return selectedRelease;
    }
	
    public WOComponent emailCurrentPage() {
        Session s = (Session)session();
        String from = "kavoy@marblesecurity.com";

        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");
        ReleaseNotesReport component = (ReleaseNotesReport)pageWithName("ReleaseNotesReport");
		component.setIsEmail(true);

        nextPage.takeValueForKey(component,"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");
        nextPage.takeValueForKey(from,"from");
        nextPage.takeValueForKey("Release Notes", "subject");

        return nextPage;
    }
	public boolean isEmail() {
	   return isEmail;
	}
	public void setIsEmail(boolean pVal) {
	   isEmail = pVal;
	}

    public EOEditingContext editingContext() {
        return editingContext;
    }
    public void setEditingContext(EOEditingContext pValue) {
        editingContext = pValue;
    }
    

    public String aVersion()
    {
        return aVersion;
    }
    public void setAVersion(String newAVersion)
    {
        aVersion = newAVersion;
    }

}

/*

	public NSArray versions2() {
		Session s;
		String sqlString;
		
		if(versions == null) {
			s = (Session)session();
			sqlString = "select distinct(value) from versions where product_id in (2,3) order by value asc";
		    versions =  (NSArray)s.rowsForSql(sqlString);
		}
		Enumeration enumer;
		enumer = versions.objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			String vers = (String)enumer.nextElement();
			
			System.out.println("Version: " + vers);
		}

		return versions;
	}

*/

/*
	public NSTimestamp releaseDateForRelease() {
		EOEditingContext ec;
        Session s;
        NSMutableArray qual = new NSMutableArray();
        NSArray dates;
        NSTimestamp releaseDate;
        EOQualifier qualifier;
		EOFetchSpecification specification;
		EOEnterpriseObject milestoneDate;
		
		releaseDate = (NSTimestamp)releaseDatesForReleases.valueForKey(currentRelease);
		if(releaseDate == null) {

			s = (Session)session();
			ec = s.defaultEditingContext();
			qualifier = EOQualifier.qualifierWithQualifierFormat("milestone='RTP-Device' and release='"+ currentRelease + "'", null);
						
			specification = new EOFetchSpecification("MilestoneDates", qualifier,null);
			//specification.setIsDeep(false);
			// Perform actual fetch
			dates = (NSArray)ec.objectsWithFetchSpecification(specification);
			milestoneDate = (EOEnterpriseObject)dates.lastObject();
			if(milestoneDate != null) {
				releaseDate = (NSTimestamp)milestoneDate.valueForKey("startDate");
				releaseDatesForReleases.takeValueForKey(releaseDate, currentRelease);
			}
		}
        return (NSTimestamp) releaseDate;
    }
*/

