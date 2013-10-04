/*
         JobLauncher, an application who launch a bat file (job) only one at a time
         Copyright (C) 2013 Maxime Soucy-Boivin

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Source : http://www.javacreed.com/running-a-batch-file-with-processbuilder/

package ca.uqac.info.Job.Launcher;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.cli.*;

public class JobLauncher 
{
	 /**
	 * Return codes
	 */
	 public static final int ERR_OK = 0;
	 public static final int ERR_ARGUMENTS = 4;

	  public static void main(final String[] args)
	  {
		 // Parse command line arguments
		 Options options = setupOptions();
		 CommandLine c_line = setupCommandLine(args, options);
		 
		 String batFolder = "";
		 String outFolderStr = "";
		 
		 File folderBat = null;
		 File[] listOfFiles = null;
		 String files = "";
		 
		 String username = "";
		 String password = "";
		 String recipient = "";
		 String message = "";
		 
		 boolean emailOpt = false;
		 GoogleMail Gmail = null;
		 
		 if(c_line.hasOption("e"))
		 {
			 emailOpt = Boolean.parseBoolean(c_line.getOptionValue("email"));
		 }
		 
		 if(emailOpt == true)
		 {
			Gmail = new GoogleMail();
		 }
		 
  	    // boolean nextJob = true;
		    
		 if (c_line.hasOption("h"))
		 {
		      showUsage(options);
		      System.exit(ERR_OK);
		 }
		 
		 //Contains a bat folder
		 if (c_line.hasOption("b"))
		 {
			 batFolder = c_line.getOptionValue("BatFolder");
		 }
		 else
		 {
		    System.err.println("No Bat Folder in Arguments");
		    System.exit(ERR_ARGUMENTS);
		 }
		 
		 //Contains a Output folder
		 if (c_line.hasOption("o"))
		 {
			 outFolderStr = c_line.getOptionValue("OutputFolder");
		 }
		 else
		 {
		    System.err.println("No Output Folder in Arguments");
		    System.exit(ERR_ARGUMENTS);
		 }
		 
		 //Contains the username
		 if(c_line.hasOption("username"))
		 {
			 username = c_line.getOptionValue("u");
		 }
		 else
		 {
		    System.err.println("No username in Arguments");
		    System.exit(ERR_ARGUMENTS);
		 }
		 
		 //Contains the password
		 if(c_line.hasOption("password"))
		 {
			 password = c_line.getOptionValue("p");
		 }
		 else
		 {
		    System.err.println("No password in Arguments");
		    System.exit(ERR_ARGUMENTS);
		 }
		 
		//Contains the recipient Email
		 if(c_line.hasOption("recipientEmail"))
		 {
			 recipient = c_line.getOptionValue("r");
		 }
		 else
		 {
		    System.err.println("No recipient Email in Arguments");
		    System.exit(ERR_ARGUMENTS);
		 }
		 
		 folderBat = new File(batFolder);
		 listOfFiles = folderBat.listFiles();
		 
		 File outFolder = new File(outFolderStr);
		 
		 //Make sure to test all of the files
		 for (int i = 0; i < listOfFiles.length; i++) 
		 {
		 
		   if (listOfFiles[i].isFile()) 
		   {
		       files = listOfFiles[i].getName();
		       String outName = outFolderStr+"\\Results_"+ files +".txt"; 
		       message = outName;
		       
		       //Make sure to use only the bat files
		       if (files.endsWith(".bat") || files.endsWith(".Bat"))
		       {
		    	   try 
		    	   {
		    		   String StartDay = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format( new Date());
		    		   long timeBefore = new Date().getTime();
		    		   
		    		   int exitStatus = launchJob(listOfFiles[i].getAbsolutePath(), outName, outFolder);
					
		    		   long timeAfter = new Date().getTime();
		    		   String EndDay = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format( new Date());
		    		   long timeLaunch = timeAfter - timeBefore;
					
		    		   if(exitStatus != 0)// An error arrived in the job launch
		    		   {
		    			   message += " exitStatus : " + exitStatus;
		    			   message += " \n Total : " + timeLaunch + " ms";
		    			   message += " \n Start : " + StartDay ;
		    			   message += "\n End : " + EndDay;
		    			   
		    			   //Send the email to tell the launch job status
		    			   sendEmail(Gmail, emailOpt, username, password, recipient, "Error :" + files, message);
		    		   }
		    		   else // Every things is fine 
		    		   {
		    			   message += " \n Total : " + timeLaunch + " ms";
		    			   message += " \n Start : " + StartDay ;
		    			   message += "\n End : " + EndDay;
		    			   
		    			   //Send the email to tell the launch job status
		    			   sendEmail(Gmail, emailOpt, username, password, recipient, files + " Done !!!", message);
		    		   }
		    	   } 
		    	   catch (Exception e) 
		    	   {
		    		   //Send a email for the error from the launch of the job
		    		   sendEmail(Gmail, emailOpt, username, password, recipient, "Error :" + files, "Launch Job and/or SMTP error !!!");
		    		
		    		   e.printStackTrace();
		    	   }
		       }// if files .bat
		     }// if isFile
		  }// For listOfFiles
		 
	  }
	  
	  /**
	   * Send a email if the option is choosen by the user
	   * @param Gmail the object who does the treatment
	   * @param emailOpt contain the choice of the user for the email option
	   * @param username the email used to send email
	   * @param password the password needed to connect into the email account
	   * @param recipient the email adress of the receiver
	   * @param title the title of the email
	   * @param message the message of the email
	   */
	  private static void sendEmail(GoogleMail Gmail, boolean emailOpt, String username, String password, String recipient, String title, String message)
	  {
		  // The option has been chosen by the user
		  if(emailOpt == true)
		  {
			  //The request is done !
			  try 
			  {
				Gmail.Send(username, password, recipient, title, message);
			  } 
			  catch (AddressException e) 
			  {
				System.out.println("AddressException during the email Sending !");
				e.printStackTrace();
			  } 
			  catch (MessagingException e) 
			  {
				System.out.println("MessagingException during the email Sending !");
				e.printStackTrace();
			  }
		  }
	  }
	  /**
	   * Sets up the ProcessBuilder for the bat file and start it
	   * @return The exitStatus 
	   */
	  private static int launchJob(String fileBat, String outName, File outFolder) throws Exception 
	  {
		    // The batch file to execute
		    final File batchFile = new File(fileBat);

		    // The output file. All activity is written to this file
		  	final File outputFile = new File(outName);

		    // Create the process
		  	final ProcessBuilder processBuilder = new ProcessBuilder(batchFile.getAbsolutePath(), outName);
		    // Redirect any output (including error) to a file. This avoids deadlocks
		    // when the buffers get full. 
		    processBuilder.redirectErrorStream(true);
		    processBuilder.redirectOutput(outputFile);
	

		    // Add a new environment variable
		    processBuilder.environment().put("JobLauncher", "Bat File Execution");

		    // Set the working directory. The batch file will run as if you are in this
		    // directory.
		    processBuilder.directory(outFolder);

		    // Start the process and wait for it to finish. 
		   /* while(nextJob != true)
		    {
		    	//Wait the end of the current Job to Launch the next one
		    }
		    
		    nextJob = false;*/
		    final Process process = processBuilder.start();
		    final int exitStatus = process.waitFor();
		    process.destroy();
		    
		    return exitStatus;
	  }
	  
	  /**
	   * Sets up the options for the command line parser
	   * @return The options
	   */
	  @SuppressWarnings("static-access")
	  private static Options setupOptions()
	  {
	    Options options = new Options();
	    Option opt;
	    opt = OptionBuilder
	        .withLongOpt("help")
	        .withDescription(
	            "Display command line usage")
	            .create("h");
	    options.addOption(opt);
	    opt = OptionBuilder
	        .withLongOpt("BatFolder")
	        .withArgName("x")
	        .hasArg()
	        .withDescription(
	            "Folder who contain the bat files to execute")
	            .create("b");
	    options.addOption(opt);
	    opt = OptionBuilder
	        .withLongOpt("OutputFolder")
	        .withArgName("x")
	        .hasArg()
	        .withDescription(
	            "The output folder who contains the results")
	            .create("o");
	    options.addOption(opt);
	     opt = OptionBuilder
	        	  .withLongOpt("username")
	        	  .withArgName("x")
	        	  .hasArg()
	        	  .withDescription(
	        	       "Set the username of the email transmitter")
	        	  .create("u");
	           options.addOption(opt);
	     opt = OptionBuilder
	 	         .withLongOpt("password")
	 	         .withArgName("x")
	 	         .hasArg()
	 	         .withDescription(
	 	        	    "Set the password of the email transmitter")
	 	         .create("p");
	 	 options.addOption(opt);
	 	opt = OptionBuilder
	 	         .withLongOpt("recipientEmail")
	 	         .withArgName("x")
	 	         .hasArg()
	 	         .withDescription(
	 	        	    "Set the email of the recipient")
	 	         .create("r");
	 	 options.addOption(opt);
	 	opt = OptionBuilder
	 	         .withLongOpt("email")
	 	         .withArgName("x")
	 	         .hasArg()
	 	         .withDescription(
	 	        	    "Set the email option to action --> true")
	 	         .create("e");
	 	 options.addOption(opt);
	 	 
	    return options;
	  }
	  
	  /**
	   * Sets up the command line parser
	   * @param args The command line arguments passed to the class' {@link main}
	   * method
	   * @param options The command line options to be used by the parser
	   * @return The object that parsed the command line parameters
	   */
	  private static CommandLine setupCommandLine(String[] args, Options options)
	  {
	    CommandLineParser parser = new PosixParser();
	    CommandLine c_line = null;
	    try
	    {
	      // parse the command line arguments
	      c_line = parser.parse(options, args);
	    }
	    catch (org.apache.commons.cli.ParseException exp)
	    {
	      // oops, something went wrong
	      System.err.println("ERROR: " + exp.getMessage() + "\n");
	      System.exit(ERR_ARGUMENTS);    
	    }
	    return c_line;
	  }
	  
	  /**
	   * Show the benchmark's usage
	   * @param options The options created for the command line parser
	   */
	  private static void showUsage(Options options)
	  {
	    HelpFormatter hf = new HelpFormatter();
	    hf.printHelp("java -jar JobLauncher.jar [options]", options);
	  }
	  
}
