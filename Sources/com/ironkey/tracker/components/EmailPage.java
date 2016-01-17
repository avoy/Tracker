package com.ironkey.tracker.components;

import com.ironkey.tracker.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.javamail.*;

public class EmailPage extends WOComponent {
	private static final long serialVersionUID = 1L;
	protected WOComponent componentToEmail;
    protected WOComponent nextPage;
    protected String htmlContent;
    protected String plainTextContent;
    protected String emailAddress;
    protected NSArray<String> toAddresses;
    protected String toName;
    protected String subject = "-- Tracker Report --";
    protected String from = "kevinavoy@ooyala.com";
    protected String fromName = "Tracker";

    public EmailPage(WOContext aContext) {
        super(aContext);
		//System.out.println( "EmailPage.EmailPage()");

    }
    public void awake() {
		//System.out.println( "EmailPage.awake()");

    }

    public WOComponent nextPage() {
		//System.out.println( "EmailPage.nextPage()");
	
        return nextPage;
    }
    public void setNextPage(WOComponent theComponent) {
		//System.out.println( "EmailPage.setNextPage()");
	
        nextPage = theComponent;
    }
    public WOComponent componentToEmail() {
		//System.out.println( "EmailPage.componentToEmail()");
	
        return componentToEmail;
    }
    public void setComponentToEmail(WOComponent theComponent) {
		//System.out.println( "EmailPage.setComponentToEmail()");
	
        componentToEmail = theComponent;
    }
    public String emailAddress() {
		//System.out.println( "EmailPage.emailAddress()");
	
        if(emailAddress == null) {
            Session s = (Session)session();
            EOEnterpriseObject u = (EOEnterpriseObject)s.getUser();
            if(u != null) {
                emailAddress = (String)u.valueForKey("loginName");
            }
            else {
                emailAddress = "kavoy@marblecloud.com";
            }
        }
        return emailAddress;
    }
    public void setEmailAddress(String newEmailAddress) {
		//System.out.println( "EmailPage.setEmailAddress()");

        emailAddress = newEmailAddress;
    }
/*   WOMailDelivery implementation
	
	
	public WOComponent sendPage() {
		try {
			//System.out.println( "EmailPage.sendPage()");
			
			// fromAddress with optional fromPersonalName
			if ( from == null ) {
				setFrom("kavoy@marblecloud.com");
			}
			
			//  toAddresses (NSArray)
			if ( toAddresses() == null ) {
				setToAddresses( new NSArray("kavoy@marblecloud.com"));
			}
			
			// reply to address
			//if ( replyToAddress != null ) eMail.setReplyToAddress( replyToAddress );

			if ( subject == null ) {
				subject = "Default Subject";
			}
			System.out.println("sending email");
			if(componentToEmail() != null) {
			

				//WOResponse resp = componentToEmail().generateResponse();

				//NSData data = resp.content();
				//System.out.println("data - " + new String(data.bytes()));
				
				WOMailDelivery.sharedInstance().composeComponentEmail(from(),toAddresses(), null, subject, (WOComponent)componentToEmail(), WOMailDelivery.SEND_NOW );

			}
			else if(plainTextContent() != null) {
				WOMailDelivery.sharedInstance().composePlainTextEmail(from(),toAddresses(), null, subject, plainTextContent(), WOMailDelivery.SEND_NOW );
			}
			else {
				WOMailDelivery.sharedInstance().composePlainTextEmail(from(),toAddresses(), null, subject, "No email body text provided...", WOMailDelivery.SEND_NOW );
			}
			System.out.println("Mail Sent");

		} catch (Exception e) {
			System.err.println("Exception sending email: " + e);

		}
		return nextPage();
	}

*/

/* ERMailDeliveryHTML implementation */
	public WOComponent sendPage() {
		try {
			//System.out.println( "EmailPage.sendPage()");
			
			// Create a new mail delivery instance
			ERMailDeliveryHTML eMail = new ERMailDeliveryHTML();

			// Set the WOComponent to be used for rendering the mail
			if(componentToEmail() != null) {
				eMail.setComponent( componentToEmail() );
			}
			else {
				if(htmlContent() != null) {
					eMail.setHTMLContent( htmlContent() );
				}
				if(plainTextContent() != null) {
					eMail.setHiddenPlainTextContent( plainTextContent() );
				}
			}
			eMail.newMail();

			// fromAddress with optional fromPersonalName
			if ( from != null && fromName != null ) {
				eMail.setFromAddress( from, fromName );
			} else if (from != null) {
				eMail.setFromAddress( from );
			}
			
			// optional toAddress and optional toPersonalName
			if ( emailAddress != null && toName() != null ) {
				eMail.setToAddress( emailAddress, toName() );
			} else if (emailAddress != null) {
				eMail.setToAddress( emailAddress );
			}
			
			// optional toAddresses (NSArray)
			if ( toAddresses() != null ) {
				eMail.setToAddresses( toAddresses() );
			}
			
			// reply to address
			//if ( replyToAddress != null ) eMail.setReplyToAddress( replyToAddress );

			if ( subject != null ) {
				eMail.setSubject( subject );
			}
			//System.out.println( " er.javamail.defaultEncoding: " + System.getProperty( "er.javamail.defaultEncoding"));
			//System.out.println( " er.javamail.smtpHost: " + System.getProperty( "er.javamail.smtpHost"));
			System.out.println("EmailPage.sendPage() - sending email");
			eMail.sendMail();
			System.out.println("EmailPage.sendPage() - mail Sent");
			
		} catch (Exception e) {
			System.err.println("Exception sending email: " + e);
			System.err.println( "At sendPage Exception, er.javamail.centralize: " + System.getProperty( "er.javamail.centralize") + " and er.javamail.adminEmail: " + System.getProperty ( "er.javamail.adminEmail") ); 		
		}
		return nextPage();
	}

	
	// Accessors
	public String subject() { return subject;}
    public void setSubject(String aString) { subject = aString;}
	public String from() { return from;}
    public void setFrom(String aString) { from = aString;}
	public String toName() { return toName;}
    public void setToName(String aString) { toName = aString;}
	public String htmlContent() { return htmlContent;}
    public void setHtmlContent(String aString) { htmlContent = aString;}
	public String plainTextContent() { return plainTextContent;}
    public void setPlainTextContent(String aString) { plainTextContent = aString;}
	public NSArray<String> toAddresses() { return toAddresses;}
    public void setToAddresses(NSArray<String> anArray) { toAddresses = anArray;}

}
