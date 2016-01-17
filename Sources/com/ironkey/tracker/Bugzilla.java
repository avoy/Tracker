package com.ironkey.tracker;

import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

/*
	Bugzilla bug = new Bugzilla();
	bug.setBugzillaId("kavoy@marblecloud.com");
	bug.setBugzillaPassword("testpw");
	
	boolean didLogin = bug.login();
	if(didLogin == true) {
	
		bug.setProduct("Device: Trusted Access");
		bug.setVersion("Jetta (TA 2.8)");
		bug.setComponent("Firefox");
		bug.setRepPlatform("PC (Intel 32bits)");
		bug.setOs("Windows");
		bug.setPriority("2 - High");
		bug.setSeverity("3 - Normal");
		bug.setItemType("Bug");
		bug.setSummary("This is a test");
		bug.setComments("1) Test \n2) Test2");
		bug.create();
	}
*/

public class Bugzilla {
    protected String component = "Other";
    protected String repPlatform = "All";
    protected String product = "Media Platorm";
//    protected String priority = "3 - Medium";
//    protected String severity = "3 - Normal";
    protected String priority = "Normal";
    protected String severity = "normal";
    protected String version = "1.5";
    //protected NSArray selectedCC;
    protected String os = "All";
    protected String itemType = "Bug";
    protected String summary = "Default Summary";
    protected String whiteboard;  // whiteboard
    protected String comments;
    protected String milestone;
    protected String estimatedTime;
    protected String assignedTo = "kevinavoy@ooyala.com";
    protected String cc;    
    protected String blocked;
	
    protected String errorMessage = "";
    protected int bugId = 0;
    protected String title;
    protected static final String POST_REQUEST_PATH = "/bugzilla/post_bug.cgi"; 
	protected boolean isLoggedIn = false;

    protected String bugzillaLogin;
    protected String bugzillaLoginCookie;
	protected String bugzillaId;
	protected String bugzillaPassword;
	protected String token;		
	protected String bugzillaHost;
	protected String bugzillaProtocol;
	protected String bugzillaPort;

    public Bugzilla() {
	//	getCookieInfo();
		bugzillaId = "iksupport@marblesecurity.com";
		bugzillaPassword = "ironkey";
		bugzillaHost = ((Application)Application.application()).bugzillaHost();
		bugzillaProtocol = ((Application)Application.application()).bugzillaProtocol();
		bugzillaPort = bugzillaProtocol.equals("https")?"443":"80";
    }

    public boolean login() {
        if(isLoggedIn == false) {
            if(bugzillaId != null) {
				HTTPConnection conn = null;
				String theData = "";
				
				try {
					theData += "Bugzilla_login=" + URLEncoder.encode(bugzillaId, "UTF-8");
					theData += "&Bugzilla_password="+ URLEncoder.encode(bugzillaPassword, "UTF-8");
					theData += "&GoAheadAndLogIn=1";
					theData += "&GoAheadAndLogIn=Login";

					conn = new HTTPConnection();
					//conn.setDebugFlag(true);
					conn.setRequestMethod("POST");
					conn.setPostData(theData);
					conn.setHost(bugzillaHost);
					conn.setProtocol(bugzillaProtocol);
					conn.setPort(bugzillaPort);
					conn.setRequestPath("/bugzilla/enter_bug.cgi?product="  + URLEncoder.encode(product, "UTF-8"));
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
					else {
						isLoggedIn = true;
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
        return isLoggedIn;

    }

    public boolean create() {
        //System.out.println("Escalate.postBugzilla()");
        HTTPConnection conn = null;
        String theData = "";
        boolean returnVal = false;

		// Get agents login credentials
		if(bugzillaLogin() != null) {

			try {
				theData += "product=" + URLEncoder.encode(product, "UTF-8");
				theData += "&version=" + URLEncoder.encode(version, "UTF-8");
				theData += "&component=" +  URLEncoder.encode(component, "UTF-8");
				theData += "&rep_platform=" + URLEncoder.encode(repPlatform, "UTF-8");
				theData += "&op_sys=" + URLEncoder.encode(os, "UTF-8");;
				theData += "&priority=" + URLEncoder.encode(priority, "UTF-8");
				theData += "&bug_severity=" + URLEncoder.encode(severity, "UTF-8");
				theData += "&bug_status=CONFIRMED";
				theData += "&assigned_to=" + URLEncoder.encode(assignedTo, "UTF-8");
				theData += "&cf_type=" + itemType;

				//theData += "&cf_hot=true";
				//theData += "&cc=support%40ironkey.com";
				if(cc != null) {
				//theData += "&cc=iksupport%40ironkey.com\nkavoy%40ironkey.com";
					theData += "&cc=" + URLEncoder.encode(cc(), "UTF-8");
				}
				if(estimatedTime() != null) {
					theData += "&estimated_time=" + estimatedTime();
				}
				// theData += "&bug_file_loc=";
				

				theData += "&short_desc="+ URLEncoder.encode(summary, "UTF-8");
				if(comments != null) {
					theData += "&comment="+ comments;
				}
				if(milestone != null) {
					theData += "&target_milestone="+ milestone;
				}

				if(whiteboard != null) {
					theData += "&status_whiteboard="+ URLEncoder.encode(whiteboard, "UTF-8");
				}
				theData += "&commentprivacy=0";
				//theData += "&keywords=support_escalation";  // this puts the value in the bug but does not add to keywords table (see below)
				theData += "&dependson=";
				if(blocked() != null) {
					theData += "&blocked=" + blocked;
				}
				theData += "&form_name=enter_bug";
				theData += "&token="+token;

				conn = new HTTPConnection();
				//conn.setDebugFlag(true);
				conn.setRequestMethod("POST");
				conn.setPostData(theData);
				conn.setHost(bugzillaHost);
				conn.setProtocol(bugzillaProtocol);
				conn.setPort(bugzillaPort);
				conn.addCookie("Bugzilla_login=" + bugzillaLogin() + "; Bugzilla_logincookie=" +bugzillaLoginCookie +"; VERSION-IronKey=Jetta" );
				conn.setRequestPath(POST_REQUEST_PATH);
				
				conn.connect();

				// Parse response for the bugID    //<TABLE BORDER=1><TD><H2>Bug 13010 posted</H2>
				String bug_id = searchForString("<title>Bug ", " Submitted &ndash; ", conn.response());				
				title = searchForString("<title>", "</title>", conn.response());
				
				if(bug_id != null) {
					setBugId(Integer.parseInt(bug_id));
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

    // This method will search for a string in a String buffer and return the data between the end
    // of the search string and the endString.
    public String searchForString(String searchString, String endString, StringBuffer theData) {
        //System.out.println("Escalate.searchForString() ");

        BufferedReader br;
        String singleLine;
        String returnString = null;
        int foundIndex;

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
	
	public String toString() {
		String bug = "";
		
		bug += "------ New Bugzilla Item --------\n";
		bug += "\tcomponent - " + component + "\n";
		bug += "\trepPlatform - " + repPlatform + "\n";
		bug += "\tproduct - " + product + "\n";
		bug += "\tpriority - " + priority + "\n";
		bug += "\tseverity - " + severity + "\n";
		bug += "\tversion - " + version + "\n";
		bug += "\tos - " + os + "\n";
		bug += "\titemType - " + itemType + "\n";
		bug += "\tsummary - " + summary + "\n";
		bug += "\twhiteboard - " + whiteboard + "\n";
		bug += "\tcomments - " + comments + "\n";
		bug += "\tmilestone - " + milestone + "\n";
		bug += "\testimatedTime - " + estimatedTime + "\n";
		bug += "\tassignedTo - " + assignedTo + "\n";
		bug += "\tblocked - " + blocked + "\n";
		
		return bug;
	
	}

    public String component() { return component; }
    public void setComponent(String pVal) { component = pVal;}
    public String repPlatform() { return repPlatform; }
    public void setRepPlatform(String pVal) { repPlatform = pVal;}
	public String product() { return product;}
    public void setProduct(String pVal) { product = pVal;}
	public String priority() { return priority;}
    public void setPriority(String pVal) { priority = pVal;}
	public String severity() { return severity;}
    public void setSeverity(String pVal) { severity = pVal;}
    public void setVersion(String pVal) { version = pVal; }
    public String version() { return version; }
	public String os() { return os;}
    public void setOs(String pVal) { os = pVal;}
	public String itemType() { return itemType;}
    public void setItemType(String pVal) { itemType = pVal;}
	public String summary() { return summary; }
    public void setSummary(String pVal) { summary = pVal;}
    protected String whiteboard() { return whiteboard; }
    public void setWhiteboard(String pVal) { whiteboard = pVal;}
	public String comments() { return comments; }
    public void setComments(String pVal) { comments = pVal;}
	public String milestone() { return milestone; }
    public void setMilestone(String pVal) { milestone = pVal;}
	public String estimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String pVal) { estimatedTime = pVal;}
	public String assignedTo() { return assignedTo; }
    public void setAssignedTo(String pVal) { assignedTo = pVal;}
	public String cc() { return cc; }
    public void setCc(String pVal) { cc = pVal;}
	public String blocked() { return blocked; }
    public void setBlocked(String pVal) { blocked = pVal;}
	public String title() { return title; }
    public void setTitle(String pVal) { title = pVal;}
	
	public String bugzillaLogin() { return bugzillaLogin; }
    public void setBugzillaLogin(String pVal) { bugzillaLogin = pVal;}
	public String bugzillaLoginCookie() { return bugzillaLoginCookie; }
    public void setBugzillaLoginCookie(String pVal) { bugzillaLoginCookie = pVal;}
	public String bugzillaId() { return bugzillaId; }
    public void setBugzillaId(String pVal) { bugzillaId = pVal;}
	public String bugzillaPassword() { return bugzillaPassword; }
    public void setBugzillaPassword(String pVal) { bugzillaPassword = pVal;}
	public int bugId() { return bugId; }
    public void setBugId(int pVal) { bugId = pVal;}
	

}


