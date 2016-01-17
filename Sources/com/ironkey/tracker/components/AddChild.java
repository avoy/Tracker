package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.*;

public class AddChild extends WOComponent {
	private static final long serialVersionUID = 1L;
	public Item currentObject;
	public String errorMessage;
	public WOComponent theNextPage;
	public Bugzilla child;	
	
	public NSArray<String> potentialTypes = new NSArray<String>(new String[] {"Bug", "Task","QA Task", "Story", "Enhancement"});
	public String aType;
	public String selectedType = "Task";
	public NSArray<String> potentialAssignees;
	public String aAssignee;
	public String selectedAssignee;
	public NSArray selectedCC;	
	public String comments;
	public String estimatedTime = "0.0";	
	public String aString;	
	public NSArray<String> potentialPriorities;
    public String selectedPriority = "3 - Medium";
    public NSArray<String> potentialSeverities;
    public String selectedSeverity = "3 - Normal";

	
	public AddChild(WOContext context) {
        super(context);
		child = new Bugzilla();
		//tasks = new NSMutableArray();
		//assigneeSelectString();  // need another way to trigger this
        String priority[] = {"1 - Urgent","2 - High", "3 - Medium", "4 - Low", "5 - Very Low"};  
        //String priority[] = {"Highest","High", "Normal", "Low", "Lowest"};  
        potentialPriorities = new NSArray<String>(priority);
        String severities[] = {"0 - Blocker","1 - Critical", "2 - Major", "3 - Normal", "4 - Minor", "5 - Trivial"};  // these should be read from db
        //String severities[] = {"blocker","critical", "major", "normal", "minor", "trivial"};  // these should be read from db
        potentialSeverities = new NSArray<String>(severities);
		
		
    }

    public WOComponent nextPage() {
        return theNextPage;
    }

    public void setNextPage(WOComponent nextPage) {
            theNextPage=nextPage;
    }

    public Item currentObject() {
        return currentObject;
    }

    public void setCurrentObject(Item anObject) {
        currentObject = (Item)anObject;
    }

    public String entity() {
            return "Story";
    }
	
	public boolean isCurrentObject() {
		return currentObject()!=null?true:false;
	}
	
	public String loginFromName(String pName) {
		Session s = (Session)session();
	
		NSMutableArray<String> tempArray = (NSMutableArray<String>)s.rowsForSql("select login_name from profiles where realname='" + pName + "' and disabledtext=''");
		return (String)tempArray.objectAtIndex(0);
	}
	public NSArray<String> potentialAssignees() {
		Session s = (Session)session();
		
		if(potentialAssignees == null) {
			NSMutableArray<String> tempArray = (NSMutableArray<String>)s.rowsForSql("select realname from profiles where disabledtext='' order by realname");
			
			//System.out.println(tempArray.count() + "\n" + tempArray.toString());
			potentialAssignees = (NSArray<String>)tempArray;

		}

		return potentialAssignees;
	}
	//<select  name='assignee[]'> <option value='0'>Gil</option> <option value='1'>Tarek</option> <option value='2'>Vijay</option> <option value='3'>Vipin</option> </select> 
	public String assigneeSelectString() {
	
		//if(assigneeSelectString == null) {
			String assigneeSelectString = "<select name='assignee'>";
			Enumeration<String> enumer = potentialAssignees().objectEnumerator();
			while(enumer.hasMoreElements()) {
				String assignee = (String)enumer.nextElement();
				//returnVal += "<option value='" + count + "'>" + assignee + "</option>";
				assigneeSelectString += "<option value='" + assignee + "'>" + assignee + "</option>";
			}
			assigneeSelectString += "</select>";
			//System.out.println( "assigneeSelectString - " + assigneeSelectString);
	//	}

		return assigneeSelectString;
	}

	public String ccString() {
		String returnVal = "";
		
		Enumeration enumer = selectedCC.objectEnumerator();
		while(enumer.hasMoreElements()) {
				String userName = (String)enumer.nextElement();
				returnVal += loginFromName(userName) + ",";
				//if(enum.hasMoreElements()) {
				//	returnVal += ","; // add comma if needed
				//}
		}
		//System.out.println("ccString - " + returnVal);
		return returnVal;
	}

    public WOComponent goCancel()
    {
        return nextPage();
    }
	
	public WOComponent goSave() {
	
		child.setBugzillaId(getBugzillaUserName());
		child.setBugzillaPassword(getBugzillaPassword());
		child.setProduct((String)currentObject.valueForKeyPath("product.productName"));

		boolean didLogin = child.login();
		
		//System.out.println("didLogin - " + didLogin);
		//System.out.println("child.bugzillaLogin() - " + child.bugzillaLogin());
		//System.out.println("child.bugzillaLoginCookie() - " + child.bugzillaLoginCookie());
		if(didLogin) {
			//potentialAssignees();
			//child.setProduct((String)currentObject.valueForKeyPath("product.productName"));
			child.setVersion((String)currentObject.valueForKey("version"));
			child.setComponent((String)currentObject.valueForKeyPath("component.componentName"));
			child.setOs("Windows");
			child.setPriority(selectedPriority);
			child.setSeverity(selectedSeverity);
			child.setItemType(selectedType);
			child.setMilestone((String)currentObject.valueForKeyPath("targetMilestone"));
			child.setEstimatedTime(estimatedTime);

			//story.setSummary("This is a test");
			//child.setBlocked("" + currentObject.bugId());  // the dependancy is set below
			child.setAssignedTo(loginFromName(selectedAssignee));
			child.setCc(ccString());

			if(comments == null) {
				child.setComments(child.summary());
			}
			else {
				child.setComments(child.summary() + "\n\n" + comments);
			}
			//System.out.println("child - \n" + child.toString());
			
			
			Item childEO = null;
			if(child.create() == true) {
				 //System.out.println("child.bugId - " + child.bugId());
				 childEO = getItem( child.bugId());
			
				// set the parent->child dependancy
				Item parent = (Item)nextPage().valueForKey("parentItem");
				parent.dropCachedChildren();			// parents will cache their children - need to clear that cache
				parent.addObjectToBothSidesOfRelationshipWithKey(childEO, "children");
				try {
					session().defaultEditingContext().saveChanges();
				}
				catch(Exception e) {
					System.err.println("Exception - " + e);
				}
			}
			else {
				errorMessage = "Error saving item - " + child.title();
				nextPage().takeValueForKey(errorMessage, "errorMessage");
			}
		}
		else {
				errorMessage = "Error logging in: Invalid Username Or Password.  Make sure your Tracker username and password are set correctly";
				nextPage().takeValueForKey(errorMessage, "errorMessage");
		}

        return nextPage();
	
	}
    public String getBugzillaUserName() {
		WOContext context = context();
        WORequest request = context.request();
		String returnVal;
		returnVal = request.cookieValueForKey("tracker_loginName");
		return returnVal;
    }
    public String getBugzillaPassword() {
		WOContext context = context();
        WORequest request = context.request();
		String returnVal;
		returnVal = request.cookieValueForKey("tracker_pw");
		return returnVal;
    }
	public Item getItem(int itemId) {
		Item anItem = null;

		NSDictionary bindings = new NSDictionary(new Object[] {itemId}, new Object[] { "bugId",});

		EOFetchSpecification fs = EOFetchSpecification.fetchSpecificationNamed( "getBug", "Item").fetchSpecificationWithQualifierBindings( bindings );
		fs.setRefreshesRefetchedObjects(true);

		NSArray list = (NSArray)session().defaultEditingContext().objectsWithFetchSpecification(fs);
		if(list.count()==1) {
			anItem = (Item)list.objectAtIndex(0);
		}
		
		return anItem;
	}
}
