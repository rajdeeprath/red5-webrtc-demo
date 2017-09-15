package org.red5.core;

import org.red5.net.websocket.WebSocketPlugin;
import org.red5.net.websocket.WebSocketScope;
import org.red5.net.websocket.WebSocketScopeManager;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
//import org.slf4j.Logger;
import org.red5.server.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



public class Application extends ApplicationAdapter implements ApplicationContextAware {

	private static Logger log = LoggerFactory.getLogger(Application.class);
	
	private ApplicationContext applicationContext;

    
	
	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
	

	
   
	@Override
	public boolean appStart(IScope arg0) {
		
		WebSocketPlugin wsPlugin = (WebSocketPlugin) PluginRegistry.getPlugin("WebSocketPlugin");
        wsPlugin.setApplication(this);
        WebSocketScopeManager manager = wsPlugin.getManager(arg0);
        WebSocketScope defaultWebSocketScope = (WebSocketScope) applicationContext.getBean("webSocketScopeDefault");
        manager.addWebSocketScope(defaultWebSocketScope);
		
        
		return super.appStart(arg0);
	}

	
	
	@Override
	public void appStop(IScope arg0) {
		
		WebSocketScopeManager manager = ((WebSocketPlugin) PluginRegistry.getPlugin("WebSocketPlugin")).getManager(scope);
        manager.removeApplication(arg0);
        manager.stop();
        
		super.appStop(arg0);
	}
	


}
