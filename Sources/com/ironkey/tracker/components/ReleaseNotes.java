package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;
import java.math.BigDecimal;
//import er.extensions.ERXEditingContextDelegate;

public class ReleaseNotes extends WOComponent {
	private static final long serialVersionUID = 1L;
	public String selectedProject;	
	public NSTimestamp today;
	public Item currentItem;
	public EOEditingContext editingContext;
    public boolean isEmail;
    public NSArray releaseNoteBugs;
	protected NSMutableDictionary foundInAllVersions;
	protected NSMutableDictionary fixedInAllVersions;

// Release Note Sections	
// - Fixed in this release - keyword=releasenote, status=Resolved/Closed, foundIn=any, version=current
// - Found in this release - keyword=releasenote, status=any, foundIn=Current, version=future

    public ReleaseNotes(WOContext aContext) {
		super(aContext);
		today = new NSTimestamp();
    }
	
	public NSArray releaseNoteBugs() {
	//System.out.println("ReleaseNotesReport.releaseNoteBugs()");
        Session s;
        NSMutableArray qual = new NSMutableArray();
        NSMutableArray qual1 = new NSMutableArray();
        EOQualifier qualifier;
		EOFetchSpecification specification;
		
		if(releaseNoteBugs == null) {
			qual.addObject(EOAndQualifier.qualifierWithQualifierFormat("keywords.keywordName = 'releasenote'", null));
			
			// The release may cross several products.  If the version is the same, it is considered the same release
			Enumeration enumer = productsForVersion(selectedProject()).objectEnumerator();
            while(enumer.hasMoreElements()) {
                EOEnterpriseObject prod = (EOEnterpriseObject)enumer.nextElement();
				qual1.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName = '" + prod.valueForKey("productName") + "'", null));
            }
			qual.addObject(new EOOrQualifier(qual1));
			
			specification = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
			specification.setRefreshesRefetchedObjects(true);

			Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("version", EOSortOrdering.CompareCaseInsensitiveAscending)
			};
 
			// This returns a new array in sorted order. 
			releaseNoteBugs=EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)session().defaultEditingContext().objectsWithFetchSpecification(specification), new NSArray(orderings)); 
		}
        return releaseNoteBugs;
    }
	
	
	public NSArray productsForVersion(String pVersion) {
		NSArray products = null;
		EOFetchSpecification fs;
		NSDictionary bindings = null;
		
		//System.out.println("pVersion - " + pVersion);
		if(pVersion != null) {
			bindings = new NSDictionary(new Object[] { pVersion}, new Object[] { "version"});
			fs=EOFetchSpecification.fetchSpecificationNamed("productsForVersion", "Products").fetchSpecificationWithQualifierBindings(bindings);
			fs.setRefreshesRefetchedObjects(true);

			products = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return products;
	}
	
	public NSMutableDictionary foundInAllVersions() {
		Enumeration enumer;
		NSMutableArray currentSortedArray;
	
		if(foundInAllVersions == null) {
			foundInAllVersions = new NSMutableDictionary();
			enumer = releaseNoteBugs().objectEnumerator();
			while(enumer.hasMoreElements() == true) {
				Item releaseNoteBug = (Item)enumer.nextElement();
				String foundIn =  (String)releaseNoteBug.versionFoundIn();
				Integer bugId =  (Integer)releaseNoteBug.bugId();
				currentSortedArray = (NSMutableArray)foundInAllVersions.objectForKey(foundIn);
				if(currentSortedArray == null) {
					currentSortedArray =  new NSMutableArray();
					foundInAllVersions.setObjectForKey(currentSortedArray,foundIn);
				}
				
				currentSortedArray.addObject(releaseNoteBug);
			}
		}
		
		return foundInAllVersions;
		
	}
	
	public NSArray foundInVersion() {
		return (NSArray)foundInAllVersions().objectForKey(selectedProject); 
	}
	public NSArray deferredFromVersion() {
		Enumeration enumer;
		NSMutableArray deferred = new NSMutableArray();
		
		enumer = foundInVersion().objectEnumerator();
		while(enumer.hasMoreElements() == true) {
			Item releaseNoteBug = (Item)enumer.nextElement();
			String version =  (String)releaseNoteBug.version();
			if(!version.equals(selectedProject)) {
				deferred.addObject(releaseNoteBug);
			}

		}

		return (NSArray)deferred; 
	}
		
	public NSArray fixedInVersion() {
		return (NSArray)fixedInAllVersions().objectForKey(selectedProject);
	}

	public NSMutableDictionary fixedInAllVersions() {	
		Enumeration enumer;
		NSMutableArray currentSortedArray;
		
		if(fixedInAllVersions == null) {
			fixedInAllVersions = new NSMutableDictionary();

			enumer = releaseNoteBugs().objectEnumerator();
			while(enumer.hasMoreElements() == true) {
				Item releaseNoteBug = (Item)enumer.nextElement();
				String version =  (String)releaseNoteBug.version();
				
				currentSortedArray = (NSMutableArray)fixedInAllVersions.objectForKey(version);
				if(currentSortedArray == null) {
					currentSortedArray =  new NSMutableArray();
					fixedInAllVersions.setObjectForKey(currentSortedArray,version);
				}
				
				currentSortedArray.addObject(releaseNoteBug);

			}
		}
		return fixedInAllVersions;

	}

	public void printAll() {
		Enumeration enumer;
		
		NSMutableArray allkeys = new NSMutableArray(foundInAllVersions.allKeys());
		NSMutableArray keys = sortArray(allkeys);
		enumer = keys.objectEnumerator();

		//enumer = foundInAllVersions.keyEnumerator();
		while(enumer.hasMoreElements() == true) {
			String aKey = (String)enumer.nextElement();
			NSMutableArray releaseNotes = (NSMutableArray)foundInAllVersions.objectForKey(aKey);
			
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
	
	public String selectedProject() {return selectedProject;
    }
	
	public void setSelectedProject(String aVal) { selectedProject = aVal;
    }

}

