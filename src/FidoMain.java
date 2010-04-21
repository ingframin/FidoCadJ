import javax.swing.*;
import java.util.prefs.*;
import java.io.*;
import java.util.*;

import globals.*;
import circuit.*;
import export.*;

/** FidoMain.java 

	The starting point of FidoCadJ.


<pre>
Version   Date           Author       Remarks
-------------------------------------------------------------------------------
1.0     April 2010      D. Bucci    First working version


    This file is part of FidoCadJ.

    FidoCadJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FidoCadJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FidoCadJ.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2008-2010 by Davide Bucci
</pre>

    
    @author Davide Bucci
    @version 1.0, April 2010
*/

class FidoMain {

    /** The main method. Shows an instance of the FidoFrame */
    public static void main(String[] args)
    {
 

        // See if there is a filename to open or an option to take into 
        // account
       	String loadFile="";
       	String libDirectory="";
       	// If this is true, the GUI will not be loaded and FidoCadJ will run as
       	// a command line utility
       	boolean commandLineOnly = false;
        
        boolean convertFile = false;
        int totx=0, toty=0;
        String exportFormat="";
        String outputFile="";
        				
        
        
        if (args.length>=1) {
        	int i;
        	boolean loaded=false;
        	boolean nextLib=false;

        	
        	for(i=0; i<args.length; ++i) {
        		if (args[i].startsWith("-")) {
        			// It is an option
        			
        			if (args[i].startsWith("-n")) {
        				// -n indicates that FidoCadJ should run only with
        				// the command line interface, without showing any
        				// GUI.
        				commandLineOnly=true;
        			} else if (args[i].startsWith("-d")) {
        				// -d indicates that the following argument is the path
        				// of the library directory. The previous library 
        				// directory will be ignored.
        				nextLib=true;
        			} else if (args[i].startsWith("-c")) {
        				// -c indicates that FidoCadJ should read and convert
        				// the given file. The structure of the command must 
        				// be as follows:
        				// -c 800 600 png test.png
        				// which is the total width and height in pixel, the 
        				// format required (SVG, EPS, PGF, PNG, PDF, EPS, SCH)
        				// and the file name to be used.
        				
        				try {
        					convertFile=true;
        					totx=Integer.parseInt(args[++i]);
        					toty=Integer.parseInt(args[++i]);
        					exportFormat=args[++i];
        					outputFile=args[++i];
        					
        				} catch (Exception E)
        				{
        					System.err.println("Unable to read the parameters given to -c");
        					System.exit(1);
        				}
        				
        				
        				convertFile=true;
        			} else if (args[i].startsWith("-h")) {
        				showCommandLineHelp();
        				System.exit(0);
        			} else {
        				System.err.println("Unrecognized option: "+args[i]);
        				showCommandLineHelp();
        				System.exit(1);
        			}
        		} else {
        			// We should process now the arguments of the different 
        			// options (if it applies).
        			if (nextLib) {
        				// This is -d: read the new library directory
        				libDirectory= args[i];
        				System.out.println("Changed the library directory: "+args[i]);
        			
        			} else {
        				if (loaded) {
        					System.err.println("Only one file can be specified in the command line");
        				}
        				// We can not load the file now, since popFrame has
        				// not been initialized yet.
        				loadFile=args[i];
        				loaded=true;
        			}
        			nextLib=false;
        		}
        	}
        
            
        }
           
        if(convertFile) {
        	// Creates a circuit object
        	ParseSchem P = new ParseSchem();
        	
        	if(loadFile.equals("")) {
        		System.err.println("You should specify a FidoCad file to convert");
        		System.exit(1);
        	}
        	
            // Reads the standard libraries
        	P.loadLibraryInJar(FidoMain.class.getResource("lib/IHRAM.FCL"), "ihram");
 			P.loadLibraryInJar(FidoMain.class.getResource("lib/FCDstdlib.fcl"), "");
 			P.loadLibraryInJar(FidoMain.class.getResource("lib/PCB_en.fcl"), "pcb");
        	
        	StringBuffer txt=new StringBuffer();    

        	try {
        		// Read the input file.
        		FileReader input = new FileReader(loadFile);
        		BufferedReader bufRead = new BufferedReader(input);
                
        		String line="";
        		txt = new StringBuffer(bufRead.readLine());
                        
        		txt.append("\n");
                        
        		while (line != null){
            		line =bufRead.readLine();
            		txt.append(line);
            		txt.append("\n");
        		}
            
        		bufRead.close();
				ArrayList LayerDesc=FidoReadApplet.CreateLayersNoDescription();
        		P.setLayers(LayerDesc);
                        
      			// Here txt contains the new circuit: parse it!

      			P.parseString(new StringBuffer(txt.toString()));       
       	 	
                ExportGraphic.exportSize(new File(outputFile),  P, 
                    exportFormat, totx, toty, 
                    true,false,true);
                System.out.println("Export completed");

            } catch(IOException ioe) {
                System.err.println("Export error: "+ioe);
            } catch(IllegalArgumentException iae) {
                System.err.println("Illegal filename");
            } catch(Exception e) {
            	System.err.println("Unable to export: "+e);
            }
            

        
        }
        
        if (!commandLineOnly) {
	        /*******************************************************************
	            PLATFORM SELECTION AND CONFIGURATION CODE GOES IN THIS SECTION
	        *******************************************************************/
        
        
	        if (System.getProperty("os.name").startsWith("Mac")) {
            
            
    	        Globals g=new Globals();
        
	            Preferences prefs_static = 
	                Preferences.userNodeForPackage(g.getClass());
            
    	        Globals.quaquaActive = prefs_static.get("QUAQUA", 
        	        "true").equals("true");
        
            	Globals.weAreOnAMac =true;
        
	            // These settings allows to obtain menus on the right place
    	        System.setProperty("com.apple.macos.useScreenMenuBar","true");
        	    // This is for JVM < 1.5 It won't harm on higher versions.
            	System.setProperty("apple.laf.useScreenMenuBar","true"); 
	            try { 
             
    	            //Globals.quaquaActive=true;
        	        //System.setProperty("Quaqua.Debug.showVisualBounds","true");
            	    //System.setProperty("Quaqua.Debug.showClipBounds","true");
                	if(Globals.quaquaActive) { 
                    	UIManager.setLookAndFeel(
                        	"ch.randelshofer.quaqua.QuaquaLookAndFeel");
                
	                    System.out.println("Quaqua look and feel active");
 	               	}
                
	                // set UI manager properties here that affect Quaqua
	            } catch (Exception e) {
	                // Quaqua is not active. Just continue!
            
    	            System.out.println("The Quaqua look and feel is not available");
        	        System.out.println("I will continue with the basic Apple l&f");
	            }
	        } else if (System.getProperty("os.name").startsWith("Win")) {
	            /* If the host system is a window system, select the Windows
    	           look and feel. This is a way to encourage people to use 
        	       FidoCadJ even on a Windows system, forgotting about Java.
               
            	*/
	            try {
	                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 	            } catch (Exception E) {}
            	Globals.quaquaActive=false;
            
        
        	} else {
            	Globals.quaquaActive=false;
        	}
        
        
        	// Un-comment to try to use the Metal LAF
        
    		/*
        	try {
            	UIManager.setLookAndFeel(
            	UIManager.getCrossPlatformLookAndFeelClassName());
            	Globals.weAreOnAMac =false;
        	} catch (Exception E) {}
        	*/
        
        
	        /*******************************************************************
    	                    END OF THE PLATFORM SELECTION CODE
        	*******************************************************************/
        
       

	        // Probably, you need to strip this code if you need to compile the
    	    // program under a non-Apple platform.
        
        	if(Globals.weAreOnAMac) {
            	AppleSpecific a=new AppleSpecific();
            	a.answerFinder();
        	}


            // Here we create the main window object
        
	        FidoFrame popFrame=new FidoFrame(true);

        
        	if (!libDirectory.equals("")) {
				popFrame.libDirectory = libDirectory;

        	}
        	popFrame.init();
        	// If a file should be loaded, load it now, since popFrame has been
        	// created and initialized.
        	if(!loadFile.equals(""))
				popFrame.Load(loadFile);

        	popFrame.setVisible(true);
		}
    }
    
    
    static private void  showCommandLineHelp()
    {
    	String help = "\nThis is FidoCadJ, version "+Globals.version+".\n"+
    	    "By Davide Bucci, 2007-2010.\n\n"+
    	    
    		"Use: java -jar fidocadj.jar [-options] [file] \n"+
    		"where options include:\n\n"+
    		
    		" -n     Does not start the graphical user interface\n\n"+
    		
    		" -d     Set the extern library directory\n"+
    		"        Usage: -d dir\n"+
    		"        where 'dir' is the directory you want to specify.\n\n"+
    		    		 
    		" -c     Convert the given file to a graphical format.\n"+
    		"        Usage: -d sx sy [eps|pdf|svg|png|jpg|fcd|sch] outfile\n"+
    		"        If you use this command line option, you *must* specify a FidoCad file to convert.\n\n"+
    		
    		" -h     Print this help. and exit.\n\n"+
    		
    		" [file] This is the optional (except if you use the -d option) FidoCad file to load at\n"+
    		"        startup time.\n\n"+
    		
    		"Example: load and convert a FidoCad drawing to a 800x600 pixel png file without using the GUI.\n"+
    		"java -jar fidocadj.jar -n -c 800 600 png out.png test.fcd\n\n";
    	System.out.println(help);
    }

}