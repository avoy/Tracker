package com.ironkey.tracker.components;

import com.webobjects.appserver.*;

public class Comment extends WOComponent {
	private static final long serialVersionUID = 1L;	
	public String aComment;

    public Comment(WOContext context) {
        super(context);
    }
	
	
	public String aComment() {return aComment;}
	public void setAComment(String pVal) {aComment = pVal;}


}
