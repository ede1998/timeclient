import java.util.ArrayList;
import java.io.*;
import java.net.*;

public class Server {
	public static ArrayList<PeerData> peers = new ArrayList<PeerData>();
	public static void main(String... args) {
		try {
		        ServerSocket ss = new ServerSocket(19999);
		        while (true) {
		        	Socket s = ss.accept();
		        	Worker w = new Worker(s);
				w.start();
		        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Worker extends Thread {
	private Socket sock;
	public Worker(Socket s) {
		sock = s;
	}
	@Override
	public void run() {
		try {
        		DataInputStream in = new DataInputStream(sock.getInputStream());
        		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        		switch (in.readByte()) {
        			case 0:
        				// add peer
					PeerData tmp = new PeerData();
					tmp.ip = in.readUTF();
					tmp.port = in.readInt();
					tmp.PID = in.readInt();
        				Server.peers.add(tmp);
					System.out.println("Added peer " + tmp.port + " with PID " + tmp.PID);
        				break;
        			case 1:
        				// send list of peers
        				for (int i = 0; i < Server.peers.size(); ++i) {
						out.writeUTF(Server.peers.get(i).ip);
						out.writeInt(Server.peers.get(i).port);
        					out.writeInt(Server.peers.get(i).PID);
        				}
					System.out.println("Sent peer table");
        				break;
        		}
        
        		// close connection
        		out.flush();
        		in.close();
        		out.close();
        		sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

