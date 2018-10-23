import java.net.*;
import java.lang.Math;
import java.io.IOException;
import java.time.LocalDateTime;

public class Client
{
  public static final int PORT = 37;

  public static void main(String[] args)
  {
    // Create socket
    DatagramSocket sock;
    try
    {
      sock = new DatagramSocket();
      sock.setSoTimeout(1000); // wait for a maximum of 1 second
    }
    catch (SocketException se)
    {
      System.out.println("Could not create socket: " + se.toString());
      return;
    }

    System.out.println("Created socket");
   

    // get time
    InetAddress[] ips = getIPs();

    byte[] answer = null;
    for (InetAddress ip: ips)
    {
      if (!send(sock, ips))
        return;
      System.out.println("Sent request to " + ip.toString());

      answer = receive(sock);
      if (answer != null)
      {
        System.out.println("Received answer");
        break;
      }
    }
    
    if (answer == null)
    {
      System.out.println("Did not receive any response");
      return;
    }

    // print time
    System.out.println("Current time is: " + convertToLDT(answer).toString());
  }

  public static InetAddress[] getIPs()
  {
    InetAddress[] ips = new InetAddress[3];
    try
    {
      ips[0] = InetAddress.getByName("ptbtime1.ptb.de");
      ips[1] = InetAddress.getByName("ptbtime2.ptb.de");
      ips[2] = InetAddress.getByName("ptbtime3.ptb.de");
    }
    catch (UnknownHostException uhe)
    {
      System.out.println("Don't know host: " + uhe.toString());
    }
    catch (SecurityException se)
    {
      System.out.println("DNS query not allowed: " + se.toString());
    }
    return ips;
  }

  public static boolean send(DatagramSocket d, InetAddress[] ips)
  {
    DatagramPacket s = new DatagramPacket(new byte[0], 0, ips[1], PORT);
    try
    {
      d.send(s);
      return true;
    }
    catch (IOException ie)
    {
      System.out.println("Could not send query: " + ie.toString());
      return false;
    }
  }

  public static byte[] receive(DatagramSocket d)
  {
    DatagramPacket r = new DatagramPacket(new byte[4], 4);

    try
    {
      d.receive(r);
      return r.getData();
    }
    catch (IOException ie)
    {
      if (!(ie instanceof SocketTimeoutException))
        System.out.println("Could not receive query: " + ie.toString());
      return null;
    }
  }

  public static LocalDateTime convertToLDT(byte[] raw)
  {
    LocalDateTime ldt = LocalDateTime.of(1900, 1, 1, 0, 0, 0);

    //int seconds = raw[3] + raw[2] << 8 + raw[1] << 8*2 + raw[0] << 8*3;
    //                               -33            121         -48           -38
    System.out.println("time: " + makePositive(raw[0]) * 256  * 256  * 256  + ";" + makePositive(raw[1]) * 256  * 256  + ";" + makePositive(raw[2])  * 256 + ";" + makePositive(raw[3]));
    long seconds = makePositive(raw[3]) + makePositive(raw[2]) * 256 + makePositive(raw[1]) * 256 * 256 + makePositive(raw[0]) * 256 * 256 * 256;
    System.out.println("Seconds passed: " + seconds);
    return ldt.plusSeconds(seconds);
  }

  public static long makePositive(byte b)
  {
    if (b >= 0)
      return b;
   else
     return 256 + b;
  }
}
