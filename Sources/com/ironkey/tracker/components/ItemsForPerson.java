package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class ItemsForPerson extends WOComponent {	
	private static final long serialVersionUID = 1L;
	public String sprintKey;
	public String daysRemainingInSprint;
	public String weekdaysRemainingInSprint;	
	public Item anEo;
	public Item anItem;
	public TopicStatus personTopic;
	public WOComponent nextPage;
	public String selectedProject;	
	//protected NSArray statusValues= new NSArray(new Object[] {"NEW", "ASSIGNED", "REOPENED", "RESOLVED", "VERIFIED", "CLOSED"});

    public ItemsForPerson(WOContext context) {
        super(context);
    }
	
	// placeholder until support for resolvedItems is added
	public NSArray resolvedItems() {
		return new NSArray();
	}
	
	public String labelForSprint() {
		String returnVal;
		
		if(sprintKey.equals("S1"))
			returnVal = "Sprint 1";
		else if(sprintKey.equals("S2"))
			returnVal = "Sprint 2";
		else if(sprintKey.equals("S3"))
			returnVal = "Sprint 3";
		else if(sprintKey.equals("S4"))
			returnVal = "Sprint 4";
		else if(sprintKey.equals("S5"))
			returnVal = "Sprint 5";
		else if(sprintKey.equals("S6"))
			returnVal = "Sprint 6";
		else if(sprintKey.equals("S7"))
			returnVal = "Sprint 7";
		else if(sprintKey.equals("S8"))
			returnVal = "Sprint 8";
		else if(sprintKey.equals("S9"))
			returnVal = "Sprint 9";
		else if(sprintKey.equals("S10"))
			returnVal = "Sprint 10";
		else if(sprintKey.equals("S11"))
			returnVal = "Sprint 11";
		else if(sprintKey.equals("S12"))
			returnVal = "Sprint 12";
		else if(sprintKey.equals("S13"))
			returnVal = "Sprint 13";
		else if(sprintKey.equals("S14"))
			returnVal = "Sprint 14";
		else if(sprintKey.equals("---"))
			returnVal = "Backlog";
		else
			returnVal = sprintKey;
			
		return returnVal;

	}
	
	public String percentComplete() {
		double complete = (double)personTopic.allItem().hoursWorkedAll();
		double all = (double)personTopic.allItem().currentEstimateHoursAll();
		

        return percentCompleteString(((double)complete/(double)all) * 100);
    }
	
	public String percentCompleteString(double pVal) {
        String returnVal = null;
        if(Double.isNaN(pVal)) {
                returnVal = "---";
        }
        else {
            try {
                NSNumberFormatter nf = new NSNumberFormatter();
                nf.setFormat("##0");
                returnVal = nf.format(new Double(pVal)) + "%";
            }
            catch(Exception e) {
                System.err.println("ReleasePlan.percentCompleteString() - " + e);
            }
        }
        return returnVal;
    }

	public TopicStatus personTopic() {
		return personTopic;
	}
	
	public void setPersonTopic(TopicStatus aTopic) {	
		personTopic = aTopic;
	}

    public WOComponent goBack()
    {
		return nextPage;
    }
}
