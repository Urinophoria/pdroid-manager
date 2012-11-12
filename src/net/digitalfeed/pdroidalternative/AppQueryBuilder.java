package net.digitalfeed.pdroidalternative;

import java.util.LinkedList;

import net.digitalfeed.pdroidalternative.DBInterface.ApplicationStatusTable;
import net.digitalfeed.pdroidalternative.DBInterface.ApplicationTable;
import net.digitalfeed.pdroidalternative.DBInterface.PermissionApplicationTable;
import net.digitalfeed.pdroidalternative.DBInterface.PermissionSettingTable;
import net.digitalfeed.pdroidalternative.DBInterface.SettingTable;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class AppQueryBuilder {
	public static final int COLUMN_TYPE_APP = 1;
	public static final int COLUMN_TYPE_STATUSFLAGS = 2;
	public static final int COLUMN_TYPE_PACKAGENAME = 4;
	public static final int COLUMN_TYPE_LABEL = 8;
	
	public static final int FILTER_BY_PERMISSION = 1;
	public static final int FILTER_BY_SETTING_GROUP_ID = 2;
	public static final int FILTER_BY_SETTING_GROUP_TITLE = 4;
	public static final int FILTER_BY_TYPE = 8;
	public static final int FILTER_BY_LABEL = 16;
	public static final int FILTER_BY_PACKAGENAME = 32;
	
	private static final int FILTER_TEXT_PERMISSION = 0;
	private static final int FILTER_TEXT_SETTING_GROUP_ID = 1;
	private static final int FILTER_TEXT_SETTING_GROUP_TITLE = 2;
	private static final int FILTER_TEXT_TYPE = 3;
	private static final int FILTER_TEXT_PACKAGENAME= 4;
	private static final int FILTER_TEXT_LABEL = 5;
	private static final int FILTER_TEXT_COUNT = 6;
	
	private int columnTypes;
	private int filterTypes;
	private String [] filterValues;
	private String queryText;
	private String [] projectionIn;
	
	public AppQueryBuilder() {
		filterValues = new String[FILTER_TEXT_COUNT];
	}
	
	public void addColumns(int columnTypeToAdd) {
		this.queryText = null;
		switch (columnTypeToAdd) {
		case COLUMN_TYPE_APP:
			columnTypes |= COLUMN_TYPE_APP;
			columnTypes &= ~COLUMN_TYPE_PACKAGENAME;
			break;
		case COLUMN_TYPE_LABEL:
			columnTypes |= COLUMN_TYPE_LABEL;
			break;
		case COLUMN_TYPE_PACKAGENAME:
			if (0 == (columnTypes & COLUMN_TYPE_APP)) {
				columnTypes |= COLUMN_TYPE_PACKAGENAME;
			}
			break;
		case COLUMN_TYPE_STATUSFLAGS:
			columnTypes |= COLUMN_TYPE_STATUSFLAGS;
			break;
		}
	}
	
	public void addFilter(int filterTypeToAdd, String filterText) {
		this.queryText = null;
		switch (filterTypeToAdd) {
		case FILTER_BY_PERMISSION:
			this.filterTypes |= FILTER_BY_PERMISSION;
			this.filterValues[FILTER_TEXT_PERMISSION] = filterText;
			break;
		case FILTER_BY_SETTING_GROUP_ID:
			this.filterTypes |= FILTER_BY_SETTING_GROUP_ID;
			this.filterValues[FILTER_TEXT_SETTING_GROUP_ID] = filterText;
			break;
		case FILTER_BY_SETTING_GROUP_TITLE:
			this.filterTypes |= FILTER_BY_SETTING_GROUP_TITLE;
			this.filterValues[FILTER_TEXT_SETTING_GROUP_TITLE] = filterText;
			break;
		case FILTER_BY_TYPE:
			this.filterTypes |= FILTER_BY_TYPE;
			this.filterValues[FILTER_TEXT_TYPE] = filterText;
			break;
		case FILTER_BY_LABEL:
			this.filterTypes |= FILTER_BY_LABEL;
			this.filterValues[FILTER_TEXT_LABEL] = filterText;
			break;
		case FILTER_BY_PACKAGENAME:
			this.filterTypes |= FILTER_BY_PACKAGENAME;
			this.filterValues[FILTER_TEXT_PACKAGENAME] = filterText;
			break;
		}
	}
	
	public Cursor doQuery(SQLiteDatabase db) {
		LinkedList<String> projectionIn = new LinkedList<String>();
		
		LinkedList<String> selectPieces = new LinkedList<String>();
		LinkedList<String> fromPieces = new LinkedList<String>();
		LinkedList<String> wherePieces = new LinkedList<String>();
		//LinkedList<String> orderPieces = null;
		
		if (0 != (this.columnTypes & COLUMN_TYPE_APP)) {
			selectPieces.add(SELECTPART_COLUMNS_APP);
		} else {
			if (0 != (this.columnTypes & COLUMN_TYPE_PACKAGENAME)) {
				selectPieces.add(SELECTPART_COLUMNS_PACKAGENAME);
			}
			if (0 != (this.columnTypes & COLUMN_TYPE_LABEL)) {
				selectPieces.add(SELECTPART_COLUMNS_LABEL);
			}
		}
		if (0 != (this.columnTypes & COLUMN_TYPE_STATUSFLAGS)) {
			selectPieces.add(SELECTPART_COLUMNS_STATUSFLAGS);
			fromPieces.add(FROMPART_JOIN_APP_WITH_STATUS);
		}
		

		if (0 != (this.filterTypes & (FILTER_BY_SETTING_GROUP_ID | FILTER_BY_SETTING_GROUP_TITLE))) {
			fromPieces.add(FROMPART_FILTER_BY_SETTING_DETAILS);
		} else if (0 != (this.filterTypes & FILTER_BY_PERMISSION)) {
			//The tables required for filtering by permissions are included by filtering by setting group or setting group name already
			fromPieces.add(FROMPART_FILTER_BY_PERMISSION);
		}

		if (0 != (this.filterTypes & FILTER_BY_PERMISSION)) {
			projectionIn.add(this.filterValues[FILTER_TEXT_PERMISSION]);
			wherePieces.add(WHEREPART_FILTER_BY_PERMISSION);
		}
		if (0 != (this.filterTypes & FILTER_BY_SETTING_GROUP_ID)) {
			projectionIn.add(this.filterValues[FILTER_TEXT_SETTING_GROUP_ID]);
			wherePieces.add(WHEREPART_FILTER_BY_SETTING_GROUP_TITLE);
		}
		if (0 != (this.filterTypes & FILTER_BY_SETTING_GROUP_TITLE)) {
			projectionIn.add(this.filterValues[FILTER_TEXT_SETTING_GROUP_TITLE]);
			wherePieces.add(WHEREPART_FILTER_BY_SETTING_GROUP_TITLE);
		}
		if (0 != (this.filterTypes & FILTER_BY_TYPE)) {
			projectionIn.add(this.filterValues[FILTER_TEXT_TYPE]);
			wherePieces.add(WHEREPART_FILTER_BY_TYPE);
		}
		if (0 != (this.filterTypes & FILTER_BY_LABEL)) {
			projectionIn.add(this.filterValues[FILTER_TEXT_LABEL]);
			wherePieces.add(WHEREPART_FILTER_BY_LABEL);
		}
		if (0 != (this.filterTypes & FILTER_BY_PACKAGENAME)) {
			projectionIn.add(this.filterValues[FILTER_TEXT_PACKAGENAME]);
			wherePieces.add(WHEREPART_FILTER_BY_PACKAGENAME);
		}

		StringBuilder queryStringBuilder = new StringBuilder("SELECT DISTINCT ")
			.append(TextUtils.join(", ", selectPieces))
			.append(FROMPART_APP_TABLE)
			.append(TextUtils.join("",fromPieces));
		if (wherePieces.size() > 0) {
			queryStringBuilder.append(" WHERE ").append(TextUtils.join(" AND ", wherePieces));
		}
		queryStringBuilder.append(" ORDER BY ").append(ORDERPART_SORT_BY_LABEL);

		this.queryText = queryStringBuilder.toString();
		
		this.projectionIn = projectionIn.toArray(new String[projectionIn.size()]);
		Log.d("PDroidAlternative", "Query text is: " + this.queryText);
		return db.rawQuery(this.queryText, this.projectionIn);
	}
	
	protected static final String SELECTPART_COLUMNS_PACKAGENAME = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PACKAGENAME;
	
	protected static final String SELECTPART_COLUMNS_LABEL = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_LABEL;
	
	protected static final String SELECTPART_COLUMNS_APP = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_LABEL + ", " +  
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PACKAGENAME + ", " + 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_UID + ", " +
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_VERSIONCODE + ", " +
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_ICON + ", " +
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_FLAGS + ", " +
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PERMISSIONS;

	//Required FROMPART_JOIN_APP_WITH_STATUS
	protected static final String SELECTPART_COLUMNS_STATUSFLAGS = 
			ApplicationStatusTable.TABLE_NAME + "." + ApplicationStatusTable.COLUMN_NAME_FLAGS;

	
	protected static final String FROMPART_APP_TABLE = 
			"  FROM " + ApplicationTable.TABLE_NAME;
	
	protected static final String FROMPART_JOIN_APP_WITH_STATUS = 
			" LEFT OUTER JOIN " + ApplicationStatusTable.TABLE_NAME +
			" ON " + ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PACKAGENAME + " =  " +
			ApplicationStatusTable.TABLE_NAME + "." + ApplicationStatusTable.COLUMN_NAME_PACKAGENAME;

	
	protected static final String FROMPART_FILTER_BY_PERMISSION = 
			" INNER JOIN " + PermissionApplicationTable.TABLE_NAME +
			" ON " + ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PACKAGENAME + " =  " +
			PermissionApplicationTable.TABLE_NAME + "." + PermissionApplicationTable.COLUMN_NAME_PACKAGENAME;
	
	
	protected static final String FROMPART_FILTER_BY_SETTING_DETAILS = " INNER JOIN " + PermissionApplicationTable.TABLE_NAME +
			" ON " + ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PACKAGENAME + " = " +
			PermissionApplicationTable.TABLE_NAME + "." + PermissionApplicationTable.COLUMN_NAME_PACKAGENAME +
			" INNER JOIN " + PermissionSettingTable.TABLE_NAME + 
			" ON " + PermissionSettingTable.TABLE_NAME + "." + PermissionSettingTable.COLUMN_NAME_PERMISSION + " = " +
			PermissionApplicationTable.TABLE_NAME + "." + PermissionApplicationTable.COLUMN_NAME_PERMISSION +
			" INNER JOIN " + SettingTable.TABLE_NAME + 
			" ON " + SettingTable.TABLE_NAME + "." + SettingTable.COLUMN_NAME_ID + " = " +
			PermissionSettingTable.TABLE_NAME + "." + PermissionSettingTable.COLUMN_NAME_SETTING;
	

	protected static final String WHEREPART_FILTER_BY_LABEL = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_LABEL + " LIKE ?";

	protected static final String WHEREPART_FILTER_BY_PACKAGENAME = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_PACKAGENAME + " LIKE ?";

	
	//this requires 'FROMPART_FILTER_BY_PERMISSION'
	protected static final String WHEREPART_FILTER_BY_PERMISSION = 
			PermissionApplicationTable.TABLE_NAME + "." + PermissionApplicationTable.COLUMN_NAME_PERMISSION + " = ?";

	//this requires the 'FROMPART_FILTER_BY_SETTING_DETAILS' to be included
	protected static final String WHEREPART_FILTER_BY_SETTING_GROUP_TITLE = 
			SettingTable.TABLE_NAME + "." + SettingTable.COLUMN_NAME_GROUP_TITLE + " = ?";

	//this requires the 'FROMPART_FILTER_BY_SETTING_DETAILS' to be included
	protected static final String WHEREPART_FILTER_BY_SETTING_GROUP_ID = 
			SettingTable.TABLE_NAME + "." + SettingTable.COLUMN_NAME_GROUP_ID + " = ?";

	
	protected static final String WHEREPART_FILTER_BY_TYPE = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_FLAGS +
			" & " + ApplicationTable.FLAG_IS_SYSTEM_APP + " = CAST(? AS INTEGER)";
	
	
	protected static final String ORDERPART_SORT_BY_LABEL = 
			ApplicationTable.TABLE_NAME + "." + ApplicationTable.COLUMN_NAME_LABEL;
}