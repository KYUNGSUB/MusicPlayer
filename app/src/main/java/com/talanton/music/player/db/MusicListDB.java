package com.talanton.music.player.db;

import android.provider.BaseColumns;

public class MusicListDB {
	private MusicListDB(){}
	
	public static final class MainCategoryInfo implements BaseColumns {
		private MainCategoryInfo(){}
		   
		public static final String TABLE_NAME = "tbl_main_category_info";
		public static final String C_ID = "_id";
		public static final String KO_NAME = "col_kname";
		public static final String EN_NAME = "col_ename";
		public static final String COUNT = "col_count";
	}
	
	public static final class SubCategoryInfo implements BaseColumns {
		private SubCategoryInfo(){}
			   
		public static final String TABLE_NAME = "tbl_sub_category_info";
		public static final String M_ID = "col_main_id";
		public static final String S_ID = "col_sub_id";
		public static final String KO_NAME = "col_kname";
		public static final String EN_NAME = "col_ename";
		public static final String COUNT = "col_count";
	}
	
	public static final class MusicContentsInfo implements BaseColumns {
		private MusicContentsInfo(){}
		   
		public static final String TABLE_NAME = "tbl_music_contents_info";
		public static final String L_ID = "_id";
		public static final String P_ID = "col_pid";
	}
	
	public static final class MusicServerInfo implements BaseColumns {
		private MusicServerInfo(){}
	   
		public static final String TABLE_NAME = "tbl_music_server_info";
		public static final String _ID = "_id";
		public static final String PRODUCT_ID = "col_pid";
		public static final String TITLE_KO = "title_ko";
		public static final String AUTHOR_KO = "author_ko";
		public static final String TITLE_EN = "title_en";
		public static final String AUTHOR_EN = "author_en";
		public static final String MUSIC_GENRE = "col_genre";
		public static final String PLAY_TIME = "col_play_time";
		public static final String FILE_SIZE = "col_file_size";
		public static final String FILE_INFO = "col_file_info";
		public static final String UUID_INFO = "col_url_info";
		public static final String MY_RATE = "col_my_rate";
		public static final String DOWNLOAD = "col_download";
		public static final String BOOKMARK = "col_bookmark";
		public static final String SORT_ORDER = "col_pid ASC";
	}
	
	public static final class MusicClientInfo implements BaseColumns {
		private MusicClientInfo(){}

		public static final String TABLE_NAME = "tbl_music_client_info";
		public static final String CLIENT_ID = "_id";
		public static final String PRODUCT_ID = "product_id";
		public static final String FILE_INFO = "col_file_info";
		public static final String PLAY_ORDER = "col_play_order";
		public static final String SORT_ORDER = "col_play_order ASC";
	}
	
	public static final class PushMessageHisory implements BaseColumns {
		private PushMessageHisory(){}

		public static final String TABLE_NAME = "tbl_push_message_history";
		public static final String PUSH_MESSAGE_ID = "_id";
		public static final String PUSH_WHEN = "col_when";
		public static final String PUSH_MESSAGE = "col_message";
	}
	
	public static final class MyLikePostInfo implements BaseColumns {
		private MyLikePostInfo(){}

		public static final String TABLE_NAME = "tbl_my_like_post_info";
		public static final String UUID_INFO = "col_url_info";
		public static final String SORT_ORDER = "col_url_info DESC";
	}
	
	public static final class CouponPurchaseInfo implements BaseColumns {
   		private CouponPurchaseInfo(){}

   		public static final String TABLE_NAME = "tbl_coupon_purchase_info";
   		public static final String PURCHASED_PRODUCT_ID_COL = "_id";
   		public static final String PURCHASED_SKU_COL = "coupon_kind";
   		public static final String PURCHASED_POINT_COL = "point";
   		public static final String PURCHASE_TIME_COL = "purchaseTime";
   		public static final String SORT_ORDER = "_id DESC";
   	}
	
	public static final class PurchaseHistoryInfo implements BaseColumns {
		private PurchaseHistoryInfo(){}

		public static final String TABLE_NAME = "tbl_purchase_history_info";
		public static final String HISTORY_ORDER_ID_COL = "_id";
		public static final String HISTORY_PRODUCT_ID_COL = "productId";
		public static final String HISTORY_FILENAME_COL = "fileName";
		public static final String HISTORY_POINT_COL = "point";
		public static final String HISTORY_PURCHASE_TIME_COL = "purchaseTime";
		public static final String SORT_ORDER = "purchaseTime DESC";
	}
}