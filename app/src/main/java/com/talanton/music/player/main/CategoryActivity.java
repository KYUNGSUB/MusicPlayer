package com.talanton.music.player.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.talanton.music.player.R;
import com.talanton.music.player.db.MusicListDB;
import com.talanton.music.player.db.MusicListDB.MusicServerInfo;
import com.talanton.music.player.db.MusicListDB.MainCategoryInfo;
import com.talanton.music.player.db.MusicListDB.SubCategoryInfo;
import com.talanton.music.player.db.MusicListDB.MusicContentsInfo;
import com.talanton.music.player.db.MusicListDBHelper;
import com.talanton.music.player.sub.BookmarkInfo;
import com.talanton.music.player.sub.MusicCategory;
import com.talanton.music.player.sub.PlayerManager;
import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.utils.MusicUtils;

import java.util.ArrayList;
import java.util.Locale;

public class CategoryActivity extends AppCompatActivity {
    private String language;
    private FrameLayout mSearchLayout;
    private LinearLayout mLogicalLayout;
    private LinearLayout mComposerLayout;
    private LinearLayout mKeywordLayout;
    private Spinner searchSpin;
    private Spinner main_spin;
    private Spinner sub_spin;
    private Spinner ccomposerSpin;
    private Spinner cgenreSpin;
    private ProgressBar pb;

    private ListAdapter resultSetAdapter;
    private ListView music_list;
    private ArrayList<MusicCategory> mainCategory;
    private int searchCondition;	// 1: 주/부 분류에 의한 검색, 2: 작곡가/장르에 의한 검색, 3: 주제어에 의한 검색
    public static final int MAIN_SUB_CATEGORY = 1;
    public static final int COMPOSER_GENRE = 2;
    public static final int KEYWORD_SEARCH = 3;

    private int mMainPosition;		// Main Category Position
    private int mSearchPosition;	// 占싯삼옙占쏙옙占�Category Position
    private ArrayList<String> subCategoryUuid;		// Uuid 저장
    protected MusicListDBHelper musicDB = null;
    private Cursor mCursor;
    private PlayerManager pm;
    private int mComposerIndex;
    private int mGenreIndex;
    private EditText key_et;
    private boolean spinnerControl = false;
    private ArrayList<BookmarkInfo> infoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        musicDB = new MusicListDBHelper(this);
        Locale lo = Locale.getDefault();
        language = lo.getLanguage();
        pm = PlayerManager.getInstance();
        initActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_category, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//		Log.i(TAG, "CategoryFragment:onOptionsItemSelected(item = " + item.getTitle());

        if(item.getItemId() == R.id.menu_spinner) {
            if(spinnerControl) {
                searchSpin.setVisibility(View.VISIBLE);
                mSearchLayout.setVisibility(View.VISIBLE);
                spinnerControl = false;
                item.setIcon(R.drawable.expander_ic_maximized);
            }
            else {
                searchSpin.setVisibility(View.GONE);
                mSearchLayout.setVisibility(View.GONE);
                spinnerControl = true;
                item.setIcon(R.drawable.expander_ic_minimized);
            }
        }
        else {
//			Log.i(TAG, "menu del all");
            if(pm.getPlayState() != PlayerManager.STOP_STATE) {
                Toast.makeText(this, R.string.guide_on_playing, Toast.LENGTH_SHORT).show();
                return true;
            }
            addMusicItemToPlaylist();
        }
        return true;
    }

    private void addMusicItemToPlaylist() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            final MySimpleCursorAdapter adapter = (MySimpleCursorAdapter)resultSetAdapter;
            infoList = new ArrayList<BookmarkInfo>();
            for(int i = 0;i < mCursor.getCount();i++) {
//			Log.i(TAG, "checked : " + adapter.getBookmarkRequest(i));
                if(adapter.getBookmarkRequest(i)) {	// 즐겨듣기 목록에 추가 요구
                    mCursor.moveToPosition(i);
                    String uuid = mCursor.getString(mCursor.getColumnIndex(MusicListDB.MusicServerInfo.UUID_INFO));
                    String filename = mCursor.getString(mCursor.getColumnIndex(MusicServerInfo.PRODUCT_ID));
                    BookmarkInfo info = new BookmarkInfo(i, uuid, filename);
                    infoList.add(info);
                }
            }
            for(int i = 0;i < infoList.size();i++) {
                BookmarkInfo info = infoList.get(i);
                int index = info.getIndex();
                mCursor.moveToPosition(index);
                long pid = mCursor.getLong(mCursor.getColumnIndex(MusicServerInfo.PRODUCT_ID));
                // 선곡에 대한 정보를 서버로 저장을 할 것인지? 아니면 Play한 것만 저장을 할 것인지?
                String filename = info.getFilename();
                updateBookmarkCompleteStatus(pid);
                updatePlaylistInfo(pid, filename);
                adapter.setBookmarkRequest(i, false);
            }
            infoList.clear();
            mCursor.requery();
            sendBroadcastInformationToLoaded();
            }
        }, 100);
    }

    private void updatePlaylistInfo(long pid, String filename) {
        SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
        ContentValues musicRowValues = new ContentValues();
        musicRowValues = new ContentValues();
        musicRowValues.put(MusicListDB.MusicClientInfo.PRODUCT_ID, pid);
        musicRowValues.put(MusicListDB.MusicClientInfo.FILE_INFO, filename);
        long id = dbHandler.insert(MusicListDB.MusicClientInfo.TABLE_NAME, null, musicRowValues);
    }

    protected void updateBookmarkCompleteStatus(final long serverID) {
        SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
        String w = MusicServerInfo.PRODUCT_ID + "=" + serverID;
        ContentValues musicRowValues = new ContentValues();
        musicRowValues.put(MusicServerInfo.BOOKMARK, 1);
        dbHandler.update(MusicServerInfo.TABLE_NAME, musicRowValues, w, null);
    }

    private void sendBroadcastInformationToLoaded() {
        MusicUtils.saveConfig(this, "playlist_changed", true);
//        Intent broadcast = new Intent(Constants.BROADCAST_ACTION);
//        broadcast.putExtra(Constants.SERVICE_COMMAND, Constants.UPDATED_FAVORITELIST_INFO);
//        sendBroadcast(broadcast);
    }

    private void initActivity() {
        Button btnGenre = (Button)findViewById(R.id.btn_genre_search);
        btnGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMusicByComposerClick(v);
            }
        });
        Button btnKeyword = (Button)findViewById(R.id.btn_keyword);
        btnKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMusicByKeywordClick(v);
            }
        });
        mSearchLayout = (FrameLayout)findViewById(R.id.search_condition);
        mLogicalLayout = (LinearLayout)findViewById(R.id.by_main_sub);
        main_spin = (Spinner)findViewById(R.id.main_spin);
        sub_spin = (Spinner)findViewById(R.id.sub_spin);

        mComposerLayout = (LinearLayout)findViewById(R.id.by_composer);
        ccomposerSpin = (Spinner)findViewById(R.id.ccomposer_spin);
        cgenreSpin = (Spinner)findViewById(R.id.cgenre_spin);

        mKeywordLayout = (LinearLayout)findViewById(R.id.by_keyword);
        searchSpin = (Spinner)findViewById(R.id.search_spin);

        searchSpinControl();
        music_list = (ListView)findViewById(R.id.control_music_list);
        pb = (ProgressBar)findViewById(R.id.progress_large);
    }

    private void mainSubCategoryControl() {
        readMainCategoryList();
        main_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				Log.i(TAG, "main spin onItemSelected(position = " + position + ")");
                mMainPosition = position;		// +1 offset
                sub_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//						Log.i(TAG, "sub spin onItemSelected(position = " + position + ")");
                        mSearchPosition = position;
                        retrieveMusicListFromDB(MAIN_SUB_CATEGORY, mMainPosition, mSearchPosition, null);
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
                subCategoryUuid = new ArrayList<String>();
                final String[] subItems = fillSubItemsFromCategoryList();
                ArrayAdapter<String> bb = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, subItems);
                bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sub_spin.setAdapter(bb);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        final String[] mainItems = fillMainItemsFromCategoryList();
//		mNoMainCategory = mainItems.length;
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mainItems);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        main_spin.setAdapter(aa);
    }

    private String[] fillMainItemsFromCategoryList() {
        String[] result = null;
        // 占쏙옙占쏙옙 resource占쏙옙 占쌍댐옙 占쏙옙占쏙옙 占쏙옙占쏙옙 占쏙옙占�처占쏙옙
        SQLiteDatabase dbHandler = musicDB.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(MainCategoryInfo.TABLE_NAME);

        String columnsToReturn [] = {
                MainCategoryInfo.KO_NAME,
                MainCategoryInfo.EN_NAME
        };

        Cursor cursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, null);
//		Log.i(TAG, "count = " + cursor.getCount());

        if(cursor.getCount() > 0) {
            result = new String[cursor.getCount()];
            int i = 0;
            cursor.moveToFirst();
            do {
                if(language.equals("ko"))
                    result[i++] = cursor.getString(cursor.getColumnIndex(MainCategoryInfo.KO_NAME));
                else
                    result[i++] = cursor.getString(cursor.getColumnIndex(MainCategoryInfo.EN_NAME));
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbHandler.close();

        return result;
    }

    private String[] fillSubItemsFromCategoryList() {
        String[] result = null;
        SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(SubCategoryInfo.TABLE_NAME);
        String w = SubCategoryInfo.M_ID + "=" + getMainCategoryId(mMainPosition);
        queryBuilder.appendWhere(w);

        String columnsToReturn [] = {
                SubCategoryInfo.KO_NAME,
                SubCategoryInfo.EN_NAME,
        };

        Cursor cursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, null);
//		Log.i(TAG, "count = " + cursor.getCount());

        if(cursor.getCount() > 0) {
            result = new String[cursor.getCount()];
            cursor.moveToFirst();
            int i = 0;
            do {
                if(language.equals("ko"))
                    result[i++] = cursor.getString(cursor.getColumnIndex(SubCategoryInfo.KO_NAME));
                else
                    result[i++] = cursor.getString(cursor.getColumnIndex(SubCategoryInfo.EN_NAME));
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbHandler.close();
        return result;
    }

    private void readMainCategoryList() {
        mainCategory = new ArrayList<MusicCategory>();
        SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(MainCategoryInfo.TABLE_NAME);

        String columnsToReturn [] = {
                MainCategoryInfo.C_ID,
        };

        Cursor cursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, null);
//		Log.i(TAG, "count = " + cursor.getCount());

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                MusicCategory category = new MusicCategory();
                category.setCode(cursor.getInt(cursor.getColumnIndex(MainCategoryInfo.C_ID)));
                mainCategory.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbHandler.close();
    }

    private void searchSpinControl() {
        searchSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				Log.d(TAG, "CategoryFragment:searchSpinControl(position = " + position + ")");
                mSearchPosition = position;
                switch(mSearchPosition) {
                    case 0:		// 占싻뤄옙 占쏙옙占쏙옙 占싯삼옙
                        mainSubCategoryControl();
                        visibilityControlOfDetailPart(1, 0, 0);
//					randomMainCategoryControl();
                        break;
                    case 1:
                        searchByComposerGenreControl();
                        visibilityControlOfDetailPart(0, 1, 0);
                        break;
                    case 2:
                        searchByKeywordControl();
                        visibilityControlOfDetailPart(0, 0, 1);
                        break;
                }
            }

            private void visibilityControlOfDetailPart(int i, int j, int k) {
                if(i == 0)	mLogicalLayout.setVisibility(View.GONE);
                else		mLogicalLayout.setVisibility(View.VISIBLE);
                if(j == 0)	mComposerLayout.setVisibility(View.GONE);
                else		mComposerLayout.setVisibility(View.VISIBLE);
                if(k == 0)	mKeywordLayout.setVisibility(View.GONE);
                else		mKeywordLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        final String[] mainItems = getResources().getStringArray(R.array.search_method);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mainItems);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpin.setAdapter(aa);
        if(MusicUtils.getConfigInteger(this, Constants.KEY_DB_VERSION, 0) == 0)
            searchSpin.setSelection(2);
        else
            searchSpin.setSelection(0);
    }

    public void searchMusicByComposerClick(View view) {
        searchCondition = COMPOSER_GENRE;
        retrieveMusicListFromDB(COMPOSER_GENRE, mComposerIndex, mGenreIndex, null);
    }

    public void searchMusicByKeywordClick(View view) {
        if(key_et.length() == 0) {	// None
            Toast.makeText(this, R.string.guide_keyword_input, Toast.LENGTH_LONG).show();
            return;
        }

        closeSoftKeyboard();
        searchCondition = KEYWORD_SEARCH;
        String keyWord = key_et.getText().toString();
        retrieveMusicListFromDB(KEYWORD_SEARCH, 0, 0, keyWord);
    }

    private int getMainCategoryId(int firstIndex) {
        return mainCategory.get(firstIndex).getCode();
    }

    private String getSubCategoryUuid(int index) {
        return subCategoryUuid.get(index);
    }

    private void retrieveMusicListFromDB(int searchMethod, int firstIndex, int secondIndex, String keyWord) {
//		Log.i(TAG, "ContentManagement:retrieveMusicListFromDB(category=" + category + ", keyword=" + keyWord + ")");

        SQLiteDatabase dbHandler = musicDB.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

//		Log.i(TAG, "category = " + category + ", keyword = " + keyWord + ", language = " + language);
        StringBuilder sb = new StringBuilder();
        if(searchMethod == MAIN_SUB_CATEGORY) {			// 占싻뤄옙 占쏙옙占쏙옙 占싯삼옙
            // 160114 by ksseo
            int[] indexMapping = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            queryBuilder.setTables(MusicListDB.MusicContentsInfo.TABLE_NAME + "," + MusicServerInfo.TABLE_NAME);
            int lPid = (indexMapping[firstIndex]+1) * 10000 + (secondIndex+1) * 100;
            int hPid = lPid + 100;

            sb.append(MusicContentsInfo.TABLE_NAME).append(".").append(MusicContentsInfo.L_ID).append(" >= ").append(lPid).append(" AND ")
                    .append(MusicContentsInfo.TABLE_NAME).append(".").append(MusicContentsInfo.L_ID).append(" < ").append(hPid)
                    .append(" AND ").append(MusicContentsInfo.TABLE_NAME).append(".").append(MusicContentsInfo.P_ID)
                    .append(" = ").append(MusicServerInfo.TABLE_NAME).append(".").append(MusicServerInfo.PRODUCT_ID);
        }
        else if (searchMethod == COMPOSER_GENRE) {			// 작곡가/장르
            queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
            String[] composerList = getResources().getStringArray(R.array.composer_category);
            if(language.equals("ko")) {
                sb.append(MusicServerInfo.AUTHOR_KO).append(" LIKE \"%").append(composerList[firstIndex]).append("\"");
            }
            else {
                sb.append(MusicServerInfo.AUTHOR_EN).append(" LIKE \"%").append(composerList[firstIndex]).append("\"");
            }
            if(secondIndex != 0) {
                sb.append(" AND ").append(MusicServerInfo.MUSIC_GENRE).append("=").append(secondIndex);
            }
        }
        else {					// 占싯삼옙占쏘에 占쏙옙占쏙옙 占싯삼옙
            queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
            if(language.equals("ko")) {	// 占쏙옙占쏙옙
                sb.append(MusicServerInfo.TITLE_KO).append(" LIKE \"%").append(keyWord).append("%\"");
            }
            else {	// 占쏙옙占쏙옙
                sb.append(MusicServerInfo.TITLE_EN).append(" LIKE \"%").append(keyWord).append("%\"");
            }
        }
        queryBuilder.appendWhere(sb.toString());

        //占쏙옙占�占쏙옙占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙 占시뤄옙占쏙옙 占싱몌옙占쏙옙(2占쏙옙 占싱삼옙 占쏙옙占싱븝옙占�占쏙옙占쏙옙占�풀占쏙옙占쏙옙占쏙옙 占쌍억옙占�占쏙옙)
        String columnsToReturn [] = {
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PRODUCT_ID,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.TITLE_KO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.AUTHOR_KO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.TITLE_EN,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.AUTHOR_EN,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PLAY_TIME,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.FILE_INFO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.UUID_INFO,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.BOOKMARK,
                MusicServerInfo.TABLE_NAME + "." + MusicServerInfo._ID
        };

        String sortOrder = MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PRODUCT_ID;
        //占쏙옙占쏙옙占�占쏙옙占쏙옙占쏙옙 占쌔댐옙 占쏙옙占�占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙 占승댐옙
        mCursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, sortOrder);

//		Log.i(TAG, "count of cursor = " + mCursor.getCount());

        //占쏙옙占쏙옙 占쏙옙占�占쏙옙占쏙옙占쏙옙 Adapter占쏙옙 Mapping 占쏙옙占쏙옙 占쏙옙占싱아울옙 占쏙옙占�占쏙옙 占쏙옙占�占싼댐옙
        resultSetAdapter = new MySimpleCursorAdapter(
                getApplicationContext(),
                R.layout.listview_category,
                mCursor,
                new String[]{
                        MusicServerInfo.TITLE_KO,
                        MusicServerInfo.AUTHOR_KO,
                        MusicServerInfo.PLAY_TIME,
                        MusicServerInfo.BOOKMARK
                },
                new int[]{
                        R.id.title_content,
                        R.id.author_content,
                        R.id.time_content,
                        R.id.bookmark_cb
                }
        );
        music_list.setAdapter(resultSetAdapter);
    }

    private void searchByComposerGenreControl() {
        ccomposerSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				Log.d(TAG, "ComposerSelection : position = " + position);
                mComposerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        cgenreSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				Log.d(TAG, "GenreSelection : position = " + position);
                mGenreIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void searchByKeywordControl() {
        key_et = (EditText)findViewById(R.id.keyword_input);
    }

    /**
     * Close the on-screen keyboard.
     */
    private void closeSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // 占쏙옙占쏙옙트 占쏙옙占쏙옙 占쌘쏙옙 占썰를 占쏙옙占쏙옙占싹곤옙 표占쏙옙
    public class PEMusicGroupListItemContainer {
        public TextView mMusicName;
        public TextView mAuthorName;
        public TextView mTime;
        public CheckBox mBookmark;
    }

    // Adapter implementation
    public class MySimpleCursorAdapter extends SimpleCursorAdapter {
        private Context mContext;
        private int mLayout;
        private boolean[] bookmarkRequest;

        public MySimpleCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
            super(context, layout, cursor, from, to);
//			Log.i(TAG, "ContentManagement:MySimpleCursorAdapter()");
            mContext = context;
            mLayout = layout;
            bookmarkRequest = new boolean[mCursor.getCount()];
        }

        public void resetBookmarkRequestInformation() {
            for(int i = 0;i < mCursor.getCount();i++) {
                bookmarkRequest[i] = false;
            }
        }

        public void setBookmarkRequest(int index, boolean flag) {
            bookmarkRequest[index] = flag;
        }

        public boolean getBookmarkRequest(int index) {
            return bookmarkRequest[index];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
//			Log.i(TAG, "getView start(position = " + position + ", convertView = " + convertView + ")");
            Cursor cursor = getCursor();

            PEMusicGroupListItemContainer musicListItem;

            if(convertView == null) {
                LayoutInflater inflate = LayoutInflater.from(mContext);
                convertView = (View)inflate.inflate(mLayout, null);
                musicListItem = new PEMusicGroupListItemContainer();

                musicListItem.mMusicName = (TextView)convertView.findViewById(R.id.title_content);
                musicListItem.mAuthorName = (TextView)convertView.findViewById(R.id.author_content);
                musicListItem.mTime = (TextView)convertView.findViewById(R.id.time_content);
                musicListItem.mBookmark = (CheckBox)convertView.findViewById(R.id.bookmark_cb);
                convertView.setTag(musicListItem);
            }
            else {
                musicListItem = (PEMusicGroupListItemContainer) convertView.getTag();
            }

            cursor.moveToPosition(position);

            if(!language.equals("ko")) {	// English
                musicListItem.mMusicName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.TITLE_EN)));
                musicListItem.mAuthorName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.AUTHOR_EN)));
            }
            else {							// Korean
                musicListItem.mMusicName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.TITLE_KO)));
                musicListItem.mAuthorName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.AUTHOR_KO)));
            }
            musicListItem.mTime.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.PLAY_TIME)));
            if(cursor.getInt(cursor.getColumnIndex(MusicServerInfo.BOOKMARK)) > 0 || getBookmarkRequest(position)) {
//				musicListItem.mBookmark.setVisibility(View.GONE);
                musicListItem.mBookmark.setChecked(true);
                musicListItem.mBookmark.setClickable(false);
            }
            else {
                musicListItem.mBookmark.setChecked(false);
                musicListItem.mBookmark.setClickable(true);
                musicListItem.mBookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int position = (int)v.getTag();
                        CheckBox cb = (CheckBox)v;
                        setBookmarkRequest(position, cb.isChecked());
                    }
                });
            }
            musicListItem.mBookmark.setTag(position);

//			Log.i(TAG, "getView end(position = " + position + ", pid = " + cursor.getLong(0) + ")");
            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constants.TAG, "CategoryActivity:onDestroy()");

        if( musicDB != null){
            musicDB.close();
        }
    }
}
