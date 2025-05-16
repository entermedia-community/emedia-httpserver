package org.entermediadb.httpserver.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Cookie extends javax.servlet.http.Cookie {
    private String domain;
    private String path;
    private Date expires;
    private int maxAge = -1;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;

    public Cookie(String name, String value) {
        super(name, value);
        setPath("/");
    }

    public String getDomain() { return domain; }
    public String getPath() { return path; }
    public Date getExpires() { return expires; }
    public int getMaxAge() { return maxAge; }
    public boolean isSecure() { return secure; }
    public boolean isHttpOnly() { return httpOnly; }
    public String getSameSite() { return sameSite; }

    public void setDomain(String domain) { this.domain = domain; }
    public void setPath(String path) { this.path = path; }
    public void setExpires(Date expires) { this.expires = expires; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }
    public void setSecure(boolean secure) { this.secure = secure; }
    public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }
    public void setSameSite(String sameSite) { this.sameSite = sameSite; }

    public String toSetCookieHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("=").append(getValue());
        
        if (domain != null) {
            sb.append("; Domain=").append(domain);
        }
        if (path != null) {
            sb.append("; Path=").append(path);
        }
        if (expires != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            sb.append("; Expires=").append(sdf.format(expires));
        }
        if (maxAge != -1) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (isSecure()) {
            sb.append("; Secure");
        }
        if (isHttpOnly()) {
            sb.append("; HttpOnly");
        }
        if (sameSite != null) {
            sb.append("; SameSite=").append(sameSite);
        }
        return sb.toString();
    }
} 