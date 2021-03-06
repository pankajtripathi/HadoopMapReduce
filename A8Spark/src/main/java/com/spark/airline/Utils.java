package com.spark.airline;

import org.apache.commons.lang3.StringUtils;

/**
 * Reusing code 
 * @author Akash Singh, Surekha Jadhwani
 * 
 * This is a utility class for processing Flight details and perform sanity checks
 *
 */
public class Utils {

	/** determines whether the record provided passes the sanity checks
	 * @param record
	 * @return true for sane record
	 */
	public static boolean isRecordSane(String[] record)
	{		
		return (record != null && 
				areStringFieldsOfRecordValid(record) &&
				areRequiredIdsGreaterThanZero(record) &&
				isTimeZoneValid(record));
	}

	/** Determine whether the string fields are correct
	 * @param record
	 * @return true for records having the string fields as per specifications given
	 */
	private static boolean areStringFieldsOfRecordValid(String[] record)
	{
		String origin = record[RecordIndex._14_ORIGIN];
		String dest = record[RecordIndex._23_DEST];
		String origin_city = record[RecordIndex._15_ORIGIN_CITY];
		String dest_city = record[RecordIndex._24_DEST_CITY];
		String origin_state = record[RecordIndex._16_ORIGIN_STATE];
		String dest_state = record[RecordIndex._25_DEST_STATE];
		String origin_state_name = record[RecordIndex._18_ORIGIN_STATE_NM];
		String dest_state_name = record[RecordIndex._27_DEST_STATE_NAME];
		return (StringUtils.isNotBlank(origin) &&
				StringUtils.isNotBlank(dest) &&
				StringUtils.isNotBlank(origin_city) &&
				StringUtils.isNotBlank(dest_city) &&
				StringUtils.isNotBlank(origin_state) &&
				StringUtils.isNotBlank(dest_state) &&
				StringUtils.isNotBlank(origin_state_name) &&
				StringUtils.isNotBlank(dest_state_name));
	}

	/** Determines whether the required fields are greater than zero
	 * @param record
	 * @return true for records having the required fields with value higher than 0
	 */
	private static boolean areRequiredIdsGreaterThanZero(String[] record)
	{
		try
		{
			int origin_airport_id = Integer.parseInt(record[RecordIndex._11_ORIGIN_AIRPORT_ID]);
			int dest_airport_id = Integer.parseInt(record[RecordIndex._20_DEST_AIRPORT_ID]);
			int origin_seq_airport_id = Integer.parseInt(record[RecordIndex._12_ORIGIN_SEQ_AIRPORT_ID]);
			int dest_seq_airport_id = Integer.parseInt(record[RecordIndex._21_DEST_SEQ_AIRPORT_ID]);
			int origin_city_market_id = Integer.parseInt(record[RecordIndex._13_ORIGIN_CITY_MARKET_ID]);
			int dest_city_market_id = Integer.parseInt(record[RecordIndex._22_DEST_CITY_MARKET_ID]);
			int origin_state_fips = Integer.parseInt(record[RecordIndex._17_ORIGIN_STATE_FIPS]);
			int dest_state_fips = Integer.parseInt(record[RecordIndex._26_DEST_STATE_FIPS]);
			int origin_wac = Integer.parseInt(record[RecordIndex._19_ORIGIN_WAC]);
			int dest_wac = Integer.parseInt(record[RecordIndex._28_DEST_WAC]);
			return (origin_airport_id > 0 &&
					dest_airport_id > 0 &&
					origin_seq_airport_id > 0 &&
					dest_seq_airport_id > 0 &&
					origin_city_market_id > 0 &&
					dest_city_market_id > 0 &&
					origin_state_fips > 0 &&
					dest_state_fips > 0 &&
					origin_wac > 0 &&
					dest_wac > 0);
		}
		catch (NumberFormatException num_format_excp){return false;}
	}

	
	/** Evaluates the timezone related checks to determine correctness of the record
	 * @param record
	 * @return True for correct records as per timezone calculations
	 */
	private static boolean isTimeZoneValid(String[] record)
	{	
		try
		{	
			int CRS_arr_time = Integer.parseInt(record[RecordIndex._40_CRS_ARR_TIME]);
			int CRS_dep_time = Integer.parseInt(record[RecordIndex._29_CRS_DEP_TIME]);
			int CRS_elapsed_time = Integer.parseInt(record[RecordIndex._50_CRS_ELAPSED_TIME]);
			CRS_arr_time = ((CRS_arr_time / 100) * 60) + CRS_arr_time % 100;
			CRS_dep_time = ((CRS_dep_time / 100) * 60) + CRS_dep_time % 100;
			int time_zone = CRS_arr_time - CRS_dep_time - CRS_elapsed_time;
			if (time_zone % 60 != 0 && !isValidBooleanString(record[RecordIndex._47_CANCELLED])) return false;

			if (!stringToBoolean(record[RecordIndex._47_CANCELLED])) {
				int arr_time = Integer.parseInt(record[RecordIndex._41_ARR_TIME]);
				int dep_time = Integer.parseInt(record[RecordIndex._30_DEP_TIME]);
				int actual_elapsed_time = Integer.parseInt(record[RecordIndex._51_ACT_ELAPSED_TIME]);
				double arr_delay = Double.parseDouble(record[RecordIndex._42_ARR_DELAY]);
				double arr_del_minutes = Double.parseDouble(record[RecordIndex._43_ARR_DEL_MINS]);
				boolean arr_del_15 = stringToBoolean(record[RecordIndex._44_ARR_DEL_15]);
				arr_time = ((arr_time / 100) * 60) + arr_time % 100;
				dep_time = ((dep_time / 100) * 60) + dep_time % 100;
				// Mod 1440 for 24 hours rotation - eg. flight was scheduled to leave at 0700 and 
				// arrive at 0900, but left at 2300 and arrived at 0100.
				int time_diff = ((arr_time - dep_time - actual_elapsed_time) - time_zone) % 1440;
				boolean arr_del_15_correct = (arr_del_minutes >= 15) ? arr_del_15 : true;
				// time diff should be zero, timezone and arr delay related fields should
				// be correct.
				return (time_diff == 0 &&
						arr_del_15_correct &&
						((arr_delay > 0 && arr_delay == arr_del_minutes) || 
								(arr_delay <= 0 && arr_del_minutes == 0)));
			}
			return true;
		}
		catch (NumberFormatException num_format_excp) {return false;}
	}

	/** converts given string to boolean value
	 * @param s
	 * @return boolean value indicating value of the string
	 */
	static boolean stringToBoolean(String s)
	{
		try
		{
			return (1 == (int) Double.parseDouble(s));
		}
		catch (NumberFormatException num_format_excp)
		{
			return ("true".equals(s));
		}
	}
	
	/** Evaluates whether given string could be successfully converted to boolean or not
	 * @param s
	 * @return true for correct boolean values
	 */
	public static boolean isValidBooleanString(String s)
	{	
		try 
		{
			return (1 == (int) Double.parseDouble(s) || (1 == (int) Double.parseDouble(s)));
		}
		catch (NumberFormatException e)
		{
			return ("true".equals(s) || "false".equals(s));
		}
	}
	
	// ------------------------------------------------------------------------
	// CONSTANTS
	// ------------------------------------------------------------------------
	
	public static final String SEPARATOR = ",";
	public static int NUM_FEATURES = 13;
	
	/**
	 * 
	 * @author Surekha Jadhwani, Akash Singh
	 * 
	 * Constants for accessing required data columns 
	 *
	 */
	public static class RecordIndex
	{
		public static final int _00_YEAR						= 0;
		public static final int _02_MONTH						= 2;
		public static final int _03_DAY_OF_MONTH				= 3;
		public static final int _04_DAY_OF_WEEK					= 4;
		public static final int _05_FLIGHT_DATE					= 5;
		public static final int _08_CARRIER_ID 					= 8;
		public static final int _10_FLIGHT_NUM					= 10;
		public static final int _11_ORIGIN_AIRPORT_ID 			= 11;
		public static final int _12_ORIGIN_SEQ_AIRPORT_ID 		= 12;
		public static final int _13_ORIGIN_CITY_MARKET_ID		= 13;
		public static final int _14_ORIGIN 						= 14;
		public static final int _15_ORIGIN_CITY 				= 15;
		public static final int _16_ORIGIN_STATE 				= 16;
		public static final int _17_ORIGIN_STATE_FIPS			= 17;
		public static final int _18_ORIGIN_STATE_NM 			= 18;
		public static final int _19_ORIGIN_WAC 					= 19;
		public static final int _20_DEST_AIRPORT_ID				= 20;
		public static final int _21_DEST_SEQ_AIRPORT_ID			= 21;
		public static final int _22_DEST_CITY_MARKET_ID			= 22;
		public static final int _23_DEST						= 23;
		public static final int _24_DEST_CITY					= 24;
		public static final int _25_DEST_STATE					= 25;
		public static final int _26_DEST_STATE_FIPS				= 26;
		public static final int _28_DEST_WAC					= 28;
		public static final int _27_DEST_STATE_NAME				= 27;
		public static final int _29_CRS_DEP_TIME 				= 29;
		public static final int _30_DEP_TIME 					= 30;
		public static final int _40_CRS_ARR_TIME 				= 40;
		public static final int _41_ARR_TIME 					= 41;
		public static final int _42_ARR_DELAY 					= 42;
		public static final int _43_ARR_DEL_MINS 				= 43;
		public static final int _44_ARR_DEL_15 					= 44;
		public static final int _47_CANCELLED 					= 47;
		public static final int _50_CRS_ELAPSED_TIME 			= 50;
		public static final int _51_ACT_ELAPSED_TIME 			= 51;
		public static final int _54_DISTANCE					= 54;
		public static final int _109_AVG_TICKET_PRICE			= 109;
	}

	/**
	 * 
	 * @author Akash Singh
	 * Constants for input command line argument prefixes 
	 * 
	 */
	public static class Arguments
	{
		public static final String INPUT_DIR_PREFIX 		= "-input=";
		public static final String OUTPUT_DIR_PREFIX 		= "-output=";
	}
}
