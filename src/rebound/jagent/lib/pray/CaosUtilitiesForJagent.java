package rebound.jagent.lib.pray;

import static rebound.text.StringUtilities.*;
import java.io.IOException;
import java.util.List;
import rebound.exceptions.ImpossibleException;
import rebound.jagent.lib.caos2pray.scanner.CosParser;
import rebound.jagent.lib.caos2pray.scanner.CosParser.Token;
import rebound.jagent.lib.caos2pray.scanner.CosParser.TokenType;

public class CaosUtilitiesForJagent
{
	public static class CaosAndRemoveScript
	{
		public final String removeScript;
		public final String otherCaos;
		
		public CaosAndRemoveScript(String removeScript, String otherCaos)
		{
			this.removeScript = removeScript;
			this.otherCaos = otherCaos;
		}
	}
	
	
	
	public static CaosAndRemoveScript parse(String caos)
	{
		CosParser parser = new CosParser(caos);
		
		StringBuilder removeScript = new StringBuilder();
		StringBuilder otherCaos = new StringBuilder();
		
		while (true)
		{
			Token t;
			try
			{
				t = parser.next();
			}
			catch (IOException exc)
			{
				throw new ImpossibleException(exc);  //it's all in memory!
			}
			
			
			if (t == null)
				break;
			
			if (t.getType() == TokenType.CAOS)
			{
				//Check for rscr
				if (t.getText().toLowerCase().contains("rscr"))
				{
					//The rest of the file is a remove script, no further C2P directives are parsed
					
					String beforeRemoveScriptPart = null;
					String removeScriptPart = null;
					{
						String text = t.getText();
						int rscrPos = text.toLowerCase().indexOf("rscr");
						beforeRemoveScriptPart = text.substring(0, rscrPos);
						removeScriptPart = text.substring(rscrPos+4);
					}
					
					otherCaos.append(beforeRemoveScriptPart);
					removeScript.append(removeScriptPart);
					break;
				}
				//else: main
			}
			//else: main
			
			
			//main
			{
				otherCaos.append(t.getOriginalText());
			}
		}
		
		return new CaosAndRemoveScript(removeScript.toString(), otherCaos.toString());
	}
	
	
	
	
	
	protected static final String Delimiter = "\n\n******\n\n";
	
	public static String mergeCaos(String a, String b)
	{
		//This is right, right?
		return a + Delimiter + b;
	}
	
	public static String mergeCaosen(List<String> a)
	{
		return joinStrings(a, Delimiter);
	}
}
