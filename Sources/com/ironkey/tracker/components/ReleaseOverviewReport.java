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

public class ReleaseOverviewReport extends WOComponent {    
	private static final long serialVersionUID = 1L;
    public EOEnterpriseObject aMilestone;
    public String selectedProject;
    public NSArray projects;
    public NSTimestamp today;
    public NSArray supportTickets;
    /** @TypeInfo Ticket */
    public EOEnterpriseObject ticketObject;
    /** @TypeInfo DataHyperlinks */
    public EOEnterpriseObject linkObject;
    public String aPatch;
    public NSArray incidents;
    /** @TypeInfo Item */
    public EOEnterpriseObject currentItem;
    public EOEditingContext editingContext;


    public ReleaseOverviewReport(WOContext aContext) {
        super(aContext);

        today = new NSTimestamp();
       editingContext = new EOEditingContext();
	   //editingContext.setDelegate(new ERXEditingContextDelegate());  // set a delegate
	   //editingContext = ERXExtensions.newEditingContext();
    }
    public NSArray projects() {
        NSMutableArray values;

        if(projects == null) {
            Session s = (Session)session();
            //values =  (NSMutableArray)s.rowsForSql("select distinct(version)from bugs where version>='"+ s.supportProject+ "'");
            values =  (NSMutableArray)s.rowsForSql("select distinct(value)from versions where product_id in (2,3,14,17,18) and value>='"+ s.supportProject+ "' order by value");
            //values.removeObject("_Future");
            values.removeObject("OTP-Demo");
            projects = new NSArray(values);
        }
        return projects;
    }
    public String ticketURL() {
      return "http://smcorpload10:8080/TicketTracker/WebObjects/TicketTracker.woa/wa/inspect?ticket=" + (Number)ticketObject.valueForKey("ticketNumber");

    }
    public String incidentURL() {
        String returnVal = null;
        if(currentItem != null) {
            returnVal = ((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id=" + (Number)currentItem.valueForKey("bugId");
        }

        return returnVal;
    }

    public String triageURL() {
        String bugNumbers = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=";
        //NSDictionary bindings = new NSDictionary(new Object[] {selectedProject, "*"}, new Object[] { "version", "type"});
        NSDictionary bindings = new NSDictionary(new Object[] {selectedProject}, new Object[] { "version"});
        EOFetchSpecification fs=EOFetchSpecification.fetchSpecificationNamed("triageBugs","Item").fetchSpecificationWithQualifierBindings(bindings);
        NSArray triageBugs = (NSMutableArray)editingContext().objectsWithFetchSpecification(fs);
        Enumeration enumer = triageBugs.objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject item = (EOEnterpriseObject)enumer.nextElement();
            bugNumbers += (Number)item.valueForKey("bugId") + ",";
        }
        return bugNumbers;
    }


    public NSArray supportTickets() {
        EOQualifier qualifier;
        NSMutableArray andQual = new NSMutableArray();
        NSMutableArray orQual = new NSMutableArray();

        if(supportTickets == null) {


            //qualifier = EOQualifier.qualifierWithQualifierFormat("priority.priorityName = %@ OR priority.priorityName = %@" , new NSArray(new Object[] {"Urgent", "High"}));
            qualifier = EOQualifier.qualifierWithQualifierFormat("priority.priorityId <= 2" , null);
            andQual.addObject(qualifier);
            

           Object itemsArray[] = {"Work-in-Progress", "Outreach-in-Progress", "New Ticket","Escalated as RFE", "Escalated as Research", "Escalated as Ops Task", "Escalated as Defect"};  // Open and Pending
            //Object itemsArray[] = {"Work-in-Progress", "Outreach-in-Progress", "New Ticket"};  // Pending
           Object orderings[]={ EOSortOrdering.sortOrderingWithKey("priority.priorityId", EOSortOrdering.CompareCaseInsensitiveAscending ), EOSortOrdering.sortOrderingWithKey( "weight",             EOSortOrdering.CompareDescending), EOSortOrdering.sortOrderingWithKey( "domain.name",             EOSortOrdering.CompareCaseInsensitiveAscending) };
           
            NSArray items = new NSArray(itemsArray);
            Enumeration itemsEnum = items.objectEnumerator();
            while(itemsEnum.hasMoreElements()) {
                String val = (String)itemsEnum.nextElement();
                orQual.addObject(EOQualifier.qualifierWithQualifierFormat("resolution.resolutionType='" + val + "'", null));
            }

            andQual.addObject(new EOOrQualifier(orQual));
             
            EOFetchSpecification specification = new EOFetchSpecification("Ticket", new EOAndQualifier(andQual),new NSArray(orderings));
            //EOFetchSpecification specification = new EOFetchSpecification("Ticket", new EOAndQualifier(andQual), null);
           // EOFetchSpecification specification = new EOFetchSpecification("Ticket", qualifier, null);
            specification.setPrefetchingRelationshipKeyPaths(new NSArray(new Object[] {"links"}));
            specification.setRefreshesRefetchedObjects(true);
            supportTickets = (NSMutableArray)editingContext().objectsWithFetchSpecification(specification);
            //System.out.println("supportTickets.count() - " + supportTickets.count());

        }

        return supportTickets;
    }


    public NSArray incidents() {
        EOQualifier qualifier;
        //NSMutableArray andQual = new NSMutableArray();
        //NSMutableArray orQual = new NSMutableArray();
        //NSMutableArray qual = new NSMutableArray();

        if(incidents == null) {

            NSTimestamp currTime = new NSTimestamp();
            NSTimestamp weekAgo =  currTime.timestampByAddingGregorianUnits(0, 0, -7, 0, 0, 0);  // same time 7 days ago
            NSArray args = new NSArray(weekAgo);
            //Zilla upgrade

            qualifier = EOQualifier.qualifierWithQualifierFormat("product.productName='Incident' and (priority='1 - Urgent' or priority='2 - High') and ((bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED') or (creationTs > %@))", args);

            //NSDictionary bindings = new NSDictionary(new Object[] {weekAgo}, new Object[] { "weekAgo"});
            //EOFetchSpecification specification=EOFetchSpecification.fetchSpecificationNamed("topIncidentsForWeek","Item").fetchSpecificationWithQualifierBindings(bindings);
            EOFetchSpecification specification = new EOFetchSpecification("Item", qualifier,null);
            specification.setRefreshesRefetchedObjects(true);
            incidents = (NSMutableArray)editingContext().objectsWithFetchSpecification(specification);
        }

        return incidents;

    }

    public boolean isIncidents() {
        return (incidents().count()>0)?true:false;
    }

    public boolean isLinks() {
        boolean returnVal = false;
        if(ticketObject !=null) {
            NSArray links = (NSArray)ticketObject.valueForKey("links");
            if((links != null) && (links.count()>0)){
                returnVal = true;
            }
        }
        return returnVal;
    }
    public boolean isMultipleLinks() {
        return supportTickets().count()>0?true:false;
    }
    public boolean isBugzillaItem() {
        boolean returnVal = false;
        if(linkObject != null) {
            Integer theType = (Integer)linkObject.valueForKey("hyperlinkType");

            if(theType.intValue() == 0) {  // bugzilla
                returnVal = true;
            }
        }
        return returnVal;
    }
    public boolean isBugzillaResolved() {
        boolean returnVal = false;
        EOQualifier qualifier;
        NSArray itemArray;
        EOEnterpriseObject item;

        if(linkObject != null) {
            Integer theType = (Integer)linkObject.valueForKey("hyperlinkType");
            if(theType.intValue() == 0) {  // bugzilla

                if(linkObject.valueForKey("hyperlinkRef") != null) {
                    qualifier = EOQualifier.qualifierWithQualifierFormat("bugId="+(String)linkObject.valueForKey("hyperlinkRef")  , null);
                    EOFetchSpecification specification = new EOFetchSpecification("Item", qualifier, null);
                    specification.setRefreshesRefetchedObjects(true);
                    itemArray = (NSMutableArray)editingContext().objectsWithFetchSpecification(specification);
                    if(itemArray.count() >0) {
                        item = (EOEnterpriseObject)itemArray.objectAtIndex(0);
                        if(item != null) {
                            String status = (String)item.valueForKey("bugStatus");
                            if((status.equals("RESOLVED")) || ((status.equals("CLOSED")))) {
                                returnVal = true;
                            }
                        }

                    }
                }

            }
        }

        return returnVal;
    }

    public String bugURL() {
        String returnVal = null;
        if(linkObject != null) {
            Integer theType = (Integer)linkObject.valueForKey("hyperlinkType");
            if(theType.intValue() == 0) {  // bugzilla
                returnVal = ((Application)Application.application()).bugzillaHostUrl() + "/show_bug.cgi?id=" + (String)linkObject.valueForKey("hyperlinkRef");
            }
        }
        return returnVal;
    }
    public String releaseVersionForEscalations() {
        String returnVal = null;
        EOQualifier qualifier;
        NSArray itemArray;
        EOEnterpriseObject item;

        if(linkObject != null) {
            Integer theType = (Integer)linkObject.valueForKey("hyperlinkType");
            if((theType.intValue() == 0)&& (linkObject.valueForKey("hyperlinkRef") != null)) {  // bugzilla
                qualifier = EOQualifier.qualifierWithQualifierFormat("bugId="+(String)linkObject.valueForKey("hyperlinkRef")  , null);
                EOFetchSpecification specification = new EOFetchSpecification("Item", qualifier, null);
                specification.setRefreshesRefetchedObjects(true);

                itemArray = (NSMutableArray)editingContext().objectsWithFetchSpecification(specification);
                if(itemArray.count() > 0) {
                    
                    item = (EOEnterpriseObject)itemArray.objectAtIndex(0);
                    if(item != null) {

                        String priority = (String)item.valueForKey("priority");
                        if(priority.equals("5 - Undefined")) {
                            returnVal = "Not Scheduled";
                        }
                        else {
                            returnVal = (String)item.valueForKeyPath("product.productName");
                            if(!returnVal.equals("Incident")) {
                                returnVal = (String)item.valueForKey("version");

                                String milestone = (String)item.valueForKey("targetMilestone");
                                if((milestone != null) && ((milestone.startsWith("patch") || (milestone.equals("hotfix") ) || (milestone.equals("datafix"))))) {
                                    returnVal += "-" + milestone;
                                }
                            }
                        }
                    }
                }
            }
        }
        return returnVal;

    }

    public int countForSQL(String pSql) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql(pSql);
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }

    public int countForStatusForMilestone(String pStatusSQL, String pMilestone) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='" + pStatusSQL+ "') and target_milestone='" + pMilestone +"' and version='"+ selectedProject()+ "' and (cf_type !='Customer' or cf_type != 'Prospect')");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }

    public int countForOpenForCurrentPatch() {
        return countForStatusForMilestone("NEW' || bug_status='ASSIGNED'  || bug_status='REOPENED", aPatch) ;
    }
    public int countForResolvedForCurrentPatch() {
        return countForStatusForMilestone("RESOLVED", aPatch) ;
    }
    public int countForClosedForCurrentPatch() {
        return countForStatusForMilestone("CLOSED", aPatch) ;
    }
    public int countForTotalForCurrentPatch() {
        return countForStatusForMilestone("NEW' || bug_status='ASSIGNED'  || bug_status='REOPENED' || bug_status='RESOLVED' || bug_status='CLOSED", aPatch) ;
    }
/*
    public WOComponent pageForOpenForCurrentPatch() {
        return listPageForSQL("(bugStatus='NEW' or bugStatus='ASSIGNED') and targetMilestone='" + aPatch +"' and version='"+ selectedProject()+ "'");
    }
    public WOComponent pageForResolvedForCurrentPatch() {
        return listPageForSQL("(bugStatus='RESOLVED') and targetMilestone='" + aPatch +"' and version='"+ selectedProject()+ "'");
    }
    public WOComponent pageForClosedForCurrentPatch() {
        return listPageForSQL("(bugStatus='CLOSED') and targetMilestone='" + aPatch +"' and version='"+ selectedProject()+ "'");
    }
    public WOComponent pageForTotalForCurrentPatch() {
        return listPageForSQL("targetMilestone='" + aPatch +"' and version='"+ selectedProject()+ "'");
    }

*/

    public NSArray patchReleasesForSelectedProject() {
        Session s = (Session)session();
		NSArray values =  s.rowsForSql("select distinct(target_milestone) from bugs where (target_milestone like 'patch%' || target_milestone like '%fix')  and version='"+ selectedProject()+ "' order by target_milestone asc");
        return values;
    }
    public NSArray milestoneCountsForSelectedProject() {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select distinct(target_milestone) from bugs where version='"+ selectedProject()+ "' order by target_milestone asc");
        return values;
    }

    public NSTimestamp dateForCurrentPatch() {
        return dateForMilestone(aPatch, selectedProject());
    }
    public String selectedProject() {
        return selectedProject;
    }
    public int resolvedBugsForSelectedProject() {
        return resolvedBugsForProject(selectedProject());
    }

    public int resolvedBugsForProject(String pProject) {
        Session s = (Session)session();
        //Zilla upgrade
        //NSArray values =  s.rowsForSql("select count(*) count from bugs where bug_status='RESOLVED' and product_id=3 and version='"+ pProject+ "'");
        //NSArray values =  s.rowsForSql("select count(*) count from bugs where bug_status='RESOLVED' and bug_severity !='enhancement'  and version='"+ pProject+ "'");
        NSArray values =  s.rowsForSql("select count(*) count from bugs where bug_status='RESOLVED'  and version='"+ pProject+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int bugsOpenForSelectedProject() {
        return bugsOpenforProject(selectedProject());
    }

    public int bugsOpenforProject(String pProject) {
        //Zilla upgrade
        Session s = (Session)session();
        //NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED') and product_id=3 and version='"+ pProject+ "'");
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and bug_severity !='enhancement' and version='"+ pProject+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
   

    public int enhancementsOpenForSelectedProject() {
        return enhancementsOpenInForProject(selectedProject());
    }

    public int enhancementsOpenInForProject(String pProject) {
        Session s = (Session)session();
        //Zilla upgrade

        //NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED') and product_id=5 and version='"+ pProject+ "'");
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and bug_severity ='enhancement' and version='"+ pProject+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int workItemsOpenForSelectedProject() {
        return workItemsOpenInForProject(selectedProject());
    }

    public int workItemsOpenInForProject(String pProject) {
        Session s = (Session)session();
        //Zilla upgrade

        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and product_id=8 and version='"+ pProject+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int supportEnhOpenForSelectedProject() {
        return supportEnhOpenForProject(selectedProject());
    }

    public int supportEnhOpenForProject(String pProject) {
        int returnVal = 0;
		Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) from bugs t0, keyworddefs t1, keywords t2 where (t0.bug_status='NEW' || t0.bug_status='ASSIGNED' || t0.bug_status='REOPENED') and t0.bug_severity='enhancement' and t0.version='"+ pProject+ "' and t0.bug_id=t2.bug_id and t1.id=t2.keywordid and t1.name = 'support_escalation'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
		if(val != null) {
			returnVal = val.intValue();
		}
		
        return returnVal;
    }

    public int supportEnhResolvedForSelectedProject() {
        return supportEnhResolvedForProject(selectedProject());
    }

    public int supportEnhResolvedForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) from bugs t0, keyworddefs t1, keywords t2 where t0.bug_status='RESOLVED' and t0.bug_severity='enhancement' and t0.version='"+ pProject+ "' and t0.bug_id=t2.bug_id and t1.id=t2.keywordid and t1.name = 'support_escalation'");
       // NSArray values =  s.rowsForSql("select count(*) from bugs where bug_status='RESOLVED'  and bug_severity='enhancement' and version='"+ pProject+ "' and keywords.keywordName = 'support_escalation'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int supportEnhClosedForSelectedProject() {
        return supportEnhClosedForProject(selectedProject());
    }

    public int supportEnhClosedForProject(String pProject) {
        Session s = (Session)session();
        //NSArray values =  s.rowsForSql("select count(*) from bugs where bug_status='CLOSED' and product_id=5 and version='"+ pProject+ "' and keywords like 'support'");
        NSArray values =  s.rowsForSql("select count(*) from bugs t0, keyworddefs t1, keywords t2 where t0.bug_status='CLOSED' and t0.bug_severity='enhancement' and t0.version='"+ pProject+ "' and t0.bug_id=t2.bug_id and t1.id=t2.keywordid and t1.name = 'support_escalation'");
        //NSArray values =  s.rowsForSql("select count(*) from bugs where bug_status='CLOSED' and bug_severity='enhancement'  and version='"+ pProject+ "' and keywords.keywordName = 'support_escalation'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }

    public int supportBugsOpenForSelectedProject() {
        return supportBugsOpenForProject(selectedProject());
    }

    public int supportBugsOpenForProject(String pProject) {
        Session s = (Session)session();
        //NSArray values =  s.rowsForSql("select count(*) from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and bug_severity !='enhancement' and version='"+ pProject+ "' and keywords like '%support%'");
        NSArray values =  s.rowsForSql("select count(*) from bugs t0, keyworddefs t1, keywords t2 where (t0.bug_status='NEW' || t0.bug_status='ASSIGNED' || t0.bug_status='REOPENED') and t0.bug_severity!='enhancement' and t0.version='"+ pProject+ "' and t0.bug_id=t2.bug_id and t1.id=t2.keywordid and t1.name = 'support_escalation'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int supportBugsResolvedForSelectedProject() {
        return supportBugsResolvedForProject(selectedProject());
    }

    public int supportBugsResolvedForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) from bugs t0, keyworddefs t1, keywords t2 where t0.bug_status='RESOLVED' and t0.bug_severity!='enhancement' and t0.version='"+ pProject+ "' and t0.bug_id=t2.bug_id and t1.id=t2.keywordid and t1.name = 'support_escalation'");
       // NSArray values =  s.rowsForSql("select count(*) from bugs where bug_status='RESOLVED' and bug_severity !='enhancement' and version='"+ pProject+ "' and keywords.keywordName = 'support_escalation'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int supportBugsClosedForSelectedProject() {
        return supportBugsClosedForProject(selectedProject());
    }

    public int supportBugsClosedForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) from bugs where bug_status='CLOSED' and bug_severity !='enhancement' and version='"+ pProject+ "' and keywords like '%support%'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }

    public int resolvedEnhancementsForSelectedProject() {
        return resolvedEnhancementsForProject(selectedProject());
    }

    public int resolvedEnhancementsForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where bug_status='RESOLVED' and bug_severity ='enhancement'  and version='"+ pProject+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }

    public double percentDefectsCompleteForSelectProject() {
        return percentDefectsCompleteForProject(selectedProject());
    }

    public double percentDefectsCompleteForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and bug_severity !='enhancement'  and version='"+ pProject+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        //values =  s.rowsForSql("select count(*) count from bugs where product_id=3 and version='"+ pProject+ "'");
        values =  s.rowsForSql("select count(*) count from bugs where bug_severity !='enhancement' and version='"+ pProject+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        return (fixed/all)*100;
    }

    public double percentEnhancementsCompleteForSelectedProject() {
        return percentEnhancementsCompleteForProject(selectedProject());
    }
    public double percentEnhancementsCompleteForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and bug_severity ='enhancement'  and version='"+ pProject+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
       // values =  s.rowsForSql("select count(*) count from bugs where product_id=5 and version='"+ pProject+ "'");
        values =  s.rowsForSql("select count(*) count from bugs where bug_severity ='enhancement' and version='"+ pProject+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        return (fixed/all)*100;
    }


    public double percentWorkItemsCompleteForSelectedProject() {
        return percentWorkItemsCompleteForProject(selectedProject());
    }

    public double percentWorkItemsCompleteForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and product_id=8 and version='"+ pProject+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        values =  s.rowsForSql("select count(*) count from bugs where product_id=8 and version='"+ pProject+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        return (fixed/all)*100;
    }

    public double percentQATasksCompleteForSelectedProject() {
        return percentQATasksComplete(selectedProject());
    }

    public double percentQATasksComplete(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and product_id=6 and version='"+ pProject+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        //System.out.println("QA Tasks - fixed - " + fixed + " - " + pProject);
        values =  s.rowsForSql("select count(*) count from bugs where product_id=6 and version='"+ pProject+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        //System.out.println("QA Tasks - all - " + all + " - " + pProject);

        return (fixed/all)*100;
    }
    public int itemsInTriageForSelectedProject() {
        return itemsInTriageForProject(selectedProject());
    }

    public int itemsInTriageForProject(String pProject) {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and assigned_to='23' and version='"+ pProject+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }

    WOComponent goListResolvedDefectsForProject(String pProject)  {

        //ItemList lb = (ItemList)listPageForSQL("bugStatus='RESOLVED' and product_id=3 and version='"+ pProject+ "'");
        //ItemList lb = (ItemList)listPageForSQL("bugStatus='RESOLVED' and bug_severity !='enhancement' and version='"+ pProject+ "'");
        ReportListWrapper lb = (ReportListWrapper)listPageForSQL("bugStatus='RESOLVED' and bugSeverity !='enhancement' and version='"+ pProject+ "'");
        lb.setIsList(false);
        lb.setIsReport(true);
        lb.setARow("Originator");
        lb.setAColumn("Priority");
        return (WOComponent)lb;
    }
    WOComponent goListResolvedEnhancementsForProject(String pProject) {
       // ItemList lb = (ItemList)listPageForSQL("bugStatus='RESOLVED' and product_id=5 and version='"+pProject + "'");
        ReportListWrapper lb = (ReportListWrapper)listPageForSQL("bugStatus='RESOLVED' and bugSeverity='enhancement' and version='"+pProject + "'");
        lb.setIsList(false);
        lb.setIsReport(true);
        lb.setARow("Originator");
        lb.setAColumn("Priority");

        return (WOComponent)lb;
    }
    WOComponent listPageForSQL(String pStatement) {
        EOQualifier qualifier = null;
        EODatabaseDataSource _queryDataSource = null;
        EOFetchSpecification fs = null;
        qualifier = EOQualifier.qualifierWithQualifierFormat(pStatement, null);
        fs = new EOFetchSpecification("Item", qualifier,null);
        fs.setRefreshesRefetchedObjects(true);

        _queryDataSource =new EODatabaseDataSource(editingContext(), "Item");
        _queryDataSource.setFetchSpecification(fs);
        //ItemList listPage=(ItemList)D2W.factory().pageForConfigurationNamed("ListMyBugs",session());
		
		WOComponent listPage = (WOComponent)pageWithName("ReportListWrapper");
		WODisplayGroup displayGroup = new WODisplayGroup();
		displayGroup.setDataSource(_queryDataSource);
		displayGroup.fetch();
		listPage.takeValueForKey(displayGroup, "displayGroup");

        return listPage;

    }

    public NSTimestamp dateForMilestone(String pMilestone, String pProject) {
        ////System.out.println("pMilestone - " + pMilestone);
        NSTimestamp returnVal = null;
        Enumeration enumer = milestoneDatesForProject(pProject).objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject eo = (EOEnterpriseObject)enumer.nextElement();
            if(pMilestone.equals((String)eo.valueForKey("milestone"))) {
                returnVal = (NSTimestamp)eo.valueForKey("startDate");
                break;
            }
        }
        return returnVal;
    }
    public NSTimestamp dateForMilestone(String pMilestone) {
        ////System.out.println("pMilestone - " + pMilestone);
        NSTimestamp returnVal = null;
        Enumeration enumer = milestoneDatesForSelectedProject().objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject eo = (EOEnterpriseObject)enumer.nextElement();
            if(pMilestone.equals((String)eo.valueForKey("milestone"))) {
                returnVal = (NSTimestamp)eo.valueForKey("startDate");
                break;
            }
        }
        return returnVal;
    }

    public NSArray milestoneDatesForSelectedProject() {
        return milestoneDatesForProject(selectedProject());
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

    public boolean isProductionRelease() {
        boolean returnVal = false;

        if(selectedProject != null) {
            Session s = (Session)session();

            if(selectedProject.equals(s.supportProject)) {
                returnVal = true;
            }
        }
        return returnVal;
    }

    public WOComponent emailCurrentPage() {
        Session s = (Session)session();
        //EOEnterpriseObject u = (EOEnterpriseObject)s.getUser();
        //String email = (String)u.valueForKey("loginName");
        //String realName = (String)u.valueForKey("realname");
        //String from = realName + " <"+email+">";
        String from = "Zilla <zilla@reardencommerce.com>";

        EmailPage nextPage = (EmailPage)pageWithName("EmailPage");
        WOComponent component = (WOComponent)pageWithName("ProductCouncilReport");

        nextPage.takeValueForKey(component,"componentToEmail");
        nextPage.takeValueForKey(component,"nextPage");
        nextPage.takeValueForKey(from,"from");
        nextPage.takeValueForKey("-- Product Council Report --", "subject");

        return nextPage;
    }

    public WOComponent refresh() {
        editingContext().refaultObjects(); 
        supportTickets = null;
        incidents = null;
        return null;
    }
    public EOEditingContext editingContext() {
        return editingContext;
    }
    public void setEditingContext(EOEditingContext pValue) {
        editingContext = pValue;
    }
    

}
