package org.red5.core;

public class InnerMessage {
	
	public String subject;
	
	public Object data;
	
	
	public InnerMessage(String subject, Object data){
		this.subject = subject;
		this.data = data;
	}
	
	public InnerMessage(){
		
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	
	

}
