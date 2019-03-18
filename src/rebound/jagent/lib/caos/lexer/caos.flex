//Note: this shall be strictly standard JFlex and not using the SimpleLexer system, if only so that I have a test case / example / sandbox(!) of a standard JFlex and non-SimpleLexer-dependent lexer  :>

package rebound.jagent.lib.caos.lexer;

import static rebound.jagent.lib.caos.lexer.TokenType.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import rebound.exceptions.ImpossibleException;
import rebound.exceptions.SyntaxException;

@SuppressWarnings("all")
/**
 * A Lexical Analyzer for CAOS script.
 */
%%


%class CAOSLexer
%public
%scanerror ImpossibleException
%type void
%yylexthrow SyntaxException
%apiprivate

%unicode
%caseless
%line
%column
%char

%xstate STRING


%{
	//Provide a no-arg constructor
	public CAOSLexer()
	{
		super();
	}
	
	
	
	protected StringBuilder stringLiteral = new StringBuilder();
	
	protected TokenType currTokenType;
	protected String currTokenValue;
	protected boolean eof;
	
	public TokenType getTokenType()
	{
		return currTokenType;
	}
	
	public String getTokenValue()
	{
		//Lazy, in case the token value is not needed (eg, comments)
		if (currTokenType == null || !currTokenType.isValueUsed())
			return null;
		else
		{
			if (currTokenValue == null)
			{
				if (currTokenType == LITERAL_STRING)
					currTokenValue = stringLiteral.toString();
				else
					currTokenValue = yytext();
			}
			return currTokenValue;
		}
	}
	
	public boolean isEOF()
	{
		return eof;
	}
	
	public int getLineNumber()
	{
		return yyline + 1;
	}
	
	public int getColumnNumber()
	{
		return yycolumn + 1;
	}
	
	public int getPosition()
	{
		return yychar;
	}
	
	
	
	public Reader getReader()
	{
		return zzReader;
	}
	
	/**
	 * Sets the underlying reader and resets the state.
	 */
	public void setReader(Reader reader)
	{
		this.zzReader = new FinalLineAppendingReader(reader); //JFlex can't match EOF, so this makes the $-terminated expressions work correctly
		reset();
	}
	
	/**
	 * Sets the underlying reader to a StringReader of the given data and resets the state.
	 */
	public void setReaderSource(String data)
	{
		this.zzReader = new StringReader(data+"\n"); //Faster than setReader(new StringReader(data))
		reset();
	}
	
	public void next() throws IOException, SyntaxException
	{
		currTokenValue = null;
		yylex();
	}
	
	public void close() throws IOException
	{
		yyclose();
	}
	
	public void reset()
	{
		eof = false;
		currTokenType = null;
		currTokenValue = null;
		stringLiteral.setLength(0);
		yyreset(zzReader); //there is no need to reset the reader; perhaps it is reading from two source texts concatenated together
	}
%}




Whitespace = [ \r\n\t]+


//<Integer literals
//There is binary but no hexadecimal (eg, 0x4C, $4C, 4Ch) or octal (eg, 0777) to my knowledge

//This is how most languages define it
//_fractionalPart = "." [0-9]+
//_exponentPart = "e" "-"? [0-9] [0-9]?
//DecimalLiteral = "-"? (([0-9] {_fractionalPart}?) | {_fractionalPart}) {_exponentPart}?

//CAOS interprets "-" to be "-0" and "." to be "0.0" and "42." to be "42.0"
_fractionalPart = "." [0-9]*
DecimalLiteral = ([0-9]+ {_fractionalPart}?) | ("-" [0-9]* {_fractionalPart}?) | {_fractionalPart}

//"%" == "%0"
BinaryLiteral = "%" [01]{0,31}
//Integer literals>


Label = ([:letter:] | "_") ([:letter:] | "_" | [:digit:])*



%%
<<EOF>> 						{eof = true; return;}

<STRING>
{
	\"						{currTokenType = LITERAL_STRING; yybegin(YYINITIAL); return;}
	\\n						{stringLiteral.append('\n');}
	\\r						{stringLiteral.append('\r');}
	\\t						{stringLiteral.append('\t');}
	\\\\						{stringLiteral.append('\\');}
	\\\"						{stringLiteral.append('"');}
	\\'						{stringLiteral.append('\'');}
	[^\\\"]+					{stringLiteral.append(yytext());} //for performance
	.						{stringLiteral.append(yycharat(0));}
}

<YYINITIAL>
{
	{Whitespace}			{}
	\"						{stringLiteral.setLength(0); yybegin(STRING);}
	
	"*" [^\r\n]* $			{currTokenType = COMMENT; return;}
//	"*" [^\r\n]* <<EOF>>	{currTokenType = COMMENT; return;} //not allowed in JFlex, and $ does not match EOF
	'\\.' | '[^'\\]'		{currTokenType = LITERAL_CHAR; return;}
	{DecimalLiteral}		{currTokenType = LITERAL_DECIMAL; return;}
	{BinaryLiteral}			{currTokenType = LITERAL_BINARY; return;}
	va[:digit:][:digit:]	{currTokenType = NUMVAR_VA; return;}
	ov[:digit:][:digit:]	{currTokenType = NUMVAR_OV; return;}
	mv[:digit:][:digit:]	{currTokenType = NUMVAR_MV; return;}
	
	subr {Whitespace} {Label}	{currTokenType = SUBROUTINE_SUBR; return;}
	gsub {Whitespace} {Label}	{currTokenType = SUBROUTINE_GSUB; return;}
	
	
	//<Simple tokens
	"["		{currTokenType = BYTEARRAYLITERAL_OPEN; return;}
	"]"		{currTokenType = BYTEARRAYLITERAL_CLOSE; return;}
	"="		{currTokenType = TOKEN_EQ; return;}
	"<>"	{currTokenType = TOKEN_NE; return;}
	"<"		{currTokenType = TOKEN_LT; return;}
	">"		{currTokenType = TOKEN_GT; return;}
	"<="	{currTokenType = TOKEN_LE; return;}
	">="	{currTokenType = TOKEN_GE; return;}
	"eq"	{currTokenType = TOKEN_EQ; return;}
	"ne"	{currTokenType = TOKEN_NE; return;}
	"lt"	{currTokenType = TOKEN_LT; return;}
	"gt"	{currTokenType = TOKEN_GT; return;}
	"le"	{currTokenType = TOKEN_LE; return;}
	"ge"	{currTokenType = TOKEN_GE; return;}
	"and"	{currTokenType = TOKEN_AND; return;}
	"or"	{currTokenType = TOKEN_OR; return;}
	
	
	"scrp"	{currTokenType = TOKEN_SCRP; return;}
	"iscr"	{currTokenType = TOKEN_ISCR; return;}
	"rscr"	{currTokenType = TOKEN_RSCR; return;}
	"endm"	{currTokenType = TOKEN_ENDM; return;}
	"doif"	{currTokenType = TOKEN_DOIF; return;}
	"elif"	{currTokenType = TOKEN_ELIF; return;}
	"else"	{currTokenType = TOKEN_ELSE; return;}
	"loop"	{currTokenType = TOKEN_LOOP; return;}
	"reps"	{currTokenType = TOKEN_REPS; return;}
	"enum"	{currTokenType = TOKEN_ENUM; return;}
	"epas"	{currTokenType = TOKEN_EPAS; return;}
	"esee"	{currTokenType = TOKEN_ESEE; return;}
	"etch"	{currTokenType = TOKEN_ETCH; return;}
	"econ"	{currTokenType = TOKEN_ECON; return;}
	"retn"	{currTokenType = TOKEN_RETN; return;}
	"endi"	{currTokenType = TOKEN_ENDI; return;}
	"ever"	{currTokenType = TOKEN_EVER; return;}
	"untl"	{currTokenType = TOKEN_UNTL; return;}
	"repe"	{currTokenType = TOKEN_REPE; return;}
	"next"	{currTokenType = TOKEN_NEXT; return;}
	"inst"	{currTokenType = TOKEN_INST; return;}
	"slow"	{currTokenType = TOKEN_SLOW; return;}
	"avar"	{currTokenType = TOKEN_AVAR; return;}
	"eame"	{currTokenType = TOKEN_EAME; return;}
	"from"	{currTokenType = TOKEN_FROM; return;}
	"game"	{currTokenType = TOKEN_GAME; return;}
	"mame"	{currTokenType = TOKEN_MAME; return;}
	"name"	{currTokenType = TOKEN_NAME; return;}
	"velx"	{currTokenType = TOKEN_VELX; return;}
	"vely"	{currTokenType = TOKEN_VELY; return;}
	"_p1_"	{currTokenType = TOKEN_P1; return;}
	"_p2_"	{currTokenType = TOKEN_P2; return;}
	"pntr"	{currTokenType = TOKEN_PNTR; return;}
	"ownr"	{currTokenType = TOKEN_OWNR; return;}
	"null"	{currTokenType = TOKEN_NULL; return;}
	
	
	"brn:" {Whitespace} "dmpb"	{currTokenType = TOKEN_BRN_DMPB; return;}
	"brn:" {Whitespace} "dmpd"	{currTokenType = TOKEN_BRN_DMPD; return;}
	"brn:" {Whitespace} "dmpl"	{currTokenType = TOKEN_BRN_DMPL; return;}
	"brn:" {Whitespace} "dmpt"	{currTokenType = TOKEN_BRN_DMPT; return;}
	"brn:" {Whitespace} "setd"	{currTokenType = TOKEN_BRN_SETD; return;}
	"brn:" {Whitespace} "setl"	{currTokenType = TOKEN_BRN_SETL; return;}
	"brn:" {Whitespace} "setn"	{currTokenType = TOKEN_BRN_SETN; return;}
	"brn:" {Whitespace} "sett"	{currTokenType = TOKEN_BRN_SETT; return;}
	
	"dbg:" {Whitespace} "asrt"	{currTokenType = TOKEN_DBG_ASRT; return;}
	"dbg:" {Whitespace} "cpro"	{currTokenType = TOKEN_DBG_CPRO; return;}
	"dbg:" {Whitespace} "flsh"	{currTokenType = TOKEN_DBG_FLSH; return;}
	"dbg:" {Whitespace} "html"	{currTokenType = TOKEN_DBG_HTML; return;}
	"dbg:" {Whitespace} "outs"	{currTokenType = TOKEN_DBG_OUTS; return;}
	"dbg:" {Whitespace} "outv"	{currTokenType = TOKEN_DBG_OUTV; return;}
	"dbg:" {Whitespace} "paws"	{currTokenType = TOKEN_DBG_PAWS; return;}
	"dbg:" {Whitespace} "play"	{currTokenType = TOKEN_DBG_PLAY; return;}
	"dbg:" {Whitespace} "poll"	{currTokenType = TOKEN_DBG_POLL; return;}
	"dbg:" {Whitespace} "prof"	{currTokenType = TOKEN_DBG_PROF; return;}
	"dbg:" {Whitespace} "tack"	{currTokenType = TOKEN_DBG_TACK; return;}
	"dbg:" {Whitespace} "tock"	{currTokenType = TOKEN_DBG_TOCK; return;}
	"dbg:" {Whitespace} "wtik"	{currTokenType = TOKEN_DBG_WTIK; return;}
	
	"file" {Whitespace} "glob"	{currTokenType = TOKEN_FILE_GLOB; return;}
	"file" {Whitespace} "iclo"	{currTokenType = TOKEN_FILE_ICLO; return;}
	"file" {Whitespace} "iope"	{currTokenType = TOKEN_FILE_IOPE; return;}
	"file" {Whitespace} "jdel"	{currTokenType = TOKEN_FILE_JDEL; return;}
	"file" {Whitespace} "oclo"	{currTokenType = TOKEN_FILE_OCLO; return;}
	"file" {Whitespace} "oflu"	{currTokenType = TOKEN_FILE_OFLU; return;}
	"file" {Whitespace} "oope"	{currTokenType = TOKEN_FILE_OOPE; return;}
	
	"gene" {Whitespace} "clon"	{currTokenType = TOKEN_GENE_CLON; return;}
	"gene" {Whitespace} "cros"	{currTokenType = TOKEN_GENE_CROS; return;}
	"gene" {Whitespace} "kill"	{currTokenType = TOKEN_GENE_KILL; return;}
	"gene" {Whitespace} "load"	{currTokenType = TOKEN_GENE_LOAD; return;}
	"gene" {Whitespace} "move"	{currTokenType = TOKEN_GENE_MOVE; return;}
	
	"gids" {Whitespace} "fmly"	{currTokenType = TOKEN_GIDS_FMLY; return;}
	"gids" {Whitespace} "gnus"	{currTokenType = TOKEN_GIDS_GNUS; return;}
	"gids" {Whitespace} "root"	{currTokenType = TOKEN_GIDS_ROOT; return;}
	"gids" {Whitespace} "spcs"	{currTokenType = TOKEN_GIDS_SPCS; return;}
	
	"hist" {Whitespace} "cage"	{currTokenType = TOKEN_HIST_CAGE; return;}
	"hist" {Whitespace} "coun"	{currTokenType = TOKEN_HIST_COUN; return;}
	"hist" {Whitespace} "cros"	{currTokenType = TOKEN_HIST_CROS; return;}
	"hist" {Whitespace} "date"	{currTokenType = TOKEN_HIST_DATE; return;}
	"hist" {Whitespace} "evnt"	{currTokenType = TOKEN_HIST_EVNT; return;}
	"hist" {Whitespace} "find"	{currTokenType = TOKEN_HIST_FIND; return;}
	"hist" {Whitespace} "finr"	{currTokenType = TOKEN_HIST_FINR; return;}
	"hist" {Whitespace} "foto"	{currTokenType = TOKEN_HIST_FOTO; return;}
	"hist" {Whitespace} "gend"	{currTokenType = TOKEN_HIST_GEND; return;}
	"hist" {Whitespace} "gnus"	{currTokenType = TOKEN_HIST_GNUS; return;}
	"hist" {Whitespace} "mon1"	{currTokenType = TOKEN_HIST_MON1; return;}
	"hist" {Whitespace} "mon2"	{currTokenType = TOKEN_HIST_MON2; return;}
	"hist" {Whitespace} "mute"	{currTokenType = TOKEN_HIST_MUTE; return;}
	"hist" {Whitespace} "name"	{currTokenType = TOKEN_HIST_NAME; return;}
	"hist" {Whitespace} "netu"	{currTokenType = TOKEN_HIST_NETU; return;}
	"hist" {Whitespace} "next"	{currTokenType = TOKEN_HIST_NEXT; return;}
	"hist" {Whitespace} "prev"	{currTokenType = TOKEN_HIST_PREV; return;}
	"hist" {Whitespace} "rtim"	{currTokenType = TOKEN_HIST_RTIM; return;}
	"hist" {Whitespace} "sean"	{currTokenType = TOKEN_HIST_SEAN; return;}
	"hist" {Whitespace} "tage"	{currTokenType = TOKEN_HIST_TAGE; return;}
	"hist" {Whitespace} "time"	{currTokenType = TOKEN_HIST_TIME; return;}
	"hist" {Whitespace} "type"	{currTokenType = TOKEN_HIST_TYPE; return;}
	"hist" {Whitespace} "utxt"	{currTokenType = TOKEN_HIST_UTXT; return;}
	"hist" {Whitespace} "vari"	{currTokenType = TOKEN_HIST_VARI; return;}
	"hist" {Whitespace} "wipe"	{currTokenType = TOKEN_HIST_WIPE; return;}
	"hist" {Whitespace} "wnam"	{currTokenType = TOKEN_HIST_WNAM; return;}
	"hist" {Whitespace} "wtik"	{currTokenType = TOKEN_HIST_WTIK; return;}
	"hist" {Whitespace} "wuid"	{currTokenType = TOKEN_HIST_WUID; return;}
	"hist" {Whitespace} "wvet"	{currTokenType = TOKEN_HIST_WVET; return;}
	"hist" {Whitespace} "year"	{currTokenType = TOKEN_HIST_YEAR; return;}
	
	"mesg" {Whitespace} "writ"	{currTokenType = TOKEN_MESG_WRIT; return;}
	"mesg" {Whitespace} "wrt+"	{currTokenType = TOKEN_MESG_WRT; return;}
	
	"net:" {Whitespace} "erra"	{currTokenType = TOKEN_NET_ERRA; return;}
	"net:" {Whitespace} "expo"	{currTokenType = TOKEN_NET_EXPO; return;}
	"net:" {Whitespace} "from"	{currTokenType = TOKEN_NET_FROM; return;}
	"net:" {Whitespace} "head"	{currTokenType = TOKEN_NET_HEAD; return;}
	"net:" {Whitespace} "hear"	{currTokenType = TOKEN_NET_HEAR; return;}
	"net:" {Whitespace} "host"	{currTokenType = TOKEN_NET_HOST; return;}
	"net:" {Whitespace} "line"	{currTokenType = TOKEN_NET_LINE; return;}
	"net:" {Whitespace} "make"	{currTokenType = TOKEN_NET_MAKE; return;}
	"net:" {Whitespace} "pass"	{currTokenType = TOKEN_NET_PASS; return;}
	"net:" {Whitespace} "rawe"	{currTokenType = TOKEN_NET_RAWE; return;}
	"net:" {Whitespace} "ruso"	{currTokenType = TOKEN_NET_RUSO; return;}
	"net:" {Whitespace} "stat"	{currTokenType = TOKEN_NET_STAT; return;}
	"net:" {Whitespace} "ulin"	{currTokenType = TOKEN_NET_ULIN; return;}
	"net:" {Whitespace} "unik"	{currTokenType = TOKEN_NET_UNIK; return;}
	"net:" {Whitespace} "user"	{currTokenType = TOKEN_NET_USER; return;}
	"net:" {Whitespace} "what"	{currTokenType = TOKEN_NET_WHAT; return;}
	"net:" {Whitespace} "whod"	{currTokenType = TOKEN_NET_WHOD; return;}
	"net:" {Whitespace} "whof"	{currTokenType = TOKEN_NET_WHOF; return;}
	"net:" {Whitespace} "whon"	{currTokenType = TOKEN_NET_WHON; return;}
	"net:" {Whitespace} "whoz"	{currTokenType = TOKEN_NET_WHOZ; return;}
	"net:" {Whitespace} "writ"	{currTokenType = TOKEN_NET_WRIT; return;}
	
	"new:" {Whitespace} "comp"	{currTokenType = TOKEN_NEW_COMP; return;}
	"new:" {Whitespace} "crag"	{currTokenType = TOKEN_NEW_CRAG; return;}
	"new:" {Whitespace} "crea"	{currTokenType = TOKEN_NEW_CREA; return;}
	"new:" {Whitespace} "simp"	{currTokenType = TOKEN_NEW_SIMP; return;}
	"new:" {Whitespace} "vhcl"	{currTokenType = TOKEN_NEW_VHCL; return;}
	
	"ordr" {Whitespace} "shou"	{currTokenType = TOKEN_ORDR_SHOU; return;}
	"ordr" {Whitespace} "sign"	{currTokenType = TOKEN_ORDR_SIGN; return;}
	"ordr" {Whitespace} "tact"	{currTokenType = TOKEN_ORDR_TACT; return;}
	"ordr" {Whitespace} "writ"	{currTokenType = TOKEN_ORDR_WRIT; return;}
	
	"pat:" {Whitespace} "butt"	{currTokenType = TOKEN_PAT_BUTT; return;}
	"pat:" {Whitespace} "cmra"	{currTokenType = TOKEN_PAT_CMRA; return;}
	"pat:" {Whitespace} "dull"	{currTokenType = TOKEN_PAT_DULL; return;}
	"pat:" {Whitespace} "fixd"	{currTokenType = TOKEN_PAT_FIXD; return;}
	"pat:" {Whitespace} "grph"	{currTokenType = TOKEN_PAT_GRPH; return;}
	"pat:" {Whitespace} "kill"	{currTokenType = TOKEN_PAT_KILL; return;}
	"pat:" {Whitespace} "move"	{currTokenType = TOKEN_PAT_MOVE; return;}
	"pat:" {Whitespace} "text"	{currTokenType = TOKEN_PAT_TEXT; return;}
	
	"pray" {Whitespace} "agti"	{currTokenType = TOKEN_PRAY_AGTI; return;}
	"pray" {Whitespace} "agts"	{currTokenType = TOKEN_PRAY_AGTS; return;}
	"pray" {Whitespace} "back"	{currTokenType = TOKEN_PRAY_BACK; return;}
	"pray" {Whitespace} "coun"	{currTokenType = TOKEN_PRAY_COUN; return;}
	"pray" {Whitespace} "deps"	{currTokenType = TOKEN_PRAY_DEPS; return;}
	"pray" {Whitespace} "expo"	{currTokenType = TOKEN_PRAY_EXPO; return;}
	"pray" {Whitespace} "file"	{currTokenType = TOKEN_PRAY_FILE; return;}
	"pray" {Whitespace} "fore"	{currTokenType = TOKEN_PRAY_FORE; return;}
	"pray" {Whitespace} "garb"	{currTokenType = TOKEN_PRAY_GARB; return;}
	"pray" {Whitespace} "impo"	{currTokenType = TOKEN_PRAY_IMPO; return;}
	"pray" {Whitespace} "injt"	{currTokenType = TOKEN_PRAY_INJT; return;}
	"pray" {Whitespace} "kill"	{currTokenType = TOKEN_PRAY_KILL; return;}
	"pray" {Whitespace} "make"	{currTokenType = TOKEN_PRAY_MAKE; return;}
	"pray" {Whitespace} "next"	{currTokenType = TOKEN_PRAY_NEXT; return;}
	"pray" {Whitespace} "prev"	{currTokenType = TOKEN_PRAY_PREV; return;}
	"pray" {Whitespace} "refr"	{currTokenType = TOKEN_PRAY_REFR; return;}
	"pray" {Whitespace} "test"	{currTokenType = TOKEN_PRAY_TEST; return;}
	
	"prt:" {Whitespace} "bang"	{currTokenType = TOKEN_PRT_BANG; return;}
	"prt:" {Whitespace} "frma"	{currTokenType = TOKEN_PRT_FRMA; return;}
	"prt:" {Whitespace} "from"	{currTokenType = TOKEN_PRT_FROM; return;}
	"prt:" {Whitespace} "inew"	{currTokenType = TOKEN_PRT_INEW; return;}
	"prt:" {Whitespace} "itot"	{currTokenType = TOKEN_PRT_ITOT; return;}
	"prt:" {Whitespace} "izap"	{currTokenType = TOKEN_PRT_IZAP; return;}
	"prt:" {Whitespace} "join"	{currTokenType = TOKEN_PRT_JOIN; return;}
	"prt:" {Whitespace} "krak"	{currTokenType = TOKEN_PRT_KRAK; return;}
	"prt:" {Whitespace} "name"	{currTokenType = TOKEN_PRT_NAME; return;}
	"prt:" {Whitespace} "onew"	{currTokenType = TOKEN_PRT_ONEW; return;}
	"prt:" {Whitespace} "otot"	{currTokenType = TOKEN_PRT_OTOT; return;}
	"prt:" {Whitespace} "ozap"	{currTokenType = TOKEN_PRT_OZAP; return;}
	"prt:" {Whitespace} "send"	{currTokenType = TOKEN_PRT_SEND; return;}
	
	"stim" {Whitespace} "shou"	{currTokenType = TOKEN_STIM_SHOU; return;}
	"stim" {Whitespace} "sign"	{currTokenType = TOKEN_STIM_SIGN; return;}
	"stim" {Whitespace} "tact"	{currTokenType = TOKEN_STIM_TACT; return;}
	"stim" {Whitespace} "writ"	{currTokenType = TOKEN_STIM_WRIT; return;}
	
	"sway" {Whitespace} "shou"	{currTokenType = TOKEN_SWAY_SHOU; return;}
	"sway" {Whitespace} "sign"	{currTokenType = TOKEN_SWAY_SIGN; return;}
	"sway" {Whitespace} "tact"	{currTokenType = TOKEN_SWAY_TACT; return;}
	"sway" {Whitespace} "writ"	{currTokenType = TOKEN_SWAY_WRIT; return;}
	
	"urge" {Whitespace} "shou"	{currTokenType = TOKEN_URGE_SHOU; return;}
	"urge" {Whitespace} "sign"	{currTokenType = TOKEN_URGE_SIGN; return;}
	"urge" {Whitespace} "tact"	{currTokenType = TOKEN_URGE_TACT; return;}
	"urge" {Whitespace} "writ"	{currTokenType = TOKEN_URGE_WRIT; return;}
	
	"_cd_" {Whitespace} "ejct"	{currTokenType = TOKEN_CD_EJCT; return;}
	"_cd_" {Whitespace} "frqh"	{currTokenType = TOKEN_CD_FRQH; return;}
	"_cd_" {Whitespace} "frql"	{currTokenType = TOKEN_CD_FRQL; return;}
	"_cd_" {Whitespace} "frqm"	{currTokenType = TOKEN_CD_FRQM; return;}
	"_cd_" {Whitespace} "init"	{currTokenType = TOKEN_CD_INIT; return;}
	"_cd_" {Whitespace} "paws"	{currTokenType = TOKEN_CD_PAWS; return;}
	"_cd_" {Whitespace} "play"	{currTokenType = TOKEN_CD_PLAY; return;}
	"_cd_" {Whitespace} "shut"	{currTokenType = TOKEN_CD_SHUT; return;}
	"_cd_" {Whitespace} "stop"	{currTokenType = TOKEN_CD_STOP; return;}
	
	
	"abba"	{currTokenType = TOKEN_ABBA; return;}
	"absv"	{currTokenType = TOKEN_ABSV; return;}
	"accg"	{currTokenType = TOKEN_ACCG; return;}
	"acos"	{currTokenType = TOKEN_ACOS; return;}
	"addb"	{currTokenType = TOKEN_ADDB; return;}
	"addm"	{currTokenType = TOKEN_ADDM; return;}
	"addr"	{currTokenType = TOKEN_ADDR; return;}
	"adds"	{currTokenType = TOKEN_ADDS; return;}
	"addv"	{currTokenType = TOKEN_ADDV; return;}
	"adin"	{currTokenType = TOKEN_ADIN; return;}
	"admp"	{currTokenType = TOKEN_ADMP; return;}
	"aero"	{currTokenType = TOKEN_AERO; return;}
	"ages"	{currTokenType = TOKEN_AGES; return;}
	"agnt"	{currTokenType = TOKEN_AGNT; return;}
	"alph"	{currTokenType = TOKEN_ALPH; return;}
	"altr"	{currTokenType = TOKEN_ALTR; return;}
	"andv"	{currTokenType = TOKEN_ANDV; return;}
	"angl"	{currTokenType = TOKEN_ANGL; return;}
	"anim"	{currTokenType = TOKEN_ANIM; return;}
	"anms"	{currTokenType = TOKEN_ANMS; return;}
	"appr"	{currTokenType = TOKEN_APPR; return;}
	"apro"	{currTokenType = TOKEN_APRO; return;}
	"asin"	{currTokenType = TOKEN_ASIN; return;}
	"aslp"	{currTokenType = TOKEN_ASLP; return;}
	"atan"	{currTokenType = TOKEN_ATAN; return;}
	"attn"	{currTokenType = TOKEN_ATTN; return;}
	"attr"	{currTokenType = TOKEN_ATTR; return;}
	"avel"	{currTokenType = TOKEN_AVEL; return;}
	
	"bang"	{currTokenType = TOKEN_BANG; return;}
	"base"	{currTokenType = TOKEN_BASE; return;}
	"bhvr"	{currTokenType = TOKEN_BHVR; return;}
	"bkds"	{currTokenType = TOKEN_BKDS; return;}
	"bkgd"	{currTokenType = TOKEN_BKGD; return;}
	"body"	{currTokenType = TOKEN_BODY; return;}
	"boot"	{currTokenType = TOKEN_BOOT; return;}
	"born"	{currTokenType = TOKEN_BORN; return;}
	"brmi"	{currTokenType = TOKEN_BRMI; return;}
	"buzz"	{currTokenType = TOKEN_BUZZ; return;}
	"bvar"	{currTokenType = TOKEN_BVAR; return;}
	"byit"	{currTokenType = TOKEN_BYIT; return;}
	"cabb"	{currTokenType = TOKEN_CABB; return;}
	"cabl"	{currTokenType = TOKEN_CABL; return;}
	"cabn"	{currTokenType = TOKEN_CABN; return;}
	"cabp"	{currTokenType = TOKEN_CABP; return;}
	"cabr"	{currTokenType = TOKEN_CABR; return;}
	"cabt"	{currTokenType = TOKEN_CABT; return;}
	"cabv"	{currTokenType = TOKEN_CABV; return;}
	"cabw"	{currTokenType = TOKEN_CABW; return;}
	"cacl"	{currTokenType = TOKEN_CACL; return;}
	"cage"	{currTokenType = TOKEN_CAGE; return;}
	"calc"	{currTokenType = TOKEN_CALC; return;}
	"calg"	{currTokenType = TOKEN_CALG; return;}
	"call"	{currTokenType = TOKEN_CALL; return;}
	"caos"	{currTokenType = TOKEN_CAOS; return;}
	"carr"	{currTokenType = TOKEN_CARR; return;}
	"cata"	{currTokenType = TOKEN_CATA; return;}
	"cati"	{currTokenType = TOKEN_CATI; return;}
	"cato"	{currTokenType = TOKEN_CATO; return;}
	"catx"	{currTokenType = TOKEN_CATX; return;}
	"char"	{currTokenType = TOKEN_CHAR; return;}
	"chem"	{currTokenType = TOKEN_CHEM; return;}
	"clac"	{currTokenType = TOKEN_CLAC; return;}
	"clik"	{currTokenType = TOKEN_CLIK; return;}
	"cmra"	{currTokenType = TOKEN_CMRA; return;}
	"cmrp"	{currTokenType = TOKEN_CMRP; return;}
	"cmrt"	{currTokenType = TOKEN_CMRT; return;}
	"cmrx"	{currTokenType = TOKEN_CMRX; return;}
	"cmry"	{currTokenType = TOKEN_CMRY; return;}
	"code"	{currTokenType = TOKEN_CODE; return;}
	"codf"	{currTokenType = TOKEN_CODF; return;}
	"codg"	{currTokenType = TOKEN_CODG; return;}
	"codp"	{currTokenType = TOKEN_CODP; return;}
	"cods"	{currTokenType = TOKEN_CODS; return;}
	"core"	{currTokenType = TOKEN_CORE; return;}
	"cos_"	{currTokenType = TOKEN_COS; return;}
	"crea"	{currTokenType = TOKEN_CREA; return;}
	
	"date"	{currTokenType = TOKEN_DATE; return;}
	"dayt"	{currTokenType = TOKEN_DAYT; return;}
	"dbg#"	{currTokenType = TOKEN_DBG; return;}
	"dbga"	{currTokenType = TOKEN_DBGA; return;}
	"dcor"	{currTokenType = TOKEN_DCOR; return;}
	"dead"	{currTokenType = TOKEN_DEAD; return;}
	"decn"	{currTokenType = TOKEN_DECN; return;}
	"dele"	{currTokenType = TOKEN_DELE; return;}
	"delg"	{currTokenType = TOKEN_DELG; return;}
	"delm"	{currTokenType = TOKEN_DELM; return;}
	"deln"	{currTokenType = TOKEN_DELN; return;}
	"delr"	{currTokenType = TOKEN_DELR; return;}
	"delw"	{currTokenType = TOKEN_DELW; return;}
	"dftx"	{currTokenType = TOKEN_DFTX; return;}
	"dfty"	{currTokenType = TOKEN_DFTY; return;}
	"dirn"	{currTokenType = TOKEN_DIRN; return;}
	"disq"	{currTokenType = TOKEN_DISQ; return;}
	"divv"	{currTokenType = TOKEN_DIVV; return;}
	"dmap"	{currTokenType = TOKEN_DMAP; return;}
	"doca"	{currTokenType = TOKEN_DOCA; return;}
	"doin"	{currTokenType = TOKEN_DOIN; return;}
	"done"	{currTokenType = TOKEN_DONE; return;}
	"door"	{currTokenType = TOKEN_DOOR; return;}
	"down"	{currTokenType = TOKEN_DOWN; return;}
	"dpas"	{currTokenType = TOKEN_DPAS; return;}
	"drea"	{currTokenType = TOKEN_DREA; return;}
	"driv"	{currTokenType = TOKEN_DRIV; return;}
	"drop"	{currTokenType = TOKEN_DROP; return;}
	"drv!"	{currTokenType = TOKEN_DRV; return;}
	"dsee"	{currTokenType = TOKEN_DSEE; return;}
	
	"eamn"	{currTokenType = TOKEN_EAMN; return;}
	"elas"	{currTokenType = TOKEN_ELAS; return;}
	"emid"	{currTokenType = TOKEN_EMID; return;}
	"emit"	{currTokenType = TOKEN_EMIT; return;}
	"erid"	{currTokenType = TOKEN_ERID; return;}
	"etik"	{currTokenType = TOKEN_ETIK; return;}
	"expr"	{currTokenType = TOKEN_EXPR; return;}
	
	"face"	{currTokenType = TOKEN_FACE; return;}
	"fade"	{currTokenType = TOKEN_FADE; return;}
	"fall"	{currTokenType = TOKEN_FALL; return;}
	"fcus"	{currTokenType = TOKEN_FCUS; return;}
	"fdmp"	{currTokenType = TOKEN_FDMP; return;}
	"flto"	{currTokenType = TOKEN_FLTO; return;}
	"fltx"	{currTokenType = TOKEN_FLTX; return;}
	"flty"	{currTokenType = TOKEN_FLTY; return;}
	"fmly"	{currTokenType = TOKEN_FMLY; return;}
	"forf"	{currTokenType = TOKEN_FORF; return;}
	"frat"	{currTokenType = TOKEN_FRAT; return;}
	"frel"	{currTokenType = TOKEN_FREL; return;}
	"fric"	{currTokenType = TOKEN_FRIC; return;}
	"frmt"	{currTokenType = TOKEN_FRMT; return;}
	"frsh"	{currTokenType = TOKEN_FRSH; return;}
	"ftoi"	{currTokenType = TOKEN_FTOI; return;}
	"fvel"	{currTokenType = TOKEN_FVEL; return;}
	"fvwm"	{currTokenType = TOKEN_FVWM; return;}
	
	"gait"	{currTokenType = TOKEN_GAIT; return;}
	"gall"	{currTokenType = TOKEN_GALL; return;}
	"gamn"	{currTokenType = TOKEN_GAMN; return;}
	"gmap"	{currTokenType = TOKEN_GMAP; return;}
	"gnam"	{currTokenType = TOKEN_GNAM; return;}
	"gnus"	{currTokenType = TOKEN_GNUS; return;}
	"goto"	{currTokenType = TOKEN_GOTO; return;}
	"gpas"	{currTokenType = TOKEN_GPAS; return;}
	"grap"	{currTokenType = TOKEN_GRAP; return;}
	"grid"	{currTokenType = TOKEN_GRID; return;}
	"grpl"	{currTokenType = TOKEN_GRPL; return;}
	"grpv"	{currTokenType = TOKEN_GRPV; return;}
	"gtos"	{currTokenType = TOKEN_GTOS; return;}
	
	"hair"	{currTokenType = TOKEN_HAIR; return;}
	"hand"	{currTokenType = TOKEN_HAND; return;}
	"heap"	{currTokenType = TOKEN_HEAP; return;}
	"held"	{currTokenType = TOKEN_HELD; return;}
	"help"	{currTokenType = TOKEN_HELP; return;}
	"hght"	{currTokenType = TOKEN_HGHT; return;}
	"hhld"	{currTokenType = TOKEN_HHLD; return;}
	"hirp"	{currTokenType = TOKEN_HIRP; return;}
	"hotp"	{currTokenType = TOKEN_HOTP; return;}
	"hots"	{currTokenType = TOKEN_HOTS; return;}
	
	"iitt"	{currTokenType = TOKEN_IITT; return;}
	"imsk"	{currTokenType = TOKEN_IMSK; return;}
	"injr"	{currTokenType = TOKEN_INJR; return;}
	"innf"	{currTokenType = TOKEN_INNF; return;}
	"inni"	{currTokenType = TOKEN_INNI; return;}
	"innl"	{currTokenType = TOKEN_INNL; return;}
	"inok"	{currTokenType = TOKEN_INOK; return;}
	"ins#"	{currTokenType = TOKEN_INS; return;}
	"itof"	{currTokenType = TOKEN_ITOF; return;}
	
	"ject"	{currTokenType = TOKEN_JECT; return;}
	
	"keyd"	{currTokenType = TOKEN_KEYD; return;}
	"kill"	{currTokenType = TOKEN_KILL; return;}
	
	"left"	{currTokenType = TOKEN_LEFT; return;}
	"like"	{currTokenType = TOKEN_LIKE; return;}
	"limb"	{currTokenType = TOKEN_LIMB; return;}
	"line"	{currTokenType = TOKEN_LINE; return;}
	"link"	{currTokenType = TOKEN_LINK; return;}
	"load"	{currTokenType = TOKEN_LOAD; return;}
	"loci"	{currTokenType = TOKEN_LOCI; return;}
	"lock"	{currTokenType = TOKEN_LOCK; return;}
	"loft"	{currTokenType = TOKEN_LOFT; return;}
	"lorp"	{currTokenType = TOKEN_LORP; return;}
	"lowa"	{currTokenType = TOKEN_LOWA; return;}
	"ltcy"	{currTokenType = TOKEN_LTCY; return;}
	
	"mann"	{currTokenType = TOKEN_MANN; return;}
	"mapd"	{currTokenType = TOKEN_MAPD; return;}
	"maph"	{currTokenType = TOKEN_MAPH; return;}
	"mapk"	{currTokenType = TOKEN_MAPK; return;}
	"mapw"	{currTokenType = TOKEN_MAPW; return;}
	"mate"	{currTokenType = TOKEN_MATE; return;}
	"mclr"	{currTokenType = TOKEN_MCLR; return;}
	"memx"	{currTokenType = TOKEN_MEMX; return;}
	"meta"	{currTokenType = TOKEN_META; return;}
	"midi"	{currTokenType = TOKEN_MIDI; return;}
	"mind"	{currTokenType = TOKEN_MIND; return;}
	"mira"	{currTokenType = TOKEN_MIRA; return;}
	"mloc"	{currTokenType = TOKEN_MLOC; return;}
	"mmsc"	{currTokenType = TOKEN_MMSC; return;}
	"modu"	{currTokenType = TOKEN_MODU; return;}
	"modv"	{currTokenType = TOKEN_MODV; return;}
	"mont"	{currTokenType = TOKEN_MONT; return;}
	"mopx"	{currTokenType = TOKEN_MOPX; return;}
	"mopy"	{currTokenType = TOKEN_MOPY; return;}
	"motr"	{currTokenType = TOKEN_MOTR; return;}
	"mous"	{currTokenType = TOKEN_MOUS; return;}
	"movs"	{currTokenType = TOKEN_MOVS; return;}
	"movx"	{currTokenType = TOKEN_MOVX; return;}
	"movy"	{currTokenType = TOKEN_MOVY; return;}
	"mows"	{currTokenType = TOKEN_MOWS; return;} //LoL
	"msec"	{currTokenType = TOKEN_MSEC; return;}
	"mthx"	{currTokenType = TOKEN_MTHX; return;}
	"mthy"	{currTokenType = TOKEN_MTHY; return;}
	"mtoa"	{currTokenType = TOKEN_MTOA; return;}
	"mtoc"	{currTokenType = TOKEN_MTOC; return;}
	"mulv"	{currTokenType = TOKEN_MULV; return;}
	"mute"	{currTokenType = TOKEN_MUTE; return;}
	"mvby"	{currTokenType = TOKEN_MVBY; return;}
	"mvft"	{currTokenType = TOKEN_MVFT; return;}
	"mvsf"	{currTokenType = TOKEN_MVSF; return;}
	"mvto"	{currTokenType = TOKEN_MVTO; return;}
	
	"namn"	{currTokenType = TOKEN_NAMN; return;}
	"ncls"	{currTokenType = TOKEN_NCLS; return;}
	"negv"	{currTokenType = TOKEN_NEGV; return;}
	"newc"	{currTokenType = TOKEN_NEWC; return;}
	"nohh"	{currTokenType = TOKEN_NOHH; return;}
	"norn"	{currTokenType = TOKEN_NORN; return;}
	"notv"	{currTokenType = TOKEN_NOTV; return;}
	"npgs"	{currTokenType = TOKEN_NPGS; return;}
	"nude"	{currTokenType = TOKEN_NUDE; return;}
	"nwld"	{currTokenType = TOKEN_NWLD; return;}
	
	"obst"	{currTokenType = TOKEN_OBST; return;}
	"ooww"	{currTokenType = TOKEN_OOWW; return;}
	"orgf"	{currTokenType = TOKEN_ORGF; return;}
	"orgi"	{currTokenType = TOKEN_ORGI; return;}
	"orgn"	{currTokenType = TOKEN_ORGN; return;}
	"orrv"	{currTokenType = TOKEN_ORRV; return;}
	"outs"	{currTokenType = TOKEN_OUTS; return;}
	"outv"	{currTokenType = TOKEN_OUTV; return;}
	"outx"	{currTokenType = TOKEN_OUTX; return;}
	"over"	{currTokenType = TOKEN_OVER; return;}
	
	"pace"	{currTokenType = TOKEN_PACE; return;}
	"page"	{currTokenType = TOKEN_PAGE; return;}
	"part"	{currTokenType = TOKEN_PART; return;}
	"paus"	{currTokenType = TOKEN_PAUS; return;}
	"paws"	{currTokenType = TOKEN_PAWS; return;}
	"pcls"	{currTokenType = TOKEN_PCLS; return;}
	"perm"	{currTokenType = TOKEN_PERM; return;}
	"plmd"	{currTokenType = TOKEN_PLMD; return;}
	"plmu"	{currTokenType = TOKEN_PLMU; return;}
	"plne"	{currTokenType = TOKEN_PLNE; return;}
	"pnxt"	{currTokenType = TOKEN_PNXT; return;}
	"posb"	{currTokenType = TOKEN_POSB; return;}
	"pose"	{currTokenType = TOKEN_POSE; return;}
	"posl"	{currTokenType = TOKEN_POSL; return;}
	"posr"	{currTokenType = TOKEN_POSR; return;}
	"post"	{currTokenType = TOKEN_POST; return;}
	"posx"	{currTokenType = TOKEN_POSX; return;}
	"posy"	{currTokenType = TOKEN_POSY; return;}
	"prop"	{currTokenType = TOKEN_PROP; return;}
	"pswd"	{currTokenType = TOKEN_PSWD; return;}
	"ptxt"	{currTokenType = TOKEN_PTXT; return;}
	"puhl"	{currTokenType = TOKEN_PUHL; return;}
	"pupt"	{currTokenType = TOKEN_PUPT; return;}
	"pure"	{currTokenType = TOKEN_PURE; return;}
	
	"quit"	{currTokenType = TOKEN_QUIT; return;}
	
	"race"	{currTokenType = TOKEN_RACE; return;}
	"rand"	{currTokenType = TOKEN_RAND; return;}
	"rate"	{currTokenType = TOKEN_RATE; return;}
	"rclr"	{currTokenType = TOKEN_RCLR; return;}
	"read"	{currTokenType = TOKEN_READ; return;}
	"reaf"	{currTokenType = TOKEN_REAF; return;}
	"rean"	{currTokenType = TOKEN_REAN; return;}
	"reaq"	{currTokenType = TOKEN_REAQ; return;}
	"relx"	{currTokenType = TOKEN_RELX; return;}
	"rely"	{currTokenType = TOKEN_RELY; return;}
	"rgam"	{currTokenType = TOKEN_RGAM; return;}
	"rght"	{currTokenType = TOKEN_RGHT; return;}
	"rloc"	{currTokenType = TOKEN_RLOC; return;}
	"rmsc"	{currTokenType = TOKEN_RMSC; return;}
	"rnge"	{currTokenType = TOKEN_RNGE; return;}
	"room"	{currTokenType = TOKEN_ROOM; return;}
	"rotn"	{currTokenType = TOKEN_ROTN; return;}
	"rpas"	{currTokenType = TOKEN_RPAS; return;}
	"rtar"	{currTokenType = TOKEN_RTAR; return;}
	"rtif"	{currTokenType = TOKEN_RTIF; return;}
	"rtim"	{currTokenType = TOKEN_RTIM; return;}
	"rtyp"	{currTokenType = TOKEN_RTYP; return;}
	"save"	{currTokenType = TOKEN_SAVE; return;}
	"sayn"	{currTokenType = TOKEN_SAYN; return;}
	"scam"	{currTokenType = TOKEN_SCAM; return;}
	"scol"	{currTokenType = TOKEN_SCOL; return;}
	"scrx"	{currTokenType = TOKEN_SCRX; return;}
	"sdmp"	{currTokenType = TOKEN_SDMP; return;}
	"sean"	{currTokenType = TOKEN_SEAN; return;}
	"seee"	{currTokenType = TOKEN_SEEE; return;}
	"seen"	{currTokenType = TOKEN_SEEN; return;}
	"seta"	{currTokenType = TOKEN_SETA; return;}
	"sets"	{currTokenType = TOKEN_SETS; return;}
	"setv"	{currTokenType = TOKEN_SETV; return;}
	"sezz"	{currTokenType = TOKEN_SEZZ; return;}
	"show"	{currTokenType = TOKEN_SHOW; return;}
	"sins"	{currTokenType = TOKEN_SINS; return;}
	"sin_"	{currTokenType = TOKEN_SIN; return;}
	"snap"	{currTokenType = TOKEN_SNAP; return;}
	"snax"	{currTokenType = TOKEN_SNAX; return;}
	"sndc"	{currTokenType = TOKEN_SNDC; return;}
	"snde"	{currTokenType = TOKEN_SNDE; return;}
	"sndl"	{currTokenType = TOKEN_SNDL; return;}
	"sndw"	{currTokenType = TOKEN_SNDW; return;}
	"sorc"	{currTokenType = TOKEN_SORC; return;}
	"sorq"	{currTokenType = TOKEN_SORQ; return;}
	"soul"	{currTokenType = TOKEN_SOUL; return;}
	"spas"	{currTokenType = TOKEN_SPAS; return;}
	"spcs"	{currTokenType = TOKEN_SPCS; return;}
	"spin"	{currTokenType = TOKEN_SPIN; return;}
	"spnl"	{currTokenType = TOKEN_SPNL; return;}
	"sqrt"	{currTokenType = TOKEN_SQRT; return;}
	"star"	{currTokenType = TOKEN_STAR; return;}
	"step"	{currTokenType = TOKEN_STEP; return;}
	"stof"	{currTokenType = TOKEN_STOF; return;}
	"stoi"	{currTokenType = TOKEN_STOI; return;}
	"stop"	{currTokenType = TOKEN_STOP; return;}
	"stpc"	{currTokenType = TOKEN_STPC; return;}
	"stpt"	{currTokenType = TOKEN_STPT; return;}
	"strk"	{currTokenType = TOKEN_STRK; return;}
	"strl"	{currTokenType = TOKEN_STRL; return;}
	"subs"	{currTokenType = TOKEN_SUBS; return;}
	"subv"	{currTokenType = TOKEN_SUBV; return;}
	"svel"	{currTokenType = TOKEN_SVEL; return;}
	
	"tack"	{currTokenType = TOKEN_TACK; return;}
	"tage"	{currTokenType = TOKEN_TAGE; return;}
	"tan_"	{currTokenType = TOKEN_TAN; return;}
	"targ"	{currTokenType = TOKEN_TARG; return;}
	"tcor"	{currTokenType = TOKEN_TCOR; return;}
	"tick"	{currTokenType = TOKEN_TICK; return;}
	"time"	{currTokenType = TOKEN_TIME; return;}
	"tino"	{currTokenType = TOKEN_TINO; return;}
	"tint"	{currTokenType = TOKEN_TINT; return;}
	"tmvb"	{currTokenType = TOKEN_TMVB; return;}
	"tmvf"	{currTokenType = TOKEN_TMVF; return;}
	"tmvt"	{currTokenType = TOKEN_TMVT; return;}
	"tntw"	{currTokenType = TOKEN_TNTW; return;}
	"torx"	{currTokenType = TOKEN_TORX; return;}
	"tory"	{currTokenType = TOKEN_TORY; return;}
	"totl"	{currTokenType = TOKEN_TOTL; return;}
	"touc"	{currTokenType = TOKEN_TOUC; return;}
	"tran"	{currTokenType = TOKEN_TRAN; return;}
	"trck"	{currTokenType = TOKEN_TRCK; return;}
	"ttar"	{currTokenType = TOKEN_TTAR; return;}
	"twin"	{currTokenType = TOKEN_TWIN; return;}
	"type"	{currTokenType = TOKEN_TYPE; return;}
	
	"ucln"	{currTokenType = TOKEN_UCLN; return;}
	"ufos"	{currTokenType = TOKEN_UFOS; return;}
	"uftx"	{currTokenType = TOKEN_UFTX; return;}
	"ufty"	{currTokenType = TOKEN_UFTY; return;}
	"uncs"	{currTokenType = TOKEN_UNCS; return;}
	"unid"	{currTokenType = TOKEN_UNID; return;}
	"unlk"	{currTokenType = TOKEN_UNLK; return;}
	"uppa"	{currTokenType = TOKEN_UPPA; return;}
	
	"varc"	{currTokenType = TOKEN_VARC; return;}
	"velo"	{currTokenType = TOKEN_VELO; return;}
	"visi"	{currTokenType = TOKEN_VISI; return;}
	"vmjr"	{currTokenType = TOKEN_VMJR; return;}
	"vmnr"	{currTokenType = TOKEN_VMNR; return;}
	"vocb"	{currTokenType = TOKEN_VOCB; return;}
	"voic"	{currTokenType = TOKEN_VOIC; return;}
	"vois"	{currTokenType = TOKEN_VOIS; return;}
	"volm"	{currTokenType = TOKEN_VOLM; return;}
	"vtos"	{currTokenType = TOKEN_VTOS; return;}
	
	"wait"	{currTokenType = TOKEN_WAIT; return;}
	"walk"	{currTokenType = TOKEN_WALK; return;}
	"wall"	{currTokenType = TOKEN_WALL; return;}
	"wdow"	{currTokenType = TOKEN_WDOW; return;}
	"wdth"	{currTokenType = TOKEN_WDTH; return;}
	"wear"	{currTokenType = TOKEN_WEAR; return;}
	"webb"	{currTokenType = TOKEN_WEBB; return;}
	"wild"	{currTokenType = TOKEN_WILD; return;}
	"wnam"	{currTokenType = TOKEN_WNAM; return;}
	"wndb"	{currTokenType = TOKEN_WNDB; return;}
	"wndh"	{currTokenType = TOKEN_WNDH; return;}
	"wndl"	{currTokenType = TOKEN_WNDL; return;}
	"wndr"	{currTokenType = TOKEN_WNDR; return;}
	"wndt"	{currTokenType = TOKEN_WNDT; return;}
	"wndw"	{currTokenType = TOKEN_WNDW; return;}
	"wnti"	{currTokenType = TOKEN_WNTI; return;}
	"wolf"	{currTokenType = TOKEN_WOLF; return;}
	"wpau"	{currTokenType = TOKEN_WPAU; return;}
	"wrld"	{currTokenType = TOKEN_WRLD; return;}
	"wtik"	{currTokenType = TOKEN_WTIK; return;}
	"wtnt"	{currTokenType = TOKEN_WTNT; return;}
	"wuid"	{currTokenType = TOKEN_WUID; return;}
	
	
	"year"	{currTokenType = TOKEN_YEAR; return;}
	
	"zomb"	{currTokenType = TOKEN_ZOMB; return;}
	"zoom"	{currTokenType = TOKEN_ZOOM; return;}
	
	"_it_"	{currTokenType = TOKEN_IT; return;}
	"_up_"	{currTokenType = TOKEN_UP; return;}
	//Simple tokens>
	
	
	.	{throw SyntaxException.inst("Syntax error at line "+(yyline+1)+", col "+(yycolumn+1));}
}

