package com.ironkey.tracker.components;


import com.webobjects.appserver.*;

public class ReportListWrapper extends WOComponent {
	private static final long serialVersionUID = 1L;

	public WODisplayGroup displayGroup;
    //protected EODatabaseDataSource _queryDataSource;
    //private boolean _hasToUpdate=false; // when set to true, the list is refreshed before the page is rendered
    protected int totalBugs;
    protected int totalActive;
    protected boolean didUpdate = false;
    protected String label;
    public String aRow = "Assignee";
    public String aColumn = "Priority";
    public boolean isList = true;
    public boolean isEstimate;
    public boolean isReport = false;
    public boolean hideController = false;
	public boolean needUpdate = true;
	public boolean commentsFlag = false;
	public boolean releaseFlag = false;

    public ReportListWrapper(WOContext context) {
        super(context);
    }
	
	public WOComponent emailCurrentPage() {
        //System.out.println("ReportListWrapper.emailCurrentPage()");
	
        WOComponent nextPage = (WOComponent)pageWithName("EmailPage");

        nextPage.takeValueForKey(componentToEmail(),"componentToEmail");
        nextPage.takeValueForKey(context().page(),"nextPage");

        return nextPage;
    }
	
	
	public WOComponent componentToEmail() {
        System.out.println("ReportListWrapper.componentToEmail()");
        WOComponent comp = null;
        if(isList() == true) {
            comp = (WOComponent)pageWithName("ItemList");
			comp.takeValueForKey(displayGroup, "itemDisplayGroup");
            ((ItemList)comp).displayAll();  // no batching in emails  - how to return it to original setting?
			((ItemList)comp).setEmailFlag(true);  // set a flag for emails to hide any content that does not display properly
			((ItemList)comp).setCommentsFlag(commentsFlag);  
			((ItemList)comp).setReleaseFlag(releaseFlag);  

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
		/*
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
*/
        return null;
    }
    public WOComponent showEstimate() {
        setIsReport(false);
        setIsList(false);
        setIsEstimate(true);
        //selectIndividualEstimate = false;
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
    public void setDisplayGroup(WODisplayGroup pDisplayGroup) {
        displayGroup = pDisplayGroup;
    }
    public String aRow() {
        return aRow;
    }
    public void setARow(String newRowType) {
        aRow = newRowType;
    }
    public String aColumn() {
        return aColumn;
    }
    public void setAColumn(String pVal) {
        aColumn = pVal;
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
	public boolean commentsFlag() {return commentsFlag;}
	public void setCommentsFlag(boolean pVal) {commentsFlag = pVal;}
	public boolean releaseFlag() {return releaseFlag;}
	public void setReleaseFlag(boolean pVal) {releaseFlag = pVal;}
	
}