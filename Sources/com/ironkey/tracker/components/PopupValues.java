package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class PopupValues extends WOComponent {
	private static final long serialVersionUID = 1L;
    protected EOEnterpriseObject object;
    protected String key;
    protected String selectedValue;
    protected String iteratedValue;

    public PopupValues(WOContext aContext) {
        super(aContext);
    }

    public void takeValuesFromRequest( WORequest aRequest, WOContext aContext) {
        super.takeValuesFromRequest (aRequest,aContext);
        try {
            //System.out.println("PopupPicker.takeValuesFromRequest -  key - " + key);
            if(object != null) {
                if(selectedValue != null) {
                    object.takeValueForKey(selectedValue,key);
                }
            }
        }
        catch(Exception e) {
            System.err.println("PopupPicker.takeValuesFromRequest() - " + e);
        }
    }

    public void setObject(EOEnterpriseObject pObject) {
        object = pObject;
    }
    public EOEnterpriseObject object() {

        return object;
    }
    public NSArray potentialValues() {
        Session s = (Session)session();
        return s.potentialValues(key);
    }
    public String selectedValue() {
        if((selectedValue == null) && (object != null)){
            selectedValue = (String)object.valueForKey(key);
        }
        return selectedValue;
    }
    

}
