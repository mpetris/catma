// $ANTLR null C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g 2020-09-12 13:11:49

package de.catma.queryengine.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CatmaQueryLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__40=40;
	public static final int T__41=41;
	public static final int T__42=42;
	public static final int T__43=43;
	public static final int T__44=44;
	public static final int T__45=45;
	public static final int T__46=46;
	public static final int T__47=47;
	public static final int T__48=48;
	public static final int T__49=49;
	public static final int T__50=50;
	public static final int COMMENT=4;
	public static final int EQUAL=5;
	public static final int FREQ=6;
	public static final int GROUPIDENT=7;
	public static final int INT=8;
	public static final int LETTER=9;
	public static final int LETTEREXTENDED=10;
	public static final int MATCH_MODE=11;
	public static final int ND_ADJACENCY=12;
	public static final int ND_ANDREFINE=13;
	public static final int ND_COLLOC=14;
	public static final int ND_COMMENT=15;
	public static final int ND_EXCLUSION=16;
	public static final int ND_FREQ=17;
	public static final int ND_ORREFINE=18;
	public static final int ND_PHRASE=19;
	public static final int ND_PROPERTY=20;
	public static final int ND_QUERY=21;
	public static final int ND_REFINE=22;
	public static final int ND_REG=23;
	public static final int ND_SIMIL=24;
	public static final int ND_TAG=25;
	public static final int ND_TAGDIFF=26;
	public static final int ND_TAGPROPERTY=27;
	public static final int ND_UNION=28;
	public static final int ND_WILD=29;
	public static final int PROPERTY=30;
	public static final int REG=31;
	public static final int SIMIL=32;
	public static final int TAG=33;
	public static final int TAGDIFF=34;
	public static final int TXT=35;
	public static final int UNEQUAL=36;
	public static final int VALUE=37;
	public static final int WHITESPACE=38;
	public static final int WILD=39;


	/**
	* overrides the default error handling. enables immediate failure
	*/
	public void reportError(RecognitionException e) {
		throw new RuntimeException(e);
	}



	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public CatmaQueryLexer() {} 
	public CatmaQueryLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public CatmaQueryLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g"; }

	// $ANTLR start "T__40"
	public final void mT__40() throws RecognitionException {
		try {
			int _type = T__40;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:16:7: ( '%' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:16:9: '%'
			{
			match('%'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__40"

	// $ANTLR start "T__41"
	public final void mT__41() throws RecognitionException {
		try {
			int _type = T__41;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:17:7: ( '&' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:17:9: '&'
			{
			match('&'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__41"

	// $ANTLR start "T__42"
	public final void mT__42() throws RecognitionException {
		try {
			int _type = T__42;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:18:7: ( '(' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:18:9: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__42"

	// $ANTLR start "T__43"
	public final void mT__43() throws RecognitionException {
		try {
			int _type = T__43;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:19:7: ( ')' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:19:9: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__43"

	// $ANTLR start "T__44"
	public final void mT__44() throws RecognitionException {
		try {
			int _type = T__44;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:20:7: ( ',' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:20:9: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__44"

	// $ANTLR start "T__45"
	public final void mT__45() throws RecognitionException {
		try {
			int _type = T__45;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:21:7: ( '-' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:21:9: '-'
			{
			match('-'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__45"

	// $ANTLR start "T__46"
	public final void mT__46() throws RecognitionException {
		try {
			int _type = T__46;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:22:7: ( ';' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:22:9: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__46"

	// $ANTLR start "T__47"
	public final void mT__47() throws RecognitionException {
		try {
			int _type = T__47;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:23:7: ( 'CI' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:23:9: 'CI'
			{
			match("CI"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__47"

	// $ANTLR start "T__48"
	public final void mT__48() throws RecognitionException {
		try {
			int _type = T__48;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:24:7: ( 'EXCL' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:24:9: 'EXCL'
			{
			match("EXCL"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__48"

	// $ANTLR start "T__49"
	public final void mT__49() throws RecognitionException {
		try {
			int _type = T__49;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:25:7: ( 'where' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:25:9: 'where'
			{
			match("where"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__49"

	// $ANTLR start "T__50"
	public final void mT__50() throws RecognitionException {
		try {
			int _type = T__50;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:26:7: ( '|' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:26:9: '|'
			{
			match('|'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__50"

	// $ANTLR start "TAG"
	public final void mTAG() throws RecognitionException {
		try {
			int _type = TAG;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:269:6: ( 'tag' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:269:8: 'tag'
			{
			match("tag"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TAG"

	// $ANTLR start "TAGDIFF"
	public final void mTAGDIFF() throws RecognitionException {
		try {
			int _type = TAGDIFF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:268:9: ( 'tagdiff' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:268:11: 'tagdiff'
			{
			match("tagdiff"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TAGDIFF"

	// $ANTLR start "MATCH_MODE"
	public final void mMATCH_MODE() throws RecognitionException {
		try {
			int _type = MATCH_MODE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:272:2: ( 'boundary' | 'overlap' | 'exact' )
			int alt1=3;
			switch ( input.LA(1) ) {
			case 'b':
				{
				alt1=1;
				}
				break;
			case 'o':
				{
				alt1=2;
				}
				break;
			case 'e':
				{
				alt1=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}
			switch (alt1) {
				case 1 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:272:4: 'boundary'
					{
					match("boundary"); 

					}
					break;
				case 2 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:272:17: 'overlap'
					{
					match("overlap"); 

					}
					break;
				case 3 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:272:29: 'exact'
					{
					match("exact"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MATCH_MODE"

	// $ANTLR start "PROPERTY"
	public final void mPROPERTY() throws RecognitionException {
		try {
			int _type = PROPERTY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:276:2: ( 'property' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:276:4: 'property'
			{
			match("property"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PROPERTY"

	// $ANTLR start "VALUE"
	public final void mVALUE() throws RecognitionException {
		try {
			int _type = VALUE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:279:7: ( 'value' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:279:9: 'value'
			{
			match("value"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VALUE"

	// $ANTLR start "REG"
	public final void mREG() throws RecognitionException {
		try {
			int _type = REG;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:282:5: ( 'reg' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:282:7: 'reg'
			{
			match("reg"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "REG"

	// $ANTLR start "FREQ"
	public final void mFREQ() throws RecognitionException {
		try {
			int _type = FREQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:285:6: ( 'freq' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:285:8: 'freq'
			{
			match("freq"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FREQ"

	// $ANTLR start "SIMIL"
	public final void mSIMIL() throws RecognitionException {
		try {
			int _type = SIMIL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:288:7: ( 'simil' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:288:9: 'simil'
			{
			match("simil"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SIMIL"

	// $ANTLR start "WILD"
	public final void mWILD() throws RecognitionException {
		try {
			int _type = WILD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:291:6: ( 'wild' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:291:8: 'wild'
			{
			match("wild"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WILD"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:293:9: ( 'comment' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:293:11: 'comment'
			{
			match("comment"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "WHITESPACE"
	public final void mWHITESPACE() throws RecognitionException {
		try {
			int _type = WHITESPACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:297:2: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\\u000C' ) )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:297:4: ( ' ' | '\\t' | '\\r' | '\\n' | '\\u000C' )
			{
			if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			skip();
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WHITESPACE"

	// $ANTLR start "EQUAL"
	public final void mEQUAL() throws RecognitionException {
		try {
			int _type = EQUAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:300:8: ( '=' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:300:10: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EQUAL"

	// $ANTLR start "UNEQUAL"
	public final void mUNEQUAL() throws RecognitionException {
		try {
			int _type = UNEQUAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:9: ( ( '<' | '>' | '<=' | '>=' ) )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:11: ( '<' | '>' | '<=' | '>=' )
			{
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:11: ( '<' | '>' | '<=' | '>=' )
			int alt2=4;
			int LA2_0 = input.LA(1);
			if ( (LA2_0=='<') ) {
				int LA2_1 = input.LA(2);
				if ( (LA2_1=='=') ) {
					alt2=3;
				}

				else {
					alt2=1;
				}

			}
			else if ( (LA2_0=='>') ) {
				int LA2_2 = input.LA(2);
				if ( (LA2_2=='=') ) {
					alt2=4;
				}

				else {
					alt2=2;
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}

			switch (alt2) {
				case 1 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:13: '<'
					{
					match('<'); 
					}
					break;
				case 2 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:19: '>'
					{
					match('>'); 
					}
					break;
				case 3 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:25: '<='
					{
					match("<="); 

					}
					break;
				case 4 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:303:32: '>='
					{
					match(">="); 

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNEQUAL"

	// $ANTLR start "TXT"
	public final void mTXT() throws RecognitionException {
		try {
			int _type = TXT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:306:5: ( '\"' (~ ( '\"' ) | '\\\\\"' )+ '\"' )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:306:7: '\"' (~ ( '\"' ) | '\\\\\"' )+ '\"'
			{
			match('\"'); 
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:306:11: (~ ( '\"' ) | '\\\\\"' )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=3;
				int LA3_0 = input.LA(1);
				if ( (LA3_0=='\\') ) {
					int LA3_2 = input.LA(2);
					if ( (LA3_2=='\"') ) {
						int LA3_4 = input.LA(3);
						if ( ((LA3_4 >= '\u0000' && LA3_4 <= '\uFFFF')) ) {
							alt3=2;
						}
						else {
							alt3=1;
						}

					}
					else if ( ((LA3_2 >= '\u0000' && LA3_2 <= '!')||(LA3_2 >= '#' && LA3_2 <= '\uFFFF')) ) {
						alt3=1;
					}

				}
				else if ( ((LA3_0 >= '\u0000' && LA3_0 <= '!')||(LA3_0 >= '#' && LA3_0 <= '[')||(LA3_0 >= ']' && LA3_0 <= '\uFFFF')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:306:12: ~ ( '\"' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:306:19: '\\\\\"'
					{
					match("\\\""); 

					}
					break;

				default :
					if ( cnt3 >= 1 ) break loop3;
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
			}

			match('\"'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TXT"

	// $ANTLR start "LETTER"
	public final void mLETTER() throws RecognitionException {
		try {
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:310:2: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LETTER"

	// $ANTLR start "LETTEREXTENDED"
	public final void mLETTEREXTENDED() throws RecognitionException {
		try {
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:314:2: ( ( '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u00FF' ) )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:
			{
			if ( (input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u00FF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LETTEREXTENDED"

	// $ANTLR start "GROUPIDENT"
	public final void mGROUPIDENT() throws RecognitionException {
		try {
			int _type = GROUPIDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:2: ( '@' ( LETTER | '_' | LETTEREXTENDED ) ( LETTER | '_' | LETTEREXTENDED | INT )+ )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:4: '@' ( LETTER | '_' | LETTEREXTENDED ) ( LETTER | '_' | LETTEREXTENDED | INT )+
			{
			match('@'); 
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u00FF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:36: ( LETTER | '_' | LETTEREXTENDED | INT )+
			int cnt4=0;
			loop4:
			while (true) {
				int alt4=5;
				switch ( input.LA(1) ) {
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'H':
				case 'I':
				case 'J':
				case 'K':
				case 'L':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
				case 'R':
				case 'S':
				case 'T':
				case 'U':
				case 'V':
				case 'W':
				case 'X':
				case 'Y':
				case 'Z':
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
					{
					alt4=1;
					}
					break;
				case '_':
					{
					alt4=2;
					}
					break;
				case '\u00C0':
				case '\u00C1':
				case '\u00C2':
				case '\u00C3':
				case '\u00C4':
				case '\u00C5':
				case '\u00C6':
				case '\u00C7':
				case '\u00C8':
				case '\u00C9':
				case '\u00CA':
				case '\u00CB':
				case '\u00CC':
				case '\u00CD':
				case '\u00CE':
				case '\u00CF':
				case '\u00D0':
				case '\u00D1':
				case '\u00D2':
				case '\u00D3':
				case '\u00D4':
				case '\u00D5':
				case '\u00D6':
				case '\u00D8':
				case '\u00D9':
				case '\u00DA':
				case '\u00DB':
				case '\u00DC':
				case '\u00DD':
				case '\u00DE':
				case '\u00DF':
				case '\u00E0':
				case '\u00E1':
				case '\u00E2':
				case '\u00E3':
				case '\u00E4':
				case '\u00E5':
				case '\u00E6':
				case '\u00E7':
				case '\u00E8':
				case '\u00E9':
				case '\u00EA':
				case '\u00EB':
				case '\u00EC':
				case '\u00ED':
				case '\u00EE':
				case '\u00EF':
				case '\u00F0':
				case '\u00F1':
				case '\u00F2':
				case '\u00F3':
				case '\u00F4':
				case '\u00F5':
				case '\u00F6':
				case '\u00F8':
				case '\u00F9':
				case '\u00FA':
				case '\u00FB':
				case '\u00FC':
				case '\u00FD':
				case '\u00FE':
				case '\u00FF':
					{
					alt4=3;
					}
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					{
					alt4=4;
					}
					break;
				}
				switch (alt4) {
				case 1 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:37: LETTER
					{
					mLETTER(); 

					}
					break;
				case 2 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:44: '_'
					{
					match('_'); 
					}
					break;
				case 3 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:48: LETTEREXTENDED
					{
					mLETTEREXTENDED(); 

					}
					break;
				case 4 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:318:63: INT
					{
					mINT(); 

					}
					break;

				default :
					if ( cnt4 >= 1 ) break loop4;
					EarlyExitException eee = new EarlyExitException(4, input);
					throw eee;
				}
				cnt4++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GROUPIDENT"

	// $ANTLR start "INT"
	public final void mINT() throws RecognitionException {
		try {
			int _type = INT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:321:5: ( ( '0' .. '9' )+ )
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:321:7: ( '0' .. '9' )+
			{
			// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:321:7: ( '0' .. '9' )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt5 >= 1 ) break loop5;
					EarlyExitException eee = new EarlyExitException(5, input);
					throw eee;
				}
				cnt5++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INT"

	@Override
	public void mTokens() throws RecognitionException {
		// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:8: ( T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | TAG | TAGDIFF | MATCH_MODE | PROPERTY | VALUE | REG | FREQ | SIMIL | WILD | COMMENT | WHITESPACE | EQUAL | UNEQUAL | TXT | GROUPIDENT | INT )
		int alt6=27;
		switch ( input.LA(1) ) {
		case '%':
			{
			alt6=1;
			}
			break;
		case '&':
			{
			alt6=2;
			}
			break;
		case '(':
			{
			alt6=3;
			}
			break;
		case ')':
			{
			alt6=4;
			}
			break;
		case ',':
			{
			alt6=5;
			}
			break;
		case '-':
			{
			alt6=6;
			}
			break;
		case ';':
			{
			alt6=7;
			}
			break;
		case 'C':
			{
			alt6=8;
			}
			break;
		case 'E':
			{
			alt6=9;
			}
			break;
		case 'w':
			{
			int LA6_10 = input.LA(2);
			if ( (LA6_10=='h') ) {
				alt6=10;
			}
			else if ( (LA6_10=='i') ) {
				alt6=20;
			}

			else {
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 6, 10, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}

			}
			break;
		case '|':
			{
			alt6=11;
			}
			break;
		case 't':
			{
			int LA6_12 = input.LA(2);
			if ( (LA6_12=='a') ) {
				int LA6_28 = input.LA(3);
				if ( (LA6_28=='g') ) {
					int LA6_29 = input.LA(4);
					if ( (LA6_29=='d') ) {
						alt6=13;
					}

					else {
						alt6=12;
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 6, 28, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 6, 12, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}

			}
			break;
		case 'b':
		case 'e':
		case 'o':
			{
			alt6=14;
			}
			break;
		case 'p':
			{
			alt6=15;
			}
			break;
		case 'v':
			{
			alt6=16;
			}
			break;
		case 'r':
			{
			alt6=17;
			}
			break;
		case 'f':
			{
			alt6=18;
			}
			break;
		case 's':
			{
			alt6=19;
			}
			break;
		case 'c':
			{
			alt6=21;
			}
			break;
		case '\t':
		case '\n':
		case '\f':
		case '\r':
		case ' ':
			{
			alt6=22;
			}
			break;
		case '=':
			{
			alt6=23;
			}
			break;
		case '<':
		case '>':
			{
			alt6=24;
			}
			break;
		case '\"':
			{
			alt6=25;
			}
			break;
		case '@':
			{
			alt6=26;
			}
			break;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			{
			alt6=27;
			}
			break;
		default:
			NoViableAltException nvae =
				new NoViableAltException("", 6, 0, input);
			throw nvae;
		}
		switch (alt6) {
			case 1 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:10: T__40
				{
				mT__40(); 

				}
				break;
			case 2 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:16: T__41
				{
				mT__41(); 

				}
				break;
			case 3 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:22: T__42
				{
				mT__42(); 

				}
				break;
			case 4 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:28: T__43
				{
				mT__43(); 

				}
				break;
			case 5 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:34: T__44
				{
				mT__44(); 

				}
				break;
			case 6 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:40: T__45
				{
				mT__45(); 

				}
				break;
			case 7 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:46: T__46
				{
				mT__46(); 

				}
				break;
			case 8 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:52: T__47
				{
				mT__47(); 

				}
				break;
			case 9 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:58: T__48
				{
				mT__48(); 

				}
				break;
			case 10 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:64: T__49
				{
				mT__49(); 

				}
				break;
			case 11 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:70: T__50
				{
				mT__50(); 

				}
				break;
			case 12 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:76: TAG
				{
				mTAG(); 

				}
				break;
			case 13 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:80: TAGDIFF
				{
				mTAGDIFF(); 

				}
				break;
			case 14 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:88: MATCH_MODE
				{
				mMATCH_MODE(); 

				}
				break;
			case 15 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:99: PROPERTY
				{
				mPROPERTY(); 

				}
				break;
			case 16 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:108: VALUE
				{
				mVALUE(); 

				}
				break;
			case 17 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:114: REG
				{
				mREG(); 

				}
				break;
			case 18 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:118: FREQ
				{
				mFREQ(); 

				}
				break;
			case 19 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:123: SIMIL
				{
				mSIMIL(); 

				}
				break;
			case 20 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:129: WILD
				{
				mWILD(); 

				}
				break;
			case 21 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:134: COMMENT
				{
				mCOMMENT(); 

				}
				break;
			case 22 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:142: WHITESPACE
				{
				mWHITESPACE(); 

				}
				break;
			case 23 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:153: EQUAL
				{
				mEQUAL(); 

				}
				break;
			case 24 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:159: UNEQUAL
				{
				mUNEQUAL(); 

				}
				break;
			case 25 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:167: TXT
				{
				mTXT(); 

				}
				break;
			case 26 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:171: GROUPIDENT
				{
				mGROUPIDENT(); 

				}
				break;
			case 27 :
				// C:\\data\\workspace201903\\catma\\grammars\\ast\\CatmaQuery.g:1:182: INT
				{
				mINT(); 

				}
				break;

		}
	}



}
