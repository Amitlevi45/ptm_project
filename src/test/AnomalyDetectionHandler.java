package test;

import java.net.*;
import java.io.*;

import test.Commands.DefaultIO;
import test.Server.ClientHandler;

public class AnomalyDetectionHandler implements ClientHandler{
	public ServerSocket server;
	public Socket curr_socket;
	public SocketIO sio;
	public boolean is_init;
	public CLI command_interface;
	public AnomalyDetectionHandler()
	{
		is_init = false;
	}
	
	public void Initialize(int port)
	{
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sio = new SocketIO();
		command_interface = new CLI(sio);
		is_init = true;
	}
	
	public void HandleClient()
	{
		try {
			curr_socket = server.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		sio.UpdateSocket(curr_socket);

		if (is_init)
		{
			command_interface.start();
		}
		
		try {
			curr_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Finish() {
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class SocketIO implements DefaultIO{
		public BufferedReader in;
		public PrintWriter out;
		public SocketIO() {}
		
		public void UpdateSocket(Socket socket)
		{
			
			try {
				in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out=new PrintWriter(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String readText()
		{
			try {
				return in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		}

		public void write(String text)
		{
			out.print(text);
			out.flush();
		}

		public float readVal()
		{
			try {
				return in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return 0;
			}
		}

		public void write(float val)
		{
			out.print(String.valueOf(val));
			out.flush();
		}
	}
}
