package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import java.util.*;
//import er.extensions.ERXEditingContextDelegate;
//import er.extensions.ERXGenericRecord;


public class EditMilestoneDates extends WOComponent implements EditPageInterface {
	private static final long serialVersionUID = 1L;
    protected NextPageDelegate nextPageDelegate;

    private String _validationExceptionString;

    /** @TypeInfo MilestoneDates */
    public EOEnterpriseObject currentObject;
    public NSArray potentialVersions;
    public NSArray potentialTypes;
    public String aType;
    public String selectedType = "Media Platform";
	/*
	protected NSArray potentialOptions;
    protected String selectedOption = "Start of Day";  // StartDate (also used as Forecast)
    protected String selectedEndOption = "Start of Day";
    protected String selectedPlannedOption = "End of Day";
    protected String selectedForcastOption = "End of Day";
    protected String selectedActualOption = "End of Day";
    protected String anOption;
	*/

    public String errorMessage;
    private EOEditingContext _inspectEditingContext;

    protected WOComponent theNextPage;

    /** @TypeInfo java.lang.String */
    protected NSMutableArray potentialMilestones;

    /** @TypeInfo java.lang.String */
    public NSArray existingMilestones;
    public String aMilestone;
    public boolean isEditMode;
    public String aRelease;
    public String selectedRelease;
    public NSTimestamp milestoneDate; // 
    public NSTimestamp milestoneEndDate; // 
    public NSTimestamp milestonePlannedDate; 
    public NSTimestamp milestoneForecastDate; 
    public NSTimestamp milestoneActualDate;
	
		
	public EditMilestoneDates(WOContext aContext) {
        super(aContext);
        errorMessage = "";
		//Object types[] = {"Device (SS)","Device (TA)","Device (El Camino)","Service"};
		Object types[] = {"Media Platform"};
        potentialTypes = new NSArray(types);
		
		//Object options[] = {"Start of Day","End of Day"};
        //potentialOptions = new NSArray(options);


    }

			// time string
//		NSTimestampFormatter formatter = new NSTimestampFormatter("%m/%d/%Y");
//		String timeString = formatter.format(today);


    public void takeValuesFromRequest(WORequest request, WOContext context) {
        // Determine if 'type' reset
		/*
        System.out.println("EditMilestoneDates.takeValuesFromRequest()") ;
		NSDictionary theDict = request.formValues();
		Enumeration enum1 = theDict.keyEnumerator();
		while(enum1.hasMoreElements()) {
            String theKey = (String)enum1.nextElement();
            System.out.println("theKey = " + theKey + " = " + theDict.objectForKey(theKey));
		}
		*/
		super.takeValuesFromRequest(request, context);
		
		//System.out.println("type = " + (String)request.formValueForKey("type"));
		String type = (String)request.formValueForKey("type");
		String release = (String)request.formValueForKey("release");
		if(type != null) {
			int newType = Integer.parseInt(type);
			int currentType = potentialTypes.indexOfObject(selectedType);
			if(newType != currentType) {
				setSelectedType((String)potentialTypes.objectAtIndex(newType));
			}
		}
		if(release != null) {
			int newRelease = Integer.parseInt(release);
			int currentRelease = -1;
			if(selectedRelease != null) {
				currentRelease = potentialVersions.indexOfObject(selectedRelease);
			}
			if((selectedRelease == null) || (newRelease != currentRelease)) {
				setSelectedRelease((String)potentialVersions.objectAtIndex(newRelease));
				potentialMilestones = null;
			}
		}
	}
    public void setSelectedType(String pType) {
        selectedType = pType;
		potentialVersions();
    }

    public NSArray potentialVersions() {
		//Object types[] = {"Client: Trusted Access - Enterprise" ,"Client: Trusted Access - Mobile", "Client: Trusted Access-Mac", "Device: Trusted Access","Internal Business Systems", "IronKey Service"};
	
        if((selectedType != null) && (selectedType.equals("CMedia Platform"))) {
            potentialVersions = potentialVersionsForProduct("1");
        }
        else { //											Media Platform
            potentialVersions = potentialVersionsForProduct("1");
        }

        return potentialVersions;
    }
	
	public NSArray potentialVersionsForProduct(String pProductIds) {
        Session s = (Session)session();
       return s.rowsForSql("select distinct(value) from versions where product_id in (" + pProductIds + ") order by value");
    }


    public void awake() {
        super.awake();
    	if (_inspectEditingContext!=null) {
            _inspectEditingContext.lock();
        }
	_validationExceptionString= null;
    }
	
	

    boolean tryToSaveChanges(boolean validateObject) throws Throwable {
        try {
            if (validateObject) {
                if (_validationExceptionString!=null) {
                    errorMessage = " Could not save your changes:<BR> "+_validationExceptionString;
                    _validationExceptionString = null;
                    return false;
                }
                currentObject.validateForSave();
            }
            if (currentObject!=null) currentObject.editingContext().saveChanges();
        } catch (NSValidation.ValidationException exception) {
            errorMessage = " Could not save your changes:<BR> " + exception.getMessage() + " ";
            return false;
        } catch (RuntimeException exception) {
            if (NSForwardException._originalThrowable(exception) instanceof EOGeneralAdaptorException) {
                errorMessage = " Could not save your changes:<BR> " + exception.getMessage() + " ";
                return false;
            } else {
                throw exception;
            }
        } 
        return true;
    }


    public WOComponent deleteAction() throws Throwable {
        // do not want to try to delete object that is not in the editingContext
        if (currentObject.editingContext()!=null) {
            currentObject.editingContext().deleteObject(currentObject);
            return tryToSaveChanges(false) ? nextPage() : null;
        }
        return nextPage();
    }

    public WOComponent nextPage() {
        return (nextPageDelegate != null) ? nextPageDelegate.nextPage(this) :
            (theNextPage!=null) ? theNextPage : null;
    }

    public void setNextPage(WOComponent nextPage) {
            theNextPage=nextPage;
    }

    public void validationFailedWithException(Throwable theException, Object theValue, String theKeyPath) {
                _validationExceptionString = (_validationExceptionString == null ? "" : _validationExceptionString) + theException.getMessage() + " ["+ theKeyPath+"="+theValue +"]<BR>";
    }

    public EOEnterpriseObject object()
    {
        return currentObject;
    }

    public void setObject(EOEnterpriseObject anObject) {
    	EOEditingContext _newContext = (anObject != null) ? anObject.editingContext() : null;
    	setEditingContext(_newContext);
        currentObject = anObject;
		if(currentObject.editingContext()!=null) {
			EOGlobalID eog = currentObject.editingContext().globalIDForObject((EOEnterpriseObject)currentObject);
			if(eog.isTemporary() != true) {
				isEditMode = true;
			}
		}
		
    }

    public boolean showCancel() {
       return nextPageDelegate!=null || theNextPage!=null;
     }

    public void setNextPageDelegate(NextPageDelegate delegate) {
           nextPageDelegate= delegate;
    }

    public WOComponent cancelAction() {
        if (currentObject.editingContext()!=null)
            currentObject.editingContext().revert();
        return nextPage();
    }

    public void sleep() {
    	if (_inspectEditingContext!=null) {
            _inspectEditingContext.unlock();
        }
        super.sleep();
    }

    public WOComponent editAction() {
        // release note:
        // this is only used when generating an inspect page
        // when generating an edit page, you can remove this method
		EditPageInterface editPage=D2W.factory().editPageForEntityNamed(currentObject.entityName(),session());
		editPage.setObject(currentObject);
        editPage.setNextPage(nextPage());    
	return (WOComponent)editPage;
    }

    public String entity() {
            // this method is used by the Header and the WebAssistant
            return "MilestoneDates";
    }

    public WOComponent submitAction() throws Throwable {
		// release note:
		// 		this method is only used in Edit pages, it can be removed in
		// Inspect pages.
		//
        // if we're editing a new object, insert before saving
        if (currentObject.editingContext()==null) {
            session().defaultEditingContext().insertObject(currentObject);
            setEditingContext(currentObject.editingContext());

        }
        // if save failed return same page with error message 
        return tryToSaveChanges(true) ? nextPage() : null;
    }

    protected void setEditingContext(EOEditingContext newEditingContext) {
        // do proper unlocking of old context and locking of new context
        if (newEditingContext != _inspectEditingContext) {
            if (_inspectEditingContext != null) {
                _inspectEditingContext.unlock();
            }
            _inspectEditingContext = newEditingContext;
            if (_inspectEditingContext != null) {
                _inspectEditingContext.lock();
            }
        }
    }

    /** @TypeInfo java.lang.String */
    public NSMutableArray potentialMilestones() {
		if(potentialMilestones == null) {
			Session s = (Session)session();
			Object milestones[] = {"---","S1","S2","S3","S4","S5","S6","S7","S8","S9","S10","S11","S12","S13","S14","CC","RC1","RTM","RTP","Device Update", "patch-1","patch-2","patch-3","patch-4"};
			potentialMilestones = new NSMutableArray(milestones);
			NSArray existing = existingMilestones();
			if(existing != null) {
				Enumeration enumer = existing.objectEnumerator();
				while(enumer.hasMoreElements()) {
					String aMilestone = (String)enumer.nextElement();
					potentialMilestones.removeObject(aMilestone);
				}
			}
			
		}
        return potentialMilestones;
    }
	

	public void setSelectedRelease(String pVal) {
		selectedRelease = pVal;
	}
	
	public String selectedRelease() {
		return selectedRelease;
	}
	public void setMilestoneDate(NSTimestamp pVal) {
		milestoneDate = pVal;
		currentObject.takeValueForKey(pVal, "startDate");
	}
	
	public NSTimestamp milestoneDate() {
		if((milestoneDate == null) && (isEditMode() == true)) {
			milestoneDate = (NSTimestamp)currentObject.valueForKey("startDate");
		}
		return milestoneDate;
	}
	public void setMilestoneEndDate(NSTimestamp pVal) {
		milestoneEndDate = endOfDayTimeStamp(pVal);
		currentObject.takeValueForKey(milestoneEndDate, "endDate");
	}
	
	public NSTimestamp milestoneEndDate() {
		if((milestoneEndDate == null) && (isEditMode() == true)) {
			milestoneEndDate = (NSTimestamp)currentObject.valueForKey("endDate");
		}
		return milestoneEndDate;
	}

	public void setMilestonePlannedDate(NSTimestamp pVal) {
		milestonePlannedDate = endOfDayTimeStamp(pVal);
		currentObject.takeValueForKey(milestonePlannedDate, "plannedDate");
	}
	public NSTimestamp milestonePlannedDate() {
		if((milestonePlannedDate == null) && (isEditMode() == true)) {
			milestonePlannedDate = (NSTimestamp)currentObject.valueForKey("plannedDate");
		}
		return milestonePlannedDate;
	}

	public void setMilestoneForecastDate(NSTimestamp pVal) {
		milestoneForecastDate = endOfDayTimeStamp(pVal);
		currentObject.takeValueForKey(milestoneForecastDate, "forecastDate");
	}
	public NSTimestamp milestoneForecastDate() {
		if((milestoneForecastDate == null) && (isEditMode() == true)) {
			milestoneForecastDate = (NSTimestamp)currentObject.valueForKey("forecastDate");
		}
		return milestoneForecastDate;
	}

	public void setMilestoneActualDate(NSTimestamp pVal) {
		milestoneActualDate = endOfDayTimeStamp(pVal);
		currentObject.takeValueForKey(milestoneActualDate, "actualDate");
	}
	public NSTimestamp milestoneActualDate() {
		if((milestoneActualDate == null) && (isEditMode() == true)) {
			milestoneActualDate = (NSTimestamp)currentObject.valueForKey("actualDate");
		}
		return milestoneActualDate;
	}
	
	public String milestone() {
		return (String)currentObject.valueForKey("milestone");
	}
	
	public String sprintDatesDefault() {
	
		String 
		returnVal = "display:none;";
		if((isEditMode() == true) && (milestone() != null) && (milestone().startsWith("S"))) {
			returnVal = "'';";
		}
		return returnVal;
	}
	public String milestoneDatesDefault() {
	
		String 
		returnVal = "display:none;";
		if((isEditMode() == true) && (milestone() != null) && (!milestone().startsWith("S"))) {
			returnVal = "'';";
		}
		return returnVal;
	}
	
	/*

	public String selectedOption() {
		return selectedOption;
	}
	
	public void setSelectedOption(String pVal) {
		selectedOption = pVal;
		// Cover method to ensure valid  date
		if(selectedOption.equals("End of Day")==true) {
			setMilestoneDate(endOfDayTimeStamp(milestoneDate()));
		}
	}
	
	public String selectedEndOption() {
		return selectedEndOption;
	}
	public void setSelectedEndOption(String pVal) {
		selectedEndOption = pVal;
		// Cover method to ensure valid  date
		if(selectedEndOption.equals("End of Day")==true) {
			setMilestoneEndDate(endOfDayTimeStamp(milestoneEndDate()));
		}
	}
	public String selectedActualOption() {
		return selectedActualOption;
	}
	public void setSelectedActualOption(String pVal) {
		selectedActualOption = pVal;
		// Cover method to ensure valid  date
		if(selectedActualOption.equals("End of Day")==true) {
			setMilestoneActualDate(endOfDayTimeStamp(milestoneActualDate()));
		}
	}
	public String selectedPlannedOption() {
		return selectedPlannedOption;
	}
	public void setSelectedPlannedOption(String pVal) {
		selectedPlannedOption = pVal;
		// Cover method to ensure valid  date
		if(selectedPlannedOption.equals("End of Day")==true) {
			setMilestonePlannedDate(endOfDayTimeStamp(milestonePlannedDate()));
		}
	}
	
	*/
	public NSTimestamp endOfDayTimeStamp(NSTimestamp pTimeStamp) {
		NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);	
		return new NSTimestamp(pTimeStamp.yearOfCommonEra(), pTimeStamp.monthOfYear(), pTimeStamp.dayOfMonth(), 23, 59, 59, tz);
	}
	
	
    public void setPotentialMilestones(NSMutableArray newPotentialMilestones) {
        potentialMilestones = newPotentialMilestones;
    }

    /** @TypeInfo java.lang.String */
    public NSArray existingMilestones() {
		if(selectedRelease !=  null) {
			Session s = (Session)session();
			existingMilestones =  (NSArray)s.rowsForSql("select distinct(milestone) from MILESTONE_DATES where release_name = '" + selectedRelease + "'");
		}
        return existingMilestones;
    }
    public void setExistingMilestones(NSArray newExistingMilestones) {
        existingMilestones = newExistingMilestones;
    }

    public boolean isEditMode() {
        return isEditMode;
    }
    public void setIsEditMode(boolean newIsEditMode)  {
        isEditMode = newIsEditMode;
    }

}
