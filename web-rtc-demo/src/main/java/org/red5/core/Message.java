package org.red5.core;

public class Message {

	String type;
	
	String sender;
	
	Object content;
	
	Long timestamp;
	
	
	public Message(String type, String sender, Object content, Long timestamp)
	{
		this.type = type;
		this.sender = sender;
		this.content = content;
		this.timestamp = timestamp;		
	}
	
	public Message()
	{
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
}
