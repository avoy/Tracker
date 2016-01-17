package com.ironkey.tracker;

import com.ironkey.tracker.components.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;

//import er.extensions.appserver.ERXDirectAction;


public class DirectAction extends WODirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults defaultAction() {

		if(validateUser() == true) { 
			return pageWithName("HomePage");
		}
		else {
			return pageWithName("HomePage");
		}
    }
    
    
	public ReleasePlan releasePlanAction() {
		WORequest request =  this.request();
		String selectedRelease = (String)request.formValueForKey("release");

		session().takeValueForKey(selectedRelease, "selectedProject");

        ReleasePlan nextPage = (ReleasePlan)pageWithName("ReleasePlan");
		nextPage.takeValueForKey(selectedRelease, "selectedProject");
		nextPage.resetDisplayedBugs();
        // Initialize your component here

        return nextPage;
    }
    
    public Escalate supportEscalateAction() {
		return (Escalate)pageWithName("Escalate");
    }
    public ReleaseNotesReport releasenotesAction() {
		return (ReleaseNotesReport)pageWithName("ReleaseNotesReport");
    }
    
    public ReleaseProjectReport projectsAction() {
		return (ReleaseProjectReport)pageWithName("ReleaseProjectReport");
    }
    
    public ProjectsByReleaseReport prioritiesAction() {
		return (ProjectsByReleaseReport)pageWithName("ProjectsByReleaseReport");
    }
    
    public SprintDetails sprintDetailsAction() {
    	
		WORequest request =  this.request();
		String release = (String)request.formValueForKey("release");
		String product = (String)request.formValueForKey("product");
		String sprintRaw = (String)request.formValueForKey("sprint");
		String sprint;
		
		if(release == null) {
			release  = "Amsterdam";
		}
		
		if(product == null) {
			product = "all";
		}
		else {
			if(product.equals("desktop")) {
				product  = "Client: Trusted Access - Desktop";
			}
			else if(product.equals("mobile")) {
				product  = "Client: Trusted Access - Mobile";
			}
			else if(product.equals("service")) {
				product  = "Marble Services";
			}
			else if(product.equals("tab")) {
				product  = "Device: Trusted Access";
			}
		}
		
		if(sprintRaw.length() == 1) {
			sprint = "S" + sprintRaw;
		}
		else if(sprintRaw.length() == 2) {
			sprint = "S" + sprintRaw.substring(1,2);
		}
		else {
			sprint = "S1";
		}
		
		SprintDetails nextPage = (SprintDetails)pageWithName("SprintDetails");
		nextPage.setReleaseStartsWith(release);
		if(product != null) {
			nextPage.setProducts(product);
		}
		nextPage.setSprint(sprint);
		
		return nextPage;
    }    
    
    public CustomerIssues customerIssuesAction() {
    	
		WORequest request =  this.request();
		String customerId = (String)request.formValueForKey("customerId");
		CustomerIssues nextPage = (CustomerIssues)pageWithName("CustomerIssues");
		if(customerId != null) {
			nextPage.setCustomerId(customerId);
		}
		
		return nextPage;
    }
    
	public WOComponent saveStoryPriorityAction() {
        Session session = (Session)session();
        String changesString = null;
		StringTokenizer changes;
        EOFetchSpecification fs;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
		NSArray<Item> itemsToBeUpdated;
        WORequest request =  this.request();

        changesString = (String)request.formValueForKey("changes");
        if(changesString != null) {
            System.out.println("DirectAction.ajaxAction() - changesString : " + changesString);
			changes = new StringTokenizer(changesString, ",");
			//numTokens = changes.countTokens();
			NSMutableDictionary changeDictionary = new NSMutableDictionary();
			while(changes.hasMoreElements()) {
				String aChange = (String)changes.nextToken();  // "123456:13"
				String[] result = aChange.split(":");
				changeDictionary.setObjectForKey(result[1],result[0]);
			}
			
			Enumeration enumer = changeDictionary.keyEnumerator();
			while(enumer.hasMoreElements()) {
				String key = (String)enumer.nextElement();
				qual.addObject(EOQualifier.qualifierWithQualifierFormat("bugId = " + key, null));
			}
			fs = new EOFetchSpecification("Item", new EOOrQualifier(qual), null);
			fs.setRefreshesRefetchedObjects(true);

			itemsToBeUpdated = (NSMutableArray)session.defaultEditingContext().objectsWithFetchSpecification(fs);
			Enumeration enumer2 = itemsToBeUpdated.objectEnumerator();
			while(enumer2.hasMoreElements()) {
				Item anItem = (Item)enumer2.nextElement();
				Integer bugId = (Integer)anItem.valueForKey("bugId");
				Integer priority = new Integer((String)changeDictionary.valueForKey(bugId.toString()));
				anItem.takeValueForKey(priority, "rank");
			}
			try {
				session.defaultEditingContext().saveChanges();
			} 
			catch (Exception exception) {
				System.err.println( " Could not save your changes: " + exception.getMessage());
			}


        }
		return (WOComponent)pageWithName("Main");
	}

/*
    public TaserReport taserAction() {
	
		WORequest request =  this.request();
		String taserId = (String)request.formValueForKey("taserId");
		TaserReport nextPage = (TaserReport)pageWithName("TaserReport");
		if(taserId != null) {
			nextPage.setTaserId(taserId);
		}
		
		return nextPage;
    }
	


    public Ops ocmAction() {
		return (Ops)pageWithName("Ops");
    }
    public SecurityIssues securityIssuesAction() {
		return (SecurityIssues)pageWithName("SecurityIssues");
    }
	
	
    public ReleaseTracking releaseTrackingAction() {
		return (ReleaseTracking)pageWithName("ReleaseTracking");
    }
    public ReleaseReport releaseReportAction() {
		return (ReleaseReport)pageWithName("ReleaseReport");
    }
	

    public HomePage viewAction() {
        Session session = (Session)session();
        String type = null;
        String project = null;
        WORequest request =  this.request();

        type = (String)request.formValueForKey("type");
        project = (String)request.formValueForKey("project");
        validateUser();
        if(project != null) {
            session.setSelectedProject(project);
        }
        
        HomePage nextPage = (HomePage)pageWithName("HomePage");
        if((type != null) && (type.equals("mybugs")) && (validateUser())) {
        //if((type != null) && (type.equals("mybugs"))) {
            nextPage.setIsMyBugs(true);
        }
        else if((type != null) && (type.equals("triage"))) {
            nextPage.setIsTriage(true);
        }
        else if((type != null) && (type.equals("release"))) {
            nextPage.setIsRelease(true);
        }
        else {
            nextPage.setIsProject(true);
        }

        if(session.homePage == null) {
            session.homePage = nextPage;
        }

        return nextPage;

    }
	*/
	public boolean validateUser() {
        boolean returnVal = false;
        String username = null;
        String password = null;
        WORequest request;
        EOEditingContext ec;
        NSArray<EOEnterpriseObject> potentialUsers;
        EOEnterpriseObject userObject;
        Session session;
        NSMutableArray<EOQualifier> qual = new NSMutableArray<EOQualifier>();
        EOQualifier qualifier;

        request =  this.request();
        session = (Session)session();
		
		if(session.getUser() == null) {
			username = request.cookieValueForKey("tracker_loginName");

			EOEnterpriseObject eo = (EOEnterpriseObject)session.getUser(); // check to see if session already exists.
																		   //System.out.println("username2 - " + username);
			if(eo != null) {
				returnVal = true;
			}
			else if(username != null) {
				password = request.cookieValueForKey("tracker_pw");
				if(password != null) {
					ec = session.defaultEditingContext();
					// Fetching a person with login and password will return only 1 record if it is a valid login.
					qualifier = EOQualifier.qualifierWithQualifierFormat("loginName = '" + username+"'", null);
					qual.addObject(qualifier);
				  // qualifier = EOQualifier.qualifierWithQualifierFormat("password = '" + password+"'", null);
				  // qual.addObject(qualifier);
					EOAndQualifier userQualifier = new EOAndQualifier(qual);
					EOFetchSpecification specification = new EOFetchSpecification("Profiles", userQualifier,null);
					specification.setIsDeep(false);
					// Perform actual fetch
					potentialUsers = (NSMutableArray)ec.objectsWithFetchSpecification(specification);
					if (potentialUsers.count() == 1) {
						returnVal = true;
						userObject = (EOEnterpriseObject)potentialUsers.lastObject();
						session.setUser(userObject);
						D2W.factory().setWebAssistantEnabled(false);  // check for Kevin and make him an admin
						setDefaultProject();
					}
				}
			}
			else {
				returnVal = false;
			}
		}
		else {
			//System.out.println("User already exists in session");
			returnVal = true;
		}
        return returnVal;
    }
	
    public void setDefaultProject() {
        WORequest request = this.request();
        String default_project = request.cookieValueForKey("default_project");
        //System.out.println("default_project2 - " + default_project);
        Session s = (Session)session();

        if(default_project != null) {
            if(s.potentialProjects().containsObject(default_project)) {
                s.takeValueForKey(default_project, "selectedProject");
            }
            else {
                s.takeValueForKey(s.selectedProject, "selectedProject");
            }
        }
    }
	/*
	public WOComponent supportItemsAction() {
        String project = null;
        String type = null;
        String status = null;
        WOComponent nextPage;
        EOQualifier qualifier=null;

        if(validateUser() == true) {
            WORequest request =  this.request();
            Session s = (Session)session();

            project = (String)request.formValueForKey("project");
            type = (String)request.formValueForKey("type");
            status = (String)request.formValueForKey("status");

            if(status.equals("open")) {
                //qualifier = EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED') and keywords like '*support*' and version='"+project + "'", null);
                qualifier = EOQualifier.qualifierWithQualifierFormat("(bugStatus='NEW' or bugStatus='ASSIGNED') and keywords.keywordName = 'support_escalation' and version='"+project + "'", null);
            }
            if(status.equals("resolved")) {
                qualifier = EOQualifier.qualifierWithQualifierFormat("bugStatus='RESOLVED' and keywords.keywordName = 'support_escalation' and version='"+project + "'", null);
            }
            if(status.equals("closed")) {
                qualifier = EOQualifier.qualifierWithQualifierFormat("bugStatus='CLOSED' and keywords.keywordName = 'support_escalation' and version='"+project + "'", null);
            }

            //nextPage = listPageForQualifier(qualifier, "Product", "State", false);
            nextPage = listPageForQualifier(qualifier, null, null, false, true);
        }
        else {
            nextPage= (WOComponent)pageWithName("Main");
        }

        return nextPage;
    }
    
    */
	
	public WOComponent supportEscalationQueueAction() {
		return escalationQueuePage("Support", false);
	}
	public WOComponent escalationQueueAction() {
		return escalationQueuePage("All", false);
	}

	public WOComponent escalationQueuePage(String pQueueType, boolean pIsEmail) {
		TopSustainingList listPage;
		listPage = (TopSustainingList)pageWithName("TopSustainingList");

		if(pQueueType.equals("All")) {
		  listPage.setSelectedQueues(new NSArray(new Object[] {"Support", "Ops", "MFG", "BizSystems"}));
		}
		else {
		  listPage.setSelectedQueues(new NSArray(new Object[] {pQueueType}));
		}
		if(pIsEmail==true) {
		    listPage.setEmailFlag(true);
		}
        return (WOComponent)listPage;
	}


	
	public WOComponent emailPageAction() {
		EmailPage em;
		WORequest request;
		String toAddress;
		String fromAddress;
		String pageName;
		String sprint;
		WOComponent tsp= null;
        StringTokenizer commas;
		int numTokens;
		
		request =  this.request();

		pageName = (String)request.formValueForKey("page");
		toAddress = (String)request.formValueForKey("email");
		fromAddress = (String)request.formValueForKey("from");
		sprint = (String)request.formValueForKey("sprint");
		if(fromAddress == null) {
			fromAddress = "kavoy@ironkey.com";
		}
		commas = new StringTokenizer(toAddress, ",");
        numTokens = commas.countTokens();
		
		em = new EmailPage(context());
		if(numTokens == 1) {
			em.setEmailAddress(toAddress);	
		}
		else {
			NSMutableArray emailAddresses = new NSMutableArray();
			while(commas.hasMoreElements()) {
				String nextAddress = (String)commas.nextToken();
				emailAddresses.addObject(nextAddress);
            }
			em.setToAddresses((NSArray)emailAddresses);	
		}
		
		em.setFrom(fromAddress);	

		/*
		if(pageName.equals("support")) {
			em.setSubject("Top Support Issues");	
			tsp = topSupportPage(true);
			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("supportServerEscalations")) {
			em.setSubject("Open Support Service/Server Escalations");	
			tsp = supportServerEscalationsPage(true);
			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		*/
		 if(pageName.equals("supportEscalationQueue")) {
			em.setSubject("Support Escalations");	
			tsp = escalationQueuePage("Support",true);
			
			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("topSupportTA")) {
			em.setSubject("Support Escalations - Trusted Access");	
			tsp =  (WOComponent)pageWithName("TopSupportIssuesList");

			tsp.takeValueForKey(new NSArray(new Object[] {"Trusted Access"}), "selectedQueues");
			tsp.takeValueForKey(new Integer(0), "emailFlag");

			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("topSupportSS")) {
			em.setSubject("Support Escalations - Secure Storage");	
			tsp =  (WOComponent)pageWithName("TopSupportIssuesList");

			tsp.takeValueForKey(new NSArray(new Object[] {"Secure Storage"}), "selectedQueues");
			tsp.takeValueForKey(new Integer(0), "emailFlag");

			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("topCustomerTA")) {
			em.setSubject("Top Customer Issues - Trusted Access");	
			tsp =  (WOComponent)pageWithName("TopCustomerIssuesList");

			tsp.takeValueForKey(new NSArray(new Object[] {"Trusted Access"}), "selectedQueues");
			tsp.takeValueForKey(new Integer(0), "emailFlag");

			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("topCustomerSS")) {
			em.setSubject("Top Customer Issues - Secure Storage");	
			tsp =  (WOComponent)pageWithName("TopCustomerIssuesList");

			tsp.takeValueForKey(new NSArray(new Object[] {"Secure Storage"}), "selectedQueues");
			tsp.takeValueForKey(new Integer(0), "emailFlag");

			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("releaseReport")) {
			em.setSubject("Release Report - Trusted Access");	
			tsp =  (WOComponent)pageWithName("ReleaseReport");

			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("ocm")) {
			em.setSubject("Ops Change Management");	
			tsp =  (WOComponent)pageWithName("Ops");
			if(tsp != null) {
				tsp.takeValueForKey(true, "emailFlag");
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		 /*
		else if(pageName.equals("openOcm")) {
			em.setSubject("Open OCM Report");	
			tsp = (WOComponent)pageWithName("Ops");
			if((tsp != null) && (((Ops)tsp).numOpen() > 0)) {
				tsp.takeValueForKey(true, "emailFlag");
				tsp.takeValueForKey(true, "onlyOpen");
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		*/
		else if(pageName.equals("taser")) {
			em.setSubject("Customer Issues Report");	
			tsp = (WOComponent)pageWithName("CustomerIssues");
			if(tsp != null) {
				tsp.takeValueForKey(true, "emailFlag");
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else if(pageName.equals("mustFix")) {
			String project = (String)request.formValueForKey("project");
			if(project == null) {
			   project = (String)session().valueForKey("selectedProject");
			}
			em.setSubject("Top Issues: Must Fix - " + project + " - Sprint: " + sprint.substring(1));	
			tsp = mustFixPage(project, sprint, true);
			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
		else {
			String project = (String)request.formValueForKey("project");
			if(project == null) {
			   project = (String)session().valueForKey("selectedProject");
			}
			em.setSubject("Top Issues: Hot Issues - " + project);	
			tsp = topIssuesPage(project, true);
			if(tsp != null) {
				em.setComponentToEmail(tsp);
				em.sendPage();
			}
		}
        return tsp;
    }
	

	public WOComponent topIssuesAction() {
		WORequest request;

		request =  this.request();
		String project = (String)request.formValueForKey("project");

		return topIssuesPage(project, false);
	}
	
	public WOComponent allowRequestAction() {
		WORequest request;

		request =  this.request();
		String user = (String)request.formValueForKey("user");
		String site = (String)request.formValueForKey("site");
		System.out.println("Need to make a call to IVR to have user enter DTMF of their pin - " + user + " - site: " + site);
		
		WOComponent nextPage = (WOComponent)pageWithName("OOBManager");
		nextPage.takeValueForKey(site, "site");
	
		return  nextPage;
	}
	

	public WOComponent topIssuesPage(String pProject, boolean pIsEmail) {
        EOFetchSpecification fs;
        NSDictionary bindings;
		EODatabaseDataSource _queryDataSource = null;
		WOComponent listPage = null;

		bindings = new NSDictionary(new Object[] { pProject}, new Object[] { "version"});
		fs = EOFetchSpecification.fetchSpecificationNamed( "topIssues", "Item").fetchSpecificationWithQualifierBindings( bindings );
        fs.setRefreshesRefetchedObjects(true);

        _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");
        _queryDataSource.setFetchSpecification(fs);

		WODisplayGroup displayGroup = new WODisplayGroup();
		displayGroup.setDataSource(_queryDataSource);
		displayGroup.fetch();
		
		if((displayGroup.allObjects().count() > 0) || (pIsEmail==false)){
			listPage = (WOComponent)pageWithName("TopIssuesList");
			listPage.takeValueForKey(displayGroup, "itemDisplayGroup");
			if(pIsEmail==true) {
				((TopIssuesList)listPage).setEmailFlag(true);
			}
		}
        return listPage;
	}
	public WOComponent mustFixPage(String pProject, String pSprint, boolean pIsEmail) {
        EOFetchSpecification fs;
        NSDictionary bindings;
		EODatabaseDataSource _queryDataSource = null;
		WOComponent listPage = null;

		bindings = new NSDictionary(new Object[] { pProject, pSprint}, new Object[] { "version", "sprint"});
		fs = EOFetchSpecification.fetchSpecificationNamed( "mustFix", "Item").fetchSpecificationWithQualifierBindings( bindings );
        fs.setRefreshesRefetchedObjects(true);

        _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");
        _queryDataSource.setFetchSpecification(fs);

		WODisplayGroup displayGroup = new WODisplayGroup();
		displayGroup.setDataSource(_queryDataSource);
		displayGroup.fetch();
		
		if((displayGroup.allObjects().count() > 0) || (pIsEmail==false)){
			listPage = (WOComponent)pageWithName("TopIssuesList");
			listPage.takeValueForKey(displayGroup, "itemDisplayGroup");
			listPage.takeValueForKey("Top Issues: Must Fix", "title");
			listPage.takeValueForKey("<b>" + pProject + " - Sprint: " + pSprint.substring(1) + "<br>P1 and P2 Bugs & Enhancements</b>", "queryDesc");
			if(pIsEmail==true) {
				((TopIssuesList)listPage).setEmailFlag(true);
			}
		}
        return listPage;
	}



	public WOResponse exportFindFixAction() {
		WORequest request;
		WOResponse response;
		WOComponent exportData;

		request =  this.request();
        SimpleDateFormat theFormat = new SimpleDateFormat("dd-MMM-yy");

        //response.setHeader("application/csv.ms-excel;", "Content-type");

		String project = (String)request.formValueForKey("project");
		FindFixReport ffr = (FindFixReport)pageWithName("FindFixReport");
        ffr.setSelectedProject(project);
        ffr.findFixOpenReport();
		exportData = (WOComponent)ffr.goExport();
		response = exportData.generateResponse();
		if(response != null) {
			response.setHeader("application/ms-excel;", "Content-type");
			response.setHeader("filename=\"trend_data-"+ theFormat.format(new Date()) + ".csv\"", "Content-Disposition");
        }

		return response;
	}

/*

	// To Do
	// Need to add a check to see if there are any outstanding Support issues before send email.
	public WOComponent emailTopSupportAction() {
		EmailPage em;
		String from;
		WORequest request;
		String toAddress;
		WOComponent tsp;
        StringTokenizer commas;
		int numTokens;
		
		from = "kavoy@ironkey.com";
		request =  this.request();
		toAddress = (String)request.formValueForKey("email");
		commas = new StringTokenizer(toAddress, ",");
        numTokens = commas.countTokens();

		// New EmailPage
		em = new EmailPage(context());
		
		// Set To addresses
		if(numTokens == 1) {
			em.setEmailAddress(toAddress);	
			//em.setToName("Kevin Avoy");
		}
		else {
			NSMutableArray emailAddresses = new NSMutableArray();
			while(commas.hasMoreElements()) {
				String nextAddress = (String)commas.nextToken();
				emailAddresses.addObject(nextAddress);
            }
			em.setToAddresses((NSArray)emailAddresses);	
		}
		
		// Set From	
		em.setFrom(from);	

		// Set Subject	
		em.setSubject("Top Support Issues");	
		
		// Set Body
		tsp = topSupportPage(true);
		em.setComponentToEmail(tsp);
		
		// Send Email
		em.sendPage();
		
        return tsp;

    }
	*/
	
	public WOComponent emailReleaseOverviewAction() {
		String from;
		WORequest request;
		String toAddress;

		from = "kavoy@ironkey.com";
		request =  this.request();
		toAddress = (String)request.formValueForKey("email");
		return emailComponentAction(toAddress, from, "Release Overview Report", pageWithName("ReleaseOverviewReport"));
	}
	
	public WOComponent emailFindFixAction() {
		String from;
		WORequest request;
		String toAddress;
		String project;
		FindFixReport nextPage;
		from = "kavoy@ironkey.com";
		
		request =  this.request();
		toAddress = (String)request.formValueForKey("email");
		project = (String)request.formValueForKey("project");

		nextPage = (FindFixReport)pageWithName("FindFixReport");
        nextPage.setSelectedProject(project);
        nextPage.findFixOpenReport();
		nextPage.takeValueForKey(true, "emailFlag");

		//return nextPage;
		return emailComponentAction(toAddress, from, "Find/Fix - " + project, (WOComponent)nextPage);

    }

	public WOComponent emailAction() {
			//System.out.println("emailAction()");

		WORequest request;
		EmailPage em;
		String toAddress;
		String from;
		String subject;
		String htmlContent;
		String plainText;

		request =  this.request();
			System.out.println("request - 1");
		
		toAddress = (String)request.formValueForKey("to");
		from = (String)request.formValueForKey("from");
		subject = (String)request.formValueForKey("subject");
		htmlContent = (String)request.formValueForKey("htmlContent");
		plainText = (String)request.formValueForKey("plainText");
		//http://zonker.local:2221/cgi-bin/WebObjects/Tracker.woa/wa/email?to=kavoy%40ironkey.com&from=kavoy%40ironkey.com&subject=this+is+the+subject&htmlContent=this+is+html
		
		System.out.println("toAddress - " + toAddress);
		System.out.println("from - " + from);
		System.out.println("subject - " + subject);
		System.out.println("htmlContent - " + htmlContent);
		System.out.println("plainText - " + plainText);

		if(from == null) {
		  from = "kavoy@ironkey.com";
		}
		if(subject == null) {
		  subject = "=== No Subject ===";
		}
		
		// New EmailPage
		em = new EmailPage(context());
		
		// Set To addresses
		em.setEmailAddress(toAddress);	
		//em.setToName("Kevin Avoy");
		
		// Set From	
		em.setFrom(from);	

		// Set Subject	
		em.setSubject(subject);	
		
		// Set Body
		if(htmlContent != null) {
			em.setHtmlContent(htmlContent);
		}
		if(plainText != null) {
			em.setPlainTextContent(plainText);
		}
		
		// Send Email
		em.sendPage();
		
		return null;
	}

	
	public WOComponent emailComponentAction(String pTo, String pFrom, String pSubject, WOComponent pComponent) {
		EmailPage em;

		if(pFrom == null) {
		  pFrom = "kavoy@ironkey.com";
		}
		if(pSubject == null) {
		  pSubject = "=== Tracker Report ===";
		}
		
		// New EmailPage
		em = new EmailPage(context());
		
		// Set To addresses
		em.setEmailAddress(pTo);	
		//em.setToName("Kevin Avoy");
		
		// Set From	
		em.setFrom(pFrom);	

		// Set Subject	
		em.setSubject(pSubject);	
		
		// Set Body
		em.setComponentToEmail(pComponent);
		
		// Send Email
		em.sendPage();
		
		
        return pComponent;

    }
	
    public WOComponent listItemsAction() {
        WORequest request =  this.request();
        NSMutableArray qual = new NSMutableArray();
        //EOQualifier qualifier;
        String requestValues = null;
        StringTokenizer tokens;
        NSArray params = new NSArray(new Object[] {"version", "status", "product.productName", "product","keywords","keyword", "bugId","milestone","priority", "vertical.componentName", "vertical", "component", "type", "severity"});
        NSMutableArray tempQualifier;
        String paramName;
        // project = (String)request.formValueForKey("project");

        Enumeration enumer = params.objectEnumerator();
        while(enumer.hasMoreElements()) {
            String param = (String)enumer.nextElement();

            requestValues = (String)request.formValueForKey(param);
            if(requestValues != null){
                if(param.equals("status")) {
                    paramName = "bugStatus";
                }
                else if(param.equals("product")) {
                    paramName = "product.productName";
                }
                else if(param.equals("component")) {
                    paramName = "component.componentName";
                }
                else if(param.equals("milestone")) {
                    paramName = "targetMilestone";
                }
                else if(param.equals("severity")) {
                    paramName = "bugSeverity";
                }
                else if(param.equals("keyword")) {
                    paramName = "keywords";
                }
                else {
                    paramName = param;
                }
                qual.addObject(createQualifierForValues(paramName, requestValues));
            }
        }

        EOAndQualifier aQualifier = new EOAndQualifier(qual);
        //specification.setIsDeep(false);
        String row = (String)request.formValueForKey("row");
        String column = (String)request.formValueForKey("column");
        String qa = (String)request.formValueForKey("qa");
        // Do they want the page wrapped with PageWrapper?
        String nowrap = (String)request.formValueForKey("nowrap");
        boolean pageWrapper = true;
        if(nowrap != null) {
            pageWrapper = false;
        }
        if(qa != null) {
            return listPageForQualifier(aQualifier, row, column, true, pageWrapper);
        }
        else {
            return listPageForQualifier(aQualifier, row, column, false, pageWrapper);
        }
    }
	
	
    public WOComponent jsonListAction() {
        WORequest request =  this.request();
        NSMutableArray qual = new NSMutableArray();
        //EOQualifier qualifier;
        String requestValues = null;
		String sortValue;
        StringTokenizer tokens;
        NSArray params = new NSArray(new Object[] {"version","hot", "status", "product","keywords","keyword", "bugId","milestone","priority", "vertical", "component", "type", "severity"});
        NSMutableArray tempQualifier;
        String paramName;
        // project = (String)request.formValueForKey("project");

        Enumeration enumer = params.objectEnumerator();
        while(enumer.hasMoreElements()) {
            String param = (String)enumer.nextElement();

            requestValues = (String)request.formValueForKey(param);
            if(requestValues != null){
                if(param.equals("status")) {
                    paramName = "bugStatus";
                }
                else if(param.equals("product")) {
                    paramName = "product.productName";
                }
                else if(param.equals("vertical")) {
                    paramName = "vertical.componentName";
                }
                else if(param.equals("milestone")) {
                    paramName = "targetMilestone";
                }
                else if(param.equals("severity")) {
                    paramName = "bugSeverity";
                }
                else if(param.equals("keyword")) {
                    paramName = "keywords";
                }
                else {
                    paramName = param;
                }
                qual.addObject(createQualifierForValues(paramName, requestValues));
            }
        }
		
		NSMutableArray orderings = null;
		// Set the sortOrder
		//sort=status,product,vertical
		sortValue = (String)request.formValueForKey("sort");
		if(sortValue != null) {
			StringTokenizer token2 = new StringTokenizer(sortValue, ",");
			orderings = new NSMutableArray();
			String sortName = "";
			//System.out.println("sortOrderValue - " + sortOrderValue);
			while(token2.hasMoreElements()) {
				boolean descFlag = false;
				String ascDesc = "";
				String sortItem = token2.nextToken();
				
				// need to tokenize for sort order
				// sort=status:asc,product:desc,vertical - items will have logical default sort
				StringTokenizer tokenAscDesc = new StringTokenizer(sortItem, ":");
				if(tokenAscDesc.countTokens() == 2) {
					sortItem = (String)tokenAscDesc.nextToken();
					ascDesc = ((String)tokenAscDesc.nextToken()).toLowerCase();
				}
				
				
				if(sortItem.equals("status")) {
					sortName = "bugStatus";
				}
				else if(sortItem.equals("product")) {
					sortName = "product.productName";
				}
				else if(sortItem.equals("vertical")) {
					sortName = "vertical.componentName";
				}
				else if(sortItem.equals("milestone")) {
					sortName = "targetMilestone";
				}
				else if(sortItem.equals("severity")) {
					sortName = "bugSeverity";
				}
				else if(sortItem.equals("keyword")) {
					sortName = "keywords.keywordName";
				}
				else if(sortItem.equals("hot")) {
					if(ascDesc.equals("")) {
						ascDesc = "desc";  // default to desc 
					}
					sortName = sortItem;
				}
				else if(sortItem.equals("created")) {
					if(ascDesc.equals("")) {
						ascDesc = "desc";  // default to desc
					}
					sortName = "creationTs";
				}
				else if(sortItem.equals("modified")) {
					if(ascDesc.equals("")) {
						ascDesc = "desc";  // default to desc 
					}
					sortName = "lastdiffed";
				}
				else {
				   sortName = sortItem;
				}
				
				if(ascDesc.equals("desc")) {
					orderings.addObject(EOSortOrdering.sortOrderingWithKey(sortName, EOSortOrdering.CompareDescending));
				}
				else {
					orderings.addObject(EOSortOrdering.sortOrderingWithKey(sortName, EOSortOrdering.CompareAscending));
				}
			}
		}
 
		// This returns a new array in sorted order. 
		//sorted=EOSortOrdering.sortedArrayUsingKeyOrderArray(projects(), new NSArray(orderings)); 

		return jsonPageForQualifier(new EOAndQualifier(qual), (NSArray)orderings);
    }


	
	public WOComponent listForMilestoneInProjectAction() {
        WOComponent nextPage = null;
        if(validateUser() == true) {
            Session session = (Session)session();
            WORequest request =  this.request();

            String milestone  = (String)request.formValueForKey("milestone");
            String project  = (String)request.formValueForKey("project");
            String status  = (String)request.formValueForKey("status");

            if(status.equals("open")) {
                return listPageForSQL("(bugStatus='NEW' or bugStatus='ASSIGNED' or bugStatus='REOPENED') and targetMilestone='" + milestone +"' and version='"+ project+ "' and  (type <> 'Customer' or type <> 'Prospect')");
            }
            else if(status.equals("resolved")) {
                return listPageForSQL("(bugStatus='RESOLVED') and targetMilestone='" + milestone +"' and version='"+ project+ "' and (type <> 'Customer' or type <> 'Prospect')");
            }
            else if(status.equals("closed")) {
                return listPageForSQL("(bugStatus='CLOSED' or bugStatus='VERIFIED') and targetMilestone='" + milestone +"' and version='"+ project+ "' and  (type <> 'Customer' or type <> 'Prospect')");
            }
            else if(status.equals("total")) {
                return listPageForSQL("targetMilestone='" + milestone +"' and version='"+ project+ "' and  (type <> 'Customer' or type <> 'Prospect')");
            }
        }
        return pageWithName("Main");
    }

    WOComponent listPageForSQL(String pStatement) {
        EOQualifier qualifier = null;
        qualifier = EOQualifier.qualifierWithQualifierFormat(pStatement, null);
        return listPageForQualifier(qualifier,null, null, false, true);

    }


    public EOQualifier createQualifierForValues(String pAttribute, String values) {
        EOQualifier qualifier;
        StringTokenizer tokens = new StringTokenizer(values, ",");
        NSMutableArray tempQualifier = new NSMutableArray();
        //System.out.println("values - " + values);
        while(tokens.hasMoreElements()) {
            if(pAttribute.equals("keywords")) {
                qualifier = EOQualifier.qualifierWithQualifierFormat(pAttribute + " = '" + tokens.nextToken()+"'", null);
            }
            else if(pAttribute.equals("bugId")) {
                qualifier = EOQualifier.qualifierWithQualifierFormat(pAttribute + " = " + tokens.nextToken(), null);
            }
            else {
                qualifier = EOQualifier.qualifierWithQualifierFormat(pAttribute + " = '" + tokens.nextToken()+"'", null);
            }
            tempQualifier.addObject(qualifier);
        }
        return new EOOrQualifier(tempQualifier);
    }
	
    public WOComponent listPageForQualifier(EOQualifier pQualifier, String pRow, String pColumn, boolean pQA , boolean pIsWrapped) {
        EODatabaseDataSource _queryDataSource = null;
        EOFetchSpecification fs = null;

        fs = new EOFetchSpecification("Item", pQualifier,null);
        fs.setRefreshesRefetchedObjects(true);

        _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");

        _queryDataSource.setFetchSpecification(fs);
		WOComponent listPage = (WOComponent)pageWithName("ReportListWrapper");
		WODisplayGroup displayGroup = new WODisplayGroup();
		displayGroup.setDataSource(_queryDataSource);
		displayGroup.fetch();
		listPage.takeValueForKey(displayGroup, "displayGroup");

		
       // ListBugs listPage=(ListBugs)D2W.factory().pageForConfigurationNamed("ListMyBugs",session());
        //listPage.setDataSource(_queryDataSource);
        if((pRow != null) || (pColumn != null)) {
            ((ReportListWrapper)listPage).setIsList(false);
            ((ReportListWrapper)listPage).setIsReport(true);
            if(pRow != null) {
                ((ReportListWrapper)listPage).setARow(pRow);
            }
            if(pColumn != null) {
                ((ReportListWrapper)listPage).setAColumn(pColumn);
            }
        }
     /*  if(pQA == true) {
            listPage.setQaContact(true);
        }
        listPage.setIsWrapped(pIsWrapped);
        if(pIsWrapped == false) {
            listPage.setHideController(true);
            listPage.setHideGraphLink(true);
            listPage.setIsToolBar(false);
            listPage.setIsReportHeader(false);
        }
		*/

        return listPage;

    }    
    public WOComponent jsonPageForQualifier(EOQualifier pQualifier, NSArray pOrderings) {
        EODatabaseDataSource _queryDataSource = null;
        EOFetchSpecification fs = null;

        fs = new EOFetchSpecification("Item", pQualifier,pOrderings);
        fs.setRefreshesRefetchedObjects(true);

        _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");

        _queryDataSource.setFetchSpecification(fs);
		WOComponent listPage = (WOComponent)pageWithName("JsonList");
		WODisplayGroup displayGroup = new WODisplayGroup();
		displayGroup.setDataSource(_queryDataSource);
		displayGroup.fetch();
		listPage.takeValueForKey(displayGroup, "displayGroup");

        return listPage;

    }    
	
	public NSArray bugsAndEnhancementsForIggy() {
        EOFetchSpecification fs;
        Session s = (Session)session();
        NSMutableArray qual = new NSMutableArray();
        NSArray bugs;

		//Bugs and Enhancements
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("(type ='Bug' or type='Enhancement')", null));
		qual.addObject(EOQualifier.qualifierWithQualifierFormat("version='Iggy (services)' ", null));
		//qual.addObject(EOQualifier.qualifierWithQualifierFormat("component.componentName='update.ironkey.com' ", null));

		Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("bugId", EOSortOrdering.CompareCaseInsensitiveAscending),
		};

		fs = new EOFetchSpecification("Item", new EOAndQualifier(qual), new NSArray(orderings));
		fs.setRefreshesRefetchedObjects(true);
		bugs = (NSArray)s.defaultEditingContext().objectsWithFetchSpecification(fs);

		return bugs;
    }

	
	public EOEnterpriseObject createEO(String entityName, EOEditingContext pEC) {
        EOEnterpriseObject anEO;
        EOClassDescription theClassDesc;

        // create an instance
        theClassDesc = EOClassDescription.classDescriptionForEntityName(entityName);
        anEO = (EOEnterpriseObject) theClassDesc.createInstanceWithEditingContext(pEC, null);
        return anEO;
    }

    public WOComponent moveIggyBugsToKiaAction() {
		NSArray myBugs = bugsAndEnhancementsForIggy();
		System.out.println("count - " + myBugs.count());
		Enumeration enumer = myBugs.objectEnumerator();
		NSTimestamp now = new NSTimestamp();

		while(enumer.hasMoreElements()) {
			Item anItem = (Item)enumer.nextElement();
			anItem.takeValueForKey("Kia (TA 3.0 Software Only)", "version");
			
			EOEnterpriseObject longDesc = createEO("Longdescs", session().defaultEditingContext());
			longDesc.takeValueForKey(new Integer(26), "who");
			longDesc.takeValueForKey(now, "bugWhen");
			longDesc.takeValueForKey(new Double(0.0), "workTime");
			longDesc.takeValueForKey("Moved from Iggy", "thetext");  // set a comment
			longDesc.takeValueForKey(new Integer(0), "isprivate");
			longDesc.takeValueForKey(new Integer(0), "alreadyWrapped");
			longDesc.takeValueForKey(new Integer(0), "descType");

			session().defaultEditingContext().insertObject(longDesc);
			anItem.addObjectToBothSidesOfRelationshipWithKey(longDesc, "descriptions");
		}
		try {
			session().defaultEditingContext().saveChanges();
		} 
		catch (Exception exception) {
			System.err.println( " Could not save your changes: " + exception.getMessage());
		}

        return null;
    }
}
/* already commented out
public WOComponent supportServerEscalationsAction() {
	return supportServerEscalationsPage(false);
}

public WOComponent supportServerEscalationsPage(boolean pIsEmail) {
    EOFetchSpecification fs;
    NSDictionary bindings;
	EODatabaseDataSource _queryDataSource = null;
	TopSupportIssuesList listPage;
    //Session s = (Session)session();

	fs = EOFetchSpecification.fetchSpecificationNamed( "supportServiceEscalations", "Item").fetchSpecificationWithQualifierBindings( null );
    fs.setRefreshesRefetchedObjects(true);

    _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");
    _queryDataSource.setFetchSpecification(fs);

	WODisplayGroup displayGroup = new WODisplayGroup();
	displayGroup.setDataSource(_queryDataSource);
	displayGroup.fetch();
	
	listPage = (TopSupportIssuesList)pageWithName("TopSupportIssuesList");
	listPage.takeValueForKey(displayGroup, "itemDisplayGroup");
	listPage.takeValueForKey("Support Service/Server Escalations","header");
	listPage.takeValueForKey("Open bugs that have been assigned to:","description1");
	listPage.takeValueForKey("'serviceescalation' or 'serverescalation'","description2");
	if(pIsEmail==true) {
	    listPage.setEmailFlag(true);
	}
    return (WOComponent)listPage;
}


public EmailPage emailPageUIAction() {
	EmailPage em;
	WORequest request;
	String toAddress;
	String fromAddress;
	String pageName;
	WOComponent tsp= null;
    StringTokenizer commas;

	request =  this.request();

	pageName = (String)request.formValueForKey("page");
	em =  (EmailPage)pageWithName("EmailPage");


}
*/

/* All of this was already commented out

public WOComponent topSupportAction() {
return topSupportPage(false);
}

public WOComponent topSupportPage(boolean pIsEmail) {
EOFetchSpecification fs;
NSDictionary bindings;
EODatabaseDataSource _queryDataSource = null;
TopSupportIssuesList listPage;
//Session s = (Session)session();

fs = EOFetchSpecification.fetchSpecificationNamed( "topSupport", "Item").fetchSpecificationWithQualifierBindings( null );
fs.setRefreshesRefetchedObjects(true);

_queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Item");
_queryDataSource.setFetchSpecification(fs);

WODisplayGroup displayGroup = new WODisplayGroup();
displayGroup.setDataSource(_queryDataSource);
displayGroup.fetch();

listPage = (TopSupportIssuesList)pageWithName("TopSupportIssuesList");
listPage.takeValueForKey(displayGroup, "itemDisplayGroup");
listPage.takeValueForKey("Top Support Issues","header");
listPage.takeValueForKey("Open Bugs with 'topsupport' keyword","description1");
listPage.takeValueForKey("all releases","description2");
if(pIsEmail==true) {
    listPage.setEmailFlag(true);
}
return (WOComponent)listPage;
}
*/

