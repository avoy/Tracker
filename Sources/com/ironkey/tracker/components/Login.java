package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.net.InetAddress;

public class Login extends WOComponent {
	private static final long serialVersionUID = 1L;
    public String username;
    public String password;
    public boolean wantsWebAssistant = false;
    public String errorMessage;

    public Login(WOContext aContext) {
        super(aContext);

        getCookieInfo();
        try {
            InetAddress local = InetAddress.getLocalHost();
            String hostname=local.getHostName();

            if(hostname.equals("kevin-avoys-computer.local")) {
                wantsWebAssistant=true;
            }
        }
        catch(Exception e) {
            System.err.println("Main.Main() - " + e);
        }

    }
	
    // Check cookies - if set, default the login/password
    public void getCookieInfo() {
        WORequest request = context().request();
        //NSDictionary cookies = request.cookieValues();
        //Enumeration enumer = cookies.keyEnumerator();
        //while(enumer.hasMoreElements()) {
          //  String key = (String)enumer.nextElement();
            //System.out.println("key - " + key + "  / Value - " + cookies.valueForKey(key));
        //}
        username = request.cookieValueForKey("tracker_loginName");
		password = request.cookieValueForKey("tracker_pw");
		String default_project = request.cookieValueForKey("default_project");
    }

    // Set cookies so users don't need to reenter credentials all the time.
    public void setCookieInfo() {
        NSTimestamp now = new NSTimestamp();
        NSTimestamp yearFromNow = now.timestampByAddingGregorianUnits(1,0,0,0,0,0);
        WOContext context = context();
        WOResponse response = context.response();
        WORequest request = context.request();

        WOCookie cookie = new WOCookie("tracker_loginName", username);
        cookie.setExpires(yearFromNow);
        cookie.setPath(request.adaptorPrefix() + "/");
        //cookie.setPath(request.adaptorPrefix() + "/" + request.applicationName());
        response.addCookie(cookie);

        WOCookie cookie2 = new WOCookie("tracker_pw", password);
        cookie2.setExpires(yearFromNow);
        cookie2.setPath(request.adaptorPrefix() + "/");
        response.addCookie(cookie2);
    }
	
	public boolean validateUser() {
        boolean returnVal = false;
        WORequest request;
        EOEditingContext ec;
        NSArray<Object> potentialUsers;
        Session session;
        EOQualifier qualifier;
        WOContext context = context();
		request = context.request();

        session = (Session)session();
		
		if(session.getUser() == null) {
			username = request.cookieValueForKey("tracker_loginName");

			EOEnterpriseObject eo = (EOEnterpriseObject)session.getUser(); // check to see if session already exists.
																		   //System.out.println("username2 - " + username);
			if(eo != null) {
				returnVal = true;
			}
			else if(username != null) {
				//password = request.cookieValueForKey("tracker_pw");
				ec = session.defaultEditingContext();
				// Fetching a person with login will return only 1 record if it is a valid login. not validating pw.
				qualifier = EOQualifier.qualifierWithQualifierFormat("loginName = '" + username+"'", null);
				EOFetchSpecification specification = new EOFetchSpecification("Profiles", qualifier,null);
				specification.setIsDeep(false);
				// Perform actual fetch
				potentialUsers = (NSArray<Object>)ec.objectsWithFetchSpecification(specification);
				if (potentialUsers.count() == 1) {
					returnVal = true;
					session.setUser((EOEnterpriseObject)potentialUsers.lastObject());
				}
			}
			else {
				returnVal = false;
			}
		}
		else {
			returnVal = true;
		}
        return returnVal;
    }
    public WOComponent defaultPage() {
        Session session;
        EOEditingContext editingContext;
        NSArray<Object> potentialUsers;
        EOEnterpriseObject userObject;
        NSMutableArray<EOQualifier> qual;
        EOQualifier qualifier;
        session = (Session)session();
        session.setUser(null);

        // Handle user login
        if (username!=null && password!=null) {
            qual = new NSMutableArray<EOQualifier>();
            editingContext = session.defaultEditingContext();
            // Fetching a person with login and password will return only 1 record if it is a valid login.
            qualifier = EOQualifier.qualifierWithQualifierFormat("loginName = '" + username+"'", null);
            qual.addObject(qualifier);
            
            //qualifier = EOQualifier.qualifierWithQualifierFormat("password = '" + password+"'", null);
            //qual.addObject(qualifier);
            EOAndQualifier userQualifier = new EOAndQualifier(qual);
            EOFetchSpecification specification = new EOFetchSpecification("Profiles", userQualifier, null);
            specification.setIsDeep(false);
            // Perform actual fetch
            potentialUsers = (NSArray<Object>)editingContext.objectsWithFetchSpecification(specification);
            if (potentialUsers.count() == 1) {
                errorMessage = "";
                setCookieInfo();  // Set a cookie if they are valid, so they don't need to enter credentials nexttime.
                userObject = (EOEnterpriseObject)potentialUsers.lastObject();  // Get the user
                session.setUser(userObject);  // Store the user in the session.
                                              // Check if user is an Admin, only admins get the 'customize' option for displaying the dtw applet.
                                              //Integer isAdmin=(Integer)userObject.storedValueForKey("isAdmin");
                                              //D2W.factory().setWebAssistantEnabled(isAdmin!=null && isAdmin.intValue()!=0);
                setDefaultProject();
                if(session.homePage == null) {
                    session.homePage = (HomePage)pageWithName("HomePage");
                        session.homePage.setIsRelease(true);
                }
                
                return session.homePage;
            }
            else {
                errorMessage="Sorry login incorrect!";
                return null;
            }
        }
        else {
            errorMessage="Please specify both fields!";
            return null;
        }

    }

    public void setDefaultProject() {

        WOContext context = context();
        if(context != null) {
            WORequest request = context.request();
            String default_project = request.cookieValueForKey("default_project");
			default_project = "1.0";
            if(default_project != null) {
                Session s = (Session)session();
				s.takeValueForKey(default_project, "selectedProject");
            }
        }

    }
}