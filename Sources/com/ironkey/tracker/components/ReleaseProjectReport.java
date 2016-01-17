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

public class ReleaseProjectReport extends WOComponent {
	private static final long serialVersionUID = 1L;
    public EOEnterpriseObject aMilestone;
    public String selectedRelease;
    public String currentRelease;
    public NSArray releases;
    public NSTimestamp today;
    public NSMutableDictionary projectsForReleases;
    public NSMutableDictionary releaseDatesForReleases;
	
    /** @TypeInfo Item */
    public EOEnterpriseObject currentItem;
    protected EOEditingContext editingContext;
    public boolean isEmail;

    public ReleaseProjectReport(WOContext aContext) {
		super(aContext);

		today = new NSTimestamp();
		editingContext = new EOEditingContext();
		//editingContext.setDelegate(new ERXEditingContextDelegate());  // set a delegate
		//editingContext = ERXExtensions.newEditingContext();

		projectsForReleases = new NSMutableDictionary();
		releaseDatesForReleases = new NSMutableDictionary();

    }
	
	
	public NSArray projectsForRelease() {
		EOEditingContext ec;
        Session s;
        NSMutableArray qual = new NSMutableArray();
        NSArray projects;
        EOQualifier qualifier;
		EOFetchSpecification specification;
		
		projects = (NSArray)projectsForReleases.valueForKey(currentRelease);
		if(projects == null) {

			s = (Session)session();
			ec = s.defaultEditingContext();
			qualifier = EOQualifier.qualifierWithQualifierFormat("type='Project' and version='"+ currentRelease + "'", null);
						
			specification = new EOFetchSpecification("Item", qualifier,null);
			//specification.setIsDeep(false);
			// Perform actual fetch
			projects = (NSArray)ec.objectsWithFetchSpecification(specification);
			projectsForReleases.takeValueForKey(projects, currentRelease);
		}
        return projects;
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
		
		releaseDate = (NSTimestamp)releaseDatesForReleases.valueForKey(currentRelease);
		if(releaseDate == null) {

			s = (Session)session();
			ec = s.defaultEditingContext();
			qualifier = EOQualifier.qualifierWithQualifierFormat("milestone='RTP' and releaseName='"+ currentRelease + "'", null);
						
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

	
    public NSArray releases() {
        NSMutableArray values;

        if(releases == null) {
            Session s = (Session)session();
            //values =  (NSMutableArray)s.rowsForSql("select distinct(version)from bugs where version>='"+ s.supportProject+ "' order by version");
            values =  (NSMutableArray)s.rowsForSql("select distinct(value)from versions where product_id in (2,3) order by value");
            values.removeObject("_Future");
            values.removeObject("OTP-Demo");
			values.addObject("_Future");  // want _Future to be the last item

            releases = new NSArray(values);
        }
        return releases;
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
        ReleaseProjectReport component = (ReleaseProjectReport)pageWithName("ReleaseProjectReport");
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
    

}
