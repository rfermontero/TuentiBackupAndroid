package com.colymore.fototuenti;

public class Cookie {

	private String cookiename;
	private String pid;
	private String tuentiemail;
	private String expires;
	private String path;
	private String domain;
	private String mid;
	private String lang;
	private String screen;

	public Cookie(String pid, String tuentiemail, String expires, String path,
			String domain, String mid, String lang) {

		super();

		this.cookiename = String.valueOf(1);
		this.pid = pid;
		this.tuentiemail = tuentiemail;
		this.expires = expires;
		this.path = "/";
		this.domain = ".tuenti.com";
		this.mid = mid;
		this.lang = lang;
		this.screen = "1920-1080-1920-1040-1-20.74";

	}

	public String getCookie() {

		String cookie = "cookiename=" + this.cookiename + "; tuentiemail="
				+ this.tuentiemail + "; expires=" + this.expires + "; path="
				+ this.path + "; domain=" + this.domain + "; mid=" + this.mid
				+ "; lang=" + this.lang + "; screen=" + this.screen;
		return cookie;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getTuentiemail() {
		return tuentiemail;
	}

	public void setTuentiemail(String tuentiemail) {
		this.tuentiemail = tuentiemail;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getScreen() {
		return screen;
	}

	public void setScreen(String screen) {
		this.screen = screen;
	}

}
