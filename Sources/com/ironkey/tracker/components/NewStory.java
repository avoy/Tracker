package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.*;

public class NewStory extends WOComponent {
	private static final long serialVersionUID = 1L;	
    private String _validationExceptionString;
    public String errorMessage;
    public WOComponent theNextPage;
    public String aRelease;
    public NSArray potentialReleases;
    public String aMilestone;
	public Bugzilla story;
	public String aString;	
    public String projectLimitCharacter = "A";
	public NSArray potentialProducts;
    public String selectedProduct = "Device: Secure Storage";
    public NSArray potentialAssignees;
    public String assigneeSelectString;
    public NSMutableArray tasks;
	
	public NewStory(WOContext context) {
        super(context);
		story = new Bugzilla();
		tasks = new NSMutableArray();
		assigneeSelectString();  // need another way to trigger this
		
    }

	public NSArray sprintKeys() {
		NSArray sprintValues= new NSArray(new Object[] {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14", "---"});

		return sprintValues;
	}
	
	public void takeValuesFromRequest( WORequest aRequest, WOContext aContext) {

        try {
			String productIndex = (String)aRequest.formValueForKey("product");

			if((aRequest != null) && (productIndex != null)) {			
				String product = productFromIndex(productIndex);
				if(!product.equals(selectedProduct())) {
					setSelectedProduct(product);
					potentialReleases = null;
				}
				else {
					super.takeValuesFromRequest(aRequest, aContext);
					//NSDictionary theDict = aRequest.formValues();
					//Enumeration enum1 = theDict.keyEnumerator();
					//while(enum1.hasMoreElements()) {
					//   String theKey = (String)enum1.nextElement();
					//   System.out.println("theKey = " + theKey + " = " + theDict.objectForKey(theKey));
					//}
					
					
					// Get tasks
					NSArray taskDescs = (NSArray)aRequest.formValuesForKey("task_description");
					NSArray taskAssignees = (NSArray)aRequest.formValuesForKey("assignee");
					NSArray taskEstimates = (NSArray)aRequest.formValuesForKey("estimate");
					int numTasks = taskDescs.count();
					for(int i = 0; i<numTasks; i++) {
						Bugzilla task = new Bugzilla();
						task.setSummary((String)taskDescs.objectAtIndex(i));
						task.setAssignedTo(loginFromName((String)taskAssignees.objectAtIndex(i)));
						task.setEstimatedTime((String)taskEstimates.objectAtIndex(i));
						tasks.addObject(task);
					}					
				}
			}
			else{
				// set all keys and submit normally
				super.takeValuesFromRequest(aRequest, aContext);
			}
        }
        catch(Exception e) {
            System.err.println("++++  NewStory.takeValuesFromRequest() - " + e);
        }
    }
	
	public NSArray potentialProducts() {
        if(potentialProducts == null) {
            Session s = (Session)session();
            NSMutableArray tempTypes = new NSMutableArray(s.potentialProducts());

            potentialProducts = new NSArray(tempTypes);
        }
        return potentialProducts;
    }

	
	public NSArray potentialReleases() {
	
		Session s;
		String productIdString = "(";
		String sqlString;
		boolean success = false;
		
		if(potentialReleases == null) {
		
			s = (Session)session();

		   int prodId = idForProduct(selectedProduct());			   
		   
		   if(prodId > 0) {
				if(( prodId == 2) || ( prodId == 3) || ( prodId == 17)) { //IronKey Services, Device: Secure Storage , Device: Trusted Access
					projectLimitCharacter = "I";							// limit versions to being 'I' or greater to reduce the list to not include older versions
				}
				else {
					projectLimitCharacter = "A";							// reset
				}
			   success = true;
			   
			}

			if(success == true) {
				productIdString += ")";
				// Also exclude versions that start with '_'
				sqlString = "select distinct(value) from versions where product_id = " + prodId + " and value>'" + projectLimitCharacter +"' and value not like '\\_%' order by value";
			
				potentialReleases =  (NSArray)s.rowsForSql(sqlString);
			}
		}
		
		return potentialReleases;
	}
	
	public int idForProduct(String pProductName) {
		//System.out.println("\n\n============ ReleasePlan.idForProduct()");

		int returnVal = -1;
        Session s = (Session)session();
        NSArray values =  s.rowsForSql("select id from products where name='"+ pProductName+ "'");
		if(values.count() > 0) {
			Short val = (Short)values.objectAtIndex(0);
			returnVal =  val.intValue();
		}
		return returnVal;
    }
	


	public void setSelectedProduct(String pVal) {
		//if((selectedProducts != null) && (!selectedProducts.equals(pValues))) {
			//setDefaultsCookie("default_products", tokenizeProductArray(pValues));
		//}

		// if different than current
		selectedProduct = pVal;
	}
	public String selectedProduct() {
		return selectedProduct;
	}
//	public String selectedProduct() {
//		return (String)selectedProducts().objectAtIndex(0);
//	}


	public String productFromIndex(String pIndex) {
		String aProduct = (String)potentialProducts().objectAtIndex(Integer.parseInt(pIndex));
		return aProduct;
	}
/*

    public String listDescriptionForProducts() {
        NSArray list = (currentObject != null) ? (NSArray) currentObject.valueForKeyPath("products") : null;
        int n= list!=null ? list.count() : 0; 
        return n +" "+ ((n == 1) ? "ReleaseProduct" : "ReleaseProducts");
    }

	public NSArray potentialReleases() {
        NSMutableArray values;

        if(potentialReleases == null) {
			String currentObjectRelease = "";
            Session s = (Session)session();
            values =  (NSMutableArray)s.rowsForSql("select distinct(value)from versions where product_id in (2,3, 17) order by value");
            values.removeObject("_Future");
            //values.removeObject("OTP-Demo");
			//values.addObject("_Future");  // want _Future to be the last item			
			//String support = (String)s.valueForKey("supportProject");
			
			NSMutableArray temp = new NSMutableArray(values);


			if(currentObject != null) {
				currentObjectRelease = (String)currentObject.valueForKey("releaseName");
				if(currentObjectRelease == null) {
					currentObjectRelease = "";
				}
			}
			potentialReleases = new NSArray(temp);
        }
        return potentialReleases;
    }
	
	public NSMutableArray existingMilestones() {
		NSArray milestones = (NSArray)currentObject.valueForKey("milestones");
		NSMutableArray existingMilestones = new NSMutableArray();
		
		Enumeration enumer = milestones.objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aMilestone = (EOEnterpriseObject)enumer.nextElement();
			String milestoneName = (String)aMilestone.valueForKey("milestone");
			if(milestoneName != null) {
				existingMilestones.addObject(milestoneName);
			}
		}
		return existingMilestones;
	}
	public NSMutableArray existingProducts() {
		NSArray products = (NSArray)currentObject.valueForKey("products");
		NSMutableArray existingProducts = new NSMutableArray();
		
		Enumeration enumer = products.objectEnumerator();
		while(enumer.hasMoreElements()) {
			EOEnterpriseObject aProduct = (EOEnterpriseObject)enumer.nextElement();
			String productName = (String)aProduct.valueForKey("productName");
			if(productName != null) {
				existingProducts.addObject(productName);
			}
		}
		return existingProducts;
	}
	*/

    public WOComponent nextPage() {
        return theNextPage;
    }

    public void setNextPage(WOComponent nextPage) {
            theNextPage=nextPage;
    }

   // public Item currentObject() {
    //    return currentObject;
    //}

    //public void setCurrentObject(Item anObject) {
     //   currentObject = (Item)anObject;
    //}

    public String entity() {
            return "Story";
    }
	
	public String loginFromName(String pName) {
		Session s = (Session)session();
	
		NSMutableArray tempArray = (NSMutableArray)s.rowsForSql("select login_name from profiles where realname='" + pName + "'");
		return (String)tempArray.objectAtIndex(0);
	}
	public NSArray potentialAssignees() {
		Session s = (Session)session();
		
		if(potentialAssignees == null) {
			NSMutableArray tempArray = (NSMutableArray)s.rowsForSql("select realname from profiles where disabledtext='' order by realname");
			tempArray.removeObject("DJ");
			tempArray.removeObject("Jijo Varghese");
			tempArray.removeObject("Matt LeBaron");
			tempArray.removeObject("David Huang");
			tempArray.removeObject("LoginKing");
			tempArray.removeObject("David Zalatimo");
			tempArray.removeObject("Change Master");
			tempArray.removeObject("Bob Dawson");
			tempArray.removeObject("Change Master");
			tempArray.removeObject("Michael deViveiros");
			tempArray.removeObject("Web Ops");
			tempArray.removeObject("Service Escalation");
			tempArray.removeObject("Enterprise Server Escalation");
			tempArray.removeObject("Sidney Ong");
			tempArray.removeObject("Stephanie Houchin");
			tempArray.removeObject("Feature Request");
			tempArray.removeObject("Propose for deferral");
			tempArray.removeObject("Mun-Wai Chung");
			tempArray.removeObject("Tihua Lee");
			tempArray.removeObject("Frank Hecker");
			tempArray.removeObject("Clint Yow");
			tempArray.removeObject("Ralston Siaotong");
			tempArray.removeObject("Support");
			tempArray.removeObject("John Zarganis");
			tempArray.removeObject("Sam Farsad");
			tempArray.removeObject("Mike Nakamura");
			tempArray.removeObject("Eric Chesterman");
			tempArray.removeObject("Brett Byers");
			tempArray.removeObject("Shekhar Kamat");
			tempArray.removeObject("Stephane DI VITO");
			tempArray.removeObject("Rafael B. Cruz");
			tempArray.removeObject("Mark Younger - SE");
			tempArray.removeObject("Ian Garatt");
			tempArray.removeObject("Chris Louie");
			tempArray.removeObject("Carlos Krystof");
			tempArray.removeObject("Wesley Asbell");
			tempArray.removeObject("ITHelpdesk System");
			tempArray.removeObject("David Murphy");
			tempArray.removeObject("Bill Paul");
			tempArray.removeObject("Martha Wu");
			tempArray.removeObject("DB Master");
			tempArray.removeObject("Gil Spencer");
			tempArray.removeObject("QA");
			tempArray.removeObject("Andrew Jordan");
			tempArray.removeObject("yngve");
			tempArray.removeObject("Sticky Password");
			// Firmware team
			tempArray.removeObject("Ahuja Ramesh");
			tempArray.removeObject("Anh Phan");
			tempArray.removeObject("ArunPrasad Ramiya Mothilal");
			tempArray.removeObject("Eric Smith");
			tempArray.removeObject("Johnny Le");
			tempArray.removeObject("Paul Fruhauf");
			tempArray.removeObject("Shannon Holland");
			tempArray.removeObject("Phison 2251-85");
			tempArray.removeObject("Sticky Password");
			tempArray.removeObject("Sticky Password");
			
			// QA not likely
			tempArray.removeObject("Kevin Avoy");			
			tempArray.removeObject("Daniel Rohde");
			tempArray.removeObject("Karthi Jothi");
			tempArray.removeObject("Greg Kuzmishchev");
			tempArray.removeObject("Chris Cassell");

			
			System.out.println(tempArray.count() + "\n" + tempArray.toString());
			potentialAssignees = (NSArray)tempArray;

		}

		return potentialAssignees;
	}
	//<select  name='assignee[]'> <option value='0'>Gil</option> <option value='1'>Tarek</option> <option value='2'>Vijay</option> <option value='3'>Vipin</option> </select> 
	public String assigneeSelectString() {
	
		//if(assigneeSelectString == null) {
			String assigneeSelectString = "<select name='assignee'>";
			int count = 0;
			Enumeration enumer = potentialAssignees().objectEnumerator();
			while(enumer.hasMoreElements()) {
				String assignee = (String)enumer.nextElement();
				//returnVal += "<option value='" + count + "'>" + assignee + "</option>";
				assigneeSelectString += "<option value='" + assignee + "'>" + assignee + "</option>";
				count++;
			}
			assigneeSelectString += "</select>";
			//System.out.println( "assigneeSelectString - " + assigneeSelectString);
	//	}

		return assigneeSelectString;
	}
	
    public WOComponent goCancel()
    {
        return null;
    }
	
	public WOComponent goSave() {
	
		
		story.setBugzillaId("kavoy@marblesecurity.com");
		story.setBugzillaPassword("test12");
	
		boolean didLogin = story.login();
		
		System.out.println("didLogin - " + didLogin);
		System.out.println("story.bugzillaLogin() - " + story.bugzillaLogin());
		System.out.println("story.bugzillaLoginCookie() - " + story.bugzillaLoginCookie());
		
		//potentialAssignees();
		story.setProduct(selectedProduct());
		story.setVersion(story.version());
		story.setComponent("Unknown");
		//story.setRepPlatform("PC (Intel 32bits)");
		story.setOs("Windows");
		story.setPriority("2 - High");
		story.setSeverity("3 - Normal");
		story.setItemType("Story");
		//story.setSummary("This is a test");
		story.setComments(story.summary() + "\n\n - New Story - Added through Tracker");
		System.out.println("Story - \n" + story.toString());
		if(story.create() == true) {
			System.out.println("story.bugId - " + story.bugId());
		
		}
		//story.setBugId(15661);
		
		Enumeration enumer = tasks.objectEnumerator();
		while(enumer.hasMoreElements()) {
			Bugzilla aTask = (Bugzilla)enumer.nextElement();
		//aTask.setBugzillaId("kavoy@marblesecurity.com");
		//aTask.setBugzillaPassword("test12");
		 //didLogin = aTask.login();

			// loginCookie - make static?
			aTask.setBugzillaLogin(story.bugzillaLogin());
			aTask.setBugzillaLoginCookie(story.bugzillaLoginCookie());

			aTask.setProduct(story.product());
			aTask.setVersion(story.version());
			aTask.setMilestone(story.milestone());
			aTask.setComponent("Unknown");
			aTask.setOs("Windows");
			aTask.setPriority("2 - High");
			aTask.setSeverity("3 - Normal");
			aTask.setItemType("Task");
			aTask.setComments(aTask.summary() + "\n\n - New Task - Added through Tracker");
			aTask.setBlocked("" + story.bugId());
			System.out.println("aTask - \n" + aTask.toString());
			if(aTask.create() == true) {
				System.out.println("aTask.bugId - " + aTask.bugId());
			}
				
		}

		return null;
	
	}
}
