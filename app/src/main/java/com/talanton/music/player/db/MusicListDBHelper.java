package com.talanton.music.player.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.talanton.music.player.db.MusicListDB;

public class MusicListDBHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "MusicListDB.db";
	private static final int    DB_VERSION = 9;

	public MusicListDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase peMusicGroupDB) {
		StringBuilder mainCTBLSQL = new StringBuilder();
	       mainCTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.MainCategoryInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.MainCategoryInfo.C_ID)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.MainCategoryInfo.KO_NAME)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MainCategoryInfo.EN_NAME)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MainCategoryInfo.COUNT)
	                   .append(" INTEGER  NOT NULL")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(mainCTBLSQL.toString());
	       
	       StringBuilder subCTBLSQL = new StringBuilder();
	       subCTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.SubCategoryInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.SubCategoryInfo._ID)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.SubCategoryInfo.M_ID)
	                   .append(" INTEGER NOT NULL, ")
	                   .append(MusicListDB.SubCategoryInfo.S_ID)
	                   .append(" INTEGER NOT NULL, ")
	                   .append(MusicListDB.SubCategoryInfo.KO_NAME)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.SubCategoryInfo.EN_NAME)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.SubCategoryInfo.COUNT)
	                   .append(" INTEGER NOT NULL")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(subCTBLSQL.toString());
	       
	       StringBuilder contentTBLSQL = new StringBuilder();
	       contentTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.MusicContentsInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.MusicContentsInfo.L_ID)
	                   .append(" INTEGER PRIMARY KEY NOT NULL, ")
	                   .append(MusicListDB.MusicContentsInfo.P_ID)
	                   .append(" TEXT NOT NULL")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(contentTBLSQL.toString());
	       
	       StringBuilder serverTBLSQL = new StringBuilder();
	       serverTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.MusicServerInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.MusicServerInfo._ID)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.MusicServerInfo.PRODUCT_ID)
	                   .append(" INTEGER NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.TITLE_KO)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.AUTHOR_KO)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.TITLE_EN)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.AUTHOR_EN)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.MUSIC_GENRE)
	                   .append(" INTEGER NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.PLAY_TIME)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.FILE_SIZE)
	                   .append(" INTEGER NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.FILE_INFO)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.UUID_INFO)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicServerInfo.MY_RATE)
	                   .append(" INTEGER default 0, ")
	                   .append(MusicListDB.MusicServerInfo.DOWNLOAD)
	                   .append(" INTEGER default 0, ")
	                   .append(MusicListDB.MusicServerInfo.BOOKMARK)
	                   .append(" INTEGER default 0")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(serverTBLSQL.toString());
	       
	       StringBuilder clientTBLSQL = new StringBuilder();
	       clientTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.MusicClientInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.MusicClientInfo.CLIENT_ID)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.MusicClientInfo.PRODUCT_ID)
	                   .append(" INTEGER NOT NULL , ")
	                   .append(MusicListDB.MusicClientInfo.FILE_INFO)
	                   .append(" TEXT NOT NULL, ")
	                   .append(MusicListDB.MusicClientInfo.PLAY_ORDER)
	                   .append(" INTEGER ")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(clientTBLSQL.toString());
	       
	       StringBuilder pushmessageTBLSQL = new StringBuilder();
	       pushmessageTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.PushMessageHisory.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.PushMessageHisory.PUSH_MESSAGE_ID)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.PushMessageHisory.PUSH_WHEN)
	                   .append(" LONG NOT NULL, ")
	                   .append(MusicListDB.PushMessageHisory.PUSH_MESSAGE)
	                   .append(" TEXT NOT NULL")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(pushmessageTBLSQL.toString());
	       
	       StringBuilder likepostTBLSQL = new StringBuilder();
	       likepostTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.MyLikePostInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.MyLikePostInfo.UUID_INFO)
	                   .append(" TEXT PRIMARY KEY NOT NULL ")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(likepostTBLSQL.toString());
	       
	       StringBuilder couponTBLSQL = new StringBuilder();
	       couponTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.CouponPurchaseInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.CouponPurchaseInfo.PURCHASED_PRODUCT_ID_COL)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.CouponPurchaseInfo.PURCHASED_SKU_COL)
	                   .append(" TEXT , ")
	                   .append(MusicListDB.CouponPurchaseInfo.PURCHASED_POINT_COL)
	                   .append(" INTEGER , ")
	                   .append(MusicListDB.CouponPurchaseInfo.PURCHASE_TIME_COL)
	                   .append(" LONG ")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(couponTBLSQL.toString());
	       
	       StringBuilder historyTBLSQL = new StringBuilder();
	       historyTBLSQL
	                   .append("CREATE TABLE ")
	                   .append(MusicListDB.PurchaseHistoryInfo.TABLE_NAME)
	                   .append(" ( ")
	                   .append(MusicListDB.PurchaseHistoryInfo.HISTORY_ORDER_ID_COL)
	                   .append(" INTEGER PRIMARY KEY AUTOINCREMENT , ")
	                   .append(MusicListDB.PurchaseHistoryInfo.HISTORY_PRODUCT_ID_COL)
	                   .append(" INTEGER , ")
	                   .append(MusicListDB.PurchaseHistoryInfo.HISTORY_FILENAME_COL)
	                   .append(" TEXT , ")
	                   .append(MusicListDB.PurchaseHistoryInfo.HISTORY_POINT_COL)
	                   .append(" INTEGER , ")
	                   .append(MusicListDB.PurchaseHistoryInfo.HISTORY_PURCHASE_TIME_COL)
	                   .append(" LONG ")
	                   .append(" ); ");
	       peMusicGroupDB.execSQL(historyTBLSQL.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase peMusicGroupDB, int oldVersion, int newVersion) {
		if (newVersion != DB_VERSION) {
    		peMusicGroupDB.execSQL("DROP TABLE IF EXISTS " + MusicListDB.MainCategoryInfo.TABLE_NAME);
    		peMusicGroupDB.execSQL("DROP TABLE IF EXISTS " + MusicListDB.SubCategoryInfo.TABLE_NAME);
    		peMusicGroupDB.execSQL("DROP TABLE IF EXISTS " + MusicListDB.MusicContentsInfo.TABLE_NAME);
    		peMusicGroupDB.execSQL("DROP TABLE IF EXISTS " + MusicListDB.MusicServerInfo.TABLE_NAME);
    		peMusicGroupDB.execSQL("DROP TABLE IF EXISTS " + MusicListDB.MusicClientInfo.TABLE_NAME);
    		onCreate(peMusicGroupDB);
            return;
        }
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}