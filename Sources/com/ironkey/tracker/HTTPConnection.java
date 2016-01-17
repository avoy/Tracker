package com.ironkey.tracker;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.StringReader;
import javax.net.ssl.SSLSocketFactory;
import javax.net.SocketFactory;
import java.security.Security;
import java.io.StringWriter;
import java.util.Vector;
import java.util.Enumeration;

/**
* Title:        HTTPConnection
* Description:  Class for making http connections (GET/POST)
* Copyright:    Copyright (c) 2007
* Company:      IronKey
* @author
* @version 1.0
*/

public class HTTPConnection {
    protected String host;
    protected String port = "80";
    protected String protocol;
    protected String requestPath;
    protected String requestMethod = "GET";
    protected String acceptString = "text/xml,application/xml,application/xhtml+xml, text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
    //protected String userAgent = "Mozilla/5.0 (compatible; MSIE 5.01; Windows NT 5.0)";
    protected String userAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.12) Gecko/20060130 Red Hat/1.7.12-1.1.3.4";
    protected String contentType = "application/x-www-form-urlencoded";
    protected String referer = "http://bugzilla/enter_bug.cgi";
    protected String cookie;
    protected Vector<String> cookies;
    protected boolean followRedirect = false;
    protected StringBuffer postData;
    protected StringBuffer response;
    protected HttpURLConnection connection;
    private boolean debugFlag = false;
    protected StringWriter debugOut;     // Output stream
    protected boolean http11 = false;

    public HTTPConnection() {
        response = new StringBuffer();
        cookies = new Vector<String>();
    }

    public StringBuffer connect() throws Exception {
        String protocolVersion = "1.0";
        Socket aSocket = null;
        DataOutputStream out;
        BufferedReader br;
        StringBuffer requestBuffer;
        String responseString;
        SocketFactory sf;

        try {
            if(http11() == true) {
                protocolVersion = "1.1";
            }
            if(protocol.equals("https")) {
                //System.out.println("protocol - " + protocol);
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                //System.setProperty("javax.net.ssl.trustStore", "C:/JBuilder4/jdk1.3/jre/lib/security/cacerts");
                //System.setProperty("javax.net.ssl.trustStore", "/Users/kevin/Documents/jssecacerts");
                sf = SSLSocketFactory.getDefault();
                aSocket = sf.createSocket(host(), Integer.parseInt(port()));
                //aSocket = new Socket(host(), Integer.parseInt(port())); // hack

            }
            else {
                aSocket = new Socket(host(), Integer.parseInt(port()));
            }
        }
        catch (IOException e) {
            System.err.println("HttpConnection.socketConnect1() - " + e);
            e.printStackTrace();
            throw e;
        }
        try {
            out = new DataOutputStream(aSocket.getOutputStream());
            if(out != null) {
                requestBuffer = new StringBuffer();

                if(requestMethod().equals("GET")) {
                    //System.out.println("requestPath - " + requestPath());
                    requestBuffer.append("GET " + requestPath() + " HTTP/" + protocolVersion + "\r\n");
                    requestBuffer.append("Host: " + host() + "\r\n");
                    requestBuffer.append("User-Agent: " + userAgent() + "\r\n");
                    requestBuffer.append("Accept: " + acceptString() + "\r\n");
                    requestBuffer.append("Accept-Language: " + "en-us,en;q=0.5" + "\r\n");
                    requestBuffer.append("Accept-Encoding: " + "gzip,deflate" + "\r\n");
                    requestBuffer.append("Accept-Charset: " + "ISO-8859-1,utf-8;q=0.7,*;q=0.7" + "\r\n");
                    requestBuffer.append("Keep-Alive: 300" + "\r\n");
                    requestBuffer.append("Connection: keep-alive" + "\r\n");
                    requestBuffer.append("Referer: " + referer + "\r\n");

                    Enumeration<String> enumer = cookies.elements();
                    while(enumer.hasMoreElements()) {
                        String aCookie = (String)enumer.nextElement();
                        requestBuffer.append("Cookie: " + aCookie + "\r\n");
                    }
                    requestBuffer.append("\r\n");
                }
                else if(requestMethod().equals("POST")) {
                    requestBuffer.append("POST " + requestPath() + " HTTP/" + protocolVersion + "\r\n");
                    requestBuffer.append("Host: " + host() + "\r\n");
                    requestBuffer.append("User-Agent: " + userAgent() + "\r\n");
                    requestBuffer.append("Accept: " + acceptString() + "\r\n");
                    requestBuffer.append("Accept-Language: " + "en-us,en;q=0.5" + "\r\n");
                    requestBuffer.append("Accept-Encoding: " + "gzip,deflate" + "\r\n");
                    requestBuffer.append("Accept-Charset: " + "ISO-8859-1,utf-8;q=0.7,*;q=0.7" + "\r\n");
                    requestBuffer.append("Keep-Alive: 300" + "\r\n");
                    requestBuffer.append("Connection: keep-alive" + "\r\n");
                    requestBuffer.append("Referer: " + referer + "\r\n");

                    Enumeration<String> enumer = cookies.elements();
                    while(enumer.hasMoreElements()) {
                        String aCookie = (String)enumer.nextElement();
                        requestBuffer.append("Cookie: " + aCookie + "\r\n");
                    }
                    requestBuffer.append("Content-Type: " + contentType() + "\r\n");
                    requestBuffer.append("Content-Length: " + postData().length() + "\r\n\r\n");

                    // Append the content
                    requestBuffer.append(postData());
                }
                if(debugFlag == true) {
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("Request \n" + requestBuffer.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
                    if(debugOut != null) {
                        debugOut.write("Request - \n" + requestBuffer.toString() + "\n");
                    }
                }

                // Write the content
                out.writeBytes(requestBuffer.toString());
                out.flush();

                // Retrieve response
                try {
                    boolean responseBegin = false;
                    br = new BufferedReader(new InputStreamReader(aSocket.getInputStream()));
                    while ((responseString = br.readLine()) != null) {
                        //System.out.println("responseString - " + responseString);
                        if(responseBegin == false) {
                            aSocket.setSoTimeout(5000);  // This is used to force the connection to timeout if it does not send an EOF
                            responseBegin = true;
                        }
                        response.append(responseString + "\n");
                        if(http11 == true) {
                            if(responseString.equals("0000")) { // terminate the socket if chunked data is complete
                                break;
                            }
                        }
                    }
                }
                catch(java.io.InterruptedIOException e) {
                    //System.out.println("Response - \n" + response.toString());
                    System.err.println("Socket was not responding");
                }
                if(debugFlag == true) {
                    System.out.println("===========================================");
                    System.out.println("Response - \n" + response.toString());
                    System.out.println("===========================================");
                    if(debugOut != null) {
                        debugOut.write("Response - \n" + response.toString());
                    }
                }
            }
            aSocket.close();
        }
        catch (IOException e) {
            System.err.println("HttpConnection.socketConnect()2 - " + e);
            e.printStackTrace();
            throw e;
        }

        return response;
    }

    public String baseURLString() {
        //System.out.println(protocol() + "://" +  host());
        return protocol() + "://" +  host();
    }
    public Vector<String> cookieFromResponse() {
        String responseLine;
        Vector<String> cookies = new Vector<String>();
        String cookieString = null;
        if(connection() == null) {  // it was a socket connection
            BufferedReader br = new BufferedReader(new StringReader(response.toString()));
            try {
                while ((responseLine = br.readLine()) != null) {
                    if(responseLine.equals("\r\n\r\n")) {
                        break;  // not sure if this is correct
                    }
                    String stringToFind = "Set-Cookie: ";
                    int cookieIndex = responseLine.indexOf(stringToFind);
                    if(cookieIndex > -1) {
                        cookieString = responseLine.substring(stringToFind.length());
                        if(cookieString != null) {
                            cookies.add(cookieString);
                            //break;
                        }
                    }
                }
            }
            catch (IOException e) {
                System.err.println("LoginLogoff2.login() - " + e);
            }
        }
        else { // http url connection
            cookieString = (String)connection().getHeaderField("Set-Cookie");
        }
        return cookies;
    }

    public int status() {
        String status = null;
        String responseLine;
        if(connection() == null) {  // it was a socket connection
            BufferedReader br = new BufferedReader(new StringReader(response.toString()));
            try {
                responseLine = br.readLine();  // first line contains status
                status = responseLine.substring(9,12);
            }
            catch (IOException e) {
                System.err.println("HTTPConnection.status() - " + e);
            }
        }
        else { // http url connection
            try{
                int s = connection().getResponseCode();
                status = "" + s;
            }
            catch(IOException e) {
                System.err.println("HTTPConnection.status() - " + e);
            }
        }
        return Integer.parseInt(status);
    }

    public String location() {
        String responseLine;
        String locationString = null;
        if(connection() == null) {  // it was a socket connection
            BufferedReader br = new BufferedReader(new StringReader(response.toString()));
            try {
                while ((responseLine = br.readLine()) != null) {
                    String stringToFind = "Location: ";
                    int locIndex = responseLine.indexOf(stringToFind);
                    if(locIndex > -1) {
                        locationString = responseLine.substring(stringToFind.length());
                        if(locationString != null) {
                            break;
                        }
                    }
                }
            }
            catch (IOException e) {
                System.err.println("LoginLogoff2.login() - " + e);
            }
        }
        else { // httpurlconnection
            locationString = (String)connection().getHeaderField("Location");
        }
        return locationString;
    }

    public String host() {return host;}
    public void setHost(String theHost) {host = theHost;}
    public String port() {return port;}
    public void setPort(String thePort) {port = thePort;}
    public String protocol() {return protocol;}
    public void setProtocol(String theProtocol) {protocol = theProtocol;}
    public String requestPath() {return requestPath;}
    public void setRequestPath(String thePath) {requestPath = thePath;}
    public String requestMethod() {return requestMethod;}
    public void setRequestMethod(String theMethod) {requestMethod = theMethod;}
    public String acceptString() {return acceptString;}
    public void setAcceptString(String theAccept) {acceptString = theAccept;}
    public String userAgent() {return userAgent;}
    public void setUserAgent(String theAgent) {userAgent = theAgent;}
    public String contentType() {return contentType;}
    public void setContentType(String theType) {contentType = theType;}
    //public String cookie() {return cookie;}
    //public void setCookie(String theCookie) {cookies.add(theCookie);}
    public void addCookie(String theCookie) {cookies.add(theCookie);}
    public boolean followRedirect() {return followRedirect;}
    public void setFollowRedirect(boolean theValue) {followRedirect = theValue;}
    public StringBuffer response() {return response;}
    public void setResponse(StringBuffer theResponse) {response = theResponse;}
    public StringBuffer postData() {return postData;}
    public void setPostData(StringBuffer theData) {postData = theData;}
    public void setPostData(String theData) {if(postData==null){postData = new StringBuffer();}postData.append(theData);}
    public HttpURLConnection connection() {return connection;}
    public void setConnection(HttpURLConnection theConnection) {connection = theConnection;}
    public boolean debugFlag() {return debugFlag;}
    public void setDebugFlag(boolean theValue) {debugFlag = theValue;}
    public StringWriter debugOut() {return debugOut;}
    public void setDebugOut(StringWriter theValue) {debugOut = theValue;}
    public void setHttp11(boolean theValue) { http11 = theValue; }
    public boolean http11() { return http11;}

    }

