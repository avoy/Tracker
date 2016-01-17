package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

public class MilestoneForProject extends WOComponent {
	private static final long serialVersionUID = 1L;
	public NSArray allMilestoneDatesForProject;
	public NSArray<EOEnterpriseObject> milestoneDatesForProject;
	public NSArray sprintDatesForProject;
	public String selectedProject;
	    /** @TypeInfo MilestoneDates */
	public EOEnterpriseObject aMilestone;

	
    public MilestoneForProject(WOContext context) {
        super(context);
    }
    
    public NSArray<EOEnterpriseObject> allMilestoneDatesForProject() {
        return allMilestoneDatesForProject(selectedProject);
    }

    public NSArray<EOEnterpriseObject> allMilestoneDatesForProject(String pProject) {
        EOFetchSpecification fs;
        NSDictionary<Object,Object> bindings = null;
		if(allMilestoneDatesForProject == null) {
			bindings = new NSDictionary(new Object[] {pProject}, new Object[] { "version"});
			fs = EOFetchSpecification.fetchSpecificationNamed( "milestoneDates", "MilestoneDates").fetchSpecificationWithQualifierBindings( bindings );
			fs.setRefreshesRefetchedObjects(true);
			allMilestoneDatesForProject =  (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
			sortDates(allMilestoneDatesForProject);
		}
		
        return allMilestoneDatesForProject;
    }
    public void sortDates(NSArray pArrayOfDates) {
        NSMutableArray milestones = new NSMutableArray();
        NSMutableArray sprints = new NSMutableArray();
		
		Enumeration<MilestoneDates> enumer = pArrayOfDates.objectEnumerator();
		while(enumer.hasMoreElements()) {
			MilestoneDates aDate = (MilestoneDates)enumer.nextElement();
			String milestoneName = (String)aDate.valueForKey("milestone");
			if(milestoneName.startsWith("S")) {
				sprints.addObject(aDate);
			}
			else {
				milestones.addObject(aDate);
			}
		}
		milestoneDatesForProject = (NSArray)milestones;
		sprintDatesForProject = (NSArray)sprints;
    }
	
	public NSArray milestoneDatesForProject() {
		if(milestoneDatesForProject == null) {
			allMilestoneDatesForProject();
		}
		return milestoneDatesForProject;
	}
	
	public NSArray sprintDatesForProject() {
		if(sprintDatesForProject == null) {
			allMilestoneDatesForProject();
		}
		return sprintDatesForProject;
	}
	public boolean isAny() {
		return (isMilestones() || isSprints()) ? true : false;
	}

	
	public boolean isMilestones() {
		return (milestoneDatesForProject().count() > 0) ? true : false;
	}
	public boolean isSprints() {
		return (sprintDatesForProject().count() > 0) ? true : false;
	}

    // Accessors
    public String selectedProject() { return selectedProject;}
    public void setSelectedProject(String newSelectedProject) { 
		selectedProject = newSelectedProject; }


}
