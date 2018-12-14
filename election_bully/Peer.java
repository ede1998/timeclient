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

		// start Bully algorithm
		bully(ds);
	}
	static private ArrayList<PeerData> peers = new ArrayList<PeerData>();
	

	public static void sleep(int ms) {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {}
	}

	public static void bully(DatagramSocket ds) {
		// start listening
		DatagramPacket dp = new DatagramPacket(new byte[2], 2);
		Random r = new Random();
		do
		{
			// wait for msg for random time
			ds.setSoTimeOut(r.nextInt(1000));
			try {
			ds.receive(dp);
			process(dp);
			} catch (SocketTimeoutException ste) {}
	        } while (r.nextInt(10) == 1);

		System.out.println("Initiating bully alg");
		sendElection(ds);

		try {
			ds.receive(dp);
			process(dp, ds);
		} catch (Exception e) {}

	}

	public static void process(DatagramPacket dp, DatagramSocket ds) {
		final int pid = (int) ProcessHandle.current().pid();
		int[] data = ByteBuffer.wrap(dp.getData()).asIntBuffer().array();
		switch (data[0]) {
			case 0: // Election
				// send answer if pid > pid_sender
				if (pid > data[1])
					sendAnswer();
				// start self elect
				sendElection(ds);
				break;
			case 1: // Answer
				break;
			case 2: // coordinator
				break;

	}

	public static void sendElection(DatagramSocket ds) {
		try {
		final int pid = (int) ProcessHandle.current().pid();
		for (int i = 0; i < Peer.peers.size(); ++i) {
			if (pid > Peer.peers.get(i).PID) continue;
			int[] data = new int[2];
			data[1] = pid;
			data[0] = 0;
			DatagramPacket dps = new DatagramPacket(data, 4);
			ds.send(dps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendAnswer(DatagramSocket ds) {

	}
	public static void sendCoordinator(DatagramSocket ds) {
		try {
		final int pid = (int) ProcessHandle.current().pid();
		for (int i = 0; i < Peer.peers.size(); ++i) {
			int[] data = new int[2];
			data[1] = pid;
			data[0] = 2;
			DatagramPacket dps = new DatagramPacket(data, 4);
			ds.send(dps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
