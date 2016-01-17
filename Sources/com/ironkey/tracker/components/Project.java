package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;
import java.math.BigDecimal;
import java.util.StringTokenizer;

public class Project extends WOComponent {
	private static final long serialVersionUID = 1L;
    public WODisplayGroup displayGroup;
    protected EODatabaseDataSource _queryDataSource;
    private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    protected int totalBugs;
    protected int totalActive;
    protected boolean didUpdate = false;
    protected String label;
    public String aRow = "Assignee";
    public String aColumn = "Priority";
    protected boolean isList;
    protected boolean isEstimate;
    protected boolean isReport = true;
    public boolean hideController = false;
    protected NSArray<String> selectedStatus;
    public String aStatus;
    protected NSArray<String> itemProducts;
    protected NSArray<String> potentialProjects;
    protected NSArray<String> selectedItemProducts = new NSArray<String>(new String[] {"Media Platform"});
    protected NSArray<String> itemTypes;
   //protected String selectedItem;	
    protected String selectedProduct;	
    public NSArray<String> selectedItemTypes = new NSArray<String>(new String[] {"Bug", "Enhancement"});
    public String aString;
    protected boolean selectIndividualEstimate = false;
    protected String selectedProject;
    //protected MilestoneDates aMilestone;
    protected String selectedMilestone;
	public boolean needUpdate = false;
	public int itemsInTriage = -1;

    public Project(WOContext aContext) {
        super(aContext);
        //System.out.println("Project.Project() ");

        // ensures we have a public default constructor
        Class<?>[] args= { com.webobjects.foundation.NSNotification.class };
        //register for change notifications to know when to refresh list
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("editingContextDidSaveChanges",args),
                                                         EOEditingContext.EditingContextDidSaveChangesNotification,
                                                         null);

        String initalStatus[] = {"UNCONFIRMED","CONFIRMED","ASSIGNED", "REOPENED"};
        selectedStatus = new NSArray<String>(initalStatus);

        Session mysession = (Session)session();
        _queryDataSource =new EODatabaseDataSource(mysession.defaultEditingContext(), "Item");
		getDefaultsFromCookies();
        selectedProject = mysession.selectedProject();
		//System.out.println("selectedProject - " + selectedProject);
	    performSearch();
		needUpdate = true;
    }
	
	
	public void takeValuesFromRequest( WORequest aRequest, WOContext aContext) {
        //System.out.println("Project - takeValuesFromRequest() ");

        try {
            /*
            NSDictionary<String, NSArray<Object>> theDict = aRequest.formValues();
            Enumeration<String> enum1 = theDict.keyEnumerator();
            
            while(enum1.hasMoreElements()) {
               String theKey = (String)enum1.nextElement();
               //System.out.println("theKey = " + theKey + " = " + theDict.objectForKey(theKey));
            }
            */
            //String product = (String)aRequest.formValueForKey("products");
			
			NSArray<Object> productIndices = (NSArray<Object>)aRequest.formValuesForKey("products");
			if((aRequest != null) && (productIndices != null)) {	
					
				NSArray<String> products = convertIndicesToProducts(productIndices);
				if(!products.equals(selectedItemProducts())) {
					//System.out.println("++++++++++ it changed ++++++++");
					setSelectedItemProducts(products);
					potentialProjects = null;
				}
				else {
					super.takeValuesFromRequest(aRequest, aContext);
				}
			}
			else{
				//System.out.println("++++++++++ No changed ++++++++");
				// set all keys and submit normally
				super.takeValuesFromRequest(aRequest, aContext);
			}
        }
        catch(Exception e) {
            System.err.println("++++  Project.takeValuesFromRequest() - " + e);
        }
    }
	

	public NSArray<String> convertIndicesToProducts(NSArray<Object> pIndices) {
		NSMutableArray<String> products = new NSMutableArray<String>();
		Enumeration<Object> enum1 = pIndices.objectEnumerator();
		while(enum1.hasMoreElements()) {
		   String theKey = (String)enum1.nextElement();
		   String aProduct = (String)itemProducts.objectAtIndex(Integer.parseInt(theKey));

		   products.addObject(aProduct);
		}
		return (NSArray<String>)products;
	}

	public void setNeedUpdate(boolean pVal) {
	 // do nothing
	}
	
	
	public NSArray<String> potentialProjects() {
		Session s;
		String productIdString = "(";
		String sqlString;
		boolean success = false;
		
		if(potentialProjects == null) {
			s = (Session)session();

			int count = selectedItemProducts().count();
			//System.out.println("count" + count);
			//System.out.println("selectedItemProducts - " + selectedItemProducts.toString());

			for(int i=0; i< count; i++) {
			   String aProduct = (String)selectedItemProducts().objectAtIndex(i);
			   //System.out.println("aProduct - " + aProduct);
			   int prodId = idForProduct(aProduct);
			   if(prodId > 0) {
				   success = true;
				   productIdString += prodId;
				   
				   if(i< count-1) {
						productIdString += ",";
					}
				}
			}
			if(success == true) {
				productIdString += ")";
				sqlString = "select distinct(value) from versions where product_id in " + productIdString + "  order by value";
			
				potentialProjects =  (NSArray<String>)s.rowsForSql(sqlString);
			}
		}
		
		return potentialProjects;
	}
	
	// need to cache at session or application level
	public int idForProduct(String pProductName) {
        Session s = (Session)session();
		return s.idForProduct(pProductName);
    }



    public void performSearch() {
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
		
		//System.out.println("Project.performSearch()");
		// Product
		NSMutableArray<EOQualifier> temp = new NSMutableArray<EOQualifier>();
		Enumeration<String> enumer = selectedItemProducts().objectEnumerator();
		while(enumer.hasMoreElements()) {
			String theType = (String)enumer.nextElement();
			temp.addObject(EOQualifier.qualifierWithQualifierFormat("product.productName='" + theType + "'", null));
		}
		qual.addObject(new EOOrQualifier(temp));

		// Type
		temp = new NSMutableArray<EOQualifier>();
		enumer = selectedItemTypes.objectEnumerator();
		while(enumer.hasMoreElements()) {
			String theType = (String)enumer.nextElement();
			temp.addObject(EOQualifier.qualifierWithQualifierFormat("type='" + theType + "'", null));
		}
		qual.addObject(new EOOrQualifier(temp));
        
		
        // Status ('NEW', 'ASSIGNED', 'REOPENED', 'RESOLVED', 'VERIFIED' ,'CLOSED')
        int numStatus = selectedStatus.count();
        if(numStatus < 6) {
			temp = new NSMutableArray();
			enumer = selectedStatus.objectEnumerator();
            while(enumer.hasMoreElements()) {
                String theStatus = (String)enumer.nextElement();
                temp.addObject(EOQualifier.qualifierWithQualifierFormat("bugStatus='" + theStatus + "'", null));
            }
            qual.addObject(new EOOrQualifier(temp));
        }

        // Project
        qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='"+ selectedProject() + "' ", null));

        // Milestone
        if(selectedMilestone != null) {
            qual.addObject(EOQualifier.qualifierWithQualifierFormat("targetMilestone='"+ selectedMilestone + "' ", null));
        }

        fs = new EOFetchSpecification("Item", new EOAndQualifier(qual),null);
        fs.setRefreshesRefetchedObjects(true);

        _queryDataSource.setFetchSpecification(fs);
        displayGroup.setDataSource(_queryDataSource);
        displayGroup.fetch();
        didUpdate = true;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        if (_hasToUpdate) {
            refreshData();
            _hasToUpdate=false;
        }

        super.appendToResponse(r,c);
    }
    public void editingContextDidSaveChanges(NSNotification notif) {
        _hasToUpdate=true;
    }
    public void refreshData() {
        displayGroup.fetch();
        totalActive = displayGroup.allObjects().count();
    }

    public void finalize() throws Throwable {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        super.finalize();
    }


    public WOComponent resetDisplayedBugs() {
        session().takeValueForKey(selectedProject, "selectedProject");
		//System.out.println("Project.resetDisplayedBugs() ");

		itemsInTriage = -1;
        performSearch();
		needUpdate = true;
        session().takeValueForKey(context().page(), "homePage");

        return null;
    }

    public boolean isList() {
        return isList;
    }
    public void setIsList(boolean newIsList) {
        isList = newIsList;
    }

    public WOComponent showReport() {
        setIsReport(true);
        setIsList(false);
        setIsEstimate(false);

        return null;
    }
    public WOComponent showList() {
        setIsReport(false);
        setIsList(true);
        setIsEstimate(false);
        // sort order
        Object orderings[]={
            EOSortOrdering.sortOrderingWithKey("priority", EOSortOrdering.CompareCaseInsensitiveAscending),
            EOSortOrdering.sortOrderingWithKey("product.productName", EOSortOrdering.CompareCaseInsensitiveAscending),
            EOSortOrdering.sortOrderingWithKey("assignee.realname", EOSortOrdering.CompareCaseInsensitiveAscending)
        };

        displayGroup.setSortOrderings(new NSArray(orderings));
        displayGroup.setNumberOfObjectsPerBatch(25);
        displayGroup.updateDisplayedObjects();
        session().takeValueForKey(context().page(), "homePage");

        return null;
    }
    public WOComponent showEstimate() {
        setIsReport(false);
        setIsList(false);
        setIsEstimate(true);
        selectIndividualEstimate = false;
        return null;
    }

    public String label() {
        if(isList() == true) {
            label = "List";
        }
        else if(isReport() == true) {
            label = "Report";
        }
        else if(isEstimate() == true) {
            label = "Estimate";
        }
        return label;
    }
    public void setLabel(String newLabel) {
        label = newLabel;
    }
/*

    public String listURL() {
        Enumeration enumer;
        String allBugNumbers = "";
        String URL;
        if(isListEmpty() == false) {
            enumer = displayGroup.allObjects().objectEnumerator();
            while(enumer.hasMoreElements()) {
                EOEnterpriseObject currObject = (EOEnterpriseObject)enumer.nextElement();
                allBugNumbers += currObject.valueForKey("bugId") + ",";

            }
            URL = "http://bugzilla/buglist.cgi?bug_id=" + allBugNumbers;
                        //String URL = "http://bugzilla/show_bug.cgi?id=" +currentObject.valueForKey("bugId");
        }
        else {
            URL = "#";
        }
        return URL;
    }    
	
	public WODisplayGroup displayGroup() {
        return displayGroup;
    };
	*/
    public void setDisplayGroup(WODisplayGroup pDisplayGroup) {
        displayGroup = pDisplayGroup;
    }
    public String aRow() {
        return aRow;
    }
    public void setARow(String newRowType) {
        aRow = newRowType;
    }
    public boolean isListEmpty()
    {
        return (listSize()==0);
    }

    public int listSize()
    {
        return displayGroup.allObjects().count();
    }
    public boolean isToolBar() {
        return true;
    }

    public boolean isEstimate() {
        return isEstimate;
    }
    public void setIsEstimate(boolean newIsEstimate) {
        isEstimate = newIsEstimate;
    }

    public boolean isReport() {
        return isReport;
    }
    public void setIsReport(boolean newIsReport) {
        isReport = newIsReport;
    }
	

    public WOComponent componentToEmail() {
        WOComponent comp = null;
        if(isList() == true) {
            comp = (WOComponent)pageWithName("ItemList");
			comp.takeValueForKey(displayGroup, "itemDisplayGroup");
   //         ((ItemList)comp).displayAll();  // no batching in emails
     //       ((ItemList)comp).setEmailFlag(true);  // set a flag for emails to hide any content that does not display properly
		}
        else if(isReport() == true) {
            comp = (ReportGenerator)pageWithName("ReportGenerator");
            //((ReportGenerator)comp).setNeedUpdate(true);
            comp.takeValueForKey(aColumn, "selectedColumn");
            comp.takeValueForKey(aRow, "selectedRow");
            comp.takeValueForKey(displayGroup, "displayGroup");
            ((ReportGenerator)comp).setHideReportSpecifier(true);
            ((ReportGenerator)comp).setHideGraphLink(true);
        }
		/*
        else if(isEstimate() == true) {
            comp = (WOComponent)pageWithName("EstimateActual");
            comp.takeValueForKey(displayGroup, "displayGroup");
            ((EstimateActual)comp).setIsSelected(false);
            ((EstimateActual)comp).setShowLinks(false);
        }
		*/
        return (WOComponent)comp;
    }
	

    public void setComponentToEmail(WOComponent pComponent) {
        // do nothing
    }
    public WOComponent currentComponent() {
        Session s;
        s = (Session)session();

        return s.homePage;
    }
	/*
    public SelectDefaults selectDefaultProject() {
        SelectDefaults sd = (SelectDefaults)pageWithName("SelectDefaults");
        sd.setNextPage((HomePage)session().valueForKey("homePage"));
        return sd;
    }
*/
    public void setSelectedStatus(NSArray pValue) {

        if((pValue != null) && (pValue.count() != 0)) {
            selectedStatus = pValue;
        }

    }
    public NSArray selectedStatus() {
        return selectedStatus;
    }
	
	public String selectedProject() {
        return selectedProject;
    }
	public void setSelectedProject(String aVal) {
		if((selectedProject != null) && (!selectedProject.equals(aVal))) {
			setDefaultsCookie("default_project",aVal);
		}
		
		selectedProject = aVal;
    }
	public void getDefaultsFromCookies() {
        WOContext context = context();
        WORequest request = context.request();
		Session s;
		String default_project = null;
		String default_products = null;
		        
		s = (Session)session();
		default_project = request.cookieValueForKey("default_project");
		default_products = request.cookieValueForKey("default_products");
		
		if(default_project != null) {
			s.setSelectedProject(default_project);
        }

		if(default_products != null) {
			StringTokenizer tokens = new StringTokenizer(default_products, "+");
			NSMutableArray productArray = new NSMutableArray();
			while(tokens.hasMoreElements()) {
				String aProduct = tokens.nextToken();
				if(aProduct.equals("IronKey Drive")) {
					aProduct = "Device: Secure Storage";
				}
				productArray.addObject(aProduct);
			}
			setSelectedItemProducts(productArray);
        }
		
    }
	
	
	public void setDefaultsCookie(String pCookieName, String pValue) {
        NSTimestamp now = new NSTimestamp();
        NSTimestamp yearFromNow = now.timestampByAddingGregorianUnits(1,0,0,0,0,0);
        WOContext context = context();
        WOResponse response = context.response();
        WORequest request = context.request();

        WOCookie cookie = WOCookie.cookieWithName(pCookieName, pValue);
        cookie.setExpires(yearFromNow);
        cookie.setPath(request.adaptorPrefix() + "/");
        //cookie.setPath(request.adaptorPrefix() + "/" + request.applicationName());
        response.addCookie(cookie);
    }
    public FindFixReport goFindFix() {
		FindFixReport nextPage = (FindFixReport)pageWithName("FindFixReport");
        nextPage.setSelectedProject(selectedProject());
        nextPage.findFixOpenReport();
		return nextPage;
    }
    
    public FindFixReport goTopPriorityFindFix() {
		FindFixReport nextPage = (FindFixReport)pageWithName("FindFixReport");
        nextPage.setSelectedProject(selectedProject());
        nextPage.setOnlyTopPriority(true);
        nextPage.findFixOpenReport();
		return nextPage;
    }

    public int resolvedBugsForProject() {
        Session s = (Session)session();
		//System.out.println("\n\n============== Project.resolvedBugsForProject()");
		
        NSArray values =  s.rowsForSql("select count(*) count from bugs where bug_status='RESOLVED' and (cf_type='Bug' or cf_type='Enhancement') and product_id in " + selectedProductIds() + " and version='"+ selectedProject()+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }
    public int resolvedEnhancementsForProject() {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where bug_status='RESOLVED' and product_id=5 and version='"+ selectedProject()+ "'");
        BigDecimal val = (BigDecimal)values.objectAtIndex(0);
        return val.intValue();
    }


    public double percentDefectsComplete() {
        Session s = (Session)session();
		//System.out.println("\n\n============== Project.percentDefectsComplete()");
		
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and product_id in " + selectedProductIds() + " and version='"+ selectedProject()+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        values =  s.rowsForSql("select count(*) count from bugs where product_id in " + selectedProductIds() + " and version='"+ selectedProject()+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        //System.out.println("fixed - " + fixed);
        //System.out.println("all - " + all);
		
		if(all == 0.0){
			return 0.0;
		}
		else {
			return (fixed/all)*100;
		}
    }
    public double percentEnhancementsComplete() {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and product_id=5 and version='"+ selectedProject()+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        values =  s.rowsForSql("select count(*) count from bugs where product_id=5 and version='"+ selectedProject()+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        return (fixed/all)*100;
    }
    public double percentWorkItemsComplete() {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and product_id=8 and version='"+ selectedProject()+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        values =  s.rowsForSql("select count(*) count from bugs where product_id=8 and version='"+ selectedProject()+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        return (fixed/all)*100;
    }
    public double percentQATasksComplete() {
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='RESOLVED' || bug_status='CLOSED') and product_id=6 and version='"+ selectedProject()+ "'");
        double fixed = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        values =  s.rowsForSql("select count(*) count from bugs where product_id=6 and version='"+ selectedProject()+ "'");
        double all = ((BigDecimal)values.objectAtIndex(0)).doubleValue();
        return (fixed/all)*100;
    }
	
	public String selectedProductIds() {
		int count = selectedItemProducts().count();
		String productIdString = "(";

		for(int i=0; i< count; i++) {
		   String aProduct = (String)selectedItemProducts().objectAtIndex(i);
		   int prodId = idForProduct(aProduct);
		   if(prodId > 0) {
			   productIdString += prodId;
			   
			   if(i< count-1) {
					productIdString += ",";
				}
			}
		}
		 productIdString += ")";
		 return productIdString;
	}
	
	
    public int itemsInTriage() {
		Session s;
	
        if(itemsInTriage < 0) {
			s = (Session)session();

			//System.out.println("\n\n============== Project.itemsInTriage()");
		   NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and product_id in " + selectedProductIds() + " and assigned_to='23' and version='"+ selectedProject()+ "'");
		//    NSArray values =  s.rowsForSql("select count(*) count from bugs where (bug_status='NEW' || bug_status='ASSIGNED' || bug_status='REOPENED') and assigned_to='23' and version='"+ selectedProject()+ "'");
			BigDecimal val = (BigDecimal)values.objectAtIndex(0);
			itemsInTriage = val.intValue();
		}
		return itemsInTriage;
    }

/*
    WOComponent listPageForSQL(String pStatement) {
        EOQualifier qualifier = null;
        EODatabaseDataSource _queryDataSource = null;
        EOFetchSpecification fs = null;
        qualifier = EOQualifier.qualifierWithQualifierFormat(pStatement, null);
        fs = new EOFetchSpecification("Item", qualifier,null);
        _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");

        _queryDataSource.setFetchSpecification(fs);
        ListBugs listPage=(ListBugs)D2W.factory().pageForConfigurationNamed("ListMyBugs",session());
        listPage.setDataSource(_queryDataSource);
        return listPage;

    }
	
    public NSTimestamp dateForMilestone(String pMilestone) {
        ////System.out.println("pMilestone - " + pMilestone);
        NSTimestamp returnVal = null;
        Enumeration enumer = milestoneDatesForProject().objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject eo = (EOEnterpriseObject)enumer.nextElement();
            if(pMilestone.equals((String)eo.valueForKey("milestone"))) {
                returnVal = (NSTimestamp)eo.valueForKey("startDate");
                break;
            }
        }
        return returnVal;
    }

    public NSArray milestoneDatesForProject() {
        EOFetchSpecification fs;
        NSDictionary bindings = null;
        NSArray milestoneDatesForProject;

        //if(milestoneDatesForProject == null) {
        // Tasks - Defects, Enhancements, WorkItems
        bindings = new NSDictionary(new Object[] {selectedProject}, new Object[] { "version"});
        fs = EOFetchSpecification.fetchSpecificationNamed( "milestoneDates", "MilestoneDates").fetchSpecificationWithQualifierBindings( bindings );
        milestoneDatesForProject =  (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);

        // }
        return milestoneDatesForProject;
    }
	*/
	public WOComponent emailCurrentPage() {
		WOComponent nextPage = (WOComponent)pageWithName("EmailPage");

       nextPage.takeValueForKey(componentToEmail(),"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");

        return nextPage;
    }

    public NSArray itemTypes() {
        if(itemTypes == null) {
            Session s = (Session)session();
            NSMutableArray tempTypes = new NSMutableArray(s.potentialTypes());
            //tempTypes.insertObjectAtIndex("Defects & Enhance", 0);
            //tempTypes.insertObjectAtIndex("All", 0);

            itemTypes = new NSArray(tempTypes);
            // Object itemTyp[] = {"All", "Defects","Feature","Enhancements", "Deployment Issues", "Work Item", "Operations Request", "Partner Issue"};
        }
        return itemTypes;
    }
    public NSArray itemProducts() {
        if(itemProducts == null) {
            Session s = (Session)session();
            NSMutableArray tempTypes = new NSMutableArray();
			Enumeration enumer = s.potentialProducts().objectEnumerator();
			while(enumer.hasMoreElements()) {
				String aProduct = (String)enumer.nextElement();
				if(!aProduct.startsWith("__")) {
					tempTypes.addObject(aProduct);
				}
			}
            itemProducts = new NSArray(tempTypes);
        }
        return itemProducts;
    }
	
	public NSArray selectedItemProducts() {
		return selectedItemProducts;
	}
	public void setSelectedItemProducts(NSArray pValues) {
		if((selectedItemProducts != null) && (!selectedItemProducts.equals(pValues))) {
			setDefaultsCookie("default_products", tokenizeProductArray(pValues));
			//System.out.print("pValues - " + pValues.toString());
		}

		// if different that current
		selectedItemProducts = pValues;
	}
	

	
	public String tokenizeProductArray(NSArray pValues) {
		String returnVal = "";
		int count = pValues.count();
		for(int i = 0; i<count; i++) {
			returnVal += pValues.objectAtIndex(i);
			if(i<count-1) {
				returnVal += "+";
			}
		}
		return returnVal;
	}
	
    public String triageURL() {
        String bugNumbers = ((Application)Application.application()).bugzillaHostUrl() + "/bugzilla/buglist.cgi?bug_id=";
        NSDictionary bindings = new NSDictionary(new Object[] {selectedProject}, new Object[] { "version"});
        EOFetchSpecification fs=EOFetchSpecification.fetchSpecificationNamed("triageBugs","Item").fetchSpecificationWithQualifierBindings(bindings);
        NSArray triageBugs = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
        Enumeration enumer = triageBugs.objectEnumerator();
        while(enumer.hasMoreElements()) {
            EOEnterpriseObject item = (EOEnterpriseObject)enumer.nextElement();
            bugNumbers += (Number)item.valueForKey("bugId") + ",";
        }
        return bugNumbers;
    }
    
    public AutomationQueues goAutomationQueues()
    {
        AutomationQueues nextPage = (AutomationQueues)pageWithName("AutomationQueues");

        // Initialize your component here

        return nextPage;
    }
    
    public Reopened goReopened()
    {
        Reopened nextPage = (Reopened)pageWithName("Reopened");

        // Initialize your component here

        return nextPage;
    }
    
    public TopSupportIssuesList goTopSupport()
    {
        TopSupportIssuesList nextPage = (TopSupportIssuesList)pageWithName("TopSupportIssuesList");

        // Initialize your component here

        return nextPage;
    }
    public ReleaseReport goReleaseReport()
    {
        ReleaseReport nextPage = (ReleaseReport)pageWithName("ReleaseReport");

        // Initialize your component here

        return nextPage;
    }
    public Recent goRecent()
    {
        Recent nextPage = (Recent)pageWithName("Recent");

        // Initialize your component here

        return nextPage;
    }
    
    public ReleaseOverviewReport goReleaseOverview()
    {
        ReleaseOverviewReport nextPage = (ReleaseOverviewReport)pageWithName("ReleaseOverviewReport");
        return nextPage;
    }
    
    public StoryPriorities goStoryPriorities()
    {
        StoryPriorities nextPage = (StoryPriorities)pageWithName("StoryPriorities");

        // Initialize your component here

        return nextPage;
    }

    public TopCustomerIssuesList goTopCustomerIssues()
    {
        TopCustomerIssuesList nextPage = (TopCustomerIssuesList)pageWithName("TopCustomerIssuesList");

        // Initialize your component here

        return nextPage;
    }
    
    public CustomerIssues goCustomerIssues()
    {
        CustomerIssues nextPage = (CustomerIssues)pageWithName("CustomerIssues");

        // Initialize your component here

        return nextPage;
    }
    
    public TaserReport goTaser()
    {
        TaserReport nextPage = (TaserReport)pageWithName("TaserReport");

        // Initialize your component here

        return nextPage;
    }
    
    public Ops goOCM()
    {
        Ops nextPage = (Ops)pageWithName("Ops");
        return nextPage;
    }

    public WorkProducts goWorkProducts()
    {
        WorkProducts nextPage = (WorkProducts)pageWithName("WorkProducts");

        // Initialize your component here

        return nextPage;
    }
    public TestPage goTestPage()
    {
        TestPage nextPage = (TestPage)pageWithName("TestPage");
        nextPage.setSelectedProject(selectedProject());
        // Initialize your component here

        return nextPage;
    }



    public BacklogTracking goBacklogTracker()
    {
        BacklogTracking nextPage = (BacklogTracking)pageWithName("BacklogTracking");

        // Initialize your component here

        return nextPage;
    }

    public SecurityIssues goSecurityIssues()
    {
        SecurityIssues nextPage = (SecurityIssues)pageWithName("SecurityIssues");

        // Initialize your component here

        return nextPage;
    }

    public ReleaseNotes goReleaseNote()
    {
        ReleaseNotes nextPage = (ReleaseNotes)pageWithName("ReleaseNotes");
        nextPage.setSelectedProject(selectedProject());
        // Initialize your component here

        return nextPage;
    }

    public NewStory goNewStory()
    {
        NewStory nextPage = (NewStory)pageWithName("NewStory");

        // Initialize your component here

        return nextPage;
    }
    
    public ReleasePlan goReleasePlan()
    {
    	ReleasePlan nextPage = (ReleasePlan)pageWithName("ReleasePlan");
        return nextPage;
    }

    
    /*
    public FeatureOverview goFeatureStatus()
    {
        FeatureOverview nextPage = (FeatureOverview)pageWithName("FeatureOverview");
        nextPage.setSelectedProject(selectedProject());
        // Initialize your component here

        return nextPage;
    }

    public ReleaseProjectReport goReleaseProject()
    {
        ReleaseProjectReport nextPage = (ReleaseProjectReport)pageWithName("ReleaseProjectReport");
        return nextPage;
    }
    public ProjectsByReleaseReport goProjectsByRelease()
    {
        ProjectsByReleaseReport nextPage = (ProjectsByReleaseReport)pageWithName("ProjectsByReleaseReport");
        return nextPage;
    }



    public WOComponent goReleaseNotes()
    {
        ReleaseNotesReport nextPage = (ReleaseNotesReport)pageWithName("ReleaseNotesReport");
        return nextPage;
    }


    public ReleaseTracking goReleaseTracking()
    {
        ReleaseTracking nextPage = (ReleaseTracking)pageWithName("ReleaseTracking");

        // Initialize your component here

        return nextPage;
    }


    public EditRelease goEditRelease()
    {
        EditRelease nextPage = (EditRelease)pageWithName("EditRelease");
		nextPage.takeValueForKey(context().page(), "nextPage");
        // Initialize your component here

        return nextPage;
    }

    public Escalate goEscalate()
    {
        Escalate nextPage = (Escalate)pageWithName("Escalate");
		nextPage.takeValueForKey(context().page(), "nextPage");
        return nextPage;
    }

    public TopSustainingList goTopSustainingIssues()
    {
        TopSustainingList nextPage = (TopSustainingList)pageWithName("TopSustainingList");

        // Initialize your component here

        return nextPage;
    }


*/
}