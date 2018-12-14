import java.net.*;
import java.util.*;
import java.io.*;

public class Peer {
	public static void main(String... args) {
		DatagramSocket ds;
		try {
			ds = new DatagramSocket();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int port = ds.getLocalPort();
		// register peer
		try { 
		Socket s = new Socket("localhost", 19999);
		DataInputStream in = new DataInputStream(s.getInputStream());
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		out.writeByte(0);
		out.writeUTF(ds.getLocalAddress().toString());
		out.writeInt(port);
		out.writeInt((int) ProcessHandle.current().pid());
		out.close();
		in.close();
		s.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// wait 4 sec
		sleep(4000);

		// get all peers
		Socket s = null;
		DataInputStream in = null;
		DataOutputStream out = null;
		try { 
		s = new Socket("localhost", 19999);
		in = new DataInputStream(s.getInputStream());
		out = new DataOutputStream(s.getOutputStream());
		out.writeByte(1);
		out.flush();
		while (true) {
			PeerData pd = new PeerData();
			pd.ip = in.readUTF();
			pd.port = in.readInt();
			pd.PID = in.readInt();
			Peer.peers.add(pd);
		}
		} catch (EOFException eof) {
			for (int i = 0; i < Peer.peers.size(); ++i) {
					//System.out.println("Added peer " + Peer.peers.get(i).port + " with PID " + Peer.peers.get(i).PID);
			}
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (s != null)
					s.close();
			} catch (IOException e) {}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// waitRandomTime
		sleep(1000);
		// start Bully algorithm
	}
	static private ArrayList<PeerData> peers = new ArrayList<PeerData>();
	

	public static void sleep(int ms) {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {}
	}
}
