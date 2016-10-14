package org.red5.core;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.logging.Red5LoggerFactory;
import org.red5.net.websocket.WebSocketConnection;
import org.red5.net.websocket.listener.WebSocketDataListener;
import org.red5.net.websocket.model.WSMessage;
import org.slf4j.Logger;

import com.google.gson.Gson;

public class WebSocketDataHandler extends WebSocketDataListener {
	
	private static Logger logger = Red5LoggerFactory.getLogger(WebSocketDataHandler.class);
	private static ConcurrentHashMap<String, WebSocketConnection> connections = new ConcurrentHashMap<String, WebSocketConnection>();
	private static ConcurrentHashMap<String, List<String>> roomMap = new ConcurrentHashMap<String, List<String>>();
	
	{
		setProtocol(null);
	}

	@Override
	public void onWSConnect(WebSocketConnection arg0) {
		// TODO Auto-generated method stub
		String username = null;
		String roomname = null;
		
		try
		{
			username = getUserName(arg0);
			roomname = getRoomName(arg0);
			
			if(username == null || username.equals(""))
			throw new Exception("Unable to login. Invalid username");
			
			if(roomname == null || roomname.equals(""))
			throw new Exception("Unable to login. Invalid roomname");
			
			if(connections.containsKey(username))
			throw new Exception("Unable to login. This user name is already taken");
			
			addToRoom(roomname, username);
			connections.put(username, arg0);
		}
		catch(Exception e)
		{
			arg0.close();
			logger.error(e.getMessage());
		}
	}
	
	

	@Override
	public void onWSDisconnect(WebSocketConnection arg0) {
		// TODO Auto-generated method stub
		// gets called a little late
	}
	
	

	@Override
	public void onWSMessage(WSMessage arg0) {
		// TODO Auto-generated method stub
		
		String path = arg0.getPath();
		WebSocketConnection sender = arg0.getConnection();
		String messageRaw = null;
		Message incoming = null;
		Message outgoing = null;
		String username = getUserName(sender);
		String roomname = getRoomName(sender);
		
		logger.info("Message Type = " + arg0.getMessageType().name());
		
		switch(arg0.getMessageType())
		{
			case TEXT:
			try 
			{
				Gson gson = new Gson();
				messageRaw = arg0.getMessageAsString();
				incoming =  gson.fromJson(messageRaw, Message.class);
				outgoing = new Message(); 
				outgoing.timestamp = System.currentTimeMillis();
				outgoing.sender = username;
				
				if(incoming.type.equalsIgnoreCase("offer"))
				{
					outgoing.type = incoming.type;
				}
				else if(incoming.type.equalsIgnoreCase("answer"))
				{
					outgoing.type = incoming.type;;
				}
				else if(incoming.type.equalsIgnoreCase("leave"))
				{
					outgoing.type = incoming.type;
				}
				else if(incoming.type.equalsIgnoreCase("candidate"))
				{
					outgoing.type = incoming.type;
				}
				else if(incoming.type.equalsIgnoreCase("arbitrary"))
				{
					outgoing.type = incoming.type;
				}
				else
				{
					outgoing.type = "unknown";
					sender.send(gson.toJson(new Message(outgoing.type, username, "error", System.currentTimeMillis())));
					return;
				}
				
				outgoing.content = incoming.content;
				WebSocketConnection receiver = getReceieverConnection(roomname, username);
				if(receiver != null)
				receiver.send(gson.toJson(outgoing));
			} 
			catch (UnsupportedEncodingException e) 
			{
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
			case BINARY:
			break;
			
			case CLOSE:
			try
			{
				removeFromRoom(roomname, username);
			}
			catch(Exception e)
			{
				logger.error(e.getMessage());
			}
			break;
			
			case CONTINUATION:
			break;
			
			case PING:
			break;
			
			case PONG:
			break;
			
			default:
			break;
		}
	}

	
	public String getUserName(WebSocketConnection conn)
	{
		Map<String, Object> map = getQueryParameters(conn);
		String username = (String) map.get("username");
		return username;
	}
	
	
	public String getRoomName(WebSocketConnection conn)
	{
		Map<String, Object> map = getQueryParameters(conn);
		String room = (String) map.get("room");
		return room;
	}
	
	
	
	private Map<String, Object> getQueryParameters(WebSocketConnection conn)
	{
		Map<String, Object> map = conn.getQuerystringParameters();
		Map<String, Object> sanitized = new HashMap<String, Object>();
		
		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Object> pair = (Map.Entry<String, Object>)it.next();
	        
	        String key = pair.getKey();
	        Object value = pair.getValue();
	        key = key.replace("?", "");	        		
	        
	        sanitized.put(key, value);
	    }
	    
	    return sanitized;
	}
	
	
	public void addToRoom(String room, String username) throws Exception
	{
		List<String> users;
		
		if(roomMap.containsKey(room))
		{
			users = roomMap.get(room);
			
			if(users.contains(username))
			throw new Exception("This room already contains a user by name " + username);
		}
		else
		{
			users = Collections.synchronizedList(new ArrayList<String>());
		}
		
		if(users.size() >=2)
		throw new Exception("No more than 2 users per room");
		
		users.add(username);
		roomMap.put(room, users);
	}
	
	
	
	
	
	public void removeFromRoom(String room, String username) throws Exception
	{
		List<String> users;
		
		if(roomMap.containsKey(room))
		{
			users = roomMap.get(room);
			
			Iterator<String> iter = users.iterator();

			while (iter.hasNext()) {
			    String user = iter.next();
			    if (user.equalsIgnoreCase(username))
			        iter.remove();
			    	removeConnection(username);
			}
		}
		else
		{
			throw new Exception("Room not found");
		}
		
		
		if(users.size() == 0)
		roomMap.remove(room);
		else
		roomMap.put(room, users);
	}
	
	
	
	
	public List<String> getRoomUsers(String room) throws Exception
	{
		List<String> users;
		
		if(roomMap.containsKey(room))
		{
			users = roomMap.get(room);
		}
		else
		{
			throw new Exception("Room not found");
		}
		
		
		if(users.size() > 0)
		return users;
		else
		return null;
	}
	
	
	
	
	public WebSocketConnection getReceieverConnection(String room, String me) throws Exception
	{
		List<String> users;
		
		if(roomMap.containsKey(room))
		{
			users = roomMap.get(room);
			
			
			Iterator<String> usersIt = users.iterator();
			while (usersIt.hasNext()) {
				String key = usersIt.next();
				if(!key.equalsIgnoreCase(me)){
					WebSocketConnection conn = connections.get(key);
					return conn;
				}
			}
			
			return null;
		}
		else
		{
			throw new Exception("Room not found");
		}
	}
	
	
	
	public void removeConnection(String username)
	{
		
		for (Iterator<Entry<String, WebSocketConnection>> iter = connections.entrySet().iterator(); iter.hasNext(); ) {
		    Entry<String, WebSocketConnection> entry = iter.next();
		    
		    if (entry.getKey().equalsIgnoreCase(username))
		        iter.remove(); // <----- remove using the iterator position!
		}
		
		return;
	}
	
	
	public void notifyEvent(String room, String event, Object data)
	{
		try
		{
			// send room join notification to all in this room
			List<String> users = getRoomUsers(room);
			if(users != null){
				Gson gson = new Gson();
				for(String user : users)
				{
					WebSocketConnection conn = connections.get(user);
					Message m = new Message("arbitrary", "server", new InnerMessage(event, data), System.currentTimeMillis());
					conn.send(gson.toJson(m));
				}
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
	}
}
