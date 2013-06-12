package Mckinsey;

import java.net.*;
import java.io.*;
import java.util.*;
public class Twitter
{

  public static void main(String[] args) throws Exception {
	  String tweetsurl = null;
	  String strSearch =" ";
	  int id = 1;
	  System.out.println("Please input what you wanna search:");
	  Scanner strinput = new Scanner(System.in);
	  String s = strinput.next();
	  
	  if(s != null)
	  {
		  strSearch = s;
		  strSearch=strSearch.replace("#", "");
		  URL tweet = new URL("http://search.twitter.com/search.json?q="+strSearch+"&rpp=100&include_entities=true&result_type=recent");
		  BufferedReader in = new BufferedReader(new InputStreamReader(tweet.openStream()));

		  String inputs;
		  String tweets;
		  
		  while ((inputs = in.readLine()) != null)
		  {
			  Scanner s1 = new Scanner(inputs).useDelimiter(",");
		      String match ="expanded_url";
    		  System.out.println("The Unique URLs are:");
		      while(s1.hasNext())
		      {
		    	  tweets=s1.next();
		          if(tweets.length()>=13)
		          {		            	 
		        	  if(tweets.substring(1,13).equals(match.substring(0,12)))
		        	  {
		        		  tweetsurl = tweets.substring(16,tweets.length()-1);
		        		  tweetsurl=tweetsurl.replaceAll("/","");
		        		  System.out.print((id++)+". ");
		        		  System.out.println(tweetsurl);
		        		  }		          
		        	  }
		          }
		      }

		  in.close();
		  }
	  else
	  {
		  System.out.println("Nothing has been input.");
		  }
	  }
  }