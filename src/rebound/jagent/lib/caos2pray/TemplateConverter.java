/*
 * Created on May 26, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import rebound.exceptions.ImpossibleException;
import rebound.io.util.TextIOUtilities;
import rebound.jagent.lib.caos2pray.exc.C2PSyntaxException;
import rebound.jagent.lib.caos2pray.exc.DependencyException;
import rebound.jagent.lib.caos2pray.exc.NotIntegerTagException;
import rebound.jagent.lib.caos2pray.exc.ScanningException;
import rebound.jagent.lib.caos2pray.exc.ScriptReadException;
import rebound.jagent.lib.caos2pray.scanner.C2PCosFile;
import rebound.jagent.lib.caos2pray.scanner.C2PParser;
import rebound.jagent.lib.caos2pray.scanner.CosParser;
import rebound.jagent.lib.pray.template.Group;
import rebound.jagent.lib.pray.template.PrayTemplate;

/**
 * This is what interprets the C2P directives and uses them to make a {@link PrayTemplate}.
 * @author RProgrammer
 */
public class TemplateConverter
{
	public static final String[] COMMANDS =
	{
	 "pray-file",
	 
	 //XXXX-name
	 "c3-name",
	 "ds-name",
	 
	 "inline",
	 "attach",
	 "depend",
	 
	 "link",
	 "rscr",
	};
	
	
	/**
	 * This scans a potential directive to check if it starts with any of the recognized commands.
	 * @return The command it starts with, or <code>null</code> if not (which means it must be a tag directive).
	 */
	public static String getCommand(String directive)
	{
		String cmdtoken = directive.split(" ")[0];
		String ld = cmdtoken.toLowerCase();
		
		if (ld.length() == "XXXX-name".length() && ld.endsWith("-name"))
			return cmdtoken;
		
		for (String s : COMMANDS)
			if (ld.startsWith(s))
				return s;
		
		return null;
	}
	
	
	
	protected File entryFile = null;
	protected CosParser cosparser = new CosParser();
	protected C2PParser c2pparser = new C2PParser();
	protected PrayTemplate template = null;
	
	
	public TemplateConverter(File entryFile, CosParser cosparser, C2PParser c2pparser)
	{
		super();
		this.entryFile = entryFile;
		this.cosparser = cosparser;
		this.c2pparser = c2pparser;
	}
	
	
	/**
	 * This parses all linked cos files and constructs the template in its entirety.
	 * @throws ScriptReadException if an IOException is caught during parsing the cos file
	 */
	public void convert() throws ScriptReadException, ScanningException, DependencyException
	{
		if (entryFile == null)
			throw new IllegalStateException("No source file set");
		
		C2PCosFile c2p = parseFile(entryFile);
		
		FriendlyTags.postProcess(c2p);
		
		this.template = new PrayTemplate();
		construct(c2p);
	}
	
	
	//*/// Bah, no one asked for multiple-source file support; I'll add it if they ask, and Sine can figure out a sane way to do it.
	/* *
	 * This parses the given entryFile, records it (filemap.put()), and proceeds to do the same for all Linked source files that are not already in the map.
	 * @param entryfile The starting point
	 * @param filemap A map of .cos files to their parsed representations.
	 * /
	public void parseAndDiscoverSources(File entryfile, Map<File, C2PCosFile> filemap) throws FileNotFoundException, NotIntegerTagException, C2PSyntaxException, IOException, ContainmentViolationException, MissingLinkedScriptException
	{
		entryfile = entryfile.getCanonicalFile();
		File dir = entryfile.getParentFile();
		C2PCosFile c2p = parseFile(entryfile);
		
		filemap.put(entryfile, c2p);
		
		for (int index : c2p.findCommands("link"))
		{
			for (String arg : c2p.getCommandArgs(index))
			{
				if (arg.contains(".."))
					throw new ContainmentViolationException("Ancestor links ('..') are not allowed: '"+arg+"'");
				
				File f = new File(dir, arg);
				f = f.getCanonicalFile();
				
				if (!filemap.containsKey(f))
				{
					if (!f.isFile())
						throw new MissingLinkedScriptException("Linked script \""+arg+"\" could not be found ("+f.getAbsolutePath()+")");
					
					parseAndDiscoverSources(f, filemap);
				}
			}
		}
	}
	///*///
	
	
	protected C2PCosFile parseFile(File file) throws NotIntegerTagException, C2PSyntaxException, ScriptReadException
	{
		C2PCosFile c2p = null;
		
		try
		{
			cosparser.init(new FileInputStream(file));
			c2pparser.init(cosparser);
			c2pparser.scan();
			c2p = c2pparser.getC2PCosFile();
			cosparser.close();
		}
		catch (IOException exc)
		{
			throw new ScriptReadException(exc, file.getName());
		}
		
		//This is needed for ensuring that this script gets linked to the pray template--not just 'Link'ed scripts
		c2p.setThisFile(file);
		
		return c2p;
	}
	
	
	
	//<Conversion
	/**
	 * This constructs the template in its entirety.
	 * @throws DependencyException 
	 */
	protected PrayTemplate construct(C2PCosFile c2PCosFile) throws ScriptReadException, C2PSyntaxException, DependencyException
	{
		File dir = entryFile.getParentFile();
		
		template.setDir(dir);
		
		
		//Desired output file
		{
			int i = c2PCosFile.getSingletonCommandIndexOf("pray-file");
			if (i != -1)
				template.setDesiredOutputFile(c2PCosFile.getCommandSingletonArg(i));
		}
		
		
		//Build Tag Groups (DSAG, AGNT)
		{
			//Find the names
			Map<String, String> blocktypes = null;
			{
				blocktypes = new HashMap<String, String>();
				
				//DS
				{
					int i = c2PCosFile.getSingletonCommandIndexOf("ds-name");
					if (i != -1)
						//DS Name
						blocktypes.put("DSAG", c2PCosFile.getCommandSingletonArg(i));
				}
				
				//C3
				{
					int i = c2PCosFile.getSingletonCommandIndexOf("c3-name");
					if (i != -1)
					{
						String c3name = null;
						{
							String[] args = c2PCosFile.getCommandArgs(i);
							
							if (args.length == 1)
							{
								c3name = args[0];
							}
							else if (args.length == 0)
							{
								if (!blocktypes.containsKey("DSAG"))
									throw new C2PSyntaxException("The c3-name command may only lack an argument if there is a ds-name to default to.");
								
								c3name = blocktypes.get("DSAG") + " C3";
							}
							else
							{
								throw new C2PSyntaxException("The c3-name command takes no more than 1 argument.");
							}
						}
						
						//C3 Name
						blocktypes.put("AGNT", c3name);
					}
				}
				
				
				//Generic
				{
					/*
					 * Syntax:
					 * 
					 * DS-Name "The Box"
					 * C3-Name "The Box C3"
					 * 
					 * DSGB-Name "The Box"
					 */
					
					for (int i = 0; i < c2PCosFile.getCommandCount(); i++)
					{
						String cmd = c2PCosFile.getCommand(i);
						if (cmd.toLowerCase().endsWith("-name"))
						{
							String id = cmd.substring(0, cmd.length()-5);
							if (id.length() == 4)
							{
								String name = c2PCosFile.getCommandSingletonArg(i);
								
								//Add the pray group
								{
									//Todo? should the id be forced to uppercase?  Or pass through the case into the final file?  (case sensitivity isn't mentioned in the spec.); we'll just pass it through for now erring on preserving information
									if (blocktypes.containsKey(id))
										throw new C2PSyntaxException("Duplicate block types: "+id);
									
									blocktypes.put(id, name);
								}
							}
						}
					}
				}
			}
			
			
			//Todo? warn: if there are no blocks defined (which is valid, if they just want a FILE container, but probably not intended)
			
			for (String id : blocktypes.keySet())
			{
				String name = blocktypes.get(id);
				
				//Check validity of name
				{
					byte[] asciiname = null;
					
					try
					{
						asciiname = name.getBytes("ASCII");
					}
					catch (UnsupportedEncodingException exc)
					{
						throw new ImpossibleException("ASCII Unsupported?!", exc);
					}
					
					if (asciiname.length >= 127) //127 is the max not 128 because of the c-string null terminator
						throw new C2PSyntaxException("Invalid name, 127 ascii characters is the maximum for a PRAY block name (name was "+asciiname.length+"; \""+name+"\")");
				}
				
				template.addGroup(makeTheGenericBlock(c2PCosFile, dir, id, name));
			}
		}
		
		
		
		
		//Build Inline groups
		{
			//Attaches
			int[] attaches = c2PCosFile.findCommands("attach");
			for (int index : attaches)
			{
				String[] args = c2PCosFile.getCommandArgs(index);
				for (String s : args)
					template.addInline("FILE", s, s);
			}
			
			//Inlines
			int[] inlines = c2PCosFile.findCommands("inline");
			for (int index : inlines)
			{
				String[] args = c2PCosFile.getCommandArgs(index);
				if (args.length == 2)
				{
					template.addInline("FILE", args[0], args[1]);
				}
				else if (args.length == 1)
				{
					template.addInline("FILE", args[0], args[0]);
				}
				else
				{
					throw new C2PSyntaxException("The inline command takes either 1 or 2 arguments, no more, no less.");
				}
			}
		}
		
		return template;
	}
	
	
	
	
	
	
	
	protected Group makeTheGenericBlock(C2PCosFile c2p, File dir, String id, String name) throws ScriptReadException, C2PSyntaxException, DependencyException
	{
		Group g = new Group();
		
		//ID & Name
		g.setID(id);
		g.setName(name);
		
		makeTheBlock_Common(c2p, dir, g);
		
		return g;
	}
	
	
	
	
	
	protected void makeTheBlock_Common(C2PCosFile c2p, File dir, Group g) throws C2PSyntaxException, ScriptReadException, DependencyException
	{
		//Required
		g.addIntTag("Agent Type", 0);
		
		//Scripts
		{
			g.addScript(c2p.getThisFile().getName());
			for (int index : c2p.findCommands("link"))
				for (String arg : c2p.getCommandArgs(index))
					g.addScript(arg);
		}
		
		
		//Dependencies
		{
			ArrayList<String> deps = new ArrayList<String>();
			
			int[] indices = c2p.findCommands("attach");
			String[] args = null;
			for (int i : indices)
			{
				args = c2p.getCommandArgs(i);
				for (String s : args)
					deps.add(s);
			}
			
			indices = c2p.findCommands("depend");
			args = null;
			for (int i : indices)
			{
				args = c2p.getCommandArgs(i);
				for (String s : args)
					deps.add(s);
			}
			
			
			
			g.addIntTag("Dependency Count", deps.size());
			int c = 0;
			for (int i = 0; i < deps.size(); i++)
			{
				g.addStringTag("Dependency "+(i+1), deps.get(i));
				c = getDependencyCategoryByExtension(deps.get(i));
				g.addIntTag("Dependency Category "+(i+1), c);
			}
		}
		
		
		
		//C3/DS soft tags (used by the standard agent injector rooms)
		{
			if (c2p.getIndexOfStringTag("Agent Animation Callery") == -1)
			{
				int i = c2p.getIndexOfStringTag("Agent Animation File");
				
				if (i != -1)
				{
					String file = c2p.getStringTagValue(i);
					if (file.length() > 4)
						g.addStringTag("Agent Animation Gallery", file.substring(0, file.length()-4));
					else
						g.addStringTag("Agent Animation Gallery", file);
				}
			}
		}
		
		
		
		//Other tags
		{
			for (int i = 0; i < c2p.getStringTagCount(); i++)
				g.addStringTag(c2p.getStringTagName(i), c2p.getStringTagValue(i));
			
			for (int i = 0; i < c2p.getIntegerTagCount(); i++)
				g.addIntTag(c2p.getIntegerTagName(i), c2p.getIntegerTagValue(i));
		}
		
		
		
		//Remove script (C2P directive takes precedence over CAOS 'rscr' command)
		{
			int i = c2p.getSingletonCommandIndexOf("rscr");
			if (i != -1)
			{
				String[] args = c2p.getCommandArgs(i);
				
				if (args.length != 1)
					throw new C2PSyntaxException("rscr takes exactly one argument.");
				
				try
				{
					File f = new File(dir, args[0]).getAbsoluteFile();
					char[] cdata = TextIOUtilities.readAll(new InputStreamReader(new FileInputStream(f)));
					g.addStringTag("Remove script", new String(cdata));
				}
				catch (IOException exc)
				{
					throw new ScriptReadException(exc, args[0], true);
				}
			}
			else if (c2p.getCAOSRemoveScript() != null)
			{
				g.addStringTag("Remove script", c2p.getCAOSRemoveScript());
			}
			//else: No remove script specified
		}
	}
	
	
	//Conversion>
	
	
	
	//<Results
	public PrayTemplate getTemplate()
	{
		return this.template;
	}
	//Results>
	
	
	
	
	//<Utilities
	public static int getDependencyCategoryByExtension(String depFilename) throws DependencyException
	{
		String ldep = depFilename.toLowerCase();
		
		if (ldep.endsWith(".c16") || depFilename.endsWith(".s16"))
			return 2;
		else if (ldep.endsWith(".blk"))
			return 6;
		else if (ldep.endsWith(".wav") || depFilename.endsWith(".mng"))
			return 1;
		else if (ldep.endsWith(".catalogue"))
			return 7;
		else
			throw new DependencyException(depFilename);
	}
	//Utilities>
}
