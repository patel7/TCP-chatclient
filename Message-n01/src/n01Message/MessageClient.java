package n01Message;
import java.net.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.io.*;

public class MessageClient {
	
	private Scanner s;
	private String host;
	private int port;
	private boolean hosting;
	private BufferedReader sockReader;
	private PrintWriter sockWriter;
	
	public MessageClient() {
		
		s = new Scanner(System.in);
		
		while(true) {
			System.out.println("Would you like to host or join?");
			String line = s.nextLine();
			if(line.toLowerCase().equals("host")) {
				hosting = true;
				break;
			} else if(line.toLowerCase().equals("join")) {
				hosting = false;
				break;
			}
		}
		
		if(hosting) {

			while((port = portPrompter(s,"Listening Port: ")) == -1) {	
			}
			
			try {
				
				ServerSocket serverSocket = new ServerSocket(port);
				Socket socket = serverSocket.accept();
				serverSocket.close();
				sockReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				sockWriter = new PrintWriter(socket.getOutputStream(), true);
				
				CompletableFuture.runAsync(this::listenForMessages);
				
				while(s.hasNextLine()) {
					sockWriter.println(s.nextLine());
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} else {
			
			System.out.print("Hostname: ");
			host = s.nextLine();
			
			while((port = portPrompter(s,"Port: ")) == -1) {	
			}
			
			try {
				
				Socket socket = new Socket(host, port);
				sockReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				sockWriter = new PrintWriter(socket.getOutputStream(), true);
				System.out.println("Successfully connected.\n");
				
				CompletableFuture.runAsync(this::listenForMessages);
				
				while(s.hasNextLine()) {
					sockWriter.println(s.nextLine());
				}
				
			} catch (ConnectException e) {
				System.out.println("Failed to connect to \"" + host + "\".");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void listenForMessages() {
		
		try {
			String receivedLine;
			receivedLine = sockReader.readLine();
			while(receivedLine != null) {
				System.out.println(receivedLine);
				receivedLine = sockReader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private int portPrompter(Scanner s, String prompt) {
		int port;
		System.out.print(prompt);
		port = s.nextInt();
		if(port < 1024 || port > 65535) {
			System.out.println("Invalid port. Valid ports: 1024 - 65535.");
			return -1;
		} else {
			return port;
		}
	}
}
