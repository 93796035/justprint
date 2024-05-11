package org.cas.client.platform.bar.net;

public class NetUtil {
	
    public static String fetchProductName(
            String description,
            String moneyLetter) {
    	if (description == null || description.trim().length() == 0) {
            return "";
        }
        int p = description.indexOf(moneyLetter);
        String html = p > 0 ? description.substring(0, p) : description;
        return trimContentFromHTML(html);
    }

    public static String fetchProductPrice(
            String description,
            String moneyLetter) {
        if (description == null || description.trim().length() == 0) {
            return "";
        }
        int p = description.lastIndexOf(moneyLetter);
        String html = p > 0 ? description.substring(p + 1) : description;
        return trimContentFromHTML(html);
    }

    // return the trimed content from html format text.
    public static String trimContentFromHTML(
            String html) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int start = html.indexOf("<");
            int end = html.indexOf(">");
            if (start < 0) {
                break;
            } else if (end > start) {
                sb.append(html.substring(0, start));
                html = html.substring(end + 1);
            }
        }

        html = sb.toString() + html;
        while (html.startsWith("&nbsp;")) {
            html = html.substring(6).trim();
        }
        while (html.endsWith("&nbsp;")) {
            html = html.substring(0, html.length() - 6).trim();
        }

        return html;
    }
    
    //if the www is missing, then add one.
    public static String validateURL(String url) {
    	if(url != null && url.trim().length() > 1 && !"null".equals(url.trim().toLowerCase())) {
        	StringBuilder protocal = new StringBuilder(url.startsWith("https://") ? "https://" : "http://");
        	
        	if(url.indexOf("//") > 0) {
        		url = url.substring(url.indexOf("//") + 2);	//remove the front end
        	}
        	if(url.indexOf("/") > 0) {
        		url = url.substring(0, url.indexOf("/"));		//remove the back end
        	}
	    	if(url.indexOf(".") < 0) {
	    		url = url + ".com";
	    	}
	    	if(url.indexOf(".") == url.lastIndexOf(".")) {
	    		url = "www." + url;
	    	}
	    	
	    	return protocal.append(url).append("/").toString();
        }else {
        	return url;
        }
	}
}
