package org.jboss.tools.esb.ui.bot.tests.examples;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.jboss.tools.ui.bot.ext.ExampleTest;
import org.jboss.tools.ui.bot.ext.SWTEclipseExt;
import org.jboss.tools.ui.bot.ext.SWTOpenExt;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.Timing;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.config.Annotations.Server;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerState;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerType;
import org.jboss.tools.ui.bot.ext.gen.ActionItem;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.NewObject.ESBESBFile;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.NewObject.ESBESBProject;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.NewObject.SeamSeamWebProject;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.NewObject.WebDynamicWebProject;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.Preference.RunDebugConsole;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.View.GeneralConsole;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import org.jboss.tools.ui.bot.ext.gen.ActionItem.NewObject.GeneralFolder;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.View.ServerServers;
import org.jboss.tools.ui.bot.ext.types.IDELabel;
import org.jboss.tools.ui.bot.ext.view.ProblemsView;
import org.jboss.tools.ui.bot.ext.view.ServersView;

/*
 * 
 * https://community.jboss.org/wiki/SmokeTestJBToolsServerSupport?prevContainerType=14
 * 
1) Open recent JBoss Tools
2) open servers view (window -> show view -> other -> Servers)
3) create new server
     - type: JBoss Community -> JBoss AS 6.0
     - use default configuration
      - finish wizard
4) double-click server in servers view to open the server editor
5) in editor, verify all server ports are accurate (JNDI / Web / JMX RMI) (right side of editor)
6) click "open launch configuration" in the editor, verify launch configuration arguments and vm args match with what is expected
   a) *** If there are any new arguments that have changed since the previous AS version, MAKE SURE the launch configuration HAS them!
7) In servers view, expand the server and look at XML Configuration/Ports, verify all labels have a number next to them
9) Create a project (seam or dynamic web is fine, seam project is better)
10) Deploy it to the server, Verify the deployment works
11) remove deployment, verify console shows deployment removed
12) Open MBean Viewer,
     a) note that the server can now be expanded,
     b) under it are mbeans. browse down to jboss.deployment -> URL ->DeploymentScanner,
     c) double-click DeploymentScanner, go to "Operations" page, click "stop" in viewer, click "stop" button on the right,
     d) verify operation completes successfully
13) in eclipse, deploy seam application again, verify console shows NO OUTPUT, deployment does NOT deploy
14) use mbean viewer / editor to execute start() operation on DeploymentScaner
15) verify console now accepts deployment
 */

@Require(server=@Server(type=ServerType.EAP,state=ServerState.Running))
public class SimpleEAPTest extends EAPExampleTest {
	
	public static String baseDir = null;  // "/opt/local/EAP6_ER41";
	public static String projectName = "DynWebProject";
	public static String projectDeployedText = "DynWebProject  [Started, Synchronized]";
	public static String serverName = "EAP-6.0";
	Hashtable<String, Integer> portTable = new Hashtable<String, Integer>();
	
	public boolean isTheAppDeployed (SWTBotTreeItem theItems[], String nameToMatch) {
		boolean retValue = false;				
		for (SWTBotTreeItem i : theItems)  {
			System.out.println (i.getText() + "[" + projectDeployedText + "]");
			if (i.getText().equals(nameToMatch)) {
				retValue = true;
			}
		}
		return retValue;
	}
	
	
	
	@Override
	public String getExampleName() {
		return "Helloworld";
	}
	@Override
	public String[] getProjectNames() {
		return new String[] {"jboss-as-helloworld", "jboss-as-helloworld"};
	}
	@Override
	protected void executeExample() {
		
		/* Ports referenced in the server's config */
		portTable.put("Web", 8080);
		portTable.put("Management", 9999);
		
		/* Test steps handled in constructor:
		   1) Open recent JBoss Tools
		   2) open servers view (window -> show view -> other -> Servers)
		   3) create new server
		    - type: JBoss Community -> JBoss AS 6.0
		    - use default configuration
		    - finish wizard
		   8) Start server, verify when server is up and running, the servers view says "Started" and not "Starting" */
		
		/* Test step: 4) double-click server in servers view to open the server editor */
		SWTBotView theSWTBotView = open.viewOpen(ServerServers.LABEL);
		SWTBotTree serverTree = bot.tree(0);		
		ServersView theServerView = new ServersView();
		SWTBotTreeItem theServer = theServerView.findServerByName(serverTree, serverName);	
		assertTrue("Found the EAP 6.0 server - and the name is correct: ", theServer.getText().equals("EAP-6.0  [Started, Synchronized]"));

		/* Test step: 5) in editor, verify all server ports are accurate (JNDI / Web / JMX RMI) (right side of editor) */
		theServer.doubleClick();
		Enumeration<String> ePortKeys = portTable.keys();
		while (ePortKeys.hasMoreElements()) {	
			String tempStr = ePortKeys.nextElement();
			assertTrue("The " + tempStr + " port should be " + portTable.get(tempStr),	bot.textWithLabel(tempStr).getText().equals(portTable.get(tempStr).toString()));		
		}

		/* Test steps:
		   6) click "open launch configuration" in the editor, verify launch configuration arguments and vm args match with what is expected 
		   a) *** If there are any new arguments that have changed since the previous AS version, MAKE SURE the launch configuration HAS them! */
		bot.hyperlink("Open launch configuration").click();
		
		bot.sleep(Timing.time3S());
			
		String tempStr1 = bot.textInGroup("Working directory:", 1).getText();
		int binStr = tempStr1.indexOf("/jboss-eap-6.0/bin");
		String baseDir = tempStr1.substring(0, binStr);
		
		//System.out.println (bot.textInGroup("Program &arguments:").getText() );
		assertTrue ("The Program arguments match ", 
				bot.textInGroup("Program &arguments:").getText().equals("-mp \"" + baseDir + "/jboss-eap-6.0/modules\" -jaxpmodule javax.xml.jaxp-provider " +
						"org.jboss.as.standalone -b localhost --server-config=standalone.xml ")); 

		//System.out.println (bot.textInGroup("VM ar&guments:").getText() );	
		// Need to convert these to a hash
		assertTrue ("The VM arguments match ", 
				bot.textInGroup("VM ar&guments:").getText().equals("-server -Xms64m -Xmx512m -XX:MaxPermSize=256m " +
						"-Djava.net.preferIPv4Stack=true " +
						"-Dorg.jboss.resolver.warning=true " +
						"-Dsun.rmi.dgc.client.gcInterval=3600000 " +
						"-Dsun.rmi.dgc.server.gcInterval=3600000 " +
						"-Djboss.modules.system.pkgs=org.jboss.byteman " +
						"-Djava.awt.headless=true " +
						"\"-Dorg.jboss.boot.log.file=" + baseDir + "/jboss-eap-6.0/standalone/log/boot.log\" " +
						"\"-Dlogging.configuration=file:" + baseDir + "/jboss-eap-6.0/standalone/configuration/logging.properties\" " +
						"\"-Djboss.home.dir=" + baseDir + "/jboss-eap-6.0\" "));
				
		bot.button("Cancel").click();
		
		/* Test step - 7) In servers view, expand the server and look at XML Configuration/Ports, verify all labels have a number next to them */
		theSWTBotView = open.viewOpen(ServerServers.LABEL);
		serverTree = bot.tree(0);		
		theServerView = new ServersView();
		theServer = theServerView.findServerByName(serverTree, serverName);
		theServer.expand();
		SWTBotTreeItem thePorts = theServer.expandNode("XML Configuration").getNode("Ports");
		ePortKeys = portTable.keys();
		while (ePortKeys.hasMoreElements()) {	
			String tempStr = ePortKeys.nextElement();
			assertTrue("The " + tempStr + " port should be " + portTable.get(tempStr), bot.textWithLabel(tempStr).getText().equals(portTable.get(tempStr).toString()));		
		}
	
		/* Test step - 9) Create a project (seam or dynamic web is fine, seam project is better) */
		theSWTBotView = open.viewOpen(ActionItem.View.GeneralNavigator.LABEL);		
		SWTBot wiz = open.newObject(ActionItem.NewObject.WebDynamicWebProject.LABEL);
		wiz.textWithLabel(WebDynamicWebProject.TEXT_PROJECT_NAME).setText(projectName);
		wiz.button(IDELabel.Button.NEXT).click();
		wiz.button(IDELabel.Button.NEXT).click();
		wiz.button("Finish").click();
		bot.sleep(Timing.time10S());
		bot.button("No").click();
		
		/* Test step - 10) Deploy it to the server, Verify the deployment works */
		theSWTBotView = open.viewOpen(ServerServers.LABEL);
		serverTree = theSWTBotView.bot().tree(0);		
		theServerView = new ServersView();

		theServerView.findServerByName(serverTree, serverName).contextMenu("Add and Remove...").click();
		SWTBotTree appTree = bot.tree(0);
		
		appTree.getTreeItem(projectName).select();
		bot.button ("Add >").click();	
		bot.sleep(Timing.time3S());
		
		/* There should only be one project deployed to the server */ 
		assertTrue (bot.tree(1).getAllItems().length == 1);
		assertTrue (projectName.equals(bot.tree(1).getTreeItem(projectName).getText()));
		bot.button("Finish").click();		
		bot.sleep(Timing.time10S());
		
		theSWTBotView = open.viewOpen(ServerServers.LABEL);
		serverTree = theSWTBotView.bot().tree(0);		
		theServerView = new ServersView();
		theServer = theServerView.findServerByName(serverTree, serverName);
		
		//SWTBotTreeItem [] theItems = theServer.getItems();
		assertTrue (isTheAppDeployed(theServer.getItems(), projectDeployedText));
		
		theSWTBotView = open.viewOpen(ServerServers.LABEL);
		serverTree = theSWTBotView.bot().tree(0);		
		theServerView = new ServersView();
		theServerView.findServerByName(serverTree, serverName).contextMenu("Add and Remove...").click();
		
		/* There should only be one project deployed to the server */ 
		assertTrue (bot.tree(1).getAllItems().length == 1);
		assertTrue (projectName.equals(bot.tree(1).getTreeItem(projectName).getText()));
		bot.button("Finish").click();
		
		/* Test step - 11) remove deployment, verify console shows deployment removed */
		SWTTestExt.servers.removeAllProjectsFromServer();		
		theServerView.findServerByName(serverTree, serverName).contextMenu("Add and Remove...").click();		
		SWTBotTree deployedAppTree = bot.tree(1);
		
		/* There should only no projects deployed to the server */ 
		assertFalse (isTheAppDeployed(theServer.getItems(), projectDeployedText));
		assertTrue (deployedAppTree.getAllItems().length == 0);
		bot.button("Finish").click();
		
//		12) Open MBean Viewer,
//		     a) note that the server can now be expanded,
//		     b) under it are mbeans. browse down to jboss.deployment -> URL ->DeploymentScanner,
//		     c) double-click DeploymentScanner, go to "Operations" page, click "stop" in viewer, click "stop" button on the right,
//		     d) verify operation completes successfully
//		13) in eclipse, deploy seam application again, verify console shows NO OUTPUT, deployment does NOT deploy
//		14) use mbean viewer / editor to execute start() operation on DeploymentScaner
//		15) verify console now accepts deployment
		
		bot.sleep(Timing.time10S());
		System.out.println("***End");
		
		SWTTestExt.servers.removeAllProjectsFromServer();
		// 16) stop server, verify server shuts down properly without error.
	}
}

//org.jboss.tools.ui.bot.ext.SWTUtilExt.displayAllBotWidgets(bot);