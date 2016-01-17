package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Enumeration;
import com.webobjects.directtoweb.*;
import er.javamail.*;

// Todo:
// 1) Handle error when POST fails
// 2) Handle error when email fails
// 3) Dynamically add versions
// 4) [DONE]Add Email support
// 5) [mostly DONE]Add different email addresses for different types/priorities
// 6) [DONE]Dynamically pull components based on Type
// 7) [mostly DONE] Link to the SFDC ticket 
// 8) Add support for Product: Basic, Personal, Enterprise
//   - build, OS
// 9) [DONE] add keyword support: support_escalation
// 10) [mostly DONE]Add CC support
// 11) Add support for Opening the page with data defined from GET (see 'e' below)
// 12) reported by: cookies
//a) Identify notification rules (James)
//b) drop down for OS and build id (Kevin)
//c) all cc multiselect list  (Kevin)
//d) No case for SFDC (NA)
//e) Interface for prepopulating data (Kevin)

public class Escalate extends WOComponent {
	private static final long serialVersionUID = 1L;
    public String ticketNumber;
    protected WOComponent nextPage;
    protected WOComponent cancelPage;
    public NSArray potentialComponents;
    public String selectedComponent = "Other";
    public NSArray potentialProducts;
    public String aType;
    public String selectedRepPlatform = "Unknown";
    public String selectedProduct = "Client: Trusted Access - Desktop";
    public NSArray potentialPriorities;
    public String selectedPriority = "3 - Medium";
    public NSArray potentialSeverities;
    public String selectedModel = "Personal";
    public NSArray potentialModels;
    public String selectedSeverity = "3 - Normal";
    public NSArray potentialVersions;
    public String selectedVersion = "Kia-patch-3 (TA 3.0.3)";
    public NSArray potentialOSes;
    public NSArray profiles;
    public NSArray selectedCC;
    /** @TypeInfo Profiles */	
    public EOEnterpriseObject aProfile;	
    public String anItem;	
    public String aString;
    public String os = "XP";
    public String build;
    public String selectedMode;
    public NSArray potentialModes;	
    public String selectedItemType = "Bug";
    public NSArray potentialItemTypes;	
    public String theSummary;
    public String comments;  // whiteboard
    public String theDetails;
    protected EOEditingContext ec;
    //protected boolean typeChanged = false;
    public String errorMessage = "";
    protected int bug_id = 0;
    protected static final String POST_REQUEST_PATH = "/post_bug.cgi"; //"/bugzilla-test/post_bug.cgi"
                                                                       //protected String ESCALATION_EMAIL_ADDRESS = "kavoy@marblesecurity.com"; // kta
    protected String ESCALATION_EMAIL_ADDRESS =  "kavoy@marblesecurity.com"; //sustaining_engineering
    protected static final String PRODUCTION_VERSION_NUMBER = "Dolomites (Patch7)";
    protected static final boolean SEND_EMAIL = true;
    protected String bugzillaLogin;
    protected String bugzillaLoginCookie;
	protected String bugzillaId;
	protected String bugzillaPassword;
	protected String token;	
	public String hot = "false";


    public Escalate(WOContext aContext) {
        super(aContext);
        //System.out.println("Escalate.Escalate()") ;

       // Object types[] = {"Defect","Enhancement", "Ops Request"};
        //Object products[] = {"Client: TA - Desktop", "Client: TA - Mobile", "Client: TA - Mac","Device (TA)", "Device (SS)","Service","Server", "OCM"};
        //potentialProducts = new NSArray(types);


        Object priority[] = {"1 - Urgent","2 - High", "3 - Medium", "4 - Low", "5 - Very Low"};  
        potentialPriorities = new NSArray(priority);
        Object models[] = {"Basic","Personal", "Enterprise"};  
        potentialModels = new NSArray(models);
        Object modes[] = {"Nonadmin","Admin"};  
        potentialModes = new NSArray(modes);
        Object oses[] = {"W2K","XP","Vista","Win7","MacOS X","Linux"};  
        potentialOSes = new NSArray(oses);
		
        Object item_types[] = {"Bug", "Enhancement"};  
        potentialItemTypes = new NSArray(item_types);
		
        Object severities[] = {"0 - Blocker","1 - Critical", "2 - Major", "3 - Normal", "4 - Minor", "5 - Trivial"};  // these should be read from db
        potentialSeverities = new NSArray(severities);
        ec = new EOEditingContext(); 
		getCookieInfo();

    }

    public void awake() {
        errorMessage = "";
    }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
        // Determine if 'type' reset
		/*
        System.out.println("Escalate.takeValuesFromRequest()") ;
		NSDictionary theDict = request.formValues();
		Enumeration enum1 = theDict.keyEnumerator();
		while(enum1.hasMoreElements()) {
            String theKey = (String)enum1.nextElement();
            System.out.println("theKey = " + theKey + " = " + theDict.objectForKey(theKey));
		}
		*/
		
		//System.out.println("type = " + (String)request.formValueForKey("type"));
		int newType = Integer.parseInt((String)request.formValueForKey("product"));
        int currentType = potentialProducts().indexOfObject(selectedProduct);
        if(newType == currentType) {
            super.takeValuesFromRequest(request, context);
        }
        else {  // Type was reset
            super.takeValuesFromRequest(request, context);
            setSelectedProduct((String)potentialProducts().objectAtIndex(newType));
			setSelectedComponent("Other");

        }
		//System.out.println("selectedProduct - " + selectedProduct);
    }

    public WOComponent submitEscalation() {
        //System.out.println("Escalate.submitEscalation()");
        WOComponent returnVal = null;
        //if(typeChanged == false) {
        boolean postSuccess = postBugzilla(); 

        if(postSuccess == true) {
            if(SEND_EMAIL == true) {
                emailPage();
            }
			// which page to return
        }
        else {
			if(!errorMessage.equals("")) {
				errorMessage = "Error posting issue to Bugzilla...";
			}
            System.err.println("Error posting issue to Bugzilla");
        }
        return returnVal;
	}
    public String bugzillaLogin() {
        if(bugzillaLogin == null) {
            if(bugzillaId != null) {
				HTTPConnection conn = null;
				//    protected static final String POST_REQUEST_PATH = "/post_bug.cgi"; //"/bugzilla-test/post_bug.cgi"
				String theData = "";
				try {
					theData += "Bugzilla_login=" + URLEncoder.encode(bugzillaId, "UTF-8");
					theData += "&Bugzilla_password="+ URLEncoder.encode(bugzillaPassword, "UTF-8");
					//theData += "&GoAheadAndLogIn=1";
					theData += "&GoAheadAndLogIn=log_in";
					String bugzillaPort = ((Application)Application.application()).bugzillaProtocol().equals("https")?"443":"80";

					conn = new HTTPConnection();
					//conn.setDebugFlag(true);
					conn.setRequestMethod("POST");
					conn.setPostData(theData);
					conn.setHost(((Application)Application.application()).bugzillaHost());
					conn.setProtocol(((Application)Application.application()).bugzillaProtocol());
					conn.setPort(bugzillaPort);
					conn.setRequestPath("/enter_bug.cgi?product="  + URLEncoder.encode(selectedProduct));
					conn.connect();

					bugzillaLogin = searchForString("Bugzilla_login=", ";", conn.response());
					bugzillaLoginCookie = searchForString("Bugzilla_logincookie=", ";", conn.response());
					token = searchForString("\"token\" value=\"", "\"", conn.response());
					if((bugzillaLogin == null) || (bugzillaLoginCookie == null)) {
						bugzillaLogin = null;
						bugzillaLoginCookie = null;
						errorMessage = "Invalid Username Or Password";
					    System.err.println( "Invalid Username Or Password");
						
					}

				}
				catch(Exception e) {
					System.err.println("Escalate.bugzillaLogin - e - " + e);
					System.err.println("conn.response() - " + conn.response());

				}
                
            }
			else {
				System.err.println("BugzillaId is null");
				errorMessage = ("BugzillaId is null - no userId cookie set");				
			}
        }
        return bugzillaLogin;

    }

    public boolean postBugzilla() {
        //System.out.println("Escalate.postBugzilla()");
        HTTPConnection conn = null;
        String theData = "";
        String response = null;
        boolean returnVal = false;

		// Get agents login credentials
		if(bugzillaLogin() != null) {

			try {
				theData += "product=" + URLEncoder.encode(selectedProduct, "UTF-8");
				//theData += "&version=" + URLEncoder.encode(PRODUCTION_VERSION_NUMBER);selectedVersion
				theData += "&version=" + URLEncoder.encode(selectedVersion, "UTF-8");
				theData += "&component=" +  URLEncoder.encode(selectedComponent, "UTF-8");
				theData += "&rep_platform=" + URLEncoder.encode(selectedRepPlatform, "UTF-8");
				theData += "&op_sys=Unknown";
				theData += "&priority=" + URLEncoder.encode(selectedPriority, "UTF-8");
				theData += "&bug_severity=" + URLEncoder.encode(selectedSeverity, "UTF-8");
				theData += "&bug_status=NEW";
				
				if(isHot() == true) {
					theData += "&cf_hot=true";
				}

				if(selectedProduct.equals("OCM")) {
					theData += "&assigned_to=ocm%40marblesecurity.com";
					selectedItemType = "OCM";
				}
				else {
					theData += "&assigned_to=bugmaster%40marblesecurity.com";
				}
				theData += "&cf_type=" + selectedItemType;

				//theData += "&cc=support%40ironkey.com";
				String cc = ccString();
				if(cc != null) {
				//theData += "&cc=iksupport%40ironkey.com\nkavoy%40marblesecurity.com";
					theData += "&cc=" + URLEncoder.encode(cc);
				}
				//theData += "&estimated_time=";
				if(ticketNumber != null) {
					//theData += "&bug_file_loc=" +"http://salesforce.com?ticket=" + ticketNumber;
				}

				// theData += "&bug_file_loc=";
				if(theSummary == null) {
					theSummary = "Default Summary";
				}
				if(os == null) { os = "";
				}
				if(build == null) { build = "";
				}
				if(selectedMode == null) { selectedMode = "";
				}
				if(selectedProduct.equals("OCM")) { theSummary = "OCM: "+ theSummary;
				}

				theData += "&short_desc="+ URLEncoder.encode(theSummary);
				if(isSS()) {
				   theData += "&comment="+ URLEncoder.encode(theSummary+"\n\nProduct: " +selectedModel+ "\nOS: "+os+"\nBuild: "+build+"\nMode: "+selectedMode+"\n\n"+theDetails);
				}
				else{
				   theData += "&comment="+ URLEncoder.encode(theSummary+"\n\n" + "OS: "+os+"\nBuild: "+build+"\nMode: "+selectedMode+"\n\n"+theDetails);
				}

				if(comments != null) {
					theData += "&status_whiteboard="+ URLEncoder.encode(comments);
				}
				theData += "&commentprivacy=0";
				theData += "&keywords=support_escalation";  // this puts the value in the bug but does not add to keywords table (see below)
				theData += "&dependson=";
				theData += "&blocked=";
				theData += "&form_name=enter_bug";
				theData += "&token="+token;
				String bugzillaPort = ((Application)Application.application()).bugzillaProtocol().equals("https")?"443":"80";

				conn = new HTTPConnection();
				//conn.setDebugFlag(true);
				conn.setRequestMethod("POST");
				conn.setPostData(theData);
				conn.setHost(((Application)Application.application()).bugzillaHost());
				conn.setProtocol(((Application)Application.application()).bugzillaProtocol());
				conn.setPort(bugzillaPort);
				conn.addCookie("Bugzilla_login=" + bugzillaLogin() + "; Bugzilla_logincookie=" +bugzillaLoginCookie +"; VERSION-Defects=Jaipur" );
				conn.setRequestPath(POST_REQUEST_PATH);
				
				conn.connect();

				// Parse response for the bugID    //<TABLE BORDER=1><TD><H2>Bug 13010 posted</H2>
				String bugId = searchForString("<title>Bug ", " Submitted &ndash; ", conn.response());
				if(bugId != null) {
				  bug_id = Integer.parseInt(bugId);

				  // Need to insert the 'support' keyword
				  /*
				  EOEnterpriseObject keyword = createEO("Keywords");
				  keyword.takeValueForKey(new Integer(bug_id), "bugId");
				  keyword.takeValueForKey(new Integer(25), "keywordid");  // 7 is the id for the support_escalation keyword
				  ec.insertObject(keyword);
				  ec.saveChanges();
					*/
				  returnVal = true;
				}
				else {
				  // not bug id
				  System.err.println("conn.response() - " + conn.response());
				}
			}
			catch(Exception e) {
				System.err.println("Escalate.postData - e - " + e);
				System.err.println("conn.response() - " + conn.response());
			}
		}
		else {
			System.err.println("BugzillaLogin() was null");
		}

        return returnVal;
    }

	public String ccString() {
		String returnVal = null;
		int count = selectedCC.count();
		
		if(count > 0 )  {
			returnVal = "";
			for (int i = 0; i < count; i++) {
				EOEnterpriseObject anEO = (EOEnterpriseObject)selectedCC.objectAtIndex(i);
				returnVal += (String)anEO.valueForKey("loginName") + ",";
			
			}
		}
		return returnVal;
	}
    public void getCookieInfo() {
        WOContext context = context();
        WORequest request = context.request();
		/*
        NSDictionary cookies = request.cookieValues();
        Enumeration enumer = cookies.keyEnumerator();
        while(enumer.hasMoreElements()) {
		  String key = (String)enumer.nextElement();
		  System.out.println("key - " + key + "  / Value - " + cookies.valueForKey(key));
        }
		*/
        String user = request.cookieValueForKey("tracker_loginName");
        if(user != null) {
           bugzillaId = user;
			String pw = request.cookieValueForKey("tracker_pw");
			
			if(pw != null) {
			   bugzillaPassword = pw;
			}
        }
		else {  // Default to generic support account
			bugzillaId = "iksupport@marblesecurity.com";
			bugzillaPassword = "ironkey";
		}
    }

    public NSArray toEmailAddress() {
        //System.out.println("Escalate.toEmailAddress()") ;
        NSMutableArray to = new NSMutableArray(); 

		// Need to add support for Imation (SS) VS IronKey (TA)
		
        //if((selectedPriority.equals("1 - Urgent")) && ((selectedType.equals("Device")) || (selectedType.equals("Enhancement")))) {
        if((isHot() == true) || (((selectedPriority.equals("1 - Urgent")) && (selectedSeverity.equals("0 - Blocker") || selectedSeverity.equals("1 - Critical"))))) {
            to.addObject("critical-support-escalation@marblesecurity.com");
            to.addObject("kavoy@marblesecurity.com");
        }
        else {
           to.addObject("noncritical-support-escalation@marblesecurity.com");
           to.addObject("kavoy@marblesecurity.com");
        }

        return (NSArray)to;
    }

  	public NSArray potentialComponents() {
		String sqlString;
		Session s;
		NSMutableArray temp;
		s = (Session)session();
		sqlString = "select name from components where product_id in (" + idForProduct(selectedProduct) + ") order by name";
		            
					
		NSMutableArray tempTypes = new NSMutableArray(s.potentialProducts());
		temp =  (NSMutableArray)s.rowsForSql(sqlString);
		
		potentialComponents = new NSArray(temp);

		return potentialComponents;
	}
 
	public NSArray potentialProducts() {
        if(potentialProducts == null) {
            Session s = (Session)session();
            NSMutableArray tempTypes = new NSMutableArray(s.potentialProducts());
			tempTypes.removeObject("_Trusted Access-Mac (Banking)");
			tempTypes.removeObject("_Internal Business Systems");
			tempTypes.removeObject("IronKey Server");
			tempTypes.removeObject("IronKey Services");
			tempTypes.removeObject("Imation-Services");
			tempTypes.removeObject("_Marketing Sites");
			tempTypes.removeObject("_Tools");
			tempTypes.removeObject("_Work Product");
			tempTypes.removeObject("_Risk Analytics Platform");
			tempTypes.removeObject("_Build_System");
			tempTypes.removeObject("__Device: El Camino");
			tempTypes.removeObject("_Device: Secure Storage");
			tempTypes.removeObject("__SDK");
			tempTypes.removeObject("__Special Projects");
            potentialProducts = new NSArray(tempTypes);
        }
        return potentialProducts;
    }


	public NSArray potentialVersions() {
		String sqlString;
		Session s;
		NSMutableArray temp;
		s = (Session)session();
		sqlString = "select distinct(value) from versions where product_id in (" + idForProduct(selectedProduct) + ") and value not like '\\_%' order by value";
		            
		temp =  (NSMutableArray)s.rowsForSql(sqlString);
		if((selectedProduct != null) && (selectedProduct.contains("Desktop"))) {
			temp.removeObject("Release 1 (TAE R1)");
			temp.removeObject("Release 2 (TAE R2)");
			temp.removeObject("Release 2.1 (TAE R2.1)");
		}
		else if((selectedProduct != null) && (selectedProduct.contains("Mobile"))) {
			temp.removeObject("Mustang (TA-Mobile 1.0)");
			temp.removeObject("Nomad (TA-Mobile 1.1)");
		}
		else if((selectedProduct != null) && (selectedProduct.contains("Storage"))) {
			temp.removeObject("Guns-n-Roses (Q2_Sustaining)");
			temp.removeObject("Hendrix H2-1");
			temp.removeObject("Inglefield (SS)");
			temp.removeObject("PKI (2.0)");
		}

		
		potentialVersions = new NSArray(temp);

		setSelectedVersion((String)potentialVersions.lastObject());
		return potentialVersions;
	}
	
	public int idForProduct(String pProductName) {
		//System.out.println("\n\n============ Escalate.idForProduct()");

		int returnVal = -1;
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select id from products where name='"+ pProductName+ "'");
		if(values.count() > 0) {
			Short val = (Short)values.objectAtIndex(0);
			returnVal =  val.intValue();
		}
		return returnVal;
    }


    public void emailPage() {
        //System.out.println("Escalate.emailPage() ");

        String htmlMessage;
        //Object tos[]={toEmailAddress()};
        //NSArray to = new NSArray(tos);
        NSArray to = toEmailAddress();
        String from = "kavoy@marblesecurity.com";
		String extra = "";

        WOMailDelivery mailDelivery = WOMailDelivery.sharedInstance();
        EscalationEmail nextPage = (EscalationEmail)WOApplication.application().pageWithName("EscalationEmail",context());
        nextPage.takeValueForKey(selectedSeverity,"severity");
        nextPage.takeValueForKey(selectedPriority,"priority");
        nextPage.takeValueForKey("" + bug_id,"bugNumber");
        nextPage.takeValueForKey(theSummary,"summary");
        nextPage.takeValueForKey(comments,"comment");
        nextPage.takeValueForKey(selectedProduct,"bugType");
        nextPage.takeValueForKey(theDetails,"aDescription");
		nextPage.takeValueForKey(isHot(),"isHot");
		
		if(isHot() == true) {
			extra = "HOT ISSUE: ";
		}
        sendPage(from, null, to, extra + "Support Escalation ("+selectedSeverity+"/"+selectedPriority+"): "+ theSummary, (WOComponent)nextPage);
		
    }

	public WOComponent sendPage( String from, String to, NSArray toAddresses, String subject,WOComponent componentToEmail) {
		String fromName = "Tracker";
		String toName = null;
		
		// Create a new mail delivery instance
		ERMailDeliveryHTML eMail = new ERMailDeliveryHTML();

		// Set the WOComponent to be used for rendering the mail
		if(componentToEmail != null) {
			eMail.setComponent( componentToEmail );
		}
		try {
			eMail.newMail();
			
			// fromAddress with optional fromPersonalName
			if ( from != null && fromName != null ) {
				eMail.setFromAddress( from, fromName );
			} else if (from != null) {
				eMail.setFromAddress( from );
			}
			
			// optional toAddress and optional toPersonalName
			if ( to != null && toName != null ) {
				eMail.setToAddress( to, toName);
			} else if (to != null) {
				eMail.setToAddress( to );
			}
			
			// optional toAddresses (NSArray)
			if ( toAddresses != null ) {
				eMail.setToAddresses( toAddresses );
			}
			
			//1-2 = high
			//3 = normal
			// 4-5 = low priority
			//if(hot!= null) {
			//	eMail.setHeader("X-Priority", 1);
			//}

			// reply to address
			//if ( replyToAddress != null ) eMail.setReplyToAddress( replyToAddress );

			if ( subject != null ) {
				eMail.setSubject( subject );
			}
			System.out.println("sending email");
			eMail.sendMail();
			System.out.println("Mail Sent");
			
		} catch (Exception e) {
			System.err.println("Exception sending email: " + e);
		}
		return nextPage;
	}
	
	

    // This method will search for a string in a String buffer and return the data between the end
    // of the search string and the endString.
    public String searchForString(String searchString, String endString, StringBuffer theData) {
        //System.out.println("Escalate.searchForString() ");

        BufferedReader br;
        String singleLine;
        String returnString = null;
        int foundIndex;
        boolean returnVal = false;

        br = new BufferedReader(new StringReader(theData.toString()));
        try {
            while ((singleLine = br.readLine()) != null) {
                foundIndex = singleLine.indexOf(searchString);
                if(foundIndex > -1) {
                    String tempString = singleLine.substring(foundIndex+searchString.length());
                    returnString = tempString.substring(0,tempString.indexOf(endString));  // Stop
                    if(returnString != null) {
                        break;
                    }
                }
            }
        }
        catch (IOException e) {
            System.err.println("Utilities.searchForString() - " + e);
        }
        return returnString;
    }


    // Note: This uses ec instead of session().defaultEditingContext()
    public EOEnterpriseObject createEO(String entityName) {
        //System.out.println("Escalate.createEO()");

        EOEnterpriseObject anEO;
        EOClassDescription theClassDesc;

        // create an instance
        theClassDesc = EOClassDescription.classDescriptionForEntityName(entityName);
        anEO = (EOEnterpriseObject) theClassDesc.createInstanceWithEditingContext(ec, null);
        return anEO;
    }
	public NSArray profiles() {
        EOFetchSpecification fs;
        NSDictionary bindings;
		NSMutableArray qual;
		Session s;
		
		if(profiles == null) {
		
			qual = new NSMutableArray();
			s = (Session)session();
		
			// Don't show disabled profiles
			qual.addObject(EOQualifier.qualifierWithQualifierFormat("disabledtext = ''", null));

			// sort order
			Object orderings[]={
				EOSortOrdering.sortOrderingWithKey("realname", EOSortOrdering.CompareCaseInsensitiveAscending),
			};
			
			fs = new EOFetchSpecification("Profiles", new EOAndQualifier(qual), new NSArray(orderings));
			fs.setRefreshesRefetchedObjects(true);

			profiles = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		}
		return profiles;
    }


    public EOEnterpriseObject eoFromFetchSpec(String namedFetchSpec, String entityName) {
        //System.out.println("Escalate.eoFromFetchSpec()");

        EOEnterpriseObject  eo = null;
        EOFetchSpecification specification = EOFetchSpecification.fetchSpecificationNamed(namedFetchSpec,entityName);

        NSMutableArray eoArray = (NSMutableArray)session().defaultEditingContext().objectsWithFetchSpecification(specification);
        if (eoArray.count() == 1) {
            eo = (EOEnterpriseObject)eoArray.lastObject();
        }
        return eo;
    }
    public void resetDefaultBugType() {
        String returnVal = "Client: Trusted Access - Desktop";
        setSelectedProduct(returnVal);
    }

    public void setSelectedComponent(String pVal) {
        selectedComponent = pVal;
    }
    public String selectedComponent() {
        return selectedComponent;
    }
    public void setSelectedVersion(String pVal) {
        selectedVersion = pVal;
    }
    public String selectedVersion() {
        return selectedVersion;
    }
	
    public String theSummary() {
        return theSummary;
    }
    protected String theDetails() {
        return theDetails;
    }
    public void setSelectedProduct(String pType) {
    	selectedProduct = pType;
		potentialComponents();
		potentialVersions();
    }
    public String hot() {
        return hot;
    }
    public void setHot(String pVal) {
        hot = pVal;
    }
	public boolean isHot() {
		return ((hot() != null) && (hot().equals("true")))?true:false;
	}
	
	public boolean isSS() {
	 return selectedProduct.equals("Device: Secure Storage")?true:false;
	}
	
	// Action methods
    public WOComponent refresh() {
        return null;
    }
    public WOComponent goCancel() {
        return cancelPage;
    }
}


