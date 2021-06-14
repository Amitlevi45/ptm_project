package test;

public class Server {

	public interface ClientHandler {
		public void Initialize(int port);

		public void HandleClient();

		public void Finish();
	}

	volatile boolean stop;
	volatile int clientLimit;
	volatile int currentClientAmount;

	public Server() {
		stop = false;
		clientLimit = 3;
		currentClientAmount = 0;
	}

	private void startServer(int port, ClientHandler ch) {
		ch.Initialize(port);
		while (!stop && currentClientAmount <= clientLimit) {
			
			ch.HandleClient();
			currentClientAmount++;
		}

		stop();

		ch.Finish();
	}

	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(() -> startServer(port, ch)).start();
	}

	public void stop() {
		stop = true;
	}
}
