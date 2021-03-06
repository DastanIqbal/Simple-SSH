package se.jassh.io;

import se.jassh.hosts.HostItem;
import se.jassh.security.Encrypter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;


public class IOHandler {

	public static String filename = "hosts.txt";
	public static String separator = ",";
	public static String breaker = ",,";

	public static void save(HostItem host, File folder)
	{
		File file = new File(folder, filename);
		ArrayList<HostItem> hostList = new ArrayList<HostItem>();

		boolean exists = file.exists();
		if(exists)
		{
			try {
				ArrayList<HostItem> hosts = load(folder);
				for(HostItem h : hosts)
				{
					if(h.getHostname().equals(host.getHostname()) && h.getPort() == host.getPort())
					{
						return;
					}
				}
				hostList.addAll(hosts);
			} catch (IOException e) {
				Log.e("CLIENT Error IOHandler.save() / IOHandler.load()", e.getMessage());
			}
			//Removes the old savefile. We'll create a new one 
			delete(folder);
		}
		hostList.add(host);

		FileOutputStream writer;

		StringBuilder builder = new StringBuilder();
		try 
		{
			//Builds new file data
			for(int i = 0; i< hostList.size() - 1; i++)
			{
				builder.append(hostList.get(i).getName() + separator + hostList.get(i).getUsername() + separator + hostList.get(i).getPassword() + separator + hostList.get(i).getHostname() + separator + hostList.get(i).getPort() + breaker);
				Log.d("CLIENT", "Appending, now string is: " + builder.toString());
			}

			builder.append(hostList.get(hostList.size()- 1).getName() + separator + hostList.get(hostList.size()- 1).getUsername() + separator + hostList.get(hostList.size()- 1).getPassword() + separator + hostList.get(hostList.size()- 1).getHostname() + separator + hostList.get(hostList.size()- 1).getPort() + breaker);
			Log.d("CLIENT", "Appending, now string is: " + builder.toString());
			byte[] bytes = Encrypter.encode(builder.toString().getBytes(), true);


			//Writes the encrypted data to file
			writer = new FileOutputStream(file, false);
			writer.write(bytes);
		} 
		catch (Exception e) 
		{
			Log.e("CLIENT Error IOHandler.save()", e.getMessage());
		}
	}

	public static ArrayList<HostItem> load(File folder) throws IOException
	{
		ArrayList<HostItem> hosts = new ArrayList<HostItem>();

		File file = new File(folder, filename);
		if(!file.exists())
		{
			throw new IOException("Error reading file");
		}

		FileInputStream in = new FileInputStream(file);
		byte[] bytes = new byte[in.available()];
		in.read(bytes);
		in.close();

		String fullstring;
		try {
			fullstring = new String(Encrypter.decode(bytes));
			String[] lines = fullstring.split(breaker);

			for(String line : lines)
			{
				String[] host = line.split(separator);
				HostItem h = new HostItem(host[0], host[1], host[2], host[3], Integer.parseInt(host[4]));
				hosts.add(h);
				Log.d("CLIENT - IOHandler.load()", "Loaded host: " + h.getName());
			}
		} catch (Exception e) {
			Log.e("CLIENT Error IOHandler.load()", e.getMessage());
		}

		return hosts;
	}

	public static void delete(File folder)
	{
		File file = new File(folder, filename);
		if(file.exists()){
			file.delete();
		}
	}

	public static void remove(File folder, HostItem host)
	{
		ArrayList<HostItem> hosts;
		try 
		{
			hosts = load(folder);
			Iterator<HostItem> it = hosts.iterator();
			while(it.hasNext())
			{
				HostItem h = it.next();
				if((h.getHostname().equals(host.getHostname()) && h.getPort() == host.getPort()))
				{
					it.remove();
					break;
				}
			}

			delete(folder);
			for(HostItem h : hosts)
			{
				save(h, folder);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
