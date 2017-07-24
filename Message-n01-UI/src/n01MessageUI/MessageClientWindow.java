package n01MessageUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class MessageClientWindow extends ApplicationWindow {
	private Text sendBox;
	private Text messageBox;
	private BufferedReader sockReader;
	private PrintWriter sockWriter;
	
	/**
	 * Create the application window.
	 * @wbp.parser.constructor
	 */
	public MessageClientWindow() {
		super(null);
		setShellStyle(SWT.BORDER | SWT.CLOSE);
		createActions();
		// addToolBar(SWT.FLAT | SWT.WRAP);
		// addMenuBar();
		// addStatusLine();
	}
	
	public MessageClientWindow(BufferedReader sockReader, PrintWriter sockWriter) {
		this();
		this.sockReader = sockReader;
		this.sockWriter = sockWriter;
	}

	protected void setSockReader(BufferedReader sockReader) {
		this.sockReader = sockReader;
	}
	
	protected void setSockWriter(PrintWriter sockWriter) {
		this.sockWriter = sockWriter;
	}
	
	protected void acceptMessages() {
		CompletableFuture.runAsync(this::listenForMessages);
		
	}
	
	protected void listenForMessages() {
		try {
			String receivedLine;
			receivedLine = sockReader.readLine();
			while(receivedLine != null) {
				System.out.println("Recieved message");
				asyncUpdate(receivedLine);
				receivedLine = sockReader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void asyncUpdate(final String receivedLine) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				messageBox.append(receivedLine + "\n");
				// messageBox.setText(receivedLine);
				messageBox.getParent().layout();
			}
		});
	}
	
	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		setStatus("");
		Composite container = new Composite(parent, SWT.NONE);
		{
			sendBox = new Text(container, SWT.BORDER);
			sendBox.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR) {
						System.out.println("Enter pressed");
						sockWriter.println(sendBox.getText());
						messageBox.append(sendBox.getText() + "\n");
						sendBox.setText("");
					}
				}
			});
			sendBox.setBounds(10, 231, 430, 27);
		}
		{
			messageBox = new Text(container, SWT.BORDER | SWT.MULTI);
			messageBox.setEditable(false);
			messageBox.setBounds(10, 10, 430, 215);
		}

		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}
	
	public static void main(String args[]) {
		try {
			MessageClientWindow window = new MessageClientWindow();
			HostJoinDialog hostJoinDialog = new HostJoinDialog(window.getShell());
			InputDialog hostnamePrompt = new InputDialog(window.getShell(), "Hostname", "Please enter a host to connect to.", "", new HostValidator());
			
			
			window.setBlockOnOpen(true);
			hostJoinDialog.setBlockOnOpen(true);
			hostnamePrompt.setBlockOnOpen(true);
			
			
			int hosting = hostJoinDialog.open();
			int port;
			String hostname;
			
			Socket socket;
			BufferedReader sockReader;
			PrintWriter sockWriter;
			
			if(hosting == 1) {
				InputDialog portPrompt = new InputDialog(window.getShell(), "Listening Port", "Please enter a port number to listen on.", "0", new PortValidator());
				portPrompt.setBlockOnOpen(true);
				
				portPrompt.open();
				port = Integer.parseInt(portPrompt.getValue());
				
				ServerSocket serverSocket = new ServerSocket(port);
				socket = serverSocket.accept();
				serverSocket.close();

			} else {
				hostnamePrompt.open();
				hostname = hostnamePrompt.getValue();
				InputDialog portPrompt = new InputDialog(window.getShell(), "Connection Port", "Please enter a port number to connect to.", "0", new PortValidator());
				portPrompt.setBlockOnOpen(true);
				portPrompt.open();
				port = Integer.parseInt(portPrompt.getValue());
				socket = new Socket(hostname, port);
			}
			
			sockReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sockWriter = new PrintWriter(socket.getOutputStream(), true);
			
			window.setSockReader(sockReader);
			window.setSockWriter(sockWriter);
			window.acceptMessages();
			window.open();
			
			Display.getCurrent().dispose();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(new Point(1, 1));
		super.configureShell(newShell);
		newShell.setText("n01's Messaging Service");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
}
