/*
 * Created on May 25, 2006
 * 	by the wonderful Eclipse(c)
 */
package rebound.jagent.lib.caos2pray.scanner;

import java.io.File;
import java.util.ArrayList;
import rebound.jagent.lib.caos2pray.exc.C2PSyntaxException;

/**
 * This holds all information C2P needs from a .cos file.
 * @author RProgrammer
 */
public class C2PCosFile
{
	protected File thisfile;
	protected ArrayList<String> stagNames, itagNames, stagVals, commands;
	protected ArrayList<String[]> commandArgs;
	protected ArrayList<Integer> itagVals;
	protected String caosRemoveScript;
	
	public C2PCosFile()
	{
		super();
		commands = new ArrayList<String>();
		commandArgs = new ArrayList<String[]>();
		stagNames = new ArrayList<String>();
		itagNames = new ArrayList<String>();
		stagVals = new ArrayList<String>();
		itagVals = new ArrayList<Integer>();
	}
	
	
	
	
	
	//<Mutaters
	public void addStringTag(String name, String value)
	{
		stagNames.add(name);
		stagVals.add(value);
	}
	
	public void addIntegerTag(String name, int val)
	{
		itagNames.add(name);
		itagVals.add(val);
	}
	
	public void addCommand(String cmd, String[] args)
	{
		commands.add(cmd);
		commandArgs.add(args);
	}
	
	public void setCAOSRemoveScript(String scr)
	{
		caosRemoveScript = scr;
	}
	
	
	public void removeStringTag(int index)
	{
		stagNames.remove(index);
		stagVals.remove(index);
	}
	
	public void removeIntegerTag(int index)
	{
		itagNames.remove(index);
		itagVals.remove(index);
	}
	
	public void removeCommand(int index)
	{
		commands.remove(index);
		commandArgs.remove(index);
	}
	
	
	public void replaceTagName(String given, String needed)
	{
		for (int i = 0; i < stagNames.size(); i++)
			if (stagNames.get(i).equalsIgnoreCase(given))
				stagNames.set(i, needed);
		
		for (int i = 0; i < itagNames.size(); i++)
			if (itagNames.get(i).equalsIgnoreCase(given))
				itagNames.set(i, needed);
	}
	//Mutaters>
	
	
	
	
	
	
	
	
	
	//<Readers
	/**
	 * -finds the indices of the instances of the given command, case-insensitively.
	 */
	public int[] findCommands(String cmd)
	{
		int[] whole = new int[getCommandCount()];
		int count = 0;
		for (int i = 0; i < whole.length; i++)
			if (cmd.equalsIgnoreCase(getCommand(i)))
				whole[count++] = i;
		int[] shrunk = new int[count];
		System.arraycopy(whole, 0, shrunk, 0, count);
		return shrunk;
	}
	
	
	/**
	 * Validates that the given command has exactly 1 occurrence and returns its index.
	 * If the command fails the validation, a {@link C2PSyntaxException} is thrown.
	 * @return The index of the single occurrence of the given command, or -1 if there are no occurrences
	 */
	public int getSingletonCommandIndexOf(String cmd) throws C2PSyntaxException
	{
		int[] cmds = findCommands(cmd);
		if (cmds.length == 0)
			return -1;
		else if (cmds.length == 1)
			return cmds[0];
		else
			throw new C2PSyntaxException("It is illegal to have multiple instances of the command '"+cmd+"'");
	}
	
	/**
	 * Validates that the given command has exactly 1 argument and returns it.
	 * If the command fails the validation, a {@link C2PSyntaxException} is thrown.
	 */
	public String getCommandSingletonArg(int index) throws C2PSyntaxException
	{
		String[] args = getCommandArgs(index);
		if (args.length != 1)
			throw new C2PSyntaxException("The "+getCommand(index)+" command takes exactly 1 argument.");
		return args[0];
	}

	
	
	/**
	 * This represents what comes after the 'rscr' CAOS meta-command.
	 * Note: 'endm' is removed if it is at the very EOF.
	 */
	public String getCAOSRemoveScript()
	{
		return this.caosRemoveScript;
	}
	
	
	public int getIntegerTagCount()
	{
		return itagNames.size();
	}
	
	public int getStringTagCount()
	{
		return stagNames.size();
	}
	
	public int getIndexOfIntegerTag(String name)
	{
		return itagNames.indexOf(name);
	}
	
	public int getIndexOfStringTag(String name)
	{
		return stagNames.indexOf(name);
	}
	
	public String getIntegerTagName(int index)
	{
		return itagNames.get(index);
	}
	
	public String getStringTagName(int index)
	{
		return stagNames.get(index);
	}
	
	public int getIntegerTagValue(int index)
	{
		return itagVals.get(index);
	}
	
	public String getStringTagValue(int index)
	{
		return stagVals.get(index);
	}
	
	
	public int getCommandCount()
	{
		return commands.size();
	}
	
	public String getCommand(int index)
	{
		return commands.get(index);
	}
	
	public String[] getCommandArgs(int index)
	{
		return commandArgs.get(index);
	}
	//Readers>





	public File getThisFile()
	{
		return this.thisfile;
	}





	public void setThisFile(File thisfile)
	{
		this.thisfile = thisfile;
	}
}
