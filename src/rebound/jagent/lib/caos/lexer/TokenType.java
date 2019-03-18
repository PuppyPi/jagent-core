package rebound.jagent.lib.caos.lexer;

/**
 * The token type enum for {@link CAOSLexer}
 * @author RProgrammer
 */
public enum TokenType
{
	//Comment
	COMMENT (true),
	
	//Simple literals
	LITERAL_STRING (true),
	LITERAL_CHAR (true),
	LITERAL_DECIMAL (true),
	LITERAL_BINARY (true),
	
	//Complex literal parts
	BYTEARRAYLITERAL_OPEN (false),
	BYTEARRAYLITERAL_CLOSE (false),
	
	//Subroutine constructs
	SUBROUTINE_SUBR (true),
	SUBROUTINE_GSUB (true),
	
	//Numbered variables
	NUMVAR_VA (true),
	NUMVAR_OV (true),
	NUMVAR_MV (true),
	
	
	//<Simple tokens (lexically)
	TOKEN_EQ (false),
	TOKEN_NE (false),
	TOKEN_GT (false),
	TOKEN_LT (false),
	TOKEN_GE (false),
	TOKEN_LE (false),
	TOKEN_AND (false),
	TOKEN_OR (false),
	
	
	TOKEN_SCRP (false),
	TOKEN_ISCR (false),
	TOKEN_RSCR (false),
	TOKEN_ENDM (false),
	TOKEN_DOIF (false),
	TOKEN_ELIF (false),
	TOKEN_ELSE (false),
	TOKEN_LOOP (false),
	TOKEN_REPS (false),
	TOKEN_ENUM (false),
	TOKEN_EPAS (false),
	TOKEN_ESEE (false),
	TOKEN_ETCH (false),
	TOKEN_ECON (false),
	TOKEN_RETN (false),
	TOKEN_ENDI (false),
	TOKEN_EVER (false),
	TOKEN_UNTL (false),
	TOKEN_REPE (false),
	TOKEN_NEXT (false),
	TOKEN_INST (false),
	TOKEN_SLOW (false),
	TOKEN_AVAR (false),
	TOKEN_EAME (false),
	TOKEN_FROM (false),
	TOKEN_GAME (false),
	TOKEN_MAME (false),
	TOKEN_NAME (false),
	TOKEN_VELX (false),
	TOKEN_VELY (false),
	TOKEN_P1 (false),
	TOKEN_P2 (false),
	TOKEN_PNTR (false),
	TOKEN_OWNR (false),
	TOKEN_NULL (false),

	
	
	//Calls
	TOKEN_BRN_DMPB (false),
	TOKEN_BRN_DMPD (false),
	TOKEN_BRN_DMPL (false),
	TOKEN_BRN_DMPT (false),
	TOKEN_BRN_SETD (false),
	TOKEN_BRN_SETL (false),
	TOKEN_BRN_SETN (false),
	TOKEN_BRN_SETT (false),

	TOKEN_DBG_ASRT (false),
	TOKEN_DBG_CPRO (false),
	TOKEN_DBG_FLSH (false),
	TOKEN_DBG_HTML (false),
	TOKEN_DBG_OUTS (false),
	TOKEN_DBG_OUTV (false),
	TOKEN_DBG_PAWS (false),
	TOKEN_DBG_PLAY (false),
	TOKEN_DBG_POLL (false),
	TOKEN_DBG_PROF (false),
	TOKEN_DBG_TACK (false),
	TOKEN_DBG_TOCK (false),
	TOKEN_DBG_WTIK (false),

	TOKEN_FILE_GLOB (false),
	TOKEN_FILE_ICLO (false),
	TOKEN_FILE_IOPE (false),
	TOKEN_FILE_JDEL (false),
	TOKEN_FILE_OCLO (false),
	TOKEN_FILE_OFLU (false),
	TOKEN_FILE_OOPE (false),

	TOKEN_GENE_CLON (false),
	TOKEN_GENE_CROS (false),
	TOKEN_GENE_KILL (false),
	TOKEN_GENE_LOAD (false),
	TOKEN_GENE_MOVE (false),

	TOKEN_GIDS_FMLY (false),
	TOKEN_GIDS_GNUS (false),
	TOKEN_GIDS_ROOT (false),
	TOKEN_GIDS_SPCS (false),

	TOKEN_HIST_CAGE (false),
	TOKEN_HIST_COUN (false),
	TOKEN_HIST_CROS (false),
	TOKEN_HIST_DATE (false),
	TOKEN_HIST_EVNT (false),
	TOKEN_HIST_FIND (false),
	TOKEN_HIST_FINR (false),
	TOKEN_HIST_FOTO (false),
	TOKEN_HIST_GEND (false),
	TOKEN_HIST_GNUS (false),
	TOKEN_HIST_MON1 (false),
	TOKEN_HIST_MON2 (false),
	TOKEN_HIST_MUTE (false),
	TOKEN_HIST_NAME (false),
	TOKEN_HIST_NETU (false),
	TOKEN_HIST_NEXT (false),
	TOKEN_HIST_PREV (false),
	TOKEN_HIST_RTIM (false),
	TOKEN_HIST_SEAN (false),
	TOKEN_HIST_TAGE (false),
	TOKEN_HIST_TIME (false),
	TOKEN_HIST_TYPE (false),
	TOKEN_HIST_UTXT (false),
	TOKEN_HIST_VARI (false),
	TOKEN_HIST_WIPE (false),
	TOKEN_HIST_WNAM (false),
	TOKEN_HIST_WTIK (false),
	TOKEN_HIST_WUID (false),
	TOKEN_HIST_WVET (false),
	TOKEN_HIST_YEAR (false),

	TOKEN_MESG_WRIT (false),
	TOKEN_MESG_WRT (false),

	TOKEN_NET_ERRA (false),
	TOKEN_NET_EXPO (false),
	TOKEN_NET_FROM (false),
	TOKEN_NET_HEAD (false),
	TOKEN_NET_HEAR (false),
	TOKEN_NET_HOST (false),
	TOKEN_NET_LINE (false),
	TOKEN_NET_MAKE (false),
	TOKEN_NET_PASS (false),
	TOKEN_NET_RAWE (false),
	TOKEN_NET_RUSO (false),
	TOKEN_NET_STAT (false),
	TOKEN_NET_ULIN (false),
	TOKEN_NET_UNIK (false),
	TOKEN_NET_USER (false),
	TOKEN_NET_WHAT (false),
	TOKEN_NET_WHOD (false),
	TOKEN_NET_WHOF (false),
	TOKEN_NET_WHON (false),
	TOKEN_NET_WHOZ (false),
	TOKEN_NET_WRIT (false),

	TOKEN_NEW_COMP (false),
	TOKEN_NEW_CRAG (false),
	TOKEN_NEW_CREA (false),
	TOKEN_NEW_SIMP (false),
	TOKEN_NEW_VHCL (false),

	TOKEN_ORDR_SHOU (false),
	TOKEN_ORDR_SIGN (false),
	TOKEN_ORDR_TACT (false),
	TOKEN_ORDR_WRIT (false),

	TOKEN_PAT_BUTT (false),
	TOKEN_PAT_CMRA (false),
	TOKEN_PAT_DULL (false),
	TOKEN_PAT_FIXD (false),
	TOKEN_PAT_GRPH (false),
	TOKEN_PAT_KILL (false),
	TOKEN_PAT_MOVE (false),
	TOKEN_PAT_TEXT (false),

	TOKEN_PRAY_AGTI (false),
	TOKEN_PRAY_AGTS (false),
	TOKEN_PRAY_BACK (false),
	TOKEN_PRAY_COUN (false),
	TOKEN_PRAY_DEPS (false),
	TOKEN_PRAY_EXPO (false),
	TOKEN_PRAY_FILE (false),
	TOKEN_PRAY_FORE (false),
	TOKEN_PRAY_GARB (false),
	TOKEN_PRAY_IMPO (false),
	TOKEN_PRAY_INJT (false),
	TOKEN_PRAY_KILL (false),
	TOKEN_PRAY_MAKE (false),
	TOKEN_PRAY_NEXT (false),
	TOKEN_PRAY_PREV (false),
	TOKEN_PRAY_REFR (false),
	TOKEN_PRAY_TEST (false),

	TOKEN_PRT_BANG (false),
	TOKEN_PRT_FRMA (false),
	TOKEN_PRT_FROM (false),
	TOKEN_PRT_INEW (false),
	TOKEN_PRT_ITOT (false),
	TOKEN_PRT_IZAP (false),
	TOKEN_PRT_JOIN (false),
	TOKEN_PRT_KRAK (false),
	TOKEN_PRT_NAME (false),
	TOKEN_PRT_ONEW (false),
	TOKEN_PRT_OTOT (false),
	TOKEN_PRT_OZAP (false),
	TOKEN_PRT_SEND (false),

	TOKEN_STIM_SHOU (false),
	TOKEN_STIM_SIGN (false),
	TOKEN_STIM_TACT (false),
	TOKEN_STIM_WRIT (false),

	TOKEN_SWAY_SHOU (false),
	TOKEN_SWAY_SIGN (false),
	TOKEN_SWAY_TACT (false),
	TOKEN_SWAY_WRIT (false),

	TOKEN_URGE_SHOU (false),
	TOKEN_URGE_SIGN (false),
	TOKEN_URGE_TACT (false),
	TOKEN_URGE_WRIT (false),

	TOKEN_CD_EJCT (false),
	TOKEN_CD_FRQH (false),
	TOKEN_CD_FRQL (false),
	TOKEN_CD_FRQM (false),
	TOKEN_CD_INIT (false),
	TOKEN_CD_PAWS (false),
	TOKEN_CD_PLAY (false),
	TOKEN_CD_SHUT (false),
	TOKEN_CD_STOP (false),


	TOKEN_ABBA (false),
	TOKEN_ABSV (false),
	TOKEN_ACCG (false),
	TOKEN_ACOS (false),
	TOKEN_ADDB (false),
	TOKEN_ADDM (false),
	TOKEN_ADDR (false),
	TOKEN_ADDS (false),
	TOKEN_ADDV (false),
	TOKEN_ADIN (false),
	TOKEN_ADMP (false),
	TOKEN_AERO (false),
	TOKEN_AGES (false),
	TOKEN_AGNT (false),
	TOKEN_ALPH (false),
	TOKEN_ALTR (false),
	TOKEN_ANDV (false),
	TOKEN_ANGL (false),
	TOKEN_ANIM (false),
	TOKEN_ANMS (false),
	TOKEN_APPR (false),
	TOKEN_APRO (false),
	TOKEN_ASIN (false),
	TOKEN_ASLP (false),
	TOKEN_ATAN (false),
	TOKEN_ATTN (false),
	TOKEN_ATTR (false),
	TOKEN_AVEL (false),

	TOKEN_BANG (false),
	TOKEN_BASE (false),
	TOKEN_BHVR (false),
	TOKEN_BKDS (false),
	TOKEN_BKGD (false),
	TOKEN_BODY (false),
	TOKEN_BOOT (false),
	TOKEN_BORN (false),
	TOKEN_BRMI (false),
	TOKEN_BUZZ (false),
	TOKEN_BVAR (false),
	TOKEN_BYIT (false),
	TOKEN_CABB (false),
	TOKEN_CABL (false),
	TOKEN_CABN (false),
	TOKEN_CABP (false),
	TOKEN_CABR (false),
	TOKEN_CABT (false),
	TOKEN_CABV (false),
	TOKEN_CABW (false),
	TOKEN_CACL (false),
	TOKEN_CAGE (false),
	TOKEN_CALC (false),
	TOKEN_CALG (false),
	TOKEN_CALL (false),
	TOKEN_CAOS (false),
	TOKEN_CARR (false),
	TOKEN_CATA (false),
	TOKEN_CATI (false),
	TOKEN_CATO (false),
	TOKEN_CATX (false),
	TOKEN_CHAR (false),
	TOKEN_CHEM (false),
	TOKEN_CLAC (false),
	TOKEN_CLIK (false),
	TOKEN_CMRA (false),
	TOKEN_CMRP (false),
	TOKEN_CMRT (false),
	TOKEN_CMRX (false),
	TOKEN_CMRY (false),
	TOKEN_CODE (false),
	TOKEN_CODF (false),
	TOKEN_CODG (false),
	TOKEN_CODP (false),
	TOKEN_CODS (false),
	TOKEN_CORE (false),
	TOKEN_COS (false),
	TOKEN_CREA (false),

	TOKEN_DATE (false),
	TOKEN_DAYT (false),
	TOKEN_DBG (false),
	TOKEN_DBGA (false),
	TOKEN_DCOR (false),
	TOKEN_DEAD (false),
	TOKEN_DECN (false),
	TOKEN_DELE (false),
	TOKEN_DELG (false),
	TOKEN_DELM (false),
	TOKEN_DELN (false),
	TOKEN_DELR (false),
	TOKEN_DELW (false),
	TOKEN_DFTX (false),
	TOKEN_DFTY (false),
	TOKEN_DIRN (false),
	TOKEN_DISQ (false),
	TOKEN_DIVV (false),
	TOKEN_DMAP (false),
	TOKEN_DOCA (false),
	TOKEN_DOIN (false),
	TOKEN_DONE (false),
	TOKEN_DOOR (false),
	TOKEN_DOWN (false),
	TOKEN_DPAS (false),
	TOKEN_DREA (false),
	TOKEN_DRIV (false),
	TOKEN_DROP (false),
	TOKEN_DRV (false),
	TOKEN_DSEE (false),

	TOKEN_EAMN (false),
	TOKEN_ELAS (false),
	TOKEN_EMID (false),
	TOKEN_EMIT (false),
	TOKEN_ERID (false),
	TOKEN_ETIK (false),
	TOKEN_EXPR (false),

	TOKEN_FACE (false),
	TOKEN_FADE (false),
	TOKEN_FALL (false),
	TOKEN_FCUS (false),
	TOKEN_FDMP (false),
	TOKEN_FLTO (false),
	TOKEN_FLTX (false),
	TOKEN_FLTY (false),
	TOKEN_FMLY (false),
	TOKEN_FORF (false),
	TOKEN_FRAT (false),
	TOKEN_FREL (false),
	TOKEN_FRIC (false),
	TOKEN_FRMT (false),
	TOKEN_FRSH (false),
	TOKEN_FTOI (false),
	TOKEN_FVEL (false),
	TOKEN_FVWM (false),

	TOKEN_GAIT (false),
	TOKEN_GALL (false),
	TOKEN_GAMN (false),
	TOKEN_GMAP (false),
	TOKEN_GNAM (false),
	TOKEN_GNUS (false),
	TOKEN_GOTO (false),
	TOKEN_GPAS (false),
	TOKEN_GRAP (false),
	TOKEN_GRID (false),
	TOKEN_GRPL (false),
	TOKEN_GRPV (false),
	TOKEN_GTOS (false),

	TOKEN_HAIR (false),
	TOKEN_HAND (false),
	TOKEN_HEAP (false),
	TOKEN_HELD (false),
	TOKEN_HELP (false),
	TOKEN_HGHT (false),
	TOKEN_HHLD (false),
	TOKEN_HIRP (false),
	TOKEN_HOTP (false),
	TOKEN_HOTS (false),

	TOKEN_IITT (false),
	TOKEN_IMSK (false),
	TOKEN_INJR (false),
	TOKEN_INNF (false),
	TOKEN_INNI (false),
	TOKEN_INNL (false),
	TOKEN_INOK (false),
	TOKEN_INS (false),
	TOKEN_ITOF (false),

	TOKEN_JECT (false),

	TOKEN_KEYD (false),
	TOKEN_KILL (false),

	TOKEN_LEFT (false),
	TOKEN_LIKE (false),
	TOKEN_LIMB (false),
	TOKEN_LINE (false),
	TOKEN_LINK (false),
	TOKEN_LOAD (false),
	TOKEN_LOCI (false),
	TOKEN_LOCK (false),
	TOKEN_LOFT (false),
	TOKEN_LORP (false),
	TOKEN_LOWA (false),
	TOKEN_LTCY (false),

	TOKEN_MANN (false),
	TOKEN_MAPD (false),
	TOKEN_MAPH (false),
	TOKEN_MAPK (false),
	TOKEN_MAPW (false),
	TOKEN_MATE (false),
	TOKEN_MCLR (false),
	TOKEN_MEMX (false),
	TOKEN_META (false),
	TOKEN_MIDI (false),
	TOKEN_MIND (false),
	TOKEN_MIRA (false),
	TOKEN_MLOC (false),
	TOKEN_MMSC (false),
	TOKEN_MODU (false),
	TOKEN_MODV (false),
	TOKEN_MONT (false),
	TOKEN_MOPX (false),
	TOKEN_MOPY (false),
	TOKEN_MOTR (false),
	TOKEN_MOUS (false),
	TOKEN_MOVS (false),
	TOKEN_MOVX (false),
	TOKEN_MOVY (false),
	TOKEN_MOWS (false), //LoL
	TOKEN_MSEC (false),
	TOKEN_MTHX (false),
	TOKEN_MTHY (false),
	TOKEN_MTOA (false),
	TOKEN_MTOC (false),
	TOKEN_MULV (false),
	TOKEN_MUTE (false),
	TOKEN_MVBY (false),
	TOKEN_MVFT (false),
	TOKEN_MVSF (false),
	TOKEN_MVTO (false),

	TOKEN_NAMN (false),
	TOKEN_NCLS (false),
	TOKEN_NEGV (false),
	TOKEN_NEWC (false),
	TOKEN_NOHH (false),
	TOKEN_NORN (false),
	TOKEN_NOTV (false),
	TOKEN_NPGS (false),
	TOKEN_NUDE (false),
	TOKEN_NWLD (false),

	TOKEN_OBST (false),
	TOKEN_OOWW (false),
	TOKEN_ORGF (false),
	TOKEN_ORGI (false),
	TOKEN_ORGN (false),
	TOKEN_ORRV (false),
	TOKEN_OUTS (false),
	TOKEN_OUTV (false),
	TOKEN_OUTX (false),
	TOKEN_OVER (false),

	TOKEN_PACE (false),
	TOKEN_PAGE (false),
	TOKEN_PART (false),
	TOKEN_PAUS (false),
	TOKEN_PAWS (false),
	TOKEN_PCLS (false),
	TOKEN_PERM (false),
	TOKEN_PLMD (false),
	TOKEN_PLMU (false),
	TOKEN_PLNE (false),
	TOKEN_PNXT (false),
	TOKEN_POSB (false),
	TOKEN_POSE (false),
	TOKEN_POSL (false),
	TOKEN_POSR (false),
	TOKEN_POST (false),
	TOKEN_POSX (false),
	TOKEN_POSY (false),
	TOKEN_PROP (false),
	TOKEN_PSWD (false),
	TOKEN_PTXT (false),
	TOKEN_PUHL (false),
	TOKEN_PUPT (false),
	TOKEN_PURE (false),

	TOKEN_QUIT (false),

	TOKEN_RACE (false),
	TOKEN_RAND (false),
	TOKEN_RATE (false),
	TOKEN_RCLR (false),
	TOKEN_READ (false),
	TOKEN_REAF (false),
	TOKEN_REAN (false),
	TOKEN_REAQ (false),
	TOKEN_RELX (false),
	TOKEN_RELY (false),
	TOKEN_RGAM (false),
	TOKEN_RGHT (false),
	TOKEN_RLOC (false),
	TOKEN_RMSC (false),
	TOKEN_RNGE (false),
	TOKEN_ROOM (false),
	TOKEN_ROTN (false),
	TOKEN_RPAS (false),
	TOKEN_RTAR (false),
	TOKEN_RTIF (false),
	TOKEN_RTIM (false),
	TOKEN_RTYP (false),
	TOKEN_SAVE (false),
	TOKEN_SAYN (false),
	TOKEN_SCAM (false),
	TOKEN_SCOL (false),
	TOKEN_SCRX (false),
	TOKEN_SDMP (false),
	TOKEN_SEAN (false),
	TOKEN_SEEE (false),
	TOKEN_SEEN (false),
	TOKEN_SETA (false),
	TOKEN_SETS (false),
	TOKEN_SETV (false),
	TOKEN_SEZZ (false),
	TOKEN_SHOW (false),
	TOKEN_SINS (false),
	TOKEN_SIN (false),
	TOKEN_SNAP (false),
	TOKEN_SNAX (false),
	TOKEN_SNDC (false),
	TOKEN_SNDE (false),
	TOKEN_SNDL (false),
	TOKEN_SNDW (false),
	TOKEN_SORC (false),
	TOKEN_SORQ (false),
	TOKEN_SOUL (false),
	TOKEN_SPAS (false),
	TOKEN_SPCS (false),
	TOKEN_SPIN (false),
	TOKEN_SPNL (false),
	TOKEN_SQRT (false),
	TOKEN_STAR (false),
	TOKEN_STEP (false),
	TOKEN_STOF (false),
	TOKEN_STOI (false),
	TOKEN_STOP (false),
	TOKEN_STPC (false),
	TOKEN_STPT (false),
	TOKEN_STRK (false),
	TOKEN_STRL (false),
	TOKEN_SUBS (false),
	TOKEN_SUBV (false),
	TOKEN_SVEL (false),

	TOKEN_TACK (false),
	TOKEN_TAGE (false),
	TOKEN_TAN (false),
	TOKEN_TARG (false),
	TOKEN_TCOR (false),
	TOKEN_TICK (false),
	TOKEN_TIME (false),
	TOKEN_TINO (false),
	TOKEN_TINT (false),
	TOKEN_TMVB (false),
	TOKEN_TMVF (false),
	TOKEN_TMVT (false),
	TOKEN_TNTW (false),
	TOKEN_TORX (false),
	TOKEN_TORY (false),
	TOKEN_TOTL (false),
	TOKEN_TOUC (false),
	TOKEN_TRAN (false),
	TOKEN_TRCK (false),
	TOKEN_TTAR (false),
	TOKEN_TWIN (false),
	TOKEN_TYPE (false),

	TOKEN_UCLN (false),
	TOKEN_UFOS (false),
	TOKEN_UFTX (false),
	TOKEN_UFTY (false),
	TOKEN_UNCS (false),
	TOKEN_UNID (false),
	TOKEN_UNLK (false),
	TOKEN_UPPA (false),

	TOKEN_VARC (false),
	TOKEN_VELO (false),
	TOKEN_VISI (false),
	TOKEN_VMJR (false),
	TOKEN_VMNR (false),
	TOKEN_VOCB (false),
	TOKEN_VOIC (false),
	TOKEN_VOIS (false),
	TOKEN_VOLM (false),
	TOKEN_VTOS (false),

	TOKEN_WAIT (false),
	TOKEN_WALK (false),
	TOKEN_WALL (false),
	TOKEN_WDOW (false),
	TOKEN_WDTH (false),
	TOKEN_WEAR (false),
	TOKEN_WEBB (false),
	TOKEN_WILD (false),
	TOKEN_WNAM (false),
	TOKEN_WNDB (false),
	TOKEN_WNDH (false),
	TOKEN_WNDL (false),
	TOKEN_WNDR (false),
	TOKEN_WNDT (false),
	TOKEN_WNDW (false),
	TOKEN_WNTI (false),
	TOKEN_WOLF (false),
	TOKEN_WPAU (false),
	TOKEN_WRLD (false),
	TOKEN_WTIK (false),
	TOKEN_WTNT (false),
	TOKEN_WUID (false),


	TOKEN_YEAR (false),

	TOKEN_ZOMB (false),
	TOKEN_ZOOM (false),

	TOKEN_IT (false),
	TOKEN_UP (false),
	//Simple tokens>
	;
	
	
	protected final boolean hasValue;
	
	//	protected final Category category; //Todo, eg: Command, Function, Constant (or nullary func), Variable
	//	protected final int arity; //Todo
	
	
	private TokenType(boolean hasValue)
	{
		this.hasValue = hasValue;
	}
	
	
	
	public boolean isValueUsed()
	{
		return this.hasValue;
	}
	
	
	
//	Add if needed
//	public static enum Category
//	{
//		/*
//		 * Potentially confusing cases:
//		 * 
//		 * NULL, OWNR, TARG - Call:Function,Nullary
//		 * TARG - Call:Command,Unary
//		 */
//		
//		
//		CALL, //Command or Function; something that is in the runnable code and could take args (though nullary ones exist, eg: fmly, gnus, spcs)
//		VAR, //Anything that can be set with SETx (setv, seta, sets, ...)
//		OPERATOR, //Used in doif,elif; eg, '!=', 'ne', 'and', etc.
//		
//		FLOW_BLOCK_OPEN,
//		FLOW_BLOCK_CLOSE,
//	}
	
	
	
	
	
	
	
	
	
	
	//<Utils
	/**
	 * extracts the body of the comment
	 */
	public static String getCommentText(String value)
	{
		return value.substring(1); //Strip "*"
	}
	
	/**
	 * extracts the subroutine label from a gsub or subr construct
	 */
	public static String getSubLabel(String value)
	{
		return value.substring(4).trim();
	}
	
	/**
	 * parses an integer literal into an actual integer
	 */
	public static int getIntegerLiteralValue(TokenType type, String value)
	{
		if (type == LITERAL_DECIMAL)
		{
			if (value.equals("-"))
				return 0;
			else
				return Integer.parseInt(value, 10);
		}
		else if (type == LITERAL_BINARY)
		{
			return Integer.parseInt(value.substring(1), 2);
		}
		else if (type == LITERAL_CHAR)
		{
			if (value.length() == 3)
				return (int)value.charAt(1); //in between the single quotes
			else //if value.length() == 4
				return (int)value.charAt(2); //for '\''
		}
		else
			throw new IllegalArgumentException("Invalid type "+type);
	}
	
	/**
	 * parses an float literal into an actual float
	 */
	public static float getFloatLiteralValue(String value)
	{
		if (value.equals("-") || value.equals(".") || value.equals("-."))
			return 0f;
		return Float.parseFloat(value);
	}
	
	/**
	 * tests if the token is an integer literal
	 */
	public static boolean isIntegerLiteral(TokenType type, String value)
	{
		return type == LITERAL_CHAR || type == LITERAL_BINARY || (type == LITERAL_DECIMAL && !isDecimalFloat(value));
	}
	
	/**
	 * tests if the decimal literal is an integer (false) or float (true)
	 */
	public static boolean isDecimalFloat(String value)
	{
		//No exponents allowed in caos
		return value.indexOf('.') != -1;
	}
	
	/**
	 * extracts the XX from vaXX, ovXX, mvXX
	 */
	public static int getVariableNumber(String value)
	{
		return Integer.parseInt(value.substring(2));
	}
	//Utils>
}
