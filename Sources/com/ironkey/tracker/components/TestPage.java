package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class TestPage extends WOComponent {
	private static final long serialVersionUID = 1L;
    public String selectedProject;
    public Item anEo;
	public NSArray<Object> aList;

    public TestPage(WOContext context) {
        super(context);

    }
	
	
	public void setSelectedProject(String aVal) {
		System.out.println("TestPage.setSelectedProject");
		selectedProject = aVal;
		//if(aList == null) {		
		//	aList = storiesForSelectedRelease();
		//}
	}

	

}
